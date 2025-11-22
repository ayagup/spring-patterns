package com.example.sse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Long Polling Pattern
 * 
 * Purpose: Implement long polling for near real-time updates with reduced server load.
 * Client makes a request and server holds it until data is available or timeout occurs.
 * 
 * Key Features:
 * - Reduced network overhead vs short polling
 * - Near real-time updates
 * - DeferredResult for async response handling
 * - Timeout management
 * - Automatic retry mechanism
 * - Message queue integration
 * 
 * Differences from SSE:
 * - Bidirectional compatibility (works with all HTTP clients)
 * - Request-response model (one update per request)
 * - Client initiates each request
 * - Better firewall/proxy compatibility
 * 
 * Use Cases:
 * - Chat applications
 * - Notification systems
 * - Real-time updates with legacy browser support
 * - Systems behind restrictive proxies
 * - Mobile applications
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LongPollingPattern {

    public static void main(String[] args) {
        SpringApplication.run(LongPollingPattern.class, args);
    }

    /**
     * Configuration
     */
    @Configuration
    public static class LongPollingConfig {
        
        @Bean
        public ExecutorService executorService() {
            return Executors.newCachedThreadPool();
        }
    }

    /**
     * Long Polling Controller
     */
    @RestController
    @RequestMapping("/api/poll")
    public static class LongPollingController {

        private final LongPollingService pollingService;

        public LongPollingController(LongPollingService pollingService) {
            this.pollingService = pollingService;
        }

        /**
         * Basic long polling endpoint
         */
        @GetMapping("/messages")
        public DeferredResult<ResponseEntity<Message>> pollMessages(
                @RequestParam(required = false) String lastMessageId,
                @RequestParam(defaultValue = "30000") long timeout) {
            
            DeferredResult<ResponseEntity<Message>> deferredResult = 
                new DeferredResult<>(timeout);

            // Set timeout handler
            deferredResult.onTimeout(() -> {
                deferredResult.setResult(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
            });

            // Set error handler
            deferredResult.onError((Throwable t) -> {
                deferredResult.setResult(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                );
            });

            // Set completion handler
            deferredResult.onCompletion(() -> {
                System.out.println("Long polling request completed");
            });

            // Register the deferred result
            pollingService.registerMessagePoll(lastMessageId, deferredResult);

            return deferredResult;
        }

        /**
         * Channel-specific long polling
         */
        @GetMapping("/channels/{channel}")
        public DeferredResult<ResponseEntity<ChannelMessage>> pollChannel(
                @PathVariable String channel,
                @RequestParam(required = false) Long lastTimestamp,
                @RequestParam(defaultValue = "45000") long timeout) {
            
            DeferredResult<ResponseEntity<ChannelMessage>> deferredResult = 
                new DeferredResult<>(timeout);

            deferredResult.onTimeout(() -> {
                deferredResult.setResult(ResponseEntity.noContent().build());
            });

            deferredResult.onError((Throwable t) -> {
                deferredResult.setResult(ResponseEntity.internalServerError().build());
            });

            pollingService.registerChannelPoll(channel, lastTimestamp, deferredResult);

            return deferredResult;
        }

        /**
         * User-specific notifications polling
         */
        @GetMapping("/notifications/{userId}")
        public DeferredResult<ResponseEntity<List<Notification>>> pollNotifications(
                @PathVariable String userId,
                @RequestParam(required = false) Long since,
                @RequestParam(defaultValue = "60000") long timeout) {
            
            DeferredResult<ResponseEntity<List<Notification>>> deferredResult = 
                new DeferredResult<>(timeout);

            deferredResult.onTimeout(() -> {
                deferredResult.setResult(ResponseEntity.ok(Collections.emptyList()));
            });

            pollingService.registerNotificationPoll(userId, since, deferredResult);

            return deferredResult;
        }

        /**
         * Task status polling
         */
        @GetMapping("/tasks/{taskId}/status")
        public DeferredResult<ResponseEntity<TaskStatus>> pollTaskStatus(
                @PathVariable String taskId,
                @RequestParam(defaultValue = "120000") long timeout) {
            
            DeferredResult<ResponseEntity<TaskStatus>> deferredResult = 
                new DeferredResult<>(timeout);

            deferredResult.onTimeout(() -> {
                // Return current status on timeout
                TaskStatus status = pollingService.getTaskStatus(taskId);
                if (status != null) {
                    deferredResult.setResult(ResponseEntity.ok(status));
                } else {
                    deferredResult.setResult(ResponseEntity.notFound().build());
                }
            });

            pollingService.registerTaskPoll(taskId, deferredResult);

            return deferredResult;
        }

        /**
         * Send message (for testing)
         */
        @PostMapping("/messages")
        public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest request) {
            Message message = pollingService.sendMessage(
                request.getContent(),
                request.getSender()
            );
            return ResponseEntity.ok(message);
        }

        /**
         * Send channel message
         */
        @PostMapping("/channels/{channel}/messages")
        public ResponseEntity<ChannelMessage> sendChannelMessage(
                @PathVariable String channel,
                @RequestBody MessageRequest request) {
            
            ChannelMessage message = pollingService.sendChannelMessage(
                channel,
                request.getContent(),
                request.getSender()
            );
            return ResponseEntity.ok(message);
        }

        /**
         * Send notification
         */
        @PostMapping("/notifications/{userId}")
        public ResponseEntity<Notification> sendNotification(
                @PathVariable String userId,
                @RequestBody NotificationRequest request) {
            
            Notification notification = pollingService.sendNotification(
                userId,
                request.getTitle(),
                request.getMessage(),
                request.getType()
            );
            return ResponseEntity.ok(notification);
        }

        /**
         * Update task status
         */
        @PutMapping("/tasks/{taskId}/status")
        public ResponseEntity<TaskStatus> updateTaskStatus(
                @PathVariable String taskId,
                @RequestBody TaskStatusUpdate update) {
            
            TaskStatus status = pollingService.updateTaskStatus(
                taskId,
                update.getStatus(),
                update.getProgress()
            );
            return ResponseEntity.ok(status);
        }

        /**
         * Get polling statistics
         */
        @GetMapping("/stats")
        public ResponseEntity<PollingStats> getStats() {
            return ResponseEntity.ok(pollingService.getStats());
        }
    }

    /**
     * Long Polling Service
     */
    @Service
    public static class LongPollingService {

        private final Map<String, Queue<Message>> messageQueues = new ConcurrentHashMap<>();
        private final Map<String, Queue<ChannelMessage>> channelQueues = new ConcurrentHashMap<>();
        private final Map<String, Queue<Notification>> notificationQueues = new ConcurrentHashMap<>();
        private final Map<String, TaskStatus> taskStatuses = new ConcurrentHashMap<>();
        
        private final List<DeferredResult<ResponseEntity<Message>>> messagePollRequests = 
            new CopyOnWriteArrayList<>();
        private final Map<String, List<DeferredResult<ResponseEntity<ChannelMessage>>>> channelPollRequests = 
            new ConcurrentHashMap<>();
        private final Map<String, List<DeferredResult<ResponseEntity<List<Notification>>>>> notificationPollRequests = 
            new ConcurrentHashMap<>();
        private final Map<String, List<DeferredResult<ResponseEntity<TaskStatus>>>> taskPollRequests = 
            new ConcurrentHashMap<>();

        private long totalRequests = 0;
        private long successfulPolls = 0;
        private long timeouts = 0;

        /**
         * Register message poll
         */
        public void registerMessagePoll(String lastMessageId, 
                                       DeferredResult<ResponseEntity<Message>> deferredResult) {
            totalRequests++;
            
            // Check if there's a pending message
            Queue<Message> queue = messageQueues.computeIfAbsent("global", k -> new ConcurrentLinkedQueue<>());
            Message message = queue.poll();
            
            if (message != null) {
                deferredResult.setResult(ResponseEntity.ok(message));
                successfulPolls++;
            } else {
                messagePollRequests.add(deferredResult);
                
                // Remove from list when completed
                deferredResult.onCompletion(() -> {
                    messagePollRequests.remove(deferredResult);
                });
                
                deferredResult.onTimeout(() -> {
                    messagePollRequests.remove(deferredResult);
                    timeouts++;
                });
            }
        }

        /**
         * Register channel poll
         */
        public void registerChannelPoll(String channel, Long lastTimestamp,
                                       DeferredResult<ResponseEntity<ChannelMessage>> deferredResult) {
            totalRequests++;
            
            Queue<ChannelMessage> queue = channelQueues.computeIfAbsent(channel, k -> new ConcurrentLinkedQueue<>());
            ChannelMessage message = queue.poll();
            
            if (message != null) {
                deferredResult.setResult(ResponseEntity.ok(message));
                successfulPolls++;
            } else {
                channelPollRequests.computeIfAbsent(channel, k -> new CopyOnWriteArrayList<>())
                    .add(deferredResult);
                
                deferredResult.onCompletion(() -> {
                    List<DeferredResult<ResponseEntity<ChannelMessage>>> requests = channelPollRequests.get(channel);
                    if (requests != null) {
                        requests.remove(deferredResult);
                    }
                });
                
                deferredResult.onTimeout(() -> {
                    List<DeferredResult<ResponseEntity<ChannelMessage>>> requests = channelPollRequests.get(channel);
                    if (requests != null) {
                        requests.remove(deferredResult);
                    }
                    timeouts++;
                });
            }
        }

        /**
         * Register notification poll
         */
        public void registerNotificationPoll(String userId, Long since,
                                            DeferredResult<ResponseEntity<List<Notification>>> deferredResult) {
            totalRequests++;
            
            Queue<Notification> queue = notificationQueues.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>());
            
            if (!queue.isEmpty()) {
                List<Notification> notifications = new ArrayList<>();
                Notification notification;
                while ((notification = queue.poll()) != null) {
                    notifications.add(notification);
                }
                deferredResult.setResult(ResponseEntity.ok(notifications));
                successfulPolls++;
            } else {
                notificationPollRequests.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
                    .add(deferredResult);
                
                deferredResult.onCompletion(() -> {
                    List<DeferredResult<ResponseEntity<List<Notification>>>> requests = notificationPollRequests.get(userId);
                    if (requests != null) {
                        requests.remove(deferredResult);
                    }
                });
                
                deferredResult.onTimeout(() -> {
                    List<DeferredResult<ResponseEntity<List<Notification>>>> requests = notificationPollRequests.get(userId);
                    if (requests != null) {
                        requests.remove(deferredResult);
                    }
                    timeouts++;
                });
            }
        }

        /**
         * Register task poll
         */
        public void registerTaskPoll(String taskId,
                                    DeferredResult<ResponseEntity<TaskStatus>> deferredResult) {
            totalRequests++;
            
            TaskStatus status = taskStatuses.get(taskId);
            
            if (status != null && status.isCompleted()) {
                deferredResult.setResult(ResponseEntity.ok(status));
                successfulPolls++;
            } else {
                taskPollRequests.computeIfAbsent(taskId, k -> new CopyOnWriteArrayList<>())
                    .add(deferredResult);
                
                deferredResult.onCompletion(() -> {
                    List<DeferredResult<ResponseEntity<TaskStatus>>> requests = taskPollRequests.get(taskId);
                    if (requests != null) {
                        requests.remove(deferredResult);
                    }
                });
                
                deferredResult.onTimeout(() -> {
                    List<DeferredResult<ResponseEntity<TaskStatus>>> requests = taskPollRequests.get(taskId);
                    if (requests != null) {
                        requests.remove(deferredResult);
                    }
                    timeouts++;
                });
            }
        }

        /**
         * Send message
         */
        public Message sendMessage(String content, String sender) {
            Message message = new Message(
                UUID.randomUUID().toString(),
                content,
                sender,
                LocalDateTime.now()
            );

            // Try to fulfill pending polls first
            if (!messagePollRequests.isEmpty()) {
                DeferredResult<ResponseEntity<Message>> deferredResult = messagePollRequests.remove(0);
                deferredResult.setResult(ResponseEntity.ok(message));
                successfulPolls++;
            } else {
                // Queue message for future polls
                messageQueues.computeIfAbsent("global", k -> new ConcurrentLinkedQueue<>())
                    .offer(message);
            }

            return message;
        }

        /**
         * Send channel message
         */
        public ChannelMessage sendChannelMessage(String channel, String content, String sender) {
            ChannelMessage message = new ChannelMessage(
                UUID.randomUUID().toString(),
                channel,
                content,
                sender,
                System.currentTimeMillis(),
                LocalDateTime.now()
            );

            List<DeferredResult<ResponseEntity<ChannelMessage>>> requests = channelPollRequests.get(channel);
            
            if (requests != null && !requests.isEmpty()) {
                DeferredResult<ResponseEntity<ChannelMessage>> deferredResult = requests.remove(0);
                deferredResult.setResult(ResponseEntity.ok(message));
                successfulPolls++;
            } else {
                channelQueues.computeIfAbsent(channel, k -> new ConcurrentLinkedQueue<>())
                    .offer(message);
            }

            return message;
        }

        /**
         * Send notification
         */
        public Notification sendNotification(String userId, String title, String message, String type) {
            Notification notification = new Notification(
                UUID.randomUUID().toString(),
                userId,
                title,
                message,
                type,
                System.currentTimeMillis(),
                LocalDateTime.now()
            );

            List<DeferredResult<ResponseEntity<List<Notification>>>> requests = notificationPollRequests.get(userId);
            
            if (requests != null && !requests.isEmpty()) {
                DeferredResult<ResponseEntity<List<Notification>>> deferredResult = requests.remove(0);
                deferredResult.setResult(ResponseEntity.ok(Arrays.asList(notification)));
                successfulPolls++;
            } else {
                notificationQueues.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>())
                    .offer(notification);
            }

            return notification;
        }

        /**
         * Update task status
         */
        public TaskStatus updateTaskStatus(String taskId, String status, int progress) {
            TaskStatus taskStatus = taskStatuses.computeIfAbsent(taskId, 
                k -> new TaskStatus(taskId, "PENDING", 0, LocalDateTime.now()));
            
            taskStatus.setStatus(status);
            taskStatus.setProgress(progress);
            taskStatus.setLastUpdated(LocalDateTime.now());

            // Notify waiting polls if task completed
            if (taskStatus.isCompleted()) {
                List<DeferredResult<ResponseEntity<TaskStatus>>> requests = taskPollRequests.get(taskId);
                if (requests != null) {
                    for (DeferredResult<ResponseEntity<TaskStatus>> deferredResult : requests) {
                        deferredResult.setResult(ResponseEntity.ok(taskStatus));
                        successfulPolls++;
                    }
                    requests.clear();
                }
            }

            return taskStatus;
        }

        /**
         * Get task status
         */
        public TaskStatus getTaskStatus(String taskId) {
            return taskStatuses.get(taskId);
        }

        /**
         * Get statistics
         */
        public PollingStats getStats() {
            int activePollsCount = messagePollRequests.size() + 
                channelPollRequests.values().stream().mapToInt(List::size).sum() +
                notificationPollRequests.values().stream().mapToInt(List::size).sum() +
                taskPollRequests.values().stream().mapToInt(List::size).sum();

            return new PollingStats(
                totalRequests,
                successfulPolls,
                timeouts,
                activePollsCount,
                messageQueues.values().stream().mapToInt(Queue::size).sum(),
                LocalDateTime.now()
            );
        }

        /**
         * Clean up old messages (scheduled task)
         */
        @Scheduled(fixedRate = 60000)
        public void cleanupOldMessages() {
            // Remove empty queues
            messageQueues.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            channelQueues.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            notificationQueues.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            
            System.out.println("Cleanup completed. Active queues: " + 
                (messageQueues.size() + channelQueues.size() + notificationQueues.size()));
        }
    }

    // Model Classes

    public static class Message {
        private String id;
        private String content;
        private String sender;
        private LocalDateTime timestamp;

        public Message() {}

        public Message(String id, String content, String sender, LocalDateTime timestamp) {
            this.id = id;
            this.content = content;
            this.sender = sender;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class ChannelMessage {
        private String id;
        private String channel;
        private String content;
        private String sender;
        private long timestampMillis;
        private LocalDateTime timestamp;

        public ChannelMessage() {}

        public ChannelMessage(String id, String channel, String content, String sender, 
                             long timestampMillis, LocalDateTime timestamp) {
            this.id = id;
            this.channel = channel;
            this.content = content;
            this.sender = sender;
            this.timestampMillis = timestampMillis;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        public long getTimestampMillis() { return timestampMillis; }
        public void setTimestampMillis(long timestampMillis) { this.timestampMillis = timestampMillis; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class Notification {
        private String id;
        private String userId;
        private String title;
        private String message;
        private String type;
        private long timestampMillis;
        private LocalDateTime timestamp;

        public Notification() {}

        public Notification(String id, String userId, String title, String message, 
                          String type, long timestampMillis, LocalDateTime timestamp) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestampMillis = timestampMillis;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public long getTimestampMillis() { return timestampMillis; }
        public void setTimestampMillis(long timestampMillis) { this.timestampMillis = timestampMillis; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class TaskStatus {
        private String taskId;
        private String status;
        private int progress;
        private LocalDateTime lastUpdated;

        public TaskStatus() {}

        public TaskStatus(String taskId, String status, int progress, LocalDateTime lastUpdated) {
            this.taskId = taskId;
            this.status = status;
            this.progress = progress;
            this.lastUpdated = lastUpdated;
        }

        public boolean isCompleted() {
            return "COMPLETED".equals(status) || "FAILED".equals(status) || "CANCELLED".equals(status);
        }

        // Getters and Setters
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    public static class MessageRequest {
        private String content;
        private String sender;

        // Getters and Setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
    }

    public static class NotificationRequest {
        private String title;
        private String message;
        private String type;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class TaskStatusUpdate {
        private String status;
        private int progress;

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
    }

    public static class PollingStats {
        private long totalRequests;
        private long successfulPolls;
        private long timeouts;
        private int activePolls;
        private int queuedMessages;
        private LocalDateTime timestamp;

        public PollingStats(long totalRequests, long successfulPolls, long timeouts,
                          int activePolls, int queuedMessages, LocalDateTime timestamp) {
            this.totalRequests = totalRequests;
            this.successfulPolls = successfulPolls;
            this.timeouts = timeouts;
            this.activePolls = activePolls;
            this.queuedMessages = queuedMessages;
            this.timestamp = timestamp;
        }

        // Getters
        public long getTotalRequests() { return totalRequests; }
        public long getSuccessfulPolls() { return successfulPolls; }
        public long getTimeouts() { return timeouts; }
        public int getActivePolls() { return activePolls; }
        public int getQueuedMessages() { return queuedMessages; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}

/*
 * Client-Side JavaScript Example:
 * 
 * // Long polling function
 * function longPoll(url, lastMessageId) {
 *     const params = lastMessageId ? `?lastMessageId=${lastMessageId}` : '';
 *     
 *     fetch(url + params)
 *         .then(response => {
 *             if (response.status === 200) {
 *                 return response.json();
 *             } else if (response.status === 204) {
 *                 // No content (timeout), retry
 *                 return null;
 *             }
 *             throw new Error('Polling failed');
 *         })
 *         .then(data => {
 *             if (data) {
 *                 console.log('Received message:', data);
 *                 handleMessage(data);
 *                 // Continue polling with new message ID
 *                 longPoll(url, data.id);
 *             } else {
 *                 // Timeout, retry immediately
 *                 longPoll(url, lastMessageId);
 *             }
 *         })
 *         .catch(error => {
 *             console.error('Polling error:', error);
 *             // Retry after delay
 *             setTimeout(() => longPoll(url, lastMessageId), 5000);
 *         });
 * }
 * 
 * function handleMessage(message) {
 *     console.log('Message:', message.content, 'from', message.sender);
 * }
 * 
 * // Start long polling
 * longPoll('/api/poll/messages');
 * 
 * // Channel-specific polling
 * function pollChannel(channel) {
 *     let lastTimestamp = null;
 *     
 *     function poll() {
 *         const params = lastTimestamp ? `?lastTimestamp=${lastTimestamp}` : '';
 *         
 *         fetch(`/api/poll/channels/${channel}${params}`)
 *             .then(response => response.ok ? response.json() : null)
 *             .then(data => {
 *                 if (data) {
 *                     console.log('Channel message:', data);
 *                     lastTimestamp = data.timestampMillis;
 *                 }
 *                 poll(); // Continue polling
 *             })
 *             .catch(error => {
 *                 console.error('Error:', error);
 *                 setTimeout(poll, 5000);
 *             });
 *     }
 *     
 *     poll();
 * }
 * 
 * pollChannel('general');
 */
