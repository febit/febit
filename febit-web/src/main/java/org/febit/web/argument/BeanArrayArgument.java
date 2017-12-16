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

import java.lang.reflect.Array;
import java.util.ArrayList;
import jodd.bean.BeanUtil;
import org.febit.lang.Iter;
import org.febit.util.ClassUtil;
import org.febit.web.ActionRequest;

/**
 *
 * @author zqq90
 */
public class BeanArrayArgument implements Argument {

    @Override
    public Object resolve(ActionRequest actionRequest, Class<?> type, String name, int index) {
        if (name == null) {
            return new RuntimeException("Can't resolve array type arg whitout name: " + actionRequest.actionConfig.handler);
        }
        final Class componentType = type.getComponentType();

        int dotIndex = name.length();
        int prefixLen = dotIndex + 1;
        final ArrayList<Object> beans = new ArrayList<>();
        int len = 0;

        Iter<String> iter = actionRequest.getParameterNames()
                .filter((param) -> param.length() > dotIndex
                && param.charAt(dotIndex) == '.'
                && param.startsWith(name));

        while (iter.hasNext()) {
            String param = iter.next();
            String[] raw = actionRequest.getParameterValues(param);
            while (len < raw.length) {
                beans.add(ClassUtil.newInstance(type));
                len++;
            }
            String key = param.substring(prefixLen);
            for (int i = 0; i < len; i++) {
                BeanUtil.declaredForcedSilent.setProperty(beans.get(i), key, raw[i]);
            }
        }

        return beans.toArray((Object[]) Array.newInstance(componentType, len));
    }

    @Override
    public Class[] matchTypes() {
        return new Class[]{
            Object[].class
        };
    }
}
