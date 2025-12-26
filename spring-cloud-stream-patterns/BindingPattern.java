package com.example.cloudstream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Binding Pattern
 * ===============
 * 
 * Demonstrates Spring Cloud Stream Bindings that connect application 
 * code to external messaging systems through named channels. Bindings
 * map logical channel names to physical destinations (topics/queues).
 * 
 * Key Concepts:
 * ------------
 * 1. Input Binding - Consume messages from external system
 * 2. Output Binding - Produce messages to external system
 * 3. Binding Properties - Configure destination, group, content-type
 * 4. Dynamic Bindings - Create bindings at runtime
 * 5. Binding Lifecycle - Start/stop bindings programmatically
 * 
 * How It Works:
 * ------------
 * - Binding connects channel to destination (topic/queue)
 * - Input bindings for consumers
 * - Output bindings for producers
 * - Configured via spring.cloud.stream.bindings.*
 * - Supports functional programming model (Java 8+)
 * 
 * Binding Configuration:
 * ---------------------
 * spring:
 *   cloud:
 *     stream:
 *       bindings:
 *         <channelName>:
 *           destination: <topic/queue name>
 *           group: <consumer group>
 *           contentType: application/json
 *           binder: <binder name>
 *           consumer/producer: <specific properties>
 * 
 * Functional Model (Recommended):
 * ------------------------------
 * - Use java.util.function.Function/Consumer/Supplier
 * - Automatic binding creation
 * - Convention: <functionName>-in-<index> / <functionName>-out-<index>
 * 
 * Legacy Model:
 * ------------
 * - @EnableBinding interface
 * - @Input/@Output annotations
 * - Custom binding interfaces
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Functional Binding (Recommended)
 */
@Configuration
class FunctionalBindingConfiguration {
    
    /**
     * Function-based binding (auto-creates bindings)
     * 
     * Bindings created automatically:
     * - processOrder-in-0 (input)
     * - processOrder-out-0 (output)
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       function:
     *         definition: processOrder
     *       bindings:
     *         processOrder-in-0:
     *           destination: orders
     *           group: order-processor
     *         processOrder-out-0:
     *           destination: processed-orders
     */
    @Bean
    public java.util.function.Function<String, String> processOrder() {
        return order -> {
            System.out.println("Processing order: " + order);
            System.out.println("  Input binding: processOrder-in-0");
            System.out.println("  Output binding: processOrder-out-0");
            return "Processed: " + order;
        };
    }
}

/**
 * Example 2: Consumer Binding
 */
@Configuration
class ConsumerBindingConfiguration {
    
    /**
     * Consumer-based binding (input only)
     * 
     * Binding created:
     * - logMessage-in-0
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       function:
     *         definition: logMessage
     *       bindings:
     *         logMessage-in-0:
     *           destination: log-events
     *           group: logger
     *           contentType: text/plain
     */
    @Bean
    public java.util.function.Consumer<String> logMessage() {
        return message -> {
            System.out.println("Logging message: " + message);
            System.out.println("  Input binding: logMessage-in-0");
            System.out.println("  No output binding (Consumer)");
        };
    }
}

/**
 * Example 3: Supplier Binding
 */
@Configuration
class SupplierBindingConfiguration {
    
    /**
     * Supplier-based binding (output only)
     * 
     * Binding created:
     * - generateEvent-out-0
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       function:
     *         definition: generateEvent
     *       bindings:
     *         generateEvent-out-0:
     *           destination: events
     *           producer:
     *             partitionCount: 3
     *       poller:
     *         fixed-delay: 5000
     */
    @Bean
    public java.util.function.Supplier<String> generateEvent() {
        return () -> {
            String event = "Event-" + System.currentTimeMillis();
            System.out.println("Generating event: " + event);
            System.out.println("  Output binding: generateEvent-out-0");
            return event;
        };
    }
}

/**
 * Example 4: Multiple Input Bindings
 */
@Configuration
class MultipleInputBindingsConfiguration {
    
    /**
     * Function with multiple inputs
     * 
     * Bindings created:
     * - mergeStreams-in-0 (first input)
     * - mergeStreams-in-1 (second input)
     * - mergeStreams-out-0 (output)
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       function:
     *         definition: mergeStreams
     *       bindings:
     *         mergeStreams-in-0:
     *           destination: stream-a
     *         mergeStreams-in-1:
     *           destination: stream-b
     *         mergeStreams-out-0:
     *           destination: merged-stream
     */
    @Bean
    public java.util.function.BiFunction<String, String, String> mergeStreams() {
        return (streamA, streamB) -> {
            System.out.println("Merging streams");
            System.out.println("  Input binding 0: " + streamA);
            System.out.println("  Input binding 1: " + streamB);
            return streamA + " + " + streamB;
        };
    }
}

/**
 * Example 5: Multiple Output Bindings
 */
@Configuration
class MultipleOutputBindingsConfiguration {
    
    /**
     * Function with multiple outputs
     * 
     * Bindings created:
     * - splitMessage-in-0 (input)
     * - splitMessage-out-0 (first output)
     * - splitMessage-out-1 (second output)
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       function:
     *         definition: splitMessage
     *       bindings:
     *         splitMessage-in-0:
     *           destination: messages
     *         splitMessage-out-0:
     *           destination: urgent-messages
     *         splitMessage-out-1:
     *           destination: normal-messages
     */
    @Bean
    public java.util.function.Function<String, String[]> splitMessage() {
        return message -> {
            System.out.println("Splitting message: " + message);
            System.out.println("  Output binding 0: urgent-messages");
            System.out.println("  Output binding 1: normal-messages");
            return new String[]{"Urgent: " + message, "Normal: " + message};
        };
    }
}

/**
 * Example 6: Binding with Consumer Group
 */
@Configuration
class ConsumerGroupBindingConfiguration {
    
    /**
     * Configure consumer group for load balancing
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         processPayment-in-0:
     *           destination: payments
     *           group: payment-processor
     *           consumer:
     *             maxAttempts: 3
     *             backOffInitialInterval: 1000
     */
    @Bean
    public java.util.function.Consumer<String> processPayment() {
        return payment -> {
            System.out.println("Processing payment: " + payment);
            System.out.println("  Consumer group: payment-processor");
            System.out.println("  Load balanced across instances");
        };
    }
}

/**
 * Example 7: Binding with Content Type
 */
@Configuration
class ContentTypeBindingConfiguration {
    
    /**
     * Configure content type for automatic conversion
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         handleJson-in-0:
     *           destination: json-data
     *           contentType: application/json
     *         handleAvro-in-0:
     *           destination: avro-data
     *           contentType: application/*+avro
     */
    @Bean
    public java.util.function.Consumer<Object> handleJson() {
        return data -> {
            System.out.println("Handling JSON data: " + data);
            System.out.println("  Content type: application/json");
            System.out.println("  Automatic deserialization");
        };
    }
}

/**
 * Example 8: Binding with Partitioning
 */
@Configuration
class PartitionedBindingConfiguration {
    
    /**
     * Configure partitioning for ordered processing
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         sendOrder-out-0:
     *           destination: orders
     *           producer:
     *             partitionCount: 3
     *             partitionKeyExpression: headers['userId']
     *         receiveOrder-in-0:
     *           destination: orders
     *           group: order-consumer
     *           consumer:
     *             partitioned: true
     *             instanceIndex: 0
     *             instanceCount: 3
     */
    @Bean
    public java.util.function.Function<String, String> sendOrder() {
        return order -> {
            System.out.println("Sending partitioned order: " + order);
            System.out.println("  Partition key: userId header");
            System.out.println("  Ensures ordering per user");
            return order;
        };
    }
}

/**
 * Example 9: Binding with Error Handling
 */
@Configuration
class ErrorHandlingBindingConfiguration {
    
    /**
     * Configure error handling for bindings
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         processWithRetry-in-0:
     *           destination: risky-operations
     *           group: processor
     *           consumer:
     *             maxAttempts: 3
     *             backOffInitialInterval: 1000
     *             backOffMaxInterval: 10000
     *             backOffMultiplier: 2.0
     *       kafka:
     *         bindings:
     *           processWithRetry-in-0:
     *             consumer:
     *               enableDlq: true
     *               dlqName: errors.risky-operations
     */
    @Bean
    public java.util.function.Consumer<String> processWithRetry() {
        return data -> {
            System.out.println("Processing with retry: " + data);
            System.out.println("  Max attempts: 3");
            System.out.println("  DLQ: errors.risky-operations");
        };
    }
}

/**
 * Example 10: Dynamic Binding Creation
 */
@Configuration
class DynamicBindingConfiguration {
    
    /**
     * Create bindings dynamically at runtime
     * 
     * Uses BindingService to create/destroy bindings
     */
    public void createDynamicBinding() {
        System.out.println("Dynamic Binding Creation");
        System.out.println("  Use BindingService.bindProducer()");
        System.out.println("  Use BindingService.bindConsumer()");
        System.out.println("  Useful for multi-tenant scenarios");
        System.out.println("  Create bindings based on runtime conditions");
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class BindingPattern {
    
    /**
     * Example: Binding overview
     */
    @Bean
    public String bindingInfo() {
        System.out.println("Spring Cloud Stream Binding Pattern");
        System.out.println("===================================");
        System.out.println("  Purpose: Connect channels to destinations");
        System.out.println("  Types: Input (consumer), Output (producer)");
        System.out.println("  Model: Functional (recommended) or Legacy");
        return "Binding Info";
    }
}

/**
 * Usage Examples and Best Practices
 */
class BindingUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Binding Pattern - Spring Cloud Stream");
        System.out.println("=====================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Map logical channels to physical destinations");
        System.out.println("- Configure input/output messaging");
        System.out.println("- Enable functional programming model\n");
        
        System.out.println("Binding Types:");
        System.out.println("1. Input Binding - Consume from destination");
        System.out.println("2. Output Binding - Produce to destination");
        System.out.println("3. Bidirectional - Both input and output\n");
        
        System.out.println("Functional Model (Recommended):");
        System.out.println("- Function<I, O> - Transform (in + out)");
        System.out.println("- Consumer<I> - Sink (in only)");
        System.out.println("- Supplier<O> - Source (out only)");
        System.out.println("- Automatic binding creation\n");
        
        System.out.println("Binding Properties:");
        System.out.println("- destination - Topic/queue name");
        System.out.println("- group - Consumer group (load balancing)");
        System.out.println("- contentType - Message format");
        System.out.println("- binder - Specific binder to use");
        System.out.println("- consumer/producer - Type-specific config\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use functional model for new applications");
        System.out.println("- Always set consumer group for load balancing");
        System.out.println("- Configure content type explicitly");
        System.out.println("- Use partitioning for ordered processing");
        System.out.println("- Enable error handling (retry + DLQ)");
        System.out.println("- Monitor binding health");
        System.out.println("- Document binding contracts\n");
        
        System.out.println("Example Configuration:");
        System.out.println("spring:");
        System.out.println("  cloud:");
        System.out.println("    stream:");
        System.out.println("      function:");
        System.out.println("        definition: processOrder");
        System.out.println("      bindings:");
        System.out.println("        processOrder-in-0:");
        System.out.println("          destination: orders");
        System.out.println("          group: order-processor");
        System.out.println("          contentType: application/json");
        System.out.println("        processOrder-out-0:");
        System.out.println("          destination: processed-orders");
    }
}
