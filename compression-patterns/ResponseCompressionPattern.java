package com.example.compression;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Compression;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Response Compression Pattern
 * 
 * Demonstrates automatic HTTP response compression to reduce bandwidth
 * and improve application performance.
 * 
 * Features:
 * - Automatic response compression
 * - Configurable compression threshold
 * - MIME type filtering
 * - GZIP compression algorithm
 * - Content-Encoding header management
 * - Compression ratio monitoring
 * 
 * Key Components:
 * - Compression configuration
 * - MIME type whitelist
 * - Minimum response size threshold
 * - Compression middleware
 */
@SpringBootApplication
public class ResponseCompressionPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(ResponseCompressionPattern.class, args);
    }
    
    /**
     * Compression Configuration
     * Configures server-level response compression
     */
    @Configuration
    public static class CompressionConfig {
        
        @Bean
        public WebServerFactoryCustomizer<TomcatServletWebServerFactory> compressionCustomizer() {
            return factory -> {
                Compression compression = new Compression();
                compression.setEnabled(true);
                
                // Minimum response size to compress (2KB)
                compression.setMinResponseSize(DataSize.ofKilobytes(2));
                
                // MIME types to compress
                compression.setMimeTypes(new String[]{
                    "text/html",
                    "text/xml",
                    "text/plain",
                    "text/css",
                    "text/javascript",
                    "application/javascript",
                    "application/json",
                    "application/xml"
                });
                
                factory.setCompression(compression);
            };
        }
    }
    
    /**
     * Sample Data Service
     * Generates test data of various sizes
     */
    @org.springframework.stereotype.Service
    public static class DataService {
        
        public Map<String, Object> generateSmallData() {
            return Map.of(
                "message", "Small data - may not be compressed",
                "size", "< 2KB",
                "timestamp", LocalDateTime.now()
            );
        }
        
        public Map<String, Object> generateLargeData() {
            List<String> items = IntStream.range(0, 1000)
                .mapToObj(i -> "Item " + i + ": This is a sample text item with enough content to demonstrate compression")
                .collect(Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Large data - should be compressed");
            data.put("size", "> 2KB");
            data.put("itemCount", items.size());
            data.put("items", items);
            data.put("timestamp", LocalDateTime.now());
            
            return data;
        }
        
        public String generateLargeText() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                sb.append("This is line ").append(i)
                  .append(": Lorem ipsum dolor sit amet, consectetur adipiscing elit. ")
                  .append("Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n");
            }
            return sb.toString();
        }
        
        public Map<String, Object> getCompressionStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("compressionEnabled", true);
            stats.put("algorithm", "GZIP");
            stats.put("minResponseSize", "2KB");
            stats.put("compressedMimeTypes", Arrays.asList(
                "text/html", "text/xml", "text/plain", "text/css",
                "text/javascript", "application/javascript", 
                "application/json", "application/xml"
            ));
            stats.put("estimatedCompressionRatio", "70-90% for text content");
            stats.put("timestamp", LocalDateTime.now());
            
            return stats;
        }
    }
    
    /**
     * Compression Test Controller
     */
    @RestController
    @RequestMapping("/api")
    public static class CompressionController {
        
        private final DataService dataService;
        
        public CompressionController(DataService dataService) {
            this.dataService = dataService;
        }
        
        /**
         * Small response - likely not compressed
         */
        @GetMapping("/small")
        public Map<String, Object> getSmallData() {
            return dataService.generateSmallData();
        }
        
        /**
         * Large JSON response - should be compressed
         */
        @GetMapping(value = "/large", produces = MediaType.APPLICATION_JSON_VALUE)
        public Map<String, Object> getLargeData() {
            return dataService.generateLargeData();
        }
        
        /**
         * Large text response - should be compressed
         */
        @GetMapping(value = "/text", produces = MediaType.TEXT_PLAIN_VALUE)
        public String getLargeText() {
            return dataService.generateLargeText();
        }
        
        /**
         * Compression configuration info
         */
        @GetMapping("/compression/info")
        public Map<String, Object> getCompressionInfo() {
            return dataService.getCompressionStats();
        }
        
        /**
         * Custom large data with specified size
         */
        @GetMapping("/data/{sizeInKB}")
        public Map<String, Object> getCustomSizeData(@PathVariable int sizeInKB) {
            int itemCount = sizeInKB * 10; // Approximate
            
            List<String> items = IntStream.range(0, itemCount)
                .mapToObj(i -> "Data item " + i + ": Sample content for compression testing")
                .collect(Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("requestedSize", sizeInKB + "KB");
            data.put("itemCount", items.size());
            data.put("items", items);
            data.put("compressed", sizeInKB >= 2);
            data.put("timestamp", LocalDateTime.now());
            
            return data;
        }
        
        /**
         * Test endpoint to check compression headers
         */
        @GetMapping("/compression/test")
        public Map<String, Object> testCompression() {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Check response headers for Content-Encoding: gzip");
            response.put("instructions", Arrays.asList(
                "1. Send request with header: Accept-Encoding: gzip",
                "2. Response will include Content-Encoding: gzip if compressed",
                "3. Large responses (>2KB) will be compressed",
                "4. Small responses may not be compressed"
            ));
            response.put("testEndpoints", Map.of(
                "small", "/api/small (< 2KB, not compressed)",
                "large", "/api/large (> 2KB, compressed)",
                "text", "/api/text (> 2KB, compressed)",
                "custom", "/api/data/{sizeInKB}"
            ));
            response.put("timestamp", LocalDateTime.now());
            
            // Add padding to ensure this response is large enough to compress
            List<String> padding = IntStream.range(0, 200)
                .mapToObj(i -> "Padding line " + i)
                .collect(Collectors.toList());
            response.put("padding", padding);
            
            return response;
        }
    }
}
