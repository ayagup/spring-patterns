### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 1:58:15 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **WebSocket Patterns**
> - STOMP Protocol Pattern
> - Message Broker Pattern
> - SockJS Pattern
> - WebSocket Handler Pattern
> - WebSocket Session Pattern
> - Subscription Pattern
> - Broadcasting Pattern
> - Point-to-Point Messaging Pattern
> - User Destination Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring WebSocket application demonstrating all these patterns.
> 
> ## 1. Project Dependencies
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
>          http://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <groupId>com.example</groupId>
>     <artifactId>websocket-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>     </parent>
>     
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-websocket</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-security</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>     </dependencies>
>     
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## 2. Main Application
> 
> ```java src/main/java/com/example/websocket/WebSocketPatternsApplication.java
> package com.example.websocket;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> 
> @SpringBootApplication
> public class WebSocketPatternsApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(WebSocketPatternsApplication.class, args);
>     }
> }
> ```
> 
> ## 3. STOMP Protocol Pattern Configuration
> 
> ```java src/main/java/com/example/websocket/config/WebSocketConfig.java
> package com.example.websocket.config;
> 
> import com.example.websocket.handler.CustomWebSocketHandler;
> import com.example.websocket.interceptor.HttpHandshakeInterceptor;
> import com.example.websocket.interceptor.ChannelInterceptor;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.messaging.simp.config.ChannelRegistration;
> import org.springframework.messaging.simp.config.MessageBrokerRegistry;
> import org.springframework.web.socket.config.annotation.*;
> 
> /**
>  * STOMP Protocol Pattern - Configures STOMP over WebSocket
>  * Message Broker Pattern - Sets up message broker for pub/sub
>  * SockJS Pattern - Enables SockJS fallback support
>  */
> @Configuration
> @EnableWebSocketMessageBroker
> public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
> 
>     private final ChannelInterceptor channelInterceptor;
> 
>     public WebSocketConfig(ChannelInterceptor channelInterceptor) {
>         this.channelInterceptor = channelInterceptor;
>     }
> 
>     /**
>      * STOMP Protocol Pattern & Message Broker Pattern
>      * Configures message broker with simple in-memory broker
>      */
>     @Override
>     public void configureMessageBroker(MessageBrokerRegistry config) {
>         // Message Broker Pattern - Topics for broadcasting
>         config.enableSimpleBroker("/topic", "/queue", "/user");
>         
>         // Application destination prefix for client messages
>         config.setApplicationDestinationPrefixes("/app");
>         
>         // User Destination Pattern - Prefix for user-specific destinations
>         config.setUserDestinationPrefix("/user");
>     }
> 
>     /**
>      * SockJS Pattern - Registers STOMP endpoints with SockJS fallback
>      */
>     @Override
>     public void registerStompEndpoints(StompEndpointRegistry registry) {
>         registry.addEndpoint("/ws")
>                 .setAllowedOriginPatterns("*")
>                 .addInterceptors(new HttpHandshakeInterceptor())
>                 .withSockJS();  // SockJS Pattern - Enables fallback options
>     }
> 
>     /**
>      * Subscription Pattern - Adds interceptor for subscription handling
>      */
>     @Override
>     public void configureClientInboundChannel(ChannelRegistration registration) {
>         registration.interceptors(channelInterceptor);
>     }
> }
> ```
> 
> ## 4. WebSocket Handler Pattern
> 
> ```java src/main/java/com/example/websocket/handler/CustomWebSocketHandler.java
> package com.example.websocket.handler;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Component;
> import org.springframework.web.socket.CloseStatus;
> import org.springframework.web.socket.TextMessage;
> import org.springframework.web.socket.WebSocketSession;
> import org.springframework.web.socket.handler.TextWebSocketHandler;
> 
> import java.io.IOException;
> import java.util.Map;
> import java.util.concurrent.ConcurrentHashMap;
> 
> /**
>  * WebSocket Handler Pattern - Custom handler for raw WebSocket connections
>  * WebSocket Session Pattern - Manages WebSocket sessions
>  */
> @Slf4j
> @Component
> public class CustomWebSocketHandler extends TextWebSocketHandler {
> 
>     // WebSocket Session Pattern - Store active sessions
>     private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
> 
>     @Override
>     public void afterConnectionEstablished(WebSocketSession session) throws Exception {
>         sessions.put(session.getId(), session);
>         log.info("WebSocket connection established: {}", session.getId());
>         
>         session.sendMessage(new TextMessage("Welcome! Your session ID: " + session.getId()));
>     }
> 
>     @Override
>     protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
>         log.info("Received message from {}: {}", session.getId(), message.getPayload());
>         
>         // Echo back to sender
>         session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
>         
>         // Broadcasting Pattern - Send to all connected clients
>         broadcastMessage("Broadcast: " + message.getPayload(), session.getId());
>     }
> 
>     @Override
>     public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
>         sessions.remove(session.getId());
>         log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
>     }
> 
>     /**
>      * Broadcasting Pattern - Send message to all sessions except sender
>      */
>     private void broadcastMessage(String message, String excludeSessionId) {
>         sessions.values().stream()
>                 .filter(session -> !session.getId().equals(excludeSessionId))
>                 .forEach(session -> {
>                     try {
>                         session.sendMessage(new TextMessage(message));
>                     } catch (IOException e) {
>                         log.error("Error broadcasting message", e);
>                     }
>                 });
>     }
> }
> ```
> 
> ## 5. Message Models
> 
> ```java src/main/java/com/example/websocket/model/ChatMessage.java
> package com.example.websocket.model;
> 
> import lombok.AllArgsConstructor;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> @Data
> @NoArgsConstructor
> @AllArgsConstructor
> public class ChatMessage {
>     private MessageType type;
>     private String content;
>     private String sender;
>     private long timestamp;
> 
>     public enum MessageType {
>         CHAT, JOIN, LEAVE
>     }
> }
> ```
> 
> ```java src/main/java/com/example/websocket/model/Notification.java
> package com.example.websocket.model;
> 
> import lombok.AllArgsConstructor;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> @Data
> @NoArgsConstructor
> @AllArgsConstructor
> public class Notification {
>     private String message;
>     private NotificationType type;
>     private String recipient;
>     private long timestamp;
> 
>     public enum NotificationType {
>         INFO, WARNING, ERROR, SUCCESS
>     }
> }
> ```
> 
> ## 6. Broadcasting Pattern Controller
> 
> ```java src/main/java/com/example/websocket/controller/BroadcastController.java
> package com.example.websocket.controller;
> 
> import com.example.websocket.model.ChatMessage;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.messaging.handler.annotation.MessageMapping;
> import org.springframework.messaging.handler.annotation.Payload;
> import org.springframework.messaging.handler.annotation.SendTo;
> import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
> import org.springframework.stereotype.Controller;
> 
> /**
>  * Broadcasting Pattern - Demonstrates message broadcasting to all subscribers
>  */
> @Slf4j
> @Controller
> public class BroadcastController {
> 
>     /**
>      * Broadcasting Pattern - Broadcasts chat messages to all subscribers of /topic/public
>      */
>     @MessageMapping("/chat.send")
>     @SendTo("/topic/public")
>     public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
>         log.info("Broadcasting message: {}", chatMessage);
>         chatMessage.setTimestamp(System.currentTimeMillis());
>         return chatMessage;
>     }
> 
>     /**
>      * Broadcasting Pattern - Handles user join events
>      */
>     @MessageMapping("/chat.join")
>     @SendTo("/topic/public")
>     public ChatMessage addUser(@Payload ChatMessage chatMessage,
>                                 SimpMessageHeaderAccessor headerAccessor) {
>         // WebSocket Session Pattern - Store username in session
>         headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
>         
>         chatMessage.setType(ChatMessage.MessageType.JOIN);
>         chatMessage.setTimestamp(System.currentTimeMillis());
>         log.info("User joined: {}", chatMessage.getSender());
>         
>         return chatMessage;
>     }
> }
> ```
> 
> ## 7. Point-to-Point Messaging Pattern
> 
> ```java src/main/java/com/example/websocket/controller/PrivateMessageController.java
> package com.example.websocket.controller;
> 
> import com.example.websocket.model.Notification;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.messaging.handler.annotation.MessageMapping;
> import org.springframework.messaging.handler.annotation.Payload;
> import org.springframework.messaging.simp.SimpMessagingTemplate;
> import org.springframework.stereotype.Controller;
> 
> import java.security.Principal;
> 
> /**
>  * Point-to-Point Messaging Pattern - Sends messages to specific users
>  * User Destination Pattern - Uses user-specific destinations
>  */
> @Slf4j
> @Controller
> public class PrivateMessageController {
> 
>     private final SimpMessagingTemplate messagingTemplate;
> 
>     public PrivateMessageController(SimpMessagingTemplate messagingTemplate) {
>         this.messagingTemplate = messagingTemplate;
>     }
> 
>     /**
>      * Point-to-Point Messaging Pattern - Send notification to specific user
>      */
>     @MessageMapping("/private.message")
>     public void sendPrivateMessage(@Payload Notification notification) {
>         log.info("Sending private message to: {}", notification.getRecipient());
>         
>         notification.setTimestamp(System.currentTimeMillis());
>         
>         // User Destination Pattern - Send to specific user's queue
>         messagingTemplate.convertAndSendToUser(
>                 notification.getRecipient(),
>                 "/queue/notifications",
>                 notification
>         );
>     }
> 
>     /**
>      * Point-to-Point Messaging Pattern - Direct message between users
>      */
>     @MessageMapping("/direct.message")
>     public void sendDirectMessage(@Payload ChatMessage message, Principal principal) {
>         log.info("Direct message from {} to {}", principal.getName(), message.getSender());
>         
>         message.setTimestamp(System.currentTimeMillis());
>         
>         // Send to specific user
>         messagingTemplate.convertAndSendToUser(
>                 message.getSender(),
>                 "/queue/messages",
>                 message
>         );
>     }
> }
> ```
> 
> ## 8. Subscription Pattern Service
> 
> ```java src/main/java/com/example/websocket/service/SubscriptionService.java
> package com.example.websocket.service;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> 
> import java.util.*;
> import java.util.concurrent.ConcurrentHashMap;
> 
> /**
>  * Subscription Pattern - Manages topic subscriptions
>  */
> @Slf4j
> @Service
> public class SubscriptionService {
> 
>     // Maps session ID to list of subscribed topics
>     private final Map<String, Set<String>> userSubscriptions = new ConcurrentHashMap<>();
>     
>     // Maps topic to list of subscriber session IDs
>     private final Map<String, Set<String>> topicSubscribers = new ConcurrentHashMap<>();
> 
>     /**
>      * Subscription Pattern - Subscribe user to a topic
>      */
>     public void subscribe(String sessionId, String topic) {
>         userSubscriptions.computeIfAbsent(sessionId, k -> new HashSet<>()).add(topic);
>         topicSubscribers.computeIfAbsent(topic, k -> new HashSet<>()).add(sessionId);
>         
>         log.info("Session {} subscribed to topic: {}", sessionId, topic);
>     }
> 
>     /**
>      * Subscription Pattern - Unsubscribe user from a topic
>      */
>     public void unsubscribe(String sessionId, String topic) {
>         Set<String> topics = userSubscriptions.get(sessionId);
>         if (topics != null) {
>             topics.remove(topic);
>         }
>         
>         Set<String> subscribers = topicSubscribers.get(topic);
>         if (subscribers != null) {
>             subscribers.remove(sessionId);
>         }
>         
>         log.info("Session {} unsubscribed from topic: {}", sessionId, topic);
>     }
> 
>     /**
>      * Subscription Pattern - Remove all subscriptions for a session
>      */
>     public void removeSession(String sessionId) {
>         Set<String> topics = userSubscriptions.remove(sessionId);
>         if (topics != null) {
>             topics.forEach(topic -> {
>                 Set<String> subscribers = topicSubscribers.get(topic);
>                 if (subscribers != null) {
>                     subscribers.remove(sessionId);
>                 }
>             });
>         }
>         
>         log.info("Removed all subscriptions for session: {}", sessionId);
>     }
> 
>     /**
>      * Get all subscribers for a topic
>      */
>     public Set<String> getTopicSubscribers(String topic) {
>         return topicSubscribers.getOrDefault(topic, Collections.emptySet());
>     }
> 
>     /**
>      * Get all topics for a session
>      */
>     public Set<String> getUserSubscriptions(String sessionId) {
>         return userSubscriptions.getOrDefault(sessionId, Collections.emptySet());
>     }
> }
> ```
> 
> ## 9. Channel Interceptor (Subscription Pattern)
> 
> ```java src/main/java/com/example/websocket/interceptor/ChannelInterceptor.java
> package com.example.websocket.interceptor;
> 
> import com.example.websocket.service.SubscriptionService;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.messaging.Message;
> import org.springframework.messaging.MessageChannel;
> import org.springframework.messaging.simp.stomp.StompCommand;
> import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
> import org.springframework.messaging.support.ChannelInterceptor;
> import org.springframework.messaging.support.MessageHeaderAccessor;
> import org.springframework.stereotype.Component;
> 
> /**
>  * Subscription Pattern - Intercepts subscription and unsubscription events
>  */
> @Slf4j
> @Component
> public class ChannelInterceptor implements org.springframework.messaging.support.ChannelInterceptor {
> 
>     private final SubscriptionService subscriptionService;
> 
>     public ChannelInterceptor(SubscriptionService subscriptionService) {
>         this.subscriptionService = subscriptionService;
>     }
> 
>     @Override
>     public Message<?> preSend(Message<?> message, MessageChannel channel) {
>         StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
>         
>         if (accessor != null) {
>             StompCommand command = accessor.getCommand();
>             
>             if (StompCommand.SUBSCRIBE.equals(command)) {
>                 // Subscription Pattern - Track new subscription
>                 String sessionId = accessor.getSessionId();
>                 String destination = accessor.getDestination();
>                 subscriptionService.subscribe(sessionId, destination);
>                 log.info("SUBSCRIBE: Session {} to {}", sessionId, destination);
>                 
>             } else if (StompCommand.UNSUBSCRIBE.equals(command)) {
>                 // Subscription Pattern - Track unsubscription
>                 String sessionId = accessor.getSessionId();
>                 String subscriptionId = accessor.getSubscriptionId();
>                 log.info("UNSUBSCRIBE: Session {} subscription {}", sessionId, subscriptionId);
>                 
>             } else if (StompCommand.DISCONNECT.equals(command)) {
>                 // Clean up subscriptions on disconnect
>                 String sessionId = accessor.getSessionId();
>                 subscriptionService.removeSession(sessionId);
>                 log.info("DISCONNECT: Session {}", sessionId);
>             }
>         }
>         
>         return message;
>     }
> }
> ```
> 
> ## 10. HTTP Handshake Interceptor
> 
> ```java src/main/java/com/example/websocket/interceptor/HttpHandshakeInterceptor.java
> package com.example.websocket.interceptor;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.server.ServerHttpRequest;
> import org.springframework.http.server.ServerHttpResponse;
> import org.springframework.web.socket.WebSocketHandler;
> import org.springframework.web.socket.server.HandshakeInterceptor;
> 
> import java.util.Map;
> 
> /**
>  * WebSocket Session Pattern - Intercepts handshake to add session attributes
>  */
> @Slf4j
> public class HttpHandshakeInterceptor implements HandshakeInterceptor {
> 
>     @Override
>     public boolean beforeHandshake(ServerHttpRequest request, 
>                                    ServerHttpResponse response,
>                                    WebSocketHandler wsHandler, 
>                                    Map<String, Object> attributes) throws Exception {
>         
>         log.info("WebSocket handshake from: {}", request.getRemoteAddress());
>         
>         // WebSocket Session Pattern - Add custom attributes to session
>         attributes.put("connectionTime", System.currentTimeMillis());
>         attributes.put("remoteAddress", request.getRemoteAddress());
>         
>         return true;
>     }
> 
>     @Override
>     public void afterHandshake(ServerHttpRequest request, 
>                               ServerHttpResponse response,
>                               WebSocketHandler wsHandler, 
>                               Exception exception) {
>         log.info("WebSocket handshake completed");
>     }
> }
> ```
> 
> ## 11. Scheduled Broadcasting Example
> 
> ```java src/main/java/com/example/websocket/service/BroadcastScheduler.java
> package com.example.websocket.service;
> 
> import com.example.websocket.model.Notification;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.messaging.simp.SimpMessagingTemplate;
> import org.springframework.scheduling.annotation.EnableScheduling;
> import org.springframework.scheduling.annotation.Scheduled;
> import org.springframework.stereotype.Service;
> 
> /**
>  * Broadcasting Pattern - Scheduled broadcasting example
>  */
> @Slf4j
> @Service
> @EnableScheduling
> public class BroadcastScheduler {
> 
>     private final SimpMessagingTemplate messagingTemplate;
> 
>     public BroadcastScheduler(SimpMessagingTemplate messagingTemplate) {
>         this.messagingTemplate = messagingTemplate;
>     }
> 
>     /**
>      * Broadcasting Pattern - Broadcast system status every 30 seconds
>      */
>     @Scheduled(fixedDelay = 30000)
>     public void broadcastSystemStatus() {
>         Notification notification = new Notification(
>                 "System health check: All systems operational",
>                 Notification.NotificationType.INFO,
>                 null,
>                 System.currentTimeMillis()
>         );
>         
>         messagingTemplate.convertAndSend("/topic/system", notification);
>         log.debug("Broadcasted system status");
>     }
> }
> ```
> 
> ## 12. WebSocket Event Listener
> 
> ```java src/main/java/com/example/websocket/listener/WebSocketEventListener.java
> package com.example.websocket.listener;
> 
> import com.example.websocket.model.ChatMessage;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.context.event.EventListener;
> import org.springframework.messaging.simp.SimpMessageSendingOperations;
> import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
> import org.springframework.stereotype.Component;
> import org.springframework.web.socket.messaging.SessionConnectedEvent;
> import org.springframework.web.socket.messaging.SessionDisconnectEvent;
> 
> /**
>  * WebSocket Session Pattern - Listens to WebSocket lifecycle events
>  */
> @Slf4j
> @Component
> public class WebSocketEventListener {
> 
>     private final SimpMessageSendingOperations messagingTemplate;
> 
>     public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
>         this.messagingTemplate = messagingTemplate;
>     }
> 
>     @EventListener
>     public void handleWebSocketConnectListener(SessionConnectedEvent event) {
>         log.info("New WebSocket connection established");
>     }
> 
>     @EventListener
>     public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
>         StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
>         
>         String username = (String) headerAccessor.getSessionAttributes().get("username");
>         
>         if (username != null) {
>             log.info("User disconnected: {}", username);
>             
>             ChatMessage chatMessage = new ChatMessage();
>             chatMessage.setType(ChatMessage.MessageType.LEAVE);
>             chatMessage.setSender(username);
>             chatMessage.setTimestamp(System.currentTimeMillis());
>             
>             messagingTemplate.convertAndSend("/topic/public", chatMessage);
>         }
>     }
> }
> ```
> 
> ## 13. Application Properties
> 
> ```properties src/main/resources/application.properties
> # Server Configuration
> server.port=8080
> 
> # WebSocket Configuration
> spring.websocket.max-text-message-size=8192
> spring.websocket.max-binary-message-size=8192
> 
> # Logging
> logging.level.com.example.websocket=DEBUG
> logging.level.org.springframework.web.socket=DEBUG
> logging.level.org.springframework.messaging=DEBUG
> ```
> 
> ## 14. HTML Client Example
> 
> ```html src/main/resources/static/index.html
> <!DOCTYPE html>
> <html>
> <head>
>     <title>WebSocket Patterns Demo</title>
>     <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
>     <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
>     <style>
>         body { font-family: Arial, sans-serif; margin: 20px; }
>         .container { max-width: 800px; margin: 0 auto; }
>         .messages { border: 1px solid #ccc; height: 300px; overflow-y: scroll; padding: 10px; margin: 10px 0; }
>         .message { margin: 5px 0; padding: 5px; background: #f0f0f0; border-radius: 3px; }
>         input, button { margin: 5px; padding: 8px; }
>     </style>
> </head>
> <body>
>     <div class="container">
>         <h1>WebSocket Patterns Demo</h1>
>         
>         <div>
>             <input type="text" id="username" placeholder="Enter username">
>             <button onclick="connect()">Connect</button>
>             <button onclick="disconnect()">Disconnect</button>
>         </div>
>         
>         <h3>Public Chat (Broadcasting Pattern)</h3>
>         <div id="publicMessages" class="messages"></div>
>         <input type="text" id="publicMessage" placeholder="Type a message">
>         <button onclick="sendPublicMessage()">Send to All</button>
>         
>         <h3>Private Messages (Point-to-Point Pattern)</h3>
>         <div id="privateMessages" class="messages"></div>
>         <input type="text" id="recipient" placeholder="Recipient username">
>         <input type="text" id="privateMessage" placeholder="Private message">
>         <button onclick="sendPrivateMessage()">Send Private</button>
>     </div>
> 
>     <script>
>         let stompClient = null;
>         let username = null;
> 
>         function connect() {
>             username = document.getElementById('username').value;
>             if (!username) {
>                 alert('Please enter a username');
>                 return;
>             }
> 
>             // SockJS Pattern - Create SockJS connection
>             const socket = new SockJS('/ws');
>             
>             // STOMP Protocol Pattern - Create STOMP client
>             stompClient = Stomp.over(socket);
> 
>             stompClient.connect({}, function(frame) {
>                 console.log('Connected: ' + frame);
> 
>                 // Subscription Pattern - Subscribe to public topic
>                 stompClient.subscribe('/topic/public', function(message) {
>                     showPublicMessage(JSON.parse(message.body));
>                 });
> 
>                 // User Destination Pattern - Subscribe to private queue
>                 stompClient.subscribe('/user/queue/notifications', function(message) {
>                     showPrivateMessage(JSON.parse(message.body));
>                 });
> 
>                 // Send join notification
>                 stompClient.send("/app/chat.join", {}, JSON.stringify({
>                     sender: username,
>                     type: 'JOIN'
>                 }));
>             });
>         }
> 
>         function disconnect() {
>             if (stompClient !== null) {
>                 stompClient.disconnect();
>             }
>             console.log("Disconnected");
>         }
> 
>         function sendPublicMessage() {
>             const messageContent = document.getElementById('publicMessage').value;
>             if (messageContent && stompClient) {
>                 const chatMessage = {
>                     sender: username,
>                     content: messageContent,
>                     type: 'CHAT'
>                 };
>                 stompClient.send("/app/chat.send", {}, JSON.stringify(chatMessage));
>                 document.getElementById('publicMessage').value = '';
>             }
>         }
> 
>         function sendPrivateMessage() {
>             const recipient = document.getElementById('recipient').value;
>             const messageContent = document.getElementById('privateMessage').value;
>             
>             if (recipient && messageContent && stompClient) {
>                 const notification = {
>                     recipient: recipient,
>                     message: messageContent,
>                     type: 'INFO'
>                 };
>                 stompClient.send("/app/private.message", {}, JSON.stringify(notification));
>                 document.getElementById('privateMessage').value = '';
>             }
>         }
> 
>         function showPublicMessage(message) {
>             const messagesDiv = document.getElementById('publicMessages');
>             const messageElement = document.createElement('div');
>             messageElement.className = 'message';
>             messageElement.textContent = `[${message.type}] ${message.sender}: ${message.content || 'joined/left'}`;
>             messagesDiv.appendChild(messageElement);
>             messagesDiv.scrollTop = messagesDiv.scrollHeight;
>         }
> 
>         function showPrivateMessage(message) {
>             const messagesDiv = document.getElementById('privateMessages');
>             const messageElement = document.createElement('div');
>             messageElement.className = 'message';
>             messageElement.textContent = `[PRIVATE] ${message.message}`;
>             messagesDiv.appendChild(messageElement);
>             messagesDiv.scrollTop = messagesDiv.scrollHeight;
>         }
>     </script>
> </body>
> </html>
> ```
> 
> ## Summary of Patterns Demonstrated:
> 
> 1. **STOMP Protocol Pattern** - `WebSocketConfig.java` configures STOMP over WebSocket
> 2. **Message Broker Pattern** - `WebSocketConfig.java` sets up topic/queue brokers
> 3. **SockJS Pattern** - `WebSocketConfig.java` enables SockJS fallback, `index.html` uses SockJS client
> 4. **WebSocket Handler Pattern** - `CustomWebSocketHandler.java` implements custom handler
> 5. **WebSocket Session Pattern** - Session management in handlers and interceptors
> 6. **Subscription Pattern** - `SubscriptionService.java` and `ChannelInterceptor.java`
> 7. **Broadcasting Pattern** - `BroadcastController.java` and `BroadcastScheduler.java`
> 8. **Point-to-Point Messaging Pattern** - `PrivateMessageController.java`
> 9. **User Destination Pattern** - User-specific routing in `PrivateMessageController.java`
> 
> Run the application and open `http://localhost:8080` to test all patterns!