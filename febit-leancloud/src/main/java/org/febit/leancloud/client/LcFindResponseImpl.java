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
package org.febit.leancloud.client;

import org.febit.leancloud.Entity;

/**
 *
 * @author zqq90
 * @param <E>
 */
public class LcFindResponseImpl<E extends Entity> extends LcBasicResponse implements LcFindResponse<E> {

    public static <T extends Entity> LcFindResponseImpl create(int statusCode, T entity) {
        LcFindResponseImpl<T> responseImpl = new LcFindResponseImpl<>();
        responseImpl.setStatusCode(statusCode);
        responseImpl.setResult(entity);
        return responseImpl;
    }

    protected E result;

    public LcFindResponseImpl() {
    }

    @Override
    public E getResult() {
        return result;
    }

    public void setResult(E result) {
        this.result = result;
    }
}
