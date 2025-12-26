package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Micrometer Pattern - Vendor-Neutral Application Metrics
 * 
 * Purpose:
 * - Collect application metrics in vendor-neutral way
 * - Support multiple monitoring systems (Prometheus, Grafana, etc.)
 * - Dimensional metrics with tags
 * - Pre-built meters for common metrics
 * - Custom application metrics
 * 
 * Use Cases:
 * - Application performance monitoring
 * - Business metrics tracking (orders, revenue, users)
 * - SLA/SLO monitoring
 * - System health metrics (CPU, memory, threads)
 * - Request/response metrics (rate, duration, errors)
 * - Database query metrics
 * - Cache hit/miss ratios
 * - JVM metrics
 * - Custom business KPIs
 * - API endpoint metrics
 * 
 * Key Concepts:
 * - MeterRegistry: Central registry for all meters
 * - Counter: Monotonically increasing value
 * - Gauge: Current value that can go up or down
 * - Timer: Measure duration and frequency
 * - DistributionSummary: Distribution of events
 * - LongTaskTimer: Track long-running tasks
 * - Tags: Dimensional metadata (key-value pairs)
 * - Composite MeterRegistry: Multiple backends
 * 
 * Supported Backends:
 * - Prometheus (pull-based, metrics endpoint)
 * - Graphite (push-based, time-series)
 * - InfluxDB (time-series database)
 * - Datadog (SaaS monitoring)
 * - New Relic (APM platform)
 * - CloudWatch (AWS monitoring)
 * - Azure Monitor
 * - StatsD
 * - JMX
 * - Simple (in-memory)
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-core</artifactId>
 * </dependency>
 * <!-- For Prometheus -->
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-registry-prometheus</artifactId>
 * </dependency>
 * 
 * Configuration (application.yml):
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: health,metrics,prometheus
 *   metrics:
 *     export:
 *       prometheus:
 *         enabled: true
 *     tags:
 *       application: ${spring.application.name}
 *       environment: ${spring.profiles.active}
 *     distribution:
 *       percentiles-histogram:
 *         http.server.requests: true
 * 
 * Access Metrics:
 * # All metrics
 * GET http://localhost:8080/actuator/metrics
 * 
 * # Specific metric
 * GET http://localhost:8080/actuator/metrics/jvm.memory.used
 * 
 * # Prometheus format
 * GET http://localhost:8080/actuator/prometheus
 * 
 * Best Practices:
 * - Use consistent naming conventions (snake_case for Prometheus)
 * - Add meaningful tags for dimensionality
 * - Avoid high-cardinality tags (e.g., user IDs, request IDs)
 * - Use timers for request durations
 * - Use counters for event counts
 * - Use gauges for current values
 * - Set up percentiles for latency metrics
 * - Monitor both technical and business metrics
 * - Use MeterBinder for reusable metrics
 * - Apply MeterFilter to customize metrics
 */
@SpringBootApplication
public class MicrometerPattern {

    public static void main(String[] args) {
        SpringApplication.run(MicrometerPattern.class, args);
    }

    // ============================================
    // Example 1: Counter - Monotonically Increasing
    // ============================================
    
    /**
     * Count events that always increase.
     * Use for: requests, orders, errors, events.
     */
    @Service
    public static class OrderService {
        
        private final Counter orderCounter;
        private final Counter orderSuccessCounter;
        private final Counter orderFailureCounter;
        
        public OrderService(MeterRegistry registry) {
            this.orderCounter = Counter.builder("orders.total")
                .description("Total number of orders")
                .tag("type", "all")
                .register(registry);
            
            this.orderSuccessCounter = Counter.builder("orders.total")
                .description("Successful orders")
                .tag("type", "success")
                .register(registry);
            
            this.orderFailureCounter = Counter.builder("orders.total")
                .description("Failed orders")
                .tag("type", "failure")
                .register(registry);
        }
        
        public void processOrder(String orderId, boolean success) {
            orderCounter.increment();
            
            if (success) {
                orderSuccessCounter.increment();
                System.out.println("Order processed successfully: " + orderId);
            } else {
                orderFailureCounter.increment();
                System.out.println("Order failed: " + orderId);
            }
        }
        
        public void processOrderWithAmount(String orderId, double amount) {
            // Increment by specific amount
            orderCounter.increment(amount);
            System.out.println("Order processed with amount: $" + amount);
        }
    }

    // ============================================
    // Example 2: Gauge - Current Value
    // ============================================
    
    /**
     * Track current value that can increase or decrease.
     * Use for: active connections, queue size, cache size.
     */
    @Service
    public static class ConnectionPoolService {
        
        private final AtomicInteger activeConnections = new AtomicInteger(0);
        private final AtomicInteger idleConnections = new AtomicInteger(10);
        
        public ConnectionPoolService(MeterRegistry registry) {
            // Gauge tracks the value of activeConnections
            Gauge.builder("connections.active", activeConnections, AtomicInteger::get)
                .description("Number of active connections")
                .tag("pool", "main")
                .register(registry);
            
            Gauge.builder("connections.idle", idleConnections, AtomicInteger::get)
                .description("Number of idle connections")
                .tag("pool", "main")
                .register(registry);
            
            // Gauge with function
            Gauge.builder("connections.total", this, 
                    ConnectionPoolService::getTotalConnections)
                .description("Total connections (active + idle)")
                .tag("pool", "main")
                .register(registry);
        }
        
        public void acquireConnection() {
            activeConnections.incrementAndGet();
            idleConnections.decrementAndGet();
            System.out.println("Connection acquired. Active: " + activeConnections.get());
        }
        
        public void releaseConnection() {
            activeConnections.decrementAndGet();
            idleConnections.incrementAndGet();
            System.out.println("Connection released. Active: " + activeConnections.get());
        }
        
        private double getTotalConnections() {
            return activeConnections.get() + idleConnections.get();
        }
    }

    // ============================================
    // Example 3: Timer - Duration and Rate
    // ============================================
    
    /**
     * Measure duration of operations and rate.
     * Use for: API requests, database queries, method execution.
     */
    @Service
    public static class PaymentService {
        
        private final Timer paymentTimer;
        private final MeterRegistry registry;
        
        public PaymentService(MeterRegistry registry) {
            this.registry = registry;
            this.paymentTimer = Timer.builder("payment.process.duration")
                .description("Payment processing duration")
                .tag("service", "payment")
                .publishPercentiles(0.5, 0.95, 0.99) // 50th, 95th, 99th percentiles
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(10))
                .register(registry);
        }
        
        public void processPayment(String paymentId) {
            // Method 1: Using record()
            paymentTimer.record(() -> {
                // Simulate payment processing
                try {
                    Thread.sleep((long) (Math.random() * 100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Payment processed: " + paymentId);
            });
        }
        
        public void processPaymentWithSample(String paymentId, String paymentType) {
            // Method 2: Using Timer.Sample for more control
            Timer.Sample sample = Timer.start(registry);
            
            try {
                // Simulate processing
                Thread.sleep((long) (Math.random() * 100));
                System.out.println("Payment processed: " + paymentId + " type: " + paymentType);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                sample.stop(Timer.builder("payment.process.duration")
                    .tag("service", "payment")
                    .tag("type", paymentType)
                    .register(registry));
            }
        }
        
        public void recordManualDuration(long milliseconds) {
            paymentTimer.record(Duration.ofMillis(milliseconds));
        }
    }

    // ============================================
    // Example 4: Distribution Summary - Event Distribution
    // ============================================
    
    /**
     * Track distribution of values (not duration).
     * Use for: request size, response size, order amounts.
     */
    @Service
    public static class AnalyticsService {
        
        private final DistributionSummary requestSizeSummary;
        private final DistributionSummary orderAmountSummary;
        
        public AnalyticsService(MeterRegistry registry) {
            this.requestSizeSummary = DistributionSummary.builder("http.request.size")
                .description("HTTP request size in bytes")
                .baseUnit("bytes")
                .publishPercentiles(0.5, 0.95, 0.99)
                .minimumExpectedValue(1.0)
                .maximumExpectedValue(1024.0 * 1024.0) // 1 MB
                .register(registry);
            
            this.orderAmountSummary = DistributionSummary.builder("order.amount")
                .description("Order amount in USD")
                .baseUnit("USD")
                .publishPercentiles(0.5, 0.95, 0.99)
                .minimumExpectedValue(1.0)
                .maximumExpectedValue(10000.0)
                .register(registry);
        }
        
        public void recordRequestSize(long sizeInBytes) {
            requestSizeSummary.record(sizeInBytes);
            System.out.println("Recorded request size: " + sizeInBytes + " bytes");
        }
        
        public void recordOrderAmount(double amount) {
            orderAmountSummary.record(amount);
            System.out.println("Recorded order amount: $" + amount);
        }
        
        public void printStatistics() {
            System.out.println("=== Request Size Statistics ===");
            System.out.println("Count: " + requestSizeSummary.count());
            System.out.println("Total: " + requestSizeSummary.totalAmount());
            System.out.println("Max: " + requestSizeSummary.max());
            System.out.println("Mean: " + requestSizeSummary.mean());
        }
    }

    // ============================================
    // Example 5: Long Task Timer - Long-Running Operations
    // ============================================
    
    /**
     * Track currently executing long-running tasks.
     * Use for: batch jobs, data migrations, long reports.
     */
    @Service
    public static class BatchJobService {
        
        private final LongTaskTimer batchJobTimer;
        
        public BatchJobService(MeterRegistry registry) {
            this.batchJobTimer = LongTaskTimer.builder("batch.job.duration")
                .description("Batch job execution duration")
                .tag("job", "data-export")
                .register(registry);
        }
        
        public void runBatchJob(String jobId) {
            LongTaskTimer.Sample sample = batchJobTimer.start();
            
            try {
                System.out.println("Starting batch job: " + jobId);
                System.out.println("Active tasks: " + batchJobTimer.activeTasks());
                
                // Simulate long-running job
                Thread.sleep(5000);
                
                System.out.println("Batch job completed: " + jobId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                long duration = sample.stop();
                System.out.println("Job duration: " + duration + "ns");
            }
        }
    }

    // ============================================
    // Example 6: Tagged Metrics - Dimensional Data
    // ============================================
    
    /**
     * Add tags for multi-dimensional metrics.
     * Tags allow filtering and aggregation.
     */
    @Service
    public static class ApiMetricsService {
        
        private final MeterRegistry registry;
        
        public ApiMetricsService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void recordApiRequest(String endpoint, String method, String status) {
            Counter.builder("api.requests.total")
                .description("Total API requests")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", status)
                .register(registry)
                .increment();
        }
        
        public void recordApiDuration(String endpoint, String method, long durationMs) {
            Timer.builder("api.request.duration")
                .description("API request duration")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry)
                .record(Duration.ofMillis(durationMs));
        }
    }

    // ============================================
    // Example 7: Custom MeterBinder
    // ============================================
    
    /**
     * Create reusable metric bindings.
     */
    @Component
    public static class CustomApplicationMetrics implements MeterBinder {
        
        private final Map<String, Object> cache = new ConcurrentHashMap<>();
        private final AtomicInteger userSessions = new AtomicInteger(0);
        
        @Override
        public void bindTo(MeterRegistry registry) {
            // Cache size gauge
            Gauge.builder("cache.size", cache, Map::size)
                .description("Current cache size")
                .tag("type", "application")
                .register(registry);
            
            // Active user sessions
            Gauge.builder("user.sessions.active", userSessions, AtomicInteger::get)
                .description("Active user sessions")
                .tag("type", "user")
                .register(registry);
        }
        
        public void addToCache(String key, Object value) {
            cache.put(key, value);
        }
        
        public void incrementSessions() {
            userSessions.incrementAndGet();
        }
        
        public void decrementSessions() {
            userSessions.decrementAndGet();
        }
    }

    // ============================================
    // Example 8: MeterFilter Configuration
    // ============================================
    
    /**
     * Filter and transform metrics.
     */
    @Configuration
    public static class MetricsConfiguration {
        
        @Bean
        public MeterFilter commonTagsFilter() {
            // Add common tags to all metrics
            return MeterFilter.commonTags(
                Tags.of(
                    "application", "demo-app",
                    "region", "us-east-1"
                )
            );
        }
        
        @Bean
        public MeterFilter denySpecificMetrics() {
            // Deny specific metrics
            return MeterFilter.deny(id -> 
                id.getName().startsWith("jvm.gc.pause") && 
                id.getTag("cause").equals("Metadata GC Threshold")
            );
        }
        
        @Bean
        public MeterFilter renameMetrics() {
            // Rename metrics
            return MeterFilter.renameTag("http.server.requests", "uri", "endpoint");
        }
        
        @Bean
        public MeterFilter maximumAllowableMetrics() {
            // Limit maximum value
            return MeterFilter.maximumAllowableMetrics(1000);
        }
    }

    // ============================================
    // Example 9: Metrics Controller
    // ============================================
    
    @RestController
    @RequestMapping("/metrics-demo")
    public static class MetricsController {
        
        private final OrderService orderService;
        private final ConnectionPoolService poolService;
        private final PaymentService paymentService;
        private final AnalyticsService analyticsService;
        private final BatchJobService batchJobService;
        private final ApiMetricsService apiMetrics;
        private final CustomApplicationMetrics customMetrics;
        
        public MetricsController(
                OrderService orderService,
                ConnectionPoolService poolService,
                PaymentService paymentService,
                AnalyticsService analyticsService,
                BatchJobService batchJobService,
                ApiMetricsService apiMetrics,
                CustomApplicationMetrics customMetrics) {
            this.orderService = orderService;
            this.poolService = poolService;
            this.paymentService = paymentService;
            this.analyticsService = analyticsService;
            this.batchJobService = batchJobService;
            this.apiMetrics = apiMetrics;
            this.customMetrics = customMetrics;
        }
        
        @PostMapping("/orders")
        public String createOrder(@RequestParam boolean success) {
            String orderId = "ORD-" + System.currentTimeMillis();
            orderService.processOrder(orderId, success);
            apiMetrics.recordApiRequest("/orders", "POST", success ? "200" : "500");
            return orderId;
        }
        
        @GetMapping("/connections/acquire")
        public String acquireConnection() {
            poolService.acquireConnection();
            return "Connection acquired";
        }
        
        @GetMapping("/connections/release")
        public String releaseConnection() {
            poolService.releaseConnection();
            return "Connection released";
        }
        
        @PostMapping("/payments")
        public String processPayment(@RequestParam String type) {
            String paymentId = "PAY-" + System.currentTimeMillis();
            paymentService.processPaymentWithSample(paymentId, type);
            return paymentId;
        }
        
        @PostMapping("/analytics/request-size")
        public String recordRequestSize(@RequestParam long size) {
            analyticsService.recordRequestSize(size);
            return "Recorded";
        }
        
        @PostMapping("/analytics/order-amount")
        public String recordOrderAmount(@RequestParam double amount) {
            analyticsService.recordOrderAmount(amount);
            return "Recorded";
        }
        
        @PostMapping("/batch-job")
        public String runBatchJob() {
            String jobId = "JOB-" + System.currentTimeMillis();
            new Thread(() -> batchJobService.runBatchJob(jobId)).start();
            return "Job started: " + jobId;
        }
        
        @PostMapping("/sessions/login")
        public String login() {
            customMetrics.incrementSessions();
            return "User logged in";
        }
        
        @PostMapping("/sessions/logout")
        public String logout() {
            customMetrics.decrementSessions();
            return "User logged out";
        }
        
        @GetMapping("/cache/add")
        public String addToCache(@RequestParam String key, @RequestParam String value) {
            customMetrics.addToCache(key, value);
            return "Added to cache";
        }
    }
}
