package com.example.cors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Global CORS Configuration Pattern
 * 
 * Demonstrates application-wide CORS configuration using WebMvcConfigurer
 * instead of per-controller annotations.
 * 
 * Features:
 * - Global CORS rules
 * - Path-based configuration
 * - Centralized CORS management
 * - Multiple origin patterns
 * - Default CORS settings
 */
@SpringBootApplication
public class GlobalCORSConfigPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(GlobalCORSConfigPattern.class, args);
    }
    
    @Configuration
    public static class GlobalCorsConfig implements WebMvcConfigurer {
        
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            // CORS for API endpoints
            registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:4200", "https://example.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Total-Count", "X-Page-Number", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
            
            // CORS for public endpoints
            registry.addMapping("/public/**")
                .allowedOrigins("*")
                .allowedMethods("GET")
                .maxAge(1800);
            
            // CORS for admin endpoints  - more restrictive
            registry.addMapping("/admin/**")
                .allowedOrigins("https://admin.example.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true)
                .maxAge(7200);
        }
        
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:4200",
                "https://example.com"
            ));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(Arrays.asList("*"));
            configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
            configuration.setAllowCredentials(true);
            configuration.setMaxAge(3600L);
            
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
    }
    
    @RestController
    @RequestMapping("/api")
    public static class ApiController {
        
        @GetMapping("/users")
        public List<Map<String, String>> getUsers() {
            return Arrays.asList(
                Map.of("id", "1", "name", "John Doe"),
                Map.of("id", "2", "name", "Jane Smith")
            );
        }
        
        @PostMapping("/users")
        public Map<String, Object> createUser(@RequestBody Map<String, String> user) {
            return Map.of(
                "message", "User created",
                "user", user,
                "timestamp", LocalDateTime.now()
            );
        }
    }
    
    @RestController
    @RequestMapping("/public")
    public static class PublicController {
        
        @GetMapping("/info")
        public Map<String, String> getInfo() {
            return Map.of(
                "message", "Public endpoint with global CORS",
                "corsConfig", "Allows all origins for GET requests"
            );
        }
    }
    
    @RestController
    @RequestMapping("/admin")
    public static class AdminController {
        
        @GetMapping("/stats")
        public Map<String, Object> getStats() {
            return Map.of(
                "totalUsers", 1250,
                "activeUsers", 856,
                "corsConfig", "Restricted to admin.example.com only"
            );
        }
    }
}
