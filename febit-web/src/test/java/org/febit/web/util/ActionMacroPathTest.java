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

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author zqq90
 */
@Test
public class ActionMacroPathTest {

    @Test
    public void test() {

        ActionMacroPath.Parser parser = ActionMacroPath.newParser();
        parser.add("/user");
        parser.add("GET:/user");
        parser.add("GET:/user/$id");
        parser.add("GET:/user/1234/send/4567");
        parser.add("GET:/user/$id/send/$to");
        parser.add("GET:/user/$id/fetch/$to");

        ActionMacroPath macroPath = null;

        // direct
        macroPath = parser.parse("/user");
        assertEquals(macroPath.key, "/user");
        assertTrue(macroPath.params.isEmpty());

        macroPath = parser.parse("GET:/user");
        assertEquals(macroPath.key, "GET:/user");
        assertTrue(macroPath.params.isEmpty());

        macroPath = parser.parse("GET:/user/1234/send/4567");
        assertEquals(macroPath.key, "GET:/user/1234/send/4567");
        assertTrue(macroPath.params.isEmpty());

        // dynamic
        macroPath = parser.parse("GET:/user/9527");
        assertEquals(macroPath.key, "GET:/user/$id");
        assertEquals(macroPath.params.get("id"), "9527");

        macroPath = parser.parse("GET:/user/9527/send/12345");
        assertEquals(macroPath.key, "GET:/user/$id/send/$to");
        assertEquals(macroPath.params.size(), 2);
        assertEquals(macroPath.params.get("id"), "9527");
        assertEquals(macroPath.params.get("to"), "12345");

        macroPath = parser.parse("GET:/user/9527/fetch/12345");
        assertEquals(macroPath.key, "GET:/user/$id/fetch/$to");
        assertEquals(macroPath.params.size(), 2);
        assertEquals(macroPath.params.get("id"), "9527");
        assertEquals(macroPath.params.get("to"), "12345");

    }
}
