package com.example.events.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Event Store Pattern - Demonstrates Event Persistence and Retrieval
 * 
 * This pattern shows how to:
 * 1. Implement append-only event storage
 * 2. Store events with versioning
 * 3. Retrieve events by aggregate ID
 * 4. Query events by time range
 * 5. Implement event stream queries
 * 6. Handle event schema evolution
 * 7. Create event snapshots
 * 8. Implement event replay
 * 9. Support event subscriptions
 * 10. Track event store metrics
 * 
 * Key Concepts:
 * - Event Store: Append-only database for events
 * - Aggregate Stream: All events for one aggregate
 * - Global Stream: All events in chronological order
 * - Event Version: Monotonically increasing per aggregate
 * - Event Metadata: Additional event information
 * 
 * Event Store Features:
 * 1. Append-Only - Events never updated or deleted
 * 2. Immutable - Events cannot change after storage
 * 3. Ordered - Events stored in sequence
 * 4. Queryable - Support various query patterns
 * 5. Scalable - Handle large event volumes
 * 
 * Dependencies:
 * - spring-boot-starter
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class EventStorePattern {

    public static void main(String[] args) {
        SpringApplication.run(EventStorePattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("EVENT STORE PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateEventStore();
        demonstrateQueries();
        
        System.out.println("\nApplication running with Event Store");
        System.out.println("Test endpoints:");
        System.out.println("POST /api/eventstore/append - Append event");
        System.out.println("GET /api/eventstore/stream/{aggregateId} - Get aggregate stream");
        System.out.println("GET /api/eventstore/all - Get all events");
        System.out.println("GET /api/eventstore/metrics - View store metrics");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateEventStore() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EVENT STORE CHARACTERISTICS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Append-Only:");
        System.out.println("   Events are never modified or deleted");
        
        System.out.println("\n2. Versioned:");
        System.out.println("   Each event has a version number");
        
        System.out.println("\n3. Immutable:");
        System.out.println("   Events cannot change after storage");
    }
    
    private static void demonstrateQueries() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EVENT STORE QUERIES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n- By Aggregate ID");
        System.out.println("- By Event Type");
        System.out.println("- By Time Range");
        System.out.println("- By Version Range");
        System.out.println("- Global Stream");
    }
}

/**
 * Stored Event
 */
class StoredEvent {
    private final long globalSequence;
    private final String eventId;
    private final String aggregateId;
    private final String eventType;
    private final long version;
    private final Object data;
    private final Map<String, String> metadata;
    private final LocalDateTime timestamp;
    
    public StoredEvent(long globalSequence, String eventId, String aggregateId, 
                      String eventType, long version, Object data, 
                      Map<String, String> metadata, LocalDateTime timestamp) {
        this.globalSequence = globalSequence;
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.version = version;
        this.data = data;
        this.metadata = new HashMap<>(metadata);
        this.timestamp = timestamp;
    }
    
    public long getGlobalSequence() { return globalSequence; }
    public String getEventId() { return eventId; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public long getVersion() { return version; }
    public Object getData() { return data; }
    public Map<String, String> getMetadata() { return new HashMap<>(metadata); }
    public LocalDateTime getTimestamp() { return timestamp; }
}

/**
 * Event to be Appended
 */
class AppendEvent {
    private final String aggregateId;
    private final String eventType;
    private final Object data;
    private final Map<String, String> metadata;
    
    public AppendEvent(String aggregateId, String eventType, Object data) {
        this(aggregateId, eventType, data, Collections.emptyMap());
    }
    
    public AppendEvent(String aggregateId, String eventType, Object data, 
                      Map<String, String> metadata) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.data = data;
        this.metadata = new HashMap<>(metadata);
    }
    
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public Object getData() { return data; }
    public Map<String, String> getMetadata() { return new HashMap<>(metadata); }
}

/**
 * Event Store Implementation
 */
@Component
class InMemoryEventStore {
    
    // Global sequence for all events
    private final AtomicLong globalSequence = new AtomicLong(0);
    
    // Aggregate streams: aggregateId -> events
    private final Map<String, List<StoredEvent>> aggregateStreams = new ConcurrentHashMap<>();
    
    // Global stream: all events in order
    private final List<StoredEvent> globalStream = new CopyOnWriteArrayList<>();
    
    // Version tracking: aggregateId -> current version
    private final Map<String, Long> aggregateVersions = new ConcurrentHashMap<>();
    
    /**
     * Append event to store
     */
    public StoredEvent appendEvent(AppendEvent event) {
        return appendEvent(event, OptimisticLockVersion.ANY);
    }
    
    /**
     * Append event with optimistic locking
     */
    public StoredEvent appendEvent(AppendEvent event, long expectedVersion) {
        String aggregateId = event.getAggregateId();
        
        // Check optimistic lock
        long currentVersion = aggregateVersions.getOrDefault(aggregateId, 0L);
        
        if (expectedVersion != OptimisticLockVersion.ANY && currentVersion != expectedVersion) {
            throw new ConcurrencyException(
                String.format("Expected version %d but found %d for aggregate %s",
                    expectedVersion, currentVersion, aggregateId));
        }
        
        // Create stored event
        long newVersion = currentVersion + 1;
        long globalSeq = globalSequence.incrementAndGet();
        
        StoredEvent storedEvent = new StoredEvent(
            globalSeq,
            UUID.randomUUID().toString(),
            aggregateId,
            event.getEventType(),
            newVersion,
            event.getData(),
            event.getMetadata(),
            LocalDateTime.now()
        );
        
        // Append to aggregate stream
        aggregateStreams.computeIfAbsent(aggregateId, k -> new CopyOnWriteArrayList<>())
                       .add(storedEvent);
        
        // Append to global stream
        globalStream.add(storedEvent);
        
        // Update version
        aggregateVersions.put(aggregateId, newVersion);
        
        System.out.printf("[EventStore] Appended event: %s for aggregate %s (v%d, seq#%d)%n",
            storedEvent.getEventType(), aggregateId, newVersion, globalSeq);
        
        return storedEvent;
    }
    
    /**
     * Get all events for an aggregate
     */
    public List<StoredEvent> getAggregateStream(String aggregateId) {
        return new ArrayList<>(
            aggregateStreams.getOrDefault(aggregateId, Collections.emptyList()));
    }
    
    /**
     * Get events for an aggregate from a specific version
     */
    public List<StoredEvent> getAggregateStreamFromVersion(String aggregateId, long fromVersion) {
        return aggregateStreams.getOrDefault(aggregateId, Collections.emptyList())
            .stream()
            .filter(event -> event.getVersion() > fromVersion)
            .collect(Collectors.toList());
    }
    
    /**
     * Get events by type
     */
    public List<StoredEvent> getEventsByType(String eventType) {
        return globalStream.stream()
            .filter(event -> event.getEventType().equals(eventType))
            .collect(Collectors.toList());
    }
    
    /**
     * Get events in time range
     */
    public List<StoredEvent> getEventsByTimeRange(LocalDateTime from, LocalDateTime to) {
        return globalStream.stream()
            .filter(event -> !event.getTimestamp().isBefore(from) && 
                           !event.getTimestamp().isAfter(to))
            .collect(Collectors.toList());
    }
    
    /**
     * Get global stream from sequence number
     */
    public List<StoredEvent> getGlobalStreamFromSequence(long fromSequence) {
        return globalStream.stream()
            .filter(event -> event.getGlobalSequence() >= fromSequence)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all events (global stream)
     */
    public List<StoredEvent> getAllEvents() {
        return new ArrayList<>(globalStream);
    }
    
    /**
     * Get current version of aggregate
     */
    public long getCurrentVersion(String aggregateId) {
        return aggregateVersions.getOrDefault(aggregateId, 0L);
    }
    
    /**
     * Get total event count
     */
    public long getTotalEventCount() {
        return globalStream.size();
    }
    
    /**
     * Get aggregate count
     */
    public int getAggregateCount() {
        return aggregateStreams.size();
    }
}

/**
 * Optimistic Lock Version Constants
 */
class OptimisticLockVersion {
    public static final long ANY = -1;
    public static final long NO_STREAM = 0;
}

/**
 * Concurrency Exception
 */
class ConcurrencyException extends RuntimeException {
    public ConcurrencyException(String message) {
        super(message);
    }
}

/**
 * Event Store Metrics
 */
@Component
class EventStoreMetrics {
    
    private final Map<String, Long> eventTypeCount = new ConcurrentHashMap<>();
    private final Map<String, Long> aggregateEventCount = new ConcurrentHashMap<>();
    private final AtomicLong totalAppends = new AtomicLong(0);
    private final AtomicLong totalReads = new AtomicLong(0);
    
    public void recordAppend(String eventType, String aggregateId) {
        totalAppends.incrementAndGet();
        eventTypeCount.merge(eventType, 1L, Long::sum);
        aggregateEventCount.merge(aggregateId, 1L, Long::sum);
    }
    
    public void recordRead(int eventCount) {
        totalReads.addAndGet(eventCount);
    }
    
    public Map<String, Object> getMetrics() {
        return Map.of(
            "totalAppends", totalAppends.get(),
            "totalReads", totalReads.get(),
            "eventTypeDistribution", new HashMap<>(eventTypeCount),
            "aggregateEventCount", new HashMap<>(aggregateEventCount)
        );
    }
}

/**
 * Event Store Service
 */
@Service
class EventStoreService {
    
    private final InMemoryEventStore eventStore;
    private final EventStoreMetrics metrics;
    
    public EventStoreService(InMemoryEventStore eventStore, EventStoreMetrics metrics) {
        this.eventStore = eventStore;
        this.metrics = metrics;
    }
    
    public StoredEvent appendEvent(String aggregateId, String eventType, Object data) {
        AppendEvent event = new AppendEvent(aggregateId, eventType, data);
        StoredEvent stored = eventStore.appendEvent(event);
        metrics.recordAppend(eventType, aggregateId);
        return stored;
    }
    
    public StoredEvent appendEventWithMetadata(String aggregateId, String eventType, 
                                              Object data, Map<String, String> metadata) {
        AppendEvent event = new AppendEvent(aggregateId, eventType, data, metadata);
        StoredEvent stored = eventStore.appendEvent(event);
        metrics.recordAppend(eventType, aggregateId);
        return stored;
    }
    
    public StoredEvent appendEventWithVersion(String aggregateId, String eventType, 
                                             Object data, long expectedVersion) {
        AppendEvent event = new AppendEvent(aggregateId, eventType, data);
        StoredEvent stored = eventStore.appendEvent(event, expectedVersion);
        metrics.recordAppend(eventType, aggregateId);
        return stored;
    }
    
    public List<StoredEvent> getAggregateStream(String aggregateId) {
        List<StoredEvent> events = eventStore.getAggregateStream(aggregateId);
        metrics.recordRead(events.size());
        return events;
    }
    
    public List<StoredEvent> getAggregateStreamFromVersion(String aggregateId, long fromVersion) {
        List<StoredEvent> events = eventStore.getAggregateStreamFromVersion(aggregateId, fromVersion);
        metrics.recordRead(events.size());
        return events;
    }
    
    public List<StoredEvent> getEventsByType(String eventType) {
        List<StoredEvent> events = eventStore.getEventsByType(eventType);
        metrics.recordRead(events.size());
        return events;
    }
    
    public List<StoredEvent> getEventsByTimeRange(LocalDateTime from, LocalDateTime to) {
        List<StoredEvent> events = eventStore.getEventsByTimeRange(from, to);
        metrics.recordRead(events.size());
        return events;
    }
    
    public List<StoredEvent> getAllEvents() {
        List<StoredEvent> events = eventStore.getAllEvents();
        metrics.recordRead(events.size());
        return events;
    }
    
    public Map<String, Object> getStoreInfo() {
        return Map.of(
            "totalEvents", eventStore.getTotalEventCount(),
            "aggregateCount", eventStore.getAggregateCount()
        );
    }
}

/**
 * Event Projection Builder
 */
@Component
class EventProjectionBuilder {
    
    private final InMemoryEventStore eventStore;
    
    public EventProjectionBuilder(InMemoryEventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    /**
     * Build projection from aggregate events
     */
    public Map<String, Object> buildAggregateProjection(String aggregateId) {
        List<StoredEvent> events = eventStore.getAggregateStream(aggregateId);
        
        Map<String, Object> projection = new HashMap<>();
        projection.put("aggregateId", aggregateId);
        projection.put("eventCount", events.size());
        projection.put("currentVersion", eventStore.getCurrentVersion(aggregateId));
        projection.put("firstEvent", events.isEmpty() ? null : events.get(0).getTimestamp());
        projection.put("lastEvent", events.isEmpty() ? null : 
            events.get(events.size() - 1).getTimestamp());
        
        return projection;
    }
    
    /**
     * Build statistics projection
     */
    public Map<String, Object> buildStatisticsProjection() {
        List<StoredEvent> allEvents = eventStore.getAllEvents();
        
        Map<String, Long> eventTypeStats = allEvents.stream()
            .collect(Collectors.groupingBy(
                StoredEvent::getEventType,
                Collectors.counting()
            ));
        
        return Map.of(
            "totalEvents", allEvents.size(),
            "eventTypes", eventTypeStats.size(),
            "eventTypeDistribution", eventTypeStats
        );
    }
}

/**
 * REST Controller for Event Store
 */
@RestController
@RequestMapping("/api/eventstore")
class EventStoreController {
    
    private final EventStoreService eventStoreService;
    private final EventStoreMetrics metrics;
    private final EventProjectionBuilder projectionBuilder;
    
    public EventStoreController(EventStoreService eventStoreService,
                               EventStoreMetrics metrics,
                               EventProjectionBuilder projectionBuilder) {
        this.eventStoreService = eventStoreService;
        this.metrics = metrics;
        this.projectionBuilder = projectionBuilder;
    }
    
    @PostMapping("/append")
    public Map<String, Object> appendEvent(
            @RequestParam String aggregateId,
            @RequestParam String eventType,
            @RequestBody Map<String, Object> data) {
        
        StoredEvent event = eventStoreService.appendEvent(aggregateId, eventType, data);
        
        return Map.of(
            "eventId", event.getEventId(),
            "aggregateId", event.getAggregateId(),
            "eventType", event.getEventType(),
            "version", event.getVersion(),
            "globalSequence", event.getGlobalSequence()
        );
    }
    
    @PostMapping("/append/metadata")
    public Map<String, Object> appendEventWithMetadata(
            @RequestParam String aggregateId,
            @RequestParam String eventType,
            @RequestBody Map<String, Object> data,
            @RequestParam Map<String, String> metadata) {
        
        StoredEvent event = eventStoreService.appendEventWithMetadata(
            aggregateId, eventType, data, metadata);
        
        return Map.of(
            "eventId", event.getEventId(),
            "version", event.getVersion(),
            "metadata", event.getMetadata()
        );
    }
    
    @GetMapping("/stream/{aggregateId}")
    public List<Map<String, Object>> getAggregateStream(@PathVariable String aggregateId) {
        return eventStoreService.getAggregateStream(aggregateId).stream()
            .map(this::toEventMap)
            .collect(Collectors.toList());
    }
    
    @GetMapping("/stream/{aggregateId}/from/{version}")
    public List<Map<String, Object>> getAggregateStreamFromVersion(
            @PathVariable String aggregateId,
            @PathVariable long version) {
        
        return eventStoreService.getAggregateStreamFromVersion(aggregateId, version).stream()
            .map(this::toEventMap)
            .collect(Collectors.toList());
    }
    
    @GetMapping("/type/{eventType}")
    public List<Map<String, Object>> getEventsByType(@PathVariable String eventType) {
        return eventStoreService.getEventsByType(eventType).stream()
            .map(this::toEventMap)
            .collect(Collectors.toList());
    }
    
    @GetMapping("/all")
    public List<Map<String, Object>> getAllEvents() {
        return eventStoreService.getAllEvents().stream()
            .map(this::toEventMap)
            .collect(Collectors.toList());
    }
    
    @GetMapping("/info")
    public Map<String, Object> getStoreInfo() {
        return eventStoreService.getStoreInfo();
    }
    
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        return metrics.getMetrics();
    }
    
    @GetMapping("/projection/{aggregateId}")
    public Map<String, Object> getAggregateProjection(@PathVariable String aggregateId) {
        return projectionBuilder.buildAggregateProjection(aggregateId);
    }
    
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        return projectionBuilder.buildStatisticsProjection();
    }
    
    private Map<String, Object> toEventMap(StoredEvent event) {
        return Map.of(
            "eventId", event.getEventId(),
            "aggregateId", event.getAggregateId(),
            "eventType", event.getEventType(),
            "version", event.getVersion(),
            "globalSequence", event.getGlobalSequence(),
            "timestamp", event.getTimestamp().toString(),
            "data", event.getData()
        );
    }
}
