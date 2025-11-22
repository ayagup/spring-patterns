package com.spring.patterns.lifecycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Init Method Pattern
 * ===================
 * 
 * Demonstrates using the @Bean(initMethod) attribute to specify custom initialization methods.
 * This approach is useful for third-party classes where you can't modify the source code.
 * 
 * KEY FEATURES:
 * =============
 * - Specify init method name in @Bean annotation
 * - Works with third-party classes
 * - XML equivalent: <bean init-method="methodName"/>
 * - Method must be public/protected and no-arg
 * - Called after dependency injection
 * - Can throw exceptions
 * 
 * WHEN TO USE:
 * ============
 * - Third-party library classes
 * - Can't modify source code
 * - Multiple beans from same class with different init methods
 * - Prefer this over modifying library code
 * 
 * vs OTHER INITIALIZATION METHODS:
 * =================================
 * @Bean(initMethod):
 *   - For third-party classes
 *   - Defined in configuration
 *   - Can specify different methods for different beans
 * 
 * @PostConstruct:
 *   - Requires modifying class
 *   - Standard annotation
 *   - Single method per class
 * 
 * InitializingBean:
 *   - Requires implementing interface
 *   - Spring-specific
 *   - Fixed method name
 */

@SpringBootApplication
public class InitMethodPattern {

    public static void main(String[] args) {
        SpringApplication.run(InitMethodPattern.class, args);
        System.out.println("\n=== Init Method Pattern Demo ===\n");
    }
}

@Configuration
class InitMethodConfig {
    
    @Bean(initMethod = "initialize")
    public DataSource dataSource() {
        return new DataSource("jdbc:postgresql://localhost:5432/mydb");
    }
    
    @Bean(initMethod = "connect")
    public EmailClient emailClient() {
        return new EmailClient("smtp.example.com", 587);
    }
    
    @Bean(initMethod = "warmUp")
    public ApplicationCache applicationCache() {
        return new ApplicationCache(1000);
    }
    
    @Bean(initMethod = "loadRules")
    public ValidationEngine validationEngine() {
        return new ValidationEngine();
    }
    
    @Bean(initMethod = "start")
    public BackgroundScheduler backgroundScheduler() {
        return new BackgroundScheduler();
    }
}

/**
 * Example 1: DataSource with initialization
 * (Simulates third-party JDBC DataSource)
 */
class DataSource {
    private final String url;
    private List<Connection> connectionPool;
    private boolean initialized;
    private LocalDateTime initializedAt;
    
    public DataSource(String url) {
        System.out.println("DataSource - Constructor: " + url);
        this.url = url;
    }
    
    // Init method specified in @Bean(initMethod = "initialize")
    public void initialize() {
        System.out.println("DataSource - initialize() method called");
        connectionPool = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            connectionPool.add(new Connection("CONN-" + i));
        }
        initialized = true;
        initializedAt = LocalDateTime.now();
        System.out.println("  Created " + connectionPool.size() + " connections");
    }
    
    public String getStatus() {
        return String.format("URL: %s, Initialized: %s, Pool Size: %d, Initialized At: %s",
            url, initialized, connectionPool != null ? connectionPool.size() : 0, initializedAt);
    }
    
    static class Connection {
        private final String id;
        
        public Connection(String id) {
            this.id = id;
        }
        
        @Override
        public String toString() {
            return "Connection{id='" + id + "'}";
        }
    }
}

/**
 * Example 2: Email Client with connection
 * (Simulates third-party email library)
 */
class EmailClient {
    private final String smtpHost;
    private final int smtpPort;
    private boolean connected;
    private LocalDateTime connectedAt;
    
    public EmailClient(String smtpHost, int smtpPort) {
        System.out.println("\nEmailClient - Constructor: " + smtpHost + ":" + smtpPort);
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
    }
    
    // Init method specified in @Bean(initMethod = "connect")
    public void connect() {
        System.out.println("EmailClient - connect() method called");
        // Simulate connection
        connected = true;
        connectedAt = LocalDateTime.now();
        System.out.println("  Connected to SMTP server");
    }
    
    public String getStatus() {
        return String.format("Host: %s:%d, Connected: %s, Connected At: %s",
            smtpHost, smtpPort, connected, connectedAt);
    }
}

/**
 * Example 3: Application Cache with warm-up
 */
class ApplicationCache {
    private final int maxSize;
    private final java.util.Map<String, Object> cache = new java.util.HashMap<>();
    private boolean warmedUp;
    private LocalDateTime warmedUpAt;
    
    public ApplicationCache(int maxSize) {
        System.out.println("\nApplicationCache - Constructor: maxSize=" + maxSize);
        this.maxSize = maxSize;
    }
    
    // Init method specified in @Bean(initMethod = "warmUp")
    public void warmUp() {
        System.out.println("ApplicationCache - warmUp() method called");
        // Pre-populate cache with frequently accessed data
        cache.put("config:app.name", "Spring Patterns");
        cache.put("config:version", "1.0.0");
        cache.put("config:timeout", 3000);
        cache.put("user:admin", "Admin User");
        cache.put("user:guest", "Guest User");
        
        warmedUp = true;
        warmedUpAt = LocalDateTime.now();
        System.out.println("  Cache warmed up with " + cache.size() + " entries");
    }
    
    public Object get(String key) {
        return cache.get(key);
    }
    
    public String getStatus() {
        return String.format("Max Size: %d, Current Size: %d, Warmed Up: %s, Warmed Up At: %s",
            maxSize, cache.size(), warmedUp, warmedUpAt);
    }
}

/**
 * Example 4: Validation Engine with rule loading
 */
class ValidationEngine {
    private List<ValidationRule> rules;
    private boolean rulesLoaded;
    private LocalDateTime loadedAt;
    
    public ValidationEngine() {
        System.out.println("\nValidationEngine - Constructor");
    }
    
    // Init method specified in @Bean(initMethod = "loadRules")
    public void loadRules() {
        System.out.println("ValidationEngine - loadRules() method called");
        rules = new ArrayList<>();
        
        rules.add(new ValidationRule("email", "^[A-Za-z0-9+_.-]+@(.+)$"));
        rules.add(new ValidationRule("phone", "^\\d{10}$"));
        rules.add(new ValidationRule("zipcode", "^\\d{5}$"));
        rules.add(new ValidationRule("ssn", "^\\d{3}-\\d{2}-\\d{4}$"));
        
        rulesLoaded = true;
        loadedAt = LocalDateTime.now();
        System.out.println("  Loaded " + rules.size() + " validation rules");
    }
    
    public boolean validate(String type, String value) {
        return rules.stream()
            .filter(rule -> rule.type.equals(type))
            .findFirst()
            .map(rule -> value.matches(rule.pattern))
            .orElse(false);
    }
    
    public String getStatus() {
        return String.format("Rules Loaded: %s, Total Rules: %d, Loaded At: %s",
            rulesLoaded, rules != null ? rules.size() : 0, loadedAt);
    }
    
    record ValidationRule(String type, String pattern) {}
}

/**
 * Example 5: Background Scheduler
 */
class BackgroundScheduler {
    private boolean running;
    private LocalDateTime startedAt;
    private int tasksScheduled = 0;
    
    public BackgroundScheduler() {
        System.out.println("\nBackgroundScheduler - Constructor");
    }
    
    // Init method specified in @Bean(initMethod = "start")
    public void start() {
        System.out.println("BackgroundScheduler - start() method called");
        running = true;
        startedAt = LocalDateTime.now();
        
        // Simulate scheduling background tasks
        scheduleTask("Database Cleanup");
        scheduleTask("Cache Refresh");
        scheduleTask("Log Rotation");
        
        System.out.println("  Scheduler started with " + tasksScheduled + " tasks");
    }
    
    private void scheduleTask(String taskName) {
        tasksScheduled++;
        System.out.println("  Scheduled: " + taskName);
    }
    
    public String getStatus() {
        return String.format("Running: %s, Tasks Scheduled: %d, Started At: %s",
            running, tasksScheduled, startedAt);
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/init-method")
class InitMethodController {
    
    private final DataSource dataSource;
    private final EmailClient emailClient;
    private final ApplicationCache applicationCache;
    private final ValidationEngine validationEngine;
    private final BackgroundScheduler backgroundScheduler;
    
    public InitMethodController(
            DataSource dataSource,
            EmailClient emailClient,
            ApplicationCache applicationCache,
            ValidationEngine validationEngine,
            BackgroundScheduler backgroundScheduler) {
        this.dataSource = dataSource;
        this.emailClient = emailClient;
        this.applicationCache = applicationCache;
        this.validationEngine = validationEngine;
        this.backgroundScheduler = backgroundScheduler;
        System.out.println("\nInitMethodController - All beans initialized\n");
    }
    
    @GetMapping("/datasource")
    public String getDataSourceStatus() {
        return dataSource.getStatus();
    }
    
    @GetMapping("/email")
    public String getEmailClientStatus() {
        return emailClient.getStatus();
    }
    
    @GetMapping("/cache")
    public String getCacheStatus() {
        return applicationCache.getStatus();
    }
    
    @GetMapping("/cache/{key}")
    public String getCacheValue(String key) {
        Object value = applicationCache.get(key);
        return value != null ? "Value: " + value : "Not found";
    }
    
    @GetMapping("/validation")
    public String getValidationStatus() {
        return validationEngine.getStatus();
    }
    
    @GetMapping("/validate/{type}/{value}")
    public String validate(String type, String value) {
        boolean valid = validationEngine.validate(type, value);
        return String.format("'%s' as %s: %s", value, type, valid ? "VALID" : "INVALID");
    }
    
    @GetMapping("/scheduler")
    public String getSchedulerStatus() {
        return backgroundScheduler.getStatus();
    }
    
    @GetMapping("/status")
    public String getStatus() {
        return String.format("""
            Init Method Pattern Status:
            
            DataSource: %s
            
            EmailClient: %s
            
            Cache: %s
            
            Validation: %s
            
            Scheduler: %s
            """,
            dataSource.getStatus(),
            emailClient.getStatus(),
            applicationCache.getStatus(),
            validationEngine.getStatus(),
            backgroundScheduler.getStatus()
        );
    }
}

/**
 * TESTING:
 * ========
 * 
 * curl http://localhost:8080/api/init-method/status
 * curl http://localhost:8080/api/init-method/datasource
 * curl http://localhost:8080/api/init-method/email
 * curl http://localhost:8080/api/init-method/cache
 * curl http://localhost:8080/api/init-method/cache/config:app.name
 * curl http://localhost:8080/api/init-method/validation
 * curl http://localhost:8080/api/init-method/validate/email/test@example.com
 * curl http://localhost:8080/api/init-method/validate/phone/1234567890
 * curl http://localhost:8080/api/init-method/scheduler
 * 
 * BEST PRACTICES:
 * ===============
 * 
 * 1. Use for third-party classes you can't modify
 * 2. Method must be public/protected and no-arg
 * 3. Document which method is used for initialization
 * 4. Handle exceptions appropriately
 * 5. Consider using @PostConstruct if you can modify the class
 * 6. Use meaningful method names (initialize, connect, start, etc.)
 * 7. Keep initialization logic simple and fast
 * 
 * ADVANTAGES:
 * ===========
 * - Works with classes you can't modify
 * - Different init methods for different beans
 * - Configuration-based approach
 * - No annotations in business logic
 * 
 * DISADVANTAGES:
 * ==============
 * - Less visible than @PostConstruct
 * - Method name must be known
 * - No compile-time validation
 * - Can be forgotten when bean definition changes
 */
