package com.example.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

/**
 * Log Correlation Pattern
 * 
 * Demonstrates correlating logs across multiple services/components
 * using correlation IDs.
 * 
 * Features:
 * - Correlation ID generation
 * - Cross-service tracking
 * - Request tracing
 * - Distributed logging
 */
@SpringBootApplication
public class LogCorrelationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(LogCorrelationPattern.class, args);
    }
    
    @org.springframework.stereotype.Service
    public static class CorrelationService {
        
        private static final Logger logger = LoggerFactory.getLogger(CorrelationService.class);
        private static final String CORRELATION_ID = "correlationId";
        
        public String getCorrelationId() {
            String correlationId = MDC.get(CORRELATION_ID);
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
                MDC.put(CORRELATION_ID, correlationId);
            }
            return correlationId;
        }
        
        public void setCorrelationId(String correlationId) {
            MDC.put(CORRELATION_ID, correlationId);
        }
        
        public void clearCorrelationId() {
            MDC.remove(CORRELATION_ID);
        }
        
        public void logWithCorrelation(String message) {
            logger.info("[{}] {}", getCorrelationId(), message);
        }
    }
    
    @RestController
    @RequestMapping("/api")
    public static class CorrelationController {
        
        private static final Logger logger = LoggerFactory.getLogger(CorrelationController.class);
        private final CorrelationService correlationService;
        
        public CorrelationController(CorrelationService correlationService) {
            this.correlationService = correlationService;
        }
        
        @GetMapping("/correlated")
        public Map<String, String> correlatedEndpoint() {
            String correlationId = correlationService.getCorrelationId();
            
            logger.info("Processing request");
            correlationService.logWithCorrelation("Step 1");
            correlationService.logWithCorrelation("Step 2");
            
            return Map.of(
                "message", "Request processed",
                "correlationId", correlationId
            );
        }
    }
}
