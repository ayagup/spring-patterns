package com.example.servicemesh;

/**
 * Retry Policy Pattern
 * ====================
 * 
 * Demonstrates retry policies in service mesh for resilience.
 * 
 * Key Concepts:
 * ------------
 * 1. Automatic Retries - Retry failed requests
 * 2. Retry Budget - Limit retry amplification
 * 3. Backoff Strategy - Exponential backoff
 * 4. Idempotency - Safe to retry operations
 * 5. Retry Conditions - When to retry
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Retry Configuration
 */
class BasicRetryConfiguration {
    
    /**
     * Istio VirtualService with retries:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: order-service-retry
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   - route:
     *     - destination:
     *         host: order-service
     *     retries:
     *       attempts: 3
     *       perTryTimeout: 2s
     *       retryOn: 5xx,reset,connect-failure,refused-stream
     */
}

/**
 * Example 2: Retry Conditions
 */
class RetryConditionsConfiguration {
    
    /**
     * retryOn conditions:
     * 
     * - 5xx: Any 5xx response code
     * - gateway-error: 502, 503, 504
     * - reset: Connection reset
     * - connect-failure: Connection failed
     * - refused-stream: Stream refused
     * - retriable-4xx: 409 (Conflict)
     * - cancelled: Request cancelled
     * - deadline-exceeded: Deadline exceeded
     * - internal: Internal error
     * - resource-exhausted: Too many requests
     * - unavailable: Service unavailable
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: comprehensive-retry
     * spec:
     *   hosts:
     *   - payment-service
     *   http:
     *   - route:
     *     - destination:
     *         host: payment-service
     *     retries:
     *       attempts: 5
     *       perTryTimeout: 3s
     *       retryOn: |
     *         5xx,
     *         reset,
     *         connect-failure,
     *         refused-stream,
     *         gateway-error,
     *         retriable-4xx
     */
}

/**
 * Example 3: Exponential Backoff
 */
class ExponentialBackoffConfiguration {
    
    /**
     * Linkerd ServiceProfile with backoff:
     * 
     * apiVersion: linkerd.io/v1alpha2
     * kind: ServiceProfile
     * metadata:
     *   name: order-service.default.svc.cluster.local
     * spec:
     *   retryBudget:
     *     retryRatio: 0.2
     *     minRetriesPerSecond: 10
     *     ttl: 10s
     *   routes:
     *   - name: POST /orders
     *     condition:
     *       method: POST
     *       pathRegex: /orders
     *     isRetryable: true
     *     retries:
     *       limit: 3
     *       backoff:
     *         minBackoff: 100ms   # Initial delay
     *         maxBackoff: 5s      # Max delay
     *         jitterRatio: 0.5    # Add randomness
     * 
     * Backoff formula:
     * delay = min(maxBackoff, minBackoff * (2 ^ attempt) * (1 + jitter))
     * 
     * Example delays:
     * Attempt 1: 100ms * 2^0 = 100ms
     * Attempt 2: 100ms * 2^1 = 200ms
     * Attempt 3: 100ms * 2^2 = 400ms
     * Attempt 4: 100ms * 2^3 = 800ms
     * Attempt 5: min(5000ms, 100ms * 2^4) = 1600ms
     */
}

/**
 * Example 4: Retry Budget
 */
class RetryBudgetConfiguration {
    
    /**
     * Prevent retry storms:
     * 
     * Retry Budget = retryRatio * successful_requests + minRetriesPerSecond
     * 
     * Example:
     * - retryRatio: 0.2 (20% extra traffic allowed)
     * - minRetriesPerSecond: 10
     * - successful_requests: 100/sec
     * 
     * Allowed retries = 0.2 * 100 + 10 = 30 retries/sec
     * 
     * apiVersion: linkerd.io/v1alpha2
     * kind: ServiceProfile
     * metadata:
     *   name: payment-service.default.svc.cluster.local
     * spec:
     *   retryBudget:
     *     retryRatio: 0.2        # Max 20% additional traffic
     *     minRetriesPerSecond: 10  # At least 10 retries/sec
     *     ttl: 10s               # Budget refresh window
     */
}

/**
 * Example 5: Idempotent Operations
 */
class IdempotentOperationsConfiguration {
    
    /**
     * Only retry idempotent operations:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: order-service-idempotent
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   # Safe to retry: GET (read-only)
     *   - match:
     *     - method:
     *         exact: GET
     *     route:
     *     - destination:
     *         host: order-service
     *     retries:
     *       attempts: 5
     *       perTryTimeout: 2s
     *       retryOn: 5xx,reset,connect-failure
     *       
     *   # Safe to retry: PUT (idempotent update)
     *   - match:
     *     - method:
     *         exact: PUT
     *     route:
     *     - destination:
     *         host: order-service
     *     retries:
     *       attempts: 3
     *       perTryTimeout: 2s
     *       retryOn: 5xx
     *       
     *   # NOT safe to retry: POST (creates resource)
     *   - match:
     *     - method:
     *         exact: POST
     *     route:
     *     - destination:
     *         host: order-service
     *     # No retries - could create duplicates
     */
}

/**
 * Example 6: Conditional Retries
 */
class ConditionalRetriesConfiguration {
    
    /**
     * Retry based on response headers:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: conditional-retry
     * spec:
     *   hosts:
     *   - payment-service
     *   http:
     *   - route:
     *     - destination:
     *         host: payment-service
     *     retries:
     *       attempts: 3
     *       perTryTimeout: 2s
     *       retryOn: 5xx
     *       retryRemoteLocalities: true  # Retry on different availability zone
     * 
     * ---
     * Application returns retry headers:
     * 
     * HTTP/1.1 503 Service Unavailable
     * Retry-After: 5
     * X-Retry-Allowed: true
     */
}

/**
 * Example 7: Per-Route Retries
 */
class PerRouteRetriesConfiguration {
    
    /**
     * Different retry policies per route:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: per-route-retry
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   # Critical endpoint: aggressive retries
     *   - match:
     *     - uri:
     *         prefix: "/orders/checkout"
     *     route:
     *     - destination:
     *         host: order-service
     *     retries:
     *       attempts: 5
     *       perTryTimeout: 3s
     *       retryOn: 5xx,reset,connect-failure
     *       
     *   # Background job: fewer retries
     *   - match:
     *     - uri:
     *         prefix: "/orders/sync"
     *     route:
     *     - destination:
     *         host: order-service
     *     retries:
     *       attempts: 2
     *       perTryTimeout: 5s
     *       retryOn: 5xx
     *       
     *   # Default: moderate retries
     *   - route:
     *     - destination:
     *         host: order-service
     *     retries:
     *       attempts: 3
     *       perTryTimeout: 2s
     *       retryOn: 5xx,reset
     */
}

/**
 * Example 8: Retry with Hedging
 */
class HedgingConfiguration {
    
    /**
     * Send multiple requests simultaneously:
     * 
     * Consul service-router.hcl:
     * 
     * Kind = "service-router"
     * Name = "order-service"
     * Routes = [
     *   {
     *     Match {
     *       HTTP {
     *         PathPrefix = "/critical/"
     *       }
     *     }
     *     Destination {
     *       Service = "order-service"
     *       NumRetries = 3
     *       RetryOnStatusCodes = [500, 502, 503, 504]
     *       
     *       # Hedging: send duplicate request after delay
     *       RequestTimeout = "10s"
     *       # Send 2nd request after 500ms if no response
     *     }
     *   }
     * ]
     */
}

/**
 * Example 9: Retry Metrics and Monitoring
 */
class RetryMetricsConfiguration {
    
    /**
     * Monitor retry behavior:
     * 
     * # Total retries
     * sum(rate(envoy_cluster_upstream_rq_retry[5m]))
     *   by (envoy_cluster_name)
     * 
     * # Retry overflow (budget exceeded)
     * sum(rate(envoy_cluster_upstream_rq_retry_overflow[5m]))
     *   by (envoy_cluster_name)
     * 
     * # Retry success rate
     * sum(rate(envoy_cluster_upstream_rq_retry_success[5m]))
     *   / sum(rate(envoy_cluster_upstream_rq_retry[5m]))
     *   by (envoy_cluster_name)
     * 
     * # Alert on high retry rate
     * - alert: HighRetryRate
     *   expr: |
     *     sum(rate(envoy_cluster_upstream_rq_retry[5m]))
     *     / sum(rate(envoy_cluster_upstream_rq_total[5m])) > 0.1
     *   for: 5m
     *   annotations:
     *     summary: "High retry rate (>10%)"
     */
}

/**
 * Example 10: Best Practices
 */
class RetryBestPractices {
    
    /**
     * 1. Set appropriate retry limits:
     *    - Critical: 3-5 attempts
     *    - Standard: 2-3 attempts
     *    - Background: 1-2 attempts
     * 
     * 2. Use exponential backoff:
     *    - Prevents thundering herd
     *    - Add jitter (randomness)
     * 
     * 3. Implement retry budgets:
     *    - Limit total retry traffic
     *    - Prevent retry storms
     * 
     * 4. Only retry safe operations:
     *    - GET: Always safe
     *    - PUT/DELETE: If idempotent
     *    - POST: Usually not safe
     * 
     * 5. Set appropriate timeouts:
     *    - perTryTimeout < totalTimeout
     *    - Leave room for retries
     * 
     * 6. Monitor retry metrics:
     *    - Retry rate
     *    - Retry success rate
     *    - Budget exhaustion
     * 
     * 7. Retry on specific conditions:
     *    - Network errors: Always retry
     *    - 5xx errors: Retry selectively
     *    - 4xx errors: Usually don't retry
     * 
     * 8. Use circuit breakers with retries:
     *    - Prevent cascading failures
     *    - Fast fail when needed
     * 
     * Example configuration:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: best-practice-retry
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   - route:
     *     - destination:
     *         host: order-service
     *     timeout: 10s              # Total request timeout
     *     retries:
     *       attempts: 3              # Max 3 retries
     *       perTryTimeout: 3s        # 3s per attempt (9s total retry time)
     *       retryOn: 5xx,reset,connect-failure
     *       
     * ---
     * apiVersion: networking.istio.io/v1beta1
     * kind: DestinationRule
     * metadata:
     *   name: circuit-breaker
     * spec:
     *   host: order-service
     *   trafficPolicy:
     *     outlierDetection:
     *       consecutiveErrors: 5
     *       interval: 30s
     *       baseEjectionTime: 30s
     */
}

/**
 * Main Pattern Class
 */
public class RetryPolicyPattern {
    
    public static void main(String[] args) {
        System.out.println("Retry Policy Pattern");
        System.out.println("===================\n");
        
        System.out.println("Retry Strategies:");
        System.out.println("1. Fixed Retry - Same delay between attempts");
        System.out.println("2. Exponential Backoff - Increasing delays");
        System.out.println("3. Jittered Backoff - Random delays to prevent thundering herd");
        System.out.println("4. Retry Budget - Limit total retry traffic\n");
        
        System.out.println("Retry Conditions:");
        System.out.println("- 5xx errors (server errors)");
        System.out.println("- Connection failures");
        System.out.println("- Timeouts");
        System.out.println("- Reset streams");
        System.out.println("- Gateway errors (502, 503, 504)\n");
        
        System.out.println("Best Practices:");
        System.out.println("✓ Only retry idempotent operations");
        System.out.println("✓ Use exponential backoff with jitter");
        System.out.println("✓ Set retry budgets to prevent storms");
        System.out.println("✓ Monitor retry rates and success");
        System.out.println("✓ Combine with circuit breakers");
        System.out.println("✓ Set appropriate timeouts\n");
        
        System.out.println("Configuration:");
        System.out.println("- attempts: Max retry count");
        System.out.println("- perTryTimeout: Timeout per attempt");
        System.out.println("- retryOn: Conditions to retry");
        System.out.println("- retryBudget: Limit retry amplification");
    }
}
