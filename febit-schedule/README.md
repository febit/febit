Febit Schedule
==============

Task schedule, used cron expression, all writen in Java.

## First glance

~~~~~java
//Create a Scheduler instance.
Scheduler scheduler = new Scheduler();

//Add my task
//
//   my task ----------- +
//   cron -------- +     |  
//                 |     |  
scheduler.addTask("*", myTask);

//Start scheduler
scheduler.start();
...
//Stop scheduler
scheduler.stop();
~~~~~

## About Cron

~~~~~
 + ---------------- minute (0 - 59)
 |  + ------------- hour (0 - 23)
 |  |  + ---------- day of month (1 - 31)
 |  |  |  + ------- month (1 - 12)
 |  |  |  |  + ---- day of week (1 - 7), Monday - Sunday
 |  |  |  |  |  + - year
 |  |  |  |  |  |  
 *  *  *  *  *  *  
~~~~~
### Star

~~~~~
m,n,k   list
*/k     per k
m/k     from m and per k
m-n     from m to n
m-n/k   from m to n and per k
1-6/2,10,11   mixed
~~~~~

### Examples

~~~~~
 */2        every two minute
 *  */2     every two hours
 30  22     22:30 every day
 30  22  *  *  1    22:30 every monday
~~~~

## Hello Task

~~~~~java
public class HelloTask implements Task{

    @Override
    public String getTaskName() {
        return "Hi, I'm HelloTask";
    }

    @Override
    public void execute(TaskContext context) {
        System.out.println("Hello Task!");
    }
}
~~~~~
~~~~~java
//Say hello every minute.
scheduler.addTask("*", new HelloTask());
~~~~

## Advance

### pause & goon

~~~~~java
//Pause all supportable tasks.
scheduler.pauseAllIfSupport();
//goon all paused tasks
scheduler.goonAllIfPaused();
~~~~~

### InitableTask & MatchableTask

### TaskExecutor & thread pool

### TaskContext & time-consuming task


