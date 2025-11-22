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
 * Cron Trigger Pattern
 * 
 * Demonstrates scheduling tasks using cron expressions in Spring.
 * Cron expressions provide powerful and flexible scheduling capabilities.
 * 
 * Cron Expression Format:
 * ┌───────────── second (0-59)
 * │ ┌───────────── minute (0-59)
 * │ │ ┌───────────── hour (0-23)
 * │ │ │ ┌───────────── day of month (1-31)
 * │ │ │ │ ┌───────────── month (1-12 or JAN-DEC)
 * │ │ │ │ │ ┌───────────── day of week (0-7 or SUN-SAT, 0 and 7 = Sunday)
 * │ │ │ │ │ │
 * * * * * * *
 * 
 * Special Characters:
 * * : all values
 * ? : no specific value (day of month/week only)
 * - : range (e.g., 10-12)
 * , : list (e.g., MON,WED,FRI)
 * / : increments (e.g., 0/15 = every 15 units)
 * L : last (e.g., L = last day of month)
 * W : weekday (e.g., 15W = nearest weekday to the 15th)
 * # : nth day (e.g., 2#3 = 3rd Tuesday of month)
 * 
 * Use Cases:
 * - Daily reports generation
 * - Business hours operations
 * - Periodic data synchronization
 * - Time-zone aware scheduling
 * - Complex recurring patterns
 * 
 * @author Spring Patterns
 */
public class CronTriggerPattern {

    /**
     * Configuration class for cron-based scheduling
     */
    @Configuration
    @EnableScheduling
    static class CronSchedulingConfig {

        @Bean
        public TaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(5);
            scheduler.setThreadNamePrefix("cron-");
            scheduler.setAwaitTerminationSeconds(30);
            scheduler.setWaitForTasksToCompleteOnShutdown(true);
            scheduler.initialize();
            return scheduler;
        }

        @Bean
        public CronScheduledTasks cronScheduledTasks() {
            return new CronScheduledTasks();
        }
    }

    /**
     * Component containing various cron-scheduled tasks
     */
    @Component
    static class CronScheduledTasks {

        private final AtomicInteger everySecondCounter = new AtomicInteger(0);
        private final AtomicInteger everyMinuteCounter = new AtomicInteger(0);
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        /**
         * Runs every second
         * Cron: */1 * * * * *
         */
        @Scheduled(cron = "*/1 * * * * *")
        public void runEverySecond() {
            int count = everySecondCounter.incrementAndGet();
            System.out.println("[Every Second] Execution #" + count + 
                             " at " + LocalDateTime.now().format(formatter));
        }

        /**
         * Runs every 5 seconds
         * Cron: */5 * * * * *
         */
        @Scheduled(cron = "*/5 * * * * *")
        public void runEveryFiveSeconds() {
            System.out.println("[Every 5 Seconds] Executed at " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs every 10 seconds
         * Cron: */10 * * * * *
         */
        @Scheduled(cron = "*/10 * * * * *")
        public void runEveryTenSeconds() {
            System.out.println("[Every 10 Seconds] Executed at " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs at the start of every minute
         * Cron: 0 * * * * *
         */
        @Scheduled(cron = "0 * * * * *")
        public void runEveryMinute() {
            int count = everyMinuteCounter.incrementAndGet();
            System.out.println("[Every Minute] Execution #" + count + 
                             " at " + LocalDateTime.now().format(formatter));
        }

        /**
         * Runs every 15 seconds
         * Cron: 0/15 * * * * *
         */
        @Scheduled(cron = "0/15 * * * * *")
        public void runEveryFifteenSeconds() {
            System.out.println("[Every 15 Seconds] Executed at " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs at seconds 0, 15, 30, and 45 of every minute
         * Cron: 0,15,30,45 * * * * *
         */
        @Scheduled(cron = "0,15,30,45 * * * * *")
        public void runAtSpecificSeconds() {
            System.out.println("[Specific Seconds] Executed at " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs every 30 seconds
         * Cron: 0/30 * * * * *
         */
        @Scheduled(cron = "0/30 * * * * *")
        public void runEveryThirtySeconds() {
            System.out.println("[Every 30 Seconds] Executed at " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs during business hours (9 AM - 5 PM) every hour
         * Cron: 0 0 9-17 * * MON-FRI
         * 
         * Note: This is for demonstration. In a real scenario, this would run hourly
         * during business hours on weekdays.
         */
        @Scheduled(cron = "0 0 9-17 * * MON-FRI")
        public void runDuringBusinessHours() {
            System.out.println("[Business Hours] Executed at " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs every day at midnight
         * Cron: 0 0 0 * * *
         */
        @Scheduled(cron = "0 0 0 * * *")
        public void runDailyAtMidnight() {
            System.out.println("[Daily Midnight] Executed at " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs every Monday at 8 AM
         * Cron: 0 0 8 * * MON
         */
        @Scheduled(cron = "0 0 8 * * MON")
        public void runWeeklyReport() {
            System.out.println("[Weekly Report] Monday 8 AM - " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs on the first day of every month at midnight
         * Cron: 0 0 0 1 * *
         */
        @Scheduled(cron = "0 0 0 1 * *")
        public void runMonthlyReport() {
            System.out.println("[Monthly Report] First day of month - " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs every quarter (Jan, Apr, Jul, Oct) on the 1st at midnight
         * Cron: 0 0 0 1 1,4,7,10 *
         */
        @Scheduled(cron = "0 0 0 1 1,4,7,10 *")
        public void runQuarterlyReport() {
            System.out.println("[Quarterly Report] Executed at " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs every weekday at 6 PM
         * Cron: 0 0 18 * * MON-FRI
         */
        @Scheduled(cron = "0 0 18 * * MON-FRI")
        public void runWeekdayEndOfDay() {
            System.out.println("[End of Day] Weekday 6 PM - " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Runs every weekend at 10 AM
         * Cron: 0 0 10 * * SAT,SUN
         */
        @Scheduled(cron = "0 0 10 * * SAT,SUN")
        public void runWeekendTask() {
            System.out.println("[Weekend Task] Saturday/Sunday 10 AM - " + 
                             LocalDateTime.now().format(formatter));
        }

        /**
         * Cron expression from application properties
         * This allows dynamic configuration without code changes
         */
        @Scheduled(cron = "${app.cron.custom:*/20 * * * * *}")
        public void runWithConfigurableCron() {
            System.out.println("[Configurable] Executed at " + 
                             LocalDateTime.now().format(formatter));
        }
    }

    /**
     * Example tasks for specific business scenarios
     */
    @Component
    static class BusinessCronTasks {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        /**
         * Database backup - Daily at 2 AM
         * Cron: 0 0 2 * * *
         */
        @Scheduled(cron = "0 0 2 * * *")
        public void performDatabaseBackup() {
            System.out.println("[DB Backup] Starting backup at " + 
                             LocalDateTime.now().format(formatter));
            // Backup logic here
        }

        /**
         * Cache cleanup - Every hour at 5 minutes past
         * Cron: 0 5 * * * *
         */
        @Scheduled(cron = "0 5 * * * *")
        public void cleanupCache() {
            System.out.println("[Cache Cleanup] Cleaning cache at " + 
                             LocalDateTime.now().format(formatter));
            // Cache cleanup logic
        }

        /**
         * Log rotation - Daily at 11:59 PM
         * Cron: 0 59 23 * * *
         */
        @Scheduled(cron = "0 59 23 * * *")
        public void rotateLogs() {
            System.out.println("[Log Rotation] Rotating logs at " + 
                             LocalDateTime.now().format(formatter));
            // Log rotation logic
        }

        /**
         * Health check - Every 2 minutes
         * Cron: 0 */2 * * * *
         */
        @Scheduled(cron = "0 */2 * * * *")
        public void performHealthCheck() {
            System.out.println("[Health Check] Checking system health at " + 
                             LocalDateTime.now().format(formatter));
            // Health check logic
        }

        /**
         * Data synchronization - Every 5 minutes during business hours
         * Cron: 0 */5 9-17 * * MON-FRI
         */
        @Scheduled(cron = "0 */5 9-17 * * MON-FRI")
        public void synchronizeData() {
            System.out.println("[Data Sync] Synchronizing data at " + 
                             LocalDateTime.now().format(formatter));
            // Sync logic
        }

        /**
         * Email digest - Every weekday at 9 AM
         * Cron: 0 0 9 * * MON-FRI
         */
        @Scheduled(cron = "0 0 9 * * MON-FRI")
        public void sendDailyDigest() {
            System.out.println("[Daily Digest] Sending email digest at " + 
                             LocalDateTime.now().format(formatter));
            // Email sending logic
        }

        /**
         * Weekly analytics report - Every Sunday at 8 PM
         * Cron: 0 0 20 * * SUN
         */
        @Scheduled(cron = "0 0 20 * * SUN")
        public void generateWeeklyAnalytics() {
            System.out.println("[Weekly Analytics] Generating report at " + 
                             LocalDateTime.now().format(formatter));
            // Analytics generation logic
        }
    }

    /**
     * Demonstration of cron trigger patterns
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Cron Trigger Pattern Demo ===\n");
        System.out.println("Starting cron-based scheduled tasks...\n");

        System.out.println("Common Cron Examples:");
        System.out.println("  */5 * * * * *       : Every 5 seconds");
        System.out.println("  0 * * * * *         : Every minute");
        System.out.println("  0 */15 * * * *      : Every 15 minutes");
        System.out.println("  0 0 * * * *         : Every hour");
        System.out.println("  0 0 0 * * *         : Every day at midnight");
        System.out.println("  0 0 9-17 * * MON-FRI: Business hours (9 AM - 5 PM, Mon-Fri)");
        System.out.println("  0 0 0 1 * *         : First day of every month");
        System.out.println("  0 0 8 * * MON       : Every Monday at 8 AM");
        System.out.println();

        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(CronSchedulingConfig.class);

        System.out.println("Scheduled tasks are now running...");
        System.out.println("Monitoring for 60 seconds...\n");

        // Let tasks run for demonstration
        Thread.sleep(60000);

        System.out.println("\n=== Execution Statistics ===");
        CronScheduledTasks tasks = context.getBean(CronScheduledTasks.class);
        System.out.println("'Every Second' task executions: " + 
                         tasks.everySecondCounter.get());
        System.out.println("'Every Minute' task executions: " + 
                         tasks.everyMinuteCounter.get());

        context.close();
        System.out.println("\n=== Demo Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. Cron expressions provide flexible scheduling patterns
 * 2. Support for complex time-based triggers
 * 3. Can be configured in application properties for runtime changes
 * 4. Suitable for business-hour and calendar-based scheduling
 * 5. Spring supports standard cron syntax plus seconds field
 * 6. Timezone support available via zone attribute
 * 
 * CRON EXPRESSION EXAMPLES:
 * 
 * Every N seconds/minutes/hours:
 *   */5 * * * * *      : Every 5 seconds
 *   0 */10 * * * *     : Every 10 minutes
 *   0 0 */2 * * *      : Every 2 hours
 * 
 * Specific times:
 *   0 30 9 * * *       : 9:30 AM every day
 *   0 0 12 * * MON-FRI : Noon on weekdays
 *   0 15 10 15 * *     : 10:15 AM on the 15th of every month
 * 
 * Business patterns:
 *   0 0 9-17 * * MON-FRI : Every hour during business hours
 *   0 */30 9-17 * * *    : Every 30 minutes, 9 AM - 5 PM
 *   0 0 0 * * SAT,SUN    : Midnight on weekends
 * 
 * BEST PRACTICES:
 * 
 * 1. Use property placeholders for configurable cron expressions
 * 2. Document cron expressions with comments
 * 3. Consider timezone implications for multi-region applications
 * 4. Test cron expressions thoroughly before production
 * 5. Use online cron expression generators/validators
 * 6. Handle exceptions in scheduled methods
 * 7. Keep scheduled tasks short or use async execution
 * 8. Monitor task execution and failures
 * 
 * WHEN TO USE:
 * 
 * - Calendar-based scheduling (daily, weekly, monthly)
 * - Business hours operations
 * - Time-zone specific tasks
 * - Complex recurring patterns
 * - Reports and batch jobs with specific timing requirements
 */
