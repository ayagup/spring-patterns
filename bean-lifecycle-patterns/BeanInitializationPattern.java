package com.spring.patterns.lifecycle;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bean Initialization Pattern
 * ===========================
 * 
 * Demonstrates various techniques for initializing Spring beans including:
 * 1. Constructor-based initialization
 * 2. @PostConstruct annotation
 * 3. InitializingBean interface (afterPropertiesSet)
 * 4. @Bean initMethod attribute
 * 5. Custom initialization logic
 * 6. Dependency injection during initialization
 * 7. Initialization order and lifecycle callbacks
 * 
 * INITIALIZATION ORDER:
 * =====================
 * 1. Constructor
 * 2. Dependency Injection (setters/fields)
 * 3. BeanPostProcessor.postProcessBeforeInitialization
 * 4. @PostConstruct methods
 * 5. InitializingBean.afterPropertiesSet()
 * 6. Custom init-method
 * 7. BeanPostProcessor.postProcessAfterInitialization
 * 
 * USE CASES:
 * ==========
 * - Load configuration/resources on startup
 * - Establish database/network connections
 * - Initialize caches
 * - Validate dependencies
 * - Start background threads
 * - Prepare data structures
 * - Register listeners
 * - Warm up the application
 */

@SpringBootApplication
public class BeanInitializationPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(BeanInitializationPattern.class, args);
        
        System.out.println("\n=== Bean Initialization Pattern Demo ===\n");
        
        // All beans are already initialized at this point
        System.out.println("Application started successfully!");
        System.out.println("Check the logs above to see the initialization order.");
    }
}

/**
 * Configuration for initialization pattern examples
 */
@Configuration
class InitializationConfig {
    
    /**
     * Bean with custom init method
     */
    @Bean(initMethod = "customInit")
    public DatabaseConnection databaseConnection() {
        return new DatabaseConnection();
    }
    
    /**
     * Bean with init method and dependencies
     */
    @Bean(initMethod = "initialize")
    public CacheManager cacheManager(ConfigurationService configService) {
        return new CacheManager(configService);
    }
    
    @Bean
    public ConfigurationService configurationService() {
        return new ConfigurationService();
    }
    
    @Bean
    public ResourceLoader resourceLoader() {
        return new ResourceLoader();
    }
    
    @Bean
    public ConnectionPool connectionPool() {
        return new ConnectionPool();
    }
    
    @Bean
    public DataValidator dataValidator() {
        return new DataValidator();
    }
}

/**
 * Example 1: Constructor-based initialization
 * ============================================
 * Basic initialization in constructor
 */
class DatabaseConnection {
    private String connectionString;
    private boolean connected;
    private LocalDateTime connectedAt;
    
    public DatabaseConnection() {
        System.out.println("1. DatabaseConnection - Constructor called");
        this.connectionString = "jdbc:postgresql://localhost:5432/mydb";
        this.connected = false;
    }
    
    /**
     * Custom init method (called via @Bean initMethod attribute)
     */
    public void customInit() {
        System.out.println("2. DatabaseConnection - customInit() called");
        connect();
    }
    
    private void connect() {
        this.connected = true;
        this.connectedAt = LocalDateTime.now();
        System.out.println("   Database connected at: " + connectedAt);
    }
    
    public String getStatus() {
        return String.format("Connected: %s, Connection String: %s, Connected At: %s",
            connected, connectionString, connectedAt);
    }
}

/**
 * Example 2: Multiple initialization methods
 * ===========================================
 * Demonstrates the order of different initialization callbacks
 */
class CacheManager implements InitializingBean {
    private final ConfigurationService configService;
    private int maxSize;
    private long ttl;
    private boolean initialized;
    private final List<String> initializationSteps = new ArrayList<>();
    
    public CacheManager(ConfigurationService configService) {
        System.out.println("\n3. CacheManager - Constructor called");
        this.configService = configService;
        initializationSteps.add("Constructor");
    }
    
    @PostConstruct
    public void postConstruct() {
        System.out.println("4. CacheManager - @PostConstruct called");
        this.maxSize = 1000;
        initializationSteps.add("@PostConstruct");
    }
    
    @Override
    public void afterPropertiesSet() {
        System.out.println("5. CacheManager - afterPropertiesSet() called (InitializingBean)");
        this.ttl = configService.getCacheTtl();
        initializationSteps.add("InitializingBean.afterPropertiesSet");
    }
    
    public void initialize() {
        System.out.println("6. CacheManager - initialize() called (custom init method)");
        this.initialized = true;
        initializationSteps.add("Custom init method");
        System.out.println("   Initialization complete! Steps: " + initializationSteps);
    }
    
    public String getConfig() {
        return String.format("MaxSize: %d, TTL: %d ms, Initialized: %s, Steps: %s",
            maxSize, ttl, initialized, initializationSteps);
    }
}

/**
 * Example 3: Configuration Service
 * =================================
 * Service initialized before dependent beans
 */
class ConfigurationService {
    private final long cacheTtl = 3600000; // 1 hour
    private final int maxConnections = 100;
    
    public ConfigurationService() {
        System.out.println("\n7. ConfigurationService - Constructor called");
    }
    
    @PostConstruct
    public void loadConfiguration() {
        System.out.println("8. ConfigurationService - @PostConstruct - Loading configuration");
        // Simulate loading config from file/database
    }
    
    public long getCacheTtl() {
        return cacheTtl;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
}

/**
 * Example 4: Resource Loader with validation
 * ===========================================
 * Initializes and validates resources
 */
class ResourceLoader implements InitializingBean {
    private final List<String> loadedResources = new ArrayList<>();
    private boolean validated;
    
    public ResourceLoader() {
        System.out.println("\n9. ResourceLoader - Constructor called");
    }
    
    @PostConstruct
    public void loadResources() {
        System.out.println("10. ResourceLoader - @PostConstruct - Loading resources");
        loadedResources.add("application.properties");
        loadedResources.add("messages.properties");
        loadedResources.add("static/css/styles.css");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("11. ResourceLoader - afterPropertiesSet() - Validating resources");
        validateResources();
    }
    
    private void validateResources() throws Exception {
        if (loadedResources.isEmpty()) {
            throw new Exception("No resources loaded!");
        }
        this.validated = true;
        System.out.println("    Validation successful! Loaded " + loadedResources.size() + " resources");
    }
    
    public List<String> getLoadedResources() {
        return new ArrayList<>(loadedResources);
    }
    
    public boolean isValidated() {
        return validated;
    }
}

/**
 * Example 5: Connection Pool with initialization
 * ===============================================
 * Manages a pool of connections initialized on startup
 */
class ConnectionPool {
    private final List<Connection> connections = new ArrayList<>();
    private final AtomicInteger connectionCounter = new AtomicInteger(0);
    private int poolSize = 10;
    
    public ConnectionPool() {
        System.out.println("\n12. ConnectionPool - Constructor called");
    }
    
    @PostConstruct
    public void initializePool() {
        System.out.println("13. ConnectionPool - @PostConstruct - Initializing pool");
        for (int i = 0; i < poolSize; i++) {
            Connection conn = new Connection(connectionCounter.incrementAndGet());
            connections.add(conn);
        }
        System.out.println("    Created " + poolSize + " connections in the pool");
    }
    
    public int getPoolSize() {
        return connections.size();
    }
    
    public List<Connection> getConnections() {
        return new ArrayList<>(connections);
    }
    
    public static class Connection {
        private final int id;
        private final LocalDateTime createdAt;
        
        public Connection(int id) {
            this.id = id;
            this.createdAt = LocalDateTime.now();
        }
        
        public int getId() {
            return id;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        @Override
        public String toString() {
            return "Connection{id=" + id + ", createdAt=" + createdAt + "}";
        }
    }
}

/**
 * Example 6: Data Validator with complex initialization
 * ======================================================
 * Validates data and initializes validation rules
 */
class DataValidator implements InitializingBean {
    private final List<ValidationRule> rules = new ArrayList<>();
    private boolean rulesLoaded;
    
    public DataValidator() {
        System.out.println("\n14. DataValidator - Constructor called");
    }
    
    @PostConstruct
    public void loadValidationRules() {
        System.out.println("15. DataValidator - @PostConstruct - Loading validation rules");
        // Simulate loading validation rules
        rules.add(new ValidationRule("email", "^[A-Za-z0-9+_.-]+@(.+)$"));
        rules.add(new ValidationRule("phone", "^\\d{10}$"));
        rules.add(new ValidationRule("zipcode", "^\\d{5}(-\\d{4})?$"));
    }
    
    @Override
    public void afterPropertiesSet() {
        System.out.println("16. DataValidator - afterPropertiesSet() - Compiling rules");
        // Simulate compilation of regex patterns
        this.rulesLoaded = true;
        System.out.println("    Compiled " + rules.size() + " validation rules");
    }
    
    public boolean validate(String type, String value) {
        return rules.stream()
            .filter(rule -> rule.getName().equals(type))
            .findFirst()
            .map(rule -> value.matches(rule.getPattern()))
            .orElse(false);
    }
    
    public List<ValidationRule> getRules() {
        return new ArrayList<>(rules);
    }
    
    public boolean isRulesLoaded() {
        return rulesLoaded;
    }
    
    public static class ValidationRule {
        private final String name;
        private final String pattern;
        
        public ValidationRule(String name, String pattern) {
            this.name = name;
            this.pattern = pattern;
        }
        
        public String getName() {
            return name;
        }
        
        public String getPattern() {
            return pattern;
        }
        
        @Override
        public String toString() {
            return "ValidationRule{name='" + name + "', pattern='" + pattern + "'}";
        }
    }
}

/**
 * REST Controller to demonstrate bean initialization
 */
@RestController
@RequestMapping("/api/initialization")
class BeanInitializationController {
    
    private final DatabaseConnection dbConnection;
    private final CacheManager cacheManager;
    private final ConfigurationService configService;
    private final ResourceLoader resourceLoader;
    private final ConnectionPool connectionPool;
    private final DataValidator dataValidator;
    
    public BeanInitializationController(
            DatabaseConnection dbConnection,
            CacheManager cacheManager,
            ConfigurationService configService,
            ResourceLoader resourceLoader,
            ConnectionPool connectionPool,
            DataValidator dataValidator) {
        this.dbConnection = dbConnection;
        this.cacheManager = cacheManager;
        this.configService = configService;
        this.resourceLoader = resourceLoader;
        this.connectionPool = connectionPool;
        this.dataValidator = dataValidator;
        System.out.println("\n17. BeanInitializationController - Constructor called");
        System.out.println("    All dependencies injected successfully!\n");
    }
    
    @GetMapping("/database")
    public String getDatabaseStatus() {
        return dbConnection.getStatus();
    }
    
    @GetMapping("/cache")
    public String getCacheConfig() {
        return cacheManager.getConfig();
    }
    
    @GetMapping("/config")
    public String getConfiguration() {
        return String.format("Cache TTL: %d ms, Max Connections: %d",
            configService.getCacheTtl(),
            configService.getMaxConnections());
    }
    
    @GetMapping("/resources")
    public String getResourcesStatus() {
        return String.format("Loaded Resources: %s, Validated: %s",
            resourceLoader.getLoadedResources(),
            resourceLoader.isValidated());
    }
    
    @GetMapping("/pool")
    public String getPoolStatus() {
        return String.format("Pool Size: %d, Connections: %s",
            connectionPool.getPoolSize(),
            connectionPool.getConnections().size());
    }
    
    @GetMapping("/validator")
    public String getValidatorStatus() {
        return String.format("Rules Loaded: %s, Total Rules: %d, Rules: %s",
            dataValidator.isRulesLoaded(),
            dataValidator.getRules().size(),
            dataValidator.getRules());
    }
    
    @GetMapping("/validate/email")
    public String validateEmail(String email) {
        boolean valid = dataValidator.validate("email", email);
        return String.format("Email '%s' is %s", email, valid ? "VALID" : "INVALID");
    }
    
    @GetMapping("/all")
    public String getAllStatus() {
        return String.format("""
            Bean Initialization Status:
            
            Database: %s
            
            Cache: %s
            
            Config: Cache TTL=%d, Max Connections=%d
            
            Resources: %d loaded, Validated=%s
            
            Connection Pool: %d connections
            
            Validator: %d rules loaded
            """,
            dbConnection.getStatus(),
            cacheManager.getConfig(),
            configService.getCacheTtl(),
            configService.getMaxConnections(),
            resourceLoader.getLoadedResources().size(),
            resourceLoader.isValidated(),
            connectionPool.getPoolSize(),
            dataValidator.getRules().size()
        );
    }
}

/**
 * INITIALIZATION ORDER EXAMPLE:
 * ==============================
 * 
 * Console output will show:
 * 
 * 1. DatabaseConnection - Constructor
 * 2. DatabaseConnection - customInit()
 * 3. CacheManager - Constructor
 * 4. CacheManager - @PostConstruct
 * 5. CacheManager - afterPropertiesSet()
 * 6. CacheManager - initialize()
 * 7. ConfigurationService - Constructor
 * 8. ConfigurationService - @PostConstruct
 * 9. ResourceLoader - Constructor
 * 10. ResourceLoader - @PostConstruct
 * 11. ResourceLoader - afterPropertiesSet()
 * 12. ConnectionPool - Constructor
 * 13. ConnectionPool - @PostConstruct
 * 14. DataValidator - Constructor
 * 15. DataValidator - @PostConstruct
 * 16. DataValidator - afterPropertiesSet()
 * 17. BeanInitializationController - Constructor
 * 
 * TESTING:
 * ========
 * 
 * curl http://localhost:8080/api/initialization/all
 * curl http://localhost:8080/api/initialization/database
 * curl http://localhost:8080/api/initialization/cache
 * curl http://localhost:8080/api/initialization/config
 * curl http://localhost:8080/api/initialization/resources
 * curl http://localhost:8080/api/initialization/pool
 * curl http://localhost:8080/api/initialization/validator
 * curl "http://localhost:8080/api/initialization/validate/email?email=test@example.com"
 * 
 * BEST PRACTICES:
 * ===============
 * 
 * 1. Use @PostConstruct for simple initialization
 *    - Most common and recommended approach
 *    - Standard JSR-250 annotation
 *    - Easy to understand and maintain
 * 
 * 2. Use InitializingBean for framework-aware initialization
 *    - When you need Spring-specific features
 *    - Can throw exceptions during initialization
 *    - Good for validation
 * 
 * 3. Use @Bean(initMethod) for third-party beans
 *    - When you can't modify the class
 *    - Useful for library classes
 *    - XML alternative: init-method attribute
 * 
 * 4. Constructor injection is preferred over field injection
 *    - Makes dependencies explicit
 *    - Easier to test
 *    - Immutable beans
 * 
 * 5. Avoid complex initialization logic
 *    - Keep initialization fast
 *    - Don't block application startup
 *    - Consider lazy initialization for heavy tasks
 * 
 * 6. Handle initialization failures gracefully
 *    - Throw exceptions for critical failures
 *    - Log warnings for non-critical issues
 *    - Provide fallback mechanisms
 * 
 * 7. Order matters for dependent beans
 *    - Use @DependsOn if needed
 *    - Spring handles dependency injection order
 *    - Be careful with circular dependencies
 * 
 * COMMON PITFALLS:
 * ================
 * 
 * 1. Circular dependencies during initialization
 * 2. Long-running initialization blocking startup
 * 3. Exceptions in initialization methods
 * 4. Assuming specific initialization order without @DependsOn
 * 5. Using field injection instead of constructor injection
 * 6. Not handling null dependencies
 * 7. Complex logic in constructors
 * 
 * WHEN TO USE EACH METHOD:
 * ========================
 * 
 * @PostConstruct:
 * - Simple initialization
 * - No exceptions needed
 * - Standard approach
 * 
 * InitializingBean.afterPropertiesSet():
 * - Need to throw checked exceptions
 * - Spring-aware initialization
 * - Validation logic
 * 
 * @Bean(initMethod):
 * - Third-party classes
 * - Can't modify source code
 * - Multiple beans of same type with different init
 * 
 * Constructor:
 * - Simple initialization
 * - Setting final fields
 * - Dependency injection
 */
