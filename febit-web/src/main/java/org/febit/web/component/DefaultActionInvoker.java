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
package org.febit.web.component;

import org.febit.lang.Defaults;
import org.febit.web.ActionConfig;
import org.febit.web.ActionRequest;
import org.febit.web.argument.Argument;

/**
 *
 * @author zqq90
 */
public class DefaultActionInvoker implements Wrapper {

    @Override
    public Object invoke(final ActionRequest request) throws Exception {
        final ActionConfig actionConfig = request.actionConfig;
        final Class[] argTypes = actionConfig.argTypes;
        final int argsLen = argTypes.length;
        final Object[] args;
        if (argsLen == 0) {
            args = Defaults.EMPTY_OBJECTS;
        } else {
            args = new Object[argsLen];
            final String[] argNames = actionConfig.argNames;
            final Argument[] arguments = actionConfig.arguments;
            for (int i = 0; i < argsLen; i++) {
                args[i] = arguments[i].resolve(request, argTypes[i], argNames[i], i);
            }
        }
        return actionConfig.method.invoke(actionConfig.action, args);
    }
}
