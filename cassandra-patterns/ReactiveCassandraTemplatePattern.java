package com.example.cassandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.cassandra.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Reactive Cassandra Template Pattern
 * 
 * Demonstrates the use of ReactiveCassandraTemplate for non-blocking,
 * reactive CQL operations with Apache Cassandra.
 * 
 * Key concepts:
 * - ReactiveCassandraTemplate for reactive operations
 * - Mono<T> for single results
 * - Flux<T> for multiple results
 * - Non-blocking I/O operations
 * - Backpressure support
 * - Reactive query operations
 * 
 * Use cases:
 * - High-throughput applications
 * - Non-blocking database operations
 * - Streaming large result sets
 * - Reactive microservices
 * - Real-time data processing
 */
@SpringBootApplication
public class ReactiveCassandraTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveCassandraTemplatePattern.class, args);
    }
}

/**
 * Event entity for time-series data
 */
record Event(
    UUID id,
    String eventType,
    String source,
    String data,
    String severity,
    LocalDateTime timestamp
) {
    public Event {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

/**
 * Service demonstrating ReactiveCassandraTemplate operations
 */
@Service
class ReactiveEventService {
    
    private final ReactiveCassandraTemplate reactiveCassandraTemplate;
    
    public ReactiveEventService(ReactiveCassandraTemplate reactiveCassandraTemplate) {
        this.reactiveCassandraTemplate = reactiveCassandraTemplate;
    }
    
    /**
     * Insert a single event
     */
    public Mono<Event> createEvent(Event event) {
        return reactiveCassandraTemplate.insert(event);
    }
    
    /**
     * Insert multiple events
     */
    public Flux<Event> createEvents(List<Event> events) {
        return reactiveCassandraTemplate.insert(events);
    }
    
    /**
     * Update an event
     */
    public Mono<Event> updateEvent(Event event) {
        return reactiveCassandraTemplate.update(event);
    }
    
    /**
     * Update event using Query and Update objects
     */
    public Mono<Boolean> updateEventSeverity(UUID id, String severity) {
        Query query = Query.query(Criteria.where("id").is(id));
        Update update = Update.update("severity", severity)
                             .set("timestamp", LocalDateTime.now());
        return reactiveCassandraTemplate.update(query, update, Event.class);
    }
    
    /**
     * Find event by ID
     */
    public Mono<Event> findById(UUID id) {
        return reactiveCassandraTemplate.selectOneById(id, Event.class);
    }
    
    /**
     * Find all events
     */
    public Flux<Event> findAll() {
        return reactiveCassandraTemplate.select(Query.empty(), Event.class);
    }
    
    /**
     * Find events by type
     */
    public Flux<Event> findByEventType(String eventType) {
        Query query = Query.query(Criteria.where("eventType").is(eventType));
        return reactiveCassandraTemplate.select(query, Event.class);
    }
    
    /**
     * Find events by source
     */
    public Flux<Event> findBySource(String source) {
        Query query = Query.query(Criteria.where("source").is(source));
        return reactiveCassandraTemplate.select(query, Event.class);
    }
    
    /**
     * Find events by severity
     */
    public Flux<Event> findBySeverity(String severity) {
        Query query = Query.query(Criteria.where("severity").is(severity));
        return reactiveCassandraTemplate.select(query, Event.class);
    }
    
    /**
     * Find events with limit
     */
    public Flux<Event> findWithLimit(int limit) {
        Query query = Query.empty().limit(limit);
        return reactiveCassandraTemplate.select(query, Event.class);
    }
    
    /**
     * Stream all events (for large datasets)
     */
    public Flux<Event> streamAll() {
        return reactiveCassandraTemplate.select(Query.empty(), Event.class);
    }
    
    /**
     * Check if event exists
     */
    public Mono<Boolean> exists(UUID id) {
        return reactiveCassandraTemplate.exists(id, Event.class);
    }
    
    /**
     * Count all events
     */
    public Mono<Long> count() {
        return reactiveCassandraTemplate.count(Event.class);
    }
    
    /**
     * Count events by type
     */
    public Mono<Long> countByEventType(String eventType) {
        Query query = Query.query(Criteria.where("eventType").is(eventType));
        return reactiveCassandraTemplate.count(query, Event.class);
    }
    
    /**
     * Count events by severity
     */
    public Mono<Long> countBySeverity(String severity) {
        Query query = Query.query(Criteria.where("severity").is(severity));
        return reactiveCassandraTemplate.count(query, Event.class);
    }
    
    /**
     * Delete event by ID
     */
    public Mono<Boolean> deleteById(UUID id) {
        return reactiveCassandraTemplate.deleteById(id, Event.class);
    }
    
    /**
     * Delete event entity
     */
    public Mono<Boolean> deleteEvent(Event event) {
        return reactiveCassandraTemplate.delete(event);
    }
    
    /**
     * Delete events by query
     */
    public Mono<Boolean> deleteByEventType(String eventType) {
        Query query = Query.query(Criteria.where("eventType").is(eventType));
        return reactiveCassandraTemplate.delete(query, Event.class);
    }
    
    /**
     * Delete events by severity
     */
    public Mono<Boolean> deleteBySeverity(String severity) {
        Query query = Query.query(Criteria.where("severity").is(severity));
        return reactiveCassandraTemplate.delete(query, Event.class);
    }
    
    /**
     * Truncate all events
     */
    public Mono<Void> truncate() {
        return reactiveCassandraTemplate.truncate(Event.class);
    }
}

/**
 * Reactive REST controller for event operations
 */
@RestController
@RequestMapping("/api/events")
class ReactiveEventController {
    
    private final ReactiveEventService eventService;
    
    public ReactiveEventController(ReactiveEventService eventService) {
        this.eventService = eventService;
    }
    
    @PostMapping
    public Mono<ResponseEntity<Event>> createEvent(@RequestBody Event event) {
        return eventService.createEvent(event)
                          .map(ResponseEntity::ok);
    }
    
    @PostMapping("/batch")
    public Flux<Event> createEvents(@RequestBody List<Event> events) {
        return eventService.createEvents(events);
    }
    
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Event>> updateEvent(@PathVariable UUID id, @RequestBody Event event) {
        Event updated = new Event(id, event.eventType(), event.source(), 
                                  event.data(), event.severity(), LocalDateTime.now());
        return eventService.updateEvent(updated)
                          .map(ResponseEntity::ok);
    }
    
    @PatchMapping("/{id}/severity")
    public Mono<ResponseEntity<Void>> updateSeverity(@PathVariable UUID id, @RequestParam String severity) {
        return eventService.updateEventSeverity(id, severity)
                          .map(updated -> ResponseEntity.noContent().<Void>build());
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Event>> getEvent(@PathVariable UUID id) {
        return eventService.findById(id)
                          .map(ResponseEntity::ok)
                          .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public Flux<Event> getAllEvents(@RequestParam(required = false) Integer limit) {
        return limit != null ? 
            eventService.findWithLimit(limit) : 
            eventService.findAll();
    }
    
    @GetMapping("/stream")
    public Flux<Event> streamAllEvents() {
        return eventService.streamAll();
    }
    
    @GetMapping("/type/{eventType}")
    public Flux<Event> getEventsByType(@PathVariable String eventType) {
        return eventService.findByEventType(eventType);
    }
    
    @GetMapping("/source/{source}")
    public Flux<Event> getEventsBySource(@PathVariable String source) {
        return eventService.findBySource(source);
    }
    
    @GetMapping("/severity/{severity}")
    public Flux<Event> getEventsBySeverity(@PathVariable String severity) {
        return eventService.findBySeverity(severity);
    }
    
    @GetMapping("/{id}/exists")
    public Mono<ResponseEntity<Boolean>> eventExists(@PathVariable UUID id) {
        return eventService.exists(id)
                          .map(ResponseEntity::ok);
    }
    
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countEvents() {
        return eventService.count()
                          .map(ResponseEntity::ok);
    }
    
    @GetMapping("/count/type/{eventType}")
    public Mono<ResponseEntity<Long>> countByEventType(@PathVariable String eventType) {
        return eventService.countByEventType(eventType)
                          .map(ResponseEntity::ok);
    }
    
    @GetMapping("/count/severity/{severity}")
    public Mono<ResponseEntity<Long>> countBySeverity(@PathVariable String severity) {
        return eventService.countBySeverity(severity)
                          .map(ResponseEntity::ok);
    }
    
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteEvent(@PathVariable UUID id) {
        return eventService.deleteById(id)
                          .map(deleted -> ResponseEntity.noContent().<Void>build());
    }
    
    @DeleteMapping("/type/{eventType}")
    public Mono<ResponseEntity<Void>> deleteByEventType(@PathVariable String eventType) {
        return eventService.deleteByEventType(eventType)
                          .map(deleted -> ResponseEntity.noContent().<Void>build());
    }
    
    @DeleteMapping("/severity/{severity}")
    public Mono<ResponseEntity<Void>> deleteBySeverity(@PathVariable String severity) {
        return eventService.deleteBySeverity(severity)
                          .map(deleted -> ResponseEntity.noContent().<Void>build());
    }
    
    @DeleteMapping("/truncate")
    public Mono<ResponseEntity<Void>> truncate() {
        return eventService.truncate()
                          .thenReturn(ResponseEntity.noContent().<Void>build());
    }
    
    @GetMapping("/info")
    public Mono<ResponseEntity<String>> getInfo() {
        return Mono.just(ResponseEntity.ok("""
            Reactive Cassandra Template Pattern
            
            This pattern demonstrates the use of ReactiveCassandraTemplate for non-blocking,
            reactive CQL operations with Apache Cassandra.
            
            Features:
            - Non-blocking I/O operations
            - Mono<T> for single results
            - Flux<T> for multiple results
            - Backpressure support
            - Streaming large datasets
            - Reactive query operations
            - Insert, update, delete operations
            - Count and exists operations
            
            Endpoints (all reactive):
            - POST /api/events - Create event (Mono)
            - POST /api/events/batch - Create multiple events (Flux)
            - PUT /api/events/{id} - Update event (Mono)
            - PATCH /api/events/{id}/severity - Update severity (Mono)
            - GET /api/events/{id} - Get event (Mono)
            - GET /api/events - Get all events (Flux, optional limit)
            - GET /api/events/stream - Stream all events (Flux)
            - GET /api/events/type/{eventType} - Filter by type (Flux)
            - GET /api/events/source/{source} - Filter by source (Flux)
            - GET /api/events/severity/{severity} - Filter by severity (Flux)
            - GET /api/events/{id}/exists - Check exists (Mono)
            - GET /api/events/count - Count all (Mono)
            - GET /api/events/count/type/{eventType} - Count by type (Mono)
            - GET /api/events/count/severity/{severity} - Count by severity (Mono)
            - DELETE /api/events/{id} - Delete event (Mono)
            - DELETE /api/events/type/{eventType} - Delete by type (Mono)
            - DELETE /api/events/severity/{severity} - Delete by severity (Mono)
            - DELETE /api/events/truncate - Truncate table (Mono)
            """));
    }
}
