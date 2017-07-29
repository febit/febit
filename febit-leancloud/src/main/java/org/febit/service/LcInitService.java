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

import org.febit.leancloud.Entity;
import org.febit.leancloud.client.LcApiClient;
import org.febit.leancloud.meta.LcIncrease;

/**
 *
 * @author zqq90
 */
public abstract class LcInitService implements Service {

    public abstract <E extends Entity> LcApiClient getLcClient(Class<E> entityType);

    protected static <E extends Entity> boolean isIncrease(Class<E> entityType) {
        return entityType.getAnnotation(LcIncrease.class) != null;
    }
}
