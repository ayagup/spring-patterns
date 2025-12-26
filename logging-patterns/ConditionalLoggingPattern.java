package com.example.conditionallogging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * Conditional Logging Pattern
 * 
 * Demonstrates selective logging based on conditions to:
 * - Reduce log volume
 * - Avoid expensive operations
 * - Improve performance
 * - Target specific scenarios
 * 
 * Techniques:
 * - Logger level checks (isDebugEnabled)
 * - Conditional expressions
 * - Lazy evaluation with Supplier
 * - Sampling/throttling
 * 
 * Use Cases:
 * - High-throughput applications
 * - Performance-critical code
 * - Production debugging
 * - Selective detail logging
 */
@SpringBootApplication
public class ConditionalLoggingPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(ConditionalLoggingPattern.class, args);
    }
}

/**
 * Service demonstrating conditional logging techniques
 */
@Service
class ConditionalLoggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConditionalLoggingService.class);
    private final Map<String, Long> lastLogTimes = new HashMap<>();
    private final Map<String, Integer> logCounters = new HashMap<>();
    
    /**
     * Log only if debug is enabled (avoids string concatenation overhead)
     */
    public void logIfDebugEnabled(String operation) {
        if (logger.isDebugEnabled()) {
            logger.debug("Debug info: {}", expensiveDataGeneration(operation));
        }
    }
    
    /**
     * Log with lazy evaluation using Supplier
     */
    public void logLazy(String message, Supplier<String> expensiveData) {
        if (logger.isDebugEnabled()) {
            logger.debug("{}: {}", message, expensiveData.get());
        }
    }
    
    /**
     * Log only on error condition
     */
    public void logOnError(boolean hasError, String operation) {
        if (hasError) {
            logger.error("Operation failed: {}", operation);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Operation succeeded: {}", operation);
        }
    }
    
    /**
     * Throttled logging - log at most once per interval
     */
    public void logThrottled(String key, String message, long intervalMillis) {
        long now = System.currentTimeMillis();
        Long lastTime = lastLogTimes.get(key);
        
        if (lastTime == null || (now - lastTime) >= intervalMillis) {
            logger.info("THROTTLED: {}", message);
            lastLogTimes.put(key, now);
        }
    }
    
    /**
     * Sampled logging - log every Nth occurrence
     */
    public void logSampled(String key, String message, int sampleRate) {
        int count = logCounters.getOrDefault(key, 0) + 1;
        logCounters.put(key, count);
        
        if (count % sampleRate == 0) {
            logger.info("SAMPLED (1/{}): {} [count: {}]", sampleRate, message, count);
        }
    }
    
    /**
     * Log based on value threshold
     */
    public void logIfThresholdExceeded(String metric, double value, double threshold) {
        if (value > threshold) {
            logger.warn("Threshold exceeded: {} = {} (threshold: {})", metric, value, threshold);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Metric within threshold: {} = {}", metric, value);
        }
    }
    
    /**
     * Log only for specific users/tenants (multi-tenancy)
     */
    public void logForSpecificUser(String userId, String message) {
        // Log verbose details only for test users
        if (userId.startsWith("test_") || userId.startsWith("admin_")) {
            logger.info("USER {}: {}", userId, message);
        }
    }
    
    /**
     * Log based on environment/profile
     */
    public void logInDevEnvironment(String message, boolean isDev) {
        if (isDev) {
            logger.debug("DEV: {}", message);
        }
    }
    
    /**
     * Conditional logging with multiple criteria
     */
    public void logComplex(boolean condition1, boolean condition2, String message) {
        if (condition1 && condition2 && logger.isDebugEnabled()) {
            logger.debug("Complex condition met: {}", message);
        } else if (condition1 || condition2) {
            logger.info("Partial condition: {}", message);
        }
    }
    
    /**
     * Expensive data generation (simulated)
     */
    private String expensiveDataGeneration(String operation) {
        // Simulate expensive operation (e.g., serialization, calculation)
        StringBuilder sb = new StringBuilder();
        sb.append("Operation: ").append(operation);
        sb.append(", Timestamp: ").append(System.currentTimeMillis());
        sb.append(", Details: ").append(generateDetails());
        return sb.toString();
    }
    
    private String generateDetails() {
        return "Complex calculation result: " + Math.random();
    }
}

/**
 * REST Controller demonstrating conditional logging
 */
@RestController
@RequestMapping("/api/conditional-logging")
class ConditionalLoggingController {
    
    private final ConditionalLoggingService loggingService;
    
    public ConditionalLoggingController(ConditionalLoggingService loggingService) {
        this.loggingService = loggingService;
    }
    
    /**
     * Test debug-enabled logging
     */
    @GetMapping("/debug-enabled")
    public Map<String, String> testDebugEnabled(@RequestParam String operation) {
        loggingService.logIfDebugEnabled(operation);
        return Map.of("status", "logged if debug enabled");
    }
    
    /**
     * Test lazy evaluation
     */
    @GetMapping("/lazy")
    public Map<String, String> testLazy(@RequestParam String message) {
        loggingService.logLazy(message, () -> "Expensive: " + UUID.randomUUID());
        return Map.of("status", "logged with lazy evaluation");
    }
    
    /**
     * Test error condition logging
     */
    @GetMapping("/error-condition")
    public Map<String, String> testErrorCondition(@RequestParam boolean hasError) {
        loggingService.logOnError(hasError, "test-operation");
        return Map.of("status", "logged based on error condition");
    }
    
    /**
     * Test throttled logging
     */
    @GetMapping("/throttled")
    public Map<String, String> testThrottled(@RequestParam(defaultValue = "test") String key) {
        loggingService.logThrottled(key, "Throttled message for " + key, 5000);
        return Map.of("status", "throttled logging executed");
    }
    
    /**
     * Test sampled logging
     */
    @GetMapping("/sampled")
    public Map<String, String> testSampled(@RequestParam(defaultValue = "test") String key) {
        loggingService.logSampled(key, "Sampled message", 10);
        return Map.of("status", "sampled logging executed");
    }
    
    /**
     * Test threshold-based logging
     */
    @GetMapping("/threshold")
    public Map<String, String> testThreshold(@RequestParam double value) {
        loggingService.logIfThresholdExceeded("response_time", value, 100.0);
        return Map.of("status", "threshold logging executed");
    }
    
    /**
     * Test user-specific logging
     */
    @GetMapping("/user-specific")
    public Map<String, String> testUserSpecific(@RequestParam String userId) {
        loggingService.logForSpecificUser(userId, "User action performed");
        return Map.of("status", "user-specific logging executed");
    }
}
