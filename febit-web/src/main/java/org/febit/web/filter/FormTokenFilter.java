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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.febit.web.ActionRequest;
import org.febit.web.Filter;
import org.febit.web.render.ResponeError;

/**
 *
 * @author zqq90
 */
public class FormTokenFilter implements Filter {

    protected String tokenKey = "__t";

    @Override
    public Object invoke(ActionRequest actionRequest) throws Exception {

        HttpServletRequest request = actionRequest.request;
        String token = request.getParameter(tokenKey);
        if (token != null) {
            HttpSession session = request.getSession();
            if (token.equals(session.getAttribute(tokenKey))) {
                // if duplicate
                return ResponeError.ERROR_204;
            }
            session.setAttribute(tokenKey, token);
        }
        return actionRequest.invoke();
    }
}
