package com.example.rabbitmq.patterns;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import java.util.Map;

/**
 * Queue Pattern
 * 
 * Demonstrates various queue configurations in RabbitMQ including
 * durable queues, exclusive queues, auto-delete queues, and queues
 * with special properties.
 * 
 * Key Features:
 * - Durable vs non-durable queues
 * - Exclusive queues
 * - Auto-delete queues
 * - Queue arguments (TTL, max length, etc.)
 * - Dead letter queues
 * - Priority queues
 * - Lazy queues
 * - Programmatic queue declaration
 */
@SpringBootApplication
public class QueuePattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    public static void main(String[] args) {
        SpringApplication.run(QueuePattern.class, args);
    }

    /**
     * Basic durable queue - survives broker restart
     */
    @Bean
    public Queue durableQueue() {
        return new Queue("queue-durable", true);
    }

    /**
     * Non-durable queue - deleted on broker restart
     */
    @Bean
    public Queue nonDurableQueue() {
        return new Queue("queue-non-durable", false);
    }

    /**
     * Exclusive queue - used by only one connection
     */
    @Bean
    public Queue exclusiveQueue() {
        return new Queue("queue-exclusive", false, true, false);
    }

    /**
     * Auto-delete queue - deleted when last consumer disconnects
     */
    @Bean
    public Queue autoDeleteQueue() {
        return new Queue("queue-auto-delete", false, false, true);
    }

    /**
     * Queue with message TTL (Time To Live)
     */
    @Bean
    public Queue ttlQueue() {
        return QueueBuilder
                .durable("queue-ttl")
                .ttl(60000) // 60 seconds
                .build();
    }

    /**
     * Queue with max length
     */
    @Bean
    public Queue maxLengthQueue() {
        return QueueBuilder
                .durable("queue-max-length")
                .maxLength(1000)
                .build();
    }

    /**
     * Queue with max length in bytes
     */
    @Bean
    public Queue maxLengthBytesQueue() {
        return QueueBuilder
                .durable("queue-max-length-bytes")
                .maxLengthBytes(1048576) // 1MB
                .build();
    }

    /**
     * Queue with overflow behavior
     */
    @Bean
    public Queue overflowQueue() {
        return QueueBuilder
                .durable("queue-overflow")
                .maxLength(100)
                .overflow(QueueBuilder.Overflow.rejectPublish)
                .build();
    }

    /**
     * Priority queue - supports message priorities
     */
    @Bean
    public Queue priorityQueue() {
        return QueueBuilder
                .durable("queue-priority")
                .maxPriority(10)
                .build();
    }

    /**
     * Lazy queue - messages stored on disk
     */
    @Bean
    public Queue lazyQueue() {
        return QueueBuilder
                .durable("queue-lazy")
                .lazy()
                .build();
    }

    /**
     * Queue with dead letter exchange
     */
    @Bean
    public Queue queueWithDLX() {
        return QueueBuilder
                .durable("queue-with-dlx")
                .deadLetterExchange("dlx-exchange")
                .deadLetterRoutingKey("dlx.key")
                .build();
    }

    /**
     * Queue with multiple arguments
     */
    @Bean
    public Queue complexQueue() {
        return QueueBuilder
                .durable("queue-complex")
                .ttl(30000)
                .maxLength(500)
                .maxPriority(5)
                .withArgument("x-queue-mode", "lazy")
                .withArgument("x-message-ttl", 60000)
                .build();
    }

    @Override
    public void run(String... args) {
        demonstrateQueueTypes();
        demonstrateProgrammaticQueueCreation();
    }

    private void demonstrateQueueTypes() {
        System.out.println("=== Queue Pattern Demonstration ===\n");

        // Durable Queue
        System.out.println("1. Durable Queue:");
        System.out.println("   - Survives broker restart");
        System.out.println("   - Messages persist to disk");
        rabbitTemplate.convertAndSend("queue-durable", "Durable message");
        System.out.println("   - Message sent to durable queue\n");

        // TTL Queue
        System.out.println("2. TTL Queue:");
        System.out.println("   - Messages expire after 60 seconds");
        rabbitTemplate.convertAndSend("queue-ttl", "TTL message");
        System.out.println("   - Message sent to TTL queue\n");

        // Max Length Queue
        System.out.println("3. Max Length Queue:");
        System.out.println("   - Limited to 1000 messages");
        System.out.println("   - Oldest messages dropped when full");
        for (int i = 1; i <= 5; i++) {
            rabbitTemplate.convertAndSend("queue-max-length", "Message " + i);
        }
        System.out.println("   - 5 messages sent to max length queue\n");

        // Priority Queue
        System.out.println("4. Priority Queue:");
        System.out.println("   - Messages processed by priority (0-10)");
        sendPriorityMessages();
        System.out.println("   - Priority messages sent\n");

        // Lazy Queue
        System.out.println("5. Lazy Queue:");
        System.out.println("   - Messages stored on disk immediately");
        System.out.println("   - Reduces memory usage for large queues");
        rabbitTemplate.convertAndSend("queue-lazy", "Lazy queue message");
        System.out.println("   - Message sent to lazy queue\n");

        // Queue with DLX
        System.out.println("6. Queue with Dead Letter Exchange:");
        System.out.println("   - Failed messages routed to DLX");
        System.out.println("   - Useful for error handling");
        rabbitTemplate.convertAndSend("queue-with-dlx", "Message with DLX");
        System.out.println("   - Message sent to queue with DLX\n");
    }

    private void sendPriorityMessages() {
        // Send messages with different priorities
        for (int priority = 1; priority <= 10; priority += 3) {
            int finalPriority = priority;
            rabbitTemplate.convertAndSend("queue-priority", 
                "Priority " + priority + " message", message -> {
                    message.getMessageProperties().setPriority(finalPriority);
                    return message;
                });
        }
    }

    private void demonstrateProgrammaticQueueCreation() {
        System.out.println("7. Programmatic Queue Creation:");
        
        // Create queue programmatically
        Queue dynamicQueue = QueueBuilder
                .durable("queue-dynamic")
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-max-length", 100)
                .build();
        
        amqpAdmin.declareQueue(dynamicQueue);
        System.out.println("   - Dynamic queue created: queue-dynamic");
        System.out.println("   - TTL: 30 seconds, Max Length: 100");
        
        rabbitTemplate.convertAndSend("queue-dynamic", "Dynamic queue message");
        System.out.println("   - Message sent to dynamic queue\n");

        // Get queue properties
        System.out.println("8. Queue Properties:");
        System.out.println("   - Name: " + dynamicQueue.getName());
        System.out.println("   - Durable: " + dynamicQueue.isDurable());
        System.out.println("   - Exclusive: " + dynamicQueue.isExclusive());
        System.out.println("   - Auto-delete: " + dynamicQueue.isAutoDelete());
        System.out.println("   - Arguments: " + dynamicQueue.getArguments());
    }
}
