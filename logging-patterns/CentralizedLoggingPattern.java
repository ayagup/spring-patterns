package com.example.centralizedlogging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Centralized Logging Pattern
 * 
 * Demonstrates integration with centralized logging systems
 * like ELK Stack (Elasticsearch, Logstash, Kibana), Splunk, or CloudWatch.
 * 
 * Configuration typically done in logback-spring.xml:
 * - Logstash encoder for JSON formatting
 * - TCP/UDP appenders for log shipping
 * - Structured logging with metadata
 * 
 * Use Cases:
 * - Cloud-native applications
 * - Microservices architectures
 * - Compliance and audit requirements
 * - Real-time log analysis and monitoring
 */
@SpringBootApplication
public class CentralizedLoggingPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(CentralizedLoggingPattern.class, args);
    }
}

/**
 * Service for centralized logging operations
 */
@Service
class CentralizedLoggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(CentralizedLoggingService.class);
    
    /**
     * Log with structured metadata for centralized systems
     */
    public void logWithMetadata(String message, Map<String, Object> metadata) {
        // Add metadata to MDC for structured logging
        metadata.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
        
        try {
            logger.info("CENTRAL: {}", message);
        } finally {
            // Clear MDC to avoid memory leaks
            metadata.keySet().forEach(MDC::remove);
        }
    }
    
    /**
     * Log business event with context
     */
    public void logBusinessEvent(String eventType, String description, Map<String, Object> context) {
        MDC.put("event.type", eventType);
        MDC.put("event.timestamp", LocalDateTime.now().toString());
        
        context.forEach((key, value) -> MDC.put("event." + key, String.valueOf(value)));
        
        try {
            logger.info("Business Event: {}", description);
        } finally {
            MDC.remove("event.type");
            MDC.remove("event.timestamp");
            context.keySet().forEach(key -> MDC.remove("event." + key));
        }
    }
    
    /**
     * Log application metric for monitoring
     */
    public void logMetric(String metricName, double value, Map<String, String> tags) {
        MDC.put("metric.name", metricName);
        MDC.put("metric.value", String.valueOf(value));
        MDC.put("metric.timestamp", String.valueOf(System.currentTimeMillis()));
        
        tags.forEach((key, val) -> MDC.put("tag." + key, val));
        
        try {
            logger.info("Metric: {} = {}", metricName, value);
        } finally {
            MDC.remove("metric.name");
            MDC.remove("metric.value");
            MDC.remove("metric.timestamp");
            tags.keySet().forEach(key -> MDC.remove("tag." + key));
        }
    }
    
    /**
     * Log error with full context for debugging
     */
    public void logErrorWithContext(String errorMessage, Exception exception, Map<String, Object> context) {
        MDC.put("error.type", exception.getClass().getSimpleName());
        MDC.put("error.message", exception.getMessage());
        
        context.forEach((key, value) -> MDC.put("context." + key, String.valueOf(value)));
        
        try {
            logger.error("Error: {} | Exception: {}", errorMessage, exception.getMessage(), exception);
        } finally {
            MDC.remove("error.type");
            MDC.remove("error.message");
            context.keySet().forEach(key -> MDC.remove("context." + key));
        }
    }
    
    /**
     * Log audit trail event
     */
    public void logAuditEvent(String userId, String action, String resource, String result) {
        MDC.put("audit.userId", userId);
        MDC.put("audit.action", action);
        MDC.put("audit.resource", resource);
        MDC.put("audit.result", result);
        MDC.put("audit.timestamp", LocalDateTime.now().toString());
        
        try {
            logger.info("AUDIT: User {} performed {} on {} with result {}", 
                userId, action, resource, result);
        } finally {
            MDC.remove("audit.userId");
            MDC.remove("audit.action");
            MDC.remove("audit.resource");
            MDC.remove("audit.result");
            MDC.remove("audit.timestamp");
        }
    }
}

/**
 * REST Controller demonstrating centralized logging
 */
@RestController
@RequestMapping("/api/central-logging")
class CentralizedLoggingController {
    
    private final CentralizedLoggingService loggingService;
    
    public CentralizedLoggingController(CentralizedLoggingService loggingService) {
        this.loggingService = loggingService;
    }
    
    /**
     * Log with metadata
     */
    @PostMapping("/log-metadata")
    public Map<String, String> logWithMetadata(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", Map.of());
        
        loggingService.logWithMetadata(message, metadata);
        return Map.of("status", "logged");
    }
    
    /**
     * Log business event
     */
    @PostMapping("/business-event")
    public Map<String, String> logBusinessEvent(@RequestBody Map<String, Object> request) {
        String eventType = (String) request.get("eventType");
        String description = (String) request.get("description");
        Map<String, Object> context = (Map<String, Object>) request.getOrDefault("context", Map.of());
        
        loggingService.logBusinessEvent(eventType, description, context);
        return Map.of("status", "event logged");
    }
    
    /**
     * Log metric
     */
    @PostMapping("/metric")
    public Map<String, String> logMetric(@RequestBody Map<String, Object> request) {
        String metricName = (String) request.get("name");
        double value = ((Number) request.get("value")).doubleValue();
        Map<String, String> tags = (Map<String, String>) request.getOrDefault("tags", Map.of());
        
        loggingService.logMetric(metricName, value, tags);
        return Map.of("status", "metric logged");
    }
    
    /**
     * Log audit event
     */
    @PostMapping("/audit")
    public Map<String, String> logAudit(@RequestBody Map<String, String> request) {
        loggingService.logAuditEvent(
            request.get("userId"),
            request.get("action"),
            request.get("resource"),
            request.get("result")
        );
        return Map.of("status", "audit logged");
    }
}
