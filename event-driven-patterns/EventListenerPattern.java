package com.example.events.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event Listener Pattern - Demonstrates Event Listening Mechanisms
 * 
 * This pattern shows how to:
 * 1. Use @EventListener annotation
 * 2. Create synchronous event listeners
 * 3. Create asynchronous event listeners
 * 4. Apply conditional listening with SpEL
 * 5. Order multiple listeners
 * 6. Handle generic events
 * 7. Listen to multiple event types
 * 8. Handle listener exceptions
 * 9. Filter events with conditions
 * 10. Track listener execution
 * 
 * Key Concepts:
 * - Event Listener: Method that responds to events
 * - @EventListener: Annotation for marking listener methods
 * - @Async: Make listener execute asynchronously
 * - SpEL Conditions: Filter events with expressions
 * - @Order: Control listener execution order
 * 
 * Listener Types:
 * 1. Synchronous - Executes in publisher's thread
 * 2. Asynchronous - Executes in separate thread
 * 3. Conditional - Only processes matching events
 * 4. Ordered - Executes in specific sequence
 * 5. Generic - Handles multiple event types
 * 
 * Dependencies:
 * - spring-context
 * - spring-boot-starter
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
@EnableAsync
public class EventListenerPattern {

    public static void main(String[] args) {
        SpringApplication.run(EventListenerPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("EVENT LISTENER PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateListenerTypes();
        demonstrateListenerFeatures();
        
        System.out.println("\nApplication running with event listeners");
        System.out.println("Test endpoints:");
        System.out.println("POST /api/listener/trigger - Trigger test event");
        System.out.println("GET /api/listener/stats - View listener statistics");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateListenerTypes() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EVENT LISTENER TYPES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Synchronous Listener:");
        System.out.println("   @EventListener");
        System.out.println("   public void handleEvent(MyEvent event) { }");
        
        System.out.println("\n2. Asynchronous Listener:");
        System.out.println("   @Async");
        System.out.println("   @EventListener");
        System.out.println("   public void handleEventAsync(MyEvent event) { }");
        
        System.out.println("\n3. Conditional Listener:");
        System.out.println("   @EventListener(condition = \"#event.amount > 1000\")");
        System.out.println("   public void handleLargeOrder(OrderEvent event) { }");
    }
    
    private static void demonstrateListenerFeatures() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("LISTENER FEATURES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n- Multiple listeners per event");
        System.out.println("- Ordered execution with @Order");
        System.out.println("- Exception handling");
        System.out.println("- Event filtering with SpEL");
        System.out.println("- Generic event handling");
    }
}

/**
 * Custom Event Classes
 */
class UserEvent {
    private final String userId;
    private final String action;
    private final LocalDateTime timestamp;
    
    public UserEvent(String userId, String action) {
        this.userId = userId;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getUserId() { return userId; }
    public String getAction() { return action; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class OrderEvent {
    private final String orderId;
    private final String customerId;
    private final double amount;
    private final LocalDateTime timestamp;
    
    public OrderEvent(String orderId, String customerId, double amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class PaymentEvent {
    private final String paymentId;
    private final String orderId;
    private final double amount;
    private final String status;
    
    public PaymentEvent(String paymentId, String orderId, double amount, String status) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }
    
    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
}

class NotificationEvent {
    private final String recipient;
    private final String message;
    private final String channel;
    
    public NotificationEvent(String recipient, String message, String channel) {
        this.recipient = recipient;
        this.message = message;
        this.channel = channel;
    }
    
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public String getChannel() { return channel; }
}

/**
 * Listener Statistics Tracker
 */
@Component
class ListenerStatistics {
    private final Map<String, Integer> listenerInvocations = new HashMap<>();
    private final List<ListenerExecution> executionHistory = new CopyOnWriteArrayList<>();
    
    public void recordExecution(String listenerName, String eventType, long executionTime) {
        listenerInvocations.merge(listenerName, 1, Integer::sum);
        executionHistory.add(new ListenerExecution(listenerName, eventType, executionTime, LocalDateTime.now()));
    }
    
    public Map<String, Integer> getInvocations() {
        return new HashMap<>(listenerInvocations);
    }
    
    public List<ListenerExecution> getExecutionHistory() {
        return new ArrayList<>(executionHistory);
    }
    
    static class ListenerExecution {
        private final String listenerName;
        private final String eventType;
        private final long executionTime;
        private final LocalDateTime timestamp;
        
        public ListenerExecution(String listenerName, String eventType, long executionTime, LocalDateTime timestamp) {
            this.listenerName = listenerName;
            this.eventType = eventType;
            this.executionTime = executionTime;
            this.timestamp = timestamp;
        }
        
        public String getListenerName() { return listenerName; }
        public String getEventType() { return eventType; }
        public long getExecutionTime() { return executionTime; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}

/**
 * Synchronous Event Listeners
 */
@Component
class SynchronousEventListener {
    
    private final ListenerStatistics statistics;
    
    public SynchronousEventListener(ListenerStatistics statistics) {
        this.statistics = statistics;
    }
    
    @EventListener
    @Order(1)
    public void handleUserEvent(UserEvent event) {
        long startTime = System.currentTimeMillis();
        
        System.out.printf("[SynchronousEventListener] Handling UserEvent for user %s, action: %s (Order 1)%n",
            event.getUserId(), event.getAction());
        
        // Simulate processing
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        statistics.recordExecution("SynchronousEventListener.handleUserEvent", 
            "UserEvent", executionTime);
    }
    
    @EventListener
    @Order(2)
    public void handleOrderEvent(OrderEvent event) {
        long startTime = System.currentTimeMillis();
        
        System.out.printf("[SynchronousEventListener] Handling OrderEvent %s, amount: %.2f (Order 2)%n",
            event.getOrderId(), event.getAmount());
        
        long executionTime = System.currentTimeMillis() - startTime;
        statistics.recordExecution("SynchronousEventListener.handleOrderEvent", 
            "OrderEvent", executionTime);
    }
}

/**
 * Asynchronous Event Listeners
 */
@Component
class AsynchronousEventListener {
    
    private final ListenerStatistics statistics;
    
    public AsynchronousEventListener(ListenerStatistics statistics) {
        this.statistics = statistics;
    }
    
    @Async
    @EventListener
    public void handleUserEventAsync(UserEvent event) {
        long startTime = System.currentTimeMillis();
        
        System.out.printf("[AsynchronousEventListener] Handling UserEvent asynchronously for user %s in thread %s%n",
            event.getUserId(), Thread.currentThread().getName());
        
        // Simulate async processing
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        statistics.recordExecution("AsynchronousEventListener.handleUserEventAsync", 
            "UserEvent", executionTime);
    }
    
    @Async
    @EventListener
    public void handleOrderEventAsync(OrderEvent event) {
        long startTime = System.currentTimeMillis();
        
        System.out.printf("[AsynchronousEventListener] Processing order %s asynchronously in thread %s%n",
            event.getOrderId(), Thread.currentThread().getName());
        
        long executionTime = System.currentTimeMillis() - startTime;
        statistics.recordExecution("AsynchronousEventListener.handleOrderEventAsync", 
            "OrderEvent", executionTime);
    }
}

/**
 * Conditional Event Listeners (using SpEL)
 */
@Component
class ConditionalEventListener {
    
    private final ListenerStatistics statistics;
    
    public ConditionalEventListener(ListenerStatistics statistics) {
        this.statistics = statistics;
    }
    
    // Only handle orders with amount > 1000
    @EventListener(condition = "#event.amount > 1000")
    public void handleLargeOrder(OrderEvent event) {
        long startTime = System.currentTimeMillis();
        
        System.out.printf("[ConditionalEventListener] Handling LARGE order %s, amount: %.2f%n",
            event.getOrderId(), event.getAmount());
        
        long executionTime = System.currentTimeMillis() - startTime;
        statistics.recordExecution("ConditionalEventListener.handleLargeOrder", 
            "OrderEvent", executionTime);
    }
    
    // Only handle orders with amount <= 100
    @EventListener(condition = "#event.amount <= 100")
    public void handleSmallOrder(OrderEvent event) {
        long startTime = System.currentTimeMillis();
        
        System.out.printf("[ConditionalEventListener] Handling SMALL order %s, amount: %.2f%n",
            event.getOrderId(), event.getAmount());
        
        long executionTime = System.currentTimeMillis() - startTime;
        statistics.recordExecution("ConditionalEventListener.handleSmallOrder", 
            "OrderEvent", executionTime);
    }
    
    // Only handle successful payments
    @EventListener(condition = "#event.status == 'SUCCESS'")
    public void handleSuccessfulPayment(PaymentEvent event) {
        long startTime = System.currentTimeMillis();
        
        System.out.printf("[ConditionalEventListener] Handling SUCCESSFUL payment %s%n",
            event.getPaymentId());
        
        long executionTime = System.currentTimeMillis() - startTime;
        statistics.recordExecution("ConditionalEventListener.handleSuccessfulPayment", 
            "PaymentEvent", executionTime);
    }
}

/**
 * Ordered Event Listeners
 */
@Component
class OrderedEventListener {
    
    private final ListenerStatistics statistics;
    
    public OrderedEventListener(ListenerStatistics statistics) {
        this.statistics = statistics;
    }
    
    @EventListener
    @Order(1)
    public void firstListener(PaymentEvent event) {
        System.out.printf("[OrderedEventListener] FIRST listener handling payment %s%n",
            event.getPaymentId());
        
        statistics.recordExecution("OrderedEventListener.firstListener", 
            "PaymentEvent", 0);
    }
    
    @EventListener
    @Order(2)
    public void secondListener(PaymentEvent event) {
        System.out.printf("[OrderedEventListener] SECOND listener handling payment %s%n",
            event.getPaymentId());
        
        statistics.recordExecution("OrderedEventListener.secondListener", 
            "PaymentEvent", 0);
    }
    
    @EventListener
    @Order(3)
    public void thirdListener(PaymentEvent event) {
        System.out.printf("[OrderedEventListener] THIRD listener handling payment %s%n",
            event.getPaymentId());
        
        statistics.recordExecution("OrderedEventListener.thirdListener", 
            "PaymentEvent", 0);
    }
}

/**
 * Multi-Event Listener (handles multiple event types)
 */
@Component
class MultiEventListener {
    
    private final ListenerStatistics statistics;
    
    public MultiEventListener(ListenerStatistics statistics) {
        this.statistics = statistics;
    }
    
    @EventListener({UserEvent.class, OrderEvent.class, PaymentEvent.class})
    public void handleAnyEvent(Object event) {
        System.out.printf("[MultiEventListener] Handling event of type: %s%n",
            event.getClass().getSimpleName());
        
        statistics.recordExecution("MultiEventListener.handleAnyEvent", 
            event.getClass().getSimpleName(), 0);
    }
}

/**
 * Error Handling Event Listener
 */
@Component
class ErrorHandlingEventListener {
    
    private final ListenerStatistics statistics;
    
    public ErrorHandlingEventListener(ListenerStatistics statistics) {
        this.statistics = statistics;
    }
    
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            System.out.printf("[ErrorHandlingEventListener] Sending notification to %s via %s%n",
                event.getRecipient(), event.getChannel());
            
            // Simulate potential error
            if ("error".equalsIgnoreCase(event.getChannel())) {
                throw new RuntimeException("Notification channel error");
            }
            
            statistics.recordExecution("ErrorHandlingEventListener.handleNotificationEvent", 
                "NotificationEvent", 0);
                
        } catch (Exception e) {
            System.err.printf("[ErrorHandlingEventListener] Error handling notification: %s%n",
                e.getMessage());
        }
    }
}

/**
 * Event Publisher Service for Testing
 */
@Service
class EventPublisherService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public EventPublisherService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void publishUserEvent(String userId, String action) {
        eventPublisher.publishEvent(new UserEvent(userId, action));
    }
    
    public void publishOrderEvent(String orderId, String customerId, double amount) {
        eventPublisher.publishEvent(new OrderEvent(orderId, customerId, amount));
    }
    
    public void publishPaymentEvent(String paymentId, String orderId, double amount, String status) {
        eventPublisher.publishEvent(new PaymentEvent(paymentId, orderId, amount, status));
    }
    
    public void publishNotificationEvent(String recipient, String message, String channel) {
        eventPublisher.publishEvent(new NotificationEvent(recipient, message, channel));
    }
}

/**
 * REST Controller for Event Listener Testing
 */
@RestController
@RequestMapping("/api/listener")
class EventListenerController {
    
    private final EventPublisherService publisherService;
    private final ListenerStatistics statistics;
    
    public EventListenerController(EventPublisherService publisherService, 
                                   ListenerStatistics statistics) {
        this.publisherService = publisherService;
        this.statistics = statistics;
    }
    
    @PostMapping("/trigger/user")
    public Map<String, String> triggerUserEvent(
            @RequestParam String userId,
            @RequestParam String action) {
        
        publisherService.publishUserEvent(userId, action);
        
        return Map.of(
            "status", "triggered",
            "eventType", "UserEvent",
            "userId", userId,
            "action", action
        );
    }
    
    @PostMapping("/trigger/order")
    public Map<String, Object> triggerOrderEvent(
            @RequestParam String orderId,
            @RequestParam String customerId,
            @RequestParam double amount) {
        
        publisherService.publishOrderEvent(orderId, customerId, amount);
        
        return Map.of(
            "status", "triggered",
            "eventType", "OrderEvent",
            "orderId", orderId,
            "amount", amount
        );
    }
    
    @PostMapping("/trigger/payment")
    public Map<String, Object> triggerPaymentEvent(
            @RequestParam String paymentId,
            @RequestParam String orderId,
            @RequestParam double amount,
            @RequestParam String status) {
        
        publisherService.publishPaymentEvent(paymentId, orderId, amount, status);
        
        return Map.of(
            "status", "triggered",
            "eventType", "PaymentEvent",
            "paymentId", paymentId,
            "paymentStatus", status
        );
    }
    
    @GetMapping("/stats")
    public Map<String, Object> getListenerStatistics() {
        return Map.of(
            "invocations", statistics.getInvocations(),
            "executionHistory", statistics.getExecutionHistory()
        );
    }
}
