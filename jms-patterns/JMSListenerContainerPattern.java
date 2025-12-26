package com.example.jms.patterns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.Session;

/**
 * JMS Listener Container Pattern
 * 
 * Demonstrates configuration and use of JMS listener containers for
 * advanced message consumption scenarios including concurrency, caching,
 * and error handling.
 * 
 * Key Features:
 * - Container factory configuration
 * - Concurrent consumers
 * - Cache level settings
 * - Session transacted mode
 * - Error handling
 * - Backoff policies
 * - Programmatic container creation
 */
@SpringBootApplication
public class JMSListenerContainerPattern implements CommandLineRunner {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    private static final String CONCURRENT_QUEUE = "jms-container-concurrent";
    private static final String TRANSACTED_QUEUE = "jms-container-transacted";
    private static final String PROGRAMMATIC_QUEUE = "jms-container-programmatic";

    public static void main(String[] args) {
        SpringApplication.run(JMSListenerContainerPattern.class, args);
    }

    /**
     * Custom container factory with concurrent consumers
     */
    @Bean
    public JmsListenerContainerFactory<?> concurrentContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        
        // Configure concurrency
        factory.setConcurrency("3-10"); // Min 3, Max 10 consumers
        
        // Configure cache level
        factory.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
        
        // Set session acknowledge mode
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        
        // Configure error handler
        factory.setErrorHandler(new CustomErrorHandler());
        
        return factory;
    }

    /**
     * Container factory with transaction support
     */
    @Bean
    public JmsListenerContainerFactory<?> transactedContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        
        // Enable session transactions
        factory.setSessionTransacted(true);
        
        // Configure transaction timeout
        factory.setReceiveTimeout(5000L);
        
        return factory;
    }

    /**
     * Programmatically created container
     */
    @Bean
    public DefaultMessageListenerContainer programmaticContainer() {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(PROGRAMMATIC_QUEUE);
        
        // Configure container properties
        container.setConcurrentConsumers(2);
        container.setMaxConcurrentConsumers(5);
        container.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
        
        // Set message listener
        MessageListenerAdapter adapter = new MessageListenerAdapter(
            new ProgrammaticMessageHandler(), "handleMessage");
        container.setMessageListener(adapter);
        
        return container;
    }

    @Override
    public void run(String... args) throws InterruptedException {
        sendTestMessages();
        Thread.sleep(5000);
    }

    private void sendTestMessages() {
        System.out.println("=== JMS Listener Container Pattern ===\n");

        // Send messages to concurrent queue
        System.out.println("1. Sending messages to concurrent queue:");
        for (int i = 1; i <= 15; i++) {
            jmsTemplate.convertAndSend(CONCURRENT_QUEUE, "Concurrent message #" + i);
        }
        System.out.println("   - 15 messages sent\n");

        // Send messages to transacted queue
        System.out.println("2. Sending messages to transacted queue:");
        for (int i = 1; i <= 5; i++) {
            jmsTemplate.convertAndSend(TRANSACTED_QUEUE, "Transacted message #" + i);
        }
        System.out.println("   - 5 messages sent\n");

        // Send messages to programmatic queue
        System.out.println("3. Sending messages to programmatic queue:");
        for (int i = 1; i <= 10; i++) {
            jmsTemplate.convertAndSend(PROGRAMMATIC_QUEUE, "Programmatic message #" + i);
        }
        System.out.println("   - 10 messages sent\n");
    }

    /**
     * Message listeners using different container factories
     */
    @Component
    static class ContainerListeners {

        /**
         * Listener using concurrent container factory
         */
        @JmsListener(destination = CONCURRENT_QUEUE, 
                    containerFactory = "concurrentContainerFactory")
        public void handleConcurrentMessage(String message) {
            String threadName = Thread.currentThread().getName();
            System.out.println("[Concurrent] Thread " + threadName + ": " + message);
            
            // Simulate processing
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Listener with transaction support
         */
        @JmsListener(destination = TRANSACTED_QUEUE, 
                    containerFactory = "transactedContainerFactory")
        public void handleTransactedMessage(String message, Session session) {
            try {
                System.out.println("[Transacted] Processing: " + message);
                System.out.println("  Session transacted: " + session.getTransacted());
                
                // Simulate processing
                Thread.sleep(200);
                
                // Transaction will auto-commit if no exception
                System.out.println("  Transaction committed");
                
            } catch (Exception e) {
                System.err.println("  Error: " + e.getMessage());
                // Transaction will rollback on exception
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
     * Custom error handler
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
