package com.example.threadprofiling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Thread Profiling Pattern
 * 
 * Demonstrates thread monitoring and profiling for multithreaded applications.
 * 
 * Features:
 * - Thread count monitoring
 * - Thread state analysis
 * - Deadlock detection
 * - Thread CPU time tracking
 * 
 * Use Cases:
 * - Concurrency issues
 * - Thread pool tuning
 * - Deadlock prevention
 * - Performance optimization
 */
@SpringBootApplication
public class ThreadProfilingPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(ThreadProfilingPattern.class, args);
    }
}

/**
 * Service for thread profiling
 */
@Service
class ThreadProfilingService {
    
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    /**
     * Get thread count summary
     */
    public Map<String, Object> getThreadCount() {
        return Map.of(
            "current", threadBean.getThreadCount(),
            "peak", threadBean.getPeakThreadCount(),
            "daemon", threadBean.getDaemonThreadCount(),
            "totalStarted", threadBean.getTotalStartedThreadCount()
        );
    }
    
    /**
     * Get all thread information
     */
    public List<Map<String, Object>> getAllThreads() {
        long[] threadIds = threadBean.getAllThreadIds();
        ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadIds, Integer.MAX_VALUE);
        
        List<Map<String, Object>> threads = new ArrayList<>();
        for (ThreadInfo info : threadInfos) {
            if (info != null) {
                threads.add(Map.of(
                    "id", info.getThreadId(),
                    "name", info.getThreadName(),
                    "state", info.getThreadState().toString(),
                    "blockedTime", info.getBlockedTime(),
                    "blockedCount", info.getBlockedCount(),
                    "waitedTime", info.getWaitedTime(),
                    "waitedCount", info.getWaitedCount(),
                    "inNative", info.isInNative(),
                    "suspended", info.isSuspended()
                ));
            }
        }
        
        return threads;
    }
    
    /**
     * Get threads by state
     */
    public Map<String, Long> getThreadsByState() {
        long[] threadIds = threadBean.getAllThreadIds();
        ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadIds);
        
        return Arrays.stream(threadInfos)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                info -> info.getThreadState().toString(),
                Collectors.counting()
            ));
    }
    
    /**
     * Detect deadlocked threads
     */
    public Map<String, Object> detectDeadlocks() {
        long[] deadlockedThreads = threadBean.findDeadlockedThreads();
        
        if (deadlockedThreads == null) {
            return Map.of("deadlocked", false, "count", 0);
        }
        
        ThreadInfo[] threadInfos = threadBean.getThreadInfo(deadlockedThreads);
        List<Map<String, Object>> deadlockDetails = new ArrayList<>();
        
        for (ThreadInfo info : threadInfos) {
            deadlockDetails.add(Map.of(
                "threadId", info.getThreadId(),
                "threadName", info.getThreadName(),
                "state", info.getThreadState().toString(),
                "lockedMonitors", info.getLockedMonitors().length,
                "lockedSynchronizers", info.getLockedSynchronizers().length
            ));
        }
        
        return Map.of(
            "deadlocked", true,
            "count", deadlockedThreads.length,
            "threads", deadlockDetails
        );
    }
    
    /**
     * Get CPU time for threads (if supported)
     */
    public List<Map<String, Object>> getThreadCPUTime() {
        if (!threadBean.isThreadCpuTimeSupported()) {
            return List.of(Map.of("error", "Thread CPU time not supported"));
        }
        
        threadBean.setThreadCpuTimeEnabled(true);
        
        long[] threadIds = threadBean.getAllThreadIds();
        List<Map<String, Object>> cpuTimes = new ArrayList<>();
        
        for (long threadId : threadIds) {
            long cpuTime = threadBean.getThreadCpuTime(threadId);
            long userTime = threadBean.getThreadUserTime(threadId);
            ThreadInfo info = threadBean.getThreadInfo(threadId);
            
            if (info != null && cpuTime > 0) {
                cpuTimes.add(Map.of(
                    "threadId", threadId,
                    "threadName", info.getThreadName(),
                    "cpuTime", cpuTime / 1_000_000 + "ms", // Convert to milliseconds
                    "userTime", userTime / 1_000_000 + "ms"
                ));
            }
        }
        
        // Sort by CPU time
        cpuTimes.sort((a, b) -> {
            String aTime = (String) a.get("cpuTime");
            String bTime = (String) b.get("cpuTime");
            long aVal = Long.parseLong(aTime.replace("ms", ""));
            long bVal = Long.parseLong(bTime.replace("ms", ""));
            return Long.compare(bVal, aVal);
        });
        
        return cpuTimes.stream().limit(20).collect(Collectors.toList());
    }
    
    /**
     * Get thread dump
     */
    public List<Map<String, Object>> getThreadDump() {
        long[] threadIds = threadBean.getAllThreadIds();
        ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadIds, Integer.MAX_VALUE);
        
        List<Map<String, Object>> dump = new ArrayList<>();
        for (ThreadInfo info : threadInfos) {
            if (info != null) {
                StackTraceElement[] stackTrace = info.getStackTrace();
                List<String> stack = Arrays.stream(stackTrace)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList());
                
                dump.add(Map.of(
                    "threadId", info.getThreadId(),
                    "threadName", info.getThreadName(),
                    "state", info.getThreadState().toString(),
                    "stackTrace", stack
                ));
            }
        }
        
        return dump;
    }
    
    /**
     * Simulate CPU-intensive task
     */
    public Future<String> simulateCPUTask(int durationMs) {
        return executorService.submit(() -> {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < durationMs) {
                // Busy wait to consume CPU
                Math.pow(Math.random(), Math.random());
            }
            return "Task completed";
        });
    }
    
    /**
     * Simulate blocked threads
     */
    public void simulateBlockedThreads(int count) {
        Object lock = new Object();
        
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                synchronized (lock) {
                    try {
                        Thread.sleep(10000); // Block for 10 seconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "BlockedThread-" + i).start();
        }
    }
}

/**
 * REST Controller demonstrating thread profiling
 */
@RestController
@RequestMapping("/api/threads")
class ThreadProfilingController {
    
    private final ThreadProfilingService profilingService;
    
    public ThreadProfilingController(ThreadProfilingService profilingService) {
        this.profilingService = profilingService;
    }
    
    @GetMapping("/count")
    public Map<String, Object> getThreadCount() {
        return profilingService.getThreadCount();
    }
    
    @GetMapping("/all")
    public List<Map<String, Object>> getAllThreads() {
        return profilingService.getAllThreads();
    }
    
    @GetMapping("/by-state")
    public Map<String, Long> getThreadsByState() {
        return profilingService.getThreadsByState();
    }
    
    @GetMapping("/deadlocks")
    public Map<String, Object> detectDeadlocks() {
        return profilingService.detectDeadlocks();
    }
    
    @GetMapping("/cpu-time")
    public List<Map<String, Object>> getThreadCPUTime() {
        return profilingService.getThreadCPUTime();
    }
    
    @GetMapping("/dump")
    public List<Map<String, Object>> getThreadDump() {
        return profilingService.getThreadDump();
    }
    
    /**
     * Simulate CPU task
     */
    @PostMapping("/simulate-cpu")
    public Map<String, String> simulateCPU(@RequestParam(defaultValue = "1000") int durationMs) {
        profilingService.simulateCPUTask(durationMs);
        return Map.of("status", "CPU task started", "duration", durationMs + "ms");
    }
    
    /**
     * Simulate blocked threads
     */
    @PostMapping("/simulate-blocked")
    public Map<String, String> simulateBlocked(@RequestParam(defaultValue = "3") int count) {
        profilingService.simulateBlockedThreads(count);
        return Map.of("status", count + " blocked threads created");
    }
}
