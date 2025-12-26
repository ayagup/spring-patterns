package com.example.methodtiming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Method Execution Timing Pattern
 * 
 * Demonstrates method-level performance profiling using AOP.
 * 
 * Features:
 * - Automatic method timing
 * - Custom @Timed annotation
 * - Execution statistics
 * - Slow method detection
 * 
 * Use Cases:
 * - Performance bottleneck identification
 * - API endpoint timing
 * - Service method profiling
 * - Database operation monitoring
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class MethodExecutionTimingPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(MethodExecutionTimingPattern.class, args);
    }
}

/**
 * Custom annotation to mark methods for timing
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface Timed {
    String value() default "";
    long slowThresholdMs() default 1000;
}

/**
 * Aspect for method execution timing
 */
@Aspect
@Component
class MethodTimingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodTimingAspect.class);
    private final TimingStatisticsService statisticsService;
    
    public MethodTimingAspect(TimingStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
    
    /**
     * Time all methods annotated with @Timed
     */
    @Around("@annotation(timed)")
    public Object timeMethod(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        
        long start = System.nanoTime();
        Object result = null;
        Throwable exception = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            long duration = (System.nanoTime() - start) / 1_000_000; // Convert to milliseconds
            
            // Record timing
            statisticsService.recordTiming(methodName, duration);
            
            // Log execution
            if (exception != null) {
                logger.error("Method {} failed after {}ms", methodName, duration);
            } else if (duration > timed.slowThresholdMs()) {
                logger.warn("SLOW METHOD: {} took {}ms (threshold: {}ms)", 
                    methodName, duration, timed.slowThresholdMs());
            } else {
                logger.debug("Method {} completed in {}ms", methodName, duration);
            }
        }
    }
    
    /**
     * Time all service methods
     */
    @Around("execution(* com.example.methodtiming.*Service.*(..))")
    public Object timeServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = (System.nanoTime() - start) / 1_000_000;
            logger.trace("Service method {} took {}ms", methodName, duration);
        }
    }
}

/**
 * Service for collecting timing statistics
 */
@Service
class TimingStatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(TimingStatisticsService.class);
    private final Map<String, List<Long>> timings = new ConcurrentHashMap<>();
    
    /**
     * Record method timing
     */
    public void recordTiming(String methodName, long durationMs) {
        timings.computeIfAbsent(methodName, k -> new ArrayList<>()).add(durationMs);
        logger.trace("Recorded timing for {}: {}ms", methodName, durationMs);
    }
    
    /**
     * Get statistics for a method
     */
    public Map<String, Object> getStatistics(String methodName) {
        List<Long> methodTimings = timings.get(methodName);
        if (methodTimings == null || methodTimings.isEmpty()) {
            return Map.of("error", "No timings found for method: " + methodName);
        }
        
        LongSummaryStatistics stats = methodTimings.stream()
            .mapToLong(Long::longValue)
            .summaryStatistics();
        
        return Map.of(
            "method", methodName,
            "count", stats.getCount(),
            "min", stats.getMin(),
            "max", stats.getMax(),
            "avg", stats.getAverage(),
            "total", stats.getSum()
        );
    }
    
    /**
     * Get all statistics
     */
    public Map<String, Map<String, Object>> getAllStatistics() {
        Map<String, Map<String, Object>> allStats = new HashMap<>();
        timings.keySet().forEach(method -> {
            allStats.put(method, getStatistics(method));
        });
        return allStats;
    }
    
    /**
     * Get slowest methods
     */
    public List<Map<String, Object>> getSlowestMethods(int limit) {
        return timings.entrySet().stream()
            .map(entry -> {
                String method = entry.getKey();
                double avgTime = entry.getValue().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
                return Map.of("method", (Object) method, "avgTime", avgTime);
            })
            .sorted((a, b) -> Double.compare((double) b.get("avgTime"), (double) a.get("avgTime")))
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Clear all statistics
     */
    public void clearStatistics() {
        timings.clear();
        logger.info("Timing statistics cleared");
    }
}

/**
 * Business service with timed methods
 */
@Service
class BusinessOperationService {
    
    @Timed(value = "fast-operation", slowThresholdMs = 50)
    public String fastOperation() throws InterruptedException {
        Thread.sleep(10);
        return "Fast operation completed";
    }
    
    @Timed(value = "slow-operation", slowThresholdMs = 50)
    public String slowOperation() throws InterruptedException {
        Thread.sleep(100);
        return "Slow operation completed";
    }
    
    @Timed(value = "database-query")
    public List<Map<String, Object>> simulateDatabaseQuery() throws InterruptedException {
        Thread.sleep(50);
        return List.of(
            Map.of("id", 1, "name", "Record 1"),
            Map.of("id", 2, "name", "Record 2")
        );
    }
    
    @Timed(value = "external-api-call", slowThresholdMs = 200)
    public Map<String, String> simulateExternalApiCall() throws InterruptedException {
        Thread.sleep(150);
        return Map.of("status", "success", "data", "External data");
    }
}

/**
 * REST Controller demonstrating method timing
 */
@RestController
@RequestMapping("/api/timing")
class MethodTimingController {
    
    private final BusinessOperationService businessService;
    private final TimingStatisticsService statisticsService;
    
    public MethodTimingController(BusinessOperationService businessService,
                                 TimingStatisticsService statisticsService) {
        this.businessService = businessService;
        this.statisticsService = statisticsService;
    }
    
    @GetMapping("/fast")
    public Map<String, String> testFast() throws InterruptedException {
        String result = businessService.fastOperation();
        return Map.of("result", result);
    }
    
    @GetMapping("/slow")
    public Map<String, String> testSlow() throws InterruptedException {
        String result = businessService.slowOperation();
        return Map.of("result", result);
    }
    
    @GetMapping("/database")
    public List<Map<String, Object>> testDatabase() throws InterruptedException {
        return businessService.simulateDatabaseQuery();
    }
    
    @GetMapping("/external-api")
    public Map<String, String> testExternalApi() throws InterruptedException {
        return businessService.simulateExternalApiCall();
    }
    
    @GetMapping("/statistics")
    public Map<String, Map<String, Object>> getStatistics() {
        return statisticsService.getAllStatistics();
    }
    
    @GetMapping("/statistics/{method}")
    public Map<String, Object> getMethodStatistics(@PathVariable String method) {
        return statisticsService.getStatistics(method);
    }
    
    @GetMapping("/slowest")
    public List<Map<String, Object>> getSlowestMethods(@RequestParam(defaultValue = "5") int limit) {
        return statisticsService.getSlowestMethods(limit);
    }
    
    @DeleteMapping("/statistics")
    public Map<String, String> clearStatistics() {
        statisticsService.clearStatistics();
        return Map.of("status", "cleared");
    }
}
