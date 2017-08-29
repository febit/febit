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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.febit.lang.Defaults;
import org.febit.util.StringUtil;
import org.febit.web.ActionConfig;
import org.febit.web.WebApp;
import org.febit.web.component.ActionManager;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zqq90
 */
public class PermissionUtil {

    public static int[] resolvePermissionMarks(Collection<String> actions) {
        if (actions == null) {
            return Defaults.EMPTY_INTS;
        }
        final ActionManager actionManager = WebApp.component(ActionManager.class);
        int[] marks = new int[(actionManager.getActionCount() >>> 5) + 1];
        for (String action : actions) {
            int id = actionManager.getActionId(action);
            if (id < 0) {
                LoggerFactory.getLogger(PermissionUtil.class).error("Not found ActionConfig for path '{}'", action);
                continue;
            }
            marks = markPermission(marks, id);
        }
        return marks;
    }

    public static void limitRules(Set<String> rules, String ruleRaw) {
        if (ruleRaw == null) {
            return;
        }
        final ActionManager actionManager = WebApp.component(ActionManager.class);
        for (String string : StringUtil.toArrayOmitCommit(ruleRaw)) {
            if (string.charAt(0) == '-') {
                List<String> paths = actionManager.getMatchPaths(string.substring(1).trim());
                if (paths != null) {
                    rules.removeAll(paths);
                }
            } else {
                List<String> paths = actionManager.getMatchPaths(string);
                if (paths != null) {
                    rules.addAll(paths);
                }
            }
        }
    }

    public static boolean checkPermission(int[] marks, ActionConfig actionConfig) {
        return checkPermission(marks, actionConfig.id);
    }

    public static boolean checkPermission(int[] marks, int id) {
        int index = id >>> 5;
        if (index < marks.length) {
            return (marks[index] & (1 << (id - index))) != 0;
        }
        return false;
    }

    public static int[] markPermission(int[] marks, int id) {
        int index = id >>> 5;
        if (index >= marks.length) {
            marks = Arrays.copyOf(marks, index + 1);
        }
        marks[index] = marks[index] | (1 << (id - index));
        return marks;
    }

    public static int[] removePermission(int[] marks, int id) {
        int index = id >>> 5;
        if (index >= marks.length) {
            marks = Arrays.copyOf(marks, index + 1);
        }
        marks[index] = marks[index] & (~(1 << (id - index)));
        return marks;
    }
}
