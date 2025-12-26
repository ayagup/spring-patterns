package com.example.amqp.patterns;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

/**
 * AMQP Template Pattern
 * 
 * Demonstrates the use of AmqpTemplate and RabbitTemplate for AMQP
 * messaging operations. Provides high-level abstraction for sending
 * and receiving messages using the AMQP protocol.
 * 
 * Key Features:
 * - Synchronous send and receive operations
 * - Message conversion
 * - Default exchange and routing key configuration
 * - Mandatory and immediate flags
 * - Return callbacks
 * - Confirm callbacks
 */
@SpringBootApplication
public class AMQPTemplatePattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String QUEUE_NAME = "amqp-template-queue";
    private static final String EXCHANGE_NAME = "amqp-template-exchange";

    public static void main(String[] args) {
        SpringApplication.run(AMQPTemplatePattern.class, args);
    }

    @Bean
    public Queue amqpQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Override
    public void run(String... args) {
        demonstrateBasicOperations();
        demonstrateMessageConversion();
        demonstrateMessageProperties();
        demonstrateReceiveOperations();
    }

    /**
     * Basic send and receive operations
     */
    private void demonstrateBasicOperations() {
        System.out.println("=== AMQP Template Pattern ===\n");
        System.out.println("1. Basic Send and Receive:");

        // Send raw message
        MessageProperties props = new MessageProperties();
        props.setContentType("text/plain");
        Message message = new Message("Hello AMQP".getBytes(), props);
        
        rabbitTemplate.send(QUEUE_NAME, message);
        System.out.println("   - Message sent: Hello AMQP");

        // Receive raw message
        Message received = rabbitTemplate.receive(QUEUE_NAME, 5000);
        if (received != null) {
            String content = new String(received.getBody());
            System.out.println("   - Message received: " + content);
            System.out.println("   - Content type: " + received.getMessageProperties().getContentType());
        }

        System.out.println();
    }

    /**
     * Automatic message conversion
     */
    private void demonstrateMessageConversion() {
        System.out.println("2. Message Conversion:");

        // ConvertAndSend - automatic conversion
        ProductMessage product = new ProductMessage("PROD-001", "Laptop", 1299.99);
        rabbitTemplate.convertAndSend(QUEUE_NAME, product);
        System.out.println("   - Product sent: " + product);

        // ReceiveAndConvert - automatic conversion
        ProductMessage receivedProduct = (ProductMessage) rabbitTemplate.receiveAndConvert(QUEUE_NAME, 5000);
        if (receivedProduct != null) {
            System.out.println("   - Product received: " + receivedProduct);
        }

        System.out.println();
    }

    /**
     * Working with message properties
     */
    private void demonstrateMessageProperties() {
        System.out.println("3. Message Properties:");

        // Send with custom properties
        rabbitTemplate.convertAndSend(QUEUE_NAME, "Message with properties", message -> {
            MessageProperties props = message.getMessageProperties();
            props.setHeader("userId", "user-123");
            props.setHeader("operation", "create");
            props.setPriority(5);
            props.setExpiration("10000"); // 10 seconds
            props.setCorrelationId("correlation-456");
            return message;
        });

        System.out.println("   - Message sent with properties");

        // Receive and inspect properties
        Message received = rabbitTemplate.receive(QUEUE_NAME, 5000);
        if (received != null) {
            MessageProperties props = received.getMessageProperties();
            System.out.println("   - Message body: " + new String(received.getBody()));
            System.out.println("   - User ID: " + props.getHeader("userId"));
            System.out.println("   - Operation: " + props.getHeader("operation"));
            System.out.println("   - Priority: " + props.getPriority());
            System.out.println("   - Correlation ID: " + props.getCorrelationId());
        }

        System.out.println();
    }

    /**
     * Different receive operations
     */
    private void demonstrateReceiveOperations() {
        System.out.println("4. Receive Operations:");

        // Send test messages
        for (int i = 1; i <= 3; i++) {
            rabbitTemplate.convertAndSend(QUEUE_NAME, "Test message #" + i);
        }

        // Receive with timeout
        System.out.println("   - Receive with 5 second timeout:");
        String msg1 = (String) rabbitTemplate.receiveAndConvert(QUEUE_NAME, 5000);
        System.out.println("     Received: " + msg1);

        // Receive immediately (no wait)
        System.out.println("   - Receive immediately (no wait):");
        String msg2 = (String) rabbitTemplate.receiveAndConvert(QUEUE_NAME);
        System.out.println("     Received: " + msg2);

        // Receive from specific queue
        System.out.println("   - Receive from specific queue:");
        String msg3 = (String) rabbitTemplate.receiveAndConvert(QUEUE_NAME);
        System.out.println("     Received: " + msg3);

        System.out.println("\n5. AMQP Template Features:");
        System.out.println("   - High-level AMQP messaging API");
        System.out.println("   - Automatic message conversion");
        System.out.println("   - Connection and channel management");
        System.out.println("   - Exception translation");
        System.out.println("   - Template method pattern");
        System.out.println("   - Publisher confirms and returns support");
    }

    /**
     * Product message class
     */
    static class ProductMessage implements java.io.Serializable {
        private String productId;
        private String name;
        private double price;

        public ProductMessage() {}

        public ProductMessage(String productId, String name, double price) {
            this.productId = productId;
            this.name = name;
            this.price = price;
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        @Override
        public String toString() {
            return "ProductMessage{productId='" + productId + "', name='" + name + 
                   "', price=" + price + "}";
        }
    }
}
