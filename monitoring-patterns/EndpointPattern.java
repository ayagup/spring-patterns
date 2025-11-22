package com.example.monitoring.endpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Endpoint Pattern - Demonstrates Custom Spring Boot Actuator Endpoints
 * 
 * This pattern shows how to:
 * 1. Create custom actuator endpoints with @Endpoint
 * 2. Implement read operations with @ReadOperation
 * 3. Implement write operations with @WriteOperation
 * 4. Implement delete operations with @DeleteOperation
 * 5. Use @Selector for path parameters
 * 6. Create Web-specific endpoints with @WebEndpoint
 * 7. Create JMX-specific endpoints with @JmxEndpoint
 * 8. Return structured data from endpoints
 * 9. Handle endpoint security and exposure
 * 10. Create endpoint extensions with @EndpointExtension
 * 
 * Key Concepts:
 * - @Endpoint: Technology-agnostic custom endpoint
 * - @WebEndpoint: Web-only custom endpoint
 * - @JmxEndpoint: JMX-only custom endpoint
 * - @ReadOperation: HTTP GET / JMX read operation
 * - @WriteOperation: HTTP POST / JMX write operation
 * - @DeleteOperation: HTTP DELETE / JMX delete operation
 * - @Selector: Path variable or operation parameter
 * 
 * Configuration:
 * management.endpoints.web.exposure.include=*
 * management.endpoints.jmx.exposure.include=*
 * management.endpoint.features.enabled=true
 * 
 * Access:
 * - Web: http://localhost:8080/actuator/features
 * - JMX: service:jmx:rmi:///jndi/rmi://localhost:9875/jmxrmi
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class EndpointPattern {

    public static void main(String[] args) {
        SpringApplication.run(EndpointPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("CUSTOM ACTUATOR ENDPOINT PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateEndpointTypes();
        demonstrateEndpointOperations();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("AVAILABLE CUSTOM ENDPOINTS");
        System.out.println("=".repeat(80));
        System.out.println("\n1. Features Endpoint:");
        System.out.println("   GET    /actuator/features           - List all features");
        System.out.println("   GET    /actuator/features/{name}    - Get specific feature");
        System.out.println("   POST   /actuator/features           - Enable feature");
        System.out.println("   DELETE /actuator/features/{name}    - Disable feature");
        
        System.out.println("\n2. Cache Management Endpoint:");
        System.out.println("   GET    /actuator/cache              - Get cache statistics");
        System.out.println("   GET    /actuator/cache/{cacheName}  - Get specific cache");
        System.out.println("   POST   /actuator/cache/clear        - Clear all caches");
        System.out.println("   DELETE /actuator/cache/{cacheName}  - Clear specific cache");
        
        System.out.println("\n3. System Info Endpoint:");
        System.out.println("   GET    /actuator/systeminfo         - Get system information");
        System.out.println("   GET    /actuator/systeminfo/memory  - Get memory info");
        System.out.println("   GET    /actuator/systeminfo/cpu     - Get CPU info");
        
        System.out.println("\n4. Release Notes Endpoint:");
        System.out.println("   GET    /actuator/releasenotes       - Get all release notes");
        System.out.println("   GET    /actuator/releasenotes/{version} - Get specific version");
        
        System.out.println("\n5. Configuration Endpoint:");
        System.out.println("   GET    /actuator/appconfig          - Get all configuration");
        System.out.println("   POST   /actuator/appconfig          - Update configuration");
        
        System.out.println("\nApplication is running. Access endpoints at the URLs above.");
        System.out.println("Press Ctrl+C to stop.\n");
    }
    
    private static void demonstrateEndpointTypes() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ENDPOINT TYPES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. @Endpoint - Technology agnostic (Web + JMX)");
        System.out.println("   - Exposed via HTTP and JMX by default");
        System.out.println("   - Use for general-purpose endpoints");
        
        System.out.println("\n2. @WebEndpoint - Web only");
        System.out.println("   - Only exposed via HTTP");
        System.out.println("   - Use for web-specific functionality");
        
        System.out.println("\n3. @JmxEndpoint - JMX only");
        System.out.println("   - Only exposed via JMX");
        System.out.println("   - Use for JMX-specific operations");
    }
    
    private static void demonstrateEndpointOperations() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ENDPOINT OPERATIONS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n@ReadOperation - Read data (GET / JMX read)");
        System.out.println("  - Non-destructive operations");
        System.out.println("  - Return data without modifying state");
        
        System.out.println("\n@WriteOperation - Modify data (POST / JMX write)");
        System.out.println("  - Operations that modify state");
        System.out.println("  - Can accept parameters");
        
        System.out.println("\n@DeleteOperation - Remove data (DELETE / JMX delete)");
        System.out.println("  - Operations that remove resources");
        System.out.println("  - Typically use @Selector for targeting");
    }
}

/**
 * Custom endpoint for managing feature flags
 */
@Endpoint(id = "features")
@Component
class FeaturesEndpoint {
    
    private final Map<String, Feature> features = new ConcurrentHashMap<>();
    
    public FeaturesEndpoint() {
        // Initialize with sample features
        features.put("newUI", new Feature("newUI", "New User Interface", true));
        features.put("darkMode", new Feature("darkMode", "Dark Mode Theme", false));
        features.put("analytics", new Feature("analytics", "Advanced Analytics", true));
        features.put("notifications", new Feature("notifications", "Push Notifications", false));
    }
    
    /**
     * Get all features
     * GET /actuator/features
     */
    @ReadOperation
    public Map<String, Object> features() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("total", features.size());
        response.put("enabled", features.values().stream().filter(Feature::isEnabled).count());
        response.put("features", features.values());
        return response;
    }
    
    /**
     * Get a specific feature
     * GET /actuator/features/{name}
     */
    @ReadOperation
    public Feature feature(@Selector String name) {
        Feature feature = features.get(name);
        if (feature == null) {
            throw new IllegalArgumentException("Feature not found: " + name);
        }
        return feature;
    }
    
    /**
     * Enable/create a feature
     * POST /actuator/features
     */
    @WriteOperation
    public Map<String, Object> enableFeature(String name, String description, boolean enabled) {
        Feature feature = new Feature(name, description, enabled);
        features.put(name, feature);
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", "enabled");
        response.put("feature", feature);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Disable/remove a feature
     * DELETE /actuator/features/{name}
     */
    @DeleteOperation
    public Map<String, Object> disableFeature(@Selector String name) {
        Feature feature = features.remove(name);
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", "disabled");
        response.put("feature", feature != null ? feature : "not found");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}

/**
 * Feature flag model
 */
class Feature {
    private String name;
    private String description;
    private boolean enabled;
    private LocalDateTime lastModified;
    
    public Feature(String name, String description, boolean enabled) {
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.lastModified = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getLastModified() { return lastModified; }
}

/**
 * Custom endpoint for cache management
 */
@Endpoint(id = "cache")
@Component
class CacheManagementEndpoint {
    
    private final Map<String, CacheInfo> caches = new ConcurrentHashMap<>();
    
    public CacheManagementEndpoint() {
        // Initialize sample caches
        caches.put("userCache", new CacheInfo("userCache", 1000, 856, 0.856));
        caches.put("productCache", new CacheInfo("productCache", 500, 423, 0.846));
        caches.put("sessionCache", new CacheInfo("sessionCache", 2000, 1543, 0.772));
    }
    
    /**
     * Get all cache statistics
     * GET /actuator/cache
     */
    @ReadOperation
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCaches", caches.size());
        stats.put("totalCapacity", caches.values().stream()
            .mapToLong(CacheInfo::getMaxSize).sum());
        stats.put("totalEntries", caches.values().stream()
            .mapToLong(CacheInfo::getCurrentSize).sum());
        stats.put("averageHitRate", caches.values().stream()
            .mapToDouble(CacheInfo::getHitRate).average().orElse(0.0));
        stats.put("caches", caches);
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }
    
    /**
     * Get specific cache information
     * GET /actuator/cache/{cacheName}
     */
    @ReadOperation
    public CacheInfo getCacheInfo(@Selector String cacheName) {
        CacheInfo cacheInfo = caches.get(cacheName);
        if (cacheInfo == null) {
            throw new IllegalArgumentException("Cache not found: " + cacheName);
        }
        return cacheInfo;
    }
    
    /**
     * Clear all caches
     * POST /actuator/cache/clear
     */
    @WriteOperation
    public Map<String, Object> clearAllCaches() {
        int clearedCount = caches.size();
        caches.values().forEach(CacheInfo::clear);
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", "clearAll");
        response.put("clearedCaches", clearedCount);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Clear specific cache
     * DELETE /actuator/cache/{cacheName}
     */
    @DeleteOperation
    public Map<String, Object> clearCache(@Selector String cacheName) {
        CacheInfo cacheInfo = caches.get(cacheName);
        if (cacheInfo != null) {
            cacheInfo.clear();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", "clear");
        response.put("cacheName", cacheName);
        response.put("status", cacheInfo != null ? "cleared" : "not found");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}

/**
 * Cache information model
 */
class CacheInfo {
    private String name;
    private long maxSize;
    private long currentSize;
    private double hitRate;
    private LocalDateTime lastCleared;
    
    public CacheInfo(String name, long maxSize, long currentSize, double hitRate) {
        this.name = name;
        this.maxSize = maxSize;
        this.currentSize = currentSize;
        this.hitRate = hitRate;
        this.lastCleared = null;
    }
    
    public void clear() {
        this.currentSize = 0;
        this.lastCleared = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getName() { return name; }
    public long getMaxSize() { return maxSize; }
    public long getCurrentSize() { return currentSize; }
    public double getHitRate() { return hitRate; }
    public LocalDateTime getLastCleared() { return lastCleared; }
}

/**
 * Web-specific endpoint for system information
 */
@WebEndpoint(id = "systeminfo")
@Component
class SystemInfoEndpoint {
    
    /**
     * Get all system information
     * GET /actuator/systeminfo
     */
    @ReadOperation
    public Map<String, Object> systemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("os", getOsInfo());
        info.put("java", getJavaInfo());
        info.put("memory", getMemoryInfo());
        info.put("cpu", getCpuInfo());
        info.put("timestamp", LocalDateTime.now());
        return info;
    }
    
    /**
     * Get memory information
     * GET /actuator/systeminfo/memory
     */
    @ReadOperation
    public Map<String, Object> getMemoryInfo(@Selector String type) {
        if ("memory".equals(type)) {
            return getMemoryInfo();
        } else if ("cpu".equals(type)) {
            return getCpuInfo();
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }
    
    private Map<String, Object> getOsInfo() {
        Map<String, Object> os = new HashMap<>();
        os.put("name", System.getProperty("os.name"));
        os.put("version", System.getProperty("os.version"));
        os.put("arch", System.getProperty("os.arch"));
        return os;
    }
    
    private Map<String, Object> getJavaInfo() {
        Map<String, Object> java = new HashMap<>();
        java.put("version", System.getProperty("java.version"));
        java.put("vendor", System.getProperty("java.vendor"));
        java.put("home", System.getProperty("java.home"));
        return java;
    }
    
    private Map<String, Object> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("max", runtime.maxMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        return memory;
    }
    
    private Map<String, Object> getCpuInfo() {
        Map<String, Object> cpu = new HashMap<>();
        cpu.put("processors", Runtime.getRuntime().availableProcessors());
        cpu.put("loadAverage", getSystemLoadAverage());
        return cpu;
    }
    
    private double getSystemLoadAverage() {
        try {
            return java.lang.management.ManagementFactory
                .getOperatingSystemMXBean().getSystemLoadAverage();
        } catch (Exception e) {
            return -1.0;
        }
    }
}

/**
 * Custom endpoint for release notes
 */
@Endpoint(id = "releasenotes")
@Component
class ReleaseNotesEndpoint {
    
    private final Map<String, ReleaseNote> releaseNotes = new ConcurrentHashMap<>();
    
    public ReleaseNotesEndpoint() {
        releaseNotes.put("1.0.0", new ReleaseNote("1.0.0", 
            LocalDateTime.of(2024, 1, 15, 10, 0),
            Arrays.asList("Initial release", "User authentication", "Basic CRUD operations")));
        
        releaseNotes.put("1.1.0", new ReleaseNote("1.1.0",
            LocalDateTime.of(2024, 2, 20, 14, 30),
            Arrays.asList("Added dashboard", "Performance improvements", "Bug fixes")));
        
        releaseNotes.put("2.0.0", new ReleaseNote("2.0.0",
            LocalDateTime.of(2024, 3, 10, 9, 0),
            Arrays.asList("Complete UI redesign", "API v2", "Microservices architecture")));
    }
    
    /**
     * Get all release notes
     * GET /actuator/releasenotes
     */
    @ReadOperation
    public Map<String, Object> getAllReleaseNotes() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalReleases", releaseNotes.size());
        response.put("latestVersion", getLatestVersion());
        response.put("releases", releaseNotes.values().stream()
            .sorted((a, b) -> b.getReleaseDate().compareTo(a.getReleaseDate()))
            .collect(Collectors.toList()));
        return response;
    }
    
    /**
     * Get specific release note
     * GET /actuator/releasenotes/{version}
     */
    @ReadOperation
    public ReleaseNote getReleaseNote(@Selector String version) {
        ReleaseNote note = releaseNotes.get(version);
        if (note == null) {
            throw new IllegalArgumentException("Release note not found: " + version);
        }
        return note;
    }
    
    /**
     * Add new release note
     * POST /actuator/releasenotes
     */
    @WriteOperation
    public Map<String, Object> addReleaseNote(String version, List<String> features) {
        ReleaseNote note = new ReleaseNote(version, LocalDateTime.now(), features);
        releaseNotes.put(version, note);
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", "added");
        response.put("releaseNote", note);
        return response;
    }
    
    private String getLatestVersion() {
        return releaseNotes.values().stream()
            .max(Comparator.comparing(ReleaseNote::getReleaseDate))
            .map(ReleaseNote::getVersion)
            .orElse("unknown");
    }
}

/**
 * Release note model
 */
class ReleaseNote {
    private String version;
    private LocalDateTime releaseDate;
    private List<String> features;
    
    public ReleaseNote(String version, LocalDateTime releaseDate, List<String> features) {
        this.version = version;
        this.releaseDate = releaseDate;
        this.features = features;
    }
    
    // Getters
    public String getVersion() { return version; }
    public LocalDateTime getReleaseDate() { return releaseDate; }
    public List<String> getFeatures() { return features; }
}

/**
 * JMX-specific endpoint for configuration
 */
@JmxEndpoint(id = "appconfig")
@Component
class ConfigurationEndpoint {
    
    private final Map<String, String> configuration = new ConcurrentHashMap<>();
    
    public ConfigurationEndpoint() {
        configuration.put("app.name", "Spring Monitoring Demo");
        configuration.put("app.version", "2.0.0");
        configuration.put("app.environment", "development");
        configuration.put("app.debug", "true");
    }
    
    /**
     * Get all configuration
     */
    @ReadOperation
    public Map<String, Object> getAllConfiguration() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalProperties", configuration.size());
        response.put("properties", configuration);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Get specific configuration property
     */
    @ReadOperation
    public String getConfiguration(@Selector String key) {
        return configuration.getOrDefault(key, "Property not found");
    }
    
    /**
     * Update configuration
     */
    @WriteOperation
    public Map<String, Object> updateConfiguration(String key, String value) {
        String oldValue = configuration.put(key, value);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("oldValue", oldValue);
        response.put("newValue", value);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * Delete configuration property
     */
    @DeleteOperation
    public Map<String, Object> deleteConfiguration(@Selector String key) {
        String removedValue = configuration.remove(key);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("removedValue", removedValue);
        response.put("status", removedValue != null ? "deleted" : "not found");
        return response;
    }
}
