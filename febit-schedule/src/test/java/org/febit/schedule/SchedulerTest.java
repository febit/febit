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

import org.febit.lang.Time;

/**
 *
 * @author zqq90
 */
public class SchedulerTest {

    public static final int ONE_HOUR = 60 * 60 * 1000;

    //@org.testng.annotations.Test
    public void schedule() throws InterruptedException {

        Scheduler scheduler = new Scheduler();

        scheduler.addTask("*", new Task() {

            @Override
            public void execute(TaskContext context) {
                println(" *", context.getTime());
            }

            @Override
            public String getTaskName() {
                return "*";
            }
        });

        scheduler.addTask("*/2", new Task() {

            @Override
            public void execute(TaskContext context) {
                println(" */2", context.getTime());
            }

            @Override
            public String getTaskName() {
                return "*/2";
            }
        });

        scheduler.addTask("*/3", new Task() {

            @Override
            public void execute(TaskContext context) {
                println(" */3", context.getTime());
            }

            @Override
            public String getTaskName() {
                return "*/3";
            }
        });

        scheduler.addTask("40,41,42,43,44", new Task() {

            @Override
            public void execute(TaskContext context) {
                println(" 40,41,42,43,44", context.getTime());
            }

            @Override
            public String getTaskName() {
                return "list";
            }
        });

        startScheduler(scheduler);
    }

    //@org.testng.annotations.Test
    public void schedule2() {

        final Scheduler scheduler = new Scheduler();

        scheduler.addTask("*", new Task() {

            @Override
            public void execute(TaskContext context) {

                println(" start", context.getTime());
                sleep(90 * 1000);
                println(" end", context.getTime());
            }

            @Override
            public String getTaskName() {
                return "*";
            }
        });

        startScheduler(scheduler);
    }

    //@org.testng.annotations.Test
    public void schedule3() {

        final Scheduler scheduler = new Scheduler();

        scheduler.addTask("*", new Task() {

            @Override
            public void execute(TaskContext context) {

                println(" click", context.getTime());
                throw new RuntimeException("exception");
            }

            @Override
            public String getTaskName() {
                return "exception";
            }
        });

        startScheduler(scheduler);
    }

    //@org.testng.annotations.Test
    public void schedule4() {

        final Scheduler scheduler = new Scheduler();

        scheduler.addTask("*", new MatchableTask() {

            @Override
            public void execute(TaskContext context) {

                println(" click", context.getTime());
            }

            @Override
            public String getTaskName() {
                return "exception";
            }

            @Override
            public boolean match(Time time) {
                boolean result = (time.minute + 1) % 3 == 0;
                println(result ? "run:" : "skip", time);
                return result;
            }
        });

        startScheduler(scheduler);
    }

    public static void println(final String message, final Time time) {
        System.out.print(new StringBuffer("[Scheduler][")
                .append(time.year)
                .append('-')
                .append(time.month)
                .append('-')
                .append(time.day)
                .append(' ')
                .append(time.hour)
                .append(':')
                .append(time.minute)
                .append(']')
                .append(message)
                .append('\n').toString());

    }

    public static void startScheduler(Scheduler scheduler) {
        scheduler.start();
        sleep(ONE_HOUR);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }
}
