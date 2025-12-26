package com.example.logaggregation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Log Aggregation Pattern
 * 
 * Demonstrates collecting and aggregating logs from multiple sources
 * for centralized processing and analysis.
 * 
 * Use Cases:
 * - Microservices log collection
 * - Multi-instance application logging
 * - Distributed system log consolidation
 * - Real-time log analytics
 */
@SpringBootApplication
public class LogAggregationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(LogAggregationPattern.class, args);
    }
}

/**
 * Service for aggregating logs from multiple sources
 */
@Service
class LogAggregationService {
    
    private static final Logger logger = LoggerFactory.getLogger(LogAggregationService.class);
    private final Queue<LogEntry> logBuffer = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    public LogAggregationService() {
        // Schedule periodic log aggregation every 10 seconds
        scheduler.scheduleAtFixedRate(this::aggregateAndFlush, 10, 10, TimeUnit.SECONDS);
    }
    
    /**
     * Add log entry to aggregation buffer
     */
    public void addLog(LogEntry entry) {
        logBuffer.offer(entry);
        logger.debug("Log entry buffered: {} from source: {}", entry.getMessage(), entry.getSource());
    }
    
    /**
     * Aggregate and flush logs to central storage
     */
    public void aggregateAndFlush() {
        if (logBuffer.isEmpty()) {
            return;
        }
        
        List<LogEntry> batch = new ArrayList<>();
        LogEntry entry;
        while ((entry = logBuffer.poll()) != null) {
            batch.add(entry);
        }
        
        logger.info("Aggregating {} log entries", batch.size());
        
        // Group logs by source
        Map<String, List<LogEntry>> logsBySource = new HashMap<>();
        for (LogEntry logEntry : batch) {
            logsBySource.computeIfAbsent(logEntry.getSource(), k -> new ArrayList<>()).add(logEntry);
        }
        
        // Process aggregated logs
        logsBySource.forEach((source, logs) -> {
            logger.info("Source: {} | Logs: {} | Errors: {}", 
                source, 
                logs.size(),
                logs.stream().filter(l -> "ERROR".equals(l.getLevel())).count());
        });
        
        // In production: send to Elasticsearch, Splunk, CloudWatch, etc.
        sendToExternalSystem(batch);
    }
    
    /**
     * Send aggregated logs to external system
     */
    private void sendToExternalSystem(List<LogEntry> logs) {
        logger.info("Sending {} aggregated logs to external system", logs.size());
        // Mock implementation - would integrate with ELK stack, Splunk, etc.
    }
    
    /**
     * Get current buffer size
     */
    public int getBufferSize() {
        return logBuffer.size();
    }
}

/**
 * Log entry model
 */
class LogEntry {
    private String source;
    private String level;
    private String message;
    private String timestamp;
    private Map<String, Object> metadata;
    
    public LogEntry() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.metadata = new HashMap<>();
    }
    
    public LogEntry(String source, String level, String message) {
        this();
        this.source = source;
        this.level = level;
        this.message = message;
    }
    
    // Getters and setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}

/**
 * REST Controller demonstrating log aggregation
 */
@RestController
@RequestMapping("/api/logs")
class LogAggregationController {
    
    private final LogAggregationService aggregationService;
    
    public LogAggregationController(LogAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }
    
    /**
     * Submit log entry for aggregation
     */
    @PostMapping("/submit")
    public Map<String, Object> submitLog(@RequestBody LogEntry logEntry) {
        aggregationService.addLog(logEntry);
        return Map.of(
            "status", "buffered",
            "bufferSize", aggregationService.getBufferSize()
        );
    }
    
    /**
     * Submit multiple log entries
     */
    @PostMapping("/submit-batch")
    public Map<String, Object> submitBatch(@RequestBody List<LogEntry> logs) {
        logs.forEach(aggregationService::addLog);
        return Map.of(
            "status", "buffered",
            "count", logs.size(),
            "bufferSize", aggregationService.getBufferSize()
        );
    }
    
    /**
     * Trigger immediate aggregation
     */
    @PostMapping("/aggregate-now")
    public Map<String, String> aggregateNow() {
        aggregationService.aggregateAndFlush();
        return Map.of("status", "aggregated");
    }
    
    /**
     * Get buffer status
     */
    @GetMapping("/buffer-status")
    public Map<String, Object> getBufferStatus() {
        return Map.of(
            "bufferSize", aggregationService.getBufferSize(),
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
