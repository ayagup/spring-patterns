package com.example.cloudstream;

import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Binder Pattern
 * ==============
 * 
 * Demonstrates the Binder abstraction in Spring Cloud Stream that provides
 * connectivity to external messaging systems (Kafka, RabbitMQ, etc.).
 * Binders abstract away the messaging middleware implementation details.
 * 
 * Key Concepts:
 * ------------
 * 1. Binder Abstraction - Middleware-agnostic messaging interface
 * 2. Multiple Binder Support - Use different message brokers simultaneously
 * 3. Binder Configuration - Configure binder-specific properties
 * 4. Producer/Consumer Properties - Control message production/consumption
 * 5. Binder Factory - Create and manage binder instances
 * 
 * How It Works:
 * ------------
 * - Binder provides connectivity to messaging middleware
 * - Abstracts Kafka, RabbitMQ, AWS Kinesis, etc.
 * - Handles connection management, serialization, partitioning
 * - Supports multiple binders in single application
 * - Configurable via application properties
 * 
 * Available Binders:
 * -----------------
 * - spring-cloud-stream-binder-kafka (Apache Kafka)
 * - spring-cloud-stream-binder-rabbit (RabbitMQ)
 * - spring-cloud-stream-binder-kinesis (AWS Kinesis)
 * - spring-cloud-stream-binder-google-pubsub (Google Pub/Sub)
 * - spring-cloud-stream-binder-solace (Solace PubSub+)
 * - spring-cloud-stream-binder-azure-eventhubs (Azure Event Hubs)
 * 
 * Configuration:
 * -------------
 * spring:
 *   cloud:
 *     stream:
 *       binders:
 *         kafka1:
 *           type: kafka
 *           environment:
 *             spring.cloud.stream.kafka.binder.brokers: localhost:9092
 *         rabbit1:
 *           type: rabbit
 *           environment:
 *             spring.rabbitmq.host: localhost
 *             spring.rabbitmq.port: 5672
 * 
 * Common Use Cases:
 * ----------------
 * - Multi-broker applications (Kafka + RabbitMQ)
 * - Migration from one broker to another
 * - Different brokers for different channels
 * - Testing with different middleware
 * - Cloud-agnostic messaging
 * - Polyglot messaging systems
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Kafka Binder Configuration
 */
@Configuration
class KafkaBinderConfiguration {
    
    /**
     * Configure Kafka binder properties
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       kafka:
     *         binder:
     *           brokers: localhost:9092
     *           defaultBrokerPort: 9092
     *           requiredAcks: 1
     *           autoCreateTopics: true
     *           autoAddPartitions: true
     *           minPartitionCount: 1
     */
    @Bean
    public String kafkaBinderConfig() {
        System.out.println("Kafka Binder Configuration");
        System.out.println("  Type: kafka");
        System.out.println("  Brokers: localhost:9092");
        System.out.println("  Auto-create topics: true");
        System.out.println("  Required acks: 1");
        return "Kafka Binder Config";
    }
}

/**
 * Example 2: RabbitMQ Binder Configuration
 */
@Configuration
class RabbitBinderConfiguration {
    
    /**
     * Configure RabbitMQ binder properties
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       rabbit:
     *         binder:
     *           adminAddresses: http://localhost:15672
     *           nodes: rabbit@localhost
     *           compressionLevel: 1
     *           connectionNamePrefix: cloud-stream-
     */
    @Bean
    public String rabbitBinderConfig() {
        System.out.println("RabbitMQ Binder Configuration");
        System.out.println("  Type: rabbit");
        System.out.println("  Host: localhost");
        System.out.println("  Port: 5672");
        System.out.println("  Admin port: 15672");
        return "RabbitMQ Binder Config";
    }
}

/**
 * Example 3: Multiple Binders Configuration
 */
@Configuration
class MultipleBindersConfiguration {
    
    /**
     * Configure multiple binders in same application
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       binders:
     *         kafka1:
     *           type: kafka
     *           environment:
     *             spring.cloud.stream.kafka.binder.brokers: localhost:9092
     *         rabbit1:
     *           type: rabbit
     *           environment:
     *             spring.rabbitmq.host: localhost
     *       bindings:
     *         orders-in-0:
     *           destination: orders
     *           binder: kafka1
     *         notifications-out-0:
     *           destination: notifications
     *           binder: rabbit1
     */
    @Bean
    public String multipleBindersConfig() {
        System.out.println("Multiple Binders Configuration");
        System.out.println("  Kafka binder: kafka1 (orders)");
        System.out.println("  RabbitMQ binder: rabbit1 (notifications)");
        System.out.println("  Use different brokers for different channels");
        return "Multiple Binders Config";
    }
}

/**
 * Example 4: Binder-Specific Producer Properties
 */
@Configuration
class ProducerPropertiesConfiguration {
    
    /**
     * Configure producer properties per binder
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         output-out-0:
     *           destination: my-topic
     *           producer:
     *             partitionCount: 3
     *             partitionKeyExpression: headers['partitionKey']
     *       kafka:
     *         bindings:
     *           output-out-0:
     *             producer:
     *               configuration:
     *                 compression.type: gzip
     *                 acks: all
     *                 batch.size: 16384
     */
    @Bean
    public String producerPropertiesConfig() {
        System.out.println("Producer Properties Configuration");
        System.out.println("  Partition count: 3");
        System.out.println("  Compression: gzip");
        System.out.println("  Acks: all");
        System.out.println("  Batch size: 16384");
        return "Producer Properties Config";
    }
}

/**
 * Example 5: Binder-Specific Consumer Properties
 */
@Configuration
class ConsumerPropertiesConfiguration {
    
    /**
     * Configure consumer properties per binder
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         input-in-0:
     *           destination: my-topic
     *           group: my-consumer-group
     *           consumer:
     *             maxAttempts: 3
     *             backOffInitialInterval: 1000
     *       kafka:
     *         bindings:
     *           input-in-0:
     *             consumer:
     *               configuration:
     *                 max.poll.records: 500
     *                 enable.auto.commit: false
     *                 auto.offset.reset: earliest
     */
    @Bean
    public String consumerPropertiesConfig() {
        System.out.println("Consumer Properties Configuration");
        System.out.println("  Consumer group: my-consumer-group");
        System.out.println("  Max attempts: 3");
        System.out.println("  Max poll records: 500");
        System.out.println("  Auto commit: false");
        return "Consumer Properties Config";
    }
}

/**
 * Example 6: Kafka Binder Health Indicator
 */
@Configuration
class BinderHealthConfiguration {
    
    /**
     * Monitor binder health
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       kafka:
     *         binder:
     *           health-timeout: 10
     *       rabbit:
     *         binder:
     *           health-check-enabled: true
     */
    @Bean
    public String binderHealthConfig() {
        System.out.println("Binder Health Configuration");
        System.out.println("  Health check enabled: true");
        System.out.println("  Health timeout: 10 seconds");
        System.out.println("  Actuator endpoint: /actuator/health/binders");
        return "Binder Health Config";
    }
}

/**
 * Example 7: Custom Binder Implementation
 */
class CustomBinder {
    
    /**
     * Create custom binder for proprietary messaging system
     * 
     * Implement:
     * - Binder interface
     * - MessageProducer for sending
     * - MessageConsumer for receiving
     * - ProvisioningProvider for topic/queue creation
     */
    public void createCustomBinder() {
        System.out.println("Custom Binder Implementation");
        System.out.println("  Implement Binder interface");
        System.out.println("  Create MessageProducer/Consumer");
        System.out.println("  Implement ProvisioningProvider");
        System.out.println("  Register via spring.factories");
    }
}

/**
 * Example 8: Binder Error Handling
 */
@Configuration
class BinderErrorHandlingConfiguration {
    
    /**
     * Configure error handling per binder
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         input-in-0:
     *           consumer:
     *             maxAttempts: 3
     *             backOffInitialInterval: 1000
     *             backOffMaxInterval: 10000
     *             backOffMultiplier: 2.0
     *       kafka:
     *         bindings:
     *           input-in-0:
     *             consumer:
     *               enableDlq: true
     *               dlqName: errors.my-topic
     *               autoCommitOnError: false
     */
    @Bean
    public String binderErrorHandlingConfig() {
        System.out.println("Binder Error Handling Configuration");
        System.out.println("  Max retry attempts: 3");
        System.out.println("  Exponential backoff: 1s -> 10s");
        System.out.println("  Dead letter queue: enabled");
        System.out.println("  DLQ topic: errors.my-topic");
        return "Binder Error Handling Config";
    }
}

/**
 * Example 9: Binder Partitioning Strategy
 */
@Configuration
class BinderPartitioningConfiguration {
    
    /**
     * Configure partitioning strategy
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         output-out-0:
     *           destination: partitioned-topic
     *           producer:
     *             partitionCount: 3
     *             partitionKeyExpression: payload.userId
     *         input-in-0:
     *           destination: partitioned-topic
     *           group: consumer-group
     *           consumer:
     *             partitioned: true
     *             instanceIndex: 0
     *             instanceCount: 3
     */
    @Bean
    public String binderPartitioningConfig() {
        System.out.println("Binder Partitioning Configuration");
        System.out.println("  Partition count: 3");
        System.out.println("  Partition key: payload.userId");
        System.out.println("  Consumer instance: 0 of 3");
        System.out.println("  Ensures ordering per partition");
        return "Binder Partitioning Config";
    }
}

/**
 * Example 10: Binder Content Type Negotiation
 */
@Configuration
class BinderContentTypeConfiguration {
    
    /**
     * Configure content type handling
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         output-out-0:
     *           destination: my-topic
     *           contentType: application/json
     *         input-in-0:
     *           destination: my-topic
     *           contentType: application/json
     *       kafka:
     *         binder:
     *           serdeResolver: default
     */
    @Bean
    public String binderContentTypeConfig() {
        System.out.println("Binder Content Type Configuration");
        System.out.println("  Content type: application/json");
        System.out.println("  Automatic serialization/deserialization");
        System.out.println("  Supports: JSON, Avro, Protobuf, Text");
        return "Binder Content Type Config";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class BinderPattern {
    
    /**
     * Example: Binder abstraction overview
     */
    @Bean
    public String binderInfo() {
        System.out.println("Spring Cloud Stream Binder Pattern");
        System.out.println("==================================");
        System.out.println("  Purpose: Abstract messaging middleware");
        System.out.println("  Supports: Kafka, RabbitMQ, Kinesis, Pub/Sub");
        System.out.println("  Benefits: Vendor-neutral, pluggable, testable");
        return "Binder Info";
    }
}

/**
 * Usage Examples and Best Practices
 */
class BinderUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Binder Pattern - Spring Cloud Stream");
        System.out.println("====================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Abstract messaging middleware connectivity");
        System.out.println("- Support multiple message brokers");
        System.out.println("- Vendor-neutral messaging interface\n");
        
        System.out.println("Available Binders:");
        System.out.println("1. Kafka - spring-cloud-stream-binder-kafka");
        System.out.println("2. RabbitMQ - spring-cloud-stream-binder-rabbit");
        System.out.println("3. AWS Kinesis - spring-cloud-stream-binder-kinesis");
        System.out.println("4. Google Pub/Sub - spring-cloud-stream-binder-google-pubsub");
        System.out.println("5. Solace - spring-cloud-stream-binder-solace");
        System.out.println("6. Azure Event Hubs - spring-cloud-stream-binder-azure-eventhubs\n");
        
        System.out.println("Configuration Properties:");
        System.out.println("- spring.cloud.stream.binders.<name>.type - Binder type");
        System.out.println("- spring.cloud.stream.binders.<name>.environment - Binder config");
        System.out.println("- spring.cloud.stream.bindings.<name>.binder - Assign binder");
        System.out.println("- spring.cloud.stream.default-binder - Default binder\n");
        
        System.out.println("Key Features:");
        System.out.println("- Multiple binders in one application");
        System.out.println("- Binder-specific configuration");
        System.out.println("- Health indicators");
        System.out.println("- Error handling (DLQ, retry)");
        System.out.println("- Partitioning support");
        System.out.println("- Content type negotiation\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use binder abstraction for portability");
        System.out.println("- Configure health checks");
        System.out.println("- Implement error handling (retry + DLQ)");
        System.out.println("- Use partitioning for ordering");
        System.out.println("- Set appropriate consumer groups");
        System.out.println("- Monitor binder metrics");
        System.out.println("- Test with TestBinder");
        System.out.println("- Document binder requirements\n");
        
        System.out.println("Example Configuration:");
        System.out.println("spring:");
        System.out.println("  cloud:");
        System.out.println("    stream:");
        System.out.println("      binders:");
        System.out.println("        kafka1:");
        System.out.println("          type: kafka");
        System.out.println("          environment:");
        System.out.println("            spring.cloud.stream.kafka.binder:");
        System.out.println("              brokers: localhost:9092");
        System.out.println("      bindings:");
        System.out.println("        input-in-0:");
        System.out.println("          destination: my-topic");
        System.out.println("          binder: kafka1");
        System.out.println("          group: my-group");
    }
}
