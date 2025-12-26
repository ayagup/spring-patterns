package com.example.servicemesh;

/**
 * Circuit Breaking Pattern
 * ========================
 * 
 * Demonstrates circuit breaker pattern in service mesh.
 * 
 * Key Concepts:
 * ------------
 * 1. Circuit States - CLOSED, OPEN, HALF_OPEN
 * 2. Failure Threshold - When to open circuit
 * 3. Outlier Detection - Remove unhealthy instances
 * 4. Connection Limits - Prevent resource exhaustion
 * 5. Fast Fail - Immediate failure when open
 * 
 * Circuit States:
 * --------------
 * CLOSED: Normal operation, requests pass through
 * OPEN: Circuit tripped, requests fail immediately
 * HALF_OPEN: Testing if service recovered
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Circuit Breaker
 */
class BasicCircuitBreakerConfiguration {
    
    /**
     * Istio DestinationRule with circuit breaker:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: DestinationRule
     * metadata:
     *   name: order-service-circuit-breaker
     * spec:
     *   host: order-service
     *   trafficPolicy:
     *     connectionPool:
     *       tcp:
     *         maxConnections: 100
     *       http:
     *         http1MaxPendingRequests: 10
     *         http2MaxRequests: 100
     *         maxRequestsPerConnection: 2
     *     outlierDetection:
     *       consecutiveErrors: 5       # Open after 5 consecutive errors
     *       interval: 10s              # Analysis window
     *       baseEjectionTime: 30s      # How long to eject instance
     *       maxEjectionPercent: 50     # Max % of instances to eject
     *       minHealthPercent: 50       # Min healthy instances required
     */
}

/**
 * Example 2: Connection Pool Circuit Breaker
 */
class ConnectionPoolCircuitBreakerConfiguration {
    
    /**
     * Prevent connection pool exhaustion:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: DestinationRule
     * metadata:
     *   name: connection-pool-cb
     * spec:
     *   host: payment-service
     *   trafficPolicy:
     *     connectionPool:
     *       tcp:
     *         maxConnections: 50           # Max TCP connections
     *         connectTimeout: 3s
     *         tcpKeepalive:
     *           time: 7200s
     *           interval: 75s
     *       http:
     *         http1MaxPendingRequests: 5   # Max pending HTTP/1.1
     *         http2MaxRequests: 50         # Max concurrent HTTP/2
     *         maxRequestsPerConnection: 1  # Prevent connection reuse issues
     *         maxRetries: 3
     *         idleTimeout: 300s
     * 
     * When limits exceeded:
     * - Returns 503 Service Unavailable
     * - Prevents cascading failures
     * - Protects downstream service
     */
}

/**
 * Example 3: Outlier Detection
 */
class OutlierDetectionConfiguration {
    
    /**
     * Detect and remove unhealthy instances:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: DestinationRule
     * metadata:
     *   name: outlier-detection
     * spec:
     *   host: order-service
     *   trafficPolicy:
     *     outlierDetection:
     *       # Consecutive 5xx errors
     *       consecutiveErrors: 5
     *       consecutive5xxErrors: 5
     *       
     *       # Gateway errors (502, 503, 504)
     *       consecutiveGatewayErrors: 3
     *       
     *       # Detection interval
     *       interval: 10s
     *       
     *       # Ejection time
     *       baseEjectionTime: 30s        # Initial ejection time
     *       maxEjectionPercent: 50       # Don't eject more than 50%
     *       
     *       # Health requirements
     *       minHealthPercent: 50         # Need 50% healthy instances
     *       
     *       # Success rate ejection
     *       splitExternalLocalOriginErrors: true
     *       enforcingSuccessRate: 100
     *       successRateMinimumHosts: 5
     *       successRateRequestVolume: 100
     *       successRateStdevFactor: 1900  # 19x standard deviation
     */
}

/**
 * Example 4: Per-Subset Circuit Breakers
 */
class PerSubsetCircuitBreakerConfiguration {
    
    /**
     * Different circuit breakers for different versions:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: DestinationRule
     * metadata:
     *   name: per-subset-cb
     * spec:
     *   host: order-service
     *   subsets:
     *   - name: v1
     *     labels:
     *       version: v1
     *     trafficPolicy:
     *       connectionPool:
     *         http:
     *           http2MaxRequests: 100
     *       outlierDetection:
     *         consecutiveErrors: 5
     *         interval: 10s
     *         baseEjectionTime: 30s
     *         
     *   - name: v2-canary
     *     labels:
     *       version: v2
     *     trafficPolicy:
     *       connectionPool:
     *         http:
     *           http2MaxRequests: 50   # More conservative for canary
     *       outlierDetection:
     *         consecutiveErrors: 3     # Faster failure detection
     *         interval: 5s
     *         baseEjectionTime: 60s    # Longer ejection time
     */
}

/**
 * Example 5: Panic Threshold
 */
class PanicThresholdConfiguration {
    
    /**
     * Load balancing panic threshold:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: DestinationRule
     * metadata:
     *   name: panic-threshold
     * spec:
     *   host: order-service
     *   trafficPolicy:
     *     loadBalancer:
     *       simple: LEAST_REQUEST
     *       localityLbSetting:
     *         enabled: true
     *     outlierDetection:
     *       minHealthPercent: 50      # Panic if < 50% healthy
     *       
     * Panic mode behavior:
     * - When healthy instances < minHealthPercent
     * - Load balancer ignores health checks
     * - Distributes traffic to all instances
     * - Prevents total service failure
     */
}

/**
 * Example 6: Linkerd Circuit Breaker
 */
class LinkerdCircuitBreakerConfiguration {
    
    /**
     * Linkerd uses passive health checking:
     * 
     * apiVersion: linkerd.io/v1alpha2
     * kind: ServiceProfile
     * metadata:
     *   name: order-service.default.svc.cluster.local
     * spec:
     *   routes:
     *   - name: POST /orders
     *     condition:
     *       method: POST
     *       pathRegex: /orders
     *     timeout: 10s
     *     retries:
     *       limit: 3
     *       backoff:
     *         minBackoff: 100ms
     *         maxBackoff: 1s
     *         
     * Linkerd automatically:
     * - Detects failing endpoints
     * - Removes from load balancing
     * - Retries with healthy instances
     */
}

/**
 * Example 7: Consul Connect Circuit Breaker
 */
class ConsulConnectCircuitBreakerConfiguration {
    
    /**
     * Consul service-defaults.hcl:
     * 
     * Kind = "service-defaults"
     * Name = "order-service"
     * Protocol = "http"
     * 
     * UpstreamConfig {
     *   Defaults {
     *     Limits {
     *       MaxConnections = 100
     *       MaxPendingRequests = 50
     *       MaxConcurrentRequests = 100
     *     }
     *   }
     * }
     * 
     * PassiveHealthCheck {
     *   MaxFailures = 5
     *   Interval = "10s"
     *   EnforcingConsecutive5xx = 100
     * }
     */
}

/**
 * Example 8: Circuit Breaker with Fallback
 */
class CircuitBreakerWithFallbackConfiguration {
    
    /**
     * Fallback when circuit is open:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: circuit-breaker-fallback
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   - fault:
     *       abort:
     *         percentage:
     *           value: 0
     *         httpStatus: 503
     *     route:
     *     - destination:
     *         host: order-service
     *       weight: 100
     *     - destination:
     *         host: order-service-fallback  # Fallback service
     *       weight: 0
     *     
     * ---
     * Application-level fallback:
     * 
     * @Service
     * public class OrderService {
     *     public Order getOrder(String id) {
     *         try {
     *             return orderClient.getOrder(id);
     *         } catch (CircuitBreakerOpenException e) {
     *             return getOrderFromCache(id);  // Fallback
     *         }
     *     }
     * }
     */
}

/**
 * Example 9: Circuit Breaker Metrics
 */
class CircuitBreakerMetricsConfiguration {
    
    /**
     * Monitor circuit breaker state:
     * 
     * # Ejections (circuit opened)
     * sum(rate(envoy_cluster_outlier_detection_ejections_active[5m]))
     *   by (envoy_cluster_name)
     * 
     * # Overflow (connection pool full)
     * sum(rate(envoy_cluster_upstream_cx_overflow[5m]))
     *   by (envoy_cluster_name)
     * 
     * # Ejection percentage
     * sum(envoy_cluster_outlier_detection_ejections_active)
     *   / sum(envoy_cluster_membership_total)
     *   by (envoy_cluster_name)
     * 
     * # Alerts
     * - alert: CircuitBreakerOpen
     *   expr: |
     *     sum(envoy_cluster_outlier_detection_ejections_active)
     *     / sum(envoy_cluster_membership_total) > 0.5
     *   for: 5m
     *   annotations:
     *     summary: "Circuit breaker ejected >50% of instances"
     * 
     * - alert: ConnectionPoolOverflow
     *   expr: |
     *     sum(rate(envoy_cluster_upstream_cx_overflow[5m])) > 10
     *   for: 5m
     *   annotations:
     *     summary: "Connection pool overflow"
     */
}

/**
 * Example 10: Circuit Breaker Best Practices
 */
class CircuitBreakerBestPractices {
    
    /**
     * 1. Set appropriate thresholds:
     *    - consecutiveErrors: 3-5 errors
     *    - interval: 10-30s
     *    - baseEjectionTime: 30-60s
     *    - maxEjectionPercent: 50-75%
     * 
     * 2. Connection pool limits:
     *    - Based on expected load
     *    - Leave headroom for spikes
     *    - Monitor utilization
     * 
     * 3. Health check requirements:
     *    - minHealthPercent: 50% (prevent total failure)
     *    - successRateMinimumHosts: 5 (enough for statistics)
     * 
     * 4. Ejection time strategy:
     *    - Short for transient failures (30s)
     *    - Long for persistent issues (60s+)
     *    - Exponential backoff possible
     * 
     * 5. Combine with other patterns:
     *    - Retries: Retry before circuit opens
     *    - Timeouts: Prevent slow requests
     *    - Fallbacks: Graceful degradation
     * 
     * 6. Monitor key metrics:
     *    - Ejection rate
     *    - Pool overflow rate
     *    - Error rates
     *    - Recovery time
     * 
     * Complete example:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: DestinationRule
     * metadata:
     *   name: production-circuit-breaker
     * spec:
     *   host: order-service
     *   trafficPolicy:
     *     connectionPool:
     *       tcp:
     *         maxConnections: 100
     *         connectTimeout: 3s
     *       http:
     *         http1MaxPendingRequests: 10
     *         http2MaxRequests: 100
     *         maxRequestsPerConnection: 2
     *         maxRetries: 3
     *         idleTimeout: 300s
     *     outlierDetection:
     *       consecutiveErrors: 5
     *       consecutive5xxErrors: 5
     *       interval: 10s
     *       baseEjectionTime: 30s
     *       maxEjectionPercent: 50
     *       minHealthPercent: 50
     *       splitExternalLocalOriginErrors: true
     *       enforcingSuccessRate: 100
     */
}

/**
 * Main Pattern Class
 */
public class CircuitBreakingPattern {
    
    public static void main(String[] args) {
        System.out.println("Circuit Breaking Pattern");
        System.out.println("=======================\n");
        
        System.out.println("Circuit States:");
        System.out.println("1. CLOSED - Normal operation");
        System.out.println("2. OPEN - Circuit tripped, fast fail");
        System.out.println("3. HALF_OPEN - Testing recovery\n");
        
        System.out.println("Key Components:");
        System.out.println("- Connection Pool: Limit concurrent connections");
        System.out.println("- Outlier Detection: Remove unhealthy instances");
        System.out.println("- Ejection Time: How long to exclude instance");
        System.out.println("- Health Threshold: Minimum healthy instances\n");
        
        System.out.println("Benefits:");
        System.out.println("✓ Prevent cascading failures");
        System.out.println("✓ Fast fail when service unhealthy");
        System.out.println("✓ Automatic recovery testing");
        System.out.println("✓ Resource protection");
        System.out.println("✓ Improved system stability\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Set realistic thresholds");
        System.out.println("- Combine with retries and timeouts");
        System.out.println("- Implement fallback strategies");
        System.out.println("- Monitor circuit state");
        System.out.println("- Maintain minimum healthy instances");
    }
}
