package com.spring.patterns.lifecycle;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Bean Destruction Pattern
 * =========================
 * 
 * Demonstrates various techniques for destroying/cleaning up Spring beans including:
 * 1. @PreDestroy annotation
 * 2. DisposableBean interface (destroy method)
 * 3. @Bean destroyMethod attribute
 * 4. Custom cleanup logic
 * 5. Resource cleanup (files, connections, threads)
 * 6. Destruction order and lifecycle callbacks
 * 
 * DESTRUCTION ORDER:
 * ==================
 * 1. @PreDestroy methods
 * 2. DisposableBean.destroy()
 * 3. Custom destroy-method
 * 
 * USE CASES:
 * ==========
 * - Close database/network connections
 * - Release file handles
 * - Shutdown thread pools
 * - Clear caches
 * - Save state before shutdown
 * - Clean up temporary files
 * - Unregister listeners
 * - Free external resources
 */

@SpringBootApplication
public class BeanDestructionPattern {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = 
            SpringApplication.run(BeanDestructionPattern.class, args);
        
        System.out.println("\n=== Bean Destruction Pattern Demo ===\n");
        System.out.println("Application started. Use /api/destruction/shutdown to trigger graceful shutdown.");
        
        // Add shutdown hook to demonstrate destruction
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== Shutdown Hook Triggered ===");
            System.out.println("Application is shutting down...\n");
        }));
    }
}

/**
 * Configuration for destruction pattern examples
 */
@Configuration
class DestructionConfig {
    
    @Bean(destroyMethod = "cleanup")
    public DatabaseConnectionManager databaseConnectionManager() {
        return new DatabaseConnectionManager();
    }
    
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolManager threadPoolManager() {
        return new ThreadPoolManager();
    }
    
    @Bean
    public FileManager fileManager() {
        return new FileManager();
    }
    
    @Bean
    public CacheManager cacheManager() {
        return new CacheManager();
    }
    
    @Bean
    public ResourceMonitor resourceMonitor() {
        return new ResourceMonitor();
    }
}

/**
 * Example 1: Database Connection Manager with cleanup
 * ====================================================
 * Closes database connections on shutdown
 */
class DatabaseConnectionManager {
    private final List<DatabaseConnection> connections = new ArrayList<>();
    private boolean active = true;
    
    public DatabaseConnectionManager() {
        System.out.println("DatabaseConnectionManager - Constructor");
        // Create some connections
        for (int i = 1; i <= 3; i++) {
            connections.add(new DatabaseConnection("DB-" + i));
        }
        System.out.println("  Created " + connections.size() + " database connections");
    }
    
    /**
     * Custom cleanup method (called via @Bean destroyMethod)
     */
    public void cleanup() {
        System.out.println("\n1. DatabaseConnectionManager - cleanup() called (custom destroy method)");
        closeAllConnections();
        this.active = false;
        System.out.println("   All database connections closed successfully");
    }
    
    private void closeAllConnections() {
        for (DatabaseConnection conn : connections) {
            conn.close();
        }
        connections.clear();
    }
    
    public int getActiveConnections() {
        return connections.size();
    }
    
    public boolean isActive() {
        return active;
    }
    
    public List<String> getConnectionStatus() {
        List<String> status = new ArrayList<>();
        for (DatabaseConnection conn : connections) {
            status.add(conn.toString());
        }
        return status;
    }
    
    static class DatabaseConnection {
        private final String name;
        private final LocalDateTime createdAt;
        private boolean open = true;
        
        public DatabaseConnection(String name) {
            this.name = name;
            this.createdAt = LocalDateTime.now();
        }
        
        public void close() {
            this.open = false;
            System.out.println("   Closed connection: " + name);
        }
        
        @Override
        public String toString() {
            return String.format("Connection{name='%s', open=%s, createdAt=%s}",
                name, open, createdAt);
        }
    }
}

/**
 * Example 2: Thread Pool Manager with shutdown
 * =============================================
 * Gracefully shuts down thread pools
 */
class ThreadPoolManager {
    private final ExecutorService executorService;
    private final List<String> tasksExecuted = new ArrayList<>();
    
    public ThreadPoolManager() {
        System.out.println("\nThreadPoolManager - Constructor");
        this.executorService = Executors.newFixedThreadPool(4);
        System.out.println("  Created thread pool with 4 threads");
    }
    
    /**
     * Custom shutdown method (called via @Bean destroyMethod)
     */
    public void shutdown() {
        System.out.println("\n2. ThreadPoolManager - shutdown() called (custom destroy method)");
        try {
            System.out.println("   Shutting down executor service...");
            executorService.shutdown();
            
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("   Forcing shutdown...");
                executorService.shutdownNow();
                
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("   Executor service did not terminate");
                }
            }
            System.out.println("   Thread pool shutdown complete. Tasks executed: " + tasksExecuted.size());
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public void executeTask(Runnable task) {
        if (!executorService.isShutdown()) {
            executorService.execute(() -> {
                task.run();
                tasksExecuted.add("Task-" + tasksExecuted.size());
            });
        }
    }
    
    public int getTasksExecuted() {
        return tasksExecuted.size();
    }
    
    public boolean isShutdown() {
        return executorService.isShutdown();
    }
}

/**
 * Example 3: File Manager with multiple cleanup methods
 * ======================================================
 * Demonstrates both @PreDestroy and DisposableBean
 */
class FileManager implements DisposableBean {
    private final List<File> tempFiles = new ArrayList<>();
    private BufferedWriter logWriter;
    private boolean cleanupComplete = false;
    
    public FileManager() {
        System.out.println("\nFileManager - Constructor");
        try {
            File logFile = File.createTempFile("app-log-", ".txt");
            logWriter = new BufferedWriter(new FileWriter(logFile));
            tempFiles.add(logFile);
            System.out.println("  Created log file: " + logFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("  Error creating log file: " + e.getMessage());
        }
    }
    
    public void createTempFile(String prefix) {
        try {
            File tempFile = File.createTempFile(prefix, ".tmp");
            tempFiles.add(tempFile);
            writeLog("Created temp file: " + tempFile.getName());
        } catch (IOException e) {
            System.err.println("Error creating temp file: " + e.getMessage());
        }
    }
    
    public void writeLog(String message) {
        try {
            if (logWriter != null) {
                logWriter.write(LocalDateTime.now() + " - " + message);
                logWriter.newLine();
                logWriter.flush();
            }
        } catch (IOException e) {
            System.err.println("Error writing log: " + e.getMessage());
        }
    }
    
    @PreDestroy
    public void preDestroy() {
        System.out.println("\n3. FileManager - @PreDestroy called");
        writeLog("Application shutting down...");
        closeLogWriter();
    }
    
    @Override
    public void destroy() {
        System.out.println("4. FileManager - destroy() called (DisposableBean)");
        deleteTempFiles();
        this.cleanupComplete = true;
        System.out.println("   FileManager cleanup complete");
    }
    
    private void closeLogWriter() {
        try {
            if (logWriter != null) {
                logWriter.close();
                System.out.println("   Log writer closed");
            }
        } catch (IOException e) {
            System.err.println("   Error closing log writer: " + e.getMessage());
        }
    }
    
    private void deleteTempFiles() {
        int deleted = 0;
        for (File file : tempFiles) {
            if (file.exists() && file.delete()) {
                deleted++;
            }
        }
        System.out.println("   Deleted " + deleted + " temp files");
        tempFiles.clear();
    }
    
    public int getTempFileCount() {
        return tempFiles.size();
    }
    
    public boolean isCleanupComplete() {
        return cleanupComplete;
    }
}

/**
 * Example 4: Cache Manager with state persistence
 * ================================================
 * Saves cache state before destruction
 */
class CacheManager implements DisposableBean {
    private final java.util.Map<String, Object> cache = new java.util.HashMap<>();
    private int hitCount = 0;
    private int missCount = 0;
    
    public CacheManager() {
        System.out.println("\nCacheManager - Constructor");
        // Pre-populate cache
        cache.put("user:1", "John Doe");
        cache.put("user:2", "Jane Smith");
        cache.put("config:timeout", 3000);
        System.out.println("  Cache initialized with " + cache.size() + " entries");
    }
    
    public Object get(String key) {
        Object value = cache.get(key);
        if (value != null) {
            hitCount++;
        } else {
            missCount++;
        }
        return value;
    }
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    @PreDestroy
    public void saveState() {
        System.out.println("\n5. CacheManager - @PreDestroy - Saving cache state");
        System.out.println("   Cache Statistics:");
        System.out.println("     Entries: " + cache.size());
        System.out.println("     Hits: " + hitCount);
        System.out.println("     Misses: " + missCount);
        System.out.println("     Hit Rate: " + 
            (hitCount + missCount > 0 ? (hitCount * 100.0 / (hitCount + missCount)) : 0) + "%");
    }
    
    @Override
    public void destroy() {
        System.out.println("6. CacheManager - destroy() - Clearing cache");
        cache.clear();
        System.out.println("   Cache cleared. Final entry count: " + cache.size());
    }
    
    public int getCacheSize() {
        return cache.size();
    }
    
    public String getStatistics() {
        return String.format("Size: %d, Hits: %d, Misses: %d",
            cache.size(), hitCount, missCount);
    }
}

/**
 * Example 5: Resource Monitor with cleanup
 * =========================================
 * Monitors resources and cleans up on shutdown
 */
class ResourceMonitor {
    private final List<String> monitoredResources = new ArrayList<>();
    private volatile boolean monitoring = true;
    private final Thread monitorThread;
    
    public ResourceMonitor() {
        System.out.println("\nResourceMonitor - Constructor");
        monitoredResources.add("CPU");
        monitoredResources.add("Memory");
        monitoredResources.add("Disk");
        
        // Start monitoring thread
        monitorThread = new Thread(() -> {
            while (monitoring) {
                try {
                    Thread.sleep(5000);
                    // Simulate monitoring
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
        System.out.println("  Started monitoring " + monitoredResources.size() + " resources");
    }
    
    @PreDestroy
    public void stopMonitoring() {
        System.out.println("\n7. ResourceMonitor - @PreDestroy - Stopping monitoring");
        monitoring = false;
        
        try {
            monitorThread.interrupt();
            monitorThread.join(2000);
            System.out.println("   Monitoring thread stopped");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("   Error stopping monitoring thread");
        }
        
        System.out.println("   Resource monitoring shutdown complete");
    }
    
    public List<String> getMonitoredResources() {
        return new ArrayList<>(monitoredResources);
    }
    
    public boolean isMonitoring() {
        return monitoring;
    }
}

/**
 * REST Controller for destruction pattern demonstration
 */
@RestController
@RequestMapping("/api/destruction")
class BeanDestructionController {
    
    private final DatabaseConnectionManager dbManager;
    private final ThreadPoolManager threadPoolManager;
    private final FileManager fileManager;
    private final CacheManager cacheManager;
    private final ResourceMonitor resourceMonitor;
    private final ConfigurableApplicationContext context;
    
    public BeanDestructionController(
            DatabaseConnectionManager dbManager,
            ThreadPoolManager threadPoolManager,
            FileManager fileManager,
            CacheManager cacheManager,
            ResourceMonitor resourceMonitor,
            ConfigurableApplicationContext context) {
        this.dbManager = dbManager;
        this.threadPoolManager = threadPoolManager;
        this.fileManager = fileManager;
        this.cacheManager = cacheManager;
        this.resourceMonitor = resourceMonitor;
        this.context = context;
    }
    
    @GetMapping("/status")
    public String getStatus() {
        return String.format("""
            Bean Destruction Pattern - Current Status:
            
            Database Manager:
              Active Connections: %d
              Active: %s
            
            Thread Pool Manager:
              Tasks Executed: %d
              Shutdown: %s
            
            File Manager:
              Temp Files: %d
              Cleanup Complete: %s
            
            Cache Manager:
              %s
            
            Resource Monitor:
              Monitored Resources: %s
              Monitoring: %s
            """,
            dbManager.getActiveConnections(),
            dbManager.isActive(),
            threadPoolManager.getTasksExecuted(),
            threadPoolManager.isShutdown(),
            fileManager.getTempFileCount(),
            fileManager.isCleanupComplete(),
            cacheManager.getStatistics(),
            resourceMonitor.getMonitoredResources(),
            resourceMonitor.isMonitoring()
        );
    }
    
    @GetMapping("/connections")
    public List<String> getConnections() {
        return dbManager.getConnectionStatus();
    }
    
    @PostMapping("/task")
    public String executeTask() {
        threadPoolManager.executeTask(() -> {
            System.out.println("Executing background task...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        return "Task submitted. Total tasks: " + threadPoolManager.getTasksExecuted();
    }
    
    @PostMapping("/tempfile")
    public String createTempFile() {
        fileManager.createTempFile("test-");
        return "Temp file created. Total: " + fileManager.getTempFileCount();
    }
    
    @PostMapping("/cache")
    public String addToCache(String key, String value) {
        cacheManager.put(key, value);
        return "Added to cache. Size: " + cacheManager.getCacheSize();
    }
    
    @GetMapping("/cache/{key}")
    public String getFromCache(String key) {
        Object value = cacheManager.get(key);
        return value != null ? "Value: " + value : "Not found";
    }
    
    @PostMapping("/shutdown")
    public String shutdown() {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Give time to return response
                System.out.println("\n=== Initiating Graceful Shutdown ===\n");
                context.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        return "Graceful shutdown initiated. Watch console for destruction callbacks.";
    }
}

/**
 * DESTRUCTION ORDER EXAMPLE:
 * ===========================
 * 
 * When context.close() is called, you'll see:
 * 
 * 1. DatabaseConnectionManager - cleanup()
 * 2. ThreadPoolManager - shutdown()
 * 3. FileManager - @PreDestroy
 * 4. FileManager - destroy()
 * 5. CacheManager - @PreDestroy
 * 6. CacheManager - destroy()
 * 7. ResourceMonitor - @PreDestroy
 * 
 * TESTING:
 * ========
 * 
 * # Check current status
 * curl http://localhost:8080/api/destruction/status
 * 
 * # View database connections
 * curl http://localhost:8080/api/destruction/connections
 * 
 * # Execute background task
 * curl -X POST http://localhost:8080/api/destruction/task
 * 
 * # Create temp file
 * curl -X POST http://localhost:8080/api/destruction/tempfile
 * 
 * # Add to cache
 * curl -X POST "http://localhost:8080/api/destruction/cache?key=test&value=hello"
 * 
 * # Get from cache
 * curl http://localhost:8080/api/destruction/cache/test
 * 
 * # Trigger graceful shutdown (watch console for destruction callbacks)
 * curl -X POST http://localhost:8080/api/destruction/shutdown
 * 
 * BEST PRACTICES:
 * ===============
 * 
 * 1. Always clean up resources in destroy methods
 *    - Close connections
 *    - Release file handles
 *    - Shutdown thread pools
 *    - Clear caches
 * 
 * 2. Use @PreDestroy for simple cleanup
 *    - Standard JSR-250 annotation
 *    - Easy to understand
 *    - Most common approach
 * 
 * 3. Use DisposableBean for framework-aware cleanup
 *    - When you need Spring-specific features
 *    - Can throw exceptions
 *    - More control over cleanup
 * 
 * 4. Use @Bean(destroyMethod) for third-party beans
 *    - When you can't modify the class
 *    - Useful for library classes
 * 
 * 5. Handle cleanup failures gracefully
 *    - Log errors but don't throw exceptions
 *    - Try to clean up as much as possible
 *    - Don't block shutdown indefinitely
 * 
 * 6. Keep cleanup methods fast
 *    - Don't wait too long for resources
 *    - Set reasonable timeouts
 *    - Consider force shutdown if needed
 * 
 * 7. Save state before cleanup
 *    - Persist important data
 *    - Log final statistics
 *    - Create audit trails
 * 
 * 8. Order matters for dependent beans
 *    - Spring destroys in reverse order of creation
 *    - Use @DependsOn if specific order needed
 * 
 * COMMON PITFALLS:
 * ================
 * 
 * 1. Resource leaks (not closing connections/files)
 * 2. Long-running cleanup blocking shutdown
 * 3. Exceptions in destroy methods
 * 4. Not waiting for threads to terminate
 * 5. Accessing destroyed beans
 * 6. Not saving important state
 * 7. Assuming specific destruction order
 * 
 * SHUTDOWN HOOKS vs SPRING DESTROY:
 * ==================================
 * 
 * Runtime.addShutdownHook():
 * - JVM-level hook
 * - Runs on JVM shutdown
 * - Not managed by Spring
 * - Use for critical cleanup
 * 
 * Spring Destroy Methods:
 * - Spring-managed cleanup
 * - Runs on context close
 * - Preferred for Spring beans
 * - Better integration with Spring lifecycle
 */
