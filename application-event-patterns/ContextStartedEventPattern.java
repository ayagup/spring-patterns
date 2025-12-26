package com.example.events;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Context Started Event Pattern
 * ==============================
 * 
 * Demonstrates handling of ContextStartedEvent, which is published when
 * the ApplicationContext is started via start() method.
 * 
 * Key Concepts:
 * ------------
 * 1. ContextStartedEvent - Published on explicit start
 * 2. Lifecycle Management - Context lifecycle control
 * 3. Manual Triggering - Not automatic like refresh
 * 4. SmartLifecycle - Integration with lifecycle beans
 * 5. Start Callback - Resume operations
 * 
 * When Event Fires:
 * ----------------
 * - ConfigurableApplicationContext.start() called
 * - NOT during normal application startup
 * - NOT during context refresh
 * - Only on explicit start() invocation
 * - After context has been stopped
 * 
 * When to Use:
 * -----------
 * - Resume paused operations
 * - Restart background tasks
 * - Re-enable scheduled jobs
 * - Reconnect to resources
 * - Resume message consumption
 * - Start monitoring
 * - Enable traffic handling
 * 
 * Common Use Cases:
 * ----------------
 * - Start/stop message listeners
 * - Pause/resume scheduled tasks
 * - Traffic routing control
 * - Maintenance mode toggle
 * - Resource connection management
 * - Graceful start after stop
 * 
 * Important Notes:
 * ---------------
 * - Not published automatically on startup
 * - Requires explicit start() call
 * - Used with stop() for lifecycle management
 * - Works with SmartLifecycle beans
 * - Parent context start doesn't trigger child
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ContextStartedEventPattern implements ApplicationListener<ContextStartedEvent> {
    
    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        System.out.println("=== Context Started Event ===");
        System.out.println("Application context started: " + 
                         event.getApplicationContext().getDisplayName());
        
        // Resume operations
        resumeOperations();
    }
    
    private void resumeOperations() {
        System.out.println("Resuming application operations");
    }
}

/**
 * Example 2: Message Listener Controller
 */
@Component
class MessageListenerController {
    
    private boolean listening = false;
    
    @EventListener
    public void startListening(ContextStartedEvent event) {
        if (!listening) {
            System.out.println("Starting message listeners...");
            
            startKafkaListeners();
            startRabbitListeners();
            startJmsListeners();
            
            listening = true;
            System.out.println("Message listeners started");
        }
    }
    
    private void startKafkaListeners() {
        System.out.println("Started: Kafka listeners");
    }
    
    private void startRabbitListeners() {
        System.out.println("Started: RabbitMQ listeners");
    }
    
    private void startJmsListeners() {
        System.out.println("Started: JMS listeners");
    }
}

/**
 * Example 3: Scheduled Task Controller
 */
@Component
class ScheduledTaskController {
    
    @EventListener
    public void startScheduledTasks(ContextStartedEvent event) {
        System.out.println("Starting scheduled tasks...");
        
        enableDataCleanupTask();
        enableReportGenerationTask();
        enableHealthCheckTask();
        
        System.out.println("Scheduled tasks started");
    }
    
    private void enableDataCleanupTask() {
        System.out.println("Enabled: Data cleanup task");
    }
    
    private void enableReportGenerationTask() {
        System.out.println("Enabled: Report generation task");
    }
    
    private void enableHealthCheckTask() {
        System.out.println("Enabled: Health check task");
    }
}

/**
 * Example 4: Connection Pool Manager
 */
@Component
class ConnectionPoolManager {
    
    @EventListener
    public void startConnectionPools(ContextStartedEvent event) {
        System.out.println("Starting connection pools...");
        
        startDatabasePool();
        startCachePool();
        startMessagingPool();
        
        System.out.println("Connection pools started");
    }
    
    private void startDatabasePool() {
        System.out.println("Started: Database connection pool");
    }
    
    private void startCachePool() {
        System.out.println("Started: Cache connection pool");
    }
    
    private void startMessagingPool() {
        System.out.println("Started: Messaging connection pool");
    }
}

/**
 * Example 5: Traffic Router
 */
@Component
class TrafficRouter {
    
    private boolean acceptingTraffic = false;
    
    @EventListener
    public void enableTraffic(ContextStartedEvent event) {
        System.out.println("Enabling traffic routing...");
        
        // Register with load balancer
        registerWithLoadBalancer();
        
        // Enable request handling
        acceptingTraffic = true;
        
        System.out.println("Traffic routing enabled - accepting requests");
    }
    
    private void registerWithLoadBalancer() {
        System.out.println("Registered with load balancer");
    }
    
    public boolean isAcceptingTraffic() {
        return acceptingTraffic;
    }
}

/**
 * Example 6: Monitoring Service
 */
@Component
class MonitoringService {
    
    @EventListener
    public void startMonitoring(ContextStartedEvent event) {
        System.out.println("Starting monitoring services...");
        
        startMetricCollection();
        startHealthChecks();
        startAlertSystem();
        
        System.out.println("Monitoring services started");
    }
    
    private void startMetricCollection() {
        System.out.println("Started: Metric collection");
    }
    
    private void startHealthChecks() {
        System.out.println("Started: Health checks");
    }
    
    private void startAlertSystem() {
        System.out.println("Started: Alert system");
    }
}

/**
 * Example 7: Background Job Manager
 */
@Component
class BackgroundJobManager {
    
    @EventListener
    public void startBackgroundJobs(ContextStartedEvent event) {
        System.out.println("Starting background jobs...");
        
        startDataSyncJob();
        startCacheRefreshJob();
        startReportJob();
        
        System.out.println("Background jobs started");
    }
    
    private void startDataSyncJob() {
        System.out.println("Started: Data synchronization job");
    }
    
    private void startCacheRefreshJob() {
        System.out.println("Started: Cache refresh job");
    }
    
    private void startReportJob() {
        System.out.println("Started: Report generation job");
    }
}

/**
 * Example 8: Resource Reconnector
 */
@Component
class ResourceReconnector {
    
    @EventListener
    public void reconnectResources(ContextStartedEvent event) {
        System.out.println("Reconnecting to external resources...");
        
        reconnectToDatabase();
        reconnectToCache();
        reconnectToMessageBroker();
        
        System.out.println("Resources reconnected");
    }
    
    private void reconnectToDatabase() {
        System.out.println("Reconnected: Database");
    }
    
    private void reconnectToCache() {
        System.out.println("Reconnected: Cache");
    }
    
    private void reconnectToMessageBroker() {
        System.out.println("Reconnected: Message broker");
    }
}

/**
 * Example 9: Maintenance Mode Controller
 */
@Component
class MaintenanceModeController {
    
    private boolean maintenanceMode = false;
    
    @EventListener
    public void exitMaintenanceMode(ContextStartedEvent event) {
        if (maintenanceMode) {
            System.out.println("Exiting maintenance mode...");
            
            enableAllServices();
            notifyUsers();
            
            maintenanceMode = false;
            System.out.println("Maintenance mode exited - services online");
        }
    }
    
    private void enableAllServices() {
        System.out.println("All services enabled");
    }
    
    private void notifyUsers() {
        System.out.println("Users notified: Services online");
    }
}

/**
 * Example 10: Lifecycle Coordinator
 */
@Component
class LifecycleCoordinator {
    
    private long startTime;
    
    @EventListener
    public void coordinateStart(ContextStartedEvent event) {
        startTime = System.currentTimeMillis();
        
        System.out.println("=== Lifecycle Coordinator ===");
        System.out.println("Context started at: " + new java.util.Date(startTime));
        
        // Coordinate startup sequence
        phase1_CoreServices();
        phase2_Messaging();
        phase3_ExternalIntegrations();
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Startup completed in " + duration + "ms");
    }
    
    private void phase1_CoreServices() {
        System.out.println("Phase 1: Starting core services");
    }
    
    private void phase2_Messaging() {
        System.out.println("Phase 2: Starting messaging");
    }
    
    private void phase3_ExternalIntegrations() {
        System.out.println("Phase 3: Starting external integrations");
    }
}

/**
 * Usage Examples
 */
class ContextStartedEventUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Context Started Event Pattern");
        System.out.println("==============================\n");
        
        System.out.println("Event Characteristics:");
        System.out.println("- NOT automatic on application startup");
        System.out.println("- Requires explicit start() call");
        System.out.println("- Used with stop() for lifecycle management");
        System.out.println("- Works with SmartLifecycle beans\n");
        
        System.out.println("Triggering the Event:");
        System.out.println("  ConfigurableApplicationContext context = ...");
        System.out.println("  context.start(); // Publishes ContextStartedEvent\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Resume message listeners");
        System.out.println("2. Enable scheduled tasks");
        System.out.println("3. Start connection pools");
        System.out.println("4. Enable traffic routing");
        System.out.println("5. Start monitoring services");
        System.out.println("6. Resume background jobs");
        System.out.println("7. Reconnect to external resources");
        System.out.println("8. Exit maintenance mode\n");
        
        System.out.println("Lifecycle Methods:");
        System.out.println("- start()  -> ContextStartedEvent");
        System.out.println("- stop()   -> ContextStoppedEvent");
        System.out.println("- close()  -> ContextClosedEvent");
        System.out.println("- refresh() -> ContextRefreshedEvent");
    }
}
