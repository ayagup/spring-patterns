package com.example.websocket.stomp.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Message Mapping Pattern
 * 
 * Demonstrates the @MessageMapping annotation for handling WebSocket messages.
 * Maps messages from clients to specific handler methods based on destination.
 * 
 * Key Features:
 * - Map messages to handler methods
 * - Flexible routing based on destination
 * - Support for wildcards in mappings
 * - Path variable extraction
 * - Hierarchical destination mapping
 * - Message conversion
 */
@SpringBootApplication
public class MessageMappingPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MessageMappingPattern.class, args);
    }

    @Override
    public void run(String... args) {
        demonstrateMessageMapping();
    }

    private void demonstrateMessageMapping() {
        System.out.println("=== Message Mapping Pattern ===\n");
        System.out.println("1. Basic Message Mapping:");
        System.out.println("   @MessageMapping(\"/hello\")");
        System.out.println("   - Maps client messages sent to '/app/hello'");
        System.out.println("   - Handler method processes the message");
        System.out.println("   - Returns response to specified destination");

        System.out.println("\n2. Hierarchical Mapping:");
        System.out.println("   @MessageMapping(\"/chat/{roomId}/send\")");
        System.out.println("   - Maps messages to '/app/chat/123/send'");
        System.out.println("   - Extracts 'roomId' as path variable");
        System.out.println("   - Supports nested destinations");

        System.out.println("\n3. Wildcard Mapping:");
        System.out.println("   @MessageMapping(\"/topic/**\")");
        System.out.println("   - Matches any destination under '/app/topic/'");
        System.out.println("   - Useful for catch-all handlers");

        System.out.println("\n4. Message Flow:");
        System.out.println("   Client → /app/hello → @MessageMapping → Handler");
        System.out.println("   Handler processes → @SendTo → /topic/greetings");
        System.out.println("   Broker broadcasts → Subscribed clients");

        System.out.println("\n5. Configuration:");
        System.out.println("   - Application destination prefix: /app");
        System.out.println("   - Message broker prefix: /topic, /queue");
        System.out.println("   - STOMP endpoint: /ws");

        System.out.println("\n6. Example Messages:");
        System.out.println("   Input: ChatMessage(user='John', content='Hello')");
        System.out.println("   Destination: /app/chat/send");
        System.out.println("   Output: Broadcasted to /topic/messages");

        System.out.println("\n7. Benefits:");
        System.out.println("   - Clean message routing");
        System.out.println("   - Type-safe message handling");
        System.out.println("   - Automatic message conversion");
        System.out.println("   - Flexible destination patterns");
        System.out.println("   - Integration with Spring MVC patterns");

        System.out.println("\nWebSocket server configured at: ws://localhost:8080/ws");
        System.out.println("Application prefix: /app");
        System.out.println("Message broker: /topic, /queue");
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
     * Message Controller with various mapping patterns
     */
    @Controller
    static class MessageController {

        /**
         * Basic message mapping
         */
        @MessageMapping("/hello")
        @SendTo("/topic/greetings")
        public String processGreeting(String message) {
            System.out.println("Received on /app/hello: " + message);
            return "Hello, " + message + "!";
        }

        /**
         * Chat message mapping
         */
        @MessageMapping("/chat/send")
        @SendTo("/topic/messages")
        public ChatMessage processChatMessage(ChatMessage message) {
            System.out.println("Chat message from " + message.getUser() + ": " + message.getContent());
            return message;
        }

        /**
         * Private message mapping
         */
        @MessageMapping("/private")
        public void processPrivateMessage(String message) {
            System.out.println("Private message: " + message);
            // Handle private message logic
        }

        /**
         * Notification mapping
         */
        @MessageMapping("/notify")
        @SendTo("/topic/notifications")
        public Notification processNotification(Notification notification) {
            System.out.println("Notification: " + notification.getMessage());
            return notification;
        }
    }

    /**
     * Chat Message DTO
     */
    static class ChatMessage {
        private String user;
        private String content;
        private long timestamp;

        public ChatMessage() {
            this.timestamp = System.currentTimeMillis();
        }

        public ChatMessage(String user, String content) {
            this.user = user;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }

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

        public Notification() {}

        public Notification(String type, String message) {
            this.type = type;
            this.message = message;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
