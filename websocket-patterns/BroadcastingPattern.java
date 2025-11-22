package com.example.websocket.broadcasting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Broadcasting Pattern
 * ===================
 * 
 * The Broadcasting Pattern sends messages to all connected clients or subscribers
 * of a specific topic. It's a publish-subscribe pattern where one sender reaches
 * multiple receivers simultaneously.
 * 
 * Key Concepts:
 * ------------
 * 1. Broadcast Types:
 *    - Global Broadcast: All connected clients
 *    - Topic Broadcast: All topic subscribers
 *    - Filtered Broadcast: Based on conditions
 *    - Scheduled Broadcast: Periodic messages
 * 
 * 2. Use Cases:
 *    - System announcements
 *    - Live updates
 *    - News feeds
 *    - Stock tickers
 *    - Sports scores
 *    - Weather updates
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
@EnableScheduling
public class BroadcastingPattern {
    public static void main(String[] args) {
        SpringApplication.run(BroadcastingPattern.class, args);
    }
}

@Configuration
@EnableWebSocketMessageBroker
class BroadcastConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-broadcast")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

/**
 * Broadcasting Service
 */
@Service
class BroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public BroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast to all subscribers of a topic
     */
    public void broadcastToTopic(String topic, Object message) {
        messagingTemplate.convertAndSend("/topic/" + topic, message);
    }

    /**
     * Broadcast system announcement
     */
    public void broadcastAnnouncement(String message) {
        BroadcastMessage announcement = new BroadcastMessage(
            "SYSTEM",
            message,
            "ANNOUNCEMENT",
            LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/announcements", announcement);
    }

    /**
     * Scheduled broadcast - Server time every 10 seconds
     */
    @Scheduled(fixedRate = 10000)
    public void broadcastServerTime() {
        Map<String, Object> timeUpdate = new HashMap<>();
        timeUpdate.put("serverTime", LocalDateTime.now().toString());
        timeUpdate.put("type", "TIME_UPDATE");
        
        messagingTemplate.convertAndSend("/topic/server-time", timeUpdate);
    }

    /**
     * Broadcast to multiple topics
     */
    public void broadcastToMultipleTopics(List<String> topics, Object message) {
        topics.forEach(topic -> broadcastToTopic(topic, message));
    }
}

@Controller
class BroadcastController {

    private final BroadcastService broadcastService;

    public BroadcastController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    /**
     * Receive message and broadcast to all
     */
    @MessageMapping("/broadcast")
    @SendTo("/topic/broadcast")
    public BroadcastMessage broadcast(BroadcastMessage message) {
        message.setTimestamp(LocalDateTime.now().toString());
        return message;
    }

    /**
     * Broadcast news update
     */
    @MessageMapping("/news")
    @SendTo("/topic/news")
    public NewsUpdate broadcastNews(NewsUpdate news) {
        news.setPublishedAt(LocalDateTime.now().toString());
        return news;
    }
}

class BroadcastMessage {
    private String sender;
    private String content;
    private String type;
    private String timestamp;

    public BroadcastMessage() {}

    public BroadcastMessage(String sender, String content, String type, String timestamp) {
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

class NewsUpdate {
    private String headline;
    private String content;
    private String category;
    private String publishedAt;

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
}

/*
 * Client Example:
 * stompClient.subscribe('/topic/broadcast', function(message) {
 *     console.log('Broadcast:', JSON.parse(message.body));
 * });
 */
