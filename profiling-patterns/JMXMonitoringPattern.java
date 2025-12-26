package com.example.jmxmonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JMX Monitoring Pattern
 * 
 * Demonstrates JMX (Java Management Extensions) for application monitoring.
 * 
 * Features:
 * - Custom MBeans
 * - Application metrics exposure
 * - JMX notifications
 * - Runtime management operations
 * 
 * Use Cases:
 * - Production monitoring
 * - Runtime configuration
 * - Performance metrics
 * - Integration with monitoring tools (JConsole, VisualVM)
 */
@SpringBootApplication
public class JMXMonitoringPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(JMXMonitoringPattern.class, args);
    }
}

/**
 * Application metrics MBean
 */
@Component
@ManagedResource(objectName = "com.example:type=ApplicationMetrics", 
                description = "Application Performance Metrics")
class ApplicationMetricsMBean {
    
    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private long startTime = System.currentTimeMillis();
    
    @ManagedAttribute(description = "Total number of requests processed")
    public long getRequestCount() {
        return requestCount.get();
    }
    
    @ManagedAttribute(description = "Total number of errors")
    public long getErrorCount() {
        return errorCount.get();
    }
    
    @ManagedAttribute(description = "Application uptime in seconds")
    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
    
    @ManagedAttribute(description = "Error rate percentage")
    public double getErrorRate() {
        long total = requestCount.get();
        return total == 0 ? 0.0 : (double) errorCount.get() / total * 100;
    }
    
    @ManagedOperation(description = "Record a successful request")
    public void recordRequest() {
        requestCount.incrementAndGet();
    }
    
    @ManagedOperation(description = "Record an error")
    public void recordError() {
        errorCount.incrementAndGet();
        requestCount.incrementAndGet();
    }
    
    @ManagedOperation(description = "Reset all counters")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "confirm", description = "Confirmation flag")
    })
    public String resetCounters(boolean confirm) {
        if (confirm) {
            requestCount.set(0);
            errorCount.set(0);
            startTime = System.currentTimeMillis();
            return "Counters reset successfully";
        }
        return "Reset cancelled - confirmation required";
    }
}

/**
 * Cache statistics MBean
 */
@Component
@ManagedResource(objectName = "com.example:type=CacheStatistics",
                description = "Cache Performance Statistics")
class CacheStatisticsMBean {
    
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);
    
    @ManagedAttribute(description = "Cache hit count")
    public long getHits() {
        return hits.get();
    }
    
    @ManagedAttribute(description = "Cache miss count")
    public long getMisses() {
        return misses.get();
    }
    
    @ManagedAttribute(description = "Cache eviction count")
    public long getEvictions() {
        return evictions.get();
    }
    
    @ManagedAttribute(description = "Cache hit ratio")
    public double getHitRatio() {
        long total = hits.get() + misses.get();
        return total == 0 ? 0.0 : (double) hits.get() / total * 100;
    }
    
    @ManagedOperation(description = "Record cache hit")
    public void recordHit() {
        hits.incrementAndGet();
    }
    
    @ManagedOperation(description = "Record cache miss")
    public void recordMiss() {
        misses.incrementAndGet();
    }
    
    @ManagedOperation(description = "Record cache eviction")
    public void recordEviction() {
        evictions.incrementAndGet();
    }
    
    @ManagedOperation(description = "Clear cache statistics")
    public void clearStatistics() {
        hits.set(0);
        misses.set(0);
        evictions.set(0);
    }
}

/**
 * Configuration MBean for runtime settings
 */
@Component
@ManagedResource(objectName = "com.example:type=Configuration",
                description = "Runtime Configuration Management")
class ConfigurationMBean {
    
    private String logLevel = "INFO";
    private int threadPoolSize = 10;
    private boolean debugMode = false;
    private int maxConnections = 100;
    
    @ManagedAttribute(description = "Current log level")
    public String getLogLevel() {
        return logLevel;
    }
    
    @ManagedAttribute(description = "Set log level")
    public void setLogLevel(String level) {
        this.logLevel = level;
        System.out.println("Log level changed to: " + level);
    }
    
    @ManagedAttribute(description = "Thread pool size")
    public int getThreadPoolSize() {
        return threadPoolSize;
    }
    
    @ManagedAttribute(description = "Set thread pool size")
    public void setThreadPoolSize(int size) {
        this.threadPoolSize = size;
        System.out.println("Thread pool size changed to: " + size);
    }
    
    @ManagedAttribute(description = "Debug mode status")
    public boolean isDebugMode() {
        return debugMode;
    }
    
    @ManagedAttribute(description = "Enable/disable debug mode")
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        System.out.println("Debug mode " + (enabled ? "enabled" : "disabled"));
    }
    
    @ManagedAttribute(description = "Maximum concurrent connections")
    public int getMaxConnections() {
        return maxConnections;
    }
    
    @ManagedAttribute(description = "Set maximum concurrent connections")
    public void setMaxConnections(int max) {
        this.maxConnections = max;
        System.out.println("Max connections changed to: " + max);
    }
    
    @ManagedOperation(description = "Get all configuration as map")
    public Map<String, Object> getAllConfiguration() {
        return Map.of(
            "logLevel", logLevel,
            "threadPoolSize", threadPoolSize,
            "debugMode", debugMode,
            "maxConnections", maxConnections
        );
    }
}

/**
 * Service for JMX operations
 */
@Service
class JMXMonitoringService {
    
    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    
    /**
     * Get all registered MBeans
     */
    public List<String> getAllMBeans() {
        Set<ObjectName> objectNames = mBeanServer.queryNames(null, null);
        List<String> mbeanNames = new ArrayList<>();
        
        for (ObjectName name : objectNames) {
            mbeanNames.add(name.toString());
        }
        
        Collections.sort(mbeanNames);
        return mbeanNames;
    }
    
    /**
     * Get MBean attributes
     */
    public Map<String, Object> getMBeanAttributes(String objectName) {
        try {
            ObjectName name = new ObjectName(objectName);
            MBeanInfo info = mBeanServer.getMBeanInfo(name);
            MBeanAttributeInfo[] attributes = info.getAttributes();
            
            Map<String, Object> attributeValues = new HashMap<>();
            for (MBeanAttributeInfo attr : attributes) {
                try {
                    Object value = mBeanServer.getAttribute(name, attr.getName());
                    attributeValues.put(attr.getName(), value);
                } catch (Exception e) {
                    attributeValues.put(attr.getName(), "Error: " + e.getMessage());
                }
            }
            
            return attributeValues;
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
    
    /**
     * Invoke MBean operation
     */
    public Object invokeMBeanOperation(String objectName, String operation, Object[] params, String[] signature) {
        try {
            ObjectName name = new ObjectName(objectName);
            return mBeanServer.invoke(name, operation, params, signature);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

/**
 * REST Controller demonstrating JMX monitoring
 */
@RestController
@RequestMapping("/api/jmx")
class JMXMonitoringController {
    
    private final JMXMonitoringService monitoringService;
    private final ApplicationMetricsMBean appMetrics;
    private final CacheStatisticsMBean cacheStats;
    private final ConfigurationMBean configuration;
    
    public JMXMonitoringController(JMXMonitoringService monitoringService,
                                   ApplicationMetricsMBean appMetrics,
                                   CacheStatisticsMBean cacheStats,
                                   ConfigurationMBean configuration) {
        this.monitoringService = monitoringService;
        this.appMetrics = appMetrics;
        this.cacheStats = cacheStats;
        this.configuration = configuration;
    }
    
    /**
     * Test endpoint that records metrics
     */
    @GetMapping("/test")
    public Map<String, String> testEndpoint(@RequestParam(defaultValue = "false") boolean simulateError) {
        if (simulateError) {
            appMetrics.recordError();
            return Map.of("status", "error simulated");
        } else {
            appMetrics.recordRequest();
            return Map.of("status", "success");
        }
    }
    
    /**
     * Test cache operations
     */
    @GetMapping("/cache-test")
    public Map<String, String> testCache(@RequestParam(defaultValue = "hit") String type) {
        switch (type.toLowerCase()) {
            case "hit":
                cacheStats.recordHit();
                break;
            case "miss":
                cacheStats.recordMiss();
                break;
            case "eviction":
                cacheStats.recordEviction();
                break;
        }
        return Map.of("status", type + " recorded");
    }
    
    /**
     * Get all MBeans
     */
    @GetMapping("/mbeans")
    public List<String> getAllMBeans() {
        return monitoringService.getAllMBeans();
    }
    
    /**
     * Get MBean attributes
     */
    @GetMapping("/mbeans/{objectName}/attributes")
    public Map<String, Object> getMBeanAttributes(@PathVariable String objectName) {
        return monitoringService.getMBeanAttributes(objectName);
    }
    
    /**
     * Get configuration
     */
    @GetMapping("/config")
    public Map<String, Object> getConfiguration() {
        return configuration.getAllConfiguration();
    }
    
    /**
     * Update configuration
     */
    @PutMapping("/config/log-level")
    public Map<String, String> setLogLevel(@RequestParam String level) {
        configuration.setLogLevel(level);
        return Map.of("status", "updated", "logLevel", level);
    }
}
