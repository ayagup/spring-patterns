package com.spring.patterns.wiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Autowiring by Primary Pattern
 * 
 * @Primary annotation marks a bean as the default choice when multiple beans
 * of the same type exist and no explicit @Qualifier is specified.
 * 
 * Characteristics:
 * - Resolves ambiguity without explicit @Qualifier
 * - Only ONE bean can be @Primary per type
 * - Works with @Bean, @Component, @Service, etc.
 * - Can be overridden with @Qualifier
 * - Provides sensible defaults
 * 
 * Primary vs Qualifier:
 * - @Primary: Implicit default selection
 * - @Qualifier: Explicit bean selection
 * - @Primary + @Qualifier: Flexibility with defaults
 * 
 * Use Cases:
 * - Default implementation of interface
 * - Primary database connection
 * - Default cache implementation
 * - Preferred payment gateway
 * - Main notification service
 */
@SpringBootApplication
public class AutowiringByPrimaryPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AutowiringByPrimaryPattern.class, args);
        
        System.out.println("\n=== Autowiring by Primary Pattern ===");
        
        // Demonstrate primary bean selection
        UserService userService = context.getBean(UserService.class);
        System.out.println("\nUserService uses:");
        userService.createUser("john@example.com", "John Doe");
        
        // Demonstrate service with explicit qualifier
        PaymentService paymentService = context.getBean(PaymentService.class);
        paymentService.processPayment(199.99);
        
        // Demonstrate notification service
        NotificationDispatcher dispatcher = context.getBean(NotificationDispatcher.class);
        dispatcher.sendNotifications();
    }
}

/**
 * Configuration with primary beans
 */
@Configuration
class PrimaryWiringConfig {
    
    // Database connections - PostgreSQL is primary
    @Bean
    @Primary
    public DatabaseClient postgresqlClient() {
        return new PostgreSQLClient();
    }
    
    @Bean
    public DatabaseClient mysqlClient() {
        return new MySQLClient();
    }
    
    @Bean
    public DatabaseClient mongoClient() {
        return new MongoDBClient();
    }
    
    // Payment processors - Stripe is primary
    @Bean
    @Primary
    public PaymentProcessor stripeProcessor() {
        return new StripeProcessor();
    }
    
    @Bean
    public PaymentProcessor paypalProcessor() {
        return new PayPalProcessor();
    }
    
    @Bean
    public PaymentProcessor squareProcessor() {
        return new SquareProcessor();
    }
    
    // Cache implementations - Redis is primary
    @Bean
    @Primary
    public CacheProvider redisCache() {
        return new RedisCacheProvider();
    }
    
    @Bean
    public CacheProvider memcachedProvider() {
        return new MemcachedProvider();
    }
    
    @Bean
    public CacheProvider ehcacheProvider() {
        return new EhCacheProvider();
    }
}

/**
 * Database Client interface
 */
interface DatabaseClient {
    String connect();
    String getType();
}

/**
 * PostgreSQL implementation (Primary)
 */
class PostgreSQLClient implements DatabaseClient {
    
    @Override
    public String connect() {
        return "Connected to PostgreSQL";
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}

/**
 * MySQL implementation
 */
class MySQLClient implements DatabaseClient {
    
    @Override
    public String connect() {
        return "Connected to MySQL";
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}

/**
 * MongoDB implementation
 */
class MongoDBClient implements DatabaseClient {
    
    @Override
    public String connect() {
        return "Connected to MongoDB";
    }
    
    @Override
    public String getType() {
        return "MongoDB";
    }
}

/**
 * Payment Processor interface
 */
interface PaymentProcessor {
    String process(double amount);
    String getName();
}

/**
 * Stripe implementation (Primary)
 */
class StripeProcessor implements PaymentProcessor {
    
    @Override
    public String process(double amount) {
        return "Stripe processed: $" + amount;
    }
    
    @Override
    public String getName() {
        return "Stripe";
    }
}

/**
 * PayPal implementation
 */
class PayPalProcessor implements PaymentProcessor {
    
    @Override
    public String process(double amount) {
        return "PayPal processed: $" + amount;
    }
    
    @Override
    public String getName() {
        return "PayPal";
    }
}

/**
 * Square implementation
 */
class SquareProcessor implements PaymentProcessor {
    
    @Override
    public String process(double amount) {
        return "Square processed: $" + amount;
    }
    
    @Override
    public String getName() {
        return "Square";
    }
}

/**
 * Cache Provider interface
 */
interface CacheProvider {
    void put(String key, Object value);
    Object get(String key);
    String getProviderName();
}

/**
 * Redis implementation (Primary)
 */
class RedisCacheProvider implements CacheProvider {
    
    @Override
    public void put(String key, Object value) {
        System.out.println("[Redis] Cached: " + key);
    }
    
    @Override
    public Object get(String key) {
        return "Redis value for: " + key;
    }
    
    @Override
    public String getProviderName() {
        return "Redis";
    }
}

/**
 * Memcached implementation
 */
class MemcachedProvider implements CacheProvider {
    
    @Override
    public void put(String key, Object value) {
        System.out.println("[Memcached] Cached: " + key);
    }
    
    @Override
    public Object get(String key) {
        return "Memcached value for: " + key;
    }
    
    @Override
    public String getProviderName() {
        return "Memcached";
    }
}

/**
 * EhCache implementation
 */
class EhCacheProvider implements CacheProvider {
    
    @Override
    public void put(String key, Object value) {
        System.out.println("[EhCache] Cached: " + key);
    }
    
    @Override
    public Object get(String key) {
        return "EhCache value for: " + key;
    }
    
    @Override
    public String getProviderName() {
        return "EhCache";
    }
}

/**
 * Example 1: Using Primary bean (no qualifier needed)
 */
@Service
class UserService {
    
    // Injects PRIMARY bean (PostgreSQL) automatically
    private final DatabaseClient databaseClient;
    
    // Injects PRIMARY bean (Redis) automatically
    private final CacheProvider cacheProvider;
    
    @Autowired
    public UserService(DatabaseClient databaseClient, CacheProvider cacheProvider) {
        this.databaseClient = databaseClient;
        this.cacheProvider = cacheProvider;
        System.out.println("\nUserService created with:");
        System.out.println("  - Database: " + databaseClient.getType());
        System.out.println("  - Cache: " + cacheProvider.getProviderName());
    }
    
    public String createUser(String email, String name) {
        String connection = databaseClient.connect();
        System.out.println(connection);
        
        cacheProvider.put("user:" + email, name);
        
        return "User created: " + name;
    }
}

/**
 * Example 2: Overriding Primary with Qualifier
 */
@Service
class PaymentService {
    
    // Uses PRIMARY (Stripe) for primary processor
    private final PaymentProcessor primaryProcessor;
    
    // Explicitly uses PayPal (overrides Primary)
    private final PaymentProcessor fallbackProcessor;
    
    @Autowired
    public PaymentService(PaymentProcessor primaryProcessor,
                         @Qualifier("paypalProcessor") PaymentProcessor fallbackProcessor) {
        this.primaryProcessor = primaryProcessor;
        this.fallbackProcessor = fallbackProcessor;
        System.out.println("\nPaymentService created with:");
        System.out.println("  - Primary: " + primaryProcessor.getName());
        System.out.println("  - Fallback: " + fallbackProcessor.getName());
    }
    
    public String processPayment(double amount) {
        try {
            String result = primaryProcessor.process(amount);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            System.out.println("Primary failed, using fallback");
            return fallbackProcessor.process(amount);
        }
    }
}

/**
 * Example 3: Mixed Primary and Qualifier usage
 */
@Service
class AnalyticsService {
    
    // Uses PRIMARY PostgreSQL
    private final DatabaseClient mainDatabase;
    
    // Explicitly uses MongoDB for analytics
    private final DatabaseClient analyticsDatabase;
    
    // Uses PRIMARY Redis cache
    private final CacheProvider cacheProvider;
    
    @Autowired
    public AnalyticsService(DatabaseClient mainDatabase,
                           @Qualifier("mongoClient") DatabaseClient analyticsDatabase,
                           CacheProvider cacheProvider) {
        this.mainDatabase = mainDatabase;
        this.analyticsDatabase = analyticsDatabase;
        this.cacheProvider = cacheProvider;
        System.out.println("\nAnalyticsService created with:");
        System.out.println("  - Main DB: " + mainDatabase.getType());
        System.out.println("  - Analytics DB: " + analyticsDatabase.getType());
        System.out.println("  - Cache: " + cacheProvider.getProviderName());
    }
    
    public void trackEvent(String event) {
        System.out.println("\nTracking event: " + event);
        System.out.println(mainDatabase.connect());
        System.out.println(analyticsDatabase.connect());
        cacheProvider.put("event:" + event, System.currentTimeMillis());
    }
}

/**
 * Example 4: Primary with Component stereotype
 */
@Component
@Primary
class EmailNotificationService implements NotificationService {
    
    @Override
    public void send(String message) {
        System.out.println("[Email] " + message);
    }
    
    @Override
    public String getChannel() {
        return "Email";
    }
}

@Component
class SmsNotificationService implements NotificationService {
    
    @Override
    public void send(String message) {
        System.out.println("[SMS] " + message);
    }
    
    @Override
    public String getChannel() {
        return "SMS";
    }
}

@Component
class PushNotificationService implements NotificationService {
    
    @Override
    public void send(String message) {
        System.out.println("[Push] " + message);
    }
    
    @Override
    public String getChannel() {
        return "Push";
    }
}

interface NotificationService {
    void send(String message);
    String getChannel();
}

/**
 * Service using primary notification service
 */
@Service
class NotificationDispatcher {
    
    // Injects PRIMARY (EmailNotificationService)
    private final NotificationService primaryNotification;
    
    // Explicitly uses SMS
    private final NotificationService smsNotification;
    
    @Autowired
    public NotificationDispatcher(NotificationService primaryNotification,
                                 @Qualifier("smsNotificationService") NotificationService smsNotification) {
        this.primaryNotification = primaryNotification;
        this.smsNotification = smsNotification;
        System.out.println("\nNotificationDispatcher created with:");
        System.out.println("  - Primary: " + primaryNotification.getChannel());
        System.out.println("  - SMS: " + smsNotification.getChannel());
    }
    
    public void sendNotifications() {
        System.out.println("\nSending notifications:");
        primaryNotification.send("Welcome to our service!");
        smsNotification.send("Verification code: 123456");
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/primary-wiring")
class PrimaryWiringController {
    
    private final UserService userService;
    private final PaymentService paymentService;
    private final AnalyticsService analyticsService;
    
    public PrimaryWiringController(UserService userService,
                                  PaymentService paymentService,
                                  AnalyticsService analyticsService) {
        this.userService = userService;
        this.paymentService = paymentService;
        this.analyticsService = analyticsService;
    }
    
    @GetMapping("/user")
    public String createUser() {
        return userService.createUser("test@example.com", "Test User");
    }
    
    @GetMapping("/payment")
    public String processPayment() {
        return paymentService.processPayment(99.99);
    }
    
    @GetMapping("/analytics")
    public String trackEvent() {
        analyticsService.trackEvent("page_view");
        return "Event tracked";
    }
}

/**
 * Key Points:
 * 
 * 1. @Primary Annotation:
 *    @Bean
 *    @Primary
 *    public Service primaryService() {
 *        return new ServiceImpl();
 *    }
 * 
 * 2. Component with @Primary:
 *    @Component
 *    @Primary
 *    class DefaultService implements Service { }
 * 
 * 3. Injection Resolution Order:
 *    1. @Qualifier specified → Use qualified bean
 *    2. @Primary exists → Use primary bean
 *    3. Bean name matches → Use named bean
 *    4. Fail with NoUniqueBeanDefinitionException
 * 
 * 4. Primary vs Qualifier:
 *    @Primary:
 *    - Implicit default
 *    - No annotation at injection point
 *    - Simplifies common case
 *    - Only one per type
 *    
 *    @Qualifier:
 *    - Explicit selection
 *    - Annotation at injection point
 *    - Overrides @Primary
 *    - Multiple possible
 * 
 * 5. When to Use @Primary:
 *    ✓ Clear default implementation exists
 *    ✓ Most use cases need same implementation
 *    ✓ Reduce @Qualifier verbosity
 *    ✓ Provide sensible defaults
 * 
 * 6. Best Practices:
 *    ✓ Use @Primary for most common implementation
 *    ✓ Document why bean is primary
 *    ✓ Combine with @Qualifier for flexibility
 *    ✓ Only one @Primary per bean type
 *    ✓ Name non-primary beans clearly
 * 
 * 7. Advantages:
 *    ✓ Cleaner code (no @Qualifier everywhere)
 *    ✓ Clear default behavior
 *    ✓ Easy to override when needed
 *    ✓ Reduces boilerplate
 * 
 * 8. Common Use Cases:
 *    - Primary database (PostgreSQL vs MySQL vs MongoDB)
 *    - Default cache (Redis vs Memcached)
 *    - Preferred payment gateway (Stripe vs PayPal)
 *    - Main notification channel (Email vs SMS)
 *    - Default messaging (Kafka vs RabbitMQ)
 * 
 * 9. Multiple @Primary Error:
 *    // ERROR: Cannot have multiple @Primary beans of same type
 *    @Bean @Primary
 *    public Service service1() { }
 *    
 *    @Bean @Primary // ERROR!
 *    public Service service2() { }
 * 
 * 10. Testing:
 *     @TestConfiguration
 *     static class TestConfig {
 *         @Bean
 *         @Primary
 *         public Service mockService() {
 *             return mock(Service.class);
 *         }
 *     }
 */
