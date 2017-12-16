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

import org.febit.util.ClassUtil;
import org.febit.util.Petite;
import org.febit.util.PetiteGlobalBeanProvider;

/**
 *
 * @author zqq90
 */
public class ComponentPetiteProvider implements PetiteGlobalBeanProvider {

    @Override
    public boolean isSupportType(Class<?> type) {
        if (Component.class.isAssignableFrom(type)) {
            return true;
        }
        return false;
    }

    @Override
    public Object newInstance(Class<?> type, Petite petite) {
        if (!isSupportType(type)) {
            return null;
        }
        if (type.isInterface()) {
            return null;
        }
        return ClassUtil.newInstance(type);
    }
}
