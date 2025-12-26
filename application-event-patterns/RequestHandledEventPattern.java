package com.example.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.RequestHandledEvent;

/**
 * Request Handled Event Pattern
 * ==============================
 * 
 * Demonstrates handling of RequestHandledEvent, published after a web
 * request has been processed by the DispatcherServlet.
 * 
 * Key Concepts:
 * ------------
 * 1. RequestHandledEvent - Web request completion event
 * 2. Request Tracking - Monitor request processing
 * 3. Performance Metrics - Request duration tracking
 * 4. Audit Logging - Request audit trail
 * 5. Post-Request Actions - After request processing
 * 
 * Event Information:
 * -----------------
 * - Session ID
 * - Request URL
 * - Client address
 * - Processing time (milliseconds)
 * - User principal
 * - Status code (if servlet-specific)
 * - Failure cause (if any)
 * 
 * When to Use:
 * -----------
 * - Request logging/auditing
 * - Performance monitoring
 * - Usage analytics
 * - Security auditing
 * - SLA monitoring
 * - Error tracking
 * - User activity tracking
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class RequestHandledEventPattern {
    
    @EventListener
    public void handleRequestComplete(RequestHandledEvent event) {
        System.out.println("=== Request Handled ===");
        System.out.println("URL: " + event.getRequestUrl());
        System.out.println("Session: " + event.getSessionId());
        System.out.println("Client: " + event.getClientAddress());
        System.out.println("Duration: " + event.getProcessingTimeMillis() + "ms");
        System.out.println("User: " + event.getUserName());
        
        if (event.wasFailure()) {
            System.out.println("Status: FAILED");
            System.out.println("Cause: " + event.getFailureCause());
        } else {
            System.out.println("Status: SUCCESS");
        }
    }
}

/**
 * Example 2: Performance Monitor
 */
@Component
class PerformanceMonitor {
    
    private static final long SLOW_REQUEST_THRESHOLD = 1000; // 1 second
    
    @EventListener
    public void monitorPerformance(RequestHandledEvent event) {
        long duration = event.getProcessingTimeMillis();
        
        if (duration > SLOW_REQUEST_THRESHOLD) {
            System.out.println("SLOW REQUEST DETECTED:");
            System.out.println("  URL: " + event.getRequestUrl());
            System.out.println("  Duration: " + duration + "ms");
            System.out.println("  Threshold: " + SLOW_REQUEST_THRESHOLD + "ms");
            
            // Alert or log slow requests
            alertSlowRequest(event);
        }
    }
    
    private void alertSlowRequest(RequestHandledEvent event) {
        System.out.println("Slow request alert sent");
    }
}

/**
 * Example 3: Request Audit Logger
 */
@Component
class RequestAuditLogger {
    
    @EventListener
    public void auditRequest(RequestHandledEvent event) {
        String auditEntry = String.format(
            "Audit: user=%s, url=%s, client=%s, duration=%dms, status=%s",
            event.getUserName() != null ? event.getUserName() : "anonymous",
            event.getRequestUrl(),
            event.getClientAddress(),
            event.getProcessingTimeMillis(),
            event.wasFailure() ? "FAILED" : "SUCCESS"
        );
        
        System.out.println(auditEntry);
        
        // Persist to audit database
        persistAuditEntry(auditEntry);
    }
    
    private void persistAuditEntry(String entry) {
        // Save to database
    }
}

/**
 * Example 4: Usage Analytics Collector
 */
@Component
class UsageAnalyticsCollector {
    
    @EventListener
    public void collectAnalytics(RequestHandledEvent event) {
        System.out.println("Collecting usage analytics...");
        
        recordEndpointUsage(event.getRequestUrl());
        recordResponseTime(event.getProcessingTimeMillis());
        recordUserActivity(event.getUserName());
        recordClientActivity(event.getClientAddress());
    }
    
    private void recordEndpointUsage(String url) {
        System.out.println("Endpoint usage recorded: " + url);
    }
    
    private void recordResponseTime(long duration) {
        System.out.println("Response time recorded: " + duration + "ms");
    }
    
    private void recordUserActivity(String user) {
        System.out.println("User activity recorded: " + user);
    }
    
    private void recordClientActivity(String client) {
        System.out.println("Client activity recorded: " + client);
    }
}

/**
 * Example 5: Error Tracker
 */
@Component
class ErrorTracker {
    
    @EventListener
    public void trackErrors(RequestHandledEvent event) {
        if (event.wasFailure()) {
            System.out.println("=== REQUEST ERROR ===");
            System.out.println("URL: " + event.getRequestUrl());
            System.out.println("User: " + event.getUserName());
            System.out.println("Cause: " + event.getFailureCause());
            
            // Track error for monitoring
            trackError(event);
            
            // Send alert if critical
            if (isCriticalError(event)) {
                sendErrorAlert(event);
            }
        }
    }
    
    private void trackError(RequestHandledEvent event) {
        System.out.println("Error tracked in monitoring system");
    }
    
    private boolean isCriticalError(RequestHandledEvent event) {
        return event.getRequestUrl().contains("/api/");
    }
    
    private void sendErrorAlert(RequestHandledEvent event) {
        System.out.println("Critical error alert sent");
    }
}

/**
 * Example 6: SLA Monitor
 */
@Component
class SLAMonitor {
    
    private static final java.util.Map<String, Long> SLA_THRESHOLDS = 
        java.util.Map.of(
            "/api/", 500L,
            "/admin/", 1000L,
            "/reports/", 5000L
        );
    
    @EventListener
    public void monitorSLA(RequestHandledEvent event) {
        String url = event.getRequestUrl();
        long duration = event.getProcessingTimeMillis();
        
        for (java.util.Map.Entry<String, Long> entry : SLA_THRESHOLDS.entrySet()) {
            if (url.startsWith(entry.getKey())) {
                if (duration > entry.getValue()) {
                    System.out.println("SLA VIOLATION:");
                    System.out.println("  Endpoint: " + url);
                    System.out.println("  Duration: " + duration + "ms");
                    System.out.println("  SLA: " + entry.getValue() + "ms");
                    
                    reportSLAViolation(url, duration, entry.getValue());
                }
                break;
            }
        }
    }
    
    private void reportSLAViolation(String url, long actual, long expected) {
        System.out.println("SLA violation reported");
    }
}

/**
 * Example 7: Session Activity Tracker
 */
@Component
class SessionActivityTracker {
    
    private final java.util.Map<String, java.util.List<String>> sessionActivity = 
        new java.util.concurrent.ConcurrentHashMap<>();
    
    @EventListener
    public void trackSessionActivity(RequestHandledEvent event) {
        String sessionId = event.getSessionId();
        if (sessionId != null) {
            sessionActivity
                .computeIfAbsent(sessionId, k -> new java.util.ArrayList<>())
                .add(event.getRequestUrl());
            
            System.out.println("Session activity: " + sessionId + 
                             " -> " + sessionActivity.get(sessionId).size() + " requests");
        }
    }
}

/**
 * Usage Examples
 */
class RequestHandledEventUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Request Handled Event Pattern");
        System.out.println("==============================\n");
        
        System.out.println("Event Information:");
        System.out.println("- Request URL");
        System.out.println("- Session ID");
        System.out.println("- Client address");
        System.out.println("- Processing time");
        System.out.println("- User principal");
        System.out.println("- Failure information\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Performance monitoring");
        System.out.println("2. Request audit logging");
        System.out.println("3. Usage analytics");
        System.out.println("4. Error tracking");
        System.out.println("5. SLA monitoring");
        System.out.println("6. Session activity tracking");
    }
}
