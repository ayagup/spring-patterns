package com.example.jms.patterns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.*;
import javax.jms.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Message Converter Pattern
 * 
 * Demonstrates various message conversion strategies for transforming
 * Java objects to/from JMS messages. Enables automatic serialization
 * and deserialization of complex objects.
 * 
 * Key Features:
 * - Simple message converter (default)
 * - Mapping Jackson 2 message converter (JSON)
 * - Marshalling message converter (XML)
 * - Custom message converter implementation
 * - Type mapping and discrimination
 */
@SpringBootApplication
public class MessageConverterPattern implements CommandLineRunner {

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final String SIMPLE_QUEUE = "converter-simple-queue";
    private static final String JSON_QUEUE = "converter-json-queue";
    private static final String CUSTOM_QUEUE = "converter-custom-queue";

    public static void main(String[] args) {
        SpringApplication.run(MessageConverterPattern.class, args);
    }

    /**
     * Simple message converter - uses Java serialization
     */
    @Bean
    public MessageConverter simpleMessageConverter() {
        return new SimpleMessageConverter();
    }

    /**
     * JSON message converter using Jackson
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        
        // Configure type mappings
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("order", OrderData.class);
        typeIdMappings.put("customer", CustomerData.class);
        converter.setTypeIdMappings(typeIdMappings);
        
        return converter;
    }

    /**
     * Custom message converter
     */
    @Bean
    public MessageConverter customMessageConverter() {
        return new CustomCsvMessageConverter();
    }

    /**
     * JmsTemplate with JSON converter
     */
    @Bean
    public JmsTemplate jsonJmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * JmsTemplate with custom converter
     */
    @Bean
    public JmsTemplate customJmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(customMessageConverter());
        return template;
    }

    @Override
    public void run(String... args) {
        demonstrateMessageConverters();
    }

    private void demonstrateMessageConverters() {
        System.out.println("=== Message Converter Pattern ===\n");

        // Test 1: Simple message converter (Java Serialization)
        System.out.println("1. Simple Message Converter (Java Serialization):");
        jmsTemplate.setMessageConverter(simpleMessageConverter());
        
        OrderData order1 = new OrderData("ORD-001", 299.99, "Laptop");
        jmsTemplate.convertAndSend(SIMPLE_QUEUE, order1);
        System.out.println("   - Sent: " + order1);
        
        OrderData received1 = (OrderData) jmsTemplate.receiveAndConvert(SIMPLE_QUEUE);
        System.out.println("   - Received: " + received1 + "\n");

        // Test 2: JSON message converter
        System.out.println("2. JSON Message Converter:");
        JmsTemplate jsonTemplate = jsonJmsTemplate(null);
        
        OrderData order2 = new OrderData("ORD-002", 1499.99, "Smartphone");
        jsonTemplate.convertAndSend(JSON_QUEUE, order2);
        System.out.println("   - Sent as JSON: " + order2);
        
        // Inspect the actual JSON message
        jmsTemplate.setMessageConverter(null);
        Message msg = jmsTemplate.receive(JSON_QUEUE);
        if (msg instanceof TextMessage) {
            try {
                System.out.println("   - JSON payload: " + ((TextMessage) msg).getText());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        // Send again and receive with converter
        jsonTemplate.convertAndSend(JSON_QUEUE, order2);
        OrderData received2 = (OrderData) jsonTemplate.receiveAndConvert(JSON_QUEUE);
        System.out.println("   - Received from JSON: " + received2 + "\n");

        // Test 3: Custom CSV converter
        System.out.println("3. Custom CSV Message Converter:");
        JmsTemplate customTemplate = customJmsTemplate(null);
        
        CustomerData customer = new CustomerData("CUST-001", "John Doe", "john@example.com");
        customTemplate.convertAndSend(CUSTOM_QUEUE, customer);
        System.out.println("   - Sent: " + customer);
        
        // Inspect CSV format
        jmsTemplate.setMessageConverter(null);
        Message csvMsg = jmsTemplate.receive(CUSTOM_QUEUE);
        if (csvMsg instanceof TextMessage) {
            try {
                System.out.println("   - CSV payload: " + ((TextMessage) csvMsg).getText());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        // Send again and receive with converter
        customTemplate.convertAndSend(CUSTOM_QUEUE, customer);
        CustomerData received3 = (CustomerData) customTemplate.receiveAndConvert(CUSTOM_QUEUE);
        System.out.println("   - Received from CSV: " + received3 + "\n");

        System.out.println("Message Converter Benefits:");
        System.out.println("- Automatic object serialization/deserialization");
        System.out.println("- Support for multiple formats (JSON, XML, custom)");
        System.out.println("- Type-safe message handling");
        System.out.println("- Decouples message format from business logic");
    }

    /**
     * Custom CSV message converter
     */
    static class CustomCsvMessageConverter implements MessageConverter {

        @Override
        public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
            if (object instanceof CustomerData) {
                CustomerData customer = (CustomerData) object;
                String csv = customer.getCustomerId() + "," + 
                           customer.getName() + "," + 
                           customer.getEmail();
                return session.createTextMessage(csv);
            }
            throw new MessageConversionException("Cannot convert " + object.getClass());
        }

        @Override
        public Object fromMessage(Message message) throws JMSException, MessageConversionException {
            if (message instanceof TextMessage) {
                String csv = ((TextMessage) message).getText();
                String[] parts = csv.split(",");
                if (parts.length == 3) {
                    return new CustomerData(parts[0].trim(), 
                                          parts[1].trim(), 
                                          parts[2].trim());
                }
            }
            throw new MessageConversionException("Cannot convert message");
        }
    }

    /**
     * Order data class
     */
    static class OrderData implements java.io.Serializable {
        private String orderId;
        private double amount;
        private String product;

        public OrderData() {}

        public OrderData(String orderId, double amount, String product) {
            this.orderId = orderId;
            this.amount = amount;
            this.product = product;
        }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getProduct() { return product; }
        public void setProduct(String product) { this.product = product; }

        @Override
        public String toString() {
            return "OrderData{orderId='" + orderId + "', amount=" + amount + 
                   ", product='" + product + "'}";
        }
    }

    /**
     * Customer data class
     */
    static class CustomerData implements java.io.Serializable {
        private String customerId;
        private String name;
        private String email;

        public CustomerData() {}

        public CustomerData(String customerId, String name, String email) {
            this.customerId = customerId;
            this.name = name;
            this.email = email;
        }

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public String toString() {
            return "CustomerData{customerId='" + customerId + "', name='" + name + 
                   "', email='" + email + "'}";
        }
    }
}
