package com.spring.patterns.scheduling;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Async Execution Pattern
 * 
 * Demonstrates combining @Async with @Scheduled for asynchronous task execution.
 * This pattern allows scheduled tasks to run in separate threads without blocking
 * the scheduler, enabling better throughput and resource utilization.
 * 
 * Key Features:
 * - Non-blocking scheduled execution
 * - Parallel task processing
 * - Separate thread pools for scheduling and execution
 * - Return Future for async result handling
 * - Exception handling in async context
 * 
 * Benefits:
 * - Prevents scheduler blocking
 * - Improves application throughput
 * - Better resource utilization
 * - Enables long-running scheduled tasks
 * - Supports concurrent executions
 * 
 * Use Cases:
 * - Long-running scheduled operations
 * - I/O intensive tasks
 * - External API calls
 * - Parallel data processing
 * - Non-critical background jobs
 * 
 * @author Spring Patterns
 */
public class AsyncExecutionPattern {

    /**
     * Configuration for async scheduling
     */
    @Configuration
    @EnableScheduling
    @EnableAsync
    static class AsyncSchedulingConfig {

        /**
         * Task scheduler for triggering scheduled tasks
         */
        @Bean
        public ThreadPoolTaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(5);
            scheduler.setThreadNamePrefix("scheduler-");
            scheduler.setAwaitTerminationSeconds(30);
            scheduler.setWaitForTasksToCompleteOnShutdown(true);
            scheduler.initialize();
            return scheduler;
        }

        /**
         * Executor for async task execution
         */
        @Bean(name = "taskExecutor")
        public Executor taskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(10);
            executor.setMaxPoolSize(20);
            executor.setQueueCapacity(100);
            executor.setThreadNamePrefix("async-exec-");
            executor.setAwaitTerminationSeconds(60);
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.initialize();
            return executor;
        }

        @Bean
        public AsyncScheduledTasks asyncTasks() {
            return new AsyncScheduledTasks();
        }

        @Bean
        public LongRunningTasks longRunningTasks() {
            return new LongRunningTasks();
        }

        @Bean
        public AsyncWithResultTasks asyncResultTasks() {
            return new AsyncWithResultTasks();
        }
    }

    /**
     * Async scheduled tasks demonstrating non-blocking execution
     */
    @Component
    static class AsyncScheduledTasks {

        private final AtomicInteger fastTaskCount = new AtomicInteger(0);
        private final AtomicInteger slowTaskCount = new AtomicInteger(0);
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        /**
         * Fast async task - Runs every 1 second
         * Executes asynchronously without blocking scheduler
         */
        @Async
        @Scheduled(fixedRate = 1000)
        public void fastAsyncTask() {
            int count = fastTaskCount.incrementAndGet();
            String thread = Thread.currentThread().getName();
            String startTime = LocalDateTime.now().format(formatter);
            
            System.out.println("[Fast Async #" + count + "] Started at " + startTime + 
                             " on thread: " + thread);
            
            try {
                Thread.sleep(100); // Quick work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("[Fast Async #" + count + "] Completed");
        }

        /**
         * Slow async task - Runs every 2 seconds
         * Takes 3 seconds but doesn't block next execution
         */
        @Async
        @Scheduled(fixedRate = 2000)
        public void slowAsyncTask() {
            int count = slowTaskCount.incrementAndGet();
            String thread = Thread.currentThread().getName();
            String startTime = LocalDateTime.now().format(formatter);
            
            System.out.println("[Slow Async #" + count + "] Started at " + startTime + 
                             " on thread: " + thread);
            
            try {
                Thread.sleep(3000); // Long work (exceeds rate)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            String endTime = LocalDateTime.now().format(formatter);
            System.out.println("[Slow Async #" + count + "] Completed at " + endTime + 
                             " (took 3s, rate is 2s)");
        }

        /**
         * Parallel processing - Multiple executions can run concurrently
         */
        @Async
        @Scheduled(fixedRate = 1500)
        public void parallelProcessingTask() {
            String thread = Thread.currentThread().getName();
            String startTime = LocalDateTime.now().format(formatter);
            
            System.out.println("[Parallel] Started at " + startTime + 
                             " on thread: " + thread);
            
            try {
                Thread.sleep(2000); // Work takes longer than rate
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("[Parallel] Completed on thread: " + thread);
        }

        public int getFastTaskCount() {
            return fastTaskCount.get();
        }

        public int getSlowTaskCount() {
            return slowTaskCount.get();
        }
    }

    /**
     * Long-running async tasks that benefit from async execution
     */
    @Component
    static class LongRunningTasks {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        /**
         * External API call - Async prevents blocking
         */
        @Async
        @Scheduled(fixedDelay = 5000)
        public void callExternalApi() {
            String thread = Thread.currentThread().getName();
            System.out.println("[External API] Calling API at " + 
                             LocalDateTime.now().format(formatter) + 
                             " on thread: " + thread);
            
            try {
                Thread.sleep(2500); // Simulate API call
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("[External API] Call completed on thread: " + thread);
        }

        /**
         * Database batch operation - Long running
         */
        @Async
        @Scheduled(fixedDelay = 8000)
        public void batchDatabaseOperation() {
            String thread = Thread.currentThread().getName();
            System.out.println("[DB Batch] Starting operation at " + 
                             LocalDateTime.now().format(formatter) + 
                             " on thread: " + thread);
            
            try {
                Thread.sleep(4000); // Simulate batch operation
                int recordsProcessed = (int) (Math.random() * 1000);
                System.out.println("[DB Batch] Processed " + recordsProcessed + 
                                 " records on thread: " + thread);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * File processing - I/O intensive
         */
        @Async
        @Scheduled(fixedDelay = 6000)
        public void processFiles() {
            String thread = Thread.currentThread().getName();
            System.out.println("[File Processing] Starting at " + 
                             LocalDateTime.now().format(formatter) + 
                             " on thread: " + thread);
            
            try {
                Thread.sleep(3000); // Simulate file I/O
                int filesProcessed = (int) (Math.random() * 20);
                System.out.println("[File Processing] Processed " + filesProcessed + 
                                 " files on thread: " + thread);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Report generation - CPU intensive
         */
        @Async
        @Scheduled(cron = "0 */2 * * * *") // Every 2 minutes
        public void generateReport() {
            String thread = Thread.currentThread().getName();
            System.out.println("[Report] Generating report at " + 
                             LocalDateTime.now().format(formatter) + 
                             " on thread: " + thread);
            
            try {
                Thread.sleep(5000); // Simulate report generation
                System.out.println("[Report] Report completed on thread: " + thread);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Async tasks with return values using CompletableFuture
     */
    @Component
    static class AsyncWithResultTasks {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        /**
         * Data fetch with result
         */
        @Async
        @Scheduled(fixedDelay = 7000)
        public CompletableFuture<String> fetchDataAsync() {
            String thread = Thread.currentThread().getName();
            System.out.println("[Async Result] Fetching data at " + 
                             LocalDateTime.now().format(formatter) + 
                             " on thread: " + thread);
            
            try {
                Thread.sleep(2000); // Simulate data fetch
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return CompletableFuture.completedFuture("ERROR");
            }
            
            String result = "Data fetched successfully";
            System.out.println("[Async Result] " + result + " on thread: " + thread);
            return CompletableFuture.completedFuture(result);
        }

        /**
         * Calculation with result
         */
        @Async
        @Scheduled(fixedDelay = 9000)
        public CompletableFuture<Integer> calculateAsync() {
            String thread = Thread.currentThread().getName();
            System.out.println("[Async Calculate] Starting calculation at " + 
                             LocalDateTime.now().format(formatter) + 
                             " on thread: " + thread);
            
            try {
                Thread.sleep(1500); // Simulate calculation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return CompletableFuture.completedFuture(-1);
            }
            
            int result = (int) (Math.random() * 1000);
            System.out.println("[Async Calculate] Result: " + result + 
                             " on thread: " + thread);
            return CompletableFuture.completedFuture(result);
        }
    }

    /**
     * Demonstration of async execution pattern
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Async Execution Pattern Demo ===\n");
        System.out.println("Async Scheduling Benefits:");
        System.out.println("1. Scheduled tasks execute in separate threads");
        System.out.println("2. Scheduler is never blocked");
        System.out.println("3. Tasks can run concurrently");
        System.out.println("4. Better resource utilization");
        System.out.println("5. Supports long-running operations\n");

        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(AsyncSchedulingConfig.class);

        System.out.println("Async scheduled tasks started...\n");
        System.out.println("Watch for:");
        System.out.println("- Tasks executing on different threads (async-exec-*)");
        System.out.println("- Scheduler thread names (scheduler-*)");
        System.out.println("- Concurrent execution of slow tasks");
        System.out.println("- No blocking despite long execution times\n");

        System.out.println("Monitoring for 20 seconds...\n");

        // Monitor for 20 seconds
        Thread.sleep(20000);

        System.out.println("\n=== Execution Statistics ===");
        AsyncScheduledTasks tasks = context.getBean(AsyncScheduledTasks.class);
        
        System.out.println("Fast Task (1s rate, 100ms execution): " + tasks.getFastTaskCount());
        System.out.println("Slow Task (2s rate, 3s execution): " + tasks.getSlowTaskCount());

        System.out.println("\n=== Key Observations ===");
        System.out.println("1. Slow task executes concurrently (3s task, 2s rate)");
        System.out.println("2. Multiple instances of slow task can run in parallel");
        System.out.println("3. Scheduler continues triggering despite long execution");
        System.out.println("4. Different threads handle each execution");
        System.out.println("5. No scheduler blocking observed");

        System.out.println("\n=== Thread Pool Configuration ===");
        System.out.println("Scheduler Pool: 5 threads for triggering tasks");
        System.out.println("Executor Pool: 10-20 threads for executing tasks");
        System.out.println("Queue Capacity: 100 for pending tasks");

        context.close();
        System.out.println("\n=== Demo Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. @Async enables non-blocking scheduled execution
 * 2. Requires @EnableAsync on configuration
 * 3. Separate thread pools for scheduling and execution
 * 4. Allows concurrent executions of same task
 * 5. Supports CompletableFuture for results
 * 6. Better for I/O and long-running operations
 * 
 * CONFIGURATION:
 * 
 * Enable Async:
 *   @Configuration
 *   @EnableAsync
 *   @EnableScheduling
 *   public class Config { }
 * 
 * Configure Executor:
 *   @Bean(name = "taskExecutor")
 *   public Executor taskExecutor() {
 *       ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 *       executor.setCorePoolSize(10);
 *       executor.setMaxPoolSize(20);
 *       executor.setQueueCapacity(100);
 *       return executor;
 *   }
 * 
 * Use Async:
 *   @Async
 *   @Scheduled(fixedRate = 5000)
 *   public void asyncTask() { }
 * 
 * BEST PRACTICES:
 * 
 * 1. Size thread pools appropriately
 *    - Scheduler pool: 5-10 threads (lightweight)
 *    - Executor pool: Based on concurrent task needs
 * 
 * 2. Configure queue capacity
 *    - Prevent memory issues with bounded queue
 *    - Set rejection policy for queue overflow
 * 
 * 3. Handle exceptions properly
 *    - Use try-catch in async methods
 *    - Configure AsyncUncaughtExceptionHandler
 * 
 * 4. Monitor thread usage
 *    - Track active threads
 *    - Watch for thread pool exhaustion
 * 
 * 5. Be aware of concurrency
 *    - Multiple executions may run simultaneously
 *    - Use thread-safe data structures
 *    - Consider synchronization if needed
 * 
 * 6. Use for appropriate scenarios
 *    - I/O operations (file, network, database)
 *    - External API calls
 *    - Long-running computations
 *    - Non-critical background tasks
 * 
 * ASYNC vs SYNCHRONOUS:
 * 
 * Synchronous (@Scheduled only):
 *   - Blocks scheduler until task completes
 *   - No concurrent executions
 *   - Predictable execution order
 *   - Lower resource usage
 *   - Simpler to reason about
 * 
 * Asynchronous (@Async + @Scheduled):
 *   - Never blocks scheduler
 *   - Concurrent executions possible
 *   - Higher throughput
 *   - Better for I/O operations
 *   - More complex error handling
 * 
 * EXAMPLE CONFIGURATIONS:
 * 
 * I/O Intensive:
 *   @Async
 *   @Scheduled(fixedDelay = 5000)
 *   public void fetchExternalData() {
 *       // API call, file I/O
 *   }
 * 
 * Long Running Batch:
 *   @Async
 *   @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
 *   public void processBatch() {
 *       // Long-running batch job
 *   }
 * 
 * With Result:
 *   @Async
 *   @Scheduled(fixedRate = 10000)
 *   public CompletableFuture<Data> fetchData() {
 *       Data result = expensiveOperation();
 *       return CompletableFuture.completedFuture(result);
 *   }
 * 
 * EXCEPTION HANDLING:
 * 
 * Configure Handler:
 *   @Configuration
 *   @EnableAsync
 *   public class AsyncConfig implements AsyncConfigurer {
 *       @Override
 *       public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
 *           return new CustomAsyncExceptionHandler();
 *       }
 *   }
 * 
 * Custom Handler:
 *   public class CustomAsyncExceptionHandler 
 *           implements AsyncUncaughtExceptionHandler {
 *       @Override
 *       public void handleUncaughtException(
 *               Throwable ex, Method method, Object... params) {
 *           // Log exception, send alert, etc.
 *       }
 *   }
 * 
 * WHEN TO USE:
 * 
 * Use @Async + @Scheduled When:
 *   ✓ Task execution time >> scheduling interval
 *   ✓ I/O intensive operations
 *   ✓ External API calls
 *   ✓ Parallel processing needed
 *   ✓ Long-running computations
 *   ✓ High throughput required
 * 
 * Use @Scheduled Only When:
 *   ✓ Quick, lightweight tasks
 *   ✓ Sequential execution required
 *   ✓ Order matters
 *   ✓ Resource-constrained environments
 *   ✓ Simple coordination needed
 * 
 * ADVANTAGES:
 * 
 * 1. Non-blocking scheduler
 * 2. Higher throughput
 * 3. Better resource utilization
 * 4. Supports long operations
 * 5. Enables parallelism
 * 6. Scalable architecture
 * 
 * CONSIDERATIONS:
 * 
 * 1. Thread pool configuration critical
 * 2. Concurrent executions require thread safety
 * 3. More complex debugging
 * 4. Exception handling more involved
 * 5. Resource management important
 * 6. May have higher memory footprint
 */
