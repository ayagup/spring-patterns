package com.spring.patterns.resourcemanagement;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Initialization Callback Pattern
 * 
 * Demonstrates Spring's initialization callbacks for performing setup tasks
 * after bean construction and dependency injection. Multiple mechanisms:
 * - InitializingBean interface
 * - @PostConstruct annotation (JSR-250)
 * - Custom init method via @Bean
 * 
 * Key Concepts:
 * - Initialization callbacks run after dependency injection
 * - Multiple callback mechanisms with execution order
 * - Useful for complex initialization logic
 * - Validates bean state before use
 */

@Configuration
@PropertySource("classpath:application.properties")
class InitializationConfig {
    
    @Bean(initMethod = "customInit")
    public DataSource dataSource() {
        return new DataSource();
    }
    
    @Bean
    public CacheService cacheService() {
        return new CacheService();
    }
    
    @Bean
    public ConfigurationManager configurationManager(@Value("${app.name:MyApp}") String appName) {
        return new ConfigurationManager(appName);
    }
}

/**
 * Bean implementing InitializingBean interface
 */
@Component
class CacheService implements InitializingBean {
    
    private boolean initialized = false;
    private int cacheSize = 0;
    private String cacheStatus = "NOT_INITIALIZED";
    
    public CacheService() {
        System.out.println("1. CacheService: Constructor called");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("2. CacheService: InitializingBean.afterPropertiesSet()");
        System.out.println("   Initializing cache...");
        
        // Perform initialization
        this.cacheSize = 1000;
        this.cacheStatus = "INITIALIZING";
        
        // Simulate initialization work
        System.out.println("   Allocating cache memory: " + cacheSize + " entries");
        System.out.println("   Loading default cache entries");
        System.out.println("   Warming up cache");
        
        this.initialized = true;
        this.cacheStatus = "READY";
        
        System.out.println("   Cache initialization complete");
        System.out.println("   Status: " + cacheStatus);
    }
    
    public void put(String key, Object value) {
        if (initialized) {
            System.out.println("  Caching: " + key + " = " + value);
        } else {
            System.out.println("  Cache not initialized!");
        }
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public String getStatus() {
        return cacheStatus;
    }
}

/**
 * Bean with @PostConstruct annotation
 */
class ConfigurationManager {
    
    private String appName;
    private boolean configured = false;
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${database.url:jdbc:mysql://localhost/mydb}")
    private String databaseUrl;
    
    public ConfigurationManager(String appName) {
        System.out.println("1. ConfigurationManager: Constructor - appName=" + appName);
        this.appName = appName;
    }
    
    @PostConstruct
    public void initialize() {
        System.out.println("2. ConfigurationManager: @PostConstruct - initialize()");
        System.out.println("   Validating configuration...");
        
        // Validate injected properties
        if (appName == null || appName.isEmpty()) {
            throw new IllegalStateException("Application name is required");
        }
        
        System.out.println("   Application Name: " + appName);
        System.out.println("   Server Port: " + serverPort);
        System.out.println("   Database URL: " + databaseUrl);
        
        // Perform initialization
        System.out.println("   Loading configuration files");
        System.out.println("   Validating settings");
        System.out.println("   Establishing connections");
        
        this.configured = true;
        System.out.println("   Configuration initialized successfully");
    }
    
    public void displayConfig() {
        if (configured) {
            System.out.println("\n=== Current Configuration ===");
            System.out.println("App: " + appName);
            System.out.println("Port: " + serverPort);
            System.out.println("DB: " + databaseUrl);
            System.out.println("Status: CONFIGURED");
        } else {
            System.out.println("Configuration not initialized!");
        }
    }
    
    public boolean isConfigured() {
        return configured;
    }
}

/**
 * Bean with custom init method
 */
class DataSource {
    
    private boolean connected = false;
    private String connectionUrl = "jdbc:postgresql://localhost:5432/appdb";
    private int maxConnections = 20;
    private int activeConnections = 0;
    
    public DataSource() {
        System.out.println("1. DataSource: Constructor called");
    }
    
    // Custom initialization method (specified in @Bean)
    public void customInit() {
        System.out.println("2. DataSource: customInit() - Custom initialization method");
        System.out.println("   Establishing database connection...");
        
        // Simulate connection establishment
        try {
            System.out.println("   Connecting to: " + connectionUrl);
            System.out.println("   Creating connection pool (max: " + maxConnections + ")");
            
            // Simulate initialization delay
            Thread.sleep(100);
            
            this.connected = true;
            this.activeConnections = 5; // Pre-create some connections
            
            System.out.println("   Connection pool created");
            System.out.println("   Pre-created " + activeConnections + " connections");
            System.out.println("   DataSource ready for use");
            
        } catch (InterruptedException e) {
            System.err.println("   Initialization interrupted: " + e.getMessage());
        }
    }
    
    public void executeQuery(String query) {
        if (connected) {
            System.out.println("  Executing: " + query);
        } else {
            System.out.println("  DataSource not connected!");
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public int getActiveConnections() {
        return activeConnections;
    }
}

/**
 * Bean demonstrating all initialization callbacks
 */
class CompleteInitializationBean implements InitializingBean {
    
    private String name;
    private boolean fullyInitialized = false;
    
    public CompleteInitializationBean() {
        System.out.println("\n=== CompleteInitializationBean Lifecycle ===");
        System.out.println("Phase 1: Constructor called");
    }
    
    public void setName(String name) {
        System.out.println("Phase 2: Setter injection - setName(" + name + ")");
        this.name = name;
    }
    
    @PostConstruct
    public void postConstruct() {
        System.out.println("Phase 3: @PostConstruct - postConstruct()");
        System.out.println("  Properties are injected: name=" + name);
        System.out.println("  Performing early initialization...");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Phase 4: InitializingBean.afterPropertiesSet()");
        System.out.println("  Validating bean state...");
        
        if (name == null) {
            throw new IllegalStateException("Name property is required");
        }
        
        System.out.println("  Bean state is valid");
    }
    
    public void customInit() {
        System.out.println("Phase 5: Custom init method - customInit()");
        System.out.println("  Final initialization steps...");
        this.fullyInitialized = true;
        System.out.println("  Bean is fully initialized and ready");
    }
    
    public void doWork() {
        if (fullyInitialized) {
            System.out.println("Phase 6: Bean operational - doWork()");
            System.out.println("  Working with name: " + name);
        } else {
            System.out.println("  Bean not fully initialized yet!");
        }
    }
}

/**
 * Demonstration of initialization validation
 */
class ValidatedBean implements InitializingBean {
    
    private String requiredProperty;
    private Integer optionalProperty;
    
    public ValidatedBean() {
        System.out.println("ValidatedBean: Constructor");
    }
    
    public void setRequiredProperty(String requiredProperty) {
        this.requiredProperty = requiredProperty;
    }
    
    public void setOptionalProperty(Integer optionalProperty) {
        this.optionalProperty = optionalProperty;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("ValidatedBean: Validating properties...");
        
        // Validate required properties
        if (requiredProperty == null || requiredProperty.isEmpty()) {
            throw new IllegalStateException(
                    "Required property 'requiredProperty' must be set");
        }
        
        // Set defaults for optional properties
        if (optionalProperty == null) {
            System.out.println("  Setting default value for optionalProperty");
            optionalProperty = 100;
        }
        
        System.out.println("  Validation passed");
        System.out.println("  Required: " + requiredProperty);
        System.out.println("  Optional: " + optionalProperty);
    }
    
    public void showValues() {
        System.out.println("ValidatedBean values:");
        System.out.println("  requiredProperty: " + requiredProperty);
        System.out.println("  optionalProperty: " + optionalProperty);
    }
}

public class InitializationCallbackPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Initialization Callback Pattern Demo ===\n");
        
        // Demo 1: InitializingBean interface
        System.out.println("=== Demo 1: InitializingBean Interface ===");
        demonstrateInitializingBean();
        
        // Demo 2: @PostConstruct annotation
        System.out.println("\n=== Demo 2: @PostConstruct Annotation ===");
        demonstratePostConstruct();
        
        // Demo 3: Custom init method
        System.out.println("\n=== Demo 3: Custom Init Method ===");
        demonstrateCustomInit();
        
        // Demo 4: Complete initialization order
        System.out.println("\n=== Demo 4: Complete Initialization Order ===");
        demonstrateInitializationOrder();
        
        // Demo 5: Initialization with validation
        System.out.println("\n=== Demo 5: Initialization with Validation ===");
        demonstrateValidation();
        
        System.out.println("\n=== Demo Completed ===");
    }
    
    private static void demonstrateInitializingBean() {
        System.out.println("Creating ApplicationContext...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(InitializationConfig.class);
        
        System.out.println("\nContext created - InitializingBean.afterPropertiesSet() executed");
        
        CacheService cache = context.getBean(CacheService.class);
        
        System.out.println("\nUsing CacheService:");
        System.out.println("Is initialized: " + cache.isInitialized());
        System.out.println("Status: " + cache.getStatus());
        cache.put("user:1", "John Doe");
        cache.put("user:2", "Jane Smith");
        
        context.close();
    }
    
    private static void demonstratePostConstruct() {
        System.out.println("Creating ApplicationContext...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(InitializationConfig.class);
        
        System.out.println("\nContext created - @PostConstruct executed");
        
        ConfigurationManager configManager = context.getBean(ConfigurationManager.class);
        
        System.out.println("\nUsing ConfigurationManager:");
        System.out.println("Is configured: " + configManager.isConfigured());
        configManager.displayConfig();
        
        context.close();
    }
    
    private static void demonstrateCustomInit() {
        System.out.println("Creating ApplicationContext...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(InitializationConfig.class);
        
        System.out.println("\nContext created - custom init method executed");
        
        DataSource dataSource = context.getBean(DataSource.class);
        
        System.out.println("\nUsing DataSource:");
        System.out.println("Is connected: " + dataSource.isConnected());
        System.out.println("Active connections: " + dataSource.getActiveConnections());
        dataSource.executeQuery("SELECT * FROM users");
        
        context.close();
    }
    
    private static void demonstrateInitializationOrder() {
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext();
        
        // Register bean with all initialization mechanisms
        context.registerBean("completeBean", CompleteInitializationBean.class,
                bd -> {
                    bd.setInitMethodName("customInit");
                    bd.getPropertyValues().add("name", "TestBean");
                });
        
        System.out.println("Refreshing context (triggers initialization)...\n");
        context.refresh();
        
        CompleteInitializationBean bean = context.getBean(CompleteInitializationBean.class);
        bean.doWork();
        
        System.out.println("\n=== Initialization Order ===");
        System.out.println("1. Constructor");
        System.out.println("2. Dependency injection (setters, fields)");
        System.out.println("3. @PostConstruct");
        System.out.println("4. InitializingBean.afterPropertiesSet()");
        System.out.println("5. Custom init method");
        
        context.close();
    }
    
    private static void demonstrateValidation() {
        System.out.println("Test 1: Valid bean with all properties\n");
        
        AnnotationConfigApplicationContext context1 = 
                new AnnotationConfigApplicationContext();
        
        context1.registerBean("validBean", ValidatedBean.class,
                bd -> {
                    bd.getPropertyValues().add("requiredProperty", "Required Value");
                    bd.getPropertyValues().add("optionalProperty", 200);
                });
        
        context1.refresh();
        ValidatedBean validBean = context1.getBean(ValidatedBean.class);
        validBean.showValues();
        context1.close();
        
        System.out.println("\nTest 2: Bean with default optional property\n");
        
        AnnotationConfigApplicationContext context2 = 
                new AnnotationConfigApplicationContext();
        
        context2.registerBean("defaultBean", ValidatedBean.class,
                bd -> bd.getPropertyValues().add("requiredProperty", "Another Value"));
        
        context2.refresh();
        ValidatedBean defaultBean = context2.getBean(ValidatedBean.class);
        defaultBean.showValues();
        context2.close();
        
        System.out.println("\nTest 3: Invalid bean (missing required property)");
        
        try {
            AnnotationConfigApplicationContext context3 = 
                    new AnnotationConfigApplicationContext();
            
            context3.registerBean("invalidBean", ValidatedBean.class);
            
            System.out.println("Refreshing context...");
            context3.refresh(); // This will fail
            
            context3.close();
            
        } catch (Exception e) {
            System.out.println("\nExpected failure: " + e.getCause().getMessage());
            System.out.println("Bean initialization failed due to validation error");
        }
    }
}

/*
 * Key Takeaways:
 * 
 * 1. Multiple initialization callback mechanisms available
 * 2. Callbacks execute after dependency injection completes
 * 3. Execution order: @PostConstruct -> InitializingBean -> custom init
 * 4. Useful for complex setup, validation, and resource allocation
 * 5. Allows validation of injected dependencies
 * 
 * Initialization Mechanisms:
 * - @PostConstruct: JSR-250 annotation, executes first
 * - InitializingBean: Spring interface, executes second
 * - Custom init method: Via @Bean(initMethod="..."), executes third
 * 
 * Initialization Order:
 * 1. Constructor execution
 * 2. Dependency injection (fields, setters, constructor params)
 * 3. BeanPostProcessor.postProcessBeforeInitialization()
 * 4. @PostConstruct annotated methods
 * 5. InitializingBean.afterPropertiesSet()
 * 6. Custom init method (specified in @Bean)
 * 7. BeanPostProcessor.postProcessAfterInitialization()
 * 
 * When to Use Each:
 * - @PostConstruct: Standard, portable (JSR-250), recommended
 * - InitializingBean: When need Spring-specific behavior
 * - Custom init: When need to specify method name externally
 * 
 * Benefits:
 * - Proper resource initialization
 * - Dependency validation
 * - Complex setup logic
 * - Standard annotations (JSR-250)
 * - Multiple callback options
 * 
 * Use Cases:
 * - Database connection initialization
 * - Cache warming
 * - Configuration validation
 * - Resource allocation
 * - Default value setting
 * - Prerequisite checking
 * - System state validation
 * - Complex object setup
 * 
 * Best Practices:
 * - Prefer @PostConstruct for portability
 * - Validate required dependencies
 * - Set default values for optional properties
 * - Keep initialization logic in callbacks, not constructors
 * - Handle initialization exceptions appropriately
 * - Make initialization idempotent when possible
 */
