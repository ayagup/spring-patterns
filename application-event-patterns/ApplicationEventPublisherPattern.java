package com.example.events;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Application Event Publisher Pattern
 * ====================================
 * 
 * Demonstrates how to publish and listen to custom application events
 * in Spring for decoupled communication between components.
 * 
 * Key Concepts:
 * ------------
 * 1. ApplicationEvent - Base class for events
 * 2. ApplicationEventPublisher - Event publishing interface
 * 3. ApplicationListener - Event listener interface
 * 4. @EventListener - Annotation-based listener
 * 5. Synchronous/Asynchronous - Event processing modes
 * 
 * Event Flow:
 * ----------
 * 1. Component publishes event
 * 2. Spring propagates to all listeners
 * 3. Listeners handle event independently
 * 4. No direct coupling between publisher and listeners
 * 
 * When to Use:
 * -----------
 * - Decouple components
 * - Notify multiple listeners
 * - Implement observer pattern
 * - Cross-cutting concerns
 * - Audit logging
 * - Email notifications
 * - Cache invalidation
 * - State change propagation
 * 
 * Advantages:
 * ----------
 * - Loose coupling
 * - Multiple listeners per event
 * - Easy to add new listeners
 * - Testable components
 * - Built-in Spring support
 * 
 * Best Practices:
 * --------------
 * - Keep events immutable
 * - Meaningful event names
 * - Document event contracts
 * - Handle exceptions in listeners
 * - Consider async for slow operations
 * - Avoid circular event chains
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Custom Event
class UserRegisteredEvent extends ApplicationEvent {
    private final String username;
    private final String email;
    private final long timestamp;
    
    public UserRegisteredEvent(Object source, String username, String email) {
        super(source);
        this.username = username;
        this.email = email;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public long getTimestamp() { return timestamp; }
}

// Event Publisher
@Component
public class ApplicationEventPublisherPattern {
    
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    
    public ApplicationEventPublisherPattern(
            org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Publish user registration event
     */
    public void publishUserRegistered(String username, String email) {
        UserRegisteredEvent event = new UserRegisteredEvent(this, username, email);
        eventPublisher.publishEvent(event);
    }
    
    /**
     * Publish generic event
     */
    public void publishEvent(Object event) {
        eventPublisher.publishEvent(event);
    }
}

/**
 * Example 2: Order Events
 */
class OrderCreatedEvent extends ApplicationEvent {
    private final Long orderId;
    private final String customerId;
    private final double amount;
    
    public OrderCreatedEvent(Object source, Long orderId, 
                            String customerId, double amount) {
        super(source);
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
    }
    
    public Long getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public double getAmount() { return amount; }
}

@Component
class OrderService {
    
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    
    public OrderService(org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void createOrder(Long orderId, String customerId, double amount) {
        // Business logic
        System.out.println("Creating order: " + orderId);
        
        // Publish event
        OrderCreatedEvent event = new OrderCreatedEvent(
            this, orderId, customerId, amount);
        eventPublisher.publishEvent(event);
    }
}

/**
 * Example 3: Event Listeners
 */

// Traditional ApplicationListener interface
@Component
class EmailNotificationListener implements ApplicationListener<UserRegisteredEvent> {
    
    @Override
    public void onApplicationEvent(UserRegisteredEvent event) {
        System.out.println("Sending welcome email to: " + event.getEmail());
        // Send email logic
    }
}

// Annotation-based listener
@Component
class AuditLogListener {
    
    @EventListener
    public void handleUserRegistration(UserRegisteredEvent event) {
        System.out.println("Audit log: User registered - " + event.getUsername());
        // Log to database
    }
    
    @EventListener
    public void handleOrderCreation(OrderCreatedEvent event) {
        System.out.println("Audit log: Order created - " + event.getOrderId());
        // Log to database
    }
}

/**
 * Example 4: Conditional Event Listener
 */
@Component
class ConditionalEventListener {
    
    @EventListener(condition = "#event.amount > 1000")
    public void handleLargeOrder(OrderCreatedEvent event) {
        System.out.println("Large order detected: " + event.getOrderId() + 
                         " - $" + event.getAmount());
        // Special handling for large orders
    }
    
    @EventListener(condition = "#event.email.endsWith('gmail.com')")
    public void handleGmailUser(UserRegisteredEvent event) {
        System.out.println("Gmail user registered: " + event.getUsername());
        // Special handling for Gmail users
    }
}

/**
 * Example 5: Multiple Event Types
 */
interface DomainEvent {
    long getTimestamp();
}

class ProductUpdatedEvent implements DomainEvent {
    private final Long productId;
    private final String field;
    private final Object oldValue;
    private final Object newValue;
    private final long timestamp;
    
    public ProductUpdatedEvent(Long productId, String field, 
                              Object oldValue, Object newValue) {
        this.productId = productId;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Long getProductId() { return productId; }
    public String getField() { return field; }
    public Object getOldValue() { return oldValue; }
    public Object getNewValue() { return newValue; }
    public long getTimestamp() { return timestamp; }
}

@Component
class ProductService {
    
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    
    public ProductService(org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void updateProductPrice(Long productId, double oldPrice, double newPrice) {
        // Update logic
        System.out.println("Updating product price: " + productId);
        
        // Publish event
        ProductUpdatedEvent event = new ProductUpdatedEvent(
            productId, "price", oldPrice, newPrice);
        eventPublisher.publishEvent(event);
    }
}

/**
 * Example 6: Generic Event Listener
 */
@Component
class GenericEventListener {
    
    @EventListener
    public void handleAllDomainEvents(DomainEvent event) {
        System.out.println("Domain event occurred at: " + event.getTimestamp());
    }
}

/**
 * Example 7: Event Hierarchy
 */
abstract class PaymentEvent extends ApplicationEvent {
    private final Long paymentId;
    private final double amount;
    
    public PaymentEvent(Object source, Long paymentId, double amount) {
        super(source);
        this.paymentId = paymentId;
        this.amount = amount;
    }
    
    public Long getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
}

class PaymentSuccessEvent extends PaymentEvent {
    private final String transactionId;
    
    public PaymentSuccessEvent(Object source, Long paymentId, 
                              double amount, String transactionId) {
        super(source, paymentId, amount);
        this.transactionId = transactionId;
    }
    
    public String getTransactionId() { return transactionId; }
}

class PaymentFailedEvent extends PaymentEvent {
    private final String errorCode;
    private final String errorMessage;
    
    public PaymentFailedEvent(Object source, Long paymentId, 
                             double amount, String errorCode, String errorMessage) {
        super(source, paymentId, amount);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
}

@Component
class PaymentEventListener {
    
    // Listen to all payment events
    @EventListener
    public void handlePayment(PaymentEvent event) {
        System.out.println("Payment event: " + event.getPaymentId());
    }
    
    // Listen to specific payment event
    @EventListener
    public void handleSuccess(PaymentSuccessEvent event) {
        System.out.println("Payment succeeded: " + event.getTransactionId());
    }
    
    @EventListener
    public void handleFailure(PaymentFailedEvent event) {
        System.out.println("Payment failed: " + event.getErrorMessage());
    }
}

/**
 * Example 8: Event with Metadata
 */
class EventMetadata {
    private final String userId;
    private final String ipAddress;
    private final String userAgent;
    
    public EventMetadata(String userId, String ipAddress, String userAgent) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    public String getUserId() { return userId; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
}

class LoginEvent extends ApplicationEvent {
    private final String username;
    private final boolean successful;
    private final EventMetadata metadata;
    
    public LoginEvent(Object source, String username, boolean successful, 
                     EventMetadata metadata) {
        super(source);
        this.username = username;
        this.successful = successful;
        this.metadata = metadata;
    }
    
    public String getUsername() { return username; }
    public boolean isSuccessful() { return successful; }
    public EventMetadata getMetadata() { return metadata; }
}

@Component
class SecurityEventListener {
    
    @EventListener(condition = "#event.successful == false")
    public void handleFailedLogin(LoginEvent event) {
        System.out.println("Failed login attempt: " + event.getUsername() + 
                         " from " + event.getMetadata().getIpAddress());
        // Security alert logic
    }
}

/**
 * Usage Examples
 */
class ApplicationEventUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Application Event Publisher Pattern");
        System.out.println("===================================\n");
        
        System.out.println("Publishing Events:");
        System.out.println("- UserRegisteredEvent");
        System.out.println("- OrderCreatedEvent");
        System.out.println("- ProductUpdatedEvent");
        System.out.println("- PaymentSuccessEvent/PaymentFailedEvent");
        System.out.println("- LoginEvent");
        
        System.out.println("\nListeners:");
        System.out.println("- EmailNotificationListener");
        System.out.println("- AuditLogListener");
        System.out.println("- ConditionalEventListener (SpEL conditions)");
        System.out.println("- GenericEventListener (all DomainEvent)");
        System.out.println("- PaymentEventListener (event hierarchy)");
        System.out.println("- SecurityEventListener (failed logins only)");
        
        System.out.println("\nBenefits:");
        System.out.println("1. Loose coupling between components");
        System.out.println("2. Multiple listeners per event");
        System.out.println("3. Easy to add new functionality");
        System.out.println("4. Built-in Spring infrastructure");
        System.out.println("5. Supports async processing");
    }
}
