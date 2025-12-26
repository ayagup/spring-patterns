package com.example.jms.patterns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * JMS Listener Pattern
 * 
 * Demonstrates the use of @JmsListener annotation for asynchronous
 * message consumption. This pattern enables declarative message handling
 * with automatic message conversion and error handling.
 * 
 * Key Features:
 * - Asynchronous message consumption
 * - Automatic message conversion
 * - Header extraction
 * - Multiple listener methods
 * - Selector support
 * - Concurrent consumers
 */
@SpringBootApplication
public class JMSListenerPattern implements CommandLineRunner {

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final String TEXT_QUEUE = "jms-listener-text-queue";
    private static final String OBJECT_QUEUE = "jms-listener-object-queue";
    private static final String HEADER_QUEUE = "jms-listener-header-queue";
    private static final String SELECTOR_QUEUE = "jms-listener-selector-queue";

    public static void main(String[] args) {
        SpringApplication.run(JMSListenerPattern.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        sendTestMessages();
        // Wait for async processing
        Thread.sleep(3000);
    }

    private void sendTestMessages() {
        System.out.println("=== Sending Test Messages ===\n");

        // Send text message
        jmsTemplate.convertAndSend(TEXT_QUEUE, "Hello from JMS Listener");
        System.out.println("1. Text message sent to: " + TEXT_QUEUE);

        // Send object message
        NotificationMessage notification = new NotificationMessage(
            "NOTIF-001", "Payment received", "user@example.com");
        jmsTemplate.convertAndSend(OBJECT_QUEUE, notification);
        System.out.println("2. Object message sent to: " + OBJECT_QUEUE);

        // Send message with headers
        jmsTemplate.convertAndSend(HEADER_QUEUE, "Message with headers", message -> {
            message.setStringProperty("userId", "12345");
            message.setStringProperty("department", "sales");
            message.setIntProperty("priority", 5);
            return message;
        });
        System.out.println("3. Message with headers sent to: " + HEADER_QUEUE);

        // Send messages with selectors
        jmsTemplate.convertAndSend(SELECTOR_QUEUE, "High priority message", message -> {
            message.setStringProperty("priority", "high");
            return message;
        });
        
        jmsTemplate.convertAndSend(SELECTOR_QUEUE, "Low priority message", message -> {
            message.setStringProperty("priority", "low");
            return message;
        });
        System.out.println("4. Messages with selectors sent to: " + SELECTOR_QUEUE + "\n");
    }

    /**
     * JMS Listener components
     */
    @Component
    static class MessageListeners {

        /**
         * Basic text message listener
         */
        @JmsListener(destination = TEXT_QUEUE)
        public void handleTextMessage(String message) {
            System.out.println("[Text Listener] Received: " + message);
        }

        /**
         * Object message listener with automatic conversion
         */
        @JmsListener(destination = OBJECT_QUEUE)
        public void handleObjectMessage(NotificationMessage notification) {
            System.out.println("\n[Object Listener] Received notification:");
            System.out.println("  ID: " + notification.getNotificationId());
            System.out.println("  Message: " + notification.getMessage());
            System.out.println("  Recipient: " + notification.getRecipient());
        }

        /**
         * Listener with header extraction
         */
        @JmsListener(destination = HEADER_QUEUE)
        public void handleMessageWithHeaders(
                @Payload String payload,
                @Header("userId") String userId,
                @Header("department") String department,
                @Header(value = "priority", required = false) Integer priority) {
            
            System.out.println("\n[Header Listener] Processing message:");
            System.out.println("  Payload: " + payload);
            System.out.println("  User ID: " + userId);
            System.out.println("  Department: " + department);
            System.out.println("  Priority: " + priority);
        }

        /**
         * Listener with message selector for high priority only
         */
        @JmsListener(destination = SELECTOR_QUEUE, selector = "priority = 'high'")
        public void handleHighPriorityMessage(String message) {
            System.out.println("\n[High Priority Listener] Received: " + message);
        }

        /**
         * Listener receiving raw JMS Message
         */
        @JmsListener(destination = TEXT_QUEUE)
        public void handleRawMessage(Message message) {
            try {
                if (message instanceof TextMessage) {
                    TextMessage textMsg = (TextMessage) message;
                    System.out.println("\n[Raw Message Listener]");
                    System.out.println("  Text: " + textMsg.getText());
                    System.out.println("  Message ID: " + message.getJMSMessageID());
                    System.out.println("  Timestamp: " + message.getJMSTimestamp());
                    System.out.println("  Priority: " + message.getJMSPriority());
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        /**
         * Concurrent listener with multiple consumers
         */
        @JmsListener(destination = TEXT_QUEUE, concurrency = "3-5")
        public void handleConcurrentMessage(String message) {
            String threadName = Thread.currentThread().getName();
            System.out.println("[Concurrent Listener] Thread " + threadName + ": " + message);
        }
    }

    /**
     * Notification message class
     */
    static class NotificationMessage implements java.io.Serializable {
        private String notificationId;
        private String message;
        private String recipient;

        public NotificationMessage() {}

        public NotificationMessage(String notificationId, String message, String recipient) {
            this.notificationId = notificationId;
            this.message = message;
            this.recipient = recipient;
        }

        public String getNotificationId() { return notificationId; }
        public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
    }
}
