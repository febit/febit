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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jodd.util.collection.SortedArrayList;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class ActionMacroPath {

    public static Parser newParser() {
        return new Parser();
    }

    public final String key;
    public final Map<String, String> params;

    public ActionMacroPath(String key, Map<String, String> params) {
        this.key = key;
        this.params = params;
    }

    public static class Parser {

        protected static final int INDEXERS_SIZE = 10;

        protected final Set<String> directPaths = new HashSet<>();

        @SuppressWarnings("unchecked")
        protected final SortedArrayList<ParserEntry>[] segmentIndexers = new SortedArrayList[INDEXERS_SIZE];
        protected final SortedArrayList<ParserEntry> segmentIndexerX = new SortedArrayList<>();

        {
            // Notice: skip init segmentIndexers[0]
            for (int i = 1; i < INDEXERS_SIZE; i++) {
                segmentIndexers[i] = new SortedArrayList<>();
            }
        }

        public void add(String key) {
            // TODO check ParserEntry conflict
            ParserEntry parserEntry = parseParserEntry(key);
            if (parserEntry == null) {
                directPaths.add(key);
            } else {
                getIndexer(parserEntry.pathParamSegments).add(parserEntry);
            }
        }

        public ActionMacroPath parse(String key) {
            if (directPaths.contains(key)) {
                return new ActionMacroPath(key, Collections.emptyMap());
            }
            String[] pathSegments = pathToSegment(key);
            ParserEntry parserEntry = findParserEntry(pathSegments);
            if (parserEntry == null) {
                return new ActionMacroPath(key, Collections.emptyMap());
            }
            Map<String, String> params = makeMap(parserEntry.paramKeys, parserEntry.exportParams(pathSegments));
            return new ActionMacroPath(parserEntry.key, params);
        }

        protected SortedArrayList<ParserEntry> getIndexer(String[] pathSegments) {
            return pathSegments.length < INDEXERS_SIZE
                    ? segmentIndexers[pathSegments.length]
                    : segmentIndexerX;
        }

        protected ParserEntry findParserEntry(String[] pathSegments) {
            if (pathSegments == null) {
                return null;
            }
            SortedArrayList<ParserEntry> list = getIndexer(pathSegments);
            for (ParserEntry parserEntry : list) {
                if (parserEntry.match(pathSegments)) {
                    return parserEntry;
                }
            }
            return null;
        }
    }

    protected static Map<String, String> makeMap(String[] keys, String[] values) {
        Map<String, String> map = new HashMap<>();
        if (keys.length != values.length) {
            throw new IllegalArgumentException("length of keys and values are not match");
        }
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return Collections.unmodifiableMap(map);
    }

    protected static String[] pathToSegment(String path) {

        if (path == null) {
            return null;
        }

        String[] segments = StringUtil.splitc(path, '/');

        // if start with '/'
        if (segments.length != 0 && segments[0].isEmpty()) {
            segments = Arrays.copyOfRange(segments, 1, segments.length);
        }

        if (segments.length == 0) {
            return null;
        }
        return segments;
    }

    /**
     *
     * @param key
     * @return null if without dynamic params
     */
    protected static ParserEntry parseParserEntry(String key) {
        if (key == null) {
            return null;
        }
        if (!key.contains("/$")) {
            return null;
        }
        String[] segments = pathToSegment(key);
        if (segments == null || segments.length == 0) {
            return null;
        }
        String[] pathSegments = new String[segments.length];
        String[] pathParamSegments = new String[segments.length];
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (!segment.isEmpty()
                    && segment.charAt(0) == '$') {
                pathParamSegments[i] = segment.substring(1);
            } else {
                pathSegments[i] = segment;
            }
        }
        return new ParserEntry(key, pathSegments, pathParamSegments);
    }

    protected static class ParserEntry implements Comparable<ParserEntry> {

        protected final String key;
        protected final String[] pathSegments;
        protected final String[] pathParamSegments;
        protected final String[] paramKeys;

        public boolean match(String[] segments) {
            if (segments.length != this.pathSegments.length) {
                return false;
            }
            for (int i = 0; i < segments.length; i++) {
                String expect = pathSegments[i];
                String segment = segments[i];
                if (expect != null
                        && !expect.equals(segment)) {
                    return false;
                }
            }
            return true;
        }

        public ParserEntry(String key, String[] pathSegments, String[] pathParamSegments) {
            this.key = key;
            this.pathSegments = pathSegments;
            this.pathParamSegments = pathParamSegments;
            List<String> keys = new ArrayList<>();
            for (String segment : pathParamSegments) {
                if (segment != null) {
                    keys.add(segment);
                }
            }
            this.paramKeys = keys.toArray(new String[keys.size()]);
        }

        public String[] exportParams(String[] pathSegments) {
            String[] params = new String[paramKeys.length];
            int index = 0;
            for (int i = 0; i < pathParamSegments.length; i++) {
                String segment = pathParamSegments[i];
                if (segment != null) {
                    params[index++] = pathSegments[i];
                }
            }
            return params;
        }

        @Override
        public int compareTo(ParserEntry o) {
            return Integer.compare(this.paramKeys.length, o.paramKeys.length);
        }
    }

}
