package com.example.springaipatterns;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat Memory Pattern
 * 
 * Demonstrates the use of Spring AI's Chat Memory for maintaining conversation
 * context and history across multiple interactions.
 * 
 * Key Concepts:
 * - Conversation history management
 * - Context window management
 * - Message persistence
 * - Session-based memory
 * - Memory size limits
 */
@SpringBootApplication
public class ChatMemoryPattern {

    public static void main(String[] args) {
        SpringApplication.run(ChatMemoryPattern.class, args);
    }

    @Service
    static class ChatMemoryService {
        
        private final Map<String, ConversationMemory> sessions = new ConcurrentHashMap<>();
        
        /**
         * Initialize a new conversation session
         */
        public String createSession(int maxMessages) {
            String sessionId = java.util.UUID.randomUUID().toString();
            sessions.put(sessionId, new ConversationMemory(maxMessages));
            return sessionId;
        }
        
        /**
         * Add user message to memory
         */
        public void addUserMessage(String sessionId, String content) {
            ConversationMemory memory = sessions.get(sessionId);
            if (memory == null) {
                throw new IllegalArgumentException("Session not found: " + sessionId);
            }
            memory.addMessage(new UserMessage(content));
        }
        
        /**
         * Add assistant message to memory
         */
        public void addAssistantMessage(String sessionId, String content) {
            ConversationMemory memory = sessions.get(sessionId);
            if (memory == null) {
                throw new IllegalArgumentException("Session not found: " + sessionId);
            }
            memory.addMessage(new AssistantMessage(content));
        }
        
        /**
         * Get conversation history
         */
        public List<Message> getHistory(String sessionId) {
            ConversationMemory memory = sessions.get(sessionId);
            if (memory == null) {
                throw new IllegalArgumentException("Session not found: " + sessionId);
            }
            return new ArrayList<>(memory.getMessages());
        }
        
        /**
         * Get recent messages
         */
        public List<Message> getRecentMessages(String sessionId, int count) {
            ConversationMemory memory = sessions.get(sessionId);
            if (memory == null) {
                throw new IllegalArgumentException("Session not found: " + sessionId);
            }
            List<Message> messages = memory.getMessages();
            int start = Math.max(0, messages.size() - count);
            return messages.subList(start, messages.size());
        }
        
        /**
         * Clear conversation history
         */
        public void clearHistory(String sessionId) {
            ConversationMemory memory = sessions.get(sessionId);
            if (memory == null) {
                throw new IllegalArgumentException("Session not found: " + sessionId);
            }
            memory.clear();
        }
        
        /**
         * Delete session
         */
        public void deleteSession(String sessionId) {
            sessions.remove(sessionId);
        }
        
        /**
         * Get conversation summary
         */
        public ConversationSummary getSummary(String sessionId) {
            ConversationMemory memory = sessions.get(sessionId);
            if (memory == null) {
                throw new IllegalArgumentException("Session not found: " + sessionId);
            }
            
            List<Message> messages = memory.getMessages();
            long userCount = messages.stream()
                .filter(m -> m instanceof UserMessage)
                .count();
            long assistantCount = messages.stream()
                .filter(m -> m instanceof AssistantMessage)
                .count();
            
            return new ConversationSummary(
                sessionId,
                messages.size(),
                (int) userCount,
                (int) assistantCount,
                memory.getMaxMessages()
            );
        }
    }

    static class ConversationMemory {
        private final List<Message> messages = new ArrayList<>();
        private final int maxMessages;
        
        public ConversationMemory(int maxMessages) {
            this.maxMessages = maxMessages;
        }
        
        public void addMessage(Message message) {
            messages.add(message);
            // Maintain size limit
            while (messages.size() > maxMessages) {
                messages.remove(0);
            }
        }
        
        public List<Message> getMessages() {
            return messages;
        }
        
        public void clear() {
            messages.clear();
        }
        
        public int getMaxMessages() {
            return maxMessages;
        }
    }

    @RestController
    @RequestMapping("/api/chat-memory")
    static class ChatMemoryController {
        
        private final ChatMemoryService chatMemoryService;
        
        public ChatMemoryController(ChatMemoryService chatMemoryService) {
            this.chatMemoryService = chatMemoryService;
        }
        
        @PostMapping("/session/create")
        public SessionResponse createSession(@RequestBody CreateSessionRequest request) {
            String sessionId = chatMemoryService.createSession(request.maxMessages());
            return new SessionResponse(sessionId, "Session created successfully");
        }
        
        @PostMapping("/message/user")
        public MessageResponse addUserMessage(@RequestBody AddMessageRequest request) {
            chatMemoryService.addUserMessage(request.sessionId(), request.content());
            return new MessageResponse("User message added", "success");
        }
        
        @PostMapping("/message/assistant")
        public MessageResponse addAssistantMessage(@RequestBody AddMessageRequest request) {
            chatMemoryService.addAssistantMessage(request.sessionId(), request.content());
            return new MessageResponse("Assistant message added", "success");
        }
        
        @GetMapping("/history/{sessionId}")
        public HistoryResponse getHistory(@PathVariable String sessionId) {
            List<Message> messages = chatMemoryService.getHistory(sessionId);
            List<MessageInfo> messageInfos = messages.stream()
                .map(m -> new MessageInfo(
                    m.getMessageType().getValue(),
                    m.getContent()
                ))
                .toList();
            return new HistoryResponse(messageInfos, messages.size());
        }
        
        @GetMapping("/history/{sessionId}/recent")
        public HistoryResponse getRecentHistory(
                @PathVariable String sessionId,
                @RequestParam(defaultValue = "5") int count) {
            List<Message> messages = chatMemoryService.getRecentMessages(sessionId, count);
            List<MessageInfo> messageInfos = messages.stream()
                .map(m -> new MessageInfo(
                    m.getMessageType().getValue(),
                    m.getContent()
                ))
                .toList();
            return new HistoryResponse(messageInfos, messages.size());
        }
        
        @DeleteMapping("/history/{sessionId}")
        public MessageResponse clearHistory(@PathVariable String sessionId) {
            chatMemoryService.clearHistory(sessionId);
            return new MessageResponse("History cleared", "success");
        }
        
        @DeleteMapping("/session/{sessionId}")
        public MessageResponse deleteSession(@PathVariable String sessionId) {
            chatMemoryService.deleteSession(sessionId);
            return new MessageResponse("Session deleted", "success");
        }
        
        @GetMapping("/summary/{sessionId}")
        public ConversationSummary getSummary(@PathVariable String sessionId) {
            return chatMemoryService.getSummary(sessionId);
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Chat Memory Pattern",
                "description", "Maintain conversation context and history across interactions",
                "features", List.of(
                    "Session management",
                    "Message history",
                    "Context window limits",
                    "Recent message retrieval",
                    "Conversation summaries"
                ),
                "endpoints", List.of(
                    "POST /api/chat-memory/session/create",
                    "POST /api/chat-memory/message/user",
                    "POST /api/chat-memory/message/assistant",
                    "GET /api/chat-memory/history/{sessionId}",
                    "GET /api/chat-memory/history/{sessionId}/recent",
                    "DELETE /api/chat-memory/history/{sessionId}",
                    "DELETE /api/chat-memory/session/{sessionId}",
                    "GET /api/chat-memory/summary/{sessionId}",
                    "GET /api/chat-memory/info"
                )
            );
        }
    }

    // DTOs
    record CreateSessionRequest(int maxMessages) {}
    record SessionResponse(String sessionId, String message) {}
    record AddMessageRequest(String sessionId, String content) {}
    record MessageResponse(String message, String status) {}
    record MessageInfo(String type, String content) {}
    record HistoryResponse(List<MessageInfo> messages, int count) {}
    record ConversationSummary(String sessionId, int totalMessages, int userMessages, int assistantMessages, int maxMessages) {}
}
