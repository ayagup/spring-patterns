package com.example.jms.patterns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.ProducerCallback;
import javax.jms.*;

/**
 * Producer Callback Pattern
 * 
 * Demonstrates the use of ProducerCallback for fine-grained control over
 * message production. Enables custom configuration of message producers
 * and sending behavior.
 * 
 * Key Features:
 * - Direct producer access
 * - Custom delivery modes
 * - Priority configuration
 * - Time-to-live settings
 * - Multiple sends per producer
 * - Producer-level QoS settings
 */
@SpringBootApplication
public class ProducerCallbackPattern implements CommandLineRunner {

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final String QUEUE_NAME = "producer-callback-queue";

    public static void main(String[] args) {
        SpringApplication.run(ProducerCallbackPattern.class, args);
    }

    @Override
    public void run(String... args) {
        demonstrateBasicProducerCallback();
        demonstrateCustomDeliveryMode();
        demonstratePriorityAndTTL();
        demonstrateBatchSending();
    }

    /**
     * Basic producer callback usage
     */
    private void demonstrateBasicProducerCallback() {
        System.out.println("=== Producer Callback Pattern ===\n");
        System.out.println("1. Basic Producer Callback:");

        jmsTemplate.execute(session -> {
            Queue queue = session.createQueue(QUEUE_NAME);
            return queue;
        }, new ProducerCallback<Void>() {
            @Override
            public Void doInJms(Session session, MessageProducer producer) 
                    throws JMSException {
                
                // Send multiple messages with same producer
                for (int i = 1; i <= 3; i++) {
                    TextMessage message = session.createTextMessage("Message #" + i);
                    producer.send(message);
                    System.out.println("   - Sent: Message #" + i);
                }
                
                return null;
            }
        });

        System.out.println();
    }

    /**
     * Custom delivery mode configuration
     */
    private void demonstrateCustomDeliveryMode() {
        System.out.println("2. Custom Delivery Mode:");

        jmsTemplate.execute(session -> session.createQueue(QUEUE_NAME), 
            new ProducerCallback<Void>() {
                @Override
                public Void doInJms(Session session, MessageProducer producer) 
                        throws JMSException {
                    
                    // Persistent delivery mode
                    producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                    TextMessage persistentMsg = session.createTextMessage("Persistent message");
                    producer.send(persistentMsg);
                    System.out.println("   - Sent with PERSISTENT mode");
                    
                    // Non-persistent delivery mode
                    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                    TextMessage nonPersistentMsg = session.createTextMessage("Non-persistent message");
                    producer.send(nonPersistentMsg);
                    System.out.println("   - Sent with NON_PERSISTENT mode");
                    
                    return null;
                }
            });

        System.out.println();
    }

    /**
     * Message priority and time-to-live
     */
    private void demonstratePriorityAndTTL() {
        System.out.println("3. Priority and Time-To-Live:");

        jmsTemplate.execute(session -> session.createQueue(QUEUE_NAME), 
            new ProducerCallback<Void>() {
                @Override
                public Void doInJms(Session session, MessageProducer producer) 
                        throws JMSException {
                    
                    // High priority message
                    producer.setPriority(9);
                    TextMessage highPriorityMsg = session.createTextMessage("High priority");
                    producer.send(highPriorityMsg);
                    System.out.println("   - Sent with priority: 9");
                    
                    // Message with TTL
                    producer.setPriority(4); // Reset to default
                    producer.setTimeToLive(10000); // 10 seconds
                    TextMessage ttlMsg = session.createTextMessage("Message with TTL");
                    producer.send(ttlMsg);
                    System.out.println("   - Sent with TTL: 10 seconds");
                    
                    // Per-message overrides
                    TextMessage customMsg = session.createTextMessage("Custom QoS message");
                    producer.send(customMsg, 
                                DeliveryMode.PERSISTENT,  // delivery mode
                                7,                         // priority
                                5000);                     // TTL (5 seconds)
                    System.out.println("   - Sent with custom QoS: priority=7, TTL=5s");
                    
                    return null;
                }
            });

        System.out.println();
    }

    /**
     * Batch sending with producer callback
     */
    private void demonstrateBatchSending() {
        System.out.println("4. Batch Sending:");

        long startTime = System.currentTimeMillis();

        Integer messageCount = jmsTemplate.execute(
            session -> session.createQueue(QUEUE_NAME), 
            new ProducerCallback<Integer>() {
                @Override
                public Integer doInJms(Session session, MessageProducer producer) 
                        throws JMSException {
                    
                    // Configure producer for batch
                    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                    
                    int count = 0;
                    // Send batch of 100 messages
                    for (int i = 1; i <= 100; i++) {
                        TextMessage message = session.createTextMessage("Batch message #" + i);
                        message.setIntProperty("batchId", 1);
                        message.setIntProperty("sequence", i);
                        producer.send(message);
                        count++;
                    }
                    
                    return count;
                }
            });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("   - Messages sent: " + messageCount);
        System.out.println("   - Time taken: " + duration + "ms");
        System.out.println("   - Throughput: " + (messageCount * 1000 / duration) + " msg/sec");

        System.out.println("\n5. Producer Callback Benefits:");
        System.out.println("   - Direct access to MessageProducer");
        System.out.println("   - Reuse producer for multiple sends");
        System.out.println("   - Custom QoS configuration");
        System.out.println("   - Per-message or producer-level settings");
        System.out.println("   - Efficient batch operations");
        System.out.println("   - Fine-grained delivery control");
    }
}
