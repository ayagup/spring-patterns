package com.example.amqp.patterns;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.UUID;

/**
 * Message Post Processor Pattern
 * 
 * Demonstrates the use of MessagePostProcessor to modify messages before sending.
 * Post processors allow for cross-cutting concerns like adding headers, setting
 * properties, message transformation, compression, encryption, etc.
 * 
 * Key Features:
 * - Add/modify message headers
 * - Set message properties dynamically
 * - Message transformation
 * - Compression before sending
 * - Encryption/signing
 * - Audit trail headers
 * - Correlation ID injection
 */
@SpringBootApplication
public class MessagePostProcessorPattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String QUEUE_NAME = "message-post-processor-queue";

    public static void main(String[] args) {
        SpringApplication.run(MessagePostProcessorPattern.class, args);
    }

    @Bean
    public Queue postProcessorQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Override
    public void run(String... args) {
        demonstrateBasicPostProcessor();
        demonstrateHeaderInjection();
        demonstrateCorrelationId();
        demonstrateTimestampInjection();
        demonstrateChainedPostProcessors();
        demonstrateConditionalProcessing();
    }

    /**
     * Basic message post processor
     */
    private void demonstrateBasicPostProcessor() {
        System.out.println("=== Message Post Processor Pattern ===\n");
        System.out.println("1. Basic Post Processor:");

        MessagePostProcessor postProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) {
                MessageProperties props = message.getMessageProperties();
                props.setHeader("processed", true);
                props.setHeader("processor", "BasicPostProcessor");
                System.out.println("   - Message post-processed");
                return message;
            }
        };

        rabbitTemplate.convertAndSend(QUEUE_NAME, "Hello AMQP", postProcessor);
        System.out.println("   - Message sent with post-processor");
        System.out.println();
    }

    /**
     * Header injection post processor
     */
    private void demonstrateHeaderInjection() {
        System.out.println("2. Header Injection Post Processor:");

        MessagePostProcessor headerInjector = message -> {
            MessageProperties props = message.getMessageProperties();
            props.setHeader("request-id", UUID.randomUUID().toString());
            props.setHeader("source", "order-service");
            props.setHeader("version", "1.0.0");
            props.setHeader("environment", "production");
            System.out.println("   - Injected headers:");
            System.out.println("     * request-id: " + props.getHeader("request-id"));
            System.out.println("     * source: order-service");
            System.out.println("     * version: 1.0.0");
            System.out.println("     * environment: production");
            return message;
        };

        rabbitTemplate.convertAndSend(QUEUE_NAME, "Order data", headerInjector);
        System.out.println();
    }

    /**
     * Correlation ID injection
     */
    private void demonstrateCorrelationId() {
        System.out.println("3. Correlation ID Injection:");

        MessagePostProcessor correlationIdInjector = message -> {
            MessageProperties props = message.getMessageProperties();
            String correlationId = UUID.randomUUID().toString();
            props.setCorrelationId(correlationId);
            System.out.println("   - Correlation ID injected: " + correlationId);
            return message;
        };

        rabbitTemplate.convertAndSend(QUEUE_NAME, "Request message", correlationIdInjector);
        System.out.println();
    }

    /**
     * Timestamp injection
     */
    private void demonstrateTimestampInjection() {
        System.out.println("4. Timestamp Injection:");

        MessagePostProcessor timestampInjector = message -> {
            MessageProperties props = message.getMessageProperties();
            Date timestamp = new Date();
            props.setTimestamp(timestamp);
            props.setHeader("sent-at", timestamp.getTime());
            System.out.println("   - Timestamp injected: " + timestamp);
            return message;
        };

        rabbitTemplate.convertAndSend(QUEUE_NAME, "Timestamped message", timestampInjector);
        System.out.println();
    }

    /**
     * Chained post processors
     */
    private void demonstrateChainedPostProcessors() {
        System.out.println("5. Chained Post Processors:");

        // First processor: Add correlation ID
        MessagePostProcessor correlationProcessor = message -> {
            message.getMessageProperties().setCorrelationId(UUID.randomUUID().toString());
            System.out.println("   - Step 1: Correlation ID added");
            return message;
        };

        // Second processor: Add timestamp
        MessagePostProcessor timestampProcessor = message -> {
            message.getMessageProperties().setTimestamp(new Date());
            System.out.println("   - Step 2: Timestamp added");
            return message;
        };

        // Third processor: Add custom headers
        MessagePostProcessor headerProcessor = message -> {
            MessageProperties props = message.getMessageProperties();
            props.setHeader("priority", "HIGH");
            props.setHeader("retry-count", 0);
            System.out.println("   - Step 3: Custom headers added");
            return message;
        };

        // Chain processors manually
        MessagePostProcessor chainedProcessor = message -> {
            message = correlationProcessor.postProcessMessage(message);
            message = timestampProcessor.postProcessMessage(message);
            message = headerProcessor.postProcessMessage(message);
            return message;
        };

        rabbitTemplate.convertAndSend(QUEUE_NAME, "Chained processing", chainedProcessor);
        System.out.println();
    }

    /**
     * Conditional processing
     */
    private void demonstrateConditionalProcessing() {
        System.out.println("6. Conditional Post Processing:");

        MessagePostProcessor conditionalProcessor = message -> {
            MessageProperties props = message.getMessageProperties();
            String messageBody = new String(message.getBody());

            // Add priority header for urgent messages
            if (messageBody.contains("URGENT")) {
                props.setPriority(10);
                props.setHeader("priority-level", "CRITICAL");
                System.out.println("   - URGENT message detected");
                System.out.println("   - Priority set to: 10 (highest)");
            } else {
                props.setPriority(5);
                props.setHeader("priority-level", "NORMAL");
                System.out.println("   - Normal message");
                System.out.println("   - Priority set to: 5 (normal)");
            }

            return message;
        };

        rabbitTemplate.convertAndSend(QUEUE_NAME, "Normal message", conditionalProcessor);
        rabbitTemplate.convertAndSend(QUEUE_NAME, "URGENT message", conditionalProcessor);

        System.out.println("\n7. Common Post Processor Use Cases:");
        System.out.println("   - Add correlation IDs for tracing");
        System.out.println("   - Inject timestamps for auditing");
        System.out.println("   - Add security headers/tokens");
        System.out.println("   - Compress large messages");
        System.out.println("   - Encrypt sensitive data");
        System.out.println("   - Add routing metadata");
        System.out.println("   - Set expiration dynamically");
        System.out.println("   - Add user/tenant context");
        System.out.println("   - Transform message format");
        System.out.println("   - Add retry/circuit breaker metadata");

        System.out.println("\n8. Post Processor Benefits:");
        System.out.println("   - Separation of concerns");
        System.out.println("   - Reusable message enhancement");
        System.out.println("   - Cross-cutting functionality");
        System.out.println("   - Clean business logic");
        System.out.println("   - Centralized message policies");
        System.out.println("   - Easy to test and maintain");
    }

    /**
     * Example: Audit trail post processor
     */
    private MessagePostProcessor createAuditPostProcessor() {
        return message -> {
            MessageProperties props = message.getMessageProperties();
            props.setHeader("audit-user", "current-user");
            props.setHeader("audit-timestamp", System.currentTimeMillis());
            props.setHeader("audit-action", "MESSAGE_SENT");
            return message;
        };
    }

    /**
     * Example: Compression post processor
     */
    private MessagePostProcessor createCompressionPostProcessor() {
        return message -> {
            // Simulate compression
            MessageProperties props = message.getMessageProperties();
            props.setHeader("compressed", true);
            props.setHeader("original-size", message.getBody().length);
            props.setContentEncoding("gzip");
            // In real implementation, compress the body here
            return message;
        };
    }
}
