package com.example.cloud.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distributed Tracing Pattern - Demonstrates Request Tracing Across Microservices
 * 
 * This pattern shows how to:
 * 1. Implement correlation ID propagation
 * 2. Use Spring Cloud Sleuth for tracing
 * 3. Integrate with Zipkin for visualization
 * 4. Implement trace context propagation
 * 5. Track request spans across services
 * 6. Add custom tags and annotations
 * 7. Implement baggage propagation
 * 8. Monitor trace metrics
 * 9. Implement sampling strategies
 * 10. Debug distributed transactions
 * 
 * Key Concepts:
 * - Trace: End-to-end request journey
 * - Span: Single unit of work (service call)
 * - Correlation ID: Unique request identifier
 * - Context Propagation: Pass trace info between services
 * - Sampling: Trace subset of requests
 * 
 * Tracing Components:
 * 1. Trace ID - Unique across entire request
 * 2. Span ID - Unique per service call
 * 3. Parent Span ID - Link to calling service
 * 4. Tags - Metadata about the span
 * 5. Logs - Events during span execution
 * 
 * Dependencies:
 * - spring-cloud-starter-sleuth
 * - spring-cloud-sleuth-zipkin
 * - micrometer-tracing-bridge-brave
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class DistributedTracingPattern {

    public static void main(String[] args) {
        SpringApplication.run(DistributedTracingPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("DISTRIBUTED TRACING PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateTracing();
        demonstrateConfiguration();
        
        System.out.println("\nApplication running with tracing enabled");
        System.out.println("Zipkin UI: http://localhost:9411");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateTracing() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TRACING CONCEPTS");
        System.out.println("=".repeat(80));
        
        System.out.println("\nTrace Hierarchy:");
        System.out.println("  Trace (e1234567)");
        System.out.println("    └─ Span: API Gateway (a1111111)");
        System.out.println("       └─ Span: User Service (a2222222, parent: a1111111)");
        System.out.println("          └─ Span: Database (a3333333, parent: a2222222)");
    }
    
    private static void demonstrateConfiguration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SLEUTH CONFIGURATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\nspring.sleuth.sampler.probability=0.1 # Sample 10% of requests");
        System.out.println("spring.zipkin.base-url=http://localhost:9411");
        System.out.println("spring.zipkin.enabled=true");
    }
}

/**
 * Trace Context Model
 */
class TraceContext {
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private Map<String, String> baggage;
    
    public TraceContext(String traceId, String spanId, String parentSpanId) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.baggage = new HashMap<>();
    }
    
    // Getters and setters
    public String getTraceId() { return traceId; }
    public String getSpanId() { return spanId; }
    public String getParentSpanId() { return parentSpanId; }
    public Map<String, String> getBaggage() { return baggage; }
    public void addBaggage(String key, String value) { baggage.put(key, value); }
}

/**
 * Span Model
 */
class Span {
    private String spanId;
    private String traceId;
    private String parentSpanId;
    private String operationName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private Map<String, String> tags;
    private List<SpanLog> logs;
    private String serviceName;
    
    public Span(String spanId, String traceId, String parentSpanId, 
                String operationName, String serviceName) {
        this.spanId = spanId;
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
        this.operationName = operationName;
        this.serviceName = serviceName;
        this.startTime = LocalDateTime.now();
        this.tags = new HashMap<>();
        this.logs = new ArrayList<>();
    }
    
    public void finish() {
        this.endTime = LocalDateTime.now();
        this.duration = java.time.Duration.between(startTime, endTime).toMillis();
    }
    
    public void addTag(String key, String value) {
        tags.put(key, value);
    }
    
    public void log(String message) {
        logs.add(new SpanLog(LocalDateTime.now(), message));
    }
    
    // Getters
    public String getSpanId() { return spanId; }
    public String getTraceId() { return traceId; }
    public String getParentSpanId() { return parentSpanId; }
    public String getOperationName() { return operationName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Long getDuration() { return duration; }
    public Map<String, String> getTags() { return tags; }
    public List<SpanLog> getLogs() { return logs; }
    public String getServiceName() { return serviceName; }
}

/**
 * Span Log Entry
 */
class SpanLog {
    private LocalDateTime timestamp;
    private String message;
    
    public SpanLog(LocalDateTime timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
}

/**
 * Tracer Service
 */
@Service
class TracerService {
    
    private final Map<String, List<Span>> traces = new ConcurrentHashMap<>();
    private final ThreadLocal<TraceContext> currentContext = new ThreadLocal<>();
    
    public TraceContext startTrace(String serviceName, String operationName) {
        String traceId = generateId();
        String spanId = generateId();
        
        TraceContext context = new TraceContext(traceId, spanId, null);
        currentContext.set(context);
        
        Span span = new Span(spanId, traceId, null, operationName, serviceName);
        recordSpan(span);
        
        return context;
    }
    
    public TraceContext continueTrace(String traceId, String parentSpanId, 
                                     String serviceName, String operationName) {
        String spanId = generateId();
        
        TraceContext context = new TraceContext(traceId, spanId, parentSpanId);
        currentContext.set(context);
        
        Span span = new Span(spanId, traceId, parentSpanId, operationName, serviceName);
        recordSpan(span);
        
        return context;
    }
    
    public void finishSpan(String traceId, String spanId) {
        List<Span> spans = traces.get(traceId);
        if (spans != null) {
            spans.stream()
                .filter(s -> s.getSpanId().equals(spanId))
                .findFirst()
                .ifPresent(Span::finish);
        }
    }
    
    public void addTag(String key, String value) {
        TraceContext context = currentContext.get();
        if (context != null) {
            List<Span> spans = traces.get(context.getTraceId());
            if (spans != null) {
                spans.stream()
                    .filter(s -> s.getSpanId().equals(context.getSpanId()))
                    .findFirst()
                    .ifPresent(s -> s.addTag(key, value));
            }
        }
    }
    
    public void log(String message) {
        TraceContext context = currentContext.get();
        if (context != null) {
            List<Span> spans = traces.get(context.getTraceId());
            if (spans != null) {
                spans.stream()
                    .filter(s -> s.getSpanId().equals(context.getSpanId()))
                    .findFirst()
                    .ifPresent(s -> s.log(message));
            }
        }
    }
    
    public TraceContext getCurrentContext() {
        return currentContext.get();
    }
    
    public void clearContext() {
        currentContext.remove();
    }
    
    public List<Span> getTrace(String traceId) {
        return traces.getOrDefault(traceId, new ArrayList<>());
    }
    
    public Collection<String> getAllTraceIds() {
        return traces.keySet();
    }
    
    private void recordSpan(Span span) {
        traces.computeIfAbsent(span.getTraceId(), k -> new ArrayList<>())
            .add(span);
    }
    
    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}

/**
 * Trace Context Propagator
 */
@Service
class TraceContextPropagator {
    
    public static final String TRACE_ID_HEADER = "X-B3-TraceId";
    public static final String SPAN_ID_HEADER = "X-B3-SpanId";
    public static final String PARENT_SPAN_ID_HEADER = "X-B3-ParentSpanId";
    public static final String SAMPLED_HEADER = "X-B3-Sampled";
    
    public Map<String, String> inject(TraceContext context) {
        Map<String, String> headers = new HashMap<>();
        headers.put(TRACE_ID_HEADER, context.getTraceId());
        headers.put(SPAN_ID_HEADER, context.getSpanId());
        if (context.getParentSpanId() != null) {
            headers.put(PARENT_SPAN_ID_HEADER, context.getParentSpanId());
        }
        headers.put(SAMPLED_HEADER, "1");
        
        // Add baggage
        context.getBaggage().forEach((key, value) -> 
            headers.put("X-Baggage-" + key, value));
        
        return headers;
    }
    
    public TraceContext extract(Map<String, String> headers) {
        String traceId = headers.get(TRACE_ID_HEADER);
        String spanId = headers.get(SPAN_ID_HEADER);
        String parentSpanId = headers.get(PARENT_SPAN_ID_HEADER);
        
        if (traceId == null || spanId == null) {
            return null;
        }
        
        TraceContext context = new TraceContext(traceId, spanId, parentSpanId);
        
        // Extract baggage
        headers.forEach((key, value) -> {
            if (key.startsWith("X-Baggage-")) {
                String baggageKey = key.substring(10);
                context.addBaggage(baggageKey, value);
            }
        });
        
        return context;
    }
}

/**
 * Demo Service showing tracing
 */
@Service
class OrderTracingService {
    
    private final TracerService tracerService;
    
    public OrderTracingService(TracerService tracerService) {
        this.tracerService = tracerService;
    }
    
    public String processOrder(String orderId) {
        // Start a new span for this operation
        TraceContext context = tracerService.startTrace("order-service", "processOrder");
        
        try {
            tracerService.addTag("order.id", orderId);
            tracerService.log("Order processing started");
            
            // Simulate order validation
            validateOrder(orderId);
            
            // Simulate payment processing
            processPayment(orderId);
            
            tracerService.log("Order processing completed");
            
            return "Order processed successfully";
        } finally {
            tracerService.finishSpan(context.getTraceId(), context.getSpanId());
            tracerService.clearContext();
        }
    }
    
    private void validateOrder(String orderId) {
        TraceContext parentContext = tracerService.getCurrentContext();
        TraceContext context = tracerService.continueTrace(
            parentContext.getTraceId(),
            parentContext.getSpanId(),
            "order-service",
            "validateOrder"
        );
        
        try {
            tracerService.addTag("validation.type", "business-rules");
            tracerService.log("Validating order: " + orderId);
            
            // Simulate validation
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            tracerService.log("Order validation successful");
        } finally {
            tracerService.finishSpan(context.getTraceId(), context.getSpanId());
        }
    }
    
    private void processPayment(String orderId) {
        TraceContext parentContext = tracerService.getCurrentContext();
        TraceContext context = tracerService.continueTrace(
            parentContext.getTraceId(),
            parentContext.getSpanId(),
            "payment-service",
            "processPayment"
        );
        
        try {
            tracerService.addTag("payment.method", "credit-card");
            tracerService.log("Processing payment for order: " + orderId);
            
            // Simulate payment
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            tracerService.log("Payment processed successfully");
        } finally {
            tracerService.finishSpan(context.getTraceId(), context.getSpanId());
        }
    }
}

/**
 * REST Controller demonstrating distributed tracing
 */
@RestController
@RequestMapping("/api/tracing")
class TracingController {
    
    private final OrderTracingService orderService;
    private final TracerService tracerService;
    
    public TracingController(OrderTracingService orderService,
                            TracerService tracerService) {
        this.orderService = orderService;
        this.tracerService = tracerService;
    }
    
    @PostMapping("/orders/{orderId}")
    public Map<String, Object> createOrder(@PathVariable String orderId) {
        String result = orderService.processOrder(orderId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("status", result);
        
        return response;
    }
    
    @GetMapping("/traces/{traceId}")
    public List<Span> getTrace(@PathVariable String traceId) {
        return tracerService.getTrace(traceId);
    }
    
    @GetMapping("/traces")
    public Collection<String> getAllTraces() {
        return tracerService.getAllTraceIds();
    }
}
