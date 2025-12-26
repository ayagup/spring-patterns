package com.example.websocket.stomp.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Map;

/**
 * Session Attributes Pattern
 * 
 * Demonstrates accessing and managing WebSocket session attributes.
 * Session attributes store user-specific data throughout the WebSocket connection.
 * 
 * Key Features:
 * - Store session-specific data
 * - Access session attributes
 * - User state management
 * - Connection tracking
 * - Session cleanup
 */
@SpringBootApplication
public class SessionAttributesPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SessionAttributesPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Session Attributes Pattern ===\n");
        System.out.println("SimpMessageHeaderAccessor provides access to session attributes");
        System.out.println("Use getSessionAttributes() to store/retrieve session data");
        System.out.println("Useful for user state, preferences, connection metadata");
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
    static class SessionController {
        @MessageMapping("/session/init")
        public void initializeSession(UserInfo userInfo, SimpMessageHeaderAccessor headerAccessor) {
            Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
            if (sessionAttrs != null) {
                sessionAttrs.put("userId", userInfo.getUserId());
                sessionAttrs.put("username", userInfo.getUsername());
                sessionAttrs.put("connectedAt", System.currentTimeMillis());
                System.out.println("Session initialized for: " + userInfo.getUsername());
            }
        }

        @MessageMapping("/session/data")
        public void accessSessionData(String message, SimpMessageHeaderAccessor headerAccessor) {
            Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
            if (sessionAttrs != null) {
                String username = (String) sessionAttrs.get("username");
                Long connectedAt = (Long) sessionAttrs.get("connectedAt");
                System.out.println("User: " + username);
                System.out.println("Connected at: " + connectedAt);
                System.out.println("Message: " + message);
            }
        }

        @MessageMapping("/session/update")
        public void updateSessionAttribute(SessionUpdate update, SimpMessageHeaderAccessor headerAccessor) {
            Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
            if (sessionAttrs != null) {
                sessionAttrs.put(update.getKey(), update.getValue());
                System.out.println("Updated session: " + update.getKey() + " = " + update.getValue());
            }
        }
    }

    static class UserInfo {
        private Long userId;
        private String username;
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    static class SessionUpdate {
        private String key;
        private Object value;
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }
}
