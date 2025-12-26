package com.example.scheduling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Trigger Pattern
 * ===============
 * 
 * Demonstrates the use of Spring's Trigger interface for custom scheduling logic.
 * Triggers determine when scheduled tasks should execute, providing flexible
 * scheduling strategies beyond simple fixed-rate or cron expressions.
 * 
 * Key Concepts:
 * ------------
 * 1. Trigger Interface - Determines next execution time
 * 2. TriggerContext - Provides execution history
 * 3. CronTrigger - Cron expression-based trigger
 * 4. PeriodicTrigger - Fixed rate/delay trigger
 * 5. Custom Triggers - Business logic-driven scheduling
 * 6. nextExecutionTime() - Calculate next execution
 * 7. Execution History - Access to previous execution times
 * 
 * Trigger Interface:
 * -----------------
 * public interface Trigger {
 *     Date nextExecutionTime(TriggerContext triggerContext);
 * }
 * 
 * TriggerContext Methods:
 * ----------------------
 * - lastScheduledExecutionTime() - When task was last scheduled
 * - lastActualExecutionTime() - When task actually started
 * - lastCompletionTime() - When task completed
 * 
 * Built-in Triggers:
 * -----------------
 * 1. CronTrigger - Cron expression scheduling
 * 2. PeriodicTrigger - Fixed rate or fixed delay
 * 
 * Custom Trigger Use Cases:
 * ------------------------
 * - Business hours scheduling
 * - Load-based scheduling
 * - Conditional execution
 * - Variable intervals
 * - Event-driven scheduling
 * - Time zone-aware scheduling
 * - Holiday-aware scheduling
 * 
 * Best Practices:
 * --------------
 * - Return null to stop scheduling
 * - Handle timezone conversions
 * - Validate trigger logic thoroughly
 * - Consider execution duration
 * - Avoid expensive computations in nextExecutionTime()
 * - Log trigger decisions for debugging
 * - Test edge cases (midnight, DST changes, etc.)
 * - Document trigger behavior
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: CronTrigger Usage
 * Standard cron expression-based scheduling
 */
@Configuration
@EnableScheduling
public class TriggerPattern implements SchedulingConfigurer {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
        
        // CronTrigger: Every minute
        CronTrigger everyMinute = new CronTrigger("0 * * * * ?");
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[CronTrigger Every Minute] " + 
                                       LocalDateTime.now().format(formatter)),
                everyMinute
            )
        );
        
        // CronTrigger: Weekdays at 9 AM (with timezone)
        CronTrigger weekdaysMorning = new CronTrigger("0 0 9 * * MON-FRI", 
                                                       ZoneId.of("America/New_York"));
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[CronTrigger Weekdays 9AM EST] " + 
                                       LocalDateTime.now().format(formatter)),
                weekdaysMorning
            )
        );
        
        // CronTrigger: Last day of month at midnight
        CronTrigger lastDayOfMonth = new CronTrigger("0 0 0 L * ?");
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[CronTrigger Last Day of Month] " + 
                                       LocalDateTime.now().format(formatter)),
                lastDayOfMonth
            )
        );
    }
    
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("trigger-task-");
        scheduler.initialize();
        return scheduler;
    }
}

/**
 * Example 2: PeriodicTrigger Usage
 * Fixed rate or fixed delay scheduling
 */
@Configuration
@EnableScheduling
class PeriodicTriggerConfiguration implements SchedulingConfigurer {
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Fixed Rate PeriodicTrigger (every 10 seconds)
        PeriodicTrigger fixedRateTrigger = new PeriodicTrigger(Duration.ofSeconds(10));
        fixedRateTrigger.setFixedRate(true); // Execute at fixed rate
        fixedRateTrigger.setInitialDelay(Duration.ofSeconds(5)); // Wait 5s before first execution
        
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[PeriodicTrigger Fixed Rate] " + 
                                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                fixedRateTrigger
            )
        );
        
        // Fixed Delay PeriodicTrigger (10 seconds after completion)
        PeriodicTrigger fixedDelayTrigger = new PeriodicTrigger(Duration.ofSeconds(10));
        fixedDelayTrigger.setFixedRate(false); // Wait after completion
        
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> {
                    System.out.println("[PeriodicTrigger Fixed Delay] Start: " + 
                                     LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    try {
                        Thread.sleep(3000); // Simulate work
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("[PeriodicTrigger Fixed Delay] End: " + 
                                     LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                },
                fixedDelayTrigger
            )
        );
        
        // PeriodicTrigger with TimeUnit
        PeriodicTrigger minutesTrigger = new PeriodicTrigger(5, TimeUnit.MINUTES);
        minutesTrigger.setFixedRate(true);
        
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[PeriodicTrigger Every 5 Minutes] " + 
                                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                minutesTrigger
            )
        );
    }
}

/**
 * Example 3: Custom Business Hours Trigger
 * Execute only during business hours (9 AM - 5 PM, Monday-Friday)
 */
class BusinessHoursTrigger implements Trigger {
    
    private final Duration interval;
    
    public BusinessHoursTrigger() {
        this(Duration.ofMinutes(30)); // Default: every 30 minutes
    }
    
    public BusinessHoursTrigger(Duration interval) {
        this.interval = interval;
    }
    
    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Date lastCompletion = triggerContext.lastCompletionTime();
        Instant now = Instant.now();
        
        // Start from last completion or now
        Instant nextTime = (lastCompletion != null) 
            ? lastCompletion.toInstant().plus(interval)
            : now;
        
        // Adjust to next business hour if needed
        LocalDateTime localNext = LocalDateTime.ofInstant(nextTime, ZoneId.systemDefault());
        
        // Skip weekends
        while (localNext.getDayOfWeek().getValue() > 5) { // Saturday or Sunday
            localNext = localNext.plusDays(1).withHour(9).withMinute(0).withSecond(0);
        }
        
        // Ensure within business hours (9 AM - 5 PM)
        int hour = localNext.getHour();
        if (hour < 9) {
            localNext = localNext.withHour(9).withMinute(0).withSecond(0);
        } else if (hour >= 17) {
            // Move to 9 AM next business day
            localNext = localNext.plusDays(1).withHour(9).withMinute(0).withSecond(0);
            // Check if next day is weekend
            while (localNext.getDayOfWeek().getValue() > 5) {
                localNext = localNext.plusDays(1);
            }
        }
        
        return Date.from(localNext.atZone(ZoneId.systemDefault()).toInstant());
    }
}

/**
 * Example 4: Adaptive Interval Trigger
 * Adjusts execution interval based on system load or other metrics
 */
class AdaptiveIntervalTrigger implements Trigger {
    
    private final Duration minInterval;
    private final Duration maxInterval;
    private final LoadProvider loadProvider;
    
    public AdaptiveIntervalTrigger(Duration minInterval, Duration maxInterval, LoadProvider loadProvider) {
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.loadProvider = loadProvider;
    }
    
    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Date lastCompletion = triggerContext.lastCompletionTime();
        Instant now = Instant.now();
        
        if (lastCompletion == null) {
            return Date.from(now.plus(minInterval));
        }
        
        // Get current system load (0.0 to 1.0)
        double load = loadProvider.getCurrentLoad();
        
        // Calculate adaptive interval
        // High load -> longer interval (reduce frequency)
        // Low load -> shorter interval (increase frequency)
        long intervalMillis = minInterval.toMillis() + 
            (long) ((maxInterval.toMillis() - minInterval.toMillis()) * load);
        
        Duration adaptedInterval = Duration.ofMillis(intervalMillis);
        Instant nextTime = lastCompletion.toInstant().plus(adaptedInterval);
        
        return Date.from(nextTime);
    }
}

interface LoadProvider {
    double getCurrentLoad(); // Returns value between 0.0 and 1.0
}

/**
 * Example 5: Conditional Execution Trigger
 * Execute only when certain conditions are met
 */
class ConditionalTrigger implements Trigger {
    
    private final Duration checkInterval;
    private final ExecutionCondition condition;
    
    public ConditionalTrigger(Duration checkInterval, ExecutionCondition condition) {
        this.checkInterval = checkInterval;
        this.condition = condition;
    }
    
    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Date lastActual = triggerContext.lastActualExecutionTime();
        Instant now = Instant.now();
        
        // Check condition
        if (condition.shouldExecute()) {
            // Condition met, schedule for immediate execution
            return Date.from(now);
        } else {
            // Condition not met, check again after interval
            Instant nextCheck = (lastActual != null)
                ? lastActual.toInstant().plus(checkInterval)
                : now.plus(checkInterval);
            
            return Date.from(nextCheck);
        }
    }
}

interface ExecutionCondition {
    boolean shouldExecute();
}

/**
 * Example 6: Exponential Backoff Trigger
 * Increases interval exponentially on failures, resets on success
 */
class ExponentialBackoffTrigger implements Trigger {
    
    private final Duration initialDelay;
    private final Duration maxDelay;
    private final double multiplier;
    private Duration currentDelay;
    private boolean lastExecutionFailed = false;
    
    public ExponentialBackoffTrigger(Duration initialDelay, Duration maxDelay, double multiplier) {
        this.initialDelay = initialDelay;
        this.maxDelay = maxDelay;
        this.multiplier = multiplier;
        this.currentDelay = initialDelay;
    }
    
    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Date lastCompletion = triggerContext.lastCompletionTime();
        Instant now = Instant.now();
        
        if (lastCompletion == null) {
            return Date.from(now.plus(initialDelay));
        }
        
        // Adjust delay based on success/failure
        if (lastExecutionFailed) {
            // Increase delay exponentially
            long newDelayMillis = (long) (currentDelay.toMillis() * multiplier);
            currentDelay = Duration.ofMillis(Math.min(newDelayMillis, maxDelay.toMillis()));
        } else {
            // Reset to initial delay on success
            currentDelay = initialDelay;
        }
        
        Instant nextTime = lastCompletion.toInstant().plus(currentDelay);
        return Date.from(nextTime);
    }
    
    public void recordFailure() {
        this.lastExecutionFailed = true;
    }
    
    public void recordSuccess() {
        this.lastExecutionFailed = false;
    }
}

/**
 * Example 7: Time Window Trigger
 * Execute only within specific time windows
 */
class TimeWindowTrigger implements Trigger {
    
    private final int startHour;
    private final int endHour;
    private final Duration interval;
    
    public TimeWindowTrigger(int startHour, int endHour, Duration interval) {
        this.startHour = startHour;
        this.endHour = endHour;
        this.interval = interval;
    }
    
    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Date lastCompletion = triggerContext.lastCompletionTime();
        LocalDateTime now = LocalDateTime.now();
        
        LocalDateTime nextTime;
        if (lastCompletion == null) {
            nextTime = now;
        } else {
            nextTime = LocalDateTime.ofInstant(lastCompletion.toInstant(), ZoneId.systemDefault())
                                  .plus(interval);
        }
        
        int hour = nextTime.getHour();
        
        // If before window, move to window start
        if (hour < startHour) {
            nextTime = nextTime.withHour(startHour).withMinute(0).withSecond(0);
        }
        // If after window, move to next day's window start
        else if (hour >= endHour) {
            nextTime = nextTime.plusDays(1)
                             .withHour(startHour)
                             .withMinute(0)
                             .withSecond(0);
        }
        
        return Date.from(nextTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}

/**
 * Example 8: One-Time Trigger
 * Execute only once at a specific time
 */
class OneTimeTrigger implements Trigger {
    
    private final Instant executionTime;
    private boolean executed = false;
    
    public OneTimeTrigger(Instant executionTime) {
        this.executionTime = executionTime;
    }
    
    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        if (executed || Instant.now().isAfter(executionTime)) {
            // Already executed or time passed, don't schedule again
            return null; // Returning null stops scheduling
        }
        
        executed = true;
        return Date.from(executionTime);
    }
}

/**
 * Usage in Configuration
 */
@Configuration
@EnableScheduling
class CustomTriggerConfiguration implements SchedulingConfigurer {
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Business hours trigger
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[Business Hours] Processing at " + 
                                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                new BusinessHoursTrigger(Duration.ofMinutes(15))
            )
        );
        
        // Adaptive interval trigger
        LoadProvider loadProvider = () -> Math.random(); // Simulated load
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[Adaptive] Processing at " + 
                                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                new AdaptiveIntervalTrigger(Duration.ofSeconds(10), Duration.ofMinutes(5), loadProvider)
            )
        );
        
        // Time window trigger (9 AM - 5 PM)
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[Time Window] Processing at " + 
                                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                new TimeWindowTrigger(9, 17, Duration.ofMinutes(30))
            )
        );
        
        // One-time trigger (execute in 1 hour)
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[One-Time] Executed at " + 
                                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                new OneTimeTrigger(Instant.now().plus(Duration.ofHours(1)))
            )
        );
    }
}

/**
 * Usage Examples:
 * ==============
 * 
 * 1. CronTrigger:
 *    CronTrigger trigger = new CronTrigger("0 0 9 * * MON-FRI");
 * 
 * 2. PeriodicTrigger (Fixed Rate):
 *    PeriodicTrigger trigger = new PeriodicTrigger(Duration.ofSeconds(10));
 *    trigger.setFixedRate(true);
 * 
 * 3. Custom Trigger:
 *    Trigger trigger = triggerContext -> {
 *        // Calculate next execution time
 *        return new Date(System.currentTimeMillis() + 60000);
 *    };
 * 
 * 4. Stop Scheduling:
 *    return null; // In nextExecutionTime()
 * 
 * Key Differences:
 * ===============
 * 
 * CronTrigger:
 * - Complex time patterns
 * - Calendar-based
 * - Timezone support
 * 
 * PeriodicTrigger:
 * - Simple intervals
 * - Fixed rate or delay
 * - Initial delay support
 * 
 * Custom Trigger:
 * - Business logic
 * - Dynamic intervals
 * - Conditional execution
 */
