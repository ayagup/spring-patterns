package com.example.websocket.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message Broker Pattern
 * ======================
 * 
 * The Message Broker Pattern provides a centralized component that handles message routing,
 * delivery, transformation, and persistence in a messaging system. Spring supports both
 * simple in-memory brokers and external message brokers like RabbitMQ, ActiveMQ, etc.
 * 
 * Key Concepts:
 * ------------
 * 1. Simple Broker:
 *    - In-memory message broker
 *    - Lightweight and fast
 *    - No clustering support
 *    - Good for single-instance applications
 * 
 * 2. External Broker (STOMP Relay):
 *    - RabbitMQ, ActiveMQ, Apache Apollo
 *    - Full-featured message broker
 *    - Clustering and persistence support
 *    - Scalability and reliability
 * 
 * 3. Message Flow:
 *    - Client sends message to application destination
 *    - Application processes and forwards to broker
 *    - Broker routes to subscribed clients
 *    - Clients receive messages
 * 
 * 4. Broker Features:
 *    - Message routing and delivery
 *    - Publish-subscribe model
 *    - Point-to-point messaging
 *    - Message transformation
 *    - Dead letter queues
 *    - Message acknowledgment
 * 
 * Use Cases:
 * ---------
 * - Real-time notifications
 * - Chat applications
 * - Live feeds and updates
 * - Event broadcasting
 * - Command and control systems
 * - IoT device communication
 * 
 * Broker Types:
 * ------------
 * 1. Simple Broker:
 *    - config.enableSimpleBroker("/topic", "/queue")
 *    - No external dependencies
 *    - Limited features
 * 
 * 2. STOMP Broker Relay:
 *    - config.enableStompBrokerRelay("/topic", "/queue")
 *    - Requires external broker (RabbitMQ, ActiveMQ)
 *    - Full messaging features
 * 
 * Best Practices:
 * --------------
 * 1. Use external broker for production
 * 2. Implement message acknowledgment
 * 3. Handle connection failures
 * 4. Monitor broker health
 * 5. Implement message retry logic
 * 6. Use message persistence for critical messages
 * 7. Implement dead letter queues
 * 8. Monitor message throughput
 * 
 * Dependencies:
 * ------------
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-websocket</artifactId>
 * </dependency>
 * 
 * For RabbitMQ STOMP relay:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-reactor-netty</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>io.projectreactor.netty</groupId>
 *     <artifactId>reactor-netty</artifactId>
 * </dependency>
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
@EnableScheduling
public class MessageBrokerPattern {

    public static void main(String[] args) {
        SpringApplication.run(MessageBrokerPattern.class, args);
    }
}

/**
 * WebSocket Message Broker Configuration
 */
@Configuration
@EnableWebSocketMessageBroker
class MessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker
     * 
     * Simple Broker Configuration (In-Memory):
     * - Lightweight and fast
     * - No external dependencies
     * - Not suitable for clustering
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory broker
        registry.enableSimpleBroker("/topic", "/queue", "/user")
                .setHeartbeatValue(new long[]{10000, 10000})  // Send/receive heartbeat
                .setTaskScheduler(taskScheduler());           // For heartbeat support

        // Application destination prefix
        registry.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix
        registry.setUserDestinationPrefix("/user");

        /*
         * External Broker Configuration (RabbitMQ):
         * 
         * registry.enableStompBrokerRelay("/topic", "/queue", "/exchange")
         *     .setRelayHost("localhost")
         *     .setRelayPort(61613)
         *     .setClientLogin("guest")
         *     .setClientPasscode("guest")
         *     .setSystemLogin("guest")
         *     .setSystemPasscode("guest")
         *     .setVirtualHost("/")
         *     .setSystemHeartbeatSendInterval(5000)
         *     .setSystemHeartbeatReceiveInterval(4000)
         *     .setUserDestinationBroadcast("/topic/unresolved-user-destination")
         *     .setUserRegistryBroadcast("/topic/registry-broadcast");
         */
    }

    /**
     * Register STOMP endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-broker")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Configure client inbound channel
     * Intercept messages from clients before they reach the broker
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new BrokerChannelInterceptor());
    }

    /**
     * Configure client outbound channel
     * Intercept messages from broker before they reach clients
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new BrokerChannelInterceptor());
    }

    @Bean
    public org.springframework.scheduling.TaskScheduler taskScheduler() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler scheduler = 
            new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
}

/**
 * Broker Channel Interceptor
 * Intercepts messages flowing through the broker
 */
class BrokerChannelInterceptor implements ChannelInterceptor {

    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(BrokerChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            
            if (StompCommand.CONNECT.equals(command)) {
                logger.info("Client connecting: {}", accessor.getSessionId());
            } else if (StompCommand.DISCONNECT.equals(command)) {
                logger.info("Client disconnecting: {}", accessor.getSessionId());
            } else if (StompCommand.SUBSCRIBE.equals(command)) {
                logger.info("Client subscribing to: {}", accessor.getDestination());
            } else if (StompCommand.SEND.equals(command)) {
                logger.info("Client sending to: {}", accessor.getDestination());
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        if (!sent) {
            logger.warn("Message not sent: {}", message);
        }
    }
}

/**
 * Message Broker Controller
 * Handles messages and routes them through the broker
 */
@Controller
class BrokerMessageController {

    /**
     * Receive message and broadcast to all subscribers
     */
    @MessageMapping("/broadcast")
    @SendTo("/topic/broadcast")
    public BrokerMessage handleBroadcast(BrokerMessage message) {
        message.setTimestamp(LocalDateTime.now().toString());
        message.setBrokerProcessed(true);
        return message;
    }

    /**
     * Route message to specific queue
     */
    @MessageMapping("/queue.send")
    @SendTo("/queue/messages")
    public BrokerMessage handleQueue(BrokerMessage message) {
        message.setTimestamp(LocalDateTime.now().toString());
        message.setBrokerProcessed(true);
        return message;
    }

    /**
     * Handle notification messages
     */
    @MessageMapping("/notify")
    @SendTo("/topic/notifications")
    public Notification handleNotification(Notification notification) {
        notification.setTimestamp(LocalDateTime.now().toString());
        notification.setDelivered(true);
        return notification;
    }
}

/**
 * Broker Service
 * Manages message broker operations and statistics
 */
@Service
class BrokerService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, BrokerStats> brokerStats = new ConcurrentHashMap<>();
    private final List<BrokerMessage> messageHistory = Collections.synchronizedList(new ArrayList<>());

    public BrokerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send message through broker to topic
     */
    public void sendToTopic(String topic, Object message) {
        messagingTemplate.convertAndSend("/topic/" + topic, message);
        recordMessage(topic, "TOPIC");
    }

    /**
     * Send message through broker to queue
     */
    public void sendToQueue(String queue, Object message) {
        messagingTemplate.convertAndSend("/queue/" + queue, message);
        recordMessage(queue, "QUEUE");
    }

    /**
     * Send message to specific user
     */
    public void sendToUser(String username, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(username, destination, message);
        recordMessage(destination, "USER");
    }

    /**
     * Scheduled task to send periodic updates through broker
     */
    @Scheduled(fixedRate = 30000)
    public void sendPeriodicUpdate() {
        ServerStatus status = new ServerStatus(
            "RUNNING",
            LocalDateTime.now().toString(),
            brokerStats.size(),
            messageHistory.size()
        );
        
        sendToTopic("server-status", status);
    }

    /**
     * Get broker statistics
     */
    public BrokerStats getBrokerStats(String destination) {
        return brokerStats.computeIfAbsent(destination, 
            k -> new BrokerStats(destination));
    }

    /**
     * Record message for statistics
     */
    private void recordMessage(String destination, String type) {
        BrokerStats stats = brokerStats.computeIfAbsent(
            destination, k -> new BrokerStats(destination));
        stats.incrementMessageCount();
        stats.setLastMessageTime(LocalDateTime.now().toString());
        stats.setType(type);
    }

    public List<BrokerMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }

    public Map<String, BrokerStats> getAllStats() {
        return new HashMap<>(brokerStats);
    }
}

/**
 * Broker Message Model
 */
class BrokerMessage {
    private String id;
    private String content;
    private String sender;
    private String destination;
    private String timestamp;
    private boolean brokerProcessed;
    private Map<String, Object> headers;

    public BrokerMessage() {
        this.id = UUID.randomUUID().toString();
        this.headers = new HashMap<>();
    }

    public BrokerMessage(String content, String sender, String destination) {
        this();
        this.content = content;
        this.sender = sender;
        this.destination = destination;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isBrokerProcessed() { return brokerProcessed; }
    public void setBrokerProcessed(boolean brokerProcessed) { this.brokerProcessed = brokerProcessed; }

    public Map<String, Object> getHeaders() { return headers; }
    public void setHeaders(Map<String, Object> headers) { this.headers = headers; }
}

/**
 * Notification Model
 */
class Notification {
    private String message;
    private String level; // INFO, WARNING, ERROR
    private String timestamp;
    private boolean delivered;

    public Notification() {}

    public Notification(String message, String level) {
        this.message = message;
        this.level = level;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }
}

/**
 * Broker Statistics
 */
class BrokerStats {
    private String destination;
    private String type;
    private long messageCount;
    private String lastMessageTime;
    private String createdAt;

    public BrokerStats(String destination) {
        this.destination = destination;
        this.messageCount = 0;
        this.createdAt = LocalDateTime.now().toString();
    }

    public void incrementMessageCount() {
        this.messageCount++;
    }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getMessageCount() { return messageCount; }
    public void setMessageCount(long messageCount) { this.messageCount = messageCount; }

    public String getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public String getCreatedAt() { return createdAt; }
}

/**
 * Server Status Model
 */
class ServerStatus {
    private String status;
    private String timestamp;
    private int activeDestinations;
    private int totalMessages;

    public ServerStatus(String status, String timestamp, int activeDestinations, int totalMessages) {
        this.status = status;
        this.timestamp = timestamp;
        this.activeDestinations = activeDestinations;
        this.totalMessages = totalMessages;
    }

    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }
    public int getActiveDestinations() { return activeDestinations; }
    public int getTotalMessages() { return totalMessages; }
}

/*
 * RabbitMQ Configuration for External Broker:
 * ===========================================
 * 
 * 1. Install RabbitMQ with STOMP plugin:
 *    rabbitmq-plugins enable rabbitmq_stomp
 *    rabbitmq-plugins enable rabbitmq_web_stomp
 * 
 * 2. application.properties:
 *    spring.rabbitmq.host=localhost
 *    spring.rabbitmq.port=5672
 *    spring.rabbitmq.username=guest
 *    spring.rabbitmq.password=guest
 *    
 *    # STOMP over WebSocket
 *    spring.rabbitmq.stomp.port=61613
 * 
 * 3. Docker Compose Example:
 *    version: '3.8'
 *    services:
 *      rabbitmq:
 *        image: rabbitmq:3-management
 *        ports:
 *          - "5672:5672"   # AMQP
 *          - "15672:15672" # Management UI
 *          - "61613:61613" # STOMP
 *        environment:
 *          RABBITMQ_DEFAULT_USER: guest
 *          RABBITMQ_DEFAULT_PASS: guest
 *        command: >
 *          bash -c "rabbitmq-plugins enable rabbitmq_stomp &&
 *                   rabbitmq-server"
 */
