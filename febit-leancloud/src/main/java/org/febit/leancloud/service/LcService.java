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
package org.febit.leancloud.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.febit.form.AddForm;
import org.febit.form.ModifyForm;
import org.febit.form.PageForm;
import org.febit.leancloud.Condition;
import org.febit.leancloud.Entity;
import org.febit.leancloud.LcException;
import org.febit.leancloud.LcQuery;
import org.febit.leancloud.client.LcApiClient;
import org.febit.leancloud.client.LcCreateResponse;
import org.febit.leancloud.client.LcDeleteResponse;
import org.febit.leancloud.client.LcFindResponse;
import org.febit.leancloud.client.LcQueryResponse;
import org.febit.leancloud.client.LcUpdateResponse;
import org.febit.leancloud.util.JsonUtil;
import org.febit.service.PageResult;
import org.febit.service.Service;
import org.febit.service.ServiceResult;
import org.febit.service.Services;
import org.febit.util.ClassUtil;
import org.febit.util.Petite;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 * @param <E>
 */
@SuppressWarnings("unchecked")
public abstract class LcService<E extends Entity> implements Service {

    public static final ServiceResult SUCCESS = ServiceResult.SUCCESS_RESULT;

    protected static final String[] DESC_CREATE = {"-createdAt"};
    protected static final String[] FILEDS_ID = {"objectId"};
    protected String _table;
    protected Class<E> _entityType;
    protected LcApiClient _client;

    @Petite.Init
    protected void init() {
        LcInitService initService = Services.get(LcInitService.class);
        _entityType = (Class<E>) ClassUtil.getRawType(LcService.class.getTypeParameters()[0], getClass());
        _table = LcApiClient.getEntityTableName(_entityType);
        _client = initService.getLcClient(_entityType);
    }

    public E find(String id) {
        return find(id, (String[]) null);
    }

    public E find(String id, String... keys) {
        if (id == null) {
            return null;
        }
        LcFindResponse<E> response = _client.find(id, _entityType, keys);
        if (!response.isOk()) {
            return handleFailedFindResponse(response, id);
        }
        return response.getResult();
    }

    public <T extends Entity> T find(String id, Class<T> destType) {
        if (id == null) {
            return null;
        }
        // TODO: resolve keys
        LcFindResponse<T> response = _client.find(id, _table, destType, null);
        if (!response.isOk()) {
            return handleFailedFindResponse(response, id);
        }
        return response.getResult();
    }

    public E find(LcQuery query) {
        query.limit(1);
        List<E> list = list(query);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean isExist(LcQuery query) {
        query.keys(FILEDS_ID);
        return find(query) != null;
    }

    public boolean isExist(String id) {
        return find(id, FILEDS_ID) != null;
    }

    public E findBy(String field, Object value) {
        LcQuery query = LcQuery.create()
                .where()
                .equal(field, value)
                .popQuery();
        return find(query);
    }

    public E findBy(
            String field, Object value,
            String field2, Object value2
    ) {
        LcQuery query = LcQuery.create()
                .where()
                .equal(field, value)
                .equal(field2, value2)
                .popQuery();
        return find(query);
    }

    public List<E> listBy(String field, Object value) {
        LcQuery query = LcQuery.create()
                .where()
                .equal(field, value)
                .popQuery();
        return list(query);
    }

    public List<E> listBy(
            String field, Object value,
            String field2, Object value2
    ) {
        LcQuery query = LcQuery.create()
                .where()
                .equal(field, value)
                .equal(field2, value2)
                .popQuery();
        return list(query);
    }

    public List<E> list(LcQuery query) {
        LcQueryResponse<E> response = _client.query(query, _entityType);
        return getResultList(response, query);
    }

    public <T extends Entity> List<E> list(LcQuery query, Class<T> destType) {
        LcQueryResponse<E> response = _client.query(query, _table, destType);
        return getResultList(response, query);
    }

    protected void resolveOrder(LcQuery query, PageForm pageForm) {

    }

    public PageResult page(LcSearchForm form, PageForm pageForm) {
        return page(form, pageForm, _entityType);
    }

    public <T extends Entity> PageResult page(LcSearchForm form, PageForm pageForm, Class<T> destType) {
        LcPageResult pageResult = new LcPageResult(pageForm);
        LcQuery query = LcQuery.create();
        query.page(pageForm);
        resolveOrder(query, pageForm);
        if (form != null) {
            form.appendTo(query.where());
        }
        page(query, pageResult, destType);
        return pageResult;
    }

    public void page(LcQuery query, PageResult pageResult) {
        page(query, pageResult, _entityType);
    }

    public <T extends Entity> void page(LcQuery query, PageResult pageResult, Class<T> destType) {
        query.setCount(true);
        LcQueryResponse<E> response = _client.query(query, _table, destType != null ? destType : _entityType);
        pageResult.setResults(getResultList(response, query));
        pageResult.setTotalSize(response.getCount());
    }

    public boolean delete(String id) {
        LcDeleteResponse response = _client.delete(id, _entityType);
        return response.isOk();
    }

    public boolean delete(String id, Condition where) {
        LcDeleteResponse response = _client.delete(id, _entityType, where);
        return response.isOk();
    }

    public boolean save(E entity) {
        LcCreateResponse response = _client.save(entity);
        if (response.isOk()) {
            entity.id(response.getObjectId());
            entity.setCreatedAt(response.getCreatedAt());
            entity.setUpdatedAt(response.getCreatedAt());  //Note: not typo: updatedAt = createdAt
        }
        return response.isOk();
    }

    public boolean update(E entity) {
        LcUpdateResponse response = _client.update(entity);
        // return real reason
        if (response.isOk()) {
            entity.setUpdatedAt(response.getUpdatedAt());
        }
        return response.isOk();
    }

    public boolean update(E entity, Condition where) {
        LcUpdateResponse response = _client.update(entity, where);
        return response.isOk();
    }

    public boolean update(String id, Condition op, Condition where) {
        LcUpdateResponse response = _client.update(id, _entityType, op, where);
        return response.isOk();
    }

    public boolean update(String id, Map<String, ?> changes) {
        LcUpdateResponse response = _client.update(id, _entityType, changes);
        return response.isOk();
    }

    public ServiceResult add(AddForm<E> form, int profile) {
        E entity = form.createAdded(profile);
        boolean isOk = save(entity);
        return isOk ? _success(extractAutoGenerated(entity)) : _error(ServiceResult.ERROR_ADD);
    }

    public ServiceResult modify(ModifyForm<E, String> form, int profile) {
        String id = form.id();
        if (id == null) {
            return _error(ServiceResult.ERROR_MODIFY_NOTFOUND);
        }
        Map<String, Object> map = form.modifyMap(profile);
        boolean isOk = update(id, map);
        return isOk ? SUCCESS : _error(ServiceResult.ERROR_MODIFY_NOTFOUND);
    }

    protected Map<String, Object> extractAutoGenerated(E entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.id());
        return map;
    }

    protected List<E> getResultList(LcQueryResponse<E> response, LcQuery query) {
        if (!response.isOk()) {
            return handleFailedQueryResponse(response, query);
        }
        List<E> results = response.getResults();
        return results != null ? results : Collections.EMPTY_LIST;
    }

    protected <T extends Entity> T handleFailedFindResponse(LcFindResponse<T> response, String id) {
        if (response.getStatusCode() == 404
                && response.getCode() == 101) {
            return null;
        }
        throw new LcException(StringUtil.format(
                "Failed to query table: {}, response {}, where id={}",
                _table, response, id
        ));
    }

    protected <T extends Entity> List<T> handleFailedQueryResponse(LcQueryResponse<T> response, LcQuery query) {
        if (response.getStatusCode() == 404
                && response.getCode() == 101) {
            return Collections.EMPTY_LIST;
        }
        throw new LcException(StringUtil.format(
                "Failed to query table: {}, response {}, where {}",
                _table, response, JsonUtil.toJsonString(query)
        ));
    }

    protected static ServiceResult _success(Object result) {
        return ServiceResult.success(result);
    }

    protected static ServiceResult _error(int code) {
        return ServiceResult.error(code);
    }

    protected static ServiceResult _error(String message) {
        return ServiceResult.error(message);
    }

    protected static ServiceResult _error(String message, Object... arguments) {
        return ServiceResult.error(message, arguments);
    }

    protected static ServiceResult _error(int code, String message) {
        return ServiceResult.error(code, message);
    }

    protected static ServiceResult _error(int code, String message, Object... arguments) {
        return ServiceResult.error(code, message, arguments);
    }

}
