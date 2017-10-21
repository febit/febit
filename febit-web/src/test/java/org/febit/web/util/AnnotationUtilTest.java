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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.febit.web.meta.Action;
import org.febit.web.meta.ActionAnnotation;
import org.febit.web.meta.DELETE;
import org.febit.web.meta.Filter;
import org.febit.web.meta.GET;
import org.febit.web.meta.HEAD;
import org.febit.web.meta.PATCH;
import org.febit.web.meta.PUT;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author zqq90
 */
@Test
public class AnnotationUtilTest {

    @ActionAnnotation
    @GET
    @PATCH
    @Filter({"c1-1", "c1-2"})
    @Action("custom-${#METHOD}-${#CLASS}")
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface CustomAction {

    }

    @ActionAnnotation
    @DELETE
    @Filter({"c2-1"})
    @Action("custom2")
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface CustomAction2 {

    }

    @Filter({"f1", "f2"})
    @CustomAction2
    @PUT
    @CustomAction
    public void action1() {
    }

    @CustomAction
    @CustomAction2
    @Filter({"f3", "f4"})
    @Action("iam-action2")
    public void action2() {
    }

    @HEAD
    public void action3() {
    }

    @Test
    public void test() throws NoSuchMethodException {

        assertNotEquals(Arrays.asList("2", "1"), Arrays.asList("1", "2"));
        assertEquals(Arrays.asList("1", "2"), Arrays.asList("1", "2"));

        Method action1 = AnnotationUtilTest.class.getMethod("action1");
        Method action2 = AnnotationUtilTest.class.getMethod("action2");
        Method action3 = AnnotationUtilTest.class.getMethod("action3");

        assertTrue(AnnotationUtil.isAction(action1));
        assertTrue(AnnotationUtil.isAction(action2));
        assertFalse(AnnotationUtil.isAction(action3));

        assertEquals(AnnotationUtil.getActionAnnoValue(action1), "custom-${#METHOD}-${#CLASS}");
        assertEquals(AnnotationUtil.getActionAnnoValue(action2), "iam-action2");
        assertEquals(AnnotationUtil.getActionAnnoValue(action3), null);

        assertEquals(AnnotationUtil.getFilters(action1),
                Arrays.asList("f1", "f2", "c2-1", "c1-1", "c1-2"));
        assertEquals(AnnotationUtil.getFilters(action2),
                Arrays.asList("c1-1", "c1-2", "c2-1", "f3", "f4"));
        assertEquals(AnnotationUtil.getFilters(action3), null);

        assertEquals(AnnotationUtil.getHttpMethods(action1),
                Arrays.asList("DELETE", "PUT", "GET", "PATCH"));
        assertEquals(AnnotationUtil.getHttpMethods(action2),
                Arrays.asList("GET", "PATCH", "DELETE"));
        assertEquals(AnnotationUtil.getHttpMethods(action3),
                Arrays.asList("HEAD"));
    }
}
