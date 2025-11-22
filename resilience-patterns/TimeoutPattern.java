package com.example.resilience.timeout;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Timeout Pattern Implementation
 * 
 * Purpose: Prevents indefinite waiting by setting time limits on operations,
 * ensuring system responsiveness and preventing resource exhaustion.
 * 
 * Key Components:
 * 1. TimeoutHandler - Manages timeout execution
 * 2. TimeoutConfig - Configuration for timeout settings
 * 3. TimeoutException - Custom exception for timeout scenarios
 * 4. TimeoutMetrics - Track timeout occurrences
 * 
 * Features:
 * - Configurable timeout duration
 * - Thread interruption on timeout
 * - Fallback on timeout
 * - Timeout metrics tracking
 * - Async execution with timeout
 */

// Timeout Exception
class TimeoutException extends RuntimeException {
    private final Duration timeoutDuration;
    
    public TimeoutException(Duration timeoutDuration) {
        super("Operation timed out after " + timeoutDuration.toMillis() + "ms");
        this.timeoutDuration = timeoutDuration;
    }
    
    public Duration getTimeoutDuration() {
        return timeoutDuration;
    }
}

// Timeout Configuration
class TimeoutConfig {
    private final Duration timeout;
    private final boolean interruptOnTimeout;
    private final boolean useFallback;
    
    public TimeoutConfig(Duration timeout) {
        this(timeout, true, false);
    }
    
    public TimeoutConfig(Duration timeout, boolean interruptOnTimeout, boolean useFallback) {
        this.timeout = timeout;
        this.interruptOnTimeout = interruptOnTimeout;
        this.useFallback = useFallback;
    }
    
    public Duration getTimeout() { return timeout; }
    public boolean isInterruptOnTimeout() { return interruptOnTimeout; }
    public boolean isUseFallback() { return useFallback; }
}

// Timeout Metrics
class TimeoutMetrics {
    private final ConcurrentHashMap<String, Integer> timeoutCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> successCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> totalExecutionTime = new ConcurrentHashMap<>();
    
    public void recordTimeout(String operationName) {
        timeoutCount.merge(operationName, 1, Integer::sum);
    }
    
    public void recordSuccess(String operationName, long executionTime) {
        successCount.merge(operationName, 1, Integer::sum);
        totalExecutionTime.merge(operationName, executionTime, Long::sum);
    }
    
    public int getTimeoutCount(String operationName) {
        return timeoutCount.getOrDefault(operationName, 0);
    }
    
    public int getSuccessCount(String operationName) {
        return successCount.getOrDefault(operationName, 0);
    }
    
    public double getTimeoutRate(String operationName) {
        int timeouts = timeoutCount.getOrDefault(operationName, 0);
        int successes = successCount.getOrDefault(operationName, 0);
        int total = timeouts + successes;
        return total == 0 ? 0.0 : (double) timeouts / total * 100;
    }
    
    public double getAverageExecutionTime(String operationName) {
        int count = successCount.getOrDefault(operationName, 0);
        long total = totalExecutionTime.getOrDefault(operationName, 0L);
        return count == 0 ? 0.0 : (double) total / count;
    }
}

// Timeout Handler
class TimeoutHandler<T> {
    private final String operationName;
    private final TimeoutConfig config;
    private final TimeoutMetrics metrics;
    private final ExecutorService executorService;
    
    public TimeoutHandler(String operationName, TimeoutConfig config, TimeoutMetrics metrics) {
        this.operationName = operationName;
        this.config = config;
        this.metrics = metrics;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    public T execute(Supplier<T> operation) {
        return execute(operation, null);
    }
    
    public T execute(Supplier<T> operation, Supplier<T> fallback) {
        Future<T> future = executorService.submit(() -> operation.get());
        long startTime = System.currentTimeMillis();
        
        try {
            T result = future.get(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;
            metrics.recordSuccess(operationName, executionTime);
            System.out.println(String.format("[Timeout] Operation '%s' completed in %dms", 
                                            operationName, executionTime));
            return result;
        } catch (java.util.concurrent.TimeoutException e) {
            metrics.recordTimeout(operationName);
            System.out.println(String.format("[Timeout] Operation '%s' timed out after %dms", 
                                            operationName, config.getTimeout().toMillis()));
            
            if (config.isInterruptOnTimeout()) {
                future.cancel(true);
            }
            
            if (config.isUseFallback() && fallback != null) {
                System.out.println("[Timeout] Using fallback for: " + operationName);
                return fallback.get();
            }
            
            throw new TimeoutException(config.getTimeout());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Operation failed", e.getCause());
        }
    }
    
    public CompletableFuture<T> executeAsync(Supplier<T> operation) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        
        CompletableFuture.supplyAsync(operation, executorService)
            .orTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        metrics.recordTimeout(operationName);
                        promise.completeExceptionally(new TimeoutException(config.getTimeout()));
                    } else {
                        promise.completeExceptionally(throwable);
                    }
                } else {
                    metrics.recordSuccess(operationName, 0);
                    promise.complete(result);
                }
            });
        
        return promise;
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

// Database Service (simulated)
@Component
class DatabaseService {
    private final TimeoutMetrics metrics = new TimeoutMetrics();
    
    public String queryWithTimeout(String query, Duration timeout) {
        TimeoutConfig config = new TimeoutConfig(timeout);
        TimeoutHandler<String> handler = new TimeoutHandler<>("database-query", config, metrics);
        
        try {
            return handler.execute(() -> performDatabaseQuery(query));
        } finally {
            handler.shutdown();
        }
    }
    
    public String queryWithTimeoutAndFallback(String query, Duration timeout) {
        TimeoutConfig config = new TimeoutConfig(timeout, true, true);
        TimeoutHandler<String> handler = new TimeoutHandler<>("database-query-with-fallback", config, metrics);
        
        try {
            return handler.execute(
                () -> performDatabaseQuery(query),
                () -> "Cached result for: " + query
            );
        } finally {
            handler.shutdown();
        }
    }
    
    private String performDatabaseQuery(String query) {
        // Simulate database query with variable execution time
        int executionTime = (int) (Math.random() * 3000); // 0-3000ms
        
        try {
            Thread.sleep(executionTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Query interrupted");
        }
        
        return "Query result for: " + query + " (took " + executionTime + "ms)";
    }
    
    public TimeoutMetrics getMetrics() {
        return metrics;
    }
}

// External API Service (simulated)
@Component
class ExternalApiService {
    private final TimeoutMetrics metrics = new TimeoutMetrics();
    
    public String callApiWithTimeout(String endpoint, Duration timeout) {
        TimeoutConfig config = new TimeoutConfig(timeout);
        TimeoutHandler<String> handler = new TimeoutHandler<>("api-call-" + endpoint, config, metrics);
        
        try {
            return handler.execute(() -> performApiCall(endpoint));
        } finally {
            handler.shutdown();
        }
    }
    
    public CompletableFuture<String> callApiAsync(String endpoint, Duration timeout) {
        TimeoutConfig config = new TimeoutConfig(timeout);
        TimeoutHandler<String> handler = new TimeoutHandler<>("async-api-call-" + endpoint, config, metrics);
        
        return handler.executeAsync(() -> performApiCall(endpoint))
            .whenComplete((result, throwable) -> handler.shutdown());
    }
    
    private String performApiCall(String endpoint) {
        // Simulate API call with variable execution time
        int executionTime = (int) (Math.random() * 4000); // 0-4000ms
        
        try {
            Thread.sleep(executionTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("API call interrupted");
        }
        
        return "API response from " + endpoint + " (took " + executionTime + "ms)";
    }
    
    public TimeoutMetrics getMetrics() {
        return metrics;
    }
}

// Batch Processing Service
@Component
class BatchProcessingService {
    private final TimeoutMetrics metrics = new TimeoutMetrics();
    
    public String processBatch(int batchSize, Duration timeoutPerItem) {
        StringBuilder results = new StringBuilder();
        int successCount = 0;
        int timeoutCount = 0;
        
        for (int i = 0; i < batchSize; i++) {
            TimeoutConfig config = new TimeoutConfig(timeoutPerItem);
            TimeoutHandler<String> handler = new TimeoutHandler<>("batch-item-" + i, config, metrics);
            
            try {
                String result = handler.execute(() -> processItem(i));
                results.append(result).append("\n");
                successCount++;
            } catch (TimeoutException e) {
                results.append("Item ").append(i).append(" timed out\n");
                timeoutCount++;
            } finally {
                handler.shutdown();
            }
        }
        
        return String.format("Batch processing complete: %d succeeded, %d timed out\n%s", 
                           successCount, timeoutCount, results.toString());
    }
    
    private String processItem(int itemId) {
        int processingTime = (int) (Math.random() * 2000); // 0-2000ms
        
        try {
            Thread.sleep(processingTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted");
        }
        
        return "Processed item " + itemId + " in " + processingTime + "ms";
    }
    
    public TimeoutMetrics getMetrics() {
        return metrics;
    }
}

// REST Controller
@RestController
@RequestMapping("/api/timeout")
class TimeoutController {
    private final DatabaseService databaseService;
    private final ExternalApiService apiService;
    private final BatchProcessingService batchService;
    
    public TimeoutController(DatabaseService databaseService, 
                           ExternalApiService apiService,
                           BatchProcessingService batchService) {
        this.databaseService = databaseService;
        this.apiService = apiService;
        this.batchService = batchService;
    }
    
    @GetMapping("/database-query")
    public String queryDatabase(@RequestParam String query, 
                               @RequestParam(defaultValue = "1000") long timeoutMs) {
        try {
            return databaseService.queryWithTimeout(query, Duration.ofMillis(timeoutMs));
        } catch (TimeoutException e) {
            return "Database query timed out: " + e.getMessage();
        }
    }
    
    @GetMapping("/database-query-with-fallback")
    public String queryDatabaseWithFallback(@RequestParam String query,
                                           @RequestParam(defaultValue = "1000") long timeoutMs) {
        return databaseService.queryWithTimeoutAndFallback(query, Duration.ofMillis(timeoutMs));
    }
    
    @GetMapping("/api-call")
    public String callApi(@RequestParam String endpoint,
                         @RequestParam(defaultValue = "2000") long timeoutMs) {
        try {
            return apiService.callApiWithTimeout(endpoint, Duration.ofMillis(timeoutMs));
        } catch (TimeoutException e) {
            return "API call timed out: " + e.getMessage();
        }
    }
    
    @GetMapping("/api-call-async")
    public CompletableFuture<String> callApiAsync(@RequestParam String endpoint,
                                                  @RequestParam(defaultValue = "2000") long timeoutMs) {
        return apiService.callApiAsync(endpoint, Duration.ofMillis(timeoutMs))
            .exceptionally(throwable -> "Async API call failed: " + throwable.getMessage());
    }
    
    @GetMapping("/batch-process")
    public String processBatch(@RequestParam(defaultValue = "10") int batchSize,
                              @RequestParam(defaultValue = "1000") long timeoutMs) {
        return batchService.processBatch(batchSize, Duration.ofMillis(timeoutMs));
    }
    
    @GetMapping("/metrics")
    public String getMetrics() {
        StringBuilder sb = new StringBuilder("Timeout Metrics:\n\n");
        
        TimeoutMetrics dbMetrics = databaseService.getMetrics();
        sb.append("Database Service:\n");
        sb.append("  Timeouts: ").append(dbMetrics.getTimeoutCount("database-query")).append("\n");
        sb.append("  Successes: ").append(dbMetrics.getSuccessCount("database-query")).append("\n");
        sb.append("  Timeout Rate: ").append(String.format("%.2f%%", dbMetrics.getTimeoutRate("database-query"))).append("\n\n");
        
        TimeoutMetrics apiMetrics = apiService.getMetrics();
        sb.append("API Service:\n");
        sb.append("  Timeouts: ").append(apiMetrics.getTimeoutCount("api-call-users")).append("\n");
        sb.append("  Successes: ").append(apiMetrics.getSuccessCount("api-call-users")).append("\n");
        
        return sb.toString();
    }
}

/**
 * Demonstration of Timeout Pattern
 */
public class TimeoutPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Timeout Pattern Demo ===\n");
        
        TimeoutMetrics metrics = new TimeoutMetrics();
        
        // Scenario 1: Operation completes within timeout
        System.out.println("1. Operation Completes Within Timeout:");
        TimeoutConfig config1 = new TimeoutConfig(Duration.ofSeconds(2));
        TimeoutHandler<String> handler1 = new TimeoutHandler<>("fast-operation", config1, metrics);
        
        try {
            String result = handler1.execute(() -> {
                try {
                    Thread.sleep(500);
                    return "Fast operation completed";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            });
            System.out.println("Result: " + result);
        } catch (TimeoutException e) {
            System.out.println("Timed out: " + e.getMessage());
        } finally {
            handler1.shutdown();
        }
        
        // Scenario 2: Operation times out
        System.out.println("\n2. Operation Times Out:");
        TimeoutConfig config2 = new TimeoutConfig(Duration.ofSeconds(1));
        TimeoutHandler<String> handler2 = new TimeoutHandler<>("slow-operation", config2, metrics);
        
        try {
            String result = handler2.execute(() -> {
                try {
                    Thread.sleep(3000);
                    return "This should not complete";
                } catch (InterruptedException e) {
                    System.out.println("   Operation was interrupted");
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            });
            System.out.println("Result: " + result);
        } catch (TimeoutException e) {
            System.out.println("Timed out: " + e.getMessage());
        } finally {
            handler2.shutdown();
        }
        
        // Scenario 3: Timeout with fallback
        System.out.println("\n3. Timeout with Fallback:");
        TimeoutConfig config3 = new TimeoutConfig(Duration.ofMillis(500), true, true);
        TimeoutHandler<String> handler3 = new TimeoutHandler<>("operation-with-fallback", config3, metrics);
        
        try {
            String result = handler3.execute(
                () -> {
                    try {
                        Thread.sleep(2000);
                        return "Primary result";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                },
                () -> "Fallback result - cached data"
            );
            System.out.println("Result: " + result);
        } catch (TimeoutException e) {
            System.out.println("Timed out: " + e.getMessage());
        } finally {
            handler3.shutdown();
        }
        
        // Scenario 4: Async execution with timeout
        System.out.println("\n4. Async Execution with Timeout:");
        TimeoutConfig config4 = new TimeoutConfig(Duration.ofSeconds(1));
        TimeoutHandler<String> handler4 = new TimeoutHandler<>("async-operation", config4, metrics);
        
        CompletableFuture<String> future = handler4.executeAsync(() -> {
            try {
                Thread.sleep(1500);
                return "Async result";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
        
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                System.out.println("Async operation failed: " + throwable.getMessage());
            } else {
                System.out.println("Async result: " + result);
            }
            handler4.shutdown();
        });
        
        // Wait for async operation
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Display metrics
        System.out.println("\n=== Timeout Metrics ===");
        System.out.println("Fast Operation - Timeouts: " + metrics.getTimeoutCount("fast-operation"));
        System.out.println("Fast Operation - Successes: " + metrics.getSuccessCount("fast-operation"));
        System.out.println("Slow Operation - Timeouts: " + metrics.getTimeoutCount("slow-operation"));
        System.out.println("Slow Operation - Successes: " + metrics.getSuccessCount("slow-operation"));
        System.out.println("Operation with Fallback - Timeouts: " + metrics.getTimeoutCount("operation-with-fallback"));
    }
}
