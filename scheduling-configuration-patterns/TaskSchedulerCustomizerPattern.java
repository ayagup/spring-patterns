package com.example.scheduling;

import org.springframework.boot.autoconfigure.task.TaskSchedulingProperties;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;

/**
 * Task Scheduler Customizer Pattern
 * ==================================
 * 
 * Demonstrates the use of TaskSchedulerCustomizer for customizing Spring Boot's
 * auto-configured TaskScheduler. This pattern provides a callback-based approach
 * to apply common customizations across all TaskScheduler beans in the application.
 * 
 * Key Concepts:
 * ------------
 * 1. TaskSchedulerCustomizer - Callback interface for scheduler customization
 * 2. Spring Boot Auto-configuration - Automatic scheduler setup
 * 3. Customizer Composition - Multiple customizers applied in order
 * 4. ThreadPoolTaskScheduler - Spring's TaskScheduler implementation
 * 5. Common Customizations - Thread pool, naming, shutdown, error handling
 * 6. Environment-specific Settings - Different configs per environment
 * 7. Task Decoration - Wrap tasks with additional behavior
 * 
 * TaskSchedulerCustomizer Interface:
 * ----------------------------------
 * @FunctionalInterface
 * public interface TaskSchedulerCustomizer {
 *     void customize(ThreadPoolTaskScheduler taskScheduler);
 * }
 * 
 * How It Works:
 * ------------
 * 1. Spring Boot auto-configures TaskScheduler
 * 2. All TaskSchedulerCustomizer beans are collected
 * 3. Each customizer is applied to the TaskScheduler
 * 4. Customizers are applied in order (use @Order for specific ordering)
 * 5. Customized scheduler is registered as bean
 * 
 * Common Customizations:
 * ---------------------
 * - Thread pool sizing
 * - Thread naming
 * - Graceful shutdown
 * - Error handling
 * - Task decoration
 * - Rejection policies
 * - Thread priorities
 * - Monitoring/metrics
 * 
 * Advantages:
 * ----------
 * - Centralized configuration
 * - Reusable across projects
 * - Clean separation of concerns
 * - Easy to test
 * - Composable (multiple customizers)
 * - Works with Spring Boot auto-configuration
 * - No need to define scheduler bean manually
 * 
 * When to Use:
 * -----------
 * - Spring Boot applications
 * - Common scheduler configuration needed
 * - Multiple scheduler customizations
 * - Environment-specific settings
 * - Cross-cutting scheduler concerns
 * - Standardize scheduler setup
 * 
 * Best Practices:
 * --------------
 * - Keep customizers focused (single responsibility)
 * - Use @Order for customizer ordering
 * - Make customizers conditional when needed
 * - Document customizer purpose
 * - Test customizers independently
 * - Avoid duplicate customizations
 * - Use properties for configurable values
 * - Consider thread pool sizing carefully
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Task Scheduler Customizer
 * Simple customization of thread pool and naming
 */
@Configuration
public class TaskSchedulerCustomizerPattern {
    
    /**
     * Basic customizer for thread pool configuration
     */
    @Bean
    public TaskSchedulerCustomizer basicTaskSchedulerCustomizer() {
        return taskScheduler -> {
            // Thread pool sizing
            taskScheduler.setPoolSize(10);
            
            // Thread naming
            taskScheduler.setThreadNamePrefix("scheduled-task-");
            
            // Graceful shutdown
            taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
            taskScheduler.setAwaitTerminationSeconds(60);
        };
    }
}

/**
 * Example 2: Production-Ready Customizer
 * Comprehensive customization for production environments
 */
@Configuration
class ProductionTaskSchedulerCustomizer {
    
    @Bean
    public TaskSchedulerCustomizer productionCustomizer() {
        return taskScheduler -> {
            // Thread pool configuration
            int processors = Runtime.getRuntime().availableProcessors();
            taskScheduler.setPoolSize(processors * 2);
            taskScheduler.setThreadNamePrefix("prod-scheduled-");
            
            // Thread configuration
            taskScheduler.setThreadPriority(Thread.NORM_PRIORITY);
            taskScheduler.setDaemon(false); // Keep JVM alive
            
            // Graceful shutdown (important for production)
            taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
            taskScheduler.setAwaitTerminationSeconds(120); // Wait up to 2 minutes
            
            // Error handling
            taskScheduler.setErrorHandler(throwable -> {
                System.err.println("[PRODUCTION] Scheduled task error: " + 
                                 throwable.getMessage());
                // Log to centralized logging
                // Send alert to monitoring system
                // Record metrics
                throwable.printStackTrace();
            });
            
            // Rejection policy
            taskScheduler.setRejectedExecutionHandler((r, executor) -> {
                System.err.println("[PRODUCTION] Task rejected - thread pool exhausted");
                // Alert operations team
                // Scale up if needed
            });
            
            // Task decorator for context propagation
            taskScheduler.setTaskDecorator(new ContextPropagatingTaskDecorator());
        };
    }
}

/**
 * Example 3: Multiple Customizers with Ordering
 * Demonstrate composition of multiple customizers
 */
@Configuration
class MultipleCustomizersConfiguration {
    
    /**
     * First customizer: Basic thread pool setup
     */
    @Bean
    @org.springframework.core.annotation.Order(1)
    public TaskSchedulerCustomizer threadPoolCustomizer() {
        return taskScheduler -> {
            int processors = Runtime.getRuntime().availableProcessors();
            taskScheduler.setPoolSize(processors * 2);
            System.out.println("Applied thread pool customization");
        };
    }
    
    /**
     * Second customizer: Thread naming
     */
    @Bean
    @org.springframework.core.annotation.Order(2)
    public TaskSchedulerCustomizer namingCustomizer() {
        return taskScheduler -> {
            taskScheduler.setThreadNamePrefix("app-scheduled-");
            taskScheduler.setThreadGroupName("scheduled-tasks");
            System.out.println("Applied naming customization");
        };
    }
    
    /**
     * Third customizer: Shutdown configuration
     */
    @Bean
    @org.springframework.core.annotation.Order(3)
    public TaskSchedulerCustomizer shutdownCustomizer() {
        return taskScheduler -> {
            taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
            taskScheduler.setAwaitTerminationSeconds(60);
            System.out.println("Applied shutdown customization");
        };
    }
    
    /**
     * Fourth customizer: Error handling
     */
    @Bean
    @org.springframework.core.annotation.Order(4)
    public TaskSchedulerCustomizer errorHandlingCustomizer() {
        return taskScheduler -> {
            taskScheduler.setErrorHandler(throwable -> {
                System.err.println("Scheduled task error: " + throwable.getMessage());
                throwable.printStackTrace();
            });
            System.out.println("Applied error handling customization");
        };
    }
}

/**
 * Example 4: Environment-Specific Customizers
 * Different configurations for different environments
 */
@Configuration
class EnvironmentSpecificCustomizers {
    
    /**
     * Development environment customizer
     */
    @Bean
    @org.springframework.context.annotation.Profile("dev")
    public TaskSchedulerCustomizer devTaskSchedulerCustomizer() {
        return taskScheduler -> {
            // Small pool for development
            taskScheduler.setPoolSize(5);
            taskScheduler.setThreadNamePrefix("dev-scheduled-");
            
            // Quick shutdown in dev
            taskScheduler.setWaitForTasksToCompleteOnShutdown(false);
            taskScheduler.setAwaitTerminationSeconds(10);
            
            // Verbose error handling for debugging
            taskScheduler.setErrorHandler(throwable -> {
                System.err.println("[DEV] Scheduled task error:");
                throwable.printStackTrace();
            });
            
            System.out.println("Development scheduler customization applied");
        };
    }
    
    /**
     * Staging environment customizer
     */
    @Bean
    @org.springframework.context.annotation.Profile("staging")
    public TaskSchedulerCustomizer stagingTaskSchedulerCustomizer() {
        return taskScheduler -> {
            // Medium pool for staging
            taskScheduler.setPoolSize(10);
            taskScheduler.setThreadNamePrefix("staging-scheduled-");
            
            // Moderate shutdown time
            taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
            taskScheduler.setAwaitTerminationSeconds(60);
            
            // Standard error handling
            taskScheduler.setErrorHandler(throwable -> {
                System.err.println("[STAGING] Scheduled task error: " + 
                                 throwable.getMessage());
            });
            
            System.out.println("Staging scheduler customization applied");
        };
    }
    
    /**
     * Production environment customizer
     */
    @Bean
    @org.springframework.context.annotation.Profile("production")
    public TaskSchedulerCustomizer prodTaskSchedulerCustomizer() {
        return taskScheduler -> {
            // Larger pool for production
            int processors = Runtime.getRuntime().availableProcessors();
            taskScheduler.setPoolSize(processors * 4);
            taskScheduler.setThreadNamePrefix("prod-scheduled-");
            
            // Graceful shutdown with longer timeout
            taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
            taskScheduler.setAwaitTerminationSeconds(120);
            
            // Production error handling with monitoring
            taskScheduler.setErrorHandler(throwable -> {
                System.err.println("[PRODUCTION] Scheduled task error: " + 
                                 throwable.getMessage());
                // Send to monitoring/alerting system
                // Log to centralized logging
            });
            
            // Task decorator for production monitoring
            taskScheduler.setTaskDecorator(new MonitoringTaskDecorator());
            
            System.out.println("Production scheduler customization applied");
        };
    }
}

/**
 * Example 5: Feature-Specific Customizers
 * Customizers for specific cross-cutting concerns
 */
@Configuration
class FeatureSpecificCustomizers {
    
    /**
     * Security context propagation customizer
     */
    @Bean
    public TaskSchedulerCustomizer securityContextCustomizer() {
        return taskScheduler -> {
            taskScheduler.setTaskDecorator(runnable -> {
                // Capture security context
                Object securityContext = captureSecurityContext();
                
                return () -> {
                    try {
                        // Set security context for task
                        setSecurityContext(securityContext);
                        runnable.run();
                    } finally {
                        // Clear security context
                        clearSecurityContext();
                    }
                };
            });
        };
    }
    
    /**
     * MDC (Mapped Diagnostic Context) propagation customizer
     */
    @Bean
    public TaskSchedulerCustomizer mdcPropagationCustomizer() {
        return taskScheduler -> {
            TaskDecorator existing = taskScheduler.getTaskDecorator();
            
            taskScheduler.setTaskDecorator(runnable -> {
                // Capture MDC
                java.util.Map<String, String> mdcContext = captureMDC();
                
                Runnable decorated = () -> {
                    try {
                        // Set MDC for task
                        setMDC(mdcContext);
                        runnable.run();
                    } finally {
                        // Clear MDC
                        clearMDC();
                    }
                };
                
                // Chain with existing decorator if present
                return (existing != null) ? existing.decorate(decorated) : decorated;
            });
        };
    }
    
    /**
     * Metrics collection customizer
     */
    @Bean
    public TaskSchedulerCustomizer metricsCustomizer() {
        return taskScheduler -> {
            taskScheduler.setTaskDecorator(runnable -> {
                String taskName = getTaskName(runnable);
                
                return () -> {
                    long startTime = System.currentTimeMillis();
                    boolean success = false;
                    
                    try {
                        runnable.run();
                        success = true;
                    } finally {
                        long duration = System.currentTimeMillis() - startTime;
                        recordMetrics(taskName, duration, success);
                    }
                };
            });
        };
    }
    
    // Helper methods
    private Object captureSecurityContext() { return null; }
    private void setSecurityContext(Object context) {}
    private void clearSecurityContext() {}
    
    private java.util.Map<String, String> captureMDC() { return new java.util.HashMap<>(); }
    private void setMDC(java.util.Map<String, String> context) {}
    private void clearMDC() {}
    
    private String getTaskName(Runnable runnable) {
        return runnable.getClass().getSimpleName();
    }
    
    private void recordMetrics(String taskName, long duration, boolean success) {
        System.out.println("[Metrics] Task: " + taskName + 
                         ", Duration: " + duration + "ms, Success: " + success);
    }
}

/**
 * Example 6: Property-Based Customizer
 * Configure scheduler from application properties
 */
@Configuration
class PropertyBasedCustomizer {
    
    @Bean
    public TaskSchedulerCustomizer propertyCustomizer(TaskSchedulingProperties properties) {
        return taskScheduler -> {
            // Pool size from properties
            if (properties.getPool() != null && properties.getPool().getSize() > 0) {
                taskScheduler.setPoolSize(properties.getPool().getSize());
            }
            
            // Thread name prefix from properties
            if (properties.getThreadNamePrefix() != null) {
                taskScheduler.setThreadNamePrefix(properties.getThreadNamePrefix());
            }
            
            // Shutdown configuration from properties
            if (properties.getShutdown() != null) {
                if (properties.getShutdown().isAwaitTermination()) {
                    taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
                    
                    Duration timeout = properties.getShutdown().getAwaitTerminationPeriod();
                    if (timeout != null) {
                        taskScheduler.setAwaitTerminationSeconds((int) timeout.getSeconds());
                    }
                }
            }
        };
    }
}

/**
 * Task Decorators for common use cases
 */
class ContextPropagatingTaskDecorator implements TaskDecorator {
    
    @Override
    public Runnable decorate(Runnable runnable) {
        // Capture contexts (MDC, SecurityContext, etc.)
        String requestId = captureRequestId();
        Object securityContext = captureSecurityContext();
        
        return () -> {
            try {
                // Set contexts for task execution
                setRequestId(requestId);
                setSecurityContext(securityContext);
                
                // Execute task
                runnable.run();
            } finally {
                // Clean up contexts
                clearRequestId();
                clearSecurityContext();
            }
        };
    }
    
    private String captureRequestId() { return "req-" + System.currentTimeMillis(); }
    private void setRequestId(String id) {}
    private void clearRequestId() {}
    
    private Object captureSecurityContext() { return null; }
    private void setSecurityContext(Object context) {}
    private void clearSecurityContext() {}
}

class MonitoringTaskDecorator implements TaskDecorator {
    
    @Override
    public Runnable decorate(Runnable runnable) {
        String taskName = runnable.getClass().getSimpleName();
        
        return () -> {
            long startTime = System.nanoTime();
            boolean success = false;
            
            try {
                System.out.println("[Monitor] Starting task: " + taskName);
                runnable.run();
                success = true;
            } catch (Exception e) {
                System.err.println("[Monitor] Task failed: " + taskName + " - " + e.getMessage());
                throw e;
            } finally {
                long duration = System.nanoTime() - startTime;
                System.out.println("[Monitor] Completed task: " + taskName + 
                                 " in " + (duration / 1_000_000) + "ms" +
                                 " (success: " + success + ")");
            }
        };
    }
}

/**
 * Usage Examples:
 * ==============
 * 
 * 1. Basic Customizer:
 *    @Bean
 *    public TaskSchedulerCustomizer customizer() {
 *        return scheduler -> {
 *            scheduler.setPoolSize(10);
 *            scheduler.setThreadNamePrefix("my-task-");
 *        };
 *    }
 * 
 * 2. Multiple Customizations:
 *    @Bean
 *    public TaskSchedulerCustomizer customizer() {
 *        return scheduler -> {
 *            scheduler.setPoolSize(10);
 *            scheduler.setWaitForTasksToCompleteOnShutdown(true);
 *            scheduler.setErrorHandler(e -> log.error("Error", e));
 *        };
 *    }
 * 
 * 3. Conditional Customizer:
 *    @Bean
 *    @Profile("production")
 *    public TaskSchedulerCustomizer prodCustomizer() {
 *        return scheduler -> {
 *            // Production-specific configuration
 *        };
 *    }
 * 
 * 4. Ordered Customizers:
 *    @Bean
 *    @Order(1)
 *    public TaskSchedulerCustomizer first() { ... }
 *    
 *    @Bean
 *    @Order(2)
 *    public TaskSchedulerCustomizer second() { ... }
 * 
 * Common Customizations:
 * =====================
 * 
 * Thread Pool:
 * - setPoolSize(int)
 * - setThreadNamePrefix(String)
 * - setThreadPriority(int)
 * - setDaemon(boolean)
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
 * Benefits:
 * ========
 * - Works with Spring Boot auto-configuration
 * - No need to manually define TaskScheduler bean
 * - Multiple customizers can be composed
 * - Customizers are reusable
 * - Clean separation of concerns
 * - Easy to test
 */
