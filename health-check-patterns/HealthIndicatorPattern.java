package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Health Indicator Pattern - Custom Health Checks
 * 
 * Purpose:
 * - Monitor application health status
 * - Expose health information via /actuator/health
 * - Custom health checks for business logic
 * - Integration with monitoring systems
 * - Kubernetes liveness/readiness probes
 * 
 * Use Cases:
 * - External service availability
 * - Database connectivity
 * - Disk space monitoring
 * - Memory availability
 * - Custom business health metrics
 * - Third-party API status
 * - Cache availability
 * - Message queue health
 * 
 * Health Status:
 * - UP: Component is working
 * - DOWN: Component is not working
 * - OUT_OF_SERVICE: Component temporarily unavailable
 * - UNKNOWN: Component state unknown
 * - Custom statuses possible
 * 
 * Configuration (application.yml):
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: health
 *   endpoint:
 *     health:
 *       show-details: always
 *       show-components: always
 *       probes:
 *         enabled: true
 *   health:
 *     defaults:
 *       enabled: true
 *     diskspace:
 *       enabled: true
 *       threshold: 10MB
 * 
 * Health Response Format:
 * {
 *   "status": "UP",
 *   "components": {
 *     "customHealth": {
 *       "status": "UP",
 *       "details": {
 *         "key": "value"
 *       }
 *     }
 *   }
 * }
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * 
 * HealthIndicator Interface:
 * public interface HealthIndicator {
 *     Health health();
 * }
 * 
 * Health Builder Methods:
 * - up(): Build UP status
 * - down(): Build DOWN status
 * - outOfService(): Build OUT_OF_SERVICE status
 * - unknown(): Build UNKNOWN status
 * - status(Status): Build with custom status
 * - withDetail(key, value): Add detail
 * - withDetails(map): Add multiple details
 * - withException(throwable): Add exception info
 * 
 * Warnings:
 * - Health checks should be fast (<3 seconds)
 * - Avoid expensive operations
 * - Don't expose sensitive information
 * - Consider caching health results
 * - Test health indicators thoroughly
 * - Handle exceptions gracefully
 * 
 * Best Practices:
 * - Keep health checks lightweight
 * - Return meaningful status details
 * - Use appropriate health statuses
 * - Implement timeout mechanisms
 * - Cache results when appropriate
 * - Log health check failures
 * - Test with realistic scenarios
 * - Document expected behavior
 * - Use correlation IDs for debugging
 * - Monitor health endpoint performance
 */
@SpringBootApplication
public class HealthIndicatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(HealthIndicatorPattern.class, args);
    }

    // ============================================
    // Example 1: Basic Health Indicator
    // ============================================
    
    @Component("custom")
    public static class BasicHealthIndicator implements HealthIndicator {
        
        @Override
        public Health health() {
            // Simple health check
            boolean healthy = checkHealth();
            
            if (healthy) {
                return Health.up()
                    .withDetail("message", "Application is healthy")
                    .withDetail("timestamp", Instant.now())
                    .build();
            } else {
                return Health.down()
                    .withDetail("message", "Application is unhealthy")
                    .withDetail("timestamp", Instant.now())
                    .build();
            }
        }
        
        private boolean checkHealth() {
            // Implement actual health check logic
            return true;
        }
    }

    // ============================================
    // Example 2: External Service Health Indicator
    // ============================================
    
    @Component("externalService")
    public static class ExternalServiceHealthIndicator implements HealthIndicator {
        
        private static final String SERVICE_URL = "https://api.example.com/health";
        private Instant lastCheckTime = Instant.now();
        private Status lastStatus = Status.UNKNOWN;
        
        @Override
        public Health health() {
            try {
                // Check external service (simulated)
                boolean serviceAvailable = checkExternalService();
                lastCheckTime = Instant.now();
                
                if (serviceAvailable) {
                    lastStatus = Status.UP;
                    return Health.up()
                        .withDetail("service", "External API")
                        .withDetail("url", SERVICE_URL)
                        .withDetail("lastCheck", lastCheckTime)
                        .withDetail("responseTime", "120ms")
                        .build();
                } else {
                    lastStatus = Status.DOWN;
                    return Health.down()
                        .withDetail("service", "External API")
                        .withDetail("url", SERVICE_URL)
                        .withDetail("lastCheck", lastCheckTime)
                        .withDetail("error", "Service unreachable")
                        .build();
                }
            } catch (Exception e) {
                lastStatus = Status.DOWN;
                return Health.down()
                    .withDetail("service", "External API")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
            }
        }
        
        private boolean checkExternalService() {
            // Simulate external service check with timeout
            try {
                Thread.sleep(50); // Simulate network call
                return Math.random() > 0.1; // 90% success rate
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    // ============================================
    // Example 3: Database Health Indicator
    // ============================================
    
    @Component("database")
    public static class DatabaseHealthIndicator implements HealthIndicator {
        
        private int activeConnections = 10;
        private final int maxConnections = 50;
        
        @Override
        public Health health() {
            try {
                // Check database connectivity
                boolean dbConnected = checkDatabaseConnection();
                
                if (!dbConnected) {
                    return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", "Connection failed")
                        .build();
                }
                
                // Check connection pool
                double utilization = (double) activeConnections / maxConnections * 100;
                
                if (utilization > 90) {
                    return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", "Connection pool exhausted")
                        .withDetail("activeConnections", activeConnections)
                        .withDetail("maxConnections", maxConnections)
                        .withDetail("utilization", String.format("%.1f%%", utilization))
                        .build();
                } else if (utilization > 75) {
                    return Health.status("DEGRADED")
                        .withDetail("database", "PostgreSQL")
                        .withDetail("warning", "High connection pool utilization")
                        .withDetail("activeConnections", activeConnections)
                        .withDetail("maxConnections", maxConnections)
                        .withDetail("utilization", String.format("%.1f%%", utilization))
                        .build();
                } else {
                    return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("activeConnections", activeConnections)
                        .withDetail("maxConnections", maxConnections)
                        .withDetail("utilization", String.format("%.1f%%", utilization))
                        .build();
                }
            } catch (Exception e) {
                return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withException(e)
                    .build();
            }
        }
        
        private boolean checkDatabaseConnection() {
            // Simulate database connectivity check
            return Math.random() > 0.05; // 95% success rate
        }
        
        public void setActiveConnections(int count) {
            this.activeConnections = Math.min(count, maxConnections);
        }
    }

    // ============================================
    // Example 4: Memory Health Indicator
    // ============================================
    
    @Component("memory")
    public static class MemoryHealthIndicator implements HealthIndicator {
        
        private static final double WARNING_THRESHOLD = 0.75; // 75%
        private static final double CRITICAL_THRESHOLD = 0.90; // 90%
        
        @Override
        public Health health() {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            double usageRatio = (double) usedMemory / maxMemory;
            
            Map<String, Object> details = new HashMap<>();
            details.put("maxMemory", formatBytes(maxMemory));
            details.put("totalMemory", formatBytes(totalMemory));
            details.put("usedMemory", formatBytes(usedMemory));
            details.put("freeMemory", formatBytes(freeMemory));
            details.put("usagePercent", String.format("%.1f%%", usageRatio * 100));
            
            if (usageRatio > CRITICAL_THRESHOLD) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("status", "CRITICAL: Memory usage above 90%")
                    .build();
            } else if (usageRatio > WARNING_THRESHOLD) {
                return Health.status("DEGRADED")
                    .withDetails(details)
                    .withDetail("status", "WARNING: Memory usage above 75%")
                    .build();
            } else {
                return Health.up()
                    .withDetails(details)
                    .withDetail("status", "Memory usage normal")
                    .build();
            }
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            char pre = "KMGTPE".charAt(exp - 1);
            return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
        }
    }

    // ============================================
    // Example 5: Cache Health Indicator
    // ============================================
    
    @Component("cache")
    public static class CacheHealthIndicator implements HealthIndicator {
        
        private boolean cacheAvailable = true;
        private long hitCount = 1000;
        private long missCount = 100;
        
        @Override
        public Health health() {
            try {
                if (!cacheAvailable) {
                    return Health.down()
                        .withDetail("cache", "Redis")
                        .withDetail("error", "Cache unavailable")
                        .build();
                }
                
                long totalRequests = hitCount + missCount;
                double hitRatio = totalRequests > 0 ? (double) hitCount / totalRequests * 100 : 0;
                
                Map<String, Object> details = new HashMap<>();
                details.put("cache", "Redis");
                details.put("hitCount", hitCount);
                details.put("missCount", missCount);
                details.put("totalRequests", totalRequests);
                details.put("hitRatio", String.format("%.2f%%", hitRatio));
                
                // Warn if hit ratio is too low
                if (hitRatio < 50) {
                    return Health.status("DEGRADED")
                        .withDetails(details)
                        .withDetail("warning", "Low cache hit ratio")
                        .build();
                }
                
                return Health.up()
                    .withDetails(details)
                    .build();
            } catch (Exception e) {
                return Health.down()
                    .withDetail("cache", "Redis")
                    .withException(e)
                    .build();
            }
        }
        
        public void setCacheAvailable(boolean available) {
            this.cacheAvailable = available;
        }
        
        public void recordHit() {
            hitCount++;
        }
        
        public void recordMiss() {
            missCount++;
        }
    }

    // ============================================
    // Example 6: Message Queue Health Indicator
    // ============================================
    
    @Component("messageQueue")
    public static class MessageQueueHealthIndicator implements HealthIndicator {
        
        private boolean connected = true;
        private int queueDepth = 10;
        private static final int MAX_QUEUE_DEPTH = 1000;
        
        @Override
        public Health health() {
            try {
                if (!connected) {
                    return Health.down()
                        .withDetail("messageQueue", "RabbitMQ")
                        .withDetail("error", "Not connected")
                        .build();
                }
                
                Map<String, Object> details = new HashMap<>();
                details.put("messageQueue", "RabbitMQ");
                details.put("queueDepth", queueDepth);
                details.put("maxQueueDepth", MAX_QUEUE_DEPTH);
                details.put("utilization", String.format("%.1f%%", 
                    (double) queueDepth / MAX_QUEUE_DEPTH * 100));
                
                if (queueDepth > MAX_QUEUE_DEPTH * 0.9) {
                    return Health.down()
                        .withDetails(details)
                        .withDetail("error", "Queue depth critical")
                        .build();
                } else if (queueDepth > MAX_QUEUE_DEPTH * 0.75) {
                    return Health.status("DEGRADED")
                        .withDetails(details)
                        .withDetail("warning", "Queue depth high")
                        .build();
                }
                
                return Health.up()
                    .withDetails(details)
                    .build();
            } catch (Exception e) {
                return Health.down()
                    .withDetail("messageQueue", "RabbitMQ")
                    .withException(e)
                    .build();
            }
        }
        
        public void setConnected(boolean connected) {
            this.connected = connected;
        }
        
        public void setQueueDepth(int depth) {
            this.queueDepth = depth;
        }
    }

    // ============================================
    // Example 7: API Rate Limit Health Indicator
    // ============================================
    
    @Component("rateLimit")
    public static class RateLimitHealthIndicator implements HealthIndicator {
        
        private int currentRequests = 100;
        private final int maxRequests = 1000;
        private final Duration window = Duration.ofMinutes(1);
        
        @Override
        public Health health() {
            double utilizationPercent = (double) currentRequests / maxRequests * 100;
            
            Map<String, Object> details = new HashMap<>();
            details.put("currentRequests", currentRequests);
            details.put("maxRequests", maxRequests);
            details.put("window", window.toString());
            details.put("utilization", String.format("%.1f%%", utilizationPercent));
            
            if (utilizationPercent >= 100) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("error", "Rate limit exceeded")
                    .build();
            } else if (utilizationPercent > 80) {
                return Health.status("DEGRADED")
                    .withDetails(details)
                    .withDetail("warning", "Approaching rate limit")
                    .build();
            }
            
            return Health.up()
                .withDetails(details)
                .build();
        }
        
        public void setCurrentRequests(int count) {
            this.currentRequests = count;
        }
    }

    // ============================================
    // Example 8: Thread Pool Health Indicator
    // ============================================
    
    @Component("threadPool")
    public static class ThreadPoolHealthIndicator implements HealthIndicator {
        
        private int activeThreads = 5;
        private final int maxThreads = 20;
        private int queuedTasks = 10;
        private final int maxQueueSize = 100;
        
        @Override
        public Health health() {
            double threadUtilization = (double) activeThreads / maxThreads * 100;
            double queueUtilization = (double) queuedTasks / maxQueueSize * 100;
            
            Map<String, Object> details = new HashMap<>();
            details.put("activeThreads", activeThreads);
            details.put("maxThreads", maxThreads);
            details.put("threadUtilization", String.format("%.1f%%", threadUtilization));
            details.put("queuedTasks", queuedTasks);
            details.put("maxQueueSize", maxQueueSize);
            details.put("queueUtilization", String.format("%.1f%%", queueUtilization));
            
            if (threadUtilization >= 100 || queueUtilization >= 100) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("error", "Thread pool exhausted")
                    .build();
            } else if (threadUtilization > 80 || queueUtilization > 80) {
                return Health.status("DEGRADED")
                    .withDetails(details)
                    .withDetail("warning", "High thread pool utilization")
                    .build();
            }
            
            return Health.up()
                .withDetails(details)
                .build();
        }
        
        public void setActiveThreads(int count) {
            this.activeThreads = Math.min(count, maxThreads);
        }
        
        public void setQueuedTasks(int count) {
            this.queuedTasks = Math.min(count, maxQueueSize);
        }
    }

    // ============================================
    // Example 9: Custom Status Health Indicator
    // ============================================
    
    @Component("customStatus")
    public static class CustomStatusHealthIndicator implements HealthIndicator {
        
        // Custom statuses
        private static final Status HEALTHY = new Status("HEALTHY");
        private static final Status DEGRADED = new Status("DEGRADED");
        private static final Status MAINTENANCE = new Status("MAINTENANCE");
        
        private String currentMode = "normal";
        
        @Override
        public Health health() {
            switch (currentMode) {
                case "maintenance":
                    return Health.status(MAINTENANCE)
                        .withDetail("mode", "maintenance")
                        .withDetail("message", "System under maintenance")
                        .withDetail("estimatedDowntime", "30 minutes")
                        .build();
                case "degraded":
                    return Health.status(DEGRADED)
                        .withDetail("mode", "degraded")
                        .withDetail("message", "Some features unavailable")
                        .withDetail("affectedFeatures", Arrays.asList("reporting", "exports"))
                        .build();
                default:
                    return Health.status(HEALTHY)
                        .withDetail("mode", "normal")
                        .withDetail("message", "All systems operational")
                        .build();
            }
        }
        
        public void setMode(String mode) {
            this.currentMode = mode;
        }
    }

    // ============================================
    // Example 10: Health Indicator REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/health-control")
    public static class HealthControlController {
        
        private final DatabaseHealthIndicator dbHealth;
        private final CacheHealthIndicator cacheHealth;
        private final MessageQueueHealthIndicator mqHealth;
        private final RateLimitHealthIndicator rateLimitHealth;
        private final ThreadPoolHealthIndicator threadPoolHealth;
        private final CustomStatusHealthIndicator customStatusHealth;
        
        public HealthControlController(
                DatabaseHealthIndicator dbHealth,
                CacheHealthIndicator cacheHealth,
                MessageQueueHealthIndicator mqHealth,
                RateLimitHealthIndicator rateLimitHealth,
                ThreadPoolHealthIndicator threadPoolHealth,
                CustomStatusHealthIndicator customStatusHealth) {
            this.dbHealth = dbHealth;
            this.cacheHealth = cacheHealth;
            this.mqHealth = mqHealth;
            this.rateLimitHealth = rateLimitHealth;
            this.threadPoolHealth = threadPoolHealth;
            this.customStatusHealth = customStatusHealth;
        }
        
        @PostMapping("/database/connections")
        public Map<String, String> setDatabaseConnections(@RequestParam int count) {
            dbHealth.setActiveConnections(count);
            return Collections.singletonMap("status", "Database connections updated");
        }
        
        @PostMapping("/cache/available")
        public Map<String, String> setCacheAvailable(@RequestParam boolean available) {
            cacheHealth.setCacheAvailable(available);
            return Collections.singletonMap("status", "Cache availability updated");
        }
        
        @PostMapping("/cache/hit")
        public Map<String, String> recordCacheHit() {
            cacheHealth.recordHit();
            return Collections.singletonMap("status", "Cache hit recorded");
        }
        
        @PostMapping("/cache/miss")
        public Map<String, String> recordCacheMiss() {
            cacheHealth.recordMiss();
            return Collections.singletonMap("status", "Cache miss recorded");
        }
        
        @PostMapping("/queue/connected")
        public Map<String, String> setQueueConnected(@RequestParam boolean connected) {
            mqHealth.setConnected(connected);
            return Collections.singletonMap("status", "Queue connection updated");
        }
        
        @PostMapping("/queue/depth")
        public Map<String, String> setQueueDepth(@RequestParam int depth) {
            mqHealth.setQueueDepth(depth);
            return Collections.singletonMap("status", "Queue depth updated");
        }
        
        @PostMapping("/ratelimit/requests")
        public Map<String, String> setCurrentRequests(@RequestParam int count) {
            rateLimitHealth.setCurrentRequests(count);
            return Collections.singletonMap("status", "Request count updated");
        }
        
        @PostMapping("/threadpool/active")
        public Map<String, String> setActiveThreads(@RequestParam int count) {
            threadPoolHealth.setActiveThreads(count);
            return Collections.singletonMap("status", "Active threads updated");
        }
        
        @PostMapping("/threadpool/queued")
        public Map<String, String> setQueuedTasks(@RequestParam int count) {
            threadPoolHealth.setQueuedTasks(count);
            return Collections.singletonMap("status", "Queued tasks updated");
        }
        
        @PostMapping("/mode")
        public Map<String, String> setMode(@RequestParam String mode) {
            customStatusHealth.setMode(mode);
            return Collections.singletonMap("status", "Mode updated to: " + mode);
        }
    }
}
