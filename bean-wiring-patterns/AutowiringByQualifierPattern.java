package com.spring.patterns.wiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autocomplete.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Autowiring by Qualifier Pattern
 * 
 * @Qualifier disambiguates which bean to inject when multiple beans
 * of the same type exist in the application context.
 * 
 * Characteristics:
 * - Resolves ambiguity for multiple beans of same type
 * - Works with @Autowired, @Inject
 * - Can be used on fields, parameters, methods
 * - Can create custom qualifier annotations
 * - More explicit than @Primary
 * 
 * Qualifier Types:
 * 1. String-based Qualifiers - @Qualifier("beanName")
 * 2. Custom Qualifier Annotations - Type-safe
 * 3. Bean Name Qualifiers - Match bean definition name
 * 4. Meta-annotations - Combine qualifiers
 * 
 * Use Cases:
 * - Multiple implementations of same interface
 * - Different configurations of same bean type
 * - Database connection pools (read/write)
 * - Multiple message queues
 * - Different cache managers
 */
@SpringBootApplication
public class AutowiringByQualifierPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AutowiringByQualifierPattern.class, args);
        
        System.out.println("\n=== Autowiring by Qualifier Pattern ===");
        
        // Demonstrate qualifier-based wiring
        PaymentProcessor processor = context.getBean(PaymentProcessor.class);
        System.out.println("\nPayment processor configuration:");
        processor.processPayment(100.00);
        
        // Demonstrate notification service
        NotificationManager manager = context.getBean(NotificationManager.class);
        manager.sendAllNotifications("System update available");
        
        // Demonstrate database service
        DatabaseManager dbManager = context.getBean(DatabaseManager.class);
        dbManager.performOperations();
    }
}

/**
 * Configuration with multiple beans of same type
 */
@Configuration
class QualifierWiringConfig {
    
    // Payment Gateway beans
    @Bean
    @Qualifier("stripe")
    public PaymentGateway stripeGateway() {
        return new StripePaymentGateway();
    }
    
    @Bean
    @Qualifier("paypal")
    public PaymentGateway paypalGateway() {
        return new PayPalPaymentGateway();
    }
    
    @Bean
    @Qualifier("square")
    public PaymentGateway squareGateway() {
        return new SquarePaymentGateway();
    }
    
    // Database Connection beans
    @Bean
    @Primary  // Default for non-qualified injection
    @Qualifier("readDb")
    public DatabaseConnection readDatabase() {
        return new DatabaseConnection("read-db.example.com", 5432);
    }
    
    @Bean
    @Qualifier("writeDb")
    public DatabaseConnection writeDatabase() {
        return new DatabaseConnection("write-db.example.com", 5432);
    }
    
    @Bean
    @Qualifier("analyticsDb")
    public DatabaseConnection analyticsDatabase() {
        return new DatabaseConnection("analytics-db.example.com", 5432);
    }
    
    // Message Queue beans
    @Bean
    @HighPriority  // Custom qualifier annotation
    public MessageQueue highPriorityQueue() {
        return new MessageQueue("high-priority", 1000);
    }
    
    @Bean
    @LowPriority  // Custom qualifier annotation
    public MessageQueue lowPriorityQueue() {
        return new MessageQueue("low-priority", 100);
    }
    
    // Cache Manager beans
    @Bean
    @LocalCache
    public CacheManager localCacheManager() {
        return new CacheManager("local", 10000);
    }
    
    @Bean
    @DistributedCache
    public CacheManager distributedCacheManager() {
        return new CacheManager("distributed", 100000);
    }
}

/**
 * Payment Gateway interface
 */
interface PaymentGateway {
    String processPayment(double amount);
    String getGatewayName();
}

/**
 * Stripe implementation
 */
class StripePaymentGateway implements PaymentGateway {
    
    @Override
    public String processPayment(double amount) {
        return "Stripe processed: $" + amount;
    }
    
    @Override
    public String getGatewayName() {
        return "Stripe";
    }
}

/**
 * PayPal implementation
 */
class PayPalPaymentGateway implements PaymentGateway {
    
    @Override
    public String processPayment(double amount) {
        return "PayPal processed: $" + amount;
    }
    
    @Override
    public String getGatewayName() {
        return "PayPal";
    }
}

/**
 * Square implementation
 */
class SquarePaymentGateway implements PaymentGateway {
    
    @Override
    public String processPayment(double amount) {
        return "Square processed: $" + amount;
    }
    
    @Override
    public String getGatewayName() {
        return "Square";
    }
}

/**
 * Database Connection
 */
class DatabaseConnection {
    private final String host;
    private final int port;
    
    public DatabaseConnection(String host, int port) {
        this.host = host;
        this.port = port;
        System.out.println("DatabaseConnection created: " + host + ":" + port);
    }
    
    public String getConnectionString() {
        return "jdbc:postgresql://" + host + ":" + port;
    }
}

/**
 * Message Queue
 */
class MessageQueue {
    private final String name;
    private final int priority;
    
    public MessageQueue(String name, int priority) {
        this.name = name;
        this.priority = priority;
        System.out.println("MessageQueue created: " + name + " (priority: " + priority + ")");
    }
    
    public void send(String message) {
        System.out.println("[" + name + "] Message sent: " + message);
    }
}

/**
 * Cache Manager
 */
class CacheManager {
    private final String type;
    private final int capacity;
    
    public CacheManager(String type, int capacity) {
        this.type = type;
        this.capacity = capacity;
        System.out.println("CacheManager created: " + type + " (capacity: " + capacity + ")");
    }
    
    public void put(String key, Object value) {
        System.out.println("[" + type + " cache] Put: " + key);
    }
}

/**
 * Example 1: String-based Qualifiers
 */
@Service
class PaymentProcessor {
    
    // Inject specific payment gateway using string qualifier
    private final PaymentGateway primaryGateway;
    private final PaymentGateway fallbackGateway;
    
    @Autowired
    public PaymentProcessor(@Qualifier("stripe") PaymentGateway primaryGateway,
                           @Qualifier("paypal") PaymentGateway fallbackGateway) {
        this.primaryGateway = primaryGateway;
        this.fallbackGateway = fallbackGateway;
        System.out.println("PaymentProcessor created");
        System.out.println("  - Primary: " + primaryGateway.getGatewayName());
        System.out.println("  - Fallback: " + fallbackGateway.getGatewayName());
    }
    
    public String processPayment(double amount) {
        try {
            return primaryGateway.processPayment(amount);
        } catch (Exception e) {
            System.out.println("Primary gateway failed, using fallback");
            return fallbackGateway.processPayment(amount);
        }
    }
}

/**
 * Example 2: Field Injection with Qualifier
 */
@Service
class DatabaseManager {
    
    @Autowired
    @Qualifier("readDb")
    private DatabaseConnection readConnection;
    
    @Autowired
    @Qualifier("writeDb")
    private DatabaseConnection writeConnection;
    
    @Autowired
    @Qualifier("analyticsDb")
    private DatabaseConnection analyticsConnection;
    
    public void performOperations() {
        System.out.println("\nDatabase operations:");
        System.out.println("  - Read from: " + readConnection.getConnectionString());
        System.out.println("  - Write to: " + writeConnection.getConnectionString());
        System.out.println("  - Analytics: " + analyticsConnection.getConnectionString());
    }
}

/**
 * Example 3: Custom Qualifier Annotations (Type-safe)
 */

// High Priority Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@interface HighPriority {
}

// Low Priority Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@interface LowPriority {
}

// Local Cache Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@interface LocalCache {
}

// Distributed Cache Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@interface DistributedCache {
}

/**
 * Service using custom qualifier annotations
 */
@Service
class NotificationManager {
    
    private final MessageQueue urgentQueue;
    private final MessageQueue normalQueue;
    
    @Autowired
    public NotificationManager(@HighPriority MessageQueue urgentQueue,
                              @LowPriority MessageQueue normalQueue) {
        this.urgentQueue = urgentQueue;
        this.normalQueue = normalQueue;
        System.out.println("NotificationManager created with priority queues");
    }
    
    public void sendAllNotifications(String message) {
        System.out.println("\nSending notifications:");
        urgentQueue.send("URGENT: " + message);
        normalQueue.send("INFO: " + message);
    }
}

/**
 * Service using cache qualifiers
 */
@Service
class CachingService {
    
    private final CacheManager localCache;
    private final CacheManager distributedCache;
    
    @Autowired
    public CachingService(@LocalCache CacheManager localCache,
                         @DistributedCache CacheManager distributedCache) {
        this.localCache = localCache;
        this.distributedCache = distributedCache;
        System.out.println("CachingService created with cache managers");
    }
    
    public void cacheData(String key, Object value) {
        System.out.println("\nCaching data:");
        localCache.put(key, value);
        distributedCache.put(key, value);
    }
}

/**
 * Example 4: Setter Injection with Qualifier
 */
@Service
class OrderProcessor {
    
    private PaymentGateway paymentGateway;
    
    @Autowired
    @Qualifier("square")
    public void setPaymentGateway(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
        System.out.println("OrderProcessor configured with: " + paymentGateway.getGatewayName());
    }
    
    public String processOrder(double amount) {
        return paymentGateway.processPayment(amount);
    }
}

/**
 * Example 5: Method Injection with Multiple Qualifiers
 */
@Service
class ReportGenerator {
    
    private DatabaseConnection readDb;
    private DatabaseConnection analyticsDb;
    private CacheManager cache;
    
    @Autowired
    public void setupDependencies(@Qualifier("readDb") DatabaseConnection readDb,
                                 @Qualifier("analyticsDb") DatabaseConnection analyticsDb,
                                 @LocalCache CacheManager cache) {
        this.readDb = readDb;
        this.analyticsDb = analyticsDb;
        this.cache = cache;
        System.out.println("ReportGenerator dependencies configured");
    }
    
    public void generateReport() {
        System.out.println("\nGenerating report:");
        System.out.println("  - Reading from: " + readDb.getConnectionString());
        System.out.println("  - Analytics from: " + analyticsDb.getConnectionString());
        cache.put("report", "data");
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/qualifier-wiring")
class QualifierWiringController {
    
    private final PaymentProcessor paymentProcessor;
    private final NotificationManager notificationManager;
    private final CachingService cachingService;
    
    public QualifierWiringController(PaymentProcessor paymentProcessor,
                                    NotificationManager notificationManager,
                                    CachingService cachingService) {
        this.paymentProcessor = paymentProcessor;
        this.notificationManager = notificationManager;
        this.cachingService = cachingService;
    }
    
    @GetMapping("/payment")
    public String processPayment() {
        return paymentProcessor.processPayment(99.99);
    }
    
    @GetMapping("/notify")
    public String sendNotification() {
        notificationManager.sendAllNotifications("Test notification");
        return "Notifications sent";
    }
    
    @GetMapping("/cache")
    public String cacheData() {
        cachingService.cacheData("test-key", "test-value");
        return "Data cached";
    }
}

/**
 * Key Points:
 * 
 * 1. @Qualifier Usage:
 *    @Autowired
 *    @Qualifier("beanName")
 *    private Service service;
 * 
 * 2. Constructor Parameter Qualifier:
 *    public MyService(@Qualifier("specific") Service service) {
 *        this.service = service;
 *    }
 * 
 * 3. Custom Qualifier Annotations:
 *    @Target({ElementType.FIELD, ElementType.PARAMETER})
 *    @Retention(RetentionPolicy.RUNTIME)
 *    @Qualifier
 *    public @interface MyQualifier { }
 * 
 * 4. Bean Definition with Qualifier:
 *    @Bean
 *    @Qualifier("myQualifier")
 *    public Service service() {
 *        return new ServiceImpl();
 *    }
 * 
 * 5. Qualifier vs @Primary:
 *    @Primary:
 *    - Default choice when no qualifier specified
 *    - Only ONE bean can be @Primary
 *    - Implicit selection
 *    
 *    @Qualifier:
 *    - Explicit bean selection
 *    - Multiple qualifiers possible
 *    - More precise control
 * 
 * 6. Advantages:
 *    ✓ Explicit bean selection
 *    ✓ Type-safe with custom annotations
 *    ✓ Self-documenting code
 *    ✓ Compile-time checking (custom qualifiers)
 * 
 * 7. Best Practices:
 *    ✓ Use custom qualifier annotations for type safety
 *    ✓ Name qualifiers clearly and consistently
 *    ✓ Document qualifier purpose
 *    ✓ Combine with @Primary for common use case
 * 
 * 8. Common Use Cases:
 *    - Multiple databases (read/write replicas)
 *    - Multiple cache implementations
 *    - Different message queues (priority levels)
 *    - Multiple payment gateways
 *    - Various notification channels
 * 
 * 9. Qualifier Resolution Order:
 *    1. @Qualifier match
 *    2. @Primary bean
 *    3. Bean name match
 *    4. Throw NoUniqueBeanDefinitionException
 * 
 * 10. Testing:
 *     @MockBean
 *     @Qualifier("stripe")
 *     private PaymentGateway gateway;
 */
