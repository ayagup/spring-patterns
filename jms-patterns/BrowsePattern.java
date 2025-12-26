package com.example.jms.patterns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.BrowserCallback;
import javax.jms.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Browse Pattern
 * 
 * Demonstrates browsing JMS queues without consuming messages.
 * Enables inspection of queue contents, message monitoring, and
 * queue management without affecting message availability.
 * 
 * Key Features:
 * - Non-destructive queue inspection
 * - Message counting
 * - Message filtering with selectors
 * - Queue monitoring
 * - Message preview
 * - Queue depth checking
 */
@SpringBootApplication
public class BrowsePattern implements CommandLineRunner {

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final String QUEUE_NAME = "browse-queue";

    public static void main(String[] args) {
        SpringApplication.run(BrowsePattern.class, args);
    }

    @Override
    public void run(String... args) {
        // Populate queue with test messages
        populateQueue();
        
        demonstrateBasicBrowse();
        demonstrateBrowseWithSelector();
        demonstrateMessageCounting();
        demonstrateMessageInspection();
        demonstrateQueueMonitoring();
    }

    /**
     * Populate queue with test messages
     */
    private void populateQueue() {
        System.out.println("=== Browse Pattern ===\n");
        System.out.println("Populating queue with test messages...");

        for (int i = 1; i <= 10; i++) {
            final int priority = i % 3; // 0, 1, or 2
            final int messageNum = i;
            
            jmsTemplate.send(QUEUE_NAME, session -> {
                TextMessage message = session.createTextMessage("Message #" + messageNum);
                message.setIntProperty("priority", priority);
                message.setIntProperty("sequence", messageNum);
                message.setStringProperty("type", messageNum % 2 == 0 ? "even" : "odd");
                return message;
            });
        }
        System.out.println("10 messages added to queue\n");
    }

    /**
     * Basic queue browsing
     */
    private void demonstrateBasicBrowse() {
        System.out.println("1. Basic Queue Browsing:");

        jmsTemplate.browse(QUEUE_NAME, new BrowserCallback<Void>() {
            @Override
            public Void doInJms(Session session, QueueBrowser browser) throws JMSException {
                System.out.println("   - Queue: " + browser.getQueue().getQueueName());
                
                @SuppressWarnings("unchecked")
                Enumeration<Message> messages = browser.getEnumeration();
                
                int count = 0;
                while (messages.hasMoreElements()) {
                    Message message = messages.nextElement();
                    if (message instanceof TextMessage) {
                        TextMessage textMsg = (TextMessage) message;
                        count++;
                        System.out.println("   - Message " + count + ": " + textMsg.getText());
                    }
                }
                
                System.out.println("   - Total messages: " + count);
                return null;
            }
        });

        System.out.println();
    }

    /**
     * Browse with message selector
     */
    private void demonstrateBrowseWithSelector() {
        System.out.println("2. Browse with Message Selector:");

        // Browse only high priority messages
        String selector = "priority = 2";
        System.out.println("   - Selector: " + selector);

        jmsTemplate.browseSelected(QUEUE_NAME, selector, new BrowserCallback<Void>() {
            @Override
            public Void doInJms(Session session, QueueBrowser browser) throws JMSException {
                @SuppressWarnings("unchecked")
                Enumeration<Message> messages = browser.getEnumeration();
                
                int count = 0;
                while (messages.hasMoreElements()) {
                    Message message = messages.nextElement();
                    if (message instanceof TextMessage) {
                        TextMessage textMsg = (TextMessage) message;
                        int priority = message.getIntProperty("priority");
                        count++;
                        System.out.println("   - " + textMsg.getText() + 
                                         " (Priority: " + priority + ")");
                    }
                }
                
                System.out.println("   - Matching messages: " + count);
                return null;
            }
        });

        System.out.println();
    }

    /**
     * Count messages in queue
     */
    private void demonstrateMessageCounting() {
        System.out.println("3. Message Counting:");

        Integer totalCount = jmsTemplate.browse(QUEUE_NAME, new BrowserCallback<Integer>() {
            @Override
            public Integer doInJms(Session session, QueueBrowser browser) throws JMSException {
                @SuppressWarnings("unchecked")
                Enumeration<Message> messages = browser.getEnumeration();
                
                int count = 0;
                while (messages.hasMoreElements()) {
                    messages.nextElement();
                    count++;
                }
                return count;
            }
        });

        System.out.println("   - Total messages in queue: " + totalCount);

        // Count by type
        String evenSelector = "type = 'even'";
        Integer evenCount = jmsTemplate.browseSelected(QUEUE_NAME, evenSelector, 
            new BrowserCallback<Integer>() {
                @Override
                public Integer doInJms(Session session, QueueBrowser browser) throws JMSException {
                    @SuppressWarnings("unchecked")
                    Enumeration<Message> messages = browser.getEnumeration();
                    int count = 0;
                    while (messages.hasMoreElements()) {
                        messages.nextElement();
                        count++;
                    }
                    return count;
                }
            });

        System.out.println("   - Even messages: " + evenCount);
        System.out.println("   - Odd messages: " + (totalCount - evenCount) + "\n");
    }

    /**
     * Detailed message inspection
     */
    private void demonstrateMessageInspection() {
        System.out.println("4. Message Inspection:");

        List<MessageInfo> messageInfos = jmsTemplate.browse(QUEUE_NAME, 
            new BrowserCallback<List<MessageInfo>>() {
                @Override
                public List<MessageInfo> doInJms(Session session, QueueBrowser browser) 
                        throws JMSException {
                    
                    List<MessageInfo> infos = new ArrayList<>();
                    
                    @SuppressWarnings("unchecked")
                    Enumeration<Message> messages = browser.getEnumeration();
                    
                    while (messages.hasMoreElements()) {
                        Message message = messages.nextElement();
                        MessageInfo info = new MessageInfo();
                        
                        if (message instanceof TextMessage) {
                            info.text = ((TextMessage) message).getText();
                        }
                        
                        info.messageId = message.getJMSMessageID();
                        info.timestamp = message.getJMSTimestamp();
                        info.priority = message.getJMSPriority();
                        info.sequence = message.getIntProperty("sequence");
                        
                        infos.add(info);
                    }
                    
                    return infos;
                }
            });

        // Display first 3 messages
        System.out.println("   - First 3 messages:");
        for (int i = 0; i < Math.min(3, messageInfos.size()); i++) {
            MessageInfo info = messageInfos.get(i);
            System.out.println("   " + (i + 1) + ". " + info.text);
            System.out.println("      ID: " + info.messageId);
            System.out.println("      Timestamp: " + info.timestamp);
            System.out.println("      Priority: " + info.priority);
            System.out.println("      Sequence: " + info.sequence);
        }

        System.out.println();
    }

    /**
     * Queue monitoring
     */
    private void demonstrateQueueMonitoring() {
        System.out.println("5. Queue Monitoring:");

        QueueStats stats = jmsTemplate.browse(QUEUE_NAME, new BrowserCallback<QueueStats>() {
            @Override
            public QueueStats doInJms(Session session, QueueBrowser browser) throws JMSException {
                QueueStats stats = new QueueStats();
                stats.queueName = browser.getQueue().getQueueName();
                
                @SuppressWarnings("unchecked")
                Enumeration<Message> messages = browser.getEnumeration();
                
                long oldestTimestamp = Long.MAX_VALUE;
                long newestTimestamp = Long.MIN_VALUE;
                
                while (messages.hasMoreElements()) {
                    Message message = messages.nextElement();
                    stats.messageCount++;
                    
                    long timestamp = message.getJMSTimestamp();
                    if (timestamp < oldestTimestamp) oldestTimestamp = timestamp;
                    if (timestamp > newestTimestamp) newestTimestamp = timestamp;
                    
                    int priority = message.getJMSPriority();
                    if (priority > 4) stats.highPriorityCount++;
                }
                
                stats.oldestMessageAge = System.currentTimeMillis() - oldestTimestamp;
                stats.newestMessageAge = System.currentTimeMillis() - newestTimestamp;
                
                return stats;
            }
        });

        System.out.println("   Queue: " + stats.queueName);
        System.out.println("   - Total messages: " + stats.messageCount);
        System.out.println("   - High priority messages: " + stats.highPriorityCount);
        System.out.println("   - Oldest message age: " + stats.oldestMessageAge + "ms");
        System.out.println("   - Newest message age: " + stats.newestMessageAge + "ms");

        System.out.println("\n6. Browse Pattern Benefits:");
        System.out.println("   - Non-destructive queue inspection");
        System.out.println("   - Message counting and filtering");
        System.out.println("   - Queue depth monitoring");
        System.out.println("   - Message preview without consumption");
        System.out.println("   - Debugging and troubleshooting");
    }

    /**
     * Message info holder
     */
    static class MessageInfo {
        String text;
        String messageId;
        long timestamp;
        int priority;
        int sequence;
    }

    /**
     * Queue statistics
     */
    static class QueueStats {
        String queueName;
        int messageCount;
        int highPriorityCount;
        long oldestMessageAge;
        long newestMessageAge;
    }
}
