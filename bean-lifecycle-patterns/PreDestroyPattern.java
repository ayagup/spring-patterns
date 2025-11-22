package com.spring.patterns.lifecycle;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @PreDestroy Pattern
 * ===================
 * 
 * The @PreDestroy annotation marks a method to be executed before bean destruction.
 * It's part of JSR-250 and is the standard way to perform cleanup operations.
 * 
 * KEY FEATURES:
 * =============
 * - Standard Java annotation (JSR-250)
 * - Called before bean is destroyed
 * - Called only once per bean instance
 * - No parameters allowed
 * - Can throw exceptions (logged, not propagated)
 * - Method can have any access modifier
 * - Only one @PreDestroy method recommended
 * 
 * EXECUTION ORDER (on shutdown):
 * ==============================
 * 1. @PreDestroy method(s)
 * 2. DisposableBean.destroy()
 * 3. Custom destroy-method
 * 
 * USE CASES:
 * ==========
 * - Close database connections
 * - Release file handles
 * - Shutdown thread pools
 * - Save application state
 * - Clear caches
 * - Unregister listeners
 * - Clean up temporary files
 * - Log final statistics
 * 
 * ADVANTAGES:
 * ===========
 * - Standard annotation (portable)
 * - Easy to understand
 * - No Spring dependencies
 * - Widely supported
 * - Good IDE support
 */

@SpringBootApplication
public class PreDestroyPattern {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PreDestroyPattern.class, args);
        System.out.println("\n=== @PreDestroy Pattern Demo ===\n");
        System.out.println("Use /api/predestroy/shutdown to trigger cleanup.\n");
    }
}

/**
 * Example 1: Connection Pool cleanup
 */
@Component
class DatabaseConnectionPool {
    private final List<Connection> connections = new ArrayList<>();
    
    public DatabaseConnectionPool() {
        System.out.println("DatabaseConnectionPool - Creating 5 connections");
        for (int i = 1; i <= 5; i++) {
            connections.add(new Connection("CONN-" + i));
        }
    }
    
    @PreDestroy
    public void closeConnections() {
        System.out.println("\n@PreDestroy - DatabaseConnectionPool - Closing all connections");
        for (Connection conn : connections) {
            conn.close();
        }
        connections.clear();
        System.out.println("  All connections closed");
    }
    
    public int getActiveConnections() {
        return connections.size();
    }
    
    static class Connection {
        private final String id;
        private boolean open = true;
        
        public Connection(String id) {
            this.id = id;
        }
        
        public void close() {
            this.open = false;
            System.out.println("  Closed: " + id);
        }
    }
}

/**
 * Example 2: Thread pool shutdown
 */
@Service
class BackgroundTaskService {
    private final ExecutorService executor;
    private int tasksCompleted = 0;
    
    public BackgroundTaskService() {
        System.out.println("BackgroundTaskService - Creating thread pool");
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    @PreDestroy
    public void shutdownExecutor() {
        System.out.println("\n@PreDestroy - BackgroundTaskService - Shutting down executor");
        try {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            System.out.println("  Executor shutdown. Tasks completed: " + tasksCompleted);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public void submitTask(Runnable task) {
        executor.execute(() -> {
            task.run();
            tasksCompleted++;
        });
    }
    
    public int getTasksCompleted() {
        return tasksCompleted;
    }
}

/**
 * Example 3: File resource cleanup
 */
@Component
class LogFileManager {
    private BufferedWriter writer;
    private final File logFile;
    
    public LogFileManager() throws IOException {
        System.out.println("LogFileManager - Creating log file");
        logFile = File.createTempFile("app-", ".log");
        writer = new BufferedWriter(new FileWriter(logFile, true));
        writeLog("Application started");
    }
    
    public void writeLog(String message) {
        try {
            writer.write(LocalDateTime.now() + " - " + message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing log: " + e.getMessage());
        }
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("\n@PreDestroy - LogFileManager - Cleaning up");
        writeLog("Application shutting down");
        
        try {
            if (writer != null) {
                writer.close();
                System.out.println("  Log file closed: " + logFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("  Error closing log file: " + e.getMessage());
        }
    }
}

/**
 * Example 4: Cache state persistence
 */
@Service
class CacheService {
    private final java.util.Map<String, Object> cache = new java.util.HashMap<>();
    private int hits = 0;
    private int misses = 0;
    
    public CacheService() {
        System.out.println("CacheService - Initializing cache");
        cache.put("key1", "value1");
        cache.put("key2", "value2");
    }
    
    public Object get(String key) {
        Object value = cache.get(key);
        if (value != null) hits++;
        else misses++;
        return value;
    }
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    @PreDestroy
    public void saveStatistics() {
        System.out.println("\n@PreDestroy - CacheService - Saving statistics");
        System.out.println("  Cache size: " + cache.size());
        System.out.println("  Hits: " + hits);
        System.out.println("  Misses: " + misses);
        System.out.println("  Hit rate: " + 
            (hits + misses > 0 ? (hits * 100.0 / (hits + misses)) : 0) + "%");
        cache.clear();
    }
    
    public String getStats() {
        return String.format("Size: %d, Hits: %d, Misses: %d", cache.size(), hits, misses);
    }
}

/**
 * Example 5: Monitoring service cleanup
 */
@Component
class MonitoringService {
    private volatile boolean running = true;
    private final Thread monitorThread;
    private long checksPerformed = 0;
    
    public MonitoringService() {
        System.out.println("MonitoringService - Starting monitoring");
        monitorThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    checksPerformed++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
    
    @PreDestroy
    public void stopMonitoring() {
        System.out.println("\n@PreDestroy - MonitoringService - Stopping monitoring");
        running = false;
        try {
            monitorThread.interrupt();
            monitorThread.join(2000);
            System.out.println("  Monitoring stopped. Checks performed: " + checksPerformed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public long getChecksPerformed() {
        return checksPerformed;
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/predestroy")
class PreDestroyController {
    
    private final DatabaseConnectionPool connectionPool;
    private final BackgroundTaskService taskService;
    private final LogFileManager logManager;
    private final CacheService cacheService;
    private final MonitoringService monitoringService;
    private final ConfigurableApplicationContext context;
    
    public PreDestroyController(
            DatabaseConnectionPool connectionPool,
            BackgroundTaskService taskService,
            LogFileManager logManager,
            CacheService cacheService,
            MonitoringService monitoringService,
            ConfigurableApplicationContext context) {
        this.connectionPool = connectionPool;
        this.taskService = taskService;
        this.logManager = logManager;
        this.cacheService = cacheService;
        this.monitoringService = monitoringService;
        this.context = context;
    }
    
    @GetMapping("/status")
    public String getStatus() {
        return String.format("""
            @PreDestroy Pattern Status:
            
            Connection Pool: %d active connections
            Task Service: %d tasks completed
            Cache Service: %s
            Monitoring Service: %d checks performed
            """,
            connectionPool.getActiveConnections(),
            taskService.getTasksCompleted(),
            cacheService.getStats(),
            monitoringService.getChecksPerformed()
        );
    }
    
    @PostMapping("/task")
    public String submitTask() {
        taskService.submitTask(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        return "Task submitted. Total: " + taskService.getTasksCompleted();
    }
    
    @PostMapping("/log")
    public String writeLog(@RequestParam String message) {
        logManager.writeLog(message);
        return "Log written: " + message;
    }
    
    @PostMapping("/cache")
    public String addToCache(@RequestParam String key, @RequestParam String value) {
        cacheService.put(key, value);
        return "Added to cache";
    }
    
    @GetMapping("/cache/{key}")
    public String getFromCache(@PathVariable String key) {
        Object value = cacheService.get(key);
        return value != null ? "Value: " + value : "Not found";
    }
    
    @PostMapping("/shutdown")
    public String shutdown() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("\n=== Triggering @PreDestroy cleanup ===\n");
                context.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return "Shutdown initiated. Watch console for @PreDestroy execution.";
    }
}

/**
 * TESTING:
 * ========
 * 
 * curl http://localhost:8080/api/predestroy/status
 * curl -X POST http://localhost:8080/api/predestroy/task
 * curl -X POST "http://localhost:8080/api/predestroy/log?message=Test"
 * curl -X POST "http://localhost:8080/api/predestroy/cache?key=test&value=hello"
 * curl http://localhost:8080/api/predestroy/cache/test
 * curl -X POST http://localhost:8080/api/predestroy/shutdown
 * 
 * BEST PRACTICES:
 * ===============
 * 
 * 1. Keep cleanup fast (don't block shutdown)
 * 2. Handle exceptions gracefully
 * 3. Release resources in reverse order of acquisition
 * 4. Log cleanup actions
 * 5. Don't throw exceptions from @PreDestroy
 * 6. Use timeouts for async operations
 * 7. Save important state before cleanup
 */
