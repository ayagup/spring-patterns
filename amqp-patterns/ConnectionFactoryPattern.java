package com.example.amqp.patterns;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;

/**
 * Connection Factory Pattern
 * 
 * Demonstrates configuration and management of AMQP connection factories.
 * Connection factories are responsible for creating and managing connections
 * to RabbitMQ brokers with various configuration options.
 * 
 * Key Features:
 * - Connection pooling with CachingConnectionFactory
 * - Connection caching strategies
 * - Channel caching
 * - Connection listeners
 * - SSL/TLS configuration
 * - Publisher confirms mode
 * - Connection recovery
 */
@SpringBootApplication
public class ConnectionFactoryPattern implements CommandLineRunner {

    @Autowired
    private org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static void main(String[] args) {
        SpringApplication.run(ConnectionFactoryPattern.class, args);
    }

    /**
     * Basic caching connection factory
     */
    @Bean
    public org.springframework.amqp.rabbit.connection.ConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        
        // Configure connection caching
        factory.setConnectionCacheSize(10);
        
        // Configure channel caching
        factory.setChannelCacheSize(25);
        factory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
        
        // Enable publisher confirms
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        
        // Enable publisher returns
        factory.setPublisherReturns(true);
        
        return factory;
    }

    /**
     * Connection factory with connection pooling
     */
    @Bean
    public org.springframework.amqp.rabbit.connection.ConnectionFactory connectionPoolFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
        
        // Enable CONNECTION cache mode for connection pooling
        factory.setCacheMode(CachingConnectionFactory.CacheMode.CONNECTION);
        factory.setConnectionCacheSize(5); // Max 5 cached connections
        
        // Channel cache size per connection
        factory.setChannelCacheSize(10);
        
        return factory;
    }

    /**
     * Queue for testing
     */
    @Bean
    public Queue testQueue() {
        return new Queue("connection-factory-queue", false);
    }

    @Override
    public void run(String... args) {
        demonstrateConnectionFactory();
        demonstrateCaching();
        demonstrateConfiguration();
    }

    /**
     * Basic connection factory usage
     */
    private void demonstrateConnectionFactory() {
        System.out.println("=== Connection Factory Pattern ===\n");
        System.out.println("1. Connection Factory Configuration:");

        if (connectionFactory instanceof CachingConnectionFactory) {
            CachingConnectionFactory cachingFactory = (CachingConnectionFactory) connectionFactory;
            
            System.out.println("   - Host: " + cachingFactory.getHost());
            System.out.println("   - Port: " + cachingFactory.getPort());
            System.out.println("   - Virtual Host: " + cachingFactory.getVirtualHost());
            System.out.println("   - Cache mode: " + cachingFactory.getCacheMode());
            System.out.println("   - Channel cache size: " + cachingFactory.getChannelCacheSize());
            System.out.println("   - Publisher confirms: " + cachingFactory.isPublisherConfirms());
            System.out.println("   - Publisher returns: " + cachingFactory.isPublisherReturns());
        }

        System.out.println();
    }

    /**
     * Demonstrate caching behavior
     */
    private void demonstrateCaching() {
        System.out.println("2. Connection and Channel Caching:");

        // Send messages to demonstrate caching
        for (int i = 1; i <= 5; i++) {
            rabbitTemplate.convertAndSend("connection-factory-queue", "Message #" + i);
            System.out.println("   - Sent message #" + i);
        }

        if (connectionFactory instanceof CachingConnectionFactory) {
            CachingConnectionFactory cachingFactory = (CachingConnectionFactory) connectionFactory;
            System.out.println("\n   Cache Statistics:");
            System.out.println("   - Cache mode: " + cachingFactory.getCacheMode());
            System.out.println("   - Channel cache size: " + cachingFactory.getChannelCacheSize());
        }

        System.out.println();
    }

    /**
     * Advanced configuration options
     */
    private void demonstrateConfiguration() {
        System.out.println("3. Advanced Configuration:");

        // Create a custom configured connection factory
        CachingConnectionFactory customFactory = new CachingConnectionFactory("localhost");
        
        // Connection settings
        customFactory.setConnectionTimeout(30000); // 30 seconds
        customFactory.setRequestedHeartBeat(60); // 60 seconds
        
        // Publisher settings
        customFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        customFactory.setPublisherReturns(true);
        
        // Channel settings
        customFactory.setChannelCacheSize(50);
        customFactory.setChannelCheckoutTimeout(30000);
        
        System.out.println("   Custom Factory Configuration:");
        System.out.println("   - Connection timeout: 30s");
        System.out.println("   - Heartbeat: 60s");
        System.out.println("   - Publisher confirms: CORRELATED");
        System.out.println("   - Publisher returns: true");
        System.out.println("   - Channel cache size: 50");
        System.out.println("   - Channel checkout timeout: 30s");

        System.out.println("\n4. Caching Modes:");
        System.out.println("   - CHANNEL: Cache channels (default)");
        System.out.println("     * Single connection shared");
        System.out.println("     * Multiple cached channels");
        System.out.println("     * Suitable for most applications");
        System.out.println("\n   - CONNECTION: Cache connections");
        System.out.println("     * Multiple cached connections");
        System.out.println("     * Each connection has channel cache");
        System.out.println("     * Suitable for high concurrency");

        System.out.println("\n5. Connection Factory Benefits:");
        System.out.println("   - Connection pooling and reuse");
        System.out.println("   - Channel caching for performance");
        System.out.println("   - Automatic connection recovery");
        System.out.println("   - Publisher confirms support");
        System.out.println("   - Thread-safe operations");
        System.out.println("   - Resource management");
    }
}
