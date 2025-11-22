package com.spring.patterns.scheduling;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task Executor Pattern
 * 
 * Demonstrates Spring's TaskExecutor abstraction for executing tasks asynchronously.
 * TaskExecutor provides a simple interface for task execution while abstracting
 * the underlying thread management details.
 * 
 * Key Features:
 * - Simple execute(Runnable) interface
 * - Thread pool management
 * - Task queuing and rejection policies
 * - Resource management
 * - Spring's abstraction over java.util.concurrent.Executor
 * 
 * Types of TaskExecutors:
 * - SimpleAsyncTaskExecutor: Creates new thread per task
 * - SyncTaskExecutor: Executes synchronously (same thread)
 * - ConcurrentTaskExecutor: Wraps java.util.concurrent.Executor
 * - ThreadPoolTaskExecutor: Configurable thread pool (most common)
 * - WorkManagerTaskExecutor: JCA WorkManager integration
 * 
 * Use Cases:
 * - Asynchronous task execution
 * - Background processing
 * - Parallel computations
 * - Resource management
 * - Controlled concurrency
 * 
 * @author Spring Patterns
 */
public class TaskExecutorPattern {

    /**
     * Configuration with multiple task executors
     */
    @Configuration
    static class TaskExecutorConfig {

        /**
         * Default task executor - General purpose
         */
        @Bean(name = "defaultExecutor")
        public TaskExecutor defaultExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(5);
            executor.setMaxPoolSize(10);
            executor.setQueueCapacity(25);
            executor.setThreadNamePrefix("default-");
            executor.initialize();
            return executor;
        }

        /**
         * I/O task executor - For I/O bound tasks
         */
        @Bean(name = "ioExecutor")
        public TaskExecutor ioExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(20);
            executor.setMaxPoolSize(50);
            executor.setQueueCapacity(100);
            executor.setThreadNamePrefix("io-");
            executor.setKeepAliveSeconds(60);
            executor.initialize();
            return executor;
        }

        /**
         * CPU task executor - For CPU bound tasks
         */
        @Bean(name = "cpuExecutor")
        public TaskExecutor cpuExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            int processors = Runtime.getRuntime().availableProcessors();
            executor.setCorePoolSize(processors);
            executor.setMaxPoolSize(processors * 2);
            executor.setQueueCapacity(50);
            executor.setThreadNamePrefix("cpu-");
            executor.initialize();
            return executor;
        }

        @Bean
        public TaskExecutorService executorService(TaskExecutor defaultExecutor, 
                                                    TaskExecutor ioExecutor, 
                                                    TaskExecutor cpuExecutor) {
            return new TaskExecutorService(defaultExecutor, ioExecutor, cpuExecutor);
        }
    }

    /**
     * Service demonstrating TaskExecutor usage
     */
    @Component
    static class TaskExecutorService {

        private final TaskExecutor defaultExecutor;
        private final TaskExecutor ioExecutor;
        private final TaskExecutor cpuExecutor;
        private final AtomicInteger taskCounter = new AtomicInteger(0);
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        public TaskExecutorService(TaskExecutor defaultExecutor, 
                                 TaskExecutor ioExecutor, 
                                 TaskExecutor cpuExecutor) {
            this.defaultExecutor = defaultExecutor;
            this.ioExecutor = ioExecutor;
            this.cpuExecutor = cpuExecutor;
        }

        /**
         * Submit simple task
         */
        public void executeSimpleTask(String taskName) {
            defaultExecutor.execute(() -> {
                String thread = Thread.currentThread().getName();
                System.out.println("[" + taskName + "] Executing on thread: " + thread + 
                                 " at " + LocalDateTime.now().format(formatter));
            });
        }

        /**
         * Submit multiple parallel tasks
         */
        public void executeParallelTasks(int count) throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(count);
            
            for (int i = 0; i < count; i++) {
                final int taskId = i;
                defaultExecutor.execute(() -> {
                    try {
                        String thread = Thread.currentThread().getName();
                        System.out.println("[Parallel Task #" + taskId + "] Started on " + thread);
                        
                        Thread.sleep(1000); // Simulate work
                        
                        System.out.println("[Parallel Task #" + taskId + "] Completed on " + thread);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(10, TimeUnit.SECONDS);
            System.out.println("All parallel tasks completed");
        }

        /**
         * Execute I/O bound task
         */
        public void executeIoTask(String operation) {
            ioExecutor.execute(() -> {
                String thread = Thread.currentThread().getName();
                System.out.println("[I/O Task: " + operation + "] Started on " + thread);
                
                try {
                    Thread.sleep(500); // Simulate I/O
                    System.out.println("[I/O Task: " + operation + "] Completed on " + thread);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        /**
         * Execute CPU bound task
         */
        public void executeCpuTask(String computation) {
            cpuExecutor.execute(() -> {
                String thread = Thread.currentThread().getName();
                System.out.println("[CPU Task: " + computation + "] Started on " + thread);
                
                // Simulate CPU work
                long result = 0;
                for (int i = 0; i < 1000000; i++) {
                    result += i;
                }
                
                System.out.println("[CPU Task: " + computation + "] Result: " + result + 
                                 " on " + thread);
            });
        }

        /**
         * Execute with error handling
         */
        public void executeWithErrorHandling(boolean shouldFail) {
            defaultExecutor.execute(() -> {
                try {
                    String thread = Thread.currentThread().getName();
                    System.out.println("[Error Handling Task] Started on " + thread);
                    
                    if (shouldFail) {
                        throw new RuntimeException("Simulated failure");
                    }
                    
                    System.out.println("[Error Handling Task] Completed successfully");
                } catch (Exception e) {
                    System.err.println("[Error Handling Task] Failed: " + e.getMessage());
                }
            });
        }

        /**
         * Execute batch of tasks
         */
        public void executeBatch(int batchSize) {
            System.out.println("Executing batch of " + batchSize + " tasks...");
            
            for (int i = 0; i < batchSize; i++) {
                final int taskId = taskCounter.incrementAndGet();
                defaultExecutor.execute(() -> {
                    String thread = Thread.currentThread().getName();
                    System.out.println("[Batch Task #" + taskId + "] Processed on " + thread);
                });
            }
        }
    }

    /**
     * Demonstration of TaskExecutor pattern
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Task Executor Pattern Demo ===\n");
        
        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(TaskExecutorConfig.class);
        
        TaskExecutorService service = context.getBean(TaskExecutorService.class);

        // 1. Simple task execution
        System.out.println("1. Executing simple tasks...");
        service.executeSimpleTask("Task-A");
        service.executeSimpleTask("Task-B");
        service.executeSimpleTask("Task-C");
        Thread.sleep(500);

        // 2. Parallel task execution
        System.out.println("\n2. Executing parallel tasks...");
        service.executeParallelTasks(5);
        Thread.sleep(500);

        // 3. I/O bound tasks
        System.out.println("\n3. Executing I/O bound tasks...");
        service.executeIoTask("Read File");
        service.executeIoTask("Database Query");
        service.executeIoTask("API Call");
        Thread.sleep(1000);

        // 4. CPU bound tasks
        System.out.println("\n4. Executing CPU bound tasks...");
        service.executeCpuTask("Calculate Sum");
        service.executeCpuTask("Process Array");
        service.executeCpuTask("Sort Data");
        Thread.sleep(500);

        // 5. Error handling
        System.out.println("\n5. Testing error handling...");
        service.executeWithErrorHandling(false);
        service.executeWithErrorHandling(true);
        Thread.sleep(500);

        // 6. Batch execution
        System.out.println("\n6. Executing batch...");
        service.executeBatch(10);
        Thread.sleep(1000);

        context.close();
        System.out.println("\n=== Demo Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. TaskExecutor provides simple async execution
 * 2. Abstracts thread pool management
 * 3. Multiple executors for different workloads
 * 4. Configure pool sizes based on task characteristics
 * 5. Proper error handling required
 * 6. Resource cleanup important
 * 
 * CONFIGURATION GUIDE:
 * 
 * I/O Bound Tasks (Network, File, Database):
 *   - High core pool size (20-50)
 *   - High max pool size (50-100)
 *   - Large queue capacity (100-500)
 *   - Tasks spend time waiting
 * 
 * CPU Bound Tasks (Calculations, Processing):
 *   - Core pool size = CPU cores
 *   - Max pool size = 2 * CPU cores
 *   - Moderate queue (50-100)
 *   - Tasks use CPU actively
 * 
 * General Purpose:
 *   - Core pool size: 5-10
 *   - Max pool size: 10-20
 *   - Queue capacity: 25-100
 *   - Mixed workload
 * 
 * BEST PRACTICES:
 * 
 * 1. Size pools based on workload type
 * 2. Set meaningful thread name prefixes
 * 3. Configure rejection policies
 * 4. Handle exceptions in tasks
 * 5. Monitor thread pool metrics
 * 6. Shut down gracefully
 * 
 * WHEN TO USE:
 * 
 * ✓ Background processing
 * ✓ Asynchronous operations
 * ✓ Parallel computations
 * ✓ Resource-intensive tasks
 * ✓ Controlled concurrency
 */
