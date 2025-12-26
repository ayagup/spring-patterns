package com.example.jms.patterns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import javax.jms.*;
import javax.naming.NamingException;

/**
 * Destination Resolver Pattern
 * 
 * Demonstrates various strategies for resolving JMS destinations (queues/topics).
 * Destination resolvers decouple destination lookups from application code
 * and enable flexible destination management.
 * 
 * Key Features:
 * - Dynamic destination resolution
 * - JNDI-based destination lookup
 * - Custom destination resolver implementation
 * - Destination caching
 * - Runtime destination configuration
 */
@SpringBootApplication
public class DestinationResolverPattern implements CommandLineRunner {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    public static void main(String[] args) {
        SpringApplication.run(DestinationResolverPattern.class, args);
    }

    /**
     * Dynamic destination resolver - creates destinations on-the-fly
     */
    @Bean
    public DestinationResolver dynamicDestinationResolver() {
        return new DynamicDestinationResolver();
    }

    /**
     * Custom destination resolver with prefix routing
     */
    @Bean
    public DestinationResolver customDestinationResolver() {
        return new CustomDestinationResolver();
    }

    /**
     * JNDI destination resolver (for application servers)
     */
    @Bean
    public DestinationResolver jndiDestinationResolver() {
        JndiDestinationResolver resolver = new JndiDestinationResolver();
        resolver.setCache(true); // Enable destination caching
        resolver.setFallbackToDynamicDestination(true); // Fallback to dynamic creation
        return resolver;
    }

    /**
     * JmsTemplate with custom resolver
     */
    @Bean
    public JmsTemplate customJmsTemplate() {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setDestinationResolver(customDestinationResolver());
        return template;
    }

    @Override
    public void run(String... args) {
        demonstrateDestinationResolvers();
    }

    private void demonstrateDestinationResolvers() {
        System.out.println("=== Destination Resolver Pattern ===\n");

        // Test 1: Dynamic destination resolution
        System.out.println("1. Dynamic Destination Resolver:");
        System.out.println("   - Creates destinations dynamically at runtime");
        jmsTemplate.setDestinationResolver(dynamicDestinationResolver());
        jmsTemplate.convertAndSend("dynamic-queue-1", "Message to dynamic queue");
        System.out.println("   - Message sent to: dynamic-queue-1\n");

        // Test 2: Custom destination resolver with routing
        System.out.println("2. Custom Destination Resolver:");
        System.out.println("   - Routes based on prefixes and patterns");
        JmsTemplate customTemplate = customJmsTemplate();
        
        customTemplate.convertAndSend("order.new", "New order message");
        System.out.println("   - 'order.new' routed to order queue");
        
        customTemplate.convertAndSend("payment.process", "Payment message");
        System.out.println("   - 'payment.process' routed to payment queue");
        
        customTemplate.convertAndSend("notification.email", "Email notification");
        System.out.println("   - 'notification.email' routed to notification queue\n");

        // Test 3: Destination resolution with session
        System.out.println("3. Session-based Destination Resolution:");
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            DestinationResolver resolver = dynamicDestinationResolver();
            Destination dest = resolver.resolveDestinationName(session, "resolved-queue", false);
            
            System.out.println("   - Resolved destination: " + dest);
            System.out.println("   - Destination class: " + dest.getClass().getSimpleName());
            
            session.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n4. Destination Resolver Features:");
        System.out.println("   - Dynamic vs. JNDI-based resolution");
        System.out.println("   - Destination caching for performance");
        System.out.println("   - Fallback strategies");
        System.out.println("   - Custom routing logic");
        System.out.println("   - Queue vs. Topic differentiation");
    }

    /**
     * Custom destination resolver with routing logic
     */
    static class CustomDestinationResolver implements DestinationResolver {

        private final DynamicDestinationResolver dynamicResolver = new DynamicDestinationResolver();

        @Override
        public Destination resolveDestinationName(Session session, String destinationName, 
                                                  boolean pubSubDomain) throws JMSException {
            
            // Apply custom routing logic
            String resolvedName = routeDestination(destinationName);
            System.out.println("   [Custom Resolver] '" + destinationName + 
                             "' -> '" + resolvedName + "'");
            
            // Use dynamic resolver for actual destination creation
            return dynamicResolver.resolveDestinationName(session, resolvedName, pubSubDomain);
        }

        /**
         * Custom routing logic based on destination name patterns
         */
        private String routeDestination(String destinationName) {
            if (destinationName.startsWith("order.")) {
                return "order-processing-queue";
            } else if (destinationName.startsWith("payment.")) {
                return "payment-processing-queue";
            } else if (destinationName.startsWith("notification.")) {
                return "notification-queue";
            } else if (destinationName.contains("priority")) {
                return "high-priority-queue";
            }
            
            // Default: use original name
            return destinationName;
        }
    }

    /**
     * Environment-aware destination resolver
     */
    static class EnvironmentAwareDestinationResolver implements DestinationResolver {

        private final DynamicDestinationResolver dynamicResolver = new DynamicDestinationResolver();
        private final String environment;

        public EnvironmentAwareDestinationResolver(String environment) {
            this.environment = environment;
        }

        @Override
        public Destination resolveDestinationName(Session session, String destinationName, 
                                                  boolean pubSubDomain) throws JMSException {
            
            // Prefix destination name with environment
            String envDestinationName = environment + "." + destinationName;
            System.out.println("   [Environment Resolver] Environment: " + environment);
            System.out.println("   Resolved: '" + destinationName + "' -> '" + 
                             envDestinationName + "'");
            
            return dynamicResolver.resolveDestinationName(
                session, envDestinationName, pubSubDomain);
        }
    }

    /**
     * Caching destination resolver
     */
    static class CachingDestinationResolver implements DestinationResolver {

        private final DynamicDestinationResolver dynamicResolver = new DynamicDestinationResolver();
        private final java.util.Map<String, Destination> cache = 
            new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public Destination resolveDestinationName(Session session, String destinationName, 
                                                  boolean pubSubDomain) throws JMSException {
            
            String cacheKey = destinationName + "-" + pubSubDomain;
            
            // Check cache first
            Destination cached = cache.get(cacheKey);
            if (cached != null) {
                System.out.println("   [Cache] Hit for: " + destinationName);
                return cached;
            }
            
            // Resolve and cache
            System.out.println("   [Cache] Miss for: " + destinationName);
            Destination destination = dynamicResolver.resolveDestinationName(
                session, destinationName, pubSubDomain);
            cache.put(cacheKey, destination);
            
            return destination;
        }

        public void clearCache() {
            cache.clear();
            System.out.println("   [Cache] Cleared");
        }
    }
}
