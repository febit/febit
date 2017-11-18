/*
 * Copyright 2017 febit.org.
 *
 * Licensed under the Apache License, Version 2.0;
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
package org.febit.web;

/**
 * HTTP status codes.
 *
 * @author zqq90
 */
public interface HttpStatus {

    // --- 1xx Informational
    /**
     * {@code 100 Continue}
     */
    public static final int CONTINUE = 100;
    /**
     * {@code 101 Switching Protocols}
     */
    public static final int SWITCHING_PROTOCOLS = 101;
    /**
     * {@code 102 Processing}
     */
    public static final int PROCESSING = 102;

    // --- 2xx Success
    /**
     * {@code 200 OK}
     */
    public static final int OK = 200;
    /**
     * {@code 201 Created}
     */
    public static final int CREATED = 201;
    /**
     * {@code 202 Accepted}
     */
    public static final int ACCEPTED = 202;
    /**
     * {@code 203 Non Authoritative Information}
     */
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;
    /**
     * {@code 204 No Content}
     */
    public static final int NO_CONTENT = 204;
    /**
     * {@code 205 Reset Content}
     */
    public static final int RESET_CONTENT = 205;
    /**
     * {@code 206 Partial Content}
     */
    public static final int PARTIAL_CONTENT = 206;
    /**
     * {@code 207 Multi-Status}
     */
    public static final int MULTI_STATUS = 207;

    // --- 3xx Redirection
    /**
     * {@code 300 Mutliple Choices}
     */
    public static final int MULTIPLE_CHOICES = 300;
    /**
     * {@code 301 Moved Permanently}
     */
    public static final int MOVED_PERMANENTLY = 301;
    /**
     * {@code 302 Moved Temporarily}
     */
    public static final int MOVED_TEMPORARILY = 302;
    /**
     * {@code 303 See Other}
     */
    public static final int SEE_OTHER = 303;
    /**
     * {@code 304 Not Modified}
     */
    public static final int NOT_MODIFIED = 304;
    /**
     * {@code 305 Use Proxy}
     */
    public static final int USE_PROXY = 305;
    /**
     * {@code 307 Temporary Redirect}
     */
    public static final int TEMPORARY_REDIRECT = 307;

    // --- 4xx Client Error
    /**
     * {@code 400 Bad Request}
     */
    public static final int BAD_REQUEST = 400;
    /**
     * {@code 401 Unauthorized}
     */
    public static final int UNAUTHORIZED = 401;
    /**
     * {@code 402 Payment Required}
     */
    public static final int PAYMENT_REQUIRED = 402;
    /**
     * {@code 403 Forbidden}
     */
    public static final int FORBIDDEN = 403;
    /**
     * {@code 404 Not Found}
     */
    public static final int NOT_FOUND = 404;
    /**
     * {@code 405 Method Not Allowed}
     */
    public static final int METHOD_NOT_ALLOWED = 405;
    /**
     * {@code 406 Not Acceptable}
     */
    public static final int NOT_ACCEPTABLE = 406;
    /**
     * {@code 407 Proxy Authentication Required}
     */
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    /**
     * {@code 408 Request Timeout}
     */
    public static final int REQUEST_TIMEOUT = 408;
    /**
     * {@code 409 Conflict}
     */
    public static final int CONFLICT = 409;
    /**
     * {@code 410 Gone}
     */
    public static final int GONE = 410;
    /**
     * {@code 411 Length Required}
     */
    public static final int LENGTH_REQUIRED = 411;
    /**
     * {@code 412 Precondition Failed}
     */
    public static final int PRECONDITION_FAILED = 412;
    /**
     * {@code 413 Request Entity Too Large}
     */
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;
    /**
     * {@code 414 Request-URI Too Long}
     */
    public static final int REQUEST_URI_TOO_LONG = 414;
    /**
     * {@code 415 Unsupported Media Type}
     */
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    /**
     * {@code 416 Requested Range Not Satisfiable}
     */
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    /**
     * {@code 417 Expectation Failed}
     */
    public static final int EXPECTATION_FAILED = 417;
    /**
     * {@code 422 Unprocessable Entity}
     */
    public static final int UNPROCESSABLE_ENTITY = 422;
    /**
     * {@code 423 Locked}
     */
    public static final int LOCKED = 423;
    /**
     * {@code 424 Failed Dependency}
     */
    public static final int FAILED_DEPENDENCY = 424;

    // --- 5xx Server Error
    /**
     * {@code 500 Server Error}
     */
    public static final int INTERNAL_SERVER_ERROR = 500;
    /**
     * {@code 501 Not Implemented}
     */
    public static final int NOT_IMPLEMENTED = 501;
    /**
     * {@code 502 Bad Gateway}
     */
    public static final int BAD_GATEWAY = 502;
    /**
     * {@code 503 Service Unavailable}
     */
    public static final int SERVICE_UNAVAILABLE = 503;
    /**
     * {@code 504 Gateway Timeout}
     */
    public static final int GATEWAY_TIMEOUT = 504;
    /**
     * {@code 505 HTTP Version Not Supported}
     */
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
    /**
     * {@code 507 Insufficient Storage}
     */
    public static final int INSUFFICIENT_STORAGE = 507;
}
