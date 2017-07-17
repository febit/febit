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

import java.util.Arrays;

/**
 *
 * @author zqq90
 */
final class IntSet {

    private int[] array;
    private int size;

    IntSet() {
        this(10);
    }

    IntSet(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Invalid capacity: " + initialCapacity);
        }
        array = new int[initialCapacity];
        size = 0;
    }

    int[] toSortedArray() {
        final int[] result = new int[size];
        System.arraycopy(array, 0, result, 0, size);
        Arrays.sort(result);
        return result;
    }

    int get(int index) {
        if (index >= 0 && index < size) {
            return array[index];
        }
        throw new IndexOutOfBoundsException();
    }

    int size() {
        return size;
    }

    void add(int element) {
        if (this.contains(element)) {
            return;
        }
        final int index = this.size++;
        int[] arr = this.array;
        if (index == arr.length) {
            System.arraycopy(arr, 0, arr = this.array = new int[((index * 3) >> 1) + 1], 0, index);
        }
        arr[index] = element;
    }

    boolean contains(int data) {
        for (int i = 0, len = size; i < len; i++) {
            if (array[i] == data) {
                return true;
            }
        }
        return false;
    }
}
