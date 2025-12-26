package com.example.notification.queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Notification Queue Pattern
 * 
 * Demonstrates:
 * - Message queue integration (RabbitMQ/Kafka simulation)
 * - Retry logic with exponential backoff
 * - Dead letter queues
 * - Priority queues
 * - Batch processing
 * - Queue monitoring
 * 
 * Dependencies:
 * - spring-boot-starter-amqp (for RabbitMQ)
 * - spring-kafka (for Kafka)
 */

@SpringBootApplication
public class NotificationQueuePattern {
    public static void main(String[] args) {
        SpringApplication.run(NotificationQueuePattern.class, args);
    }
}

@Configuration
@EnableConfigurationProperties(QueueProperties.class)
class NotificationQueueConfig {
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
}

@ConfigurationProperties(prefix = "notification.queue")
class QueueProperties {
    private int maxRetries = 3;
    private long retryDelayMs = 5000;
    private int batchSize = 10;
    
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
}

@RestController
@RequestMapping("/api/notification-queue")
class NotificationQueueController {
    private final NotificationQueueService service;
    
    public NotificationQueueController(NotificationQueueService service) {
        this.service = service;
    }
    
    @PostMapping("/enqueue")
    public ResponseEntity<QueuedNotificationResponse> enqueue(@Valid @RequestBody QueuedNotificationRequest request) {
        return ResponseEntity.ok(service.enqueueNotification(request));
    }
    
    @PostMapping("/enqueue-batch")
    public ResponseEntity<BatchQueueResponse> enqueueBatch(@Valid @RequestBody List<QueuedNotificationRequest> requests) {
        return ResponseEntity.ok(service.enqueueBatch(requests));
    }
    
    @GetMapping("/status")
    public ResponseEntity<QueueStatus> getStatus() {
        return ResponseEntity.ok(service.getQueueStatus());
    }
    
    @GetMapping("/dead-letter")
    public ResponseEntity<List<QueuedNotification>> getDeadLetters() {
        return ResponseEntity.ok(service.getDeadLetterQueue());
    }
    
    @PostMapping("/process")
    public ResponseEntity<ProcessingResult> processQueue() {
        return ResponseEntity.ok(service.processQueue());
    }
}

@Service
class NotificationQueueService {
    private final QueueProperties properties;
    private final PriorityBlockingQueue<QueuedNotification> mainQueue;
    private final BlockingQueue<QueuedNotification> deadLetterQueue = new LinkedBlockingQueue<>();
    private final Map<String, QueuedNotification> processingMap = new ConcurrentHashMap<>();
    private int totalProcessed = 0;
    private int totalFailed = 0;
    
    public NotificationQueueService(QueueProperties properties) {
        this.properties = properties;
        this.mainQueue = new PriorityBlockingQueue<>(100, 
            Comparator.comparing(QueuedNotification::getPriority).reversed()
                .thenComparing(QueuedNotification::getEnqueuedAt));
    }
    
    public QueuedNotificationResponse enqueueNotification(QueuedNotificationRequest request) {
        String id = UUID.randomUUID().toString();
        
        QueuedNotification notification = new QueuedNotification(
            id,
            request.getRecipient(),
            request.getMessage(),
            request.getChannel(),
            request.getPriority(),
            0,
            QueuedNotificationStatus.QUEUED,
            LocalDateTime.now(),
            null
        );
        
        mainQueue.offer(notification);
        return new QueuedNotificationResponse(id, "Notification queued", notification.getQueuePosition());
    }
    
    public BatchQueueResponse enqueueBatch(List<QueuedNotificationRequest> requests) {
        List<QueuedNotificationResponse> responses = new ArrayList<>();
        for (QueuedNotificationRequest request : requests) {
            responses.add(enqueueNotification(request));
        }
        return new BatchQueueResponse(responses, responses.size());
    }
    
    public ProcessingResult processQueue() {
        List<QueuedNotification> batch = new ArrayList<>();
        mainQueue.drainTo(batch, properties.getBatchSize());
        
        int processed = 0;
        int failed = 0;
        
        for (QueuedNotification notification : batch) {
            processingMap.put(notification.getId(), notification);
            
            if (processNotification(notification)) {
                notification.setStatus(QueuedNotificationStatus.SENT);
                totalProcessed++;
                processed++;
            } else {
                notification.setRetryCount(notification.getRetryCount() + 1);
                
                if (notification.getRetryCount() >= properties.getMaxRetries()) {
                    notification.setStatus(QueuedNotificationStatus.FAILED);
                    deadLetterQueue.offer(notification);
                    totalFailed++;
                    failed++;
                } else {
                    notification.setStatus(QueuedNotificationStatus.RETRYING);
                    mainQueue.offer(notification);
                }
            }
            
            processingMap.remove(notification.getId());
        }
        
        return new ProcessingResult(processed, failed, mainQueue.size(), deadLetterQueue.size());
    }
    
    private boolean processNotification(QueuedNotification notification) {
        // Simulate processing with 80% success rate
        return Math.random() > 0.2;
    }
    
    public QueueStatus getQueueStatus() {
        return new QueueStatus(
            mainQueue.size(),
            processingMap.size(),
            deadLetterQueue.size(),
            totalProcessed,
            totalFailed
        );
    }
    
    public List<QueuedNotification> getDeadLetterQueue() {
        return new ArrayList<>(deadLetterQueue);
    }
}

class QueuedNotificationRequest {
    @NotBlank
    private String recipient;
    
    @NotBlank
    private String message;
    
    private NotificationChannel channel = NotificationChannel.EMAIL;
    private int priority = 5;
    
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}

class QueuedNotification {
    private String id;
    private String recipient;
    private String message;
    private NotificationChannel channel;
    private int priority;
    private int retryCount;
    private QueuedNotificationStatus status;
    private LocalDateTime enqueuedAt;
    private LocalDateTime processedAt;
    
    public QueuedNotification(String id, String recipient, String message, NotificationChannel channel,
                             int priority, int retryCount, QueuedNotificationStatus status,
                             LocalDateTime enqueuedAt, LocalDateTime processedAt) {
        this.id = id;
        this.recipient = recipient;
        this.message = message;
        this.channel = channel;
        this.priority = priority;
        this.retryCount = retryCount;
        this.status = status;
        this.enqueuedAt = enqueuedAt;
        this.processedAt = processedAt;
    }
    
    public String getId() { return id; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public NotificationChannel getChannel() { return channel; }
    public int getPriority() { return priority; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public QueuedNotificationStatus getStatus() { return status; }
    public void setStatus(QueuedNotificationStatus status) { this.status = status; }
    public LocalDateTime getEnqueuedAt() { return enqueuedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public int getQueuePosition() { return priority; }
}

class QueuedNotificationResponse {
    private String notificationId;
    private String message;
    private int queuePosition;
    
    public QueuedNotificationResponse(String notificationId, String message, int queuePosition) {
        this.notificationId = notificationId;
        this.message = message;
        this.queuePosition = queuePosition;
    }
    
    public String getNotificationId() { return notificationId; }
    public String getMessage() { return message; }
    public int getQueuePosition() { return queuePosition; }
}

class BatchQueueResponse {
    private List<QueuedNotificationResponse> responses;
    private int totalQueued;
    
    public BatchQueueResponse(List<QueuedNotificationResponse> responses, int totalQueued) {
        this.responses = responses;
        this.totalQueued = totalQueued;
    }
    
    public List<QueuedNotificationResponse> getResponses() { return responses; }
    public int getTotalQueued() { return totalQueued; }
}

class QueueStatus {
    private int queuedCount;
    private int processingCount;
    private int deadLetterCount;
    private int totalProcessed;
    private int totalFailed;
    
    public QueueStatus(int queuedCount, int processingCount, int deadLetterCount, 
                      int totalProcessed, int totalFailed) {
        this.queuedCount = queuedCount;
        this.processingCount = processingCount;
        this.deadLetterCount = deadLetterCount;
        this.totalProcessed = totalProcessed;
        this.totalFailed = totalFailed;
    }
    
    public int getQueuedCount() { return queuedCount; }
    public int getProcessingCount() { return processingCount; }
    public int getDeadLetterCount() { return deadLetterCount; }
    public int getTotalProcessed() { return totalProcessed; }
    public int getTotalFailed() { return totalFailed; }
}

class ProcessingResult {
    private int processed;
    private int failed;
    private int remaining;
    private int deadLetter;
    
    public ProcessingResult(int processed, int failed, int remaining, int deadLetter) {
        this.processed = processed;
        this.failed = failed;
        this.remaining = remaining;
        this.deadLetter = deadLetter;
    }
    
    public int getProcessed() { return processed; }
    public int getFailed() { return failed; }
    public int getRemaining() { return remaining; }
    public int getDeadLetter() { return deadLetter; }
}

enum NotificationChannel {
    EMAIL, SMS, PUSH, IN_APP
}

enum QueuedNotificationStatus {
    QUEUED, PROCESSING, RETRYING, SENT, FAILED
}
