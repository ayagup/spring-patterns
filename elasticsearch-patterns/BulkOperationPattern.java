package com.example.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bulk Operation Pattern
 * 
 * Demonstrates efficient bulk operations in Elasticsearch for
 * high-throughput data indexing, updating, and deletion.
 * 
 * Key concepts:
 * - Bulk indexing operations
 * - Bulk update operations
 * - Bulk delete operations
 * - Mixed bulk operations
 * - Error handling in bulk
 * - Performance optimization
 * 
 * Use cases:
 * - High-volume data ingestion
 * - Batch processing
 * - Data migration
 * - ETL pipelines
 * - Real-time indexing
 */
@SpringBootApplication
public class BulkOperationPattern {

    public static void main(String[] args) {
        SpringApplication.run(BulkOperationPattern.class, args);
    }
}

/**
 * Document for bulk operations
 */
record Document(
    String id,
    String title,
    String content,
    String author,
    String category,
    List<String> tags,
    Integer views,
    LocalDateTime createdDate,
    LocalDateTime updatedDate
) {}

/**
 * Bulk operation result
 */
record BulkResult(
    int total,
    int successful,
    int failed,
    List<String> errors
) {}

/**
 * Service demonstrating bulk operations
 */
@Service
class BulkOperationService {
    
    private final ElasticsearchOperations elasticsearchOperations;
    private static final String INDEX_NAME = "documents";
    
    public BulkOperationService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
    
    /**
     * Bulk index documents
     */
    public BulkResult bulkIndexDocuments(List<Document> documents) {
        List<IndexQuery> queries = documents.stream()
            .map(document -> new IndexQueryBuilder()
                .withId(document.id())
                .withObject(document)
                .build())
            .collect(Collectors.toList());
        
        try {
            List<String> ids = elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of(INDEX_NAME));
            return new BulkResult(
                documents.size(),
                ids.size(),
                documents.size() - ids.size(),
                new ArrayList<>()
            );
        } catch (Exception e) {
            return new BulkResult(
                documents.size(),
                0,
                documents.size(),
                List.of(e.getMessage())
            );
        }
    }
    
    /**
     * Bulk update documents
     */
    public BulkResult bulkUpdateDocuments(List<Document> documents) {
        List<UpdateQuery> queries = documents.stream()
            .map(document -> UpdateQuery.builder(document.id())
                .withDocument(
                    org.springframework.data.elasticsearch.core.document.Document.create()
                )
                .build())
            .collect(Collectors.toList());
        
        try {
            elasticsearchOperations.bulkUpdate(queries, IndexCoordinates.of(INDEX_NAME));
            return new BulkResult(
                documents.size(),
                documents.size(),
                0,
                new ArrayList<>()
            );
        } catch (Exception e) {
            return new BulkResult(
                documents.size(),
                0,
                documents.size(),
                List.of(e.getMessage())
            );
        }
    }
    
    /**
     * Bulk delete documents by IDs
     */
    public BulkResult bulkDeleteDocuments(List<String> ids) {
        try {
            for (String id : ids) {
                elasticsearchOperations.delete(id, IndexCoordinates.of(INDEX_NAME));
            }
            return new BulkResult(
                ids.size(),
                ids.size(),
                0,
                new ArrayList<>()
            );
        } catch (Exception e) {
            return new BulkResult(
                ids.size(),
                0,
                ids.size(),
                List.of(e.getMessage())
            );
        }
    }
    
    /**
     * Bulk delete by query
     */
    public BulkResult bulkDeleteByQuery(String category) {
        try {
            Criteria criteria = new Criteria("category").is(category);
            Query query = new CriteriaQuery(criteria);
            
            elasticsearchOperations.delete(query, Document.class, IndexCoordinates.of(INDEX_NAME));
            return new BulkResult(
                1,
                1,
                0,
                new ArrayList<>()
            );
        } catch (Exception e) {
            return new BulkResult(
                1,
                0,
                1,
                List.of(e.getMessage())
            );
        }
    }
    
    /**
     * Bulk update views count (increment)
     */
    public BulkResult bulkIncrementViews(List<String> documentIds, Integer increment) {
        List<Document> documents = new ArrayList<>();
        
        for (String id : documentIds) {
            Document doc = elasticsearchOperations.get(id, Document.class, IndexCoordinates.of(INDEX_NAME));
            if (doc != null) {
                Document updated = new Document(
                    doc.id(),
                    doc.title(),
                    doc.content(),
                    doc.author(),
                    doc.category(),
                    doc.tags(),
                    doc.views() + increment,
                    doc.createdDate(),
                    LocalDateTime.now()
                );
                documents.add(updated);
            }
        }
        
        return bulkUpdateDocuments(documents);
    }
    
    /**
     * Bulk update category
     */
    public BulkResult bulkUpdateCategory(List<String> documentIds, String newCategory) {
        List<Document> documents = new ArrayList<>();
        
        for (String id : documentIds) {
            Document doc = elasticsearchOperations.get(id, Document.class, IndexCoordinates.of(INDEX_NAME));
            if (doc != null) {
                Document updated = new Document(
                    doc.id(),
                    doc.title(),
                    doc.content(),
                    doc.author(),
                    newCategory,
                    doc.tags(),
                    doc.views(),
                    doc.createdDate(),
                    LocalDateTime.now()
                );
                documents.add(updated);
            }
        }
        
        return bulkUpdateDocuments(documents);
    }
    
    /**
     * Bulk add tag to documents
     */
    public BulkResult bulkAddTag(List<String> documentIds, String tag) {
        List<Document> documents = new ArrayList<>();
        
        for (String id : documentIds) {
            Document doc = elasticsearchOperations.get(id, Document.class, IndexCoordinates.of(INDEX_NAME));
            if (doc != null) {
                List<String> newTags = new ArrayList<>(doc.tags());
                if (!newTags.contains(tag)) {
                    newTags.add(tag);
                }
                
                Document updated = new Document(
                    doc.id(),
                    doc.title(),
                    doc.content(),
                    doc.author(),
                    doc.category(),
                    newTags,
                    doc.views(),
                    doc.createdDate(),
                    LocalDateTime.now()
                );
                documents.add(updated);
            }
        }
        
        return bulkUpdateDocuments(documents);
    }
    
    /**
     * Chunked bulk indexing for very large datasets
     */
    public BulkResult chunkedBulkIndex(List<Document> documents, int chunkSize) {
        int total = documents.size();
        int successful = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < documents.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, documents.size());
            List<Document> chunk = documents.subList(i, end);
            
            BulkResult result = bulkIndexDocuments(chunk);
            successful += result.successful();
            failed += result.failed();
            errors.addAll(result.errors());
        }
        
        return new BulkResult(total, successful, failed, errors);
    }
    
    /**
     * Get total document count
     */
    public long count() {
        Query query = Query.findAll();
        return elasticsearchOperations.count(query, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Get document by ID
     */
    public Document getDocument(String id) {
        return elasticsearchOperations.get(id, Document.class, IndexCoordinates.of(INDEX_NAME));
    }
}

/**
 * REST controller for bulk operations
 */
@RestController
@RequestMapping("/api/documents")
class BulkOperationController {
    
    private final BulkOperationService bulkOperationService;
    
    public BulkOperationController(BulkOperationService bulkOperationService) {
        this.bulkOperationService = bulkOperationService;
    }
    
    @PostMapping("/bulk/index")
    public ResponseEntity<BulkResult> bulkIndexDocuments(@RequestBody List<Document> documents) {
        return ResponseEntity.ok(bulkOperationService.bulkIndexDocuments(documents));
    }
    
    @PutMapping("/bulk/update")
    public ResponseEntity<BulkResult> bulkUpdateDocuments(@RequestBody List<Document> documents) {
        return ResponseEntity.ok(bulkOperationService.bulkUpdateDocuments(documents));
    }
    
    @DeleteMapping("/bulk/delete")
    public ResponseEntity<BulkResult> bulkDeleteDocuments(@RequestBody List<String> ids) {
        return ResponseEntity.ok(bulkOperationService.bulkDeleteDocuments(ids));
    }
    
    @DeleteMapping("/bulk/delete-by-category/{category}")
    public ResponseEntity<BulkResult> bulkDeleteByQuery(@PathVariable String category) {
        return ResponseEntity.ok(bulkOperationService.bulkDeleteByQuery(category));
    }
    
    @PatchMapping("/bulk/increment-views")
    public ResponseEntity<BulkResult> bulkIncrementViews(
            @RequestBody List<String> documentIds,
            @RequestParam Integer increment) {
        return ResponseEntity.ok(bulkOperationService.bulkIncrementViews(documentIds, increment));
    }
    
    @PatchMapping("/bulk/update-category")
    public ResponseEntity<BulkResult> bulkUpdateCategory(
            @RequestBody List<String> documentIds,
            @RequestParam String category) {
        return ResponseEntity.ok(bulkOperationService.bulkUpdateCategory(documentIds, category));
    }
    
    @PatchMapping("/bulk/add-tag")
    public ResponseEntity<BulkResult> bulkAddTag(
            @RequestBody List<String> documentIds,
            @RequestParam String tag) {
        return ResponseEntity.ok(bulkOperationService.bulkAddTag(documentIds, tag));
    }
    
    @PostMapping("/bulk/chunked-index")
    public ResponseEntity<BulkResult> chunkedBulkIndex(
            @RequestBody List<Document> documents,
            @RequestParam(defaultValue = "1000") int chunkSize) {
        return ResponseEntity.ok(bulkOperationService.chunkedBulkIndex(documents, chunkSize));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable String id) {
        Document document = bulkOperationService.getDocument(id);
        return document != null ? ResponseEntity.ok(document) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(bulkOperationService.count());
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Bulk Operation Pattern
            
            This pattern demonstrates efficient bulk operations for high-throughput
            Elasticsearch operations.
            
            Operations:
            - Bulk indexing: Index multiple documents in single request
            - Bulk update: Update multiple documents efficiently
            - Bulk delete: Delete multiple documents by IDs or query
            - Bulk field updates: Update specific fields across documents
            - Chunked bulk: Process very large datasets in chunks
            
            Features:
            - High-throughput data ingestion
            - Batch processing support
            - Error handling and reporting
            - Chunked processing for large datasets
            - Partial success handling
            - Performance optimization
            
            Benefits:
            - Reduced network overhead
            - Improved indexing speed (10-100x)
            - Efficient resource utilization
            - Better throughput for batch operations
            
            Best Practices:
            - Use bulk operations for >1 document
            - Keep bulk size 1,000-5,000 documents
            - Chunk very large datasets
            - Handle partial failures
            - Monitor bulk operation metrics
            
            Endpoints:
            - POST /api/documents/bulk/index - Bulk index documents
            - PUT /api/documents/bulk/update - Bulk update documents
            - DELETE /api/documents/bulk/delete - Bulk delete by IDs
            - DELETE /api/documents/bulk/delete-by-category/{category} - Delete by query
            - PATCH /api/documents/bulk/increment-views - Increment views
            - PATCH /api/documents/bulk/update-category - Update category
            - PATCH /api/documents/bulk/add-tag - Add tag
            - POST /api/documents/bulk/chunked-index?chunkSize= - Chunked bulk index
            - GET /api/documents/{id} - Get document
            - GET /api/documents/count - Count documents
            """);
    }
}
