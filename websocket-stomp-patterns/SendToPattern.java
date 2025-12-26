package com.example.websocket.stomp.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * SendTo Pattern
 * 
 * Demonstrates the @SendTo annotation for broadcasting messages to specific
 * destinations after processing. Messages are sent through the message broker
 * to all subscribed clients.
 * 
 * Key Features:
 * - Broadcast to specific destination
 * - Send through message broker
 * - Multiple subscribers receive message
 * - Topic-based broadcasting
 * - Queue-based point-to-point
 * - Return value routing
 */
@SpringBootApplication
public class SendToPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SendToPattern.class, args);
    }

    @Override
    public void run(String... args) {
        demonstrateSendTo();
    }

    private void demonstrateSendTo() {
        System.out.println("=== SendTo Pattern ===\n");
        System.out.println("1. @SendTo Annotation:");
        System.out.println("   - Routes return value to specified destination");
        System.out.println("   - Broadcasts to all subscribers");
        System.out.println("   - Uses message broker for delivery");
        System.out.println("   - Supports topic and queue destinations");

        System.out.println("\n2. Message Flow:");
        System.out.println("   Client A → /app/chat → @MessageMapping");
        System.out.println("   Handler processes → @SendTo(\"/topic/messages\")");
        System.out.println("   Broker broadcasts → All subscribed clients");

        System.out.println("\n3. Destination Types:");
        System.out.println("   /topic/* - Broadcast to all subscribers (pub-sub)");
        System.out.println("   /queue/* - Point-to-point messaging");
        System.out.println("   Custom destinations supported");

        System.out.println("\n4. Use Cases:");
        System.out.println("   - Chat room broadcasting");
        System.out.println("   - Real-time notifications");
        System.out.println("   - Live data updates");
        System.out.println("   - Collaborative editing");
        System.out.println("   - Game state synchronization");

        System.out.println("\n5. Multiple Destinations:");
        System.out.println("   @SendTo({\"/topic/messages\", \"/topic/archive\"})");
        System.out.println("   Sends to multiple destinations simultaneously");

        System.out.println("\n6. Comparison:");
        System.out.println("   @SendTo: Broadcast to all subscribers");
        System.out.println("   @SendToUser: Send to specific user only");
        System.out.println("   SimpMessagingTemplate: Programmatic sending");

        System.out.println("\nWebSocket server configured at: ws://localhost:8080/ws");
    }

    /**
     * WebSocket Configuration
     */
    @Configuration
    @EnableWebSocketMessageBroker
    static class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic", "/queue");
            config.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/ws").withSockJS();
        }
    }

    /**
     * SendTo Controller
     */
    @Controller
    static class BroadcastController {

        /**
         * Broadcast chat messages to all subscribers
         */
        @MessageMapping("/chat/send")
        @SendTo("/topic/messages")
        public ChatMessage broadcastMessage(ChatMessage message) {
            System.out.println("Broadcasting message from " + message.getUser());
            message.setTimestamp(System.currentTimeMillis());
            return message;
        }

        /**
         * Broadcast notifications to all users
         */
        @MessageMapping("/notify")
        @SendTo("/topic/notifications")
        public Notification broadcastNotification(Notification notification) {
            System.out.println("Broadcasting notification: " + notification.getMessage());
            return notification;
        }

        /**
         * Broadcast to multiple destinations
         */
        @MessageMapping("/important")
        @SendTo({"/topic/notifications", "/topic/archive"})
        public ImportantMessage broadcastImportant(ImportantMessage message) {
            System.out.println("Broadcasting important message to multiple destinations");
            return message;
        }

        /**
         * Broadcast live updates
         */
        @MessageMapping("/updates/live")
        @SendTo("/topic/live-updates")
        public LiveUpdate broadcastLiveUpdate(LiveUpdate update) {
            System.out.println("Broadcasting live update: " + update.getType());
            return update;
        }

        /**
         * Broadcast game state
         */
        @MessageMapping("/game/state")
        @SendTo("/topic/game-state")
        public GameState broadcastGameState(GameState state) {
            System.out.println("Broadcasting game state for game: " + state.getGameId());
            return state;
        }

        /**
         * Broadcast to queue (point-to-point)
         */
        @MessageMapping("/task/assign")
        @SendTo("/queue/tasks")
        public Task assignTask(Task task) {
            System.out.println("Assigning task to queue: " + task.getTaskId());
            return task;
        }

        /**
         * Broadcast event with metadata
         */
        @MessageMapping("/events/publish")
        @SendTo("/topic/events")
        public Event publishEvent(Event event) {
            System.out.println("Publishing event: " + event.getEventType());
            event.setPublishedAt(System.currentTimeMillis());
            return event;
        }
    }

    /**
     * Chat Message DTO
     */
    static class ChatMessage {
        private String user;
        private String content;
        private long timestamp;

        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Notification DTO
     */
    static class Notification {
        private String type;
        private String message;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * Important Message DTO
     */
    static class ImportantMessage {
        private String title;
        private String content;
        private String priority;

        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getPriority() { return priority; }
    }

    /**
     * Live Update DTO
     */
    static class LiveUpdate {
        private String type;
        private Object data;

        public String getType() { return type; }
        public Object getData() { return data; }
    }

    /**
     * Game State DTO
     */
    static class GameState {
        private String gameId;
        private String state;
        private int score;

        public String getGameId() { return gameId; }
        public String getState() { return state; }
        public int getScore() { return score; }
    }

    /**
     * Task DTO
     */
    static class Task {
        private String taskId;
        private String description;
        private String priority;

        public String getTaskId() { return taskId; }
        public String getDescription() { return description; }
        public String getPriority() { return priority; }
    }

    /**
     * Event DTO
     */
    static class Event {
        private String eventType;
        private String payload;
        private long publishedAt;

        public String getEventType() { return eventType; }
        public String getPayload() { return payload; }
        public long getPublishedAt() { return publishedAt; }
        public void setPublishedAt(long publishedAt) { this.publishedAt = publishedAt; }
    }
}
