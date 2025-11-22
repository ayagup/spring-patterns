package com.example.resilience.bulkhead;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Bulkhead Isolation Pattern Implementation
 * 
 * Purpose: Isolates resources to prevent cascading failures across different parts of the system.
 * Limits resource consumption by partitioning the system into isolated pools.
 * 
 * Key Components:
 * 1. Bulkhead - Resource isolation container
 * 2. ThreadPoolBulkhead - Thread pool-based isolation
 * 3. SemaphoreBulkhead - Semaphore-based isolation
 * 4. BulkheadMetrics - Track resource usage
 * 
 * Features:
 * - Thread pool isolation
 * - Semaphore-based limiting
 * - Resource pool management
 * - Queue management for pending requests
 * - Bulkhead metrics tracking
 */

// Bulkhead Interface
interface Bulkhead {
    <T> T execute(Supplier<T> operation);
    String getBulkheadType();
    int getAvailableCapacity();
}

// Semaphore-based Bulkhead
class SemaphoreBulkhead implements Bulkhead {
    private final String name;
    private final Semaphore semaphore;
    private final int maxConcurrentCalls;
    private final Duration maxWaitDuration;
    
    public SemaphoreBulkhead(String name, int maxConcurrentCalls, Duration maxWaitDuration) {
        this.name = name;
        this.maxConcurrentCalls = maxConcurrentCalls;
        this.maxWaitDuration = maxWaitDuration;
        this.semaphore = new Semaphore(maxConcurrentCalls);
    }
    
    @Override
    public <T> T execute(Supplier<T> operation) {
        try {
            boolean acquired = semaphore.tryAcquire(maxWaitDuration.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new BulkheadFullException("Bulkhead '" + name + "' is full");
            }
            
            try {
                return operation.get();
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Bulkhead execution interrupted", e);
        }
    }
    
    @Override
    public String getBulkheadType() {
        return "Semaphore(max=" + maxConcurrentCalls + ")";
    }
    
    @Override
    public int getAvailableCapacity() {
        return semaphore.availablePermits();
    }
}

// Thread Pool-based Bulkhead
class ThreadPoolBulkhead implements Bulkhead {
    private final String name;
    private final ThreadPoolExecutor executor;
    private final int maxThreadPoolSize;
    
    public ThreadPoolBulkhead(String name, int corePoolSize, int maxPoolSize, int queueCapacity) {
        this.name = name;
        this.maxThreadPoolSize = maxPoolSize;
        this.executor = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueCapacity),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }
    
    @Override
    public <T> T execute(Supplier<T> operation) {
        Future<T> future = executor.submit(operation::get);
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Bulkhead execution interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Bulkhead execution failed", e.getCause());
        } catch (RejectedExecutionException e) {
            throw new BulkheadFullException("Bulkhead '" + name + "' thread pool is full");
        }
    }
    
    @Override
    public String getBulkheadType() {
        return "ThreadPool(max=" + maxThreadPoolSize + ")";
    }
    
    @Override
    public int getAvailableCapacity() {
        return executor.getMaximumPoolSize() - executor.getActiveCount();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Bulkhead Exception
class BulkheadFullException extends RuntimeException {
    public BulkheadFullException(String message) {
        super(message);
    }
}

// Bulkhead Service
@Component
class BulkheadService {
    private final SemaphoreBulkhead paymentBulkhead;
    private final ThreadPoolBulkhead orderBulkhead;
    
    public BulkheadService() {
        this.paymentBulkhead = new SemaphoreBulkhead("payment", 5, Duration.ofSeconds(2));
        this.orderBulkhead = new ThreadPoolBulkhead("order", 3, 10, 20);
    }
    
    public String processPayment(String paymentId) {
        return paymentBulkhead.execute(() -> {
            // Simulate payment processing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Payment " + paymentId + " processed";
        });
    }
    
    public String processOrder(String orderId) {
        return orderBulkhead.execute(() -> {
            // Simulate order processing
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Order " + orderId + " processed";
        });
    }
}

/**
 * Health Check Pattern Implementation
 * 
 * Purpose: Monitors system health and dependencies to ensure reliability.
 * Provides readiness and liveness probes for orchestration platforms.
 */

enum HealthStatus {
    UP, DOWN, DEGRADED, UNKNOWN
}

class HealthCheckResult {
    private final String name;
    private final HealthStatus status;
    private final String message;
    private final long responseTime;
    
    public HealthCheckResult(String name, HealthStatus status, String message, long responseTime) {
        this.name = name;
        this.status = status;
        this.message = message;
        this.responseTime = responseTime;
    }
    
    public String getName() { return name; }
    public HealthStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public long getResponseTime() { return responseTime; }
}

interface HealthIndicator {
    HealthCheckResult check();
}

class DatabaseHealthIndicator implements HealthIndicator {
    @Override
    public HealthCheckResult check() {
        long start = System.currentTimeMillis();
        try {
            // Simulate database ping
            Thread.sleep(10);
            boolean isUp = Math.random() > 0.1; // 90% uptime
            long responseTime = System.currentTimeMillis() - start;
            
            return new HealthCheckResult(
                "database",
                isUp ? HealthStatus.UP : HealthStatus.DOWN,
                isUp ? "Database connection successful" : "Database connection failed",
                responseTime
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new HealthCheckResult("database", HealthStatus.DOWN, "Check interrupted", 0);
        }
    }
}

class ExternalApiHealthIndicator implements HealthIndicator {
    @Override
    public HealthCheckResult check() {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(20);
            boolean isUp = Math.random() > 0.2; // 80% uptime
            long responseTime = System.currentTimeMillis() - start;
            
            return new HealthCheckResult(
                "external-api",
                isUp ? HealthStatus.UP : HealthStatus.DOWN,
                isUp ? "API responding" : "API not responding",
                responseTime
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new HealthCheckResult("external-api", HealthStatus.DOWN, "Check interrupted", 0);
        }
    }
}

@Component
class HealthCheckService {
    private final List<HealthIndicator> indicators = new ArrayList<>();
    
    public HealthCheckService() {
        indicators.add(new DatabaseHealthIndicator());
        indicators.add(new ExternalApiHealthIndicator());
    }
    
    public Map<String, HealthCheckResult> checkAll() {
        Map<String, HealthCheckResult> results = new HashMap<>();
        for (HealthIndicator indicator : indicators) {
            HealthCheckResult result = indicator.check();
            results.put(result.getName(), result);
        }
        return results;
    }
    
    public HealthStatus getOverallHealth() {
        boolean allUp = true;
        boolean anyDegraded = false;
        
        for (HealthIndicator indicator : indicators) {
            HealthCheckResult result = indicator.check();
            if (result.getStatus() == HealthStatus.DOWN) {
                return HealthStatus.DOWN;
            } else if (result.getStatus() == HealthStatus.DEGRADED) {
                anyDegraded = true;
            } else if (result.getStatus() != HealthStatus.UP) {
                allUp = false;
            }
        }
        
        if (anyDegraded) return HealthStatus.DEGRADED;
        return allUp ? HealthStatus.UP : HealthStatus.UNKNOWN;
    }
}

/**
 * Graceful Degradation Pattern Implementation
 * 
 * Purpose: Maintains partial functionality when full service is unavailable.
 * Provides degraded service levels instead of complete failure.
 */

enum ServiceLevel {
    FULL, REDUCED, MINIMAL, UNAVAILABLE
}

class GracefulDegradationService {
    private ServiceLevel currentLevel = ServiceLevel.FULL;
    
    public String getContent(String contentId) {
        switch (currentLevel) {
            case FULL:
                return getFullContent(contentId);
            case REDUCED:
                return getReducedContent(contentId);
            case MINIMAL:
                return getMinimalContent(contentId);
            default:
                return "Service temporarily unavailable";
        }
    }
    
    private String getFullContent(String contentId) {
        return "Full content for " + contentId + " with images, videos, and interactive features";
    }
    
    private String getReducedContent(String contentId) {
        return "Reduced content for " + contentId + " (text and basic images only)";
    }
    
    private String getMinimalContent(String contentId) {
        return "Minimal content for " + contentId + " (text only)";
    }
    
    public void setServiceLevel(ServiceLevel level) {
        this.currentLevel = level;
        System.out.println("Service level changed to: " + level);
    }
    
    public ServiceLevel getCurrentLevel() {
        return currentLevel;
    }
}

/**
 * Fail Fast Pattern Implementation
 * 
 * Purpose: Detects invalid conditions early and fails immediately
 * rather than attempting doomed operations.
 */

class FailFastValidator {
    public void validate(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }
        
        if (input.length() > 100) {
            throw new IllegalArgumentException("Input exceeds maximum length of 100 characters");
        }
    }
    
    public void validateEmail(String email) {
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    public <T> T executeWithValidation(Supplier<T> operation, Runnable... validations) {
        // Run all validations first (fail fast)
        for (Runnable validation : validations) {
            validation.run();
        }
        
        // Only execute operation if all validations pass
        return operation.get();
    }
}

/**
 * Fail Safe Pattern Implementation
 * 
 * Purpose: Handles errors gracefully by providing safe defaults
 * and preventing error propagation.
 */

class FailSafeExecutor {
    public <T> T executeSafely(Supplier<T> operation, T defaultValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            System.out.println("Operation failed safely: " + e.getMessage());
            return defaultValue;
        }
    }
    
    public <T> Optional<T> executeSafelyOptional(Supplier<T> operation) {
        try {
            return Optional.ofNullable(operation.get());
        } catch (Exception e) {
            System.out.println("Operation failed, returning empty: " + e.getMessage());
            return Optional.empty();
        }
    }
}

/**
 * Throttling Pattern Implementation
 * 
 * Purpose: Controls request rate by queuing and processing requests
 * at a controlled pace.
 */

class ThrottlingService {
    private final BlockingQueue<Runnable> queue;
    private final ScheduledExecutorService scheduler;
    private final long delayBetweenRequests;
    
    public ThrottlingService(int queueCapacity, long delayBetweenRequestsMs) {
        this.queue = new LinkedBlockingQueue<>(queueCapacity);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.delayBetweenRequests = delayBetweenRequestsMs;
        startProcessing();
    }
    
    private void startProcessing() {
        scheduler.scheduleAtFixedRate(() -> {
            Runnable task = queue.poll();
            if (task != null) {
                task.run();
            }
        }, 0, delayBetweenRequests, TimeUnit.MILLISECONDS);
    }
    
    public boolean submit(Runnable task) {
        return queue.offer(task);
    }
    
    public int getQueueSize() {
        return queue.size();
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
}

/**
 * Debouncing Pattern Implementation
 * 
 * Purpose: Delays execution until a quiet period has elapsed,
 * preventing excessive calls for rapidly changing inputs.
 */

class DebouncingService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingTask;
    private final long debounceDelayMs;
    
    public DebouncingService(long debounceDelayMs) {
        this.debounceDelayMs = debounceDelayMs;
    }
    
    public synchronized void debounce(Runnable task) {
        if (pendingTask != null && !pendingTask.isDone()) {
            pendingTask.cancel(false);
        }
        
        pendingTask = scheduler.schedule(task, debounceDelayMs, TimeUnit.MILLISECONDS);
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
}

/**
 * Cache Stampede Prevention Pattern Implementation
 * 
 * Purpose: Prevents multiple simultaneous cache misses from overwhelming
 * the backend by coordinating cache refresh operations.
 */

class CacheStampedePreventionService<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<K, CompletableFuture<V>> loadingKeys = new ConcurrentHashMap<>();
    private final long cacheTtlMs;
    
    public CacheStampedePreventionService(long cacheTtlMs) {
        this.cacheTtlMs = cacheTtlMs;
    }
    
    public V get(K key, Supplier<V> loader) {
        V cachedValue = cache.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // Check if another thread is already loading this key
        CompletableFuture<V> loadingFuture = loadingKeys.computeIfAbsent(key, k -> {
            return CompletableFuture.supplyAsync(() -> {
                V value = loader.get();
                cache.put(key, value);
                return value;
            }).whenComplete((result, throwable) -> {
                loadingKeys.remove(key);
            });
        });
        
        try {
            return loadingFuture.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load value for key: " + key, e);
        }
    }
}

/**
 * Main demonstration class
 */
public class ResilientPatternsSuite {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Resilient Patterns Suite Demo ===\n");
        
        // Bulkhead Pattern
        System.out.println("1. Bulkhead Pattern:");
        SemaphoreBulkhead bulkhead = new SemaphoreBulkhead("test", 2, Duration.ofSeconds(1));
        System.out.println("   Available capacity: " + bulkhead.getAvailableCapacity());
        
        // Health Check Pattern
        System.out.println("\n2. Health Check Pattern:");
        HealthCheckService healthService = new HealthCheckService();
        System.out.println("   Overall health: " + healthService.getOverallHealth());
        
        // Graceful Degradation
        System.out.println("\n3. Graceful Degradation Pattern:");
        GracefulDegradationService degradationService = new GracefulDegradationService();
        degradationService.setServiceLevel(ServiceLevel.REDUCED);
        System.out.println("   " + degradationService.getContent("content123"));
        
        // Fail Fast
        System.out.println("\n4. Fail Fast Pattern:");
        FailFastValidator validator = new FailFastValidator();
        try {
            validator.validate("");
        } catch (IllegalArgumentException e) {
            System.out.println("   Validation failed fast: " + e.getMessage());
        }
        
        // Fail Safe
        System.out.println("\n5. Fail Safe Pattern:");
        FailSafeExecutor failSafe = new FailSafeExecutor();
        String result = failSafe.executeSafely(() -> {
            throw new RuntimeException("Operation failed");
        }, "Default safe value");
        System.out.println("   Result: " + result);
        
        // Cache Stampede Prevention
        System.out.println("\n6. Cache Stampede Prevention:");
        CacheStampedePreventionService<String, String> cacheService = 
            new CacheStampedePreventionService<>(60000);
        String value = cacheService.get("key1", () -> "Loaded value");
        System.out.println("   Cache value: " + value);
        
        System.out.println("\n=== Demo Complete ===");
    }
}
