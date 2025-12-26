package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Histogram Pattern - Statistical Distribution with Buckets
 * 
 * Purpose:
 * - Track statistical distributions with server-side buckets
 * - Calculate percentiles on the backend
 * - Aggregate histogram data across instances
 * - Support cumulative histograms
 * - Monitor value distributions with pre-defined buckets
 * 
 * Use Cases:
 * - Request latency distributions
 * - Response size distributions
 * - Query execution time distributions
 * - Transaction amount distributions
 * - Resource utilization histograms
 * - SLA bucket monitoring
 * - Performance threshold tracking
 * 
 * vs Client-side Percentiles:
 * - Histogram: Server calculates percentiles, aggregatable
 * - Client Percentiles: Client calculates, not aggregatable
 * 
 * Histogram Types:
 * - Cumulative: Buckets are cumulative (le - less than or equal)
 * - Non-cumulative: Each bucket is independent
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     distribution:
 *       percentiles-histogram:
 *         http.server.requests: true
 *       slo:
 *         http.server.requests: 10ms,50ms,100ms,200ms,500ms,1s,2s,5s
 *       minimum-expected-value:
 *         http.server.requests: 1ms
 *       maximum-expected-value:
 *         http.server.requests: 10s
 * 
 * Prometheus Format:
 * # TYPE http_server_requests_seconds histogram
 * http_server_requests_seconds_bucket{le="0.01"} 100
 * http_server_requests_seconds_bucket{le="0.05"} 250
 * http_server_requests_seconds_bucket{le="0.1"} 450
 * http_server_requests_seconds_bucket{le="+Inf"} 500
 * http_server_requests_seconds_count 500
 * http_server_requests_seconds_sum 45.5
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
 * Warnings:
 * - Too many buckets increase memory usage
 * - Choose bucket boundaries carefully
 * - Align buckets with SLAs/SLOs
 * - Histograms are approximations
 * - Cannot calculate exact percentiles
 * - More accurate than client-side percentiles for aggregation
 * 
 * Best Practices:
 * - Define SLO-aligned buckets
 * - Use power-of-2 or power-of-10 bucket boundaries
 * - Set min/max expected values
 * - Monitor bucket distribution
 * - Adjust buckets based on actual data
 * - Use histograms for aggregatable metrics
 * - Combine with summary metrics (count, sum, mean)
 */
@SpringBootApplication
public class HistogramPattern {

    public static void main(String[] args) {
        SpringApplication.run(HistogramPattern.class, args);
    }

    // ============================================
    // Example 1: HTTP Request Latency Histogram
    // ============================================
    
    @Service
    public static class HttpLatencyHistogramService {
        
        private final Timer requestTimer;
        private final Map<String, Timer> endpointTimers = new ConcurrentHashMap<>();
        private final MeterRegistry registry;
        
        public HttpLatencyHistogramService(MeterRegistry registry) {
            this.registry = registry;
            
            // Timer with histogram buckets (SLO-based)
            this.requestTimer = Timer.builder("http.request.latency")
                .description("HTTP request latency histogram")
                .tags("service", "api")
                .serviceLevelObjectives(
                    Duration.ofMillis(10),    // 10ms - very fast
                    Duration.ofMillis(50),    // 50ms - fast
                    Duration.ofMillis(100),   // 100ms - acceptable
                    Duration.ofMillis(200),   // 200ms - slow
                    Duration.ofMillis(500),   // 500ms - very slow
                    Duration.ofSeconds(1),    // 1s - timeout warning
                    Duration.ofSeconds(2),    // 2s - near timeout
                    Duration.ofSeconds(5)     // 5s - timeout
                )
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(10))
                .publishPercentileHistogram()  // Enable histogram publishing
                .register(registry);
        }
        
        public String processRequest(String endpoint, String method) {
            return requestTimer.record(() -> {
                // Simulate varying latencies
                simulateLatency(endpoint);
                return "Processed: " + method + " " + endpoint;
            });
        }
        
        public String processEndpointRequest(String endpoint, int latencyMs) {
            Timer timer = endpointTimers.computeIfAbsent(endpoint, ep ->
                Timer.builder("http.request.latency.endpoint")
                    .description("Request latency per endpoint")
                    .tag("endpoint", ep)
                    .serviceLevelObjectives(
                        Duration.ofMillis(10),
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(500)
                    )
                    .publishPercentileHistogram()
                    .register(registry)
            );
            
            return timer.record(() -> {
                simulateLatency(latencyMs);
                return "Endpoint response: " + endpoint;
            });
        }
        
        private void simulateLatency(String endpoint) {
            try {
                // Vary latency based on endpoint
                if (endpoint.contains("fast")) {
                    Thread.sleep(20);
                } else if (endpoint.contains("slow")) {
                    Thread.sleep(300);
                } else {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void simulateLatency(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public Map<String, Object> getHistogramStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("count", requestTimer.count());
            stats.put("total_time_ms", requestTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("mean_ms", requestTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("max_ms", requestTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
            return stats;
        }
    }

    // ============================================
    // Example 2: Database Query Time Histogram
    // ============================================
    
    @Service
    public static class DatabaseQueryHistogramService {
        
        private final Timer selectTimer;
        private final Timer insertTimer;
        private final Timer updateTimer;
        private final Timer deleteTimer;
        
        public DatabaseQueryHistogramService(MeterRegistry registry) {
            // SELECT queries (typically fast)
            this.selectTimer = Timer.builder("db.query.duration")
                .description("Database query duration")
                .tags("operation", "SELECT")
                .serviceLevelObjectives(
                    Duration.ofMillis(5),
                    Duration.ofMillis(10),
                    Duration.ofMillis(25),
                    Duration.ofMillis(50),
                    Duration.ofMillis(100)
                )
                .publishPercentileHistogram()
                .register(registry);
            
            // INSERT queries
            this.insertTimer = Timer.builder("db.query.duration")
                .description("Database insert duration")
                .tags("operation", "INSERT")
                .serviceLevelObjectives(
                    Duration.ofMillis(10),
                    Duration.ofMillis(50),
                    Duration.ofMillis(100),
                    Duration.ofMillis(500)
                )
                .publishPercentileHistogram()
                .register(registry);
            
            // UPDATE queries
            this.updateTimer = Timer.builder("db.query.duration")
                .description("Database update duration")
                .tags("operation", "UPDATE")
                .serviceLevelObjectives(
                    Duration.ofMillis(20),
                    Duration.ofMillis(100),
                    Duration.ofMillis(500),
                    Duration.ofSeconds(1)
                )
                .publishPercentileHistogram()
                .register(registry);
            
            // DELETE queries
            this.deleteTimer = Timer.builder("db.query.duration")
                .description("Database delete duration")
                .tags("operation", "DELETE")
                .serviceLevelObjectives(
                    Duration.ofMillis(10),
                    Duration.ofMillis(50),
                    Duration.ofMillis(200),
                    Duration.ofSeconds(1)
                )
                .publishPercentileHistogram()
                .register(registry);
        }
        
        public List<Map<String, Object>> executeSelect(String query) {
            return selectTimer.record(() -> {
                simulateQuery(30);
                return Arrays.asList(
                    Map.of("id", 1, "name", "Record 1"),
                    Map.of("id", 2, "name", "Record 2")
                );
            });
        }
        
        public int executeInsert(String table, Map<String, Object> data) {
            return insertTimer.record(() -> {
                simulateQuery(50);
                return 1; // rows affected
            });
        }
        
        public int executeUpdate(String table, Map<String, Object> data, String condition) {
            return updateTimer.record(() -> {
                simulateQuery(100);
                return 5; // rows affected
            });
        }
        
        public int executeDelete(String table, String condition) {
            return deleteTimer.record(() -> {
                simulateQuery(40);
                return 3; // rows affected
            });
        }
        
        private void simulateQuery(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public Map<String, Object> getQueryStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("select_count", selectTimer.count());
            stats.put("select_mean_ms", selectTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("insert_count", insertTimer.count());
            stats.put("insert_mean_ms", insertTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("update_count", updateTimer.count());
            stats.put("update_mean_ms", updateTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("delete_count", deleteTimer.count());
            stats.put("delete_mean_ms", deleteTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            return stats;
        }
    }

    // ============================================
    // Example 3: Response Size Histogram
    // ============================================
    
    @Service
    public static class ResponseSizeHistogramService {
        
        private final MeterRegistry registry;
        private final Map<String, Timer> responseSizeTimers = new ConcurrentHashMap<>();
        
        public ResponseSizeHistogramService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public byte[] generateResponse(String contentType, int sizeCategory) {
            Timer timer = responseSizeTimers.computeIfAbsent(contentType, ct ->
                Timer.builder("response.generation.time")
                    .description("Response generation time by content type")
                    .tag("content_type", ct)
                    .serviceLevelObjectives(
                        Duration.ofMillis(10),
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(500)
                    )
                    .publishPercentileHistogram()
                    .register(registry)
            );
            
            return timer.record(() -> {
                // Simulate response generation time based on size
                try {
                    Thread.sleep(sizeCategory * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new byte[sizeCategory * 1024]; // KB
            });
        }
    }

    // ============================================
    // Example 4: Transaction Amount Histogram
    // ============================================
    
    @Service
    public static class TransactionHistogramService {
        
        private final Timer transactionTimer;
        private final Map<String, Timer> currencyTimers = new ConcurrentHashMap<>();
        private final MeterRegistry registry;
        
        public TransactionHistogramService(MeterRegistry registry) {
            this.registry = registry;
            
            // Transaction processing time histogram
            this.transactionTimer = Timer.builder("transaction.processing.time")
                .description("Transaction processing time histogram")
                .tags("type", "payment")
                .serviceLevelObjectives(
                    Duration.ofMillis(50),
                    Duration.ofMillis(100),
                    Duration.ofMillis(200),
                    Duration.ofMillis(500),
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(2)
                )
                .minimumExpectedValue(Duration.ofMillis(10))
                .maximumExpectedValue(Duration.ofSeconds(5))
                .publishPercentileHistogram()
                .register(registry);
        }
        
        public String processTransaction(String transactionId, double amount, String currency) {
            Timer timer = currencyTimers.computeIfAbsent(currency, c ->
                Timer.builder("transaction.processing.time.currency")
                    .description("Transaction time per currency")
                    .tag("currency", c)
                    .serviceLevelObjectives(
                        Duration.ofMillis(100),
                        Duration.ofMillis(500),
                        Duration.ofSeconds(1)
                    )
                    .publishPercentileHistogram()
                    .register(registry)
            );
            
            return timer.record(() -> {
                // Simulate payment gateway latency
                simulatePaymentGateway(amount);
                return "Transaction processed: " + transactionId;
            });
        }
        
        private void simulatePaymentGateway(double amount) {
            try {
                // Larger amounts take longer (fraud checks, etc.)
                int latency = amount > 1000 ? 300 : amount > 100 ? 150 : 50;
                Thread.sleep(latency);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public Map<String, Object> getTransactionStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("transaction_count", transactionTimer.count());
            stats.put("mean_ms", transactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("max_ms", transactionTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
            return stats;
        }
    }

    // ============================================
    // Example 5: Resource Utilization Histogram
    // ============================================
    
    @Service
    public static class ResourceUtilizationHistogramService {
        
        private final Timer cpuIntensiveTimer;
        private final Timer memoryIntensiveTimer;
        private final Timer ioIntensiveTimer;
        
        public ResourceUtilizationHistogramService(MeterRegistry registry) {
            // CPU-intensive operations
            this.cpuIntensiveTimer = Timer.builder("operation.duration")
                .description("CPU-intensive operation duration")
                .tags("type", "cpu")
                .serviceLevelObjectives(
                    Duration.ofMillis(100),
                    Duration.ofMillis(500),
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(5)
                )
                .publishPercentileHistogram()
                .register(registry);
            
            // Memory-intensive operations
            this.memoryIntensiveTimer = Timer.builder("operation.duration")
                .description("Memory-intensive operation duration")
                .tags("type", "memory")
                .serviceLevelObjectives(
                    Duration.ofMillis(50),
                    Duration.ofMillis(200),
                    Duration.ofMillis(500),
                    Duration.ofSeconds(2)
                )
                .publishPercentileHistogram()
                .register(registry);
            
            // I/O-intensive operations
            this.ioIntensiveTimer = Timer.builder("operation.duration")
                .description("I/O-intensive operation duration")
                .tags("type", "io")
                .serviceLevelObjectives(
                    Duration.ofMillis(200),
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(5),
                    Duration.ofSeconds(10)
                )
                .publishPercentileHistogram()
                .register(registry);
        }
        
        public String performCpuIntensiveTask(int complexity) {
            return cpuIntensiveTimer.record(() -> {
                // Simulate CPU work
                try {
                    Thread.sleep(complexity * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "CPU task completed";
            });
        }
        
        public String performMemoryIntensiveTask(int sizeMB) {
            return memoryIntensiveTimer.record(() -> {
                // Simulate memory allocation
                try {
                    Thread.sleep(sizeMB * 5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Memory task completed";
            });
        }
        
        public String performIoIntensiveTask(int ioOps) {
            return ioIntensiveTimer.record(() -> {
                // Simulate I/O operations
                try {
                    Thread.sleep(ioOps * 20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "I/O task completed";
            });
        }
    }

    // ============================================
    // Example 6: SLA Bucket Monitoring
    // ============================================
    
    @Service
    public static class SLAMonitoringService {
        
        private final Timer slaTimer;
        private long totalRequests = 0;
        private long slaViolations = 0;
        private final Duration slaThreshold = Duration.ofMillis(200);
        
        public SLAMonitoringService(MeterRegistry registry) {
            // SLA-focused histogram with specific buckets
            this.slaTimer = Timer.builder("api.sla.latency")
                .description("API latency for SLA monitoring")
                .tags("sla", "200ms")
                .serviceLevelObjectives(
                    Duration.ofMillis(50),    // 25% of SLA
                    Duration.ofMillis(100),   // 50% of SLA
                    Duration.ofMillis(150),   // 75% of SLA
                    Duration.ofMillis(200),   // 100% of SLA (threshold)
                    Duration.ofMillis(300),   // 150% of SLA (warning)
                    Duration.ofMillis(500)    // 250% of SLA (critical)
                )
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(2))
                .publishPercentileHistogram()
                .register(registry);
        }
        
        public String processWithSLA(String requestId, int expectedLatencyMs) {
            Timer.Sample sample = Timer.start();
            
            try {
                Thread.sleep(expectedLatencyMs);
                return "Request processed: " + requestId;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Request interrupted: " + requestId;
            } finally {
                Duration duration = Duration.ofNanos(sample.stop(slaTimer));
                totalRequests++;
                
                if (duration.compareTo(slaThreshold) > 0) {
                    slaViolations++;
                    System.out.println("SLA VIOLATION: " + requestId + 
                        " took " + duration.toMillis() + "ms (SLA: " + 
                        slaThreshold.toMillis() + "ms)");
                }
            }
        }
        
        public Map<String, Object> getSLAStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total_requests", totalRequests);
            stats.put("sla_violations", slaViolations);
            stats.put("sla_compliance_percent", 
                totalRequests > 0 ? (double)(totalRequests - slaViolations) / totalRequests * 100 : 100.0);
            stats.put("sla_threshold_ms", slaThreshold.toMillis());
            stats.put("mean_latency_ms", slaTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("max_latency_ms", slaTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
            return stats;
        }
    }

    // ============================================
    // Example 7: Histogram REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/histogram")
    public static class HistogramController {
        
        private final HttpLatencyHistogramService httpService;
        private final DatabaseQueryHistogramService dbService;
        private final TransactionHistogramService transactionService;
        private final ResourceUtilizationHistogramService resourceService;
        private final SLAMonitoringService slaService;
        
        public HistogramController(
                HttpLatencyHistogramService httpService,
                DatabaseQueryHistogramService dbService,
                TransactionHistogramService transactionService,
                ResourceUtilizationHistogramService resourceService,
                SLAMonitoringService slaService) {
            this.httpService = httpService;
            this.dbService = dbService;
            this.transactionService = transactionService;
            this.resourceService = resourceService;
            this.slaService = slaService;
        }
        
        @GetMapping("/request/{endpoint}")
        public String processRequest(
                @PathVariable String endpoint,
                @RequestParam(defaultValue = "GET") String method) {
            return httpService.processRequest(endpoint, method);
        }
        
        @GetMapping("/request/latency/{endpoint}")
        public String processWithLatency(
                @PathVariable String endpoint,
                @RequestParam int latencyMs) {
            return httpService.processEndpointRequest(endpoint, latencyMs);
        }
        
        @GetMapping("/http/stats")
        public Map<String, Object> getHttpStats() {
            return httpService.getHistogramStats();
        }
        
        @GetMapping("/db/select")
        public List<Map<String, Object>> dbSelect(@RequestParam String query) {
            return dbService.executeSelect(query);
        }
        
        @PostMapping("/db/insert")
        public int dbInsert(
                @RequestParam String table,
                @RequestBody Map<String, Object> data) {
            return dbService.executeInsert(table, data);
        }
        
        @GetMapping("/db/stats")
        public Map<String, Object> getDbStats() {
            return dbService.getQueryStats();
        }
        
        @PostMapping("/transaction")
        public String processTransaction(
                @RequestParam String transactionId,
                @RequestParam double amount,
                @RequestParam(defaultValue = "USD") String currency) {
            return transactionService.processTransaction(transactionId, amount, currency);
        }
        
        @GetMapping("/transaction/stats")
        public Map<String, Object> getTransactionStats() {
            return transactionService.getTransactionStats();
        }
        
        @PostMapping("/resource/cpu")
        public String cpuTask(@RequestParam(defaultValue = "10") int complexity) {
            return resourceService.performCpuIntensiveTask(complexity);
        }
        
        @PostMapping("/resource/memory")
        public String memoryTask(@RequestParam(defaultValue = "10") int sizeMB) {
            return resourceService.performMemoryIntensiveTask(sizeMB);
        }
        
        @PostMapping("/resource/io")
        public String ioTask(@RequestParam(defaultValue = "5") int ioOps) {
            return resourceService.performIoIntensiveTask(ioOps);
        }
        
        @GetMapping("/sla/process")
        public String processWithSLA(
                @RequestParam String requestId,
                @RequestParam(defaultValue = "100") int expectedLatencyMs) {
            return slaService.processWithSLA(requestId, expectedLatencyMs);
        }
        
        @GetMapping("/sla/stats")
        public Map<String, Object> getSLAStats() {
            return slaService.getSLAStats();
        }
    }
}
