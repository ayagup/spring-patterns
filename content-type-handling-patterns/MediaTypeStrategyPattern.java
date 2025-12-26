package com.example.contenttypehandling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.accept.ParameterContentNegotiationStrategy;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Media Type Strategy Pattern
 * 
 * Demonstrates different strategies for content negotiation and media type resolution
 * in Spring MVC. This pattern allows flexible configuration of how clients and servers
 * agree on response format.
 * 
 * Key Concepts:
 * - Content Negotiation Manager
 * - Multiple negotiation strategies
 * - Accept header strategy
 * - Parameter-based strategy
 * - Path extension strategy
 * - Fixed content strategy
 * 
 * Use Cases:
 * - RESTful APIs with multiple response formats
 * - Browser-friendly content negotiation
 * - Legacy system integration
 * - API versioning through content types
 */
@SpringBootApplication
public class MediaTypeStrategyPattern {

    public static void main(String[] args) {
        SpringApplication.run(MediaTypeStrategyPattern.class, args);
    }
}

/**
 * Configuration for Content Negotiation using WebMvcConfigurer
 */
@Configuration
class ContentNegotiationConfig implements WebMvcConfigurer {

    /**
     * Configure content negotiation strategies
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                // Favor parameter-based negotiation
                .favorParameter(true)
                .parameterName("format")  // Use ?format=json or ?format=xml
                
                // Favor path extension (deprecated in recent versions)
                .favorPathExtension(false)  // Disable .json, .xml extensions
                
                // Use Accept header as fallback
                .ignoreAcceptHeader(false)
                
                // Default content type if none specified
                .defaultContentType(MediaType.APPLICATION_JSON)
                
                // Register custom media types
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xml", MediaType.APPLICATION_XML)
                .mediaType("html", MediaType.TEXT_HTML)
                .mediaType("pdf", MediaType.APPLICATION_PDF)
                .mediaType("csv", MediaType.valueOf("text/csv"));
    }

    /**
     * Configure message converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new MappingJackson2XmlHttpMessageConverter());
    }
}

/**
 * Header-based Content Negotiation Strategy Configuration
 */
@Configuration
class HeaderStrategyConfig {

    /**
     * Strategy that uses Accept header for content negotiation
     */
    @Bean
    public ContentNegotiationStrategy headerStrategy() {
        return new HeaderContentNegotiationStrategy();
    }

    /**
     * Content negotiation manager with header strategy
     */
    @Bean
    public ContentNegotiationManager headerBasedManager() {
        return new ContentNegotiationManager(headerStrategy());
    }
}

/**
 * Parameter-based Content Negotiation Strategy Configuration
 */
@Configuration
class ParameterStrategyConfig {

    /**
     * Strategy that uses query parameter for content negotiation
     */
    @Bean
    public ContentNegotiationStrategy parameterStrategy() {
        Map<String, MediaType> mediaTypes = new HashMap<>();
        mediaTypes.put("json", MediaType.APPLICATION_JSON);
        mediaTypes.put("xml", MediaType.APPLICATION_XML);
        mediaTypes.put("html", MediaType.TEXT_HTML);
        
        return new ParameterContentNegotiationStrategy(mediaTypes);
    }

    /**
     * Content negotiation manager with parameter strategy
     */
    @Bean
    public ContentNegotiationManager parameterBasedManager() {
        return new ContentNegotiationManager(parameterStrategy());
    }
}

/**
 * Fixed Content Negotiation Strategy Configuration
 */
@Configuration
class FixedStrategyConfig {

    /**
     * Strategy that always returns a fixed media type
     * Useful for specific endpoints that only support one format
     */
    @Bean
    public ContentNegotiationStrategy fixedJsonStrategy() {
        return new FixedContentNegotiationStrategy(MediaType.APPLICATION_JSON);
    }

    /**
     * Strategy for XML-only endpoints
     */
    @Bean
    public ContentNegotiationStrategy fixedXmlStrategy() {
        return new FixedContentNegotiationStrategy(MediaType.APPLICATION_XML);
    }

    /**
     * Content negotiation manager with fixed strategy
     */
    @Bean
    public ContentNegotiationManager fixedContentManager() {
        return new ContentNegotiationManager(fixedJsonStrategy());
    }
}

/**
 * Combined Content Negotiation Strategy Configuration
 */
@Configuration
class CombinedStrategyConfig {

    /**
     * Content negotiation manager with multiple strategies
     * Strategies are tried in order until one succeeds
     */
    @Bean
    public ContentNegotiationManager combinedManager() {
        // Create parameter strategy
        Map<String, MediaType> mediaTypes = new HashMap<>();
        mediaTypes.put("json", MediaType.APPLICATION_JSON);
        mediaTypes.put("xml", MediaType.APPLICATION_XML);
        ParameterContentNegotiationStrategy parameterStrategy = 
                new ParameterContentNegotiationStrategy(mediaTypes);
        
        // Create header strategy
        HeaderContentNegotiationStrategy headerStrategy = 
                new HeaderContentNegotiationStrategy();
        
        // Create fixed fallback strategy
        FixedContentNegotiationStrategy fixedStrategy = 
                new FixedContentNegotiationStrategy(MediaType.APPLICATION_JSON);
        
        // Combine strategies: try parameter first, then header, then fixed
        return new ContentNegotiationManager(
                Arrays.asList(parameterStrategy, headerStrategy, fixedStrategy)
        );
    }
}

/**
 * Controller demonstrating different content negotiation strategies
 */
@RestController
@RequestMapping("/api/data")
class ContentNegotiationController {

    /**
     * Endpoint supporting multiple formats via Accept header
     * Example: curl -H "Accept: application/json" http://localhost:8080/api/data/item/1
     */
    @GetMapping(value = "/item/{id}", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public DataItem getItem(@PathVariable Long id) {
        return new DataItem(id, "Item " + id, "Description for item " + id);
    }

    /**
     * Endpoint supporting parameter-based negotiation
     * Example: http://localhost:8080/api/data/items?format=json
     * Example: http://localhost:8080/api/data/items?format=xml
     */
    @GetMapping(value = "/items", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public List<DataItem> getItems(@RequestParam(required = false) String format) {
        return Arrays.asList(
                new DataItem(1L, "Item 1", "Description 1"),
                new DataItem(2L, "Item 2", "Description 2"),
                new DataItem(3L, "Item 3", "Description 3")
        );
    }

    /**
     * JSON-only endpoint using fixed strategy
     */
    @GetMapping(value = "/json-only", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getJsonOnly() {
        return Map.of("format", "json", "message", "This endpoint only returns JSON");
    }

    /**
     * XML-only endpoint using fixed strategy
     */
    @GetMapping(value = "/xml-only", produces = MediaType.APPLICATION_XML_VALUE)
    public Map<String, String> getXmlOnly() {
        return Map.of("format", "xml", "message", "This endpoint only returns XML");
    }
}

/**
 * Custom Content Negotiation Strategy
 */
class CustomHeaderStrategy implements ContentNegotiationStrategy {

    @Override
    public List<MediaType> resolveMediaTypes(org.springframework.web.context.request.NativeWebRequest webRequest) {
        String customHeader = webRequest.getHeader("X-Response-Format");
        
        if ("json".equalsIgnoreCase(customHeader)) {
            return Arrays.asList(MediaType.APPLICATION_JSON);
        } else if ("xml".equalsIgnoreCase(customHeader)) {
            return Arrays.asList(MediaType.APPLICATION_XML);
        } else if ("html".equalsIgnoreCase(customHeader)) {
            return Arrays.asList(MediaType.TEXT_HTML);
        }
        
        // Return default
        return Arrays.asList(MediaType.APPLICATION_JSON);
    }
}

/**
 * Configuration using custom strategy
 */
@Configuration
class CustomStrategyConfig {

    @Bean
    public ContentNegotiationManager customManager() {
        return new ContentNegotiationManager(new CustomHeaderStrategy());
    }
}

/**
 * Domain model
 */
class DataItem {
    private Long id;
    private String name;
    private String description;

    public DataItem(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

/**
 * Documentation:
 * 
 * Content Negotiation Strategies:
 * 
 * 1. HeaderContentNegotiationStrategy:
 *    - Uses Accept header to determine response format
 *    - Most RESTful approach
 *    - Example: Accept: application/json
 * 
 * 2. ParameterContentNegotiationStrategy:
 *    - Uses query parameter to determine format
 *    - Browser-friendly
 *    - Example: ?format=json
 * 
 * 3. PathExtensionContentNegotiationStrategy:
 *    - Uses file extension in URL
 *    - Deprecated in Spring 5.3+
 *    - Example: /api/item/1.json
 * 
 * 4. FixedContentNegotiationStrategy:
 *    - Always returns same media type
 *    - Used for specific endpoints
 * 
 * 5. Custom Strategy:
 *    - Implement ContentNegotiationStrategy interface
 *    - Define custom logic for media type resolution
 * 
 * Strategy Order:
 * - Strategies are tried in the order they are registered
 * - First successful strategy wins
 * - Common order: Parameter → Path Extension → Header → Fixed
 * 
 * Best Practices:
 * - Prefer Accept header for RESTful APIs
 * - Use parameter strategy for browser compatibility
 * - Disable path extension strategy (security risk)
 * - Always provide a default content type
 * - Document supported formats clearly
 * - Use fixed strategy for single-format endpoints
 * 
 * Configuration Options:
 * - favorParameter: Enable query parameter negotiation
 * - favorPathExtension: Enable path extension (deprecated)
 * - ignoreAcceptHeader: Disable Accept header
 * - defaultContentType: Fallback when no match found
 * - mediaType: Register custom media type mappings
 * 
 * Common Issues:
 * - 406 Not Acceptable: No converter for requested type
 * - Missing message converters
 * - Incorrect media type mappings
 * - Strategy order conflicts
 */
