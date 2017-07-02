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
package org.febit.web;

import java.lang.reflect.Method;
import org.febit.web.argument.Argument;
import org.febit.web.component.Wrapper;
import org.febit.web.meta.IgnoreXsrf;

/**
 *
 * @author zqq90
 */
public class ActionConfig {

    public final int id;
    public final Object action;
    public final Method method;
    public final String path;
    public final boolean isHtml;
    public final boolean isIgnoreXsrf;
    public final Class[] argTypes;
    public final String[] argNames;
    public final Argument[] arguments;
    public final Wrapper[] wrappers;

    public ActionConfig(int id, Object action, Method method, String path, Class[] argTypes, String[] argNames, Argument[] arguments, Wrapper[] wrappers) {
        this.id = id;
        this.action = action;
        this.method = method;
        this.path = path;
        this.argTypes = argTypes;
        this.argNames = argNames;
        this.arguments = arguments;
        this.wrappers = wrappers;
        this.isHtml = path.endsWith(".html") || path.endsWith("htm");
        this.isIgnoreXsrf = this.isHtml || method.getAnnotation(IgnoreXsrf.class) != null;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ActionConfig other = (ActionConfig) obj;
        return this.id == other.id
                && this.action == other.action
                && this.method == other.method;
    }
}
