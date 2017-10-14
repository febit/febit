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
package org.febit.leancloud.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.febit.easyokhttp.EasyRequest;
import org.febit.lang.ConcurrentIdentityMap;
import org.febit.leancloud.Condition;
import org.febit.leancloud.Entity;
import org.febit.leancloud.LcQuery;
import org.febit.leancloud.util.JsonUtil;
import org.febit.leancloud.meta.LcTable;
import org.febit.util.EncryptUtil;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 * @param <C>
 */
public class LcApiClient<C extends LcApiClient> implements Closeable, AutoCloseable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LcApiClient.class);

    public static final int MAX_REDIRECT = 100;
    public static final String HEAD_X_LC_ID = "X-LC-Id";
    public static final String HEAD_X_LC_SIGN = "X-LC-Sign";
    public static final String HEAD_X_LC_SESSION = "X-LC-Session";
    public static final String HEAD_ACCEPT = "Accept";
    public static final String APPLICATION_JSON = "application/json";

    private static final ConcurrentIdentityMap<String> TABLE_NAME_CACHE = new ConcurrentIdentityMap<>(64);

    public static Builder<Builder, LcApiClient> builder() {
        return new Builder<Builder, LcApiClient>() {
            @Override
            public LcApiClient build() {
                return new LcApiClient(this);
            }
        };
    }

    public static boolean isRequestFailed(int statusCode) {
        return statusCode < 200 || statusCode >= 300;
    }

    public static <E extends Entity> String getEntityTableName(E entity) {
        return getEntityTableName(entity.getClass());
    }

    public static <E extends Entity> String getEntityTableName(Class<E> entityType) {
        String name = TABLE_NAME_CACHE.unsafeGet(entityType);
        if (name != null) {
            return name;
        }
        return TABLE_NAME_CACHE.putIfAbsent(entityType, _resolveTableName(entityType));
    }

    protected static <E extends Entity> String _resolveTableName(Class<E> entityType) {
        String tableName = null;
        if (tableName == null || tableName.isEmpty()) {
            LcTable mongoAnno = entityType.getAnnotation(LcTable.class);
            if (mongoAnno != null) {
                tableName = mongoAnno.value();
            }
        }
        if (tableName == null || tableName.isEmpty()) {
            tableName = entityType.getSimpleName();
            LOG.warn("Lc Entity for '{}' used class name '{}' as table name.", entityType.getName(), tableName);
        }
        return tableName;
    }

    protected final Headers.Builder defaultHeaders;
    protected final OkHttpClient okHttpClient;

    // -----> sign keys
    protected String _authSign = null;
    protected final String appId;
    protected final String appKey;
    protected final String masterKey;

    // ----> remote settings
    protected final String scheme;
    protected final int port;
    protected final String apiHost;
    protected final String apiVersion;

    public LcApiClient(Builder builder) {
        this.appId = builder.appId;
        if (appId == null) {
            throw new IllegalStateException("appId is required!");
        }
        this.appKey = builder.appKey;
        this.masterKey = builder.masterKey;
        this.scheme = builder.scheme;
        this.port = builder.port;
        this.apiHost = builder.apiHost;
        this.apiVersion = builder.apiVersion;
        this.okHttpClient = builder.httpClientBuilder.build();
        this.defaultHeaders = builder.defaultHeaders.build().newBuilder();
        this.defaultHeaders.set(HEAD_X_LC_ID, appId);
    }

    public <T extends Entity> LcFindResponse<T> find(String id, Class<T> entityType) {
        return find(id, entityType, null);
    }

    public <T extends Entity> LcFindResponse<T> find(String id, Class<T> entityType, String[] keys) {
        if (StringUtil.isEmpty(id)) {
            throw new IllegalArgumentException("id is required");
        }
        String table = getEntityTableName(entityType);
        EasyRequest request = new EasyRequest()
                .get()
                .addPathSegment(apiVersion)
                .addPathSegment("classes")
                .addPathSegment(table)
                .addPathSegment(id);
        if (keys != null && keys.length != 0) {
            request.query("keys", StringUtil.join(keys, ','));
        }
        return readFindResponse(sendRequest(request), entityType);
    }

    public <T extends Entity> LcQueryResponse<T> query(LcQuery query, Class<T> entityType) {
        String table = getEntityTableName(entityType);
        EasyRequest request = new EasyRequest()
                .get()
                .addPathSegment(apiVersion)
                .addPathSegment("classes")
                .addPathSegment(table);

        // http querys:
        if (query.isCount()) {
            request.query("count", "1");
        }
        if (query.getLimit() != null) {
            request.query("limit", query.getLimit());
        }
        if (query.getSkip() != null) {
            request.query("skip", query.getSkip());
        }
        if (query.getInclude() != null) {
            request.query("include", query.getInclude());
        }
        String[] order = query.getOrder();
        if (order != null && order.length != 0) {
            request.query("order", StringUtil.join(order, ','));
        }
        String[] keys = query.getKeys();
        if (keys != null && keys.length != 0) {
            request.query("keys", StringUtil.join(keys, ','));
        }
        Condition where = query.getWhere();
        if (where != null) {
            request.query("where", JsonUtil.toJsonString(where));
        }
        return readQueryResponse(sendRequest(request), entityType);
    }

    public <T extends Entity> LcDeleteResponse delete(String id, Class<T> entityType) {
        if (StringUtil.isEmpty(id)) {
            throw new IllegalArgumentException("id is required");
        }
        String table = getEntityTableName(entityType);
        EasyRequest request = new EasyRequest()
                .delete()
                .addPathSegment(apiVersion)
                .addPathSegment("classes")
                .addPathSegment(table)
                .addPathSegment(id);
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcDeleteResponse delete(String id, Class<T> entityType, Condition where) {
        if (StringUtil.isEmpty(id)) {
            throw new IllegalArgumentException("id is required");
        }
        String table = getEntityTableName(entityType);
        EasyRequest request = new EasyRequest()
                .delete()
                .addPathSegment(apiVersion)
                .addPathSegment("classes")
                .addPathSegment(table);
        if (id != null) {
            request.addPathSegment(id);
        }
        if (where != null) {
            request.query("where", JsonUtil.toJsonString(where));
        }
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcCreateResponse save(T entity) {
        String table = getEntityTableName(entity);
        EasyRequest request = new EasyRequest()
                .post()
                .addPathSegment(apiVersion)
                .addPathSegment("classes")
                .addPathSegment(table);
        setJsonBody(request, entity);
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcUpdateResponse update(T entity) {
        if (!entity._isPersistent()) {
            throw new IllegalArgumentException("entity.id is required");
        }
        String table = getEntityTableName(entity);
        EasyRequest request = new EasyRequest()
                .put()
                .addPathSegment(apiVersion)
                .addPathSegment("classes")
                .addPathSegment(table)
                .addPathSegment(entity.id());
        setJsonBody(request, entity, "objectId");
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcUpdateResponse update(String id, Class<T> entityType, Map<String, ?> changes) {
        if (StringUtil.isEmpty(id)) {
            throw new IllegalArgumentException("id is required");
        }
        String table = getEntityTableName(entityType);
        EasyRequest request = new EasyRequest()
                .put()
                .addPathSegment(apiVersion)
                .addPathSegment("classes")
                .addPathSegment(table)
                .addPathSegment(id);
        setJsonBody(request, changes);
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcUpdateResponse update(T entity, Condition where) {
        String table = getEntityTableName(entity);
        EasyRequest request = new EasyRequest()
                .put()
                .addPathSegment(apiVersion)
                .addPathSegment("classes")
                .addPathSegment(table);
        if (entity._isPersistent()) {
            request.addPathSegment(entity.id());
        }
        if (where != null) {
            request.query("where", JsonUtil.toJsonString(where));
        }
        setJsonBody(request, entity, "objectId");
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcUpdateResponse update(String id, Class<T> entityType, Condition op, Condition where) {
        String table = getEntityTableName(entityType);
        EasyRequest request = new EasyRequest()
                .put()
                .addPathSegment(apiVersion)
                .addPathSegment("classes")
                .addPathSegment(table);
        if (id != null) {
            request.addPathSegment(id);
        }
        if (where != null) {
            request.query("where", JsonUtil.toJsonString(where));
        }
        setJsonBody(request, op);
        return readBasicResponse(sendRequest(request));
    }

    protected EasyRequest setJsonBody(EasyRequest request, Object obj) {
        return request.jsonBody(JsonUtil.toJsonString(obj));
    }

    protected EasyRequest setJsonBody(EasyRequest request, Object obj, String... excludes) {
        return request.jsonBody(JsonUtil.toJsonString(obj, excludes));
    }

    public void resetAppSign() {
        this._authSign = null;
    }

    protected String resolveAuthSign() {
        long now = getTimestamp();
        if (masterKey != null) {
            return EncryptUtil.md5(now + masterKey) + ',' + now + ",master";
        } else if (appKey != null) {
            return EncryptUtil.md5(now + appKey) + ',' + now;
        }
        throw new IllegalStateException("masterKey/appKey is required!");
    }

    protected <E extends Entity> LcFindResponse<E> readFindResponse(Response httpResponse, Class<E> entityType) {
        if (isRequestFailed(httpResponse.code())) {
            return readResponse(httpResponse, LcFindResponseImpl.class);
        }
        E entity;
        try {
            entity = JsonUtil.parseJson(httpResponse.body().string(), entityType);
        } catch (Exception e) {
            LOG.error("Failed to parse response", e);
            return readResponse(503, null, LcFindResponseImpl.class);
        }
        return LcFindResponseImpl.create(httpResponse.code(), entity);
    }

    protected <E extends Entity> LcQueryResponse<E> readQueryResponse(Response httpResponse, Class<E> entityType) {
        if (isRequestFailed(httpResponse.code())) {
            return readResponse(httpResponse, LcQueryResponseImpl.class);
        }
        LcQueryResponseImpl response;
        try {
            response = JsonUtil.parseJson(httpResponse.body().string(), LcQueryResponseImpl.class, "results.values", entityType);
            response.setStatusCode(httpResponse.code());
        } catch (Exception e) {
            LOG.error("Failed to parse response", e);
            response = readResponse(503, null, LcQueryResponseImpl.class);
        }
        return response;
    }

    protected LcBasicResponse readBasicResponse(Response httpResponse) {
        return readResponse(httpResponse, LcBasicResponse.class);
    }

    protected <T extends LcBasicResponse> T readResponse(Response httpResponse, Class<T> responseType) {
        String body;
        try {
            body = httpResponse.body().string();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return readResponse(httpResponse.code(), body, responseType);
    }

    protected <T extends LcBasicResponse> T readResponse(int statusCode, String responseText, Class<T> responseType) {
        T response = null;
        if (responseText != null
                && !responseText.isEmpty()) {
            try {
                response = JsonUtil.parseJson(responseText, responseType);
            } catch (Exception ex) {
                response = null;
            }
        }
        if (response == null) {
            try {
                response = (T) responseType.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
        response.setStatusCode(statusCode);
        return response;
    }

    protected Response sendRequest(EasyRequest request) {
        request.scheme(scheme)
                .host(apiHost)
                .port(port);
        addDefaultHeaders(request);
        try {
            return request.send(okHttpClient);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Add default headers to the request. If request already has a header set, default header will be ignored.
     *
     * @param request
     */
    protected synchronized void addDefaultHeaders(EasyRequest request) {
        String sign = this._authSign;
        if (sign == null) {
            sign = resolveAuthSign();
            this._authSign = sign;
        }
        request.headers(defaultHeaders.build());
        request.header(HEAD_X_LC_SIGN, sign);
    }

    protected long getTimestamp() {
        return System.currentTimeMillis();
    }

    @Override
    public void close() {
        okHttpClient.connectionPool().evictAll();
    }

    public static abstract class Builder<B extends Builder, C extends LcApiClient> {

        protected final OkHttpClient.Builder httpClientBuilder;
        protected final Headers.Builder defaultHeaders;

        protected String appId;
        protected String appKey;
        protected String masterKey;

        protected String scheme = "https";
        protected int port = 443;
        protected String apiHost = "api.leancloud.cn";
        protected String apiVersion = "1.1";

        protected Builder() {
            this.defaultHeaders = new Headers.Builder();
            this.defaultHeaders.set(HEAD_ACCEPT, APPLICATION_JSON);
            this.httpClientBuilder = new OkHttpClient.Builder();
        }

        public abstract C build();

        public OkHttpClient.Builder getHttpClientBuilder() {
            return httpClientBuilder;
        }

        public B appId(String appId) {
            this.appId = appId;
            return (B) this;
        }

        public B appKey(String appKey) {
            this.appKey = appKey;
            return (B) this;
        }

        public B masterKey(String masterKey) {
            this.masterKey = masterKey;
            return (B) this;
        }

        public B scheme(String scheme) {
            this.scheme = scheme;
            return (B) this;
        }

        public B port(int port) {
            this.port = port;
            return (B) this;
        }

        public B apiHost(String apiHost) {
            this.apiHost = apiHost;
            return (B) this;
        }

        public B apiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return (B) this;
        }
    }
}
