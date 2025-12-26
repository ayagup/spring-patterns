package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Percentile Pattern - Client-Side Percentile Calculations
 * 
 * Purpose:
 * - Calculate percentiles on the client side (P50, P95, P99, P99.9)
 * - Monitor SLA compliance
 * - Track performance distributions
 * - Identify outliers and slow requests
 * - Capacity planning based on percentiles
 * 
 * Use Cases:
 * - API latency monitoring (P95, P99)
 * - Database query performance
 * - Message processing times
 * - Service response times
 * - SLA/SLO tracking
 * - Performance regression detection
 * - Capacity planning
 * - User experience monitoring
 * 
 * Common Percentiles:
 * - P50 (median): Middle value, typical request
 * - P90: 90% of requests faster than this
 * - P95: 95% of requests faster (common SLA)
 * - P99: 99% of requests faster (tail latency)
 * - P99.9: 99.9% of requests faster (extreme outliers)
 * 
 * vs Histogram:
 * - Percentiles: Client calculates, cannot aggregate across instances
 * - Histogram: Server calculates, aggregatable
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     distribution:
 *       percentiles:
 *         http.server.requests: 0.5,0.9,0.95,0.99,0.999
 *       slo:
 *         http.server.requests: 10ms,50ms,100ms,200ms,500ms,1s
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
 * 
 * Warnings:
 * - Percentiles are approximations (HdrHistogram)
 * - Cannot aggregate percentiles across instances
 * - Memory overhead for maintaining distribution
 * - Accuracy vs memory tradeoff (precision)
 * - Rotates data periodically
 * - Not suitable for cross-instance aggregation
 * 
 * Best Practices:
 * - Use P95/P99 for SLA monitoring
 * - Monitor P50 for typical experience
 * - Track P99.9 for worst-case scenarios
 * - Set SLO boundaries aligned with business needs
 * - Combine with histogram for aggregation
 * - Alert on P95/P99 degradation
 * - Use percentiles for capacity planning
 * - Document SLA targets
 */
@SpringBootApplication
public class PercentilePattern {

    public static void main(String[] args) {
        SpringApplication.run(PercentilePattern.class, args);
    }

    // ============================================
    // Example 1: API Latency Percentiles
    // ============================================
    
    @Service
    public static class ApiPercentileService {
        
        private final Timer apiTimer;
        private final Map<String, Timer> endpointTimers = new ConcurrentHashMap<>();
        private final MeterRegistry registry;
        
        public ApiPercentileService(MeterRegistry registry) {
            this.registry = registry;
            
            // API request timer with percentiles
            this.apiTimer = Timer.builder("api.request.duration")
                .description("API request duration with percentiles")
                .tags("service", "api")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99, 0.999)  // P50, P90, P95, P99, P99.9
                .percentilePrecision(2)  // 2 significant digits
                .serviceLevelObjectives(
                    Duration.ofMillis(50),
                    Duration.ofMillis(100),
                    Duration.ofMillis(200),
                    Duration.ofMillis(500)
                )
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(10))
                .register(registry);
        }
        
        public String processApiRequest(String endpoint, int simulatedLatency) {
            return apiTimer.record(() -> {
                simulateWork(simulatedLatency);
                return "Processed: " + endpoint;
            });
        }
        
        public String processEndpointWithPercentiles(String endpoint, int latencyMs) {
            Timer timer = endpointTimers.computeIfAbsent(endpoint, ep ->
                Timer.builder("api.request.duration.endpoint")
                    .description("Request duration per endpoint")
                    .tag("endpoint", ep)
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .serviceLevelObjectives(
                        Duration.ofMillis(100),
                        Duration.ofMillis(500),
                        Duration.ofSeconds(1)
                    )
                    .register(registry)
            );
            
            return timer.record(() -> {
                simulateWork(latencyMs);
                return "Endpoint response: " + endpoint;
            });
        }
        
        private void simulateWork(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public Map<String, Object> getPercentileStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("count", apiTimer.count());
            stats.put("mean_ms", apiTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("max_ms", apiTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
            
            // Note: Actual percentile values are exported via /actuator/metrics
            // and /actuator/prometheus endpoints
            stats.put("note", "Percentile values available at /actuator/metrics/api.request.duration");
            
            return stats;
        }
    }

    // ============================================
    // Example 2: Database Query Percentiles
    // ============================================
    
    @Service
    public static class DatabasePercentileService {
        
        private final Timer queryTimer;
        private final Map<String, Timer> tableTimers = new ConcurrentHashMap<>();
        private final MeterRegistry registry;
        
        public DatabasePercentileService(MeterRegistry registry) {
            this.registry = registry;
            
            // Database query timer with percentiles
            this.queryTimer = Timer.builder("db.query.duration")
                .description("Database query duration with percentiles")
                .tags("database", "postgres")
                .publishPercentiles(0.5, 0.95, 0.99, 0.999)
                .serviceLevelObjectives(
                    Duration.ofMillis(10),    // Very fast
                    Duration.ofMillis(50),    // Fast
                    Duration.ofMillis(100),   // Acceptable
                    Duration.ofMillis(500),   // Slow
                    Duration.ofSeconds(1)     // Very slow
                )
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(30))
                .register(registry);
        }
        
        public List<Map<String, Object>> executeQuery(String table, String query, int expectedRows) {
            Timer timer = tableTimers.computeIfAbsent(table, t ->
                Timer.builder("db.query.duration.table")
                    .description("Query duration per table")
                    .tag("table", t)
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .serviceLevelObjectives(
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(500)
                    )
                    .register(registry)
            );
            
            return timer.record(() -> {
                // Simulate query execution time based on result size
                int latency = Math.min(10 + expectedRows / 10, 500);
                simulateQuery(latency);
                
                List<Map<String, Object>> results = new ArrayList<>();
                for (int i = 0; i < expectedRows; i++) {
                    results.add(Map.of("id", i, "data", "row" + i));
                }
                return results;
            });
        }
        
        private void simulateQuery(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public Map<String, Object> getQueryPercentiles() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("query_count", queryTimer.count());
            stats.put("mean_ms", queryTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("max_ms", queryTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("percentiles_location", "/actuator/metrics/db.query.duration");
            return stats;
        }
    }

    // ============================================
    // Example 3: Service Call Percentiles
    // ============================================
    
    @Service
    public static class ServiceCallPercentileService {
        
        private final Timer externalServiceTimer;
        private final Timer internalServiceTimer;
        private final Map<String, Timer> serviceTimers = new ConcurrentHashMap<>();
        private final MeterRegistry registry;
        
        public ServiceCallPercentileService(MeterRegistry registry) {
            this.registry = registry;
            
            // External service calls (higher latency, more variance)
            this.externalServiceTimer = Timer.builder("service.call.duration")
                .description("External service call duration")
                .tags("type", "external")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99, 0.999)
                .serviceLevelObjectives(
                    Duration.ofMillis(100),
                    Duration.ofMillis(500),
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(2),
                    Duration.ofSeconds(5)
                )
                .minimumExpectedValue(Duration.ofMillis(10))
                .maximumExpectedValue(Duration.ofSeconds(30))
                .register(registry);
            
            // Internal service calls (lower latency, less variance)
            this.internalServiceTimer = Timer.builder("service.call.duration")
                .description("Internal service call duration")
                .tags("type", "internal")
                .publishPercentiles(0.5, 0.95, 0.99)
                .serviceLevelObjectives(
                    Duration.ofMillis(10),
                    Duration.ofMillis(50),
                    Duration.ofMillis(100),
                    Duration.ofMillis(200)
                )
                .register(registry);
        }
        
        public String callExternalService(String serviceName, String endpoint) {
            Timer timer = serviceTimers.computeIfAbsent(serviceName, s ->
                Timer.builder("service.call.duration.service")
                    .description("Call duration per service")
                    .tag("service", s)
                    .tag("type", "external")
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(registry)
            );
            
            return timer.record(() -> {
                // Simulate external service latency with variance
                int baseLatency = 200;
                int variance = (int) (Math.random() * 300);
                simulateCall(baseLatency + variance);
                return "External response from " + serviceName;
            });
        }
        
        public String callInternalService(String serviceName, String endpoint) {
            return internalServiceTimer.record(() -> {
                // Simulate internal service latency (fast, consistent)
                int latency = 20 + (int) (Math.random() * 30);
                simulateCall(latency);
                return "Internal response from " + serviceName;
            });
        }
        
        private void simulateCall(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public Map<String, Object> getServiceCallStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("external_calls", externalServiceTimer.count());
            stats.put("external_mean_ms", externalServiceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("internal_calls", internalServiceTimer.count());
            stats.put("internal_mean_ms", internalServiceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            return stats;
        }
    }

    // ============================================
    // Example 4: Message Processing Percentiles
    // ============================================
    
    @Service
    public static class MessageProcessingPercentileService {
        
        private final Timer messageTimer;
        private final Map<String, Timer> queueTimers = new ConcurrentHashMap<>();
        private final MeterRegistry registry;
        
        public MessageProcessingPercentileService(MeterRegistry registry) {
            this.registry = registry;
            
            // Message processing timer
            this.messageTimer = Timer.builder("message.processing.duration")
                .description("Message processing duration")
                .tags("system", "messaging")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .serviceLevelObjectives(
                    Duration.ofMillis(50),
                    Duration.ofMillis(100),
                    Duration.ofMillis(500),
                    Duration.ofSeconds(1)
                )
                .register(registry);
        }
        
        public String processMessage(String queueName, String messageId, int payloadSize) {
            Timer timer = queueTimers.computeIfAbsent(queueName, q ->
                Timer.builder("message.processing.duration.queue")
                    .description("Processing duration per queue")
                    .tag("queue", q)
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(registry)
            );
            
            return timer.record(() -> {
                // Processing time varies with payload size
                int latency = Math.min(10 + payloadSize / 100, 1000);
                simulateProcessing(latency);
                return "Message processed: " + messageId;
            });
        }
        
        private void simulateProcessing(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public Map<String, Object> getMessageStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("messages_processed", messageTimer.count());
            stats.put("mean_ms", messageTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("max_ms", messageTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
            return stats;
        }
    }

    // ============================================
    // Example 5: SLA Compliance Monitoring
    // ============================================
    
    @Service
    public static class SLAComplianceService {
        
        private final Timer slaTimer;
        private final Duration slaTarget = Duration.ofMillis(200);  // SLA: 200ms
        private long totalRequests = 0;
        private long slaViolations = 0;
        
        public SLAComplianceService(MeterRegistry registry) {
            // Timer focused on SLA compliance
            this.slaTimer = Timer.builder("api.sla.compliance")
                .description("API latency for SLA compliance")
                .tags("sla_target", slaTarget.toMillis() + "ms")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99, 0.999)
                .serviceLevelObjectives(
                    slaTarget.dividedBy(4),      // 50ms (25% of SLA)
                    slaTarget.dividedBy(2),      // 100ms (50% of SLA)
                    slaTarget.multipliedBy(3).dividedBy(4),  // 150ms (75% of SLA)
                    slaTarget,                   // 200ms (100% of SLA)
                    slaTarget.multipliedBy(2)    // 400ms (200% of SLA - violation)
                )
                .register(registry);
        }
        
        public String processRequestWithSLA(String requestId, int expectedLatency) {
            Timer.Sample sample = Timer.start();
            
            try {
                simulateRequest(expectedLatency);
                return "Request processed: " + requestId;
            } finally {
                Duration duration = Duration.ofNanos(sample.stop(slaTimer));
                totalRequests++;
                
                if (duration.compareTo(slaTarget) > 0) {
                    slaViolations++;
                    System.out.println(String.format(
                        "SLA VIOLATION: Request %s took %dms (SLA: %dms, P95 target)",
                        requestId, duration.toMillis(), slaTarget.toMillis()));
                }
            }
        }
        
        private void simulateRequest(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public Map<String, Object> getSLACompliance() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total_requests", totalRequests);
            stats.put("sla_violations", slaViolations);
            stats.put("sla_target_ms", slaTarget.toMillis());
            
            double compliancePercent = totalRequests > 0 
                ? (double) (totalRequests - slaViolations) / totalRequests * 100 
                : 100.0;
            stats.put("sla_compliance_percent", compliancePercent);
            stats.put("mean_latency_ms", slaTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            stats.put("max_latency_ms", slaTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
            
            // SLA status
            if (compliancePercent >= 99.9) {
                stats.put("sla_status", "EXCELLENT");
            } else if (compliancePercent >= 99.0) {
                stats.put("sla_status", "GOOD");
            } else if (compliancePercent >= 95.0) {
                stats.put("sla_status", "WARNING");
            } else {
                stats.put("sla_status", "CRITICAL");
            }
            
            return stats;
        }
    }

    // ============================================
    // Example 6: Performance Regression Detection
    // ============================================
    
    @Service
    public static class PerformanceRegressionService {
        
        private final Timer performanceTimer;
        private final List<Double> recentP95Values = new ArrayList<>();
        private final int windowSize = 10;
        
        public PerformanceRegressionService(MeterRegistry registry) {
            this.performanceTimer = Timer.builder("performance.monitoring")
                .description("Performance monitoring with regression detection")
                .tags("monitoring", "regression")
                .publishPercentiles(0.5, 0.95, 0.99)
                .serviceLevelObjectives(
                    Duration.ofMillis(100),
                    Duration.ofMillis(200),
                    Duration.ofMillis(500)
                )
                .register(registry);
        }
        
        public String executeOperation(String operationName, int complexity) {
            return performanceTimer.record(() -> {
                // Simulate operation with varying complexity
                int latency = complexity * 10;
                simulateOperation(latency);
                return "Operation completed: " + operationName;
            });
        }
        
        private void simulateOperation(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public Map<String, Object> checkPerformanceRegression(double currentP95) {
            Map<String, Object> result = new HashMap<>();
            
            // Add current P95 to window
            recentP95Values.add(currentP95);
            if (recentP95Values.size() > windowSize) {
                recentP95Values.remove(0);
            }
            
            // Calculate baseline (average of recent P95 values)
            double baseline = recentP95Values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(currentP95);
            
            // Check for regression (> 20% increase)
            double threshold = baseline * 1.2;
            boolean regression = currentP95 > threshold;
            
            result.put("current_p95_ms", currentP95);
            result.put("baseline_p95_ms", baseline);
            result.put("threshold_ms", threshold);
            result.put("regression_detected", regression);
            
            if (regression) {
                double increase = ((currentP95 - baseline) / baseline) * 100;
                result.put("performance_degradation_percent", increase);
                result.put("alert", "Performance regression detected!");
            }
            
            return result;
        }
    }

    // ============================================
    // Example 7: Percentile REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/percentile")
    public static class PercentileController {
        
        private final ApiPercentileService apiService;
        private final DatabasePercentileService dbService;
        private final ServiceCallPercentileService serviceCallService;
        private final MessageProcessingPercentileService messageService;
        private final SLAComplianceService slaService;
        private final PerformanceRegressionService regressionService;
        
        public PercentileController(
                ApiPercentileService apiService,
                DatabasePercentileService dbService,
                ServiceCallPercentileService serviceCallService,
                MessageProcessingPercentileService messageService,
                SLAComplianceService slaService,
                PerformanceRegressionService regressionService) {
            this.apiService = apiService;
            this.dbService = dbService;
            this.serviceCallService = serviceCallService;
            this.messageService = messageService;
            this.slaService = slaService;
            this.regressionService = regressionService;
        }
        
        @GetMapping("/api/{endpoint}")
        public String processApi(
                @PathVariable String endpoint,
                @RequestParam(defaultValue = "100") int latencyMs) {
            return apiService.processApiRequest(endpoint, latencyMs);
        }
        
        @GetMapping("/api/endpoint/{endpoint}")
        public String processEndpoint(
                @PathVariable String endpoint,
                @RequestParam(defaultValue = "100") int latencyMs) {
            return apiService.processEndpointWithPercentiles(endpoint, latencyMs);
        }
        
        @GetMapping("/api/stats")
        public Map<String, Object> getApiStats() {
            return apiService.getPercentileStats();
        }
        
        @GetMapping("/db/query")
        public List<Map<String, Object>> queryDb(
                @RequestParam String table,
                @RequestParam String query,
                @RequestParam(defaultValue = "10") int expectedRows) {
            return dbService.executeQuery(table, query, expectedRows);
        }
        
        @GetMapping("/db/stats")
        public Map<String, Object> getDbStats() {
            return dbService.getQueryPercentiles();
        }
        
        @GetMapping("/service/external/{serviceName}")
        public String callExternal(
                @PathVariable String serviceName,
                @RequestParam(defaultValue = "/api/data") String endpoint) {
            return serviceCallService.callExternalService(serviceName, endpoint);
        }
        
        @GetMapping("/service/internal/{serviceName}")
        public String callInternal(
                @PathVariable String serviceName,
                @RequestParam(defaultValue = "/api/data") String endpoint) {
            return serviceCallService.callInternalService(serviceName, endpoint);
        }
        
        @GetMapping("/service/stats")
        public Map<String, Object> getServiceStats() {
            return serviceCallService.getServiceCallStats();
        }
        
        @PostMapping("/message")
        public String processMessage(
                @RequestParam String queueName,
                @RequestParam String messageId,
                @RequestParam(defaultValue = "1000") int payloadSize) {
            return messageService.processMessage(queueName, messageId, payloadSize);
        }
        
        @GetMapping("/message/stats")
        public Map<String, Object> getMessageStats() {
            return messageService.getMessageStats();
        }
        
        @GetMapping("/sla/request")
        public String slaRequest(
                @RequestParam String requestId,
                @RequestParam(defaultValue = "100") int expectedLatency) {
            return slaService.processRequestWithSLA(requestId, expectedLatency);
        }
        
        @GetMapping("/sla/compliance")
        public Map<String, Object> getSLACompliance() {
            return slaService.getSLACompliance();
        }
        
        @PostMapping("/performance/execute")
        public String executeOperation(
                @RequestParam String operationName,
                @RequestParam(defaultValue = "10") int complexity) {
            return regressionService.executeOperation(operationName, complexity);
        }
        
        @GetMapping("/performance/regression")
        public Map<String, Object> checkRegression(@RequestParam double currentP95) {
            return regressionService.checkPerformanceRegression(currentP95);
        }
    }
}
