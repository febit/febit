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
package org.febit.schedule.cron;

/**
 *
 * @author zqq90
 */
class RangeDivAtom implements Atom, AtomProto {

    final int min;
    final int max;
    final int div;

    RangeDivAtom(int min, int max, int div) {
        this.min = min;
        this.max = max;
        this.div = div;
    }

    @Override
    public boolean match(final int value) {
        final int temp;
        return value <= max
                && (temp = value - min) >= 0
                && (temp % div) == 0;
    }

    @Override
    public int maxNumber(int min, int max) {
        if (min < this.min) {
            min = this.min;
        }
        if (max > this.max) {
            max = this.max;
        }
        return (max - min) / div + 1;
    }

    @Override
    public void render(final IntSet list, int min, int max) {
        if (min < this.min) {
            min = this.min;
        }
        if (max > this.max) {
            max = this.max;
        }
        int step = this.min;
        while (step < min) {
            step += div;
        }
        while (step <= max) {
            list.add(step);
            step += div;
        }
    }
}
