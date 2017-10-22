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
package org.febit.web.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.febit.web.component.ArgumentManager;
import org.febit.web.component.ArgumentResolver;
import org.febit.web.meta.Filter;
import org.febit.web.meta.Action;
import org.febit.web.meta.ActionAnnotation;
import org.febit.web.meta.ArgumentAnnotation;
import org.febit.web.meta.HttpMethod;
import org.febit.web.meta.IgnoreXsrf;

/**
 *
 * @author zqq90
 */
public class AnnotationUtil {

    public static boolean isActionAnnotation(Class<? extends Annotation> annoType) {
        return annoType.getAnnotation(ActionAnnotation.class) != null;
    }

    public static boolean isActionAnnotation(Annotation anno) {
        return isActionAnnotation(anno.annotationType());
    }

    public static boolean hasIgnoreXsrfAnno(Method method) {
        return hasAnnotation(method, IgnoreXsrf.class);
    }

    public static boolean hasActionAnno(Method method) {
        return hasAnnotation(method, Action.class);
    }

    public static boolean isAction(AnnotatedElement element) {
        if (element.getAnnotation(Action.class) != null) {
            return true;
        }
        for (Annotation anno : element.getAnnotations()) {
            if (AnnotationUtil.isActionAnnotation(anno)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnnotation(Method method, Class<? extends Annotation> annoClass) {
        if (method.getAnnotation(annoClass) != null) {
            return true;
        }
        for (Annotation anno : method.getAnnotations()) {
            if (!isActionAnnotation(anno)) {
                continue;
            }
            if (anno.annotationType().getAnnotation(annoClass) != null) {
                return true;
            }
        }
        return false;
    }

    public static String getHttpMethod(Annotation anno) {
        if (anno == null) {
            return null;
        }
        if (anno instanceof HttpMethod) {
            return ((HttpMethod) anno).value();
        }
        return getHttpMethod(anno.annotationType().getAnnotation(HttpMethod.class));
    }

    public static List<String> getHttpMethods(Method method) {
        List<String> httpMethods = new ArrayList<>();
        String httpMethod = getHttpMethod(method.getAnnotation(HttpMethod.class));
        if (httpMethod != null) {
            httpMethods.add(httpMethod);
        }
        for (Annotation anno : method.getAnnotations()) {
            httpMethod = getHttpMethod(anno);
            if (httpMethod != null) {
                httpMethods.add(httpMethod);
            }
            if (AnnotationUtil.isActionAnnotation(anno)) {
                for (Annotation interAnnotation : anno.annotationType().getAnnotations()) {
                    httpMethod = getHttpMethod(interAnnotation);
                    if (httpMethod != null) {
                        httpMethods.add(httpMethod);
                    }
                }
            }
        }
        return httpMethods;
    }

    public static Class<? extends ArgumentResolver> getArgumentResolverClass(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            ArgumentAnnotation argAnno = annotation.annotationType().getAnnotation(ArgumentAnnotation.class);
            if (argAnno != null) {
                return argAnno.resolver();
            }
        }
        // Not found
        return null;
    }

    public static List<String> getFilters(Method method) {
        List<String> filters = new ArrayList<>();
        boolean hasFilter = false;
        for (Annotation anno : method.getAnnotations()) {
            if (anno instanceof Filter) {
                hasFilter = true;
                filters.addAll(Arrays.asList(((Filter) anno).value()));
                continue;
            }
            if (!AnnotationUtil.isActionAnnotation(anno)) {
                continue;
            }
            Filter filter = anno.annotationType().getAnnotation(Filter.class);
            if (filter != null) {
                hasFilter = true;
                filters.addAll(Arrays.asList(filter.value()));
            }
        }
        return hasFilter ? filters : null;
    }

    public static String getActionAnnoValue(Method method) {
        Action actionAnno;
        actionAnno = method.getAnnotation(Action.class);
        if (actionAnno != null) {
            return actionAnno.value();
        }
        Annotation[] annotations = method.getAnnotations();
        for (int i = annotations.length - 1; i >= 0; i--) {
            Annotation anno = annotations[i];
            if (!AnnotationUtil.isActionAnnotation(anno)) {
                continue;
            }
            actionAnno = anno.annotationType().getAnnotation(Action.class);
            if (actionAnno != null) {
                return actionAnno.value();
            }
        }
        return null;
    }

}
