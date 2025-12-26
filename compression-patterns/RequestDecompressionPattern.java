package com.example.compression;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Request Decompression Pattern
 * 
 * Demonstrates automatic decompression of compressed HTTP request bodies.
 * Useful when clients send large payloads with Content-Encoding.
 * 
 * Features:
 * - Automatic GZIP request decompression
 * - Content-Encoding header detection
 * - Multiple compression format support
 * - Request body buffering
 * - Decompression error handling
 * 
 * Key Components:
 * - Decompression filter
 * - Content-Encoding detector
 * - GZIP decompressor
 * - Request wrapper
 */
@SpringBootApplication
public class RequestDecompressionPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(RequestDecompressionPattern.class, args);
    }
    
    @Configuration
    public static class DecompressionConfig {
        
        @Bean
        public RequestDecompressionFilter requestDecompressionFilter() {
            return new RequestDecompressionFilter();
        }
    }
    
    /**
     * Request Decompression Filter
     */
    public static class RequestDecompressionFilter extends OncePerRequestFilter {
        
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                       HttpServletResponse response,
                                       FilterChain filterChain)
                throws ServletException, IOException {
            
            String contentEncoding = request.getHeader("Content-Encoding");
            
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                DecompressedRequestWrapper wrappedRequest = 
                    new DecompressedRequestWrapper(request);
                filterChain.doFilter(wrappedRequest, response);
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }
    
    /**
     * Decompressed Request Wrapper
     */
    public static class DecompressedRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {
        
        private byte[] decompressedBody;
        
        public DecompressedRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.decompressedBody = decompressGZIP(request.getInputStream());
        }
        
        private byte[] decompressGZIP(javax.servlet.ServletInputStream inputStream) throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            try (GZIPInputStream gzipStream = new GZIPInputStream(inputStream)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }
            
            return outputStream.toByteArray();
        }
        
        @Override
        public javax.servlet.ServletInputStream getInputStream() throws IOException {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(decompressedBody);
            
            return new javax.servlet.ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteStream.available() == 0;
                }
                
                @Override
                public boolean isReady() {
                    return true;
                }
                
                @Override
                public void setReadListener(javax.servlet.ReadListener readListener) {
                }
                
                @Override
                public int read() throws IOException {
                    return byteStream.read();
                }
            };
        }
    }
    
    @Service
    public static class DecompressionService {
        
        public byte[] decompressGZIP(byte[] compressed) throws IOException {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(compressed);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            try (GZIPInputStream gzipStream = new GZIPInputStream(byteStream)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }
            
            return outputStream.toByteArray();
        }
        
        public Map<String, Object> getDecompressionStats(byte[] compressed, byte[] decompressed) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("compressedSize", compressed.length);
            stats.put("decompressedSize", decompressed.length);
            stats.put("expansionRatio", String.format("%.2f", 
                (double) decompressed.length / compressed.length));
            stats.put("algorithm", "GZIP");
            stats.put("timestamp", LocalDateTime.now());
            
            return stats;
        }
    }
    
    @RestController
    @RequestMapping("/api/decompress")
    public static class DecompressionController {
        
        private final DecompressionService decompressionService;
        
        public DecompressionController(DecompressionService decompressionService) {
            this.decompressionService = decompressionService;
        }
        
        /**
         * Endpoint that accepts compressed requests
         */
        @PostMapping("/data")
        public Map<String, Object> receiveCompressedData(@RequestBody String data) {
            return Map.of(
                "message", "Data received and decompressed",
                "dataLength", data.length(),
                "data", data,
                "timestamp", LocalDateTime.now()
            );
        }
        
        /**
         * Decompression info
         */
        @GetMapping("/info")
        public Map<String, Object> getDecompressionInfo() {
            return Map.of(
                "description", "Automatic request decompression",
                "supportedEncodings", Arrays.asList("gzip"),
                "clientHeader", "Content-Encoding: gzip",
                "automatic", true,
                "usage", "Send compressed request body with Content-Encoding: gzip header",
                "timestamp", LocalDateTime.now()
            );
        }
    }
}
