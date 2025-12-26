package com.example.rabbitmq.patterns;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Direct Exchange Pattern
 * 
 * Demonstrates the Direct Exchange routing mechanism where messages are
 * routed to queues based on exact routing key matches. This is the default
 * exchange type and is ideal for simple point-to-point messaging.
 * 
 * Key Features:
 * - Exact routing key matching
 * - One-to-one queue routing
 * - Multiple queues with different keys
 * - Default exchange behavior
 * - Routing key validation
 */
@SpringBootApplication
public class DirectExchangePattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String DIRECT_EXCHANGE = "direct-exchange-demo";

    public static void main(String[] args) {
        SpringApplication.run(DirectExchangePattern.class, args);
    }

    /**
     * Direct exchange configuration
     */
    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder
                .directExchange(DIRECT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Queues for different order operations
     */
    @Bean
    public Queue orderCreationQueue() {
        return new Queue("direct-order-creation", true);
    }

    @Bean
    public Queue orderPaymentQueue() {
        return new Queue("direct-order-payment", true);
    }

    @Bean
    public Queue orderShippingQueue() {
        return new Queue("direct-order-shipping", true);
    }

    @Bean
    public Queue orderCancellationQueue() {
        return new Queue("direct-order-cancellation", true);
    }

    /**
     * Bindings with specific routing keys
     */
    @Bean
    public Binding orderCreationBinding() {
        return BindingBuilder
                .bind(orderCreationQueue())
                .to(orderExchange())
                .with("order.create");
    }

    @Bean
    public Binding orderPaymentBinding() {
        return BindingBuilder
                .bind(orderPaymentQueue())
                .to(orderExchange())
                .with("order.payment");
    }

    @Bean
    public Binding orderShippingBinding() {
        return BindingBuilder
                .bind(orderShippingQueue())
                .to(orderExchange())
                .with("order.shipping");
    }

    @Bean
    public Binding orderCancellationBinding() {
        return BindingBuilder
                .bind(orderCancellationQueue())
                .to(orderExchange())
                .with("order.cancel");
    }

    @Override
    public void run(String... args) throws InterruptedException {
        demonstrateDirectExchange();
        Thread.sleep(2000);
    }

    private void demonstrateDirectExchange() {
        System.out.println("=== Direct Exchange Pattern ===\n");

        // Send order creation message
        System.out.println("1. Creating new order:");
        OrderEvent createEvent = new OrderEvent("ORD-001", "CREATE", "New order created");
        rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, "order.create", createEvent);
        System.out.println("   - Sent to: order.create");
        System.out.println("   - Routed to: direct-order-creation queue\n");

        // Send payment message
        System.out.println("2. Processing payment:");
        OrderEvent paymentEvent = new OrderEvent("ORD-001", "PAYMENT", "Payment processed");
        rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, "order.payment", paymentEvent);
        System.out.println("   - Sent to: order.payment");
        System.out.println("   - Routed to: direct-order-payment queue\n");

        // Send shipping message
        System.out.println("3. Shipping order:");
        OrderEvent shippingEvent = new OrderEvent("ORD-001", "SHIPPING", "Order shipped");
        rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, "order.shipping", shippingEvent);
        System.out.println("   - Sent to: order.shipping");
        System.out.println("   - Routed to: direct-order-shipping queue\n");

        // Send cancellation message
        System.out.println("4. Cancelling order:");
        OrderEvent cancelEvent = new OrderEvent("ORD-002", "CANCEL", "Order cancelled");
        rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, "order.cancel", cancelEvent);
        System.out.println("   - Sent to: order.cancel");
        System.out.println("   - Routed to: direct-order-cancellation queue\n");

        // Invalid routing key - message will be lost
        System.out.println("5. Invalid routing key:");
        rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, "order.invalid", 
            new OrderEvent("ORD-003", "INVALID", "Invalid operation"));
        System.out.println("   - Sent to: order.invalid");
        System.out.println("   - No matching queue - message discarded\n");

        System.out.println("Direct Exchange Characteristics:");
        System.out.println("- Exact routing key match required");
        System.out.println("- Messages routed to one specific queue");
        System.out.println("- Simple and efficient routing");
        System.out.println("- Ideal for task distribution");
    }

    /**
     * Message listeners for each queue
     */
    @Component
    static class OrderListeners {

        @RabbitListener(queues = "direct-order-creation")
        public void handleOrderCreation(OrderEvent event) {
            System.out.println("[Creation Handler] " + event);
        }

        @RabbitListener(queues = "direct-order-payment")
        public void handleOrderPayment(OrderEvent event) {
            System.out.println("[Payment Handler] " + event);
        }

        @RabbitListener(queues = "direct-order-shipping")
        public void handleOrderShipping(OrderEvent event) {
            System.out.println("[Shipping Handler] " + event);
        }

        @RabbitListener(queues = "direct-order-cancellation")
        public void handleOrderCancellation(OrderEvent event) {
            System.out.println("[Cancellation Handler] " + event);
        }
    }

    /**
     * Order event class
     */
    static class OrderEvent implements java.io.Serializable {
        private String orderId;
        private String eventType;
        private String description;

        public OrderEvent() {}

        public OrderEvent(String orderId, String eventType, String description) {
            this.orderId = orderId;
            this.eventType = eventType;
            this.description = description;
        }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return "OrderEvent{orderId='" + orderId + "', eventType='" + eventType + 
                   "', description='" + description + "'}";
        }
    }
}
