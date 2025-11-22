package com.example.microservices.serversideloadbalancing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;

/**
 * Server-Side Load Balancing Pattern
 * 
 * This pattern demonstrates server-side load balancing where a central load balancer
 * (reverse proxy) distributes incoming requests across multiple backend servers.
 * Unlike client-side load balancing, the load balancing logic resides on the server side.
 * 
 * Key Components:
 * 1. LoadBalancerGateway - Central gateway that receives all requests
 * 2. BackendServer - Represents backend service instances
 * 3. HealthChecker - Monitors backend server health
 * 4. LoadBalancingStrategy - Different algorithms for distributing load
 * 
 * Use Cases:
 * - API Gateway scenarios
 * - Nginx/HAProxy configurations
 * - Cloud load balancers (AWS ELB, Azure Load Balancer)
 * - SSL termination at load balancer
 * - Centralized routing and traffic management
 */

@SpringBootApplication
public class ServerSideLoadBalancingPattern {

    public static void main(String[] args) {
        SpringApplication.run(ServerSideLoadBalancingPattern.class, args);
        
        // Demonstration
        System.out.println("=== Server-Side Load Balancing Pattern Demo ===\n");
        
        LoadBalancerGateway gateway = new LoadBalancerGateway();
        HealthChecker healthChecker = new HealthChecker(gateway);
        
        // Register backend servers
        gateway.registerServer(new BackendServer("backend-1", "192.168.1.10", 8080, "/api"));
        gateway.registerServer(new BackendServer("backend-2", "192.168.1.11", 8080, "/api"));
        gateway.registerServer(new BackendServer("backend-3", "192.168.1.12", 8080, "/api"));
        
        System.out.println("Registered " + gateway.getServerCount() + " backend servers\n");
        
        // Simulate incoming requests
        System.out.println("Round Robin Load Distribution:");
        for (int i = 1; i <= 6; i++) {
            BackendServer server = gateway.routeRequest("/api/users");
            System.out.println("Request " + i + " -> " + server.getServerId() + " (" + server.getHost() + ")");
        }
        
        System.out.println("\nServer Health Status:");
        gateway.getServerPool().forEach(server -> {
            boolean healthy = healthChecker.isHealthy(server);
            System.out.println(server.getServerId() + ": " + (healthy ? "HEALTHY" : "UNHEALTHY") + 
                             " (Active Connections: " + server.getActiveConnections() + ")");
        });
        
        // Demonstrate weighted distribution
        System.out.println("\nWeighted Load Distribution (based on capacity):");
        gateway.setStrategy(new WeightedLoadBalancingStrategy());
        gateway.getServerPool().get(0).setWeight(5);  // Higher capacity
        gateway.getServerPool().get(1).setWeight(3);
        gateway.getServerPool().get(2).setWeight(2);
        
        Map<String, Integer> distribution = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            BackendServer server = gateway.routeRequest("/api/products");
            distribution.merge(server.getServerId(), 1, Integer::sum);
        }
        distribution.forEach((serverId, count) -> 
            System.out.println(serverId + ": " + count + " requests"));
    }
}

/**
 * Load Balancer Gateway - Central reverse proxy
 * Receives all incoming requests and distributes them to backend servers
 */
@Service
class LoadBalancerGateway {
    private final List<BackendServer> serverPool = new ArrayList<>();
    private LoadBalancingStrategy strategy = new RoundRobinStrategy();
    private final Map<String, Integer> stickySessionMap = new ConcurrentHashMap<>();
    
    public void registerServer(BackendServer server) {
        serverPool.add(server);
        System.out.println("Registered backend server: " + server.getServerId() + 
                         " at " + server.getHost() + ":" + server.getPort());
    }
    
    public void deregisterServer(String serverId) {
        serverPool.removeIf(server -> server.getServerId().equals(serverId));
        System.out.println("Deregistered backend server: " + serverId);
    }
    
    public BackendServer routeRequest(String path) {
        return routeRequest(path, null);
    }
    
    public BackendServer routeRequest(String path, String sessionId) {
        // Check for sticky session
        if (sessionId != null && stickySessionMap.containsKey(sessionId)) {
            int serverIndex = stickySessionMap.get(sessionId);
            if (serverIndex < serverPool.size()) {
                BackendServer server = serverPool.get(serverIndex);
                if (server.isHealthy()) {
                    server.incrementConnections();
                    return server;
                }
            }
        }
        
        // Get healthy servers only
        List<BackendServer> healthyServers = serverPool.stream()
            .filter(BackendServer::isHealthy)
            .toList();
        
        if (healthyServers.isEmpty()) {
            throw new RuntimeException("No healthy backend servers available");
        }
        
        // Apply load balancing strategy
        BackendServer selectedServer = strategy.selectServer(healthyServers);
        selectedServer.incrementConnections();
        
        // Store sticky session if provided
        if (sessionId != null) {
            stickySessionMap.put(sessionId, serverPool.indexOf(selectedServer));
        }
        
        return selectedServer;
    }
    
    public void setStrategy(LoadBalancingStrategy strategy) {
        this.strategy = strategy;
    }
    
    public List<BackendServer> getServerPool() {
        return serverPool;
    }
    
    public int getServerCount() {
        return serverPool.size();
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalServers", serverPool.size());
        stats.put("healthyServers", serverPool.stream().filter(BackendServer::isHealthy).count());
        stats.put("totalConnections", serverPool.stream().mapToInt(BackendServer::getActiveConnections).sum());
        stats.put("strategy", strategy.getClass().getSimpleName());
        return stats;
    }
}

/**
 * Backend Server representation
 */
class BackendServer {
    private final String serverId;
    private final String host;
    private final int port;
    private final String contextPath;
    private boolean healthy = true;
    private int activeConnections = 0;
    private int weight = 1;  // For weighted load balancing
    private LocalDateTime lastHealthCheck;
    private long responseTime = 100; // Average response time in ms
    
    public BackendServer(String serverId, String host, int port, String contextPath) {
        this.serverId = serverId;
        this.host = host;
        this.port = port;
        this.contextPath = contextPath;
        this.lastHealthCheck = LocalDateTime.now();
    }
    
    public void incrementConnections() {
        activeConnections++;
    }
    
    public void decrementConnections() {
        if (activeConnections > 0) {
            activeConnections--;
        }
    }
    
    public String getUrl() {
        return "http://" + host + ":" + port + contextPath;
    }
    
    // Getters and setters
    public String getServerId() { return serverId; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getContextPath() { return contextPath; }
    public boolean isHealthy() { return healthy; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }
    public int getActiveConnections() { return activeConnections; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public LocalDateTime getLastHealthCheck() { return lastHealthCheck; }
    public void setLastHealthCheck(LocalDateTime lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; }
    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
}

/**
 * Load Balancing Strategy Interface
 */
interface LoadBalancingStrategy {
    BackendServer selectServer(List<BackendServer> servers);
}

/**
 * Round Robin Strategy - Sequential distribution
 */
class RoundRobinStrategy implements LoadBalancingStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);
    
    @Override
    public BackendServer selectServer(List<BackendServer> servers) {
        int index = counter.getAndIncrement() % servers.size();
        return servers.get(index);
    }
}

/**
 * Least Connections Strategy - Route to server with fewest active connections
 */
class LeastConnectionsStrategy implements LoadBalancingStrategy {
    @Override
    public BackendServer selectServer(List<BackendServer> servers) {
        return servers.stream()
            .min(Comparator.comparingInt(BackendServer::getActiveConnections))
            .orElse(servers.get(0));
    }
}

/**
 * Weighted Load Balancing Strategy - Distribute based on server capacity/weight
 */
class WeightedLoadBalancingStrategy implements LoadBalancingStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);
    
    @Override
    public BackendServer selectServer(List<BackendServer> servers) {
        int totalWeight = servers.stream().mapToInt(BackendServer::getWeight).sum();
        int random = counter.getAndIncrement() % totalWeight;
        
        int cumulativeWeight = 0;
        for (BackendServer server : servers) {
            cumulativeWeight += server.getWeight();
            if (random < cumulativeWeight) {
                return server;
            }
        }
        
        return servers.get(0);
    }
}

/**
 * IP Hash Strategy - Consistent routing based on client IP
 */
class IPHashStrategy implements LoadBalancingStrategy {
    @Override
    public BackendServer selectServer(List<BackendServer> servers) {
        // In real implementation, would use actual client IP
        String clientIP = "192.168.1.100";
        int hash = Math.abs(clientIP.hashCode());
        int index = hash % servers.size();
        return servers.get(index);
    }
}

/**
 * Health Checker - Monitors backend server health
 */
class HealthChecker {
    private final LoadBalancerGateway gateway;
    
    public HealthChecker(LoadBalancerGateway gateway) {
        this.gateway = gateway;
    }
    
    public boolean isHealthy(BackendServer server) {
        // Simulate health check
        // In real implementation, would make HTTP request to health endpoint
        return server.isHealthy() && server.getActiveConnections() < 100;
    }
    
    public void performHealthCheck(BackendServer server) {
        try {
            // Simulate HTTP health check to server.getUrl() + "/health"
            boolean healthy = checkServerHealth(server);
            server.setHealthy(healthy);
            server.setLastHealthCheck(LocalDateTime.now());
            
            if (!healthy) {
                System.out.println("Server " + server.getServerId() + " marked as unhealthy");
            }
        } catch (Exception e) {
            server.setHealthy(false);
            System.err.println("Health check failed for " + server.getServerId());
        }
    }
    
    private boolean checkServerHealth(BackendServer server) {
        // Simulate health check logic
        // Return true if server responds to health endpoint within timeout
        return Math.random() > 0.1; // 90% healthy
    }
    
    public void startPeriodicHealthChecks(long intervalMillis) {
        // In real implementation, would use scheduled executor
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gateway.getServerPool().forEach(HealthChecker.this::performHealthCheck);
            }
        }, 0, intervalMillis);
    }
}

/**
 * REST Controller for Load Balancer Management
 */
@RestController
@RequestMapping("/lb")
class LoadBalancerController {
    private final LoadBalancerGateway gateway;
    
    public LoadBalancerController(LoadBalancerGateway gateway) {
        this.gateway = gateway;
    }
    
    @PostMapping("/servers")
    public ResponseEntity<String> registerServer(@RequestBody BackendServer server) {
        gateway.registerServer(server);
        return ResponseEntity.ok("Server registered: " + server.getServerId());
    }
    
    @DeleteMapping("/servers/{serverId}")
    public ResponseEntity<String> deregisterServer(@PathVariable String serverId) {
        gateway.deregisterServer(serverId);
        return ResponseEntity.ok("Server deregistered: " + serverId);
    }
    
    @GetMapping("/servers")
    public ResponseEntity<List<BackendServer>> getServers() {
        return ResponseEntity.ok(gateway.getServerPool());
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(gateway.getStatistics());
    }
    
    @PostMapping("/strategy/{strategyType}")
    public ResponseEntity<String> setStrategy(@PathVariable String strategyType) {
        LoadBalancingStrategy strategy = switch (strategyType.toLowerCase()) {
            case "roundrobin" -> new RoundRobinStrategy();
            case "leastconnections" -> new LeastConnectionsStrategy();
            case "weighted" -> new WeightedLoadBalancingStrategy();
            case "iphash" -> new IPHashStrategy();
            default -> throw new IllegalArgumentException("Unknown strategy: " + strategyType);
        };
        
        gateway.setStrategy(strategy);
        return ResponseEntity.ok("Strategy set to: " + strategyType);
    }
}
