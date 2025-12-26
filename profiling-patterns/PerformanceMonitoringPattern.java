package com.example.performancemonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Performance Monitoring Pattern
 * 
 * Demonstrates comprehensive application performance monitoring using Micrometer.
 * 
 * Features:
 * - Counters for request tracking
 * - Gauges for current state
 * - Timers for operation duration
 * - Distribution summaries for metrics
 * 
 * Use Cases:
 * - Application performance tracking
 * - SLA monitoring
 * - Capacity planning
 * - Performance regression detection
 */
@SpringBootApplication
public class PerformanceMonitoringPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(PerformanceMonitoringPattern.class, args);
    }
}

/**
 * Service for performance monitoring operations
 */
@Service
class PerformanceMonitoringService {
    
    private final MeterRegistry meterRegistry;
    private final Counter requestCounter;
    private final Timer requestTimer;
    private final DistributionSummary responseSizeSummary;
    private final Gauge activeRequestsGauge;
    private int activeRequests = 0;
    
    public PerformanceMonitoringService() {
        this.meterRegistry = new SimpleMeterRegistry();
        
        // Counter for total requests
        this.requestCounter = Counter.builder("api.requests.total")
            .description("Total number of API requests")
            .tag("type", "http")
            .register(meterRegistry);
        
        // Timer for request duration
        this.requestTimer = Timer.builder("api.requests.duration")
            .description("API request duration")
            .publishPercentiles(0.5, 0.95, 0.99)
            .publishPercentileHistogram()
            .register(meterRegistry);
        
        // Distribution summary for response sizes
        this.responseSizeSummary = DistributionSummary.builder("api.response.size")
            .description("Response payload size")
            .baseUnit("bytes")
            .register(meterRegistry);
        
        // Gauge for active requests
        this.activeRequestsGauge = Gauge.builder("api.requests.active", this, 
                PerformanceMonitoringService::getActiveRequests)
            .description("Number of active requests")
            .register(meterRegistry);
    }
    
    /**
     * Record an API request
     */
    public Map<String, Object> recordRequest(String endpoint, long duration, int responseSize) {
        requestCounter.increment();
        requestTimer.record(duration, TimeUnit.MILLISECONDS);
        responseSizeSummary.record(responseSize);
        
        return Map.of(
            "endpoint", endpoint,
            "duration", duration,
            "responseSize", responseSize,
            "recorded", true
        );
    }
    
    /**
     * Simulate processing with monitoring
     */
    public String processWithMonitoring(String operation) throws InterruptedException {
        activeRequests++;
        
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // Simulate processing
            Thread.sleep((long) (Math.random() * 100));
            return "Processed: " + operation;
        } finally {
            sample.stop(requestTimer);
            activeRequests--;
        }
    }
    
    /**
     * Get performance metrics
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("totalRequests", requestCounter.count());
        metrics.put("activeRequests", activeRequests);
        
        // Timer metrics
        metrics.put("avgDuration", requestTimer.mean(TimeUnit.MILLISECONDS));
        metrics.put("maxDuration", requestTimer.max(TimeUnit.MILLISECONDS));
        metrics.put("totalTime", requestTimer.totalTime(TimeUnit.MILLISECONDS));
        
        // Response size metrics
        metrics.put("avgResponseSize", responseSizeSummary.mean());
        metrics.put("maxResponseSize", responseSizeSummary.max());
        metrics.put("totalResponseSize", responseSizeSummary.totalAmount());
        
        return metrics;
    }
    
    /**
     * Record custom metric
     */
    public void recordCustomMetric(String name, double value, Map<String, String> tags) {
        Counter.builder(name)
            .tags(Tags.of(tags.entrySet().stream()
                .map(e -> Tag.of(e.getKey(), e.getValue()))
                .toArray(Tag[]::new)))
            .register(meterRegistry)
            .increment(value);
    }
    
    private int getActiveRequests() {
        return activeRequests;
    }
}

/**
 * REST Controller demonstrating performance monitoring
 */
@RestController
@RequestMapping("/api/performance")
class PerformanceMonitoringController {
    
    private final PerformanceMonitoringService monitoringService;
    
    public PerformanceMonitoringController(PerformanceMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }
    
    /**
     * Record API request metrics
     */
    @PostMapping("/record")
    public Map<String, Object> recordRequest(@RequestBody Map<String, Object> request) {
        String endpoint = (String) request.get("endpoint");
        long duration = ((Number) request.get("duration")).longValue();
        int responseSize = ((Number) request.get("responseSize")).intValue();
        
        return monitoringService.recordRequest(endpoint, duration, responseSize);
    }
    
    /**
     * Process operation with monitoring
     */
    @GetMapping("/process")
    public Map<String, String> processOperation(@RequestParam String operation) throws InterruptedException {
        String result = monitoringService.processWithMonitoring(operation);
        return Map.of("result", result);
    }
    
    /**
     * Get current performance metrics
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        return monitoringService.getMetrics();
    }
    
    /**
     * Record custom metric
     */
    @PostMapping("/custom-metric")
    public Map<String, String> recordCustomMetric(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        double value = ((Number) request.get("value")).doubleValue();
        Map<String, String> tags = (Map<String, String>) request.getOrDefault("tags", Map.of());
        
        monitoringService.recordCustomMetric(name, value, tags);
        return Map.of("status", "recorded");
    }
}
