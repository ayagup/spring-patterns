# Resilience Patterns for Spring Applications

This directory contains comprehensive implementations of 13 essential resilience patterns for building robust, fault-tolerant Spring applications.

## Table of Contents

1. [Circuit Breaker Pattern](#1-circuit-breaker-pattern)
2. [Fallback Pattern](#2-fallback-pattern)
3. [Timeout Pattern](#3-timeout-pattern)
4. [Retry Pattern](#4-retry-pattern)
5. [Rate Limiter Pattern](#5-rate-limiter-pattern)
6. [Bulkhead Isolation Pattern](#6-bulkhead-isolation-pattern)
7. [Health Check Pattern](#7-health-check-pattern)
8. [Graceful Degradation Pattern](#8-graceful-degradation-pattern)
9. [Fail Fast Pattern](#9-fail-fast-pattern)
10. [Fail Safe Pattern](#10-fail-safe-pattern)
11. [Throttling Pattern](#11-throttling-pattern)
12. [Debouncing Pattern](#12-debouncing-pattern)
13. [Cache Stampede Prevention Pattern](#13-cache-stampede-prevention-pattern)

---

## 1. Circuit Breaker Pattern

**File:** `CircuitBreakerPattern.java`

### Purpose
Prevents cascading failures by detecting failures and preventing further calls to a failing service. Implements a state machine with CLOSED, OPEN, and HALF_OPEN states.

### Key Components
- **CircuitBreaker**: Main circuit breaker with state management
- **CircuitBreakerState**: Enum for states (CLOSED, OPEN, HALF_OPEN)
- **CircuitBreakerConfig**: Configuration for thresholds and timeouts
- **CircuitBreakerMetrics**: Tracking success/failure metrics

### Configuration Example
```java
CircuitBreakerConfig config = new CircuitBreakerConfig(
    3,                          // failureThreshold
    2,                          // successThreshold
    Duration.ofSeconds(60),     // timeout
    Duration.ofSeconds(5),      // callTimeout
    10                          // slidingWindowSize
);

CircuitBreaker circuitBreaker = new CircuitBreaker("payment-service", config);
```

### Usage Example
```java
try {
    String result = circuitBreaker.execute(() -> 
        externalService.processPayment(paymentId)
    );
} catch (CircuitBreakerOpenException e) {
    // Circuit is open, use fallback
    return fallbackPaymentProcessor.process(paymentId);
}
```

### Features
- Automatic state transitions based on failure threshold
- Configurable timeout for OPEN state
- Half-open state for testing service recovery
- Metrics tracking (success rate, failure rate, call counts)
- Thread-safe implementation

### Spring Configuration
```java
@Configuration
public class ResilienceConfig {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return new CircuitBreakerRegistry();
    }
}
```

---

## 2. Fallback Pattern

**File:** `FallbackPattern.java`

### Purpose
Provides alternative responses when primary service fails, ensuring graceful degradation and preventing complete service outage.

### Key Components
- **FallbackHandler**: Executes primary and fallback logic
- **FallbackStrategy**: Different fallback strategies
- **FallbackChain**: Chain of fallbacks with priority
- **FallbackMetrics**: Track fallback usage

### Fallback Strategies
1. **Default Value Fallback**: Returns a predefined default value
2. **Cached Value Fallback**: Returns data from cache
3. **Alternative Service Fallback**: Routes to backup service
4. **Static Content Fallback**: Returns static content

### Usage Example
```java
FallbackChain<User> fallbackChain = new FallbackChain<>("UserService", metrics);

fallbackChain
    .addStrategy(new CachedValueFallback<>(cache, userId))
    .addStrategy(new AlternativeServiceFallback<>(() -> getFromBackupDatabase(userId)))
    .addStrategy(new DefaultValueFallback<>(new User("guest", "Guest User")));

FallbackHandler<User> handler = new FallbackHandler<>(
    "UserService",
    () -> getPrimaryUser(userId),
    fallbackChain,
    metrics
);

User user = handler.execute();
```

### Benefits
- Prevents complete service outage
- Multiple fallback levels
- Cached responses for better performance
- Metrics for monitoring fallback usage

---

## 3. Timeout Pattern

**File:** `TimeoutPattern.java`

### Purpose
Prevents indefinite waiting by setting time limits on operations, ensuring system responsiveness and preventing resource exhaustion.

### Key Components
- **TimeoutHandler**: Manages timeout execution
- **TimeoutConfig**: Configuration for timeout settings
- **TimeoutException**: Custom exception for timeout scenarios
- **TimeoutMetrics**: Track timeout occurrences

### Usage Example
```java
TimeoutConfig config = new TimeoutConfig(Duration.ofSeconds(5));
TimeoutHandler<String> handler = new TimeoutHandler<>("database-query", config, metrics);

try {
    String result = handler.execute(() -> performDatabaseQuery(query));
} catch (TimeoutException e) {
    // Handle timeout
    return getCachedResult(query);
}
```

### Timeout with Fallback
```java
TimeoutConfig config = new TimeoutConfig(Duration.ofSeconds(2), true, true);
TimeoutHandler<String> handler = new TimeoutHandler<>("api-call", config, metrics);

String result = handler.execute(
    () -> callExternalApi(),
    () -> "Cached API response"  // Fallback
);
```

### Features
- Configurable timeout duration
- Thread interruption on timeout
- Fallback on timeout
- Async execution with timeout
- Timeout metrics tracking

---

## 4. Retry Pattern

**File:** `RetryPattern.java`

### Purpose
Automatically retries failed operations with configurable strategies, improving reliability in the face of transient failures.

### Backoff Strategies

#### 1. Fixed Backoff
```java
BackoffStrategy fixed = new FixedBackoffStrategy(1000); // 1 second between retries
```

#### 2. Exponential Backoff
```java
BackoffStrategy exponential = new ExponentialBackoffStrategy(
    100,    // initialDelayMs
    2.0,    // multiplier
    10000   // maxDelayMs
);
// Results in: 100ms, 200ms, 400ms, 800ms, 1600ms...
```

#### 3. Exponential Backoff with Jitter
```java
BackoffStrategy jitter = new ExponentialBackoffWithJitterStrategy(100, 2.0, 10000);
// Adds randomness to prevent thundering herd
```

#### 4. Linear Backoff
```java
BackoffStrategy linear = new LinearBackoffStrategy(500, 200);
// Results in: 500ms, 700ms, 900ms, 1100ms...
```

### Usage Example
```java
RetryPolicy policy = new RetryPolicy(5, new ExponentialBackoffStrategy());
RetryHandler<String> handler = new RetryHandler<>("unreliable-service", policy, metrics);

try {
    String result = handler.execute(() -> callUnreliableService());
} catch (RetryExhaustedException e) {
    // All retries failed
    log.error("Failed after {} attempts", e.getAttempts());
}
```

### Conditional Retry
```java
Predicate<Exception> retryPredicate = e -> 
    e instanceof RuntimeException && e.getMessage().contains("Temporary");

RetryPolicy policy = new RetryPolicy(3, new FixedBackoffStrategy(1000), retryPredicate, null);
```

---

## 5. Rate Limiter Pattern

**File:** `RateLimiterPattern.java`

### Purpose
Controls the rate of requests to prevent system overload, ensuring fair resource allocation and protecting against abuse.

### Rate Limiting Algorithms

#### 1. Token Bucket
```java
// Capacity: 100 tokens, Refill: 10 tokens/second
TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(100, 10);

if (limiter.tryAcquire()) {
    processRequest();
} else {
    rejectRequest("Rate limit exceeded");
}
```

**Use Case**: Allows bursts up to capacity, smooth refill rate

#### 2. Sliding Window
```java
// Max 50 requests per minute
SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(50, Duration.ofMinutes(1));

if (limiter.tryAcquire()) {
    processRequest();
}
```

**Use Case**: Precise rate limiting, no boundary issues

#### 3. Fixed Window
```java
// Max 1000 requests per hour
FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(1000, Duration.ofHours(1));
```

**Use Case**: Simple implementation, efficient memory usage

#### 4. Leaky Bucket
```java
// Capacity: 100, Leak rate: 10 requests/second
LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(100, 10);
```

**Use Case**: Enforces constant output rate

### Per-User Rate Limiting
```java
@Component
public class RateLimitedService {
    private final RateLimiterRegistry registry;
    
    public String processRequest(String userId) {
        String key = "user:" + userId;
        
        if (!registry.tryAcquire(key)) {
            throw new RateLimitException("Rate limit exceeded for user: " + userId);
        }
        
        return processUserRequest(userId);
    }
}
```

---

## 6. Bulkhead Isolation Pattern

**File:** `ResilientPatternsSuite.java`

### Purpose
Isolates resources to prevent cascading failures across different parts of the system. Limits resource consumption by partitioning the system into isolated pools.

### Bulkhead Types

#### 1. Semaphore-based Bulkhead
```java
// Max 5 concurrent calls, wait up to 2 seconds
SemaphoreBulkhead bulkhead = new SemaphoreBulkhead("payment", 5, Duration.ofSeconds(2));

try {
    String result = bulkhead.execute(() -> processPayment(paymentId));
} catch (BulkheadFullException e) {
    // Bulkhead is full
    return queueForLaterProcessing(paymentId);
}
```

**Use Case**: Lightweight, low overhead, good for in-process isolation

#### 2. Thread Pool Bulkhead
```java
// Core: 3 threads, Max: 10 threads, Queue: 20
ThreadPoolBulkhead bulkhead = new ThreadPoolBulkhead("order", 3, 10, 20);

String result = bulkhead.execute(() -> processOrder(orderId));
```

**Use Case**: True isolation, protects from thread starvation

### Benefits
- Prevents resource exhaustion
- Isolates failures to specific components
- Configurable capacity per service
- Queue management for pending requests

---

## 7. Health Check Pattern

**File:** `ResilientPatternsSuite.java`

### Purpose
Monitors system health and dependencies to ensure reliability. Provides readiness and liveness probes for orchestration platforms.

### Health Indicators

```java
public class DatabaseHealthIndicator implements HealthIndicator {
    @Override
    public HealthCheckResult check() {
        try {
            // Check database connection
            dataSource.getConnection().isValid(1);
            return new HealthCheckResult(
                "database",
                HealthStatus.UP,
                "Database connection successful",
                responseTime
            );
        } catch (SQLException e) {
            return new HealthCheckResult(
                "database",
                HealthStatus.DOWN,
                "Database connection failed: " + e.getMessage(),
                responseTime
            );
        }
    }
}
```

### Health Status Levels
- **UP**: Service is healthy and operating normally
- **DOWN**: Service is unavailable
- **DEGRADED**: Service is partially functional
- **UNKNOWN**: Health status cannot be determined

### Aggregate Health Check
```java
@Component
public class HealthCheckService {
    private final List<HealthIndicator> indicators;
    
    public HealthStatus getOverallHealth() {
        Map<String, HealthCheckResult> results = checkAll();
        
        // If any critical component is DOWN, overall status is DOWN
        boolean hasFailures = results.values().stream()
            .anyMatch(r -> r.getStatus() == HealthStatus.DOWN);
            
        return hasFailures ? HealthStatus.DOWN : HealthStatus.UP;
    }
}
```

### Spring Boot Actuator Integration
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        HealthCheckResult result = performHealthCheck();
        
        return result.getStatus() == HealthStatus.UP
            ? Health.up().withDetail("responseTime", result.getResponseTime()).build()
            : Health.down().withDetail("error", result.getMessage()).build();
    }
}
```

---

## 8. Graceful Degradation Pattern

**File:** `ResilientPatternsSuite.java`

### Purpose
Maintains partial functionality when full service is unavailable. Provides degraded service levels instead of complete failure.

### Service Levels
```java
public enum ServiceLevel {
    FULL,         // All features available
    REDUCED,      // Some features disabled
    MINIMAL,      // Only essential features
    UNAVAILABLE   // Service completely unavailable
}
```

### Implementation Example
```java
public class GracefulDegradationService {
    private ServiceLevel currentLevel = ServiceLevel.FULL;
    
    public String getContent(String contentId) {
        switch (currentLevel) {
            case FULL:
                return getFullContent(contentId); // Images, videos, interactive
            case REDUCED:
                return getReducedContent(contentId); // Text and basic images
            case MINIMAL:
                return getMinimalContent(contentId); // Text only
            default:
                return "Service temporarily unavailable";
        }
    }
    
    public void adjustServiceLevel(SystemMetrics metrics) {
        if (metrics.getCpuUsage() > 90) {
            setServiceLevel(ServiceLevel.REDUCED);
        } else if (metrics.getCpuUsage() > 95) {
            setServiceLevel(ServiceLevel.MINIMAL);
        }
    }
}
```

### Use Cases
- High system load
- Dependency failures
- Resource constraints
- Planned maintenance

---

## 9. Fail Fast Pattern

**File:** `ResilientPatternsSuite.java`

### Purpose
Detects invalid conditions early and fails immediately rather than attempting doomed operations.

### Validation Examples
```java
public class FailFastValidator {
    public void validateOrder(Order order) {
        // Fail fast on null or invalid input
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        
        if (order.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Order total must be greater than zero");
        }
        
        // Only proceed if all validations pass
    }
    
    public <T> T executeWithValidation(
        Supplier<T> operation, 
        Runnable... validations
    ) {
        // Run all validations first (fail fast)
        for (Runnable validation : validations) {
            validation.run();
        }
        
        // Only execute operation if all validations pass
        return operation.get();
    }
}
```

### Benefits
- Reduces wasted resources
- Faster error detection
- Clearer error messages
- Prevents cascading failures

---

## 10. Fail Safe Pattern

**File:** `ResilientPatternsSuite.java`

### Purpose
Handles errors gracefully by providing safe defaults and preventing error propagation.

### Implementation Examples
```java
public class FailSafeExecutor {
    // Return default value on failure
    public <T> T executeSafely(Supplier<T> operation, T defaultValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.warn("Operation failed safely: {}", e.getMessage());
            return defaultValue;
        }
    }
    
    // Return Optional on failure
    public <T> Optional<T> executeSafelyOptional(Supplier<T> operation) {
        try {
            return Optional.ofNullable(operation.get());
        } catch (Exception e) {
            log.warn("Operation failed, returning empty: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    // Execute with error suppression
    public void executeSilently(Runnable operation) {
        try {
            operation.run();
        } catch (Exception e) {
            log.debug("Operation failed silently: {}", e.getMessage());
            // Suppress exception
        }
    }
}
```

### Use Cases
- Non-critical operations
- Optional features
- Background tasks
- Logging and monitoring

---

## 11. Throttling Pattern

**File:** `ResilientPatternsSuite.java`

### Purpose
Controls request rate by queuing and processing requests at a controlled pace.

### Implementation
```java
public class ThrottlingService {
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
}
```

### Usage Example
```java
// Process max 10 requests per second
ThrottlingService throttler = new ThrottlingService(100, 100);

// Submit requests
throttler.submit(() -> processRequest(request1));
throttler.submit(() -> processRequest(request2));
```

### Difference from Rate Limiting
- **Rate Limiting**: Rejects requests when limit exceeded
- **Throttling**: Queues requests and processes at controlled rate

---

## 12. Debouncing Pattern

**File:** `ResilientPatternsSuite.java`

### Purpose
Delays execution until a quiet period has elapsed, preventing excessive calls for rapidly changing inputs.

### Implementation
```java
public class DebouncingService {
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pendingTask;
    private final long debounceDelayMs;
    
    public synchronized void debounce(Runnable task) {
        // Cancel previous pending task
        if (pendingTask != null && !pendingTask.isDone()) {
            pendingTask.cancel(false);
        }
        
        // Schedule new task
        pendingTask = scheduler.schedule(
            task, 
            debounceDelayMs, 
            TimeUnit.MILLISECONDS
        );
    }
}
```

### Usage Examples

#### 1. Search Input Debouncing
```java
// Wait 300ms after user stops typing before searching
DebouncingService searchDebouncer = new DebouncingService(300);

public void onSearchInputChange(String query) {
    searchDebouncer.debounce(() -> performSearch(query));
}
```

#### 2. Auto-save Debouncing
```java
// Auto-save 2 seconds after last edit
DebouncingService autoSaveDebouncer = new DebouncingService(2000);

public void onDocumentEdit() {
    autoSaveDebouncer.debounce(() -> saveDocument());
}
```

### Benefits
- Reduces unnecessary API calls
- Improves performance
- Prevents server overload
- Better user experience

---

## 13. Cache Stampede Prevention Pattern

**File:** `ResilientPatternsSuite.java`

### Purpose
Prevents multiple simultaneous cache misses from overwhelming the backend by coordinating cache refresh operations.

### The Problem
When a popular cache entry expires:
1. Multiple requests arrive simultaneously
2. All requests find cache miss
3. All requests query backend simultaneously
4. Backend gets overwhelmed (stampede)

### Implementation
```java
public class CacheStampedePreventionService<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<K, CompletableFuture<V>> loadingKeys = new ConcurrentHashMap<>();
    
    public V get(K key, Supplier<V> loader) {
        // Check cache first
        V cachedValue = cache.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // Check if another thread is already loading this key
        CompletableFuture<V> loadingFuture = loadingKeys.computeIfAbsent(key, k -> {
            return CompletableFuture.supplyAsync(() -> {
                V value = loader.get(); // Only ONE thread loads
                cache.put(key, value);
                return value;
            }).whenComplete((result, throwable) -> {
                loadingKeys.remove(key);
            });
        });
        
        try {
            return loadingFuture.get(); // Other threads wait for the result
        } catch (Exception e) {
            throw new RuntimeException("Failed to load value", e);
        }
    }
}
```

### Advanced: Probabilistic Early Expiration
```java
public class ProbabilisticCacheRefresh<K, V> {
    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final long ttlMs;
    private final double beta = 1.0; // Tuning parameter
    
    public V get(K key, Supplier<V> loader) {
        CacheEntry<V> entry = cache.get(key);
        
        if (entry != null) {
            long now = System.currentTimeMillis();
            long delta = now - entry.getCreatedAt();
            long timeToExpire = ttlMs - delta;
            
            // Probabilistically refresh before actual expiration
            // Higher load (lower timeToExpire) = higher probability
            double random = Math.random();
            double xfetch = delta * beta * Math.log(random);
            
            if (timeToExpire <= xfetch) {
                // Trigger early refresh
                refreshAsync(key, loader);
            }
            
            return entry.getValue();
        }
        
        // Cache miss - load normally
        return loadAndCache(key, loader);
    }
}
```

---

## Dependencies

Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Resilience4j (Optional - for production use) -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot2</artifactId>
        <version>2.1.0</version>
    </dependency>
    
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-circuitbreaker</artifactId>
        <version>2.1.0</version>
    </dependency>
    
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-ratelimiter</artifactId>
        <version>2.1.0</version>
    </dependency>
    
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-bulkhead</artifactId>
        <version>2.1.0</version>
    </dependency>
    
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-retry</artifactId>
        <version>2.1.0</version>
    </dependency>
    
    <!-- Spring Boot Actuator for Health Checks -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Micrometer for Metrics -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

---

## Best Practices

### 1. Combine Patterns
```java
@Service
public class ResilientService {
    private final CircuitBreaker circuitBreaker;
    private final RateLimiter rateLimiter;
    private final RetryPolicy retryPolicy;
    
    public String processRequest(String requestId) {
        // Apply rate limiting first
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitException("Too many requests");
        }
        
        // Apply circuit breaker
        return circuitBreaker.execute(() -> {
            // Apply retry logic
            return retryHandler.execute(() -> {
                // Actual business logic
                return externalService.call(requestId);
            });
        });
    }
}
```

### 2. Monitor Metrics
```java
@Component
public class ResilienceMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    public void recordCircuitBreakerState(String name, CircuitBreakerState state) {
        meterRegistry.gauge("circuit.breaker.state", 
            Tags.of("name", name), 
            state.ordinal());
    }
    
    public void recordRateLimitRejection(String endpoint) {
        meterRegistry.counter("rate.limit.rejected", 
            Tags.of("endpoint", endpoint)).increment();
    }
}
```

### 3. Configure Timeouts Appropriately
- **Database queries**: 1-5 seconds
- **HTTP API calls**: 2-10 seconds
- **External service calls**: 5-30 seconds
- **Batch operations**: 30-300 seconds

### 4. Set Reasonable Retry Limits
- **Transient errors**: 3-5 retries
- **Network issues**: 2-3 retries
- **Never retry**: 4xx errors (except 429)
- **Always retry**: 503, 504

### 5. Circuit Breaker Configuration
```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 60s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
```

---

## Testing Strategies

### 1. Testing Circuit Breaker
```java
@Test
public void testCircuitBreakerOpens() {
    CircuitBreaker cb = new CircuitBreaker("test", config);
    
    // Cause failures to open circuit
    for (int i = 0; i < config.getFailureThreshold(); i++) {
        try {
            cb.execute(() -> { throw new RuntimeException("Fail"); });
        } catch (Exception e) {
            // Expected
        }
    }
    
    assertEquals(CircuitBreakerState.OPEN, cb.getState());
    
    // Verify circuit rejects calls
    assertThrows(CircuitBreakerOpenException.class, 
        () -> cb.execute(() -> "success"));
}
```

### 2. Testing Rate Limiter
```java
@Test
public void testRateLimiterRejects() {
    RateLimiter limiter = new TokenBucketRateLimiter(5, 1);
    
    // First 5 should succeed
    for (int i = 0; i < 5; i++) {
        assertTrue(limiter.tryAcquire());
    }
    
    // 6th should fail
    assertFalse(limiter.tryAcquire());
}
```

### 3. Testing Retry with Backoff
```java
@Test
public void testExponentialBackoff() {
    BackoffStrategy backoff = new ExponentialBackoffStrategy(100, 2.0, 10000);
    
    assertEquals(100, backoff.calculateBackoff(1));
    assertEquals(200, backoff.calculateBackoff(2));
    assertEquals(400, backoff.calculateBackoff(3));
    assertEquals(800, backoff.calculateBackoff(4));
}
```

---

## Production Checklist

- [ ] Configure appropriate timeouts for all external calls
- [ ] Implement circuit breakers for all external dependencies
- [ ] Add fallbacks for critical services
- [ ] Set up rate limiting for public APIs
- [ ] Configure retry policies with exponential backoff
- [ ] Implement health checks for all dependencies
- [ ] Add metrics and monitoring for all patterns
- [ ] Set up alerts for circuit breaker state changes
- [ ] Test failure scenarios in staging environment
- [ ] Document degraded functionality behavior
- [ ] Configure bulkheads for resource-intensive operations
- [ ] Implement cache stampede prevention for hot data
- [ ] Add debouncing for user-triggered operations
- [ ] Test system behavior under high load

---

## Monitoring and Observability

### Key Metrics to Track

1. **Circuit Breaker Metrics**
   - State (CLOSED/OPEN/HALF_OPEN)
   - Failure rate
   - Success rate after recovery
   - Time spent in each state

2. **Rate Limiter Metrics**
   - Requests allowed vs rejected
   - Current token/permit count
   - Rejection rate by endpoint

3. **Retry Metrics**
   - Number of retries per operation
   - Retry success rate
   - Average attempts before success

4. **Timeout Metrics**
   - Timeout occurrences
   - Average response time
   - Timeout rate by operation

5. **Bulkhead Metrics**
   - Available capacity
   - Queue depth
   - Rejected requests

### Example Dashboard Queries (Prometheus)

```promql
# Circuit breaker failure rate
rate(circuit_breaker_failure_total[5m]) / rate(circuit_breaker_calls_total[5m])

# Rate limit rejection rate
rate(rate_limit_rejected_total[1m])

# Average retry attempts
rate(retry_attempts_total[5m]) / rate(retry_operations_total[5m])
```

---

## References

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Release It! by Michael Nygard](https://pragprog.com/titles/mnee2/release-it-second-edition/)
- [Building Microservices by Sam Newman](https://samnewman.io/books/building_microservices_2nd_edition/)
- [Circuit Breaker Pattern - Martin Fowler](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Spring Cloud Circuit Breaker](https://spring.io/projects/spring-cloud-circuitbreaker)
- [Hystrix Wiki](https://github.com/Netflix/Hystrix/wiki)

---

## License

These implementations are provided as educational examples for building resilient Spring applications.

---

## Contributing

Feel free to extend these patterns with additional features or create new resilience patterns based on your specific requirements.
