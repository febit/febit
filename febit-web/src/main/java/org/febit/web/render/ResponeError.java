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
package org.febit.web.render;

import jodd.util.MimeTypes;
import org.febit.web.ActionRequest;
import org.febit.web.util.ServletUtil;

/**
 *
 * @author zqq90
 */
public final class ResponeError implements Renderable {

    public static final ResponeError ERROR_403 = new ResponeError(403);
    public static final ResponeError ERROR_404 = new ResponeError(404);
    public static final ResponeError ERROR_204 = new ResponeError(204);

    private final int code;
    private final String text;

    public ResponeError(int code) {
        this(code, null);
    }

    public ResponeError(int code, String text) {
        this.code = code;
        this.text = text;
    }

    @Override
    public Object render(ActionRequest actionRequest) throws Exception {
        actionRequest.response.sendError(this.code);
        if (text != null && !text.isEmpty()) {
            ServletUtil.setContentAndContentType(actionRequest.response, MimeTypes.MIME_TEXT_PLAIN, text);
        }
        return null;
    }
}
