package com.example.scheduling;

import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Cron Expression Pattern
 * =======================
 * 
 * Demonstrates the use of cron expressions for flexible task scheduling in Spring.
 * Cron expressions provide powerful time-based scheduling with support for complex
 * patterns including specific times, ranges, intervals, and calendar-based scheduling.
 * 
 * Key Concepts:
 * ------------
 * 1. Cron Expression - Time-based scheduling pattern
 * 2. Cron Fields - second, minute, hour, day, month, weekday
 * 3. Special Characters - *, ?, -, , / L W #
 * 4. CronTrigger - Spring's cron implementation
 * 5. CronExpression - Parse and validate cron strings
 * 6. Timezone Support - Schedule in specific timezones
 * 7. Next Execution - Calculate next run time
 * 
 * Cron Expression Format:
 * ----------------------
 * ┌───────────── second (0-59)
 * │ ┌───────────── minute (0-59)
 * │ │ ┌───────────── hour (0-23)
 * │ │ │ ┌───────────── day of month (1-31)
 * │ │ │ │ ┌───────────── month (1-12 or JAN-DEC)
 * │ │ │ │ │ ┌───────────── day of week (0-7 or SUN-SAT, 0 & 7 = Sunday)
 * │ │ │ │ │ │
 * * * * * * *
 * 
 * Special Characters:
 * ------------------
 * * (asterisk)     - All values (any value)
 * ? (question)     - No specific value (day-of-month or day-of-week only)
 * - (dash)         - Range of values (10-12)
 * , (comma)        - List of values (1,5,10)
 * / (slash)        - Increment (0/15 = 0, 15, 30, 45)
 * L (last)         - Last day of month or last specific weekday
 * W (weekday)      - Nearest weekday to given day
 * # (hash)         - Nth occurrence of weekday (2#1 = first Tuesday)
 * 
 * Examples:
 * --------
 * "0 0 * * * *"          - Every hour
 * "0 */15 * * * *"       - Every 15 minutes
 * "0 0 0 * * *"          - Every day at midnight
 * "0 0 9-17 * * MON-FRI" - Weekdays, every hour from 9 AM to 5 PM
 * "0 0 0 L * *"          - Last day of every month
 * 
 * When to Use:
 * -----------
 * - Calendar-based scheduling
 * - Business hours operations
 * - Daily/weekly/monthly tasks
 * - Specific time requirements
 * - Complex time patterns
 * - Timezone-aware scheduling
 * 
 * Best Practices:
 * --------------
 * - Validate expressions before deployment
 * - Test with different timezones
 * - Document complex expressions
 * - Use constants for common patterns
 * - Consider DST changes
 * - Externalize to properties
 * - Test edge cases (month end, leap years)
 * - Use online cron calculators for verification
 * 
 * @author Spring Patterns
 * @version 1.0
 */

public class CronExpressionPattern {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Example 1: Common Cron Expressions
     */
    public static class CommonCronExpressions {
        
        // Every second
        public static final String EVERY_SECOND = "* * * * * *";
        
        // Every 5 seconds
        public static final String EVERY_5_SECONDS = "*/5 * * * * *";
        
        // Every 10 seconds
        public static final String EVERY_10_SECONDS = "*/10 * * * * *";
        
        // Every 30 seconds
        public static final String EVERY_30_SECONDS = "*/30 * * * * *";
        
        // Every minute (at 0 seconds)
        public static final String EVERY_MINUTE = "0 * * * * *";
        
        // Every 5 minutes
        public static final String EVERY_5_MINUTES = "0 */5 * * * *";
        
        // Every 10 minutes
        public static final String EVERY_10_MINUTES = "0 */10 * * * *";
        
        // Every 15 minutes
        public static final String EVERY_15_MINUTES = "0 */15 * * * *";
        
        // Every 30 minutes
        public static final String EVERY_30_MINUTES = "0 */30 * * * *";
        
        // Every hour (at 0 minutes, 0 seconds)
        public static final String EVERY_HOUR = "0 0 * * * *";
        
        // Every 2 hours
        public static final String EVERY_2_HOURS = "0 0 */2 * * *";
        
        // Every day at midnight
        public static final String DAILY_MIDNIGHT = "0 0 0 * * *";
        
        // Every day at noon
        public static final String DAILY_NOON = "0 0 12 * * *";
        
        // Every day at 2 AM
        public static final String DAILY_2AM = "0 0 2 * * *";
        
        // Every weekday at 9 AM
        public static final String WEEKDAYS_9AM = "0 0 9 * * MON-FRI";
        
        // Every weekend at 10 AM
        public static final String WEEKENDS_10AM = "0 0 10 * * SAT,SUN";
        
        // Every Monday at 8 AM
        public static final String MONDAYS_8AM = "0 0 8 * * MON";
        
        // Every Friday at 5 PM
        public static final String FRIDAYS_5PM = "0 0 17 * * FRI";
        
        // First day of month at midnight
        public static final String FIRST_DAY_OF_MONTH = "0 0 0 1 * *";
        
        // Last day of month at midnight
        public static final String LAST_DAY_OF_MONTH = "0 0 0 L * *";
        
        // Every quarter (1st of Jan, Apr, Jul, Oct)
        public static final String QUARTERLY = "0 0 0 1 1,4,7,10 *";
        
        // Every year (January 1st at midnight)
        public static final String YEARLY = "0 0 0 1 1 *";
    }
    
    /**
     * Example 2: Business Hours Cron Expressions
     */
    public static class BusinessHoursCronExpressions {
        
        // Weekdays, every hour from 9 AM to 5 PM
        public static final String BUSINESS_HOURS_HOURLY = "0 0 9-17 * * MON-FRI";
        
        // Weekdays, every 30 minutes from 9 AM to 5 PM
        public static final String BUSINESS_HOURS_HALF_HOURLY = "0 0,30 9-17 * * MON-FRI";
        
        // Weekdays at start of business (9 AM)
        public static final String BUSINESS_START = "0 0 9 * * MON-FRI";
        
        // Weekdays at end of business (5 PM)
        public static final String BUSINESS_END = "0 0 17 * * MON-FRI";
        
        // Weekdays at lunch time (12 PM)
        public static final String LUNCH_TIME = "0 0 12 * * MON-FRI";
        
        // Every weekday at 9 AM, 12 PM, and 5 PM
        public static final String THREE_TIMES_DAILY = "0 0 9,12,17 * * MON-FRI";
    }
    
    /**
     * Example 3: Advanced Cron Expressions with Special Characters
     */
    public static class AdvancedCronExpressions {
        
        // Last Friday of every month at 5 PM
        public static final String LAST_FRIDAY_5PM = "0 0 17 * * 5L";
        
        // First Monday of every month at 9 AM
        public static final String FIRST_MONDAY_9AM = "0 0 9 * * 1#1";
        
        // Second Tuesday of every month at 10 AM
        public static final String SECOND_TUESDAY_10AM = "0 0 10 * * 2#2";
        
        // Third Wednesday of every month at 2 PM
        public static final String THIRD_WEDNESDAY_2PM = "0 0 14 * * 3#3";
        
        // Nearest weekday to the 15th of every month
        public static final String MIDMONTH_WEEKDAY = "0 0 0 15W * *";
        
        // Last weekday of every month
        public static final String LAST_WEEKDAY_OF_MONTH = "0 0 0 LW * *";
        
        // Every 5 minutes during business hours (9 AM - 5 PM, Mon-Fri)
        public static final String BUSINESS_HOURS_5MIN = "0 */5 9-17 * * MON-FRI";
        
        // Specific times: 9 AM, 12 PM, 3 PM, 6 PM every day
        public static final String SPECIFIC_TIMES = "0 0 9,12,15,18 * * *";
        
        // Every 2 hours on weekdays
        public static final String WEEKDAYS_2_HOURS = "0 0 */2 * * MON-FRI";
    }
    
    /**
     * Example 4: Parsing and Validating Cron Expressions
     */
    public static void parseCronExpression(String cronExpression) {
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            System.out.println("Cron expression is valid: " + cronExpression);
            
            // Calculate next execution times
            LocalDateTime now = LocalDateTime.now();
            System.out.println("Current time: " + now.format(formatter));
            
            LocalDateTime next = cron.next(now);
            if (next != null) {
                System.out.println("Next execution: " + next.format(formatter));
                
                LocalDateTime afterNext = cron.next(next);
                if (afterNext != null) {
                    System.out.println("Following execution: " + afterNext.format(formatter));
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid cron expression: " + cronExpression);
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Example 5: Using CronTrigger with Timezone
     */
    public static void demonstrateCronTriggerWithTimezone() {
        // New York timezone
        CronTrigger nyTrigger = new CronTrigger("0 0 9 * * MON-FRI", 
                                                 ZoneId.of("America/New_York"));
        Date nextNY = nyTrigger.nextExecutionTime(null);
        System.out.println("Next execution (NY): " + 
                         LocalDateTime.ofInstant(nextNY.toInstant(), ZoneId.of("America/New_York"))
                                    .format(formatter));
        
        // London timezone
        CronTrigger londonTrigger = new CronTrigger("0 0 9 * * MON-FRI", 
                                                     ZoneId.of("Europe/London"));
        Date nextLondon = londonTrigger.nextExecutionTime(null);
        System.out.println("Next execution (London): " + 
                         LocalDateTime.ofInstant(nextLondon.toInstant(), ZoneId.of("Europe/London"))
                                    .format(formatter));
        
        // Tokyo timezone
        CronTrigger tokyoTrigger = new CronTrigger("0 0 9 * * MON-FRI", 
                                                    ZoneId.of("Asia/Tokyo"));
        Date nextTokyo = tokyoTrigger.nextExecutionTime(null);
        System.out.println("Next execution (Tokyo): " + 
                         LocalDateTime.ofInstant(nextTokyo.toInstant(), ZoneId.of("Asia/Tokyo"))
                                    .format(formatter));
    }
    
    /**
     * Example 6: Testing Cron Expressions
     */
    public static void testCronExpression(String cronExpression, int count) {
        System.out.println("\nTesting cron: " + cronExpression);
        System.out.println("Next " + count + " execution times:");
        
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            LocalDateTime current = LocalDateTime.now();
            
            for (int i = 0; i < count; i++) {
                current = cron.next(current);
                if (current == null) {
                    System.out.println("No more executions");
                    break;
                }
                System.out.println((i + 1) + ". " + current.format(formatter));
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Example 7: Cron Expression Builder (Helper)
     */
    public static class CronBuilder {
        private String second = "*";
        private String minute = "*";
        private String hour = "*";
        private String dayOfMonth = "*";
        private String month = "*";
        private String dayOfWeek = "?";
        
        public CronBuilder everySecond() {
            this.second = "*";
            return this;
        }
        
        public CronBuilder atSecond(int second) {
            this.second = String.valueOf(second);
            return this;
        }
        
        public CronBuilder everyMinute() {
            this.minute = "*";
            return this;
        }
        
        public CronBuilder atMinute(int minute) {
            this.minute = String.valueOf(minute);
            return this;
        }
        
        public CronBuilder everyNMinutes(int n) {
            this.minute = "*/" + n;
            return this;
        }
        
        public CronBuilder everyHour() {
            this.hour = "*";
            return this;
        }
        
        public CronBuilder atHour(int hour) {
            this.hour = String.valueOf(hour);
            return this;
        }
        
        public CronBuilder everyNHours(int n) {
            this.hour = "*/" + n;
            return this;
        }
        
        public CronBuilder hourRange(int start, int end) {
            this.hour = start + "-" + end;
            return this;
        }
        
        public CronBuilder everyDay() {
            this.dayOfMonth = "*";
            this.dayOfWeek = "?";
            return this;
        }
        
        public CronBuilder atDayOfMonth(int day) {
            this.dayOfMonth = String.valueOf(day);
            this.dayOfWeek = "?";
            return this;
        }
        
        public CronBuilder lastDayOfMonth() {
            this.dayOfMonth = "L";
            this.dayOfWeek = "?";
            return this;
        }
        
        public CronBuilder weekdays() {
            this.dayOfMonth = "?";
            this.dayOfWeek = "MON-FRI";
            return this;
        }
        
        public CronBuilder weekends() {
            this.dayOfMonth = "?";
            this.dayOfWeek = "SAT,SUN";
            return this;
        }
        
        public CronBuilder onDaysOfWeek(String... days) {
            this.dayOfMonth = "?";
            this.dayOfWeek = String.join(",", days);
            return this;
        }
        
        public CronBuilder everyMonth() {
            this.month = "*";
            return this;
        }
        
        public CronBuilder inMonths(int... months) {
            this.month = java.util.Arrays.stream(months)
                .mapToObj(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
            return this;
        }
        
        public String build() {
            return String.join(" ", second, minute, hour, dayOfMonth, month, dayOfWeek);
        }
    }
    
    /**
     * Main method with usage examples
     */
    public static void main(String[] args) {
        System.out.println("=== Cron Expression Pattern Examples ===\n");
        
        // Example 1: Parse and validate
        System.out.println("1. Parsing cron expressions:");
        parseCronExpression("0 0 9 * * MON-FRI"); // Weekdays at 9 AM
        parseCronExpression("0 */15 * * * *");    // Every 15 minutes
        System.out.println();
        
        // Example 2: Test execution times
        System.out.println("2. Testing execution times:");
        testCronExpression("0 */5 * * * *", 5);  // Every 5 minutes
        System.out.println();
        
        // Example 3: Timezone demonstration
        System.out.println("3. Timezone-aware scheduling:");
        demonstrateCronTriggerWithTimezone();
        System.out.println();
        
        // Example 4: Using CronBuilder
        System.out.println("4. Using CronBuilder:");
        String cron1 = new CronBuilder()
            .atSecond(0)
            .atMinute(0)
            .atHour(9)
            .weekdays()
            .everyMonth()
            .build();
        System.out.println("Built cron (weekdays 9 AM): " + cron1);
        parseCronExpression(cron1);
        
        String cron2 = new CronBuilder()
            .atSecond(0)
            .everyNMinutes(15)
            .hourRange(9, 17)
            .weekdays()
            .everyMonth()
            .build();
        System.out.println("\nBuilt cron (business hours, every 15 min): " + cron2);
        parseCronExpression(cron2);
        
        String cron3 = new CronBuilder()
            .atSecond(0)
            .atMinute(0)
            .atHour(0)
            .lastDayOfMonth()
            .everyMonth()
            .build();
        System.out.println("\nBuilt cron (last day of month): " + cron3);
        parseCronExpression(cron3);
    }
}

/**
 * Common Cron Patterns Reference:
 * ===============================
 * 
 * Time-based:
 * ----------
 * "* * * * * *"           - Every second
 * "0 * * * * *"           - Every minute
 * "0 0 * * * *"           - Every hour
 * "0 0 0 * * *"           - Every day at midnight
 * "0 0 12 * * *"          - Every day at noon
 * 
 * Interval-based:
 * --------------
 * "*/5 * * * * *"         - Every 5 seconds
 * "0 */10 * * * *"        - Every 10 minutes
 * "0 0 */2 * * *"         - Every 2 hours
 * 
 * Day-based:
 * ---------
 * "0 0 0 * * MON"         - Every Monday at midnight
 * "0 0 0 * * MON-FRI"     - Weekdays at midnight
 * "0 0 0 * * SAT,SUN"     - Weekends at midnight
 * "0 0 9 * * MON-FRI"     - Weekdays at 9 AM
 * 
 * Month-based:
 * -----------
 * "0 0 0 1 * *"           - First day of every month
 * "0 0 0 L * *"           - Last day of every month
 * "0 0 0 15 * *"          - 15th of every month
 * "0 0 0 1 1,4,7,10 *"    - Quarterly (Jan, Apr, Jul, Oct)
 * 
 * Advanced:
 * --------
 * "0 0 9 * * 1#1"         - First Monday of every month
 * "0 0 9 * * 5L"          - Last Friday of every month
 * "0 0 0 15W * *"         - Nearest weekday to 15th
 * "0 0 0 LW * *"          - Last weekday of month
 * 
 * Validation Tips:
 * ===============
 * - Use online cron calculators (crontab.guru)
 * - Test with CronExpression.parse()
 * - Verify next execution times
 * - Test timezone handling
 * - Consider DST transitions
 * - Document complex expressions
 */
