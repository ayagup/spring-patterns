package com.example.cloudstream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Configuration;

/**
 * Consumer Group Pattern
 * ======================
 * 
 * Demonstrates consumer groups for load balancing and fault tolerance
 * in Spring Cloud Stream applications.
 * 
 * Key Concepts:
 * ------------
 * 1. Consumer Group - Logical group of consumers
 * 2. Load Balancing - Distribute messages across instances
 * 3. Competing Consumers - Multiple instances, one receives
 * 4. Durable Subscription - Persistent queue/offset
 * 5. Exactly-Once Processing - Each message to one consumer
 * 
 * How It Works:
 * ------------
 * - Group name identifies consumer group
 * - Multiple instances share same group
 * - Each message delivered to ONE instance
 * - Kafka: Consumer group, partitions assigned
 * - RabbitMQ: Durable queue, competing consumers
 * 
 * Configuration:
 * -------------
 * spring.cloud.stream.bindings.input:
 *   destination: orders
 *   group: order-service
 * 
 * Without Group:
 * - Each instance gets ALL messages (broadcast)
 * - Temporary subscription (Kafka)
 * - Non-durable queue (RabbitMQ)
 * 
 * With Group:
 * - Messages load-balanced across instances
 * - Persistent subscription (Kafka)
 * - Durable queue (RabbitMQ)
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Consumer Group
 */
@Configuration
@EnableBinding(Sink.class)
class BasicConsumerGroupExample {
    
    /**
     * Basic consumer group configuration
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   destination: orders
     *   group: order-processors
     * 
     * Result:
     * - 3 instances of this app
     * - Share consumer group "order-processors"
     * - Each message to ONE instance
     */
    
    @StreamListener(Sink.INPUT)
    public void handleOrder(Object order) {
        System.out.println("Basic Consumer Group");
        System.out.println("  Group: order-processors");
        System.out.println("  Instances: 3");
        System.out.println("  Message delivery: One instance only");
    }
}

/**
 * Example 2: Multiple Consumer Groups
 */
@Configuration
@EnableBinding(Sink.class)
class MultipleConsumerGroupsExample {
    
    /**
     * Multiple groups consume same topic
     * 
     * Group 1 (order-service):
     * spring.cloud.stream.bindings.input:
     *   destination: orders
     *   group: order-service
     * 
     * Group 2 (notification-service):
     * spring.cloud.stream.bindings.input:
     *   destination: orders
     *   group: notification-service
     * 
     * Result:
     * - Both groups receive ALL messages
     * - Within each group, load-balanced
     * - Independent consumption
     */
    
    public void demonstrateMultipleGroups() {
        System.out.println("Multiple Consumer Groups");
        System.out.println("  Topic: orders");
        System.out.println("  Group 1: order-service (3 instances)");
        System.out.println("  Group 2: notification-service (2 instances)");
        System.out.println("  Each group: Gets all messages");
    }
}

/**
 * Example 3: No Consumer Group (Broadcast)
 */
@Configuration
@EnableBinding(Sink.class)
class NoConsumerGroupExample {
    
    /**
     * Without consumer group = broadcast
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   destination: announcements
     *   # NO group specified
     * 
     * Result:
     * - Each instance gets ALL messages
     * - Temporary subscription
     * - Lost on restart (Kafka)
     * - Auto-delete queue (RabbitMQ)
     */
    
    public void demonstrateNoGroup() {
        System.out.println("No Consumer Group (Broadcast)");
        System.out.println("  Behavior: Each instance gets all messages");
        System.out.println("  Subscription: Temporary");
        System.out.println("  Use case: Announcements, cache invalidation");
    }
}

/**
 * Example 4: Consumer Group with Kafka
 */
@Configuration
@EnableBinding(Sink.class)
class KafkaConsumerGroupExample {
    
    /**
     * Kafka-specific consumer group behavior
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   destination: orders
     *   group: order-service
     * spring.cloud.stream.kafka.bindings.input.consumer:
     *   startOffset: earliest
     *   resetOffsets: true
     * 
     * Kafka behavior:
     * - Consumer group coordination
     * - Partition assignment
     * - Offset management
     * - Rebalancing
     */
    
    public void demonstrateKafkaConsumerGroup() {
        System.out.println("Kafka Consumer Group");
        System.out.println("  Group: order-service");
        System.out.println("  Offset: earliest");
        System.out.println("  Coordination: Kafka group coordinator");
        System.out.println("  Partitions: Auto-assigned");
    }
}

/**
 * Example 5: Consumer Group with RabbitMQ
 */
@Configuration
@EnableBinding(Sink.class)
class RabbitMQConsumerGroupExample {
    
    /**
     * RabbitMQ-specific consumer group behavior
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   destination: orders
     *   group: order-service
     * spring.cloud.stream.rabbit.bindings.input.consumer:
     *   durableSubscription: true
     *   queueNameGroupOnly: true
     * 
     * RabbitMQ behavior:
     * - Durable queue created: orders.order-service
     * - Competing consumers pattern
     * - Round-robin distribution
     */
    
    public void demonstrateRabbitMQConsumerGroup() {
        System.out.println("RabbitMQ Consumer Group");
        System.out.println("  Group: order-service");
        System.out.println("  Queue: orders.order-service");
        System.out.println("  Durable: true");
        System.out.println("  Distribution: Round-robin");
    }
}

/**
 * Example 6: Consumer Group Scaling
 */
@Configuration
@EnableBinding(Sink.class)
class ConsumerGroupScalingExample {
    
    /**
     * Scale consumer instances
     * 
     * Scenario:
     * - Start with 2 instances
     * - Scale to 5 instances
     * - All in same consumer group
     * 
     * Result:
     * - Automatic load redistribution
     * - Kafka: Partition rebalancing
     * - RabbitMQ: More competing consumers
     * - Higher throughput
     */
    
    public void demonstrateScaling() {
        System.out.println("Consumer Group Scaling");
        System.out.println("  Initial instances: 2");
        System.out.println("  Scaled instances: 5");
        System.out.println("  Effect: Load redistributed");
        System.out.println("  Throughput: Increased");
    }
}

/**
 * Example 7: Consumer Group with Partitioning
 */
@Configuration
@EnableBinding(Sink.class)
class PartitionedConsumerGroupExample {
    
    /**
     * Combine consumer group with partitioning
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   destination: orders
     *   group: order-service
     *   consumer:
     *     partitioned: true
     * spring.cloud.stream.instanceCount: 3
     * spring.cloud.stream.instanceIndex: ${INSTANCE_INDEX}
     * 
     * Result:
     * - Instance 0: Partitions 0
     * - Instance 1: Partitions 1
     * - Instance 2: Partitions 2
     */
    
    public void demonstratePartitionedGroup() {
        System.out.println("Partitioned Consumer Group");
        System.out.println("  Group: order-service");
        System.out.println("  Partitions: 3");
        System.out.println("  Instance 0: Partition 0");
        System.out.println("  Instance 1: Partition 1");
        System.out.println("  Instance 2: Partition 2");
    }
}

/**
 * Example 8: Consumer Group Offset Management
 */
@Configuration
@EnableBinding(Sink.class)
class OffsetManagementExample {
    
    /**
     * Manage consumer group offsets
     * 
     * Configuration (Kafka):
     * spring.cloud.stream.kafka.bindings.input.consumer:
     *   startOffset: earliest  # or latest
     *   resetOffsets: false
     *   autoCommitOffset: true
     *   autoCommitOnError: false
     * 
     * Behavior:
     * - Start from earliest/latest on first run
     * - Resume from committed offset
     * - Auto-commit on success
     * - Don't commit on error
     */
    
    public void demonstrateOffsetManagement() {
        System.out.println("Consumer Group Offset Management");
        System.out.println("  Start offset: earliest");
        System.out.println("  Auto-commit: true");
        System.out.println("  Commit on error: false");
        System.out.println("  Resume: From last committed");
    }
}

/**
 * Example 9: Consumer Group Error Handling
 */
@Configuration
@EnableBinding(Sink.class)
class ConsumerGroupErrorHandlingExample {
    
    /**
     * Error handling in consumer group
     * 
     * Configuration:
     * spring.cloud.stream.bindings.input:
     *   group: order-service
     *   consumer:
     *     maxAttempts: 3
     *     backOffInitialInterval: 1000
     * spring.cloud.stream.kafka.bindings.input.consumer:
     *   enableDlq: true
     *   dlqName: error.orders.order-service
     * 
     * Behavior:
     * - Retry 3 times
     * - Exponential backoff
     * - Send to DLQ after max attempts
     * - Group continues processing
     */
    
    public void demonstrateErrorHandling() {
        System.out.println("Consumer Group Error Handling");
        System.out.println("  Max attempts: 3");
        System.out.println("  Backoff: Exponential");
        System.out.println("  DLQ: error.orders.order-service");
        System.out.println("  Effect: Failed messages isolated");
    }
}

/**
 * Example 10: Consumer Group Monitoring
 */
@Configuration
@EnableBinding(Sink.class)
class ConsumerGroupMonitoringExample {
    
    /**
     * Monitor consumer group metrics
     * 
     * Kafka Metrics:
     * - Consumer lag per partition
     * - Offset committed/current
     * - Rebalance rate
     * - Consumption rate
     * 
     * RabbitMQ Metrics:
     * - Queue depth
     * - Consumer count
     * - Message rate
     * - Acknowledgment rate
     * 
     * Actuator endpoint:
     * /actuator/health/binders
     */
    
    public void demonstrateMonitoring() {
        System.out.println("Consumer Group Monitoring");
        System.out.println("  Metrics: Lag, rate, rebalances");
        System.out.println("  Endpoint: /actuator/health/binders");
        System.out.println("  Kafka: Consumer lag per partition");
        System.out.println("  RabbitMQ: Queue depth, consumers");
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConsumerGroupPattern {
    
    /**
     * Core Consumer Group concepts
     */
    public void demonstrateConsumerGroupPattern() {
        System.out.println("\n=== Consumer Group Pattern ===");
        System.out.println("Load balancing and fault tolerance");
        System.out.println("\nKey Benefits:");
        System.out.println("  - Load balancing across instances");
        System.out.println("  - Fault tolerance");
        System.out.println("  - Durable subscriptions");
        System.out.println("  - Exactly-once semantics per group");
        System.out.println("\nWith vs Without Group:");
        System.out.println("  With: One message -> one instance");
        System.out.println("  Without: One message -> all instances");
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConsumerGroupPatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Consumer Group Pattern");
        System.out.println("======================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Load balance messages");
        System.out.println("- Enable horizontal scaling");
        System.out.println("- Provide fault tolerance\n");
        
        System.out.println("Configuration:");
        System.out.println("spring.cloud.stream.bindings.input:");
        System.out.println("  destination: orders");
        System.out.println("  group: order-service\n");
        
        System.out.println("Behavior:");
        System.out.println("- Messages distributed across instances");
        System.out.println("- Each message to ONE instance in group");
        System.out.println("- Durable subscription");
        System.out.println("- Offset/position tracked\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Always use groups in production");
        System.out.println("- Choose meaningful group names");
        System.out.println("- Monitor consumer lag");
        System.out.println("- Configure error handling");
        System.out.println("- Use DLQ for failed messages");
        System.out.println("- Scale instances based on lag");
        System.out.println("- Test rebalancing behavior");
        System.out.println("- Document group ownership");
    }
}
