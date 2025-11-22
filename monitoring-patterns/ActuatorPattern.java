package com.example.monitoring.actuator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Actuator Pattern - Spring Boot Actuator Integration
 * 
 * Demonstrates:
 * 1. Built-in actuator endpoints (health, metrics, info, env)
 * 2. Custom actuator endpoints
 * 3. Health indicators for system monitoring
 * 4. Info contributors for application metadata
 * 5. Metrics collection and exposure
 * 6. Management endpoint security
 * 7. Actuator web endpoints
 * 8. Custom health checks
 * 
 * Required Dependencies:
 * - spring-boot-starter-actuator
 * - spring-boot-starter-web
 * 
 * Configuration (application.properties):
 * management.endpoints.web.exposure.include=*
 * management.endpoint.health.show-details=always
 * management.endpoints.web.base-path=/actuator
 */
@SpringBootApplication
public class ActuatorPattern {

    public static void main(String[] args) {
        System.out.println("=== Spring Boot Actuator Pattern Demo ===\n");
        
        SpringApplication app = new SpringApplication(ActuatorPattern.class);
        
        // Configure properties programmatically
        Properties properties = new Properties();
        properties.put("server.port", "8080");
        properties.put("management.endpoints.web.exposure.include", "*");
        properties.put("management.endpoint.health.show-details", "always");
        properties.put("management.endpoints.web.base-path", "/actuator");
        properties.put("management.endpoint.health.show-components", "always");
        properties.put("info.app.name", "Actuator Demo Application");
        properties.put("info.app.version", "1.0.0");
        properties.put("info.app.description", "Demonstrating Spring Boot Actuator");
        
        app.setDefaultProperties(properties);
        app.run(args);
    }

    @Component
    public static class ActuatorDemo {
        
        @EventListener(ApplicationReadyEvent.class)
        public void onApplicationReady() {
            System.out.println("\n=== Actuator Endpoints Available ===");
            System.out.println("Health:      http://localhost:8080/actuator/health");
            System.out.println("Metrics:     http://localhost:8080/actuator/metrics");
            System.out.println("Info:        http://localhost:8080/actuator/info");
            System.out.println("Env:         http://localhost:8080/actuator/env");
            System.out.println("Beans:       http://localhost:8080/actuator/beans");
            System.out.println("Mappings:    http://localhost:8080/actuator/mappings");
            System.out.println("Conditions:  http://localhost:8080/actuator/conditions");
            System.out.println("ConfigProps: http://localhost:8080/actuator/configprops");
            System.out.println("Loggers:     http://localhost:8080/actuator/loggers");
            System.out.println("ThreadDump:  http://localhost:8080/actuator/threaddump");
            System.out.println("HeapDump:    http://localhost:8080/actuator/heapdump");
            System.out.println("\nCustom Endpoints:");
            System.out.println("Application: http://localhost:8080/actuator/application");
            System.out.println("Statistics:  http://localhost:8080/actuator/statistics");
            System.out.println("Cache:       http://localhost:8080/actuator/cache");
            System.out.println("\nPress Ctrl+C to stop the application\n");
        }
    }

    // ==================== Custom Health Indicators ====================

    /**
     * Database Health Indicator
     * Checks database connectivity and performance
     */
    @Component
    public static class DatabaseHealthIndicator implements HealthIndicator {
        
        private final AtomicInteger connectionCount = new AtomicInteger(0);
        private volatile boolean isConnected = true;

        @Override
        public Health health() {
            try {
                // Simulate database health check
                checkDatabaseConnection();
                
                Map<String, Object> details = new HashMap<>();
                details.put("database", "PostgreSQL");
                details.put("status", "UP");
                details.put("connections", connectionCount.get());
                details.put("maxConnections", 100);
                details.put("responseTime", "15ms");
                details.put("lastCheck", LocalDateTime.now());

                return Health.up()
                    .withDetails(details)
                    .build();
                    
            } catch (Exception e) {
                return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("database", "PostgreSQL")
                    .build();
            }
        }

        private void checkDatabaseConnection() {
            // Simulate connection check
            connectionCount.set(new Random().nextInt(50) + 10);
            if (!isConnected) {
                throw new RuntimeException("Database connection failed");
            }
        }

        public void setConnected(boolean connected) {
            this.isConnected = connected;
        }
    }

    /**
     * External Service Health Indicator
     * Monitors external API availability
     */
    @Component
    public static class ExternalServiceHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                boolean apiAvailable = checkExternalApi();
                
                if (apiAvailable) {
                    return Health.up()
                        .withDetail("service", "External Payment API")
                        .withDetail("status", "Available")
                        .withDetail("responseTime", "250ms")
                        .withDetail("lastCheck", LocalDateTime.now())
                        .build();
                } else {
                    return Health.down()
                        .withDetail("service", "External Payment API")
                        .withDetail("status", "Unavailable")
                        .withDetail("error", "Connection timeout")
                        .build();
                }
            } catch (Exception e) {
                return Health.down()
                    .withException(e)
                    .build();
            }
        }

        private boolean checkExternalApi() {
            // Simulate API check - 90% success rate
            return Math.random() > 0.1;
        }
    }

    /**
     * Disk Space Health Indicator
     * Monitors available disk space
     */
    @Component
    public static class DiskSpaceHealthIndicator implements HealthIndicator {

        private static final long THRESHOLD = 10L * 1024 * 1024 * 1024; // 10GB

        @Override
        public Health health() {
            long freeSpace = getFreeSpace();
            long totalSpace = getTotalSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercent = (usedSpace * 100.0) / totalSpace;

            Map<String, Object> details = new HashMap<>();
            details.put("total", formatBytes(totalSpace));
            details.put("used", formatBytes(usedSpace));
            details.put("free", formatBytes(freeSpace));
            details.put("usagePercent", String.format("%.2f%%", usagePercent));
            details.put("threshold", formatBytes(THRESHOLD));

            if (freeSpace >= THRESHOLD) {
                return Health.up().withDetails(details).build();
            } else {
                details.put("warning", "Low disk space");
                return Health.down().withDetails(details).build();
            }
        }

        private long getFreeSpace() {
            // Simulate disk space check
            return 50L * 1024 * 1024 * 1024; // 50GB
        }

        private long getTotalSpace() {
            return 500L * 1024 * 1024 * 1024; // 500GB
        }

        private String formatBytes(long bytes) {
            long gb = bytes / (1024 * 1024 * 1024);
            return gb + "GB";
        }
    }

    // ==================== Custom Info Contributors ====================

    /**
     * Application Build Info Contributor
     */
    @Component
    public static class BuildInfoContributor implements InfoContributor {

        @Override
        public void contribute(Info.Builder builder) {
            Map<String, Object> buildInfo = new HashMap<>();
            buildInfo.put("version", "1.0.0");
            buildInfo.put("buildTime", "2024-01-15T10:30:00Z");
            buildInfo.put("buildNumber", "123");
            buildInfo.put("gitCommit", "a1b2c3d4e5f6");
            buildInfo.put("gitBranch", "main");
            
            builder.withDetail("build", buildInfo);
        }
    }

    /**
     * Application Environment Info Contributor
     */
    @Component
    public static class EnvironmentInfoContributor implements InfoContributor {

        @Override
        public void contribute(Info.Builder builder) {
            Map<String, Object> envInfo = new HashMap<>();
            envInfo.put("environment", "production");
            envInfo.put("region", "us-east-1");
            envInfo.put("datacenter", "aws-dc1");
            envInfo.put("javaVersion", System.getProperty("java.version"));
            envInfo.put("osName", System.getProperty("os.name"));
            envInfo.put("osVersion", System.getProperty("os.version"));
            
            builder.withDetail("environment", envInfo);
        }
    }

    /**
     * Application Team Info Contributor
     */
    @Component
    public static class TeamInfoContributor implements InfoContributor {

        @Override
        public void contribute(Info.Builder builder) {
            Map<String, Object> teamInfo = new HashMap<>();
            teamInfo.put("team", "Platform Engineering");
            teamInfo.put("contact", "platform@example.com");
            teamInfo.put("slack", "#platform-team");
            teamInfo.put("oncall", "oncall@example.com");
            
            builder.withDetail("team", teamInfo);
        }
    }

    // ==================== Custom Actuator Endpoints ====================

    /**
     * Application Status Endpoint
     * Provides custom application status information
     */
    @Component
    @Endpoint(id = "application")
    public static class ApplicationEndpoint {

        private final AtomicLong requestCount = new AtomicLong(0);
        private final long startTime = System.currentTimeMillis();

        @ReadOperation
        public Map<String, Object> getApplicationStatus() {
            Map<String, Object> status = new HashMap<>();
            status.put("name", "Actuator Demo Application");
            status.put("status", "RUNNING");
            status.put("uptime", getUptime());
            status.put("requests", requestCount.get());
            status.put("startTime", new Date(startTime));
            status.put("currentTime", new Date());
            
            return status;
        }

        @ReadOperation
        public Map<String, Object> getApplicationStatusFiltered(
                @Selector String component) {
            
            Map<String, Object> status = new HashMap<>();
            
            switch (component.toLowerCase()) {
                case "uptime":
                    status.put("uptime", getUptime());
                    break;
                case "requests":
                    status.put("requests", requestCount.get());
                    break;
                case "time":
                    status.put("startTime", new Date(startTime));
                    status.put("currentTime", new Date());
                    break;
                default:
                    status.put("error", "Unknown component: " + component);
            }
            
            return status;
        }

        @WriteOperation
        public Map<String, Object> resetCounters() {
            long oldCount = requestCount.getAndSet(0);
            
            Map<String, Object> result = new HashMap<>();
            result.put("action", "reset");
            result.put("previousCount", oldCount);
            result.put("currentCount", 0);
            result.put("timestamp", new Date());
            
            return result;
        }

        @DeleteOperation
        public Map<String, Object> shutdown() {
            Map<String, Object> result = new HashMap<>();
            result.put("action", "shutdown");
            result.put("message", "Application shutdown initiated");
            result.put("timestamp", new Date());
            
            return result;
        }

        private String getUptime() {
            long uptimeMillis = System.currentTimeMillis() - startTime;
            long seconds = uptimeMillis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            
            return String.format("%dh %dm %ds", 
                hours, minutes % 60, seconds % 60);
        }

        public void incrementRequestCount() {
            requestCount.incrementAndGet();
        }
    }

    /**
     * Statistics Endpoint
     * Provides application statistics
     */
    @Component
    @Endpoint(id = "statistics")
    public static class StatisticsEndpoint {

        private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
        private final Map<String, AtomicLong> timers = new ConcurrentHashMap<>();

        public StatisticsEndpoint() {
            counters.put("http.requests", new AtomicLong(0));
            counters.put("http.errors", new AtomicLong(0));
            counters.put("database.queries", new AtomicLong(0));
            counters.put("cache.hits", new AtomicLong(0));
            counters.put("cache.misses", new AtomicLong(0));
            
            timers.put("http.response.time", new AtomicLong(0));
            timers.put("database.query.time", new AtomicLong(0));
        }

        @ReadOperation
        public Map<String, Object> getStatistics() {
            Map<String, Object> stats = new HashMap<>();
            
            Map<String, Long> counterValues = new HashMap<>();
            counters.forEach((key, value) -> counterValues.put(key, value.get()));
            stats.put("counters", counterValues);
            
            Map<String, Long> timerValues = new HashMap<>();
            timers.forEach((key, value) -> timerValues.put(key, value.get()));
            stats.put("timers", timerValues);
            
            stats.put("timestamp", new Date());
            
            return stats;
        }

        @ReadOperation
        public Map<String, Object> getStatisticsByType(
                @Selector String type) {
            
            Map<String, Object> result = new HashMap<>();
            
            if ("counters".equals(type)) {
                Map<String, Long> counterValues = new HashMap<>();
                counters.forEach((key, value) -> counterValues.put(key, value.get()));
                result.put("counters", counterValues);
            } else if ("timers".equals(type)) {
                Map<String, Long> timerValues = new HashMap<>();
                timers.forEach((key, value) -> timerValues.put(key, value.get()));
                result.put("timers", timerValues);
            } else {
                result.put("error", "Unknown type: " + type);
            }
            
            return result;
        }

        @WriteOperation
        public Map<String, Object> incrementCounter(String name, Long value) {
            counters.computeIfAbsent(name, k -> new AtomicLong(0))
                    .addAndGet(value != null ? value : 1);
            
            Map<String, Object> result = new HashMap<>();
            result.put("action", "increment");
            result.put("counter", name);
            result.put("newValue", counters.get(name).get());
            
            return result;
        }

        @DeleteOperation
        public Map<String, Object> resetStatistics() {
            counters.values().forEach(counter -> counter.set(0));
            timers.values().forEach(timer -> timer.set(0));
            
            Map<String, Object> result = new HashMap<>();
            result.put("action", "reset");
            result.put("message", "All statistics reset");
            result.put("timestamp", new Date());
            
            return result;
        }
    }

    /**
     * Cache Management Endpoint
     * Provides cache monitoring and management
     */
    @Component
    @Endpoint(id = "cache")
    public static class CacheEndpoint {

        private final Map<String, Map<String, Object>> caches = new ConcurrentHashMap<>();
        private final Map<String, AtomicInteger> cacheStats = new ConcurrentHashMap<>();

        public CacheEndpoint() {
            // Initialize sample caches
            caches.put("users", new ConcurrentHashMap<>());
            caches.put("products", new ConcurrentHashMap<>());
            caches.put("sessions", new ConcurrentHashMap<>());
            
            cacheStats.put("users", new AtomicInteger(100));
            cacheStats.put("products", new AtomicInteger(500));
            cacheStats.put("sessions", new AtomicInteger(50));
        }

        @ReadOperation
        public Map<String, Object> getCacheInfo() {
            Map<String, Object> info = new HashMap<>();
            
            List<Map<String, Object>> cacheList = new ArrayList<>();
            caches.forEach((name, cache) -> {
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("name", name);
                cacheInfo.put("size", cache.size());
                cacheInfo.put("hitCount", cacheStats.getOrDefault(name, new AtomicInteger(0)).get());
                cacheList.add(cacheInfo);
            });
            
            info.put("caches", cacheList);
            info.put("totalCaches", caches.size());
            info.put("timestamp", new Date());
            
            return info;
        }

        @ReadOperation
        public Map<String, Object> getCacheDetails(@Selector String cacheName) {
            Map<String, Object> details = new HashMap<>();
            
            if (caches.containsKey(cacheName)) {
                Map<String, Object> cache = caches.get(cacheName);
                details.put("name", cacheName);
                details.put("size", cache.size());
                details.put("hitCount", cacheStats.get(cacheName).get());
                details.put("entries", cache.size());
            } else {
                details.put("error", "Cache not found: " + cacheName);
            }
            
            return details;
        }

        @DeleteOperation
        public Map<String, Object> clearCache(String cacheName) {
            Map<String, Object> result = new HashMap<>();
            
            if (cacheName == null || cacheName.isEmpty()) {
                // Clear all caches
                caches.values().forEach(Map::clear);
                result.put("action", "clear-all");
                result.put("message", "All caches cleared");
            } else if (caches.containsKey(cacheName)) {
                caches.get(cacheName).clear();
                result.put("action", "clear");
                result.put("cache", cacheName);
                result.put("message", "Cache cleared");
            } else {
                result.put("error", "Cache not found: " + cacheName);
            }
            
            result.put("timestamp", new Date());
            return result;
        }
    }
}
