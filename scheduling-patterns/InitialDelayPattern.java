package com.spring.patterns.scheduling;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Initial Delay Pattern
 * 
 * Demonstrates scheduling tasks with an initial delay before first execution.
 * The initialDelay parameter postpones the first execution, after which the
 * task runs according to fixedRate or fixedDelay settings.
 * 
 * Key Features:
 * - Delay first execution
 * - Allows application initialization before task starts
 * - Combine with fixedRate or fixedDelay
 * - Useful for staggered startup
 * - Prevents startup load spikes
 * 
 * Syntax:
 * @Scheduled(fixedRate = 5000, initialDelay = 10000)
 * @Scheduled(fixedDelay = 3000, initialDelay = 5000)
 * @Scheduled(fixedRateString = "${rate}", initialDelayString = "${delay}")
 * 
 * Use Cases:
 * - Wait for application warmup
 * - Delayed data synchronization
 * - Staggered service startup
 * - Resource-intensive initialization
 * - Preventing thundering herd on startup
 * 
 * @author Spring Patterns
 */
public class InitialDelayPattern {

    /**
     * Configuration for initial delay scheduling
     */
    @Configuration
    @EnableScheduling
    static class InitialDelayConfig {

        @Bean
        public TaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(8);
            scheduler.setThreadNamePrefix("initial-delay-");
            scheduler.setAwaitTerminationSeconds(30);
            scheduler.setWaitForTasksToCompleteOnShutdown(true);
            scheduler.initialize();
            return scheduler;
        }

        @Bean
        public InitialDelayTasks initialDelayTasks() {
            return new InitialDelayTasks();
        }

        @Bean
        public StaggeredStartupTasks staggeredTasks() {
            return new StaggeredStartupTasks();
        }

        @Bean
        public WarmupTasks warmupTasks() {
            return new WarmupTasks();
        }
    }

    /**
     * Component demonstrating various initial delay patterns
     */
    @Component
    static class InitialDelayTasks {

        private final AtomicInteger shortDelayCount = new AtomicInteger(0);
        private final AtomicInteger mediumDelayCount = new AtomicInteger(0);
        private final AtomicInteger longDelayCount = new AtomicInteger(0);
        private final AtomicLong applicationStartTime = new AtomicLong(System.currentTimeMillis());
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        /**
         * Short initial delay - 2 seconds
         * Then runs every second
         */
        @Scheduled(fixedRate = 1000, initialDelay = 2000)
        public void shortInitialDelay() {
            int count = shortDelayCount.incrementAndGet();
            long timeSinceStart = System.currentTimeMillis() - applicationStartTime.get();
            
            if (count == 1) {
                System.out.println("[Short Delay] First execution at: " + 
                                 LocalDateTime.now().format(formatter) + 
                                 " (" + timeSinceStart + "ms after start)");
            } else {
                System.out.println("[Short Delay #" + count + "] Executed at: " + 
                                 LocalDateTime.now().format(formatter));
            }
        }

        /**
         * Medium initial delay - 5 seconds
         * Then runs every 2 seconds
         */
        @Scheduled(fixedRate = 2000, initialDelay = 5000)
        public void mediumInitialDelay() {
            int count = mediumDelayCount.incrementAndGet();
            long timeSinceStart = System.currentTimeMillis() - applicationStartTime.get();
            
            if (count == 1) {
                System.out.println("[Medium Delay] First execution at: " + 
                                 LocalDateTime.now().format(formatter) + 
                                 " (" + timeSinceStart + "ms after start)");
            } else {
                System.out.println("[Medium Delay #" + count + "] Executed at: " + 
                                 LocalDateTime.now().format(formatter));
            }
        }

        /**
         * Long initial delay - 10 seconds
         * Then runs every 3 seconds
         */
        @Scheduled(fixedRate = 3000, initialDelay = 10000)
        public void longInitialDelay() {
            int count = longDelayCount.incrementAndGet();
            long timeSinceStart = System.currentTimeMillis() - applicationStartTime.get();
            
            if (count == 1) {
                System.out.println("[Long Delay] First execution at: " + 
                                 LocalDateTime.now().format(formatter) + 
                                 " (" + timeSinceStart + "ms after start)");
            } else {
                System.out.println("[Long Delay #" + count + "] Executed at: " + 
                                 LocalDateTime.now().format(formatter));
            }
        }

        /**
         * Initial delay with fixed delay
         * 3 second initial delay, then 2 second delay between executions
         */
        @Scheduled(fixedDelay = 2000, initialDelay = 3000)
        public void initialDelayWithFixedDelay() {
            System.out.println("[Initial + Fixed Delay] Executed at: " + 
                             LocalDateTime.now().format(formatter));
            
            // Simulate work
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Configurable initial delay via properties
         */
        @Scheduled(fixedRateString = "${app.task.rate:2000}", 
                   initialDelayString = "${app.task.initialDelay:4000}")
        public void configurableInitialDelay() {
            System.out.println("[Configurable Initial Delay] Executed at: " + 
                             LocalDateTime.now().format(formatter));
        }

        public int getShortDelayCount() {
            return shortDelayCount.get();
        }

        public int getMediumDelayCount() {
            return mediumDelayCount.get();
        }

        public int getLongDelayCount() {
            return longDelayCount.get();
        }
    }

    /**
     * Staggered startup to prevent resource contention
     */
    @Component
    static class StaggeredStartupTasks {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        private final long startupTime = System.currentTimeMillis();

        /**
         * Database sync - Start immediately after warmup (1 second delay)
         */
        @Scheduled(fixedDelay = 15000, initialDelay = 1000)
        public void databaseSync() {
            long elapsed = System.currentTimeMillis() - startupTime;
            System.out.println("[DB Sync] Starting at " + 
                             LocalDateTime.now().format(formatter) + 
                             " (+" + elapsed + "ms from startup)");
        }

        /**
         * Cache refresh - Start after DB sync (5 second delay)
         */
        @Scheduled(fixedDelay = 20000, initialDelay = 5000)
        public void cacheRefresh() {
            long elapsed = System.currentTimeMillis() - startupTime;
            System.out.println("[Cache Refresh] Starting at " + 
                             LocalDateTime.now().format(formatter) + 
                             " (+" + elapsed + "ms from startup)");
        }

        /**
         * Report generation - Start after cache is warm (10 second delay)
         */
        @Scheduled(fixedDelay = 30000, initialDelay = 10000)
        public void reportGeneration() {
            long elapsed = System.currentTimeMillis() - startupTime;
            System.out.println("[Report Generation] Starting at " + 
                             LocalDateTime.now().format(formatter) + 
                             " (+" + elapsed + "ms from startup)");
        }

        /**
         * External API sync - Start after all local services ready (15 second delay)
         */
        @Scheduled(fixedDelay = 25000, initialDelay = 15000)
        public void externalApiSync() {
            long elapsed = System.currentTimeMillis() - startupTime;
            System.out.println("[External API Sync] Starting at " + 
                             LocalDateTime.now().format(formatter) + 
                             " (+" + elapsed + "ms from startup)");
        }

        /**
         * Backup job - Start well after peak load (60 second delay)
         */
        @Scheduled(fixedDelay = 3600000, initialDelay = 60000)
        public void backupJob() {
            long elapsed = System.currentTimeMillis() - startupTime;
            System.out.println("[Backup Job] Starting at " + 
                             LocalDateTime.now().format(formatter) + 
                             " (+" + elapsed + "ms from startup)");
        }
    }

    /**
     * Tasks that need warmup before execution
     */
    @Component
    static class WarmupTasks {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        private boolean warmupComplete = false;

        /**
         * Quick warmup check - 1 second delay
         */
        @Scheduled(fixedRate = 5000, initialDelay = 1000)
        public void quickWarmupTask() {
            if (!warmupComplete) {
                System.out.println("[Quick Warmup] Starting at " + 
                                 LocalDateTime.now().format(formatter));
                // Simulate warmup
                warmupComplete = true;
            } else {
                System.out.println("[Quick Warmup] Running normally at " + 
                                 LocalDateTime.now().format(formatter));
            }
        }

        /**
         * Resource-intensive task - Wait for warmup (8 second delay)
         */
        @Scheduled(fixedDelay = 10000, initialDelay = 8000)
        public void resourceIntensiveTask() {
            System.out.println("[Resource Intensive] Executing at " + 
                             LocalDateTime.now().format(formatter) + 
                             " (warmup complete: " + warmupComplete + ")");
            
            // Simulate heavy processing
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Data migration - Significant delay to ensure system is ready (30 seconds)
         */
        @Scheduled(fixedDelay = 300000, initialDelay = 30000)
        public void dataMigrationTask() {
            System.out.println("[Data Migration] Starting at " + 
                             LocalDateTime.now().format(formatter) + 
                             " (system warmed up)");
        }
    }

    /**
     * Demonstration of initial delay patterns
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Initial Delay Pattern Demo ===\n");
        System.out.println("Application starting at: " + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        System.out.println("\nInitial Delay Benefits:");
        System.out.println("1. Allow application warmup before tasks start");
        System.out.println("2. Stagger task execution to prevent resource spikes");
        System.out.println("3. Ensure dependencies are ready");
        System.out.println("4. Control startup sequence\n");

        long appStartTime = System.currentTimeMillis();
        
        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(InitialDelayConfig.class);

        System.out.println("Context initialized. Waiting for scheduled tasks...\n");
        System.out.println("Task Startup Schedule:");
        System.out.println("  +1s  : Quick warmup, DB sync");
        System.out.println("  +2s  : Short delay task");
        System.out.println("  +3s  : Initial+Fixed delay task");
        System.out.println("  +5s  : Medium delay task, Cache refresh");
        System.out.println("  +10s : Long delay task, Report generation");
        System.out.println("  +15s : External API sync");
        System.out.println("\nMonitoring for 25 seconds...\n");

        // Monitor for 25 seconds to see staggered startup
        Thread.sleep(25000);

        System.out.println("\n=== Execution Statistics ===");
        InitialDelayTasks tasks = context.getBean(InitialDelayTasks.class);
        
        long totalRuntime = System.currentTimeMillis() - appStartTime;
        System.out.println("Total runtime: " + totalRuntime + "ms");
        System.out.println("\nTask Execution Counts:");
        System.out.println("Short Delay (2s initial, 1s rate): " + tasks.getShortDelayCount());
        System.out.println("Medium Delay (5s initial, 2s rate): " + tasks.getMediumDelayCount());
        System.out.println("Long Delay (10s initial, 3s rate): " + tasks.getLongDelayCount());

        System.out.println("\n=== Timeline Analysis ===");
        System.out.println("Short Delay: First at ~2s, then every 1s");
        System.out.println("  Expected in 25s: ~23 executions");
        System.out.println("  Actual: " + tasks.getShortDelayCount());
        System.out.println("\nMedium Delay: First at ~5s, then every 2s");
        System.out.println("  Expected in 25s: ~10 executions");
        System.out.println("  Actual: " + tasks.getMediumDelayCount());
        System.out.println("\nLong Delay: First at ~10s, then every 3s");
        System.out.println("  Expected in 25s: ~5 executions");
        System.out.println("  Actual: " + tasks.getLongDelayCount());

        context.close();
        System.out.println("\n=== Demo Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. initialDelay postpones first execution only
 * 2. Combines with fixedRate or fixedDelay for subsequent executions
 * 3. Measured in milliseconds
 * 4. Essential for staggered startup and warmup
 * 5. Prevents resource contention at startup
 * 6. Can be configured via properties
 * 
 * USAGE PATTERNS:
 * 
 * With Fixed Rate:
 *   @Scheduled(fixedRate = 5000, initialDelay = 10000)
 *   - Wait 10 seconds, then run every 5 seconds
 * 
 * With Fixed Delay:
 *   @Scheduled(fixedDelay = 3000, initialDelay = 5000)
 *   - Wait 5 seconds, then 3 second delay between executions
 * 
 * With Cron:
 *   @Scheduled(cron = "0 * * * * *", initialDelay = 60000)
 *   - Wait 1 minute, then run every minute
 * 
 * Configurable:
 *   @Scheduled(fixedRateString = "${rate}", initialDelayString = "${delay}")
 *   - Both rate and delay from properties
 * 
 * BEST PRACTICES:
 * 
 * 1. Stagger startup for multiple tasks
 *    - Task 1: initialDelay = 1000
 *    - Task 2: initialDelay = 5000
 *    - Task 3: initialDelay = 10000
 * 
 * 2. Allow time for dependencies
 *    - Database connections: short delay (1-2s)
 *    - Cache warmup: medium delay (5-10s)
 *    - External APIs: longer delay (10-30s)
 * 
 * 3. Resource-intensive tasks
 *    - Use longer initial delays
 *    - Prevent startup load spikes
 *    - Allow system stabilization
 * 
 * 4. Configuration
 *    - Use properties for flexibility
 *    - Different delays per environment
 *    - Adjust based on load testing
 * 
 * 5. Monitoring
 *    - Log first execution time
 *    - Track time from application start
 *    - Verify startup sequence
 * 
 * STAGGERED STARTUP EXAMPLE:
 * 
 * // Immediate priority tasks
 * @Scheduled(fixedDelay = 10000, initialDelay = 1000)  // +1s
 * public void criticalHealthCheck() { }
 * 
 * // High priority tasks
 * @Scheduled(fixedDelay = 15000, initialDelay = 5000)  // +5s
 * public void databaseSync() { }
 * 
 * // Normal priority tasks
 * @Scheduled(fixedDelay = 30000, initialDelay = 15000) // +15s
 * public void reportGeneration() { }
 * 
 * // Low priority tasks
 * @Scheduled(fixedDelay = 60000, initialDelay = 60000) // +1min
 * public void dataArchival() { }
 * 
 * COMMON SCENARIOS:
 * 
 * 1. Application Warmup:
 *    Problem: Tasks fail because dependencies not ready
 *    Solution: initialDelay = 5000 // Wait for warmup
 * 
 * 2. Thundering Herd:
 *    Problem: All tasks start simultaneously at boot
 *    Solution: Stagger with different initialDelays
 * 
 * 3. External API Rate Limits:
 *    Problem: Hit rate limits on startup
 *    Solution: Longer initialDelay to space out calls
 * 
 * 4. Database Connection Pool:
 *    Problem: Connection pool not initialized
 *    Solution: initialDelay = 2000 // Wait for pool
 * 
 * 5. Cache Dependencies:
 *    Problem: Cache not populated yet
 *    Solution: Ensure cache warmup task has shorter initialDelay
 * 
 * ADVANTAGES:
 * 
 * 1. Controlled startup sequence
 * 2. Prevents resource contention
 * 3. Allows dependency initialization
 * 4. Reduces startup failures
 * 5. Improves system stability
 * 6. Enables graceful warmup
 * 
 * WHEN TO USE:
 * 
 * ✓ Application needs warmup time
 * ✓ Tasks depend on other services
 * ✓ Preventing startup load spikes
 * ✓ Staggering resource-intensive tasks
 * ✓ External API integrations
 * ✓ Database-heavy operations
 * ✓ Cache population required
 */
