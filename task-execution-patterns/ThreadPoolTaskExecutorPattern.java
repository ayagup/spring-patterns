package com.example.taskexecution;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskRejectedException;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread Pool Task Executor Pattern - Configurable Thread Pool for Async Tasks
 * 
 * ThreadPoolTaskExecutor is Spring's implementation of TaskExecutor that provides
 * a configurable thread pool for executing asynchronous tasks. It wraps Java's
 * ThreadPoolExecutor with Spring-specific features and lifecycle management.
 * 
 * Key Configuration Properties:
 * 
 * 1. Core Pool Size:
 *    - Minimum number of threads to keep alive
 *    - Threads created up to this limit immediately
 *    - Default: 1
 * 
 * 2. Max Pool Size:
 *    - Maximum number of threads allowed
 *    - Additional threads created when queue is full
 *    - Default: Integer.MAX_VALUE
 * 
 * 3. Queue Capacity:
 *    - Size of task queue (before creating new threads)
 *    - Tasks queue up before max pool size is reached
 *    - Default: Integer.MAX_VALUE (unbounded)
 * 
 * 4. Keep Alive Seconds:
 *    - Time idle threads wait before termination
 *    - Applies to threads beyond core pool size
 *    - Default: 60 seconds
 * 
 * 5. Thread Name Prefix:
 *    - Prefix for thread names
 *    - Helps identify threads in logs
 * 
 * 6. Rejection Policy:
 *    - AbortPolicy: Throws RejectedExecutionException (default)
 *    - CallerRunsPolicy: Runs task in caller's thread
 *    - DiscardPolicy: Silently discards task
 *    - DiscardOldestPolicy: Discards oldest queued task
 * 
 * Thread Creation Sequence:
 * 1. If threads < corePoolSize: Create new thread
 * 2. If threads >= corePoolSize: Queue task
 * 3. If queue full && threads < maxPoolSize: Create new thread
 * 4. If queue full && threads >= maxPoolSize: Apply rejection policy
 * 
 * Lifecycle Methods:
 * - initialize(): Initialize thread pool
 * - shutdown(): Graceful shutdown (wait for tasks)
 * - destroy(): Force shutdown
 * - setAwaitTerminationSeconds(): Wait time for shutdown
 * - setWaitForTasksToCompleteOnShutdown(): Complete tasks on shutdown
 * 
 * Monitoring:
 * - getActiveCount(): Number of active threads
 * - getPoolSize(): Current pool size
 * - getThreadPoolExecutor(): Access underlying executor
 * 
 * Use Cases:
 * - @Async method execution
 * - Background task processing
 * - Event handling
 * - Batch processing
 * - I/O-bound operations
 * - Scheduled tasks
 * 
 * Best Practices:
 * - Set appropriate core/max pool sizes
 * - Configure bounded queue capacity
 * - Use meaningful thread names
 * - Monitor pool metrics
 * - Configure graceful shutdown
 * - Choose appropriate rejection policy
 * - Tune for workload characteristics
 */
public class ThreadPoolTaskExecutorPattern {

    /**
     * Basic ThreadPoolTaskExecutor configuration
     */
    @Configuration
    static class BasicExecutorConfiguration {
        
        @Bean(name = "taskExecutor")
        public ThreadPoolTaskExecutor taskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            
            // Core configuration
            executor.setCorePoolSize(5);
            executor.setMaxPoolSize(10);
            executor.setQueueCapacity(25);
            
            // Thread naming
            executor.setThreadNamePrefix("async-task-");
            
            // Graceful shutdown
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            
            executor.initialize();
            return executor;
        }
    }

    /**
     * Advanced configuration with custom rejection policy
     */
    @Configuration
    static class AdvancedExecutorConfiguration {
        
        @Bean(name = "customExecutor")
        public ThreadPoolTaskExecutor customExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            
            executor.setCorePoolSize(10);
            executor.setMaxPoolSize(20);
            executor.setQueueCapacity(100);
            executor.setKeepAliveSeconds(120);
            executor.setThreadNamePrefix("custom-async-");
            
            // Custom thread factory
            executor.setThreadFactory(new CustomizableThreadFactory("custom-thread-"));
            
            // Rejection policy: caller runs
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            
            // Allow core threads to timeout
            executor.setAllowCoreThreadTimeOut(true);
            
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(120);
            
            executor.initialize();
            return executor;
        }
    }

    /**
     * Multiple executor configuration for different workloads
     */
    @Configuration
    static class MultipleExecutorConfiguration {
        
        /**
         * Executor for CPU-intensive tasks
         */
        @Bean(name = "cpuExecutor")
        public ThreadPoolTaskExecutor cpuBoundExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            
            // CPU-bound: threads = number of processors
            int processors = Runtime.getRuntime().availableProcessors();
            executor.setCorePoolSize(processors);
            executor.setMaxPoolSize(processors * 2);
            executor.setQueueCapacity(50);
            executor.setThreadNamePrefix("cpu-task-");
            
            executor.initialize();
            return executor;
        }
        
        /**
         * Executor for I/O-intensive tasks
         */
        @Bean(name = "ioExecutor")
        public ThreadPoolTaskExecutor ioBoundExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            
            // I/O-bound: more threads than processors
            executor.setCorePoolSize(20);
            executor.setMaxPoolSize(50);
            executor.setQueueCapacity(200);
            executor.setThreadNamePrefix("io-task-");
            executor.setKeepAliveSeconds(60);
            
            executor.initialize();
            return executor;
        }
        
        /**
         * Executor for low-priority background tasks
         */
        @Bean(name = "backgroundExecutor")
        public ThreadPoolTaskExecutor backgroundExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            
            executor.setCorePoolSize(2);
            executor.setMaxPoolSize(5);
            executor.setQueueCapacity(1000);
            executor.setThreadNamePrefix("background-");
            executor.setKeepAliveSeconds(300);
            
            // Discard oldest policy for background tasks
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
            
            executor.initialize();
            return executor;
        }
    }

    /**
     * Custom rejection handler
     */
    static class LoggingRejectedExecutionHandler implements RejectedExecutionHandler {
        
        private AtomicInteger rejectedCount = new AtomicInteger(0);
        
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            int count = rejectedCount.incrementAndGet();
            System.err.println("Task rejected! Count: " + count);
            System.err.println("Pool size: " + executor.getPoolSize());
            System.err.println("Active threads: " + executor.getActiveCount());
            System.err.println("Queue size: " + executor.getQueue().size());
            
            // Optionally throw exception
            throw new TaskRejectedException("Task rejected: " + r, null);
        }
        
        public int getRejectedCount() {
            return rejectedCount.get();
        }
    }

    /**
     * Task monitoring and statistics
     */
    static class ExecutorMonitor {
        
        private ThreadPoolTaskExecutor executor;
        
        public ExecutorMonitor(ThreadPoolTaskExecutor executor) {
            this.executor = executor;
        }
        
        public void printStatistics() {
            ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
            
            System.out.println("\n=== Thread Pool Statistics ===");
            System.out.println("Core Pool Size: " + threadPoolExecutor.getCorePoolSize());
            System.out.println("Max Pool Size: " + threadPoolExecutor.getMaximumPoolSize());
            System.out.println("Current Pool Size: " + threadPoolExecutor.getPoolSize());
            System.out.println("Active Threads: " + threadPoolExecutor.getActiveCount());
            System.out.println("Largest Pool Size: " + threadPoolExecutor.getLargestPoolSize());
            System.out.println("Task Count: " + threadPoolExecutor.getTaskCount());
            System.out.println("Completed Tasks: " + threadPoolExecutor.getCompletedTaskCount());
            System.out.println("Queue Size: " + threadPoolExecutor.getQueue().size());
            System.out.println("Queue Remaining Capacity: " + 
                             threadPoolExecutor.getQueue().remainingCapacity());
        }
        
        public boolean isHealthy() {
            ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
            int queueSize = threadPoolExecutor.getQueue().size();
            int queueCapacity = threadPoolExecutor.getQueue().size() + 
                               threadPoolExecutor.getQueue().remainingCapacity();
            
            // Healthy if queue is less than 80% full
            return queueSize < (queueCapacity * 0.8);
        }
    }

    /**
     * Usage examples
     */
    static class ThreadPoolTaskExecutorExamples {
        
        public void demonstrateBasicUsage() {
            System.out.println("\n=== Basic ThreadPoolTaskExecutor ===");
            
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(3);
            executor.setMaxPoolSize(5);
            executor.setQueueCapacity(10);
            executor.setThreadNamePrefix("demo-");
            executor.initialize();
            
            // Submit tasks
            for (int i = 1; i <= 5; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.println("Task " + taskId + " executing on: " + 
                                     Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            // Monitor
            ExecutorMonitor monitor = new ExecutorMonitor(executor);
            monitor.printStatistics();
            
            executor.shutdown();
        }
        
        public void demonstrateRejectionPolicies() {
            System.out.println("\n=== Rejection Policies ===");
            
            // Abort Policy (default)
            System.out.println("\n1. Abort Policy:");
            ThreadPoolTaskExecutor abortExecutor = new ThreadPoolTaskExecutor();
            abortExecutor.setCorePoolSize(1);
            abortExecutor.setMaxPoolSize(1);
            abortExecutor.setQueueCapacity(2);
            abortExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
            abortExecutor.initialize();
            
            try {
                for (int i = 0; i < 5; i++) {
                    abortExecutor.submit(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                    });
                }
            } catch (TaskRejectedException e) {
                System.out.println("Task rejected with AbortPolicy");
            }
            abortExecutor.shutdown();
            
            // Caller Runs Policy
            System.out.println("\n2. Caller Runs Policy:");
            ThreadPoolTaskExecutor callerRunsExecutor = new ThreadPoolTaskExecutor();
            callerRunsExecutor.setCorePoolSize(1);
            callerRunsExecutor.setMaxPoolSize(1);
            callerRunsExecutor.setQueueCapacity(2);
            callerRunsExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            callerRunsExecutor.initialize();
            
            for (int i = 0; i < 5; i++) {
                final int taskId = i;
                callerRunsExecutor.submit(() -> {
                    System.out.println("Task " + taskId + " on: " + Thread.currentThread().getName());
                });
            }
            callerRunsExecutor.shutdown();
        }
        
        public void demonstrateGracefulShutdown() throws InterruptedException {
            System.out.println("\n=== Graceful Shutdown ===");
            
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(2);
            executor.setMaxPoolSize(2);
            executor.setQueueCapacity(10);
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(30);
            executor.initialize();
            
            // Submit long-running tasks
            for (int i = 1; i <= 5; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.println("Task " + taskId + " started");
                    try {
                        Thread.sleep(2000);
                        System.out.println("Task " + taskId + " completed");
                    } catch (InterruptedException e) {
                        System.out.println("Task " + taskId + " interrupted");
                    }
                });
            }
            
            System.out.println("Initiating shutdown...");
            executor.shutdown();
            System.out.println("Shutdown completed");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Thread Pool Task Executor Pattern - Configurable Thread Pool");
        System.out.println("=============================================================");
        
        ThreadPoolTaskExecutorExamples examples = new ThreadPoolTaskExecutorExamples();
        
        examples.demonstrateBasicUsage();
        Thread.sleep(2000);
        
        examples.demonstrateRejectionPolicies();
        Thread.sleep(2000);
        
        examples.demonstrateGracefulShutdown();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Configuration Properties:");
        System.out.println("- corePoolSize: Minimum threads (default: 1)");
        System.out.println("- maxPoolSize: Maximum threads (default: Integer.MAX_VALUE)");
        System.out.println("- queueCapacity: Task queue size (default: Integer.MAX_VALUE)");
        System.out.println("- keepAliveSeconds: Idle timeout (default: 60)");
        
        System.out.println("\nRejection Policies:");
        System.out.println("- AbortPolicy: Throw exception (default)");
        System.out.println("- CallerRunsPolicy: Run in caller's thread");
        System.out.println("- DiscardPolicy: Silently discard");
        System.out.println("- DiscardOldestPolicy: Discard oldest queued");
        
        System.out.println("\nThread Creation Sequence:");
        System.out.println("1. threads < corePoolSize → create thread");
        System.out.println("2. threads >= corePoolSize → queue task");
        System.out.println("3. queue full & threads < maxPoolSize → create thread");
        System.out.println("4. queue full & threads >= maxPoolSize → reject");
    }
}
