package com.example.websocket.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket Session Pattern
 * =========================
 * 
 * The WebSocket Session Pattern manages the lifecycle, state, and metadata of individual
 * WebSocket connections. Sessions track user identity, connection details, attributes,
 * and subscriptions throughout the connection lifecycle.
 * 
 * Key Concepts:
 * ------------
 * 1. Session Lifecycle:
 *    - CONNECT: Initial connection attempt
 *    - CONNECTED: Connection established
 *    - SUBSCRIBE: Client subscribes to destination
 *    - UNSUBSCRIBE: Client unsubscribes from destination
 *    - DISCONNECT: Connection terminated
 * 
 * 2. Session Attributes:
 *    - Session ID: Unique identifier
 *    - User Principal: Authenticated user
 *    - Custom attributes: Application-specific data
 *    - Subscription IDs: Active subscriptions
 *    - Connection metadata: IP, headers, etc.
 * 
 * 3. Session Management:
 *    - Track active sessions
 *    - Monitor session health
 *    - Handle session timeout
 *    - Clean up resources
 *    - Session statistics
 * 
 * Use Cases:
 * ---------
 * - User presence tracking
 * - Online/offline status
 * - Connection monitoring
 * - Session-specific data storage
 * - Multi-device support
 * - Session timeout handling
 * - Connection quality monitoring
 * 
 * Session Events:
 * --------------
 * - SessionConnectEvent: Before connection established
 * - SessionConnectedEvent: After connection established
 * - SessionSubscribeEvent: User subscribes to destination
 * - SessionUnsubscribeEvent: User unsubscribes
 * - SessionDisconnectEvent: Connection closed
 * 
 * Best Practices:
 * --------------
 * 1. Clean up resources on disconnect
 * 2. Use concurrent collections for session storage
 * 3. Implement session timeout
 * 4. Track session statistics
 * 5. Handle session errors gracefully
 * 6. Store minimal data in sessions
 * 7. Implement heartbeat monitoring
 * 8. Log session lifecycle events
 * 
 * Dependencies:
 * ------------
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-websocket</artifactId>
 * </dependency>
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
@EnableScheduling
public class WebSocketSessionPattern {

    public static void main(String[] args) {
        SpringApplication.run(WebSocketSessionPattern.class, args);
    }
}

/**
 * WebSocket Session Configuration
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketSessionConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-session")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                    .setHeartbeatTime(25000);
    }
}

/**
 * WebSocket Session Manager
 * Manages all active WebSocket sessions
 */
@Service
class WebSocketSessionManager {

    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(WebSocketSessionManager.class);

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketSessionManager(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle new connection
     */
    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        logger.info("Session connecting: {}", sessionId);
        
        // Extract connection metadata
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put("connectTime", LocalDateTime.now().toString());
        }
    }

    /**
     * Handle established connection
     */
    @EventListener
    public void handleConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal user = accessor.getUser();
        
        logger.info("Session connected: {} - User: {}", 
            sessionId, user != null ? user.getName() : "anonymous");

        // Create session info
        SessionInfo sessionInfo = new SessionInfo(
            sessionId,
            user != null ? user.getName() : "anonymous",
            LocalDateTime.now().toString()
        );
        
        // Extract remote address and user agent
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionInfo.addAttribute("remoteAddress", 
                sessionAttributes.getOrDefault("remoteAddress", "unknown"));
            sessionInfo.addAttribute("userAgent", 
                sessionAttributes.getOrDefault("userAgent", "unknown"));
        }

        sessions.put(sessionId, sessionInfo);
        
        // Track user sessions for multi-device support
        String username = sessionInfo.getUsername();
        userSessions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet())
                   .add(sessionId);

        // Broadcast user online status
        broadcastUserStatus(username, "ONLINE");
        
        // Send session welcome message
        sendSessionInfo(sessionId);
    }

    /**
     * Handle subscription
     */
    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();
        String destination = accessor.getDestination();
        
        logger.info("Session {} subscribed to {} (subscription: {})", 
            sessionId, destination, subscriptionId);

        SessionInfo sessionInfo = sessions.get(sessionId);
        if (sessionInfo != null) {
            sessionInfo.addSubscription(subscriptionId, destination);
        }
    }

    /**
     * Handle unsubscription
     */
    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();
        
        logger.info("Session {} unsubscribed (subscription: {})", 
            sessionId, subscriptionId);

        SessionInfo sessionInfo = sessions.get(sessionId);
        if (sessionInfo != null) {
            sessionInfo.removeSubscription(subscriptionId);
        }
    }

    /**
     * Handle disconnection
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        logger.info("Session disconnected: {}", sessionId);

        SessionInfo sessionInfo = sessions.remove(sessionId);
        
        if (sessionInfo != null) {
            String username = sessionInfo.getUsername();
            
            // Remove from user sessions
            Set<String> userSessionSet = userSessions.get(username);
            if (userSessionSet != null) {
                userSessionSet.remove(sessionId);
                
                // If user has no more active sessions, mark as offline
                if (userSessionSet.isEmpty()) {
                    userSessions.remove(username);
                    broadcastUserStatus(username, "OFFLINE");
                }
            }
            
            // Log session duration
            long duration = sessionInfo.getDuration();
            logger.info("Session {} lasted {} seconds", sessionId, duration);
        }
    }

    /**
     * Scheduled task to check session health
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorSessions() {
        logger.debug("Monitoring {} active sessions", sessions.size());
        
        LocalDateTime now = LocalDateTime.now();
        List<String> stale Sessions = new ArrayList<>();
        
        sessions.forEach((sessionId, sessionInfo) -> {
            // Check for stale sessions (no activity for 10 minutes)
            if (sessionInfo.isStale(600)) { // 600 seconds = 10 minutes
                staleSessions.add(sessionId);
            }
        });
        
        // Clean up stale sessions
        staleSessions.forEach(sessionId -> {
            logger.warn("Removing stale session: {}", sessionId);
            sessions.remove(sessionId);
        });
    }

    /**
     * Send session information to client
     */
    private void sendSessionInfo(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        if (info != null) {
            messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                info.toMap()
            );
        }
    }

    /**
     * Broadcast user online/offline status
     */
    private void broadcastUserStatus(String username, String status) {
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("username", username);
        statusUpdate.put("status", status);
        statusUpdate.put("timestamp", LocalDateTime.now().toString());
        statusUpdate.put("deviceCount", getUserDeviceCount(username));
        
        messagingTemplate.convertAndSend("/topic/user-status", statusUpdate);
    }

    /**
     * Get session information
     */
    public SessionInfo getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Get all active sessions
     */
    public Collection<SessionInfo> getAllSessions() {
        return new ArrayList<>(sessions.values());
    }

    /**
     * Get sessions for specific user
     */
    public List<SessionInfo> getUserSessions(String username) {
        Set<String> sessionIds = userSessions.get(username);
        if (sessionIds == null) {
            return Collections.emptyList();
        }
        
        return sessionIds.stream()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get number of devices for user
     */
    public int getUserDeviceCount(String username) {
        Set<String> sessionIds = userSessions.get(username);
        return sessionIds != null ? sessionIds.size() : 0;
    }

    /**
     * Get session statistics
     */
    public SessionStatistics getStatistics() {
        return new SessionStatistics(
            sessions.size(),
            userSessions.size(),
            sessions.values().stream()
                    .mapToLong(SessionInfo::getDuration)
                    .average()
                    .orElse(0)
        );
    }

    /**
     * Is user online
     */
    public boolean isUserOnline(String username) {
        Set<String> sessionIds = userSessions.get(username);
        return sessionIds != null && !sessionIds.isEmpty();
    }

    /**
     * Get online users
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(userSessions.keySet());
    }
}

/**
 * Session Message Controller
 */
@Controller
class SessionMessageController {

    private final WebSocketSessionManager sessionManager;

    public SessionMessageController(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Get session information
     */
    @MessageMapping("/session.info")
    @SendTo("/topic/session-info")
    public Map<String, Object> getSessionInfo(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        SessionInfo info = sessionManager.getSession(sessionId);
        
        return info != null ? info.toMap() : Collections.emptyMap();
    }

    /**
     * Get all active sessions
     */
    @MessageMapping("/session.all")
    @SendTo("/topic/all-sessions")
    public List<Map<String, Object>> getAllSessions() {
        return sessionManager.getAllSessions().stream()
                .map(SessionInfo::toMap)
                .collect(Collectors.toList());
    }

    /**
     * Update session attribute
     */
    @MessageMapping("/session.update")
    public void updateSessionAttribute(
            SessionAttributeUpdate update,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String sessionId = headerAccessor.getSessionId();
        SessionInfo info = sessionManager.getSession(sessionId);
        
        if (info != null) {
            info.addAttribute(update.getKey(), update.getValue());
        }
    }

    /**
     * Get session statistics
     */
    @MessageMapping("/session.stats")
    @SendTo("/topic/session-stats")
    public SessionStatistics getStatistics() {
        return sessionManager.getStatistics();
    }
}

/**
 * Session Information
 */
class SessionInfo {
    private final String sessionId;
    private final String username;
    private final String connectedAt;
    private final Map<String, String> subscriptions;
    private final Map<String, Object> attributes;
    private String lastActivityAt;

    public SessionInfo(String sessionId, String username, String connectedAt) {
        this.sessionId = sessionId;
        this.username = username;
        this.connectedAt = connectedAt;
        this.lastActivityAt = connectedAt;
        this.subscriptions = new ConcurrentHashMap<>();
        this.attributes = new ConcurrentHashMap<>();
    }

    public void addSubscription(String subscriptionId, String destination) {
        subscriptions.put(subscriptionId, destination);
        updateActivity();
    }

    public void removeSubscription(String subscriptionId) {
        subscriptions.remove(subscriptionId);
        updateActivity();
    }

    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
        updateActivity();
    }

    private void updateActivity() {
        this.lastActivityAt = LocalDateTime.now().toString();
    }

    public boolean isStale(long maxInactiveSeconds) {
        LocalDateTime lastActivity = LocalDateTime.parse(lastActivityAt);
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(maxInactiveSeconds);
        return lastActivity.isBefore(threshold);
    }

    public long getDuration() {
        LocalDateTime connected = LocalDateTime.parse(connectedAt);
        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(connected, now).getSeconds();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", sessionId);
        map.put("username", username);
        map.put("connectedAt", connectedAt);
        map.put("lastActivityAt", lastActivityAt);
        map.put("duration", getDuration());
        map.put("subscriptions", new HashMap<>(subscriptions));
        map.put("attributes", new HashMap<>(attributes));
        return map;
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public String getUsername() { return username; }
    public String getConnectedAt() { return connectedAt; }
    public String getLastActivityAt() { return lastActivityAt; }
    public Map<String, String> getSubscriptions() { return new HashMap<>(subscriptions); }
    public Map<String, Object> getAttributes() { return new HashMap<>(attributes); }
}

/**
 * Session Statistics
 */
class SessionStatistics {
    private int totalSessions;
    private int uniqueUsers;
    private double averageDuration;
    private String timestamp;

    public SessionStatistics(int totalSessions, int uniqueUsers, double averageDuration) {
        this.totalSessions = totalSessions;
        this.uniqueUsers = uniqueUsers;
        this.averageDuration = averageDuration;
        this.timestamp = LocalDateTime.now().toString();
    }

    public int getTotalSessions() { return totalSessions; }
    public int getUniqueUsers() { return uniqueUsers; }
    public double getAverageDuration() { return averageDuration; }
    public String getTimestamp() { return timestamp; }
}

/**
 * Session Attribute Update
 */
class SessionAttributeUpdate {
    private String key;
    private Object value;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
}

/*
 * Client-Side JavaScript Example:
 * ================================
 * 
 * var socket = new SockJS('/ws-session');
 * var stompClient = Stomp.over(socket);
 * 
 * stompClient.connect({}, function(frame) {
 *     console.log('Connected: ' + frame);
 *     
 *     // Subscribe to session-specific updates
 *     var sessionId = getSessionId(frame); // Extract from frame
 *     stompClient.subscribe('/topic/session/' + sessionId, function(message) {
 *         var sessionInfo = JSON.parse(message.body);
 *         console.log('Session info:', sessionInfo);
 *     });
 *     
 *     // Subscribe to user status updates
 *     stompClient.subscribe('/topic/user-status', function(message) {
 *         var status = JSON.parse(message.body);
 *         console.log('User status:', status.username, status.status);
 *     });
 *     
 *     // Get session information
 *     stompClient.send("/app/session.info", {}, "{}");
 *     
 *     // Subscribe to session stats
 *     stompClient.subscribe('/topic/session-stats', function(message) {
 *         var stats = JSON.parse(message.body);
 *         console.log('Active sessions:', stats.totalSessions);
 *         console.log('Online users:', stats.uniqueUsers);
 *     });
 * });
 * 
 * // Update session attribute
 * function updateSessionAttribute(key, value) {
 *     stompClient.send("/app/session.update", {}, JSON.stringify({
 *         key: key,
 *         value: value
 *     }));
 * }
 * 
 * // Get statistics
 * function getStatistics() {
 *     stompClient.send("/app/session.stats", {}, "{}");
 * }
 */
