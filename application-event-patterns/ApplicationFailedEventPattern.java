package com.example.events;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Application Failed Event Pattern
 * ================================
 * 
 * Demonstrates handling of ApplicationFailedEvent, published when the
 * application fails to start. This event provides access to the startup
 * exception and allows for cleanup and error reporting.
 * 
 * Key Concepts:
 * ------------
 * 1. ApplicationFailedEvent - Application startup failure
 * 2. Failure Handling - Handle startup errors
 * 3. Error Reporting - Report startup failures
 * 4. Cleanup on Failure - Clean up partial initialization
 * 5. Failure Recovery - Attempt recovery or shutdown gracefully
 * 
 * Event Information:
 * -----------------
 * - ApplicationContext (may be null if not created)
 * - Exception that caused the failure
 * - SpringApplication instance
 * 
 * When Fired:
 * ----------
 * - Exception during application startup
 * - Bean creation failures
 * - Auto-configuration failures
 * - Database connection failures
 * - Missing required properties
 * - Port binding failures
 * 
 * When to Use:
 * -----------
 * - Log startup failures
 * - Send failure alerts
 * - Clean up partial initialization
 * - Report to monitoring systems
 * - Attempt graceful shutdown
 * - Debug startup issues
 * - Generate failure reports
 * 
 * Important Notes:
 * ---------------
 * - Context may be null (failed before creation)
 * - Application will shut down after event handling
 * - Use for cleanup and reporting only
 * - Cannot prevent application shutdown
 * - Last chance to report errors
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ApplicationFailedEventPattern {
    
    @EventListener
    public void handleApplicationFailed(ApplicationFailedEvent event) {
        System.err.println("=== APPLICATION STARTUP FAILED ===");
        
        Throwable exception = event.getException();
        System.err.println("Failure Cause: " + exception.getClass().getName());
        System.err.println("Error Message: " + exception.getMessage());
        
        // Print stack trace
        exception.printStackTrace();
        
        // Perform cleanup
        performEmergencyCleanup();
    }
    
    private void performEmergencyCleanup() {
        System.err.println("Performing emergency cleanup...");
    }
}

/**
 * Example 2: Failure Alert Sender
 */
@Component
class FailureAlertSender {
    
    @EventListener
    public void sendFailureAlert(ApplicationFailedEvent event) {
        System.err.println("Sending failure alerts...");
        
        Throwable exception = event.getException();
        
        notifyAdministrators(exception);
        notifyMonitoringSystem(exception);
        notifyOnCallTeam(exception);
        
        System.err.println("Failure alerts sent");
    }
    
    private void notifyAdministrators(Throwable exception) {
        System.err.println("Email sent to administrators:");
        System.err.println("  Subject: Application Startup Failed");
        System.err.println("  Error: " + exception.getMessage());
    }
    
    private void notifyMonitoringSystem(Throwable exception) {
        System.err.println("Monitoring system notified:");
        System.err.println("  Status: DOWN");
        System.err.println("  Reason: " + exception.getMessage());
    }
    
    private void notifyOnCallTeam(Throwable exception) {
        System.err.println("On-call team notified via SMS/Slack");
    }
}

/**
 * Example 3: Startup Failure Logger
 */
@Component
class StartupFailureLogger {
    
    @EventListener
    public void logFailure(ApplicationFailedEvent event) {
        System.err.println("=== STARTUP FAILURE LOG ===");
        System.err.println("Timestamp: " + new java.util.Date());
        
        Throwable exception = event.getException();
        
        logExceptionDetails(exception);
        logSystemInfo();
        logEnvironmentInfo();
        
        // Save to file
        saveFailureReport(exception);
    }
    
    private void logExceptionDetails(Throwable exception) {
        System.err.println("\nException Details:");
        System.err.println("  Type: " + exception.getClass().getName());
        System.err.println("  Message: " + exception.getMessage());
        
        if (exception.getCause() != null) {
            System.err.println("  Cause: " + exception.getCause().getMessage());
        }
    }
    
    private void logSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        System.err.println("\nSystem Information:");
        System.err.println("  Java Version: " + System.getProperty("java.version"));
        System.err.println("  OS: " + System.getProperty("os.name"));
        System.err.println("  Available Memory: " + runtime.freeMemory() / 1024 / 1024 + " MB");
    }
    
    private void logEnvironmentInfo() {
        System.err.println("\nEnvironment:");
        System.err.println("  User: " + System.getProperty("user.name"));
        System.err.println("  Working Dir: " + System.getProperty("user.dir"));
    }
    
    private void saveFailureReport(Throwable exception) {
        System.err.println("\nFailure report saved to: startup-failure.log");
    }
}

/**
 * Example 4: Resource Cleanup Handler
 */
@Component
class ResourceCleanupHandler {
    
    @EventListener
    public void cleanupResources(ApplicationFailedEvent event) {
        System.err.println("Cleaning up resources after startup failure...");
        
        closeDatabase();
        releaseFileHandles();
        shutdownThreadPools();
        releaseNetworkConnections();
        
        System.err.println("Resource cleanup complete");
    }
    
    private void closeDatabase() {
        System.err.println("Closing database connections...");
    }
    
    private void releaseFileHandles() {
        System.err.println("Releasing file handles...");
    }
    
    private void shutdownThreadPools() {
        System.err.println("Shutting down thread pools...");
    }
    
    private void releaseNetworkConnections() {
        System.err.println("Releasing network connections...");
    }
}

/**
 * Example 5: Failure Type Analyzer
 */
@Component
class FailureTypeAnalyzer {
    
    @EventListener
    public void analyzeFailure(ApplicationFailedEvent event) {
        Throwable exception = event.getException();
        
        System.err.println("Analyzing failure type...");
        
        if (isPortBindingFailure(exception)) {
            handlePortBindingFailure(exception);
        } else if (isDatabaseConnectionFailure(exception)) {
            handleDatabaseFailure(exception);
        } else if (isBeanCreationFailure(exception)) {
            handleBeanCreationFailure(exception);
        } else if (isConfigurationFailure(exception)) {
            handleConfigurationFailure(exception);
        } else {
            handleUnknownFailure(exception);
        }
    }
    
    private boolean isPortBindingFailure(Throwable exception) {
        return exception.getMessage() != null && 
               exception.getMessage().contains("Address already in use");
    }
    
    private boolean isDatabaseConnectionFailure(Throwable exception) {
        return exception.getMessage() != null && 
               (exception.getMessage().contains("database") || 
                exception.getMessage().contains("connection"));
    }
    
    private boolean isBeanCreationFailure(Throwable exception) {
        return exception.getClass().getName().contains("BeanCreationException");
    }
    
    private boolean isConfigurationFailure(Throwable exception) {
        return exception.getMessage() != null && 
               exception.getMessage().contains("property");
    }
    
    private void handlePortBindingFailure(Throwable exception) {
        System.err.println("PORT BINDING FAILURE:");
        System.err.println("  Another application is using the port");
        System.err.println("  Suggestion: Change server.port in application.properties");
    }
    
    private void handleDatabaseFailure(Throwable exception) {
        System.err.println("DATABASE CONNECTION FAILURE:");
        System.err.println("  Cannot connect to database");
        System.err.println("  Suggestion: Check database URL, credentials, and availability");
    }
    
    private void handleBeanCreationFailure(Throwable exception) {
        System.err.println("BEAN CREATION FAILURE:");
        System.err.println("  Failed to create a Spring bean");
        System.err.println("  Suggestion: Check bean dependencies and configuration");
    }
    
    private void handleConfigurationFailure(Throwable exception) {
        System.err.println("CONFIGURATION FAILURE:");
        System.err.println("  Missing or invalid configuration property");
        System.err.println("  Suggestion: Check application.properties");
    }
    
    private void handleUnknownFailure(Throwable exception) {
        System.err.println("UNKNOWN FAILURE:");
        System.err.println("  " + exception.getMessage());
    }
}

/**
 * Example 6: Service Registry Notifier
 */
@Component
class ServiceRegistryNotifier {
    
    @EventListener
    public void notifyServiceRegistry(ApplicationFailedEvent event) {
        System.err.println("Notifying service registry of failure...");
        
        deregisterFromEureka();
        deregisterFromConsul();
        updateLoadBalancer();
        
        System.err.println("Service registry notified");
    }
    
    private void deregisterFromEureka() {
        System.err.println("Deregistered from Eureka");
    }
    
    private void deregisterFromConsul() {
        System.err.println("Deregistered from Consul");
    }
    
    private void updateLoadBalancer() {
        System.err.println("Load balancer updated - instance marked as DOWN");
    }
}

/**
 * Example 7: Failure Metrics Collector
 */
@Component
class FailureMetricsCollector {
    
    @EventListener
    public void collectMetrics(ApplicationFailedEvent event) {
        System.err.println("Collecting failure metrics...");
        
        recordFailureCount();
        recordFailureType(event.getException());
        recordFailureTime();
        
        System.err.println("Failure metrics collected");
    }
    
    private void recordFailureCount() {
        System.err.println("Failure count incremented");
    }
    
    private void recordFailureType(Throwable exception) {
        System.err.println("Failure type recorded: " + exception.getClass().getSimpleName());
    }
    
    private void recordFailureTime() {
        System.err.println("Failure time recorded: " + new java.util.Date());
    }
}

/**
 * Example 8: Emergency Contact Notifier
 */
@Component
class EmergencyContactNotifier {
    
    @EventListener
    public void notifyEmergencyContacts(ApplicationFailedEvent event) {
        Throwable exception = event.getException();
        
        if (isCriticalFailure(exception)) {
            System.err.println("CRITICAL FAILURE - Notifying emergency contacts");
            
            sendSMSAlert(exception);
            makePhoneCall(exception);
            sendSlackMessage(exception);
        }
    }
    
    private boolean isCriticalFailure(Throwable exception) {
        // Production environment or critical service
        return true; // For demo purposes
    }
    
    private void sendSMSAlert(Throwable exception) {
        System.err.println("SMS alert sent to on-call engineer");
    }
    
    private void makePhoneCall(Throwable exception) {
        System.err.println("Automated phone call initiated");
    }
    
    private void sendSlackMessage(Throwable exception) {
        System.err.println("Slack message sent to #alerts channel:");
        System.err.println("  @here Production service failed to start!");
        System.err.println("  Error: " + exception.getMessage());
    }
}

/**
 * Usage Examples
 */
class ApplicationFailedEventUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Application Failed Event Pattern");
        System.out.println("=================================\n");
        
        System.out.println("Event Characteristics:");
        System.out.println("- Fired when application fails to start");
        System.out.println("- Context may be null");
        System.out.println("- Provides access to exception");
        System.out.println("- Application will shut down after event\n");
        
        System.out.println("Common Failure Types:");
        System.out.println("1. Port binding failures (address in use)");
        System.out.println("2. Database connection failures");
        System.out.println("3. Bean creation failures");
        System.out.println("4. Configuration failures (missing properties)");
        System.out.println("5. Auto-configuration failures\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Send failure alerts");
        System.out.println("2. Log failure details");
        System.out.println("3. Clean up resources");
        System.out.println("4. Analyze failure type");
        System.out.println("5. Notify service registry");
        System.out.println("6. Collect failure metrics");
        System.out.println("7. Emergency notifications\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Don't throw exceptions (won't prevent shutdown)");
        System.out.println("- Keep handlers quick and simple");
        System.out.println("- Log to external systems (app may not start)");
        System.out.println("- Send alerts to wake up on-call team");
        System.out.println("- Clean up any partial initialization");
    }
}
