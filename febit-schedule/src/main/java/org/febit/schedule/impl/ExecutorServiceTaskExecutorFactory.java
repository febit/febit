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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import org.febit.schedule.Task;
import org.febit.schedule.TaskExecutor;
import org.febit.schedule.TaskExecutorFactory;

/**
 *
 * @author zqq90
 */
public class ExecutorServiceTaskExecutorFactory implements TaskExecutorFactory {

    protected final ExecutorService executorService;

    public ExecutorServiceTaskExecutorFactory(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public TaskExecutor createTaskExecutor(Task task) {
        return new ExecuterServiceTaskExecutor(task);
    }

    protected class ExecuterServiceTaskExecutor extends AbstractTaskExecutor {

        protected volatile Future<?> _future;

        public ExecuterServiceTaskExecutor(Task task) {
            super(task);
        }

        @Override
        protected void run(Runnable runnable) throws RejectedExecutionException {
            this._future = executorService.submit(runnable);
        }

        @Override
        protected void tillStop() {
            Future<?> future = this._future;
            if (future == null
                    || future.isCancelled()
                    || future.isDone()) {
                return;
            }
            try {
                future.get();
            } catch (CancellationException
                    | InterruptedException
                    | ExecutionException ex) {
                // ignore
            }
        }
    }
}
