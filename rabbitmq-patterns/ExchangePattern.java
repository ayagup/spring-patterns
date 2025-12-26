package com.example.rabbitmq.patterns;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

/**
 * Exchange Pattern
 * 
 * Demonstrates the configuration and use of different types of exchanges
 * in RabbitMQ. Exchanges are routing mechanisms that determine how messages
 * are distributed to queues.
 * 
 * Key Features:
 * - Direct exchange configuration
 * - Topic exchange configuration
 * - Fanout exchange configuration
 * - Headers exchange configuration
 * - Exchange properties (durable, auto-delete)
 * - Exchange arguments
 */
@SpringBootApplication
public class ExchangePattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String DIRECT_EXCHANGE = "exchange-direct";
    private static final String TOPIC_EXCHANGE = "exchange-topic";
    private static final String FANOUT_EXCHANGE = "exchange-fanout";
    private static final String HEADERS_EXCHANGE = "exchange-headers";

    public static void main(String[] args) {
        SpringApplication.run(ExchangePattern.class, args);
    }

    /**
     * Direct Exchange - Routes messages based on exact routing key match
     */
    @Bean
    public DirectExchange directExchange() {
        return ExchangeBuilder
                .directExchange(DIRECT_EXCHANGE)
                .durable(true)
                .autoDelete(false)
                .build();
    }

    /**
     * Topic Exchange - Routes messages based on routing pattern matching
     */
    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder
                .topicExchange(TOPIC_EXCHANGE)
                .durable(true)
                .autoDelete(false)
                .build();
    }

    /**
     * Fanout Exchange - Routes messages to all bound queues
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return ExchangeBuilder
                .fanoutExchange(FANOUT_EXCHANGE)
                .durable(true)
                .autoDelete(false)
                .build();
    }

    /**
     * Headers Exchange - Routes messages based on header attributes
     */
    @Bean
    public HeadersExchange headersExchange() {
        return ExchangeBuilder
                .headersExchange(HEADERS_EXCHANGE)
                .durable(true)
                .autoDelete(false)
                .build();
    }

    /**
     * Custom exchange with arguments
     */
    @Bean
    public DirectExchange customExchange() {
        return ExchangeBuilder
                .directExchange("exchange-custom")
                .durable(true)
                .autoDelete(false)
                .withArgument("x-message-ttl", 60000)
                .withArgument("alternate-exchange", "exchange-alternate")
                .build();
    }

    /**
     * Alternate exchange for unroutable messages
     */
    @Bean
    public FanoutExchange alternateExchange() {
        return ExchangeBuilder
                .fanoutExchange("exchange-alternate")
                .durable(true)
                .build();
    }

    /**
     * Queues for demonstration
     */
    @Bean
    public Queue directQueue() {
        return new Queue("exchange-direct-queue", false);
    }

    @Bean
    public Queue topicQueue() {
        return new Queue("exchange-topic-queue", false);
    }

    @Bean
    public Queue fanoutQueue1() {
        return new Queue("exchange-fanout-queue-1", false);
    }

    @Bean
    public Queue fanoutQueue2() {
        return new Queue("exchange-fanout-queue-2", false);
    }

    @Bean
    public Queue headersQueue() {
        return new Queue("exchange-headers-queue", false);
    }

    @Bean
    public Queue alternateQueue() {
        return new Queue("exchange-alternate-queue", false);
    }

    /**
     * Bindings
     */
    @Bean
    public Binding directBinding() {
        return BindingBuilder
                .bind(directQueue())
                .to(directExchange())
                .with("direct.key");
    }

    @Bean
    public Binding topicBinding() {
        return BindingBuilder
                .bind(topicQueue())
                .to(topicExchange())
                .with("topic.*.message");
    }

    @Bean
    public Binding fanoutBinding1() {
        return BindingBuilder
                .bind(fanoutQueue1())
                .to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder
                .bind(fanoutQueue2())
                .to(fanoutExchange());
    }

    @Bean
    public Binding headersBinding() {
        return BindingBuilder
                .bind(headersQueue())
                .to(headersExchange())
                .whereAll(java.util.Map.of("format", "pdf", "type", "report"))
                .match();
    }

    @Bean
    public Binding alternateBinding() {
        return BindingBuilder
                .bind(alternateQueue())
                .to(alternateExchange());
    }

    @Override
    public void run(String... args) {
        demonstrateExchangeTypes();
    }

    private void demonstrateExchangeTypes() {
        System.out.println("=== Exchange Pattern Demonstration ===\n");

        // Direct Exchange
        System.out.println("1. Direct Exchange:");
        System.out.println("   - Routing based on exact key match");
        System.out.println("   - Exchange: " + DIRECT_EXCHANGE);
        rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, "direct.key", "Direct message");
        System.out.println("   - Message sent with routing key: direct.key\n");

        // Topic Exchange
        System.out.println("2. Topic Exchange:");
        System.out.println("   - Routing based on pattern matching");
        System.out.println("   - Exchange: " + TOPIC_EXCHANGE);
        rabbitTemplate.convertAndSend(TOPIC_EXCHANGE, "topic.important.message", "Topic message");
        System.out.println("   - Message sent with routing key: topic.important.message");
        System.out.println("   - Pattern: topic.*.message\n");

        // Fanout Exchange
        System.out.println("3. Fanout Exchange:");
        System.out.println("   - Broadcasts to all bound queues");
        System.out.println("   - Exchange: " + FANOUT_EXCHANGE);
        rabbitTemplate.convertAndSend(FANOUT_EXCHANGE, "", "Fanout broadcast");
        System.out.println("   - Message broadcast to all queues\n");

        // Headers Exchange
        System.out.println("4. Headers Exchange:");
        System.out.println("   - Routing based on message headers");
        System.out.println("   - Exchange: " + HEADERS_EXCHANGE);
        rabbitTemplate.convertAndSend(HEADERS_EXCHANGE, "", "Headers message", message -> {
            MessageProperties props = message.getMessageProperties();
            props.setHeader("format", "pdf");
            props.setHeader("type", "report");
            return message;
        });
        System.out.println("   - Message sent with headers: format=pdf, type=report\n");

        // Exchange Properties
        System.out.println("5. Exchange Properties:");
        System.out.println("   - Durable: Survives broker restart");
        System.out.println("   - Auto-delete: Deleted when no queues bound");
        System.out.println("   - Arguments: Custom exchange behavior");
        System.out.println("   - Alternate exchange: Routes unroutable messages");
    }
}
