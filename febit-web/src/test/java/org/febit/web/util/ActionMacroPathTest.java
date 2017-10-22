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
