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
package org.febit.web.argument;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.febit.web.ActionRequest;

/**
 *
 * @author zqq90
 */
public class ServletArgument implements Argument {

    @Override
    public Object resolve(ActionRequest request, Class type, String name, int index) {
        if (type == HttpServletRequest.class) {
            return request.request;
        }
        if (type == HttpSession.class) {
            return request.request.getSession();
        }
        if (type == HttpServletResponse.class) {
            return request.response;
        }
        return null;
    }

    @Override
    public Class[] matchTypes() {
        return new Class[]{
            HttpServletRequest.class,
            HttpServletResponse.class,
            HttpSession.class
        };
    }
}
