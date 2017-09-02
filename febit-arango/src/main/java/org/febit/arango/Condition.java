// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package org.febit.arango;

import java.util.Collection;
import java.util.Map;
import org.febit.form.Order;
import org.febit.util.StringUtil;

/**
 * Condition.
 *
 * @author zqq90
 */
public class Condition {

    public static Condition start() {
        return new Condition();
    }

    public static Condition c() {
        return new Condition();
    }

    public static Condition c(String key, Object value) {
        return new Condition().equal(key, value);
    }

    public static Condition c(
            String key, Object value,
            String key2, Object value2
    ) {
        return new Condition()
                .equal(key, value)
                .equal(key2, value2);
    }

    public static Condition c(
            String key, Object value,
            String key2, Object value2,
            String key3, Object value3
    ) {
        return new Condition()
                .equal(key, value)
                .equal(key2, value2)
                .equal(key3, value3);
    }

    protected final BindVars bindVars = new BindVars();
    protected final StringBuilder _filterBuffer = new StringBuilder();
    protected Order _order = null;
    protected int skip = 0;
    protected int limit = 0;

    public Condition() {
    }

    public BindVars vars() {
        return bindVars;
    }

    protected StringBuilder _emitFilter(StringBuilder buf) {
        if (_filterBuffer.length() != 0) {
            buf.append("\n FILTER ")
                    .append(_filterBuffer).append(' ');
        }
        return buf;
    }

    protected StringBuilder _emitLimit(StringBuilder buf) {
        if (limit != 0) {
            buf.append("\n LIMIT ");
            if (skip != 0) {
                buf.append(skip).append(',');
            }
            buf.append(limit).append(' ');
        }
        return buf;
    }

    protected StringBuilder _emitOrder(StringBuilder buf) {
        if (_order != null && !_order.isEmpty()) {
            buf.append("\n SORT ");
            int i = 0;
            for (Order.Entry entry : _order) {
                if (i != 0) {
                    buf.append(',');
                }
                buf.append("d.`").append(ArangoUtil.fixFieldName(entry.field)).append('`');
                if (!entry.asc) {
                    buf.append(" DESC");
                }
                i++;
            }
        }
        return buf;
    }

    protected Order order() {
        if (_order == null) {
            _order = new Order();
        }
        return _order;
    }

    protected StringBuilder filterBuffer() {
        if (_filterBuffer.length() != 0) {
            _filterBuffer.append(" && ");
        }
        return _filterBuffer;
    }

    public String buildForQuery(String table) {
        StringBuilder buf = new StringBuilder();
        buf.append("FOR d in ").append(table).append('\n');
        _emitFilter(buf);
        _emitOrder(buf);
        _emitLimit(buf);
        buf.append("\n RETURN d");
        return buf.toString();
    }

    public String buildForUpdate(String table, Map<String, Object> changes) {
        StringBuilder buf = new StringBuilder();
        buf.append("FOR d in ").append(table).append('\n');
        _emitFilter(buf);

        buf.append("\n UPDATE d WITH {");
        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            buf.append(key)
                    .append(" : @")
                    .append(addBindVar(key, value));
        }
        buf.append("  } IN ").append(table);

        buf.append("\n RETURN OLD._key");
        return buf.toString();
    }

    public String buildForQueryField(String table, String field) {
        StringBuilder buf = new StringBuilder();
        buf.append("FOR d in ").append(table).append('\n');
        _emitFilter(buf);
        _emitOrder(buf);
        _emitLimit(buf);
        buf.append("\n RETURN d.`").append(field).append('`');
        return buf.toString();
    }

    public String buildForQuery(String table, String... fields) {
        StringBuilder buf = new StringBuilder();
        buf.append("FOR d in ").append(table).append('\n');
        _emitFilter(buf);
        _emitOrder(buf);
        _emitLimit(buf);
        buf.append("\n RETURN {");
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (i != 0) {
                buf.append(',');
            }
            buf.append(field).append(':').append("d.`").append(field).append('`');
        }
        buf.append("\n }");
        return buf.toString();
    }

    public String buildForQueryFirst(String table) {
        StringBuilder buf = new StringBuilder();
        buf.append("FOR d in ").append(table).append('\n');
        _emitFilter(buf);
        _emitOrder(buf);
        buf.append("\n LIMIT 1"
                + "\n RETURN d");
        return buf.toString();
    }

    public String buildForExist(String table) {
        StringBuilder buf = new StringBuilder();
        buf.append("FOR d in ").append(table).append('\n');
        _emitFilter(buf);
        buf.append("\n LIMIT 1"
                + "\n COLLECT WITH COUNT INTO length"
                + "\n RETURN length>0");
        return buf.toString();
    }

    public String buildForCount(String table) {
        StringBuilder buf = new StringBuilder();
        buf.append("FOR d in ").append(table).append('\n');
        _emitFilter(buf);
        buf.append("\n COLLECT WITH COUNT INTO length"
                + "\n RETURN length");
        return buf.toString();
    }

    public Condition asc(String... fields) {
        order().asc(fields);
        return this;
    }

    public Condition desc(String... fields) {
        order().desc(fields);
        return this;
    }

    public Condition asc(String field) {
        order().asc(field);
        return this;
    }

    public Condition desc(String field) {
        order().desc(field);
        return this;
    }

    public Condition keywords(String q, String... keys) {
        if (keys == null || keys.length == 0
                || StringUtil.isBlank(q)) {
            return this;
        }
        String bindName = addBindVar("q", '%' + escapeLike(q.trim()) + '%');
        StringBuilder buf = filterBuffer();

        buf.append('(');
        for (int i = 0; i < keys.length; i++) {
            String prop = fixColumnName(keys[i]);
            if (i != 0) {
                buf.append(" || ");
            }
            buf.append("d.`").append(prop)
                    .append("` LIKE @")
                    .append(bindName);
        }
        buf.append(')');
        return this;
    }

    protected String fixColumnName(String prop) {
        return prop.trim();
    }

    protected String escapeLike(String src) {
        if (src == null || src.isEmpty()) {
            return "";
        }
        int len = src.length();
        StringBuilder buffer = new StringBuilder(len + Math.min(len / 2 + 1, 64));
        for (char c : src.toCharArray()) {
            switch (c) {
                case '_':
                case '%':
                case '\\':
                    buffer.append('\\');
            }
            buffer.append(c);
        }
        return buffer.toString();
    }

    public void addBindVarDirect(String name, Object value) {
        this.bindVars.putDirect(name, value);
    }

    public String addBindVar(String name, Object value) {
        return this.bindVars.add(name, value);
    }

    public Condition expr(String expr) {
        filterBuffer().append(expr);
        return this;
    }

    public Condition equal(String prop, Object value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` == @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition notEqual(String prop, Object value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` != @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition like(String prop, String value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` LIKE @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition notLike(String prop, String value) {
        prop = fixColumnName(prop);
        filterBuffer().append("!(d.`").append(prop)
                .append("` LIKE @")
                .append(addBindVar(prop, value))
                .append(')');
        return this;
    }

    public Condition startWith(String prop, String part) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` LIKE @")
                .append(addBindVar(prop, escapeLike(part) + '%'));
        return this;
    }

    public Condition endWith(String prop, String part) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` LIKE @")
                .append(addBindVar(prop, '%' + escapeLike(part)));
        return this;
    }

    public Condition contains(String prop, String part) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` LIKE @")
                .append(addBindVar(prop, '%' + escapeLike(part) + '%'));
        return this;
    }

    public Condition regex(String prop, String regex) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` =~ @")
                .append(addBindVar(prop, regex));
        return this;
    }

    public Condition regexNotMatch(String prop, String regex) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` !~ @")
                .append(addBindVar(prop, regex));
        return this;
    }

    public Condition greater(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` > @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition greaterEqual(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` >= @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition less(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` < @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition lessEqual(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` <= @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition between(String prop, Number left, Number right) {
        prop = fixColumnName(prop);
        greaterEqual(prop, left);
        lessEqual(prop, right);
        return this;
    }

    public Condition in(String prop, short[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition in(String prop, boolean[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition in(String prop, int[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition in(String prop, long[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition in(String prop, float[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition in(String prop, double[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public <T> Condition in(String prop, T... values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition in(String prop, Collection values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition notIn(String prop, boolean[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NOT IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition notIn(String prop, short[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NOT IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition notIn(String prop, int[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NOT IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition notIn(String prop, long[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NOT IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition notIn(String prop, float[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NOT IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition notIn(String prop, double[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NOT IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public <T> Condition notIn(String prop, T... values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NOT IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition notIn(String prop, Collection values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NOT IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    /* ****** Array comparison operators ****** */
    public Condition anyEqual(String prop, Object value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY == @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition anyNotEqual(String prop, Object value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY != @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition anyGreater(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY > @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition anyGreaterEqual(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY >= @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition anyLess(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY < @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition anyLessEqual(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY <= @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition allEqual(String prop, Object value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL == @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition allNotEqual(String prop, Object value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL != @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition allGreater(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL > @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition allGreaterEqual(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL >= @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition allLess(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL < @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition allLessEqual(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL <= @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition noneEqual(String prop, Object value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE == @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition noneNotEqual(String prop, Object value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE != @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition noneGreater(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE > @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition noneGreaterEqual(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE >= @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition noneLess(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE < @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition noneLessEqual(String prop, Number value) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE <= @")
                .append(addBindVar(prop, value));
        return this;
    }

    public Condition allIn(String prop, boolean[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition allIn(String prop, short[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition allIn(String prop, int[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition allIn(String prop, long[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition allIn(String prop, float[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition allIn(String prop, double[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public <T> Condition allIn(String prop, T... values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition allIn(String prop, Collection values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ALL IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition anyIn(String prop, boolean[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition anyIn(String prop, short[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition anyIn(String prop, int[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition anyIn(String prop, long[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition anyIn(String prop, float[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition anyIn(String prop, double[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public <T> Condition anyIn(String prop, T... values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition anyIn(String prop, Collection values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` ANY IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition noneIn(String prop, boolean[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition noneIn(String prop, short[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition noneIn(String prop, int[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition noneIn(String prop, long[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition noneIn(String prop, float[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition noneIn(String prop, double[] values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public <T> Condition noneIn(String prop, T... values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public Condition noneIn(String prop, Collection values) {
        prop = fixColumnName(prop);
        filterBuffer().append("d.`").append(prop)
                .append("` NONE IN @")
                .append(addBindVar(prop, values));
        return this;
    }

    public int getSkip() {
        return skip;
    }

    public int getLimit() {
        return limit;
    }

    public Condition limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Condition limit(int skip, int limit) {
        this.skip = skip;
        this.limit = limit;
        return this;
    }

}
