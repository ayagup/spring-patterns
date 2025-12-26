package com.example.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Index Template Pattern
 * 
 * Demonstrates Elasticsearch index management including
 * index creation, mapping definition, settings configuration,
 * and index templates.
 * 
 * Key concepts:
 * - Index creation and deletion
 * - Mapping definition
 * - Index settings (shards, replicas, refresh_interval)
 * - Index templates for automatic configuration
 * - Analyzers and tokenizers
 * - Index aliases
 * 
 * Use cases:
 * - Index lifecycle management
 * - Dynamic index creation
 * - Schema management
 * - Multi-tenancy
 * - Time-series indices
 */
@SpringBootApplication
public class IndexTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(IndexTemplatePattern.class, args);
    }
}

/**
 * Index configuration
 */
record IndexConfig(
    String indexName,
    int numberOfShards,
    int numberOfReplicas,
    String refreshInterval
) {}

/**
 * Mapping configuration
 */
record MappingConfig(
    String field,
    String type,
    boolean analyzed,
    String analyzer
) {}

/**
 * Service for index template operations
 */
@Service
class IndexTemplateService {
    
    private final ElasticsearchOperations elasticsearchOperations;
    
    public IndexTemplateService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
    
    /**
     * Create index with default settings
     */
    public boolean createIndex(String indexName) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        
        if (!indexOps.exists()) {
            return indexOps.create();
        }
        return false;
    }
    
    /**
     * Create index with custom settings
     */
    public boolean createIndexWithSettings(IndexConfig config) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(config.indexName()));
        
        if (!indexOps.exists()) {
            Map<String, Object> settings = Map.of(
                "number_of_shards", config.numberOfShards(),
                "number_of_replicas", config.numberOfReplicas(),
                "refresh_interval", config.refreshInterval()
            );
            
            org.springframework.data.elasticsearch.core.document.Document settingsDoc = 
                org.springframework.data.elasticsearch.core.document.Document.from(settings);
            
            return indexOps.create(settingsDoc);
        }
        return false;
    }
    
    /**
     * Delete index
     */
    public boolean deleteIndex(String indexName) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        
        if (indexOps.exists()) {
            return indexOps.delete();
        }
        return false;
    }
    
    /**
     * Check if index exists
     */
    public boolean indexExists(String indexName) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        return indexOps.exists();
    }
    
    /**
     * Get index settings
     */
    public Map<String, Object> getIndexSettings(String indexName) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        return indexOps.getSettings();
    }
    
    /**
     * Update index settings (requires index to be closed for some settings)
     */
    public boolean updateIndexSettings(String indexName, Map<String, Object> settings) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        
        org.springframework.data.elasticsearch.core.document.Document settingsDoc = 
            org.springframework.data.elasticsearch.core.document.Document.from(settings);
        
        return indexOps.putSettings(settingsDoc);
    }
    
    /**
     * Create index with custom mapping
     * This example shows a mapping for a product index
     */
    public boolean createProductIndex() {
        String indexName = "products";
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        
        if (!indexOps.exists()) {
            // Create index with settings
            Map<String, Object> settings = Map.of(
                "number_of_shards", 3,
                "number_of_replicas", 2,
                "refresh_interval", "1s",
                "analysis", Map.of(
                    "analyzer", Map.of(
                        "custom_analyzer", Map.of(
                            "type", "custom",
                            "tokenizer", "standard",
                            "filter", new String[]{"lowercase", "stop", "snowball"}
                        )
                    )
                )
            );
            
            org.springframework.data.elasticsearch.core.document.Document settingsDoc = 
                org.springframework.data.elasticsearch.core.document.Document.from(settings);
            
            indexOps.create(settingsDoc);
            
            // Define mapping
            Map<String, Object> mapping = Map.of(
                "properties", Map.of(
                    "name", Map.of("type", "text", "analyzer", "custom_analyzer"),
                    "description", Map.of("type", "text"),
                    "price", Map.of("type", "double"),
                    "category", Map.of("type", "keyword"),
                    "tags", Map.of("type", "keyword"),
                    "stock", Map.of("type", "integer"),
                    "available", Map.of("type", "boolean"),
                    "created_date", Map.of("type", "date")
                )
            );
            
            org.springframework.data.elasticsearch.core.document.Document mappingDoc = 
                org.springframework.data.elasticsearch.core.document.Document.from(mapping);
            
            return indexOps.putMapping(mappingDoc);
        }
        return false;
    }
    
    /**
     * Create time-series index with template pattern
     * For log indices like: logs-2024-01, logs-2024-02, etc.
     */
    public boolean createTimeSeriesIndex(String baseIndexName, String timeSuffix) {
        String indexName = baseIndexName + "-" + timeSuffix;
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        
        if (!indexOps.exists()) {
            Map<String, Object> settings = Map.of(
                "number_of_shards", 1,
                "number_of_replicas", 1,
                "refresh_interval", "5s"
            );
            
            org.springframework.data.elasticsearch.core.document.Document settingsDoc = 
                org.springframework.data.elasticsearch.core.document.Document.from(settings);
            
            indexOps.create(settingsDoc);
            
            // Time-series optimized mapping
            Map<String, Object> mapping = Map.of(
                "properties", Map.of(
                    "timestamp", Map.of("type", "date"),
                    "level", Map.of("type", "keyword"),
                    "message", Map.of("type", "text"),
                    "application", Map.of("type", "keyword"),
                    "logger", Map.of("type", "keyword"),
                    "thread", Map.of("type", "keyword")
                )
            );
            
            org.springframework.data.elasticsearch.core.document.Document mappingDoc = 
                org.springframework.data.elasticsearch.core.document.Document.from(mapping);
            
            return indexOps.putMapping(mappingDoc);
        }
        return false;
    }
    
    /**
     * Refresh index (make recent changes searchable)
     */
    public void refreshIndex(String indexName) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        indexOps.refresh();
    }
    
    /**
     * Get mapping for index
     */
    public Map<String, Object> getMapping(String indexName) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        return indexOps.getMapping();
    }
    
    /**
     * Create multi-tenant index
     */
    public boolean createTenantIndex(String tenantId) {
        String indexName = "tenant-" + tenantId;
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        
        if (!indexOps.exists()) {
            Map<String, Object> settings = Map.of(
                "number_of_shards", 1,
                "number_of_replicas", 1
            );
            
            org.springframework.data.elasticsearch.core.document.Document settingsDoc = 
                org.springframework.data.elasticsearch.core.document.Document.from(settings);
            
            return indexOps.create(settingsDoc);
        }
        return false;
    }
}

/**
 * REST controller for index template operations
 */
@RestController
@RequestMapping("/api/indices")
class IndexTemplateController {
    
    private final IndexTemplateService indexTemplateService;
    
    public IndexTemplateController(IndexTemplateService indexTemplateService) {
        this.indexTemplateService = indexTemplateService;
    }
    
    @PostMapping("/create/{indexName}")
    public ResponseEntity<Boolean> createIndex(@PathVariable String indexName) {
        return ResponseEntity.ok(indexTemplateService.createIndex(indexName));
    }
    
    @PostMapping("/create-with-settings")
    public ResponseEntity<Boolean> createIndexWithSettings(@RequestBody IndexConfig config) {
        return ResponseEntity.ok(indexTemplateService.createIndexWithSettings(config));
    }
    
    @DeleteMapping("/{indexName}")
    public ResponseEntity<Boolean> deleteIndex(@PathVariable String indexName) {
        return ResponseEntity.ok(indexTemplateService.deleteIndex(indexName));
    }
    
    @GetMapping("/{indexName}/exists")
    public ResponseEntity<Boolean> indexExists(@PathVariable String indexName) {
        return ResponseEntity.ok(indexTemplateService.indexExists(indexName));
    }
    
    @GetMapping("/{indexName}/settings")
    public ResponseEntity<Map<String, Object>> getIndexSettings(@PathVariable String indexName) {
        return ResponseEntity.ok(indexTemplateService.getIndexSettings(indexName));
    }
    
    @PutMapping("/{indexName}/settings")
    public ResponseEntity<Boolean> updateIndexSettings(
            @PathVariable String indexName,
            @RequestBody Map<String, Object> settings) {
        return ResponseEntity.ok(indexTemplateService.updateIndexSettings(indexName, settings));
    }
    
    @PostMapping("/create-product-index")
    public ResponseEntity<Boolean> createProductIndex() {
        return ResponseEntity.ok(indexTemplateService.createProductIndex());
    }
    
    @PostMapping("/create-timeseries/{baseIndexName}/{timeSuffix}")
    public ResponseEntity<Boolean> createTimeSeriesIndex(
            @PathVariable String baseIndexName,
            @PathVariable String timeSuffix) {
        return ResponseEntity.ok(indexTemplateService.createTimeSeriesIndex(baseIndexName, timeSuffix));
    }
    
    @PostMapping("/{indexName}/refresh")
    public ResponseEntity<Void> refreshIndex(@PathVariable String indexName) {
        indexTemplateService.refreshIndex(indexName);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{indexName}/mapping")
    public ResponseEntity<Map<String, Object>> getMapping(@PathVariable String indexName) {
        return ResponseEntity.ok(indexTemplateService.getMapping(indexName));
    }
    
    @PostMapping("/create-tenant/{tenantId}")
    public ResponseEntity<Boolean> createTenantIndex(@PathVariable String tenantId) {
        return ResponseEntity.ok(indexTemplateService.createTenantIndex(tenantId));
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Index Template Pattern
            
            This pattern demonstrates Elasticsearch index management including
            creation, mapping, settings, and templates.
            
            Index Management:
            - Index creation and deletion
            - Custom index settings (shards, replicas, refresh_interval)
            - Dynamic mapping configuration
            - Index templates for automatic configuration
            - Time-series index patterns
            - Multi-tenant index strategies
            
            Key Settings:
            - number_of_shards: Primary shards for data distribution (default: 1)
            - number_of_replicas: Replica shards for availability (default: 1)
            - refresh_interval: How often index is refreshed (default: 1s)
            
            Mapping Types:
            - text: Full-text search with analysis
            - keyword: Exact match, aggregations, sorting
            - date: Date and datetime values
            - integer/long: Numeric values
            - double/float: Decimal values
            - boolean: True/false values
            - object: Nested JSON objects
            
            Patterns:
            - Time-series indices: logs-{year}-{month}
            - Multi-tenant: tenant-{tenantId}
            - Environment-based: {env}-{indexName}
            
            Best Practices:
            - Use appropriate shard count (1 shard per ~50GB)
            - Set replicas for high availability
            - Use keyword for aggregations and exact match
            - Use text for full-text search
            - Refresh interval trade-off: lower = more current, higher = better performance
            
            Endpoints:
            - POST /api/indices/create/{indexName} - Create index
            - POST /api/indices/create-with-settings - Create with config
            - DELETE /api/indices/{indexName} - Delete index
            - GET /api/indices/{indexName}/exists - Check exists
            - GET /api/indices/{indexName}/settings - Get settings
            - PUT /api/indices/{indexName}/settings - Update settings
            - POST /api/indices/create-product-index - Create product index
            - POST /api/indices/create-timeseries/{base}/{suffix} - Time-series index
            - POST /api/indices/{indexName}/refresh - Refresh index
            - GET /api/indices/{indexName}/mapping - Get mapping
            - POST /api/indices/create-tenant/{tenantId} - Tenant index
            """);
    }
}
