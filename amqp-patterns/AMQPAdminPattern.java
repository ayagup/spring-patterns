package com.example.amqp.patterns;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import javax.management.MBeanServerConnection;
import java.util.Properties;

/**
 * AMQP Admin Pattern
 * 
 * Demonstrates the use of RabbitAdmin for runtime management of
 * AMQP entities (exchanges, queues, bindings). Enables dynamic
 * infrastructure configuration and administration.
 * 
 * Key Features:
 * - Dynamic queue declaration
 * - Dynamic exchange declaration
 * - Dynamic binding creation
 * - Queue purging
 * - Queue deletion
 * - Exchange deletion
 * - Queue properties inspection
 */
@SpringBootApplication
public class AMQPAdminPattern implements CommandLineRunner {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static void main(String[] args) {
        SpringApplication.run(AMQPAdminPattern.class, args);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Override
    public void run(String... args) {
        demonstrateDynamicQueueCreation();
        demonstrateDynamicExchangeCreation();
        demonstrateDynamicBindings();
        demonstrateQueueManagement();
        demonstrateQueueProperties();
    }

    /**
     * Dynamic queue creation
     */
    private void demonstrateDynamicQueueCreation() {
        System.out.println("=== AMQP Admin Pattern ===\n");
        System.out.println("1. Dynamic Queue Creation:");

        // Create simple queue
        Queue simpleQueue = new Queue("admin-simple-queue", false);
        rabbitAdmin.declareQueue(simpleQueue);
        System.out.println("   - Created simple queue: " + simpleQueue.getName());

        // Create durable queue
        Queue durableQueue = new Queue("admin-durable-queue", true);
        rabbitAdmin.declareQueue(durableQueue);
        System.out.println("   - Created durable queue: " + durableQueue.getName());

        // Create queue with arguments
        Queue ttlQueue = QueueBuilder
                .durable("admin-ttl-queue")
                .ttl(30000)
                .maxLength(100)
                .build();
        rabbitAdmin.declareQueue(ttlQueue);
        System.out.println("   - Created TTL queue: " + ttlQueue.getName());
        System.out.println("     TTL: 30s, Max Length: 100");

        System.out.println();
    }

    /**
     * Dynamic exchange creation
     */
    private void demonstrateDynamicExchangeCreation() {
        System.out.println("2. Dynamic Exchange Creation:");

        // Create direct exchange
        DirectExchange directExchange = new DirectExchange("admin-direct-exchange");
        rabbitAdmin.declareExchange(directExchange);
        System.out.println("   - Created direct exchange: " + directExchange.getName());

        // Create topic exchange
        TopicExchange topicExchange = new TopicExchange("admin-topic-exchange");
        rabbitAdmin.declareExchange(topicExchange);
        System.out.println("   - Created topic exchange: " + topicExchange.getName());

        // Create fanout exchange
        FanoutExchange fanoutExchange = new FanoutExchange("admin-fanout-exchange");
        rabbitAdmin.declareExchange(fanoutExchange);
        System.out.println("   - Created fanout exchange: " + fanoutExchange.getName());

        // Create headers exchange
        HeadersExchange headersExchange = new HeadersExchange("admin-headers-exchange");
        rabbitAdmin.declareExchange(headersExchange);
        System.out.println("   - Created headers exchange: " + headersExchange.getName());

        System.out.println();
    }

    /**
     * Dynamic binding creation
     */
    private void demonstrateDynamicBindings() {
        System.out.println("3. Dynamic Binding Creation:");

        // Create queue and exchange
        Queue queue = new Queue("admin-bind-queue", false);
        DirectExchange exchange = new DirectExchange("admin-bind-exchange");
        
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        System.out.println("   - Created queue and exchange");

        // Create binding
        Binding binding = BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("admin.key");
        rabbitAdmin.declareBinding(binding);
        System.out.println("   - Created binding: " + queue.getName() + 
                         " -> " + exchange.getName() + " (key: admin.key)");

        // Test the binding
        rabbitTemplate.convertAndSend("admin-bind-exchange", "admin.key", 
            "Test message for dynamic binding");
        System.out.println("   - Test message sent");

        String received = (String) rabbitTemplate.receiveAndConvert("admin-bind-queue", 2000);
        System.out.println("   - Message received: " + received);

        System.out.println();
    }

    /**
     * Queue management operations
     */
    private void demonstrateQueueManagement() {
        System.out.println("4. Queue Management:");

        // Create and populate a queue
        Queue managedQueue = new Queue("admin-managed-queue", false);
        rabbitAdmin.declareQueue(managedQueue);
        System.out.println("   - Created managed queue");

        // Add messages to queue
        for (int i = 1; i <= 5; i++) {
            rabbitTemplate.convertAndSend("admin-managed-queue", "Message #" + i);
        }
        System.out.println("   - Added 5 messages to queue");

        // Get queue properties (message count)
        Properties queueProps = rabbitAdmin.getQueueProperties("admin-managed-queue");
        if (queueProps != null) {
            System.out.println("   - Message count: " + queueProps.get("QUEUE_MESSAGE_COUNT"));
            System.out.println("   - Consumer count: " + queueProps.get("QUEUE_CONSUMER_COUNT"));
        }

        // Purge queue (remove all messages)
        rabbitAdmin.purgeQueue("admin-managed-queue");
        System.out.println("   - Queue purged");

        // Verify purge
        queueProps = rabbitAdmin.getQueueProperties("admin-managed-queue");
        if (queueProps != null) {
            System.out.println("   - Message count after purge: " + 
                             queueProps.get("QUEUE_MESSAGE_COUNT"));
        }

        // Delete queue
        boolean deleted = rabbitAdmin.deleteQueue("admin-managed-queue");
        System.out.println("   - Queue deleted: " + deleted);

        System.out.println();
    }

    /**
     * Queue properties inspection
     */
    private void demonstrateQueueProperties() {
        System.out.println("5. Queue Properties Inspection:");

        // Create queue with specific properties
        Queue inspectQueue = QueueBuilder
                .durable("admin-inspect-queue")
                .withArgument("x-message-ttl", 60000)
                .withArgument("x-max-length", 1000)
                .withArgument("x-max-priority", 10)
                .build();
        
        rabbitAdmin.declareQueue(inspectQueue);
        System.out.println("   - Created queue with custom properties");

        // Get queue information
        Properties props = rabbitAdmin.getQueueProperties("admin-inspect-queue");
        if (props != null) {
            System.out.println("   - Queue name: " + props.get("QUEUE_NAME"));
            System.out.println("   - Message count: " + props.get("QUEUE_MESSAGE_COUNT"));
            System.out.println("   - Consumer count: " + props.get("QUEUE_CONSUMER_COUNT"));
        }

        // Queue info
        System.out.println("   - Queue durable: " + inspectQueue.isDurable());
        System.out.println("   - Queue exclusive: " + inspectQueue.isExclusive());
        System.out.println("   - Queue auto-delete: " + inspectQueue.isAutoDelete());
        System.out.println("   - Queue arguments: " + inspectQueue.getArguments());

        System.out.println("\n6. AMQP Admin Benefits:");
        System.out.println("   - Dynamic infrastructure management");
        System.out.println("   - Runtime queue/exchange creation");
        System.out.println("   - Binding management");
        System.out.println("   - Queue purging and deletion");
        System.out.println("   - Infrastructure monitoring");
        System.out.println("   - Multi-tenancy support");
    }
}
