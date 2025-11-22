package com.example.conversion.converter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Converter Pattern - Demonstrates Spring's Type Conversion System
 * 
 * This pattern shows how to:
 * 1. Implement Converter interface for simple conversions
 * 2. Create GenericConverter for complex conversions
 * 3. Use ConditionalConverter for conditional conversion
 * 4. Implement bidirectional converters
 * 5. Register converters with ConversionService
 * 6. Handle collection conversions
 * 7. Convert between custom types
 * 8. Use ConverterFactory for family of converters
 * 9. Chain multiple converters
 * 10. Handle null and edge cases
 * 
 * Key Concepts:
 * - Converter<S, T>: Simple one-to-one conversion
 * - GenericConverter: Complex multi-type conversion
 * - ConditionalConverter: Conditional conversion logic
 * - ConverterFactory: Factory for related converters
 * - ConversionService: Central conversion service
 * 
 * Converter Types:
 * 1. Converter - Simple source to target conversion
 * 2. GenericConverter - Multiple source/target types
 * 3. ConditionalConverter - Conditional conversion
 * 4. ConditionalGenericConverter - Combines both
 * 5. ConverterFactory - Creates converters for type families
 * 
 * Use Cases:
 * - String to Date conversion
 * - DTO to Entity mapping
 * - Enum conversions
 * - Custom type conversions
 * - Collection conversions
 * 
 * Dependencies:
 * - spring-core
 * - spring-context
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class ConverterPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(ConverterPattern.class, args);
        demonstrateConverters(context);
    }
    
    /**
     * Configures conversion service with custom converters
     */
    @Bean
    public ConversionService conversionService() {
        DefaultConversionService service = new DefaultConversionService();
        
        // Register custom converters
        service.addConverter(new StringToUserConverter());
        service.addConverter(new UserToStringConverter());
        service.addConverter(new StringToMoneyConverter());
        service.addConverter(new MoneyToStringConverter());
        service.addConverter(new StringToAddressConverter());
        service.addConverter(new EntityToDtoConverter());
        
        return service;
    }
    
    /**
     * Demonstrates various converter scenarios
     */
    private static void demonstrateConverters(org.springframework.context.ApplicationContext context) {
        System.out.println("=== Converter Pattern Demonstrations ===\n");
        
        ConversionService conversionService = context.getBean(ConversionService.class);
        
        // Demo 1: Simple converters
        demonstrateSimpleConverters(conversionService);
        
        // Demo 2: Bidirectional converters
        demonstrateBidirectionalConverters(conversionService);
        
        // Demo 3: Complex object conversions
        demonstrateComplexConverters(conversionService);
        
        // Demo 4: Collection conversions
        demonstrateCollectionConverters(conversionService);
        
        // Demo 5: Conditional converters
        demonstrateConditionalConverters(conversionService);
    }
    
    /**
     * Demonstrates simple converters
     */
    private static void demonstrateSimpleConverters(ConversionService service) {
        System.out.println("1. Simple Converters:");
        
        // String to User
        User user = service.convert("John:Doe:john@example.com", User.class);
        System.out.println("   String to User: " + user);
        
        // String to Money
        Money money = service.convert("USD:100.50", Money.class);
        System.out.println("   String to Money: " + money);
        
        System.out.println();
    }
    
    /**
     * Demonstrates bidirectional converters
     */
    private static void demonstrateBidirectionalConverters(ConversionService service) {
        System.out.println("2. Bidirectional Converters:");
        
        // User to String and back
        User originalUser = new User("Jane", "Smith", "jane@example.com");
        String userString = service.convert(originalUser, String.class);
        User convertedUser = service.convert(userString, User.class);
        
        System.out.println("   Original User: " + originalUser);
        System.out.println("   User as String: " + userString);
        System.out.println("   Converted Back: " + convertedUser);
        
        System.out.println();
    }
    
    /**
     * Demonstrates complex object conversions
     */
    private static void demonstrateComplexConverters(ConversionService service) {
        System.out.println("3. Complex Object Conversions:");
        
        ProductEntity entity = new ProductEntity();
        entity.setId(1L);
        entity.setName("Laptop");
        entity.setPrice(new BigDecimal("999.99"));
        entity.setInStock(true);
        
        ProductDto dto = service.convert(entity, ProductDto.class);
        System.out.println("   Entity to DTO: " + dto);
        
        System.out.println();
    }
    
    /**
     * Demonstrates collection conversions
     */
    private static void demonstrateCollectionConverters(ConversionService service) {
        System.out.println("4. Collection Conversions:");
        
        List<String> userStrings = Arrays.asList(
            "Alice:Wonder:alice@example.com",
            "Bob:Builder:bob@example.com"
        );
        
        // Convert list of strings to list of users
        List<User> users = userStrings.stream()
            .map(str -> service.convert(str, User.class))
            .collect(Collectors.toList());
        
        System.out.println("   Converted " + users.size() + " users:");
        users.forEach(u -> System.out.println("     - " + u));
        
        System.out.println();
    }
    
    /**
     * Demonstrates conditional converters
     */
    private static void demonstrateConditionalConverters(ConversionService service) {
        System.out.println("5. Conditional Converters:");
        
        // Address conversion
        Address address = service.convert("123 Main St, New York, NY, 10001", Address.class);
        System.out.println("   String to Address: " + address);
        
        System.out.println();
    }
}

// ============================================================================
// Simple Converters
// ============================================================================

/**
 * Converts String to User
 * Format: "firstName:lastName:email"
 */
@Component
class StringToUserConverter implements Converter<String, User> {
    
    @Override
    public User convert(String source) {
        String[] parts = source.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid user format. Expected: firstName:lastName:email");
        }
        return new User(parts[0], parts[1], parts[2]);
    }
}

/**
 * Converts User to String
 */
@Component
class UserToStringConverter implements Converter<User, String> {
    
    @Override
    public String convert(User source) {
        return source.getFirstName() + ":" + source.getLastName() + ":" + source.getEmail();
    }
}

/**
 * Converts String to Money
 * Format: "CURRENCY:AMOUNT"
 */
@Component
class StringToMoneyConverter implements Converter<String, Money> {
    
    @Override
    public Money convert(String source) {
        String[] parts = source.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid money format. Expected: CURRENCY:AMOUNT");
        }
        return new Money(parts[0], new BigDecimal(parts[1]));
    }
}

/**
 * Converts Money to String
 */
@Component
class MoneyToStringConverter implements Converter<Money, String> {
    
    @Override
    public String convert(Money source) {
        return source.getCurrency() + ":" + source.getAmount();
    }
}

// ============================================================================
// Generic Converter
// ============================================================================

/**
 * Generic converter for String to Address conversion
 */
@Component
class StringToAddressConverter implements GenericConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Address.class));
    }
    
    @Override
    public Object convert(Object source, org.springframework.core.convert.TypeDescriptor sourceType,
                         org.springframework.core.convert.TypeDescriptor targetType) {
        
        if (source == null) {
            return null;
        }
        
        String addressString = (String) source;
        String[] parts = addressString.split(",");
        
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid address format. Expected: street, city, state, zipCode");
        }
        
        Address address = new Address();
        address.setStreet(parts[0].trim());
        address.setCity(parts[1].trim());
        address.setState(parts[2].trim());
        address.setZipCode(parts[3].trim());
        
        return address;
    }
}

// ============================================================================
// Conditional Converter
// ============================================================================

/**
 * Conditional converter for Entity to DTO conversion
 */
@Component
class EntityToDtoConverter implements ConditionalGenericConverter {
    
    @Override
    public boolean matches(org.springframework.core.convert.TypeDescriptor sourceType,
                          org.springframework.core.convert.TypeDescriptor targetType) {
        // Only convert if source ends with "Entity" and target ends with "Dto"
        return sourceType.getType().getSimpleName().endsWith("Entity") &&
               targetType.getType().getSimpleName().endsWith("Dto");
    }
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return null; // Indicates we need to use matches() method
    }
    
    @Override
    public Object convert(Object source, org.springframework.core.convert.TypeDescriptor sourceType,
                         org.springframework.core.convert.TypeDescriptor targetType) {
        
        if (source == null) {
            return null;
        }
        
        if (source instanceof ProductEntity) {
            ProductEntity entity = (ProductEntity) source;
            ProductDto dto = new ProductDto();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setPrice(entity.getPrice());
            dto.setAvailable(entity.isInStock());
            return dto;
        }
        
        throw new UnsupportedOperationException("Conversion not supported");
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * User entity
 */
class User {
    private String firstName;
    private String lastName;
    private String email;
    
    public User() {}
    
    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

/**
 * Money value object
 */
class Money {
    private String currency;
    private BigDecimal amount;
    
    public Money() {}
    
    public Money(String currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
    }
    
    // Getters and setters
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    @Override
    public String toString() {
        return currency + " " + amount;
    }
}

/**
 * Address value object
 */
class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    
    // Getters and setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " " + zipCode;
    }
}

/**
 * Product entity
 */
class ProductEntity {
    private Long id;
    private String name;
    private BigDecimal price;
    private boolean inStock;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }
}

/**
 * Product DTO
 */
class ProductDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private boolean available;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    
    @Override
    public String toString() {
        return "ProductDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", available=" + available +
                '}';
    }
}

// ============================================================================
// Conversion Service Usage
// ============================================================================

/**
 * Service demonstrating converter usage
 */
@Component
class ConversionServiceExample {
    
    private final ConversionService conversionService;
    
    public ConversionServiceExample(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    /**
     * Checks if conversion is possible
     */
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return conversionService.canConvert(sourceType, targetType);
    }
    
    /**
     * Converts object to target type
     */
    public <T> T convert(Object source, Class<T> targetType) {
        return conversionService.convert(source, targetType);
    }
    
    /**
     * Demonstrates various conversions
     */
    public void demonstrateConversions() {
        // String to User
        if (canConvert(String.class, User.class)) {
            User user = convert("Test:User:test@example.com", User.class);
            System.out.println("Converted user: " + user);
        }
        
        // User to String
        User user = new User("John", "Doe", "john@example.com");
        if (canConvert(User.class, String.class)) {
            String userString = convert(user, String.class);
            System.out.println("User as string: " + userString);
        }
    }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating converter usage in REST endpoints
 */
@RestController
@RequestMapping("/api/converters")
class ConverterController {
    
    private final ConversionService conversionService;
    
    public ConverterController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    /**
     * Converts string to user
     */
    @GetMapping("/user")
    public ResponseEntity<User> convertToUser(@RequestParam String data) {
        User user = conversionService.convert(data, User.class);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Converts string to money
     */
    @GetMapping("/money")
    public ResponseEntity<Money> convertToMoney(@RequestParam String data) {
        Money money = conversionService.convert(data, Money.class);
        return ResponseEntity.ok(money);
    }
    
    /**
     * Converts string to address
     */
    @GetMapping("/address")
    public ResponseEntity<Address> convertToAddress(@RequestParam String data) {
        Address address = conversionService.convert(data, Address.class);
        return ResponseEntity.ok(address);
    }
    
    /**
     * Checks if conversion is supported
     */
    @GetMapping("/can-convert")
    public ResponseEntity<Map<String, Boolean>> canConvert(
            @RequestParam String sourceType,
            @RequestParam String targetType) {
        
        try {
            Class<?> source = Class.forName(sourceType);
            Class<?> target = Class.forName(targetType);
            boolean canConvert = conversionService.canConvert(source, target);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("canConvert", canConvert);
            return ResponseEntity.ok(response);
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
