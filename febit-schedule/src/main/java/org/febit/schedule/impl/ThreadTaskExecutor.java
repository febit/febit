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
package org.febit.schedule.impl;

import org.febit.schedule.Task;
import org.febit.schedule.util.ThreadUtil;

/**
 *
 * @author zqq90
 */
public class ThreadTaskExecutor extends AbstractTaskExecutor {

    protected final String threadNamePrefix;
    protected volatile Thread executeThread;

    public ThreadTaskExecutor(Task task) {
        this("schedule-" + task.getTaskName() + '-', task);
    }

    public ThreadTaskExecutor(String threadNamePrefix, Task task) {
        super(task);
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    protected void run(Runnable runnable) throws Exception {
        Thread thread = new Thread(runnable, threadNamePrefix + ThreadUtil.nextThreadNumber());
        thread.start();
        this.executeThread = thread;
    }

    @Override
    protected void tillStop() {
        ThreadUtil.tillDies(this.executeThread);
    }
}
