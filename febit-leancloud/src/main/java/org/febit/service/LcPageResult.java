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

import java.util.List;

/**
 * @author zqq90
 */
public final class LcPageResult implements PageResult {

    private final int page;
    private final int pageSize;
    private long totalSize;
    private List results;
    private Object links;

    public LcPageResult() {
        this(1, 10);
    }

    public LcPageResult(PageForm from) {
        this(from.getPage(), from.getLimit());
    }

    public LcPageResult(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    public int getCurrSize() {
        return results != null ? results.size() : 0;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public List getResults() {
        return results;
    }

    @Override
    public void setResults(List results) {
        this.results = results;
    }

    @Override
    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public Object getLinks() {
        return links;
    }

    @Override
    public void setLinks(Object links) {
        this.links = links;
    }

    @Override
    public int getTotalPage() {
        if (pageSize <= 0) {
            return 1;
        }
        return (int) (totalSize / pageSize + (totalSize % pageSize == 0 ? 0 : 1));
    }
}
