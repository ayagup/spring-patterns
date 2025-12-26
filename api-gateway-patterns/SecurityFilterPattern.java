import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Security Filter Pattern - Spring Cloud Gateway
 * ==============================================
 * 
 * Security filters handle authentication, authorization, and security headers.
 * 
 * Security Concerns:
 * - Authentication: Verify user identity (JWT, OAuth2, API keys)
 * - Authorization: Check permissions (roles, scopes)
 * - Security Headers: Add security-related headers
 * - Input Validation: Validate request parameters
 * - Rate Limiting: Prevent abuse
 * 
 * Use Cases:
 * - JWT token validation
 * - OAuth2 resource server
 * - API key authentication
 * - Role-based access control
 * - Security header injection
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-security</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
 * </dependency>
 */
@Configuration
public class SecurityFilterPattern {

    /**
     * Example 1: JWT Authentication Filter
     */
    @Component
    public static class JwtAuthenticationFilterFactory
            extends AbstractGatewayFilterFactory<JwtAuthenticationFilterFactory.Config> {
        
        public JwtAuthenticationFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
                
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
                
                String token = authHeader.substring(7);
                if (!validateJwt(token)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                
                // Add user info to request
                exchange.getAttributes().put("userId", extractUserId(token));
                
                return chain.filter(exchange);
            };
        }
        
        private boolean validateJwt(String token) {
            // Validate JWT (use JWT library in production)
            return token.length() > 10;
        }
        
        private String extractUserId(String token) {
            // Extract user ID from JWT
            return "user-" + token.hashCode();
        }
        
        public static class Config {
            private String jwtSecret = "secret";
            
            public String getJwtSecret() {
                return jwtSecret;
            }
            
            public void setJwtSecret(String jwtSecret) {
                this.jwtSecret = jwtSecret;
            }
        }
    }

    /**
     * Example 2: API Key Authentication Filter
     */
    @Component
    public static class ApiKeyAuthenticationFilterFactory
            extends AbstractGatewayFilterFactory<ApiKeyAuthenticationFilterFactory.Config> {
        
        public ApiKeyAuthenticationFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
                
                if (apiKey == null || !config.getValidApiKeys().contains(apiKey)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    exchange.getResponse().getHeaders().add("WWW-Authenticate", "API-Key");
                    return exchange.getResponse().setComplete();
                }
                
                return chain.filter(exchange);
            };
        }
        
        public static class Config {
            private List<String> validApiKeys = Arrays.asList("key1", "key2");
            
            public List<String> getValidApiKeys() {
                return validApiKeys;
            }
            
            public void setValidApiKeys(List<String> validApiKeys) {
                this.validApiKeys = validApiKeys;
            }
        }
    }

    /**
     * Example 3: Role-Based Authorization Filter
     */
    @Component
    public static class RoleBasedAuthorizationFilterFactory
            extends AbstractGatewayFilterFactory<RoleBasedAuthorizationFilterFactory.Config> {
        
        public RoleBasedAuthorizationFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                String userRole = exchange.getRequest().getHeaders().getFirst("X-User-Role");
                
                if (userRole == null || !config.getAllowedRoles().contains(userRole)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                
                return chain.filter(exchange);
            };
        }
        
        public static class Config {
            private List<String> allowedRoles = Arrays.asList("ADMIN", "USER");
            
            public List<String> getAllowedRoles() {
                return allowedRoles;
            }
            
            public void setAllowedRoles(List<String> allowedRoles) {
                this.allowedRoles = allowedRoles;
            }
        }
    }

    /**
     * YAML Configuration:
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         - id: protected_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/admin/**
     *           filters:
     *             - name: JwtAuthentication
     *               args:
     *                 jwtSecret: ${JWT_SECRET}
     *             - name: RoleBasedAuthorization
     *               args:
     *                 allowedRoles:
     *                   - ADMIN
     */
}
