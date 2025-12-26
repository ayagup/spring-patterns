package com.example.tracing;

import brave.Tracer;
import brave.Span;
import org.springframework.stereotype.Service;

/**
 * Tracer Pattern
 * ==============
 * 
 * Demonstrates Tracer for creating and managing spans in distributed tracing.
 * 
 * Key Concepts:
 * ------------
 * 1. Tracer - Span factory and manager
 * 2. Span Creation - nextSpan(), newTrace()
 * 3. Span Scope - withSpanInScope()
 * 4. Current Span - currentSpan()
 * 5. Context Management - Propagation
 * 
 * Tracer Operations:
 * -----------------
 * - nextSpan() - Create child of current span
 * - newTrace() - Start new trace
 * - currentSpan() - Get active span
 * - withSpanInScope() - Set span as current
 * - join() - Continue existing trace
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Tracer Usage
 */
@Service
class BasicTracerExample {
    
    private final Tracer tracer;
    
    public BasicTracerExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void demonstrateTracer() {
        // Create new trace
        Span newTrace = tracer.newTrace().name("new-trace").start();
        try {
            System.out.println("New Trace ID: " + newTrace.context().traceIdString());
        } finally {
            newTrace.finish();
        }
        
        // Create child span
        Span parentSpan = tracer.nextSpan().name("parent").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(parentSpan)) {
            Span childSpan = tracer.nextSpan().name("child").start();
            try {
                System.out.println("Child Span ID: " + childSpan.context().spanIdString());
            } finally {
                childSpan.finish();
            }
        } finally {
            parentSpan.finish();
        }
    }
}

/**
 * Example 2: Span Scope Management
 */
@Service
class SpanScopeExample {
    
    private final Tracer tracer;
    
    public SpanScopeExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void demonstrateScope() {
        Span span = tracer.nextSpan().name("operation").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // Span is now in scope
            Span current = tracer.currentSpan();
            System.out.println("Current span: " + current.context().spanIdString());
            
            // All operations here are under this span
            doWork();
            
        } finally {
            span.finish();
        }
    }
    
    private void doWork() {
        // Can access current span
        Span current = tracer.currentSpan();
        if (current != null) {
            current.tag("work", "done");
        }
    }
}

/**
 * Example 3: Service Tracing
 */
@Service
class ServiceTracingExample {
    
    private final Tracer tracer;
    
    public ServiceTracingExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void processOrder(String orderId) {
        Span span = tracer.nextSpan().name("process-order").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            span.tag("order.id", orderId);
            
            validateOrder(orderId);
            enrichOrder(orderId);
            saveOrder(orderId);
            
        } finally {
            span.finish();
        }
    }
    
    private void validateOrder(String orderId) {
        Span span = tracer.nextSpan().name("validate").start();
        try {
            span.tag("order.id", orderId);
            System.out.println("Validating: " + orderId);
        } finally {
            span.finish();
        }
    }
    
    private void enrichOrder(String orderId) {
        Span span = tracer.nextSpan().name("enrich").start();
        try {
            span.tag("order.id", orderId);
            System.out.println("Enriching: " + orderId);
        } finally {
            span.finish();
        }
    }
    
    private void saveOrder(String orderId) {
        Span span = tracer.nextSpan().name("save").start();
        try {
            span.tag("order.id", orderId);
            System.out.println("Saving: " + orderId);
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 4: HTTP Client Tracing
 */
@Service
class HttpClientTracingExample {
    
    private final Tracer tracer;
    
    public HttpClientTracingExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public String makeHttpCall(String url) {
        Span span = tracer.nextSpan()
            .kind(Span.Kind.CLIENT)
            .name("http-get")
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            span.tag("http.method", "GET");
            span.tag("http.url", url);
            
            // Make actual HTTP call
            String response = callHttp(url);
            
            span.tag("http.status_code", "200");
            return response;
            
        } catch (Exception e) {
            span.tag("error", "true");
            span.tag("error.message", e.getMessage());
            throw e;
        } finally {
            span.finish();
        }
    }
    
    private String callHttp(String url) {
        return "Response from " + url;
    }
}

/**
 * Example 5: Database Tracing
 */
@Service
class DatabaseTracingExample {
    
    private final Tracer tracer;
    
    public DatabaseTracingExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void executeQuery(String sql) {
        Span span = tracer.nextSpan()
            .kind(Span.Kind.CLIENT)
            .name("database-query")
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            span.tag("db.type", "postgresql");
            span.tag("db.statement", sql);
            span.tag("db.instance", "orders");
            
            // Execute query
            executeDb(sql);
            
        } finally {
            span.finish();
        }
    }
    
    private void executeDb(String sql) {
        System.out.println("Executing: " + sql);
    }
}

/**
 * Example 6: Messaging Tracing
 */
@Service
class MessagingTracingExample {
    
    private final Tracer tracer;
    
    public MessagingTracingExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void sendMessage(String topic, String message) {
        Span span = tracer.nextSpan()
            .kind(Span.Kind.PRODUCER)
            .name("kafka-send")
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            span.tag("messaging.system", "kafka");
            span.tag("messaging.destination", topic);
            span.tag("messaging.operation", "send");
            
            // Send message
            send(topic, message);
            
        } finally {
            span.finish();
        }
    }
    
    public void receiveMessage(String topic, String message) {
        Span span = tracer.nextSpan()
            .kind(Span.Kind.CONSUMER)
            .name("kafka-receive")
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            span.tag("messaging.system", "kafka");
            span.tag("messaging.destination", topic);
            span.tag("messaging.operation", "receive");
            
            // Process message
            process(message);
            
        } finally {
            span.finish();
        }
    }
    
    private void send(String topic, String message) {
        System.out.println("Sending to " + topic + ": " + message);
    }
    
    private void process(String message) {
        System.out.println("Processing: " + message);
    }
}

/**
 * Example 7: Async Tracing
 */
@Service
class AsyncTracingExample {
    
    private final Tracer tracer;
    
    public AsyncTracingExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void asyncOperation() {
        Span parentSpan = tracer.nextSpan().name("async-parent").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(parentSpan)) {
            // Get current context for async
            var context = parentSpan.context();
            
            // Simulate async execution
            new Thread(() -> {
                asyncTask(context);
            }).start();
            
        } finally {
            parentSpan.finish();
        }
    }
    
    private void asyncTask(brave.propagation.TraceContext context) {
        Span childSpan = tracer.nextSpan(context).name("async-child").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(childSpan)) {
            System.out.println("Async task executing");
        } finally {
            childSpan.finish();
        }
    }
}

/**
 * Example 8: Error Tracing
 */
@Service
class ErrorTracingExample {
    
    private final Tracer tracer;
    
    public ErrorTracingExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void operationWithError(boolean shouldFail) {
        Span span = tracer.nextSpan().name("risky-operation").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            if (shouldFail) {
                throw new RuntimeException("Operation failed");
            }
            System.out.println("Operation succeeded");
            
        } catch (Exception e) {
            span.tag("error", "true");
            span.tag("error.type", e.getClass().getSimpleName());
            span.tag("error.message", e.getMessage());
            span.annotate("error.thrown");
            throw e;
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 9: Custom Span Builder
 */
@Service
class CustomSpanBuilderExample {
    
    private final Tracer tracer;
    
    public CustomSpanBuilderExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void customSpan() {
        Span span = tracer.nextSpan()
            .name("custom-operation")
            .kind(Span.Kind.CLIENT)
            .tag("custom.tag", "value")
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            span.annotate("operation.start");
            
            // Do work
            performWork();
            
            span.annotate("operation.complete");
            
        } finally {
            span.finish();
        }
    }
    
    private void performWork() {
        System.out.println("Working...");
    }
}

/**
 * Example 10: Complete Microservice Tracing
 */
@Service
class MicroserviceTracingExample {
    
    private final Tracer tracer;
    
    public MicroserviceTracingExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void handleRequest(String userId, String productId) {
        Span span = tracer.nextSpan()
            .name("handle-request")
            .kind(Span.Kind.SERVER)
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            span.tag("user.id", userId);
            span.tag("product.id", productId);
            
            // Authenticate
            authenticateUser(userId);
            
            // Get product from database
            getProduct(productId);
            
            // Call recommendation service
            getRecommendations(productId);
            
            // Log event
            logEvent(userId, productId);
            
        } finally {
            span.finish();
        }
    }
    
    private void authenticateUser(String userId) {
        Span span = tracer.nextSpan().name("authenticate").start();
        try {
            span.tag("user.id", userId);
            System.out.println("Authenticating: " + userId);
        } finally {
            span.finish();
        }
    }
    
    private void getProduct(String productId) {
        Span span = tracer.nextSpan()
            .name("get-product")
            .kind(Span.Kind.CLIENT)
            .start();
        
        try {
            span.tag("db.type", "postgresql");
            span.tag("db.operation", "SELECT");
            span.tag("product.id", productId);
            System.out.println("Getting product: " + productId);
        } finally {
            span.finish();
        }
    }
    
    private void getRecommendations(String productId) {
        Span span = tracer.nextSpan()
            .name("get-recommendations")
            .kind(Span.Kind.CLIENT)
            .start();
        
        try {
            span.tag("http.method", "GET");
            span.tag("peer.service", "recommendation-service");
            span.tag("product.id", productId);
            System.out.println("Getting recommendations for: " + productId);
        } finally {
            span.finish();
        }
    }
    
    private void logEvent(String userId, String productId) {
        Span span = tracer.nextSpan()
            .name("log-event")
            .kind(Span.Kind.PRODUCER)
            .start();
        
        try {
            span.tag("messaging.system", "kafka");
            span.tag("messaging.destination", "user-events");
            span.tag("user.id", userId);
            span.tag("product.id", productId);
            System.out.println("Logging event");
        } finally {
            span.finish();
        }
    }
}

/**
 * Main Pattern Class
 */
public class TracerPattern {
    
    public void demonstrateTracerPattern() {
        System.out.println("\n=== Tracer Pattern ===");
        System.out.println("Span creation and management");
        System.out.println("\nKey Operations:");
        System.out.println("  - nextSpan() - Create child span");
        System.out.println("  - newTrace() - Start new trace");
        System.out.println("  - currentSpan() - Get active span");
        System.out.println("  - withSpanInScope() - Set current span");
        System.out.println("\nSpan Kinds:");
        System.out.println("  - SERVER (incoming request)");
        System.out.println("  - CLIENT (outgoing request)");
        System.out.println("  - PRODUCER (send message)");
        System.out.println("  - CONSUMER (receive message)");
    }
}
