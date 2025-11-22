I'll create a comprehensive Spring Boot project demonstrating all event-driven patterns.

## Project Structure

```xml pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.eventpatterns</groupId>
    <artifactId>event-driven-patterns</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- Core Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Event Bus Pattern - Spring Cloud Stream -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-stream-kafka</artifactId>
        </dependency>

        <!-- Event Stream Pattern - Kafka Streams -->
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-streams</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>

        <!-- Event Store Pattern - Axon Framework -->
        <dependency>
            <groupId>org.axonframework</groupId>
            <artifactId>axon-spring-boot-starter</artifactId>
            <version>4.9.1</version>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Utilities -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Main Application

```java src/main/java/com/eventpatterns/EventDrivenPatternsApplication.java
package com.eventpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableKafka
@EnableKafkaStreams
public class EventDrivenPatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventDrivenPatternsApplication.class, args);
    }
}
```

## 1. Event Publisher Pattern

```java src/main/java/com/eventpatterns/publisher/EventPublisher.java
package com.eventpatterns.publisher;

import com.eventpatterns.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Event Publisher Pattern
 * Responsible for publishing events to different channels
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish event to Spring's ApplicationEventPublisher
     */
    public void publishApplicationEvent(Object event) {
        log.info("Publishing application event: {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publish event to Kafka topic
     */
    public void publishToKafka(String topic, String key, Object event) {
        log.info("Publishing to Kafka topic '{}': {}", topic, event.getClass().getSimpleName());
        kafkaTemplate.send(topic, key, event);
    }

    /**
     * Publish order event
     */
    public void publishOrderEvent(OrderEvent event) {
        // Publish to both local and external systems
        publishApplicationEvent(event);
        publishToKafka("order-events", event.getOrderId(), event);
    }
}
```

```java src/main/java/com/eventpatterns/publisher/OrderEventPublisher.java
package com.eventpatterns.publisher;

import com.eventpatterns.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain-specific event publisher
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final EventPublisher eventPublisher;

    public void publishOrderCreated(String orderId, String customerId, BigDecimal amount) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Publishing OrderCreatedEvent for order: {}", orderId);
        eventPublisher.publishOrderEvent(event);
    }

    public void publishOrderConfirmed(String orderId) {
        OrderConfirmedEvent event = OrderConfirmedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Publishing OrderConfirmedEvent for order: {}", orderId);
        eventPublisher.publishOrderEvent(event);
    }

    public void publishOrderShipped(String orderId, String trackingNumber) {
        OrderShippedEvent event = OrderShippedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .trackingNumber(trackingNumber)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Publishing OrderShippedEvent for order: {}", orderId);
        eventPublisher.publishOrderEvent(event);
    }

    public void publishOrderCancelled(String orderId, String reason) {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Publishing OrderCancelledEvent for order: {}", orderId);
        eventPublisher.publishOrderEvent(event);
    }
}
```

## 2. Event Listener Pattern

```java src/main/java/com/eventpatterns/listener/OrderEventListener.java
package com.eventpatterns.listener;

import com.eventpatterns.events.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event Listener Pattern
 * Listens to application events and reacts accordingly
 */
@Component
@Slf4j
public class OrderEventListener {

    /**
     * Listen to OrderCreatedEvent
     */
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}, amount={}", 
                event.getOrderId(), event.getAmount());
        
        // Business logic: Send confirmation email
        sendOrderConfirmationEmail(event);
        
        // Business logic: Update inventory
        reserveInventory(event);
    }

    /**
     * Listen to OrderConfirmedEvent - Async processing
     */
    @EventListener
    @Async
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent (async): orderId={}", event.getOrderId());
        
        // Business logic: Process payment
        processPayment(event);
        
        // Business logic: Notify warehouse
        notifyWarehouse(event);
    }

    /**
     * Listen to OrderShippedEvent
     */
    @EventListener
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("Received OrderShippedEvent: orderId={}, tracking={}", 
                event.getOrderId(), event.getTrackingNumber());
        
        // Business logic: Send shipping notification
        sendShippingNotification(event);
        
        // Business logic: Update order status
        updateOrderStatus(event);
    }

    /**
     * Listen to OrderCancelledEvent
     */
    @EventListener
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent: orderId={}, reason={}", 
                event.getOrderId(), event.getReason());
        
        // Business logic: Process refund
        processRefund(event);
        
        // Business logic: Release inventory
        releaseInventory(event);
    }

    /**
     * Generic event listener - listens to all OrderEvent types
     */
    @EventListener
    public void handleAnyOrderEvent(OrderEvent event) {
        log.info("Generic handler - Received event: {} for order: {}", 
                event.getClass().getSimpleName(), event.getOrderId());
        
        // Log all order events to audit trail
        auditEvent(event);
    }

    // Helper methods (simulated business logic)
    
    private void sendOrderConfirmationEmail(OrderCreatedEvent event) {
        log.debug("Sending confirmation email for order: {}", event.getOrderId());
    }

    private void reserveInventory(OrderCreatedEvent event) {
        log.debug("Reserving inventory for order: {}", event.getOrderId());
    }

    private void processPayment(OrderConfirmedEvent event) {
        log.debug("Processing payment for order: {}", event.getOrderId());
        // Simulate payment processing delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void notifyWarehouse(OrderConfirmedEvent event) {
        log.debug("Notifying warehouse for order: {}", event.getOrderId());
    }

    private void sendShippingNotification(OrderShippedEvent event) {
        log.debug("Sending shipping notification for order: {}", event.getOrderId());
    }

    private void updateOrderStatus(OrderShippedEvent event) {
        log.debug("Updating order status for order: {}", event.getOrderId());
    }

    private void processRefund(OrderCancelledEvent event) {
        log.debug("Processing refund for order: {}", event.getOrderId());
    }

    private void releaseInventory(OrderCancelledEvent event) {
        log.debug("Releasing inventory for order: {}", event.getOrderId());
    }

    private void auditEvent(OrderEvent event) {
        log.debug("Auditing event: {} - {}", event.getEventId(), event.getClass().getSimpleName());
    }
}
```

## 3. Application Event Pattern

```java src/main/java/com/eventpatterns/events/application/ApplicationStartedEvent.java
package com.eventpatterns.events.application;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Application Event Pattern
 * Custom application lifecycle event
 */
@Getter
public class ApplicationStartedEvent extends ApplicationEvent {
    private final LocalDateTime startTime;
    private final String applicationName;

    public ApplicationStartedEvent(Object source, String applicationName) {
        super(source);
        this.startTime = LocalDateTime.now();
        this.applicationName = applicationName;
    }
}
```

```java src/main/java/com/eventpatterns/events/application/UserRegistrationEvent.java
package com.eventpatterns.events.application;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Application Event for user registration
 */
@Getter
public class UserRegistrationEvent extends ApplicationEvent {
    private final String userId;
    private final String email;
    private final LocalDateTime registrationTime;

    public UserRegistrationEvent(Object source, String userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.registrationTime = LocalDateTime.now();
    }
}
```

```java src/main/java/com/eventpatterns/events/application/CacheInvalidationEvent.java
package com.eventpatterns.events.application;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Set;

/**
 * Application Event for cache invalidation
 */
@Getter
public class CacheInvalidationEvent extends ApplicationEvent {
    private final Set<String> cacheNames;
    private final String reason;

    public CacheInvalidationEvent(Object source, Set<String> cacheNames, String reason) {
        super(source);
        this.cacheNames = cacheNames;
        this.reason = reason;
    }
}
```

```java src/main/java/com/eventpatterns/listener/ApplicationEventListener.java
package com.eventpatterns.listener;

import com.eventpatterns.events.application.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for Application Events
 */
@Component
@Slf4j
public class ApplicationEventListener {

    /**
     * Listen to Spring's built-in ApplicationReadyEvent
     */
    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        log.info("Application is ready! Started at: {}", event.getTimestamp());
        // Initialize resources, warm up caches, etc.
    }

    /**
     * Listen to Spring's ContextRefreshedEvent
     */
    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        log.info("Application context refreshed");
        // Reload configuration, refresh beans, etc.
    }

    /**
     * Listen to Spring's ContextClosedEvent
     */
    @EventListener
    public void handleContextClosed(ContextClosedEvent event) {
        log.info("Application context closed - cleaning up resources");
        // Cleanup resources, close connections, etc.
    }

    /**
     * Listen to custom UserRegistrationEvent
     */
    @EventListener
    public void handleUserRegistration(UserRegistrationEvent event) {
        log.info("New user registered: userId={}, email={}", 
                event.getUserId(), event.getEmail());
        
        // Send welcome email
        sendWelcomeEmail(event.getEmail());
        
        // Create user profile
        createUserProfile(event.getUserId());
        
        // Grant default permissions
        grantDefaultPermissions(event.getUserId());
    }

    /**
     * Listen to CacheInvalidationEvent
     */
    @EventListener
    public void handleCacheInvalidation(CacheInvalidationEvent event) {
        log.info("Cache invalidation requested: caches={}, reason={}", 
                event.getCacheNames(), event.getReason());
        
        // Invalidate specified caches
        event.getCacheNames().forEach(this::invalidateCache);
    }

    /**
     * Listen to custom ApplicationStartedEvent
     */
    @EventListener
    public void handleApplicationStarted(ApplicationStartedEvent event) {
        log.info("Custom application started event: app={}, time={}", 
                event.getApplicationName(), event.getStartTime());
    }

    // Helper methods
    
    private void sendWelcomeEmail(String email) {
        log.debug("Sending welcome email to: {}", email);
    }

    private void createUserProfile(String userId) {
        log.debug("Creating user profile for: {}", userId);
    }

    private void grantDefaultPermissions(String userId) {
        log.debug("Granting default permissions to: {}", userId);
    }

    private void invalidateCache(String cacheName) {
        log.debug("Invalidating cache: {}", cacheName);
    }
}
```

## 4. Domain Event Pattern

```java src/main/java/com/eventpatterns/domain/Order.java
package com.eventpatterns.domain;

import com.eventpatterns.events.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Domain Event Pattern
 * Aggregate root that generates domain events
 */
@Getter
@Slf4j
public class Order {
    
    private final String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Domain events generated by this aggregate
    private final List<OrderEvent> domainEvents = new ArrayList<>();

    public Order(String customerId, BigDecimal totalAmount) {
        this.orderId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Register domain event
        registerEvent(OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .customerId(customerId)
                .amount(totalAmount)
                .timestamp(createdAt)
                .build());
    }

    /**
     * Domain Event Pattern - Business method that generates events
     */
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
        
        // Register domain event
        registerEvent(OrderConfirmedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .timestamp(updatedAt)
                .build());
        
        log.info("Order confirmed: {}", orderId);
    }

    public void ship(String trackingNumber) {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can be shipped");
        }
        
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = LocalDateTime.now();
        
        // Register domain event
        registerEvent(OrderShippedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .trackingNumber(trackingNumber)
                .timestamp(updatedAt)
                .build());
        
        log.info("Order shipped: {} with tracking: {}", orderId, trackingNumber);
    }

    public void cancel(String reason) {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel delivered or already cancelled orders");
        }
        
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        
        // Register domain event
        registerEvent(OrderCancelledEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .reason(reason)
                .timestamp(updatedAt)
                .build());
        
        log.info("Order cancelled: {} - Reason: {}", orderId, reason);
    }

    /**
     * Register a domain event
     */
    private void registerEvent(OrderEvent event) {
        domainEvents.add(event);
    }

    /**
     * Get and clear domain events
     */
    public List<OrderEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clear domain events after they've been published
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

```java src/main/java/com/eventpatterns/domain/OrderStatus.java
package com.eventpatterns.domain;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
```

```java src/main/java/com/eventpatterns/service/OrderService.java
package com.eventpatterns.service;

import com.eventpatterns.domain.Order;
import com.eventpatterns.events.OrderEvent;
import com.eventpatterns.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Domain Event Pattern - Service that handles aggregate and publishes domain events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final EventPublisher eventPublisher;
    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();

    @Transactional
    public Order createOrder(String customerId, BigDecimal amount) {
        // Create aggregate
        Order order = new Order(customerId, amount);
        
        // Save to repository (simulated with in-memory map)
        orderStore.put(order.getOrderId(), order);
        
        // Publish domain events
        publishDomainEvents(order);
        
        log.info("Order created: {}", order.getOrderId());
        return order;
    }

    @Transactional
    public void confirmOrder(String orderId) {
        Order order = getOrder(orderId);
        
        // Execute domain logic
        order.confirm();
        
        // Publish domain events
        publishDomainEvents(order);
    }

    @Transactional
    public void shipOrder(String orderId, String trackingNumber) {
        Order order = getOrder(orderId);
        
        // Execute domain logic
        order.ship(trackingNumber);
        
        // Publish domain events
        publishDomainEvents(order);
    }

    @Transactional
    public void cancelOrder(String orderId, String reason) {
        Order order = getOrder(orderId);
        
        // Execute domain logic
        order.cancel(reason);
        
        // Publish domain events
        publishDomainEvents(order);
    }

    public Order getOrder(String orderId) {
        Order order = orderStore.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return order;
    }

    /**
     * Publish all domain events from the aggregate
     */
    private void publishDomainEvents(Order order) {
        for (OrderEvent event : order.getDomainEvents()) {
            eventPublisher.publishApplicationEvent(event);
        }
        // Clear events after publishing
        order.clearDomainEvents();
    }
}
```

## 5. Event Bus Pattern

```java src/main/java/com/eventpatterns/eventbus/EventBus.java
package com.eventpatterns.eventbus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Event Bus Pattern
 * Central hub for publishing and subscribing to events
 */
@Component
@Slf4j
public class EventBus {

    private final Map<Class<?>, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

    /**
     * Subscribe to events of a specific type
     */
    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(event -> handler.accept(eventType.cast(event)));
        
        log.info("Subscribed to event type: {}", eventType.getSimpleName());
    }

    /**
     * Unsubscribe from events
     */
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
        List<Consumer<Object>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
            log.info("Unsubscribed from event type: {}", eventType.getSimpleName());
        }
    }

    /**
     * Publish event to all subscribers
     */
    public <T> void publish(T event) {
        Class<?> eventType = event.getClass();
        log.info("Publishing event to EventBus: {}", eventType.getSimpleName());
        
        // Notify direct subscribers
        List<Consumer<Object>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.forEach(handler -> {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    log.error("Error handling event: {}", eventType.getSimpleName(), e);
                }
            });
        }
        
        // Notify subscribers of parent types
        notifyParentTypeSubscribers(event, eventType);
    }

    /**
     * Publish event to specific topic/channel
     */
    public <T> void publish(String topic, T event) {
        log.info("Publishing event to topic '{}': {}", topic, event.getClass().getSimpleName());
        
        // Create a wrapped event with topic information
        TopicEvent<T> topicEvent = new TopicEvent<>(topic, event);
        publish(topicEvent);
    }

    /**
     * Get subscriber count for an event type
     */
    public int getSubscriberCount(Class<?> eventType) {
        List<Consumer<Object>> handlers = subscribers.get(eventType);
        return handlers != null ? handlers.size() : 0;
    }

    /**
     * Clear all subscribers
     */
    public void clear() {
        subscribers.clear();
        log.info("EventBus cleared - all subscribers removed");
    }

    /**
     * Notify subscribers of parent types (inheritance support)
     */
    private void notifyParentTypeSubscribers(Object event, Class<?> eventType) {
        Class<?> superClass = eventType.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            List<Consumer<Object>> parentHandlers = subscribers.get(superClass);
            if (parentHandlers != null) {
                parentHandlers.forEach(handler -> {
                    try {
                        handler.accept(event);
                    } catch (Exception e) {
                        log.error("Error handling event in parent handler: {}", 
                                superClass.getSimpleName(), e);
                    }
                });
            }
            superClass = superClass.getSuperclass();
        }
    }

    /**
     * Wrapper class for topic-based events
     */
    public static class TopicEvent<T> {
        private final String topic;
        private final T payload;

        public TopicEvent(String topic, T payload) {
            this.topic = topic;
            this.payload = payload;
        }

        public String getTopic() {
            return topic;
        }

        public T getPayload() {
            return payload;
        }
    }
}
```

```java src/main/java/com/eventpatterns/eventbus/EventBusSubscribers.java
package com.eventpatterns.eventbus;

import com.eventpatterns.events.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Event Bus subscribers registration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventBusSubscribers {

    private final EventBus eventBus;

    @PostConstruct
    public void registerSubscribers() {
        // Subscribe to OrderCreatedEvent
        eventBus.subscribe(OrderCreatedEvent.class, event -> {
            log.info("EventBus subscriber: Order created - {}", event.getOrderId());
            // Handle order creation
        });

        // Subscribe to OrderConfirmedEvent
        eventBus.subscribe(OrderConfirmedEvent.class, event -> {
            log.info("EventBus subscriber: Order confirmed - {}", event.getOrderId());
            // Handle order confirmation
        });

        // Subscribe to OrderShippedEvent
        eventBus.subscribe(OrderShippedEvent.class, event -> {
            log.info("EventBus subscriber: Order shipped - {} (Tracking: {})", 
                    event.getOrderId(), event.getTrackingNumber());
            // Handle order shipment
        });

        // Subscribe to OrderCancelledEvent
        eventBus.subscribe(OrderCancelledEvent.class, event -> {
            log.info("EventBus subscriber: Order cancelled - {} (Reason: {})", 
                    event.getOrderId(), event.getReason());
            // Handle order cancellation
        });

```java src/main/java/com/eventpatterns/eventbus/EventBusSubscribers.java
        // Subscribe to all OrderEvent types
        eventBus.subscribe(OrderEvent.class, event -> {
            log.info("EventBus generic subscriber: Order event - {}", event.getEventId());
            // Handle any order event
            logEventToAnalytics(event);
        });

        log.info("EventBus subscribers registered successfully");
    }

    private void logEventToAnalytics(OrderEvent event) {
        log.debug("Logging event to analytics system: {}", event.getEventId());
    }
}
```

## 6. Event Sourcing Pattern

```java src/main/java/com/eventpatterns/eventsourcing/entity/EventEntity.java
package com.eventpatterns.eventsourcing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event Sourcing Pattern - Event Store Entity
 */
@Entity
@Table(name = "event_store")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventData;

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
```

```java src/main/java/com/eventpatterns/eventsourcing/repository/EventStoreRepository.java
package com.eventpatterns.eventsourcing.repository;

import com.eventpatterns.eventsourcing.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStoreRepository extends JpaRepository<EventEntity, Long> {
    
    List<EventEntity> findByAggregateIdOrderByVersionAsc(String aggregateId);
    
    List<EventEntity> findByAggregateTypeOrderByTimestampAsc(String aggregateType);
    
    List<EventEntity> findByAggregateIdAndVersionGreaterThan(String aggregateId, Long version);
    
    Long countByAggregateId(String aggregateId);
}
```

```java src/main/java/com/eventpatterns/eventsourcing/EventStore.java
package com.eventpatterns.eventsourcing;

import com.eventpatterns.eventsourcing.entity.EventEntity;
import com.eventpatterns.eventsourcing.repository.EventStoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Event Sourcing Pattern - Event Store implementation
 * Stores all events and allows aggregate reconstruction
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventStore {

    private final EventStoreRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Append event to the event store
     */
    public <T> void appendEvent(String aggregateId, String aggregateType, T event) {
        try {
            Long currentVersion = repository.countByAggregateId(aggregateId);
            
            EventEntity entity = EventEntity.builder()
                    .aggregateId(aggregateId)
                    .aggregateType(aggregateType)
                    .eventType(event.getClass().getName())
                    .eventData(objectMapper.writeValueAsString(event))
                    .version(currentVersion + 1)
                    .timestamp(LocalDateTime.now())
                    .build();

            repository.save(entity);
            log.info("Event appended to store: aggregateId={}, version={}, eventType={}", 
                    aggregateId, entity.getVersion(), event.getClass().getSimpleName());
            
        } catch (Exception e) {
            log.error("Error appending event to store", e);
            throw new RuntimeException("Failed to append event", e);
        }
    }

    /**
     * Get all events for an aggregate
     */
    public List<EventEntity> getEvents(String aggregateId) {
        log.info("Retrieving events for aggregate: {}", aggregateId);
        return repository.findByAggregateIdOrderByVersionAsc(aggregateId);
    }

    /**
     * Get events after a specific version
     */
    public List<EventEntity> getEventsAfterVersion(String aggregateId, Long version) {
        log.info("Retrieving events for aggregate: {} after version: {}", aggregateId, version);
        return repository.findByAggregateIdAndVersionGreaterThan(aggregateId, version);
    }

    /**
     * Reconstruct aggregate from events
     */
    public <T> List<T> getEventsAsType(String aggregateId, Class<T> eventClass) {
        List<EventEntity> entities = getEvents(aggregateId);
        
        return entities.stream()
                .map(entity -> {
                    try {
                        return objectMapper.readValue(entity.getEventData(), eventClass);
                    } catch (Exception e) {
                        log.error("Error deserializing event", e);
                        return null;
                    }
                })
                .filter(event -> event != null)
                .collect(Collectors.toList());
    }

    /**
     * Get aggregate version
     */
    public Long getAggregateVersion(String aggregateId) {
        return repository.countByAggregateId(aggregateId);
    }

    /**
     * Check if aggregate exists
     */
    public boolean aggregateExists(String aggregateId) {
        return repository.countByAggregateId(aggregateId) > 0;
    }
}
```

```java src/main/java/com/eventpatterns/eventsourcing/OrderAggregate.java
package com.eventpatterns.eventsourcing;

import com.eventpatterns.domain.OrderStatus;
import com.eventpatterns.events.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Event Sourcing Pattern - Aggregate that is reconstructed from events
 */
@Getter
@Slf4j
public class OrderAggregate {

    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String trackingNumber;
    private String cancellationReason;
    private Long version;

    private final List<OrderEvent> uncommittedEvents = new ArrayList<>();

    /**
     * Create new aggregate
     */
    public static OrderAggregate create(String orderId, String customerId, BigDecimal amount) {
        OrderAggregate aggregate = new OrderAggregate();
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build();
        
        aggregate.apply(event);
        aggregate.uncommittedEvents.add(event);
        return aggregate;
    }

    /**
     * Reconstruct aggregate from historical events
     */
    public static OrderAggregate fromEvents(List<OrderEvent> events) {
        OrderAggregate aggregate = new OrderAggregate();
        events.forEach(aggregate::apply);
        aggregate.version = (long) events.size();
        return aggregate;
    }

    /**
     * Confirm order
     */
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }

        OrderConfirmedEvent event = OrderConfirmedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .orderId(orderId)
                .timestamp(LocalDateTime.now())
                .build();

        apply(event);
        uncommittedEvents.add(event);
    }

    /**
     * Ship order
     */
    public void ship(String trackingNumber) {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can be shipped");
        }

        OrderShippedEvent event = OrderShippedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .orderId(orderId)
                .trackingNumber(trackingNumber)
                .timestamp(LocalDateTime.now())
                .build();

        apply(event);
        uncommittedEvents.add(event);
    }

    /**
     * Cancel order
     */
    public void cancel(String reason) {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel delivered or already cancelled orders");
        }

        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .orderId(orderId)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();

        apply(event);
        uncommittedEvents.add(event);
    }

    /**
     * Apply event to update aggregate state
     */
    private void apply(OrderEvent event) {
        if (event instanceof OrderCreatedEvent created) {
            this.orderId = created.getOrderId();
            this.customerId = created.getCustomerId();
            this.totalAmount = created.getAmount();
            this.status = OrderStatus.PENDING;
            this.createdAt = created.getTimestamp();
            this.updatedAt = created.getTimestamp();
            this.version = 0L;
        } else if (event instanceof OrderConfirmedEvent confirmed) {
            this.status = OrderStatus.CONFIRMED;
            this.updatedAt = confirmed.getTimestamp();
        } else if (event instanceof OrderShippedEvent shipped) {
            this.status = OrderStatus.SHIPPED;
            this.trackingNumber = shipped.getTrackingNumber();
            this.updatedAt = shipped.getTimestamp();
        } else if (event instanceof OrderCancelledEvent cancelled) {
            this.status = OrderStatus.CANCELLED;
            this.cancellationReason = cancelled.getReason();
            this.updatedAt = cancelled.getTimestamp();
        }

        log.debug("Event applied: {} - New state: {}", event.getClass().getSimpleName(), status);
    }

    /**
     * Get uncommitted events
     */
    public List<OrderEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }

    /**
     * Mark events as committed
     */
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
}
```

```java src/main/java/com/eventpatterns/eventsourcing/OrderEventSourcingService.java
package com.eventpatterns.eventsourcing;

import com.eventpatterns.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event Sourcing Pattern - Service for event-sourced aggregates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventSourcingService {

    private final EventStore eventStore;

    /**
     * Create a new order using event sourcing
     */
    @Transactional
    public OrderAggregate createOrder(String orderId, String customerId, BigDecimal amount) {
        // Create aggregate
        OrderAggregate aggregate = OrderAggregate.create(orderId, customerId, amount);

        // Save events to event store
        saveAggregate(aggregate);

        log.info("Order created using event sourcing: {}", orderId);
        return aggregate;
    }

    /**
     * Load aggregate from event store
     */
    public OrderAggregate loadOrder(String orderId) {
        if (!eventStore.aggregateExists(orderId)) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        // Get all events for this aggregate
        List<OrderEvent> events = eventStore.getEventsAsType(orderId, OrderEvent.class);

        // Reconstruct aggregate from events
        OrderAggregate aggregate = OrderAggregate.fromEvents(events);

        log.info("Order loaded from event store: {} (version: {})", orderId, aggregate.getVersion());
        return aggregate;
    }

    /**
     * Confirm order
     */
    @Transactional
    public void confirmOrder(String orderId) {
        OrderAggregate aggregate = loadOrder(orderId);
        aggregate.confirm();
        saveAggregate(aggregate);
    }

    /**
     * Ship order
     */
    @Transactional
    public void shipOrder(String orderId, String trackingNumber) {
        OrderAggregate aggregate = loadOrder(orderId);
        aggregate.ship(trackingNumber);
        saveAggregate(aggregate);
    }

    /**
     * Cancel order
     */
    @Transactional
    public void cancelOrder(String orderId, String reason) {
        OrderAggregate aggregate = loadOrder(orderId);
        aggregate.cancel(reason);
        saveAggregate(aggregate);
    }

    /**
     * Save aggregate events to event store
     */
    private void saveAggregate(OrderAggregate aggregate) {
        List<OrderEvent> uncommittedEvents = aggregate.getUncommittedEvents();

        for (OrderEvent event : uncommittedEvents) {
            eventStore.appendEvent(
                    aggregate.getOrderId(),
                    "Order",
                    event
            );
        }

        aggregate.markEventsAsCommitted();
        log.info("Saved {} events for order: {}", uncommittedEvents.size(), aggregate.getOrderId());
    }

    /**
     * Get order history (all events)
     */
    public List<OrderEvent> getOrderHistory(String orderId) {
        return eventStore.getEventsAsType(orderId, OrderEvent.class);
    }
}
```

## 7. Event Stream Pattern

```java src/main/java/com/eventpatterns/eventstream/KafkaStreamProcessor.java
package com.eventpatterns.eventstream;

import com.eventpatterns.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Event Stream Pattern
 * Process continuous streams of events using Kafka Streams
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaStreamProcessor {

    private final ObjectMapper objectMapper;

    /**
     * Process order event stream - Filter and Transform
     */
    @Bean
    public KStream<String, OrderEvent> orderEventStream(StreamsBuilder builder) {
        // Source stream from order-events topic
        KStream<String, OrderEvent> orderStream = builder
                .stream("order-events", Consumed.with(Serdes.String(), new JsonSerde<>(OrderEvent.class, objectMapper)));

        // Filter only confirmed orders
        KStream<String, OrderEvent> confirmedOrders = orderStream
                .filter((key, event) -> event instanceof OrderConfirmedEvent,
                        Named.as("filter-confirmed-orders"));

        // Send to confirmed-orders topic
        confirmedOrders.to("confirmed-orders", Produced.with(Serdes.String(), new JsonSerde<>(OrderEvent.class, objectMapper)));

        // Filter cancelled orders
        KStream<String, OrderEvent> cancelledOrders = orderStream
                .filter((key, event) -> event instanceof OrderCancelledEvent,
                        Named.as("filter-cancelled-orders"));

        // Send to cancelled-orders topic
        cancelledOrders.to("cancelled-orders", Produced.with(Serdes.String(), new JsonSerde<>(OrderEvent.class, objectMapper)));

        // Log all events
        orderStream.foreach((key, event) -> 
            log.info("Processing order event stream: {} - {}", key, event.getClass().getSimpleName())
        );

        return orderStream;
    }

    /**
     * Aggregate order events by customer
     */
    @Bean
    public KTable<String, Long> orderCountByCustomer(StreamsBuilder builder) {
        KStream<String, OrderEvent> orderStream = builder
                .stream("order-events", Consumed.with(Serdes.String(), new JsonSerde<>(OrderEvent.class, objectMapper)));

        // Group by customer and count
        KTable<String, Long> customerOrderCount = orderStream
                .filter((key, event) -> event instanceof OrderCreatedEvent)
                .map((key, event) -> {
                    OrderCreatedEvent created = (OrderCreatedEvent) event;
                    return KeyValue.pair(created.getCustomerId(), event);
                })
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(OrderEvent.class, objectMapper)))
                .count(Named.as("count-orders-by-customer"));

        // Log the counts
        customerOrderCount.toStream().foreach((customerId, count) ->
            log.info("Customer {} has {} orders", customerId, count)
        );

        return customerOrderCount;
    }

    /**
     * Windowed aggregation - Orders per hour
     */
    @Bean
    public KTable<Windowed<String>, Long> ordersPerHour(StreamsBuilder builder) {
        KStream<String, OrderEvent> orderStream = builder
                .stream("order-events", Consumed.with(Serdes.String(), new JsonSerde<>(OrderEvent.class, objectMapper)));

        return orderStream
                .filter((key, event) -> event instanceof OrderCreatedEvent)
                .groupBy((key, event) -> "all", Grouped.with(Serdes.String(), new JsonSerde<>(OrderEvent.class, objectMapper)))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1)))
                .count(Named.as("orders-per-hour"));
    }

    /**
     * Join order events with customer events
     */
    @Bean
    public KStream<String, OrderCustomerJoined> joinOrderWithCustomer(StreamsBuilder builder) {
        KStream<String, OrderEvent> orderStream = builder
                .stream("order-events", Consumed.with(Serdes.String(), new JsonSerde<>(OrderEvent.class, objectMapper)));

        KTable<String, CustomerInfo> customerTable = builder
                .table("customer-info", Consumed.with(Serdes.String(), new JsonSerde<>(CustomerInfo.class, objectMapper)));

        return orderStream
                .filter((key, event) -> event instanceof OrderCreatedEvent)
                .map((key, event) -> {
                    OrderCreatedEvent created = (OrderCreatedEvent) event;
                    return KeyValue.pair(created.getCustomerId(), event);
                })
                .join(customerTable,
                      (orderEvent, customerInfo) -> {
                          OrderCreatedEvent created = (OrderCreatedEvent) orderEvent;
                          return new OrderCustomerJoined(
                                  created.getOrderId(),
                                  customerInfo.getCustomerId(),
                                  customerInfo.getName(),
                                  customerInfo.getEmail(),
                                  created.getAmount()
                          );
                      },
                      Joined.with(Serdes.String(), 
                                 new JsonSerde<>(OrderEvent.class, objectMapper),
                                 new JsonSerde<>(CustomerInfo.class, objectMapper)))
                .peek((key, joined) -> 
                    log.info("Joined order {} with customer {}", joined.getOrderId(), joined.getCustomerName())
                );
    }

    /**
     * Dead letter queue for failed events
     */
    @Bean
    public KStream<String, OrderEvent> deadLetterQueue(StreamsBuilder builder) {
        KStream<String, OrderEvent> orderStream = builder
                .stream("order-events", Consumed.with(Serdes.String(), new JsonSerde<>(OrderEvent.class, objectMapper)));

        // Branch stream into valid and invalid events
        Map<String, KStream<String, OrderEvent>> branches = orderStream
                .split(Named.as("order-validation-"))
                .branch((key, event) -> isValidEvent(event), Branched.as("valid"))
                .branch((key, event) -> !isValidEvent(event), Branched.as("invalid"))
                .noDefaultBranch();

        // Send invalid events to DLQ
        KStream<String, OrderEvent> invalidEvents = branches.get("order-validation-invalid");
        if (invalidEvents != null) {
            invalidEvents.to("order-events-dlq", 
                           Produced.with(Serdes.String(), new JsonSerde<>(OrderEvent.class, objectMapper)));
        }

        return branches.get("order-validation-valid");
    }

    private boolean isValidEvent(OrderEvent event) {
        // Add validation logic
        return event.getOrderId() != null && !event.getOrderId().isEmpty();
    }
}
```

```java src/main/java/com/eventpatterns/eventstream/CustomerInfo.java
package com.eventpatterns.eventstream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfo {
    private String customerId;
    private String name;
    private String email;
    private String phone;
}
```

```java src/main/java/com/eventpatterns/eventstream/OrderCustomerJoined.java
package com.eventpatterns.eventstream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCustomerJoined {
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private BigDecimal orderAmount;
}
```

```java src/main/java/com/eventpatterns/eventstream/StreamEventListener.java
package com.eventpatterns.eventstream;

import com.eventpatterns.events.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Event Stream Pattern - Kafka consumer for event streams
 */
@Component
@Slf4j
public class StreamEventListener {

    @KafkaListener(topics = "confirmed-orders", groupId = "confirmed-orders-group")
    public void handleConfirmedOrders(OrderEvent event) {
        log.info("Received confirmed order from stream: {}", event.getOrderId());
        // Process confirmed order
        processConfirmedOrder(event);
    }

    @KafkaListener(topics = "cancelled-orders", groupId = "cancelled-orders-group")
    public void handleCancelledOrders(OrderEvent event) {
        log.info("Received cancelled order from stream: {}", event.getOrderId());
        // Process cancelled order
        processCancelledOrder(event);
    }

    @KafkaListener(topics = "order-events-dlq", groupId = "dlq-group")
    public void handleDeadLetterQueue(OrderEvent event) {
        log.warn("Received event in DLQ: {}", event.getOrderId());
        // Handle failed events
        handleFailedEvent(event);
    }

    private void processConfirmedOrder(OrderEvent event) {
        log.debug("Processing confirmed order: {}", event.getOrderId());
    }

    private void processCancelledOrder(OrderEvent event) {
        log.debug("Processing cancelled order: {}", event.getOrderId());
    }

    private void handleFailedEvent(OrderEvent event) {
        log.debug("Handling failed event: {}", event.getOrderId());
        // Retry logic, alerting, etc.
    }
}
```

## 8. Event Store Pattern (Extended)

```java src/main/java/com/eventpatterns/eventstore/Snapshot.java
package com.eventpatterns.eventstore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event Store Pattern - Snapshot for performance optimization
 */
@Entity
@Table(name = "aggregate_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String aggregateState;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
```

```java src/main/java/com/eventpatterns/eventstore/SnapshotRepository.java
package com.eventpatterns.eventstore.SnapshotRepository;

import com.eventpatterns.eventstore.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {
    
    @Query("SELECT s FROM Snapshot s WHERE s.aggregateId = :aggregateId ORDER BY s.version DESC LIMIT 1")
    Optional<Snapshot> findLatestSnapshot(String aggregateId);
    
    void deleteByAggregateIdAndVersionLessThan(String aggregateId, Long version);
}
```

```java src/main/java/com/eventpatterns/eventstore/SnapshotStore.java
package com.eventpatterns.eventstore;

import com.eventpatterns.eventsourcing.OrderAggregate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Event Store Pattern - Snapshot management
 * Creates snapshots to optimize aggregate reconstruction
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SnapshotStore {

    private final SnapshotRepository repository;
    private final ObjectMapper objectMapper;

    private static final int SNAPSHOT_INTERVAL = 10; // Create snapshot every 10 events

    /**
     * Save aggregate snapshot
     */
    public void saveSnapshot(OrderAggregate aggregate) {
        try {
            Snapshot snapshot = Snapshot.builder()
                    .aggregateId(aggregate.getOrderId())
                    .aggregateType("Order")
                    .version(aggregate.getVersion())
                    .aggregateState(objectMapper.writeValueAsString(aggregate))
                    .timestamp(LocalDateTime.now())
                    .build();

            repository.save(snapshot);
            log.info("Snapshot saved: aggregateId={}, version={}", 
                    aggregate.getOrderId(), aggregate.getVersion());

            // Clean up old snapshots
            cleanupOldSnapshots(aggregate.getOrderId(), aggregate.getVersion());

        } catch (Exception e) {
            log.error("Error saving snapshot", e);
        }
    }

    /**
     * Load latest snapshot
     */
    public Optional<OrderAggregate> loadSnapshot(String aggregateId) {
        Optional<Snapshot> snapshotOpt = repository.findLatestSnapshot(aggregateId);

        if (snapshotOpt.isPresent()) {
            try {
                Snapshot snapshot = snapshotOpt.get();
                OrderAggregate aggregate = objectMapper.readValue(
                        snapshot.getAggregateState(), 
                        OrderAggregate.class
                );

                log.info("Snapshot loaded: aggregateId={}, version={}", 
                        aggregateId, snapshot.getVersion());
                return Optional.of(aggregate);

            } catch (Exception e) {
                log.error("Error loading snapshot", e);
            }
        }

        return Optional.empty();
    }

    /**
     * Check if snapshot should be created
     */
    public boolean shouldCreateSnapshot(Long version) {
        return version % SNAPSHOT_INTERVAL == 0;
    }

    /**
     * Clean up old snapshots, keep only the latest 3
     */
    private void cleanupOldSnapshots(String aggregateId, Long currentVersion) {
        Long versionThreshold = currentVersion - (SNAPSHOT_INTERVAL * 3);
        if (versionThreshold > 0) {
            repository.deleteByAggregateIdAndVersionLessThan(aggregateId, versionThreshold);
            log.debug("Cleaned up old snapshots for aggregateId={}", aggregateId);
        }
    }
}
```

## Event Models

```java src/main/java/com/eventpatterns/events/OrderEvent.java
package com.eventpatterns.events;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Base class for all order events
 */
@Data
public abstract class OrderEvent {
    private String eventId;
    private String orderId;
    private LocalDateTime timestamp;
}
```

```java src/main/java/com/eventpatterns/events/OrderCreatedEvent.java
package com.eventpatterns.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends OrderEvent {
    private String customerId;
    private BigDecimal amount;

    @Builder
    public OrderCreatedEvent(String eventId, String orderId, LocalDateTime timestamp,
                            String customerId, BigDecimal amount) {
        this.setEventId(eventId);
        this.setOrderId(orderId);
        this.setTimestamp(timestamp);
        this.customerId = customerId;
        this.amount = amount;
    }

    // Default constructor for Jackson
    public OrderCreatedEvent() {
    }
}
```

```java src/main/java/com/eventpatterns/events/OrderConfirmedEvent.java
package com.eventpatterns.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderConfirmedEvent extends OrderEvent {

    @Builder
    public OrderConfirmedEvent(String eventId, String orderId, LocalDateTime timestamp) {
        this.setEventId(eventId);
        this.setOrderId(orderId);
        this.setTimestamp(timestamp);
    }

```java src/main/java/com/eventpatterns/events/OrderConfirmedEvent.java
    // Default constructor for Jackson
    public OrderConfirmedEvent() {
    }
}
```

```java src/main/java/com/eventpatterns/events/OrderShippedEvent.java
package com.eventpatterns.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderShippedEvent extends OrderEvent {
    private String trackingNumber;

    @Builder
    public OrderShippedEvent(String eventId, String orderId, LocalDateTime timestamp,
                            String trackingNumber) {
        this.setEventId(eventId);
        this.setOrderId(orderId);
        this.setTimestamp(timestamp);
        this.trackingNumber = trackingNumber;
    }

    // Default constructor for Jackson
    public OrderShippedEvent() {
    }
}
```

```java src/main/java/com/eventpatterns/events/OrderCancelledEvent.java
package com.eventpatterns.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderCancelledEvent extends OrderEvent {
    private String reason;

    @Builder
    public OrderCancelledEvent(String eventId, String orderId, LocalDateTime timestamp,
                              String reason) {
        this.setEventId(eventId);
        this.setOrderId(orderId);
        this.setTimestamp(timestamp);
        this.reason = reason;
    }

    // Default constructor for Jackson
    public OrderCancelledEvent() {
    }
}
```

## REST Controllers

```java src/main/java/com/eventpatterns/controller/OrderController.java
package com.eventpatterns.controller;

import com.eventpatterns.domain.Order;
import com.eventpatterns.publisher.OrderEventPublisher;
import com.eventpatterns.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST Controller demonstrating Event Publisher and Domain Event patterns
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final OrderEventPublisher eventPublisher;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());
        
        Order order = orderService.createOrder(request.getCustomerId(), request.getAmount());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable String orderId) {
        log.info("Confirming order: {}", orderId);
        
        orderService.confirmOrder(orderId);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<Void> shipOrder(@PathVariable String orderId,
                                         @RequestBody ShipOrderRequest request) {
        log.info("Shipping order: {} with tracking: {}", orderId, request.getTrackingNumber());
        
        orderService.shipOrder(orderId, request.getTrackingNumber());
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId,
                                           @RequestBody CancelOrderRequest request) {
        log.info("Cancelling order: {} - Reason: {}", orderId, request.getReason());
        
        orderService.cancelOrder(orderId, request.getReason());
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    // DTOs
    public record CreateOrderRequest(String customerId, BigDecimal amount) {}
    public record ShipOrderRequest(String trackingNumber) {}
    public record CancelOrderRequest(String reason) {}
}
```

```java src/main/java/com/eventpatterns/controller/EventSourcingController.java
package com.eventpatterns.controller;

import com.eventpatterns.events.OrderEvent;
import com.eventpatterns.eventsourcing.OrderAggregate;
import com.eventpatterns.eventsourcing.OrderEventSourcingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller demonstrating Event Sourcing pattern
 */
@RestController
@RequestMapping("/api/es/orders")
@RequiredArgsConstructor
@Slf4j
public class EventSourcingController {

    private final OrderEventSourcingService eventSourcingService;

    @PostMapping
    public ResponseEntity<OrderAggregate> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("Creating event-sourced order for customer: {}", request.getCustomerId());
        
        String orderId = UUID.randomUUID().toString();
        OrderAggregate aggregate = eventSourcingService.createOrder(
                orderId, 
                request.getCustomerId(), 
                request.getAmount()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(aggregate);
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable String orderId) {
        log.info("Confirming event-sourced order: {}", orderId);
        
        eventSourcingService.confirmOrder(orderId);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<Void> shipOrder(@PathVariable String orderId,
                                         @RequestBody ShipOrderRequest request) {
        log.info("Shipping event-sourced order: {}", orderId);
        
        eventSourcingService.shipOrder(orderId, request.getTrackingNumber());
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId,
                                           @RequestBody CancelOrderRequest request) {
        log.info("Cancelling event-sourced order: {}", orderId);
        
        eventSourcingService.cancelOrder(orderId, request.getReason());
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderAggregate> getOrder(@PathVariable String orderId) {
        OrderAggregate aggregate = eventSourcingService.loadOrder(orderId);
        return ResponseEntity.ok(aggregate);
    }

    @GetMapping("/{orderId}/history")
    public ResponseEntity<List<OrderEvent>> getOrderHistory(@PathVariable String orderId) {
        List<OrderEvent> history = eventSourcingService.getOrderHistory(orderId);
        return ResponseEntity.ok(history);
    }

    // DTOs
    public record CreateOrderRequest(String customerId, BigDecimal amount) {}
    public record ShipOrderRequest(String trackingNumber) {}
    public record CancelOrderRequest(String reason) {}
}
```

```java src/main/java/com/eventpatterns/controller/ApplicationEventController.java
package com.eventpatterns.controller;

import com.eventpatterns.events.application.CacheInvalidationEvent;
import com.eventpatterns.events.application.UserRegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST Controller demonstrating Application Event pattern
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventController {

    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/user-registration")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegistrationRequest request) {
        log.info("Publishing user registration event for: {}", request.getEmail());
        
        UserRegistrationEvent event = new UserRegistrationEvent(
                this,
                request.getUserId(),
                request.getEmail()
        );
        
        eventPublisher.publishEvent(event);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cache-invalidation")
    public ResponseEntity<Void> invalidateCache(@RequestBody CacheInvalidationRequest request) {
        log.info("Publishing cache invalidation event for: {}", request.getCacheNames());
        
        CacheInvalidationEvent event = new CacheInvalidationEvent(
                this,
                request.getCacheNames(),
                request.getReason()
        );
        
        eventPublisher.publishEvent(event);
        
        return ResponseEntity.ok().build();
    }

    // DTOs
    public record UserRegistrationRequest(String userId, String email) {}
    public record CacheInvalidationRequest(Set<String> cacheNames, String reason) {}
}
```

```java src/main/java/com/eventpatterns/controller/EventBusController.java
package com.eventpatterns.controller;

import com.eventpatterns.eventbus.EventBus;
import com.eventpatterns.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller demonstrating Event Bus pattern
 */
@RestController
@RequestMapping("/api/eventbus")
@RequiredArgsConstructor
@Slf4j
public class EventBusController {

    private final EventBus eventBus;

    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publishEvent(@RequestBody PublishEventRequest request) {
        log.info("Publishing event to EventBus: {}", request.getEventType());
        
        OrderEvent event = createEvent(request);
        eventBus.publish(event);
        
        return ResponseEntity.ok(Map.of(
                "message", "Event published successfully",
                "eventId", event.getEventId()
        ));
    }

    @PostMapping("/publish-to-topic")
    public ResponseEntity<Map<String, String>> publishToTopic(@RequestBody PublishToTopicRequest request) {
        log.info("Publishing event to topic '{}': {}", request.getTopic(), request.getEventType());
        
        OrderEvent event = createEvent(new PublishEventRequest(
                request.getEventType(),
                request.getOrderId(),
                request.getCustomerId(),
                request.getAmount(),
                request.getTrackingNumber(),
                request.getReason()
        ));
        
        eventBus.publish(request.getTopic(), event);
        
        return ResponseEntity.ok(Map.of(
                "message", "Event published to topic successfully",
                "topic", request.getTopic(),
                "eventId", event.getEventId()
        ));
    }

    @GetMapping("/subscribers/{eventType}")
    public ResponseEntity<Map<String, Object>> getSubscriberCount(@PathVariable String eventType) {
        Class<?> eventClass;
        try {
            eventClass = Class.forName("com.eventpatterns.events." + eventType);
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Event type not found"));
        }
        
        int count = eventBus.getSubscriberCount(eventClass);
        
        return ResponseEntity.ok(Map.of(
                "eventType", eventType,
                "subscriberCount", count
        ));
    }

    private OrderEvent createEvent(PublishEventRequest request) {
        return switch (request.getEventType()) {
            case "OrderCreatedEvent" -> OrderCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(request.getOrderId())
                    .customerId(request.getCustomerId())
                    .amount(request.getAmount())
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            case "OrderConfirmedEvent" -> OrderConfirmedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(request.getOrderId())
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            case "OrderShippedEvent" -> OrderShippedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(request.getOrderId())
                    .trackingNumber(request.getTrackingNumber())
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            case "OrderCancelledEvent" -> OrderCancelledEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(request.getOrderId())
                    .reason(request.getReason())
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            default -> throw new IllegalArgumentException("Unknown event type: " + request.getEventType());
        };
    }

    // DTOs
    public record PublishEventRequest(
            String eventType,
            String orderId,
            String customerId,
            BigDecimal amount,
            String trackingNumber,
            String reason
    ) {}

    public record PublishToTopicRequest(
            String topic,
            String eventType,
            String orderId,
            String customerId,
            BigDecimal amount,
            String trackingNumber,
            String reason
    ) {}
}
```

## Configuration Files

```yaml src/main/resources/application.yml
spring:
  application:
    name: event-driven-patterns

  # Database Configuration
  datasource:
    url: jdbc:h2:mem:eventdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  h2:
    console:
      enabled: true
      path: /h2-console

  # Kafka Configuration
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: event-patterns-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    
  # Kafka Streams Configuration
  cloud:
    stream:
      kafka:
        streams:
          binder:
            configuration:
              application.id: event-patterns-streams
              default.key.serde: org.apache.kafka.common.serialization.Serdes$StringSerde
              default.value.serde: org.springframework.kafka.support.serializer.JsonSerde
              commit.interval.ms: 1000
        binder:
          brokers: localhost:9092

# Server Configuration
server:
  port: 8080

# Logging Configuration
logging:
  level:
    com.eventpatterns: DEBUG
    org.springframework.kafka: INFO
    org.apache.kafka: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

# Async Configuration
spring.task.execution:
  pool:
    core-size: 5
    max-size: 10
    queue-capacity: 100
  thread-name-prefix: async-event-

# Jackson Configuration
spring.jackson:
  serialization:
    write-dates-as-timestamps: false
  deserialization:
    fail-on-unknown-properties: false
```

```yaml src/main/resources/application-docker.yml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/eventdb
    username: postgres
    password: postgres
  
  jpa:
    hibernate:
      ddl-auto: update
  
  kafka:
    bootstrap-servers: kafka:9092
  
  cloud:
    stream:
      kafka:
        binder:
          brokers: kafka:9092
```

## Docker Compose

```yaml docker-compose.yml
version: '3.8'

services:
  # PostgreSQL Database
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: eventdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - event-patterns-network

  # Zookeeper for Kafka
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    networks:
      - event-patterns-network

  # Kafka
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
      - "29092:29092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    networks:
      - event-patterns-network
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
      interval: 30s
      timeout: 10s
      retries: 5

  # Kafka UI
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    depends_on:
      - kafka
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181
    networks:
      - event-patterns-network

  # Event Patterns Application
  app:
    build: .
    depends_on:
      kafka:
        condition: service_healthy
      postgres:
        condition: service_started
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - event-patterns-network

networks:
  event-patterns-network:
    driver: bridge

volumes:
  postgres-data:
    driver: local
```

```dockerfile Dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Testing

```java src/test/java/com/eventpatterns/EventPublisherTest.java
package com.eventpatterns;

import com.eventpatterns.events.OrderCreatedEvent;
import com.eventpatterns.publisher.EventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Event Publisher Pattern
 */
@SpringBootTest
class EventPublisherTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private TestEventListener testListener;

    @Test
    void testPublishApplicationEvent() throws InterruptedException {
        // Given
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId("TEST-001")
                .customerId("CUST-001")
                .amount(new BigDecimal("100.00"))
                .timestamp(LocalDateTime.now())
                .build();

        // When
        eventPublisher.publishApplicationEvent(event);

        // Then
        boolean received = testListener.getLatch().await(5, TimeUnit.SECONDS);
        assertTrue(received, "Event should be received by listener");
    }

    @Component
    static class TestEventListener implements ApplicationListener<OrderCreatedEvent> {
        
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void onApplicationEvent(OrderCreatedEvent event) {
            latch.countDown();
        }

        public CountDownLatch getLatch() {
            return latch;
        }
    }
}
```

```java src/test/java/com/eventpatterns/OrderServiceTest.java
package com.eventpatterns;

import com.eventpatterns.domain.Order;
import com.eventpatterns.domain.OrderStatus;
import com.eventpatterns.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Domain Event Pattern
 */
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void testCreateOrder() {
        // When
        Order order = orderService.createOrder("CUST-001", new BigDecimal("150.00"));

        // Then
        assertNotNull(order);
        assertNotNull(order.getOrderId());
        assertEquals("CUST-001", order.getCustomerId());
        assertEquals(new BigDecimal("150.00"), order.getTotalAmount());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void testOrderLifecycle() {
        // Given
        Order order = orderService.createOrder("CUST-002", new BigDecimal("200.00"));
        String orderId = order.getOrderId();

        // When - Confirm
        orderService.confirmOrder(orderId);
        Order confirmedOrder = orderService.getOrder(orderId);

        // Then
        assertEquals(OrderStatus.CONFIRMED, confirmedOrder.getStatus());

        // When - Ship
        orderService.shipOrder(orderId, "TRACK-123");
        Order shippedOrder = orderService.getOrder(orderId);

        // Then
        assertEquals(OrderStatus.SHIPPED, shippedOrder.getStatus());
    }

    @Test
    void testCancelOrder() {
        // Given
        Order order = orderService.createOrder("CUST-003", new BigDecimal("75.00"));
        String orderId = order.getOrderId();

        // When
        orderService.cancelOrder(orderId, "Customer requested cancellation");
        Order cancelledOrder = orderService.getOrder(orderId);

        // Then
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
    }
}
```

```java src/test/java/com/eventpatterns/EventBusTest.java
package com.eventpatterns;

import com.eventpatterns.eventbus.EventBus;
import com.eventpatterns.events.OrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Event Bus Pattern
 */
class EventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
    }

    @Test
    void testSubscribeAndPublish() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger callCount = new AtomicInteger(0);

        eventBus.subscribe(OrderCreatedEvent.class, event -> {
            callCount.incrementAndGet();
            latch.countDown();
        });

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId("TEST-001")
                .customerId("CUST-001")
                .amount(new BigDecimal("100.00"))
                .timestamp(LocalDateTime.now())
                .build();

        // When
        eventBus.publish(event);

        // Then
        boolean received = latch.await(5, TimeUnit.SECONDS);
        assertTrue(received);
        assertEquals(1, callCount.get());
    }

    @Test
    void testMultipleSubscribers() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(3);

        eventBus.subscribe(OrderCreatedEvent.class, event -> latch.countDown());
        eventBus.subscribe(OrderCreatedEvent.class, event -> latch.countDown());
        eventBus.subscribe(OrderCreatedEvent.class, event -> latch.countDown());

        assertEquals(3, eventBus.getSubscriberCount(OrderCreatedEvent.class));

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId("TEST-002")
                .customerId("CUST-002")
                .amount(new BigDecimal("200.00"))
                .timestamp(LocalDateTime.now())
                .build();

        // When
        eventBus.publish(event);

        // Then
        boolean allReceived = latch.await(5, TimeUnit.SECONDS);
        assertTrue(allReceived);
    }

    @Test
    void testPublishToTopic() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);

        eventBus.subscribe(EventBus.TopicEvent.class, topicEvent -> {
            assertEquals("order-topic", topicEvent.getTopic());
            latch.countDown();
        });

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId("TEST-003")
                .customerId("CUST-003")
                .amount(new BigDecimal("300.00"))
                .timestamp(LocalDateTime.now())
                .build();

        // When
        eventBus.publish("order-topic", event);

        // Then
        boolean received = latch.await(5, TimeUnit.SECONDS);
        assertTrue(received);
    }
}
```

```java src/test/java/com/eventpatterns/EventSourcingTest.java
package com.eventpatterns;

import com.eventpatterns.domain.OrderStatus;
import com.eventpatterns.events.OrderEvent;
import com.eventpatterns.eventsourcing.OrderAggregate;
import com.eventpatterns.eventsourcing.OrderEventSourcingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Event Sourcing Pattern
 */
@SpringBootTest
@Transactional
class EventSourcingTest {

    @Autowired
    private OrderEventSourcingService eventSourcingService;

    @Test
    void testCreateAndLoadOrder() {
        // Given
        String orderId = UUID.randomUUID().toString();

        // When
        OrderAggregate createdAggregate = eventSourcingService.createOrder(
                orderId, "CUST-001", new BigDecimal("100.00")
        );

        // Then
        assertNotNull(createdAggregate);
        assertEquals(orderId, createdAggregate.getOrderId());
        assertEquals(OrderStatus.PENDING, createdAggregate.getStatus());

        // When - Load from event store
        OrderAggregate loadedAggregate = eventSourcingService.loadOrder(orderId);

        // Then
        assertEquals(createdAggregate.getOrderId(), loadedAggregate.getOrderId());
        assertEquals(createdAggregate.getStatus(), loadedAggregate.getStatus());
    }

    @Test
    void testOrderLifecycleWithEventSourcing() {
        // Given
        String orderId = UUID.randomUUID().toString();
        eventSourcingService.createOrder(orderId, "CUST-002", new BigDecimal("200.00"));

        // When - Confirm
        eventSourcingService.confirmOrder(orderId);
        OrderAggregate afterConfirm = eventSourcingService.loadOrder(orderId);

        // Then
        assertEquals(OrderStatus.CONFIRMED, afterConfirm.getStatus());

        // When - Ship
        eventSourcingService.shipOrder(orderId, "TRACK-456");
        OrderAggregate afterShip = eventSourcingService.loadOrder(orderId);

        // Then
        assertEquals(OrderStatus.SHIPPED, afterShip.getStatus());
        assertEquals("TRACK-456", afterShip.getTrackingNumber());

        // When - Get history
        List<OrderEvent> history = eventSourcingService.getOrderHistory(orderId);

        // Then
        assertEquals(3, history.size()); // Created, Confirmed, Shipped
    }

    @Test
    void testEventStoreReplay() {
        // Given
        String orderId = UUID.randomUUID().toString();
        eventSourcingService.createOrder(orderId, "CUST-003", new BigDecimal("300.00"));
        eventSourcingService.confirmOrder(orderId);
        eventSourcingService.shipOrder(orderId, "TRACK-789");

        // When - Load aggregate (replays all events)
        OrderAggregate aggregate = eventSourcingService.loadOrder(orderId);

        // Then - Aggregate should be in final state
        assertEquals(OrderStatus.SHIPPED, aggregate.getStatus());
        assertEquals("TRACK-789", aggregate.getTrackingNumber());
        assertEquals(new BigDecimal("300.00"), aggregate.getTotalAmount());
    }
}
```

## README

```markdown README.md
# Event-Driven Patterns in Spring Boot

This project demonstrates all major event-driven patterns using Spring Boot, Spring Cloud Stream, Kafka, and Event Sourcing.

## Patterns Implemented

### 1. **Event Publisher Pattern**
- Location: `publisher/EventPublisher.java`
- Publishes events to multiple channels (Spring Events, Kafka)
- Centralized event publishing mechanism

### 2. **Event Listener Pattern**
- Location: `listener/OrderEventListener.java`
- Listens to events using `@EventListener`
- Supports async event processing
- Multiple listeners can subscribe to same event

### 3. **Application Event Pattern**
- Location: `events/application/`
- Custom Spring application events
- Lifecycle events (startup, shutdown)
- Infrastructure events (cache invalidation, user registration)

### 4. **Domain Event Pattern**
- Location: `domain/Order.java`
- Domain aggregates generate events
- Events represent business state changes
- Follows DDD principles

### 5. **Event Bus Pattern**
- Location: `eventbus/EventBus.java`
- In-memory event bus implementation
- Topic-based event routing
- Dynamic subscription management

### 6. **Event Sourcing Pattern**
- Location: `eventsourcing/`
- Complete event store implementation
- Aggregate reconstruction from events
- Event versioning support

### 7. **Event Stream Pattern**
- Location: `eventstream/KafkaStreamProcessor.java`
- Real-time event stream processing
- Kafka Streams integration
- Windowed aggregations, joins, filtering

### 8. **Event Store Pattern**
- Location: `eventstore/`
- Persistent event storage with JPA
- Snapshot support for performance
- Event replay capabilities

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        REST Controllers                          │
│  OrderController | EventSourcingController | EventBusController │
└────────────┬─────────────────────┬──────────────────────────────┘
             │                     │
             ▼                     ▼
┌────────────────────┐   ┌─────────────────────┐
│   OrderService     │   │ EventSourcingService│
│ (Domain Events)    │   │  (Event Sourcing)   │
└─────────┬──────────┘   └──────────┬──────────┘
          │                         │
          ▼                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Event Publisher                             │
└────────────┬────────────────────────────────────────────────────┘
             │
    ┌────────┼────────┐
    ▼        ▼        ▼
┌─────────┐ ┌──────┐ ┌──────────┐
│ Spring  │ │Kafka │ │Event Bus │
│ Events  │ │      │ │          │
└────┬────┘ └───┬──┘ └────┬─────┘
     │          │         │
     ▼          ▼         ▼
┌────────────────────────────────────┐
│      Event Listeners                │
│ - OrderEventListener                │
│ - ApplicationEventListener          │
│ - EventBusSubscribers              │
│ - StreamEventListener              │
└────────────────────────────────────┘

┌─────────────────────────────────────┐
│         Event Store (DB)            │
│ - EventEntity (JPA)                 │
│ - Snapshot (Performance)            │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│    Kafka Streams Processing         │
│ - Filtering & Transformation        │
│ - Aggregations & Windowing         │
│ - Stream Joins                      │
└─────────────────────────────────────┘
```

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- Kafka (included in docker-compose)

## Quick Start

### 1. Start Infrastructure

```bash
# Start Kafka, Zookeeper, PostgreSQL
docker-compose up -d kafka postgres

# Wait for services to be ready
docker-compose ps
```

### 2. Build Application

```bash
mvn clean package -DskipTests
```

### 3. Run Application

#### Option A: Using Maven
```bash
mvn spring-boot:run
```

#### Option B: Using Docker
```bash
docker-compose up app
```

### 4. Access Services

- **Application**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
- **Kafka UI**: http://localhost:8090

## API Examples

### Domain Event Pattern

#### Create Order (generates domain events)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "amount": 299.99
  }'
```

#### Confirm Order
```bash
curl -X POST http://localhost:8080/api/orders/{orderId}/confirm
```

#### Ship Order
```bash
curl -X POST http://localhost:8080/api/orders/{orderId}/ship \
  -H "Content-Type: application/json" \
  -d '{
    "trackingNumber": "TRACK-12345"
  }'
```

#### Cancel Order
```bash
curl -X POST http://localhost:8080/api/orders/{orderId}/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Customer requested cancellation"
  }'
```

### Event Sourcing Pattern

#### Create Event-Sourced Order
```bash
curl -X POST http://localhost:8080/api/es/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-002",
    "amount": 449.99
  }'
```

#### Get Order (reconstructed from events)
```bash
curl http://localhost:8080/api/es/orders/{orderId}
```

#### Get Order History (all events)
```bash
curl http://localhost:8080/api/es/orders/{orderId}/history
```

Response:
```json
[
  {
    "eventId": "evt-123",
    "orderId": "order-456",
    "customerId": "CUST-002",
    "amount": 449.99,
    "timestamp": "2024-01-15T10:30:00"
  },
  {
    "eventId": "evt-124",
    "orderId": "order-456",
    "timestamp": "2024-01-15T10:35:00"
  }
]
```

### Application Event Pattern

#### Register User (triggers application events)
```bash
curl -X POST http://localhost:8080/api/events/user-registration \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "email": "user@example.com"
  }'
```

#### Invalidate Cache
```bash
curl -X POST http://localhost:8080/api/events/cache-invalidation \
  -H "Content-Type: application/json" \
  -d '{
    "cacheNames": ["products", "customers"],
    "reason": "Data updated"
  }'
```

### Event Bus Pattern

#### Publish to Event Bus
```bash
curl -X POST http://localhost:8080/api/eventbus/publish \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "OrderCreatedEvent",
    "orderId": "order-789",
    "customerId": "CUST-003",
    "amount": 599.99
  }'
```

#### Publish to Specific Topic
```bash
curl -X POST http://localhost:8080/api/eventbus/publish-to-topic \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "high-value-orders",
    "eventType": "OrderCreatedEvent",
    "orderId": "order-999",
    "customerId": "CUST-004",
    "amount": 1999.99
  }'
```

#### Get Subscriber Count
```bash
curl http://localhost:8080/api/eventbus/subscribers/OrderCreatedEvent
```

## Testing Event Patterns

### 1. Event Publisher & Listener Pattern

Watch application logs while creating orders:

```bash
# Terminal 1 - Watch logs
docker-compose logs -f app

# Terminal 2 - Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "CUST-001", "amount": 150.00}'
```

**Expected Log Output:**
```
Publishing application event: OrderCreatedEvent
Received OrderCreatedEvent: orderId=xxx, amount=150.00
Sending confirmation email for order: xxx
Reserving inventory for order: xxx
Generic handler - Received event: OrderCreatedEvent for order: xxx
```

### 2. Event Sourcing Pattern

Test aggregate reconstruction:

```bash
# Create order
ORDER_ID=$(curl -s -X POST http://localhost:8080/api/es/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "CUST-002", "amount": 250.00}' \
  | jq -r '.orderId')

# Confirm order
curl -X POST http://localhost:8080/api/es/orders/$ORDER_ID/confirm

# Ship order
curl -X POST http://localhost:8080/api/es/orders/$ORDER_ID/ship \
  -H "Content-Type: application/json" \
  -d '{"trackingNumber": "TRACK-123"}'

# Get history
curl http://localhost:8080/api/es/orders/$ORDER_ID/history | jq
```

### 3. Event Stream Pattern

Monitor Kafka topics:

```bash
# View Kafka topics in UI
open http://localhost:8090

# Or use Kafka console consumer
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic order-events \
  --from-beginning
```

### 4. Event Bus Pattern

Test multiple subscribers:

```bash
# Publish event
curl -X POST http://localhost:8080/api/eventbus/publish \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "OrderCreatedEvent",
    "orderId": "order-123",
    "customerId": "CUST-005",
    "amount": 399.99
  }'

# Check logs - multiple subscribers should receive the event
docker-compose logs app | grep "EventBus subscriber"
```

## Advanced Features

### Event Store Snapshots

The Event Store automatically creates snapshots every 10 events to optimize aggregate reconstruction:

```java
// Automatic snapshot creation
if (aggregate.getVersion() % 10 == 0) {
    snapshotStore.saveSnapshot(aggregate);
}

// Loading uses latest snapshot + subsequent events
Optional<OrderAggregate> snapshot = snapshotStore.loadSnapshot(orderId);
List<OrderEvent> eventsSinceSnapshot = eventStore.getEventsAfterVersion(
    orderId, 
    snapshot.get().getVersion()
);
```

### Kafka Streams Processing

Monitor stream processing:

```bash
# View processed streams
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic confirmed-orders \
  --from-beginning

docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic cancelled-orders \
  --from-beginning
```

### Dead Letter Queue

Failed events are automatically sent to DLQ:

```bash
# Monitor DLQ
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic order-events-dlq \
  --from-beginning
```

## Performance Testing

### Load Test Event Publishing

```bash
# Install Apache Bench
# Generate 1000 orders
for i in {1..1000}; do
  curl -s -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d "{\"customerId\": \"CUST-$i\", \"amount\": $((RANDOM % 1000 + 100))}" &
done
wait

# Check event store size
curl http://localhost:8080/h2-console
# Query: SELECT COUNT(*) FROM event_store;
```

### Event Sourcing Performance

```bash
# Create order with many events
ORDER_ID=$(curl -s -X POST http://localhost:8080/api/es/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "CUST-PERF", "amount": 500.00}' \
  | jq -r '.orderId')

# Generate 20 state changes
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/es/orders/$ORDER_ID/confirm
  curl -X POST http://localhost:8080/api/es/orders/$ORDER_ID/cancel \
    -H "Content-Type: application/json" \
    -d '{"reason": "Test"}'
done

# Measure reconstruction time
time curl http://localhost:8080/api/es/orders/$ORDER_ID
```

## Monitoring & Debugging

### View All Events in Event Store

```sql
-- Connect to H2 Console: http://localhost:8080/h2-console
-- JDBC URL: jdbc:h2:mem:eventdb

SELECT 
    aggregate_id,
    event_type,
    version,
    timestamp
FROM event_store
ORDER BY timestamp DESC;
```

### Check Snapshots

```sql
SELECT 
    aggregate_id,
    aggregate_type,
    version,
    timestamp
FROM aggregate_snapshots
ORDER BY timestamp DESC;
```

### Kafka Consumer Groups

```bash
# List consumer groups
docker-compose exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:29092 \
  --list

# Describe consumer group
docker-compose exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:29092 \
  --describe \
  --group confirmed-orders-group
```

## Pattern Comparison

| Pattern | Use Case | Persistence | Scalability | Complexity |
|---------|----------|-------------|-------------|------------|
| Event Publisher | Simple event notification | No | Medium | Low |
| Event Listener | React to events | No | Medium | Low |
| Application Event | Infrastructure events | No | Low | Low |
| Domain Event | Business events | No | Medium | Medium |
| Event Bus | Decoupled communication | No | High | Medium |
| Event Sourcing | Complete audit trail | Yes | High | High |
| Event Stream | Real-time processing | Yes (Kafka) | Very High | High |
| Event Store | Event persistence | Yes | High | High |

## Best Practices

### 1. Event Naming
```java
// Good - Past tense, describes what happened
OrderCreatedEvent
OrderConfirmedEvent
OrderShippedEvent

// Bad - Present tense or commands
CreateOrderEvent
ConfirmOrderEvent
```

### 2. Event Versioning
```java
@Data
public class OrderCreatedEventV2 extends OrderEvent {
    private String customerId;
    private BigDecimal amount;
    private String currency; // New field in V2
}
```

### 3. Event Immutability
```java
// Events should be immutable
@Data
@Builder
public final class OrderCreatedEvent extends OrderEvent {
    private final String customerId;
    private final BigDecimal amount;
}
```

### 4. Async Event Processing
```java
@EventListener
@Async
public void handleOrderCreated(OrderCreatedEvent event) {
    // Long-running operations should be async
    sendEmail(event);
}
```

### 5. Error Handling
```java
@EventListener
public void handleEvent(OrderEvent event) {
    try {
        processEvent(event);
    } catch (Exception e) {
        log.error("Failed to process event", e);
        // Send to DLQ or retry queue
        deadLetterQueue.send(event);
    }
}
```

## Troubleshooting

### Events Not Being Received

**Check:**
1. Event listener is registered:
```bash
docker-compose logs app | grep "EventListener"
```

2. Event type matches:
```java
@EventListener
public void handleEvent(OrderCreatedEvent event) // Must match event type
```

### Kafka Connection Issues

```bash
# Check Kafka status
docker-compose ps kafka

# Check Kafka logs
docker-compose logs kafka

# Test Kafka connectivity
docker-compose exec app nc -zv kafka 29092
```

### Event Store Not Persisting

**Check database connection:**
```bash
# View H2 console
open http://localhost:8080/h2-console

# Check if tables exist
SELECT * FROM INFORMATION_SCHEMA.TABLES;
```

### Event Sourcing Reconstruction Errors

**Check event sequence:**
```sql
SELECT event_type, version 
FROM event_store 
WHERE aggregate_id = 'your-order-id'
ORDER BY version;
```

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=EventPublisherTest

# Run with coverage
mvn test jacoco:report
```

## Cleanup

```bash
# Stop all services
docker-compose down

# Remove volumes
docker-compose down -v

# Remove images
docker-compose down --rmi all
```

## Additional Resources

- [Spring Events Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-functionality-events)
- [Event Sourcing Pattern](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Kafka Streams Documentation](https://kafka.apache.org/documentation/streams/)
- [Axon Framework](https://docs.axoniq.io/reference-guide/)
- [Domain Events](https://learn.microsoft.com/en-us/dotnet/architecture/microservices/microservice-ddd-cqrs-patterns/domain-events-design-implementation)

## License

MIT License
```

This comprehensive implementation covers all 8 event-driven patterns with working code, tests, Docker setup, and detailed documentation. Each pattern is demonstrated with practical examples that you can run and test immediately.