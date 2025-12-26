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
 * Topic Exchange Pattern
 * 
 * Demonstrates the Topic Exchange routing mechanism where messages are
 * routed based on pattern matching. Uses wildcards (* and #) for flexible
 * message routing to multiple queues.
 * 
 * Key Features:
 * - Pattern-based routing
 * - Wildcard support (* matches one word, # matches zero or more words)
 * - Multi-queue routing
 * - Hierarchical routing keys
 * - Flexible message distribution
 */
@SpringBootApplication
public class TopicExchangePattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String TOPIC_EXCHANGE = "topic-exchange-demo";

    public static void main(String[] args) {
        SpringApplication.run(TopicExchangePattern.class, args);
    }

    /**
     * Topic exchange configuration
     */
    @Bean
    public TopicExchange logExchange() {
        return ExchangeBuilder
                .topicExchange(TOPIC_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Queues for different log levels and services
     */
    @Bean
    public Queue allLogsQueue() {
        return new Queue("topic-all-logs", true);
    }

    @Bean
    public Queue errorLogsQueue() {
        return new Queue("topic-error-logs", true);
    }

    @Bean
    public Queue authServiceLogsQueue() {
        return new Queue("topic-auth-service-logs", true);
    }

    @Bean
    public Queue criticalLogsQueue() {
        return new Queue("topic-critical-logs", true);
    }

    /**
     * Topic bindings with wildcard patterns
     */
    @Bean
    public Binding allLogsBinding() {
        // # matches zero or more words
        return BindingBuilder
                .bind(allLogsQueue())
                .to(logExchange())
                .with("log.#");
    }

    @Bean
    public Binding errorLogsBinding() {
        // * matches exactly one word
        return BindingBuilder
                .bind(errorLogsQueue())
                .to(logExchange())
                .with("log.*.error");
    }

    @Bean
    public Binding authServiceBinding() {
        return BindingBuilder
                .bind(authServiceLogsQueue())
                .to(logExchange())
                .with("log.auth.*");
    }

    @Bean
    public Binding criticalLogsBinding() {
        // Multiple wildcards
        return BindingBuilder
                .bind(criticalLogsQueue())
                .to(logExchange())
                .with("log.*.critical");
    }

    @Override
    public void run(String... args) throws InterruptedException {
        demonstrateTopicExchange();
        Thread.sleep(2000);
    }

    private void demonstrateTopicExchange() {
        System.out.println("=== Topic Exchange Pattern ===\n");

        System.out.println("Binding Patterns:");
        System.out.println("- log.# -> all-logs (matches everything starting with 'log.')");
        System.out.println("- log.*.error -> error-logs (matches any service error)");
        System.out.println("- log.auth.* -> auth-service-logs (matches all auth service logs)");
        System.out.println("- log.*.critical -> critical-logs (matches any critical log)\n");

        // Test 1: Auth service info log
        System.out.println("1. Sending: log.auth.info");
        sendLog("log.auth.info", "User logged in successfully");
        System.out.println("   Routes to: all-logs, auth-service-logs\n");

        // Test 2: Payment service error
        System.out.println("2. Sending: log.payment.error");
        sendLog("log.payment.error", "Payment processing failed");
        System.out.println("   Routes to: all-logs, error-logs\n");

        // Test 3: Auth service critical
        System.out.println("3. Sending: log.auth.critical");
        sendLog("log.auth.critical", "Security breach detected");
        System.out.println("   Routes to: all-logs, auth-service-logs, critical-logs\n");

        // Test 4: Multiple level routing
        System.out.println("4. Sending: log.order.service.error");
        sendLog("log.order.service.error", "Order service database connection lost");
        System.out.println("   Routes to: all-logs only (# matches multiple words)\n");

        // Test 5: Warning level
        System.out.println("5. Sending: log.notification.warning");
        sendLog("log.notification.warning", "Email queue is getting full");
        System.out.println("   Routes to: all-logs only\n");

        System.out.println("Topic Exchange Characteristics:");
        System.out.println("- Pattern-based routing with wildcards");
        System.out.println("- * matches exactly one word");
        System.out.println("- # matches zero or more words");
        System.out.println("- Messages can route to multiple queues");
        System.out.println("- Ideal for publish-subscribe with filtering");
    }

    private void sendLog(String routingKey, String message) {
        LogMessage log = new LogMessage(
            routingKey,
            message,
            System.currentTimeMillis()
        );
        rabbitTemplate.convertAndSend(TOPIC_EXCHANGE, routingKey, log);
    }

    /**
     * Message listeners for different log queues
     */
    @Component
    static class LogListeners {

        @RabbitListener(queues = "topic-all-logs")
        public void handleAllLogs(LogMessage log) {
            System.out.println("  [All Logs] " + log.getRoutingKey() + ": " + log.getMessage());
        }

        @RabbitListener(queues = "topic-error-logs")
        public void handleErrorLogs(LogMessage log) {
            System.out.println("  [Error Logs] " + log.getRoutingKey() + ": " + log.getMessage());
        }

        @RabbitListener(queues = "topic-auth-service-logs")
        public void handleAuthLogs(LogMessage log) {
            System.out.println("  [Auth Service] " + log.getRoutingKey() + ": " + log.getMessage());
        }

        @RabbitListener(queues = "topic-critical-logs")
        public void handleCriticalLogs(LogMessage log) {
            System.out.println("  [Critical Logs] " + log.getRoutingKey() + ": " + log.getMessage());
        }
    }

    /**
     * Log message class
     */
    static class LogMessage implements java.io.Serializable {
        private String routingKey;
        private String message;
        private long timestamp;

        public LogMessage() {}

        public LogMessage(String routingKey, String message, long timestamp) {
            this.routingKey = routingKey;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getRoutingKey() { return routingKey; }
        public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
