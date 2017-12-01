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
package org.febit.leancloud;

import org.febit.form.Order;
import org.febit.form.PageForm;

/**
 *
 * @author zqq90
 * @param <T>
 */
public class LcQuery<T extends LcQuery> {

    public static LcQuery create() {
        return new LcQuery();
    }

    protected Integer limit;
    protected Integer skip;
    protected String[] order;
    protected String[] keys;
    protected String include;
    protected Condition where;
    protected boolean count = false;

    public T limit(int limit) {
        this.limit = limit;
        return (T) this;
    }

    public T skip(int skip) {
        this.skip = skip;
        return (T) this;
    }

    public T include(String include) {
        this.include = include;
        return (T) this;
    }

    public T order(String... order) {
        this.order = order;
        return (T) this;
    }

    public T page(PageForm pageForm) {
        setLimit(pageForm.getLimit());
        setSkip(pageForm.getLimit() * (pageForm.getPage() - 1));
        return (T) this;
    }

    public T keys(String... keys) {
        this.keys = keys;
        return (T) this;
    }

    public Condition where() {
        Condition c = this.where;
        if (c == null) {
            c = Condition.c().setQuery(this);
            this.where = c;
        }
        return c;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public String[] getOrder() {
        return order;
    }

    public void setOrder(String[] order) {
        this.order = order;
    }

    public void setOrder(Order order) {
        if (order == null) {
            setOrder((String[]) null);
            return;
        }
        String[] array = new String[order.size()];
        for (int i = 0; i < order.size(); i++) {
            Order.Entry entry = order.get(i);
            array[i] = entry.asc ? entry.field : '-' + entry.field;
        }
        setOrder(array);
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public Condition getWhere() {
        return where;
    }

    public void setWhere(Condition where) {
        this.where = where;
    }

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

}
