package com.example.websocket.stomp.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Destination Variable Pattern
 * 
 * Demonstrates @DestinationVariable for extracting path variables from message destinations.
 * Similar to @PathVariable in REST, allows dynamic routing based on destination parts.
 * 
 * Key Features:
 * - Extract variables from message destination
 * - Dynamic routing based on destination
 * - Multiple variable extraction
 * - Type conversion support
 * - Room/channel based messaging
 */
@SpringBootApplication
public class DestinationVariablePattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DestinationVariablePattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Destination Variable Pattern ===\n");
        System.out.println("@DestinationVariable extracts path segments from STOMP destinations:");
        System.out.println("  /app/chat/{roomId} → extracts roomId");
        System.out.println("  /app/user/{userId}/message → extracts userId");
        System.out.println("\nUse cases: Chat rooms, user-specific channels, game sessions");
    }

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

    @Controller
    static class ChatController {
        @MessageMapping("/chat/{roomId}/send")
        @SendTo("/topic/chat/{roomId}")
        public ChatMessage sendToRoom(@DestinationVariable String roomId, ChatMessage message) {
            System.out.println("Message to room " + roomId + ": " + message.getContent());
            message.setRoom(roomId);
            return message;
        }

        @MessageMapping("/game/{gameId}/player/{playerId}/move")
        @SendTo("/topic/game/{gameId}")
        public GameMove processMove(
                @DestinationVariable String gameId,
                @DestinationVariable Long playerId,
                GameMove move) {
            System.out.println("Game: " + gameId + ", Player: " + playerId);
            return move;
        }
    }

    static class ChatMessage {
        private String content;
        private String room;
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getRoom() { return room; }
        public void setRoom(String room) { this.room = room; }
    }

    static class GameMove {
        private String move;
        public String getMove() { return move; }
        public void setMove(String move) { this.move = move; }
    }
}
