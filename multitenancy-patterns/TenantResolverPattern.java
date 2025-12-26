package com.example.multitenancy.resolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tenant Resolver Pattern
 * Resolves tenant configuration and metadata
 */

@SpringBootApplication
public class TenantResolverPattern {
    public static void main(String[] args) {
        SpringApplication.run(TenantResolverPattern.class, args);
    }
}

@Component
class TenantResolver {
    private final Map<String, TenantConfig> tenantConfigs = new ConcurrentHashMap<>();
    
    public TenantResolver() {
        // Initialize sample tenants
        tenantConfigs.put("tenant1", new TenantConfig("tenant1", "Tenant One", "jdbc:postgresql://localhost/tenant1_db"));
        tenantConfigs.put("tenant2", new TenantConfig("tenant2", "Tenant Two", "jdbc:postgresql://localhost/tenant2_db"));
    }
    
    public Optional<TenantConfig> resolve(String tenantId) {
        return Optional.ofNullable(tenantConfigs.get(tenantId));
    }
    
    public void register(TenantConfig config) {
        tenantConfigs.put(config.getTenantId(), config);
    }
}

@RestController
@RequestMapping("/api/tenant-resolver")
class TenantResolverController {
    private final TenantResolver resolver;
    
    public TenantResolverController(TenantResolver resolver) {
        this.resolver = resolver;
    }
    
    @GetMapping("/resolve/{tenantId}")
    public ResponseEntity<TenantConfig> resolveTenant(@PathVariable String tenantId) {
        return resolver.resolve(tenantId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/register")
    public ResponseEntity<String> registerTenant(@RequestBody TenantConfig config) {
        resolver.register(config);
        return ResponseEntity.ok("Tenant registered: " + config.getTenantId());
    }
}

class TenantConfig {
    private String tenantId;
    private String tenantName;
    private String databaseUrl;
    
    public TenantConfig() {}
    
    public TenantConfig(String tenantId, String tenantName, String databaseUrl) {
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.databaseUrl = databaseUrl;
    }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public String getDatabaseUrl() { return databaseUrl; }
    public void setDatabaseUrl(String databaseUrl) { this.databaseUrl = databaseUrl; }
}
