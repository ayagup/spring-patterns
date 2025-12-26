package com.example.taskexecution;

import org.springframework.scheduling.concurrent.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple Async Task Executor Pattern - Non-Thread-Pooling Async Executor
 * 
 * SimpleAsyncTaskExecutor creates a new thread for each task execution without
 * reusing threads. It's the simplest TaskExecutor implementation but not
 * recommended for production due to resource overhead.
 * 
 * Key Characteristics:
 * 
 * 1. Thread Creation:
 *    - Creates new thread for EVERY task
 *    - No thread reuse or pooling
 *    - Thread dies after task completion
 * 
 * 2. Asynchronous Execution:
 *    - Tasks execute asynchronously
 *    - Fire-and-forget semantics
 *    - Non-blocking submission
 * 
 * 3. Concurrency Limit:
 *    - Optional concurrency limit
 *    - Throttles concurrent executions
 *    - Blocks when limit reached
 * 
 * Configuration Options:
 * 
 * - concurrencyLimit:
 *   * -1 (default): No limit, unlimited threads
 *   * > 0: Maximum concurrent threads
 *   * Blocks submission when limit reached
 * 
 * - threadNamePrefix:
 *   * Prefix for thread names
 *   * Helps identify threads in logs
 * 
 * - threadFactory:
 *   * Custom thread creation logic
 *   * Set daemon status, priority, etc.
 * 
 * - threadPriority:
 *   * Thread priority (1-10)
 *   * Default: Thread.NORM_PRIORITY (5)
 * 
 * - daemon:
 *   * Whether threads are daemon threads
 *   * Default: false (non-daemon)
 * 
 * Thread Lifecycle:
 * 1. Task submitted
 * 2. New thread created
 * 3. Task executes
 * 4. Thread terminates
 * 
 * Advantages:
 * + Simple implementation
 * + No queue management
 * + Each task has dedicated thread
 * + No thread pool overhead
 * + Isolation between tasks
 * 
 * Disadvantages:
 * - High resource overhead
 * - Thread creation cost
 * - No thread reuse
 * - Not scalable
 * - Can exhaust system resources
 * 
 * Comparison with ThreadPoolTaskExecutor:
 * 
 * SimpleAsyncTaskExecutor:
 * - Creates new thread per task
 * - No thread reuse
 * - Simple but inefficient
 * - Good for development/testing
 * 
 * ThreadPoolTaskExecutor:
 * - Reuses threads from pool
 * - Efficient resource usage
 * - Production-ready
 * - Better performance
 * 
 * Use Cases:
 * - Development and testing
 * - Prototyping
 * - Very low task volume
 * - Task isolation requirements
 * - Non-production environments
 * - Quick async implementation
 * 
 * Production Alternative:
 * For production use, prefer ThreadPoolTaskExecutor
 * or ConcurrentTaskExecutor with proper thread pool.
 * 
 * Best Practices:
 * - Avoid in production
 * - Use for testing only
 * - Set concurrency limits
 * - Monitor thread creation
 * - Consider alternatives for scale
 */
public class SimpleAsyncTaskExecutorPattern {

    /**
     * Basic SimpleAsyncTaskExecutor configuration
     */
    @Configuration
    static class BasicConfiguration {
        
        @Bean(name = "simpleAsyncExecutor")
        public TaskExecutor simpleAsyncTaskExecutor() {
            SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
            
            // Thread naming
            executor.setThreadNamePrefix("simple-async-");
            
            return executor;
        }
    }

    /**
     * Configuration with concurrency limit
     */
    @Configuration
    static class LimitedConcurrencyConfiguration {
        
        @Bean(name = "limitedAsyncExecutor")
        public TaskExecutor limitedAsyncExecutor() {
            SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
            
            // Set concurrency limit
            executor.setConcurrencyLimit(5); // Max 5 concurrent threads
            executor.setThreadNamePrefix("limited-async-");
            
            return executor;
        }
    }

    /**
     * Custom configuration with thread factory
     */
    @Configuration
    static class CustomConfiguration {
        
        @Bean(name = "customSimpleExecutor")
        public TaskExecutor customSimpleExecutor() {
            SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
            
            executor.setThreadNamePrefix("custom-simple-");
            executor.setThreadPriority(Thread.MAX_PRIORITY);
            executor.setDaemon(true); // Daemon threads
            
            // Custom thread factory
            executor.setThreadFactory(new CustomThreadFactory("custom-"));
            
            return executor;
        }
    }

    /**
     * Custom thread factory
     */
    static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        public CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(namePrefix + "thread-" + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            
            // Add uncaught exception handler
            thread.setUncaughtExceptionHandler((t, e) -> {
                System.err.println("Uncaught exception in thread " + t.getName());
                e.printStackTrace();
            });
            
            return thread;
        }
    }

    /**
     * Monitoring and statistics
     */
    static class ExecutorMonitor {
        
        private SimpleAsyncTaskExecutor executor;
        private AtomicInteger taskCount = new AtomicInteger(0);
        
        public ExecutorMonitor(SimpleAsyncTaskExecutor executor) {
            this.executor = executor;
        }
        
        public void submitTask(Runnable task) {
            int count = taskCount.incrementAndGet();
            System.out.println("Submitting task #" + count);
            
            executor.execute(() -> {
                System.out.println("Task #" + count + " executing on: " + 
                                 Thread.currentThread().getName());
                task.run();
            });
        }
        
        public void printInfo() {
            System.out.println("\n=== Executor Info ===");
            System.out.println("Concurrency Limit: " + executor.getConcurrencyLimit());
            System.out.println("Thread Name Prefix: " + executor.getThreadNamePrefix());
            System.out.println("Thread Priority: " + executor.getThreadPriority());
            System.out.println("Is Daemon: " + executor.isDaemon());
            System.out.println("Total Tasks Submitted: " + taskCount.get());
        }
    }

    /**
     * Usage examples
     */
    static class SimpleAsyncTaskExecutorExamples {
        
        public void demonstrateBasicUsage() throws InterruptedException {
            System.out.println("\n=== Basic SimpleAsyncTaskExecutor ===");
            
            SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
            executor.setThreadNamePrefix("demo-");
            
            // Submit tasks - each gets its own thread
            for (int i = 1; i <= 5; i++) {
                final int taskId = i;
                executor.execute(() -> {
                    System.out.println("Task " + taskId + " on: " + 
                                     Thread.currentThread().getName() + 
                                     " (ID: " + Thread.currentThread().getId() + ")");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            Thread.sleep(1000);
            System.out.println("Notice: Each task runs on a different thread!");
        }
        
        public void demonstrateConcurrencyLimit() throws InterruptedException {
            System.out.println("\n=== Concurrency Limit ===");
            
            SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
            executor.setThreadNamePrefix("limited-");
            executor.setConcurrencyLimit(3); // Only 3 concurrent threads
            
            System.out.println("Submitting 6 tasks with limit of 3 concurrent threads:");
            
            // Submit more tasks than concurrency limit
            for (int i = 1; i <= 6; i++) {
                final int taskId = i;
                System.out.println("Submitting task " + taskId + "...");
                
                executor.execute(() -> {
                    System.out.println("Task " + taskId + " started on: " + 
                                     Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("Task " + taskId + " completed");
                });
            }
            
            Thread.sleep(3000);
            System.out.println("Tasks completed with concurrency limiting");
        }
        
        public void demonstrateThreadCreation() throws InterruptedException {
            System.out.println("\n=== Thread Creation Overhead ===");
            
            SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
            executor.setThreadNamePrefix("overhead-");
            
            long startTime = System.currentTimeMillis();
            
            // Create many threads - demonstrates overhead
            for (int i = 1; i <= 10; i++) {
                final int taskId = i;
                executor.execute(() -> {
                    System.out.println("Task " + taskId + " on NEW thread: " + 
                                     Thread.currentThread().getId());
                    // Quick task
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            Thread.sleep(1500);
            long endTime = System.currentTimeMillis();
            
            System.out.println("Time taken: " + (endTime - startTime) + "ms");
            System.out.println("Note: Thread creation adds overhead!");
        }
        
        public void demonstrateDaemonThreads() throws InterruptedException {
            System.out.println("\n=== Daemon Threads ===");
            
            SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
            executor.setThreadNamePrefix("daemon-");
            executor.setDaemon(true);
            
            executor.execute(() -> {
                System.out.println("Daemon thread: " + Thread.currentThread().getName());
                System.out.println("Is daemon: " + Thread.currentThread().isDaemon());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            Thread.sleep(200);
            System.out.println("Daemon threads won't prevent JVM shutdown");
        }
        
        public void demonstrateMonitoring() throws InterruptedException {
            System.out.println("\n=== Monitoring ===");
            
            SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
            executor.setThreadNamePrefix("monitored-");
            executor.setConcurrencyLimit(3);
            
            ExecutorMonitor monitor = new ExecutorMonitor(executor);
            
            // Submit tasks through monitor
            for (int i = 1; i <= 5; i++) {
                monitor.submitTask(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            Thread.sleep(1000);
            monitor.printInfo();
        }
        
        public void compareWithThreadPool() throws InterruptedException {
            System.out.println("\n=== Comparison: SimpleAsync vs ThreadPool ===");
            
            // SimpleAsyncTaskExecutor
            System.out.println("\n1. SimpleAsyncTaskExecutor:");
            SimpleAsyncTaskExecutor simpleExecutor = new SimpleAsyncTaskExecutor();
            simpleExecutor.setThreadNamePrefix("simple-");
            
            for (int i = 1; i <= 3; i++) {
                simpleExecutor.execute(() -> {
                    System.out.println("  Simple: " + Thread.currentThread().getName() + 
                                     " (ID: " + Thread.currentThread().getId() + ")");
                });
            }
            
            Thread.sleep(500);
            
            // Thread pool would reuse threads
            System.out.println("\n2. With ThreadPool (simulated):");
            System.out.println("  - Same threads would be reused");
            System.out.println("  - Lower overhead");
            System.out.println("  - Better for production");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Simple Async Task Executor Pattern - Non-Pooling Executor");
        System.out.println("===========================================================");
        
        SimpleAsyncTaskExecutorExamples examples = new SimpleAsyncTaskExecutorExamples();
        
        examples.demonstrateBasicUsage();
        examples.demonstrateConcurrencyLimit();
        examples.demonstrateThreadCreation();
        examples.demonstrateDaemonThreads();
        examples.demonstrateMonitoring();
        examples.compareWithThreadPool();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Characteristics:");
        System.out.println("- Creates NEW thread for EVERY task");
        System.out.println("- No thread reuse or pooling");
        System.out.println("- Optional concurrency limit");
        System.out.println("- Simple but inefficient");
        
        System.out.println("\nConfiguration:");
        System.out.println("- concurrencyLimit: Max concurrent threads (-1 = unlimited)");
        System.out.println("- threadNamePrefix: Thread naming");
        System.out.println("- threadPriority: Thread priority (1-10)");
        System.out.println("- daemon: Daemon thread status");
        
        System.out.println("\nWhen to Use:");
        System.out.println("✓ Development and testing");
        System.out.println("✓ Prototyping");
        System.out.println("✓ Very low task volume");
        System.out.println("✗ Production environments");
        System.out.println("✗ High task volume");
        
        System.out.println("\nProduction Alternative:");
        System.out.println("Use ThreadPoolTaskExecutor instead!");
    }
}
