package com.example.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.*;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;

/**
 * Container Integration Patterns:
 * - Docker Integration Pattern
 * - Kubernetes Integration Pattern
 * - Health Probe Pattern
 * - Readiness Probe Pattern
 * - Liveness Probe Pattern
 * - Graceful Shutdown Pattern
 * - Container Lifecycle Pattern
 * 
 * Demonstrates cloud-native application patterns for containerized environments.
 * 
 * Use Cases:
 * - Kubernetes deployments
 * - Docker containers
 * - Orchestration platforms
 * - Cloud-native applications
 * - Microservices infrastructure
 */
@SpringBootApplication
public class ContainerIntegrationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(ContainerIntegrationPattern.class, args);
    }
}

/**
 * Pattern 1: Health Probe Service
 */
@Service
class HealthProbeService {
    
    private boolean healthy = true;
    private final Map<String, Boolean> componentHealth = new ConcurrentHashMap<>();
    
    public HealthProbeService() {
        componentHealth.put("database", true);
        componentHealth.put("cache", true);
        componentHealth.put("messageQueue", true);
    }
    
    /**
     * Overall health check
     */
    public Map<String, Object> checkHealth() {
        boolean allHealthy = componentHealth.values().stream().allMatch(h -> h);
        
        return Map.of(
            "status", allHealthy ? "UP" : "DOWN",
            "components", new HashMap<>(componentHealth),
            "timestamp", System.currentTimeMillis()
        );
    }
    
    /**
     * Set component health
     */
    public void setComponentHealth(String component, boolean healthy) {
        componentHealth.put(component, healthy);
    }
    
    public boolean isHealthy() {
        return healthy && componentHealth.values().stream().allMatch(h -> h);
    }
}

/**
 * Pattern 2: Readiness Probe Service
 */
@Service
class ReadinessProbeService {
    
    private boolean ready = false;
    private final List<String> dependencies = Arrays.asList("database", "cache", "config");
    private final Set<String> initializedDependencies = ConcurrentHashMap.newKeySet();
    
    /**
     * Check if application is ready to serve traffic
     */
    public Map<String, Object> checkReadiness() {
        boolean allReady = initializedDependencies.containsAll(dependencies);
        ready = allReady;
        
        Map<String, Object> status = new HashMap<>();
        status.put("ready", ready);
        status.put("requiredDependencies", dependencies);
        status.put("initializedDependencies", new ArrayList<>(initializedDependencies));
        status.put("pendingDependencies", 
            dependencies.stream()
                .filter(d -> !initializedDependencies.contains(d))
                .toList());
        
        return status;
    }
    
    /**
     * Mark dependency as initialized
     */
    public void initializeDependency(String dependency) {
        initializedDependencies.add(dependency);
    }
    
    /**
     * Simulate initialization
     */
    public void initializeAll() {
        dependencies.forEach(this::initializeDependency);
    }
    
    public boolean isReady() {
        return ready;
    }
}

/**
 * Pattern 3: Liveness Probe Service
 */
@Service
class LivenessProbeService {
    
    private long lastHeartbeat = System.currentTimeMillis();
    private boolean alive = true;
    private final long deadThresholdMs = 30000; // 30 seconds
    
    /**
     * Check if application is alive (not deadlocked)
     */
    public Map<String, Object> checkLiveness() {
        long now = System.currentTimeMillis();
        long timeSinceHeartbeat = now - lastHeartbeat;
        
        boolean isAlive = alive && timeSinceHeartbeat < deadThresholdMs;
        
        return Map.of(
            "alive", isAlive,
            "lastHeartbeat", lastHeartbeat,
            "timeSinceHeartbeat", timeSinceHeartbeat + "ms",
            "threshold", deadThresholdMs + "ms"
        );
    }
    
    /**
     * Record heartbeat
     */
    public void heartbeat() {
        lastHeartbeat = System.currentTimeMillis();
    }
    
    /**
     * Simulate deadlock/unhealthy state
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public boolean isAlive() {
        return alive && (System.currentTimeMillis() - lastHeartbeat) < deadThresholdMs;
    }
}

/**
 * Pattern 4: Graceful Shutdown Service
 */
@Service
class GracefulShutdownService {
    
    private final Set<String> activeRequests = ConcurrentHashMap.newKeySet();
    private boolean shuttingDown = false;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    /**
     * Start processing request
     */
    public String startRequest(String requestId) {
        if (shuttingDown) {
            return "Service is shutting down, request rejected";
        }
        
        activeRequests.add(requestId);
        return "Request " + requestId + " started";
    }
    
    /**
     * Complete request
     */
    public void completeRequest(String requestId) {
        activeRequests.remove(requestId);
    }
    
    /**
     * Initiate graceful shutdown
     */
    public Map<String, Object> initiateShutdown() {
        shuttingDown = true;
        
        return Map.of(
            "status", "Shutdown initiated",
            "activeRequests", activeRequests.size(),
            "message", "Waiting for active requests to complete"
        );
    }
    
    /**
     * Wait for all requests to complete
     */
    public Map<String, Object> waitForCompletion(int timeoutSeconds) throws InterruptedException {
        long start = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000L;
        
        while (!activeRequests.isEmpty() && (System.currentTimeMillis() - start) < timeout) {
            Thread.sleep(100);
        }
        
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        
        return Map.of(
            "completed", activeRequests.isEmpty(),
            "remainingRequests", activeRequests.size(),
            "duration", (System.currentTimeMillis() - start) + "ms"
        );
    }
    
    @PreDestroy
    public void preDestroy() {
        System.out.println("PreDestroy: Gracefully shutting down...");
        shuttingDown = true;
        executorService.shutdown();
    }
    
    public boolean isShuttingDown() {
        return shuttingDown;
    }
    
    public int getActiveRequestCount() {
        return activeRequests.size();
    }
}

/**
 * Pattern 5-7: Container Lifecycle Service
 * Combines Docker/Kubernetes integration and lifecycle management
 */
@Service
class ContainerLifecycleService {
    
    private String phase = "STARTING";
    private final Map<String, String> metadata = new ConcurrentHashMap<>();
    private final List<String> lifecycleEvents = new CopyOnWriteArrayList<>();
    
    public ContainerLifecycleService() {
        // Simulate reading Kubernetes/Docker environment
        metadata.put("POD_NAME", System.getenv().getOrDefault("HOSTNAME", "localhost"));
        metadata.put("NAMESPACE", System.getenv().getOrDefault("NAMESPACE", "default"));
        metadata.put("NODE_NAME", System.getenv().getOrDefault("NODE_NAME", "local-node"));
        
        recordEvent("Container starting");
    }
    
    /**
     * Record lifecycle event
     */
    public void recordEvent(String event) {
        String timestampedEvent = System.currentTimeMillis() + ": " + event;
        lifecycleEvents.add(timestampedEvent);
        System.out.println("Lifecycle: " + timestampedEvent);
    }
    
    /**
     * Get container metadata (Kubernetes/Docker)
     */
    public Map<String, Object> getMetadata() {
        return Map.of(
            "podName", metadata.get("POD_NAME"),
            "namespace", metadata.get("NAMESPACE"),
            "nodeName", metadata.get("NODE_NAME"),
            "phase", phase,
            "startTime", lifecycleEvents.isEmpty() ? "unknown" : lifecycleEvents.get(0)
        );
    }
    
    /**
     * Update lifecycle phase
     */
    public void setPhase(String newPhase) {
        String oldPhase = this.phase;
        this.phase = newPhase;
        recordEvent("Phase changed: " + oldPhase + " -> " + newPhase);
    }
    
    /**
     * Get lifecycle events
     */
    public List<String> getLifecycleEvents() {
        return new ArrayList<>(lifecycleEvents);
    }
    
    /**
     * Get Docker/Kubernetes environment info
     */
    public Map<String, String> getContainerEnvironment() {
        Map<String, String> env = new HashMap<>();
        
        // Docker environment variables
        env.put("DOCKER_CONTAINER_ID", System.getenv().getOrDefault("HOSTNAME", "N/A"));
        
        // Kubernetes environment variables
        env.put("K8S_POD_NAME", System.getenv().getOrDefault("HOSTNAME", "N/A"));
        env.put("K8S_POD_NAMESPACE", System.getenv().getOrDefault("POD_NAMESPACE", "N/A"));
        env.put("K8S_POD_IP", System.getenv().getOrDefault("POD_IP", "N/A"));
        env.put("K8S_SERVICE_ACCOUNT", System.getenv().getOrDefault("SERVICE_ACCOUNT", "N/A"));
        
        return env;
    }
}

/**
 * Application lifecycle listener
 */
@Component
class ApplicationLifecycleListener implements 
        ApplicationListener<ApplicationReadyEvent> {
    
    private final ReadinessProbeService readiness;
    private final ContainerLifecycleService lifecycle;
    
    public ApplicationLifecycleListener(ReadinessProbeService readiness,
                                       ContainerLifecycleService lifecycle) {
        this.readiness = readiness;
        this.lifecycle = lifecycle;
    }
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        lifecycle.recordEvent("Application ready");
        lifecycle.setPhase("RUNNING");
        
        // Initialize all dependencies
        readiness.initializeAll();
        lifecycle.recordEvent("All dependencies initialized");
    }
}

/**
 * Shutdown listener
 */
@Component
class ShutdownListener implements ApplicationListener<ContextClosedEvent> {
    
    private final ContainerLifecycleService lifecycle;
    
    public ShutdownListener(ContainerLifecycleService lifecycle) {
        this.lifecycle = lifecycle;
    }
    
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        lifecycle.recordEvent("Application shutting down");
        lifecycle.setPhase("TERMINATING");
    }
}

/**
 * REST Controller for Container Integration Patterns
 */
@RestController
@RequestMapping("/api/container")
class ContainerController {
    
    private final HealthProbeService health;
    private final ReadinessProbeService readiness;
    private final LivenessProbeService liveness;
    private final GracefulShutdownService shutdown;
    private final ContainerLifecycleService lifecycle;
    
    public ContainerController(HealthProbeService health,
                              ReadinessProbeService readiness,
                              LivenessProbeService liveness,
                              GracefulShutdownService shutdown,
                              ContainerLifecycleService lifecycle) {
        this.health = health;
        this.readiness = readiness;
        this.liveness = liveness;
        this.shutdown = shutdown;
        this.lifecycle = lifecycle;
    }
    
    // Health endpoints
    @GetMapping("/health")
    public Map<String, Object> health() {
        return health.checkHealth();
    }
    
    @PostMapping("/health/{component}")
    public Map<String, String> setHealth(@PathVariable String component, @RequestParam boolean healthy) {
        health.setComponentHealth(component, healthy);
        return Map.of("status", component + " health set to " + healthy);
    }
    
    // Readiness endpoints
    @GetMapping("/ready")
    public Map<String, Object> ready() {
        return readiness.checkReadiness();
    }
    
    @PostMapping("/ready/init/{dependency}")
    public Map<String, String> initDependency(@PathVariable String dependency) {
        readiness.initializeDependency(dependency);
        return Map.of("status", dependency + " initialized");
    }
    
    // Liveness endpoints
    @GetMapping("/alive")
    public Map<String, Object> alive() {
        return liveness.checkLiveness();
    }
    
    @PostMapping("/alive/heartbeat")
    public Map<String, String> heartbeat() {
        liveness.heartbeat();
        return Map.of("status", "heartbeat recorded");
    }
    
    // Graceful shutdown endpoints
    @PostMapping("/request/start")
    public Map<String, String> startRequest(@RequestParam String requestId) {
        String result = shutdown.startRequest(requestId);
        return Map.of("status", result);
    }
    
    @PostMapping("/request/complete")
    public Map<String, String> completeRequest(@RequestParam String requestId) {
        shutdown.completeRequest(requestId);
        return Map.of("status", "Request " + requestId + " completed");
    }
    
    @PostMapping("/shutdown")
    public Map<String, Object> initiateShutdown() {
        return shutdown.initiateShutdown();
    }
    
    @GetMapping("/shutdown/status")
    public Map<String, Object> shutdownStatus() {
        return Map.of(
            "shuttingDown", shutdown.isShuttingDown(),
            "activeRequests", shutdown.getActiveRequestCount()
        );
    }
    
    // Lifecycle endpoints
    @GetMapping("/metadata")
    public Map<String, Object> getMetadata() {
        return lifecycle.getMetadata();
    }
    
    @GetMapping("/lifecycle/events")
    public List<String> getLifecycleEvents() {
        return lifecycle.getLifecycleEvents();
    }
    
    @GetMapping("/environment")
    public Map<String, String> getEnvironment() {
        return lifecycle.getContainerEnvironment();
    }
    
    @PostMapping("/lifecycle/phase")
    public Map<String, String> setPhase(@RequestParam String phase) {
        lifecycle.setPhase(phase);
        return Map.of("status", "Phase set to " + phase);
    }
}
