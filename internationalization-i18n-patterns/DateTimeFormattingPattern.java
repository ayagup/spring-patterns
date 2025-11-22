package com.spring.patterns.internationalization;

import java.time.*;
import java.time.format.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * Date/Time Formatting Pattern
 * 
 * Demonstrates date/time formatting for internationalization:
 * - Locale-specific date/time formatting
 * - Custom format patterns
 * - DateTimeFormatter usage
 * - Multiple formatting styles
 * - Localized month/day names
 * 
 * Use Cases:
 * 1. Display dates in user's locale
 * 2. Format timestamps for logging
 * 3. Generate reports with localized dates
 * 4. Calendar applications
 * 5. Event scheduling display
 */

/**
 * Date Time Formatter Service
 */
class DateTimeFormatterService {
    
    private final Locale locale;
    
    public DateTimeFormatterService(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Format with predefined style
     */
    public String formatShort(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(locale);
        return formatter.format(dateTime);
    }
    
    public String formatMedium(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(locale);
        return formatter.format(dateTime);
    }
    
    public String formatLong(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
            .withLocale(locale);
        return formatter.format(dateTime);
    }
    
    public String formatFull(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
            .withLocale(locale);
        return formatter.format(dateTime);
    }
    
    /**
     * Format date only
     */
    public String formatDate(LocalDate date, FormatStyle style) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(style)
            .withLocale(locale);
        return formatter.format(date);
    }
    
    /**
     * Format time only
     */
    public String formatTime(LocalTime time, FormatStyle style) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(style)
            .withLocale(locale);
        return formatter.format(time);
    }
    
    /**
     * Format with custom pattern
     */
    public String formatWithPattern(TemporalAccessor temporal, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
        return formatter.format(temporal);
    }
}

/**
 * Custom Date Time Format Builder
 */
class CustomDateTimeFormatBuilder {
    
    private final Locale locale;
    
    public CustomDateTimeFormatBuilder(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Build ISO 8601 formatter
     */
    public DateTimeFormatter buildISO8601() {
        return DateTimeFormatter.ISO_DATE_TIME.withLocale(locale);
    }
    
    /**
     * Build custom pattern formatter
     */
    public DateTimeFormatter buildCustom(String pattern) {
        return DateTimeFormatter.ofPattern(pattern, locale);
    }
    
    /**
     * Build localized formatter
     */
    public DateTimeFormatter buildLocalized(FormatStyle dateStyle, FormatStyle timeStyle) {
        return DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle)
            .withLocale(locale);
    }
    
    /**
     * Build formatter with resolver style
     */
    public DateTimeFormatter buildWithResolverStyle(String pattern, ResolverStyle style) {
        return DateTimeFormatter.ofPattern(pattern, locale)
            .withResolverStyle(style);
    }
}

/**
 * Common Date Format Patterns
 */
class CommonDateFormatPatterns {
    
    public static final String ISO_DATE = "yyyy-MM-dd";
    public static final String US_DATE = "MM/dd/yyyy";
    public static final String EU_DATE = "dd/MM/yyyy";
    public static final String ISO_DATETIME = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
    public static final String LONG_DATE = "MMMM d, yyyy";
    public static final String SHORT_DATE = "M/d/yy";
    public static final String TIME_24H = "HH:mm:ss";
    public static final String TIME_12H = "hh:mm:ss a";
    public static final String MONTH_YEAR = "MMMM yyyy";
    public static final String YEAR_MONTH = "yyyy-MM";
    public static final String DAY_OF_WEEK = "EEEE";
    public static final String FULL_DATETIME = "EEEE, MMMM d, yyyy 'at' h:mm a";
    
    /**
     * Get formatter for pattern
     */
    public static DateTimeFormatter getFormatter(String pattern, Locale locale) {
        return DateTimeFormatter.ofPattern(pattern, locale);
    }
}

/**
 * Localized Date Components Provider
 */
class LocalizedDateComponentsProvider {
    
    private final Locale locale;
    
    public LocalizedDateComponentsProvider(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Get localized month names
     */
    public List<String> getMonthNames(TextStyle style) {
        List<String> months = new ArrayList<>();
        for (Month month : Month.values()) {
            months.add(month.getDisplayName(style, locale));
        }
        return months;
    }
    
    /**
     * Get localized day of week names
     */
    public List<String> getDayOfWeekNames(TextStyle style) {
        List<String> days = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            days.add(day.getDisplayName(style, locale));
        }
        return days;
    }
    
    /**
     * Get AM/PM markers
     */
    public List<String> getAmPmStrings() {
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        return Arrays.asList(symbols.getAmPmStrings());
    }
}

/**
 * Relative Date Formatter
 */
class RelativeDateFormatter {
    
    private final Locale locale;
    
    public RelativeDateFormatter(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Format relative to now
     */
    public String formatRelative(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(dateTime, now);
        
        if (days == 0) {
            return "Today";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days == -1) {
            return "Tomorrow";
        } else if (days > 1 && days < 7) {
            return days + " days ago";
        } else if (days < -1 && days > -7) {
            return Math.abs(days) + " days from now";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale);
            return formatter.format(dateTime.toLocalDate());
        }
    }
    
    /**
     * Format time ago
     */
    public String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        
        long seconds = ChronoUnit.SECONDS.between(dateTime, now);
        if (seconds < 60) return "Just now";
        
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 60) return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 30) return days + " day" + (days > 1 ? "s" : "") + " ago";
        
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months < 12) return months + " month" + (months > 1 ? "s" : "") + " ago";
        
        long years = ChronoUnit.YEARS.between(dateTime, now);
        return years + " year" + (years > 1 ? "s" : "") + " ago";
    }
}

/**
 * Date Range Formatter
 */
class DateRangeFormatter {
    
    private final Locale locale;
    
    public DateRangeFormatter(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Format date range
     */
    public String formatRange(LocalDate start, LocalDate end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale);
        
        if (start.getYear() == end.getYear()) {
            if (start.getMonth() == end.getMonth()) {
                return String.format("%s %d - %d, %d",
                    start.getMonth().getDisplayName(TextStyle.FULL, locale),
                    start.getDayOfMonth(),
                    end.getDayOfMonth(),
                    start.getYear());
            } else {
                return String.format("%s - %s, %d",
                    start.format(DateTimeFormatter.ofPattern("MMMM d", locale)),
                    end.format(DateTimeFormatter.ofPattern("MMMM d", locale)),
                    start.getYear());
            }
        } else {
            return formatter.format(start) + " - " + formatter.format(end);
        }
    }
    
    /**
     * Format duration
     */
    public String formatDuration(LocalDateTime start, LocalDateTime end) {
        long hours = ChronoUnit.HOURS.between(start, end);
        long minutes = ChronoUnit.MINUTES.between(start, end) % 60;
        
        if (hours > 0) {
            return String.format("%d hour%s %d minute%s",
                hours, hours > 1 ? "s" : "",
                minutes, minutes != 1 ? "s" : "");
        } else {
            return String.format("%d minute%s", minutes, minutes != 1 ? "s" : "");
        }
    }
}

/**
 * Calendar Formatter
 */
class CalendarFormatter {
    
    private final Locale locale;
    
    public CalendarFormatter(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Format calendar header
     */
    public String formatCalendarHeader(YearMonth yearMonth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale);
        return formatter.format(yearMonth);
    }
    
    /**
     * Get first day of week
     */
    public DayOfWeek getFirstDayOfWeek() {
        // Default implementation - could be locale-specific
        return DayOfWeek.SUNDAY;
    }
    
    /**
     * Format day name for calendar
     */
    public String formatDayName(DayOfWeek day, TextStyle style) {
        return day.getDisplayName(style, locale);
    }
}

/**
 * Timestamp Formatter
 */
class TimestampFormatter {
    
    private final Locale locale;
    private final ZoneId zoneId;
    
    public TimestampFormatter(Locale locale, ZoneId zoneId) {
        this.locale = locale;
        this.zoneId = zoneId;
    }
    
    /**
     * Format instant to string
     */
    public String format(Instant instant) {
        ZonedDateTime zdt = instant.atZone(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(locale);
        return formatter.format(zdt);
    }
    
    /**
     * Format with timezone
     */
    public String formatWithTimezone(Instant instant) {
        ZonedDateTime zdt = instant.atZone(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z", locale);
        return formatter.format(zdt);
    }
    
    /**
     * Format for logging
     */
    public String formatForLogging(Instant instant) {
        ZonedDateTime zdt = instant.atZone(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", locale);
        return formatter.format(zdt);
    }
}

/**
 * Demonstration class
 */
public class DateTimeFormattingPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Date/Time Formatting Pattern Demo ===\n");
        
        // 1. Basic Date/Time Formatting
        demonstrateBasicFormatting();
        
        // 2. Custom Pattern Formatting
        demonstrateCustomPatterns();
        
        // 3. Localized Components
        demonstrateLocalizedComponents();
        
        // 4. Relative Date Formatting
        demonstrateRelativeDates();
        
        // 5. Date Range Formatting
        demonstrateDateRanges();
        
        // 6. Calendar Formatting
        demonstrateCalendarFormatting();
        
        // 7. Timestamp Formatting
        demonstrateTimestampFormatting();
    }
    
    private static void demonstrateBasicFormatting() {
        System.out.println("1. Basic Date/Time Formatting:");
        
        LocalDateTime now = LocalDateTime.now();
        
        Locale[] locales = {Locale.US, Locale.UK, Locale.GERMANY, Locale.FRANCE, Locale.JAPAN};
        
        for (Locale locale : locales) {
            DateTimeFormatterService service = new DateTimeFormatterService(locale);
            System.out.println(locale.getDisplayCountry() + ":");
            System.out.println("  Short: " + service.formatShort(now));
            System.out.println("  Medium: " + service.formatMedium(now));
            System.out.println();
        }
    }
    
    private static void demonstrateCustomPatterns() {
        System.out.println("2. Custom Pattern Formatting:");
        
        LocalDateTime now = LocalDateTime.now();
        Locale locale = Locale.US;
        
        String[] patterns = {
            CommonDateFormatPatterns.ISO_DATE,
            CommonDateFormatPatterns.US_DATE,
            CommonDateFormatPatterns.TIMESTAMP,
            CommonDateFormatPatterns.LONG_DATE,
            CommonDateFormatPatterns.FULL_DATETIME
        };
        
        for (String pattern : patterns) {
            DateTimeFormatter formatter = CommonDateFormatPatterns.getFormatter(pattern, locale);
            try {
                System.out.println(pattern + ": " + formatter.format(now));
            } catch (Exception e) {
                System.out.println(pattern + ": " + formatter.format(now.toLocalDate()));
            }
        }
        System.out.println();
    }
    
    private static void demonstrateLocalizedComponents() {
        System.out.println("3. Localized Components:");
        
        Locale[] locales = {Locale.US, Locale.FRENCH, Locale.GERMAN};
        
        for (Locale locale : locales) {
            LocalizedDateComponentsProvider provider = new LocalizedDateComponentsProvider(locale);
            System.out.println(locale.getDisplayLanguage() + ":");
            
            List<String> months = provider.getMonthNames(TextStyle.FULL);
            System.out.println("  Months: " + String.join(", ", months.subList(0, 3)) + "...");
            
            List<String> days = provider.getDayOfWeekNames(TextStyle.SHORT);
            System.out.println("  Days: " + String.join(", ", days));
            System.out.println();
        }
    }
    
    private static void demonstrateRelativeDates() {
        System.out.println("4. Relative Date Formatting:");
        
        RelativeDateFormatter formatter = new RelativeDateFormatter(Locale.US);
        LocalDateTime now = LocalDateTime.now();
        
        LocalDateTime[] dates = {
            now,
            now.minusDays(1),
            now.plusDays(1),
            now.minusDays(3),
            now.minusHours(2),
            now.minusMinutes(30),
            now.minusDays(100)
        };
        
        for (LocalDateTime date : dates) {
            System.out.println(formatter.formatRelative(date) + 
                " (" + formatter.formatTimeAgo(date) + ")");
        }
        System.out.println();
    }
    
    private static void demonstrateDateRanges() {
        System.out.println("5. Date Range Formatting:");
        
        DateRangeFormatter formatter = new DateRangeFormatter(Locale.US);
        LocalDate today = LocalDate.now();
        
        // Same month range
        System.out.println("Same month: " + 
            formatter.formatRange(today, today.plusDays(7)));
        
        // Different month, same year
        System.out.println("Different months: " + 
            formatter.formatRange(today, today.plusMonths(2)));
        
        // Different years
        System.out.println("Different years: " + 
            formatter.formatRange(today, today.plusYears(1)));
        
        // Duration
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2).plusMinutes(30);
        System.out.println("Duration: " + formatter.formatDuration(start, end));
        System.out.println();
    }
    
    private static void demonstrateCalendarFormatting() {
        System.out.println("6. Calendar Formatting:");
        
        CalendarFormatter formatter = new CalendarFormatter(Locale.US);
        YearMonth current = YearMonth.now();
        
        System.out.println("Calendar Header: " + formatter.formatCalendarHeader(current));
        System.out.println("First Day of Week: " + formatter.getFirstDayOfWeek());
        
        System.out.print("Day Names: ");
        for (DayOfWeek day : DayOfWeek.values()) {
            System.out.print(formatter.formatDayName(day, TextStyle.SHORT) + " ");
        }
        System.out.println("\n");
    }
    
    private static void demonstrateTimestampFormatting() {
        System.out.println("7. Timestamp Formatting:");
        
        TimestampFormatter formatter = new TimestampFormatter(Locale.US, ZoneId.systemDefault());
        Instant now = Instant.now();
        
        System.out.println("Standard: " + formatter.format(now));
        System.out.println("With Timezone: " + formatter.formatWithTimezone(now));
        System.out.println("For Logging: " + formatter.formatForLogging(now));
    }
}
