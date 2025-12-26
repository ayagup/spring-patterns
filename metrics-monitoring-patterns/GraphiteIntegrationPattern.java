package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.*;
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
 * Graphite Integration Pattern - Graphite Metrics Export
 * 
 * Purpose:
 * - Export metrics to Graphite time-series database
 * - Push-based metric delivery
 * - Hierarchical metric naming
 * - Carbon protocol support
 * - Real-time metric streaming
 * - Integration with Grafana
 * 
 * Use Cases:
 * - Graphite monitoring stack
 * - Time-series data storage
 * - Real-time dashboards
 * - Historical trend analysis
 * - Capacity planning
 * - Performance monitoring
 * - Legacy system integration
 * - Custom aggregation functions
 * 
 * Graphite Architecture:
 * - Carbon: Metric ingestion service
 * - Whisper: Time-series database
 * - Graphite-Web: Visualization interface
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     export:
 *       graphite:
 *         enabled: true
 *         host: localhost
 *         port: 2004
 *         protocol: plaintext  # or pickled
 *         step: 1m
 *         tags-as-prefix:
 *           - application
 *           - environment
 *         graphite-tags-enabled: false
 * 
 * Metric Naming:
 * - Hierarchical: app.demo.http.requests.count
 * - With tags: app.demo.http.requests;method=GET;status=200
 * 
 * Carbon Line Protocol:
 * <metric path> <metric value> <metric timestamp>
 * servers.host1.cpu.usage 45.2 1625097600
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-registry-graphite</artifactId>
 * </dependency>
 * 
 * Graphite Functions:
 * - averageSeries(): Average of multiple metrics
 * - sumSeries(): Sum of multiple metrics
 * - derivative(): Rate of change
 * - movingAverage(): Smoothed values
 * - percentileOfSeries(): Calculate percentiles
 * 
 * Warnings:
 * - Push model increases network traffic
 * - High metric volume can overwhelm Carbon
 * - Whisper files can grow large
 * - Consider retention policies
 * - Monitor Carbon queue size
 * - Test with realistic load
 * 
 * Best Practices:
 * - Use consistent naming hierarchy
 * - Configure appropriate step duration
 * - Set up retention policies
 * - Monitor Carbon performance
 * - Use tags for dimensions (if supported)
 * - Create aggregation rules
 * - Set up storage schemas
 * - Configure carbon-relay for scaling
 * - Use pickle protocol for better performance
 * - Document metric naming conventions
 */
@SpringBootApplication
public class GraphiteIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(GraphiteIntegrationPattern.class, args);
    }

    // ============================================
    // Example 1: Graphite Configuration
    // ============================================
    
    @Configuration
    public static class GraphiteConfiguration {
        
        @Bean
        public MeterRegistryCustomizer<MeterRegistry> graphiteCustomizer() {
            return registry -> {
                // Add common tags that will be part of metric path
                registry.config()
                    .commonTags("application", "demo-app")
                    .commonTags("environment", "production")
                    .commonTags("datacenter", "dc1");
                
                System.out.println("Graphite registry customized");
            };
        }
    }

    // ============================================
    // Example 2: HTTP Metrics for Graphite
    // ============================================
    
    @Service
    public static class GraphiteHttpMetricsService {
        
        private final Counter requestCounter;
        private final Timer requestTimer;
        private final Map<String, Counter> endpointCounters = new HashMap<>();
        private final MeterRegistry registry;
        
        public GraphiteHttpMetricsService(MeterRegistry registry) {
            this.registry = registry;
            
            // Request counter
            this.requestCounter = Counter.builder("http.requests")
                .description("Total HTTP requests")
                .tags("service", "api")
                .register(registry);
            
            // Request duration timer
            this.requestTimer = Timer.builder("http.request.duration")
                .description("HTTP request duration")
                .tags("service", "api")
                .register(registry);
        }
        
        public String handleRequest(String method, String endpoint, int statusCode) {
            requestCounter.increment();
            
            // Per-endpoint counter
            String key = method + ":" + endpoint;
            endpointCounters.computeIfAbsent(key, k ->
                Counter.builder("http.requests.endpoint")
                    .description("Requests per endpoint")
                    .tag("method", method)
                    .tag("endpoint", endpoint)
                    .tag("status", String.valueOf(statusCode))
                    .register(registry)
            ).increment();
            
            return requestTimer.record(() -> {
                try {
                    Thread.sleep(50 + (int) (Math.random() * 100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Processed: " + method + " " + endpoint;
            });
        }
    }

    // ============================================
    // Example 3: System Metrics for Graphite
    // ============================================
    
    @Service
    public static class GraphiteSystemMetricsService {
        
        private final Gauge cpuUsage;
        private final Gauge memoryUsage;
        private final Counter diskReads;
        private final Counter diskWrites;
        
        public GraphiteSystemMetricsService(MeterRegistry registry) {
            // CPU usage gauge
            this.cpuUsage = Gauge.builder("system.cpu.usage", 
                    this, service -> 20 + Math.random() * 60)
                .description("CPU usage percentage")
                .baseUnit("percent")
                .tags("type", "system")
                .register(registry);
            
            // Memory usage gauge
            this.memoryUsage = Gauge.builder("system.memory.usage", 
                    this, service -> 4096 + Math.random() * 4096)
                .description("Memory usage in MB")
                .baseUnit("megabytes")
                .tags("type", "system")
                .register(registry);
            
            // Disk reads counter
            this.diskReads = Counter.builder("system.disk.reads")
                .description("Total disk read operations")
                .tags("type", "io")
                .register(registry);
            
            // Disk writes counter
            this.diskWrites = Counter.builder("system.disk.writes")
                .description("Total disk write operations")
                .tags("type", "io")
                .register(registry);
        }
        
        public void recordDiskRead(int bytes) {
            diskReads.increment();
        }
        
        public void recordDiskWrite(int bytes) {
            diskWrites.increment();
        }
    }

    // ============================================
    // Example 4: Application Metrics for Graphite
    // ============================================
    
    @Service
    public static class GraphiteApplicationMetricsService {
        
        private final Counter transactionCounter;
        private final Timer transactionTimer;
        private final Gauge activeSessionsGauge;
        private int activeSessions = 0;
        
        public GraphiteApplicationMetricsService(MeterRegistry registry) {
            // Transaction counter
            this.transactionCounter = Counter.builder("app.transactions")
                .description("Total transactions")
                .tags("application", "demo")
                .register(registry);
            
            // Transaction duration timer
            this.transactionTimer = Timer.builder("app.transaction.duration")
                .description("Transaction processing duration")
                .tags("application", "demo")
                .register(registry);
            
            // Active sessions gauge
            this.activeSessionsGauge = Gauge.builder("app.sessions.active", 
                    this, service -> service.activeSessions)
                .description("Number of active sessions")
                .tags("application", "demo")
                .register(registry);
        }
        
        public String processTransaction(String transactionId, String type) {
            transactionCounter.increment();
            
            return transactionTimer.record(() -> {
                try {
                    Thread.sleep(100 + (int) (Math.random() * 200));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Transaction processed: " + transactionId;
            });
        }
        
        public void sessionCreated() {
            activeSessions++;
        }
        
        public void sessionDestroyed() {
            if (activeSessions > 0) {
                activeSessions--;
            }
        }
    }

    // ============================================
    // Example 5: Business Metrics for Graphite
    // ============================================
    
    @Service
    public static class GraphiteBusinessMetricsService {
        
        private final Counter salesCounter;
        private final Counter revenueCounter;
        private final Gauge inventoryGauge;
        private final Timer orderProcessingTimer;
        private int inventoryCount = 1000;
        
        public GraphiteBusinessMetricsService(MeterRegistry registry) {
            // Sales counter
            this.salesCounter = Counter.builder("business.sales")
                .description("Total number of sales")
                .tags("department", "sales")
                .register(registry);
            
            // Revenue counter (in cents)
            this.revenueCounter = Counter.builder("business.revenue.cents")
                .description("Total revenue in cents")
                .tags("department", "sales")
                .register(registry);
            
            // Inventory gauge
            this.inventoryGauge = Gauge.builder("business.inventory", 
                    this, service -> service.inventoryCount)
                .description("Current inventory count")
                .tags("department", "warehouse")
                .register(registry);
            
            // Order processing timer
            this.orderProcessingTimer = Timer.builder("business.order.processing.duration")
                .description("Order processing duration")
                .tags("department", "sales")
                .register(registry);
        }
        
        public String processSale(String productId, int amountCents, int quantity) {
            salesCounter.increment();
            revenueCounter.increment(amountCents);
            inventoryCount -= quantity;
            
            return orderProcessingTimer.record(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Sale processed: " + productId;
            });
        }
        
        public void restockInventory(int quantity) {
            inventoryCount += quantity;
        }
    }

    // ============================================
    // Example 6: Graphite Metric Path Builder
    // ============================================
    
    @Service
    public static class GraphiteMetricPathService {
        
        /**
         * Build hierarchical metric path for Graphite.
         * Example: servers.host1.application.demo.http.requests
         */
        public String buildMetricPath(String... components) {
            return String.join(".", components);
        }
        
        /**
         * Build metric path with tags (Graphite 1.1+).
         * Example: servers.host1.requests;method=GET;status=200
         */
        public String buildMetricPathWithTags(String basePath, Map<String, String> tags) {
            StringBuilder sb = new StringBuilder(basePath);
            
            if (tags != null && !tags.isEmpty()) {
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    sb.append(";").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            
            return sb.toString();
        }
        
        /**
         * Get example metric paths.
         */
        public List<Map<String, String>> getMetricPathExamples() {
            List<Map<String, String>> examples = new ArrayList<>();
            
            examples.add(Map.of(
                "path", "servers.host1.cpu.usage",
                "description", "CPU usage on host1"
            ));
            
            examples.add(Map.of(
                "path", "application.demo.http.requests.count",
                "description", "HTTP request count"
            ));
            
            examples.add(Map.of(
                "path", "application.demo.http.requests;method=GET;status=200",
                "description", "HTTP GET requests with 200 status (with tags)"
            ));
            
            examples.add(Map.of(
                "path", "business.sales.revenue.daily",
                "description", "Daily revenue"
            ));
            
            examples.add(Map.of(
                "path", "database.connections.active",
                "description", "Active database connections"
            ));
            
            return examples;
        }
    }

    // ============================================
    // Example 7: Graphite Query Examples
    // ============================================
    
    @Service
    public static class GraphiteQueryExamplesService {
        
        public List<Map<String, String>> getQueryExamples() {
            List<Map<String, String>> examples = new ArrayList<>();
            
            // Average series
            examples.add(Map.of(
                "name", "Average HTTP Requests",
                "query", "averageSeries(app.*.http.requests)",
                "description", "Average HTTP requests across all applications"
            ));
            
            // Sum series
            examples.add(Map.of(
                "name", "Total Revenue",
                "query", "sumSeries(business.*.revenue.cents)",
                "description", "Total revenue across all business units"
            ));
            
            // Derivative (rate of change)
            examples.add(Map.of(
                "name", "Request Rate",
                "query", "derivative(app.demo.http.requests)",
                "description", "Rate of requests per second"
            ));
            
            // Moving average
            examples.add(Map.of(
                "name", "CPU Usage (5-minute average)",
                "query", "movingAverage(system.cpu.usage, 5)",
                "description", "5-minute moving average of CPU usage"
            ));
            
            // Percentile
            examples.add(Map.of(
                "name", "95th Percentile Latency",
                "query", "percentileOfSeries(app.*.request.duration, 95)",
                "description", "95th percentile of request duration"
            ));
            
            // Scale
            examples.add(Map.of(
                "name", "Revenue in Dollars",
                "query", "scale(business.revenue.cents, 0.01)",
                "description", "Convert cents to dollars"
            ));
            
            // Highest current
            examples.add(Map.of(
                "name", "Top 5 Endpoints",
                "query", "highestCurrent(app.http.requests.endpoint.*, 5)",
                "description", "Top 5 endpoints by current request count"
            ));
            
            return examples;
        }
    }

    // ============================================
    // Example 8: Graphite Storage Schema Info
    // ============================================
    
    @Service
    public static class GraphiteStorageSchemaService {
        
        public List<Map<String, String>> getStorageSchemaExamples() {
            List<Map<String, String>> schemas = new ArrayList<>();
            
            // High-resolution for recent data
            schemas.add(Map.of(
                "name", "High Resolution",
                "pattern", "^app\\..*",
                "retentions", "10s:6h,1m:7d,10m:1y",
                "description", "10-second resolution for 6 hours, 1-minute for 7 days, 10-minute for 1 year"
            ));
            
            // Medium resolution
            schemas.add(Map.of(
                "name", "Medium Resolution",
                "pattern", "^business\\..*",
                "retentions", "1m:7d,10m:30d,1h:1y",
                "description", "1-minute for 7 days, 10-minute for 30 days, 1-hour for 1 year"
            ));
            
            // Low resolution for long-term
            schemas.add(Map.of(
                "name", "Low Resolution",
                "pattern", "^system\\..*",
                "retentions", "5m:7d,1h:30d,1d:5y",
                "description", "5-minute for 7 days, 1-hour for 30 days, 1-day for 5 years"
            ));
            
            return schemas;
        }
    }

    // ============================================
    // Example 9: Graphite Integration REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/graphite")
    public static class GraphiteController {
        
        private final GraphiteHttpMetricsService httpMetrics;
        private final GraphiteSystemMetricsService systemMetrics;
        private final GraphiteApplicationMetricsService appMetrics;
        private final GraphiteBusinessMetricsService businessMetrics;
        private final GraphiteMetricPathService pathService;
        private final GraphiteQueryExamplesService queryExamples;
        private final GraphiteStorageSchemaService storageSchema;
        
        public GraphiteController(
                GraphiteHttpMetricsService httpMetrics,
                GraphiteSystemMetricsService systemMetrics,
                GraphiteApplicationMetricsService appMetrics,
                GraphiteBusinessMetricsService businessMetrics,
                GraphiteMetricPathService pathService,
                GraphiteQueryExamplesService queryExamples,
                GraphiteStorageSchemaService storageSchema) {
            this.httpMetrics = httpMetrics;
            this.systemMetrics = systemMetrics;
            this.appMetrics = appMetrics;
            this.businessMetrics = businessMetrics;
            this.pathService = pathService;
            this.queryExamples = queryExamples;
            this.storageSchema = storageSchema;
        }
        
        @GetMapping("/request")
        public String simulateRequest(
                @RequestParam(defaultValue = "GET") String method,
                @RequestParam(defaultValue = "/api/data") String endpoint,
                @RequestParam(defaultValue = "200") int status) {
            return httpMetrics.handleRequest(method, endpoint, status);
        }
        
        @PostMapping("/system/disk/read")
        public Map<String, String> recordDiskRead(@RequestParam int bytes) {
            systemMetrics.recordDiskRead(bytes);
            return Collections.singletonMap("status", "disk read recorded");
        }
        
        @PostMapping("/system/disk/write")
        public Map<String, String> recordDiskWrite(@RequestParam int bytes) {
            systemMetrics.recordDiskWrite(bytes);
            return Collections.singletonMap("status", "disk write recorded");
        }
        
        @PostMapping("/transaction")
        public String processTransaction(
                @RequestParam String transactionId,
                @RequestParam(defaultValue = "purchase") String type) {
            return appMetrics.processTransaction(transactionId, type);
        }
        
        @PostMapping("/session/create")
        public Map<String, String> createSession() {
            appMetrics.sessionCreated();
            return Collections.singletonMap("status", "session created");
        }
        
        @PostMapping("/session/destroy")
        public Map<String, String> destroySession() {
            appMetrics.sessionDestroyed();
            return Collections.singletonMap("status", "session destroyed");
        }
        
        @PostMapping("/sale")
        public String processSale(
                @RequestParam String productId,
                @RequestParam int amountCents,
                @RequestParam(defaultValue = "1") int quantity) {
            return businessMetrics.processSale(productId, amountCents, quantity);
        }
        
        @PostMapping("/inventory/restock")
        public Map<String, String> restockInventory(@RequestParam int quantity) {
            businessMetrics.restockInventory(quantity);
            return Collections.singletonMap("status", "inventory restocked");
        }
        
        @GetMapping("/metric-path")
        public Map<String, String> buildMetricPath(@RequestParam String[] components) {
            String path = pathService.buildMetricPath(components);
            return Collections.singletonMap("metric_path", path);
        }
        
        @GetMapping("/metric-path/examples")
        public List<Map<String, String>> getMetricPathExamples() {
            return pathService.getMetricPathExamples();
        }
        
        @GetMapping("/query-examples")
        public List<Map<String, String>> getQueryExamples() {
            return queryExamples.getQueryExamples();
        }
        
        @GetMapping("/storage-schema")
        public List<Map<String, String>> getStorageSchemaExamples() {
            return storageSchema.getStorageSchemaExamples();
        }
    }
}
