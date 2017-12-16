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
package org.febit.schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import org.febit.lang.Time;
import org.febit.schedule.cron.CronParser;
import org.febit.schedule.cron.InvalidCronException;
import org.febit.schedule.cron.Matcher;
import org.febit.schedule.impl.DefaultTaskExecutorFactory;
import org.febit.schedule.impl.ExecutorServiceTaskExecutorFactory;
import org.febit.schedule.util.ThreadUtil;

/**
 *
 * @author zqq90
 */
public class Scheduler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Scheduler.class);

    private static final int TTL = 60 * 1000;
    private static final String THREAD_NAME_PREFIX = "febit-scheduler-";

    private boolean daemon;
    private boolean useNotifyThread;
    private int timeOffset;

    private final Object lock = new Object();
    private final ArrayList<TaskExecutorEntry> executorEntrys;
    private boolean initialized;
    private boolean started;
    private boolean paused;
    private Thread notifyThread;
    private TimerThread timerThread;
    private TaskExecutorFactory executorFactory;

    public Scheduler() {
        this.executorEntrys = new ArrayList<>();
        this.timeOffset = TimeZone.getDefault().getRawOffset();
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public void setTimeZone(int timeZone) {
        timeOffset = timeZone * (60 * 60 * 1000); // timeZone * 1h
    }

    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

    public int getTimeOffset() {
        return timeOffset;
    }

    public void setUseNotifyThread(boolean useNotifyThread) {
        this.useNotifyThread = useNotifyThread;
    }

    public void setExecutorFactory(TaskExecutorFactory executorFactory) {
        this.executorFactory = executorFactory;
    }

    public void setExecutorFactory(ExecutorService executorService) {
        setExecutorFactory(new ExecutorServiceTaskExecutorFactory(executorService));
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isUseNotifyThread() {
        return useNotifyThread;
    }

    public boolean isDaemon() {
        return daemon;
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        synchronized (lock) {
            if (!initialized) {
                if (this.executorFactory == null) {
                    executorFactory = new DefaultTaskExecutorFactory();
                }
                initialized = true;
            }
        }
    }

    public void addTask(String cron, Task task) throws InvalidCronException {
        initialize();
        this.addTask(CronParser.parse(cron), this.executorFactory.createTaskExecutor(task));
    }

    private void addTask(Matcher matcher, TaskExecutor executor) {
        synchronized (this.executorEntrys) {
            this.executorEntrys.add(new TaskExecutorEntry(matcher, executor));
        }
    }

    private TaskExecutorEntry[] getTaskExecutors() {
        synchronized (executorEntrys) {
            return executorEntrys.toArray(new TaskExecutorEntry[executorEntrys.size()]);
        }
    }

    public boolean remove(final Task task) {
        TaskExecutor taskExecutor = null;
        synchronized (this.executorEntrys) {
            for (Iterator<TaskExecutorEntry> it = executorEntrys.iterator(); it.hasNext();) {
                taskExecutor = it.next().executor;
                if (taskExecutor.getTask() == task) {
                    it.remove();
                    break;
                }
            }
        }
        if (taskExecutor != null) {
            taskExecutor.stopAndWait();
            return true;
        }
        return false;
    }

    public void start() throws IllegalStateException, IllegalArgumentException {
        initialize();
        synchronized (lock) {
            if (started) {
                throw new IllegalStateException("Scheduler already started");
            }
            timerThread = new TimerThread();
            timerThread.start();
            // Change the state of the scheduler.
            started = true;
        }
    }

    public void stop() throws IllegalStateException {
        synchronized (lock) {
            if (started) {
                // Interrupts the timer and waits for its death.
                ThreadUtil.interruptAndTillDies(this.timerThread);
                ThreadUtil.interruptAndTillDies(this.notifyThread);
                this.timerThread = null;
                this.notifyThread = null;
                final TaskExecutorEntry[] entrys = getTaskExecutors();
                for (TaskExecutorEntry entry : entrys) {
                    entry.executor.askforStop();
                }
                if (paused) {
                    for (TaskExecutorEntry entry : entrys) {
                        entry.executor.goonIfPaused();
                    }
                    paused = false;
                }
                for (TaskExecutorEntry entry : entrys) {
                    entry.executor.stopAndWait();
                }
                // Change the state of the object.
                started = false;
            } else {
                throw new IllegalStateException("Scheduler not started");
            }
        }
    }

    public void pauseAllIfSupport() throws IllegalStateException {
        synchronized (lock) {
            if (started) {
                if (!paused) {
                    // Interrupts the timer and waits for its death.
                    ThreadUtil.interruptAndTillDies(this.timerThread);
                    ThreadUtil.interruptAndTillDies(this.notifyThread);
                    this.timerThread = null;
                    this.notifyThread = null;
                    final TaskExecutorEntry[] entrys = getTaskExecutors();
                    for (TaskExecutorEntry entry : entrys) {
                        entry.executor.askforPause();
                    }
                    // Change the state of the object.
                    paused = true;
                }
            } else {
                throw new IllegalStateException("Scheduler not started");
            }
        }
    }

    public void goonAllIfPaused() throws IllegalStateException {
        synchronized (lock) {
            if (started) {
                if (paused) {
                    // Interrupts the timer and waits for its death.
                    final TaskExecutorEntry[] entrys = getTaskExecutors();
                    for (TaskExecutorEntry entry : entrys) {
                        entry.executor.goonIfPaused();
                    }
                    timerThread = new TimerThread();
                    timerThread.start();
                    paused = false;
                }
            } else {
                throw new IllegalStateException("Scheduler not started");
            }
        }
    }

    private void click(final long millis) {
        if (this.useNotifyThread) {
            final Thread thread = new Thread(() -> {
                Scheduler.this.notifyAllExecutor(millis);
            }, THREAD_NAME_PREFIX + "notify-" + ThreadUtil.nextThreadNumber());
            if (this.notifyThread != null && this.notifyThread.isAlive()) {
                //FIXME: alert this unexpected state
            }
            thread.setDaemon(this.daemon);
            thread.start();
            this.notifyThread = thread;
        } else {
            this.notifyAllExecutor(millis);
        }
    }

    private void notifyAllExecutor(final long millis) {
        final TaskExecutorEntry[] entrys = getTaskExecutors();
        final Time time = new Time(millis, this.timeOffset);
        for (TaskExecutorEntry entry : entrys) {
            try {
                entry.notify(time);
            } catch (Exception e) {
                LOG.error("Failed to notify executor for task: " + entry.executor.getTask().getTaskName(), e);
            }
        }
    }

    private class TimerThread extends Thread {

        TimerThread() {
            super(THREAD_NAME_PREFIX + "timer-" + ThreadUtil.nextThreadNumber());
        }

        private void safeSleepToMillis(final long nextMillis) throws InterruptedException {
            long sleepMillis = nextMillis - System.currentTimeMillis();
            //MISSTAKE_ALLOW = 200
            while (sleepMillis > 200) {
                Thread.sleep(sleepMillis);
                sleepMillis = nextMillis - System.currentTimeMillis();
            }
        }

        @Override
        public void run() {
            long nextMinuteMillis = ((System.currentTimeMillis() / TTL) + 1) * TTL;
            // Work until the scheduler is started.
            for (;;) {
                try {
                    safeSleepToMillis(nextMinuteMillis);
                    Scheduler.this.click(nextMinuteMillis);
                } catch (InterruptedException e) {
                    // exit if interrupted!
                    break;
                }

                // Calculating next minute.
                do {
                    nextMinuteMillis += TTL;
                } while (nextMinuteMillis < System.currentTimeMillis());
            }
        }
    }

    private final static class TaskExecutorEntry {

        private final Matcher matcher;
        private final TaskExecutor executor;

        TaskExecutorEntry(Matcher matcher, TaskExecutor executor) {
            this.matcher = matcher;
            this.executor = executor;
        }

        void notify(Time time) {
            if (matcher.match(time)) {
                executor.runIfNot(time);
            }
        }
    }

}
