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
package org.febit.web.render;

import org.febit.web.ActionRequest;
import org.febit.web.util.RenderUtil;

/**
 *
 * @author zqq90
 */
public class JsonData implements Renderable {

    protected String _boxName;
    protected final Object _value;
    protected final String[] profiles;

    public JsonData() {
        this._value = null;
        this.profiles = null;
    }

    public JsonData(Object value) {
        this._value = value;
        this.profiles = null;
    }

    public JsonData(Object value, String... profiles) {
        this._value = value;
        this.profiles = profiles;
    }

    public JsonData box(String boxName) {
        this._boxName = boxName;
        return this;
    }

    @Override
    public Object render(ActionRequest actionRequest) throws Exception {
        RenderUtil.renderJson(actionRequest, _value, _boxName, profiles);
        return null;
    }

    public String[] getProfiles() {
        return profiles;
    }

    public Object getValue() {
        return _value;
    }

    public String getBoxName() {
        return _boxName;
    }
}
