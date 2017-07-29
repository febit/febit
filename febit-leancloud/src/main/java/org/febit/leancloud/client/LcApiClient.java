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
import java.util.List;
import java.util.Map;
import jodd.http.Cookie;
import jodd.http.HttpConnectionProvider;
import jodd.http.HttpException;
import jodd.http.HttpMultiMap;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.JoddHttp;
import jodd.http.LcHackUtil;
import jodd.http.ProxyInfo;
import jodd.util.StringPool;
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

    // ---> http client settings
    protected boolean _keepAlive = false;
    protected HttpConnectionProvider httpConnectionProvider = JoddHttp.httpConnectionProvider;
    // ---->
    protected final HttpMultiMap<String> defaultHeaders;
    protected final HttpMultiMap<Cookie> cookies;
    protected HttpResponse lastHttpResponse;

    // -----> sign keys
    protected boolean _initedSign = false;
    protected String _appId;
    protected String _appKey;
    protected String _masterKey;

    // ----> remote settings
    protected String _protocol = "https";
    protected int _port = 443;
    protected String _apiHost = "api.leancloud.cn";
    protected String _apiVersion = "1.1";

    public LcApiClient() {
        this.cookies = HttpMultiMap.newCaseInsensitveMap();
        this.defaultHeaders = HttpMultiMap.newCaseInsensitveMap();
        this.defaultHeaders.set(HEAD_ACCEPT, APPLICATION_JSON);
    }

    public <T extends Entity> LcFindResponse<T> find(String id, Class<T> entityType) {
        return find(id, entityType, null);
    }

    public <T extends Entity> LcFindResponse<T> find(String id, Class<T> entityType, String[] keys) {
        if (StringUtil.isEmpty(id)) {
            throw new IllegalArgumentException("id is required");
        }
        String table = getEntityTableName(entityType);
        HttpRequest request = new HttpRequest()
                .method("GET")
                .path('/' + _apiVersion + "/classes/" + table + '/' + id);
        if (keys != null && keys.length != 0) {
            request.query("keys", StringUtil.join(keys, ','));
        }
        return readFindResponse(sendRequest(request), entityType);
    }

    public <T extends Entity> LcQueryResponse<T> query(LcQuery query, Class<T> entityType) {
        String table = getEntityTableName(entityType);
        HttpRequest request = new HttpRequest()
                .method("GET")
                .path('/' + _apiVersion + "/classes/" + table);

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
        HttpRequest request = new HttpRequest()
                .method("DELETE")
                .path('/' + _apiVersion + "/classes/" + table + '/' + id);
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcDeleteResponse delete(String id, Class<T> entityType, Condition where) {
        if (StringUtil.isEmpty(id)) {
            throw new IllegalArgumentException("id is required");
        }
        String table = getEntityTableName(entityType);
        HttpRequest request = new HttpRequest()
                .method("DELETE");
        if (id != null) {
            request.path('/' + _apiVersion + "/classes/" + table + '/' + id);
        } else {
            request.path('/' + _apiVersion + "/classes/" + table);
        }
        if (where != null) {
            request.query("where", JsonUtil.toJsonString(where));
        }
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcCreateResponse save(T entity) {
        String table = getEntityTableName(entity);
        HttpRequest request = new HttpRequest()
                .method("POST")
                .path('/' + _apiVersion + "/classes/" + table);
        setJsonBody(request, entity);
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcUpdateResponse update(T entity) {
        if (!entity._isPersistent()) {
            throw new IllegalArgumentException("entity.id is required");
        }
        String table = getEntityTableName(entity);
        HttpRequest request = new HttpRequest()
                .method("PUT")
                .path('/' + _apiVersion + "/classes/" + table + '/' + entity.id());
        setJsonBody(request, entity, "objectId");
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcUpdateResponse update(String id, Class<T> entityType, Map<String, ?> changes) {
        if (StringUtil.isEmpty(id)) {
            throw new IllegalArgumentException("id is required");
        }
        String table = getEntityTableName(entityType);
        HttpRequest request = new HttpRequest()
                .method("PUT")
                .path('/' + _apiVersion + "/classes/" + table + '/' + id);
        setJsonBody(request, changes);
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcUpdateResponse update(T entity, Condition where) {
        String table = getEntityTableName(entity);
        HttpRequest request = new HttpRequest()
                .method("PUT");
        if (entity._isPersistent()) {
            request.path('/' + _apiVersion + "/classes/" + table + '/' + entity.id());
        } else {
            request.path('/' + _apiVersion + "/classes/" + table);
        }
        if (where != null) {
            request.query("where", JsonUtil.toJsonString(where));
        }
        setJsonBody(request, entity, "objectId");
        return readBasicResponse(sendRequest(request));
    }

    public <T extends Entity> LcUpdateResponse update(String id, Class<T> entityType, Condition op, Condition where) {
        String table = getEntityTableName(entityType);
        HttpRequest request = new HttpRequest()
                .method("PUT");
        if (id != null) {
            request.path('/' + _apiVersion + "/classes/" + table + '/' + id);
        } else {
            request.path('/' + _apiVersion + "/classes/" + table);
        }
        if (where != null) {
            request.query("where", JsonUtil.toJsonString(where));
        }
        setJsonBody(request, op);
        return readBasicResponse(sendRequest(request));
    }

    protected HttpRequest setJsonBody(HttpRequest request, Object obj) {
        return request.bodyText(JsonUtil.toJsonString(obj), APPLICATION_JSON, "UTF-8");
    }

    protected HttpRequest setJsonBody(HttpRequest request, Object obj, String... excludes) {
        return request.bodyText(JsonUtil.toJsonString(obj, excludes), APPLICATION_JSON, "UTF-8");
    }

    protected void updateApiHeaders() {
        if (_appId == null) {
            throw new IllegalStateException("appId is required!");
        }
        this.defaultHeaders.set(HEAD_X_LC_ID, _appId);
        this.defaultHeaders.set(HEAD_X_LC_SIGN, resolveAppSign());
        _initedSign = true;
    }

    protected String resolveAppSign() {
        long now = getTimestamp();
        String masterKey = this._masterKey;
        String appKey = this._appKey;
        if (masterKey != null) {
            return EncryptUtil.md5(now + masterKey) + ',' + now + ",master";
        } else if (appKey != null) {
            return EncryptUtil.md5(now + appKey) + ',' + now;
        }
        throw new IllegalStateException("masterKey/appKey is required!");
    }

    protected <E extends Entity> LcFindResponse<E> readFindResponse(HttpResponse httpResponse, Class<E> entityType) {
        if (isRequestFailed(httpResponse.statusCode())) {
            return readResponse(httpResponse, LcFindResponseImpl.class);
        }
        E entity;
        try {
            entity = JsonUtil.parseJson(httpResponse.bodyText(), entityType);
        } catch (Exception e) {
            LOG.error("Failed to parse response", e);
            return readResponse(503, null, LcFindResponseImpl.class);
        }
        return LcFindResponseImpl.create(httpResponse.statusCode(), entity);
    }

    protected <E extends Entity> LcQueryResponse<E> readQueryResponse(HttpResponse httpResponse, Class<E> entityType) {
        if (isRequestFailed(httpResponse.statusCode())) {
            return readResponse(httpResponse, LcQueryResponseImpl.class);
        }
        LcQueryResponseImpl response;
        try {
            response = JsonUtil.parseJson(httpResponse.bodyText(), LcQueryResponseImpl.class, "results.values", entityType);
            response.setStatusCode(httpResponse.statusCode());
        } catch (Exception e) {
            LOG.error("Failed to parse response", e);
            response = readResponse(503, null, LcQueryResponseImpl.class);
        }
        return response;
    }

    protected LcBasicResponse readBasicResponse(HttpResponse httpResponse) {
        return readResponse(httpResponse, LcBasicResponse.class);
    }

    protected <T extends LcBasicResponse> T readResponse(HttpResponse httpResponse, Class<T> responseType) {
        return readResponse(httpResponse.statusCode(), httpResponse.bodyText(), responseType);
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

    protected synchronized HttpResponse sendRequest(HttpRequest httpRequest) {
        httpRequest.protocol(_protocol)
                .host(_apiHost)
                .port(_port);

        for (int i = 0; i < MAX_REDIRECT; i++) {
            HttpResponse previouseResponse = this.lastHttpResponse;
            this.lastHttpResponse = null;
            addDefaultHeaders(httpRequest);
            // send request
            try {
                if (!_keepAlive) {
                    httpRequest.open(httpConnectionProvider);
                } else {
                    // keeping alive
                    if (previouseResponse == null) {
                        httpRequest.open(httpConnectionProvider).connectionKeepAlive(true);
                    } else {
                        httpRequest.keepAlive(previouseResponse, true);
                    }
                }
                this.lastHttpResponse = httpRequest.send();
            } catch (HttpException e) {
                this.lastHttpResponse = new HttpResponse();
                this.lastHttpResponse.statusCode(503);
                LcHackUtil.assignHttpRequest(lastHttpResponse, httpRequest);
            }

            switch (lastHttpResponse.statusCode()) {
                // 301: moved permanently
                case 301:
                // 302: redirect, 303: see other
                case 302:
                case 303:
                    String newPath = resolveLocation(lastHttpResponse);
                    httpRequest = HttpRequest.get(newPath);
                    continue;
                // 307: temporary redirect
                case 307:
                    newPath = resolveLocation(lastHttpResponse);
                    String originalMethod = httpRequest.method();
                    httpRequest = new HttpRequest()
                            .method(originalMethod)
                            .set(newPath);
                    continue;
                default:
            }

            // break loops
            break;
        }
        return this.lastHttpResponse;
    }

    /**
     * Add default headers to the request. If request already has a header set, default header will be ignored.
     *
     * @param httpRequest
     */
    protected void addDefaultHeaders(HttpRequest httpRequest) {
        if (!_initedSign) {
            updateApiHeaders();
        }
        List<Map.Entry<String, String>> entries = defaultHeaders.entries();
        HttpMultiMap<String> headers = httpRequest.headers();
        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();
            if (!headers.contains(name)) {
                headers.add(name, entry.getValue());
            }
        }
    }

    protected String resolveLocation(HttpResponse httpResponse) {
        String location = httpResponse.header("location");
        if (location.startsWith(StringPool.SLASH)) {
            HttpRequest httpRequest = httpResponse.getHttpRequest();
            location = httpRequest.hostUrl() + location;
        }
        return location;
    }

    protected long getTimestamp() {
        return System.currentTimeMillis();
    }

    @Override
    public synchronized void close() {
        if (lastHttpResponse != null) {
            lastHttpResponse.close();
        }
    }

    // ----> 
    /**
     * Returns <code>true</code> if keep alive is used.
     *
     * @return if keep alive
     */
    public boolean isKeepAlive() {
        return _keepAlive;
    }

    public C keepAlive() {
        this._keepAlive = true;
        return (C) this;
    }

    /**
     * Defines that persistent HTTP connection should be used.
     *
     * @param keepAlive
     * @return this client
     */
    public C setKeepAlive(boolean keepAlive) {
        this._keepAlive = keepAlive;
        return (C) this;
    }

    /**
     * Defines proxy for a browser.
     *
     * @param proxyInfo
     * @return this client
     */
    public C setProxyInfo(ProxyInfo proxyInfo) {
        httpConnectionProvider.useProxy(proxyInfo);
        return (C) this;
    }

    /**
     * Defines {@link jodd.http.HttpConnectionProvider} for this browser session. Resets the previous proxy definition,
     * if set.
     *
     * @param httpConnectionProvider
     * @return this client
     */
    public C setHttpConnectionProvider(HttpConnectionProvider httpConnectionProvider) {
        this.httpConnectionProvider = httpConnectionProvider;
        return (C) this;
    }

    public C setDefaultHeader(String name, String value) {
        this.defaultHeaders.set(name, value);
        return (C) this;
    }

    public C setAppId(String appId) {
        this._appId = appId;
        this._initedSign = false;
        return (C) this;
    }

    public C setAppKey(String appKey) {
        this._appKey = appKey;
        this._initedSign = false;
        return (C) this;
    }

    public C setMasterKey(String masterKey) {
        this._masterKey = masterKey;
        this._initedSign = false;
        return (C) this;
    }
}
