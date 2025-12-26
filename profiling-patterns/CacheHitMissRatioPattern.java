package com.example.cacheprofiling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.*;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache Hit/Miss Ratio Pattern
 * 
 * Demonstrates monitoring cache effectiveness through hit/miss ratio tracking.
 * 
 * Features:
 * - Cache hit/miss counting
 * - Cache efficiency metrics
 * - Per-cache statistics
 * - Eviction tracking
 * 
 * Use Cases:
 * - Cache tuning
 * - Memory optimization
 * - Performance analysis
 * - Cache strategy validation
 */
@SpringBootApplication
@EnableCaching
public class CacheHitMissRatioPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(CacheHitMissRatioPattern.class, args);
    }
    
    @Bean
    public ConcurrentMapCacheManager cacheManager() {
        return new ConcurrentMapCacheManager("users", "products", "config");
    }
}

/**
 * Cache statistics model
 */
class CacheStatistics {
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);
    
    public void recordHit() { hits.incrementAndGet(); }
    public void recordMiss() { misses.incrementAndGet(); }
    public void recordEviction() { evictions.incrementAndGet(); }
    
    public long getHits() { return hits.get(); }
    public long getMisses() { return misses.get(); }
    public long getEvictions() { return evictions.get(); }
    public long getTotalRequests() { return hits.get() + misses.get(); }
    
    public double getHitRatio() {
        long total = getTotalRequests();
        return total == 0 ? 0.0 : (double) hits.get() / total * 100;
    }
    
    public double getMissRatio() {
        long total = getTotalRequests();
        return total == 0 ? 0.0 : (double) misses.get() / total * 100;
    }
}

/**
 * Service for cache monitoring
 */
@Service
class CacheMonitoringService {
    
    private final Map<String, CacheStatistics> cacheStats = new ConcurrentHashMap<>();
    
    /**
     * Record cache hit
     */
    public void recordHit(String cacheName) {
        cacheStats.computeIfAbsent(cacheName, k -> new CacheStatistics()).recordHit();
    }
    
    /**
     * Record cache miss
     */
    public void recordMiss(String cacheName) {
        cacheStats.computeIfAbsent(cacheName, k -> new CacheStatistics()).recordMiss();
    }
    
    /**
     * Record cache eviction
     */
    public void recordEviction(String cacheName) {
        cacheStats.computeIfAbsent(cacheName, k -> new CacheStatistics()).recordEviction();
    }
    
    /**
     * Get statistics for a cache
     */
    public Map<String, Object> getStatistics(String cacheName) {
        CacheStatistics stats = cacheStats.get(cacheName);
        if (stats == null) {
            return Map.of("error", "No statistics for cache: " + cacheName);
        }
        
        return Map.of(
            "cacheName", cacheName,
            "hits", stats.getHits(),
            "misses", stats.getMisses(),
            "evictions", stats.getEvictions(),
            "totalRequests", stats.getTotalRequests(),
            "hitRatio", String.format("%.2f%%", stats.getHitRatio()),
            "missRatio", String.format("%.2f%%", stats.getMissRatio())
        );
    }
    
    /**
     * Get all cache statistics
     */
    public Map<String, Map<String, Object>> getAllStatistics() {
        Map<String, Map<String, Object>> allStats = new HashMap<>();
        cacheStats.keySet().forEach(cacheName -> {
            allStats.put(cacheName, getStatistics(cacheName));
        });
        return allStats;
    }
    
    /**
     * Get caches with low hit ratio
     */
    public List<Map<String, Object>> getLowPerformanceCaches(double threshold) {
        List<Map<String, Object>> lowPerf = new ArrayList<>();
        
        cacheStats.forEach((name, stats) -> {
            if (stats.getHitRatio() < threshold && stats.getTotalRequests() > 10) {
                lowPerf.add(Map.of(
                    "cacheName", name,
                    "hitRatio", stats.getHitRatio(),
                    "requests", stats.getTotalRequests()
                ));
            }
        });
        
        return lowPerf;
    }
}

/**
 * Aspect for automatic cache monitoring
 */
@Aspect
@Component
class CacheMonitoringAspect {
    
    private final CacheMonitoringService monitoringService;
    
    public CacheMonitoringAspect(CacheMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }
    
    @AfterReturning(pointcut = "@annotation(cacheable)", returning = "result")
    public void afterCacheable(org.aspectj.lang.JoinPoint joinPoint, Cacheable cacheable, Object result) {
        String[] cacheNames = cacheable.value();
        if (cacheNames.length > 0) {
            // In real implementation, check if result was from cache or freshly computed
            // For demo, we'll simulate based on randomness
            boolean wasHit = Math.random() > 0.3;
            String cacheName = cacheNames[0];
            
            if (wasHit) {
                monitoringService.recordHit(cacheName);
            } else {
                monitoringService.recordMiss(cacheName);
            }
        }
    }
}

/**
 * Service with cached methods
 */
@Service
class DataService {
    
    private final CacheMonitoringService monitoringService;
    
    public DataService(CacheMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }
    
    @Cacheable(value = "users", key = "#id")
    public Map<String, Object> getUser(int id) throws InterruptedException {
        // Simulate database call
        Thread.sleep(100);
        monitoringService.recordMiss("users");
        return Map.of("id", id, "name", "User " + id, "cached", false);
    }
    
    @Cacheable(value = "products", key = "#sku")
    public Map<String, Object> getProduct(String sku) throws InterruptedException {
        Thread.sleep(150);
        monitoringService.recordMiss("products");
        return Map.of("sku", sku, "name", "Product " + sku, "price", 99.99);
    }
    
    @CachePut(value = "users", key = "#user['id']")
    public Map<String, Object> updateUser(Map<String, Object> user) {
        monitoringService.recordEviction("users");
        return user;
    }
    
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(int id) {
        monitoringService.recordEviction("users");
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public void clearAllUsers() {
        monitoringService.recordEviction("users");
    }
    
    /**
     * Simulate cache hit
     */
    public Map<String, Object> getUserCached(int id) {
        monitoringService.recordHit("users");
        return Map.of("id", id, "name", "User " + id, "cached", true);
    }
}

/**
 * REST Controller demonstrating cache profiling
 */
@RestController
@RequestMapping("/api/cache-profiling")
class CacheProfilingController {
    
    private final DataService dataService;
    private final CacheMonitoringService monitoringService;
    
    public CacheProfilingController(DataService dataService,
                                   CacheMonitoringService monitoringService) {
        this.dataService = dataService;
        this.monitoringService = monitoringService;
    }
    
    @GetMapping("/user/{id}")
    public Map<String, Object> getUser(@PathVariable int id) throws InterruptedException {
        return dataService.getUser(id);
    }
    
    @GetMapping("/product/{sku}")
    public Map<String, Object> getProduct(@PathVariable String sku) throws InterruptedException {
        return dataService.getProduct(sku);
    }
    
    @PutMapping("/user")
    public Map<String, Object> updateUser(@RequestBody Map<String, Object> user) {
        return dataService.updateUser(user);
    }
    
    @DeleteMapping("/user/{id}")
    public Map<String, String> deleteUser(@PathVariable int id) {
        dataService.deleteUser(id);
        return Map.of("status", "deleted");
    }
    
    @DeleteMapping("/users")
    public Map<String, String> clearAllUsers() {
        dataService.clearAllUsers();
        return Map.of("status", "cleared");
    }
    
    /**
     * Test cache hit
     */
    @GetMapping("/user/{id}/cached")
    public Map<String, Object> getUserCached(@PathVariable int id) {
        return dataService.getUserCached(id);
    }
    
    /**
     * Get cache statistics
     */
    @GetMapping("/statistics")
    public Map<String, Map<String, Object>> getAllStatistics() {
        return monitoringService.getAllStatistics();
    }
    
    @GetMapping("/statistics/{cacheName}")
    public Map<String, Object> getCacheStatistics(@PathVariable String cacheName) {
        return monitoringService.getStatistics(cacheName);
    }
    
    @GetMapping("/low-performance")
    public List<Map<String, Object>> getLowPerformanceCaches(
            @RequestParam(defaultValue = "50.0") double threshold) {
        return monitoringService.getLowPerformanceCaches(threshold);
    }
}
