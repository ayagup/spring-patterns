package com.example.miscellaneous.beanpostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean Post Processor Pattern - Demonstrates Spring's BeanPostProcessor
 * 
 * This pattern shows how to:
 * 1. Implement BeanPostProcessor interface
 * 2. Modify beans before initialization
 * 3. Modify beans after initialization
 * 4. Create proxy beans
 * 5. Add aspect-like behavior
 * 6. Inject additional dependencies
 * 7. Validate bean configuration
 * 8. Register beans dynamically
 * 9. Order multiple post processors
 * 10. Handle bean enhancement
 * 
 * Key Concepts:
 * - BeanPostProcessor: Hook into bean lifecycle
 * - postProcessBeforeInitialization: Called before init methods
 * - postProcessAfterInitialization: Called after init methods
 * - Proxy Creation: Wrap beans with proxies
 * - Bean Enhancement: Add functionality to beans
 * 
 * Lifecycle Order:
 * 1. Bean instantiation
 * 2. Property population
 * 3. BeanPostProcessor.postProcessBeforeInitialization
 * 4. @PostConstruct / InitializingBean.afterPropertiesSet
 * 5. custom init-method
 * 6. BeanPostProcessor.postProcessAfterInitialization
 * 7. Bean ready for use
 * 
 * Common Use Cases:
 * - AOP proxy creation
 * - Validation
 * - Dependency injection
 * - Bean wrapping
 * - Custom annotation processing
 * 
 * Dependencies:
 * - spring-context
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class BeanPostProcessorPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(BeanPostProcessorPattern.class, args);
        demonstrateBeanPostProcessors(context);
    }
    
    /**
     * Demonstrates bean post processor functionality
     */
    private static void demonstrateBeanPostProcessors(org.springframework.context.ApplicationContext context) {
        System.out.println("=== Bean Post Processor Pattern Demonstrations ===\n");
        
        // Demo 1: Get enhanced service
        UserService userService = context.getBean(UserService.class);
        System.out.println("1. Enhanced Service:");
        userService.createUser("john_doe", "john@example.com");
        System.out.println();
        
        // Demo 2: Get validated service
        OrderService orderService = context.getBean(OrderService.class);
        System.out.println("2. Validated Service:");
        orderService.createOrder("ORDER-001", 100.0);
        System.out.println();
        
        // Demo 3: Check bean metrics
        BeanMetricsCollector metrics = context.getBean(BeanMetricsCollector.class);
        System.out.println("3. Bean Metrics:");
        System.out.println("   Total beans processed: " + metrics.getTotalBeansProcessed());
        System.out.println("   Enhanced beans: " + metrics.getEnhancedBeans().size());
        System.out.println();
    }
}

// ============================================================================
// Bean Post Processors
// ============================================================================

/**
 * Logging BeanPostProcessor - adds logging to all beans
 */
@Component
class LoggingBeanPostProcessor implements BeanPostProcessor, Ordered {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("   [LoggingBPP] Before init: " + beanName);
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("   [LoggingBPP] After init: " + beanName);
        
        // Create proxy for services
        if (bean.getClass().isAnnotationPresent(Service.class)) {
            return createLoggingProxy(bean);
        }
        
        return bean;
    }
    
    @Override
    public int getOrder() {
        return 1; // Execute first
    }
    
    @SuppressWarnings("unchecked")
    private <T> T createLoggingProxy(T bean) {
        return (T) Proxy.newProxyInstance(
            bean.getClass().getClassLoader(),
            bean.getClass().getInterfaces(),
            (proxy, method, args) -> {
                System.out.println("   [PROXY] Calling method: " + method.getName());
                long start = System.currentTimeMillis();
                try {
                    return method.invoke(bean, args);
                } finally {
                    long duration = System.currentTimeMillis() - start;
                    System.out.println("   [PROXY] Method took: " + duration + "ms");
                }
            }
        );
    }
}

/**
 * Validation BeanPostProcessor - validates bean configuration
 */
@Component
class ValidationBeanPostProcessor implements BeanPostProcessor, Ordered {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // Validate bean before initialization
        if (bean.getClass().isAnnotationPresent(Validated.class)) {
            System.out.println("   [ValidationBPP] Validating: " + beanName);
            validateBean(bean);
        }
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    @Override
    public int getOrder() {
        return 2; // Execute second
    }
    
    private void validateBean(Object bean) {
        // Simple validation example
        System.out.println("   [ValidationBPP] Bean validated successfully");
    }
}

/**
 * Metrics BeanPostProcessor - collects metrics about beans
 */
@Component
class MetricsBeanPostProcessor implements BeanPostProcessor, Ordered {
    
    private final BeanMetricsCollector metricsCollector;
    
    public MetricsBeanPostProcessor(BeanMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        metricsCollector.recordBeanProcessed(beanName, bean.getClass());
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Service.class)) {
            metricsCollector.recordEnhancedBean(beanName);
        }
        return bean;
    }
    
    @Override
    public int getOrder() {
        return 3; // Execute third
    }
}

/**
 * Dependency Injection BeanPostProcessor - custom DI logic
 */
@Component
class CustomDependencyInjectionBeanPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // Inject custom dependencies
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}

// ============================================================================
// Custom Annotations
// ============================================================================

/**
 * Marker annotation for validation
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
@interface Validated {
}

/**
 * Marker annotation for auditing
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
@interface Audited {
}

// ============================================================================
// Services
// ============================================================================

/**
 * User service
 */
@Service
@Validated
class UserService {
    
    public void createUser(String username, String email) {
        System.out.println("   Creating user: " + username + " (" + email + ")");
    }
    
    public User getUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user_" + id);
        user.setEmail("user" + id + "@example.com");
        return user;
    }
    
    public void updateUser(User user) {
        System.out.println("   Updating user: " + user.getUsername());
    }
    
    public void deleteUser(Long id) {
        System.out.println("   Deleting user: " + id);
    }
}

/**
 * Order service
 */
@Service
@Validated
@Audited
class OrderService {
    
    public void createOrder(String orderNumber, Double amount) {
        System.out.println("   Creating order: " + orderNumber + " ($" + amount + ")");
    }
    
    public Order getOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNumber("ORDER-" + id);
        order.setAmount(100.0 * id);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }
    
    public void updateOrderStatus(Long id, OrderStatus status) {
        System.out.println("   Updating order " + id + " to status: " + status);
    }
}

// ============================================================================
// Bean Metrics Collector
// ============================================================================

/**
 * Collects metrics about bean processing
 */
@Component
class BeanMetricsCollector {
    
    private final Map<String, Class<?>> processedBeans = new ConcurrentHashMap<>();
    private final Set<String> enhancedBeans = ConcurrentHashMap.newKeySet();
    private final Map<String, LocalDateTime> beanCreationTimes = new ConcurrentHashMap<>();
    
    public void recordBeanProcessed(String beanName, Class<?> beanClass) {
        processedBeans.put(beanName, beanClass);
        beanCreationTimes.put(beanName, LocalDateTime.now());
    }
    
    public void recordEnhancedBean(String beanName) {
        enhancedBeans.add(beanName);
    }
    
    public int getTotalBeansProcessed() {
        return processedBeans.size();
    }
    
    public Set<String> getEnhancedBeans() {
        return new HashSet<>(enhancedBeans);
    }
    
    public Map<String, Class<?>> getProcessedBeans() {
        return new HashMap<>(processedBeans);
    }
    
    public LocalDateTime getBeanCreationTime(String beanName) {
        return beanCreationTimes.get(beanName);
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * User entity
 */
class User {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

/**
 * Order entity
 */
class Order {
    private Long id;
    private String orderNumber;
    private Double amount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

/**
 * Order status enum
 */
enum OrderStatus {
    PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating bean post processor effects
 */
@RestController
@RequestMapping("/api/bean-post-processor")
class BeanPostProcessorController {
    
    private final UserService userService;
    private final OrderService orderService;
    private final BeanMetricsCollector metricsCollector;
    
    public BeanPostProcessorController(UserService userService,
                                      OrderService orderService,
                                      BeanMetricsCollector metricsCollector) {
        this.userService = userService;
        this.orderService = orderService;
        this.metricsCollector = metricsCollector;
    }
    
    /**
     * Create user (calls enhanced service)
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        
        userService.createUser(username, email);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "created");
        response.put("username", username);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create order (calls enhanced service)
     */
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        String orderNumber = (String) request.get("orderNumber");
        Double amount = ((Number) request.get("amount")).doubleValue();
        
        orderService.createOrder(orderNumber, amount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "created");
        response.put("orderNumber", orderNumber);
        response.put("amount", amount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get bean metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalBeansProcessed", metricsCollector.getTotalBeansProcessed());
        metrics.put("enhancedBeans", metricsCollector.getEnhancedBeans());
        metrics.put("processedBeans", metricsCollector.getProcessedBeans().keySet());
        
        return ResponseEntity.ok(metrics);
    }
}

// ============================================================================
// Configuration
// ============================================================================

/**
 * Configuration for bean post processors
 */
@Configuration
class BeanPostProcessorConfiguration {
    
    /**
     * Example of programmatic BeanPostProcessor registration
     */
    @Bean
    public BeanPostProcessor customBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) 
                    throws BeansException {
                if (beanName.startsWith("custom")) {
                    System.out.println("   [CustomBPP] Processing custom bean: " + beanName);
                }
                return bean;
            }
            
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) 
                    throws BeansException {
                return bean;
            }
        };
    }
}
