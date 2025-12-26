package com.example.cloudstream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;

/**
 * Input Channel Pattern
 * ====================
 * 
 * Demonstrates @Input annotation that defines inbound message channels
 * for receiving messages from external messaging middleware.
 * 
 * Key Concepts:
 * ------------
 * 1. @Input - Define input channel
 * 2. SubscribableChannel - Inbound message channel
 * 3. Channel Name - Unique identifier
 * 4. Message Consumers - Channel subscribers
 * 5. Consumer Groups - Load balancing
 * 
 * How It Works:
 * ------------
 * - @Input creates SubscribableChannel bean
 * - Channel name maps to binding destination
 * - Messages arrive from middleware
 * - Multiple consumers can subscribe
 * - Consumer groups enable load balancing
 * 
 * Usage Patterns:
 * --------------
 * - @StreamListener on @Input channel
 * - @ServiceActivator with inputChannel
 * - Direct subscription to channel
 * - Integration flows
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Input Channel
 */
interface BasicInputChannels {
    String INPUT = "input";
    
    @Input(INPUT)
    SubscribableChannel input();
}

@Configuration
@EnableBinding(BasicInputChannels.class)
class BasicInputChannelExample {
    
    /**
     * Basic input channel
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   destination: orders
     *   group: order-processors
     */
    
    public void demonstrateBasicInput() {
        System.out.println("Basic Input Channel");
        System.out.println("  Channel name: input");
        System.out.println("  Type: SubscribableChannel");
        System.out.println("  Destination: orders topic");
        System.out.println("  Group: order-processors");
    }
}

/**
 * Example 2: Multiple Input Channels
 */
interface MultipleInputChannels {
    String ORDERS = "orderInput";
    String PAYMENTS = "paymentInput";
    String NOTIFICATIONS = "notificationInput";
    
    @Input(ORDERS)
    SubscribableChannel orderInput();
    
    @Input(PAYMENTS)
    SubscribableChannel paymentInput();
    
    @Input(NOTIFICATIONS)
    SubscribableChannel notificationInput();
}

@Configuration
@EnableBinding(MultipleInputChannels.class)
class MultipleInputChannelsExample {
    
    /**
     * Multiple input channels
     * 
     * Configuration:
     * spring.cloud.stream.bindings:
     *   orderInput:
     *     destination: orders
     *     group: order-service
     *   paymentInput:
     *     destination: payments
     *     group: order-service
     *   notificationInput:
     *     destination: notifications
     *     group: order-service
     */
    
    public void demonstrateMultipleInputs() {
        System.out.println("Multiple Input Channels");
        System.out.println("  Channel 1: orderInput -> orders");
        System.out.println("  Channel 2: paymentInput -> payments");
        System.out.println("  Channel 3: notificationInput -> notifications");
    }
}

/**
 * Example 3: Input Channel with Consumer Group
 */
interface ConsumerGroupInputChannel {
    String INPUT = "input";
    
    @Input(INPUT)
    SubscribableChannel input();
}

@Configuration
@EnableBinding(ConsumerGroupInputChannel.class)
class ConsumerGroupInputExample {
    
    /**
     * Input channel with consumer group
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   destination: events
     *   group: event-processor-group
     * 
     * Benefits:
     * - Load balancing across instances
     * - Persistent subscription (Kafka)
     * - Durable queues (RabbitMQ)
     * - Exactly-once processing
     */
    
    public void demonstrateConsumerGroup() {
        System.out.println("Input Channel with Consumer Group");
        System.out.println("  Group: event-processor-group");
        System.out.println("  Instances: 3 replicas");
        System.out.println("  Load balancing: Enabled");
        System.out.println("  Message delivery: Each message to one instance");
    }
}

/**
 * Example 4: Input Channel with Content Type
 */
interface ContentTypeInputChannel {
    String INPUT = "input";
    
    @Input(INPUT)
    SubscribableChannel input();
}

@Configuration
@EnableBinding(ContentTypeInputChannel.class)
class ContentTypeInputExample {
    
    /**
     * Input channel with content type
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   destination: orders
     *   content-type: application/json
     * 
     * Automatic deserialization to:
     * - POJO objects
     * - Maps
     * - Collections
     */
    
    public void demonstrateContentType() {
        System.out.println("Input Channel with Content Type");
        System.out.println("  Content-Type: application/json");
        System.out.println("  Deserialization: Automatic");
        System.out.println("  Target: Order POJO");
    }
}

/**
 * Example 5: Partitioned Input Channel
 */
interface PartitionedInputChannel {
    String INPUT = "input";
    
    @Input(INPUT)
    SubscribableChannel input();
}

@Configuration
@EnableBinding(PartitionedInputChannel.class)
class PartitionedInputExample {
    
    /**
     * Partitioned input channel
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   destination: orders
     *   group: order-processors
     *   consumer:
     *     partitioned: true
     * spring.cloud.stream.instanceCount: 3
     * spring.cloud.stream.instanceIndex: 0
     */
    
    public void demonstratePartitionedInput() {
        System.out.println("Partitioned Input Channel");
        System.out.println("  Partitioned: true");
        System.out.println("  Instance count: 3");
        System.out.println("  Instance index: 0");
        System.out.println("  Each instance: Specific partitions");
    }
}

/**
 * Example 6: Input Channel with Error Handling
 */
interface ErrorHandlingInputChannel {
    String INPUT = "input";
    
    @Input(INPUT)
    SubscribableChannel input();
}

@Configuration
@EnableBinding(ErrorHandlingInputChannel.class)
class ErrorHandlingInputExample {
    
    /**
     * Input channel with error handling
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   consumer:
     *     maxAttempts: 3
     *     backOffInitialInterval: 1000
     *     backOffMaxInterval: 10000
     *     backOffMultiplier: 2.0
     *   destination: orders
     */
    
    public void demonstrateErrorHandling() {
        System.out.println("Input Channel with Error Handling");
        System.out.println("  Max attempts: 3");
        System.out.println("  Initial backoff: 1000ms");
        System.out.println("  Max backoff: 10000ms");
        System.out.println("  Backoff multiplier: 2.0");
    }
}

/**
 * Example 7: Input Channel with Dead Letter Queue
 */
interface DLQInputChannel {
    String INPUT = "input";
    
    @Input(INPUT)
    SubscribableChannel input();
}

@Configuration
@EnableBinding(DLQInputChannel.class)
class DLQInputExample {
    
    /**
     * Input channel with DLQ
     * 
     * Configuration (Kafka):
     * spring.cloud.stream.kafka.bindings.input.consumer:
     *   enableDlq: true
     *   dlqName: error.orders.order-service
     *   autoCommitOnError: true
     * 
     * Configuration (RabbitMQ):
     * spring.cloud.stream.rabbit.bindings.input.consumer:
     *   republishToDlq: true
     *   dlqDeadLetterExchange: errors
     */
    
    public void demonstrateDLQ() {
        System.out.println("Input Channel with Dead Letter Queue");
        System.out.println("  DLQ enabled: true");
        System.out.println("  DLQ name: error.orders.order-service");
        System.out.println("  Failed messages: Sent to DLQ");
    }
}

/**
 * Example 8: Batch Input Channel
 */
interface BatchInputChannel {
    String INPUT = "input";
    
    @Input(INPUT)
    SubscribableChannel input();
}

@Configuration
@EnableBinding(BatchInputChannel.class)
class BatchInputExample {
    
    /**
     * Batch input channel
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   consumer:
     *     batch-mode: true
     * spring.cloud.stream.kafka.bindings.input.consumer:
     *   max-poll-records: 100
     */
    
    public void demonstrateBatchInput() {
        System.out.println("Batch Input Channel");
        System.out.println("  Batch mode: enabled");
        System.out.println("  Max poll records: 100");
        System.out.println("  Receive: List<Message<?>");
    }
}

/**
 * Example 9: Input Channel with ServiceActivator
 */
interface ServiceActivatorInputChannel {
    String INPUT = "orderInput";
    
    @Input(INPUT)
    SubscribableChannel orderInput();
}

@Configuration
@EnableBinding(ServiceActivatorInputChannel.class)
class ServiceActivatorInputExample {
    
    /**
     * Use @ServiceActivator to consume messages
     */
    @ServiceActivator(inputChannel = "orderInput")
    public void handleOrder(Message<?> message) {
        System.out.println("ServiceActivator Input");
        System.out.println("  Received: " + message.getPayload());
        System.out.println("  Headers: " + message.getHeaders());
    }
}

/**
 * Example 10: Input Channel with Conditional Routing
 */
interface ConditionalInputChannels {
    String HIGH_PRIORITY = "highPriorityInput";
    String LOW_PRIORITY = "lowPriorityInput";
    
    @Input(HIGH_PRIORITY)
    SubscribableChannel highPriorityInput();
    
    @Input(LOW_PRIORITY)
    SubscribableChannel lowPriorityInput();
}

@Configuration
@EnableBinding(ConditionalInputChannels.class)
class ConditionalInputExample {
    
    /**
     * Multiple inputs for different priorities
     * 
     * Configuration:
     * spring.cloud.stream.bindings:
     *   highPriorityInput:
     *     destination: orders.high
     *     group: processor
     *   lowPriorityInput:
     *     destination: orders.low
     *     group: processor
     */
    
    public void demonstrateConditionalInput() {
        System.out.println("Conditional Input Channels");
        System.out.println("  High priority: orders.high");
        System.out.println("  Low priority: orders.low");
        System.out.println("  Processing: Priority-based");
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class InputChannelPattern {
    
    /**
     * Core @Input concepts
     */
    public void demonstrateInputChannelPattern() {
        System.out.println("\n=== Input Channel Pattern ===");
        System.out.println("Define inbound message channels");
        System.out.println("\nFeatures:");
        System.out.println("  - SubscribableChannel type");
        System.out.println("  - Consumer groups");
        System.out.println("  - Content type conversion");
        System.out.println("  - Partitioning support");
        System.out.println("  - Error handling");
        System.out.println("  - Dead letter queues");
    }
}

/**
 * Usage Examples and Best Practices
 */
class InputChannelPatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Input Channel Pattern");
        System.out.println("=====================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Receive messages from middleware");
        System.out.println("- Define inbound channels");
        System.out.println("- Configure message consumption\n");
        
        System.out.println("Configuration:");
        System.out.println("spring:");
        System.out.println("  cloud:");
        System.out.println("    stream:");
        System.out.println("      bindings:");
        System.out.println("        input:");
        System.out.println("          destination: orders");
        System.out.println("          group: order-service");
        System.out.println("          content-type: application/json\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Always use consumer groups");
        System.out.println("- Configure error handling");
        System.out.println("- Enable DLQ for production");
        System.out.println("- Use partitioning for scale");
        System.out.println("- Set appropriate content types");
        System.out.println("- Monitor channel health");
        System.out.println("- Use batch mode for high throughput");
    }
}
