package com.example.contentnegotiation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * CONTENT TYPE PATTERN
 * =====================
 * 
 * Demonstrates handling of Content-Type header in HTTP requests.
 * The server validates and processes incoming content based on Content-Type.
 * 
 * Key Concepts:
 * - Content-Type validation and parsing
 * - Charset handling and encoding
 * - Multipart form data processing
 * - Binary vs text content handling
 * - Content-Type mismatch error handling
 * 
 * Use Cases:
 * - File upload processing
 * - Form submission handling
 * - API payload validation
 * - Multi-format data ingestion
 * - Content encoding management
 */

@SpringBootApplication
public class ContentTypePattern {

    public static void main(String[] args) {
        SpringApplication.run(ContentTypePattern.class, args);
        demonstrateContentTypeHandling();
    }

    private static void demonstrateContentTypeHandling() {
        System.out.println("=== Content Type Pattern Demonstrations ===\n");

        // Demo 1: Content-Type parsing
        ContentTypeParser parser = new ContentTypeParser();
        String contentType = "application/json; charset=UTF-8";
        System.out.println("1. Content-Type Parsing:");
        System.out.println("   Input: " + contentType);
        ParsedContentType parsed = parser.parse(contentType);
        System.out.println("   Media Type: " + parsed.mediaType);
        System.out.println("   Charset: " + parsed.charset);
        System.out.println("   Parameters: " + parsed.parameters);

        // Demo 2: Charset detection
        System.out.println("\n2. Charset Detection:");
        List<String> contentTypes = Arrays.asList(
            "text/html; charset=ISO-8859-1",
            "application/json; charset=UTF-8",
            "text/plain",
            "application/xml; charset=UTF-16"
        );
        for (String ct : contentTypes) {
            ParsedContentType pct = parser.parse(ct);
            System.out.println("   " + ct + " -> " + pct.getCharsetOrDefault());
        }

        // Demo 3: Boundary extraction for multipart
        System.out.println("\n3. Multipart Boundary Extraction:");
        String multipartType = "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW";
        ParsedContentType multipart = parser.parse(multipartType);
        System.out.println("   Content-Type: " + multipartType);
        System.out.println("   Boundary: " + multipart.parameters.get("boundary"));

        // Demo 4: Content-Type validation
        System.out.println("\n4. Content-Type Validation:");
        ContentTypeValidator validator = new ContentTypeValidator();
        List<String> allowed = Arrays.asList("application/json", "application/xml", "text/plain");
        String testType = "application/json";
        boolean valid = validator.isValid(testType, allowed);
        System.out.println("   Testing: " + testType);
        System.out.println("   Allowed: " + allowed);
        System.out.println("   Valid: " + valid);
    }
}

// ============================================================================
// REST CONTROLLERS
// ============================================================================

@RestController
@RequestMapping("/api/content")
class ContentTypeController {

    /**
     * Accept JSON content
     */
    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> handleJson(@RequestBody Map<String, Object> data) {
        System.out.println("Received JSON: " + data);
        return ResponseEntity.ok(new ApiResponse(
            true,
            "JSON content processed successfully",
            data,
            LocalDateTime.now()
        ));
    }

    /**
     * Accept XML content
     */
    @PostMapping(value = "/xml", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ApiResponse> handleXml(@RequestBody String xmlData) {
        System.out.println("Received XML: " + xmlData);
        return ResponseEntity.ok(new ApiResponse(
            true,
            "XML content processed successfully",
            Map.of("xml", xmlData),
            LocalDateTime.now()
        ));
    }

    /**
     * Accept plain text
     */
    @PostMapping(value = "/text", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<ApiResponse> handleText(@RequestBody String text) {
        System.out.println("Received text: " + text);
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Text content processed successfully",
            Map.of("text", text, "length", text.length()),
            LocalDateTime.now()
        ));
    }

    /**
     * Accept multiple content types
     */
    @PostMapping(value = "/multi", consumes = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE,
        MediaType.TEXT_PLAIN_VALUE
    })
    public ResponseEntity<ApiResponse> handleMultiple(
            @RequestBody String content,
            @RequestHeader("Content-Type") String contentType) {
        
        System.out.println("Content-Type: " + contentType);
        System.out.println("Content: " + content);
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Content processed with type: " + contentType,
            Map.of("contentType", contentType, "content", content),
            LocalDateTime.now()
        ));
    }

    /**
     * Handle form URL encoded data
     */
    @PostMapping(value = "/form", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse> handleFormData(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String phone) {
        
        Map<String, String> formData = new HashMap<>();
        formData.put("name", name);
        formData.put("email", email);
        formData.put("phone", phone);
        
        System.out.println("Form data received: " + formData);
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Form data processed successfully",
            formData,
            LocalDateTime.now()
        ));
    }

    /**
     * Handle multipart file upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                false,
                "File is empty",
                null,
                LocalDateTime.now()
            ));
        }
        
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("filename", file.getOriginalFilename());
        fileInfo.put("contentType", file.getContentType());
        fileInfo.put("size", file.getSize());
        fileInfo.put("description", description);
        
        System.out.println("File uploaded: " + fileInfo);
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "File uploaded successfully",
            fileInfo,
            LocalDateTime.now()
        ));
    }

    /**
     * Handle binary data
     */
    @PostMapping(value = "/binary", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ApiResponse> handleBinary(@RequestBody byte[] data) {
        System.out.println("Received binary data: " + data.length + " bytes");
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Binary data processed successfully",
            Map.of("size", data.length, "type", "binary"),
            LocalDateTime.now()
        ));
    }

    /**
     * Handle content with charset specification
     */
    @PostMapping(value = "/charset", consumes = "text/plain;charset=UTF-8")
    public ResponseEntity<ApiResponse> handleCharsetText(
            @RequestBody String text,
            @RequestHeader("Content-Type") String contentType) {
        
        ContentTypeParser parser = new ContentTypeParser();
        ParsedContentType parsed = parser.parse(contentType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("text", text);
        result.put("charset", parsed.getCharsetOrDefault());
        result.put("length", text.length());
        result.put("bytes", text.getBytes(StandardCharsets.UTF_8).length);
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Charset-aware text processed",
            result,
            LocalDateTime.now()
        ));
    }
}

@RestController
@RequestMapping("/api/validation")
class ContentTypeValidationController {

    private final ContentTypeValidator validator = new ContentTypeValidator();

    /**
     * Validate content type before processing
     */
    @PostMapping("/strict")
    public ResponseEntity<?> strictValidation(
            @RequestBody String content,
            @RequestHeader("Content-Type") String contentType) {
        
        List<String> allowed = Arrays.asList(
            "application/json",
            "application/xml"
        );
        
        if (!validator.isValid(contentType, allowed)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(Map.of(
                    "error", "Unsupported Content-Type",
                    "received", contentType,
                    "allowed", allowed
                ));
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "Content-Type validated successfully",
            "contentType", contentType
        ));
    }

    /**
     * Custom content type handling
     */
    @PostMapping(value = "/custom", consumes = "application/vnd.company.api+json")
    public ResponseEntity<ApiResponse> handleCustomContentType(@RequestBody String content) {
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Custom content type processed",
            Map.of("content", content),
            LocalDateTime.now()
        ));
    }
}

// ============================================================================
// CONTENT TYPE PARSER
// ============================================================================

class ContentTypeParser {

    /**
     * Parse Content-Type header into components
     */
    public ParsedContentType parse(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return new ParsedContentType("application/octet-stream", null, new HashMap<>());
        }

        String[] parts = contentType.split(";");
        String mediaType = parts[0].trim();
        String charset = null;
        Map<String, String> parameters = new HashMap<>();

        for (int i = 1; i < parts.length; i++) {
            String param = parts[i].trim();
            String[] keyValue = param.split("=", 2);
            
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim().replaceAll("^\"|\"$", "");
                
                parameters.put(key, value);
                
                if ("charset".equalsIgnoreCase(key)) {
                    charset = value;
                }
            }
        }

        return new ParsedContentType(mediaType, charset, parameters);
    }

    /**
     * Extract media type without parameters
     */
    public String extractMediaType(String contentType) {
        if (contentType == null) return null;
        int semicolon = contentType.indexOf(';');
        return semicolon > 0 ? contentType.substring(0, semicolon).trim() : contentType.trim();
    }

    /**
     * Extract charset from Content-Type
     */
    public String extractCharset(String contentType) {
        ParsedContentType parsed = parse(contentType);
        return parsed.charset;
    }
}

class ParsedContentType {
    final String mediaType;
    final String charset;
    final Map<String, String> parameters;

    ParsedContentType(String mediaType, String charset, Map<String, String> parameters) {
        this.mediaType = mediaType;
        this.charset = charset;
        this.parameters = parameters;
    }

    public String getCharsetOrDefault() {
        return charset != null ? charset : "UTF-8";
    }
}

// ============================================================================
// CONTENT TYPE VALIDATOR
// ============================================================================

class ContentTypeValidator {

    /**
     * Validate if content type is in allowed list
     */
    public boolean isValid(String contentType, List<String> allowedTypes) {
        if (contentType == null || allowedTypes == null) {
            return false;
        }

        ContentTypeParser parser = new ContentTypeParser();
        String mediaType = parser.extractMediaType(contentType);

        return allowedTypes.stream()
            .anyMatch(allowed -> allowed.equalsIgnoreCase(mediaType));
    }

    /**
     * Validate content type matches pattern
     */
    public boolean matches(String contentType, String pattern) {
        if (contentType == null || pattern == null) {
            return false;
        }

        ContentTypeParser parser = new ContentTypeParser();
        String mediaType = parser.extractMediaType(contentType);
        
        // Handle wildcards
        if (pattern.equals("*/*")) return true;
        
        String[] patternParts = pattern.split("/");
        String[] typeParts = mediaType.split("/");
        
        if (patternParts.length != 2 || typeParts.length != 2) {
            return false;
        }
        
        boolean typeMatch = patternParts[0].equals("*") || 
                          patternParts[0].equalsIgnoreCase(typeParts[0]);
        boolean subtypeMatch = patternParts[1].equals("*") || 
                             patternParts[1].equalsIgnoreCase(typeParts[1]);
        
        return typeMatch && subtypeMatch;
    }

    /**
     * Check if content type is textual
     */
    public boolean isTextual(String contentType) {
        if (contentType == null) return false;
        
        ContentTypeParser parser = new ContentTypeParser();
        String mediaType = parser.extractMediaType(contentType).toLowerCase();
        
        return mediaType.startsWith("text/") ||
               mediaType.equals("application/json") ||
               mediaType.equals("application/xml") ||
               mediaType.equals("application/javascript") ||
               mediaType.endsWith("+json") ||
               mediaType.endsWith("+xml");
    }

    /**
     * Check if content type is binary
     */
    public boolean isBinary(String contentType) {
        return !isTextual(contentType);
    }
}

// ============================================================================
// CONTENT TYPE PROCESSOR
// ============================================================================

class ContentTypeProcessor {

    private final ContentTypeParser parser = new ContentTypeParser();
    private final ContentTypeValidator validator = new ContentTypeValidator();

    /**
     * Process content based on its type
     */
    public ProcessedContent process(String content, String contentType) {
        ParsedContentType parsed = parser.parse(contentType);
        
        ProcessedContent result = new ProcessedContent();
        result.originalContent = content;
        result.mediaType = parsed.mediaType;
        result.charset = parsed.getCharsetOrDefault();
        result.isTextual = validator.isTextual(contentType);
        result.isBinary = validator.isBinary(contentType);
        result.processedAt = LocalDateTime.now();
        
        // Apply type-specific processing
        if (parsed.mediaType.contains("json")) {
            result.processedContent = processAsJson(content);
            result.format = "JSON";
        } else if (parsed.mediaType.contains("xml")) {
            result.processedContent = processAsXml(content);
            result.format = "XML";
        } else if (parsed.mediaType.startsWith("text/")) {
            result.processedContent = processAsText(content);
            result.format = "Text";
        } else {
            result.processedContent = content;
            result.format = "Binary";
        }
        
        return result;
    }

    private String processAsJson(String content) {
        // Simulate JSON processing
        return "Processed as JSON: " + content;
    }

    private String processAsXml(String content) {
        // Simulate XML processing
        return "Processed as XML: " + content;
    }

    private String processAsText(String content) {
        // Simulate text processing
        return "Processed as Text: " + content;
    }
}

class ProcessedContent {
    String originalContent;
    String processedContent;
    String mediaType;
    String charset;
    String format;
    boolean isTextual;
    boolean isBinary;
    LocalDateTime processedAt;

    @Override
    public String toString() {
        return String.format("ProcessedContent[format=%s, mediaType=%s, charset=%s, textual=%b]",
            format, mediaType, charset, isTextual);
    }
}

// ============================================================================
// EXCEPTION HANDLING
// ============================================================================

@RestControllerAdvice
class ContentTypeExceptionHandler {

    /**
     * Handle unsupported content type
     */
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(
            org.springframework.web.HttpMediaTypeNotSupportedException ex) {
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Unsupported Media Type");
        error.put("message", ex.getMessage());
        error.put("contentType", ex.getContentType() != null ? ex.getContentType().toString() : "unknown");
        error.put("supportedTypes", ex.getSupportedMediaTypes());
        error.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    /**
     * Handle missing content type
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Invalid Request");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.badRequest().body(error);
    }
}

// ============================================================================
// RESPONSE WRAPPER
// ============================================================================

class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
    private LocalDateTime timestamp;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, Object data, LocalDateTime timestamp) {
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
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

/*
 * BEST PRACTICES:
 * ===============
 * 1. Always validate Content-Type header
 * 2. Handle charset encoding properly
 * 3. Provide clear error messages for unsupported types
 * 4. Support standard content types (JSON, XML, form data)
 * 5. Handle multipart form data for file uploads
 * 6. Use appropriate consumes attributes in @RequestMapping
 * 7. Document required Content-Type in API specifications
 * 8. Handle missing Content-Type headers gracefully
 * 
 * COMMON PITFALLS:
 * ================
 * 1. Not specifying consumes attribute
 * 2. Ignoring charset in Content-Type
 * 3. Poor error handling for invalid content types
 * 4. Not supporting common content types
 * 5. Hardcoding charset assumptions
 * 
 * TESTING SCENARIOS:
 * ==================
 * curl -X POST -H "Content-Type: application/json" -d '{"key":"value"}' http://localhost:8080/api/content/json
 * curl -X POST -H "Content-Type: application/xml" -d '<data>value</data>' http://localhost:8080/api/content/xml
 * curl -X POST -H "Content-Type: text/plain" -d 'plain text' http://localhost:8080/api/content/text
 * curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'name=John&email=john@example.com' http://localhost:8080/api/content/form
 * curl -X POST -H "Content-Type: multipart/form-data" -F "file=@document.pdf" http://localhost:8080/api/content/upload
 */
