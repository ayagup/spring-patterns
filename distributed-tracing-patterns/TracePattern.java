package com.example.tracing;

import brave.Tracer;
import brave.propagation.TraceContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Trace Pattern
 * =============
 * 
 * Demonstrates Trace concept in distributed tracing for representing
 * an end-to-end request flow across multiple services.
 * 
 * Key Concepts:
 * ------------
 * 1. Trace - Complete request journey
 * 2. Trace ID - Unique identifier for entire request
 * 3. Span Collection - Multiple spans in one trace
 * 4. Causality - Parent-child relationships
 * 5. Timeline - Spans ordered by time
 * 
 * Trace Structure:
 * ---------------
 * Trace
 *  └── Root Span (Service A)
 *       ├── Child Span 1 (Service B)
 *       │    └── Child Span 1.1 (Database)
 *       └── Child Span 2 (Service C)
 *            └── Child Span 2.1 (Cache)
 * 
 * Use Cases:
 * ---------
 * - Request flow visualization
 * - Performance analysis
 * - Latency debugging
 * - Service dependency mapping
 * - Error propagation tracking
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Trace Context
 */
@Service
class BasicTraceExample {
    
    private final Tracer tracer;
    
    public BasicTraceExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Access current trace context
     */
    public void demonstrateTraceContext() {
        TraceContext context = tracer.currentSpan().context();
        
        System.out.println("Trace ID: " + context.traceIdString());
        System.out.println("Span ID: " + context.spanIdString());
        System.out.println("Parent ID: " + context.parentIdString());
        System.out.println("Sampled: " + context.sampled());
    }
}

/**
 * Example 2: Multi-Service Trace
 */
@Service
class MultiServiceTraceExample {
    
    private final Tracer tracer;
    
    public MultiServiceTraceExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Simulate request across multiple services
     */
    public void processRequest(String requestId) {
        // Service A - Entry point
        var rootSpan = tracer.nextSpan().name("service-a-process").start();
        
        try (var ws = tracer.withSpanInScope(rootSpan)) {
            String traceId = rootSpan.context().traceIdString();
            System.out.println("Trace ID: " + traceId);
            
            // Call Service B
            callServiceB(requestId);
            
            // Call Service C
            callServiceC(requestId);
            
        } finally {
            rootSpan.finish();
        }
    }
    
    private void callServiceB(String requestId) {
        var span = tracer.nextSpan().name("service-b-call").start();
        try (var ws = tracer.withSpanInScope(span)) {
            System.out.println("Service B processing: " + requestId);
            // Database call
            queryDatabase();
        } finally {
            span.finish();
        }
    }
    
    private void callServiceC(String requestId) {
        var span = tracer.nextSpan().name("service-c-call").start();
        try (var ws = tracer.withSpanInScope(span)) {
            System.out.println("Service C processing: " + requestId);
            // Cache call
            checkCache();
        } finally {
            span.finish();
        }
    }
    
    private void queryDatabase() {
        var span = tracer.nextSpan().name("database-query").start();
        try {
            System.out.println("Querying database...");
        } finally {
            span.finish();
        }
    }
    
    private void checkCache() {
        var span = tracer.nextSpan().name("cache-lookup").start();
        try {
            System.out.println("Checking cache...");
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 3: Trace Timeline Collector
 */
class TraceTimeline {
    
    static class SpanInfo {
        String spanId;
        String name;
        long startTime;
        long duration;
        String parentId;
        
        SpanInfo(String spanId, String name, long startTime, long duration, String parentId) {
            this.spanId = spanId;
            this.name = name;
            this.startTime = startTime;
            this.duration = duration;
            this.parentId = parentId;
        }
        
        @Override
        public String toString() {
            return String.format("Span[%s] %s: %dms (parent: %s)",
                spanId, name, duration, parentId != null ? parentId : "root");
        }
    }
    
    private final List<SpanInfo> spans = new ArrayList<>();
    
    public void addSpan(SpanInfo span) {
        spans.add(span);
    }
    
    public void printTimeline() {
        System.out.println("\n=== Trace Timeline ===");
        spans.sort((a, b) -> Long.compare(a.startTime, b.startTime));
        spans.forEach(System.out::println);
    }
    
    public long getTotalDuration() {
        if (spans.isEmpty()) return 0;
        long min = spans.stream().mapToLong(s -> s.startTime).min().orElse(0);
        long max = spans.stream().mapToLong(s -> s.startTime + s.duration).max().orElse(0);
        return max - min;
    }
}

/**
 * Example 4: Distributed Trace Example
 */
@Service
class DistributedTraceExample {
    
    private final Tracer tracer;
    
    public DistributedTraceExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * E-commerce order processing trace
     */
    public void processOrder(String orderId) {
        var rootSpan = tracer.nextSpan().name("order-processing").start();
        
        try (var ws = tracer.withSpanInScope(rootSpan)) {
            String traceId = rootSpan.context().traceIdString();
            System.out.println("\n=== Processing Order (Trace: " + traceId + ") ===");
            
            // Step 1: Validate order
            validateOrder(orderId);
            
            // Step 2: Check inventory
            checkInventory(orderId);
            
            // Step 3: Process payment
            processPayment(orderId);
            
            // Step 4: Ship order
            shipOrder(orderId);
            
        } finally {
            rootSpan.finish();
        }
    }
    
    private void validateOrder(String orderId) {
        var span = tracer.nextSpan().name("validate-order").start();
        try (var ws = tracer.withSpanInScope(span)) {
            span.tag("order.id", orderId);
            System.out.println("Validating order: " + orderId);
        } finally {
            span.finish();
        }
    }
    
    private void checkInventory(String orderId) {
        var span = tracer.nextSpan().name("check-inventory").start();
        try (var ws = tracer.withSpanInScope(span)) {
            span.tag("order.id", orderId);
            System.out.println("Checking inventory: " + orderId);
            
            // Database query
            var dbSpan = tracer.nextSpan().name("inventory-db-query").start();
            try {
                System.out.println("  - Querying inventory database");
            } finally {
                dbSpan.finish();
            }
        } finally {
            span.finish();
        }
    }
    
    private void processPayment(String orderId) {
        var span = tracer.nextSpan().name("process-payment").start();
        try (var ws = tracer.withSpanInScope(span)) {
            span.tag("order.id", orderId);
            System.out.println("Processing payment: " + orderId);
            
            // External payment gateway
            var gatewaySpan = tracer.nextSpan().name("payment-gateway-call").start();
            try {
                gatewaySpan.tag("gateway", "stripe");
                System.out.println("  - Calling payment gateway");
            } finally {
                gatewaySpan.finish();
            }
        } finally {
            span.finish();
        }
    }
    
    private void shipOrder(String orderId) {
        var span = tracer.nextSpan().name("ship-order").start();
        try (var ws = tracer.withSpanInScope(span)) {
            span.tag("order.id", orderId);
            System.out.println("Shipping order: " + orderId);
            
            // Shipping service call
            var shippingSpan = tracer.nextSpan().name("shipping-service-call").start();
            try {
                shippingSpan.tag("carrier", "fedex");
                System.out.println("  - Creating shipping label");
            } finally {
                shippingSpan.finish();
            }
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 5: Trace with Error Handling
 */
@Service
class TraceWithErrorExample {
    
    private final Tracer tracer;
    
    public TraceWithErrorExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Track errors across trace
     */
    public void processWithError(String data) {
        var rootSpan = tracer.nextSpan().name("error-prone-process").start();
        
        try (var ws = tracer.withSpanInScope(rootSpan)) {
            String traceId = rootSpan.context().traceIdString();
            
            step1();
            step2(data); // May fail
            step3();
            
        } catch (Exception e) {
            rootSpan.tag("error", "true");
            rootSpan.tag("error.message", e.getMessage());
            System.out.println("Error in trace: " + e.getMessage());
            throw e;
        } finally {
            rootSpan.finish();
        }
    }
    
    private void step1() {
        var span = tracer.nextSpan().name("step-1").start();
        try {
            System.out.println("Step 1 complete");
        } finally {
            span.finish();
        }
    }
    
    private void step2(String data) {
        var span = tracer.nextSpan().name("step-2").start();
        try {
            if (data == null) {
                span.tag("error", "true");
                throw new IllegalArgumentException("Data cannot be null");
            }
            System.out.println("Step 2 complete");
        } finally {
            span.finish();
        }
    }
    
    private void step3() {
        var span = tracer.nextSpan().name("step-3").start();
        try {
            System.out.println("Step 3 complete");
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 6: Parallel Operations in Trace
 */
@Service
class ParallelTraceExample {
    
    private final Tracer tracer;
    
    public ParallelTraceExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Multiple parallel operations in same trace
     */
    public void processParallel(String requestId) {
        var rootSpan = tracer.nextSpan().name("parallel-processing").start();
        
        try (var ws = tracer.withSpanInScope(rootSpan)) {
            System.out.println("Processing parallel operations...");
            
            // These would typically run in parallel threads
            fetchUserData(requestId);
            fetchProductData(requestId);
            fetchInventoryData(requestId);
            
        } finally {
            rootSpan.finish();
        }
    }
    
    private void fetchUserData(String requestId) {
        var span = tracer.nextSpan().name("fetch-user-data").start();
        try {
            span.tag("request.id", requestId);
            System.out.println("Fetching user data");
        } finally {
            span.finish();
        }
    }
    
    private void fetchProductData(String requestId) {
        var span = tracer.nextSpan().name("fetch-product-data").start();
        try {
            span.tag("request.id", requestId);
            System.out.println("Fetching product data");
        } finally {
            span.finish();
        }
    }
    
    private void fetchInventoryData(String requestId) {
        var span = tracer.nextSpan().name("fetch-inventory-data").start();
        try {
            span.tag("request.id", requestId);
            System.out.println("Fetching inventory data");
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 7: Trace Context Propagation
 */
@Service
class TraceContextPropagationExample {
    
    private final Tracer tracer;
    
    public TraceContextPropagationExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Propagate trace context to async operations
     */
    public void asyncOperation() {
        var rootSpan = tracer.nextSpan().name("async-root").start();
        
        try (var ws = tracer.withSpanInScope(rootSpan)) {
            TraceContext context = rootSpan.context();
            
            // Simulate async operation
            processAsync(context);
            
        } finally {
            rootSpan.finish();
        }
    }
    
    private void processAsync(TraceContext parentContext) {
        // Create child span with parent context
        var childSpan = tracer.nextSpan(parentContext).name("async-child").start();
        
        try (var ws = tracer.withSpanInScope(childSpan)) {
            System.out.println("Async processing with trace: " +
                childSpan.context().traceIdString());
        } finally {
            childSpan.finish();
        }
    }
}

/**
 * Example 8: Cross-Process Trace
 */
@Service
class CrossProcessTraceExample {
    
    private final Tracer tracer;
    
    public CrossProcessTraceExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Simulate trace across process boundaries
     */
    public void sendRequest(String data) {
        var span = tracer.nextSpan().name("http-request").start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            TraceContext context = span.context();
            
            // In real scenario, these headers would be sent over HTTP
            String traceId = context.traceIdString();
            String spanId = context.spanIdString();
            
            System.out.println("Sending request with headers:");
            System.out.println("  X-B3-TraceId: " + traceId);
            System.out.println("  X-B3-SpanId: " + spanId);
            
            // Simulate remote service
            receiveRequest(traceId, spanId);
            
        } finally {
            span.finish();
        }
    }
    
    private void receiveRequest(String traceId, String spanId) {
        // In real scenario, create span from received headers
        System.out.println("Received request with trace: " + traceId);
    }
}

/**
 * Example 9: Trace Metrics Collection
 */
@Service
class TraceMetricsExample {
    
    private final Tracer tracer;
    
    public TraceMetricsExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Collect metrics from trace
     */
    public void collectMetrics(String operation) {
        long startTime = System.currentTimeMillis();
        var span = tracer.nextSpan().name(operation).start();
        
        try (var ws = tracer.withSpanInScope(span)) {
            // Do work
            doWork();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Tag with metrics
            span.tag("duration.ms", String.valueOf(duration));
            span.tag("operation.type", operation);
            
            if (duration > 100) {
                span.tag("slow.operation", "true");
            }
            
        } finally {
            span.finish();
        }
    }
    
    private void doWork() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Example 10: Complete Request Trace
 */
@Service
class CompleteRequestTraceExample {
    
    private final Tracer tracer;
    
    public CompleteRequestTraceExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Complete API request with full trace
     */
    public void handleApiRequest(String userId, String productId) {
        var rootSpan = tracer.nextSpan()
            .name("GET /api/products/{id}")
            .kind(brave.Span.Kind.SERVER)
            .start();
        
        try (var ws = tracer.withSpanInScope(rootSpan)) {
            String traceId = rootSpan.context().traceIdString();
            rootSpan.tag("http.method", "GET");
            rootSpan.tag("user.id", userId);
            
            System.out.println("\n=== API Request Trace: " + traceId + " ===");
            
            // Authentication
            authenticate(userId);
            
            // Authorization
            authorize(userId, productId);
            
            // Fetch data
            String product = fetchProduct(productId);
            
            // Log access
            logAccess(userId, productId);
            
            rootSpan.tag("http.status_code", "200");
            System.out.println("Request completed successfully");
            
        } catch (Exception e) {
            rootSpan.tag("error", "true");
            rootSpan.tag("http.status_code", "500");
            throw e;
        } finally {
            rootSpan.finish();
        }
    }
    
    private void authenticate(String userId) {
        var span = tracer.nextSpan().name("authenticate").start();
        try {
            span.tag("user.id", userId);
            System.out.println("Authenticating user: " + userId);
        } finally {
            span.finish();
        }
    }
    
    private void authorize(String userId, String productId) {
        var span = tracer.nextSpan().name("authorize").start();
        try {
            span.tag("user.id", userId);
            span.tag("product.id", productId);
            System.out.println("Authorizing access to product: " + productId);
        } finally {
            span.finish();
        }
    }
    
    private String fetchProduct(String productId) {
        var span = tracer.nextSpan().name("fetch-product").start();
        try {
            span.tag("product.id", productId);
            System.out.println("Fetching product: " + productId);
            return "Product " + productId;
        } finally {
            span.finish();
        }
    }
    
    private void logAccess(String userId, String productId) {
        var span = tracer.nextSpan().name("log-access").start();
        try {
            span.tag("user.id", userId);
            span.tag("product.id", productId);
            System.out.println("Logging access event");
        } finally {
            span.finish();
        }
    }
}

/**
 * Main Pattern Class
 */
public class TracePattern {
    
    /**
     * Core Trace pattern demonstration
     */
    public void demonstrateTracePattern() {
        System.out.println("\n=== Trace Pattern ===");
        System.out.println("End-to-end request flow tracking");
        System.out.println("\nKey Components:");
        System.out.println("  - Trace ID (unique request identifier)");
        System.out.println("  - Span Collection (all operations)");
        System.out.println("  - Timeline (chronological order)");
        System.out.println("  - Causality (parent-child relationships)");
        System.out.println("\nUse Cases:");
        System.out.println("  - Performance analysis");
        System.out.println("  - Latency debugging");
        System.out.println("  - Service dependency mapping");
        System.out.println("  - Error tracking");
        System.out.println("  - Request flow visualization");
    }
}
