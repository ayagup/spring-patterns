package com.example.tracing;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Context Propagation Pattern
 * ===========================
 * 
 * Demonstrates trace context propagation across service boundaries.
 * 
 * Key Concepts:
 * ------------
 * 1. Trace Context - Trace ID, Span ID, Parent ID, Sampling
 * 2. Propagation Formats - B3, W3C Trace Context, Jaeger
 * 3. Injection - Add context to outgoing requests
 * 4. Extraction - Read context from incoming requests
 * 5. Cross-cutting - Works with HTTP, messaging, gRPC
 * 
 * Propagation Formats:
 * -------------------
 * - B3 Single Header: b3={TraceId}-{SpanId}-{SamplingState}-{ParentSpanId}
 * - B3 Multi Header: X-B3-TraceId, X-B3-SpanId, X-B3-ParentSpanId, X-B3-Sampled
 * - W3C Trace Context: traceparent, tracestate
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: HTTP Header Propagation (B3 Format)
 */
@Service
class B3HeaderPropagationExample {
    
    private final Tracer tracer;
    
    public B3HeaderPropagationExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public Map<String, String> prepareHttpHeaders() {
        Map<String, String> headers = new HashMap<>();
        
        Span span = tracer.currentSpan();
        if (span != null) {
            TraceContext context = span.context();
            
            // B3 Multi-Header format
            headers.put("X-B3-TraceId", context.traceIdString());
            headers.put("X-B3-SpanId", context.spanIdString());
            
            if (context.parentId() != null) {
                headers.put("X-B3-ParentSpanId", 
                    Long.toHexString(context.parentId()));
            }
            
            headers.put("X-B3-Sampled", context.sampled() ? "1" : "0");
            
            System.out.println("Injected B3 headers: " + headers);
        }
        
        return headers;
    }
    
    public void extractFromHeaders(Map<String, String> headers) {
        String traceId = headers.get("X-B3-TraceId");
        String spanId = headers.get("X-B3-SpanId");
        String sampled = headers.get("X-B3-Sampled");
        
        System.out.println("Extracted TraceId: " + traceId);
        System.out.println("Extracted SpanId: " + spanId);
        System.out.println("Sampled: " + sampled);
    }
}

/**
 * Example 2: B3 Single Header Format
 */
@Service
class B3SingleHeaderExample {
    
    private final Tracer tracer;
    
    public B3SingleHeaderExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public String createB3SingleHeader() {
        Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        
        TraceContext context = span.context();
        
        // Format: {TraceId}-{SpanId}-{SamplingState}-{ParentSpanId}
        StringBuilder b3 = new StringBuilder();
        b3.append(context.traceIdString()).append("-");
        b3.append(context.spanIdString()).append("-");
        b3.append(context.sampled() ? "1" : "0");
        
        if (context.parentId() != null) {
            b3.append("-").append(Long.toHexString(context.parentId()));
        }
        
        String b3Header = b3.toString();
        System.out.println("B3 Single Header: " + b3Header);
        
        return b3Header;
    }
    
    public void parseB3SingleHeader(String b3Header) {
        String[] parts = b3Header.split("-");
        
        if (parts.length >= 3) {
            System.out.println("TraceId: " + parts[0]);
            System.out.println("SpanId: " + parts[1]);
            System.out.println("Sampled: " + parts[2]);
            
            if (parts.length == 4) {
                System.out.println("ParentSpanId: " + parts[3]);
            }
        }
    }
}

/**
 * Example 3: W3C Trace Context Format
 */
@Service
class W3CTraceContextExample {
    
    private final Tracer tracer;
    
    public W3CTraceContextExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public String createW3CTraceparent() {
        Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        
        TraceContext context = span.context();
        
        // Format: version-traceId-spanId-flags
        String traceparent = String.format("00-%s-%s-%s",
            padTraceId(context.traceIdString()),
            context.spanIdString(),
            context.sampled() ? "01" : "00"
        );
        
        System.out.println("W3C traceparent: " + traceparent);
        return traceparent;
    }
    
    private String padTraceId(String traceId) {
        // W3C requires 32-character trace ID
        return String.format("%032x", Long.parseUnsignedLong(traceId, 16));
    }
    
    public void parseW3CTraceparent(String traceparent) {
        String[] parts = traceparent.split("-");
        
        if (parts.length == 4) {
            System.out.println("Version: " + parts[0]);
            System.out.println("TraceId: " + parts[1]);
            System.out.println("SpanId: " + parts[2]);
            System.out.println("Flags: " + parts[3]);
        }
    }
}

/**
 * Example 4: RestTemplate with Context Propagation
 */
@Service
class RestTemplateContextPropagationExample {
    
    private final Tracer tracer;
    
    public RestTemplateContextPropagationExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void callExternalService(String url) {
        Span span = tracer.nextSpan().name("http.client").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            
            // Prepare headers with trace context
            HttpHeaders headers = new HttpHeaders();
            injectTraceContext(headers);
            
            // Simulate HTTP call
            System.out.println("Calling: " + url);
            System.out.println("Headers: " + headers);
            
        } finally {
            span.finish();
        }
    }
    
    private void injectTraceContext(HttpHeaders headers) {
        Span span = tracer.currentSpan();
        if (span != null) {
            TraceContext context = span.context();
            headers.add("X-B3-TraceId", context.traceIdString());
            headers.add("X-B3-SpanId", context.spanIdString());
            headers.add("X-B3-Sampled", context.sampled() ? "1" : "0");
        }
    }
}

/**
 * Example 5: Message Queue Context Propagation
 */
@Service
class MessageQueueContextPropagationExample {
    
    private final Tracer tracer;
    
    public MessageQueueContextPropagationExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public Map<String, String> sendMessage(String payload) {
        Span span = tracer.nextSpan()
            .name("message.send")
            .kind(Span.Kind.PRODUCER)
            .start();
        
        Map<String, String> messageHeaders = new HashMap<>();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            
            // Inject trace context into message headers
            TraceContext context = span.context();
            messageHeaders.put("traceId", context.traceIdString());
            messageHeaders.put("spanId", context.spanIdString());
            messageHeaders.put("sampled", String.valueOf(context.sampled()));
            
            System.out.println("Sending message with headers: " + messageHeaders);
            
        } finally {
            span.finish();
        }
        
        return messageHeaders;
    }
    
    public void receiveMessage(Map<String, String> messageHeaders, String payload) {
        // Extract parent context from headers
        String traceId = messageHeaders.get("traceId");
        String spanId = messageHeaders.get("spanId");
        
        System.out.println("Received message with TraceId: " + traceId);
        
        // Create child span
        Span span = tracer.nextSpan()
            .name("message.receive")
            .kind(Span.Kind.CONSUMER)
            .start();
        
        try {
            System.out.println("Processing message");
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 6: gRPC Context Propagation
 */
@Service
class GrpcContextPropagationExample {
    
    private final Tracer tracer;
    
    public GrpcContextPropagationExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public Map<String, String> prepareGrpcMetadata() {
        Map<String, String> metadata = new HashMap<>();
        
        Span span = tracer.currentSpan();
        if (span != null) {
            TraceContext context = span.context();
            
            // gRPC metadata keys (lowercase required)
            metadata.put("x-b3-traceid", context.traceIdString());
            metadata.put("x-b3-spanid", context.spanIdString());
            metadata.put("x-b3-sampled", context.sampled() ? "1" : "0");
            
            System.out.println("gRPC metadata: " + metadata);
        }
        
        return metadata;
    }
    
    public void extractFromGrpcMetadata(Map<String, String> metadata) {
        String traceId = metadata.get("x-b3-traceid");
        String spanId = metadata.get("x-b3-spanid");
        
        System.out.println("Extracted from gRPC - TraceId: " + traceId);
    }
}

/**
 * Example 7: Async Processing Context Propagation
 */
@Service
class AsyncContextPropagationExample {
    
    private final Tracer tracer;
    
    public AsyncContextPropagationExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void processAsync() {
        Span parentSpan = tracer.currentSpan();
        TraceContext parentContext = parentSpan != null ? parentSpan.context() : null;
        
        System.out.println("Parent TraceId: " + 
            (parentContext != null ? parentContext.traceIdString() : "none"));
        
        // Simulate async execution
        new Thread(() -> {
            // Create span with parent context
            Span asyncSpan = tracer.nextSpan().name("async.task").start();
            
            try (Tracer.SpanInScope ws = tracer.withSpanInScope(asyncSpan)) {
                System.out.println("Async TraceId: " + asyncSpan.context().traceIdString());
                
                // Async work
                Thread.sleep(100);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                asyncSpan.finish();
            }
        }).start();
    }
}

/**
 * Example 8: Database Context Propagation
 */
@Service
class DatabaseContextPropagationExample {
    
    private final Tracer tracer;
    
    public DatabaseContextPropagationExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void executeWithContext(String sql) {
        Span span = tracer.nextSpan()
            .name("db.query")
            .kind(Span.Kind.CLIENT)
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            
            // Add trace context as SQL comment
            String contextComment = String.format(
                "/* TraceId:%s SpanId:%s */",
                span.context().traceIdString(),
                span.context().spanIdString()
            );
            
            String sqlWithContext = contextComment + " " + sql;
            System.out.println("SQL with context: " + sqlWithContext);
            
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 9: Multi-Protocol Context Propagation
 */
@Service
class MultiProtocolContextPropagationExample {
    
    private final Tracer tracer;
    
    public MultiProtocolContextPropagationExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void propagateAcrossProtocols() {
        Span httpSpan = tracer.nextSpan().name("http.request").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(httpSpan)) {
            
            // HTTP to Messaging
            Map<String, String> messageHeaders = propagateToMessaging();
            
            // Messaging to gRPC
            Map<String, String> grpcMetadata = propagateToGrpc();
            
            // gRPC to Database
            propagateToDatabase();
            
            System.out.println("Context propagated across all protocols");
            
        } finally {
            httpSpan.finish();
        }
    }
    
    private Map<String, String> propagateToMessaging() {
        Span span = tracer.nextSpan().name("message.send").start();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("traceId", span.context().traceIdString());
            return headers;
        } finally {
            span.finish();
        }
    }
    
    private Map<String, String> propagateToGrpc() {
        Span span = tracer.nextSpan().name("grpc.call").start();
        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("x-b3-traceid", span.context().traceIdString());
            return metadata;
        } finally {
            span.finish();
        }
    }
    
    private void propagateToDatabase() {
        Span span = tracer.nextSpan().name("db.query").start();
        try {
            System.out.println("DB with TraceId: " + span.context().traceIdString());
        } finally {
            span.finish();
        }
    }
}

/**
 * Example 10: Custom Context Propagation
 */
@Service
class CustomContextPropagationExample {
    
    private final Tracer tracer;
    
    public CustomContextPropagationExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public Map<String, String> createCustomHeaders() {
        Map<String, String> headers = new HashMap<>();
        
        Span span = tracer.currentSpan();
        if (span != null) {
            TraceContext context = span.context();
            
            // Custom header format
            headers.put("X-Trace-Context", 
                String.format("%s:%s:%s",
                    context.traceIdString(),
                    context.spanIdString(),
                    context.sampled() ? "1" : "0"
                )
            );
            
            // Additional metadata
            headers.put("X-Service-Name", "order-service");
            headers.put("X-Service-Version", "v2.1");
            headers.put("X-Request-Id", java.util.UUID.randomUUID().toString());
            
            System.out.println("Custom headers: " + headers);
        }
        
        return headers;
    }
    
    public void parseCustomHeaders(Map<String, String> headers) {
        String traceContext = headers.get("X-Trace-Context");
        
        if (traceContext != null) {
            String[] parts = traceContext.split(":");
            System.out.println("TraceId: " + parts[0]);
            System.out.println("SpanId: " + parts[1]);
            System.out.println("Sampled: " + parts[2]);
        }
    }
}

/**
 * Main Pattern Class
 */
public class ContextPropagationPattern {
    
    public static void main(String[] args) {
        System.out.println("Context Propagation Pattern");
        System.out.println("===========================\n");
        
        System.out.println("Propagation Formats:");
        System.out.println("1. B3 Multi-Header: X-B3-TraceId, X-B3-SpanId, X-B3-ParentSpanId, X-B3-Sampled");
        System.out.println("2. B3 Single: b3={TraceId}-{SpanId}-{Sampled}-{ParentSpanId}");
        System.out.println("3. W3C Trace Context: traceparent, tracestate\n");
        
        System.out.println("Use Cases:");
        System.out.println("- HTTP REST calls");
        System.out.println("- Message queues (Kafka, RabbitMQ)");
        System.out.println("- gRPC services");
        System.out.println("- Database queries");
        System.out.println("- Async processing\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use standard formats (B3, W3C)");
        System.out.println("- Inject context in all outgoing requests");
        System.out.println("- Extract context from incoming requests");
        System.out.println("- Handle missing context gracefully");
        System.out.println("- Propagate across all protocols");
    }
}
