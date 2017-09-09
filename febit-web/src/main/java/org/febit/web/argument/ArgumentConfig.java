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

import org.febit.web.ActionRequest;

/**
 *
 * @author zqq90
 */
public class ArgumentConfig {

    protected final int index;
    protected final String name;
    protected final Class<?> type;
    protected final Argument argument;

    public ArgumentConfig(int index, String name, Class<?> type, Argument argument) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.argument = argument;
    }

    public Object resolve(final ActionRequest request) {
        return argument.resolve(request, type, name, index);
    }
}
