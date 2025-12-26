package com.example.springaipatterns;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Document Transformer Pattern
 * 
 * Demonstrates the use of Spring AI's Document Transformers for processing
 * and transforming documents (chunking, splitting, enriching).
 * 
 * Key Concepts:
 * - Text chunking and splitting
 * - Token-based splitting
 * - Metadata enrichment
 * - Document preprocessing
 * - Content transformation
 */
@SpringBootApplication
public class DocumentTransformerPattern {

    public static void main(String[] args) {
        SpringApplication.run(DocumentTransformerPattern.class, args);
    }

    @Service
    static class DocumentTransformerService {
        
        /**
         * Split document into chunks using token-based splitter
         */
        public List<Document> splitByTokens(Document document, int chunkSize, int overlapSize) {
            TextSplitter splitter = new TokenTextSplitter(chunkSize, overlapSize);
            return splitter.apply(List.of(document));
        }
        
        /**
         * Split multiple documents
         */
        public List<Document> splitDocuments(List<Document> documents, int chunkSize, int overlapSize) {
            TextSplitter splitter = new TokenTextSplitter(chunkSize, overlapSize);
            return splitter.apply(documents);
        }
        
        /**
         * Enrich document with metadata
         */
        public Document enrichMetadata(Document document, Map<String, Object> additionalMetadata) {
            Map<String, Object> enrichedMetadata = new java.util.HashMap<>(document.getMetadata());
            enrichedMetadata.putAll(additionalMetadata);
            return new Document(document.getId(), document.getContent(), enrichedMetadata);
        }
        
        /**
         * Transform document content
         */
        public Document transformContent(Document document, ContentTransformation transformation) {
            String transformedContent = switch (transformation) {
                case LOWERCASE -> document.getContent().toLowerCase();
                case UPPERCASE -> document.getContent().toUpperCase();
                case TRIM -> document.getContent().trim();
                case REMOVE_WHITESPACE -> document.getContent().replaceAll("\\s+", " ");
            };
            
            return new Document(document.getId(), transformedContent, document.getMetadata());
        }
        
        /**
         * Filter documents by content length
         */
        public List<Document> filterByLength(List<Document> documents, int minLength, int maxLength) {
            return documents.stream()
                .filter(doc -> {
                    int length = doc.getContent().length();
                    return length >= minLength && length <= maxLength;
                })
                .collect(Collectors.toList());
        }
    }

    @RestController
    @RequestMapping("/api/document-transformer")
    static class DocumentTransformerController {
        
        private final DocumentTransformerService transformerService;
        
        public DocumentTransformerController(DocumentTransformerService transformerService) {
            this.transformerService = transformerService;
        }
        
        @PostMapping("/split")
        public SplitResponse split(@RequestBody SplitRequest request) {
            Document doc = new Document(request.id(), request.content(), request.metadata());
            List<Document> chunks = transformerService.splitByTokens(
                doc,
                request.chunkSize(),
                request.overlapSize()
            );
            
            List<DocumentInfo> docInfos = chunks.stream()
                .map(d -> new DocumentInfo(d.getId(), d.getContent(), d.getMetadata()))
                .collect(Collectors.toList());
            
            return new SplitResponse(docInfos, chunks.size());
        }
        
        @PostMapping("/enrich")
        public DocumentInfo enrich(@RequestBody EnrichRequest request) {
            Document doc = new Document(request.id(), request.content(), request.metadata());
            Document enriched = transformerService.enrichMetadata(doc, request.additionalMetadata());
            return new DocumentInfo(enriched.getId(), enriched.getContent(), enriched.getMetadata());
        }
        
        @PostMapping("/transform")
        public DocumentInfo transform(@RequestBody TransformRequest request) {
            Document doc = new Document(request.id(), request.content(), request.metadata());
            Document transformed = transformerService.transformContent(doc, request.transformation());
            return new DocumentInfo(transformed.getId(), transformed.getContent(), transformed.getMetadata());
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Document Transformer Pattern",
                "description", "Transform and process documents (chunking, splitting, enriching)",
                "features", List.of(
                    "Token-based splitting",
                    "Metadata enrichment",
                    "Content transformation",
                    "Document filtering",
                    "Batch processing"
                ),
                "endpoints", List.of(
                    "POST /api/document-transformer/split",
                    "POST /api/document-transformer/enrich",
                    "POST /api/document-transformer/transform",
                    "GET /api/document-transformer/info"
                )
            );
        }
    }

    enum ContentTransformation {
        LOWERCASE, UPPERCASE, TRIM, REMOVE_WHITESPACE
    }

    // DTOs
    record SplitRequest(String id, String content, Map<String, Object> metadata, int chunkSize, int overlapSize) {}
    record SplitResponse(List<DocumentInfo> chunks, int count) {}
    record EnrichRequest(String id, String content, Map<String, Object> metadata, Map<String, Object> additionalMetadata) {}
    record TransformRequest(String id, String content, Map<String, Object> metadata, ContentTransformation transformation) {}
    record DocumentInfo(String id, String content, Map<String, Object> metadata) {}
}
