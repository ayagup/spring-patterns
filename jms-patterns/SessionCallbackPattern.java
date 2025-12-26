package com.example.jms.patterns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import javax.jms.*;

/**
 * Session Callback Pattern
 * 
 * Demonstrates the use of SessionCallback for executing multiple JMS
 * operations within a single JMS session. Provides fine-grained control
 * over session lifecycle and transaction boundaries.
 * 
 * Key Features:
 * - Single session for multiple operations
 * - Transaction control
 * - Resource management
 * - Batch operations
 * - Session-level configuration
 */
@SpringBootApplication
public class SessionCallbackPattern implements CommandLineRunner {

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final String QUEUE_NAME = "session-callback-queue";

    public static void main(String[] args) {
        SpringApplication.run(SessionCallbackPattern.class, args);
    }

    @Override
    public void run(String... args) {
        demonstrateSessionCallback();
        demonstrateBatchOperations();
        demonstrateTransactionalOperations();
        demonstrateSessionProperties();
    }

    /**
     * Basic session callback usage
     */
    private void demonstrateSessionCallback() {
        System.out.println("=== Session Callback Pattern ===\n");
        System.out.println("1. Basic Session Callback:");

        Integer result = jmsTemplate.execute(new SessionCallback<Integer>() {
            @Override
            public Integer doInJms(Session session) throws JMSException {
                // Create producer and send messages
                MessageProducer producer = session.createProducer(
                    session.createQueue(QUEUE_NAME));
                
                int messageCount = 0;
                for (int i = 1; i <= 5; i++) {
                    TextMessage message = session.createTextMessage("Message #" + i);
                    message.setIntProperty("sequence", i);
                    producer.send(message);
                    messageCount++;
                    System.out.println("   - Sent: Message #" + i);
                }
                
                producer.close();
                return messageCount;
            }
        });

        System.out.println("   - Total messages sent: " + result + "\n");
    }

    /**
     * Batch operations using session callback
     */
    private void demonstrateBatchOperations() {
        System.out.println("2. Batch Operations:");

        jmsTemplate.execute(new SessionCallback<Void>() {
            @Override
            public Void doInJms(Session session) throws JMSException {
                Queue queue = session.createQueue(QUEUE_NAME);
                MessageProducer producer = session.createProducer(queue);
                
                // Send batch of messages
                System.out.println("   - Sending batch of 10 messages");
                for (int i = 1; i <= 10; i++) {
                    TextMessage message = session.createTextMessage("Batch message #" + i);
                    message.setBooleanProperty("batch", true);
                    message.setIntProperty("batchId", 1);
                    producer.send(message);
                }
                
                System.out.println("   - Batch sent successfully");
                
                // Consume batch
                MessageConsumer consumer = session.createConsumer(queue);
                int receivedCount = 0;
                
                Message msg;
                while ((msg = consumer.receive(1000)) != null) {
                    if (msg instanceof TextMessage) {
                        TextMessage txtMsg = (TextMessage) msg;
                        receivedCount++;
                    }
                }
                
                System.out.println("   - Batch received: " + receivedCount + " messages");
                
                consumer.close();
                producer.close();
                return null;
            }
        });

        System.out.println();
    }

    /**
     * Transactional operations
     */
    private void demonstrateTransactionalOperations() {
        System.out.println("3. Transactional Operations:");

        boolean success = jmsTemplate.execute(new SessionCallback<Boolean>() {
            @Override
            public Boolean doInJms(Session session) throws JMSException {
                try {
                    // Check if session is transacted
                    boolean transacted = session.getTransacted();
                    System.out.println("   - Session transacted: " + transacted);
                    
                    Queue queue = session.createQueue(QUEUE_NAME);
                    MessageProducer producer = session.createProducer(queue);
                    
                    // Send messages in transaction
                    for (int i = 1; i <= 3; i++) {
                        TextMessage message = session.createTextMessage(
                            "Transactional message #" + i);
                        producer.send(message);
                        System.out.println("   - Sent: Transactional message #" + i);
                    }
                    
                    // Commit transaction (if transacted)
                    if (transacted) {
                        session.commit();
                        System.out.println("   - Transaction committed");
                    }
                    
                    producer.close();
                    return true;
                    
                } catch (Exception e) {
                    // Rollback on error
                    if (session.getTransacted()) {
                        session.rollback();
                        System.out.println("   - Transaction rolled back");
                    }
                    return false;
                }
            }
        });

        System.out.println("   - Operation success: " + success + "\n");
    }

    /**
     * Working with session properties and configuration
     */
    private void demonstrateSessionProperties() {
        System.out.println("4. Session Properties:");

        jmsTemplate.execute(new SessionCallback<Void>() {
            @Override
            public Void doInJms(Session session) throws JMSException {
                // Display session properties
                System.out.println("   - Transacted: " + session.getTransacted());
                System.out.println("   - Acknowledge mode: " + session.getAcknowledgeMode());
                
                // Create temporary queue
                TemporaryQueue tempQueue = session.createTemporaryQueue();
                System.out.println("   - Temporary queue created: " + tempQueue.getQueueName());
                
                // Send message to temporary queue
                MessageProducer producer = session.createProducer(tempQueue);
                TextMessage message = session.createTextMessage("Temporary message");
                producer.send(message);
                System.out.println("   - Message sent to temporary queue");
                
                // Receive from temporary queue
                MessageConsumer consumer = session.createConsumer(tempQueue);
                Message received = consumer.receive(1000);
                if (received instanceof TextMessage) {
                    System.out.println("   - Received: " + ((TextMessage) received).getText());
                }
                
                // Cleanup
                consumer.close();
                producer.close();
                tempQueue.delete();
                System.out.println("   - Temporary queue deleted");
                
                return null;
            }
        });

        System.out.println("\n5. Session Callback Benefits:");
        System.out.println("   - Single session for multiple operations");
        System.out.println("   - Explicit transaction control");
        System.out.println("   - Efficient resource management");
        System.out.println("   - Batch processing support");
        System.out.println("   - Fine-grained error handling");
    }
}
