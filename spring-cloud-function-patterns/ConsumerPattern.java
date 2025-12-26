package com.example.cloudfunction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

/**
 * Consumer Pattern
 * ================
 * 
 * Demonstrates the Consumer<T> pattern in Spring Cloud Function
 * for one-way message processing and side effects.
 * 
 * Key Concepts:
 * ------------
 * 1. Consumer<T> - Accepts input T, returns nothing (void)
 * 2. Side Effects - Write to database, send notifications, log
 * 3. Fire-and-Forget - No response expected
 * 4. Event Handling - Process events asynchronously
 * 5. Integration - Works with messaging systems
 * 
 * How It Works:
 * ------------
 * - Define @Bean of type Consumer<T>
 * - Accepts input, performs action, no return value
 * - Perfect for event handling and side effects
 * - Can be triggered by HTTP POST, messaging, or events
 * - No response body (HTTP 202 Accepted)
 * 
 * Use Cases:
 * ---------
 * - Database writes
 * - Sending notifications (email, SMS)
 * - Logging and auditing
 * - Cache invalidation
 * - Metrics collection
 * - Event processing
 * - Message acknowledgment
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Simple Logging Consumer
 */
@Configuration
class SimpleLoggingConsumerExample {
    
    /**
     * Simple consumer that logs messages
     * 
     * Usage (HTTP):
     * POST /logMessage
     * Body: "Important event occurred"
     * Response: 202 Accepted (no body)
     */
    @Bean
    public Consumer<String> logMessage() {
        return message -> {
            System.out.println("=== LOG MESSAGE ===");
            System.out.println("Timestamp: " + System.currentTimeMillis());
            System.out.println("Message: " + message);
            System.out.println("==================");
        };
    }
}

/**
 * Example 2: Database Writer Consumer
 */
@Configuration
class DatabaseWriterConsumerExample {
    
    static class Order {
        private String id;
        private String customerId;
        private double amount;
        
        public Order() {}
        public Order(String id, String customerId, double amount) {
            this.id = id;
            this.customerId = customerId;
            this.amount = amount;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
    
    /**
     * Save order to database
     * 
     * Simulates database write operation
     */
    @Bean
    public Consumer<Order> saveOrder() {
        return order -> {
            System.out.println("Saving order to database:");
            System.out.println("  Order ID: " + order.getId());
            System.out.println("  Customer: " + order.getCustomerId());
            System.out.println("  Amount: $" + order.getAmount());
            
            // Simulate database write
            // orderRepository.save(order);
            
            System.out.println("Order saved successfully!");
        };
    }
}

/**
 * Example 3: Notification Sender Consumer
 */
@Configuration
class NotificationSenderConsumerExample {
    
    static class NotificationRequest {
        private String recipient;
        private String subject;
        private String body;
        private String type; // EMAIL, SMS, PUSH
        
        public NotificationRequest() {}
        
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
    
    /**
     * Send notification based on type
     */
    @Bean
    public Consumer<NotificationRequest> sendNotification() {
        return request -> {
            System.out.println("Sending notification:");
            System.out.println("  Type: " + request.getType());
            System.out.println("  Recipient: " + request.getRecipient());
            System.out.println("  Subject: " + request.getSubject());
            
            switch (request.getType()) {
                case "EMAIL":
                    sendEmail(request);
                    break;
                case "SMS":
                    sendSMS(request);
                    break;
                case "PUSH":
                    sendPush(request);
                    break;
                default:
                    System.err.println("Unknown notification type: " + request.getType());
            }
        };
    }
    
    private void sendEmail(NotificationRequest request) {
        System.out.println("Email sent to: " + request.getRecipient());
    }
    
    private void sendSMS(NotificationRequest request) {
        System.out.println("SMS sent to: " + request.getRecipient());
    }
    
    private void sendPush(NotificationRequest request) {
        System.out.println("Push notification sent to: " + request.getRecipient());
    }
}

/**
 * Example 4: Event Handler Consumer
 */
@Configuration
class EventHandlerConsumerExample {
    
    static class OrderEvent {
        private String eventType; // CREATED, UPDATED, CANCELLED
        private String orderId;
        private long timestamp;
        
        public OrderEvent() {}
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * Handle order events
     */
    @Bean
    public Consumer<OrderEvent> handleOrderEvent() {
        return event -> {
            System.out.println("Processing order event:");
            System.out.println("  Event Type: " + event.getEventType());
            System.out.println("  Order ID: " + event.getOrderId());
            System.out.println("  Timestamp: " + event.getTimestamp());
            
            // Process based on event type
            switch (event.getEventType()) {
                case "CREATED":
                    System.out.println("  Action: Send confirmation email");
                    break;
                case "UPDATED":
                    System.out.println("  Action: Update inventory");
                    break;
                case "CANCELLED":
                    System.out.println("  Action: Refund payment");
                    break;
            }
        };
    }
}

/**
 * Example 5: Cache Invalidation Consumer
 */
@Configuration
class CacheInvalidationConsumerExample {
    
    static class CacheInvalidationRequest {
        private String cacheKey;
        private java.util.List<String> patterns;
        
        public CacheInvalidationRequest() {}
        
        public String getCacheKey() { return cacheKey; }
        public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }
        public java.util.List<String> getPatterns() { return patterns; }
        public void setPatterns(java.util.List<String> patterns) { this.patterns = patterns; }
    }
    
    /**
     * Invalidate cache entries
     */
    @Bean
    public Consumer<CacheInvalidationRequest> invalidateCache() {
        return request -> {
            System.out.println("Invalidating cache:");
            
            if (request.getCacheKey() != null) {
                System.out.println("  Key: " + request.getCacheKey());
                // cacheManager.evict(request.getCacheKey());
            }
            
            if (request.getPatterns() != null) {
                request.getPatterns().forEach(pattern -> {
                    System.out.println("  Pattern: " + pattern);
                    // cacheManager.evictByPattern(pattern);
                });
            }
            
            System.out.println("Cache invalidation complete!");
        };
    }
}

/**
 * Example 6: Metrics Collector Consumer
 */
@Configuration
class MetricsCollectorConsumerExample {
    
    static class MetricData {
        private String metricName;
        private double value;
        private java.util.Map<String, String> tags;
        
        public MetricData() {}
        
        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public java.util.Map<String, String> getTags() { return tags; }
        public void setTags(java.util.Map<String, String> tags) { this.tags = tags; }
    }
    
    /**
     * Collect and record metrics
     */
    @Bean
    public Consumer<MetricData> collectMetric() {
        return metric -> {
            System.out.println("Recording metric:");
            System.out.println("  Name: " + metric.getMetricName());
            System.out.println("  Value: " + metric.getValue());
            
            if (metric.getTags() != null) {
                System.out.println("  Tags:");
                metric.getTags().forEach((key, value) -> 
                    System.out.println("    " + key + "=" + value)
                );
            }
            
            // Record to metrics system
            // meterRegistry.counter(metric.getMetricName(), ...).increment(metric.getValue());
        };
    }
}

/**
 * Example 7: Audit Logger Consumer
 */
@Configuration
class AuditLoggerConsumerExample {
    
    static class AuditEvent {
        private String userId;
        private String action;
        private String resource;
        private long timestamp;
        private java.util.Map<String, Object> metadata;
        
        public AuditEvent() {}
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public java.util.Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }
    }
    
    /**
     * Log audit events
     */
    @Bean
    public Consumer<AuditEvent> logAudit() {
        return event -> {
            System.out.println("=== AUDIT LOG ===");
            System.out.println("User: " + event.getUserId());
            System.out.println("Action: " + event.getAction());
            System.out.println("Resource: " + event.getResource());
            System.out.println("Timestamp: " + new java.util.Date(event.getTimestamp()));
            
            if (event.getMetadata() != null) {
                System.out.println("Metadata:");
                event.getMetadata().forEach((key, value) ->
                    System.out.println("  " + key + ": " + value)
                );
            }
            
            System.out.println("================");
            
            // Persist to audit log database
            // auditRepository.save(event);
        };
    }
}

/**
 * Example 8: Batch Processing Consumer
 */
@Configuration
class BatchProcessingConsumerExample {
    
    /**
     * Process batch of items
     */
    @Bean
    public Consumer<java.util.List<String>> processBatch() {
        return batch -> {
            System.out.println("Processing batch:");
            System.out.println("  Batch size: " + batch.size());
            
            for (int i = 0; i < batch.size(); i++) {
                String item = batch.get(i);
                System.out.println("  [" + (i + 1) + "] Processing: " + item);
                
                // Process each item
                processItem(item);
            }
            
            System.out.println("Batch processing complete!");
        };
    }
    
    private void processItem(String item) {
        // Simulate processing
        System.out.println("    -> Item processed: " + item);
    }
}

/**
 * Example 9: Error Handling Consumer
 */
@Configuration
class ErrorHandlingConsumerExample {
    
    /**
     * Consumer with error handling
     */
    @Bean
    public Consumer<String> processWithErrorHandling() {
        return input -> {
            try {
                System.out.println("Processing input: " + input);
                
                // Potentially failing operation
                if (input.contains("error")) {
                    throw new RuntimeException("Simulated error");
                }
                
                // Successful processing
                System.out.println("Processing successful!");
                
            } catch (Exception e) {
                System.err.println("Error occurred: " + e.getMessage());
                
                // Error handling strategies:
                // 1. Log and continue
                // 2. Send to dead letter queue
                // 3. Retry
                // 4. Alert monitoring system
                
                System.err.println("Error logged, continuing...");
            }
        };
    }
}

/**
 * Example 10: Workflow Trigger Consumer
 */
@Configuration
class WorkflowTriggerConsumerExample {
    
    static class WorkflowRequest {
        private String workflowId;
        private String trigger;
        private java.util.Map<String, Object> parameters;
        
        public WorkflowRequest() {}
        
        public String getWorkflowId() { return workflowId; }
        public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
        public String getTrigger() { return trigger; }
        public void setTrigger(String trigger) { this.trigger = trigger; }
        public java.util.Map<String, Object> getParameters() { return parameters; }
        public void setParameters(java.util.Map<String, Object> parameters) { this.parameters = parameters; }
    }
    
    /**
     * Trigger workflows based on events
     */
    @Bean
    public Consumer<WorkflowRequest> triggerWorkflow() {
        return request -> {
            System.out.println("Triggering workflow:");
            System.out.println("  Workflow ID: " + request.getWorkflowId());
            System.out.println("  Trigger: " + request.getTrigger());
            System.out.println("  Parameters: " + request.getParameters());
            
            // Start workflow
            // workflowEngine.start(request.getWorkflowId(), request.getParameters());
            
            System.out.println("Workflow triggered successfully!");
        };
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConsumerPattern {
    
    /**
     * Core Consumer pattern demonstration
     */
    public void demonstrateConsumerPattern() {
        System.out.println("\n=== Consumer Pattern ===");
        System.out.println("One-way message processing");
        System.out.println("\nKey Characteristics:");
        System.out.println("  - Accepts input");
        System.out.println("  - No return value (void)");
        System.out.println("  - Side effects (write, send, log)");
        System.out.println("  - Fire-and-forget");
        System.out.println("\nUse Cases:");
        System.out.println("  - Database writes");
        System.out.println("  - Notifications");
        System.out.println("  - Logging/auditing");
        System.out.println("  - Cache invalidation");
        System.out.println("  - Event handling");
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConsumerPatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Consumer Pattern Usage");
        System.out.println("=====================\n");
        
        System.out.println("1. Define Consumer:");
        System.out.println("@Bean");
        System.out.println("public Consumer<Order> saveOrder() {");
        System.out.println("    return order -> repository.save(order);");
        System.out.println("}\n");
        
        System.out.println("2. HTTP Invocation:");
        System.out.println("POST /saveOrder");
        System.out.println("Body: {\"id\":\"123\", \"amount\":99.99}");
        System.out.println("Response: 202 Accepted (no body)\n");
        
        System.out.println("3. Messaging Integration:");
        System.out.println("spring.cloud.function.definition=saveOrder");
        System.out.println("spring.cloud.stream.bindings.saveOrder-in-0.destination=orders\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Keep processing fast");
        System.out.println("- Handle errors gracefully");
        System.out.println("- Use idempotent operations");
        System.out.println("- Log important events");
        System.out.println("- Consider async processing");
    }
}
