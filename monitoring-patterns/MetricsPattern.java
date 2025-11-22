package com.example.monitoring.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Metrics Pattern - Demonstrates Spring Boot Actuator Metrics with Micrometer
 * 
 * This pattern shows how to:
 * 1. Create and use Counter metrics for counting events
 * 2. Create and use Timer metrics for measuring latency
 * 3. Create and use Gauge metrics for observable values
 * 4. Create and use DistributionSummary for tracking distributions
 * 5. Use custom tags for dimensional metrics
 * 6. Create custom MeterBinder for metric registration
 * 7. Customize MeterRegistry configuration
 * 8. Use LongTaskTimer for tracking ongoing operations
 * 9. Create multi-gauge for dynamic metric sets
 * 10. Export metrics to different backends
 * 
 * Key Concepts:
 * - Counter: Monotonically increasing counter
 * - Timer: Measures short-duration events and latency
 * - Gauge: Current value of a monitored item
 * - DistributionSummary: Distribution of events
 * - Tags: Dimensional data for metrics filtering
 * - MeterRegistry: Central registry for all metrics
 * 
 * Dependencies:
 * - spring-boot-starter-actuator
 * - micrometer-core
 * - micrometer-registry-prometheus (optional)
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Main Spring Boot application demonstrating metrics patterns
 */
@SpringBootApplication
@EnableScheduling
public class MetricsPattern {

    public static void main(String[] args) {
        SpringApplication.run(MetricsPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("METRICS PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        // Demonstrate basic metrics
        demonstrateCounterMetrics();
        demonstrateTimerMetrics();
        demonstrateGaugeMetrics();
        demonstrateDistributionSummaryMetrics();
        demonstrateLongTaskTimer();
        
        System.out.println("\nMetrics available at: http://localhost:8080/actuator/metrics");
        System.out.println("Prometheus metrics at: http://localhost:8080/actuator/prometheus");
        System.out.println("\nApplication is running. Press Ctrl+C to stop.");
    }
    
    private static void demonstrateCounterMetrics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COUNTER METRICS DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        MeterRegistry registry = new SimpleMeterRegistry();
        
        // Simple counter
        Counter requestCounter = Counter.builder("http.requests")
            .description("Total HTTP requests")
            .tag("method", "GET")
            .tag("status", "200")
            .register(registry);
        
        requestCounter.increment();
        requestCounter.increment(5);
        
        System.out.println("\nRequest Counter Value: " + requestCounter.count());
        
        // Counter with dynamic tags
        Counter errorCounter = registry.counter("http.errors", 
            "method", "POST", 
            "status", "500");
        errorCounter.increment();
        
        System.out.println("Error Counter Value: " + errorCounter.count());
        
        // Function counter
        AtomicInteger processedItems = new AtomicInteger(0);
        FunctionCounter.builder("items.processed", processedItems, AtomicInteger::get)
            .description("Items processed")
            .register(registry);
        
        processedItems.incrementAndGet();
        System.out.println("Processed Items: " + processedItems.get());
    }
    
    private static void demonstrateTimerMetrics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TIMER METRICS DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        MeterRegistry registry = new SimpleMeterRegistry();
        
        // Basic timer
        Timer requestTimer = Timer.builder("http.request.duration")
            .description("HTTP request duration")
            .tag("endpoint", "/api/users")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);
        
        // Time a task
        requestTimer.record(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        System.out.println("\nTimer Count: " + requestTimer.count());
        System.out.println("Total Time: " + requestTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) + " ms");
        System.out.println("Max Time: " + requestTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS) + " ms");
        System.out.println("Mean Time: " + requestTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS) + " ms");
        
        // Timer with sample
        Timer.Sample sample = Timer.start(registry);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        sample.stop(Timer.builder("operation.duration")
            .tag("type", "database")
            .register(registry));
    }
    
    private static void demonstrateGaugeMetrics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("GAUGE METRICS DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        MeterRegistry registry = new SimpleMeterRegistry();
        
        // Simple gauge tracking a collection size
        List<String> cache = new ArrayList<>();
        cache.add("item1");
        cache.add("item2");
        
        Gauge.builder("cache.size", cache, List::size)
            .description("Current cache size")
            .tag("type", "memory")
            .register(registry);
        
        System.out.println("\nCache Size Gauge: " + cache.size());
        
        // Gauge with custom supplier
        AtomicInteger queueSize = new AtomicInteger(42);
        Gauge.builder("queue.size", queueSize, AtomicInteger::get)
            .description("Current queue size")
            .register(registry);
        
        System.out.println("Queue Size Gauge: " + queueSize.get());
        
        // Time gauge for tracking time-based values
        TimeGauge.builder("process.uptime", () -> System.currentTimeMillis() - 1000000, 
            java.util.concurrent.TimeUnit.MILLISECONDS)
            .description("Process uptime")
            .register(registry);
    }
    
    private static void demonstrateDistributionSummaryMetrics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DISTRIBUTION SUMMARY METRICS DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        MeterRegistry registry = new SimpleMeterRegistry();
        
        // Distribution summary for tracking response sizes
        DistributionSummary responseSizeSummary = DistributionSummary.builder("http.response.size")
            .description("HTTP response size in bytes")
            .baseUnit("bytes")
            .publishPercentiles(0.5, 0.95, 0.99)
            .minimumExpectedValue(1.0)
            .maximumExpectedValue(100000.0)
            .register(registry);
        
        // Record some values
        responseSizeSummary.record(1024);
        responseSizeSummary.record(2048);
        responseSizeSummary.record(512);
        responseSizeSummary.record(4096);
        
        System.out.println("\nDistribution Summary Count: " + responseSizeSummary.count());
        System.out.println("Total Amount: " + responseSizeSummary.totalAmount());
        System.out.println("Max: " + responseSizeSummary.max());
        System.out.println("Mean: " + responseSizeSummary.mean());
    }
    
    private static void demonstrateLongTaskTimer() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("LONG TASK TIMER DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        MeterRegistry registry = new SimpleMeterRegistry();
        
        LongTaskTimer longTaskTimer = LongTaskTimer.builder("long.task")
            .description("Long running task")
            .register(registry);
        
        LongTaskTimer.Sample task = longTaskTimer.start();
        
        System.out.println("\nActive Tasks: " + longTaskTimer.activeTasks());
        System.out.println("Duration: " + longTaskTimer.duration(java.util.concurrent.TimeUnit.MILLISECONDS) + " ms");
        
        task.stop();
    }
}

/**
 * Configuration for metrics customization
 */
@Configuration
class MetricsConfig {
    
    /**
     * Customize meter registry with common tags
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "metrics-demo", "environment", "dev")
            .meterFilter(MeterFilter.maximumAllowableTags("http", "uri", 100, MeterFilter.deny()))
            .meterFilter(MeterFilter.denyNameStartsWith("jvm.gc.pause"));
    }
    
    /**
     * Custom distribution configuration
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configureDistributions() {
        return registry -> registry.config()
            .meterFilter(new MeterFilter() {
                @Override
                public DistributionStatisticConfig configure(Meter.Id id, 
                    DistributionStatisticConfig config) {
                    if (id.getName().startsWith("http")) {
                        return DistributionStatisticConfig.builder()
                            .percentiles(0.5, 0.95, 0.99)
                            .percentilesHistogram(true)
                            .build()
                            .merge(config);
                    }
                    return config;
                }
            });
    }
}

/**
 * Custom MeterBinder for business metrics
 */
@Component
class BusinessMetricsBinder implements MeterBinder {
    
    private final Map<String, AtomicInteger> businessCounters = new ConcurrentHashMap<>();
    
    public BusinessMetricsBinder() {
        businessCounters.put("orders", new AtomicInteger(0));
        businessCounters.put("customers", new AtomicInteger(0));
        businessCounters.put("products", new AtomicInteger(0));
    }
    
    @Override
    public void bindTo(MeterRegistry registry) {
        // Bind gauges for business metrics
        businessCounters.forEach((name, counter) -> 
            Gauge.builder("business." + name + ".count", counter, AtomicInteger::get)
                .description("Number of " + name)
                .tag("type", "business")
                .register(registry)
        );
        
        // Bind custom counter
        Counter.builder("business.transactions")
            .description("Total business transactions")
            .tag("type", "sales")
            .register(registry);
    }
    
    public void incrementOrder() {
        businessCounters.get("orders").incrementAndGet();
    }
    
    public void incrementCustomer() {
        businessCounters.get("customers").incrementAndGet();
    }
}

/**
 * Service demonstrating counter metrics
 */
@Service
class OrderService {
    
    private final Counter orderCounter;
    private final Counter successCounter;
    private final Counter failureCounter;
    private final MeterRegistry registry;
    
    public OrderService(MeterRegistry registry) {
        this.registry = registry;
        this.orderCounter = Counter.builder("orders.total")
            .description("Total orders received")
            .register(registry);
        
        this.successCounter = Counter.builder("orders.processed")
            .tag("status", "success")
            .register(registry);
        
        this.failureCounter = Counter.builder("orders.processed")
            .tag("status", "failure")
            .register(registry);
    }
    
    public void processOrder(String orderId) {
        orderCounter.increment();
        
        // Simulate processing
        boolean success = ThreadLocalRandom.current().nextBoolean();
        
        if (success) {
            successCounter.increment();
        } else {
            failureCounter.increment();
        }
        
        // Record with dynamic tags
        registry.counter("orders.by.type", 
            "type", orderId.startsWith("PREMIUM") ? "premium" : "standard",
            "result", success ? "success" : "failure")
            .increment();
    }
}

/**
 * Service demonstrating timer metrics
 */
@Service
class PaymentService {
    
    private final Timer paymentTimer;
    private final Timer databaseTimer;
    private final MeterRegistry registry;
    
    public PaymentService(MeterRegistry registry) {
        this.registry = registry;
        this.paymentTimer = Timer.builder("payment.processing.duration")
            .description("Payment processing time")
            .publishPercentiles(0.5, 0.95, 0.99)
            .publishPercentileHistogram()
            .sla(Duration.ofMillis(100), Duration.ofMillis(500), Duration.ofMillis(1000))
            .minimumExpectedValue(Duration.ofMillis(10))
            .maximumExpectedValue(Duration.ofSeconds(5))
            .register(registry);
        
        this.databaseTimer = Timer.builder("database.query.duration")
            .description("Database query time")
            .publishPercentiles(0.5, 0.95)
            .register(registry);
    }
    
    public void processPayment(double amount) {
        paymentTimer.record(() -> {
            // Simulate payment processing
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(50, 200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Using Timer.Sample for more control
        Timer.Sample sample = Timer.start(registry);
        try {
            // Simulate database query
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        sample.stop(databaseTimer);
    }
    
    public void processPaymentWithTags(double amount, String method) {
        Timer.builder("payment.method.duration")
            .tag("method", method)
            .tag("range", getAmountRange(amount))
            .register(registry)
            .record(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
    }
    
    private String getAmountRange(double amount) {
        if (amount < 100) return "small";
        if (amount < 1000) return "medium";
        return "large";
    }
}

/**
 * Service demonstrating gauge metrics
 */
@Service
class CacheMonitorService {
    
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final Queue<String> pendingQueue = new LinkedList<>();
    
    public CacheMonitorService(MeterRegistry registry) {
        // Gauge for cache size
        Gauge.builder("cache.entries", cache, Map::size)
            .description("Number of entries in cache")
            .tag("type", "memory")
            .register(registry);
        
        // Gauge for active connections
        Gauge.builder("connections.active", activeConnections, AtomicInteger::get)
            .description("Active database connections")
            .register(registry);
        
        // Gauge for queue size
        Gauge.builder("queue.pending", pendingQueue, Queue::size)
            .description("Pending items in queue")
            .register(registry);
        
        // Multi-gauge for cache stats
        MultiGauge cacheStats = MultiGauge.builder("cache.stats")
            .description("Cache statistics")
            .register(registry);
        
        // Update cache stats periodically
        cacheStats.register(
            Arrays.asList(
                MultiGauge.Row.of(Tags.of("stat", "size"), cache.size()),
                MultiGauge.Row.of(Tags.of("stat", "capacity"), 1000),
                MultiGauge.Row.of(Tags.of("stat", "hits"), 0),
                MultiGauge.Row.of(Tags.of("stat", "misses"), 0)
            ),
            true
        );
    }
    
    public void addToCache(String key, Object value) {
        cache.put(key, value);
    }
    
    public void removeFromCache(String key) {
        cache.remove(key);
    }
    
    public void openConnection() {
        activeConnections.incrementAndGet();
    }
    
    public void closeConnection() {
        activeConnections.decrementAndGet();
    }
    
    public void addToPendingQueue(String item) {
        pendingQueue.offer(item);
    }
    
    public String pollFromQueue() {
        return pendingQueue.poll();
    }
}

/**
 * Service demonstrating distribution summary metrics
 */
@Service
class RequestAnalyticsService {
    
    private final DistributionSummary responseSizeSummary;
    private final DistributionSummary requestPayloadSummary;
    
    public RequestAnalyticsService(MeterRegistry registry) {
        this.responseSizeSummary = DistributionSummary.builder("http.response.bytes")
            .description("HTTP response size distribution")
            .baseUnit("bytes")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .publishPercentileHistogram()
            .minimumExpectedValue(1.0)
            .maximumExpectedValue(1_000_000.0)
            .serviceLevelObjectives(1024.0, 10240.0, 102400.0)
            .register(registry);
        
        this.requestPayloadSummary = DistributionSummary.builder("http.request.payload")
            .description("HTTP request payload size")
            .baseUnit("bytes")
            .tag("type", "json")
            .publishPercentiles(0.5, 0.95)
            .register(registry);
    }
    
    public void recordResponse(int sizeInBytes) {
        responseSizeSummary.record(sizeInBytes);
    }
    
    public void recordRequest(int sizeInBytes, String contentType) {
        DistributionSummary.builder("request.payload.size")
            .tag("content-type", contentType)
            .register(new SimpleMeterRegistry())
            .record(sizeInBytes);
    }
}

/**
 * Service demonstrating long task timer
 */
@Service
class BatchProcessorService {
    
    private final LongTaskTimer batchProcessingTimer;
    private final MeterRegistry registry;
    
    public BatchProcessorService(MeterRegistry registry) {
        this.registry = registry;
        this.batchProcessingTimer = LongTaskTimer.builder("batch.processing")
            .description("Batch processing tasks")
            .tag("type", "data-import")
            .register(registry);
    }
    
    public void processBatch(List<String> items) {
        LongTaskTimer.Sample task = batchProcessingTimer.start();
        
        try {
            // Simulate batch processing
            for (String item : items) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            task.stop();
        }
    }
    
    public void processLongRunningTask() {
        LongTaskTimer taskTimer = LongTaskTimer.builder("long.running.task")
            .tag("operation", "data-migration")
            .register(registry);
        
        LongTaskTimer.Sample task = taskTimer.start();
        
        try {
            // Simulate long task
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            task.stop();
        }
    }
}

/**
 * REST Controller demonstrating metrics in action
 */
@RestController
@RequestMapping("/api/metrics-demo")
class MetricsController {
    
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CacheMonitorService cacheMonitorService;
    private final RequestAnalyticsService requestAnalyticsService;
    private final MeterRegistry registry;
    
    public MetricsController(OrderService orderService, 
                           PaymentService paymentService,
                           CacheMonitorService cacheMonitorService,
                           RequestAnalyticsService requestAnalyticsService,
                           MeterRegistry registry) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.cacheMonitorService = cacheMonitorService;
        this.requestAnalyticsService = requestAnalyticsService;
        this.registry = registry;
    }
    
    @PostMapping("/order")
    public String createOrder(@RequestParam String orderId) {
        Timer.Sample sample = Timer.start(registry);
        
        try {
            orderService.processOrder(orderId);
            requestAnalyticsService.recordResponse(256);
            return "Order processed: " + orderId;
        } finally {
            sample.stop(Timer.builder("api.order.duration")
                .tag("endpoint", "/order")
                .register(registry));
        }
    }
    
    @PostMapping("/payment")
    public String processPayment(@RequestParam double amount, 
                                @RequestParam(defaultValue = "card") String method) {
        paymentService.processPaymentWithTags(amount, method);
        return "Payment processed: $" + amount;
    }
    
    @GetMapping("/cache/{key}")
    public String getCacheValue(@PathVariable String key) {
        cacheMonitorService.addToCache(key, "value-" + key);
        return "Cached: " + key;
    }
}

/**
 * Scheduled task to generate sample metrics
 */
@Component
class MetricsGenerator {
    
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CacheMonitorService cacheMonitorService;
    
    public MetricsGenerator(OrderService orderService, 
                          PaymentService paymentService,
                          CacheMonitorService cacheMonitorService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.cacheMonitorService = cacheMonitorService;
    }
    
    @Scheduled(fixedRate = 5000)
    public void generateOrderMetrics() {
        String orderId = "ORD-" + System.currentTimeMillis();
        orderService.processOrder(orderId);
    }
    
    @Scheduled(fixedRate = 3000)
    public void generatePaymentMetrics() {
        double amount = ThreadLocalRandom.current().nextDouble(10, 1000);
        paymentService.processPayment(amount);
    }
    
    @Scheduled(fixedRate = 2000)
    public void updateCacheMetrics() {
        cacheMonitorService.openConnection();
        cacheMonitorService.addToCache("key-" + System.currentTimeMillis(), "value");
        
        if (ThreadLocalRandom.current().nextBoolean()) {
            cacheMonitorService.closeConnection();
        }
    }
}
