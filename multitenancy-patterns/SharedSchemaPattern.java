package com.example.multitenancy.sharedschema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shared Schema Pattern
 * All tenants share the same database schema with tenant_id column
 */

@SpringBootApplication
public class SharedSchemaPattern {
    public static void main(String[] args) {
        SpringApplication.run(SharedSchemaPattern.class, args);
    }
}

@Service
class SharedSchemaDataService {
    private final List<TenantData> dataStore = new ArrayList<>();
    
    public SharedSchemaDataService() {
        // Sample data
        dataStore.add(new TenantData("1", "tenant1", "Data 1 for Tenant 1"));
        dataStore.add(new TenantData("2", "tenant1", "Data 2 for Tenant 1"));
        dataStore.add(new TenantData("3", "tenant2", "Data 1 for Tenant 2"));
    }
    
    public List<TenantData> findByTenant(String tenantId) {
        return dataStore.stream()
            .filter(d -> d.getTenantId().equals(tenantId))
            .collect(Collectors.toList());
    }
    
    public TenantData save(TenantData data) {
        dataStore.add(data);
        return data;
    }
}

@RestController
@RequestMapping("/api/shared-schema")
class SharedSchemaController {
    private final SharedSchemaDataService service;
    
    public SharedSchemaController(SharedSchemaDataService service) {
        this.service = service;
    }
    
    @GetMapping("/data/{tenantId}")
    public ResponseEntity<List<TenantData>> getData(@PathVariable String tenantId) {
        return ResponseEntity.ok(service.findByTenant(tenantId));
    }
    
    @PostMapping("/data")
    public ResponseEntity<TenantData> saveData(@RequestBody TenantData data) {
        return ResponseEntity.ok(service.save(data));
    }
}

class TenantData {
    private String id;
    private String tenantId;
    private String data;
    
    public TenantData() {}
    
    public TenantData(String id, String tenantId, String data) {
        this.id = id;
        this.tenantId = tenantId;
        this.data = data;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
