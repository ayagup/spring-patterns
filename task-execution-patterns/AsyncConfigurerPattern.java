package com.example.taskexecution;

import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Async Configurer Pattern - Configure @Async Behavior
 * 
 * AsyncConfigurer is an interface that allows customization of the async
 * execution infrastructure, including the executor and exception handler
 * used for @Async annotated methods.
 * 
 * Key Methods:
 * 
 * 1. getAsyncExecutor():
 *    - Returns the Executor for @Async methods
 *    - Can return custom executor
 *    - Default: SimpleAsyncTaskExecutor
 * 
 * 2. getAsyncUncaughtExceptionHandler():
 *    - Returns exception handler for async methods
 *    - Handles exceptions from void async methods
 *    - Default: SimpleAsyncUncaughtExceptionHandler
 * 
 * Configuration:
 * - Implement AsyncConfigurer interface
 * - Override getAsyncExecutor()
 * - Override getAsyncUncaughtExceptionHandler()
 * - Use with @EnableAsync
 * 
 * Use Cases:
 * - Custom executor for @Async
 * - Centralized exception handling
 * - Thread pool configuration
 * - Context propagation
 * - Monitoring async methods
 * 
 * Best Practices:
 * - Configure proper thread pool
 * - Handle async exceptions
 * - Set meaningful thread names
 * - Configure graceful shutdown
 * - Monitor executor metrics
 */
public class AsyncConfigurerPattern {

    /**
     * Basic AsyncConfigurer implementation
     */
    @Configuration
    @EnableAsync
    static class BasicAsyncConfiguration implements AsyncConfigurer {
        
        @Override
        public Executor getAsyncExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(5);
            executor.setMaxPoolSize(10);
            executor.setQueueCapacity(100);
            executor.setThreadNamePrefix("async-");
            executor.initialize();
            return executor;
        }
        
        @Override
        public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
            return new SimpleAsyncExceptionHandler();
        }
    }

    /**
     * Production AsyncConfigurer
     */
    @Configuration
    @EnableAsync
    static class ProductionAsyncConfiguration implements AsyncConfigurer {
        
        @Override
        public Executor getAsyncExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            
            // Production sizing
            int processors = Runtime.getRuntime().availableProcessors();
            executor.setCorePoolSize(processors * 2);
            executor.setMaxPoolSize(processors * 4);
            executor.setQueueCapacity(500);
            executor.setKeepAliveSeconds(60);
            
            // Thread configuration
            executor.setThreadNamePrefix("prod-async-");
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            
            executor.initialize();
            return executor;
        }
        
        @Override
        public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
            return new LoggingAsyncExceptionHandler();
        }
    }

    /**
     * Simple exception handler
     */
    static class SimpleAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            System.err.println("Async exception in method: " + method.getName());
            System.err.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Logging exception handler
     */
    static class LoggingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            System.err.println("\n=== Async Method Exception ===");
            System.err.println("Method: " + method.getDeclaringClass().getName() + 
                             "." + method.getName());
            System.err.println("Parameters: " + params.length);
            for (int i = 0; i < params.length; i++) {
                System.err.println("  Param[" + i + "]: " + params[i]);
            }
            System.err.println("Exception Type: " + ex.getClass().getName());
            System.err.println("Exception Message: " + ex.getMessage());
            System.err.println("Stack Trace:");
            ex.printStackTrace();
            System.err.println("============================\n");
        }
    }

    /**
     * Usage examples
     */
    static class AsyncConfigurerExamples {
        
        public void demonstrateBasicConfiguration() {
            System.out.println("\n=== Basic AsyncConfigurer ===");
            
            BasicAsyncConfiguration config = new BasicAsyncConfiguration();
            
            Executor executor = config.getAsyncExecutor();
            System.out.println("Async executor configured:");
            System.out.println("- Type: " + executor.getClass().getSimpleName());
            
            AsyncUncaughtExceptionHandler handler = config.getAsyncUncaughtExceptionHandler();
            System.out.println("Exception handler: " + handler.getClass().getSimpleName());
        }
        
        public void demonstrateExceptionHandling() throws NoSuchMethodException {
            System.out.println("\n=== Exception Handling ===");
            
            AsyncUncaughtExceptionHandler handler = new LoggingAsyncExceptionHandler();
            
            // Simulate async exception
            Method method = AsyncConfigurerPattern.class.getMethod("main", String[].class);
            Exception exception = new RuntimeException("Simulated async error");
            
            handler.handleUncaughtException(exception, method, "param1", "param2");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Async Configurer Pattern - Configure @Async Behavior");
        System.out.println("======================================================");
        
        AsyncConfigurerExamples examples = new AsyncConfigurerExamples();
        examples.demonstrateBasicConfiguration();
        examples.demonstrateExceptionHandling();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("AsyncConfigurer Methods:");
        System.out.println("- getAsyncExecutor(): Returns executor for @Async");
        System.out.println("- getAsyncUncaughtExceptionHandler(): Exception handling");
        
        System.out.println("\nConfiguration:");
        System.out.println("1. Implement AsyncConfigurer");
        System.out.println("2. Annotate with @Configuration @EnableAsync");
        System.out.println("3. Override getAsyncExecutor()");
        System.out.println("4. Override getAsyncUncaughtExceptionHandler()");
        
        System.out.println("\nUse Cases:");
        System.out.println("- Custom executor for @Async methods");
        System.out.println("- Centralized exception handling");
        System.out.println("- Thread pool customization");
        System.out.println("- Monitoring async execution");
    }
}
