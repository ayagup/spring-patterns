package com.example.websocket.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Subscription Pattern
 * ===================
 * 
 * The Subscription Pattern manages client subscriptions to message destinations.
 * Clients subscribe to topics or queues to receive relevant messages, enabling
 * targeted message delivery and efficient resource usage.
 * 
 * Key Concepts:
 * ------------
 * 1. Subscription Types:
 *    - Topic Subscription: /topic/* - Broadcast to all subscribers
 *    - Queue Subscription: /queue/* - Point-to-point, load balanced
 *    - User Subscription: /user/* - User-specific messages
 *    - Dynamic Subscription: Subscribe/unsubscribe at runtime
 * 
 * 2. Subscription Management:
 *    - Track active subscriptions
 *    - Monitor subscription lifecycle
 *    - Handle subscription errors
 *    - Clean up orphaned subscriptions
 * 
 * 3. Subscription Patterns:
 *    - Wildcard subscriptions
 *    - Hierarchical destinations
 *    - Filtered subscriptions
 *    - Priority subscriptions
 * 
 * Use Cases:
 * ---------
 * - Chat rooms (topic subscriptions)
 * - User notifications (user subscriptions)
 * - Live data feeds (filtered subscriptions)
 * - Event broadcasting
 * - Real-time dashboards
 * - Collaborative applications
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class SubscriptionPattern {

    public static void main(String[] args) {
        SpringApplication.run(SubscriptionPattern.class, args);
    }
}

@Configuration
@EnableWebSocketMessageBroker
class SubscriptionConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-subscription")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

/**
 * Subscription Manager Service
 */
@Service
class SubscriptionManager {

    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(SubscriptionManager.class);

    // Map<SessionId, Map<SubscriptionId, SubscriptionInfo>>
    private final Map<String, Map<String, SubscriptionInfo>> sessionSubscriptions = new ConcurrentHashMap<>();
    
    // Map<Destination, Set<SubscriptionInfo>>
    private final Map<String, Set<SubscriptionInfo>> destinationSubscriptions = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    public SubscriptionManager(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle new subscription
     */
    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();
        String destination = accessor.getDestination();

        logger.info("New subscription: Session={}, Subscription={}, Destination={}", 
            sessionId, subscriptionId, destination);

        SubscriptionInfo info = new SubscriptionInfo(
            subscriptionId,
            sessionId,
            destination,
            LocalDateTime.now().toString()
        );

        // Track by session
        sessionSubscriptions.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
                           .put(subscriptionId, info);

        // Track by destination
        destinationSubscriptions.computeIfAbsent(destination, k -> ConcurrentHashMap.newKeySet())
                               .add(info);

        // Notify about subscription count
        notifySubscriptionCount(destination);
    }

    /**
     * Handle unsubscription
     */
    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();

        logger.info("Unsubscribe: Session={}, Subscription={}", sessionId, subscriptionId);

        Map<String, SubscriptionInfo> subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            SubscriptionInfo info = subscriptions.remove(subscriptionId);
            
            if (info != null) {
                // Remove from destination tracking
                Set<SubscriptionInfo> destSubs = destinationSubscriptions.get(info.getDestination());
                if (destSubs != null) {
                    destSubs.remove(info);
                    notifySubscriptionCount(info.getDestination());
                }
            }
        }
    }

    /**
     * Get all subscriptions for a session
     */
    public Map<String, SubscriptionInfo> getSessionSubscriptions(String sessionId) {
        return new HashMap<>(sessionSubscriptions.getOrDefault(sessionId, Collections.emptyMap()));
    }

    /**
     * Get subscriber count for destination
     */
    public int getSubscriberCount(String destination) {
        Set<SubscriptionInfo> subs = destinationSubscriptions.get(destination);
        return subs != null ? subs.size() : 0;
    }

    /**
     * Get all destinations
     */
    public Set<String> getAllDestinations() {
        return new HashSet<>(destinationSubscriptions.keySet());
    }

    /**
     * Notify about subscription count changes
     */
    private void notifySubscriptionCount(String destination) {
        int count = getSubscriberCount(destination);
        
        Map<String, Object> update = new HashMap<>();
        update.put("destination", destination);
        update.put("subscriberCount", count);
        update.put("timestamp", LocalDateTime.now().toString());
        
        messagingTemplate.convertAndSend("/topic/subscription-updates", update);
    }

    /**
     * Get subscription statistics
     */
    public SubscriptionStatistics getStatistics() {
        int totalSubscriptions = sessionSubscriptions.values().stream()
                .mapToInt(Map::size)
                .sum();
        
        return new SubscriptionStatistics(
            totalSubscriptions,
            destinationSubscriptions.size(),
            sessionSubscriptions.size()
        );
    }
}

/**
 * Subscription Controller
 */
@Controller
class SubscriptionController {

    private final SubscriptionManager subscriptionManager;

    public SubscriptionController(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    /**
     * Subscribe to dynamic topic
     */
    @MessageMapping("/subscribe.topic/{topic}")
    @SendTo("/topic/{topic}")
    public Map<String, Object> subscribeToTopic(@DestinationVariable String topic) {
        int count = subscriptionManager.getSubscriberCount("/topic/" + topic);
        
        Map<String, Object> response = new HashMap<>();
        response.put("topic", topic);
        response.put("subscriberCount", count);
        response.put("message", "Subscribed successfully");
        return response;
    }

    /**
     * Get subscription info
     */
    @MessageMapping("/subscription.info")
    @SendTo("/topic/subscription-info")
    public Map<String, SubscriptionInfo> getSubscriptionInfo(SimpMessageHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        return subscriptionManager.getSessionSubscriptions(sessionId);
    }

    /**
     * Get subscription statistics
     */
    @MessageMapping("/subscription.stats")
    @SendTo("/topic/subscription-stats")
    public SubscriptionStatistics getStatistics() {
        return subscriptionManager.getStatistics();
    }

    /**
     * Publish to subscribed topics
     */
    @MessageMapping("/publish.to/{destination}")
    @SendTo("/topic/{destination}")
    public Message publishToDestination(
            @DestinationVariable String destination,
            Message message) {
        message.setTimestamp(LocalDateTime.now().toString());
        message.setDestination("/topic/" + destination);
        return message;
    }
}

/**
 * Subscription Information
 */
class SubscriptionInfo {
    private final String subscriptionId;
    private final String sessionId;
    private final String destination;
    private final String subscribedAt;

    public SubscriptionInfo(String subscriptionId, String sessionId, 
                           String destination, String subscribedAt) {
        this.subscriptionId = subscriptionId;
        this.sessionId = sessionId;
        this.destination = destination;
        this.subscribedAt = subscribedAt;
    }

    public String getSubscriptionId() { return subscriptionId; }
    public String getSessionId() { return sessionId; }
    public String getDestination() { return destination; }
    public String getSubscribedAt() { return subscribedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionInfo that = (SubscriptionInfo) o;
        return subscriptionId.equals(that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId);
    }
}

/**
 * Subscription Statistics
 */
class SubscriptionStatistics {
    private int totalSubscriptions;
    private int uniqueDestinations;
    private int activeSessions;
    private String timestamp;

    public SubscriptionStatistics(int totalSubscriptions, int uniqueDestinations, int activeSessions) {
        this.totalSubscriptions = totalSubscriptions;
        this.uniqueDestinations = uniqueDestinations;
        this.activeSessions = activeSessions;
        this.timestamp = LocalDateTime.now().toString();
    }

    public int getTotalSubscriptions() { return totalSubscriptions; }
    public int getUniqueDestinations() { return uniqueDestinations; }
    public int getActiveSessions() { return activeSessions; }
    public String getTimestamp() { return timestamp; }
}

/**
 * Message Model
 */
class Message {
    private String content;
    private String sender;
    private String destination;
    private String timestamp;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

/*
 * Client-Side JavaScript Example:
 * ================================
 * 
 * var stompClient = Stomp.over(new SockJS('/ws-subscription'));
 * var subscriptions = {};
 * 
 * stompClient.connect({}, function(frame) {
 *     // Subscribe to a topic
 *     subscriptions['chat'] = stompClient.subscribe('/topic/chat', function(message) {
 *         console.log('Chat message:', JSON.parse(message.body));
 *     });
 *     
 *     // Subscribe to subscription updates
 *     subscriptions['updates'] = stompClient.subscribe('/topic/subscription-updates', function(message) {
 *         var update = JSON.parse(message.body);
 *         console.log('Subscribers for', update.destination + ':', update.subscriberCount);
 *     });
 *     
 *     // Subscribe to user-specific queue
 *     subscriptions['private'] = stompClient.subscribe('/user/queue/private', function(message) {
 *         console.log('Private message:', JSON.parse(message.body));
 *     });
 *     
 *     // Dynamic topic subscription
 *     function subscribeToTopic(topic) {
 *         var dest = '/topic/' + topic;
 *         if (!subscriptions[topic]) {
 *             subscriptions[topic] = stompClient.subscribe(dest, function(message) {
 *                 console.log('Message from ' + topic + ':', JSON.parse(message.body));
 *             });
 *             
 *             // Notify server
 *             stompClient.send("/app/subscribe.topic/" + topic, {}, "{}");
 *         }
 *     }
 *     
 *     // Unsubscribe from topic
 *     function unsubscribeFromTopic(topic) {
 *         if (subscriptions[topic]) {
 *             subscriptions[topic].unsubscribe();
 *             delete subscriptions[topic];
 *         }
 *     }
 *     
 *     // Get subscription info
 *     stompClient.send("/app/subscription.info", {}, "{}");
 *     stompClient.subscribe('/topic/subscription-info', function(message) {
 *         var info = JSON.parse(message.body);
 *         console.log('My subscriptions:', info);
 *     });
 * });
 * 
 * // Publish to subscribed topic
 * function publish(topic, content) {
 *     stompClient.send("/app/publish.to/" + topic, {}, JSON.stringify({
 *         content: content,
 *         sender: 'User123'
 *     }));
 * }
 */
