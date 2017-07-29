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

import org.febit.lang.ConcurrentIdentityMap;
import org.febit.util.ClassUtil;
import org.febit.util.Petite;
import org.febit.web.ActionRequest;
import org.febit.web.argument.Argument;

/**
 *
 * @author zqq90
 */
public class ArgumentManager implements Component {

    protected final ConcurrentIdentityMap<Argument> argumentCache;

    protected Argument[] arguments;
    protected Argument defaultArgument;

    public ArgumentManager() {
        this.argumentCache = new ConcurrentIdentityMap<>();
    }

    @Petite.Init
    public void init() {
        for (Argument argument : arguments) {
            for (Class type : argument.matchTypes()) {
                this.argumentCache.put(type, argument);
            }
        }
    }

    public Argument resolveArgument(final Class type, final String name, final int index) {
        Argument argument = argumentCache.get(type);
        if (argument != null) {
            return argument;
        }
        for (Class cls : ClassUtil.impls(type)) {
            argument = argumentCache.get(cls);
            if (argument != null) {
                break;
            }
        }
        if (argument == null) {
            argument = defaultArgument;
        }
        return argumentCache.putIfAbsent(type, argument);
    }

    public Object resolve(final ActionRequest request, final Class type, final String name, final int index) {
        Argument argument = argumentCache.get(type);
        if (argument == null) {
            argument = resolveArgument(type, name, index);
        }
        return argument.resolve(request, type, name, index);
    }
}
