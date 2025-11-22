package com.spring.patterns.factory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * FactoryBean Pattern
 * 
 * FactoryBean is a special interface that allows complex bean initialization
 * logic to be encapsulated in a factory that produces the actual bean.
 * 
 * Characteristics:
 * - Implements FactoryBean<T> interface
 * - getObject() returns the actual bean instance
 * - getObjectType() returns the type of object created
 * - isSingleton() determines if bean is singleton
 * - Allows complex initialization logic
 * - Bean name with & prefix returns factory itself
 * 
 * Use Cases:
 * - Complex object construction (database connections, thread pools)
 * - Integration with legacy code
 * - Dynamic bean creation
 * - Proxy generation
 * - Resource initialization (connection pools, caches)
 * - Third-party library integration
 */
@SpringBootApplication
public class FactoryBeanPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(FactoryBeanPattern.class, args);
        
        System.out.println("\n=== FactoryBean Pattern ===");
        
        // Get the actual product (not the factory)
        DatabaseConnection dbConn = context.getBean("databaseConnection", DatabaseConnection.class);
        System.out.println("\n1. DatabaseConnection bean:");
        System.out.println("   Type: " + dbConn.getClass().getSimpleName());
        System.out.println("   Connection: " + dbConn.getConnectionInfo());
        
        // Get the factory itself using & prefix
        Object factory = context.getBean("&databaseConnection");
        System.out.println("\n2. FactoryBean itself (with & prefix):");
        System.out.println("   Type: " + factory.getClass().getSimpleName());
        System.out.println("   Is FactoryBean: " + (factory instanceof FactoryBean));
        
        // Demonstrate thread pool factory
        CustomThreadPool threadPool = context.getBean("threadPool", CustomThreadPool.class);
        System.out.println("\n3. Thread Pool from factory:");
        threadPool.execute(() -> System.out.println("   Task executed in thread pool"));
        
        // Demonstrate service factory
        ComplexService service = context.getBean("complexService", ComplexService.class);
        service.performOperation();
    }
}

/**
 * Configuration
 */
@Configuration
class FactoryBeanConfig {
    
    @Bean
    public DatabaseConnectionFactoryBean databaseConnection() {
        DatabaseConnectionFactoryBean factory = new DatabaseConnectionFactoryBean();
        factory.setUrl("jdbc:postgresql://localhost:5432/mydb");
        factory.setUsername("admin");
        factory.setPassword("secret");
        factory.setMaxConnections(10);
        return factory;
    }
    
    @Bean
    public ThreadPoolFactoryBean threadPool() {
        ThreadPoolFactoryBean factory = new ThreadPoolFactoryBean();
        factory.setCorePoolSize(5);
        factory.setMaxPoolSize(10);
        factory.setQueueCapacity(100);
        return factory;
    }
    
    @Bean
    public ComplexServiceFactoryBean complexService() {
        return new ComplexServiceFactoryBean();
    }
}

/**
 * Database Connection class (the actual bean)
 */
class DatabaseConnection {
    private final String url;
    private final String username;
    private final int maxConnections;
    private Connection connection;
    
    public DatabaseConnection(String url, String username, int maxConnections) {
        this.url = url;
        this.username = username;
        this.maxConnections = maxConnections;
        System.out.println("   DatabaseConnection created with " + maxConnections + " max connections");
    }
    
    public String getConnectionInfo() {
        return "Connected to " + url + " as " + username;
    }
    
    public void connect() throws SQLException {
        // Simulate connection
        System.out.println("   Establishing connection to " + url);
    }
    
    public void disconnect() {
        System.out.println("   Closing connection to " + url);
    }
}

/**
 * Example 1: Database Connection Factory Bean
 * 
 * Creates complex database connection with custom initialization
 */
class DatabaseConnectionFactoryBean implements FactoryBean<DatabaseConnection> {
    
    private String url;
    private String username;
    private String password;
    private int maxConnections;
    
    @Override
    public DatabaseConnection getObject() throws Exception {
        System.out.println("DatabaseConnectionFactoryBean.getObject() called");
        
        // Complex initialization logic
        validateConfiguration();
        DatabaseConnection connection = new DatabaseConnection(url, username, maxConnections);
        connection.connect();
        
        return connection;
    }
    
    @Override
    public Class<?> getObjectType() {
        return DatabaseConnection.class;
    }
    
    @Override
    public boolean isSingleton() {
        return true; // Single database connection
    }
    
    private void validateConfiguration() {
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("Database URL is required");
        }
        System.out.println("   Configuration validated");
    }
    
    // Setters for configuration
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
}

/**
 * Thread Pool class (the actual bean)
 */
class CustomThreadPool {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final int queueCapacity;
    
    public CustomThreadPool(int corePoolSize, int maxPoolSize, int queueCapacity) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.queueCapacity = queueCapacity;
        System.out.println("   CustomThreadPool created: core=" + corePoolSize + 
                         ", max=" + maxPoolSize + ", queue=" + queueCapacity);
    }
    
    public void execute(Runnable task) {
        System.out.println("   Executing task in thread pool...");
        task.run();
    }
    
    public void shutdown() {
        System.out.println("   Shutting down thread pool");
    }
}

/**
 * Example 2: Thread Pool Factory Bean
 * 
 * Creates and configures custom thread pool
 */
class ThreadPoolFactoryBean implements FactoryBean<CustomThreadPool> {
    
    private int corePoolSize = 1;
    private int maxPoolSize = 1;
    private int queueCapacity = 0;
    
    @Override
    public CustomThreadPool getObject() throws Exception {
        System.out.println("ThreadPoolFactoryBean.getObject() called");
        
        // Validate and create thread pool
        validatePoolConfiguration();
        return new CustomThreadPool(corePoolSize, maxPoolSize, queueCapacity);
    }
    
    @Override
    public Class<?> getObjectType() {
        return CustomThreadPool.class;
    }
    
    @Override
    public boolean isSingleton() {
        return true; // Single thread pool instance
    }
    
    private void validatePoolConfiguration() {
        if (maxPoolSize < corePoolSize) {
            throw new IllegalArgumentException("Max pool size must be >= core pool size");
        }
        System.out.println("   Thread pool configuration validated");
    }
    
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
    
    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }
}

/**
 * Complex Service class (the actual bean)
 */
class ComplexService {
    private final String serviceId;
    private final Properties configuration;
    private boolean initialized;
    
    public ComplexService(String serviceId, Properties configuration) {
        this.serviceId = serviceId;
        this.configuration = configuration;
        this.initialized = false;
    }
    
    public void initialize() {
        System.out.println("   Initializing ComplexService: " + serviceId);
        // Complex initialization logic
        this.initialized = true;
    }
    
    public void performOperation() {
        if (!initialized) {
            throw new IllegalStateException("Service not initialized");
        }
        System.out.println("\n4. ComplexService performing operation");
        System.out.println("   Service ID: " + serviceId);
        System.out.println("   Configuration: " + configuration);
    }
    
    public void destroy() {
        System.out.println("   Destroying ComplexService: " + serviceId);
    }
}

/**
 * Example 3: Complex Service Factory Bean
 * 
 * Creates service with complex initialization and configuration
 */
class ComplexServiceFactoryBean implements FactoryBean<ComplexService> {
    
    @Override
    public ComplexService getObject() throws Exception {
        System.out.println("ComplexServiceFactoryBean.getObject() called");
        
        // Complex creation logic
        String serviceId = generateServiceId();
        Properties config = loadConfiguration();
        
        ComplexService service = new ComplexService(serviceId, config);
        service.initialize();
        
        return service;
    }
    
    @Override
    public Class<?> getObjectType() {
        return ComplexService.class;
    }
    
    @Override
    public boolean isSingleton() {
        return true;
    }
    
    private String generateServiceId() {
        return "SERVICE-" + System.currentTimeMillis();
    }
    
    private Properties loadConfiguration() {
        Properties props = new Properties();
        props.setProperty("environment", "production");
        props.setProperty("timeout", "30000");
        props.setProperty("retries", "3");
        return props;
    }
}

/**
 * Example 4: Cache Manager Factory Bean
 */
class CacheManager {
    private final String cacheType;
    private final int maxSize;
    
    public CacheManager(String cacheType, int maxSize) {
        this.cacheType = cacheType;
        this.maxSize = maxSize;
        System.out.println("   CacheManager created: type=" + cacheType + ", maxSize=" + maxSize);
    }
    
    public void put(String key, Object value) {
        System.out.println("   Cache PUT: " + key);
    }
    
    public Object get(String key) {
        return "Cached value for: " + key;
    }
}

@Component
class CacheManagerFactoryBean implements FactoryBean<CacheManager> {
    
    @Override
    public CacheManager getObject() throws Exception {
        // Determine cache type based on environment
        String cacheType = determineCacheType();
        int maxSize = calculateOptimalSize();
        
        return new CacheManager(cacheType, maxSize);
    }
    
    @Override
    public Class<?> getObjectType() {
        return CacheManager.class;
    }
    
    @Override
    public boolean isSingleton() {
        return true;
    }
    
    private String determineCacheType() {
        // Logic to determine cache type
        return "REDIS";
    }
    
    private int calculateOptimalSize() {
        // Calculate based on available memory
        return 10000;
    }
}

/**
 * Example 5: Prototype FactoryBean (non-singleton)
 */
class PrototypeObject {
    private final String id;
    
    public PrototypeObject() {
        this.id = "PROTO-" + System.currentTimeMillis();
        System.out.println("   PrototypeObject created with ID: " + id);
    }
    
    public String getId() {
        return id;
    }
}

@Component
class PrototypeFactoryBean implements FactoryBean<PrototypeObject> {
    
    @Override
    public PrototypeObject getObject() throws Exception {
        // Create new instance every time
        return new PrototypeObject();
    }
    
    @Override
    public Class<?> getObjectType() {
        return PrototypeObject.class;
    }
    
    @Override
    public boolean isSingleton() {
        return false; // Prototype scope - new instance each time
    }
}

/**
 * REST Controller demonstrating FactoryBean usage
 */
@RestController
@RequestMapping("/api/factory-bean")
class FactoryBeanController {
    
    private final DatabaseConnection databaseConnection;
    private final CustomThreadPool threadPool;
    private final ComplexService complexService;
    
    public FactoryBeanController(DatabaseConnection databaseConnection,
                                CustomThreadPool threadPool,
                                ComplexService complexService) {
        this.databaseConnection = databaseConnection;
        this.threadPool = threadPool;
        this.complexService = complexService;
    }
    
    @GetMapping("/db-info")
    public String getDatabaseInfo() {
        return databaseConnection.getConnectionInfo();
    }
    
    @GetMapping("/execute-task")
    public String executeTask() {
        threadPool.execute(() -> System.out.println("Task executed"));
        return "Task submitted to thread pool";
    }
    
    @GetMapping("/service-operation")
    public String performServiceOperation() {
        complexService.performOperation();
        return "Service operation completed";
    }
}

/**
 * Key Points:
 * 
 * 1. FactoryBean Interface:
 *    public interface FactoryBean<T> {
 *        T getObject() throws Exception;
 *        Class<?> getObjectType();
 *        default boolean isSingleton() { return true; }
 *    }
 * 
 * 2. Getting Beans:
 *    // Get product bean (DatabaseConnection)
 *    context.getBean("databaseConnection")
 *    
 *    // Get factory bean itself (DatabaseConnectionFactoryBean)
 *    context.getBean("&databaseConnection")
 * 
 * 3. Singleton vs Prototype:
 *    isSingleton() = true  → Same instance returned
 *    isSingleton() = false → New instance each call
 * 
 * 4. Use Cases:
 *    ✓ Complex object construction
 *    ✓ Database connection pools
 *    ✓ Thread pools
 *    ✓ Cache managers
 *    ✓ Proxy generation
 *    ✓ Third-party library integration
 *    ✓ Dynamic bean creation
 * 
 * 5. Advantages:
 *    ✓ Encapsulates complex creation logic
 *    ✓ Separation of concerns
 *    ✓ Reusable factory logic
 *    ✓ Type-safe
 *    ✓ Spring container managed
 * 
 * 6. When to Use:
 *    - Complex initialization required
 *    - Multiple steps to create bean
 *    - Integration with legacy code
 *    - Need to create proxy or wrapper
 *    - Dynamic bean creation based on config
 * 
 * 7. Common FactoryBean Implementations in Spring:
 *    - ProxyFactoryBean (AOP proxies)
 *    - JndiObjectFactoryBean (JNDI lookups)
 *    - LocalSessionFactoryBean (Hibernate)
 *    - MethodInvokingFactoryBean (Method invocation)
 *    - ServiceLocatorFactoryBean (Service locator)
 * 
 * 8. Lifecycle:
 *    1. Spring creates FactoryBean instance
 *    2. Calls getObject() to get product bean
 *    3. Returns product bean to requesting code
 *    4. Factory itself is also managed by Spring
 * 
 * 9. Best Practices:
 *    ✓ Implement getObjectType() correctly
 *    ✓ Be clear about singleton vs prototype
 *    ✓ Handle exceptions in getObject()
 *    ✓ Validate configuration before creation
 *    ✓ Use for complex object graphs
 * 
 * 10. Testing:
 *     @Test
 *     void testFactoryBean() throws Exception {
 *         DatabaseConnectionFactoryBean factory = 
 *             new DatabaseConnectionFactoryBean();
 *         factory.setUrl("jdbc:test");
 *         factory.setUsername("test");
 *         
 *         DatabaseConnection conn = factory.getObject();
 *         assertNotNull(conn);
 *     }
 */
