package com.example.taskexecution;

import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrent Task Executor Pattern - Adapter for Java's Executor Framework
 * 
 * ConcurrentTaskExecutor is a Spring adapter that wraps standard Java
 * java.util.concurrent.Executor instances, allowing them to be used as
 * Spring TaskExecutor implementations.
 * 
 * Key Features:
 * 
 * 1. Adapter Pattern:
 *    - Wraps any java.util.concurrent.Executor
 *    - Implements Spring's TaskExecutor interface
 *    - Enables Spring integration with Java executors
 * 
 * 2. Flexible Configuration:
 *    - Can wrap any Executor implementation
 *    - ThreadPoolExecutor
 *    - ScheduledThreadPoolExecutor
 *    - ForkJoinPool
 *    - Custom Executor implementations
 * 
 * 3. Lightweight:
 *    - No additional thread management
 *    - Delegates to underlying executor
 *    - Minimal overhead
 * 
 * Common Wrapped Executors:
 * 
 * - Executors.newCachedThreadPool()
 *   * Creates threads as needed
 *   * Reuses idle threads
 *   * Unbounded thread creation
 * 
 * - Executors.newFixedThreadPool(n)
 *   * Fixed number of threads
 *   * Unbounded queue
 *   * Good for stable load
 * 
 * - Executors.newSingleThreadExecutor()
 *   * Single worker thread
 *   * Sequential task execution
 *   * Guaranteed ordering
 * 
 * - Executors.newWorkStealingPool()
 *   * Uses ForkJoinPool
 *   * Work stealing algorithm
 *   * Good for recursive tasks
 * 
 * Comparison with ThreadPoolTaskExecutor:
 * 
 * ConcurrentTaskExecutor:
 * + Wraps existing Java executors
 * + Lighter weight
 * + More flexible executor choices
 * - Less Spring-specific features
 * - No built-in lifecycle management
 * 
 * ThreadPoolTaskExecutor:
 * + Spring-native lifecycle
 * + More configuration options
 * + Better integration with Spring
 * - More heavyweight
 * - Limited to ThreadPoolExecutor
 * 
 * Use Cases:
 * - Integration with existing executor infrastructure
 * - Simple async task execution
 * - Bridging Java and Spring concurrency
 * - Custom executor requirements
 * - Lightweight task execution
 * 
 * Best Practices:
 * - Choose appropriate executor for workload
 * - Consider executor characteristics
 * - Manage executor lifecycle
 * - Monitor executor metrics
 * - Use bounded queues when possible
 * - Handle rejected tasks
 */
public class ConcurrentTaskExecutorPattern {

    /**
     * Basic ConcurrentTaskExecutor configurations
     */
    @Configuration
    static class BasicConfiguration {
        
        /**
         * Cached thread pool executor
         * Creates threads as needed, reuses idle threads
         */
        @Bean(name = "cachedExecutor")
        public TaskExecutor cachedThreadPoolExecutor() {
            Executor executor = Executors.newCachedThreadPool();
            return new ConcurrentTaskExecutor(executor);
        }
        
        /**
         * Fixed thread pool executor
         * Fixed number of threads with unbounded queue
         */
        @Bean(name = "fixedExecutor")
        public TaskExecutor fixedThreadPoolExecutor() {
            Executor executor = Executors.newFixedThreadPool(10);
            return new ConcurrentTaskExecutor(executor);
        }
        
        /**
         * Single thread executor
         * Sequential task execution
         */
        @Bean(name = "singleExecutor")
        public TaskExecutor singleThreadExecutor() {
            Executor executor = Executors.newSingleThreadExecutor();
            return new ConcurrentTaskExecutor(executor);
        }
        
        /**
         * Work stealing pool
         * Uses ForkJoinPool for parallel processing
         */
        @Bean(name = "workStealingExecutor")
        public TaskExecutor workStealingPoolExecutor() {
            Executor executor = Executors.newWorkStealingPool();
            return new ConcurrentTaskExecutor(executor);
        }
    }

    /**
     * Custom ThreadPoolExecutor wrapped by ConcurrentTaskExecutor
     */
    @Configuration
    static class CustomExecutorConfiguration {
        
        @Bean(name = "customConcurrentExecutor")
        public TaskExecutor customConcurrentExecutor() {
            // Create custom ThreadPoolExecutor
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                5,  // core pool size
                10, // max pool size
                60L, // keep alive time
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), // bounded queue
                new CustomThreadFactory("custom-"),
                new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
            );
            
            // Wrap with ConcurrentTaskExecutor
            return new ConcurrentTaskExecutor(threadPoolExecutor);
        }
        
        @Bean(name = "boundedExecutor")
        public TaskExecutor boundedExecutor() {
            // Bounded executor with ArrayBlockingQueue
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                3,
                6,
                30L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(50),
                new CustomThreadFactory("bounded-"),
                new ThreadPoolExecutor.AbortPolicy()
            );
            
            return new ConcurrentTaskExecutor(threadPoolExecutor);
        }
    }

    /**
     * ForkJoinPool-based configuration
     */
    @Configuration
    static class ForkJoinConfiguration {
        
        @Bean(name = "forkJoinExecutor")
        public TaskExecutor forkJoinPoolExecutor() {
            // Custom ForkJoinPool
            ForkJoinPool forkJoinPool = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                true // async mode
            );
            
            return new ConcurrentTaskExecutor(forkJoinPool);
        }
        
        @Bean(name = "commonPoolExecutor")
        public TaskExecutor commonPoolExecutor() {
            // Use common ForkJoinPool
            return new ConcurrentTaskExecutor(ForkJoinPool.commonPool());
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
            Thread thread = new Thread(r, namePrefix + threadNumber.getOrElse(1));
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    /**
     * Executor monitoring utility
     */
    static class ExecutorMonitor {
        
        public static void monitorThreadPoolExecutor(ThreadPoolExecutor executor) {
            System.out.println("\n=== ThreadPoolExecutor Statistics ===");
            System.out.println("Active Count: " + executor.getActiveCount());
            System.out.println("Pool Size: " + executor.getPoolSize());
            System.out.println("Core Pool Size: " + executor.getCorePoolSize());
            System.out.println("Max Pool Size: " + executor.getMaximumPoolSize());
            System.out.println("Task Count: " + executor.getTaskCount());
            System.out.println("Completed Tasks: " + executor.getCompletedTaskCount());
            System.out.println("Queue Size: " + executor.getQueue().size());
        }
        
        public static void monitorForkJoinPool(ForkJoinPool pool) {
            System.out.println("\n=== ForkJoinPool Statistics ===");
            System.out.println("Parallelism: " + pool.getParallelism());
            System.out.println("Pool Size: " + pool.getPoolSize());
            System.out.println("Active Thread Count: " + pool.getActiveThreadCount());
            System.out.println("Running Thread Count: " + pool.getRunningThreadCount());
            System.out.println("Queued Submission Count: " + pool.getQueuedSubmissionCount());
            System.out.println("Queued Task Count: " + pool.getQueuedTaskCount());
            System.out.println("Steal Count: " + pool.getStealCount());
        }
    }

    /**
     * Usage examples
     */
    static class ConcurrentTaskExecutorExamples {
        
        public void demonstrateCachedThreadPool() throws InterruptedException {
            System.out.println("\n=== Cached Thread Pool ===");
            
            ExecutorService executor = Executors.newCachedThreadPool();
            TaskExecutor taskExecutor = new ConcurrentTaskExecutor(executor);
            
            // Submit multiple tasks
            for (int i = 1; i <= 5; i++) {
                final int taskId = i;
                taskExecutor.execute(() -> {
                    System.out.println("Task " + taskId + " on: " + 
                                     Thread.currentThread().getName());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            Thread.sleep(1000);
            executor.shutdown();
            System.out.println("Cached thread pool tasks completed");
        }
        
        public void demonstrateFixedThreadPool() throws InterruptedException {
            System.out.println("\n=== Fixed Thread Pool ===");
            
            ExecutorService executor = Executors.newFixedThreadPool(3);
            TaskExecutor taskExecutor = new ConcurrentTaskExecutor(executor);
            
            // Submit tasks (more than thread count)
            for (int i = 1; i <= 10; i++) {
                final int taskId = i;
                taskExecutor.execute(() -> {
                    System.out.println("Task " + taskId + " executing on: " + 
                                     Thread.currentThread().getName());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            Thread.sleep(2000);
            executor.shutdown();
            System.out.println("Fixed thread pool tasks completed");
        }
        
        public void demonstrateSingleThreadExecutor() throws InterruptedException {
            System.out.println("\n=== Single Thread Executor ===");
            
            ExecutorService executor = Executors.newSingleThreadExecutor();
            TaskExecutor taskExecutor = new ConcurrentTaskExecutor(executor);
            
            // Tasks execute sequentially
            for (int i = 1; i <= 5; i++) {
                final int taskId = i;
                taskExecutor.execute(() -> {
                    System.out.println("Task " + taskId + " at " + System.currentTimeMillis());
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            Thread.sleep(2000);
            executor.shutdown();
            System.out.println("Sequential execution completed");
        }
        
        public void demonstrateCustomExecutor() throws InterruptedException {
            System.out.println("\n=== Custom ThreadPoolExecutor ===");
            
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2, 4, 30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5),
                new CustomThreadFactory("custom-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
            
            TaskExecutor taskExecutor = new ConcurrentTaskExecutor(executor);
            
            // Submit tasks
            for (int i = 1; i <= 8; i++) {
                final int taskId = i;
                taskExecutor.execute(() -> {
                    System.out.println("Task " + taskId + " on: " + 
                                     Thread.currentThread().getName());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            ExecutorMonitor.monitorThreadPoolExecutor(executor);
            
            Thread.sleep(2000);
            executor.shutdown();
        }
        
        public void demonstrateForkJoinPool() throws InterruptedException {
            System.out.println("\n=== ForkJoinPool ===");
            
            ForkJoinPool forkJoinPool = new ForkJoinPool(4);
            TaskExecutor taskExecutor = new ConcurrentTaskExecutor(forkJoinPool);
            
            // Submit tasks
            for (int i = 1; i <= 10; i++) {
                final int taskId = i;
                taskExecutor.execute(() -> {
                    System.out.println("Task " + taskId + " on: " + 
                                     Thread.currentThread().getName());
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            ExecutorMonitor.monitorForkJoinPool(forkJoinPool);
            
            Thread.sleep(1000);
            forkJoinPool.shutdown();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Concurrent Task Executor Pattern - Java Executor Adapter");
        System.out.println("==========================================================");
        
        ConcurrentTaskExecutorExamples examples = new ConcurrentTaskExecutorExamples();
        
        examples.demonstrateCachedThreadPool();
        examples.demonstrateFixedThreadPool();
        examples.demonstrateSingleThreadExecutor();
        examples.demonstrateCustomExecutor();
        examples.demonstrateForkJoinPool();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Executor Types:");
        
        System.out.println("\nCachedThreadPool:");
        System.out.println("+ Creates threads as needed");
        System.out.println("+ Reuses idle threads");
        System.out.println("- Unbounded thread creation");
        
        System.out.println("\nFixedThreadPool:");
        System.out.println("+ Fixed thread count");
        System.out.println("+ Predictable resources");
        System.out.println("- Unbounded queue (memory risk)");
        
        System.out.println("\nSingleThreadExecutor:");
        System.out.println("+ Sequential execution");
        System.out.println("+ Guaranteed ordering");
        System.out.println("- No parallelism");
        
        System.out.println("\nWorkStealingPool:");
        System.out.println("+ Work stealing algorithm");
        System.out.println("+ Good for recursive tasks");
        System.out.println("+ Automatic load balancing");
        
        System.out.println("\nUse ConcurrentTaskExecutor when:");
        System.out.println("- Integrating with existing Java executors");
        System.out.println("- Need lightweight task execution");
        System.out.println("- Custom executor requirements");
        System.out.println("- Bridging Java and Spring concurrency");
    }
}
