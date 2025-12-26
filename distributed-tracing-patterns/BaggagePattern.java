package com.example.tracing;

import brave.Tracer;
import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig;
import org.springframework.stereotype.Service;

/**
 * Baggage Pattern
 * ===============
 * 
 * Demonstrates Baggage for propagating contextual information across
 * service boundaries in distributed tracing.
 * 
 * Key Concepts:
 * ------------
 * 1. Baggage - Key-value pairs propagated with trace
 * 2. BaggageField - Typed baggage accessor
 * 3. Propagation - Automatic header transmission
 * 4. Scope - Baggage tied to current span
 * 5. Use Cases - User ID, tenant ID, request context
 * 
 * Baggage vs Tags:
 * ---------------
 * - Tags: Metadata for single span (not propagated)
 * - Baggage: Context propagated across all services
 * 
 * Configuration:
 * -------------
 * spring.sleuth.baggage.remote-fields=user-id,tenant-id,correlation-id
 * spring.sleuth.baggage.local-fields=request-id
 * spring.sleuth.propagation.type=B3,W3C
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Baggage Usage
 */
@Service
class BasicBaggageExample {
    
    private final Tracer tracer;
    private final BaggageField userIdField;
    
    public BasicBaggageExample(Tracer tracer) {
        this.tracer = tracer;
        this.userIdField = BaggageField.create("user-id");
    }
    
    public void demonstrateBaggage(String userId) {
        var span = tracer.nextSpan().name("operation").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Set baggage
            userIdField.updateValue(userId);
            
            // Baggage is now available everywhere in trace
            System.out.println("User ID baggage set: " + userId);
            
            // Call other services (baggage propagates automatically)
            callServiceB();
            
        } finally {
            span.finish();
        }
    }
    
    private void callServiceB() {
        // Baggage is accessible here
        String userId = userIdField.getValue();
        System.out.println("Service B sees user ID: " + userId);
    }
}

/**
 * Example 2: Multi-Tenant Baggage
 */
@Service
class MultiTenantBaggageExample {
    
    private final Tracer tracer;
    private final BaggageField tenantIdField;
    
    public MultiTenantBaggageExample(Tracer tracer) {
        this.tracer = tracer;
        this.tenantIdField = BaggageField.create("tenant-id");
    }
    
    public void processRequest(String tenantId, String request) {
        var span = tracer.nextSpan().name("process-request").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Set tenant context
            tenantIdField.updateValue(tenantId);
            
            System.out.println("Processing for tenant: " + tenantId);
            
            // All downstream calls see tenant ID
            queryDatabase(request);
            callExternalApi(request);
            
        } finally {
            span.finish();
        }
    }
    
    private void queryDatabase(String request) {
        String tenantId = tenantIdField.getValue();
        System.out.println("Database query for tenant: " + tenantId);
        // Use tenant ID for data isolation
    }
    
    private void callExternalApi(String request) {
        String tenantId = tenantIdField.getValue();
        System.out.println("API call for tenant: " + tenantId);
        // Tenant ID propagated in HTTP headers
    }
}

/**
 * Example 3: Correlation ID Baggage
 */
@Service
class CorrelationBaggageExample {
    
    private final Tracer tracer;
    private final BaggageField correlationIdField;
    
    public CorrelationBaggageExample(Tracer tracer) {
        this.tracer = tracer;
        this.correlationIdField = BaggageField.create("correlation-id");
    }
    
    public void handleRequest(String correlationId) {
        var span = tracer.nextSpan().name("handle-request").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Set correlation ID for request tracking
            correlationIdField.updateValue(correlationId);
            
            System.out.println("Request correlation: " + correlationId);
            
            // Process through multiple services
            step1();
            step2();
            step3();
            
        } finally {
            span.finish();
        }
    }
    
    private void step1() {
        String corrId = correlationIdField.getValue();
        System.out.println("Step 1 - Correlation: " + corrId);
    }
    
    private void step2() {
        String corrId = correlationIdField.getValue();
        System.out.println("Step 2 - Correlation: " + corrId);
    }
    
    private void step3() {
        String corrId = correlationIdField.getValue();
        System.out.println("Step 3 - Correlation: " + corrId);
    }
}

/**
 * Example 4: User Context Baggage
 */
@Service
class UserContextBaggageExample {
    
    private final Tracer tracer;
    private final BaggageField userIdField;
    private final BaggageField userRoleField;
    private final BaggageField userTierField;
    
    public UserContextBaggageExample(Tracer tracer) {
        this.tracer = tracer;
        this.userIdField = BaggageField.create("user-id");
        this.userRoleField = BaggageField.create("user-role");
        this.userTierField = BaggageField.create("user-tier");
    }
    
    public void processUserRequest(String userId, String role, String tier) {
        var span = tracer.nextSpan().name("user-request").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Set complete user context
            userIdField.updateValue(userId);
            userRoleField.updateValue(role);
            userTierField.updateValue(tier);
            
            System.out.println("User context set: " + userId + "/" + role + "/" + tier);
            
            // Authorize based on role
            if ("ADMIN".equals(userRoleField.getValue())) {
                performAdminOperation();
            } else {
                performUserOperation();
            }
            
            // Apply tier-based features
            applyTierFeatures();
            
        } finally {
            span.finish();
        }
    }
    
    private void performAdminOperation() {
        System.out.println("Admin operation for user: " + userIdField.getValue());
    }
    
    private void performUserOperation() {
        System.out.println("User operation for user: " + userIdField.getValue());
    }
    
    private void applyTierFeatures() {
        String tier = userTierField.getValue();
        System.out.println("Applying " + tier + " tier features");
    }
}

/**
 * Example 5: Request Metadata Baggage
 */
@Service
class RequestMetadataBaggageExample {
    
    private final Tracer tracer;
    private final BaggageField requestIdField;
    private final BaggageField requestSourceField;
    private final BaggageField requestPriorityField;
    
    public RequestMetadataBaggageExample(Tracer tracer) {
        this.tracer = tracer;
        this.requestIdField = BaggageField.create("request-id");
        this.requestSourceField = BaggageField.create("request-source");
        this.requestPriorityField = BaggageField.create("request-priority");
    }
    
    public void handleIncomingRequest(String requestId, String source, String priority) {
        var span = tracer.nextSpan().name("incoming-request").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Set request metadata
            requestIdField.updateValue(requestId);
            requestSourceField.updateValue(source);
            requestPriorityField.updateValue(priority);
            
            System.out.println("Request: " + requestId + " from " + source +
                " with priority " + priority);
            
            // Route based on priority
            if ("HIGH".equals(requestPriorityField.getValue())) {
                processHighPriority();
            } else {
                processNormalPriority();
            }
            
        } finally {
            span.finish();
        }
    }
    
    private void processHighPriority() {
        System.out.println("Fast lane processing for: " + requestIdField.getValue());
    }
    
    private void processNormalPriority() {
        System.out.println("Normal processing for: " + requestIdField.getValue());
    }
}

/**
 * Example 6: Feature Flag Baggage
 */
@Service
class FeatureFlagBaggageExample {
    
    private final Tracer tracer;
    private final BaggageField featureFlagField;
    
    public FeatureFlagBaggageExample(Tracer tracer) {
        this.tracer = tracer;
        this.featureFlagField = BaggageField.create("feature-flags");
    }
    
    public void processWithFeatures(String flags) {
        var span = tracer.nextSpan().name("feature-process").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Set feature flags (e.g., "NEW_UI,BETA_SEARCH")
            featureFlagField.updateValue(flags);
            
            System.out.println("Features enabled: " + flags);
            
            // Check features across services
            if (isFeatureEnabled("NEW_UI")) {
                useNewUI();
            } else {
                useOldUI();
            }
            
            if (isFeatureEnabled("BETA_SEARCH")) {
                useBetaSearch();
            }
            
        } finally {
            span.finish();
        }
    }
    
    private boolean isFeatureEnabled(String feature) {
        String flags = featureFlagField.getValue();
        return flags != null && flags.contains(feature);
    }
    
    private void useNewUI() {
        System.out.println("Using new UI");
    }
    
    private void useOldUI() {
        System.out.println("Using old UI");
    }
    
    private void useBetaSearch() {
        System.out.println("Using beta search");
    }
}

/**
 * Example 7: Geographic Region Baggage
 */
@Service
class GeographicBaggageExample {
    
    private final Tracer tracer;
    private final BaggageField regionField;
    private final BaggageField countryField;
    
    public GeographicBaggageExample(Tracer tracer) {
        this.tracer = tracer;
        this.regionField = BaggageField.create("region");
        this.countryField = BaggageField.create("country");
    }
    
    public void processRegionalRequest(String region, String country) {
        var span = tracer.nextSpan().name("regional-request").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Set geographic context
            regionField.updateValue(region);
            countryField.updateValue(country);
            
            System.out.println("Processing for region: " + region + ", country: " + country);
            
            // Route to regional services
            routeToRegion();
            
            // Apply regional compliance
            applyCompliance();
            
        } finally {
            span.finish();
        }
    }
    
    private void routeToRegion() {
        String region = regionField.getValue();
        System.out.println("Routing to " + region + " data center");
    }
    
    private void applyCompliance() {
        String country = countryField.getValue();
        if ("EU".equals(regionField.getValue())) {
            System.out.println("Applying GDPR compliance for " + country);
        }
    }
}

/**
 * Example 8: Session Baggage
 */
@Service
class SessionBaggageExample {
    
    private final Tracer tracer;
    private final BaggageField sessionIdField;
    private final BaggageField deviceTypeField;
    
    public SessionBaggageExample(Tracer tracer) {
        this.tracer = tracer;
        this.sessionIdField = BaggageField.create("session-id");
        this.deviceTypeField = BaggageField.create("device-type");
    }
    
    public void handleSessionRequest(String sessionId, String deviceType) {
        var span = tracer.nextSpan().name("session-request").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Set session context
            sessionIdField.updateValue(sessionId);
            deviceTypeField.updateValue(deviceType);
            
            System.out.println("Session: " + sessionId + " from " + deviceType);
            
            // Optimize for device
            if ("MOBILE".equals(deviceTypeField.getValue())) {
                optimizeForMobile();
            } else {
                optimizeForDesktop();
            }
            
        } finally {
            span.finish();
        }
    }
    
    private void optimizeForMobile() {
        System.out.println("Mobile optimization for session: " + sessionIdField.getValue());
    }
    
    private void optimizeForDesktop() {
        System.out.println("Desktop optimization for session: " + sessionIdField.getValue());
    }
}

/**
 * Example 9: A/B Test Baggage
 */
@Service
class ABTestBaggageExample {
    
    private final Tracer tracer;
    private final BaggageField experimentIdField;
    private final BaggageField variantField;
    
    public ABTestBaggageExample(Tracer tracer) {
        this.tracer = tracer;
        this.experimentIdField = BaggageField.create("experiment-id");
        this.variantField = BaggageField.create("variant");
    }
    
    public void processExperiment(String experimentId, String variant) {
        var span = tracer.nextSpan().name("ab-test").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Set experiment context
            experimentIdField.updateValue(experimentId);
            variantField.updateValue(variant);
            
            System.out.println("Experiment: " + experimentId + ", variant: " + variant);
            
            // Apply variant across all services
            if ("A".equals(variantField.getValue())) {
                useVariantA();
            } else if ("B".equals(variantField.getValue())) {
                useVariantB();
            }
            
            // Track experiment results
            trackResults();
            
        } finally {
            span.finish();
        }
    }
    
    private void useVariantA() {
        System.out.println("Using algorithm A for experiment: " + experimentIdField.getValue());
    }
    
    private void useVariantB() {
        System.out.println("Using algorithm B for experiment: " + experimentIdField.getValue());
    }
    
    private void trackResults() {
        System.out.println("Tracking results for " + experimentIdField.getValue() +
            " variant " + variantField.getValue());
    }
}

/**
 * Example 10: Complete Baggage Context
 */
@Service
class CompleteBaggageExample {
    
    private final Tracer tracer;
    private final BaggageField userIdField;
    private final BaggageField tenantIdField;
    private final BaggageField correlationIdField;
    private final BaggageField requestSourceField;
    
    public CompleteBaggageExample(Tracer tracer) {
        this.tracer = tracer;
        this.userIdField = BaggageField.create("user-id");
        this.tenantIdField = BaggageField.create("tenant-id");
        this.correlationIdField = BaggageField.create("correlation-id");
        this.requestSourceField = BaggageField.create("request-source");
    }
    
    public void processCompleteRequest(String userId, String tenantId,
                                       String correlationId, String source) {
        var span = tracer.nextSpan().name("complete-request").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Set all context
            userIdField.updateValue(userId);
            tenantIdField.updateValue(tenantId);
            correlationIdField.updateValue(correlationId);
            requestSourceField.updateValue(source);
            
            System.out.println("\n=== Request Context ===");
            System.out.println("User: " + userIdField.getValue());
            System.out.println("Tenant: " + tenantIdField.getValue());
            System.out.println("Correlation: " + correlationIdField.getValue());
            System.out.println("Source: " + requestSourceField.getValue());
            
            // All services see this context
            authenticateUser();
            authorizeRequest();
            processData();
            auditRequest();
            
        } finally {
            span.finish();
        }
    }
    
    private void authenticateUser() {
        System.out.println("Authenticating user: " + userIdField.getValue());
    }
    
    private void authorizeRequest() {
        System.out.println("Authorizing for tenant: " + tenantIdField.getValue());
    }
    
    private void processData() {
        System.out.println("Processing request: " + correlationIdField.getValue());
    }
    
    private void auditRequest() {
        System.out.println("Auditing request from: " + requestSourceField.getValue());
    }
}

/**
 * Main Pattern Class
 */
public class BaggagePattern {
    
    public void demonstrateBaggagePattern() {
        System.out.println("\n=== Baggage Pattern ===");
        System.out.println("Propagate context across services");
        System.out.println("\nKey Concepts:");
        System.out.println("  - BaggageField for typed access");
        System.out.println("  - Automatic propagation in headers");
        System.out.println("  - Available across all services");
        System.out.println("\nCommon Use Cases:");
        System.out.println("  - User/Tenant ID");
        System.out.println("  - Correlation ID");
        System.out.println("  - Feature flags");
        System.out.println("  - A/B test variants");
        System.out.println("  - Geographic region");
        System.out.println("\nBest Practices:");
        System.out.println("  - Keep baggage small (limited header size)");
        System.out.println("  - Use for cross-cutting concerns");
        System.out.println("  - Don't use for large payloads");
    }
}
