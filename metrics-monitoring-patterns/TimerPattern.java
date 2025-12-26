package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Timer Pattern - Duration and Rate Measurement
 * 
 * Purpose:
 * - Measure operation duration
 * - Track event frequency (rate)
 * - Record both count and total time
 * - Calculate percentiles
 * - Monitor SLA/SLO compliance
 * 
 * Use Cases:
 * - HTTP request latency
 * - Database query duration
 * - Method execution time
 * - External API call duration
 * - Cache operation timing
 * - Business process duration
 * - Batch job timing
 * - Message processing time
 * - File I/O operations
 * - Network operations
 * 
 * Timer Metrics:
 * - Count: Number of events
 * - Total Time: Sum of all durations
 * - Max: Maximum duration
 * - Mean: Average duration
 * - Percentiles: p50, p95, p99
 * - Histogram: Distribution buckets
 * 
 * Recording Methods:
 * - timer.record(Runnable): Time a runnable
 * - timer.record(Callable): Time and return value
 * - timer.record(Duration): Record specific duration
 * - Timer.Sample: Manual start/stop
 * - @Timed annotation: AOP-based timing
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-core</artifactId>
 * </dependency>
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     distribution:
 *       percentiles-histogram:
 *         http.server.requests: true
 *       percentiles:
 *         http.server.requests: 0.5, 0.95, 0.99
 *       slo:
 *         http.server.requests: 10ms, 50ms, 100ms, 500ms, 1s
 * 
 * Query Examples (Prometheus):
 * # Request rate (per second)
 * rate(http_request_duration_seconds_count[5m])
 * 
 * # Average latency
 * rate(http_request_duration_seconds_sum[5m]) / rate(http_request_duration_seconds_count[5m])
 * 
 * # 95th percentile
 * histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))
 * 
 * # SLO compliance (% of requests < 100ms)
 * sum(rate(http_request_duration_seconds_bucket{le="0.1"}[5m])) / sum(rate(http_request_duration_seconds_count[5m]))
 * 
 * Best Practices:
 * - Use for latency measurement
 * - Enable percentile histograms for SLO tracking
 * - Set minimum/maximum expected values
 * - Use tags for dimensionality
 * - Monitor both average and percentiles
 * - Set up SLO buckets
 * - Use Timer.Sample for complex flows
 * - Cache Timer instances
 * - Add meaningful descriptions
 * - Monitor timeouts separately
 */
@SpringBootApplication
public class TimerPattern {

    public static void main(String[] args) {
        SpringApplication.run(TimerPattern.class, args);
    }

    // ============================================
    // Example 1: Basic Timer with Runnable
    // ============================================
    
    @Service
    public static class BasicTimerService {
        
        private final Timer requestTimer;
        
        public BasicTimerService(MeterRegistry registry) {
            this.requestTimer = Timer.builder("requests.duration")
                .description("Request processing duration")
                .tag("service", "basic")
                .register(registry);
        }
        
        public void processRequest(String requestId) {
            requestTimer.record(() -> {
                // Simulate processing
                try {
                    Thread.sleep((long) (Math.random() * 100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Request processed: " + requestId);
            });
            
            printTimerStats();
        }
        
        private void printTimerStats() {
            System.out.println(String.format(
                "Timer Stats - Count: %d, Total: %.2fs, Max: %.2fms, Mean: %.2fms",
                requestTimer.count(),
                requestTimer.totalTime(TimeUnit.SECONDS),
                requestTimer.max(TimeUnit.MILLISECONDS),
                requestTimer.mean(TimeUnit.MILLISECONDS)
            ));
        }
    }

    // ============================================
    // Example 2: Timer with Callable (Return Value)
    // ============================================
    
    @Service
    public static class DatabaseService {
        
        private final Timer queryTimer;
        
        public DatabaseService(MeterRegistry registry) {
            this.queryTimer = Timer.builder("database.query.duration")
                .description("Database query execution time")
                .tag("database", "postgres")
                .publishPercentiles(0.5, 0.95, 0.99)
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(5))
                .register(registry);
        }
        
        public String executeQuery(String query) {
            return queryTimer.record(() -> {
                // Simulate database query
                try {
                    Thread.sleep((long) (Math.random() * 50));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String result = "Result for: " + query;
                System.out.println("Query executed: " + query);
                return result;
            });
        }
    }

    // ============================================
    // Example 3: Timer.Sample for Manual Control
    // ============================================
    
    @Service
    public static class PaymentService {
        
        private final MeterRegistry registry;
        
        public PaymentService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void processPayment(String paymentId, String paymentType, boolean success) {
            Timer.Sample sample = Timer.start(registry);
            
            try {
                // Simulate payment processing
                Thread.sleep((long) (Math.random() * 200));
                
                if (!success) {
                    throw new RuntimeException("Payment failed");
                }
                
                System.out.println("Payment processed: " + paymentId);
            } catch (Exception e) {
                System.out.println("Payment error: " + e.getMessage());
            } finally {
                // Stop timer with tags based on outcome
                sample.stop(Timer.builder("payment.processing.duration")
                    .description("Payment processing time")
                    .tag("type", paymentType)
                    .tag("status", success ? "success" : "failure")
                    .publishPercentiles(0.95, 0.99)
                    .register(registry));
            }
        }
    }

    // ============================================
    // Example 4: Timer with Duration Recording
    // ============================================
    
    @Service
    public static class ExternalApiService {
        
        private final Timer apiCallTimer;
        
        public ExternalApiService(MeterRegistry registry) {
            this.apiCallTimer = Timer.builder("external.api.duration")
                .description("External API call duration")
                .tag("api", "partner")
                .publishPercentileHistogram()
                .serviceLevelObjectives(
                    Duration.ofMillis(50),
                    Duration.ofMillis(100),
                    Duration.ofMillis(500),
                    Duration.ofSeconds(1)
                )
                .register(registry);
        }
        
        public void callExternalApi(String endpoint, long durationMs) {
            apiCallTimer.record(Duration.ofMillis(durationMs));
            System.out.println("API call to " + endpoint + " took " + durationMs + "ms");
        }
        
        public void callApi(String endpoint) {
            long start = System.currentTimeMillis();
            
            try {
                // Simulate API call
                Thread.sleep((long) (Math.random() * 300));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long duration = System.currentTimeMillis() - start;
            apiCallTimer.record(Duration.ofMillis(duration));
            System.out.println("API call completed in " + duration + "ms");
        }
    }

    // ============================================
    // Example 5: HTTP Request Timer with Tags
    // ============================================
    
    @Service
    public static class HttpRequestTimerService {
        
        private final MeterRegistry registry;
        
        public HttpRequestTimerService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void recordHttpRequest(String method, String endpoint, int statusCode, long durationMs) {
            Timer.builder("http.request.duration")
                .description("HTTP request duration")
                .tag("method", method)
                .tag("endpoint", endpoint)
                .tag("status", String.valueOf(statusCode))
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry)
                .record(Duration.ofMillis(durationMs));
            
            System.out.println(String.format(
                "HTTP %s %s -> %d (%dms)", method, endpoint, statusCode, durationMs));
        }
    }

    // ============================================
    // Example 6: Cache Operation Timer
    // ============================================
    
    @Service
    public static class CacheTimerService {
        
        private final Timer cacheGetTimer;
        private final Timer cachePutTimer;
        
        public CacheTimerService(MeterRegistry registry) {
            this.cacheGetTimer = Timer.builder("cache.operation.duration")
                .description("Cache operation duration")
                .tag("operation", "get")
                .publishPercentiles(0.95, 0.99)
                .register(registry);
            
            this.cachePutTimer = Timer.builder("cache.operation.duration")
                .description("Cache operation duration")
                .tag("operation", "put")
                .publishPercentiles(0.95, 0.99)
                .register(registry);
        }
        
        public Object getCacheValue(String key) {
            return cacheGetTimer.record(() -> {
                // Simulate cache lookup
                try {
                    Thread.sleep((long) (Math.random() * 10));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Cache get: " + key);
                return "value-" + key;
            });
        }
        
        public void putCacheValue(String key, Object value) {
            cachePutTimer.record(() -> {
                // Simulate cache put
                try {
                    Thread.sleep((long) (Math.random() * 20));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Cache put: " + key);
            });
        }
    }

    // ============================================
    // Example 7: Business Process Timer
    // ============================================
    
    @Service
    public static class OrderProcessingTimerService {
        
        private final Timer orderTimer;
        private final MeterRegistry registry;
        
        public OrderProcessingTimerService(MeterRegistry registry) {
            this.registry = registry;
            this.orderTimer = Timer.builder("order.processing.duration")
                .description("Order processing time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);
        }
        
        public void processOrder(String orderId, String orderType, int itemCount) {
            Timer.Sample sample = Timer.start(registry);
            
            try {
                // Simulate order validation
                Thread.sleep(50);
                
                // Simulate inventory check
                Thread.sleep(30);
                
                // Simulate payment processing
                Thread.sleep(100);
                
                // Simulate fulfillment
                Thread.sleep(itemCount * 10L);
                
                System.out.println("Order processed: " + orderId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                sample.stop(Timer.builder("order.processing.duration")
                    .tag("type", orderType)
                    .tag("items", String.valueOf(itemCount))
                    .register(registry));
            }
        }
    }

    // ============================================
    // Example 8: Message Processing Timer
    // ============================================
    
    @Service
    public static class MessageProcessingTimerService {
        
        private final Timer processingTimer;
        
        public MessageProcessingTimerService(MeterRegistry registry) {
            this.processingTimer = Timer.builder("message.processing.duration")
                .description("Message processing time")
                .tag("queue", "orders")
                .publishPercentiles(0.95, 0.99)
                .minimumExpectedValue(Duration.ofMillis(10))
                .maximumExpectedValue(Duration.ofSeconds(10))
                .register(registry);
        }
        
        public void processMessage(String messageId, String messageType) {
            processingTimer.record(() -> {
                try {
                    // Simulate message processing
                    Thread.sleep((long) (Math.random() * 150));
                    System.out.println("Message processed: " + messageId + " type: " + messageType);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    // ============================================
    // Example 9: Batch Job Timer
    // ============================================
    
    @Service
    public static class BatchJobTimerService {
        
        private final Timer batchTimer;
        
        public BatchJobTimerService(MeterRegistry registry) {
            this.batchTimer = Timer.builder("batch.job.duration")
                .description("Batch job execution time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .minimumExpectedValue(Duration.ofSeconds(1))
                .maximumExpectedValue(Duration.ofMinutes(10))
                .register(registry);
        }
        
        public void runBatchJob(String jobName, int recordCount) {
            batchTimer.record(() -> {
                try {
                    System.out.println("Batch job started: " + jobName);
                    
                    // Simulate processing records
                    for (int i = 0; i < recordCount; i++) {
                        Thread.sleep(10);
                    }
                    
                    System.out.println("Batch job completed: " + jobName + " (" + recordCount + " records)");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    // ============================================
    // Example 10: Timer Controller
    // ============================================
    
    @RestController
    @RequestMapping("/timer-demo")
    public static class TimerController {
        
        private final BasicTimerService basicTimer;
        private final DatabaseService databaseService;
        private final PaymentService paymentService;
        private final ExternalApiService externalApi;
        private final HttpRequestTimerService httpTimer;
        private final CacheTimerService cacheTimer;
        private final OrderProcessingTimerService orderTimer;
        private final MessageProcessingTimerService messageTimer;
        private final BatchJobTimerService batchTimer;
        
        public TimerController(
                BasicTimerService basicTimer,
                DatabaseService databaseService,
                PaymentService paymentService,
                ExternalApiService externalApi,
                HttpRequestTimerService httpTimer,
                CacheTimerService cacheTimer,
                OrderProcessingTimerService orderTimer,
                MessageProcessingTimerService messageTimer,
                BatchJobTimerService batchTimer) {
            this.basicTimer = basicTimer;
            this.databaseService = databaseService;
            this.paymentService = paymentService;
            this.externalApi = externalApi;
            this.httpTimer = httpTimer;
            this.cacheTimer = cacheTimer;
            this.orderTimer = orderTimer;
            this.messageTimer = messageTimer;
            this.batchTimer = batchTimer;
        }
        
        @GetMapping("/basic")
        public String basicRequest() {
            String requestId = "REQ-" + System.currentTimeMillis();
            basicTimer.processRequest(requestId);
            return requestId;
        }
        
        @GetMapping("/database")
        public String databaseQuery(@RequestParam String query) {
            return databaseService.executeQuery(query);
        }
        
        @PostMapping("/payment")
        public String processPayment(@RequestParam String type, @RequestParam boolean success) {
            String paymentId = "PAY-" + System.currentTimeMillis();
            paymentService.processPayment(paymentId, type, success);
            return paymentId;
        }
        
        @GetMapping("/external-api")
        public String callExternalApi(@RequestParam String endpoint) {
            externalApi.callApi(endpoint);
            return "API called";
        }
        
        @GetMapping("/http-request")
        public String recordHttpRequest(
                @RequestParam String method,
                @RequestParam String endpoint,
                @RequestParam int status,
                @RequestParam long duration) {
            httpTimer.recordHttpRequest(method, endpoint, status, duration);
            return "HTTP request recorded";
        }
        
        @GetMapping("/cache/get")
        public Object getCacheValue(@RequestParam String key) {
            return cacheTimer.getCacheValue(key);
        }
        
        @PostMapping("/cache/put")
        public String putCacheValue(@RequestParam String key, @RequestParam String value) {
            cacheTimer.putCacheValue(key, value);
            return "Cache put completed";
        }
        
        @PostMapping("/order")
        public String processOrder(@RequestParam String type, @RequestParam int items) {
            String orderId = "ORD-" + System.currentTimeMillis();
            new Thread(() -> orderTimer.processOrder(orderId, type, items)).start();
            return orderId;
        }
        
        @PostMapping("/message")
        public String processMessage(@RequestParam String type) {
            String messageId = "MSG-" + System.currentTimeMillis();
            messageTimer.processMessage(messageId, type);
            return messageId;
        }
        
        @PostMapping("/batch")
        public String runBatchJob(@RequestParam String jobName, @RequestParam int records) {
            new Thread(() -> batchTimer.runBatchJob(jobName, records)).start();
            return "Batch job started: " + jobName;
        }
    }
}
