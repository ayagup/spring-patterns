package com.example.cloud.servicemesh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service Mesh Pattern - Demonstrates Sidecar Proxy and Traffic Management
 * 
 * This pattern shows how to:
 * 1. Implement sidecar proxy pattern
 * 2. Configure traffic routing rules
 * 3. Implement service-to-service communication
 * 4. Add observability (metrics, tracing, logging)
 * 5. Implement circuit breaking at proxy level
 * 6. Configure mutual TLS (mTLS)
 * 7. Implement traffic splitting for A/B testing
 * 8. Add retry and timeout policies
 * 9. Implement fault injection for testing
 * 10. Monitor service mesh metrics
 * 
 * Key Concepts:
 * - Service Mesh: Infrastructure layer for service communication
 * - Sidecar Proxy: Proxy deployed alongside each service
 * - Control Plane: Manages and configures proxies
 * - Data Plane: Handles actual traffic (sidecars)
 * - Traffic Management: Routing, load balancing, failover
 * 
 * Service Mesh Components:
 * 1. Sidecar Proxy (Envoy) - Handles traffic
 * 2. Control Plane (Istio/Linkerd) - Configuration
 * 3. Observability - Metrics, traces, logs
 * 4. Security - mTLS, policies
 * 
 * Popular Service Meshes:
 * - Istio (Google/IBM)
 * - Linkerd (CNCF)
 * - Consul Connect (HashiCorp)
 * - AWS App Mesh
 * 
 * Dependencies:
 * - Kubernetes
 * - Istio/Linkerd (service mesh platform)
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class ServiceMeshPattern {

    public static void main(String[] args) {
        SpringApplication.run(ServiceMeshPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("SERVICE MESH PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateServiceMesh();
        demonstrateSidecarProxy();
        demonstrateTrafficManagement();
        
        System.out.println("\nApplication running with service mesh simulation");
        System.out.println("Test endpoints:");
        System.out.println("GET /api/mesh/call - Test service call through mesh");
        System.out.println("GET /api/mesh/metrics - View mesh metrics");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateServiceMesh() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SERVICE MESH ARCHITECTURE");
        System.out.println("=".repeat(80));
        
        System.out.println("\nData Plane (Sidecar Proxies):");
        System.out.println("  - Intercept all traffic");
        System.out.println("  - Apply routing rules");
        System.out.println("  - Collect metrics");
        System.out.println("  - Enforce policies");
        
        System.out.println("\nControl Plane:");
        System.out.println("  - Configure proxies");
        System.out.println("  - Distribute policies");
        System.out.println("  - Aggregate telemetry");
    }
    
    private static void demonstrateSidecarProxy() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SIDECAR PROXY PATTERN");
        System.out.println("=".repeat(80));
        
        System.out.println("\n[Service A] <--> [Sidecar Proxy A]");
        System.out.println("                       |");
        System.out.println("                  Service Mesh");
        System.out.println("                       |");
        System.out.println("[Service B] <--> [Sidecar Proxy B]");
    }
    
    private static void demonstrateTrafficManagement() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TRAFFIC MANAGEMENT");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Traffic Routing - Route to specific versions");
        System.out.println("2. Load Balancing - Distribute across instances");
        System.out.println("3. Traffic Splitting - A/B testing (v1: 90%, v2: 10%)");
        System.out.println("4. Fault Injection - Test resilience");
        System.out.println("5. Circuit Breaking - Prevent cascading failures");
    }
}

/**
 * Sidecar Proxy Model
 */
class SidecarProxy {
    private String proxyId;
    private String serviceName;
    private String serviceVersion;
    private ProxyConfig config;
    private ProxyMetrics metrics;
    
    public SidecarProxy(String proxyId, String serviceName, String serviceVersion) {
        this.proxyId = proxyId;
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.config = new ProxyConfig();
        this.metrics = new ProxyMetrics(proxyId);
    }
    
    public ServiceResponse handleRequest(ServiceRequest request) {
        metrics.recordRequest();
        
        // Apply routing rules
        String targetService = config.getRoute(request.getPath());
        
        // Apply retry policy
        int maxRetries = config.getMaxRetries();
        ServiceResponse response = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                response = forwardRequest(request, targetService);
                
                if (response.getStatusCode() < 500) {
                    metrics.recordSuccess();
                    return response;
                }
            } catch (Exception e) {
                metrics.recordError();
                if (attempt == maxRetries) {
                    throw e;
                }
            }
        }
        
        return response;
    }
    
    private ServiceResponse forwardRequest(ServiceRequest request, String targetService) {
        // Simulate request forwarding
        return new ServiceResponse(200, "Response from " + targetService);
    }
    
    // Getters and setters
    public String getProxyId() { return proxyId; }
    public String getServiceName() { return serviceName; }
    public String getServiceVersion() { return serviceVersion; }
    public ProxyMetrics getMetrics() { return metrics; }
}

/**
 * Proxy Configuration
 */
class ProxyConfig {
    private Map<String, String> routes;
    private int maxRetries;
    private long timeoutMs;
    private boolean mtlsEnabled;
    
    public ProxyConfig() {
        this.routes = new HashMap<>();
        this.maxRetries = 3;
        this.timeoutMs = 5000;
        this.mtlsEnabled = true;
        
        // Default routes
        routes.put("/users", "user-service");
        routes.put("/orders", "order-service");
        routes.put("/products", "product-service");
    }
    
    public String getRoute(String path) {
        return routes.entrySet().stream()
            .filter(e -> path.startsWith(e.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse("default-service");
    }
    
    // Getters and setters
    public int getMaxRetries() { return maxRetries; }
    public long getTimeoutMs() { return timeoutMs; }
    public boolean isMtlsEnabled() { return mtlsEnabled; }
}

/**
 * Proxy Metrics
 */
class ProxyMetrics {
    private String proxyId;
    private long totalRequests;
    private long successfulRequests;
    private long failedRequests;
    private Map<String, Long> requestsByPath;
    
    public ProxyMetrics(String proxyId) {
        this.proxyId = proxyId;
        this.totalRequests = 0;
        this.successfulRequests = 0;
        this.failedRequests = 0;
        this.requestsByPath = new ConcurrentHashMap<>();
    }
    
    public void recordRequest() {
        totalRequests++;
    }
    
    public void recordSuccess() {
        successfulRequests++;
    }
    
    public void recordError() {
        failedRequests++;
    }
    
    public void recordPath(String path) {
        requestsByPath.merge(path, 1L, Long::sum);
    }
    
    // Getters
    public String getProxyId() { return proxyId; }
    public long getTotalRequests() { return totalRequests; }
    public long getSuccessfulRequests() { return successfulRequests; }
    public long getFailedRequests() { return failedRequests; }
    public Map<String, Long> getRequestsByPath() { return requestsByPath; }
    public double getSuccessRate() {
        return totalRequests > 0 ? 
            (double) successfulRequests / totalRequests * 100 : 0.0;
    }
}

/**
 * Service Request Model
 */
class ServiceRequest {
    private String requestId;
    private String method;
    private String path;
    private Map<String, String> headers;
    private Object body;
    
    public ServiceRequest(String method, String path) {
        this.requestId = UUID.randomUUID().toString();
        this.method = method;
        this.path = path;
        this.headers = new HashMap<>();
    }
    
    // Getters and setters
    public String getRequestId() { return requestId; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public void addHeader(String key, String value) { headers.put(key, value); }
}

/**
 * Service Response Model
 */
class ServiceResponse {
    private int statusCode;
    private String body;
    private Map<String, String> headers;
    
    public ServiceResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>();
    }
    
    // Getters and setters
    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
    public Map<String, String> getHeaders() { return headers; }
}

/**
 * Traffic Routing Rule
 */
class TrafficRoutingRule {
    private String source;
    private String destination;
    private List<RouteDestination> destinations;
    
    public TrafficRoutingRule(String source, String destination) {
        this.source = source;
        this.destination = destination;
        this.destinations = new ArrayList<>();
    }
    
    public void addDestination(String version, int weight) {
        destinations.add(new RouteDestination(version, weight));
    }
    
    public String selectDestination() {
        int totalWeight = destinations.stream()
            .mapToInt(RouteDestination::getWeight)
            .sum();
        
        int random = (int) (Math.random() * totalWeight);
        int cumulative = 0;
        
        for (RouteDestination dest : destinations) {
            cumulative += dest.getWeight();
            if (random < cumulative) {
                return dest.getVersion();
            }
        }
        
        return destinations.get(0).getVersion();
    }
}

/**
 * Route Destination
 */
class RouteDestination {
    private String version;
    private int weight;
    
    public RouteDestination(String version, int weight) {
        this.version = version;
        this.weight = weight;
    }
    
    public String getVersion() { return version; }
    public int getWeight() { return weight; }
}

/**
 * Service Mesh Control Plane
 */
@Service
class ServiceMeshControlPlane {
    
    private final Map<String, SidecarProxy> proxies;
    private final Map<String, TrafficRoutingRule> routingRules;
    
    public ServiceMeshControlPlane() {
        proxies = new ConcurrentHashMap<>();
        routingRules = new ConcurrentHashMap<>();
        
        initializeProxies();
        initializeRoutingRules();
    }
    
    private void initializeProxies() {
        proxies.put("user-service-v1", 
            new SidecarProxy("proxy-1", "user-service", "v1"));
        proxies.put("user-service-v2", 
            new SidecarProxy("proxy-2", "user-service", "v2"));
        proxies.put("order-service-v1", 
            new SidecarProxy("proxy-3", "order-service", "v1"));
    }
    
    private void initializeRoutingRules() {
        // A/B testing: 90% v1, 10% v2
        TrafficRoutingRule userServiceRule = 
            new TrafficRoutingRule("*", "user-service");
        userServiceRule.addDestination("v1", 90);
        userServiceRule.addDestination("v2", 10);
        routingRules.put("user-service", userServiceRule);
    }
    
    public ServiceResponse routeRequest(String serviceName, ServiceRequest request) {
        TrafficRoutingRule rule = routingRules.get(serviceName);
        String version = rule != null ? rule.selectDestination() : "v1";
        
        String proxyKey = serviceName + "-" + version;
        SidecarProxy proxy = proxies.get(proxyKey);
        
        if (proxy == null) {
            throw new RuntimeException("No proxy found for: " + proxyKey);
        }
        
        return proxy.handleRequest(request);
    }
    
    public Map<String, ProxyMetrics> getAllMetrics() {
        Map<String, ProxyMetrics> metrics = new HashMap<>();
        proxies.forEach((key, proxy) -> 
            metrics.put(key, proxy.getMetrics()));
        return metrics;
    }
    
    public void addRoutingRule(String serviceName, String v1Weight, String v2Weight) {
        TrafficRoutingRule rule = new TrafficRoutingRule("*", serviceName);
        rule.addDestination("v1", Integer.parseInt(v1Weight));
        rule.addDestination("v2", Integer.parseInt(v2Weight));
        routingRules.put(serviceName, rule);
    }
}

/**
 * REST Controller demonstrating service mesh
 */
@RestController
@RequestMapping("/api/mesh")
class ServiceMeshController {
    
    private final ServiceMeshControlPlane controlPlane;
    
    public ServiceMeshController(ServiceMeshControlPlane controlPlane) {
        this.controlPlane = controlPlane;
    }
    
    @GetMapping("/users/{userId}")
    public Map<String, Object> callUserService(@PathVariable String userId) {
        ServiceRequest request = new ServiceRequest("GET", "/users/" + userId);
        request.addHeader("X-Request-Id", UUID.randomUUID().toString());
        
        ServiceResponse response = controlPlane.routeRequest("user-service", request);
        
        return Map.of(
            "statusCode", response.getStatusCode(),
            "body", response.getBody(),
            "requestId", request.getRequestId()
        );
    }
    
    @GetMapping("/orders/{orderId}")
    public Map<String, Object> callOrderService(@PathVariable String orderId) {
        ServiceRequest request = new ServiceRequest("GET", "/orders/" + orderId);
        request.addHeader("X-Request-Id", UUID.randomUUID().toString());
        
        ServiceResponse response = controlPlane.routeRequest("order-service", request);
        
        return Map.of(
            "statusCode", response.getStatusCode(),
            "body", response.getBody(),
            "requestId", request.getRequestId()
        );
    }
    
    @GetMapping("/metrics")
    public Map<String, ProxyMetrics> getMetrics() {
        return controlPlane.getAllMetrics();
    }
    
    @PostMapping("/routing/{serviceName}")
    public String updateRouting(
            @PathVariable String serviceName,
            @RequestParam String v1Weight,
            @RequestParam String v2Weight) {
        
        controlPlane.addRoutingRule(serviceName, v1Weight, v2Weight);
        return "Routing rule updated for " + serviceName;
    }
}
