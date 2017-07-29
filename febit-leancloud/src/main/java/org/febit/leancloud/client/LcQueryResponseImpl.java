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
package org.febit.leancloud.client;

import java.util.List;
import org.febit.leancloud.Entity;

/**
 *
 * @author zqq90
 * @param <E>
 */
public class LcQueryResponseImpl<E extends Entity> extends LcBasicResponse implements LcQueryResponse<E> {

    protected int count = -1;
    protected List<E> results;

    @Override
    public List<E> getResults() {
        return results;
    }

    public void setResults(List<E> results) {
        this.results = results;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getCount() {
        return count;
    }

}
