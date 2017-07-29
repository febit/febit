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
package org.febit.web.util;

import java.util.List;
import org.febit.service.PageResult;

/**
 *
 * @author zqq90
 */
public class JsonPageUtil {

    public static Object wrap(Object[] rows) {
        return new SimpleJsonPage(rows);
    }

    public static Object wrap(List rows) {
        return new ListJsonPage(rows);
    }

    public static Object wrap(PageResult result) {
        return new PageResultWrapper(result);
    }

    public static final class SimpleJsonPage {

        private final Object[] rows;

        protected SimpleJsonPage(Object[] rows) {
            this.rows = rows;
        }

        public int getPage() {
            return 0;
        }

        public int getTotal() {
            return rows.length;
        }

        public Object[] getRows() {
            return rows;
        }
    }

    public static final class ListJsonPage {

        private final List rows;

        protected ListJsonPage(List rows) {
            this.rows = rows;
        }

        public int getPage() {
            return 0;
        }

        public int getTotal() {
            return rows.size();
        }

        public List getRows() {
            return rows;
        }
    }

    public static class PageResultWrapper {

        private final PageResult results;

        protected PageResultWrapper(PageResult results) {
            this.results = results;
        }

        public int getPage() {
            return results.getPage();
        }

        public List getRows() {
            return results.getResults();
        }

        public long getTotal() {
            return results.getTotalSize();
        }

        public Object getLinks() {
            return results.getLinks();
        }
    }

}
