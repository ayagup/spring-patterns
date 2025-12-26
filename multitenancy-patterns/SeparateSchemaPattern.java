package com.example.multitenancy.separateschema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Separate Schema Pattern
 * Each tenant has its own database schema within the same database
 */

@SpringBootApplication
public class SeparateSchemaPattern {
    public static void main(String[] args) {
        SpringApplication.run(SeparateSchemaPattern.class, args);
    }
}

@Service
class SeparateSchemaDataService {
    private final Map<String, List<SchemaData>> schemaDataStores = new ConcurrentHashMap<>();
    
    public List<SchemaData> findByTenantSchema(String tenantId) {
        String schemaName = "schema_" + tenantId;
        return schemaDataStores.getOrDefault(schemaName, Collections.emptyList());
    }
    
    public SchemaData save(String tenantId, SchemaData data) {
        String schemaName = "schema_" + tenantId;
        schemaDataStores.computeIfAbsent(schemaName, k -> new ArrayList<>()).add(data);
        return data;
    }
    
    public void initializeSchema(String tenantId) {
        String schemaName = "schema_" + tenantId;
        schemaDataStores.putIfAbsent(schemaName, new ArrayList<>());
    }
}

@RestController
@RequestMapping("/api/separate-schema")
class SeparateSchemaController {
    private final SeparateSchemaDataService service;
    
    public SeparateSchemaController(SeparateSchemaDataService service) {
        this.service = service;
    }
    
    @PostMapping("/initialize/{tenantId}")
    public ResponseEntity<String> initializeSchema(@PathVariable String tenantId) {
        service.initializeSchema(tenantId);
        return ResponseEntity.ok("Schema initialized for tenant: " + tenantId);
    }
    
    @GetMapping("/data/{tenantId}")
    public ResponseEntity<List<SchemaData>> getData(@PathVariable String tenantId) {
        return ResponseEntity.ok(service.findByTenantSchema(tenantId));
    }
    
    @PostMapping("/data/{tenantId}")
    public ResponseEntity<SchemaData> saveData(@PathVariable String tenantId, @RequestBody SchemaData data) {
        return ResponseEntity.ok(service.save(tenantId, data));
    }
}

class SchemaData {
    private String id;
    private String content;
    
    public SchemaData() {}
    
    public SchemaData(String id, String content) {
        this.id = id;
        this.content = content;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
