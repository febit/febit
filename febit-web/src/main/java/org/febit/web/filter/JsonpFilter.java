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
package org.febit.web.filter;

import java.io.OutputStream;
import jodd.util.MimeTypes;
import org.febit.web.ActionRequest;
import org.febit.web.OuterFilter;

/**
 *
 * @author zqq90
 */
public class JsonpFilter implements OuterFilter {

    protected String callbackKey = "_cb";
    protected String callbackDefault = "callback";

    @Override
    public Object invoke(ActionRequest actionRequest) throws Exception {
        String callback = actionRequest.request.getParameter(callbackKey);
        if (callback == null) {
            callback = callbackDefault;
        }
        OutputStream out = actionRequest.response.getOutputStream();
        out.write(callback.getBytes("UTF-8"));
        out.write('(');
        Object ret = actionRequest.invoke();
        out.write(')');
        actionRequest.response.setContentType(MimeTypes.MIME_APPLICATION_JAVASCRIPT);
        return ret;
    }
}
