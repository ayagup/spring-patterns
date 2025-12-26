package com.example.events;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Custom Application Event Pattern
 * ================================
 * 
 * Demonstrates creating and using custom application events in Spring.
 * Custom events allow domain-specific event-driven communication between
 * application components.
 * 
 * Key Concepts:
 * ------------
 * 1. Custom Event Class - Extend ApplicationEvent
 * 2. Event Publisher - Inject and use ApplicationEventPublisher
 * 3. Event Listeners - @EventListener or ApplicationListener<E>
 * 4. Event Payload - Data carried by the event
 * 5. Event Hierarchy - Event inheritance and polymorphism
 * 
 * Creating Custom Events:
 * ----------------------
 * 1. Extend ApplicationEvent (traditional)
 * 2. Use any object as event (modern Spring)
 * 3. Add relevant fields and methods
 * 4. Make immutable for thread safety
 * 5. Override toString() for logging
 * 
 * Publishing Events:
 * -----------------
 * 1. Inject ApplicationEventPublisher
 * 2. Call publishEvent(event)
 * 3. Synchronous by default
 * 4. Use @Async for asynchronous
 * 
 * Listening to Events:
 * -------------------
 * 1. @EventListener annotation
 * 2. ApplicationListener<E> interface
 * 3. Conditional listeners with SpEL
 * 4. Multiple listeners per event
 * 5. Event hierarchy support
 * 
 * When to Use:
 * -----------
 * - Domain event-driven design
 * - Decouple application components
 * - Cross-cutting concerns (audit, notification)
 * - Asynchronous processing
 * - Event sourcing patterns
 * - Workflow orchestration
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// ============================================================
// TRADITIONAL APPROACH: Extend ApplicationEvent
// ============================================================

/**
 * User Registration Event (Traditional)
 */
class UserRegistrationEvent extends ApplicationEvent {
    private final String username;
    private final String email;
    private final long timestamp;
    
    public UserRegistrationEvent(Object source, String username, String email) {
        super(source);
        this.username = username;
        this.email = email;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "UserRegistrationEvent{username='" + username + "', email='" + email + "'}";
    }
}

/**
 * Order Placed Event (Traditional)
 */
class OrderPlacedEvent extends ApplicationEvent {
    private final String orderId;
    private final String customerId;
    private final double totalAmount;
    
    public OrderPlacedEvent(Object source, String orderId, String customerId, double totalAmount) {
        super(source);
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public double getTotalAmount() { return totalAmount; }
    
    @Override
    public String toString() {
        return "OrderPlacedEvent{orderId='" + orderId + "', amount=" + totalAmount + "}";
    }
}

// ============================================================
// MODERN APPROACH: Any object as event (Spring 4.2+)
// ============================================================

/**
 * Payment Processed Event (POJO)
 */
class PaymentProcessedEvent {
    private final String paymentId;
    private final String orderId;
    private final double amount;
    private final String paymentMethod;
    private final boolean success;
    
    public PaymentProcessedEvent(String paymentId, String orderId, double amount, 
                                  String paymentMethod, boolean success) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.success = success;
    }
    
    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public boolean isSuccess() { return success; }
    
    @Override
    public String toString() {
        return "PaymentProcessedEvent{paymentId='" + paymentId + "', success=" + success + "}";
    }
}

/**
 * Inventory Updated Event (POJO)
 */
class InventoryUpdatedEvent {
    private final String productId;
    private final int quantity;
    private final String operation; // "ADDED" or "REMOVED"
    
    public InventoryUpdatedEvent(String productId, int quantity, String operation) {
        this.productId = productId;
        this.quantity = quantity;
        this.operation = operation;
    }
    
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public String getOperation() { return operation; }
    
    @Override
    public String toString() {
        return "InventoryUpdatedEvent{productId='" + productId + "', quantity=" + quantity + "}";
    }
}

// ============================================================
// EVENT PUBLISHERS
// ============================================================

@Component
class UserService {
    private final ApplicationEventPublisher eventPublisher;
    
    public UserService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void registerUser(String username, String email) {
        System.out.println("Registering user: " + username);
        
        // Perform registration logic...
        
        // Publish event
        UserRegistrationEvent event = new UserRegistrationEvent(this, username, email);
        eventPublisher.publishEvent(event);
        
        System.out.println("User registration event published");
    }
}

@Component
class OrderService {
    private final ApplicationEventPublisher eventPublisher;
    
    public OrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void placeOrder(String orderId, String customerId, double amount) {
        System.out.println("Placing order: " + orderId);
        
        // Perform order logic...
        
        // Publish event
        OrderPlacedEvent event = new OrderPlacedEvent(this, orderId, customerId, amount);
        eventPublisher.publishEvent(event);
        
        System.out.println("Order placed event published");
    }
}

@Component
class PaymentService {
    private final ApplicationEventPublisher eventPublisher;
    
    public PaymentService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void processPayment(String paymentId, String orderId, double amount, String method) {
        System.out.println("Processing payment: " + paymentId);
        
        // Process payment...
        boolean success = true; // Simulated success
        
        // Publish event (POJO, no need to extend ApplicationEvent)
        PaymentProcessedEvent event = new PaymentProcessedEvent(
            paymentId, orderId, amount, method, success
        );
        eventPublisher.publishEvent(event);
        
        System.out.println("Payment processed event published");
    }
}

// ============================================================
// EVENT LISTENERS
// ============================================================

/**
 * Email Notification Listener
 */
@Component
class EmailNotificationListener {
    
    @EventListener
    public void handleUserRegistration(UserRegistrationEvent event) {
        System.out.println("Sending welcome email to: " + event.getEmail());
        sendWelcomeEmail(event.getEmail());
    }
    
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        System.out.println("Sending order confirmation email");
        sendOrderConfirmation(event.getOrderId(), event.getCustomerId());
    }
    
    @EventListener
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        if (event.isSuccess()) {
            System.out.println("Sending payment receipt email");
            sendPaymentReceipt(event.getPaymentId(), event.getAmount());
        }
    }
    
    private void sendWelcomeEmail(String email) {
        System.out.println("  Welcome email sent to: " + email);
    }
    
    private void sendOrderConfirmation(String orderId, String customerId) {
        System.out.println("  Order confirmation sent for: " + orderId);
    }
    
    private void sendPaymentReceipt(String paymentId, double amount) {
        System.out.println("  Payment receipt sent: $" + amount);
    }
}

/**
 * Audit Log Listener
 */
@Component
class AuditLogListener {
    
    @EventListener
    public void auditUserRegistration(UserRegistrationEvent event) {
        logAuditEntry("USER_REGISTERED", 
                     "User: " + event.getUsername() + ", Email: " + event.getEmail());
    }
    
    @EventListener
    public void auditOrderPlaced(OrderPlacedEvent event) {
        logAuditEntry("ORDER_PLACED", 
                     "OrderId: " + event.getOrderId() + ", Amount: $" + event.getTotalAmount());
    }
    
    @EventListener
    public void auditPaymentProcessed(PaymentProcessedEvent event) {
        logAuditEntry("PAYMENT_PROCESSED", 
                     "PaymentId: " + event.getPaymentId() + ", Success: " + event.isSuccess());
    }
    
    private void logAuditEntry(String action, String details) {
        System.out.println("AUDIT: " + action + " - " + details + 
                         " at " + new java.util.Date());
    }
}

/**
 * Conditional Listener (SpEL)
 */
@Component
class ConditionalEventListener {
    
    // Only handle large orders (> $1000)
    @EventListener(condition = "#event.totalAmount > 1000")
    public void handleLargeOrder(OrderPlacedEvent event) {
        System.out.println("LARGE ORDER DETECTED: $" + event.getTotalAmount());
        notifyManagement(event);
    }
    
    // Only handle failed payments
    @EventListener(condition = "!#event.success")
    public void handleFailedPayment(PaymentProcessedEvent event) {
        System.out.println("PAYMENT FAILED: " + event.getPaymentId());
        alertFinanceTeam(event);
    }
    
    private void notifyManagement(OrderPlacedEvent event) {
        System.out.println("Management notified of large order");
    }
    
    private void alertFinanceTeam(PaymentProcessedEvent event) {
        System.out.println("Finance team alerted of payment failure");
    }
}

/**
 * Traditional Listener Interface
 */
@Component
class TraditionalOrderListener implements ApplicationListener<OrderPlacedEvent> {
    
    @Override
    public void onApplicationEvent(OrderPlacedEvent event) {
        System.out.println("Traditional listener received order event: " + event.getOrderId());
        processOrder(event);
    }
    
    private void processOrder(OrderPlacedEvent event) {
        System.out.println("Processing order through traditional listener");
    }
}

// ============================================================
// EVENT HIERARCHY EXAMPLE
// ============================================================

/**
 * Base Payment Event
 */
abstract class PaymentEvent extends ApplicationEvent {
    private final String paymentId;
    
    public PaymentEvent(Object source, String paymentId) {
        super(source);
        this.paymentId = paymentId;
    }
    
    public String getPaymentId() { return paymentId; }
}

/**
 * Payment Success Event
 */
class PaymentSuccessEvent extends PaymentEvent {
    private final String transactionId;
    
    public PaymentSuccessEvent(Object source, String paymentId, String transactionId) {
        super(source, paymentId);
        this.transactionId = transactionId;
    }
    
    public String getTransactionId() { return transactionId; }
}

/**
 * Payment Failure Event
 */
class PaymentFailureEvent extends PaymentEvent {
    private final String errorCode;
    private final String errorMessage;
    
    public PaymentFailureEvent(Object source, String paymentId, 
                               String errorCode, String errorMessage) {
        super(source, paymentId);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
}

/**
 * Hierarchy Event Listener
 */
@Component
class PaymentEventListener {
    
    // Listen to all payment events
    @EventListener
    public void handleAnyPayment(PaymentEvent event) {
        System.out.println("Payment event received: " + event.getPaymentId());
    }
    
    // Listen only to success events
    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        System.out.println("Payment successful: " + event.getTransactionId());
    }
    
    // Listen only to failure events
    @EventListener
    public void handlePaymentFailure(PaymentFailureEvent event) {
        System.out.println("Payment failed: " + event.getErrorCode() + 
                         " - " + event.getErrorMessage());
    }
}

/**
 * Main Pattern Class
 */
@Component
public class CustomApplicationEventPattern {
    
    public static void demonstrateCustomEvents() {
        System.out.println("=== Custom Application Event Pattern ===\n");
        
        System.out.println("Creating custom events:");
        System.out.println("1. Traditional: Extend ApplicationEvent");
        System.out.println("2. Modern: Use any POJO (Spring 4.2+)");
        System.out.println("3. Hierarchy: Extend custom events\n");
        
        System.out.println("Publishing events:");
        System.out.println("- Inject ApplicationEventPublisher");
        System.out.println("- Call publishEvent(event)\n");
        
        System.out.println("Listening to events:");
        System.out.println("- @EventListener annotation");
        System.out.println("- ApplicationListener<E> interface");
        System.out.println("- Conditional listeners with SpEL");
        System.out.println("- Event hierarchy support");
    }
}

/**
 * Usage Examples
 */
class CustomApplicationEventUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Custom Application Event Pattern");
        System.out.println("=================================\n");
        
        System.out.println("Event Creation Approaches:");
        System.out.println("1. Traditional: Extend ApplicationEvent");
        System.out.println("   - Required before Spring 4.2");
        System.out.println("   - Contains event source");
        System.out.println("2. Modern: Use any POJO");
        System.out.println("   - Simpler, no inheritance");
        System.out.println("   - Spring 4.2+ support\n");
        
        System.out.println("Listening Approaches:");
        System.out.println("1. @EventListener annotation (recommended)");
        System.out.println("2. ApplicationListener<E> interface");
        System.out.println("3. Conditional with SpEL expressions\n");
        
        System.out.println("Benefits:");
        System.out.println("- Loose coupling between components");
        System.out.println("- Easy to add new listeners");
        System.out.println("- Supports event hierarchy");
        System.out.println("- Can be asynchronous with @Async");
        System.out.println("- Type-safe event handling\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Make events immutable");
        System.out.println("- Use meaningful event names");
        System.out.println("- Include relevant context data");
        System.out.println("- Override toString() for logging");
        System.out.println("- Handle exceptions in listeners");
        System.out.println("- Consider async for slow operations");
    }
}
