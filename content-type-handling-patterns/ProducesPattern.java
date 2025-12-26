package com.example.contenttypehandling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Produces Pattern
 * 
 * Demonstrates the @Produces annotation pattern in Spring MVC for specifying
 * the media types that a controller method can produce in the response.
 * 
 * Key Concepts:
 * - @produces annotation specifies content types the endpoint can return
 * - Supports multiple media types
 * - Content negotiation based on Accept header
 * - Can be applied at class or method level
 * 
 * Use Cases:
 * - RESTful APIs returning different formats (JSON, XML, HTML)
 * - Content negotiation between client and server
 * - API versioning through media types
 * - Supporting multiple response formats
 */
@SpringBootApplication
public class ProducesPattern {

    public static void main(String[] args) {
        SpringApplication.run(ProducesPattern.class, args);
    }
}

/**
 * Controller demonstrating single produces type
 */
@RestController
@RequestMapping("/api/users")
class UserController {

    /**
     * Produces JSON only
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getUserAsJson(@PathVariable Long id) {
        return new User(id, "John Doe", "john@example.com");
    }

    /**
     * Produces XML only
     */
    @GetMapping(value = "/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public User getUserAsXml(@PathVariable Long id) {
        return new User(id, "Jane Smith", "jane@example.com");
    }

    /**
     * Produces plain text
     */
    @GetMapping(value = "/{id}/text", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getUserAsText(@PathVariable Long id) {
        return "User: John Doe (ID: " + id + ")";
    }

    /**
     * Produces HTML
     */
    @GetMapping(value = "/{id}/html", produces = MediaType.TEXT_HTML_VALUE)
    public String getUserAsHtml(@PathVariable Long id) {
        return "<html><body><h1>User: John Doe</h1><p>ID: " + id + "</p></body></html>";
    }
}

/**
 * Controller demonstrating multiple produces types with content negotiation
 */
@RestController
@RequestMapping("/api/products")
class ProductController {

    /**
     * Produces both JSON and XML based on Accept header
     * Client can request: Accept: application/json or Accept: application/xml
     */
    @GetMapping(value = "/{id}", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public Product getProduct(@PathVariable Long id) {
        return new Product(id, "Laptop", 999.99);
    }

    /**
     * Produces JSON, XML, or plain text
     */
    @GetMapping(value = "/list", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_PLAIN_VALUE
    })
    public List<Product> getProducts() {
        return List.of(
                new Product(1L, "Laptop", 999.99),
                new Product(2L, "Mouse", 29.99),
                new Product(3L, "Keyboard", 79.99)
        );
    }
}

/**
 * Controller with class-level produces annotation
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin
class ReportController {

    /**
     * Class-level produces - all methods default to JSON unless overridden
     */
    @GetMapping("/sales")
    public Map<String, Object> getSalesReport() {
        return Map.of(
                "totalSales", 150000,
                "period", "Q1 2024",
                "growth", "15%"
        );
    }

    /**
     * Override class-level produces with method-level
     */
    @GetMapping(value = "/sales/csv", produces = "text/csv")
    public String getSalesReportCsv() {
        return "Period,Total Sales,Growth\nQ1 2024,150000,15%";
    }
}

/**
 * Controller demonstrating custom media types
 */
@RestController
@RequestMapping("/api/v1/data")
class CustomMediaTypeController {

    /**
     * Custom media type with version
     */
    @GetMapping(value = "/item", produces = "application/vnd.company.item-v1+json")
    public Map<String, Object> getItemV1() {
        return Map.of("version", "1.0", "data", "Item data v1");
    }

    /**
     * Custom media type with different version
     */
    @GetMapping(value = "/item", produces = "application/vnd.company.item-v2+json")
    public Map<String, Object> getItemV2() {
        return Map.of("version", "2.0", "data", "Item data v2", "extraField", "New in v2");
    }

    /**
     * Vendor-specific media type
     */
    @GetMapping(value = "/resource", produces = "application/vnd.company.resource+json")
    public Map<String, Object> getResource() {
        return Map.of("type", "resource", "content", "Vendor-specific format");
    }
}

/**
 * Controller demonstrating produces with quality parameters
 */
@RestController
@RequestMapping("/api/content")
class ContentNegotiationController {

    /**
     * Prefer JSON over XML using quality parameters
     * JSON has default quality of 1.0, XML has 0.8
     */
    @GetMapping(value = "/data", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            "application/xml;q=0.8"
    })
    public Map<String, String> getData() {
        return Map.of("message", "Content negotiation example");
    }

    /**
     * Multiple formats with quality preferences
     */
    @GetMapping(value = "/document", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            "application/xml;q=0.9",
            "text/html;q=0.7",
            "text/plain;q=0.5"
    })
    public Map<String, String> getDocument() {
        return Map.of("title", "Document", "content", "Document content");
    }
}

/**
 * Domain model classes
 */
class User {
    private Long id;
    private String name;
    private String email;

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

class Product {
    private Long id;
    private String name;
    private Double price;

    public Product(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // Getters and setters
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
 * @produces Annotation:
 * - Specifies media types that a method can produce
 * - Applied to @RequestMapping or its shortcuts (@GetMapping, etc.)
 * - Can be used at class level (applies to all methods)
 * - Method-level overrides class-level
 * 
 * Content Negotiation:
 * - Based on Accept header in HTTP request
 * - Spring selects appropriate method based on requested media type
 * - If no match found, returns 406 Not Acceptable
 * 
 * Quality Parameters:
 * - Used to express preference between multiple media types
 * - Format: "media/type;q=0.8" where q is between 0 and 1
 * - Higher quality value = higher preference
 * 
 * Best Practices:
 * - Use standard media types when possible
 * - Provide multiple formats for flexibility
 * - Use custom media types for API versioning
 * - Document supported media types in API documentation
 * - Consider client capabilities when choosing formats
 * 
 * Common Media Types:
 * - APPLICATION_JSON_VALUE: "application/json"
 * - APPLICATION_XML_VALUE: "application/xml"
 * - TEXT_PLAIN_VALUE: "text/plain"
 * - TEXT_HTML_VALUE: "text/html"
 * - APPLICATION_PDF_VALUE: "application/pdf"
 * - IMAGE_JPEG_VALUE: "image/jpeg"
 * - IMAGE_PNG_VALUE: "image/png"
 */
