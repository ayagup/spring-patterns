package com.example.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reactive Elasticsearch Template Pattern
 * 
 * Demonstrates the use of ReactiveElasticsearchOperations for
 * non-blocking, reactive Elasticsearch operations.
 * 
 * Key concepts:
 * - ReactiveElasticsearchOperations for reactive operations
 * - Mono for single results
 * - Flux for multiple results
 * - Non-blocking I/O
 * - Backpressure support
 * - Streaming results
 * 
 * Use cases:
 * - High-throughput search
 * - Real-time data indexing
 * - Streaming search results
 * - Asynchronous operations
 * - Scalable search services
 */
@SpringBootApplication
public class ReactiveElasticsearchTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveElasticsearchTemplatePattern.class, args);
    }
}

/**
 * Log document for reactive operations
 */
record Log(
    String id,
    String application,
    String level,
    String message,
    String logger,
    String thread,
    LocalDateTime timestamp
) {}

/**
 * Service demonstrating reactive Elasticsearch operations
 */
@Service
class ReactiveLogService {
    
    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;
    private static final String INDEX_NAME = "logs";
    
    public ReactiveLogService(ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
    }
    
    /**
     * Index a single log (reactive)
     */
    public Mono<Log> indexLog(Log log) {
        IndexQuery indexQuery = new IndexQueryBuilder()
            .withId(log.id())
            .withObject(log)
            .build();
        
        return reactiveElasticsearchOperations
            .index(indexQuery, IndexCoordinates.of(INDEX_NAME))
            .thenReturn(log);
    }
    
    /**
     * Index multiple logs in bulk (reactive)
     */
    public Flux<Log> indexLogsBulk(List<Log> logs) {
        List<IndexQuery> queries = logs.stream()
            .map(log -> new IndexQueryBuilder()
                .withId(log.id())
                .withObject(log)
                .build())
            .toList();
        
        return reactiveElasticsearchOperations
            .bulkIndex(queries, IndexCoordinates.of(INDEX_NAME))
            .thenMany(Flux.fromIterable(logs));
    }
    
    /**
     * Get log by ID (reactive)
     */
    public Mono<Log> getLog(String id) {
        return reactiveElasticsearchOperations.get(id, Log.class, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Search logs by application (reactive)
     */
    public Flux<Log> searchByApplication(String application) {
        Criteria criteria = new Criteria("application").is(application);
        Query query = new CriteriaQuery(criteria);
        
        return reactiveElasticsearchOperations
            .search(query, Log.class, IndexCoordinates.of(INDEX_NAME))
            .map(SearchHit::getContent);
    }
    
    /**
     * Search logs by level (reactive)
     */
    public Flux<Log> searchByLevel(String level) {
        Criteria criteria = new Criteria("level").is(level);
        Query query = new CriteriaQuery(criteria);
        
        return reactiveElasticsearchOperations
            .search(query, Log.class, IndexCoordinates.of(INDEX_NAME))
            .map(SearchHit::getContent);
    }
    
    /**
     * Search logs by message content (reactive full-text search)
     */
    public Flux<Log> searchByMessage(String message) {
        Criteria criteria = new Criteria("message").contains(message);
        Query query = new CriteriaQuery(criteria);
        
        return reactiveElasticsearchOperations
            .search(query, Log.class, IndexCoordinates.of(INDEX_NAME))
            .map(SearchHit::getContent);
    }
    
    /**
     * Search logs by logger (reactive)
     */
    public Flux<Log> searchByLogger(String logger) {
        Criteria criteria = new Criteria("logger").is(logger);
        Query query = new CriteriaQuery(criteria);
        
        return reactiveElasticsearchOperations
            .search(query, Log.class, IndexCoordinates.of(INDEX_NAME))
            .map(SearchHit::getContent);
    }
    
    /**
     * Search logs by time range (reactive)
     */
    public Flux<Log> searchByTimeRange(LocalDateTime start, LocalDateTime end) {
        Criteria criteria = new Criteria("timestamp")
            .between(start, end);
        Query query = new CriteriaQuery(criteria);
        
        return reactiveElasticsearchOperations
            .search(query, Log.class, IndexCoordinates.of(INDEX_NAME))
            .map(SearchHit::getContent);
    }
    
    /**
     * Search error logs by application (reactive)
     */
    public Flux<Log> searchErrorLogs(String application) {
        Criteria criteria = new Criteria("application").is(application)
            .and(new Criteria("level").is("ERROR"));
        Query query = new CriteriaQuery(criteria);
        
        return reactiveElasticsearchOperations
            .search(query, Log.class, IndexCoordinates.of(INDEX_NAME))
            .map(SearchHit::getContent);
    }
    
    /**
     * Stream all logs (reactive)
     */
    public Flux<Log> streamAll() {
        Query query = Query.findAll();
        
        return reactiveElasticsearchOperations
            .search(query, Log.class, IndexCoordinates.of(INDEX_NAME))
            .map(SearchHit::getContent);
    }
    
    /**
     * Count logs (reactive)
     */
    public Mono<Long> count() {
        Query query = Query.findAll();
        return reactiveElasticsearchOperations.count(query, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Count logs by application (reactive)
     */
    public Mono<Long> countByApplication(String application) {
        Criteria criteria = new Criteria("application").is(application);
        Query query = new CriteriaQuery(criteria);
        return reactiveElasticsearchOperations.count(query, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Count logs by level (reactive)
     */
    public Mono<Long> countByLevel(String level) {
        Criteria criteria = new Criteria("level").is(level);
        Query query = new CriteriaQuery(criteria);
        return reactiveElasticsearchOperations.count(query, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Check if log exists (reactive)
     */
    public Mono<Boolean> exists(String id) {
        return reactiveElasticsearchOperations.exists(id, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Delete log by ID (reactive)
     */
    public Mono<String> deleteLog(String id) {
        return reactiveElasticsearchOperations.delete(id, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Delete logs by application (reactive)
     */
    public Mono<Void> deleteByApplication(String application) {
        Criteria criteria = new Criteria("application").is(application);
        Query query = new CriteriaQuery(criteria);
        
        return reactiveElasticsearchOperations
            .delete(query, Log.class, IndexCoordinates.of(INDEX_NAME))
            .then();
    }
}

/**
 * REST controller for reactive log operations
 */
@RestController
@RequestMapping("/api/logs")
class ReactiveLogController {
    
    private final ReactiveLogService reactiveLogService;
    
    public ReactiveLogController(ReactiveLogService reactiveLogService) {
        this.reactiveLogService = reactiveLogService;
    }
    
    @PostMapping
    public Mono<ResponseEntity<Log>> indexLog(@RequestBody Log log) {
        return reactiveLogService.indexLog(log)
            .map(ResponseEntity::ok);
    }
    
    @PostMapping("/bulk")
    public Flux<Log> indexLogsBulk(@RequestBody List<Log> logs) {
        return reactiveLogService.indexLogsBulk(logs);
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Log>> getLog(@PathVariable String id) {
        return reactiveLogService.getLog(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search/application/{application}")
    public Flux<Log> searchByApplication(@PathVariable String application) {
        return reactiveLogService.searchByApplication(application);
    }
    
    @GetMapping("/search/level/{level}")
    public Flux<Log> searchByLevel(@PathVariable String level) {
        return reactiveLogService.searchByLevel(level);
    }
    
    @GetMapping("/search/message")
    public Flux<Log> searchByMessage(@RequestParam String message) {
        return reactiveLogService.searchByMessage(message);
    }
    
    @GetMapping("/search/logger/{logger}")
    public Flux<Log> searchByLogger(@PathVariable String logger) {
        return reactiveLogService.searchByLogger(logger);
    }
    
    @GetMapping("/search/timerange")
    public Flux<Log> searchByTimeRange(
            @RequestParam String start,
            @RequestParam String end) {
        return reactiveLogService.searchByTimeRange(
            LocalDateTime.parse(start),
            LocalDateTime.parse(end)
        );
    }
    
    @GetMapping("/search/errors/{application}")
    public Flux<Log> searchErrorLogs(@PathVariable String application) {
        return reactiveLogService.searchErrorLogs(application);
    }
    
    @GetMapping("/stream/all")
    public Flux<Log> streamAll() {
        return reactiveLogService.streamAll();
    }
    
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> count() {
        return reactiveLogService.count()
            .map(ResponseEntity::ok);
    }
    
    @GetMapping("/count/application/{application}")
    public Mono<ResponseEntity<Long>> countByApplication(@PathVariable String application) {
        return reactiveLogService.countByApplication(application)
            .map(ResponseEntity::ok);
    }
    
    @GetMapping("/count/level/{level}")
    public Mono<ResponseEntity<Long>> countByLevel(@PathVariable String level) {
        return reactiveLogService.countByLevel(level)
            .map(ResponseEntity::ok);
    }
    
    @GetMapping("/{id}/exists")
    public Mono<ResponseEntity<Boolean>> exists(@PathVariable String id) {
        return reactiveLogService.exists(id)
            .map(ResponseEntity::ok);
    }
    
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteLog(@PathVariable String id) {
        return reactiveLogService.deleteLog(id)
            .map(ResponseEntity::ok);
    }
    
    @DeleteMapping("/application/{application}")
    public Mono<ResponseEntity<Void>> deleteByApplication(@PathVariable String application) {
        return reactiveLogService.deleteByApplication(application)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
    
    @GetMapping("/info")
    public Mono<ResponseEntity<String>> getInfo() {
        return Mono.just(ResponseEntity.ok("""
            Reactive Elasticsearch Template Pattern
            
            This pattern demonstrates ReactiveElasticsearchOperations for
            fully reactive, non-blocking Elasticsearch operations.
            
            Features:
            - Non-blocking I/O with Project Reactor
            - Mono<T> for single results
            - Flux<T> for multiple results
            - Backpressure support
            - Streaming search results
            - Reactive bulk operations
            - Asynchronous indexing and querying
            
            Benefits:
            - High throughput for search operations
            - Efficient resource utilization
            - Scalable for concurrent requests
            - Real-time data streaming
            
            Endpoints:
            - POST /api/logs - Index log (returns Mono)
            - POST /api/logs/bulk - Index logs bulk (returns Flux)
            - GET /api/logs/{id} - Get log (returns Mono)
            - GET /api/logs/search/application/{application} - Search by app (Flux)
            - GET /api/logs/search/level/{level} - Search by level (Flux)
            - GET /api/logs/search/message?message= - Search by message (Flux)
            - GET /api/logs/search/logger/{logger} - Search by logger (Flux)
            - GET /api/logs/search/timerange?start=&end= - Search by time (Flux)
            - GET /api/logs/search/errors/{application} - Search errors (Flux)
            - GET /api/logs/stream/all - Stream all logs (Flux)
            - GET /api/logs/count - Count logs (Mono)
            - GET /api/logs/count/application/{application} - Count by app (Mono)
            - GET /api/logs/count/level/{level} - Count by level (Mono)
            - GET /api/logs/{id}/exists - Check exists (Mono)
            - DELETE /api/logs/{id} - Delete log (Mono)
            - DELETE /api/logs/application/{application} - Delete by app (Mono)
            """));
    }
}
