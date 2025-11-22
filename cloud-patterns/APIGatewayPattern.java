package com.example.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API Gateway Pattern - Demonstrates API Gateway with Routing, Filtering, and Rate Limiting
 * 
 * This pattern shows how to:
 * 1. Configure gateway routes
 * 2. Implement request/response filtering
 * 3. Add authentication and authorization
 * 4. Implement rate limiting
 * 5. Add request/response logging
 * 6. Transform requests and responses
 * 7. Implement circuit breaker at gateway
 * 8. Add CORS support
 * 9. Implement API versioning
 * 10. Monitor gateway metrics
 * 
 * Key Concepts:
 * - Single Entry Point: All client requests go through gateway
 * - Routing: Direct requests to appropriate microservices
 * - Filtering: Modify requests/responses
 * - Rate Limiting: Control request rates
 * - Authentication: Centralized auth
 * 
 * Spring Cloud Gateway Features:
 * - Predicates: Match requests (path, header, method, etc.)
 * - Filters: Transform requests/responses
 * - Global Filters: Apply to all routes
 * - Route-specific Filters: Apply to specific routes
 * 
 * Dependencies:
 * - spring-cloud-starter-gateway
 * - spring-cloud-starter-circuitbreaker-reactor-resilience4j
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class APIGatewayPattern {

    public static void main(String[] args) {
        SpringApplication.run(APIGatewayPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("API GATEWAY PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("GATEWAY FEATURES");
        System.out.println("=".repeat(80));
        System.out.println("\n1. Request Routing");
        System.out.println("2. Load Balancing");
        System.out.println("3. Authentication & Authorization");
        System.out.println("4. Rate Limiting");
        System.out.println("5. Request/Response Transformation");
        System.out.println("6. Circuit Breaking");
        System.out.println("7. Logging & Monitoring");
        System.out.println("8. CORS Handling");
        System.out.println("9. API Versioning");
        System.out.println("10. Request Caching");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONFIGURED ROUTES");
        System.out.println("=".repeat(80));
        System.out.println("\n/api/users/** → user-service");
        System.out.println("/api/orders/** → order-service");
        System.out.println("/api/products/** → product-service");
        System.out.println("/api/payments/** → payment-service");
        
        System.out.println("\nGateway running on port 8080");
        System.out.println("Press Ctrl+C to stop.\n");
    }
}

/**
 * Gateway Route Configuration
 */
@Configuration
class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // User Service Route
            .route("user_service", r -> r
                .path("/api/users/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway", "API-Gateway")
                    .addResponseHeader("X-Response-Gateway", "API-Gateway"))
                .uri("lb://user-service"))
            
            // Order Service Route
            .route("order_service", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway", "API-Gateway"))
                .uri("lb://order-service"))
            
            // Product Service Route with Circuit Breaker
            .route("product_service", r -> r
                .path("/api/products/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .circuitBreaker(config -> config
                        .setName("productServiceCB")
                        .setFallbackUri("forward:/fallback/products")))
                .uri("lb://product-service"))
            
            // Payment Service Route with Rate Limiting
            .route("payment_service", r -> r
                .path("/api/payments/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .requestRateLimiter(config -> config
                        .setRateLimiter(new CustomRateLimiter())
                        .setKeyResolver(new IPKeyResolver())))
                .uri("lb://payment-service"))
            
            .build();
    }
}

/**
 * Global Logging Filter
 */
@Component
class LoggingGlobalFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        System.out.printf("[Gateway] %s %s %s%n",
            LocalDateTime.now(),
            request.getMethod(),
            request.getURI());
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            System.out.printf("[Gateway] Response Status: %s%n", 
                response.getStatusCode());
        }));
    }
    
    @Override
    public int getOrder() {
        return -1; // High priority
    }
}

/**
 * Authentication Global Filter
 */
@Component
class AuthenticationGlobalFilter implements GlobalFilter, Ordered {
    
    private final Set<String> publicPaths = Set.of("/api/auth/login", "/api/auth/register");
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Skip authentication for public paths
        if (publicPaths.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }
        
        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        
        // Validate token (simplified)
        String token = authHeader.substring(7);
        if (!isValidToken(token)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        
        // Add user info to request headers
        ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-User-Id", extractUserId(token))
            .build();
        
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
    
    private boolean isValidToken(String token) {
        // Simplified token validation
        return token != null && token.length() > 10;
    }
    
    private String extractUserId(String token) {
        // Simplified user ID extraction
        return "user-" + token.substring(0, 8);
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
}

/**
 * Rate Limiting Filter
 */
@Component
class RateLimitingGlobalFilter implements GlobalFilter, Ordered {
    
    private final Map<String, List<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private final int maxRequestsPerMinute = 60;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIP = getClientIP(exchange);
        
        if (isRateLimitExceeded(clientIP)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().add("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
            response.getHeaders().add("X-RateLimit-Remaining", "0");
            return response.setComplete();
        }
        
        recordRequest(clientIP);
        
        int remaining = getRemainingRequests(clientIP);
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", 
            String.valueOf(maxRequestsPerMinute));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", 
            String.valueOf(remaining));
        
        return chain.filter(exchange);
    }
    
    private String getClientIP(ServerWebExchange exchange) {
        return exchange.getRequest().getRemoteAddress() != null ?
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() :
            "unknown";
    }
    
    private boolean isRateLimitExceeded(String clientIP) {
        List<Long> timestamps = requestTimestamps.getOrDefault(clientIP, new ArrayList<>());
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000;
        
        // Remove old timestamps
        timestamps.removeIf(time -> time < oneMinuteAgo);
        
        return timestamps.size() >= maxRequestsPerMinute;
    }
    
    private void recordRequest(String clientIP) {
        requestTimestamps.computeIfAbsent(clientIP, k -> new ArrayList<>())
            .add(System.currentTimeMillis());
    }
    
    private int getRemainingRequests(String clientIP) {
        List<Long> timestamps = requestTimestamps.getOrDefault(clientIP, new ArrayList<>());
        return Math.max(0, maxRequestsPerMinute - timestamps.size());
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}

/**
 * Custom Rate Limiter (Simplified)
 */
class CustomRateLimiter {
    // Implementation for rate limiting
}

/**
 * IP-based Key Resolver for Rate Limiting
 */
class IPKeyResolver {
    // Implementation for key resolution
}

/**
 * Request Transformation Filter
 */
class RequestTransformationFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Transform request
        ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-Request-Time", LocalDateTime.now().toString())
            .header("X-Request-Id", UUID.randomUUID().toString())
            .build();
        
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}

/**
 * Response Transformation Filter
 */
class ResponseTransformationFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add("X-Response-Time", LocalDateTime.now().toString());
            response.getHeaders().add("X-Powered-By", "Spring Cloud Gateway");
        }));
    }
}

/**
 * CORS Configuration Filter
 */
@Component
class CORSGlobalFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        
        response.getHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS");
        response.getHeaders().add("Access-Control-Allow-Headers", 
            "Authorization, Content-Type");
        response.getHeaders().add("Access-Control-Max-Age", "3600");
        
        if ("OPTIONS".equals(exchange.getRequest().getMethod().toString())) {
            response.setStatusCode(HttpStatus.OK);
            return response.setComplete();
        }
        
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -2; // Highest priority
    }
}

/**
 * Gateway Metrics Collector
 */
@Component
class GatewayMetricsFilter implements GlobalFilter, Ordered {
    
    private final Map<String, Long> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> responseTimes = new ConcurrentHashMap<>();
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        long startTime = System.currentTimeMillis();
        
        requestCounts.merge(path, 1L, Long::sum);
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            responseTimes.put(path, duration);
        }));
    }
    
    public Map<String, Long> getRequestCounts() {
        return new HashMap<>(requestCounts);
    }
    
    public Map<String, Long> getResponseTimes() {
        return new HashMap<>(responseTimes);
    }
    
    @Override
    public int getOrder() {
        return 2;
    }
}
