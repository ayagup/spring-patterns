package com.example.resilience.circuitbreaker;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Circuit Breaker Pattern Implementation
 * 
 * Purpose: Prevents cascading failures by detecting failures and preventing further calls
 * to a failing service. Implements state machine with CLOSED, OPEN, and HALF_OPEN states.
 * 
 * Key Components:
 * 1. CircuitBreaker - Main circuit breaker with state management
 * 2. CircuitBreakerState - Enum for states (CLOSED, OPEN, HALF_OPEN)
 * 3. CircuitBreakerConfig - Configuration for thresholds and timeouts
 * 4. CircuitBreakerMetrics - Tracking success/failure metrics
 * 
 * Features:
 * - Automatic state transitions based on failure threshold
 * - Configurable timeout for OPEN state
 * - Half-open state for testing service recovery
 * - Metrics tracking (success rate, failure rate, call counts)
 * - Thread-safe implementation
 */

// Circuit Breaker State
enum CircuitBreakerState {
    CLOSED,      // Normal operation, requests pass through
    OPEN,        // Circuit is open, requests fail fast
    HALF_OPEN    // Testing if service recovered
}

// Circuit Breaker Configuration
class CircuitBreakerConfig {
    private final int failureThreshold;           // Number of failures before opening
    private final int successThreshold;           // Successes needed to close from half-open
    private final Duration timeout;               // How long to stay in OPEN state
    private final Duration callTimeout;           // Timeout for individual calls
    private final int slidingWindowSize;          // Size of sliding window for metrics
    
    public CircuitBreakerConfig() {
        this(5, 2, Duration.ofSeconds(60), Duration.ofSeconds(5), 10);
    }
    
    public CircuitBreakerConfig(int failureThreshold, int successThreshold, 
                                Duration timeout, Duration callTimeout, int slidingWindowSize) {
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.timeout = timeout;
        this.callTimeout = callTimeout;
        this.slidingWindowSize = slidingWindowSize;
    }
    
    public int getFailureThreshold() { return failureThreshold; }
    public int getSuccessThreshold() { return successThreshold; }
    public Duration getTimeout() { return timeout; }
    public Duration getCallTimeout() { return callTimeout; }
    public int getSlidingWindowSize() { return slidingWindowSize; }
}

// Circuit Breaker Metrics
class CircuitBreakerMetrics {
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger totalCalls = new AtomicInteger(0);
    private final AtomicInteger rejectedCalls = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    public void recordSuccess(long responseTime) {
        successCount.incrementAndGet();
        totalCalls.incrementAndGet();
        totalResponseTime.addAndGet(responseTime);
    }
    
    public void recordFailure() {
        failureCount.incrementAndGet();
        totalCalls.incrementAndGet();
    }
    
    public void recordRejected() {
        rejectedCalls.incrementAndGet();
    }
    
    public void reset() {
        successCount.set(0);
        failureCount.set(0);
        totalCalls.set(0);
        totalResponseTime.set(0);
        // Don't reset rejectedCalls as it's cumulative
    }
    
    public int getSuccessCount() { return successCount.get(); }
    public int getFailureCount() { return failureCount.get(); }
    public int getTotalCalls() { return totalCalls.get(); }
    public int getRejectedCalls() { return rejectedCalls.get(); }
    
    public double getFailureRate() {
        int total = totalCalls.get();
        return total == 0 ? 0.0 : (double) failureCount.get() / total * 100;
    }
    
    public double getAverageResponseTime() {
        int total = totalCalls.get();
        return total == 0 ? 0.0 : (double) totalResponseTime.get() / total;
    }
}

// Circuit Breaker Exception
class CircuitBreakerOpenException extends RuntimeException {
    public CircuitBreakerOpenException(String circuitBreakerName) {
        super("Circuit breaker '" + circuitBreakerName + "' is OPEN");
    }
}

// Main Circuit Breaker Implementation
class CircuitBreaker {
    private final String name;
    private final CircuitBreakerConfig config;
    private final CircuitBreakerMetrics metrics;
    private volatile CircuitBreakerState state;
    private volatile Instant lastStateChange;
    private final AtomicInteger consecutiveSuccesses = new AtomicInteger(0);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    
    public CircuitBreaker(String name, CircuitBreakerConfig config) {
        this.name = name;
        this.config = config;
        this.metrics = new CircuitBreakerMetrics();
        this.state = CircuitBreakerState.CLOSED;
        this.lastStateChange = Instant.now();
    }
    
    public CircuitBreaker(String name) {
        this(name, new CircuitBreakerConfig());
    }
    
    public <T> T execute(Supplier<T> supplier) {
        // Check if circuit should transition from OPEN to HALF_OPEN
        if (state == CircuitBreakerState.OPEN) {
            if (shouldAttemptReset()) {
                transitionTo(CircuitBreakerState.HALF_OPEN);
            } else {
                metrics.recordRejected();
                throw new CircuitBreakerOpenException(name);
            }
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            T result = supplier.get();
            onSuccess(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }
    
    private boolean shouldAttemptReset() {
        return Duration.between(lastStateChange, Instant.now()).compareTo(config.getTimeout()) >= 0;
    }
    
    private synchronized void onSuccess(long responseTime) {
        metrics.recordSuccess(responseTime);
        consecutiveFailures.set(0);
        
        if (state == CircuitBreakerState.HALF_OPEN) {
            int successes = consecutiveSuccesses.incrementAndGet();
            if (successes >= config.getSuccessThreshold()) {
                transitionTo(CircuitBreakerState.CLOSED);
            }
        }
    }
    
    private synchronized void onFailure() {
        metrics.recordFailure();
        consecutiveSuccesses.set(0);
        
        int failures = consecutiveFailures.incrementAndGet();
        
        if (state == CircuitBreakerState.HALF_OPEN) {
            // Any failure in HALF_OPEN goes back to OPEN
            transitionTo(CircuitBreakerState.OPEN);
        } else if (state == CircuitBreakerState.CLOSED) {
            if (failures >= config.getFailureThreshold()) {
                transitionTo(CircuitBreakerState.OPEN);
            }
        }
    }
    
    private void transitionTo(CircuitBreakerState newState) {
        CircuitBreakerState oldState = this.state;
        this.state = newState;
        this.lastStateChange = Instant.now();
        
        System.out.println(String.format(
            "[CircuitBreaker: %s] State transition: %s -> %s at %s",
            name, oldState, newState, lastStateChange
        ));
        
        if (newState == CircuitBreakerState.CLOSED) {
            consecutiveFailures.set(0);
            consecutiveSuccesses.set(0);
            metrics.reset();
        } else if (newState == CircuitBreakerState.HALF_OPEN) {
            consecutiveSuccesses.set(0);
        }
    }
    
    public CircuitBreakerState getState() {
        return state;
    }
    
    public CircuitBreakerMetrics getMetrics() {
        return metrics;
    }
    
    public String getName() {
        return name;
    }
}

// Circuit Breaker Registry
@Component
class CircuitBreakerRegistry {
    private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    
    public CircuitBreaker getOrCreate(String name) {
        return circuitBreakers.computeIfAbsent(name, n -> new CircuitBreaker(n));
    }
    
    public CircuitBreaker getOrCreate(String name, CircuitBreakerConfig config) {
        return circuitBreakers.computeIfAbsent(name, n -> new CircuitBreaker(n, config));
    }
    
    public CircuitBreaker get(String name) {
        return circuitBreakers.get(name);
    }
    
    public ConcurrentHashMap<String, CircuitBreaker> getAllCircuitBreakers() {
        return new ConcurrentHashMap<>(circuitBreakers);
    }
}

// Service with Circuit Breaker Protection
@Component
class ProtectedService {
    private final CircuitBreakerRegistry registry;
    
    public ProtectedService(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }
    
    public String callExternalService(String serviceName) {
        CircuitBreaker circuitBreaker = registry.getOrCreate(serviceName);
        
        return circuitBreaker.execute(() -> {
            // Simulate external service call
            return performServiceCall(serviceName);
        });
    }
    
    private String performServiceCall(String serviceName) {
        // Simulate network call with potential failure
        if (Math.random() < 0.3) { // 30% failure rate
            throw new RuntimeException("Service call failed: " + serviceName);
        }
        
        // Simulate processing time
        try {
            Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "Success response from " + serviceName;
    }
}

// REST Controller
@RestController
@RequestMapping("/api/circuit-breaker")
class CircuitBreakerController {
    private final ProtectedService protectedService;
    private final CircuitBreakerRegistry registry;
    
    public CircuitBreakerController(ProtectedService protectedService, CircuitBreakerRegistry registry) {
        this.protectedService = protectedService;
        this.registry = registry;
    }
    
    @GetMapping("/call/{serviceName}")
    public String callService(@PathVariable String serviceName) {
        try {
            return protectedService.callExternalService(serviceName);
        } catch (CircuitBreakerOpenException e) {
            return "Circuit breaker is OPEN for " + serviceName + ". Service temporarily unavailable.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/status/{serviceName}")
    public String getStatus(@PathVariable String serviceName) {
        CircuitBreaker cb = registry.get(serviceName);
        if (cb == null) {
            return "Circuit breaker not found: " + serviceName;
        }
        
        CircuitBreakerMetrics metrics = cb.getMetrics();
        return String.format(
            "Circuit Breaker: %s\n" +
            "State: %s\n" +
            "Success Count: %d\n" +
            "Failure Count: %d\n" +
            "Rejected Calls: %d\n" +
            "Failure Rate: %.2f%%\n" +
            "Average Response Time: %.2f ms",
            serviceName,
            cb.getState(),
            metrics.getSuccessCount(),
            metrics.getFailureCount(),
            metrics.getRejectedCalls(),
            metrics.getFailureRate(),
            metrics.getAverageResponseTime()
        );
    }
    
    @GetMapping("/all-status")
    public String getAllStatus() {
        StringBuilder status = new StringBuilder("Circuit Breaker Status:\n\n");
        
        registry.getAllCircuitBreakers().forEach((name, cb) -> {
            CircuitBreakerMetrics metrics = cb.getMetrics();
            status.append(String.format(
                "%s: %s (Failures: %d, Successes: %d, Rejected: %d)\n",
                name, cb.getState(), 
                metrics.getFailureCount(),
                metrics.getSuccessCount(),
                metrics.getRejectedCalls()
            ));
        });
        
        return status.toString();
    }
}

/**
 * Demonstration of Circuit Breaker Pattern
 */
public class CircuitBreakerPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Circuit Breaker Pattern Demo ===\n");
        
        // Create circuit breaker with custom configuration
        CircuitBreakerConfig config = new CircuitBreakerConfig(
            3,                          // failureThreshold
            2,                          // successThreshold
            Duration.ofSeconds(5),      // timeout
            Duration.ofSeconds(2),      // callTimeout
            10                          // slidingWindowSize
        );
        
        CircuitBreaker circuitBreaker = new CircuitBreaker("payment-service", config);
        
        // Simulate service calls
        System.out.println("1. Making successful calls (Circuit should stay CLOSED):");
        for (int i = 0; i < 5; i++) {
            try {
                String result = circuitBreaker.execute(() -> "Payment processed successfully");
                System.out.println("   Call " + (i + 1) + ": " + result + " - State: " + circuitBreaker.getState());
            } catch (Exception e) {
                System.out.println("   Call " + (i + 1) + ": Failed - " + e.getMessage());
            }
        }
        
        System.out.println("\n2. Making failing calls (Circuit should OPEN after 3 failures):");
        for (int i = 0; i < 5; i++) {
            try {
                String result = circuitBreaker.execute(() -> {
                    throw new RuntimeException("Payment gateway timeout");
                });
                System.out.println("   Call " + (i + 1) + ": " + result);
            } catch (CircuitBreakerOpenException e) {
                System.out.println("   Call " + (i + 1) + ": " + e.getMessage() + " - State: " + circuitBreaker.getState());
            } catch (Exception e) {
                System.out.println("   Call " + (i + 1) + ": Failed - " + e.getMessage() + " - State: " + circuitBreaker.getState());
            }
        }
        
        System.out.println("\n3. Waiting for timeout (5 seconds) to transition to HALF_OPEN...");
        try {
            Thread.sleep(5100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n4. Making calls after timeout (Circuit should be HALF_OPEN):");
        for (int i = 0; i < 3; i++) {
            try {
                String result = circuitBreaker.execute(() -> "Payment processed successfully");
                System.out.println("   Call " + (i + 1) + ": " + result + " - State: " + circuitBreaker.getState());
            } catch (Exception e) {
                System.out.println("   Call " + (i + 1) + ": Failed - " + e.getMessage() + " - State: " + circuitBreaker.getState());
            }
        }
        
        // Display final metrics
        CircuitBreakerMetrics metrics = circuitBreaker.getMetrics();
        System.out.println("\n=== Final Metrics ===");
        System.out.println("Total Calls: " + metrics.getTotalCalls());
        System.out.println("Success Count: " + metrics.getSuccessCount());
        System.out.println("Failure Count: " + metrics.getFailureCount());
        System.out.println("Rejected Calls: " + metrics.getRejectedCalls());
        System.out.println("Failure Rate: " + String.format("%.2f%%", metrics.getFailureRate()));
        System.out.println("Average Response Time: " + String.format("%.2f ms", metrics.getAverageResponseTime()));
        System.out.println("Final State: " + circuitBreaker.getState());
    }
}
