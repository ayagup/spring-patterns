package com.example.demo.patterns.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Config Change Event Pattern - Listen and React to Configuration Changes
 * 
 * Purpose:
 * - Detect when configuration properties change
 * - Execute custom logic in response to config updates
 * - Track configuration change history
 * - Validate new configuration before applying
 * - Coordinate multiple components on config change
 * 
 * Use Cases:
 * - Clear caches when cache TTL changes
 * - Reconnect database when connection properties change
 * - Update circuit breaker when thresholds change
 * - Refresh API client when endpoint URL changes
 * - Reload security rules when auth config changes
 * - Update thread pool size when concurrency settings change
 * - Invalidate sessions when session timeout changes
 * - Restart scheduled tasks when cron expressions change
 * - Reload feature flags for A/B testing
 * - Update rate limiting when quota changes
 * 
 * Key Events:
 * - RefreshScopeRefreshedEvent: Fired when @RefreshScope beans recreated
 * - EnvironmentChangeEvent: Fired when Environment properties change
 * - RefreshEvent: Custom event for specific config changes
 * - PropertySourcesPropertyChangedEvent: Property source modification
 * 
 * Implementation Patterns:
 * 1. Listen to RefreshScopeRefreshedEvent
 * 2. Listen to EnvironmentChangeEvent
 * 3. Custom configuration change event
 * 4. Track changed properties
 * 5. Validate configuration changes
 * 6. Rollback on invalid configuration
 * 7. Cascade refresh to dependent components
 * 8. Configuration change notification
 * 9. Audit configuration changes
 * 10. Compare before/after configuration
 * 11. Conditional reload based on changed properties
 * 12. Asynchronous configuration reload
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-config</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * 
 * Configuration (application.yml):
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: refresh,env,health
 * 
 * app:
 *   cache:
 *     ttl: 3600
 *     max-size: 1000
 *   database:
 *     url: jdbc:mysql://localhost:3306/mydb
 *     max-connections: 10
 *   feature:
 *     new-ui: false
 *     beta-api: false
 * 
 * Warnings:
 * - Event listeners execute synchronously (may block refresh)
 * - Heavy operations should be async
 * - Failed event handlers can prevent refresh
 * - Multiple listeners execute in undefined order
 * - Exception in listener may leave inconsistent state
 * - Listeners triggered multiple times for same refresh
 * - Be cautious with stateful operations
 * - Test event handling thoroughly
 * - Monitor event processing time
 * - Consider idempotent operations
 * 
 * Best Practices:
 * - Keep event handlers lightweight
 * - Use @Async for long-running operations
 * - Log all configuration changes
 * - Implement proper error handling
 * - Use @Order to control listener execution order
 * - Validate configuration before applying
 * - Provide rollback mechanism
 * - Monitor configuration change metrics
 * - Document expected behavior
 * - Test with various configuration scenarios
 */
@SpringBootApplication
public class ConfigChangeEventPattern {

    public static void main(String[] args) {
        SpringApplication.run(ConfigChangeEventPattern.class, args);
    }

    // ============================================
    // Example 1: RefreshScopeRefreshedEvent Listener
    // ============================================
    
    /**
     * Listen to refresh scope events.
     * Triggered when @RefreshScope beans are recreated.
     */
    @Component
    public static class RefreshScopeEventListener {
        
        private final List<RefreshEvent> refreshHistory = new CopyOnWriteArrayList<>();
        
        @EventListener
        public void handleRefreshScopeRefreshed(RefreshScopeRefreshedEvent event) {
            String eventName = event.getName();
            String eventSource = event.getSource().toString();
            
            RefreshEvent refreshEvent = new RefreshEvent(
                "RefreshScopeRefreshed",
                eventName,
                LocalDateTime.now(),
                Collections.emptySet()
            );
            
            refreshHistory.add(refreshEvent);
            
            System.out.println("=== RefreshScope Refreshed ===");
            System.out.println("  Event: " + eventName);
            System.out.println("  Source: " + eventSource);
            System.out.println("  Timestamp: " + refreshEvent.timestamp);
            System.out.println("  Total Refreshes: " + refreshHistory.size());
            
            // Keep last 100 events
            if (refreshHistory.size() > 100) {
                refreshHistory.remove(0);
            }
        }
        
        public List<RefreshEvent> getRefreshHistory() {
            return new ArrayList<>(refreshHistory);
        }
        
        public static class RefreshEvent {
            public final String eventType;
            public final String eventName;
            public final LocalDateTime timestamp;
            public final Set<String> changedKeys;
            
            public RefreshEvent(String eventType, String eventName, 
                              LocalDateTime timestamp, Set<String> changedKeys) {
                this.eventType = eventType;
                this.eventName = eventName;
                this.timestamp = timestamp;
                this.changedKeys = changedKeys;
            }
        }
    }

    // ============================================
    // Example 2: Configuration Change Tracker
    // ============================================
    
    /**
     * Track which configuration properties changed.
     * Store before/after values for audit.
     */
    @Component
    public static class ConfigurationChangeTracker {
        
        private final Environment environment;
        private final Map<String, String> previousValues = new ConcurrentHashMap<>();
        private final List<ConfigChange> changeHistory = new CopyOnWriteArrayList<>();
        
        public ConfigurationChangeTracker(Environment environment) {
            this.environment = environment;
        }
        
        @PostConstruct
        public void captureInitialState() {
            captureCurrentState();
        }
        
        @EventListener
        public void handleRefresh(RefreshScopeRefreshedEvent event) {
            Map<String, String> currentValues = captureCurrentState();
            List<ConfigChange> changes = detectChanges(previousValues, currentValues);
            
            if (!changes.isEmpty()) {
                changeHistory.addAll(changes);
                
                System.out.println("=== Configuration Changes Detected ===");
                changes.forEach(change -> {
                    System.out.println("  " + change.key + ": " + 
                        change.oldValue + " -> " + change.newValue);
                });
                
                // Keep last 500 changes
                if (changeHistory.size() > 500) {
                    changeHistory.subList(0, changeHistory.size() - 500).clear();
                }
            }
            
            previousValues.clear();
            previousValues.putAll(currentValues);
        }
        
        private Map<String, String> captureCurrentState() {
            Map<String, String> state = new HashMap<>();
            
            // Capture important properties
            List<String> propertiesToTrack = Arrays.asList(
                "app.cache.ttl",
                "app.cache.max-size",
                "app.database.url",
                "app.database.max-connections",
                "app.feature.new-ui",
                "app.feature.beta-api"
            );
            
            for (String property : propertiesToTrack) {
                String value = environment.getProperty(property);
                if (value != null) {
                    state.put(property, value);
                }
            }
            
            return state;
        }
        
        private List<ConfigChange> detectChanges(Map<String, String> oldValues, 
                                                  Map<String, String> newValues) {
            List<ConfigChange> changes = new ArrayList<>();
            LocalDateTime timestamp = LocalDateTime.now();
            
            // Check for changed or new properties
            for (Map.Entry<String, String> entry : newValues.entrySet()) {
                String key = entry.getKey();
                String newValue = entry.getValue();
                String oldValue = oldValues.get(key);
                
                if (oldValue == null || !oldValue.equals(newValue)) {
                    changes.add(new ConfigChange(key, oldValue, newValue, timestamp));
                }
            }
            
            // Check for removed properties
            for (Map.Entry<String, String> entry : oldValues.entrySet()) {
                String key = entry.getKey();
                if (!newValues.containsKey(key)) {
                    changes.add(new ConfigChange(key, entry.getValue(), null, timestamp));
                }
            }
            
            return changes;
        }
        
        public List<ConfigChange> getChangeHistory() {
            return new ArrayList<>(changeHistory);
        }
        
        public List<ConfigChange> getChangeHistory(String propertyKey) {
            return changeHistory.stream()
                .filter(change -> change.key.equals(propertyKey))
                .collect(Collectors.toList());
        }
        
        public static class ConfigChange {
            public final String key;
            public final String oldValue;
            public final String newValue;
            public final LocalDateTime timestamp;
            
            public ConfigChange(String key, String oldValue, String newValue, 
                              LocalDateTime timestamp) {
                this.key = key;
                this.oldValue = oldValue;
                this.newValue = newValue;
                this.timestamp = timestamp;
            }
        }
    }

    // ============================================
    // Example 3: Cache Invalidation on Config Change
    // ============================================
    
    /**
     * Clear cache when cache configuration changes.
     */
    @Service
    public static class CacheInvalidationService {
        
        private final Map<String, Object> cache = new ConcurrentHashMap<>();
        private final ConfigurationChangeTracker changeTracker;
        
        public CacheInvalidationService(ConfigurationChangeTracker changeTracker) {
            this.changeTracker = changeTracker;
        }
        
        @EventListener
        public void handleConfigChange(RefreshScopeRefreshedEvent event) {
            List<ConfigurationChangeTracker.ConfigChange> recentChanges = 
                changeTracker.getChangeHistory();
            
            boolean cacheConfigChanged = recentChanges.stream()
                .anyMatch(change -> change.key.startsWith("app.cache."));
            
            if (cacheConfigChanged) {
                int cacheSize = cache.size();
                cache.clear();
                
                System.out.println("=== Cache Invalidated ===");
                System.out.println("  Cleared " + cacheSize + " entries");
                System.out.println("  Reason: Cache configuration changed");
            }
        }
        
        public void put(String key, Object value) {
            cache.put(key, value);
        }
        
        public Object get(String key) {
            return cache.get(key);
        }
        
        public int size() {
            return cache.size();
        }
    }

    // ============================================
    // Example 4: Database Reconnection on Config Change
    // ============================================
    
    /**
     * Reconnect to database when connection properties change.
     */
    @Service
    public static class DatabaseReconnectionService {
        
        private String currentConnectionUrl;
        private boolean connected = false;
        private final ConfigurationChangeTracker changeTracker;
        
        public DatabaseReconnectionService(ConfigurationChangeTracker changeTracker) {
            this.changeTracker = changeTracker;
        }
        
        @EventListener
        public void handleConfigChange(RefreshScopeRefreshedEvent event) {
            List<ConfigurationChangeTracker.ConfigChange> changes = 
                changeTracker.getChangeHistory();
            
            boolean dbConfigChanged = changes.stream()
                .anyMatch(change -> change.key.startsWith("app.database."));
            
            if (dbConfigChanged) {
                reconnect();
            }
        }
        
        private void reconnect() {
            System.out.println("=== Database Reconnection ===");
            
            if (connected) {
                disconnect();
            }
            
            connect();
        }
        
        private void connect() {
            // Simulate database connection
            currentConnectionUrl = "jdbc:mysql://localhost:3306/mydb";
            connected = true;
            System.out.println("  Connected to: " + currentConnectionUrl);
        }
        
        private void disconnect() {
            System.out.println("  Disconnecting from: " + currentConnectionUrl);
            connected = false;
        }
        
        public boolean isConnected() {
            return connected;
        }
    }

    // ============================================
    // Example 5: Feature Flag Change Listener
    // ============================================
    
    /**
     * React to feature flag changes.
     * Enable/disable features dynamically.
     */
    @Service
    public static class FeatureFlagChangeListener {
        
        private final Map<String, FeatureFlagChange> flagChangeHistory = new ConcurrentHashMap<>();
        
        @EventListener
        public void handleConfigChange(RefreshScopeRefreshedEvent event) {
            System.out.println("=== Feature Flag Change Check ===");
            
            // In real implementation, would compare old and new feature flags
            FeatureFlagChange change = new FeatureFlagChange(
                "new-ui",
                false,
                true,
                LocalDateTime.now()
            );
            
            flagChangeHistory.put(change.flagName, change);
            
            System.out.println("  Flag: " + change.flagName);
            System.out.println("  Changed: " + change.oldValue + " -> " + change.newValue);
            
            handleFeatureChange(change);
        }
        
        private void handleFeatureChange(FeatureFlagChange change) {
            if (change.flagName.equals("new-ui")) {
                if (change.newValue) {
                    System.out.println("  Action: Enable new UI");
                } else {
                    System.out.println("  Action: Disable new UI");
                }
            }
        }
        
        public Map<String, FeatureFlagChange> getFlagChangeHistory() {
            return new HashMap<>(flagChangeHistory);
        }
        
        public static class FeatureFlagChange {
            public final String flagName;
            public final boolean oldValue;
            public final boolean newValue;
            public final LocalDateTime timestamp;
            
            public FeatureFlagChange(String flagName, boolean oldValue, 
                                   boolean newValue, LocalDateTime timestamp) {
                this.flagName = flagName;
                this.oldValue = oldValue;
                this.newValue = newValue;
                this.timestamp = timestamp;
            }
        }
    }

    // ============================================
    // Example 6: Configuration Validation Listener
    // ============================================
    
    /**
     * Validate new configuration before applying.
     * Reject invalid configurations.
     */
    @Component
    public static class ConfigurationValidator {
        
        private final Environment environment;
        private final List<ValidationResult> validationHistory = new CopyOnWriteArrayList<>();
        
        public ConfigurationValidator(Environment environment) {
            this.environment = environment;
        }
        
        @EventListener
        public void validateConfiguration(RefreshScopeRefreshedEvent event) {
            System.out.println("=== Validating Configuration ===");
            
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            // Validate cache TTL
            Integer cacheTtl = environment.getProperty("app.cache.ttl", Integer.class);
            if (cacheTtl != null) {
                if (cacheTtl < 0) {
                    errors.add("Cache TTL cannot be negative: " + cacheTtl);
                } else if (cacheTtl > 86400) {
                    warnings.add("Cache TTL is very high: " + cacheTtl + "s (>24h)");
                }
            }
            
            // Validate max connections
            Integer maxConnections = environment.getProperty("app.database.max-connections", Integer.class);
            if (maxConnections != null) {
                if (maxConnections <= 0) {
                    errors.add("Max connections must be positive: " + maxConnections);
                } else if (maxConnections > 100) {
                    warnings.add("Max connections is very high: " + maxConnections);
                }
            }
            
            ValidationResult result = new ValidationResult(
                errors.isEmpty(),
                errors,
                warnings,
                LocalDateTime.now()
            );
            
            validationHistory.add(result);
            
            if (!errors.isEmpty()) {
                System.out.println("  VALIDATION FAILED:");
                errors.forEach(error -> System.out.println("    ERROR: " + error));
            }
            
            if (!warnings.isEmpty()) {
                System.out.println("  Warnings:");
                warnings.forEach(warning -> System.out.println("    WARN: " + warning));
            }
            
            if (errors.isEmpty() && warnings.isEmpty()) {
                System.out.println("  Validation PASSED");
            }
            
            // Keep last 100 validations
            if (validationHistory.size() > 100) {
                validationHistory.remove(0);
            }
        }
        
        public List<ValidationResult> getValidationHistory() {
            return new ArrayList<>(validationHistory);
        }
        
        public static class ValidationResult {
            public final boolean valid;
            public final List<String> errors;
            public final List<String> warnings;
            public final LocalDateTime timestamp;
            
            public ValidationResult(boolean valid, List<String> errors, 
                                  List<String> warnings, LocalDateTime timestamp) {
                this.valid = valid;
                this.errors = new ArrayList<>(errors);
                this.warnings = new ArrayList<>(warnings);
                this.timestamp = timestamp;
            }
        }
    }

    // ============================================
    // Example 7: Config Change Event Controller
    // ============================================
    
    /**
     * REST endpoints to view configuration change events.
     */
    @RestController
    @RequestMapping("/config-events")
    public static class ConfigChangeEventController {
        
        private final RefreshScopeEventListener refreshListener;
        private final ConfigurationChangeTracker changeTracker;
        private final CacheInvalidationService cacheService;
        private final DatabaseReconnectionService dbService;
        private final FeatureFlagChangeListener flagListener;
        private final ConfigurationValidator validator;
        private final ContextRefresher contextRefresher;
        
        public ConfigChangeEventController(
                RefreshScopeEventListener refreshListener,
                ConfigurationChangeTracker changeTracker,
                CacheInvalidationService cacheService,
                DatabaseReconnectionService dbService,
                FeatureFlagChangeListener flagListener,
                ConfigurationValidator validator,
                ContextRefresher contextRefresher) {
            this.refreshListener = refreshListener;
            this.changeTracker = changeTracker;
            this.cacheService = cacheService;
            this.dbService = dbService;
            this.flagListener = flagListener;
            this.validator = validator;
            this.contextRefresher = contextRefresher;
        }
        
        @GetMapping("/refresh-history")
        public List<RefreshScopeEventListener.RefreshEvent> getRefreshHistory() {
            return refreshListener.getRefreshHistory();
        }
        
        @GetMapping("/changes")
        public List<ConfigurationChangeTracker.ConfigChange> getChanges() {
            return changeTracker.getChangeHistory();
        }
        
        @GetMapping("/changes/{property}")
        public List<ConfigurationChangeTracker.ConfigChange> getPropertyChanges(
                @PathVariable String property) {
            return changeTracker.getChangeHistory(property);
        }
        
        @GetMapping("/cache/size")
        public Map<String, Object> getCacheInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("size", cacheService.size());
            return info;
        }
        
        @GetMapping("/database/status")
        public Map<String, Object> getDatabaseStatus() {
            Map<String, Object> status = new HashMap<>();
            status.put("connected", dbService.isConnected());
            return status;
        }
        
        @GetMapping("/feature-flags/history")
        public Map<String, FeatureFlagChangeListener.FeatureFlagChange> getFlagHistory() {
            return flagListener.getFlagChangeHistory();
        }
        
        @GetMapping("/validation/history")
        public List<ConfigurationValidator.ValidationResult> getValidationHistory() {
            return validator.getValidationHistory();
        }
        
        @PostMapping("/trigger-refresh")
        public Map<String, Object> triggerRefresh() {
            Set<String> refreshedKeys = contextRefresher.refresh();
            
            Map<String, Object> result = new HashMap<>();
            result.put("refreshedKeys", refreshedKeys);
            result.put("count", refreshedKeys.size());
            result.put("timestamp", LocalDateTime.now());
            
            return result;
        }
    }
}
