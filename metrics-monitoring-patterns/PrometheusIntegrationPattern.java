package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;

/**
 * Prometheus Integration Pattern - Prometheus Metrics Export
 * 
 * Purpose:
 * - Export metrics in Prometheus format
 * - Pull-based scraping model
 * - Dimensional metrics with labels
 * - Histogram and summary support
 * - Service discovery integration
 * - Grafana dashboard integration
 * 
 * Use Cases:
 * - Prometheus monitoring stack
 * - Kubernetes metrics collection
 * - Service mesh observability
 * - Cloud-native monitoring
 * - Multi-dimensional metrics
 * - Long-term metric storage
 * - Alert rule configuration
 * - Grafana visualization
 * 
 * Prometheus Metric Types:
 * - Counter: Monotonically increasing (requests, errors)
 * - Gauge: Current value (memory, connections)
 * - Histogram: Distribution with buckets (latency)
 * - Summary: Client-side percentiles
 * 
 * Configuration (application.yml):
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: health,info,metrics,prometheus
 *   metrics:
 *     tags:
 *       application: ${spring.application.name}
 *       instance: ${HOSTNAME:localhost}
 *     distribution:
 *       percentiles-histogram:
 *         http.server.requests: true
 *       slo:
 *         http.server.requests: 10ms,50ms,100ms,500ms,1s
 *     export:
 *       prometheus:
 *         enabled: true
 *         step: 1m
 *         descriptions: true
 * 
 * Prometheus Scrape Config:
 * scrape_configs:
 *   - job_name: 'spring-boot'
 *     metrics_path: '/actuator/prometheus'
 *     scrape_interval: 15s
 *     static_configs:
 *       - targets: ['localhost:8080']
 *         labels:
 *           application: 'demo-app'
 *           environment: 'production'
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-registry-prometheus</artifactId>
 * </dependency>
 * 
 * Endpoints:
 * - GET /actuator/prometheus - Metrics in Prometheus format
 * - GET /actuator/metrics - JSON format metrics list
 * - GET /actuator/metrics/{name} - Specific metric details
 * 
 * Prometheus Query Examples:
 * - rate(http_server_requests_seconds_count[5m])
 * - histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
 * - sum(jvm_memory_used_bytes) by (area)
 * 
 * Warnings:
 * - High cardinality labels cause performance issues
 * - Too many metrics increase scrape time
 * - Histogram buckets consume memory
 * - Label names must match Prometheus conventions
 * - Avoid dynamic label values
 * - Monitor scrape duration
 * 
 * Best Practices:
 * - Use consistent label names
 * - Keep label cardinality low (<10 values)
 * - Configure appropriate histogram buckets
 * - Set scrape interval based on data resolution needs
 * - Use service discovery in production
 * - Create Grafana dashboards
 * - Set up alerting rules
 * - Document custom metrics
 * - Use job labels for grouping
 * - Enable descriptions for clarity
 */
@SpringBootApplication
public class PrometheusIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(PrometheusIntegrationPattern.class, args);
    }

    // ============================================
    // Example 1: Prometheus Registry Configuration
    // ============================================
    
    @Configuration
    public static class PrometheusConfiguration {
        
        @Bean
        public MeterRegistryCustomizer<PrometheusMeterRegistry> prometheusCustomizer() {
            return registry -> {
                // Add common labels (tags)
                registry.config()
                    .commonTags("application", "demo-app")
                    .commonTags("environment", "production")
                    .commonTags("region", "us-east-1");
                
                System.out.println("Prometheus registry customized");
            };
        }
    }

    // ============================================
    // Example 2: HTTP Request Metrics for Prometheus
    // ============================================
    
    @Service
    public static class PrometheusHttpMetricsService {
        
        private final Counter requestCounter;
        private final Timer requestTimer;
        private final Map<String, Counter> statusCounters = new HashMap<>();
        private final MeterRegistry registry;
        
        public PrometheusHttpMetricsService(MeterRegistry registry) {
            this.registry = registry;
            
            // Request counter with labels
            this.requestCounter = Counter.builder("http_requests_total")
                .description("Total HTTP requests")
                .tags("service", "api")
                .register(registry);
            
            // Request duration histogram
            this.requestTimer = Timer.builder("http_request_duration_seconds")
                .description("HTTP request duration")
                .tags("service", "api")
                .serviceLevelObjectives(
                    Duration.ofMillis(10),
                    Duration.ofMillis(50),
                    Duration.ofMillis(100),
                    Duration.ofMillis(200),
                    Duration.ofMillis(500),
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(2),
                    Duration.ofSeconds(5)
                )
                .publishPercentileHistogram()
                .register(registry);
        }
        
        public String handleRequest(String method, String endpoint, int statusCode) {
            requestCounter.increment();
            
            return requestTimer.record(() -> {
                // Record status code counter
                String statusKey = String.valueOf(statusCode);
                statusCounters.computeIfAbsent(statusKey, s ->
                    Counter.builder("http_requests_total")
                        .description("HTTP requests by status")
                        .tag("method", method)
                        .tag("status", s)
                        .register(registry)
                ).increment();
                
                // Simulate request processing
                try {
                    Thread.sleep(50 + (int) (Math.random() * 150));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                return "Request processed: " + method + " " + endpoint;
            });
        }
    }

    // ============================================
    // Example 3: Database Metrics for Prometheus
    // ============================================
    
    @Service
    public static class PrometheusDatabaseMetricsService {
        
        private final Counter queryCounter;
        private final Timer queryTimer;
        private final Gauge activeConnections;
        
        public PrometheusDatabaseMetricsService(MeterRegistry registry) {
            // Query counter with operation label
            this.queryCounter = Counter.builder("db_queries_total")
                .description("Total database queries")
                .tags("database", "postgres")
                .register(registry);
            
            // Query duration histogram
            this.queryTimer = Timer.builder("db_query_duration_seconds")
                .description("Database query duration")
                .tags("database", "postgres")
                .serviceLevelObjectives(
                    Duration.ofMillis(5),
                    Duration.ofMillis(10),
                    Duration.ofMillis(25),
                    Duration.ofMillis(50),
                    Duration.ofMillis(100),
                    Duration.ofMillis(500)
                )
                .publishPercentileHistogram()
                .register(registry);
            
            // Active connections gauge
            this.activeConnections = Gauge.builder("db_connections_active", 
                    this, service -> 5 + Math.random() * 5)
                .description("Active database connections")
                .tags("database", "postgres")
                .register(registry);
        }
        
        public List<Map<String, Object>> executeQuery(String operation, String table) {
            queryCounter.increment();
            
            return queryTimer.record(() -> {
                // Simulate query execution
                try {
                    Thread.sleep(10 + (int) (Math.random() * 40));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                return Arrays.asList(
                    Map.of("id", 1, "data", "row1"),
                    Map.of("id", 2, "data", "row2")
                );
            });
        }
    }

    // ============================================
    // Example 4: Application Metrics for Prometheus
    // ============================================
    
    @Service
    public static class PrometheusApplicationMetricsService {
        
        private final Counter userLoginCounter;
        private final Counter errorCounter;
        private final Gauge activeUsersGauge;
        private int activeUsers = 0;
        
        public PrometheusApplicationMetricsService(MeterRegistry registry) {
            // User login counter
            this.userLoginCounter = Counter.builder("app_user_logins_total")
                .description("Total user logins")
                .tags("application", "demo")
                .register(registry);
            
            // Error counter with type label
            this.errorCounter = Counter.builder("app_errors_total")
                .description("Total application errors")
                .tags("application", "demo")
                .register(registry);
            
            // Active users gauge
            this.activeUsersGauge = Gauge.builder("app_users_active", 
                    this, service -> service.activeUsers)
                .description("Number of active users")
                .tags("application", "demo")
                .register(registry);
        }
        
        public void recordLogin(String username) {
            userLoginCounter.increment();
            activeUsers++;
            System.out.println("User logged in: " + username);
        }
        
        public void recordLogout(String username) {
            if (activeUsers > 0) {
                activeUsers--;
            }
            System.out.println("User logged out: " + username);
        }
        
        public void recordError(String errorType) {
            Counter.builder("app_errors_total")
                .description("Application errors by type")
                .tag("error_type", errorType)
                .register(errorCounter.getId().getConventionName() != null ? 
                    null : (MeterRegistry) null)
                .increment();
        }
    }

    // ============================================
    // Example 5: Business Metrics for Prometheus
    // ============================================
    
    @Service
    public static class PrometheusBusinessMetricsService {
        
        private final Counter orderCounter;
        private final Counter revenueCounter;
        private final Gauge inventoryGauge;
        private int inventoryCount = 1000;
        
        public PrometheusBusinessMetricsService(MeterRegistry registry) {
            // Order counter
            this.orderCounter = Counter.builder("business_orders_total")
                .description("Total orders")
                .tags("business", "sales")
                .register(registry);
            
            // Revenue counter (in cents)
            this.revenueCounter = Counter.builder("business_revenue_cents_total")
                .description("Total revenue in cents")
                .tags("business", "sales")
                .register(registry);
            
            // Inventory gauge
            this.inventoryGauge = Gauge.builder("business_inventory_items", 
                    this, service -> service.inventoryCount)
                .description("Current inventory count")
                .tags("business", "inventory")
                .register(registry);
        }
        
        public void recordOrder(String productId, int amountCents, int quantity) {
            orderCounter.increment();
            revenueCounter.increment(amountCents);
            inventoryCount -= quantity;
            
            System.out.println("Order recorded: " + productId + 
                " - $" + (amountCents / 100.0) + " (qty: " + quantity + ")");
        }
        
        public void restockInventory(int quantity) {
            inventoryCount += quantity;
            System.out.println("Inventory restocked: +" + quantity);
        }
    }

    // ============================================
    // Example 6: Custom Histogram Configuration
    // ============================================
    
    @Service
    public static class PrometheusHistogramService {
        
        private final Timer customTimer;
        
        public PrometheusHistogramService(MeterRegistry registry) {
            // Custom histogram with specific buckets for SLA monitoring
            this.customTimer = Timer.builder("api_sla_duration_seconds")
                .description("API duration for SLA monitoring")
                .tags("sla", "200ms")
                .serviceLevelObjectives(
                    Duration.ofMillis(50),    // 25% of SLA
                    Duration.ofMillis(100),   // 50% of SLA
                    Duration.ofMillis(150),   // 75% of SLA
                    Duration.ofMillis(200),   // 100% of SLA (target)
                    Duration.ofMillis(300),   // 150% of SLA
                    Duration.ofMillis(500)    // 250% of SLA
                )
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(5))
                .register(registry);
        }
        
        public String processRequest(String requestId, int expectedLatency) {
            return customTimer.record(() -> {
                try {
                    Thread.sleep(expectedLatency);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Request completed: " + requestId;
            });
        }
    }

    // ============================================
    // Example 7: Prometheus Metrics Scrape Service
    // ============================================
    
    @Service
    public static class PrometheusMetricsService {
        
        private final PrometheusMeterRegistry prometheusRegistry;
        
        public PrometheusMetricsService(MeterRegistry registry) {
            if (registry instanceof PrometheusMeterRegistry) {
                this.prometheusRegistry = (PrometheusMeterRegistry) registry;
            } else {
                this.prometheusRegistry = null;
            }
        }
        
        public String getPrometheusMetrics() {
            if (prometheusRegistry != null) {
                return prometheusRegistry.scrape();
            }
            return "# Prometheus registry not available";
        }
        
        public Map<String, Object> getMetricsInfo() {
            Map<String, Object> info = new HashMap<>();
            
            if (prometheusRegistry != null) {
                info.put("registry_type", "Prometheus");
                info.put("total_meters", prometheusRegistry.getMeters().size());
                info.put("scrape_format", "text/plain; version=0.0.4");
                info.put("endpoint", "/actuator/prometheus");
            } else {
                info.put("error", "Prometheus registry not configured");
            }
            
            return info;
        }
    }

    // ============================================
    // Example 8: Prometheus Query Examples Service
    // ============================================
    
    @Service
    public static class PrometheusQueryExamplesService {
        
        public List<Map<String, String>> getQueryExamples() {
            List<Map<String, String>> examples = new ArrayList<>();
            
            // Request rate
            examples.add(Map.of(
                "name", "Request Rate (per second)",
                "query", "rate(http_requests_total[5m])",
                "description", "HTTP requests per second over last 5 minutes"
            ));
            
            // P95 latency
            examples.add(Map.of(
                "name", "P95 Latency",
                "query", "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
                "description", "95th percentile request latency"
            ));
            
            // Error rate
            examples.add(Map.of(
                "name", "Error Rate",
                "query", "rate(http_requests_total{status=~\"5..\"}[5m])",
                "description", "5xx error rate per second"
            ));
            
            // Success rate percentage
            examples.add(Map.of(
                "name", "Success Rate",
                "query", "sum(rate(http_requests_total{status=~\"2..\"}[5m])) / sum(rate(http_requests_total[5m])) * 100",
                "description", "Percentage of successful requests"
            ));
            
            // JVM memory usage
            examples.add(Map.of(
                "name", "JVM Memory Usage",
                "query", "sum(jvm_memory_used_bytes) by (area)",
                "description", "JVM memory usage by area (heap/non-heap)"
            ));
            
            // Database query rate
            examples.add(Map.of(
                "name", "Database Query Rate",
                "query", "rate(db_queries_total[5m])",
                "description", "Database queries per second"
            ));
            
            // Active users
            examples.add(Map.of(
                "name", "Active Users",
                "query", "app_users_active",
                "description", "Current number of active users"
            ));
            
            // Total revenue today
            examples.add(Map.of(
                "name", "Revenue Today",
                "query", "increase(business_revenue_cents_total[24h]) / 100",
                "description", "Total revenue in dollars over last 24 hours"
            ));
            
            return examples;
        }
    }

    // ============================================
    // Example 9: Prometheus Integration REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/prometheus")
    public static class PrometheusController {
        
        private final PrometheusHttpMetricsService httpMetrics;
        private final PrometheusDatabaseMetricsService dbMetrics;
        private final PrometheusApplicationMetricsService appMetrics;
        private final PrometheusBusinessMetricsService businessMetrics;
        private final PrometheusHistogramService histogramService;
        private final PrometheusMetricsService metricsService;
        private final PrometheusQueryExamplesService queryExamples;
        
        public PrometheusController(
                PrometheusHttpMetricsService httpMetrics,
                PrometheusDatabaseMetricsService dbMetrics,
                PrometheusApplicationMetricsService appMetrics,
                PrometheusBusinessMetricsService businessMetrics,
                PrometheusHistogramService histogramService,
                PrometheusMetricsService metricsService,
                PrometheusQueryExamplesService queryExamples) {
            this.httpMetrics = httpMetrics;
            this.dbMetrics = dbMetrics;
            this.appMetrics = appMetrics;
            this.businessMetrics = businessMetrics;
            this.histogramService = histogramService;
            this.metricsService = metricsService;
            this.queryExamples = queryExamples;
        }
        
        @GetMapping("/request")
        public String simulateRequest(
                @RequestParam(defaultValue = "GET") String method,
                @RequestParam(defaultValue = "/api/data") String endpoint,
                @RequestParam(defaultValue = "200") int status) {
            return httpMetrics.handleRequest(method, endpoint, status);
        }
        
        @GetMapping("/db/query")
        public List<Map<String, Object>> simulateQuery(
                @RequestParam(defaultValue = "SELECT") String operation,
                @RequestParam(defaultValue = "users") String table) {
            return dbMetrics.executeQuery(operation, table);
        }
        
        @PostMapping("/login")
        public Map<String, String> login(@RequestParam String username) {
            appMetrics.recordLogin(username);
            return Collections.singletonMap("status", "logged in");
        }
        
        @PostMapping("/logout")
        public Map<String, String> logout(@RequestParam String username) {
            appMetrics.recordLogout(username);
            return Collections.singletonMap("status", "logged out");
        }
        
        @PostMapping("/order")
        public Map<String, String> createOrder(
                @RequestParam String productId,
                @RequestParam int amountCents,
                @RequestParam(defaultValue = "1") int quantity) {
            businessMetrics.recordOrder(productId, amountCents, quantity);
            return Collections.singletonMap("status", "order created");
        }
        
        @PostMapping("/inventory/restock")
        public Map<String, String> restockInventory(@RequestParam int quantity) {
            businessMetrics.restockInventory(quantity);
            return Collections.singletonMap("status", "inventory restocked");
        }
        
        @GetMapping("/sla/process")
        public String processSlaRequest(
                @RequestParam String requestId,
                @RequestParam(defaultValue = "100") int latencyMs) {
            return histogramService.processRequest(requestId, latencyMs);
        }
        
        @GetMapping("/scrape")
        public String scrapeMetrics() {
            return metricsService.getPrometheusMetrics();
        }
        
        @GetMapping("/info")
        public Map<String, Object> getMetricsInfo() {
            return metricsService.getMetricsInfo();
        }
        
        @GetMapping("/query-examples")
        public List<Map<String, String>> getQueryExamples() {
            return queryExamples.getQueryExamples();
        }
    }
}
