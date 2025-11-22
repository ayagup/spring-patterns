package com.spring.patterns.factory;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BeanNameAware Pattern
 * 
 * BeanNameAware allows beans to know their name in the Spring container.
 * The bean name is injected via setBeanName() callback during initialization.
 * 
 * Characteristics:
 * - Implements BeanNameAware interface
 * - setBeanName() called during initialization
 * - Receives bean name as String
 * - Useful for logging and debugging
 * - Can use name for identification
 * - Early callback in lifecycle
 * 
 * Use Cases:
 * - Logging bean name
 * - Bean identification
 * - Debugging purposes
 * - Self-registration
 * - Name-based behavior
 * - Audit trails
 */
@SpringBootApplication
public class BeanNameAwarePattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(BeanNameAwarePattern.class, args);
        
        System.out.println("\n=== BeanNameAware Pattern ===");
        
        // Example 1: Service with bean name tracking
        AuditService auditService = context.getBean(AuditService.class);
        auditService.logAction("User login");
        
        // Example 2: Multiple instances knowing their names
        Worker worker1 = context.getBean("emailWorker", Worker.class);
        worker1.doWork();
        
        Worker worker2 = context.getBean("smsWorker", Worker.class);
        worker2.doWork();
        
        Worker worker3 = context.getBean("pushWorker", Worker.class);
        worker3.doWork();
        
        // Example 3: Cache with name-based identification
        CacheService cacheService = context.getBean(CacheService.class);
        cacheService.put("key1", "value1");
        cacheService.get("key1");
        
        // Example 4: Task scheduler
        TaskScheduler scheduler = context.getBean("dailyScheduler", TaskScheduler.class);
        scheduler.schedule();
    }
}

/**
 * Configuration
 */
@Configuration
class BeanNameAwareConfig {
    
    @Bean
    public Worker emailWorker() {
        return new Worker("Sending emails");
    }
    
    @Bean
    public Worker smsWorker() {
        return new Worker("Sending SMS");
    }
    
    @Bean
    public Worker pushWorker() {
        return new Worker("Sending push notifications");
    }
    
    @Bean
    public TaskScheduler dailyScheduler() {
        return new TaskScheduler("0 0 0 * * *"); // Daily at midnight
    }
    
    @Bean
    public TaskScheduler hourlyScheduler() {
        return new TaskScheduler("0 0 * * * *"); // Every hour
    }
}

/**
 * Example 1: Audit Service with Bean Name Tracking
 */
@Component
class AuditService implements BeanNameAware {
    
    private String beanName;
    
    @Override
    public void setBeanName(String name) {
        System.out.println("AuditService.setBeanName() called with: " + name);
        this.beanName = name;
    }
    
    public void logAction(String action) {
        System.out.println("\n1. Audit Log:");
        System.out.println("   Bean: " + beanName);
        System.out.println("   Action: " + action);
        System.out.println("   Timestamp: " + java.time.LocalDateTime.now());
    }
    
    public String getBeanName() {
        return beanName;
    }
}

/**
 * Example 2: Worker with Name-based Identification
 */
class Worker implements BeanNameAware {
    
    private String beanName;
    private final String task;
    
    public Worker(String task) {
        this.task = task;
    }
    
    @Override
    public void setBeanName(String name) {
        System.out.println("Worker.setBeanName() called with: " + name);
        this.beanName = name;
    }
    
    public void doWork() {
        System.out.println("\n2. Worker Executing:");
        System.out.println("   Worker Name: " + beanName);
        System.out.println("   Task: " + task);
        System.out.println("   Status: Working...");
    }
    
    public String getWorkerName() {
        return beanName;
    }
}

/**
 * Example 3: Cache Service with Bean Name
 */
@Component
class CacheService implements BeanNameAware {
    
    private String beanName;
    private final java.util.Map<String, Object> cache = new java.util.HashMap<>();
    
    @Override
    public void setBeanName(String name) {
        System.out.println("CacheService.setBeanName() called with: " + name);
        this.beanName = name;
    }
    
    public void put(String key, Object value) {
        System.out.println("\n3. Cache PUT:");
        System.out.println("   Cache: " + beanName);
        System.out.println("   Key: " + key);
        cache.put(key, value);
    }
    
    public Object get(String key) {
        System.out.println("\n3. Cache GET:");
        System.out.println("   Cache: " + beanName);
        System.out.println("   Key: " + key);
        return cache.get(key);
    }
    
    public void clear() {
        System.out.println("Clearing cache: " + beanName);
        cache.clear();
    }
}

/**
 * Example 4: Task Scheduler with Name
 */
class TaskScheduler implements BeanNameAware {
    
    private String beanName;
    private final String cronExpression;
    
    public TaskScheduler(String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
    @Override
    public void setBeanName(String name) {
        System.out.println("TaskScheduler.setBeanName() called with: " + name);
        this.beanName = name;
    }
    
    public void schedule() {
        System.out.println("\n4. Task Scheduled:");
        System.out.println("   Scheduler: " + beanName);
        System.out.println("   Cron: " + cronExpression);
        System.out.println("   Next run: Calculating...");
    }
    
    public String getSchedulerName() {
        return beanName;
    }
}

/**
 * Example 5: Logger with Bean Name
 */
@Component
class BeanLogger implements BeanNameAware {
    
    private String beanName;
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("BeanLogger initialized with name: " + name);
    }
    
    public void log(String level, String message) {
        System.out.println("[" + beanName + "] " + level + ": " + message);
    }
    
    public void info(String message) {
        log("INFO", message);
    }
    
    public void warn(String message) {
        log("WARN", message);
    }
    
    public void error(String message) {
        log("ERROR", message);
    }
}

/**
 * Example 6: Registry Entry
 */
@Component
class RegistryEntry implements BeanNameAware {
    
    private String beanName;
    private String status = "INACTIVE";
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("RegistryEntry created: " + name);
    }
    
    public void register() {
        System.out.println("Registering " + beanName + " in registry");
        status = "ACTIVE";
    }
    
    public void unregister() {
        System.out.println("Unregistering " + beanName + " from registry");
        status = "INACTIVE";
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getName() {
        return beanName;
    }
}

/**
 * Example 7: Performance Monitor
 */
@Component
class PerformanceMonitor implements BeanNameAware {
    
    private String beanName;
    private long startTime;
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        this.startTime = System.currentTimeMillis();
    }
    
    public void measureExecutionTime(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        long end = System.currentTimeMillis();
        
        System.out.println("Performance Monitor: " + beanName);
        System.out.println("  Execution time: " + (end - start) + "ms");
    }
    
    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/bean-name-aware")
class BeanNameAwareController {
    
    private final AuditService auditService;
    private final CacheService cacheService;
    
    public BeanNameAwareController(AuditService auditService, CacheService cacheService) {
        this.auditService = auditService;
        this.cacheService = cacheService;
    }
    
    @GetMapping("/audit/{action}")
    public String logAction(@org.springframework.web.bind.annotation.PathVariable String action) {
        auditService.logAction(action);
        return "Action logged by: " + auditService.getBeanName();
    }
    
    @GetMapping("/cache/{key}/{value}")
    public String cacheValue(@org.springframework.web.bind.annotation.PathVariable String key,
                            @org.springframework.web.bind.annotation.PathVariable String value) {
        cacheService.put(key, value);
        return "Cached in: " + auditService.getBeanName();
    }
}

/**
 * Key Points:
 * 
 * 1. BeanNameAware Interface:
 *    public interface BeanNameAware extends Aware {
 *        void setBeanName(String name);
 *    }
 * 
 * 2. Callback Lifecycle:
 *    1. Bean instantiated
 *    2. Dependencies injected
 *    3. setBeanName() called ← BeanNameAware
 *    4. setBeanFactory() called
 *    5. setApplicationContext() called
 *    6. @PostConstruct methods
 *    7. Bean ready
 * 
 * 3. Bean Name Sources:
 *    - @Component("myName")
 *    - @Bean(name = "myName")
 *    - Class name (default)
 *    - Method name (for @Bean methods)
 * 
 * 4. Use Cases:
 *    ✓ Logging with bean identification
 *    ✓ Self-registration
 *    ✓ Debugging
 *    ✓ Audit trails
 *    ✓ Metrics collection
 *    ✓ Name-based behavior
 * 
 * 5. Advantages:
 *    ✓ Simple interface
 *    ✓ Early lifecycle callback
 *    ✓ No Spring coupling (just string)
 *    ✓ Useful for debugging
 *    ✓ Enables self-awareness
 * 
 * 6. Best Practices:
 *    ✓ Use for logging/debugging
 *    ✓ Don't use for business logic
 *    ✓ Store name in field
 *    ✓ Include in toString()
 *    ✓ Use for identification only
 * 
 * 7. Common Patterns:
 *    - Logging: [beanName] message
 *    - Registry: Register bean by name
 *    - Metrics: Track bean performance
 *    - Audit: Bean activity logging
 * 
 * 8. Testing:
 *    @Test
 *    void testBeanNameAware() {
 *        AuditService service = new AuditService();
 *        service.setBeanName("testAuditService");
 *        
 *        assertEquals("testAuditService", service.getBeanName());
 *    }
 * 
 * 9. When NOT to Use:
 *    ✗ Business logic based on name
 *    ✗ Critical functionality
 *    ✗ Type resolution
 *    ✗ Configuration
 * 
 * 10. Alternatives:
 *     - @Value("#{beanName}") for specific cases
 *     - Explicit constructor parameters
 *     - Configuration properties
 */
