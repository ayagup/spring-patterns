package com.example.monitoring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Health Indicator Pattern - Custom Health Checks
 * 
 * Demonstrates:
 * 1. Custom health indicators for various components
 * 2. Database connection health checks
 * 3. External service health validation
 * 4. Disk space monitoring
 * 5. Memory usage health checks
 * 6. Custom health status (UP, DOWN, OUT_OF_SERVICE, UNKNOWN)
 * 7. Health details and metadata
 * 8. Reactive health indicators
 * 9. Composite health checks
 * 10. Health thresholds and warnings
 */
public class HealthIndicatorPattern {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Health Indicator Pattern Demo ===\n");

        // Create health indicators
        DatabaseHealthIndicator dbHealth = new DatabaseHealthIndicator();
        ApiHealthIndicator apiHealth = new ApiHealthIndicator();
        DiskSpaceHealthIndicator diskHealth = new DiskSpaceHealthIndicator();
        MemoryHealthIndicator memoryHealth = new MemoryHealthIndicator();
        CacheHealthIndicator cacheHealth = new CacheHealthIndicator();
        ThreadPoolHealthIndicator threadPoolHealth = new ThreadPoolHealthIndicator();
        CustomServiceHealthIndicator serviceHealth = new CustomServiceHealthIndicator();

        // Demonstrate health checks
        demonstrateHealthChecks(dbHealth, apiHealth, diskHealth, memoryHealth, 
                              cacheHealth, threadPoolHealth, serviceHealth);

        // Simulate health state changes
        System.out.println("\n=== Simulating Health State Changes ===\n");
        simulateHealthChanges(dbHealth, apiHealth, serviceHealth);

        // Display overall health status
        displayOverallHealth(dbHealth, apiHealth, diskHealth, memoryHealth,
                           cacheHealth, threadPoolHealth, serviceHealth);

        System.out.println("\n=== Health Indicator Pattern Demo Completed ===");
    }

    private static void demonstrateHealthChecks(HealthIndicator... indicators) {
        System.out.println("1. Performing Health Checks\n");

        for (HealthIndicator indicator : indicators) {
            Health health = indicator.health();
            String name = indicator.getClass().getSimpleName();
            
            System.out.println(name + ":");
            System.out.println("  Status: " + health.getStatus());
            
            Map<String, Object> details = health.getDetails();
            if (!details.isEmpty()) {
                System.out.println("  Details:");
                details.forEach((key, value) -> 
                    System.out.println("    " + key + ": " + value));
            }
            System.out.println();
        }
    }

    private static void simulateHealthChanges(
            DatabaseHealthIndicator dbHealth,
            ApiHealthIndicator apiHealth,
            CustomServiceHealthIndicator serviceHealth) throws InterruptedException {
        
        System.out.println("Simulating database connection failure...");
        dbHealth.simulateFailure();
        Health health = dbHealth.health();
        System.out.println("Database Status: " + health.getStatus());
        System.out.println("Error: " + health.getDetails().get("error") + "\n");

        Thread.sleep(1000);

        System.out.println("Restoring database connection...");
        dbHealth.simulateRecover();
        health = dbHealth.health();
        System.out.println("Database Status: " + health.getStatus() + "\n");

        System.out.println("Simulating API degradation...");
        apiHealth.setResponseTime(5000);
        health = apiHealth.health();
        System.out.println("API Status: " + health.getStatus());
        System.out.println("Warning: " + health.getDetails().get("warning") + "\n");

        System.out.println("Putting service in maintenance mode...");
        serviceHealth.setMaintenance(true);
        health = serviceHealth.health();
        System.out.println("Service Status: " + health.getStatus());
        System.out.println("Reason: " + health.getDetails().get("reason") + "\n");
    }

    private static void displayOverallHealth(HealthIndicator... indicators) {
        System.out.println("=== Overall Health Status ===\n");

        int up = 0, down = 0, degraded = 0, outOfService = 0, unknown = 0;

        for (HealthIndicator indicator : indicators) {
            Health health = indicator.health();
            Status status = health.getStatus();

            if (Status.UP.equals(status)) up++;
            else if (Status.DOWN.equals(status)) down++;
            else if (Status.OUT_OF_SERVICE.equals(status)) outOfService++;
            else if (Status.UNKNOWN.equals(status)) unknown++;
            else degraded++; // Custom status
        }

        System.out.println("UP:             " + up);
        System.out.println("DOWN:           " + down);
        System.out.println("DEGRADED:       " + degraded);
        System.out.println("OUT_OF_SERVICE: " + outOfService);
        System.out.println("UNKNOWN:        " + unknown);

        String overallStatus = down > 0 || outOfService > 0 ? "UNHEALTHY" : 
                              degraded > 0 ? "DEGRADED" : "HEALTHY";
        System.out.println("\nOverall Status: " + overallStatus);
    }

    // ==================== Health Indicator Implementations ====================

    /**
     * Database Health Indicator
     * Checks database connectivity and query performance
     */
    @Component
    public static class DatabaseHealthIndicator implements HealthIndicator {

        private final AtomicBoolean connected = new AtomicBoolean(true);
        private final AtomicInteger activeConnections = new AtomicInteger(5);

        @Override
        public Health health() {
            try {
                if (!connected.get()) {
                    return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", "Connection refused")
                        .withDetail("host", "localhost:5432")
                        .withDetail("timestamp", LocalDateTime.now())
                        .build();
                }

                // Simulate connection check
                long responseTime = checkConnection();

                Map<String, Object> details = new HashMap<>();
                details.put("database", "PostgreSQL");
                details.put("version", "15.2");
                details.put("host", "localhost:5432");
                details.put("schema", "public");
                details.put("activeConnections", activeConnections.get());
                details.put("maxConnections", 100);
                details.put("responseTime", responseTime + "ms");
                details.put("lastCheck", LocalDateTime.now());

                if (responseTime > 1000) {
                    return Health.status("SLOW")
                        .withDetails(details)
                        .withDetail("warning", "Slow database response")
                        .build();
                }

                return Health.up().withDetails(details).build();

            } catch (Exception e) {
                return Health.down()
                    .withException(e)
                    .withDetail("database", "PostgreSQL")
                    .build();
            }
        }

        private long checkConnection() {
            // Simulate database query
            return new Random().nextInt(200) + 10;
        }

        public void simulateFailure() {
            connected.set(false);
        }

        public void simulateRecover() {
            connected.set(true);
        }
    }

    /**
     * External API Health Indicator
     * Monitors external service availability
     */
    @Component
    public static class ApiHealthIndicator implements HealthIndicator {

        private volatile long responseTime = 150;
        private static final long TIMEOUT_THRESHOLD = 3000;
        private static final long SLOW_THRESHOLD = 1000;

        @Override
        public Health health() {
            try {
                boolean available = checkApiAvailability();

                if (!available) {
                    return Health.down()
                        .withDetail("service", "Payment API")
                        .withDetail("endpoint", "https://api.payment.com")
                        .withDetail("error", "Service unavailable")
                        .withDetail("timeout", TIMEOUT_THRESHOLD + "ms")
                        .build();
                }

                Map<String, Object> details = new HashMap<>();
                details.put("service", "Payment API");
                details.put("endpoint", "https://api.payment.com");
                details.put("responseTime", responseTime + "ms");
                details.put("version", "v2.1");
                details.put("lastCheck", LocalDateTime.now());

                if (responseTime > SLOW_THRESHOLD) {
                    return Health.status("DEGRADED")
                        .withDetails(details)
                        .withDetail("warning", "API response is slow")
                        .build();
                }

                return Health.up().withDetails(details).build();

            } catch (Exception e) {
                return Health.down()
                    .withException(e)
                    .withDetail("service", "Payment API")
                    .build();
            }
        }

        private boolean checkApiAvailability() {
            // Simulate API check
            return responseTime < TIMEOUT_THRESHOLD;
        }

        public void setResponseTime(long time) {
            this.responseTime = time;
        }
    }

    /**
     * Disk Space Health Indicator
     * Monitors available disk space
     */
    @Component
    public static class DiskSpaceHealthIndicator implements HealthIndicator {

        private static final long CRITICAL_THRESHOLD = 5L * 1024 * 1024 * 1024; // 5GB
        private static final long WARNING_THRESHOLD = 20L * 1024 * 1024 * 1024; // 20GB

        @Override
        public Health health() {
            File root = new File("/");
            long freeSpace = root.getFreeSpace();
            long totalSpace = root.getTotalSpace();
            long usableSpace = root.getUsableSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercent = (usedSpace * 100.0) / totalSpace;

            Map<String, Object> details = new HashMap<>();
            details.put("total", formatBytes(totalSpace));
            details.put("free", formatBytes(freeSpace));
            details.put("usable", formatBytes(usableSpace));
            details.put("used", formatBytes(usedSpace));
            details.put("usagePercent", String.format("%.2f%%", usagePercent));
            details.put("path", root.getAbsolutePath());
            details.put("threshold", formatBytes(CRITICAL_THRESHOLD));

            if (freeSpace < CRITICAL_THRESHOLD) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("error", "Critical: Low disk space")
                    .build();
            }

            if (freeSpace < WARNING_THRESHOLD) {
                return Health.status("WARNING")
                    .withDetails(details)
                    .withDetail("warning", "Disk space running low")
                    .build();
            }

            return Health.up().withDetails(details).build();
        }

        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + "B";
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            char pre = "KMGTPE".charAt(exp - 1);
            return String.format("%.2f%sB", bytes / Math.pow(1024, exp), pre);
        }
    }

    /**
     * Memory Health Indicator
     * Monitors JVM memory usage
     */
    @Component
    public static class MemoryHealthIndicator implements HealthIndicator {

        private static final double WARNING_THRESHOLD = 0.80; // 80%
        private static final double CRITICAL_THRESHOLD = 0.95; // 95%

        @Override
        public Health health() {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double usagePercent = (double) usedMemory / maxMemory;

            Map<String, Object> details = new HashMap<>();
            details.put("max", formatBytes(maxMemory));
            details.put("total", formatBytes(totalMemory));
            details.put("used", formatBytes(usedMemory));
            details.put("free", formatBytes(freeMemory));
            details.put("usagePercent", String.format("%.2f%%", usagePercent * 100));
            details.put("availableProcessors", runtime.availableProcessors());

            if (usagePercent >= CRITICAL_THRESHOLD) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("error", "Critical: Memory usage very high")
                    .build();
            }

            if (usagePercent >= WARNING_THRESHOLD) {
                return Health.status("WARNING")
                    .withDetails(details)
                    .withDetail("warning", "Memory usage high")
                    .build();
            }

            return Health.up().withDetails(details).build();
        }

        private String formatBytes(long bytes) {
            return String.format("%.2fMB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Cache Health Indicator
     * Monitors cache performance
     */
    @Component
    public static class CacheHealthIndicator implements HealthIndicator {

        private final AtomicInteger size = new AtomicInteger(150);
        private final AtomicInteger maxSize = new AtomicInteger(1000);
        private final AtomicInteger hitCount = new AtomicInteger(850);
        private final AtomicInteger missCount = new AtomicInteger(150);

        @Override
        public Health health() {
            int currentSize = size.get();
            int max = maxSize.get();
            double fillRatio = (double) currentSize / max;
            long totalRequests = hitCount.get() + missCount.get();
            double hitRatio = totalRequests == 0 ? 0 : 
                            (double) hitCount.get() / totalRequests;

            Map<String, Object> details = new HashMap<>();
            details.put("name", "applicationCache");
            details.put("provider", "Caffeine");
            details.put("size", currentSize);
            details.put("maxSize", max);
            details.put("fillRatio", String.format("%.2f%%", fillRatio * 100));
            details.put("hitCount", hitCount.get());
            details.put("missCount", missCount.get());
            details.put("hitRatio", String.format("%.2f%%", hitRatio * 100));
            details.put("totalRequests", totalRequests);

            if (hitRatio < 0.5) {
                return Health.status("DEGRADED")
                    .withDetails(details)
                    .withDetail("warning", "Low cache hit ratio")
                    .build();
            }

            if (fillRatio > 0.9) {
                return Health.status("WARNING")
                    .withDetails(details)
                    .withDetail("warning", "Cache nearly full")
                    .build();
            }

            return Health.up().withDetails(details).build();
        }
    }

    /**
     * Thread Pool Health Indicator
     * Monitors thread pool status
     */
    @Component
    public static class ThreadPoolHealthIndicator implements HealthIndicator {

        private final AtomicInteger activeThreads = new AtomicInteger(8);
        private final AtomicInteger poolSize = new AtomicInteger(10);
        private final AtomicInteger maxPoolSize = new AtomicInteger(50);
        private final AtomicInteger queueSize = new AtomicInteger(5);
        private final AtomicInteger queueCapacity = new AtomicInteger(100);

        @Override
        public Health health() {
            int active = activeThreads.get();
            int pool = poolSize.get();
            int max = maxPoolSize.get();
            int queue = queueSize.get();
            int capacity = queueCapacity.get();

            double utilization = (double) active / pool;
            double queueUtilization = (double) queue / capacity;

            Map<String, Object> details = new HashMap<>();
            details.put("name", "taskExecutor");
            details.put("activeThreads", active);
            details.put("poolSize", pool);
            details.put("maxPoolSize", max);
            details.put("queueSize", queue);
            details.put("queueCapacity", capacity);
            details.put("utilization", String.format("%.2f%%", utilization * 100));
            details.put("queueUtilization", String.format("%.2f%%", queueUtilization * 100));

            if (utilization >= 0.9 && queueUtilization >= 0.8) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("error", "Thread pool and queue near capacity")
                    .build();
            }

            if (utilization >= 0.8) {
                return Health.status("WARNING")
                    .withDetails(details)
                    .withDetail("warning", "High thread pool utilization")
                    .build();
            }

            return Health.up().withDetails(details).build();
        }
    }

    /**
     * Custom Service Health Indicator
     * Application-specific health check
     */
    @Component
    public static class CustomServiceHealthIndicator implements HealthIndicator {

        private final AtomicBoolean maintenanceMode = new AtomicBoolean(false);
        private final LocalDateTime startTime = LocalDateTime.now();
        private final AtomicInteger processedRequests = new AtomicInteger(1250);
        private final AtomicInteger failedRequests = new AtomicInteger(12);

        @Override
        public Health health() {
            if (maintenanceMode.get()) {
                return Health.outOfService()
                    .withDetail("service", "Application")
                    .withDetail("reason", "Scheduled maintenance")
                    .withDetail("maintenanceMode", true)
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
            }

            long uptime = Duration.between(startTime, LocalDateTime.now()).toSeconds();
            int processed = processedRequests.get();
            int failed = failedRequests.get();
            double errorRate = (double) failed / processed * 100;

            Map<String, Object> details = new HashMap<>();
            details.put("service", "Application");
            details.put("version", "1.0.0");
            details.put("uptime", uptime + "s");
            details.put("startTime", startTime);
            details.put("processedRequests", processed);
            details.put("failedRequests", failed);
            details.put("errorRate", String.format("%.2f%%", errorRate));
            details.put("maintenanceMode", false);

            if (errorRate > 5.0) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("error", "High error rate")
                    .build();
            }

            if (errorRate > 2.0) {
                return Health.status("DEGRADED")
                    .withDetails(details)
                    .withDetail("warning", "Elevated error rate")
                    .build();
            }

            return Health.up().withDetails(details).build();
        }

        public void setMaintenance(boolean maintenance) {
            this.maintenanceMode.set(maintenance);
        }
    }

    /**
     * Reactive Health Indicator
     * Non-blocking health check
     */
    @Component
    public static class ReactiveExternalServiceHealthIndicator 
            implements ReactiveHealthIndicator {

        @Override
        public Mono<Health> health() {
            return Mono.fromCallable(() -> {
                // Simulate async health check
                Thread.sleep(100);
                
                Map<String, Object> details = new HashMap<>();
                details.put("service", "External Service");
                details.put("endpoint", "https://external.api.com");
                details.put("responseTime", "100ms");
                details.put("timestamp", LocalDateTime.now());

                return Health.up().withDetails(details).build();
            });
        }
    }
}
