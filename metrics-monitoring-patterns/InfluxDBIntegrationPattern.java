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
import java.time.Instant;
import java.util.*;

/**
 * InfluxDB Integration Pattern - InfluxDB Time-Series Metrics
 * 
 * Purpose:
 * - Export metrics to InfluxDB time-series database
 * - High-performance metric storage
 * - Line protocol format
 * - Retention policies
 * - Continuous queries
 * - Downsampling
 * 
 * Use Cases:
 * - Time-series data storage
 * - IoT sensor data
 * - Application performance monitoring
 * - Infrastructure monitoring
 * - Business metrics tracking
 * - Real-time analytics
 * - Historical trend analysis
 * - Capacity planning
 * 
 * InfluxDB Concepts:
 * - Measurement: Similar to table (e.g., http_requests)
 * - Tags: Indexed metadata (e.g., method=GET, status=200)
 * - Fields: Actual metric values (e.g., count=123, duration=45.2)
 * - Timestamp: Nanosecond precision
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     export:
 *       influx:
 *         enabled: true
 *         uri: http://localhost:8086
 *         db: mydb
 *         user: admin
 *         password: admin123
 *         step: 1m
 *         batch-size: 10000
 *         compressed: true
 *         connect-timeout: 1s
 *         read-timeout: 10s
 *         num-threads: 2
 *         consistency: one
 *         retention-policy: autogen
 * 
 * Line Protocol Format:
 * <measurement>[,<tag_key>=<tag_value>...] <field_key>=<field_value>[,<field_key>=<field_value>...] [<timestamp>]
 * 
 * Examples:
 * http_requests,method=GET,status=200 count=123,duration=45.2 1625097600000000000
 * cpu_usage,host=server1,region=us-east value=78.5 1625097600000000000
 * 
 * Retention Policies:
 * - autogen: Default, infinite retention
 * - Custom: Define duration and replication factor
 *   CREATE RETENTION POLICY "one_week" ON "mydb" DURATION 7d REPLICATION 1
 * 
 * Continuous Queries:
 * - Automated downsampling
 * - Pre-compute aggregations
 * - Example: Calculate hourly averages from 1-minute data
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-registry-influx</artifactId>
 * </dependency>
 * 
 * InfluxQL Queries:
 * - SELECT mean("duration") FROM "http_requests" WHERE time > now() - 1h GROUP BY time(5m)
 * - SELECT count("count") FROM "api_calls" WHERE "status" = '200' GROUP BY "endpoint"
 * - SELECT percentile("latency", 95) FROM "requests" WHERE time > now() - 1d
 * 
 * Flux Language (InfluxDB 2.x):
 * from(bucket: "mydb")
 *   |> range(start: -1h)
 *   |> filter(fn: (r) => r._measurement == "http_requests")
 *   |> mean()
 * 
 * Warnings:
 * - High cardinality tags can impact performance
 * - Avoid using unique values as tags (e.g., user IDs)
 * - Configure appropriate retention policies
 * - Monitor memory usage
 * - Use batch writes for efficiency
 * - Test with realistic data volumes
 * - Consider downsampling for long-term storage
 * 
 * Best Practices:
 * - Use tags for dimensions (indexed)
 * - Use fields for measurements (not indexed)
 * - Keep tag cardinality low (<100k unique combinations)
 * - Create appropriate retention policies
 * - Set up continuous queries for downsampling
 * - Use batch writes (10k-50k points)
 * - Enable compression
 * - Monitor shard group size
 * - Use appropriate precision (usually seconds or milliseconds)
 * - Create indexes on frequently queried tags
 */
@SpringBootApplication
public class InfluxDBIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(InfluxDBIntegrationPattern.class, args);
    }

    // ============================================
    // Example 1: InfluxDB Configuration
    // ============================================
    
    @Configuration
    public static class InfluxDBConfiguration {
        
        @Bean
        public MeterRegistryCustomizer<MeterRegistry> influxCustomizer() {
            return registry -> {
                // Add common tags for all metrics
                registry.config()
                    .commonTags("application", "demo-app")
                    .commonTags("environment", "production")
                    .commonTags("datacenter", "dc1")
                    .commonTags("region", "us-east");
                
                System.out.println("InfluxDB registry customized");
            };
        }
    }

    // ============================================
    // Example 2: HTTP Metrics for InfluxDB
    // ============================================
    
    @Service
    public static class InfluxHTTPMetricsService {
        
        private final Counter requestCounter;
        private final Timer requestTimer;
        private final Map<String, Counter> statusCounters = new HashMap<>();
        private final MeterRegistry registry;
        
        public InfluxHTTPMetricsService(MeterRegistry registry) {
            this.registry = registry;
            
            // Total request counter
            this.requestCounter = Counter.builder("http_requests_total")
                .description("Total HTTP requests")
                .tags("service", "api")
                .register(registry);
            
            // Request duration timer
            this.requestTimer = Timer.builder("http_request_duration_seconds")
                .description("HTTP request duration in seconds")
                .tags("service", "api")
                .register(registry);
        }
        
        public String handleRequest(String method, String endpoint, int statusCode) {
            // Increment total counter
            requestCounter.increment();
            
            // Increment status-specific counter
            String statusKey = String.valueOf(statusCode);
            statusCounters.computeIfAbsent(statusKey, k ->
                Counter.builder("http_requests_total")
                    .tag("method", method)
                    .tag("endpoint", endpoint)
                    .tag("status", statusKey)
                    .register(registry)
            ).increment();
            
            // Record request duration
            return requestTimer.record(() -> {
                try {
                    Thread.sleep(20 + (int) (Math.random() * 80));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Request processed: " + method + " " + endpoint;
            });
        }
    }

    // ============================================
    // Example 3: Database Metrics for InfluxDB
    // ============================================
    
    @Service
    public static class InfluxDatabaseMetricsService {
        
        private final Counter queryCounter;
        private final Timer queryTimer;
        private final Gauge connectionPoolSize;
        private int activeConnections = 10;
        
        public InfluxDatabaseMetricsService(MeterRegistry registry) {
            // Query counter
            this.queryCounter = Counter.builder("db_queries_total")
                .description("Total database queries")
                .tags("database", "postgres")
                .register(registry);
            
            // Query duration timer
            this.queryTimer = Timer.builder("db_query_duration_seconds")
                .description("Database query duration")
                .tags("database", "postgres")
                .register(registry);
            
            // Connection pool gauge
            this.connectionPoolSize = Gauge.builder("db_connections_active", 
                    this, service -> service.activeConnections)
                .description("Active database connections")
                .tags("database", "postgres")
                .register(registry);
        }
        
        public String executeQuery(String operation, String table) {
            queryCounter.increment();
            
            return queryTimer.record(() -> {
                try {
                    Thread.sleep(10 + (int) (Math.random() * 40));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Query executed: " + operation + " on " + table;
            });
        }
        
        public void connectionAcquired() {
            activeConnections++;
        }
        
        public void connectionReleased() {
            if (activeConnections > 0) {
                activeConnections--;
            }
        }
    }

    // ============================================
    // Example 4: Application Metrics for InfluxDB
    // ============================================
    
    @Service
    public static class InfluxApplicationMetricsService {
        
        private final Counter loginCounter;
        private final Counter errorCounter;
        private final Gauge activeUsersGauge;
        private int activeUsers = 0;
        
        public InfluxApplicationMetricsService(MeterRegistry registry) {
            // User login counter
            this.loginCounter = Counter.builder("app_user_logins_total")
                .description("Total user logins")
                .tags("app", "demo")
                .register(registry);
            
            // Error counter
            this.errorCounter = Counter.builder("app_errors_total")
                .description("Total application errors")
                .tags("app", "demo")
                .register(registry);
            
            // Active users gauge
            this.activeUsersGauge = Gauge.builder("app_users_active", 
                    this, service -> service.activeUsers)
                .description("Currently active users")
                .tags("app", "demo")
                .register(registry);
        }
        
        public void userLogin(String userId) {
            loginCounter.increment();
            activeUsers++;
        }
        
        public void userLogout(String userId) {
            if (activeUsers > 0) {
                activeUsers--;
            }
        }
        
        public void recordError(String errorType) {
            errorCounter.increment();
        }
    }

    // ============================================
    // Example 5: Business Metrics for InfluxDB
    // ============================================
    
    @Service
    public static class InfluxBusinessMetricsService {
        
        private final Counter ordersCounter;
        private final Counter revenueCounter;
        private final Gauge inventoryGauge;
        private final Timer orderProcessingTimer;
        private int inventoryCount = 5000;
        
        public InfluxBusinessMetricsService(MeterRegistry registry) {
            // Orders counter
            this.ordersCounter = Counter.builder("business_orders_total")
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
                .tags("business", "warehouse")
                .register(registry);
            
            // Order processing timer
            this.orderProcessingTimer = Timer.builder("business_order_processing_seconds")
                .description("Order processing duration")
                .tags("business", "sales")
                .register(registry);
        }
        
        public String processOrder(String orderId, int amountCents, int quantity) {
            ordersCounter.increment();
            revenueCounter.increment(amountCents);
            inventoryCount -= quantity;
            
            return orderProcessingTimer.record(() -> {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Order processed: " + orderId;
            });
        }
        
        public void restockInventory(int quantity) {
            inventoryCount += quantity;
        }
    }

    // ============================================
    // Example 6: Line Protocol Builder
    // ============================================
    
    @Service
    public static class InfluxLineProtocolService {
        
        /**
         * Build InfluxDB line protocol format.
         */
        public String buildLineProtocol(
                String measurement,
                Map<String, String> tags,
                Map<String, Object> fields,
                Long timestamp) {
            
            StringBuilder sb = new StringBuilder();
            
            // Measurement name
            sb.append(escapeKey(measurement));
            
            // Tags (optional)
            if (tags != null && !tags.isEmpty()) {
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    sb.append(",").append(escapeKey(entry.getKey()))
                      .append("=").append(escapeTagValue(entry.getValue()));
                }
            }
            
            // Space separator
            sb.append(" ");
            
            // Fields (required)
            boolean first = true;
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(escapeKey(entry.getKey())).append("=")
                  .append(formatFieldValue(entry.getValue()));
                first = false;
            }
            
            // Timestamp (optional, nanoseconds)
            if (timestamp != null) {
                sb.append(" ").append(timestamp);
            }
            
            return sb.toString();
        }
        
        private String escapeKey(String key) {
            return key.replace(" ", "\\ ")
                     .replace(",", "\\,")
                     .replace("=", "\\=");
        }
        
        private String escapeTagValue(String value) {
            return value.replace(" ", "\\ ")
                       .replace(",", "\\,")
                       .replace("=", "\\=");
        }
        
        private String formatFieldValue(Object value) {
            if (value instanceof String) {
                return "\"" + ((String) value).replace("\"", "\\\"") + "\"";
            } else if (value instanceof Integer || value instanceof Long) {
                return value + "i";
            } else if (value instanceof Boolean) {
                return value.toString();
            } else {
                return value.toString();
            }
        }
        
        public List<String> getLineProtocolExamples() {
            List<String> examples = new ArrayList<>();
            
            // Example 1: Simple measurement with one field
            examples.add(buildLineProtocol(
                "cpu_usage",
                Map.of("host", "server1", "region", "us-east"),
                Map.of("value", 78.5),
                1625097600000000000L
            ));
            
            // Example 2: HTTP request with multiple fields
            examples.add(buildLineProtocol(
                "http_requests",
                Map.of("method", "GET", "status", "200", "endpoint", "/api/users"),
                Map.of("count", 123, "duration", 45.2),
                System.nanoTime()
            ));
            
            // Example 3: Business metric
            examples.add(buildLineProtocol(
                "orders",
                Map.of("product", "widget", "region", "us-west"),
                Map.of("quantity", 10, "revenue", 99.99),
                null
            ));
            
            return examples;
        }
    }

    // ============================================
    // Example 7: Retention Policy Examples
    // ============================================
    
    @Service
    public static class InfluxRetentionPolicyService {
        
        public List<Map<String, Object>> getRetentionPolicyExamples() {
            List<Map<String, Object>> policies = new ArrayList<>();
            
            // Short-term high resolution
            policies.add(Map.of(
                "name", "realtime",
                "duration", "7d",
                "replication", 1,
                "shardDuration", "1h",
                "description", "High-resolution data for 7 days",
                "query", "CREATE RETENTION POLICY \"realtime\" ON \"mydb\" DURATION 7d REPLICATION 1 SHARD DURATION 1h"
            ));
            
            // Medium-term downsampled
            policies.add(Map.of(
                "name", "weekly",
                "duration", "4w",
                "replication", 1,
                "shardDuration", "1d",
                "description", "Downsampled data for 4 weeks",
                "query", "CREATE RETENTION POLICY \"weekly\" ON \"mydb\" DURATION 4w REPLICATION 1 SHARD DURATION 1d"
            ));
            
            // Long-term aggregated
            policies.add(Map.of(
                "name", "yearly",
                "duration", "52w",
                "replication", 1,
                "shardDuration", "7d",
                "description", "Aggregated data for 1 year",
                "query", "CREATE RETENTION POLICY \"yearly\" ON \"mydb\" DURATION 52w REPLICATION 1 SHARD DURATION 7d"
            ));
            
            // Infinite retention
            policies.add(Map.of(
                "name", "infinite",
                "duration", "INF",
                "replication", 1,
                "shardDuration", "7d",
                "description", "Permanent storage",
                "query", "CREATE RETENTION POLICY \"infinite\" ON \"mydb\" DURATION INF REPLICATION 1 SHARD DURATION 7d"
            ));
            
            return policies;
        }
    }

    // ============================================
    // Example 8: Continuous Query Examples
    // ============================================
    
    @Service
    public static class InfluxContinuousQueryService {
        
        public List<Map<String, String>> getContinuousQueryExamples() {
            List<Map<String, String>> queries = new ArrayList<>();
            
            // Hourly average
            queries.add(Map.of(
                "name", "cq_http_requests_hourly",
                "description", "Calculate hourly average of HTTP requests",
                "query", "CREATE CONTINUOUS QUERY \"cq_http_requests_hourly\" ON \"mydb\" " +
                        "BEGIN " +
                        "  SELECT mean(\"count\") AS \"count\" " +
                        "  INTO \"weekly\".\"http_requests_hourly\" " +
                        "  FROM \"realtime\".\"http_requests\" " +
                        "  GROUP BY time(1h), * " +
                        "END"
            ));
            
            // Daily sum
            queries.add(Map.of(
                "name", "cq_revenue_daily",
                "description", "Calculate daily sum of revenue",
                "query", "CREATE CONTINUOUS QUERY \"cq_revenue_daily\" ON \"mydb\" " +
                        "BEGIN " +
                        "  SELECT sum(\"revenue\") AS \"revenue\" " +
                        "  INTO \"yearly\".\"revenue_daily\" " +
                        "  FROM \"weekly\".\"orders\" " +
                        "  GROUP BY time(1d), * " +
                        "END"
            ));
            
            // 95th percentile
            queries.add(Map.of(
                "name", "cq_latency_p95",
                "description", "Calculate 95th percentile of latency every 5 minutes",
                "query", "CREATE CONTINUOUS QUERY \"cq_latency_p95\" ON \"mydb\" " +
                        "BEGIN " +
                        "  SELECT percentile(\"duration\", 95) AS \"p95\" " +
                        "  INTO \"weekly\".\"latency_p95\" " +
                        "  FROM \"realtime\".\"http_requests\" " +
                        "  GROUP BY time(5m), * " +
                        "END"
            ));
            
            return queries;
        }
    }

    // ============================================
    // Example 9: InfluxQL Query Examples
    // ============================================
    
    @Service
    public static class InfluxQLQueryExamplesService {
        
        public List<Map<String, String>> getInfluxQLExamples() {
            List<Map<String, String>> examples = new ArrayList<>();
            
            // Basic query
            examples.add(Map.of(
                "name", "Recent HTTP Requests",
                "query", "SELECT * FROM \"http_requests\" WHERE time > now() - 1h",
                "description", "Get all HTTP requests from last hour"
            ));
            
            // Aggregation with GROUP BY
            examples.add(Map.of(
                "name", "Requests per Minute",
                "query", "SELECT count(\"count\") FROM \"http_requests\" WHERE time > now() - 1h GROUP BY time(1m)",
                "description", "Count requests per minute"
            ));
            
            // Mean calculation
            examples.add(Map.of(
                "name", "Average Response Time",
                "query", "SELECT mean(\"duration\") FROM \"http_requests\" WHERE time > now() - 1h GROUP BY time(5m), \"endpoint\"",
                "description", "Average response time per endpoint"
            ));
            
            // Percentile
            examples.add(Map.of(
                "name", "95th Percentile Latency",
                "query", "SELECT percentile(\"duration\", 95) FROM \"http_requests\" WHERE time > now() - 1d",
                "description", "95th percentile of request duration"
            ));
            
            // Multiple aggregations
            examples.add(Map.of(
                "name", "Request Statistics",
                "query", "SELECT count(\"count\"), mean(\"duration\"), max(\"duration\"), min(\"duration\") FROM \"http_requests\" WHERE time > now() - 1h",
                "description", "Comprehensive request statistics"
            ));
            
            // WHERE clause with tags
            examples.add(Map.of(
                "name", "Successful Requests",
                "query", "SELECT count(\"count\") FROM \"http_requests\" WHERE \"status\" = '200' AND time > now() - 1h",
                "description", "Count only successful requests"
            ));
            
            return examples;
        }
    }

    // ============================================
    // Example 10: InfluxDB Integration REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/influx")
    public static class InfluxDBController {
        
        private final InfluxHTTPMetricsService httpMetrics;
        private final InfluxDatabaseMetricsService dbMetrics;
        private final InfluxApplicationMetricsService appMetrics;
        private final InfluxBusinessMetricsService businessMetrics;
        private final InfluxLineProtocolService lineProtocol;
        private final InfluxRetentionPolicyService retentionPolicy;
        private final InfluxContinuousQueryService continuousQuery;
        private final InfluxQLQueryExamplesService queryExamples;
        
        public InfluxDBController(
                InfluxHTTPMetricsService httpMetrics,
                InfluxDatabaseMetricsService dbMetrics,
                InfluxApplicationMetricsService appMetrics,
                InfluxBusinessMetricsService businessMetrics,
                InfluxLineProtocolService lineProtocol,
                InfluxRetentionPolicyService retentionPolicy,
                InfluxContinuousQueryService continuousQuery,
                InfluxQLQueryExamplesService queryExamples) {
            this.httpMetrics = httpMetrics;
            this.dbMetrics = dbMetrics;
            this.appMetrics = appMetrics;
            this.businessMetrics = businessMetrics;
            this.lineProtocol = lineProtocol;
            this.retentionPolicy = retentionPolicy;
            this.continuousQuery = continuousQuery;
            this.queryExamples = queryExamples;
        }
        
        @GetMapping("/request")
        public String simulateRequest(
                @RequestParam(defaultValue = "GET") String method,
                @RequestParam(defaultValue = "/api/data") String endpoint,
                @RequestParam(defaultValue = "200") int status) {
            return httpMetrics.handleRequest(method, endpoint, status);
        }
        
        @PostMapping("/db/query")
        public String executeQuery(
                @RequestParam(defaultValue = "SELECT") String operation,
                @RequestParam(defaultValue = "users") String table) {
            return dbMetrics.executeQuery(operation, table);
        }
        
        @PostMapping("/db/connection/acquire")
        public Map<String, String> acquireConnection() {
            dbMetrics.connectionAcquired();
            return Collections.singletonMap("status", "connection acquired");
        }
        
        @PostMapping("/db/connection/release")
        public Map<String, String> releaseConnection() {
            dbMetrics.connectionReleased();
            return Collections.singletonMap("status", "connection released");
        }
        
        @PostMapping("/user/login")
        public Map<String, String> userLogin(@RequestParam String userId) {
            appMetrics.userLogin(userId);
            return Collections.singletonMap("status", "user logged in");
        }
        
        @PostMapping("/user/logout")
        public Map<String, String> userLogout(@RequestParam String userId) {
            appMetrics.userLogout(userId);
            return Collections.singletonMap("status", "user logged out");
        }
        
        @PostMapping("/error")
        public Map<String, String> recordError(@RequestParam String errorType) {
            appMetrics.recordError(errorType);
            return Collections.singletonMap("status", "error recorded");
        }
        
        @PostMapping("/order")
        public String processOrder(
                @RequestParam String orderId,
                @RequestParam int amountCents,
                @RequestParam(defaultValue = "1") int quantity) {
            return businessMetrics.processOrder(orderId, amountCents, quantity);
        }
        
        @PostMapping("/inventory/restock")
        public Map<String, String> restockInventory(@RequestParam int quantity) {
            businessMetrics.restockInventory(quantity);
            return Collections.singletonMap("status", "inventory restocked");
        }
        
        @GetMapping("/line-protocol/examples")
        public List<String> getLineProtocolExamples() {
            return lineProtocol.getLineProtocolExamples();
        }
        
        @GetMapping("/retention-policies")
        public List<Map<String, Object>> getRetentionPolicies() {
            return retentionPolicy.getRetentionPolicyExamples();
        }
        
        @GetMapping("/continuous-queries")
        public List<Map<String, String>> getContinuousQueries() {
            return continuousQuery.getContinuousQueryExamples();
        }
        
        @GetMapping("/query-examples")
        public List<Map<String, String>> getQueryExamples() {
            return queryExamples.getInfluxQLExamples();
        }
    }
}
