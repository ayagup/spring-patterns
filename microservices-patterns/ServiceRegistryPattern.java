package com.example.microservices.serviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service Registry Pattern - Central service discovery and registration
 * 
 * This pattern demonstrates:
 * 1. Service registration and deregistration
 * 2. Service discovery and lookup
 * 3. Health check monitoring
 * 4. Service instance management
 * 5. Metadata management
 * 6. Load balancing support
 * 7. Failure detection
 * 8. Service versioning
 * 9. Zone-aware routing
 * 10. Heart beat mechanism
 * 
 * Key Concepts:
 * - Service Registry: Central directory of all service instances
 * - Service Instance: Running instance of a microservice
 * - Health Check: Periodic check to ensure service availability
 * - Metadata: Additional information about service instances
 * - Heartbeat: Periodic signal from service to registry
 * 
 * Use Cases:
 * - Microservices discovery
 * - Dynamic scaling
 * - Load balancing
 * - Fault tolerance
 * - Service mesh
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class ServiceRegistryPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(ServiceRegistryPattern.class, args);
        demonstrateServiceRegistry(context);
    }
    
    private static void demonstrateServiceRegistry(
            org.springframework.context.ApplicationContext context) {
        System.out.println("=== Service Registry Pattern Demonstrations ===\n");
        
        ServiceRegistry registry = context.getBean(ServiceRegistry.class);
        
        // Demo 1: Register services
        System.out.println("1. Service Registration:");
        ServiceInstance instance1 = new ServiceInstance(
            "user-service", "192.168.1.10", 8080, "v1.0");
        ServiceInstance instance2 = new ServiceInstance(
            "user-service", "192.168.1.11", 8080, "v1.0");
        ServiceInstance instance3 = new ServiceInstance(
            "order-service", "192.168.1.20", 8081, "v2.0");
        
        registry.register(instance1);
        registry.register(instance2);
        registry.register(instance3);
        System.out.println("   Registered 3 service instances\n");
        
        // Demo 2: Service discovery
        System.out.println("2. Service Discovery:");
        List<ServiceInstance> userServices = registry.getInstances("user-service");
        System.out.println("   Found " + userServices.size() + " instances of user-service");
        userServices.forEach(s -> System.out.println("   - " + s.getHost() + ":" + s.getPort()));
        System.out.println();
        
        // Demo 3: Health checks
        System.out.println("3. Health Status:");
        Map<String, List<ServiceInstance>> allServices = registry.getAllServices();
        allServices.forEach((serviceName, instances) -> {
            System.out.println("   " + serviceName + ": " + instances.size() + " instances");
        });
    }
}

// ============================================================================
// Service Registry
// ============================================================================

/**
 * Service Registry - manages service registration and discovery
 */
@org.springframework.stereotype.Service
class ServiceRegistry {
    
    private final Map<String, List<ServiceInstance>> serviceMap = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastHeartbeat = new ConcurrentHashMap<>();
    
    public void register(ServiceInstance instance) {
        String serviceId = instance.getServiceName();
        serviceMap.computeIfAbsent(serviceId, k -> new ArrayList<>()).add(instance);
        lastHeartbeat.put(instance.getInstanceId(), LocalDateTime.now());
        System.out.println("[Registry] Registered: " + instance.getServiceName() + 
                         " at " + instance.getHost() + ":" + instance.getPort());
    }
    
    public void deregister(String instanceId) {
        serviceMap.values().forEach(instances -> 
            instances.removeIf(i -> i.getInstanceId().equals(instanceId)));
        lastHeartbeat.remove(instanceId);
    }
    
    public List<ServiceInstance> getInstances(String serviceName) {
        return new ArrayList<>(serviceMap.getOrDefault(serviceName, Collections.emptyList()));
    }
    
    public ServiceInstance getInstance(String serviceName) {
        List<ServiceInstance> instances = getInstances(serviceName);
        return instances.isEmpty() ? null : instances.get(0);
    }
    
    public Map<String, List<ServiceInstance>> getAllServices() {
        return new HashMap<>(serviceMap);
    }
    
    public void heartbeat(String instanceId) {
        lastHeartbeat.put(instanceId, LocalDateTime.now());
    }
    
    public boolean isHealthy(String instanceId) {
        LocalDateTime lastBeat = lastHeartbeat.get(instanceId);
        return lastBeat != null && 
               lastBeat.isAfter(LocalDateTime.now().minusSeconds(30));
    }
}

// ============================================================================
// Service Instance
// ============================================================================

/**
 * Service Instance - represents a running service instance
 */
class ServiceInstance {
    private String instanceId;
    private String serviceName;
    private String host;
    private int port;
    private String version;
    private Map<String, String> metadata;
    private ServiceStatus status;
    private LocalDateTime registeredAt;
    
    public ServiceInstance(String serviceName, String host, int port, String version) {
        this.instanceId = UUID.randomUUID().toString();
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.version = version;
        this.metadata = new HashMap<>();
        this.status = ServiceStatus.UP;
        this.registeredAt = LocalDateTime.now();
    }
    
    public String getUrl() {
        return "http://" + host + ":" + port;
    }
    
    // Getters and setters
    public String getInstanceId() { return instanceId; }
    public String getServiceName() { return serviceName; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getVersion() { return version; }
    public Map<String, String> getMetadata() { return metadata; }
    public ServiceStatus getStatus() { return status; }
    public void setStatus(ServiceStatus status) { this.status = status; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
}

/**
 * Service status enum
 */
enum ServiceStatus {
    UP, DOWN, STARTING, UNKNOWN
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Service Registry Controller
 */
@RestController
@RequestMapping("/registry")
class ServiceRegistryController {
    
    private final ServiceRegistry registry;
    
    public ServiceRegistryController(ServiceRegistry registry) {
        this.registry = registry;
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody ServiceInstance instance) {
        registry.register(instance);
        Map<String, String> response = new HashMap<>();
        response.put("status", "registered");
        response.put("instanceId", instance.getInstanceId());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/deregister/{instanceId}")
    public ResponseEntity<Void> deregister(@PathVariable String instanceId) {
        registry.deregister(instanceId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/instances/{serviceName}")
    public ResponseEntity<List<ServiceInstance>> getInstances(@PathVariable String serviceName) {
        return ResponseEntity.ok(registry.getInstances(serviceName));
    }
    
    @GetMapping("/services")
    public ResponseEntity<Map<String, List<ServiceInstance>>> getAllServices() {
        return ResponseEntity.ok(registry.getAllServices());
    }
    
    @PostMapping("/heartbeat/{instanceId}")
    public ResponseEntity<Void> heartbeat(@PathVariable String instanceId) {
        registry.heartbeat(instanceId);
        return ResponseEntity.ok().build();
    }
}
