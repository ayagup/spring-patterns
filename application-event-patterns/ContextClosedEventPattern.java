package com.example.events;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Context Closed Event Pattern
 * =============================
 * 
 * Demonstrates handling of ContextClosedEvent, which is published when
 * the ApplicationContext is closed via close() method.
 * 
 * Key Concepts:
 * ------------
 * 1. ContextClosedEvent - Published before context shutdown
 * 2. Final Cleanup - Last chance for cleanup
 * 3. Resource Release - Close all resources
 * 4. Graceful Shutdown - Orderly shutdown process
 * 5. Not Restartable - Context cannot be restarted
 * 
 * When Event Fires:
 * ----------------
 * - ConfigurableApplicationContext.close() called
 * - JVM shutdown hook triggered
 * - Before all beans destroyed
 * - Before context resources released
 * - Application shutdown
 * 
 * When to Use:
 * -----------
 * - Close file handles
 * - Close network connections
 * - Flush pending data
 * - Persist state
 * - Clean up resources
 * - Log shutdown info
 * - Send shutdown notifications
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ContextClosedEventPattern implements ApplicationListener<ContextClosedEvent> {
    
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.out.println("=== Context Closed Event ===");
        System.out.println("Application shutting down: " + 
                         event.getApplicationContext().getDisplayName());
        cleanupResources();
    }
    
    private void cleanupResources() {
        System.out.println("Cleaning up resources");
    }
}

/**
 * Example 2: Database Connection Closer
 */
@Component
class DatabaseConnectionCloser {
    
    @EventListener
    public void closeConnections(ContextClosedEvent event) {
        System.out.println("Closing database connections...");
        
        closeConnectionPool();
        flushPendingTransactions();
        
        System.out.println("Database connections closed");
    }
    
    private void closeConnectionPool() {
        System.out.println("Connection pool closed");
    }
    
    private void flushPendingTransactions() {
        System.out.println("Pending transactions flushed");
    }
}

/**
 * Example 3: File Resource Closer
 */
@Component
class FileResourceCloser {
    
    @EventListener
    public void closeFileResources(ContextClosedEvent event) {
        System.out.println("Closing file resources...");
        
        closeOpenFiles();
        flushBuffers();
        releaseLocks();
        
        System.out.println("File resources closed");
    }
    
    private void closeOpenFiles() {
        System.out.println("Open files closed");
    }
    
    private void flushBuffers() {
        System.out.println("Buffers flushed");
    }
    
    private void releaseLocks() {
        System.out.println("File locks released");
    }
}

/**
 * Example 4: Network Connection Closer
 */
@Component
class NetworkConnectionCloser {
    
    @EventListener
    public void closeNetworkConnections(ContextClosedEvent event) {
        System.out.println("Closing network connections...");
        
        closeHttpClients();
        closeWebSocketConnections();
        closeFtpConnections();
        
        System.out.println("Network connections closed");
    }
    
    private void closeHttpClients() {
        System.out.println("HTTP clients closed");
    }
    
    private void closeWebSocketConnections() {
        System.out.println("WebSocket connections closed");
    }
    
    private void closeFtpConnections() {
        System.out.println("FTP connections closed");
    }
}

/**
 * Example 5: State Persister
 */
@Component
class StatePersister {
    
    @EventListener
    public void persistState(ContextClosedEvent event) {
        System.out.println("Persisting application state...");
        
        saveApplicationState();
        saveUserSessions();
        savePendingTasks();
        
        System.out.println("Application state persisted");
    }
    
    private void saveApplicationState() {
        System.out.println("Application state saved");
    }
    
    private void saveUserSessions() {
        System.out.println("User sessions saved");
    }
    
    private void savePendingTasks() {
        System.out.println("Pending tasks saved");
    }
}

/**
 * Example 6: Shutdown Notifier
 */
@Component
class ShutdownNotifier {
    
    @EventListener
    public void notifyShutdown(ContextClosedEvent event) {
        System.out.println("Sending shutdown notifications...");
        
        notifyAdministrators();
        notifyMonitoringSystem();
        updateServiceRegistry();
        
        System.out.println("Shutdown notifications sent");
    }
    
    private void notifyAdministrators() {
        System.out.println("Administrators notified");
    }
    
    private void notifyMonitoringSystem() {
        System.out.println("Monitoring system notified");
    }
    
    private void updateServiceRegistry() {
        System.out.println("Service registry updated");
    }
}

/**
 * Example 7: Audit Logger
 */
@Component
class ShutdownAuditLogger {
    
    @EventListener
    public void logShutdown(ContextClosedEvent event) {
        long shutdownTime = System.currentTimeMillis();
        
        System.out.println("Logging shutdown event...");
        System.out.println("Shutdown time: " + new java.util.Date(shutdownTime));
        
        logActiveUsers();
        logPendingOperations();
        logSystemMetrics();
        
        System.out.println("Shutdown audit complete");
    }
    
    private void logActiveUsers() {
        System.out.println("Active users logged");
    }
    
    private void logPendingOperations() {
        System.out.println("Pending operations logged");
    }
    
    private void logSystemMetrics() {
        System.out.println("System metrics logged");
    }
}

/**
 * Usage Examples
 */
class ContextClosedEventUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Context Closed Event Pattern");
        System.out.println("=============================\n");
        
        System.out.println("Event Characteristics:");
        System.out.println("- Published before context shutdown");
        System.out.println("- Final cleanup opportunity");
        System.out.println("- Context cannot be restarted");
        System.out.println("- Triggered by close() or JVM shutdown\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Close database connections");
        System.out.println("2. Close file resources");
        System.out.println("3. Close network connections");
        System.out.println("4. Persist application state");
        System.out.println("5. Send shutdown notifications");
        System.out.println("6. Log shutdown audit trail");
    }
}
