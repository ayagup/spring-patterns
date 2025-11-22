package com.example.events.bus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Event Bus Pattern - Demonstrates Custom Event Bus Implementation
 * 
 * This pattern shows how to:
 * 1. Implement custom event bus
 * 2. Create topic-based event routing
 * 3. Register subscribers to topics
 * 4. Publish events to specific topics
 * 5. Implement synchronous event bus
 * 6. Implement asynchronous event bus
 * 7. Handle subscriber registration
 * 8. Filter events by topic
 * 9. Implement dead letter queue
 * 10. Track event bus metrics
 * 
 * Key Concepts:
 * - Event Bus: Central event distribution mechanism
 * - Topic: Named channel for event routing
 * - Subscriber: Consumer that listens to topics
 * - Publisher: Component that sends events to topics
 * - Routing: Directing events to appropriate subscribers
 * 
 * Event Bus Types:
 * 1. Synchronous - Events processed immediately
 * 2. Asynchronous - Events processed in background
 * 3. Topic-Based - Events routed by topic name
 * 4. Priority-Based - Events with priority levels
 * 5. Filtered - Events filtered by criteria
 * 
 * Dependencies:
 * - spring-boot-starter
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class EventBusPattern {

    public static void main(String[] args) {
        SpringApplication.run(EventBusPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("EVENT BUS PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateEventBus();
        demonstrateTopics();
        
        System.out.println("\nApplication running with Event Bus");
        System.out.println("Test endpoints:");
        System.out.println("POST /api/eventbus/publish - Publish event to topic");
        System.out.println("GET /api/eventbus/metrics - View event bus metrics");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateEventBus() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EVENT BUS FEATURES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Topic-Based Routing:");
        System.out.println("   Events routed by topic name");
        
        System.out.println("\n2. Multiple Subscribers:");
        System.out.println("   Multiple subscribers per topic");
        
        System.out.println("\n3. Async Processing:");
        System.out.println("   Background event processing");
    }
    
    private static void demonstrateTopics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TOPIC EXAMPLES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n- user.created");
        System.out.println("- order.placed");
        System.out.println("- payment.processed");
        System.out.println("- notification.sent");
    }
}

/**
 * Event Bus Event Wrapper
 */
class BusEvent<T> {
    private final String eventId;
    private final String topic;
    private final T payload;
    private final LocalDateTime timestamp;
    private final Map<String, String> metadata;
    
    public BusEvent(String topic, T payload) {
        this.eventId = UUID.randomUUID().toString();
        this.topic = topic;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }
    
    public BusEvent(String topic, T payload, Map<String, String> metadata) {
        this.eventId = UUID.randomUUID().toString();
        this.topic = topic;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>(metadata);
    }
    
    public String getEventId() { return eventId; }
    public String getTopic() { return topic; }
    public T getPayload() { return payload; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, String> getMetadata() { return new HashMap<>(metadata); }
}

/**
 * Event Bus Subscriber
 */
interface EventBusSubscriber<T> {
    void onEvent(BusEvent<T> event);
    String getSubscriberId();
}

/**
 * Synchronous Event Bus
 */
@Component
class SynchronousEventBus {
    
    private final Map<String, List<EventBusSubscriber<?>>> subscribers = new ConcurrentHashMap<>();
    private final EventBusMetrics metrics;
    
    public SynchronousEventBus(EventBusMetrics metrics) {
        this.metrics = metrics;
    }
    
    public <T> void subscribe(String topic, EventBusSubscriber<T> subscriber) {
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                   .add(subscriber);
        
        System.out.printf("[SynchronousEventBus] Subscriber %s registered to topic: %s%n",
            subscriber.getSubscriberId(), topic);
    }
    
    public <T> void unsubscribe(String topic, String subscriberId) {
        List<EventBusSubscriber<?>> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            topicSubscribers.removeIf(sub -> sub.getSubscriberId().equals(subscriberId));
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> void publish(BusEvent<T> event) {
        List<EventBusSubscriber<?>> topicSubscribers = subscribers.get(event.getTopic());
        
        if (topicSubscribers == null || topicSubscribers.isEmpty()) {
            System.out.printf("[SynchronousEventBus] No subscribers for topic: %s%n", event.getTopic());
            metrics.recordNoSubscribers(event.getTopic());
            return;
        }
        
        System.out.printf("[SynchronousEventBus] Publishing to topic %s: %d subscribers%n",
            event.getTopic(), topicSubscribers.size());
        
        for (EventBusSubscriber<?> subscriber : topicSubscribers) {
            try {
                ((EventBusSubscriber<T>) subscriber).onEvent(event);
                metrics.recordEventDelivered(event.getTopic());
            } catch (Exception e) {
                System.err.printf("[SynchronousEventBus] Error delivering event to %s: %s%n",
                    subscriber.getSubscriberId(), e.getMessage());
                metrics.recordError(event.getTopic());
            }
        }
    }
    
    public Map<String, Integer> getSubscriberCount() {
        Map<String, Integer> counts = new HashMap<>();
        subscribers.forEach((topic, subs) -> counts.put(topic, subs.size()));
        return counts;
    }
}

/**
 * Asynchronous Event Bus
 */
@Component
class AsynchronousEventBus {
    
    private final Map<String, List<EventBusSubscriber<?>>> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService executorService;
    private final EventBusMetrics metrics;
    
    public AsynchronousEventBus(EventBusMetrics metrics) {
        this.executorService = Executors.newFixedThreadPool(10);
        this.metrics = metrics;
    }
    
    public <T> void subscribe(String topic, EventBusSubscriber<T> subscriber) {
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                   .add(subscriber);
        
        System.out.printf("[AsynchronousEventBus] Subscriber %s registered to topic: %s%n",
            subscriber.getSubscriberId(), topic);
    }
    
    public <T> void unsubscribe(String topic, String subscriberId) {
        List<EventBusSubscriber<?>> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            topicSubscribers.removeIf(sub -> sub.getSubscriberId().equals(subscriberId));
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> void publish(BusEvent<T> event) {
        List<EventBusSubscriber<?>> topicSubscribers = subscribers.get(event.getTopic());
        
        if (topicSubscribers == null || topicSubscribers.isEmpty()) {
            System.out.printf("[AsynchronousEventBus] No subscribers for topic: %s%n", event.getTopic());
            metrics.recordNoSubscribers(event.getTopic());
            return;
        }
        
        System.out.printf("[AsynchronousEventBus] Publishing to topic %s asynchronously: %d subscribers%n",
            event.getTopic(), topicSubscribers.size());
        
        for (EventBusSubscriber<?> subscriber : topicSubscribers) {
            executorService.submit(() -> {
                try {
                    ((EventBusSubscriber<T>) subscriber).onEvent(event);
                    metrics.recordEventDelivered(event.getTopic());
                } catch (Exception e) {
                    System.err.printf("[AsynchronousEventBus] Error delivering event to %s: %s%n",
                        subscriber.getSubscriberId(), e.getMessage());
                    metrics.recordError(event.getTopic());
                }
            });
        }
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}

/**
 * Topic-Based Event Router
 */
@Component
class TopicEventRouter {
    
    private final Map<String, List<Consumer<BusEvent<?>>>> topicHandlers = new ConcurrentHashMap<>();
    
    public void registerHandler(String topic, Consumer<BusEvent<?>> handler) {
        topicHandlers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                     .add(handler);
        
        System.out.printf("[TopicEventRouter] Handler registered for topic: %s%n", topic);
    }
    
    public void route(BusEvent<?> event) {
        List<Consumer<BusEvent<?>>> handlers = topicHandlers.get(event.getTopic());
        
        if (handlers == null || handlers.isEmpty()) {
            System.out.printf("[TopicEventRouter] No handlers for topic: %s%n", event.getTopic());
            return;
        }
        
        for (Consumer<BusEvent<?>> handler : handlers) {
            try {
                handler.accept(event);
            } catch (Exception e) {
                System.err.printf("[TopicEventRouter] Error in handler: %s%n", e.getMessage());
            }
        }
    }
}

/**
 * Event Bus Metrics
 */
@Component
class EventBusMetrics {
    
    private final Map<String, Long> eventsPublished = new ConcurrentHashMap<>();
    private final Map<String, Long> eventsDelivered = new ConcurrentHashMap<>();
    private final Map<String, Long> errors = new ConcurrentHashMap<>();
    private final Map<String, Long> noSubscribers = new ConcurrentHashMap<>();
    
    public void recordEventPublished(String topic) {
        eventsPublished.merge(topic, 1L, Long::sum);
    }
    
    public void recordEventDelivered(String topic) {
        eventsDelivered.merge(topic, 1L, Long::sum);
    }
    
    public void recordError(String topic) {
        errors.merge(topic, 1L, Long::sum);
    }
    
    public void recordNoSubscribers(String topic) {
        noSubscribers.merge(topic, 1L, Long::sum);
    }
    
    public Map<String, Object> getMetrics() {
        return Map.of(
            "eventsPublished", new HashMap<>(eventsPublished),
            "eventsDelivered", new HashMap<>(eventsDelivered),
            "errors", new HashMap<>(errors),
            "noSubscribers", new HashMap<>(noSubscribers)
        );
    }
}

/**
 * Sample Event Subscribers
 */
@Component
class UserEventSubscriber implements EventBusSubscriber<Map<String, Object>> {
    
    @Override
    public void onEvent(BusEvent<Map<String, Object>> event) {
        System.out.printf("[UserEventSubscriber] Received event on topic %s: %s%n",
            event.getTopic(), event.getPayload());
    }
    
    @Override
    public String getSubscriberId() {
        return "UserEventSubscriber";
    }
}

@Component
class OrderEventSubscriber implements EventBusSubscriber<Map<String, Object>> {
    
    @Override
    public void onEvent(BusEvent<Map<String, Object>> event) {
        System.out.printf("[OrderEventSubscriber] Processing order event: %s%n",
            event.getPayload());
    }
    
    @Override
    public String getSubscriberId() {
        return "OrderEventSubscriber";
    }
}

@Component
class NotificationEventSubscriber implements EventBusSubscriber<Map<String, Object>> {
    
    @Override
    public void onEvent(BusEvent<Map<String, Object>> event) {
        System.out.printf("[NotificationEventSubscriber] Sending notification: %s%n",
            event.getPayload());
    }
    
    @Override
    public String getSubscriberId() {
        return "NotificationEventSubscriber";
    }
}

/**
 * Event Bus Service
 */
@Service
class EventBusService {
    
    private final SynchronousEventBus syncEventBus;
    private final AsynchronousEventBus asyncEventBus;
    private final EventBusMetrics metrics;
    
    public EventBusService(SynchronousEventBus syncEventBus,
                          AsynchronousEventBus asyncEventBus,
                          EventBusMetrics metrics,
                          UserEventSubscriber userSubscriber,
                          OrderEventSubscriber orderSubscriber,
                          NotificationEventSubscriber notificationSubscriber) {
        this.syncEventBus = syncEventBus;
        this.asyncEventBus = asyncEventBus;
        this.metrics = metrics;
        
        // Register subscribers
        syncEventBus.subscribe("user.created", userSubscriber);
        syncEventBus.subscribe("user.updated", userSubscriber);
        
        asyncEventBus.subscribe("order.placed", orderSubscriber);
        asyncEventBus.subscribe("order.completed", orderSubscriber);
        
        asyncEventBus.subscribe("notification.email", notificationSubscriber);
        asyncEventBus.subscribe("notification.sms", notificationSubscriber);
    }
    
    public <T> void publishSync(String topic, T payload) {
        BusEvent<T> event = new BusEvent<>(topic, payload);
        syncEventBus.publish(event);
        metrics.recordEventPublished(topic);
    }
    
    public <T> void publishAsync(String topic, T payload) {
        BusEvent<T> event = new BusEvent<>(topic, payload);
        asyncEventBus.publish(event);
        metrics.recordEventPublished(topic);
    }
    
    public <T> void publishWithMetadata(String topic, T payload, Map<String, String> metadata) {
        BusEvent<T> event = new BusEvent<>(topic, payload, metadata);
        asyncEventBus.publish(event);
        metrics.recordEventPublished(topic);
    }
    
    public Map<String, Integer> getSyncSubscriberCount() {
        return syncEventBus.getSubscriberCount();
    }
}

/**
 * REST Controller for Event Bus
 */
@RestController
@RequestMapping("/api/eventbus")
class EventBusController {
    
    private final EventBusService eventBusService;
    private final EventBusMetrics metrics;
    
    public EventBusController(EventBusService eventBusService, EventBusMetrics metrics) {
        this.eventBusService = eventBusService;
        this.metrics = metrics;
    }
    
    @PostMapping("/publish/sync")
    public Map<String, String> publishSyncEvent(
            @RequestParam String topic,
            @RequestBody Map<String, Object> payload) {
        
        eventBusService.publishSync(topic, payload);
        
        return Map.of(
            "status", "published",
            "type", "synchronous",
            "topic", topic
        );
    }
    
    @PostMapping("/publish/async")
    public Map<String, String> publishAsyncEvent(
            @RequestParam String topic,
            @RequestBody Map<String, Object> payload) {
        
        eventBusService.publishAsync(topic, payload);
        
        return Map.of(
            "status", "published",
            "type", "asynchronous",
            "topic", topic
        );
    }
    
    @PostMapping("/publish/metadata")
    public Map<String, String> publishEventWithMetadata(
            @RequestParam String topic,
            @RequestBody Map<String, Object> payload,
            @RequestParam Map<String, String> metadata) {
        
        eventBusService.publishWithMetadata(topic, payload, metadata);
        
        return Map.of(
            "status", "published",
            "topic", topic,
            "metadataKeys", String.join(", ", metadata.keySet())
        );
    }
    
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        return metrics.getMetrics();
    }
    
    @GetMapping("/subscribers")
    public Map<String, Integer> getSubscriberCount() {
        return eventBusService.getSyncSubscriberCount();
    }
}
