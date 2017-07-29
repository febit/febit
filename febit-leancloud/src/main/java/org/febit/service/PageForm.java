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
package org.febit.service;

import org.febit.util.ArraysUtil;

/**
 *
 * @author zqq90
 */
public class PageForm {

    public static int defaultMaxLimit = 200;

    private static final int MIN_PAGE = 1;

    protected int page;
    protected int limit;
    protected String[] asc;
    protected String[] desc;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page > MIN_PAGE ? page : MIN_PAGE;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSafeLimit() {
        return getSafeLimit(defaultMaxLimit);
    }

    public int getSafeLimit(int max) {
        return limit < max ? (limit > 1 ? limit : 1) : max;
    }

    public void setAsc(String[] asc) {
        this.asc = asc;
    }

    public void setDesc(String[] desc) {
        this.desc = desc;
    }

    public void setDefaultOrder(String[] asc, String[] desc) {
        if (this.asc == null && this.desc == null) {
            this.asc = asc;
            this.desc = desc;
        }
    }

    public String[] keepOrder(final String... fields) {
        if (fields == null
                || (this.asc == null && this.desc == null)) {
            return new String[0];
        }
        Order order = new Order();
        if (this.asc != null) {
            for (String item : this.asc) {
                if (ArraysUtil.contains(fields, item)) {
                    order.asc(item);
                }
            }
        }
        if (this.desc != null) {
            for (String item : this.desc) {
                if (ArraysUtil.contains(fields, item)) {
                    order.desc(item);
                }
            }
        }
        return order.export();
    }

    @Override
    public String toString() {
        return "Page{" + "page=" + page + ", limit=" + limit + '}';
    }
}
