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
 * Dead Letter Exchange Pattern
 * 
 * Demonstrates the Dead Letter Exchange (DLX) mechanism for handling
 * rejected, expired, or failed messages. Messages are automatically
 * routed to a DLX when specific conditions are met.
 * 
 * Key Features:
 * - Automatic message routing on failure
 * - TTL-based message expiry
 * - Rejection handling
 * - Maximum retry attempts
 * - Error message analysis
 * - Message recovery strategies
 */
@SpringBootApplication
public class DeadLetterExchangePattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String MAIN_EXCHANGE = "dlx-main-exchange";
    private static final String DLX_EXCHANGE = "dlx-dead-letter-exchange";

    public static void main(String[] args) {
        SpringApplication.run(DeadLetterExchangePattern.class, args);
    }

    /**
     * Main exchange
     */
    @Bean
    public DirectExchange mainExchange() {
        return ExchangeBuilder
                .directExchange(MAIN_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Dead Letter Exchange
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Main queue with DLX configuration
     */
    @Bean
    public Queue mainQueue() {
        return QueueBuilder
                .durable("dlx-main-queue")
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("dlx.dead-letter")
                .ttl(10000) // 10 seconds message TTL
                .build();
    }

    /**
     * Queue with max length for overflow testing
     */
    @Bean
    public Queue overflowQueue() {
        return QueueBuilder
                .durable("dlx-overflow-queue")
                .maxLength(5)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("dlx.overflow")
                .build();
    }

    /**
     * Queue with retry limit
     */
    @Bean
    public Queue retryQueue() {
        return QueueBuilder
                .durable("dlx-retry-queue")
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("dlx.retry-exceeded")
                .build();
    }

    /**
     * Dead letter queue for expired messages
     */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue("dlx-dead-letter-queue", true);
    }

    /**
     * Dead letter queue for overflow
     */
    @Bean
    public Queue overflowDeadLetterQueue() {
        return new Queue("dlx-overflow-dead-letter-queue", true);
    }

    /**
     * Dead letter queue for retry exceeded
     */
    @Bean
    public Queue retryDeadLetterQueue() {
        return new Queue("dlx-retry-exceeded-queue", true);
    }

    /**
     * Bindings
     */
    @Bean
    public Binding mainQueueBinding() {
        return BindingBuilder
                .bind(mainQueue())
                .to(mainExchange())
                .with("main.key");
    }

    @Bean
    public Binding overflowQueueBinding() {
        return BindingBuilder
                .bind(overflowQueue())
                .to(mainExchange())
                .with("overflow.key");
    }

    @Bean
    public Binding retryQueueBinding() {
        return BindingBuilder
                .bind(retryQueue())
                .to(mainExchange())
                .with("retry.key");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlx.dead-letter");
    }

    @Bean
    public Binding overflowDeadLetterBinding() {
        return BindingBuilder
                .bind(overflowDeadLetterQueue())
                .to(deadLetterExchange())
                .with("dlx.overflow");
    }

    @Bean
    public Binding retryDeadLetterBinding() {
        return BindingBuilder
                .bind(retryDeadLetterQueue())
                .to(deadLetterExchange())
                .with("dlx.retry-exceeded");
    }

    @Override
    public void run(String... args) throws InterruptedException {
        demonstrateDeadLetterExchange();
        Thread.sleep(15000); // Wait for TTL expiry
    }

    private void demonstrateDeadLetterExchange() {
        System.out.println("=== Dead Letter Exchange Pattern ===\n");

        // Test 1: Message with TTL expiry
        System.out.println("1. Sending message with TTL (will expire in 10 seconds):");
        TaskMessage task1 = new TaskMessage("TASK-001", "Process with expiry", 0);
        rabbitTemplate.convertAndSend(MAIN_EXCHANGE, "main.key", task1);
        System.out.println("   - Message sent to main queue (TTL: 10s)");
        System.out.println("   - Will route to DLX after expiry\n");

        // Test 2: Queue overflow
        System.out.println("2. Causing queue overflow (max 5 messages):");
        for (int i = 1; i <= 7; i++) {
            TaskMessage task = new TaskMessage("OVERFLOW-" + i, "Overflow test", 0);
            rabbitTemplate.convertAndSend(MAIN_EXCHANGE, "overflow.key", task);
        }
        System.out.println("   - Sent 7 messages to queue with max 5");
        System.out.println("   - Oldest 2 messages routed to overflow DLX\n");

        // Test 3: Rejected message (simulated via retry exceeded)
        System.out.println("3. Sending message that will be rejected:");
        TaskMessage task3 = new TaskMessage("REJECT-001", "Will be rejected", 3);
        rabbitTemplate.convertAndSend(MAIN_EXCHANGE, "retry.key", task3);
        System.out.println("   - Message sent to retry queue");
        System.out.println("   - Will be rejected after max retries\n");

        System.out.println("Dead Letter Exchange Scenarios:");
        System.out.println("- Message TTL expiration");
        System.out.println("- Queue length exceeded");
        System.out.println("- Message rejected by consumer");
        System.out.println("- Consumer negative acknowledgment");
        System.out.println("\nWaiting for TTL expiry (10 seconds)...");
    }

    /**
     * Message listeners
     */
    @Component
    static class TaskListeners {

        // Main queue consumer (not consuming to trigger TTL)
        // Commented out to let messages expire
        /*
        @RabbitListener(queues = "dlx-main-queue")
        public void handleMainQueue(TaskMessage task) {
            System.out.println("[Main Queue] Processing: " + task.getTaskId());
        }
        */

        /**
         * Retry queue with rejection logic
         */
        @RabbitListener(queues = "dlx-retry-queue")
        public void handleRetryQueue(TaskMessage task, 
                org.springframework.amqp.core.Channel channel,
                @org.springframework.messaging.handler.annotation.Header(
                    org.springframework.amqp.support.AmqpHeaders.DELIVERY_TAG) long tag) {
            
            try {
                System.out.println("[Retry Queue] Attempt: " + task.getRetryCount());
                
                if (task.getRetryCount() >= 3) {
                    // Reject after max retries - routes to DLX
                    channel.basicReject(tag, false);
                    System.out.println("  - Rejected: Max retries exceeded\n");
                } else {
                    task.setRetryCount(task.getRetryCount() + 1);
                    channel.basicAck(tag, false);
                    // Re-queue for retry
                    // In real scenario, would send back to queue
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Dead letter queue handler
         */
        @RabbitListener(queues = "dlx-dead-letter-queue")
        public void handleDeadLetter(TaskMessage task,
                @org.springframework.messaging.handler.annotation.Headers java.util.Map<String, Object> headers) {
            
            System.out.println("\n[Dead Letter Queue] Received expired message:");
            System.out.println("  Task: " + task.getTaskId());
            System.out.println("  Description: " + task.getDescription());
            System.out.println("  Death reason: " + headers.get("x-first-death-reason"));
            System.out.println("  Death queue: " + headers.get("x-first-death-queue"));
            System.out.println("  Death exchange: " + headers.get("x-first-death-exchange"));
        }

        /**
         * Overflow dead letter handler
         */
        @RabbitListener(queues = "dlx-overflow-dead-letter-queue")
        public void handleOverflowDeadLetter(TaskMessage task) {
            System.out.println("[Overflow DLX] Received: " + task.getTaskId());
            System.out.println("  - Reason: Queue length exceeded");
        }

        /**
         * Retry exceeded handler
         */
        @RabbitListener(queues = "dlx-retry-exceeded-queue")
        public void handleRetryExceeded(TaskMessage task) {
            System.out.println("\n[Retry Exceeded DLX] Maximum retries reached:");
            System.out.println("  Task: " + task.getTaskId());
            System.out.println("  Retry count: " + task.getRetryCount());
            System.out.println("  Action: Manual intervention required");
        }
    }

    /**
     * Task message class
     */
    static class TaskMessage implements java.io.Serializable {
        private String taskId;
        private String description;
        private int retryCount;

        public TaskMessage() {}

        public TaskMessage(String taskId, String description, int retryCount) {
            this.taskId = taskId;
            this.description = description;
            this.retryCount = retryCount;
        }

        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    }
}
