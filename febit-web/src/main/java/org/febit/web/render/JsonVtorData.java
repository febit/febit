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
package org.febit.web.render;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import jodd.bean.BeanTemplateParser;
import jodd.util.StringPool;
import jodd.util.buffer.FastCharBuffer;
import org.febit.util.StringUtil;
import org.febit.vtor.Vtor;
import org.febit.web.ActionRequest;
import org.febit.web.HttpStatus;
import org.febit.web.util.I18nUtil;
import org.febit.web.util.RenderUtil;
import org.febit.web.util.ServletUtil;

/**
 *
 * @author zqq90
 */
public final class JsonVtorData implements Renderable {

    protected static final BeanTemplateParser BEAN_TEMPLATE_PARSER = new BeanTemplateParser();

    private final List<Vtor> _vtors;
    private final String _targetName;

    public JsonVtorData(List<Vtor> vtors, String targetName) {
        this._vtors = vtors != null ? vtors : Collections.emptyList();
        this._targetName = targetName;
    }

    @Override
    public Object render(ActionRequest actionRequest) throws Exception {

        final List<Vtor> vtors = this._vtors;
        final String targetName = this._targetName;

        final FastCharBuffer buffer = new FastCharBuffer(200).append("{\"vtor\":[");
        boolean appendTargetName = false;
        char[] targetNameChars = null;
        if (targetName != null) {
            targetNameChars = (targetName + ".").toCharArray();
            appendTargetName = true;
        }

        final Locale locale = I18nUtil.getLocale(actionRequest);
        final String bundleName = I18nUtil.getBundleName(actionRequest);

        for (int i = 0, vtorsSize = vtors.size(); i < vtorsSize; i++) {
            final Vtor vtor = vtors.get(i);
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append("{\"name\":\"");
            if (appendTargetName) {
                buffer.append(targetNameChars);
            }
            buffer.append(vtor.name).append('"').append(',');
            buffer.append("\"error\":\"")
                    .append(resolveValidationMessage(vtor, bundleName, locale))
                    .append('"')
                    .append('}');
        }

        actionRequest.response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        ServletUtil.setContentAndContentType(
                actionRequest.response,
                RenderUtil.MIME_TEXT_JSON,
                StringUtil.toString(buffer.append(']').append('}')));
        return null;
    }

    protected static String resolveValidationMessage(Vtor vtor, String bundleName, Locale locale) {
        final String msg = I18nUtil.findMessage(bundleName, locale, vtor.message);
        if (msg != null) {
            return StringUtil.escapeUTF8(BEAN_TEMPLATE_PARSER.parse(msg, vtor));
        }
        return StringPool.EMPTY;
    }
}
