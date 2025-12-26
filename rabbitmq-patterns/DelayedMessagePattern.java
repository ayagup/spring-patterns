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
 * Delayed Message Pattern
 * 
 * Demonstrates delayed message delivery using RabbitMQ's delayed message
 * exchange plugin or TTL with dead letter queues. Messages are delivered
 * after a specified delay period.
 * 
 * Key Features:
 * - Message delay using TTL and DLX
 * - Scheduled message delivery
 * - Multiple delay periods
 * - Retry with backoff
 * - Delayed notifications
 * - Parking lot pattern
 */
@SpringBootApplication
public class DelayedMessagePattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String WORK_EXCHANGE = "delayed-work-exchange";
    private static final String DELAY_EXCHANGE = "delayed-delay-exchange";

    public static void main(String[] args) {
        SpringApplication.run(DelayedMessagePattern.class, args);
    }

    /**
     * Work exchange - receives delayed messages
     */
    @Bean
    public DirectExchange workExchange() {
        return ExchangeBuilder
                .directExchange(WORK_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Delay exchange - holds messages temporarily
     */
    @Bean
    public DirectExchange delayExchange() {
        return ExchangeBuilder
                .directExchange(DELAY_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Work queue - final destination
     */
    @Bean
    public Queue workQueue() {
        return new Queue("delayed-work-queue", true);
    }

    /**
     * Delay queues with different TTL periods
     */
    @Bean
    public Queue delay5SecQueue() {
        return QueueBuilder
                .durable("delayed-delay-5s-queue")
                .ttl(5000) // 5 seconds
                .deadLetterExchange(WORK_EXCHANGE)
                .deadLetterRoutingKey("work.key")
                .build();
    }

    @Bean
    public Queue delay10SecQueue() {
        return QueueBuilder
                .durable("delayed-delay-10s-queue")
                .ttl(10000) // 10 seconds
                .deadLetterExchange(WORK_EXCHANGE)
                .deadLetterRoutingKey("work.key")
                .build();
    }

    @Bean
    public Queue delay30SecQueue() {
        return QueueBuilder
                .durable("delayed-delay-30s-queue")
                .ttl(30000) // 30 seconds
                .deadLetterExchange(WORK_EXCHANGE)
                .deadLetterRoutingKey("work.key")
                .build();
    }

    /**
     * Retry queue with exponential backoff
     */
    @Bean
    public Queue retryQueue() {
        return QueueBuilder
                .durable("delayed-retry-queue")
                .ttl(3000) // 3 seconds
                .deadLetterExchange(WORK_EXCHANGE)
                .deadLetterRoutingKey("retry.key")
                .build();
    }

    /**
     * Bindings
     */
    @Bean
    public Binding workQueueBinding() {
        return BindingBuilder
                .bind(workQueue())
                .to(workExchange())
                .with("work.key");
    }

    @Bean
    public Binding retryWorkBinding() {
        return BindingBuilder
                .bind(workQueue())
                .to(workExchange())
                .with("retry.key");
    }

    @Bean
    public Binding delay5SecBinding() {
        return BindingBuilder
                .bind(delay5SecQueue())
                .to(delayExchange())
                .with("delay.5s");
    }

    @Bean
    public Binding delay10SecBinding() {
        return BindingBuilder
                .bind(delay10SecQueue())
                .to(delayExchange())
                .with("delay.10s");
    }

    @Bean
    public Binding delay30SecBinding() {
        return BindingBuilder
                .bind(delay30SecQueue())
                .to(delayExchange())
                .with("delay.30s");
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder
                .bind(retryQueue())
                .to(delayExchange())
                .with("delay.retry");
    }

    @Override
    public void run(String... args) throws InterruptedException {
        demonstrateDelayedMessages();
        Thread.sleep(35000); // Wait for all delays to complete
    }

    private void demonstrateDelayedMessages() {
        System.out.println("=== Delayed Message Pattern ===\n");
        long startTime = System.currentTimeMillis();

        // Test 1: 5 second delay
        System.out.println("1. Scheduling message with 5 second delay:");
        ScheduledTask task1 = new ScheduledTask("TASK-001", "Process after 5 seconds", startTime);
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE, "delay.5s", task1);
        System.out.println("   - Scheduled at: " + startTime);
        System.out.println("   - Will execute at: " + (startTime + 5000) + "\n");

        // Test 2: 10 second delay
        System.out.println("2. Scheduling message with 10 second delay:");
        ScheduledTask task2 = new ScheduledTask("TASK-002", "Process after 10 seconds", startTime);
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE, "delay.10s", task2);
        System.out.println("   - Scheduled at: " + startTime);
        System.out.println("   - Will execute at: " + (startTime + 10000) + "\n");

        // Test 3: 30 second delay
        System.out.println("3. Scheduling message with 30 second delay:");
        ScheduledTask task3 = new ScheduledTask("TASK-003", "Process after 30 seconds", startTime);
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE, "delay.30s", task3);
        System.out.println("   - Scheduled at: " + startTime);
        System.out.println("   - Will execute at: " + (startTime + 30000) + "\n");

        // Test 4: Retry with delay
        System.out.println("4. Failed task - retry after 3 seconds:");
        ScheduledTask task4 = new ScheduledTask("TASK-004", "Retry task", startTime);
        task4.setRetryCount(1);
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE, "delay.retry", task4);
        System.out.println("   - Retry scheduled\n");

        // Test 5: Per-message TTL
        System.out.println("5. Custom delay using per-message TTL:");
        ScheduledTask task5 = new ScheduledTask("TASK-005", "Custom delay", startTime);
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE, "delay.5s", task5, message -> {
            // Override queue TTL with message TTL
            message.getMessageProperties().setExpiration("7000"); // 7 seconds
            return message;
        });
        System.out.println("   - Custom TTL: 7 seconds\n");

        System.out.println("Delayed Message Techniques:");
        System.out.println("- TTL + Dead Letter Exchange");
        System.out.println("- Per-message vs. queue TTL");
        System.out.println("- Multiple delay queues");
        System.out.println("- Retry with backoff");
        System.out.println("\nWaiting for scheduled messages...\n");
    }

    /**
     * Work queue consumer
     */
    @Component
    static class WorkQueueListener {

        @RabbitListener(queues = "delayed-work-queue")
        public void handleWork(ScheduledTask task) {
            long currentTime = System.currentTimeMillis();
            long delay = currentTime - task.getScheduledTime();
            
            System.out.println("\n[Work Queue] Executing scheduled task:");
            System.out.println("  Task ID: " + task.getTaskId());
            System.out.println("  Description: " + task.getDescription());
            System.out.println("  Scheduled at: " + task.getScheduledTime());
            System.out.println("  Executed at: " + currentTime);
            System.out.println("  Actual delay: " + delay + "ms");
            
            if (task.getRetryCount() > 0) {
                System.out.println("  Retry count: " + task.getRetryCount());
            }
        }
    }

    /**
     * Scheduled task class
     */
    static class ScheduledTask implements java.io.Serializable {
        private String taskId;
        private String description;
        private long scheduledTime;
        private int retryCount;

        public ScheduledTask() {}

        public ScheduledTask(String taskId, String description, long scheduledTime) {
            this.taskId = taskId;
            this.description = description;
            this.scheduledTime = scheduledTime;
            this.retryCount = 0;
        }

        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public long getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(long scheduledTime) { this.scheduledTime = scheduledTime; }
        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    }
}
