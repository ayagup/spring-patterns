import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Circuit Breaker Pattern - Spring Cloud Gateway
 * ==============================================
 * 
 * Circuit Breaker prevents cascading failures by stopping requests to failing services.
 * Provides fallback responses when services are unavailable.
 * 
 * Circuit States:
 * - CLOSED: Normal operation, requests pass through
 * - OPEN: Service failing, requests fail immediately with fallback
 * - HALF_OPEN: Testing if service recovered, limited requests allowed
 * 
 * Spring Cloud Gateway integrates with:
 * - Resilience4j: Recommended, lightweight, Java 8+ functional library
 * - Spring Cloud CircuitBreaker: Abstraction over circuit breaker implementations
 * 
 * Key Metrics:
 * - Failure Rate: Percentage of failed requests to open circuit
 * - Slow Call Rate: Percentage of slow requests to open circuit
 * - Wait Duration: Time in OPEN state before transitioning to HALF_OPEN
 * - Permitted Calls in Half-Open: Number of test requests in HALF_OPEN state
 * - Sliding Window Size: Number of requests to calculate failure rate
 * 
 * Use Cases:
 * - Prevent cascading failures
 * - Fail fast when service is down
 * - Provide fallback responses
 * - Automatic service recovery detection
 * - Reduce load on failing services
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
 * </dependency>
 */
@Configuration
public class CircuitBreakerPattern {

    /**
     * Example 1: Basic Circuit Breaker with Fallback
     * Circuit opens after 50% failure rate, fallback to static response.
     */
    @Bean
    public RouteLocator basicCircuitBreakerRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("circuit_breaker_basic", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .circuitBreaker(c -> c
                        .setName("ordersCircuitBreaker")
                        .setFallbackUri("forward:/fallback/orders")
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 2: Circuit Breaker with Custom Fallback
     * Fallback to different backend service.
     */
    @Bean
    public RouteLocator customFallbackRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("circuit_breaker_custom_fallback", r -> r
                .path("/api/inventory/**")
                .filters(f -> f
                    .circuitBreaker(c -> c
                        .setName("inventoryCircuitBreaker")
                        .setFallbackUri("http://localhost:8082/fallback/inventory")
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 3: Circuit Breaker with Status Codes
     * Open circuit on specific HTTP status codes.
     */
    @Bean
    public RouteLocator statusCodeCircuitBreakerRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("circuit_breaker_status_codes", r -> r
                .path("/api/payments/**")
                .filters(f -> f
                    .circuitBreaker(c -> c
                        .setName("paymentsCircuitBreaker")
                        .setFallbackUri("forward:/fallback/payments")
                        .setStatusCodes("500", "502", "503", "504")  // 5xx errors
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 4: Circuit Breaker with Route-Specific Fallback
     * Different fallback for different routes.
     */
    @Bean
    public RouteLocator routeSpecificFallbackRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // User service with user fallback
            .route("circuit_breaker_users", r -> r
                .path("/api/users/**")
                .filters(f -> f
                    .circuitBreaker(c -> c
                        .setName("usersCircuitBreaker")
                        .setFallbackUri("forward:/fallback/users")
                    )
                )
                .uri("http://localhost:8081"))
            
            // Product service with product fallback
            .route("circuit_breaker_products", r -> r
                .path("/api/products/**")
                .filters(f -> f
                    .circuitBreaker(c -> c
                        .setName("productsCircuitBreaker")
                        .setFallbackUri("forward:/fallback/products")
                    )
                )
                .uri("http://localhost:8082"))
            
            .build();
    }

    /**
     * Example 5: Resilience4j Circuit Breaker Configuration
     * Configure failure rate, wait duration, sliding window.
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .failureRateThreshold(50)  // Open circuit at 50% failure rate
                .waitDurationInOpenState(Duration.ofSeconds(30))  // Wait 30s before HALF_OPEN
                .slidingWindowSize(10)  // Calculate failure rate over 10 requests
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .permittedNumberOfCallsInHalfOpenState(3)  // Allow 3 test requests in HALF_OPEN
                .minimumNumberOfCalls(5)  // Minimum 5 requests before calculating failure rate
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)  // Record all exceptions
                .ignoreExceptions(IllegalArgumentException.class)  // Ignore validation errors
                .build())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))  // Request timeout
                .build())
            .build());
    }

    /**
     * Example 6: Custom Circuit Breaker per Route
     * Different configurations for different routes.
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> customCircuitBreakerConfig() {
        return factory -> {
            // Strict circuit breaker for critical services
            factory.configure(builder -> builder
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                    .failureRateThreshold(25)  // Open at 25% failure rate (strict)
                    .waitDurationInOpenState(Duration.ofMinutes(1))
                    .slidingWindowSize(20)
                    .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                    .timeoutDuration(Duration.ofSeconds(3))  // Shorter timeout
                    .build())
                .build(), "criticalCircuitBreaker");
            
            // Lenient circuit breaker for non-critical services
            factory.configure(builder -> builder
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                    .failureRateThreshold(75)  // Open at 75% failure rate (lenient)
                    .waitDurationInOpenState(Duration.ofSeconds(15))
                    .slidingWindowSize(5)
                    .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                    .timeoutDuration(Duration.ofSeconds(10))  // Longer timeout
                    .build())
                .build(), "nonCriticalCircuitBreaker");
        };
    }

    /**
     * Example 7: Slow Call Detection
     * Open circuit when requests are slow (not just failing).
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> slowCallDetectionConfig() {
        return factory -> factory.configure(builder -> builder
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .slowCallRateThreshold(50)  // Open at 50% slow calls
                .slowCallDurationThreshold(Duration.ofSeconds(3))  // >3s is slow
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build())
            .build(), "slowCallCircuitBreaker");
    }

    /**
     * Example 8: Circuit Breaker with Fallback Headers
     * Add exception details to fallback response headers.
     */
    @Bean
    public RouteLocator fallbackHeadersRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("circuit_breaker_fallback_headers", r -> r
                .path("/api/recommendations/**")
                .filters(f -> f
                    .circuitBreaker(c -> c
                        .setName("recommendationsCircuitBreaker")
                        .setFallbackUri("forward:/fallback/recommendations")
                    )
                    .fallbackHeaders()  // Add exception headers to fallback request
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 9: Fallback Controller
     * Handle fallback requests with custom responses.
     */
    @Component
    public static class FallbackController {
        
        @GetMapping("/fallback/orders")
        public Mono<String> ordersFallback(ServerWebExchange exchange) {
            Throwable exception = exchange.getAttribute("circuitBreakerException");
            
            System.err.println("Orders service unavailable: " + 
                (exception != null ? exception.getMessage() : "unknown"));
            
            return Mono.just("{\"message\": \"Orders service temporarily unavailable\"}");
        }
        
        @GetMapping("/fallback/users")
        public Mono<String> usersFallback() {
            return Mono.just("{\"message\": \"User service temporarily unavailable\"}");
        }
        
        @GetMapping("/fallback/products")
        public Mono<String> productsFallback() {
            // Return cached product list
            return Mono.just("{\"products\": [], \"cached\": true}");
        }
        
        @GetMapping("/fallback/payments")
        public Mono<String> paymentsFallback(ServerWebExchange exchange) {
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return Mono.just("{\"message\": \"Payment processing temporarily unavailable\"}");
        }
    }

    /**
     * Example 10: Circuit Breaker with Retry
     * Combine circuit breaker with retry for transient failures.
     */
    @Bean
    public RouteLocator circuitBreakerWithRetryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("circuit_breaker_with_retry", r -> r
                .path("/api/external/**")
                .filters(f -> f
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setStatuses(HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE)
                        .setBackoff(
                            Duration.ofMillis(100),  // firstBackoff
                            Duration.ofMillis(500),  // maxBackoff
                            2,                       // factor
                            true                     // basedOnPreviousValue
                        )
                    )
                    .circuitBreaker(c -> c
                        .setName("externalCircuitBreaker")
                        .setFallbackUri("forward:/fallback/external")
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * YAML Configuration Example
     * ==========================
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         # Basic circuit breaker
     *         - id: circuit_breaker_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/orders/**
     *           filters:
     *             - name: CircuitBreaker
     *               args:
     *                 name: ordersCircuitBreaker
     *                 fallbackUri: forward:/fallback/orders
     * 
     *         # Circuit breaker with status codes
     *         - id: circuit_breaker_status_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/payments/**
     *           filters:
     *             - name: CircuitBreaker
     *               args:
     *                 name: paymentsCircuitBreaker
     *                 fallbackUri: forward:/fallback/payments
     *                 statusCodes:
     *                   - 500
     *                   - 502
     *                   - 503
     *                   - 504
     * 
     *         # Circuit breaker with fallback headers
     *         - id: circuit_breaker_fallback_headers_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/recommendations/**
     *           filters:
     *             - name: CircuitBreaker
     *               args:
     *                 name: recommendationsCircuitBreaker
     *                 fallbackUri: forward:/fallback/recommendations
     *             - FallbackHeaders
     * 
     *         # Circuit breaker with retry
     *         - id: circuit_breaker_retry_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/external/**
     *           filters:
     *             - name: Retry
     *               args:
     *                 retries: 3
     *                 statuses: BAD_GATEWAY, SERVICE_UNAVAILABLE
     *                 backoff:
     *                   firstBackoff: 100ms
     *                   maxBackoff: 500ms
     *                   factor: 2
     *                   basedOnPreviousValue: true
     *             - name: CircuitBreaker
     *               args:
     *                 name: externalCircuitBreaker
     *                 fallbackUri: forward:/fallback/external
     * 
     *   # Resilience4j configuration
     *   resilience4j:
     *     circuitbreaker:
     *       instances:
     *         ordersCircuitBreaker:
     *           failure-rate-threshold: 50
     *           wait-duration-in-open-state: 30s
     *           sliding-window-size: 10
     *           sliding-window-type: COUNT_BASED
     *           permitted-number-of-calls-in-half-open-state: 3
     *           minimum-number-of-calls: 5
     *           automatic-transition-from-open-to-half-open-enabled: true
     * 
     *         criticalCircuitBreaker:
     *           failure-rate-threshold: 25
     *           wait-duration-in-open-state: 1m
     *           sliding-window-size: 20
     * 
     *         slowCallCircuitBreaker:
     *           slow-call-rate-threshold: 50
     *           slow-call-duration-threshold: 3s
     *           sliding-window-size: 10
     *           minimum-number-of-calls: 5
     * 
     *     timelimiter:
     *       instances:
     *         ordersCircuitBreaker:
     *           timeout-duration: 5s
     */

    /**
     * Circuit Breaker Events:
     * ======================
     * 
     * Listen to circuit breaker events for monitoring:
     * - onSuccess
     * - onError
     * - onStateTransition (CLOSED -> OPEN -> HALF_OPEN)
     * - onSlowCallRateExceeded
     * - onFailureRateExceeded
     */
    @EventListener
    public void onCircuitBreakerEvent(CircuitBreakerOnStateTransitionEvent event) {
        System.out.println("Circuit breaker state transition: " +
            event.getCircuitBreakerName() + " - " +
            event.getStateTransition().getFromState() + " -> " +
            event.getStateTransition().getToState());
    }

    /**
     * Best Practices:
     * ===============
     * 
     * 1. Set Appropriate Thresholds: Balance sensitivity and stability
     * 2. Use Sliding Windows: More accurate than fixed windows
     * 3. Provide Meaningful Fallbacks: Don't just return errors
     * 4. Monitor Circuit State: Alert on state transitions
     * 5. Test Circuit Behavior: Simulate failures to validate configuration
     * 6. Combine with Retry: Retry transient failures before opening circuit
     * 7. Configure Timeouts: Prevent hanging requests
     * 8. Use Different Configs: Critical vs. non-critical services
     * 9. Log State Changes: Debug circuit behavior
     * 10. Graceful Degradation: Provide reduced functionality in fallback
     * 
     * Common Pitfalls:
     * ================
     * 
     * 1. Too sensitive: Opens circuit on transient failures
     * 2. Too lenient: Doesn't protect against cascading failures
     * 3. No fallback: Returns errors instead of degraded response
     * 4. Blocking fallback: Defeats reactive nature
     * 5. Same config for all: One size doesn't fit all services
     * 6. Not monitoring: Miss circuit state changes
     * 7. No timeout: Circuit never opens on slow responses
     * 8. Too short wait duration: Circuit thrashes open/closed
     * 9. No minimum calls: Opens on first failure
     * 10. Ignoring half-open: Circuit stuck in open state
     * 
     * When to Use:
     * ============
     * 
     * - Calling unreliable external services
     * - Preventing cascading failures
     * - Protecting downstream services
     * - Failing fast when service is down
     * - Providing fallback responses
     * - Detecting service recovery automatically
     * 
     * When NOT to Use:
     * ================
     * 
     * - Internal reliable services (low failure rate)
     * - No acceptable fallback available
     * - Critical operations (cannot fail)
     * - Services with SLA guarantees
     */
}
