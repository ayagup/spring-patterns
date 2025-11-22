package com.spring.patterns.scope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton Scope Pattern
 * 
 * The singleton scope (default scope) creates a single instance of a bean per Spring IoC container.
 * All requests for that bean return the same shared instance.
 * 
 * Characteristics:
 * - Default scope in Spring
 * - One instance per ApplicationContext
 * - Shared state across the application
 * - Thread-safe considerations required
 * - Created at startup (eager) or first request (lazy)
 * 
 * Use Cases:
 * - Stateless services
 * - Configuration objects
 * - Utility beans
 * - DAO/Repository beans
 * - Controllers and Services
 * 
 * Best Practices:
 * - Design for immutability when possible
 * - Avoid mutable state or use thread-safe collections
 * - Use for stateless or thread-safe beans
 * - Consider @Lazy for expensive beans
 */
@SpringBootApplication
public class SingletonScopePattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(SingletonScopePattern.class, args);
        
        // Demonstrate singleton behavior
        System.out.println("\n=== Singleton Scope Demonstration ===");
        
        // Get multiple references to singleton beans
        UserService service1 = context.getBean(UserService.class);
        UserService service2 = context.getBean(UserService.class);
        
        System.out.println("service1 hash: " + service1.hashCode());
        System.out.println("service2 hash: " + service2.hashCode());
        System.out.println("Same instance? " + (service1 == service2));
        
        // Demonstrate shared state
        service1.incrementCounter();
        service1.incrementCounter();
        System.out.println("Counter via service1: " + service1.getCounter());
        System.out.println("Counter via service2: " + service2.getCounter());
        
        // Get ApplicationConfig multiple times
        ApplicationConfig config1 = context.getBean(ApplicationConfig.class);
        ApplicationConfig config2 = context.getBean(ApplicationConfig.class);
        System.out.println("\nconfig1 == config2? " + (config1 == config2));
        
        // Demonstrate thread-safe singleton
        DatabaseConnectionPool pool = context.getBean(DatabaseConnectionPool.class);
        System.out.println("Connection pool size: " + pool.getPoolSize());
    }
}

/**
 * Configuration class defining singleton beans
 */
@Configuration
class SingletonBeanConfig {
    
    /**
     * Explicit singleton scope declaration (optional, as it's the default)
     */
    @Bean
    @Scope("singleton")  // Same as @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public UserService userService() {
        return new UserService();
    }
    
    /**
     * Implicit singleton scope (default)
     */
    @Bean
    public ApplicationConfig applicationConfig() {
        return new ApplicationConfig("MyApp", "1.0.0");
    }
    
    /**
     * Thread-safe singleton for resource pooling
     */
    @Bean
    public DatabaseConnectionPool connectionPool() {
        return new DatabaseConnectionPool(10);
    }
    
    /**
     * Stateless singleton service
     */
    @Bean
    public EmailService emailService() {
        return new EmailService();
    }
    
    /**
     * Singleton with expensive initialization
     */
    @Bean
    public CacheManager cacheManager() {
        System.out.println("Initializing CacheManager (expensive operation)...");
        return new CacheManager();
    }
}

/**
 * Singleton service with mutable state (requires thread-safety)
 */
class UserService {
    private final AtomicInteger counter = new AtomicInteger(0);
    private final LocalDateTime createdAt = LocalDateTime.now();
    
    public UserService() {
        System.out.println("UserService instance created at: " + createdAt);
    }
    
    public void incrementCounter() {
        counter.incrementAndGet();
    }
    
    public int getCounter() {
        return counter.get();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public String getUserInfo(String userId) {
        // Stateless operation - safe in singleton
        return "User info for: " + userId + " (served by instance created at " + createdAt + ")";
    }
}

/**
 * Immutable singleton configuration (thread-safe by design)
 */
class ApplicationConfig {
    private final String appName;
    private final String version;
    private final LocalDateTime createdAt = LocalDateTime.now();
    
    public ApplicationConfig(String appName, String version) {
        this.appName = appName;
        this.version = version;
        System.out.println("ApplicationConfig created: " + appName + " v" + version);
    }
    
    public String getAppName() {
        return appName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

/**
 * Thread-safe singleton for resource pooling
 */
class DatabaseConnectionPool {
    private final int poolSize;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    
    public DatabaseConnectionPool(int poolSize) {
        this.poolSize = poolSize;
        System.out.println("DatabaseConnectionPool initialized with size: " + poolSize);
    }
    
    public synchronized String acquireConnection() {
        if (activeConnections.get() < poolSize) {
            int connId = activeConnections.incrementAndGet();
            return "Connection-" + connId;
        }
        throw new RuntimeException("Pool exhausted");
    }
    
    public synchronized void releaseConnection(String connectionId) {
        activeConnections.decrementAndGet();
    }
    
    public int getPoolSize() {
        return poolSize;
    }
    
    public int getActiveConnections() {
        return activeConnections.get();
    }
}

/**
 * Stateless singleton service (inherently thread-safe)
 */
class EmailService {
    
    public EmailService() {
        System.out.println("EmailService initialized");
    }
    
    public String sendEmail(String to, String subject, String body) {
        // Stateless operation - no instance variables modified
        return "Email sent to " + to + " with subject: " + subject;
    }
    
    public boolean validateEmail(String email) {
        return email != null && email.contains("@");
    }
}

/**
 * Singleton with expensive initialization
 */
class CacheManager {
    private final LocalDateTime initTime = LocalDateTime.now();
    
    public CacheManager() {
        // Simulate expensive initialization
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public LocalDateTime getInitTime() {
        return initTime;
    }
    
    public Object get(String key) {
        return "Cached value for: " + key;
    }
}

/**
 * REST Controller demonstrating singleton scope usage
 */
@RestController
@RequestMapping("/api/singleton")
class SingletonController {
    
    private final UserService userService;
    private final ApplicationConfig appConfig;
    private final DatabaseConnectionPool connectionPool;
    private final EmailService emailService;
    
    // Constructor injection - all injected beans are singletons
    public SingletonController(UserService userService, 
                              ApplicationConfig appConfig,
                              DatabaseConnectionPool connectionPool,
                              EmailService emailService) {
        this.userService = userService;
        this.appConfig = appConfig;
        this.connectionPool = connectionPool;
        this.emailService = emailService;
    }
    
    @GetMapping("/user-info")
    public String getUserInfo() {
        return userService.getUserInfo("user123");
    }
    
    @GetMapping("/increment")
    public String incrementCounter() {
        userService.incrementCounter();
        return "Counter: " + userService.getCounter();
    }
    
    @GetMapping("/counter")
    public int getCounter() {
        return userService.getCounter();
    }
    
    @GetMapping("/config")
    public String getConfig() {
        return appConfig.getAppName() + " v" + appConfig.getVersion() + 
               " (created at: " + appConfig.getCreatedAt() + ")";
    }
    
    @GetMapping("/pool-status")
    public String getPoolStatus() {
        return "Pool size: " + connectionPool.getPoolSize() + 
               ", Active connections: " + connectionPool.getActiveConnections();
    }
    
    @GetMapping("/send-email")
    public String sendEmail() {
        return emailService.sendEmail("user@example.com", "Test", "Hello from singleton!");
    }
    
    @GetMapping("/service-instance")
    public String getServiceInstance() {
        return "UserService instance hash: " + userService.hashCode() + 
               ", Created at: " + userService.getCreatedAt();
    }
}

/**
 * Key Points:
 * 
 * 1. Singleton Lifecycle:
 *    - Created once per ApplicationContext
 *    - Destroyed when ApplicationContext closes
 *    - Shared across all injection points
 * 
 * 2. Thread Safety:
 *    - Stateless beans are inherently thread-safe
 *    - Use AtomicInteger, ConcurrentHashMap for mutable state
 *    - Synchronized methods for complex operations
 *    - Prefer immutability
 * 
 * 3. Memory Efficiency:
 *    - Single instance reduces memory overhead
 *    - Shared across entire application
 *    - Suitable for most Spring beans
 * 
 * 4. Performance:
 *    - No object creation overhead on each request
 *    - Faster bean lookup
 *    - Cached by Spring container
 * 
 * 5. Testing Considerations:
 *    - State persists between tests
 *    - Use @DirtiesContext to reset
 *    - Mock singletons carefully
 * 
 * 6. When to Use:
 *    ✓ Stateless services
 *    ✓ Configuration beans
 *    ✓ Repository/DAO beans
 *    ✓ Controllers
 *    ✓ Utility classes
 * 
 * 7. When NOT to Use:
 *    ✗ Beans with user-specific state
 *    ✗ Beans requiring new instance per request
 *    ✗ Non-thread-safe third-party libraries
 *    ✗ Beans with request/session-specific data
 */
