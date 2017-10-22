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
package org.febit.web;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.febit.web.component.Wrapper;

/**
 *
 * @author zqq90
 */
public class ActionRequest {

    public final ActionConfig actionConfig;
    public final HttpServletRequest request;
    public final HttpServletResponse response;

    protected final Wrapper[] wrappers;
    protected int currentIndex = 0;

    public ActionRequest(ActionConfig actionConfig, HttpServletRequest request, HttpServletResponse response) {
        this.actionConfig = actionConfig;
        this.request = request;
        this.response = response;
        this.wrappers = actionConfig.wrappers;
    }

    public final Object invoke() throws Exception {
        return wrappers[currentIndex++].invoke(this);
    }

    public ActionRequest attr(String key, Object value) {
        this.request.setAttribute(key, value);
        return this;
    }

    public String getParameter(String name) {
        return this.request.getParameter(name);
    }

    public Enumeration<String> getParameterNames() {
        return this.request.getParameterNames();
    }

    public String[] getParameterValues(String name) {
        return this.request.getParameterValues(name);
    }

    public Object attr(String key) {
        this.request.getAttribute(key);
        return this;
    }
}
