package com.example.events;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Context Stopped Event Pattern
 * ==============================
 * 
 * Demonstrates handling of ContextStoppedEvent, which is published when
 * the ApplicationContext is stopped via stop() method.
 * 
 * Key Concepts:
 * ------------
 * 1. ContextStoppedEvent - Published on explicit stop
 * 2. Graceful Shutdown - Clean resource release
 * 3. Pause Operations - Temporarily halt processing
 * 4. SmartLifecycle - Integration with lifecycle beans
 * 5. Resumable State - Can be restarted with start()
 * 
 * When Event Fires:
 * ----------------
 * - ConfigurableApplicationContext.stop() called
 * - NOT during normal shutdown
 * - NOT during context close
 * - Only on explicit stop() invocation
 * - Before context restart
 * 
 * When to Use:
 * -----------
 * - Pause message consumption
 * - Disable scheduled tasks
 * - Stop accepting requests
 * - Release temporary resources
 * - Flush caches
 * - Checkpoint state
 * - Enter maintenance mode
 * 
 * Common Use Cases:
 * ----------------
 * - Graceful degradation
 * - Maintenance mode
 * - Traffic routing control
 * - Resource cleanup
 * - State persistence
 * - Listener pause
 * - Connection release
 * 
 * Important Notes:
 * ---------------
 * - Different from close() - context can restart
 * - Requires explicit stop() call
 * - Use with start() for lifecycle management
 * - Works with SmartLifecycle beans
 * - Clean shutdown vs. hard shutdown
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ContextStoppedEventPattern implements ApplicationListener<ContextStoppedEvent> {
    
    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {
        System.out.println("=== Context Stopped Event ===");
        System.out.println("Application context stopped: " + 
                         event.getApplicationContext().getDisplayName());
        
        // Pause operations
        pauseOperations();
    }
    
    private void pauseOperations() {
        System.out.println("Pausing application operations");
    }
}

/**
 * Example 2: Message Listener Stopper
 */
@Component
class MessageListenerStopper {
    
    @EventListener
    public void stopListening(ContextStoppedEvent event) {
        System.out.println("Stopping message listeners...");
        
        stopKafkaListeners();
        stopRabbitListeners();
        stopJmsListeners();
        
        System.out.println("Message listeners stopped");
    }
    
    private void stopKafkaListeners() {
        System.out.println("Stopped: Kafka listeners");
    }
    
    private void stopRabbitListeners() {
        System.out.println("Stopped: RabbitMQ listeners");
    }
    
    private void stopJmsListeners() {
        System.out.println("Stopped: JMS listeners");
    }
}

/**
 * Example 3: Scheduled Task Disabler
 */
@Component
class ScheduledTaskDisabler {
    
    @EventListener
    public void disableScheduledTasks(ContextStoppedEvent event) {
        System.out.println("Disabling scheduled tasks...");
        
        disableDataCleanupTask();
        disableReportGenerationTask();
        disableHealthCheckTask();
        
        System.out.println("Scheduled tasks disabled");
    }
    
    private void disableDataCleanupTask() {
        System.out.println("Disabled: Data cleanup task");
    }
    
    private void disableReportGenerationTask() {
        System.out.println("Disabled: Report generation task");
    }
    
    private void disableHealthCheckTask() {
        System.out.println("Disabled: Health check task");
    }
}

/**
 * Example 4: Traffic Router Stopper
 */
@Component
class TrafficRouterStopper {
    
    @EventListener
    public void disableTraffic(ContextStoppedEvent event) {
        System.out.println("Disabling traffic routing...");
        
        // Deregister from load balancer
        deregisterFromLoadBalancer();
        
        // Stop accepting new requests
        stopAcceptingRequests();
        
        System.out.println("Traffic routing disabled");
    }
    
    private void deregisterFromLoadBalancer() {
        System.out.println("Deregistered from load balancer");
    }
    
    private void stopAcceptingRequests() {
        System.out.println("Stopped accepting new requests");
    }
}

/**
 * Example 5: Cache Flusher
 */
@Component
class CacheFlusher {
    
    @EventListener
    public void flushCaches(ContextStoppedEvent event) {
        System.out.println("Flushing caches...");
        
        flushUserCache();
        flushProductCache();
        flushSessionCache();
        
        System.out.println("Caches flushed");
    }
    
    private void flushUserCache() {
        System.out.println("Flushed: User cache");
    }
    
    private void flushProductCache() {
        System.out.println("Flushed: Product cache");
    }
    
    private void flushSessionCache() {
        System.out.println("Flushed: Session cache");
    }
}

/**
 * Example 6: State Checkpoint Manager
 */
@Component
class StateCheckpointManager {
    
    @EventListener
    public void checkpointState(ContextStoppedEvent event) {
        System.out.println("Creating state checkpoint...");
        
        saveApplicationState();
        saveProcessingQueue();
        saveActiveTransactions();
        
        System.out.println("State checkpoint created");
    }
    
    private void saveApplicationState() {
        System.out.println("Saved: Application state");
    }
    
    private void saveProcessingQueue() {
        System.out.println("Saved: Processing queue");
    }
    
    private void saveActiveTransactions() {
        System.out.println("Saved: Active transactions");
    }
}

/**
 * Example 7: Connection Pool Releaser
 */
@Component
class ConnectionPoolReleaser {
    
    @EventListener
    public void releaseConnectionPools(ContextStoppedEvent event) {
        System.out.println("Releasing connection pools...");
        
        releaseDatabaseConnections();
        releaseCacheConnections();
        releaseMessagingConnections();
        
        System.out.println("Connection pools released");
    }
    
    private void releaseDatabaseConnections() {
        System.out.println("Released: Database connections");
    }
    
    private void releaseCacheConnections() {
        System.out.println("Released: Cache connections");
    }
    
    private void releaseMessagingConnections() {
        System.out.println("Released: Messaging connections");
    }
}

/**
 * Example 8: Monitoring Stopper
 */
@Component
class MonitoringStopper {
    
    @EventListener
    public void stopMonitoring(ContextStoppedEvent event) {
        System.out.println("Stopping monitoring services...");
        
        stopMetricCollection();
        stopHealthChecks();
        stopAlertSystem();
        
        System.out.println("Monitoring services stopped");
    }
    
    private void stopMetricCollection() {
        System.out.println("Stopped: Metric collection");
    }
    
    private void stopHealthChecks() {
        System.out.println("Stopped: Health checks");
    }
    
    private void stopAlertSystem() {
        System.out.println("Stopped: Alert system");
    }
}

/**
 * Example 9: Background Job Stopper
 */
@Component
class BackgroundJobStopper {
    
    @EventListener
    public void stopBackgroundJobs(ContextStoppedEvent event) {
        System.out.println("Stopping background jobs...");
        
        stopDataSyncJob();
        stopCacheRefreshJob();
        stopReportJob();
        
        System.out.println("Background jobs stopped");
    }
    
    private void stopDataSyncJob() {
        System.out.println("Stopped: Data synchronization job");
    }
    
    private void stopCacheRefreshJob() {
        System.out.println("Stopped: Cache refresh job");
    }
    
    private void stopReportJob() {
        System.out.println("Stopped: Report generation job");
    }
}

/**
 * Example 10: Maintenance Mode Activator
 */
@Component
class MaintenanceModeActivator {
    
    @EventListener
    public void enterMaintenanceMode(ContextStoppedEvent event) {
        System.out.println("Entering maintenance mode...");
        
        displayMaintenancePage();
        notifyAdministrators();
        logMaintenanceStart();
        
        System.out.println("Maintenance mode activated");
    }
    
    private void displayMaintenancePage() {
        System.out.println("Maintenance page displayed to users");
    }
    
    private void notifyAdministrators() {
        System.out.println("Administrators notified");
    }
    
    private void logMaintenanceStart() {
        System.out.println("Maintenance start time logged");
    }
}

/**
 * Usage Examples
 */
class ContextStoppedEventUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Context Stopped Event Pattern");
        System.out.println("==============================\n");
        
        System.out.println("Event Characteristics:");
        System.out.println("- Published on explicit stop() call");
        System.out.println("- NOT during normal shutdown");
        System.out.println("- Context can be restarted");
        System.out.println("- Used with start() for lifecycle management\n");
        
        System.out.println("Triggering the Event:");
        System.out.println("  ConfigurableApplicationContext context = ...");
        System.out.println("  context.stop(); // Publishes ContextStoppedEvent\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Stop message listeners");
        System.out.println("2. Disable scheduled tasks");
        System.out.println("3. Stop accepting traffic");
        System.out.println("4. Flush caches");
        System.out.println("5. Checkpoint application state");
        System.out.println("6. Release connections");
        System.out.println("7. Stop monitoring");
        System.out.println("8. Pause background jobs");
        System.out.println("9. Enter maintenance mode\n");
        
        System.out.println("Lifecycle Comparison:");
        System.out.println("- stop()   -> ContextStoppedEvent  (Resumable)");
        System.out.println("- close()  -> ContextClosedEvent   (Final)");
        System.out.println("- start()  -> ContextStartedEvent  (Resume)");
    }
}
