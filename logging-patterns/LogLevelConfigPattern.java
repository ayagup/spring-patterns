package com.example.loglevelconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Log Level Configuration Pattern
 * 
 * Demonstrates dynamic runtime configuration of logging levels
 * without application restart.
 * 
 * Features:
 * - Runtime log level changes
 * - Package/class-specific configuration
 * - Temporary debug mode
 * - LoggingSystem integration
 * 
 * Use Cases:
 * - Production debugging
 * - Performance tuning
 * - Troubleshooting specific components
 * - Reducing log volume
 */
@SpringBootApplication
public class LogLevelConfigPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(LogLevelConfigPattern.class, args);
    }
}

/**
 * Service for dynamic log level configuration
 */
@Service
class LogLevelConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(LogLevelConfigService.class);
    private final LoggingSystem loggingSystem;
    private final Map<String, String> originalLevels = new HashMap<>();
    
    public LogLevelConfigService(LoggingSystem loggingSystem) {
        this.loggingSystem = loggingSystem;
    }
    
    /**
     * Set log level for specific logger
     */
    public void setLogLevel(String loggerName, String level) {
        // Store original level if not already stored
        if (!originalLevels.containsKey(loggerName)) {
            LogLevel current = loggingSystem.getLoggerConfiguration(loggerName).getEffectiveLevel();
            originalLevels.put(loggerName, current != null ? current.name() : "INFO");
        }
        
        LogLevel logLevel = LogLevel.valueOf(level.toUpperCase());
        loggingSystem.setLogLevel(loggerName, logLevel);
        
        logger.info("Log level changed: {} -> {}", loggerName, level);
    }
    
    /**
     * Reset logger to original level
     */
    public void resetLogLevel(String loggerName) {
        String originalLevel = originalLevels.get(loggerName);
        if (originalLevel != null) {
            loggingSystem.setLogLevel(loggerName, LogLevel.valueOf(originalLevel));
            logger.info("Log level reset: {} -> {}", loggerName, originalLevel);
        }
    }
    
    /**
     * Set log level for entire package
     */
    public void setPackageLogLevel(String packageName, String level) {
        setLogLevel(packageName, level);
        logger.info("Package log level set: {} -> {}", packageName, level);
    }
    
    /**
     * Enable debug mode for specific duration
     */
    public void enableDebugMode(String loggerName, long durationSeconds) {
        setLogLevel(loggerName, "DEBUG");
        
        // Schedule reset after duration
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                resetLogLevel(loggerName);
                logger.info("Debug mode expired for: {}", loggerName);
            }
        }, durationSeconds * 1000);
        
        logger.info("Debug mode enabled for {} seconds: {}", durationSeconds, loggerName);
    }
    
    /**
     * Get current log level for logger
     */
    public String getCurrentLevel(String loggerName) {
        LogLevel level = loggingSystem.getLoggerConfiguration(loggerName).getEffectiveLevel();
        return level != null ? level.name() : "UNKNOWN";
    }
    
    /**
     * Get all configured loggers
     */
    public Map<String, String> getAllConfiguredLoggers() {
        Map<String, String> loggers = new HashMap<>();
        originalLevels.forEach((name, level) -> {
            String currentLevel = getCurrentLevel(name);
            loggers.put(name, currentLevel);
        });
        return loggers;
    }
}

/**
 * Service to demonstrate different log levels
 */
@Service
class LogLevelDemoService {
    
    private static final Logger logger = LoggerFactory.getLogger(LogLevelDemoService.class);
    
    public void demonstrateLogLevels() {
        logger.trace("TRACE: Most detailed information");
        logger.debug("DEBUG: Detailed debugging information");
        logger.info("INFO: General informational messages");
        logger.warn("WARN: Warning messages");
        logger.error("ERROR: Error messages");
    }
    
    public void performOperation(String operation) {
        logger.debug("Starting operation: {}", operation);
        logger.info("Executing: {}", operation);
        logger.debug("Operation completed: {}", operation);
    }
}

/**
 * REST Controller for log level management
 */
@RestController
@RequestMapping("/api/log-config")
class LogLevelConfigController {
    
    private final LogLevelConfigService configService;
    private final LogLevelDemoService demoService;
    
    public LogLevelConfigController(LogLevelConfigService configService, 
                                   LogLevelDemoService demoService) {
        this.configService = configService;
        this.demoService = demoService;
    }
    
    /**
     * Set log level for specific logger
     */
    @PostMapping("/set-level")
    public Map<String, String> setLogLevel(@RequestBody Map<String, String> request) {
        String loggerName = request.get("logger");
        String level = request.get("level");
        
        configService.setLogLevel(loggerName, level);
        
        return Map.of(
            "logger", loggerName,
            "level", level,
            "status", "updated"
        );
    }
    
    /**
     * Reset logger to original level
     */
    @PostMapping("/reset-level")
    public Map<String, String> resetLogLevel(@RequestParam String logger) {
        configService.resetLogLevel(logger);
        return Map.of("status", "reset", "logger", logger);
    }
    
    /**
     * Enable temporary debug mode
     */
    @PostMapping("/debug-mode")
    public Map<String, Object> enableDebugMode(@RequestBody Map<String, Object> request) {
        String logger = (String) request.get("logger");
        long duration = ((Number) request.getOrDefault("durationSeconds", 60)).longValue();
        
        configService.enableDebugMode(logger, duration);
        
        return Map.of(
            "logger", logger,
            "duration", duration,
            "status", "debug mode enabled"
        );
    }
    
    /**
     * Get current log level
     */
    @GetMapping("/current-level")
    public Map<String, String> getCurrentLevel(@RequestParam String logger) {
        String level = configService.getCurrentLevel(logger);
        return Map.of("logger", logger, "level", level);
    }
    
    /**
     * Get all configured loggers
     */
    @GetMapping("/all-loggers")
    public Map<String, Map<String, String>> getAllLoggers() {
        return Map.of("loggers", configService.getAllConfiguredLoggers());
    }
    
    /**
     * Demonstrate all log levels
     */
    @GetMapping("/demo")
    public Map<String, String> demonstrateLogLevels() {
        demoService.demonstrateLogLevels();
        return Map.of("status", "demonstrated");
    }
}
