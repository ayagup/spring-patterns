package com.example.miscellaneous.destructionawarebeanpostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Destruction Aware Bean Post Processor Pattern
 * 
 * This pattern shows how to:
 * 1. Implement DestructionAwareBeanPostProcessor interface
 * 2. Execute cleanup logic before bean destruction
 * 3. Release resources properly
 * 4. Track bean lifecycle
 * 5. Close connections
 * 6. Clean up temporary files
 * 7. Flush buffers
 * 8. Log destruction events
 * 9. Validate cleanup
 * 10. Handle destruction errors
 * 
 * Key Concepts:
 * - DestructionAwareBeanPostProcessor: Hook into bean destruction
 * - postProcessBeforeDestruction: Called before bean is destroyed
 * - requiresDestruction: Check if bean requires cleanup
 * - Resource Cleanup: Proper resource management
 * - Graceful Shutdown: Clean application shutdown
 * 
 * Destruction Order:
 * 1. Context shutdown initiated
 * 2. DestructionAwareBeanPostProcessor.postProcessBeforeDestruction
 * 3. @PreDestroy methods
 * 4. DisposableBean.destroy()
 * 5. custom destroy-method
 * 6. Bean destroyed
 * 
 * Common Use Cases:
 * - Database connection cleanup
 * - File handle closing
 * - Thread pool shutdown
 * - Cache clearing
 * - Audit logging
 * 
 * Dependencies:
 * - spring-context
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class DestructionAwareBeanPostProcessorPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(DestructionAwareBeanPostProcessorPattern.class, args);
        demonstrateDestructionCallbacks(context);
        
        // Trigger shutdown to see destruction callbacks
        System.out.println("\n=== Triggering Application Shutdown ===");
        context.close();
    }
    
    /**
     * Demonstrates destruction-aware functionality
     */
    private static void demonstrateDestructionCallbacks(
            org.springframework.context.ApplicationContext context) {
        System.out.println("=== Destruction Aware Bean Post Processor Pattern ===\n");
        
        // Demo 1: Use resource-managing service
        ResourceManagedService resourceService = context.getBean(ResourceManagedService.class);
        System.out.println("1. Resource Managed Service:");
        resourceService.useResource();
        System.out.println();
        
        // Demo 2: Use connection pool
        ConnectionPoolService poolService = context.getBean(ConnectionPoolService.class);
        System.out.println("2. Connection Pool Service:");
        poolService.getConnection();
        System.out.println();
        
        // Demo 3: Check destruction tracker
        DestructionTracker tracker = context.getBean(DestructionTracker.class);
        System.out.println("3. Destruction Tracker:");
        System.out.println("   Tracked beans: " + tracker.getTrackedBeans().size());
        System.out.println();
    }
}

// ============================================================================
// Destruction Aware Bean Post Processors
// ============================================================================

/**
 * Cleanup BeanPostProcessor - handles resource cleanup
 */
@Component
class CleanupBeanPostProcessor implements DestructionAwareBeanPostProcessor {
    
    private final DestructionTracker tracker;
    
    public CleanupBeanPostProcessor(DestructionTracker tracker) {
        this.tracker = tracker;
    }
    
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        System.out.println("   [CleanupBPP] Cleaning up bean: " + beanName);
        tracker.recordDestruction(beanName);
        
        // Perform cleanup based on bean type
        if (bean instanceof ResourceHolder) {
            ((ResourceHolder) bean).cleanup();
        }
    }
    
    @Override
    public boolean requiresDestruction(Object bean) {
        // Only process beans that need cleanup
        return bean instanceof ResourceHolder || 
               bean.getClass().isAnnotationPresent(Service.class);
    }
}

/**
 * Logging Destruction BeanPostProcessor - logs all destructions
 */
@Component
class LoggingDestructionBeanPostProcessor implements DestructionAwareBeanPostProcessor {
    
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        System.out.println("   [LoggingDestruction] Destroying bean: " + beanName + 
                         " (" + bean.getClass().getSimpleName() + ")");
    }
    
    @Override
    public boolean requiresDestruction(Object bean) {
        return true; // Log all bean destructions
    }
}

/**
 * Resource Release BeanPostProcessor - releases various resources
 */
@Component
class ResourceReleaseBeanPostProcessor implements DestructionAwareBeanPostProcessor {
    
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (bean instanceof AutoCloseable) {
            try {
                System.out.println("   [ResourceRelease] Closing AutoCloseable: " + beanName);
                ((AutoCloseable) bean).close();
            } catch (Exception e) {
                System.err.println("   [ResourceRelease] Error closing " + beanName + ": " + 
                                 e.getMessage());
            }
        }
    }
    
    @Override
    public boolean requiresDestruction(Object bean) {
        return bean instanceof AutoCloseable;
    }
}

// ============================================================================
// Resource Holder Interface
// ============================================================================

/**
 * Interface for beans that hold resources
 */
interface ResourceHolder {
    void cleanup();
}

// ============================================================================
// Destruction Tracker
// ============================================================================

/**
 * Tracks bean destruction events
 */
@Component
class DestructionTracker {
    
    private final Map<String, LocalDateTime> destructionTimes = new ConcurrentHashMap<>();
    private final Set<String> trackedBeans = ConcurrentHashMap.newKeySet();
    
    public void recordDestruction(String beanName) {
        destructionTimes.put(beanName, LocalDateTime.now());
        trackedBeans.add(beanName);
    }
    
    public Set<String> getTrackedBeans() {
        return new HashSet<>(trackedBeans);
    }
    
    public LocalDateTime getDestructionTime(String beanName) {
        return destructionTimes.get(beanName);
    }
    
    public Map<String, LocalDateTime> getAllDestructionTimes() {
        return new HashMap<>(destructionTimes);
    }
}

// ============================================================================
// Services with Cleanup Logic
// ============================================================================

/**
 * Service that manages resources
 */
@Service
class ResourceManagedService implements ResourceHolder, DisposableBean {
    
    private final List<String> resources = new ArrayList<>();
    private boolean active = true;
    
    public ResourceManagedService() {
        System.out.println("   [ResourceManagedService] Initializing resources");
        resources.add("Resource-1");
        resources.add("Resource-2");
    }
    
    public void useResource() {
        if (active) {
            System.out.println("   Using resources: " + resources);
        }
    }
    
    @Override
    public void cleanup() {
        System.out.println("   [ResourceManagedService] Cleaning up " + resources.size() + 
                         " resources");
        resources.clear();
    }
    
    @PreDestroy
    public void preDestroy() {
        System.out.println("   [ResourceManagedService] @PreDestroy called");
        active = false;
    }
    
    @Override
    public void destroy() {
        System.out.println("   [ResourceManagedService] DisposableBean.destroy() called");
    }
}

/**
 * Connection pool service
 */
@Service
class ConnectionPoolService implements ResourceHolder {
    
    private final Map<String, Connection> connectionPool = new ConcurrentHashMap<>();
    private int connectionCounter = 0;
    
    public ConnectionPoolService() {
        System.out.println("   [ConnectionPoolService] Initializing connection pool");
        initializePool();
    }
    
    private void initializePool() {
        for (int i = 0; i < 5; i++) {
            String id = "CONN-" + (++connectionCounter);
            connectionPool.put(id, new Connection(id));
        }
    }
    
    public Connection getConnection() {
        Connection conn = connectionPool.values().stream()
            .filter(c -> !c.isInUse())
            .findFirst()
            .orElse(null);
            
        if (conn != null) {
            conn.setInUse(true);
            System.out.println("   Retrieved connection: " + conn.getId());
        }
        
        return conn;
    }
    
    @Override
    public void cleanup() {
        System.out.println("   [ConnectionPoolService] Closing " + connectionPool.size() + 
                         " connections");
        connectionPool.values().forEach(Connection::close);
        connectionPool.clear();
    }
    
    @PreDestroy
    public void shutdown() {
        System.out.println("   [ConnectionPoolService] Shutting down connection pool");
    }
}

/**
 * File handler service
 */
@Service
class FileHandlerService implements ResourceHolder {
    
    private final Set<String> openFiles = ConcurrentHashMap.newKeySet();
    
    public void openFile(String filename) {
        openFiles.add(filename);
        System.out.println("   Opened file: " + filename);
    }
    
    public void closeFile(String filename) {
        if (openFiles.remove(filename)) {
            System.out.println("   Closed file: " + filename);
        }
    }
    
    @Override
    public void cleanup() {
        System.out.println("   [FileHandlerService] Closing " + openFiles.size() + " open files");
        openFiles.clear();
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * Connection model
 */
class Connection {
    private final String id;
    private boolean inUse;
    private boolean closed;
    
    public Connection(String id) {
        this.id = id;
        this.inUse = false;
        this.closed = false;
    }
    
    public void close() {
        if (!closed) {
            System.out.println("   [Connection] Closing connection: " + id);
            closed = true;
            inUse = false;
        }
    }
    
    // Getters and setters
    public String getId() { return id; }
    public boolean isInUse() { return inUse; }
    public void setInUse(boolean inUse) { this.inUse = inUse; }
    public boolean isClosed() { return closed; }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating destruction tracking
 */
@RestController
@RequestMapping("/api/destruction-aware")
class DestructionAwareController {
    
    private final DestructionTracker tracker;
    private final ResourceManagedService resourceService;
    private final ConnectionPoolService poolService;
    
    public DestructionAwareController(DestructionTracker tracker,
                                     ResourceManagedService resourceService,
                                     ConnectionPoolService poolService) {
        this.tracker = tracker;
        this.resourceService = resourceService;
        this.poolService = poolService;
    }
    
    /**
     * Get destruction statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("trackedBeans", tracker.getTrackedBeans());
        stats.put("destructionTimes", tracker.getAllDestructionTimes());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Use resource service
     */
    @PostMapping("/use-resource")
    public ResponseEntity<Map<String, String>> useResource() {
        resourceService.useResource();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Resource used");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get connection from pool
     */
    @GetMapping("/connection")
    public ResponseEntity<Map<String, String>> getConnection() {
        Connection conn = poolService.getConnection();
        
        Map<String, String> response = new HashMap<>();
        if (conn != null) {
            response.put("status", "success");
            response.put("connectionId", conn.getId());
        } else {
            response.put("status", "error");
            response.put("message", "No connections available");
        }
        
        return ResponseEntity.ok(response);
    }
}
