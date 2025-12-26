package com.example.websocket.stomp.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

/**
 * Principal Pattern
 * 
 * Demonstrates Principal parameter for accessing authenticated user information.
 * Principal represents the authenticated user in WebSocket connections.
 * 
 * Key Features:
 * - Access authenticated user
 * - User-specific message handling
 * - Security context integration
 * - Session-based user tracking
 * - Audit trail support
 */
@SpringBootApplication
public class PrincipalPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PrincipalPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Principal Pattern ===\n");
        System.out.println("Principal parameter provides authenticated user information");
        System.out.println("Automatically injected by Spring Security");
        System.out.println("Use principal.getName() to get username");
    }

    @Configuration
    @EnableWebSocketMessageBroker
    static class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic", "/queue");
            config.setApplicationDestinationPrefixes("/app");
            config.setUserDestinationPrefix("/user");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/ws").withSockJS();
        }
    }

    @Controller
    static class UserAwareController {
        @MessageMapping("/user/message")
        public void handleUserMessage(String message, Principal principal) {
            String username = principal != null ? principal.getName() : "anonymous";
            System.out.println("Message from " + username + ": " + message);
        }

        @MessageMapping("/user/profile/update")
        public void updateProfile(ProfileUpdate update, Principal principal) {
            if (principal != null) {
                System.out.println("Updating profile for: " + principal.getName());
                System.out.println("New value: " + update.getValue());
            }
        }

        @MessageMapping("/user/action")
        public void logUserAction(UserAction action, Principal principal) {
            String user = principal != null ? principal.getName() : "anonymous";
            System.out.println("User: " + user + " performed: " + action.getActionType());
        }
    }

    static class ProfileUpdate {
        private String field;
        private String value;
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    static class UserAction {
        private String actionType;
        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }
    }
}
