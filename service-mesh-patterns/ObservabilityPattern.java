package com.example.servicemesh;

/**
 * Observability Pattern
 * =====================
 * 
 * Demonstrates observability in service mesh (metrics, logs, traces).
 * 
 * Key Concepts:
 * ------------
 * 1. Metrics - Prometheus, Grafana
 * 2. Distributed Tracing - Jaeger, Zipkin
 * 3. Access Logs - Envoy logs
 * 4. Service Graph - Kiali
 * 5. Golden Signals - Latency, Traffic, Errors, Saturation
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Prometheus Metrics
 */
class PrometheusMetricsConfiguration {
    
    /**
     * Istio automatically exposes Prometheus metrics:
     * 
     * Service Metrics:
     * - istio_requests_total: Total requests
     * - istio_request_duration_milliseconds: Request duration
     * - istio_request_bytes: Request size
     * - istio_response_bytes: Response size
     * - istio_tcp_connections_opened_total: TCP connections
     * - istio_tcp_connections_closed_total: Closed connections
     * 
     * Scrape configuration:
     * 
     * apiVersion: v1
     * kind: ConfigMap
     * metadata:
     *   name: prometheus
     * data:
     *   prometheus.yml: |
     *     global:
     *       scrape_interval: 15s
     *     scrape_configs:
     *     - job_name: 'istio-mesh'
     *       kubernetes_sd_configs:
     *       - role: endpoints
     *         namespaces:
     *           names:
     *           - istio-system
     *       relabel_configs:
     *       - source_labels: [__meta_kubernetes_service_name]
     *         action: keep
     *         regex: istio-telemetry
     */
}

/**
 * Example 2: Grafana Dashboards
 */
class GrafanaDashboardsConfiguration {
    
    /**
     * Pre-built Istio dashboards:
     * 
     * 1. Istio Mesh Dashboard:
     *    - Global request volume
     *    - Global success rate
     *    - 4xx/5xx errors
     *    - P50, P90, P99 latencies
     *    
     * 2. Istio Service Dashboard:
     *    - Service-specific metrics
     *    - Incoming/outgoing requests
     *    - Request duration
     *    - Success rates
     *    
     * 3. Istio Workload Dashboard:
     *    - Pod-level metrics
     *    - Resource usage
     *    - Network I/O
     *    
     * 4. Istio Performance Dashboard:
     *    - Control plane metrics
     *    - Data plane metrics
     *    - Pilot sync time
     *    
     * Import dashboards:
     * - Mesh: ID 7639
     * - Service: ID 7636
     * - Workload: ID 7630
     */
}

/**
 * Example 3: Distributed Tracing with Jaeger
 */
class JaegerTracingConfiguration {
    
    /**
     * Enable Jaeger tracing:
     * 
     * apiVersion: install.istio.io/v1alpha1
     * kind: IstioOperator
     * spec:
     *   meshConfig:
     *     enableTracing: true
     *     defaultConfig:
     *       tracing:
     *         sampling: 100.0  # 100% sampling (adjust for production)
     *         zipkin:
     *           address: jaeger-collector.istio-system.svc:9411
     * 
     * ---
     * Jaeger deployment:
     * 
     * kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.14/samples/addons/jaeger.yaml
     * 
     * Access Jaeger UI:
     * istioctl dashboard jaeger
     * 
     * Trace propagation headers:
     * - x-request-id
     * - x-b3-traceid
     * - x-b3-spanid
     * - x-b3-parentspanid
     * - x-b3-sampled
     * - x-b3-flags
     */
}

/**
 * Example 4: Access Logs
 */
class AccessLogsConfiguration {
    
    /**
     * Enable access logs:
     * 
     * apiVersion: v1
     * kind: ConfigMap
     * metadata:
     *   name: istio
     *   namespace: istio-system
     * data:
     *   mesh: |
     *     accessLogFile: /dev/stdout
     *     accessLogEncoding: JSON
     *     accessLogFormat: |
     *       {
     *         "start_time": "%START_TIME%",
     *         "method": "%REQ(:METHOD)%",
     *         "path": "%REQ(X-ENVOY-ORIGINAL-PATH?:PATH)%",
     *         "protocol": "%PROTOCOL%",
     *         "response_code": "%RESPONSE_CODE%",
     *         "response_flags": "%RESPONSE_FLAGS%",
     *         "bytes_received": "%BYTES_RECEIVED%",
     *         "bytes_sent": "%BYTES_SENT%",
     *         "duration": "%DURATION%",
     *         "upstream_service_time": "%RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)%",
     *         "x_forwarded_for": "%REQ(X-FORWARDED-FOR)%",
     *         "user_agent": "%REQ(USER-AGENT)%",
     *         "request_id": "%REQ(X-REQUEST-ID)%",
     *         "authority": "%REQ(:AUTHORITY)%",
     *         "upstream_host": "%UPSTREAM_HOST%",
     *         "upstream_cluster": "%UPSTREAM_CLUSTER%"
     *       }
     * 
     * ---
     * Conditional logging (only errors):
     * 
     * apiVersion: telemetry.istio.io/v1alpha1
     * kind: Telemetry
     * metadata:
     *   name: error-logging
     * spec:
     *   accessLogging:
     *   - providers:
     *     - name: envoy
     *     filter:
     *       expression: response.code >= 400
     */
}

/**
 * Example 5: Kiali Service Graph
 */
class KialiServiceGraphConfiguration {
    
    /**
     * Install Kiali:
     * 
     * kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.14/samples/addons/kiali.yaml
     * 
     * Access Kiali:
     * istioctl dashboard kiali
     * 
     * Kiali features:
     * 1. Service Graph:
     *    - Visual topology
     *    - Traffic flow
     *    - Health status
     *    - Error rates
     *    
     * 2. Traffic Metrics:
     *    - Request volume
     *    - Success rate
     *    - Response times
     *    - TCP traffic
     *    
     * 3. Distributed Tracing:
     *    - Integrated Jaeger
     *    - Trace details
     *    
     * 4. Configuration Validation:
     *    - VirtualService errors
     *    - DestinationRule issues
     *    - Authorization problems
     */
}

/**
 * Example 6: Custom Metrics
 */
class CustomMetricsConfiguration {
    
    /**
     * Define custom metrics:
     * 
     * apiVersion: telemetry.istio.io/v1alpha1
     * kind: Telemetry
     * metadata:
     *   name: custom-metrics
     * spec:
     *   metrics:
     *   - providers:
     *     - name: prometheus
     *     dimensions:
     *       request_host: request.host
     *       request_path: request.path
     *       response_code: response.code
     *       source_cluster: source.cluster
     *       destination_cluster: destination.cluster
     *       custom_tag: request.headers['x-custom-tag']
     * 
     * ---
     * Query custom metrics:
     * 
     * # Total requests by path
     * sum(istio_requests_total{request_path="/orders"}) by (response_code)
     * 
     * # 95th percentile latency by host
     * histogram_quantile(0.95, 
     *   sum(rate(istio_request_duration_milliseconds_bucket[5m])) 
     *   by (le, request_host))
     */
}

/**
 * Example 7: Golden Signals
 */
class GoldenSignalsConfiguration {
    
    /**
     * Monitor 4 golden signals:
     * 
     * 1. LATENCY (How long requests take):
     * 
     * # P50 latency
     * histogram_quantile(0.50,
     *   sum(rate(istio_request_duration_milliseconds_bucket[5m]))
     *   by (destination_service, le))
     * 
     * # P95 latency
     * histogram_quantile(0.95,
     *   sum(rate(istio_request_duration_milliseconds_bucket[5m]))
     *   by (destination_service, le))
     * 
     * # P99 latency
     * histogram_quantile(0.99,
     *   sum(rate(istio_request_duration_milliseconds_bucket[5m]))
     *   by (destination_service, le))
     * 
     * ---
     * 2. TRAFFIC (How much demand):
     * 
     * # Requests per second
     * sum(rate(istio_requests_total[5m]))
     *   by (destination_service)
     * 
     * ---
     * 3. ERRORS (Rate of failed requests):
     * 
     * # Error rate
     * sum(rate(istio_requests_total{response_code=~"5.."}[5m]))
     *   / sum(rate(istio_requests_total[5m]))
     *   by (destination_service)
     * 
     * # Success rate
     * sum(rate(istio_requests_total{response_code!~"5.."}[5m]))
     *   / sum(rate(istio_requests_total[5m]))
     *   by (destination_service)
     * 
     * ---
     * 4. SATURATION (Resource utilization):
     * 
     * # Connection pool utilization
     * istio_tcp_connections_opened_total
     *   / istio_tcp_max_connections
     */
}

/**
 * Example 8: Alerting Rules
 */
class AlertingRulesConfiguration {
    
    /**
     * Prometheus alerting rules:
     * 
     * apiVersion: v1
     * kind: ConfigMap
     * metadata:
     *   name: prometheus-rules
     * data:
     *   alerts.yml: |
     *     groups:
     *     - name: istio
     *       interval: 30s
     *       rules:
     *       
     *       # High error rate
     *       - alert: HighErrorRate
     *         expr: |
     *           sum(rate(istio_requests_total{response_code=~"5.."}[5m]))
     *           / sum(rate(istio_requests_total[5m])) > 0.05
     *         for: 5m
     *         labels:
     *           severity: warning
     *         annotations:
     *           summary: "High error rate detected"
     *           description: "Error rate is {{ $value }} for {{ $labels.destination_service }}"
     *       
     *       # High latency
     *       - alert: HighLatency
     *         expr: |
     *           histogram_quantile(0.99,
     *             sum(rate(istio_request_duration_milliseconds_bucket[5m]))
     *             by (destination_service, le)) > 1000
     *         for: 5m
     *         labels:
     *           severity: warning
     *         annotations:
     *           summary: "High latency detected"
     *           description: "P99 latency is {{ $value }}ms for {{ $labels.destination_service }}"
     *       
     *       # Low success rate
     *       - alert: LowSuccessRate
     *         expr: |
     *           sum(rate(istio_requests_total{response_code!~"5.."}[5m]))
     *           / sum(rate(istio_requests_total[5m])) < 0.95
     *         for: 5m
     *         labels:
     *           severity: critical
     *         annotations:
     *           summary: "Low success rate"
     *           description: "Success rate is {{ $value }} for {{ $labels.destination_service }}"
     */
}

/**
 * Example 9: Log Aggregation
 */
class LogAggregationConfiguration {
    
    /**
     * Fluentd configuration for Istio logs:
     * 
     * <source>
     *   @type tail
     *   path /var/log/containers/*_istio-proxy-*.log
     *   pos_file /var/log/fluentd-istio-proxy.pos
     *   tag istio.proxy
     *   <parse>
     *     @type json
     *     time_key time
     *     time_format %Y-%m-%dT%H:%M:%S.%NZ
     *   </parse>
     * </source>
     * 
     * <filter istio.proxy>
     *   @type parser
     *   key_name log
     *   <parse>
     *     @type json
     *   </parse>
     * </filter>
     * 
     * <match istio.proxy>
     *   @type elasticsearch
     *   host elasticsearch.logging.svc
     *   port 9200
     *   logstash_format true
     *   logstash_prefix istio
     * </match>
     * 
     * ---
     * Query in Elasticsearch:
     * 
     * {
     *   "query": {
     *     "bool": {
     *       "filter": [
     *         { "term": { "response_code": "500" }},
     *         { "range": { "@timestamp": { "gte": "now-1h" }}}
     *       ]
     *     }
     *   }
     * }
     */
}

/**
 * Example 10: SLI/SLO Monitoring
 */
class SLISLOMonitoringConfiguration {
    
    /**
     * Service Level Indicators (SLIs):
     * 
     * 1. Availability SLI:
     *    SLI = successful_requests / total_requests
     *    
     * 2. Latency SLI:
     *    SLI = requests_under_threshold / total_requests
     *    
     * 3. Throughput SLI:
     *    SLI = actual_throughput / expected_throughput
     * 
     * ---
     * Service Level Objectives (SLOs):
     * 
     * SLO: 99.9% availability (error budget: 0.1%)
     * SLO: 95% of requests < 200ms
     * SLO: Support 1000 RPS
     * 
     * ---
     * Error budget calculation:
     * 
     * # 30-day error budget (0.1%)
     * 30 days * 24 hours * 60 minutes * 0.001 = 43.2 minutes downtime allowed
     * 
     * # Remaining error budget
     * error_budget_remaining = 1 - (
     *   sum(rate(istio_requests_total{response_code=~"5.."}[30d]))
     *   / sum(rate(istio_requests_total[30d]))
     *   / 0.001
     * )
     * 
     * # Alert when error budget < 10%
     * - alert: ErrorBudgetExhausted
     *   expr: error_budget_remaining < 0.1
     *   annotations:
     *     summary: "Error budget running low"
     */
}

/**
 * Main Pattern Class
 */
public class ObservabilityPattern {
    
    public static void main(String[] args) {
        System.out.println("Observability Pattern");
        System.out.println("====================\n");
        
        System.out.println("Three Pillars of Observability:");
        System.out.println("1. METRICS - Prometheus + Grafana");
        System.out.println("2. LOGS - Envoy access logs + Fluentd");
        System.out.println("3. TRACES - Jaeger/Zipkin distributed tracing\n");
        
        System.out.println("Golden Signals:");
        System.out.println("- LATENCY: Request duration (P50, P95, P99)");
        System.out.println("- TRAFFIC: Requests per second");
        System.out.println("- ERRORS: Error rate (4xx, 5xx)");
        System.out.println("- SATURATION: Resource utilization\n");
        
        System.out.println("Service Mesh Observability Tools:");
        System.out.println("✓ Kiali - Service graph and topology");
        System.out.println("✓ Prometheus - Metrics collection");
        System.out.println("✓ Grafana - Metrics visualization");
        System.out.println("✓ Jaeger - Distributed tracing");
        System.out.println("✓ Envoy - Access logs\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Monitor all 4 golden signals");
        System.out.println("- Define SLIs and SLOs");
        System.out.println("- Set up alerting rules");
        System.out.println("- Aggregate logs centrally");
        System.out.println("- Sample traces appropriately (1-10%)");
    }
}
