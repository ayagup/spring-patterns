package com.example.monitoring.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tracing Pattern - Demonstrates Distributed Tracing with Spring Boot
 * 
 * This pattern shows how to:
 * 1. Enable distributed tracing with Micrometer Tracing
 * 2. Create custom spans with @NewSpan
 * 3. Add span tags with @SpanTag
 * 4. Propagate trace context across services
 * 5. Create manual spans with Tracer
 * 6. Use Observation API for metrics and tracing
 * 7. Correlate logs with trace IDs
 * 8. Track asynchronous operations
 * 9. Add baggage for contextual information
 * 10. Export traces to backends (Zipkin, Jaeger)
 * 
 * Key Concepts:
 * - Trace: End-to-end request flow across services
 * - Span: Single operation within a trace
 * - Trace Context: Trace ID, Span ID, sampling decision
 * - Baggage: Key-value pairs propagated with trace
 * - Sampling: Decision to record trace or not
 * 
 * Dependencies:
 * - micrometer-tracing-bridge-brave (or otel)
 * - zipkin-reporter-brave (for Zipkin export)
 * - spring-boot-starter-actuator
 * 
 * Configuration:
 * management.tracing.sampling.probability=1.0
 * management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
 * 
 * Access Zipkin UI:
 * http://localhost:9411/zipkin
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class TracingPattern {

    public static void main(String[] args) {
        SpringApplication.run(TracingPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("DISTRIBUTED TRACING PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateTracingConcepts();
        demonstrateSpanOperations();
        demonstrateTraceContext();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TRACING SETUP");
        System.out.println("=".repeat(80));
        System.out.println("\n1. Start Zipkin (optional):");
        System.out.println("   docker run -d -p 9411:9411 openzipkin/zipkin");
        
        System.out.println("\n2. Access Zipkin UI:");
        System.out.println("   http://localhost:9411/zipkin");
        
        System.out.println("\n3. Test tracing endpoints:");
        System.out.println("   GET  /api/trace/order/{orderId}");
        System.out.println("   POST /api/trace/payment");
        System.out.println("   GET  /api/trace/user/{userId}");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TRACING FEATURES");
        System.out.println("=".repeat(80));
        System.out.println("\n✓ Automatic span creation for HTTP requests");
        System.out.println("✓ Custom spans with @NewSpan annotation");
        System.out.println("✓ Manual span creation with Tracer");
        System.out.println("✓ Span tags for additional context");
        System.out.println("✓ Trace context propagation");
        System.out.println("✓ Observation API integration");
        System.out.println("✓ Log correlation with trace IDs");
        System.out.println("✓ Distributed tracing across microservices");
        
        System.out.println("\nApplication is running. Make requests to see traces in Zipkin.");
        System.out.println("Press Ctrl+C to stop.\n");
    }
    
    private static void demonstrateTracingConcepts() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DISTRIBUTED TRACING CONCEPTS");
        System.out.println("=".repeat(80));
        
        System.out.println("\nWhat is Distributed Tracing?");
        System.out.println("- Track requests across multiple services");
        System.out.println("- Visualize call chains and dependencies");
        System.out.println("- Identify performance bottlenecks");
        System.out.println("- Debug issues in microservices");
        
        System.out.println("\nKey Components:");
        System.out.println("Trace     - Unique ID for entire request flow");
        System.out.println("Span      - Individual operation (DB query, HTTP call, etc.)");
        System.out.println("Parent    - Calling span");
        System.out.println("Context   - Metadata propagated between services");
        System.out.println("Tags      - Key-value attributes on spans");
        System.out.println("Logs      - Timestamped events within spans");
    }
    
    private static void demonstrateSpanOperations() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SPAN OPERATIONS");
        System.out.println("=".repeat(80));
        
        System.out.println("\nSpan Lifecycle:");
        System.out.println("1. Start   - Begin timing an operation");
        System.out.println("2. Tag     - Add metadata (user.id, http.method, etc.)");
        System.out.println("3. Log     - Record events (error, cache.miss, etc.)");
        System.out.println("4. Finish  - Complete and report the span");
        
        System.out.println("\nCommon Span Types:");
        System.out.println("SERVER    - Incoming request handling");
        System.out.println("CLIENT    - Outgoing HTTP/RPC call");
        System.out.println("PRODUCER  - Message publishing");
        System.out.println("CONSUMER  - Message consumption");
        System.out.println("INTERNAL  - Internal method calls");
    }
    
    private static void demonstrateTraceContext() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TRACE CONTEXT PROPAGATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\nHTTP Headers:");
        System.out.println("X-B3-TraceId      - Trace identifier");
        System.out.println("X-B3-SpanId       - Span identifier");
        System.out.println("X-B3-ParentSpanId - Parent span");
        System.out.println("X-B3-Sampled      - Sampling decision (1=sample, 0=don't)");
        
        System.out.println("\nW3C Trace Context:");
        System.out.println("traceparent - version-traceId-spanId-flags");
        System.out.println("tracestate  - vendor-specific data");
    }
}

/**
 * Tracing configuration
 */
@Configuration
class TracingConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

/**
 * Service demonstrating @NewSpan annotation
 */
@Service
class OrderTracingService {
    
    private final Tracer tracer;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    
    public OrderTracingService(Tracer tracer, 
                              InventoryService inventoryService,
                              PaymentService paymentService) {
        this.tracer = tracer;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
    }
    
    /**
     * Automatically create a span for this method
     */
    @NewSpan(name = "processOrder")
    public OrderResult processOrder(@SpanTag("order.id") String orderId,
                                   @SpanTag("user.id") String userId) {
        // Add custom tag to current span
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("order.type", "online");
            currentSpan.event("order.validation.started");
        }
        
        // Simulate order validation
        sleep(50);
        
        // Check inventory (creates child span)
        boolean inStock = inventoryService.checkInventory(orderId);
        
        if (!inStock) {
            if (currentSpan != null) {
                currentSpan.tag("order.status", "out_of_stock");
                currentSpan.event("order.failed.inventory");
            }
            return new OrderResult(orderId, false, "Out of stock");
        }
        
        // Process payment (creates child span)
        boolean paymentSuccess = paymentService.processPayment(userId, 99.99);
        
        if (!paymentSuccess) {
            if (currentSpan != null) {
                currentSpan.tag("order.status", "payment_failed");
                currentSpan.event("order.failed.payment");
            }
            return new OrderResult(orderId, false, "Payment failed");
        }
        
        // Complete order
        if (currentSpan != null) {
            currentSpan.tag("order.status", "completed");
            currentSpan.event("order.completed");
        }
        
        return new OrderResult(orderId, true, "Order completed");
    }
    
    /**
     * Create manual span with Tracer
     */
    public void updateOrderStatus(String orderId, String status) {
        Span span = tracer.nextSpan().name("updateOrderStatus");
        
        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            span.tag("order.id", orderId);
            span.tag("order.new_status", status);
            span.event("status.update.started");
            
            // Simulate database update
            sleep(30);
            
            span.event("status.update.completed");
        } finally {
            span.end();
        }
    }
    
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Inventory service with custom spans
 */
@Service
class InventoryService {
    
    private final Tracer tracer;
    
    public InventoryService(Tracer tracer) {
        this.tracer = tracer;
    }
    
    @NewSpan(name = "checkInventory")
    public boolean checkInventory(@SpanTag("product.id") String productId) {
        Span span = tracer.currentSpan();
        
        if (span != null) {
            span.tag("inventory.location", "warehouse-1");
            span.event("inventory.check.started");
        }
        
        // Simulate database query
        sleep(20);
        
        boolean inStock = ThreadLocalRandom.current().nextBoolean();
        
        if (span != null) {
            span.tag("inventory.available", String.valueOf(inStock));
            span.tag("inventory.quantity", String.valueOf(
                ThreadLocalRandom.current().nextInt(0, 100)));
            span.event("inventory.check.completed");
        }
        
        return inStock;
    }
    
    @NewSpan(name = "reserveInventory")
    public void reserveInventory(@SpanTag("product.id") String productId,
                                 @SpanTag("quantity") int quantity) {
        Span span = tracer.currentSpan();
        
        if (span != null) {
            span.tag("reservation.warehouse", "warehouse-1");
        }
        
        sleep(30);
        
        if (span != null) {
            span.event("inventory.reserved");
        }
    }
    
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Payment service with tracing
 */
@Service
class PaymentService {
    
    private final Tracer tracer;
    
    public PaymentService(Tracer tracer) {
        this.tracer = tracer;
    }
    
    @NewSpan(name = "processPayment")
    public boolean processPayment(@SpanTag("user.id") String userId,
                                 @SpanTag("amount") double amount) {
        Span span = tracer.currentSpan();
        
        if (span != null) {
            span.tag("payment.method", "credit_card");
            span.tag("payment.currency", "USD");
            span.event("payment.validation.started");
        }
        
        // Validate payment
        sleep(40);
        
        // Call external payment gateway
        boolean success = callPaymentGateway(amount);
        
        if (span != null) {
            span.tag("payment.status", success ? "approved" : "declined");
            span.event(success ? "payment.approved" : "payment.declined");
        }
        
        return success;
    }
    
    @NewSpan(name = "callPaymentGateway")
    private boolean callPaymentGateway(@SpanTag("amount") double amount) {
        Span span = tracer.currentSpan();
        
        if (span != null) {
            span.tag("gateway.provider", "stripe");
            span.tag("gateway.endpoint", "https://api.stripe.com/v1/charges");
        }
        
        // Simulate external API call
        sleep(60);
        
        boolean success = ThreadLocalRandom.current().nextBoolean();
        
        if (span != null) {
            span.tag("gateway.response_code", success ? "200" : "402");
        }
        
        return success;
    }
    
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * User service with observation API
 */
@Service
class UserService {
    
    private final ObservationRegistry observationRegistry;
    private final Tracer tracer;
    
    public UserService(ObservationRegistry observationRegistry, Tracer tracer) {
        this.observationRegistry = observationRegistry;
        this.tracer = tracer;
    }
    
    public UserProfile getUserProfile(String userId) {
        // Use Observation API (creates both metrics and spans)
        return Observation.createNotStarted("user.profile.fetch", observationRegistry)
            .lowCardinalityKeyValue("user.id", userId)
            .highCardinalityKeyValue("user.email", userId + "@example.com")
            .observe(() -> {
                // Simulate database query
                sleep(25);
                
                return new UserProfile(userId, "John Doe", "john@example.com");
            });
    }
    
    @NewSpan(name = "updateUserProfile")
    public void updateUserProfile(@SpanTag("user.id") String userId,
                                 Map<String, String> updates) {
        Span span = tracer.currentSpan();
        
        if (span != null) {
            span.tag("update.fields", String.join(",", updates.keySet()));
            span.event("profile.update.started");
        }
        
        sleep(35);
        
        if (span != null) {
            span.event("profile.update.completed");
        }
    }
    
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * REST Controller with automatic tracing
 */
@RestController
@RequestMapping("/api/trace")
class TracingController {
    
    private final OrderTracingService orderService;
    private final UserService userService;
    private final Tracer tracer;
    
    public TracingController(OrderTracingService orderService,
                           UserService userService,
                           Tracer tracer) {
        this.orderService = orderService;
        this.userService = userService;
        this.tracer = tracer;
    }
    
    @GetMapping("/order/{orderId}")
    public OrderResult getOrder(@PathVariable String orderId,
                               @RequestParam(defaultValue = "user123") String userId) {
        // Automatically creates a span for HTTP request
        return orderService.processOrder(orderId, userId);
    }
    
    @PostMapping("/order/{orderId}/status")
    public String updateOrderStatus(@PathVariable String orderId,
                                   @RequestParam String status) {
        orderService.updateOrderStatus(orderId, status);
        return "Order status updated";
    }
    
    @GetMapping("/user/{userId}")
    public UserProfile getUserProfile(@PathVariable String userId) {
        return userService.getUserProfile(userId);
    }
    
    @GetMapping("/trace-info")
    public Map<String, String> getTraceInfo() {
        Map<String, String> info = new HashMap<>();
        
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            info.put("traceId", currentSpan.context().traceId());
            info.put("spanId", currentSpan.context().spanId());
            info.put("sampled", String.valueOf(currentSpan.context().sampled()));
        } else {
            info.put("message", "No active span");
        }
        
        return info;
    }
    
    @PostMapping("/simulate/complex-flow")
    public Map<String, Object> simulateComplexFlow() {
        Span rootSpan = tracer.nextSpan().name("complex-business-flow");
        
        try (Tracer.SpanInScope ws = tracer.withSpan(rootSpan.start())) {
            rootSpan.tag("flow.type", "complex");
            rootSpan.event("flow.started");
            
            Map<String, Object> result = new HashMap<>();
            
            // Step 1: Get user
            UserProfile user = userService.getUserProfile("user456");
            result.put("user", user);
            
            // Step 2: Process order
            OrderResult order = orderService.processOrder("ORD-" + 
                UUID.randomUUID().toString().substring(0, 8), user.getId());
            result.put("order", order);
            
            // Step 3: Update status
            if (order.isSuccess()) {
                orderService.updateOrderStatus(order.getOrderId(), "COMPLETED");
            }
            
            rootSpan.tag("flow.status", order.isSuccess() ? "success" : "failed");
            rootSpan.event("flow.completed");
            
            result.put("traceId", rootSpan.context().traceId());
            
            return result;
        } finally {
            rootSpan.end();
        }
    }
}

/**
 * Order result model
 */
class OrderResult {
    private String orderId;
    private boolean success;
    private String message;
    
    public OrderResult(String orderId, boolean success, String message) {
        this.orderId = orderId;
        this.success = success;
        this.message = message;
    }
    
    public String getOrderId() { return orderId; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}

/**
 * User profile model
 */
class UserProfile {
    private String id;
    private String name;
    private String email;
    
    public UserProfile(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

/**
 * Component demonstrating baggage propagation
 */
@Component
class BaggageExample {
    
    private final Tracer tracer;
    
    public BaggageExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void processWith Baggage(String userId) {
        Span span = tracer.nextSpan().name("processWithBaggage");
        
        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            // Add baggage (propagated to downstream services)
            // Note: Baggage API varies by implementation
            span.tag("user.id", userId);
            span.tag("request.id", UUID.randomUUID().toString());
            
            // Baggage is automatically propagated in HTTP headers
            callDownstreamService();
            
        } finally {
            span.end();
        }
    }
    
    private void callDownstreamService() {
        // Trace context and baggage automatically propagated
        sleep(20);
    }
    
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
