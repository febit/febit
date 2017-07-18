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
package org.febit.web.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jodd.io.FileNameUtil;
import jodd.util.MimeTypes;
import jodd.util.StringPool;
import org.febit.util.CollectionUtil;
import org.febit.web.WebApp;

/**
 *
 * @author zqq90
 */
public class ServletUtil {

    public static final String INCLUDE_REQUEST_URI = "javax.servlet.include.request_uri";
    public static final String INCLUDE_CONTEXT_PATH = "javax.servlet.include.context_path";
    public static final String INCLUDE_SERVLET_PATH = "javax.servlet.include.servlet_path";
    public static final String INCLUDE_PATH_INFO = "javax.servlet.include.path_info";
    public static final String INCLUDE_QUERY_STRING = "javax.servlet.include.query_string";

    public static String getIncludeRequestUri(HttpServletRequest request) {
        return (String) request.getAttribute(INCLUDE_REQUEST_URI);
    }

    public static String getIncludeContextPath(HttpServletRequest request) {
        return (String) request.getAttribute(INCLUDE_CONTEXT_PATH);
    }

    public static String getIncludeServletPath(HttpServletRequest request) {
        return (String) request.getAttribute(INCLUDE_SERVLET_PATH);
    }

    public static String getIncludePathInfo(HttpServletRequest request) {
        return (String) request.getAttribute(INCLUDE_PATH_INFO);
    }

    public static String getIncludeQueryString(HttpServletRequest request) {
        return (String) request.getAttribute(INCLUDE_QUERY_STRING);
    }

    public static boolean isMultipartRequest(HttpServletRequest request) {
        String type = request.getHeader("Content-Type");
        return (type != null) && type.startsWith("multipart/form-data");
    }

    public static void redirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        if (url.startsWith(StringPool.SLASH)) {
            String contextPath = request.getContextPath();
            if (contextPath != null && contextPath.length() != 1) {
                //assert contextPath != "/"
                url = contextPath + url;
            }
        }
        response.sendRedirect(response.encodeRedirectURL(url));
    }

    public static void prepareResponse(HttpServletResponse response, String fileName, String mimeType, int fileSize) {
        if ((mimeType == null) && (fileName != null)) {
            String extension = FileNameUtil.getExtension(fileName);
            mimeType = MimeTypes.getMimeType(extension);
        }

        if (mimeType != null) {
            response.setContentType(mimeType);
        }

        if (fileSize >= 0) {
            response.setContentLength(fileSize);
        }

        if (fileName != null) {
            String name = FileNameUtil.getName(fileName);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + name + '\"');
        }
    }

    public static Map<String, String> getParamStringMap(HttpServletRequest request) {
        Map<String, Object> map = request.getParameterMap();

        boolean encode = "GET".equalsIgnoreCase(request.getMethod());
        Map<String, String> ret = CollectionUtil.createMap(map.size());
        for (Map.Entry<String, Object> entrySet : map.entrySet()) {
            Object value = entrySet.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof String[]) {
                String[] arr = (String[]) value;
                if (arr.length == 0) {
                    continue;
                }
                value = arr[0];
            }
            if (encode) {
                try {
                    ret.put(entrySet.getKey(), new String(value.toString().getBytes("ISO-8859-1"), "UTF-8"));
                } catch (UnsupportedEncodingException ignore) {
                    ret.put(entrySet.getKey(), value.toString());
                }
            } else {
                ret.put(entrySet.getKey(), value.toString());
            }
        }
        return ret;
    }

    public static ServletContext getServletContext() {
        return WebApp.instance().getServletContext();
    }

    public static void setContentAndContentType(final HttpServletResponse response, final String mimetype, final String context) throws IOException {
        final String encoding = response.getCharacterEncoding();
        response.setContentType(mimetype);
        response.setCharacterEncoding(encoding);
        ServletUtil.setResponseContent(response, context.getBytes(encoding));
    }

    public static void setResponseContent(final HttpServletResponse response, final byte[] data) throws IOException {
        final OutputStream out = response.getOutputStream();
        try {
            out.write(data);
        } finally {
            out.flush();
        }
    }

    private static final Pattern MOBILE_CLIENT = Pattern.compile("phone|android|mobile|wp7|wp8|ucweb", Pattern.CASE_INSENSITIVE);

    public static boolean isMobileClient() {
        return isMobileClient(WebApp.request().request);
    }

    public static boolean isMobileClient(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return false;
        }
        boolean ret = MOBILE_CLIENT.matcher(userAgent).find();
        return ret;
    }

    public static String getRequestPath(HttpServletRequest request) {
        String path = getServletPath(request);
        if (path == null || path.isEmpty()) {
            path = request.getPathInfo();
        }
        return path;
    }

    public static String getServletPath(HttpServletRequest request) {
        String result = getIncludeServletPath(request);
        if (result != null) {
            return result;
        }
        return request.getServletPath();
    }

    public static void preventCaching(HttpServletResponse response) {
        response.setHeader("Cache-Control", "max-age=0, must-revalidate, no-cache, no-store, private, post-check=0, pre-check=0");  // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0);
    }
}
