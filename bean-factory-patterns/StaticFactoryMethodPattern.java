package com.spring.patterns.factory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Static Factory Method Pattern
 * 
 * Static factory methods are static methods that create and return bean instances.
 * Spring can be configured to call these static methods instead of constructors.
 * 
 * Characteristics:
 * - Uses static factory methods for bean creation
 * - Declared with factory-method attribute in XML or @Bean returning method call
 * - No instance of factory class needed
 * - Often used for singleton pattern
 * - Common in utility and helper classes
 * - Thread-safe creation possible
 * 
 * Use Cases:
 * - Singleton pattern implementation
 * - Complex object creation with validation
 * - Caching instances
 * - Alternative constructors
 * - Named constructors for clarity
 * - Third-party class integration
 * - Enum-like bean creation
 */
@SpringBootApplication
public class StaticFactoryMethodPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(StaticFactoryMethodPattern.class, args);
        
        System.out.println("\n=== Static Factory Method Pattern ===");
        
        // Example 1: Singleton instance
        DatabaseConnectionPool pool = context.getBean(DatabaseConnectionPool.class);
        pool.getConnection();
        
        // Example 2: Configuration with validation
        AppConfiguration config = context.getBean(AppConfiguration.class);
        config.printConfig();
        
        // Example 3: Named constructor pattern
        CustomerService customerService = context.getBean(CustomerService.class);
        customerService.createCustomer("premium");
        
        // Example 4: Cached instances
        CacheManager cacheManager1 = context.getBean("cacheManager", CacheManager.class);
        CacheManager cacheManager2 = context.getBean("cacheManager", CacheManager.class);
        System.out.println("\nCache managers are same instance: " + (cacheManager1 == cacheManager2));
        
        // Example 5: Enum-like beans
        PaymentMethod creditCard = context.getBean("creditCardPayment", PaymentMethod.class);
        creditCard.process(100.0);
        
        PaymentMethod paypal = context.getBean("paypalPayment", PaymentMethod.class);
        paypal.process(50.0);
    }
}

/**
 * Configuration with Static Factory Methods
 */
@Configuration
class StaticFactoryConfig {
    
    /**
     * Example 1: Singleton Pattern
     * Bean created via static factory method
     */
    @Bean
    public DatabaseConnectionPool databaseConnectionPool() {
        System.out.println("Creating DatabaseConnectionPool via static factory");
        return DatabaseConnectionPool.getInstance();
    }
    
    /**
     * Example 2: Configuration with Validation
     * Static factory validates before creation
     */
    @Bean
    public AppConfiguration appConfiguration() {
        System.out.println("Creating AppConfiguration via static factory");
        return AppConfiguration.loadFromEnvironment();
    }
    
    /**
     * Example 3: Named Constructor Pattern
     * Static factory method with clear intent
     */
    @Bean
    public CustomerService customerService(CustomerRepository customerRepository) {
        System.out.println("Creating CustomerService via static factory");
        return CustomerService.createWithDefaults(customerRepository);
    }
    
    /**
     * Example 4: Cached Instance
     * Static factory returns cached instance
     */
    @Bean
    public CacheManager cacheManager() {
        System.out.println("Creating CacheManager via static factory");
        return CacheManager.getOrCreate("default");
    }
    
    /**
     * Example 5: Enum-like Pattern
     * Multiple beans from static factory
     */
    @Bean
    public PaymentMethod creditCardPayment() {
        return PaymentMethod.creditCard();
    }
    
    @Bean
    public PaymentMethod paypalPayment() {
        return PaymentMethod.paypal();
    }
    
    @Bean
    public PaymentMethod bitcoinPayment() {
        return PaymentMethod.bitcoin();
    }
    
    /**
     * Example 6: Builder Pattern Integration
     */
    @Bean
    public EmailTemplate emailTemplate() {
        return EmailTemplate.builder()
            .subject("Welcome")
            .from("noreply@example.com")
            .template("welcome.html")
            .build();
    }
    
    /**
     * Example 7: Resource Creation
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return ThreadPoolExecutor.create(10, 20, 100);
    }
    
    /**
     * Supporting bean
     */
    @Bean
    public CustomerRepository customerRepository() {
        return new CustomerRepository();
    }
}

/**
 * Example 1: Singleton Pattern with Static Factory
 */
class DatabaseConnectionPool {
    private static DatabaseConnectionPool instance;
    private static final Object lock = new Object();
    
    private final int maxConnections;
    private int activeConnections = 0;
    
    // Private constructor
    private DatabaseConnectionPool(int maxConnections) {
        this.maxConnections = maxConnections;
        System.out.println("   DatabaseConnectionPool initialized with " + maxConnections + " max connections");
    }
    
    /**
     * Static factory method - Thread-safe singleton
     */
    public static DatabaseConnectionPool getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DatabaseConnectionPool(50);
                }
            }
        }
        return instance;
    }
    
    public Connection getConnection() {
        if (activeConnections < maxConnections) {
            activeConnections++;
            System.out.println("\n1. Connection acquired (active: " + activeConnections + ")");
            return new Connection("connection-" + activeConnections);
        }
        throw new RuntimeException("Connection pool exhausted");
    }
    
    public void releaseConnection(Connection connection) {
        activeConnections--;
        System.out.println("   Connection released (active: " + activeConnections + ")");
    }
}

class Connection {
    private final String id;
    
    public Connection(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
}

/**
 * Example 2: Configuration with Validation
 */
class AppConfiguration {
    private final String environment;
    private final String apiKey;
    private final int timeout;
    
    private AppConfiguration(String environment, String apiKey, int timeout) {
        this.environment = environment;
        this.apiKey = apiKey;
        this.timeout = timeout;
    }
    
    /**
     * Static factory method with validation
     */
    public static AppConfiguration loadFromEnvironment() {
        String env = System.getProperty("app.env", "development");
        String apiKey = System.getProperty("app.apiKey", "default-key");
        int timeout = Integer.parseInt(System.getProperty("app.timeout", "5000"));
        
        // Validation
        if (apiKey.equals("default-key") && env.equals("production")) {
            throw new IllegalStateException("Cannot use default API key in production");
        }
        
        System.out.println("   Configuration loaded: env=" + env + ", timeout=" + timeout);
        return new AppConfiguration(env, apiKey, timeout);
    }
    
    /**
     * Alternative static factory for testing
     */
    public static AppConfiguration createForTesting() {
        return new AppConfiguration("test", "test-key", 1000);
    }
    
    public void printConfig() {
        System.out.println("\n2. AppConfiguration:");
        System.out.println("   Environment: " + environment);
        System.out.println("   Timeout: " + timeout + "ms");
    }
}

/**
 * Example 3: Named Constructor Pattern
 */
class CustomerService {
    private final CustomerRepository customerRepository;
    private final boolean enableNotifications;
    private final int maxRetries;
    
    private CustomerService(CustomerRepository repository, boolean enableNotifications, int maxRetries) {
        this.customerRepository = repository;
        this.enableNotifications = enableNotifications;
        this.maxRetries = maxRetries;
    }
    
    /**
     * Static factory: Create with defaults
     */
    public static CustomerService createWithDefaults(CustomerRepository repository) {
        System.out.println("   CustomerService created with defaults");
        return new CustomerService(repository, true, 3);
    }
    
    /**
     * Static factory: Create for batch processing
     */
    public static CustomerService createForBatch(CustomerRepository repository) {
        System.out.println("   CustomerService created for batch processing");
        return new CustomerService(repository, false, 1);
    }
    
    /**
     * Static factory: Create for premium tier
     */
    public static CustomerService createPremiumTier(CustomerRepository repository) {
        System.out.println("   CustomerService created for premium tier");
        return new CustomerService(repository, true, 5);
    }
    
    public void createCustomer(String tier) {
        System.out.println("\n3. Creating customer (" + tier + " tier)");
        System.out.println("   Notifications: " + enableNotifications);
        System.out.println("   Max retries: " + maxRetries);
        customerRepository.save(new Customer(UUID.randomUUID().toString(), tier));
    }
}

class CustomerRepository {
    private final Map<String, Customer> customers = new HashMap<>();
    
    public void save(Customer customer) {
        customers.put(customer.getId(), customer);
        System.out.println("   Customer saved: " + customer.getId());
    }
}

class Customer {
    private final String id;
    private final String tier;
    
    public Customer(String id, String tier) {
        this.id = id;
        this.tier = tier;
    }
    
    public String getId() {
        return id;
    }
}

/**
 * Example 4: Cached Instance Pattern
 */
class CacheManager {
    private static final Map<String, CacheManager> instances = new HashMap<>();
    
    private final String name;
    private final Map<String, Object> cache = new HashMap<>();
    
    private CacheManager(String name) {
        this.name = name;
        System.out.println("   CacheManager created: " + name);
    }
    
    /**
     * Static factory: Get or create cached instance
     */
    public static CacheManager getOrCreate(String name) {
        return instances.computeIfAbsent(name, CacheManager::new);
    }
    
    /**
     * Static factory: Create new instance (not cached)
     */
    public static CacheManager createNew(String name) {
        return new CacheManager(name + "-" + System.currentTimeMillis());
    }
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    public Object get(String key) {
        return cache.get(key);
    }
}

/**
 * Example 5: Enum-like Pattern
 */
class PaymentMethod {
    private final String type;
    private final String provider;
    private final double feePercentage;
    
    private PaymentMethod(String type, String provider, double feePercentage) {
        this.type = type;
        this.provider = provider;
        this.feePercentage = feePercentage;
    }
    
    /**
     * Static factory methods for different payment types
     */
    public static PaymentMethod creditCard() {
        System.out.println("   Creating CREDIT_CARD payment method");
        return new PaymentMethod("CREDIT_CARD", "Stripe", 2.9);
    }
    
    public static PaymentMethod paypal() {
        System.out.println("   Creating PAYPAL payment method");
        return new PaymentMethod("PAYPAL", "PayPal", 3.5);
    }
    
    public static PaymentMethod bitcoin() {
        System.out.println("   Creating BITCOIN payment method");
        return new PaymentMethod("BITCOIN", "Coinbase", 1.0);
    }
    
    public static PaymentMethod bankTransfer() {
        return new PaymentMethod("BANK_TRANSFER", "ACH", 0.5);
    }
    
    public void process(double amount) {
        double fee = amount * feePercentage / 100;
        System.out.println("\n5. Processing " + type + " payment:");
        System.out.println("   Amount: $" + amount);
        System.out.println("   Fee: $" + fee);
        System.out.println("   Provider: " + provider);
    }
}

/**
 * Example 6: Builder Pattern with Static Factory
 */
class EmailTemplate {
    private final String subject;
    private final String from;
    private final String template;
    
    private EmailTemplate(Builder builder) {
        this.subject = builder.subject;
        this.from = builder.from;
        this.template = builder.template;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    static class Builder {
        private String subject;
        private String from;
        private String template;
        
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }
        
        public Builder from(String from) {
            this.from = from;
            return this;
        }
        
        public Builder template(String template) {
            this.template = template;
            return this;
        }
        
        public EmailTemplate build() {
            return new EmailTemplate(this);
        }
    }
}

/**
 * Example 7: Resource Creation Pattern
 */
class ThreadPoolExecutor {
    private final int coreSize;
    private final int maxSize;
    private final int queueCapacity;
    
    private ThreadPoolExecutor(int coreSize, int maxSize, int queueCapacity) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.queueCapacity = queueCapacity;
        System.out.println("   ThreadPoolExecutor created: core=" + coreSize + 
                         ", max=" + maxSize + ", queue=" + queueCapacity);
    }
    
    /**
     * Static factory with default configuration
     */
    public static ThreadPoolExecutor createDefault() {
        return new ThreadPoolExecutor(5, 10, 50);
    }
    
    /**
     * Static factory with custom configuration
     */
    public static ThreadPoolExecutor create(int coreSize, int maxSize, int queueCapacity) {
        if (maxSize < coreSize) {
            throw new IllegalArgumentException("Max size must be >= core size");
        }
        return new ThreadPoolExecutor(coreSize, maxSize, queueCapacity);
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/static-factory")
class StaticFactoryController {
    
    private final DatabaseConnectionPool connectionPool;
    private final CustomerService customerService;
    
    public StaticFactoryController(DatabaseConnectionPool connectionPool,
                                  CustomerService customerService) {
        this.connectionPool = connectionPool;
        this.customerService = customerService;
    }
    
    @GetMapping("/connection")
    public String getConnection() {
        Connection conn = connectionPool.getConnection();
        return "Connection acquired: " + conn.getId();
    }
    
    @GetMapping("/customer/{tier}")
    public String createCustomer(@PathVariable String tier) {
        customerService.createCustomer(tier);
        return "Customer created with tier: " + tier;
    }
}

/**
 * Key Points:
 * 
 * 1. Static Factory Method:
 *    public static MyClass getInstance() {
 *        return new MyClass();
 *    }
 * 
 * 2. Spring Configuration:
 *    @Bean
 *    public MyClass myClass() {
 *        return MyClass.getInstance(); // Call static factory
 *    }
 * 
 * 3. Advantages:
 *    ✓ No factory instance needed
 *    ✓ Clear naming (getInstance, create, of, valueOf)
 *    ✓ Can return cached instances
 *    ✓ Can return subtype
 *    ✓ Thread-safe singleton possible
 *    ✓ Validation before creation
 * 
 * 4. Common Static Factory Names:
 *    - getInstance() - Singleton
 *    - create() - New instance
 *    - valueOf() - Type conversion
 *    - of() - Alternative to constructor
 *    - from() - Type conversion
 *    - newInstance() - Guaranteed new
 * 
 * 5. Use Cases:
 *    ✓ Singleton pattern
 *    ✓ Object pooling
 *    ✓ Cached instances
 *    ✓ Named constructors
 *    ✓ Validation logic
 *    ✓ Alternative to complex constructors
 * 
 * 6. Best Practices:
 *    ✓ Use clear, descriptive names
 *    ✓ Document caching behavior
 *    ✓ Make constructors private when appropriate
 *    ✓ Consider thread safety
 *    ✓ Validate parameters
 * 
 * 7. Thread Safety:
 *    - Double-checked locking
 *    - Synchronized methods
 *    - Enum singleton
 *    - Static initializer
 * 
 * 8. Comparison:
 *    Constructor: new MyClass()
 *    Static Factory: MyClass.getInstance()
 */
