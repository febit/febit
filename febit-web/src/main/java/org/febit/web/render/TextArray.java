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
import java.io.Writer;
import javax.servlet.http.HttpServletResponse;
import org.febit.web.ActionRequest;

/**
 *
 * @author zqq90
 */
public class TextArray implements Renderable {

    protected final String mimetype;
    protected final String[] array;

    public TextArray(String mimetype, String... array) {
        this.mimetype = mimetype;
        this.array = array;
    }

    @Override
    public Object render(final ActionRequest actionRequest) throws IOException {
        final HttpServletResponse response = actionRequest.response;
        final String encoding = response.getCharacterEncoding();
        response.setContentType(this.mimetype);
        response.setCharacterEncoding(encoding);

        final Writer writer = response.getWriter();
        final int size;
        final String[] buffer;
        size = (buffer = this.array).length;
        try {
            for (int i = 0; i < size; i++) {
                writer.write(buffer[i]);
            }
        } finally {
            writer.close();
        }
        return null;
    }
}
