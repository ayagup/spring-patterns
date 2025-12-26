package com.example.springaipatterns;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval Augmented Generation) Pattern
 * 
 * Demonstrates the implementation of RAG using Spring AI for combining
 * document retrieval with LLM generation to provide context-aware responses.
 * 
 * Key Concepts:
 * - Document retrieval from vector store
 * - Context augmentation
 * - Prompt engineering with retrieved context
 * - Response generation with LLM
 * - Source citation
 */
@SpringBootApplication
public class RAGPattern {

    public static void main(String[] args) {
        SpringApplication.run(RAGPattern.class, args);
    }

    @Service
    static class RAGService {
        
        private final VectorStore vectorStore;
        private final ChatClient chatClient;
        
        public RAGService(VectorStore vectorStore, ChatClient chatClient) {
            this.vectorStore = vectorStore;
            this.chatClient = chatClient;
        }
        
        /**
         * Perform RAG: retrieve relevant documents and generate response
         */
        public RAGResponse query(String question, int topK) {
            // Step 1: Retrieve relevant documents
            List<Document> relevantDocs = retrieveDocuments(question, topK);
            
            // Step 2: Build context from retrieved documents
            String context = buildContext(relevantDocs);
            
            // Step 3: Create augmented prompt
            String augmentedPrompt = buildPrompt(question, context);
            
            // Step 4: Generate response
            Prompt prompt = new Prompt(new UserMessage(augmentedPrompt));
            ChatResponse chatResponse = chatClient.call(prompt);
            String answer = chatResponse.getResult().getOutput().getContent();
            
            // Step 5: Extract sources
            List<String> sources = relevantDocs.stream()
                .map(doc -> doc.getMetadata().getOrDefault("source", "Unknown").toString())
                .distinct()
                .collect(Collectors.toList());
            
            return new RAGResponse(
                question,
                answer,
                context,
                sources,
                relevantDocs.size()
            );
        }
        
        /**
         * Retrieve relevant documents from vector store
         */
        private List<Document> retrieveDocuments(String query, int topK) {
            SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(0.7);
            return vectorStore.similaritySearch(searchRequest);
        }
        
        /**
         * Build context from retrieved documents
         */
        private String buildContext(List<Document> documents) {
            return documents.stream()
                .map(doc -> {
                    String source = doc.getMetadata().getOrDefault("source", "Unknown").toString();
                    return String.format("[Source: %s]\n%s", source, doc.getContent());
                })
                .collect(Collectors.joining("\n\n---\n\n"));
        }
        
        /**
         * Build augmented prompt with context
         */
        private String buildPrompt(String question, String context) {
            return String.format("""
                You are a helpful assistant that answers questions based on the provided context.
                
                Context:
                %s
                
                Question: %s
                
                Instructions:
                - Answer the question using ONLY the information from the context above
                - If the context doesn't contain enough information to answer, say so
                - Be concise and accurate
                - Cite sources when possible
                
                Answer:
                """, context, question);
        }
        
        /**
         * Perform RAG with custom prompt template
         */
        public RAGResponse queryWithTemplate(
                String question,
                int topK,
                String promptTemplate) {
            // Retrieve relevant documents
            List<Document> relevantDocs = retrieveDocuments(question, topK);
            
            // Build context
            String context = buildContext(relevantDocs);
            
            // Use custom template
            String augmentedPrompt = String.format(promptTemplate, context, question);
            
            // Generate response
            Prompt prompt = new Prompt(new UserMessage(augmentedPrompt));
            ChatResponse chatResponse = chatClient.call(prompt);
            String answer = chatResponse.getResult().getOutput().getContent();
            
            // Extract sources
            List<String> sources = relevantDocs.stream()
                .map(doc -> doc.getMetadata().getOrDefault("source", "Unknown").toString())
                .distinct()
                .collect(Collectors.toList());
            
            return new RAGResponse(question, answer, context, sources, relevantDocs.size());
        }
        
        /**
         * Perform RAG with filtering
         */
        public RAGResponse queryWithFilter(
                String question,
                int topK,
                Map<String, Object> metadataFilter) {
            // Retrieve with filter
            SearchRequest searchRequest = SearchRequest.query(question)
                .withTopK(topK)
                .withSimilarityThreshold(0.7)
                .withFilterExpression(metadataFilter);
            
            List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);
            
            // Build context and prompt
            String context = buildContext(relevantDocs);
            String augmentedPrompt = buildPrompt(question, context);
            
            // Generate response
            Prompt prompt = new Prompt(new UserMessage(augmentedPrompt));
            ChatResponse chatResponse = chatClient.call(prompt);
            String answer = chatResponse.getResult().getOutput().getContent();
            
            // Extract sources
            List<String> sources = relevantDocs.stream()
                .map(doc -> doc.getMetadata().getOrDefault("source", "Unknown").toString())
                .distinct()
                .collect(Collectors.toList());
            
            return new RAGResponse(question, answer, context, sources, relevantDocs.size());
        }
    }

    @RestController
    @RequestMapping("/api/rag")
    static class RAGController {
        
        private final RAGService ragService;
        
        public RAGController(RAGService ragService) {
            this.ragService = ragService;
        }
        
        @PostMapping("/query")
        public RAGResponse query(@RequestBody RAGQueryRequest request) {
            return ragService.query(request.question(), request.topK());
        }
        
        @PostMapping("/query-template")
        public RAGResponse queryWithTemplate(@RequestBody RAGTemplateRequest request) {
            return ragService.queryWithTemplate(
                request.question(),
                request.topK(),
                request.promptTemplate()
            );
        }
        
        @PostMapping("/query-filter")
        public RAGResponse queryWithFilter(@RequestBody RAGFilterRequest request) {
            return ragService.queryWithFilter(
                request.question(),
                request.topK(),
                request.metadataFilter()
            );
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "RAG (Retrieval Augmented Generation) Pattern",
                "description", "Combine document retrieval with LLM generation for context-aware responses",
                "features", List.of(
                    "Document retrieval from vector store",
                    "Context augmentation",
                    "Source citation",
                    "Custom prompt templates",
                    "Metadata filtering",
                    "Similarity threshold control"
                ),
                "workflow", List.of(
                    "1. Retrieve relevant documents",
                    "2. Build context from documents",
                    "3. Augment prompt with context",
                    "4. Generate LLM response",
                    "5. Cite sources"
                ),
                "endpoints", List.of(
                    "POST /api/rag/query",
                    "POST /api/rag/query-template",
                    "POST /api/rag/query-filter",
                    "GET /api/rag/info"
                )
            );
        }
    }

    // DTOs
    record RAGQueryRequest(String question, int topK) {}
    record RAGTemplateRequest(String question, int topK, String promptTemplate) {}
    record RAGFilterRequest(String question, int topK, Map<String, Object> metadataFilter) {}
    record RAGResponse(
        String question,
        String answer,
        String context,
        List<String> sources,
        int documentsRetrieved
    ) {}
}
