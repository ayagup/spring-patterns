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
 * Priority Queue Pattern
 * 
 * Demonstrates priority queue functionality where messages with higher
 * priority are consumed before lower priority messages. Enables critical
 * message processing and task prioritization.
 * 
 * Key Features:
 * - Message priority levels (0-255, higher is more important)
 * - Priority-based consumption order
 * - Queue max priority configuration
 * - Priority for urgent tasks
 * - SLA-based processing
 */
@SpringBootApplication
public class PriorityQueuePattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String PRIORITY_EXCHANGE = "priority-exchange";

    public static void main(String[] args) {
        SpringApplication.run(PriorityQueuePattern.class, args);
    }

    /**
     * Exchange for priority messages
     */
    @Bean
    public DirectExchange priorityExchange() {
        return ExchangeBuilder
                .directExchange(PRIORITY_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Priority queue with max priority of 10
     */
    @Bean
    public Queue taskPriorityQueue() {
        return QueueBuilder
                .durable("priority-task-queue")
                .maxPriority(10) // Supports priorities 0-10
                .build();
    }

    /**
     * Priority queue for orders (0-255 priority)
     */
    @Bean
    public Queue orderPriorityQueue() {
        return QueueBuilder
                .durable("priority-order-queue")
                .maxPriority(255) // Full priority range
                .build();
    }

    /**
     * Bindings
     */
    @Bean
    public Binding taskPriorityBinding() {
        return BindingBuilder
                .bind(taskPriorityQueue())
                .to(priorityExchange())
                .with("task.priority");
    }

    @Bean
    public Binding orderPriorityBinding() {
        return BindingBuilder
                .bind(orderPriorityQueue())
                .to(priorityExchange())
                .with("order.priority");
    }

    @Override
    public void run(String... args) throws InterruptedException {
        demonstratePriorityQueue();
        Thread.sleep(3000); // Wait for message processing
    }

    private void demonstratePriorityQueue() {
        System.out.println("=== Priority Queue Pattern ===\n");

        System.out.println("Sending tasks with different priorities (0-10):\n");

        // Send low priority tasks first
        sendTask("TASK-001", "Low priority task", 1);
        sendTask("TASK-002", "Low priority task", 2);
        
        // Send medium priority tasks
        sendTask("TASK-003", "Medium priority task", 5);
        sendTask("TASK-004", "Medium priority task", 5);
        
        // Send high priority tasks
        sendTask("TASK-005", "High priority task", 8);
        sendTask("TASK-006", "High priority task", 9);
        
        // Send critical priority task
        sendTask("TASK-007", "CRITICAL priority task", 10);

        System.out.println("\nExpected processing order (highest priority first):");
        System.out.println("1. TASK-007 (Priority 10) - CRITICAL");
        System.out.println("2. TASK-006 (Priority 9)  - High");
        System.out.println("3. TASK-005 (Priority 8)  - High");
        System.out.println("4. TASK-003 (Priority 5)  - Medium");
        System.out.println("5. TASK-004 (Priority 5)  - Medium");
        System.out.println("6. TASK-002 (Priority 2)  - Low");
        System.out.println("7. TASK-001 (Priority 1)  - Low\n");

        // Demonstrate order processing with priorities
        System.out.println("\nSending orders with various priorities (0-255):\n");
        
        sendOrder("ORD-001", "Regular order", 50);
        sendOrder("ORD-002", "VIP customer order", 200);
        sendOrder("ORD-003", "Bulk order", 30);
        sendOrder("ORD-004", "Emergency order", 255);
        sendOrder("ORD-005", "Premium customer", 180);

        System.out.println("\nPriority Queue Characteristics:");
        System.out.println("- Higher priority messages consumed first");
        System.out.println("- Priority range: 0 (lowest) to max-priority (highest)");
        System.out.println("- Max priority configured per queue");
        System.out.println("- Messages with same priority follow FIFO");
        System.out.println("- Ideal for SLA-based processing");
    }

    private void sendTask(String taskId, String description, int priority) {
        PriorityTask task = new PriorityTask(taskId, description, priority);
        
        rabbitTemplate.convertAndSend(PRIORITY_EXCHANGE, "task.priority", task, message -> {
            message.getMessageProperties().setPriority(priority);
            return message;
        });
        
        System.out.println("Sent: " + taskId + " (Priority " + priority + ") - " + description);
    }

    private void sendOrder(String orderId, String description, int priority) {
        PriorityOrder order = new PriorityOrder(orderId, description, priority);
        
        rabbitTemplate.convertAndSend(PRIORITY_EXCHANGE, "order.priority", order, message -> {
            message.getMessageProperties().setPriority(priority);
            return message;
        });
        
        String level = priority >= 200 ? "URGENT" : 
                      priority >= 100 ? "HIGH" : 
                      priority >= 50 ? "MEDIUM" : "NORMAL";
        
        System.out.println("Sent: " + orderId + " (Priority " + priority + " - " + level + ")");
    }

    /**
     * Task and Order listeners
     */
    @Component
    static class PriorityListeners {

        private int taskSequence = 1;
        private int orderSequence = 1;

        @RabbitListener(queues = "priority-task-queue")
        public void handlePriorityTask(PriorityTask task) {
            System.out.println("\n[Task Processor #" + taskSequence++ + "]");
            System.out.println("  Processing: " + task.getTaskId());
            System.out.println("  Priority: " + task.getPriority());
            System.out.println("  Description: " + task.getDescription());
            
            // Simulate processing time
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @RabbitListener(queues = "priority-order-queue")
        public void handlePriorityOrder(PriorityOrder order) {
            System.out.println("\n[Order Processor #" + orderSequence++ + "]");
            System.out.println("  Processing: " + order.getOrderId());
            System.out.println("  Priority: " + order.getPriority());
            System.out.println("  Description: " + order.getDescription());
            
            String level = order.getPriority() >= 200 ? "URGENT" : 
                          order.getPriority() >= 100 ? "HIGH" : 
                          order.getPriority() >= 50 ? "MEDIUM" : "NORMAL";
            System.out.println("  Level: " + level);
        }
    }

    /**
     * Priority task class
     */
    static class PriorityTask implements java.io.Serializable {
        private String taskId;
        private String description;
        private int priority;

        public PriorityTask() {}

        public PriorityTask(String taskId, String description, int priority) {
            this.taskId = taskId;
            this.description = description;
            this.priority = priority;
        }

        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
    }

    /**
     * Priority order class
     */
    static class PriorityOrder implements java.io.Serializable {
        private String orderId;
        private String description;
        private int priority;

        public PriorityOrder() {}

        public PriorityOrder(String orderId, String description, int priority) {
            this.orderId = orderId;
            this.description = description;
            this.priority = priority;
        }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
    }
}
