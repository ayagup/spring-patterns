package com.example.scheduling;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled Annotation Pattern
 * ============================
 * 
 * Demonstrates the use of @Scheduled annotation for declarative task scheduling in Spring.
 * The @Scheduled annotation marks methods to be scheduled for execution based on various
 * scheduling strategies: fixed rate, fixed delay, cron expressions, and initial delay.
 * 
 * Key Concepts:
 * ------------
 * 1. @EnableScheduling - Enables Spring's scheduled task execution capability
 * 2. @Scheduled - Marks methods for scheduled execution
 * 3. Fixed Rate - Execute at fixed intervals (ignores execution time)
 * 4. Fixed Delay - Wait fixed time between executions (after completion)
 * 5. Cron Expression - Schedule using cron syntax
 * 6. Initial Delay - Delay before first execution
 * 7. Time Unit - Specify time units (MILLISECONDS, SECONDS, MINUTES, etc.)
 * 
 * Scheduling Attributes:
 * ---------------------
 * - fixedRate: Execute every X time units (parallel if previous not finished)
 * - fixedDelay: Wait X time units after previous execution completes
 * - initialDelay: Wait X time units before first execution
 * - cron: Use cron expression for scheduling
 * - zone: Timezone for cron expression
 * - timeUnit: Time unit for fixedRate/fixedDelay/initialDelay
 * 
 * Fixed Rate vs Fixed Delay:
 * -------------------------
 * Fixed Rate (fixedRate = 1000):
 *   Start: 0s -> End: 2s (task takes 2s)
 *   Start: 1s -> End: 3s (next starts at 1s, even though previous not done)
 *   Start: 2s -> End: 4s
 *   
 * Fixed Delay (fixedDelay = 1000):
 *   Start: 0s -> End: 2s (task takes 2s)
 *   Start: 3s -> End: 5s (waits 1s AFTER previous completes)
 *   Start: 6s -> End: 8s
 * 
 * When to Use:
 * -----------
 * - Periodic tasks (cleanup, data sync, report generation)
 * - Background jobs (email sending, cache refresh)
 * - Monitoring tasks (health checks, metrics collection)
 * - Scheduled data processing
 * - Time-based triggers
 * - Maintenance operations
 * 
 * Best Practices:
 * --------------
 * - Use fixedDelay for dependent tasks (avoid overlap)
 * - Use fixedRate for independent tasks (precise timing)
 * - Use cron for complex schedules (business hours, specific days)
 * - Add initialDelay to prevent startup congestion
 * - Keep scheduled methods lightweight
 * - Handle exceptions (won't reschedule on error)
 * - Use @Async for long-running tasks
 * - Configure thread pool for concurrent tasks
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Configuration class to enable scheduling
 */
@Configuration
@EnableScheduling
public class ScheduledAnnotationPattern {
    // Scheduling is now enabled for this application
}

/**
 * Example 1: Fixed Rate Scheduling
 * Executes at fixed intervals regardless of previous execution completion
 */
@Component
class FixedRateScheduledTask {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private int executionCount = 0;
    
    /**
     * Executes every 5 seconds using fixedRate
     * If previous execution is still running, next execution will still start
     */
    @Scheduled(fixedRate = 5000)
    public void executeFixedRate() {
        System.out.println("[Fixed Rate] Execution #" + (++executionCount) + 
                         " at " + LocalDateTime.now().format(formatter));
    }
    
    /**
     * Executes every 10 seconds with 5 second initial delay
     */
    @Scheduled(fixedRate = 10, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void executeWithInitialDelay() {
        System.out.println("[Fixed Rate with Initial Delay] Execution at " + 
                         LocalDateTime.now().format(formatter));
    }
    
    /**
     * Executes every 1 minute
     */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void executeEveryMinute() {
        System.out.println("[Every Minute] Execution at " + 
                         LocalDateTime.now().format(formatter));
    }
}

/**
 * Example 2: Fixed Delay Scheduling
 * Waits for fixed duration after previous execution completes before starting next
 */
@Component
class FixedDelayScheduledTask {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * Waits 5 seconds after completion before next execution
     * Prevents overlap even if task takes longer than delay
     */
    @Scheduled(fixedDelay = 5000)
    public void executeFixedDelay() {
        System.out.println("[Fixed Delay] Started at " + 
                         LocalDateTime.now().format(formatter));
        
        // Simulate work
        try {
            Thread.sleep(2000); // Takes 2 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[Fixed Delay] Completed at " + 
                         LocalDateTime.now().format(formatter));
        // Next execution will start 5 seconds after this point
    }
    
    /**
     * Waits 30 seconds after completion with 10 second initial delay
     */
    @Scheduled(fixedDelay = 30, initialDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void executeLongDelay() {
        System.out.println("[Long Fixed Delay] Execution at " + 
                         LocalDateTime.now().format(formatter));
    }
}

/**
 * Example 3: Cron Expression Scheduling
 * Uses cron expressions for complex scheduling patterns
 */
@Component
class CronScheduledTask {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Executes every minute at 0 seconds
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 * * * * ?")
    public void executeEveryMinute() {
        System.out.println("[Cron Every Minute] Execution at " + 
                         LocalDateTime.now().format(formatter));
    }
    
    /**
     * Executes every day at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void executeDailyAt2AM() {
        System.out.println("[Cron Daily 2AM] Execution at " + 
                         LocalDateTime.now().format(formatter));
        // Good for nightly batch jobs
    }
    
    /**
     * Executes every weekday (Monday-Friday) at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void executeWeekdaysAt9AM() {
        System.out.println("[Cron Weekdays 9AM] Execution at " + 
                         LocalDateTime.now().format(formatter));
        // Good for business hours tasks
    }
    
    /**
     * Executes every 5 minutes during business hours (9AM-5PM, weekdays)
     */
    @Scheduled(cron = "0 */5 9-17 * * MON-FRI")
    public void executeBusinessHours() {
        System.out.println("[Cron Business Hours] Execution at " + 
                         LocalDateTime.now().format(formatter));
    }
    
    /**
     * Executes at specific times (9AM, 12PM, 6PM) every day
     */
    @Scheduled(cron = "0 0 9,12,18 * * ?")
    public void executeSpecificTimes() {
        System.out.println("[Cron Specific Times] Execution at " + 
                         LocalDateTime.now().format(formatter));
    }
    
    /**
     * Executes on the 1st day of every month at midnight
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void executeMonthly() {
        System.out.println("[Cron Monthly] Execution at " + 
                         LocalDateTime.now().format(formatter));
        // Good for monthly reports
    }
    
    /**
     * Executes in specific timezone (New York)
     */
    @Scheduled(cron = "0 0 12 * * ?", zone = "America/New_York")
    public void executeInTimezone() {
        System.out.println("[Cron with Timezone] Execution at " + 
                         LocalDateTime.now().format(formatter));
    }
}

/**
 * Example 4: Property-driven Scheduling
 * Uses properties for flexible configuration
 */
@Component
class PropertyDrivenScheduledTask {
    
    /**
     * Scheduling configured via properties
     * application.properties:
     *   scheduling.fixedRate=5000
     *   scheduling.fixedDelay=10000
     *   scheduling.cron=0 0 * * * ?
     *   scheduling.initialDelay=5000
     */
    
    @Scheduled(fixedRateString = "${scheduling.fixedRate:5000}")
    public void executeWithPropertyFixedRate() {
        System.out.println("[Property Fixed Rate] Execution at " + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    @Scheduled(fixedDelayString = "${scheduling.fixedDelay:10000}", 
               initialDelayString = "${scheduling.initialDelay:5000}")
    public void executeWithPropertyFixedDelay() {
        System.out.println("[Property Fixed Delay] Execution at " + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    @Scheduled(cron = "${scheduling.cron:0 0 * * * ?}")
    public void executeWithPropertyCron() {
        System.out.println("[Property Cron] Execution at " + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}

/**
 * Example 5: Multiple Schedules on Same Method
 * Method can have multiple @Scheduled annotations
 */
@Component
class MultipleSchedulesTask {
    
    /**
     * Executes at multiple intervals:
     * - Every 10 seconds
     * - Every minute at 0 seconds
     * - Every hour at 0 minutes
     */
    @Scheduled(fixedRate = 10000)
    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 * * * ?")
    public void executeMultipleTimes() {
        System.out.println("[Multiple Schedules] Execution at " + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}

/**
 * Example 6: Conditional Scheduling
 * Enable/disable scheduling based on configuration
 */
@Component
class ConditionalScheduledTask {
    
    /**
     * Only executes if property is set to true
     * application.properties:
     *   scheduling.enabled=true
     */
    @Scheduled(fixedRate = 5000)
    // @ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true")
    public void executeConditionally() {
        System.out.println("[Conditional] Execution at " + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}

/**
 * Example 7: Error Handling in Scheduled Tasks
 * Demonstrates proper exception handling
 */
@Component
class ErrorHandlingScheduledTask {
    
    private int failureCount = 0;
    
    /**
     * Scheduled method with exception handling
     * Note: Uncaught exceptions will prevent future executions
     */
    @Scheduled(fixedRate = 10000)
    public void executeWithErrorHandling() {
        try {
            // Simulate occasional failure
            if (Math.random() < 0.3) {
                throw new RuntimeException("Simulated error");
            }
            
            System.out.println("[Error Handling] Successful execution at " + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        } catch (Exception e) {
            failureCount++;
            System.err.println("[Error Handling] Failed (count: " + failureCount + "): " + 
                             e.getMessage());
            // Log error, send alert, etc.
            // Task will continue to be scheduled
        }
    }
}

/**
 * Example 8: Common Cron Expressions Reference
 */
class CronExpressionReference {
    
    /**
     * Cron Expression Format:
     * second minute hour day-of-month month day-of-week
     * 
     * Special Characters:
     * * - any value
     * ? - no specific value (day-of-month or day-of-week)
     * - - range (10-12)
     * , - list (1,5,10)
     * / - increment (0/15 = every 15 units)
     * L - last (last day of month, last Friday)
     * W - weekday (nearest weekday to given day)
     * # - nth occurrence (2#1 = first Tuesday)
     * 
     * Common Examples:
     */
    
    // @Scheduled(cron = "0 0 * * * ?")           // Every hour
    // @Scheduled(cron = "0 0 0 * * ?")           // Every day at midnight
    // @Scheduled(cron = "0 0 12 * * ?")          // Every day at noon
    // @Scheduled(cron = "0 15 10 * * ?")         // Every day at 10:15 AM
    // @Scheduled(cron = "0 0/5 * * * ?")         // Every 5 minutes
    // @Scheduled(cron = "0 0 0 1 * ?")           // First day of every month
    // @Scheduled(cron = "0 0 0 L * ?")           // Last day of every month
    // @Scheduled(cron = "0 0 0 * * SUN")         // Every Sunday at midnight
    // @Scheduled(cron = "0 0 9-17 * * MON-FRI")  // Weekdays 9AM-5PM every hour
    // @Scheduled(cron = "0 0 0 1 1 ?")           // January 1st every year
}

/**
 * Usage Examples:
 * ==============
 * 
 * 1. Basic Fixed Rate:
 *    @Scheduled(fixedRate = 5000)
 *    public void task() { ... }
 * 
 * 2. Fixed Delay with Initial Delay:
 *    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
 *    public void task() { ... }
 * 
 * 3. Cron Expression:
 *    @Scheduled(cron = "0 0 2 * * ?")
 *    public void task() { ... }
 * 
 * 4. Property-driven:
 *    @Scheduled(fixedRateString = "${app.task.rate}")
 *    public void task() { ... }
 * 
 * 5. Time Unit Specification:
 *    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
 *    public void task() { ... }
 * 
 * Common Pitfalls:
 * ===============
 * - Forgetting @EnableScheduling
 * - Not handling exceptions (stops future executions)
 * - Using fixedRate for dependent tasks (causes overlap)
 * - Long-running tasks blocking scheduler thread
 * - Not considering timezone for cron expressions
 * - Hardcoding schedule values (use properties)
 * - Not configuring thread pool (single thread by default)
 */
