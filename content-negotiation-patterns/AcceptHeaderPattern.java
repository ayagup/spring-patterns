package com.example.contentnegotiation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ACCEPT HEADER PATTERN
 * =====================
 * 
 * Demonstrates content negotiation based on the Accept HTTP header.
 * The server inspects the client's Accept header to determine the response format.
 * 
 * Key Concepts:
 * - Accept header parsing and priority evaluation
 * - Quality value (q-value) based content selection
 * - Multiple media type support
 * - Wildcard media type handling
 * - Default fallback strategies
 * 
 * Use Cases:
 * - REST APIs supporting multiple response formats
 * - Browser vs API client differentiation
 * - Mobile vs desktop content adaptation
 * - Progressive enhancement strategies
 */

@SpringBootApplication
public class AcceptHeaderPattern {

    public static void main(String[] args) {
        SpringApplication.run(AcceptHeaderPattern.class, args);
        demonstrateAcceptHeaderNegotiation();
    }

    private static void demonstrateAcceptHeaderNegotiation() {
        System.out.println("=== Accept Header Pattern Demonstrations ===\n");

        // Demo 1: Quality value evaluation
        AcceptHeaderParser parser = new AcceptHeaderParser();
        String acceptHeader = "application/json;q=0.9, application/xml;q=0.8, text/html;q=1.0";
        System.out.println("1. Quality Value Parsing:");
        System.out.println("   Accept Header: " + acceptHeader);
        List<MediaTypePreference> preferences = parser.parse(acceptHeader);
        preferences.forEach(pref -> 
            System.out.println("   - " + pref.mediaType + " (quality: " + pref.quality + ")")
        );

        // Demo 2: Best match selection
        System.out.println("\n2. Best Match Selection:");
        List<String> supported = Arrays.asList("application/json", "application/xml", "text/plain");
        String bestMatch = parser.findBestMatch(acceptHeader, supported);
        System.out.println("   Supported formats: " + supported);
        System.out.println("   Best match: " + bestMatch);

        // Demo 3: Wildcard handling
        System.out.println("\n3. Wildcard Handling:");
        String wildcardAccept = "application/json, */*;q=0.5";
        System.out.println("   Accept Header: " + wildcardAccept);
        MediaTypePreference wildcard = parser.parse(wildcardAccept).stream()
            .filter(p -> p.mediaType.contains("*"))
            .findFirst()
            .orElse(null);
        if (wildcard != null) {
            System.out.println("   Wildcard found: " + wildcard.mediaType + " (quality: " + wildcard.quality + ")");
        }

        // Demo 4: Complex negotiation scenario
        System.out.println("\n4. Complex Negotiation Scenario:");
        String complexAccept = "text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8";
        System.out.println("   Client Accept: " + complexAccept);
        List<String> serverFormats = Arrays.asList("application/json", "application/xml", "text/plain");
        String selected = parser.findBestMatch(complexAccept, serverFormats);
        System.out.println("   Server Supports: " + serverFormats);
        System.out.println("   Selected Format: " + selected);
    }
}

// ============================================================================
// CONFIGURATION
// ============================================================================

@Configuration
class AcceptHeaderConfiguration implements WebMvcConfigurer {

    /**
     * Configure content negotiation strategy
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(false)  // Don't use format parameter
            .favorPathExtension(false)  // Don't use path extension
            .ignoreAcceptHeader(false)  // Use Accept header (default)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("html", MediaType.TEXT_HTML);
    }
}

// ============================================================================
// REST CONTROLLERS
// ============================================================================

@RestController
@RequestMapping("/api/products")
class ProductController {

    /**
     * Returns product based on Accept header
     * Supports: application/json, application/xml, text/plain
     */
    @GetMapping(value = "/{id}", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE,
        MediaType.TEXT_PLAIN_VALUE
    })
    public ResponseEntity<?> getProduct(
            @PathVariable Long id,
            @RequestHeader(value = "Accept", required = false) String acceptHeader) {
        
        Product product = new Product(id, "Sample Product", 99.99, "Electronics");
        
        System.out.println("Accept Header: " + acceptHeader);
        
        // Spring automatically negotiates based on Accept header
        return ResponseEntity.ok(product);
    }

    /**
     * JSON-only endpoint
     */
    @GetMapping(value = "/json/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Product getProductJson(@PathVariable Long id) {
        return new Product(id, "JSON Product", 149.99, "Technology");
    }

    /**
     * XML-only endpoint
     */
    @GetMapping(value = "/xml/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public Product getProductXml(@PathVariable Long id) {
        return new Product(id, "XML Product", 199.99, "Software");
    }

    /**
     * Multiple representations with explicit negotiation
     */
    @GetMapping("/negotiated")
    public ResponseEntity<?> getNegotiatedProduct(@RequestHeader("Accept") String accept) {
        Product product = new Product(1L, "Negotiated Product", 299.99, "Premium");
        
        if (accept.contains("application/json")) {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(product);
        } else if (accept.contains("application/xml")) {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(product);
        } else if (accept.contains("text/plain")) {
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(product.toString());
        }
        
        // Default fallback
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(product);
    }
}

@RestController
@RequestMapping("/api/users")
class UserController {

    /**
     * Automatic content negotiation with custom response wrapper
     */
    @GetMapping(value = "/{id}", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public ApiResponse<User> getUser(@PathVariable Long id) {
        User user = new User(id, "john.doe@example.com", "John", "Doe");
        return new ApiResponse<>(true, "User retrieved successfully", user, LocalDateTime.now());
    }

    /**
     * List endpoint with pagination metadata
     */
    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public PagedResponse<User> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<User> users = Arrays.asList(
            new User(1L, "user1@example.com", "Alice", "Smith"),
            new User(2L, "user2@example.com", "Bob", "Johnson"),
            new User(3L, "user3@example.com", "Charlie", "Brown")
        );
        
        return new PagedResponse<>(users, page, size, 3, 1);
    }
}

// ============================================================================
// ACCEPT HEADER PARSER
// ============================================================================

class AcceptHeaderParser {

    /**
     * Parse Accept header into media type preferences
     */
    public List<MediaTypePreference> parse(String acceptHeader) {
        if (acceptHeader == null || acceptHeader.isEmpty()) {
            return Collections.singletonList(new MediaTypePreference("*/*", 1.0));
        }

        List<MediaTypePreference> preferences = new ArrayList<>();
        String[] mediaTypes = acceptHeader.split(",");

        for (String mediaType : mediaTypes) {
            mediaType = mediaType.trim();
            double quality = 1.0;
            String type = mediaType;

            // Extract quality value if present
            if (mediaType.contains(";q=")) {
                String[] parts = mediaType.split(";q=");
                type = parts[0].trim();
                try {
                    quality = Double.parseDouble(parts[1].trim());
                } catch (NumberFormatException e) {
                    quality = 1.0;
                }
            } else if (mediaType.contains(";")) {
                // Handle other parameters
                type = mediaType.split(";")[0].trim();
            }

            preferences.add(new MediaTypePreference(type, quality));
        }

        // Sort by quality (descending) and specificity
        preferences.sort((a, b) -> {
            int qualityCompare = Double.compare(b.quality, a.quality);
            if (qualityCompare != 0) return qualityCompare;
            
            // More specific types take precedence
            return Integer.compare(getSpecificity(b.mediaType), getSpecificity(a.mediaType));
        });

        return preferences;
    }

    /**
     * Find best matching media type from supported formats
     */
    public String findBestMatch(String acceptHeader, List<String> supportedFormats) {
        List<MediaTypePreference> preferences = parse(acceptHeader);

        for (MediaTypePreference preference : preferences) {
            for (String supported : supportedFormats) {
                if (matches(preference.mediaType, supported)) {
                    return supported;
                }
            }
        }

        // Return first supported format as fallback
        return supportedFormats.isEmpty() ? null : supportedFormats.get(0);
    }

    /**
     * Check if media types match (including wildcards)
     */
    private boolean matches(String pattern, String mediaType) {
        if (pattern.equals("*/*")) return true;
        if (pattern.equals(mediaType)) return true;
        
        String[] patternParts = pattern.split("/");
        String[] typeParts = mediaType.split("/");
        
        if (patternParts.length != 2 || typeParts.length != 2) return false;
        
        if (patternParts[0].equals("*") || patternParts[0].equals(typeParts[0])) {
            if (patternParts[1].equals("*") || patternParts[1].equals(typeParts[1])) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Calculate specificity score for media type
     */
    private int getSpecificity(String mediaType) {
        if (mediaType.equals("*/*")) return 0;
        String[] parts = mediaType.split("/");
        if (parts.length != 2) return 0;
        
        int score = 0;
        if (!parts[0].equals("*")) score += 2;
        if (!parts[1].equals("*")) score += 1;
        return score;
    }
}

// ============================================================================
// ACCEPT HEADER PRIORITY MANAGER
// ============================================================================

class AcceptHeaderPriorityManager {

    private final Map<String, Double> defaultPriorities;

    public AcceptHeaderPriorityManager() {
        this.defaultPriorities = new HashMap<>();
        defaultPriorities.put("application/json", 0.9);
        defaultPriorities.put("application/xml", 0.8);
        defaultPriorities.put("text/html", 0.7);
        defaultPriorities.put("text/plain", 0.5);
    }

    /**
     * Get effective quality for a media type
     */
    public double getEffectiveQuality(String mediaType, double clientQuality) {
        Double serverPriority = defaultPriorities.getOrDefault(mediaType, 0.5);
        return clientQuality * serverPriority;
    }

    /**
     * Rank media types by combined client/server preference
     */
    public List<MediaTypePreference> rankMediaTypes(List<MediaTypePreference> clientPreferences) {
        List<MediaTypePreference> ranked = new ArrayList<>();
        
        for (MediaTypePreference pref : clientPreferences) {
            double effective = getEffectiveQuality(pref.mediaType, pref.quality);
            ranked.add(new MediaTypePreference(pref.mediaType, effective));
        }
        
        ranked.sort((a, b) -> Double.compare(b.quality, a.quality));
        return ranked;
    }
}

// ============================================================================
// DOMAIN MODELS
// ============================================================================

class Product {
    private Long id;
    private String name;
    private Double price;
    private String category;

    public Product() {}

    public Product(Long id, String name, Double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return String.format("Product[id=%d, name='%s', price=%.2f, category='%s']",
            id, name, price, category);
    }
}

class User {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;

    public User() {}

    public User(Long id, String email, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}

// ============================================================================
// RESPONSE WRAPPERS
// ============================================================================

class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data, LocalDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

class PagedResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    public PagedResponse() {}

    public PagedResponse(List<T> content, int pageNumber, int pageSize, 
                        long totalElements, int totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    // Getters and setters
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}

class MediaTypePreference {
    final String mediaType;
    final double quality;

    MediaTypePreference(String mediaType, double quality) {
        this.mediaType = mediaType;
        this.quality = quality;
    }
}

/*
 * BEST PRACTICES:
 * ===============
 * 1. Always provide a default content type
 * 2. Support at least JSON and XML for REST APIs
 * 3. Use quality values appropriately in responses
 * 4. Handle wildcard Accept headers gracefully
 * 5. Document supported media types in API docs
 * 6. Consider caching negotiated content
 * 7. Log content negotiation decisions
 * 8. Test with various Accept header combinations
 * 
 * COMMON PITFALLS:
 * ================
 * 1. Not handling missing Accept headers
 * 2. Ignoring quality values in negotiation
 * 3. Poor fallback strategies
 * 4. Not supporting wildcards properly
 * 5. Inconsistent media type handling across endpoints
 * 
 * TESTING SCENARIOS:
 * ==================
 * curl -H "Accept: application/json" http://localhost:8080/api/products/1
 * curl -H "Accept: application/xml" http://localhost:8080/api/products/1
 * curl -H "Accept: text/plain" http://localhost:8080/api/products/1
 * curl -H "Accept: application/json;q=0.9, application/xml;q=1.0" http://localhost:8080/api/products/1
 * curl -H "Accept: */*" http://localhost:8080/api/products/1
 */
