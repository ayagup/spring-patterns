import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS Configuration Pattern - Spring Cloud Gateway
 * =================================================
 * 
 * Cross-Origin Resource Sharing (CORS) allows controlled access to resources
 * from different origins (domain, protocol, or port).
 * 
 * CORS Headers:
 * - Access-Control-Allow-Origin: Allowed origins
 * - Access-Control-Allow-Methods: Allowed HTTP methods
 * - Access-Control-Allow-Headers: Allowed request headers
 * - Access-Control-Allow-Credentials: Allow credentials
 * - Access-Control-Max-Age: Preflight cache duration
 * - Access-Control-Expose-Headers: Headers accessible to client
 * 
 * Use Cases:
 * - Frontend-backend communication from different domains
 * - Mobile app API access
 * - Third-party integrations
 * - Microservices architecture
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 */
@Configuration
public class CorsConfigurationPattern {

    /**
     * Example 1: Global CORS Configuration
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://example.com"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }

    /**
     * Example 2: Path-Specific CORS
     */
    @Bean
    public CorsWebFilter pathSpecificCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Public API - permissive CORS
        CorsConfiguration publicConfig = new CorsConfiguration();
        publicConfig.setAllowedOrigins(Arrays.asList("*"));
        publicConfig.setAllowedMethods(Arrays.asList("GET", "POST"));
        publicConfig.setAllowedHeaders(Arrays.asList("*"));
        source.registerCorsConfiguration("/api/public/**", publicConfig);
        
        // Admin API - restrictive CORS
        CorsConfiguration adminConfig = new CorsConfiguration();
        adminConfig.setAllowedOrigins(Arrays.asList("https://admin.example.com"));
        adminConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        adminConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        adminConfig.setAllowCredentials(true);
        source.registerCorsConfiguration("/api/admin/**", adminConfig);
        
        return new CorsWebFilter(source);
    }

    /**
     * YAML Configuration:
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       globalcors:
     *         cors-configurations:
     *           '[/**]':
     *             allowedOrigins: "http://localhost:3000"
     *             allowedMethods:
     *               - GET
     *               - POST
     *               - PUT
     *               - DELETE
     *             allowedHeaders: "*"
     *             allowCredentials: true
     *             maxAge: 3600
     */
}
