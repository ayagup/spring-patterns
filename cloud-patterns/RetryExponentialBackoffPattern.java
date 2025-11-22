package com.example.cloud.retry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Retry with Exponential Backoff Pattern - Demonstrates Retry Logic with Backoff Strategy
 * 
 * This pattern shows how to:
 * 1. Implement retry logic for transient failures
 * 2. Use exponential backoff between retries
 * 3. Configure max retry attempts
 * 4. Implement retry policies
 * 5. Handle different exception types
 * 6. Add jitter to backoff delays
 * 7. Monitor retry metrics
 * 8. Implement circuit breaker with retry
 * 9. Configure conditional retry
 * 10. Test retry behavior
 * 
 * Key Concepts:
 * - Retry: Automatically retry failed operations
 * - Exponential Backoff: Increase delay between retries
 * - Jitter: Random delay to prevent synchronized retries
 * - Transient Failures: Temporary failures that might succeed on retry
 * - Max Attempts: Limit number of retry attempts
 * 
 * Retry Strategies:
 * 1. Fixed Delay - Same delay between retries
 * 2. Exponential Backoff - 1s, 2s, 4s, 8s...
 * 3. Exponential with Jitter - Add randomness
 * 
 * Dependencies:
 * - spring-retry
 * - resilience4j-retry
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
@EnableRetry
public class RetryExponentialBackoffPattern {

    public static void main(String[] args) {
        SpringApplication.run(RetryExponentialBackoffPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("RETRY WITH EXPONENTIAL BACKOFF PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateBackoffStrategies();
        demonstrateConfiguration();
        
        System.out.println("\nApplication running with retry enabled");
        System.out.println("Test endpoints:");
        System.out.println("POST /api/retry/call - Test retry with backoff");
        System.out.println("GET /api/retry/metrics - View retry metrics");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateBackoffStrategies() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("BACKOFF STRATEGIES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Fixed Delay: 1s, 1s, 1s, 1s");
        System.out.println("2. Exponential: 1s, 2s, 4s, 8s, 16s");
        System.out.println("3. Exponential with Jitter: 1s±0.5s, 2s±1s, 4s±2s");
    }
    
    private static void demonstrateConfiguration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RETRY CONFIGURATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\n@Retryable(");
        System.out.println("  value = {TransientException.class},");
        System.out.println("  maxAttempts = 5,");
        System.out.println("  backoff = @Backoff(");
        System.out.println("    delay = 1000,");
        System.out.println("    multiplier = 2,");
        System.out.println("    maxDelay = 10000");
        System.out.println("  )");
        System.out.println(")");
    }
}

/**
 * Retry Configuration
 */
@Configuration
class RetryConfig {
    
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Exponential backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);  // 1 second
        backOffPolicy.setMultiplier(2.0);        // Double each time
        backOffPolicy.setMaxInterval(10000);     // Max 10 seconds
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }
}

/**
 * Retry Metrics
 */
class RetryMetrics {
    private String operationName;
    private int totalAttempts;
    private int successCount;
    private int failureCount;
    private List<RetryAttempt> attempts;
    
    public RetryMetrics(String operationName) {
        this.operationName = operationName;
        this.totalAttempts = 0;
        this.successCount = 0;
        this.failureCount = 0;
        this.attempts = new ArrayList<>();
    }
    
    public void recordAttempt(int attemptNumber, boolean success, long delayMs) {
        totalAttempts++;
        if (success) {
            successCount++;
        } else {
            failureCount++;
        }
        attempts.add(new RetryAttempt(attemptNumber, success, delayMs, LocalDateTime.now()));
    }
    
    // Getters
    public String getOperationName() { return operationName; }
    public int getTotalAttempts() { return totalAttempts; }
    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }
    public List<RetryAttempt> getAttempts() { return attempts; }
    public double getSuccessRate() {
        return totalAttempts > 0 ? (double) successCount / totalAttempts * 100 : 0.0;
    }
}

/**
 * Retry Attempt Record
 */
class RetryAttempt {
    private int attemptNumber;
    private boolean success;
    private long delayMs;
    private LocalDateTime timestamp;
    
    public RetryAttempt(int attemptNumber, boolean success, long delayMs, LocalDateTime timestamp) {
        this.attemptNumber = attemptNumber;
        this.success = success;
        this.delayMs = delayMs;
        this.timestamp = timestamp;
    }
    
    // Getters
    public int getAttemptNumber() { return attemptNumber; }
    public boolean isSuccess() { return success; }
    public long getDelayMs() { return delayMs; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

/**
 * Custom Retry Executor
 */
class RetryExecutor {
    private final int maxAttempts;
    private final long initialDelayMs;
    private final double multiplier;
    private final long maxDelayMs;
    private final boolean useJitter;
    
    public RetryExecutor(int maxAttempts, long initialDelayMs, double multiplier,
                        long maxDelayMs, boolean useJitter) {
        this.maxAttempts = maxAttempts;
        this.initialDelayMs = initialDelayMs;
        this.multiplier = multiplier;
        this.maxDelayMs = maxDelayMs;
        this.useJitter = useJitter;
    }
    
    public <T> T execute(RetryableOperation<T> operation, RetryMetrics metrics) throws Exception {
        int attempt = 0;
        long delay = initialDelayMs;
        Exception lastException = null;
        
        while (attempt < maxAttempts) {
            attempt++;
            
            try {
                T result = operation.execute();
                metrics.recordAttempt(attempt, true, delay);
                return result;
            } catch (TransientException e) {
                lastException = e;
                metrics.recordAttempt(attempt, false, delay);
                
                if (attempt >= maxAttempts) {
                    break;
                }
                
                // Calculate next delay
                long actualDelay = calculateDelay(delay);
                
                System.out.printf("Attempt %d failed, retrying in %dms...%n", 
                    attempt, actualDelay);
                
                try {
                    Thread.sleep(actualDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
                
                // Exponential backoff
                delay = Math.min((long) (delay * multiplier), maxDelayMs);
            }
        }
        
        throw new MaxRetriesExceededException(
            "Max retries (" + maxAttempts + ") exceeded", lastException);
    }
    
    private long calculateDelay(long baseDelay) {
        if (!useJitter) {
            return baseDelay;
        }
        
        // Add jitter: ±25% of base delay
        long jitter = (long) (baseDelay * 0.25);
        long randomJitter = ThreadLocalRandom.current().nextLong(-jitter, jitter + 1);
        return Math.max(0, baseDelay + randomJitter);
    }
}

/**
 * Retryable Operation Interface
 */
@FunctionalInterface
interface RetryableOperation<T> {
    T execute() throws Exception;
}

/**
 * Transient Exception (retryable)
 */
class TransientException extends Exception {
    public TransientException(String message) {
        super(message);
    }
}

/**
 * Max Retries Exceeded Exception
 */
class MaxRetriesExceededException extends RuntimeException {
    public MaxRetriesExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * External Service Simulator
 */
class ExternalService {
    private int callCount = 0;
    private final double failureRate;
    
    public ExternalService(double failureRate) {
        this.failureRate = failureRate;
    }
    
    public String call() throws TransientException {
        callCount++;
        
        // Simulate transient failures
        if (Math.random() < failureRate) {
            throw new TransientException(
                "Transient failure on attempt " + callCount);
        }
        
        return "Success on attempt " + callCount;
    }
    
    public void reset() {
        callCount = 0;
    }
}

/**
 * Retry Service with Spring @Retryable
 */
@Service
class SpringRetryService {
    
    private final ExternalService externalService = new ExternalService(0.7);
    
    @Retryable(
        value = {TransientException.class},
        maxAttempts = 5
    )
    public String callWithRetry() throws TransientException {
        return externalService.call();
    }
}

/**
 * Retry Service with Custom Logic
 */
@Service
class CustomRetryService {
    
    private final Map<String, RetryMetrics> metricsMap = new HashMap<>();
    
    public String callWithExponentialBackoff(String operationName) throws Exception {
        RetryMetrics metrics = metricsMap.computeIfAbsent(
            operationName, RetryMetrics::new);
        
        RetryExecutor executor = new RetryExecutor(
            5,      // max attempts
            1000,   // initial delay (1 second)
            2.0,    // multiplier
            10000,  // max delay (10 seconds)
            true    // use jitter
        );
        
        ExternalService service = new ExternalService(0.6);
        
        return executor.execute(() -> service.call(), metrics);
    }
    
    public String callWithFixedDelay(String operationName) throws Exception {
        RetryMetrics metrics = metricsMap.computeIfAbsent(
            operationName, RetryMetrics::new);
        
        RetryExecutor executor = new RetryExecutor(
            3,      // max attempts
            2000,   // delay (2 seconds)
            1.0,    // no multiplier (fixed delay)
            2000,   // same as delay
            false   // no jitter
        );
        
        ExternalService service = new ExternalService(0.5);
        
        return executor.execute(() -> service.call(), metrics);
    }
    
    public Map<String, RetryMetrics> getAllMetrics() {
        return new HashMap<>(metricsMap);
    }
    
    public RetryMetrics getMetrics(String operationName) {
        return metricsMap.get(operationName);
    }
}

/**
 * REST Controller demonstrating retry patterns
 */
@RestController
@RequestMapping("/api/retry")
class RetryController {
    
    private final CustomRetryService customRetryService;
    
    public RetryController(CustomRetryService customRetryService) {
        this.customRetryService = customRetryService;
    }
    
    @PostMapping("/exponential/{operation}")
    public Map<String, Object> callWithExponentialBackoff(@PathVariable String operation) {
        try {
            String result = customRetryService.callWithExponentialBackoff(operation);
            return Map.of(
                "status", "success",
                "result", result,
                "metrics", customRetryService.getMetrics(operation)
            );
        } catch (MaxRetriesExceededException e) {
            return Map.of(
                "status", "failed",
                "message", e.getMessage(),
                "metrics", customRetryService.getMetrics(operation)
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
    
    @PostMapping("/fixed/{operation}")
    public Map<String, Object> callWithFixedDelay(@PathVariable String operation) {
        try {
            String result = customRetryService.callWithFixedDelay(operation);
            return Map.of(
                "status", "success",
                "result", result,
                "metrics", customRetryService.getMetrics(operation)
            );
        } catch (MaxRetriesExceededException e) {
            return Map.of(
                "status", "failed",
                "message", e.getMessage(),
                "metrics", customRetryService.getMetrics(operation)
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
    
    @GetMapping("/metrics")
    public Map<String, RetryMetrics> getAllMetrics() {
        return customRetryService.getAllMetrics();
    }
    
    @GetMapping("/metrics/{operation}")
    public RetryMetrics getMetrics(@PathVariable String operation) {
        return customRetryService.getMetrics(operation);
    }
}
