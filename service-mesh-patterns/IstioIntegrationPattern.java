package com.example.servicemesh;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Istio Integration Pattern
 * =========================
 * 
 * Demonstrates integration with Istio service mesh platform.
 * 
 * Key Concepts:
 * ------------
 * 1. Envoy Sidecar - Automatic proxy injection
 * 2. Traffic Management - Routing, load balancing
 * 3. Security - mTLS, RBAC, Authorization
 * 4. Observability - Metrics, logs, traces
 * 5. Resilience - Retries, circuit breakers, timeouts
 * 
 * Istio Architecture:
 * ------------------
 * Data Plane: Envoy sidecars intercept all network traffic
 * Control Plane: Istiod manages configuration and certificates
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Virtual Service for Traffic Routing
 */
class VirtualServiceExample {
    
    /**
     * virtual-service.yaml:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: order-service
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   - match:
     *     - headers:
     *         user-type:
     *           exact: premium
     *     route:
     *     - destination:
     *         host: order-service
     *         subset: v2
     *   - route:
     *     - destination:
     *         host: order-service
     *         subset: v1
     */
}

/**
 * Example 2: Destination Rule for Load Balancing
 */
class DestinationRuleExample {
    
    /**
     * destination-rule.yaml:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: DestinationRule
     * metadata:
     *   name: order-service
     * spec:
     *   host: order-service
     *   trafficPolicy:
     *     loadBalancer:
     *       simple: LEAST_REQUEST
     *     connectionPool:
     *       tcp:
     *         maxConnections: 100
     *       http:
     *         http1MaxPendingRequests: 50
     *         http2MaxRequests: 100
     *     outlierDetection:
     *       consecutiveErrors: 5
     *       interval: 30s
     *       baseEjectionTime: 30s
     *       maxEjectionPercent: 50
     *   subsets:
     *   - name: v1
     *     labels:
     *       version: v1
     *   - name: v2
     *     labels:
     *       version: v2
     */
}

/**
 * Example 3: Gateway for External Traffic
 */
class GatewayExample {
    
    /**
     * gateway.yaml:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: Gateway
     * metadata:
     *   name: order-gateway
     * spec:
     *   selector:
     *     istio: ingressgateway
     *   servers:
     *   - port:
     *       number: 80
     *       name: http
     *       protocol: HTTP
     *     hosts:
     *     - "api.example.com"
     *   - port:
     *       number: 443
     *       name: https
     *       protocol: HTTPS
     *     tls:
     *       mode: SIMPLE
     *       credentialName: api-cert
     *     hosts:
     *     - "api.example.com"
     */
}

/**
 * Example 4: mTLS (Mutual TLS) Configuration
 */
class MTLSExample {
    
    /**
     * peer-authentication.yaml:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: PeerAuthentication
     * metadata:
     *   name: default
     *   namespace: default
     * spec:
     *   mtls:
     *     mode: STRICT  # STRICT, PERMISSIVE, DISABLE
     * 
     * ---
     * # Per-service mTLS
     * apiVersion: security.istio.io/v1beta1
     * kind: PeerAuthentication
     * metadata:
     *   name: order-service-mtls
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   mtls:
     *     mode: STRICT
     */
}

/**
 * Example 5: Authorization Policy (RBAC)
 */
class AuthorizationPolicyExample {
    
    /**
     * authorization-policy.yaml:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: order-service-authz
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: ALLOW
     *   rules:
     *   - from:
     *     - source:
     *         principals: ["cluster.local/ns/default/sa/frontend"]
     *     to:
     *     - operation:
     *         methods: ["GET", "POST"]
     *         paths: ["/orders/*"]
     *     when:
     *     - key: request.auth.claims[role]
     *       values: ["admin", "user"]
     */
}

/**
 * Example 6: Canary Deployment
 */
class CanaryDeploymentExample {
    
    /**
     * canary-virtual-service.yaml:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: order-service-canary
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   - match:
     *     - headers:
     *         canary:
     *           exact: "true"
     *     route:
     *     - destination:
     *         host: order-service
     *         subset: v2
     *   - route:
     *     - destination:
     *         host: order-service
     *         subset: v1
     *       weight: 90
     *     - destination:
     *         host: order-service
     *         subset: v2
     *       weight: 10
     */
}

/**
 * Example 7: Circuit Breaker
 */
class CircuitBreakerExample {
    
    /**
     * circuit-breaker.yaml:
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
     *       consecutiveErrors: 5
     *       interval: 10s
     *       baseEjectionTime: 30s
     *       maxEjectionPercent: 50
     *       minHealthPercent: 50
     */
}

/**
 * Example 8: Request Timeout
 */
class RequestTimeoutExample {
    
    /**
     * timeout-virtual-service.yaml:
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
     *     timeout: 10s
     *     retries:
     *       attempts: 3
     *       perTryTimeout: 3s
     *       retryOn: 5xx,reset,connect-failure,refused-stream
     */
}

/**
 * Example 9: Fault Injection (Chaos Testing)
 */
class FaultInjectionExample {
    
    /**
     * fault-injection.yaml:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: order-service-fault
     * spec:
     *   hosts:
     *   - order-service
     *   http:
     *   - fault:
     *       delay:
     *         percentage:
     *           value: 10.0
     *         fixedDelay: 5s
     *       abort:
     *         percentage:
     *           value: 5.0
     *         httpStatus: 500
     *     route:
     *     - destination:
     *         host: order-service
     */
}

/**
 * Example 10: Service Entry for External Services
 */
class ServiceEntryExample {
    
    /**
     * service-entry.yaml:
     * 
     * apiVersion: networking.istio.io/v1beta1
     * kind: ServiceEntry
     * metadata:
     *   name: external-payment-api
     * spec:
     *   hosts:
     *   - api.payment-provider.com
     *   ports:
     *   - number: 443
     *     name: https
     *     protocol: HTTPS
     *   location: MESH_EXTERNAL
     *   resolution: DNS
     * 
     * ---
     * # Virtual Service for external service
     * apiVersion: networking.istio.io/v1beta1
     * kind: VirtualService
     * metadata:
     *   name: external-payment-api
     * spec:
     *   hosts:
     *   - api.payment-provider.com
     *   tls:
     *   - match:
     *     - port: 443
     *       sniHosts:
     *       - api.payment-provider.com
     *     route:
     *     - destination:
     *         host: api.payment-provider.com
     *         port:
     *           number: 443
     *     timeout: 30s
     */
}

/**
 * Installation and Setup
 */
@Configuration
class IstioSetup {
    
    /**
     * Install Istio:
     * 
     * 1. Download Istio:
     *    curl -L https://istio.io/downloadIstio | sh -
     *    cd istio-*
     *    export PATH=$PWD/bin:$PATH
     *    
     * 2. Install Istio on Kubernetes:
     *    istioctl install --set profile=demo -y
     *    
     * 3. Enable sidecar injection:
     *    kubectl label namespace default istio-injection=enabled
     *    
     * 4. Deploy application:
     *    kubectl apply -f deployment.yaml
     *    
     * 5. Access Istio dashboard:
     *    istioctl dashboard kiali
     *    istioctl dashboard jaeger
     *    istioctl dashboard grafana
     *    istioctl dashboard prometheus
     */
}

/**
 * Spring Boot Application Configuration
 */
@Service
class SpringBootIstioExample {
    
    /**
     * No code changes required!
     * Istio sidecar automatically intercepts traffic.
     * 
     * deployment.yaml:
     * 
     * apiVersion: apps/v1
     * kind: Deployment
     * metadata:
     *   name: order-service
     * spec:
     *   replicas: 3
     *   selector:
     *     matchLabels:
     *       app: order-service
     *       version: v1
     *   template:
     *     metadata:
     *       labels:
     *         app: order-service
     *         version: v1
     *       annotations:
     *         sidecar.istio.io/inject: "true"
     *     spec:
     *       containers:
     *       - name: order-service
     *         image: order-service:v1
     *         ports:
     *         - containerPort: 8080
     *         env:
     *         - name: SPRING_PROFILES_ACTIVE
     *           value: kubernetes
     */
    
    public void businessLogic() {
        // Your Spring Boot code - no changes needed!
        System.out.println("Istio handles traffic management automatically");
    }
}

/**
 * Observability Configuration
 */
@ConfigurationProperties(prefix = "istio.observability")
class IstioObservability {
    
    private Map<String, String> metrics = new HashMap<>();
    
    /**
     * Istio automatically provides:
     * 
     * 1. Metrics (Prometheus):
     *    - Request rate, duration, size
     *    - Success/error rates
     *    - TCP connections
     *    
     * 2. Traces (Jaeger):
     *    - Distributed tracing
     *    - Service dependencies
     *    
     * 3. Logs (Envoy):
     *    - Access logs
     *    - Audit logs
     *    
     * 4. Visualization (Kiali):
     *    - Service graph
     *    - Traffic flow
     *    - Health status
     */
    
    public Map<String, String> getMetrics() {
        return metrics;
    }
    
    public void setMetrics(Map<String, String> metrics) {
        this.metrics = metrics;
    }
}

/**
 * Main Pattern Class
 */
public class IstioIntegrationPattern {
    
    public static void main(String[] args) {
        System.out.println("Istio Integration Pattern");
        System.out.println("=========================\n");
        
        System.out.println("Istio Features:");
        System.out.println("✓ Traffic Management - Routing, load balancing, canary");
        System.out.println("✓ Security - mTLS, RBAC, authentication");
        System.out.println("✓ Observability - Metrics, logs, traces");
        System.out.println("✓ Resilience - Retries, circuit breakers, timeouts");
        System.out.println("✓ Policy Enforcement - Rate limiting, quotas\n");
        
        System.out.println("Key Components:");
        System.out.println("1. VirtualService - Traffic routing rules");
        System.out.println("2. DestinationRule - Load balancing, circuit breaker");
        System.out.println("3. Gateway - External traffic entry");
        System.out.println("4. ServiceEntry - External services");
        System.out.println("5. PeerAuthentication - mTLS configuration");
        System.out.println("6. AuthorizationPolicy - Access control\n");
        
        System.out.println("Benefits:");
        System.out.println("- Zero code changes required");
        System.out.println("- Automatic mTLS encryption");
        System.out.println("- Advanced traffic management");
        System.out.println("- Built-in observability");
        System.out.println("- Consistent security policies");
        System.out.println("- Service mesh wide resilience\n");
        
        System.out.println("Use Cases:");
        System.out.println("- Microservices communication");
        System.out.println("- Canary/blue-green deployments");
        System.out.println("- Service-to-service authentication");
        System.out.println("- Traffic splitting and routing");
        System.out.println("- Chaos engineering (fault injection)");
    }
}
