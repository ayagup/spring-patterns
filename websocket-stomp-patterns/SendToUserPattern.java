package com.example.websocket.stomp.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

/**
 * SendToUser Pattern
 * 
 * Demonstrates the @SendToUser annotation for sending messages to specific users.
 * Messages are sent only to the user who sent the original message.
 * 
 * Key Features:
 * - Send message to specific user only
 * - User-specific queue routing
 * - Private messaging support
 * - Session-based user identification
 * - No broadcasting to other users
 * - Automatic user destination resolution
 */
@SpringBootApplication
public class SendToUserPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SendToUserPattern.class, args);
    }

    @Override
    public void run(String... args) {
        demonstrateSendToUser();
    }

    private void demonstrateSendToUser() {
        System.out.println("=== SendToUser Pattern ===\n");
        System.out.println("1. @SendToUser vs @SendTo:");
        System.out.println("   @SendTo: Broadcasts to ALL subscribers");
        System.out.println("   @SendToUser: Sends ONLY to message sender");

        System.out.println("\n2. User Destination:");
        System.out.println("   @SendToUser(\"/queue/reply\")");
        System.out.println("   Actual destination: /user/{username}/queue/reply");
        System.out.println("   Spring automatically adds user prefix");

        System.out.println("\n3. Message Flow:");
        System.out.println("   User 'john' → /app/private → @MessageMapping");
        System.out.println("   Handler processes → @SendToUser(\"/queue/reply\")");
        System.out.println("   → /user/john/queue/reply");
        System.out.println("   Only 'john' receives the response");

        System.out.println("\n4. Use Cases:");
        System.out.println("   - Private messages");
        System.out.println("   - User-specific notifications");
        System.out.println("   - Personal data responses");
        System.out.println("   - Error messages to sender");
        System.out.println("   - Confirmation messages");
        System.out.println("   - User profile updates");

        System.out.println("\n5. Configuration:");
        System.out.println("   - User destination prefix: /user");
        System.out.println("   - Principal from authentication");
        System.out.println("   - Session-based user tracking");

        System.out.println("\n6. Subscription:");
        System.out.println("   Client subscribes to: /user/queue/reply");
        System.out.println("   Spring converts to: /user/{session}/queue/reply");
        System.out.println("   Each user gets unique queue");

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
            config.setUserDestinationPrefix("/user");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/ws").withSockJS();
        }
    }

    /**
     * SendToUser Controller
     */
    @Controller
    static class PrivateMessageController {

        /**
         * Send private reply to message sender
         */
        @MessageMapping("/private/message")
        @SendToUser("/queue/reply")
        public PrivateReply sendPrivateReply(PrivateMessage message, Principal principal) {
            System.out.println("Private message from: " + 
                (principal != null ? principal.getName() : "anonymous"));
            System.out.println("Content: " + message.getContent());
            
            return new PrivateReply(
                "Received your private message: " + message.getContent(),
                System.currentTimeMillis()
            );
        }

        /**
         * Send user-specific notification
         */
        @MessageMapping("/user/notify")
        @SendToUser("/queue/notifications")
        public UserNotification sendUserNotification(String message, Principal principal) {
            String username = principal != null ? principal.getName() : "User";
            System.out.println("Sending notification to: " + username);
            
            return new UserNotification(
                "INFO",
                "Notification for " + username + ": " + message,
                username
            );
        }

        /**
         * Send profile update confirmation
         */
        @MessageMapping("/user/profile/update")
        @SendToUser("/queue/confirmations")
        public ProfileUpdateConfirmation confirmProfileUpdate(
                ProfileUpdate update, Principal principal) {
            
            String username = principal != null ? principal.getName() : "User";
            System.out.println("Profile updated for: " + username);
            
            return new ProfileUpdateConfirmation(
                "SUCCESS",
                "Profile updated successfully",
                username,
                update.getFieldName()
            );
        }

        /**
         * Send user-specific error
         */
        @MessageMapping("/user/validate")
        @SendToUser("/queue/errors")
        public ValidationResult validateUserData(UserData data, Principal principal) {
            String username = principal != null ? principal.getName() : "User";
            System.out.println("Validating data for: " + username);
            
            if (data.getValue() == null || data.getValue().isEmpty()) {
                return new ValidationResult(false, "Value cannot be empty");
            }
            
            return new ValidationResult(true, "Validation passed");
        }

        /**
         * Send personalized response
         */
        @MessageMapping("/user/request")
        @SendToUser("/queue/responses")
        public PersonalizedResponse sendPersonalizedResponse(
                UserRequest request, Principal principal) {
            
            String username = principal != null ? principal.getName() : "Guest";
            System.out.println("Processing request for: " + username);
            
            return new PersonalizedResponse(
                "Hello " + username + "!",
                "Your request has been processed",
                request.getRequestType()
            );
        }

        /**
         * Send user-specific data
         */
        @MessageMapping("/user/data/fetch")
        @SendToUser("/queue/data")
        public UserSpecificData fetchUserData(String dataType, Principal principal) {
            String username = principal != null ? principal.getName() : "User";
            System.out.println("Fetching " + dataType + " for: " + username);
            
            return new UserSpecificData(
                username,
                dataType,
                "Sample data for " + username
            );
        }
    }

    /**
     * Private Message DTO
     */
    static class PrivateMessage {
        private String content;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    /**
     * Private Reply DTO
     */
    static class PrivateReply {
        private String message;
        private long timestamp;

        public PrivateReply(String message, long timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * User Notification DTO
     */
    static class UserNotification {
        private String type;
        private String message;
        private String username;

        public UserNotification(String type, String message, String username) {
            this.type = type;
            this.message = message;
            this.username = username;
        }

        public String getType() { return type; }
        public String getMessage() { return message; }
        public String getUsername() { return username; }
    }

    /**
     * Profile Update DTO
     */
    static class ProfileUpdate {
        private String fieldName;
        private String value;

        public String getFieldName() { return fieldName; }
        public String getValue() { return value; }
    }

    /**
     * Profile Update Confirmation DTO
     */
    static class ProfileUpdateConfirmation {
        private String status;
        private String message;
        private String username;
        private String field;

        public ProfileUpdateConfirmation(String status, String message, 
                String username, String field) {
            this.status = status;
            this.message = message;
            this.username = username;
            this.field = field;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getUsername() { return username; }
        public String getField() { return field; }
    }

    /**
     * User Data DTO
     */
    static class UserData {
        private String value;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    /**
     * Validation Result DTO
     */
    static class ValidationResult {
        private boolean valid;
        private String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }

    /**
     * User Request DTO
     */
    static class UserRequest {
        private String requestType;

        public String getRequestType() { return requestType; }
        public void setRequestType(String requestType) { this.requestType = requestType; }
    }

    /**
     * Personalized Response DTO
     */
    static class PersonalizedResponse {
        private String greeting;
        private String message;
        private String requestType;

        public PersonalizedResponse(String greeting, String message, String requestType) {
            this.greeting = greeting;
            this.message = message;
            this.requestType = requestType;
        }

        public String getGreeting() { return greeting; }
        public String getMessage() { return message; }
        public String getRequestType() { return requestType; }
    }

    /**
     * User Specific Data DTO
     */
    static class UserSpecificData {
        private String username;
        private String dataType;
        private String data;

        public UserSpecificData(String username, String dataType, String data) {
            this.username = username;
            this.dataType = dataType;
            this.data = data;
        }

        public String getUsername() { return username; }
        public String getDataType() { return dataType; }
        public String getData() { return data; }
    }
}
