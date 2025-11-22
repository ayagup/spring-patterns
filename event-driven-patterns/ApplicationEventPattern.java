package com.example.events.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Application Event Pattern - Demonstrates Spring ApplicationEvent Hierarchy
 * 
 * This pattern shows how to:
 * 1. Extend ApplicationEvent for custom events
 * 2. Listen to context lifecycle events
 * 3. Create custom application events
 * 4. Implement ApplicationListener interface
 * 5. Handle ContextRefreshedEvent
 * 6. Handle ContextStartedEvent and ContextStoppedEvent
 * 7. Handle ContextClosedEvent
 * 8. Handle SpringApplicationEvent
 * 9. Create event hierarchies
 * 10. Propagate events through context
 * 
 * Key Concepts:
 * - ApplicationEvent: Base class for Spring events
 * - ApplicationListener: Interface for event listeners
 * - Context Events: Lifecycle events of ApplicationContext
 * - Event Hierarchy: Parent-child event relationships
 * - Event Source: Object that triggered the event
 * 
 * Built-in Context Events:
 * 1. ContextRefreshedEvent - Context initialized or refreshed
 * 2. ContextStartedEvent - Context explicitly started
 * 3. ContextStoppedEvent - Context explicitly stopped
 * 4. ContextClosedEvent - Context about to close
 * 5. RequestHandledEvent - HTTP request handled (web apps)
 * 
 * Dependencies:
 * - spring-context
 * - spring-boot-starter
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class ApplicationEventPattern {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationEventPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("APPLICATION EVENT PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateContextEvents();
        demonstrateCustomEvents();
        
        System.out.println("\nApplication running with ApplicationEvent patterns");
        System.out.println("Test endpoints:");
        System.out.println("POST /api/app-events/custom - Publish custom application event");
        System.out.println("GET /api/app-events/history - View event history");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateContextEvents() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONTEXT LIFECYCLE EVENTS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. ContextRefreshedEvent - ApplicationContext initialized/refreshed");
        System.out.println("2. ContextStartedEvent - ApplicationContext started");
        System.out.println("3. ContextStoppedEvent - ApplicationContext stopped");
        System.out.println("4. ContextClosedEvent - ApplicationContext closing");
    }
    
    private static void demonstrateCustomEvents() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CUSTOM APPLICATION EVENTS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n- Extend ApplicationEvent");
        System.out.println("- Pass event source to constructor");
        System.out.println("- Add custom properties");
        System.out.println("- Publish via ApplicationEventPublisher");
    }
}

/**
 * Custom Application Events
 */
class CustomApplicationEvent extends ApplicationEvent {
    private final String eventType;
    private final LocalDateTime timestamp;
    
    public CustomApplicationEvent(Object source, String eventType) {
        super(source);
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getEventType() { return eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

/**
 * User Registration Application Event
 */
class UserRegistrationEvent extends ApplicationEvent {
    private final String userId;
    private final String username;
    private final String email;
    private final LocalDateTime registrationTime;
    
    public UserRegistrationEvent(Object source, String userId, String username, String email) {
        super(source);
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.registrationTime = LocalDateTime.now();
    }
    
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public LocalDateTime getRegistrationTime() { return registrationTime; }
}

/**
 * Order Completed Application Event
 */
class OrderCompletedEvent extends ApplicationEvent {
    private final String orderId;
    private final String customerId;
    private final double totalAmount;
    private final LocalDateTime completionTime;
    
    public OrderCompletedEvent(Object source, String orderId, String customerId, double totalAmount) {
        super(source);
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.completionTime = LocalDateTime.now();
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getCompletionTime() { return completionTime; }
}

/**
 * System Alert Application Event
 */
class SystemAlertEvent extends ApplicationEvent {
    private final String alertLevel;
    private final String message;
    private final LocalDateTime alertTime;
    
    public SystemAlertEvent(Object source, String alertLevel, String message) {
        super(source);
        this.alertLevel = alertLevel;
        this.message = message;
        this.alertTime = LocalDateTime.now();
    }
    
    public String getAlertLevel() { return alertLevel; }
    public String getMessage() { return message; }
    public LocalDateTime getAlertTime() { return alertTime; }
}

/**
 * Context Event Listener (listening to built-in context events)
 */
@Component
class ContextEventListener {
    
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        System.out.println("\n[ContextEventListener] ========================================");
        System.out.println("[ContextEventListener] ContextRefreshedEvent received!");
        System.out.println("[ContextEventListener] Application context initialized or refreshed");
        System.out.println("[ContextEventListener] Timestamp: " + LocalDateTime.now());
        System.out.println("[ContextEventListener] ========================================\n");
    }
    
    @EventListener
    public void handleContextStart(ContextStartedEvent event) {
        System.out.println("[ContextEventListener] ContextStartedEvent received!");
        System.out.println("[ContextEventListener] Application context explicitly started");
    }
    
    @EventListener
    public void handleContextStop(ContextStoppedEvent event) {
        System.out.println("[ContextEventListener] ContextStoppedEvent received!");
        System.out.println("[ContextEventListener] Application context stopped");
    }
    
    @EventListener
    public void handleContextClose(ContextClosedEvent event) {
        System.out.println("\n[ContextEventListener] ========================================");
        System.out.println("[ContextEventListener] ContextClosedEvent received!");
        System.out.println("[ContextEventListener] Application context is closing");
        System.out.println("[ContextEventListener] Timestamp: " + LocalDateTime.now());
        System.out.println("[ContextEventListener] ========================================\n");
    }
}

/**
 * Spring Boot Event Listener (listening to SpringBoot-specific events)
 */
@Component
class SpringBootEventListener {
    
    @EventListener
    public void handleApplicationStarting(ApplicationStartingEvent event) {
        // Note: This won't be caught by @EventListener as it fires before ApplicationContext is created
        System.out.println("[SpringBootEventListener] ApplicationStartingEvent - App is starting");
    }
    
    @EventListener
    public void handleApplicationStarted(ApplicationStartedEvent event) {
        System.out.println("[SpringBootEventListener] ApplicationStartedEvent - App has started");
    }
    
    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        System.out.println("\n[SpringBootEventListener] ========================================");
        System.out.println("[SpringBootEventListener] ApplicationReadyEvent - App is ready!");
        System.out.println("[SpringBootEventListener] Application can now accept traffic");
        System.out.println("[SpringBootEventListener] Timestamp: " + LocalDateTime.now());
        System.out.println("[SpringBootEventListener] ========================================\n");
    }
    
    @EventListener
    public void handleApplicationFailed(ApplicationFailedEvent event) {
        System.err.println("[SpringBootEventListener] ApplicationFailedEvent - App failed to start");
        if (event.getException() != null) {
            System.err.println("[SpringBootEventListener] Exception: " + event.getException().getMessage());
        }
    }
}

/**
 * Custom Event Listener (using ApplicationListener interface)
 */
@Component
class CustomEventApplicationListener implements ApplicationListener<CustomApplicationEvent> {
    
    @Override
    public void onApplicationEvent(CustomApplicationEvent event) {
        System.out.printf("[CustomEventApplicationListener] CustomApplicationEvent received!%n");
        System.out.printf("[CustomEventApplicationListener] Type: %s, Source: %s%n",
            event.getEventType(), event.getSource().getClass().getSimpleName());
    }
}

/**
 * User Event Listener
 */
@Component
class UserEventListener {
    
    @EventListener
    public void handleUserRegistration(UserRegistrationEvent event) {
        System.out.printf("[UserEventListener] User registered: %s (%s)%n",
            event.getUsername(), event.getEmail());
        System.out.printf("[UserEventListener] Registration time: %s%n",
            event.getRegistrationTime());
    }
}

/**
 * Order Event Listener
 */
@Component
class OrderEventListener {
    
    @EventListener
    public void handleOrderCompleted(OrderCompletedEvent event) {
        System.out.printf("[OrderEventListener] Order completed: %s%n", event.getOrderId());
        System.out.printf("[OrderEventListener] Customer: %s, Amount: $%.2f%n",
            event.getCustomerId(), event.getTotalAmount());
    }
}

/**
 * System Alert Listener
 */
@Component
class SystemAlertListener {
    
    @EventListener
    public void handleSystemAlert(SystemAlertEvent event) {
        System.out.printf("[SystemAlertListener] ALERT [%s]: %s%n",
            event.getAlertLevel(), event.getMessage());
    }
}

/**
 * Event History Tracker
 */
@Component
class ApplicationEventHistory {
    private final List<EventRecord> events = new ArrayList<>();
    
    @EventListener
    public void recordCustomEvent(CustomApplicationEvent event) {
        recordEvent(event, "CustomApplicationEvent");
    }
    
    @EventListener
    public void recordUserRegistration(UserRegistrationEvent event) {
        recordEvent(event, "UserRegistrationEvent");
    }
    
    @EventListener
    public void recordOrderCompleted(OrderCompletedEvent event) {
        recordEvent(event, "OrderCompletedEvent");
    }
    
    @EventListener
    public void recordSystemAlert(SystemAlertEvent event) {
        recordEvent(event, "SystemAlertEvent");
    }
    
    private void recordEvent(ApplicationEvent event, String eventType) {
        events.add(new EventRecord(eventType, event.getSource().getClass().getSimpleName(), 
            LocalDateTime.now()));
    }
    
    public List<EventRecord> getHistory() {
        return new ArrayList<>(events);
    }
    
    static class EventRecord {
        private final String eventType;
        private final String source;
        private final LocalDateTime timestamp;
        
        public EventRecord(String eventType, String source, LocalDateTime timestamp) {
            this.eventType = eventType;
            this.source = source;
            this.timestamp = timestamp;
        }
        
        public String getEventType() { return eventType; }
        public String getSource() { return source; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}

/**
 * Custom Event Publisher Service
 */
@Service
class CustomEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public CustomEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void publishCustomEvent(String eventType) {
        CustomApplicationEvent event = new CustomApplicationEvent(this, eventType);
        eventPublisher.publishEvent(event);
        System.out.printf("[CustomEventPublisher] Published CustomApplicationEvent: %s%n", eventType);
    }
    
    public void publishUserRegistration(String userId, String username, String email) {
        UserRegistrationEvent event = new UserRegistrationEvent(this, userId, username, email);
        eventPublisher.publishEvent(event);
        System.out.printf("[CustomEventPublisher] Published UserRegistrationEvent for %s%n", username);
    }
    
    public void publishOrderCompleted(String orderId, String customerId, double amount) {
        OrderCompletedEvent event = new OrderCompletedEvent(this, orderId, customerId, amount);
        eventPublisher.publishEvent(event);
        System.out.printf("[CustomEventPublisher] Published OrderCompletedEvent: %s%n", orderId);
    }
    
    public void publishSystemAlert(String level, String message) {
        SystemAlertEvent event = new SystemAlertEvent(this, level, message);
        eventPublisher.publishEvent(event);
        System.out.printf("[CustomEventPublisher] Published SystemAlertEvent: %s%n", level);
    }
}

/**
 * Context Lifecycle Manager
 */
@Service
class ContextLifecycleManager {
    
    private final ApplicationContext applicationContext;
    
    public ContextLifecycleManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    public Map<String, String> getContextInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("displayName", applicationContext.getDisplayName());
        info.put("id", applicationContext.getId());
        info.put("startupDate", new Date(applicationContext.getStartupDate()).toString());
        info.put("beanDefinitionCount", String.valueOf(applicationContext.getBeanDefinitionCount()));
        
        return info;
    }
}

/**
 * REST Controller for Application Events
 */
@RestController
@RequestMapping("/api/app-events")
class ApplicationEventController {
    
    private final CustomEventPublisher customEventPublisher;
    private final ApplicationEventHistory eventHistory;
    private final ContextLifecycleManager lifecycleManager;
    
    public ApplicationEventController(CustomEventPublisher customEventPublisher,
                                     ApplicationEventHistory eventHistory,
                                     ContextLifecycleManager lifecycleManager) {
        this.customEventPublisher = customEventPublisher;
        this.eventHistory = eventHistory;
        this.lifecycleManager = lifecycleManager;
    }
    
    @PostMapping("/custom")
    public Map<String, String> publishCustomEvent(@RequestParam String eventType) {
        customEventPublisher.publishCustomEvent(eventType);
        
        return Map.of(
            "status", "published",
            "eventType", "CustomApplicationEvent",
            "customType", eventType
        );
    }
    
    @PostMapping("/user-registration")
    public Map<String, String> publishUserRegistration(
            @RequestParam String userId,
            @RequestParam String username,
            @RequestParam String email) {
        
        customEventPublisher.publishUserRegistration(userId, username, email);
        
        return Map.of(
            "status", "published",
            "eventType", "UserRegistrationEvent",
            "userId", userId
        );
    }
    
    @PostMapping("/order-completed")
    public Map<String, Object> publishOrderCompleted(
            @RequestParam String orderId,
            @RequestParam String customerId,
            @RequestParam double amount) {
        
        customEventPublisher.publishOrderCompleted(orderId, customerId, amount);
        
        return Map.of(
            "status", "published",
            "eventType", "OrderCompletedEvent",
            "orderId", orderId,
            "amount", amount
        );
    }
    
    @PostMapping("/system-alert")
    public Map<String, String> publishSystemAlert(
            @RequestParam String level,
            @RequestParam String message) {
        
        customEventPublisher.publishSystemAlert(level, message);
        
        return Map.of(
            "status", "published",
            "eventType", "SystemAlertEvent",
            "level", level
        );
    }
    
    @GetMapping("/history")
    public List<ApplicationEventHistory.EventRecord> getEventHistory() {
        return eventHistory.getHistory();
    }
    
    @GetMapping("/context-info")
    public Map<String, String> getContextInfo() {
        return lifecycleManager.getContextInfo();
    }
}
