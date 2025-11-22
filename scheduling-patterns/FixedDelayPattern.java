package com.spring.patterns.scheduling;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed Delay Pattern
 * 
 * Demonstrates scheduling tasks with a fixed delay using @Scheduled(fixedDelay).
 * Fixed delay scheduling waits for the previous execution to complete before
 * starting the next execution after the specified delay.
 * 
 * Key Characteristics:
 * - Wait for task completion before starting delay timer
 * - Next execution starts: previous END + delay
 * - Ensures tasks never overlap
 * - Suitable for resource-intensive or sequential operations
 * 
 * Difference from Fixed Rate:
 * Fixed Delay: Time between END of one and START of next
 * Fixed Rate: Time between START of executions
 * 
 * Use Cases:
 * - Database maintenance tasks
 * - Batch processing
 * - Resource-intensive operations
 * - Sequential data processing
 * - Long-running tasks
 * - Tasks with variable execution time
 * 
 * @author Spring Patterns
 */
public class FixedDelayPattern {

    /**
     * Configuration for fixed delay scheduling
     */
    @Configuration
    @EnableScheduling
    static class FixedDelayConfig {

        @Bean
        public TaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(10);
            scheduler.setThreadNamePrefix("fixed-delay-");
            scheduler.setAwaitTerminationSeconds(30);
            scheduler.setWaitForTasksToCompleteOnShutdown(true);
            scheduler.initialize();
            return scheduler;
        }

        @Bean
        public FixedDelayTasks fixedDelayTasks() {
            return new FixedDelayTasks();
        }

        @Bean
        public FixedDelayBusinessTasks businessTasks() {
            return new FixedDelayBusinessTasks();
        }

        @Bean
        public VariableExecutionTimeTasks variableTimeTasks() {
            return new VariableExecutionTimeTasks();
        }
    }

    /**
     * Component demonstrating various fixed delay patterns
     */
    @Component
    static class FixedDelayTasks {

        private final AtomicInteger shortTaskCount = new AtomicInteger(0);
        private final AtomicInteger longTaskCount = new AtomicInteger(0);
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        /**
         * Short task - 1 second delay, 500ms execution
         * Total cycle: 1.5 seconds
         */
        @Scheduled(fixedDelay = 1000)
        public void shortTask() {
            int count = shortTaskCount.incrementAndGet();
            String startTime = LocalDateTime.now().format(formatter);
            
            System.out.println("[Short Task #" + count + "] Started at: " + startTime);
            
            try {
                Thread.sleep(500); // 500ms work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            String endTime = LocalDateTime.now().format(formatter);
            System.out.println("[Short Task #" + count + "] Finished at: " + endTime + 
                             " (next starts in 1 second)");
        }

        /**
         * Long task - 2 second delay, 3 second execution
         * Total cycle: 5 seconds (no overlap guaranteed)
         */
        @Scheduled(fixedDelay = 2000)
        public void longTask() {
            int count = longTaskCount.incrementAndGet();
            String startTime = LocalDateTime.now().format(formatter);
            
            System.out.println("[Long Task #" + count + "] Started at: " + startTime);
            
            try {
                Thread.sleep(3000); // 3 seconds work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            String endTime = LocalDateTime.now().format(formatter);
            System.out.println("[Long Task #" + count + "] Finished at: " + endTime + 
                             " (waited 3s, now waiting 2s before next)");
        }

        /**
         * Very short delay - 500ms
         * Rapid but sequential execution
         */
        @Scheduled(fixedDelay = 500)
        public void rapidSequentialTask() {
            System.out.println("[Rapid Sequential] Executed at: " + 
                             LocalDateTime.now().format(formatter));
            
            try {
                Thread.sleep(200); // 200ms work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Moderate delay - 3 seconds
         */
        @Scheduled(fixedDelay = 3000)
        public void moderateDelayTask() {
            System.out.println("[Moderate Delay] Executed at: " + 
                             LocalDateTime.now().format(formatter));
            
            try {
                Thread.sleep(1000); // 1 second work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Configurable delay via properties
         */
        @Scheduled(fixedDelayString = "${app.fixed.delay:2000}")
        public void configurableDelayTask() {
            System.out.println("[Configurable Delay] Executed at: " + 
                             LocalDateTime.now().format(formatter));
        }

        public int getShortTaskCount() {
            return shortTaskCount.get();
        }

        public int getLongTaskCount() {
            return longTaskCount.get();
        }
    }

    /**
     * Real-world business scenarios using fixed delay
     */
    @Component
    static class FixedDelayBusinessTasks {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        private final AtomicInteger batchCount = new AtomicInteger(0);
        private final AtomicInteger cleanupCount = new AtomicInteger(0);

        /**
         * Batch processing - Process after previous batch completes
         * 5 second delay between batches
         */
        @Scheduled(fixedDelay = 5000)
        public void processBatch() {
            int count = batchCount.incrementAndGet();
            System.out.println("[Batch Processing #" + count + "] Starting at: " + 
                             LocalDateTime.now().format(formatter));
            
            // Simulate batch processing (variable time)
            try {
                int processingTime = 2000 + (int) (Math.random() * 2000); // 2-4 seconds
                Thread.sleep(processingTime);
                System.out.println("[Batch Processing #" + count + "] Completed in " + 
                                 processingTime + "ms");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Database cleanup - Ensure previous cleanup completes
         * 10 second delay
         */
        @Scheduled(fixedDelay = 10000)
        public void cleanupDatabase() {
            int count = cleanupCount.incrementAndGet();
            System.out.println("[DB Cleanup #" + count + "] Starting at: " + 
                             LocalDateTime.now().format(formatter));
            
            // Simulate cleanup (2 seconds)
            try {
                Thread.sleep(2000);
                int recordsCleaned = (int) (Math.random() * 100);
                System.out.println("[DB Cleanup #" + count + "] Cleaned " + 
                                 recordsCleaned + " records");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Report generation - Sequential, no overlap
         * 15 second delay
         */
        @Scheduled(fixedDelay = 15000)
        public void generateReport() {
            System.out.println("[Report Generation] Starting at: " + 
                             LocalDateTime.now().format(formatter));
            
            // Simulate report generation
            try {
                Thread.sleep(3000);
                System.out.println("[Report Generation] Report completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * File processing - Sequential processing
         * 7 second delay
         */
        @Scheduled(fixedDelay = 7000)
        public void processFiles() {
            System.out.println("[File Processing] Starting at: " + 
                             LocalDateTime.now().format(formatter));
            
            // Simulate file processing
            try {
                Thread.sleep(2500);
                int filesProcessed = (int) (Math.random() * 10);
                System.out.println("[File Processing] Processed " + filesProcessed + " files");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Data export - Ensure complete before next export
         * 20 second delay
         */
        @Scheduled(fixedDelay = 20000)
        public void exportData() {
            System.out.println("[Data Export] Starting at: " + 
                             LocalDateTime.now().format(formatter));
            
            // Simulate data export
            try {
                Thread.sleep(4000);
                System.out.println("[Data Export] Export completed successfully");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Log archival - Sequential archiving
         * 12 second delay
         */
        @Scheduled(fixedDelay = 12000)
        public void archiveLogs() {
            System.out.println("[Log Archival] Starting at: " + 
                             LocalDateTime.now().format(formatter));
            
            // Simulate log archival
            try {
                Thread.sleep(1500);
                System.out.println("[Log Archival] Logs archived successfully");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public int getBatchCount() {
            return batchCount.get();
        }

        public int getCleanupCount() {
            return cleanupCount.get();
        }
    }

    /**
     * Demonstrates behavior with variable execution times
     */
    @Component
    static class VariableExecutionTimeTasks {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        /**
         * Variable duration task - execution time varies
         * 2 second delay after completion
         */
        @Scheduled(fixedDelay = 2000)
        public void variableDurationTask() {
            long startTime = System.currentTimeMillis();
            String startTimeStr = LocalDateTime.now().format(formatter);
            
            System.out.println("[Variable Duration] Started at: " + startTimeStr);
            
            // Variable execution time (1-4 seconds)
            int duration = 1000 + (int) (Math.random() * 3000);
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long endTime = System.currentTimeMillis();
            String endTimeStr = LocalDateTime.now().format(formatter);
            
            System.out.println("[Variable Duration] Finished at: " + endTimeStr + 
                             " (took " + (endTime - startTime) + "ms, next in 2s)");
        }
    }

    /**
     * Demonstration of fixed delay scheduling
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Fixed Delay Pattern Demo ===\n");
        System.out.println("Fixed Delay Scheduling:");
        System.out.println("- Waits for task completion before starting delay");
        System.out.println("- Time measured from END of previous execution");
        System.out.println("- Tasks never overlap");
        System.out.println("- Perfect for sequential, resource-intensive operations\n");

        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(FixedDelayConfig.class);

        System.out.println("Starting scheduled tasks...\n");
        System.out.println("Monitoring for 30 seconds...\n");

        // Monitor for 30 seconds
        Thread.sleep(30000);

        System.out.println("\n=== Execution Statistics ===");
        FixedDelayTasks tasks = context.getBean(FixedDelayTasks.class);
        FixedDelayBusinessTasks businessTasks = context.getBean(FixedDelayBusinessTasks.class);

        System.out.println("Short Task (1s delay, 500ms execution) count: " + 
                         tasks.getShortTaskCount());
        System.out.println("Long Task (2s delay, 3s execution) count: " + 
                         tasks.getLongTaskCount());
        System.out.println("Batch Processing (5s delay) count: " + 
                         businessTasks.getBatchCount());
        System.out.println("Database Cleanup (10s delay) count: " + 
                         businessTasks.getCleanupCount());

        System.out.println("\n=== Key Observations ===");
        System.out.println("1. Short Task: Total cycle = 500ms execution + 1000ms delay = 1.5s");
        System.out.println("2. Long Task: Total cycle = 3000ms execution + 2000ms delay = 5s");
        System.out.println("3. No task overlaps - sequential execution guaranteed");
        System.out.println("4. Adaptsto variable execution times automatically");

        System.out.println("\n=== When to Use Fixed Delay ===");
        System.out.println("✓ Resource-intensive operations (DB, file I/O)");
        System.out.println("✓ Tasks with variable execution time");
        System.out.println("✓ Sequential processing requirements");
        System.out.println("✓ Batch jobs");
        System.out.println("✓ Tasks that must complete before next runs");

        context.close();
        System.out.println("\n=== Demo Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. Fixed delay waits for task completion before starting delay timer
 * 2. Prevents task overlap - ideal for resource-intensive operations
 * 3. Adapts naturally to variable execution times
 * 4. Total cycle time = execution time + delay
 * 5. More predictable resource usage than fixed rate
 * 6. Measured in milliseconds
 * 
 * FIXED DELAY vs FIXED RATE:
 * 
 * Use Fixed Delay When:
 *   ✓ Task duration varies significantly
 *   ✓ Resource-intensive operations (DB, file I/O)
 *   ✓ Tasks must not overlap
 *   ✓ Sequential processing required
 *   ✓ Batch operations
 *   ✓ Long-running tasks
 * 
 * Use Fixed Rate When:
 *   ✓ Consistent timing required
 *   ✓ Lightweight, quick operations
 *   ✓ Time-sensitive monitoring
 *   ✓ Regular intervals more important than completion
 *   ✓ Parallel execution acceptable
 * 
 * Example Timelines (2 second delay/rate, variable execution):
 * 
 * Fixed Delay (1s, then 3s, then 2s execution):
 *   00:00 - Start execution 1 (1s duration)
 *   00:01 - End execution 1
 *   00:03 - Start execution 2 (2s delay after end) (3s duration)
 *   00:06 - End execution 2
 *   00:08 - Start execution 3 (2s delay after end) (2s duration)
 *   00:10 - End execution 3
 *   Cycle times: 3s, 5s, 4s (execution + delay)
 * 
 * Fixed Rate (same execution times):
 *   00:00 - Start execution 1 (1s duration)
 *   00:01 - End execution 1
 *   00:02 - Start execution 2 (3s duration)
 *   00:04 - Start execution 3 would queue (execution 2 not done)
 *   00:05 - End execution 2, Start execution 3
 *   Potential queuing and overlap issues
 * 
 * BEST PRACTICES:
 * 
 * 1. Choose delay longer than typical execution time
 * 2. Use for tasks that must complete before next runs
 * 3. Ideal for sequential batch processing
 * 4. Monitor execution times to optimize delay
 * 5. Handle exceptions to prevent scheduler breakdown
 * 6. Use properties for configurable delays
 * 7. Log execution start/end for monitoring
 * 8. Consider impact of failures on schedule
 * 
 * COMMON USE CASES:
 * 
 * Database Operations:
 *   @Scheduled(fixedDelay = 30000) // 30 seconds after completion
 *   public void cleanupExpiredRecords() { }
 * 
 * Batch Processing:
 *   @Scheduled(fixedDelay = 60000) // 1 minute after batch completes
 *   public void processBatchOrders() { }
 * 
 * File Processing:
 *   @Scheduled(fixedDelay = 5000) // 5 seconds after processing done
 *   public void processIncomingFiles() { }
 * 
 * Data Export:
 *   @Scheduled(fixedDelay = 300000) // 5 minutes after export completes
 *   public void exportDataToWarehouse() { }
 * 
 * ADVANTAGES:
 * 
 * 1. No task overlap - resource safe
 * 2. Adapts to varying execution times
 * 3. Predictable resource usage
 * 4. Simple to reason about
 * 5. No queuing issues
 * 6. Better for long-running tasks
 * 
 * CONSIDERATIONS:
 * 
 * 1. Total cycle time = execution + delay
 * 2. Long execution delays next run
 * 3. Not suitable for strict timing requirements
 * 4. Task failures affect subsequent schedule
 * 5. May have lower throughput than fixed rate
 */
