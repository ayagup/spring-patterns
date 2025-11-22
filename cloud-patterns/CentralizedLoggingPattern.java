package com.example.cloud.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized Logging Pattern - Demonstrates Structured Logging and Aggregation
 * 
 * This pattern shows how to:
 * 1. Implement structured logging with JSON format
 * 2. Use MDC (Mapped Diagnostic Context) for correlation
 * 3. Implement log aggregation patterns
 * 4. Integrate with ELK stack (Elasticsearch, Logstash, Kibana)
 * 5. Add contextual information to logs
 * 6. Implement log levels and filtering
 * 7. Track request flows across services
 * 8. Implement async logging
 * 9. Add business metrics to logs
 * 10. Monitor log patterns
 * 
 * Key Concepts:
 * - Structured Logging: JSON/key-value format
 * - MDC: Thread-local context for correlation
 * - Correlation ID: Track requests across services
 * - Log Aggregation: Collect logs from all services
 * - Centralized View: Single place to search all logs
 * 
 * Log Levels:
 * - TRACE: Very detailed debugging
 * - DEBUG: Detailed debugging
 * - INFO: General information
 * - WARN: Warning messages
 * - ERROR: Error messages
 * - FATAL: Critical failures
 * 
 * Dependencies:
 * - spring-boot-starter-logging (Logback)
 * - logstash-logback-encoder
 * - spring-cloud-starter-sleuth (for trace context)
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class CentralizedLoggingPattern {
    
    private static final Logger logger = LoggerFactory.getLogger(CentralizedLoggingPattern.class);

    public static void main(String[] args) {
        SpringApplication.run(CentralizedLoggingPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("CENTRALIZED LOGGING PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateStructuredLogging();
        demonstrateMDC();
        demonstrateELKIntegration();
        
        System.out.println("\nApplication running with centralized logging");
        System.out.println("Logs available at: /var/log/application.log");
        System.out.println("Kibana UI: http://localhost:5601");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateStructuredLogging() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("STRUCTURED LOGGING");
        System.out.println("=".repeat(80));
        
        System.out.println("\nJSON Log Format:");
        System.out.println("{");
        System.out.println("  \"timestamp\": \"2024-01-01T12:00:00\",");
        System.out.println("  \"level\": \"INFO\",");
        System.out.println("  \"logger\": \"com.example.UserService\",");
        System.out.println("  \"message\": \"User created\",");
        System.out.println("  \"userId\": \"12345\",");
        System.out.println("  \"traceId\": \"abc123\",");
        System.out.println("  \"spanId\": \"xyz789\"");
        System.out.println("}");
    }
    
    private static void demonstrateMDC() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("MAPPED DIAGNOSTIC CONTEXT (MDC)");
        System.out.println("=".repeat(80));
        
        System.out.println("\nMDC.put(\"userId\", \"12345\");");
        System.out.println("MDC.put(\"requestId\", UUID.randomUUID().toString());");
        System.out.println("logger.info(\"Processing request\"); // Includes MDC context");
        System.out.println("MDC.clear(); // Clean up");
    }
    
    private static void demonstrateELKIntegration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ELK STACK INTEGRATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Logback → Logstash (JSON encoder)");
        System.out.println("2. Logstash → Elasticsearch (indexing)");
        System.out.println("3. Kibana → Elasticsearch (visualization)");
    }
}

/**
 * Log Entry Model
 */
class LogEntry {
    private LocalDateTime timestamp;
    private String level;
    private String logger;
    private String message;
    private String traceId;
    private String spanId;
    private String serviceName;
    private Map<String, Object> context;
    private Throwable exception;
    
    public LogEntry(String level, String logger, String message, String serviceName) {
        this.timestamp = LocalDateTime.now();
        this.level = level;
        this.logger = logger;
        this.message = message;
        this.serviceName = serviceName;
        this.context = new HashMap<>();
    }
    
    // Getters and setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getLogger() { return logger; }
    public String getMessage() { return message; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }
    public String getServiceName() { return serviceName; }
    public Map<String, Object> getContext() { return context; }
    public void addContext(String key, Object value) { context.put(key, value); }
    public Throwable getException() { return exception; }
    public void setException(Throwable exception) { this.exception = exception; }
}

/**
 * Structured Logger
 */
class StructuredLogger {
    private final Logger logger;
    private final String serviceName;
    
    public StructuredLogger(Class<?> clazz, String serviceName) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.serviceName = serviceName;
    }
    
    public void info(String message, Map<String, Object> context) {
        LogEntry entry = createLogEntry("INFO", message, context);
        logger.info(formatLogEntry(entry));
    }
    
    public void warn(String message, Map<String, Object> context) {
        LogEntry entry = createLogEntry("WARN", message, context);
        logger.warn(formatLogEntry(entry));
    }
    
    public void error(String message, Throwable throwable, Map<String, Object> context) {
        LogEntry entry = createLogEntry("ERROR", message, context);
        entry.setException(throwable);
        logger.error(formatLogEntry(entry), throwable);
    }
    
    public void debug(String message, Map<String, Object> context) {
        if (logger.isDebugEnabled()) {
            LogEntry entry = createLogEntry("DEBUG", message, context);
            logger.debug(formatLogEntry(entry));
        }
    }
    
    private LogEntry createLogEntry(String level, String message, Map<String, Object> context) {
        LogEntry entry = new LogEntry(level, logger.getName(), message, serviceName);
        
        // Add MDC context
        String traceId = MDC.get("traceId");
        String spanId = MDC.get("spanId");
        if (traceId != null) entry.setTraceId(traceId);
        if (spanId != null) entry.setSpanId(spanId);
        
        // Add custom context
        if (context != null) {
            context.forEach(entry::addContext);
        }
        
        return entry;
    }
    
    private String formatLogEntry(LogEntry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"timestamp\":\"").append(entry.getTimestamp()).append("\",");
        sb.append("\"level\":\"").append(entry.getLevel()).append("\",");
        sb.append("\"service\":\"").append(entry.getServiceName()).append("\",");
        sb.append("\"logger\":\"").append(entry.getLogger()).append("\",");
        sb.append("\"message\":\"").append(entry.getMessage()).append("\"");
        
        if (entry.getTraceId() != null) {
            sb.append(",\"traceId\":\"").append(entry.getTraceId()).append("\"");
        }
        if (entry.getSpanId() != null) {
            sb.append(",\"spanId\":\"").append(entry.getSpanId()).append("\"");
        }
        
        if (!entry.getContext().isEmpty()) {
            entry.getContext().forEach((key, value) -> 
                sb.append(",\"").append(key).append("\":\"").append(value).append("\""));
        }
        
        sb.append("}");
        return sb.toString();
    }
}

/**
 * MDC (Mapped Diagnostic Context) Manager
 */
@Service
class MDCManager {
    
    private static final String TRACE_ID = "traceId";
    private static final String SPAN_ID = "spanId";
    private static final String USER_ID = "userId";
    private static final String REQUEST_ID = "requestId";
    private static final String SESSION_ID = "sessionId";
    
    public void setTraceContext(String traceId, String spanId) {
        MDC.put(TRACE_ID, traceId);
        MDC.put(SPAN_ID, spanId);
    }
    
    public void setUserContext(String userId) {
        MDC.put(USER_ID, userId);
    }
    
    public void setRequestContext(String requestId) {
        MDC.put(REQUEST_ID, requestId);
    }
    
    public void setSessionContext(String sessionId) {
        MDC.put(SESSION_ID, sessionId);
    }
    
    public Map<String, String> getContext() {
        Map<String, String> context = new HashMap<>();
        MDC.getCopyOfContextMap().forEach(context::put);
        return context;
    }
    
    public void clear() {
        MDC.clear();
    }
    
    public void clearTrace() {
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
    }
}

/**
 * Log Aggregator (In-Memory for demo)
 */
@Service
class LogAggregator {
    
    private final List<LogEntry> logs = new ArrayList<>();
    private final Map<String, List<LogEntry>> logsByService = new ConcurrentHashMap<>();
    private final Map<String, List<LogEntry>> logsByTrace = new ConcurrentHashMap<>();
    
    public void aggregate(LogEntry entry) {
        logs.add(entry);
        
        // Index by service
        logsByService.computeIfAbsent(entry.getServiceName(), k -> new ArrayList<>())
            .add(entry);
        
        // Index by trace ID
        if (entry.getTraceId() != null) {
            logsByTrace.computeIfAbsent(entry.getTraceId(), k -> new ArrayList<>())
                .add(entry);
        }
    }
    
    public List<LogEntry> getLogsByService(String serviceName) {
        return new ArrayList<>(logsByService.getOrDefault(serviceName, new ArrayList<>()));
    }
    
    public List<LogEntry> getLogsByTrace(String traceId) {
        return new ArrayList<>(logsByTrace.getOrDefault(traceId, new ArrayList<>()));
    }
    
    public List<LogEntry> getLogsByLevel(String level) {
        return logs.stream()
            .filter(log -> log.getLevel().equals(level))
            .toList();
    }
    
    public List<LogEntry> getRecentLogs(int limit) {
        int fromIndex = Math.max(0, logs.size() - limit);
        return new ArrayList<>(logs.subList(fromIndex, logs.size()));
    }
    
    public Map<String, Long> getLogCountsByLevel() {
        Map<String, Long> counts = new HashMap<>();
        logs.forEach(log -> 
            counts.merge(log.getLevel(), 1L, Long::sum));
        return counts;
    }
}

/**
 * Demo Service with Structured Logging
 */
@Service
class UserLoggingService {
    
    private final StructuredLogger logger;
    private final MDCManager mdcManager;
    private final LogAggregator logAggregator;
    
    public UserLoggingService(MDCManager mdcManager, LogAggregator logAggregator) {
        this.logger = new StructuredLogger(UserLoggingService.class, "user-service");
        this.mdcManager = mdcManager;
        this.logAggregator = logAggregator;
    }
    
    public void createUser(String userId, String username) {
        try {
            // Set MDC context
            mdcManager.setUserContext(userId);
            mdcManager.setRequestContext(UUID.randomUUID().toString());
            
            // Log with context
            Map<String, Object> context = new HashMap<>();
            context.put("userId", userId);
            context.put("username", username);
            context.put("action", "create_user");
            
            logger.info("Creating user", context);
            
            // Simulate user creation
            processUserCreation(userId, username);
            
            logger.info("User created successfully", context);
            
        } catch (Exception e) {
            Map<String, Object> errorContext = Map.of(
                "userId", userId,
                "error", e.getMessage()
            );
            logger.error("Failed to create user", e, errorContext);
            throw e;
        } finally {
            mdcManager.clear();
        }
    }
    
    private void processUserCreation(String userId, String username) {
        Map<String, Object> context = Map.of(
            "userId", userId,
            "step", "validation"
        );
        logger.debug("Validating user data", context);
        
        // Simulate processing
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        context = Map.of(
            "userId", userId,
            "step", "persistence"
        );
        logger.debug("Persisting user data", context);
    }
}

/**
 * REST Controller demonstrating centralized logging
 */
@RestController
@RequestMapping("/api/logging")
class LoggingController {
    
    private final UserLoggingService userService;
    private final LogAggregator logAggregator;
    private final MDCManager mdcManager;
    
    public LoggingController(UserLoggingService userService,
                            LogAggregator logAggregator,
                            MDCManager mdcManager) {
        this.userService = userService;
        this.logAggregator = logAggregator;
        this.mdcManager = mdcManager;
    }
    
    @PostMapping("/users")
    public Map<String, Object> createUser(
            @RequestParam String userId,
            @RequestParam String username) {
        
        userService.createUser(userId, username);
        
        return Map.of(
            "userId", userId,
            "username", username,
            "status", "created"
        );
    }
    
    @GetMapping("/logs/service/{serviceName}")
    public List<LogEntry> getLogsByService(@PathVariable String serviceName) {
        return logAggregator.getLogsByService(serviceName);
    }
    
    @GetMapping("/logs/trace/{traceId}")
    public List<LogEntry> getLogsByTrace(@PathVariable String traceId) {
        return logAggregator.getLogsByTrace(traceId);
    }
    
    @GetMapping("/logs/level/{level}")
    public List<LogEntry> getLogsByLevel(@PathVariable String level) {
        return logAggregator.getLogsByLevel(level);
    }
    
    @GetMapping("/logs/recent")
    public List<LogEntry> getRecentLogs(@RequestParam(defaultValue = "100") int limit) {
        return logAggregator.getRecentLogs(limit);
    }
    
    @GetMapping("/logs/stats")
    public Map<String, Object> getLogStats() {
        return Map.of(
            "countsByLevel", logAggregator.getLogCountsByLevel(),
            "mdcContext", mdcManager.getContext()
        );
    }
}
