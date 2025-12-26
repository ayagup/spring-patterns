package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Readiness State Pattern - Kubernetes Readiness Probe Support
 * 
 * Purpose:
 * - Kubernetes readiness probe integration
 * - Traffic routing control
 * - Graceful service degradation
 * - Temporary issue handling
 * - Load balancer integration
 * 
 * Use Cases:
 * - Kubernetes deployments
 * - Rolling updates
 * - Temporary outages
 * - External dependency failures
 * - Maintenance mode
 * - Warm-up periods
 * 
 * Readiness States:
 * - ACCEPTING_TRAFFIC: Ready to receive requests
 * - REFUSING_TRAFFIC: Temporarily unavailable
 * 
 * Configuration (application.yml):
 * management:
 *   endpoint:
 *     health:
 *       probes:
 *         enabled: true
 *       group:
 *         readiness:
 *           include: readinessState,db,cache
 * 
 * Kubernetes Configuration:
 * readinessProbe:
 *   httpGet:
 *     path: /actuator/health/readiness
 *     port: 8080
 *   initialDelaySeconds: 10
 *   periodSeconds: 5
 *   failureThreshold: 3
 * 
 * When to Use REFUSING_TRAFFIC:
 * - Database temporarily down
 * - Cache unavailable
 * - External API slow
 * - Maintenance mode
 * - Application warming up
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * 
 * Warnings:
 * - Don't confuse with liveness
 * - REFUSING_TRAFFIC stops traffic
 * - Test readiness logic
 * - Monitor probe failures
 * - Consider probe timing
 * - Document readiness criteria
 * 
 * Best Practices:
 * - Use for temporary issues
 * - Set appropriate delays
 * - Monitor readiness state
 * - Test probe configuration
 * - Document traffic criteria
 * - Log state changes
 * - Handle graceful degradation
 * - Implement retry logic
 * - Consider startup time
 * - Test rolling updates
 */
@SpringBootApplication
public class ReadinessStatePattern {

    public static void main(String[] args) {
        SpringApplication.run(ReadinessStatePattern.class, args);
    }

    // ============================================
    // Example 1: Readiness State Manager
    // ============================================
    
    @Component
    public static class ReadinessStateManager {
        
        private final ApplicationAvailability availability;
        private final ApplicationEventPublisher eventPublisher;
        private final ApplicationContext context;
        
        public ReadinessStateManager(
                ApplicationAvailability availability,
                ApplicationEventPublisher eventPublisher,
                ApplicationContext context) {
            this.availability = availability;
            this.eventPublisher = eventPublisher;
            this.context = context;
        }
        
        public ReadinessState getCurrentState() {
            return availability.getReadinessState();
        }
        
        public void acceptTraffic() {
            System.out.println("Accepting traffic");
            AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
        }
        
        public void refuseTraffic(String reason) {
            System.out.println("Refusing traffic: " + reason);
            AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC);
        }
    }

    // ============================================
    // Example 2: Database Readiness Check
    // ============================================
    
    @Component
    public static class DatabaseReadinessCheck implements HealthIndicator {
        
        private final ReadinessStateManager readinessManager;
        private boolean databaseReady = true;
        
        public DatabaseReadinessCheck(ReadinessStateManager readinessManager) {
            this.readinessManager = readinessManager;
        }
        
        @Override
        public Health health() {
            if (checkDatabaseConnection()) {
                databaseReady = true;
                return Health.up()
                    .withDetail("database", "ready")
                    .withDetail("connections", 10)
                    .withDetail("responseTime", "15ms")
                    .build();
            } else {
                databaseReady = false;
                readinessManager.refuseTraffic("Database not ready");
                
                return Health.down()
                    .withDetail("database", "not ready")
                    .withDetail("reason", "Connection failed")
                    .withDetail("action", "Traffic will be refused")
                    .build();
            }
        }
        
        private boolean checkDatabaseConnection() {
            // Simulate database check
            return Math.random() > 0.1; // 90% success
        }
        
        public void setDatabaseReady(boolean ready) {
            this.databaseReady = ready;
        }
    }

    // ============================================
    // Example 3: Cache Readiness Check
    // ============================================
    
    @Component
    public static class CacheReadinessCheck implements HealthIndicator {
        
        private final ReadinessStateManager readinessManager;
        private boolean cacheReady = true;
        
        public CacheReadinessCheck(ReadinessStateManager readinessManager) {
            this.readinessManager = readinessManager;
        }
        
        @Override
        public Health health() {
            if (checkCacheAvailability()) {
                cacheReady = true;
                return Health.up()
                    .withDetail("cache", "ready")
                    .withDetail("type", "Redis")
                    .withDetail("hitRatio", "85%")
                    .build();
            } else {
                cacheReady = false;
                readinessManager.refuseTraffic("Cache not ready");
                
                return Health.down()
                    .withDetail("cache", "not ready")
                    .withDetail("reason", "Cache unavailable")
                    .withDetail("action", "Traffic will be refused")
                    .build();
            }
        }
        
        private boolean checkCacheAvailability() {
            // Simulate cache check
            return Math.random() > 0.05; // 95% success
        }
    }

    // ============================================
    // Example 4: External Service Readiness Check
    // ============================================
    
    @Component
    public static class ExternalServiceReadinessCheck implements HealthIndicator {
        
        private final ReadinessStateManager readinessManager;
        private int failureCount = 0;
        private static final int FAILURE_THRESHOLD = 3;
        
        public ExternalServiceReadinessCheck(ReadinessStateManager readinessManager) {
            this.readinessManager = readinessManager;
        }
        
        @Override
        public Health health() {
            if (checkExternalService()) {
                failureCount = 0; // Reset on success
                return Health.up()
                    .withDetail("externalService", "ready")
                    .withDetail("url", "https://api.example.com")
                    .withDetail("responseTime", "120ms")
                    .build();
            } else {
                failureCount++;
                
                if (failureCount >= FAILURE_THRESHOLD) {
                    readinessManager.refuseTraffic("External service not ready");
                    
                    return Health.down()
                        .withDetail("externalService", "not ready")
                        .withDetail("failureCount", failureCount)
                        .withDetail("action", "Traffic will be refused")
                        .build();
                }
                
                return Health.status("DEGRADED")
                    .withDetail("externalService", "degraded")
                    .withDetail("failureCount", failureCount)
                    .withDetail("threshold", FAILURE_THRESHOLD)
                    .build();
            }
        }
        
        private boolean checkExternalService() {
            // Simulate external service check
            return Math.random() > 0.2; // 80% success
        }
    }

    // ============================================
    // Example 5: Warm-up Readiness Check
    // ============================================
    
    @Component
    public static class WarmupReadinessCheck implements HealthIndicator {
        
        private final ReadinessStateManager readinessManager;
        private final Instant startTime = Instant.now();
        private static final long WARMUP_DURATION_SECONDS = 30;
        private boolean warmupComplete = false;
        
        public WarmupReadinessCheck(ReadinessStateManager readinessManager) {
            this.readinessManager = readinessManager;
        }
        
        @Override
        public Health health() {
            long elapsedSeconds = Instant.now().getEpochSecond() - startTime.getEpochSecond();
            
            if (elapsedSeconds < WARMUP_DURATION_SECONDS) {
                long remainingSeconds = WARMUP_DURATION_SECONDS - elapsedSeconds;
                readinessManager.refuseTraffic("Application warming up");
                
                return Health.down()
                    .withDetail("warmup", "in progress")
                    .withDetail("elapsed", elapsedSeconds + "s")
                    .withDetail("remaining", remainingSeconds + "s")
                    .withDetail("action", "Traffic refused during warm-up")
                    .build();
            }
            
            if (!warmupComplete) {
                warmupComplete = true;
                readinessManager.acceptTraffic();
            }
            
            return Health.up()
                .withDetail("warmup", "complete")
                .withDetail("ready", true)
                .build();
        }
    }

    // ============================================
    // Example 6: Maintenance Mode Manager
    // ============================================
    
    @Component
    public static class MaintenanceModeManager implements HealthIndicator {
        
        private final ReadinessStateManager readinessManager;
        private boolean maintenanceMode = false;
        private String maintenanceReason = "";
        
        public MaintenanceModeManager(ReadinessStateManager readinessManager) {
            this.readinessManager = readinessManager;
        }
        
        @Override
        public Health health() {
            if (maintenanceMode) {
                readinessManager.refuseTraffic("Maintenance mode active");
                
                return Health.down()
                    .withDetail("maintenanceMode", true)
                    .withDetail("reason", maintenanceReason)
                    .withDetail("action", "Traffic refused during maintenance")
                    .build();
            }
            
            return Health.up()
                .withDetail("maintenanceMode", false)
                .build();
        }
        
        public void enableMaintenanceMode(String reason) {
            this.maintenanceMode = true;
            this.maintenanceReason = reason;
            readinessManager.refuseTraffic("Maintenance: " + reason);
        }
        
        public void disableMaintenanceMode() {
            this.maintenanceMode = false;
            this.maintenanceReason = "";
            readinessManager.acceptTraffic();
        }
    }

    // ============================================
    // Example 7: Readiness State REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/readiness")
    public static class ReadinessController {
        
        private final ReadinessStateManager readinessManager;
        private final DatabaseReadinessCheck databaseCheck;
        private final CacheReadinessCheck cacheCheck;
        private final MaintenanceModeManager maintenanceManager;
        
        public ReadinessController(
                ReadinessStateManager readinessManager,
                DatabaseReadinessCheck databaseCheck,
                CacheReadinessCheck cacheCheck,
                MaintenanceModeManager maintenanceManager) {
            this.readinessManager = readinessManager;
            this.databaseCheck = databaseCheck;
            this.cacheCheck = cacheCheck;
            this.maintenanceManager = maintenanceManager;
        }
        
        @GetMapping("/state")
        public Map<String, Object> getCurrentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("readinessState", readinessManager.getCurrentState().toString());
            state.put("timestamp", Instant.now());
            return state;
        }
        
        @PostMapping("/accept-traffic")
        public Map<String, String> acceptTraffic() {
            readinessManager.acceptTraffic();
            return Collections.singletonMap("status", 
                "Application accepting traffic");
        }
        
        @PostMapping("/refuse-traffic")
        public Map<String, String> refuseTraffic(@RequestParam String reason) {
            readinessManager.refuseTraffic(reason);
            return Collections.singletonMap("status", 
                "Application refusing traffic: " + reason);
        }
        
        @PostMapping("/maintenance/enable")
        public Map<String, String> enableMaintenance(@RequestParam String reason) {
            maintenanceManager.enableMaintenanceMode(reason);
            return Collections.singletonMap("status", 
                "Maintenance mode enabled: " + reason);
        }
        
        @PostMapping("/maintenance/disable")
        public Map<String, String> disableMaintenance() {
            maintenanceManager.disableMaintenanceMode();
            return Collections.singletonMap("status", 
                "Maintenance mode disabled");
        }
        
        @GetMapping("/info")
        public Map<String, Object> getReadinessInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("description", "Readiness probe for Kubernetes");
            info.put("endpoint", "/actuator/health/readiness");
            info.put("states", Arrays.asList("ACCEPTING_TRAFFIC", "REFUSING_TRAFFIC"));
            info.put("purpose", "Control traffic routing to pod");
            info.put("action", "Removes pod from load balancer when REFUSING_TRAFFIC");
            
            Map<String, String> config = new HashMap<>();
            config.put("initialDelaySeconds", "10");
            config.put("periodSeconds", "5");
            config.put("failureThreshold", "3");
            info.put("recommendedConfig", config);
            
            return info;
        }
    }
}
