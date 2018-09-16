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
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.ServiceLoader;
import jodd.introspector.ClassIntrospector;
import jodd.introspector.Getter;
import jodd.introspector.PropertyDescriptor;
import jodd.io.StreamUtil;
import jodd.json.JsonContext;
import jodd.json.JsonException;
import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import jodd.json.TypeJsonSerializer;
import jodd.util.UnsafeUtil;
import jodd.buffer.FastCharBuffer;
import org.febit.util.CollectionUtil;
import org.febit.util.Priority;

/**
 *
 * @author zqq90
 */
public class Json {

    private static final JsonSerializer SERIALIZER;

    static {
        SERIALIZER = new JsonSerializer()
                .deep(true)
                .withSerializer(Object.class, Json::serializeObject)
                .withSerializer(Map.class, (TypeJsonSerializer<Map>) Json::serializeMap)
                .withSerializer(long.class, (TypeJsonSerializer<Long>) Json::serializeLong)
                .withSerializer(Long.class, (TypeJsonSerializer<Long>) Json::serializeLong)
                .withSerializer(long[].class, (TypeJsonSerializer<long[]>) Json::serializeLongArray);

        // apply plugins
        CollectionUtil.read(ServiceLoader.load(JsonSerializerPlugin.class))
                .stream()
                .sorted(Priority.DESC)
                .forEach(p -> p.apply(SERIALIZER));
    }

    public static void writeTo(Appendable writer, Object source, String... profiles) {
        new InternalJsonContext(SERIALIZER, writer, profiles).serialize(source);
    }

    public static String toJsonString(Object source, String... profiles) {
        final FastCharBuffer buf = new FastCharBuffer();
        new InternalJsonContext(SERIALIZER, buf, profiles).serialize(source);
        return buf.toString();
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

    private static boolean serializeObject(final JsonContext jsonContext, final Object source) {
        if (jsonContext.pushValue(source)) {
            // prevent circular dependencies
            return false;
        }
        InternalJsonContext internalJsonContext = (InternalJsonContext) jsonContext;
        internalJsonContext.writeOpenObject();
        final PropertyDescriptor[] propertyDescriptors = ClassIntrospector.get()
                .lookup(source.getClass())
                .getAllPropertyDescriptors();
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
        jsonContext.popValue();
        return true;
    }

    private static boolean serializeMap(final JsonContext jsonContext, Map<?, ?> map) {
        if (jsonContext.pushValue(map)) {
            // prevent circular dependencies
            return false;
        }
        jsonContext.writeOpenObject();
        boolean notfirst = false;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            final Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            final Object key = entry.getKey();
            jsonContext.pushName(key != null ? key.toString() : null, notfirst);
            jsonContext.serialize(value);
            if (!notfirst && jsonContext.isNamePopped()) {
                notfirst = true;
            }
        }
        jsonContext.writeCloseObject();
        jsonContext.popValue();
        return true;
    }

    private static boolean serializeLong(JsonContext jsonContext, Long value) {
        jsonContext.write(new StringBuilder().append('\"').append(value).append('\"'));
        return true;
    }

    private static boolean serializeLongArray(JsonContext jsonContext, long[] array) {
        jsonContext.writeOpenArray();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                jsonContext.writeComma();
            }
            jsonContext.write(new StringBuilder().append('\"').append(array[i]).append('\"'));
        }
        jsonContext.writeCloseArray();
        return true;
    }
}
