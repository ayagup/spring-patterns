package com.example.monitoring.auditing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Auditing Pattern - Demonstrates Spring Boot Auditing and Audit Events
 * 
 * This pattern shows how to:
 * 1. Enable JPA auditing with @EnableJpaAuditing
 * 2. Use @CreatedBy and @LastModifiedBy annotations
 * 3. Use @CreatedDate and @LastModifiedDate annotations
 * 4. Implement AuditEventRepository for custom audit storage
 * 5. Publish custom audit events
 * 6. Listen to audit events with @EventListener
 * 7. Track security events and authentication
 * 8. Audit entity changes and modifications
 * 9. Create audit trails for business operations
 * 10. Query and retrieve audit history
 * 
 * Key Concepts:
 * - @EnableJpaAuditing: Enable JPA auditing
 * - @EntityListeners: Register entity listeners
 * - @CreatedBy/@LastModifiedBy: Track who created/modified
 * - @CreatedDate/@LastModifiedDate: Track when created/modified
 * - AuditEvent: Represents an audit event
 * - AuditEventRepository: Store and retrieve audit events
 * - ApplicationEventPublisher: Publish audit events
 * 
 * Dependencies:
 * - spring-boot-starter-data-jpa
 * - spring-boot-starter-security
 * - spring-boot-starter-actuator
 * 
 * Access:
 * GET /actuator/auditevents
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class AuditingPattern {

    public static void main(String[] args) {
        SpringApplication.run(AuditingPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("AUDITING PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateAuditingConcepts();
        demonstrateEntityAuditing();
        demonstrateSecurityAuditing();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("AUDIT ENDPOINTS");
        System.out.println("=".repeat(80));
        System.out.println("\nAudit Events: GET /actuator/auditevents");
        System.out.println("Custom Audit API: GET /api/audit/*");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("AUDIT FEATURES");
        System.out.println("=".repeat(80));
        System.out.println("\n1. Entity Auditing");
        System.out.println("   - Automatic tracking of created/modified by/date");
        System.out.println("   - JPA entity listeners");
        
        System.out.println("\n2. Security Auditing");
        System.out.println("   - Authentication events");
        System.out.println("   - Authorization failures");
        System.out.println("   - Security-related actions");
        
        System.out.println("\n3. Business Auditing");
        System.out.println("   - Order processing");
        System.out.println("   - Payment transactions");
        System.out.println("   - User actions");
        
        System.out.println("\n4. Change Tracking");
        System.out.println("   - Field-level changes");
        System.out.println("   - Before/after values");
        System.out.println("   - Change history");
        
        System.out.println("\nApplication is running. Check audit events at /actuator/auditevents");
        System.out.println("Press Ctrl+C to stop.\n");
    }
    
    private static void demonstrateAuditingConcepts() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("AUDITING CONCEPTS");
        System.out.println("=".repeat(80));
        
        System.out.println("\nWhy Audit?");
        System.out.println("- Compliance requirements (SOX, HIPAA, GDPR)");
        System.out.println("- Security incident investigation");
        System.out.println("- User activity tracking");
        System.out.println("- Debugging and troubleshooting");
        System.out.println("- Business analytics and insights");
        
        System.out.println("\nWhat to Audit?");
        System.out.println("- Authentication and authorization events");
        System.out.println("- Data access and modifications");
        System.out.println("- Administrative actions");
        System.out.println("- Business transactions");
        System.out.println("- Security violations");
    }
    
    private static void demonstrateEntityAuditing() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ENTITY AUDITING");
        System.out.println("=".repeat(80));
        
        System.out.println("\nJPA Auditing Annotations:");
        System.out.println("@CreatedBy      - Who created the entity");
        System.out.println("@CreatedDate    - When entity was created");
        System.out.println("@LastModifiedBy - Who last modified the entity");
        System.out.println("@LastModifiedDate - When entity was last modified");
        System.out.println("\n@EntityListeners(AuditingEntityListener.class) - Enable auditing");
    }
    
    private static void demonstrateSecurityAuditing() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SECURITY AUDITING");
        System.out.println("=".repeat(80));
        
        System.out.println("\nSecurity Events:");
        System.out.println("AUTHENTICATION_SUCCESS   - User logged in");
        System.out.println("AUTHENTICATION_FAILURE   - Failed login attempt");
        System.out.println("AUTHORIZATION_FAILURE    - Access denied");
        System.out.println("LOGOUT                   - User logged out");
        System.out.println("PASSWORD_CHANGE          - Password modified");
        System.out.println("ACCOUNT_LOCKED           - Account locked");
    }
}

/**
 * JPA Auditing Configuration
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class JpaAuditingConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
}

/**
 * Auditor provider implementation
 */
class AuditorAwareImpl implements AuditorAware<String> {
    
    @Override
    public Optional<String> getCurrentAuditor() {
        // In real application, get from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return Optional.of(authentication.getName());
        }
        return Optional.of("system");
    }
}

/**
 * Base auditable entity
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
abstract class AuditableEntity {
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
    
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    // Getters and setters
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    
    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { 
        this.lastModifiedDate = lastModifiedDate; 
    }
}

/**
 * Example auditable entity - User
 */
@Entity
@Table(name = "users")
class User extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    private boolean active;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

/**
 * Example auditable entity - Order
 */
@Entity
@Table(name = "orders")
class Order extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String orderNumber;
    private Double amount;
    private String status;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

/**
 * Custom Audit Event Repository
 */
@Component
class CustomAuditEventRepository implements AuditEventRepository {
    
    private final Queue<AuditEvent> auditEvents = new ConcurrentLinkedQueue<>();
    private static final int MAX_EVENTS = 1000;
    
    @Override
    public void add(AuditEvent event) {
        auditEvents.offer(event);
        
        // Keep only last MAX_EVENTS
        while (auditEvents.size() > MAX_EVENTS) {
            auditEvents.poll();
        }
        
        // Log audit event
        System.out.printf("[AUDIT] %s - %s by %s at %s%n",
            event.getType(),
            event.getData(),
            event.getPrincipal(),
            event.getTimestamp()
        );
    }
    
    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {
        return auditEvents.stream()
            .filter(event -> principal == null || principal.equals(event.getPrincipal()))
            .filter(event -> after == null || event.getTimestamp().isAfter(after))
            .filter(event -> type == null || type.equals(event.getType()))
            .collect(Collectors.toList());
    }
}

/**
 * Audit event publisher service
 */
@Service
class AuditEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public AuditEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void publishAuthenticationSuccess(String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("timestamp", LocalDateTime.now());
        data.put("ipAddress", "127.0.0.1");
        
        AuditEvent auditEvent = new AuditEvent(username, "AUTHENTICATION_SUCCESS", data);
        eventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));
    }
    
    public void publishAuthenticationFailure(String username, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("reason", reason);
        data.put("timestamp", LocalDateTime.now());
        
        AuditEvent auditEvent = new AuditEvent(username, "AUTHENTICATION_FAILURE", data);
        eventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));
    }
    
    public void publishAuthorizationFailure(String username, String resource) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("resource", resource);
        data.put("timestamp", LocalDateTime.now());
        
        AuditEvent auditEvent = new AuditEvent(username, "AUTHORIZATION_FAILURE", data);
        eventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));
    }
    
    public void publishOrderCreated(String username, String orderNumber, double amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderNumber", orderNumber);
        data.put("amount", amount);
        data.put("timestamp", LocalDateTime.now());
        
        AuditEvent auditEvent = new AuditEvent(username, "ORDER_CREATED", data);
        eventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));
    }
    
    public void publishOrderStatusChanged(String username, String orderNumber, 
                                         String oldStatus, String newStatus) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderNumber", orderNumber);
        data.put("oldStatus", oldStatus);
        data.put("newStatus", newStatus);
        data.put("timestamp", LocalDateTime.now());
        
        AuditEvent auditEvent = new AuditEvent(username, "ORDER_STATUS_CHANGED", data);
        eventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));
    }
    
    public void publishPaymentProcessed(String username, String paymentId, double amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("paymentId", paymentId);
        data.put("amount", amount);
        data.put("timestamp", LocalDateTime.now());
        
        AuditEvent auditEvent = new AuditEvent(username, "PAYMENT_PROCESSED", data);
        eventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));
    }
    
    public void publishDataAccessed(String username, String entityType, Long entityId) {
        Map<String, Object> data = new HashMap<>();
        data.put("entityType", entityType);
        data.put("entityId", entityId);
        data.put("action", "READ");
        data.put("timestamp", LocalDateTime.now());
        
        AuditEvent auditEvent = new AuditEvent(username, "DATA_ACCESSED", data);
        eventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));
    }
    
    public void publishDataModified(String username, String entityType, Long entityId,
                                   Map<String, Object> changes) {
        Map<String, Object> data = new HashMap<>();
        data.put("entityType", entityType);
        data.put("entityId", entityId);
        data.put("action", "UPDATE");
        data.put("changes", changes);
        data.put("timestamp", LocalDateTime.now());
        
        AuditEvent auditEvent = new AuditEvent(username, "DATA_MODIFIED", data);
        eventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));
    }
    
    public void publishConfigurationChanged(String username, String key, 
                                           String oldValue, String newValue) {
        Map<String, Object> data = new HashMap<>();
        data.put("configKey", key);
        data.put("oldValue", oldValue);
        data.put("newValue", newValue);
        data.put("timestamp", LocalDateTime.now());
        
        AuditEvent auditEvent = new AuditEvent(username, "CONFIGURATION_CHANGED", data);
        eventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));
    }
}

/**
 * Audit event listener
 */
@Component
class AuditEventListener {
    
    private final CustomAuditEventRepository auditEventRepository;
    
    public AuditEventListener(CustomAuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }
    
    @EventListener
    public void onAuditEvent(AuditApplicationEvent auditEvent) {
        AuditEvent event = auditEvent.getAuditEvent();
        auditEventRepository.add(event);
        
        // Additional processing based on event type
        switch (event.getType()) {
            case "AUTHENTICATION_FAILURE":
                handleAuthenticationFailure(event);
                break;
            case "AUTHORIZATION_FAILURE":
                handleAuthorizationFailure(event);
                break;
            case "PAYMENT_PROCESSED":
                handlePaymentProcessed(event);
                break;
            default:
                // Log or process other events
                break;
        }
    }
    
    private void handleAuthenticationFailure(AuditEvent event) {
        // Check for brute force attacks
        System.out.println("WARNING: Authentication failure for " + event.getPrincipal());
    }
    
    private void handleAuthorizationFailure(AuditEvent event) {
        // Log unauthorized access attempts
        System.out.println("WARNING: Authorization failure for " + event.getPrincipal());
    }
    
    private void handlePaymentProcessed(AuditEvent event) {
        // Notify relevant systems
        System.out.println("INFO: Payment processed: " + event.getData());
    }
}

/**
 * Service demonstrating auditing in action
 */
@Service
class OrderAuditService {
    
    private final AuditEventPublisher auditPublisher;
    private final Map<String, Order> orders = new HashMap<>();
    
    public OrderAuditService(AuditEventPublisher auditPublisher) {
        this.auditPublisher = auditPublisher;
        
        // Initialize with sample orders
        Order order1 = new Order();
        order1.setId(1L);
        order1.setOrderNumber("ORD-001");
        order1.setAmount(299.99);
        order1.setStatus("PENDING");
        orders.put("ORD-001", order1);
    }
    
    public Order createOrder(String username, String orderNumber, double amount) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setAmount(amount);
        order.setStatus("PENDING");
        
        orders.put(orderNumber, order);
        
        // Audit the order creation
        auditPublisher.publishOrderCreated(username, orderNumber, amount);
        
        return order;
    }
    
    public void updateOrderStatus(String username, String orderNumber, String newStatus) {
        Order order = orders.get(orderNumber);
        if (order != null) {
            String oldStatus = order.getStatus();
            order.setStatus(newStatus);
            
            // Audit the status change
            auditPublisher.publishOrderStatusChanged(username, orderNumber, oldStatus, newStatus);
        }
    }
    
    public void processPayment(String username, String orderNumber) {
        Order order = orders.get(orderNumber);
        if (order != null) {
            String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
            
            // Audit the payment
            auditPublisher.publishPaymentProcessed(username, paymentId, order.getAmount());
            
            updateOrderStatus(username, orderNumber, "PAID");
        }
    }
    
    public Order getOrder(String username, String orderNumber) {
        Order order = orders.get(orderNumber);
        if (order != null) {
            // Audit data access
            auditPublisher.publishDataAccessed(username, "Order", order.getId());
        }
        return order;
    }
}

/**
 * REST Controller demonstrating audit events
 */
@RestController
@RequestMapping("/api/audit")
class AuditController {
    
    private final OrderAuditService orderAuditService;
    private final AuditEventPublisher auditPublisher;
    private final CustomAuditEventRepository auditEventRepository;
    
    public AuditController(OrderAuditService orderAuditService,
                          AuditEventPublisher auditPublisher,
                          CustomAuditEventRepository auditEventRepository) {
        this.orderAuditService = orderAuditService;
        this.auditPublisher = auditPublisher;
        this.auditEventRepository = auditEventRepository;
    }
    
    @PostMapping("/orders")
    public Order createOrder(@RequestParam String orderNumber, @RequestParam double amount) {
        String username = getCurrentUsername();
        return orderAuditService.createOrder(username, orderNumber, amount);
    }
    
    @PutMapping("/orders/{orderNumber}/status")
    public void updateStatus(@PathVariable String orderNumber, @RequestParam String status) {
        String username = getCurrentUsername();
        orderAuditService.updateOrderStatus(username, orderNumber, status);
    }
    
    @PostMapping("/orders/{orderNumber}/payment")
    public void processPayment(@PathVariable String orderNumber) {
        String username = getCurrentUsername();
        orderAuditService.processPayment(username, orderNumber);
    }
    
    @GetMapping("/orders/{orderNumber}")
    public Order getOrder(@PathVariable String orderNumber) {
        String username = getCurrentUsername();
        return orderAuditService.getOrder(username, orderNumber);
    }
    
    @GetMapping("/events")
    public List<AuditEvent> getAuditEvents(@RequestParam(required = false) String principal,
                                          @RequestParam(required = false) String type) {
        return auditEventRepository.find(principal, null, type);
    }
    
    @PostMapping("/simulate/auth-success")
    public String simulateAuthSuccess() {
        auditPublisher.publishAuthenticationSuccess("john.doe");
        return "Authentication success event published";
    }
    
    @PostMapping("/simulate/auth-failure")
    public String simulateAuthFailure() {
        auditPublisher.publishAuthenticationFailure("unknown.user", "Invalid credentials");
        return "Authentication failure event published";
    }
    
    private String getCurrentUsername() {
        // In real application, get from SecurityContext
        return "demo-user";
    }
}
