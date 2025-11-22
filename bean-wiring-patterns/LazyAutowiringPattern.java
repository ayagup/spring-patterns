package com.spring.patterns.wiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

/**
 * Lazy Autowiring Pattern
 * 
 * @Lazy annotation delays bean initialization until first access.
 * By default, Spring creates all singleton beans at startup (eager initialization).
 * 
 * Characteristics:
 * - Bean created on FIRST ACCESS, not at startup
 * - Reduces startup time
 * - Saves memory if bean never used
 * - Creates proxy for lazy dependencies
 * - Can be used on beans, methods, and injection points
 * 
 * Lazy Levels:
 * 1. Bean Level - @Lazy on @Component/@Service/@Bean
 * 2. Injection Point Level - @Lazy on @Autowired
 * 3. Configuration Level - @Lazy on @Configuration
 * 
 * Use Cases:
 * - Heavy initialization (database, connections)
 * - Conditional usage (may not be needed)
 * - Break circular dependencies
 * - Improve startup time
 * - Optional features
 * - Development/testing environments
 */
@SpringBootApplication
public class LazyAutowiringPattern {

    public static void main(String[] args) {
        System.out.println("=== Application Starting ===");
        ApplicationContext context = SpringApplication.run(LazyAutowiringPattern.class, args);
        
        System.out.println("\n=== Lazy Autowiring Pattern ===");
        System.out.println("Application started - lazy beans NOT yet created");
        
        // Lazy beans are created on first access
        System.out.println("\n--- Accessing UserService ---");
        UserService userService = context.getBean(UserService.class);
        
        System.out.println("\n--- Calling createUser (triggers lazy dependency) ---");
        userService.createUser("john@example.com");
        
        System.out.println("\n--- Accessing OrderService ---");
        OrderService orderService = context.getBean(OrderService.class);
        orderService.createOrder("USER-123", "Laptop");
    }
}

/**
 * Configuration
 */
@Configuration
class LazyWiringConfig {
    
    // Eager bean - created at startup
    @Bean
    public EagerService eagerService() {
        return new EagerService();
    }
    
    // Lazy bean - created on first access
    @Bean
    @Lazy
    public LazyService lazyService() {
        return new LazyService();
    }
    
    // Heavy initialization bean - should be lazy
    @Bean
    @Lazy
    public HeavyService heavyService() {
        return new HeavyService();
    }
}

/**
 * Eager Service - Created at startup
 */
class EagerService {
    
    public EagerService() {
        System.out.println("[STARTUP] EagerService created (eager initialization)");
        simulateHeavyInit();
    }
    
    private void simulateHeavyInit() {
        // Simulates heavy initialization
    }
    
    public String process() {
        return "Eager service processing";
    }
}

/**
 * Lazy Service - Created on first access
 */
class LazyService {
    
    public LazyService() {
        System.out.println("[ON-DEMAND] LazyService created (lazy initialization)");
        simulateHeavyInit();
    }
    
    private void simulateHeavyInit() {
        System.out.println("  - Loading configuration...");
        System.out.println("  - Establishing connections...");
        System.out.println("  - Warming up caches...");
    }
    
    public String process() {
        return "Lazy service processing";
    }
}

/**
 * Heavy Service - Expensive to create
 */
class HeavyService {
    
    public HeavyService() {
        System.out.println("[ON-DEMAND] HeavyService created");
        initializeHeavyResources();
    }
    
    private void initializeHeavyResources() {
        System.out.println("  - Loading large dataset...");
        System.out.println("  - Initializing connection pool...");
        System.out.println("  - Building caches...");
    }
    
    public String processHeavyTask() {
        return "Heavy task completed";
    }
}

/**
 * Example 1: Lazy Bean Definition
 */
@Component
@Lazy  // Bean created on first access
class DatabaseService {
    
    public DatabaseService() {
        System.out.println("[ON-DEMAND] DatabaseService created");
        connectToDatabase();
    }
    
    private void connectToDatabase() {
        System.out.println("  - Establishing database connection...");
        System.out.println("  - Loading database schema...");
    }
    
    public String query(String sql) {
        System.out.println("Executing query: " + sql);
        return "Query result";
    }
}

/**
 * Example 2: Lazy Dependency Injection
 */
@Service
class UserService {
    
    // Eager dependency - created at startup
    private final EagerService eagerService;
    
    // Lazy dependency - created on first use
    @Lazy
    @Autowired
    private LazyService lazyService;
    
    // Lazy dependency via constructor
    private final HeavyService heavyService;
    
    @Autowired
    public UserService(EagerService eagerService,
                      @Lazy HeavyService heavyService) {
        this.eagerService = eagerService;
        this.heavyService = heavyService;
        System.out.println("[STARTUP] UserService created");
        System.out.println("  - EagerService injected: " + (eagerService != null));
        System.out.println("  - HeavyService proxy injected: " + (heavyService != null));
    }
    
    public String createUser(String email) {
        System.out.println("\nCreating user: " + email);
        
        // Eager service already created
        eagerService.process();
        
        // Lazy service created NOW (first access)
        System.out.println("About to use LazyService (will be created now)...");
        String result = lazyService.process();
        System.out.println(result);
        
        // Heavy service created NOW (first access)
        System.out.println("About to use HeavyService (will be created now)...");
        String heavyResult = heavyService.processHeavyTask();
        System.out.println(heavyResult);
        
        return "USER-" + System.currentTimeMillis();
    }
}

/**
 * Example 3: Breaking Circular Dependencies with @Lazy
 */
@Service
class OrderService {
    
    private final PaymentService paymentService;
    
    @Autowired
    public OrderService(@Lazy PaymentService paymentService) {
        this.paymentService = paymentService;
        System.out.println("[STARTUP] OrderService created with lazy PaymentService");
    }
    
    public String createOrder(String userId, String product) {
        System.out.println("\nCreating order: " + product);
        
        // PaymentService created when accessed
        String paymentResult = paymentService.processPayment(100.00);
        
        return "ORDER-" + System.currentTimeMillis();
    }
}

@Service
class PaymentService {
    
    private final OrderService orderService;
    
    @Autowired
    public PaymentService(@Lazy OrderService orderService) {
        this.orderService = orderService;
        System.out.println("[STARTUP] PaymentService created with lazy OrderService");
    }
    
    public String processPayment(double amount) {
        System.out.println("Processing payment: $" + amount);
        return "PAYMENT-" + System.currentTimeMillis();
    }
}

/**
 * Example 4: Conditional Lazy Loading
 */
@Component
@Lazy
class AnalyticsService {
    
    public AnalyticsService() {
        System.out.println("[ON-DEMAND] AnalyticsService created");
        initializeAnalytics();
    }
    
    private void initializeAnalytics() {
        System.out.println("  - Connecting to analytics platform...");
        System.out.println("  - Loading analytics configuration...");
    }
    
    public void trackEvent(String event) {
        System.out.println("Event tracked: " + event);
    }
}

@Service
class ReportService {
    
    // Analytics only loaded if report generation is called
    @Lazy
    @Autowired
    private AnalyticsService analyticsService;
    
    public ReportService() {
        System.out.println("[STARTUP] ReportService created (AnalyticsService NOT created yet)");
    }
    
    public String generateReport(String reportType) {
        System.out.println("\nGenerating report: " + reportType);
        
        if (reportType.equals("detailed")) {
            // Analytics created only for detailed reports
            System.out.println("Detailed report requires analytics...");
            analyticsService.trackEvent("DETAILED_REPORT_GENERATED");
        }
        
        return "REPORT-" + System.currentTimeMillis();
    }
}

/**
 * Example 5: Lazy Configuration
 */
@Configuration
@Lazy  // All beans in this config are lazy
class LazyConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        System.out.println("[ON-DEMAND] CacheManager created from lazy config");
        return new CacheManager();
    }
    
    @Bean
    public MonitoringService monitoringService() {
        System.out.println("[ON-DEMAND] MonitoringService created from lazy config");
        return new MonitoringService();
    }
}

class CacheManager {
    
    public void put(String key, Object value) {
        System.out.println("Cache put: " + key);
    }
    
    public Object get(String key) {
        return "Cached value: " + key;
    }
}

class MonitoringService {
    
    public void logMetric(String metric, double value) {
        System.out.println("Metric logged: " + metric + " = " + value);
    }
}

/**
 * Example 6: Lazy with PostConstruct
 */
@Component
@Lazy
class InitializationService {
    
    public InitializationService() {
        System.out.println("[ON-DEMAND] InitializationService constructor");
    }
    
    @PostConstruct
    public void init() {
        System.out.println("[ON-DEMAND] InitializationService @PostConstruct called");
        System.out.println("  - Running initialization logic...");
    }
    
    public String initialize() {
        return "Initialization complete";
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/lazy-wiring")
class LazyWiringController {
    
    @Lazy
    @Autowired
    private DatabaseService databaseService;
    
    @Lazy
    @Autowired
    private AnalyticsService analyticsService;
    
    @Lazy
    @Autowired
    private InitializationService initService;
    
    private final ReportService reportService;
    
    public LazyWiringController(ReportService reportService) {
        this.reportService = reportService;
        System.out.println("[STARTUP] LazyWiringController created");
    }
    
    @GetMapping("/database")
    public String queryDatabase() {
        System.out.println("\n--- Database endpoint called ---");
        return databaseService.query("SELECT * FROM users");
    }
    
    @GetMapping("/analytics")
    public String trackEvent() {
        System.out.println("\n--- Analytics endpoint called ---");
        analyticsService.trackEvent("API_CALL");
        return "Event tracked";
    }
    
    @GetMapping("/report/simple")
    public String generateSimpleReport() {
        System.out.println("\n--- Simple report endpoint called ---");
        return reportService.generateReport("simple");
    }
    
    @GetMapping("/report/detailed")
    public String generateDetailedReport() {
        System.out.println("\n--- Detailed report endpoint called ---");
        return reportService.generateReport("detailed");
    }
    
    @GetMapping("/init")
    public String initialize() {
        System.out.println("\n--- Initialization endpoint called ---");
        return initService.initialize();
    }
}

/**
 * Key Points:
 * 
 * 1. Lazy Bean Definition:
 *    @Component
 *    @Lazy
 *    class MyService { }
 *    // Created on first access
 * 
 * 2. Lazy Dependency Injection:
 *    @Autowired
 *    @Lazy
 *    private Service service;
 *    // Proxy created at startup, bean created on first use
 * 
 * 3. Lazy Constructor Injection:
 *    @Autowired
 *    public MyService(@Lazy Dependency dep) {
 *        this.dep = dep; // Proxy injected
 *    }
 * 
 * 4. Lazy vs Eager:
 *    
 *    Eager (Default):
 *    ✓ Created at startup
 *    ✓ Fail-fast (errors at startup)
 *    ✓ Ready immediately
 *    ✗ Slower startup
 *    ✗ More memory
 *    
 *    Lazy:
 *    ✓ Faster startup
 *    ✓ Less memory (if not used)
 *    ✓ On-demand creation
 *    ✗ First access slower
 *    ✗ Delayed error detection
 * 
 * 5. When to Use @Lazy:
 *    ✓ Heavy initialization (DB connections, file I/O)
 *    ✓ Conditional usage (may not be needed)
 *    ✓ Break circular dependencies
 *    ✓ Improve startup time
 *    ✓ Development/testing
 *    ✓ Optional features
 * 
 * 6. Circular Dependency Resolution:
 *    @Service
 *    class A {
 *        @Autowired
 *        public A(@Lazy B b) { }
 *    }
 *    
 *    @Service
 *    class B {
 *        @Autowired
 *        public B(@Lazy A a) { }
 *    }
 *    // @Lazy breaks the cycle
 * 
 * 7. Proxy Mechanism:
 *    - Spring creates CGLIB proxy for lazy beans
 *    - Proxy delegates to real bean on first method call
 *    - Bean initialized when method called
 *    - Subsequent calls use same instance
 * 
 * 8. Lazy Configuration:
 *    @Configuration
 *    @Lazy
 *    class Config {
 *        @Bean
 *        public Service service() { }
 *    }
 *    // ALL beans in config are lazy
 * 
 * 9. Best Practices:
 *    ✓ Use for heavy initialization
 *    ✓ Document why bean is lazy
 *    ✓ Test lazy bean access
 *    ✓ Consider startup time vs runtime trade-off
 *    ✓ Don't overuse (most beans should be eager)
 * 
 * 10. Common Pitfalls:
 *     ✗ Overusing @Lazy (delayed error detection)
 *     ✗ Using @Lazy for simple beans
 *     ✗ Not considering first access performance
 *     ✗ Circular dependencies without @Lazy
 *     ✗ Assuming lazy beans are singletons per access
 * 
 * 11. Testing Lazy Beans:
 *     @Test
 *     void testLazyBean() {
 *         // Bean not created yet
 *         assertNotNull(lazyService); // Proxy exists
 *         
 *         // First access creates bean
 *         lazyService.process();
 *         
 *         // Bean now initialized
 *     }
 * 
 * 12. Performance Considerations:
 *     Startup Time:
 *     - Lazy: Faster startup
 *     - Eager: Slower startup
 *     
 *     Runtime:
 *     - Lazy: First access slower
 *     - Eager: Consistent performance
 *     
 *     Memory:
 *     - Lazy: Lower if bean unused
 *     - Eager: Fixed memory usage
 */
