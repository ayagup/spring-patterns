package com.example.websocket.pointtopoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Point-to-Point Messaging Pattern
 * ================================
 * 
 * The Point-to-Point Messaging Pattern enables direct communication between two clients
 * without broadcasting to all subscribers. Messages are sent to a specific queue that
 * only the intended recipient can consume.
 * 
 * Key Concepts:
 * ------------
 * 1. Queue-based Communication:
 *    - /queue/* destinations for private messages
 *    - One-to-one message delivery
 *    - Message consumed by single recipient
 * 
 * 2. Use Cases:
 *    - Private chat messages
 *    - Direct notifications
 *    - Task assignment
 *    - File sharing requests
 *    - Command execution
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class PointToPointMessagingPattern {
    public static void main(String[] args) {
        SpringApplication.run(PointToPointMessagingPattern.class, args);
    }
}

@Configuration
@EnableWebSocketMessageBroker
class PointToPointConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-p2p")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

/**
 * Point-to-Point Messaging Service
 */
@Service
class PointToPointService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    public PointToPointService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send private message to specific session
     */
    public void sendToSession(String sessionId, Object message) {
        messagingTemplate.convertAndSend("/queue/private-" + sessionId, message);
    }

    /**
     * Send message to user's queue
     */
    public void sendToUser(String username, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(username, destination, message);
    }

    /**
     * Register session with username
     */
    public void registerSession(String sessionId, String username) {
        sessionToUser.put(sessionId, username);
    }

    /**
     * Unregister session
     */
    public void unregisterSession(String sessionId) {
        sessionToUser.remove(sessionId);
    }

    /**
     * Get username for session
     */
    public String getUsername(String sessionId) {
        return sessionToUser.get(sessionId);
    }
}

@Controller
class PointToPointController {

    private final PointToPointService p2pService;

    public PointToPointController(PointToPointService p2pService) {
        this.p2pService = p2pService;
    }

    /**
     * Send private message to specific user
     */
    @MessageMapping("/private.send")
    public void sendPrivateMessage(
            @Payload PrivateMessage message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        message.setTimestamp(LocalDateTime.now().toString());
        message.setSenderId(headerAccessor.getSessionId());
        
        // Send to recipient's private queue
        p2pService.sendToSession(message.getRecipientId(), message);
    }

    /**
     * Send direct notification
     */
    @MessageMapping("/notify.user")
    public void notifyUser(@Payload UserNotification notification) {
        notification.setTimestamp(LocalDateTime.now().toString());
        p2pService.sendToUser(
            notification.getUsername(),
            "/queue/notifications",
            notification
        );
    }
}

class PrivateMessage {
    private String senderId;
    private String recipientId;
    private String content;
    private String timestamp;

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

class UserNotification {
    private String username;
    private String message;
    private String type;
    private String timestamp;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

/*
 * Client Example:
 * // Subscribe to private queue
 * stompClient.subscribe('/queue/private-' + sessionId, function(message) {
 *     console.log('Private message:', JSON.parse(message.body));
 * });
 * 
 * // Send private message
 * stompClient.send("/app/private.send", {}, JSON.stringify({
 *     recipientId: 'session-123',
 *     content: 'Hello!'
 * }));
 */
