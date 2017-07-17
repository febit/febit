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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimeZone;
import org.febit.lang.Time;
import org.febit.schedule.cron.CronParser;
import org.febit.schedule.cron.InvalidCronException;
import org.febit.schedule.cron.Matcher;
import org.febit.schedule.impl.DefaultTaskExecutorFactory;
import org.febit.schedule.util.ThreadUtil;

/**
 *
 * @author zqq90
 */
public final class Scheduler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Scheduler.class);

    private static final int TTL = 60 * 1000;

    private boolean daemon;
    private boolean enableNotifyThread;
    private int timeOffset;

    private final Object lock = new Object();
    private final ArrayList<TaskExecutorEntry> executorEntrys;
    private boolean initialized;
    private boolean started;
    private boolean paused;
    private NotifyThread notifyThread;
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

    public void setEnableNotifyThread(boolean enableNotifyThread) {
        this.enableNotifyThread = enableNotifyThread;
    }

    public void setExecutorFactory(TaskExecutorFactory executorFactory) {
        this.executorFactory = executorFactory;
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
            timerThread = new TimerThread(this);
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
                final TaskExecutorEntry[] entrys = getTaskExecutors();
                for (TaskExecutorEntry entry : entrys) {
                    entry.executor.askforStop();
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
                    timerThread = new TimerThread(this);
                    timerThread.start();
                    paused = false;
                }
            } else {
                throw new IllegalStateException("Scheduler not started");
            }
        }
    }

    private void click(final long millis) {
        if (this.enableNotifyThread) {
            final NotifyThread myNotifyThread = new NotifyThread(this, "webit-scheduler-notify-" + ThreadUtil.nextThreadNumber());
            //XXX: if this.notifyThread != null ??
            this.notifyThread = myNotifyThread;
            myNotifyThread.startNotify(millis);
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

    private final static class NotifyThread extends Thread {

        private final Scheduler scheduler;
        private long millis;

        NotifyThread(Scheduler scheduler, String name) {
            super(name);
            this.scheduler = scheduler;
            this.setDaemon(scheduler.daemon);
        }

        void startNotify(long millis) {
            this.millis = millis;
            this.start();
        }

        @Override
        public void run() {
            this.scheduler.notifyAllExecutor(this.millis);
        }
    }

    private final static class TimerThread extends Thread {

        private final Scheduler scheduler;

        TimerThread(Scheduler scheduler) {
            super("webit-scheduler-timer-" + ThreadUtil.nextThreadNumber());
            this.scheduler = scheduler;
        }

        private static void safeSleepToMillis(long nextMinute) throws InterruptedException {
            long sleepTime = nextMinute - System.currentTimeMillis();
            //MISSTAKE_ALLOW = 200
            while (sleepTime > 200) {
                Thread.sleep(sleepTime);
            }
        }

        @Override
        public void run() {
            long nextMinute = ((System.currentTimeMillis() / TTL) + 1) * TTL;
            // Work until the scheduler is started.
            for (;;) {
                try {
                    safeSleepToMillis(nextMinute);
                    this.scheduler.click(nextMinute);
                } catch (InterruptedException e) {
                    // exit if interrupted!
                    break;
                }

                // Calculating next minute.
                do {
                    nextMinute += TTL;
                } while (nextMinute < System.currentTimeMillis());
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
