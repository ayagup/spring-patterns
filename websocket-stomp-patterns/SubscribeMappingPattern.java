package com.example.websocket.stomp.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SubscribeMapping;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Subscribe Mapping Pattern
 * 
 * Demonstrates the @SubscribeMapping annotation for handling subscription requests.
 * Unlike @MessageMapping, this responds directly to the subscriber without routing
 * through the message broker.
 * 
 * Key Features:
 * - Handle subscription events
 * - Send initial data on subscription
 * - Direct response to subscriber
 * - No broker involvement
 * - One-time data delivery
 * - Client-specific responses
 */
@SpringBootApplication
public class SubscribeMappingPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SubscribeMappingPattern.class, args);
    }

    @Override
    public void run(String... args) {
        demonstrateSubscribeMapping();
    }

    private void demonstrateSubscribeMapping() {
        System.out.println("=== Subscribe Mapping Pattern ===\n");
        System.out.println("1. @SubscribeMapping Purpose:");
        System.out.println("   - Handles STOMP SUBSCRIBE frames");
        System.out.println("   - Sends response directly to subscriber");
        System.out.println("   - No message broker involvement");
        System.out.println("   - One-time data delivery on subscription");

        System.out.println("\n2. @MessageMapping vs @SubscribeMapping:");
        System.out.println("   @MessageMapping:");
        System.out.println("   - Client sends message → Handler → Broker → All subscribers");
        System.out.println("   - Goes through message broker");
        System.out.println("   - Broadcast to all subscribers");
        System.out.println("\n   @SubscribeMapping:");
        System.out.println("   - Client subscribes → Handler → Direct response");
        System.out.println("   - Bypasses message broker");
        System.out.println("   - Only to subscribing client");

        System.out.println("\n3. Use Cases:");
        System.out.println("   - Send initial state on subscription");
        System.out.println("   - Provide welcome messages");
        System.out.println("   - Return configuration data");
        System.out.println("   - Send user-specific information");
        System.out.println("   - Acknowledge subscription");

        System.out.println("\n4. Example Flow:");
        System.out.println("   Client: SUBSCRIBE /app/products");
        System.out.println("   Server: @SubscribeMapping(\"/products\")");
        System.out.println("   Server: Returns product list directly");
        System.out.println("   Client: Receives initial product data");

        System.out.println("\n5. Benefits:");
        System.out.println("   - Efficient initial data delivery");
        System.out.println("   - No unnecessary broker routing");
        System.out.println("   - Client-specific responses");
        System.out.println("   - Reduced server load");
        System.out.println("   - Immediate feedback to subscriber");

        System.out.println("\n6. Common Patterns:");
        System.out.println("   - Cache warm-up on subscription");
        System.out.println("   - User presence notification");
        System.out.println("   - Initial dashboard data");
        System.out.println("   - Active users list");
        System.out.println("   - Server status information");

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
     * Subscribe Mapping Controller
     */
    @Controller
    static class SubscriptionController {

        /**
         * Send initial product list on subscription
         */
        @SubscribeMapping("/products")
        public List<Product> getProducts() {
            System.out.println("Client subscribed to /app/products");
            System.out.println("Sending initial product list...");
            
            List<Product> products = new ArrayList<>();
            products.add(new Product(1L, "Laptop", 999.99));
            products.add(new Product(2L, "Mouse", 29.99));
            products.add(new Product(3L, "Keyboard", 79.99));
            
            return products;
        }

        /**
         * Send welcome message on subscription
         */
        @SubscribeMapping("/welcome")
        public WelcomeMessage getWelcomeMessage() {
            System.out.println("Client subscribed to /app/welcome");
            return new WelcomeMessage("Welcome to the chat!", 
                "Server is online", 
                System.currentTimeMillis());
        }

        /**
         * Send server status on subscription
         */
        @SubscribeMapping("/status")
        public ServerStatus getServerStatus() {
            System.out.println("Client subscribed to /app/status");
            return new ServerStatus("ONLINE", 
                Runtime.getRuntime().freeMemory(), 
                Thread.activeCount());
        }

        /**
         * Send active users count on subscription
         */
        @SubscribeMapping("/users/active")
        public ActiveUsers getActiveUsers() {
            System.out.println("Client subscribed to /app/users/active");
            List<String> users = List.of("Alice", "Bob", "Charlie");
            return new ActiveUsers(users.size(), users);
        }

        /**
         * Send configuration on subscription
         */
        @SubscribeMapping("/config")
        public AppConfig getAppConfig() {
            System.out.println("Client subscribed to /app/config");
            return new AppConfig("v1.0.0", 60000, true);
        }

        /**
         * Regular message mapping for comparison
         */
        @MessageMapping("/broadcast")
        public String broadcastMessage(String message) {
            System.out.println("Broadcasting message: " + message);
            return message;
        }
    }

    /**
     * Product DTO
     */
    static class Product {
        private Long id;
        private String name;
        private double price;

        public Product(Long id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
    }

    /**
     * Welcome Message DTO
     */
    static class WelcomeMessage {
        private String message;
        private String status;
        private long timestamp;

        public WelcomeMessage(String message, String status, long timestamp) {
            this.message = message;
            this.status = status;
            this.timestamp = timestamp;
        }

        public String getMessage() { return message; }
        public String getStatus() { return status; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * Server Status DTO
     */
    static class ServerStatus {
        private String status;
        private long freeMemory;
        private int activeThreads;

        public ServerStatus(String status, long freeMemory, int activeThreads) {
            this.status = status;
            this.freeMemory = freeMemory;
            this.activeThreads = activeThreads;
        }

        public String getStatus() { return status; }
        public long getFreeMemory() { return freeMemory; }
        public int getActiveThreads() { return activeThreads; }
    }

    /**
     * Active Users DTO
     */
    static class ActiveUsers {
        private int count;
        private List<String> users;

        public ActiveUsers(int count, List<String> users) {
            this.count = count;
            this.users = users;
        }

        public int getCount() { return count; }
        public List<String> getUsers() { return users; }
    }

    /**
     * App Config DTO
     */
    static class AppConfig {
        private String version;
        private int heartbeatInterval;
        private boolean compressionEnabled;

        public AppConfig(String version, int heartbeatInterval, boolean compressionEnabled) {
            this.version = version;
            this.heartbeatInterval = heartbeatInterval;
            this.compressionEnabled = compressionEnabled;
        }

        public String getVersion() { return version; }
        public int getHeartbeatInterval() { return heartbeatInterval; }
        public boolean isCompressionEnabled() { return compressionEnabled; }
    }
}
