package com.spring.patterns.internationalization;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Timezone Handling Pattern
 * 
 * Demonstrates timezone handling for internationalization:
 * - ZoneId and ZonedDateTime usage
 * - Timezone conversion
 * - UTC normalization
 * - Daylight saving time handling
 * - Timezone-aware scheduling
 * 
 * Use Cases:
 * 1. Global application timezone support
 * 2. User-specific timezone preferences
 * 3. Meeting scheduling across timezones
 * 4. Event timestamp normalization
 * 5. Timezone conversion utilities
 */

/**
 * Timezone Service
 */
class TimezoneService {
    
    private final ZoneId defaultZone;
    
    public TimezoneService(ZoneId defaultZone) {
        this.defaultZone = defaultZone;
    }
    
    /**
     * Get current time in default timezone
     */
    public ZonedDateTime getCurrentTime() {
        return ZonedDateTime.now(defaultZone);
    }
    
    /**
     * Get current time in specific timezone
     */
    public ZonedDateTime getCurrentTime(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId);
    }
    
    /**
     * Convert time to different timezone
     */
    public ZonedDateTime convertToTimezone(ZonedDateTime dateTime, ZoneId targetZone) {
        return dateTime.withZoneSameInstant(targetZone);
    }
    
    /**
     * Convert local datetime to zoned datetime
     */
    public ZonedDateTime toZonedDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.atZone(zoneId);
    }
    
    /**
     * Get timezone offset
     */
    public ZoneOffset getOffset(ZoneId zoneId, Instant instant) {
        return zoneId.getRules().getOffset(instant);
    }
}

/**
 * Timezone Converter
 */
class TimezoneConverter {
    
    /**
     * Convert between timezones
     */
    public ZonedDateTime convert(ZonedDateTime source, ZoneId targetZone) {
        return source.withZoneSameInstant(targetZone);
    }
    
    /**
     * Convert to UTC
     */
    public ZonedDateTime toUTC(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
    }
    
    /**
     * Convert from UTC
     */
    public ZonedDateTime fromUTC(ZonedDateTime utcDateTime, ZoneId targetZone) {
        return utcDateTime.withZoneSameInstant(targetZone);
    }
    
    /**
     * Batch convert times to multiple timezones
     */
    public Map<ZoneId, ZonedDateTime> convertToMultiple(ZonedDateTime source, 
                                                        List<ZoneId> targetZones) {
        Map<ZoneId, ZonedDateTime> results = new LinkedHashMap<>();
        for (ZoneId zone : targetZones) {
            results.put(zone, convert(source, zone));
        }
        return results;
    }
}

/**
 * Timezone Information Provider
 */
class TimezoneInformationProvider {
    
    /**
     * Get all available timezone IDs
     */
    public Set<String> getAllTimezoneIds() {
        return ZoneId.getAvailableZoneIds();
    }
    
    /**
     * Get timezone info
     */
    public TimezoneInfo getTimezoneInfo(ZoneId zoneId) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZoneOffset offset = now.getOffset();
        
        return new TimezoneInfo(
            zoneId.getId(),
            zoneId.getDisplayName(java.time.format.TextStyle.FULL, Locale.US),
            offset.getTotalSeconds() / 3600.0,
            zoneId.getRules().isDaylightSavings(Instant.now()),
            now
        );
    }
    
    /**
     * Get common timezones
     */
    public List<ZoneId> getCommonTimezones() {
        return Arrays.asList(
            ZoneId.of("America/New_York"),
            ZoneId.of("America/Chicago"),
            ZoneId.of("America/Denver"),
            ZoneId.of("America/Los_Angeles"),
            ZoneId.of("Europe/London"),
            ZoneId.of("Europe/Paris"),
            ZoneId.of("Europe/Berlin"),
            ZoneId.of("Asia/Tokyo"),
            ZoneId.of("Asia/Shanghai"),
            ZoneId.of("Asia/Dubai"),
            ZoneId.of("Australia/Sydney"),
            ZoneOffset.UTC
        );
    }
}

record TimezoneInfo(
    String id,
    String displayName,
    double offsetHours,
    boolean isDaylightSaving,
    ZonedDateTime currentTime
) {}

/**
 * Meeting Scheduler with timezone support
 */
class MeetingScheduler {
    
    /**
     * Schedule meeting across timezones
     */
    public MeetingSchedule scheduleMeeting(LocalDateTime hostTime, ZoneId hostZone,
                                          List<ZoneId> participantZones) {
        ZonedDateTime hostZonedTime = hostTime.atZone(hostZone);
        Map<ZoneId, ZonedDateTime> participantTimes = new LinkedHashMap<>();
        
        for (ZoneId zone : participantZones) {
            participantTimes.put(zone, hostZonedTime.withZoneSameInstant(zone));
        }
        
        return new MeetingSchedule(hostZonedTime, participantTimes);
    }
    
    /**
     * Find best meeting time across timezones
     */
    public ZonedDateTime findBestTime(List<ZoneId> participantZones, 
                                     int preferredHourStart, 
                                     int preferredHourEnd) {
        // Find time that falls within business hours for all participants
        ZonedDateTime candidate = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
        
        for (int hour = 0; hour < 24; hour++) {
            ZonedDateTime testTime = candidate.withHour(hour);
            boolean acceptable = true;
            
            for (ZoneId zone : participantZones) {
                ZonedDateTime localTime = testTime.withZoneSameInstant(zone);
                int localHour = localTime.getHour();
                
                if (localHour < preferredHourStart || localHour >= preferredHourEnd) {
                    acceptable = false;
                    break;
                }
            }
            
            if (acceptable) {
                return testTime;
            }
        }
        
        return null; // No suitable time found
    }
}

record MeetingSchedule(
    ZonedDateTime hostTime,
    Map<ZoneId, ZonedDateTime> participantTimes
) {}

/**
 * Timezone Offset Calculator
 */
class TimezoneOffsetCalculator {
    
    /**
     * Calculate offset between two timezones
     */
    public Duration calculateOffset(ZoneId zone1, ZoneId zone2) {
        Instant now = Instant.now();
        ZoneOffset offset1 = zone1.getRules().getOffset(now);
        ZoneOffset offset2 = zone2.getRules().getOffset(now);
        
        return Duration.ofSeconds(offset2.getTotalSeconds() - offset1.getTotalSeconds());
    }
    
    /**
     * Get time difference in hours
     */
    public double getTimeDifferenceHours(ZoneId zone1, ZoneId zone2) {
        Duration offset = calculateOffset(zone1, zone2);
        return offset.toMinutes() / 60.0;
    }
}

/**
 * Daylight Saving Time Handler
 */
class DaylightSavingTimeHandler {
    
    /**
     * Check if timezone observes DST
     */
    public boolean observesDST(ZoneId zoneId) {
        return !zoneId.getRules().getTransitionRules().isEmpty();
    }
    
    /**
     * Check if currently in DST
     */
    public boolean isInDST(ZoneId zoneId) {
        return zoneId.getRules().isDaylightSavings(Instant.now());
    }
    
    /**
     * Get next DST transition
     */
    public ZoneOffsetTransition getNextTransition(ZoneId zoneId) {
        return zoneId.getRules().nextTransition(Instant.now());
    }
    
    /**
     * Get DST transition info
     */
    public DSTTransitionInfo getTransitionInfo(ZoneOffsetTransition transition) {
        if (transition == null) {
            return null;
        }
        
        return new DSTTransitionInfo(
            transition.getDateTimeBefore().toString(),
            transition.getDateTimeAfter().toString(),
            transition.getOffsetBefore(),
            transition.getOffsetAfter(),
            transition.getDuration()
        );
    }
}

record DSTTransitionInfo(
    String before,
    String after,
    ZoneOffset offsetBefore,
    ZoneOffset offsetAfter,
    Duration gap
) {}

/**
 * UTC Normalizer
 */
class UTCNormalizer {
    
    /**
     * Normalize to UTC
     */
    public Instant normalize(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant();
    }
    
    /**
     * Normalize multiple times to UTC
     */
    public List<Instant> normalizeAll(List<ZonedDateTime> zonedDateTimes) {
        return zonedDateTimes.stream()
            .map(ZonedDateTime::toInstant)
            .toList();
    }
    
    /**
     * Store time in UTC, retrieve in user timezone
     */
    public ZonedDateTime retrieveInUserTimezone(Instant utcTime, ZoneId userZone) {
        return utcTime.atZone(userZone);
    }
}

/**
 * Timezone-aware Event Manager
 */
class TimezoneAwareEventManager {
    
    private final Map<String, Event> events = new HashMap<>();
    
    /**
     * Create event with timezone
     */
    public void createEvent(String id, String title, ZonedDateTime eventTime) {
        Instant utcTime = eventTime.toInstant();
        events.put(id, new Event(id, title, utcTime));
    }
    
    /**
     * Get event in user timezone
     */
    public ZonedDateTime getEventTime(String id, ZoneId userTimezone) {
        Event event = events.get(id);
        if (event == null) {
            return null;
        }
        return event.time().atZone(userTimezone);
    }
    
    /**
     * Get all events in user timezone
     */
    public List<EventView> getAllEventsForUser(ZoneId userTimezone) {
        return events.values().stream()
            .map(e -> new EventView(
                e.id(),
                e.title(),
                e.time().atZone(userTimezone)
            ))
            .toList();
    }
}

record Event(String id, String title, Instant time) {}
record EventView(String id, String title, ZonedDateTime localTime) {}

/**
 * Demonstration class
 */
public class TimezoneHandlingPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Timezone Handling Pattern Demo ===\n");
        
        // 1. Timezone Service Demo
        demonstrateTimezoneService();
        
        // 2. Timezone Conversion Demo
        demonstrateTimezoneConversion();
        
        // 3. Timezone Information Demo
        demonstrateTimezoneInformation();
        
        // 4. Meeting Scheduling Demo
        demonstrateMeetingScheduling();
        
        // 5. Daylight Saving Time Demo
        demonstrateDaylightSavingTime();
        
        // 6. UTC Normalization Demo
        demonstrateUTCNormalization();
        
        // 7. Timezone-aware Events Demo
        demonstrateTimezoneAwareEvents();
    }
    
    private static void demonstrateTimezoneService() {
        System.out.println("1. Timezone Service Demo:");
        
        TimezoneService service = new TimezoneService(ZoneId.of("America/New_York"));
        
        System.out.println("Current time (New York): " + service.getCurrentTime());
        System.out.println("Current time (Tokyo): " + 
            service.getCurrentTime(ZoneId.of("Asia/Tokyo")));
        System.out.println("Current time (London): " + 
            service.getCurrentTime(ZoneId.of("Europe/London")));
        System.out.println();
    }
    
    private static void demonstrateTimezoneConversion() {
        System.out.println("2. Timezone Conversion Demo:");
        
        TimezoneConverter converter = new TimezoneConverter();
        ZonedDateTime nyTime = ZonedDateTime.now(ZoneId.of("America/New_York"));
        
        System.out.println("New York Time: " + nyTime);
        System.out.println("Converted to Tokyo: " + 
            converter.convert(nyTime, ZoneId.of("Asia/Tokyo")));
        System.out.println("Converted to London: " + 
            converter.convert(nyTime, ZoneId.of("Europe/London")));
        System.out.println("Converted to UTC: " + converter.toUTC(nyTime));
        System.out.println();
    }
    
    private static void demonstrateTimezoneInformation() {
        System.out.println("3. Timezone Information Demo:");
        
        TimezoneInformationProvider provider = new TimezoneInformationProvider();
        List<ZoneId> commonZones = provider.getCommonTimezones();
        
        System.out.println("Common Timezones:");
        for (ZoneId zone : commonZones.subList(0, 5)) {
            TimezoneInfo info = provider.getTimezoneInfo(zone);
            System.out.printf("  %s: UTC%+.1f, DST: %b%n", 
                info.displayName(), info.offsetHours(), info.isDaylightSaving());
        }
        System.out.println();
    }
    
    private static void demonstrateMeetingScheduling() {
        System.out.println("4. Meeting Scheduling Demo:");
        
        MeetingScheduler scheduler = new MeetingScheduler();
        LocalDateTime hostTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        ZoneId hostZone = ZoneId.of("America/New_York");
        
        List<ZoneId> participantZones = Arrays.asList(
            ZoneId.of("Europe/London"),
            ZoneId.of("Asia/Tokyo"),
            ZoneId.of("Australia/Sydney")
        );
        
        MeetingSchedule schedule = scheduler.scheduleMeeting(hostTime, hostZone, participantZones);
        
        System.out.println("Meeting scheduled for:");
        System.out.println("  Host (New York): " + 
            schedule.hostTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")));
        
        schedule.participantTimes().forEach((zone, time) ->
            System.out.println("  Participant (" + zone.getId() + "): " +
                time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")))
        );
        System.out.println();
    }
    
    private static void demonstrateDaylightSavingTime() {
        System.out.println("5. Daylight Saving Time Demo:");
        
        DaylightSavingTimeHandler handler = new DaylightSavingTimeHandler();
        ZoneId nyZone = ZoneId.of("America/New_York");
        
        System.out.println("New York DST Status:");
        System.out.println("  Observes DST: " + handler.observesDST(nyZone));
        System.out.println("  Currently in DST: " + handler.isInDST(nyZone));
        
        ZoneOffsetTransition nextTransition = handler.getNextTransition(nyZone);
        if (nextTransition != null) {
            DSTTransitionInfo info = handler.getTransitionInfo(nextTransition);
            System.out.println("  Next transition: " + info.before() + " → " + info.after());
        }
        System.out.println();
    }
    
    private static void demonstrateUTCNormalization() {
        System.out.println("6. UTC Normalization Demo:");
        
        UTCNormalizer normalizer = new UTCNormalizer();
        
        List<ZonedDateTime> times = Arrays.asList(
            ZonedDateTime.now(ZoneId.of("America/New_York")),
            ZonedDateTime.now(ZoneId.of("Europe/London")),
            ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))
        );
        
        System.out.println("Original times:");
        times.forEach(time -> System.out.println("  " + time));
        
        System.out.println("\nNormalized to UTC:");
        List<Instant> normalized = normalizer.normalizeAll(times);
        normalized.forEach(instant -> System.out.println("  " + instant));
        
        System.out.println("\nRetrieved in Paris timezone:");
        ZoneId parisZone = ZoneId.of("Europe/Paris");
        normalized.forEach(instant -> 
            System.out.println("  " + normalizer.retrieveInUserTimezone(instant, parisZone)));
        System.out.println();
    }
    
    private static void demonstrateTimezoneAwareEvents() {
        System.out.println("7. Timezone-aware Events Demo:");
        
        TimezoneAwareEventManager manager = new TimezoneAwareEventManager();
        
        // Create events in different timezones
        manager.createEvent("evt1", "Morning Meeting", 
            ZonedDateTime.now(ZoneId.of("America/New_York")).plusDays(1).withHour(9));
        manager.createEvent("evt2", "Afternoon Workshop", 
            ZonedDateTime.now(ZoneId.of("Europe/London")).plusDays(1).withHour(14));
        
        // View events in Tokyo timezone
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        System.out.println("Events in Tokyo timezone:");
        manager.getAllEventsForUser(tokyoZone).forEach(event ->
            System.out.println("  " + event.title() + ": " + 
                event.localTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")))
        );
    }
}
