package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Custom Health Check Pattern - Application-Specific Health Checks
 * 
 * Purpose:
 * - Domain-specific health validation
 * - Business logic health checks
 * - Custom component monitoring
 * - Application state verification
 * - Feature-specific health
 * 
 * Use Cases:
 * - License validation
 * - Feature flag checks
 * - Business rule validation
 * - Configuration verification
 * - Resource availability
 * - Integration health
 * 
 * Implementation:
 * Implement HealthIndicator interface with custom logic
 * 
 * Configuration (application.yml):
 * management:
 *   endpoint:
 *     health:
 *       show-details: always
 *   health:
 *     custom:
 *       enabled: true
 * 
 * Custom Health Criteria:
 * - License expiration
 * - API quota remaining
 * - Batch job status
 * - Scheduled task health
 * - Integration status
 * - Configuration validity
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * 
 * Best Practices:
 * - Keep checks fast (<1 second)
 * - Return meaningful details
 * - Use appropriate status codes
 * - Handle errors gracefully
 * - Cache when possible
 * - Document health criteria
 * - Test thoroughly
 * - Monitor check performance
 * - Log failures
 * - Provide actionable information
 */
@SpringBootApplication
public class CustomHealthCheckPattern {

    public static void main(String[] args) {
        SpringApplication.run(CustomHealthCheckPattern.class, args);
    }

    // ============================================
    // Example 1: License Health Check
    // ============================================
    
    @Component("license")
    public static class LicenseHealthIndicator implements HealthIndicator {
        
        private Instant licenseExpiration = Instant.now().plusSeconds(86400 * 30); // 30 days
        
        @Override
        public Health health() {
            Instant now = Instant.now();
            long daysUntilExpiration = (licenseExpiration.getEpochSecond() - now.getEpochSecond()) / 86400;
            
            if (now.isAfter(licenseExpiration)) {
                return Health.down()
                    .withDetail("license", "expired")
                    .withDetail("expiredOn", licenseExpiration)
                    .withDetail("action", "Renew license immediately")
                    .build();
            } else if (daysUntilExpiration <= 7) {
                return Health.status("WARNING")
                    .withDetail("license", "expiring soon")
                    .withDetail("daysRemaining", daysUntilExpiration)
                    .withDetail("expiresOn", licenseExpiration)
                    .build();
            }
            
            return Health.up()
                .withDetail("license", "valid")
                .withDetail("daysRemaining", daysUntilExpiration)
                .withDetail("expiresOn", licenseExpiration)
                .build();
        }
        
        public void setLicenseExpiration(Instant expiration) {
            this.licenseExpiration = expiration;
        }
    }

    // ============================================
    // Example 2: API Quota Health Check
    // ============================================
    
    @Component("apiQuota")
    public static class ApiQuotaHealthIndicator implements HealthIndicator {
        
        private int usedQuota = 7500;
        private final int totalQuota = 10000;
        
        @Override
        public Health health() {
            double usagePercent = (double) usedQuota / totalQuota * 100;
            int remaining = totalQuota - usedQuota;
            
            if (usagePercent >= 100) {
                return Health.down()
                    .withDetail("quota", "exhausted")
                    .withDetail("used", usedQuota)
                    .withDetail("total", totalQuota)
                    .withDetail("remaining", 0)
                    .build();
            } else if (usagePercent >= 90) {
                return Health.status("WARNING")
                    .withDetail("quota", "critical")
                    .withDetail("used", usedQuota)
                    .withDetail("total", totalQuota)
                    .withDetail("remaining", remaining)
                    .withDetail("usagePercent", String.format("%.1f%%", usagePercent))
                    .build();
            } else if (usagePercent >= 75) {
                return Health.status("DEGRADED")
                    .withDetail("quota", "high usage")
                    .withDetail("used", usedQuota)
                    .withDetail("remaining", remaining)
                    .withDetail("usagePercent", String.format("%.1f%%", usagePercent))
                    .build();
            }
            
            return Health.up()
                .withDetail("quota", "available")
                .withDetail("used", usedQuota)
                .withDetail("remaining", remaining)
                .withDetail("usagePercent", String.format("%.1f%%", usagePercent))
                .build();
        }
        
        public void setUsedQuota(int used) {
            this.usedQuota = Math.min(used, totalQuota);
        }
    }

    // ============================================
    // Example 3: Batch Job Health Check
    // ============================================
    
    @Component("batchJob")
    public static class BatchJobHealthIndicator implements HealthIndicator {
        
        private String lastJobStatus = "COMPLETED";
        private Instant lastRunTime = Instant.now().minusSeconds(3600);
        private int failureCount = 0;
        
        @Override
        public Health health() {
            long hoursSinceLastRun = (Instant.now().getEpochSecond() - lastRunTime.getEpochSecond()) / 3600;
            
            if ("FAILED".equals(lastJobStatus) && failureCount >= 3) {
                return Health.down()
                    .withDetail("batchJob", "failed")
                    .withDetail("status", lastJobStatus)
                    .withDetail("failureCount", failureCount)
                    .withDetail("lastRun", lastRunTime)
                    .build();
            } else if (hoursSinceLastRun > 24) {
                return Health.status("WARNING")
                    .withDetail("batchJob", "stale")
                    .withDetail("lastRun", lastRunTime)
                    .withDetail("hoursSinceLastRun", hoursSinceLastRun)
                    .build();
            } else if ("FAILED".equals(lastJobStatus)) {
                return Health.status("DEGRADED")
                    .withDetail("batchJob", "recent failure")
                    .withDetail("status", lastJobStatus)
                    .withDetail("failureCount", failureCount)
                    .build();
            }
            
            return Health.up()
                .withDetail("batchJob", "healthy")
                .withDetail("status", lastJobStatus)
                .withDetail("lastRun", lastRunTime)
                .withDetail("failureCount", failureCount)
                .build();
        }
        
        public void setJobStatus(String status) {
            this.lastJobStatus = status;
            this.lastRunTime = Instant.now();
            if ("FAILED".equals(status)) {
                failureCount++;
            } else if ("COMPLETED".equals(status)) {
                failureCount = 0;
            }
        }
    }

    // ============================================
    // Example 4: Feature Flag Health Check
    // ============================================
    
    @Component("featureFlags")
    public static class FeatureFlagHealthIndicator implements HealthIndicator {
        
        private final Map<String, Boolean> features = new HashMap<>();
        
        public FeatureFlagHealthIndicator() {
            features.put("newCheckout", true);
            features.put("recommendation", true);
            features.put("socialLogin", false);
            features.put("analytics", true);
        }
        
        @Override
        public Health health() {
            int totalFeatures = features.size();
            long enabledFeatures = features.values().stream().filter(Boolean::booleanValue).count();
            
            Map<String, Object> details = new HashMap<>();
            details.put("totalFeatures", totalFeatures);
            details.put("enabledFeatures", enabledFeatures);
            details.put("disabledFeatures", totalFeatures - enabledFeatures);
            details.put("features", features);
            
            return Health.up()
                .withDetails(details)
                .build();
        }
        
        public void setFeature(String name, boolean enabled) {
            features.put(name, enabled);
        }
    }

    // ============================================
    // Example 5: Configuration Health Check
    // ============================================
    
    @Component("configuration")
    public static class ConfigurationHealthIndicator implements HealthIndicator {
        
        private boolean configValid = true;
        private List<String> missingConfigs = new ArrayList<>();
        
        @Override
        public Health health() {
            // Validate required configurations
            validateConfigurations();
            
            if (!configValid) {
                return Health.down()
                    .withDetail("configuration", "invalid")
                    .withDetail("missingConfigs", missingConfigs)
                    .withDetail("action", "Check application configuration")
                    .build();
            }
            
            return Health.up()
                .withDetail("configuration", "valid")
                .withDetail("timestamp", Instant.now())
                .build();
        }
        
        private void validateConfigurations() {
            missingConfigs.clear();
            // Simulate configuration validation
            configValid = Math.random() > 0.05; // 95% success
            if (!configValid) {
                missingConfigs.add("database.url");
                missingConfigs.add("api.key");
            }
        }
    }

    // ============================================
    // Example 6: Storage Capacity Health Check
    // ============================================
    
    @Component("storageCapacity")
    public static class StorageCapacityHealthIndicator implements HealthIndicator {
        
        private long usedStorage = 800_000_000_000L; // 800GB
        private final long totalStorage = 1_000_000_000_000L; // 1TB
        
        @Override
        public Health health() {
            double usagePercent = (double) usedStorage / totalStorage * 100;
            long available = totalStorage - usedStorage;
            
            if (usagePercent >= 95) {
                return Health.down()
                    .withDetail("storage", "critical")
                    .withDetail("usagePercent", String.format("%.1f%%", usagePercent))
                    .withDetail("available", formatBytes(available))
                    .withDetail("action", "Free up storage immediately")
                    .build();
            } else if (usagePercent >= 85) {
                return Health.status("WARNING")
                    .withDetail("storage", "high usage")
                    .withDetail("usagePercent", String.format("%.1f%%", usagePercent))
                    .withDetail("available", formatBytes(available))
                    .build();
            }
            
            return Health.up()
                .withDetail("storage", "normal")
                .withDetail("usagePercent", String.format("%.1f%%", usagePercent))
                .withDetail("available", formatBytes(available))
                .withDetail("total", formatBytes(totalStorage))
                .build();
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            char pre = "KMGTPE".charAt(exp - 1);
            return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
        }
        
        public void setUsedStorage(long bytes) {
            this.usedStorage = Math.min(bytes, totalStorage);
        }
    }

    // ============================================
    // Example 7: Scheduled Task Health Check
    // ============================================
    
    @Component("scheduledTasks")
    public static class ScheduledTaskHealthIndicator implements HealthIndicator {
        
        private final Map<String, TaskInfo> tasks = new HashMap<>();
        
        public ScheduledTaskHealthIndicator() {
            tasks.put("dataSync", new TaskInfo("RUNNING", Instant.now()));
            tasks.put("cleanup", new TaskInfo("COMPLETED", Instant.now().minusSeconds(300)));
            tasks.put("report", new TaskInfo("COMPLETED", Instant.now().minusSeconds(600)));
        }
        
        @Override
        public Health health() {
            Map<String, Object> details = new HashMap<>();
            boolean allHealthy = true;
            
            for (Map.Entry<String, TaskInfo> entry : tasks.entrySet()) {
                String taskName = entry.getKey();
                TaskInfo info = entry.getValue();
                
                Map<String, Object> taskDetails = new HashMap<>();
                taskDetails.put("status", info.status);
                taskDetails.put("lastRun", info.lastRun);
                
                details.put(taskName, taskDetails);
                
                if ("FAILED".equals(info.status)) {
                    allHealthy = false;
                }
            }
            
            if (!allHealthy) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("message", "One or more tasks failed")
                    .build();
            }
            
            return Health.up()
                .withDetails(details)
                .build();
        }
        
        public void updateTask(String name, String status) {
            tasks.put(name, new TaskInfo(status, Instant.now()));
        }
        
        private static class TaskInfo {
            String status;
            Instant lastRun;
            
            TaskInfo(String status, Instant lastRun) {
                this.status = status;
                this.lastRun = lastRun;
            }
        }
    }

    // ============================================
    // Example 8: Custom Health REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/custom-health")
    public static class CustomHealthController {
        
        private final LicenseHealthIndicator licenseHealth;
        private final ApiQuotaHealthIndicator apiQuotaHealth;
        private final BatchJobHealthIndicator batchJobHealth;
        private final FeatureFlagHealthIndicator featureFlagHealth;
        private final StorageCapacityHealthIndicator storageHealth;
        private final ScheduledTaskHealthIndicator taskHealth;
        
        public CustomHealthController(
                LicenseHealthIndicator licenseHealth,
                ApiQuotaHealthIndicator apiQuotaHealth,
                BatchJobHealthIndicator batchJobHealth,
                FeatureFlagHealthIndicator featureFlagHealth,
                StorageCapacityHealthIndicator storageHealth,
                ScheduledTaskHealthIndicator taskHealth) {
            this.licenseHealth = licenseHealth;
            this.apiQuotaHealth = apiQuotaHealth;
            this.batchJobHealth = batchJobHealth;
            this.featureFlagHealth = featureFlagHealth;
            this.storageHealth = storageHealth;
            this.taskHealth = taskHealth;
        }
        
        @PostMapping("/license/expiration")
        public Map<String, String> setLicenseExpiration(@RequestParam long daysFromNow) {
            licenseHealth.setLicenseExpiration(
                Instant.now().plusSeconds(daysFromNow * 86400));
            return Collections.singletonMap("status", 
                "License expiration updated to " + daysFromNow + " days from now");
        }
        
        @PostMapping("/quota/usage")
        public Map<String, String> setQuotaUsage(@RequestParam int used) {
            apiQuotaHealth.setUsedQuota(used);
            return Collections.singletonMap("status", 
                "API quota usage updated to " + used);
        }
        
        @PostMapping("/batch/status")
        public Map<String, String> setBatchJobStatus(@RequestParam String status) {
            batchJobHealth.setJobStatus(status);
            return Collections.singletonMap("status", 
                "Batch job status updated to " + status);
        }
        
        @PostMapping("/feature/{name}")
        public Map<String, String> setFeatureFlag(
                @PathVariable String name, 
                @RequestParam boolean enabled) {
            featureFlagHealth.setFeature(name, enabled);
            return Collections.singletonMap("status", 
                "Feature " + name + " set to " + enabled);
        }
        
        @PostMapping("/storage/usage")
        public Map<String, String> setStorageUsage(@RequestParam long bytes) {
            storageHealth.setUsedStorage(bytes);
            return Collections.singletonMap("status", 
                "Storage usage updated");
        }
        
        @PostMapping("/task/{name}/status")
        public Map<String, String> setTaskStatus(
                @PathVariable String name,
                @RequestParam String status) {
            taskHealth.updateTask(name, status);
            return Collections.singletonMap("status", 
                "Task " + name + " status updated to " + status);
        }
        
        @GetMapping("/info")
        public Map<String, Object> getCustomHealthInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("customHealthChecks", Arrays.asList(
                "license", "apiQuota", "batchJob", "featureFlags",
                "configuration", "storageCapacity", "scheduledTasks"
            ));
            info.put("description", "Application-specific health checks");
            return info;
        }
    }
}
