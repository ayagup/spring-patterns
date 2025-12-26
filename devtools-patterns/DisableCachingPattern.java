package com.example.devtools.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 🚫 SPRING BOOT DEVTOOLS - DISABLE CACHING PATTERN 🚫
 * =====================================================
 * 
 * Demonstrates how Spring Boot DevTools automatically disables various
 * caching mechanisms during development to provide immediate feedback
 * on code changes. Understanding when and how caching is disabled helps
 * optimize the development workflow.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ TEMPLATE ENGINE CACHE DISABLING:
 *    - Thymeleaf: spring.thymeleaf.cache=false
 *    - FreeMarker: spring.freemarker.cache=false
 *    - Groovy Templates: spring.groovy.template.cache=false
 *    - Mustache: spring.mustache.cache=false
 *    - Immediate template updates without restart
 * 
 * 2️⃣ STATIC RESOURCE CACHE DISABLING:
 *    - spring.web.resources.cache.cachecontrol.no-cache=true
 *    - spring.web.resources.chain.cache=false
 *    - CSS/JS changes reflected immediately
 *    - No browser hard refresh needed with LiveReload
 * 
 * 3️⃣ APPLICATION-LEVEL CACHE CONTROL:
 *    - Spring @Cacheable still works
 *    - Can disable programmatically for development
 *    - Configure cache-specific behavior
 * 
 * 4️⃣ PRODUCTION VS DEVELOPMENT:
 *    - Development: All caches disabled for fast feedback
 *    - Production: All caches enabled for performance
 *    - Profile-based configuration
 * 
 * 📦 DEPENDENCIES:
 * ===============
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-devtools</artifactId>
 *     <optional>true</optional>
 *     <scope>runtime</scope>
 * </dependency>
 * 
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-cache</artifactId>
 * </dependency>
 * 
 * ⚙️ CONFIGURATION (application.yml):
 * ===================================
 * 
 * # Development Profile (DevTools active)
 * spring:
 *   profiles:
 *     active: dev
 *   
 *   # Template caching (auto-disabled by DevTools)
 *   thymeleaf:
 *     cache: false      # DevTools default
 *   
 *   freemarker:
 *     cache: false      # DevTools default
 *   
 *   # Static resources (auto-disabled by DevTools)
 *   web:
 *     resources:
 *       cache:
 *         cachecontrol:
 *           no-cache: true    # DevTools default
 *       chain:
 *         cache: false         # DevTools default
 *   
 *   # Application caching (manual control)
 *   cache:
 *     type: simple
 *     cache-names:
 *       - users
 *       - products
 * 
 * # Production Profile
 * ---
 * spring:
 *   config:
 *     activate:
 *       on-profile: prod
 *   
 *   # Enable all caches in production
 *   thymeleaf:
 *     cache: true
 *   
 *   web:
 *     resources:
 *       cache:
 *         cachecontrol:
 *           max-age: 365d
 *           cache-public: true
 *       chain:
 *         cache: true
 *   
 *   cache:
 *     type: redis  # Or caffeine, ehcache, etc.
 * 
 * 🔄 CACHE TYPES AFFECTED BY DEVTOOLS:
 * ====================================
 * 
 * 1. Template Engine Caches (AUTO-DISABLED):
 *    ✅ Thymeleaf template cache
 *    ✅ FreeMarker template cache
 *    ✅ Groovy template cache
 *    ✅ Mustache template cache
 * 
 * 2. Static Resource Caches (AUTO-DISABLED):
 *    ✅ Browser cache headers (no-cache)
 *    ✅ Resource chain cache
 *    ✅ WebJars cache
 * 
 * 3. Application Caches (MANUAL CONTROL):
 *    ⚠️ @Cacheable annotations still work
 *    ⚠️ CacheManager beans still active
 *    ⚠️ Must disable manually if needed
 * 
 * 💡 BENEFITS:
 * ===========
 * ✅ See template changes immediately
 * ✅ CSS/JS updates without hard refresh
 * ✅ No manual cache clearing
 * ✅ Faster development iteration
 * ✅ Automatic in development (no config needed)
 * 
 * ⚠️ CAVEATS:
 * ==========
 * ⚠️ Performance impact (acceptable in development)
 * ⚠️ Not representative of production behavior
 * ⚠️ May miss production caching issues
 * ⚠️ Application-level caching still active
 * ⚠️ External caches (Redis, Memcached) unaffected
 * 
 * 💡 WHEN TO USE:
 * ==============
 * ✅ Local development (always with DevTools)
 * ✅ Template development (Thymeleaf, FreeMarker)
 * ✅ Frontend development (CSS, JS, images)
 * ✅ Rapid prototyping
 * ✅ UI/UX iteration
 * 
 * ❌ WHEN NOT TO USE:
 * ==================
 * ❌ Production environments
 * ❌ Performance testing
 * ❌ Load testing
 * ❌ Cache behavior testing
 * ❌ When testing production cache configuration
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
@EnableCaching
public class DisableCachingPattern {

    public static void main(String[] args) {
        SpringApplication.run(DisableCachingPattern.class, args);
    }
}

/**
 * Cache Configuration
 * Demonstrates conditional cache enabling/disabling
 */
@Configuration
class CacheConfiguration {

    /**
     * Development Cache Manager (Simple in-memory)
     */
    @Bean
    @Profile("dev")
    public CacheManager devCacheManager() {
        System.out.println("🔧 Development Cache Manager initialized (in-memory)");
        return new ConcurrentMapCacheManager("users", "products", "orders");
    }

    /**
     * Production Cache Manager (would be Redis, Caffeine, etc.)
     */
    @Bean
    @Profile("prod")
    public CacheManager prodCacheManager() {
        System.out.println("🚀 Production Cache Manager initialized");
        return new ConcurrentMapCacheManager("users", "products", "orders");
    }

    /**
     * Static Resource Configuration
     */
    @Bean
    public WebMvcConfigurer resourceConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Note: DevTools automatically sets no-cache for static resources
                registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/");
            }
        };
    }
}

/**
 * Cache Monitoring Service
 * Tracks cache hits/misses and provides statistics
 */
@Service
class CacheMonitoringService {

    private final Map<String, CacheStatistics> statistics = new ConcurrentHashMap<>();

    public void recordCacheHit(String cacheName, String key) {
        statistics.computeIfAbsent(cacheName, k -> new CacheStatistics())
            .recordHit();
        System.out.println("✅ Cache HIT: " + cacheName + " [" + key + "]");
    }

    public void recordCacheMiss(String cacheName, String key) {
        statistics.computeIfAbsent(cacheName, k -> new CacheStatistics())
            .recordMiss();
        System.out.println("❌ Cache MISS: " + cacheName + " [" + key + "]");
    }

    public void recordCacheEviction(String cacheName, String key) {
        statistics.computeIfAbsent(cacheName, k -> new CacheStatistics())
            .recordEviction();
        System.out.println("🗑️ Cache EVICT: " + cacheName + " [" + key + "]");
    }

    public Map<String, CacheStatistics> getAllStatistics() {
        return new ConcurrentHashMap<>(statistics);
    }

    public CacheStatistics getStatistics(String cacheName) {
        return statistics.getOrDefault(cacheName, new CacheStatistics());
    }

    public void reset() {
        statistics.clear();
    }
}

/**
 * Cache Statistics
 */
class CacheStatistics {
    private final AtomicInteger hits = new AtomicInteger(0);
    private final AtomicInteger misses = new AtomicInteger(0);
    private final AtomicInteger evictions = new AtomicInteger(0);
    private final LocalDateTime createdAt = LocalDateTime.now();

    public void recordHit() {
        hits.incrementAndGet();
    }

    public void recordMiss() {
        misses.incrementAndGet();
    }

    public void recordEviction() {
        evictions.incrementAndGet();
    }

    public int getHits() { return hits.get(); }
    public int getMisses() { return misses.get(); }
    public int getEvictions() { return evictions.get(); }
    public int getTotal() { return hits.get() + misses.get(); }
    
    public double getHitRate() {
        int total = getTotal();
        return total > 0 ? (double) hits.get() / total * 100 : 0.0;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Duration getAge() {
        return Duration.between(createdAt, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return String.format("CacheStats{hits=%d, misses=%d, evictions=%d, hitRate=%.2f%%, age=%ds}",
            hits.get(), misses.get(), evictions.get(), getHitRate(), getAge().getSeconds());
    }
}

/**
 * User Service with Caching
 * Demonstrates @Cacheable, @CachePut, @CacheEvict
 */
@Service
class UserCacheService {

    private final Map<Long, User> database = new ConcurrentHashMap<>();
    private final CacheMonitoringService monitoring;
    private final AtomicInteger dbCallCount = new AtomicInteger(0);

    public UserCacheService(CacheMonitoringService monitoring) {
        this.monitoring = monitoring;
        // Initialize test data
        database.put(1L, new User(1L, "Alice", "alice@example.com"));
        database.put(2L, new User(2L, "Bob", "bob@example.com"));
        database.put(3L, new User(3L, "Charlie", "charlie@example.com"));
    }

    /**
     * Cacheable method - results cached
     * In development with DevTools: Still caches (DevTools doesn't disable @Cacheable)
     */
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        dbCallCount.incrementAndGet();
        monitoring.recordCacheMiss("users", "user:" + id);
        
        System.out.println("🔍 Fetching user from DATABASE (not cache): " + id);
        simulateDatabaseDelay();
        
        return database.get(id);
    }

    /**
     * CachePut - updates cache
     */
    @CachePut(value = "users", key = "#user.id")
    public User updateUser(User user) {
        dbCallCount.incrementAndGet();
        monitoring.recordCacheEviction("users", "user:" + user.getId());
        
        System.out.println("💾 Updating user in DATABASE and CACHE: " + user.getId());
        database.put(user.getId(), user);
        
        return user;
    }

    /**
     * CacheEvict - removes from cache
     */
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        dbCallCount.incrementAndGet();
        monitoring.recordCacheEviction("users", "user:" + id);
        
        System.out.println("🗑️ Deleting user from DATABASE and CACHE: " + id);
        database.remove(id);
    }

    /**
     * CacheEvict with allEntries - clears entire cache
     */
    @CacheEvict(value = "users", allEntries = true)
    public void clearCache() {
        System.out.println("🧹 Clearing ALL user cache entries");
    }

    /**
     * Get database call count (measures cache effectiveness)
     */
    public int getDatabaseCallCount() {
        return dbCallCount.get();
    }

    public void resetDatabaseCallCount() {
        dbCallCount.set(0);
    }

    private void simulateDatabaseDelay() {
        try {
            Thread.sleep(100); // Simulate DB query latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * User Entity
 */
class User {
    private Long id;
    private String name;
    private String email;

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s'}", id, name, email);
    }
}

/**
 * Cache Configuration Info Service
 * Provides information about cache configuration
 */
@Service
class CacheConfigInfoService {

    /**
     * Get template cache status
     */
    public Map<String, Object> getTemplateCacheStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        
        status.put("thymeleaf", Map.of(
            "property", "spring.thymeleaf.cache",
            "devToolsDefault", "false (DISABLED)",
            "productionDefault", "true (ENABLED)",
            "impact", "Template changes visible immediately in dev"
        ));
        
        status.put("freemarker", Map.of(
            "property", "spring.freemarker.cache",
            "devToolsDefault", "false (DISABLED)",
            "impact", "FreeMarker templates reload automatically"
        ));
        
        return status;
    }

    /**
     * Get static resource cache status
     */
    public Map<String, Object> getStaticResourceCacheStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        
        status.put("resourceChain", Map.of(
            "property", "spring.web.resources.chain.cache",
            "devToolsDefault", "false (DISABLED)",
            "impact", "CSS/JS changes reflected immediately"
        ));
        
        status.put("cacheControl", Map.of(
            "property", "spring.web.resources.cache.cachecontrol.no-cache",
            "devToolsDefault", "true (NO CACHE)",
            "impact", "Browser doesn't cache resources during development"
        ));
        
        return status;
    }

    /**
     * Get application cache status
     */
    public Map<String, Object> getApplicationCacheStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        
        status.put("note", "@Cacheable annotations still work with DevTools");
        status.put("cacheManager", "Active (not disabled by DevTools)");
        status.put("recommendation", "Disable manually if needed for testing");
        
        return status;
    }

    /**
     * Get cache comparison (Dev vs Prod)
     */
    public Map<String, Object> getCacheComparison() {
        Map<String, Object> comparison = new LinkedHashMap<>();
        
        Map<String, String> dev = new LinkedHashMap<>();
        dev.put("Templates", "❌ Disabled (fast feedback)");
        dev.put("Static Resources", "❌ Disabled (LiveReload)");
        dev.put("Application Cache", "✅ Enabled (still works)");
        dev.put("Performance", "⚠️ Slower (acceptable in dev)");
        
        Map<String, String> prod = new LinkedHashMap<>();
        prod.put("Templates", "✅ Enabled (performance)");
        prod.put("Static Resources", "✅ Enabled (CDN, long TTL)");
        prod.put("Application Cache", "✅ Enabled (Redis, etc.)");
        prod.put("Performance", "🚀 Fast (all caches active)");
        
        comparison.put("Development", dev);
        comparison.put("Production", prod);
        
        return comparison;
    }
}

/**
 * Disable Caching REST Controller
 */
@RestController
@RequestMapping("/api/caching")
class DisableCachingController {

    private final CacheManager cacheManager;
    private final UserCacheService userCacheService;
    private final CacheMonitoringService monitoringService;
    private final CacheConfigInfoService configInfoService;

    public DisableCachingController(CacheManager cacheManager,
                                    UserCacheService userCacheService,
                                    CacheMonitoringService monitoringService,
                                    CacheConfigInfoService configInfoService) {
        this.cacheManager = cacheManager;
        this.userCacheService = userCacheService;
        this.monitoringService = monitoringService;
        this.configInfoService = configInfoService;
    }

    /**
     * GET /api/caching/users/{id}
     * Get user (cached)
     */
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        User user = userCacheService.getUserById(id);
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("⏱️ Request duration: " + duration + "ms");
        return user;
    }

    /**
     * PUT /api/caching/users/{id}
     * Update user (cache updated)
     */
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userCacheService.updateUser(user);
    }

    /**
     * DELETE /api/caching/users/{id}
     * Delete user (cache evicted)
     */
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        userCacheService.deleteUser(id);
        return "User " + id + " deleted";
    }

    /**
     * DELETE /api/caching/users
     * Clear user cache
     */
    @DeleteMapping("/users")
    public String clearCache() {
        userCacheService.clearCache();
        return "User cache cleared";
    }

    /**
     * GET /api/caching/statistics
     * Get cache statistics
     */
    @GetMapping("/statistics")
    public Map<String, CacheStatistics> getStatistics() {
        return monitoringService.getAllStatistics();
    }

    /**
     * GET /api/caching/db-calls
     * Get database call count
     */
    @GetMapping("/db-calls")
    public Map<String, Integer> getDatabaseCalls() {
        return Map.of("databaseCalls", userCacheService.getDatabaseCallCount());
    }

    /**
     * DELETE /api/caching/statistics
     * Reset statistics
     */
    @DeleteMapping("/statistics")
    public String resetStatistics() {
        monitoringService.reset();
        userCacheService.resetDatabaseCallCount();
        return "Statistics reset";
    }

    /**
     * GET /api/caching/config/templates
     * Get template cache configuration
     */
    @GetMapping("/config/templates")
    public Map<String, Object> getTemplateCacheConfig() {
        return configInfoService.getTemplateCacheStatus();
    }

    /**
     * GET /api/caching/config/static-resources
     * Get static resource cache configuration
     */
    @GetMapping("/config/static-resources")
    public Map<String, Object> getStaticResourceCacheConfig() {
        return configInfoService.getStaticResourceCacheStatus();
    }

    /**
     * GET /api/caching/config/application
     * Get application cache configuration
     */
    @GetMapping("/config/application")
    public Map<String, Object> getApplicationCacheConfig() {
        return configInfoService.getApplicationCacheStatus();
    }

    /**
     * GET /api/caching/comparison
     * Get dev vs prod cache comparison
     */
    @GetMapping("/comparison")
    public Map<String, Object> getCacheComparison() {
        return configInfoService.getCacheComparison();
    }

    /**
     * GET /api/caching/names
     * Get all cache names
     */
    @GetMapping("/names")
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ BASIC SETUP (Caching disabled by DevTools):
 * ------------------------------------------------
 * # Just add DevTools dependency
 * # Templates and static resources automatically uncached!
 * 
 * 2️⃣ TEST CACHE BEHAVIOR:
 * ------------------------
 * # First call (cache miss, DB query)
 * curl http://localhost:8080/api/caching/users/1
 * # Duration: ~100ms
 * 
 * # Second call (cache hit, no DB query)
 * curl http://localhost:8080/api/caching/users/1
 * # Duration: ~1ms
 * 
 * 3️⃣ GET CACHE STATISTICS:
 * -------------------------
 * curl http://localhost:8080/api/caching/statistics
 * curl http://localhost:8080/api/caching/db-calls
 * 
 * 4️⃣ EVICT CACHE:
 * ----------------
 * curl -X DELETE http://localhost:8080/api/caching/users/1
 * curl -X DELETE http://localhost:8080/api/caching/users  # Clear all
 * 
 * 5️⃣ GET CACHE CONFIGURATION INFO:
 * ---------------------------------
 * curl http://localhost:8080/api/caching/config/templates
 * curl http://localhost:8080/api/caching/config/static-resources
 * curl http://localhost:8080/api/caching/comparison
 * 
 * 6️⃣ DISABLE APPLICATION CACHING IN DEV:
 * ---------------------------------------
 * @Profile("dev")
 * @Bean
 * public CacheManager cacheManager() {
 *     return new NoOpCacheManager();  // Disables @Cacheable
 * }
 * 
 * 7️⃣ PRODUCTION CONFIGURATION:
 * -----------------------------
 * spring:
 *   thymeleaf:
 *     cache: true
 *   web:
 *     resources:
 *       cache:
 *         cachecontrol:
 *           max-age: 31536000  # 1 year
 *   cache:
 *     type: redis
 *     redis:
 *       time-to-live: 600000   # 10 minutes
 */
