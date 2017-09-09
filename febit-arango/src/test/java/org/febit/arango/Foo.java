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

/**
 *
 * @author zqq90
 */
public class Foo extends Entity {

    String string;
    String[] strings;
    Bar bar;
    Bar[] bars;
    Foo foo;
    String nullable;
    int i;
    int[] ints;

    @Override
    public String toString() {
        return "Foo{id=" + _id + ", " + "string=" + string + ", strings=" + strings + ", bar=" + bar + ", bars=" + bars + ", foo=" + foo + ", nullable=" + nullable + ", i=" + i + ", ints=" + ints + '}';
    }
}
