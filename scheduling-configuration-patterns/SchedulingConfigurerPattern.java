package com.example.scheduling;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Scheduling Configurer Pattern
 * ==============================
 * 
 * Demonstrates the SchedulingConfigurer interface for centralized scheduling configuration.
 * This pattern provides fine-grained control over the scheduling infrastructure including
 * custom task schedulers, error handling, and programmatic task registration.
 * 
 * Key Concepts:
 * ------------
 * 1. SchedulingConfigurer - Interface for scheduling configuration
 * 2. configureTasks() - Register scheduled tasks programmatically
 * 3. Custom TaskScheduler - Configure dedicated scheduler
 * 4. Thread Pool Configuration - Control scheduler thread pool
 * 5. Error Handling - Configure exception handling
 * 6. Graceful Shutdown - Proper task termination
 * 7. Task Registration - Programmatic task scheduling
 * 
 * SchedulingConfigurer Interface:
 * ------------------------------
 * public interface SchedulingConfigurer {
 *     void configureTasks(ScheduledTaskRegistrar taskRegistrar);
 * }
 * 
 * Benefits:
 * --------
 * - Custom TaskScheduler configuration
 * - Centralized scheduling setup
 * - Programmatic task registration
 * - Control over thread pool
 * - Custom error handling
 * - Conditional task registration
 * - Enhanced monitoring
 * 
 * Configuration Options:
 * --------------------
 * - Pool size (number of threads)
 * - Thread naming
 * - Rejection policy
 * - Await termination
 * - Daemon threads
 * - Task decorators
 * - Error handlers
 * 
 * When to Use:
 * -----------
 * - Need custom TaskScheduler
 * - Complex thread pool requirements
 * - Programmatic task registration
 * - Advanced error handling
 * - Custom monitoring/metrics
 * - Conditional scheduling
 * - Dynamic task management
 * 
 * Best Practices:
 * --------------
 * - Configure appropriate pool size
 * - Set meaningful thread names
 * - Enable graceful shutdown
 * - Handle task exceptions
 * - Monitor thread pool metrics
 * - Use task decorators for context propagation
 * - Document scheduler configuration
 * - Test with realistic workloads
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Scheduling Configuration
 * Configure custom TaskScheduler with basic settings
 */
@Configuration
@EnableScheduling
public class SchedulingConfigurerPattern implements SchedulingConfigurer {
    
    @Value("${scheduling.pool.size:10}")
    private int poolSize;
    
    @Value("${scheduling.thread.name.prefix:scheduled-}")
    private String threadNamePrefix;
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Set custom TaskScheduler
        taskRegistrar.setScheduler(taskScheduler());
        
        // Programmatically register tasks (optional)
        taskRegistrar.addFixedRateTask(
            () -> System.out.println("[Programmatic Task] Execution at " + 
                                   java.time.LocalDateTime.now()),
            5000
        );
    }
    
    @Bean(destroyMethod = "shutdown")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(threadNamePrefix);
        scheduler.initialize();
        return scheduler;
    }
}

/**
 * Example 2: Advanced Scheduling Configuration
 * Configure with error handling, graceful shutdown, and monitoring
 */
@Configuration
@EnableScheduling
class AdvancedSchedulingConfiguration implements SchedulingConfigurer {
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(advancedTaskScheduler());
    }
    
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler advancedTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // Thread pool configuration
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("scheduled-task-");
        
        // Graceful shutdown configuration
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        
        // Thread configuration
        scheduler.setDaemon(false);
        scheduler.setThreadPriority(Thread.NORM_PRIORITY);
        
        // Error handling
        scheduler.setErrorHandler(throwable -> {
            System.err.println("Error in scheduled task: " + throwable.getMessage());
            // Log to monitoring system
            // Send alert
            // Record metrics
        });
        
        // Rejection policy (when queue is full)
        scheduler.setRejectedExecutionHandler((r, executor) -> {
            System.err.println("Task rejected: " + r.toString());
            // Log rejection
            // Alert operations team
        });
        
        // Task decorator (for context propagation)
        scheduler.setTaskDecorator(runnable -> {
            // Capture context before task execution
            String context = captureContext();
            
            return () -> {
                try {
                    // Set context for task execution
                    setContext(context);
                    runnable.run();
                } finally {
                    // Clear context after execution
                    clearContext();
                }
            };
        });
        
        scheduler.initialize();
        return scheduler;
    }
    
    private String captureContext() {
        // Capture MDC, SecurityContext, etc.
        return "captured-context";
    }
    
    private void setContext(String context) {
        // Set MDC, SecurityContext, etc.
    }
    
    private void clearContext() {
        // Clear MDC, SecurityContext, etc.
    }
}

/**
 * Example 3: Environment-Specific Scheduling Configuration
 * Different configurations for different environments
 */
@Configuration
@EnableScheduling
class EnvironmentSpecificSchedulingConfiguration implements SchedulingConfigurer {
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(environmentTaskScheduler());
    }
    
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler environmentTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // Environment-specific configuration
        switch (activeProfile) {
            case "production":
                // Production: Higher pool size, strict error handling
                scheduler.setPoolSize(20);
                scheduler.setThreadNamePrefix("prod-scheduled-");
                scheduler.setWaitForTasksToCompleteOnShutdown(true);
                scheduler.setAwaitTerminationSeconds(120);
                
                scheduler.setErrorHandler(throwable -> {
                    System.err.println("[PRODUCTION] Scheduled task error: " + 
                                     throwable.getMessage());
                    // Send to monitoring system
                    // Trigger alerts
                });
                break;
                
            case "staging":
                // Staging: Medium pool size, logging
                scheduler.setPoolSize(10);
                scheduler.setThreadNamePrefix("staging-scheduled-");
                scheduler.setWaitForTasksToCompleteOnShutdown(true);
                scheduler.setAwaitTerminationSeconds(60);
                
                scheduler.setErrorHandler(throwable -> {
                    System.err.println("[STAGING] Scheduled task error: " + 
                                     throwable.getMessage());
                    throwable.printStackTrace();
                });
                break;
                
            case "dev":
            default:
                // Development: Smaller pool, verbose logging
                scheduler.setPoolSize(5);
                scheduler.setThreadNamePrefix("dev-scheduled-");
                scheduler.setWaitForTasksToCompleteOnShutdown(false);
                scheduler.setAwaitTerminationSeconds(10);
                
                scheduler.setErrorHandler(throwable -> {
                    System.err.println("[DEV] Scheduled task error: " + 
                                     throwable.getMessage());
                    throwable.printStackTrace();
                });
                break;
        }
        
        scheduler.initialize();
        return scheduler;
    }
}

/**
 * Example 4: Multiple TaskScheduler Configuration
 * Configure separate schedulers for different task types
 */
@Configuration
@EnableScheduling
class MultipleSchedulerConfiguration implements SchedulingConfigurer {
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Default scheduler for @Scheduled annotations
        taskRegistrar.setScheduler(defaultTaskScheduler());
    }
    
    /**
     * Default scheduler for general scheduled tasks
     */
    @Bean(name = "defaultTaskScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler defaultTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("default-scheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }
    
    /**
     * High-priority scheduler for critical tasks
     */
    @Bean(name = "highPriorityScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler highPriorityScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("high-priority-");
        scheduler.setThreadPriority(Thread.MAX_PRIORITY);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(120);
        scheduler.initialize();
        return scheduler;
    }
    
    /**
     * Background scheduler for low-priority tasks
     */
    @Bean(name = "backgroundScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler backgroundScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3);
        scheduler.setThreadNamePrefix("background-");
        scheduler.setThreadPriority(Thread.MIN_PRIORITY);
        scheduler.setDaemon(true); // Background tasks
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        scheduler.initialize();
        return scheduler;
    }
    
    /**
     * I/O-bound task scheduler (larger pool)
     */
    @Bean(name = "ioTaskScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler ioTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(20); // Larger pool for I/O-bound tasks
        scheduler.setThreadNamePrefix("io-task-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }
    
    /**
     * CPU-bound task scheduler (smaller pool)
     */
    @Bean(name = "cpuTaskScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler cpuTaskScheduler() {
        int processors = Runtime.getRuntime().availableProcessors();
        
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(processors); // One thread per CPU
        scheduler.setThreadNamePrefix("cpu-task-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }
}

/**
 * Example 5: Monitoring-Enhanced Scheduler Configuration
 * Add metrics and monitoring to scheduled tasks
 */
@Configuration
@EnableScheduling
class MonitoringSchedulerConfiguration implements SchedulingConfigurer {
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(monitoredTaskScheduler());
    }
    
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler monitoredTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("monitored-");
        
        // Task decorator with monitoring
        scheduler.setTaskDecorator(runnable -> {
            String taskName = getTaskName(runnable);
            long startTime = System.currentTimeMillis();
            
            return () -> {
                try {
                    System.out.println("[Monitor] Starting task: " + taskName);
                    runnable.run();
                    
                    long duration = System.currentTimeMillis() - startTime;
                    System.out.println("[Monitor] Completed task: " + taskName + 
                                     " in " + duration + "ms");
                    
                    // Record metrics
                    recordTaskMetrics(taskName, duration, true);
                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - startTime;
                    System.err.println("[Monitor] Failed task: " + taskName + 
                                     " after " + duration + "ms - " + e.getMessage());
                    
                    // Record failure metrics
                    recordTaskMetrics(taskName, duration, false);
                    throw e;
                }
            };
        });
        
        scheduler.setErrorHandler(throwable -> {
            System.err.println("[Monitor] Task error: " + throwable.getMessage());
            recordError(throwable);
        });
        
        scheduler.initialize();
        return scheduler;
    }
    
    private String getTaskName(Runnable runnable) {
        // Extract task name from runnable
        return runnable.getClass().getSimpleName();
    }
    
    private void recordTaskMetrics(String taskName, long duration, boolean success) {
        // Record to metrics system (Micrometer, Prometheus, etc.)
        System.out.println("[Metrics] Task: " + taskName + 
                         ", Duration: " + duration + "ms" +
                         ", Success: " + success);
    }
    
    private void recordError(Throwable throwable) {
        // Record error to monitoring system
        System.err.println("[Metrics] Error recorded: " + throwable.getMessage());
    }
}

/**
 * Usage Examples:
 * ==============
 * 
 * 1. Basic Configuration:
 *    @Configuration
 *    @EnableScheduling
 *    class Config implements SchedulingConfigurer {
 *        @Override
 *        public void configureTasks(ScheduledTaskRegistrar registrar) {
 *            registrar.setScheduler(taskScheduler());
 *        }
 *        
 *        @Bean
 *        public TaskScheduler taskScheduler() {
 *            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
 *            scheduler.setPoolSize(10);
 *            scheduler.initialize();
 *            return scheduler;
 *        }
 *    }
 * 
 * 2. With Error Handling:
 *    scheduler.setErrorHandler(throwable -> {
 *        log.error("Scheduled task error", throwable);
 *    });
 * 
 * 3. With Graceful Shutdown:
 *    scheduler.setWaitForTasksToCompleteOnShutdown(true);
 *    scheduler.setAwaitTerminationSeconds(60);
 * 
 * 4. With Task Decorator:
 *    scheduler.setTaskDecorator(runnable -> {
 *        return () -> {
 *            // Pre-execution logic
 *            runnable.run();
 *            // Post-execution logic
 *        };
 *    });
 * 
 * 5. Programmatic Task Registration:
 *    @Override
 *    public void configureTasks(ScheduledTaskRegistrar registrar) {
 *        registrar.setScheduler(taskScheduler());
 *        registrar.addFixedRateTask(() -> doTask(), 5000);
 *    }
 * 
 * Configuration Options:
 * =====================
 * 
 * Thread Pool:
 * - setPoolSize(int) - Number of threads
 * - setThreadNamePrefix(String) - Thread naming
 * - setThreadPriority(int) - Thread priority
 * - setDaemon(boolean) - Daemon threads
 * 
 * Shutdown:
 * - setWaitForTasksToCompleteOnShutdown(boolean)
 * - setAwaitTerminationSeconds(int)
 * 
 * Error Handling:
 * - setErrorHandler(ErrorHandler)
 * - setRejectedExecutionHandler(RejectedExecutionHandler)
 * 
 * Task Decoration:
 * - setTaskDecorator(TaskDecorator)
 * 
 * Best Practices:
 * ==============
 * - Size pool based on workload (CPU vs I/O bound)
 * - Enable graceful shutdown in production
 * - Add error handlers for monitoring
 * - Use task decorators for context propagation
 * - Monitor thread pool metrics
 * - Test with realistic loads
 * - Document configuration decisions
 */
