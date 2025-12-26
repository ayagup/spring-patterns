package com.example.notification.realtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Real-Time Notification Pattern
 * 
 * Demonstrates:
 * - WebSocket notifications
 * - Server-Sent Events (SSE)
 * - Connection management
 * - Real-time event broadcasting
 * - User-specific notifications
 * - Connection status tracking
 * 
 * Dependencies:
 * - spring-boot-starter-websocket
 * - spring-boot-starter-web
 */

@SpringBootApplication
public class RealTimeNotificationPattern {
    public static void main(String[] args) {
        SpringApplication.run(RealTimeNotificationPattern.class, args);
    }
}

@Configuration
class RealTimeNotificationConfig {}

@RestController
@RequestMapping("/api/realtime-notifications")
class RealTimeNotificationController {
    private final RealTimeNotificationService service;
    
    public RealTimeNotificationController(RealTimeNotificationService service) {
        this.service = service;
    }
    
    @GetMapping("/subscribe/{userId}")
    public SseEmitter subscribe(@PathVariable String userId) {
        return service.subscribe(userId);
    }
    
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@Valid @RequestBody RealTimeNotificationRequest request) {
        service.sendNotification(request);
        return ResponseEntity.ok("Notification sent");
    }
    
    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@Valid @RequestBody BroadcastRequest request) {
        service.broadcast(request.getMessage());
        return ResponseEntity.ok("Broadcast sent");
    }
    
    @GetMapping("/connections")
    public ResponseEntity<Map<String, Integer>> getConnections() {
        return ResponseEntity.ok(service.getConnectionStats());
    }
}

@Service
class RealTimeNotificationService {
    private final Map<String, List<SseEmitter>> userConnections = new ConcurrentHashMap<>();
    private final Map<String, ConnectionInfo> connectionInfo = new ConcurrentHashMap<>();
    
    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        userConnections.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        String connectionId = UUID.randomUUID().toString();
        connectionInfo.put(connectionId, new ConnectionInfo(userId, LocalDateTime.now()));
        
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));
        
        // Send welcome message
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("Connected to real-time notifications"));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }
        
        return emitter;
    }
    
    public void sendNotification(RealTimeNotificationRequest request) {
        List<SseEmitter> emitters = userConnections.get(request.getUserId());
        if (emitters != null) {
            RealTimeNotification notification = new RealTimeNotification(
                UUID.randomUUID().toString(),
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                LocalDateTime.now()
            );
            
            sendToEmitters(emitters, "notification", notification);
        }
    }
    
    public void broadcast(String message) {
        RealTimeNotification notification = new RealTimeNotification(
            UUID.randomUUID().toString(),
            "Broadcast",
            message,
            NotificationType.INFO,
            LocalDateTime.now()
        );
        
        userConnections.values().forEach(emitters -> 
            sendToEmitters(emitters, "broadcast", notification)
        );
    }
    
    private void sendToEmitters(List<SseEmitter> emitters, String eventName, Object data) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        
        emitters.removeAll(deadEmitters);
    }
    
    private void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userConnections.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userConnections.remove(userId);
            }
        }
    }
    
    public Map<String, Integer> getConnectionStats() {
        Map<String, Integer> stats = new HashMap<>();
        userConnections.forEach((userId, emitters) -> 
            stats.put(userId, emitters.size())
        );
        return stats;
    }
}

class RealTimeNotificationRequest {
    @NotBlank
    private String userId;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String message;
    
    private NotificationType type = NotificationType.INFO;
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
}

class BroadcastRequest {
    @NotBlank
    private String message;
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

class RealTimeNotification {
    private String id;
    private String title;
    private String message;
    private NotificationType type;
    private LocalDateTime timestamp;
    
    public RealTimeNotification(String id, String title, String message, 
                               NotificationType type, LocalDateTime timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }
    
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public NotificationType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class ConnectionInfo {
    private String userId;
    private LocalDateTime connectedAt;
    
    public ConnectionInfo(String userId, LocalDateTime connectedAt) {
        this.userId = userId;
        this.connectedAt = connectedAt;
    }
    
    public String getUserId() { return userId; }
    public LocalDateTime getConnectedAt() { return connectedAt; }
}

enum NotificationType {
    INFO, SUCCESS, WARNING, ERROR
}
