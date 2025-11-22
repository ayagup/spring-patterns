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
 * Fixed Rate Pattern
 * 
 * Demonstrates scheduling tasks at a fixed rate using @Scheduled(fixedRate).
 * Fixed rate scheduling executes tasks at regular intervals, regardless of
 * the previous execution's completion time.
 * 
 * Key Characteristics:
 * - Tasks execute at fixed intervals
 * - Next execution starts after the specified period from the PREVIOUS START
 * - If task takes longer than the interval, next execution may queue up
 * - Suitable for tasks that must run at regular intervals
 * 
 * Difference from Fixed Delay:
 * Fixed Rate: Time between START of executions
 * Fixed Delay: Time between END of one and START of next
 * 
 * Use Cases:
 * - Periodic health checks
 * - Regular status updates
 * - Time-sensitive monitoring
 * - Metrics collection
 * - Real-time data polling
 * 
 * @author Spring Patterns
 */
public class FixedRatePattern {

    /**
     * Configuration for fixed rate scheduling
     */
    @Configuration
    @EnableScheduling
    static class FixedRateConfig {

        @Bean
        public TaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(10);
            scheduler.setThreadNamePrefix("fixed-rate-");
            scheduler.setAwaitTerminationSeconds(30);
            scheduler.setWaitForTasksToCompleteOnShutdown(true);
            scheduler.initialize();
            return scheduler;
        }

        @Bean
        public FixedRateTasks fixedRateTasks() {
            return new FixedRateTasks();
        }

        @Bean
        public FixedRateBusinessTasks fixedRateBusinessTasks() {
            return new FixedRateBusinessTasks();
        }

        @Bean
        public FixedRateComparisonTasks comparisonTasks() {
            return new FixedRateComparisonTasks();
        }
    }

    /**
     * Component demonstrating various fixed rate scheduling patterns
     */
    @Component
    static class FixedRateTasks {

        private final AtomicInteger quickTaskCount = new AtomicInteger(0);
        private final AtomicInteger slowTaskCount = new AtomicInteger(0);
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        /**
         * Quick task - Executes every 1 second (1000ms)
         * Task completes before next execution
         */
        @Scheduled(fixedRate = 1000)
        public void quickTask() {
            int count = quickTaskCount.incrementAndGet();
            String startTime = LocalDateTime.now().format(formatter);
            
            System.out.println("[Quick Task #" + count + "] Started at: " + startTime);
            
            // Quick execution (100ms)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            String endTime = LocalDateTime.now().format(formatter);
            System.out.println("[Quick Task #" + count + "] Finished at: " + endTime);
        }

        /**
         * Slow task - Executes every 2 seconds
         * Task may take longer than the rate
         */
        @Scheduled(fixedRate = 2000)
        public void slowTask() {
            int count = slowTaskCount.incrementAndGet();
            String startTime = LocalDateTime.now().format(formatter);
            
            System.out.println("[Slow Task #" + count + "] Started at: " + startTime);
            
            // Slow execution (3 seconds - longer than rate)
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            String endTime = LocalDateTime.now().format(formatter);
            System.out.println("[Slow Task #" + count + "] Finished at: " + endTime + 
                             " (took 3 seconds, rate is 2 seconds)");
        }

        /**
         * Very frequent task - Every 500ms
         */
        @Scheduled(fixedRate = 500)
        public void veryFrequentTask() {
            System.out.println("[Very Frequent] Executed at: " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Moderate frequency - Every 5 seconds
         */
        @Scheduled(fixedRate = 5000)
        public void moderateTask() {
            System.out.println("[Moderate Task] Executed at: " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Using property placeholder for rate
         */
        @Scheduled(fixedRate = ${app.fixed.rate:3000})
        public void configurableRateTask() {
            System.out.println("[Configurable] Executed at: " + 
                             LocalDateTime.now().format(formatter));
        }

        public int getQuickTaskCount() {
            return quickTaskCount.get();
        }

        public int getSlowTaskCount() {
            return slowTaskCount.get();
        }
    }

    /**
     * Real-world business scenarios using fixed rate
     */
    @Component
    static class FixedRateBusinessTasks {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        private final AtomicInteger healthCheckCount = new AtomicInteger(0);
        private final AtomicInteger metricsCount = new AtomicInteger(0);

        /**
         * Health check - Every 10 seconds
         * Critical for monitoring system health
         */
        @Scheduled(fixedRate = 10000)
        public void performHealthCheck() {
            int count = healthCheckCount.incrementAndGet();
            System.out.println("[Health Check #" + count + "] Checking system health at: " + 
                             LocalDateTime.now().format(formatter));
            
            // Simulate health check
            boolean healthy = Math.random() > 0.1; // 90% success rate
            System.out.println("[Health Check #" + count + "] Status: " + 
                             (healthy ? "HEALTHY" : "WARNING"));
        }

        /**
         * Metrics collection - Every 5 seconds
         * Collect and report system metrics
         */
        @Scheduled(fixedRate = 5000)
        public void collectMetrics() {
            int count = metricsCount.incrementAndGet();
            System.out.println("[Metrics Collection #" + count + "] Collecting at: " + 
                             LocalDateTime.now().format(formatter));
            
            // Simulate metrics collection
            double cpuUsage = Math.random() * 100;
            double memoryUsage = Math.random() * 100;
            
            System.out.printf("[Metrics #%d] CPU: %.2f%%, Memory: %.2f%%%n", 
                            count, cpuUsage, memoryUsage);
        }

        /**
         * API polling - Every 3 seconds
         * Poll external API for updates
         */
        @Scheduled(fixedRate = 3000)
        public void pollExternalApi() {
            System.out.println("[API Polling] Fetching data at: " + 
                             LocalDateTime.now().format(formatter));
            // Simulate API call
        }

        /**
         * Cache refresh - Every 30 seconds
         * Keep cache up-to-date with regular refreshes
         */
        @Scheduled(fixedRate = 30000)
        public void refreshCache() {
            System.out.println("[Cache Refresh] Updating cache at: " + 
                             LocalDateTime.now().format(formatter));
            // Cache refresh logic
        }

        /**
         * Queue monitoring - Every 2 seconds
         * Monitor queue size and process messages
         */
        @Scheduled(fixedRate = 2000)
        public void monitorQueue() {
            System.out.println("[Queue Monitor] Checking queue at: " + 
                             LocalDateTime.now().format(formatter));
            
            // Simulate queue check
            int queueSize = (int) (Math.random() * 100);
            System.out.println("[Queue Monitor] Messages in queue: " + queueSize);
        }

        /**
         * Status update - Every 15 seconds
         * Send status updates to monitoring system
         */
        @Scheduled(fixedRate = 15000)
        public void sendStatusUpdate() {
            System.out.println("[Status Update] Sending update at: " + 
                             LocalDateTime.now().format(formatter));
            // Send status to monitoring system
        }

        public int getHealthCheckCount() {
            return healthCheckCount.get();
        }

        public int getMetricsCount() {
            return metricsCount.get();
        }
    }

    /**
     * Comparison between Fixed Rate and Fixed Delay behavior
     */
    @Component
    static class FixedRateComparisonTasks {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        /**
         * Fixed Rate: 2 seconds interval, 1 second execution
         * Next execution starts 2 seconds after PREVIOUS START
         */
        @Scheduled(fixedRate = 2000)
        public void fixedRateExample() {
            String startTime = LocalDateTime.now().format(formatter);
            System.out.println("[Fixed Rate Example] Started at: " + startTime);
            
            try {
                Thread.sleep(1000); // 1 second work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            String endTime = LocalDateTime.now().format(formatter);
            System.out.println("[Fixed Rate Example] Finished at: " + endTime + 
                             " (next starts 1 second after this start)");
        }

        /**
         * Fixed Delay: 2 seconds delay, 1 second execution
         * Next execution starts 2 seconds after PREVIOUS END
         */
        @Scheduled(fixedDelay = 2000)
        public void fixedDelayExample() {
            String startTime = LocalDateTime.now().format(formatter);
            System.out.println("[Fixed Delay Example] Started at: " + startTime);
            
            try {
                Thread.sleep(1000); // 1 second work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            String endTime = LocalDateTime.now().format(formatter);
            System.out.println("[Fixed Delay Example] Finished at: " + endTime + 
                             " (next starts 2 seconds after this end)");
        }
    }

    /**
     * Demonstration of fixed rate scheduling
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Fixed Rate Pattern Demo ===\n");
        System.out.println("Fixed Rate Scheduling:");
        System.out.println("- Executes at regular intervals");
        System.out.println("- Time measured from START of previous execution");
        System.out.println("- May queue up if task takes longer than rate\n");

        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(FixedRateConfig.class);

        System.out.println("Starting scheduled tasks...\n");
        System.out.println("Monitoring for 20 seconds...\n");

        // Monitor for 20 seconds
        Thread.sleep(20000);

        System.out.println("\n=== Execution Statistics ===");
        FixedRateTasks tasks = context.getBean(FixedRateTasks.class);
        FixedRateBusinessTasks businessTasks = context.getBean(FixedRateBusinessTasks.class);

        System.out.println("Quick Task (1s rate) executions: " + tasks.getQuickTaskCount());
        System.out.println("Slow Task (2s rate, 3s execution) executions: " + tasks.getSlowTaskCount());
        System.out.println("Health Check (10s rate) executions: " + businessTasks.getHealthCheckCount());
        System.out.println("Metrics Collection (5s rate) executions: " + businessTasks.getMetricsCount());

        System.out.println("\n=== Key Observations ===");
        System.out.println("1. Quick Task: Completes before next execution (normal behavior)");
        System.out.println("2. Slow Task: Takes longer than rate, may cause queuing");
        System.out.println("3. Fixed Rate: Consistent timing regardless of execution duration");
        System.out.println("4. Use Fixed Rate for: time-sensitive, periodic monitoring");

        context.close();
        System.out.println("\n=== Demo Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. Fixed rate executes at regular intervals from START of previous execution
 * 2. Suitable for time-sensitive periodic tasks
 * 3. May cause task queuing if execution exceeds rate
 * 4. Ensures consistent timing regardless of task duration
 * 5. Measured in milliseconds
 * 6. Can be configured via properties
 * 
 * FIXED RATE vs FIXED DELAY:
 * 
 * Fixed Rate (fixedRate):
 *   - Interval from START to START
 *   - Next execution starts at: previousStart + rate
 *   - May run concurrently if task is slow
 *   - Use for: regular intervals, monitoring, polling
 * 
 * Fixed Delay (fixedDelay):
 *   - Interval from END to START
 *   - Next execution starts at: previousEnd + delay
 *   - Never runs concurrently
 *   - Use for: sequential tasks, resource-intensive operations
 * 
 * Example Timeline (2 second rate, 1 second task):
 * 
 * Fixed Rate:
 *   00:00 - Start execution 1
 *   00:01 - End execution 1
 *   00:02 - Start execution 2 (2s from previous START)
 *   00:03 - End execution 2
 *   00:04 - Start execution 3
 * 
 * Fixed Delay:
 *   00:00 - Start execution 1
 *   00:01 - End execution 1
 *   00:03 - Start execution 2 (2s from previous END)
 *   00:04 - End execution 2
 *   00:06 - Start execution 3
 * 
 * BEST PRACTICES:
 * 
 * 1. Choose rate appropriate for task duration
 * 2. Monitor task execution time vs rate
 * 3. Use thread pool large enough for concurrent executions
 * 4. Handle InterruptedException properly
 * 5. Consider fixed delay if task duration varies significantly
 * 6. Use properties for configurable rates
 * 7. Add logging to track execution patterns
 * 8. Implement proper error handling in scheduled methods
 * 
 * COMMON PITFALLS:
 * 
 * 1. Rate shorter than task duration → queuing
 * 2. Too many concurrent executions → resource exhaustion
 * 3. Not handling exceptions → silent failures
 * 4. Fixed rate when fixed delay is more appropriate
 * 5. Blocking operations in scheduled methods
 * 
 * WHEN TO USE:
 * 
 * - Health checks and heartbeats
 * - Regular status updates
 * - Periodic monitoring
 * - Time-sensitive polling
 * - Metrics collection
 * - Real-time data synchronization
 * - Regular cache refreshes
 */
