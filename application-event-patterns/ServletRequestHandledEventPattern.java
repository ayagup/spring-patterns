package com.example.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

/**
 * Servlet Request Handled Event Pattern
 * =====================================
 * 
 * Demonstrates handling of ServletRequestHandledEvent, a subclass of
 * RequestHandledEvent that includes additional servlet-specific information
 * like HTTP status code and method.
 * 
 * Key Concepts:
 * ------------
 * 1. ServletRequestHandledEvent - Servlet request completion event
 * 2. HTTP Status Tracking - Monitor HTTP response codes
 * 3. HTTP Method Tracking - Track GET, POST, PUT, DELETE, etc.
 * 4. Servlet Context - Servlet-specific information
 * 5. HTTP Metrics - HTTP-level performance metrics
 * 
 * Event Information (extends RequestHandledEvent):
 * ------------------------------------------------
 * - Session ID
 * - Request URL
 * - Client address
 * - Processing time
 * - User principal
 * - HTTP status code (200, 404, 500, etc.)
 * - HTTP method (GET, POST, PUT, DELETE)
 * - Failure cause
 * 
 * When to Use:
 * -----------
 * - HTTP status code monitoring
 * - REST API analytics
 * - Error rate tracking (4xx, 5xx)
 * - Method-specific logging
 * - HTTP performance metrics
 * - API usage patterns
 * - Security monitoring
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ServletRequestHandledEventPattern {
    
    @EventListener
    public void handleServletRequest(ServletRequestHandledEvent event) {
        System.out.println("=== Servlet Request Handled ===");
        System.out.println("Method: " + event.getMethod());
        System.out.println("URL: " + event.getRequestUrl());
        System.out.println("Status: " + event.getStatusCode());
        System.out.println("Duration: " + event.getProcessingTimeMillis() + "ms");
        System.out.println("User: " + event.getUserName());
        System.out.println("Client: " + event.getClientAddress());
        
        if (event.wasFailure()) {
            System.out.println("Failure: " + event.getFailureCause());
        }
    }
}

/**
 * Example 2: HTTP Status Monitor
 */
@Component
class HTTPStatusMonitor {
    
    @EventListener
    public void monitorStatus(ServletRequestHandledEvent event) {
        int status = event.getStatusCode();
        
        if (status >= 500) {
            System.out.println("SERVER ERROR (5xx):");
            System.out.println("  Method: " + event.getMethod());
            System.out.println("  URL: " + event.getRequestUrl());
            System.out.println("  Status: " + status);
            System.out.println("  Cause: " + event.getFailureCause());
            
            alertServerError(event);
        } else if (status >= 400) {
            System.out.println("CLIENT ERROR (4xx):");
            System.out.println("  Method: " + event.getMethod());
            System.out.println("  URL: " + event.getRequestUrl());
            System.out.println("  Status: " + status);
            
            trackClientError(event);
        } else if (status >= 200 && status < 300) {
            System.out.println("SUCCESS (2xx): " + event.getMethod() + " " + 
                             event.getRequestUrl());
        }
    }
    
    private void alertServerError(ServletRequestHandledEvent event) {
        System.out.println("Server error alert sent");
    }
    
    private void trackClientError(ServletRequestHandledEvent event) {
        System.out.println("Client error tracked");
    }
}

/**
 * Example 3: REST API Analytics
 */
@Component
class RESTAPIAnalytics {
    
    private final java.util.Map<String, Integer> methodCounts = 
        new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<Integer, Integer> statusCounts = 
        new java.util.concurrent.ConcurrentHashMap<>();
    
    @EventListener
    public void trackAPIUsage(ServletRequestHandledEvent event) {
        // Track HTTP method usage
        String method = event.getMethod();
        methodCounts.merge(method, 1, Integer::sum);
        
        // Track status code distribution
        int status = event.getStatusCode();
        statusCounts.merge(status, 1, Integer::sum);
        
        System.out.println("API Analytics:");
        System.out.println("  Method counts: " + methodCounts);
        System.out.println("  Status counts: " + statusCounts);
    }
}

/**
 * Example 4: HTTP Method Performance Tracker
 */
@Component
class HTTPMethodPerformanceTracker {
    
    private final java.util.Map<String, java.util.List<Long>> methodDurations = 
        new java.util.concurrent.ConcurrentHashMap<>();
    
    @EventListener
    public void trackMethodPerformance(ServletRequestHandledEvent event) {
        String method = event.getMethod();
        long duration = event.getProcessingTimeMillis();
        
        methodDurations
            .computeIfAbsent(method, k -> new java.util.ArrayList<>())
            .add(duration);
        
        // Calculate average for each method
        double average = methodDurations.get(method).stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        System.out.println(method + " average duration: " + average + "ms");
    }
}

/**
 * Example 5: Error Rate Monitor
 */
@Component
class ErrorRateMonitor {
    
    private int totalRequests = 0;
    private int errorRequests = 0;
    
    @EventListener
    public void monitorErrorRate(ServletRequestHandledEvent event) {
        totalRequests++;
        
        if (event.getStatusCode() >= 400) {
            errorRequests++;
        }
        
        double errorRate = (double) errorRequests / totalRequests * 100;
        
        System.out.println("Error Rate: " + String.format("%.2f%%", errorRate) +
                         " (" + errorRequests + "/" + totalRequests + ")");
        
        if (errorRate > 10.0) {
            System.out.println("WARNING: High error rate detected!");
            alertHighErrorRate(errorRate);
        }
    }
    
    private void alertHighErrorRate(double rate) {
        System.out.println("High error rate alert sent: " + rate + "%");
    }
}

/**
 * Example 6: Endpoint Usage Pattern Analyzer
 */
@Component
class EndpointUsagePatternAnalyzer {
    
    private final java.util.Map<String, java.util.Map<String, Integer>> endpointMethodCounts = 
        new java.util.concurrent.ConcurrentHashMap<>();
    
    @EventListener
    public void analyzeUsagePatterns(ServletRequestHandledEvent event) {
        String url = event.getRequestUrl();
        String method = event.getMethod();
        
        endpointMethodCounts
            .computeIfAbsent(url, k -> new java.util.concurrent.ConcurrentHashMap<>())
            .merge(method, 1, Integer::sum);
        
        System.out.println("Endpoint usage: " + url);
        System.out.println("  Methods: " + endpointMethodCounts.get(url));
    }
}

/**
 * Example 7: Security Event Monitor
 */
@Component
class SecurityEventMonitor {
    
    private final java.util.Map<String, Integer> unauthorizedAttempts = 
        new java.util.concurrent.ConcurrentHashMap<>();
    
    @EventListener
    public void monitorSecurity(ServletRequestHandledEvent event) {
        int status = event.getStatusCode();
        
        if (status == 401 || status == 403) {
            String client = event.getClientAddress();
            unauthorizedAttempts.merge(client, 1, Integer::sum);
            
            System.out.println("SECURITY EVENT:");
            System.out.println("  Client: " + client);
            System.out.println("  Status: " + status);
            System.out.println("  URL: " + event.getRequestUrl());
            System.out.println("  Total attempts: " + unauthorizedAttempts.get(client));
            
            if (unauthorizedAttempts.get(client) > 5) {
                System.out.println("WARNING: Multiple unauthorized attempts from " + client);
                blockSuspiciousClient(client);
            }
        }
    }
    
    private void blockSuspiciousClient(String client) {
        System.out.println("Blocking client: " + client);
    }
}

/**
 * Example 8: API Response Code Statistics
 */
@Component
class APIResponseCodeStatistics {
    
    @EventListener
    public void collectStatistics(ServletRequestHandledEvent event) {
        int status = event.getStatusCode();
        String category = getStatusCategory(status);
        
        System.out.println("Response: " + event.getMethod() + " " + 
                         event.getRequestUrl() + " -> " + status + " (" + category + ")");
        
        recordStatistic(category, status, event);
    }
    
    private String getStatusCategory(int status) {
        if (status >= 200 && status < 300) return "Success";
        if (status >= 300 && status < 400) return "Redirection";
        if (status >= 400 && status < 500) return "Client Error";
        if (status >= 500) return "Server Error";
        return "Unknown";
    }
    
    private void recordStatistic(String category, int status, ServletRequestHandledEvent event) {
        System.out.println("Statistic recorded: " + category + " - " + status);
    }
}

/**
 * Usage Examples
 */
class ServletRequestHandledEventUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Servlet Request Handled Event Pattern");
        System.out.println("======================================\n");
        
        System.out.println("Additional Servlet Information:");
        System.out.println("- HTTP status code (200, 404, 500, etc.)");
        System.out.println("- HTTP method (GET, POST, PUT, DELETE)");
        System.out.println("- All RequestHandledEvent information\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. HTTP status code monitoring");
        System.out.println("2. REST API analytics");
        System.out.println("3. Error rate tracking (4xx, 5xx)");
        System.out.println("4. Method-specific performance");
        System.out.println("5. Security event monitoring");
        System.out.println("6. Endpoint usage patterns");
        System.out.println("7. Response code statistics");
        
        System.out.println("\nStatus Code Categories:");
        System.out.println("- 2xx: Success");
        System.out.println("- 3xx: Redirection");
        System.out.println("- 4xx: Client Error");
        System.out.println("- 5xx: Server Error");
    }
}
