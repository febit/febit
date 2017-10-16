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
package org.febit.easyokhttp;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.EasyOkhttpHackUtil;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.http.HttpMethod;
import okio.ByteString;

/**
 *
 * @author zqq90
 */
public class EasyRequest {

  public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
  public static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain; charset=utf-8");

  protected final HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
  protected final Headers.Builder headers = new Headers.Builder();
  protected HttpUrl url;
  protected String method;
  protected RequestBody body;
  protected FormBody.Builder form;
  protected Object tag;

  public static EasyRequest create() {
    return new EasyRequest();
  }

  public Response send(OkHttpClient client) throws IOException {
    return newCall(client).execute();
  }

  public Response sendSilent(OkHttpClient client) {
    try {
      return send(client);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public Call newCall(OkHttpClient client) {
    return client.newCall(build());
  }

  protected void checkMethodAndBody() {
    if (method == null) {
      throw new NullPointerException("method == null");
    }
    if (method.length() == 0) {
      throw new IllegalArgumentException("method.length() == 0");
    }
    if (body != null && !HttpMethod.permitsRequestBody(method)) {
      throw new IllegalArgumentException("method " + method + " must not have a request body.");
    }
    if (body == null && HttpMethod.requiresRequestBody(method)) {
      this.body = Util.EMPTY_REQUEST;
    }
  }

  public Request build() {
    // resolve url
    HttpUrl url = urlBuilder.build();

    // resolve body
    if (form != null) {
      if (body != null) {
        throw new IllegalArgumentException("either form(..) or body(..)");
      }
      body = form.build();
    }

    checkMethodAndBody();
    return EasyOkhttpHackUtil.createRequestBuilder(url, method, headers, body, tag).build();
  }

  // ==> body
  public EasyRequest body(RequestBody body) {
    this.body = body;
    return this;
  }

  public EasyRequest body(String body) {
    return body(RequestBody.create(null, body));
  }

  public EasyRequest textBody(String json) {
    return body(RequestBody.create(MEDIA_TYPE_TEXT, json));
  }

  public EasyRequest jsonBody(String json) {
    return body(RequestBody.create(MEDIA_TYPE_JSON, json));
  }

  public EasyRequest jsonBody(Object json) {
    throw new UnsupportedOperationException();
  }

  public EasyRequest form(String name, Number value) {
    return form(name, String.valueOf(value));
  }

  public EasyRequest form(String name, String value) {
    if (form == null) {
      form = new FormBody.Builder();
    }
    form.add(name, value);
    return this;
  }

  // ==> url
  public EasyRequest scheme(String scheme) {
    urlBuilder.scheme(scheme);
    return this;
  }

  public EasyRequest query(String name, Number value) {
    return query(name, String.valueOf(value));
  }

  public EasyRequest query(String name, String value) {
    urlBuilder.addQueryParameter(name, value);
    return this;
  }

  public EasyRequest fragment(String fragment) {
    urlBuilder.fragment(fragment);
    return this;
  }

  public EasyRequest addPathSegment(String path) {
    urlBuilder.addPathSegment(path);
    return this;
  }

  public EasyRequest path(String path) {
    urlBuilder.encodedPath(path);
    return this;
  }

  public EasyRequest host(String host) {
    urlBuilder.host(host);
    return this;
  }

  public EasyRequest port(int port) {
    urlBuilder.port(port);
    return this;
  }

  public EasyRequest username(String username) {
    urlBuilder.username(username);
    return this;
  }

  public EasyRequest password(String password) {
    urlBuilder.password(password);
    return this;
  }

  public EasyRequest url(URL url) {
    HttpUrl parsed = HttpUrl.get(url);
    return url(parsed);
  }

  public EasyRequest url(String url) {
    HttpUrl parsed = HttpUrl.parse(url);
    return url(parsed);
  }

  public EasyRequest url(HttpUrl url) {
    EasyOkhttpHackUtil.setUrl(urlBuilder, url);
    return this;
  }

  // ==> method
  public EasyRequest method(String method) {
    this.method = method;
    return this;
  }

  public EasyRequest get() {
    this.method = "GET";
    return this;
  }

  public EasyRequest post() {
    this.method = "POST";
    return this;
  }

  public EasyRequest options() {
    this.method = "OPTIONS";
    return this;
  }

  public EasyRequest head() {
    this.method = "HEAD";
    return this;
  }

  public EasyRequest put() {
    this.method = "PUT";
    return this;
  }

  public EasyRequest patch() {
    this.method = "PATCH";
    return this;
  }

  public EasyRequest delete() {
    this.method = "DELETE";
    return this;
  }

  // ==> headers
  public EasyRequest header(String name, String value) {
    headers.set(name, value);
    return this;
  }

  public EasyRequest addHeader(String name, String value) {
    headers.add(name, value);
    return this;
  }

  public EasyRequest removeHeader(String name) {
    headers.removeAll(name);
    return this;
  }

  public EasyRequest headers(Headers headers) {
    EasyOkhttpHackUtil.addAllToBuilder(headers, this.headers);
    return this;
  }

  public EasyRequest basicAuthentication(String username, String password) {
    String encoded = ByteString.encodeUtf8(username + ':' + password).base64();
    header("Authorization", "Basic " + encoded);
    return this;
  }

  public EasyRequest tag(Object tag) {
    this.tag = tag;
    return this;
  }

  public EasyRequest cacheControl(CacheControl cacheControl) {
    String value = cacheControl.toString();
    if (value.isEmpty()) {
      return removeHeader("Cache-Control");
    }
    return header("Cache-Control", value);
  }

}
