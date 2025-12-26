package com.example.cors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Method-level CORS Pattern
 * 
 * Demonstrates fine-grained CORS configuration at the method level,
 * allowing different CORS settings for each endpoint.
 * 
 * Features:
 * - Per-method CORS configuration
 * - Maximum flexibility
 * - Different origins per endpoint
 * - Override class-level settings
 */
@SpringBootApplication
public class MethodLevelCORSPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(MethodLevelCORSPattern.class, args);
    }
    
    @RestController
    @RequestMapping("/api")
    public static class MethodCorsController {
        
        /**
         * Public endpoint - open to all origins
         */
        @CrossOrigin(origins = "*")
        @GetMapping("/public/data")
        public Map<String, String> getPublicData() {
            return Map.of(
                "message", "Public data - CORS enabled for all origins",
                "timestamp", LocalDateTime.now().toString()
            );
        }
        
        /**
         * Restricted endpoint - specific origin only
         */
        @CrossOrigin(origins = "http://localhost:3000",
                    methods = RequestMethod.GET,
                    allowedHeaders = {"Content-Type"},
                    maxAge = 3600)
        @GetMapping("/users")
        public List<Map<String, String>> getUsers() {
            return Arrays.asList(
                Map.of("id", "1", "name", "John"),
                Map.of("id", "2", "name", "Jane")
            );
        }
        
        /**
         * POST endpoint - multiple origins
         */
        @CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"},
                    methods = RequestMethod.POST,
                    allowedHeaders = {"Content-Type", "Authorization"},
                    allowCredentials = "true")
        @PostMapping("/data")
        public Map<String, Object> createData(@RequestBody Map<String, Object> data) {
            return Map.of(
                "message", "Data created",
                "data", data,
                "timestamp", LocalDateTime.now()
            );
        }
        
        /**
         * Admin endpoint - very restrictive
         */
        @CrossOrigin(origins = "https://admin.example.com",
                    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE},
                    allowedHeaders = {"Authorization"},
                    allowCredentials = "true",
                    maxAge = 7200)
        @GetMapping("/admin/settings")
        public Map<String, Object> getAdminSettings() {
            return Map.of(
                "setting1", "value1",
                "setting2", "value2",
                "timestamp", LocalDateTime.now()
            );
        }
        
        /**
         * No CORS - restricted to same origin
         */
        @GetMapping("/internal/data")
        public Map<String, String> getInternalData() {
            return Map.of(
                "message", "Internal data - no CORS headers",
                "access", "Same origin only"
            );
        }
    }
    
    @RestController
    @RequestMapping("/demo")
    public static class DemoController {
        
        @GetMapping("/cors-methods")
        public Map<String, Object> getCorsMethodsInfo() {
            return Map.of(
                "pattern", "Method-level CORS",
                "endpoints", Map.of(
                    "/api/public/data", "All origins (*)",
                    "/api/users", "localhost:3000 only",
                    "/api/data", "localhost:3000 + localhost:4200",
                    "/admin/settings", "admin.example.com only",
                    "/internal/data", "No CORS (same origin)"
                ),
                "benefit", "Fine-grained control per endpoint",
                "timestamp", LocalDateTime.now()
            );
        }
    }
}
