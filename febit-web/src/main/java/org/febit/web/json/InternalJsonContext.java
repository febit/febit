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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jodd.introspector.ClassDescriptor;
import jodd.introspector.ClassIntrospector;
import jodd.introspector.FieldDescriptor;
import jodd.introspector.MethodDescriptor;
import jodd.introspector.PropertyDescriptor;
import jodd.json.JsonContext;
import jodd.json.JsonSerializer;
import org.febit.lang.IdentityMap;
import org.febit.web.json.meta.JsonExclude;

/**
 *
 * @author zqq90
 */
public final class InternalJsonContext extends JsonContext {

    private final Set<String> profiles;
    private final IdentityMap<Class, Set<String>> excludesPool = new IdentityMap<>(16);

    public InternalJsonContext(JsonSerializer jsonSerializer, Appendable appendable, String[] profiles) {
        super(jsonSerializer, appendable, true);
        if (profiles != null) {
            this.profiles = new HashSet<>(Arrays.asList(profiles));
        } else {
            this.profiles = Collections.emptySet();
        }
    }

    public boolean isExcluded(final Object source, final String name) {
        if (!(source instanceof Map)) {
            return getExcludeds(source.getClass()).contains(name);
        }
        return false;
    }

    private Set<String> getExcludeds(Class clz) {
        Set<String> excludes;
        if ((excludes = excludesPool.get(clz)) != null) {
            return excludes;
        }
        excludesPool.put(clz, excludes = resolveExcludeds(clz));
        return excludes;
    }

    private Set<String> resolveExcludeds(final Class clz) {
        final Set<String> excludes = new HashSet<>();
        ClassDescriptor classDescriptor = ClassIntrospector.lookup(clz);
        for (PropertyDescriptor propertyDescriptor : classDescriptor.getAllPropertyDescriptors()) {
            if (propertyDescriptor.getGetter(false) != null) {
                if (isExcluded(propertyDescriptor)) {
                    excludes.add(propertyDescriptor.getName());
                }
            }
        }
        if (excludes.isEmpty()) {
            return Collections.emptySet();
        }
        return excludes;
    }

    private boolean isExcluded(final PropertyDescriptor propertyDescriptor) {
        // check for transient flag
        final FieldDescriptor fieldDescriptor = propertyDescriptor.getFieldDescriptor();
        if (fieldDescriptor != null) {
            if (Modifier.isTransient(fieldDescriptor.getField().getModifiers())) {
                return true;
            }
            if (isExcluded(fieldDescriptor.getField().getAnnotation(JsonExclude.class))) {
                return true;
            }
        }

        final MethodDescriptor methodDescriptor = propertyDescriptor.getReadMethodDescriptor();
        if (methodDescriptor != null) {
            if (isExcluded(methodDescriptor.getMethod().getAnnotation(JsonExclude.class))) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcluded(final JsonExclude annotation) {
        if (annotation == null) {
            return false;
        }
        if (annotation.always()) {
            return true;
        }
        if (this.profiles.isEmpty()) {
            return false;
        }
        String[] annotationProfiles;
        int i = (annotationProfiles = annotation.value()).length;
        while (i != 0) {
            if (this.profiles.contains(annotationProfiles[--i])) {
                return true;
            }
        }
        return false;
    }
}
