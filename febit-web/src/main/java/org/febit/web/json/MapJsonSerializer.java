/**
 * Copyright 2013 febit.org (support@febit.org)
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

import java.util.Map;
import jodd.json.JsonContext;
import jodd.json.impl.ValueJsonSerializer;

/**
 *
 * @author zqq90
 */
class MapJsonSerializer extends ValueJsonSerializer<Map<?, ?>> {

    @Override
    public void serializeValue(final JsonContext jsonContext, Map<?, ?> map) {
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
    }
}
