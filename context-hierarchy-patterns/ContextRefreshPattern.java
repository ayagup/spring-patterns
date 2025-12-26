package com.example.contexthierarchy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Context Refresh Pattern
 * 
 * Demonstrates refreshing the application context to reload configuration,
 * re-initialize beans, and apply new settings without full restart.
 * 
 * Key Concepts:
 * - Context refresh operation
 * - Bean re-initialization
 * - Configuration reload
 * - Refresh lifecycle
 * - @RefreshScope annotation
 * 
 * Use Cases:
 * - Configuration updates
 * - Bean recreation
 * - Hot reload scenarios
 * - Dynamic property changes
 * - Cloud config updates
 */
@SpringBootApplication
public class ContextRefreshPattern {

    public static void main(String[] args) {
        SpringApplication.run(ContextRefreshPattern.class, args);
    }
}

/**
 * Bean tracking refresh events
 */
@Service
class RefreshTrackingBean {
    
    private static final List<String> refreshEvents = new ArrayList<>();
    private int initializationCount = 0;

    @PostConstruct
    public void init() {
        initializationCount++;
        String event = "Bean initialized (count: " + initializationCount + ")";
        refreshEvents.add(event);
        System.out.println(event);
    }

    public int getInitializationCount() {
        return initializationCount;
    }

    public static List<String> getRefreshEvents() {
        return new ArrayList<>(refreshEvents);
    }
}

/**
 * Service demonstrating context refresh
 */
@Service
class ContextRefreshService {

    private final ConfigurableApplicationContext applicationContext;

    public ContextRefreshService(ApplicationContext applicationContext) {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * Manually refresh the context
     * WARNING: Use with caution in production
     */
    public Map<String, Object> refreshContext() {
        try {
            // Refresh reinitializes all beans
            applicationContext.refresh();
            
            return Map.of(
                    "status", "success",
                    "message", "Context refreshed successfully",
                    "timestamp", System.currentTimeMillis()
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * Get context state
     */
    public Map<String, Object> getContextState() {
        return Map.of(
                "active", applicationContext.isActive(),
                "running", applicationContext.isRunning(),
                "beanCount", applicationContext.getBeanDefinitionCount(),
                "startupDate", applicationContext.getStartupDate()
        );
    }
}

/**
 * Controller for context refresh operations
 */
@RestController
class ContextRefreshController {

    private final ContextRefreshService refreshService;
    private final RefreshTrackingBean trackingBean;

    public ContextRefreshController(ContextRefreshService refreshService,
                                   RefreshTrackingBean trackingBean) {
        this.refreshService = refreshService;
        this.trackingBean = trackingBean;
    }

    @GetMapping("/context-refresh/state")
    public Map<String, Object> getContextState() {
        return refreshService.getContextState();
    }

    @PostMapping("/context-refresh/refresh")
    public Map<String, Object> refreshContext() {
        return refreshService.refreshContext();
    }

    @GetMapping("/context-refresh/tracking")
    public Map<String, Object> getTrackingInfo() {
        return Map.of(
                "initializationCount", trackingBean.getInitializationCount(),
                "events", RefreshTrackingBean.getRefreshEvents()
        );
    }
}

/**
 * Documentation:
 * 
 * Context Refresh:
 * - Closes current context
 * - Destroys all beans
 * - Reloads bean definitions
 * - Re-initializes all beans
 * - Triggers lifecycle callbacks
 * 
 * Refresh Process:
 * 1. Prepare to refresh
 * 2. Close old bean factory
 * 3. Create new bean factory
 * 4. Load bean definitions
 * 5. Prepare beans
 * 6. Post-process beans
 * 7. Initialize beans
 * 8. Finish refresh
 * 
 * Manual Refresh:
 * ConfigurableApplicationContext context = ...;
 * context.refresh();
 * 
 * Spring Cloud Config Refresh:
 * @RefreshScope
 * @Component
 * class MyConfig {
 *     @Value("${dynamic.property}")
 *     private String property;
 * }
 * 
 * // Trigger refresh via actuator
 * POST /actuator/refresh
 * 
 * Refresh Events:
 * @EventListener
 * public void onRefresh(ContextRefreshedEvent event) {
 *     // Handle refresh
 * }
 * 
 * @RefreshScope:
 * - Beans recreated on refresh
 * - Properties reloaded
 * - Lazy proxy created
 * - Thread-safe
 * 
 * Use Cases:
 * 1. Configuration Updates:
 *    - Spring Cloud Config changes
 *    - Property file modifications
 *    - Environment updates
 * 
 * 2. Feature Toggles:
 *    - Enable/disable features dynamically
 *    - A/B testing
 *    - Gradual rollouts
 * 
 * 3. Cache Refresh:
 *    - Reload cached data
 *    - Update lookup tables
 *    - Refresh metadata
 * 
 * Best Practices:
 * - Use @RefreshScope sparingly
 * - Don't refresh entire context in production
 * - Use actuator refresh endpoint
 * - Monitor refresh operations
 * - Handle refresh failures
 * - Test refresh scenarios
 * 
 * Limitations:
 * - Not all beans can be refreshed
 * - Singletons may have state
 * - Expensive operation
 * - Temporary unavailability
 * - Potential memory leaks
 * 
 * Alternatives:
 * - @RefreshScope for specific beans
 * - Actuator refresh endpoint
 * - Spring Cloud Bus for broadcast
 * - Graceful restart
 * - Feature flags
 */
