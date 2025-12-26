package com.example.email.async;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Async Email Pattern
 * 
 * Demonstrates:
 * - Asynchronous email sending with @Async
 * - Thread pool configuration for email tasks
 * - CompletableFuture for async operations
 * - Email queue management
 * - Batch email processing
 * - Email retry mechanism
 * - Non-blocking email operations
 * - Task status tracking
 * - Priority-based email queue
 * 
 * Dependencies:
 * - spring-boot-starter-mail
 * - spring-boot-starter-web
 */

@SpringBootApplication
@EnableAsync
public class AsyncEmailPattern {
    public static void main(String[] args) {
        SpringApplication.run(AsyncEmailPattern.class, args);
    }
}

@Configuration
@EnableConfigurationProperties(AsyncMailProperties.class)
class AsyncMailConfig {
    
    @Bean
    public JavaMailSender javaMailSender(AsyncMailProperties properties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(properties.getHost());
        mailSender.setPort(properties.getPort());
        mailSender.setUsername(properties.getUsername());
        mailSender.setPassword(properties.getPassword());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        return mailSender;
    }
    
    @Bean(name = "emailTaskExecutor")
    public ThreadPoolTaskExecutor emailTaskExecutor(AsyncMailProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix("email-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}

@ConfigurationProperties(prefix = "async.mail")
class AsyncMailProperties {
    private String host = "smtp.gmail.com";
    private int port = 587;
    private String username;
    private String password;
    private String from = "noreply@example.com";
    private int corePoolSize = 5;
    private int maxPoolSize = 10;
    private int queueCapacity = 100;
    private int maxRetries = 3;
    private long retryDelayMs = 5000;
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public int getCorePoolSize() { return corePoolSize; }
    public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
}

@RestController
@RequestMapping("/api/async-email")
class AsyncEmailController {
    
    private final AsyncEmailService asyncEmailService;
    
    public AsyncEmailController(AsyncEmailService asyncEmailService) {
        this.asyncEmailService = asyncEmailService;
    }
    
    @PostMapping("/send")
    public ResponseEntity<AsyncEmailResponse> sendAsync(
            @Valid @RequestBody AsyncEmailRequest request) {
        String taskId = asyncEmailService.sendEmailAsync(request);
        return ResponseEntity.ok(new AsyncEmailResponse(taskId, AsyncTaskStatus.QUEUED, 
            "Email queued for async delivery", LocalDateTime.now()));
    }
    
    @PostMapping("/send-batch")
    public ResponseEntity<BatchAsyncResponse> sendBatchAsync(
            @Valid @RequestBody List<AsyncEmailRequest> requests) {
        List<String> taskIds = asyncEmailService.sendBatchEmailsAsync(requests);
        return ResponseEntity.ok(new BatchAsyncResponse(taskIds, 
            "Batch emails queued for delivery", LocalDateTime.now()));
    }
    
    @PostMapping("/send-priority")
    public ResponseEntity<AsyncEmailResponse> sendPriorityAsync(
            @Valid @RequestBody PriorityEmailRequest request) {
        String taskId = asyncEmailService.sendPriorityEmailAsync(request);
        return ResponseEntity.ok(new AsyncEmailResponse(taskId, AsyncTaskStatus.QUEUED,
            "Priority email queued", LocalDateTime.now()));
    }
    
    @GetMapping("/status/{taskId}")
    public ResponseEntity<EmailTaskStatus> getTaskStatus(@PathVariable String taskId) {
        return asyncEmailService.getTaskStatus(taskId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/queue-status")
    public ResponseEntity<QueueStatus> getQueueStatus() {
        return ResponseEntity.ok(asyncEmailService.getQueueStatus());
    }
    
    @GetMapping("/stats")
    public ResponseEntity<AsyncEmailStats> getStats() {
        return ResponseEntity.ok(asyncEmailService.getStats());
    }
}

@Service
class AsyncEmailService {
    
    private final JavaMailSender mailSender;
    private final AsyncMailProperties properties;
    private final ThreadPoolTaskExecutor executor;
    private final Map<String, EmailTaskStatus> taskStatuses = new ConcurrentHashMap<>();
    private final PriorityBlockingQueue<EmailTask> priorityQueue = 
        new PriorityBlockingQueue<>(100, Comparator.comparingInt(EmailTask::getPriority).reversed());
    private int totalQueued = 0;
    private int totalSent = 0;
    private int totalFailed = 0;
    
    public AsyncEmailService(JavaMailSender mailSender, AsyncMailProperties properties,
                            ThreadPoolTaskExecutor executor) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.executor = executor;
    }
    
    public String sendEmailAsync(AsyncEmailRequest request) {
        String taskId = UUID.randomUUID().toString();
        totalQueued++;
        
        CompletableFuture.runAsync(() -> {
            updateTaskStatus(taskId, AsyncTaskStatus.PROCESSING, "Sending email...");
            sendEmailWithRetry(taskId, request, 0);
        }, executor);
        
        updateTaskStatus(taskId, AsyncTaskStatus.QUEUED, "Email queued");
        return taskId;
    }
    
    public List<String> sendBatchEmailsAsync(List<AsyncEmailRequest> requests) {
        List<String> taskIds = new ArrayList<>();
        
        for (AsyncEmailRequest request : requests) {
            String taskId = sendEmailAsync(request);
            taskIds.add(taskId);
        }
        
        return taskIds;
    }
    
    public String sendPriorityEmailAsync(PriorityEmailRequest request) {
        String taskId = UUID.randomUUID().toString();
        totalQueued++;
        
        EmailTask task = new EmailTask(taskId, request.toAsyncEmailRequest(), 
                                       request.getPriority());
        priorityQueue.offer(task);
        
        CompletableFuture.runAsync(() -> {
            try {
                EmailTask nextTask = priorityQueue.take();
                updateTaskStatus(nextTask.getTaskId(), AsyncTaskStatus.PROCESSING, 
                               "Sending priority email...");
                sendEmailWithRetry(nextTask.getTaskId(), nextTask.getRequest(), 0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                updateTaskStatus(taskId, AsyncTaskStatus.FAILED, "Task interrupted");
            }
        }, executor);
        
        updateTaskStatus(taskId, AsyncTaskStatus.QUEUED, "Priority email queued");
        return taskId;
    }
    
    @Async("emailTaskExecutor")
    private void sendEmailWithRetry(String taskId, AsyncEmailRequest request, int attempt) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(properties.getFrom());
            message.setTo(request.getTo());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            
            if (request.getCc() != null && !request.getCc().isEmpty()) {
                message.setCc(request.getCc().toArray(new String[0]));
            }
            
            mailSender.send(message);
            totalSent++;
            updateTaskStatus(taskId, AsyncTaskStatus.COMPLETED, "Email sent successfully");
            
        } catch (Exception e) {
            if (attempt < properties.getMaxRetries()) {
                updateTaskStatus(taskId, AsyncTaskStatus.RETRYING, 
                    "Retry attempt " + (attempt + 1) + " of " + properties.getMaxRetries());
                
                try {
                    Thread.sleep(properties.getRetryDelayMs());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                
                sendEmailWithRetry(taskId, request, attempt + 1);
            } else {
                totalFailed++;
                updateTaskStatus(taskId, AsyncTaskStatus.FAILED, 
                    "Failed after " + properties.getMaxRetries() + " retries: " + e.getMessage());
            }
        }
    }
    
    private void updateTaskStatus(String taskId, AsyncTaskStatus status, String message) {
        EmailTaskStatus taskStatus = taskStatuses.getOrDefault(taskId, 
            new EmailTaskStatus(taskId, status, message, LocalDateTime.now()));
        
        taskStatus.setStatus(status);
        taskStatus.setMessage(message);
        taskStatus.setLastUpdated(LocalDateTime.now());
        
        taskStatuses.put(taskId, taskStatus);
    }
    
    public Optional<EmailTaskStatus> getTaskStatus(String taskId) {
        return Optional.ofNullable(taskStatuses.get(taskId));
    }
    
    public QueueStatus getQueueStatus() {
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor.getThreadPoolExecutor();
        return new QueueStatus(
            tpe.getActiveCount(),
            tpe.getQueue().size(),
            priorityQueue.size(),
            tpe.getCompletedTaskCount()
        );
    }
    
    public AsyncEmailStats getStats() {
        return new AsyncEmailStats(totalQueued, totalSent, totalFailed, 
            taskStatuses.size(), properties.getCorePoolSize(), properties.getMaxPoolSize());
    }
}

// Models
class AsyncEmailRequest {
    @Email @NotBlank private String to;
    private List<@Email String> cc;
    @NotBlank private String subject;
    @NotBlank private String body;
    
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public List<String> getCc() { return cc; }
    public void setCc(List<String> cc) { this.cc = cc; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}

class PriorityEmailRequest extends AsyncEmailRequest {
    private int priority = 5; // 1-10, higher is more important
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public AsyncEmailRequest toAsyncEmailRequest() {
        AsyncEmailRequest request = new AsyncEmailRequest();
        request.setTo(this.getTo());
        request.setCc(this.getCc());
        request.setSubject(this.getSubject());
        request.setBody(this.getBody());
        return request;
    }
}

class EmailTask {
    private String taskId;
    private AsyncEmailRequest request;
    private int priority;
    
    public EmailTask(String taskId, AsyncEmailRequest request, int priority) {
        this.taskId = taskId;
        this.request = request;
        this.priority = priority;
    }
    
    public String getTaskId() { return taskId; }
    public AsyncEmailRequest getRequest() { return request; }
    public int getPriority() { return priority; }
}

class EmailTaskStatus {
    private String taskId;
    private AsyncTaskStatus status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    
    public EmailTaskStatus(String taskId, AsyncTaskStatus status, String message, LocalDateTime createdAt) {
        this.taskId = taskId;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
        this.lastUpdated = createdAt;
    }
    
    public String getTaskId() { return taskId; }
    public AsyncTaskStatus getStatus() { return status; }
    public void setStatus(AsyncTaskStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}

class AsyncEmailResponse {
    private String taskId;
    private AsyncTaskStatus status;
    private String message;
    private LocalDateTime timestamp;
    
    public AsyncEmailResponse(String taskId, AsyncTaskStatus status, String message, LocalDateTime timestamp) {
        this.taskId = taskId;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public String getTaskId() { return taskId; }
    public AsyncTaskStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class BatchAsyncResponse {
    private List<String> taskIds;
    private String message;
    private LocalDateTime timestamp;
    
    public BatchAsyncResponse(List<String> taskIds, String message, LocalDateTime timestamp) {
        this.taskIds = taskIds;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public List<String> getTaskIds() { return taskIds; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class QueueStatus {
    private int activeThreads;
    private int queuedTasks;
    private int priorityQueueSize;
    private long completedTasks;
    
    public QueueStatus(int activeThreads, int queuedTasks, int priorityQueueSize, long completedTasks) {
        this.activeThreads = activeThreads;
        this.queuedTasks = queuedTasks;
        this.priorityQueueSize = priorityQueueSize;
        this.completedTasks = completedTasks;
    }
    
    public int getActiveThreads() { return activeThreads; }
    public int getQueuedTasks() { return queuedTasks; }
    public int getPriorityQueueSize() { return priorityQueueSize; }
    public long getCompletedTasks() { return completedTasks; }
}

class AsyncEmailStats {
    private int totalQueued;
    private int totalSent;
    private int totalFailed;
    private int totalTasks;
    private int corePoolSize;
    private int maxPoolSize;
    
    public AsyncEmailStats(int totalQueued, int totalSent, int totalFailed, int totalTasks,
                          int corePoolSize, int maxPoolSize) {
        this.totalQueued = totalQueued;
        this.totalSent = totalSent;
        this.totalFailed = totalFailed;
        this.totalTasks = totalTasks;
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
    }
    
    public int getTotalQueued() { return totalQueued; }
    public int getTotalSent() { return totalSent; }
    public int getTotalFailed() { return totalFailed; }
    public int getTotalTasks() { return totalTasks; }
    public int getCorePoolSize() { return corePoolSize; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public double getSuccessRate() {
        return totalQueued == 0 ? 0 : (double) totalSent / totalQueued * 100;
    }
}

enum AsyncTaskStatus {
    QUEUED,
    PROCESSING,
    RETRYING,
    COMPLETED,
    FAILED
}

/*
 * Application Properties:
 * 
 * async.mail.host=smtp.gmail.com
 * async.mail.port=587
 * async.mail.username=your-email@gmail.com
 * async.mail.password=your-password
 * async.mail.from=noreply@example.com
 * async.mail.core-pool-size=5
 * async.mail.max-pool-size=10
 * async.mail.queue-capacity=100
 * async.mail.max-retries=3
 * async.mail.retry-delay-ms=5000
 * 
 * Usage Examples:
 * 
 * 1. Send Async Email:
 * POST /api/async-email/send
 * {
 *   "to": "user@example.com",
 *   "subject": "Async Test",
 *   "body": "This email is sent asynchronously"
 * }
 * Response: {"taskId": "abc-123", "status": "QUEUED"}
 * 
 * 2. Check Task Status:
 * GET /api/async-email/status/abc-123
 * 
 * 3. Send Priority Email:
 * POST /api/async-email/send-priority
 * {
 *   "to": "urgent@example.com",
 *   "subject": "Urgent",
 *   "body": "High priority email",
 *   "priority": 10
 * }
 * 
 * 4. Get Queue Status:
 * GET /api/async-email/queue-status
 */
