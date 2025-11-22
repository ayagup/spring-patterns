package com.spring.patterns.contentnegotiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Custom Message Converter Pattern
 * 
 * Demonstrates how to create custom HTTP message converters to handle
 * specialized content types or custom serialization/deserialization logic.
 * 
 * Use Cases:
 * - Custom binary formats
 * - Legacy data formats
 * - Proprietary protocols
 * - Custom CSV/text formats
 * - Special encoding requirements
 */

// ===================== Domain Models =====================

record Product(
    Long id,
    String name,
    Double price,
    String category,
    LocalDateTime createdAt
) {}

record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timestamp
) {
    static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, LocalDateTime.now());
    }
    
    static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}

// ===================== Custom CSV Message Converter =====================

/**
 * Custom converter for CSV format
 * Handles conversion between Product objects and CSV representation
 */
@Component
class CsvMessageConverter extends AbstractHttpMessageConverter<Product> {
    
    private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv");
    
    public CsvMessageConverter() {
        super(CSV_MEDIA_TYPE);
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return Product.class.isAssignableFrom(clazz);
    }
    
    @Override
    protected Product readInternal(
            Class<? extends Product> clazz,
            HttpInputMessage inputMessage
    ) throws IOException, HttpMessageNotReadableException {
        
        try (InputStream inputStream = inputMessage.getBody()) {
            String csv = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            String[] parts = csv.split(",");
            
            if (parts.length != 5) {
                throw new HttpMessageNotReadableException(
                    "Invalid CSV format. Expected 5 fields.", 
                    inputMessage
                );
            }
            
            return new Product(
                Long.parseLong(parts[0].trim()),
                parts[1].trim(),
                Double.parseDouble(parts[2].trim()),
                parts[3].trim(),
                LocalDateTime.parse(parts[4].trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
    }
    
    @Override
    protected void writeInternal(
            Product product,
            HttpOutputMessage outputMessage
    ) throws IOException, HttpMessageNotWritableException {
        
        String csv = String.format("%d,%s,%.2f,%s,%s",
            product.id(),
            product.name(),
            product.price(),
            product.category(),
            product.createdAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        try (OutputStream outputStream = outputMessage.getBody()) {
            outputStream.write(csv.getBytes(StandardCharsets.UTF_8));
        }
    }
}

// ===================== Custom Pipe-Delimited Message Converter =====================

/**
 * Custom converter for pipe-delimited format
 * Demonstrates handling of custom text formats
 */
@Component
class PipeDelimitedMessageConverter extends AbstractHttpMessageConverter<Product> {
    
    private static final MediaType PIPE_MEDIA_TYPE = 
        new MediaType("text", "pipe-delimited");
    
    public PipeDelimitedMessageConverter() {
        super(PIPE_MEDIA_TYPE);
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return Product.class.isAssignableFrom(clazz);
    }
    
    @Override
    protected Product readInternal(
            Class<? extends Product> clazz,
            HttpInputMessage inputMessage
    ) throws IOException, HttpMessageNotReadableException {
        
        try (InputStream inputStream = inputMessage.getBody()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            String[] parts = content.split("\\|");
            
            if (parts.length != 5) {
                throw new HttpMessageNotReadableException(
                    "Invalid pipe-delimited format. Expected 5 fields.", 
                    inputMessage
                );
            }
            
            return new Product(
                Long.parseLong(parts[0].trim()),
                parts[1].trim(),
                Double.parseDouble(parts[2].trim()),
                parts[3].trim(),
                LocalDateTime.parse(parts[4].trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
    }
    
    @Override
    protected void writeInternal(
            Product product,
            HttpOutputMessage outputMessage
    ) throws IOException, HttpMessageNotWritableException {
        
        String content = String.format("%d|%s|%.2f|%s|%s",
            product.id(),
            product.name(),
            product.price(),
            product.category(),
            product.createdAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        try (OutputStream outputStream = outputMessage.getBody()) {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
}

// ===================== Custom Fixed-Width Message Converter =====================

/**
 * Custom converter for fixed-width format
 * Useful for legacy systems and mainframe integrations
 */
@Component
class FixedWidthMessageConverter extends AbstractHttpMessageConverter<Product> {
    
    private static final MediaType FIXED_WIDTH_MEDIA_TYPE = 
        new MediaType("text", "fixed-width");
    
    // Field widths: id(10), name(30), price(10), category(20), createdAt(20)
    private static final int[] FIELD_WIDTHS = {10, 30, 10, 20, 20};
    
    public FixedWidthMessageConverter() {
        super(FIXED_WIDTH_MEDIA_TYPE);
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return Product.class.isAssignableFrom(clazz);
    }
    
    @Override
    protected Product readInternal(
            Class<? extends Product> clazz,
            HttpInputMessage inputMessage
    ) throws IOException, HttpMessageNotReadableException {
        
        try (InputStream inputStream = inputMessage.getBody()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            int pos = 0;
            String[] fields = new String[5];
            
            for (int i = 0; i < FIELD_WIDTHS.length; i++) {
                int endPos = Math.min(pos + FIELD_WIDTHS[i], content.length());
                fields[i] = content.substring(pos, endPos).trim();
                pos = endPos;
            }
            
            return new Product(
                Long.parseLong(fields[0]),
                fields[1],
                Double.parseDouble(fields[2]),
                fields[3],
                LocalDateTime.parse(fields[4], DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
    }
    
    @Override
    protected void writeInternal(
            Product product,
            HttpOutputMessage outputMessage
    ) throws IOException, HttpMessageNotWritableException {
        
        String content = String.format(
            "%-10d%-30s%-10.2f%-20s%-20s",
            product.id(),
            truncate(product.name(), 30),
            product.price(),
            truncate(product.category(), 20),
            product.createdAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        try (OutputStream outputStream = outputMessage.getBody()) {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    private String truncate(String value, int maxLength) {
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}

// ===================== Custom JSON Wrapper Converter =====================

/**
 * Custom converter that wraps JSON responses with metadata
 */
@Component
class JsonWrapperMessageConverter extends AbstractHttpMessageConverter<Object> {
    
    private static final MediaType JSON_WRAPPER_MEDIA_TYPE = 
        new MediaType("application", "vnd.company.wrapped+json");
    
    private final ObjectMapper objectMapper;
    
    public JsonWrapperMessageConverter(ObjectMapper objectMapper) {
        super(JSON_WRAPPER_MEDIA_TYPE);
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return true; // Support all types
    }
    
    @Override
    protected Object readInternal(
            Class<?> clazz,
            HttpInputMessage inputMessage
    ) throws IOException, HttpMessageNotReadableException {
        
        try (InputStream inputStream = inputMessage.getBody()) {
            // Unwrap the response
            Map<String, Object> wrapper = objectMapper.readValue(inputStream, Map.class);
            Object data = wrapper.get("data");
            
            // Convert data to target class
            return objectMapper.convertValue(data, clazz);
        }
    }
    
    @Override
    protected void writeInternal(
            Object object,
            HttpOutputMessage outputMessage
    ) throws IOException, HttpMessageNotWritableException {
        
        // Wrap the response
        Map<String, Object> wrapper = Map.of(
            "success", true,
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "version", "1.0",
            "data", object
        );
        
        try (OutputStream outputStream = outputMessage.getBody()) {
            objectMapper.writeValue(outputStream, wrapper);
        }
    }
}

// ===================== WebMvc Configuration =====================

@Configuration
class CustomConverterConfig implements WebMvcConfigurer {
    
    private final CsvMessageConverter csvMessageConverter;
    private final PipeDelimitedMessageConverter pipeDelimitedMessageConverter;
    private final FixedWidthMessageConverter fixedWidthMessageConverter;
    private final JsonWrapperMessageConverter jsonWrapperMessageConverter;
    
    public CustomConverterConfig(
            CsvMessageConverter csvMessageConverter,
            PipeDelimitedMessageConverter pipeDelimitedMessageConverter,
            FixedWidthMessageConverter fixedWidthMessageConverter,
            JsonWrapperMessageConverter jsonWrapperMessageConverter
    ) {
        this.csvMessageConverter = csvMessageConverter;
        this.pipeDelimitedMessageConverter = pipeDelimitedMessageConverter;
        this.fixedWidthMessageConverter = fixedWidthMessageConverter;
        this.jsonWrapperMessageConverter = jsonWrapperMessageConverter;
    }
    
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Add custom converters
        // Insert at beginning to give them priority
        converters.add(0, csvMessageConverter);
        converters.add(1, pipeDelimitedMessageConverter);
        converters.add(2, fixedWidthMessageConverter);
        converters.add(3, jsonWrapperMessageConverter);
    }
}

// ===================== REST Controller =====================

@RestController
@RequestMapping("/api/custom-converter")
class CustomConverterController {
    
    /**
     * Endpoint supporting multiple custom formats
     * 
     * Examples:
     * 
     * GET /api/custom-converter/product/1
     * Accept: text/csv
     * Response: 1,Laptop,999.99,Electronics,2024-01-15T10:30:00
     * 
     * GET /api/custom-converter/product/1
     * Accept: text/pipe-delimited
     * Response: 1|Laptop|999.99|Electronics|2024-01-15T10:30:00
     * 
     * GET /api/custom-converter/product/1
     * Accept: text/fixed-width
     * Response: 1         Laptop                        999.99    Electronics         2024-01-15T10:30:00 
     * 
     * GET /api/custom-converter/product/1
     * Accept: application/vnd.company.wrapped+json
     * Response: {"success":true,"timestamp":"...","version":"1.0","data":{...}}
     */
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return new Product(
            id,
            "Laptop",
            999.99,
            "Electronics",
            LocalDateTime.of(2024, 1, 15, 10, 30)
        );
    }
    
    /**
     * POST endpoint accepting custom formats
     * 
     * Examples:
     * 
     * POST /api/custom-converter/product
     * Content-Type: text/csv
     * Body: 2,Smartphone,599.99,Electronics,2024-01-15T11:00:00
     * 
     * POST /api/custom-converter/product
     * Content-Type: text/pipe-delimited
     * Body: 2|Smartphone|599.99|Electronics|2024-01-15T11:00:00
     */
    @PostMapping("/product")
    public ApiResponse<Product> createProduct(@RequestBody Product product) {
        System.out.println("Received product: " + product);
        return ApiResponse.success(product);
    }
    
    /**
     * Demonstrates format conversion
     * Accept different format, respond in another
     * 
     * Example:
     * POST /api/custom-converter/convert
     * Content-Type: text/csv
     * Accept: text/pipe-delimited
     * Body: 3,Tablet,399.99,Electronics,2024-01-15T12:00:00
     */
    @PostMapping("/convert")
    public Product convertFormat(@RequestBody Product product) {
        return product;
    }
    
    /**
     * Batch processing with custom formats
     */
    @GetMapping("/products")
    public List<Product> getProducts() {
        return List.of(
            new Product(1L, "Laptop", 999.99, "Electronics", LocalDateTime.now()),
            new Product(2L, "Smartphone", 599.99, "Electronics", LocalDateTime.now()),
            new Product(3L, "Tablet", 399.99, "Electronics", LocalDateTime.now())
        );
    }
}

// ===================== Testing Examples =====================

@RestController
@RequestMapping("/api/converter-examples")
class ConverterExamplesController {
    
    /**
     * Example demonstrating converter selection based on Content-Type
     */
    @PostMapping("/echo")
    public Product echo(@RequestBody Product product) {
        return product;
    }
    
    /**
     * Example with multiple supported input formats
     */
    @PostMapping(
        value = "/process",
        consumes = {"text/csv", "text/pipe-delimited", "application/json"}
    )
    public ApiResponse<String> processProduct(@RequestBody Product product) {
        return ApiResponse.success(
            "Processed product: " + product.name() + " ($" + product.price() + ")"
        );
    }
    
    /**
     * Example with specific output format
     */
    @GetMapping(
        value = "/export/{id}",
        produces = "text/csv"
    )
    public Product exportProduct(@PathVariable Long id) {
        return new Product(
            id,
            "Exported Product",
            123.45,
            "Export",
            LocalDateTime.now()
        );
    }
}

/**
 * Key Concepts Demonstrated:
 * 
 * 1. AbstractHttpMessageConverter:
 *    - Base class for custom converters
 *    - Implement supports(), readInternal(), writeInternal()
 * 
 * 2. Media Type Support:
 *    - Define custom media types
 *    - Register in converter constructor
 * 
 * 3. Serialization/Deserialization:
 *    - Convert between HTTP messages and Java objects
 *    - Handle input/output streams
 * 
 * 4. Error Handling:
 *    - HttpMessageNotReadableException for parse errors
 *    - HttpMessageNotWritableException for write errors
 * 
 * 5. Converter Registration:
 *    - Use WebMvcConfigurer.extendMessageConverters()
 *    - Control converter order (priority)
 * 
 * 6. Content Negotiation:
 *    - Spring selects converter based on Accept header
 *    - Request body parsing based on Content-Type
 * 
 * 7. Custom Formats:
 *    - CSV (Comma-Separated Values)
 *    - Pipe-delimited
 *    - Fixed-width
 *    - JSON wrapper with metadata
 * 
 * 8. Use Cases:
 *    - Legacy system integration
 *    - Custom binary formats
 *    - Specialized text formats
 *    - API versioning with custom media types
 *    - Response wrapping/unwrapping
 * 
 * Testing with curl:
 * 
 * # CSV format
 * curl -X POST http://localhost:8080/api/custom-converter/product \
 *   -H "Content-Type: text/csv" \
 *   -d "1,Laptop,999.99,Electronics,2024-01-15T10:30:00"
 * 
 * # Pipe-delimited format
 * curl -X GET http://localhost:8080/api/custom-converter/product/1 \
 *   -H "Accept: text/pipe-delimited"
 * 
 * # Fixed-width format
 * curl -X GET http://localhost:8080/api/custom-converter/product/1 \
 *   -H "Accept: text/fixed-width"
 * 
 * # JSON wrapped format
 * curl -X GET http://localhost:8080/api/custom-converter/product/1 \
 *   -H "Accept: application/vnd.company.wrapped+json"
 * 
 * # Format conversion
 * curl -X POST http://localhost:8080/api/custom-converter/convert \
 *   -H "Content-Type: text/csv" \
 *   -H "Accept: text/pipe-delimited" \
 *   -d "3,Tablet,399.99,Electronics,2024-01-15T12:00:00"
 */
