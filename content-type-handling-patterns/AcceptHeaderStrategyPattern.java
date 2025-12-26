package com.example.contenttypehandling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Accept Header Strategy Pattern
 * 
 * Demonstrates content negotiation based on the Accept HTTP header.
 * This is the most RESTful approach to content negotiation where the client
 * specifies desired response format via the Accept header.
 * 
 * Key Concepts:
 * - HTTP Accept header parsing
 * - Quality values (q-factor)
 * - Media type precedence
 * - Multiple accept types
 * - Wildcard handling (*/<marker>*</marker>)
 * 
 * Use Cases:
 * - RESTful API content negotiation
 * - Client-driven format selection
 * - Multi-format APIs
 * - API versioning via content types
 */
@SpringBootApplication
public class AcceptHeaderStrategyPattern {

    public static void main(String[] args) {
        SpringApplication.run(AcceptHeaderStrategyPattern.class, args);
    }
}

/**
 * Configuration for Accept Header Strategy
 */
@Configuration
class AcceptHeaderConfig implements WebMvcConfigurer {

    /**
     * Configure content negotiation to use Accept header
     */
    @Bean
    public ContentNegotiationManager contentNegotiationManager() {
        // Use only Accept header for content negotiation
        HeaderContentNegotiationStrategy strategy = new HeaderContentNegotiationStrategy();
        return new ContentNegotiationManager(strategy);
    }
}

/**
 * Controller demonstrating basic Accept header handling
 */
@RestController
@RequestMapping("/api/resources")
class ResourceController {

    /**
     * Returns JSON or XML based on Accept header
     * Accept: application/json -> returns JSON
     * Accept: application/xml -> returns XML
     */
    @GetMapping(value = "/{id}", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public Resource getResource(@PathVariable Long id) {
        return new Resource(id, "Resource " + id, "active");
    }

    /**
     * Returns JSON, XML, or HTML based on Accept header
     * Accept: text/html -> returns HTML
     */
    @GetMapping(value = "/{id}/view", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_HTML_VALUE
    })
    public Resource getResourceView(@PathVariable Long id) {
        return new Resource(id, "Resource " + id, "active");
    }

    /**
     * Returns multiple formats including plain text
     */
    @GetMapping(value = "/{id}/info", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_PLAIN_VALUE
    })
    public Resource getResourceInfo(@PathVariable Long id) {
        return new Resource(id, "Resource " + id, "active");
    }
}

/**
 * Controller demonstrating quality value handling
 */
@RestController
@RequestMapping("/api/products")
class ProductAcceptController {

    /**
     * Handles multiple accept types with quality values
     * Accept: application/json;q=0.9, application/xml;q=0.8
     * JSON preferred over XML due to higher q-value
     */
    @GetMapping(produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public List<ProductInfo> getProducts() {
        return Arrays.asList(
                new ProductInfo(1L, "Product 1", 99.99),
                new ProductInfo(2L, "Product 2", 149.99)
        );
    }

    /**
     * Handles wildcard Accept headers
     * Accept: *\/* -> returns default (JSON)
     * Accept: application/* -> returns JSON or XML
     */
    @GetMapping(value = "/{id}", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_HTML_VALUE
    })
    public ProductInfo getProduct(@PathVariable Long id) {
        return new ProductInfo(id, "Product " + id, 99.99);
    }
}

/**
 * Service to inspect and log Accept header
 */
@RestController
@RequestMapping("/api/accept")
class AcceptHeaderInspector {

    /**
     * Returns information about the Accept header
     */
    @GetMapping("/inspect")
    public Map<String, Object> inspectAcceptHeader(HttpServletRequest request) {
        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        
        return Map.of(
                "acceptHeader", acceptHeader != null ? acceptHeader : "Not provided",
                "parsedTypes", parseAcceptHeader(acceptHeader),
                "preferredType", determinePreferredType(acceptHeader)
        );
    }

    /**
     * Parse Accept header into individual media types
     */
    private List<String> parseAcceptHeader(String acceptHeader) {
        if (acceptHeader == null || acceptHeader.isEmpty()) {
            return List.of("*/*");
        }
        return Arrays.asList(acceptHeader.split(",\\s*"));
    }

    /**
     * Determine preferred media type from Accept header
     */
    private String determinePreferredType(String acceptHeader) {
        if (acceptHeader == null || acceptHeader.isEmpty()) {
            return "application/json (default)";
        }
        
        // Simple implementation - in real scenario, parse quality values
        if (acceptHeader.contains("application/json")) {
            return "application/json";
        } else if (acceptHeader.contains("application/xml")) {
            return "application/xml";
        } else if (acceptHeader.contains("text/html")) {
            return "text/html";
        } else {
            return "application/json (default)";
        }
    }
}

/**
 * Controller demonstrating strict Accept header matching
 */
@RestController
@RequestMapping("/api/strict")
class StrictAcceptController {

    /**
     * Only accepts application/json
     * Returns 406 Not Acceptable for other Accept headers
     */
    @GetMapping(value = "/json-only", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> jsonOnly() {
        return Map.of("format", "json", "strict", "true");
    }

    /**
     * Only accepts application/xml
     */
    @GetMapping(value = "/xml-only", produces = MediaType.APPLICATION_XML_VALUE)
    public Map<String, String> xmlOnly() {
        return Map.of("format", "xml", "strict", "true");
    }

    /**
     * Only accepts text/plain
     */
    @GetMapping(value = "/text-only", produces = MediaType.TEXT_PLAIN_VALUE)
    public String textOnly() {
        return "Plain text response";
    }
}

/**
 * Controller demonstrating custom media types in Accept header
 */
@RestController
@RequestMapping("/api/v1/custom")
class CustomAcceptController {

    /**
     * Version 1 of API - custom media type
     * Accept: application/vnd.company.api-v1+json
     */
    @GetMapping(value = "/data", produces = "application/vnd.company.api-v1+json")
    public Map<String, Object> getDataV1() {
        return Map.of("version", "1.0", "data", "API v1 data");
    }

    /**
     * Version 2 of API - custom media type
     * Accept: application/vnd.company.api-v2+json
     */
    @GetMapping(value = "/data", produces = "application/vnd.company.api-v2+json")
    public Map<String, Object> getDataV2() {
        return Map.of("version", "2.0", "data", "API v2 data", "newField", "Added in v2");
    }

    /**
     * Handles both versions
     */
    @GetMapping(value = "/flexible", produces = {
            "application/vnd.company.api-v1+json",
            "application/vnd.company.api-v2+json",
            MediaType.APPLICATION_JSON_VALUE
    })
    public Map<String, Object> getDataFlexible(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader) {
        if (acceptHeader.contains("v2")) {
            return Map.of("version", "2.0", "data", "v2 data");
        } else {
            return Map.of("version", "1.0", "data", "v1 data");
        }
    }
}

/**
 * Domain models
 */
class Resource {
    private Long id;
    private String name;
    private String status;

    public Resource(Long id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

class ProductInfo {
    private Long id;
    private String name;
    private Double price;

    public ProductInfo(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

/**
 * Documentation:
 * 
 * Accept Header Format:
 * - Accept: application/json
 * - Accept: application/xml, application/json
 * - Accept: text/html;q=0.9, application/json;q=1.0
 * - Accept: *\/*
 * - Accept: application/*
 * 
 * Quality Values (q-factor):
 * - Range: 0.0 to 1.0 (default is 1.0)
 * - Higher value = higher preference
 * - Example: Accept: text/html;q=0.9, application/json;q=1.0
 * - JSON preferred due to q=1.0
 * 
 * Wildcard Handling:
 * - *\/* : Accepts any media type
 * - application/* : Accepts any application type
 * - text/* : Accepts any text type
 * 
 * Precedence Rules:
 * 1. Exact match beats wildcard
 * 2. More specific beats less specific
 * 3. Higher q-value beats lower q-value
 * 4. First in list if equal q-values
 * 
 * Best Practices:
 * - Always specify produces in @RequestMapping
 * - Support multiple formats when possible
 * - Use quality values to express preferences
 * - Provide default format for *\/*
 * - Return 406 for unsupported formats
 * - Use custom media types for versioning
 * - Document supported Accept headers
 * 
 * Common HTTP Status Codes:
 * - 200 OK: Successful response with requested format
 * - 406 Not Acceptable: Requested format not supported
 * - 415 Unsupported Media Type: For Content-Type issues
 * 
 * Advantages:
 * - RESTful and standard HTTP approach
 * - Client controls response format
 * - No URL pollution with format parameters
 * - Supports complex negotiations with q-values
 * - Cacheable with proper Vary header
 * 
 * Disadvantages:
 * - Not browser-friendly (can't easily change in address bar)
 * - Harder to test manually
 * - Requires HTTP client support
 * - More complex caching (need Vary: Accept header)
 */
