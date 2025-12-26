package com.example.springai;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.ContentFormatTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Spring AI Document Processing & RAG Patterns
 * 
 * Covers:
 * - Document Reader: Load documents from various sources
 * - Document Writer: Save documents
 * - Document Transformer: Process and split documents
 * - Chat Memory: Conversation context management
 * - RAG: Retrieval Augmented Generation
 * 
 * @author Spring Patterns
 */

@Data
class RAGRequest {
    private String query;
    private int contextSize = 3;
}

@Data
class RAGResponse {
    private String answer;
    private List<String> sources;
    private List<Document> context;
}

/**
 * Document Reader Service
 */
@Service
@Slf4j
class DocumentReaderService {
    
    /**
     * Read text document
     */
    public List<Document> readTextDocument(Resource resource) {
        log.info("Reading text document");
        TextReader reader = new TextReader(resource);
        return reader.get();
    }
    
    /**
     * Read PDF document
     */
    public List<Document> readPdfDocument(Resource resource) {
        log.info("Reading PDF document");
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
        return reader.get();
    }
    
    /**
     * Read JSON document
     */
    public List<Document> readJsonDocument(Resource resource) {
        log.info("Reading JSON document");
        JsonReader reader = new JsonReader(resource);
        return reader.get();
    }
}

/**
 * Document Writer Service
 */
@Service
@Slf4j
class DocumentWriterService {
    
    private final VectorStore vectorStore;
    
    public DocumentWriterService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    
    /**
     * Write documents to vector store
     */
    public void writeToVectorStore(List<Document> documents) {
        log.info("Writing {} documents to vector store", documents.size());
        vectorStore.add(documents);
    }
    
    /**
     * Batch write with metadata
     */
    public void batchWrite(List<Document> documents, Map<String, Object> commonMetadata) {
        log.info("Batch writing {} documents", documents.size());
        
        documents.forEach(doc -> {
            Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
            metadata.putAll(commonMetadata);
            doc.setMetadata(metadata);
        });
        
        vectorStore.add(documents);
    }
}

/**
 * Document Transformer Service
 */
@Service
@Slf4j
class DocumentTransformerService {
    
    /**
     * Split documents into chunks using token splitter
     */
    public List<Document> splitByTokens(List<Document> documents, int chunkSize) {
        log.info("Splitting {} documents by tokens (chunk size: {})", documents.size(), chunkSize);
        
        TokenTextSplitter splitter = new TokenTextSplitter(chunkSize, 50, 5, 1000, true);
        return splitter.apply(documents);
    }
    
    /**
     * Format document content
     */
    public List<Document> formatContent(List<Document> documents) {
        log.info("Formatting {} documents", documents.size());
        
        ContentFormatTransformer transformer = new ContentFormatTransformer();
        return transformer.apply(documents);
    }
    
    /**
     * Chain multiple transformers
     */
    public List<Document> chainTransformers(List<Document> documents) {
        // Format content
        documents = formatContent(documents);
        
        // Split into chunks
        documents = splitByTokens(documents, 500);
        
        return documents;
    }
}

/**
 * Chat Memory Service
 */
@Service
@Slf4j
class ChatMemoryService {
    
    private final Map<String, List<Document>> sessionMemory = new HashMap<>();
    
    /**
     * Store conversation turn in memory
     */
    public void storeConversation(String sessionId, String userMessage, String assistantResponse) {
        log.info("Storing conversation for session: {}", sessionId);
        
        sessionMemory.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(
                new Document("user-" + System.currentTimeMillis(), userMessage, 
                        Map.of("role", "user", "timestamp", System.currentTimeMillis()))
        );
        
        sessionMemory.get(sessionId).add(
                new Document("assistant-" + System.currentTimeMillis(), assistantResponse,
                        Map.of("role", "assistant", "timestamp", System.currentTimeMillis()))
        );
    }
    
    /**
     * Get conversation history
     */
    public List<Document> getHistory(String sessionId) {
        return sessionMemory.getOrDefault(sessionId, Collections.emptyList());
    }
    
    /**
     * Get recent context window
     */
    public List<Document> getRecentContext(String sessionId, int turns) {
        List<Document> history = getHistory(sessionId);
        int start = Math.max(0, history.size() - (turns * 2)); // 2 messages per turn
        return history.subList(start, history.size());
    }
    
    /**
     * Clear session memory
     */
    public void clearSession(String sessionId) {
        sessionMemory.remove(sessionId);
        log.info("Cleared session: {}", sessionId);
    }
}

/**
 * RAG (Retrieval Augmented Generation) Service
 */
@Service
@Slf4j
class RAGService {
    
    private final VectorStore vectorStore;
    private final ChatMemoryService memoryService;
    
    public RAGService(VectorStore vectorStore, ChatMemoryService memoryService) {
        this.vectorStore = vectorStore;
        this.memoryService = memoryService;
    }
    
    /**
     * Basic RAG: Retrieve context and generate response
     */
    public RAGResponse performRAG(String query, int contextSize) {
        log.info("Performing RAG for query: {}", query);
        
        // 1. Retrieve relevant documents
        List<Document> context = vectorStore.similaritySearch(query);
        context = context.subList(0, Math.min(contextSize, context.size()));
        
        // 2. Build augmented prompt
        String augmentedPrompt = buildAugmentedPrompt(query, context);
        
        // 3. Generate response (would use ChatClient here)
        String answer = "Generated answer using context...";
        
        // 4. Extract sources
        List<String> sources = context.stream()
                .map(doc -> doc.getMetadata().getOrDefault("source", "unknown").toString())
                .distinct()
                .toList();
        
        RAGResponse response = new RAGResponse();
        response.setAnswer(answer);
        response.setSources(sources);
        response.setContext(context);
        
        return response;
    }
    
    /**
     * RAG with conversation memory
     */
    public RAGResponse performRAGWithMemory(String sessionId, String query, int contextSize) {
        log.info("Performing RAG with memory for session: {}", sessionId);
        
        // Get conversation history
        List<Document> history = memoryService.getRecentContext(sessionId, 3);
        
        // Retrieve documents
        List<Document> retrievedContext = vectorStore.similaritySearch(query);
        retrievedContext = retrievedContext.subList(0, Math.min(contextSize, retrievedContext.size()));
        
        // Combine history and retrieved context
        String augmentedPrompt = buildAugmentedPromptWithHistory(query, history, retrievedContext);
        
        // Generate response
        String answer = "Generated answer with memory...";
        
        // Store in memory
        memoryService.storeConversation(sessionId, query, answer);
        
        RAGResponse response = new RAGResponse();
        response.setAnswer(answer);
        response.setContext(retrievedContext);
        
        return response;
    }
    
    private String buildAugmentedPrompt(String query, List<Document> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Context:\n");
        context.forEach(doc -> prompt.append("- ").append(doc.getContent()).append("\n"));
        prompt.append("\nQuestion: ").append(query);
        prompt.append("\n\nAnswer based on the context above:");
        return prompt.toString();
    }
    
    private String buildAugmentedPromptWithHistory(String query, List<Document> history, List<Document> context) {
        StringBuilder prompt = new StringBuilder();
        
        if (!history.isEmpty()) {
            prompt.append("Conversation History:\n");
            history.forEach(doc -> prompt.append(doc.getMetadata().get("role"))
                    .append(": ").append(doc.getContent()).append("\n"));
            prompt.append("\n");
        }
        
        prompt.append("Context:\n");
        context.forEach(doc -> prompt.append("- ").append(doc.getContent()).append("\n"));
        prompt.append("\nQuestion: ").append(query);
        
        return prompt.toString();
    }
}

/**
 * Information Service
 */
@Service
class DocumentRAGInfoService {
    
    public String getPatternInfo() {
        return """
                Spring AI Document & RAG Patterns
                =================================
                
                1. Document Reader
                   - TextReader: Plain text files
                   - PagePdfDocumentReader: PDF documents
                   - JsonReader: JSON files
                   - Custom readers: Implement DocumentReader
                
                2. Document Writer
                   - Write to VectorStore
                   - Batch operations
                   - Metadata enrichment
                
                3. Document Transformer
                   - TokenTextSplitter: Split by tokens
                   - ContentFormatTransformer: Format content
                   - Custom transformers
                   - Transformer chains
                
                4. Chat Memory
                   - Session-based memory
                   - Conversation history
                   - Context windows
                   - Memory persistence
                
                5. RAG (Retrieval Augmented Generation)
                   - Semantic retrieval
                   - Context augmentation
                   - Memory integration
                   - Source attribution
                
                RAG Pipeline:
                1. Retrieve: Find relevant documents
                2. Augment: Add context to prompt
                3. Generate: LLM generates response
                4. Cite: Include sources
                
                Best Practices:
                1. Chunk documents appropriately (500-1000 tokens)
                2. Include overlap in chunks
                3. Enrich with metadata
                4. Use similarity thresholds
                5. Implement conversation memory
                6. Track token usage
                7. Cache frequent queries
                8. Monitor retrieval quality
                """;
    }
}

@RestController
@RequestMapping("/ai/rag")
@Slf4j
class RAGController {
    
    private final DocumentReaderService readerService;
    private final DocumentWriterService writerService;
    private final DocumentTransformerService transformerService;
    private final ChatMemoryService memoryService;
    private final RAGService ragService;
    private final DocumentRAGInfoService infoService;
    
    public RAGController(DocumentReaderService readerService,
                        DocumentWriterService writerService,
                        DocumentTransformerService transformerService,
                        ChatMemoryService memoryService,
                        RAGService ragService,
                        DocumentRAGInfoService infoService) {
        this.readerService = readerService;
        this.writerService = writerService;
        this.transformerService = transformerService;
        this.memoryService = memoryService;
        this.ragService = ragService;
        this.infoService = infoService;
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return infoService.getPatternInfo();
    }
    
    @PostMapping("/query")
    public RAGResponse performRAG(@RequestBody RAGRequest request) {
        return ragService.performRAG(request.getQuery(), request.getContextSize());
    }
    
    @PostMapping("/query/{sessionId}")
    public RAGResponse performRAGWithMemory(@PathVariable String sessionId,
                                           @RequestBody RAGRequest request) {
        return ragService.performRAGWithMemory(sessionId, request.getQuery(), request.getContextSize());
    }
    
    @GetMapping("/memory/{sessionId}")
    public List<Document> getMemory(@PathVariable String sessionId) {
        return memoryService.getHistory(sessionId);
    }
    
    @DeleteMapping("/memory/{sessionId}")
    public String clearMemory(@PathVariable String sessionId) {
        memoryService.clearSession(sessionId);
        return "Memory cleared for session: " + sessionId;
    }
}

@SpringBootApplication
public class DocumentRAGPattern {
    public static void main(String[] args) {
        SpringApplication.run(DocumentRAGPattern.class, args);
    }
}
