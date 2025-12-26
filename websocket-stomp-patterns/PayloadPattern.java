package com.example.websocket.stomp.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Payload Pattern
 * 
 * Demonstrates @Payload annotation for explicit message body binding and validation.
 * Enables validation of incoming messages using Bean Validation annotations.
 * 
 * Key Features:
 * - Explicit payload binding
 * - Message validation support
 * - Custom converters
 * - Type-safe message handling
 * - Required payload enforcement
 */
@SpringBootApplication
public class PayloadPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PayloadPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Payload Pattern ===\n");
        System.out.println("@Payload - Explicitly bind and validate message body");
        System.out.println("Supports JSR-303 validation annotations");
        System.out.println("Enables type-safe message processing");
    }

    @Configuration
    @EnableWebSocketMessageBroker
    static class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic");
            config.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/ws").withSockJS();
        }
    }

    @Controller
    @Validated
    static class MessageController {
        @MessageMapping("/validate")
        public void processValidatedMessage(@Payload @Valid ValidatedMessage message) {
            System.out.println("Valid message: " + message.getContent());
        }

        @MessageMapping("/explicit")
        public void processExplicitPayload(@Payload String message) {
            System.out.println("Explicit payload: " + message);
        }

        @MessageMapping("/required")
        public void processRequiredPayload(@Payload(required = true) ChatMessage message) {
            System.out.println("Required payload received: " + message.getText());
        }
    }

    static class ValidatedMessage {
        @NotBlank(message = "Content cannot be blank")
        private String content;

        @NotNull(message = "User ID is required")
        private Long userId;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    static class ChatMessage {
        private String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
