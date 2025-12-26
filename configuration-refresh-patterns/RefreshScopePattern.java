package com.example.demo.patterns.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @RefreshScope Pattern - Spring Cloud Configuration Refresh
 * 
 * Purpose:
 * - Enable beans to be recreated when configuration changes
 * - Refresh properties without restarting application
 * - Support dynamic configuration updates
 * - Maintain application uptime during config changes
 * - Clear cached bean instances on refresh events
 * 
 * Use Cases:
 * - Feature toggle updates without restart
 * - Database connection pool reconfiguration
 * - API endpoint URL changes
 * - Timeout and threshold adjustments
 * - Circuit breaker threshold updates
 * - Rate limiting configuration changes
 * - Cache size/TTL modifications
 * - Thread pool size adjustments
 * - Log level changes
 * - Service discovery endpoint updates
 * 
 * Key Concepts:
 * - @RefreshScope: Annotate beans to enable refresh capability
 * - RefreshScope: Custom scope that caches bean instances
 * - ContextRefresher: Triggers refresh of all @RefreshScope beans
 * - Refresh Event: ApplicationEvent fired when refresh occurs
 * - Bean Recreation: Old instance destroyed, new created with updated properties
 * - Property Resolution: Re-evaluates @Value, @ConfigurationProperties
 * - Lifecycle Callbacks: @PostConstruct/@PreDestroy called on refresh
 * - Thread Safety: Synchronized access during bean recreation
 * 
 * Implementation Patterns:
 * 1. Basic @RefreshScope bean
 * 2. @RefreshScope with @Value properties
 * 3. @RefreshScope with @ConfigurationProperties
 * 4. Database connection refresh
 * 5. Cache configuration refresh
 * 6. Circuit breaker threshold refresh
 * 7. Feature toggle refresh
 * 8. API client configuration refresh
 * 9. Thread pool configuration refresh
 * 10. Multi-bean refresh coordination
 * 11. Refresh lifecycle management
 * 12. Refresh event listening
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
 * spring:
 *   cloud:
 *     config:
 *       uri: http://config-server:8888
 *       fail-fast: true
 *       retry:
 *         initial-interval: 1000
 *         max-attempts: 3
 * 
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: refresh,health,info
 * 
 * # Application properties that can be refreshed
 * app:
 *   feature:
 *     new-ui: false
 *     beta-api: false
 *   database:
 *     max-connections: 10
 *     timeout: 5000
 *   cache:
 *     size: 1000
 *     ttl: 3600
 *   circuit-breaker:
 *     failure-threshold: 50
 *     wait-duration: 5000
 * 
 * Trigger Refresh:
 * POST http://localhost:8080/actuator/refresh
 * 
 * Warnings:
 * - @RefreshScope beans recreated on every refresh (performance cost)
 * - Not suitable for heavy initialization beans (use selectively)
 * - Beans in RefreshScope are proxies (CGLIB), may affect debugging
 * - Singleton beans referencing @RefreshScope beans hold old instances
 * - Use @Autowired for @RefreshScope dependencies, not constructor injection
 * - Refresh is synchronous and blocks request processing
 * - Multiple concurrent refreshes may cause issues
 * - Stateful beans lose state on refresh
 * - Database connections closed and reopened
 * - Long-running operations interrupted
 * 
 * Best Practices:
 * - Use @RefreshScope only for configuration-dependent beans
 * - Keep @RefreshScope beans lightweight
 * - Implement proper cleanup in @PreDestroy
 * - Handle refresh events for coordination
 * - Test refresh behavior thoroughly
 * - Monitor refresh performance impact
 * - Use Spring Cloud Bus for distributed refresh
 * - Document which properties are refreshable
 * - Validate new configuration before applying
 * - Log configuration changes for audit
 */
@SpringBootApplication
@EnableScheduling
public class RefreshScopePattern {

    public static void main(String[] args) {
        SpringApplication.run(RefreshScopePattern.class, args);
    }

    // ============================================
    // Example 1: Basic @RefreshScope Bean
    // ============================================
    
    /**
     * Simplest @RefreshScope usage.
     * Bean recreated when /actuator/refresh is called.
     */
    @Service
    @RefreshScope
    public static class BasicRefreshableService {
        
        @Value("${app.message:Default Message}")
        private String message;
        
        private final String instanceId;
        private final LocalDateTime createdAt;
        
        public BasicRefreshableService() {
            this.instanceId = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            System.out.println("BasicRefreshableService created: " + instanceId);
        }
        
        public String getMessage() {
            return String.format("Message: %s (Instance: %s, Created: %s)", 
                message, instanceId.substring(0, 8), createdAt);
        }
        
        @PreDestroy
        public void destroy() {
            System.out.println("BasicRefreshableService destroyed: " + instanceId);
        }
    }

    // ============================================
    // Example 2: Feature Toggle with @RefreshScope
    // ============================================
    
    /**
     * Enable/disable features without restart.
     * Update feature flags in config server, refresh to apply.
     */
    @Service
    @RefreshScope
    public static class FeatureToggleService {
        
        @Value("${app.feature.new-ui:false}")
        private boolean newUiEnabled;
        
        @Value("${app.feature.beta-api:false}")
        private boolean betaApiEnabled;
        
        @Value("${app.feature.advanced-analytics:false}")
        private boolean advancedAnalyticsEnabled;
        
        private int refreshCount = 0;
        
        @PostConstruct
        public void init() {
            refreshCount++;
            System.out.println("FeatureToggleService initialized (refresh #" + refreshCount + ")");
            logFeatureStatus();
        }
        
        public boolean isNewUiEnabled() {
            return newUiEnabled;
        }
        
        public boolean isBetaApiEnabled() {
            return betaApiEnabled;
        }
        
        public boolean isAdvancedAnalyticsEnabled() {
            return advancedAnalyticsEnabled;
        }
        
        public Map<String, Boolean> getAllFeatures() {
            Map<String, Boolean> features = new LinkedHashMap<>();
            features.put("newUi", newUiEnabled);
            features.put("betaApi", betaApiEnabled);
            features.put("advancedAnalytics", advancedAnalyticsEnabled);
            return features;
        }
        
        private void logFeatureStatus() {
            System.out.println("Feature Flags:");
            System.out.println("  New UI: " + newUiEnabled);
            System.out.println("  Beta API: " + betaApiEnabled);
            System.out.println("  Advanced Analytics: " + advancedAnalyticsEnabled);
        }
    }

    // ============================================
    // Example 3: Database Configuration Refresh
    // ============================================
    
    /**
     * Update database connection pool settings without restart.
     * Closes old connections, creates new pool with updated settings.
     */
    @Component
    @RefreshScope
    public static class DatabaseConfig {
        
        @Value("${app.database.max-connections:10}")
        private int maxConnections;
        
        @Value("${app.database.timeout:5000}")
        private int connectionTimeout;
        
        @Value("${app.database.idle-timeout:600000}")
        private int idleTimeout;
        
        @Value("${app.database.pool-name:HikariPool}")
        private String poolName;
        
        private final Set<String> activeConnections = ConcurrentHashMap.newKeySet();
        private final String poolId;
        
        public DatabaseConfig() {
            this.poolId = UUID.randomUUID().toString().substring(0, 8);
            System.out.println("DatabaseConfig created with pool ID: " + poolId);
        }
        
        @PostConstruct
        public void initializePool() {
            System.out.println("Initializing connection pool: " + poolId);
            System.out.println("  Max Connections: " + maxConnections);
            System.out.println("  Connection Timeout: " + connectionTimeout + "ms");
            System.out.println("  Idle Timeout: " + idleTimeout + "ms");
            System.out.println("  Pool Name: " + poolName);
        }
        
        @PreDestroy
        public void closePool() {
            System.out.println("Closing connection pool: " + poolId);
            System.out.println("  Active connections: " + activeConnections.size());
            activeConnections.clear();
        }
        
        public String getConnection() {
            String connectionId = poolId + "-" + UUID.randomUUID().toString().substring(0, 8);
            activeConnections.add(connectionId);
            return connectionId;
        }
        
        public void releaseConnection(String connectionId) {
            activeConnections.remove(connectionId);
        }
        
        public int getMaxConnections() {
            return maxConnections;
        }
        
        public int getActiveConnectionCount() {
            return activeConnections.size();
        }
    }

    // ============================================
    // Example 4: Cache Configuration Refresh
    // ============================================
    
    /**
     * Update cache settings dynamically.
     * Cache cleared and resized on refresh.
     */
    @Service
    @RefreshScope
    public static class CacheConfig {
        
        @Value("${app.cache.size:1000}")
        private int maxSize;
        
        @Value("${app.cache.ttl:3600}")
        private int ttlSeconds;
        
        @Value("${app.cache.enabled:true}")
        private boolean enabled;
        
        private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
        private final AtomicInteger hitCount = new AtomicInteger(0);
        private final AtomicInteger missCount = new AtomicInteger(0);
        
        @PostConstruct
        public void init() {
            System.out.println("CacheConfig initialized:");
            System.out.println("  Max Size: " + maxSize);
            System.out.println("  TTL: " + ttlSeconds + "s");
            System.out.println("  Enabled: " + enabled);
        }
        
        @PreDestroy
        public void cleanup() {
            System.out.println("CacheConfig cleanup:");
            System.out.println("  Entries: " + cache.size());
            System.out.println("  Hits: " + hitCount.get());
            System.out.println("  Misses: " + missCount.get());
            cache.clear();
        }
        
        public void put(String key, Object value) {
            if (!enabled) return;
            
            if (cache.size() >= maxSize) {
                evictOldest();
            }
            
            cache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlSeconds * 1000));
        }
        
        public Object get(String key) {
            if (!enabled) {
                missCount.incrementAndGet();
                return null;
            }
            
            CacheEntry entry = cache.get(key);
            if (entry == null || entry.isExpired()) {
                missCount.incrementAndGet();
                if (entry != null) {
                    cache.remove(key);
                }
                return null;
            }
            
            hitCount.incrementAndGet();
            return entry.value;
        }
        
        private void evictOldest() {
            cache.entrySet().stream()
                .min(Comparator.comparingLong(e -> e.getValue().expiryTime))
                .ifPresent(e -> cache.remove(e.getKey()));
        }
        
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("size", cache.size());
            stats.put("maxSize", maxSize);
            stats.put("ttl", ttlSeconds);
            stats.put("enabled", enabled);
            stats.put("hits", hitCount.get());
            stats.put("misses", missCount.get());
            double hitRate = (hitCount.get() + missCount.get()) == 0 ? 0 :
                (double) hitCount.get() / (hitCount.get() + missCount.get()) * 100;
            stats.put("hitRate", String.format("%.2f%%", hitRate));
            return stats;
        }
        
        private static class CacheEntry {
            final Object value;
            final long expiryTime;
            
            CacheEntry(Object value, long expiryTime) {
                this.value = value;
                this.expiryTime = expiryTime;
            }
            
            boolean isExpired() {
                return System.currentTimeMillis() > expiryTime;
            }
        }
    }

    // ============================================
    // Example 5: Circuit Breaker Threshold Refresh
    // ============================================
    
    /**
     * Update circuit breaker thresholds without restart.
     * Allows tuning failure detection dynamically.
     */
    @Service
    @RefreshScope
    public static class CircuitBreakerConfig {
        
        @Value("${app.circuit-breaker.failure-threshold:50}")
        private int failureThresholdPercentage;
        
        @Value("${app.circuit-breaker.wait-duration:5000}")
        private int waitDurationMillis;
        
        @Value("${app.circuit-breaker.sliding-window-size:100}")
        private int slidingWindowSize;
        
        @Value("${app.circuit-breaker.minimum-calls:10}")
        private int minimumNumberOfCalls;
        
        private CircuitState currentState = CircuitState.CLOSED;
        private int failureCount = 0;
        private int successCount = 0;
        private long lastStateChangeTime = System.currentTimeMillis();
        
        @PostConstruct
        public void init() {
            System.out.println("CircuitBreakerConfig initialized:");
            System.out.println("  Failure Threshold: " + failureThresholdPercentage + "%");
            System.out.println("  Wait Duration: " + waitDurationMillis + "ms");
            System.out.println("  Sliding Window Size: " + slidingWindowSize);
            System.out.println("  Minimum Calls: " + minimumNumberOfCalls);
            resetState();
        }
        
        public void recordSuccess() {
            successCount++;
            evaluateState();
        }
        
        public void recordFailure() {
            failureCount++;
            evaluateState();
        }
        
        private void evaluateState() {
            int totalCalls = successCount + failureCount;
            
            if (totalCalls < minimumNumberOfCalls) {
                return;
            }
            
            double failureRate = (double) failureCount / totalCalls * 100;
            
            if (currentState == CircuitState.CLOSED && failureRate >= failureThresholdPercentage) {
                transitionTo(CircuitState.OPEN);
            } else if (currentState == CircuitState.OPEN && 
                      System.currentTimeMillis() - lastStateChangeTime >= waitDurationMillis) {
                transitionTo(CircuitState.HALF_OPEN);
            } else if (currentState == CircuitState.HALF_OPEN && failureRate < failureThresholdPercentage) {
                transitionTo(CircuitState.CLOSED);
            }
        }
        
        private void transitionTo(CircuitState newState) {
            System.out.println("Circuit Breaker transition: " + currentState + " -> " + newState);
            currentState = newState;
            lastStateChangeTime = System.currentTimeMillis();
            if (newState == CircuitState.CLOSED) {
                resetState();
            }
        }
        
        private void resetState() {
            failureCount = 0;
            successCount = 0;
        }
        
        public CircuitState getCurrentState() {
            return currentState;
        }
        
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("state", currentState);
            stats.put("failureThreshold", failureThresholdPercentage);
            stats.put("successCount", successCount);
            stats.put("failureCount", failureCount);
            int total = successCount + failureCount;
            stats.put("failureRate", total == 0 ? 0 : (double) failureCount / total * 100);
            return stats;
        }
        
        enum CircuitState {
            CLOSED, OPEN, HALF_OPEN
        }
    }

    // ============================================
    // Example 6: API Client Configuration Refresh
    // ============================================
    
    /**
     * Update external API client settings.
     * Timeout, retry, base URL can change without restart.
     */
    @Service
    @RefreshScope
    public static class ApiClientConfig {
        
        @Value("${app.api.base-url:http://localhost:8080}")
        private String baseUrl;
        
        @Value("${app.api.timeout:3000}")
        private int timeoutMillis;
        
        @Value("${app.api.max-retries:3}")
        private int maxRetries;
        
        @Value("${app.api.retry-delay:1000}")
        private int retryDelayMillis;
        
        @Value("${app.api.api-key:}")
        private String apiKey;
        
        private int requestCount = 0;
        
        @PostConstruct
        public void init() {
            System.out.println("ApiClientConfig initialized:");
            System.out.println("  Base URL: " + baseUrl);
            System.out.println("  Timeout: " + timeoutMillis + "ms");
            System.out.println("  Max Retries: " + maxRetries);
            System.out.println("  Retry Delay: " + retryDelayMillis + "ms");
            System.out.println("  API Key: " + (apiKey.isEmpty() ? "Not set" : "***"));
        }
        
        public String makeRequest(String endpoint) {
            requestCount++;
            System.out.println("Making request to: " + baseUrl + endpoint);
            System.out.println("  Timeout: " + timeoutMillis + "ms");
            System.out.println("  Request #" + requestCount);
            return "Response from " + endpoint;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public int getTimeoutMillis() {
            return timeoutMillis;
        }
        
        public int getMaxRetries() {
            return maxRetries;
        }
    }

    // ============================================
    // Example 7: Thread Pool Configuration Refresh
    // ============================================
    
    /**
     * Update thread pool size dynamically.
     * Useful for scaling worker threads based on load.
     */
    @Configuration
    @RefreshScope
    public static class ThreadPoolConfig {
        
        @Value("${app.thread-pool.core-size:10}")
        private int corePoolSize;
        
        @Value("${app.thread-pool.max-size:20}")
        private int maxPoolSize;
        
        @Value("${app.thread-pool.queue-capacity:100}")
        private int queueCapacity;
        
        @Value("${app.thread-pool.keep-alive:60}")
        private int keepAliveSeconds;
        
        private final String poolId = UUID.randomUUID().toString().substring(0, 8);
        
        @PostConstruct
        public void init() {
            System.out.println("ThreadPoolConfig initialized (ID: " + poolId + "):");
            System.out.println("  Core Size: " + corePoolSize);
            System.out.println("  Max Size: " + maxPoolSize);
            System.out.println("  Queue Capacity: " + queueCapacity);
            System.out.println("  Keep Alive: " + keepAliveSeconds + "s");
        }
        
        @PreDestroy
        public void shutdown() {
            System.out.println("ThreadPoolConfig shutdown (ID: " + poolId + ")");
        }
        
        public int getCorePoolSize() {
            return corePoolSize;
        }
        
        public int getMaxPoolSize() {
            return maxPoolSize;
        }
        
        public int getQueueCapacity() {
            return queueCapacity;
        }
    }

    // ============================================
    // Example 8: Refresh Controller
    // ============================================
    
    /**
     * REST endpoints to view and trigger refresh.
     */
    @RestController
    public static class RefreshController {
        
        private final BasicRefreshableService basicService;
        private final FeatureToggleService featureService;
        private final DatabaseConfig databaseConfig;
        private final CacheConfig cacheConfig;
        private final CircuitBreakerConfig circuitBreakerConfig;
        private final ApiClientConfig apiClientConfig;
        private final ThreadPoolConfig threadPoolConfig;
        private final ContextRefresher contextRefresher;
        
        public RefreshController(
                BasicRefreshableService basicService,
                FeatureToggleService featureService,
                DatabaseConfig databaseConfig,
                CacheConfig cacheConfig,
                CircuitBreakerConfig circuitBreakerConfig,
                ApiClientConfig apiClientConfig,
                ThreadPoolConfig threadPoolConfig,
                ContextRefresher contextRefresher) {
            this.basicService = basicService;
            this.featureService = featureService;
            this.databaseConfig = databaseConfig;
            this.cacheConfig = cacheConfig;
            this.circuitBreakerConfig = circuitBreakerConfig;
            this.apiClientConfig = apiClientConfig;
            this.threadPoolConfig = threadPoolConfig;
            this.contextRefresher = contextRefresher;
        }
        
        @GetMapping("/config/basic")
        public String getBasicConfig() {
            return basicService.getMessage();
        }
        
        @GetMapping("/config/features")
        public Map<String, Boolean> getFeatures() {
            return featureService.getAllFeatures();
        }
        
        @GetMapping("/config/database")
        public Map<String, Object> getDatabaseConfig() {
            Map<String, Object> config = new HashMap<>();
            config.put("maxConnections", databaseConfig.getMaxConnections());
            config.put("activeConnections", databaseConfig.getActiveConnectionCount());
            return config;
        }
        
        @GetMapping("/config/cache")
        public Map<String, Object> getCacheStats() {
            return cacheConfig.getStats();
        }
        
        @GetMapping("/config/circuit-breaker")
        public Map<String, Object> getCircuitBreakerConfig() {
            return circuitBreakerConfig.getStats();
        }
        
        @GetMapping("/config/api-client")
        public Map<String, Object> getApiClientConfig() {
            Map<String, Object> config = new HashMap<>();
            config.put("baseUrl", apiClientConfig.getBaseUrl());
            config.put("timeout", apiClientConfig.getTimeoutMillis());
            config.put("maxRetries", apiClientConfig.getMaxRetries());
            return config;
        }
        
        @GetMapping("/config/thread-pool")
        public Map<String, Object> getThreadPoolConfig() {
            Map<String, Object> config = new HashMap<>();
            config.put("corePoolSize", threadPoolConfig.getCorePoolSize());
            config.put("maxPoolSize", threadPoolConfig.getMaxPoolSize());
            config.put("queueCapacity", threadPoolConfig.getQueueCapacity());
            return config;
        }
        
        @PostMapping("/config/refresh-manual")
        public Map<String, Object> refreshConfig() {
            System.out.println("Manual refresh triggered...");
            Set<String> refreshedKeys = contextRefresher.refresh();
            
            Map<String, Object> result = new HashMap<>();
            result.put("refreshedKeys", refreshedKeys);
            result.put("count", refreshedKeys.size());
            result.put("timestamp", LocalDateTime.now());
            
            return result;
        }
    }
}
