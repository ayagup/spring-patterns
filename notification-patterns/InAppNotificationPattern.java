package com.example.notification.inapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-App Notification Pattern
 * 
 * Demonstrates:
 * - In-application notification center
 * - User notification preferences
 * - Read/unread status tracking
 * - Notification categories
 * - Notification archiving
 * - Notification badges/counters
 */

@SpringBootApplication
public class InAppNotificationPattern {
    public static void main(String[] args) {
        SpringApplication.run(InAppNotificationPattern.class, args);
    }
}

@Configuration
class InAppNotificationConfig {}

@RestController
@RequestMapping("/api/in-app-notifications")
class InAppNotificationController {
    private final InAppNotificationService service;
    
    public InAppNotificationController(InAppNotificationService service) {
        this.service = service;
    }
    
    @PostMapping("/send")
    public ResponseEntity<InAppNotification> send(@Valid @RequestBody InAppNotificationRequest request) {
        return ResponseEntity.ok(service.sendNotification(request));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InAppNotification>> getUserNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(service.getUserNotifications(userId));
    }
    
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<InAppNotification>> getUnreadNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(service.getUnreadNotifications(userId));
    }
    
    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<InAppNotification> markAsRead(@PathVariable String notificationId) {
        return service.markAsRead(notificationId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/user/{userId}/mark-all-read")
    public ResponseEntity<Integer> markAllAsRead(@PathVariable String userId) {
        return ResponseEntity.ok(service.markAllAsRead(userId));
    }
    
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId) {
        service.deleteNotification(notificationId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<NotificationCount> getNotificationCount(@PathVariable String userId) {
        return ResponseEntity.ok(service.getNotificationCount(userId));
    }
}

@Service
class InAppNotificationService {
    private final Map<String, List<InAppNotification>> userNotifications = new ConcurrentHashMap<>();
    private final Map<String, InAppNotification> allNotifications = new ConcurrentHashMap<>();
    
    public InAppNotification sendNotification(InAppNotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();
        
        InAppNotification notification = new InAppNotification(
            notificationId,
            request.getUserId(),
            request.getTitle(),
            request.getMessage(),
            request.getCategory(),
            request.getPriority(),
            false,
            LocalDateTime.now(),
            request.getActionUrl()
        );
        
        allNotifications.put(notificationId, notification);
        userNotifications.computeIfAbsent(request.getUserId(), k -> new ArrayList<>()).add(notification);
        
        return notification;
    }
    
    public List<InAppNotification> getUserNotifications(String userId) {
        return userNotifications.getOrDefault(userId, Collections.emptyList());
    }
    
    public List<InAppNotification> getUnreadNotifications(String userId) {
        return getUserNotifications(userId).stream()
            .filter(n -> !n.isRead())
            .collect(Collectors.toList());
    }
    
    public Optional<InAppNotification> markAsRead(String notificationId) {
        InAppNotification notification = allNotifications.get(notificationId);
        if (notification != null) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        return Optional.ofNullable(notification);
    }
    
    public int markAllAsRead(String userId) {
        List<InAppNotification> notifications = getUserNotifications(userId);
        int count = 0;
        for (InAppNotification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notification.setReadAt(LocalDateTime.now());
                count++;
            }
        }
        return count;
    }
    
    public void deleteNotification(String notificationId) {
        InAppNotification notification = allNotifications.remove(notificationId);
        if (notification != null) {
            List<InAppNotification> userNotes = userNotifications.get(notification.getUserId());
            if (userNotes != null) {
                userNotes.remove(notification);
            }
        }
    }
    
    public NotificationCount getNotificationCount(String userId) {
        List<InAppNotification> notifications = getUserNotifications(userId);
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        return new NotificationCount((int) unreadCount, notifications.size());
    }
}

class InAppNotificationRequest {
    @NotBlank
    private String userId;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String message;
    
    private NotificationCategory category = NotificationCategory.GENERAL;
    private NotificationPriority priority = NotificationPriority.NORMAL;
    private String actionUrl;
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public NotificationCategory getCategory() { return category; }
    public void setCategory(NotificationCategory category) { this.category = category; }
    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
}

class InAppNotification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private NotificationCategory category;
    private NotificationPriority priority;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String actionUrl;
    
    public InAppNotification(String id, String userId, String title, String message, 
                            NotificationCategory category, NotificationPriority priority,
                            boolean read, LocalDateTime createdAt, String actionUrl) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.category = category;
        this.priority = priority;
        this.read = read;
        this.createdAt = createdAt;
        this.actionUrl = actionUrl;
    }
    
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public NotificationCategory getCategory() { return category; }
    public NotificationPriority getPriority() { return priority; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public String getActionUrl() { return actionUrl; }
}

class NotificationCount {
    private int unread;
    private int total;
    
    public NotificationCount(int unread, int total) {
        this.unread = unread;
        this.total = total;
    }
    
    public int getUnread() { return unread; }
    public int getTotal() { return total; }
}

enum NotificationCategory {
    GENERAL, SOCIAL, SYSTEM, MARKETING, ALERT
}

enum NotificationPriority {
    LOW, NORMAL, HIGH, URGENT
}
