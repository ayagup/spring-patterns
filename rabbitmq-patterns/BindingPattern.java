package com.example.rabbitmq.patterns;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import java.util.Map;
import java.util.HashMap;

/**
 * Binding Pattern
 * 
 * Demonstrates various types of bindings between exchanges and queues.
 * Bindings define the routing rules that determine how messages flow
 * from exchanges to queues.
 * 
 * Key Features:
 * - Direct exchange bindings with routing keys
 * - Topic exchange bindings with patterns
 * - Fanout exchange bindings
 * - Headers exchange bindings with match criteria
 * - Multiple bindings to same queue
 * - Queue-to-queue bindings
 * - Exchange-to-exchange bindings
 */
@SpringBootApplication
public class BindingPattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static void main(String[] args) {
        SpringApplication.run(BindingPattern.class, args);
    }

    // Exchanges
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("binding-direct-exchange");
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("binding-topic-exchange");
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("binding-fanout-exchange");
    }

    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange("binding-headers-exchange");
    }

    @Bean
    public DirectExchange sourceExchange() {
        return new DirectExchange("binding-source-exchange");
    }

    @Bean
    public DirectExchange destExchange() {
        return new DirectExchange("binding-dest-exchange");
    }

    // Queues
    @Bean
    public Queue directQueue() {
        return new Queue("binding-direct-queue");
    }

    @Bean
    public Queue topicQueue1() {
        return new Queue("binding-topic-queue-1");
    }

    @Bean
    public Queue topicQueue2() {
        return new Queue("binding-topic-queue-2");
    }

    @Bean
    public Queue fanoutQueue() {
        return new Queue("binding-fanout-queue");
    }

    @Bean
    public Queue headersQueue() {
        return new Queue("binding-headers-queue");
    }

    @Bean
    public Queue multiBindQueue() {
        return new Queue("binding-multi-bind-queue");
    }

    @Bean
    public Queue destQueue() {
        return new Queue("binding-dest-queue");
    }

    /**
     * Simple direct binding with routing key
     */
    @Bean
    public Binding directBinding() {
        return BindingBuilder
                .bind(directQueue())
                .to(directExchange())
                .with("direct.key");
    }

    /**
     * Topic bindings with wildcard patterns
     */
    @Bean
    public Binding topicBinding1() {
        // * matches exactly one word
        return BindingBuilder
                .bind(topicQueue1())
                .to(topicExchange())
                .with("order.*.created");
    }

    @Bean
    public Binding topicBinding2() {
        // # matches zero or more words
        return BindingBuilder
                .bind(topicQueue2())
                .to(topicExchange())
                .with("order.#");
    }

    /**
     * Fanout binding - no routing key needed
     */
    @Bean
    public Binding fanoutBinding() {
        return BindingBuilder
                .bind(fanoutQueue())
                .to(fanoutExchange());
    }

    /**
     * Headers binding with "all" match
     */
    @Bean
    public Binding headersBindingAll() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("format", "pdf");
        headers.put("type", "report");
        
        return BindingBuilder
                .bind(headersQueue())
                .to(headersExchange())
                .whereAll(headers)
                .match();
    }

    /**
     * Headers binding with "any" match
     */
    @Bean
    public Binding headersBindingAny() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("priority", "high");
        headers.put("urgent", "true");
        
        return BindingBuilder
                .bind(headersQueue())
                .to(headersExchange())
                .whereAny(headers)
                .match();
    }

    /**
     * Multiple bindings to same queue with different routing keys
     */
    @Bean
    public Binding multiBinding1() {
        return BindingBuilder
                .bind(multiBindQueue())
                .to(directExchange())
                .with("key1");
    }

    @Bean
    public Binding multiBinding2() {
        return BindingBuilder
                .bind(multiBindQueue())
                .to(directExchange())
                .with("key2");
    }

    @Bean
    public Binding multiBinding3() {
        return BindingBuilder
                .bind(multiBindQueue())
                .to(directExchange())
                .with("key3");
    }

    /**
     * Exchange-to-exchange binding
     */
    @Bean
    public Binding exchangeBinding() {
        return BindingBuilder
                .bind(destExchange())
                .to(sourceExchange())
                .with("source.key");
    }

    @Bean
    public Binding destQueueBinding() {
        return BindingBuilder
                .bind(destQueue())
                .to(destExchange())
                .with("source.key");
    }

    /**
     * Binding with arguments
     */
    @Bean
    public Binding bindingWithArguments() {
        return BindingBuilder
                .bind(directQueue())
                .to(directExchange())
                .with("custom.key")
                .and(Map.of("x-match", "all", "custom-header", "value"));
    }

    @Override
    public void run(String... args) {
        demonstrateBindings();
    }

    private void demonstrateBindings() {
        System.out.println("=== Binding Pattern Demonstration ===\n");

        // Direct binding
        System.out.println("1. Direct Binding:");
        System.out.println("   - Exact routing key match");
        rabbitTemplate.convertAndSend("binding-direct-exchange", "direct.key", 
            "Direct message");
        System.out.println("   - Message sent: direct.key -> binding-direct-queue\n");

        // Topic bindings
        System.out.println("2. Topic Bindings:");
        System.out.println("   - Pattern matching with wildcards");
        
        rabbitTemplate.convertAndSend("binding-topic-exchange", "order.payment.created", 
            "Order payment created");
        System.out.println("   - order.payment.created -> topicQueue1 (order.*.created)");
        
        rabbitTemplate.convertAndSend("binding-topic-exchange", "order.payment.updated", 
            "Order payment updated");
        System.out.println("   - order.payment.updated -> topicQueue2 (order.#)");
        
        rabbitTemplate.convertAndSend("binding-topic-exchange", "order.shipping.tracking.updated", 
            "Tracking updated");
        System.out.println("   - order.shipping.tracking.updated -> topicQueue2 (order.#)\n");

        // Fanout binding
        System.out.println("3. Fanout Binding:");
        System.out.println("   - Broadcasts to all bound queues");
        rabbitTemplate.convertAndSend("binding-fanout-exchange", "", 
            "Fanout broadcast");
        System.out.println("   - Message broadcast to all queues\n");

        // Headers binding
        System.out.println("4. Headers Binding:");
        System.out.println("   - Routes based on message headers");
        
        rabbitTemplate.convertAndSend("binding-headers-exchange", "", 
            "PDF Report", message -> {
                message.getMessageProperties().setHeader("format", "pdf");
                message.getMessageProperties().setHeader("type", "report");
                return message;
            });
        System.out.println("   - Headers match (all): format=pdf, type=report\n");

        // Multiple bindings
        System.out.println("5. Multiple Bindings:");
        System.out.println("   - Same queue bound with different keys");
        rabbitTemplate.convertAndSend("binding-direct-exchange", "key1", "Message 1");
        rabbitTemplate.convertAndSend("binding-direct-exchange", "key2", "Message 2");
        rabbitTemplate.convertAndSend("binding-direct-exchange", "key3", "Message 3");
        System.out.println("   - All messages routed to multi-bind-queue\n");

        // Exchange-to-exchange binding
        System.out.println("6. Exchange-to-Exchange Binding:");
        System.out.println("   - Messages flow through multiple exchanges");
        rabbitTemplate.convertAndSend("binding-source-exchange", "source.key", 
            "Chained message");
        System.out.println("   - source-exchange -> dest-exchange -> dest-queue\n");

        // Binding properties
        System.out.println("7. Binding Properties:");
        System.out.println("   - Routing key: Determines message routing");
        System.out.println("   - Arguments: Additional binding configuration");
        System.out.println("   - Destination: Queue or exchange");
        System.out.println("   - Exchange: Source exchange");
    }
}
