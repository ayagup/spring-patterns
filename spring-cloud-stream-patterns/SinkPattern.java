package com.example.cloudstream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.SubscribableChannel;

/**
 * Sink Pattern
 * ============
 * 
 * Demonstrates Sink interface for message consumers that only receive
 * messages without sending any.
 * 
 * Key Concepts:
 * ------------
 * 1. Sink - Consumer-only interface
 * 2. Input Channel - Inbound messages
 * 3. Message Consumption - Receive and process
 * 4. No Output - One-way communication
 * 5. Event Handling - React to events
 * 
 * Built-in Sink Interface:
 * -----------------------
 * public interface Sink {
 *   String INPUT = "input";
 * 
 *   @Input(INPUT)
 *   SubscribableChannel input();
 * }
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Order Event Consumer
 */
@Configuration
@EnableBinding(Sink.class)
class OrderEventConsumer {
    
    /**
     * Consume order events
     * 
     * @StreamListener(Sink.INPUT)
     * public void handleOrderEvent(Order order) {
     *   System.out.println("Received order: " + order.getId());
     *   orderService.process(order);
     * }
     */
    
    public void demonstrateOrderEventConsumer() {
        System.out.println("Order Event Consumer (Sink)");
        System.out.println("  Pattern: Consumer only");
        System.out.println("  Events: ORDER_CREATED, ORDER_UPDATED");
        System.out.println("  Source: order-events topic");
    }
}

/**
 * Example 2: Email Notification Sender
 */
@Configuration
@EnableBinding(Sink.class)
class EmailNotificationSender {
    
    /**
     * Send email notifications
     * 
     * @StreamListener(Sink.INPUT)
     * public void sendEmail(NotificationRequest request) {
     *   emailService.send(
     *     request.getTo(),
     *     request.getSubject(),
     *     request.getBody()
     *   );
     * }
     */
    
    public void demonstrateEmailNotification() {
        System.out.println("Email Notification Sender");
        System.out.println("  Input: Notification requests");
        System.out.println("  Action: Send emails");
        System.out.println("  Use case: Async email sending");
    }
}

/**
 * Example 3: Database Writer
 */
@Configuration
@EnableBinding(Sink.class)
class DatabaseWriter {
    
    /**
     * Write messages to database
     * 
     * @StreamListener(Sink.INPUT)
     * public void writeToDatabase(Order order) {
     *   orderRepository.save(order);
     *   System.out.println("Order saved: " + order.getId());
     * }
     */
    
    public void demonstrateDatabaseWriter() {
        System.out.println("Database Writer");
        System.out.println("  Input: Order messages");
        System.out.println("  Action: Save to database");
        System.out.println("  Use case: Event sourcing, audit log");
    }
}

/**
 * Example 4: File Writer
 */
@Configuration
@EnableBinding(Sink.class)
class FileWriter {
    
    /**
     * Write messages to file
     * 
     * @StreamListener(Sink.INPUT)
     * public void writeToFile(LogEntry entry) {
     *   fileWriter.append(entry.toString());
     * }
     */
    
    public void demonstrateFileWriter() {
        System.out.println("File Writer");
        System.out.println("  Input: Log entries");
        System.out.println("  Action: Append to file");
        System.out.println("  Use case: Log aggregation");
    }
}

/**
 * Example 5: Cache Updater
 */
@Configuration
@EnableBinding(Sink.class)
class CacheUpdater {
    
    /**
     * Update cache based on events
     * 
     * @StreamListener(Sink.INPUT)
     * public void updateCache(ProductUpdate update) {
     *   productCache.put(
     *     update.getProductId(),
     *     update.getProduct()
     *   );
     * }
     */
    
    public void demonstrateCacheUpdater() {
        System.out.println("Cache Updater");
        System.out.println("  Input: Product updates");
        System.out.println("  Action: Update Redis cache");
        System.out.println("  Use case: Cache synchronization");
    }
}

/**
 * Example 6: Metrics Collector
 */
@Configuration
@EnableBinding(Sink.class)
class MetricsCollector {
    
    /**
     * Collect and store metrics
     * 
     * @StreamListener(Sink.INPUT)
     * public void collectMetrics(Metric metric) {
     *   metricsRegistry.record(metric);
     *   if (metric.getValue() > threshold) {
     *     alertService.sendAlert(metric);
     *   }
     * }
     */
    
    public void demonstrateMetricsCollector() {
        System.out.println("Metrics Collector");
        System.out.println("  Input: Application metrics");
        System.out.println("  Action: Record and alert");
        System.out.println("  Use case: Monitoring");
    }
}

/**
 * Example 7: Search Index Updater
 */
@Configuration
@EnableBinding(Sink.class)
class SearchIndexUpdater {
    
    /**
     * Update Elasticsearch index
     * 
     * @StreamListener(Sink.INPUT)
     * public void updateIndex(Product product) {
     *   elasticsearchTemplate.save(product);
     * }
     */
    
    public void demonstrateSearchIndexUpdater() {
        System.out.println("Search Index Updater");
        System.out.println("  Input: Product changes");
        System.out.println("  Action: Update Elasticsearch");
        System.out.println("  Use case: Search index sync");
    }
}

/**
 * Example 8: Analytics Processor
 */
@Configuration
@EnableBinding(Sink.class)
class AnalyticsProcessor {
    
    /**
     * Process analytics events
     * 
     * @StreamListener(Sink.INPUT)
     * public void processAnalytics(ClickEvent event) {
     *   analyticsService.record(event);
     *   realtimeDashboard.update(event);
     * }
     */
    
    public void demonstrateAnalyticsProcessor() {
        System.out.println("Analytics Processor");
        System.out.println("  Input: User click events");
        System.out.println("  Action: Record and visualize");
        System.out.println("  Use case: Real-time analytics");
    }
}

/**
 * Example 9: Webhook Caller
 */
@Configuration
@EnableBinding(Sink.class)
class WebhookCaller {
    
    /**
     * Call external webhooks
     * 
     * @StreamListener(Sink.INPUT)
     * public void callWebhook(WebhookEvent event) {
     *   restTemplate.postForEntity(
     *     event.getUrl(),
     *     event.getPayload(),
     *     String.class
     *   );
     * }
     */
    
    public void demonstrateWebhookCaller() {
        System.out.println("Webhook Caller");
        System.out.println("  Input: Webhook events");
        System.out.println("  Action: HTTP POST to external URL");
        System.out.println("  Use case: Third-party integration");
    }
}

/**
 * Example 10: Audit Logger
 */
@Configuration
@EnableBinding(Sink.class)
class AuditLogger {
    
    /**
     * Log audit events
     * 
     * @StreamListener(Sink.INPUT)
     * public void logAudit(AuditEvent event) {
     *   auditLog.info(
     *     "User {} performed {} on {}",
     *     event.getUserId(),
     *     event.getAction(),
     *     event.getResource()
     *   );
     *   auditRepository.save(event);
     * }
     */
    
    public void demonstrateAuditLogger() {
        System.out.println("Audit Logger");
        System.out.println("  Input: Audit events");
        System.out.println("  Action: Log and persist");
        System.out.println("  Use case: Compliance, security");
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class SinkPattern {
    
    /**
     * Core Sink concepts
     */
    public void demonstrateSinkPattern() {
        System.out.println("\n=== Sink Pattern ===");
        System.out.println("Consumer-only message pattern");
        System.out.println("\nCharacteristics:");
        System.out.println("  - Input channel only");
        System.out.println("  - No output channel");
        System.out.println("  - One-way communication");
        System.out.println("\nCommon Use Cases:");
        System.out.println("  - Event handling");
        System.out.println("  - Notification sending");
        System.out.println("  - Database writing");
        System.out.println("  - File writing");
        System.out.println("  - Cache updates");
        System.out.println("  - Index synchronization");
    }
}

/**
 * Usage Examples and Best Practices
 */
class SinkPatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Sink Pattern");
        System.out.println("============\n");
        
        System.out.println("Purpose:");
        System.out.println("- Consume messages only");
        System.out.println("- No message production");
        System.out.println("- Event handling\n");
        
        System.out.println("Configuration:");
        System.out.println("spring:");
        System.out.println("  cloud:");
        System.out.println("    stream:");
        System.out.println("      bindings:");
        System.out.println("        input:");
        System.out.println("          destination: events");
        System.out.println("          group: consumer-group\n");
        
        System.out.println("Usage:");
        System.out.println("@StreamListener(Sink.INPUT)");
        System.out.println("public void handle(Order order) {");
        System.out.println("  // Process order");
        System.out.println("  orderService.process(order);");
        System.out.println("}\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Always use consumer groups");
        System.out.println("- Handle errors gracefully");
        System.out.println("- Use idempotent processing");
        System.out.println("- Configure DLQ for failures");
        System.out.println("- Monitor processing metrics");
        System.out.println("- Log message reception");
        System.out.println("- Use transactions when needed");
    }
}
