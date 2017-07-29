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
package org.febit.web.json;

import java.lang.reflect.InvocationTargetException;
import jodd.introspector.ClassIntrospector;
import jodd.introspector.Getter;
import jodd.introspector.PropertyDescriptor;
import jodd.json.JsonContext;
import jodd.json.JsonException;
import jodd.json.impl.ValueJsonSerializer;

/**
 *
 * @author zqq90
 */
public final class ObjectJsonSerializer extends ValueJsonSerializer<Object> {

    @Override
    public void serializeValue(final JsonContext jsonContext, final Object source) {
        InternalJsonContext internalJsonContext = (InternalJsonContext) jsonContext;
        internalJsonContext.writeOpenObject();
        final PropertyDescriptor[] propertyDescriptors = ClassIntrospector.lookup(source.getClass()).getAllPropertyDescriptors();
        boolean notfirst = false;
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final String propertyName = propertyDescriptor.getName();
            if (internalJsonContext.isExcluded(source, propertyName)) {
                continue;
            }
            final Getter getter = propertyDescriptor.getGetter(false);
            if (getter == null) {
                continue;
            }
            final Object value;
            try {
                value = getter.invokeGetter(source);
            } catch (InvocationTargetException | IllegalAccessException ex) {
                throw new JsonException(ex);
            }
            if (value == null) {
                continue;
            }
            internalJsonContext.pushName(propertyName, notfirst);
            internalJsonContext.serialize(value);
            if (!notfirst && internalJsonContext.isNamePopped()) {
                notfirst = true;
            }
        }
        internalJsonContext.writeCloseObject();
    }
}
