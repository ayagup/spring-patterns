package com.example.amqp.patterns;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Message Properties Pattern
 * 
 * Demonstrates the use of AMQP message properties to control message behavior
 * and routing. Message properties provide metadata about the message including
 * headers, content type, encoding, priority, expiration, and more.
 * 
 * Key Features:
 * - Standard AMQP properties (content type, encoding, priority, etc.)
 * - Custom headers
 * - Message persistence
 * - Message expiration (TTL)
 * - Correlation ID for request-reply
 * - Reply-to queue
 * - Message ID and timestamp
 */
@SpringBootApplication
public class MessagePropertiesPattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String QUEUE_NAME = "message-properties-queue";

    public static void main(String[] args) {
        SpringApplication.run(MessagePropertiesPattern.class, args);
    }

    @Bean
    public Queue propertiesQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Override
    public void run(String... args) {
        demonstrateBasicProperties();
        demonstrateHeaders();
        demonstratePriority();
        demonstrateExpiration();
        demonstrateRequestReplyProperties();
        demonstratePersistence();
    }

    /**
     * Basic message properties
     */
    private void demonstrateBasicProperties() {
        System.out.println("=== Message Properties Pattern ===\n");
        System.out.println("1. Basic Message Properties:");

        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setContentEncoding("UTF-8");
        properties.setMessageId("MSG-" + System.currentTimeMillis());
        properties.setTimestamp(new Date());
        properties.setAppId("message-properties-app");
        properties.setUserId("guest");

        Message message = new Message("{\"text\":\"Hello AMQP\"}".getBytes(), properties);
        rabbitTemplate.send(QUEUE_NAME, message);

        System.out.println("   Message sent with properties:");
        System.out.println("   - Content Type: " + properties.getContentType());
        System.out.println("   - Content Encoding: " + properties.getContentEncoding());
        System.out.println("   - Message ID: " + properties.getMessageId());
        System.out.println("   - Timestamp: " + properties.getTimestamp());
        System.out.println("   - App ID: " + properties.getAppId());
        System.out.println();
    }

    /**
     * Custom headers
     */
    private void demonstrateHeaders() {
        System.out.println("2. Custom Headers:");

        Map<String, Object> headers = new HashMap<>();
        headers.put("request-id", "REQ-12345");
        headers.put("client-version", "1.0.0");
        headers.put("retry-count", 0);
        headers.put("priority-level", "HIGH");
        headers.put("source-system", "order-service");

        MessageProperties properties = MessagePropertiesBuilder.newInstance()
                .setContentType("application/json")
                .copyHeaders(headers)
                .build();

        Message message = new Message("{\"order\":\"12345\"}".getBytes(), properties);
        rabbitTemplate.send(QUEUE_NAME, message);

        System.out.println("   Message sent with custom headers:");
        headers.forEach((key, value) -> 
            System.out.println("   - " + key + ": " + value)
        );
        System.out.println();
    }

    /**
     * Message priority
     */
    private void demonstratePriority() {
        System.out.println("3. Message Priority:");

        for (int priority = 0; priority <= 10; priority += 5) {
            MessageProperties properties = MessagePropertiesBuilder.newInstance()
                    .setPriority(priority)
                    .setContentType("text/plain")
                    .build();

            String messageBody = "Priority " + priority + " message";
            Message message = new Message(messageBody.getBytes(), properties);
            rabbitTemplate.send(QUEUE_NAME, message);

            System.out.println("   - Sent message with priority: " + priority);
        }
        System.out.println("   Note: Priority ranges from 0 (lowest) to 10 (highest)");
        System.out.println();
    }

    /**
     * Message expiration (TTL - Time To Live)
     */
    private void demonstrateExpiration() {
        System.out.println("4. Message Expiration (TTL):");

        // Message expires in 10 seconds
        MessageProperties properties = MessagePropertiesBuilder.newInstance()
                .setExpiration("10000") // milliseconds
                .setContentType("text/plain")
                .build();

        Message message = new Message("Expiring message".getBytes(), properties);
        rabbitTemplate.send(QUEUE_NAME, message);

        System.out.println("   - Message sent with 10 second TTL");
        System.out.println("   - Message will be discarded if not consumed within 10s");
        System.out.println();
    }

    /**
     * Request-reply properties
     */
    private void demonstrateRequestReplyProperties() {
        System.out.println("5. Request-Reply Properties:");

        String correlationId = "CORR-" + System.currentTimeMillis();
        String replyTo = "reply-queue";

        MessageProperties properties = MessagePropertiesBuilder.newInstance()
                .setCorrelationId(correlationId)
                .setReplyTo(replyTo)
                .setContentType("application/json")
                .build();

        Message message = new Message("{\"request\":\"data\"}".getBytes(), properties);
        rabbitTemplate.send(QUEUE_NAME, message);

        System.out.println("   Request message sent with:");
        System.out.println("   - Correlation ID: " + correlationId);
        System.out.println("   - Reply-To: " + replyTo);
        System.out.println("   Used for correlating request and reply messages");
        System.out.println();
    }

    /**
     * Message persistence
     */
    private void demonstratePersistence() {
        System.out.println("6. Message Persistence:");

        // Persistent message
        MessageProperties persistentProps = MessagePropertiesBuilder.newInstance()
                .setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE) // 2 = persistent
                .setContentType("text/plain")
                .build();

        Message persistentMessage = new Message("Persistent message".getBytes(), persistentProps);
        rabbitTemplate.send(QUEUE_NAME, persistentMessage);
        System.out.println("   - Sent PERSISTENT message (survives broker restart)");

        // Non-persistent message
        MessageProperties nonPersistentProps = MessagePropertiesBuilder.newInstance()
                .setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE)
                .setContentType("text/plain")
                .build();
        nonPersistentProps.setDeliveryMode(1); // 1 = non-persistent

        Message nonPersistentMessage = new Message("Non-persistent message".getBytes(), nonPersistentProps);
        rabbitTemplate.send(QUEUE_NAME, nonPersistentMessage);
        System.out.println("   - Sent NON-PERSISTENT message (in-memory only)");

        System.out.println("\n7. All AMQP Message Properties:");
        System.out.println("   Standard Properties:");
        System.out.println("   - contentType: MIME type of message body");
        System.out.println("   - contentEncoding: Character encoding");
        System.out.println("   - headers: Custom key-value pairs");
        System.out.println("   - deliveryMode: 1=non-persistent, 2=persistent");
        System.out.println("   - priority: 0-10 (queue must support priority)");
        System.out.println("   - correlationId: For request-reply correlation");
        System.out.println("   - replyTo: Queue name for replies");
        System.out.println("   - expiration: TTL in milliseconds");
        System.out.println("   - messageId: Application-specific message identifier");
        System.out.println("   - timestamp: Message creation time");
        System.out.println("   - type: Message type name");
        System.out.println("   - userId: Authenticated user");
        System.out.println("   - appId: Application identifier");
        System.out.println("   - clusterId: Cluster identifier");

        System.out.println("\n8. Property Builder Pattern:");
        System.out.println("   Use MessagePropertiesBuilder for fluent API:");
        System.out.println("   MessagePropertiesBuilder.newInstance()");
        System.out.println("       .setContentType(\"application/json\")");
        System.out.println("       .setPriority(5)");
        System.out.println("       .setExpiration(\"30000\")");
        System.out.println("       .setHeader(\"custom\", \"value\")");
        System.out.println("       .build()");
    }
}
