package com.example.contenttypehandling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Consumes Pattern
 * 
 * Demonstrates the @Consumes annotation pattern in Spring MVC for specifying
 * the media types that a controller method can consume in the request body.
 * 
 * Key Concepts:
 * - @consumes annotation specifies content types the endpoint can accept
 * - Validates Content-Type header of incoming request
 * - Supports multiple media types
 * - Can be applied at class or method level
 * 
 * Use Cases:
 * - RESTful APIs accepting different input formats
 * - Input validation based on content type
 * - Processing multipart form data, JSON, XML, etc.
 * - API versioning through media types
 */
@SpringBootApplication
public class ConsumesPattern {

    public static void main(String[] args) {
        SpringApplication.run(ConsumesPattern.class, args);
    }
}

/**
 * Controller demonstrating single consumes type
 */
@RestController
@RequestMapping("/api/users")
class UserConsumesController {

    /**
     * Consumes JSON only
     * Request must have Content-Type: application/json
     */
    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse createUserFromJson(@RequestBody UserRequest request) {
        return new UserResponse("Created user from JSON: " + request.getName());
    }

    /**
     * Consumes XML only
     * Request must have Content-Type: application/xml
     */
    @PostMapping(value = "/xml", consumes = MediaType.APPLICATION_XML_VALUE)
    public UserResponse createUserFromXml(@RequestBody UserRequest request) {
        return new UserResponse("Created user from XML: " + request.getName());
    }

    /**
     * Consumes plain text
     * Request must have Content-Type: text/plain
     */
    @PostMapping(value = "/text", consumes = MediaType.TEXT_PLAIN_VALUE)
    public UserResponse createUserFromText(@RequestBody String text) {
        return new UserResponse("Created user from text: " + text);
    }

    /**
     * Consumes form data
     * Request must have Content-Type: application/x-www-form-urlencoded
     */
    @PostMapping(value = "/form", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public UserResponse createUserFromForm(
            @RequestParam String name,
            @RequestParam String email) {
        return new UserResponse("Created user from form: " + name + " (" + email + ")");
    }
}

/**
 * Controller demonstrating multiple consumes types
 */
@RestController
@RequestMapping("/api/products")
class ProductConsumesController {

    /**
     * Consumes both JSON and XML
     * Request can have Content-Type: application/json OR application/xml
     */
    @PostMapping(consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        return new ProductResponse("Created product: " + request.getName());
    }

    /**
     * Consumes JSON, XML, or form data
     */
    @PutMapping(value = "/{id}", consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE
    })
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @RequestBody(required = false) ProductRequest request) {
        return new ProductResponse("Updated product " + id);
    }
}

/**
 * Controller demonstrating multipart file upload
 */
@RestController
@RequestMapping("/api/files")
class FileUploadController {

    /**
     * Consumes multipart form data for file upload
     * Request must have Content-Type: multipart/form-data
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse uploadFile(
            @RequestParam("file") String file,
            @RequestParam(value = "description", required = false) String description) {
        return new FileUploadResponse("File uploaded successfully", file);
    }

    /**
     * Consumes multipart mixed for complex uploads
     */
    @PostMapping(value = "/upload-multiple", consumes = "multipart/mixed")
    public FileUploadResponse uploadMultipleFiles(@RequestBody String files) {
        return new FileUploadResponse("Multiple files uploaded", files);
    }
}

/**
 * Controller with class-level consumes annotation
 */
@RestController
@RequestMapping("/api/data")
class DataConsumesController {

    /**
     * Inherits JSON consumes from class level (if defined)
     */
    @PostMapping
    public DataResponse createData(@RequestBody DataRequest request) {
        return new DataResponse("Data created: " + request.getValue());
    }

    /**
     * Override class-level consumes with method-level
     */
    @PostMapping(value = "/xml", consumes = MediaType.APPLICATION_XML_VALUE)
    public DataResponse createDataFromXml(@RequestBody DataRequest request) {
        return new DataResponse("Data created from XML: " + request.getValue());
    }

    /**
     * No consumes restriction - accepts any content type
     */
    @PostMapping("/any")
    public DataResponse createDataAny(@RequestBody String data) {
        return new DataResponse("Data created: " + data);
    }
}

/**
 * Controller demonstrating custom media types
 */
@RestController
@RequestMapping("/api/v1/custom")
class CustomMediaTypeConsumesController {

    /**
     * Consumes custom vendor-specific media type
     */
    @PostMapping(value = "/item", consumes = "application/vnd.company.item-v1+json")
    public Map<String, String> createItemV1(@RequestBody Map<String, Object> item) {
        return Map.of("message", "Created item v1", "version", "1.0");
    }

    /**
     * Consumes different version of custom media type
     */
    @PostMapping(value = "/item", consumes = "application/vnd.company.item-v2+json")
    public Map<String, String> createItemV2(@RequestBody Map<String, Object> item) {
        return Map.of("message", "Created item v2", "version", "2.0");
    }

    /**
     * Consumes patch+json for partial updates
     */
    @PatchMapping(value = "/resource/{id}", consumes = "application/merge-patch+json")
    public Map<String, String> patchResource(
            @PathVariable Long id,
            @RequestBody Map<String, Object> patch) {
        return Map.of("message", "Resource patched", "id", id.toString());
    }
}

/**
 * Controller demonstrating consumes with produces
 */
@RestController
@RequestMapping("/api/convert")
class ContentConversionController {

    /**
     * Consumes JSON and produces XML
     */
    @PostMapping(
            value = "/json-to-xml",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ConversionResponse convertJsonToXml(@RequestBody ConversionRequest request) {
        return new ConversionResponse("Converted from JSON to XML", request.getData());
    }

    /**
     * Consumes XML and produces JSON
     */
    @PostMapping(
            value = "/xml-to-json",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ConversionResponse convertXmlToJson(@RequestBody ConversionRequest request) {
        return new ConversionResponse("Converted from XML to JSON", request.getData());
    }

    /**
     * Consumes multiple types and produces multiple types
     */
    @PostMapping(
            value = "/transform",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ConversionResponse transform(@RequestBody ConversionRequest request) {
        return new ConversionResponse("Transformed data", request.getData());
    }
}

/**
 * Request/Response DTOs
 */
class UserRequest {
    private String name;
    private String email;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

class UserResponse {
    private String message;

    public UserResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

class ProductRequest {
    private String name;
    private Double price;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

class ProductResponse {
    private String message;

    public ProductResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

class FileUploadResponse {
    private String message;
    private String fileName;

    public FileUploadResponse(String message, String fileName) {
        this.message = message;
        this.fileName = fileName;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}

class DataRequest {
    private String value;

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

class DataResponse {
    private String message;

    public DataResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

class ConversionRequest {
    private String data;

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}

class ConversionResponse {
    private String message;
    private String result;

    public ConversionResponse(String message, String result) {
        this.message = message;
        this.result = result;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}

/**
 * Documentation:
 * 
 * @consumes Annotation:
 * - Specifies media types that a method can consume
 * - Applied to @RequestMapping or its shortcuts (@PostMapping, @PutMapping, etc.)
 * - Validates Content-Type header of incoming request
 * - Can be used at class level (applies to all methods)
 * - Method-level overrides class-level
 * 
 * Content-Type Validation:
 * - Spring checks Content-Type header against consumes value
 * - If no match, returns 415 Unsupported Media Type
 * - If Content-Type is missing and consumes is specified, request is rejected
 * 
 * Common Media Types for Consumes:
 * - APPLICATION_JSON_VALUE: "application/json"
 * - APPLICATION_XML_VALUE: "application/xml"
 * - APPLICATION_FORM_URLENCODED_VALUE: "application/x-www-form-urlencoded"
 * - MULTIPART_FORM_DATA_VALUE: "multipart/form-data"
 * - TEXT_PLAIN_VALUE: "text/plain"
 * - APPLICATION_OCTET_STREAM_VALUE: "application/octet-stream"
 * 
 * Best Practices:
 * - Always specify consumes for methods that accept request body
 * - Use specific media types rather than accepting all types
 * - Provide clear error messages for unsupported media types
 * - Document accepted media types in API documentation
 * - Combine consumes with produces for clear API contracts
 * 
 * Special Cases:
 * - Multipart/form-data for file uploads
 * - Application/x-www-form-urlencoded for HTML forms
 * - Custom vendor media types for versioning
 * - Patch+json for partial updates (RFC 7396)
 * 
 * Error Handling:
 * - 415 Unsupported Media Type when Content-Type doesn't match
 * - 400 Bad Request when request body is malformed
 * - Use @ExceptionHandler for custom error responses
 */
