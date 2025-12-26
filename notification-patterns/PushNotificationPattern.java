package com.example.notification.push;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Push Notification Pattern
 * 
 * Demonstrates:
 * - Firebase Cloud Messaging (FCM) integration
 * - Apple Push Notification Service (APNS)
 * - Device token management
 * - Topic-based notifications
 * - Notification scheduling
 * - Platform-specific payloads (iOS, Android, Web)
 * - Silent notifications
 * - Rich notifications with images
 * 
 * Dependencies:
 * - firebase-admin (for FCM)
 * - spring-boot-starter-web
 */

@SpringBootApplication
public class PushNotificationPattern {
    public static void main(String[] args) {
        SpringApplication.run(PushNotificationPattern.class, args);
    }
}

@Configuration
@EnableConfigurationProperties(PushNotificationProperties.class)
class PushNotificationConfig {
    // Firebase Admin SDK initialization would go here
}

@ConfigurationProperties(prefix = "push.notification")
class PushNotificationProperties {
    private String firebaseCredentialsPath;
    private String apnsKeyPath;
    private String apnsTeamId;
    private String apnsKeyId;
    private boolean sandbox = true;
    
    public String getFirebaseCredentialsPath() { return firebaseCredentialsPath; }
    public void setFirebaseCredentialsPath(String firebaseCredentialsPath) { 
        this.firebaseCredentialsPath = firebaseCredentialsPath; 
    }
    public String getApnsKeyPath() { return apnsKeyPath; }
    public void setApnsKeyPath(String apnsKeyPath) { this.apnsKeyPath = apnsKeyPath; }
    public String getApnsTeamId() { return apnsTeamId; }
    public void setApnsTeamId(String apnsTeamId) { this.apnsTeamId = apnsTeamId; }
    public String getApnsKeyId() { return apnsKeyId; }
    public void setApnsKeyId(String apnsKeyId) { this.apnsKeyId = apnsKeyId; }
    public boolean isSandbox() { return sandbox; }
    public void setSandbox(boolean sandbox) { this.sandbox = sandbox; }
}

@RestController
@RequestMapping("/api/push-notifications")
class PushNotificationController {
    
    private final PushNotificationService pushNotificationService;
    
    public PushNotificationController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }
    
    @PostMapping("/send")
    public ResponseEntity<PushNotificationResponse> sendPushNotification(
            @Valid @RequestBody PushNotificationRequest request) {
        PushNotificationResponse response = pushNotificationService.sendPushNotification(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-topic")
    public ResponseEntity<PushNotificationResponse> sendToTopic(
            @Valid @RequestBody TopicNotificationRequest request) {
        PushNotificationResponse response = pushNotificationService.sendToTopic(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-batch")
    public ResponseEntity<BatchPushResponse> sendBatch(
            @Valid @RequestBody List<PushNotificationRequest> requests) {
        BatchPushResponse response = pushNotificationService.sendBatchNotifications(requests);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/devices/register")
    public ResponseEntity<DeviceRegistrationResponse> registerDevice(
            @Valid @RequestBody DeviceRegistrationRequest request) {
        pushNotificationService.registerDevice(request);
        return ResponseEntity.ok(new DeviceRegistrationResponse(request.getDeviceToken(), 
            "Device registered successfully"));
    }
    
    @PostMapping("/topics/subscribe")
    public ResponseEntity<String> subscribeToTopic(
            @RequestParam String deviceToken,
            @RequestParam String topic) {
        pushNotificationService.subscribeToTopic(deviceToken, topic);
        return ResponseEntity.ok("Subscribed to topic: " + topic);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<PushNotificationStats> getStats() {
        return ResponseEntity.ok(pushNotificationService.getStats());
    }
}

@Service
class PushNotificationService {
    
    private final Map<String, DeviceInfo> registeredDevices = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> topicSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, PushNotificationHistory> notificationHistory = new ConcurrentHashMap<>();
    private int totalSent = 0;
    private int totalFailed = 0;
    
    public PushNotificationResponse sendPushNotification(PushNotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            // Mock FCM/APNS send logic
            DeviceInfo device = registeredDevices.get(request.getDeviceToken());
            if (device == null) {
                throw new RuntimeException("Device not registered");
            }
            
            // Build platform-specific payload
            Map<String, Object> payload = buildPayload(request, device.getPlatform());
            
            // Simulate sending notification
            boolean success = sendToDevice(device, payload);
            
            if (success) {
                totalSent++;
                recordHistory(notificationId, request.getDeviceToken(), request.getTitle(),
                    PushNotificationStatus.SENT, sentAt, null);
                return new PushNotificationResponse(notificationId, PushNotificationStatus.SENT,
                    "Push notification sent successfully", sentAt);
            } else {
                throw new RuntimeException("Failed to send notification");
            }
            
        } catch (Exception e) {
            totalFailed++;
            recordHistory(notificationId, request.getDeviceToken(), request.getTitle(),
                PushNotificationStatus.FAILED, sentAt, e.getMessage());
            return new PushNotificationResponse(notificationId, PushNotificationStatus.FAILED,
                "Failed to send: " + e.getMessage(), sentAt);
        }
    }
    
    public PushNotificationResponse sendToTopic(TopicNotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            Set<String> subscribers = topicSubscriptions.getOrDefault(request.getTopic(), new HashSet<>());
            
            if (subscribers.isEmpty()) {
                throw new RuntimeException("No subscribers for topic: " + request.getTopic());
            }
            
            int successCount = 0;
            for (String deviceToken : subscribers) {
                try {
                    PushNotificationRequest deviceRequest = new PushNotificationRequest();
                    deviceRequest.setDeviceToken(deviceToken);
                    deviceRequest.setTitle(request.getTitle());
                    deviceRequest.setBody(request.getBody());
                    deviceRequest.setData(request.getData());
                    
                    PushNotificationResponse response = sendPushNotification(deviceRequest);
                    if (response.getStatus() == PushNotificationStatus.SENT) {
                        successCount++;
                    }
                } catch (Exception e) {
                    // Continue with next device
                }
            }
            
            recordHistory(notificationId, "topic:" + request.getTopic(), request.getTitle(),
                PushNotificationStatus.SENT, sentAt, "Sent to " + successCount + " devices");
            
            return new PushNotificationResponse(notificationId, PushNotificationStatus.SENT,
                "Sent to " + successCount + " of " + subscribers.size() + " subscribers", sentAt);
            
        } catch (Exception e) {
            totalFailed++;
            return new PushNotificationResponse(notificationId, PushNotificationStatus.FAILED,
                e.getMessage(), sentAt);
        }
    }
    
    public BatchPushResponse sendBatchNotifications(List<PushNotificationRequest> requests) {
        List<PushNotificationResponse> responses = new ArrayList<>();
        
        for (PushNotificationRequest request : requests) {
            responses.add(sendPushNotification(request));
        }
        
        long successCount = responses.stream()
            .filter(r -> r.getStatus() == PushNotificationStatus.SENT)
            .count();
        
        return new BatchPushResponse(responses, (int) successCount, 
            requests.size() - (int) successCount);
    }
    
    public void registerDevice(DeviceRegistrationRequest request) {
        DeviceInfo deviceInfo = new DeviceInfo(
            request.getDeviceToken(),
            request.getPlatform(),
            request.getUserId(),
            LocalDateTime.now()
        );
        registeredDevices.put(request.getDeviceToken(), deviceInfo);
    }
    
    public void subscribeToTopic(String deviceToken, String topic) {
        topicSubscriptions.computeIfAbsent(topic, k -> new HashSet<>()).add(deviceToken);
    }
    
    private Map<String, Object> buildPayload(PushNotificationRequest request, DevicePlatform platform) {
        Map<String, Object> payload = new HashMap<>();
        
        switch (platform) {
            case IOS:
                // APNS payload
                Map<String, Object> aps = new HashMap<>();
                Map<String, Object> alert = new HashMap<>();
                alert.put("title", request.getTitle());
                alert.put("body", request.getBody());
                aps.put("alert", alert);
                aps.put("sound", request.getSound() != null ? request.getSound() : "default");
                aps.put("badge", request.getBadge() != null ? request.getBadge() : 1);
                payload.put("aps", aps);
                if (request.getData() != null) {
                    payload.putAll(request.getData());
                }
                break;
                
            case ANDROID:
                // FCM payload
                Map<String, String> notification = new HashMap<>();
                notification.put("title", request.getTitle());
                notification.put("body", request.getBody());
                if (request.getImageUrl() != null) {
                    notification.put("image", request.getImageUrl());
                }
                payload.put("notification", notification);
                if (request.getData() != null) {
                    payload.put("data", request.getData());
                }
                payload.put("priority", request.getPriority() != null ? request.getPriority() : "high");
                break;
                
            case WEB:
                // Web push payload
                payload.put("title", request.getTitle());
                payload.put("body", request.getBody());
                if (request.getImageUrl() != null) {
                    payload.put("icon", request.getImageUrl());
                }
                if (request.getData() != null) {
                    payload.put("data", request.getData());
                }
                break;
        }
        
        return payload;
    }
    
    private boolean sendToDevice(DeviceInfo device, Map<String, Object> payload) {
        // Mock implementation - would actually send via FCM/APNS
        return true;
    }
    
    private void recordHistory(String notificationId, String target, String title,
                              PushNotificationStatus status, LocalDateTime sentAt, String message) {
        PushNotificationHistory history = new PushNotificationHistory(
            notificationId, target, title, status, sentAt, message
        );
        notificationHistory.put(notificationId, history);
    }
    
    public PushNotificationStats getStats() {
        return new PushNotificationStats(
            totalSent,
            totalFailed,
            registeredDevices.size(),
            topicSubscriptions.size()
        );
    }
}

// Models
class PushNotificationRequest {
    @NotBlank private String deviceToken;
    @NotBlank private String title;
    @NotBlank private String body;
    private String imageUrl;
    private String sound;
    private Integer badge;
    private String priority;
    private Map<String, String> data;
    
    public String getDeviceToken() { return deviceToken; }
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSound() { return sound; }
    public void setSound(String sound) { this.sound = sound; }
    public Integer getBadge() { return badge; }
    public void setBadge(Integer badge) { this.badge = badge; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Map<String, String> getData() { return data; }
    public void setData(Map<String, String> data) { this.data = data; }
}

class TopicNotificationRequest {
    @NotBlank private String topic;
    @NotBlank private String title;
    @NotBlank private String body;
    private Map<String, String> data;
    
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Map<String, String> getData() { return data; }
    public void setData(Map<String, String> data) { this.data = data; }
}

class DeviceRegistrationRequest {
    @NotBlank private String deviceToken;
    @NotBlank private DevicePlatform platform;
    private String userId;
    
    public String getDeviceToken() { return deviceToken; }
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
    public DevicePlatform getPlatform() { return platform; }
    public void setPlatform(DevicePlatform platform) { this.platform = platform; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}

class DeviceInfo {
    private String deviceToken;
    private DevicePlatform platform;
    private String userId;
    private LocalDateTime registeredAt;
    
    public DeviceInfo(String deviceToken, DevicePlatform platform, String userId, LocalDateTime registeredAt) {
        this.deviceToken = deviceToken;
        this.platform = platform;
        this.userId = userId;
        this.registeredAt = registeredAt;
    }
    
    public String getDeviceToken() { return deviceToken; }
    public DevicePlatform getPlatform() { return platform; }
    public String getUserId() { return userId; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
}

class PushNotificationResponse {
    private String notificationId;
    private PushNotificationStatus status;
    private String message;
    private LocalDateTime timestamp;
    
    public PushNotificationResponse(String notificationId, PushNotificationStatus status,
                                   String message, LocalDateTime timestamp) {
        this.notificationId = notificationId;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public String getNotificationId() { return notificationId; }
    public PushNotificationStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class BatchPushResponse {
    private List<PushNotificationResponse> responses;
    private int successCount;
    private int failureCount;
    
    public BatchPushResponse(List<PushNotificationResponse> responses, int successCount, int failureCount) {
        this.responses = responses;
        this.successCount = successCount;
        this.failureCount = failureCount;
    }
    
    public List<PushNotificationResponse> getResponses() { return responses; }
    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }
}

class DeviceRegistrationResponse {
    private String deviceToken;
    private String message;
    
    public DeviceRegistrationResponse(String deviceToken, String message) {
        this.deviceToken = deviceToken;
        this.message = message;
    }
    
    public String getDeviceToken() { return deviceToken; }
    public String getMessage() { return message; }
}

class PushNotificationHistory {
    private String notificationId;
    private String target;
    private String title;
    private PushNotificationStatus status;
    private LocalDateTime sentAt;
    private String message;
    
    public PushNotificationHistory(String notificationId, String target, String title,
                                  PushNotificationStatus status, LocalDateTime sentAt, String message) {
        this.notificationId = notificationId;
        this.target = target;
        this.title = title;
        this.status = status;
        this.sentAt = sentAt;
        this.message = message;
    }
    
    public String getNotificationId() { return notificationId; }
    public String getTarget() { return target; }
    public String getTitle() { return title; }
    public PushNotificationStatus getStatus() { return status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public String getMessage() { return message; }
}

class PushNotificationStats {
    private int totalSent;
    private int totalFailed;
    private int registeredDevices;
    private int activeTopics;
    
    public PushNotificationStats(int totalSent, int totalFailed, int registeredDevices, int activeTopics) {
        this.totalSent = totalSent;
        this.totalFailed = totalFailed;
        this.registeredDevices = registeredDevices;
        this.activeTopics = activeTopics;
    }
    
    public int getTotalSent() { return totalSent; }
    public int getTotalFailed() { return totalFailed; }
    public int getRegisteredDevices() { return registeredDevices; }
    public int getActiveTopics() { return activeTopics; }
}

enum DevicePlatform {
    IOS,
    ANDROID,
    WEB
}

enum PushNotificationStatus {
    SENT,
    FAILED,
    PENDING
}

/*
 * Usage Examples:
 * 
 * 1. Register Device:
 * POST /api/push-notifications/devices/register
 * {
 *   "deviceToken": "fcm_token_123",
 *   "platform": "ANDROID",
 *   "userId": "user123"
 * }
 * 
 * 2. Send Push Notification:
 * POST /api/push-notifications/send
 * {
 *   "deviceToken": "fcm_token_123",
 *   "title": "New Message",
 *   "body": "You have a new message",
 *   "imageUrl": "https://example.com/image.png",
 *   "data": {"messageId": "msg123"}
 * }
 * 
 * 3. Send to Topic:
 * POST /api/push-notifications/send-topic
 * {
 *   "topic": "breaking-news",
 *   "title": "Breaking News",
 *   "body": "Important update"
 * }
 */
