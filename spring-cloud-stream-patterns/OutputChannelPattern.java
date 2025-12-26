package com.example.cloudstream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Output Channel Pattern
 * ======================
 * 
 * Demonstrates @Output annotation that defines outbound message channels
 * for sending messages to external messaging middleware.
 * 
 * Key Concepts:
 * ------------
 * 1. @Output - Define output channel
 * 2. MessageChannel - Outbound message channel
 * 3. Channel Name - Unique identifier
 * 4. Message Producers - Channel senders
 * 5. Partitioning - Message routing
 * 
 * How It Works:
 * ------------
 * - @Output creates MessageChannel bean
 * - Channel name maps to binding destination
 * - Messages sent to middleware
 * - Supports partitioning
 * - Configurable serialization
 * 
 * Usage Patterns:
 * --------------
 * - Direct channel.send(message)
 * - @SendTo annotation
 * - Integration flows
 * - Stream bridge
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Output Channel
 */
interface BasicOutputChannels {
    String OUTPUT = "output";
    
    @Output(OUTPUT)
    MessageChannel output();
}

@Configuration
@EnableBinding(BasicOutputChannels.class)
class BasicOutputChannelExample {
    
    /**
     * Basic output channel
     * 
     * Configuration:
     * spring.cloud.stream.bindings.output:
     *   destination: orders
     * 
     * Usage:
     * @Autowired BasicOutputChannels channels;
     * channels.output().send(MessageBuilder.withPayload("data").build());
     */
    
    public void demonstrateBasicOutput() {
        System.out.println("Basic Output Channel");
        System.out.println("  Channel name: output");
        System.out.println("  Type: MessageChannel");
        System.out.println("  Destination: orders topic");
    }
}

/**
 * Example 2: Multiple Output Channels
 */
interface MultipleOutputChannels {
    String ORDERS = "orderOutput";
    String NOTIFICATIONS = "notificationOutput";
    String AUDIT = "auditOutput";
    
    @Output(ORDERS)
    MessageChannel orderOutput();
    
    @Output(NOTIFICATIONS)
    MessageChannel notificationOutput();
    
    @Output(AUDIT)
    MessageChannel auditOutput();
}

@Configuration
@EnableBinding(MultipleOutputChannels.class)
class MultipleOutputChannelsExample {
    
    /**
     * Multiple output channels
     * 
     * Configuration:
     * spring.cloud.stream.bindings:
     *   orderOutput:
     *     destination: orders
     *   notificationOutput:
     *     destination: notifications
     *   auditOutput:
     *     destination: audit-log
     */
    
    public void demonstrateMultipleOutputs() {
        System.out.println("Multiple Output Channels");
        System.out.println("  Channel 1: orderOutput -> orders");
        System.out.println("  Channel 2: notificationOutput -> notifications");
        System.out.println("  Channel 3: auditOutput -> audit-log");
    }
}

/**
 * Example 3: Output Channel with Content Type
 */
interface ContentTypeOutputChannel {
    String OUTPUT = "output";
    
    @Output(OUTPUT)
    MessageChannel output();
}

@Configuration
@EnableBinding(ContentTypeOutputChannel.class)
class ContentTypeOutputExample {
    
    /**
     * Output channel with content type
     * 
     * Configuration:
     * spring.cloud.stream.bindings.output:
     *   destination: orders
     *   content-type: application/json
     * 
     * Automatic serialization from:
     * - POJO objects
     * - Maps
     * - Collections
     */
    
    public void demonstrateContentType() {
        System.out.println("Output Channel with Content Type");
        System.out.println("  Content-Type: application/json");
        System.out.println("  Serialization: Automatic");
        System.out.println("  Source: Order POJO");
    }
}

/**
 * Example 4: Partitioned Output Channel
 */
interface PartitionedOutputChannel {
    String OUTPUT = "output";
    
    @Output(OUTPUT)
    MessageChannel output();
}

@Configuration
@EnableBinding(PartitionedOutputChannel.class)
class PartitionedOutputExample {
    
    /**
     * Partitioned output channel
     * 
     * Configuration:
     * spring.cloud.stream.bindings.output:
     *   destination: orders
     *   producer:
     *     partitionKeyExpression: payload.customerId
     *     partitionCount: 3
     * 
     * Messages routed by: customerId hash
     */
    
    public void demonstratePartitionedOutput() {
        System.out.println("Partitioned Output Channel");
        System.out.println("  Partition key: payload.customerId");
        System.out.println("  Partition count: 3");
        System.out.println("  Routing: Hash-based");
    }
}

/**
 * Example 5: Output Channel with Error Handling
 */
interface ErrorHandlingOutputChannel {
    String OUTPUT = "output";
    
    @Output(OUTPUT)
    MessageChannel output();
}

@Configuration
@EnableBinding(ErrorHandlingOutputChannel.class)
class ErrorHandlingOutputExample {
    
    /**
     * Output channel with error handling
     * 
     * Configuration:
     * spring.cloud.stream.bindings.output:
     *   producer:
     *     errorChannelEnabled: true
     * 
     * Error channel: output.errors
     */
    
    public void demonstrateErrorHandling() {
        System.out.println("Output Channel with Error Handling");
        System.out.println("  Error channel: enabled");
        System.out.println("  Error destination: output.errors");
        System.out.println("  Failed sends: Captured");
    }
}

/**
 * Example 6: Output Channel with Compression
 */
interface CompressedOutputChannel {
    String OUTPUT = "output";
    
    @Output(OUTPUT)
    MessageChannel output();
}

@Configuration
@EnableBinding(CompressedOutputChannel.class)
class CompressedOutputExample {
    
    /**
     * Output channel with compression
     * 
     * Configuration (Kafka):
     * spring.cloud.stream.kafka.bindings.output.producer:
     *   compression-type: gzip
     * 
     * Compression types:
     * - none
     * - gzip
     * - snappy
     * - lz4
     * - zstd
     */
    
    public void demonstrateCompression() {
        System.out.println("Output Channel with Compression");
        System.out.println("  Compression: gzip");
        System.out.println("  Benefit: Reduced network usage");
    }
}

/**
 * Example 7: Output Channel with Headers
 */
interface HeaderOutputChannel {
    String OUTPUT = "output";
    
    @Output(OUTPUT)
    MessageChannel output();
}

@Configuration
@EnableBinding(HeaderOutputChannel.class)
class HeaderOutputExample {
    
    /**
     * Output channel with headers
     * 
     * Usage:
     * Message<?> message = MessageBuilder
     *   .withPayload(order)
     *   .setHeader("type", "NEW_ORDER")
     *   .setHeader("priority", "HIGH")
     *   .setHeader("correlationId", uuid)
     *   .build();
     * channel.output().send(message);
     */
    
    public void demonstrateHeaders() {
        System.out.println("Output Channel with Headers");
        System.out.println("  Headers: type, priority, correlationId");
        System.out.println("  Use case: Message metadata");
    }
}

/**
 * Example 8: Output Channel with Required Acks
 */
interface RequiredAcksOutputChannel {
    String OUTPUT = "output";
    
    @Output(OUTPUT)
    MessageChannel output();
}

@Configuration
@EnableBinding(RequiredAcksOutputChannel.class)
class RequiredAcksOutputExample {
    
    /**
     * Output channel with required acks (Kafka)
     * 
     * Configuration:
     * spring.cloud.stream.kafka.bindings.output.producer:
     *   configuration:
     *     acks: all  # or 0, 1, all
     *     retries: 3
     *     max.in.flight.requests.per.connection: 1
     */
    
    public void demonstrateRequiredAcks() {
        System.out.println("Output Channel with Required Acks");
        System.out.println("  Acks: all (strongest guarantee)");
        System.out.println("  Retries: 3");
        System.out.println("  Idempotent: enabled");
    }
}

/**
 * Example 9: Output Channel with Use Native Encoding
 */
interface NativeEncodingOutputChannel {
    String OUTPUT = "output";
    
    @Output(OUTPUT)
    MessageChannel output();
}

@Configuration
@EnableBinding(NativeEncodingOutputChannel.class)
class NativeEncodingOutputExample {
    
    /**
     * Output channel with native encoding
     * 
     * Configuration:
     * spring.cloud.stream.bindings.output:
     *   producer:
     *     useNativeEncoding: true
     * spring.cloud.stream.kafka.bindings.output.producer:
     *   configuration:
     *     value.serializer: org.apache.kafka.common.serialization.ByteArraySerializer
     */
    
    public void demonstrateNativeEncoding() {
        System.out.println("Output Channel with Native Encoding");
        System.out.println("  Native encoding: enabled");
        System.out.println("  Serializer: Kafka native");
        System.out.println("  Use case: Custom serialization");
    }
}

/**
 * Example 10: Output Channel with Transaction
 */
interface TransactionalOutputChannel {
    String OUTPUT = "output";
    
    @Output(OUTPUT)
    MessageChannel output();
}

@Configuration
@EnableBinding(TransactionalOutputChannel.class)
class TransactionalOutputExample {
    
    /**
     * Transactional output channel (Kafka)
     * 
     * Configuration:
     * spring.cloud.stream.kafka.binder:
     *   transaction:
     *     transaction-id-prefix: tx-
     * spring.cloud.stream.kafka.bindings.output.producer:
     *   configuration:
     *     enable.idempotence: true
     */
    
    public void demonstrateTransaction() {
        System.out.println("Transactional Output Channel");
        System.out.println("  Transactions: enabled");
        System.out.println("  Transaction ID: tx-{binder-id}");
        System.out.println("  Semantics: Exactly-once");
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class OutputChannelPattern {
    
    /**
     * Core @Output concepts
     */
    public void demonstrateOutputChannelPattern() {
        System.out.println("\n=== Output Channel Pattern ===");
        System.out.println("Define outbound message channels");
        System.out.println("\nFeatures:");
        System.out.println("  - MessageChannel type");
        System.out.println("  - Partitioning support");
        System.out.println("  - Content type conversion");
        System.out.println("  - Error handling");
        System.out.println("  - Compression");
        System.out.println("  - Transactional sends");
    }
}

/**
 * Usage Examples and Best Practices
 */
class OutputChannelPatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Output Channel Pattern");
        System.out.println("======================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Send messages to middleware");
        System.out.println("- Define outbound channels");
        System.out.println("- Configure message production\n");
        
        System.out.println("Usage:");
        System.out.println("@Autowired");
        System.out.println("private MessageChannel output;");
        System.out.println("");
        System.out.println("output.send(MessageBuilder");
        System.out.println("  .withPayload(order)");
        System.out.println("  .setHeader(\"type\", \"ORDER\")");
        System.out.println("  .build());\n");
        
        System.out.println("Configuration:");
        System.out.println("spring:");
        System.out.println("  cloud:");
        System.out.println("    stream:");
        System.out.println("      bindings:");
        System.out.println("        output:");
        System.out.println("          destination: orders");
        System.out.println("          content-type: application/json\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Set appropriate content types");
        System.out.println("- Use partitioning for ordering");
        System.out.println("- Configure acks for durability");
        System.out.println("- Enable compression for large messages");
        System.out.println("- Use headers for metadata");
        System.out.println("- Handle send errors");
        System.out.println("- Use transactions when needed");
    }
}
