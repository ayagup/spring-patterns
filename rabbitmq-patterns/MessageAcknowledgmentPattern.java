package com.example.rabbitmq.patterns;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

/**
 * Message Acknowledgment Pattern
 * 
 * Demonstrates different message acknowledgment modes in RabbitMQ:
 * automatic, manual, and their implications for message reliability
 * and processing guarantees.
 * 
 * Key Features:
 * - Automatic acknowledgment (AUTO mode)
 * - Manual acknowledgment (MANUAL mode)
 * - Positive acknowledgment (basicAck)
 * - Negative acknowledgment (basicNack)
 * - Message rejection (basicReject)
 * - Requeue strategies
 * - At-least-once delivery guarantee
 */
@SpringBootApplication
public class MessageAcknowledgmentPattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String ACK_EXCHANGE = "ack-exchange";

    public static void main(String[] args) {
        SpringApplication.run(MessageAcknowledgmentPattern.class, args);
    }

    /**
     * Exchange
     */
    @Bean
    public DirectExchange ackExchange() {
        return ExchangeBuilder
                .directExchange(ACK_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Queues for different acknowledgment modes
     */
    @Bean
    public Queue autoAckQueue() {
        return new Queue("ack-auto-queue", true);
    }

    @Bean
    public Queue manualAckQueue() {
        return new Queue("ack-manual-queue", true);
    }

    @Bean
    public Queue manualNackQueue() {
        return new Queue("ack-manual-nack-queue", true);
    }

    @Bean
    public Queue rejectQueue() {
        return new Queue("ack-reject-queue", true);
    }

    /**
     * Bindings
     */
    @Bean
    public Binding autoAckBinding() {
        return BindingBuilder.bind(autoAckQueue()).to(ackExchange()).with("auto.ack");
    }

    @Bean
    public Binding manualAckBinding() {
        return BindingBuilder.bind(manualAckQueue()).to(ackExchange()).with("manual.ack");
    }

    @Bean
    public Binding manualNackBinding() {
        return BindingBuilder.bind(manualNackQueue()).to(ackExchange()).with("manual.nack");
    }

    @Bean
    public Binding rejectBinding() {
        return BindingBuilder.bind(rejectQueue()).to(ackExchange()).with("reject");
    }

    /**
     * Container factory with AUTO acknowledgment
     */
    @Bean
    public SimpleRabbitListenerContainerFactory autoAckContainerFactory(
            ConnectionFactory connectionFactory) {
        
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }

    /**
     * Container factory with MANUAL acknowledgment
     */
    @Bean
    public SimpleRabbitListenerContainerFactory manualAckContainerFactory(
            ConnectionFactory connectionFactory) {
        
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    @Override
    public void run(String... args) throws InterruptedException {
        demonstrateAcknowledgmentModes();
        Thread.sleep(3000);
    }

    private void demonstrateAcknowledgmentModes() {
        System.out.println("=== Message Acknowledgment Pattern ===\n");

        // Test 1: Auto acknowledgment
        System.out.println("1. Auto Acknowledgment Mode:");
        System.out.println("   - Message automatically acknowledged upon receipt");
        ProcessingTask task1 = new ProcessingTask("AUTO-001", "Auto ack task", true);
        rabbitTemplate.convertAndSend(ACK_EXCHANGE, "auto.ack", task1);
        System.out.println("   - Sent: " + task1.getTaskId() + "\n");

        // Test 2: Manual acknowledgment - success
        System.out.println("2. Manual Acknowledgment (Success):");
        System.out.println("   - Message manually acknowledged after processing");
        ProcessingTask task2 = new ProcessingTask("MANUAL-001", "Manual ack task", true);
        rabbitTemplate.convertAndSend(ACK_EXCHANGE, "manual.ack", task2);
        System.out.println("   - Sent: " + task2.getTaskId() + "\n");

        // Test 3: Manual negative acknowledgment
        System.out.println("3. Manual Negative Acknowledgment:");
        System.out.println("   - Message processing fails, NACK with requeue");
        ProcessingTask task3 = new ProcessingTask("NACK-001", "Will fail", false);
        rabbitTemplate.convertAndSend(ACK_EXCHANGE, "manual.nack", task3);
        System.out.println("   - Sent: " + task3.getTaskId() + "\n");

        // Test 4: Message rejection
        System.out.println("4. Message Rejection:");
        System.out.println("   - Message rejected without requeue");
        ProcessingTask task4 = new ProcessingTask("REJECT-001", "Will be rejected", false);
        rabbitTemplate.convertAndSend(ACK_EXCHANGE, "reject", task4);
        System.out.println("   - Sent: " + task4.getTaskId() + "\n");

        System.out.println("Acknowledgment Modes:");
        System.out.println("- AUTO: Automatic ack on delivery");
        System.out.println("- MANUAL: Explicit ack/nack required");
        System.out.println("- NONE: No acknowledgment (fire and forget)\n");
    }

    /**
     * Message listeners with different acknowledgment modes
     */
    @Component
    static class AcknowledgmentListeners {

        /**
         * Auto acknowledgment listener
         */
        @RabbitListener(queues = "ack-auto-queue", 
                       containerFactory = "autoAckContainerFactory")
        public void handleAutoAck(ProcessingTask task) {
            System.out.println("[Auto ACK] Processing: " + task.getTaskId());
            System.out.println("  - Message auto-acknowledged on receipt");
            System.out.println("  - Processing: " + task.getDescription());
            
            if (!task.isSuccess()) {
                System.out.println("  - Processing failed but message already acknowledged!");
            } else {
                System.out.println("  - Processing successful");
            }
        }

        /**
         * Manual acknowledgment - success case
         */
        @RabbitListener(queues = "ack-manual-queue", 
                       containerFactory = "manualAckContainerFactory")
        public void handleManualAck(ProcessingTask task, Channel channel,
                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
            
            try {
                System.out.println("\n[Manual ACK] Processing: " + task.getTaskId());
                System.out.println("  - Delivery Tag: " + deliveryTag);
                
                // Simulate processing
                if (task.isSuccess()) {
                    System.out.println("  - Processing successful");
                    
                    // Manually acknowledge
                    channel.basicAck(deliveryTag, false);
                    System.out.println("  - Message acknowledged (basicAck)");
                } else {
                    throw new RuntimeException("Processing failed");
                }
                
            } catch (Exception e) {
                System.err.println("  - Error: " + e.getMessage());
                try {
                    // Negative acknowledgment without requeue
                    channel.basicNack(deliveryTag, false, false);
                    System.out.println("  - Message negatively acknowledged (basicNack)");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * Manual negative acknowledgment with requeue
         */
        @RabbitListener(queues = "ack-manual-nack-queue", 
                       containerFactory = "manualAckContainerFactory")
        public void handleManualNack(ProcessingTask task, Channel channel,
                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                @Header(AmqpHeaders.REDELIVERED) boolean redelivered) {
            
            try {
                System.out.println("\n[Manual NACK] Processing: " + task.getTaskId());
                System.out.println("  - Delivery Tag: " + deliveryTag);
                System.out.println("  - Redelivered: " + redelivered);
                
                if (redelivered) {
                    // Already tried once, don't requeue again
                    System.out.println("  - Message already redelivered, rejecting");
                    channel.basicNack(deliveryTag, false, false);
                    return;
                }
                
                if (!task.isSuccess()) {
                    throw new RuntimeException("Processing failed");
                }
                
                channel.basicAck(deliveryTag, false);
                System.out.println("  - Message acknowledged");
                
            } catch (Exception e) {
                System.err.println("  - Processing error: " + e.getMessage());
                try {
                    // Negative acknowledgment WITH requeue
                    channel.basicNack(deliveryTag, false, true);
                    System.out.println("  - Message requeued for retry (basicNack with requeue)");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * Message rejection using basicReject
         */
        @RabbitListener(queues = "ack-reject-queue", 
                       containerFactory = "manualAckContainerFactory")
        public void handleReject(ProcessingTask task, Channel channel,
                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
            
            try {
                System.out.println("\n[Reject] Processing: " + task.getTaskId());
                System.out.println("  - Delivery Tag: " + deliveryTag);
                
                if (!task.isSuccess()) {
                    // Reject without requeue (basicReject can only reject one message)
                    channel.basicReject(deliveryTag, false);
                    System.out.println("  - Message rejected (basicReject)");
                    System.out.println("  - Message will be discarded or sent to DLX");
                } else {
                    channel.basicAck(deliveryTag, false);
                    System.out.println("  - Message acknowledged");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processing task class
     */
    static class ProcessingTask implements java.io.Serializable {
        private String taskId;
        private String description;
        private boolean success;

        public ProcessingTask() {}

        public ProcessingTask(String taskId, String description, boolean success) {
            this.taskId = taskId;
            this.description = description;
            this.success = success;
        }

        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}
