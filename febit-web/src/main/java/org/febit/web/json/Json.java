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

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import jodd.io.StreamUtil;
import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import jodd.util.UnsafeUtil;
import jodd.util.buffer.FastCharBuffer;

/**
 *
 * @author zqq90
 */
public class Json {

    private static final JsonSerializer SERIALIZER;

    static {
        LongJsonSerializer longJsonSerializer = new LongJsonSerializer();
        LongArrayJsonSerializer logArrayJsonSerializer = new LongArrayJsonSerializer();
        SERIALIZER = new JsonSerializer()
                .deep(true)
                .withSerializer(Object.class, new ObjectJsonSerializer())
                .withSerializer(Map.class, new MapJsonSerializer())
                .withSerializer(long.class, longJsonSerializer)
                .withSerializer(Long.class, longJsonSerializer)
                .withSerializer(long[].class, logArrayJsonSerializer);
    }

    public static void writeTo(Appendable writer, Object source, String... profiles) {
        new InternalJsonContext(SERIALIZER, writer, profiles).serialize(source);
    }

    public static String toJsonString(Object source, String... profiles) {
        final FastCharBuffer fastCharBuffer = new FastCharBuffer();
        new InternalJsonContext(SERIALIZER, fastCharBuffer, profiles).serialize(source);
        return UnsafeUtil.createString(fastCharBuffer.toArray());
    }

    protected static JsonParser newParser() {
        return new JsonParser();
    }

    public static <T> T parse(String raw, Class<T> type) {
        return newParser().parse(raw, type);
    }

    public static <T> T parse(char[] input, Class<T> type) throws IOException {
        return newParser().parse(input, type);
    }

    public static <T> T parse(Reader reader, Class<T> type) throws IOException {
        char[] input = StreamUtil.readChars(reader);
        return parse(input, type);
    }
}
