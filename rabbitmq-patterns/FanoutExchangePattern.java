package com.example.rabbitmq.patterns;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Fanout Exchange Pattern
 * 
 * Demonstrates the Fanout Exchange broadcasting mechanism where messages
 * are routed to all bound queues regardless of routing key. This implements
 * a publish-subscribe pattern.
 * 
 * Key Features:
 * - Broadcasts to all bound queues
 * - Ignores routing keys
 * - Publish-subscribe pattern
 * - Multiple consumer support
 * - Event notification system
 */
@SpringBootApplication
public class FanoutExchangePattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String FANOUT_EXCHANGE = "fanout-exchange-demo";

    public static void main(String[] args) {
        SpringApplication.run(FanoutExchangePattern.class, args);
    }

    /**
     * Fanout exchange configuration
     */
    @Bean
    public FanoutExchange notificationExchange() {
        return ExchangeBuilder
                .fanoutExchange(FANOUT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Multiple queues for different notification channels
     */
    @Bean
    public Queue emailNotificationQueue() {
        return new Queue("fanout-email-notifications", true);
    }

    @Bean
    public Queue smsNotificationQueue() {
        return new Queue("fanout-sms-notifications", true);
    }

    @Bean
    public Queue pushNotificationQueue() {
        return new Queue("fanout-push-notifications", true);
    }

    @Bean
    public Queue auditLogQueue() {
        return new Queue("fanout-audit-log", true);
    }

    @Bean
    public Queue analyticsQueue() {
        return new Queue("fanout-analytics", true);
    }

    /**
     * Fanout bindings - no routing key needed
     */
    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailNotificationQueue())
                .to(notificationExchange());
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder
                .bind(smsNotificationQueue())
                .to(notificationExchange());
    }

    @Bean
    public Binding pushBinding() {
        return BindingBuilder
                .bind(pushNotificationQueue())
                .to(notificationExchange());
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder
                .bind(auditLogQueue())
                .to(notificationExchange());
    }

    @Bean
    public Binding analyticsBinding() {
        return BindingBuilder
                .bind(analyticsQueue())
                .to(notificationExchange());
    }

    @Override
    public void run(String... args) throws InterruptedException {
        demonstrateFanoutExchange();
        Thread.sleep(2000);
    }

    private void demonstrateFanoutExchange() {
        System.out.println("=== Fanout Exchange Pattern ===\n");

        System.out.println("Bound Queues:");
        System.out.println("- fanout-email-notifications");
        System.out.println("- fanout-sms-notifications");
        System.out.println("- fanout-push-notifications");
        System.out.println("- fanout-audit-log");
        System.out.println("- fanout-analytics\n");

        // Test 1: Order placed event
        System.out.println("1. Broadcasting: Order Placed Event");
        NotificationEvent orderEvent = new NotificationEvent(
            "ORDER_PLACED",
            "New order #12345 has been placed",
            "user@example.com",
            "high"
        );
        rabbitTemplate.convertAndSend(FANOUT_EXCHANGE, "", orderEvent);
        System.out.println("   Broadcast to ALL 5 queues simultaneously\n");

        // Test 2: Payment received event
        System.out.println("2. Broadcasting: Payment Received Event");
        NotificationEvent paymentEvent = new NotificationEvent(
            "PAYMENT_RECEIVED",
            "Payment of $299.99 received for order #12345",
            "user@example.com",
            "medium"
        );
        rabbitTemplate.convertAndSend(FANOUT_EXCHANGE, "", paymentEvent);
        System.out.println("   Broadcast to ALL 5 queues simultaneously\n");

        // Test 3: Shipping update
        System.out.println("3. Broadcasting: Shipping Update");
        NotificationEvent shippingEvent = new NotificationEvent(
            "SHIPPING_UPDATE",
            "Your order #12345 has been shipped",
            "user@example.com",
            "low"
        );
        // Routing key is ignored in fanout exchange
        rabbitTemplate.convertAndSend(FANOUT_EXCHANGE, "ignored.routing.key", shippingEvent);
        System.out.println("   Broadcast to ALL 5 queues (routing key ignored)\n");

        System.out.println("Fanout Exchange Characteristics:");
        System.out.println("- Broadcasts to all bound queues");
        System.out.println("- Routing key is ignored");
        System.out.println("- Implements pub-sub pattern");
        System.out.println("- All consumers receive all messages");
        System.out.println("- Ideal for event notifications");
    }

    /**
     * Notification listeners for different channels
     */
    @Component
    static class NotificationListeners {

        @RabbitListener(queues = "fanout-email-notifications")
        public void handleEmailNotification(NotificationEvent event) {
            System.out.println("  [Email Service] Sending email to " + event.getRecipient());
            System.out.println("    Subject: " + event.getEventType());
            System.out.println("    Body: " + event.getMessage());
        }

        @RabbitListener(queues = "fanout-sms-notifications")
        public void handleSmsNotification(NotificationEvent event) {
            System.out.println("  [SMS Service] Sending SMS to " + event.getRecipient());
            System.out.println("    Message: " + event.getMessage());
        }

        @RabbitListener(queues = "fanout-push-notifications")
        public void handlePushNotification(NotificationEvent event) {
            System.out.println("  [Push Service] Sending push notification");
            System.out.println("    Title: " + event.getEventType());
            System.out.println("    Message: " + event.getMessage());
        }

        @RabbitListener(queues = "fanout-audit-log")
        public void handleAuditLog(NotificationEvent event) {
            System.out.println("  [Audit Log] Recording event");
            System.out.println("    Type: " + event.getEventType());
            System.out.println("    Priority: " + event.getPriority());
            System.out.println("    Timestamp: " + System.currentTimeMillis());
        }

        @RabbitListener(queues = "fanout-analytics")
        public void handleAnalytics(NotificationEvent event) {
            System.out.println("  [Analytics] Processing event metrics");
            System.out.println("    Event: " + event.getEventType());
            System.out.println("    Priority: " + event.getPriority());
        }
    }

    /**
     * Notification event class
     */
    static class NotificationEvent implements java.io.Serializable {
        private String eventType;
        private String message;
        private String recipient;
        private String priority;

        public NotificationEvent() {}

        public NotificationEvent(String eventType, String message, String recipient, String priority) {
            this.eventType = eventType;
            this.message = message;
            this.recipient = recipient;
            this.priority = priority;
        }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
}
