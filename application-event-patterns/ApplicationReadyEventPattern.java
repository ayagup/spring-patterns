package com.example.events;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Application Ready Event Pattern
 * ===============================
 * 
 * Demonstrates handling of ApplicationReadyEvent, published when the
 * application is ready to service requests. This is the last event
 * fired during startup, after all initialization is complete.
 * 
 * Key Concepts:
 * ------------
 * 1. ApplicationReadyEvent - Application fully started
 * 2. Post-Startup Actions - After application ready
 * 3. Final Initialization - Last initialization step
 * 4. Service Readiness - Signal ready to handle traffic
 * 5. Health Check Ready - Application healthy and ready
 * 
 * Event Timeline (Startup Order):
 * ------------------------------
 * 1. ApplicationStartingEvent - Very early, before context creation
 * 2. ApplicationEnvironmentPreparedEvent - Environment ready
 * 3. ApplicationContextInitializedEvent - Context created
 * 4. ApplicationPreparedEvent - Context loaded but not refreshed
 * 5. ContextRefreshedEvent - Context refreshed
 * 6. WebServerInitializedEvent - Web server started
 * 7. ApplicationStartedEvent - Context refreshed, before runners
 * 8. ApplicationReadyEvent - Application ready ← THIS EVENT
 * 
 * When to Use:
 * -----------
 * - Enable health check endpoint
 * - Register with service discovery
 * - Start accepting requests
 * - Send startup notifications
 * - Initialize non-critical features
 * - Start background monitoring
 * - Log application readiness
 * - Trigger post-startup workflows
 * 
 * Important Notes:
 * ---------------
 * - Fires after ApplicationStartedEvent
 * - Fires after all CommandLineRunner and ApplicationRunner
 * - Application is fully ready to service requests
 * - Web server is running and accepting connections
 * - All auto-configuration is complete
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ApplicationReadyEventPattern {
    
    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        System.out.println("=== Application Ready ===");
        System.out.println("Application is fully started and ready to service requests");
        System.out.println("Timestamp: " + new java.util.Date());
        
        // Perform final initialization
        performFinalInitialization();
    }
    
    private void performFinalInitialization() {
        System.out.println("Final initialization complete");
    }
}

/**
 * Example 2: Health Check Enabler
 */
@Component
class HealthCheckEnabler {
    
    private volatile boolean ready = false;
    
    @EventListener
    public void enableHealthCheck(ApplicationReadyEvent event) {
        System.out.println("Enabling health check endpoint...");
        
        // Mark application as ready for health checks
        ready = true;
        
        System.out.println("Health check endpoint enabled");
        System.out.println("Application is now reporting as HEALTHY");
    }
    
    public boolean isReady() {
        return ready;
    }
}

/**
 * Example 3: Service Discovery Registration
 */
@Component
class ServiceDiscoveryRegistration {
    
    @EventListener
    public void registerWithServiceDiscovery(ApplicationReadyEvent event) {
        System.out.println("Registering with service discovery...");
        
        registerWithEureka();
        registerWithConsul();
        updateLoadBalancer();
        
        System.out.println("Service registration complete");
    }
    
    private void registerWithEureka() {
        System.out.println("Registered with Eureka");
    }
    
    private void registerWithConsul() {
        System.out.println("Registered with Consul");
    }
    
    private void updateLoadBalancer() {
        System.out.println("Load balancer updated - now accepting traffic");
    }
}

/**
 * Example 4: Startup Notification Sender
 */
@Component
class StartupNotificationSender {
    
    @EventListener
    public void sendStartupNotifications(ApplicationReadyEvent event) {
        System.out.println("Sending startup notifications...");
        
        notifyAdministrators();
        notifyMonitoringSystem();
        notifyDependentServices();
        logStartupMetrics();
    }
    
    private void notifyAdministrators() {
        System.out.println("Email sent to administrators: Application started successfully");
    }
    
    private void notifyMonitoringSystem() {
        System.out.println("Monitoring system notified: Application UP");
    }
    
    private void notifyDependentServices() {
        System.out.println("Dependent services notified: Service available");
    }
    
    private void logStartupMetrics() {
        long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        System.out.println("Startup time: " + uptime + "ms");
    }
}

/**
 * Example 5: Background Job Starter
 */
@Component
class BackgroundJobStarter {
    
    @EventListener
    public void startBackgroundJobs(ApplicationReadyEvent event) {
        System.out.println("Starting background jobs...");
        
        startDataSyncJob();
        startCleanupJob();
        startReportGenerationJob();
        startHealthCheckJob();
    }
    
    private void startDataSyncJob() {
        System.out.println("Data sync job started");
    }
    
    private void startCleanupJob() {
        System.out.println("Cleanup job started");
    }
    
    private void startReportGenerationJob() {
        System.out.println("Report generation job started");
    }
    
    private void startHealthCheckJob() {
        System.out.println("Health check job started");
    }
}

/**
 * Example 6: Feature Flag Activator
 */
@Component
class FeatureFlagActivator {
    
    @EventListener
    public void activateFeatures(ApplicationReadyEvent event) {
        System.out.println("Activating feature flags...");
        
        activateNewUserInterface();
        activateBetaFeatures();
        activateExperimentalAPIs();
        
        System.out.println("Feature flags activated");
    }
    
    private void activateNewUserInterface() {
        System.out.println("New UI feature activated");
    }
    
    private void activateBetaFeatures() {
        System.out.println("Beta features activated");
    }
    
    private void activateExperimentalAPIs() {
        System.out.println("Experimental APIs activated");
    }
}

/**
 * Example 7: Traffic Acceptance Controller
 */
@Component
class TrafficAcceptanceController {
    
    private volatile boolean acceptingTraffic = false;
    
    @EventListener
    public void startAcceptingTraffic(ApplicationReadyEvent event) {
        System.out.println("=== Starting Traffic Acceptance ===");
        
        // Perform final checks
        if (performReadinessChecks()) {
            acceptingTraffic = true;
            System.out.println("NOW ACCEPTING TRAFFIC");
            
            notifyLoadBalancer();
        } else {
            System.out.println("Readiness checks failed - NOT accepting traffic");
        }
    }
    
    private boolean performReadinessChecks() {
        System.out.println("Performing readiness checks...");
        System.out.println("  Database connection: OK");
        System.out.println("  Cache connection: OK");
        System.out.println("  External services: OK");
        return true;
    }
    
    private void notifyLoadBalancer() {
        System.out.println("Load balancer notified - routing traffic to this instance");
    }
    
    public boolean isAcceptingTraffic() {
        return acceptingTraffic;
    }
}

/**
 * Example 8: Monitoring System Initializer
 */
@Component
class MonitoringSystemInitializer {
    
    @EventListener
    public void initializeMonitoring(ApplicationReadyEvent event) {
        System.out.println("Initializing monitoring systems...");
        
        startMetricCollection();
        startLogAggregation();
        startAlertingSystem();
        startPerformanceMonitoring();
    }
    
    private void startMetricCollection() {
        System.out.println("Metric collection started - sending metrics to monitoring system");
    }
    
    private void startLogAggregation() {
        System.out.println("Log aggregation started - sending logs to central logging");
    }
    
    private void startAlertingSystem() {
        System.out.println("Alerting system started - ready to send alerts");
    }
    
    private void startPerformanceMonitoring() {
        System.out.println("Performance monitoring started - tracking request performance");
    }
}

/**
 * Example 9: Readiness Log Publisher
 */
@Component
class ReadinessLogPublisher {
    
    @EventListener
    public void publishReadinessLog(ApplicationReadyEvent event) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("APPLICATION READY");
        System.out.println("=".repeat(60));
        
        logApplicationInfo();
        logSystemInfo();
        logStartupTime();
        
        System.out.println("=".repeat(60) + "\n");
    }
    
    private void logApplicationInfo() {
        System.out.println("Application: Spring Boot Application");
        System.out.println("Version: 1.0.0");
        System.out.println("Environment: PRODUCTION");
    }
    
    private void logSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Available Processors: " + runtime.availableProcessors());
        System.out.println("Total Memory: " + runtime.totalMemory() / 1024 / 1024 + " MB");
        System.out.println("Free Memory: " + runtime.freeMemory() / 1024 / 1024 + " MB");
    }
    
    private void logStartupTime() {
        long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        System.out.println("Startup Time: " + uptime + "ms");
        System.out.println("Ready to serve requests!");
    }
}

/**
 * Usage Examples
 */
class ApplicationReadyEventUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Application Ready Event Pattern");
        System.out.println("================================\n");
        
        System.out.println("Event Characteristics:");
        System.out.println("- Last event in startup sequence");
        System.out.println("- Fires after ApplicationStartedEvent");
        System.out.println("- Fires after all runners complete");
        System.out.println("- Application fully ready for requests\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Enable health check endpoint");
        System.out.println("2. Register with service discovery");
        System.out.println("3. Start accepting traffic");
        System.out.println("4. Send startup notifications");
        System.out.println("5. Start background jobs");
        System.out.println("6. Activate feature flags");
        System.out.println("7. Initialize monitoring");
        System.out.println("8. Log readiness status\n");
        
        System.out.println("Startup Event Order:");
        System.out.println("1. ApplicationStartingEvent");
        System.out.println("2. ApplicationEnvironmentPreparedEvent");
        System.out.println("3. ApplicationContextInitializedEvent");
        System.out.println("4. ApplicationPreparedEvent");
        System.out.println("5. ContextRefreshedEvent");
        System.out.println("6. WebServerInitializedEvent");
        System.out.println("7. ApplicationStartedEvent");
        System.out.println("8. ApplicationReadyEvent ← READY TO SERVE");
    }
}
