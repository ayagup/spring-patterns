package com.example.memoryprofiling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.management.*;
import java.util.*;

/**
 * Memory Profiling Pattern
 * 
 * Demonstrates JVM memory monitoring and profiling.
 * 
 * Features:
 * - Heap memory tracking
 * - GC monitoring
 * - Memory pool analysis
 * - Memory leak detection
 * 
 * Use Cases:
 * - Memory optimization
 * - Leak detection
 * - Capacity planning
 * - Performance tuning
 */
@SpringBootApplication
@EnableScheduling
public class MemoryProfilingPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(MemoryProfilingPattern.class, args);
    }
}

/**
 * Service for memory profiling
 */
@Service
class MemoryProfilingService {
    
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private final List<MemoryPoolMXBean> poolBeans = ManagementFactory.getMemoryPoolMXBeans();
    private final Runtime runtime = Runtime.getRuntime();
    
    /**
     * Get current heap memory usage
     */
    public Map<String, Object> getHeapMemory() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        return Map.of(
            "init", formatBytes(heapUsage.getInit()),
            "used", formatBytes(heapUsage.getUsed()),
            "committed", formatBytes(heapUsage.getCommitted()),
            "max", formatBytes(heapUsage.getMax()),
            "usagePercentage", String.format("%.2f%%", 
                (double) heapUsage.getUsed() / heapUsage.getMax() * 100)
        );
    }
    
    /**
     * Get non-heap memory usage
     */
    public Map<String, Object> getNonHeapMemory() {
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        return Map.of(
            "init", formatBytes(nonHeapUsage.getInit()),
            "used", formatBytes(nonHeapUsage.getUsed()),
            "committed", formatBytes(nonHeapUsage.getCommitted()),
            "max", nonHeapUsage.getMax() == -1 ? "undefined" : formatBytes(nonHeapUsage.getMax())
        );
    }
    
    /**
     * Get garbage collection statistics
     */
    public List<Map<String, Object>> getGCStatistics() {
        List<Map<String, Object>> gcStats = new ArrayList<>();
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            gcStats.add(Map.of(
                "name", gcBean.getName(),
                "collectionCount", gcBean.getCollectionCount(),
                "collectionTime", gcBean.getCollectionTime() + "ms",
                "memoryPoolNames", Arrays.toString(gcBean.getMemoryPoolNames())
            ));
        }
        
        return gcStats;
    }
    
    /**
     * Get memory pool details
     */
    public List<Map<String, Object>> getMemoryPools() {
        List<Map<String, Object>> pools = new ArrayList<>();
        
        for (MemoryPoolMXBean poolBean : poolBeans) {
            MemoryUsage usage = poolBean.getUsage();
            
            pools.add(Map.of(
                "name", poolBean.getName(),
                "type", poolBean.getType().toString(),
                "used", formatBytes(usage.getUsed()),
                "max", usage.getMax() == -1 ? "undefined" : formatBytes(usage.getMax()),
                "usagePercentage", usage.getMax() == -1 ? "N/A" : 
                    String.format("%.2f%%", (double) usage.getUsed() / usage.getMax() * 100)
            ));
        }
        
        return pools;
    }
    
    /**
     * Get runtime memory information
     */
    public Map<String, Object> getRuntimeMemory() {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        return Map.of(
            "total", formatBytes(totalMemory),
            "free", formatBytes(freeMemory),
            "used", formatBytes(usedMemory),
            "max", formatBytes(maxMemory),
            "usagePercentage", String.format("%.2f%%", (double) usedMemory / maxMemory * 100)
        );
    }
    
    /**
     * Trigger garbage collection
     */
    public Map<String, Object> triggerGC() {
        long beforeUsed = runtime.totalMemory() - runtime.freeMemory();
        
        System.gc();
        
        long afterUsed = runtime.totalMemory() - runtime.freeMemory();
        long freed = beforeUsed - afterUsed;
        
        return Map.of(
            "beforeUsed", formatBytes(beforeUsed),
            "afterUsed", formatBytes(afterUsed),
            "freed", formatBytes(freed),
            "status", "GC triggered"
        );
    }
    
    /**
     * Get comprehensive memory summary
     */
    public Map<String, Object> getMemorySummary() {
        return Map.of(
            "heap", getHeapMemory(),
            "nonHeap", getNonHeapMemory(),
            "runtime", getRuntimeMemory(),
            "gc", getGCStatistics(),
            "pools", getMemoryPools()
        );
    }
    
    /**
     * Detect potential memory issues
     */
    public List<String> detectMemoryIssues() {
        List<String> issues = new ArrayList<>();
        
        // Check heap usage
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        double heapUsagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        
        if (heapUsagePercent > 90) {
            issues.add("CRITICAL: Heap memory usage is " + String.format("%.2f%%", heapUsagePercent));
        } else if (heapUsagePercent > 75) {
            issues.add("WARNING: Heap memory usage is " + String.format("%.2f%%", heapUsagePercent));
        }
        
        // Check GC frequency
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            if (gcBean.getCollectionCount() > 1000) {
                issues.add("WARNING: High GC count for " + gcBean.getName() + 
                    ": " + gcBean.getCollectionCount());
            }
        }
        
        if (issues.isEmpty()) {
            issues.add("No memory issues detected");
        }
        
        return issues;
    }
    
    /**
     * Schedule periodic memory monitoring
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorMemory() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        double usagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        
        System.out.println(String.format("Memory Monitor: Heap usage %.2f%% (%s / %s)",
            usagePercent,
            formatBytes(heapUsage.getUsed()),
            formatBytes(heapUsage.getMax())));
    }
    
    /**
     * Format bytes to human-readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.2f %s", bytes / Math.pow(1024, exp), pre);
    }
}

/**
 * REST Controller demonstrating memory profiling
 */
@RestController
@RequestMapping("/api/memory")
class MemoryProfilingController {
    
    private final MemoryProfilingService profilingService;
    private final List<byte[]> memoryLeakSimulator = new ArrayList<>();
    
    public MemoryProfilingController(MemoryProfilingService profilingService) {
        this.profilingService = profilingService;
    }
    
    @GetMapping("/heap")
    public Map<String, Object> getHeapMemory() {
        return profilingService.getHeapMemory();
    }
    
    @GetMapping("/non-heap")
    public Map<String, Object> getNonHeapMemory() {
        return profilingService.getNonHeapMemory();
    }
    
    @GetMapping("/gc")
    public List<Map<String, Object>> getGCStatistics() {
        return profilingService.getGCStatistics();
    }
    
    @GetMapping("/pools")
    public List<Map<String, Object>> getMemoryPools() {
        return profilingService.getMemoryPools();
    }
    
    @GetMapping("/runtime")
    public Map<String, Object> getRuntimeMemory() {
        return profilingService.getRuntimeMemory();
    }
    
    @GetMapping("/summary")
    public Map<String, Object> getMemorySummary() {
        return profilingService.getMemorySummary();
    }
    
    @PostMapping("/gc")
    public Map<String, Object> triggerGC() {
        return profilingService.triggerGC();
    }
    
    @GetMapping("/issues")
    public Map<String, List<String>> detectIssues() {
        return Map.of("issues", profilingService.detectMemoryIssues());
    }
    
    /**
     * Simulate memory allocation
     */
    @PostMapping("/allocate")
    public Map<String, String> allocateMemory(@RequestParam(defaultValue = "10") int megabytes) {
        memoryLeakSimulator.add(new byte[megabytes * 1024 * 1024]);
        return Map.of("status", "Allocated " + megabytes + " MB");
    }
    
    /**
     * Clear allocated memory
     */
    @DeleteMapping("/allocate")
    public Map<String, String> clearAllocatedMemory() {
        int size = memoryLeakSimulator.size();
        memoryLeakSimulator.clear();
        return Map.of("status", "Cleared " + size + " allocations");
    }
}
