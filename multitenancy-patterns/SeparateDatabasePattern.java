package com.example.multitenancy.separatedatabase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Separate Database Pattern
 * Each tenant has its own dedicated database
 */

@SpringBootApplication
public class SeparateDatabasePattern {
    public static void main(String[] args) {
        SpringApplication.run(SeparateDatabasePattern.class, args);
    }
}

@Service
class SeparateDatabaseService {
    private final Map<String, DatabaseConnection> tenantDatabases = new ConcurrentHashMap<>();
    private final Map<String, List<DatabaseRecord>> databases = new ConcurrentHashMap<>();
    
    public void registerTenantDatabase(String tenantId, String dbUrl) {
        tenantDatabases.put(tenantId, new DatabaseConnection(tenantId, dbUrl));
        databases.putIfAbsent(tenantId, new ArrayList<>());
    }
    
    public List<DatabaseRecord> query(String tenantId, String query) {
        return databases.getOrDefault(tenantId, Collections.emptyList());
    }
    
    public DatabaseRecord insert(String tenantId, DatabaseRecord record) {
        databases.computeIfAbsent(tenantId, k -> new ArrayList<>()).add(record);
        return record;
    }
}

@RestController
@RequestMapping("/api/separate-database")
class SeparateDatabaseController {
    private final SeparateDatabaseService service;
    
    public SeparateDatabaseController(SeparateDatabaseService service) {
        this.service = service;
    }
    
    @PostMapping("/register/{tenantId}")
    public ResponseEntity<String> registerDatabase(
            @PathVariable String tenantId,
            @RequestBody DatabaseRegistration registration) {
        service.registerTenantDatabase(tenantId, registration.getDbUrl());
        return ResponseEntity.ok("Database registered for tenant: " + tenantId);
    }
    
    @GetMapping("/query/{tenantId}")
    public ResponseEntity<List<DatabaseRecord>> queryData(@PathVariable String tenantId) {
        return ResponseEntity.ok(service.query(tenantId, "SELECT * FROM data"));
    }
    
    @PostMapping("/insert/{tenantId}")
    public ResponseEntity<DatabaseRecord> insertData(
            @PathVariable String tenantId,
            @RequestBody DatabaseRecord record) {
        return ResponseEntity.ok(service.insert(tenantId, record));
    }
}

class DatabaseConnection {
    private String tenantId;
    private String dbUrl;
    
    public DatabaseConnection(String tenantId, String dbUrl) {
        this.tenantId = tenantId;
        this.dbUrl = dbUrl;
    }
    
    public String getTenantId() { return tenantId; }
    public String getDbUrl() { return dbUrl; }
}

class DatabaseRegistration {
    private String dbUrl;
    
    public String getDbUrl() { return dbUrl; }
    public void setDbUrl(String dbUrl) { this.dbUrl = dbUrl; }
}

class DatabaseRecord {
    private String id;
    private String data;
    
    public DatabaseRecord() {}
    
    public DatabaseRecord(String id, String data) {
        this.id = id;
        this.data = data;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
