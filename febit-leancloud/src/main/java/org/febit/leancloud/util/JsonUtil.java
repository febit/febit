/**
 * Copyright 2013 febit.org (support@febit.org)
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
package org.febit.leancloud.util;

import jodd.introspector.Getter;
import jodd.introspector.PropertyDescriptor;
import jodd.json.JsonContext;
import jodd.json.JsonException;
import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import jodd.json.TypeJsonSerializer;
import jodd.json.TypeJsonVisitor;
import jodd.json.impl.ValueJsonSerializer;
import org.febit.leancloud.Entity;

/**
 *
 * @author zqq90
 */
public class JsonUtil {

    private static final JsonSerializer JSON_SERIALIZER;

    static {
        JSON_SERIALIZER = JsonSerializer.create()
                .setClassMetadataName(null)
                .withSerializer(Entity.class, new EntityJsonSerializer())
                .deep(true);
    }

    protected static class EntityJsonSerializer extends ValueJsonSerializer<Entity> {

        @Override
        public void serializeValue(final JsonContext jsonContext, Entity value) {
            jsonContext.writeOpenObject();
            new EntitySerializer(jsonContext, value).visit();
            jsonContext.writeCloseObject();
        }
    }

    protected static class EntitySerializer extends TypeJsonVisitor {

        protected final Entity source;

        public EntitySerializer(JsonContext jsonContext, Entity bean) {
            super(jsonContext, bean.getClass());
            this.source = bean;
        }

        @Override
        protected final void onSerializableProperty(String propertyName, PropertyDescriptor propertyDescriptor) {
            if (propertyDescriptor == null) {
                // skip metadata - classname
                return;
            }

            propertyName = typeData.resolveJsonName(propertyName);
            // exclude createdAt and updatedAt
            if ("createdAt".equals(propertyName)
                    || "updatedAt".equals(propertyName)) {
                return;
            }

            Getter getter = propertyDescriptor.getGetter(declared);
            if (getter == null) {
                return;
            }
            Object value;
            try {
                value = getter.invokeGetter(source);
            } catch (Exception ex) {
                throw new JsonException(ex);
            }
            if (value == null
                    && ("objectId".equals(propertyName)
                    || "ACL".equals(propertyName))) {
                return;
            }
            jsonContext.pushName(propertyName, count > 0);
            jsonContext.serialize(value);
            if (jsonContext.isNamePopped()) {
                count++;
            }
        }
    }

    public static void registerSerializer(Class type, TypeJsonSerializer typeJsonSerializer) {
        JSON_SERIALIZER.withSerializer(type, typeJsonSerializer);
    }

    public static String toJsonString(Object obj) {
        return JSON_SERIALIZER.serialize(obj);
    }

    public static String toJsonString(Object obj, String... excludes) {
        return JSON_SERIALIZER.exclude(excludes).serialize(obj);
    }

    public static <T> T parseJson(String input, Class<T> targetType) {
        return JsonParser.create()
                .parse(input, targetType);
    }

    public static <T> T parseJson(String input, Class<T> targetType, String mapPath, Class mapType) {
        return JsonParser.create()
                .map(mapPath, mapType)
                .parse(input, targetType);
    }
}
