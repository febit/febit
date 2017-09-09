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
