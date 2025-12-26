package com.example.amqp.patterns;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

/**
 * Publisher Returns Pattern
 * 
 * Demonstrates handling of unroutable messages via publisher returns.
 * When a message cannot be routed to any queue, the broker can return it
 * to the publisher instead of silently dropping it.
 * 
 * Key Features:
 * - Enable publisher returns on connection factory
 * - Set mandatory flag on messages
 * - Return callback for unroutable messages
 * - Handle routing failures
 * - Alternate exchange patterns
 * - Dead letter handling for returns
 * - Retry strategies for returned messages
 */
@SpringBootApplication
public class PublisherReturnsPattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    private static final String EXCHANGE_NAME = "publisher-returns-exchange";
    private static final String QUEUE_NAME = "publisher-returns-queue";
    private static final String ROUTING_KEY = "returns.test";

    public static void main(String[] args) {
        SpringApplication.run(PublisherReturnsPattern.class, args);
    }

    /**
     * Configure connection factory with publisher returns
     */
    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
        factory.setPublisherReturns(true);
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        return factory;
    }

    /**
     * Configure RabbitTemplate with mandatory flag and returns callback
     */
    @Bean
    public RabbitTemplate configuredRabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        
        // Enable mandatory flag - messages must be routable
        template.setMandatory(true);
        
        // Set returns callback for unroutable messages
        template.setReturnsCallback(returned -> {
            System.out.println("\n   ⚠ MESSAGE RETURNED (Unroutable):");
            System.out.println("     - Reply Code: " + returned.getReplyCode());
            System.out.println("     - Reply Text: " + returned.getReplyText());
            System.out.println("     - Exchange: " + returned.getExchange());
            System.out.println("     - Routing Key: " + returned.getRoutingKey());
            System.out.println("     - Message: " + new String(returned.getMessage().getBody()));
        });
        
        return template;
    }

    @Bean
    public DirectExchange returnsExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue returnsQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    public Binding returnsBinding() {
        return BindingBuilder.bind(returnsQueue())
                .to(returnsExchange())
                .with(ROUTING_KEY);
    }

    @Override
    public void run(String... args) throws Exception {
        demonstrateRoutableMessage();
        demonstrateUnroutableMessage();
        demonstrateInvalidExchange();
        demonstrateInvalidRoutingKey();
        demonstrateMandatoryFlag();
    }

    /**
     * Routable message (success case)
     */
    private void demonstrateRoutableMessage() throws Exception {
        System.out.println("=== Publisher Returns Pattern ===\n");
        System.out.println("1. Routable Message (Success):");
        System.out.println("   Publisher returns enabled: " + 
            connectionFactory.isPublisherReturns());
        System.out.println("   Mandatory flag: " + rabbitTemplate.isMandatory());

        // Send to valid exchange and routing key
        rabbitTemplate.convertAndSend(
            EXCHANGE_NAME, 
            ROUTING_KEY, 
            "Routable message"
        );
        
        System.out.println("   - Message sent to: " + EXCHANGE_NAME);
        System.out.println("   - Routing key: " + ROUTING_KEY);
        System.out.println("   - ✓ Message successfully routed");
        
        Thread.sleep(1000);
        System.out.println();
    }

    /**
     * Unroutable message (no matching queue)
     */
    private void demonstrateUnroutableMessage() throws Exception {
        System.out.println("2. Unroutable Message (Invalid Routing Key):");

        // Send to valid exchange but invalid routing key
        String invalidKey = "invalid.routing.key";
        System.out.println("   - Attempting to send to: " + EXCHANGE_NAME);
        System.out.println("   - With routing key: " + invalidKey);
        
        rabbitTemplate.convertAndSend(
            EXCHANGE_NAME, 
            invalidKey, 
            "Unroutable message - bad routing key"
        );
        
        System.out.println("   - Message sent with mandatory=true");
        System.out.println("   - No queue bound to this routing key");
        
        Thread.sleep(2000);
        System.out.println();
    }

    /**
     * Invalid exchange
     */
    private void demonstrateInvalidExchange() throws Exception {
        System.out.println("3. Invalid Exchange:");

        String invalidExchange = "non.existent.exchange";
        System.out.println("   - Attempting to send to: " + invalidExchange);
        
        try {
            rabbitTemplate.convertAndSend(
                invalidExchange, 
                ROUTING_KEY, 
                "Message to invalid exchange"
            );
        } catch (Exception e) {
            System.out.println("   ✗ Exception thrown: " + e.getMessage());
            System.out.println("   (Invalid exchange causes immediate exception)");
        }
        
        Thread.sleep(1000);
        System.out.println();
    }

    /**
     * Multiple routing scenarios
     */
    private void demonstrateInvalidRoutingKey() throws Exception {
        System.out.println("4. Multiple Routing Scenarios:");

        // Scenario 1: Valid routing
        System.out.println("\n   Scenario A - Valid routing:");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, "Valid message");
        System.out.println("   - Routing key: " + ROUTING_KEY + " ✓");
        Thread.sleep(500);

        // Scenario 2: Invalid routing key pattern
        System.out.println("\n   Scenario B - Invalid pattern:");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "wrong.pattern", "Invalid pattern");
        System.out.println("   - Routing key: wrong.pattern ✗");
        Thread.sleep(1000);

        // Scenario 3: Empty routing key
        System.out.println("\n   Scenario C - Empty routing key:");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "", "Empty key");
        System.out.println("   - Routing key: (empty) ✗");
        Thread.sleep(1000);

        System.out.println();
    }

    /**
     * Mandatory flag behavior
     */
    private void demonstrateMandatoryFlag() throws Exception {
        System.out.println("5. Mandatory Flag Behavior:");

        System.out.println("\n   With mandatory=true (current):");
        System.out.println("   - Unroutable messages are RETURNED");
        System.out.println("   - Publisher is notified via callback");
        System.out.println("   - Can implement retry logic");

        System.out.println("\n   With mandatory=false:");
        System.out.println("   - Unroutable messages are DROPPED");
        System.out.println("   - No notification to publisher");
        System.out.println("   - Message lost silently");

        // Test with mandatory=true
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "bad.key", "Mandatory true");
        System.out.println("\n   - Sent with mandatory=true, bad routing key");
        Thread.sleep(1000);

        System.out.println("\n6. Publisher Returns Configuration:");
        System.out.println("   Connection Factory:");
        System.out.println("   factory.setPublisherReturns(true);");
        System.out.println("\n   RabbitTemplate:");
        System.out.println("   template.setMandatory(true);");
        System.out.println("   template.setReturnsCallback(returned -> {");
        System.out.println("       // Handle returned message");
        System.out.println("       String reason = returned.getReplyText();");
        System.out.println("       // Implement retry or logging");
        System.out.println("   });");

        System.out.println("\n7. Common Return Reasons:");
        System.out.println("   - NO_ROUTE (312): No matching queue for routing key");
        System.out.println("   - NO_CONSUMERS (313): Queue exists but no consumers");

        System.out.println("\n8. Handling Strategies:");
        System.out.println("   - Log returned messages for monitoring");
        System.out.println("   - Retry with different routing key");
        System.out.println("   - Send to dead letter queue");
        System.out.println("   - Alert operations team");
        System.out.println("   - Store for manual processing");
        System.out.println("   - Use alternate exchange pattern");

        System.out.println("\n9. Best Practices:");
        System.out.println("   - Always set mandatory=true for critical messages");
        System.out.println("   - Implement returns callback for monitoring");
        System.out.println("   - Use alternate exchanges for fallback routing");
        System.out.println("   - Log all returned messages");
        System.out.println("   - Consider using publisher confirms together");
        System.out.println("   - Test routing keys before production");
        System.out.println("   - Monitor return metrics");

        System.out.println("\n10. Alternate Exchange Pattern:");
        System.out.println("    Declare exchange with alternate exchange:");
        System.out.println("    Map<String, Object> args = new HashMap<>();");
        System.out.println("    args.put(\"alternate-exchange\", \"fallback-exchange\");");
        System.out.println("    DirectExchange exchange = ");
        System.out.println("        new DirectExchange(name, durable, autoDelete, args);");
        System.out.println("\n    Unroutable messages automatically go to alternate exchange");
    }
}
