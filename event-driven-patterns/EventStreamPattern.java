package com.example.events.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event Stream Pattern - Demonstrates Reactive Event Streams
 * 
 * This pattern shows how to:
 * 1. Create reactive event streams with Project Reactor
 * 2. Use Flux for multiple events
 * 3. Use Mono for single events
 * 4. Handle backpressure
 * 5. Transform event streams
 * 6. Filter event streams
 * 7. Combine multiple streams
 * 8. Buffer and window events
 * 9. Handle stream errors
 * 10. Implement hot vs cold streams
 * 
 * Key Concepts:
 * - Flux: Stream of 0 to N elements
 * - Mono: Stream of 0 or 1 element
 * - Backpressure: Handle slow consumers
 * - Hot Stream: Emits regardless of subscribers
 * - Cold Stream: Emits only when subscribed
 * 
 * Stream Operations:
 * 1. Transformation - map, flatMap, transform
 * 2. Filtering - filter, take, skip
 * 3. Combination - merge, zip, concat
 * 4. Buffering - buffer, window
 * 5. Error Handling - onErrorResume, retry
 * 
 * Dependencies:
 * - spring-boot-starter-webflux
 * - reactor-core
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class EventStreamPattern {

    public static void main(String[] args) {
        SpringApplication.run(EventStreamPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("EVENT STREAM PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateReactiveStreams();
        demonstrateStreamOperations();
        
        System.out.println("\nApplication running with Reactive Event Streams");
        System.out.println("Test endpoints:");
        System.out.println("GET /api/stream/events - Get event stream (SSE)");
        System.out.println("POST /api/stream/publish - Publish event to stream");
        System.out.println("GET /api/stream/buffered - Get buffered events");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateReactiveStreams() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("REACTIVE STREAM TYPES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Flux - Multiple values:");
        System.out.println("   Flux.just(1, 2, 3)");
        
        System.out.println("\n2. Mono - Single value:");
        System.out.println("   Mono.just(\"value\")");
        
        System.out.println("\n3. Backpressure Strategies:");
        System.out.println("   - Buffer: Store events");
        System.out.println("   - Drop: Discard events");
        System.out.println("   - Latest: Keep latest event");
    }
    
    private static void demonstrateStreamOperations() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("STREAM OPERATIONS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n- map() - Transform events");
        System.out.println("- filter() - Filter events");
        System.out.println("- flatMap() - Flatten nested streams");
        System.out.println("- buffer() - Batch events");
        System.out.println("- merge() - Combine streams");
    }
}

/**
 * Stream Event
 */
class StreamEvent {
    private final String eventId;
    private final String eventType;
    private final Object payload;
    private final LocalDateTime timestamp;
    
    public StreamEvent(String eventType, Object payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Object getPayload() { return payload; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

/**
 * Hot Event Stream Publisher (Sinks)
 */
@Component
class HotEventStream {
    
    private final Sinks.Many<StreamEvent> sink;
    private final Flux<StreamEvent> eventStream;
    
    public HotEventStream() {
        // Create a multicast sink (hot stream)
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
        this.eventStream = sink.asFlux();
    }
    
    public void publish(StreamEvent event) {
        Sinks.EmitResult result = sink.tryEmitNext(event);
        
        if (result.isSuccess()) {
            System.out.printf("[HotEventStream] Published event: %s%n", event.getEventType());
        } else {
            System.err.printf("[HotEventStream] Failed to publish event: %s%n", result);
        }
    }
    
    public Flux<StreamEvent> asFlux() {
        return eventStream;
    }
}

/**
 * Cold Event Stream Generator
 */
@Component
class ColdEventStream {
    
    public Flux<StreamEvent> generatePeriodicEvents(Duration interval, int count) {
        return Flux.interval(interval)
            .take(count)
            .map(i -> new StreamEvent("Periodic", 
                Map.of("sequence", i, "timestamp", LocalDateTime.now())))
            .doOnNext(event -> 
                System.out.printf("[ColdEventStream] Generated event %s%n", 
                    event.getEventId()));
    }
    
    public Flux<StreamEvent> generateEventRange(int count) {
        return Flux.range(1, count)
            .map(i -> new StreamEvent("Range", Map.of("value", i)))
            .doOnNext(event -> 
                System.out.printf("[ColdEventStream] Generated range event%n"));
    }
}

/**
 * Event Stream Transformer
 */
@Component
class EventStreamTransformer {
    
    public Flux<StreamEvent> transform(Flux<StreamEvent> stream) {
        return stream
            .map(event -> {
                System.out.printf("[Transformer] Transforming event: %s%n", 
                    event.getEventType());
                return event;
            })
            .filter(event -> {
                // Example filter: only process certain event types
                boolean shouldProcess = !event.getEventType().equals("Ignored");
                if (!shouldProcess) {
                    System.out.printf("[Transformer] Filtering out event: %s%n", 
                        event.getEventType());
                }
                return shouldProcess;
            });
    }
    
    public Flux<List<StreamEvent>> buffer(Flux<StreamEvent> stream, int bufferSize) {
        return stream
            .buffer(bufferSize)
            .doOnNext(events -> 
                System.out.printf("[Transformer] Buffered %d events%n", events.size()));
    }
    
    public Flux<List<StreamEvent>> bufferWithTimeout(Flux<StreamEvent> stream, 
                                                     int maxSize, Duration timeout) {
        return stream
            .bufferTimeout(maxSize, timeout)
            .doOnNext(events -> 
                System.out.printf("[Transformer] Buffered %d events (timeout)%n", 
                    events.size()));
    }
}

/**
 * Event Stream Combiner
 */
@Component
class EventStreamCombiner {
    
    public Flux<StreamEvent> merge(Flux<StreamEvent> stream1, Flux<StreamEvent> stream2) {
        return Flux.merge(stream1, stream2)
            .doOnNext(event -> 
                System.out.printf("[Combiner] Merged event: %s%n", event.getEventType()));
    }
    
    public Flux<StreamEvent> concat(Flux<StreamEvent> stream1, Flux<StreamEvent> stream2) {
        return Flux.concat(stream1, stream2)
            .doOnNext(event -> 
                System.out.printf("[Combiner] Concatenated event: %s%n", 
                    event.getEventType()));
    }
    
    public Flux<String> zip(Flux<StreamEvent> stream1, Flux<StreamEvent> stream2) {
        return Flux.zip(stream1, stream2)
            .map(tuple -> String.format("Zipped: %s + %s", 
                tuple.getT1().getEventType(), 
                tuple.getT2().getEventType()))
            .doOnNext(result -> 
                System.out.printf("[Combiner] %s%n", result));
    }
}

/**
 * Backpressure Handler
 */
@Component
class BackpressureHandler {
    
    public Flux<StreamEvent> handleWithBuffer(Flux<StreamEvent> stream) {
        return stream
            .onBackpressureBuffer(100, 
                dropped -> System.err.printf("[Backpressure] Dropped event: %s%n", 
                    dropped.getEventType()))
            .doOnNext(event -> 
                System.out.printf("[Backpressure] Processing with buffer: %s%n", 
                    event.getEventType()));
    }
    
    public Flux<StreamEvent> handleWithDrop(Flux<StreamEvent> stream) {
        return stream
            .onBackpressureDrop(dropped -> 
                System.err.printf("[Backpressure] Dropped: %s%n", 
                    dropped.getEventType()))
            .doOnNext(event -> 
                System.out.printf("[Backpressure] Processing (drop mode): %s%n", 
                    event.getEventType()));
    }
    
    public Flux<StreamEvent> handleWithLatest(Flux<StreamEvent> stream) {
        return stream
            .onBackpressureLatest()
            .doOnNext(event -> 
                System.out.printf("[Backpressure] Processing latest: %s%n", 
                    event.getEventType()));
    }
}

/**
 * Error Handling Stream
 */
@Component
class ErrorHandlingStream {
    
    public Flux<StreamEvent> handleErrors(Flux<StreamEvent> stream) {
        return stream
            .onErrorResume(error -> {
                System.err.printf("[ErrorHandler] Error occurred: %s%n", 
                    error.getMessage());
                return Flux.empty();
            })
            .doOnError(error -> 
                System.err.printf("[ErrorHandler] Stream error: %s%n", 
                    error.getMessage()));
    }
    
    public Flux<StreamEvent> retryOnError(Flux<StreamEvent> stream, int maxAttempts) {
        return stream
            .retry(maxAttempts)
            .doOnNext(event -> 
                System.out.printf("[ErrorHandler] Event after retry: %s%n", 
                    event.getEventType()));
    }
}

/**
 * Event Stream Processor Service
 */
@Service
class EventStreamProcessor {
    
    private final HotEventStream hotStream;
    private final ColdEventStream coldStream;
    private final EventStreamTransformer transformer;
    private final EventStreamCombiner combiner;
    private final BackpressureHandler backpressureHandler;
    
    public EventStreamProcessor(HotEventStream hotStream,
                               ColdEventStream coldStream,
                               EventStreamTransformer transformer,
                               EventStreamCombiner combiner,
                               BackpressureHandler backpressureHandler) {
        this.hotStream = hotStream;
        this.coldStream = coldStream;
        this.transformer = transformer;
        this.combiner = combiner;
        this.backpressureHandler = backpressureHandler;
    }
    
    public void publishToHotStream(StreamEvent event) {
        hotStream.publish(event);
    }
    
    public Flux<StreamEvent> subscribeToHotStream() {
        return hotStream.asFlux();
    }
    
    public Flux<StreamEvent> generateColdStream(int count) {
        return coldStream.generateEventRange(count);
    }
    
    public Flux<StreamEvent> generatePeriodicStream(Duration interval, int count) {
        return coldStream.generatePeriodicEvents(interval, count);
    }
    
    public Flux<List<StreamEvent>> getBufferedStream(int bufferSize) {
        return transformer.buffer(hotStream.asFlux(), bufferSize);
    }
    
    public Flux<StreamEvent> getTransformedStream() {
        return transformer.transform(hotStream.asFlux());
    }
    
    public Flux<StreamEvent> getMergedStreams() {
        Flux<StreamEvent> stream1 = coldStream.generateEventRange(5);
        Flux<StreamEvent> stream2 = coldStream.generateEventRange(5);
        return combiner.merge(stream1, stream2);
    }
}

/**
 * Stream Statistics
 */
@Component
class StreamStatistics {
    
    private final Map<String, Long> eventCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastEventTime = new ConcurrentHashMap<>();
    
    public void recordEvent(String eventType) {
        eventCounts.merge(eventType, 1L, Long::sum);
        lastEventTime.put(eventType, LocalDateTime.now());
    }
    
    public Map<String, Object> getStatistics() {
        return Map.of(
            "eventCounts", new HashMap<>(eventCounts),
            "lastEventTimes", new HashMap<>(lastEventTime),
            "totalEvents", eventCounts.values().stream().mapToLong(Long::longValue).sum()
        );
    }
}

/**
 * REST Controller for Event Streams
 */
@RestController
@RequestMapping("/api/stream")
class EventStreamController {
    
    private final EventStreamProcessor processor;
    private final StreamStatistics statistics;
    
    public EventStreamController(EventStreamProcessor processor, 
                                 StreamStatistics statistics) {
        this.processor = processor;
        this.statistics = statistics;
    }
    
    @PostMapping("/publish")
    public Map<String, String> publishEvent(
            @RequestParam String eventType,
            @RequestBody Map<String, Object> payload) {
        
        StreamEvent event = new StreamEvent(eventType, payload);
        processor.publishToHotStream(event);
        statistics.recordEvent(eventType);
        
        return Map.of(
            "status", "published",
            "eventId", event.getEventId(),
            "eventType", eventType
        );
    }
    
    @GetMapping(value = "/events", produces = "text/event-stream")
    public Flux<StreamEvent> streamEvents() {
        return processor.subscribeToHotStream()
            .doOnNext(event -> statistics.recordEvent(event.getEventType()));
    }
    
    @GetMapping("/events/cold")
    public Flux<StreamEvent> getColdStream(@RequestParam(defaultValue = "10") int count) {
        return processor.generateColdStream(count)
            .doOnNext(event -> statistics.recordEvent(event.getEventType()));
    }
    
    @GetMapping("/events/periodic")
    public Flux<StreamEvent> getPeriodicStream(
            @RequestParam(defaultValue = "1") long intervalSeconds,
            @RequestParam(defaultValue = "10") int count) {
        
        return processor.generatePeriodicStream(Duration.ofSeconds(intervalSeconds), count)
            .doOnNext(event -> statistics.recordEvent(event.getEventType()));
    }
    
    @GetMapping("/events/buffered")
    public Flux<List<StreamEvent>> getBufferedStream(
            @RequestParam(defaultValue = "5") int bufferSize) {
        
        return processor.getBufferedStream(bufferSize);
    }
    
    @GetMapping("/events/transformed")
    public Flux<StreamEvent> getTransformedStream() {
        return processor.getTransformedStream();
    }
    
    @GetMapping("/events/merged")
    public Flux<StreamEvent> getMergedStreams() {
        return processor.getMergedStreams();
    }
    
    @GetMapping("/statistics")
    public Mono<Map<String, Object>> getStatistics() {
        return Mono.just(statistics.getStatistics());
    }
}
