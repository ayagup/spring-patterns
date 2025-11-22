# Spring WebSocket Patterns

This directory contains comprehensive implementations of all major WebSocket patterns used in Spring applications for real-time, bidirectional communication.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Pattern Descriptions](#pattern-descriptions)
3. [Comparison Matrix](#comparison-matrix)
4. [When to Use Each Pattern](#when-to-use-each-pattern)
5. [Configuration](#configuration)
6. [Best Practices](#best-practices)
7. [Common Use Cases](#common-use-cases)
8. [Security Considerations](#security-considerations)
9. [Performance Tips](#performance-tips)
10. [Troubleshooting](#troubleshooting)

## Overview

WebSocket patterns enable real-time, full-duplex communication between clients and servers. Spring provides comprehensive WebSocket support through STOMP protocol, message brokers, and low-level WebSocket handlers.

### Patterns Included

1. **STOMP Protocol Pattern** - Text-oriented messaging protocol over WebSocket
2. **Message Broker Pattern** - Centralized message routing and delivery
3. **SockJS Pattern** - WebSocket with fallback transport options
4. **WebSocket Handler Pattern** - Low-level WebSocket message handling
5. **WebSocket Session Pattern** - Session lifecycle and state management
6. **Subscription Pattern** - Client subscription management
7. **Broadcasting Pattern** - One-to-many message distribution
8. **Point-to-Point Messaging Pattern** - Direct client-to-client communication
9. **User Destination Pattern** - User-specific message routing

## Pattern Descriptions

### 1. STOMP Protocol Pattern
**File:** `STOMPProtocolPattern.java`

STOMP (Simple Text Oriented Messaging Protocol) provides an interoperable wire format for WebSocket communication.

**Key Features:**
- Text-based protocol
- Frame-based messaging
- Destination routing
- Acknowledgments
- Heart-beating

**STOMP Frame Types:**
```
CONNECT    - Client connection
SUBSCRIBE  - Subscribe to destination
SEND       - Send message
MESSAGE    - Server-to-client message
DISCONNECT - Close connection
ERROR      - Error notification
```

**Example:**
```java
@MessageMapping("/chat")
@SendTo("/topic/messages")
public ChatMessage handleChat(ChatMessage message) {
    return message;
}
```

### 2. Message Broker Pattern
**File:** `MessageBrokerPattern.java`

Centralized message broker handles routing, delivery, and transformation of messages.

**Broker Types:**
- **Simple Broker**: In-memory, lightweight
- **External Broker**: RabbitMQ, ActiveMQ, Apache Apollo

**Key Features:**
- Message routing
- Publish-subscribe
- Point-to-point messaging
- Message persistence
- Dead letter queues

**Configuration:**
```java
@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic", "/queue");
    config.setApplicationDestinationPrefixes("/app");
}
```

### 3. SockJS Pattern
**File:** `SockJSPattern.java`

SockJS provides WebSocket-like API with automatic fallback to alternative transports.

**Transport Protocols (in order of preference):**
1. WebSocket (native)
2. HTTP Streaming
3. HTTP Long Polling
4. JSONP Polling

**Key Features:**
- Cross-browser compatibility
- Transparent fallback
- Heartbeat support
- CORS support
- Session management

**Configuration:**
```java
registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .withSockJS()
            .setHeartbeatTime(25000)
            .setStreamBytesLimit(512 * 1024);
```

### 4. WebSocket Handler Pattern
**File:** `WebSocketHandlerPattern.java`

Low-level WebSocket API for direct message handling without STOMP layer.

**Handler Types:**
- `TextWebSocketHandler` - Text messages
- `BinaryWebSocketHandler` - Binary data
- `AbstractWebSocketHandler` - Both types

**Lifecycle Methods:**
```java
afterConnectionEstablished()  // Connection opened
handleMessage()               // Process message
handleTransportError()        // Handle errors
afterConnectionClosed()       // Connection closed
```

**Use Cases:**
- Custom protocols
- Binary data transmission
- Gaming applications
- Low-latency requirements

### 5. WebSocket Session Pattern
**File:** `WebSocketSessionPattern.java`

Manages WebSocket session lifecycle, state, and metadata.

**Session Events:**
- `SessionConnectEvent` - Connection attempt
- `SessionConnectedEvent` - Connection established
- `SessionSubscribeEvent` - Subscription created
- `SessionUnsubscribeEvent` - Subscription removed
- `SessionDisconnectEvent` - Connection closed

**Key Features:**
- Session tracking
- User presence
- Multi-device support
- Session attributes
- Connection monitoring

**Example:**
```java
@EventListener
public void handleConnected(SessionConnectedEvent event) {
    String sessionId = accessor.getSessionId();
    // Track session
}
```

### 6. Subscription Pattern
**File:** `SubscriptionPattern.java`

Manages client subscriptions to message destinations.

**Subscription Types:**
- Topic (`/topic/*`) - Broadcast to all subscribers
- Queue (`/queue/*`) - Point-to-point, load balanced
- User (`/user/*`) - User-specific messages

**Key Features:**
- Dynamic subscriptions
- Subscription lifecycle
- Subscriber tracking
- Wildcard destinations

**Example:**
```java
// Client subscribes
stompClient.subscribe('/topic/chat', function(message) {
    console.log(JSON.parse(message.body));
});
```

### 7. Broadcasting Pattern
**File:** `BroadcastingPattern.java`

Sends messages to all subscribers of a topic simultaneously.

**Broadcast Types:**
- Global broadcast
- Topic broadcast
- Filtered broadcast
- Scheduled broadcast

**Use Cases:**
- System announcements
- Live updates
- News feeds
- Stock tickers
- Real-time dashboards

**Example:**
```java
@Scheduled(fixedRate = 10000)
public void broadcastServerTime() {
    messagingTemplate.convertAndSend("/topic/time", 
        LocalDateTime.now());
}
```

### 8. Point-to-Point Messaging Pattern
**File:** `PointToPointMessagingPattern.java`

Direct communication between two clients using queues.

**Key Features:**
- One-to-one messaging
- Queue-based delivery
- Message consumed by single recipient
- Private communication

**Use Cases:**
- Private chat
- Direct notifications
- Task assignment
- File sharing requests

**Example:**
```java
@MessageMapping("/private.send")
public void sendPrivate(PrivateMessage message) {
    messagingTemplate.convertAndSend(
        "/queue/private-" + message.getRecipientId(),
        message
    );
}
```

### 9. User Destination Pattern
**File:** `UserDestinationPattern.java`

Routes messages to specific authenticated users across all their active sessions.

**Key Features:**
- User-based routing
- Multi-device support
- Automatic session resolution
- Authentication required

**Destination Format:**
- `/user/{username}/queue/*`
- `/user/{username}/topic/*`

**Example:**
```java
@MessageMapping("/user.message")
@SendToUser("/queue/reply")
public UserResponse handleMessage(String message, Principal principal) {
    return new UserResponse("Hello " + principal.getName());
}
```

## Comparison Matrix

| Pattern | Complexity | Use Case | Protocol | Authentication | Multi-Device |
|---------|-----------|----------|----------|----------------|--------------|
| STOMP Protocol | Medium | Standard messaging | STOMP | Optional | ✅ Yes |
| Message Broker | Medium-High | Message routing | Any | Optional | ✅ Yes |
| SockJS | Low-Medium | Cross-browser | Any | Optional | ✅ Yes |
| WebSocket Handler | Low | Custom protocol | Raw WS | Optional | ⚠️ Manual |
| Session Management | Medium | Connection tracking | Any | Optional | ✅ Yes |
| Subscription | Low-Medium | Topic management | STOMP | Optional | ✅ Yes |
| Broadcasting | Low | One-to-many | STOMP | No | ✅ Yes |
| Point-to-Point | Low | One-to-one | STOMP | Optional | ⚠️ Limited |
| User Destination | Medium | User-specific | STOMP | ✅ Required | ✅ Yes |

## When to Use Each Pattern

### Use STOMP Protocol When:
- ✅ Need standard messaging protocol
- ✅ Want broad client library support
- ✅ Building chat applications
- ✅ Require publish-subscribe model
- ❌ Need binary data transmission
- ❌ Very high throughput required

### Use Message Broker When:
- ✅ Need scalable message routing
- ✅ Clustering required
- ✅ Message persistence needed
- ✅ Complex routing logic
- ❌ Simple applications
- ❌ Minimal infrastructure desired

### Use SockJS When:
- ✅ Cross-browser compatibility critical
- ✅ Firewall/proxy environments
- ✅ Corporate networks
- ✅ Legacy browser support needed
- ❌ WebSocket-only environment
- ❌ Maximum performance required

### Use WebSocket Handler When:
- ✅ Custom protocol needed
- ✅ Binary data transmission
- ✅ Low-level control required
- ✅ Gaming or streaming applications
- ❌ Standard messaging sufficient
- ❌ Want STOMP features

### Use Session Management When:
- ✅ Track user presence
- ✅ Monitor connections
- ✅ Session-specific data needed
- ✅ Multi-device scenarios
- ❌ Stateless applications
- ❌ Simple use cases

### Use Subscription Pattern When:
- ✅ Dynamic topic subscription
- ✅ Need subscription tracking
- ✅ Filtered message delivery
- ✅ Topic-based routing
- ❌ Simple broadcasting only

### Use Broadcasting When:
- ✅ System-wide announcements
- ✅ Live data feeds
- ✅ Real-time updates for all
- ✅ News or ticker applications
- ❌ Private messaging needed
- ❌ User-specific data

### Use Point-to-Point When:
- ✅ Private chat messages
- ✅ Direct notifications
- ✅ One-to-one communication
- ✅ Task assignments
- ❌ Broadcasting required
- ❌ User authentication available

### Use User Destination When:
- ✅ User authentication available
- ✅ Multi-device support needed
- ✅ Personalized notifications
- ✅ User-specific updates
- ❌ Anonymous users
- ❌ No authentication

## Configuration

### Maven Dependencies

```xml
<dependencies>
    <!-- Spring WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- SockJS -->
    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>sockjs-client</artifactId>
        <version>1.5.1</version>
    </dependency>
    
    <!-- STOMP -->
    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>stomp-websocket</artifactId>
        <version>2.3.4</version>
    </dependency>
    
    <!-- For external broker (RabbitMQ) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-reactor-netty</artifactId>
    </dependency>
    
    <!-- Security (for user destinations) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

### Application Properties

```properties
# WebSocket Configuration
spring.websocket.message-buffer-size=512KB
spring.websocket.send-buffer-size-limit=512KB
spring.websocket.send-time-limit=20000
spring.websocket.http-buffer-size=8KB

# SockJS Configuration
spring.websocket.sockjs.heartbeat-time=25000
spring.websocket.sockjs.disconnect-delay=5000
spring.websocket.sockjs.stream-bytes-limit=524288
spring.websocket.sockjs.http-message-cache-size=1000

# STOMP Configuration
spring.websocket.stomp.heartbeat.client=10000
spring.websocket.stomp.heartbeat.server=10000

# Logging
logging.level.org.springframework.messaging=DEBUG
logging.level.org.springframework.web.socket=DEBUG

# CORS
spring.websocket.allowed-origins=http://localhost:8080,https://example.com
```

### Basic Configuration

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple in-memory broker
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Application destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // With SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Native WebSocket
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
}
```

### External Broker Configuration (RabbitMQ)

```java
@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableStompBrokerRelay("/topic", "/queue", "/exchange")
          .setRelayHost("localhost")
          .setRelayPort(61613)
          .setClientLogin("guest")
          .setClientPasscode("guest")
          .setSystemLogin("guest")
          .setSystemPasscode("guest")
          .setVirtualHost("/")
          .setSystemHeartbeatSendInterval(5000)
          .setSystemHeartbeatReceiveInterval(4000);
}
```

## Best Practices

### 1. Message Design
- Keep messages small and focused
- Use JSON for cross-platform compatibility
- Include timestamps in messages
- Version your message formats
- Document message schemas

### 2. Connection Management
- Implement heartbeat mechanism
- Handle reconnection gracefully
- Clean up resources on disconnect
- Monitor connection health
- Implement connection pooling

### 3. Error Handling
```java
@MessageExceptionHandler
@SendToUser("/queue/errors")
public String handleException(Exception exception) {
    return exception.getMessage();
}
```

### 4. Security
- Always authenticate users for sensitive operations
- Validate message content
- Implement rate limiting
- Use HTTPS/WSS in production
- Sanitize user input

```java
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig 
        extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .simpDestMatchers("/app/**").authenticated()
            .simpSubscribeDestMatchers("/user/**").authenticated()
            .anyMessage().denyAll();
    }
}
```

### 5. Performance Optimization
- Use binary WebSocket for large data
- Implement message compression
- Batch messages when possible
- Use appropriate buffer sizes
- Monitor memory usage

### 6. Scalability
- Use external message broker for clustering
- Implement session affinity
- Consider Redis for session storage
- Monitor broker performance
- Use load balancing

### 7. Testing
```java
@WebMvcTest
@AutoConfigureMockMvc
class WebSocketTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testWebSocketConnection() throws Exception {
        // Test WebSocket handshake
        mockMvc.perform(get("/ws")
                .header("Upgrade", "websocket"))
               .andExpect(status().is1xxInformational());
    }
}
```

## Common Use Cases

### 1. Real-Time Chat Application
```
Patterns: STOMP + Subscription + Broadcasting + User Destination
- Public channels: Broadcasting Pattern
- Private messages: User Destination Pattern
- Typing indicators: Subscription Pattern
```

### 2. Live Dashboard
```
Patterns: SockJS + Broadcasting + Subscription
- Real-time metrics: Broadcasting Pattern
- User preferences: Subscription Pattern
- Cross-browser support: SockJS Pattern
```

### 3. Collaborative Editing
```
Patterns: WebSocket Handler + Session + Broadcasting
- Document changes: Broadcasting Pattern
- User presence: Session Pattern
- Low latency: WebSocket Handler Pattern
```

### 4. Notification System
```
Patterns: User Destination + Point-to-Point
- Personal notifications: User Destination Pattern
- Direct messages: Point-to-Point Pattern
- Multi-device delivery: User Destination Pattern
```

### 5. Gaming Application
```
Patterns: WebSocket Handler + Broadcasting
- Game state updates: Broadcasting Pattern
- Binary data: WebSocket Handler Pattern
- Low latency: Native WebSocket
```

## Security Considerations

### 1. Authentication
```java
@Configuration
public class WebSocketAuthConfig {
    
    @Bean
    public WebSocketHandlerDecoratorFactory authDecoratorFactory() {
        return handler -> new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) 
                    throws Exception {
                // Verify authentication
                Principal principal = session.getPrincipal();
                if (principal == null) {
                    session.close(CloseStatus.NOT_ACCEPTABLE);
                    return;
                }
                super.afterConnectionEstablished(session);
            }
        };
    }
}
```

### 2. Authorization
```java
@MessageMapping("/admin/**")
@PreAuthorize("hasRole('ADMIN')")
public void adminAction(Message message) {
    // Admin-only action
}
```

### 3. Input Validation
```java
@MessageMapping("/message")
public void handleMessage(@Validated @Payload ChatMessage message) {
    // Message automatically validated
}
```

### 4. CSRF Protection
```java
@Override
protected boolean sameOriginDisabled() {
    return false; // Enable same-origin policy
}
```

### 5. Rate Limiting
```java
public class RateLimitingInterceptor implements ChannelInterceptor {
    
    private final RateLimiter rateLimiter;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitExceededException();
        }
        return message;
    }
}
```

## Performance Tips

### 1. Connection Optimization
- Use connection pooling
- Implement heartbeat efficiently
- Close idle connections
- Reuse WebSocket connections

### 2. Message Optimization
- Compress large messages
- Use binary format when appropriate
- Batch related messages
- Implement message pagination

### 3. Broker Optimization
```properties
# Increase buffer sizes for high throughput
spring.websocket.message-buffer-size=1MB
spring.websocket.send-buffer-size-limit=2MB

# Tune heartbeat for your use case
spring.websocket.sockjs.heartbeat-time=20000
```

### 4. Memory Management
- Clean up closed sessions promptly
- Limit message history
- Use weak references for caches
- Monitor heap usage

### 5. Monitoring
```java
@Component
public class WebSocketMetrics {
    
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        meterRegistry.counter("websocket.connections").increment();
    }
    
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        meterRegistry.counter("websocket.disconnections").increment();
    }
}
```

## Troubleshooting

### Common Issues

#### 1. Connection Fails
**Problem:** WebSocket handshake fails

**Solution:**
- Check CORS configuration
- Verify endpoint URL
- Check firewall/proxy settings
- Enable SockJS fallback

#### 2. Messages Not Received
**Problem:** Client doesn't receive messages

**Solution:**
- Verify subscription destination
- Check message broker configuration
- Validate message format
- Enable debug logging

#### 3. Session Timeout
**Problem:** Connections drop unexpectedly

**Solution:**
- Implement heartbeat
- Adjust timeout settings
- Handle reconnection
- Monitor network quality

#### 4. Memory Leak
**Problem:** Memory usage grows over time

**Solution:**
- Clean up closed sessions
- Limit message history
- Remove stale subscriptions
- Profile memory usage

#### 5. Scalability Issues
**Problem:** Performance degrades with many connections

**Solution:**
- Use external broker
- Implement load balancing
- Optimize message size
- Consider clustering

### Debug Configuration

```properties
# Enable detailed logging
logging.level.org.springframework.web.socket=TRACE
logging.level.org.springframework.messaging=TRACE
logging.level.org.springframework.messaging.simp=TRACE

# Log all STOMP frames
logging.level.org.springframework.messaging.simp.stomp=TRACE
```

## Summary

This collection provides comprehensive examples of all major Spring WebSocket patterns. Choose the appropriate pattern based on:

- **Communication Model** - One-to-one, one-to-many, or many-to-many
- **Authentication Needs** - Anonymous vs authenticated users
- **Protocol Requirements** - STOMP, raw WebSocket, or custom
- **Browser Support** - Modern browsers vs legacy support
- **Scalability** - Single instance vs clustered deployment
- **Data Type** - Text messages vs binary data
- **Complexity** - Simple broadcasting vs complex routing

Each pattern has its place in a well-designed real-time application. Understanding when and how to use each pattern will help you build robust, scalable WebSocket applications.

## Additional Resources

- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
- [STOMP Protocol Specification](https://stomp.github.io/)
- [SockJS Documentation](https://github.com/sockjs/sockjs-protocol)
- [WebSocket API (MDN)](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)
- [RFC 6455 - WebSocket Protocol](https://tools.ietf.org/html/rfc6455)

---

**Note:** All patterns are demonstrated with complete, runnable Spring Boot applications. Each file contains detailed JavaDoc comments, examples, client-side JavaScript code, and configuration examples.
