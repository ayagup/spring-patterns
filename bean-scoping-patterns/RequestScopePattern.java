package com.spring.patterns.scope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Request Scope Pattern
 * 
 * Request scope creates a new bean instance for each HTTP request.
 * The bean is available throughout the request lifecycle and destroyed when request completes.
 * 
 * Characteristics:
 * - New instance per HTTP request
 * - Lifecycle tied to request
 * - Destroyed after request completes
 * - Available only in web-aware ApplicationContext
 * - Thread-safe (isolated per request)
 * 
 * Use Cases:
 * - Request-specific data
 * - Form backing objects
 * - Request correlation/tracking
 * - Per-request processing state
 * - Request-scoped services
 * 
 * Configuration:
 * - @RequestScope or @Scope(WebApplicationContext.SCOPE_REQUEST)
 * - Requires web application context
 * - Use proxyMode for injection into singletons
 */
@SpringBootApplication
public class RequestScopePattern {

    public static void main(String[] args) {
        SpringApplication.run(RequestScopePattern.class, args);
        System.out.println("\n=== Request Scope Pattern Started ===");
        System.out.println("Test endpoints:");
        System.out.println("  GET http://localhost:8080/api/request/info");
        System.out.println("  GET http://localhost:8080/api/request/counter");
        System.out.println("  POST http://localhost:8080/api/request/submit");
    }
}

/**
 * Configuration for request-scoped beans
 */
@Configuration
class RequestScopedConfig {
    
    /**
     * Using @RequestScope annotation (shorthand)
     */
    @Bean
    @RequestScope
    public RequestInfo requestInfo() {
        return new RequestInfo();
    }
    
    /**
     * Using @Scope with explicit configuration
     * proxyMode = TARGET_CLASS creates CGLIB proxy for injection into singletons
     */
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public RequestContext requestContext() {
        return new RequestContext();
    }
    
    /**
     * Request-scoped form object
     */
    @Bean
    @RequestScope
    public SubmissionForm submissionForm() {
        return new SubmissionForm();
    }
}

/**
 * Request-scoped bean for request information
 */
@Component
@RequestScope
class RequestInfo {
    private final String requestId;
    private final LocalDateTime timestamp;
    private final AtomicInteger accessCount = new AtomicInteger(0);
    
    public RequestInfo() {
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        System.out.println("RequestInfo created: " + requestId);
    }
    
    @PostConstruct
    public void init() {
        System.out.println("RequestInfo initialized: " + requestId);
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("RequestInfo destroyed: " + requestId + 
                         " (accessed " + accessCount.get() + " times)");
    }
    
    public void incrementAccess() {
        accessCount.incrementAndGet();
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public int getAccessCount() {
        return accessCount.get();
    }
}

/**
 * Request-scoped context for tracking request state
 */
class RequestContext {
    private final String correlationId;
    private final LocalDateTime startTime;
    private String userId;
    private String operation;
    
    public RequestContext() {
        this.correlationId = "REQ-" + UUID.randomUUID().toString().substring(0, 8);
        this.startTime = LocalDateTime.now();
        System.out.println("RequestContext created: " + correlationId);
    }
    
    @PreDestroy
    public void cleanup() {
        long duration = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
        System.out.println("RequestContext destroyed: " + correlationId + 
                         " (duration: " + duration + "ms)");
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
}

/**
 * Request-scoped form object
 */
class SubmissionForm {
    private final String formId;
    private String name;
    private String email;
    private String message;
    
    public SubmissionForm() {
        this.formId = "FORM-" + System.currentTimeMillis();
        System.out.println("SubmissionForm created: " + formId);
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("SubmissionForm destroyed: " + formId);
    }
    
    // Getters and setters
    public String getFormId() { return formId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

/**
 * Singleton service using request-scoped beans
 * Requires proxyMode to inject request-scoped beans into singleton
 */
@Component
class RequestProcessor {
    
    // Spring injects a proxy that delegates to current request's instance
    private final RequestInfo requestInfo;
    private final RequestContext requestContext;
    
    public RequestProcessor(RequestInfo requestInfo, RequestContext requestContext) {
        this.requestInfo = requestInfo;
        this.requestContext = requestContext;
        System.out.println("RequestProcessor created (singleton)");
    }
    
    public String processRequest(String userId, String operation) {
        requestContext.setUserId(userId);
        requestContext.setOperation(operation);
        requestInfo.incrementAccess();
        
        return "Processed request " + requestInfo.getRequestId() + 
               " (correlation: " + requestContext.getCorrelationId() + ")" +
               " for user: " + userId;
    }
    
    public String getRequestDetails() {
        return "Request ID: " + requestInfo.getRequestId() + 
               ", Correlation: " + requestContext.getCorrelationId() + 
               ", Started: " + requestContext.getStartTime() + 
               ", Access count: " + requestInfo.getAccessCount();
    }
}

/**
 * REST Controller demonstrating request scope
 */
@RestController
@RequestMapping("/api/request")
class RequestScopeController {
    
    private final RequestInfo requestInfo;
    private final RequestContext requestContext;
    private final RequestProcessor requestProcessor;
    private final SubmissionForm submissionForm;
    
    public RequestScopeController(RequestInfo requestInfo,
                                 RequestContext requestContext,
                                 RequestProcessor requestProcessor,
                                 SubmissionForm submissionForm) {
        this.requestInfo = requestInfo;
        this.requestContext = requestContext;
        this.requestProcessor = requestProcessor;
        this.submissionForm = submissionForm;
    }
    
    @GetMapping("/info")
    public String getRequestInfo(HttpServletRequest request) {
        requestInfo.incrementAccess();
        requestContext.setOperation("GET_INFO");
        
        return "Request Info: " + 
               "\n  Request ID: " + requestInfo.getRequestId() +
               "\n  Correlation ID: " + requestContext.getCorrelationId() +
               "\n  Timestamp: " + requestInfo.getTimestamp() +
               "\n  Access Count: " + requestInfo.getAccessCount() +
               "\n  Remote Addr: " + request.getRemoteAddr();
    }
    
    @GetMapping("/counter")
    public String getCounter() {
        requestInfo.incrementAccess();
        requestInfo.incrementAccess();
        requestInfo.incrementAccess();
        
        return "Access count for this request: " + requestInfo.getAccessCount() + 
               " (Request ID: " + requestInfo.getRequestId() + ")";
    }
    
    @PostMapping("/submit")
    public String submitForm(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String message) {
        submissionForm.setName(name);
        submissionForm.setEmail(email);
        submissionForm.setMessage(message);
        
        requestContext.setUserId(email);
        requestContext.setOperation("SUBMIT_FORM");
        
        return "Form submitted: " +
               "\n  Form ID: " + submissionForm.getFormId() +
               "\n  Name: " + submissionForm.getName() +
               "\n  Email: " + submissionForm.getEmail() +
               "\n  Correlation ID: " + requestContext.getCorrelationId();
    }
    
    @GetMapping("/process")
    public String processRequest(@RequestParam(defaultValue = "guest") String userId) {
        String result = requestProcessor.processRequest(userId, "PROCESS");
        return result + "\n" + requestProcessor.getRequestDetails();
    }
    
    @GetMapping("/details")
    public String getDetails() {
        return requestProcessor.getRequestDetails();
    }
    
    @GetMapping("/multiple-access")
    public String multipleAccess() {
        // All accesses within same request see same instance
        requestInfo.incrementAccess();
        String id1 = requestInfo.getRequestId();
        
        requestInfo.incrementAccess();
        String id2 = requestInfo.getRequestId();
        
        requestInfo.incrementAccess();
        String id3 = requestInfo.getRequestId();
        
        return "Same instance? " + (id1.equals(id2) && id2.equals(id3)) +
               "\n  Request ID: " + id1 +
               "\n  Total accesses: " + requestInfo.getAccessCount();
    }
}

/**
 * Key Points:
 * 
 * 1. Lifecycle:
 *    - Created at first access during request
 *    - Destroyed after request completes
 *    - @PostConstruct called on creation
 *    - @PreDestroy called on destruction
 * 
 * 2. Scoped Proxy:
 *    - Required when injecting into singleton beans
 *    - proxyMode = TARGET_CLASS (CGLIB proxy)
 *    - proxyMode = INTERFACES (JDK proxy)
 *    - Proxy delegates to current request's instance
 * 
 * 3. Thread Safety:
 *    - Each request has its own instance
 *    - No shared state between requests
 *    - Inherently thread-safe
 * 
 * 4. Annotations:
 *    - @RequestScope (shorthand)
 *    - @Scope(WebApplicationContext.SCOPE_REQUEST)
 *    - Both are equivalent
 * 
 * 5. Use Cases:
 *    ✓ Request correlation tracking
 *    ✓ Form backing objects
 *    ✓ Request-specific configuration
 *    ✓ Per-request logging context
 *    ✓ Request metrics collection
 * 
 * 6. Best Practices:
 *    - Always use proxyMode for singleton injection
 *    - Keep beans lightweight
 *    - Clean up resources in @PreDestroy
 *    - Avoid heavy initialization
 * 
 * 7. Testing:
 *    - Requires web application context
 *    - Use @WebMvcTest or @SpringBootTest
 *    - MockMvc for integration tests
 * 
 * 8. Performance:
 *    - Bean creation overhead per request
 *    - Proxy overhead (minimal)
 *    - Consider for request-specific state only
 */
