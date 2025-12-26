package com.example.websocket.stomp.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Message Exception Handler Pattern
 * 
 * Demonstrates the @MessageExceptionHandler annotation for handling exceptions
 * that occur during WebSocket message processing.
 * 
 * Key Features:
 * - Handle exceptions in WebSocket controllers
 * - Send error messages to clients
 * - Different handling strategies
 * - Client-specific vs broadcast errors
 * - Exception type-specific handlers
 * - Graceful error recovery
 */
@SpringBootApplication
public class MessageExceptionHandlerPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MessageExceptionHandlerPattern.class, args);
    }

    @Override
    public void run(String... args) {
        demonstrateMessageExceptionHandler();
    }

    private void demonstrateMessageExceptionHandler() {
        System.out.println("=== Message Exception Handler Pattern ===\n");
        System.out.println("1. Exception Handling in WebSocket:");
        System.out.println("   - @MessageExceptionHandler catches exceptions");
        System.out.println("   - Can send error to specific user or broadcast");
        System.out.println("   - Prevents connection termination");
        System.out.println("   - Provides user-friendly error messages");

        System.out.println("\n2. Handler Strategies:");
        System.out.println("   @SendToUser: Send error to message sender only");
        System.out.println("   @SendTo: Broadcast error to all subscribers");
        System.out.println("   No annotation: Handle internally without response");

        System.out.println("\n3. Exception Types:");
        System.out.println("   - ValidationException: Invalid message format");
        System.out.println("   - BusinessException: Business logic errors");
        System.out.println("   - RuntimeException: Unexpected errors");
        System.out.println("   - Custom exceptions: Domain-specific errors");

        System.out.println("\n4. Error Flow:");
        System.out.println("   Client sends message → Handler throws exception");
        System.out.println("   → @MessageExceptionHandler catches");
        System.out.println("   → Error message sent to client");
        System.out.println("   → Connection remains active");

        System.out.println("\n5. Best Practices:");
        System.out.println("   - Always provide @MessageExceptionHandler");
        System.out.println("   - Send meaningful error messages");
        System.out.println("   - Log exceptions for debugging");
        System.out.println("   - Don't expose sensitive information");
        System.out.println("   - Handle different exception types separately");

        System.out.println("\n6. Error Response Format:");
        System.out.println("   { \"error\": \"ERROR_CODE\",");
        System.out.println("     \"message\": \"User-friendly message\",");
        System.out.println("     \"timestamp\": 1234567890 }");

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
     * Message Controller with Exception Handling
     */
    @Controller
    static class ExceptionHandlingController {

        /**
         * Message handler that may throw exceptions
         */
        @MessageMapping("/process")
        @SendTo("/topic/results")
        public ProcessResult processMessage(ProcessRequest request) {
            System.out.println("Processing request: " + request.getData());

            // Validation
            if (request.getData() == null || request.getData().isEmpty()) {
                throw new ValidationException("Data cannot be empty");
            }

            // Business logic
            if (request.getData().equals("error")) {
                throw new BusinessException("Cannot process 'error' data");
            }

            if (request.getData().equals("crash")) {
                throw new RuntimeException("Unexpected error occurred");
            }

            return new ProcessResult("SUCCESS", "Processed: " + request.getData());
        }

        /**
         * Handle validation exceptions - send to user only
         */
        @MessageExceptionHandler(ValidationException.class)
        @SendToUser("/queue/errors")
        public ErrorMessage handleValidationException(ValidationException ex) {
            System.err.println("Validation error: " + ex.getMessage());
            return new ErrorMessage(
                "VALIDATION_ERROR",
                ex.getMessage(),
                System.currentTimeMillis()
            );
        }

        /**
         * Handle business exceptions - send to user only
         */
        @MessageExceptionHandler(BusinessException.class)
        @SendToUser("/queue/errors")
        public ErrorMessage handleBusinessException(BusinessException ex) {
            System.err.println("Business error: " + ex.getMessage());
            return new ErrorMessage(
                "BUSINESS_ERROR",
                ex.getMessage(),
                System.currentTimeMillis()
            );
        }

        /**
         * Handle runtime exceptions - send to user only
         */
        @MessageExceptionHandler(RuntimeException.class)
        @SendToUser("/queue/errors")
        public ErrorMessage handleRuntimeException(RuntimeException ex) {
            System.err.println("Runtime error: " + ex.getMessage());
            return new ErrorMessage(
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again.",
                System.currentTimeMillis()
            );
        }

        /**
         * Generic exception handler for all other exceptions
         */
        @MessageExceptionHandler(Exception.class)
        @SendToUser("/queue/errors")
        public ErrorMessage handleGenericException(Exception ex) {
            System.err.println("Generic error: " + ex.getMessage());
            return new ErrorMessage(
                "ERROR",
                "An error occurred while processing your request",
                System.currentTimeMillis()
            );
        }

        /**
         * Broadcast error handler - sends to all users
         */
        @MessageExceptionHandler(BroadcastException.class)
        @SendTo("/topic/errors")
        public ErrorMessage handleBroadcastException(BroadcastException ex) {
            System.err.println("Broadcast error: " + ex.getMessage());
            return new ErrorMessage(
                "BROADCAST_ERROR",
                ex.getMessage(),
                System.currentTimeMillis()
            );
        }
    }

    /**
     * Process Request DTO
     */
    static class ProcessRequest {
        private String data;

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    /**
     * Process Result DTO
     */
    static class ProcessResult {
        private String status;
        private String message;

        public ProcessResult(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }

    /**
     * Error Message DTO
     */
    static class ErrorMessage {
        private String error;
        private String message;
        private long timestamp;

        public ErrorMessage(String error, String message, long timestamp) {
            this.error = error;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getError() { return error; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * Custom Exception Classes
     */
    static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }

    static class BroadcastException extends RuntimeException {
        public BroadcastException(String message) {
            super(message);
        }
    }
}
