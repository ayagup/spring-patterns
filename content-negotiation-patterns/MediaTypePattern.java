package com.example.contentnegotiation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MEDIA TYPE PATTERN
 * ===================
 * 
 * Demonstrates comprehensive media type handling and management strategies.
 * Covers custom media types, vendor-specific types, and versioning through media types.
 * 
 * Key Concepts:
 * - Standard media types (application/json, application/xml, etc.)
 * - Custom/vendor-specific media types
 * - Media type versioning
 * - Structured syntax suffix (+json, +xml)
 * - Media type parameters
 * - Quality factors and precedence
 * 
 * Use Cases:
 * - API versioning through media types
 * - Custom data format support
 * - Vendor-specific API extensions
 * - Structured data with custom semantics
 * - Backward compatibility management
 */

@SpringBootApplication
public class MediaTypePattern {

    public static void main(String[] args) {
        SpringApplication.run(MediaTypePattern.class, args);
        demonstrateMediaTypes();
    }

    private static void demonstrateMediaTypes() {
        System.out.println("=== Media Type Pattern Demonstrations ===\n");

        // Demo 1: Standard media types
        System.out.println("1. Standard Media Types:");
        MediaTypeRegistry registry = new MediaTypeRegistry();
        registry.getStandardTypes().forEach((name, type) -> 
            System.out.println("   " + name + ": " + type)
        );

        // Demo 2: Custom vendor media types
        System.out.println("\n2. Custom Vendor Media Types:");
        List<String> customTypes = Arrays.asList(
            "application/vnd.company.api+json",
            "application/vnd.company.api.v1+json",
            "application/vnd.company.api.v2+json",
            "application/vnd.github.v3+json",
            "application/vnd.api+json"
        );
        customTypes.forEach(type -> {
            MediaTypeInfo info = MediaTypeAnalyzer.analyze(type);
            System.out.println("   " + type);
            System.out.println("      Vendor: " + info.vendor);
            System.out.println("      Version: " + info.version);
            System.out.println("      Suffix: " + info.suffix);
        });

        // Demo 3: Media type comparison
        System.out.println("\n3. Media Type Comparison:");
        MediaTypeComparator comparator = new MediaTypeComparator();
        String type1 = "application/json";
        String type2 = "application/vnd.api+json";
        System.out.println("   Comparing: " + type1 + " vs " + type2);
        System.out.println("   Compatible: " + comparator.areCompatible(type1, type2));
        System.out.println("   More specific: " + comparator.moreSpecific(type1, type2));

        // Demo 4: Media type selection
        System.out.println("\n4. Media Type Selection:");
        MediaTypeSelector selector = new MediaTypeSelector();
        List<String> supported = Arrays.asList(
            "application/json",
            "application/xml",
            "application/vnd.company.api.v1+json",
            "application/vnd.company.api.v2+json"
        );
        String accept = "application/vnd.company.api.v2+json, application/json;q=0.9";
        String selected = selector.selectBestMatch(accept, supported);
        System.out.println("   Accept: " + accept);
        System.out.println("   Supported: " + supported);
        System.out.println("   Selected: " + selected);
    }
}

// ============================================================================
// CONFIGURATION
// ============================================================================

@Configuration
class MediaTypeConfiguration implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(true)
            .parameterName("mediaType")
            .favorPathExtension(false)
            .ignoreAcceptHeader(false)
            .useRegisteredExtensionsOnly(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            // Standard types
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("html", MediaType.TEXT_HTML)
            .mediaType("pdf", MediaType.APPLICATION_PDF);
            // Custom vendor types registered in controller
    }

    /**
     * Register custom media type constants
     */
    @Bean
    public MediaTypeRegistry mediaTypeRegistry() {
        return new MediaTypeRegistry();
    }
}

// ============================================================================
// REST CONTROLLERS
// ============================================================================

@RestController
@RequestMapping("/api/v1/resources")
class MediaTypeV1Controller {

    /**
     * Version 1 API using vendor-specific media type
     */
    @GetMapping(value = "/{id}", produces = "application/vnd.company.api.v1+json")
    public ResponseEntity<ResourceV1> getResourceV1(@PathVariable Long id) {
        ResourceV1 resource = new ResourceV1(id, "Resource V1", "Version 1 data");
        return ResponseEntity.ok(resource);
    }

    /**
     * Version 1 collection endpoint
     */
    @GetMapping(produces = "application/vnd.company.api.v1+json")
    public ResponseEntity<List<ResourceV1>> getResourcesV1() {
        List<ResourceV1> resources = Arrays.asList(
            new ResourceV1(1L, "Resource 1", "Data 1"),
            new ResourceV1(2L, "Resource 2", "Data 2")
        );
        return ResponseEntity.ok(resources);
    }
}

@RestController
@RequestMapping("/api/v2/resources")
class MediaTypeV2Controller {

    /**
     * Version 2 API with enhanced media type
     */
    @GetMapping(value = "/{id}", produces = "application/vnd.company.api.v2+json")
    public ResponseEntity<ResourceV2> getResourceV2(@PathVariable Long id) {
        ResourceV2 resource = new ResourceV2(
            id, 
            "Resource V2", 
            "Version 2 enhanced data",
            LocalDateTime.now(),
            Map.of("extra", "metadata")
        );
        return ResponseEntity.ok(resource);
    }

    /**
     * Version 2 with backward compatibility
     */
    @GetMapping(produces = {
        "application/vnd.company.api.v2+json",
        "application/vnd.company.api.v1+json",
        MediaType.APPLICATION_JSON_VALUE
    })
    public ResponseEntity<?> getResources(@RequestHeader("Accept") String accept) {
        if (accept.contains("v2")) {
            return ResponseEntity.ok(Arrays.asList(
                new ResourceV2(1L, "R1", "Data", LocalDateTime.now(), Map.of("v", "2")),
                new ResourceV2(2L, "R2", "Data", LocalDateTime.now(), Map.of("v", "2"))
            ));
        } else {
            return ResponseEntity.ok(Arrays.asList(
                new ResourceV1(1L, "R1", "Data"),
                new ResourceV1(2L, "R2", "Data")
            ));
        }
    }
}

@RestController
@RequestMapping("/api/media")
class CustomMediaTypeController {

    /**
     * Support multiple custom media types
     */
    @GetMapping(value = "/data", produces = {
        "application/json",
        "application/xml",
        "application/vnd.company.data+json",
        "application/vnd.company.data+xml"
    })
    public ResponseEntity<DataResource> getData() {
        return ResponseEntity.ok(new DataResource("sample-data", "Custom format data"));
    }

    /**
     * Collection+JSON format
     */
    @GetMapping(value = "/collection", produces = "application/vnd.collection+json")
    public ResponseEntity<CollectionResponse> getCollection() {
        CollectionResponse response = new CollectionResponse();
        response.version = "1.0";
        response.items = Arrays.asList(
            Map.of("id", "1", "name", "Item 1"),
            Map.of("id", "2", "name", "Item 2")
        );
        response.links = Arrays.asList(
            Map.of("rel", "self", "href", "/api/media/collection")
        );
        return ResponseEntity.ok(response);
    }

    /**
     * HAL+JSON format
     */
    @GetMapping(value = "/hal", produces = "application/hal+json")
    public ResponseEntity<HalResource> getHalResource() {
        HalResource resource = new HalResource();
        resource.data = Map.of("id", 1, "name", "HAL Resource");
        resource.links = Map.of(
            "self", "/api/media/hal",
            "related", "/api/media/hal/related"
        );
        return ResponseEntity.ok(resource);
    }

    /**
     * JSON-LD format
     */
    @GetMapping(value = "/jsonld", produces = "application/ld+json")
    public ResponseEntity<JsonLdResource> getJsonLd() {
        JsonLdResource resource = new JsonLdResource();
        resource.context = "https://schema.org";
        resource.type = "Product";
        resource.data = Map.of(
            "name", "Product Name",
            "description", "Product Description"
        );
        return ResponseEntity.ok(resource);
    }

    /**
     * Problem Details (RFC 7807)
     */
    @GetMapping(value = "/problem", produces = "application/problem+json")
    public ResponseEntity<ProblemDetails> getProblemDetails() {
        ProblemDetails problem = new ProblemDetails(
            "https://api.company.com/problems/resource-not-found",
            "Resource Not Found",
            404,
            "The requested resource was not found",
            "/api/media/problem"
        );
        return ResponseEntity.status(404).body(problem);
    }
}

// ============================================================================
// MEDIA TYPE REGISTRY
// ============================================================================

class MediaTypeRegistry {

    private final Map<String, String> standardTypes = new LinkedHashMap<>();
    private final Map<String, String> customTypes = new LinkedHashMap<>();

    public MediaTypeRegistry() {
        initializeStandardTypes();
        initializeCustomTypes();
    }

    private void initializeStandardTypes() {
        standardTypes.put("JSON", "application/json");
        standardTypes.put("XML", "application/xml");
        standardTypes.put("HTML", "text/html");
        standardTypes.put("PLAIN", "text/plain");
        standardTypes.put("PDF", "application/pdf");
        standardTypes.put("CSV", "text/csv");
        standardTypes.put("FORM", "application/x-www-form-urlencoded");
        standardTypes.put("MULTIPART", "multipart/form-data");
        standardTypes.put("OCTET_STREAM", "application/octet-stream");
    }

    private void initializeCustomTypes() {
        customTypes.put("API_V1_JSON", "application/vnd.company.api.v1+json");
        customTypes.put("API_V2_JSON", "application/vnd.company.api.v2+json");
        customTypes.put("HAL_JSON", "application/hal+json");
        customTypes.put("JSON_LD", "application/ld+json");
        customTypes.put("COLLECTION_JSON", "application/vnd.collection+json");
        customTypes.put("PROBLEM_JSON", "application/problem+json");
        customTypes.put("JSON_API", "application/vnd.api+json");
        customTypes.put("GITHUB_V3", "application/vnd.github.v3+json");
    }

    public Map<String, String> getStandardTypes() {
        return Collections.unmodifiableMap(standardTypes);
    }

    public Map<String, String> getCustomTypes() {
        return Collections.unmodifiableMap(customTypes);
    }

    public String getType(String key) {
        String type = standardTypes.get(key);
        return type != null ? type : customTypes.get(key);
    }
}

// ============================================================================
// MEDIA TYPE ANALYZER
// ============================================================================

class MediaTypeAnalyzer {

    /**
     * Analyze media type and extract components
     */
    public static MediaTypeInfo analyze(String mediaType) {
        MediaTypeInfo info = new MediaTypeInfo();
        
        if (mediaType == null || !mediaType.contains("/")) {
            return info;
        }

        String[] parts = mediaType.split(";")[0].trim().split("/");
        if (parts.length != 2) return info;

        info.type = parts[0].trim();
        String subtype = parts[1].trim();

        // Extract suffix (e.g., +json, +xml)
        if (subtype.contains("+")) {
            String[] subtypeParts = subtype.split("\\+");
            info.tree = subtypeParts[0];
            info.suffix = subtypeParts[1];
        } else {
            info.tree = subtype;
        }

        // Extract vendor from tree (vnd.company.api.v1)
        if (info.tree.startsWith("vnd.")) {
            String[] treeParts = info.tree.split("\\.");
            if (treeParts.length >= 2) {
                info.vendor = treeParts[1];
            }
            
            // Extract version
            for (String part : treeParts) {
                if (part.matches("v\\d+")) {
                    info.version = part;
                }
            }
        }

        info.original = mediaType;
        return info;
    }
}

class MediaTypeInfo {
    String original;
    String type;
    String tree;
    String suffix;
    String vendor;
    String version;

    @Override
    public String toString() {
        return String.format("MediaTypeInfo[type=%s, tree=%s, suffix=%s, vendor=%s, version=%s]",
            type, tree, suffix, vendor, version);
    }
}

// ============================================================================
// MEDIA TYPE COMPARATOR
// ============================================================================

class MediaTypeComparator {

    /**
     * Check if two media types are compatible
     */
    public boolean areCompatible(String type1, String type2) {
        MediaTypeInfo info1 = MediaTypeAnalyzer.analyze(type1);
        MediaTypeInfo info2 = MediaTypeAnalyzer.analyze(type2);

        // Same suffix means compatible representation
        if (info1.suffix != null && info1.suffix.equals(info2.suffix)) {
            return true;
        }

        // Exact match
        return type1.equals(type2);
    }

    /**
     * Determine which type is more specific
     */
    public String moreSpecific(String type1, String type2) {
        if (type1.equals("*/*")) return type2;
        if (type2.equals("*/*")) return type1;

        MediaTypeInfo info1 = MediaTypeAnalyzer.analyze(type1);
        MediaTypeInfo info2 = MediaTypeAnalyzer.analyze(type2);

        // Vendor-specific is more specific than generic
        if (info1.vendor != null && info2.vendor == null) return type1;
        if (info2.vendor != null && info1.vendor == null) return type2;

        // Versioned is more specific than unversioned
        if (info1.version != null && info2.version == null) return type1;
        if (info2.version != null && info1.version == null) return type2;

        return type1; // Default to first
    }
}

// ============================================================================
// MEDIA TYPE SELECTOR
// ============================================================================

class MediaTypeSelector {

    /**
     * Select best matching media type
     */
    public String selectBestMatch(String acceptHeader, List<String> supportedTypes) {
        if (acceptHeader == null || acceptHeader.isEmpty()) {
            return supportedTypes.isEmpty() ? null : supportedTypes.get(0);
        }

        List<MediaTypePreference> preferences = parseAcceptHeader(acceptHeader);
        
        for (MediaTypePreference pref : preferences) {
            for (String supported : supportedTypes) {
                if (matches(pref.mediaType, supported)) {
                    return supported;
                }
            }
        }

        return supportedTypes.isEmpty() ? null : supportedTypes.get(0);
    }

    private List<MediaTypePreference> parseAcceptHeader(String acceptHeader) {
        List<MediaTypePreference> prefs = new ArrayList<>();
        
        for (String part : acceptHeader.split(",")) {
            part = part.trim();
            double quality = 1.0;
            String type = part;
            
            if (part.contains(";q=")) {
                String[] qParts = part.split(";q=");
                type = qParts[0].trim();
                quality = Double.parseDouble(qParts[1].trim());
            }
            
            prefs.add(new MediaTypePreference(type, quality));
        }
        
        prefs.sort((a, b) -> Double.compare(b.quality, a.quality));
        return prefs;
    }

    private boolean matches(String requested, String supported) {
        if (requested.equals(supported)) return true;
        if (requested.equals("*/*")) return true;
        
        MediaTypeInfo req = MediaTypeAnalyzer.analyze(requested);
        MediaTypeInfo sup = MediaTypeAnalyzer.analyze(supported);
        
        // Match by suffix
        if (req.suffix != null && req.suffix.equals(sup.suffix)) {
            return true;
        }
        
        return false;
    }
}

class MediaTypePreference {
    final String mediaType;
    final double quality;

    MediaTypePreference(String mediaType, double quality) {
        this.mediaType = mediaType;
        this.quality = quality;
    }
}

// ============================================================================
// DOMAIN MODELS
// ============================================================================

class ResourceV1 {
    private Long id;
    private String name;
    private String data;

    public ResourceV1() {}

    public ResourceV1(Long id, String name, String data) {
        this.id = id;
        this.name = name;
        this.data = data;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}

class ResourceV2 {
    private Long id;
    private String name;
    private String data;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;

    public ResourceV2() {}

    public ResourceV2(Long id, String name, String data, LocalDateTime timestamp, 
                     Map<String, Object> metadata) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}

class DataResource {
    private String id;
    private String content;

    public DataResource() {}

    public DataResource(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

class CollectionResponse {
    String version;
    List<Map<String, String>> items;
    List<Map<String, String>> links;

    // Getters and setters omitted for brevity
}

class HalResource {
    Map<String, Object> data;
    Map<String, String> links;

    // Getters and setters omitted for brevity
}

class JsonLdResource {
    String context;
    String type;
    Map<String, Object> data;

    // Getters and setters omitted for brevity
}

class ProblemDetails {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;

    public ProblemDetails() {}

    public ProblemDetails(String type, String title, int status, String detail, String instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getInstance() { return instance; }
    public void setInstance(String instance) { this.instance = instance; }
}

/*
 * BEST PRACTICES:
 * ===============
 * 1. Use vendor-specific media types for custom APIs
 * 2. Version APIs through media types (vnd.company.api.v1+json)
 * 3. Use structured syntax suffixes (+json, +xml)
 * 4. Support standard formats alongside custom ones
 * 5. Document all custom media types
 * 6. Follow RFC 6838 for media type registration
 * 7. Use meaningful vendor tree prefixes
 * 8. Maintain backward compatibility
 * 
 * COMMON PITFALLS:
 * ================
 * 1. Creating non-standard media types without documentation
 * 2. Not following vendor tree naming conventions
 * 3. Overusing custom media types
 * 4. Breaking backward compatibility
 * 5. Not registering custom types with IANA when appropriate
 * 
 * STANDARD MEDIA TYPE FORMATS:
 * ============================
 * - application/json - Standard JSON
 * - application/hal+json - HAL (Hypertext Application Language)
 * - application/vnd.api+json - JSON:API specification
 * - application/ld+json - JSON-LD (Linked Data)
 * - application/problem+json - RFC 7807 Problem Details
 * - application/vnd.collection+json - Collection+JSON
 */
