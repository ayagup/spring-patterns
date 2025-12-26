import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Path Rewriting Pattern - Spring Cloud Gateway
 * ============================================
 * 
 * Path rewriting transforms request URLs before routing to backend services.
 * Enables API versioning, path normalization, and legacy URL support.
 * 
 * Path Rewriting Filters:
 * - RewritePath: Regex-based path transformation
 * - StripPrefix: Remove path segments
 * - SetPath: Replace entire path
 * - PrefixPath: Add path prefix
 * 
 * Use Cases:
 * - API versioning (external /v2/users -> internal /users)
 * - Path normalization
 * - Legacy URL support
 * - Microservice path mapping
 * - URL beautification
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 */
@Configuration
public class PathRewritingPattern {

    /**
     * Example 1: RewritePath - Regex Replacement
     * Transform path using regular expressions.
     */
    @Bean
    public RouteLocator rewritePathRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // API versioning: /api/v1/users/123 -> /users/123
            .route("api_version_rewrite", r -> r
                .path("/api/v1/**")
                .filters(f -> f
                    .rewritePath("/api/v1/(?<segment>.*)", "/${segment}")
                )
                .uri("http://localhost:8081"))
            
            // Service prefix: /gateway/api/users -> /api/users
            .route("gateway_prefix_rewrite", r -> r
                .path("/gateway/**")
                .filters(f -> f
                    .rewritePath("/gateway/(?<remaining>.*)", "/${remaining}")
                )
                .uri("http://localhost:8081"))
            
            // Path parameter extraction: /users/123/orders -> /orders?userId=123
            .route("path_to_query_rewrite", r -> r
                .path("/users/{id}/orders")
                .filters(f -> f
                    .rewritePath("/users/(?<id>.*)/orders", "/orders?userId=${id}")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 2: StripPrefix - Remove Path Segments
     * Remove N path segments from beginning.
     */
    @Bean
    public RouteLocator stripPrefixRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Remove first segment: /api/users -> /users
            .route("strip_one_segment", r -> r
                .path("/api/**")
                .filters(f -> f
                    .stripPrefix(1)  // Remove /api
                )
                .uri("http://localhost:8081"))
            
            // Remove two segments: /gateway/api/users -> /users
            .route("strip_two_segments", r -> r
                .path("/gateway/api/**")
                .filters(f -> f
                    .stripPrefix(2)  // Remove /gateway/api
                )
                .uri("http://localhost:8081"))
            
            // Service routing: /services/users/** -> /**
            .route("service_strip", r -> r
                .path("/services/{service}/**")
                .filters(f -> f
                    .stripPrefix(2)  // Remove /services/{service}
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 3: SetPath - Replace Entire Path
     * Set new path, can use path variables.
     */
    @Bean
    public RouteLocator setPathRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Map specific path: /user/123 -> /api/v2/users/123
            .route("set_path_users", r -> r
                .path("/user/{id}")
                .filters(f -> f
                    .setPath("/api/v2/users/{id}")
                )
                .uri("http://localhost:8081"))
            
            // Redirect to health endpoint: /status -> /actuator/health
            .route("set_path_health", r -> r
                .path("/status")
                .filters(f -> f
                    .setPath("/actuator/health")
                )
                .uri("http://localhost:8081"))
            
            // Complex mapping: /products/{category}/{id} -> /catalog/{category}/items/{id}
            .route("set_path_catalog", r -> r
                .path("/products/{category}/{id}")
                .filters(f -> f
                    .setPath("/catalog/{category}/items/{id}")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 4: PrefixPath - Add Path Prefix
     * Prepend path segment to request path.
     */
    @Bean
    public RouteLocator prefixPathRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Add API version prefix: /users -> /v2/users
            .route("prefix_version", r -> r
                .path("/users/**")
                .filters(f -> f
                    .prefixPath("/v2")
                )
                .uri("http://localhost:8081"))
            
            // Add backend prefix: /api/** -> /backend/api/**
            .route("prefix_backend", r -> r
                .path("/api/**")
                .filters(f -> f
                    .prefixPath("/backend")
                )
                .uri("http://localhost:8081"))
            
            // Service namespace: /** -> /microservice/**
            .route("prefix_namespace", r -> r
                .path("/**")
                .filters(f -> f
                    .prefixPath("/microservice")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 5: Combine Path Rewriting Filters
     * Use multiple path filters together.
     */
    @Bean
    public RouteLocator combinedPathRewritingRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Strip prefix then rewrite: /gateway/v1/users/123 -> /api/users/123
            .route("strip_and_rewrite", r -> r
                .path("/gateway/v1/**")
                .filters(f -> f
                    .stripPrefix(1)           // Remove /gateway
                    .rewritePath("/v1/(?<segment>.*)", "/api/${segment}")
                )
                .uri("http://localhost:8081"))
            
            // Rewrite then prefix: /old/users -> /v2/new/users
            .route("rewrite_and_prefix", r -> r
                .path("/old/**")
                .filters(f -> f
                    .rewritePath("/old/(?<path>.*)", "/new/${path}")
                    .prefixPath("/v2")
                )
                .uri("http://localhost:8081"))
            
            // Complex transformation: /external/api/v1/users/123 -> /internal/users/123
            .route("complex_transform", r -> r
                .path("/external/api/v1/**")
                .filters(f -> f
                    .stripPrefix(3)  // Remove /external/api/v1
                    .prefixPath("/internal")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 6: Legacy URL Support
     * Support old URLs while migrating to new structure.
     */
    @Bean
    public RouteLocator legacyUrlRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Old product URL: /product.php?id=123 -> /products/123
            .route("legacy_product_url", r -> r
                .path("/product.php")
                .and().query("id")
                .filters(f -> f
                    .setPath("/products/{id}")
                )
                .uri("http://localhost:8081"))
            
            // Old user URL: /user_profile.jsp?user=john -> /users/john/profile
            .route("legacy_user_url", r -> r
                .path("/user_profile.jsp")
                .filters(f -> f
                    .rewritePath("/user_profile\\.jsp", "/users/{user}/profile")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 7: Service-Specific Path Mapping
     * Different path transformations for different microservices.
     */
    @Bean
    public RouteLocator serviceSpecificPathRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // User service: /api/users/** -> /users/**
            .route("user_service_path", r -> r
                .path("/api/users/**")
                .filters(f -> f
                    .rewritePath("/api/users/(?<segment>.*)", "/users/${segment}")
                )
                .uri("lb://user-service"))
            
            // Order service: /api/orders/** -> /v2/orders/**
            .route("order_service_path", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .rewritePath("/api/orders/(?<segment>.*)", "/v2/orders/${segment}")
                )
                .uri("lb://order-service"))
            
            // Product service: /api/products/** -> /catalog/**
            .route("product_service_path", r -> r
                .path("/api/products/**")
                .filters(f -> f
                    .rewritePath("/api/products/(?<segment>.*)", "/catalog/${segment}")
                )
                .uri("lb://product-service"))
            
            .build();
    }

    /**
     * Example 8: Path Normalization
     * Normalize paths for consistent routing.
     */
    @Bean
    public RouteLocator pathNormalizationRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Remove trailing slash: /users/ -> /users
            .route("remove_trailing_slash", r -> r
                .path("/**")
                .filters(f -> f
                    .rewritePath("/(?<segment>.*)/+$", "/${segment}")
                )
                .uri("http://localhost:8081"))
            
            // Lowercase paths: /Users/ABC -> /users/abc
            .route("lowercase_path", r -> r
                .path("/**")
                .filters(f -> f
                    .filter((exchange, chain) -> {
                        String path = exchange.getRequest().getPath().value();
                        String lowercasePath = path.toLowerCase();
                        if (!path.equals(lowercasePath)) {
                            ServerHttpRequest request = exchange.getRequest().mutate()
                                .path(lowercasePath)
                                .build();
                            return chain.filter(exchange.mutate().request(request).build());
                        }
                        return chain.filter(exchange);
                    })
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 9: Dynamic Path Rewriting
     * Rewrite paths based on request attributes.
     */
    @Bean
    public RouteLocator dynamicPathRewritingRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("dynamic_path_rewrite", r -> r
                .path("/api/**")
                .filters(f -> f
                    .filter((exchange, chain) -> {
                        // Get API version from header
                        String apiVersion = exchange.getRequest()
                            .getHeaders()
                            .getFirst("X-API-Version");
                        
                        String currentPath = exchange.getRequest().getPath().value();
                        String newPath = String.format("/%s%s", 
                            apiVersion != null ? apiVersion : "v1", 
                            currentPath
                        );
                        
                        ServerHttpRequest request = exchange.getRequest().mutate()
                            .path(newPath)
                            .build();
                        
                        return chain.filter(exchange.mutate().request(request).build());
                    })
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 10: Path Rewriting with Query Parameters
     * Transform path segments into query parameters.
     */
    @Bean
    public RouteLocator pathToQueryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // /search/books/java -> /search?category=books&query=java
            .route("path_to_query", r -> r
                .path("/search/{category}/{query}")
                .filters(f -> f
                    .setPath("/search?category={category}&query={query}")
                )
                .uri("http://localhost:8081"))
            
            // /users/123/posts/456 -> /posts/456?userId=123
            .route("nested_path_to_query", r -> r
                .path("/users/{userId}/posts/{postId}")
                .filters(f -> f
                    .setPath("/posts/{postId}?userId={userId}")
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * YAML Configuration Example:
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         # RewritePath example
     *         - id: rewrite_path_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/v1/**
     *           filters:
     *             - RewritePath=/api/v1/(?<segment>.*), /${segment}
     * 
     *         # StripPrefix example
     *         - id: strip_prefix_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/gateway/api/**
     *           filters:
     *             - StripPrefix=2
     * 
     *         # SetPath example
     *         - id: set_path_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/user/{id}
     *           filters:
     *             - SetPath=/api/v2/users/{id}
     * 
     *         # PrefixPath example
     *         - id: prefix_path_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/users/**
     *           filters:
     *             - PrefixPath=/v2
     * 
     *         # Combined filters
     *         - id: combined_path_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/gateway/v1/**
     *           filters:
     *             - StripPrefix=1
     *             - RewritePath=/v1/(?<segment>.*), /api/${segment}
     */

    /**
     * Best Practices:
     * 1. Use RewritePath for complex transformations
     * 2. Use StripPrefix for simple segment removal
     * 3. Use SetPath for complete path replacement
     * 4. Use PrefixPath for adding namespaces
     * 5. Test regex patterns thoroughly
     * 6. Document path transformations
     * 7. Handle trailing slashes consistently
     * 8. Preserve query parameters
     * 9. Use path variables for dynamic segments
     * 10. Validate transformed paths
     * 
     * Common Pitfalls:
     * 1. Incorrect regex patterns (missing escape characters)
     * 2. Lost query parameters during rewriting
     * 3. Multiple conflicting transformations
     * 4. Not handling edge cases (trailing slash, etc.)
     * 5. Breaking RESTful URLs
     * 6. Not documenting transformations
     * 7. Overly complex regex patterns
     * 8. Not testing with various URL formats
     * 9. Forgetting URL encoding
     * 10. Breaking backward compatibility
     * 
     * When to Use:
     * - API versioning
     * - Microservice path mapping
     * - Legacy URL support
     * - Path normalization
     * - Service abstraction
     * - URL beautification
     * 
     * When NOT to Use:
     * - Simple routing (no transformation needed)
     * - Breaking RESTful conventions unnecessarily
     * - Complex business logic (use filters instead)
     * - When query parameters are more appropriate
     */
}
