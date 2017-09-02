// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package org.febit.arango;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zqq90
 */
public class BindVars extends HashMap<String, Object> {

    private int _nextColId = 0;

    protected String nextColId() {
        return nextColId(null);
    }

    protected String nextColId(String name) {
        StringBuilder buf = new StringBuilder();
        buf.append("_c_").append(_nextColId++);
        if (name != null) {
            buf.append('_').append(name.trim());
        }
        return buf.toString();
    }

    @Override
    @Deprecated
    public Object put(String key, Object value) {
        return super.put(key, value);
    }

    public Object putDirect(String key, Object value) {
        return super.put(key, value);
    }

    @Override
    @Deprecated
    public void putAll(Map<? extends String, ? extends Object> m) {
        super.putAll(m);
    }

    @Override
    @Deprecated
    public Object remove(Object key) {
        return super.remove(key);
    }

    public String add(String prop, Object value) {
        String bindName = nextColId(prop);
        super.put(bindName, value);
        return bindName;
    }
}
