# Spring Serialization Patterns

## Overview

This directory contains comprehensive examples of 8 different serialization patterns commonly used in Spring Boot applications. Each pattern demonstrates a specific approach to converting Java objects to and from various formats.

## Table of Contents

1. [Pattern Overview](#pattern-overview)
2. [Pattern Descriptions](#pattern-descriptions)
3. [Pattern Comparison](#pattern-comparison)
4. [When to Use Each Pattern](#when-to-use-each-pattern)
5. [Best Practices](#best-practices)
6. [Performance Considerations](#performance-considerations)
7. [Testing Strategies](#testing-strategies)
8. [Common Pitfalls](#common-pitfalls)

---

## Pattern Overview

### 1. JSON Serialization Pattern
**File:** `JSONSerializationPattern.java`

Demonstrates JSON serialization and deserialization using Jackson library.

**Key Features:**
- ObjectMapper configuration
- Date/Time handling with JavaTimeModule
- Pretty printing and formatting
- Null value handling
- Simple and complex object serialization
- Collection serialization

**Use Cases:**
- REST API request/response
- Configuration files
- NoSQL database persistence
- Inter-service communication

---

### 2. XML Serialization Pattern
**File:** `XMLSerializationPattern.java`

Demonstrates XML serialization using JAXB (Java Architecture for XML Binding).

**Key Features:**
- JAXB Marshaller/Unmarshaller
- XML annotations (@XmlRootElement, @XmlElement, @XmlAttribute)
- Collection handling with @XmlElementWrapper
- Complex nested object structures
- Formatted XML output

**Use Cases:**
- SOAP web services
- Legacy system integration
- Document generation
- Regulatory compliance
- Enterprise application integration

---

### 3. Java Serialization Pattern
**File:** `JavaSerializationPattern.java`

Demonstrates native Java serialization using Serializable interface.

**Key Features:**
- Serializable interface
- ObjectOutputStream/ObjectInputStream
- transient keyword for excluding fields
- serialVersionUID for version control
- Externalizable interface
- Deep copy using serialization

**Use Cases:**
- Session persistence
- Object caching
- Deep cloning
- RMI (Remote Method Invocation)

**⚠️ Warning:** Java serialization has security and performance concerns. Consider JSON/XML/Protobuf for most use cases.

---

### 4. Custom Serializer Pattern
**File:** `CustomSerializerPattern.java`

Demonstrates creating custom Jackson serializers for specific types.

**Key Features:**
- JsonSerializer<T> implementation
- @JsonSerialize annotation
- Custom date/time formatting
- Sensitive data masking (credit cards, SSN)
- Currency formatting
- Field-level customization

**Use Cases:**
- Custom date/time formats
- Masking sensitive data
- Complex type serialization
- Data transformation during serialization
- Computed fields

---

### 5. Custom Deserializer Pattern
**File:** `CustomDeserializerPattern.java`

Demonstrates creating custom Jackson deserializers for flexible JSON parsing.

**Key Features:**
- JsonDeserializer<T> implementation
- @JsonDeserialize annotation
- Multiple format support (dates, phones, prices)
- Data validation during deserialization
- Backward compatibility
- Error handling

**Use Cases:**
- Parsing multiple date formats
- String to enum conversion with fallback
- Data validation
- Legacy JSON format support
- Data normalization

---

### 6. Jackson Integration Pattern
**File:** `JacksonIntegrationPattern.java`

Comprehensive Jackson library integration with advanced features.

**Key Features:**
- Jackson annotations (@JsonProperty, @JsonIgnore, @JsonFormat)
- JSON Views for selective serialization
- Polymorphic type handling (@JsonTypeInfo, @JsonSubTypes)
- @JsonCreator for custom constructors
- @JsonUnwrapped for flattening
- Property naming strategies

**Use Cases:**
- REST API development
- Complex object hierarchies
- DTO transformations
- API versioning
- Selective data exposure

---

### 7. JAXB Integration Pattern
**File:** `JAXBIntegrationPattern.java`

Comprehensive JAXB integration with advanced XML features.

**Key Features:**
- JAXB annotations
- @XmlJavaTypeAdapter for custom type conversion
- @XmlType for property ordering
- Jaxb2Marshaller configuration
- Schema validation
- Nested objects

**Use Cases:**
- SOAP services
- XML configuration
- Standards compliance
- Document generation
- Enterprise messaging

---

### 8. Protobuf Integration Pattern
**File:** `ProtobufIntegrationPattern.java`

Demonstrates Protocol Buffers for high-performance binary serialization.

**Key Features:**
- Schema definition (.proto files)
- Binary serialization
- Forward/backward compatibility
- Type safety
- Code generation
- gRPC integration

**Use Cases:**
- High-performance microservices
- gRPC services
- Mobile applications
- IoT data transmission
- Real-time streaming

---

## Pattern Comparison

### Format Comparison

| Pattern | Format | Size | Speed | Human Readable | Type Safety |
|---------|--------|------|-------|----------------|-------------|
| JSON | Text | Medium | Medium | ✅ Yes | ❌ No |
| XML | Text | Large | Slow | ✅ Yes | ⚠️ Partial |
| Java Serialization | Binary | Medium | Fast | ❌ No | ✅ Yes |
| Protobuf | Binary | Small | Very Fast | ❌ No | ✅ Yes |

### Size Comparison (Example: User Object)

```
Protobuf:     40-50 bytes   (Baseline)
JSON:        150-180 bytes  (3-4x larger)
XML:         250-300 bytes  (6-7x larger)
Java Serial: 200-250 bytes  (5x larger)
```

### Performance Comparison

```
Serialization Speed:
1. Protobuf:     100% (Fastest)
2. Java Serial:   80%
3. JSON:          40%
4. XML:           20% (Slowest)

Deserialization Speed:
1. Protobuf:     100% (Fastest)
2. Java Serial:   75%
3. JSON:          50%
4. XML:           25% (Slowest)
```

---

## When to Use Each Pattern

### JSON Serialization
✅ **Use When:**
- Building REST APIs
- Need human-readable format
- Working with JavaScript frontends
- Wide tooling support needed
- Debugging ease is important

❌ **Avoid When:**
- Maximum performance required
- Size constraints are critical
- Binary format acceptable

---

### XML Serialization
✅ **Use When:**
- SOAP web services
- Enterprise integrations
- Regulatory compliance required
- Legacy system support
- Schema validation needed

❌ **Avoid When:**
- Performance is critical
- Modern microservices
- Size efficiency matters

---

### Java Serialization
✅ **Use When:**
- Java-only environment
- Session persistence
- Deep object cloning
- Legacy code integration

❌ **Avoid When:**
- Cross-language communication needed
- Security is a major concern
- Long-term data storage
- Version migration required

---

### Custom Serializer/Deserializer
✅ **Use When:**
- Custom formatting required
- Masking sensitive data
- Multiple date formats
- Complex transformations
- Backward compatibility

❌ **Avoid When:**
- Standard serialization suffices
- Maintenance overhead unacceptable
- Simple use cases

---

### Jackson Integration
✅ **Use When:**
- REST API development
- Complex object hierarchies
- Selective serialization needed
- JSON Views required
- Polymorphic types

❌ **Avoid When:**
- Simple JSON needs
- Performance critical
- Binary format preferred

---

### JAXB Integration
✅ **Use When:**
- SOAP services
- XML-based standards
- Schema validation
- Enterprise integration
- Document generation

❌ **Avoid When:**
- REST APIs
- Modern microservices
- Performance critical

---

### Protobuf Integration
✅ **Use When:**
- gRPC services
- High-performance needed
- Microservices communication
- Mobile/IoT applications
- Size efficiency critical

❌ **Avoid When:**
- Human readability required
- Debugging ease important
- Simple use cases
- Schema evolution complex

---

## Best Practices

### 1. JSON Serialization Best Practices

```java
// ✅ Good: Configure ObjectMapper as a bean
@Bean
public ObjectMapper objectMapper() {
    return Jackson2ObjectMapperBuilder.json()
        .modules(new JavaTimeModule())
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();
}

// ❌ Bad: Creating new ObjectMapper instances repeatedly
public String serialize(Object obj) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper(); // Creates new instance each time
    return mapper.writeValueAsString(obj);
}
```

### 2. Handle Null Values Properly

```java
// ✅ Good: Configure null handling globally
objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

// ✅ Good: Field-level null handling
@JsonInclude(JsonInclude.Include.NON_NULL)
private String optionalField;
```

### 3. Use Appropriate Date/Time Formats

```java
// ✅ Good: Use ISO-8601 format
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
private LocalDateTime timestamp;

// ❌ Bad: Using timestamps (not human-readable)
private long timestamp; // Milliseconds since epoch
```

### 4. Version Your Serialization Format

```java
// ✅ Good: Include version in serialized data
public class VersionedData {
    private String version = "1.0";
    private Object data;
}

// ✅ Good: Use serialVersionUID for Java serialization
private static final long serialVersionUID = 1L;
```

### 5. Mask Sensitive Data

```java
// ✅ Good: Custom serializer for sensitive data
public class CreditCardSerializer extends JsonSerializer<CreditCard> {
    @Override
    public void serialize(CreditCard value, JsonGenerator gen, 
                         SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("number", "****-****-****-" + 
            value.getNumber().substring(value.getNumber().length() - 4));
        gen.writeEndObject();
    }
}

// ❌ Bad: Exposing sensitive data
public class CreditCard {
    public String number; // Full card number exposed!
}
```

### 6. Use JSON Views for Different Audiences

```java
// ✅ Good: Define views for different contexts
class Views {
    public static class Public {}
    public static class Internal extends Public {}
}

public class User {
    @JsonView(Views.Public.class)
    private String username;
    
    @JsonView(Views.Internal.class)
    private String socialSecurityNumber; // Only for internal use
}
```

### 7. Handle Polymorphic Types Correctly

```java
// ✅ Good: Use @JsonTypeInfo for polymorphism
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Circle.class, name = "circle"),
    @JsonSubTypes.Type(value = Rectangle.class, name = "rectangle")
})
abstract class Shape {
    // Base class
}
```

### 8. Use DTOs for API Boundaries

```java
// ✅ Good: Separate entity and DTO
@Entity
public class User {
    private String password; // Never expose
}

public class UserDTO {
    private Long id;
    private String username;
    // No password field
}

// ❌ Bad: Exposing entities directly
@GetMapping("/user/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id); // Exposes everything!
}
```

### 9. Configure Custom Deserializers for Flexibility

```java
// ✅ Good: Support multiple date formats
public class FlexibleDateDeserializer extends JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };
    
    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException {
        String dateString = p.getText();
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (Exception e) {
                // Try next formatter
            }
        }
        throw new IOException("Unable to parse date: " + dateString);
    }
}
```

### 10. Use Builder Pattern for Complex Objects

```java
// ✅ Good: Immutable objects with builder
@JsonDeserialize(builder = Person.Builder.class)
public class Person {
    private final String name;
    private final int age;
    
    private Person(Builder builder) {
        this.name = builder.name;
        this.age = builder.age;
    }
    
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private String name;
        private int age;
        
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder setAge(int age) {
            this.age = age;
            return this;
        }
        
        public Person build() {
            return new Person(this);
        }
    }
}
```

### 11. Handle Unknown Properties Gracefully

```java
// ✅ Good: Ignore unknown properties for backward compatibility
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    private String status;
    private Object data;
}

// Or globally:
objectMapper.configure(
    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
);
```

### 12. Use Property Naming Strategies

```java
// ✅ Good: Configure naming strategy globally
objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

// Or per class:
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApiRequest {
    private String userId;      // Serialized as "user_id"
    private String firstName;   // Serialized as "first_name"
}
```

---

## Performance Considerations

### 1. ObjectMapper Reuse

```java
// ✅ Good: Reuse ObjectMapper (thread-safe after configuration)
private static final ObjectMapper MAPPER = new ObjectMapper();

// ❌ Bad: Creating new instances
private ObjectMapper createMapper() {
    return new ObjectMapper(); // Expensive operation!
}
```

### 2. Streaming for Large Data

```java
// ✅ Good: Use streaming for large collections
@GetMapping("/users/stream")
public void streamUsers(OutputStream output) throws IOException {
    JsonGenerator generator = objectMapper.getFactory().createGenerator(output);
    generator.writeStartArray();
    
    userRepository.findAll().forEach(user -> {
        try {
            objectMapper.writeValue(generator, user);
        } catch (IOException e) {
            // Handle error
        }
    });
    
    generator.writeEndArray();
    generator.close();
}
```

### 3. Disable Unnecessary Features

```java
// ✅ Good: Disable unused features
objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
```

### 4. Use Protobuf for Performance-Critical Paths

```java
// ✅ Good: Use Protobuf for high-throughput scenarios
// Protobuf is 3-10x smaller and 20-100x faster than JSON/XML
UserProto.User user = UserProto.User.newBuilder()
    .setId(1)
    .setName("John")
    .build();
    
byte[] bytes = user.toByteArray(); // Very fast serialization
```

---

## Testing Strategies

### 1. Unit Testing Serialization

```java
@Test
public void testUserSerialization() throws JsonProcessingException {
    // Arrange
    User user = new User(1L, "john@example.com", "John Doe");
    ObjectMapper mapper = new ObjectMapper();
    
    // Act
    String json = mapper.writeValueAsString(user);
    
    // Assert
    assertThat(json).contains("\"id\":1");
    assertThat(json).contains("\"email\":\"john@example.com\"");
    assertThat(json).contains("\"name\":\"John Doe\"");
}
```

### 2. Unit Testing Deserialization

```java
@Test
public void testUserDeserialization() throws JsonProcessingException {
    // Arrange
    String json = "{\"id\":1,\"email\":\"john@example.com\",\"name\":\"John Doe\"}";
    ObjectMapper mapper = new ObjectMapper();
    
    // Act
    User user = mapper.readValue(json, User.class);
    
    // Assert
    assertThat(user.getId()).isEqualTo(1L);
    assertThat(user.getEmail()).isEqualTo("john@example.com");
    assertThat(user.getName()).isEqualTo("John Doe");
}
```

### 3. Testing Custom Serializers

```java
@Test
public void testCreditCardMasking() throws JsonProcessingException {
    // Arrange
    CreditCard card = new CreditCard("1234567890123456", "John Doe", "12/25", "123");
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(CreditCard.class, new CreditCardSerializer());
    mapper.registerModule(module);
    
    // Act
    String json = mapper.writeValueAsString(card);
    
    // Assert
    assertThat(json).contains("****-****-****-3456");
    assertThat(json).doesNotContain("1234567890123456");
    assertThat(json).contains("\"cvv\":\"***\"");
}
```

### 4. Testing JSON Views

```java
@Test
public void testPublicView() throws JsonProcessingException {
    // Arrange
    OrderDTO order = new OrderDTO(1L, "ORD-001", 99.99, "SECRET-TOKEN");
    ObjectMapper mapper = new ObjectMapper();
    
    // Act
    String publicJson = mapper.writerWithView(Views.Public.class)
        .writeValueAsString(order);
    
    // Assert
    assertThat(publicJson).contains("\"id\":1");
    assertThat(publicJson).contains("\"orderNumber\":\"ORD-001\"");
    assertThat(publicJson).doesNotContain("SECRET-TOKEN");
}
```

### 5. Integration Testing with MockMvc

```java
@WebMvcTest(UserController.class)
public class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testGetUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.password").doesNotExist()); // Ensure password not exposed
    }
    
    @Test
    public void testCreateUser() throws Exception {
        String userJson = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("John Doe"));
    }
}
```

### 6. Testing XML Serialization

```java
@Test
public void testXMLMarshalling() throws JAXBException {
    // Arrange
    BookXML book = new BookXML(1L, "Test Book", "ISBN-123", 29.99, LocalDate.now());
    JAXBContext context = JAXBContext.newInstance(BookXML.class);
    Marshaller marshaller = context.createMarshaller();
    StringWriter writer = new StringWriter();
    
    // Act
    marshaller.marshal(book, writer);
    String xml = writer.toString();
    
    // Assert
    assertThat(xml).contains("<book");
    assertThat(xml).contains("<title>Test Book</title>");
    assertThat(xml).contains("ISBN-123");
}
```

---

## Common Pitfalls

### 1. ❌ Circular References

```java
// Problem:
public class Parent {
    private List<Child> children;
}

public class Child {
    private Parent parent; // Circular reference!
}

// Solution 1: Use @JsonManagedReference and @JsonBackReference
public class Parent {
    @JsonManagedReference
    private List<Child> children;
}

public class Child {
    @JsonBackReference
    private Parent parent;
}

// Solution 2: Use @JsonIgnore
public class Child {
    @JsonIgnore
    private Parent parent;
}
```

### 2. ❌ Exposing Sensitive Data

```java
// Problem:
@Entity
public class User {
    private String password; // Exposed in JSON!
}

// Solution:
@Entity
public class User {
    @JsonIgnore
    private String password;
}
```

### 3. ❌ Not Handling Null Values

```java
// Problem: NullPointerException during serialization

// Solution 1: Null checks
@JsonSerialize(using = CustomSerializer.class)
public class CustomSerializer extends JsonSerializer<MyType> {
    @Override
    public void serialize(MyType value, JsonGenerator gen, 
                         SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        // Serialize...
    }
}

// Solution 2: Exclude nulls
@JsonInclude(JsonInclude.Include.NON_NULL)
private String optionalField;
```

### 4. ❌ Incompatible Date Formats

```java
// Problem: Date parsing fails with different formats

// Solution: Use custom deserializer supporting multiple formats
public class FlexibleDateDeserializer extends JsonDeserializer<LocalDate> {
    // Try multiple formats as shown in best practices
}
```

### 5. ❌ Performance Issues with Large Collections

```java
// Problem: Loading entire collection into memory

// Solution: Use streaming
@GetMapping("/users")
public void streamUsers(HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    JsonGenerator generator = objectMapper.getFactory()
        .createGenerator(response.getOutputStream());
    
    generator.writeStartArray();
    userRepository.findAll().forEach(user -> {
        try {
            objectMapper.writeValue(generator, user);
        } catch (IOException e) {
            // Handle error
        }
    });
    generator.writeEndArray();
    generator.close();
}
```

### 6. ❌ Not Versioning Serialized Data

```java
// Problem: Breaking changes when schema evolves

// Solution 1: Include version field
public class VersionedEntity {
    private String schemaVersion = "2.0";
    private Object data;
}

// Solution 2: Use @JsonIgnoreProperties
@JsonIgnoreProperties(ignoreUnknown = true)
public class BackwardCompatibleEntity {
    // Old fields
    private String name;
    
    // New fields (won't break old clients)
    private String email;
}
```

---

## Summary

### Quick Reference Table

| Pattern | Format | Size | Speed | Use Case | Complexity |
|---------|--------|------|-------|----------|------------|
| JSON | Text | ★★★ | ★★★ | REST APIs, General purpose | Low |
| XML | Text | ★ | ★ | SOAP, Enterprise | Medium |
| Java | Binary | ★★★ | ★★★★ | Java-only, Caching | Low |
| Custom Serializer | Text/Binary | ★★★ | ★★★ | Special formatting | Medium |
| Custom Deserializer | Text/Binary | ★★★ | ★★★ | Flexible parsing | Medium |
| Jackson | Text | ★★★ | ★★★ | Advanced JSON | Medium |
| JAXB | Text | ★ | ★ | Advanced XML | High |
| Protobuf | Binary | ★★★★★ | ★★★★★ | gRPC, Performance | High |

**Legend:** ★ = Poor, ★★★ = Good, ★★★★★ = Excellent

---

## Key Takeaways

1. **JSON is the default choice** for most REST APIs due to wide support and human readability
2. **XML is for enterprise** and SOAP services, regulatory compliance
3. **Avoid Java serialization** except for specific Java-only use cases
4. **Custom serializers/deserializers** provide flexibility for special formatting needs
5. **Jackson** is the de-facto standard for JSON in Spring Boot
6. **JAXB** is mature and reliable for XML processing
7. **Protobuf** offers best performance but requires more setup
8. **Always mask sensitive data** during serialization
9. **Use DTOs** to control what gets serialized
10. **Test serialization** thoroughly, especially for APIs

---

## Additional Resources

- [Jackson Documentation](https://github.com/FasterXML/jackson)
- [JAXB Tutorial](https://docs.oracle.com/javase/tutorial/jaxb/)
- [Protocol Buffers Guide](https://developers.google.com/protocol-buffers)
- [Spring Boot REST Documentation](https://spring.io/guides/gs/rest-service/)
- [RFC 7807 - Problem Details](https://tools.ietf.org/html/rfc7807)

---

## Running the Examples

Each pattern file contains a `main()` method demonstrating the pattern:

```bash
# Compile and run individual patterns
javac JSONSerializationPattern.java
java JSONSerializationPattern

# Or run as Spring Boot application
./mvnw spring-boot:run
```

---

## Dependencies

Add these to your `pom.xml` or `build.gradle`:

```xml
<!-- Jackson for JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>

<!-- JAXB for XML -->
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.1</version>
</dependency>

<!-- Protobuf -->
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>3.24.0</version>
</dependency>
```

---

**Last Updated:** 2024
**Author:** Spring Patterns Team
