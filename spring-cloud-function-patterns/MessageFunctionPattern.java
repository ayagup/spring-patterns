package com.example.cloudfunction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import java.util.function.Function;

/**
 * Message Function Pattern
 * =========================
 * 
 * Demonstrates Spring Cloud Function integration with Spring Messaging
 * for header manipulation, message routing, and enrichment.
 * 
 * Key Concepts:
 * ------------
 * 1. Message<T> - Payload + Headers wrapper
 * 2. Header Manipulation - Read/write message metadata
 * 3. Content Routing - Route based on headers
 * 4. Message Enrichment - Add metadata
 * 5. Protocol Translation - Convert between formats
 * 
 * How It Works:
 * ------------
 * - Input/Output: Message<T> instead of plain T
 * - Access headers: message.getHeaders()
 * - Access payload: message.getPayload()
 * - Build messages: MessageBuilder.withPayload().setHeader()
 * - Headers preserved across function calls
 * 
 * Benefits:
 * --------
 * - Full message control
 * - Header-based routing
 * - Message correlation
 * - Protocol bridging
 * - Metadata enrichment
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Message Function
 */
@Configuration
class BasicMessageFunctionExample {
    
    /**
     * Process message with headers
     * 
     * Input: Message<String>
     * Output: Message<String>
     */
    @Bean
    public Function<Message<String>, Message<String>> processMessage() {
        return message -> {
            System.out.println("=== Processing Message ===");
            
            // Access payload
            String payload = message.getPayload();
            System.out.println("Payload: " + payload);
            
            // Access headers
            message.getHeaders().forEach((key, value) -> 
                System.out.println("Header: " + key + " = " + value)
            );
            
            // Transform payload
            String result = payload.toUpperCase();
            
            // Build response with headers
            return MessageBuilder
                .withPayload(result)
                .copyHeaders(message.getHeaders())
                .setHeader("processed", "true")
                .setHeader("timestamp", System.currentTimeMillis())
                .build();
        };
    }
}

/**
 * Example 2: Header-Based Routing
 */
@Configuration
class HeaderBasedRoutingExample {
    
    /**
     * Route message based on header value
     */
    @Bean
    public Function<Message<String>, Message<String>> routeByHeader() {
        return message -> {
            // Get routing header
            String routeKey = (String) message.getHeaders().get("route");
            System.out.println("Routing with key: " + routeKey);
            
            String payload = message.getPayload();
            String result;
            
            if ("premium".equals(routeKey)) {
                result = "PREMIUM: " + payload.toUpperCase();
            } else if ("standard".equals(routeKey)) {
                result = "STANDARD: " + payload;
            } else {
                result = "DEFAULT: " + payload.toLowerCase();
            }
            
            return MessageBuilder
                .withPayload(result)
                .copyHeaders(message.getHeaders())
                .setHeader("processed_route", routeKey)
                .build();
        };
    }
}

/**
 * Example 3: Message Enrichment
 */
@Configuration
class MessageEnrichmentExample {
    
    static class OrderPayload {
        private String id;
        private double amount;
        
        public OrderPayload(String id, double amount) {
            this.id = id;
            this.amount = amount;
        }
        
        public String getId() { return id; }
        public double getAmount() { return amount; }
    }
    
    /**
     * Enrich message with additional headers
     */
    @Bean
    public Function<Message<OrderPayload>, Message<OrderPayload>> enrichOrder() {
        return message -> {
            OrderPayload order = message.getPayload();
            
            // Enrich with metadata
            String priority = order.getAmount() > 1000 ? "HIGH" : "NORMAL";
            String category = determineCategory(order);
            
            return MessageBuilder
                .withPayload(order)
                .copyHeaders(message.getHeaders())
                .setHeader("priority", priority)
                .setHeader("category", category)
                .setHeader("enriched_at", System.currentTimeMillis())
                .setHeader("enriched_by", "order-service")
                .build();
        };
    }
    
    private String determineCategory(OrderPayload order) {
        if (order.getAmount() > 5000) return "ENTERPRISE";
        if (order.getAmount() > 1000) return "BUSINESS";
        return "CONSUMER";
    }
}

/**
 * Example 4: Correlation ID Pattern
 */
@Configuration
class CorrelationIdExample {
    
    /**
     * Maintain correlation ID across processing
     */
    @Bean
    public Function<Message<String>, Message<String>> processWithCorrelation() {
        return message -> {
            // Get or generate correlation ID
            String correlationId = (String) message.getHeaders()
                .getOrDefault("correlationId", java.util.UUID.randomUUID().toString());
            
            System.out.println("Processing with correlation ID: " + correlationId);
            
            String payload = message.getPayload();
            String result = payload.toUpperCase();
            
            return MessageBuilder
                .withPayload(result)
                .setHeader("correlationId", correlationId)
                .setHeader("step", "processed")
                .setHeader("timestamp", System.currentTimeMillis())
                .build();
        };
    }
}

/**
 * Example 5: Content Type Handling
 */
@Configuration
class ContentTypeHandlingExample {
    
    /**
     * Handle different content types
     */
    @Bean
    public Function<Message<String>, Message<String>> handleContentType() {
        return message -> {
            // Get content type header
            String contentType = (String) message.getHeaders()
                .getOrDefault("contentType", "text/plain");
            
            System.out.println("Content type: " + contentType);
            
            String payload = message.getPayload();
            String result;
            
            switch (contentType) {
                case "application/json":
                    result = "{\"data\":\"" + payload + "\"}";
                    break;
                case "application/xml":
                    result = "<data>" + payload + "</data>";
                    break;
                default:
                    result = payload;
            }
            
            return MessageBuilder
                .withPayload(result)
                .setHeader("contentType", contentType)
                .setHeader("processed", "true")
                .build();
        };
    }
}

/**
 * Example 6: Priority Processing
 */
@Configuration
class PriorityProcessingExample {
    
    /**
     * Process based on priority header
     */
    @Bean
    public Function<Message<String>, Message<String>> processByPriority() {
        return message -> {
            Integer priority = (Integer) message.getHeaders().getOrDefault("priority", 5);
            System.out.println("Processing with priority: " + priority);
            
            String payload = message.getPayload();
            String result;
            
            if (priority >= 8) {
                result = "[URGENT] " + payload;
                System.out.println("Urgent processing activated");
            } else if (priority >= 5) {
                result = "[NORMAL] " + payload;
            } else {
                result = "[LOW] " + payload;
            }
            
            return MessageBuilder
                .withPayload(result)
                .copyHeaders(message.getHeaders())
                .setHeader("processed_at", System.currentTimeMillis())
                .build();
        };
    }
}

/**
 * Example 7: Message Validation
 */
@Configuration
class MessageValidationExample {
    
    /**
     * Validate message and add validation headers
     */
    @Bean
    public Function<Message<String>, Message<String>> validateMessage() {
        return message -> {
            String payload = message.getPayload();
            boolean valid = true;
            java.util.List<String> errors = new java.util.ArrayList<>();
            
            // Validate payload
            if (payload == null || payload.isEmpty()) {
                valid = false;
                errors.add("Payload is empty");
            }
            
            // Validate required headers
            if (!message.getHeaders().containsKey("userId")) {
                valid = false;
                errors.add("Missing userId header");
            }
            
            System.out.println("Validation result: " + (valid ? "VALID" : "INVALID"));
            
            return MessageBuilder
                .withPayload(payload)
                .copyHeaders(message.getHeaders())
                .setHeader("validation_status", valid ? "VALID" : "INVALID")
                .setHeader("validation_errors", errors)
                .setHeader("validated_at", System.currentTimeMillis())
                .build();
        };
    }
}

/**
 * Example 8: Message Filtering
 */
@Configuration
class MessageFilteringExample {
    
    /**
     * Filter messages based on headers
     */
    @Bean
    public Function<Message<String>, Message<String>> filterMessage() {
        return message -> {
            // Check if message should be processed
            Boolean enabled = (Boolean) message.getHeaders()
                .getOrDefault("processing_enabled", true);
            
            String region = (String) message.getHeaders()
                .getOrDefault("region", "US");
            
            if (!enabled || !"US".equals(region)) {
                System.out.println("Message filtered out");
                return MessageBuilder
                    .withPayload("")
                    .setHeader("filtered", "true")
                    .setHeader("reason", "Processing disabled or region mismatch")
                    .build();
            }
            
            // Process message
            String result = message.getPayload().toUpperCase();
            
            return MessageBuilder
                .withPayload(result)
                .copyHeaders(message.getHeaders())
                .setHeader("filtered", "false")
                .build();
        };
    }
}

/**
 * Example 9: Trace Context Propagation
 */
@Configuration
class TraceContextPropagationExample {
    
    /**
     * Propagate distributed tracing context
     */
    @Bean
    public Function<Message<String>, Message<String>> propagateTrace() {
        return message -> {
            // Extract trace context
            String traceId = (String) message.getHeaders()
                .getOrDefault("traceId", generateTraceId());
            String spanId = generateSpanId();
            String parentSpanId = (String) message.getHeaders().get("spanId");
            
            System.out.println("Trace: " + traceId + ", Span: " + spanId);
            
            String result = message.getPayload().toUpperCase();
            
            return MessageBuilder
                .withPayload(result)
                .setHeader("traceId", traceId)
                .setHeader("spanId", spanId)
                .setHeader("parentSpanId", parentSpanId)
                .setHeader("serviceName", "message-processor")
                .build();
        };
    }
    
    private String generateTraceId() {
        return "trace-" + System.currentTimeMillis();
    }
    
    private String generateSpanId() {
        return "span-" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}

/**
 * Example 10: Message Transformation Pipeline
 */
@Configuration
class MessageTransformationPipelineExample {
    
    /**
     * Multi-step message transformation
     */
    @Bean
    public Function<Message<String>, Message<String>> transformPipeline() {
        return message -> {
            System.out.println("=== Transformation Pipeline ===");
            
            // Step 1: Extract and log
            String payload = message.getPayload();
            System.out.println("Step 1 - Input: " + payload);
            
            // Step 2: Transform
            String transformed = payload.trim().toUpperCase();
            System.out.println("Step 2 - Transformed: " + transformed);
            
            // Step 3: Enrich
            String enriched = "[PROCESSED] " + transformed;
            System.out.println("Step 3 - Enriched: " + enriched);
            
            // Step 4: Build output message
            return MessageBuilder
                .withPayload(enriched)
                .copyHeaders(message.getHeaders())
                .setHeader("pipeline_steps", 3)
                .setHeader("transformed", "true")
                .setHeader("enriched", "true")
                .setHeader("completed_at", System.currentTimeMillis())
                .build();
        };
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class MessageFunctionPattern {
    
    /**
     * Core Message Function demonstration
     */
    public void demonstrateMessageFunction() {
        System.out.println("\n=== Message Function Pattern ===");
        System.out.println("Spring Messaging integration");
        System.out.println("\nKey Features:");
        System.out.println("  - Message<T> wrapper");
        System.out.println("  - Header manipulation");
        System.out.println("  - Payload transformation");
        System.out.println("  - Message routing");
        System.out.println("  - Metadata enrichment");
        System.out.println("\nUse Cases:");
        System.out.println("  - Header-based routing");
        System.out.println("  - Correlation tracking");
        System.out.println("  - Message enrichment");
        System.out.println("  - Content negotiation");
        System.out.println("  - Distributed tracing");
    }
}

/**
 * Usage Examples
 */
class MessageFunctionUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Message Function Pattern Usage");
        System.out.println("==============================\n");
        
        System.out.println("1. Basic Message Function:");
        System.out.println("@Bean");
        System.out.println("public Function<Message<String>, Message<String>> process() {");
        System.out.println("    return message -> MessageBuilder");
        System.out.println("        .withPayload(message.getPayload().toUpperCase())");
        System.out.println("        .copyHeaders(message.getHeaders())");
        System.out.println("        .setHeader(\"processed\", true)");
        System.out.println("        .build();");
        System.out.println("}\n");
        
        System.out.println("2. Access Headers:");
        System.out.println("String value = (String) message.getHeaders().get(\"key\");");
        System.out.println("message.getHeaders().forEach((k, v) -> ...);\n");
        
        System.out.println("3. Build Message:");
        System.out.println("Message<String> msg = MessageBuilder");
        System.out.println("    .withPayload(\"data\")");
        System.out.println("    .setHeader(\"correlationId\", uuid)");
        System.out.println("    .setHeader(\"timestamp\", now)");
        System.out.println("    .build();\n");
        
        System.out.println("Benefits:");
        System.out.println("- Full message control");
        System.out.println("- Header-based logic");
        System.out.println("- Message correlation");
        System.out.println("- Metadata tracking");
        System.out.println("- Protocol bridging");
    }
}
