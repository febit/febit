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
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import jodd.bean.BeanUtil;
import org.febit.util.ClassUtil;
import org.febit.web.ActionRequest;

/**
 *
 * @author zqq90
 */
public class BeanArrayArgument implements Argument {

    @Override
    public Object resolve(ActionRequest request, Class type, String name, int index) {
        if (name == null) {
            return new RuntimeException("Can't resolve array type arg whitout name: " + request.actionConfig.handler);
        }
        final Class componentType = type.getComponentType();

        int dotIndex = name.length();
        int prefixLen = dotIndex + 1;
        final HttpServletRequest req = request.request;
        final ArrayList beans = new ArrayList();
        int len = 0;
        final Enumeration enumeration = req.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String param = (String) enumeration.nextElement();
            if (param.length() > dotIndex
                    && param.charAt(dotIndex) == '.'
                    && param.startsWith(name)) {
                String[] raw = req.getParameterValues(param);
                while (len < raw.length) {
                    beans.add(ClassUtil.newInstance(type));
                    len++;
                }
                String key = param.substring(prefixLen);
                for (int i = 0; i < len; i++) {
                    BeanUtil.declaredForcedSilent.setProperty(beans.get(i), key, raw[i]);
                }
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
