package com.example.redis.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Redis Messaging Pattern
 * 
 * Demonstrates Redis as a message broker for application messaging.
 * Redis Messaging provides:
 * - Message queuing with lists
 * - Reliable message delivery
 * - Message persistence
 * - Consumer groups (Redis Streams)
 * - Priority queues
 * - Dead letter queues
 * 
 * Use cases:
 * - Task queuing
 * - Job processing
 * - Event-driven architecture
 * - Asynchronous processing
 * - Inter-service communication
 * - Notification systems
 */

@Configuration
class RedisMessagingConfig {
    
    @Bean
    public RedisTemplate<String, TaskMessage> taskRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, TaskMessage> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(TaskMessage.class));
        return template;
    }
    
    @Bean
    public ChannelTopic taskCompletionTopic() {
        return new ChannelTopic("task:completion");
    }
    
    @Bean
    public RedisMessageListenerContainer taskListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter taskCompletionListener) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(taskCompletionListener, taskCompletionTopic());
        return container;
    }
    
    @Bean
    public MessageListenerAdapter taskCompletionListener(TaskCompletionListener listener) {
        return new MessageListenerAdapter(listener, "handleMessage");
    }
}

record TaskMessage(
    String taskId,
    String taskType,
    String payload,
    int priority,
    LocalDateTime createdAt,
    String status
) {}

@Service
class TaskProducer {
    
    private final RedisTemplate<String, TaskMessage> redisTemplate;
    private static final String TASK_QUEUE = "tasks:queue";
    private static final String HIGH_PRIORITY_QUEUE = "tasks:high-priority";
    private static final String PROCESSING_QUEUE = "tasks:processing";
    
    public TaskProducer(RedisTemplate<String, TaskMessage> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void sendTask(TaskMessage task) {
        if (task.priority() > 5) {
            redisTemplate.opsForList().leftPush(HIGH_PRIORITY_QUEUE, task);
        } else {
            redisTemplate.opsForList().leftPush(TASK_QUEUE, task);
        }
    }
    
    public void sendTaskToFront(TaskMessage task) {
        redisTemplate.opsForList().rightPush(TASK_QUEUE, task);
    }
    
    public long getQueueSize() {
        Long size = redisTemplate.opsForList().size(TASK_QUEUE);
        return size != null ? size : 0;
    }
    
    public long getHighPriorityQueueSize() {
        Long size = redisTemplate.opsForList().size(HIGH_PRIORITY_QUEUE);
        return size != null ? size : 0;
    }
    
    public List<TaskMessage> peekTasks(int count) {
        return redisTemplate.opsForList().range(TASK_QUEUE, 0, count - 1);
    }
}

@Service
class TaskConsumer {
    
    private final RedisTemplate<String, TaskMessage> redisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private static final String TASK_QUEUE = "tasks:queue";
    private static final String HIGH_PRIORITY_QUEUE = "tasks:high-priority";
    private static final String PROCESSING_QUEUE = "tasks:processing";
    private static final String COMPLETED_QUEUE = "tasks:completed";
    private static final String FAILED_QUEUE = "tasks:failed";
    
    public TaskConsumer(RedisTemplate<String, TaskMessage> redisTemplate,
                       RedisTemplate<String, String> stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    public TaskMessage consumeTask() {
        // Try high-priority queue first
        TaskMessage task = redisTemplate.opsForList().rightPopAndLeftPush(
            HIGH_PRIORITY_QUEUE, PROCESSING_QUEUE
        );
        
        if (task == null) {
            // Fall back to normal queue
            task = redisTemplate.opsForList().rightPopAndLeftPush(
                TASK_QUEUE, PROCESSING_QUEUE
            );
        }
        
        return task;
    }
    
    public TaskMessage blockingConsumeTask(long timeoutSeconds) {
        TaskMessage task = redisTemplate.opsForList().rightPopAndLeftPush(
            HIGH_PRIORITY_QUEUE, PROCESSING_QUEUE,
            timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS
        );
        
        if (task == null) {
            task = redisTemplate.opsForList().rightPopAndLeftPush(
                TASK_QUEUE, PROCESSING_QUEUE,
                timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS
            );
        }
        
        return task;
    }
    
    public void markTaskCompleted(TaskMessage task) {
        // Remove from processing queue
        redisTemplate.opsForList().remove(PROCESSING_QUEUE, 1, task);
        
        // Add to completed queue
        TaskMessage completed = new TaskMessage(
            task.taskId(),
            task.taskType(),
            task.payload(),
            task.priority(),
            task.createdAt(),
            "COMPLETED"
        );
        redisTemplate.opsForList().leftPush(COMPLETED_QUEUE, completed);
        
        // Publish completion event
        stringRedisTemplate.convertAndSend("task:completion", task.taskId());
    }
    
    public void markTaskFailed(TaskMessage task, String error) {
        // Remove from processing queue
        redisTemplate.opsForList().remove(PROCESSING_QUEUE, 1, task);
        
        // Add to failed queue (dead letter queue)
        TaskMessage failed = new TaskMessage(
            task.taskId(),
            task.taskType(),
            task.payload() + " | Error: " + error,
            task.priority(),
            task.createdAt(),
            "FAILED"
        );
        redisTemplate.opsForList().leftPush(FAILED_QUEUE, failed);
    }
    
    public long getProcessingCount() {
        Long size = redisTemplate.opsForList().size(PROCESSING_QUEUE);
        return size != null ? size : 0;
    }
    
    public long getCompletedCount() {
        Long size = redisTemplate.opsForList().size(COMPLETED_QUEUE);
        return size != null ? size : 0;
    }
    
    public long getFailedCount() {
        Long size = redisTemplate.opsForList().size(FAILED_QUEUE);
        return size != null ? size : 0;
    }
}

@Component
class TaskCompletionListener {
    
    private final List<String> completedTaskIds = new CopyOnWriteArrayList<>();
    
    public void handleMessage(String taskId) {
        System.out.println("Task completed: " + taskId);
        completedTaskIds.add(taskId);
    }
    
    public List<String> getCompletedTaskIds() {
        return List.copyOf(completedTaskIds);
    }
}

@RestController
@RequestMapping("/api/redis/messaging")
class RedisMessagingController {
    
    private final TaskProducer taskProducer;
    private final TaskConsumer taskConsumer;
    private final TaskCompletionListener taskCompletionListener;
    
    public RedisMessagingController(TaskProducer taskProducer,
                                   TaskConsumer taskConsumer,
                                   TaskCompletionListener taskCompletionListener) {
        this.taskProducer = taskProducer;
        this.taskConsumer = taskConsumer;
        this.taskCompletionListener = taskCompletionListener;
    }
    
    @PostMapping("/tasks")
    public String sendTask(@RequestBody TaskMessage task) {
        taskProducer.sendTask(task);
        return "Task sent to queue";
    }
    
    @GetMapping("/tasks/consume")
    public TaskMessage consumeTask() {
        return taskConsumer.consumeTask();
    }
    
    @GetMapping("/tasks/consume/blocking")
    public TaskMessage blockingConsumeTask(@RequestParam(defaultValue = "5") long timeoutSeconds) {
        return taskConsumer.blockingConsumeTask(timeoutSeconds);
    }
    
    @PostMapping("/tasks/{taskId}/complete")
    public String markTaskCompleted(@RequestBody TaskMessage task) {
        taskConsumer.markTaskCompleted(task);
        return "Task marked as completed";
    }
    
    @PostMapping("/tasks/{taskId}/failed")
    public String markTaskFailed(@RequestBody TaskMessage task, @RequestParam String error) {
        taskConsumer.markTaskFailed(task, error);
        return "Task marked as failed";
    }
    
    @GetMapping("/tasks/stats")
    public Map<String, Object> getStats() {
        return Map.of(
            "queueSize", taskProducer.getQueueSize(),
            "highPriorityQueueSize", taskProducer.getHighPriorityQueueSize(),
            "processingCount", taskConsumer.getProcessingCount(),
            "completedCount", taskConsumer.getCompletedCount(),
            "failedCount", taskConsumer.getFailedCount()
        );
    }
    
    @GetMapping("/tasks/peek")
    public List<TaskMessage> peekTasks(@RequestParam(defaultValue = "10") int count) {
        return taskProducer.peekTasks(count);
    }
    
    @GetMapping("/tasks/completed")
    public List<String> getCompletedTasks() {
        return taskCompletionListener.getCompletedTaskIds();
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return """
                Redis Messaging Pattern
                ======================
                Features:
                - Task queuing with priority
                - Reliable message delivery
                - Processing queue for in-flight tasks
                - Completed and failed queues
                - Blocking and non-blocking consumption
                - Pub/Sub for completion events
                
                Queues:
                - tasks:queue: Normal priority tasks
                - tasks:high-priority: High priority tasks
                - tasks:processing: Tasks being processed
                - tasks:completed: Successfully completed tasks
                - tasks:failed: Failed tasks (dead letter queue)
                
                Operations:
                - POST /tasks: Send task to queue
                - GET /tasks/consume: Consume next task
                - GET /tasks/consume/blocking: Wait for task
                - POST /tasks/{id}/complete: Mark completed
                - POST /tasks/{id}/failed: Mark failed
                - GET /tasks/stats: Queue statistics
                - GET /tasks/peek: Preview tasks
                
                Pattern: RPOPLPUSH for reliable processing
                """;
    }
}
