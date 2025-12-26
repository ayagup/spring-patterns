package com.example.contenttypehandling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Content Type Resolver Pattern
 * 
 * Demonstrates custom content type resolution strategies in Spring MVC.
 * This pattern shows how to implement custom logic for determining the
 * appropriate content type for request/response handling.
 * 
 * Key Concepts:
 * - ContentNegotiationStrategy interface
 * - Custom resolution logic
 * - Request inspection
 * - Fallback mechanisms
 * - Context-aware type selection
 * 
 * Use Cases:
 * - Custom content negotiation rules
 * - Legacy system integration
 * - Special routing based on custom headers
 * - Multi-tenant content type resolution
 * - Device-specific format selection
 */
@SpringBootApplication
public class ContentTypeResolverPattern {

    public static void main(String[] args) {
        SpringApplication.run(ContentTypeResolverPattern.class, args);
    }
}

/**
 * Custom Content Type Resolver based on custom header
 */
class CustomHeaderResolver implements ContentNegotiationStrategy {

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest request) {
        // Check for custom X-Response-Format header
        String format = request.getHeader("X-Response-Format");
        
        if ("json".equalsIgnoreCase(format)) {
            return List.of(MediaType.APPLICATION_JSON);
        } else if ("xml".equalsIgnoreCase(format)) {
            return List.of(MediaType.APPLICATION_XML);
        } else if ("html".equalsIgnoreCase(format)) {
            return List.of(MediaType.TEXT_HTML);
        }
        
        // Return empty list to try next strategy
        return List.of();
    }
}

/**
 * Device-based Content Type Resolver
 */
class DeviceBasedResolver implements ContentNegotiationStrategy {

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest request) {
        String userAgent = request.getHeader("User-Agent");
        
        if (userAgent == null) {
            return List.of();
        }
        
        // Mobile devices get simplified JSON
        if (userAgent.toLowerCase().contains("mobile")) {
            return List.of(MediaType.APPLICATION_JSON);
        }
        
        // Desktop browsers get HTML
        if (userAgent.toLowerCase().contains("mozilla") || 
            userAgent.toLowerCase().contains("chrome")) {
            return List.of(MediaType.TEXT_HTML);
        }
        
        // API clients get JSON
        return List.of(MediaType.APPLICATION_JSON);
    }
}

/**
 * User Role-based Content Type Resolver
 */
class RoleBasedResolver implements ContentNegotiationStrategy {

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest request) {
        // Check for role header (in real app, check security context)
        String role = request.getHeader("X-User-Role");
        
        if ("ADMIN".equalsIgnoreCase(role)) {
            // Admins get detailed XML
            return List.of(MediaType.APPLICATION_XML);
        } else if ("USER".equalsIgnoreCase(role)) {
            // Regular users get JSON
            return List.of(MediaType.APPLICATION_JSON);
        } else if ("GUEST".equalsIgnoreCase(role)) {
            // Guests get HTML
            return List.of(MediaType.TEXT_HTML);
        }
        
        return List.of();
    }
}

/**
 * Tenant-based Content Type Resolver
 */
class TenantBasedResolver implements ContentNegotiationStrategy {

    private final Map<String, MediaType> tenantFormats = Map.of(
            "tenant-a", MediaType.APPLICATION_JSON,
            "tenant-b", MediaType.APPLICATION_XML,
            "tenant-c", MediaType.TEXT_HTML
    );

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest request) {
        String tenantId = request.getHeader("X-Tenant-ID");
        
        if (tenantId != null && tenantFormats.containsKey(tenantId.toLowerCase())) {
            return List.of(tenantFormats.get(tenantId.toLowerCase()));
        }
        
        return List.of();
    }
}

/**
 * API Version-based Content Type Resolver
 */
class VersionBasedResolver implements ContentNegotiationStrategy {

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest request) {
        String apiVersion = request.getHeader("X-API-Version");
        
        if ("1.0".equals(apiVersion)) {
            return List.of(MediaType.parseMediaType("application/vnd.api-v1+json"));
        } else if ("2.0".equals(apiVersion)) {
            return List.of(MediaType.parseMediaType("application/vnd.api-v2+json"));
        } else if ("3.0".equals(apiVersion)) {
            return List.of(MediaType.parseMediaType("application/vnd.api-v3+json"));
        }
        
        return List.of();
    }
}

/**
 * Path-based Content Type Resolver
 */
class PathBasedResolver implements ContentNegotiationStrategy {

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest request) {
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        
        if (servletRequest != null) {
            String path = servletRequest.getRequestURI();
            
            // API endpoints return JSON
            if (path.startsWith("/api/")) {
                return List.of(MediaType.APPLICATION_JSON);
            }
            
            // Web endpoints return HTML
            if (path.startsWith("/web/")) {
                return List.of(MediaType.TEXT_HTML);
            }
            
            // Admin endpoints return XML
            if (path.startsWith("/admin/")) {
                return List.of(MediaType.APPLICATION_XML);
            }
        }
        
        return List.of();
    }
}

/**
 * Composite Content Type Resolver with fallback
 */
class CompositeResolver implements ContentNegotiationStrategy {

    private final List<ContentNegotiationStrategy> strategies;

    public CompositeResolver(List<ContentNegotiationStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest request) {
        // Try each strategy in order
        for (ContentNegotiationStrategy strategy : strategies) {
            List<MediaType> types = strategy.resolveMediaTypes(request);
            if (!types.isEmpty()) {
                return types;
            }
        }
        
        // Default fallback
        return List.of(MediaType.APPLICATION_JSON);
    }
}

/**
 * Configuration using custom resolvers
 */
@Configuration
class CustomResolverConfig {

    @Bean
    public ContentNegotiationManager customContentNegotiationManager() {
        // Create composite resolver with multiple strategies
        List<ContentNegotiationStrategy> strategies = List.of(
                new CustomHeaderResolver(),
                new DeviceBasedResolver(),
                new RoleBasedResolver(),
                new TenantBasedResolver(),
                new VersionBasedResolver(),
                new PathBasedResolver()
        );
        
        CompositeResolver compositeResolver = new CompositeResolver(strategies);
        return new ContentNegotiationManager(compositeResolver);
    }
}

/**
 * Controller demonstrating custom resolver usage
 */
@RestController
@RequestMapping("/api/content")
class ContentResolverController {

    /**
     * Returns content in format determined by custom resolvers
     */
    @GetMapping(value = "/data", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_HTML_VALUE
    })
    public ContentData getData(HttpServletRequest request) {
        String resolvedType = determineResolvedType(request);
        return new ContentData("Sample data", resolvedType);
    }

    /**
     * Returns list with custom resolution
     */
    @GetMapping(value = "/list", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public List<ContentData> getDataList() {
        return List.of(
                new ContentData("Item 1", "auto"),
                new ContentData("Item 2", "auto"),
                new ContentData("Item 3", "auto")
        );
    }

    /**
     * Helper method to determine resolved content type
     */
    private String determineResolvedType(HttpServletRequest request) {
        // Check various headers/paths used by resolvers
        if (request.getHeader("X-Response-Format") != null) {
            return "Custom header";
        } else if (request.getHeader("User-Agent") != null) {
            return "Device-based";
        } else if (request.getHeader("X-User-Role") != null) {
            return "Role-based";
        } else if (request.getHeader("X-Tenant-ID") != null) {
            return "Tenant-based";
        } else if (request.getHeader("X-API-Version") != null) {
            return "Version-based";
        } else {
            return "Path-based";
        }
    }
}

/**
 * Controller for testing different resolver scenarios
 */
@RestController
@RequestMapping("/api/test-resolver")
class ResolverTestController {

    @GetMapping("/custom-header")
    public Map<String, String> testCustomHeader(
            @RequestHeader(value = "X-Response-Format", defaultValue = "json") String format) {
        return Map.of("resolver", "CustomHeaderResolver", "format", format);
    }

    @GetMapping("/device")
    public Map<String, String> testDeviceResolver(
            @RequestHeader(value = "User-Agent", defaultValue = "unknown") String userAgent) {
        return Map.of("resolver", "DeviceBasedResolver", "userAgent", userAgent);
    }

    @GetMapping("/role")
    public Map<String, String> testRoleResolver(
            @RequestHeader(value = "X-User-Role", defaultValue = "guest") String role) {
        return Map.of("resolver", "RoleBasedResolver", "role", role);
    }

    @GetMapping("/tenant")
    public Map<String, String> testTenantResolver(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        return Map.of("resolver", "TenantBasedResolver", "tenant", tenantId);
    }

    @GetMapping("/version")
    public Map<String, String> testVersionResolver(
            @RequestHeader(value = "X-API-Version", defaultValue = "1.0") String version) {
        return Map.of("resolver", "VersionBasedResolver", "version", version);
    }
}

/**
 * Domain model
 */
class ContentData {
    private String content;
    private String resolvedBy;

    public ContentData(String content, String resolvedBy) {
        this.content = content;
        this.resolvedBy = resolvedBy;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
}

/**
 * Documentation:
 * 
 * ContentNegotiationStrategy Interface:
 * - Single method: List<MediaType> resolveMediaTypes(NativeWebRequest request)
 * - Return list of media types in order of preference
 * - Return empty list if strategy cannot determine type
 * - Can inspect any part of the request (headers, path, parameters, etc.)
 * 
 * Custom Resolver Strategies:
 * 
 * 1. Custom Header Resolver:
 *    - Checks X-Response-Format header
 *    - Simple and explicit
 *    - Good for API clients
 * 
 * 2. Device-Based Resolver:
 *    - Inspects User-Agent header
 *    - Different formats for mobile vs desktop
 *    - User experience optimization
 * 
 * 3. Role-Based Resolver:
 *    - Based on user permissions
 *    - Different data formats per role
 *    - Security-aware formatting
 * 
 * 4. Tenant-Based Resolver:
 *    - Multi-tenant applications
 *    - Per-tenant format preferences
 *    - Customization per customer
 * 
 * 5. Version-Based Resolver:
 *    - API versioning via content types
 *    - Different formats per API version
 *    - Backward compatibility
 * 
 * 6. Path-Based Resolver:
 *    - Format based on URL structure
 *    - /api/ -> JSON, /web/ -> HTML
 *    - Clear separation of concerns
 * 
 * Composite Pattern:
 * - Combines multiple resolvers
 * - Try each in sequence
 * - First successful resolver wins
 * - Provides fallback mechanism
 * 
 * Best Practices:
 * - Return empty list if resolver doesn't apply
 * - Provide sensible defaults
 * - Document custom headers/requirements
 * - Consider caching resolved types
 * - Use composite pattern for flexibility
 * - Log resolution decisions for debugging
 * 
 * Implementation Tips:
 * - Access request via NativeWebRequest
 * - Cast to HttpServletRequest if needed
 * - Check for null values
 * - Handle edge cases gracefully
 * - Consider performance impact
 * - Test with different scenarios
 * 
 * Common Use Cases:
 * - Custom business rules for format selection
 * - Legacy system compatibility
 * - Special client requirements
 * - Gradual migration strategies
 * - A/B testing different formats
 * - Feature flags for format rollout
 */
