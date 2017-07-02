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

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import jodd.util.MimeTypes;
import org.febit.web.ActionRequest;
import org.febit.web.util.ServletUtil;

/**
 *
 * @author zqq90
 */
public class Text implements Renderable {

    protected final String mimetype;// = MimeTypes.MIME_TEXT_PLAIN;
    protected final String data;

    public Text(String data) {
        this(MimeTypes.MIME_TEXT_PLAIN, data);
    }

    public Text(String mimetype, String data) {
        this.mimetype = mimetype;
        this.data = data;
    }

    @Override
    public Object render(final ActionRequest actionRequest) throws IOException {
        final HttpServletResponse response = actionRequest.response;
        final String encoding = response.getCharacterEncoding();
        response.setContentType(this.mimetype);
        response.setCharacterEncoding(encoding);
        ServletUtil.setResponseContent(response, this.data.getBytes(encoding));
        return null;
    }
}
