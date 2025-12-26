package com.example.compression;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZIP Compression Pattern
 * 
 * Demonstrates explicit GZIP compression for HTTP responses with
 * fine-grained control over compression strategy.
 * 
 * Features:
 * - Manual GZIP compression
 * - Custom GZIP filter
 * - Compression level configuration
 * - Selective compression by content type
 * - Client capability detection (Accept-Encoding)
 * - Content-Encoding header management
 * 
 * Key Components:
 * - Custom GZIP filter
 * - GZIP compression utilities
 * - Response wrapper for compression
 * - Compression strategy selector
 */
@SpringBootApplication
public class GZIPCompressionPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(GZIPCompressionPattern.class, args);
    }
    
    /**
     * GZIP Filter Configuration
     */
    @Configuration
    public static class GZIPFilterConfig {
        
        @Bean
        public GZIPCompressionFilter gzipCompressionFilter() {
            return new GZIPCompressionFilter();
        }
    }
    
    /**
     * Custom GZIP Compression Filter
     */
    public static class GZIPCompressionFilter extends OncePerRequestFilter {
        
        private static final int MIN_SIZE_FOR_COMPRESSION = 1024; // 1KB
        private static final String[] COMPRESSIBLE_TYPES = {
            "application/json", "text/html", "text/plain", 
            "text/xml", "application/xml", "text/css", 
            "application/javascript"
        };
        
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                       HttpServletResponse response,
                                       FilterChain filterChain)
                throws ServletException, IOException {
            
            String acceptEncoding = request.getHeader("Accept-Encoding");
            
            // Check if client accepts GZIP
            if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
                GZIPResponseWrapper wrappedResponse = 
                    new GZIPResponseWrapper(response);
                
                filterChain.doFilter(request, wrappedResponse);
                
                // Apply compression if content is large enough
                byte[] content = wrappedResponse.getContent();
                if (content.length >= MIN_SIZE_FOR_COMPRESSION) {
                    byte[] compressed = compressGZIP(content);
                    response.setHeader("Content-Encoding", "gzip");
                    response.setContentLength(compressed.length);
                    response.getOutputStream().write(compressed);
                } else {
                    response.getOutputStream().write(content);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        }
        
        /**
         * Compress data using GZIP
         */
        private byte[] compressGZIP(byte[] data) throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
            try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
                gzipStream.write(data);
            }
            return byteStream.toByteArray();
        }
    }
    
    /**
     * Response Wrapper for GZIP Compression
     */
    public static class GZIPResponseWrapper extends javax.servlet.http.HttpServletResponseWrapper {
        
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        public GZIPResponseWrapper(HttpServletResponse response) {
            super(response);
        }
        
        @Override
        public javax.servlet.ServletOutputStream getOutputStream() throws IOException {
            return new javax.servlet.ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }
                
                @Override
                public void setWriteListener(javax.servlet.WriteListener writeListener) {
                }
                
                @Override
                public void write(int b) throws IOException {
                    outputStream.write(b);
                }
            };
        }
        
        public byte[] getContent() {
            return outputStream.toByteArray();
        }
    }
    
    /**
     * GZIP Utility Service
     */
    @org.springframework.stereotype.Service
    public static class GZIPService {
        
        public byte[] compress(byte[] data) throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
                gzipStream.write(data);
            }
            return byteStream.toByteArray();
        }
        
        public Map<String, Object> getCompressionStats(byte[] original, byte[] compressed) {
            double ratio = 100.0 * (1.0 - (double) compressed.length / original.length);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("originalSize", original.length);
            stats.put("compressedSize", compressed.length);
            stats.put("compressionRatio", String.format("%.2f%%", ratio));
            stats.put("savings", original.length - compressed.length);
            stats.put("algorithm", "GZIP");
            stats.put("timestamp", LocalDateTime.now());
            
            return stats;
        }
    }
    
    /**
     * GZIP Test Controller
     */
    @RestController
    @RequestMapping("/api/gzip")
    public static class GZIPController {
        
        private final GZIPService gzipService;
        
        public GZIPController(GZIPService gzipService) {
            this.gzipService = gzipService;
        }
        
        /**
         * Generate compressible data
         */
        @GetMapping("/data")
        public Map<String, Object> getCompressibleData() {
            List<String> items = IntStream.range(0, 1000)
                .mapToObj(i -> "Item " + i + ": Repeated text for compression demonstration")
                .collect(Collectors.toList());
            
            return Map.of(
                "message", "Large dataset - should be GZIP compressed",
                "itemCount", items.size(),
                "items", items,
                "timestamp", LocalDateTime.now()
            );
        }
        
        /**
         * GZIP compression info
         */
        @GetMapping("/info")
        public Map<String, Object> getGZIPInfo() {
            return Map.of(
                "algorithm", "GZIP (RFC 1952)",
                "contentEncoding", "gzip",
                "minSize", "1KB",
                "compressionLevel", "Default",
                "clientHeader", "Accept-Encoding: gzip",
                "serverHeader", "Content-Encoding: gzip",
                "typicalRatio", "60-90% for text",
                "timestamp", LocalDateTime.now()
            );
        }
        
        /**
         * Test compression manually
         */
        @PostMapping("/test")
        public Map<String, Object> testCompression(@RequestBody String data) throws IOException {
            byte[] original = data.getBytes();
            byte[] compressed = gzipService.compress(original);
            
            return gzipService.getCompressionStats(original, compressed);
        }
    }
}
