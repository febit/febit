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
package org.febit.web.filter;

import org.febit.web.ActionRequest;
import org.febit.web.RenderedFilter;

/**
 *
 * @author zqq90
 */
public class UTF8EncodingFilter implements RenderedFilter {

    @Override
    public Object invoke(final ActionRequest actionRequest) throws Exception {
        actionRequest.request.setCharacterEncoding("UTF-8");
        actionRequest.response.setCharacterEncoding("UTF-8");
        return actionRequest.invoke();
    }
}
