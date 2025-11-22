package com.spring.patterns.scheduling;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task Scheduler Pattern
 * 
 * Demonstrates the programmatic use of Spring's TaskScheduler interface for scheduling tasks.
 * 
 * Key Features:
 * - Schedule tasks at fixed times
 * - Schedule with delays
 * - Schedule with cron expressions
 * - Schedule with triggers
 * - Cancel scheduled tasks
 * - Dynamic scheduling
 * 
 * Use Cases:
 * - Dynamic task scheduling based on runtime conditions
 * - Scheduling tasks that need to be cancelled
 * - Programmatic control over task execution
 * - Complex scheduling scenarios
 * 
 * @author Spring Patterns
 */
public class TaskSchedulerPattern {

    /**
     * Configuration class for TaskScheduler
     */
    @Configuration
    @EnableScheduling
    static class TaskSchedulerConfig {

        @Bean
        public TaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(10);
            scheduler.setThreadNamePrefix("scheduled-task-");
            scheduler.setAwaitTerminationSeconds(60);
            scheduler.setWaitForTasksToCompleteOnShutdown(true);
            scheduler.initialize();
            return scheduler;
        }

        @Bean
        public TaskSchedulerService taskSchedulerService(TaskScheduler taskScheduler) {
            return new TaskSchedulerService(taskScheduler);
        }
    }

    /**
     * Service demonstrating various TaskScheduler scheduling methods
     */
    static class TaskSchedulerService {
        
        private final TaskScheduler taskScheduler;
        private final AtomicInteger taskCounter = new AtomicInteger(0);

        public TaskSchedulerService(TaskScheduler taskScheduler) {
            this.taskScheduler = taskScheduler;
        }

        /**
         * Schedule a task to run once at a specific time
         */
        public ScheduledFuture<?> scheduleAtFixedTime() {
            Instant startTime = Instant.now().plusSeconds(5);
            
            return taskScheduler.schedule(
                () -> System.out.println("[Fixed Time] Task executed at: " + Instant.now()),
                startTime
            );
        }

        /**
         * Schedule a task with a fixed delay between executions
         */
        public ScheduledFuture<?> scheduleWithFixedDelay() {
            Date startTime = new Date(System.currentTimeMillis() + 2000);
            
            return taskScheduler.scheduleWithFixedDelay(
                () -> {
                    int count = taskCounter.incrementAndGet();
                    System.out.println("[Fixed Delay] Execution #" + count + 
                                     " at: " + Instant.now());
                    
                    // Simulate work
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                },
                startTime,
                Duration.ofSeconds(3)
            );
        }

        /**
         * Schedule a task at a fixed rate
         */
        public ScheduledFuture<?> scheduleAtFixedRate() {
            Date startTime = new Date(System.currentTimeMillis() + 1000);
            
            return taskScheduler.scheduleAtFixedRate(
                () -> System.out.println("[Fixed Rate] Executed at: " + Instant.now()),
                startTime,
                Duration.ofSeconds(2)
            );
        }

        /**
         * Schedule a task with a cron expression
         */
        public ScheduledFuture<?> scheduleWithCron() {
            // Every 5 seconds
            CronTrigger cronTrigger = new CronTrigger("*/5 * * * * ?");
            
            return taskScheduler.schedule(
                () -> System.out.println("[Cron] Task executed at: " + Instant.now()),
                cronTrigger
            );
        }

        /**
         * Schedule a task with a custom trigger
         */
        public ScheduledFuture<?> scheduleWithCustomTrigger() {
            PeriodicTrigger periodicTrigger = new PeriodicTrigger(Duration.ofSeconds(4));
            periodicTrigger.setInitialDelay(Duration.ofSeconds(2));
            periodicTrigger.setFixedRate(false);
            
            return taskScheduler.schedule(
                () -> System.out.println("[Custom Trigger] Task executed at: " + Instant.now()),
                periodicTrigger
            );
        }

        /**
         * Schedule a task that can be cancelled dynamically
         */
        public CancellableTask scheduleCancellableTask() {
            AtomicInteger executionCount = new AtomicInteger(0);
            
            ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
                () -> {
                    int count = executionCount.incrementAndGet();
                    System.out.println("[Cancellable] Execution #" + count + 
                                     " at: " + Instant.now());
                },
                Instant.now().plusSeconds(1),
                Duration.ofSeconds(2)
            );
            
            return new CancellableTask(future, executionCount);
        }

        /**
         * Schedule multiple related tasks
         */
        public void scheduleTaskChain() {
            System.out.println("[Task Chain] Starting task chain...");
            
            // Task 1: Runs after 2 seconds
            taskScheduler.schedule(
                () -> {
                    System.out.println("[Task Chain] Step 1: Data validation completed");
                    
                    // Task 2: Runs 3 seconds after Task 1
                    taskScheduler.schedule(
                        () -> {
                            System.out.println("[Task Chain] Step 2: Data processing completed");
                            
                            // Task 3: Runs 2 seconds after Task 2
                            taskScheduler.schedule(
                                () -> System.out.println("[Task Chain] Step 3: Notification sent"),
                                Instant.now().plusSeconds(2)
                            );
                        },
                        Instant.now().plusSeconds(3)
                    );
                },
                Instant.now().plusSeconds(2)
            );
        }

        /**
         * Schedule a conditional task that reschedules itself
         */
        public void scheduleConditionalTask() {
            AtomicInteger attempts = new AtomicInteger(0);
            
            scheduleWithRetry(attempts);
        }

        private void scheduleWithRetry(AtomicInteger attempts) {
            taskScheduler.schedule(
                () -> {
                    int attempt = attempts.incrementAndGet();
                    System.out.println("[Conditional] Attempt #" + attempt + 
                                     " at: " + Instant.now());
                    
                    // Simulate condition check
                    boolean success = attempt >= 3;
                    
                    if (!success && attempt < 5) {
                        System.out.println("[Conditional] Rescheduling...");
                        scheduleWithRetry(attempts);
                    } else {
                        System.out.println("[Conditional] Task " + 
                                         (success ? "completed successfully" : "failed after max attempts"));
                    }
                },
                Instant.now().plusSeconds(2)
            );
        }
    }

    /**
     * Wrapper for a cancellable scheduled task
     */
    static class CancellableTask {
        private final ScheduledFuture<?> future;
        private final AtomicInteger executionCount;

        public CancellableTask(ScheduledFuture<?> future, AtomicInteger executionCount) {
            this.future = future;
            this.executionCount = executionCount;
        }

        public void cancel() {
            if (!future.isCancelled()) {
                future.cancel(false);
                System.out.println("[Cancellable] Task cancelled after " + 
                                 executionCount.get() + " executions");
            }
        }

        public boolean isCancelled() {
            return future.isCancelled();
        }

        public boolean isDone() {
            return future.isDone();
        }

        public int getExecutionCount() {
            return executionCount.get();
        }
    }

    /**
     * Demonstration of TaskScheduler patterns
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Task Scheduler Pattern Demo ===\n");

        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(TaskSchedulerConfig.class);

        TaskSchedulerService service = context.getBean(TaskSchedulerService.class);

        // 1. Schedule at fixed time
        System.out.println("1. Scheduling task at fixed time (5 seconds from now)...");
        ScheduledFuture<?> fixedTimeTask = service.scheduleAtFixedTime();

        // 2. Schedule with fixed delay
        System.out.println("2. Scheduling task with fixed delay (3 seconds between executions)...");
        ScheduledFuture<?> fixedDelayTask = service.scheduleWithFixedDelay();

        // 3. Schedule at fixed rate
        System.out.println("3. Scheduling task at fixed rate (every 2 seconds)...");
        ScheduledFuture<?> fixedRateTask = service.scheduleAtFixedRate();

        // 4. Schedule with cron expression
        System.out.println("4. Scheduling task with cron (every 5 seconds)...");
        ScheduledFuture<?> cronTask = service.scheduleWithCron();

        // 5. Schedule with custom trigger
        System.out.println("5. Scheduling task with custom trigger...");
        ScheduledFuture<?> customTriggerTask = service.scheduleWithCustomTrigger();

        // 6. Schedule cancellable task
        System.out.println("6. Scheduling cancellable task...");
        CancellableTask cancellableTask = service.scheduleCancellableTask();

        // 7. Schedule task chain
        System.out.println("7. Scheduling task chain...");
        service.scheduleTaskChain();

        // 8. Schedule conditional task
        System.out.println("8. Scheduling conditional task with retry...");
        service.scheduleConditionalTask();

        System.out.println("\n=== Monitoring scheduled tasks ===\n");

        // Let tasks run for a while
        Thread.sleep(10000);

        // Cancel the cancellable task after some executions
        System.out.println("\n=== Cancelling cancellable task ===");
        cancellableTask.cancel();

        // Let remaining tasks run
        Thread.sleep(5000);

        // Cancel all remaining tasks
        System.out.println("\n=== Cancelling all tasks ===");
        fixedDelayTask.cancel(false);
        fixedRateTask.cancel(false);
        cronTask.cancel(false);
        customTriggerTask.cancel(false);

        System.out.println("\n=== Task Status ===");
        System.out.println("Fixed Time Task - Done: " + fixedTimeTask.isDone());
        System.out.println("Fixed Delay Task - Cancelled: " + fixedDelayTask.isCancelled());
        System.out.println("Fixed Rate Task - Cancelled: " + fixedRateTask.isCancelled());
        System.out.println("Cron Task - Cancelled: " + cronTask.isCancelled());
        System.out.println("Custom Trigger Task - Cancelled: " + customTriggerTask.isCancelled());
        System.out.println("Cancellable Task - Total Executions: " + cancellableTask.getExecutionCount());

        context.close();
        System.out.println("\n=== Demo Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. TaskScheduler provides programmatic control over task scheduling
 * 2. Supports multiple scheduling strategies (fixed time, delay, rate, cron)
 * 3. Returns ScheduledFuture for task control and cancellation
 * 4. Enables dynamic scheduling based on runtime conditions
 * 5. Supports complex scheduling scenarios (chaining, conditional)
 * 6. Thread-safe and suitable for concurrent scheduling
 * 
 * BEST PRACTICES:
 * 
 * 1. Configure appropriate thread pool size based on task count
 * 2. Always cancel tasks when no longer needed to prevent resource leaks
 * 3. Use appropriate scheduling method for your use case
 * 4. Handle InterruptedException in scheduled tasks
 * 5. Consider using triggers for complex scheduling logic
 * 6. Monitor task execution and handle failures gracefully
 * 7. Set waitForTasksToCompleteOnShutdown for graceful shutdown
 * 
 * WHEN TO USE:
 * 
 * - Need programmatic control over scheduling
 * - Tasks need to be cancelled dynamically
 * - Complex scheduling logic required
 * - Runtime determination of schedule
 * - Need to monitor or control task execution
 * - Scheduling tasks from non-Spring beans
 */
