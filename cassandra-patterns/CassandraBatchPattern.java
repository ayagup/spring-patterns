package com.example.cassandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cassandra Batch Pattern
 * 
 * Demonstrates the use of batch operations in Apache Cassandra
 * for atomic writes and improved performance.
 * 
 * Key concepts:
 * - Logged batches (atomic)
 * - Unlogged batches (non-atomic, better performance)
 * - Counter batches
 * - Batch size limits
 * - Batch best practices
 * 
 * Use cases:
 * - Multiple inserts/updates as single operation
 * - Maintaining denormalized data consistency
 * - Bulk operations
 * - Transactional writes (logged batch)
 * - High-throughput writes (unlogged batch)
 */
@SpringBootApplication
public class CassandraBatchPattern {

    public static void main(String[] args) {
        SpringApplication.run(CassandraBatchPattern.class, args);
    }
}

/**
 * Log entry entity
 */
record LogEntry(
    UUID id,
    String application,
    String level,
    String message,
    String thread,
    LocalDateTime timestamp
) {
    public LogEntry {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

/**
 * Service demonstrating batch operations
 */
@Service
class LogBatchService {
    
    private final CassandraTemplate cassandraTemplate;
    private final CqlTemplate cqlTemplate;
    
    public LogBatchService(CassandraTemplate cassandraTemplate, CqlTemplate cqlTemplate) {
        this.cassandraTemplate = cassandraTemplate;
        this.cqlTemplate = cqlTemplate;
    }
    
    /**
     * Insert multiple log entries using logged batch (atomic)
     * All operations succeed or fail together
     */
    public List<LogEntry> insertLoggedBatch(List<LogEntry> entries) {
        return cassandraTemplate.batchOps()
                                .insert(entries)
                                .execute();
    }
    
    /**
     * Insert multiple log entries using unlogged batch (better performance)
     * Operations are not atomic
     */
    public void insertUnloggedBatch(List<LogEntry> entries) {
        StringBuilder cql = new StringBuilder("BEGIN UNLOGGED BATCH\n");
        for (LogEntry entry : entries) {
            cql.append(String.format(
                "INSERT INTO log_entries (id, application, level, message, thread, timestamp) " +
                "VALUES (%s, '%s', '%s', '%s', '%s', '%s');\n",
                entry.id(), entry.application(), entry.level(), 
                entry.message(), entry.thread(), entry.timestamp()
            ));
        }
        cql.append("APPLY BATCH;");
        cqlTemplate.execute(cql.toString());
    }
    
    /**
     * Update multiple log entries in logged batch
     */
    public List<LogEntry> updateLoggedBatch(List<LogEntry> entries) {
        return cassandraTemplate.batchOps()
                                .update(entries)
                                .execute();
    }
    
    /**
     * Delete multiple log entries in logged batch
     */
    public void deleteLoggedBatch(List<LogEntry> entries) {
        cassandraTemplate.batchOps()
                        .delete(entries)
                        .execute();
    }
    
    /**
     * Delete by IDs in batch
     */
    public void deleteByIdsBatch(List<UUID> ids) {
        StringBuilder cql = new StringBuilder("BEGIN BATCH\n");
        for (UUID id : ids) {
            cql.append(String.format("DELETE FROM log_entries WHERE id = %s;\n", id));
        }
        cql.append("APPLY BATCH;");
        cqlTemplate.execute(cql.toString());
    }
    
    /**
     * Mixed batch operations (insert, update, delete)
     */
    public void mixedBatch(List<LogEntry> toInsert, List<LogEntry> toUpdate, List<UUID> toDelete) {
        StringBuilder cql = new StringBuilder("BEGIN BATCH\n");
        
        // Insert operations
        for (LogEntry entry : toInsert) {
            cql.append(String.format(
                "INSERT INTO log_entries (id, application, level, message, thread, timestamp) " +
                "VALUES (%s, '%s', '%s', '%s', '%s', '%s');\n",
                entry.id(), entry.application(), entry.level(), 
                entry.message(), entry.thread(), entry.timestamp()
            ));
        }
        
        // Update operations
        for (LogEntry entry : toUpdate) {
            cql.append(String.format(
                "UPDATE log_entries SET level = '%s', message = '%s' WHERE id = %s;\n",
                entry.level(), entry.message(), entry.id()
            ));
        }
        
        // Delete operations
        for (UUID id : toDelete) {
            cql.append(String.format("DELETE FROM log_entries WHERE id = %s;\n", id));
        }
        
        cql.append("APPLY BATCH;");
        cqlTemplate.execute(cql.toString());
    }
    
    /**
     * Batch insert with different tables (denormalization pattern)
     * Keep data consistent across multiple tables
     */
    public void insertDenormalizedBatch(LogEntry entry) {
        StringBuilder cql = new StringBuilder("BEGIN BATCH\n");
        
        // Insert into main table
        cql.append(String.format(
            "INSERT INTO log_entries (id, application, level, message, thread, timestamp) " +
            "VALUES (%s, '%s', '%s', '%s', '%s', '%s');\n",
            entry.id(), entry.application(), entry.level(), 
            entry.message(), entry.thread(), entry.timestamp()
        ));
        
        // Insert into application-specific table (denormalized)
        cql.append(String.format(
            "INSERT INTO log_entries_by_application (application, timestamp, id, level, message, thread) " +
            "VALUES ('%s', '%s', %s, '%s', '%s', '%s');\n",
            entry.application(), entry.timestamp(), entry.id(), 
            entry.level(), entry.message(), entry.thread()
        ));
        
        // Insert into level-specific table (denormalized)
        cql.append(String.format(
            "INSERT INTO log_entries_by_level (level, timestamp, id, application, message, thread) " +
            "VALUES ('%s', '%s', %s, '%s', '%s', '%s');\n",
            entry.level(), entry.timestamp(), entry.id(), 
            entry.application(), entry.message(), entry.thread()
        ));
        
        cql.append("APPLY BATCH;");
        cqlTemplate.execute(cql.toString());
    }
    
    /**
     * Conditional batch (lightweight transactions)
     */
    public boolean conditionalBatch(LogEntry entry) {
        StringBuilder cql = new StringBuilder("BEGIN BATCH\n");
        
        cql.append(String.format(
            "INSERT INTO log_entries (id, application, level, message, thread, timestamp) " +
            "VALUES (%s, '%s', '%s', '%s', '%s', '%s') IF NOT EXISTS;\n",
            entry.id(), entry.application(), entry.level(), 
            entry.message(), entry.thread(), entry.timestamp()
        ));
        
        cql.append("APPLY BATCH;");
        
        try {
            cqlTemplate.execute(cql.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Find all log entries
     */
    public List<LogEntry> findAll() {
        return cassandraTemplate.selectAll(LogEntry.class);
    }
    
    /**
     * Find by application
     */
    public List<LogEntry> findByApplication(String application) {
        String cql = "SELECT * FROM log_entries WHERE application = ? ALLOW FILTERING";
        return cqlTemplate.query(cql, (row, rowNum) -> 
            new LogEntry(
                row.getUuid("id"),
                row.getString("application"),
                row.getString("level"),
                row.getString("message"),
                row.getString("thread"),
                row.get("timestamp", LocalDateTime.class)
            ), application);
    }
    
    /**
     * Count all log entries
     */
    public long count() {
        return cassandraTemplate.count(LogEntry.class);
    }
}

/**
 * REST controller for batch operations
 */
@RestController
@RequestMapping("/api/logs")
class LogBatchController {
    
    private final LogBatchService logBatchService;
    
    public LogBatchController(LogBatchService logBatchService) {
        this.logBatchService = logBatchService;
    }
    
    @PostMapping("/batch/logged")
    public ResponseEntity<List<LogEntry>> insertLoggedBatch(@RequestBody List<LogEntry> entries) {
        return ResponseEntity.ok(logBatchService.insertLoggedBatch(entries));
    }
    
    @PostMapping("/batch/unlogged")
    public ResponseEntity<String> insertUnloggedBatch(@RequestBody List<LogEntry> entries) {
        logBatchService.insertUnloggedBatch(entries);
        return ResponseEntity.ok("Unlogged batch inserted");
    }
    
    @PutMapping("/batch")
    public ResponseEntity<List<LogEntry>> updateBatch(@RequestBody List<LogEntry> entries) {
        return ResponseEntity.ok(logBatchService.updateLoggedBatch(entries));
    }
    
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<LogEntry> entries) {
        logBatchService.deleteLoggedBatch(entries);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/batch/ids")
    public ResponseEntity<Void> deleteByIdsBatch(@RequestBody List<UUID> ids) {
        logBatchService.deleteByIdsBatch(ids);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/batch/mixed")
    public ResponseEntity<String> mixedBatch(
            @RequestParam(required = false) List<LogEntry> toInsert,
            @RequestParam(required = false) List<LogEntry> toUpdate,
            @RequestParam(required = false) List<UUID> toDelete) {
        logBatchService.mixedBatch(
            toInsert != null ? toInsert : new ArrayList<>(),
            toUpdate != null ? toUpdate : new ArrayList<>(),
            toDelete != null ? toDelete : new ArrayList<>()
        );
        return ResponseEntity.ok("Mixed batch executed");
    }
    
    @PostMapping("/batch/denormalized")
    public ResponseEntity<String> insertDenormalizedBatch(@RequestBody LogEntry entry) {
        logBatchService.insertDenormalizedBatch(entry);
        return ResponseEntity.ok("Denormalized batch inserted");
    }
    
    @PostMapping("/batch/conditional")
    public ResponseEntity<Boolean> conditionalBatch(@RequestBody LogEntry entry) {
        boolean success = logBatchService.conditionalBatch(entry);
        return ResponseEntity.ok(success);
    }
    
    @GetMapping
    public ResponseEntity<List<LogEntry>> getAllLogs() {
        return ResponseEntity.ok(logBatchService.findAll());
    }
    
    @GetMapping("/application/{application}")
    public ResponseEntity<List<LogEntry>> getByApplication(@PathVariable String application) {
        return ResponseEntity.ok(logBatchService.findByApplication(application));
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> countLogs() {
        return ResponseEntity.ok(logBatchService.count());
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Cassandra Batch Pattern
            
            This pattern demonstrates the use of batch operations in Apache Cassandra
            for atomic writes and improved performance.
            
            Features:
            - Logged batches (atomic, all-or-nothing)
            - Unlogged batches (better performance, not atomic)
            - Counter batches
            - Mixed operations (insert, update, delete)
            - Denormalized batch writes
            - Conditional batches (lightweight transactions)
            
            Best Practices:
            - Use logged batches for operations on the same partition key
            - Use unlogged batches for bulk operations on different partitions
            - Keep batch size small (recommended: < 100 operations)
            - Avoid batches with different partition keys when possible
            - Use conditional batches sparingly (expensive)
            
            Endpoints:
            - POST /api/logs/batch/logged - Insert logged batch (atomic)
            - POST /api/logs/batch/unlogged - Insert unlogged batch (fast)
            - PUT /api/logs/batch - Update batch
            - DELETE /api/logs/batch - Delete batch
            - DELETE /api/logs/batch/ids - Delete by IDs batch
            - POST /api/logs/batch/mixed - Mixed operations batch
            - POST /api/logs/batch/denormalized - Denormalized batch
            - POST /api/logs/batch/conditional - Conditional batch
            - GET /api/logs - Get all logs
            - GET /api/logs/application/{application} - Filter by application
            - GET /api/logs/count - Count logs
            """);
    }
}
