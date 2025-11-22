package com.example.cloud.bulkhead;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Bulkhead Pattern - Demonstrates Resource Isolation and Fault Containment
 * 
 * This pattern shows how to:
 * 1. Implement semaphore-based bulkhead
 * 2. Implement thread pool bulkhead
 * 3. Isolate resources per service
 * 4. Prevent resource exhaustion
 * 5. Configure concurrent call limits
 * 6. Handle bulkhead full scenarios
 * 7. Monitor bulkhead metrics
 * 8. Implement fallback mechanisms
 * 9. Configure timeout and queue size
 * 10. Test bulkhead behavior
 * 
 * Key Concepts:
 * - Bulkhead: Isolate resources to prevent cascading failures
 * - Semaphore: Limit concurrent calls
 * - Thread Pool: Separate thread pools per service
 * - Resource Isolation: Prevent one service from consuming all resources
 * - Fault Containment: Failures don't spread
 * 
 * Bulkhead Types:
 * 1. Semaphore Bulkhead - Limits concurrent calls
 * 2. Thread Pool Bulkhead - Uses separate thread pool
 * 
 * Dependencies:
 * - resilience4j-spring-boot3
 * - resilience4j-bulkhead
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class BulkheadPattern {

    public static void main(String[] args) {
        SpringApplication.run(BulkheadPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("BULKHEAD PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateBulkheadTypes();
        demonstrateConfiguration();
        
        System.out.println("\nApplication running with bulkhead protection");
        System.out.println("Test endpoints:");
        System.out.println("POST /api/bulkhead/process - Test semaphore bulkhead");
        System.out.println("POST /api/bulkhead/async - Test thread pool bulkhead");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateBulkheadTypes() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("BULKHEAD TYPES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Semaphore Bulkhead:");
        System.out.println("   - Limits concurrent calls");
        System.out.println("   - Lightweight, no thread overhead");
        System.out.println("   - Suitable for fast operations");
        
        System.out.println("\n2. Thread Pool Bulkhead:");
        System.out.println("   - Separate thread pool");
        System.out.println("   - Queue for pending requests");
        System.out.println("   - Suitable for long-running operations");
    }
    
    private static void demonstrateConfiguration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("BULKHEAD CONFIGURATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\nSemaphore Bulkhead:");
        System.out.println("maxConcurrentCalls=10");
        System.out.println("maxWaitDuration=500ms");
        
        System.out.println("\nThread Pool Bulkhead:");
        System.out.println("maxThreadPoolSize=10");
        System.out.println("coreThreadPoolSize=5");
        System.out.println("queueCapacity=20");
        System.out.println("keepAliveDuration=20ms");
    }
}

/**
 * Bulkhead Configuration
 */
@Configuration
class BulkheadConfigBean {
    
    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig config = BulkheadConfig.custom()
            .maxConcurrentCalls(10)
            .maxWaitDuration(Duration.ofMillis(500))
            .build();
        
        return BulkheadRegistry.of(config);
    }
    
    @Bean
    public ThreadPoolBulkheadConfig threadPoolBulkheadConfig() {
        return ThreadPoolBulkheadConfig.custom()
            .maxThreadPoolSize(10)
            .coreThreadPoolSize(5)
            .queueCapacity(20)
            .keepAliveDuration(Duration.ofMillis(20))
            .build();
    }
}

/**
 * Semaphore Bulkhead Implementation
 */
class SemaphoreBulkhead {
    private final Semaphore semaphore;
    private final String name;
    private final long maxWaitTimeMs;
    private long totalRequests = 0;
    private long rejectedRequests = 0;
    private long successfulRequests = 0;
    
    public SemaphoreBulkhead(String name, int maxConcurrentCalls, long maxWaitTimeMs) {
        this.name = name;
        this.semaphore = new Semaphore(maxConcurrentCalls);
        this.maxWaitTimeMs = maxWaitTimeMs;
    }
    
    public <T> T execute(Supplier<T> operation) throws Exception {
        totalRequests++;
        
        boolean acquired = semaphore.tryAcquire(maxWaitTimeMs, TimeUnit.MILLISECONDS);
        
        if (!acquired) {
            rejectedRequests++;
            throw new BulkheadFullException(
                "Bulkhead " + name + " is full, request rejected");
        }
        
        try {
            T result = operation.get();
            successfulRequests++;
            return result;
        } finally {
            semaphore.release();
        }
    }
    
    public BulkheadMetrics getMetrics() {
        return new BulkheadMetrics(
            name,
            semaphore.availablePermits(),
            totalRequests,
            successfulRequests,
            rejectedRequests
        );
    }
}

/**
 * Thread Pool Bulkhead Implementation
 */
class CustomThreadPoolBulkhead {
    private final String name;
    private final ExecutorService executorService;
    private final BlockingQueue<Runnable> queue;
    private long totalRequests = 0;
    private long rejectedRequests = 0;
    private long successfulRequests = 0;
    
    public CustomThreadPoolBulkhead(String name, int corePoolSize, 
                                   int maxPoolSize, int queueCapacity) {
        this.name = name;
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
        this.executorService = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            60L,
            TimeUnit.SECONDS,
            queue,
            new ThreadPoolExecutor.AbortPolicy()
        );
    }
    
    public <T> CompletableFuture<T> execute(Supplier<T> operation) {
        totalRequests++;
        
        try {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(operation, executorService);
            future.thenRun(() -> successfulRequests++);
            return future;
        } catch (RejectedExecutionException e) {
            rejectedRequests++;
            throw new BulkheadFullException(
                "Thread pool bulkhead " + name + " is full, request rejected");
        }
    }
    
    public BulkheadMetrics getMetrics() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
        return new BulkheadMetrics(
            name,
            executor.getActiveCount(),
            totalRequests,
            successfulRequests,
            rejectedRequests,
            queue.size(),
            executor.getPoolSize()
        );
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}

/**
 * Bulkhead Metrics
 */
class BulkheadMetrics {
    private final String name;
    private final int availablePermits;
    private final long totalRequests;
    private final long successfulRequests;
    private final long rejectedRequests;
    private final int queueSize;
    private final int poolSize;
    
    public BulkheadMetrics(String name, int availablePermits, long totalRequests,
                          long successfulRequests, long rejectedRequests) {
        this(name, availablePermits, totalRequests, successfulRequests, 
             rejectedRequests, 0, 0);
    }
    
    public BulkheadMetrics(String name, int availablePermits, long totalRequests,
                          long successfulRequests, long rejectedRequests,
                          int queueSize, int poolSize) {
        this.name = name;
        this.availablePermits = availablePermits;
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.rejectedRequests = rejectedRequests;
        this.queueSize = queueSize;
        this.poolSize = poolSize;
    }
    
    // Getters
    public String getName() { return name; }
    public int getAvailablePermits() { return availablePermits; }
    public long getTotalRequests() { return totalRequests; }
    public long getSuccessfulRequests() { return successfulRequests; }
    public long getRejectedRequests() { return rejectedRequests; }
    public int getQueueSize() { return queueSize; }
    public int getPoolSize() { return poolSize; }
    public double getSuccessRate() {
        return totalRequests > 0 ? 
            (double) successfulRequests / totalRequests * 100 : 0.0;
    }
}

/**
 * Bulkhead Full Exception
 */
class BulkheadFullException extends RuntimeException {
    public BulkheadFullException(String message) {
        super(message);
    }
}

/**
 * Bulkhead Service
 */
@Service
class BulkheadService {
    
    private final SemaphoreBulkhead userServiceBulkhead;
    private final SemaphoreBulkhead paymentServiceBulkhead;
    private final CustomThreadPoolBulkhead asyncBulkhead;
    
    public BulkheadService() {
        this.userServiceBulkhead = new SemaphoreBulkhead("user-service", 5, 1000);
        this.paymentServiceBulkhead = new SemaphoreBulkhead("payment-service", 3, 500);
        this.asyncBulkhead = new CustomThreadPoolBulkhead("async-service", 3, 5, 10);
    }
    
    public String callUserService(String userId) throws Exception {
        return userServiceBulkhead.execute(() -> {
            // Simulate user service call
            simulateWork(100);
            return "User service response for: " + userId;
        });
    }
    
    public String callPaymentService(String paymentId) throws Exception {
        return paymentServiceBulkhead.execute(() -> {
            // Simulate payment service call
            simulateWork(200);
            return "Payment service response for: " + paymentId;
        });
    }
    
    public CompletableFuture<String> callAsyncService(String requestId) {
        return asyncBulkhead.execute(() -> {
            // Simulate async operation
            simulateWork(500);
            return "Async service response for: " + requestId;
        });
    }
    
    public Map<String, BulkheadMetrics> getAllMetrics() {
        Map<String, BulkheadMetrics> metrics = new HashMap<>();
        metrics.put("user-service", userServiceBulkhead.getMetrics());
        metrics.put("payment-service", paymentServiceBulkhead.getMetrics());
        metrics.put("async-service", asyncBulkhead.getMetrics());
        return metrics;
    }
    
    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * REST Controller demonstrating bulkhead pattern
 */
@RestController
@RequestMapping("/api/bulkhead")
class BulkheadController {
    
    private final BulkheadService bulkheadService;
    
    public BulkheadController(BulkheadService bulkheadService) {
        this.bulkheadService = bulkheadService;
    }
    
    @PostMapping("/user/{userId}")
    public Map<String, Object> callUserService(@PathVariable String userId) {
        try {
            String response = bulkheadService.callUserService(userId);
            return Map.of(
                "status", "success",
                "response", response
            );
        } catch (BulkheadFullException e) {
            return Map.of(
                "status", "rejected",
                "message", e.getMessage()
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
    
    @PostMapping("/payment/{paymentId}")
    public Map<String, Object> callPaymentService(@PathVariable String paymentId) {
        try {
            String response = bulkheadService.callPaymentService(paymentId);
            return Map.of(
                "status", "success",
                "response", response
            );
        } catch (BulkheadFullException e) {
            return Map.of(
                "status", "rejected",
                "message", e.getMessage()
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
    
    @PostMapping("/async/{requestId}")
    public Map<String, Object> callAsyncService(@PathVariable String requestId) {
        try {
            CompletableFuture<String> future = bulkheadService.callAsyncService(requestId);
            String response = future.get(2, TimeUnit.SECONDS);
            return Map.of(
                "status", "success",
                "response", response
            );
        } catch (BulkheadFullException e) {
            return Map.of(
                "status", "rejected",
                "message", e.getMessage()
            );
        } catch (TimeoutException e) {
            return Map.of(
                "status", "timeout",
                "message", "Request timed out"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
    
    @GetMapping("/metrics")
    public Map<String, BulkheadMetrics> getMetrics() {
        return bulkheadService.getAllMetrics();
    }
}
