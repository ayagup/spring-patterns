package com.example.demo.patterns.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dynamic Configuration Pattern - Runtime Configuration Updates
 * 
 * Purpose:
 * - Update configuration at runtime without restart
 * - Load configuration from external sources dynamically
 * - Support hot-reload of configuration changes
 * - Enable feature toggles and A/B testing
 * - Provide real-time configuration updates
 * 
 * Use Cases:
 * - Feature flag management (enable/disable features)
 * - A/B testing configuration (split traffic percentages)
 * - Business rules updates (pricing, discounts)
 * - API rate limits adjustment
 * - Circuit breaker thresholds tuning
 * - Cache configuration changes
 * - Database connection pool resizing
 * - Timeout and retry settings
 * - Security policy updates
 * - Multi-tenant configuration per tenant
 * 
 * Configuration Sources:
 * - Database: Store config in database tables
 * - REST API: Fetch config from external API
 * - Redis/Cache: Distributed configuration cache
 * - File System: Watch configuration files
 * - Environment Variables: Override via env vars
 * - Configuration Server: Spring Cloud Config Server
 * - Consul/Etcd: Distributed key-value stores
 * - AWS Parameter Store: Cloud-based config
 * - Azure App Configuration: Azure config service
 * - HashiCorp Vault: Secrets management
 * 
 * Implementation Patterns:
 * 1. Dynamic property source from database
 * 2. Scheduled configuration reload
 * 3. REST API configuration endpoint
 * 4. Feature toggle service
 * 5. A/B testing configuration
 * 6. Multi-tenant dynamic config
 * 7. Configuration versioning
 * 8. Configuration rollback
 * 9. Configuration validation before apply
 * 10. Gradual rollout (canary config)
 * 11. Configuration change notifications
 * 12. Configuration audit trail
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
 *   dynamic:
 *     reload-interval: 30000  # 30 seconds
 *     enabled: true
 * 
 * Warnings:
 * - Frequent reloads can impact performance
 * - Validate configuration before applying
 * - Handle reload failures gracefully
 * - Test with various configuration values
 * - Monitor configuration change frequency
 * - Consider eventual consistency in distributed systems
 * - Protect configuration endpoints with security
 * - Log all configuration changes for audit
 * - Implement rollback for bad configurations
 * - Be cautious with stateful services
 * 
 * Best Practices:
 * - Use @RefreshScope for config-dependent beans
 * - Implement configuration validation
 * - Provide default values for all properties
 * - Version configuration changes
 * - Enable configuration rollback
 * - Monitor configuration reload metrics
 * - Document configuration properties
 * - Test configuration changes in staging
 * - Use circuit breakers for external config sources
 * - Cache configuration locally for resilience
 */
@SpringBootApplication
@EnableScheduling
public class DynamicConfigurationPattern {

    public static void main(String[] args) {
        SpringApplication.run(DynamicConfigurationPattern.class, args);
    }

    // ============================================
    // Example 1: In-Memory Dynamic Property Source
    // ============================================
    
    /**
     * Store configuration in memory, update at runtime.
     * Serves as cache for external configuration.
     */
    @Component
    public static class DynamicPropertySource {
        
        private final ConfigurableEnvironment environment;
        private final Map<String, Object> dynamicProperties = new ConcurrentHashMap<>();
        private static final String PROPERTY_SOURCE_NAME = "dynamicPropertySource";
        
        public DynamicPropertySource(ConfigurableEnvironment environment) {
            this.environment = environment;
            initializePropertySource();
        }
        
        private void initializePropertySource() {
            MutablePropertySources propertySources = environment.getPropertySources();
            
            if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
                MapPropertySource propertySource = new MapPropertySource(
                    PROPERTY_SOURCE_NAME, 
                    dynamicProperties
                );
                
                // Add as highest priority (first)
                propertySources.addFirst(propertySource);
                
                System.out.println("DynamicPropertySource initialized");
            }
        }
        
        public void setProperty(String key, Object value) {
            Object oldValue = dynamicProperties.get(key);
            dynamicProperties.put(key, value);
            
            System.out.println("Property updated: " + key);
            System.out.println("  Old: " + oldValue);
            System.out.println("  New: " + value);
        }
        
        public Object getProperty(String key) {
            return dynamicProperties.get(key);
        }
        
        public void removeProperty(String key) {
            Object removed = dynamicProperties.remove(key);
            System.out.println("Property removed: " + key + " = " + removed);
        }
        
        public Map<String, Object> getAllProperties() {
            return new HashMap<>(dynamicProperties);
        }
        
        public void setProperties(Map<String, Object> properties) {
            dynamicProperties.putAll(properties);
            System.out.println("Bulk update: " + properties.size() + " properties");
        }
    }

    // ============================================
    // Example 2: Feature Toggle Service
    // ============================================
    
    /**
     * Dynamic feature flag management.
     * Enable/disable features at runtime.
     */
    @Service
    public static class FeatureToggleService {
        
        private final DynamicPropertySource propertySource;
        private final ApplicationEventPublisher eventPublisher;
        private final Map<String, FeatureFlag> features = new ConcurrentHashMap<>();
        
        public FeatureToggleService(DynamicPropertySource propertySource,
                                   ApplicationEventPublisher eventPublisher) {
            this.propertySource = propertySource;
            this.eventPublisher = eventPublisher;
            initializeFeatures();
        }
        
        private void initializeFeatures() {
            // Initialize default features
            registerFeature("new-ui", false, "New UI design");
            registerFeature("beta-api", false, "Beta API endpoints");
            registerFeature("advanced-search", true, "Advanced search functionality");
            registerFeature("real-time-notifications", false, "Real-time push notifications");
        }
        
        public void registerFeature(String name, boolean enabled, String description) {
            FeatureFlag flag = new FeatureFlag(name, enabled, description);
            features.put(name, flag);
            propertySource.setProperty("app.feature." + name, enabled);
        }
        
        public boolean isEnabled(String featureName) {
            FeatureFlag flag = features.get(featureName);
            return flag != null && flag.enabled;
        }
        
        public void enable(String featureName) {
            updateFeature(featureName, true);
        }
        
        public void disable(String featureName) {
            updateFeature(featureName, false);
        }
        
        private void updateFeature(String featureName, boolean enabled) {
            FeatureFlag flag = features.get(featureName);
            if (flag == null) {
                System.out.println("Feature not found: " + featureName);
                return;
            }
            
            boolean wasEnabled = flag.enabled;
            flag.enabled = enabled;
            flag.lastModified = LocalDateTime.now();
            flag.toggleCount++;
            
            propertySource.setProperty("app.feature." + featureName, enabled);
            
            // Trigger refresh event
            eventPublisher.publishEvent(new RefreshEvent(this, null, "Feature toggle: " + featureName));
            
            System.out.println("Feature " + featureName + ": " + wasEnabled + " -> " + enabled);
        }
        
        public Map<String, FeatureFlag> getAllFeatures() {
            return new HashMap<>(features);
        }
        
        public static class FeatureFlag {
            public String name;
            public boolean enabled;
            public String description;
            public LocalDateTime lastModified = LocalDateTime.now();
            public int toggleCount = 0;
            
            public FeatureFlag(String name, boolean enabled, String description) {
                this.name = name;
                this.enabled = enabled;
                this.description = description;
            }
        }
    }

    // ============================================
    // Example 3: A/B Testing Configuration
    // ============================================
    
    /**
     * Manage A/B testing configurations.
     * Split traffic between variants.
     */
    @Service
    public static class ABTestingService {
        
        private final DynamicPropertySource propertySource;
        private final Map<String, ABTest> activeTests = new ConcurrentHashMap<>();
        
        public ABTestingService(DynamicPropertySource propertySource) {
            this.propertySource = propertySource;
            initializeTests();
        }
        
        private void initializeTests() {
            // Create default A/B tests
            createTest("homepage-layout", 50, "Test new homepage layout");
            createTest("checkout-flow", 20, "Test simplified checkout");
        }
        
        public void createTest(String testName, int percentageB, String description) {
            ABTest test = new ABTest(testName, percentageB, description);
            activeTests.put(testName, test);
            
            propertySource.setProperty("app.abtest." + testName + ".percentage-b", percentageB);
            
            System.out.println("A/B Test created: " + testName);
            System.out.println("  Variant A: " + (100 - percentageB) + "%");
            System.out.println("  Variant B: " + percentageB + "%");
        }
        
        public String getVariant(String testName, String userId) {
            ABTest test = activeTests.get(testName);
            if (test == null) {
                return "A"; // Default variant
            }
            
            // Simple hash-based assignment
            int hash = Math.abs(userId.hashCode());
            int bucket = hash % 100;
            
            String variant = bucket < test.percentageB ? "B" : "A";
            test.recordAssignment(variant);
            
            return variant;
        }
        
        public void updateTestPercentage(String testName, int percentageB) {
            ABTest test = activeTests.get(testName);
            if (test == null) {
                System.out.println("Test not found: " + testName);
                return;
            }
            
            int oldPercentage = test.percentageB;
            test.percentageB = percentageB;
            test.lastModified = LocalDateTime.now();
            
            propertySource.setProperty("app.abtest." + testName + ".percentage-b", percentageB);
            
            System.out.println("A/B Test updated: " + testName);
            System.out.println("  Variant B: " + oldPercentage + "% -> " + percentageB + "%");
        }
        
        public Map<String, ABTest> getAllTests() {
            return new HashMap<>(activeTests);
        }
        
        public static class ABTest {
            public String name;
            public int percentageB;
            public String description;
            public LocalDateTime created = LocalDateTime.now();
            public LocalDateTime lastModified = LocalDateTime.now();
            public final AtomicInteger assignmentsA = new AtomicInteger(0);
            public final AtomicInteger assignmentsB = new AtomicInteger(0);
            
            public ABTest(String name, int percentageB, String description) {
                this.name = name;
                this.percentageB = percentageB;
                this.description = description;
            }
            
            public void recordAssignment(String variant) {
                if ("A".equals(variant)) {
                    assignmentsA.incrementAndGet();
                } else {
                    assignmentsB.incrementAndGet();
                }
            }
        }
    }

    // ============================================
    // Example 4: Dynamic Rate Limiting Configuration
    // ============================================
    
    /**
     * Adjust rate limits at runtime.
     */
    @Service
    @RefreshScope
    public static class DynamicRateLimitService {
        
        @Value("${app.rate-limit.requests-per-second:10}")
        private int requestsPerSecond;
        
        @Value("${app.rate-limit.burst-capacity:20}")
        private int burstCapacity;
        
        private final AtomicInteger currentRequests = new AtomicInteger(0);
        private long lastResetTime = System.currentTimeMillis();
        
        @PostConstruct
        public void init() {
            System.out.println("DynamicRateLimitService initialized:");
            System.out.println("  Requests/sec: " + requestsPerSecond);
            System.out.println("  Burst capacity: " + burstCapacity);
        }
        
        public boolean allowRequest() {
            resetIfNeeded();
            
            int current = currentRequests.incrementAndGet();
            boolean allowed = current <= requestsPerSecond;
            
            if (!allowed) {
                currentRequests.decrementAndGet();
            }
            
            return allowed;
        }
        
        private void resetIfNeeded() {
            long now = System.currentTimeMillis();
            if (now - lastResetTime >= 1000) {
                currentRequests.set(0);
                lastResetTime = now;
            }
        }
        
        public Map<String, Object> getConfig() {
            Map<String, Object> config = new HashMap<>();
            config.put("requestsPerSecond", requestsPerSecond);
            config.put("burstCapacity", burstCapacity);
            config.put("currentRequests", currentRequests.get());
            return config;
        }
    }

    // ============================================
    // Example 5: Scheduled Configuration Reload
    // ============================================
    
    /**
     * Periodically reload configuration from external source.
     */
    @Component
    public static class ScheduledConfigReloader {
        
        private final DynamicPropertySource propertySource;
        private final ApplicationEventPublisher eventPublisher;
        private int reloadCount = 0;
        
        public ScheduledConfigReloader(DynamicPropertySource propertySource,
                                      ApplicationEventPublisher eventPublisher) {
            this.propertySource = propertySource;
            this.eventPublisher = eventPublisher;
        }
        
        @Scheduled(fixedDelayString = "${app.dynamic.reload-interval:30000}")
        public void reloadConfiguration() {
            if (!isReloadEnabled()) {
                return;
            }
            
            reloadCount++;
            System.out.println("=== Scheduled Configuration Reload #" + reloadCount + " ===");
            
            // Simulate fetching from external source
            Map<String, Object> externalConfig = fetchExternalConfiguration();
            
            // Update properties
            propertySource.setProperties(externalConfig);
            
            // Trigger refresh
            eventPublisher.publishEvent(new RefreshEvent(this, null, "Scheduled reload"));
            
            System.out.println("  Loaded " + externalConfig.size() + " properties");
        }
        
        private Map<String, Object> fetchExternalConfiguration() {
            // Simulate external config fetch
            Map<String, Object> config = new HashMap<>();
            config.put("app.message", "Updated at " + LocalDateTime.now());
            config.put("app.version", "1.0." + reloadCount);
            return config;
        }
        
        private boolean isReloadEnabled() {
            Object enabled = propertySource.getProperty("app.dynamic.enabled");
            return enabled == null || Boolean.parseBoolean(enabled.toString());
        }
        
        public int getReloadCount() {
            return reloadCount;
        }
    }

    // ============================================
    // Example 6: Dynamic Configuration Controller
    // ============================================
    
    /**
     * REST endpoints to manage dynamic configuration.
     */
    @RestController
    @RequestMapping("/dynamic-config")
    public static class DynamicConfigController {
        
        private final DynamicPropertySource propertySource;
        private final FeatureToggleService featureService;
        private final ABTestingService abTestService;
        private final DynamicRateLimitService rateLimitService;
        private final ScheduledConfigReloader reloader;
        private final ApplicationEventPublisher eventPublisher;
        
        public DynamicConfigController(
                DynamicPropertySource propertySource,
                FeatureToggleService featureService,
                ABTestingService abTestService,
                DynamicRateLimitService rateLimitService,
                ScheduledConfigReloader reloader,
                ApplicationEventPublisher eventPublisher) {
            this.propertySource = propertySource;
            this.featureService = featureService;
            this.abTestService = abTestService;
            this.rateLimitService = rateLimitService;
            this.reloader = reloader;
            this.eventPublisher = eventPublisher;
        }
        
        @GetMapping("/properties")
        public Map<String, Object> getAllProperties() {
            return propertySource.getAllProperties();
        }
        
        @GetMapping("/properties/{key}")
        public Map<String, Object> getProperty(@PathVariable String key) {
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("value", propertySource.getProperty(key));
            return result;
        }
        
        @PostMapping("/properties/{key}")
        public Map<String, Object> setProperty(@PathVariable String key, 
                                              @RequestBody String value) {
            propertySource.setProperty(key, value);
            eventPublisher.publishEvent(new RefreshEvent(this, null, "Property updated: " + key));
            
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("value", value);
            result.put("timestamp", LocalDateTime.now());
            return result;
        }
        
        @DeleteMapping("/properties/{key}")
        public void removeProperty(@PathVariable String key) {
            propertySource.removeProperty(key);
        }
        
        @GetMapping("/features")
        public Map<String, FeatureToggleService.FeatureFlag> getFeatures() {
            return featureService.getAllFeatures();
        }
        
        @PostMapping("/features/{name}/enable")
        public void enableFeature(@PathVariable String name) {
            featureService.enable(name);
        }
        
        @PostMapping("/features/{name}/disable")
        public void disableFeature(@PathVariable String name) {
            featureService.disable(name);
        }
        
        @GetMapping("/ab-tests")
        public Map<String, ABTestingService.ABTest> getABTests() {
            return abTestService.getAllTests();
        }
        
        @PostMapping("/ab-tests/{name}/percentage")
        public void updateTestPercentage(@PathVariable String name, 
                                        @RequestParam int percentage) {
            abTestService.updateTestPercentage(name, percentage);
        }
        
        @GetMapping("/ab-tests/{name}/variant")
        public Map<String, String> getVariant(@PathVariable String name, 
                                             @RequestParam String userId) {
            String variant = abTestService.getVariant(name, userId);
            Map<String, String> result = new HashMap<>();
            result.put("test", name);
            result.put("userId", userId);
            result.put("variant", variant);
            return result;
        }
        
        @GetMapping("/rate-limit")
        public Map<String, Object> getRateLimitConfig() {
            return rateLimitService.getConfig();
        }
        
        @GetMapping("/reload-stats")
        public Map<String, Object> getReloadStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("reloadCount", reloader.getReloadCount());
            return stats;
        }
        
        @PostMapping("/reload-now")
        public Map<String, Object> reloadNow() {
            reloader.reloadConfiguration();
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "reloaded");
            result.put("timestamp", LocalDateTime.now());
            return result;
        }
    }
}
