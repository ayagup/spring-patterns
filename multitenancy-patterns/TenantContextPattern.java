package com.example.multitenancy.context;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

/**
 * Tenant Context Pattern
 * Thread-safe tenant context storage using ThreadLocal
 */

@SpringBootApplication
public class TenantContextPattern {
    public static void main(String[] args) {
        SpringApplication.run(TenantContextPattern.class, args);
    }
}

@Component
class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }
    
    public static String getTenantId() {
        return currentTenant.get();
    }
    
    public static void clear() {
        currentTenant.remove();
    }
}

@RestController
@RequestMapping("/api/tenant-context")
class TenantContextController {
    
    @PostMapping("/set/{tenantId}")
    public ResponseEntity<String> setTenant(@PathVariable String tenantId) {
        TenantContext.setTenantId(tenantId);
        return ResponseEntity.ok("Tenant set to: " + tenantId);
    }
    
    @GetMapping("/get")
    public ResponseEntity<TenantContextInfo> getTenant() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(new TenantContextInfo(tenantId, Thread.currentThread().getName()));
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearTenant() {
        TenantContext.clear();
        return ResponseEntity.ok("Tenant context cleared");
    }
}

class TenantContextInfo {
    private String tenantId;
    private String threadName;
    
    public TenantContextInfo(String tenantId, String threadName) {
        this.tenantId = tenantId;
        this.threadName = threadName;
    }
    
    public String getTenantId() { return tenantId; }
    public String getThreadName() { return threadName; }
}
