import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Route Predicate Pattern - Spring Cloud Gateway
 * ============================================
 * 
 * Predicates determine if a route should be matched based on incoming request attributes.
 * Spring Cloud Gateway provides built-in predicates for path, method, headers, query params,
 * cookies, host, time-based routing, and more.
 * 
 * Built-in Route Predicates:
 * - Path: Match request path using Ant-style patterns
 * - Method: Match HTTP method (GET, POST, PUT, DELETE, etc.)
 * - Header: Match request header with optional regex
 * - Query: Match query parameter with optional regex
 * - Cookie: Match cookie with optional regex
 * - Host: Match Host header with Ant-style patterns
 * - RemoteAddr: Match remote IP address with CIDR notation
 * - Before/After/Between: Time-based routing
 * - Weight: Weighted routing for gradual rollout
 * - CloudFoundryRouteService: Cloud Foundry routing
 * 
 * Use Cases:
 * - API versioning (header-based, path-based)
 * - Feature flags and A/B testing
 * - Canary deployments
 * - Geographic routing
 * - Time-based access control
 * - Request routing based on custom headers
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 */
@Configuration
public class RoutePredicatePattern {

    /**
     * Example 1: Path Predicate
     * Matches request path using Ant-style patterns.
     * - ** matches zero or more path segments
     * - * matches zero or more characters in a path segment
     * - ? matches exactly one character
     */
    @Bean
    public RouteLocator pathPredicateRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Match exact path
            .route("exact_path_route", r -> r.path("/api/users")
                .uri("http://localhost:8081"))
            
            // Match path with single wildcard
            .route("wildcard_path_route", r -> r.path("/api/users/*")
                .uri("http://localhost:8081"))
            
            // Match path with double wildcard (multiple segments)
            .route("double_wildcard_route", r -> r.path("/api/**")
                .uri("http://localhost:8081"))
            
            // Match multiple paths
            .route("multiple_paths_route", r -> r.path("/api/v1/**", "/api/v2/**")
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 2: Method Predicate
     * Matches HTTP method (GET, POST, PUT, DELETE, PATCH, etc.)
     */
    @Bean
    public RouteLocator methodPredicateRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Match single HTTP method
            .route("get_method_route", r -> r
                .method(HttpMethod.GET)
                .and().path("/api/users")
                .uri("http://localhost:8081"))
            
            // Match multiple HTTP methods
            .route("post_put_method_route", r -> r
                .method(HttpMethod.POST, HttpMethod.PUT)
                .and().path("/api/users/**")
                .uri("http://localhost:8081"))
            
            // Read-only endpoint (GET, HEAD, OPTIONS)
            .route("readonly_route", r -> r
                .method(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS)
                .and().path("/api/public/**")
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 3: Header Predicate
     * Matches request header with optional regex pattern.
     */
    @Bean
    public RouteLocator headerPredicateRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Match header existence
            .route("header_exists_route", r -> r
                .header("X-Request-Id")
                .uri("http://localhost:8081"))
            
            // Match header with exact value
            .route("header_exact_value_route", r -> r
                .header("X-API-Version", "v2")
                .and().path("/api/**")
                .uri("http://localhost:8082"))
            
            // Match header with regex pattern
            .route("header_regex_route", r -> r
                .header("X-Request-Type", "admin|internal")
                .uri("http://localhost:8083"))
            
            // API versioning via header
            .route("api_version_header_route", r -> r
                .header("Accept-Version", "2\\..*")  // Matches 2.x
                .uri("http://localhost:8082"))
            
            // Feature flag routing
            .route("feature_flag_route", r -> r
                .header("X-Feature-Flag", "new-ui-enabled")
                .uri("http://localhost:8090"))
            
            .build();
    }

    /**
     * Example 4: Query Parameter Predicate
     * Matches query parameter with optional regex pattern.
     */
    @Bean
    public RouteLocator queryPredicateRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Match query parameter existence
            .route("query_exists_route", r -> r
                .query("debug")
                .uri("http://localhost:8084"))
            
            // Match query parameter with exact value
            .route("query_exact_value_route", r -> r
                .query("version", "2")
                .uri("http://localhost:8082"))
            
            // Match query parameter with regex
            .route("query_regex_route", r -> r
                .query("region", "us-.*")  // Matches us-east, us-west, etc.
                .uri("http://localhost:8085"))
            
            // A/B testing via query parameter
            .route("ab_test_route", r -> r
                .query("variant", "B")
                .uri("http://localhost:8090"))
            
            // Multiple query parameters
            .route("multiple_query_route", r -> r
                .query("source", "mobile")
                .and().query("version", "3\\..*")
                .uri("http://localhost:8086"))
            
            .build();
    }

    /**
     * Example 5: Cookie Predicate
     * Matches cookie with optional regex pattern.
     */
    @Bean
    public RouteLocator cookiePredicateRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Match cookie existence
            .route("cookie_exists_route", r -> r
                .cookie("session")
                .uri("http://localhost:8081"))
            
            // Match cookie with regex value
            .route("cookie_regex_route", r -> r
                .cookie("user_type", "premium|enterprise")
                .uri("http://localhost:8087"))
            
            // Beta user routing
            .route("beta_user_route", r -> r
                .cookie("beta_access", "true")
                .uri("http://localhost:8090"))
            
            .build();
    }

    /**
     * Example 6: Host Predicate
     * Matches Host header with Ant-style patterns.
     * Supports URI template variables: {sub}.example.com
     */
    @Bean
    public RouteLocator hostPredicateRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Match exact host
            .route("exact_host_route", r -> r
                .host("api.example.com")
                .uri("http://localhost:8081"))
            
            // Match multiple hosts
            .route("multiple_hosts_route", r -> r
                .host("api.example.com", "api.example.org")
                .uri("http://localhost:8081"))
            
            // Match host with wildcard
            .route("wildcard_host_route", r -> r
                .host("*.example.com")
                .uri("http://localhost:8081"))
            
            // Match host with URI template variable
            .route("template_host_route", r -> r
                .host("{subdomain}.example.com")
                .uri("http://localhost:8081"))
            
            // Environment-based routing
            .route("staging_host_route", r -> r
                .host("*.staging.example.com")
                .uri("http://localhost:8088"))
            
            .build();
    }

    /**
     * Example 7: RemoteAddr Predicate
     * Matches client IP address using CIDR notation (IPv4 and IPv6).
     */
    @Bean
    public RouteLocator remoteAddrPredicateRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Match single IP address
            .route("single_ip_route", r -> r
                .remoteAddr("192.168.1.1/32")
                .uri("http://localhost:8081"))
            
            // Match IP range (CIDR notation)
            .route("ip_range_route", r -> r
                .remoteAddr("192.168.1.0/24")  // 192.168.1.0 - 192.168.1.255
                .uri("http://localhost:8081"))
            
            // Internal network routing
            .route("internal_network_route", r -> r
                .remoteAddr("10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16")
                .uri("http://localhost:8089"))
            
            // IPv6 support
            .route("ipv6_route", r -> r
                .remoteAddr("2001:db8::/32")
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 8: Time-Based Predicates
     * Route based on request time (Before, After, Between).
     * Uses ZonedDateTime for timezone support.
     */
    @Bean
    public RouteLocator timeBasedPredicateRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Route active after specific time
            .route("after_time_route", r -> r
                .after(ZonedDateTime.parse("2024-01-01T00:00:00+00:00"))
                .uri("http://localhost:8081"))
            
            // Route active before specific time
            .route("before_time_route", r -> r
                .before(ZonedDateTime.parse("2025-01-01T00:00:00+00:00"))
                .uri("http://localhost:8081"))
            
            // Route active between times (maintenance window)
            .route("between_time_route", r -> r
                .between(
                    ZonedDateTime.parse("2024-06-01T00:00:00+00:00"),
                    ZonedDateTime.parse("2024-06-30T23:59:59+00:00")
                )
                .uri("http://localhost:8090"))
            
            .build();
    }

    /**
     * Example 9: Weight-Based Predicate
     * Weighted routing for canary deployments and gradual rollout.
     * Weights are normalized to percentages.
     */
    @Bean
    public RouteLocator weightPredicateRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // 80% traffic to stable version
            .route("weight_80_route", r -> r
                .weight("version", 80)
                .uri("http://localhost:8081"))
            
            // 20% traffic to canary version
            .route("weight_20_route", r -> r
                .weight("version", 20)
                .uri("http://localhost:8090"))
            
            .build();
    }

    /**
     * Example 10: Composite Predicates (AND/OR)
     * Combine multiple predicates with logical operators.
     */
    @Bean
    public RouteLocator compositePredicateRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // AND: All predicates must match
            .route("and_predicate_route", r -> r
                .path("/api/admin/**")
                .and().method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                .and().header("X-Admin-Token")
                .uri("http://localhost:8091"))
            
            // Complex API versioning
            .route("api_v2_route", r -> r
                .path("/api/**")
                .and().header("Accept-Version", "2\\..*")
                .and().method(HttpMethod.GET, HttpMethod.POST)
                .uri("http://localhost:8082"))
            
            // Geographic + time-based routing
            .route("geo_time_route", r -> r
                .path("/api/events/**")
                .and().query("region", "us-.*")
                .and().after(ZonedDateTime.parse("2024-01-01T00:00:00+00:00"))
                .uri("http://localhost:8092"))
            
            // Feature flag + user type routing
            .route("feature_user_route", r -> r
                .header("X-Feature-New-UI", "enabled")
                .and().cookie("user_type", "premium|enterprise")
                .and().path("/dashboard/**")
                .uri("http://localhost:8093"))
            
            // Multi-condition admin access
            .route("admin_access_route", r -> r
                .path("/admin/**")
                .and().remoteAddr("192.168.1.0/24")
                .and().header("X-API-Key")
                .and().method(HttpMethod.GET, HttpMethod.POST)
                .uri("http://localhost:8094"))
            
            .build();
    }

    /**
     * YAML Configuration Example
     * ==========================
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         # Path predicate
     *         - id: path_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     * 
     *         # Method predicate
     *         - id: method_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Method=GET,POST
     * 
     *         # Header predicate
     *         - id: header_route
     *           uri: http://localhost:8082
     *           predicates:
     *             - Header=X-API-Version, v2
     * 
     *         # Query predicate
     *         - id: query_route
     *           uri: http://localhost:8085
     *           predicates:
     *             - Query=region, us-.*
     * 
     *         # Cookie predicate
     *         - id: cookie_route
     *           uri: http://localhost:8087
     *           predicates:
     *             - Cookie=user_type, premium|enterprise
     * 
     *         # Host predicate
     *         - id: host_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Host=*.example.com
     * 
     *         # RemoteAddr predicate
     *         - id: remote_addr_route
     *           uri: http://localhost:8089
     *           predicates:
     *             - RemoteAddr=192.168.1.0/24
     * 
     *         # Time-based predicates
     *         - id: after_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - After=2024-01-01T00:00:00+00:00
     * 
     *         - id: before_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Before=2025-01-01T00:00:00+00:00
     * 
     *         - id: between_route
     *           uri: http://localhost:8090
     *           predicates:
     *             - Between=2024-06-01T00:00:00+00:00, 2024-06-30T23:59:59+00:00
     * 
     *         # Weight predicate (canary deployment)
     *         - id: weight_80
     *           uri: http://localhost:8081
     *           predicates:
     *             - Weight=version, 80
     * 
     *         - id: weight_20
     *           uri: http://localhost:8090
     *           predicates:
     *             - Weight=version, 20
     * 
     *         # Composite predicates
     *         - id: composite_route
     *           uri: http://localhost:8091
     *           predicates:
     *             - Path=/api/admin/**
     *             - Method=POST,PUT,DELETE
     *             - Header=X-Admin-Token
     * 
     *         # API versioning
     *         - id: api_v2
     *           uri: http://localhost:8082
     *           predicates:
     *             - Path=/api/**
     *             - Header=Accept-Version, 2\..*
     *             - Method=GET,POST
     * 
     *         # Feature flag routing
     *         - id: feature_route
     *           uri: http://localhost:8093
     *           predicates:
     *             - Header=X-Feature-New-UI, enabled
     *             - Cookie=user_type, premium|enterprise
     *             - Path=/dashboard/**
     */

    /**
     * Custom Route Predicate Factory
     * ==============================
     * 
     * Create custom predicates for specific business logic.
     */
    @Component
    public static class TenantRoutePredicateFactory 
            extends org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory<TenantRoutePredicateFactory.Config> {
        
        public TenantRoutePredicateFactory() {
            super(Config.class);
        }
        
        @Override
        public java.util.function.Predicate<org.springframework.web.server.ServerWebExchange> 
                apply(Config config) {
            return exchange -> {
                String tenantId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Tenant-ID");
                return config.getTenantIds().contains(tenantId);
            };
        }
        
        public static class Config {
            private List<String> tenantIds;
            
            public List<String> getTenantIds() {
                return tenantIds;
            }
            
            public void setTenantIds(List<String> tenantIds) {
                this.tenantIds = tenantIds;
            }
        }
    }

    /**
     * Best Practices:
     * ===============
     * 
     * 1. Predicate Order: More specific predicates first
     * 2. Performance: Use lightweight predicates (path, method) before heavy ones (regex)
     * 3. Security: Validate IP addresses and headers to prevent spoofing
     * 4. Testing: Test predicate combinations thoroughly
     * 5. Monitoring: Log route matches for debugging
     * 6. Caching: Predicates are evaluated on every request - keep them fast
     * 7. Regex: Use anchors (^, $) in regex patterns for exact matching
     * 8. Time zones: Always use ZonedDateTime for time-based predicates
     * 9. Weight: Ensure weights sum correctly for percentage-based routing
     * 10. Fallback: Provide default route for unmatched requests
     * 
     * Common Pitfalls:
     * ================
     * 
     * 1. Overlapping routes: First matching route wins
     * 2. Regex escaping: Remember to escape special characters (\\.)
     * 3. Header case: Headers are case-insensitive in HTTP
     * 4. IP spoofing: Don't trust X-Forwarded-For without validation
     * 5. Time zones: Missing timezone causes unexpected behavior
     * 6. Path encoding: Handle URL-encoded characters properly
     * 7. Weight normalization: Weights are relative, not absolute percentages
     * 8. Cookie security: Validate cookie values to prevent injection
     * 9. Host header: Can be spoofed - use with caution for security
     * 10. Predicate caching: Predicates are not cached - optimize for performance
     * 
     * When to Use:
     * ============
     * 
     * - Path: API versioning, URL-based routing
     * - Method: Read-only vs. write endpoints
     * - Header: API versioning, feature flags, client type routing
     * - Query: A/B testing, debug mode, regional routing
     * - Cookie: User-specific routing, session-based routing
     * - Host: Multi-tenancy, environment routing
     * - RemoteAddr: Internal vs. external traffic, IP whitelisting
     * - Time: Maintenance windows, time-limited features
     * - Weight: Canary deployments, gradual rollout
     * 
     * When NOT to Use:
     * ================
     * 
     * - Don't use predicates for complex business logic (use filters instead)
     * - Don't rely on client-provided headers for security (can be spoofed)
     * - Don't use regex when exact matching suffices (performance)
     * - Don't use time-based predicates for authentication (use tokens)
     * - Don't trust X-Forwarded-For without proxy validation
     */
}
