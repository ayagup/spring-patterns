package com.example.demo.patterns.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Actuator Refresh Pattern - Manual Configuration Refresh Endpoint
 * 
 * Purpose:
 * - Provide /actuator/refresh endpoint for manual config refresh
 * - Trigger configuration reload on demand
 * - Return list of refreshed property keys
 * - Enable integration with CI/CD pipelines
 * - Support automated configuration deployments
 * 
 * Use Cases:
 * - Manual configuration refresh after config server update
 * - CI/CD pipeline integration (deploy config, trigger refresh)
 * - Testing configuration changes in staging
 * - Emergency configuration updates
 * - Gradual rollout of config changes
 * - Configuration validation before full deployment
 * - Coordinated refresh across multiple services
 * - Configuration troubleshooting
 * - Development and testing workflows
 * - Monitoring configuration drift
 * 
 * Key Concepts:
 * - /actuator/refresh: POST endpoint to trigger refresh
 * - ContextRefresher: Core component that performs refresh
 * - RefreshEndpoint: Spring Boot Actuator endpoint
 * - Property Key List: Returns keys of changed properties
 * - @RefreshScope: Beans recreated on refresh
 * - Environment Refresh: Updates Spring Environment
 * - Custom Refresh Logic: Extend with additional actions
 * 
 * Implementation Patterns:
 * 1. Basic refresh endpoint usage
 * 2. Custom refresh endpoint with validation
 * 3. Refresh with pre/post hooks
 * 4. Selective property refresh
 * 5. Refresh with rollback capability
 * 6. Refresh history tracking
 * 7. Refresh metrics and monitoring
 * 8. Secure refresh endpoint
 * 9. Conditional refresh logic
 * 10. Multi-stage refresh process
 * 11. Refresh event broadcasting
 * 12. Refresh impact analysis
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
 *         include: refresh,health,info,env
 *   endpoint:
 *     refresh:
 *       enabled: true
 * 
 * # Security for refresh endpoint
 * spring:
 *   security:
 *     user:
 *       name: admin
 *       password: secret
 * 
 * Usage:
 * # Trigger refresh
 * curl -X POST http://localhost:8080/actuator/refresh \
 *   -u admin:secret
 * 
 * # Response example:
 * [
 *   "app.message",
 *   "app.feature.new-ui",
 *   "app.database.max-connections"
 * ]
 * 
 * Warnings:
 * - Refresh is synchronous and blocks the request
 * - Large configuration may cause timeout
 * - Concurrent refreshes should be prevented
 * - Failed refresh may leave inconsistent state
 * - Secure endpoint to prevent unauthorized access
 * - Monitor refresh duration
 * - Test refresh impact before production use
 * - Consider refresh during low-traffic periods
 * - Implement circuit breaker for external config sources
 * - Log all refresh operations for audit
 * 
 * Best Practices:
 * - Secure refresh endpoint with authentication
 * - Implement rate limiting for refresh requests
 * - Track refresh history for audit
 * - Monitor refresh success/failure metrics
 * - Validate configuration before applying
 * - Provide rollback mechanism
 * - Document refresh procedures
 * - Test refresh in staging environment
 * - Use refresh endpoint in CI/CD pipelines
 * - Implement refresh notifications
 */
@SpringBootApplication
public class ActuatorRefreshPattern {

    public static void main(String[] args) {
        SpringApplication.run(ActuatorRefreshPattern.class, args);
    }

    // ============================================
    // Example 1: Basic Refresh Endpoint Usage
    // ============================================
    
    /**
     * Demonstrates using the standard /actuator/refresh endpoint.
     */
    @Component
    public static class RefreshEndpointUsageDemo {
        
        private final ContextRefresher contextRefresher;
        
        public RefreshEndpointUsageDemo(ContextRefresher contextRefresher) {
            this.contextRefresher = contextRefresher;
        }
        
        @PostConstruct
        public void demonstrateUsage() {
            System.out.println("=== Refresh Endpoint Available ===");
            System.out.println("  POST /actuator/refresh");
            System.out.println("  Returns: List of refreshed property keys");
            System.out.println();
            System.out.println("  Example:");
            System.out.println("  curl -X POST http://localhost:8080/actuator/refresh");
        }
        
        public Set<String> performRefresh() {
            System.out.println("Performing manual refresh...");
            Set<String> refreshedKeys = contextRefresher.refresh();
            System.out.println("Refreshed " + refreshedKeys.size() + " properties:");
            refreshedKeys.forEach(key -> System.out.println("  - " + key));
            return refreshedKeys;
        }
    }

    // ============================================
    // Example 2: Custom Refresh Endpoint
    // ============================================
    
    /**
     * Custom refresh endpoint with additional functionality.
     */
    @Component
    @Endpoint(id = "custom-refresh")
    public static class CustomRefreshEndpoint {
        
        private final ContextRefresher contextRefresher;
        private final ConfigurableEnvironment environment;
        private final List<RefreshRecord> refreshHistory = new CopyOnWriteArrayList<>();
        
        public CustomRefreshEndpoint(ContextRefresher contextRefresher,
                                    ConfigurableEnvironment environment) {
            this.contextRefresher = contextRefresher;
            this.environment = environment;
        }
        
        @WriteOperation
        public Map<String, Object> refresh() {
            LocalDateTime startTime = LocalDateTime.now();
            
            System.out.println("=== Custom Refresh Started ===");
            System.out.println("  Time: " + startTime);
            
            // Perform refresh
            Set<String> refreshedKeys = contextRefresher.refresh();
            
            LocalDateTime endTime = LocalDateTime.now();
            
            // Record refresh
            RefreshRecord record = new RefreshRecord(
                UUID.randomUUID().toString(),
                startTime,
                endTime,
                refreshedKeys,
                true,
                null
            );
            
            refreshHistory.add(record);
            
            // Keep last 100 records
            if (refreshHistory.size() > 100) {
                refreshHistory.remove(0);
            }
            
            System.out.println("  Refreshed: " + refreshedKeys.size() + " properties");
            System.out.println("  Duration: " + java.time.Duration.between(startTime, endTime).toMillis() + "ms");
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("id", record.id);
            response.put("startTime", startTime);
            response.put("endTime", endTime);
            response.put("refreshedKeys", refreshedKeys);
            response.put("count", refreshedKeys.size());
            response.put("success", true);
            
            return response;
        }
        
        @ReadOperation
        public Map<String, Object> getRefreshHistory() {
            Map<String, Object> result = new HashMap<>();
            result.put("totalRefreshes", refreshHistory.size());
            result.put("history", refreshHistory);
            return result;
        }
        
        public static class RefreshRecord {
            public final String id;
            public final LocalDateTime startTime;
            public final LocalDateTime endTime;
            public final Set<String> refreshedKeys;
            public final boolean success;
            public final String errorMessage;
            
            public RefreshRecord(String id, LocalDateTime startTime, LocalDateTime endTime,
                               Set<String> refreshedKeys, boolean success, String errorMessage) {
                this.id = id;
                this.startTime = startTime;
                this.endTime = endTime;
                this.refreshedKeys = refreshedKeys;
                this.success = success;
                this.errorMessage = errorMessage;
            }
        }
    }

    // ============================================
    // Example 3: Refresh with Pre/Post Hooks
    // ============================================
    
    /**
     * Execute custom logic before and after refresh.
     */
    @Service
    public static class RefreshHookService {
        
        private final ContextRefresher contextRefresher;
        private final List<RefreshHook> preRefreshHooks = new ArrayList<>();
        private final List<RefreshHook> postRefreshHooks = new ArrayList<>();
        
        public RefreshHookService(ContextRefresher contextRefresher) {
            this.contextRefresher = contextRefresher;
            registerDefaultHooks();
        }
        
        private void registerDefaultHooks() {
            // Pre-refresh hooks
            addPreRefreshHook(new RefreshHook("ValidateConfig", () -> {
                System.out.println("  [Pre] Validating configuration...");
                // Validation logic here
            }));
            
            addPreRefreshHook(new RefreshHook("BackupConfig", () -> {
                System.out.println("  [Pre] Backing up current configuration...");
                // Backup logic here
            }));
            
            // Post-refresh hooks
            addPostRefreshHook(new RefreshHook("ClearCaches", () -> {
                System.out.println("  [Post] Clearing caches...");
                // Cache clearing logic here
            }));
            
            addPostRefreshHook(new RefreshHook("NotifyServices", () -> {
                System.out.println("  [Post] Notifying dependent services...");
                // Notification logic here
            }));
        }
        
        public void addPreRefreshHook(RefreshHook hook) {
            preRefreshHooks.add(hook);
        }
        
        public void addPostRefreshHook(RefreshHook hook) {
            postRefreshHooks.add(hook);
        }
        
        public Set<String> performRefreshWithHooks() {
            System.out.println("=== Refresh with Hooks ===");
            
            // Execute pre-refresh hooks
            System.out.println("Executing pre-refresh hooks:");
            for (RefreshHook hook : preRefreshHooks) {
                try {
                    hook.execute();
                } catch (Exception e) {
                    System.err.println("  [Pre] Hook failed: " + hook.name + " - " + e.getMessage());
                }
            }
            
            // Perform refresh
            System.out.println("Performing refresh...");
            Set<String> refreshedKeys = contextRefresher.refresh();
            System.out.println("  Refreshed: " + refreshedKeys.size() + " properties");
            
            // Execute post-refresh hooks
            System.out.println("Executing post-refresh hooks:");
            for (RefreshHook hook : postRefreshHooks) {
                try {
                    hook.execute();
                } catch (Exception e) {
                    System.err.println("  [Post] Hook failed: " + hook.name + " - " + e.getMessage());
                }
            }
            
            return refreshedKeys;
        }
        
        public static class RefreshHook {
            public final String name;
            private final Runnable action;
            
            public RefreshHook(String name, Runnable action) {
                this.name = name;
                this.action = action;
            }
            
            public void execute() {
                action.run();
            }
        }
    }

    // ============================================
    // Example 4: Refresh Metrics Tracker
    // ============================================
    
    /**
     * Track refresh metrics for monitoring.
     */
    @Component
    public static class RefreshMetricsTracker {
        
        private final Map<String, Integer> propertyRefreshCounts = new ConcurrentHashMap<>();
        private int totalRefreshes = 0;
        private int successfulRefreshes = 0;
        private int failedRefreshes = 0;
        private LocalDateTime lastRefreshTime;
        private long totalRefreshDurationMillis = 0;
        
        public void recordRefresh(Set<String> refreshedKeys, long durationMillis, boolean success) {
            totalRefreshes++;
            lastRefreshTime = LocalDateTime.now();
            totalRefreshDurationMillis += durationMillis;
            
            if (success) {
                successfulRefreshes++;
                
                // Track per-property refresh counts
                for (String key : refreshedKeys) {
                    propertyRefreshCounts.merge(key, 1, Integer::sum);
                }
            } else {
                failedRefreshes++;
            }
        }
        
        public Map<String, Object> getMetrics() {
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalRefreshes", totalRefreshes);
            metrics.put("successfulRefreshes", successfulRefreshes);
            metrics.put("failedRefreshes", failedRefreshes);
            metrics.put("lastRefreshTime", lastRefreshTime);
            
            if (totalRefreshes > 0) {
                double avgDuration = (double) totalRefreshDurationMillis / totalRefreshes;
                metrics.put("averageRefreshDurationMs", avgDuration);
                
                double successRate = (double) successfulRefreshes / totalRefreshes * 100;
                metrics.put("successRate", String.format("%.2f%%", successRate));
            }
            
            metrics.put("propertyRefreshCounts", new HashMap<>(propertyRefreshCounts));
            
            return metrics;
        }
        
        public Map<String, Integer> getMostRefreshedProperties(int limit) {
            return propertyRefreshCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(LinkedHashMap::new, 
                    (map, entry) -> map.put(entry.getKey(), entry.getValue()), 
                    LinkedHashMap::putAll);
        }
    }

    // ============================================
    // Example 5: Selective Property Refresh
    // ============================================
    
    /**
     * Refresh only specific properties instead of all.
     */
    @Service
    public static class SelectiveRefreshService {
        
        private final ConfigurableEnvironment environment;
        private final ApplicationEventPublisher eventPublisher;
        
        public SelectiveRefreshService(ConfigurableEnvironment environment,
                                      ApplicationEventPublisher eventPublisher) {
            this.environment = environment;
            this.eventPublisher = eventPublisher;
        }
        
        public Map<String, String> refreshProperties(Set<String> propertyKeys) {
            Map<String, String> refreshedProperties = new HashMap<>();
            
            System.out.println("=== Selective Refresh ===");
            System.out.println("  Properties to refresh: " + propertyKeys.size());
            
            for (String key : propertyKeys) {
                String value = environment.getProperty(key);
                if (value != null) {
                    refreshedProperties.put(key, value);
                    System.out.println("  Refreshed: " + key + " = " + value);
                }
            }
            
            return refreshedProperties;
        }
        
        public Map<String, String> refreshPropertiesWithPrefix(String prefix) {
            Map<String, String> refreshedProperties = new HashMap<>();
            
            System.out.println("=== Refresh by Prefix ===");
            System.out.println("  Prefix: " + prefix);
            
            // In real implementation, would iterate all property sources
            // For demonstration, showing concept
            
            return refreshedProperties;
        }
    }

    // ============================================
    // Example 6: Refresh with Validation
    // ============================================
    
    /**
     * Validate configuration before applying refresh.
     */
    @Service
    public static class ValidatingRefreshService {
        
        private final ContextRefresher contextRefresher;
        private final ConfigurableEnvironment environment;
        
        public ValidatingRefreshService(ContextRefresher contextRefresher,
                                       ConfigurableEnvironment environment) {
            this.contextRefresher = contextRefresher;
            this.environment = environment;
        }
        
        public RefreshResult refreshWithValidation() {
            System.out.println("=== Refresh with Validation ===");
            
            // Capture current state
            Map<String, String> currentState = captureState();
            
            // Perform refresh
            long startTime = System.currentTimeMillis();
            Set<String> refreshedKeys = contextRefresher.refresh();
            long duration = System.currentTimeMillis() - startTime;
            
            // Validate new state
            ValidationResult validation = validateConfiguration();
            
            if (!validation.isValid()) {
                System.out.println("  Validation FAILED:");
                validation.errors.forEach(error -> System.out.println("    - " + error));
                
                // In real implementation, would rollback here
                
                return new RefreshResult(false, refreshedKeys, validation.errors, duration);
            }
            
            System.out.println("  Validation PASSED");
            System.out.println("  Refreshed: " + refreshedKeys.size() + " properties");
            
            return new RefreshResult(true, refreshedKeys, Collections.emptyList(), duration);
        }
        
        private Map<String, String> captureState() {
            Map<String, String> state = new HashMap<>();
            // Capture important properties
            return state;
        }
        
        private ValidationResult validateConfiguration() {
            List<String> errors = new ArrayList<>();
            
            // Validate database max connections
            Integer maxConn = environment.getProperty("app.database.max-connections", Integer.class);
            if (maxConn != null && maxConn <= 0) {
                errors.add("Invalid max-connections: " + maxConn);
            }
            
            // Validate cache TTL
            Integer cacheTtl = environment.getProperty("app.cache.ttl", Integer.class);
            if (cacheTtl != null && cacheTtl < 0) {
                errors.add("Invalid cache TTL: " + cacheTtl);
            }
            
            return new ValidationResult(errors.isEmpty(), errors);
        }
        
        public static class RefreshResult {
            public final boolean success;
            public final Set<String> refreshedKeys;
            public final List<String> errors;
            public final long durationMillis;
            
            public RefreshResult(boolean success, Set<String> refreshedKeys, 
                               List<String> errors, long durationMillis) {
                this.success = success;
                this.refreshedKeys = refreshedKeys;
                this.errors = errors;
                this.durationMillis = durationMillis;
            }
        }
        
        public static class ValidationResult {
            public final boolean valid;
            public final List<String> errors;
            
            public ValidationResult(boolean valid, List<String> errors) {
                this.valid = valid;
                this.errors = errors;
            }
            
            public boolean isValid() {
                return valid;
            }
        }
    }

    // ============================================
    // Example 7: Refresh Controller
    // ============================================
    
    /**
     * REST endpoints for refresh operations.
     */
    @RestController
    @RequestMapping("/refresh")
    public static class RefreshController {
        
        private final RefreshEndpointUsageDemo basicRefresh;
        private final RefreshHookService hookService;
        private final RefreshMetricsTracker metricsTracker;
        private final SelectiveRefreshService selectiveRefresh;
        private final ValidatingRefreshService validatingRefresh;
        
        public RefreshController(
                RefreshEndpointUsageDemo basicRefresh,
                RefreshHookService hookService,
                RefreshMetricsTracker metricsTracker,
                SelectiveRefreshService selectiveRefresh,
                ValidatingRefreshService validatingRefresh) {
            this.basicRefresh = basicRefresh;
            this.hookService = hookService;
            this.metricsTracker = metricsTracker;
            this.selectiveRefresh = selectiveRefresh;
            this.validatingRefresh = validatingRefresh;
        }
        
        @PostMapping("/basic")
        public Map<String, Object> performBasicRefresh() {
            long start = System.currentTimeMillis();
            Set<String> refreshedKeys = basicRefresh.performRefresh();
            long duration = System.currentTimeMillis() - start;
            
            metricsTracker.recordRefresh(refreshedKeys, duration, true);
            
            Map<String, Object> result = new HashMap<>();
            result.put("refreshedKeys", refreshedKeys);
            result.put("count", refreshedKeys.size());
            result.put("durationMs", duration);
            return result;
        }
        
        @PostMapping("/with-hooks")
        public Map<String, Object> performRefreshWithHooks() {
            long start = System.currentTimeMillis();
            Set<String> refreshedKeys = hookService.performRefreshWithHooks();
            long duration = System.currentTimeMillis() - start;
            
            metricsTracker.recordRefresh(refreshedKeys, duration, true);
            
            Map<String, Object> result = new HashMap<>();
            result.put("refreshedKeys", refreshedKeys);
            result.put("count", refreshedKeys.size());
            result.put("durationMs", duration);
            return result;
        }
        
        @PostMapping("/validated")
        public ValidatingRefreshService.RefreshResult performValidatedRefresh() {
            ValidatingRefreshService.RefreshResult result = validatingRefresh.refreshWithValidation();
            metricsTracker.recordRefresh(result.refreshedKeys, result.durationMillis, result.success);
            return result;
        }
        
        @PostMapping("/selective")
        public Map<String, String> performSelectiveRefresh(@RequestBody Set<String> propertyKeys) {
            return selectiveRefresh.refreshProperties(propertyKeys);
        }
        
        @GetMapping("/metrics")
        public Map<String, Object> getMetrics() {
            return metricsTracker.getMetrics();
        }
        
        @GetMapping("/top-refreshed/{limit}")
        public Map<String, Integer> getTopRefreshed(@PathVariable int limit) {
            return metricsTracker.getMostRefreshedProperties(limit);
        }
    }
}
