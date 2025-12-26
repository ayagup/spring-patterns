package com.example.events;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Context Refreshed Event Pattern
 * ================================
 * 
 * Demonstrates handling of ContextRefreshedEvent, which is published when
 * the ApplicationContext is initialized or refreshed.
 * 
 * Key Concepts:
 * ------------
 * 1. ContextRefreshedEvent - Published on context refresh
 * 2. Initialization Complete - All beans initialized
 * 3. Post-Startup Actions - Execute after context ready
 * 4. Multiple Triggers - Can fire multiple times
 * 5. Parent-Child Context - Fires for each context
 * 
 * When Event Fires:
 * ----------------
 * - Application startup (first time)
 * - Context refresh via refresh() method
 * - ConfigurableApplicationContext.refresh()
 * - After all beans instantiated and injected
 * - Before application ready event
 * 
 * When to Use:
 * -----------
 * - Initialize data after startup
 * - Cache warming
 * - Start background jobs
 * - Validate configuration
 * - Connect to external systems
 * - Register runtime components
 * - Load reference data
 * 
 * Common Use Cases:
 * ----------------
 * - Database migration check
 * - Cache preloading
 * - Scheduled task registration
 * - WebSocket initialization
 * - External service health check
 * - Feature flag loading
 * - Metric initialization
 * 
 * Important Notes:
 * ---------------
 * - Can fire multiple times (on refresh)
 * - Fires for parent and child contexts separately
 * - Check context to avoid duplicate work
 * - Avoid long-running operations
 * - Consider async for slow tasks
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ContextRefreshedEventPattern implements ApplicationListener<ContextRefreshedEvent> {
    
    private boolean initialized = false;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Avoid duplicate initialization on refresh
        if (!initialized) {
            System.out.println("Context refreshed - initializing application");
            initializeApplication();
            initialized = true;
        }
    }
    
    private void initializeApplication() {
        System.out.println("Performing post-startup initialization");
        // Initialization logic
    }
}

/**
 * Example 2: Cache Warming
 */
@Component
class CacheWarmingListener {
    
    @EventListener
    public void warmCache(ContextRefreshedEvent event) {
        System.out.println("Warming up caches...");
        
        // Load frequently accessed data into cache
        loadUserCache();
        loadProductCache();
        loadConfigurationCache();
        
        System.out.println("Cache warming completed");
    }
    
    private void loadUserCache() {
        // Load users into cache
        System.out.println("Loading user cache");
    }
    
    private void loadProductCache() {
        // Load products into cache
        System.out.println("Loading product cache");
    }
    
    private void loadConfigurationCache() {
        // Load configuration into cache
        System.out.println("Loading configuration cache");
    }
}

/**
 * Example 3: Database Migration Validator
 */
@Component
class DatabaseMigrationValidator {
    
    @EventListener
    public void validateMigrations(ContextRefreshedEvent event) {
        System.out.println("Validating database migrations...");
        
        // Check if all migrations applied
        boolean migrationsValid = checkMigrations();
        
        if (!migrationsValid) {
            System.err.println("WARNING: Database migrations incomplete!");
        } else {
            System.out.println("Database migrations validated successfully");
        }
    }
    
    private boolean checkMigrations() {
        // Check migration status
        return true;
    }
}

/**
 * Example 4: Scheduled Task Initializer
 */
@Component
class ScheduledTaskInitializer {
    
    @EventListener
    public void initializeScheduledTasks(ContextRefreshedEvent event) {
        System.out.println("Initializing scheduled tasks...");
        
        // Register dynamic scheduled tasks
        registerDataCleanupTask();
        registerReportGenerationTask();
        registerHealthCheckTask();
        
        System.out.println("Scheduled tasks initialized");
    }
    
    private void registerDataCleanupTask() {
        System.out.println("Registered: Data cleanup task");
    }
    
    private void registerReportGenerationTask() {
        System.out.println("Registered: Report generation task");
    }
    
    private void registerHealthCheckTask() {
        System.out.println("Registered: Health check task");
    }
}

/**
 * Example 5: External Service Connector
 */
@Component
class ExternalServiceConnector {
    
    private boolean connected = false;
    
    @EventListener
    public void connectToExternalServices(ContextRefreshedEvent event) {
        if (!connected) {
            System.out.println("Connecting to external services...");
            
            connectToPaymentGateway();
            connectToEmailService();
            connectToSmsProvider();
            
            connected = true;
            System.out.println("External services connected");
        }
    }
    
    private void connectToPaymentGateway() {
        System.out.println("Connected: Payment Gateway");
    }
    
    private void connectToEmailService() {
        System.out.println("Connected: Email Service");
    }
    
    private void connectToSmsProvider() {
        System.out.println("Connected: SMS Provider");
    }
}

/**
 * Example 6: Reference Data Loader
 */
@Component
class ReferenceDataLoader {
    
    @EventListener
    public void loadReferenceData(ContextRefreshedEvent event) {
        System.out.println("Loading reference data...");
        
        // Load static/reference data
        loadCountries();
        loadCurrencies();
        loadTimeZones();
        loadIndustries();
        
        System.out.println("Reference data loaded");
    }
    
    private void loadCountries() {
        System.out.println("Loaded: Countries");
    }
    
    private void loadCurrencies() {
        System.out.println("Loaded: Currencies");
    }
    
    private void loadTimeZones() {
        System.out.println("Loaded: Time zones");
    }
    
    private void loadIndustries() {
        System.out.println("Loaded: Industries");
    }
}

/**
 * Example 7: Context-Aware Listener
 */
@Component
class ContextAwareRefreshListener {
    
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        // Get the application context that was refreshed
        String contextId = event.getApplicationContext().getId();
        String displayName = event.getApplicationContext().getDisplayName();
        
        System.out.println("Context refreshed:");
        System.out.println("  ID: " + contextId);
        System.out.println("  Display Name: " + displayName);
        
        // Check if this is the root context
        if (event.getApplicationContext().getParent() == null) {
            System.out.println("  Type: Root Application Context");
            handleRootContextRefresh();
        } else {
            System.out.println("  Type: Child Application Context");
            handleChildContextRefresh();
        }
    }
    
    private void handleRootContextRefresh() {
        System.out.println("Performing root context initialization");
    }
    
    private void handleChildContextRefresh() {
        System.out.println("Performing child context initialization");
    }
}

/**
 * Example 8: Metric Initializer
 */
@Component
class MetricInitializer {
    
    @EventListener
    public void initializeMetrics(ContextRefreshedEvent event) {
        System.out.println("Initializing application metrics...");
        
        registerCounters();
        registerGauges();
        registerTimers();
        
        System.out.println("Metrics initialized");
    }
    
    private void registerCounters() {
        System.out.println("Registered: Request counters");
    }
    
    private void registerGauges() {
        System.out.println("Registered: System gauges");
    }
    
    private void registerTimers() {
        System.out.println("Registered: Operation timers");
    }
}

/**
 * Example 9: Feature Flag Loader
 */
@Component
class FeatureFlagLoader {
    
    @EventListener
    public void loadFeatureFlags(ContextRefreshedEvent event) {
        System.out.println("Loading feature flags...");
        
        // Load feature flags from configuration/database
        java.util.Map<String, Boolean> flags = loadFlags();
        
        System.out.println("Feature flags loaded: " + flags.size() + " flags");
        flags.forEach((key, value) -> 
            System.out.println("  " + key + ": " + (value ? "ENABLED" : "DISABLED")));
    }
    
    private java.util.Map<String, Boolean> loadFlags() {
        java.util.Map<String, Boolean> flags = new java.util.HashMap<>();
        flags.put("new-dashboard", true);
        flags.put("beta-features", false);
        flags.put("advanced-analytics", true);
        return flags;
    }
}

/**
 * Example 10: Async Initialization
 */
@Component
class AsyncInitializer {
    
    @EventListener
    @org.springframework.scheduling.annotation.Async
    public void performAsyncInitialization(ContextRefreshedEvent event) {
        System.out.println("Starting async initialization (Thread: " + 
                         Thread.currentThread().getName() + ")");
        
        // Perform time-consuming initialization asynchronously
        try {
            Thread.sleep(2000); // Simulate slow operation
            System.out.println("Async initialization completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Usage Examples
 */
class ContextRefreshedEventUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Context Refreshed Event Pattern");
        System.out.println("================================\n");
        
        System.out.println("Event Timing:");
        System.out.println("- Fired after all beans initialized");
        System.out.println("- Before ApplicationReadyEvent");
        System.out.println("- Can fire multiple times on refresh");
        System.out.println("- Fires for parent and child contexts\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Cache warming");
        System.out.println("2. Database migration validation");
        System.out.println("3. Scheduled task registration");
        System.out.println("4. External service connection");
        System.out.println("5. Reference data loading");
        System.out.println("6. Metric initialization");
        System.out.println("7. Feature flag loading");
        System.out.println("8. Configuration validation\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Check for duplicate execution");
        System.out.println("- Use async for slow operations");
        System.out.println("- Handle parent/child contexts");
        System.out.println("- Keep initialization fast");
        System.out.println("- Log initialization steps");
    }
}
