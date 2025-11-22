package com.spring.patterns.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Scope Pattern
 * 
 * WebSocket scope creates bean instances bound to WebSocket sessions.
 * Beans persist for the duration of the WebSocket connection.
 * 
 * Characteristics:
 * - One instance per WebSocket session
 * - Lifecycle tied to WebSocket connection
 * - Destroyed when connection closes
 * - Available in WebSocket-enabled applications
 * 
 * Use Cases:
 * - WebSocket session state
 * - Real-time chat sessions
 * - Live updates tracking
 * - WebSocket-specific configuration
 * - Per-connection caching
 */
@SpringBootApplication
@EnableWebSocketMessageBroker
public class WebSocketScopePattern {

    public static void main(String[] args) {
        SpringApplication.run(WebSocketScopePattern.class, args);
        System.out.println("\n=== WebSocket Scope Pattern Started ===");
        System.out.println("WebSocket endpoint: ws://localhost:8080/ws");
    }
}

/**
 * WebSocket scope configuration
 */
@Configuration
class WebSocketScopedConfig {
    
    @Bean
    @org.springframework.context.annotation.Scope(
        value = "websocket", 
        proxyMode = ScopedProxyMode.TARGET_CLASS
    )
    public WebSocketSession webSocketSession() {
        return new WebSocketSession();
    }
    
    @Bean
    @org.springframework.context.annotation.Scope(value = "websocket")
    public ChatContext chatContext() {
        return new ChatContext();
    }
}

/**
 * WebSocket-scoped session bean
 */
class WebSocketSession {
    private final String sessionId;
    private final LocalDateTime connectedAt;
    private String username;
    private final List<String> messageHistory = new ArrayList<>();
    
    public WebSocketSession() {
        this.sessionId = "WS-" + UUID.randomUUID().toString().substring(0, 8);
        this.connectedAt = LocalDateTime.now();
        System.out.println("WebSocketSession created: " + sessionId);
    }
    
    public void addMessage(String message) {
        messageHistory.add(message);
    }
    
    public String getSessionId() { return sessionId; }
    public LocalDateTime getConnectedAt() { return connectedAt; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getMessageHistory() { return new ArrayList<>(messageHistory); }
}

/**
 * WebSocket chat context
 */
class ChatContext {
    private final String contextId;
    private String currentRoom = "general";
    private final Map<String, Object> attributes = new HashMap<>();
    
    public ChatContext() {
        this.contextId = "CTX-" + System.currentTimeMillis();
        System.out.println("ChatContext created: " + contextId);
    }
    
    public String getContextId() { return contextId; }
    public String getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(String room) { this.currentRoom = room; }
    public void setAttribute(String key, Object value) { attributes.put(key, value); }
    public Object getAttribute(String key) { return attributes.get(key); }
}

/**
 * Custom Scope Pattern
 * 
 * Custom scopes allow you to define your own bean scoping logic.
 * Implements Spring's Scope interface to control bean lifecycle.
 */
@Component
class CustomScope implements Scope {
    
    private final Map<String, Object> scopedObjects = new ConcurrentHashMap<>();
    private final Map<String, Runnable> destructionCallbacks = new ConcurrentHashMap<>();
    
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return scopedObjects.computeIfAbsent(name, k -> {
            System.out.println("Creating bean in custom scope: " + name);
            return objectFactory.getObject();
        });
    }
    
    @Override
    public Object remove(String name) {
        System.out.println("Removing bean from custom scope: " + name);
        destructionCallbacks.remove(name);
        return scopedObjects.remove(name);
    }
    
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        destructionCallbacks.put(name, callback);
    }
    
    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }
    
    @Override
    public String getConversationId() {
        return "custom-scope-" + Thread.currentThread().getId();
    }
    
    public void clear() {
        destructionCallbacks.values().forEach(Runnable::run);
        destructionCallbacks.clear();
        scopedObjects.clear();
    }
}

/**
 * Custom scope registration
 */
@Configuration
class CustomScopeConfig {
    
    @Bean
    public static org.springframework.beans.factory.config.CustomScopeConfigurer customScopeConfigurer() {
        org.springframework.beans.factory.config.CustomScopeConfigurer configurer = 
            new org.springframework.beans.factory.config.CustomScopeConfigurer();
        
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("custom", new CustomScope());
        configurer.setScopes(scopes);
        
        return configurer;
    }
    
    @Bean
    @org.springframework.context.annotation.Scope("custom")
    public CustomScopedBean customScopedBean() {
        return new CustomScopedBean();
    }
}

/**
 * Bean using custom scope
 */
class CustomScopedBean {
    private final String beanId;
    private final LocalDateTime createdAt;
    
    public CustomScopedBean() {
        this.beanId = "CUSTOM-" + UUID.randomUUID().toString().substring(0, 8);
        this.createdAt = LocalDateTime.now();
        System.out.println("CustomScopedBean created: " + beanId);
    }
    
    public String getBeanId() { return beanId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

/**
 * REST Controller for testing
 */
@RestController
@RequestMapping("/api/scope")
class CustomScopeController {
    
    private final CustomScopedBean customBean;
    
    public CustomScopeController(CustomScopedBean customBean) {
        this.customBean = customBean;
    }
    
    @GetMapping("/custom/info")
    public String getCustomBeanInfo() {
        return "Custom Scoped Bean:\n" +
               "  Bean ID: " + customBean.getBeanId() + "\n" +
               "  Created: " + customBean.getCreatedAt();
    }
}

/**
 * Key Points:
 * 
 * 1. WebSocket Scope:
 *    - Bound to WebSocket session lifecycle
 *    - Created on connection, destroyed on disconnect
 *    - Use for real-time communication state
 * 
 * 2. Custom Scope Implementation:
 *    - Implement org.springframework.beans.factory.config.Scope
 *    - Control bean creation, retrieval, and destruction
 *    - Register via CustomScopeConfigurer
 * 
 * 3. Custom Scope Methods:
 *    - get(): Create or retrieve bean instance
 *    - remove(): Remove bean from scope
 *    - registerDestructionCallback(): Cleanup logic
 *    - resolveContextualObject(): Resolve contextual references
 *    - getConversationId(): Unique scope identifier
 * 
 * 4. Use Cases for Custom Scopes:
 *    ✓ Tenant-specific scoping
 *    ✓ Feature-flag based scoping
 *    ✓ Time-based scoping
 *    ✓ Cache-backed scoping
 *    ✓ Custom lifecycle requirements
 * 
 * 5. WebSocket Scope Use Cases:
 *    ✓ Chat applications
 *    ✓ Live notifications
 *    ✓ Real-time collaboration
 *    ✓ Game sessions
 *    ✓ Live streaming state
 */
