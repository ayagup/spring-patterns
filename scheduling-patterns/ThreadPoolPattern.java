package com.spring.patterns.scheduling;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread Pool Pattern
 * 
 * Demonstrates ThreadPoolTaskExecutor configuration and tuning for optimal performance.
 * This pattern covers thread pool sizing, queue management, rejection policies,
 * and monitoring for various workload types.
 * 
 * Key Configuration Parameters:
 * - corePoolSize: Minimum threads to keep alive
 * - maxPoolSize: Maximum threads to create
 * - queueCapacity: Size of task queue
 * - keepAliveSeconds: Time before idle threads terminate
 * - rejectionPolicy: How to handle rejected tasks
 * 
 * Thread Pool Behavior:
 * 1. Tasks submitted with core threads < corePoolSize → create new thread
 * 2. Core threads busy, queue not full → queue task
 * 3. Queue full, threads < maxPoolSize → create new thread
 * 4. Queue full, threads = maxPoolSize → apply rejection policy
 * 
 * Use Cases:
 * - High-throughput applications
 * - Resource-intensive processing
 * - Concurrent request handling
 * - Batch processing
 * - Scalable services
 * 
 * @author Spring Patterns
 */
public class ThreadPoolPattern {

    /**
     * Configuration with various thread pool strategies
     */
    @Configuration
    static class ThreadPoolConfig {

        /**
         * Fixed size thread pool - Predictable resource usage
         */
        @Bean(name = "fixedThreadPool")
        public ThreadPoolTaskExecutor fixedThreadPool() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(10);
            executor.setMaxPoolSize(10); // Same as core = fixed size
            executor.setQueueCapacity(100);
            executor.setThreadNamePrefix("fixed-");
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            executor.initialize();
            return executor;
        }

        /**
         * Elastic thread pool - Scales based on load
         */
        @Bean(name = "elasticThreadPool")
        public ThreadPoolTaskExecutor elasticThreadPool() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(5);
            executor.setMaxPoolSize(50); // Can scale up to 50
            executor.setQueueCapacity(200);
            executor.setKeepAliveSeconds(60);
            executor.setAllowCoreThreadTimeOut(true);
            executor.setThreadNamePrefix("elastic-");
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            executor.initialize();
            return executor;
        }

        /**
         * CPU-bound thread pool - Sized for CPU tasks
         */
        @Bean(name = "cpuBoundPool")
        public ThreadPoolTaskExecutor cpuBoundPool() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            int cores = Runtime.getRuntime().availableProcessors();
            executor.setCorePoolSize(cores);
            executor.setMaxPoolSize(cores * 2);
            executor.setQueueCapacity(cores * 10);
            executor.setThreadNamePrefix("cpu-");
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            executor.initialize();
            System.out.println("[CPU Pool] Configured with " + cores + " core threads");
            return executor;
        }

        /**
         * I/O-bound thread pool - Large pool for I/O wait
         */
        @Bean(name = "ioBoundPool")
        public ThreadPoolTaskExecutor ioBoundPool() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(50);
            executor.setMaxPoolSize(200);
            executor.setQueueCapacity(500);
            executor.setKeepAliveSeconds(120);
            executor.setThreadNamePrefix("io-");
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            executor.initialize();
            return executor;
        }

        /**
         * Thread pool with custom rejection policy
         */
        @Bean(name = "customRejectionPool")
        public ThreadPoolTaskExecutor customRejectionPool() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(2);
            executor.setMaxPoolSize(4);
            executor.setQueueCapacity(2); // Small queue to trigger rejection
            executor.setThreadNamePrefix("reject-");
            
            // Custom rejection handler
            executor.setRejectedExecutionHandler((r, e) -> {
                System.err.println("[Rejection Handler] Task rejected: " + r.toString());
                System.err.println("[Rejection Handler] Pool: " + e.getPoolSize() + 
                                 " active, Queue: " + e.getQueue().size());
            });
            
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            executor.initialize();
            return executor;
        }

        @Bean
        public ThreadPoolService threadPoolService(ThreadPoolTaskExecutor fixedThreadPool,
                                                   ThreadPoolTaskExecutor elasticThreadPool,
                                                   ThreadPoolTaskExecutor cpuBoundPool,
                                                   ThreadPoolTaskExecutor ioBoundPool,
                                                   ThreadPoolTaskExecutor customRejectionPool) {
            return new ThreadPoolService(fixedThreadPool, elasticThreadPool, cpuBoundPool, 
                                        ioBoundPool, customRejectionPool);
        }
    }

    /**
     * Service demonstrating thread pool usage and monitoring
     */
    @Component
    static class ThreadPoolService {

        private final ThreadPoolTaskExecutor fixedPool;
        private final ThreadPoolTaskExecutor elasticPool;
        private final ThreadPoolTaskExecutor cpuPool;
        private final ThreadPoolTaskExecutor ioPool;
        private final ThreadPoolTaskExecutor rejectionPool;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        public ThreadPoolService(ThreadPoolTaskExecutor fixed, ThreadPoolTaskExecutor elastic,
                               ThreadPoolTaskExecutor cpu, ThreadPoolTaskExecutor io,
                               ThreadPoolTaskExecutor rejection) {
            this.fixedPool = fixed;
            this.elasticPool = elastic;
            this.cpuPool = cpu;
            this.ioPool = io;
            this.rejectionPool = rejection;
        }

        /**
         * Demonstrate fixed pool behavior
         */
        public void testFixedPool() throws InterruptedException {
            System.out.println("\n=== Testing Fixed Thread Pool ===");
            CountDownLatch latch = new CountDownLatch(15);
            
            for (int i = 0; i < 15; i++) {
                final int taskId = i;
                fixedPool.execute(() -> {
                    try {
                        String thread = Thread.currentThread().getName();
                        System.out.println("[Fixed #" + taskId + "] Started on " + thread);
                        Thread.sleep(1000);
                        System.out.println("[Fixed #" + taskId + "] Completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(5, TimeUnit.SECONDS);
            printPoolStats("Fixed Pool", fixedPool);
        }

        /**
         * Demonstrate elastic pool scaling
         */
        public void testElasticPool() throws InterruptedException {
            System.out.println("\n=== Testing Elastic Thread Pool ===");
            
            // Submit tasks gradually to observe scaling
            for (int i = 0; i < 30; i++) {
                final int taskId = i;
                elasticPool.execute(() -> {
                    try {
                        String thread = Thread.currentThread().getName();
                        System.out.println("[Elastic #" + taskId + "] on " + thread);
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                
                if (i % 10 == 0) {
                    printPoolStats("Elastic Pool", elasticPool);
                    Thread.sleep(100);
                }
            }
            
            Thread.sleep(2000);
            printPoolStats("Elastic Pool (Final)", elasticPool);
        }

        /**
         * CPU-bound tasks
         */
        public void testCpuBoundPool() {
            System.out.println("\n=== Testing CPU-Bound Pool ===");
            
            for (int i = 0; i < 10; i++) {
                final int taskId = i;
                cpuPool.execute(() -> {
                    String thread = Thread.currentThread().getName();
                    System.out.println("[CPU Task #" + taskId + "] Computing on " + thread);
                    
                    // Simulate CPU work
                    long result = 0;
                    for (int j = 0; j < 10000000; j++) {
                        result += j;
                    }
                    
                    System.out.println("[CPU Task #" + taskId + "] Result: " + result);
                });
            }
        }

        /**
         * I/O-bound tasks
         */
        public void testIoBoundPool() {
            System.out.println("\n=== Testing I/O-Bound Pool ===");
            
            for (int i = 0; i < 20; i++) {
                final int taskId = i;
                ioPool.execute(() -> {
                    try {
                        String thread = Thread.currentThread().getName();
                        System.out.println("[I/O Task #" + taskId + "] Starting on " + thread);
                        Thread.sleep(500); // Simulate I/O wait
                        System.out.println("[I/O Task #" + taskId + "] Completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }

        /**
         * Test rejection policy
         */
        public void testRejectionPolicy() throws InterruptedException {
            System.out.println("\n=== Testing Rejection Policy ===");
            System.out.println("Pool: core=2, max=4, queue=2");
            System.out.println("Submitting 10 tasks to trigger rejections...");
            
            AtomicInteger completed = new AtomicInteger(0);
            
            for (int i = 0; i < 10; i++) {
                final int taskId = i;
                try {
                    rejectionPool.execute(() -> {
                        try {
                            String thread = Thread.currentThread().getName();
                            System.out.println("[Reject Test #" + taskId + "] on " + thread);
                            Thread.sleep(2000); // Long task to fill pool
                            completed.incrementAndGet();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                    System.out.println("Task #" + taskId + " submitted");
                } catch (Exception e) {
                    System.err.println("Task #" + taskId + " rejected immediately");
                }
                Thread.sleep(100);
            }
            
            Thread.sleep(5000);
            System.out.println("Completed tasks: " + completed.get());
        }

        /**
         * Print thread pool statistics
         */
        private void printPoolStats(String poolName, ThreadPoolTaskExecutor executor) {
            ThreadPoolExecutor tpe = executor.getThreadPoolExecutor();
            System.out.println("\n[" + poolName + " Stats]");
            System.out.println("  Active Threads: " + tpe.getActiveCount());
            System.out.println("  Pool Size: " + tpe.getPoolSize());
            System.out.println("  Core Pool Size: " + tpe.getCorePoolSize());
            System.out.println("  Max Pool Size: " + tpe.getMaximumPoolSize());
            System.out.println("  Queue Size: " + tpe.getQueue().size());
            System.out.println("  Queue Remaining: " + tpe.getQueue().remainingCapacity());
            System.out.println("  Completed Tasks: " + tpe.getCompletedTaskCount());
        }
    }

    /**
     * Demonstration of thread pool patterns
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Thread Pool Pattern Demo ===\n");
        System.out.println("Thread Pool Sizing Guidelines:");
        System.out.println("  CPU-bound: core threads = CPU cores");
        System.out.println("  I/O-bound: core threads = 2-4x CPU cores");
        System.out.println("  Mixed: Start with CPU cores, adjust based on testing\n");

        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(ThreadPoolConfig.class);
        
        ThreadPoolService service = context.getBean(ThreadPoolService.class);

        // Test different pool types
        service.testFixedPool();
        Thread.sleep(2000);
        
        service.testElasticPool();
        Thread.sleep(2000);
        
        service.testCpuBoundPool();
        Thread.sleep(2000);
        
        service.testIoBoundPool();
        Thread.sleep(2000);
        
        service.testRejectionPolicy();
        
        context.close();
        System.out.println("\n=== Demo Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. Size pools based on workload characteristics
 * 2. Configure rejection policies for overload
 * 3. Monitor pool statistics
 * 4. Choose appropriate queue capacity
 * 5. Set thread name prefixes for debugging
 * 6. Enable graceful shutdown
 * 
 * SIZING FORMULAS:
 * 
 * CPU-Bound Tasks:
 *   Optimal threads = Number of CPU cores
 *   Example: 8-core system → 8 threads
 * 
 * I/O-Bound Tasks:
 *   Optimal threads = Cores * (1 + Wait Time / Compute Time)
 *   Example: If wait:compute = 10:1 → 8 * 11 = 88 threads
 * 
 * Mixed Workload:
 *   Start with CPU cores, monitor and adjust
 *   Use separate pools for different task types
 * 
 * REJECTION POLICIES:
 * 
 * AbortPolicy (Default):
 *   - Throws RejectedExecutionException
 *   - Caller must handle exception
 *   - Use when tasks cannot be dropped
 * 
 * CallerRunsPolicy:
 *   - Runs task in caller thread
 *   - Provides throttling
 *   - Use for rate limiting
 * 
 * DiscardPolicy:
 *   - Silently drops task
 *   - Use for non-critical tasks
 * 
 * DiscardOldestPolicy:
 *   - Drops oldest queued task
 *   - Attempts to execute new task
 *   - Use when newer tasks more important
 * 
 * Custom:
 *   - Implement RejectedExecutionHandler
 *   - Log, queue elsewhere, etc.
 * 
 * CONFIGURATION PATTERNS:
 * 
 * Fixed Size Pool:
 *   core = max = desired threads
 *   Predictable resource usage
 *   Use when load is predictable
 * 
 * Elastic Pool:
 *   core < max
 *   Scales with load
 *   Use for variable workloads
 * 
 * Bounded Queue:
 *   Limited queue capacity
 *   Provides backpressure
 *   Use with rejection policy
 * 
 * Unbounded Queue:
 *   Integer.MAX_VALUE capacity
 *   No backpressure
 *   Risk of OOM
 * 
 * MONITORING:
 * 
 * Key Metrics:
 *   - Active thread count
 *   - Pool size (current)
 *   - Queue size
 *   - Completed task count
 *   - Rejection count
 * 
 * Warning Signs:
 *   - Pool always at max → increase size
 *   - Queue always full → increase capacity or threads
 *   - High rejection rate → review sizing
 *   - Low utilization → reduce pool size
 * 
 * BEST PRACTICES:
 * 
 * 1. Separate pools for different workloads
 * 2. Size based on task characteristics
 * 3. Configure rejection policies
 * 4. Set meaningful thread names
 * 5. Monitor pool statistics
 * 6. Test under load
 * 7. Enable graceful shutdown
 * 8. Set keep-alive for elastic pools
 * 9. Bound queue sizes
 * 10. Log rejections
 * 
 * COMMON MISTAKES:
 * 
 * 1. Too many threads → context switching overhead
 * 2. Too few threads → underutilized resources
 * 3. Unbounded queue → memory issues
 * 4. Wrong rejection policy → lost tasks
 * 5. No monitoring → can't optimize
 * 6. Same pool for all tasks → resource contention
 * 
 * WHEN TO USE EACH TYPE:
 * 
 * Fixed Pool:
 *   ✓ Predictable workload
 *   ✓ Known resource requirements
 *   ✓ Prevent over-provisioning
 * 
 * Elastic Pool:
 *   ✓ Variable workload
 *   ✓ Burst traffic
 *   ✓ Unknown load patterns
 * 
 * CPU-Bound Pool:
 *   ✓ Calculations
 *   ✓ Data processing
 *   ✓ Image/video processing
 * 
 * I/O-Bound Pool:
 *   ✓ Database operations
 *   ✓ File I/O
 *   ✓ Network calls
 *   ✓ External APIs
 */
