package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Health Endpoint Pattern - Custom Health Endpoints
 * 
 * Purpose:
 * - Customize health endpoint behavior
 * - Create custom health endpoints
 * - Expose health via different paths
 * - Add custom health operations
 * - Health endpoint extensions
 * 
 * Use Cases:
 * - Custom health aggregation
 * - Additional health operations
 * - Component-specific health
 * - Administrative health actions
 * - Health history tracking
 * - Custom health formats
 * 
 * Default Health Endpoint:
 * - Path: /actuator/health
 * - Shows: Aggregated health status
 * - Components: Individual health indicators
 * 
 * Configuration (application.yml):
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: health
 *       base-path: /actuator
 *   endpoint:
 *     health:
 *       show-details: always
 *       show-components: always
 *       group:
 *         liveness:
 *           include: livenessState
 *         readiness:
 *           include: readinessState,db
 * 
 * Custom Endpoint Annotations:
 * @Endpoint - Define custom endpoint
 * @ReadOperation - HTTP GET operation
 * @WriteOperation - HTTP POST operation
 * @DeleteOperation - HTTP DELETE operation
 * @Selector - Path variable
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * 
 * Endpoint Features:
 * - Custom operations
 * - Path selectors
 * - Parameter binding
 * - Response customization
 * - JMX exposure
 * - Web exposure
 * 
 * Warnings:
 * - Secure custom endpoints properly
 * - Validate input parameters
 * - Avoid expensive operations
 * - Test endpoint security
 * - Document custom endpoints
 * - Handle errors gracefully
 * 
 * Best Practices:
 * - Follow REST conventions
 * - Use meaningful endpoint IDs
 * - Document endpoint operations
 * - Implement proper security
 * - Return consistent formats
 * - Add operation descriptions
 * - Use selectors for specific resources
 * - Cache expensive operations
 * - Log endpoint access
 * - Monitor endpoint performance
 */
@SpringBootApplication
public class HealthEndpointPattern {

    public static void main(String[] args) {
        SpringApplication.run(HealthEndpointPattern.class, args);
    }

    // ============================================
    // Example 1: Custom Health Endpoint
    // ============================================
    
    @Endpoint(id = "customhealth")
    @Component
    public static class CustomHealthEndpoint {
        
        private final Map<String, HealthIndicator> healthIndicators = new HashMap<>();
        private final List<HealthCheckHistory> history = new ArrayList<>();
        
        public CustomHealthEndpoint() {
            // Register health indicators
            healthIndicators.put("database", new DatabaseHealth());
            healthIndicators.put("cache", new CacheHealth());
            healthIndicators.put("api", new ApiHealth());
        }
        
        @ReadOperation
        public Map<String, Object> health() {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> components = new HashMap<>();
            
            boolean allHealthy = true;
            for (Map.Entry<String, HealthIndicator> entry : healthIndicators.entrySet()) {
                Health health = entry.getValue().health();
                components.put(entry.getKey(), health);
                
                if (!"UP".equals(health.getStatus().getCode())) {
                    allHealthy = false;
                }
            }
            
            result.put("status", allHealthy ? "UP" : "DOWN");
            result.put("components", components);
            result.put("timestamp", Instant.now());
            
            // Record in history
            history.add(new HealthCheckHistory(allHealthy ? "UP" : "DOWN", Instant.now()));
            if (history.size() > 100) {
                history.remove(0); // Keep last 100 entries
            }
            
            return result;
        }
        
        @ReadOperation
        public Map<String, Object> healthByComponent(@Selector String component) {
            HealthIndicator indicator = healthIndicators.get(component);
            if (indicator == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Component not found: " + component);
                return error;
            }
            
            Health health = indicator.health();
            Map<String, Object> result = new HashMap<>();
            result.put("component", component);
            result.put("status", health.getStatus().getCode());
            result.put("details", health.getDetails());
            result.put("timestamp", Instant.now());
            
            return result;
        }
        
        @ReadOperation
        public List<HealthCheckHistory> history() {
            return new ArrayList<>(history);
        }
        
        private static class DatabaseHealth implements HealthIndicator {
            @Override
            public Health health() {
                return Health.up().withDetail("database", "PostgreSQL").build();
            }
        }
        
        private static class CacheHealth implements HealthIndicator {
            @Override
            public Health health() {
                return Health.up().withDetail("cache", "Redis").build();
            }
        }
        
        private static class ApiHealth implements HealthIndicator {
            @Override
            public Health health() {
                return Health.up().withDetail("api", "External").build();
            }
        }
        
        private static class HealthCheckHistory {
            String status;
            Instant timestamp;
            
            HealthCheckHistory(String status, Instant timestamp) {
                this.status = status;
                this.timestamp = timestamp;
            }
            
            public String getStatus() { return status; }
            public Instant getTimestamp() { return timestamp; }
        }
    }

    // ============================================
    // Example 2: Component Health Endpoint
    // ============================================
    
    @Endpoint(id = "componenthealth")
    @Component
    public static class ComponentHealthEndpoint {
        
        private final Map<String, ComponentHealth> components = new HashMap<>();
        
        public ComponentHealthEndpoint() {
            components.put("web", new ComponentHealth("web", true));
            components.put("database", new ComponentHealth("database", true));
            components.put("cache", new ComponentHealth("cache", true));
            components.put("messaging", new ComponentHealth("messaging", true));
        }
        
        @ReadOperation
        public Map<String, Object> getAllComponents() {
            Map<String, Object> result = new HashMap<>();
            
            for (Map.Entry<String, ComponentHealth> entry : components.entrySet()) {
                ComponentHealth comp = entry.getValue();
                Map<String, Object> details = new HashMap<>();
                details.put("enabled", comp.enabled);
                details.put("healthy", comp.enabled && Math.random() > 0.1);
                details.put("lastCheck", Instant.now());
                result.put(entry.getKey(), details);
            }
            
            return result;
        }
        
        @ReadOperation
        public Map<String, Object> getComponent(@Selector String name) {
            ComponentHealth comp = components.get(name);
            if (comp == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Component not found");
                return error;
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("name", comp.name);
            details.put("enabled", comp.enabled);
            details.put("healthy", comp.enabled && Math.random() > 0.1);
            details.put("lastCheck", Instant.now());
            
            return details;
        }
        
        @WriteOperation
        public Map<String, String> enableComponent(@Selector String name) {
            ComponentHealth comp = components.get(name);
            if (comp == null) {
                return Collections.singletonMap("error", "Component not found");
            }
            
            comp.enabled = true;
            return Collections.singletonMap("status", "Component " + name + " enabled");
        }
        
        @WriteOperation
        public Map<String, String> disableComponent(@Selector String name) {
            ComponentHealth comp = components.get(name);
            if (comp == null) {
                return Collections.singletonMap("error", "Component not found");
            }
            
            comp.enabled = false;
            return Collections.singletonMap("status", "Component " + name + " disabled");
        }
        
        private static class ComponentHealth {
            String name;
            boolean enabled;
            
            ComponentHealth(String name, boolean enabled) {
                this.name = name;
                this.enabled = enabled;
            }
        }
    }

    // ============================================
    // Example 3: Health Summary Endpoint
    // ============================================
    
    @Endpoint(id = "healthsummary")
    @Component
    public static class HealthSummaryEndpoint {
        
        @ReadOperation
        public Map<String, Object> getSummary() {
            Map<String, Object> summary = new HashMap<>();
            
            // Overall status
            summary.put("status", "UP");
            summary.put("timestamp", Instant.now());
            
            // Component counts
            Map<String, Integer> counts = new HashMap<>();
            counts.put("total", 10);
            counts.put("up", 9);
            counts.put("down", 1);
            counts.put("unknown", 0);
            summary.put("components", counts);
            
            // Availability percentage
            double availability = (9.0 / 10.0) * 100;
            summary.put("availability", String.format("%.2f%%", availability));
            
            // Recent issues
            List<Map<String, Object>> issues = new ArrayList<>();
            Map<String, Object> issue = new HashMap<>();
            issue.put("component", "externalApi");
            issue.put("status", "DOWN");
            issue.put("since", Instant.now().minusSeconds(300));
            issues.add(issue);
            summary.put("recentIssues", issues);
            
            return summary;
        }
        
        @ReadOperation
        public Map<String, Object> getDetailedSummary() {
            Map<String, Object> summary = new HashMap<>();
            
            // Detailed component status
            List<Map<String, Object>> components = new ArrayList<>();
            
            components.add(createComponentStatus("database", "UP", 99.9, 15));
            components.add(createComponentStatus("cache", "UP", 99.5, 5));
            components.add(createComponentStatus("messaging", "UP", 98.0, 50));
            components.add(createComponentStatus("externalApi", "DOWN", 85.0, 200));
            
            summary.put("components", components);
            summary.put("overallHealth", 95.6);
            summary.put("criticalIssues", 1);
            summary.put("warnings", 0);
            
            return summary;
        }
        
        private Map<String, Object> createComponentStatus(
                String name, String status, double uptime, int avgResponseTime) {
            Map<String, Object> comp = new HashMap<>();
            comp.put("name", name);
            comp.put("status", status);
            comp.put("uptime", uptime);
            comp.put("avgResponseTime", avgResponseTime + "ms");
            return comp;
        }
    }

    // ============================================
    // Example 4: Health Diagnostics Endpoint
    // ============================================
    
    @Endpoint(id = "healthdiagnostics")
    @Component
    public static class HealthDiagnosticsEndpoint {
        
        @ReadOperation
        public Map<String, Object> getDiagnostics() {
            Map<String, Object> diagnostics = new HashMap<>();
            
            // System diagnostics
            diagnostics.put("jvm", getJvmDiagnostics());
            diagnostics.put("os", getOsDiagnostics());
            diagnostics.put("application", getApplicationDiagnostics());
            diagnostics.put("timestamp", Instant.now());
            
            return diagnostics;
        }
        
        @ReadOperation
        public Map<String, Object> getDiagnosticsByType(@Selector String type) {
            switch (type.toLowerCase()) {
                case "jvm":
                    return getJvmDiagnostics();
                case "os":
                    return getOsDiagnostics();
                case "application":
                    return getApplicationDiagnostics();
                default:
                    return Collections.singletonMap("error", "Unknown diagnostic type");
            }
        }
        
        private Map<String, Object> getJvmDiagnostics() {
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> jvm = new HashMap<>();
            jvm.put("maxMemory", formatBytes(runtime.maxMemory()));
            jvm.put("totalMemory", formatBytes(runtime.totalMemory()));
            jvm.put("freeMemory", formatBytes(runtime.freeMemory()));
            jvm.put("processors", runtime.availableProcessors());
            jvm.put("javaVersion", System.getProperty("java.version"));
            return jvm;
        }
        
        private Map<String, Object> getOsDiagnostics() {
            Map<String, Object> os = new HashMap<>();
            os.put("name", System.getProperty("os.name"));
            os.put("version", System.getProperty("os.version"));
            os.put("arch", System.getProperty("os.arch"));
            return os;
        }
        
        private Map<String, Object> getApplicationDiagnostics() {
            Map<String, Object> app = new HashMap<>();
            app.put("uptime", "5d 12h 30m");
            app.put("threads", Thread.activeCount());
            app.put("requests", 1000000);
            app.put("errors", 125);
            app.put("errorRate", "0.0125%");
            return app;
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            char pre = "KMGTPE".charAt(exp - 1);
            return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
        }
    }

    // ============================================
    // Example 5: Health History Endpoint
    // ============================================
    
    @Endpoint(id = "healthhistory")
    @Component
    public static class HealthHistoryEndpoint {
        
        private final List<HealthSnapshot> history = new ArrayList<>();
        
        public HealthHistoryEndpoint() {
            // Initialize with some history
            for (int i = 0; i < 10; i++) {
                history.add(new HealthSnapshot(
                    "UP",
                    Instant.now().minusSeconds(i * 60),
                    95.0 + Math.random() * 5
                ));
            }
        }
        
        @ReadOperation
        public List<HealthSnapshot> getHistory() {
            return new ArrayList<>(history);
        }
        
        @ReadOperation
        public List<HealthSnapshot> getHistoryByPeriod(@Selector String period) {
            Instant cutoff;
            switch (period.toLowerCase()) {
                case "hour":
                    cutoff = Instant.now().minusSeconds(3600);
                    break;
                case "day":
                    cutoff = Instant.now().minusSeconds(86400);
                    break;
                case "week":
                    cutoff = Instant.now().minusSeconds(604800);
                    break;
                default:
                    cutoff = Instant.now().minusSeconds(3600);
            }
            
            final Instant finalCutoff = cutoff;
            return history.stream()
                .filter(snapshot -> snapshot.timestamp.isAfter(finalCutoff))
                .collect(java.util.stream.Collectors.toList());
        }
        
        @WriteOperation
        public Map<String, String> recordSnapshot() {
            HealthSnapshot snapshot = new HealthSnapshot(
                Math.random() > 0.1 ? "UP" : "DOWN",
                Instant.now(),
                90.0 + Math.random() * 10
            );
            history.add(snapshot);
            
            if (history.size() > 1000) {
                history.remove(0);
            }
            
            return Collections.singletonMap("status", "Snapshot recorded");
        }
        
        public static class HealthSnapshot {
            private String status;
            private Instant timestamp;
            private double availability;
            
            public HealthSnapshot(String status, Instant timestamp, double availability) {
                this.status = status;
                this.timestamp = timestamp;
                this.availability = availability;
            }
            
            public String getStatus() { return status; }
            public Instant getTimestamp() { return timestamp; }
            public double getAvailability() { return availability; }
        }
    }

    // ============================================
    // Example 6: Health Actions Endpoint
    // ============================================
    
    @Endpoint(id = "healthactions")
    @Component
    public static class HealthActionsEndpoint {
        
        @WriteOperation
        public Map<String, String> refreshHealth() {
            // Trigger health check refresh
            return Collections.singletonMap("status", "Health checks refreshed");
        }
        
        @WriteOperation
        public Map<String, String> clearCache() {
            // Clear health check cache
            return Collections.singletonMap("status", "Health cache cleared");
        }
        
        @WriteOperation
        public Map<String, String> resetCounters() {
            // Reset health counters
            return Collections.singletonMap("status", "Health counters reset");
        }
        
        @ReadOperation
        public Map<String, Object> getAvailableActions() {
            Map<String, Object> actions = new HashMap<>();
            
            List<String> writeActions = Arrays.asList(
                "refreshHealth",
                "clearCache",
                "resetCounters"
            );
            
            actions.put("writeOperations", writeActions);
            actions.put("description", "Available health management actions");
            
            return actions;
        }
    }

    // ============================================
    // Example 7: Health Endpoint REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/health-endpoints")
    public static class HealthEndpointController {
        
        @GetMapping("/info")
        public Map<String, Object> getEndpointInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("customHealthEndpoint", "/actuator/customhealth");
            info.put("componentHealthEndpoint", "/actuator/componenthealth");
            info.put("healthSummaryEndpoint", "/actuator/healthsummary");
            info.put("healthDiagnosticsEndpoint", "/actuator/healthdiagnostics");
            info.put("healthHistoryEndpoint", "/actuator/healthhistory");
            info.put("healthActionsEndpoint", "/actuator/healthactions");
            return info;
        }
    }
}
