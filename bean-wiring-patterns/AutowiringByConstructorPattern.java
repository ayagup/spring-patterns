package com.spring.patterns.wiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Autowiring by Constructor Pattern
 * 
 * Constructor-based dependency injection is the RECOMMENDED approach in Spring.
 * Dependencies are injected through class constructor.
 * 
 * Characteristics:
 * - Dependencies are IMMUTABLE (final fields)
 * - All required dependencies must be satisfied
 * - Better testability (can instantiate without Spring)
 * - Makes dependencies explicit
 * - Thread-safe initialization
 * - @Autowired is OPTIONAL since Spring 4.3 for single constructor
 * 
 * Advantages:
 * ✓ Immutability (final fields)
 * ✓ Testability (easy mocking)
 * ✓ Null-safety
 * ✓ Clear dependencies
 * ✓ Prevents circular dependencies (fails fast)
 * ✓ IoC container independence
 * 
 * Types:
 * 1. Single Constructor (implicit autowiring)
 * 2. Multiple Constructors (explicit @Autowired)
 * 3. Optional Dependencies (Optional<T> or @Nullable)
 * 4. Mixed Required/Optional Dependencies
 * 
 * Use Cases:
 * - All service layer classes
 * - Controllers
 * - Any class with mandatory dependencies
 * - Immutable configurations
 */
@SpringBootApplication
public class AutowiringByConstructorPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AutowiringByConstructorPattern.class, args);
        
        System.out.println("\n=== Autowiring by Constructor Pattern ===");
        
        // Demonstrate constructor injection
        UserService userService = context.getBean(UserService.class);
        System.out.println("\nUserService dependencies:");
        System.out.println("  - UserRepository: " + (userService.hasRepository() ? "✓" : "✗"));
        System.out.println("  - EmailService: " + (userService.hasEmailService() ? "✓" : "✗"));
        System.out.println("  - NotificationService: " + (userService.hasNotificationService() ? "✓" : "✗"));
        
        // Test functionality
        userService.createUser("john@example.com", "John Doe");
        
        // Demonstrate optional dependencies
        ReportService reportService = context.getBean(ReportService.class);
        reportService.generateReport("Q4 Sales");
    }
}

/**
 * Configuration
 */
@Configuration
class ConstructorWiringConfig {
    
    @Bean
    public AuditService auditService() {
        return new AuditService();
    }
}

/**
 * Repository layer
 */
@Repository
class UserRepository {
    
    public UserRepository() {
        System.out.println("UserRepository created");
    }
    
    public String save(String email, String name) {
        System.out.println("User saved: " + name + " (" + email + ")");
        return "USER-" + System.currentTimeMillis();
    }
    
    public String findById(String id) {
        return "User: " + id;
    }
}

/**
 * Email service
 */
@Component
class EmailService {
    
    public EmailService() {
        System.out.println("EmailService created");
    }
    
    public void sendEmail(String to, String subject, String body) {
        System.out.println("Email sent to " + to + ": " + subject);
    }
}

/**
 * Notification service
 */
@Component
class NotificationService {
    
    public NotificationService() {
        System.out.println("NotificationService created");
    }
    
    public void notify(String message) {
        System.out.println("Notification: " + message);
    }
}

/**
 * Audit service
 */
class AuditService {
    
    public AuditService() {
        System.out.println("AuditService created");
    }
    
    public void logAction(String action) {
        System.out.println("Audit log: " + action);
    }
}

/**
 * Example 1: Single Constructor - Implicit Autowiring (Recommended)
 * No @Autowired needed since Spring 4.3+ for single constructor
 */
@Service
class UserService {
    
    // All dependencies are final - immutable
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    
    // @Autowired is OPTIONAL for single constructor
    public UserService(UserRepository userRepository,
                      EmailService emailService,
                      NotificationService notificationService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        System.out.println("UserService created via constructor injection");
    }
    
    public String createUser(String email, String name) {
        String userId = userRepository.save(email, name);
        emailService.sendEmail(email, "Welcome", "Welcome " + name);
        notificationService.notify("User created: " + name);
        return userId;
    }
    
    public boolean hasRepository() { return userRepository != null; }
    public boolean hasEmailService() { return emailService != null; }
    public boolean hasNotificationService() { return notificationService != null; }
}

/**
 * Example 2: Multiple Constructors - Explicit @Autowired Required
 */
@Service
class OrderService {
    
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    
    // Primary constructor for full dependencies
    @Autowired // REQUIRED when multiple constructors exist
    public OrderService(UserRepository userRepository,
                       NotificationService notificationService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
        System.out.println("OrderService created with all dependencies");
    }
    
    // Secondary constructor for testing or manual instantiation
    public OrderService(UserRepository userRepository,
                       NotificationService notificationService) {
        this(userRepository, notificationService, new AuditService());
        System.out.println("OrderService created with minimal dependencies");
    }
    
    public String createOrder(String userId, String product) {
        String orderId = "ORDER-" + System.currentTimeMillis();
        auditService.logAction("Order created: " + orderId);
        notificationService.notify("Order " + orderId + " created for " + userId);
        return orderId;
    }
}

/**
 * Example 3: Optional Dependencies using Optional<T>
 */
@Service
class ReportService {
    
    private final UserRepository userRepository;
    private final Optional<EmailService> emailService;
    private final Optional<AuditService> auditService;
    
    public ReportService(UserRepository userRepository,
                        Optional<EmailService> emailService,
                        Optional<AuditService> auditService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.auditService = auditService;
        System.out.println("ReportService created");
        System.out.println("  - EmailService present: " + emailService.isPresent());
        System.out.println("  - AuditService present: " + auditService.isPresent());
    }
    
    public String generateReport(String reportName) {
        String reportId = "REPORT-" + System.currentTimeMillis();
        
        // Use optional dependencies if available
        emailService.ifPresent(service -> 
            service.sendEmail("admin@example.com", "Report Ready", reportName)
        );
        
        auditService.ifPresent(service -> 
            service.logAction("Report generated: " + reportName)
        );
        
        return reportId;
    }
}

/**
 * Example 4: Optional Dependencies using @Nullable
 */
@Service
class AnalyticsService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    public AnalyticsService(UserRepository userRepository,
                           @Nullable EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        System.out.println("AnalyticsService created");
        System.out.println("  - EmailService: " + (emailService != null ? "available" : "not available"));
    }
    
    public void trackEvent(String event) {
        System.out.println("Event tracked: " + event);
        
        if (emailService != null) {
            emailService.sendEmail("analytics@example.com", "Event", event);
        }
    }
}

/**
 * Example 5: Immutable Configuration using Constructor Injection
 */
@Component
class DatabaseConfig {
    
    private final String url;
    private final String username;
    private final int maxConnections;
    
    public DatabaseConfig() {
        // Default values
        this.url = "jdbc:postgresql://localhost:5432/mydb";
        this.username = "admin";
        this.maxConnections = 10;
        System.out.println("DatabaseConfig created with defaults");
    }
    
    // Getters only - no setters (immutable)
    public String getUrl() { return url; }
    public String getUsername() { return username; }
    public int getMaxConnections() { return maxConnections; }
}

/**
 * Example 6: Service with Configuration Dependency
 */
@Service
class DatabaseService {
    
    private final DatabaseConfig config;
    private final AuditService auditService;
    
    public DatabaseService(DatabaseConfig config, AuditService auditService) {
        this.config = config;
        this.auditService = auditService;
        System.out.println("DatabaseService initialized");
        System.out.println("  - DB URL: " + config.getUrl());
        System.out.println("  - Max Connections: " + config.getMaxConnections());
    }
    
    public String connect() {
        auditService.logAction("Database connection established");
        return "Connected to: " + config.getUrl();
    }
}

/**
 * REST Controller with Constructor Injection
 */
@RestController
@RequestMapping("/api/constructor-wiring")
class ConstructorWiringController {
    
    // All dependencies injected via constructor
    private final UserService userService;
    private final OrderService orderService;
    private final ReportService reportService;
    private final DatabaseService databaseService;
    
    // Single constructor - @Autowired optional
    public ConstructorWiringController(UserService userService,
                                      OrderService orderService,
                                      ReportService reportService,
                                      DatabaseService databaseService) {
        this.userService = userService;
        this.orderService = orderService;
        this.reportService = reportService;
        this.databaseService = databaseService;
    }
    
    @GetMapping("/user")
    public String createUser() {
        return userService.createUser("test@example.com", "Test User");
    }
    
    @GetMapping("/order")
    public String createOrder() {
        return orderService.createOrder("USER-123", "Laptop");
    }
    
    @GetMapping("/report")
    public String generateReport() {
        return reportService.generateReport("Monthly Sales");
    }
    
    @GetMapping("/db")
    public String connectDatabase() {
        return databaseService.connect();
    }
}

/**
 * Key Points:
 * 
 * 1. Constructor Injection Benefits:
 *    ✓ Immutability - final fields
 *    ✓ Testability - easy to mock dependencies
 *    ✓ Null-safety - NPE impossible
 *    ✓ Clear contract - all dependencies visible
 *    ✓ Fail-fast - missing dependencies detected at startup
 * 
 * 2. @Autowired on Constructor:
 *    - Optional for SINGLE constructor (Spring 4.3+)
 *    - Required for MULTIPLE constructors
 *    - Marks the constructor Spring should use
 * 
 * 3. Single Constructor (Recommended):
 *    @Service
 *    class MyService {
 *        private final Dependency dep;
 *        
 *        // @Autowired optional
 *        public MyService(Dependency dep) {
 *            this.dep = dep;
 *        }
 *    }
 * 
 * 4. Multiple Constructors:
 *    @Service
 *    class MyService {
 *        @Autowired // REQUIRED
 *        public MyService(Dep1 d1, Dep2 d2) { }
 *        
 *        public MyService(Dep1 d1) { } // For testing
 *    }
 * 
 * 5. Optional Dependencies:
 *    Option A: Optional<T>
 *    public MyService(Optional<Dependency> dep) {
 *        dep.ifPresent(d -> ...);
 *    }
 *    
 *    Option B: @Nullable
 *    public MyService(@Nullable Dependency dep) {
 *        if (dep != null) { ... }
 *    }
 * 
 * 6. Comparison with Other Injection Types:
 *    
 *    Constructor Injection:
 *    ✓ Immutable
 *    ✓ Easy to test
 *    ✓ Null-safe
 *    ✓ Prevents circular dependencies
 *    
 *    Field Injection:
 *    ✗ Mutable
 *    ✗ Hard to test
 *    ✗ Hides dependencies
 *    
 *    Setter Injection:
 *    ✗ Mutable
 *    ✓ Optional dependencies
 *    ✓ Allows reconfiguration
 * 
 * 7. Testing with Constructor Injection:
 *    @Test
 *    void testUserService() {
 *        UserRepository mockRepo = mock(UserRepository.class);
 *        EmailService mockEmail = mock(EmailService.class);
 *        NotificationService mockNotif = mock(NotificationService.class);
 *        
 *        // Easy instantiation without Spring
 *        UserService service = new UserService(mockRepo, mockEmail, mockNotif);
 *        
 *        // Test...
 *    }
 * 
 * 8. Best Practices:
 *    ✓ Use constructor injection by default
 *    ✓ Make dependencies final
 *    ✓ Avoid too many dependencies (SRP violation)
 *    ✓ Use Optional<T> for optional dependencies
 *    ✓ Keep one constructor for most cases
 * 
 * 9. Common Pitfalls:
 *    ✗ Too many constructor parameters (>5 suggests design issue)
 *    ✗ Circular dependencies (refactor to break cycle)
 *    ✗ Missing @Autowired with multiple constructors
 *    ✗ Mixing injection types in same class
 * 
 * 10. When NOT to Use:
 *     - Need to reconfigure dependencies at runtime
 *     - Working with legacy code requiring setters
 *     - JavaBeans specification compliance needed
 */
