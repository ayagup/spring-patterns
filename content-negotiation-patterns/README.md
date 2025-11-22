# Content Negotiation Patterns

This directory contains comprehensive examples of **Content Negotiation Patterns** in Spring Framework. Content negotiation is the mechanism that allows a single resource to be represented in different formats based on client preferences.

## Overview

Content negotiation enables RESTful APIs to serve the same resource in multiple formats (JSON, XML, CSV, etc.) based on the client's `Accept` header or other negotiation strategies. Spring provides powerful mechanisms to handle content negotiation automatically.

## Patterns Included

### 1. Accept Header Pattern (`AcceptHeaderPattern.java`)
Demonstrates how to use the HTTP `Accept` header for content negotiation.

**Key Features:**
- Accept header parsing
- Media type prioritization
- Quality values (q-values)
- Wildcard media types
- Multiple format support

**Example:**
```bash
# Request JSON
curl -H "Accept: application/json" http://localhost:8080/api/products/1

# Request XML
curl -H "Accept: application/xml" http://localhost:8080/api/products/1

# Request with quality values
curl -H "Accept: application/json;q=0.9, application/xml;q=0.8" http://localhost:8080/api/products/1
```

**Use Cases:**
- REST API versioning
- Multi-format APIs
- Mobile vs web clients
- Legacy system support

---

### 2. Content Type Pattern (`ContentTypePattern.java`)
Demonstrates handling of different request content types.

**Key Features:**
- Content-Type header handling
- Request body parsing
- Multiple input formats
- Automatic deserialization
- Custom content types

**Example:**
```bash
# Send JSON
curl -X POST -H "Content-Type: application/json" \
  -d '{"name":"Product","price":99.99}' \
  http://localhost:8080/api/products

# Send XML
curl -X POST -H "Content-Type: application/xml" \
  -d '<product><name>Product</name><price>99.99</price></product>' \
  http://localhost:8080/api/products
```

**Use Cases:**
- Form submissions
- File uploads
- API integrations
- Mobile app backends

---

### 3. Media Type Pattern (`MediaTypePattern.java`)
Deep dive into Spring's MediaType class and media type matching.

**Key Features:**
- MediaType class usage
- Media type matching
- Custom media types
- Vendor-specific types
- Versioned media types

**Example:**
```java
MediaType json = MediaType.APPLICATION_JSON;
MediaType custom = new MediaType("application", "vnd.company.v1+json");
```

**Use Cases:**
- API versioning
- Custom formats
- Vendor-specific APIs
- Fine-grained control

---

### 4. View Negotiation Pattern (`ViewNegotiationPattern.java`)
Demonstrates view resolution based on content negotiation.

**Key Features:**
- ContentNegotiatingViewResolver
- Multiple view technologies
- Format-specific views
- View name resolution
- Template engines

**Example:**
```bash
# HTML view
curl -H "Accept: text/html" http://localhost:8080/products

# JSON view
curl -H "Accept: application/json" http://localhost:8080/products

# PDF view
curl -H "Accept: application/pdf" http://localhost:8080/products
```

**Use Cases:**
- Web + API endpoints
- Report generation
- Document downloads
- Multi-channel delivery

---

### 5. JSON/XML Conversion Pattern (`JSONXMLConversionPattern.java`)
Demonstrates bidirectional JSON and XML conversion.

**Key Features:**
- Jackson ObjectMapper
- Jackson XmlMapper
- Automatic conversion
- Custom serializers
- Pretty printing

**Example:**
```bash
# JSON to XML conversion
curl -X POST -H "Content-Type: application/json" \
  -H "Accept: application/xml" \
  -d '{"id":1,"name":"Product"}' \
  http://localhost:8080/api/convert

# XML to JSON conversion
curl -X POST -H "Content-Type: application/xml" \
  -H "Accept: application/json" \
  -d '<product><id>1</id><name>Product</name></product>' \
  http://localhost:8080/api/convert
```

**Use Cases:**
- Format conversion APIs
- Legacy system integration
- Data transformation
- Import/export features

---

### 6. Custom Message Converter Pattern (`CustomMessageConverterPattern.java`)
Shows how to create custom HTTP message converters for specialized formats.

**Key Features:**
- AbstractHttpMessageConverter
- Custom format support (CSV, pipe-delimited, fixed-width)
- Custom serialization logic
- Error handling
- Converter registration

**Example:**
```bash
# CSV format
curl -X POST -H "Content-Type: text/csv" \
  -d "1,Laptop,999.99,Electronics,2024-01-15T10:30:00" \
  http://localhost:8080/api/products

# Pipe-delimited format
curl -H "Accept: text/pipe-delimited" \
  http://localhost:8080/api/products/1
# Response: 1|Laptop|999.99|Electronics|2024-01-15T10:30:00

# Fixed-width format
curl -H "Accept: text/fixed-width" \
  http://localhost:8080/api/products/1
```

**Use Cases:**
- Legacy format support
- Custom binary protocols
- Specialized text formats
- Mainframe integration
- ETL processes

---

### 7. HTTP Message Converter Pattern (`HTTPMessageConverterPattern.java`)
Comprehensive guide to Spring's HTTP message converter system.

**Key Features:**
- MappingJackson2HttpMessageConverter (JSON)
- MappingJackson2XmlHttpMessageConverter (XML)
- StringHttpMessageConverter (Text)
- ByteArrayHttpMessageConverter (Binary)
- ResourceHttpMessageConverter (Files)
- Converter configuration
- Converter ordering

**Example:**
```java
@Configuration
class ConverterConfig implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new MappingJackson2XmlHttpMessageConverter());
    }
}
```

**Use Cases:**
- REST API development
- Multi-format support
- File uploads/downloads
- Custom serialization

---

## Content Negotiation Strategies

Spring supports multiple content negotiation strategies:

### 1. **Accept Header (Recommended)**
```bash
curl -H "Accept: application/json" http://localhost:8080/api/resource
```

### 2. **Path Extension (Deprecated)**
```bash
curl http://localhost:8080/api/resource.json
curl http://localhost:8080/api/resource.xml
```

### 3. **Query Parameter**
```bash
curl http://localhost:8080/api/resource?format=json
curl http://localhost:8080/api/resource?format=xml
```

### 4. **Fixed Produces**
```java
@GetMapping(value = "/resource", produces = MediaType.APPLICATION_JSON_VALUE)
```

---

## Configuration Examples

### Enable Content Negotiation
```java
@Configuration
class WebConfig implements WebMvcConfigurer {
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(true)
            .parameterName("format")
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("csv", new MediaType("text", "csv"));
    }
}
```

### Custom Message Converter
```java
@Bean
public MappingJackson2HttpMessageConverter jsonConverter() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    converter.setObjectMapper(objectMapper);
    return converter;
}
```

---

## Media Types Reference

### Standard Media Types
- **JSON**: `application/json`
- **XML**: `application/xml`, `text/xml`
- **HTML**: `text/html`
- **Plain Text**: `text/plain`
- **CSV**: `text/csv`
- **PDF**: `application/pdf`
- **Binary**: `application/octet-stream`

### Custom/Vendor Media Types
- **Versioned API**: `application/vnd.company.v1+json`
- **Custom Format**: `application/vnd.company.custom+json`
- **Wrapped JSON**: `application/vnd.api.wrapped+json`

---

## Best Practices

### 1. **Prefer Accept Header**
Use the `Accept` header for content negotiation rather than path extensions or query parameters.

### 2. **Support Multiple Formats**
```java
@GetMapping(produces = {
    MediaType.APPLICATION_JSON_VALUE,
    MediaType.APPLICATION_XML_VALUE
})
public Resource getResource() { }
```

### 3. **Use Proper Media Types**
Use standard media types when possible. Create custom vendor-specific types for special cases.

### 4. **Configure Defaults**
```java
configurer.defaultContentType(MediaType.APPLICATION_JSON);
```

### 5. **Handle Unsupported Types**
Implement proper error handling for unsupported media types (406 Not Acceptable).

### 6. **Version APIs Properly**
```java
@GetMapping(produces = "application/vnd.company.v1+json")
public ResourceV1 getResourceV1() { }

@GetMapping(produces = "application/vnd.company.v2+json")
public ResourceV2 getResourceV2() { }
```

### 7. **Use UTF-8 Encoding**
```java
new StringHttpMessageConverter(StandardCharsets.UTF_8);
```

### 8. **Test All Formats**
Test your API with different `Accept` and `Content-Type` headers.

---

## Testing Examples

### Using curl

```bash
# Test JSON response
curl -v -H "Accept: application/json" http://localhost:8080/api/products/1

# Test XML response
curl -v -H "Accept: application/xml" http://localhost:8080/api/products/1

# Test CSV response
curl -v -H "Accept: text/csv" http://localhost:8080/api/products/1

# Test JSON request
curl -X POST -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"name":"Product","price":99.99}' \
  http://localhost:8080/api/products

# Test XML request
curl -X POST -H "Content-Type: application/xml" \
  -H "Accept: application/xml" \
  -d '<product><name>Product</name><price>99.99</price></product>' \
  http://localhost:8080/api/products

# Test quality values
curl -H "Accept: application/json;q=0.9, application/xml;q=0.8, text/csv;q=0.7" \
  http://localhost:8080/api/products/1
```

### Using Spring MockMvc

```java
@Test
void testContentNegotiation() throws Exception {
    mockMvc.perform(get("/api/products/1")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    
    mockMvc.perform(get("/api/products/1")
            .accept(MediaType.APPLICATION_XML))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_XML));
}
```

---

## Common Issues & Solutions

### Issue 1: 406 Not Acceptable
**Problem**: Server cannot produce content in requested format.

**Solution**: 
- Add appropriate message converter
- Configure supported media types
- Check Accept header value

### Issue 2: 415 Unsupported Media Type
**Problem**: Server cannot consume the provided Content-Type.

**Solution**:
- Add message converter for the content type
- Check Content-Type header
- Configure `consumes` attribute

### Issue 3: Wrong Format Returned
**Problem**: Server returns wrong format despite Accept header.

**Solution**:
- Check converter ordering
- Verify media type configuration
- Check default content type settings

### Issue 4: Charset Issues
**Problem**: Special characters not displaying correctly.

**Solution**:
```java
converter.setSupportedMediaTypes(List.of(
    new MediaType("application", "json", StandardCharsets.UTF_8)
));
```

---

## Dependencies

Add these dependencies to your `pom.xml`:

```xml
<!-- Spring Web MVC -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Jackson JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Jackson XML -->
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-xml</artifactId>
</dependency>

<!-- Jackson Java 8 Date/Time -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

---

## Related Patterns

- **API Design Patterns**: RESTful API design, HATEOAS
- **Web MVC Patterns**: Controllers, Request Mapping
- **Validation Patterns**: Request validation, Error handling
- **Security Patterns**: Content-Type validation, CSRF

---

## Additional Resources

- [Spring Content Negotiation Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-multiple-representations)
- [HTTP Content Negotiation RFC](https://tools.ietf.org/html/rfc7231#section-5.3)
- [Jackson Documentation](https://github.com/FasterXML/jackson-docs)
- [Media Types (IANA)](https://www.iana.org/assignments/media-types/media-types.xhtml)

---

## Summary

Content Negotiation Patterns provide powerful mechanisms for building flexible APIs that can serve multiple representations of resources. By properly implementing these patterns, you can:

✅ Support multiple client types (web, mobile, legacy systems)  
✅ Enable format flexibility without code duplication  
✅ Build truly RESTful APIs  
✅ Implement API versioning strategies  
✅ Handle legacy format requirements  
✅ Provide better developer experience

All patterns include comprehensive examples, testing strategies, and best practices for production use.
