package com.spring.patterns.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Prototype Scope Pattern
 * 
 * Prototype scope creates a new bean instance every time it is requested from the container.
 * Unlike singleton, prototype beans are not cached and no single instance is shared.
 * 
 * Characteristics:
 * - New instance on every getBean() call
 * - New instance for every injection point
 * - Spring doesn't manage complete lifecycle (no destruction callbacks by default)
 * - Each instance is independent
 * - Not cached by container
 * 
 * Use Cases:
 * - Stateful beans
 * - Command objects
 * - Task objects
 * - User-specific operations
 * - Beans with mutable state
 * 
 * Injection Strategies:
 * - ObjectFactory<T>
 * - Provider<T> (JSR-330)
 * - @Lookup method injection
 * - ApplicationContext.getBean()
 */
@SpringBootApplication
public class PrototypeScopePattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(PrototypeScopePattern.class, args);
        
        System.out.println("\n=== Prototype Scope Demonstration ===");
        
        // Get multiple instances
        ShoppingCart cart1 = context.getBean(ShoppingCart.class);
        ShoppingCart cart2 = context.getBean(ShoppingCart.class);
        
        System.out.println("cart1 ID: " + cart1.getCartId() + ", hash: " + cart1.hashCode());
        System.out.println("cart2 ID: " + cart2.getCartId() + ", hash: " + cart2.hashCode());
        System.out.println("Same instance? " + (cart1 == cart2));
        
        // Add items to different carts
        cart1.addItem("Laptop");
        cart2.addItem("Mouse");
        
        System.out.println("cart1 items: " + cart1.getItems());
        System.out.println("cart2 items: " + cart2.getItems());
        
        // Demonstrate UserSession
        UserSession session1 = context.getBean(UserSession.class);
        UserSession session2 = context.getBean(UserSession.class);
        
        System.out.println("\nsession1 ID: " + session1.getSessionId());
        System.out.println("session2 ID: " + session2.getSessionId());
        System.out.println("Different sessions? " + (!session1.getSessionId().equals(session2.getSessionId())));
    }
}

/**
 * Configuration for prototype beans
 */
@Configuration
class PrototypeBeanConfig {
    
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ShoppingCart shoppingCart() {
        return new ShoppingCart();
    }
    
    @Bean
    @Scope("prototype")
    public UserSession userSession() {
        return new UserSession();
    }
    
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public TaskExecutor taskExecutor() {
        return new TaskExecutor();
    }
    
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ReportGenerator reportGenerator() {
        return new ReportGenerator();
    }
}

/**
 * Prototype bean - new instance per request
 */
class ShoppingCart {
    private final String cartId;
    private final LocalDateTime createdAt;
    private final java.util.List<String> items = new java.util.ArrayList<>();
    
    public ShoppingCart() {
        this.cartId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        System.out.println("ShoppingCart created: " + cartId);
    }
    
    @PostConstruct
    public void init() {
        System.out.println("ShoppingCart initialized: " + cartId);
    }
    
    @PreDestroy
    public void cleanup() {
        // NOTE: @PreDestroy is NOT called for prototype beans by Spring
        System.out.println("ShoppingCart cleanup: " + cartId);
    }
    
    public void addItem(String item) {
        items.add(item);
    }
    
    public java.util.List<String> getItems() {
        return new java.util.ArrayList<>(items);
    }
    
    public String getCartId() {
        return cartId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

/**
 * Prototype bean for user sessions
 */
class UserSession {
    private final String sessionId;
    private final LocalDateTime loginTime;
    private String username;
    
    public UserSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.loginTime = LocalDateTime.now();
        System.out.println("UserSession created: " + sessionId);
    }
    
    public void login(String username) {
        this.username = username;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public String getUsername() {
        return username;
    }
}

/**
 * Prototype bean for task execution
 */
class TaskExecutor {
    private final String taskId;
    private String taskName;
    private String status = "PENDING";
    
    public TaskExecutor() {
        this.taskId = UUID.randomUUID().toString();
    }
    
    public void execute(String taskName) {
        this.taskName = taskName;
        this.status = "RUNNING";
        System.out.println("Executing task: " + taskName + " [" + taskId + "]");
        this.status = "COMPLETED";
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public String getStatus() {
        return status;
    }
}

/**
 * Prototype bean for report generation
 */
class ReportGenerator {
    private final String reportId;
    private final LocalDateTime generatedAt;
    
    public ReportGenerator() {
        this.reportId = UUID.randomUUID().toString();
        this.generatedAt = LocalDateTime.now();
    }
    
    public String generateReport(String reportType) {
        return "Report [" + reportId + "] Type: " + reportType + 
               ", Generated: " + generatedAt;
    }
    
    public String getReportId() {
        return reportId;
    }
}

/**
 * Singleton service that needs prototype beans - using ObjectFactory
 */
@Component
class OrderService {
    
    @Autowired
    private ObjectFactory<ShoppingCart> shoppingCartFactory;
    
    @Autowired
    private ObjectFactory<TaskExecutor> taskExecutorFactory;
    
    public String createOrder(String userId) {
        // Get new ShoppingCart instance
        ShoppingCart cart = shoppingCartFactory.getObject();
        cart.addItem("Product-" + System.currentTimeMillis());
        
        return "Order created for user: " + userId + 
               ", Cart ID: " + cart.getCartId() + 
               ", Items: " + cart.getItems().size();
    }
    
    public String executeTask(String taskName) {
        // Get new TaskExecutor instance
        TaskExecutor executor = taskExecutorFactory.getObject();
        executor.execute(taskName);
        return "Task executed: " + executor.getTaskId();
    }
}

/**
 * Singleton service using @Lookup method injection
 */
@Component
abstract class ReportService {
    
    // Method injection - Spring will implement this method
    @Lookup
    protected abstract ReportGenerator createReportGenerator();
    
    public String generateUserReport(String userId) {
        ReportGenerator generator = createReportGenerator();
        return generator.generateReport("User Report for: " + userId);
    }
    
    public String generateSalesReport() {
        ReportGenerator generator = createReportGenerator();
        return generator.generateReport("Sales Report");
    }
}

/**
 * Singleton service using ApplicationContext directly
 */
@Component
class SessionManager {
    
    @Autowired
    private ApplicationContext context;
    
    public UserSession createSession(String username) {
        UserSession session = context.getBean(UserSession.class);
        session.login(username);
        return session;
    }
    
    public String getSessionInfo(String username) {
        UserSession session = createSession(username);
        return "Session created for " + username + 
               ", ID: " + session.getSessionId() + 
               ", Login time: " + session.getLoginTime();
    }
}

/**
 * REST Controller demonstrating prototype scope
 */
@RestController
@RequestMapping("/api/prototype")
class PrototypeController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private ApplicationContext context;
    
    @GetMapping("/create-cart")
    public String createCart() {
        ShoppingCart cart1 = context.getBean(ShoppingCart.class);
        ShoppingCart cart2 = context.getBean(ShoppingCart.class);
        
        cart1.addItem("Item-A");
        cart2.addItem("Item-B");
        
        return "Cart 1 ID: " + cart1.getCartId() + " (items: " + cart1.getItems() + "), " +
               "Cart 2 ID: " + cart2.getCartId() + " (items: " + cart2.getItems() + ")";
    }
    
    @GetMapping("/create-order")
    public String createOrder() {
        return orderService.createOrder("user-" + System.currentTimeMillis());
    }
    
    @GetMapping("/execute-task")
    public String executeTask() {
        return orderService.executeTask("ProcessPayment");
    }
    
    @GetMapping("/generate-report")
    public String generateReport() {
        String report1 = reportService.generateUserReport("user123");
        String report2 = reportService.generateSalesReport();
        return report1 + " | " + report2;
    }
    
    @GetMapping("/create-session")
    public String createSession() {
        String session1 = sessionManager.getSessionInfo("alice");
        String session2 = sessionManager.getSessionInfo("bob");
        return session1 + " | " + session2;
    }
    
    @GetMapping("/compare-instances")
    public String compareInstances() {
        UserSession s1 = context.getBean(UserSession.class);
        UserSession s2 = context.getBean(UserSession.class);
        UserSession s3 = context.getBean(UserSession.class);
        
        return "Session 1: " + s1.getSessionId() + ", " +
               "Session 2: " + s2.getSessionId() + ", " +
               "Session 3: " + s3.getSessionId() + ", " +
               "All different? " + 
               (!s1.getSessionId().equals(s2.getSessionId()) && 
                !s2.getSessionId().equals(s3.getSessionId()));
    }
}

/**
 * Key Points:
 * 
 * 1. Lifecycle Management:
 *    - Spring calls @PostConstruct
 *    - Spring does NOT call @PreDestroy (manual cleanup needed)
 *    - Container doesn't track prototype instances
 *    - Client responsible for cleanup
 * 
 * 2. Injection Strategies:
 *    a) ObjectFactory<T> - recommended for singletons
 *    b) Provider<T> (JSR-330) - standard alternative
 *    c) @Lookup - method injection (requires abstract method)
 *    d) ApplicationContext.getBean() - direct lookup
 * 
 * 3. Memory Considerations:
 *    - Each instance consumes memory
 *    - No automatic cleanup
 *    - Can cause memory leaks if not managed
 *    - Monitor instance creation
 * 
 * 4. Performance Impact:
 *    - Object creation overhead on each request
 *    - Slower than singleton
 *    - Dependency injection overhead
 *    - Consider object pooling for expensive objects
 * 
 * 5. Thread Safety:
 *    - Each thread gets its own instance (if requested)
 *    - No shared state between instances
 *    - Inherently thread-safe (isolated state)
 * 
 * 6. When to Use:
 *    ✓ Stateful beans
 *    ✓ Command/Task objects
 *    ✓ Per-request processing
 *    ✓ User-specific data holders
 *    ✓ Mutable beans with request-scoped state
 * 
 * 7. When NOT to Use:
 *    ✗ Stateless services (use singleton)
 *    ✗ Expensive objects (consider pooling)
 *    ✗ High-frequency requests (performance impact)
 *    ✗ Configuration beans
 * 
 * 8. Common Pitfalls:
 *    - Injecting prototype into singleton directly (loses prototype behavior)
 *    - Memory leaks from uncleaned instances
 *    - Performance degradation from excessive creation
 *    - Expecting @PreDestroy to be called
 */
