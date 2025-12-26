package com.example.cloudstream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * Processor Pattern
 * =================
 * 
 * Demonstrates Processor interface that combines both @Input and @Output
 * channels for message transformation scenarios.
 * 
 * Key Concepts:
 * ------------
 * 1. Processor - Bidirectional message flow
 * 2. Input Channel - Receive messages
 * 3. Output Channel - Send transformed messages
 * 4. Message Transformation - Process and forward
 * 5. @SendTo - Link input to output
 * 
 * How It Works:
 * ------------
 * - Receives messages from input channel
 * - Processes/transforms the message
 * - Sends result to output channel
 * - Typical pattern: consume -> transform -> produce
 * 
 * Built-in Processor Interface:
 * ----------------------------
 * public interface Processor extends Source, Sink {
 *   @Input(Sink.INPUT)
 *   SubscribableChannel input();
 * 
 *   @Output(Source.OUTPUT)
 *   MessageChannel output();
 * }
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Processor
 */
@Configuration
@EnableBinding(Processor.class)
class BasicProcessorExample {
    
    /**
     * Basic message processor
     * 
     * Configuration:
     * spring.cloud.stream.bindings:
     *   input:
     *     destination: raw-orders
     *     group: processor
     *   output:
     *     destination: processed-orders
     */
    
    public void demonstrateBasicProcessor() {
        System.out.println("Basic Processor");
        System.out.println("  Input: raw-orders");
        System.out.println("  Output: processed-orders");
        System.out.println("  Pattern: consume -> transform -> produce");
    }
}

/**
 * Example 2: String Transformer Processor
 */
@Configuration
@EnableBinding(Processor.class)
class StringTransformerProcessor {
    
    /**
     * Transform strings to uppercase
     * 
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public String transform(String input) {
     *   return input.toUpperCase();
     * }
     */
    
    public void demonstrateStringTransformer() {
        System.out.println("String Transformer Processor");
        System.out.println("  Input: lowercase strings");
        System.out.println("  Output: UPPERCASE STRINGS");
        System.out.println("  Transformation: toUpperCase()");
    }
}

/**
 * Example 3: Object Mapping Processor
 */
@Configuration
@EnableBinding(Processor.class)
class ObjectMappingProcessor {
    
    /**
     * Map Order to OrderDTO
     * 
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public OrderDTO transform(Order order) {
     *   return new OrderDTO(
     *     order.getId(),
     *     order.getCustomerId(),
     *     order.getTotal()
     *   );
     * }
     */
    
    public void demonstrateObjectMapping() {
        System.out.println("Object Mapping Processor");
        System.out.println("  Input: Order entity");
        System.out.println("  Output: OrderDTO");
        System.out.println("  Transformation: Entity to DTO");
    }
}

/**
 * Example 4: Enrichment Processor
 */
@Configuration
@EnableBinding(Processor.class)
class EnrichmentProcessor {
    
    /**
     * Enrich order with customer data
     * 
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public EnrichedOrder enrich(Order order) {
     *   Customer customer = customerService
     *     .findById(order.getCustomerId());
     *   return new EnrichedOrder(order, customer);
     * }
     */
    
    public void demonstrateEnrichment() {
        System.out.println("Enrichment Processor");
        System.out.println("  Input: Basic order");
        System.out.println("  Enrichment: Add customer data");
        System.out.println("  Output: Enriched order");
    }
}

/**
 * Example 5: Filtering Processor
 */
@Configuration
@EnableBinding(Processor.class)
class FilteringProcessor {
    
    /**
     * Filter high-value orders
     * 
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public Message<Order> filter(Order order) {
     *   if (order.getTotal() > 1000) {
     *     return MessageBuilder.withPayload(order).build();
     *   }
     *   return null;  // Filtered out
     * }
     */
    
    public void demonstrateFiltering() {
        System.out.println("Filtering Processor");
        System.out.println("  Input: All orders");
        System.out.println("  Filter: total > 1000");
        System.out.println("  Output: High-value orders only");
    }
}

/**
 * Example 6: Aggregation Processor
 */
@Configuration
@EnableBinding(Processor.class)
class AggregationProcessor {
    
    /**
     * Aggregate order items
     * 
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public OrderSummary aggregate(List<OrderItem> items) {
     *   double total = items.stream()
     *     .mapToDouble(OrderItem::getPrice)
     *     .sum();
     *   return new OrderSummary(items.size(), total);
     * }
     */
    
    public void demonstrateAggregation() {
        System.out.println("Aggregation Processor");
        System.out.println("  Input: List of order items");
        System.out.println("  Aggregation: Sum prices, count items");
        System.out.println("  Output: Order summary");
    }
}

/**
 * Example 7: Validation Processor
 */
@Configuration
@EnableBinding(Processor.class)
class ValidationProcessor {
    
    /**
     * Validate and forward orders
     * 
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public Order validate(Order order) {
     *   if (order.getTotal() <= 0) {
     *     throw new InvalidOrderException();
     *   }
     *   if (order.getCustomerId() == null) {
     *     throw new InvalidOrderException();
     *   }
     *   return order;
     * }
     */
    
    public void demonstrateValidation() {
        System.out.println("Validation Processor");
        System.out.println("  Input: Unvalidated order");
        System.out.println("  Validation: Check total, customerId");
        System.out.println("  Output: Valid order (or error)");
    }
}

/**
 * Example 8: Splitting Processor
 */
@Configuration
@EnableBinding(Processor.class)
class SplittingProcessor {
    
    /**
     * Split batch into individual messages
     * 
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public List<Order> split(OrderBatch batch) {
     *   return batch.getOrders();
     * }
     */
    
    public void demonstrateSplitting() {
        System.out.println("Splitting Processor");
        System.out.println("  Input: OrderBatch (multiple orders)");
        System.out.println("  Split: Extract individual orders");
        System.out.println("  Output: Individual Order messages");
    }
}

/**
 * Example 9: Routing Processor
 */
@Configuration
@EnableBinding(Processor.class)
class RoutingProcessor {
    
    /**
     * Route based on order type
     * 
     * @StreamListener(Processor.INPUT)
     * public void route(Order order, 
     *     @Output("highPriority") MessageChannel highPriority,
     *     @Output("lowPriority") MessageChannel lowPriority) {
     *   if (order.isPriority()) {
     *     highPriority.send(MessageBuilder.withPayload(order).build());
     *   } else {
     *     lowPriority.send(MessageBuilder.withPayload(order).build());
     *   }
     * }
     */
    
    public void demonstrateRouting() {
        System.out.println("Routing Processor");
        System.out.println("  Input: All orders");
        System.out.println("  Route by: Priority flag");
        System.out.println("  Output 1: High priority orders");
        System.out.println("  Output 2: Low priority orders");
    }
}

/**
 * Example 10: Async Processor
 */
@Configuration
@EnableBinding(Processor.class)
class AsyncProcessor {
    
    /**
     * Asynchronous message processing
     * 
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public CompletableFuture<Order> processAsync(Order order) {
     *   return CompletableFuture.supplyAsync(() -> {
     *     // Long-running processing
     *     enrichOrder(order);
     *     return order;
     *   });
     * }
     */
    
    public void demonstrateAsyncProcessing() {
        System.out.println("Async Processor");
        System.out.println("  Input: Order");
        System.out.println("  Processing: Asynchronous");
        System.out.println("  Output: Processed order (when complete)");
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ProcessorPattern {
    
    /**
     * Core Processor concepts
     */
    public void demonstrateProcessorPattern() {
        System.out.println("\n=== Processor Pattern ===");
        System.out.println("Transform messages: input -> process -> output");
        System.out.println("\nCommon Transformations:");
        System.out.println("  - String transformation");
        System.out.println("  - Object mapping");
        System.out.println("  - Enrichment");
        System.out.println("  - Filtering");
        System.out.println("  - Aggregation");
        System.out.println("  - Validation");
        System.out.println("  - Splitting");
        System.out.println("  - Routing");
    }
}

/**
 * Usage Examples and Best Practices
 */
class ProcessorPatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Processor Pattern");
        System.out.println("=================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Transform messages");
        System.out.println("- Process and forward");
        System.out.println("- Enrich, filter, validate\n");
        
        System.out.println("Configuration:");
        System.out.println("spring:");
        System.out.println("  cloud:");
        System.out.println("    stream:");
        System.out.println("      bindings:");
        System.out.println("        input:");
        System.out.println("          destination: raw-data");
        System.out.println("          group: processor");
        System.out.println("        output:");
        System.out.println("          destination: processed-data\n");
        
        System.out.println("Usage:");
        System.out.println("@StreamListener(Processor.INPUT)");
        System.out.println("@SendTo(Processor.OUTPUT)");
        System.out.println("public String transform(String input) {");
        System.out.println("  return input.toUpperCase();");
        System.out.println("}\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Keep transformations simple");
        System.out.println("- Handle errors gracefully");
        System.out.println("- Use appropriate return types");
        System.out.println("- Consider async for long operations");
        System.out.println("- Validate input data");
        System.out.println("- Log transformations");
        System.out.println("- Test thoroughly");
    }
}
