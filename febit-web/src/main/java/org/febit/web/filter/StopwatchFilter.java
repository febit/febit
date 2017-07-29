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
package org.febit.web.filter;

import org.febit.util.Stopwatch;
import org.febit.web.ActionRequest;
import org.febit.web.RenderedFilter;

/**
 *
 * @author zqq90
 */
public class StopwatchFilter implements RenderedFilter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StopwatchFilter.class);

    @Override
    public Object invoke(ActionRequest actionRequest) throws Exception {
        Stopwatch stopwatch = Stopwatch.startNew();
        try {
            return actionRequest.invoke();
        } finally {
            stopwatch.stop();
            LOG.info("Action finished in {} ms: {}", stopwatch.nowInMillis(), actionRequest.actionConfig.path);
        }
    }
}
