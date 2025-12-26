package com.example.amqp.patterns;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

/**
 * Channel Pattern
 * 
 * Demonstrates direct access to AMQP channels for fine-grained control
 * over messaging operations. Channels are lightweight virtual connections
 * multiplexed over a single TCP connection.
 * 
 * Key Features:
 * - Direct channel access via ChannelCallback
 * - Channel-level operations
 * - Transaction management
 * - QoS (Quality of Service) configuration
 * - Publisher confirms
 * - Flow control
 */
@SpringBootApplication
public class ChannelPattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    private static final String QUEUE_NAME = "channel-pattern-queue";

    public static void main(String[] args) {
        SpringApplication.run(ChannelPattern.class, args);
    }

    @Bean
    public Queue channelQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Override
    public void run(String... args) {
        demonstrateChannelCallback();
        demonstrateChannelTransactions();
        demonstrateQoSConfiguration();
        demonstratePublisherConfirms();
    }

    /**
     * Basic channel callback usage
     */
    private void demonstrateChannelCallback() {
        System.out.println("=== Channel Pattern ===\n");
        System.out.println("1. Channel Callback:");

        Boolean result = rabbitTemplate.execute(new ChannelCallback<Boolean>() {
            @Override
            public Boolean doInRabbit(Channel channel) throws Exception {
                System.out.println("   - Channel number: " + channel.getChannelNumber());
                System.out.println("   - Channel open: " + channel.isOpen());
                
                // Declare queue directly on channel
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                System.out.println("   - Queue declared: " + QUEUE_NAME);
                
                // Publish messages directly
                for (int i = 1; i <= 3; i++) {
                    String message = "Channel message #" + i;
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                    System.out.println("   - Published: " + message);
                }
                
                return true;
            }
        });

        System.out.println("   - Operation result: " + result + "\n");
    }

    /**
     * Channel-level transactions
     */
    private void demonstrateChannelTransactions() {
        System.out.println("2. Channel Transactions:");

        rabbitTemplate.execute(new ChannelCallback<Void>() {
            @Override
            public Void doInRabbit(Channel channel) throws Exception {
                try {
                    // Enable transactions on channel
                    channel.txSelect();
                    System.out.println("   - Transaction started");
                    
                    // Publish messages in transaction
                    for (int i = 1; i <= 3; i++) {
                        String message = "Transactional message #" + i;
                        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                        System.out.println("   - Published: " + message);
                    }
                    
                    // Commit transaction
                    channel.txCommit();
                    System.out.println("   - Transaction committed");
                    
                } catch (Exception e) {
                    // Rollback on error
                    channel.txRollback();
                    System.out.println("   - Transaction rolled back");
                    throw e;
                }
                
                return null;
            }
        });

        System.out.println();
    }

    /**
     * QoS (Quality of Service) configuration
     */
    private void demonstrateQoSConfiguration() {
        System.out.println("3. QoS Configuration:");

        rabbitTemplate.execute(new ChannelCallback<Void>() {
            @Override
            public Void doInRabbit(Channel channel) throws Exception {
                // Set prefetch count (number of unacked messages per channel)
                channel.basicQos(10); // Prefetch 10 messages
                System.out.println("   - Prefetch count set to: 10");
                
                // Set prefetch size (in bytes)
                channel.basicQos(0, 10, false); 
                // prefetchSize=0 (no limit), prefetchCount=10, global=false (per channel)
                System.out.println("   - QoS configured:");
                System.out.println("     * Prefetch size: unlimited");
                System.out.println("     * Prefetch count: 10");
                System.out.println("     * Scope: per-channel");
                
                // Global QoS (per connection)
                channel.basicQos(0, 50, true);
                System.out.println("     * Global prefetch: 50 (per connection)");
                
                return null;
            }
        });

        System.out.println();
    }

    /**
     * Publisher confirms
     */
    private void demonstratePublisherConfirms() {
        System.out.println("4. Publisher Confirms:");

        rabbitTemplate.execute(new ChannelCallback<Void>() {
            @Override
            public Void doInRabbit(Channel channel) throws Exception {
                // Enable publisher confirms
                channel.confirmSelect();
                System.out.println("   - Publisher confirms enabled");
                
                // Publish messages
                for (int i = 1; i <= 3; i++) {
                    String message = "Confirmed message #" + i;
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                    
                    // Wait for confirm
                    boolean confirmed = channel.waitForConfirms(5000);
                    System.out.println("   - Message " + i + " confirmed: " + confirmed);
                }
                
                return null;
            }
        });

        System.out.println("\n5. Channel Operations:");
        System.out.println("   - basicPublish: Publish messages");
        System.out.println("   - basicConsume: Start consuming");
        System.out.println("   - basicAck: Acknowledge messages");
        System.out.println("   - basicNack: Negative acknowledgment");
        System.out.println("   - basicReject: Reject messages");
        System.out.println("   - basicQos: Set QoS parameters");
        System.out.println("   - txSelect/txCommit/txRollback: Transactions");
        System.out.println("   - confirmSelect/waitForConfirms: Publisher confirms");

        System.out.println("\n6. Channel Benefits:");
        System.out.println("   - Lightweight virtual connections");
        System.out.println("   - Thread-safe when not shared");
        System.out.println("   - Fine-grained control over messaging");
        System.out.println("   - Transaction support");
        System.out.println("   - QoS configuration");
        System.out.println("   - Direct AMQP protocol access");
    }

    /**
     * Demonstrate channel information
     */
    private void demonstrateChannelInfo() {
        Connection connection = connectionFactory.createConnection();
        Channel channel = connection.createChannel(false);
        
        System.out.println("\n7. Channel Information:");
        System.out.println("   - Channel number: " + channel.getChannelNumber());
        System.out.println("   - Is open: " + channel.isOpen());
        
        try {
            channel.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
