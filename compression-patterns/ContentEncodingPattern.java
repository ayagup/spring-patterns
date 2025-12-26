package com.example.compression;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Content Encoding Pattern
 * 
 * Demonstrates Content-Encoding header management for indicating
 * the encoding transformations applied to the response body.
 * 
 * Features:
 * - Content-Encoding header management
 * - Multiple encoding support (gzip, deflate, br, identity)
 * - Accept-Encoding negotiation
 * - Vary header for caching
 * - Encoding quality values (q-values)
 * - Client capability detection
 * 
 * Key Components:
 * - Content negotiation
 * - Encoding selector
 * - Header manager
 * - Cache directives
 */
@SpringBootApplication
public class ContentEncodingPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(ContentEncodingPattern.class, args);
    }
    
    /**
     * Content Encoding Service
     */
    @Service
    public static class ContentEncodingService {
        
        /**
         * Select best encoding based on Accept-Encoding header
         */
        public String selectEncoding(String acceptEncoding) {
            if (acceptEncoding == null || acceptEncoding.isEmpty()) {
                return "identity";
            }
            
            Map<String, Double> encodings = parseAcceptEncoding(acceptEncoding);
            
            // Priority: br > gzip > deflate > identity
            if (encodings.containsKey("br") && encodings.get("br") > 0) {
                return "br"; // Brotli
            } else if (encodings.containsKey("gzip") && encodings.get("gzip") > 0) {
                return "gzip";
            } else if (encodings.containsKey("deflate") && encodings.get("deflate") > 0) {
                return "deflate";
            }
            
            return "identity";
        }
        
        /**
         * Parse Accept-Encoding header with q-values
         */
        private Map<String, Double> parseAcceptEncoding(String acceptEncoding) {
            Map<String, Double> encodings = new HashMap<>();
            
            String[] parts = acceptEncoding.split(",");
            for (String part : parts) {
                part = part.trim();
                String[] tokens = part.split(";");
                String encoding = tokens[0].trim();
                double quality = 1.0;
                
                if (tokens.length > 1) {
                    String qValue = tokens[1].trim();
                    if (qValue.startsWith("q=")) {
                        try {
                            quality = Double.parseDouble(qValue.substring(2));
                        } catch (NumberFormatException e) {
                            quality = 1.0;
                        }
                    }
                }
                
                encodings.put(encoding, quality);
            }
            
            return encodings;
        }
        
        /**
         * Get encoding info
         */
        public Map<String, Object> getEncodingInfo(String encoding) {
            Map<String, Object> info = new HashMap<>();
            info.put("encoding", encoding);
            
            switch (encoding) {
                case "gzip":
                    info.put("algorithm", "GZIP (RFC 1952)");
                    info.put("compressionRatio", "60-90%");
                    info.put("speed", "Fast");
                    info.put("support", "Universal");
                    break;
                case "deflate":
                    info.put("algorithm", "DEFLATE (RFC 1951)");
                    info.put("compressionRatio", "50-80%");
                    info.put("speed", "Fast");
                    info.put("support", "Wide");
                    break;
                case "br":
                    info.put("algorithm", "Brotli (RFC 7932)");
                    info.put("compressionRatio", "70-95%");
                    info.put("speed", "Medium");
                    info.put("support", "Modern browsers");
                    break;
                case "identity":
                    info.put("algorithm", "No encoding");
                    info.put("compressionRatio", "0%");
                    info.put("speed", "N/A");
                    info.put("support", "All");
                    break;
            }
            
            info.put("timestamp", LocalDateTime.now());
            return info;
        }
        
        /**
         * Get supported encodings
         */
        public List<Map<String, String>> getSupportedEncodings() {
            return Arrays.asList(
                Map.of("name", "gzip", "description", "GNU Zip compression"),
                Map.of("name", "deflate", "description", "DEFLATE compression"),
                Map.of("name", "br", "description", "Brotli compression"),
                Map.of("name", "identity", "description", "No transformation")
            );
        }
    }
    
    /**
     * Content Encoding Controller
     */
    @RestController
    @RequestMapping("/api/encoding")
    public static class ContentEncodingController {
        
        private final ContentEncodingService encodingService;
        
        public ContentEncodingController(ContentEncodingService encodingService) {
            this.encodingService = encodingService;
        }
        
        /**
         * Negotiate content encoding with client
         */
        @GetMapping("/negotiate")
        public Map<String, Object> negotiateEncoding(
                @RequestHeader(value = HttpHeaders.ACCEPT_ENCODING, 
                              defaultValue = "identity") String acceptEncoding,
                HttpServletResponse response) {
            
            String selectedEncoding = encodingService.selectEncoding(acceptEncoding);
            
            // Set response headers
            response.setHeader(HttpHeaders.CONTENT_ENCODING, selectedEncoding);
            response.setHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
            
            Map<String, Object> result = new HashMap<>();
            result.put("clientSent", acceptEncoding);
            result.put("serverSelected", selectedEncoding);
            result.put("responseHeaders", Map.of(
                "Content-Encoding", selectedEncoding,
                "Vary", "Accept-Encoding"
            ));
            result.put("timestamp", LocalDateTime.now());
            
            return result;
        }
        
        /**
         * Get encoding information
         */
        @GetMapping("/info/{encoding}")
        public Map<String, Object> getEncodingInfo(@PathVariable String encoding) {
            return encodingService.getEncodingInfo(encoding);
        }
        
        /**
         * List all supported encodings
         */
        @GetMapping("/supported")
        public Map<String, Object> getSupportedEncodings() {
            return Map.of(
                "encodings", encodingService.getSupportedEncodings(),
                "timestamp", LocalDateTime.now()
            );
        }
        
        /**
         * Test endpoint with large data
         */
        @GetMapping(value = "/test/large", produces = MediaType.APPLICATION_JSON_VALUE)
        public Map<String, Object> testLargeResponse(
                @RequestHeader(value = HttpHeaders.ACCEPT_ENCODING, 
                              defaultValue = "identity") String acceptEncoding,
                HttpServletResponse response) {
            
            String encoding = encodingService.selectEncoding(acceptEncoding);
            response.setHeader(HttpHeaders.CONTENT_ENCODING, encoding);
            response.setHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
            
            List<String> items = IntStream.range(0, 500)
                .mapToObj(i -> "Item " + i + ": Sample data for encoding test")
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Large response for encoding test");
            result.put("encoding", encoding);
            result.put("itemCount", items.size());
            result.put("items", items);
            result.put("timestamp", LocalDateTime.now());
            
            return result;
        }
        
        /**
         * Get encoding best practices
         */
        @GetMapping("/best-practices")
        public Map<String, Object> getBestPractices() {
            return Map.of(
                "practices", Arrays.asList(
                    "Always include Vary: Accept-Encoding header",
                    "Use Brotli for static assets when possible",
                    "Use GZIP for dynamic content",
                    "Don't compress already compressed formats (images, videos)",
                    "Set minimum size threshold (e.g., 1KB)",
                    "Compress text-based content types only"
                ),
                "recommendedEncodings", Map.of(
                    "staticAssets", "br (Brotli)",
                    "dynamicContent", "gzip",
                    "apiResponses", "gzip",
                    "legacy", "deflate"
                ),
                "headers", Map.of(
                    "clientRequest", "Accept-Encoding: gzip, deflate, br",
                    "serverResponse", "Content-Encoding: gzip",
                    "caching", "Vary: Accept-Encoding"
                ),
                "timestamp", LocalDateTime.now()
            );
        }
    }
}
