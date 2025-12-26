package com.example.tracing;

import brave.Span;
import brave.Tracer;
import org.springframework.stereotype.Service;

/**
 * Span Pattern
 * =============
 * 
 * Demonstrates Span concept in distributed tracing for representing
 * a single unit of work in a distributed system.
 * 
 * Key Concepts:
 * ------------
 * 1. Span - Single operation/unit of work
 * 2. Span ID - Unique identifier for the span
 * 3. Parent Span - Hierarchical relationship
 * 4. Tags - Key-value metadata
 * 5. Annotations - Timestamped events
 * 
 * Span Lifecycle:
 * --------------
 * 1. Start - Create new span
 * 2. Tag - Add metadata
 * 3. Log/Annotate - Record events
 * 4. Finish - Complete span
 * 
 * Span Types:
 * ----------
 * - Server Span - Handling incoming request
 * - Client Span - Making outgoing request
 * - Local Span - Internal operation
 * - Producer Span - Sending message
 * - Consumer Span - Receiving message
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Span Creation
 */
@Service
class BasicSpanExample {
    
    private final Tracer tracer;
    
    public BasicSpanExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Create and use a basic span
     */
    public void processWithSpan() {
        // Start a new span
        Span span = tracer.nextSpan().name("process-operation").start();
        
        try {
            System.out.println("Span ID: " + span.context().spanIdString());
            System.out.println("Trace ID: " + span.context().traceIdString());
            
            // Do work
            doWork();
            
        } finally {
            // Always finish the span
            span.finish();
        }
    }
    
    private void doWork() {
        System.out.println("Processing...");
    }
}

/**
 * Example 2: Span with Tags
 */
@Service
class SpanWithTagsExample {
    
    private final Tracer tracer;
    
    public SpanWithTagsExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Add metadata to span using tags
     */
    public void processOrder(String orderId, double amount) {
        Span span = tracer.nextSpan().name("process-order").start();
        
        try {
            // Add tags for filtering and analysis
            span.tag("order.id", orderId);
            span.tag("order.amount", String.valueOf(amount));
            span.tag("order.type", amount > 1000 ? "high-value" : "standard");
            span.tag("service.name", "order-service");
            
            System.out.println("Processing order: " + orderId);
            
            // Business logic
            if (amount > 1000) {
                span.tag("priority", "high");
            }
            
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 3: Span with Annotations
 */
@Service
class SpanWithAnnotationsExample {
    
    private final Tracer tracer;
    
    public SpanWithAnnotationsExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Record timestamped events in span
     */
    public void processWithEvents() {
        Span span = tracer.nextSpan().name("multi-step-process").start();
        
        try {
            span.annotate("validation.start");
            validate();
            span.annotate("validation.complete");
            
            span.annotate("enrichment.start");
            enrich();
            span.annotate("enrichment.complete");
            
            span.annotate("persistence.start");
            persist();
            span.annotate("persistence.complete");
            
        } finally {
            span.finish();
        }
    }
    
    private void validate() { System.out.println("Validating..."); }
    private void enrich() { System.out.println("Enriching..."); }
    private void persist() { System.out.println("Persisting..."); }
}

/**
 * Example 4: Child Span (Nested Spans)
 */
@Service
class ChildSpanExample {
    
    private final Tracer tracer;
    
    public ChildSpanExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Create hierarchical spans
     */
    public void parentOperation() {
        Span parentSpan = tracer.nextSpan().name("parent-operation").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(parentSpan)) {
            System.out.println("Parent span: " + parentSpan.context().spanIdString());
            
            // Child operation 1
            childOperation1();
            
            // Child operation 2
            childOperation2();
            
        } finally {
            parentSpan.finish();
        }
    }
    
    private void childOperation1() {
        Span childSpan = tracer.nextSpan().name("child-operation-1").start();
        try {
            System.out.println("Child span 1: " + childSpan.context().spanIdString());
            // Work here
        } finally {
            childSpan.finish();
        }
    }
    
    private void childOperation2() {
        Span childSpan = tracer.nextSpan().name("child-operation-2").start();
        try {
            System.out.println("Child span 2: " + childSpan.context().spanIdString());
            // Work here
        } finally {
            childSpan.finish();
        }
    }
}

/**
 * Example 5: Error Handling in Spans
 */
@Service
class SpanErrorHandlingExample {
    
    private final Tracer tracer;
    
    public SpanErrorHandlingExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Record errors in spans
     */
    public void processWithErrorHandling(String input) {
        Span span = tracer.nextSpan().name("error-prone-operation").start();
        
        try {
            if (input == null || input.isEmpty()) {
                throw new IllegalArgumentException("Input cannot be empty");
            }
            
            // Process
            System.out.println("Processing: " + input);
            
        } catch (Exception e) {
            // Tag span with error
            span.tag("error", "true");
            span.tag("error.message", e.getMessage());
            span.tag("error.type", e.getClass().getSimpleName());
            
            // Log the error
            span.annotate("error.occurred");
            
            throw e;
            
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 6: Client Span (Outgoing Request)
 */
@Service
class ClientSpanExample {
    
    private final Tracer tracer;
    
    public ClientSpanExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Create span for outgoing HTTP request
     */
    public void makeHttpRequest(String url) {
        Span span = tracer.nextSpan()
            .name("http-client-request")
            .kind(Span.Kind.CLIENT)
            .start();
        
        try {
            span.tag("http.method", "GET");
            span.tag("http.url", url);
            span.tag("peer.service", "external-api");
            
            span.annotate("request.start");
            
            // Simulate HTTP call
            String response = callExternalService(url);
            
            span.annotate("request.complete");
            span.tag("http.status_code", "200");
            
            System.out.println("Response: " + response);
            
        } finally {
            span.finish();
        }
    }
    
    private String callExternalService(String url) {
        return "Response from " + url;
    }
}

/**
 * Example 7: Server Span (Incoming Request)
 */
@Service
class ServerSpanExample {
    
    private final Tracer tracer;
    
    public ServerSpanExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Create span for incoming HTTP request
     */
    public void handleRequest(String method, String path) {
        Span span = tracer.nextSpan()
            .name(method + " " + path)
            .kind(Span.Kind.SERVER)
            .start();
        
        try {
            span.tag("http.method", method);
            span.tag("http.path", path);
            span.tag("component", "spring-webmvc");
            
            span.annotate("request.received");
            
            // Handle request
            processRequest(path);
            
            span.annotate("response.sent");
            span.tag("http.status_code", "200");
            
        } finally {
            span.finish();
        }
    }
    
    private void processRequest(String path) {
        System.out.println("Handling: " + path);
    }
}

/**
 * Example 8: Producer/Consumer Spans (Messaging)
 */
@Service
class MessagingSpanExample {
    
    private final Tracer tracer;
    
    public MessagingSpanExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Producer span for sending message
     */
    public void sendMessage(String topic, String message) {
        Span span = tracer.nextSpan()
            .name("send-" + topic)
            .kind(Span.Kind.PRODUCER)
            .start();
        
        try {
            span.tag("messaging.system", "kafka");
            span.tag("messaging.destination", topic);
            span.tag("messaging.operation", "send");
            
            System.out.println("Sending message to: " + topic);
            
        } finally {
            span.finish();
        }
    }
    
    /**
     * Consumer span for receiving message
     */
    public void receiveMessage(String topic, String message) {
        Span span = tracer.nextSpan()
            .name("receive-" + topic)
            .kind(Span.Kind.CONSUMER)
            .start();
        
        try {
            span.tag("messaging.system", "kafka");
            span.tag("messaging.destination", topic);
            span.tag("messaging.operation", "receive");
            
            System.out.println("Received message from: " + topic);
            
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 9: Database Span
 */
@Service
class DatabaseSpanExample {
    
    private final Tracer tracer;
    
    public DatabaseSpanExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Create span for database operation
     */
    public void queryDatabase(String sql) {
        Span span = tracer.nextSpan()
            .name("database-query")
            .kind(Span.Kind.CLIENT)
            .start();
        
        try {
            span.tag("db.type", "sql");
            span.tag("db.instance", "order-db");
            span.tag("db.statement", sql);
            span.tag("peer.service", "postgresql");
            
            span.annotate("query.start");
            
            // Execute query
            executeQuery(sql);
            
            span.annotate("query.complete");
            
        } finally {
            span.finish();
        }
    }
    
    private void executeQuery(String sql) {
        System.out.println("Executing: " + sql);
    }
}

/**
 * Example 10: Span with Custom Duration
 */
@Service
class CustomDurationSpanExample {
    
    private final Tracer tracer;
    
    public CustomDurationSpanExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Measure operation duration in span
     */
    public void timedOperation() {
        long startTime = System.currentTimeMillis();
        Span span = tracer.nextSpan().name("timed-operation").start();
        
        try {
            span.tag("operation.type", "computation");
            
            // Simulate work
            Thread.sleep(100);
            
            long duration = System.currentTimeMillis() - startTime;
            span.tag("duration.ms", String.valueOf(duration));
            
            System.out.println("Operation took: " + duration + "ms");
            
        } catch (InterruptedException e) {
            span.tag("error", "interrupted");
            Thread.currentThread().interrupt();
        } finally {
            span.finish();
        }
    }
}

/**
 * Main Pattern Class
 */
public class SpanPattern {
    
    /**
     * Core Span pattern demonstration
     */
    public void demonstrateSpanPattern() {
        System.out.println("\n=== Span Pattern ===");
        System.out.println("Unit of work in distributed tracing");
        System.out.println("\nKey Components:");
        System.out.println("  - Span ID (unique identifier)");
        System.out.println("  - Trace ID (request identifier)");
        System.out.println("  - Parent Span ID (hierarchy)");
        System.out.println("  - Tags (metadata)");
        System.out.println("  - Annotations (events)");
        System.out.println("\nSpan Types:");
        System.out.println("  - SERVER (incoming request)");
        System.out.println("  - CLIENT (outgoing request)");
        System.out.println("  - PRODUCER (send message)");
        System.out.println("  - CONSUMER (receive message)");
        System.out.println("\nBest Practices:");
        System.out.println("  - Always finish spans");
        System.out.println("  - Use try-finally blocks");
        System.out.println("  - Tag with relevant metadata");
        System.out.println("  - Record errors");
        System.out.println("  - Use appropriate span kinds");
    }
}
