package com.example.jms.patterns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * JMS Template Pattern
 * 
 * Demonstrates the use of JmsTemplate for synchronous JMS operations.
 * JmsTemplate is the central class in Spring's JMS support, providing
 * a simplified API for sending and receiving messages.
 * 
 * Key Features:
 * - Synchronous send and receive operations
 * - Message conversion
 * - Default destination configuration
 * - Session and connection management
 * - Exception translation
 * - Request-reply support
 */
@SpringBootApplication
public class JMSTemplatePattern implements CommandLineRunner {

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final String QUEUE_NAME = "jms-template-queue";
    private static final String TOPIC_NAME = "jms-template-topic";

    public static void main(String[] args) {
        SpringApplication.run(JMSTemplatePattern.class, args);
    }

    @Override
    public void run(String... args) {
        demonstrateSendAndReceive();
        demonstrateConvertAndSend();
        demonstrateMessageCreator();
        demonstrateMessageProperties();
        demonstrateRequestReply();
    }

    /**
     * Basic send and receive with text messages
     */
    private void demonstrateSendAndReceive() {
        System.out.println("=== Basic Send and Receive ===\n");

        // Send text message
        jmsTemplate.send(QUEUE_NAME, session -> 
            session.createTextMessage("Hello JMS Template"));
        System.out.println("Message sent to: " + QUEUE_NAME);

        // Receive message
        String received = (String) jmsTemplate.receiveAndConvert(QUEUE_NAME);
        System.out.println("Message received: " + received + "\n");
    }

    /**
     * Convert and send with automatic message conversion
     */
    private void demonstrateConvertAndSend() {
        System.out.println("=== Convert and Send ===\n");

        // Send object with automatic conversion
        OrderMessage order = new OrderMessage("ORD-001", 499.99, "john@example.com");
        jmsTemplate.convertAndSend(QUEUE_NAME, order);
        System.out.println("Order sent: " + order);

        // Receive with automatic conversion
        OrderMessage receivedOrder = (OrderMessage) jmsTemplate.receiveAndConvert(QUEUE_NAME);
        System.out.println("Order received: " + receivedOrder + "\n");
    }

    /**
     * Using MessageCreator for custom message creation
     */
    private void demonstrateMessageCreator() {
        System.out.println("=== Message Creator ===\n");

        // Create custom message with properties
        jmsTemplate.send(QUEUE_NAME, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage("Custom message");
                message.setStringProperty("sender", "system");
                message.setIntProperty("priority", 5);
                message.setLongProperty("timestamp", System.currentTimeMillis());
                return message;
            }
        });
        System.out.println("Custom message sent with properties\n");

        // Receive and inspect message
        Message received = jmsTemplate.receive(QUEUE_NAME);
        if (received instanceof TextMessage) {
            try {
                TextMessage textMsg = (TextMessage) received;
                System.out.println("Text: " + textMsg.getText());
                System.out.println("Sender: " + textMsg.getStringProperty("sender"));
                System.out.println("Priority: " + textMsg.getIntProperty("priority"));
                System.out.println("Timestamp: " + textMsg.getLongProperty("timestamp") + "\n");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Working with message properties and headers
     */
    private void demonstrateMessageProperties() {
        System.out.println("=== Message Properties ===\n");

        // Send message with properties using post processor
        jmsTemplate.convertAndSend(QUEUE_NAME, "Message with properties", message -> {
            message.setStringProperty("type", "notification");
            message.setStringProperty("source", "payment-service");
            message.setIntProperty("retryCount", 0);
            message.setJMSPriority(9);
            return message;
        });
        System.out.println("Message with properties sent");

        // Receive and display properties
        jmsTemplate.setReceiveTimeout(5000);
        Message received = jmsTemplate.receive(QUEUE_NAME);
        if (received != null) {
            try {
                System.out.println("Message ID: " + received.getJMSMessageID());
                System.out.println("Priority: " + received.getJMSPriority());
                System.out.println("Type: " + received.getStringProperty("type"));
                System.out.println("Source: " + received.getStringProperty("source"));
                System.out.println("Retry Count: " + received.getIntProperty("retryCount") + "\n");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Request-reply pattern using JmsTemplate
     */
    private void demonstrateRequestReply() {
        System.out.println("=== Request-Reply Pattern ===\n");

        // Set up reply destination
        jmsTemplate.setDefaultDestination(null);
        
        // Send request and receive reply
        String request = "Calculate: 10 + 20";
        System.out.println("Sending request: " + request);
        
        // Simulate request-reply
        jmsTemplate.convertAndSend(QUEUE_NAME, request);
        
        // In real scenario, another service would process and reply
        // Simulating immediate reply for demonstration
        jmsTemplate.convertAndSend(QUEUE_NAME, "Result: 30");
        
        String reply = (String) jmsTemplate.receiveAndConvert(QUEUE_NAME);
        System.out.println("Received reply: " + reply + "\n");

        System.out.println("JmsTemplate Features:");
        System.out.println("- Automatic connection and session management");
        System.out.println("- Message conversion support");
        System.out.println("- Exception translation to Spring's DataAccessException");
        System.out.println("- Configurable timeouts and QoS parameters");
        System.out.println("- Support for both queues and topics");
    }

    /**
     * Order message class for serialization
     */
    static class OrderMessage implements java.io.Serializable {
        private String orderId;
        private double amount;
        private String customerEmail;

        public OrderMessage() {}

        public OrderMessage(String orderId, double amount, String customerEmail) {
            this.orderId = orderId;
            this.amount = amount;
            this.customerEmail = customerEmail;
        }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

        @Override
        public String toString() {
            return "OrderMessage{orderId='" + orderId + "', amount=" + amount + 
                   ", customerEmail='" + customerEmail + "'}";
        }
    }
}
