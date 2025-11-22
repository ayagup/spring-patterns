package com.example.websocket.handler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket Handler Pattern
 * =========================
 * 
 * The WebSocket Handler Pattern provides low-level control over WebSocket connections
 * through handler classes that process raw WebSocket messages. Unlike STOMP, this pattern
 * gives you direct access to WebSocket frames and full control over the protocol.
 * 
 * Key Concepts:
 * ------------
 * 1. Handler Types:
 *    a) TextWebSocketHandler - Handles text messages
 *    b) BinaryWebSocketHandler - Handles binary messages
 *    c) AbstractWebSocketHandler - Handles both text and binary
 * 
 * 2. Lifecycle Methods:
 *    - afterConnectionEstablished() - Called when connection opens
 *    - handleMessage() - Process incoming messages
 *    - handleTransportError() - Handle errors
 *    - afterConnectionClosed() - Called when connection closes
 * 
 * 3. Message Types:
 *    - TextMessage - UTF-8 text data
 *    - BinaryMessage - Binary data
 *    - PingMessage - Ping frame
 *    - PongMessage - Pong frame
 * 
 * Use Cases:
 * ---------
 * - Custom WebSocket protocols
 * - Binary data transmission (files, images)
 * - Gaming applications
 * - Video/audio streaming
 * - IoT device communication
 * - Low-latency applications
 * - Custom message formats
 * 
 * Advantages:
 * ----------
 * - Full control over WebSocket protocol
 * - No overhead from STOMP layer
 * - Direct message handling
 * - Support for binary data
 * - Custom framing and encoding
 * 
 * Best Practices:
 * --------------
 * 1. Use appropriate handler type (Text/Binary)
 * 2. Handle errors gracefully
 * 3. Manage session lifecycle properly
 * 4. Implement connection pooling
 * 5. Use concurrent data structures for session storage
 * 6. Implement heartbeat/ping-pong mechanism
 * 7. Handle large messages appropriately
 * 8. Implement rate limiting
 * 
 * Dependencies:
 * ------------
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-websocket</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>com.fasterxml.jackson.core</groupId>
 *     <artifactId>jackson-databind</artifactId>
 * </dependency>
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class WebSocketHandlerPattern {

    public static void main(String[] args) {
        SpringApplication.run(WebSocketHandlerPattern.class, args);
    }
}

/**
 * WebSocket Configuration
 */
@Configuration
@EnableWebSocket
class WebSocketHandlerConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatHandler;
    private final BinaryDataWebSocketHandler binaryHandler;
    private final EchoWebSocketHandler echoHandler;

    public WebSocketHandlerConfig(
            ChatWebSocketHandler chatHandler,
            BinaryDataWebSocketHandler binaryHandler,
            EchoWebSocketHandler echoHandler) {
        this.chatHandler = chatHandler;
        this.binaryHandler = binaryHandler;
        this.echoHandler = echoHandler;
    }

    /**
     * Register WebSocket handlers
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Chat handler (text messages)
        registry.addHandler(chatHandler, "/ws/chat")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS();

        // Binary data handler
        registry.addHandler(binaryHandler, "/ws/binary")
                .setAllowedOrigins("*");

        // Echo handler (for testing)
        registry.addHandler(echoHandler, "/ws/echo")
                .setAllowedOrigins("*")
                .withSockJS();

        // Native WebSocket (no SockJS)
        registry.addHandler(chatHandler, "/ws/chat-native")
                .setAllowedOrigins("*");
    }
}

/**
 * Chat WebSocket Handler
 * Handles text-based chat messages
 */
@Component
class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<ChatMessage> messageHistory = new CopyOnWriteArrayList<>();

    /**
     * Called when a new WebSocket connection is established
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New WebSocket connection: {}", session.getId());
        
        sessions.put(session.getId(), session);
        
        // Send welcome message
        WelcomeMessage welcome = new WelcomeMessage(
            session.getId(),
            "Welcome to WebSocket Chat!",
            sessions.size(),
            LocalDateTime.now().toString()
        );
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcome)));
        
        // Broadcast join notification
        broadcastMessage(new ChatMessage(
            "SYSTEM",
            "User " + session.getId() + " joined the chat",
            MessageType.JOIN
        ));
    }

    /**
     * Handle incoming text messages
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Received message from {}: {}", session.getId(), message.getPayload());
        
        try {
            ChatMessage chatMessage = objectMapper.readValue(
                message.getPayload(), ChatMessage.class);
            
            chatMessage.setTimestamp(LocalDateTime.now().toString());
            chatMessage.setSenderId(session.getId());
            
            // Store in history
            messageHistory.add(chatMessage);
            
            // Handle different message types
            switch (chatMessage.getType()) {
                case CHAT:
                    broadcastMessage(chatMessage);
                    break;
                case PRIVATE:
                    sendPrivateMessage(chatMessage);
                    break;
                case TYPING:
                    broadcastTypingIndicator(chatMessage);
                    break;
                default:
                    logger.warn("Unknown message type: {}", chatMessage.getType());
            }
            
        } catch (Exception e) {
            logger.error("Error handling message", e);
            sendError(session, "Failed to process message: " + e.getMessage());
        }
    }

    /**
     * Handle transport errors
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error for session {}: {}", 
            session.getId(), exception.getMessage());
        
        if (session.isOpen()) {
            sendError(session, "Transport error occurred");
        }
    }

    /**
     * Called when WebSocket connection is closed
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {} - Status: {}", 
            session.getId(), status);
        
        sessions.remove(session.getId());
        
        // Broadcast leave notification
        broadcastMessage(new ChatMessage(
            "SYSTEM",
            "User " + session.getId() + " left the chat",
            MessageType.LEAVE
        ));
    }

    /**
     * Check if handler supports partial messages
     */
    @Override
    public boolean supportsPartialMessages() {
        return true;  // Support for large messages sent in chunks
    }

    /**
     * Broadcast message to all connected clients
     */
    private void broadcastMessage(ChatMessage message) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            logger.error("Error serializing message", e);
            return;
        }

        TextMessage textMessage = new TextMessage(payload);
        
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    logger.error("Error sending message to {}", session.getId(), e);
                }
            }
        });
    }

    /**
     * Send private message to specific user
     */
    private void sendPrivateMessage(ChatMessage message) {
        WebSocketSession targetSession = sessions.get(message.getRecipientId());
        
        if (targetSession != null && targetSession.isOpen()) {
            try {
                String payload = objectMapper.writeValueAsString(message);
                targetSession.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                logger.error("Error sending private message", e);
            }
        }
    }

    /**
     * Broadcast typing indicator
     */
    private void broadcastTypingIndicator(ChatMessage message) {
        // Only broadcast to others, not sender
        sessions.values().stream()
                .filter(s -> !s.getId().equals(message.getSenderId()) && s.isOpen())
                .forEach(session -> {
                    try {
                        session.sendMessage(new TextMessage(
                            objectMapper.writeValueAsString(message)
                        ));
                    } catch (IOException e) {
                        logger.error("Error broadcasting typing indicator", e);
                    }
                });
    }

    /**
     * Send error message to client
     */
    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            ErrorMessage error = new ErrorMessage(errorMessage, LocalDateTime.now().toString());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            logger.error("Error sending error message", e);
        }
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public List<ChatMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }
}

/**
 * Binary Data WebSocket Handler
 * Handles binary data transmission (files, images, etc.)
 */
@Component
class BinaryDataWebSocketHandler extends BinaryWebSocketHandler {

    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(BinaryDataWebSocketHandler.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, BinaryDataTransfer> transfers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Binary WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        logger.debug("Received binary message from {}: {} bytes", 
            session.getId(), message.getPayloadLength());

        String transferId = session.getId();
        BinaryDataTransfer transfer = transfers.computeIfAbsent(
            transferId, k -> new BinaryDataTransfer(transferId));

        // Accumulate binary data
        transfer.addChunk(message.getPayload().array());

        // Check if transfer is complete (based on message flag)
        if (message.isLast()) {
            logger.info("Binary transfer complete: {} - Total bytes: {}", 
                transferId, transfer.getTotalBytes());
            
            // Process complete binary data
            processBinaryData(session, transfer);
            transfers.remove(transferId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Binary WebSocket connection closed: {}", session.getId());
        sessions.remove(session.getId());
        transfers.remove(session.getId());
    }

    /**
     * Process complete binary data
     */
    private void processBinaryData(WebSocketSession session, BinaryDataTransfer transfer) throws IOException {
        byte[] data = transfer.getData();
        
        // Example: Echo back the binary data
        session.sendMessage(new BinaryMessage(data));
        
        // Example: Save to file, process image, etc.
        logger.info("Processed {} bytes of binary data", data.length);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}

/**
 * Echo WebSocket Handler
 * Simple echo server for testing
 */
@Component
class EchoWebSocketHandler extends AbstractWebSocketHandler {

    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(EchoWebSocketHandler.class);

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Echo: {}", message.getPayload());
        
        // Echo the message back with timestamp
        String echoMessage = String.format(
            "{\"original\": \"%s\", \"timestamp\": \"%s\"}",
            message.getPayload(),
            LocalDateTime.now()
        );
        
        session.sendMessage(new TextMessage(echoMessage));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        logger.debug("Echo binary: {} bytes", message.getPayloadLength());
        session.sendMessage(message);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        logger.debug("Received pong from {}", session.getId());
    }
}

/**
 * Chat Message Model
 */
class ChatMessage {
    private String senderId;
    private String recipientId;
    private String content;
    private MessageType type;
    private String timestamp;

    public ChatMessage() {}

    public ChatMessage(String senderId, String content, MessageType type) {
        this.senderId = senderId;
        this.content = content;
        this.type = type;
    }

    // Getters and Setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

enum MessageType {
    CHAT, PRIVATE, JOIN, LEAVE, TYPING, SYSTEM
}

/**
 * Welcome Message
 */
class WelcomeMessage {
    private String sessionId;
    private String message;
    private int activeUsers;
    private String timestamp;

    public WelcomeMessage(String sessionId, String message, int activeUsers, String timestamp) {
        this.sessionId = sessionId;
        this.message = message;
        this.activeUsers = activeUsers;
        this.timestamp = timestamp;
    }

    public String getSessionId() { return sessionId; }
    public String getMessage() { return message; }
    public int getActiveUsers() { return activeUsers; }
    public String getTimestamp() { return timestamp; }
}

/**
 * Error Message
 */
class ErrorMessage {
    private String error;
    private String timestamp;

    public ErrorMessage(String error, String timestamp) {
        this.error = error;
        this.timestamp = timestamp;
    }

    public String getError() { return error; }
    public String getTimestamp() { return timestamp; }
}

/**
 * Binary Data Transfer
 */
class BinaryDataTransfer {
    private String transferId;
    private List<byte[]> chunks;
    private long totalBytes;

    public BinaryDataTransfer(String transferId) {
        this.transferId = transferId;
        this.chunks = new ArrayList<>();
        this.totalBytes = 0;
    }

    public void addChunk(byte[] chunk) {
        chunks.add(chunk);
        totalBytes += chunk.length;
    }

    public byte[] getData() {
        byte[] result = new byte[(int) totalBytes];
        int offset = 0;
        
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, result, offset, chunk.length);
            offset += chunk.length;
        }
        
        return result;
    }

    public String getTransferId() { return transferId; }
    public long getTotalBytes() { return totalBytes; }
}

/*
 * Client-Side JavaScript Example:
 * ================================
 * 
 * // Text WebSocket
 * var ws = new WebSocket('ws://localhost:8080/ws/chat-native');
 * 
 * ws.onopen = function() {
 *     console.log('Connected to WebSocket');
 * };
 * 
 * ws.onmessage = function(event) {
 *     var message = JSON.parse(event.data);
 *     console.log('Received:', message);
 * };
 * 
 * ws.onerror = function(error) {
 *     console.error('WebSocket error:', error);
 * };
 * 
 * ws.onclose = function(event) {
 *     console.log('WebSocket closed:', event.code, event.reason);
 * };
 * 
 * // Send text message
 * function sendMessage(content) {
 *     var message = {
 *         content: content,
 *         type: 'CHAT'
 *     };
 *     ws.send(JSON.stringify(message));
 * }
 * 
 * // Binary WebSocket
 * var binaryWs = new WebSocket('ws://localhost:8080/ws/binary');
 * binaryWs.binaryType = 'arraybuffer';
 * 
 * // Send binary data
 * function sendFile(file) {
 *     var reader = new FileReader();
 *     reader.onload = function(event) {
 *         binaryWs.send(event.target.result);
 *     };
 *     reader.readAsArrayBuffer(file);
 * }
 * 
 * // Heartbeat/Ping
 * setInterval(function() {
 *     if (ws.readyState === WebSocket.OPEN) {
 *         ws.send(JSON.stringify({ type: 'PING' }));
 *     }
 * }, 30000);
 */
