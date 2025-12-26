package com.example.contexthierarchy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Context Close Pattern
 * 
 * Demonstrates properly closing and cleaning up application context,
 * including destruction callbacks, resource release, and shutdown hooks.
 * 
 * Key Concepts:
 * - Context shutdown
 * - Destruction callbacks
 * - Resource cleanup
 * - Shutdown hooks
 * - Graceful shutdown
 * 
 * Use Cases:
 * - Application shutdown
 * - Resource cleanup
 * - Connection closing
 * - Thread pool termination
 * - Graceful degradation
 */
@SpringBootApplication
public class ContextClosePattern {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = 
            SpringApplication.run(ContextClosePattern.class, args);
        
        // Register shutdown hook
        context.registerShutdownHook();
    }
}

/**
 * Bean demonstrating cleanup on context close
 */
@Service
class CleanupBean {
    
    private static final List<String> cleanupEvents = new ArrayList<>();
    private boolean resourcesAcquired = true;

    @PreDestroy
    public void cleanup() {
        String event = "CleanupBean: Releasing resources";
        cleanupEvents.add(event);
        System.out.println(event);
        
        resourcesAcquired = false;
    }

    public boolean isResourcesAcquired() {
        return resourcesAcquired;
    }

    public static List<String> getCleanupEvents() {
        return new ArrayList<>(cleanupEvents);
    }
}

/**
 * Bean listening to context close event
 */
@Service
class ContextCloseListener {
    
    private static final List<String> closeEvents = new ArrayList<>();

    @EventListener
    public void onContextClose(ContextClosedEvent event) {
        String eventMsg = "Context close event received at " + System.currentTimeMillis();
        closeEvents.add(eventMsg);
        System.out.println(eventMsg);
        
        // Perform cleanup
        performCleanup();
    }

    private void performCleanup() {
        System.out.println("Performing final cleanup tasks...");
        // Close connections, flush caches, etc.
    }

    public static List<String> getCloseEvents() {
        return new ArrayList<>(closeEvents);
    }
}

/**
 * Service managing context lifecycle
 */
@Service
class ContextCloseService implements org.springframework.beans.factory.DisposableBean {

    private final ConfigurableApplicationContext applicationContext;
    private static final List<String> lifecycleEvents = new ArrayList<>();

    public ContextCloseService(ApplicationContext applicationContext) {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * Close context gracefully
     */
    public Map<String, String> closeContext() {
        try {
            lifecycleEvents.add("Initiating context close");
            applicationContext.close();
            
            return Map.of(
                    "status", "success",
                    "message", "Context closed successfully"
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * Get context status
     */
    public Map<String, Object> getContextStatus() {
        return Map.of(
                "active", applicationContext.isActive(),
                "running", applicationContext.isRunning()
        );
    }

    @Override
    public void destroy() {
        lifecycleEvents.add("DisposableBean.destroy() called");
        System.out.println("ContextCloseService: destroy() called");
    }

    public static List<String> getLifecycleEvents() {
        return new ArrayList<>(lifecycleEvents);
    }
}

/**
 * Controller for context close operations
 */
@RestController
class ContextCloseController {

    private final ContextCloseService closeService;
    private final CleanupBean cleanupBean;

    public ContextCloseController(ContextCloseService closeService,
                                 CleanupBean cleanupBean) {
        this.closeService = closeService;
        this.cleanupBean = cleanupBean;
    }

    @GetMapping("/context-close/status")
    public Map<String, Object> getStatus() {
        return closeService.getContextStatus();
    }

    @GetMapping("/context-close/cleanup-status")
    public Map<String, Object> getCleanupStatus() {
        return Map.of(
                "resourcesAcquired", cleanupBean.isResourcesAcquired(),
                "cleanupEvents", CleanupBean.getCleanupEvents(),
                "closeEvents", ContextCloseListener.getCloseEvents(),
                "lifecycleEvents", ContextCloseService.getLifecycleEvents()
        );
    }

    @PostMapping("/context-close/close")
    public Map<String, String> closeContext() {
        return closeService.closeContext();
    }
}

/**
 * Documentation:
 * 
 * Context Close Process:
 * 1. Publish ContextClosedEvent
 * 2. Call @PreDestroy methods
 * 3. Call DisposableBean.destroy()
 * 4. Call custom destroy methods
 * 5. Close bean factory
 * 6. Release resources
 * 
 * Manual Close:
 * ConfigurableApplicationContext context = ...;
 * context.close();
 * 
 * Shutdown Hook:
 * context.registerShutdownHook();
 * // Ensures close on JVM shutdown
 * 
 * Listening to Close Event:
 * @EventListener
 * public void onClose(ContextClosedEvent event) {
 *     // Cleanup logic
 * }
 * 
 * Destruction Callbacks:
 * 
 * 1. @PreDestroy:
 *    @PreDestroy
 *    public void cleanup() {
 *        // Cleanup resources
 *    }
 * 
 * 2. DisposableBean:
 *    class MyBean implements DisposableBean {
 *        @Override
 *        public void destroy() {
 *            // Cleanup
 *        }
 *    }
 * 
 * 3. Custom destroy method:
 *    @Bean(destroyMethod = "cleanup")
 *    public MyBean myBean() {
 *        return new MyBean();
 *    }
 * 
 * Graceful Shutdown:
 * server.shutdown=graceful
 * spring.lifecycle.timeout-per-shutdown-phase=20s
 * 
 * Best Practices:
 * - Always register shutdown hook
 * - Close resources in @PreDestroy
 * - Handle exceptions in cleanup
 * - Set reasonable timeouts
 * - Log cleanup operations
 * - Test shutdown scenarios
 * - Don't throw from destroy
 * 
 * Common Cleanup Tasks:
 * - Close database connections
 * - Shutdown thread pools
 * - Flush caches
 * - Close file handles
 * - Stop background tasks
 * - Release locks
 * - Save state
 * 
 * Spring Boot:
 * - Automatic shutdown hook
 * - Graceful shutdown support
 * - Actuator shutdown endpoint
 * - Lifecycle management
 * 
 * Testing:
 * @AfterEach
 * void cleanup() {
 *     context.close();
 * }
 */
