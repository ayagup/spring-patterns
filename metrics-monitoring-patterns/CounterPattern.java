package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Counter Pattern - Monotonically Increasing Metrics
 * 
 * Purpose:
 * - Track counts that only increase
 * - Measure event occurrences
 * - Calculate rates (events per second)
 * - Monitor totals over time
 * 
 * Use Cases:
 * - HTTP request counts
 * - Error counts
 * - Order counts
 * - Message counts
 * - Cache hits/misses
 * - API calls
 * - Event processing counts
 * - User actions (logins, signups)
 * - Database queries
 * - Job executions
 * 
 * Counter Characteristics:
 * - Only increases (never decreases)
 * - Resets to zero on application restart
 * - Can increment by 1 or custom amount
 * - Thread-safe
 * - Low overhead
 * - Supports tags for dimensionality
 * 
 * Counter vs Gauge:
 * - Counter: Monotonically increasing (requests, errors)
 * - Gauge: Can increase or decrease (memory, connections)
 * 
 * Rate Calculation:
 * - Prometheus: rate(counter[5m]) - per-second rate
 * - Grafana: Show rate instead of raw counter value
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
 *     tags:
 *       application: demo-app
 *       environment: production
 * 
 * Query Examples (Prometheus):
 * # Total requests
 * http_requests_total
 * 
 * # Requests per second (5m rate)
 * rate(http_requests_total[5m])
 * 
 * # Requests by status
 * http_requests_total{status="200"}
 * 
 * # Error rate
 * rate(http_requests_total{status="500"}[5m])
 * 
 * Best Practices:
 * - Use meaningful metric names (snake_case for Prometheus)
 * - Add tags for dimensionality (endpoint, method, status)
 * - Avoid high-cardinality tags (user IDs, request IDs)
 * - Use _total suffix for counter names
 * - Cache Counter instances (don't create new ones repeatedly)
 * - Use Counter.builder() for better configuration
 * - Add descriptions for documentation
 * - Monitor both success and failure counts
 * - Track business metrics (orders, revenue)
 * - Use tags consistently across metrics
 * 
 * Common Patterns:
 * - Success/failure counters
 * - Per-endpoint counters
 * - Per-user-type counters
 * - Time-based counters (hourly, daily)
 * - Cumulative counters
 */
@SpringBootApplication
public class CounterPattern {

    public static void main(String[] args) {
        SpringApplication.run(CounterPattern.class, args);
    }

    // ============================================
    // Example 1: Basic Counter
    // ============================================
    
    @Service
    public static class BasicCounterService {
        
        private final Counter requestCounter;
        
        public BasicCounterService(MeterRegistry registry) {
            this.requestCounter = Counter.builder("http.requests.total")
                .description("Total HTTP requests")
                .register(registry);
        }
        
        public void handleRequest() {
            requestCounter.increment();
            System.out.println("Request handled. Total: " + requestCounter.count());
        }
        
        public void handleBulkRequests(int count) {
            requestCounter.increment(count);
            System.out.println("Handled " + count + " requests. Total: " + requestCounter.count());
        }
    }

    // ============================================
    // Example 2: Tagged Counter
    // ============================================
    
    /**
     * Use tags to create multi-dimensional metrics.
     * Tags allow filtering and aggregation.
     */
    @Service
    public static class HttpMetricsService {
        
        private final MeterRegistry registry;
        
        public HttpMetricsService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void recordRequest(String endpoint, String method, int statusCode) {
            Counter.builder("http.requests.total")
                .description("Total HTTP requests")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", String.valueOf(statusCode))
                .register(registry)
                .increment();
            
            System.out.println(String.format(
                "Request: %s %s -> %d", method, endpoint, statusCode));
        }
        
        public void recordError(String endpoint, String errorType) {
            Counter.builder("http.errors.total")
                .description("Total HTTP errors")
                .tag("endpoint", endpoint)
                .tag("error_type", errorType)
                .register(registry)
                .increment();
            
            System.out.println("Error recorded: " + endpoint + " - " + errorType);
        }
    }

    // ============================================
    // Example 3: Success/Failure Counters
    // ============================================
    
    @Service
    public static class PaymentProcessingService {
        
        private final Counter successCounter;
        private final Counter failureCounter;
        private final Counter totalCounter;
        
        public PaymentProcessingService(MeterRegistry registry) {
            this.successCounter = Counter.builder("payment.processed.total")
                .description("Total successful payments")
                .tag("status", "success")
                .register(registry);
            
            this.failureCounter = Counter.builder("payment.processed.total")
                .description("Total failed payments")
                .tag("status", "failure")
                .register(registry);
            
            this.totalCounter = Counter.builder("payment.attempts.total")
                .description("Total payment attempts")
                .register(registry);
        }
        
        public void processPayment(String paymentId, boolean success) {
            totalCounter.increment();
            
            if (success) {
                successCounter.increment();
                System.out.println("Payment succeeded: " + paymentId);
            } else {
                failureCounter.increment();
                System.out.println("Payment failed: " + paymentId);
            }
            
            System.out.println(String.format(
                "Success: %.0f, Failure: %.0f, Total: %.0f, Success Rate: %.2f%%",
                successCounter.count(),
                failureCounter.count(),
                totalCounter.count(),
                (successCounter.count() / totalCounter.count()) * 100
            ));
        }
        
        public void processPaymentWithAmount(String paymentId, double amount, boolean success) {
            totalCounter.increment();
            
            Counter amountCounter = Counter.builder("payment.amount.total")
                .description("Total payment amount")
                .tag("status", success ? "success" : "failure")
                .baseUnit("USD")
                .register(registry);
            
            amountCounter.increment(amount);
            
            if (success) {
                successCounter.increment();
                System.out.println("Payment succeeded: " + paymentId + " Amount: $" + amount);
            } else {
                failureCounter.increment();
                System.out.println("Payment failed: " + paymentId + " Amount: $" + amount);
            }
        }
    }

    // ============================================
    // Example 4: Cache Metrics Counter
    // ============================================
    
    @Service
    public static class CacheMetricsService {
        
        private final Counter hitCounter;
        private final Counter missCounter;
        private final Counter evictionCounter;
        private final MeterRegistry registry;
        
        public CacheMetricsService(MeterRegistry registry) {
            this.registry = registry;
            
            this.hitCounter = Counter.builder("cache.hits.total")
                .description("Total cache hits")
                .tag("cache", "main")
                .register(registry);
            
            this.missCounter = Counter.builder("cache.misses.total")
                .description("Total cache misses")
                .tag("cache", "main")
                .register(registry);
            
            this.evictionCounter = Counter.builder("cache.evictions.total")
                .description("Total cache evictions")
                .tag("cache", "main")
                .register(registry);
        }
        
        public void recordHit(String key) {
            hitCounter.increment();
            System.out.println("Cache hit: " + key);
            printCacheStats();
        }
        
        public void recordMiss(String key) {
            missCounter.increment();
            System.out.println("Cache miss: " + key);
            printCacheStats();
        }
        
        public void recordEviction(String key) {
            evictionCounter.increment();
            System.out.println("Cache eviction: " + key);
        }
        
        private void printCacheStats() {
            double hits = hitCounter.count();
            double misses = missCounter.count();
            double total = hits + misses;
            double hitRate = total > 0 ? (hits / total) * 100 : 0;
            
            System.out.println(String.format(
                "Cache Stats - Hits: %.0f, Misses: %.0f, Hit Rate: %.2f%%",
                hits, misses, hitRate
            ));
        }
    }

    // ============================================
    // Example 5: Order Processing Counter
    // ============================================
    
    @Service
    public static class OrderMetricsService {
        
        private final MeterRegistry registry;
        
        public OrderMetricsService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void recordOrder(String productCategory, String region, double amount) {
            // Count orders
            Counter.builder("orders.total")
                .description("Total orders")
                .tag("category", productCategory)
                .tag("region", region)
                .register(registry)
                .increment();
            
            // Revenue counter
            Counter.builder("orders.revenue.total")
                .description("Total revenue")
                .tag("category", productCategory)
                .tag("region", region)
                .baseUnit("USD")
                .register(registry)
                .increment(amount);
            
            System.out.println(String.format(
                "Order recorded - Category: %s, Region: %s, Amount: $%.2f",
                productCategory, region, amount
            ));
        }
        
        public void recordOrderStatus(String status, String reason) {
            Counter.builder("orders.status.total")
                .description("Orders by status")
                .tag("status", status)
                .tag("reason", reason)
                .register(registry)
                .increment();
        }
    }

    // ============================================
    // Example 6: API Rate Limiting Counter
    // ============================================
    
    @Service
    public static class RateLimitingService {
        
        private final MeterRegistry registry;
        private final Map<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
        
        public RateLimitingService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public boolean checkRateLimit(String userId, int maxRequests) {
            String key = "user:" + userId;
            AtomicLong count = requestCounts.computeIfAbsent(key, k -> new AtomicLong(0));
            long currentCount = count.incrementAndGet();
            
            Counter.builder("api.requests.total")
                .description("API requests per user")
                .tag("user", userId)
                .register(registry)
                .increment();
            
            if (currentCount > maxRequests) {
                Counter.builder("api.rate_limit.exceeded.total")
                    .description("Rate limit exceeded count")
                    .tag("user", userId)
                    .register(registry)
                    .increment();
                
                System.out.println("Rate limit exceeded for user: " + userId);
                return false;
            }
            
            System.out.println("Request allowed for user: " + userId + " (" + currentCount + "/" + maxRequests + ")");
            return true;
        }
        
        public void resetRateLimit(String userId) {
            String key = "user:" + userId;
            requestCounts.remove(key);
            System.out.println("Rate limit reset for user: " + userId);
        }
    }

    // ============================================
    // Example 7: Message Queue Counter
    // ============================================
    
    @Service
    public static class MessageQueueMetricsService {
        
        private final Counter publishedCounter;
        private final Counter consumedCounter;
        private final Counter deadLetterCounter;
        private final Counter retryCounter;
        
        public MessageQueueMetricsService(MeterRegistry registry) {
            this.publishedCounter = Counter.builder("messages.published.total")
                .description("Total messages published")
                .tag("queue", "orders")
                .register(registry);
            
            this.consumedCounter = Counter.builder("messages.consumed.total")
                .description("Total messages consumed")
                .tag("queue", "orders")
                .register(registry);
            
            this.deadLetterCounter = Counter.builder("messages.dead_letter.total")
                .description("Messages sent to dead letter queue")
                .tag("queue", "orders")
                .register(registry);
            
            this.retryCounter = Counter.builder("messages.retry.total")
                .description("Message retry attempts")
                .tag("queue", "orders")
                .register(registry);
        }
        
        public void publishMessage(String messageId) {
            publishedCounter.increment();
            System.out.println("Message published: " + messageId + " (Total: " + publishedCounter.count() + ")");
        }
        
        public void consumeMessage(String messageId, boolean success) {
            if (success) {
                consumedCounter.increment();
                System.out.println("Message consumed: " + messageId + " (Total: " + consumedCounter.count() + ")");
            } else {
                retryCounter.increment();
                System.out.println("Message retry: " + messageId + " (Retries: " + retryCounter.count() + ")");
            }
        }
        
        public void sendToDeadLetter(String messageId, String reason) {
            deadLetterCounter.increment();
            System.out.println("Message to DLQ: " + messageId + " - " + reason);
        }
        
        public void printStats() {
            System.out.println("=== Message Queue Stats ===");
            System.out.println("Published: " + publishedCounter.count());
            System.out.println("Consumed: " + consumedCounter.count());
            System.out.println("Dead Letter: " + deadLetterCounter.count());
            System.out.println("Retries: " + retryCounter.count());
        }
    }

    // ============================================
    // Example 8: Database Query Counter
    // ============================================
    
    @Service
    public static class DatabaseMetricsService {
        
        private final MeterRegistry registry;
        
        public DatabaseMetricsService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void recordQuery(String operation, String table, boolean success) {
            Counter.builder("database.queries.total")
                .description("Total database queries")
                .tag("operation", operation)
                .tag("table", table)
                .tag("status", success ? "success" : "failure")
                .register(registry)
                .increment();
            
            System.out.println(String.format(
                "DB Query - Operation: %s, Table: %s, Success: %s",
                operation, table, success
            ));
        }
        
        public void recordSlowQuery(String operation, String table, long durationMs) {
            if (durationMs > 1000) {
                Counter.builder("database.slow_queries.total")
                    .description("Slow database queries")
                    .tag("operation", operation)
                    .tag("table", table)
                    .register(registry)
                    .increment();
                
                System.out.println("Slow query detected: " + operation + " on " + table + " (" + durationMs + "ms)");
            }
        }
    }

    // ============================================
    // Example 9: User Activity Counter
    // ============================================
    
    @Service
    public static class UserActivityService {
        
        private final MeterRegistry registry;
        
        public UserActivityService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void recordLogin(String userType, boolean success) {
            Counter.builder("user.logins.total")
                .description("User login attempts")
                .tag("user_type", userType)
                .tag("status", success ? "success" : "failure")
                .register(registry)
                .increment();
        }
        
        public void recordSignup(String userType, String source) {
            Counter.builder("user.signups.total")
                .description("User signups")
                .tag("user_type", userType)
                .tag("source", source)
                .register(registry)
                .increment();
        }
        
        public void recordAction(String action, String feature) {
            Counter.builder("user.actions.total")
                .description("User actions")
                .tag("action", action)
                .tag("feature", feature)
                .register(registry)
                .increment();
        }
    }

    // ============================================
    // Example 10: Counter Controller
    // ============================================
    
    @RestController
    @RequestMapping("/counter-demo")
    public static class CounterController {
        
        private final BasicCounterService basicCounter;
        private final HttpMetricsService httpMetrics;
        private final PaymentProcessingService paymentService;
        private final CacheMetricsService cacheMetrics;
        private final OrderMetricsService orderMetrics;
        private final RateLimitingService rateLimiting;
        private final MessageQueueMetricsService messageQueue;
        private final DatabaseMetricsService databaseMetrics;
        private final UserActivityService userActivity;
        
        public CounterController(
                BasicCounterService basicCounter,
                HttpMetricsService httpMetrics,
                PaymentProcessingService paymentService,
                CacheMetricsService cacheMetrics,
                OrderMetricsService orderMetrics,
                RateLimitingService rateLimiting,
                MessageQueueMetricsService messageQueue,
                DatabaseMetricsService databaseMetrics,
                UserActivityService userActivity) {
            this.basicCounter = basicCounter;
            this.httpMetrics = httpMetrics;
            this.paymentService = paymentService;
            this.cacheMetrics = cacheMetrics;
            this.orderMetrics = orderMetrics;
            this.rateLimiting = rateLimiting;
            this.messageQueue = messageQueue;
            this.databaseMetrics = databaseMetrics;
            this.userActivity = userActivity;
        }
        
        @GetMapping("/basic")
        public String basicRequest() {
            basicCounter.handleRequest();
            return "Request counted";
        }
        
        @GetMapping("/http")
        public String httpRequest(@RequestParam String endpoint, @RequestParam String method) {
            httpMetrics.recordRequest(endpoint, method, 200);
            return "HTTP request recorded";
        }
        
        @PostMapping("/payment")
        public String processPayment(@RequestParam boolean success, @RequestParam(required = false) Double amount) {
            String paymentId = "PAY-" + System.currentTimeMillis();
            if (amount != null) {
                paymentService.processPaymentWithAmount(paymentId, amount, success);
            } else {
                paymentService.processPayment(paymentId, success);
            }
            return paymentId;
        }
        
        @GetMapping("/cache/hit")
        public String cacheHit(@RequestParam String key) {
            cacheMetrics.recordHit(key);
            return "Cache hit recorded";
        }
        
        @GetMapping("/cache/miss")
        public String cacheMiss(@RequestParam String key) {
            cacheMetrics.recordMiss(key);
            return "Cache miss recorded";
        }
        
        @PostMapping("/order")
        public String createOrder(@RequestParam String category, @RequestParam String region, @RequestParam double amount) {
            orderMetrics.recordOrder(category, region, amount);
            return "Order recorded";
        }
        
        @GetMapping("/rate-limit")
        public String checkRateLimit(@RequestParam String userId) {
            boolean allowed = rateLimiting.checkRateLimit(userId, 10);
            return allowed ? "Request allowed" : "Rate limit exceeded";
        }
        
        @PostMapping("/message/publish")
        public String publishMessage() {
            String messageId = "MSG-" + System.currentTimeMillis();
            messageQueue.publishMessage(messageId);
            return messageId;
        }
        
        @PostMapping("/db/query")
        public String recordQuery(@RequestParam String operation, @RequestParam String table) {
            databaseMetrics.recordQuery(operation, table, true);
            return "Query recorded";
        }
        
        @PostMapping("/user/login")
        public String recordLogin(@RequestParam String userType, @RequestParam boolean success) {
            userActivity.recordLogin(userType, success);
            return "Login recorded";
        }
    }
}
