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
package org.febit.web.argument;

import org.febit.util.StringUtil;
import org.febit.web.ActionRequest;

/**
 *
 * @author zqq90
 */
public class StringArrayTypeArgument implements Argument {

    @Override
    public Object resolve(ActionRequest request, Class type, String name, int index) {
        String raw = request.request.getParameter(name);
        if (raw == null) {
            return null;
        }
        return StringUtil.toArrayExcludeCommit(raw);
//        String[] array = request.request.getParameterValues(name);
//        if (array == null) {
//            return null;
//        }
//        return array;
    }

    @Override
    public Class[] matchTypes() {
        return new Class[]{
            String[].class
        };
    }
}
