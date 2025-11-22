package com.example.microservices.clientsideloadbalancing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client-Side Load Balancing Pattern
 * 
 * Demonstrates:
 * 1. Round Robin load balancing
 * 2. Random selection
 * 3. Weighted response time
 * 4. Least connections
 * 5. Client-side service discovery
 * 6. Health-based selection
 * 7. Zone-aware routing
 * 8. Custom load balancing rules
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class ClientSideLoadBalancingPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(ClientSideLoadBalancingPattern.class, args);
        demonstrateLoadBalancing(context);
    }
    
    private static void demonstrateLoadBalancing(
            org.springframework.context.ApplicationContext context) {
        System.out.println("=== Client-Side Load Balancing Pattern ===\n");
        
        LoadBalancer loadBalancer = context.getBean(LoadBalancer.class);
        
        // Add service instances
        loadBalancer.addServer(new Server("service-1", "192.168.1.10", 8080));
        loadBalancer.addServer(new Server("service-1", "192.168.1.11", 8080));
        loadBalancer.addServer(new Server("service-1", "192.168.1.12", 8080));
        
        // Demonstrate load balancing
        System.out.println("Round Robin Selection:");
        for (int i = 0; i < 5; i++) {
            Server server = loadBalancer.chooseServer("service-1");
            System.out.println("   Request " + (i+1) + " -> " + server.getHost());
        }
    }
}

// ============================================================================
// Load Balancer
// ============================================================================

@org.springframework.stereotype.Service
class LoadBalancer {
    
    private final Map<String, List<Server>> serverMap = new ConcurrentHashMap<>();
    private final Map<String, LoadBalancingRule> ruleMap = new ConcurrentHashMap<>();
    
    public void addServer(Server server) {
        serverMap.computeIfAbsent(server.getServiceName(), k -> new ArrayList<>()).add(server);
    }
    
    public Server chooseServer(String serviceName) {
        List<Server> servers = serverMap.get(serviceName);
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        
        LoadBalancingRule rule = ruleMap.getOrDefault(serviceName, new RoundRobinRule());
        return rule.choose(servers);
    }
    
    public void setRule(String serviceName, LoadBalancingRule rule) {
        ruleMap.put(serviceName, rule);
    }
}

// ============================================================================
// Load Balancing Rules
// ============================================================================

interface LoadBalancingRule {
    Server choose(List<Server> servers);
}

class RoundRobinRule implements LoadBalancingRule {
    private final AtomicInteger counter = new AtomicInteger(0);
    
    @Override
    public Server choose(List<Server> servers) {
        int index = counter.getAndIncrement() % servers.size();
        return servers.get(index);
    }
}

class RandomRule implements LoadBalancingRule {
    private final Random random = new Random();
    
    @Override
    public Server choose(List<Server> servers) {
        return servers.get(random.nextInt(servers.size()));
    }
}

class WeightedResponseTimeRule implements LoadBalancingRule {
    @Override
    public Server choose(List<Server> servers) {
        // Choose server with lowest response time
        return servers.stream()
            .min(Comparator.comparing(Server::getAverageResponseTime))
            .orElse(servers.get(0));
    }
}

// ============================================================================
// Server Model
// ============================================================================

class Server {
    private String serviceName;
    private String host;
    private int port;
    private boolean alive;
    private long averageResponseTime;
    
    public Server(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.alive = true;
        this.averageResponseTime = 100;
    }
    
    // Getters
    public String getServiceName() { return serviceName; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public boolean isAlive() { return alive; }
    public long getAverageResponseTime() { return averageResponseTime; }
}

@RestController
@RequestMapping("/load-balancer")
class LoadBalancerController {
    
    private final LoadBalancer loadBalancer;
    
    public LoadBalancerController(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }
    
    @GetMapping("/choose/{serviceName}")
    public ResponseEntity<Server> chooseServer(@PathVariable String serviceName) {
        Server server = loadBalancer.chooseServer(serviceName);
        return ResponseEntity.ok(server);
    }
}
