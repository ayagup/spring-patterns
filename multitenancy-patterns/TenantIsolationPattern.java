package com.example.multitenancy.isolation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * Tenant Isolation Pattern
 * Ensures data isolation and prevents cross-tenant data access
 */

@SpringBootApplication
public class TenantIsolationPattern {
    public static void main(String[] args) {
        SpringApplication.run(TenantIsolationPattern.class, args);
    }
}

@Component
class TenantIsolationFilter {
    public boolean validateAccess(String requestingTenant, String resourceTenant) {
        return requestingTenant != null && requestingTenant.equals(resourceTenant);
    }
    
    public void enforceIsolation(String currentTenant, String resourceId, String resourceTenant) {
        if (!validateAccess(currentTenant, resourceTenant)) {
            throw new TenantIsolationViolationException(
                "Access denied: Tenant " + currentTenant + " cannot access resource of tenant " + resourceTenant
            );
        }
    }
}

@RestController
@RequestMapping("/api/tenant-isolation")
class TenantIsolationController {
    private final TenantIsolationFilter filter;
    private final Map<String, IsolatedResource> resources = new HashMap<>();
    
    public TenantIsolationController(TenantIsolationFilter filter) {
        this.filter = filter;
        // Sample data
        resources.put("res1", new IsolatedResource("res1", "tenant1", "Resource 1 data"));
        resources.put("res2", new IsolatedResource("res2", "tenant2", "Resource 2 data"));
    }
    
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<IsolatedResource> getResource(
            @PathVariable String resourceId,
            @RequestHeader("X-Tenant-ID") String currentTenant) {
        
        IsolatedResource resource = resources.get(resourceId);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        
        filter.enforceIsolation(currentTenant, resourceId, resource.getTenantId());
        return ResponseEntity.ok(resource);
    }
}

class IsolatedResource {
    private String id;
    private String tenantId;
    private String data;
    
    public IsolatedResource(String id, String tenantId, String data) {
        this.id = id;
        this.tenantId = tenantId;
        this.data = data;
    }
    
    public String getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getData() { return data; }
}

class TenantIsolationViolationException extends RuntimeException {
    public TenantIsolationViolationException(String message) {
        super(message);
    }
}
