package com.example.cors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * CORS Filter Pattern
 * 
 * Demonstrates CORS implementation using a custom filter for
 * low-level control over CORS headers and request processing.
 * 
 * Features:
 * - Filter-based CORS handling
 * - Custom CORS logic
 * - Request/response inspection
 * - Dynamic origin validation
 * - Preflight request handling
 */
@SpringBootApplication
public class CORSFilterPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(CORSFilterPattern.class, args);
    }
    
    @Configuration
    public static class CorsFilterConfig {
        
        /**
         * Spring's built-in CORS filter
         */
        @Bean
        public CorsFilter corsFilter() {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.addAllowedOrigin("http://localhost:3000");
            config.addAllowedOrigin("http://localhost:4200");
            config.addAllowedOrigin("https://example.com");
            config.addAllowedHeader("*");
            config.addAllowedMethod("*");
            config.setMaxAge(3600L);
            
            source.registerCorsConfiguration("/**", config);
            return new CorsFilter(source);
        }
        
        /**
         * Custom CORS filter for advanced use cases
         */
        @Bean
        public CustomCorsFilter customCorsFilter() {
            return new CustomCorsFilter();
        }
    }
    
    /**
     * Custom CORS Filter
     * Provides full control over CORS logic
     */
    public static class CustomCorsFilter extends org.springframework.web.filter.OncePerRequestFilter {
        
        private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",
            "http://localhost:4200",
            "https://example.com"
        );
        
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                       HttpServletResponse response,
                                       FilterChain filterChain)
                throws ServletException, IOException {
            
            String origin = request.getHeader("Origin");
            
            // Validate origin
            if (origin != null && isAllowedOrigin(origin)) {
                // Set CORS headers
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Methods", 
                    "GET, POST, PUT, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers",
                    "Content-Type, Authorization, X-Requested-With");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Access-Control-Expose-Headers",
                    "Authorization, X-Total-Count");
            }
            
            // Handle preflight requests
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
            
            filterChain.doFilter(request, response);
        }
        
        private boolean isAllowedOrigin(String origin) {
            return ALLOWED_ORIGINS.contains(origin);
        }
    }
    
    /**
     * Dynamic Origin Validator
     * Validates origins based on custom logic
     */
    @org.springframework.stereotype.Component
    public static class OriginValidator {
        
        private final Set<String> whitelistedOrigins = new HashSet<>(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:4200",
            "https://example.com",
            "https://app.example.com"
        ));
        
        public boolean isAllowed(String origin) {
            if (origin == null) {
                return false;
            }
            
            // Check whitelist
            if (whitelistedOrigins.contains(origin)) {
                return true;
            }
            
            // Custom logic: allow subdomains of example.com
            if (origin.endsWith(".example.com") && origin.startsWith("https://")) {
                return true;
            }
            
            // Custom logic: allow localhost with any port
            if (origin.startsWith("http://localhost:")) {
                return true;
            }
            
            return false;
        }
        
        public void addOrigin(String origin) {
            whitelistedOrigins.add(origin);
        }
        
        public void removeOrigin(String origin) {
            whitelistedOrigins.remove(origin);
        }
        
        public Set<String> getAllowedOrigins() {
            return new HashSet<>(whitelistedOrigins);
        }
    }
    
    @RestController
    @RequestMapping("/api")
    public static class CorsFilterController {
        
        private final OriginValidator originValidator;
        
        public CorsFilterController(OriginValidator originValidator) {
            this.originValidator = originValidator;
        }
        
        @GetMapping("/data")
        public Map<String, Object> getData() {
            return Map.of(
                "message", "Data protected by CORS filter",
                "timestamp", LocalDateTime.now(),
                "data", Arrays.asList("item1", "item2", "item3")
            );
        }
        
        @PostMapping("/data")
        public Map<String, Object> createData(@RequestBody Map<String, Object> data) {
            return Map.of(
                "message", "Data created",
                "receivedData", data,
                "timestamp", LocalDateTime.now()
            );
        }
        
        @GetMapping("/origins")
        public Map<String, Object> getAllowedOrigins() {
            return Map.of(
                "allowedOrigins", originValidator.getAllowedOrigins(),
                "validationLogic", Arrays.asList(
                    "Whitelist check",
                    "Subdomain matching (*.example.com)",
                    "Localhost with any port"
                ),
                "timestamp", LocalDateTime.now()
            );
        }
        
        @PostMapping("/origins")
        public Map<String, String> addOrigin(@RequestBody Map<String, String> request) {
            String origin = request.get("origin");
            originValidator.addOrigin(origin);
            
            return Map.of(
                "message", "Origin added to whitelist",
                "origin", origin,
                "timestamp", LocalDateTime.now().toString()
            );
        }
    }
    
    @RestController
    @RequestMapping("/info")
    public static class InfoController {
        
        @GetMapping("/cors-filter")
        public Map<String, Object> getCorsFilterInfo() {
            return Map.of(
                "pattern", "CORS Filter Pattern",
                "description", "Filter-based CORS implementation",
                "features", Arrays.asList(
                    "Low-level control over CORS headers",
                    "Custom origin validation logic",
                    "Preflight request handling",
                    "Dynamic origin management",
                    "Request/response inspection"
                ),
                "filterOrder", "Executes before Spring Security",
                "headers", Map.of(
                    "request", "Origin",
                    "response", Arrays.asList(
                        "Access-Control-Allow-Origin",
                        "Access-Control-Allow-Methods",
                        "Access-Control-Allow-Headers",
                        "Access-Control-Allow-Credentials",
                        "Access-Control-Max-Age",
                        "Access-Control-Expose-Headers"
                    )
                ),
                "timestamp", LocalDateTime.now()
            );
        }
    }
}
