package org.febit.arango;

import java.util.List;
import org.febit.form.PageForm;
import org.febit.service.PageResult;

/**
 * @author zqq90
 */
public final class ArangoPageResult implements PageResult {

    private final int page;
    private final int pageSize;
    private long totalSize;
    private List results;
    private Object links;

    public ArangoPageResult() {
        this(1, 10);
    }

    public ArangoPageResult(PageForm from) {
        this(from.getPage(), from.getLimit());
    }

    public ArangoPageResult(int page, int pageSize) {
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
