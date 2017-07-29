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

import javax.servlet.http.HttpServletResponse;
import org.febit.web.ActionRequest;

/**
 *
 * @author zqq90
 */
public class EtagResult implements Renderable {

    protected String etag;

    public EtagResult(String etag) {
        this.etag = etag;
    }

    @Override
    public Object render(ActionRequest actionRequest) throws Exception {
        HttpServletResponse response = actionRequest.response;
        response.setHeader("Etag", etag);
        response.setStatus(304);
        return null;
    }

    public static EtagResult result(String etag) {
        return new EtagResult(etag);
    }
}
