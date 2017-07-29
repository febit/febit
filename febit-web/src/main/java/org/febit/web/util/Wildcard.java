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

import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class Wildcard extends jodd.util.Wildcard {

    public static String[] splitcPathPattern(String path) {
        return StringUtil.splitc(path, PATH_SEPARATORS);
    }

    public static boolean matchPathTokens(String tokens, String[] patterns) {
        return matchTokens(splitcPathPattern(tokens), patterns);
    }

    public static boolean matchPathTokens(String[] tokens, String[] patterns) {
        return matchTokens(tokens, patterns);
    }
}
