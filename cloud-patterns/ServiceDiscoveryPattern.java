package com.example.cloud.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Service Discovery Pattern - Demonstrates Service Registration and Discovery
 * 
 * This pattern shows how to:
 * 1. Register services with discovery server (Eureka/Consul)
 * 2. Discover service instances dynamically
 * 3. Implement client-side service discovery
 * 4. Handle service health checks
 * 5. Implement service instance selection
 * 6. Use DiscoveryClient for service lookup
 * 7. Implement custom service registry
 * 8. Handle service deregistration
 * 9. Implement heartbeat mechanisms
 * 10. Use service metadata for routing
 * 
 * Key Concepts:
 * - Service Registry: Central registry of service instances
 * - Service Registration: Services register themselves on startup
 * - Service Discovery: Clients find service instances dynamically
 * - Health Checks: Monitor service availability
 * - Load Balancing: Distribute requests across instances
 * 
 * Discovery Servers:
 * - Netflix Eureka
 * - HashiCorp Consul
 * - Apache Zookeeper
 * - Kubernetes Service Discovery
 * 
 * Dependencies:
 * - spring-cloud-starter-netflix-eureka-server
 * - spring-cloud-starter-netflix-eureka-client
 * - spring-cloud-starter-consul-discovery
 * 
 * Configuration:
 * eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
 * eureka.instance.preferIpAddress=true
 * spring.application.name=my-service
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class ServiceDiscoveryPattern {

    public static void main(String[] args) {
        // Demonstrate different discovery patterns
        demonstrateServiceDiscovery();
        demonstrateEurekaServer();
        demonstrateConsulDiscovery();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SERVICE DISCOVERY PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DISCOVERY SERVER OPTIONS");
        System.out.println("=".repeat(80));
        System.out.println("\n1. Netflix Eureka");
        System.out.println("   - Port: 8761");
        System.out.println("   - UI: http://localhost:8761");
        System.out.println("   - Self-preservation mode");
        
        System.out.println("\n2. HashiCorp Consul");
        System.out.println("   - Port: 8500");
        System.out.println("   - UI: http://localhost:8500/ui");
        System.out.println("   - Health checks, KV store");
        
        System.out.println("\n3. Apache Zookeeper");
        System.out.println("   - Port: 2181");
        System.out.println("   - Distributed coordination");
        
        System.out.println("\n4. Kubernetes");
        System.out.println("   - Built-in service discovery");
        System.out.println("   - DNS-based discovery");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SERVICE REGISTRATION & DISCOVERY FLOW");
        System.out.println("=".repeat(80));
        System.out.println("\n1. Service Instance starts");
        System.out.println("2. Registers with Discovery Server");
        System.out.println("3. Sends periodic heartbeats");
        System.out.println("4. Client queries Discovery Server");
        System.out.println("5. Receives list of healthy instances");
        System.out.println("6. Selects instance (load balancing)");
        System.out.println("7. Makes request to selected instance");
        System.out.println("8. Service deregisters on shutdown");
        
        System.out.println("\nApplication ready. Press Ctrl+C to stop.\n");
    }
    
    private static void demonstrateServiceDiscovery() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SERVICE DISCOVERY CONCEPTS");
        System.out.println("=".repeat(80));
        
        System.out.println("\nWhy Service Discovery?");
        System.out.println("- Dynamic service instance locations");
        System.out.println("- Auto-scaling support");
        System.out.println("- Fault tolerance");
        System.out.println("- No hardcoded URLs");
        System.out.println("- Zero-downtime deployments");
        
        System.out.println("\nDiscovery Patterns:");
        System.out.println("Client-Side Discovery: Client queries registry and load balances");
        System.out.println("Server-Side Discovery: Load balancer queries registry");
    }
    
    private static void demonstrateEurekaServer() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("NETFLIX EUREKA CONFIGURATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\nEureka Server:");
        System.out.println("@EnableEurekaServer on main class");
        System.out.println("eureka.client.registerWithEureka=false");
        System.out.println("eureka.client.fetchRegistry=false");
        
        System.out.println("\nEureka Client:");
        System.out.println("@EnableDiscoveryClient or @EnableEurekaClient");
        System.out.println("eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/");
        System.out.println("eureka.instance.preferIpAddress=true");
    }
    
    private static void demonstrateConsulDiscovery() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONSUL DISCOVERY CONFIGURATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\nConsul Setup:");
        System.out.println("spring.cloud.consul.host=localhost");
        System.out.println("spring.cloud.consul.port=8500");
        System.out.println("spring.cloud.consul.discovery.healthCheckPath=/actuator/health");
        System.out.println("spring.cloud.consul.discovery.healthCheckInterval=10s");
    }
}

/**
 * Eureka Server Configuration
 */
@Configuration
@EnableEurekaServer
class EurekaServerConfig {
    // Eureka server will be started with this configuration
    // Access at: http://localhost:8761
}

/**
 * Service that registers with Discovery Server
 */
@SpringBootApplication
@EnableDiscoveryClient
class ServiceProviderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ServiceProviderApplication.class, args);
    }
}

/**
 * REST Controller in Service Provider
 */
@RestController
@RequestMapping("/api")
class ServiceProviderController {
    
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Service Provider");
        response.put("timestamp", LocalDateTime.now());
        response.put("instance", "instance-1");
        return response;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "service-provider");
        info.put("version", "1.0.0");
        info.put("status", "UP");
        return info;
    }
}

/**
 * Service Consumer using DiscoveryClient
 */
@Service
class ServiceConsumer {
    
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    
    public ServiceConsumer(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }
    
    /**
     * Discover and call service using DiscoveryClient
     */
    public String callService(String serviceId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        
        if (instances.isEmpty()) {
            throw new RuntimeException("No instances available for service: " + serviceId);
        }
        
        // Simple round-robin selection
        ServiceInstance instance = instances.get(
            ThreadLocalRandom.current().nextInt(instances.size())
        );
        
        String url = instance.getUri().toString() + "/api/hello";
        return restTemplate.getForObject(url, String.class);
    }
    
    /**
     * Get all registered services
     */
    public List<String> getAllServices() {
        return discoveryClient.getServices();
    }
    
    /**
     * Get instances of a specific service
     */
    public List<ServiceInstance> getServiceInstances(String serviceId) {
        return discoveryClient.getInstances(serviceId);
    }
}

/**
 * Custom Service Registry Implementation
 */
@Component
class CustomServiceRegistry {
    
    private final Map<String, List<ServiceInstanceInfo>> registry = new ConcurrentHashMap<>();
    
    /**
     * Register a service instance
     */
    public void register(String serviceId, ServiceInstanceInfo instance) {
        registry.computeIfAbsent(serviceId, k -> new ArrayList<>()).add(instance);
        System.out.printf("Registered service: %s, instance: %s%n", 
            serviceId, instance.getInstanceId());
    }
    
    /**
     * Deregister a service instance
     */
    public void deregister(String serviceId, String instanceId) {
        List<ServiceInstanceInfo> instances = registry.get(serviceId);
        if (instances != null) {
            instances.removeIf(i -> i.getInstanceId().equals(instanceId));
            System.out.printf("Deregistered service: %s, instance: %s%n", 
                serviceId, instanceId);
        }
    }
    
    /**
     * Get all instances of a service
     */
    public List<ServiceInstanceInfo> getInstances(String serviceId) {
        return registry.getOrDefault(serviceId, Collections.emptyList())
            .stream()
            .filter(ServiceInstanceInfo::isHealthy)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all registered services
     */
    public Set<String> getAllServices() {
        return registry.keySet();
    }
    
    /**
     * Update instance health status
     */
    public void updateHealth(String serviceId, String instanceId, boolean healthy) {
        List<ServiceInstanceInfo> instances = registry.get(serviceId);
        if (instances != null) {
            instances.stream()
                .filter(i -> i.getInstanceId().equals(instanceId))
                .findFirst()
                .ifPresent(i -> i.setHealthy(healthy));
        }
    }
}

/**
 * Service Instance Information
 */
class ServiceInstanceInfo {
    private String instanceId;
    private String host;
    private int port;
    private boolean healthy;
    private Map<String, String> metadata;
    private LocalDateTime registrationTime;
    private LocalDateTime lastHeartbeat;
    
    public ServiceInstanceInfo(String instanceId, String host, int port) {
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.healthy = true;
        this.metadata = new HashMap<>();
        this.registrationTime = LocalDateTime.now();
        this.lastHeartbeat = LocalDateTime.now();
    }
    
    public URI getUri() {
        return URI.create("http://" + host + ":" + port);
    }
    
    public void heartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
        this.healthy = true;
    }
    
    // Getters and setters
    public String getInstanceId() { return instanceId; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public boolean isHealthy() { return healthy; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }
    public Map<String, String> getMetadata() { return metadata; }
    public LocalDateTime getRegistrationTime() { return registrationTime; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
}

/**
 * Service Discovery Client
 */
@Service
class ServiceDiscoveryClient {
    
    private final CustomServiceRegistry registry;
    
    public ServiceDiscoveryClient(CustomServiceRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Discover service instance using different strategies
     */
    public ServiceInstanceInfo discoverService(String serviceId, 
                                               SelectionStrategy strategy) {
        List<ServiceInstanceInfo> instances = registry.getInstances(serviceId);
        
        if (instances.isEmpty()) {
            throw new RuntimeException("No healthy instances for service: " + serviceId);
        }
        
        return strategy.select(instances);
    }
}

/**
 * Instance selection strategies
 */
interface SelectionStrategy {
    ServiceInstanceInfo select(List<ServiceInstanceInfo> instances);
}

/**
 * Round-robin selection strategy
 */
class RoundRobinStrategy implements SelectionStrategy {
    private int currentIndex = 0;
    
    @Override
    public ServiceInstanceInfo select(List<ServiceInstanceInfo> instances) {
        ServiceInstanceInfo instance = instances.get(currentIndex % instances.size());
        currentIndex++;
        return instance;
    }
}

/**
 * Random selection strategy
 */
class RandomStrategy implements SelectionStrategy {
    @Override
    public ServiceInstanceInfo select(List<ServiceInstanceInfo> instances) {
        int index = ThreadLocalRandom.current().nextInt(instances.size());
        return instances.get(index);
    }
}

/**
 * Least connections strategy
 */
class LeastConnectionsStrategy implements SelectionStrategy {
    private final Map<String, Integer> connections = new ConcurrentHashMap<>();
    
    @Override
    public ServiceInstanceInfo select(List<ServiceInstanceInfo> instances) {
        return instances.stream()
            .min(Comparator.comparingInt(i -> 
                connections.getOrDefault(i.getInstanceId(), 0)))
            .orElse(instances.get(0));
    }
    
    public void incrementConnections(String instanceId) {
        connections.merge(instanceId, 1, Integer::sum);
    }
    
    public void decrementConnections(String instanceId) {
        connections.computeIfPresent(instanceId, (k, v) -> Math.max(0, v - 1));
    }
}

/**
 * Health Check Service
 */
@Component
class HealthCheckService {
    
    private final CustomServiceRegistry registry;
    
    public HealthCheckService(CustomServiceRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Perform health check on all instances
     */
    public void performHealthChecks() {
        for (String serviceId : registry.getAllServices()) {
            List<ServiceInstanceInfo> instances = registry.getInstances(serviceId);
            
            for (ServiceInstanceInfo instance : instances) {
                boolean healthy = checkInstanceHealth(instance);
                registry.updateHealth(serviceId, instance.getInstanceId(), healthy);
            }
        }
    }
    
    private boolean checkInstanceHealth(ServiceInstanceInfo instance) {
        try {
            // Simulate health check HTTP call
            // In real implementation, call instance's /health endpoint
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

/**
 * Service registration and deregistration handler
 */
@Component
class ServiceLifecycleManager {
    
    private final CustomServiceRegistry registry;
    private final String serviceId;
    private final ServiceInstanceInfo currentInstance;
    
    public ServiceLifecycleManager(CustomServiceRegistry registry) {
        this.registry = registry;
        this.serviceId = "my-service";
        this.currentInstance = new ServiceInstanceInfo(
            UUID.randomUUID().toString(),
            "localhost",
            8080
        );
    }
    
    /**
     * Register service on startup
     */
    public void onStartup() {
        currentInstance.getMetadata().put("version", "1.0.0");
        currentInstance.getMetadata().put("zone", "us-east-1");
        
        registry.register(serviceId, currentInstance);
        
        // Start heartbeat
        startHeartbeat();
    }
    
    /**
     * Deregister service on shutdown
     */
    public void onShutdown() {
        registry.deregister(serviceId, currentInstance.getInstanceId());
    }
    
    /**
     * Send periodic heartbeats
     */
    private void startHeartbeat() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentInstance.heartbeat();
            }
        }, 0, 30000); // Every 30 seconds
    }
}

/**
 * REST Controller demonstrating service discovery
 */
@RestController
@RequestMapping("/api/discovery")
class DiscoveryDemoController {
    
    private final ServiceDiscoveryClient discoveryClient;
    private final CustomServiceRegistry registry;
    private final DiscoveryClient springDiscoveryClient;
    
    public DiscoveryDemoController(ServiceDiscoveryClient discoveryClient,
                                   CustomServiceRegistry registry,
                                   DiscoveryClient springDiscoveryClient) {
        this.discoveryClient = discoveryClient;
        this.registry = registry;
        this.springDiscoveryClient = springDiscoveryClient;
    }
    
    @GetMapping("/services")
    public Set<String> getAllServices() {
        return registry.getAllServices();
    }
    
    @GetMapping("/services/{serviceId}/instances")
    public List<ServiceInstanceInfo> getInstances(@PathVariable String serviceId) {
        return registry.getInstances(serviceId);
    }
    
    @PostMapping("/services/{serviceId}/register")
    public String registerInstance(@PathVariable String serviceId,
                                   @RequestParam String host,
                                   @RequestParam int port) {
        String instanceId = UUID.randomUUID().toString();
        ServiceInstanceInfo instance = new ServiceInstanceInfo(instanceId, host, port);
        registry.register(serviceId, instance);
        return "Registered instance: " + instanceId;
    }
    
    @DeleteMapping("/services/{serviceId}/instances/{instanceId}")
    public String deregisterInstance(@PathVariable String serviceId,
                                     @PathVariable String instanceId) {
        registry.deregister(serviceId, instanceId);
        return "Deregistered instance: " + instanceId;
    }
    
    @GetMapping("/discover/{serviceId}")
    public ServiceInstanceInfo discoverService(@PathVariable String serviceId) {
        return discoveryClient.discoverService(serviceId, new RandomStrategy());
    }
}

/**
 * Configuration for RestTemplate with LoadBalancing
 */
@Configuration
class DiscoveryConfiguration {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
