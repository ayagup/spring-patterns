package com.example.rabbitmq.patterns;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Rabbit Listener Pattern
 * 
 * Demonstrates the use of @RabbitListener annotation for asynchronous message
 * consumption. This pattern enables automatic message handling with various
 * parameter binding options.
 * 
 * Key Features:
 * - Asynchronous message consumption
 * - Automatic message conversion
 * - Header extraction
 * - Multiple listener methods
 * - Exception handling
 */
@SpringBootApplication
public class RabbitListenerPattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String TEXT_QUEUE = "listener-text-queue";
    private static final String OBJECT_QUEUE = "listener-object-queue";
    private static final String HEADER_QUEUE = "listener-header-queue";

    public static void main(String[] args) {
        SpringApplication.run(RabbitListenerPattern.class, args);
    }

    @Bean
    public Queue textQueue() {
        return new Queue(TEXT_QUEUE, false);
    }

    @Bean
    public Queue objectQueue() {
        return new Queue(OBJECT_QUEUE, false);
    }

    @Bean
    public Queue headerQueue() {
        return new Queue(HEADER_QUEUE, false);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        sendTestMessages();
        // Wait for async processing
        Thread.sleep(3000);
    }

    private void sendTestMessages() {
        System.out.println("=== Sending Test Messages ===\n");
        
        // Send simple text message
        rabbitTemplate.convertAndSend(TEXT_QUEUE, "Hello from RabbitListener Pattern");
        
        // Send object message
        UserMessage user = new UserMessage("john.doe", "john@example.com");
        rabbitTemplate.convertAndSend(OBJECT_QUEUE, user);
        
        // Send message with headers
        rabbitTemplate.convertAndSend(HEADER_QUEUE, "Message with headers", message -> {
            MessageProperties props = message.getMessageProperties();
            props.setHeader("userId", "12345");
            props.setHeader("priority", "high");
            props.setHeader("source", "web-app");
            return message;
        });
    }

    /**
     * Message listener component
     */
    @Component
    static class MessageListenerComponent {

        /**
         * Basic listener for text messages
         */
        @RabbitListener(queues = TEXT_QUEUE)
        public void handleTextMessage(String message) {
            System.out.println("\n[Text Listener] Received: " + message);
        }

        /**
         * Listener with automatic object conversion
         */
        @RabbitListener(queues = OBJECT_QUEUE)
        public void handleObjectMessage(UserMessage user) {
            System.out.println("\n[Object Listener] Received user: " + user);
            System.out.println("  Username: " + user.getUsername());
            System.out.println("  Email: " + user.getEmail());
        }

        /**
         * Listener with header extraction using @Header
         */
        @RabbitListener(queues = HEADER_QUEUE)
        public void handleMessageWithHeaders(
                @Payload String payload,
                @Header("userId") String userId,
                @Header("priority") String priority,
                @Header(value = "source", required = false) String source) {
            
            System.out.println("\n[Header Listener] Processing message:");
            System.out.println("  Payload: " + payload);
            System.out.println("  User ID: " + userId);
            System.out.println("  Priority: " + priority);
            System.out.println("  Source: " + source);
        }

        /**
         * Listener receiving raw Message object
         */
        @RabbitListener(queues = TEXT_QUEUE)
        public void handleRawMessage(Message message) {
            System.out.println("\n[Raw Message Listener]");
            System.out.println("  Body: " + new String(message.getBody()));
            System.out.println("  Content Type: " + message.getMessageProperties().getContentType());
            System.out.println("  Timestamp: " + message.getMessageProperties().getTimestamp());
        }
    }

    /**
     * Domain class for message consumption
     */
    static class UserMessage implements java.io.Serializable {
        private String username;
        private String email;

        public UserMessage() {}

        public UserMessage(String username, String email) {
            this.username = username;
            this.email = email;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public String toString() {
            return "UserMessage{username='" + username + "', email='" + email + "'}";
        }
    }
}
