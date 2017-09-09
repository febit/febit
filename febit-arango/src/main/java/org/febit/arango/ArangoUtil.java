/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.arango;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.util.MapBuilder;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.febit.arango.meta.Arango;
import org.febit.arango.meta.ArangoId;
import org.febit.arango.meta.ArangoIgnore;
import org.febit.arango.meta.ArangoLink;
import org.febit.bean.FieldInfo;
import org.febit.bean.FieldInfoResolver;
import org.febit.lang.ConcurrentIdentityMap;
import org.febit.service.Services;
import org.febit.util.ArraysUtil;
import org.febit.util.ClassUtil;
import org.febit.util.CollectionUtil;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class ArangoUtil {

    private static final ArangoLinkInfo[] EMPTY_LINKS = new ArangoLinkInfo[0];

    private static final ConcurrentIdentityMap<String[]> BEAN_FIELDS_CACHING = new ConcurrentIdentityMap<>(64);
    private static final ConcurrentIdentityMap<ArangoLinkInfo[]> LINKS_CACHING = new ConcurrentIdentityMap<>(64);

    private static class Holder {

        static final SecureRandom RANDOM = new SecureRandom();
    }

    public static String randomKey() {
        SecureRandom random = Holder.RANDOM;
        byte[] randomBytes = new byte[16];
        random.nextBytes(randomBytes);
        return StringUtil.hex(randomBytes);
    }

    public static MapBuilder map() {
        return new MapBuilder();
    }

    public static Map<String, Object> vars(String key, Object value) {
        return map().put(key, value).get();
    }

    public static Map<String, Object> vars(
            String key, Object value,
            String key2, Object value2
    ) {
        return map()
                .put(key, value)
                .put(key2, value2)
                .get();
    }

    public static Map<String, Object> vars(
            String key, Object value,
            String key2, Object value2,
            String key3, Object value3
    ) {
        return map()
                .put(key, value)
                .put(key2, value2)
                .put(key3, value3)
                .get();
    }

    public static String[] fields(Class type) {
        String[] ret = BEAN_FIELDS_CACHING.unsafeGet(type);
        if (ret != null) {
            return ret;
        }
        return resolveFieldsIfAbsent(type);
    }

    protected static synchronized String[] resolveFieldsIfAbsent(Class type) {
        FieldInfo[] fieldInfos = new ArangoFieldInfoResolver(type).resolveAndSort();
        ArrayList<String> settableFields = new ArrayList<>(fieldInfos.length);
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.isSettable()) {
                settableFields.add(fieldInfo.name);
            }
        }
        String[] ret = ArraysUtil.exportStringArray(settableFields);
        return BEAN_FIELDS_CACHING.putIfAbsent(type, ret);
    }

    public static <T> T[] readToArray(ArangoCursor<T> cursor, Class<T> clazz) {
        return readToArray(cursor.asListRemaining(), clazz);
    }

    public static <T> T[] readToArray(List<T> list, Class<T> clazz) {
        return list.toArray((T[]) Array.newInstance(clazz, list.size()));
    }

    private static ArangoLinkInfo[] collectInfosIfAbsent(Class type) {
        ArangoLinkInfo[] infos = LINKS_CACHING.get(type);
        if (infos != null) {
            return infos;
        }

        final List<ArangoLinkInfo> list = new ArrayList<>();
        for (Field field : ClassUtil.getAccessableMemberFields(type)) {
            if (field.getType() != String.class) {
                continue;
            }
            ArangoLink linkAnno = field.getAnnotation(ArangoLink.class);
            if (linkAnno == null) {
                continue;
            }
            ClassUtil.setAccessible(field);
            Class<Entity> linkType = linkAnno.value();
            Class<?> serviceClass = linkAnno.service() != Object.class ? linkAnno.service()
                    : linkType.getAnnotation(ArangoLink.class).value();

            list.add(new ArangoLinkInfo(((ArangoService) Services.get(serviceClass)).getDao(), field, linkType));
        }
        infos = list.isEmpty() ? EMPTY_LINKS : list.toArray(new ArangoLinkInfo[list.size()]);
        return LINKS_CACHING.putIfAbsent(type, infos);
    }

    public static Map<String, Map<String, Entity>> resolveLinks(Collection<?> rows, Class type) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        ArangoLinkInfo[] infos = LINKS_CACHING.unsafeGet(type);
        if (infos == null) {
            infos = collectInfosIfAbsent(type);
        }
        if (infos.length == 0) {
            return null;
        }
        final Map<String, Map<String, Entity>> ret = CollectionUtil.createMap(infos.length);

        final HashSet<String> ids = new HashSet<>(rows.size());
        for (ArangoLinkInfo info : infos) {

            //collect ids
            ids.clear();
            final Field field = info.field;
            for (Object row : rows) {
                try {
                    String id = (String) field.get(row);
                    if (id != null) {
                        ids.add(id);
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (ids.isEmpty()) {
                continue;
            }

            //fetch entitys
            Map<String, Entity> map = CollectionUtil.createMap(ids.size());
            Map<String, Object> vars = new HashMap<>(4);
            vars.put("keys", ids);
            ArangoCursor<Entity> iterator = info.dao.cursor(
                    "FOR d in " + info.dao.getTableName() + ""
                    + " FILTER d._key in @keys "
                    + " RETURN d",
                    vars, null, info.linkType);

            while (iterator.hasNext()) {
                Entity next = iterator.next();
                map.put(next.id(), next);
            }
            ret.put(info.name, map);
        }

        return ret;
    }

    public static long nextSeq(ArangoDatabase db, String key) {
        String aql = "UPSERT { _key: @_key } \n" + " INSERT {  _key: @_key, next: 1, created: DATE_NOW() } \n" + " UPDATE { next: OLD.next + 1 } IN sys_seq\n" + " RETURN NEW.next";
        ArangoCursor<Long> cursor = db.query(aql, vars("_key", key), null, Long.class);
        if (!cursor.hasNext()) {
            throw new ArangoDBException("Need next!");
        }
        return cursor.next();
    }

    static class ArangoLinkInfo {

        public final ArangoDao dao;
        public final String name;
        public final Field field;
        public final Class<Entity> linkType;

        public ArangoLinkInfo(ArangoDao dao, Field field, Class linkType) {
            this.dao = dao;
            this.field = field;
            this.name = field.getName();
            this.linkType = linkType;
        }
    }

    protected static class ArangoFieldInfoResolver extends FieldInfoResolver {

        public ArangoFieldInfoResolver(Class beanType) {
            super(beanType);
        }

        @Override
        public int compare(FieldInfo o1, FieldInfo o2) {
            String name1 = o1.name;
            String name2 = o2.name;
            if (name1.equals("_key")) {
                return name2.equals("_key") ? 0 : -1;
            }
            if (name2.equals("_key")) {
                return 1;
            }
            return name1.compareTo(name2);
        }

        @Override
        protected String formatName(String name) {
            return fixFieldName(name);
        }

        @Override
        protected void registSetterMethod(String name, Method method) {
            if (method.getAnnotation(Arango.class) == null) {
                return;
            }
            super.registSetterMethod(name, method);
        }

        @Override
        protected void registGetterMethod(String name, Method method) {
            if (method.getAnnotation(Arango.class) == null) {
                return;
            }
            super.registGetterMethod(name, method);
        }

        @Override
        protected void registField(String name, Field field) {
            if (ClassUtil.isTransient(field)) {
                return;
            }
            if (field.getAnnotation(ArangoIgnore.class) != null) {
                return;
            }
            if (field.getAnnotation(ArangoId.class) != null) {
                super.registField("_key", field);
                return;
            }
            super.registField(name, field);
        }
    }

    public static String fixFieldName(String name) {
        switch (name) {
            case "_id":
            case "_key":
            case "id":
            case "key":
                return "_key";
            default:
        }
        return name;
    }
}
