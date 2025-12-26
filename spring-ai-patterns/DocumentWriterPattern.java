package com.example.springaipatterns;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Document Writer Pattern
 * 
 * Demonstrates the use of Spring AI's Document Writer for persisting
 * documents to vector stores and other storage backends.
 * 
 * Key Concepts:
 * - Document persistence to vector stores
 * - Batch document writing
 * - Document update operations
 * - Error handling and validation
 * - Storage backend abstraction
 */
@SpringBootApplication
public class DocumentWriterPattern {

    public static void main(String[] args) {
        SpringApplication.run(DocumentWriterPattern.class, args);
    }

    @Service
    static class DocumentWriterService {
        
        private final VectorStore vectorStore;
        
        public DocumentWriterService(VectorStore vectorStore) {
            this.vectorStore = vectorStore;
        }
        
        /**
         * Write a single document to vector store
         */
        public void writeDocument(Document document) {
            vectorStore.add(List.of(document));
        }
        
        /**
         * Write multiple documents to vector store
         */
        public void writeDocuments(List<Document> documents) {
            vectorStore.add(documents);
        }
        
        /**
         * Write documents in batches
         */
        public void writeBatch(List<Document> documents, int batchSize) {
            for (int i = 0; i < documents.size(); i += batchSize) {
                int end = Math.min(i + batchSize, documents.size());
                List<Document> batch = documents.subList(i, end);
                vectorStore.add(batch);
            }
        }
        
        /**
         * Update existing document
         */
        public void updateDocument(Document document) {
            // Delete old version and add new version
            vectorStore.delete(List.of(document.getId()));
            vectorStore.add(List.of(document));
        }
        
        /**
         * Write with validation
         */
        public WriteResult writeWithValidation(List<Document> documents) {
            List<Document> validDocuments = documents.stream()
                .filter(doc -> doc.getContent() != null && !doc.getContent().trim().isEmpty())
                .collect(Collectors.toList());
            
            int skipped = documents.size() - validDocuments.size();
            
            if (!validDocuments.isEmpty()) {
                vectorStore.add(validDocuments);
            }
            
            return new WriteResult(validDocuments.size(), skipped, "success");
        }
    }

    @RestController
    @RequestMapping("/api/document-writer")
    static class DocumentWriterController {
        
        private final DocumentWriterService documentWriterService;
        
        public DocumentWriterController(DocumentWriterService documentWriterService) {
            this.documentWriterService = documentWriterService;
        }
        
        @PostMapping("/write")
        public WriteResponse write(@RequestBody WriteRequest request) {
            try {
                Document doc = new Document(request.id(), request.content(), request.metadata());
                documentWriterService.writeDocument(doc);
                return new WriteResponse(1, "success");
            } catch (Exception e) {
                return new WriteResponse(0, "error: " + e.getMessage());
            }
        }
        
        @PostMapping("/write-batch")
        public WriteResponse writeBatch(@RequestBody WriteBatchRequest request) {
            try {
                List<Document> documents = request.documents().stream()
                    .map(req -> new Document(req.id(), req.content(), req.metadata()))
                    .collect(Collectors.toList());
                
                documentWriterService.writeDocuments(documents);
                return new WriteResponse(documents.size(), "success");
            } catch (Exception e) {
                return new WriteResponse(0, "error: " + e.getMessage());
            }
        }
        
        @PostMapping("/write-validated")
        public WriteResult writeValidated(@RequestBody WriteBatchRequest request) {
            List<Document> documents = request.documents().stream()
                .map(req -> new Document(req.id(), req.content(), req.metadata()))
                .collect(Collectors.toList());
            
            return documentWriterService.writeWithValidation(documents);
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Document Writer Pattern",
                "description", "Persist documents to vector stores",
                "features", List.of(
                    "Single document writing",
                    "Batch writing",
                    "Document updates",
                    "Validation",
                    "Error handling"
                ),
                "endpoints", List.of(
                    "POST /api/document-writer/write",
                    "POST /api/document-writer/write-batch",
                    "POST /api/document-writer/write-validated",
                    "GET /api/document-writer/info"
                )
            );
        }
    }

    // DTOs
    record WriteRequest(String id, String content, Map<String, Object> metadata) {}
    record DocumentRequest(String id, String content, Map<String, Object> metadata) {}
    record WriteBatchRequest(List<DocumentRequest> documents) {}
    record WriteResponse(int count, String status) {}
    record WriteResult(int written, int skipped, String status) {}
}
