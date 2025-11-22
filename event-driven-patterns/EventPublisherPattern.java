package com.example.events.publisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event Publisher Pattern - Demonstrates Event Publishing Mechanisms
 * 
 * This pattern shows how to:
 * 1. Use ApplicationEventPublisher to publish events
 * 2. Implement custom event publishers
 * 3. Publish synchronous events
 * 4. Publish asynchronous events
 * 5. Create custom events
 * 6. Publish events transactionally
 * 7. Add event metadata
 * 8. Track published events
 * 9. Implement conditional publishing
 * 10. Handle event publishing errors
 * 
 * Key Concepts:
 * - Event Publisher: Component that publishes events
 * - Event: Object representing something that happened
 * - Synchronous: Event processed immediately in same thread
 * - Asynchronous: Event processed later in different thread
 * - Decoupling: Publishers don't know about listeners
 * 
 * Event Publishing Patterns:
 * 1. Direct Publishing - ApplicationEventPublisher.publishEvent()
 * 2. Custom Publisher - Implement ApplicationEventPublisherAware
 * 3. Transactional Publishing - Publish after transaction commits
 * 4. Batch Publishing - Publish multiple events
 * 5. Conditional Publishing - Publish based on conditions
 * 
 * Dependencies:
 * - spring-context
 * - spring-boot-starter
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class EventPublisherPattern {

    public static void main(String[] args) {
        SpringApplication.run(EventPublisherPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("EVENT PUBLISHER PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateEventPublishing();
        demonstrateEventTypes();
        
        System.out.println("\nApplication running with event publishing");
        System.out.println("Test endpoints:");
        System.out.println("POST /api/events/publish - Publish custom event");
        System.out.println("GET /api/events/history - View event history");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateEventPublishing() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EVENT PUBLISHING MECHANISMS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Direct Publishing:");
        System.out.println("   applicationEventPublisher.publishEvent(event)");
        
        System.out.println("\n2. Custom Publisher:");
        System.out.println("   Implement ApplicationEventPublisherAware");
        
        System.out.println("\n3. Transactional Publishing:");
        System.out.println("   @TransactionalEventListener(phase = AFTER_COMMIT)");
    }
    
    private static void demonstrateEventTypes() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EVENT TYPES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n- ApplicationEvent: Base Spring event class");
        System.out.println("- Custom Events: POJO events (Spring 4.2+)");
        System.out.println("- Domain Events: Business domain events");
        System.out.println("- System Events: Technical/infrastructure events");
    }
}

/**
 * Base Event Class
 */
class BaseEvent {
    private final String eventId;
    private final LocalDateTime timestamp;
    private final String eventType;
    
    public BaseEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
    }
    
    public String getEventId() { return eventId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEventType() { return eventType; }
}

/**
 * User Created Event
 */
class UserCreatedEvent extends BaseEvent {
    private final String userId;
    private final String username;
    private final String email;
    
    public UserCreatedEvent(String userId, String username, String email) {
        super("UserCreated");
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
    
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}

/**
 * Order Placed Event
 */
class OrderPlacedEvent extends BaseEvent {
    private final String orderId;
    private final String customerId;
    private final double amount;
    private final List<String> items;
    
    public OrderPlacedEvent(String orderId, String customerId, double amount, List<String> items) {
        super("OrderPlaced");
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.items = new ArrayList<>(items);
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public double getAmount() { return amount; }
    public List<String> getItems() { return new ArrayList<>(items); }
}

/**
 * Payment Processed Event
 */
class PaymentProcessedEvent extends BaseEvent {
    private final String paymentId;
    private final String orderId;
    private final double amount;
    private final String paymentMethod;
    private final String status;
    
    public PaymentProcessedEvent(String paymentId, String orderId, double amount, 
                                 String paymentMethod, String status) {
        super("PaymentProcessed");
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }
    
    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
}

/**
 * Email Notification Event
 */
class EmailNotificationEvent extends BaseEvent {
    private final String recipient;
    private final String subject;
    private final String body;
    
    public EmailNotificationEvent(String recipient, String subject, String body) {
        super("EmailNotification");
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }
    
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
}

/**
 * Event Publishing History Tracker
 */
@Component
class EventPublishingHistory {
    private final List<EventRecord> publishedEvents = new CopyOnWriteArrayList<>();
    
    public void recordEvent(Object event, String publisherName) {
        publishedEvents.add(new EventRecord(event, publisherName, LocalDateTime.now()));
    }
    
    public List<EventRecord> getHistory() {
        return new ArrayList<>(publishedEvents);
    }
    
    public List<EventRecord> getHistoryByType(String eventType) {
        return publishedEvents.stream()
            .filter(record -> record.getEventType().equals(eventType))
            .toList();
    }
    
    public int getEventCount() {
        return publishedEvents.size();
    }
    
    static class EventRecord {
        private final Object event;
        private final String publisherName;
        private final LocalDateTime publishedAt;
        
        public EventRecord(Object event, String publisherName, LocalDateTime publishedAt) {
            this.event = event;
            this.publisherName = publisherName;
            this.publishedAt = publishedAt;
        }
        
        public Object getEvent() { return event; }
        public String getPublisherName() { return publisherName; }
        public LocalDateTime getPublishedAt() { return publishedAt; }
        
        public String getEventType() {
            if (event instanceof BaseEvent) {
                return ((BaseEvent) event).getEventType();
            }
            return event.getClass().getSimpleName();
        }
    }
}

/**
 * User Event Publisher Service
 */
@Service
class UserEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    private final EventPublishingHistory history;
    
    public UserEventPublisher(ApplicationEventPublisher eventPublisher, 
                             EventPublishingHistory history) {
        this.eventPublisher = eventPublisher;
        this.history = history;
    }
    
    public void publishUserCreated(String userId, String username, String email) {
        UserCreatedEvent event = new UserCreatedEvent(userId, username, email);
        eventPublisher.publishEvent(event);
        history.recordEvent(event, "UserEventPublisher");
        
        System.out.printf("[UserEventPublisher] Published: UserCreatedEvent for user %s%n", userId);
    }
    
    public void publishMultipleEvents(List<UserCreatedEvent> events) {
        for (UserCreatedEvent event : events) {
            eventPublisher.publishEvent(event);
            history.recordEvent(event, "UserEventPublisher");
        }
        
        System.out.printf("[UserEventPublisher] Published batch of %d events%n", events.size());
    }
}

/**
 * Order Event Publisher Service
 */
@Service
class OrderEventPublisher implements ApplicationEventPublisherAware {
    
    private ApplicationEventPublisher eventPublisher;
    private final EventPublishingHistory history;
    
    public OrderEventPublisher(EventPublishingHistory history) {
        this.history = history;
    }
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
    
    public void publishOrderPlaced(String orderId, String customerId, double amount, List<String> items) {
        OrderPlacedEvent event = new OrderPlacedEvent(orderId, customerId, amount, items);
        eventPublisher.publishEvent(event);
        history.recordEvent(event, "OrderEventPublisher");
        
        System.out.printf("[OrderEventPublisher] Published: OrderPlacedEvent for order %s%n", orderId);
        
        // Publish related events
        publishPaymentRequired(orderId, amount);
    }
    
    private void publishPaymentRequired(String orderId, double amount) {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
            UUID.randomUUID().toString(),
            orderId,
            amount,
            "pending",
            "PENDING"
        );
        eventPublisher.publishEvent(event);
        history.recordEvent(event, "OrderEventPublisher");
    }
}

/**
 * Notification Event Publisher Service
 */
@Service
class NotificationEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    private final EventPublishingHistory history;
    
    public NotificationEventPublisher(ApplicationEventPublisher eventPublisher,
                                     EventPublishingHistory history) {
        this.eventPublisher = eventPublisher;
        this.history = history;
    }
    
    public void publishEmailNotification(String recipient, String subject, String body) {
        EmailNotificationEvent event = new EmailNotificationEvent(recipient, subject, body);
        eventPublisher.publishEvent(event);
        history.recordEvent(event, "NotificationEventPublisher");
        
        System.out.printf("[NotificationEventPublisher] Published: EmailNotificationEvent to %s%n", recipient);
    }
    
    public void publishWelcomeEmail(String email, String username) {
        String subject = "Welcome to our platform!";
        String body = String.format("Hello %s, welcome aboard!", username);
        publishEmailNotification(email, subject, body);
    }
}

/**
 * Conditional Event Publisher
 */
@Service
class ConditionalEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    private final EventPublishingHistory history;
    private boolean publishingEnabled = true;
    
    public ConditionalEventPublisher(ApplicationEventPublisher eventPublisher,
                                    EventPublishingHistory history) {
        this.eventPublisher = eventPublisher;
        this.history = history;
    }
    
    public void publishIfEnabled(Object event, String publisherName) {
        if (publishingEnabled) {
            eventPublisher.publishEvent(event);
            history.recordEvent(event, publisherName);
            System.out.printf("[ConditionalEventPublisher] Published: %s%n", 
                event.getClass().getSimpleName());
        } else {
            System.out.println("[ConditionalEventPublisher] Publishing disabled, event skipped");
        }
    }
    
    public void enablePublishing() {
        publishingEnabled = true;
    }
    
    public void disablePublishing() {
        publishingEnabled = false;
    }
    
    public boolean isPublishingEnabled() {
        return publishingEnabled;
    }
}

/**
 * REST Controller for Event Publishing
 */
@RestController
@RequestMapping("/api/events")
class EventPublisherController {
    
    private final UserEventPublisher userEventPublisher;
    private final OrderEventPublisher orderEventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;
    private final EventPublishingHistory history;
    
    public EventPublisherController(UserEventPublisher userEventPublisher,
                                   OrderEventPublisher orderEventPublisher,
                                   NotificationEventPublisher notificationEventPublisher,
                                   EventPublishingHistory history) {
        this.userEventPublisher = userEventPublisher;
        this.orderEventPublisher = orderEventPublisher;
        this.notificationEventPublisher = notificationEventPublisher;
        this.history = history;
    }
    
    @PostMapping("/user")
    public Map<String, Object> publishUserCreatedEvent(
            @RequestParam String userId,
            @RequestParam String username,
            @RequestParam String email) {
        
        userEventPublisher.publishUserCreated(userId, username, email);
        
        return Map.of(
            "status", "published",
            "eventType", "UserCreatedEvent",
            "userId", userId
        );
    }
    
    @PostMapping("/order")
    public Map<String, Object> publishOrderPlacedEvent(
            @RequestParam String orderId,
            @RequestParam String customerId,
            @RequestParam double amount,
            @RequestParam List<String> items) {
        
        orderEventPublisher.publishOrderPlaced(orderId, customerId, amount, items);
        
        return Map.of(
            "status", "published",
            "eventType", "OrderPlacedEvent",
            "orderId", orderId
        );
    }
    
    @PostMapping("/notification")
    public Map<String, Object> publishEmailNotification(
            @RequestParam String recipient,
            @RequestParam String subject,
            @RequestParam String body) {
        
        notificationEventPublisher.publishEmailNotification(recipient, subject, body);
        
        return Map.of(
            "status", "published",
            "eventType", "EmailNotificationEvent",
            "recipient", recipient
        );
    }
    
    @GetMapping("/history")
    public Map<String, Object> getEventHistory() {
        return Map.of(
            "totalEvents", history.getEventCount(),
            "events", history.getHistory()
        );
    }
    
    @GetMapping("/history/{eventType}")
    public List<EventPublishingHistory.EventRecord> getEventHistoryByType(
            @PathVariable String eventType) {
        return history.getHistoryByType(eventType);
    }
}
