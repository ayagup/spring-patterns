package com.example.springaipatterns;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Vector Store Pattern
 * 
 * Demonstrates the use of Spring AI's Vector Store for storing and retrieving
 * document embeddings with similarity search capabilities.
 * 
 * Supported Vector Stores:
 * - Pinecone
 * - Weaviate
 * - Milvus
 * - Chroma
 * - Simple Vector Store (in-memory)
 * 
 * Key Concepts:
 * - Document storage with embeddings
 * - Similarity search
 * - Metadata filtering
 * - Top-K retrieval
 * - Threshold-based filtering
 */
@SpringBootApplication
public class VectorStorePattern {

    public static void main(String[] args) {
        SpringApplication.run(VectorStorePattern.class, args);
    }

    @Configuration
    static class VectorStoreConfiguration {
        
        @Bean
        public VectorStore vectorStore() {
            // Using SimpleVectorStore for demonstration
            // In production, use Pinecone, Weaviate, Milvus, or Chroma
            return new SimpleVectorStore();
        }
    }

    @Service
    static class VectorStoreService {
        
        private final VectorStore vectorStore;
        
        public VectorStoreService(VectorStore vectorStore) {
            this.vectorStore = vectorStore;
        }
        
        /**
         * Add a single document to the vector store
         */
        public String addDocument(String content, Map<String, Object> metadata) {
            String id = UUID.randomUUID().toString();
            Document document = new Document(id, content, metadata);
            vectorStore.add(List.of(document));
            return id;
        }
        
        /**
         * Add multiple documents to the vector store
         */
        public List<String> addDocuments(List<DocumentRequest> requests) {
            List<Document> documents = requests.stream()
                .map(req -> {
                    String id = UUID.randomUUID().toString();
                    return new Document(id, req.content(), req.metadata());
                })
                .collect(Collectors.toList());
            
            vectorStore.add(documents);
            
            return documents.stream()
                .map(Document::getId)
                .collect(Collectors.toList());
        }
        
        /**
         * Perform similarity search
         */
        public List<Document> similaritySearch(String query, int topK) {
            SearchRequest request = SearchRequest.query(query)
                .withTopK(topK);
            return vectorStore.similaritySearch(request);
        }
        
        /**
         * Perform similarity search with threshold
         */
        public List<Document> similaritySearchWithThreshold(
                String query, int topK, double threshold) {
            SearchRequest request = SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(threshold);
            return vectorStore.similaritySearch(request);
        }
        
        /**
         * Perform similarity search with metadata filter
         */
        public List<Document> similaritySearchWithFilter(
                String query, int topK, Map<String, Object> filterExpression) {
            SearchRequest request = SearchRequest.query(query)
                .withTopK(topK)
                .withFilterExpression(filterExpression);
            return vectorStore.similaritySearch(request);
        }
        
        /**
         * Delete documents by IDs
         */
        public void deleteDocuments(List<String> ids) {
            vectorStore.delete(ids);
        }
        
        /**
         * Get document by ID
         */
        public Document getDocument(String id) {
            List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query("").withTopK(1)
            );
            return results.stream()
                .filter(doc -> doc.getId().equals(id))
                .findFirst()
                .orElse(null);
        }
    }

    @RestController
    @RequestMapping("/api/vector-store")
    static class VectorStoreController {
        
        private final VectorStoreService vectorStoreService;
        
        public VectorStoreController(VectorStoreService vectorStoreService) {
            this.vectorStoreService = vectorStoreService;
        }
        
        @PostMapping("/add")
        public AddDocumentResponse addDocument(@RequestBody AddDocumentRequest request) {
            String id = vectorStoreService.addDocument(
                request.content(),
                request.metadata()
            );
            return new AddDocumentResponse(id, "Document added successfully");
        }
        
        @PostMapping("/add-batch")
        public AddBatchResponse addDocuments(@RequestBody AddBatchRequest request) {
            List<String> ids = vectorStoreService.addDocuments(request.documents());
            return new AddBatchResponse(ids, ids.size());
        }
        
        @PostMapping("/search")
        public SearchResponse search(@RequestBody SearchQueryRequest request) {
            List<Document> results = vectorStoreService.similaritySearch(
                request.query(),
                request.topK()
            );
            
            List<SearchResult> searchResults = results.stream()
                .map(doc -> new SearchResult(
                    doc.getId(),
                    doc.getContent(),
                    doc.getMetadata()
                ))
                .collect(Collectors.toList());
            
            return new SearchResponse(searchResults, results.size());
        }
        
        @PostMapping("/search-threshold")
        public SearchResponse searchWithThreshold(@RequestBody ThresholdSearchRequest request) {
            List<Document> results = vectorStoreService.similaritySearchWithThreshold(
                request.query(),
                request.topK(),
                request.threshold()
            );
            
            List<SearchResult> searchResults = results.stream()
                .map(doc -> new SearchResult(
                    doc.getId(),
                    doc.getContent(),
                    doc.getMetadata()
                ))
                .collect(Collectors.toList());
            
            return new SearchResponse(searchResults, results.size());
        }
        
        @PostMapping("/search-filter")
        public SearchResponse searchWithFilter(@RequestBody FilterSearchRequest request) {
            List<Document> results = vectorStoreService.similaritySearchWithFilter(
                request.query(),
                request.topK(),
                request.filter()
            );
            
            List<SearchResult> searchResults = results.stream()
                .map(doc -> new SearchResult(
                    doc.getId(),
                    doc.getContent(),
                    doc.getMetadata()
                ))
                .collect(Collectors.toList());
            
            return new SearchResponse(searchResults, results.size());
        }
        
        @DeleteMapping("/delete")
        public DeleteResponse deleteDocuments(@RequestBody DeleteRequest request) {
            vectorStoreService.deleteDocuments(request.ids());
            return new DeleteResponse(request.ids().size(), "Documents deleted successfully");
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Vector Store Pattern",
                "description", "Document storage and similarity search using vector embeddings",
                "vectorStores", List.of("Pinecone", "Weaviate", "Milvus", "Chroma", "Simple"),
                "features", List.of(
                    "Document storage",
                    "Similarity search",
                    "Top-K retrieval",
                    "Threshold filtering",
                    "Metadata filtering",
                    "Batch operations"
                ),
                "endpoints", List.of(
                    "POST /api/vector-store/add",
                    "POST /api/vector-store/add-batch",
                    "POST /api/vector-store/search",
                    "POST /api/vector-store/search-threshold",
                    "POST /api/vector-store/search-filter",
                    "DELETE /api/vector-store/delete",
                    "GET /api/vector-store/info"
                )
            );
        }
    }

    // DTOs
    record DocumentRequest(String content, Map<String, Object> metadata) {}
    record AddDocumentRequest(String content, Map<String, Object> metadata) {}
    record AddDocumentResponse(String id, String message) {}
    record AddBatchRequest(List<DocumentRequest> documents) {}
    record AddBatchResponse(List<String> ids, int count) {}
    record SearchQueryRequest(String query, int topK) {}
    record ThresholdSearchRequest(String query, int topK, double threshold) {}
    record FilterSearchRequest(String query, int topK, Map<String, Object> filter) {}
    record SearchResult(String id, String content, Map<String, Object> metadata) {}
    record SearchResponse(List<SearchResult> results, int count) {}
    record DeleteRequest(List<String> ids) {}
    record DeleteResponse(int count, String message) {}
}
