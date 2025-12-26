package com.example.rabbitmq.patterns;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

/**
 * Rabbit Listener Container Pattern
 * 
 * Demonstrates configuration and use of message listener containers for
 * advanced message consumption scenarios including concurrency, prefetch,
 * and error handling.
 * 
 * Key Features:
 * - Container factory configuration
 * - Concurrent consumers
 * - Prefetch count settings
 * - Acknowledgment modes
 * - Error handling strategies
 * - Programmatic container creation
 */
@SpringBootApplication
public class RabbitListenerContainerPattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    private static final String CONCURRENT_QUEUE = "container-concurrent-queue";
    private static final String MANUAL_ACK_QUEUE = "container-manual-ack-queue";
    private static final String PROGRAMMATIC_QUEUE = "container-programmatic-queue";

    public static void main(String[] args) {
        SpringApplication.run(RabbitListenerContainerPattern.class, args);
    }

    @Bean
    public Queue concurrentQueue() {
        return new Queue(CONCURRENT_QUEUE, false);
    }

    @Bean
    public Queue manualAckQueue() {
        return new Queue(MANUAL_ACK_QUEUE, false);
    }

    @Bean
    public Queue programmaticQueue() {
        return new Queue(PROGRAMMATIC_QUEUE, false);
    }

    /**
     * Custom container factory with concurrent consumers
     */
    @Bean
    public SimpleRabbitListenerContainerFactory concurrentContainerFactory(
            ConnectionFactory connectionFactory) {
        
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        
        // Configure concurrent consumers
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        
        // Configure prefetch count
        factory.setPrefetchCount(10);
        
        // Set acknowledge mode
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        
        // Configure error handler
        factory.setErrorHandler(new CustomErrorHandler());
        
        return factory;
    }

    /**
     * Container factory with manual acknowledgment
     */
    @Bean
    public SimpleRabbitListenerContainerFactory manualAckContainerFactory(
            ConnectionFactory connectionFactory) {
        
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(5);
        
        return factory;
    }

    /**
     * Programmatically created container
     */
    @Bean
    public SimpleMessageListenerContainer programmaticContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(PROGRAMMATIC_QUEUE);
        container.setConcurrentConsumers(2);
        container.setMaxConcurrentConsumers(5);
        container.setPrefetchCount(5);
        
        // Set message listener
        MessageListenerAdapter adapter = new MessageListenerAdapter(
            new ProgrammaticMessageHandler(), "handleMessage");
        container.setMessageListener(adapter);
        
        return container;
    }

    @Override
    public void run(String... args) throws InterruptedException {
        sendTestMessages();
        // Wait for async processing
        Thread.sleep(5000);
    }

    private void sendTestMessages() {
        System.out.println("=== Sending Test Messages ===\n");
        
        // Send messages to concurrent queue
        System.out.println("Sending messages to concurrent queue...");
        for (int i = 1; i <= 15; i++) {
            rabbitTemplate.convertAndSend(CONCURRENT_QUEUE, 
                "Concurrent message #" + i);
        }
        
        // Send messages to manual ack queue
        System.out.println("Sending messages to manual ack queue...");
        for (int i = 1; i <= 5; i++) {
            rabbitTemplate.convertAndSend(MANUAL_ACK_QUEUE, 
                "Manual ack message #" + i);
        }
        
        // Send messages to programmatic queue
        System.out.println("Sending messages to programmatic queue...");
        for (int i = 1; i <= 10; i++) {
            rabbitTemplate.convertAndSend(PROGRAMMATIC_QUEUE, 
                "Programmatic message #" + i);
        }
    }

    /**
     * Message listeners using different container factories
     */
    @Component
    static class MessageListeners {

        /**
         * Listener using concurrent container factory
         */
        @RabbitListener(queues = CONCURRENT_QUEUE, 
                       containerFactory = "concurrentContainerFactory")
        public void handleConcurrentMessage(String message) {
            String threadName = Thread.currentThread().getName();
            System.out.println("[Concurrent] Thread " + threadName + ": " + message);
            
            // Simulate processing
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Listener with manual acknowledgment
         */
        @RabbitListener(queues = MANUAL_ACK_QUEUE, 
                       containerFactory = "manualAckContainerFactory")
        public void handleManualAckMessage(String message, 
                org.springframework.amqp.core.Channel channel,
                @org.springframework.messaging.handler.annotation.Header(
                    org.springframework.amqp.support.AmqpHeaders.DELIVERY_TAG) long tag) {
            
            try {
                System.out.println("[Manual ACK] Processing: " + message);
                
                // Simulate processing
                Thread.sleep(200);
                
                // Manually acknowledge
                channel.basicAck(tag, false);
                System.out.println("[Manual ACK] Acknowledged: " + message);
                
            } catch (Exception e) {
                System.err.println("[Manual ACK] Error processing: " + e.getMessage());
                try {
                    // Reject and requeue on error
                    channel.basicNack(tag, false, true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Handler for programmatically created container
     */
    static class ProgrammaticMessageHandler {
        public void handleMessage(String message) {
            String threadName = Thread.currentThread().getName();
            System.out.println("[Programmatic] Thread " + threadName + ": " + message);
        }
    }

    /**
     * Custom error handler for container
     */
    static class CustomErrorHandler implements ErrorHandler {
        @Override
        public void handleError(Throwable t) {
            System.err.println("[Error Handler] Caught exception: " + t.getMessage());
            // Implement custom error handling logic
            // e.g., logging, alerting, dead letter queue routing
        }
    }
}
