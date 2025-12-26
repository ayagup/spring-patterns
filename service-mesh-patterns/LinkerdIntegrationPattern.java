package com.example.servicemesh;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

/**
 * Linkerd Integration Pattern
 * ===========================
 * 
 * Demonstrates integration with Linkerd service mesh.
 * 
 * Key Concepts:
 * ------------
 * 1. Linkerd Proxy - Ultra-light Rust-based proxy
 * 2. Automatic mTLS - Zero-config mutual TLS
 * 3. Golden Metrics - SUCCESS_RATE, LATENCY, RPS
 * 4. Service Profiles - Per-route metrics and retries
 * 5. Traffic Splits - Canary and blue-green deployments
 * 
 * Linkerd Architecture:
 * --------------------
 * Data Plane: linkerd2-proxy sidecars (Rust, 10MB memory)
 * Control Plane: destination, identity, proxy-injector
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Linkerd Installation
 */
class LinkerdInstallation {
    
    /**
     * Install Linkerd:
     * 
     * 1. Install CLI:
     *    curl --proto '=https' --tlsv1.2 -sSfL https://run.linkerd.io/install | sh
     *    export PATH=$PATH:$HOME/.linkerd2/bin
     *    
     * 2. Validate cluster:
     *    linkerd check --pre
     *    
     * 3. Install Linkerd:
     *    linkerd install | kubectl apply -f -
     *    
     * 4. Verify installation:
     *    linkerd check
     *    
     * 5. Enable viz extension:
     *    linkerd viz install | kubectl apply -f -
     *    
     * 6. View dashboard:
     *    linkerd viz dashboard
     */
}

/**
 * Example 2: Automatic Proxy Injection
 */
class ProxyInjectionExample {
    
    /**
     * deployment.yaml with annotation:
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
     *   template:
     *     metadata:
     *       labels:
     *         app: order-service
     *       annotations:
     *         linkerd.io/inject: enabled  # Enable proxy injection
     *     spec:
     *       containers:
     *       - name: order-service
     *         image: order-service:v1
     *         ports:
     *         - containerPort: 8080
     * 
     * ---
     * Or enable namespace-wide:
     * kubectl annotate namespace default linkerd.io/inject=enabled
     */
}

/**
 * Example 3: Service Profile for Per-Route Metrics
 */
class ServiceProfileExample {
    
    /**
     * service-profile.yaml:
     * 
     * apiVersion: linkerd.io/v1alpha2
     * kind: ServiceProfile
     * metadata:
     *   name: order-service.default.svc.cluster.local
     *   namespace: default
     * spec:
     *   routes:
     *   - name: GET /orders
     *     condition:
     *       method: GET
     *       pathRegex: /orders
     *     responseClasses:
     *     - condition:
     *         status:
     *           min: 200
     *           max: 299
     *       isFailure: false
     *     - condition:
     *         status:
     *           min: 500
     *           max: 599
     *       isFailure: true
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
     *         jitterRatio: 0.2
     * 
     * ---
     * Auto-generate from live traffic:
     * linkerd profile --tap deploy/order-service --tap-duration 30s
     */
}

/**
 * Example 4: Traffic Split for Canary Deployment
 */
class TrafficSplitExample {
    
    /**
     * traffic-split.yaml:
     * 
     * apiVersion: split.smi-spec.io/v1alpha1
     * kind: TrafficSplit
     * metadata:
     *   name: order-service-canary
     * spec:
     *   service: order-service
     *   backends:
     *   - service: order-service-v1
     *     weight: 90
     *   - service: order-service-v2
     *     weight: 10
     * 
     * ---
     * Progressive canary with Flagger:
     * 
     * apiVersion: flagger.app/v1beta1
     * kind: Canary
     * metadata:
     *   name: order-service
     * spec:
     *   targetRef:
     *     apiVersion: apps/v1
     *     kind: Deployment
     *     name: order-service
     *   service:
     *     port: 8080
     *   analysis:
     *     interval: 1m
     *     threshold: 5
     *     maxWeight: 50
     *     stepWeight: 10
     *     metrics:
     *     - name: request-success-rate
     *       thresholdRange:
     *         min: 99
     *     - name: request-duration
     *       thresholdRange:
     *         max: 500
     */
}

/**
 * Example 5: Automatic mTLS
 */
class AutomaticMTLSExample {
    
    /**
     * Linkerd automatically enables mTLS!
     * No configuration required.
     * 
     * Check mTLS status:
     * linkerd viz edges deployment/order-service
     * 
     * Output shows:
     * SRC           DST           SECURED
     * frontend      order-service √
     * 
     * Certificate rotation:
     * - Automatic every 24 hours
     * - Configured in identity controller
     * 
     * Custom certificate issuer:
     * linkerd install \
     *   --identity-trust-anchors-file ca.crt \
     *   --identity-issuer-certificate-file issuer.crt \
     *   --identity-issuer-key-file issuer.key
     */
}

/**
 * Example 6: Golden Metrics Monitoring
 */
@Service
class GoldenMetricsExample {
    
    /**
     * Linkerd automatically tracks:
     * 
     * 1. SUCCESS_RATE: % of successful requests
     * 2. REQUESTS_PER_SECOND: Traffic volume
     * 3. LATENCY (P50, P95, P99): Response times
     * 
     * View metrics:
     * linkerd viz stat deploy/order-service
     * 
     * Output:
     * NAME            MESHED   SUCCESS      RPS   LATENCY_P50   LATENCY_P95   LATENCY_P99
     * order-service      3/3   100.00%   2.5rps           5ms          10ms          20ms
     * 
     * Real-time traffic:
     * linkerd viz tap deploy/order-service
     * 
     * Top routes:
     * linkerd viz routes deploy/order-service
     */
    
    public void businessMethod() {
        System.out.println("Linkerd tracks golden metrics automatically");
    }
}

/**
 * Example 7: Retry Configuration
 */
class RetryConfigurationExample {
    
    /**
     * service-profile.yaml with retries:
     * 
     * apiVersion: linkerd.io/v1alpha2
     * kind: ServiceProfile
     * metadata:
     *   name: payment-service.default.svc.cluster.local
     * spec:
     *   retryBudget:
     *     retryRatio: 0.2        # Max 20% extra requests
     *     minRetriesPerSecond: 10
     *     ttl: 10s
     *     
     *   routes:
     *   - name: POST /payments
     *     condition:
     *       method: POST
     *       pathRegex: /payments
     *     isRetryable: true
     *     retries:
     *       limit: 3
     *       backoff:
     *         minBackoff: 100ms
     *         maxBackoff: 5s
     *         jitterRatio: 0.5
     */
}

/**
 * Example 8: Timeout Configuration
 */
class TimeoutConfigurationExample {
    
    /**
     * service-profile.yaml with timeouts:
     * 
     * apiVersion: linkerd.io/v1alpha2
     * kind: ServiceProfile
     * metadata:
     *   name: slow-service.default.svc.cluster.local
     * spec:
     *   routes:
     *   - name: GET /slow-operation
     *     condition:
     *       method: GET
     *       pathRegex: /slow-operation
     *     timeout: 10s  # Request timeout
     */
}

/**
 * Example 9: Multi-Cluster Communication
 */
class MultiClusterExample {
    
    /**
     * Setup multi-cluster:
     * 
     * 1. Install Linkerd on both clusters
     * 
     * 2. Install multi-cluster extension:
     *    linkerd multicluster install | kubectl apply -f -
     *    
     * 3. Link clusters:
     *    linkerd --context=east multicluster link --cluster-name east | \
     *      kubectl --context=west apply -f -
     *    
     * 4. Export service:
     *    kubectl --context=east label svc/order-service \
     *      mirror.linkerd.io/exported=true
     *      
     * 5. Service automatically available in west cluster as:
     *    order-service-east.default.svc.cluster.local
     */
}

/**
 * Example 10: Debugging with Tap and Top
 */
class DebuggingExample {
    
    /**
     * Real-time request tap:
     * linkerd viz tap deploy/order-service
     * 
     * Filter by method:
     * linkerd viz tap deploy/order-service --method POST
     * 
     * Filter by path:
     * linkerd viz tap deploy/order-service --path /orders
     * 
     * Filter by response code:
     * linkerd viz tap deploy/order-service --to deploy/payment-service
     * 
     * Top routes:
     * linkerd viz top deploy/order-service
     * 
     * Profile traffic:
     * linkerd profile --tap deploy/order-service --tap-duration 60s
     */
}

/**
 * Spring Boot Configuration
 */
@Configuration
class LinkerdSpringConfig {
    
    /**
     * No code changes required!
     * 
     * deployment.yaml:
     * 
     * apiVersion: apps/v1
     * kind: Deployment
     * metadata:
     *   name: order-service
     * spec:
     *   template:
     *     metadata:
     *       annotations:
     *         linkerd.io/inject: enabled
     *         config.linkerd.io/proxy-cpu-limit: "1"
     *         config.linkerd.io/proxy-cpu-request: "100m"
     *         config.linkerd.io/proxy-memory-limit: "100Mi"
     *         config.linkerd.io/proxy-memory-request: "20Mi"
     *     spec:
     *       containers:
     *       - name: order-service
     *         image: order-service:v1
     */
}

/**
 * Linkerd Extensions
 */
@ConfigurationProperties(prefix = "linkerd.extensions")
class LinkerdExtensions {
    
    /**
     * Install extensions:
     * 
     * 1. Viz (dashboard and metrics):
     *    linkerd viz install | kubectl apply -f -
     *    linkerd viz dashboard
     *    
     * 2. Jaeger (distributed tracing):
     *    linkerd jaeger install | kubectl apply -f -
     *    linkerd jaeger dashboard
     *    
     * 3. Multi-cluster:
     *    linkerd multicluster install | kubectl apply -f -
     *    
     * 4. SMI (Service Mesh Interface):
     *    kubectl apply -k github.com/linkerd/linkerd-smi
     */
}

/**
 * Main Pattern Class
 */
public class LinkerdIntegrationPattern {
    
    public static void main(String[] args) {
        System.out.println("Linkerd Integration Pattern");
        System.out.println("===========================\n");
        
        System.out.println("Linkerd Features:");
        System.out.println("✓ Ultra-light proxy (10MB memory, written in Rust)");
        System.out.println("✓ Automatic mTLS - zero configuration");
        System.out.println("✓ Golden metrics - success rate, RPS, latency");
        System.out.println("✓ Per-route metrics with Service Profiles");
        System.out.println("✓ Traffic splits for canary deployments");
        System.out.println("✓ Multi-cluster support\n");
        
        System.out.println("Golden Metrics:");
        System.out.println("1. SUCCESS_RATE - % successful requests");
        System.out.println("2. REQUESTS_PER_SECOND - Traffic volume");
        System.out.println("3. LATENCY (P50, P95, P99) - Response times\n");
        
        System.out.println("Key Components:");
        System.out.println("- ServiceProfile: Per-route config, retries, timeouts");
        System.out.println("- TrafficSplit: Canary and blue-green deployments");
        System.out.println("- Server: Authorization policies\n");
        
        System.out.println("Advantages over Istio:");
        System.out.println("- Simpler: Fewer concepts, easier to learn");
        System.out.println("- Lighter: 10MB vs 50MB memory per proxy");
        System.out.println("- Faster: Rust-based proxy, better performance");
        System.out.println("- Automatic: mTLS enabled by default\n");
        
        System.out.println("Quick Start:");
        System.out.println("1. curl -sL https://run.linkerd.io/install | sh");
        System.out.println("2. linkerd install | kubectl apply -f -");
        System.out.println("3. kubectl annotate namespace default linkerd.io/inject=enabled");
        System.out.println("4. linkerd viz install | kubectl apply -f -");
        System.out.println("5. linkerd viz dashboard");
    }
}
