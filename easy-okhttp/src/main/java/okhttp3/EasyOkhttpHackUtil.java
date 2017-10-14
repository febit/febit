/*
 * Copyright 2017 febit.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okhttp3;

import static okhttp3.HttpUrl.defaultPort;

/**
 *
 * @author zqq90
 */
public class EasyOkhttpHackUtil {

  public static void setUrl(HttpUrl.Builder urlBuilder, HttpUrl url) {
    String scheme = url.scheme();
    int port = url.port();
    urlBuilder.scheme = scheme;
    urlBuilder.encodedUsername = url.encodedUsername();
    urlBuilder.encodedPassword = url.encodedPassword();
    urlBuilder.host = url.host;
    // If we're set to a default port, unset it in case of a scheme change.
    urlBuilder.port = port != defaultPort(scheme) ? port : -1;
    urlBuilder.encodedPathSegments.clear();
    urlBuilder.encodedPathSegments.addAll(url.encodedPathSegments());
    urlBuilder.encodedQuery(url.encodedQuery());
    urlBuilder.encodedFragment = url.encodedFragment();
  }

  public static Request.Builder createRequestBuilder(
    HttpUrl url,
    String method,
    Headers.Builder headers,
    RequestBody body,
    Object tag
  ) {
    Request.Builder builder = new Request.Builder();
    builder.url = url;
    builder.method = method;
    builder.body = body;
    builder.tag = tag;
    builder.headers = headers;
    return builder;
  }
}
