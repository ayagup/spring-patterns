package com.example.cloudstream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Configuration;

/**
 * Partitioning Pattern
 * ====================
 * 
 * Demonstrates message partitioning for ordered processing and
 * load distribution across consumer instances.
 * 
 * Key Concepts:
 * ------------
 * 1. Partitioning - Message routing strategy
 * 2. Partition Key - Routing determinant
 * 3. Partition Count - Number of partitions
 * 4. Instance Index - Consumer instance identifier
 * 5. Ordered Processing - Same partition = same consumer
 * 
 * How It Works:
 * ------------
 * - Messages with same key -> same partition
 * - Each partition -> specific consumer instance
 * - Guarantees order within partition
 * - Enables horizontal scaling
 * - Load balancing across instances
 * 
 * Configuration (Producer):
 * ------------------------
 * spring.cloud.stream.bindings.output:
 *   producer:
 *     partitionKeyExpression: payload.customerId
 *     partitionCount: 3
 * 
 * Configuration (Consumer):
 * ------------------------
 * spring.cloud.stream.bindings.input:
 *   consumer:
 *     partitioned: true
 * spring.cloud.stream.instanceCount: 3
 * spring.cloud.stream.instanceIndex: 0
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Customer-Based Partitioning
 */
@Configuration
@EnableBinding(Processor.class)
class CustomerPartitioningExample {
    
    /**
     * Partition by customer ID
     * 
     * Producer:
     * spring.cloud.stream.bindings.output.producer:
     *   partitionKeyExpression: payload.customerId
     *   partitionCount: 3
     * 
     * Consumer:
     * spring.cloud.stream.bindings.input.consumer:
     *   partitioned: true
     * spring.cloud.stream.instanceCount: 3
     * spring.cloud.stream.instanceIndex: ${INSTANCE_INDEX:0}
     */
    
    public void demonstrateCustomerPartitioning() {
        System.out.println("Customer-Based Partitioning");
        System.out.println("  Partition key: customerId");
        System.out.println("  Partition count: 3");
        System.out.println("  Guarantee: Same customer -> same instance");
        System.out.println("  Use case: Customer session state");
    }
}

/**
 * Example 2: Order ID Partitioning
 */
@Configuration
@EnableBinding(Processor.class)
class OrderIdPartitioningExample {
    
    /**
     * Partition by order ID
     * 
     * Producer:
     * spring.cloud.stream.bindings.output.producer:
     *   partitionKeyExpression: payload.orderId
     *   partitionCount: 5
     * 
     * Benefits:
     * - Order processing order guaranteed
     * - Related order events to same consumer
     * - Simplified state management
     */
    
    public void demonstrateOrderIdPartitioning() {
        System.out.println("Order ID Partitioning");
        System.out.println("  Partition key: orderId");
        System.out.println("  Partition count: 5");
        System.out.println("  Guarantee: Order events in sequence");
    }
}

/**
 * Example 3: Hash-Based Partitioning
 */
@Configuration
@EnableBinding(Processor.class)
class HashBasedPartitioningExample {
    
    /**
     * Use hash function for partitioning
     * 
     * Producer:
     * spring.cloud.stream.bindings.output.producer:
     *   partitionKeyExpression: payload.id.hashCode()
     *   partitionCount: 4
     * 
     * Distribution:
     * - Even distribution across partitions
     * - Deterministic routing
     * - Scalable partitioning
     */
    
    public void demonstrateHashPartitioning() {
        System.out.println("Hash-Based Partitioning");
        System.out.println("  Partition key: id.hashCode()");
        System.out.println("  Partition count: 4");
        System.out.println("  Distribution: Even across instances");
    }
}

/**
 * Example 4: Custom Partition Strategy
 */
@Configuration
@EnableBinding(Processor.class)
class CustomPartitionStrategyExample {
    
    /**
     * Custom partitioning logic
     * 
     * Producer:
     * spring.cloud.stream.bindings.output.producer:
     *   partitionKeyExpression: headers['partitionKey']
     *   partitionCount: 3
     * 
     * Set partition key in code:
     * Message<?> message = MessageBuilder
     *   .withPayload(order)
     *   .setHeader("partitionKey", order.getRegion())
     *   .build();
     */
    
    public void demonstrateCustomPartitionStrategy() {
        System.out.println("Custom Partition Strategy");
        System.out.println("  Partition key: header 'partitionKey'");
        System.out.println("  Logic: Business-specific");
        System.out.println("  Example: Region-based routing");
    }
}

/**
 * Example 5: Multi-Field Partitioning
 */
@Configuration
@EnableBinding(Processor.class)
class MultiFieldPartitioningExample {
    
    /**
     * Partition by multiple fields
     * 
     * Producer:
     * spring.cloud.stream.bindings.output.producer:
     *   partitionKeyExpression: payload.tenantId + '-' + payload.region
     *   partitionCount: 6
     * 
     * Composite key:
     * - tenantId + region
     * - More granular distribution
     * - Multi-dimensional partitioning
     */
    
    public void demonstrateMultiFieldPartitioning() {
        System.out.println("Multi-Field Partitioning");
        System.out.println("  Partition key: tenantId + region");
        System.out.println("  Partition count: 6");
        System.out.println("  Use case: Multi-tenant, multi-region");
    }
}

/**
 * Example 6: Dynamic Partition Count
 */
@Configuration
@EnableBinding(Processor.class)
class DynamicPartitionCountExample {
    
    /**
     * Dynamic partition configuration
     * 
     * Producer:
     * spring.cloud.stream.bindings.output.producer:
     *   partitionKeyExpression: payload.customerId
     *   partitionCount: ${PARTITION_COUNT:3}
     * 
     * Consumer:
     * spring.cloud.stream.instanceCount: ${INSTANCE_COUNT:3}
     * spring.cloud.stream.instanceIndex: ${INSTANCE_INDEX:0}
     * 
     * Scale by:
     * - Increasing PARTITION_COUNT
     * - Increasing INSTANCE_COUNT
     * - Deploying more instances
     */
    
    public void demonstrateDynamicPartitioning() {
        System.out.println("Dynamic Partition Count");
        System.out.println("  Config: Environment variables");
        System.out.println("  PARTITION_COUNT: Configurable");
        System.out.println("  INSTANCE_COUNT: Configurable");
        System.out.println("  Scaling: Dynamic");
    }
}

/**
 * Example 7: Kafka Native Partitioning
 */
@Configuration
@EnableBinding(Processor.class)
class KafkaNativePartitioningExample {
    
    /**
     * Use Kafka's native partitioning
     * 
     * Kafka-specific:
     * spring.cloud.stream.kafka.bindings.output.producer:
     *   messageKeyExpression: payload.customerId
     * 
     * Kafka handles:
     * - Partition assignment
     * - Rebalancing
     * - Consumer group coordination
     */
    
    public void demonstrateKafkaNativePartitioning() {
        System.out.println("Kafka Native Partitioning");
        System.out.println("  Message key: customerId");
        System.out.println("  Partitioner: Kafka default");
        System.out.println("  Benefit: Kafka-optimized");
    }
}

/**
 * Example 8: Partition-Aware Consumer
 */
@Configuration
@EnableBinding(Processor.class)
class PartitionAwareConsumerExample {
    
    /**
     * Consumer aware of partition
     * 
     * @StreamListener(Processor.INPUT)
     * public void handle(
     *     Order order,
     *     @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
     *   System.out.println("Processing order from partition: " + partition);
     *   processOrder(order, partition);
     * }
     */
    
    public void demonstratePartitionAwareConsumer() {
        System.out.println("Partition-Aware Consumer");
        System.out.println("  Access: Partition ID from header");
        System.out.println("  Use case: Partition-specific processing");
        System.out.println("  Example: Per-partition metrics");
    }
}

/**
 * Example 9: Stateful Partitioned Processing
 */
@Configuration
@EnableBinding(Processor.class)
class StatefulPartitionedProcessingExample {
    
    /**
     * Maintain state per partition
     * 
     * Each instance:
     * - Processes specific partitions
     * - Maintains local state for those partitions
     * - No shared state needed
     * 
     * Benefits:
     * - No coordination overhead
     * - Local caching per partition
     * - Simplified state management
     */
    
    public void demonstrateStatefulProcessing() {
        System.out.println("Stateful Partitioned Processing");
        System.out.println("  State: Per-partition local cache");
        System.out.println("  Coordination: None needed");
        System.out.println("  Use case: Aggregations, counting");
    }
}

/**
 * Example 10: Partition Rebalancing
 */
@Configuration
@EnableBinding(Processor.class)
class PartitionRebalancingExample {
    
    /**
     * Handle partition rebalancing
     * 
     * When instances added/removed:
     * - Kafka rebalances partitions
     * - Some consumers lose partitions
     * - Some consumers gain partitions
     * 
     * Handle with:
     * - Rebalance listeners
     * - State persistence
     * - Graceful handoff
     */
    
    public void demonstratePartitionRebalancing() {
        System.out.println("Partition Rebalancing");
        System.out.println("  Trigger: Instance count change");
        System.out.println("  Action: Reassign partitions");
        System.out.println("  Handle: Rebalance listeners");
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class PartitioningPattern {
    
    /**
     * Core Partitioning concepts
     */
    public void demonstratePartitioningPattern() {
        System.out.println("\n=== Partitioning Pattern ===");
        System.out.println("Ordered processing and load distribution");
        System.out.println("\nKey Benefits:");
        System.out.println("  - Guaranteed message order per key");
        System.out.println("  - Horizontal scaling");
        System.out.println("  - Load balancing");
        System.out.println("  - Stateful processing");
        System.out.println("\nCommon Partition Keys:");
        System.out.println("  - Customer ID");
        System.out.println("  - Order ID");
        System.out.println("  - Tenant ID");
        System.out.println("  - Region/Location");
        System.out.println("  - Hash of entity ID");
    }
}

/**
 * Usage Examples and Best Practices
 */
class PartitioningPatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Partitioning Pattern");
        System.out.println("====================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Ordered message processing");
        System.out.println("- Load distribution");
        System.out.println("- Horizontal scaling\n");
        
        System.out.println("Producer Configuration:");
        System.out.println("spring.cloud.stream.bindings.output.producer:");
        System.out.println("  partitionKeyExpression: payload.customerId");
        System.out.println("  partitionCount: 3\n");
        
        System.out.println("Consumer Configuration:");
        System.out.println("spring.cloud.stream.bindings.input.consumer:");
        System.out.println("  partitioned: true");
        System.out.println("spring.cloud.stream.instanceCount: 3");
        System.out.println("spring.cloud.stream.instanceIndex: 0\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Choose partition key carefully");
        System.out.println("- Ensure even distribution");
        System.out.println("- Match partition count to instances");
        System.out.println("- Handle rebalancing gracefully");
        System.out.println("- Monitor partition lag");
        System.out.println("- Use consistent partition count");
    }
}
