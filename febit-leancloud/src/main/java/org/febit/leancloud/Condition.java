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
package org.febit.leancloud;

import java.util.Collection;
import java.util.LinkedHashMap;
import org.febit.util.StringUtil;

/**
 * TODO: $select $dontSelect
 *
 * @author zqq90
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class Condition<T extends Condition> extends LinkedHashMap<String, Object> {

    public static Condition c() {
        return new Condition();
    }

    public static Condition c(String field, Object value) {
        return new Condition()
                .add(field, value);
    }

    protected static Condition c(String field, Object value, String field2, Object value2) {
        return new Condition()
                .add(field, value)
                .add(field2, value2);
    }

    public static Condition c(
            String field, Object value,
            String field2, Object value2,
            String field3, Object value3
    ) {
        return new Condition()
                .add(field, value)
                .add(field2, value2)
                .add(field3, value3);
    }

    public static Condition c(
            String field, Object value,
            String field2, Object value2,
            String field3, Object value3,
            String field4, Object value4
    ) {
        return new Condition()
                .add(field, value)
                .add(field2, value2)
                .add(field3, value3)
                .add(field4, value4);
    }

    public static Condition c(
            String field, Object value,
            String field2, Object value2,
            String field3, Object value3,
            String field4, Object value4,
            String field5, Object value5
    ) {
        return new Condition()
                .add(field, value)
                .add(field2, value2)
                .add(field3, value3)
                .add(field4, value4)
                .add(field5, value5);
    }

    protected LcQuery query;

    public Condition() {
    }

    public T setQuery(LcQuery query) {
        this.query = query;
        return (T) this;
    }

    public LcQuery popQuery() {
        if (this.query == null) {
            this.query = LcQuery.create();
        }
        return this.query;
    }

    public T keywords(String q, String... keys) {
        if (StringUtil.isNotBlank(q)) {
            q = q.trim();
            StringBuilder buffer = new StringBuilder(q.length() * 3 / 2 + 3);
            StringUtil.escapeRegex(buffer, q);
            final int len = keys.length;
            Condition[] array = new Condition[keys.length];
            Condition val = c(
                    "$regex", buffer.toString()
            );
            for (int i = 0; i < len; i++) {
                array[i] = c(keys[i], val);
            }
            or(array);
        }
        return (T) this;
    }

    public T equal(String prop, Object value) {
        put(prop, value);
        return (T) this;
    }

    public T startWith(String prop, String part) {
        if (part != null && !part.isEmpty()) {
            StringBuilder buffer
                    = new StringBuilder(part.length() * 3 / 2 + 3)
                            .append('^');
            StringUtil.escapeRegex(buffer, part);
            return regex(prop, buffer.toString());
        }
        return (T) this;
    }

    public T endWith(String prop, String part) {
        if (part != null && !part.isEmpty()) {
            StringBuilder buffer = new StringBuilder(part.length() * 3 / 2 + 3);
            StringUtil.escapeRegex(buffer, part);
            return regex(prop, buffer.append('$').toString());
        }
        return (T) this;
    }

    public T contains(String prop, String part) {
        if (part != null && !part.isEmpty()) {
            StringBuilder buffer = new StringBuilder(part.length() * 3 / 2 + 3);
            StringUtil.escapeRegex(buffer, part);
            return regex(prop, buffer.toString());
        }
        return (T) this;
    }

    public T add(String key, Object value) {
        put(key, value);
        return (T) this;
    }

    public T and(Condition... list) {
        put("$and", list);
        return (T) this;
    }

    public T or(Condition... list) {
        put("$or", list);
        return (T) this;
    }

    public T and(Collection list) {
        put("$and", list);
        return (T) this;
    }

    public T or(Collection list) {
        put("$or", list);
        return (T) this;
    }

//  public T nor(Collection list) {
//    put("$nor", list);
//    return (T) this;
//  }
    public Condition createIfAbsent(String prop) {
        Object result = get(prop);
        if (result == null) {
            Condition tmp = new Condition();
            put(prop, tmp);
            return tmp;
        }
        if (result instanceof Condition) {
            return (Condition) result;
        }
        throw new LcException("Key already exist and not a 'Condition': " + prop);
    }

    public T regex(String prop, String regex, String flags) {
        createIfAbsent(prop)
                .add("$regex", regex)
                .add("$options", flags);
        return (T) this;
    }

    public T regex(String prop, String regex) {
        createIfAbsent(prop).put("$regex", regex);
        return (T) this;
    }

    public T greater(String prop, Number value) {
        createIfAbsent(prop).put("$gt", value);
        return (T) this;
    }

    public T greaterEqual(String prop, Number value) {
        createIfAbsent(prop).put("$gte", value);
        return (T) this;
    }

    public T less(String prop, Number value) {
        createIfAbsent(prop).put("$lt", value);
        return (T) this;
    }

    public T lessEqual(String prop, Number value) {
        createIfAbsent(prop).put("$lte", value);
        return (T) this;
    }

    public T between(String prop, Number left, Number right) {
        createIfAbsent(prop)
                .add("$gte", left)
                .add("$lte", right);
        return (T) this;
    }

    public T notEqual(String prop, Object value) {
        createIfAbsent(prop).put("$ne", value);
        return (T) this;
    }

    public T in(String prop, int[] values) {
        createIfAbsent(prop).put("$in", values);
        return (T) this;
    }

    public T in(String prop, long[] values) {
        createIfAbsent(prop).put("$in", values);
        return (T) this;
    }

    public T in(String prop, Object... values) {
        createIfAbsent(prop).put("$in", values);
        return (T) this;
    }

    public T in(String prop, Collection values) {
        createIfAbsent(prop).put("$in", values);
        return (T) this;
    }

    public T notIn(String prop, int[] values) {
        createIfAbsent(prop).put("$nin", values);
        return (T) this;
    }

    public T notIn(String prop, long[] values) {
        createIfAbsent(prop).put("$nin", values);
        return (T) this;
    }

    public T notIn(String prop, Object... values) {
        createIfAbsent(prop).put("$nin", values);
        return (T) this;
    }

    public T notIn(String prop, Collection values) {
        createIfAbsent(prop).put("$nin", values);
        return (T) this;
    }

    public T all(String prop, int[] values) {
        createIfAbsent(prop).put("$all", values);
        return (T) this;
    }

    public T all(String prop, long[] values) {
        createIfAbsent(prop).put("$all", values);
        return (T) this;
    }

    public T all(String prop, Object... values) {
        createIfAbsent(prop).put("$all", values);
        return (T) this;
    }

    public T all(String prop, Collection values) {
        createIfAbsent(prop).put("$all", values);
        return (T) this;
    }

    public T exists(String prop, Boolean value) {
        createIfAbsent(prop).put("$exists", value);
        return (T) this;
    }

    public T near(String prop, Number x, Number y, double maxDistance) {
        return near(prop, x, y, maxDistance, -1);
    }

    public T near(String prop, Number x, Number y, double maxDistance,
            double minDistance) {
        Condition ret = createIfAbsent(prop);
        ret.put("$nearSphere", c(
                "__type", "GeoPoint",
                "latitude", x,
                "longitude", y
        ));
        if (maxDistance >= 0) {
            ret.put("$maxDistanceInRadians", maxDistance);
        }
        if (minDistance >= 0) {
            ret.put("$minDistanceInRadians", minDistance);
        }
        return (T) this;
    }

    public T within(String prop, Number x, Number y, Number x2, Number y2) {
        Condition[] coords = new Condition[2];
        coords[0] = c(
                "__type", "GeoPoint",
                "latitude", x,
                "longitude", y
        );
        coords[1] = c(
                "__type", "GeoPoint",
                "latitude", x2,
                "longitude", y2
        );
        createIfAbsent(prop).put("$within", c("$box", coords));
        return (T) this;
    }
}
