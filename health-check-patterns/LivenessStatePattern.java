package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Liveness State Pattern - Kubernetes Liveness Probe Support
 * 
 * Purpose:
 * - Kubernetes liveness probe integration
 * - Application crash detection
 * - Automatic pod restart trigger
 * - Internal state monitoring
 * - Broken state detection
 * 
 * Use Cases:
 * - Kubernetes deployments
 * - Container health checks
 * - Deadlock detection
 * - Out-of-memory recovery
 * - Application restart triggers
 * - Broken state recovery
 * 
 * Liveness States:
 * - CORRECT: Application running normally
 * - BROKEN: Application in unrecoverable state
 * 
 * Configuration (application.yml):
 * management:
 *   endpoint:
 *     health:
 *       probes:
 *         enabled: true
 *       group:
 *         liveness:
 *           include: livenessState
 * 
 * Kubernetes Configuration:
 * livenessProbe:
 *   httpGet:
 *     path: /actuator/health/liveness
 *     port: 8080
 *   initialDelaySeconds: 30
 *   periodSeconds: 10
 *   failureThreshold: 3
 * 
 * When to Use BROKEN:
 * - Deadlock detected
 * - Critical resource exhausted
 * - Internal corruption
 * - Unrecoverable errors
 * - Memory leaks
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * 
 * Warnings:
 * - Don't use for temporary issues
 * - Setting BROKEN triggers pod restart
 * - Test liveness logic carefully
 * - Avoid false positives
 * - Consider restart impact
 * - Document liveness criteria
 * 
 * Best Practices:
 * - Use for unrecoverable states only
 * - Set appropriate failure thresholds
 * - Monitor liveness checks
 * - Test failure scenarios
 * - Document restart triggers
 * - Log state changes
 * - Use readiness for temporary issues
 * - Implement proper detection logic
 * - Consider startup time
 * - Test in staging environment
 */
@SpringBootApplication
public class LivenessStatePattern {

    public static void main(String[] args) {
        SpringApplication.run(LivenessStatePattern.class, args);
    }

    // Liveness state is automatically managed by Spring Boot
    // Access via ApplicationAvailability

    // ============================================
    // Example 1: Liveness State Monitor
    // ============================================
    
    @Component
    public static class LivenessStateMonitor {
        
        private final ApplicationAvailability availability;
        private final ApplicationEventPublisher eventPublisher;
        private final ApplicationContext context;
        
        public LivenessStateMonitor(
                ApplicationAvailability availability,
                ApplicationEventPublisher eventPublisher,
                ApplicationContext context) {
            this.availability = availability;
            this.eventPublisher = eventPublisher;
            this.context = context;
        }
        
        public LivenessState getCurrentState() {
            return availability.getLivenessState();
        }
        
        public void markAsBroken(String reason) {
            System.out.println("Marking application as BROKEN: " + reason);
            AvailabilityChangeEvent.publish(context, LivenessState.BROKEN);
        }
        
        public void markAsCorrect() {
            System.out.println("Marking application as CORRECT");
            AvailabilityChangeEvent.publish(context, LivenessState.CORRECT);
        }
    }

    // ============================================
    // Example 2: Deadlock Detector
    // ============================================
    
    @Component
    public static class DeadlockDetector implements HealthIndicator {
        
        private final LivenessStateMonitor livenessMonitor;
        private boolean deadlockDetected = false;
        
        public DeadlockDetector(LivenessStateMonitor livenessMonitor) {
            this.livenessMonitor = livenessMonitor;
        }
        
        @Override
        public Health health() {
            if (detectDeadlock()) {
                deadlockDetected = true;
                livenessMonitor.markAsBroken("Deadlock detected");
                
                return Health.down()
                    .withDetail("reason", "Deadlock detected")
                    .withDetail("action", "Pod will be restarted")
                    .withDetail("timestamp", Instant.now())
                    .build();
            }
            
            return Health.up()
                .withDetail("deadlockCheck", "passed")
                .withDetail("timestamp", Instant.now())
                .build();
        }
        
        private boolean detectDeadlock() {
            // Simulate deadlock detection
            return Math.random() < 0.01; // 1% chance
        }
        
        public boolean isDeadlockDetected() {
            return deadlockDetected;
        }
    }

    // ============================================
    // Example 3: Memory Leak Detector
    // ============================================
    
    @Component
    public static class MemoryLeakDetector implements HealthIndicator {
        
        private final LivenessStateMonitor livenessMonitor;
        private static final double MEMORY_THRESHOLD = 0.95; // 95%
        private int consecutiveHighMemory = 0;
        private static final int FAILURE_THRESHOLD = 5;
        
        public MemoryLeakDetector(LivenessStateMonitor livenessMonitor) {
            this.livenessMonitor = livenessMonitor;
        }
        
        @Override
        public Health health() {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            double usageRatio = (double) usedMemory / maxMemory;
            
            if (usageRatio > MEMORY_THRESHOLD) {
                consecutiveHighMemory++;
                
                if (consecutiveHighMemory >= FAILURE_THRESHOLD) {
                    livenessMonitor.markAsBroken("Memory leak detected");
                    
                    return Health.down()
                        .withDetail("reason", "Sustained high memory usage")
                        .withDetail("memoryUsage", String.format("%.1f%%", usageRatio * 100))
                        .withDetail("consecutiveFailures", consecutiveHighMemory)
                        .withDetail("action", "Pod will be restarted")
                        .build();
                }
            } else {
                consecutiveHighMemory = 0;
            }
            
            return Health.up()
                .withDetail("memoryUsage", String.format("%.1f%%", usageRatio * 100))
                .withDetail("consecutiveHighMemory", consecutiveHighMemory)
                .build();
        }
    }

    // ============================================
    // Example 4: Thread Pool Monitor
    // ============================================
    
    @Component
    public static class ThreadPoolMonitor implements HealthIndicator {
        
        private final LivenessStateMonitor livenessMonitor;
        private boolean threadPoolExhausted = false;
        
        public ThreadPoolMonitor(LivenessStateMonitor livenessMonitor) {
            this.livenessMonitor = livenessMonitor;
        }
        
        @Override
        public Health health() {
            int activeThreads = Thread.activeCount();
            
            // Check for thread pool exhaustion (simplified)
            if (activeThreads > 1000) {
                threadPoolExhausted = true;
                livenessMonitor.markAsBroken("Thread pool exhausted");
                
                return Health.down()
                    .withDetail("reason", "Thread pool exhausted")
                    .withDetail("activeThreads", activeThreads)
                    .withDetail("action", "Pod will be restarted")
                    .build();
            }
            
            return Health.up()
                .withDetail("activeThreads", activeThreads)
                .withDetail("status", "normal")
                .build();
        }
    }

    // ============================================
    // Example 5: Critical Resource Monitor
    // ============================================
    
    @Component
    public static class CriticalResourceMonitor implements HealthIndicator {
        
        private final LivenessStateMonitor livenessMonitor;
        private boolean resourceCorrupted = false;
        
        public CriticalResourceMonitor(LivenessStateMonitor livenessMonitor) {
            this.livenessMonitor = livenessMonitor;
        }
        
        @Override
        public Health health() {
            if (checkResourceIntegrity()) {
                return Health.up()
                    .withDetail("resources", "intact")
                    .withDetail("timestamp", Instant.now())
                    .build();
            } else {
                resourceCorrupted = true;
                livenessMonitor.markAsBroken("Critical resource corrupted");
                
                return Health.down()
                    .withDetail("reason", "Critical resource corrupted")
                    .withDetail("action", "Pod will be restarted")
                    .withDetail("timestamp", Instant.now())
                    .build();
            }
        }
        
        private boolean checkResourceIntegrity() {
            // Simulate resource integrity check
            return Math.random() > 0.005; // 99.5% success
        }
    }

    // ============================================
    // Example 6: Liveness State REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/liveness")
    public static class LivenessController {
        
        private final LivenessStateMonitor livenessMonitor;
        private final DeadlockDetector deadlockDetector;
        private final MemoryLeakDetector memoryLeakDetector;
        
        public LivenessController(
                LivenessStateMonitor livenessMonitor,
                DeadlockDetector deadlockDetector,
                MemoryLeakDetector memoryLeakDetector) {
            this.livenessMonitor = livenessMonitor;
            this.deadlockDetector = deadlockDetector;
            this.memoryLeakDetector = memoryLeakDetector;
        }
        
        @GetMapping("/state")
        public Map<String, Object> getCurrentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("livenessState", livenessMonitor.getCurrentState().toString());
            state.put("timestamp", Instant.now());
            return state;
        }
        
        @PostMapping("/mark-broken")
        public Map<String, String> markAsBroken(@RequestParam String reason) {
            livenessMonitor.markAsBroken(reason);
            return Collections.singletonMap("status", 
                "Application marked as BROKEN. Pod will be restarted.");
        }
        
        @PostMapping("/mark-correct")
        public Map<String, String> markAsCorrect() {
            livenessMonitor.markAsCorrect();
            return Collections.singletonMap("status", 
                "Application marked as CORRECT");
        }
        
        @GetMapping("/detectors")
        public Map<String, Object> getDetectorStatus() {
            Map<String, Object> status = new HashMap<>();
            status.put("deadlockDetector", 
                deadlockDetector.isDeadlockDetected() ? "DETECTED" : "NORMAL");
            status.put("memoryLeakDetector", "ACTIVE");
            status.put("threadPoolMonitor", "ACTIVE");
            status.put("resourceMonitor", "ACTIVE");
            return status;
        }
        
        @GetMapping("/info")
        public Map<String, Object> getLivenessInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("description", "Liveness probe for Kubernetes");
            info.put("endpoint", "/actuator/health/liveness");
            info.put("states", Arrays.asList("CORRECT", "BROKEN"));
            info.put("purpose", "Detect unrecoverable application states");
            info.put("action", "Triggers pod restart when BROKEN");
            
            Map<String, String> config = new HashMap<>();
            config.put("initialDelaySeconds", "30");
            config.put("periodSeconds", "10");
            config.put("failureThreshold", "3");
            info.put("recommendedConfig", config);
            
            return info;
        }
    }
}
