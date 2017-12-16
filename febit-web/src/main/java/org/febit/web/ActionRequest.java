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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.febit.lang.Iter;
import org.febit.util.ArraysUtil;
import org.febit.util.CollectionUtil;
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
    protected final Map<String, String> macroParams;

    public ActionRequest(ActionConfig actionConfig,
            HttpServletRequest request,
            HttpServletResponse response,
            Map<String, String> macroParams) {
        this.actionConfig = actionConfig;
        this.request = request;
        this.response = response;
        this.macroParams = macroParams != null ? macroParams : Collections.emptyMap();
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
        String value = macroParams.get(name);
        if (value != null) {
            return value;
        }
        return this.request.getParameter(name);
    }

    @SuppressWarnings("unchecked")
    public Iter<String> getParameterNames() {
        Enumeration<String> paramNames = this.request.getParameterNames();
        if (macroParams.isEmpty()) {
            return CollectionUtil.toIter(paramNames);
        }
        return CollectionUtil.concat(
                macroParams.keySet().iterator(),
                CollectionUtil.toIter(paramNames)
                        .filter(name -> !macroParams.containsKey(name))
        );
    }

    public String[] getParameterValues(String name) {
        String macroValue = macroParams.get(name);
        String[] params = this.request.getParameterValues(name);
        if (macroValue == null) {
            return params;
        } else if (params == null) {
            return new String[]{macroValue};
        } else {
            return ArraysUtil.append(params, macroValue);
        }
    }

    public Object attr(String key) {
        this.request.getAttribute(key);
        return this;
    }
}
