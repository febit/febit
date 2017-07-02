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
package org.febit.service;

import java.util.ArrayList;

/**
 *
 * @author zqq90
 */
public class Order {

    protected ArrayList<String> fields = new ArrayList<>();

    public Order asc(String... fields) {
        for (int i = 0, len = fields.length; i < len; i++) {
            asc(fields[i]);
        }
        return this;
    }

    public Order desc(String... fields) {
        for (String field : fields) {
            desc(field);
        }
        return this;
    }

    public Order asc(String field) {
        fields.add(field);
        return this;
    }

    public Order desc(String field) {
        fields.add('-' + field);
        return this;
    }

    public static Order create() {
        return new Order();
    }

    public String[] export() {
        return fields.toArray(new String[fields.size()]);
    }

}
