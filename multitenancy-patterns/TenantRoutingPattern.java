package com.example.multitenancy.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tenant Routing Pattern
 * Routes requests to appropriate tenant-specific resources
 */

@SpringBootApplication
public class TenantRoutingPattern {
    public static void main(String[] args) {
        SpringApplication.run(TenantRoutingPattern.class, args);
    }
}

@Service
class TenantRouter {
    private final Map<String, String> tenantRoutes = new ConcurrentHashMap<>();
    
    public TenantRouter() {
        // Initialize routes
        tenantRoutes.put("tenant1", "http://tenant1.example.com");
        tenantRoutes.put("tenant2", "http://tenant2.example.com");
        tenantRoutes.put("tenant3", "http://tenant3.example.com");
    }
    
    public String routeRequest(String tenantId, String path) {
        String baseUrl = tenantRoutes.getOrDefault(tenantId, "http://default.example.com");
        return baseUrl + path;
    }
    
    public void registerRoute(String tenantId, String url) {
        tenantRoutes.put(tenantId, url);
    }
    
    public Map<String, String> getAllRoutes() {
        return new HashMap<>(tenantRoutes);
    }
}

@RestController
@RequestMapping("/api/tenant-routing")
class TenantRoutingController {
    private final TenantRouter router;
    
    public TenantRoutingController(TenantRouter router) {
        this.router = router;
    }
    
    @GetMapping("/route/{tenantId}")
    public ResponseEntity<RouteInfo> getRoute(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "/api/data") String path) {
        
        String routedUrl = router.routeRequest(tenantId, path);
        return ResponseEntity.ok(new RouteInfo(tenantId, path, routedUrl));
    }
    
    @PostMapping("/register")
    public ResponseEntity<String> registerRoute(@RequestBody TenantRouteRegistration registration) {
        router.registerRoute(registration.getTenantId(), registration.getUrl());
        return ResponseEntity.ok("Route registered for tenant: " + registration.getTenantId());
    }
    
    @GetMapping("/routes")
    public ResponseEntity<Map<String, String>> getAllRoutes() {
        return ResponseEntity.ok(router.getAllRoutes());
    }
}

class RouteInfo {
    private String tenantId;
    private String requestPath;
    private String routedUrl;
    
    public RouteInfo(String tenantId, String requestPath, String routedUrl) {
        this.tenantId = tenantId;
        this.requestPath = requestPath;
        this.routedUrl = routedUrl;
    }
    
    public String getTenantId() { return tenantId; }
    public String getRequestPath() { return requestPath; }
    public String getRoutedUrl() { return routedUrl; }
}

class TenantRouteRegistration {
    private String tenantId;
    private String url;
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
