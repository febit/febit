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
package org.febit.schedule;

import org.febit.lang.Time;

/**
 *
 * @author zqq90
 */
public interface TaskExecutor {

    int STATE_STOPPED = 1;
    int STATE_PAUSED = 2;
    int STATE_STOPPING = 3;
    int STATE_PAUSING = 4;
    int STATE_RUNNING = 5;
    int STATE_HOLDING = 6;

    void runIfNot(Time time);

    void stopAndWait();

    void goonIfPaused();

    void askforPause();

    void askforStop();

    Task getTask();

    int getState();
}
