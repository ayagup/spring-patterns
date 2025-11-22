package com.spring.patterns.lifecycle;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @PostConstruct Pattern
 * ======================
 * 
 * The @PostConstruct annotation marks a method to be executed after dependency injection
 * is complete. It's part of JSR-250 and is the most common way to perform initialization.
 * 
 * KEY FEATURES:
 * =============
 * - Standard Java annotation (JSR-250)
 * - Called after all dependencies are injected
 * - Called only once per bean instance
 * - Can access all injected dependencies
 * - Cannot accept parameters
 * - Can throw checked exceptions
 * - Method can have any access modifier
 * - Multiple @PostConstruct methods allowed (not recommended)
 * 
 * EXECUTION ORDER:
 * ================
 * 1. Constructor
 * 2. Dependency Injection (setters/fields)
 * 3. @PostConstruct method(s)
 * 
 * USE CASES:
 * ==========
 * - Initialize configuration
 * - Load data from database
 * - Establish connections
 * - Populate caches
 * - Validate dependencies
 * - Register listeners
 * - Start background processes
 * - Compile patterns/templates
 * 
 * ADVANTAGES:
 * ===========
 * - Standard annotation (portable)
 * - Easy to understand
 * - No Spring dependencies in bean code
 * - Good IDE support
 * - Widely used pattern
 * 
 * vs OTHER INITIALIZATION METHODS:
 * =================================
 * @PostConstruct:
 *   - Standard, portable
 *   - Cannot return value
 *   - Limited exception handling
 * 
 * InitializingBean.afterPropertiesSet():
 *   - Spring-specific
 *   - Better exception handling
 *   - Tighter Spring integration
 * 
 * @Bean(initMethod):
 *   - For third-party classes
 *   - Can't modify source code
 *   - XML equivalent available
 */

@SpringBootApplication
public class PostConstructPattern {

    public static void main(String[] args) {
        SpringApplication.run(PostConstructPattern.class, args);
        System.out.println("\n=== @PostConstruct Pattern Demo ===\n");
        System.out.println("Check the logs above to see @PostConstruct execution order.\n");
    }
}

@Configuration
class PostConstructConfig {
    
    @Bean
    public ApplicationSettings applicationSettings() {
        return new ApplicationSettings();
    }
}

/**
 * Example 1: Basic @PostConstruct usage
 * ======================================
 * Simple initialization after dependency injection
 */
@Service
class UserService {
    private final DatabaseService databaseService;
    private List<User> cachedUsers;
    private LocalDateTime cacheLoadedAt;
    
    public UserService(DatabaseService databaseService) {
        System.out.println("UserService - Constructor");
        this.databaseService = databaseService;
    }
    
    @PostConstruct
    public void init() {
        System.out.println("UserService - @PostConstruct - Loading users into cache");
        loadUsersFromDatabase();
        System.out.println("  Cached " + cachedUsers.size() + " users");
    }
    
    private void loadUsersFromDatabase() {
        cachedUsers = databaseService.fetchAllUsers();
        cacheLoadedAt = LocalDateTime.now();
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(cachedUsers);
    }
    
    public String getCacheInfo() {
        return String.format("Cached Users: %d, Loaded At: %s",
            cachedUsers.size(), cacheLoadedAt);
    }
}

/**
 * Example 2: Configuration loading with @PostConstruct
 * =====================================================
 * Loads and validates configuration on startup
 */
class ApplicationSettings {
    private Map<String, String> settings;
    private boolean validated;
    private LocalDateTime loadedAt;
    
    public ApplicationSettings() {
        System.out.println("\nApplicationSettings - Constructor");
    }
    
    @PostConstruct
    public void loadSettings() {
        System.out.println("ApplicationSettings - @PostConstruct - Loading settings");
        settings = new HashMap<>();
        
        // Simulate loading from properties file
        settings.put("app.name", "Spring Patterns Demo");
        settings.put("app.version", "1.0.0");
        settings.put("app.env", "development");
        settings.put("max.connections", "100");
        settings.put("cache.ttl", "3600");
        
        validateSettings();
        loadedAt = LocalDateTime.now();
        
        System.out.println("  Loaded " + settings.size() + " settings");
    }
    
    private void validateSettings() {
        if (settings.isEmpty()) {
            throw new IllegalStateException("No settings loaded!");
        }
        validated = true;
    }
    
    public String getSetting(String key) {
        return settings.get(key);
    }
    
    public Map<String, String> getAllSettings() {
        return new HashMap<>(settings);
    }
    
    public boolean isValidated() {
        return validated;
    }
    
    public LocalDateTime getLoadedAt() {
        return loadedAt;
    }
}

/**
 * Example 3: Database connection pool initialization
 * ===================================================
 * Creates connection pool in @PostConstruct
 */
@Component
class DatabaseConnectionPool {
    private List<Connection> connections;
    private int poolSize = 10;
    private LocalDateTime initializedAt;
    
    public DatabaseConnectionPool() {
        System.out.println("\nDatabaseConnectionPool - Constructor");
    }
    
    @PostConstruct
    public void initializePool() {
        System.out.println("DatabaseConnectionPool - @PostConstruct - Initializing pool");
        connections = new ArrayList<>();
        
        for (int i = 1; i <= poolSize; i++) {
            connections.add(new Connection("CONN-" + i));
        }
        
        initializedAt = LocalDateTime.now();
        System.out.println("  Created " + poolSize + " database connections");
    }
    
    public Connection getConnection() {
        if (connections.isEmpty()) {
            throw new IllegalStateException("No connections available");
        }
        return connections.get(0);
    }
    
    public int getAvailableConnections() {
        return connections.size();
    }
    
    public LocalDateTime getInitializedAt() {
        return initializedAt;
    }
    
    static class Connection {
        private final String id;
        private final LocalDateTime createdAt;
        
        public Connection(String id) {
            this.id = id;
            this.createdAt = LocalDateTime.now();
        }
        
        public String getId() {
            return id;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        @Override
        public String toString() {
            return String.format("Connection{id='%s', createdAt=%s}", id, createdAt);
        }
    }
}

/**
 * Example 4: Email service with template compilation
 * ===================================================
 * Compiles email templates in @PostConstruct
 */
@Service
class EmailService {
    private final Map<String, EmailTemplate> templates = new HashMap<>();
    private boolean templatesCompiled;
    
    public EmailService() {
        System.out.println("\nEmailService - Constructor");
    }
    
    @PostConstruct
    public void compileTemplates() {
        System.out.println("EmailService - @PostConstruct - Compiling email templates");
        
        templates.put("welcome", new EmailTemplate(
            "Welcome to our service!",
            "Hello {{name}}, welcome to our amazing service!"
        ));
        
        templates.put("reset-password", new EmailTemplate(
            "Password Reset Request",
            "Click here to reset your password: {{resetLink}}"
        ));
        
        templates.put("order-confirmation", new EmailTemplate(
            "Order Confirmation",
            "Thank you for your order #{{orderNumber}}. Total: ${{total}}"
        ));
        
        templatesCompiled = true;
        System.out.println("  Compiled " + templates.size() + " email templates");
    }
    
    public String getTemplate(String name) {
        EmailTemplate template = templates.get(name);
        return template != null ? template.toString() : "Template not found";
    }
    
    public int getTemplateCount() {
        return templates.size();
    }
    
    public boolean areTemplatesCompiled() {
        return templatesCompiled;
    }
    
    record EmailTemplate(String subject, String body) {}
}

/**
 * Example 5: Cache warming service
 * =================================
 * Pre-loads frequently accessed data
 */
@Service
class CacheWarmingService {
    private final ProductService productService;
    private List<Product> popularProducts;
    private LocalDateTime warmedAt;
    
    public CacheWarmingService(ProductService productService) {
        System.out.println("\nCacheWarmingService - Constructor");
        this.productService = productService;
    }
    
    @PostConstruct
    public void warmCache() {
        System.out.println("CacheWarmingService - @PostConstruct - Warming cache");
        popularProducts = productService.getPopularProducts();
        warmedAt = LocalDateTime.now();
        System.out.println("  Cached " + popularProducts.size() + " popular products");
    }
    
    public List<Product> getPopularProducts() {
        return new ArrayList<>(popularProducts);
    }
    
    public LocalDateTime getWarmedAt() {
        return warmedAt;
    }
}

/**
 * Supporting services and models
 */
@Service
class DatabaseService {
    public List<User> fetchAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "john@example.com", "John Doe"));
        users.add(new User(2L, "jane@example.com", "Jane Smith"));
        users.add(new User(3L, "bob@example.com", "Bob Johnson"));
        return users;
    }
}

@Service
class ProductService {
    public List<Product> getPopularProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1L, "Laptop", 999.99));
        products.add(new Product(2L, "Mouse", 29.99));
        products.add(new Product(3L, "Keyboard", 79.99));
        return products;
    }
}

record User(Long id, String email, String name) {}
record Product(Long id, String name, double price) {}

/**
 * REST Controller for testing
 */
@RestController
@RequestMapping("/api/postconstruct")
class PostConstructController {
    
    private final UserService userService;
    private final ApplicationSettings settings;
    private final DatabaseConnectionPool connectionPool;
    private final EmailService emailService;
    private final CacheWarmingService cacheWarming;
    
    public PostConstructController(
            UserService userService,
            ApplicationSettings settings,
            DatabaseConnectionPool connectionPool,
            EmailService emailService,
            CacheWarmingService cacheWarming) {
        this.userService = userService;
        this.settings = settings;
        this.connectionPool = connectionPool;
        this.emailService = emailService;
        this.cacheWarming = cacheWarming;
        System.out.println("\nPostConstructController - Constructor (all beans initialized)\n");
    }
    
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/users/cache-info")
    public String getUserCacheInfo() {
        return userService.getCacheInfo();
    }
    
    @GetMapping("/settings")
    public Map<String, String> getSettings() {
        return settings.getAllSettings();
    }
    
    @GetMapping("/settings/status")
    public String getSettingsStatus() {
        return String.format("Validated: %s, Loaded At: %s",
            settings.isValidated(), settings.getLoadedAt());
    }
    
    @GetMapping("/pool/status")
    public String getPoolStatus() {
        return String.format("Available Connections: %d, Initialized At: %s",
            connectionPool.getAvailableConnections(),
            connectionPool.getInitializedAt());
    }
    
    @GetMapping("/email/templates")
    public String getEmailTemplates() {
        return String.format("Templates Compiled: %s, Count: %d",
            emailService.areTemplatesCompiled(),
            emailService.getTemplateCount());
    }
    
    @GetMapping("/email/template/{name}")
    public String getEmailTemplate(String name) {
        return emailService.getTemplate(name);
    }
    
    @GetMapping("/cache/popular-products")
    public List<Product> getPopularProducts() {
        return cacheWarming.getPopularProducts();
    }
    
    @GetMapping("/cache/warmed-at")
    public String getCacheWarmedAt() {
        return "Cache warmed at: " + cacheWarming.getWarmedAt();
    }
    
    @GetMapping("/status")
    public String getStatus() {
        return String.format("""
            @PostConstruct Pattern Status:
            
            User Service: %s
            Settings: Validated=%s, Count=%d
            Connection Pool: %d connections
            Email Service: %d templates
            Cache Warming: %d products
            """,
            userService.getCacheInfo(),
            settings.isValidated(),
            settings.getAllSettings().size(),
            connectionPool.getAvailableConnections(),
            emailService.getTemplateCount(),
            cacheWarming.getPopularProducts().size()
        );
    }
}

/**
 * TESTING:
 * ========
 * 
 * curl http://localhost:8080/api/postconstruct/status
 * curl http://localhost:8080/api/postconstruct/users
 * curl http://localhost:8080/api/postconstruct/users/cache-info
 * curl http://localhost:8080/api/postconstruct/settings
 * curl http://localhost:8080/api/postconstruct/settings/status
 * curl http://localhost:8080/api/postconstruct/pool/status
 * curl http://localhost:8080/api/postconstruct/email/templates
 * curl http://localhost:8080/api/postconstruct/email/template/welcome
 * curl http://localhost:8080/api/postconstruct/cache/popular-products
 * curl http://localhost:8080/api/postconstruct/cache/warmed-at
 * 
 * BEST PRACTICES:
 * ===============
 * 
 * 1. Keep @PostConstruct methods simple and fast
 *    - Don't block application startup
 *    - Consider async initialization for heavy tasks
 * 
 * 2. Handle exceptions appropriately
 *    - Throw exceptions for critical failures
 *    - Log warnings for non-critical issues
 * 
 * 3. Validate dependencies in @PostConstruct
 *    - Check that injected dependencies are not null
 *    - Validate configuration values
 * 
 * 4. Avoid circular dependencies
 *    - Be careful when beans depend on each other
 *    - Use @Lazy if needed
 * 
 * 5. Use meaningful method names
 *    - init(), initialize(), setup() are common
 *    - Can use specific names like loadCache(), compileTemplates()
 * 
 * 6. Document what the @PostConstruct method does
 *    - Explain the initialization logic
 *    - Note any side effects
 * 
 * 7. Only one @PostConstruct method per class (recommended)
 *    - Multiple methods are allowed but confusing
 *    - Order is not guaranteed for multiple methods
 * 
 * COMMON PITFALLS:
 * ================
 * 
 * 1. Long-running operations in @PostConstruct
 *    - Blocks application startup
 *    - Consider using @Async or scheduled tasks
 * 
 * 2. Assuming specific initialization order
 *    - Use @DependsOn or @Order if order matters
 * 
 * 3. Accessing proxy methods
 *    - Self-invocation doesn't work with proxies
 *    - Use ApplicationContext to get proxy
 * 
 * 4. Throwing unchecked exceptions
 *    - Will prevent bean creation
 *    - Application may fail to start
 * 
 * 5. Not handling null dependencies
 *    - Constructor injection guarantees non-null
 *    - Field injection can be null
 */
