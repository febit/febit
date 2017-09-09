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

import com.arangodb.util.MapBuilder;
import java.util.List;
import java.util.Map;
import jodd.util.ReflectUtil;
import org.febit.form.AddForm;
import org.febit.form.ModifyForm;
import org.febit.form.PageForm;
import org.febit.service.PageResult;
import org.febit.service.Service;
import org.febit.service.ServiceResult;
import org.febit.service.Services;
import org.febit.util.Petite;

/**
 *
 * @author zqq90
 * @param <E>
 */
public abstract class ArangoService<E extends Entity> implements Service {

    protected static final ServiceResult SUCCESS = ServiceResult.SUCCESS_RESULT;

    protected ArangoDao<E> dao;

    @Petite.Init
    protected void initDao() {
        Class<E> entityType = (Class<E>) ReflectUtil.getRawType(ArangoService.class.getTypeParameters()[0], getClass());
        this.dao = Services.get(ArangoInitService.class).createDao(entityType);
    }

    public boolean getBoolById(String key, String field) {
        Boolean ret = dao.findXById(key, field, Boolean.class);
        if (ret == null) {
            return false;
        }
        return ret;
    }

    public Integer getIntById(String key, String field, Integer defaultValue) {
        Integer ret = dao.findXById(key, field, Integer.class);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    public Long getLongById(String key, String field, Long defaultValue) {
        Long ret = dao.findXById(key, field, Long.class);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    public String getStringById(String key, String field, String defaultValue) {
        String ret = dao.findXById(key, field, String.class);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    public boolean updateXbyId(String name, Object value, String key) {
        return dao.updateXById(key, name, value);
    }

    public ServiceResult add(AddForm<E> form, int profile) {
        E entity = form.createAdded(profile);
        add(entity);
        return _success(entity.id());
    }

    public void add(E entity) {
        dao.save(entity);
    }

    public boolean modify(E entity) {
        dao.update(entity);
        return true;
    }

    public ServiceResult modify(ModifyForm<E, String> form, int profile) {
        String id = form.id();
        if (id == null) {
            return _error(ServiceResult.ERROR_MODIFY_NOTFOUND);
        }
        Map<String, Object> map = form.modifyMap(profile);
        if (modify(id, map)) {
            return SUCCESS;
        }
        return _error(ServiceResult.ERROR_MODIFY_NOTFOUND);
    }

    public boolean modify(String key, Map<String, Object> map) {
        return dao.update(key, map);
    }

    protected void resolveOrder(Condition condition, PageForm pageForm) {

    }

    public PageResult page(ArangoSearchForm form, PageForm pageForm, Class type) {
        ArangoPageResult pageResult = new ArangoPageResult(pageForm);
        Condition condition = c();
        resolveOrder(condition, pageForm);
        if (form != null) {
            form.appendTo(condition);
        }
        dao.page(condition, pageResult, type);
        return pageResult;
    }

    public void page(Condition query, PageResult pageResult, Class type) {
        dao.page(query, pageResult, type);
    }

    public List<E> list(ArangoSearchForm form) {
        return dao.list(form.appendTo(c()));
    }

    public Object delete(String key) {
        dao.delete(key);
        return SUCCESS;
    }

    public Object delete(String[] keys) {
        dao.delete(keys);
        return SUCCESS;
    }

    public int delete(Condition query) {
        return dao.delete(query);
    }

    public <T> T find(Condition query, Class<T> clazz) {
        return dao.find(query, clazz);
    }

    public <T> T find(String key, Class<T> type) {
        return dao.find(key, type);
    }

    public E find(String key) {
        return dao.find(key);
    }

    public E[] find(String[] keys) {
        return dao.find(keys);
    }

    public <T> T[] find(String[] keys, Class<T> type) {
        return dao.find(keys, type);
    }

    public <T> T findXByY(String x, String y, Object value, Class<T> type) {
        return dao.queryFieldFirst(Condition.c().equal(x, value), y, type);
    }

    public <T> List<T> list(Class<T> type) {
        return dao.list(c(), type);
    }

    public <T> List<T> list(Condition query, Class<T> type) {
        return dao.list(query, type);
    }

    public ArangoDao<E> getDao() {
        return dao;
    }

    protected static <T> ServiceResult<T> _success() {
        return ServiceResult.success(null);
    }

    protected static <T> ServiceResult<T> _success(T result) {
        return ServiceResult.success(result);
    }

    protected static ServiceResult _error(int code) {
        return ServiceResult.error(code);
    }

    protected static ServiceResult _error(String message, Object... arguments) {
        return ServiceResult.error(message, arguments);
    }

    protected static ServiceResult _error(int code, String message, Object... arguments) {
        return ServiceResult.error(code, message, arguments);
    }

    protected static Condition c() {
        return Condition.c();
    }

    protected static Condition c(String key, Object value) {
        return Condition.c(key, value);
    }

    protected static Condition c(
            String key, Object value,
            String key2, Object value2
    ) {
        return Condition.c(key, value, key2, value2);
    }

    protected static Condition c(
            String key, Object value,
            String key2, Object value2,
            String key3, Object value3
    ) {
        return Condition.c(key, value, key2, value2, key3, value3);
    }

    protected static Map<String, Object> byId(String id) {
        return vars("_key", id);
    }

    protected static MapBuilder map() {
        return ArangoUtil.map();
    }

    protected static Map<String, Object> vars(String key, Object value) {
        return ArangoUtil.vars(key, value);
    }

    protected static Map<String, Object> vars(
            String key, Object value,
            String key2, Object value2
    ) {
        return ArangoUtil.vars(key, value, key2, value2);
    }

    protected static Map<String, Object> vars(
            String key, Object value,
            String key2, Object value2,
            String key3, Object value3
    ) {
        return ArangoUtil.vars(key, value, key2, value2, key3, value3);
    }
}
