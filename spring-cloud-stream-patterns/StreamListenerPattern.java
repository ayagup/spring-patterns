package com.example.cloudstream;

import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

/**
 * Stream Listener Pattern
 * =======================
 * 
 * Demonstrates @StreamListener annotation for message consumption in
 * Spring Cloud Stream (Legacy model). Modern applications should use
 * functional programming model (Function/Consumer/Supplier).
 * 
 * Key Concepts:
 * ------------
 * 1. @StreamListener - Annotate methods to consume messages
 * 2. @Payload - Extract message payload
 * 3. @Header/@Headers - Access message headers
 * 4. @SendTo - Route output to specific channel
 * 5. Conditional Dispatching - Route based on content/condition
 * 
 * How It Works:
 * ------------
 * - Annotate method with @StreamListener
 * - Specify input channel
 * - Method invoked for each message
 * - Supports message transformation
 * - Can route to output channels
 * 
 * Migration Note:
 * --------------
 * @StreamListener is deprecated in favor of functional model:
 * - Use Function<I, O> instead of @StreamListener + @SendTo
 * - Use Consumer<I> instead of @StreamListener (void return)
 * - Use Supplier<O> for producing messages
 * 
 * This pattern is documented for legacy applications.
 * New applications should use functional model.
 * 
 * @author Spring Patterns
 * @version 1.0
 * @deprecated Use functional programming model
 */

/**
 * Example 1: Basic Stream Listener
 */
@Configuration
class BasicStreamListenerConfiguration {
    
    /**
     * Simple message consumer
     * 
     * Legacy:
     * @StreamListener(Sink.INPUT)
     * public void handleMessage(String message) {
     *     // Process message
     * }
     * 
     * Modern (recommended):
     * @Bean
     * public Consumer<String> handleMessage() {
     *     return message -> {
     *         // Process message
     *     };
     * }
     */
    @Bean
    public java.util.function.Consumer<String> handleMessage() {
        return message -> {
            System.out.println("Handling message (Functional model)");
            System.out.println("  Message: " + message);
            System.out.println("  Preferred over @StreamListener");
        };
    }
}

/**
 * Example 2: Stream Listener with Payload
 */
@Configuration
class PayloadStreamListenerConfiguration {
    
    /**
     * Extract typed payload
     * 
     * Legacy:
     * @StreamListener(Sink.INPUT)
     * public void handle(@Payload Order order) {
     *     // Process order
     * }
     * 
     * Modern:
     * @Bean
     * public Consumer<Order> handle() {
     *     return order -> {
     *         // Process order
     *     };
     * }
     */
    @Bean
    public java.util.function.Consumer<Order> handleOrder() {
        return order -> {
            System.out.println("Handling order (Functional)");
            System.out.println("  Order ID: " + order.getId());
            System.out.println("  Automatic deserialization");
        };
    }
}

/**
 * Example 3: Stream Listener with Headers
 */
@Configuration
class HeaderStreamListenerConfiguration {
    
    /**
     * Access message headers
     * 
     * Legacy:
     * @StreamListener(Sink.INPUT)
     * public void handle(@Payload String message,
     *                    @Header("userId") String userId) {
     *     // Use userId header
     * }
     * 
     * Modern (with Message):
     * @Bean
     * public Consumer<Message<String>> handle() {
     *     return message -> {
     *         String userId = message.getHeaders().get("userId", String.class);
     *         // Use userId
     *     };
     * }
     */
    @Bean
    public java.util.function.Consumer<org.springframework.messaging.Message<String>> handleWithHeaders() {
        return message -> {
            System.out.println("Handling message with headers (Functional)");
            System.out.println("  Payload: " + message.getPayload());
            System.out.println("  Headers: " + message.getHeaders());
            
            if (message.getHeaders().containsKey("userId")) {
                System.out.println("  User ID: " + message.getHeaders().get("userId"));
            }
        };
    }
}

/**
 * Example 4: Stream Listener with SendTo
 */
@Configuration
class SendToStreamListenerConfiguration {
    
    /**
     * Transform and route to output
     * 
     * Legacy:
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public String transform(String input) {
     *     return input.toUpperCase();
     * }
     * 
     * Modern:
     * @Bean
     * public Function<String, String> transform() {
     *     return input -> input.toUpperCase();
     * }
     */
    @Bean
    public java.util.function.Function<String, String> transformMessage() {
        return input -> {
            System.out.println("Transforming message (Functional)");
            System.out.println("  Input: " + input);
            String output = input.toUpperCase();
            System.out.println("  Output: " + output);
            return output;
        };
    }
}

/**
 * Example 5: Conditional Stream Listener
 */
@Configuration
class ConditionalStreamListenerConfiguration {
    
    /**
     * Conditional message routing
     * 
     * Legacy:
     * @StreamListener(target = Sink.INPUT, 
     *                 condition = "headers['type']=='ORDER'")
     * public void handleOrder(String message) {
     *     // Handle order
     * }
     * 
     * @StreamListener(target = Sink.INPUT,
     *                 condition = "headers['type']=='PAYMENT'")
     * public void handlePayment(String message) {
     *     // Handle payment
     * }
     * 
     * Modern (use Spring Integration router):
     * @Bean
     * public Consumer<Message<String>> route() {
     *     return message -> {
     *         String type = message.getHeaders().get("type", String.class);
     *         if ("ORDER".equals(type)) {
     *             // Handle order
     *         } else if ("PAYMENT".equals(type)) {
     *             // Handle payment
     *         }
     *     };
     * }
     */
    @Bean
    public java.util.function.Consumer<org.springframework.messaging.Message<String>> routeByType() {
        return message -> {
            String type = (String) message.getHeaders().get("type");
            System.out.println("Routing message (Functional)");
            System.out.println("  Type: " + type);
            
            if ("ORDER".equals(type)) {
                System.out.println("  Routing to order handler");
            } else if ("PAYMENT".equals(type)) {
                System.out.println("  Routing to payment handler");
            }
        };
    }
}

/**
 * Example 6: Stream Listener with Exception Handling
 */
@Configuration
class ErrorHandlingStreamListenerConfiguration {
    
    /**
     * Handle errors in message processing
     * 
     * Legacy:
     * @StreamListener(Sink.INPUT)
     * public void handle(String message) {
     *     try {
     *         // Process message
     *     } catch (Exception e) {
     *         // Handle error
     *     }
     * }
     * 
     * Modern (with error handling):
     * @Bean
     * public Consumer<String> handle() {
     *     return message -> {
     *         try {
     *             // Process message
     *         } catch (Exception e) {
     *             // Handle error
     *             throw new RuntimeException(e); // DLQ routing
     *         }
     *     };
     * }
     */
    @Bean
    public java.util.function.Consumer<String> handleWithErrorHandling() {
        return message -> {
            try {
                System.out.println("Processing message: " + message);
                // Simulate processing
                if (message.contains("ERROR")) {
                    throw new RuntimeException("Processing failed");
                }
            } catch (Exception e) {
                System.out.println("Error handling message: " + e.getMessage());
                System.out.println("  Will retry based on configuration");
                System.out.println("  Sent to DLQ after max attempts");
                throw e; // Re-throw for framework handling
            }
        };
    }
}

/**
 * Example 7: Batch Stream Listener
 */
@Configuration
class BatchStreamListenerConfiguration {
    
    /**
     * Process messages in batches
     * 
     * Legacy:
     * @StreamListener(Sink.INPUT)
     * public void handleBatch(List<String> messages) {
     *     // Process batch
     * }
     * 
     * Modern:
     * @Bean
     * public Consumer<List<String>> handleBatch() {
     *     return messages -> {
     *         // Process batch
     *     };
     * }
     * 
     * Configuration:
     * spring.cloud.stream.bindings.handleBatch-in-0.consumer.batch-mode=true
     */
    @Bean
    public java.util.function.Consumer<java.util.List<String>> handleBatch() {
        return messages -> {
            System.out.println("Handling batch (Functional)");
            System.out.println("  Batch size: " + messages.size());
            System.out.println("  Messages: " + messages);
            System.out.println("  Enable with: consumer.batch-mode=true");
        };
    }
}

/**
 * Example 8: Reactive Stream Listener
 */
@Configuration
class ReactiveStreamListenerConfiguration {
    
    /**
     * Reactive message processing
     * 
     * Legacy:
     * @StreamListener(Sink.INPUT)
     * public void handle(Flux<String> messages) {
     *     messages.subscribe(msg -> {
     *         // Process
     *     });
     * }
     * 
     * Modern:
     * @Bean
     * public Function<Flux<String>, Mono<Void>> handle() {
     *     return flux -> flux
     *         .doOnNext(msg -> {
     *             // Process
     *         })
     *         .then();
     * }
     */
    @Bean
    public java.util.function.Function<
        reactor.core.publisher.Flux<String>, 
        reactor.core.publisher.Mono<Void>> handleReactive() {
        
        return flux -> flux
            .doOnNext(message -> {
                System.out.println("Handling reactive message: " + message);
                System.out.println("  Backpressure supported");
                System.out.println("  Non-blocking processing");
            })
            .then();
    }
}

/**
 * Example 9: Stream Listener with Multiple Outputs
 */
@Configuration
class MultiOutputStreamListenerConfiguration {
    
    /**
     * Route to multiple outputs
     * 
     * Legacy:
     * @StreamListener(Processor.INPUT)
     * @SendTo({Processor.OUTPUT, "anotherOutput"})
     * public String[] split(String message) {
     *     return new String[]{message + "-1", message + "-2"};
     * }
     * 
     * Modern (use StreamBridge):
     * @Bean
     * public Consumer<String> split(StreamBridge bridge) {
     *     return message -> {
     *         bridge.send("output1", message + "-1");
     *         bridge.send("output2", message + "-2");
     *     };
     * }
     */
    public void splitToMultipleOutputs() {
        System.out.println("Multiple outputs (use StreamBridge in functional model)");
        System.out.println("  StreamBridge.send(channelName, message)");
        System.out.println("  Dynamic routing to multiple destinations");
    }
}

/**
 * Example 10: Stream Listener Migration Guide
 */
class StreamListenerMigrationGuide {
    
    public void migrationExamples() {
        System.out.println("Stream Listener Migration Guide");
        System.out.println("===============================\n");
        
        System.out.println("Simple Consumer:");
        System.out.println("BEFORE:");
        System.out.println("  @StreamListener(Sink.INPUT)");
        System.out.println("  public void handle(String msg) { }");
        System.out.println("\nAFTER:");
        System.out.println("  @Bean");
        System.out.println("  public Consumer<String> handle() {");
        System.out.println("    return msg -> { };");
        System.out.println("  }\n");
        
        System.out.println("Processor (Transform):");
        System.out.println("BEFORE:");
        System.out.println("  @StreamListener(Processor.INPUT)");
        System.out.println("  @SendTo(Processor.OUTPUT)");
        System.out.println("  public String transform(String in) { }");
        System.out.println("\nAFTER:");
        System.out.println("  @Bean");
        System.out.println("  public Function<String, String> transform() {");
        System.out.println("    return in -> { };");
        System.out.println("  }\n");
    }
}

/**
 * Simple Order class for examples
 */
class Order {
    private String id;
    private String product;
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
}

/**
 * Main Pattern Class
 */
@Configuration
public class StreamListenerPattern {
    
    /**
     * Example: Stream Listener overview
     */
    @Bean
    public String streamListenerInfo() {
        System.out.println("Spring Cloud Stream Listener Pattern (LEGACY)");
        System.out.println("=============================================");
        System.out.println("  Status: Deprecated");
        System.out.println("  Replacement: Functional programming model");
        System.out.println("  Use: Function/Consumer/Supplier beans");
        return "Stream Listener Info";
    }
}

/**
 * Usage Examples and Best Practices
 */
class StreamListenerUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Stream Listener Pattern (LEGACY)");
        System.out.println("================================\n");
        
        System.out.println("⚠️  DEPRECATION NOTICE:");
        System.out.println("@StreamListener is deprecated.");
        System.out.println("Use functional programming model instead.\n");
        
        System.out.println("Modern Replacements:");
        System.out.println("1. Consumer<T> - Replace @StreamListener (void)");
        System.out.println("2. Function<I, O> - Replace @StreamListener + @SendTo");
        System.out.println("3. Supplier<O> - For message production\n");
        
        System.out.println("Benefits of Functional Model:");
        System.out.println("- Simpler programming model");
        System.out.println("- Better testability");
        System.out.println("- Reactive support");
        System.out.println("- Type safety");
        System.out.println("- No annotation complexity\n");
        
        System.out.println("Configuration:");
        System.out.println("spring:");
        System.out.println("  cloud:");
        System.out.println("    stream:");
        System.out.println("      function:");
        System.out.println("        definition: handleMessage");
        System.out.println("      bindings:");
        System.out.println("        handleMessage-in-0:");
        System.out.println("          destination: my-topic");
    }
}
