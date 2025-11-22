package com.example.websocket.stomp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * STOMP Protocol Pattern
 * =====================
 * 
 * STOMP (Simple Text Oriented Messaging Protocol) is a messaging protocol that provides
 * an interoperable wire format allowing STOMP clients to communicate with STOMP message
 * brokers. Spring provides STOMP over WebSocket support for building real-time messaging
 * applications.
 * 
 * Key Concepts:
 * ------------
 * 1. STOMP Frames:
 *    - CONNECT: Client connection
 *    - SEND: Send message to destination
 *    - SUBSCRIBE: Subscribe to destination
 *    - UNSUBSCRIBE: Unsubscribe from destination
 *    - DISCONNECT: Client disconnect
 *    - MESSAGE: Server-to-client message
 * 
 * 2. Destinations:
 *    - /topic/* - Broadcast to all subscribers
 *    - /queue/* - Point-to-point messaging
 *    - /app/* - Application-specific endpoints
 *    - /user/* - User-specific destinations
 * 
 * 3. Message Flow:
 *    - Client sends STOMP frame
 *    - Message broker routes to destination
 *    - Subscribers receive messages
 * 
 * Use Cases:
 * ---------
 * - Real-time chat applications
 * - Live notifications
 * - Collaborative editing
 * - Live dashboards
 * - Stock ticker updates
 * - Gaming applications
 * 
 * Best Practices:
 * --------------
 * 1. Use appropriate destination prefixes
 * 2. Handle connection errors gracefully
 * 3. Implement heartbeat mechanism
 * 4. Secure WebSocket endpoints
 * 5. Use message size limits
 * 6. Implement reconnection logic
 * 7. Monitor active connections
 * 
 * Dependencies:
 * ------------
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-websocket</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.webjars</groupId>
 *     <artifactId>sockjs-client</artifactId>
 *     <version>1.5.1</version>
 * </dependency>
 * <dependency>
 *     <groupId>org.webjars</groupId>
 *     <artifactId>stomp-websocket</artifactId>
 *     <version>2.3.4</version>
 * </dependency>
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class STOMPProtocolPattern {

    public static void main(String[] args) {
        SpringApplication.run(STOMPProtocolPattern.class, args);
    }
}

/**
 * WebSocket Configuration with STOMP Protocol
 * Configures message broker, STOMP endpoints, and destination prefixes
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker options
     * 
     * Destination Prefixes:
     * - /app: Application destination prefix (messages to @MessageMapping)
     * - /topic: Broker destination for broadcasting
     * - /queue: Broker destination for point-to-point
     * - /user: User-specific destinations
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for /topic and /queue destinations
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix (default is /user)
        config.setUserDestinationPrefix("/user");
        
        /*
         * For production, use external message broker:
         * 
         * config.enableStompBrokerRelay("/topic", "/queue")
         *     .setRelayHost("localhost")
         *     .setRelayPort(61613)
         *     .setClientLogin("guest")
         *     .setClientPasscode("guest")
         *     .setSystemLogin("guest")
         *     .setSystemPasscode("guest")
         *     .setVirtualHost("/")
         *     .setSystemHeartbeatSendInterval(5000)
         *     .setSystemHeartbeatReceiveInterval(4000);
         */
    }

    /**
     * Register STOMP endpoints
     * Clients connect to these endpoints to establish WebSocket connection
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register /ws endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Register native WebSocket endpoint (without SockJS)
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
}

/**
 * STOMP Message Controller
 * Handles incoming STOMP messages and sends responses
 */
@Controller
class StompMessageController {

    /**
     * Handle chat messages sent to /app/chat
     * Broadcast to all subscribers at /topic/messages
     * 
     * STOMP Frame Example:
     * SEND
     * destination:/app/chat
     * content-type:application/json
     * 
     * {"from":"John","content":"Hello World"}
     */
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage handleChatMessage(ChatMessage message) {
        message.setTimestamp(getCurrentTimestamp());
        message.setType(MessageType.CHAT);
        return message;
    }

    /**
     * Handle direct messages to specific user
     * 
     * STOMP Frame Example:
     * SEND
     * destination:/app/private
     * content-type:application/json
     * 
     * {"to":"user123","content":"Private message"}
     */
    @MessageMapping("/private")
    @SendTo("/topic/messages")
    public ChatMessage handlePrivateMessage(
            ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String username = headerAccessor.getUser().getName();
        message.setFrom(username);
        message.setTimestamp(getCurrentTimestamp());
        message.setType(MessageType.PRIVATE);
        return message;
    }

    /**
     * Handle typing indicator
     */
    @MessageMapping("/typing")
    @SendTo("/topic/typing")
    public TypingIndicator handleTyping(TypingIndicator indicator) {
        indicator.setTimestamp(getCurrentTimestamp());
        return indicator;
    }

    /**
     * Handle user join notification
     */
    @MessageMapping("/join")
    @SendTo("/topic/messages")
    public ChatMessage handleUserJoin(ChatMessage message) {
        message.setType(MessageType.JOIN);
        message.setTimestamp(getCurrentTimestamp());
        return message;
    }

    /**
     * Handle user leave notification
     */
    @MessageMapping("/leave")
    @SendTo("/topic/messages")
    public ChatMessage handleUserLeave(ChatMessage message) {
        message.setType(MessageType.LEAVE);
        message.setTimestamp(getCurrentTimestamp());
        return message;
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}

/**
 * Chat Message Model
 * Represents a message sent via STOMP protocol
 */
class ChatMessage {
    private String from;
    private String to;
    private String content;
    private MessageType type;
    private String timestamp;

    public ChatMessage() {}

    public ChatMessage(String from, String content, MessageType type) {
        this.from = from;
        this.content = content;
        this.type = type;
    }

    // Getters and Setters
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}

/**
 * Message Type Enumeration
 */
enum MessageType {
    CHAT,
    PRIVATE,
    JOIN,
    LEAVE,
    TYPING,
    SYSTEM
}

/**
 * Typing Indicator Model
 */
class TypingIndicator {
    private String username;
    private boolean isTyping;
    private String timestamp;

    public TypingIndicator() {}

    public TypingIndicator(String username, boolean isTyping) {
        this.username = username;
        this.isTyping = isTyping;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isTyping() { return isTyping; }
    public void setTyping(boolean typing) { isTyping = typing; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

/**
 * Web Controller for serving HTML page
 */
@Controller
class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}

/*
 * Client-Side JavaScript Example (using SockJS and STOMP.js):
 * ============================================================
 * 
 * // Connect to WebSocket
 * var socket = new SockJS('/ws');
 * var stompClient = Stomp.over(socket);
 * 
 * // Connect with headers
 * stompClient.connect({}, function(frame) {
 *     console.log('Connected: ' + frame);
 *     
 *     // Subscribe to topic
 *     stompClient.subscribe('/topic/messages', function(message) {
 *         var msg = JSON.parse(message.body);
 *         console.log('Received: ', msg);
 *     });
 *     
 *     // Subscribe to user-specific queue
 *     stompClient.subscribe('/user/queue/private', function(message) {
 *         var msg = JSON.parse(message.body);
 *         console.log('Private message: ', msg);
 *     });
 * });
 * 
 * // Send message
 * function sendMessage() {
 *     stompClient.send("/app/chat", {}, JSON.stringify({
 *         'from': 'John',
 *         'content': 'Hello World'
 *     }));
 * }
 * 
 * // Send typing indicator
 * function sendTyping() {
 *     stompClient.send("/app/typing", {}, JSON.stringify({
 *         'username': 'John',
 *         'isTyping': true
 *     }));
 * }
 * 
 * // Disconnect
 * function disconnect() {
 *     if (stompClient !== null) {
 *         stompClient.disconnect();
 *     }
 *     console.log("Disconnected");
 * }
 * 
 * // Error handling
 * stompClient.ws.onerror = function(error) {
 *     console.error('WebSocket error:', error);
 * };
 * 
 * stompClient.ws.onclose = function() {
 *     console.log('WebSocket connection closed');
 * };
 */

/*
 * application.properties Configuration:
 * =====================================
 * 
 * # WebSocket Configuration
 * spring.websocket.message-buffer-size=512KB
 * spring.websocket.http-buffer-size=8KB
 * spring.websocket.send-time-limit=20000
 * spring.websocket.send-buffer-size-limit=512KB
 * 
 * # STOMP Configuration
 * spring.websocket.stomp.heartbeat.client=10000
 * spring.websocket.stomp.heartbeat.server=10000
 * 
 * # Logging
 * logging.level.org.springframework.messaging=DEBUG
 * logging.level.org.springframework.web.socket=DEBUG
 */

/*
 * Security Configuration Example:
 * ===============================
 * 
 * @Configuration
 * @EnableWebSocketSecurity
 * public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
 *     
 *     @Override
 *     protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
 *         messages
 *             .simpDestMatchers("/app/**").authenticated()
 *             .simpSubscribeDestMatchers("/topic/**", "/queue/**").authenticated()
 *             .anyMessage().denyAll();
 *     }
 *     
 *     @Override
 *     protected boolean sameOriginDisabled() {
 *         return true;
 *     }
 * }
 */

/*
 * STOMP Protocol Frame Examples:
 * ==============================
 * 
 * 1. CONNECT Frame:
 * -----------------
 * CONNECT
 * accept-version:1.2
 * host:example.com
 * login:user
 * passcode:password
 * heart-beat:10000,10000
 * 
 * ^@
 * 
 * 2. CONNECTED Frame (Response):
 * ------------------------------
 * CONNECTED
 * version:1.2
 * heart-beat:10000,10000
 * server:Spring-STOMP/2.5
 * 
 * ^@
 * 
 * 3. SUBSCRIBE Frame:
 * ------------------
 * SUBSCRIBE
 * id:sub-0
 * destination:/topic/messages
 * 
 * ^@
 * 
 * 4. SEND Frame:
 * -------------
 * SEND
 * destination:/app/chat
 * content-type:application/json
 * content-length:45
 * 
 * {"from":"John","content":"Hello World"}^@
 * 
 * 5. MESSAGE Frame (Server to Client):
 * ------------------------------------
 * MESSAGE
 * destination:/topic/messages
 * message-id:msg-001
 * subscription:sub-0
 * content-type:application/json
 * content-length:75
 * 
 * {"from":"John","content":"Hello World","timestamp":"2025-11-16T10:30:00"}^@
 * 
 * 6. UNSUBSCRIBE Frame:
 * --------------------
 * UNSUBSCRIBE
 * id:sub-0
 * 
 * ^@
 * 
 * 7. DISCONNECT Frame:
 * -------------------
 * DISCONNECT
 * receipt:disconnect-001
 * 
 * ^@
 * 
 * 8. ERROR Frame:
 * --------------
 * ERROR
 * message:Access denied
 * content-type:text/plain
 * content-length:15
 * 
 * Access denied!^@
 */
