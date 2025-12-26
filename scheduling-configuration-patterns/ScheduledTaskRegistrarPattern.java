package com.example.scheduling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled Task Registrar Pattern
 * ================================
 * 
 * Demonstrates programmatic registration of scheduled tasks using ScheduledTaskRegistrar.
 * This pattern provides more flexibility than @Scheduled annotation, allowing dynamic
 * task registration, custom triggers, and runtime configuration.
 * 
 * Key Concepts:
 * ------------
 * 1. ScheduledTaskRegistrar - Central registry for scheduled tasks
 * 2. SchedulingConfigurer - Interface for programmatic scheduling configuration
 * 3. TriggerTask - Task with custom trigger
 * 4. CronTrigger - Cron-based trigger
 * 5. PeriodicTrigger - Periodic execution trigger
 * 6. TaskScheduler - Executes scheduled tasks
 * 7. Dynamic Registration - Register tasks at runtime
 * 
 * Task Registration Methods:
 * -------------------------
 * - addTriggerTask() - Register task with custom trigger
 * - addCronTask() - Register task with cron expression
 * - addFixedRateTask() - Register task with fixed rate
 * - addFixedDelayTask() - Register task with fixed delay
 * - setScheduler() - Set custom task scheduler
 * 
 * Advantages over @Scheduled:
 * --------------------------
 * - Dynamic task registration (add/remove at runtime)
 * - Programmatic trigger creation
 * - Conditional task registration
 * - Custom trigger implementations
 * - Multiple tasks from configuration
 * - Database-driven scheduling
 * - Runtime schedule modification
 * 
 * When to Use:
 * -----------
 * - Dynamic scheduling requirements
 * - Schedule determined at runtime
 * - Database-driven task configuration
 * - Multi-tenant scheduling
 * - Conditional task registration
 * - Custom trigger logic
 * - Task lifecycle management
 * 
 * Best Practices:
 * --------------
 * - Configure custom TaskScheduler
 * - Use appropriate thread pool size
 * - Handle task failures gracefully
 * - Avoid blocking operations in triggers
 * - Clean up registered tasks
 * - Monitor scheduled task execution
 * - Use meaningful task identifiers
 * - Document trigger behavior
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Task Registration
 * Demonstrates programmatic task registration using SchedulingConfigurer
 */
@Configuration
@EnableScheduling
public class ScheduledTaskRegistrarPattern implements SchedulingConfigurer {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Set custom scheduler (optional)
        taskRegistrar.setScheduler(taskScheduler());
        
        // Register fixed rate task (every 5 seconds)
        taskRegistrar.addFixedRateTask(() -> {
            System.out.println("[Fixed Rate Task] Execution at " + 
                             LocalDateTime.now().format(formatter));
        }, 5000);
        
        // Register fixed delay task (5 seconds after completion)
        taskRegistrar.addFixedDelayTask(() -> {
            System.out.println("[Fixed Delay Task] Execution at " + 
                             LocalDateTime.now().format(formatter));
        }, 5000);
        
        // Register cron task (every minute)
        taskRegistrar.addCronTask(() -> {
            System.out.println("[Cron Task] Execution at " + 
                             LocalDateTime.now().format(formatter));
        }, "0 * * * * ?");
    }
    
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
}

/**
 * Example 2: Advanced Task Registration with Custom Triggers
 */
@Configuration
@EnableScheduling
class AdvancedTaskRegistrarConfiguration implements SchedulingConfigurer {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Custom TaskScheduler
        taskRegistrar.setScheduler(customTaskScheduler());
        
        // 1. Register task with CronTrigger
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[CronTrigger] Execution at " + 
                                       LocalDateTime.now().format(formatter)),
                new CronTrigger("0 */5 * * * ?") // Every 5 minutes
            )
        );
        
        // 2. Register task with PeriodicTrigger (fixed delay)
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(Duration.ofSeconds(10));
        periodicTrigger.setFixedRate(false); // Fixed delay
        periodicTrigger.setInitialDelay(Duration.ofSeconds(5));
        
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[PeriodicTrigger] Execution at " + 
                                       LocalDateTime.now().format(formatter)),
                periodicTrigger
            )
        );
        
        // 3. Register task with custom Trigger
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[Custom Trigger] Execution at " + 
                                       LocalDateTime.now().format(formatter)),
                triggerContext -> {
                    // Custom trigger logic: execute every 15 seconds
                    Date lastCompletion = triggerContext.lastCompletionTime();
                    Date now = new Date();
                    
                    if (lastCompletion == null) {
                        // First execution after 5 seconds
                        return new Date(now.getTime() + 5000);
                    }
                    
                    // Next execution 15 seconds after last completion
                    return new Date(lastCompletion.getTime() + 15000);
                }
            )
        );
    }
    
    @Bean
    public TaskScheduler customTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("custom-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}

/**
 * Example 3: Dynamic Task Registration
 * Register tasks based on runtime conditions or configuration
 */
@Configuration
@EnableScheduling
class DynamicTaskRegistrarConfiguration implements SchedulingConfigurer {
    
    // Simulated configuration (could come from database, properties, etc.)
    private boolean enableDailyReport = true;
    private boolean enableHourlySync = true;
    private String reportCron = "0 0 2 * * ?"; // 2 AM daily
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
        
        // Conditionally register daily report task
        if (enableDailyReport) {
            taskRegistrar.addCronTask(
                this::generateDailyReport,
                reportCron
            );
            System.out.println("Registered daily report task with cron: " + reportCron);
        }
        
        // Conditionally register hourly sync task
        if (enableHourlySync) {
            taskRegistrar.addFixedRateTask(
                this::syncData,
                3600000 // 1 hour in milliseconds
            );
            System.out.println("Registered hourly sync task");
        }
        
        // Register tasks from external configuration
        registerTasksFromConfiguration(taskRegistrar);
    }
    
    private void generateDailyReport() {
        System.out.println("[Daily Report] Generating report at " + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    private void syncData() {
        System.out.println("[Hourly Sync] Syncing data at " + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    private void registerTasksFromConfiguration(ScheduledTaskRegistrar taskRegistrar) {
        // Simulate reading from database or configuration file
        TaskConfiguration[] configurations = getTaskConfigurations();
        
        for (TaskConfiguration config : configurations) {
            if (config.isEnabled()) {
                switch (config.getType()) {
                    case CRON:
                        taskRegistrar.addCronTask(
                            () -> executeTask(config.getName()),
                            config.getCronExpression()
                        );
                        break;
                    case FIXED_RATE:
                        taskRegistrar.addFixedRateTask(
                            () -> executeTask(config.getName()),
                            config.getInterval()
                        );
                        break;
                    case FIXED_DELAY:
                        taskRegistrar.addFixedDelayTask(
                            () -> executeTask(config.getName()),
                            config.getInterval()
                        );
                        break;
                }
                System.out.println("Registered task: " + config.getName());
            }
        }
    }
    
    private TaskConfiguration[] getTaskConfigurations() {
        // Simulated configuration
        return new TaskConfiguration[] {
            new TaskConfiguration("Cache Refresh", TaskType.FIXED_RATE, true, 300000, null),
            new TaskConfiguration("Cleanup", TaskType.CRON, true, 0, "0 0 3 * * ?"),
            new TaskConfiguration("Health Check", TaskType.FIXED_DELAY, true, 60000, null)
        };
    }
    
    private void executeTask(String taskName) {
        System.out.println("[" + taskName + "] Execution at " + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("dynamic-task-");
        scheduler.initialize();
        return scheduler;
    }
}

/**
 * Task configuration model
 */
enum TaskType {
    CRON, FIXED_RATE, FIXED_DELAY
}

class TaskConfiguration {
    private String name;
    private TaskType type;
    private boolean enabled;
    private long interval;
    private String cronExpression;
    
    public TaskConfiguration(String name, TaskType type, boolean enabled, 
                           long interval, String cronExpression) {
        this.name = name;
        this.type = type;
        this.enabled = enabled;
        this.interval = interval;
        this.cronExpression = cronExpression;
    }
    
    public String getName() { return name; }
    public TaskType getType() { return type; }
    public boolean isEnabled() { return enabled; }
    public long getInterval() { return interval; }
    public String getCronExpression() { return cronExpression; }
}

/**
 * Example 4: Custom Trigger Implementation
 * Create business-specific scheduling logic
 */
@Configuration
@EnableScheduling
class CustomTriggerConfiguration implements SchedulingConfigurer {
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Business hours trigger (9 AM - 5 PM, weekdays)
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[Business Hours] Task execution at " + 
                                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                new BusinessHoursTrigger()
            )
        );
        
        // Adaptive trigger (adjusts based on load)
        taskRegistrar.addTriggerTask(
            new TriggerTask(
                () -> System.out.println("[Adaptive] Task execution at " + 
                                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                new AdaptiveTrigger()
            )
        );
    }
    
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.initialize();
        return scheduler;
    }
}

/**
 * Custom trigger: Execute only during business hours
 */
class BusinessHoursTrigger implements org.springframework.scheduling.Trigger {
    
    @Override
    public Date nextExecutionTime(org.springframework.scheduling.TriggerContext triggerContext) {
        Date now = new Date();
        LocalDateTime localNow = LocalDateTime.now();
        
        // Check if weekday (Monday-Friday)
        int dayOfWeek = localNow.getDayOfWeek().getValue();
        if (dayOfWeek > 5) { // Saturday or Sunday
            // Schedule for next Monday at 9 AM
            LocalDateTime nextMonday = localNow.plusDays(8 - dayOfWeek).withHour(9).withMinute(0).withSecond(0);
            return java.sql.Timestamp.valueOf(nextMonday);
        }
        
        // Check if within business hours (9 AM - 5 PM)
        int hour = localNow.getHour();
        if (hour < 9) {
            // Schedule for 9 AM today
            LocalDateTime nineAM = localNow.withHour(9).withMinute(0).withSecond(0);
            return java.sql.Timestamp.valueOf(nineAM);
        } else if (hour >= 17) {
            // Schedule for 9 AM next business day
            LocalDateTime nextDay = localNow.plusDays(dayOfWeek == 5 ? 3 : 1)
                                           .withHour(9).withMinute(0).withSecond(0);
            return java.sql.Timestamp.valueOf(nextDay);
        }
        
        // Execute every 30 minutes during business hours
        return new Date(now.getTime() + TimeUnit.MINUTES.toMillis(30));
    }
}

/**
 * Custom trigger: Adaptive interval based on system load
 */
class AdaptiveTrigger implements org.springframework.scheduling.Trigger {
    
    private long baseInterval = 60000; // 1 minute
    
    @Override
    public Date nextExecutionTime(org.springframework.scheduling.TriggerContext triggerContext) {
        Date lastCompletion = triggerContext.lastCompletionTime();
        Date now = new Date();
        
        if (lastCompletion == null) {
            return new Date(now.getTime() + baseInterval);
        }
        
        // Simulate load-based adjustment
        double systemLoad = getSystemLoad();
        long adaptedInterval;
        
        if (systemLoad > 0.8) {
            // High load: reduce frequency (2x interval)
            adaptedInterval = baseInterval * 2;
        } else if (systemLoad < 0.3) {
            // Low load: increase frequency (0.5x interval)
            adaptedInterval = baseInterval / 2;
        } else {
            // Normal load: use base interval
            adaptedInterval = baseInterval;
        }
        
        return new Date(lastCompletion.getTime() + adaptedInterval);
    }
    
    private double getSystemLoad() {
        // Simulate system load (0.0 to 1.0)
        return Math.random();
    }
}

/**
 * Usage Examples:
 * ==============
 * 
 * 1. Basic Task Registration:
 *    @Override
 *    public void configureTasks(ScheduledTaskRegistrar registrar) {
 *        registrar.addFixedRateTask(() -> doTask(), 5000);
 *    }
 * 
 * 2. Cron Task:
 *    registrar.addCronTask(() -> doTask(), "0 0 2 * * ?");
 * 
 * 3. Custom Trigger:
 *    registrar.addTriggerTask(
 *        new TriggerTask(() -> doTask(), new CustomTrigger())
 *    );
 * 
 * 4. Conditional Registration:
 *    if (condition) {
 *        registrar.addFixedRateTask(() -> doTask(), interval);
 *    }
 * 
 * 5. Set Custom Scheduler:
 *    registrar.setScheduler(customTaskScheduler());
 * 
 * Comparison with @Scheduled:
 * ==========================
 * 
 * @Scheduled Annotation:
 * - Declarative, annotation-based
 * - Static schedule configuration
 * - Simple to use
 * - Limited flexibility
 * 
 * ScheduledTaskRegistrar:
 * - Programmatic, configuration-based
 * - Dynamic schedule configuration
 * - More complex
 * - Highly flexible
 * 
 * Common Use Cases:
 * ================
 * - Multi-tenant scheduling (different schedules per tenant)
 * - Database-driven task configuration
 * - Dynamic task registration/deregistration
 * - Business logic-driven scheduling
 * - Custom trigger implementations
 * - Runtime schedule modification
 */
