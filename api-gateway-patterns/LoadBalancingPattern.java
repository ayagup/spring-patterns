import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Load Balancing Pattern - Spring Cloud Gateway
 * ==============================================
 * 
 * Load balancing distributes requests across multiple service instances
 * for high availability, fault tolerance, and optimal resource utilization.
 * 
 * Spring Cloud LoadBalancer (replaces Netflix Ribbon):
 * - Client-side load balancing
 * - Service discovery integration (Eureka, Consul, etc.)
 * - Multiple load balancing algorithms
 * - Health-aware routing
 * - Customizable selection strategies
 * 
 * Load Balancing Algorithms:
 * - Round Robin: Distributes requests evenly in sequence
 * - Random: Selects instance randomly
 * - Weighted: Assigns weight to each instance
 * - Least Connections: Routes to instance with fewest active connections
 * - Sticky Session: Routes same user to same instance
 * - Zone Aware: Prefers instances in same availability zone
 * - Health Aware: Excludes unhealthy instances
 * 
 * Use Cases:
 * - Distribute load across multiple instances
 * - High availability and fault tolerance
 * - Horizontal scaling
 * - Blue-green deployments
 * - Canary deployments
 * - Geographic routing
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-loadbalancer</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
 * </dependency>
 */
@Configuration
public class LoadBalancingPattern {

    /**
     * Example 1: Basic Load Balancing with Service Discovery
     * Uses lb:// scheme to route to service instances from service registry.
     */
    @Bean
    public RouteLocator basicLoadBalancerRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Load balance across user-service instances
            .route("lb_user_service", r -> r
                .path("/users/**")
                .uri("lb://user-service"))  // lb:// scheme for load balancing
            
            // Load balance across order-service instances
            .route("lb_order_service", r -> r
                .path("/orders/**")
                .uri("lb://order-service"))
            
            // Load balance across product-service instances
            .route("lb_product_service", r -> r
                .path("/products/**")
                .uri("lb://product-service"))
            
            .build();
    }

    /**
     * Example 2: Round Robin Load Balancer (Default)
     * Distributes requests evenly across instances in sequence.
     */
    @LoadBalancerClient(name = "user-service", configuration = RoundRobinLoadBalancerConfig.class)
    public static class RoundRobinLoadBalancerConfig {
        
        @Bean
        public ReactorLoadBalancer<ServiceInstance> roundRobinLoadBalancer(
                LoadBalancerClientFactory clientFactory,
                @Value("${spring.application.name}") String serviceId) {
            
            return new RoundRobinLoadBalancer(
                clientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class),
                serviceId
            );
        }
    }

    /**
     * Example 3: Random Load Balancer
     * Selects instance randomly for each request.
     */
    @LoadBalancerClient(name = "product-service", configuration = RandomLoadBalancerConfig.class)
    public static class RandomLoadBalancerConfig {
        
        @Bean
        public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
                LoadBalancerClientFactory clientFactory,
                @Value("${spring.application.name}") String serviceId) {
            
            return new RandomLoadBalancer(
                clientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class),
                serviceId
            );
        }
    }

    /**
     * Example 4: Weighted Load Balancer
     * Assigns weight to each instance based on capacity/performance.
     */
    @Component
    public static class WeightedLoadBalancer implements ReactorServiceInstanceLoadBalancer {
        
        private final String serviceId;
        private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
        
        public WeightedLoadBalancer(
                String serviceId,
                ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider) {
            this.serviceId = serviceId;
            this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        }
        
        @Override
        public Mono<Response<ServiceInstance>> choose(Request request) {
            ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
            
            return supplier.get(request).next().map(instances -> {
                if (instances.isEmpty()) {
                    return new EmptyResponse();
                }
                
                // Weighted selection based on metadata
                ServiceInstance selected = selectByWeight(instances);
                return new DefaultResponse(selected);
            });
        }
        
        private ServiceInstance selectByWeight(List<ServiceInstance> instances) {
            int totalWeight = instances.stream()
                .mapToInt(instance -> getWeight(instance))
                .sum();
            
            int randomWeight = ThreadLocalRandom.current().nextInt(totalWeight);
            int currentWeight = 0;
            
            for (ServiceInstance instance : instances) {
                currentWeight += getWeight(instance);
                if (randomWeight < currentWeight) {
                    return instance;
                }
            }
            
            return instances.get(0);  // Fallback
        }
        
        private int getWeight(ServiceInstance instance) {
            // Get weight from instance metadata
            String weightStr = instance.getMetadata().get("weight");
            return weightStr != null ? Integer.parseInt(weightStr) : 1;
        }
    }

    /**
     * Example 5: Sticky Session Load Balancer
     * Routes same user to same instance (session affinity).
     */
    @Component
    public static class StickySessionLoadBalancer implements ReactorServiceInstanceLoadBalancer {
        
        private final Map<String, String> sessionToInstance = new ConcurrentHashMap<>();
        private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
        
        public StickySessionLoadBalancer(
                ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider) {
            this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        }
        
        @Override
        public Mono<Response<ServiceInstance>> choose(Request request) {
            return serviceInstanceListSupplierProvider.getIfAvailable().get(request).next()
                .map(instances -> {
                    if (instances.isEmpty()) {
                        return new EmptyResponse();
                    }
                    
                    // Get session ID from request
                    String sessionId = getSessionId(request);
                    
                    // Check if session already mapped to instance
                    String instanceId = sessionToInstance.get(sessionId);
                    if (instanceId != null) {
                        ServiceInstance instance = findInstanceById(instances, instanceId);
                        if (instance != null) {
                            return new DefaultResponse(instance);
                        }
                    }
                    
                    // New session - select instance and remember mapping
                    ServiceInstance selected = instances.get(0);  // Or use round-robin
                    sessionToInstance.put(sessionId, selected.getInstanceId());
                    
                    return new DefaultResponse(selected);
                });
        }
        
        private String getSessionId(Request request) {
            // Extract session ID from cookie or header
            return "session-" + request.getContext().hashCode();
        }
        
        private ServiceInstance findInstanceById(List<ServiceInstance> instances, String instanceId) {
            return instances.stream()
                .filter(i -> i.getInstanceId().equals(instanceId))
                .findFirst()
                .orElse(null);
        }
    }

    /**
     * Example 6: Zone-Aware Load Balancer
     * Prefers instances in same availability zone for reduced latency.
     */
    @Component
    public static class ZoneAwareLoadBalancer implements ReactorServiceInstanceLoadBalancer {
        
        private final String currentZone;
        private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
        
        public ZoneAwareLoadBalancer(
                @Value("${spring.cloud.gateway.zone:us-east-1a}") String currentZone,
                ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider) {
            this.currentZone = currentZone;
            this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        }
        
        @Override
        public Mono<Response<ServiceInstance>> choose(Request request) {
            return serviceInstanceListSupplierProvider.getIfAvailable().get(request).next()
                .map(instances -> {
                    if (instances.isEmpty()) {
                        return new EmptyResponse();
                    }
                    
                    // Prefer instances in same zone
                    List<ServiceInstance> sameZoneInstances = instances.stream()
                        .filter(i -> currentZone.equals(i.getMetadata().get("zone")))
                        .collect(Collectors.toList());
                    
                    // Use same-zone instances if available, otherwise use any instance
                    List<ServiceInstance> candidates = sameZoneInstances.isEmpty() 
                        ? instances 
                        : sameZoneInstances;
                    
                    // Round-robin within zone
                    int index = ThreadLocalRandom.current().nextInt(candidates.size());
                    return new DefaultResponse(candidates.get(index));
                });
        }
    }

    /**
     * Example 7: Health-Aware Load Balancer
     * Excludes unhealthy instances from selection.
     */
    @Component
    public static class HealthAwareLoadBalancer implements ReactorServiceInstanceLoadBalancer {
        
        private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
        private final Map<String, HealthStatus> healthCache = new ConcurrentHashMap<>();
        
        public HealthAwareLoadBalancer(
                ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider) {
            this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        }
        
        @Override
        public Mono<Response<ServiceInstance>> choose(Request request) {
            return serviceInstanceListSupplierProvider.getIfAvailable().get(request).next()
                .map(instances -> {
                    // Filter healthy instances
                    List<ServiceInstance> healthyInstances = instances.stream()
                        .filter(this::isHealthy)
                        .collect(Collectors.toList());
                    
                    if (healthyInstances.isEmpty()) {
                        // Fallback to all instances if none are healthy
                        healthyInstances = instances;
                    }
                    
                    // Round-robin among healthy instances
                    int index = ThreadLocalRandom.current().nextInt(healthyInstances.size());
                    return new DefaultResponse(healthyInstances.get(index));
                });
        }
        
        private boolean isHealthy(ServiceInstance instance) {
            String instanceId = instance.getInstanceId();
            HealthStatus status = healthCache.get(instanceId);
            
            if (status == null || status.isExpired()) {
                // Check health (simplified - use actual health check in production)
                status = checkHealth(instance);
                healthCache.put(instanceId, status);
            }
            
            return status.isHealthy();
        }
        
        private HealthStatus checkHealth(ServiceInstance instance) {
            // Simplified health check - implement actual health check
            return new HealthStatus(true, Instant.now());
        }
        
        private static class HealthStatus {
            private final boolean healthy;
            private final Instant timestamp;
            
            public HealthStatus(boolean healthy, Instant timestamp) {
                this.healthy = healthy;
                this.timestamp = timestamp;
            }
            
            public boolean isHealthy() {
                return healthy;
            }
            
            public boolean isExpired() {
                return Duration.between(timestamp, Instant.now()).getSeconds() > 30;
            }
        }
    }

    /**
     * Example 8: Least Connections Load Balancer
     * Routes to instance with fewest active connections.
     */
    @Component
    public static class LeastConnectionsLoadBalancer implements ReactorServiceInstanceLoadBalancer {
        
        private final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
        private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
        
        public LeastConnectionsLoadBalancer(
                ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider) {
            this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        }
        
        @Override
        public Mono<Response<ServiceInstance>> choose(Request request) {
            return serviceInstanceListSupplierProvider.getIfAvailable().get(request).next()
                .map(instances -> {
                    if (instances.isEmpty()) {
                        return new EmptyResponse();
                    }
                    
                    // Find instance with least connections
                    ServiceInstance selected = instances.stream()
                        .min(Comparator.comparingInt(i -> 
                            connectionCounts.computeIfAbsent(
                                i.getInstanceId(), 
                                k -> new AtomicInteger(0)
                            ).get()
                        ))
                        .orElse(instances.get(0));
                    
                    // Increment connection count
                    connectionCounts.get(selected.getInstanceId()).incrementAndGet();
                    
                    return new DefaultResponse(selected);
                });
        }
        
        public void releaseConnection(String instanceId) {
            AtomicInteger count = connectionCounts.get(instanceId);
            if (count != null) {
                count.decrementAndGet();
            }
        }
    }

    /**
     * Example 9: Load Balancer with Health Check Filter
     * Automatic health checks for instances.
     */
    @Bean
    public RouteLocator healthCheckLoadBalancerRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("health_check_lb", r -> r
                .path("/api/**")
                .filters(f -> f
                    .filter((exchange, chain) -> {
                        // Add health check logic
                        return chain.filter(exchange)
                            .doOnError(throwable -> {
                                // Mark instance as unhealthy on error
                                System.err.println("Instance health check failed: " + throwable.getMessage());
                            });
                    })
                )
                .uri("lb://api-service"))
            
            .build();
    }

    /**
     * Example 10: WebClient with Load Balancing
     * Use WebClient with load balancing for outbound requests.
     */
    @Bean
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
            .filter(new ReactorLoadBalancerExchangeFilterFunction(loadBalancerClient()));
    }
    
    @Bean
    public WebClient loadBalancedWebClient(WebClient.Builder builder) {
        return builder.build();
    }
    
    // Usage example:
    public Mono<String> callUserService(WebClient webClient) {
        return webClient.get()
            .uri("lb://user-service/users/123")  // Load balanced request
            .retrieve()
            .bodyToMono(String.class);
    }

    /**
     * YAML Configuration Example
     * ==========================
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         # Load balanced route with service discovery
     *         - id: lb_route
     *           uri: lb://user-service
     *           predicates:
     *             - Path=/users/**
     * 
     *         # Load balanced with filters
     *         - id: lb_filtered_route
     *           uri: lb://product-service
     *           predicates:
     *             - Path=/products/**
     *           filters:
     *             - StripPrefix=1
     *             - AddRequestHeader=X-Gateway, true
     * 
     *     # Load balancer configuration
     *     loadbalancer:
     *       ribbon:
     *         enabled: false  # Disable Ribbon (deprecated)
     *       cache:
     *         enabled: true
     *         ttl: 35s
     *       health-check:
     *         initial-delay: 0
     *         interval: 30s
     * 
     *   # Eureka client configuration
     *   application:
     *     name: api-gateway
     * 
     * eureka:
     *   client:
     *     service-url:
     *       defaultZone: http://localhost:8761/eureka/
     *     registry-fetch-interval-seconds: 5
     *   instance:
     *     prefer-ip-address: true
     *     lease-renewal-interval-in-seconds: 10
     *     lease-expiration-duration-in-seconds: 30
     *     metadata-map:
     *       zone: us-east-1a
     *       weight: 100
     */

    /**
     * Best Practices:
     * ===============
     * 
     * 1. Use Service Discovery: Automatic instance registration/deregistration
     * 2. Health Checks: Exclude unhealthy instances from load balancing
     * 3. Zone Awareness: Prefer instances in same zone for lower latency
     * 4. Graceful Shutdown: Deregister before shutdown to avoid failed requests
     * 5. Connection Pooling: Reuse connections for better performance
     * 6. Monitoring: Track request distribution and instance health
     * 7. Weighted Balancing: Assign weights based on instance capacity
     * 8. Sticky Sessions: Use when needed (stateful sessions)
     * 9. Failover: Retry on different instance if one fails
     * 10. Load Balancer Warmup: Gradually increase traffic to new instances
     * 
     * Common Pitfalls:
     * ================
     * 
     * 1. No health checks: Routing to dead instances
     * 2. Sticky sessions everywhere: Defeats purpose of load balancing
     * 3. Same weight for all: Ignores instance capacity differences
     * 4. No failover: Single instance failure affects all requests
     * 5. Not using service discovery: Manual instance management
     * 6. No connection limits: Instance overload
     * 7. Missing circuit breaker: Cascading failures
     * 8. Ignoring zones: Cross-zone traffic increases latency/cost
     * 9. No monitoring: Missing imbalanced load distribution
     * 10. Hardcoded URIs: Defeats purpose of load balancing
     * 
     * When to Use:
     * ============
     * 
     * - Multiple instances of same service
     * - High availability requirements
     * - Horizontal scaling
     * - Traffic distribution
     * - Blue-green/canary deployments
     * - Geographic distribution
     * 
     * When NOT to Use:
     * ================
     * 
     * - Single instance (no balancing needed)
     * - Direct service-to-service calls (use service mesh instead)
     * - Stateful sessions without sticky sessions
     * - Already have external load balancer (L4/L7)
     */
}
