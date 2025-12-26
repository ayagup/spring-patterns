package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Composite Health Indicator Pattern - Aggregate Multiple Health Checks
 * 
 * Purpose:
 * - Combine multiple health indicators
 * - Aggregate health status
 * - Hierarchical health structure
 * - Custom status aggregation logic
 * - Group related health checks
 * 
 * Use Cases:
 * - System-wide health aggregation
 * - Subsystem health grouping
 * - Custom health hierarchies
 * - Conditional health aggregation
 * - Weighted health calculation
 * - Dependencies health tracking
 * 
 * Status Aggregation:
 * Default order (worst to best):
 * 1. DOWN
 * 2. OUT_OF_SERVICE
 * 3. UP
 * 4. UNKNOWN
 * 
 * Custom ordering possible via StatusAggregator
 * 
 * Configuration (application.yml):
 * management:
 *   endpoint:
 *     health:
 *       show-details: always
 *       show-components: always
 *       group:
 *         critical:
 *           include: db,cache
 *           show-details: always
 *         optional:
 *           include: external,reporting
 *           show-details: when-authorized
 * 
 * CompositeHealthContributor:
 * - Aggregates multiple HealthContributors
 * - Hierarchical structure support
 * - Iterator over contributors
 * - Named contributor access
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * 
 * Health Aggregation Strategies:
 * - SimpleStatusAggregator: Default order-based
 * - Custom StatusAggregator: Custom logic
 * - Weighted aggregation: Priority-based
 * - Majority voting: Democratic decision
 * 
 * Warnings:
 * - Composite checks take longer
 * - One failing check affects overall status
 * - Consider parallel execution
 * - Set appropriate timeouts
 * - Monitor aggregation performance
 * - Avoid circular dependencies
 * 
 * Best Practices:
 * - Group related health checks
 * - Use meaningful names
 * - Implement custom aggregation when needed
 * - Consider health check criticality
 * - Cache composite results
 * - Log aggregation decisions
 * - Test aggregation logic
 * - Document health hierarchy
 * - Use health groups for different audiences
 * - Monitor composite health performance
 */
@SpringBootApplication
public class CompositeHealthIndicatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(CompositeHealthIndicatorPattern.class, args);
    }

    // ============================================
    // Example 1: Basic Composite Health Indicator
    // ============================================
    
    @Component("system")
    public static class SystemHealthIndicator implements CompositeHealthContributor {
        
        private final Map<String, HealthContributor> contributors = new LinkedHashMap<>();
        
        public SystemHealthIndicator() {
            // Add component health indicators
            contributors.put("application", new ApplicationHealthIndicator());
            contributors.put("infrastructure", new InfrastructureHealthIndicator());
            contributors.put("external", new ExternalHealthIndicator());
        }
        
        @Override
        public HealthContributor getContributor(String name) {
            return contributors.get(name);
        }
        
        @Override
        public Iterator<NamedContributor<HealthContributor>> iterator() {
            return contributors.entrySet().stream()
                .map(entry -> NamedContributor.of(entry.getKey(), entry.getValue()))
                .iterator();
        }
        
        // Individual health indicators
        private static class ApplicationHealthIndicator implements HealthIndicator {
            @Override
            public Health health() {
                return Health.up()
                    .withDetail("component", "application")
                    .withDetail("version", "1.0.0")
                    .build();
            }
        }
        
        private static class InfrastructureHealthIndicator implements HealthIndicator {
            @Override
            public Health health() {
                return Health.up()
                    .withDetail("component", "infrastructure")
                    .withDetail("loadBalancer", "UP")
                    .withDetail("storage", "UP")
                    .build();
            }
        }
        
        private static class ExternalHealthIndicator implements HealthIndicator {
            @Override
            public Health health() {
                return Health.up()
                    .withDetail("component", "external")
                    .withDetail("api", "UP")
                    .withDetail("partners", "UP")
                    .build();
            }
        }
    }

    // ============================================
    // Example 2: Database Cluster Composite Health
    // ============================================
    
    @Component("databaseCluster")
    public static class DatabaseClusterHealthIndicator implements CompositeHealthContributor {
        
        private final Map<String, HealthContributor> contributors = new LinkedHashMap<>();
        
        public DatabaseClusterHealthIndicator() {
            // Multiple database instances
            contributors.put("primary", new DatabaseInstanceHealth("primary", true));
            contributors.put("replica1", new DatabaseInstanceHealth("replica1", false));
            contributors.put("replica2", new DatabaseInstanceHealth("replica2", false));
        }
        
        @Override
        public HealthContributor getContributor(String name) {
            return contributors.get(name);
        }
        
        @Override
        public Iterator<NamedContributor<HealthContributor>> iterator() {
            return contributors.entrySet().stream()
                .map(entry -> NamedContributor.of(entry.getKey(), entry.getValue()))
                .iterator();
        }
        
        private static class DatabaseInstanceHealth implements HealthIndicator {
            private final String instanceName;
            private final boolean isPrimary;
            
            public DatabaseInstanceHealth(String instanceName, boolean isPrimary) {
                this.instanceName = instanceName;
                this.isPrimary = isPrimary;
            }
            
            @Override
            public Health health() {
                boolean connected = Math.random() > 0.05; // 95% success rate
                int connections = (int) (Math.random() * 50);
                
                if (!connected) {
                    return Health.down()
                        .withDetail("instance", instanceName)
                        .withDetail("primary", isPrimary)
                        .withDetail("error", "Connection failed")
                        .build();
                }
                
                return Health.up()
                    .withDetail("instance", instanceName)
                    .withDetail("primary", isPrimary)
                    .withDetail("connections", connections)
                    .withDetail("replicationLag", isPrimary ? 0 : (int) (Math.random() * 100) + "ms")
                    .build();
            }
        }
    }

    // ============================================
    // Example 3: Microservices Composite Health
    // ============================================
    
    @Component("microservices")
    public static class MicroservicesHealthIndicator implements CompositeHealthContributor {
        
        private final Map<String, HealthContributor> contributors = new LinkedHashMap<>();
        
        public MicroservicesHealthIndicator() {
            // Different microservices
            contributors.put("userService", new ServiceHealthIndicator("user-service", "http://user-service:8080"));
            contributors.put("orderService", new ServiceHealthIndicator("order-service", "http://order-service:8080"));
            contributors.put("paymentService", new ServiceHealthIndicator("payment-service", "http://payment-service:8080"));
            contributors.put("inventoryService", new ServiceHealthIndicator("inventory-service", "http://inventory-service:8080"));
        }
        
        @Override
        public HealthContributor getContributor(String name) {
            return contributors.get(name);
        }
        
        @Override
        public Iterator<NamedContributor<HealthContributor>> iterator() {
            return contributors.entrySet().stream()
                .map(entry -> NamedContributor.of(entry.getKey(), entry.getValue()))
                .iterator();
        }
        
        private static class ServiceHealthIndicator implements HealthIndicator {
            private final String serviceName;
            private final String serviceUrl;
            
            public ServiceHealthIndicator(String serviceName, String serviceUrl) {
                this.serviceName = serviceName;
                this.serviceUrl = serviceUrl;
            }
            
            @Override
            public Health health() {
                boolean available = Math.random() > 0.1; // 90% availability
                
                if (available) {
                    return Health.up()
                        .withDetail("service", serviceName)
                        .withDetail("url", serviceUrl)
                        .withDetail("responseTime", (int) (Math.random() * 200) + "ms")
                        .withDetail("instances", (int) (Math.random() * 3) + 1)
                        .build();
                } else {
                    return Health.down()
                        .withDetail("service", serviceName)
                        .withDetail("url", serviceUrl)
                        .withDetail("error", "Service unavailable")
                        .build();
                }
            }
        }
    }

    // ============================================
    // Example 4: Custom Status Aggregator
    // ============================================
    
    @Component
    public static class CustomStatusAggregator implements StatusAggregator {
        
        // Custom status ordering (worst to best)
        private static final List<String> STATUS_ORDER = Arrays.asList(
            "DOWN",
            "OUT_OF_SERVICE",
            "DEGRADED",
            "UNKNOWN",
            "UP"
        );
        
        @Override
        public Status getAggregateStatus(Set<Status> statuses) {
            // Return worst status based on custom order
            return statuses.stream()
                .min(Comparator.comparingInt(status -> 
                    STATUS_ORDER.indexOf(status.getCode())))
                .orElse(Status.UNKNOWN);
        }
    }

    // ============================================
    // Example 5: Weighted Health Aggregator
    // ============================================
    
    @Component("weightedSystem")
    public static class WeightedHealthIndicator implements HealthIndicator {
        
        private final Map<String, Integer> componentWeights = new HashMap<>();
        private final Map<String, HealthIndicator> components = new HashMap<>();
        
        public WeightedHealthIndicator() {
            // Initialize components with weights
            components.put("database", new ComponentHealth("database"));
            componentWeights.put("database", 10); // Critical
            
            components.put("cache", new ComponentHealth("cache"));
            componentWeights.put("cache", 7); // Important
            
            components.put("reporting", new ComponentHealth("reporting"));
            componentWeights.put("reporting", 3); // Optional
        }
        
        @Override
        public Health health() {
            Map<String, Health> healthChecks = new HashMap<>();
            int totalWeight = 0;
            int upWeight = 0;
            
            for (Map.Entry<String, HealthIndicator> entry : components.entrySet()) {
                String name = entry.getKey();
                Health health = entry.getValue().health();
                healthChecks.put(name, health);
                
                int weight = componentWeights.get(name);
                totalWeight += weight;
                
                if (Status.UP.equals(health.getStatus())) {
                    upWeight += weight;
                }
            }
            
            double healthPercentage = (double) upWeight / totalWeight * 100;
            
            Map<String, Object> details = new HashMap<>();
            details.put("totalWeight", totalWeight);
            details.put("upWeight", upWeight);
            details.put("healthPercentage", String.format("%.1f%%", healthPercentage));
            details.put("components", healthChecks);
            
            Status overallStatus;
            if (healthPercentage >= 90) {
                overallStatus = Status.UP;
            } else if (healthPercentage >= 70) {
                overallStatus = new Status("DEGRADED");
            } else {
                overallStatus = Status.DOWN;
            }
            
            return Health.status(overallStatus)
                .withDetails(details)
                .build();
        }
        
        private static class ComponentHealth implements HealthIndicator {
            private final String name;
            
            public ComponentHealth(String name) {
                this.name = name;
            }
            
            @Override
            public Health health() {
                boolean healthy = Math.random() > 0.2; // 80% success
                return healthy ? Health.up().withDetail("component", name).build()
                              : Health.down().withDetail("component", name).build();
            }
        }
    }

    // ============================================
    // Example 6: Conditional Health Aggregator
    // ============================================
    
    @Component("conditional")
    public static class ConditionalHealthIndicator implements HealthIndicator {
        
        private boolean productionMode = true;
        
        @Override
        public Health health() {
            List<Health> criticalChecks = performCriticalChecks();
            List<Health> optionalChecks = performOptionalChecks();
            
            // Check critical components
            boolean criticalHealthy = criticalChecks.stream()
                .allMatch(h -> Status.UP.equals(h.getStatus()));
            
            if (!criticalHealthy) {
                return Health.down()
                    .withDetail("critical", "One or more critical components down")
                    .withDetail("criticalChecks", criticalChecks)
                    .build();
            }
            
            // In production, also check optional components
            if (productionMode) {
                boolean optionalHealthy = optionalChecks.stream()
                    .allMatch(h -> Status.UP.equals(h.getStatus()));
                
                if (!optionalHealthy) {
                    return Health.status("DEGRADED")
                        .withDetail("message", "Optional components degraded")
                        .withDetail("criticalChecks", criticalChecks)
                        .withDetail("optionalChecks", optionalChecks)
                        .build();
                }
            }
            
            return Health.up()
                .withDetail("criticalChecks", criticalChecks)
                .withDetail("optionalChecks", optionalChecks)
                .build();
        }
        
        private List<Health> performCriticalChecks() {
            return Arrays.asList(
                Health.up().withDetail("type", "database").build(),
                Health.up().withDetail("type", "authentication").build()
            );
        }
        
        private List<Health> performOptionalChecks() {
            return Arrays.asList(
                Health.up().withDetail("type", "analytics").build(),
                Health.up().withDetail("type", "recommendations").build()
            );
        }
        
        public void setProductionMode(boolean production) {
            this.productionMode = production;
        }
    }

    // ============================================
    // Example 7: Hierarchical Composite Health
    // ============================================
    
    @Component("enterprise")
    public static class EnterpriseHealthIndicator implements CompositeHealthContributor {
        
        private final Map<String, HealthContributor> contributors = new LinkedHashMap<>();
        
        public EnterpriseHealthIndicator() {
            // Create hierarchy: Enterprise -> Regions -> Services
            contributors.put("usEast", new RegionHealthContributor("us-east"));
            contributors.put("usWest", new RegionHealthContributor("us-west"));
            contributors.put("europe", new RegionHealthContributor("europe"));
        }
        
        @Override
        public HealthContributor getContributor(String name) {
            return contributors.get(name);
        }
        
        @Override
        public Iterator<NamedContributor<HealthContributor>> iterator() {
            return contributors.entrySet().stream()
                .map(entry -> NamedContributor.of(entry.getKey(), entry.getValue()))
                .iterator();
        }
        
        private static class RegionHealthContributor implements CompositeHealthContributor {
            private final Map<String, HealthContributor> services = new LinkedHashMap<>();
            
            public RegionHealthContributor(String region) {
                services.put("api", new SimpleHealthIndicator(region + "-api"));
                services.put("database", new SimpleHealthIndicator(region + "-database"));
                services.put("cache", new SimpleHealthIndicator(region + "-cache"));
            }
            
            @Override
            public HealthContributor getContributor(String name) {
                return services.get(name);
            }
            
            @Override
            public Iterator<NamedContributor<HealthContributor>> iterator() {
                return services.entrySet().stream()
                    .map(entry -> NamedContributor.of(entry.getKey(), entry.getValue()))
                    .iterator();
            }
        }
        
        private static class SimpleHealthIndicator implements HealthIndicator {
            private final String name;
            
            public SimpleHealthIndicator(String name) {
                this.name = name;
            }
            
            @Override
            public Health health() {
                boolean healthy = Math.random() > 0.1; // 90% healthy
                return healthy ? Health.up().withDetail("service", name).build()
                              : Health.down().withDetail("service", name).build();
            }
        }
    }

    // ============================================
    // Example 8: Dependency Health Tracker
    // ============================================
    
    @Component("dependencies")
    public static class DependencyHealthIndicator implements HealthIndicator {
        
        private final Map<String, DependencyInfo> dependencies = new HashMap<>();
        
        public DependencyHealthIndicator() {
            dependencies.put("database", new DependencyInfo("database", true));
            dependencies.put("cache", new DependencyInfo("cache", false));
            dependencies.put("messageQueue", new DependencyInfo("messageQueue", false));
            dependencies.put("externalApi", new DependencyInfo("externalApi", false));
        }
        
        @Override
        public Health health() {
            Map<String, Object> details = new HashMap<>();
            List<String> criticalDown = new ArrayList<>();
            List<String> optionalDown = new ArrayList<>();
            
            for (Map.Entry<String, DependencyInfo> entry : dependencies.entrySet()) {
                String name = entry.getKey();
                DependencyInfo info = entry.getValue();
                boolean healthy = Math.random() > 0.1; // 90% healthy
                
                Map<String, Object> depDetails = new HashMap<>();
                depDetails.put("critical", info.critical);
                depDetails.put("status", healthy ? "UP" : "DOWN");
                details.put(name, depDetails);
                
                if (!healthy) {
                    if (info.critical) {
                        criticalDown.add(name);
                    } else {
                        optionalDown.add(name);
                    }
                }
            }
            
            if (!criticalDown.isEmpty()) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("criticalDependenciesDown", criticalDown)
                    .build();
            } else if (!optionalDown.isEmpty()) {
                return Health.status("DEGRADED")
                    .withDetails(details)
                    .withDetail("optionalDependenciesDown", optionalDown)
                    .build();
            }
            
            return Health.up()
                .withDetails(details)
                .build();
        }
        
        private static class DependencyInfo {
            String name;
            boolean critical;
            
            public DependencyInfo(String name, boolean critical) {
                this.name = name;
                this.critical = critical;
            }
        }
    }

    // ============================================
    // Example 9: Composite Health REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/composite-health")
    public static class CompositeHealthController {
        
        private final ConditionalHealthIndicator conditionalHealth;
        
        public CompositeHealthController(ConditionalHealthIndicator conditionalHealth) {
            this.conditionalHealth = conditionalHealth;
        }
        
        @PostMapping("/production-mode")
        public Map<String, String> setProductionMode(@RequestParam boolean production) {
            conditionalHealth.setProductionMode(production);
            return Collections.singletonMap("status", 
                "Production mode set to: " + production);
        }
        
        @GetMapping("/info")
        public Map<String, Object> getHealthInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("compositeHealthSupported", true);
            info.put("customAggregationAvailable", true);
            info.put("hierarchicalHealthSupported", true);
            info.put("weightedAggregationSupported", true);
            return info;
        }
    }
}
