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
package org.febit.web.util;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import jodd.net.MimeTypes;
import org.febit.service.ServiceResult;
import org.febit.util.StringUtil;
import org.febit.web.ActionRequest;
import org.febit.web.json.Json;
import org.febit.web.render.JsonData;

/**
 *
 * @author zqq90
 */
public class RenderUtil {

    public static final String MIME_TEXT_JSON = "application/json;charset=utf-8";

    public static Object toJsonResult(final ServiceResult result, final String... profiles) {
        if (profiles != null && result.value != null) {
            return new JsonData(result.value, profiles);
        }
        return result;
    }

    public static Object renderAsJson(final ActionRequest actionRequest, final ServiceResult result) throws IOException {
        if (result.value != null) {
            return RenderUtil.renderJson(actionRequest, result.value, null, null);
        }
        if (result.success()) {
            return RenderUtil.renderSuccessJson(actionRequest);
        }
        return RenderUtil.renderErrorJson(actionRequest, result.getCode(), result.getMessage(), result.getArgs());
    }

    public static Object renderSuccessJson(ActionRequest actionRequest) throws IOException {
        ServletUtil.setContentAndContentType(actionRequest.response, RenderUtil.MIME_TEXT_JSON, "{\"code\":0}");
        return null;
    }

    protected static String resolveMessage(final ActionRequest actionRequest, final String origin) {
        if (origin == null) {
            return null;
        }
        String translated = I18nUtil.findMessage(actionRequest, origin);
        if (translated != null) {
            return translated;
        }
        return origin;
    }

    public static Object renderErrorJson(final ActionRequest actionRequest, int code, String msg, Object[] args) throws IOException {
        msg = resolveMessage(actionRequest, msg);
        final StringBuilder buffer = new StringBuilder((msg != null ? (msg.length() << 1) : 0) + 20);
        buffer.append("{\"code\":").append(code);
        if (msg != null) {
            buffer.append(",\"msg\":");
            StringUtil.escapeUTF8(args == null || args.length == 0 ? msg : StringUtil.format(msg, args), buffer, true);
        }
        ServletUtil.setContentAndContentType(
                actionRequest.response,
                RenderUtil.MIME_TEXT_JSON,
                buffer.append('}').toString());
        return null;
    }

    public static Object renderJson(ActionRequest actionRequest, Object value, String boxName, String[] profiles) throws IOException {
        final HttpServletResponse response = actionRequest.response;
        final String encoding = response.getCharacterEncoding();
        response.setContentType(MimeTypes.MIME_APPLICATION_JSON);
        response.setCharacterEncoding(encoding);
        final OutputStream out = response.getOutputStream();
        try {
            if (value == null) {
                out.write("{}".getBytes(encoding));
                return null;
            }
            final boolean hasBox = (boxName != null && !boxName.isEmpty());

            final StringBuilder buffer = new StringBuilder(255);
            if (hasBox) {
                buffer.append("{\"").append(boxName).append("\":");
            }
            if (value instanceof Integer) {
                buffer.append((Integer) value);
            } else if (value instanceof String) {
                StringUtil.escapeUTF8((String) value, buffer, true);
            } else {
                Json.writeTo(buffer, value, profiles);
            }
            if (hasBox) {
                buffer.append('}');
            }
            out.write(buffer.toString().getBytes(encoding));
        } finally {
            //Notice: no close
            out.flush();
        }
        return null;
    }

}
