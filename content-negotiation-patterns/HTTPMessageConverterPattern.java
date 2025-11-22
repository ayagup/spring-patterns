package com.spring.patterns.contentnegotiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * HTTP Message Converter Pattern
 * 
 * Demonstrates comprehensive usage of Spring's HttpMessageConverter system
 * for converting HTTP request/response bodies to/from Java objects.
 * 
 * Message converters handle:
 * - Request body deserialization (JSON -> Object)
 * - Response body serialization (Object -> JSON)
 * - Content type negotiation
 * - Character encoding
 * - Media type support
 */

// ===================== Domain Models =====================

record User(
    Long id,
    String username,
    String email,
    LocalDateTime createdAt
) {}

record Order(
    Long id,
    Long userId,
    List<OrderItem> items,
    Double totalAmount,
    OrderStatus status,
    LocalDateTime orderDate
) {}

record OrderItem(
    Long productId,
    String productName,
    Integer quantity,
    Double price
) {}

enum OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}

record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timestamp,
    Metadata metadata
) {}

record Metadata(
    String version,
    String requestId,
    Long processingTimeMs
) {}

// ===================== JSON Message Converter Configuration =====================

@Configuration
class JsonMessageConverterConfig {
    
    /**
     * Customize default JSON converter with ObjectMapper
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = 
            new MappingJackson2HttpMessageConverter();
        
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
            .json()
            .indentOutput(true)
            .dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))
            .timeZone(TimeZone.getDefault())
            .build();
        
        converter.setObjectMapper(objectMapper);
        
        // Configure supported media types
        converter.setSupportedMediaTypes(List.of(
            MediaType.APPLICATION_JSON,
            new MediaType("application", "vnd.api+json"),
            new MediaType("application", "*+json")
        ));
        
        return converter;
    }
}

// ===================== XML Message Converter Configuration =====================

@Configuration
class XmlMessageConverterConfig {
    
    /**
     * Configure XML message converter
     */
    @Bean
    public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter() {
        MappingJackson2XmlHttpMessageConverter converter = 
            new MappingJackson2XmlHttpMessageConverter();
        
        ObjectMapper xmlMapper = Jackson2ObjectMapperBuilder
            .xml()
            .indentOutput(true)
            .dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))
            .build();
        
        converter.setObjectMapper(xmlMapper);
        
        // Configure supported media types
        converter.setSupportedMediaTypes(List.of(
            MediaType.APPLICATION_XML,
            MediaType.TEXT_XML,
            new MediaType("application", "*+xml")
        ));
        
        return converter;
    }
}

// ===================== String Message Converter Configuration =====================

@Configuration
class StringMessageConverterConfig {
    
    /**
     * Configure string message converter with UTF-8
     */
    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        StringHttpMessageConverter converter = 
            new StringHttpMessageConverter(StandardCharsets.UTF_8);
        
        // Support text media types
        converter.setSupportedMediaTypes(List.of(
            MediaType.TEXT_PLAIN,
            MediaType.TEXT_HTML,
            new MediaType("text", "*")
        ));
        
        return converter;
    }
}

// ===================== Binary Message Converter Configuration =====================

@Configuration
class BinaryMessageConverterConfig {
    
    /**
     * Configure byte array message converter
     */
    @Bean
    public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
        ByteArrayHttpMessageConverter converter = 
            new ByteArrayHttpMessageConverter();
        
        // Support binary media types
        converter.setSupportedMediaTypes(List.of(
            MediaType.APPLICATION_OCTET_STREAM,
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            MediaType.APPLICATION_PDF
        ));
        
        return converter;
    }
}

// ===================== Resource Message Converter Configuration =====================

@Configuration
class ResourceMessageConverterConfig {
    
    /**
     * Configure resource message converter for file downloads
     */
    @Bean
    public ResourceHttpMessageConverter resourceHttpMessageConverter() {
        ResourceHttpMessageConverter converter = 
            new ResourceHttpMessageConverter();
        
        converter.setSupportedMediaTypes(List.of(
            MediaType.ALL
        ));
        
        return converter;
    }
}

// ===================== Global Converter Configuration =====================

@Configuration
class MessageConverterConfig implements WebMvcConfigurer {
    
    private final MappingJackson2HttpMessageConverter jsonConverter;
    private final MappingJackson2XmlHttpMessageConverter xmlConverter;
    private final StringHttpMessageConverter stringConverter;
    private final ByteArrayHttpMessageConverter byteArrayConverter;
    private final ResourceHttpMessageConverter resourceConverter;
    
    public MessageConverterConfig(
            MappingJackson2HttpMessageConverter jsonConverter,
            MappingJackson2XmlHttpMessageConverter xmlConverter,
            StringHttpMessageConverter stringConverter,
            ByteArrayHttpMessageConverter byteArrayConverter,
            ResourceHttpMessageConverter resourceConverter
    ) {
        this.jsonConverter = jsonConverter;
        this.xmlConverter = xmlConverter;
        this.stringConverter = stringConverter;
        this.byteArrayConverter = byteArrayConverter;
        this.resourceConverter = resourceConverter;
    }
    
    /**
     * Configure message converters in specific order
     * Order matters - first matching converter wins
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Clear default converters
        converters.clear();
        
        // Add converters in priority order
        converters.add(byteArrayConverter);      // 1. Binary data
        converters.add(stringConverter);          // 2. String/text
        converters.add(resourceConverter);        // 3. Resources/files
        converters.add(jsonConverter);            // 4. JSON (most common)
        converters.add(xmlConverter);             // 5. XML
    }
    
    /**
     * Extend (don't replace) default converters
     * Use this when you want to keep Spring's defaults
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Find and customize existing JSON converter
        converters.stream()
            .filter(MappingJackson2HttpMessageConverter.class::isInstance)
            .map(MappingJackson2HttpMessageConverter.class::cast)
            .findFirst()
            .ifPresent(converter -> {
                // Customize converter
                List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
                mediaTypes.add(new MediaType("application", "vnd.custom+json"));
                converter.setSupportedMediaTypes(mediaTypes);
            });
    }
}

// ===================== REST Controller Examples =====================

@RestController
@RequestMapping("/api/converter")
class MessageConverterController {
    
    /**
     * Automatic JSON serialization/deserialization
     * 
     * POST /api/converter/users
     * Content-Type: application/json
     * Body: {"id":1,"username":"john","email":"john@example.com","createdAt":"2024-01-15T10:30:00"}
     */
    @PostMapping("/users")
    public ApiResponse<User> createUser(@RequestBody User user) {
        return new ApiResponse<>(
            true,
            "User created successfully",
            user,
            LocalDateTime.now(),
            new Metadata("v1", "req-123", 45L)
        );
    }
    
    /**
     * XML support with Accept header
     * 
     * GET /api/converter/users/1
     * Accept: application/xml
     */
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return new User(
            id,
            "john_doe",
            "john@example.com",
            LocalDateTime.now()
        );
    }
    
    /**
     * Complex object serialization
     * 
     * POST /api/converter/orders
     * Content-Type: application/json
     */
    @PostMapping("/orders")
    public ApiResponse<Order> createOrder(@RequestBody Order order) {
        return new ApiResponse<>(
            true,
            "Order created successfully",
            order,
            LocalDateTime.now(),
            new Metadata("v1", "req-124", 67L)
        );
    }
    
    /**
     * List serialization
     * 
     * GET /api/converter/orders
     * Accept: application/json
     */
    @GetMapping("/orders")
    public List<Order> getOrders() {
        return List.of(
            new Order(
                1L,
                101L,
                List.of(
                    new OrderItem(1L, "Laptop", 1, 999.99),
                    new OrderItem(2L, "Mouse", 2, 29.99)
                ),
                1059.97,
                OrderStatus.CONFIRMED,
                LocalDateTime.now()
            ),
            new Order(
                2L,
                102L,
                List.of(
                    new OrderItem(3L, "Keyboard", 1, 79.99)
                ),
                79.99,
                OrderStatus.PENDING,
                LocalDateTime.now()
            )
        );
    }
    
    /**
     * Plain text response
     * 
     * GET /api/converter/text/hello
     * Accept: text/plain
     */
    @GetMapping(value = "/text/{message}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getTextMessage(@PathVariable String message) {
        return "Echo: " + message;
    }
    
    /**
     * Binary data handling
     * 
     * POST /api/converter/upload
     * Content-Type: application/octet-stream
     */
    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ApiResponse<String> uploadBinary(@RequestBody byte[] data) {
        return new ApiResponse<>(
            true,
            "Binary data uploaded",
            "Received " + data.length + " bytes",
            LocalDateTime.now(),
            new Metadata("v1", "req-125", 23L)
        );
    }
}

// ===================== Advanced Converter Examples =====================

@RestController
@RequestMapping("/api/advanced-converter")
class AdvancedConverterController {
    
    /**
     * Multiple media type support
     * Client can request JSON or XML
     * 
     * GET /api/advanced-converter/data/1
     * Accept: application/json (or application/xml)
     */
    @GetMapping(
        value = "/data/{id}",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public Order getData(@PathVariable Long id) {
        return new Order(
            id,
            101L,
            List.of(new OrderItem(1L, "Product", 1, 99.99)),
            99.99,
            OrderStatus.PENDING,
            LocalDateTime.now()
        );
    }
    
    /**
     * Content negotiation with custom media types
     * 
     * GET /api/advanced-converter/custom/1
     * Accept: application/vnd.api+json
     */
    @GetMapping(
        value = "/custom/{id}",
        produces = "application/vnd.api+json"
    )
    public User getCustomFormat(@PathVariable Long id) {
        return new User(id, "user" + id, "user" + id + "@example.com", LocalDateTime.now());
    }
    
    /**
     * Consuming multiple formats
     * 
     * POST /api/advanced-converter/process
     * Content-Type: application/json (or application/xml)
     */
    @PostMapping(
        value = "/process",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ApiResponse<String> processData(@RequestBody User user) {
        return new ApiResponse<>(
            true,
            "Processed user: " + user.username(),
            "User ID: " + user.id(),
            LocalDateTime.now(),
            new Metadata("v1", "req-126", 34L)
        );
    }
}

/**
 * Key Concepts Demonstrated:
 * 
 * 1. Message Converter Types:
 *    - MappingJackson2HttpMessageConverter (JSON)
 *    - MappingJackson2XmlHttpMessageConverter (XML)
 *    - StringHttpMessageConverter (Text)
 *    - ByteArrayHttpMessageConverter (Binary)
 *    - ResourceHttpMessageConverter (Files)
 * 
 * 2. Converter Configuration:
 *    - configureMessageConverters() - Replace defaults
 *    - extendMessageConverters() - Extend defaults
 *    - Converter ordering matters
 * 
 * 3. Media Type Support:
 *    - Configure supported media types per converter
 *    - Custom media types (vnd.api+json)
 *    - Wildcard patterns (*+json, *+xml)
 * 
 * 4. ObjectMapper Customization:
 *    - Date formatting
 *    - Pretty printing
 *    - Timezone handling
 *    - Custom serializers/deserializers
 * 
 * 5. Content Negotiation:
 *    - Accept header for response format
 *    - Content-Type header for request format
 *    - produces/consumes attributes
 * 
 * 6. Character Encoding:
 *    - UTF-8 for string converters
 *    - Proper charset handling
 * 
 * 7. Request/Response Processing:
 *    - Automatic deserialization with @RequestBody
 *    - Automatic serialization of return values
 *    - Type-safe conversion
 * 
 * 8. Use Cases:
 *    - REST API endpoints
 *    - Multi-format support (JSON/XML)
 *    - Binary file uploads/downloads
 *    - Custom serialization formats
 *    - Legacy system integration
 * 
 * Testing with curl:
 * 
 * # JSON request/response
 * curl -X POST http://localhost:8080/api/converter/users \
 *   -H "Content-Type: application/json" \
 *   -d '{"id":1,"username":"john","email":"john@example.com","createdAt":"2024-01-15T10:30:00"}'
 * 
 * # XML response
 * curl -X GET http://localhost:8080/api/converter/users/1 \
 *   -H "Accept: application/xml"
 * 
 * # Text response
 * curl -X GET http://localhost:8080/api/converter/text/hello \
 *   -H "Accept: text/plain"
 * 
 * # Binary upload
 * curl -X POST http://localhost:8080/api/converter/upload \
 *   -H "Content-Type: application/octet-stream" \
 *   --data-binary @file.bin
 * 
 * # Content negotiation
 * curl -X GET http://localhost:8080/api/advanced-converter/data/1 \
 *   -H "Accept: application/json"
 * 
 * curl -X GET http://localhost:8080/api/advanced-converter/data/1 \
 *   -H "Accept: application/xml"
 * 
 * Best Practices:
 * 
 * 1. Use extendMessageConverters() to preserve defaults
 * 2. Order converters by specificity (most specific first)
 * 3. Configure UTF-8 for string converters
 * 4. Use Jackson2ObjectMapperBuilder for consistent configuration
 * 5. Support multiple formats when appropriate
 * 6. Use proper media types (not just application/json)
 * 7. Handle character encoding properly
 * 8. Test with different Accept/Content-Type headers
 */
