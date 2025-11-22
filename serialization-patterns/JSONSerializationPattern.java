package com.example.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON Serialization Pattern
 * 
 * Demonstrates JSON serialization and deserialization using Jackson library in Spring.
 * 
 * Key Concepts:
 * 1. ObjectMapper - Main class for JSON operations
 * 2. Serialization - Converting Java objects to JSON
 * 3. Deserialization - Converting JSON to Java objects
 * 4. Custom Jackson configuration
 * 5. Pretty printing and formatting
 * 6. Date/Time handling
 * 7. Null value handling
 * 8. Property naming strategies
 * 9. JSON Views for selective serialization
 * 10. Polymorphic type handling
 * 
 * Use Cases:
 * - REST API request/response handling
 * - Configuration file management
 * - Data persistence in NoSQL databases
 * - Inter-service communication
 * - Event streaming
 * - API documentation
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class JSONSerializationPattern {

    public static void main(String[] args) {
        SpringApplication.run(JSONSerializationPattern.class, args);
        
        // Demo JSON serialization
        demonstrateJSONSerialization();
    }
    
    /**
     * Configure custom ObjectMapper bean
     */
    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
            .json()
            .modules(new JavaTimeModule())
            .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }
    
    private static void demonstrateJSONSerialization() {
        System.out.println("=== JSON Serialization Pattern Demo ===\n");
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        try {
            // 1. Simple Object Serialization
            User user = new User(1L, "john.doe@example.com", "John Doe", LocalDate.of(1990, 1, 15));
            String userJson = mapper.writeValueAsString(user);
            System.out.println("1. Simple Object to JSON:");
            System.out.println(userJson);
            System.out.println();
            
            // 2. Complex Object Serialization
            Address address = new Address("123 Main St", "New York", "NY", "10001", "USA");
            Customer customer = new Customer(
                101L, 
                "Alice Johnson", 
                "alice@example.com",
                address,
                LocalDateTime.now()
            );
            String customerJson = mapper.writeValueAsString(customer);
            System.out.println("2. Complex Object to JSON:");
            System.out.println(customerJson);
            System.out.println();
            
            // 3. Collection Serialization
            List<Product> products = new ArrayList<>();
            products.add(new Product(1L, "Laptop", 999.99, "Electronics"));
            products.add(new Product(2L, "Mouse", 29.99, "Electronics"));
            products.add(new Product(3L, "Keyboard", 79.99, "Electronics"));
            
            String productsJson = mapper.writeValueAsString(products);
            System.out.println("3. Collection to JSON:");
            System.out.println(productsJson);
            System.out.println();
            
            // 4. Deserialization
            String jsonInput = "{\"id\":2,\"email\":\"jane@example.com\",\"name\":\"Jane Smith\",\"birthDate\":\"1992-05-20\"}";
            User deserializedUser = mapper.readValue(jsonInput, User.class);
            System.out.println("4. JSON to Object:");
            System.out.println("Deserialized User: " + deserializedUser);
            System.out.println();
            
            // 5. Array Deserialization
            String productsArray = "[{\"id\":4,\"name\":\"Tablet\",\"price\":499.99,\"category\":\"Electronics\"}]";
            Product[] deserializedProducts = mapper.readValue(productsArray, Product[].class);
            System.out.println("5. JSON Array to Object Array:");
            for (Product p : deserializedProducts) {
                System.out.println("Product: " + p);
            }
            System.out.println();
            
            // 6. Null Handling
            User userWithNulls = new User(3L, "bob@example.com", null, null);
            String nullHandlingJson = mapper.writeValueAsString(userWithNulls);
            System.out.println("6. Null Value Handling:");
            System.out.println(nullHandlingJson);
            System.out.println();
            
            // 7. Pretty Print vs Compact
            ObjectMapper compactMapper = new ObjectMapper();
            String compactJson = compactMapper.writeValueAsString(user);
            System.out.println("7. Compact JSON:");
            System.out.println(compactJson);
            System.out.println();
            
        } catch (JsonProcessingException e) {
            System.err.println("JSON Processing Error: " + e.getMessage());
        }
    }
}

/**
 * Simple User entity for JSON serialization
 */
class User {
    private Long id;
    private String email;
    private String name;
    private LocalDate birthDate;
    
    public User() {}
    
    public User(Long id, String email, String name, LocalDate birthDate) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', name='" + name + 
               "', birthDate=" + birthDate + "}";
    }
}

/**
 * Address entity for nested object serialization
 */
class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    public Address() {}
    
    public Address(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    // Getters and Setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}

/**
 * Customer entity with nested Address
 */
class Customer {
    private Long id;
    private String name;
    private String email;
    private Address address;
    private LocalDateTime registeredAt;
    
    public Customer() {}
    
    public Customer(Long id, String name, String email, Address address, LocalDateTime registeredAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.registeredAt = registeredAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}

/**
 * Product entity for collection serialization
 */
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
    
    // Getters and Setters
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
        return "Product{id=" + id + ", name='" + name + "', price=" + price + 
               ", category='" + category + "'}";
    }
}

/**
 * REST Controller demonstrating JSON serialization
 */
@RestController
@RequestMapping("/api/json")
class JSONSerializationController {
    
    private final ObjectMapper objectMapper;
    
    public JSONSerializationController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        return new User(id, "user" + id + "@example.com", "User " + id, LocalDate.now().minusYears(25));
    }
    
    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        System.out.println("Received user: " + user);
        return user;
    }
    
    @GetMapping("/products")
    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1L, "Laptop", 999.99, "Electronics"));
        products.add(new Product(2L, "Phone", 699.99, "Electronics"));
        return products;
    }
    
    @GetMapping("/customer/{id}")
    public Customer getCustomer(@PathVariable Long id) {
        Address address = new Address("123 Main St", "New York", "NY", "10001", "USA");
        return new Customer(id, "Customer " + id, "customer" + id + "@example.com", 
                          address, LocalDateTime.now());
    }
    
    @PostMapping("/serialize")
    public String serializeObject(@RequestBody Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
