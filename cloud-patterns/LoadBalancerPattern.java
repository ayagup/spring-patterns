package com.example.cloud.loadbalancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Load Balancer Pattern - Demonstrates Client-Side Load Balancing
 * 
 * This pattern shows how to:
 * 1. Implement round-robin load balancing
 * 2. Implement weighted load balancing
 * 3. Implement least connections strategy
 * 4. Implement random selection
 * 5. Use Spring Cloud LoadBalancer
 * 6. Create custom load balancer algorithms
 * 7. Implement sticky sessions
 * 8. Handle instance health awareness
 * 9. Implement zone-aware load balancing
 * 10. Monitor load balancer metrics
 * 
 * Key Concepts:
 * - Client-Side LB: Client chooses server instance
 * - Server-Side LB: Load balancer sits between client and servers
 * - Round Robin: Distribute requests evenly
 * - Weighted: Prefer instances with higher weights
 * - Least Connections: Route to least busy instance
 * 
 * Load Balancing Strategies:
 * 1. Round Robin - Sequential distribution
 * 2. Weighted Round Robin - Weight-based distribution
 * 3. Random - Random selection
 * 4. Least Connections - Least active connections
 * 5. IP Hash - Consistent hashing by client IP
 * 6. Least Response Time - Fastest responding instance
 * 
 * Dependencies:
 * - spring-cloud-starter-loadbalancer
 * - spring-cloud-starter-netflix-ribbon (legacy)
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class LoadBalancerPattern {

    public static void main(String[] args) {
        SpringApplication.run(LoadBalancerPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("LOAD BALANCER PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateStrategies();
        demonstrateConfiguration();
        
        System.out.println("\nApplication is running. Test load balancing at:");
        System.out.println("GET /api/lb/call-service");
        System.out.println("GET /api/lb/stats");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateStrategies() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("LOAD BALANCING STRATEGIES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Round Robin: server1, server2, server3, server1...");
        System.out.println("2. Weighted: server1(70%), server2(20%), server3(10%)");
        System.out.println("3. Random: Random selection");
        System.out.println("4. Least Connections: Route to server with fewest connections");
        System.out.println("5. IP Hash: Same client → same server");
        System.out.println("6. Least Response Time: Fastest server");
    }
    
    private static void demonstrateConfiguration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONFIGURATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\nSpring Cloud LoadBalancer:");
        System.out.println("spring.cloud.loadbalancer.ribbon.enabled=false");
        System.out.println("spring.cloud.loadbalancer.cache.enabled=true");
    }
}

/**
 * Load Balancer Configuration
 */
@Configuration
class LoadBalancerConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

/**
 * Server Instance Model
 */
class ServerInstance {
    private String id;
    private String host;
    private int port;
    private int weight;
    private int activeConnections;
    private long totalRequests;
    private double avgResponseTime;
    private boolean healthy;
    
    public ServerInstance(String id, String host, int port, int weight) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.weight = weight;
        this.activeConnections = 0;
        this.totalRequests = 0;
        this.avgResponseTime = 0.0;
        this.healthy = true;
    }
    
    public String getUrl() {
        return "http://" + host + ":" + port;
    }
    
    public void incrementConnections() {
        activeConnections++;
        totalRequests++;
    }
    
    public void decrementConnections() {
        activeConnections = Math.max(0, activeConnections - 1);
    }
    
    // Getters and setters
    public String getId() { return id; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public int getWeight() { return weight; }
    public int getActiveConnections() { return activeConnections; }
    public long getTotalRequests() { return totalRequests; }
    public double getAvgResponseTime() { return avgResponseTime; }
    public boolean isHealthy() { return healthy; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }
    public void setAvgResponseTime(double avgResponseTime) { 
        this.avgResponseTime = avgResponseTime; 
    }
}

/**
 * Round Robin Load Balancer
 */
class RoundRobinLoadBalancer {
    private final List<ServerInstance> servers;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    public RoundRobinLoadBalancer(List<ServerInstance> servers) {
        this.servers = new ArrayList<>(servers);
    }
    
    public ServerInstance selectServer() {
        List<ServerInstance> healthyServers = getHealthyServers();
        if (healthyServers.isEmpty()) {
            throw new RuntimeException("No healthy servers available");
        }
        
        int index = Math.abs(currentIndex.getAndIncrement() % healthyServers.size());
        return healthyServers.get(index);
    }
    
    private List<ServerInstance> getHealthyServers() {
        return servers.stream()
            .filter(ServerInstance::isHealthy)
            .toList();
    }
}

/**
 * Weighted Round Robin Load Balancer
 */
class WeightedRoundRobinLoadBalancer {
    private final List<ServerInstance> servers;
    private final List<ServerInstance> weightedList;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    public WeightedRoundRobinLoadBalancer(List<ServerInstance> servers) {
        this.servers = new ArrayList<>(servers);
        this.weightedList = buildWeightedList();
    }
    
    private List<ServerInstance> buildWeightedList() {
        List<ServerInstance> list = new ArrayList<>();
        for (ServerInstance server : servers) {
            for (int i = 0; i < server.getWeight(); i++) {
                list.add(server);
            }
        }
        return list;
    }
    
    public ServerInstance selectServer() {
        if (weightedList.isEmpty()) {
            throw new RuntimeException("No servers configured");
        }
        
        int index = Math.abs(currentIndex.getAndIncrement() % weightedList.size());
        ServerInstance server = weightedList.get(index);
        
        if (!server.isHealthy()) {
            return selectServer(); // Retry with next server
        }
        
        return server;
    }
}

/**
 * Random Load Balancer
 */
class RandomLoadBalancer {
    private final List<ServerInstance> servers;
    
    public RandomLoadBalancer(List<ServerInstance> servers) {
        this.servers = new ArrayList<>(servers);
    }
    
    public ServerInstance selectServer() {
        List<ServerInstance> healthyServers = servers.stream()
            .filter(ServerInstance::isHealthy)
            .toList();
        
        if (healthyServers.isEmpty()) {
            throw new RuntimeException("No healthy servers available");
        }
        
        int index = ThreadLocalRandom.current().nextInt(healthyServers.size());
        return healthyServers.get(index);
    }
}

/**
 * Least Connections Load Balancer
 */
class LeastConnectionsLoadBalancer {
    private final List<ServerInstance> servers;
    
    public LeastConnectionsLoadBalancer(List<ServerInstance> servers) {
        this.servers = new ArrayList<>(servers);
    }
    
    public ServerInstance selectServer() {
        return servers.stream()
            .filter(ServerInstance::isHealthy)
            .min(Comparator.comparingInt(ServerInstance::getActiveConnections))
            .orElseThrow(() -> new RuntimeException("No healthy servers available"));
    }
}

/**
 * IP Hash Load Balancer (Sticky Sessions)
 */
class IPHashLoadBalancer {
    private final List<ServerInstance> servers;
    
    public IPHashLoadBalancer(List<ServerInstance> servers) {
        this.servers = new ArrayList<>(servers);
    }
    
    public ServerInstance selectServer(String clientIP) {
        List<ServerInstance> healthyServers = servers.stream()
            .filter(ServerInstance::isHealthy)
            .toList();
        
        if (healthyServers.isEmpty()) {
            throw new RuntimeException("No healthy servers available");
        }
        
        int hash = Math.abs(clientIP.hashCode());
        int index = hash % healthyServers.size();
        return healthyServers.get(index);
    }
}

/**
 * Least Response Time Load Balancer
 */
class LeastResponseTimeLoadBalancer {
    private final List<ServerInstance> servers;
    
    public LeastResponseTimeLoadBalancer(List<ServerInstance> servers) {
        this.servers = new ArrayList<>(servers);
    }
    
    public ServerInstance selectServer() {
        return servers.stream()
            .filter(ServerInstance::isHealthy)
            .min(Comparator.comparingDouble(ServerInstance::getAvgResponseTime))
            .orElseThrow(() -> new RuntimeException("No healthy servers available"));
    }
}

/**
 * Load Balancer Service
 */
@Service
class LoadBalancerService {
    
    private final List<ServerInstance> servers;
    private final Map<String, Object> loadBalancers;
    
    public LoadBalancerService() {
        // Initialize servers
        servers = Arrays.asList(
            new ServerInstance("server-1", "localhost", 8081, 5),
            new ServerInstance("server-2", "localhost", 8082, 3),
            new ServerInstance("server-3", "localhost", 8083, 2)
        );
        
        // Initialize load balancers
        loadBalancers = new HashMap<>();
        loadBalancers.put("roundRobin", new RoundRobinLoadBalancer(servers));
        loadBalancers.put("weighted", new WeightedRoundRobinLoadBalancer(servers));
        loadBalancers.put("random", new RandomLoadBalancer(servers));
        loadBalancers.put("leastConnections", new LeastConnectionsLoadBalancer(servers));
        loadBalancers.put("ipHash", new IPHashLoadBalancer(servers));
        loadBalancers.put("leastResponseTime", new LeastResponseTimeLoadBalancer(servers));
    }
    
    public ServerInstance selectServer(String strategy) {
        Object lb = loadBalancers.get(strategy);
        
        if (lb instanceof RoundRobinLoadBalancer) {
            return ((RoundRobinLoadBalancer) lb).selectServer();
        } else if (lb instanceof WeightedRoundRobinLoadBalancer) {
            return ((WeightedRoundRobinLoadBalancer) lb).selectServer();
        } else if (lb instanceof RandomLoadBalancer) {
            return ((RandomLoadBalancer) lb).selectServer();
        } else if (lb instanceof LeastConnectionsLoadBalancer) {
            return ((LeastConnectionsLoadBalancer) lb).selectServer();
        } else if (lb instanceof LeastResponseTimeLoadBalancer) {
            return ((LeastResponseTimeLoadBalancer) lb).selectServer();
        }
        
        throw new IllegalArgumentException("Unknown strategy: " + strategy);
    }
    
    public ServerInstance selectServerWithIP(String clientIP) {
        IPHashLoadBalancer lb = (IPHashLoadBalancer) loadBalancers.get("ipHash");
        return lb.selectServer(clientIP);
    }
    
    public List<ServerInstance> getAllServers() {
        return new ArrayList<>(servers);
    }
    
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalServers", servers.size());
        stats.put("healthyServers", servers.stream().filter(ServerInstance::isHealthy).count());
        stats.put("totalRequests", servers.stream().mapToLong(ServerInstance::getTotalRequests).sum());
        stats.put("activeConnections", servers.stream().mapToInt(ServerInstance::getActiveConnections).sum());
        stats.put("servers", servers);
        return stats;
    }
}

/**
 * REST Controller demonstrating load balancing
 */
@RestController
@RequestMapping("/api/lb")
class LoadBalancerController {
    
    private final LoadBalancerService loadBalancerService;
    private final RestTemplate restTemplate;
    
    public LoadBalancerController(LoadBalancerService loadBalancerService,
                                 RestTemplate restTemplate) {
        this.loadBalancerService = loadBalancerService;
        this.restTemplate = restTemplate;
    }
    
    @GetMapping("/call-service")
    public Map<String, Object> callService(
            @RequestParam(defaultValue = "roundRobin") String strategy,
            @RequestParam(required = false) String clientIP) {
        
        ServerInstance server;
        if ("ipHash".equals(strategy) && clientIP != null) {
            server = loadBalancerService.selectServerWithIP(clientIP);
        } else {
            server = loadBalancerService.selectServer(strategy);
        }
        
        server.incrementConnections();
        
        Map<String, Object> response = new HashMap<>();
        response.put("selectedServer", server.getId());
        response.put("url", server.getUrl());
        response.put("strategy", strategy);
        response.put("weight", server.getWeight());
        response.put("activeConnections", server.getActiveConnections());
        
        server.decrementConnections();
        
        return response;
    }
    
    @GetMapping("/servers")
    public List<ServerInstance> getServers() {
        return loadBalancerService.getAllServers();
    }
    
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return loadBalancerService.getStats();
    }
    
    @PostMapping("/servers/{serverId}/health")
    public String updateHealth(@PathVariable String serverId,
                              @RequestParam boolean healthy) {
        loadBalancerService.getAllServers().stream()
            .filter(s -> s.getId().equals(serverId))
            .findFirst()
            .ifPresent(s -> s.setHealthy(healthy));
        
        return "Server " + serverId + " health updated to: " + healthy;
    }
}
