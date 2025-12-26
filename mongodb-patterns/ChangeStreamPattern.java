package com.example.mongodb;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Change Stream Pattern
 * 
 * Demonstrates MongoDB change streams for real-time data monitoring.
 * 
 * Change Stream Features:
 * - Real-time change notifications
 * - Watch insert, update, delete operations
 * - Resume after disconnection
 * - Filter change events
 * - Full document lookup
 * 
 * Use Cases:
 * - Real-time sync
 * - Audit logging
 * - Cache invalidation
 * - Event-driven processing
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class ChangeStreamPattern {

    @Bean
    public ChangeStreamService changeStreamService(MongoTemplate mongoTemplate) {
        return new ChangeStreamService(mongoTemplate);
    }
}

@RestController
@RequestMapping("/api/mongo/changestream")
class ChangeStreamService {

    private final MongoTemplate mongoTemplate;

    public ChangeStreamService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void watchCollection(String collectionName) {
        mongoTemplate.changeStream(collectionName, ChangeStreamOptions.empty(), ChangeEvent.class)
            .listen()
            .doOnNext(event -> {
                System.out.println("Change detected: " + event.getOperationType());
                System.out.println("Document: " + event.getBody());
            })
            .subscribe();
    }

    record ChangeEvent(String operationType, Object body, Object documentKey) {}
}

@RestController
@RequestMapping("/api/mongo/changestream")
class ChangeStreamController {

    private final ChangeStreamService service;

    public ChangeStreamController(ChangeStreamService service) {
        this.service = service;
    }

    @PostMapping("/watch/{collection}")
    public ResponseEntity<String> watchCollection(@PathVariable String collection) {
        service.watchCollection(collection);
        return ResponseEntity.ok("Watching collection: " + collection);
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getInfo() {
        return ResponseEntity.ok(new PatternInfo(
            "Change Stream Pattern",
            "Real-time MongoDB data monitoring",
            "1.0",
            List.of("Real-time notifications", "Resume capability", "Event filtering"),
            List.of("Real-time sync", "Audit logging", "Cache invalidation")
        ));
    }

    record PatternInfo(String name, String description, String version,
                      List<String> features, List<String> useCases) {}
}
