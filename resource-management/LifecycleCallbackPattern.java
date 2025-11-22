package com.spring.patterns.resourcemanagement;

import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Lifecycle Callback Pattern
 * 
 * Demonstrates Spring's bean lifecycle callbacks using various mechanisms:
 * - @PostConstruct and @PreDestroy annotations (JSR-250)
 * - Lifecycle interface
 * - SmartLifecycle interface
 * - Custom init and destroy methods
 * 
 * Key Concepts:
 * - Bean lifecycle: instantiation -> initialization -> usage -> destruction
 * - Multiple callback mechanisms available
 * - Callbacks execute in specific order
 * - Useful for resource management and cleanup
 */

@Configuration
class LifecycleConfig {
    
    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public DatabaseConnection databaseConnection() {
        return new DatabaseConnection();
    }
    
    @Bean
    public CacheManager cacheManager() {
        return new CacheManager();
    }
    
    @Bean
    public MessageBroker messageBroker() {
        return new MessageBroker();
    }
    
    @Bean
    public SmartServer smartServer() {
        return new SmartServer();
    }
}

/**
 * Bean with @PostConstruct and @PreDestroy annotations
 */
@Component
class CacheManager {
    
    private boolean initialized = false;
    private boolean running = false;
    
    public CacheManager() {
        System.out.println("1. CacheManager: Constructor called");
    }
    
    @PostConstruct
    public void init() {
        System.out.println("2. CacheManager: @PostConstruct - Initializing cache");
        this.initialized = true;
        this.running = true;
        System.out.println("   Cache initialized and ready");
    }
    
    public void performCaching(String key, Object value) {
        if (running) {
            System.out.println("   Caching: " + key + " = " + value);
        } else {
            System.out.println("   Cache not running!");
        }
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("3. CacheManager: @PreDestroy - Cleaning up cache");
        this.running = false;
        this.initialized = false;
        System.out.println("   Cache cleaned up");
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public boolean isRunning() {
        return running;
    }
}

/**
 * Bean with Lifecycle interface
 */
class MessageBroker implements Lifecycle {
    
    private volatile boolean running = false;
    
    public MessageBroker() {
        System.out.println("1. MessageBroker: Constructor called");
    }
    
    @Override
    public void start() {
        System.out.println("2. MessageBroker: Lifecycle.start() - Starting message broker");
        if (!running) {
            this.running = true;
            System.out.println("   Message broker started");
        } else {
            System.out.println("   Message broker already running");
        }
    }
    
    @Override
    public void stop() {
        System.out.println("3. MessageBroker: Lifecycle.stop() - Stopping message broker");
        if (running) {
            this.running = false;
            System.out.println("   Message broker stopped");
        } else {
            System.out.println("   Message broker already stopped");
        }
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    public void sendMessage(String message) {
        if (running) {
            System.out.println("   Sending message: " + message);
        } else {
            System.out.println("   Cannot send message - broker not running");
        }
    }
}

/**
 * Bean with SmartLifecycle interface (more control over lifecycle)
 */
class SmartServer implements SmartLifecycle {
    
    private volatile boolean running = false;
    private final int phase = 1; // Startup/shutdown order
    
    public SmartServer() {
        System.out.println("1. SmartServer: Constructor called");
    }
    
    @Override
    public void start() {
        System.out.println("2. SmartServer: SmartLifecycle.start() - Starting server");
        if (!running) {
            this.running = true;
            System.out.println("   Server started on port 8080");
            System.out.println("   Accepting connections...");
        }
    }
    
    @Override
    public void stop() {
        System.out.println("3. SmartServer: SmartLifecycle.stop() - Stopping server");
        if (running) {
            this.running = false;
            System.out.println("   Server stopped");
        }
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public boolean isAutoStartup() {
        // Automatically start when context refreshes
        return true;
    }
    
    @Override
    public void stop(Runnable callback) {
        System.out.println("3. SmartServer: SmartLifecycle.stop(callback) - Graceful shutdown");
        stop();
        callback.run();
        System.out.println("   Graceful shutdown completed");
    }
    
    @Override
    public int getPhase() {
        // Controls startup/shutdown order (lower values start first)
        return phase;
    }
    
    public void handleRequest(String request) {
        if (running) {
            System.out.println("   Handling request: " + request);
        } else {
            System.out.println("   Server not running - cannot handle request");
        }
    }
}

/**
 * Bean with custom init and destroy methods
 */
class DatabaseConnection {
    
    private boolean connected = false;
    private String connectionUrl = "jdbc:mysql://localhost:3306/mydb";
    
    public DatabaseConnection() {
        System.out.println("1. DatabaseConnection: Constructor called");
    }
    
    // Custom initialization method (specified in @Bean)
    public void customInit() {
        System.out.println("2. DatabaseConnection: customInit() - Establishing connection");
        this.connected = true;
        System.out.println("   Connected to: " + connectionUrl);
        System.out.println("   Connection pool created");
    }
    
    public void executeQuery(String query) {
        if (connected) {
            System.out.println("   Executing query: " + query);
        } else {
            System.out.println("   Not connected - cannot execute query");
        }
    }
    
    // Custom destroy method (specified in @Bean)
    public void customDestroy() {
        System.out.println("3. DatabaseConnection: customDestroy() - Closing connection");
        if (connected) {
            this.connected = false;
            System.out.println("   Connection closed");
            System.out.println("   Connection pool destroyed");
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
}

/**
 * Bean demonstrating all lifecycle callbacks together
 */
class CompleteLifecycleBean implements Lifecycle {
    
    private volatile boolean running = false;
    
    public CompleteLifecycleBean() {
        System.out.println("\n=== CompleteLifecycleBean Lifecycle ===");
        System.out.println("Phase 1: Constructor called");
    }
    
    @PostConstruct
    public void postConstruct() {
        System.out.println("Phase 2: @PostConstruct - Bean initialization");
    }
    
    public void customInit() {
        System.out.println("Phase 3: Custom init method");
    }
    
    @Override
    public void start() {
        System.out.println("Phase 4: Lifecycle.start() - Bean starting");
        this.running = true;
    }
    
    public void doWork() {
        System.out.println("Phase 5: Bean is operational and doing work");
    }
    
    @Override
    public void stop() {
        System.out.println("Phase 6: Lifecycle.stop() - Bean stopping");
        this.running = false;
    }
    
    @PreDestroy
    public void preDestroy() {
        System.out.println("Phase 7: @PreDestroy - Bean cleanup");
    }
    
    public void customDestroy() {
        System.out.println("Phase 8: Custom destroy method");
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
}

public class LifecycleCallbackPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Lifecycle Callback Pattern Demo ===\n");
        
        // Demo 1: @PostConstruct and @PreDestroy
        System.out.println("=== Demo 1: @PostConstruct and @PreDestroy ===");
        demonstratePostConstructPreDestroy();
        
        // Demo 2: Lifecycle interface
        System.out.println("\n=== Demo 2: Lifecycle Interface ===");
        demonstrateLifecycleInterface();
        
        // Demo 3: SmartLifecycle interface
        System.out.println("\n=== Demo 3: SmartLifecycle Interface ===");
        demonstrateSmartLifecycle();
        
        // Demo 4: Custom init and destroy methods
        System.out.println("\n=== Demo 4: Custom Init and Destroy Methods ===");
        demonstrateCustomMethods();
        
        // Demo 5: Complete lifecycle demonstration
        System.out.println("\n=== Demo 5: Complete Lifecycle Order ===");
        demonstrateCompleteLifecycle();
        
        System.out.println("\n=== Demo Completed ===");
    }
    
    private static void demonstratePostConstructPreDestroy() {
        System.out.println("Creating ApplicationContext...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(LifecycleConfig.class);
        
        System.out.println("\nContext created - beans initialized");
        
        // Use the bean
        CacheManager cacheManager = context.getBean(CacheManager.class);
        System.out.println("\nUsing CacheManager:");
        System.out.println("Is initialized: " + cacheManager.isInitialized());
        System.out.println("Is running: " + cacheManager.isRunning());
        cacheManager.performCaching("user:123", "John Doe");
        
        // Close context (triggers @PreDestroy)
        System.out.println("\nClosing context...\n");
        context.close();
        System.out.println("\nContext closed - cleanup completed");
    }
    
    private static void demonstrateLifecycleInterface() {
        System.out.println("Creating ApplicationContext...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(LifecycleConfig.class);
        
        // Get the lifecycle bean
        MessageBroker broker = context.getBean(MessageBroker.class);
        
        System.out.println("\nManually starting lifecycle bean:");
        context.start(); // Explicitly start lifecycle beans
        
        System.out.println("\nUsing MessageBroker:");
        System.out.println("Is running: " + broker.isRunning());
        broker.sendMessage("Hello, World!");
        
        System.out.println("\nManually stopping lifecycle bean:");
        context.stop(); // Explicitly stop lifecycle beans
        
        System.out.println("\nAttempting to use stopped broker:");
        broker.sendMessage("This should fail");
        
        System.out.println("\nClosing context...\n");
        context.close();
    }
    
    private static void demonstrateSmartLifecycle() {
        System.out.println("Creating ApplicationContext...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(LifecycleConfig.class);
        
        // SmartLifecycle beans start automatically if isAutoStartup() returns true
        System.out.println("\nContext created - SmartLifecycle beans auto-started");
        
        SmartServer server = context.getBean(SmartServer.class);
        
        System.out.println("\nUsing SmartServer:");
        System.out.println("Is running: " + server.isRunning());
        server.handleRequest("GET /api/users");
        
        System.out.println("\nClosing context (triggers graceful shutdown)...\n");
        context.close();
        System.out.println("\nContext closed");
    }
    
    private static void demonstrateCustomMethods() {
        System.out.println("Creating ApplicationContext...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(LifecycleConfig.class);
        
        System.out.println("\nContext created - custom init methods executed");
        
        DatabaseConnection db = context.getBean(DatabaseConnection.class);
        
        System.out.println("\nUsing DatabaseConnection:");
        System.out.println("Is connected: " + db.isConnected());
        db.executeQuery("SELECT * FROM users");
        
        System.out.println("\nClosing context (triggers custom destroy)...\n");
        context.close();
        System.out.println("\nContext closed");
    }
    
    private static void demonstrateCompleteLifecycle() {
        // Create configuration with complete lifecycle bean
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext();
        
        // Register bean with all callbacks
        context.registerBean("completeBean", CompleteLifecycleBean.class,
                bd -> {
                    bd.setInitMethodName("customInit");
                    bd.setDestroyMethodName("customDestroy");
                });
        
        System.out.println("\nRefreshing context (triggers initialization)...");
        context.refresh();
        
        // Start lifecycle
        System.out.println("\nStarting context (triggers lifecycle start)...");
        context.start();
        
        // Use the bean
        CompleteLifecycleBean bean = context.getBean(CompleteLifecycleBean.class);
        bean.doWork();
        
        // Stop lifecycle
        System.out.println("\nStopping context (triggers lifecycle stop)...");
        context.stop();
        
        // Close context (triggers destruction)
        System.out.println("\nClosing context (triggers destruction callbacks)...");
        context.close();
        
        System.out.println("\n=== Lifecycle Complete ===");
    }
}

/*
 * Key Takeaways:
 * 
 * 1. Multiple callback mechanisms for bean lifecycle management
 * 2. Callbacks execute in specific order during initialization and destruction
 * 3. @PostConstruct/@PreDestroy for JSR-250 standard callbacks
 * 4. Lifecycle interface for start/stop control
 * 5. SmartLifecycle for automatic startup and phased shutdown
 * 
 * Initialization Order:
 * 1. Constructor
 * 2. Dependency injection
 * 3. BeanPostProcessor.postProcessBeforeInitialization()
 * 4. @PostConstruct annotated methods
 * 5. InitializingBean.afterPropertiesSet()
 * 6. Custom init-method
 * 7. BeanPostProcessor.postProcessAfterInitialization()
 * 
 * Destruction Order:
 * 1. @PreDestroy annotated methods
 * 2. DisposableBean.destroy()
 * 3. Custom destroy-method
 * 
 * Lifecycle Interfaces:
 * - Lifecycle: Basic start/stop control
 * - SmartLifecycle: Auto-start and phased shutdown
 * - Phased: Control startup/shutdown order
 * 
 * Benefits:
 * - Resource initialization and cleanup
 * - Graceful startup and shutdown
 * - Multiple callback options for different needs
 * - Standard annotations (JSR-250)
 * - Integration with container lifecycle
 * 
 * Use Cases:
 * - Database connection management
 * - Cache initialization and cleanup
 * - Message broker startup/shutdown
 * - Server lifecycle management
 * - Resource pool management
 * - Background task scheduling
 */
