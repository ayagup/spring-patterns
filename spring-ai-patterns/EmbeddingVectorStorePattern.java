package com.example.springai;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingResponse;
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
 * Spring AI Embedding & Vector Store Patterns
 * 
 * Demonstrates:
 * - EmbeddingClient: Convert text to vector embeddings
 * - VectorStore: Store and search embeddings
 * - Semantic search
 * - Similarity search
 * - RAG (Retrieval Augmented Generation) foundation
 * 
 * @author Spring Patterns
 */

@Data
class DocumentData {
    private String id;
    private String content;
    private Map<String, Object> metadata;
}

@Data
class SearchQuery {
    private String query;
    private int topK = 5;
    private double similarityThreshold = 0.7;
}

@Service
@Slf4j
class EmbeddingService {
    
    private final EmbeddingClient embeddingClient;
    
    public EmbeddingService(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }
    
    /**
     * Generate embedding for single text
     */
    public List<Double> embedText(String text) {
        log.info("Generating embedding for text: {}", text.substring(0, Math.min(50, text.length())));
        
        EmbeddingResponse response = embeddingClient.embed(text);
        return response.getResult().getOutput();
    }
    
    /**
     * Generate embeddings for multiple texts
     */
    public List<List<Double>> embedTexts(List<String> texts) {
        log.info("Generating embeddings for {} texts", texts.size());
        
        List<EmbeddingResponse> responses = embeddingClient.embed(texts);
        return responses.stream()
                .map(response -> response.getResult().getOutput())
                .collect(Collectors.toList());
    }
    
    /**
     * Get embedding dimension
     */
    public int getEmbeddingDimension() {
        List<Double> embedding = embedText("test");
        return embedding.size();
    }
}

@Service
@Slf4j
class VectorStoreService {
    
    private final VectorStore vectorStore;
    private final EmbeddingClient embeddingClient;
    
    public VectorStoreService(VectorStore vectorStore, EmbeddingClient embeddingClient) {
        this.vectorStore = vectorStore;
        this.embeddingClient = embeddingClient;
    }
    
    /**
     * Add single document
     */
    public void addDocument(String id, String content, Map<String, Object> metadata) {
        log.info("Adding document: {}", id);
        
        Document document = new Document(id, content, metadata);
        vectorStore.add(List.of(document));
    }
    
    /**
     * Add multiple documents
     */
    public void addDocuments(List<DocumentData> documents) {
        log.info("Adding {} documents", documents.size());
        
        List<Document> docs = documents.stream()
                .map(d -> new Document(d.getId(), d.getContent(), d.getMetadata()))
                .toList();
        
        vectorStore.add(docs);
    }
    
    /**
     * Semantic similarity search
     */
    public List<Document> search(String query, int topK) {
        log.info("Searching for: {}", query);
        
        SearchRequest request = SearchRequest.query(query)
                .withTopK(topK);
        
        return vectorStore.similaritySearch(request);
    }
    
    /**
     * Search with similarity threshold
     */
    public List<Document> searchWithThreshold(String query, int topK, double threshold) {
        log.info("Searching with threshold {}: {}", threshold, query);
        
        SearchRequest request = SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(threshold);
        
        return vectorStore.similaritySearch(request);
    }
    
    /**
     * Search with metadata filter
     */
    public List<Document> searchWithFilter(String query, Map<String, Object> filter) {
        log.info("Searching with filter: {}", filter);
        
        SearchRequest request = SearchRequest.query(query)
                .withTopK(5)
                .withFilterExpression(filter.toString());
        
        return vectorStore.similaritySearch(request);
    }
    
    /**
     * Delete document
     */
    public void deleteDocument(String id) {
        log.info("Deleting document: {}", id);
        vectorStore.delete(List.of(id));
    }
}

@Service
class VectorStoreInfoService {
    
    public String getPatternInfo() {
        return """
                Spring AI Embedding & Vector Store Patterns
                ===========================================
                
                Embedding Client:
                - Converts text to vector embeddings
                - Numerical representation of semantic meaning
                - Foundation for semantic search
                - Dimensionality varies by model (768, 1536, etc.)
                
                Vector Store:
                - Stores document embeddings
                - Enables similarity search
                - Supports metadata filtering
                - Efficient nearest neighbor search
                
                Use Cases:
                1. Semantic Search
                   - Find similar documents
                   - Content recommendation
                   - Duplicate detection
                
                2. RAG (Retrieval Augmented Generation)
                   - Retrieve relevant context
                   - Augment LLM prompts
                   - Improve response accuracy
                
                3. Document Organization
                   - Clustering similar content
                   - Automatic categorization
                   - Content discovery
                
                Supported Vector Stores:
                - In-Memory: Simple vector store
                - Chroma: Open-source vector database
                - Pinecone: Managed vector database
                - Weaviate: Vector search engine
                - Milvus: Cloud-native vector database
                - Redis: With vector similarity search
                - PostgreSQL: With pgvector extension
                
                Best Practices:
                1. Choose appropriate embedding model
                2. Use metadata for filtering
                3. Set similarity thresholds appropriately
                4. Batch document additions
                5. Monitor vector store size
                6. Implement cleanup strategies
                7. Use consistent embedding models
                8. Test with representative data
                """;
    }
    
    public List<String> getVectorStoreTypes() {
        return List.of(
                "In-Memory: SimpleVectorStore",
                "Chroma: Chromadb integration",
                "Pinecone: Managed vector DB",
                "Weaviate: Vector search engine",
                "Milvus: Scalable vector DB",
                "Redis: With RediSearch",
                "PostgreSQL: With pgvector"
        );
    }
}

@RestController
@RequestMapping("/ai/vector")
@Slf4j
class VectorStoreController {
    
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final VectorStoreInfoService infoService;
    
    public VectorStoreController(EmbeddingService embeddingService,
                                VectorStoreService vectorStoreService,
                                VectorStoreInfoService infoService) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.infoService = infoService;
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return infoService.getPatternInfo();
    }
    
    @GetMapping("/stores")
    public List<String> getVectorStoreTypes() {
        return infoService.getVectorStoreTypes();
    }
    
    @PostMapping("/embed")
    public List<Double> embedText(@RequestBody String text) {
        return embeddingService.embedText(text);
    }
    
    @GetMapping("/dimension")
    public int getEmbeddingDimension() {
        return embeddingService.getEmbeddingDimension();
    }
    
    @PostMapping("/document")
    public String addDocument(@RequestBody DocumentData document) {
        vectorStoreService.addDocument(
                document.getId(),
                document.getContent(),
                document.getMetadata()
        );
        return "Document added: " + document.getId();
    }
    
    @PostMapping("/documents")
    public String addDocuments(@RequestBody List<DocumentData> documents) {
        vectorStoreService.addDocuments(documents);
        return "Added " + documents.size() + " documents";
    }
    
    @PostMapping("/search")
    public List<Document> search(@RequestBody SearchQuery query) {
        return vectorStoreService.search(query.getQuery(), query.getTopK());
    }
    
    @PostMapping("/search/threshold")
    public List<Document> searchWithThreshold(@RequestBody SearchQuery query) {
        return vectorStoreService.searchWithThreshold(
                query.getQuery(),
                query.getTopK(),
                query.getSimilarityThreshold()
        );
    }
    
    @DeleteMapping("/document/{id}")
    public String deleteDocument(@PathVariable String id) {
        vectorStoreService.deleteDocument(id);
        return "Document deleted: " + id;
    }
}

@SpringBootApplication
public class EmbeddingVectorStorePattern {
    public static void main(String[] args) {
        SpringApplication.run(EmbeddingVectorStorePattern.class, args);
    }
}
