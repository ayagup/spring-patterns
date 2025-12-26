package com.example.taskexecution;

import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Task Executor Customizer Pattern - Customize Task Executors
 * 
 * TaskExecutorCustomizer is a callback interface in Spring Boot that allows
 * customization of ThreadPoolTaskExecutor beans before they are fully initialized.
 * It provides a hook to apply common configuration across all task executors.
 * 
 * Key Concepts:
 * 
 * 1. Customizer Interface:
 *    - Callback for customizing executors
 *    - Applied automatically by Spring Boot
 *    - Can modify any executor property
 * 
 * 2. Auto-Configuration Integration:
 *    - Works with Spring Boot auto-configuration
 *    - Applied to auto-configured executors
 *    - Applied to custom executors
 * 
 * 3. Multiple Customizers:
 *    - Can define multiple customizers
 *    - Applied in order
 *    - Composition of customizations
 * 
 * Common Customizations:
 * 
 * - Thread Pool Sizing:
 *   * Core pool size
 *   * Max pool size
 *   * Queue capacity
 * 
 * - Thread Configuration:
 *   * Thread name prefix
 *   * Thread priority
 *   * Daemon status
 * 
 * - Rejection Handling:
 *   * Rejection policy
 *   * Custom rejection handler
 * 
 * - Lifecycle Management:
 *   * Graceful shutdown
 *   * Await termination
 *   * Task completion on shutdown
 * 
 * - Task Decoration:
 *   * Task decorators
 *   * Context propagation
 *   * MDC copying
 * 
 * TaskDecorator:
 * - Wraps tasks before execution
 * - Useful for context propagation
 * - MDC, SecurityContext, etc.
 * 
 * Use Cases:
 * - Common executor configuration
 * - Organization-wide standards
 * - Context propagation setup
 * - Monitoring integration
 * - Security context propagation
 * - Logging context propagation
 * - Performance monitoring
 * 
 * Advantages:
 * + Centralized configuration
 * + DRY principle
 * + Consistent executor setup
 * + Easy to maintain
 * + Applies to all executors
 * 
 * Best Practices:
 * - Use for common settings
 * - Keep customizers focused
 * - Document customizations
 * - Consider order of customizers
 * - Test customizer effects
 */
public class TaskExecutorCustomizerPattern {

    /**
     * Basic task executor customizer
     */
    @Configuration
    static class BasicCustomizerConfiguration {
        
        @Bean
        public TaskExecutorCustomizer basicTaskExecutorCustomizer() {
            return executor -> {
                // Common thread pool settings
                executor.setCorePoolSize(10);
                executor.setMaxPoolSize(20);
                executor.setQueueCapacity(100);
                executor.setKeepAliveSeconds(60);
                
                // Thread naming
                executor.setThreadNamePrefix("custom-async-");
                
                // Graceful shutdown
                executor.setWaitForTasksToCompleteOnShutdown(true);
                executor.setAwaitTerminationSeconds(60);
            };
        }
    }

    /**
     * Production-ready customizer with monitoring
     */
    @Configuration
    static class ProductionCustomizerConfiguration {
        
        @Bean
        public TaskExecutorCustomizer productionTaskExecutorCustomizer() {
            return executor -> {
                // Production sizing
                int processors = Runtime.getRuntime().availableProcessors();
                executor.setCorePoolSize(processors * 2);
                executor.setMaxPoolSize(processors * 4);
                executor.setQueueCapacity(500);
                
                // Thread configuration
                executor.setThreadNamePrefix("prod-async-");
                executor.setThreadPriority(Thread.NORM_PRIORITY);
                executor.setDaemon(false);
                
                // Rejection policy
                executor.setRejectedExecutionHandler(
                    new ThreadPoolExecutor.CallerRunsPolicy()
                );
                
                // Graceful shutdown
                executor.setWaitForTasksToCompleteOnShutdown(true);
                executor.setAwaitTerminationSeconds(120);
                
                // Task decorator for context propagation
                executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
            };
        }
    }

    /**
     * Multiple customizers for different concerns
     */
    @Configuration
    static class MultipleCustomizersConfiguration {
        
        /**
         * Customizer for thread pool sizing
         */
        @Bean
        public TaskExecutorCustomizer threadPoolSizingCustomizer() {
            return executor -> {
                executor.setCorePoolSize(5);
                executor.setMaxPoolSize(15);
                executor.setQueueCapacity(200);
                System.out.println("Applied: Thread pool sizing customizer");
            };
        }
        
        /**
         * Customizer for thread naming
         */
        @Bean
        public TaskExecutorCustomizer threadNamingCustomizer() {
            return executor -> {
                String appName = System.getProperty("app.name", "myapp");
                executor.setThreadNamePrefix(appName + "-async-");
                System.out.println("Applied: Thread naming customizer");
            };
        }
        
        /**
         * Customizer for graceful shutdown
         */
        @Bean
        public TaskExecutorCustomizer shutdownCustomizer() {
            return executor -> {
                executor.setWaitForTasksToCompleteOnShutdown(true);
                executor.setAwaitTerminationSeconds(90);
                System.out.println("Applied: Shutdown customizer");
            };
        }
        
        /**
         * Customizer for context propagation
         */
        @Bean
        public TaskExecutorCustomizer contextCustomizer() {
            return executor -> {
                executor.setTaskDecorator(new LoggingTaskDecorator());
                System.out.println("Applied: Context propagation customizer");
            };
        }
    }

    /**
     * Environment-specific customizer
     */
    @Configuration
    static class EnvironmentSpecificConfiguration {
        
        @Bean
        public TaskExecutorCustomizer environmentCustomizer() {
            String env = System.getProperty("env", "dev");
            
            return executor -> {
                if ("prod".equals(env)) {
                    // Production settings
                    executor.setCorePoolSize(20);
                    executor.setMaxPoolSize(40);
                    executor.setQueueCapacity(1000);
                    executor.setThreadNamePrefix("prod-");
                } else if ("staging".equals(env)) {
                    // Staging settings
                    executor.setCorePoolSize(10);
                    executor.setMaxPoolSize(20);
                    executor.setQueueCapacity(500);
                    executor.setThreadNamePrefix("staging-");
                } else {
                    // Development settings
                    executor.setCorePoolSize(2);
                    executor.setMaxPoolSize(5);
                    executor.setQueueCapacity(50);
                    executor.setThreadNamePrefix("dev-");
                }
            };
        }
    }

    /**
     * Task decorator for context propagation
     */
    static class ContextPropagatingTaskDecorator implements TaskDecorator {
        
        @Override
        public Runnable decorate(Runnable task) {
            // Capture context from current thread
            String contextValue = "captured-context"; // In real app: MDC.get(), SecurityContext, etc.
            
            return () -> {
                // Set context in executing thread
                System.out.println("Context propagated: " + contextValue);
                
                try {
                    task.run();
                } finally {
                    // Clean up context
                    System.out.println("Context cleaned up");
                }
            };
        }
    }

    /**
     * Logging task decorator
     */
    static class LoggingTaskDecorator implements TaskDecorator {
        
        private static int taskCounter = 0;
        
        @Override
        public Runnable decorate(Runnable task) {
            int taskId = ++taskCounter;
            
            return () -> {
                long startTime = System.currentTimeMillis();
                System.out.println("[Task " + taskId + "] Started on: " + 
                                 Thread.currentThread().getName());
                
                try {
                    task.run();
                } catch (Exception e) {
                    System.err.println("[Task " + taskId + "] Error: " + e.getMessage());
                    throw e;
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    System.out.println("[Task " + taskId + "] Completed in " + 
                                     duration + "ms");
                }
            };
        }
    }

    /**
     * Usage examples
     */
    static class TaskExecutorCustomizerExamples {
        
        public void demonstrateBasicCustomizer() {
            System.out.println("\n=== Basic Customizer ===");
            
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            
            // Apply customizer
            TaskExecutorCustomizer customizer = new BasicCustomizerConfiguration()
                .basicTaskExecutorCustomizer();
            customizer.customize(executor);
            
            executor.initialize();
            
            System.out.println("Executor configured:");
            System.out.println("- Core pool size: " + executor.getCorePoolSize());
            System.out.println("- Max pool size: " + executor.getMaxPoolSize());
            System.out.println("- Queue capacity: " + executor.getQueueCapacity());
            System.out.println("- Thread prefix: " + executor.getThreadNamePrefix());
            
            executor.shutdown();
        }
        
        public void demonstrateMultipleCustomizers() {
            System.out.println("\n=== Multiple Customizers ===");
            
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            
            // Apply multiple customizers
            MultipleCustomizersConfiguration config = new MultipleCustomizersConfiguration();
            
            config.threadPoolSizingCustomizer().customize(executor);
            config.threadNamingCustomizer().customize(executor);
            config.shutdownCustomizer().customize(executor);
            config.contextCustomizer().customize(executor);
            
            executor.initialize();
            
            System.out.println("\nFinal configuration:");
            System.out.println("- Core pool size: " + executor.getCorePoolSize());
            System.out.println("- Thread prefix: " + executor.getThreadNamePrefix());
            
            executor.shutdown();
        }
        
        public void demonstrateTaskDecorator() throws InterruptedException {
            System.out.println("\n=== Task Decorator ===");
            
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(2);
            executor.setMaxPoolSize(2);
            executor.setThreadNamePrefix("decorated-");
            executor.setTaskDecorator(new LoggingTaskDecorator());
            executor.initialize();
            
            // Submit tasks
            for (int i = 1; i <= 3; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.println("  Task " + taskId + " executing");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            Thread.sleep(2000);
            executor.shutdown();
        }
        
        public void demonstrateEnvironmentCustomization() {
            System.out.println("\n=== Environment-Specific Customization ===");
            
            // Test different environments
            for (String env : new String[]{"dev", "staging", "prod"}) {
                System.setProperty("env", env);
                
                ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                
                TaskExecutorCustomizer customizer = new EnvironmentSpecificConfiguration()
                    .environmentCustomizer();
                customizer.customize(executor);
                
                executor.initialize();
                
                System.out.println("\nEnvironment: " + env);
                System.out.println("- Core pool size: " + executor.getCorePoolSize());
                System.out.println("- Max pool size: " + executor.getMaxPoolSize());
                System.out.println("- Thread prefix: " + executor.getThreadNamePrefix());
                
                executor.shutdown();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Task Executor Customizer Pattern - Executor Customization");
        System.out.println("===========================================================");
        
        TaskExecutorCustomizerExamples examples = new TaskExecutorCustomizerExamples();
        
        examples.demonstrateBasicCustomizer();
        examples.demonstrateMultipleCustomizers();
        examples.demonstrateTaskDecorator();
        examples.demonstrateEnvironmentCustomization();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TaskExecutorCustomizer:");
        System.out.println("- Callback for customizing executors");
        System.out.println("- Applied automatically by Spring Boot");
        System.out.println("- Can have multiple customizers");
        
        System.out.println("\nCommon Customizations:");
        System.out.println("- Thread pool sizing");
        System.out.println("- Thread naming");
        System.out.println("- Rejection policies");
        System.out.println("- Graceful shutdown");
        System.out.println("- Task decoration");
        
        System.out.println("\nTaskDecorator Uses:");
        System.out.println("- Context propagation");
        System.out.println("- MDC/logging context");
        System.out.println("- Security context");
        System.out.println("- Performance monitoring");
        System.out.println("- Error handling");
        
        System.out.println("\nBenefits:");
        System.out.println("✓ Centralized configuration");
        System.out.println("✓ DRY principle");
        System.out.println("✓ Consistent setup");
        System.out.println("✓ Easy maintenance");
    }
}
