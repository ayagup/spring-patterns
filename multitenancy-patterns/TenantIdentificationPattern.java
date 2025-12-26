package com.example.multitenancy.identification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Tenant Identification Pattern
 * Demonstrates multiple strategies for identifying tenants in requests
 */

@SpringBootApplication
public class TenantIdentificationPattern {
    public static void main(String[] args) {
        SpringApplication.run(TenantIdentificationPattern.class, args);
    }
}

@Component
class TenantIdentifier {
    public String identifyTenant(HttpServletRequest request) {
        // Strategy 1: Header-based
        String headerTenant = request.getHeader("X-Tenant-ID");
        if (headerTenant != null) return headerTenant;
        
        // Strategy 2: Subdomain-based
        String host = request.getServerName();
        if (host.contains(".")) {
            return host.split("\\.")[0];
        }
        
        // Strategy 3: Path-based
        String path = request.getRequestURI();
        if (path.startsWith("/tenant/")) {
            return path.split("/")[2];
        }
        
        return "default";
    }
}

@RestController
@RequestMapping("/api/tenant-identification")
class TenantIdentificationController {
    private final TenantIdentifier identifier;
    
    public TenantIdentificationController(TenantIdentifier identifier) {
        this.identifier = identifier;
    }
    
    @GetMapping("/current")
    public ResponseEntity<TenantInfo> getCurrentTenant(HttpServletRequest request) {
        String tenantId = identifier.identifyTenant(request);
        return ResponseEntity.ok(new TenantInfo(tenantId, request.getHeader("X-Tenant-ID"), 
            request.getServerName(), request.getRequestURI()));
    }
}

class TenantInfo {
    private String identifiedTenant;
    private String headerValue;
    private String serverName;
    private String requestUri;
    
    public TenantInfo(String identifiedTenant, String headerValue, String serverName, String requestUri) {
        this.identifiedTenant = identifiedTenant;
        this.headerValue = headerValue;
        this.serverName = serverName;
        this.requestUri = requestUri;
    }
    
    public String getIdentifiedTenant() { return identifiedTenant; }
    public String getHeaderValue() { return headerValue; }
    public String getServerName() { return serverName; }
    public String getRequestUri() { return requestUri; }
}
