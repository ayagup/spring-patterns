package com.spring.patterns.scope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.annotation.ApplicationScope;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Application Scope Pattern
 * 
 * Application scope creates a single bean instance per ServletContext (web application).
 * Similar to singleton but specifically for web applications - shared across all sessions and requests.
 * 
 * Characteristics:
 * - One instance per ServletContext
 * - Shared across all sessions and requests
 * - Lifecycle tied to web application
 * - Destroyed when application stops
 * - Available only in web-aware ApplicationContext
 * 
 * Difference from Singleton:
 * - Singleton: One per ApplicationContext
 * - Application: One per ServletContext (web app specific)
 * - Multiple ApplicationContexts can share same ServletContext
 * 
 * Use Cases:
 * - Application-wide configuration
 * - Global counters and statistics
 * - Application-level caching
 * - Shared resources across all users
 * - Application metadata
 */
@SpringBootApplication
public class ApplicationScopePattern {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationScopePattern.class, args);
        System.out.println("\n=== Application Scope Pattern Started ===");
        System.out.println("Test endpoints:");
        System.out.println("  GET http://localhost:8080/api/app/stats");
        System.out.println("  GET http://localhost:8080/api/app/config");
        System.out.println("  POST http://localhost:8080/api/app/visit");
    }
}

/**
 * Configuration for application-scoped beans
 */
@Configuration
class ApplicationScopedConfig {
    
    @Bean
    @ApplicationScope
    public ApplicationStatistics applicationStatistics() {
        return new ApplicationStatistics();
    }
    
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ApplicationConfig applicationConfig() {
        return new ApplicationConfig();
    }
    
    @Bean
    @ApplicationScope
    public VisitorTracker visitorTracker() {
        return new VisitorTracker();
    }
}

/**
 * Application-scoped statistics
 */
@Component
@ApplicationScope
class ApplicationStatistics {
    private final LocalDateTime startTime;
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalSessions = new AtomicLong(0);
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final Map<String, AtomicLong> endpointHits = new ConcurrentHashMap<>();
    
    public ApplicationStatistics() {
        this.startTime = LocalDateTime.now();
        System.out.println("ApplicationStatistics created at: " + startTime);
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("ApplicationStatistics destroyed. Total requests: " + totalRequests.get());
    }
    
    public void incrementRequests() {
        totalRequests.incrementAndGet();
    }
    
    public void incrementSessions() {
        totalSessions.incrementAndGet();
    }
    
    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }
    
    public void decrementActiveUsers() {
        activeUsers.decrementAndGet();
    }
    
    public void recordEndpointHit(String endpoint) {
        endpointHits.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    public LocalDateTime getStartTime() { return startTime; }
    public long getTotalRequests() { return totalRequests.get(); }
    public long getTotalSessions() { return totalSessions.get(); }
    public int getActiveUsers() { return activeUsers.get(); }
    public Map<String, AtomicLong> getEndpointHits() { return endpointHits; }
    
    public long getUptimeSeconds() {
        return java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }
}

/**
 * Application-scoped configuration
 */
class ApplicationConfig {
    private final String applicationName = "Spring Scoping Demo";
    private final String version = "1.0.0";
    private final LocalDateTime deploymentTime;
    private final Map<String, String> features = new ConcurrentHashMap<>();
    private final Map<String, String> settings = new ConcurrentHashMap<>();
    
    public ApplicationConfig() {
        this.deploymentTime = LocalDateTime.now();
        initializeDefaults();
        System.out.println("ApplicationConfig created: " + applicationName + " v" + version);
    }
    
    private void initializeDefaults() {
        features.put("authentication", "enabled");
        features.put("caching", "enabled");
        features.put("logging", "enabled");
        
        settings.put("maxUploadSize", "10MB");
        settings.put("sessionTimeout", "30m");
        settings.put("defaultLocale", "en_US");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("ApplicationConfig destroyed: " + applicationName);
    }
    
    public String getApplicationName() { return applicationName; }
    public String getVersion() { return version; }
    public LocalDateTime getDeploymentTime() { return deploymentTime; }
    public Map<String, String> getFeatures() { return new HashMap<>(features); }
    public Map<String, String> getSettings() { return new HashMap<>(settings); }
    
    public void enableFeature(String feature) {
        features.put(feature, "enabled");
    }
    
    public void disableFeature(String feature) {
        features.put(feature, "disabled");
    }
    
    public boolean isFeatureEnabled(String feature) {
        return "enabled".equals(features.get(feature));
    }
    
    public void setSetting(String key, String value) {
        settings.put(key, value);
    }
    
    public String getSetting(String key) {
        return settings.get(key);
    }
}

/**
 * Application-scoped visitor tracker
 */
@Component
@ApplicationScope
class VisitorTracker {
    private final Set<String> uniqueVisitors = ConcurrentHashMap.newKeySet();
    private final Map<String, VisitorInfo> visitorDetails = new ConcurrentHashMap<>();
    private final AtomicLong totalVisits = new AtomicLong(0);
    
    public VisitorTracker() {
        System.out.println("VisitorTracker created");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("VisitorTracker destroyed. Unique visitors: " + uniqueVisitors.size() + 
                         ", Total visits: " + totalVisits.get());
    }
    
    public void recordVisit(String visitorId, String ipAddress, String userAgent) {
        totalVisits.incrementAndGet();
        uniqueVisitors.add(visitorId);
        
        VisitorInfo info = visitorDetails.computeIfAbsent(visitorId, 
            k -> new VisitorInfo(visitorId, ipAddress, userAgent));
        info.incrementVisits();
        info.updateLastVisit();
    }
    
    public int getUniqueVisitorCount() {
        return uniqueVisitors.size();
    }
    
    public long getTotalVisits() {
        return totalVisits.get();
    }
    
    public VisitorInfo getVisitorInfo(String visitorId) {
        return visitorDetails.get(visitorId);
    }
    
    public Collection<VisitorInfo> getAllVisitors() {
        return new ArrayList<>(visitorDetails.values());
    }
}

/**
 * Visitor information class
 */
class VisitorInfo {
    private final String visitorId;
    private final String ipAddress;
    private final String userAgent;
    private final LocalDateTime firstVisit;
    private LocalDateTime lastVisit;
    private final AtomicInteger visitCount = new AtomicInteger(0);
    
    public VisitorInfo(String visitorId, String ipAddress, String userAgent) {
        this.visitorId = visitorId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.firstVisit = LocalDateTime.now();
        this.lastVisit = firstVisit;
    }
    
    public void incrementVisits() {
        visitCount.incrementAndGet();
    }
    
    public void updateLastVisit() {
        this.lastVisit = LocalDateTime.now();
    }
    
    public String getVisitorId() { return visitorId; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public LocalDateTime getFirstVisit() { return firstVisit; }
    public LocalDateTime getLastVisit() { return lastVisit; }
    public int getVisitCount() { return visitCount.get(); }
}

/**
 * REST Controller demonstrating application scope
 */
@RestController
@RequestMapping("/api/app")
class ApplicationScopeController {
    
    private final ApplicationStatistics statistics;
    private final ApplicationConfig config;
    private final VisitorTracker visitorTracker;
    
    public ApplicationScopeController(ApplicationStatistics statistics,
                                     ApplicationConfig config,
                                     VisitorTracker visitorTracker) {
        this.statistics = statistics;
        this.config = config;
        this.visitorTracker = visitorTracker;
    }
    
    @GetMapping("/stats")
    public String getStatistics() {
        statistics.incrementRequests();
        statistics.recordEndpointHit("/stats");
        
        StringBuilder sb = new StringBuilder();
        sb.append("Application Statistics:\n");
        sb.append("  Started: ").append(statistics.getStartTime()).append("\n");
        sb.append("  Uptime: ").append(statistics.getUptimeSeconds()).append(" seconds\n");
        sb.append("  Total requests: ").append(statistics.getTotalRequests()).append("\n");
        sb.append("  Total sessions: ").append(statistics.getTotalSessions()).append("\n");
        sb.append("  Active users: ").append(statistics.getActiveUsers()).append("\n");
        sb.append("  Endpoint hits:\n");
        
        statistics.getEndpointHits().forEach((endpoint, hits) -> 
            sb.append("    ").append(endpoint).append(": ").append(hits.get()).append("\n"));
        
        return sb.toString();
    }
    
    @GetMapping("/config")
    public String getConfig() {
        statistics.incrementRequests();
        statistics.recordEndpointHit("/config");
        
        StringBuilder sb = new StringBuilder();
        sb.append("Application Configuration:\n");
        sb.append("  Name: ").append(config.getApplicationName()).append("\n");
        sb.append("  Version: ").append(config.getVersion()).append("\n");
        sb.append("  Deployed: ").append(config.getDeploymentTime()).append("\n");
        sb.append("  Features:\n");
        
        config.getFeatures().forEach((feature, status) -> 
            sb.append("    ").append(feature).append(": ").append(status).append("\n"));
        
        sb.append("  Settings:\n");
        config.getSettings().forEach((key, value) -> 
            sb.append("    ").append(key).append(": ").append(value).append("\n"));
        
        return sb.toString();
    }
    
    @PostMapping("/visit")
    public String recordVisit(@RequestParam String visitorId,
                             @RequestParam String ipAddress,
                             @RequestParam(required = false) String userAgent) {
        statistics.incrementRequests();
        statistics.recordEndpointHit("/visit");
        
        visitorTracker.recordVisit(visitorId, ipAddress, 
                                   userAgent != null ? userAgent : "Unknown");
        
        VisitorInfo info = visitorTracker.getVisitorInfo(visitorId);
        
        return "Visit recorded:\n" +
               "  Visitor ID: " + info.getVisitorId() + "\n" +
               "  IP: " + info.getIpAddress() + "\n" +
               "  First visit: " + info.getFirstVisit() + "\n" +
               "  Last visit: " + info.getLastVisit() + "\n" +
               "  Visit count: " + info.getVisitCount() + "\n" +
               "  Unique visitors: " + visitorTracker.getUniqueVisitorCount() + "\n" +
               "  Total visits: " + visitorTracker.getTotalVisits();
    }
    
    @GetMapping("/visitors")
    public String getVisitors() {
        statistics.incrementRequests();
        statistics.recordEndpointHit("/visitors");
        
        StringBuilder sb = new StringBuilder();
        sb.append("Visitor Information:\n");
        sb.append("  Unique visitors: ").append(visitorTracker.getUniqueVisitorCount()).append("\n");
        sb.append("  Total visits: ").append(visitorTracker.getTotalVisits()).append("\n");
        sb.append("  Details:\n");
        
        visitorTracker.getAllVisitors().stream()
            .sorted((a, b) -> b.getVisitCount() - a.getVisitCount())
            .limit(10)
            .forEach(info -> 
                sb.append("    ").append(info.getVisitorId())
                  .append(" - ").append(info.getVisitCount())
                  .append(" visits\n"));
        
        return sb.toString();
    }
    
    @GetMapping("/info")
    public String getApplicationInfo(ServletContext servletContext) {
        statistics.incrementRequests();
        statistics.recordEndpointHit("/info");
        
        return "Application Information:\n" +
               "  Name: " + config.getApplicationName() + "\n" +
               "  Version: " + config.getVersion() + "\n" +
               "  Deployment time: " + config.getDeploymentTime() + "\n" +
               "  Uptime: " + statistics.getUptimeSeconds() + " seconds\n" +
               "  Context path: " + servletContext.getContextPath() + "\n" +
               "  Server info: " + servletContext.getServerInfo() + "\n" +
               "  Servlet version: " + servletContext.getMajorVersion() + "." + 
                                       servletContext.getMinorVersion();
    }
}

/**
 * Key Points:
 * 
 * 1. Scope Comparison:
 *    - Singleton: Per ApplicationContext
 *    - Application: Per ServletContext (web app)
 *    - Both are application-wide, but application scope is web-specific
 * 
 * 2. Lifecycle:
 *    - Created on first access
 *    - Lives for entire application lifetime
 *    - Destroyed when ServletContext is destroyed
 *    - @PreDestroy called on application shutdown
 * 
 * 3. Thread Safety:
 *    - Shared across all requests and sessions
 *    - Must be thread-safe
 *    - Use ConcurrentHashMap, AtomicInteger, etc.
 *    - Synchronize mutable operations
 * 
 * 4. Use Cases:
 *    ✓ Application-wide statistics
 *    ✓ Global configuration
 *    ✓ Application metadata
 *    ✓ Shared caches
 *    ✓ Application-level counters
 *    ✓ Feature toggles
 * 
 * 5. When to Use Application vs Singleton:
 *    - Use Application: Web-specific shared state
 *    - Use Singleton: General application state
 *    - Most cases: Singleton is sufficient
 *    - Application: When ServletContext binding is important
 * 
 * 6. Best Practices:
 *    - Design for thread-safety
 *    - Use concurrent collections
 *    - Keep beans stateless when possible
 *    - Monitor memory usage
 *    - Clean up resources in @PreDestroy
 * 
 * 7. Memory Considerations:
 *    - Single instance for entire application
 *    - Can accumulate data over time
 *    - Implement cleanup strategies
 *    - Monitor heap usage
 * 
 * 8. Testing:
 *    - Requires web application context
 *    - Use @WebMvcTest or @SpringBootTest
 *    - State persists across tests
 *    - Use @DirtiesContext if needed
 */
