package com.example.microservices.servicecommunication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;

/**
 * Service-to-Service Communication Pattern
 * 
 * This pattern demonstrates different approaches for microservices to communicate with each other.
 * Includes synchronous (REST) and asynchronous (messaging) communication patterns.
 * 
 * Key Components:
 * 1. SyncCommunicationService - REST-based synchronous communication
 * 2. AsyncCommunicationService - Message-based asynchronous communication
 * 3. CircuitBreaker - Fault tolerance for service calls
 * 4. ServiceRegistry - Service discovery for dynamic endpoints
 * 5. RequestTracing - Distributed tracing across services
 * 
 * Communication Patterns:
 * - Synchronous REST (HTTP)
 * - Asynchronous Messaging (Event-driven)
 * - Circuit Breaker for resilience
 * - Request/Response with timeout
 * - Fire-and-Forget
 * - Request Correlation
 * 
 * Use Cases:
 * - API composition
 * - Event-driven architectures
 * - Saga patterns for distributed transactions
 * - Microservice orchestration
 * - Resilient inter-service communication
 */

@SpringBootApplication
public class ServiceToServiceCommunicationPattern {

    public static void main(String[] args) {
        SpringApplication.run(ServiceToServiceCommunicationPattern.class, args);
        
        // Demonstration
        System.out.println("=== Service-to-Service Communication Pattern Demo ===\n");
        
        ServiceRegistry registry = new ServiceRegistry();
        CircuitBreaker circuitBreaker = new CircuitBreaker("order-service", 3, 5000);
        
        // Register services
        registry.registerService("user-service", "http://localhost:8081");
        registry.registerService("order-service", "http://localhost:8082");
        registry.registerService("inventory-service", "http://localhost:8083");
        
        // Synchronous Communication
        System.out.println("1. Synchronous REST Communication:");
        SyncCommunicationService syncService = new SyncCommunicationService(registry, circuitBreaker);
        
        try {
            Map<String, Object> user = syncService.getUser("user123");
            System.out.println("Fetched user: " + user.get("name"));
            
            Map<String, Object> orderRequest = new HashMap<>();
            orderRequest.put("userId", "user123");
            orderRequest.put("productId", "prod456");
            orderRequest.put("quantity", 2);
            
            Map<String, Object> order = syncService.createOrder(orderRequest);
            System.out.println("Created order: " + order.get("orderId"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Asynchronous Communication
        System.out.println("\n2. Asynchronous Messaging Communication:");
        AsyncCommunicationService asyncService = new AsyncCommunicationService();
        MessageBroker broker = new MessageBroker();
        
        // Subscribe to events
        broker.subscribe("order.created", event -> {
            System.out.println("Order Service received: " + event.getEventType() + 
                             " - OrderID: " + event.getData().get("orderId"));
        });
        
        broker.subscribe("order.created", event -> {
            System.out.println("Inventory Service received: " + event.getEventType() + 
                             " - Updating inventory for order: " + event.getData().get("orderId"));
        });
        
        broker.subscribe("order.created", event -> {
            System.out.println("Notification Service received: " + event.getEventType() + 
                             " - Sending email for order: " + event.getData().get("orderId"));
        });
        
        // Publish event
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", "ORD-" + UUID.randomUUID().toString().substring(0, 8));
        orderData.put("userId", "user123");
        orderData.put("total", 299.99);
        
        asyncService.publishEvent(broker, "order.created", orderData);
        
        // Request-Response Pattern with Correlation
        System.out.println("\n3. Request-Response with Correlation ID:");
        String correlationId = UUID.randomUUID().toString();
        RequestContext context = new RequestContext(correlationId);
        
        System.out.println("Request started with correlation ID: " + correlationId);
        Map<String, Object> response = syncService.getWithCorrelation("user-service", "/api/users/user123", context);
        System.out.println("Response received for correlation ID: " + context.getCorrelationId());
        
        // Circuit Breaker Demo
        System.out.println("\n4. Circuit Breaker Pattern:");
        System.out.println("Circuit state: " + circuitBreaker.getState());
        
        // Simulate failures
        for (int i = 0; i < 5; i++) {
            try {
                circuitBreaker.call(() -> {
                    if (Math.random() > 0.3) {
                        throw new RuntimeException("Service unavailable");
                    }
                    return "Success";
                });
            } catch (Exception e) {
                System.out.println("Attempt " + (i + 1) + " failed: " + e.getMessage());
            }
        }
        
        System.out.println("Circuit state after failures: " + circuitBreaker.getState());
    }
}

/**
 * Synchronous Communication Service - REST-based
 */
@Service
class SyncCommunicationService {
    private final ServiceRegistry registry;
    private final CircuitBreaker circuitBreaker;
    private final RestTemplate restTemplate;
    
    public SyncCommunicationService(ServiceRegistry registry, CircuitBreaker circuitBreaker) {
        this.registry = registry;
        this.circuitBreaker = circuitBreaker;
        this.restTemplate = new RestTemplate();
    }
    
    public Map<String, Object> getUser(String userId) {
        String serviceUrl = registry.getServiceUrl("user-service");
        String url = serviceUrl + "/api/users/" + userId;
        
        return circuitBreaker.call(() -> {
            // Simulate REST call
            Map<String, Object> user = new HashMap<>();
            user.put("id", userId);
            user.put("name", "John Doe");
            user.put("email", "john@example.com");
            return user;
        });
    }
    
    public Map<String, Object> createOrder(Map<String, Object> orderRequest) {
        String serviceUrl = registry.getServiceUrl("order-service");
        String url = serviceUrl + "/api/orders";
        
        return circuitBreaker.call(() -> {
            // Simulate REST POST
            Map<String, Object> order = new HashMap<>();
            order.put("orderId", "ORD-" + UUID.randomUUID().toString().substring(0, 8));
            order.put("userId", orderRequest.get("userId"));
            order.put("status", "CREATED");
            order.put("createdAt", LocalDateTime.now());
            return order;
        });
    }
    
    public Map<String, Object> getWithCorrelation(String serviceName, String path, RequestContext context) {
        String serviceUrl = registry.getServiceUrl(serviceName);
        String url = serviceUrl + path;
        
        // Add correlation ID to headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", context.getCorrelationId());
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        
        // Simulate REST call with headers
        Map<String, Object> response = new HashMap<>();
        response.put("correlationId", context.getCorrelationId());
        response.put("data", getUser(path.substring(path.lastIndexOf('/') + 1)));
        
        return response;
    }
    
    public CompletableFuture<Map<String, Object>> getAsync(String serviceName, String path) {
        return CompletableFuture.supplyAsync(() -> {
            String serviceUrl = registry.getServiceUrl(serviceName);
            // Simulate async REST call
            return new HashMap<>(Map.of("async", true, "service", serviceName));
        });
    }
}

/**
 * Asynchronous Communication Service - Event-based
 */
@Service
class AsyncCommunicationService {
    
    public void publishEvent(MessageBroker broker, String eventType, Map<String, Object> data) {
        Event event = new Event(eventType, data);
        broker.publish(eventType, event);
        System.out.println("Published event: " + eventType);
    }
    
    public void sendMessage(String queue, Object message) {
        // Simulate sending message to queue
        System.out.println("Sent message to queue '" + queue + "': " + message);
    }
    
    public void publishToTopic(String topic, Object message) {
        // Simulate publishing to topic (pub/sub)
        System.out.println("Published to topic '" + topic + "': " + message);
    }
}

/**
 * Simple Message Broker for event-driven communication
 */
class MessageBroker {
    private final Map<String, List<EventHandler>> subscribers = new ConcurrentHashMap<>();
    
    public void subscribe(String eventType, EventHandler handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }
    
    public void publish(String eventType, Event event) {
        List<EventHandler> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.forEach(handler -> {
                try {
                    handler.handle(event);
                } catch (Exception e) {
                    System.err.println("Error handling event: " + e.getMessage());
                }
            });
        }
    }
    
    public void unsubscribe(String eventType, EventHandler handler) {
        List<EventHandler> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }
}

/**
 * Event representation
 */
class Event {
    private final String eventId;
    private final String eventType;
    private final Map<String, Object> data;
    private final LocalDateTime timestamp;
    
    public Event(String eventType, Map<String, Object> data) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Map<String, Object> getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

/**
 * Event Handler interface
 */
@FunctionalInterface
interface EventHandler {
    void handle(Event event);
}

/**
 * Circuit Breaker for fault tolerance
 */
class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }
    
    private final String serviceName;
    private final int failureThreshold;
    private final long resetTimeoutMillis;
    private State state = State.CLOSED;
    private int failureCount = 0;
    private long lastFailureTime = 0;
    
    public CircuitBreaker(String serviceName, int failureThreshold, long resetTimeoutMillis) {
        this.serviceName = serviceName;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMillis = resetTimeoutMillis;
    }
    
    public <T> T call(Callable<T> operation) {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > resetTimeoutMillis) {
                state = State.HALF_OPEN;
                System.out.println("Circuit HALF_OPEN - attempting recovery");
            } else {
                throw new RuntimeException("Circuit breaker is OPEN for " + serviceName);
            }
        }
        
        try {
            T result = operation.call();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw new RuntimeException("Service call failed: " + e.getMessage(), e);
        }
    }
    
    private void onSuccess() {
        failureCount = 0;
        if (state == State.HALF_OPEN) {
            state = State.CLOSED;
            System.out.println("Circuit CLOSED - service recovered");
        }
    }
    
    private void onFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        
        if (failureCount >= failureThreshold) {
            state = State.OPEN;
            System.out.println("Circuit OPEN - too many failures (" + failureCount + ")");
        }
    }
    
    public State getState() { return state; }
    public int getFailureCount() { return failureCount; }
}

/**
 * Service Registry for service discovery
 */
class ServiceRegistry {
    private final Map<String, String> services = new ConcurrentHashMap<>();
    
    public void registerService(String serviceName, String url) {
        services.put(serviceName, url);
        System.out.println("Registered service: " + serviceName + " -> " + url);
    }
    
    public String getServiceUrl(String serviceName) {
        String url = services.get(serviceName);
        if (url == null) {
            throw new RuntimeException("Service not found: " + serviceName);
        }
        return url;
    }
    
    public void deregisterService(String serviceName) {
        services.remove(serviceName);
    }
}

/**
 * Request Context for correlation and tracing
 */
class RequestContext {
    private final String correlationId;
    private final String requestId;
    private final LocalDateTime startTime;
    private final Map<String, String> metadata;
    
    public RequestContext(String correlationId) {
        this.correlationId = correlationId;
        this.requestId = UUID.randomUUID().toString();
        this.startTime = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }
    
    public String getCorrelationId() { return correlationId; }
    public String getRequestId() { return requestId; }
    public LocalDateTime getStartTime() { return startTime; }
    public Map<String, String> getMetadata() { return metadata; }
    
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
}

/**
 * REST Controller for service endpoints
 */
@RestController
@RequestMapping("/api")
class ServiceCommunicationController {
    private final SyncCommunicationService syncService;
    private final AsyncCommunicationService asyncService;
    private final MessageBroker broker;
    
    public ServiceCommunicationController(SyncCommunicationService syncService,
                                         AsyncCommunicationService asyncService,
                                         MessageBroker broker) {
        this.syncService = syncService;
        this.asyncService = asyncService;
        this.broker = broker;
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String userId) {
        Map<String, Object> user = syncService.getUser(userId);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> order = syncService.createOrder(request);
        
        // Publish event asynchronously
        asyncService.publishEvent(broker, "order.created", order);
        
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/events/{eventType}")
    public ResponseEntity<String> publishEvent(@PathVariable String eventType,
                                               @RequestBody Map<String, Object> data) {
        asyncService.publishEvent(broker, eventType, data);
        return ResponseEntity.ok("Event published: " + eventType);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(health);
    }
}
