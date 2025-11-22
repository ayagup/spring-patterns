package com.example.websocket.userdestination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * User Destination Pattern
 * ========================
 * 
 * The User Destination Pattern enables sending messages to specific authenticated users
 * regardless of their session IDs. Spring automatically resolves /user/* destinations
 * to the appropriate user sessions, supporting multi-device scenarios.
 * 
 * Key Concepts:
 * ------------
 * 1. User-specific Messaging:
 *    - /user/{username}/queue/* - User private queue
 *    - /user/{username}/topic/* - User private topic
 *    - Automatic session resolution
 *    - Multi-device support
 * 
 * 2. Features:
 *    - User authentication required
 *    - Automatic message routing to user sessions
 *    - Support for multiple sessions per user
 *    - User presence tracking
 * 
 * 3. Use Cases:
 *    - Personalized notifications
 *    - User-specific updates
 *    - Private messaging
 *    - Task assignments
 *    - User alerts
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class UserDestinationPattern {
    public static void main(String[] args) {
        SpringApplication.run(UserDestinationPattern.class, args);
    }
}

@Configuration
@EnableWebSocketMessageBroker
class UserDestinationConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-user")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

/**
 * User Messaging Service
 */
@Service
class UserMessagingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    public UserMessagingService(
            SimpMessagingTemplate messagingTemplate,
            SimpUserRegistry userRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
    }

    /**
     * Send message to specific user
     * Automatically routes to all user's active sessions
     */
    public void sendToUser(String username, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(username, destination, message);
    }

    /**
     * Send notification to user
     */
    public void notifyUser(String username, String message, String type) {
        UserNotification notification = new UserNotification(
            username,
            message,
            type,
            LocalDateTime.now().toString()
        );
        
        sendToUser(username, "/queue/notifications", notification);
    }

    /**
     * Send private message to user
     */
    public void sendPrivateMessage(String from, String to, String content) {
        PrivateMessage message = new PrivateMessage(
            from,
            to,
            content,
            LocalDateTime.now().toString()
        );
        
        sendToUser(to, "/queue/private", message);
    }

    /**
     * Check if user is online
     */
    public boolean isUserOnline(String username) {
        return userRegistry.getUser(username) != null;
    }

    /**
     * Get all online users
     */
    public Set<String> getOnlineUsers() {
        Set<String> users = new HashSet<>();
        userRegistry.getUsers().forEach(user -> 
            users.add(user.getName())
        );
        return users;
    }

    /**
     * Get user session count
     */
    public int getUserSessionCount(String username) {
        var user = userRegistry.getUser(username);
        return user != null ? user.getSessions().size() : 0;
    }

    /**
     * Broadcast to all online users
     */
    public void broadcastToAllUsers(Object message) {
        getOnlineUsers().forEach(username -> 
            sendToUser(username, "/queue/broadcast", message)
        );
    }
}

@Controller
class UserDestinationController {

    private final UserMessagingService userMessagingService;

    public UserDestinationController(UserMessagingService userMessagingService) {
        this.userMessagingService = userMessagingService;
    }

    /**
     * Send message to authenticated user
     * Response automatically sent to /user/{username}/queue/reply
     */
    @MessageMapping("/user.message")
    @SendToUser("/queue/reply")
    public UserResponse handleUserMessage(
            @Payload String message,
            Principal principal) {
        
        return new UserResponse(
            "Received: " + message,
            principal.getName(),
            LocalDateTime.now().toString()
        );
    }

    /**
     * Send private message to another user
     */
    @MessageMapping("/user.send")
    public void sendToUser(
            @Payload PrivateMessage message,
            Principal principal) {
        
        message.setSender(principal.getName());
        message.setTimestamp(LocalDateTime.now().toString());
        
        userMessagingService.sendToUser(
            message.getRecipient(),
            "/queue/private",
            message
        );
    }

    /**
     * Request user notification
     */
    @MessageMapping("/user.notify")
    public void notifyUser(
            @Payload NotificationRequest request,
            Principal principal) {
        
        userMessagingService.notifyUser(
            request.getUsername(),
            request.getMessage(),
            request.getType()
        );
    }

    /**
     * Get online users list
     */
    @MessageMapping("/user.online")
    @SendToUser("/queue/online-users")
    public OnlineUsersResponse getOnlineUsers(Principal principal) {
        Set<String> users = userMessagingService.getOnlineUsers();
        return new OnlineUsersResponse(
            new ArrayList<>(users),
            users.size(),
            LocalDateTime.now().toString()
        );
    }

    /**
     * Check user status
     */
    @MessageMapping("/user.status")
    @SendToUser("/queue/status")
    public UserStatusResponse checkUserStatus(
            @Payload String username,
            Principal principal) {
        
        boolean online = userMessagingService.isUserOnline(username);
        int sessions = userMessagingService.getUserSessionCount(username);
        
        return new UserStatusResponse(
            username,
            online,
            sessions,
            LocalDateTime.now().toString()
        );
    }
}

/**
 * User Response Model
 */
class UserResponse {
    private String message;
    private String username;
    private String timestamp;

    public UserResponse(String message, String username, String timestamp) {
        this.message = message;
        this.username = username;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public String getUsername() { return username; }
    public String getTimestamp() { return timestamp; }
}

/**
 * Private Message Model
 */
class PrivateMessage {
    private String sender;
    private String recipient;
    private String content;
    private String timestamp;

    public PrivateMessage() {}

    public PrivateMessage(String sender, String recipient, String content, String timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

/**
 * User Notification Model
 */
class UserNotification {
    private String username;
    private String message;
    private String type;
    private String timestamp;

    public UserNotification(String username, String message, String type, String timestamp) {
        this.username = username;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public String getTimestamp() { return timestamp; }
}

/**
 * Notification Request Model
 */
class NotificationRequest {
    private String username;
    private String message;
    private String type;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

/**
 * Online Users Response
 */
class OnlineUsersResponse {
    private List<String> users;
    private int count;
    private String timestamp;

    public OnlineUsersResponse(List<String> users, int count, String timestamp) {
        this.users = users;
        this.count = count;
        this.timestamp = timestamp;
    }

    public List<String> getUsers() { return users; }
    public int getCount() { return count; }
    public String getTimestamp() { return timestamp; }
}

/**
 * User Status Response
 */
class UserStatusResponse {
    private String username;
    private boolean online;
    private int sessionCount;
    private String timestamp;

    public UserStatusResponse(String username, boolean online, int sessionCount, String timestamp) {
        this.username = username;
        this.online = online;
        this.sessionCount = sessionCount;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }
    public boolean isOnline() { return online; }
    public int getSessionCount() { return sessionCount; }
    public String getTimestamp() { return timestamp; }
}

/*
 * Client-Side JavaScript Example:
 * ================================
 * 
 * var socket = new SockJS('/ws-user');
 * var stompClient = Stomp.over(socket);
 * 
 * stompClient.connect({login: 'john', passcode: 'password'}, function(frame) {
 *     console.log('Connected as:', frame.headers['user-name']);
 *     
 *     // Subscribe to user-specific queue
 *     stompClient.subscribe('/user/queue/notifications', function(message) {
 *         var notification = JSON.parse(message.body);
 *         console.log('Notification:', notification);
 *     });
 *     
 *     // Subscribe to private messages
 *     stompClient.subscribe('/user/queue/private', function(message) {
 *         var msg = JSON.parse(message.body);
 *         console.log('Private message from', msg.sender + ':', msg.content);
 *     });
 *     
 *     // Subscribe to user reply queue
 *     stompClient.subscribe('/user/queue/reply', function(message) {
 *         var response = JSON.parse(message.body);
 *         console.log('Response:', response.message);
 *     });
 * });
 * 
 * // Send message to own user destination
 * function sendUserMessage(content) {
 *     stompClient.send("/app/user.message", {}, content);
 * }
 * 
 * // Send private message to another user
 * function sendPrivateMessage(recipient, content) {
 *     stompClient.send("/app/user.send", {}, JSON.stringify({
 *         recipient: recipient,
 *         content: content
 *     }));
 * }
 * 
 * // Get online users
 * function getOnlineUsers() {
 *     stompClient.send("/app/user.online", {}, "{}");
 * }
 * 
 * // Check user status
 * function checkUserStatus(username) {
 *     stompClient.send("/app/user.status", {}, username);
 * }
 */

/*
 * Security Configuration for User Authentication:
 * ===============================================
 * 
 * @Configuration
 * public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
 *     
 *     @Override
 *     protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
 *         messages
 *             .simpDestMatchers("/app/**").authenticated()
 *             .simpSubscribeDestMatchers("/user/**").authenticated()
 *             .anyMessage().denyAll();
 *     }
 *     
 *     @Override
 *     protected boolean sameOriginDisabled() {
 *         return true;
 *     }
 * }
 * 
 * // Custom Principal Handshake Handler
 * public class UserHandshakeHandler extends DefaultHandshakeHandler {
 *     
 *     @Override
 *     protected Principal determineUser(
 *             ServerHttpRequest request,
 *             WebSocketHandler wsHandler,
 *             Map<String, Object> attributes) {
 *         
 *         // Extract username from request headers or session
 *         String username = extractUsername(request);
 *         return new StompPrincipal(username);
 *     }
 * }
 * 
 * // Register custom handshake handler
 * @Override
 * public void registerStompEndpoints(StompEndpointRegistry registry) {
 *     registry.addEndpoint("/ws-user")
 *         .setHandshakeHandler(new UserHandshakeHandler())
 *         .setAllowedOriginPatterns("*")
 *         .withSockJS();
 * }
 */
