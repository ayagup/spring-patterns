package com.example.serialization;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Jackson Integration Pattern
 * 
 * Comprehensive demonstration of Jackson library integration with Spring Boot.
 * 
 * Key Concepts:
 * 1. Jackson Annotations - @JsonProperty, @JsonIgnore, @JsonFormat, etc.
 * 2. Custom ObjectMapper configuration
 * 3. Naming strategies - Snake case, camel case, etc.
 * 4. Date/Time handling
 * 5. Polymorphic type handling - @JsonTypeInfo, @JsonSubTypes
 * 6. JSON Views for selective serialization
 * 7. @JsonCreator for custom constructors
 * 8. @JsonValue for custom serialization
 * 9. @JsonUnwrapped for flattening
 * 10. @JsonAlias for multiple field names
 * 
 * Use Cases:
 * - REST API development
 * - Configuration management
 * - Complex object hierarchies
 * - DTO transformations
 * - API versioning
 * - Selective data exposure
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class JacksonIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(JacksonIntegrationPattern.class, args);
        demonstrateJacksonIntegration();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
            .modules(new JavaTimeModule())
            .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();
    }
    
    private static void demonstrateJacksonIntegration() {
        System.out.println("=== Jackson Integration Pattern Demo ===\n");
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            
            // 1. Basic Annotations
            UserDTO user = new UserDTO(1L, "john.doe", "john@example.com", "password123", 
                                      LocalDateTime.now(), true);
            System.out.println("1. Jackson Annotations (@JsonIgnore, @JsonProperty):");
            System.out.println(mapper.writeValueAsString(user));
            System.out.println();
            
            // 2. JSON Views
            OrderDTO order = new OrderDTO(101L, "ORD-001", 299.99, "SECRET-TOKEN");
            
            System.out.println("2. JSON Views - Public View:");
            System.out.println(mapper.writerWithView(Views.Public.class)
                .writeValueAsString(order));
            
            System.out.println("\n2. JSON Views - Internal View:");
            System.out.println(mapper.writerWithView(Views.Internal.class)
                .writeValueAsString(order));
            System.out.println();
            
            // 3. Polymorphic Types
            List<Shape> shapes = Arrays.asList(
                new Circle(1, 5.0),
                new Rectangle(2, 10.0, 20.0),
                new Triangle(3, 3.0, 4.0, 5.0)
            );
            
            System.out.println("3. Polymorphic Type Handling:");
            System.out.println(mapper.writeValueAsString(shapes));
            System.out.println();
            
            // 4. JsonUnwrapped
            ContactInfo contact = new ContactInfo("555-1234", "john@example.com");
            Employee emp = new Employee(1L, "John Doe", contact);
            
            System.out.println("4. @JsonUnwrapped - Flattening:");
            System.out.println(mapper.writeValueAsString(emp));
            System.out.println();
            
            // 5. Custom @JsonCreator
            String pointJson = "{\"x\":10,\"y\":20}";
            Point point = mapper.readValue(pointJson, Point.class);
            System.out.println("5. @JsonCreator:");
            System.out.println("Deserialized: " + point);
            System.out.println();
            
            // 6. @JsonValue
            Status status = Status.ACTIVE;
            System.out.println("6. @JsonValue for Enum:");
            System.out.println("Status JSON: " + mapper.writeValueAsString(status));
            System.out.println();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// JSON Views
class Views {
    public static class Public {}
    public static class Internal extends Public {}
}

/**
 * User DTO with Jackson annotations
 */
class UserDTO {
    @JsonProperty("user_id")
    private Long id;
    
    private String username;
    private String email;
    
    @JsonIgnore  // Never serialize password
    private String password;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean active;
    
    public UserDTO() {}
    
    public UserDTO(Long id, String username, String email, String password, 
                  LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.active = active;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

/**
 * Order DTO with JSON Views
 */
class OrderDTO {
    @JsonView(Views.Public.class)
    private Long id;
    
    @JsonView(Views.Public.class)
    private String orderNumber;
    
    @JsonView(Views.Public.class)
    private Double amount;
    
    @JsonView(Views.Internal.class)
    private String internalToken;
    
    public OrderDTO() {}
    
    public OrderDTO(Long id, String orderNumber, Double amount, String internalToken) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.internalToken = internalToken;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getInternalToken() { return internalToken; }
    public void setInternalToken(String internalToken) { this.internalToken = internalToken; }
}

/**
 * Polymorphic Shape hierarchy
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Circle.class, name = "circle"),
    @JsonSubTypes.Type(value = Rectangle.class, name = "rectangle"),
    @JsonSubTypes.Type(value = Triangle.class, name = "triangle")
})
abstract class Shape {
    private int id;
    
    public Shape() {}
    public Shape(int id) { this.id = id; }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    abstract double area();
}

class Circle extends Shape {
    private double radius;
    
    public Circle() {}
    public Circle(int id, double radius) {
        super(id);
        this.radius = radius;
    }
    
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
    
    @Override
    double area() { return Math.PI * radius * radius; }
}

class Rectangle extends Shape {
    private double width;
    private double height;
    
    public Rectangle() {}
    public Rectangle(int id, double width, double height) {
        super(id);
        this.width = width;
        this.height = height;
    }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    @Override
    double area() { return width * height; }
}

class Triangle extends Shape {
    private double side1;
    private double side2;
    private double side3;
    
    public Triangle() {}
    public Triangle(int id, double side1, double side2, double side3) {
        super(id);
        this.side1 = side1;
        this.side2 = side2;
        this.side3 = side3;
    }
    
    public double getSide1() { return side1; }
    public void setSide1(double side1) { this.side1 = side1; }
    
    public double getSide2() { return side2; }
    public void setSide2(double side2) { this.side2 = side2; }
    
    public double getSide3() { return side3; }
    public void setSide3(double side3) { this.side3 = side3; }
    
    @Override
    double area() {
        double s = (side1 + side2 + side3) / 2;
        return Math.sqrt(s * (s - side1) * (s - side2) * (s - side3));
    }
}

/**
 * Contact Info for unwrapping demo
 */
class ContactInfo {
    private String phone;
    private String email;
    
    public ContactInfo() {}
    public ContactInfo(String phone, String email) {
        this.phone = phone;
        this.email = email;
    }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

/**
 * Employee with unwrapped contact
 */
class Employee {
    private Long id;
    private String name;
    
    @JsonUnwrapped
    private ContactInfo contact;
    
    public Employee() {}
    public Employee(Long id, String name, ContactInfo contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public ContactInfo getContact() { return contact; }
    public void setContact(ContactInfo contact) { this.contact = contact; }
}

/**
 * Point with JsonCreator
 */
class Point {
    private final int x;
    private final int y;
    
    @JsonCreator
    public Point(@JsonProperty("x") int x, @JsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    
    @Override
    public String toString() {
        return "Point(" + x + ", " + y + ")";
    }
}

/**
 * Status enum with JsonValue
 */
enum Status {
    ACTIVE("active"),
    INACTIVE("inactive"),
    SUSPENDED("suspended");
    
    private final String value;
    
    Status(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
}

@RestController
@RequestMapping("/api/jackson")
class JacksonIntegrationController {
    
    @JsonView(Views.Public.class)
    @GetMapping("/order/{id}")
    public OrderDTO getOrder(@PathVariable Long id) {
        return new OrderDTO(id, "ORD-" + id, 99.99, "SECRET");
    }
    
    @PostMapping("/shape")
    public Shape createShape(@RequestBody Shape shape) {
        return shape;
    }
}
