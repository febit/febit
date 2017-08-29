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

import org.febit.convert.Convert;
import org.febit.util.StringUtil;
import org.febit.web.ActionRequest;

/**
 *
 * @author zqq90
 */
public class BaseArrayTypeArgument implements Argument {

    @Override
    public Object resolve(ActionRequest request, Class type, String name, int index) {
        String raw = request.request.getParameter(name);
        if (raw == null) {
            return null;
        }
        String[] array = StringUtil.toArrayOmitCommit(raw);
        final int len = array.length;
        if (type == int[].class) {
            int[] result = new int[len];
            for (int i = 0; i < len; i++) {
                result[i] = Convert.toInt(array[i]);
            }
            return result;
        }
        if (type == long[].class) {
            long[] result = new long[len];
            for (int i = 0; i < len; i++) {
                result[i] = Convert.toLong(array[i]);
            }
            return result;
        }
        if (type == Integer[].class) {
            Integer[] result = new Integer[len];
            for (int i = 0; i < len; i++) {
                result[i] = Convert.toInt(array[i]);
            }
            return result;
        }
        if (type == Long[].class) {
            Long[] result = new Long[len];
            for (int i = 0; i < len; i++) {
                result[i] = Convert.toLong(array[i]);
            }
            return result;
        }
        if (type == boolean[].class) {
            boolean[] result = new boolean[len];
            for (int i = 0; i < len; i++) {
                result[i] = Convert.toBool(array[i]);
            }
            return result;
        }
        if (type == Boolean[].class) {
            Boolean[] result = new Boolean[len];
            for (int i = 0; i < len; i++) {
                result[i] = Convert.toBool(array[i]);
            }
            return result;
        }
        return null;
    }

    @Override
    public Class[] matchTypes() {
        return new Class[]{
            int[].class,
            Integer[].class,
            long[].class,
            Long[].class,
            boolean[].class,
            Boolean[].class
        };
    }
}
