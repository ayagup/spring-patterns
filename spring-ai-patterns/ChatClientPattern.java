package com.example.springai;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI Chat Client Pattern
 * 
 * Demonstrates using ChatClient for LLM interactions:
 * - Simple text completions
 * - Multi-turn conversations
 * - System prompts and roles
 * - Streaming responses
 * - Chat history management
 * 
 * Use Cases:
 * - Chatbots and virtual assistants
 * - Content generation
 * - Code assistance
 * - Question answering systems
 * - Conversational AI
 * 
 * @author Spring Patterns
 */

@Data
class ChatRequest {
    private String message;
    private String systemPrompt;
    private List<String> history;
}

@Data
class ChatHistoryEntry {
    private String role;  // user, assistant, system
    private String content;
    private Long timestamp;
}

/**
 * Chat Client Service demonstrating various patterns
 */
@Service
@Slf4j
class ChatClientService {
    
    private final ChatClient chatClient;
    private final List<Message> conversationHistory = new ArrayList<>();
    
    public ChatClientService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    /**
     * Simple single-turn chat
     */
    public String simpleChat(String userMessage) {
        log.info("Simple chat request: {}", userMessage);
        
        ChatResponse response = chatClient.call(
                new Prompt(new UserMessage(userMessage))
        );
        
        return response.getResult().getOutput().getContent();
    }
    
    /**
     * Chat with system prompt
     */
    public String chatWithSystemPrompt(String userMessage, String systemPrompt) {
        log.info("Chat with system prompt: {}", systemPrompt);
        
        List<Message> messages = List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userMessage)
        );
        
        ChatResponse response = chatClient.call(new Prompt(messages));
        return response.getResult().getOutput().getContent();
    }
    
    /**
     * Multi-turn conversation with history
     */
    public String conversationWithHistory(String userMessage) {
        log.info("Conversation with history: {}", userMessage);
        
        // Add user message to history
        conversationHistory.add(new UserMessage(userMessage));
        
        // Call with full history
        ChatResponse response = chatClient.call(new Prompt(conversationHistory));
        
        // Add assistant response to history
        String assistantMessage = response.getResult().getOutput().getContent();
        conversationHistory.add(new AssistantMessage(assistantMessage));
        
        return assistantMessage;
    }
    
    /**
     * Clear conversation history
     */
    public void clearHistory() {
        conversationHistory.clear();
        log.info("Conversation history cleared");
    }
    
    /**
     * Get conversation history
     */
    public List<ChatHistoryEntry> getHistory() {
        return conversationHistory.stream()
                .map(msg -> {
                    ChatHistoryEntry entry = new ChatHistoryEntry();
                    entry.setRole(msg.getMessageType().getValue());
                    entry.setContent(msg.getContent());
                    entry.setTimestamp(System.currentTimeMillis());
                    return entry;
                })
                .toList();
    }
    
    /**
     * Streaming chat response
     */
    public Flux<String> streamingChat(String userMessage) {
        log.info("Streaming chat: {}", userMessage);
        
        return chatClient.stream(new Prompt(new UserMessage(userMessage)))
                .map(response -> response.getResult().getOutput().getContent());
    }
}

/**
 * Advanced Chat patterns
 */
@Service
@Slf4j
class AdvancedChatService {
    
    private final ChatClient chatClient;
    
    public AdvancedChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    /**
     * Chat with role-based system prompt
     */
    public String chatAsExpert(String userMessage, String expertRole) {
        String systemPrompt = String.format(
                "You are an expert %s. Provide detailed, professional responses.", expertRole
        );
        
        List<Message> messages = List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userMessage)
        );
        
        ChatResponse response = chatClient.call(new Prompt(messages));
        return response.getResult().getOutput().getContent();
    }
    
    /**
     * Structured chat with constraints
     */
    public String structuredChat(String userMessage, int maxWords) {
        String systemPrompt = String.format(
                "Provide a concise response in maximum %d words.", maxWords
        );
        
        List<Message> messages = List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userMessage)
        );
        
        ChatResponse response = chatClient.call(new Prompt(messages));
        return response.getResult().getOutput().getContent();
    }
    
    /**
     * Multi-step reasoning chat
     */
    public String chainOfThoughtChat(String problem) {
        String systemPrompt = """
                Think step-by-step to solve this problem:
                1. Understand the problem
                2. Break it into steps
                3. Solve each step
                4. Provide the final answer
                """;
        
        List<Message> messages = List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(problem)
        );
        
        ChatResponse response = chatClient.call(new Prompt(messages));
        return response.getResult().getOutput().getContent();
    }
}

/**
 * Chat Client Information Service
 */
@Service
class ChatClientInfoService {
    
    public String getPatternInfo() {
        return """
                Spring AI Chat Client Pattern
                =============================
                
                Purpose:
                - Interact with Large Language Models (LLMs)
                - Build conversational AI applications
                - Generate text completions
                - Manage chat history and context
                
                Key Features:
                1. Simple API
                   - call(): Synchronous chat
                   - stream(): Streaming responses
                   - Prompt-based interface
                
                2. Message Types
                   - SystemMessage: Instructions to AI
                   - UserMessage: User input
                   - AssistantMessage: AI responses
                   - FunctionMessage: Function results
                
                3. Conversation Management
                   - Multi-turn conversations
                   - Context preservation
                   - History tracking
                   - Role-based prompting
                
                4. Response Handling
                   - Synchronous responses
                   - Streaming (Flux)
                   - Error handling
                   - Token usage tracking
                
                Use Cases:
                - Chatbots and virtual assistants
                - Content generation
                - Code assistance and completion
                - Question answering systems
                - Document summarization
                - Translation services
                - Creative writing assistance
                
                Best Practices:
                1. Use system prompts for behavior
                2. Maintain conversation context
                3. Implement proper error handling
                4. Stream long responses
                5. Monitor token usage
                6. Clear history when appropriate
                7. Validate and sanitize input
                8. Cache responses when possible
                """;
    }
    
    public List<String> getMessageTypes() {
        return List.of(
                "SystemMessage: AI behavior instructions",
                "UserMessage: User input/questions",
                "AssistantMessage: AI responses",
                "FunctionMessage: Function call results"
        );
    }
    
    public List<String> getChatPatterns() {
        return List.of(
                "Single-turn: One question, one answer",
                "Multi-turn: Conversation with history",
                "Role-based: Expert persona prompting",
                "Structured: Constrained responses",
                "Chain-of-thought: Step-by-step reasoning",
                "Few-shot: Examples in system prompt",
                "Streaming: Real-time response generation"
        );
    }
}

/**
 * REST Controller for Chat Client
 */
@RestController
@RequestMapping("/ai/chat")
@Slf4j
class ChatClientController {
    
    private final ChatClientService chatService;
    private final AdvancedChatService advancedService;
    private final ChatClientInfoService infoService;
    
    public ChatClientController(ChatClientService chatService,
                               AdvancedChatService advancedService,
                               ChatClientInfoService infoService) {
        this.chatService = chatService;
        this.advancedService = advancedService;
        this.infoService = infoService;
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return infoService.getPatternInfo();
    }
    
    @GetMapping("/message-types")
    public List<String> getMessageTypes() {
        return infoService.getMessageTypes();
    }
    
    @GetMapping("/patterns")
    public List<String> getChatPatterns() {
        return infoService.getChatPatterns();
    }
    
    @PostMapping("/simple")
    public String simpleChat(@RequestBody String message) {
        return chatService.simpleChat(message);
    }
    
    @PostMapping("/with-system-prompt")
    public String chatWithSystemPrompt(@RequestBody ChatRequest request) {
        return chatService.chatWithSystemPrompt(
                request.getMessage(),
                request.getSystemPrompt()
        );
    }
    
    @PostMapping("/conversation")
    public String conversation(@RequestBody String message) {
        return chatService.conversationWithHistory(message);
    }
    
    @DeleteMapping("/conversation/clear")
    public String clearHistory() {
        chatService.clearHistory();
        return "Conversation history cleared";
    }
    
    @GetMapping("/conversation/history")
    public List<ChatHistoryEntry> getHistory() {
        return chatService.getHistory();
    }
    
    @PostMapping("/streaming")
    public Flux<String> streamingChat(@RequestBody String message) {
        return chatService.streamingChat(message);
    }
    
    @PostMapping("/expert/{role}")
    public String chatAsExpert(@PathVariable String role, @RequestBody String message) {
        return advancedService.chatAsExpert(message, role);
    }
    
    @PostMapping("/structured")
    public String structuredChat(@RequestBody String message, @RequestParam int maxWords) {
        return advancedService.structuredChat(message, maxWords);
    }
    
    @PostMapping("/chain-of-thought")
    public String chainOfThoughtChat(@RequestBody String problem) {
        return advancedService.chainOfThoughtChat(problem);
    }
}

/**
 * Configuration for Chat Client
 */
@Configuration
class ChatClientConfig {
    
    // ChatClient bean would be auto-configured by Spring AI
    // or manually configured with specific LLM provider
}

@SpringBootApplication
public class ChatClientPattern {
    public static void main(String[] args) {
        SpringApplication.run(ChatClientPattern.class, args);
    }
}
