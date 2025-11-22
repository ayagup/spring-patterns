package com.example.cloud.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Circuit Breaker Pattern - Demonstrates Fault Tolerance with Circuit Breaker
 * 
 * This pattern shows how to:
 * 1. Implement circuit breaker with three states (CLOSED, OPEN, HALF_OPEN)
 * 2. Configure failure rate thresholds
 * 3. Implement fallback methods
 * 4. Handle slow call detection
 * 5. Configure wait duration in OPEN state
 * 6. Implement automatic state transitions
 * 7. Use Resilience4j CircuitBreaker
 * 8. Monitor circuit breaker metrics
 * 9. Configure sliding window for failure tracking
 * 10. Implement manual circuit breaker control
 * 
 * Key Concepts:
 * - CLOSED: Normal operation, requests pass through
 * - OPEN: Too many failures, requests fail immediately
 * - HALF_OPEN: Testing if service recovered
 * - Failure Threshold: Percentage to trigger OPEN state
 * - Wait Duration: Time to wait before trying HALF_OPEN
 * 
 * Circuit Breaker States:
 * CLOSED → OPEN (failure threshold exceeded)
 * OPEN → HALF_OPEN (wait duration elapsed)
 * HALF_OPEN → CLOSED (successful calls)
 * HALF_OPEN → OPEN (failures continue)
 * 
 * Dependencies:
 * - io.github.resilience4j:resilience4j-spring-boot3
 * - io.github.resilience4j:resilience4j-circuitbreaker
 * 
 * Configuration:
 * resilience4j.circuitbreaker.instances.myService.failureRateThreshold=50
 * resilience4j.circuitbreaker.instances.myService.waitDurationInOpenState=10s
 * resilience4j.circuitbreaker.instances.myService.slidingWindowSize=10
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class CircuitBreakerPattern {

    public static void main(String[] args) {
        SpringApplication.run(CircuitBreakerPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("CIRCUIT BREAKER PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateCircuitBreakerStates();
        demonstrateConfiguration();
        demonstrateFallbacks();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CIRCUIT BREAKER STATES");
        System.out.println("=".repeat(80));
        System.out.println("\nCLOSED:");
        System.out.println("  - Normal operation");
        System.out.println("  - All requests allowed");
        System.out.println("  - Tracks failure rate");
        
        System.out.println("\nOPEN:");
        System.out.println("  - Circuit is open (broken)");
        System.out.println("  - Requests fail immediately");
        System.out.println("  - Fallback methods called");
        System.out.println("  - Waits for recovery period");
        
        System.out.println("\nHALF_OPEN:");
        System.out.println("  - Testing if service recovered");
        System.out.println("  - Limited requests allowed");
        System.out.println("  - Success → CLOSED");
        System.out.println("  - Failure → OPEN");
        
        System.out.println("\nApplication is running. Test circuit breaker at:");
        System.out.println("GET /api/circuit/call-service");
        System.out.println("GET /api/circuit/state");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateCircuitBreakerStates() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CIRCUIT BREAKER CONCEPTS");
        System.out.println("=".repeat(80));
        
        System.out.println("\nWhy Circuit Breaker?");
        System.out.println("- Prevent cascading failures");
        System.out.println("- Fail fast instead of waiting");
        System.out.println("- Automatic recovery testing");
        System.out.println("- Preserve system resources");
        System.out.println("- Improve user experience with fallbacks");
    }
    
    private static void demonstrateConfiguration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONFIGURATION PARAMETERS");
        System.out.println("=".repeat(80));
        
        System.out.println("\nKey Parameters:");
        System.out.println("failureRateThreshold: % of failures to open circuit (default: 50%)");
        System.out.println("waitDurationInOpenState: Time before HALF_OPEN (default: 60s)");
        System.out.println("slidingWindowSize: Number of calls to track (default: 100)");
        System.out.println("permittedNumberOfCallsInHalfOpenState: Test calls (default: 10)");
        System.out.println("slowCallDurationThreshold: Threshold for slow calls (default: 60s)");
        System.out.println("slowCallRateThreshold: % of slow calls to open (default: 100%)");
    }
    
    private static void demonstrateFallbacks() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("FALLBACK STRATEGIES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Return cached data");
        System.out.println("2. Return default values");
        System.out.println("3. Return empty response");
        System.out.println("4. Queue request for later");
        System.out.println("5. Graceful degradation");
    }
}

/**
 * Circuit Breaker Configuration
 */
@Configuration
class CircuitBreakerConfiguration {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .slidingWindowSize(10)
            .permittedNumberOfCallsInHalfOpenState(5)
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .slowCallRateThreshold(50)
            .recordExceptions(Exception.class)
            .build();
        
        return CircuitBreakerRegistry.of(config);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

/**
 * Service with Circuit Breaker annotation
 */
@Service
class ExternalServiceClient {
    
    private final RestTemplate restTemplate;
    private final Map<String, Object> cache = new HashMap<>();
    
    public ExternalServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Call external service with circuit breaker protection
     */
    @CircuitBreaker(name = "externalService", fallbackMethod = "fallbackResponse")
    public String callExternalService(String request) {
        // Simulate external service call
        if (ThreadLocalRandom.current().nextInt(100) < 40) {
            throw new RuntimeException("External service unavailable");
        }
        
        return "Response from external service: " + request;
    }
    
    /**
     * Fallback method when circuit is open
     */
    private String fallbackResponse(String request, Exception ex) {
        System.out.println("Circuit breaker activated. Using fallback. Error: " + ex.getMessage());
        
        // Return cached data if available
        if (cache.containsKey(request)) {
            return "Cached: " + cache.get(request);
        }
        
        return "Service temporarily unavailable. Please try again later.";
    }
    
    /**
     * Slow service call with timeout protection
     */
    @CircuitBreaker(name = "slowService", fallbackMethod = "fallbackSlowService")
    public String callSlowService() {
        try {
            Thread.sleep(3000); // Simulate slow service
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Slow service response";
    }
    
    private String fallbackSlowService(Exception ex) {
        return "Service is slow. Using cached response.";
    }
}

/**
 * Manual Circuit Breaker Implementation
 */
@Service
class ManualCircuitBreakerService {
    
    private final CircuitBreakerRegistry registry;
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker;
    
    public ManualCircuitBreakerService(CircuitBreakerRegistry registry) {
        this.registry = registry;
        this.circuitBreaker = registry.circuitBreaker("manualService");
    }
    
    /**
     * Execute with circuit breaker using functional style
     */
    public <T> T executeWithCircuitBreaker(Supplier<T> supplier, Function<Throwable, T> fallback) {
        Supplier<T> decorated = io.github.resilience4j.circuitbreaker.CircuitBreaker
            .decorateSupplier(circuitBreaker, supplier);
        
        try {
            return decorated.get();
        } catch (Exception e) {
            return fallback.apply(e);
        }
    }
    
    /**
     * Execute callable with circuit breaker
     */
    public <T> T executeCallable(Callable<T> callable, Function<Throwable, T> fallback) {
        Callable<T> decorated = io.github.resilience4j.circuitbreaker.CircuitBreaker
            .decorateCallable(circuitBreaker, callable);
        
        try {
            return decorated.call();
        } catch (Exception e) {
            return fallback.apply(e);
        }
    }
    
    /**
     * Get circuit breaker state
     */
    public io.github.resilience4j.circuitbreaker.CircuitBreaker.State getState() {
        return circuitBreaker.getState();
    }
    
    /**
     * Get circuit breaker metrics
     */
    public Map<String, Object> getMetrics() {
        io.github.resilience4j.circuitbreaker.CircuitBreaker.Metrics metrics = 
            circuitBreaker.getMetrics();
        
        Map<String, Object> result = new HashMap<>();
        result.put("failureRate", metrics.getFailureRate());
        result.put("numberOfFailedCalls", metrics.getNumberOfFailedCalls());
        result.put("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls());
        result.put("numberOfNotPermittedCalls", metrics.getNumberOfNotPermittedCalls());
        result.put("numberOfSlowCalls", metrics.getNumberOfSlowCalls());
        result.put("slowCallRate", metrics.getSlowCallRate());
        
        return result;
    }
    
    /**
     * Manually transition to OPEN state
     */
    public void transitionToOpenState() {
        circuitBreaker.transitionToOpenState();
    }
    
    /**
     * Manually transition to CLOSED state
     */
    public void transitionToClosedState() {
        circuitBreaker.transitionToClosedState();
    }
    
    /**
     * Manually transition to HALF_OPEN state
     */
    public void transitionToHalfOpenState() {
        circuitBreaker.transitionToHalfOpenState();
    }
    
    /**
     * Reset circuit breaker
     */
    public void reset() {
        circuitBreaker.reset();
    }
}

/**
 * Custom Circuit Breaker Implementation
 */
class CustomCircuitBreaker {
    
    private enum State { CLOSED, OPEN, HALF_OPEN }
    
    private State state = State.CLOSED;
    private int failureCount = 0;
    private int successCount = 0;
    private long lastFailureTime = 0;
    
    private final int failureThreshold;
    private final long timeout;
    private final int successThreshold;
    
    public CustomCircuitBreaker(int failureThreshold, long timeout, int successThreshold) {
        this.failureThreshold = failureThreshold;
        this.timeout = timeout;
        this.successThreshold = successThreshold;
    }
    
    /**
     * Execute operation with circuit breaker
     */
    public <T> T execute(Supplier<T> operation, Supplier<T> fallback) {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > timeout) {
                state = State.HALF_OPEN;
                successCount = 0;
            } else {
                return fallback.get();
            }
        }
        
        try {
            T result = operation.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            return fallback.get();
        }
    }
    
    private synchronized void onSuccess() {
        failureCount = 0;
        
        if (state == State.HALF_OPEN) {
            successCount++;
            if (successCount >= successThreshold) {
                state = State.CLOSED;
            }
        }
    }
    
    private synchronized void onFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        
        if (failureCount >= failureThreshold) {
            state = State.OPEN;
        }
    }
    
    public State getState() {
        return state;
    }
}

/**
 * Payment Service with Circuit Breaker
 */
@Service
class PaymentService {
    
    private final ManualCircuitBreakerService circuitBreakerService;
    
    public PaymentService(ManualCircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }
    
    public String processPayment(double amount) {
        return circuitBreakerService.executeWithCircuitBreaker(
            () -> {
                // Simulate payment processing
                if (ThreadLocalRandom.current().nextBoolean()) {
                    throw new RuntimeException("Payment gateway error");
                }
                return "Payment of $" + amount + " processed successfully";
            },
            throwable -> "Payment failed. Please try again. Using alternative payment method."
        );
    }
}

/**
 * Order Service with Circuit Breaker
 */
@Service
class OrderService {
    
    private final ExternalServiceClient externalClient;
    private final PaymentService paymentService;
    
    public OrderService(ExternalServiceClient externalClient, PaymentService paymentService) {
        this.externalClient = externalClient;
        this.paymentService = paymentService;
    }
    
    public Map<String, Object> placeOrder(String orderId, double amount) {
        Map<String, Object> result = new HashMap<>();
        
        // Call inventory service with circuit breaker
        String inventoryCheck = externalClient.callExternalService("check-" + orderId);
        result.put("inventory", inventoryCheck);
        
        // Process payment with circuit breaker
        String paymentResult = paymentService.processPayment(amount);
        result.put("payment", paymentResult);
        
        result.put("orderId", orderId);
        result.put("status", "completed");
        
        return result;
    }
}

/**
 * REST Controller demonstrating circuit breaker
 */
@RestController
@RequestMapping("/api/circuit")
class CircuitBreakerController {
    
    private final ExternalServiceClient externalClient;
    private final ManualCircuitBreakerService manualCircuitBreaker;
    private final OrderService orderService;
    private final PaymentService paymentService;
    
    public CircuitBreakerController(ExternalServiceClient externalClient,
                                   ManualCircuitBreakerService manualCircuitBreaker,
                                   OrderService orderService,
                                   PaymentService paymentService) {
        this.externalClient = externalClient;
        this.manualCircuitBreaker = manualCircuitBreaker;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }
    
    @GetMapping("/call-service")
    public String callService(@RequestParam(defaultValue = "test") String request) {
        return externalClient.callExternalService(request);
    }
    
    @GetMapping("/call-slow-service")
    public String callSlowService() {
        return externalClient.callSlowService();
    }
    
    @PostMapping("/order")
    public Map<String, Object> placeOrder(@RequestParam String orderId,
                                         @RequestParam double amount) {
        return orderService.placeOrder(orderId, amount);
    }
    
    @PostMapping("/payment")
    public String processPayment(@RequestParam double amount) {
        return paymentService.processPayment(amount);
    }
    
    @GetMapping("/state")
    public Map<String, Object> getCircuitBreakerState() {
        Map<String, Object> state = new HashMap<>();
        state.put("state", manualCircuitBreaker.getState());
        state.put("metrics", manualCircuitBreaker.getMetrics());
        return state;
    }
    
    @PostMapping("/state/open")
    public String openCircuit() {
        manualCircuitBreaker.transitionToOpenState();
        return "Circuit breaker transitioned to OPEN state";
    }
    
    @PostMapping("/state/close")
    public String closeCircuit() {
        manualCircuitBreaker.transitionToClosedState();
        return "Circuit breaker transitioned to CLOSED state";
    }
    
    @PostMapping("/state/half-open")
    public String halfOpenCircuit() {
        manualCircuitBreaker.transitionToHalfOpenState();
        return "Circuit breaker transitioned to HALF_OPEN state";
    }
    
    @PostMapping("/reset")
    public String resetCircuit() {
        manualCircuitBreaker.reset();
        return "Circuit breaker reset";
    }
}
