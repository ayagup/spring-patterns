package com.example.servicemesh;

/**
 * Timeout Policy Pattern
 * ======================
 * 
 * Demonstrates timeout policies in service mesh.
 * 
 * Key Concepts:
 * ------------
 * 1. Request Timeout - Maximum time for complete request
 * 2. Connection Timeout - Time to establish connection
 * 3. Idle Timeout - Maximum idle time
 * 4. Per-Try Timeout - Timeout per retry attempt
 * 5. Stream Timeout - Timeout for streaming requests
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Request Timeout
 */
class BasicRequestTimeoutConfiguration {
    
    /**
     * Istio VirtualService with timeout:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: order-service-timeout
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   - route:
     *     - destination:
     *         host: order-service
     *     timeout: 10s  # Total request timeout
     */
}

/**
 * Example 2: Per-Route Timeouts
 */
class PerRouteTimeoutConfiguration {
    
    /**
     * Different timeouts for different endpoints:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: per-route-timeout
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   # Fast endpoint: short timeout
     *   - match:
     *     - uri:
     *         prefix: "/orders/status"
     *     route:
     *     - destination:
     *         host: order-service
     *     timeout: 2s
     *     
     *   # Batch processing: long timeout
     *   - match:
     *     - uri:
     *         prefix: "/orders/export"
     *     route:
     *     - destination:
     *         host: order-service
     *     timeout: 60s
     *     
     *   # Default: moderate timeout
     *   - route:
     *     - destination:
     *         host: order-service
     *     timeout: 10s
     */
}

/**
 * Example 3: Connection and Idle Timeouts
 */
class ConnectionIdleTimeoutConfiguration {
    
    /**
     * Istio DestinationRule with connection settings:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: DestinationRule
     * metadata:
     *   name: connection-timeout
     * spec:
     *   host: order-service
     *   trafficPolicy:
     *     connectionPool:
     *       tcp:
     *         maxConnections: 100
     *         connectTimeout: 3s      # Connection establishment timeout
     *         tcpKeepalive:
     *           time: 7200s
     *           interval: 75s
     *           probes: 10
     *       http:
     *         http1MaxPendingRequests: 50
     *         http2MaxRequests: 100
     *         maxRequestsPerConnection: 2
     *         maxRetries: 3
     *         idleTimeout: 3600s     # Idle connection timeout
     *         h2UpgradePolicy: UPGRADE
     */
}

/**
 * Example 4: Timeout with Retries
 */
class TimeoutWithRetriesConfiguration {
    
    /**
     * Coordinate timeouts with retries:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: timeout-retry
     * spec:
     *   hosts:
     *   - payment-service
     *   http:
     *   - route:
     *     - destination:
     *         host: payment-service
     *     timeout: 10s           # Total timeout (must be > perTryTimeout * attempts)
     *     retries:
     *       attempts: 3
     *       perTryTimeout: 3s    # 3s per attempt, 9s max for retries
     *       retryOn: 5xx,reset,connect-failure
     * 
     * Calculation:
     * - Per-try timeout: 3s
     * - Max attempts: 3
     * - Max retry time: 3s * 3 = 9s
     * - Total timeout: 10s (should be >= 9s)
     */
}

/**
 * Example 5: Streaming Timeouts
 */
class StreamingTimeoutConfiguration {
    
    /**
     * WebSocket and gRPC streaming:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: streaming-timeout
     * spec:
     *   hosts:
     *   - streaming-service
     *   http:
     *   - match:
     *     - uri:
     *         prefix: "/stream"
     *     route:
     *     - destination:
     *         host: streaming-service
     *     timeout: 0s  # Disable timeout for streaming
     * 
     * ---
     * DestinationRule for streaming:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: DestinationRule
     * metadata:
     *   name: streaming-connection
     * spec:
     *   host: streaming-service
     *   trafficPolicy:
     *     connectionPool:
     *       http:
     *         idleTimeout: 7200s     # 2 hours for long streams
     *         http2MaxRequests: 1000
     */
}

/**
 * Example 6: Timeout Headers
 */
class TimeoutHeadersConfiguration {
    
    /**
     * Client-specified timeouts via headers:
     * 
     * Request headers:
     * grpc-timeout: 10S
     * x-envoy-upstream-rq-timeout-ms: 10000
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: header-timeout
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   - match:
     *     - headers:
     *         x-custom-timeout:
     *           regex: "\\d+"
     *     route:
     *     - destination:
     *         host: order-service
     *     headers:
     *       request:
     *         set:
     *           x-envoy-upstream-rq-timeout-ms: "%REQ(x-custom-timeout)%"
     */
}

/**
 * Example 7: Cascading Timeouts
 */
class CascadingTimeoutsConfiguration {
    
    /**
     * Timeout hierarchy for service chains:
     * 
     * Client -> Frontend (20s) -> Order Service (15s) -> Payment Service (10s)
     * 
     * # Frontend VirtualService
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: frontend
     * spec:
     *   hosts:
     *   - frontend
     *   http:
     *   - route:
     *     - destination:
     *         host: frontend
     *     timeout: 20s
     * 
     * ---
     * # Order Service VirtualService
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: order-service
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   - route:
     *     - destination:
     *         host: order-service
     *     timeout: 15s
     * 
     * ---
     * # Payment Service VirtualService
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: payment-service
     * spec:
     *   hosts:
     *   - payment-service
     *   http:
     *   - route:
     *     - destination:
     *         host: payment-service
     *     timeout: 10s
     */
}

/**
 * Example 8: Linkerd Timeouts
 */
class LinkerdTimeoutConfiguration {
    
    /**
     * Linkerd ServiceProfile:
     * 
     * apiVersion: linkerd.io/v1alpha2
     * kind: ServiceProfile
     * metadata:
     *   name: order-service.default.svc.cluster.local
     * spec:
     *   routes:
     *   - name: GET /orders
     *     condition:
     *       method: GET
     *       pathRegex: /orders
     *     timeout: 5s
     *     
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
     */
}

/**
 * Example 9: Consul Connect Timeouts
 */
class ConsulConnectTimeoutConfiguration {
    
    /**
     * Consul service-resolver.hcl:
     * 
     * Kind = "service-resolver"
     * Name = "order-service"
     * 
     * ConnectTimeout = "5s"
     * 
     * RequestTimeout = "10s"
     * 
     * Subsets = {
     *   "v1" = {
     *     Filter = "Service.Tags contains \"version=v1\""
     *   }
     *   "v2" = {
     *     Filter = "Service.Tags contains \"version=v2\""
     *   }
     * }
     * 
     * ---
     * service-router.hcl with route timeouts:
     * 
     * Kind = "service-router"
     * Name = "order-service"
     * Routes = [
     *   {
     *     Match {
     *       HTTP {
     *         PathPrefix = "/fast"
     *       }
     *     }
     *     Destination {
     *       Service = "order-service"
     *       RequestTimeout = "2s"
     *     }
     *   },
     *   {
     *     Match {
     *       HTTP {
     *         PathPrefix = "/slow"
     *       }
     *     }
     *     Destination {
     *       Service = "order-service"
     *       RequestTimeout = "30s"
     *     }
     *   }
     * ]
     */
}

/**
 * Example 10: Timeout Best Practices
 */
class TimeoutBestPractices {
    
    /**
     * 1. Set realistic timeouts:
     *    - Based on SLA requirements
     *    - Based on P99 latency measurements
     *    - Add buffer for retries
     * 
     * 2. Timeout hierarchy:
     *    Client timeout > Service1 timeout > Service2 timeout
     *    
     *    Example:
     *    - Client: 30s
     *    - Service A: 25s
     *    - Service B: 20s
     *    - Service C: 15s
     * 
     * 3. Coordinate with retries:
     *    total_timeout > perTryTimeout * max_attempts
     *    
     *    Example:
     *    - perTryTimeout: 3s
     *    - attempts: 3
     *    - total_timeout: >= 10s (3*3 + buffer)
     * 
     * 4. Different timeouts per operation:
     *    - Read: Short (1-5s)
     *    - Write: Medium (5-10s)
     *    - Batch: Long (30-60s)
     *    - Streaming: Disabled (0s)
     * 
     * 5. Handle timeouts gracefully:
     *    - Return 504 Gateway Timeout
     *    - Log timeout events
     *    - Monitor timeout rates
     * 
     * 6. Connection pool timeouts:
     *    - connectTimeout: 3-5s
     *    - idleTimeout: 300-3600s
     *    - maxConnectionDuration: 0 (unlimited)
     * 
     * Example configuration:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: timeout-best-practice
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   # Fast read operations
     *   - match:
     *     - method:
     *         exact: GET
     *     route:
     *     - destination:
     *         host: order-service
     *     timeout: 5s
     *     retries:
     *       attempts: 3
     *       perTryTimeout: 1s
     *       
     *   # Write operations
     *   - match:
     *     - method:
     *         regex: "POST|PUT|PATCH"
     *     route:
     *     - destination:
     *         host: order-service
     *     timeout: 15s
     *     retries:
     *       attempts: 2
     *       perTryTimeout: 5s
     * 
     * ---
     * Monitoring timeouts:
     * 
     * # Timeout rate
     * sum(rate(envoy_cluster_upstream_rq_timeout[5m]))
     *   / sum(rate(envoy_cluster_upstream_rq_total[5m]))
     *   by (envoy_cluster_name)
     * 
     * # Alert on high timeout rate
     * - alert: HighTimeoutRate
     *   expr: |
     *     sum(rate(envoy_cluster_upstream_rq_timeout[5m]))
     *     / sum(rate(envoy_cluster_upstream_rq_total[5m])) > 0.01
     *   for: 5m
     *   annotations:
     *     summary: "High timeout rate (>1%)"
     */
}

/**
 * Main Pattern Class
 */
public class TimeoutPolicyPattern {
    
    public static void main(String[] args) {
        System.out.println("Timeout Policy Pattern");
        System.out.println("=====================\n");
        
        System.out.println("Timeout Types:");
        System.out.println("1. Request Timeout - Total time for request");
        System.out.println("2. Connection Timeout - Time to establish connection");
        System.out.println("3. Idle Timeout - Maximum idle time");
        System.out.println("4. Per-Try Timeout - Timeout per retry attempt");
        System.out.println("5. Stream Timeout - For long-lived streams\n");
        
        System.out.println("Best Practices:");
        System.out.println("✓ Set realistic timeouts based on SLAs");
        System.out.println("✓ Use timeout hierarchy (cascade)");
        System.out.println("✓ Coordinate with retry policies");
        System.out.println("✓ Different timeouts per operation type");
        System.out.println("✓ Monitor timeout rates");
        System.out.println("✓ Handle timeouts gracefully\n");
        
        System.out.println("Timeout Formula:");
        System.out.println("total_timeout >= perTryTimeout * max_attempts + buffer\n");
        
        System.out.println("Recommended Values:");
        System.out.println("- Connection: 3-5s");
        System.out.println("- Fast operations: 1-5s");
        System.out.println("- Standard operations: 5-15s");
        System.out.println("- Batch operations: 30-60s");
        System.out.println("- Streaming: Disabled (0s) or very long");
    }
}
