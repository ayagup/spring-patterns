package com.example.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Structured Logging Pattern
 * 
 * Demonstrates structured logging using JSON format for better
 * log aggregation, searchability, and analysis.
 * 
 * Features:
 * - JSON-formatted logs
 * - Structured data fields
 * - Easy parsing and querying
 * - Machine-readable format
 * - Context preservation
 */
@SpringBootApplication
public class StructuredLoggingPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(StructuredLoggingPattern.class, args);
    }
    
    @org.springframework.stereotype.Service
    public static class StructuredLogger {
        
        private static final Logger logger = LoggerFactory.getLogger(StructuredLogger.class);
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        public void logStructured(String level, String message, Map<String, Object> context) {
            try {
                Map<String, Object> logEntry = new LinkedHashMap<>();
                logEntry.put("timestamp", LocalDateTime.now().toString());
                logEntry.put("level", level);
                logEntry.put("message", message);
                logEntry.put("context", context);
                
                String json = objectMapper.writeValueAsString(logEntry);
                
                switch (level.toUpperCase()) {
                    case "INFO":
                        logger.info(json);
                        break;
                    case "WARN":
                        logger.warn(json);
                        break;
                    case "ERROR":
                        logger.error(json);
                        break;
                    default:
                        logger.debug(json);
                }
            } catch (JsonProcessingException e) {
                logger.error("Failed to create structured log", e);
            }
        }
        
        public void logEvent(String eventType, String userId, String action, Object data) {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("eventType", eventType);
            context.put("userId", userId);
            context.put("action", action);
            context.put("data", data);
            
            logStructured("INFO", "Event logged", context);
        }
        
        public void logError(String errorCode, String message, Exception exception) {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("errorCode", errorCode);
            context.put("exception", exception.getClass().getSimpleName());
            context.put("stackTrace", Arrays.toString(exception.getStackTrace()));
            
            logStructured("ERROR", message, context);
        }
    }
    
    @RestController
    @RequestMapping("/api")
    public static class LoggingController {
        
        private final StructuredLogger structuredLogger;
        
        public LoggingController(StructuredLogger structuredLogger) {
            this.structuredLogger = structuredLogger;
        }
        
        @PostMapping("/event")
        public Map<String, String> logEvent(@RequestBody Map<String, Object> eventData) {
            structuredLogger.logEvent(
                (String) eventData.get("eventType"),
                (String) eventData.get("userId"),
                (String) eventData.get("action"),
                eventData.get("data")
            );
            
            return Map.of("message", "Event logged in structured format");
        }
        
        @GetMapping("/test-logs")
        public Map<String, String> testLogs() {
            structuredLogger.logStructured("INFO", "Test log entry", 
                Map.of("testId", "123", "result", "success"));
            
            structuredLogger.logEvent("USER_ACTION", "user123", "LOGIN", 
                Map.of("ip", "192.168.1.1", "userAgent", "Mozilla/5.0"));
            
            return Map.of("message", "Test logs generated");
        }
    }
}
