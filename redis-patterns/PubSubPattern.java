package com.example.redis.pubsub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Redis Pub/Sub Pattern
 * 
 * Demonstrates Redis Publish/Subscribe messaging pattern.
 * Redis Pub/Sub provides:
 * - Real-time message broadcasting
 * - Channel-based messaging
 * - Pattern-based subscriptions
 * - Asynchronous message delivery
 * - Multiple subscribers support
 * - Fire-and-forget messaging
 * 
 * Use cases:
 * - Real-time notifications
 * - Chat applications
 * - Event broadcasting
 * - Cache invalidation
 * - Distributed logging
 * - Live updates
 */

@Configuration
class RedisPubSubConfig {
    
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter orderListener,
            MessageListenerAdapter notificationListener) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Subscribe to specific channels
        container.addMessageListener(orderListener, new ChannelTopic("orders"));
        container.addMessageListener(notificationListener, new ChannelTopic("notifications"));
        
        // Subscribe to pattern (all channels matching pattern)
        // container.addMessageListener(listener, new PatternTopic("events.*"));
        
        return container;
    }
    
    @Bean
    public MessageListenerAdapter orderListener(OrderSubscriber orderSubscriber) {
        return new MessageListenerAdapter(orderSubscriber, "onMessage");
    }
    
    @Bean
    public MessageListenerAdapter notificationListener(NotificationSubscriber notificationSubscriber) {
        return new MessageListenerAdapter(notificationSubscriber, "onMessage");
    }
}

record OrderMessage(String orderId, String customerId, double amount, String status, LocalDateTime timestamp) {}

record NotificationMessage(String userId, String title, String content, String type, LocalDateTime timestamp) {}

@Service
class RedisPublisher {
    
    private final StringRedisTemplate stringRedisTemplate;
    
    public RedisPublisher(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    public void publishOrder(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }
    
    public void publishNotification(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }
    
    public void publishToPattern(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }
}

@Service
class OrderSubscriber implements MessageListener {
    
    private final List<String> receivedMessages = new CopyOnWriteArrayList<>();
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());
        
        System.out.println("Order Channel: " + channel);
        System.out.println("Order Message: " + body);
        System.out.println("Order Received at: " + LocalDateTime.now());
        
        receivedMessages.add(body);
        
        // Process order message
        processOrder(body);
    }
    
    private void processOrder(String message) {
        // Business logic for processing orders
        System.out.println("Processing order: " + message);
    }
    
    public List<String> getReceivedMessages() {
        return List.copyOf(receivedMessages);
    }
    
    public void clearMessages() {
        receivedMessages.clear();
    }
}

@Service
class NotificationSubscriber implements MessageListener {
    
    private final List<String> receivedNotifications = new CopyOnWriteArrayList<>();
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());
        
        System.out.println("Notification Channel: " + channel);
        System.out.println("Notification Message: " + body);
        System.out.println("Notification Received at: " + LocalDateTime.now());
        
        receivedNotifications.add(body);
        
        // Process notification
        sendNotification(body);
    }
    
    private void sendNotification(String message) {
        // Business logic for sending notifications (email, push, SMS, etc.)
        System.out.println("Sending notification: " + message);
    }
    
    public List<String> getReceivedNotifications() {
        return List.copyOf(receivedNotifications);
    }
    
    public void clearNotifications() {
        receivedNotifications.clear();
    }
}

@RestController
@RequestMapping("/api/redis/pubsub")
class RedisPubSubController {
    
    private final RedisPublisher redisPublisher;
    private final OrderSubscriber orderSubscriber;
    private final NotificationSubscriber notificationSubscriber;
    
    public RedisPubSubController(RedisPublisher redisPublisher,
                                 OrderSubscriber orderSubscriber,
                                 NotificationSubscriber notificationSubscriber) {
        this.redisPublisher = redisPublisher;
        this.orderSubscriber = orderSubscriber;
        this.notificationSubscriber = notificationSubscriber;
    }
    
    @PostMapping("/orders/publish")
    public String publishOrder(@RequestBody String message) {
        redisPublisher.publishOrder("orders", message);
        return "Order message published";
    }
    
    @PostMapping("/notifications/publish")
    public String publishNotification(@RequestBody String message) {
        redisPublisher.publishNotification("notifications", message);
        return "Notification message published";
    }
    
    @PostMapping("/publish")
    public String publishToChannel(@RequestParam String channel, @RequestBody String message) {
        redisPublisher.publishToPattern(channel, message);
        return "Message published to channel: " + channel;
    }
    
    @GetMapping("/orders/received")
    public List<String> getReceivedOrders() {
        return orderSubscriber.getReceivedMessages();
    }
    
    @DeleteMapping("/orders/received")
    public String clearReceivedOrders() {
        orderSubscriber.clearMessages();
        return "Received orders cleared";
    }
    
    @GetMapping("/notifications/received")
    public List<String> getReceivedNotifications() {
        return notificationSubscriber.getReceivedNotifications();
    }
    
    @DeleteMapping("/notifications/received")
    public String clearReceivedNotifications() {
        notificationSubscriber.clearNotifications();
        return "Received notifications cleared";
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return """
                Redis Pub/Sub Pattern
                ====================
                Features:
                - Real-time message broadcasting
                - Channel-based messaging
                - Pattern subscriptions (e.g., events.*)
                - Multiple subscribers per channel
                - Fire-and-forget delivery
                - Asynchronous processing
                
                Configuration:
                - RedisMessageListenerContainer for subscribers
                - MessageListenerAdapter for message handling
                - ChannelTopic for specific channels
                - PatternTopic for pattern matching
                
                Use Cases:
                - Real-time notifications
                - Chat applications
                - Event broadcasting
                - Cache invalidation across nodes
                - Live updates and feeds
                - Distributed logging
                
                Channels:
                - orders: Order-related messages
                - notifications: Notification messages
                - Custom channels supported
                
                Note: Pub/Sub messages are not persisted.
                      If no subscribers exist, messages are lost.
                """;
    }
}
