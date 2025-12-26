import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Gateway Filter Pattern - Spring Cloud Gateway
 * =============================================
 * 
 * Gateway Filters modify incoming requests and outgoing responses for specific routes.
 * Filters are applied in a chain and can be executed before or after proxying the request.
 * 
 * Built-in Gateway Filters:
 * - AddRequestHeader/AddResponseHeader: Add headers
 * - RemoveRequestHeader/RemoveResponseHeader: Remove headers
 * - SetRequestHeader/SetResponseHeader: Set/replace headers
 * - AddRequestParameter: Add query parameter
 * - RewritePath: Rewrite request path
 * - StripPrefix: Remove path segments
 * - SetPath: Set new path
 * - SetStatus: Set response status
 * - RedirectTo: Redirect response
 * - RequestRateLimiter: Rate limiting
 * - Retry: Automatic retry
 * - CircuitBreaker: Circuit breaker pattern
 * - FallbackHeaders: Add exception headers
 * - RequestSize: Limit request size
 * - ModifyRequestBody/ModifyResponseBody: Transform body
 * - PrefixPath: Add path prefix
 * - PreserveHostHeader: Forward original Host header
 * - SetRequestHostHeader: Set Host header
 * - SaveSession: Force WebSession save
 * - SecureHeaders: Add security headers
 * - DedupeResponseHeader: Remove duplicate headers
 * - MapRequestHeader: Map header values
 * - TokenRelay: Relay OAuth2 token
 * 
 * Use Cases:
 * - Request/response transformation
 * - Header manipulation
 * - Authentication/authorization
 * - Logging and monitoring
 * - Request validation
 * - Response modification
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 */
@Configuration
public class GatewayFilterPattern {

    /**
     * Example 1: AddRequestHeader and AddResponseHeader Filters
     * Add headers to request/response.
     */
    @Bean
    public RouteLocator addHeaderRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Add request header
            .route("add_request_header", r -> r
                .path("/api/**")
                .filters(f -> f
                    .addRequestHeader("X-Request-Source", "gateway")
                    .addRequestHeader("X-Request-Time", "#{T(System).currentTimeMillis()}")
                )
                .uri("http://localhost:8081"))
            
            // Add response header
            .route("add_response_header", r -> r
                .path("/api/**")
                .filters(f -> f
                    .addResponseHeader("X-Response-Time", "#{T(System).currentTimeMillis()}")
                    .addResponseHeader("X-Powered-By", "Spring Cloud Gateway")
                )
                .uri("http://localhost:8081"))
            
            // Add multiple headers
            .route("add_multiple_headers", r -> r
                .path("/api/**")
                .filters(f -> f
                    .addRequestHeader("X-Correlation-ID", "#{T(java.util.UUID).randomUUID().toString()}")
                    .addRequestHeader("X-Gateway-Version", "1.0")
                    .addResponseHeader("X-Content-Type-Options", "nosniff")
                    .addResponseHeader("X-Frame-Options", "DENY")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 2: RemoveRequestHeader and RemoveResponseHeader Filters
     * Remove headers from request/response.
     */
    @Bean
    public RouteLocator removeHeaderRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Remove sensitive request headers
            .route("remove_request_headers", r -> r
                .path("/api/**")
                .filters(f -> f
                    .removeRequestHeader("Cookie")
                    .removeRequestHeader("Authorization")
                )
                .uri("http://localhost:8081"))
            
            // Remove server information from response
            .route("remove_response_headers", r -> r
                .path("/api/**")
                .filters(f -> f
                    .removeResponseHeader("Server")
                    .removeResponseHeader("X-Application-Context")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 3: SetRequestHeader and SetStatus Filters
     * Set/replace headers and HTTP status.
     */
    @Bean
    public RouteLocator setHeaderStatusRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Set request header (replaces existing)
            .route("set_request_header", r -> r
                .path("/api/**")
                .filters(f -> f
                    .setRequestHeader("Host", "backend-service")
                )
                .uri("http://localhost:8081"))
            
            // Set response status
            .route("set_status", r -> r
                .path("/maintenance")
                .filters(f -> f
                    .setStatus(HttpStatus.SERVICE_UNAVAILABLE)
                )
                .uri("no://op"))  // No operation - status set directly
            
            .build();
    }

    /**
     * Example 4: AddRequestParameter Filter
     * Add query parameter to request.
     */
    @Bean
    public RouteLocator addRequestParameterRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Add query parameter
            .route("add_request_parameter", r -> r
                .path("/api/**")
                .filters(f -> f
                    .addRequestParameter("source", "gateway")
                    .addRequestParameter("timestamp", "#{T(System).currentTimeMillis()}")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 5: RewritePath and StripPrefix Filters
     * Modify request path.
     */
    @Bean
    public RouteLocator pathModificationRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Rewrite path with regex
            .route("rewrite_path", r -> r
                .path("/api/v1/**")
                .filters(f -> f
                    .rewritePath("/api/v1/(?<segment>.*)", "/v1/${segment}")
                )
                .uri("http://localhost:8081"))
            
            // Strip prefix segments
            .route("strip_prefix", r -> r
                .path("/gateway/api/**")
                .filters(f -> f
                    .stripPrefix(1)  // Remove first segment (/gateway)
                )
                .uri("http://localhost:8081"))
            
            // Strip multiple prefix segments
            .route("strip_multiple_prefix", r -> r
                .path("/external/gateway/api/**")
                .filters(f -> f
                    .stripPrefix(2)  // Remove /external/gateway
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 6: SetPath and PrefixPath Filters
     * Set or add path prefix.
     */
    @Bean
    public RouteLocator pathSetPrefixRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Set path (replaces entire path)
            .route("set_path", r -> r
                .path("/api/users/{id}")
                .filters(f -> f
                    .setPath("/v2/users/{id}")
                )
                .uri("http://localhost:8081"))
            
            // Add path prefix
            .route("prefix_path", r -> r
                .path("/api/**")
                .filters(f -> f
                    .prefixPath("/backend")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 7: RedirectTo Filter
     * Redirect to another URL.
     */
    @Bean
    public RouteLocator redirectRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Redirect with 301 (permanent)
            .route("redirect_permanent", r -> r
                .path("/old-api/**")
                .filters(f -> f
                    .redirect(301, "http://localhost:8081/new-api")
                )
                .uri("no://op"))
            
            // Redirect with 302 (temporary)
            .route("redirect_temporary", r -> r
                .path("/maintenance")
                .filters(f -> f
                    .redirect(302, "http://localhost:8081/status")
                )
                .uri("no://op"))
            
            .build();
    }

    /**
     * Example 8: RequestSize Filter
     * Limit request body size.
     */
    @Bean
    public RouteLocator requestSizeRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Limit request size to 5MB
            .route("request_size_limit", r -> r
                .path("/api/upload/**")
                .filters(f -> f
                    .requestSize(5000000L)  // 5MB in bytes
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 9: SecureHeaders Filter
     * Add security headers (X-Xss-Protection, Strict-Transport-Security, etc.)
     */
    @Bean
    public RouteLocator secureHeadersRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Add security headers
            .route("secure_headers", r -> r
                .path("/api/**")
                .filters(f -> f
                    .secureHeaders()  // Adds security headers by default
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 10: DedupeResponseHeader Filter
     * Remove duplicate response headers.
     */
    @Bean
    public RouteLocator dedupeHeaderRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Deduplicate response headers
            .route("dedupe_headers", r -> r
                .path("/api/**")
                .filters(f -> f
                    .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                    .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_LAST")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Custom Gateway Filter
     * ====================
     * 
     * Create custom filter for request/response modification.
     */
    @Component
    public static class LoggingGatewayFilterFactory 
            extends AbstractGatewayFilterFactory<LoggingGatewayFilterFactory.Config> {
        
        public LoggingGatewayFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                ServerHttpRequest request = exchange.getRequest();
                
                if (config.isPreLogger()) {
                    System.out.println("Pre-filter: " + request.getMethod() + " " + request.getURI());
                }
                
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    if (config.isPostLogger()) {
                        ServerHttpResponse response = exchange.getResponse();
                        System.out.println("Post-filter: Status " + response.getStatusCode());
                    }
                }));
            };
        }
        
        public static class Config {
            private boolean preLogger = true;
            private boolean postLogger = true;
            
            public boolean isPreLogger() {
                return preLogger;
            }
            
            public void setPreLogger(boolean preLogger) {
                this.preLogger = preLogger;
            }
            
            public boolean isPostLogger() {
                return postLogger;
            }
            
            public void setPostLogger(boolean postLogger) {
                this.postLogger = postLogger;
            }
        }
    }

    /**
     * Custom Ordered Gateway Filter
     * =============================
     * 
     * Control filter execution order.
     */
    @Component
    public static class AuthenticationGatewayFilterFactory 
            extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> 
            implements Ordered {
        
        public AuthenticationGatewayFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                ServerHttpRequest request = exchange.getRequest();
                String authToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                
                if (authToken == null || !authToken.startsWith("Bearer ")) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
                
                // Validate token (simplified)
                String token = authToken.substring(7);
                if (!config.getValidTokens().contains(token)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                
                // Add user info to request
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", "user123")
                    .build();
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            };
        }
        
        @Override
        public int getOrder() {
            return -100;  // Execute before other filters
        }
        
        public static class Config {
            private List<String> validTokens = Arrays.asList("valid-token-1", "valid-token-2");
            
            public List<String> getValidTokens() {
                return validTokens;
            }
            
            public void setValidTokens(List<String> validTokens) {
                this.validTokens = validTokens;
            }
        }
    }

    /**
     * YAML Configuration Example
     * ==========================
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         - id: add_headers_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             - AddRequestHeader=X-Request-Source, gateway
     *             - AddResponseHeader=X-Response-Time, #{T(System).currentTimeMillis()}
     * 
     *         - id: remove_headers_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             - RemoveRequestHeader=Cookie
     *             - RemoveResponseHeader=Server
     * 
     *         - id: rewrite_path_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/v1/**
     *           filters:
     *             - RewritePath=/api/v1/(?<segment>.*), /v1/${segment}
     * 
     *         - id: strip_prefix_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/gateway/api/**
     *           filters:
     *             - StripPrefix=1
     * 
     *         - id: set_status_route
     *           uri: no://op
     *           predicates:
     *             - Path=/maintenance
     *           filters:
     *             - SetStatus=503
     * 
     *         - id: redirect_route
     *           uri: no://op
     *           predicates:
     *             - Path=/old-api/**
     *           filters:
     *             - RedirectTo=301, http://localhost:8081/new-api
     * 
     *         - id: request_size_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/upload/**
     *           filters:
     *             - RequestSize=5000000
     * 
     *         - id: secure_headers_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             - SecureHeaders
     * 
     *         - id: dedupe_headers_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             - DedupeResponseHeader=Access-Control-Allow-Origin, RETAIN_FIRST
     * 
     *         - id: custom_logging_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             - name: Logging
     *               args:
     *                 preLogger: true
     *                 postLogger: true
     */

    /**
     * Best Practices:
     * ===============
     * 
     * 1. Filter Order: Use Ordered interface for predictable execution
     * 2. Immutability: Use request.mutate() for request modification
     * 3. Error Handling: Handle exceptions in filters gracefully
     * 4. Performance: Avoid blocking operations in filters
     * 5. Security: Validate and sanitize all input
     * 6. Logging: Log filter execution for debugging
     * 7. Testing: Unit test custom filters thoroughly
     * 8. Documentation: Document filter behavior and configuration
     * 9. Monitoring: Add metrics for filter performance
     * 10. Idempotency: Ensure filters are idempotent
     * 
     * Common Pitfalls:
     * ================
     * 
     * 1. Blocking calls in reactive chain
     * 2. Modifying request without mutation
     * 3. Forgetting to call chain.filter()
     * 4. Not handling errors properly
     * 5. Creating memory leaks in filters
     * 6. Using mutable shared state
     * 7. Not considering filter order
     * 8. Excessive logging in filters
     * 9. Not cleaning up resources
     * 10. Assuming filter execution order
     * 
     * When to Use:
     * ============
     * 
     * - Request/response transformation
     * - Header manipulation
     * - Authentication/authorization
     * - Logging and monitoring
     * - Path rewriting
     * - Request validation
     * - Response modification
     * 
     * When NOT to Use:
     * ================
     * 
     * - Complex business logic (use backend services)
     * - Heavy computation (use async processing)
     * - Database operations (use backend services)
     * - Long-running tasks (use async processing)
     */
}
