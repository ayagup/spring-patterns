package com.example.amqp.patterns;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Publisher Confirms Pattern
 * 
 * Demonstrates publisher confirms for reliable message delivery.
 * Publisher confirms provide acknowledgments from the broker that messages
 * have been successfully received and persisted (for durable messages).
 * 
 * Key Features:
 * - Enable publisher confirms on connection factory
 * - Correlation data for tracking confirms
 * - Async confirm callbacks
 * - Synchronous waiting for confirms
 * - Handling confirm failures
 * - Batch confirms
 * - Confirm timeout handling
 */
@SpringBootApplication
public class PublisherConfirmsPattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    private static final String QUEUE_NAME = "publisher-confirms-queue";

    public static void main(String[] args) {
        SpringApplication.run(PublisherConfirmsPattern.class, args);
    }

    /**
     * Configure connection factory with publisher confirms
     */
    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        return factory;
    }

    /**
     * Configure RabbitTemplate with confirm callback
     */
    @Bean
    public RabbitTemplate configuredRabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        
        // Set confirm callback
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("   ✓ Message confirmed: " + 
                    (correlationData != null ? correlationData.getId() : "unknown"));
            } else {
                System.out.println("   ✗ Message rejected: " + 
                    (correlationData != null ? correlationData.getId() : "unknown") +
                    " - Reason: " + cause);
            }
        });
        
        return template;
    }

    @Bean
    public Queue confirmsQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Override
    public void run(String... args) throws Exception {
        demonstrateBasicConfirms();
        demonstrateCorrelationData();
        demonstrateAsyncConfirms();
        demonstrateBatchConfirms();
        demonstrateConfirmTimeout();
    }

    /**
     * Basic publisher confirms
     */
    private void demonstrateBasicConfirms() throws Exception {
        System.out.println("=== Publisher Confirms Pattern ===\n");
        System.out.println("1. Basic Publisher Confirms:");
        System.out.println("   Publisher confirms enabled: " + 
            connectionFactory.isPublisherConfirms());

        // Send message and wait for confirm
        rabbitTemplate.convertAndSend(QUEUE_NAME, "Message with confirm");
        System.out.println("   - Message sent, waiting for confirm...");
        
        // Give time for confirm callback
        Thread.sleep(1000);
        System.out.println();
    }

    /**
     * Using correlation data
     */
    private void demonstrateCorrelationData() throws Exception {
        System.out.println("2. Correlation Data:");

        for (int i = 1; i <= 3; i++) {
            String messageId = "MSG-" + i;
            CorrelationData correlationData = new CorrelationData(messageId);
            
            rabbitTemplate.convertAndSend(QUEUE_NAME, "Message " + i, correlationData);
            System.out.println("   - Sent message with correlation ID: " + messageId);
        }

        // Wait for confirms
        Thread.sleep(2000);
        System.out.println();
    }

    /**
     * Async confirms with CompletableFuture
     */
    private void demonstrateAsyncConfirms() throws Exception {
        System.out.println("3. Async Confirms with CompletableFuture:");

        for (int i = 1; i <= 3; i++) {
            String messageId = "ASYNC-" + UUID.randomUUID();
            CorrelationData correlationData = new CorrelationData(messageId);
            
            // Get the future for this confirm
            CompletableFuture<CorrelationData.Confirm> future = 
                correlationData.getFuture();
            
            // Send message
            rabbitTemplate.convertAndSend(QUEUE_NAME, "Async message " + i, correlationData);
            System.out.println("   - Sent async message: " + messageId);
            
            // Handle confirm asynchronously
            future.thenAccept(confirm -> {
                if (confirm.isAck()) {
                    System.out.println("     ✓ Async confirm received for: " + messageId);
                } else {
                    System.out.println("     ✗ Async reject for: " + messageId + 
                        " - Reason: " + confirm.getReason());
                }
            }).exceptionally(ex -> {
                System.out.println("     ✗ Confirm error for: " + messageId + 
                    " - " + ex.getMessage());
                return null;
            });
        }

        // Wait for all confirms
        Thread.sleep(2000);
        System.out.println();
    }

    /**
     * Batch confirms
     */
    private void demonstrateBatchConfirms() throws Exception {
        System.out.println("4. Batch Confirms:");

        int batchSize = 5;
        System.out.println("   Sending batch of " + batchSize + " messages...");

        for (int i = 1; i <= batchSize; i++) {
            CorrelationData correlationData = 
                new CorrelationData("BATCH-" + i);
            
            rabbitTemplate.convertAndSend(QUEUE_NAME, 
                "Batch message " + i, correlationData);
        }

        System.out.println("   - All messages sent");
        System.out.println("   - Waiting for batch confirms...");
        
        // Wait for all confirms
        Thread.sleep(2000);
        System.out.println();
    }

    /**
     * Confirm timeout handling
     */
    private void demonstrateConfirmTimeout() throws Exception {
        System.out.println("5. Confirm Timeout Handling:");

        CorrelationData correlationData = new CorrelationData("TIMEOUT-TEST");
        CompletableFuture<CorrelationData.Confirm> future = 
            correlationData.getFuture();

        rabbitTemplate.convertAndSend(QUEUE_NAME, "Timeout test", correlationData);
        System.out.println("   - Message sent, waiting for confirm with timeout...");

        try {
            // Wait for confirm with timeout
            CorrelationData.Confirm confirm = future.get(5, TimeUnit.SECONDS);
            if (confirm.isAck()) {
                System.out.println("   ✓ Confirm received within timeout");
            } else {
                System.out.println("   ✗ Message rejected: " + confirm.getReason());
            }
        } catch (Exception e) {
            System.out.println("   ✗ Timeout waiting for confirm: " + e.getMessage());
        }

        System.out.println("\n6. Publisher Confirms Configuration:");
        System.out.println("   Connection Factory:");
        System.out.println("   factory.setPublisherConfirmType(");
        System.out.println("       CachingConnectionFactory.ConfirmType.CORRELATED);");
        System.out.println("\n   RabbitTemplate Callback:");
        System.out.println("   template.setConfirmCallback((correlationData, ack, cause) -> {");
        System.out.println("       if (ack) {");
        System.out.println("           // Message confirmed");
        System.out.println("       } else {");
        System.out.println("           // Message rejected");
        System.out.println("       }");
        System.out.println("   });");

        System.out.println("\n7. Confirm Types:");
        System.out.println("   - NONE: No confirms (default)");
        System.out.println("   - SIMPLE: Basic confirms without correlation");
        System.out.println("   - CORRELATED: Confirms with correlation data");

        System.out.println("\n8. Use Cases:");
        System.out.println("   - Ensure message delivery to broker");
        System.out.println("   - Implement retry logic for failed sends");
        System.out.println("   - Track message status in workflow");
        System.out.println("   - Audit successful deliveries");
        System.out.println("   - Handle broker failures gracefully");
        System.out.println("   - Build reliable messaging systems");

        System.out.println("\n9. Best Practices:");
        System.out.println("   - Always use correlation data for tracking");
        System.out.println("   - Set appropriate confirm timeouts");
        System.out.println("   - Implement retry logic for rejects");
        System.out.println("   - Log all confirms for auditing");
        System.out.println("   - Handle exceptions in callbacks");
        System.out.println("   - Consider batching for high throughput");
    }
}
