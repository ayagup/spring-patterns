package com.example.microservices.eurekadiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Eureka Discovery Pattern
 * 
 * This pattern demonstrates Netflix Eureka service discovery, which enables services
 * to register themselves and discover other services without hard-coded hostnames and ports.
 * 
 * Key Components:
 * 1. EurekaServer - Service registry that maintains service instance information
 * 2. EurekaClient - Service that registers with Eureka and discovers other services
 * 3. InstanceInfo - Metadata about a service instance
 * 4. LeaseManager - Manages service instance leases and renewals
 * 5. HealthCheck - Monitors service instance health
 * 
 * Features:
 * - Service registration and deregistration
 * - Service discovery with load balancing
 * - Health check and heartbeat mechanism
 * - Self-preservation mode during network partitions
 * - Zone-aware load balancing
 * - Instance metadata and status management
 * 
 * Use Cases:
 * - Dynamic service discovery in microservices
 * - Client-side load balancing
 * - Failover and high availability
 * - Zero-downtime deployments
 * - Multi-zone deployments
 */

@SpringBootApplication
public class EurekaDiscoveryPattern {

    public static void main(String[] args) {
        SpringApplication.run(EurekaDiscoveryPattern.class, args);
        
        // Demonstration
        System.out.println("=== Eureka Discovery Pattern Demo ===\n");
        
        // Create Eureka Server
        EurekaServer eurekaServer = new EurekaServer();
        eurekaServer.start();
        
        // Create Eureka Clients (Services)
        System.out.println("1. Registering Services with Eureka:");
        EurekaClient userService1 = new EurekaClient("user-service", "192.168.1.10", 8081, eurekaServer);
        EurekaClient userService2 = new EurekaClient("user-service", "192.168.1.11", 8081, eurekaServer);
        EurekaClient orderService1 = new EurekaClient("order-service", "192.168.1.20", 8082, eurekaServer);
        
        userService1.register();
        userService2.register();
        orderService1.register();
        
        // Wait for registration
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        
        // Service Discovery
        System.out.println("\n2. Service Discovery:");
        List<InstanceInfo> userInstances = eurekaServer.getInstances("user-service");
        System.out.println("Discovered " + userInstances.size() + " instances of user-service:");
        userInstances.forEach(instance -> 
            System.out.println("  - " + instance.getInstanceId() + " at " + instance.getHomePageUrl()));
        
        // Health Check
        System.out.println("\n3. Health Check and Heartbeat:");
        userService1.sendHeartbeat();
        userService2.sendHeartbeat();
        orderService1.sendHeartbeat();
        
        System.out.println("All services sent heartbeat");
        
        // Simulate service failure
        System.out.println("\n4. Simulating Service Failure:");
        userService2.deregister();
        System.out.println("user-service instance 2 deregistered");
        
        userInstances = eurekaServer.getInstances("user-service");
        System.out.println("Remaining user-service instances: " + userInstances.size());
        
        // Zone-aware discovery
        System.out.println("\n5. Zone-Aware Service Discovery:");
        userService1.getInstanceInfo().setZone("us-east-1a");
        orderService1.getInstanceInfo().setZone("us-east-1b");
        
        List<InstanceInfo> zoneInstances = eurekaServer.getInstancesByZone("user-service", "us-east-1a");
        System.out.println("Instances in zone us-east-1a: " + zoneInstances.size());
        
        // Registry statistics
        System.out.println("\n6. Eureka Server Statistics:");
        Map<String, Object> stats = eurekaServer.getStatistics();
        stats.forEach((key, value) -> System.out.println("  " + key + ": " + value));
        
        // Self-preservation mode
        System.out.println("\n7. Self-Preservation Mode:");
        eurekaServer.enableSelfPreservation(true);
        System.out.println("Self-preservation enabled: " + eurekaServer.isSelfPreservationEnabled());
    }
}

/**
 * Eureka Server - Service Registry
 */
@Service
class EurekaServer {
    private final Map<String, List<InstanceInfo>> registry = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastRenewalTime = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private boolean selfPreservationEnabled = false;
    private final int renewalThresholdPercent = 85;
    private final long evictionIntervalSeconds = 60;
    
    public void start() {
        System.out.println("Eureka Server started on port 8761");
        
        // Start eviction task
        scheduler.scheduleAtFixedRate(this::evictExpiredLeases, 
            evictionIntervalSeconds, evictionIntervalSeconds, TimeUnit.SECONDS);
        
        // Start self-preservation check
        scheduler.scheduleAtFixedRate(this::checkSelfPreservation, 
            30, 30, TimeUnit.SECONDS);
    }
    
    public void register(InstanceInfo instance) {
        String appName = instance.getAppName();
        registry.computeIfAbsent(appName, k -> new CopyOnWriteArrayList<>()).add(instance);
        lastRenewalTime.put(instance.getInstanceId(), LocalDateTime.now());
        
        instance.setStatus(InstanceStatus.UP);
        System.out.println("Registered: " + instance.getInstanceId() + 
                         " [" + instance.getAppName() + "] at " + instance.getHomePageUrl());
    }
    
    public void deregister(String instanceId) {
        registry.values().forEach(instances -> 
            instances.removeIf(i -> i.getInstanceId().equals(instanceId)));
        lastRenewalTime.remove(instanceId);
        System.out.println("Deregistered: " + instanceId);
    }
    
    public boolean renew(String instanceId) {
        boolean renewed = lastRenewalTime.containsKey(instanceId);
        if (renewed) {
            lastRenewalTime.put(instanceId, LocalDateTime.now());
        }
        return renewed;
    }
    
    public List<InstanceInfo> getInstances(String appName) {
        return new ArrayList<>(registry.getOrDefault(appName, Collections.emptyList()));
    }
    
    public List<InstanceInfo> getInstancesByZone(String appName, String zone) {
        return registry.getOrDefault(appName, Collections.emptyList()).stream()
            .filter(instance -> zone.equals(instance.getZone()))
            .collect(Collectors.toList());
    }
    
    public List<String> getApplications() {
        return new ArrayList<>(registry.keySet());
    }
    
    public InstanceInfo getInstance(String instanceId) {
        return registry.values().stream()
            .flatMap(List::stream)
            .filter(i -> i.getInstanceId().equals(instanceId))
            .findFirst()
            .orElse(null);
    }
    
    private void evictExpiredLeases() {
        if (selfPreservationEnabled) {
            System.out.println("Self-preservation mode: skipping eviction");
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredInstances = new ArrayList<>();
        
        lastRenewalTime.forEach((instanceId, lastRenewal) -> {
            if (lastRenewal.plusSeconds(90).isBefore(now)) {
                expiredInstances.add(instanceId);
            }
        });
        
        expiredInstances.forEach(this::deregister);
        
        if (!expiredInstances.isEmpty()) {
            System.out.println("Evicted " + expiredInstances.size() + " expired instances");
        }
    }
    
    private void checkSelfPreservation() {
        int totalInstances = (int) registry.values().stream()
            .mapToLong(List::size)
            .sum();
        
        if (totalInstances == 0) return;
        
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(90);
        long renewedCount = lastRenewalTime.values().stream()
            .filter(time -> time.isAfter(threshold))
            .count();
        
        int renewalPercentage = (int) ((renewedCount * 100.0) / totalInstances);
        
        if (renewalPercentage < renewalThresholdPercent) {
            selfPreservationEnabled = true;
            System.out.println("Self-preservation mode activated: renewal rate " + 
                             renewalPercentage + "% < threshold " + renewalThresholdPercent + "%");
        }
    }
    
    public void enableSelfPreservation(boolean enabled) {
        this.selfPreservationEnabled = enabled;
    }
    
    public boolean isSelfPreservationEnabled() {
        return selfPreservationEnabled;
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", registry.size());
        stats.put("totalInstances", registry.values().stream().mapToLong(List::size).sum());
        stats.put("selfPreservationEnabled", selfPreservationEnabled);
        stats.put("renewalThresholdPercent", renewalThresholdPercent);
        
        Map<String, Long> appCounts = registry.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue().size()));
        stats.put("instancesPerApp", appCounts);
        
        return stats;
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
}

/**
 * Eureka Client - Service that registers with Eureka
 */
class EurekaClient {
    private final String appName;
    private final InstanceInfo instanceInfo;
    private final EurekaServer eurekaServer;
    private final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);
    
    public EurekaClient(String appName, String ipAddress, int port, EurekaServer eurekaServer) {
        this.appName = appName;
        this.eurekaServer = eurekaServer;
        this.instanceInfo = new InstanceInfo(appName, ipAddress, port);
    }
    
    public void register() {
        eurekaServer.register(instanceInfo);
        
        // Start automatic heartbeat
        heartbeatScheduler.scheduleAtFixedRate(
            this::sendHeartbeat, 30, 30, TimeUnit.SECONDS);
    }
    
    public void deregister() {
        eurekaServer.deregister(instanceInfo.getInstanceId());
        heartbeatScheduler.shutdown();
    }
    
    public boolean sendHeartbeat() {
        boolean renewed = eurekaServer.renew(instanceInfo.getInstanceId());
        if (!renewed) {
            System.out.println("Heartbeat failed for " + instanceInfo.getInstanceId() + ", re-registering...");
            register();
        }
        return renewed;
    }
    
    public List<InstanceInfo> discoverService(String serviceName) {
        return eurekaServer.getInstances(serviceName);
    }
    
    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }
}

/**
 * Instance Information - Metadata about a service instance
 */
class InstanceInfo {
    private final String instanceId;
    private final String appName;
    private final String ipAddress;
    private final int port;
    private final String homePageUrl;
    private final String statusPageUrl;
    private final String healthCheckUrl;
    private InstanceStatus status;
    private String zone;
    private final Map<String, String> metadata;
    private final LocalDateTime registrationTime;
    
    public InstanceInfo(String appName, String ipAddress, int port) {
        this.instanceId = ipAddress + ":" + appName + ":" + port;
        this.appName = appName;
        this.ipAddress = ipAddress;
        this.port = port;
        this.homePageUrl = "http://" + ipAddress + ":" + port + "/";
        this.statusPageUrl = homePageUrl + "actuator/info";
        this.healthCheckUrl = homePageUrl + "actuator/health";
        this.status = InstanceStatus.STARTING;
        this.zone = "default";
        this.metadata = new HashMap<>();
        this.registrationTime = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getInstanceId() { return instanceId; }
    public String getAppName() { return appName; }
    public String getIpAddress() { return ipAddress; }
    public int getPort() { return port; }
    public String getHomePageUrl() { return homePageUrl; }
    public String getStatusPageUrl() { return statusPageUrl; }
    public String getHealthCheckUrl() { return healthCheckUrl; }
    public InstanceStatus getStatus() { return status; }
    public void setStatus(InstanceStatus status) { this.status = status; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public Map<String, String> getMetadata() { return metadata; }
    public LocalDateTime getRegistrationTime() { return registrationTime; }
    
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
}

/**
 * Instance Status enumeration
 */
enum InstanceStatus {
    UP,           // Ready to receive traffic
    DOWN,         // Not ready to receive traffic
    STARTING,     // Starting up
    OUT_OF_SERVICE, // Intentionally taken out of service
    UNKNOWN       // Unknown status
}

/**
 * Lease Manager - Manages instance leases
 */
class LeaseManager {
    private final Map<String, Lease> leases = new ConcurrentHashMap<>();
    private final int leaseDurationSeconds = 90;
    private final int leaseRenewalIntervalSeconds = 30;
    
    public void register(String instanceId) {
        Lease lease = new Lease(instanceId, leaseDurationSeconds);
        leases.put(instanceId, lease);
    }
    
    public boolean renew(String instanceId) {
        Lease lease = leases.get(instanceId);
        if (lease != null) {
            lease.renew();
            return true;
        }
        return false;
    }
    
    public void cancel(String instanceId) {
        leases.remove(instanceId);
    }
    
    public boolean isExpired(String instanceId) {
        Lease lease = leases.get(instanceId);
        return lease != null && lease.isExpired();
    }
    
    public int getLeaseDurationSeconds() {
        return leaseDurationSeconds;
    }
    
    public int getLeaseRenewalIntervalSeconds() {
        return leaseRenewalIntervalSeconds;
    }
}

/**
 * Lease - Represents a service instance lease
 */
class Lease {
    private final String instanceId;
    private final int durationSeconds;
    private LocalDateTime lastRenewalTime;
    
    public Lease(String instanceId, int durationSeconds) {
        this.instanceId = instanceId;
        this.durationSeconds = durationSeconds;
        this.lastRenewalTime = LocalDateTime.now();
    }
    
    public void renew() {
        this.lastRenewalTime = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(lastRenewalTime.plusSeconds(durationSeconds));
    }
    
    public String getInstanceId() { return instanceId; }
    public int getDurationSeconds() { return durationSeconds; }
    public LocalDateTime getLastRenewalTime() { return lastRenewalTime; }
}

/**
 * REST Controller for Eureka Server
 */
@RestController
@RequestMapping("/eureka")
class EurekaServerController {
    private final EurekaServer eurekaServer;
    
    public EurekaServerController(EurekaServer eurekaServer) {
        this.eurekaServer = eurekaServer;
    }
    
    @PostMapping("/apps/{appName}")
    public Map<String, Object> register(@PathVariable String appName, 
                                       @RequestBody InstanceInfo instance) {
        eurekaServer.register(instance);
        return Map.of("status", "registered", "instanceId", instance.getInstanceId());
    }
    
    @DeleteMapping("/apps/{appName}/{instanceId}")
    public Map<String, Object> deregister(@PathVariable String appName,
                                         @PathVariable String instanceId) {
        eurekaServer.deregister(instanceId);
        return Map.of("status", "deregistered", "instanceId", instanceId);
    }
    
    @PutMapping("/apps/{appName}/{instanceId}")
    public Map<String, Object> renewLease(@PathVariable String appName,
                                         @PathVariable String instanceId) {
        boolean renewed = eurekaServer.renew(instanceId);
        return Map.of("status", renewed ? "renewed" : "not_found", "instanceId", instanceId);
    }
    
    @GetMapping("/apps/{appName}")
    public List<InstanceInfo> getInstances(@PathVariable String appName) {
        return eurekaServer.getInstances(appName);
    }
    
    @GetMapping("/apps")
    public List<String> getApplications() {
        return eurekaServer.getApplications();
    }
    
    @GetMapping("/apps/{appName}/{instanceId}")
    public InstanceInfo getInstance(@PathVariable String appName,
                                    @PathVariable String instanceId) {
        return eurekaServer.getInstance(instanceId);
    }
    
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return eurekaServer.getStatistics();
    }
}

/**
 * Discovery Client - Used by services to discover other services
 */
@Service
class DiscoveryClient {
    private final EurekaServer eurekaServer;
    private final Map<String, List<InstanceInfo>> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cacheRefreshScheduler = Executors.newScheduledThreadPool(1);
    
    public DiscoveryClient(EurekaServer eurekaServer) {
        this.eurekaServer = eurekaServer;
        
        // Refresh cache every 30 seconds
        cacheRefreshScheduler.scheduleAtFixedRate(
            this::refreshCache, 30, 30, TimeUnit.SECONDS);
    }
    
    public List<InstanceInfo> getInstances(String serviceName) {
        return cache.getOrDefault(serviceName, eurekaServer.getInstances(serviceName));
    }
    
    public InstanceInfo chooseInstance(String serviceName) {
        List<InstanceInfo> instances = getInstances(serviceName);
        if (instances.isEmpty()) {
            return null;
        }
        // Simple round-robin
        return instances.get(new Random().nextInt(instances.size()));
    }
    
    private void refreshCache() {
        List<String> applications = eurekaServer.getApplications();
        applications.forEach(app -> 
            cache.put(app, eurekaServer.getInstances(app)));
    }
    
    public void shutdown() {
        cacheRefreshScheduler.shutdown();
    }
}
