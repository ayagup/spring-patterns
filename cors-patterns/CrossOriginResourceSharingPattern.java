package com.example.cors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Cross-Origin Resource Sharing (CORS) Pattern
 * 
 * Demonstrates CORS configuration to allow cross-origin requests
 * from different domains, enabling browser-based API access.
 * 
 * Features:
 * - CORS header management
 * - Origin validation
 * - Allowed methods configuration
 * - Preflight request handling
 * - Credentials support
 * - Exposed headers
 * 
 * Key Components:
 * - @CrossOrigin annotation
 * - CORS headers (Access-Control-*)
 * - Preflight OPTIONS handling
 * - Origin whitelist
 */
@SpringBootApplication
public class CrossOriginResourceSharingPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(CrossOriginResourceSharingPattern.class, args);
    }
    
    /**
     * Controller with basic CORS support
     */
    @RestController
    @RequestMapping("/api")
    public static class CORSController {
        
        /**
         * Endpoint with @CrossOrigin annotation
         * Allows requests from specific origins
         */
        @CrossOrigin(origins = {"http://localhost:3000", "https://example.com"},
                    methods = {RequestMethod.GET, RequestMethod.POST},
                    maxAge = 3600,
                    allowedHeaders = {"Content-Type", "Authorization"},
                    exposedHeaders = {"X-Total-Count", "X-Page-Number"},
                    allowCredentials = "true")
        @GetMapping("/data")
        public Map<String, Object> getData() {
            return Map.of(
                "message", "CORS enabled endpoint",
                "data", Arrays.asList("item1", "item2", "item3"),
                "timestamp", LocalDateTime.now()
            );
        }
        
        /**
         * Endpoint allowing all origins (for development only)
         */
        @CrossOrigin(origins = "*")
        @GetMapping("/public")
        public Map<String, String> getPublicData() {
            return Map.of(
                "message", "Public endpoint with CORS enabled for all origins",
                "warning", "Use specific origins in production",
                "timestamp", LocalDateTime.now().toString()
            );
        }
        
        /**
         * POST endpoint with CORS
         */
        @CrossOrigin(origins = "http://localhost:3000",
                    methods = RequestMethod.POST,
                    allowedHeaders = "*")
        @PostMapping("/submit")
        public Map<String, Object> submitData(@RequestBody Map<String, Object> data) {
            return Map.of(
                "message", "Data received via CORS request",
                "receivedData", data,
                "timestamp", LocalDateTime.now()
            );
        }
        
        /**
         * Endpoint with manual CORS headers
         */
        @GetMapping("/manual-cors")
        public Map<String, Object> manualCORS(HttpServletResponse response) {
            // Manually set CORS headers
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Expose-Headers", "X-Custom-Header");
            
            return Map.of(
                "message", "Endpoint with manually configured CORS headers",
                "corsEnabled", true,
                "timestamp", LocalDateTime.now()
            );
        }
        
        /**
         * CORS information endpoint
         */
        @CrossOrigin(origins = "*")
        @GetMapping("/cors-info")
        public Map<String, Object> getCORSInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("description", "Cross-Origin Resource Sharing (CORS)");
            info.put("purpose", "Allow cross-origin requests from browsers");
            
            info.put("headers", Map.of(
                "Access-Control-Allow-Origin", "Specifies allowed origins",
                "Access-Control-Allow-Methods", "Specifies allowed HTTP methods",
                "Access-Control-Allow-Headers", "Specifies allowed headers",
                "Access-Control-Max-Age", "Preflight cache duration",
                "Access-Control-Allow-Credentials", "Allow credentials (cookies)",
                "Access-Control-Expose-Headers", "Headers exposed to client"
            ));
            
            info.put("preflightRequest", Map.of(
                "method", "OPTIONS",
                "purpose", "Browser checks permissions before actual request",
                "triggeredBy", "Non-simple requests (custom headers, methods other than GET/POST)"
            ));
            
            info.put("bestPractices", Arrays.asList(
                "Never use '*' for allowedOrigins in production with credentials",
                "Specify explicit origins for security",
                "Use maxAge to reduce preflight requests",
                "Only expose necessary headers",
                "Validate origin on server side"
            ));
            
            info.put("timestamp", LocalDateTime.now());
            
            return info;
        }
        
        /**
         * Preflight OPTIONS handler
         * Browsers send OPTIONS request for preflight
         */
        @CrossOrigin(origins = "http://localhost:3000")
        @RequestMapping(value = "/preflight-test", method = RequestMethod.OPTIONS)
        public Map<String, String> preflightHandler() {
            return Map.of(
                "message", "Preflight OPTIONS request handled",
                "timestamp", LocalDateTime.now().toString()
            );
        }
    }
}
