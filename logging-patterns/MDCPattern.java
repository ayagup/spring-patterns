package com.example.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

/**
 * MDC (Mapped Diagnostic Context) Pattern
 * 
 * Demonstrates using SLF4J MDC to add contextual information
 * to all log statements within a thread.
 * 
 * Features:
 * - Thread-local context storage
 * - Automatic context propagation
 * - Request ID tracking
 * - User context in logs
 */
@SpringBootApplication
public class MDCPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(MDCPattern.class, args);
    }
    
    @org.springframework.stereotype.Service
    public static class MDCService {
        
        private static final Logger logger = LoggerFactory.getLogger(MDCService.class);
        
        public void processWithContext(String userId, String requestId, String operation) {
            try {
                MDC.put("userId", userId);
                MDC.put("requestId", requestId);
                MDC.put("operation", operation);
                
                logger.info("Starting operation");
                performOperation();
                logger.info("Operation completed");
                
            } finally {
                MDC.clear();
            }
        }
        
        private void performOperation() {
            logger.debug("Executing business logic");
        }
    }
    
    @org.springframework.stereotype.Component
    public static class MDCFilter extends org.springframework.web.filter.OncePerRequestFilter {
        
        @Override
        protected void doFilterInternal(
                javax.servlet.http.HttpServletRequest request,
                javax.servlet.http.HttpServletResponse response,
                javax.servlet.FilterChain filterChain)
                throws java.io.IOException, javax.servlet.ServletException {
            
            try {
                MDC.put("requestId", UUID.randomUUID().toString());
                MDC.put("method", request.getMethod());
                MDC.put("uri", request.getRequestURI());
                
                filterChain.doFilter(request, response);
                
            } finally {
                MDC.clear();
            }
        }
    }
    
    @RestController
    @RequestMapping("/api")
    public static class MDCController {
        
        private static final Logger logger = LoggerFactory.getLogger(MDCController.class);
        private final MDCService mdcService;
        
        public MDCController(MDCService mdcService) {
            this.mdcService = mdcService;
        }
        
        @GetMapping("/process")
        public Map<String, String> process(@RequestParam String userId) {
            logger.info("Processing request for user: {}", userId);
            
            mdcService.processWithContext(
                userId, 
                MDC.get("requestId"), 
                "process-data"
            );
            
            return Map.of(
                "message", "Process completed",
                "requestId", MDC.get("requestId")
            );
        }
    }
}
