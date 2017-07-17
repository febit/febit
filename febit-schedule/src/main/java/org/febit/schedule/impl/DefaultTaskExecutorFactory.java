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
package org.febit.schedule.impl;

import org.febit.schedule.Task;
import org.febit.schedule.TaskExecutor;
import org.febit.schedule.TaskExecutorFactory;

/**
 *
 * @author zqq90
 */
public class DefaultTaskExecutorFactory implements TaskExecutorFactory {

    @Override
    public TaskExecutor createTaskExecutor(Task task) {
        return new DefaultTaskExecutor(task);
    }
}
