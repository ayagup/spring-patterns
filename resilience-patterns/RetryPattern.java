package com.example.resilience.retry;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Retry Pattern Implementation
 * 
 * Purpose: Automatically retries failed operations with configurable strategies,
 * improving reliability in the face of transient failures.
 * 
 * Key Components:
 * 1. RetryPolicy - Configuration for retry behavior
 * 2. BackoffStrategy - Different backoff strategies (Fixed, Exponential, Random)
 * 3. RetryHandler - Executes operations with retry logic
 * 4. RetryMetrics - Track retry attempts and success rates
 * 
 * Features:
 * - Configurable retry attempts
 * - Multiple backoff strategies
 * - Conditional retry based on exception type
 * - Retry metrics tracking
 * - Exponential backoff with jitter
 */

// Backoff Strategy Interface
interface BackoffStrategy {
    long calculateBackoff(int attemptNumber);
    String getStrategyName();
}

// Fixed Backoff Strategy
class FixedBackoffStrategy implements BackoffStrategy {
    private final long fixedDelayMs;
    
    public FixedBackoffStrategy(long fixedDelayMs) {
        this.fixedDelayMs = fixedDelayMs;
    }
    
    @Override
    public long calculateBackoff(int attemptNumber) {
        return fixedDelayMs;
    }
    
    @Override
    public String getStrategyName() {
        return "FixedBackoff(" + fixedDelayMs + "ms)";
    }
}

// Exponential Backoff Strategy
class ExponentialBackoffStrategy implements BackoffStrategy {
    private final long initialDelayMs;
    private final double multiplier;
    private final long maxDelayMs;
    
    public ExponentialBackoffStrategy(long initialDelayMs, double multiplier, long maxDelayMs) {
        this.initialDelayMs = initialDelayMs;
        this.multiplier = multiplier;
        this.maxDelayMs = maxDelayMs;
    }
    
    public ExponentialBackoffStrategy() {
        this(100, 2.0, 10000);
    }
    
    @Override
    public long calculateBackoff(int attemptNumber) {
        long delay = (long) (initialDelayMs * Math.pow(multiplier, attemptNumber - 1));
        return Math.min(delay, maxDelayMs);
    }
    
    @Override
    public String getStrategyName() {
        return String.format("ExponentialBackoff(initial=%dms, multiplier=%.1f, max=%dms)", 
                           initialDelayMs, multiplier, maxDelayMs);
    }
}

// Exponential Backoff with Jitter
class ExponentialBackoffWithJitterStrategy implements BackoffStrategy {
    private final long initialDelayMs;
    private final double multiplier;
    private final long maxDelayMs;
    private final Random random = new Random();
    
    public ExponentialBackoffWithJitterStrategy(long initialDelayMs, double multiplier, long maxDelayMs) {
        this.initialDelayMs = initialDelayMs;
        this.multiplier = multiplier;
        this.maxDelayMs = maxDelayMs;
    }
    
    public ExponentialBackoffWithJitterStrategy() {
        this(100, 2.0, 10000);
    }
    
    @Override
    public long calculateBackoff(int attemptNumber) {
        long exponentialDelay = (long) (initialDelayMs * Math.pow(multiplier, attemptNumber - 1));
        long cappedDelay = Math.min(exponentialDelay, maxDelayMs);
        // Add random jitter (0-100% of calculated delay)
        return (long) (cappedDelay * random.nextDouble());
    }
    
    @Override
    public String getStrategyName() {
        return "ExponentialBackoffWithJitter";
    }
}

// Linear Backoff Strategy
class LinearBackoffStrategy implements BackoffStrategy {
    private final long initialDelayMs;
    private final long incrementMs;
    
    public LinearBackoffStrategy(long initialDelayMs, long incrementMs) {
        this.initialDelayMs = initialDelayMs;
        this.incrementMs = incrementMs;
    }
    
    @Override
    public long calculateBackoff(int attemptNumber) {
        return initialDelayMs + (incrementMs * (attemptNumber - 1));
    }
    
    @Override
    public String getStrategyName() {
        return String.format("LinearBackoff(initial=%dms, increment=%dms)", initialDelayMs, incrementMs);
    }
}

// Retry Policy
class RetryPolicy {
    private final int maxAttempts;
    private final BackoffStrategy backoffStrategy;
    private final Predicate<Exception> retryableException;
    private final Duration timeout;
    
    public RetryPolicy(int maxAttempts, BackoffStrategy backoffStrategy) {
        this(maxAttempts, backoffStrategy, e -> true, null);
    }
    
    public RetryPolicy(int maxAttempts, BackoffStrategy backoffStrategy, 
                      Predicate<Exception> retryableException, Duration timeout) {
        this.maxAttempts = maxAttempts;
        this.backoffStrategy = backoffStrategy;
        this.retryableException = retryableException;
        this.timeout = timeout;
    }
    
    public int getMaxAttempts() { return maxAttempts; }
    public BackoffStrategy getBackoffStrategy() { return backoffStrategy; }
    public Predicate<Exception> getRetryableException() { return retryableException; }
    public Duration getTimeout() { return timeout; }
}

// Retry Metrics
class RetryMetrics {
    private final ConcurrentHashMap<String, Integer> totalAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> successfulRetries = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> failedRetries = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> firstAttemptSuccess = new ConcurrentHashMap<>();
    
    public void recordAttempt(String operationName) {
        totalAttempts.merge(operationName, 1, Integer::sum);
    }
    
    public void recordSuccessfulRetry(String operationName) {
        successfulRetries.merge(operationName, 1, Integer::sum);
    }
    
    public void recordFailedRetry(String operationName) {
        failedRetries.merge(operationName, 1, Integer::sum);
    }
    
    public void recordFirstAttemptSuccess(String operationName) {
        firstAttemptSuccess.merge(operationName, 1, Integer::sum);
    }
    
    public int getTotalAttempts(String operationName) {
        return totalAttempts.getOrDefault(operationName, 0);
    }
    
    public int getSuccessfulRetries(String operationName) {
        return successfulRetries.getOrDefault(operationName, 0);
    }
    
    public int getFailedRetries(String operationName) {
        return failedRetries.getOrDefault(operationName, 0);
    }
    
    public double getRetrySuccessRate(String operationName) {
        int successful = successfulRetries.getOrDefault(operationName, 0);
        int failed = failedRetries.getOrDefault(operationName, 0);
        int total = successful + failed;
        return total == 0 ? 0.0 : (double) successful / total * 100;
    }
}

// Retry Exception
class RetryExhaustedException extends RuntimeException {
    private final int attempts;
    
    public RetryExhaustedException(String operationName, int attempts, Exception lastException) {
        super(String.format("Retry exhausted for '%s' after %d attempts", operationName, attempts), 
              lastException);
        this.attempts = attempts;
    }
    
    public int getAttempts() {
        return attempts;
    }
}

// Retry Handler
class RetryHandler<T> {
    private final String operationName;
    private final RetryPolicy policy;
    private final RetryMetrics metrics;
    
    public RetryHandler(String operationName, RetryPolicy policy, RetryMetrics metrics) {
        this.operationName = operationName;
        this.policy = policy;
        this.metrics = metrics;
    }
    
    public T execute(Supplier<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= policy.getMaxAttempts(); attempt++) {
            try {
                metrics.recordAttempt(operationName);
                
                System.out.println(String.format("[Retry] Attempt %d/%d for operation: %s", 
                                                attempt, policy.getMaxAttempts(), operationName));
                
                T result = operation.get();
                
                if (attempt == 1) {
                    metrics.recordFirstAttemptSuccess(operationName);
                } else {
                    metrics.recordSuccessfulRetry(operationName);
                    System.out.println(String.format("[Retry] Operation '%s' succeeded on attempt %d", 
                                                    operationName, attempt));
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                
                // Check if exception is retryable
                if (!policy.getRetryableException().test(e)) {
                    System.out.println(String.format("[Retry] Non-retryable exception for '%s': %s", 
                                                    operationName, e.getMessage()));
                    metrics.recordFailedRetry(operationName);
                    throw e;
                }
                
                // Check if we should retry
                if (attempt < policy.getMaxAttempts()) {
                    long backoffMs = policy.getBackoffStrategy().calculateBackoff(attempt);
                    
                    System.out.println(String.format("[Retry] Attempt %d failed for '%s': %s. Retrying in %dms...", 
                                                    attempt, operationName, e.getMessage(), backoffMs));
                    
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    System.out.println(String.format("[Retry] All %d attempts failed for '%s'", 
                                                    attempt, operationName));
                    metrics.recordFailedRetry(operationName);
                }
            }
        }
        
        throw new RetryExhaustedException(operationName, policy.getMaxAttempts(), lastException);
    }
}

// Unreliable Service (simulated)
@Component
class UnreliableService {
    private final RetryMetrics metrics = new RetryMetrics();
    private int callCount = 0;
    private final Random random = new Random();
    
    public String callWithRetry(int successAfterAttempts) {
        RetryPolicy policy = new RetryPolicy(5, new ExponentialBackoffStrategy());
        RetryHandler<String> handler = new RetryHandler<>("unreliable-service", policy, metrics);
        
        return handler.execute(() -> simulateUnreliableCall(successAfterAttempts));
    }
    
    public String callWithFixedRetry() {
        RetryPolicy policy = new RetryPolicy(3, new FixedBackoffStrategy(1000));
        RetryHandler<String> handler = new RetryHandler<>("fixed-retry-service", policy, metrics);
        
        return handler.execute(this::randomFailure);
    }
    
    public String callWithConditionalRetry() {
        // Only retry on specific exceptions
        Predicate<Exception> retryPredicate = e -> 
            e instanceof RuntimeException && e.getMessage().contains("Temporary");
        
        RetryPolicy policy = new RetryPolicy(3, new ExponentialBackoffStrategy(), retryPredicate, null);
        RetryHandler<String> handler = new RetryHandler<>("conditional-retry-service", policy, metrics);
        
        return handler.execute(this::conditionalFailure);
    }
    
    private String simulateUnreliableCall(int successAfterAttempts) {
        callCount++;
        
        if (callCount < successAfterAttempts) {
            throw new RuntimeException("Temporary failure - network timeout");
        }
        
        callCount = 0;
        return "Success after " + successAfterAttempts + " attempts";
    }
    
    private String randomFailure() {
        if (random.nextDouble() < 0.6) { // 60% failure rate
            throw new RuntimeException("Temporary service unavailability");
        }
        return "Service call successful";
    }
    
    private String conditionalFailure() {
        double rand = random.nextDouble();
        
        if (rand < 0.3) {
            throw new RuntimeException("Temporary database connection issue");
        } else if (rand < 0.4) {
            throw new IllegalArgumentException("Invalid request - not retryable");
        }
        
        return "Conditional retry successful";
    }
    
    public RetryMetrics getMetrics() {
        return metrics;
    }
}

// Database Repository with Retry
@Component
class DatabaseRepository {
    private final RetryMetrics metrics = new RetryMetrics();
    private final Random random = new Random();
    
    public String saveWithRetry(String data) {
        RetryPolicy policy = new RetryPolicy(
            3, 
            new ExponentialBackoffWithJitterStrategy(200, 2.0, 5000)
        );
        RetryHandler<String> handler = new RetryHandler<>("database-save", policy, metrics);
        
        return handler.execute(() -> saveToDatabase(data));
    }
    
    private String saveToDatabase(String data) {
        if (random.nextDouble() < 0.5) {
            throw new RuntimeException("Temporary database connection timeout");
        }
        return "Saved: " + data;
    }
    
    public RetryMetrics getMetrics() {
        return metrics;
    }
}

// REST Controller
@RestController
@RequestMapping("/api/retry")
class RetryController {
    private final UnreliableService unreliableService;
    private final DatabaseRepository databaseRepository;
    
    public RetryController(UnreliableService unreliableService, DatabaseRepository databaseRepository) {
        this.unreliableService = unreliableService;
        this.databaseRepository = databaseRepository;
    }
    
    @GetMapping("/call/{attempts}")
    public String callService(@PathVariable int attempts) {
        try {
            return unreliableService.callWithRetry(attempts);
        } catch (RetryExhaustedException e) {
            return "Failed after " + e.getAttempts() + " attempts: " + e.getCause().getMessage();
        }
    }
    
    @GetMapping("/fixed-retry")
    public String fixedRetry() {
        try {
            return unreliableService.callWithFixedRetry();
        } catch (RetryExhaustedException e) {
            return "Fixed retry failed: " + e.getMessage();
        }
    }
    
    @GetMapping("/conditional-retry")
    public String conditionalRetry() {
        try {
            return unreliableService.callWithConditionalRetry();
        } catch (Exception e) {
            return "Conditional retry result: " + e.getMessage();
        }
    }
    
    @PostMapping("/save")
    public String saveData(@RequestParam String data) {
        try {
            return databaseRepository.saveWithRetry(data);
        } catch (RetryExhaustedException e) {
            return "Save failed after retries: " + e.getMessage();
        }
    }
    
    @GetMapping("/metrics")
    public String getMetrics() {
        StringBuilder sb = new StringBuilder("Retry Metrics:\n\n");
        
        RetryMetrics serviceMetrics = unreliableService.getMetrics();
        sb.append("Unreliable Service:\n");
        sb.append("  Total Attempts: ").append(serviceMetrics.getTotalAttempts("unreliable-service")).append("\n");
        sb.append("  Successful Retries: ").append(serviceMetrics.getSuccessfulRetries("unreliable-service")).append("\n");
        sb.append("  Failed Retries: ").append(serviceMetrics.getFailedRetries("unreliable-service")).append("\n");
        sb.append("  Retry Success Rate: ").append(String.format("%.2f%%", serviceMetrics.getRetrySuccessRate("unreliable-service"))).append("\n\n");
        
        RetryMetrics dbMetrics = databaseRepository.getMetrics();
        sb.append("Database Repository:\n");
        sb.append("  Total Attempts: ").append(dbMetrics.getTotalAttempts("database-save")).append("\n");
        sb.append("  Successful Retries: ").append(dbMetrics.getSuccessfulRetries("database-save")).append("\n");
        
        return sb.toString();
    }
}

/**
 * Demonstration of Retry Pattern
 */
public class RetryPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Retry Pattern Demo ===\n");
        
        RetryMetrics metrics = new RetryMetrics();
        
        // Scenario 1: Fixed Backoff Retry
        System.out.println("1. Fixed Backoff Retry (500ms between attempts):");
        RetryPolicy policy1 = new RetryPolicy(3, new FixedBackoffStrategy(500));
        RetryHandler<String> handler1 = new RetryHandler<>("test-operation-1", policy1, metrics);
        
        int[] attempt1 = {0};
        try {
            String result = handler1.execute(() -> {
                attempt1[0]++;
                if (attempt1[0] < 3) {
                    throw new RuntimeException("Temporary failure");
                }
                return "Success on attempt " + attempt1[0];
            });
            System.out.println("Final result: " + result + "\n");
        } catch (RetryExhaustedException e) {
            System.out.println("Failed: " + e.getMessage() + "\n");
        }
        
        // Scenario 2: Exponential Backoff Retry
        System.out.println("2. Exponential Backoff Retry (100ms, 200ms, 400ms):");
        RetryPolicy policy2 = new RetryPolicy(4, new ExponentialBackoffStrategy(100, 2.0, 10000));
        RetryHandler<String> handler2 = new RetryHandler<>("test-operation-2", policy2, metrics);
        
        int[] attempt2 = {0};
        try {
            String result = handler2.execute(() -> {
                attempt2[0]++;
                if (attempt2[0] < 3) {
                    throw new RuntimeException("Service temporarily unavailable");
                }
                return "Success with exponential backoff";
            });
            System.out.println("Final result: " + result + "\n");
        } catch (RetryExhaustedException e) {
            System.out.println("Failed: " + e.getMessage() + "\n");
        }
        
        // Scenario 3: Conditional Retry (only retry specific exceptions)
        System.out.println("3. Conditional Retry (only retry RuntimeException):");
        Predicate<Exception> retryPredicate = e -> e instanceof RuntimeException;
        RetryPolicy policy3 = new RetryPolicy(3, new FixedBackoffStrategy(200), retryPredicate, null);
        RetryHandler<String> handler3 = new RetryHandler<>("conditional-operation", policy3, metrics);
        
        try {
            handler3.execute(() -> {
                throw new IllegalArgumentException("Invalid input - not retryable");
            });
        } catch (IllegalArgumentException e) {
            System.out.println("Non-retryable exception caught: " + e.getMessage() + "\n");
        }
        
        // Scenario 4: Linear Backoff
        System.out.println("4. Linear Backoff Retry (500ms, 700ms, 900ms):");
        RetryPolicy policy4 = new RetryPolicy(3, new LinearBackoffStrategy(500, 200));
        RetryHandler<String> handler4 = new RetryHandler<>("linear-operation", policy4, metrics);
        
        int[] attempt4 = {0};
        try {
            String result = handler4.execute(() -> {
                attempt4[0]++;
                if (attempt4[0] < 2) {
                    throw new RuntimeException("Transient error");
                }
                return "Success with linear backoff";
            });
            System.out.println("Final result: " + result + "\n");
        } catch (RetryExhaustedException e) {
            System.out.println("Failed: " + e.getMessage() + "\n");
        }
        
        // Display metrics
        System.out.println("=== Retry Metrics ===");
        System.out.println("Test Operation 1 - Total Attempts: " + metrics.getTotalAttempts("test-operation-1"));
        System.out.println("Test Operation 1 - Successful Retries: " + metrics.getSuccessfulRetries("test-operation-1"));
        System.out.println("Test Operation 2 - Total Attempts: " + metrics.getTotalAttempts("test-operation-2"));
        System.out.println("Test Operation 2 - Retry Success Rate: " + 
                         String.format("%.2f%%", metrics.getRetrySuccessRate("test-operation-2")));
    }
}
