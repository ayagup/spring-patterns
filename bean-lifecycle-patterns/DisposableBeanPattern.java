package com.spring.patterns.lifecycle;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DisposableBean Pattern
 * =======================
 * 
 * Demonstrates the DisposableBean interface for bean cleanup.
 * This is a Spring-specific interface that provides a destroy() callback.
 * 
 * KEY FEATURES:
 * =============
 * - Spring-specific interface
 * - Single destroy() method
 * - Called before bean destruction
 * - Can throw exceptions
 * - Tighter Spring integration than @PreDestroy
 * - Allows programmatic cleanup logic
 * 
 * INTERFACE:
 * ==========
 * public interface DisposableBean {
 *     void destroy() throws Exception;
 * }
 * 
 * EXECUTION ORDER (on shutdown):
 * ==============================
 * 1. @PreDestroy methods
 * 2. DisposableBean.destroy()
 * 3. Custom destroy-method
 * 
 * WHEN TO USE:
 * ============
 * - Need Spring-aware cleanup
 * - Want to throw checked exceptions
 * - Require programmatic control
 * - Building Spring framework extensions
 * 
 * vs OTHER CLEANUP METHODS:
 * ==========================
 * DisposableBean.destroy():
 *   - Spring-specific
 *   - Can throw checked exceptions
 *   - Tighter framework integration
 * 
 * @PreDestroy:
 *   - Standard annotation (JSR-250)
 *   - Portable across containers
 *   - More commonly used
 * 
 * @Bean(destroyMethod):
 *   - For third-party classes
 *   - Configuration-based
 *   - No interface implementation needed
 */

@SpringBootApplication
public class DisposableBeanPattern {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DisposableBeanPattern.class, args);
        System.out.println("\n=== DisposableBean Pattern Demo ===\n");
        System.out.println("Use /api/disposable/shutdown to trigger cleanup.\n");
    }
}

/**
 * Example 1: Database Session Manager
 */
@Component
class DatabaseSessionManager implements DisposableBean {
    private final List<Session> activeSessions = new ArrayList<>();
    private boolean active = true;
    
    public DatabaseSessionManager() {
        System.out.println("DatabaseSessionManager - Creating sessions");
        for (int i = 1; i <= 5; i++) {
            activeSessions.add(new Session("SESSION-" + i));
        }
        System.out.println("  Created " + activeSessions.size() + " sessions");
    }
    
    @Override
    public void destroy() throws Exception {
        System.out.println("\nDatabaseSessionManager - destroy() method called");
        
        for (Session session : activeSessions) {
            session.close();
        }
        
        if (!activeSessions.isEmpty()) {
            throw new Exception("Failed to close all sessions");
        }
        
        active = false;
        System.out.println("  All sessions closed successfully");
    }
    
    public int getActiveSessions() {
        return activeSessions.size();
    }
    
    public boolean isActive() {
        return active;
    }
    
    static class Session {
        private final String id;
        private final LocalDateTime createdAt;
        
        public Session(String id) {
            this.id = id;
            this.createdAt = LocalDateTime.now();
        }
        
        public void close() {
            System.out.println("  Closing session: " + id);
        }
    }
}

/**
 * Example 2: Message Queue Consumer
 */
@Service
class MessageQueueConsumer implements DisposableBean {
    private boolean consuming = true;
    private int messagesProcessed = 0;
    private final List<String> pendingMessages = new ArrayList<>();
    
    public MessageQueueConsumer() {
        System.out.println("\nMessageQueueConsumer - Starting consumer");
        pendingMessages.add("Message1");
        pendingMessages.add("Message2");
    }
    
    public void processMessage() {
        if (!pendingMessages.isEmpty()) {
            pendingMessages.remove(0);
            messagesProcessed++;
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("\nMessageQueueConsumer - destroy() method called");
        consuming = false;
        
        // Process pending messages before shutdown
        System.out.println("  Processing " + pendingMessages.size() + " pending messages");
        while (!pendingMessages.isEmpty()) {
            processMessage();
        }
        
        System.out.println("  Total messages processed: " + messagesProcessed);
        System.out.println("  Consumer stopped");
    }
    
    public String getStatus() {
        return String.format("Consuming: %s, Processed: %d, Pending: %d",
            consuming, messagesProcessed, pendingMessages.size());
    }
}

/**
 * Example 3: Transaction Manager
 */
@Component
class TransactionManager implements DisposableBean {
    private final List<Transaction> activeTransactions = new ArrayList<>();
    private boolean active = true;
    
    public TransactionManager() {
        System.out.println("\nTransactionManager - Initializing");
    }
    
    public void beginTransaction(String id) {
        Transaction tx = new Transaction(id);
        activeTransactions.add(tx);
    }
    
    public void commitTransaction(String id) {
        activeTransactions.removeIf(tx -> tx.id.equals(id));
    }
    
    @Override
    public void destroy() {
        System.out.println("\nTransactionManager - destroy() method called");
        
        if (!activeTransactions.isEmpty()) {
            System.out.println("  Rolling back " + activeTransactions.size() + " active transactions");
            for (Transaction tx : activeTransactions) {
                tx.rollback();
            }
            activeTransactions.clear();
        }
        
        active = false;
        System.out.println("  Transaction manager shutdown complete");
    }
    
    public String getStatus() {
        return String.format("Active: %s, Active Transactions: %d",
            active, activeTransactions.size());
    }
    
    static class Transaction {
        private final String id;
        private final LocalDateTime startedAt;
        
        public Transaction(String id) {
            this.id = id;
            this.startedAt = LocalDateTime.now();
        }
        
        public void rollback() {
            System.out.println("  Rolling back transaction: " + id);
        }
    }
}

/**
 * Example 4: File Upload Manager
 */
@Service
class FileUploadManager implements DisposableBean {
    private final List<UploadSession> activeSessions = new ArrayList<>();
    private final List<String> tempFiles = new ArrayList<>();
    
    public FileUploadManager() {
        System.out.println("\nFileUploadManager - Initializing");
    }
    
    public void startUpload(String filename) {
        UploadSession session = new UploadSession(filename);
        activeSessions.add(session);
        tempFiles.add("/tmp/" + filename);
    }
    
    @Override
    public void destroy() {
        System.out.println("\nFileUploadManager - destroy() method called");
        
        // Cancel active uploads
        if (!activeSessions.isEmpty()) {
            System.out.println("  Canceling " + activeSessions.size() + " active uploads");
            activeSessions.clear();
        }
        
        // Clean up temp files
        if (!tempFiles.isEmpty()) {
            System.out.println("  Deleting " + tempFiles.size() + " temp files");
            tempFiles.clear();
        }
        
        System.out.println("  File upload manager cleanup complete");
    }
    
    public String getStatus() {
        return String.format("Active Uploads: %d, Temp Files: %d",
            activeSessions.size(), tempFiles.size());
    }
    
    static class UploadSession {
        private final String filename;
        private final LocalDateTime startedAt;
        
        public UploadSession(String filename) {
            this.filename = filename;
            this.startedAt = LocalDateTime.now();
        }
    }
}

/**
 * Example 5: Cache with persistence
 */
@Component
class PersistentCache implements DisposableBean {
    private final java.util.Map<String, Object> cache = new java.util.HashMap<>();
    private int hits = 0;
    private int misses = 0;
    
    public PersistentCache() {
        System.out.println("\nPersistentCache - Loading cache");
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
    
    @Override
    public void destroy() throws Exception {
        System.out.println("\nPersistentCache - destroy() method called");
        
        // Save cache to persistent storage
        System.out.println("  Saving cache to disk");
        System.out.println("  Cache entries: " + cache.size());
        System.out.println("  Hit rate: " + 
            (hits + misses > 0 ? (hits * 100.0 / (hits + misses)) : 0) + "%");
        
        // Simulate save
        if (cache.isEmpty()) {
            throw new Exception("Cannot save empty cache");
        }
        
        cache.clear();
        System.out.println("  Cache persisted and cleared");
    }
    
    public String getStats() {
        return String.format("Size: %d, Hits: %d, Misses: %d", cache.size(), hits, misses);
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/disposable")
class DisposableBeanController {
    
    private final DatabaseSessionManager sessionManager;
    private final MessageQueueConsumer messageConsumer;
    private final TransactionManager transactionManager;
    private final FileUploadManager uploadManager;
    private final PersistentCache persistentCache;
    private final ConfigurableApplicationContext context;
    
    public DisposableBeanController(
            DatabaseSessionManager sessionManager,
            MessageQueueConsumer messageConsumer,
            TransactionManager transactionManager,
            FileUploadManager uploadManager,
            PersistentCache persistentCache,
            ConfigurableApplicationContext context) {
        this.sessionManager = sessionManager;
        this.messageConsumer = messageConsumer;
        this.transactionManager = transactionManager;
        this.uploadManager = uploadManager;
        this.persistentCache = persistentCache;
        this.context = context;
        System.out.println("\nDisposableBeanController - All beans initialized\n");
    }
    
    @GetMapping("/status")
    public String getStatus() {
        return String.format("""
            DisposableBean Pattern Status:
            
            Session Manager: Active=%s, Sessions=%d
            Message Consumer: %s
            Transaction Manager: %s
            Upload Manager: %s
            Persistent Cache: %s
            """,
            sessionManager.isActive(),
            sessionManager.getActiveSessions(),
            messageConsumer.getStatus(),
            transactionManager.getStatus(),
            uploadManager.getStatus(),
            persistentCache.getStats()
        );
    }
    
    @PostMapping("/transaction/begin")
    public String beginTransaction(@RequestParam String id) {
        transactionManager.beginTransaction(id);
        return "Transaction started: " + id;
    }
    
    @PostMapping("/transaction/commit")
    public String commitTransaction(@RequestParam String id) {
        transactionManager.commitTransaction(id);
        return "Transaction committed: " + id;
    }
    
    @PostMapping("/upload")
    public String startUpload(@RequestParam String filename) {
        uploadManager.startUpload(filename);
        return "Upload started: " + filename;
    }
    
    @PostMapping("/cache")
    public String addToCache(@RequestParam String key, @RequestParam String value) {
        persistentCache.put(key, value);
        return "Added to cache";
    }
    
    @GetMapping("/cache/{key}")
    public String getFromCache(@PathVariable String key) {
        Object value = persistentCache.get(key);
        return value != null ? "Value: " + value : "Not found";
    }
    
    @PostMapping("/shutdown")
    public String shutdown() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("\n=== Triggering DisposableBean.destroy() ===\n");
                context.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return "Shutdown initiated. Watch console for DisposableBean.destroy() execution.";
    }
}

/**
 * TESTING:
 * ========
 * 
 * curl http://localhost:8080/api/disposable/status
 * curl -X POST "http://localhost:8080/api/disposable/transaction/begin?id=TX1"
 * curl -X POST "http://localhost:8080/api/disposable/transaction/commit?id=TX1"
 * curl -X POST "http://localhost:8080/api/disposable/upload?filename=test.pdf"
 * curl -X POST "http://localhost:8080/api/disposable/cache?key=test&value=hello"
 * curl http://localhost:8080/api/disposable/cache/test
 * curl -X POST http://localhost:8080/api/disposable/shutdown
 * 
 * BEST PRACTICES:
 * ===============
 * 
 * 1. Use for Spring-aware cleanup
 * 2. Can throw checked exceptions
 * 3. Good for framework extensions
 * 4. Handle cleanup errors gracefully
 * 5. Release resources in reverse order
 * 6. Log cleanup actions
 * 7. Don't block shutdown indefinitely
 * 
 * ADVANTAGES:
 * ===========
 * - Can throw checked exceptions
 * - Spring-aware cleanup
 * - Programmatic control
 * - Framework integration
 * 
 * DISADVANTAGES:
 * ==============
 * - Couples code to Spring
 * - Less portable than @PreDestroy
 * - Requires interface implementation
 */
