import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Retry Pattern - Spring Cloud Gateway
 * ====================================
 * 
 * Retry filter automatically retries failed requests to handle transient failures.
 * Implements exponential backoff to avoid overwhelming failing services.
 * 
 * Retry Strategies:
 * - Fixed Delay: Constant delay between retries
 * - Exponential Backoff: Increasing delay (100ms, 200ms, 400ms, etc.)
 * - Jittered Backoff: Random delay to prevent thundering herd
 * 
 * Retry Configuration:
 * - retries: Number of retry attempts
 * - statuses: HTTP status codes to retry (5xx errors)
 * - methods: HTTP methods to retry (GET, HEAD, etc.)
 * - series: Status series to retry (SERVER_ERROR, CLIENT_ERROR)
 * - exceptions: Exception types to retry
 * - backoff: Exponential backoff configuration
 * 
 * Use Cases:
 * - Transient network failures
 * - Service temporarily unavailable
 * - Rate limit errors (with backoff)
 * - Gateway timeout errors
 * - Connection timeouts
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 */
@Configuration
public class RetryPattern {

    /**
     * Example 1: Basic Retry with Default Settings
     * Retries 3 times on 5xx errors.
     */
    @Bean
    public RouteLocator basicRetryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("basic_retry", r -> r
                .path("/api/**")
                .filters(f -> f
                    .retry(3)  // Retry 3 times
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 2: Retry with Specific Status Codes
     * Retry only on specific HTTP status codes.
     */
    @Bean
    public RouteLocator statusCodeRetryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("status_code_retry", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setStatuses(
                            HttpStatus.BAD_GATEWAY,           // 502
                            HttpStatus.SERVICE_UNAVAILABLE,   // 503
                            HttpStatus.GATEWAY_TIMEOUT        // 504
                        )
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 3: Retry with Status Series
     * Retry on entire status series (4xx, 5xx).
     */
    @Bean
    public RouteLocator statusSeriesRetryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("status_series_retry", r -> r
                .path("/api/external/**")
                .filters(f -> f
                    .retry(retryConfig -> retryConfig
                        .setRetries(5)
                        .setSeries(
                            HttpStatus.Series.SERVER_ERROR  // All 5xx errors
                        )
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 4: Retry with HTTP Methods
     * Retry only on safe/idempotent HTTP methods.
     */
    @Bean
    public RouteLocator methodRetryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("method_retry", r -> r
                .path("/api/**")
                .filters(f -> f
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setMethods(
                            HttpMethod.GET,    // Safe and idempotent
                            HttpMethod.HEAD,   // Safe and idempotent
                            HttpMethod.PUT,    // Idempotent
                            HttpMethod.DELETE  // Idempotent
                        )
                        // Don't retry POST (not idempotent)
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 5: Exponential Backoff
     * Retry with increasing delay between attempts.
     */
    @Bean
    public RouteLocator exponentialBackoffRetryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("exponential_backoff_retry", r -> r
                .path("/api/payments/**")
                .filters(f -> f
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setStatuses(HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE)
                        .setBackoff(
                            Duration.ofMillis(100),  // firstBackoff: 100ms
                            Duration.ofMillis(1000), // maxBackoff: 1000ms
                            2,                       // factor: doubles each retry
                            true                     // basedOnPreviousValue: true
                        )
                        // Retry delays: 100ms, 200ms, 400ms
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 6: Fixed Delay Backoff
     * Retry with constant delay between attempts.
     */
    @Bean
    public RouteLocator fixedDelayRetryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("fixed_delay_retry", r -> r
                .path("/api/notifications/**")
                .filters(f -> f
                    .retry(retryConfig -> retryConfig
                        .setRetries(5)
                        .setStatuses(HttpStatus.SERVICE_UNAVAILABLE)
                        .setBackoff(
                            Duration.ofMillis(500),  // firstBackoff: 500ms
                            Duration.ofMillis(500),  // maxBackoff: 500ms (same as first)
                            1,                       // factor: 1 (no multiplication)
                            false                    // basedOnPreviousValue: false
                        )
                        // All retries wait 500ms
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 7: Retry with Exceptions
     * Retry on specific exception types.
     */
    @Bean
    public RouteLocator exceptionRetryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("exception_retry", r -> r
                .path("/api/inventory/**")
                .filters(f -> f
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setExceptions(
                            java.io.IOException.class,
                            java.util.concurrent.TimeoutException.class
                        )
                        .setBackoff(
                            Duration.ofMillis(100),
                            Duration.ofMillis(500),
                            2,
                            true
                        )
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 8: Comprehensive Retry Configuration
     * Combine status codes, methods, exceptions, and backoff.
     */
    @Bean
    public RouteLocator comprehensiveRetryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("comprehensive_retry", r -> r
                .path("/api/users/**")
                .filters(f -> f
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        // Retry on specific status codes
                        .setStatuses(
                            HttpStatus.BAD_GATEWAY,
                            HttpStatus.SERVICE_UNAVAILABLE,
                            HttpStatus.GATEWAY_TIMEOUT
                        )
                        // Retry only on safe methods
                        .setMethods(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.PUT, HttpMethod.DELETE)
                        // Retry on specific exceptions
                        .setExceptions(
                            java.io.IOException.class,
                            java.util.concurrent.TimeoutException.class
                        )
                        // Exponential backoff
                        .setBackoff(
                            Duration.ofMillis(100),  // Start at 100ms
                            Duration.ofMillis(2000), // Max 2000ms
                            2,                       // Double each time
                            true
                        )
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 9: Rate Limit Retry
     * Retry on 429 Too Many Requests with longer backoff.
     */
    @Bean
    public RouteLocator rateLimitRetryRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("rate_limit_retry", r -> r
                .path("/api/external/**")
                .filters(f -> f
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setStatuses(HttpStatus.TOO_MANY_REQUESTS)  // 429
                        .setBackoff(
                            Duration.ofSeconds(1),   // Start at 1s
                            Duration.ofSeconds(10),  // Max 10s
                            2,
                            true
                        )
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * Example 10: Retry with Circuit Breaker
     * Combine retry (transient failures) with circuit breaker (persistent failures).
     */
    @Bean
    public RouteLocator retryWithCircuitBreakerRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("retry_circuit_breaker", r -> r
                .path("/api/products/**")
                .filters(f -> f
                    // Retry first (transient failures)
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setStatuses(HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE)
                        .setBackoff(
                            Duration.ofMillis(100),
                            Duration.ofMillis(500),
                            2,
                            true
                        )
                    )
                    // Circuit breaker if retries fail (persistent failures)
                    .circuitBreaker(c -> c
                        .setName("productsCircuitBreaker")
                        .setFallbackUri("forward:/fallback/products")
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
     *         # Basic retry
     *         - id: basic_retry_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             - name: Retry
     *               args:
     *                 retries: 3
     * 
     *         # Retry with status codes
     *         - id: status_retry_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/orders/**
     *           filters:
     *             - name: Retry
     *               args:
     *                 retries: 3
     *                 statuses: BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT
     * 
     *         # Exponential backoff
     *         - id: backoff_retry_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/payments/**
     *           filters:
     *             - name: Retry
     *               args:
     *                 retries: 3
     *                 statuses: BAD_GATEWAY, SERVICE_UNAVAILABLE
     *                 backoff:
     *                   firstBackoff: 100ms
     *                   maxBackoff: 1000ms
     *                   factor: 2
     *                   basedOnPreviousValue: true
     * 
     *         # Retry with methods
     *         - id: method_retry_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             - name: Retry
     *               args:
     *                 retries: 3
     *                 methods: GET, HEAD, PUT, DELETE
     * 
     *         # Comprehensive retry
     *         - id: comprehensive_retry_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/users/**
     *           filters:
     *             - name: Retry
     *               args:
     *                 retries: 3
     *                 statuses: BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT
     *                 methods: GET, HEAD, PUT, DELETE
     *                 exceptions:
     *                   - java.io.IOException
     *                   - java.util.concurrent.TimeoutException
     *                 backoff:
     *                   firstBackoff: 100ms
     *                   maxBackoff: 2000ms
     *                   factor: 2
     *                   basedOnPreviousValue: true
     * 
     *         # Retry with circuit breaker
     *         - id: retry_circuit_breaker_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/products/**
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
     *                 name: productsCircuitBreaker
     *                 fallbackUri: forward:/fallback/products
     */

    /**
     * Retry Metrics:
     * =============
     * 
     * Monitor retry behavior with metrics:
     * - Total retry attempts
     * - Successful retries
     * - Failed retries (max attempts exceeded)
     * - Average retry count per request
     * - Backoff times
     */

    /**
     * Best Practices:
     * ===============
     * 
     * 1. Idempotent Operations: Only retry safe/idempotent methods (GET, PUT, DELETE)
     * 2. Exponential Backoff: Prevent overwhelming failing services
     * 3. Max Backoff: Cap maximum delay to avoid excessive wait times
     * 4. Limited Retries: Don't retry forever (3-5 attempts typical)
     * 5. Jittered Backoff: Add randomness to prevent thundering herd
     * 6. Status Codes: Retry only retriable errors (5xx, not 4xx)
     * 7. Circuit Breaker: Combine with circuit breaker for persistent failures
     * 8. Monitoring: Track retry rates and success/failure
     * 9. Timeouts: Set appropriate timeouts to avoid hanging retries
     * 10. Documentation: Document retry behavior for API consumers
     * 
     * Idempotent HTTP Methods:
     * ========================
     * 
     * SAFE to retry:
     * - GET: Retrieves data, no side effects
     * - HEAD: Like GET but without body
     * - PUT: Idempotent update (same result if called multiple times)
     * - DELETE: Idempotent deletion (deleting twice = same result)
     * - OPTIONS: Retrieves supported methods
     * 
     * NOT SAFE to retry (by default):
     * - POST: Creates resource, not idempotent (creates multiple resources)
     * - PATCH: Partial update, may not be idempotent
     * 
     * Exception: POST can be retried if API is designed to be idempotent
     * (e.g., using idempotency keys, request IDs)
     * 
     * Retriable vs. Non-Retriable Errors:
     * ===================================
     * 
     * RETRIABLE (transient failures):
     * - 500 Internal Server Error (may be temporary)
     * - 502 Bad Gateway (upstream server issue)
     * - 503 Service Unavailable (temporary overload)
     * - 504 Gateway Timeout (timeout, may succeed on retry)
     * - 429 Too Many Requests (rate limit, retry with backoff)
     * - Connection timeout
     * - Connection reset
     * 
     * NON-RETRIABLE (permanent failures):
     * - 400 Bad Request (invalid request, won't change)
     * - 401 Unauthorized (need authentication)
     * - 403 Forbidden (no permission)
     * - 404 Not Found (resource doesn't exist)
     * - 405 Method Not Allowed (wrong HTTP method)
     * - 409 Conflict (business logic conflict)
     * - 422 Unprocessable Entity (validation error)
     * 
     * Common Pitfalls:
     * ================
     * 
     * 1. Retrying non-idempotent operations (POST creates duplicates)
     * 2. No backoff: Hammers failing service
     * 3. Too many retries: Wastes resources
     * 4. Retrying 4xx errors: Client errors won't fix themselves
     * 5. No maximum backoff: Delays grow unbounded
     * 6. Not combining with circuit breaker: Retries persist even when service is down
     * 7. Same backoff for all: Different operations need different strategies
     * 8. No monitoring: Missing retry storms
     * 9. Blocking retries: Blocks reactive chain
     * 10. No timeout: Retries can hang indefinitely
     * 
     * When to Use:
     * ============
     * 
     * - Transient network failures
     * - Temporary service unavailability
     * - Rate limiting (with appropriate backoff)
     * - Gateway timeouts
     * - Idempotent operations
     * - External API calls (unreliable networks)
     * 
     * When NOT to Use:
     * ================
     * 
     * - Non-idempotent operations (without idempotency keys)
     * - Client errors (4xx) - won't fix themselves
     * - Real-time critical operations (retries add latency)
     * - Already have circuit breaker (may conflict)
     * - Expensive operations (retries multiply cost)
     * - Data modification without idempotency guarantees
     */
}
