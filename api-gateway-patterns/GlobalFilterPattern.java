import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Global Filter Pattern - Spring Cloud Gateway
 * ============================================
 * 
 * Global Filters apply to ALL routes and execute in an ordered chain.
 * Unlike Gateway Filters (route-specific), Global Filters handle cross-cutting concerns
 * like logging, security, monitoring, and request tracking.
 * 
 * Key Characteristics:
 * - Applied to ALL routes automatically
 * - Execute in ordered chain (Ordered interface)
 * - Can modify request/response
 * - Can short-circuit the chain
 * - Execute before and after proxying
 * 
 * Built-in Global Filters:
 * - ForwardRoutingFilter: Forward scheme routing
 * - LoadBalancerClientFilter: Load balancing
 * - NettyRoutingFilter: HTTP/HTTPS routing
 * - NettyWriteResponseFilter: Write response
 * - RouteToRequestUrlFilter: Convert route to request URL
 * - WebsocketRoutingFilter: WebSocket routing
 * - GatewayMetricsFilter: Metrics collection
 * - ForwardPathFilter: Forward path handling
 * 
 * Common Use Cases:
 * - Request/response logging
 * - Authentication and authorization
 * - Correlation ID injection
 * - Request timing and metrics
 * - Security headers
 * - Error handling
 * - API rate limiting
 * - Request validation
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 */
@Component
public class GlobalFilterPattern {

    /**
     * Example 1: Request Logging Global Filter
     * Logs all incoming requests with method, path, headers.
     * Order: -100 (executes early)
     */
    @Component
    public static class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {
        
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            
            System.out.println("=== Incoming Request ===");
            System.out.println("Method: " + request.getMethod());
            System.out.println("Path: " + request.getPath());
            System.out.println("Query: " + request.getQueryParams());
            System.out.println("Headers: " + request.getHeaders());
            
            return chain.filter(exchange);
        }
        
        @Override
        public int getOrder() {
            return -100;  // Execute early in chain
        }
    }

    /**
     * Example 2: Response Logging Global Filter
     * Logs all outgoing responses with status and headers.
     * Order: 100 (executes late)
     */
    @Component
    public static class ResponseLoggingGlobalFilter implements GlobalFilter, Ordered {
        
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                
                System.out.println("=== Outgoing Response ===");
                System.out.println("Status: " + response.getStatusCode());
                System.out.println("Headers: " + response.getHeaders());
            }));
        }
        
        @Override
        public int getOrder() {
            return 100;  // Execute late in chain
        }
    }

    /**
     * Example 3: Correlation ID Global Filter
     * Injects correlation ID for request tracking across services.
     * Order: -90
     */
    @Component
    public static class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {
        
        private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
        
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            
            // Get or generate correlation ID
            String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            
            // Add to request
            ServerHttpRequest modifiedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();
            
            // Add to response
            exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);
            
            // Store in exchange attributes for other filters
            exchange.getAttributes().put("correlationId", correlationId);
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }
        
        @Override
        public int getOrder() {
            return -90;
        }
    }

    /**
     * Example 4: Request Timing Global Filter
     * Measures request processing time and adds to response header.
     * Order: -80 (start timer early)
     */
    @Component
    public static class RequestTimingGlobalFilter implements GlobalFilter, Ordered {
        
        private static final String START_TIME_ATTR = "requestStartTime";
        private static final String ELAPSED_TIME_HEADER = "X-Response-Time";
        
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            // Record start time
            exchange.getAttributes().put(START_TIME_ATTR, Instant.now());
            
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                // Calculate elapsed time
                Instant startTime = exchange.getAttribute(START_TIME_ATTR);
                if (startTime != null) {
                    Duration elapsed = Duration.between(startTime, Instant.now());
                    long elapsedMs = elapsed.toMillis();
                    
                    // Add to response header
                    exchange.getResponse().getHeaders()
                        .add(ELAPSED_TIME_HEADER, elapsedMs + "ms");
                    
                    System.out.println("Request processed in " + elapsedMs + "ms");
                }
            }));
        }
        
        @Override
        public int getOrder() {
            return -80;
        }
    }

    /**
     * Example 5: Authentication Global Filter
     * Validates authentication token for all requests.
     * Order: -70
     */
    @Component
    public static class AuthenticationGlobalFilter implements GlobalFilter, Ordered {
        
        private static final String AUTH_HEADER = "Authorization";
        private static final List<String> PUBLIC_PATHS = Arrays.asList("/login", "/register", "/health");
        
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            
            // Skip authentication for public paths
            if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }
            
            // Check authorization header
            String authHeader = request.getHeaders().getFirst(AUTH_HEADER);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            // Validate token (simplified - use JWT library in production)
            String token = authHeader.substring(7);
            if (!validateToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            
            // Add user info to attributes
            exchange.getAttributes().put("userId", extractUserId(token));
            
            return chain.filter(exchange);
        }
        
        private boolean validateToken(String token) {
            // Simplified validation - use JWT library in production
            return token.length() > 10;
        }
        
        private String extractUserId(String token) {
            // Simplified extraction - decode JWT in production
            return "user-" + token.hashCode();
        }
        
        @Override
        public int getOrder() {
            return -70;
        }
    }

    /**
     * Example 6: Security Headers Global Filter
     * Adds security headers to all responses.
     * Order: 90
     */
    @Component
    public static class SecurityHeadersGlobalFilter implements GlobalFilter, Ordered {
        
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                HttpHeaders headers = exchange.getResponse().getHeaders();
                
                // Security headers
                headers.add("X-Content-Type-Options", "nosniff");
                headers.add("X-Frame-Options", "DENY");
                headers.add("X-XSS-Protection", "1; mode=block");
                headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                headers.add("Content-Security-Policy", "default-src 'self'");
                headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
                headers.add("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
            }));
        }
        
        @Override
        public int getOrder() {
            return 90;
        }
    }

    /**
     * Example 7: Error Handling Global Filter
     * Handles errors and provides consistent error responses.
     * Order: -60
     */
    @Component
    public static class ErrorHandlingGlobalFilter implements GlobalFilter, Ordered {
        
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            return chain.filter(exchange)
                .onErrorResume(throwable -> {
                    System.err.println("Error in gateway: " + throwable.getMessage());
                    
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    response.getHeaders().add("Content-Type", "application/json");
                    
                    String errorJson = String.format(
                        "{\"error\": \"%s\", \"message\": \"%s\"}",
                        "GATEWAY_ERROR",
                        throwable.getMessage()
                    );
                    
                    return response.writeWith(
                        Mono.just(response.bufferFactory().wrap(errorJson.getBytes()))
                    );
                });
        }
        
        @Override
        public int getOrder() {
            return -60;
        }
    }

    /**
     * Example 8: Rate Limiting Global Filter
     * Simple in-memory rate limiting for all routes.
     * Order: -50
     */
    @Component
    public static class RateLimitingGlobalFilter implements GlobalFilter, Ordered {
        
        private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
        private final int requestsPerMinute = 100;
        
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            String clientId = getClientId(exchange);
            
            RateLimitInfo info = rateLimitMap.computeIfAbsent(
                clientId,
                k -> new RateLimitInfo(requestsPerMinute)
            );
            
            if (!info.allowRequest()) {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders()
                    .add("X-RateLimit-Retry-After", "60");
                return exchange.getResponse().setComplete();
            }
            
            // Add rate limit headers
            exchange.getResponse().getHeaders()
                .add("X-RateLimit-Limit", String.valueOf(requestsPerMinute))
                .add("X-RateLimit-Remaining", String.valueOf(info.getRemainingRequests()));
            
            return chain.filter(exchange);
        }
        
        private String getClientId(ServerWebExchange exchange) {
            // Use IP address or API key
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null) {
                return apiKey;
            }
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        
        @Override
        public int getOrder() {
            return -50;
        }
        
        private static class RateLimitInfo {
            private final int limit;
            private int count = 0;
            private long windowStart = System.currentTimeMillis();
            
            public RateLimitInfo(int limit) {
                this.limit = limit;
            }
            
            public synchronized boolean allowRequest() {
                long now = System.currentTimeMillis();
                
                // Reset window if minute has passed
                if (now - windowStart > 60000) {
                    count = 0;
                    windowStart = now;
                }
                
                if (count < limit) {
                    count++;
                    return true;
                }
                
                return false;
            }
            
            public int getRemainingRequests() {
                return Math.max(0, limit - count);
            }
        }
    }

    /**
     * Example 9: Request Validation Global Filter
     * Validates request headers and parameters.
     * Order: -40
     */
    @Component
    public static class RequestValidationGlobalFilter implements GlobalFilter, Ordered {
        
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            
            // Validate Content-Type for POST/PUT
            if (request.getMethod() == HttpMethod.POST || request.getMethod() == HttpMethod.PUT) {
                String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                if (contentType == null) {
                    exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                    return exchange.getResponse().setComplete();
                }
            }
            
            // Validate required headers
            String apiVersion = request.getHeaders().getFirst("X-API-Version");
            if (apiVersion == null) {
                // Set default version
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-API-Version", "1.0")
                    .build();
                exchange = exchange.mutate().request(modifiedRequest).build();
            }
            
            return chain.filter(exchange);
        }
        
        @Override
        public int getOrder() {
            return -40;
        }
    }

    /**
     * Example 10: Metrics Collection Global Filter
     * Collects metrics for all requests (status codes, response times, etc.)
     * Order: -30
     */
    @Component
    public static class MetricsCollectionGlobalFilter implements GlobalFilter, Ordered {
        
        private final Map<String, MetricsData> metricsMap = new ConcurrentHashMap<>();
        
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            String path = exchange.getRequest().getPath().value();
            Instant startTime = Instant.now();
            
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                Duration elapsed = Duration.between(startTime, Instant.now());
                HttpStatus status = exchange.getResponse().getStatusCode();
                
                MetricsData metrics = metricsMap.computeIfAbsent(
                    path,
                    k -> new MetricsData()
                );
                
                metrics.recordRequest(status, elapsed.toMillis());
                
                // Log metrics periodically
                if (metrics.getRequestCount() % 100 == 0) {
                    System.out.println("Metrics for " + path + ": " + metrics);
                }
            }));
        }
        
        @Override
        public int getOrder() {
            return -30;
        }
        
        private static class MetricsData {
            private long requestCount = 0;
            private long totalResponseTime = 0;
            private long successCount = 0;
            private long errorCount = 0;
            
            public synchronized void recordRequest(HttpStatus status, long responseTime) {
                requestCount++;
                totalResponseTime += responseTime;
                
                if (status.is2xxSuccessful()) {
                    successCount++;
                } else if (status.is4xxClientError() || status.is5xxServerError()) {
                    errorCount++;
                }
            }
            
            public long getRequestCount() {
                return requestCount;
            }
            
            public double getAverageResponseTime() {
                return requestCount > 0 ? (double) totalResponseTime / requestCount : 0;
            }
            
            @Override
            public String toString() {
                return String.format(
                    "Requests: %d, Avg Response: %.2fms, Success: %d, Errors: %d",
                    requestCount, getAverageResponseTime(), successCount, errorCount
                );
            }
        }
    }

    /**
     * Best Practices:
     * ===============
     * 
     * 1. Order Management: Use negative order for pre-processing, positive for post-processing
     * 2. Immutability: Use exchange.mutate() for modifications
     * 3. Error Handling: Use onErrorResume() for graceful error handling
     * 4. Performance: Avoid blocking operations in reactive chain
     * 5. State Management: Use exchange attributes for passing data between filters
     * 6. Logging: Log at appropriate levels (DEBUG for verbose, ERROR for exceptions)
     * 7. Security: Validate and sanitize all input
     * 8. Metrics: Collect metrics for monitoring and alerting
     * 9. Testing: Unit test filters in isolation
     * 10. Documentation: Document filter purpose and order
     * 
     * Filter Execution Order:
     * ======================
     * 
     * Pre-filters (before routing):
     * -100: Request Logging
     * -90: Correlation ID
     * -80: Request Timing (start)
     * -70: Authentication
     * -60: Error Handling
     * -50: Rate Limiting
     * -40: Request Validation
     * -30: Metrics Collection (start)
     * 
     * Routing filters (built-in):
     * 0: Default order
     * 
     * Post-filters (after routing):
     * 90: Security Headers
     * 100: Response Logging
     * 
     * Common Pitfalls:
     * ================
     * 
     * 1. Blocking operations in reactive chain
     * 2. Mutable shared state across requests
     * 3. Not calling chain.filter() - breaks chain
     * 4. Memory leaks from storing request data
     * 5. Incorrect filter order
     * 6. Not handling errors properly
     * 7. Excessive logging in production
     * 8. Using exchange attributes incorrectly
     * 9. Not cleaning up resources
     * 10. Assuming filter execution in single thread
     * 
     * When to Use:
     * ============
     * 
     * - Cross-cutting concerns for ALL routes
     * - Request/response logging
     * - Authentication/authorization
     * - Correlation ID injection
     * - Request timing and metrics
     * - Security headers
     * - Error handling
     * - Rate limiting (global)
     * 
     * When NOT to Use:
     * ================
     * 
     * - Route-specific logic (use Gateway Filters)
     * - Heavy computation (use async processing)
     * - Business logic (belongs in services)
     * - Database operations (use services)
     * - Complex transformations (use dedicated service)
     */
}
