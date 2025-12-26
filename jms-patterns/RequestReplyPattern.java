package com.example.jms.patterns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import javax.jms.*;

/**
 * Request-Reply Pattern
 * 
 * Demonstrates synchronous request-reply messaging using JMS.
 * Enables bidirectional communication where a sender waits for
 * a response from the receiver.
 * 
 * Key Features:
 * - Synchronous request-reply
 * - Temporary reply queues
 * - Correlation ID tracking
 * - @SendTo for automatic replies
 * - Request timeout handling
 * - Reply-to destination management
 */
@SpringBootApplication
public class RequestReplyPattern implements CommandLineRunner {

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final String REQUEST_QUEUE = "request-queue";
    private static final String REPLY_QUEUE = "reply-queue";

    public static void main(String[] args) {
        SpringApplication.run(RequestReplyPattern.class, args);
    }

    /**
     * Configure JmsTemplate for request-reply
     */
    @Bean
    public JmsTemplate replyJmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setReceiveTimeout(5000); // 5 second timeout
        return template;
    }

    @Override
    public void run(String... args) throws InterruptedException {
        // Wait for listener to be ready
        Thread.sleep(1000);
        
        demonstrateBasicRequestReply();
        demonstrateTemporaryReplyQueue();
        demonstrateCorrelationId();
        demonstrateRequestWithTimeout();
    }

    /**
     * Basic request-reply using sendAndReceive
     */
    private void demonstrateBasicRequestReply() {
        System.out.println("=== Request-Reply Pattern ===\n");
        System.out.println("1. Basic Request-Reply:");

        // Send request and wait for reply
        String request = "Calculate: 10 + 20";
        System.out.println("   - Sending request: " + request);
        
        String reply = (String) jmsTemplate.convertSendAndReceive(REQUEST_QUEUE, request);
        System.out.println("   - Received reply: " + reply + "\n");
    }

    /**
     * Request-reply using temporary queue
     */
    private void demonstrateTemporaryReplyQueue() {
        System.out.println("2. Request-Reply with Temporary Queue:");

        jmsTemplate.execute(session -> {
            // Create temporary reply queue
            TemporaryQueue replyQueue = session.createTemporaryQueue();
            System.out.println("   - Created temporary reply queue: " + replyQueue.getQueueName());
            
            // Create request message with reply-to
            TextMessage request = session.createTextMessage("Process order: ORD-001");
            request.setJMSReplyTo(replyQueue);
            
            // Create correlation ID
            String correlationId = "CORR-" + System.currentTimeMillis();
            request.setJMSCorrelationID(correlationId);
            
            // Send request
            MessageProducer producer = session.createProducer(session.createQueue(REQUEST_QUEUE));
            producer.send(request);
            System.out.println("   - Request sent with correlation ID: " + correlationId);
            
            // Wait for reply
            MessageConsumer replyConsumer = session.createConsumer(replyQueue);
            Message reply = replyConsumer.receive(5000);
            
            if (reply instanceof TextMessage) {
                TextMessage textReply = (TextMessage) reply;
                System.out.println("   - Reply received: " + textReply.getText());
                System.out.println("   - Reply correlation ID: " + reply.getJMSCorrelationID());
            }
            
            // Cleanup
            replyConsumer.close();
            producer.close();
            replyQueue.delete();
            
            return null;
        }, true);

        System.out.println();
    }

    /**
     * Request-reply with correlation ID
     */
    private void demonstrateCorrelationId() {
        System.out.println("3. Request-Reply with Correlation ID:");

        jmsTemplate.execute(session -> {
            Queue requestQueue = session.createQueue(REQUEST_QUEUE);
            Queue replyQueue = session.createQueue(REPLY_QUEUE);
            
            // Send multiple requests
            String[] requests = {"REQ-001: Data A", "REQ-002: Data B", "REQ-003: Data C"};
            
            for (String reqData : requests) {
                TextMessage message = session.createTextMessage(reqData);
                String correlationId = reqData.split(":")[0];
                message.setJMSCorrelationID(correlationId);
                message.setJMSReplyTo(replyQueue);
                
                MessageProducer producer = session.createProducer(requestQueue);
                producer.send(message);
                System.out.println("   - Sent: " + reqData + " (Correlation: " + correlationId + ")");
                producer.close();
            }
            
            // Receive replies with correlation ID matching
            MessageConsumer consumer = session.createConsumer(replyQueue);
            
            for (int i = 0; i < 3; i++) {
                Message reply = consumer.receive(5000);
                if (reply instanceof TextMessage) {
                    TextMessage textReply = (TextMessage) reply;
                    System.out.println("   - Reply: " + textReply.getText() + 
                                     " (Correlation: " + reply.getJMSCorrelationID() + ")");
                }
            }
            
            consumer.close();
            return null;
        }, true);

        System.out.println();
    }

    /**
     * Request with timeout handling
     */
    private void demonstrateRequestWithTimeout() {
        System.out.println("4. Request with Timeout:");

        // Set short timeout
        jmsTemplate.setReceiveTimeout(2000);
        
        System.out.println("   - Sending request with 2s timeout");
        Object reply = jmsTemplate.convertSendAndReceive(REQUEST_QUEUE, "Slow operation");
        
        if (reply != null) {
            System.out.println("   - Reply received: " + reply);
        } else {
            System.out.println("   - No reply received (timeout)");
        }

        System.out.println("\n5. Request-Reply Benefits:");
        System.out.println("   - Synchronous communication");
        System.out.println("   - Correlation ID for request tracking");
        System.out.println("   - Temporary queues for isolation");
        System.out.println("   - Timeout control");
        System.out.println("   - Bidirectional messaging");
    }

    /**
     * Request processor (server-side)
     */
    @Component
    static class RequestProcessor {

        /**
         * Process requests and send replies using @SendTo
         */
        @JmsListener(destination = REQUEST_QUEUE)
        @SendTo(REPLY_QUEUE)
        public String processRequest(String request) {
            System.out.println("\n   [Server] Received request: " + request);
            
            // Process the request
            String reply;
            if (request.contains("Calculate")) {
                reply = "Result: 30";
            } else if (request.contains("order")) {
                reply = "Order processed successfully";
            } else {
                reply = "Processed: " + request;
            }
            
            System.out.println("   [Server] Sending reply: " + reply);
            return reply;
        }

        /**
         * Manual reply handling with JMSReplyTo
         */
        @JmsListener(destination = REQUEST_QUEUE)
        public void processRequestWithManualReply(Message message, Session session) {
            try {
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String request = textMessage.getText();
                    
                    // Check for reply-to destination
                    Destination replyTo = message.getJMSReplyTo();
                    if (replyTo != null) {
                        // Create reply message
                        TextMessage reply = session.createTextMessage("Reply to: " + request);
                        
                        // Set correlation ID
                        String correlationId = message.getJMSCorrelationID();
                        if (correlationId != null) {
                            reply.setJMSCorrelationID(correlationId);
                        }
                        
                        // Send reply
                        MessageProducer producer = session.createProducer(replyTo);
                        producer.send(reply);
                        producer.close();
                    }
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
