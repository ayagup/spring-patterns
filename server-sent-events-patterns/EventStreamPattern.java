package com.example.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Event Stream Pattern
 * 
 * Purpose: Structured event streaming with multiple event types and event metadata.
 * Combines SSE with event categorization, filtering, and metadata management.
 * 
 * Key Features:
 * - Multiple event types in single stream
 * - Event categorization and filtering
 * - Event metadata (ID, timestamp, retry)
 * - Event subscription management
 * - Event history and replay
 * - Event transformation
 * 
 * Use Cases:
 * - Multi-source event aggregation
 * - Complex event processing
 * - Event-driven architectures
 * - Real-time analytics dashboards
 * - Activity feeds
 * - Audit trails
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class EventStreamPattern {

    public static void main(String[] args) {
        SpringApplication.run(EventStreamPattern.class, args);
    }

    /**
     * Configuration
     */
    @Configuration
    public static class EventStreamConfig {
        
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
        
        @Bean
        public ExecutorService executorService() {
            return Executors.newCachedThreadPool();
        }
    }

    /**
     * Event Stream Controller
     */
    @RestController
    @RequestMapping("/api/events")
    public static class EventStreamController {

        private final EventStreamService eventStreamService;

        public EventStreamController(EventStreamService eventStreamService) {
            this.eventStreamService = eventStreamService;
        }

        /**
         * Subscribe to all events
         */
        @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter streamAllEvents(
                @RequestParam(required = false) String lastEventId) {
            
            SseEmitter emitter = new SseEmitter(0L); // No timeout
            
            eventStreamService.subscribe("all", null, emitter);
            
            emitter.onCompletion(() -> eventStreamService.unsubscribe(emitter));
            emitter.onTimeout(() -> eventStreamService.unsubscribe(emitter));
            emitter.onError((ex) -> eventStreamService.unsubscribe(emitter));
            
            // Send connection event
            eventStreamService.sendConnectionEvent(emitter, lastEventId);
            
            return emitter;
        }

        /**
         * Subscribe to specific event types
         */
        @GetMapping(path = "/stream/types", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter streamEventTypes(
                @RequestParam List<String> types,
                @RequestParam(required = false) String lastEventId) {
            
            SseEmitter emitter = new SseEmitter(300000L); // 5 minutes
            
            eventStreamService.subscribe("filtered", types, emitter);
            
            emitter.onCompletion(() -> eventStreamService.unsubscribe(emitter));
            emitter.onTimeout(() -> eventStreamService.unsubscribe(emitter));
            emitter.onError((ex) -> eventStreamService.unsubscribe(emitter));
            
            return emitter;
        }

        /**
         * Subscribe to category events
         */
        @GetMapping(path = "/stream/category/{category}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter streamCategoryEvents(
                @PathVariable String category,
                @RequestParam(required = false) String lastEventId) {
            
            SseEmitter emitter = new SseEmitter(0L);
            
            eventStreamService.subscribe(category, null, emitter);
            
            emitter.onCompletion(() -> eventStreamService.unsubscribe(emitter));
            emitter.onTimeout(() -> eventStreamService.unsubscribe(emitter));
            emitter.onError((ex) -> eventStreamService.unsubscribe(emitter));
            
            return emitter;
        }

        /**
         * Publish event
         */
        @PostMapping("/publish")
        public Map<String, Object> publishEvent(@RequestBody EventData eventData) {
            Event event = eventStreamService.publishEvent(eventData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("eventId", event.getId());
            response.put("published", true);
            response.put("timestamp", event.getTimestamp());
            
            return response;
        }

        /**
         * Get event history
         */
        @GetMapping("/history")
        public List<Event> getEventHistory(
                @RequestParam(defaultValue = "100") int limit,
                @RequestParam(required = false) String type,
                @RequestParam(required = false) String category) {
            
            return eventStreamService.getEventHistory(limit, type, category);
        }

        /**
         * Get event by ID
         */
        @GetMapping("/history/{eventId}")
        public Event getEvent(@PathVariable String eventId) {
            return eventStreamService.getEvent(eventId);
        }

        /**
         * Get event stream statistics
         */
        @GetMapping("/stats")
        public EventStreamStats getStats() {
            return eventStreamService.getStats();
        }

        /**
         * Replay events from specific ID
         */
        @GetMapping(path = "/replay/{fromEventId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter replayEvents(@PathVariable String fromEventId) {
            SseEmitter emitter = new SseEmitter(60000L); // 1 minute timeout
            
            eventStreamService.replayEvents(fromEventId, emitter);
            
            return emitter;
        }
    }

    /**
     * Event Stream Service
     */
    @Service
    public static class EventStreamService {

        private final Map<String, List<EventSubscription>> subscriptions = new ConcurrentHashMap<>();
        private final List<Event> eventHistory = new CopyOnWriteArrayList<>();
        private final Map<String, Event> eventIndex = new ConcurrentHashMap<>();
        private final ObjectMapper objectMapper;
        
        private static final int MAX_HISTORY_SIZE = 1000;
        private long eventCounter = 0;
        private long totalEventsPublished = 0;

        public EventStreamService(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        /**
         * Subscribe to events
         */
        public void subscribe(String category, List<String> eventTypes, SseEmitter emitter) {
            EventSubscription subscription = new EventSubscription(
                UUID.randomUUID().toString(),
                category,
                eventTypes,
                emitter,
                LocalDateTime.now()
            );
            
            subscriptions.computeIfAbsent(category, k -> new CopyOnWriteArrayList<>())
                .add(subscription);
            
            System.out.println("New subscription: category=" + category + 
                             ", types=" + eventTypes + 
                             ", total=" + getTotalSubscriptions());
        }

        /**
         * Unsubscribe
         */
        public void unsubscribe(SseEmitter emitter) {
            for (List<EventSubscription> subs : subscriptions.values()) {
                subs.removeIf(sub -> sub.getEmitter().equals(emitter));
            }
            
            System.out.println("Subscription removed. Total: " + getTotalSubscriptions());
        }

        /**
         * Publish event
         */
        public Event publishEvent(EventData eventData) {
            Event event = new Event(
                generateEventId(),
                eventData.getType(),
                eventData.getCategory(),
                eventData.getData(),
                eventData.getMetadata(),
                System.currentTimeMillis(),
                LocalDateTime.now()
            );
            
            // Add to history
            addToHistory(event);
            
            // Broadcast to subscribers
            broadcastEvent(event);
            
            totalEventsPublished++;
            
            return event;
        }

        /**
         * Send connection event
         */
        public void sendConnectionEvent(SseEmitter emitter, String lastEventId) {
            try {
                Map<String, Object> connectionData = new HashMap<>();
                connectionData.put("status", "connected");
                connectionData.put("timestamp", LocalDateTime.now());
                connectionData.put("serverTime", System.currentTimeMillis());
                
                if (lastEventId != null) {
                    connectionData.put("lastEventId", lastEventId);
                }
                
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(generateEventId())
                    .name("connection")
                    .data(connectionData);
                
                emitter.send(event);
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }

        /**
         * Broadcast event to subscribers
         */
        private void broadcastEvent(Event event) {
            // Broadcast to "all" subscribers
            broadcastToCategory("all", event);
            
            // Broadcast to category-specific subscribers
            if (event.getCategory() != null) {
                broadcastToCategory(event.getCategory(), event);
            }
            
            // Broadcast to filtered subscribers
            broadcastToCategory("filtered", event);
        }

        /**
         * Broadcast to specific category
         */
        private void broadcastToCategory(String category, Event event) {
            List<EventSubscription> subs = subscriptions.get(category);
            if (subs == null || subs.isEmpty()) {
                return;
            }
            
            List<EventSubscription> deadSubscriptions = new ArrayList<>();
            
            for (EventSubscription subscription : subs) {
                // Filter by event types if specified
                if (subscription.getEventTypes() != null && 
                    !subscription.getEventTypes().isEmpty() &&
                    !subscription.getEventTypes().contains(event.getType())) {
                    continue;
                }
                
                try {
                    SseEmitter.SseEventBuilder sseEvent = SseEmitter.event()
                        .id(event.getId())
                        .name(event.getType())
                        .data(event);
                    
                    subscription.getEmitter().send(sseEvent);
                } catch (IOException e) {
                    deadSubscriptions.add(subscription);
                }
            }
            
            // Remove dead subscriptions
            if (!deadSubscriptions.isEmpty()) {
                subs.removeAll(deadSubscriptions);
            }
        }

        /**
         * Add event to history
         */
        private void addToHistory(Event event) {
            eventHistory.add(event);
            eventIndex.put(event.getId(), event);
            
            // Trim history if too large
            if (eventHistory.size() > MAX_HISTORY_SIZE) {
                Event removed = eventHistory.remove(0);
                eventIndex.remove(removed.getId());
            }
        }

        /**
         * Generate event ID
         */
        private synchronized String generateEventId() {
            return String.format("evt-%d-%d", 
                System.currentTimeMillis(), 
                ++eventCounter);
        }

        /**
         * Get event history
         */
        public List<Event> getEventHistory(int limit, String type, String category) {
            return eventHistory.stream()
                .filter(e -> type == null || type.equals(e.getType()))
                .filter(e -> category == null || category.equals(e.getCategory()))
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                .limit(limit)
                .toList();
        }

        /**
         * Get event by ID
         */
        public Event getEvent(String eventId) {
            return eventIndex.get(eventId);
        }

        /**
         * Replay events from specific ID
         */
        public void replayEvents(String fromEventId, SseEmitter emitter) {
            new Thread(() -> {
                try {
                    boolean found = false;
                    
                    for (Event event : eventHistory) {
                        if (found || event.getId().equals(fromEventId)) {
                            found = true;
                            
                            if (!event.getId().equals(fromEventId)) {
                                SseEmitter.SseEventBuilder sseEvent = SseEmitter.event()
                                    .id(event.getId())
                                    .name(event.getType())
                                    .data(event);
                                
                                emitter.send(sseEvent);
                                
                                // Small delay between events
                                Thread.sleep(10);
                            }
                        }
                    }
                    
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }).start();
        }

        /**
         * Get statistics
         */
        public EventStreamStats getStats() {
            int totalSubs = getTotalSubscriptions();
            Map<String, Integer> subscriptionsByCategory = new HashMap<>();
            
            for (Map.Entry<String, List<EventSubscription>> entry : subscriptions.entrySet()) {
                subscriptionsByCategory.put(entry.getKey(), entry.getValue().size());
            }
            
            Map<String, Long> eventsByType = eventHistory.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Event::getType, 
                    java.util.stream.Collectors.counting()
                ));
            
            return new EventStreamStats(
                totalEventsPublished,
                eventHistory.size(),
                totalSubs,
                subscriptionsByCategory,
                eventsByType,
                LocalDateTime.now()
            );
        }

        /**
         * Get total subscriptions
         */
        private int getTotalSubscriptions() {
            return subscriptions.values().stream()
                .mapToInt(List::size)
                .sum();
        }

        /**
         * Scheduled event generation (demo)
         */
        @Scheduled(fixedRate = 10000)
        public void generateSystemEvents() {
            if (getTotalSubscriptions() == 0) {
                return;
            }
            
            // System heartbeat event
            EventData heartbeat = new EventData(
                "system.heartbeat",
                "system",
                Map.of(
                    "status", "healthy",
                    "uptime", System.currentTimeMillis(),
                    "subscriptions", getTotalSubscriptions()
                ),
                Map.of("source", "scheduler")
            );
            
            publishEvent(heartbeat);
        }

        /**
         * Scheduled event generation (demo) - Market data
         */
        @Scheduled(fixedRate = 5000)
        public void generateMarketEvents() {
            if (getTotalSubscriptions() == 0) {
                return;
            }
            
            Random random = new Random();
            String[] symbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"};
            String symbol = symbols[random.nextInt(symbols.length)];
            
            EventData marketUpdate = new EventData(
                "market.update",
                "market",
                Map.of(
                    "symbol", symbol,
                    "price", 100 + random.nextDouble() * 400,
                    "change", (random.nextDouble() - 0.5) * 10,
                    "volume", random.nextInt(10000) + 1000
                ),
                Map.of("source", "market-feed", "priority", "high")
            );
            
            publishEvent(marketUpdate);
        }
    }

    // Model Classes

    public static class Event {
        private String id;
        private String type;
        private String category;
        private Map<String, Object> data;
        private Map<String, Object> metadata;
        private long timestampMillis;
        private LocalDateTime timestamp;

        public Event() {}

        public Event(String id, String type, String category, Map<String, Object> data,
                    Map<String, Object> metadata, long timestampMillis, LocalDateTime timestamp) {
            this.id = id;
            this.type = type;
            this.category = category;
            this.data = data;
            this.metadata = metadata;
            this.timestampMillis = timestampMillis;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        public long getTimestampMillis() { return timestampMillis; }
        public void setTimestampMillis(long timestampMillis) { this.timestampMillis = timestampMillis; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class EventData {
        private String type;
        private String category;
        private Map<String, Object> data;
        private Map<String, Object> metadata;

        public EventData() {}

        public EventData(String type, String category, Map<String, Object> data, 
                        Map<String, Object> metadata) {
            this.type = type;
            this.category = category;
            this.data = data;
            this.metadata = metadata;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class EventSubscription {
        private String id;
        private String category;
        private List<String> eventTypes;
        private SseEmitter emitter;
        private LocalDateTime subscribedAt;

        public EventSubscription(String id, String category, List<String> eventTypes,
                                SseEmitter emitter, LocalDateTime subscribedAt) {
            this.id = id;
            this.category = category;
            this.eventTypes = eventTypes;
            this.emitter = emitter;
            this.subscribedAt = subscribedAt;
        }

        // Getters
        public String getId() { return id; }
        public String getCategory() { return category; }
        public List<String> getEventTypes() { return eventTypes; }
        public SseEmitter getEmitter() { return emitter; }
        public LocalDateTime getSubscribedAt() { return subscribedAt; }
    }

    public static class EventStreamStats {
        private long totalEventsPublished;
        private int eventHistorySize;
        private int activeSubscriptions;
        private Map<String, Integer> subscriptionsByCategory;
        private Map<String, Long> eventsByType;
        private LocalDateTime timestamp;

        public EventStreamStats(long totalEventsPublished, int eventHistorySize,
                               int activeSubscriptions, Map<String, Integer> subscriptionsByCategory,
                               Map<String, Long> eventsByType, LocalDateTime timestamp) {
            this.totalEventsPublished = totalEventsPublished;
            this.eventHistorySize = eventHistorySize;
            this.activeSubscriptions = activeSubscriptions;
            this.subscriptionsByCategory = subscriptionsByCategory;
            this.eventsByType = eventsByType;
            this.timestamp = timestamp;
        }

        // Getters
        public long getTotalEventsPublished() { return totalEventsPublished; }
        public int getEventHistorySize() { return eventHistorySize; }
        public int getActiveSubscriptions() { return activeSubscriptions; }
        public Map<String, Integer> getSubscriptionsByCategory() { return subscriptionsByCategory; }
        public Map<String, Long> getEventsByType() { return eventsByType; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}

/*
 * Client-Side JavaScript Example:
 * 
 * // Subscribe to all events
 * const eventSource = new EventSource('/api/events/stream');
 * 
 * eventSource.addEventListener('connection', function(event) {
 *     console.log('Connected:', JSON.parse(event.data));
 * });
 * 
 * eventSource.addEventListener('system.heartbeat', function(event) {
 *     const data = JSON.parse(event.data);
 *     console.log('Heartbeat:', data);
 * });
 * 
 * eventSource.addEventListener('market.update', function(event) {
 *     const data = JSON.parse(event.data);
 *     console.log('Market update:', data.data.symbol, data.data.price);
 * });
 * 
 * // Subscribe to specific event types
 * const marketSource = new EventSource(
 *     '/api/events/stream/types?types=market.update&types=market.alert'
 * );
 * 
 * marketSource.addEventListener('market.update', function(event) {
 *     handleMarketUpdate(JSON.parse(event.data));
 * });
 * 
 * // Subscribe to category
 * const systemSource = new EventSource('/api/events/stream/category/system');
 * 
 * systemSource.onmessage = function(event) {
 *     console.log('System event:', JSON.parse(event.data));
 * };
 * 
 * // Publish event
 * function publishEvent(type, category, data) {
 *     fetch('/api/events/publish', {
 *         method: 'POST',
 *         headers: {'Content-Type': 'application/json'},
 *         body: JSON.stringify({
 *             type: type,
 *             category: category,
 *             data: data,
 *             metadata: {source: 'web-client'}
 *         })
 *     })
 *     .then(response => response.json())
 *     .then(result => console.log('Event published:', result));
 * }
 * 
 * publishEvent('user.action', 'user', {action: 'click', button: 'submit'});
 */
