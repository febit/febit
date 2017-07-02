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

import org.febit.convert.Convert;
import org.febit.web.ActionRequest;

/**
 *
 * @author zqq90
 */
public class BaseTypeArgument implements Argument {

    @Override
    public Object resolve(ActionRequest request, Class type, String name, int index) {
        final String raw = request.request.getParameter(name);
        if (type == String.class) {
            return raw;
        }
        if (type == int.class) {
            if (raw == null || raw.length() == 0) {
                return 0;
            }
            return Convert.toInt(raw);
        }
        if (type == long.class) {
            if (raw == null || raw.length() == 0) {
                return 0;
            }
            return Convert.toLong(raw);
        }
        if (type == boolean.class) {
            return Convert.toBool(raw);
        }
        if (raw == null) {
            return null;
        }
        if (type == Boolean.class) {
            return Convert.toBool(raw);
        }
        if (raw.length() == 0) {
            return null;
        }
        if (type == Integer.class) {
            return Convert.toInt(raw);
        }
        if (type == Long.class) {
            return Convert.toLong(raw);
        }
        return null;
    }

    @Override
    public Class[] matchTypes() {
        return new Class[]{
            String.class,
            int.class,
            Integer.class,
            long.class,
            Long.class,
            boolean.class,
            Boolean.class
        };
    }
}
