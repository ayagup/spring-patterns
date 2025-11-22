package com.example.websocket.sockjs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SockJS Pattern
 * ==============
 * 
 * SockJS is a JavaScript library that provides a WebSocket-like object with fallback options
 * for browsers that don't support WebSocket. It enables seamless real-time communication
 * with automatic fallback to other transport protocols.
 * 
 * Key Concepts:
 * ------------
 * 1. Transport Protocols (in order of preference):
 *    a) WebSocket - Native WebSocket protocol
 *    b) HTTP Streaming - chunked transfer encoding
 *    c) HTTP Long Polling - long-lived HTTP requests
 *    d) JSONP Polling - cross-domain polling
 * 
 * 2. Features:
 *    - Cross-browser compatibility
 *    - Transparent fallback mechanism
 *    - Heartbeat for connection health
 *    - Session management
 *    - CORS support
 *    - Cookie-based session affinity
 * 
 * 3. Session Lifecycle:
 *    - Client initiates connection
 *    - SockJS negotiates best transport
 *    - Establishes connection
 *    - Maintains heartbeat
 *    - Handles disconnection/reconnection
 * 
 * Use Cases:
 * ---------
 * - Browser compatibility required
 * - Firewall/proxy environments
 * - Corporate networks blocking WebSocket
 * - Mobile network with unreliable connections
 * - Legacy browser support
 * - Cross-domain communication
 * 
 * Transport Protocol Selection:
 * ----------------------------
 * 1. WebSocket (preferred):
 *    - Full-duplex communication
 *    - Low latency
 *    - Efficient bandwidth usage
 * 
 * 2. XHR Streaming:
 *    - HTTP chunked transfer
 *    - One long-lived connection
 *    - Works through proxies
 * 
 * 3. XHR Polling:
 *    - Short-lived HTTP requests
 *    - Compatible with all environments
 *    - Higher latency and overhead
 * 
 * Best Practices:
 * --------------
 * 1. Always enable SockJS for production
 * 2. Configure appropriate heartbeat intervals
 * 3. Handle transport fallback gracefully
 * 4. Implement connection retry logic
 * 5. Monitor transport protocol usage
 * 6. Configure CORS properly
 * 7. Use session affinity in load-balanced environments
 * 8. Test fallback mechanisms
 * 
 * Browser Compatibility:
 * ---------------------
 * - Chrome, Firefox, Safari: WebSocket
 * - IE 10+: WebSocket
 * - IE 8-9: XHR Streaming/Polling
 * - Old browsers: JSONP Polling
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
public class SockJSPattern {

    public static void main(String[] args) {
        SpringApplication.run(SockJSPattern.class, args);
    }
}

/**
 * SockJS WebSocket Configuration
 */
@Configuration
@EnableWebSocketMessageBroker
class SockJSConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Register SockJS endpoints with various configuration options
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS endpoint with all options
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                    .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
                    .setStreamBytesLimit(512 * 1024)      // 512KB streaming limit
                    .setHttpMessageCacheSize(1000)         // Message cache size
                    .setHeartbeatTime(25000)               // Heartbeat interval (25 seconds)
                    .setDisconnectDelay(5000)              // Delay before disconnect (5 seconds)
                    .setSessionCookieNeeded(true)          // Enable session cookie
                    .setWebSocketEnabled(true)             // Enable WebSocket transport
                    .setSuppressCors(false)                // Don't suppress CORS
                    .setInterceptors(new HttpSessionHandshakeInterceptor()); // HTTP session interceptor

        // Native WebSocket endpoint (no SockJS fallback)
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");

        // SockJS with custom transport options
        registry.addEndpoint("/ws-custom")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                    .setTransportHandlers(new CustomSockJSTransportHandler())
                    .setWebSocketEnabled(true);
    }

    /**
     * Configure WebSocket transport with decorators
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(128 * 1024)        // 128KB max message size
                   .setSendBufferSizeLimit(512 * 1024)       // 512KB send buffer
                   .setSendTimeLimit(20 * 1000)              // 20 seconds send timeout
                   .setTimeToFirstMessage(30 * 1000)         // 30 seconds to first message
                   .addDecoratorFactory(new ConnectionMonitoringDecoratorFactory());
    }
}

/**
 * Custom SockJS Transport Handler
 */
class CustomSockJSTransportHandler extends org.springframework.web.socket.sockjs.transport.handler.DefaultSockJsService {
    
    public CustomSockJSTransportHandler(org.springframework.scheduling.TaskScheduler scheduler) {
        super(scheduler);
    }
}

/**
 * WebSocket Handler Decorator Factory
 * Monitors WebSocket connection lifecycle
 */
class ConnectionMonitoringDecoratorFactory implements WebSocketHandlerDecoratorFactory {
    
    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(ConnectionMonitoringDecoratorFactory.class);

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {
            
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                logger.info("SockJS connection established: {} - Transport: {}", 
                    session.getId(), 
                    session.getUri());
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                logger.info("SockJS connection closed: {} - Status: {}", 
                    session.getId(), 
                    closeStatus);
                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }
}

/**
 * SockJS Message Controller
 */
@Controller
class SockJSMessageController {

    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();

    /**
     * Handle incoming messages via SockJS
     */
    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public SockJSMessage handleMessage(
            SockJSMessage message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String sessionId = headerAccessor.getSessionId();
        
        // Track session
        activeSessions.computeIfAbsent(sessionId, k -> new SessionInfo(sessionId));
        activeSessions.get(sessionId).incrementMessageCount();
        
        // Process message
        message.setTimestamp(LocalDateTime.now().toString());
        message.setSessionId(sessionId);
        message.setTransportType(getTransportType(headerAccessor));
        
        return message;
    }

    /**
     * Get connection info for monitoring
     */
    @MessageMapping("/connection.info")
    @SendTo("/topic/connection-info")
    public ConnectionInfo getConnectionInfo(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        SessionInfo sessionInfo = activeSessions.get(sessionId);
        
        return new ConnectionInfo(
            sessionId,
            getTransportType(headerAccessor),
            sessionInfo != null ? sessionInfo.getMessageCount() : 0,
            sessionInfo != null ? sessionInfo.getConnectedAt() : null,
            LocalDateTime.now().toString()
        );
    }

    /**
     * Handle heartbeat/ping messages
     */
    @MessageMapping("/heartbeat")
    @SendTo("/topic/heartbeat")
    public HeartbeatResponse handleHeartbeat(HeartbeatRequest request) {
        return new HeartbeatResponse(
            request.getClientId(),
            LocalDateTime.now().toString(),
            "ALIVE"
        );
    }

    private String getTransportType(SimpMessageHeaderAccessor headerAccessor) {
        // Extract transport type from session attributes
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.containsKey("transport")) {
            return sessionAttributes.get("transport").toString();
        }
        return "UNKNOWN";
    }
}

/**
 * SockJS Message Model
 */
class SockJSMessage {
    private String content;
    private String sender;
    private String sessionId;
    private String transportType;
    private String timestamp;

    public SockJSMessage() {}

    public SockJSMessage(String content, String sender) {
        this.content = content;
        this.sender = sender;
    }

    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

/**
 * Session Information
 */
class SessionInfo {
    private String sessionId;
    private String connectedAt;
    private int messageCount;
    private String lastActivityAt;

    public SessionInfo(String sessionId) {
        this.sessionId = sessionId;
        this.connectedAt = LocalDateTime.now().toString();
        this.messageCount = 0;
        this.lastActivityAt = this.connectedAt;
    }

    public void incrementMessageCount() {
        this.messageCount++;
        this.lastActivityAt = LocalDateTime.now().toString();
    }

    public String getSessionId() { return sessionId; }
    public String getConnectedAt() { return connectedAt; }
    public int getMessageCount() { return messageCount; }
    public String getLastActivityAt() { return lastActivityAt; }
}

/**
 * Connection Information
 */
class ConnectionInfo {
    private String sessionId;
    private String transportType;
    private int messageCount;
    private String connectedAt;
    private String currentTime;

    public ConnectionInfo(String sessionId, String transportType, int messageCount, 
                         String connectedAt, String currentTime) {
        this.sessionId = sessionId;
        this.transportType = transportType;
        this.messageCount = messageCount;
        this.connectedAt = connectedAt;
        this.currentTime = currentTime;
    }

    public String getSessionId() { return sessionId; }
    public String getTransportType() { return transportType; }
    public int getMessageCount() { return messageCount; }
    public String getConnectedAt() { return connectedAt; }
    public String getCurrentTime() { return currentTime; }
}

/**
 * Heartbeat Request/Response
 */
class HeartbeatRequest {
    private String clientId;
    private String timestamp;

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

class HeartbeatResponse {
    private String clientId;
    private String serverTime;
    private String status;

    public HeartbeatResponse(String clientId, String serverTime, String status) {
        this.clientId = clientId;
        this.serverTime = serverTime;
        this.status = status;
    }

    public String getClientId() { return clientId; }
    public String getServerTime() { return serverTime; }
    public String getStatus() { return status; }
}

/*
 * Client-Side JavaScript Example (SockJS + STOMP):
 * ================================================
 * 
 * // Import SockJS and STOMP libraries
 * <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
 * <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js"></script>
 * 
 * // Create SockJS instance
 * var socket = new SockJS('/ws-sockjs', null, {
 *     transports: ['websocket', 'xhr-streaming', 'xhr-polling'],
 *     timeout: 30000,
 *     heartbeat_delay: 25000
 * });
 * 
 * // Create STOMP client over SockJS
 * var stompClient = Stomp.over(socket);
 * 
 * // Configure STOMP client
 * stompClient.heartbeat.outgoing = 20000; // 20 seconds
 * stompClient.heartbeat.incoming = 20000;
 * 
 * // Connect to server
 * stompClient.connect({}, function(frame) {
 *     console.log('Connected via SockJS: ' + frame);
 *     console.log('Transport protocol: ' + socket._transport.transportName);
 *     
 *     // Subscribe to topics
 *     stompClient.subscribe('/topic/messages', function(message) {
 *         var msg = JSON.parse(message.body);
 *         console.log('Received:', msg);
 *         console.log('Transport:', msg.transportType);
 *     });
 *     
 *     // Subscribe to connection info
 *     stompClient.subscribe('/topic/connection-info', function(message) {
 *         var info = JSON.parse(message.body);
 *         console.log('Connection Info:', info);
 *     });
 * }, function(error) {
 *     console.error('SockJS connection error:', error);
 *     // Implement reconnection logic
 *     setTimeout(function() {
 *         location.reload();
 *     }, 5000);
 * });
 * 
 * // Send message
 * function sendMessage(content) {
 *     stompClient.send("/app/message", {}, JSON.stringify({
 *         'content': content,
 *         'sender': 'User123'
 *     }));
 * }
 * 
 * // Get connection info
 * function getConnectionInfo() {
 *     stompClient.send("/app/connection.info", {}, "{}");
 * }
 * 
 * // Monitor transport changes
 * socket.onopen = function() {
 *     console.log('SockJS opened with transport: ' + socket._transport.transportName);
 * };
 * 
 * socket.onclose = function() {
 *     console.log('SockJS connection closed');
 * };
 * 
 * // Heartbeat mechanism
 * setInterval(function() {
 *     if (stompClient.connected) {
 *         stompClient.send("/app/heartbeat", {}, JSON.stringify({
 *             'clientId': 'client-123',
 *             'timestamp': new Date().toISOString()
 *         }));
 *     }
 * }, 30000);
 * 
 * // Disconnect
 * function disconnect() {
 *     if (stompClient !== null) {
 *         stompClient.disconnect();
 *     }
 *     socket.close();
 * }
 */

/*
 * Transport Protocol Detection:
 * ============================
 * 
 * SockJS tries transports in this order:
 * 
 * 1. WebSocket (if supported and not blocked)
 * 2. XHR Streaming (HTTP chunked transfer)
 * 3. XHR Polling (long polling)
 * 4. JSONP Polling (cross-domain polling)
 * 
 * You can force a specific transport:
 * 
 * var socket = new SockJS('/ws-sockjs', null, {
 *     transports: ['websocket']  // WebSocket only
 * });
 * 
 * var socket = new SockJS('/ws-sockjs', null, {
 *     transports: ['xhr-polling']  // Polling only
 * });
 */

/*
 * application.properties for SockJS:
 * ==================================
 * 
 * # WebSocket Configuration
 * spring.websocket.message-buffer-size=512KB
 * spring.websocket.send-buffer-size-limit=512KB
 * spring.websocket.send-time-limit=20000
 * 
 * # SockJS Configuration
 * spring.websocket.sockjs.heartbeat-time=25000
 * spring.websocket.sockjs.disconnect-delay=5000
 * spring.websocket.sockjs.stream-bytes-limit=524288
 * spring.websocket.sockjs.http-message-cache-size=1000
 * spring.websocket.sockjs.client-library-url=https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js
 * 
 * # CORS Configuration
 * spring.websocket.allowed-origins=http://localhost:8080,https://example.com
 */
