package com.example.cloudstream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;

/**
 * Source Pattern
 * ==============
 * 
 * Demonstrates Source interface for message producers that only send
 * messages without receiving any.
 * 
 * Key Concepts:
 * ------------
 * 1. Source - Producer-only interface
 * 2. Output Channel - Outbound messages
 * 3. Message Production - Generate and send
 * 4. No Input - One-way communication
 * 5. Event Publishing - Domain events
 * 
 * Built-in Source Interface:
 * -------------------------
 * public interface Source {
 *   String OUTPUT = "output";
 * 
 *   @Output(OUTPUT)
 *   MessageChannel output();
 * }
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Order Event Publisher
 */
@Configuration
@EnableBinding(Source.class)
class OrderEventPublisher {
    
    /**
     * Publish order events
     * 
     * @Autowired
     * private Source source;
     * 
     * public void publishOrderCreated(Order order) {
     *   Message<Order> message = MessageBuilder
     *     .withPayload(order)
     *     .setHeader("eventType", "ORDER_CREATED")
     *     .build();
     *   source.output().send(message);
     * }
     */
    
    public void demonstrateOrderEventPublisher() {
        System.out.println("Order Event Publisher (Source)");
        System.out.println("  Pattern: Producer only");
        System.out.println("  Events: ORDER_CREATED, ORDER_UPDATED");
        System.out.println("  Destination: order-events");
    }
}

/**
 * Example 2: Scheduled Message Producer
 */
@Configuration
@EnableBinding(Source.class)
class ScheduledMessageProducer {
    
    /**
     * Send scheduled messages
     * 
     * @Scheduled(fixedRate = 5000)
     * public void sendHeartbeat() {
     *   source.output().send(MessageBuilder
     *     .withPayload("HEARTBEAT")
     *     .setHeader("timestamp", System.currentTimeMillis())
     *     .build());
     * }
     */
    
    public void demonstrateScheduledProducer() {
        System.out.println("Scheduled Message Producer");
        System.out.println("  Schedule: Every 5 seconds");
        System.out.println("  Message: HEARTBEAT");
        System.out.println("  Use case: Health monitoring");
    }
}

/**
 * Example 3: REST API to Stream Bridge
 */
@Configuration
@EnableBinding(Source.class)
class RestApiStreamBridge {
    
    /**
     * Publish messages from REST API
     * 
     * @PostMapping("/orders")
     * public ResponseEntity<Order> createOrder(@RequestBody Order order) {
     *   // Save order
     *   orderRepository.save(order);
     *   
     *   // Publish event
     *   source.output().send(MessageBuilder
     *     .withPayload(order)
     *     .build());
     *   
     *   return ResponseEntity.ok(order);
     * }
     */
    
    public void demonstrateRestToStream() {
        System.out.println("REST API to Stream Bridge");
        System.out.println("  REST: POST /orders");
        System.out.println("  Stream: Publish order-created event");
        System.out.println("  Pattern: Synchronous API + Async event");
    }
}

/**
 * Example 4: Database Change Publisher
 */
@Configuration
@EnableBinding(Source.class)
class DatabaseChangePublisher {
    
    /**
     * Publish database changes
     * 
     * @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
     * public void publishOrderChange(OrderChangedEvent event) {
     *   source.output().send(MessageBuilder
     *     .withPayload(event.getOrder())
     *     .setHeader("changeType", event.getType())
     *     .build());
     * }
     */
    
    public void demonstrateDatabaseChangePublisher() {
        System.out.println("Database Change Publisher");
        System.out.println("  Trigger: After commit");
        System.out.println("  Events: INSERT, UPDATE, DELETE");
        System.out.println("  Use case: CDC (Change Data Capture)");
    }
}

/**
 * Example 5: Batch Message Producer
 */
@Configuration
@EnableBinding(Source.class)
class BatchMessageProducer {
    
    /**
     * Publish batch of messages
     * 
     * public void publishOrders(List<Order> orders) {
     *   orders.forEach(order -> {
     *     source.output().send(MessageBuilder
     *       .withPayload(order)
     *       .build());
     *   });
     * }
     */
    
    public void demonstrateBatchProducer() {
        System.out.println("Batch Message Producer");
        System.out.println("  Input: List of orders");
        System.out.println("  Output: Individual messages");
        System.out.println("  Use case: Bulk data publishing");
    }
}

/**
 * Example 6: File Upload to Stream
 */
@Configuration
@EnableBinding(Source.class)
class FileUploadStreamPublisher {
    
    /**
     * Process uploaded file and publish
     * 
     * @PostMapping("/upload")
     * public void uploadFile(@RequestParam("file") MultipartFile file) {
     *   List<Order> orders = csvParser.parse(file);
     *   orders.forEach(order -> {
     *     source.output().send(MessageBuilder
     *       .withPayload(order)
     *       .build());
     *   });
     * }
     */
    
    public void demonstrateFileUploadStream() {
        System.out.println("File Upload to Stream");
        System.out.println("  Input: CSV file upload");
        System.out.println("  Process: Parse CSV");
        System.out.println("  Output: Stream of order messages");
    }
}

/**
 * Example 7: External API Integration
 */
@Configuration
@EnableBinding(Source.class)
class ExternalApiIntegration {
    
    /**
     * Fetch from external API and publish
     * 
     * @Scheduled(fixedRate = 60000)
     * public void fetchAndPublish() {
     *   List<Order> orders = externalApi.fetchOrders();
     *   orders.forEach(order -> {
     *     source.output().send(MessageBuilder
     *       .withPayload(order)
     *       .build());
     *   });
     * }
     */
    
    public void demonstrateExternalApiIntegration() {
        System.out.println("External API Integration");
        System.out.println("  Schedule: Every minute");
        System.out.println("  Fetch: External API");
        System.out.println("  Publish: To stream");
    }
}

/**
 * Example 8: Sensor Data Publisher
 */
@Configuration
@EnableBinding(Source.class)
class SensorDataPublisher {
    
    /**
     * Publish IoT sensor data
     * 
     * public void publishSensorReading(SensorReading reading) {
     *   source.output().send(MessageBuilder
     *     .withPayload(reading)
     *     .setHeader("sensorId", reading.getSensorId())
     *     .setHeader("timestamp", reading.getTimestamp())
     *     .build());
     * }
     */
    
    public void demonstrateSensorDataPublisher() {
        System.out.println("Sensor Data Publisher");
        System.out.println("  Source: IoT sensors");
        System.out.println("  Data: Temperature, humidity, pressure");
        System.out.println("  Use case: IoT telemetry");
    }
}

/**
 * Example 9: Notification Publisher
 */
@Configuration
@EnableBinding(Source.class)
class NotificationPublisher {
    
    /**
     * Publish notifications
     * 
     * public void sendNotification(Notification notification) {
     *   source.output().send(MessageBuilder
     *     .withPayload(notification)
     *     .setHeader("type", notification.getType())
     *     .setHeader("priority", notification.getPriority())
     *     .build());
     * }
     */
    
    public void demonstrateNotificationPublisher() {
        System.out.println("Notification Publisher");
        System.out.println("  Types: Email, SMS, Push");
        System.out.println("  Priority: High, Normal, Low");
        System.out.println("  Use case: User notifications");
    }
}

/**
 * Example 10: Metrics Publisher
 */
@Configuration
@EnableBinding(Source.class)
class MetricsPublisher {
    
    /**
     * Publish application metrics
     * 
     * @Scheduled(fixedRate = 10000)
     * public void publishMetrics() {
     *   Metrics metrics = metricsCollector.collect();
     *   source.output().send(MessageBuilder
     *     .withPayload(metrics)
     *     .setHeader("appName", "order-service")
     *     .build());
     * }
     */
    
    public void demonstrateMetricsPublisher() {
        System.out.println("Metrics Publisher");
        System.out.println("  Schedule: Every 10 seconds");
        System.out.println("  Metrics: CPU, memory, requests");
        System.out.println("  Use case: Application monitoring");
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class SourcePattern {
    
    /**
     * Core Source concepts
     */
    public void demonstrateSourcePattern() {
        System.out.println("\n=== Source Pattern ===");
        System.out.println("Producer-only message pattern");
        System.out.println("\nCharacteristics:");
        System.out.println("  - Output channel only");
        System.out.println("  - No input channel");
        System.out.println("  - One-way communication");
        System.out.println("\nCommon Use Cases:");
        System.out.println("  - Event publishing");
        System.out.println("  - Scheduled producers");
        System.out.println("  - REST to stream bridge");
        System.out.println("  - Database change events");
        System.out.println("  - IoT data ingestion");
    }
}

/**
 * Usage Examples and Best Practices
 */
class SourcePatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Source Pattern");
        System.out.println("==============\n");
        
        System.out.println("Purpose:");
        System.out.println("- Produce messages only");
        System.out.println("- No message consumption");
        System.out.println("- Event publishing\n");
        
        System.out.println("Configuration:");
        System.out.println("spring:");
        System.out.println("  cloud:");
        System.out.println("    stream:");
        System.out.println("      bindings:");
        System.out.println("        output:");
        System.out.println("          destination: events\n");
        
        System.out.println("Usage:");
        System.out.println("@Autowired");
        System.out.println("private Source source;");
        System.out.println("");
        System.out.println("source.output().send(");
        System.out.println("  MessageBuilder.withPayload(data).build()");
        System.out.println(");\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Add meaningful headers");
        System.out.println("- Use consistent event types");
        System.out.println("- Handle send failures");
        System.out.println("- Consider partitioning");
        System.out.println("- Monitor publish rates");
        System.out.println("- Use transactions when needed");
    }
}
