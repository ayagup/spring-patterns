package com.example.websocket.stomp.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Map;

/**
 * Header Pattern
 * 
 * Demonstrates @Header and @Headers annotations for accessing STOMP message headers.
 * Headers carry metadata about the message (content-type, timestamp, custom headers).
 * 
 * Key Features:
 * - Access individual headers with @Header
 * - Access all headers with @Headers
 * - Required vs optional headers
 * - Default header values
 * - Type conversion for headers
 */
@SpringBootApplication
public class HeaderPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(HeaderPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Header Pattern ===\n");
        System.out.println("@Header - Extract single header value");
        System.out.println("@Headers - Extract all headers as Map");
        System.out.println("\nCommon headers: content-type, destination, message-id, subscription");
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
    static class MessageController {
        @MessageMapping("/process")
        public void processWithHeaders(
                @Header("content-type") String contentType,
                @Header(value = "priority", defaultValue = "NORMAL") String priority,
                @Header(value = "custom-header", required = false) String customHeader,
                @Headers Map<String, Object> headers,
                String message) {
            
            System.out.println("Content-Type: " + contentType);
            System.out.println("Priority: " + priority);
            System.out.println("Custom Header: " + customHeader);
            System.out.println("All headers: " + headers.keySet());
            System.out.println("Message: " + message);
        }
    }
}
