package com.example.bluegreen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Blue-Green Deployment Patterns:
 * - Feature Toggle Pattern
 * - Canary Deployment Pattern
 * - A/B Testing Pattern
 * - Rolling Deployment Pattern
 * - Shadow Deployment Pattern
 * 
 * Demonstrates zero-downtime deployment strategies.
 * 
 * Use Cases:
 * - Production deployments without downtime
 * - Gradual feature rollouts
 * - User-based testing
 * - Risk mitigation
 * - Easy rollbacks
 */
@SpringBootApplication
public class BlueGreenDeploymentPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(BlueGreenDeploymentPattern.class, args);
    }
}

/**
 * Pattern 1: Feature Toggle Service
 */
@Service
class FeatureToggleService {
    
    private final Map<String, Boolean> features = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userFeatures = new ConcurrentHashMap<>();
    
    public FeatureToggleService() {
        // Initialize default features
        features.put("new-ui", false);
        features.put("payment-v2", false);
        features.put("experimental-search", false);
    }
    
    /**
     * Check if feature is enabled globally
     */
    public boolean isEnabled(String featureName) {
        return features.getOrDefault(featureName, false);
    }
    
    /**
     * Check if feature is enabled for specific user
     */
    public boolean isEnabledForUser(String featureName, String userId) {
        Set<String> users = userFeatures.get(featureName);
        if (users != null && users.contains(userId)) {
            return true;
        }
        return isEnabled(featureName);
    }
    
    /**
     * Toggle feature globally
     */
    public void toggleFeature(String featureName, boolean enabled) {
        features.put(featureName, enabled);
    }
    
    /**
     * Enable feature for specific users
     */
    public void enableForUsers(String featureName, Set<String> userIds) {
        userFeatures.put(featureName, userIds);
    }
    
    public Map<String, Boolean> getAllFeatures() {
        return new HashMap<>(features);
    }
}

/**
 * Pattern 2: Canary Deployment Service
 */
@Service
class CanaryDeploymentService {
    
    private int canaryPercentage = 0;
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private String canaryVersion = "v2.0";
    private String stableVersion = "v1.0";
    
    /**
     * Route request to canary or stable version
     */
    public Map<String, String> routeRequest() {
        int count = requestCounter.incrementAndGet();
        boolean useCanary = (count % 100) < canaryPercentage;
        
        return Map.of(
            "version", useCanary ? canaryVersion : stableVersion,
            "type", useCanary ? "canary" : "stable",
            "requestNumber", String.valueOf(count)
        );
    }
    
    /**
     * Set canary traffic percentage (0-100)
     */
    public void setCanaryPercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        this.canaryPercentage = percentage;
    }
    
    public Map<String, Object> getCanaryStatus() {
        return Map.of(
            "canaryPercentage", canaryPercentage,
            "canaryVersion", canaryVersion,
            "stableVersion", stableVersion,
            "totalRequests", requestCounter.get()
        );
    }
}

/**
 * Pattern 3: A/B Testing Service
 */
@Service
class ABTestingService {
    
    private final Map<String, String> userVariants = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> experimentMetrics = new ConcurrentHashMap<>();
    
    /**
     * Assign user to variant (A or B)
     */
    public String assignVariant(String userId, String experimentName) {
        String key = experimentName + ":" + userId;
        return userVariants.computeIfAbsent(key, k -> {
            // 50/50 split based on user ID hash
            return (userId.hashCode() % 2 == 0) ? "A" : "B";
        });
    }
    
    /**
     * Track conversion/metric for experiment
     */
    public void trackConversion(String experimentName, String variant) {
        Map<String, Integer> metrics = experimentMetrics
            .computeIfAbsent(experimentName, k -> new ConcurrentHashMap<>());
        
        metrics.merge(variant, 1, Integer::sum);
    }
    
    /**
     * Get experiment results
     */
    public Map<String, Object> getExperimentResults(String experimentName) {
        Map<String, Integer> metrics = experimentMetrics.get(experimentName);
        if (metrics == null) {
            return Map.of("error", "No data for experiment: " + experimentName);
        }
        
        int totalA = metrics.getOrDefault("A", 0);
        int totalB = metrics.getOrDefault("B", 0);
        int total = totalA + totalB;
        
        return Map.of(
            "experimentName", experimentName,
            "variantA", totalA,
            "variantB", totalB,
            "total", total,
            "percentageA", total > 0 ? (double) totalA / total * 100 : 0,
            "percentageB", total > 0 ? (double) totalB / total * 100 : 0
        );
    }
}

/**
 * Pattern 4: Rolling Deployment Service
 */
@Service
class RollingDeploymentService {
    
    private final List<String> instances = Arrays.asList("instance-1", "instance-2", "instance-3", "instance-4");
    private final Map<String, String> instanceVersions = new ConcurrentHashMap<>();
    private int currentDeploymentIndex = 0;
    
    public RollingDeploymentService() {
        instances.forEach(instance -> instanceVersions.put(instance, "v1.0"));
    }
    
    /**
     * Deploy new version to next instance
     */
    public Map<String, Object> deployNext(String newVersion) {
        if (currentDeploymentIndex >= instances.size()) {
            return Map.of(
                "status", "complete",
                "message", "All instances already deployed"
            );
        }
        
        String instance = instances.get(currentDeploymentIndex);
        String oldVersion = instanceVersions.get(instance);
        instanceVersions.put(instance, newVersion);
        currentDeploymentIndex++;
        
        return Map.of(
            "status", "deployed",
            "instance", instance,
            "oldVersion", oldVersion,
            "newVersion", newVersion,
            "progress", currentDeploymentIndex + "/" + instances.size()
        );
    }
    
    /**
     * Rollback deployment
     */
    public Map<String, String> rollback(String version) {
        instances.forEach(instance -> instanceVersions.put(instance, version));
        currentDeploymentIndex = 0;
        return Map.of("status", "rolled back to " + version);
    }
    
    public Map<String, Object> getDeploymentStatus() {
        return Map.of(
            "instances", new HashMap<>(instanceVersions),
            "totalInstances", instances.size(),
            "deployedInstances", currentDeploymentIndex
        );
    }
}

/**
 * Pattern 5: Shadow Deployment Service
 */
@Service
class ShadowDeploymentService {
    
    private boolean shadowEnabled = false;
    private final List<Map<String, Object>> shadowRequests = new ArrayList<>();
    
    /**
     * Process request in both primary and shadow systems
     */
    public Map<String, Object> processRequest(Map<String, Object> request) {
        // Process in primary system
        Map<String, Object> primaryResponse = executePrimary(request);
        
        // If shadow enabled, also process in shadow system
        if (shadowEnabled) {
            executeShadow(request);
        }
        
        return primaryResponse;
    }
    
    private Map<String, Object> executePrimary(Map<String, Object> request) {
        return Map.of(
            "system", "primary",
            "result", "Primary result for: " + request.get("data"),
            "timestamp", System.currentTimeMillis()
        );
    }
    
    private void executeShadow(Map<String, Object> request) {
        // Execute in shadow system (new version)
        Map<String, Object> shadowResult = Map.of(
            "system", "shadow",
            "result", "Shadow result for: " + request.get("data"),
            "timestamp", System.currentTimeMillis()
        );
        
        shadowRequests.add(shadowResult);
    }
    
    public void enableShadow(boolean enabled) {
        this.shadowEnabled = enabled;
    }
    
    public Map<String, Object> getShadowMetrics() {
        return Map.of(
            "shadowEnabled", shadowEnabled,
            "shadowRequestsProcessed", shadowRequests.size(),
            "recentRequests", shadowRequests.stream().limit(10).toList()
        );
    }
}

/**
 * REST Controller for Blue-Green Deployment Patterns
 */
@RestController
@RequestMapping("/api/deployment")
class DeploymentController {
    
    private final FeatureToggleService featureToggle;
    private final CanaryDeploymentService canary;
    private final ABTestingService abTesting;
    private final RollingDeploymentService rolling;
    private final ShadowDeploymentService shadow;
    
    public DeploymentController(FeatureToggleService featureToggle,
                                CanaryDeploymentService canary,
                                ABTestingService abTesting,
                                RollingDeploymentService rolling,
                                ShadowDeploymentService shadow) {
        this.featureToggle = featureToggle;
        this.canary = canary;
        this.abTesting = abTesting;
        this.rolling = rolling;
        this.shadow = shadow;
    }
    
    // Feature Toggle endpoints
    @GetMapping("/feature/{name}")
    public Map<String, Boolean> checkFeature(@PathVariable String name, 
                                             @RequestParam(required = false) String userId) {
        boolean enabled = userId != null ? 
            featureToggle.isEnabledForUser(name, userId) : 
            featureToggle.isEnabled(name);
        return Map.of("enabled", enabled);
    }
    
    @PostMapping("/feature/{name}")
    public Map<String, String> toggleFeature(@PathVariable String name, @RequestParam boolean enabled) {
        featureToggle.toggleFeature(name, enabled);
        return Map.of("status", "Feature " + name + " " + (enabled ? "enabled" : "disabled"));
    }
    
    @GetMapping("/features")
    public Map<String, Boolean> getAllFeatures() {
        return featureToggle.getAllFeatures();
    }
    
    // Canary Deployment endpoints
    @GetMapping("/canary/route")
    public Map<String, String> routeRequest() {
        return canary.routeRequest();
    }
    
    @PostMapping("/canary/percentage")
    public Map<String, String> setCanaryPercentage(@RequestParam int percentage) {
        canary.setCanaryPercentage(percentage);
        return Map.of("status", "Canary percentage set to " + percentage + "%");
    }
    
    @GetMapping("/canary/status")
    public Map<String, Object> getCanaryStatus() {
        return canary.getCanaryStatus();
    }
    
    // A/B Testing endpoints
    @GetMapping("/ab/{experiment}")
    public Map<String, String> getVariant(@PathVariable String experiment, @RequestParam String userId) {
        String variant = abTesting.assignVariant(userId, experiment);
        return Map.of("variant", variant);
    }
    
    @PostMapping("/ab/{experiment}/convert")
    public Map<String, String> trackConversion(@PathVariable String experiment, @RequestParam String variant) {
        abTesting.trackConversion(experiment, variant);
        return Map.of("status", "conversion tracked");
    }
    
    @GetMapping("/ab/{experiment}/results")
    public Map<String, Object> getResults(@PathVariable String experiment) {
        return abTesting.getExperimentResults(experiment);
    }
    
    // Rolling Deployment endpoints
    @PostMapping("/rolling/deploy")
    public Map<String, Object> deployNext(@RequestParam String version) {
        return rolling.deployNext(version);
    }
    
    @PostMapping("/rolling/rollback")
    public Map<String, String> rollback(@RequestParam String version) {
        return rolling.rollback(version);
    }
    
    @GetMapping("/rolling/status")
    public Map<String, Object> getRollingStatus() {
        return rolling.getDeploymentStatus();
    }
    
    // Shadow Deployment endpoints
    @PostMapping("/shadow/process")
    public Map<String, Object> processShadow(@RequestBody Map<String, Object> request) {
        return shadow.processRequest(request);
    }
    
    @PostMapping("/shadow/toggle")
    public Map<String, String> toggleShadow(@RequestParam boolean enabled) {
        shadow.enableShadow(enabled);
        return Map.of("status", "Shadow deployment " + (enabled ? "enabled" : "disabled"));
    }
    
    @GetMapping("/shadow/metrics")
    public Map<String, Object> getShadowMetrics() {
        return shadow.getShadowMetrics();
    }
}
