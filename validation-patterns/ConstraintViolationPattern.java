package com.example.validation.constraintviolation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Constraint Violation Pattern - Demonstrates Handling JSR-303/380 Constraint Violations
 * 
 * This pattern shows how to:
 * 1. Handle ConstraintViolation objects
 * 2. Extract violation information
 * 3. Process validation errors
 * 4. Format error messages
 * 5. Access property paths
 * 6. Get violation metadata
 * 7. Handle ConstraintViolationException
 * 8. Build custom error responses
 * 9. Aggregate violations
 * 10. Localize error messages
 * 
 * Key Concepts:
 * - ConstraintViolation: Represents a single validation error
 * - ConstraintViolationException: Exception containing violations
 * - Path: Property path to the invalid value
 * - MessageInterpolator: Resolves violation messages
 * - ConstraintDescriptor: Metadata about the constraint
 * 
 * Violation Information Available:
 * 1. Message - Error message
 * 2. Message Template - Uninterpolated message
 * 3. Root Bean - Object being validated
 * 4. Invalid Value - The value that failed validation
 * 5. Property Path - Path to the property
 * 6. Constraint Descriptor - Constraint metadata
 * 
 * Dependencies:
 * - spring-boot-starter-validation
 * - jakarta.validation-api
 * - hibernate-validator
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class ConstraintViolationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(ConstraintViolationPattern.class, args);
        demonstrateConstraintViolations();
    }
    
    /**
     * Demonstrates various constraint violation scenarios
     */
    private static void demonstrateConstraintViolations() {
        System.out.println("=== Constraint Violation Pattern Demonstrations ===\n");
        
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        
        // Demo 1: Basic constraint violations
        demonstrateBasicViolations(validator);
        
        // Demo 2: Nested object violations
        demonstrateNestedViolations(validator);
        
        // Demo 3: Collection violations
        demonstrateCollectionViolations(validator);
        
        // Demo 4: Violation metadata extraction
        demonstrateViolationMetadata(validator);
        
        // Demo 5: Custom violation handling
        demonstrateCustomViolationHandling(validator);
    }
    
    /**
     * Demonstrates basic constraint violations
     */
    private static void demonstrateBasicViolations(Validator validator) {
        System.out.println("1. Basic Constraint Violations:");
        
        User invalidUser = new User(null, "ab", "invalid-email", -5);
        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        
        System.out.println("   Violations found: " + violations.size());
        for (ConstraintViolation<User> violation : violations) {
            System.out.println("   - Field: " + violation.getPropertyPath());
            System.out.println("     Invalid Value: " + violation.getInvalidValue());
            System.out.println("     Message: " + violation.getMessage());
            System.out.println();
        }
    }
    
    /**
     * Demonstrates nested object violations
     */
    private static void demonstrateNestedViolations(Validator validator) {
        System.out.println("2. Nested Object Violations:");
        
        Address invalidAddress = new Address(null, "", "123", "US");
        User userWithInvalidAddress = new User("John", "Doe", "john@example.com", 30);
        userWithInvalidAddress.setAddress(invalidAddress);
        
        Set<ConstraintViolation<User>> violations = validator.validate(userWithInvalidAddress);
        
        for (ConstraintViolation<User> violation : violations) {
            System.out.println("   - Path: " + violation.getPropertyPath());
            System.out.println("     Message: " + violation.getMessage());
            System.out.println();
        }
    }
    
    /**
     * Demonstrates collection violations
     */
    private static void demonstrateCollectionViolations(Validator validator) {
        System.out.println("3. Collection Element Violations:");
        
        Order order = new Order();
        order.addItem(new OrderItem("", -10.0, 0)); // Invalid item
        order.addItem(new OrderItem("Valid Item", 50.0, 2)); // Valid item
        
        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        
        for (ConstraintViolation<Order> violation : violations) {
            System.out.println("   - Path: " + violation.getPropertyPath());
            System.out.println("     Message: " + violation.getMessage());
            System.out.println();
        }
    }
    
    /**
     * Demonstrates extracting violation metadata
     */
    private static void demonstrateViolationMetadata(Validator validator) {
        System.out.println("4. Violation Metadata:");
        
        User user = new User(null, "ab", "invalid", -5);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        for (ConstraintViolation<User> violation : violations) {
            System.out.println("   - Property: " + violation.getPropertyPath());
            System.out.println("     Message Template: " + violation.getMessageTemplate());
            System.out.println("     Interpolated Message: " + violation.getMessage());
            System.out.println("     Invalid Value: " + violation.getInvalidValue());
            System.out.println("     Root Bean Class: " + violation.getRootBeanClass().getSimpleName());
            System.out.println("     Constraint: " + violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
            System.out.println();
        }
    }
    
    /**
     * Demonstrates custom violation handling
     */
    private static void demonstrateCustomViolationHandling(Validator validator) {
        System.out.println("5. Custom Violation Handling:");
        
        User user = new User(null, "ab", "invalid", -5);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        ViolationHandler handler = new ViolationHandler();
        Map<String, List<String>> errors = handler.formatViolations(violations);
        
        System.out.println("   Formatted Errors:");
        errors.forEach((field, messages) -> {
            System.out.println("   - " + field + ":");
            messages.forEach(msg -> System.out.println("     * " + msg));
        });
        System.out.println();
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * User entity with validation constraints
 */
class User {
    
    @NotNull(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotNull(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotNull(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age must be less than 150")
    private int age;
    
    @Valid
    private Address address;
    
    public User(String firstName, String lastName, String email, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
    }
    
    // Getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
}

/**
 * Address entity with validation constraints
 */
class Address {
    
    @NotBlank(message = "Street is required")
    private String street;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "Zip code is required")
    @Pattern(regexp = "\\d{5}", message = "Zip code must be 5 digits")
    private String zipCode;
    
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country code must be 2 characters")
    private String country;
    
    public Address(String street, String city, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    // Getters and setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}

/**
 * Order entity with collection validation
 */
class Order {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItem> items = new ArrayList<>();
    
    @NotNull(message = "Order date is required")
    @PastOrPresent(message = "Order date cannot be in the future")
    private LocalDate orderDate;
    
    public Order() {
        this.orderDate = LocalDate.now();
    }
    
    public void addItem(OrderItem item) {
        this.items.add(item);
    }
    
    // Getters and setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
}

/**
 * Order item with validation constraints
 */
class OrderItem {
    
    @NotBlank(message = "Product name is required")
    private String productName;
    
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
    
    public OrderItem(String productName, Double price, int quantity) {
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }
    
    // Getters and setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

// ============================================================================
// Violation Handling
// ============================================================================

/**
 * Service for handling constraint violations
 */
@Service
class ViolationHandler {
    
    /**
     * Formats violations into a map of field errors
     */
    public <T> Map<String, List<String>> formatViolations(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
            .collect(Collectors.groupingBy(
                violation -> violation.getPropertyPath().toString(),
                Collectors.mapping(
                    ConstraintViolation::getMessage,
                    Collectors.toList()
                )
            ));
    }
    
    /**
     * Creates a detailed error response
     */
    public <T> ViolationErrorResponse createErrorResponse(Set<ConstraintViolation<T>> violations) {
        ViolationErrorResponse response = new ViolationErrorResponse();
        
        for (ConstraintViolation<T> violation : violations) {
            ViolationDetail detail = new ViolationDetail(
                violation.getPropertyPath().toString(),
                violation.getMessage(),
                violation.getInvalidValue(),
                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()
            );
            response.addViolation(detail);
        }
        
        return response;
    }
    
    /**
     * Extracts violation summaries
     */
    public <T> List<String> getViolationMessages(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
    }
    
    /**
     * Groups violations by constraint type
     */
    public <T> Map<String, List<ConstraintViolation<T>>> groupByConstraintType(
            Set<ConstraintViolation<T>> violations) {
        return violations.stream()
            .collect(Collectors.groupingBy(
                violation -> violation.getConstraintDescriptor()
                    .getAnnotation()
                    .annotationType()
                    .getSimpleName()
            ));
    }
}

/**
 * Detailed violation information
 */
class ViolationDetail {
    private String field;
    private String message;
    private Object invalidValue;
    private String constraintType;
    
    public ViolationDetail(String field, String message, Object invalidValue, String constraintType) {
        this.field = field;
        this.message = message;
        this.invalidValue = invalidValue;
        this.constraintType = constraintType;
    }
    
    // Getters and setters
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Object getInvalidValue() { return invalidValue; }
    public void setInvalidValue(Object invalidValue) { this.invalidValue = invalidValue; }
    
    public String getConstraintType() { return constraintType; }
    public void setConstraintType(String constraintType) { this.constraintType = constraintType; }
    
    @Override
    public String toString() {
        return String.format("ViolationDetail{field='%s', message='%s', invalidValue=%s, constraintType='%s'}",
            field, message, invalidValue, constraintType);
    }
}

/**
 * Violation error response
 */
class ViolationErrorResponse {
    private List<ViolationDetail> violations = new ArrayList<>();
    private int violationCount;
    
    public void addViolation(ViolationDetail detail) {
        violations.add(detail);
        violationCount = violations.size();
    }
    
    public List<ViolationDetail> getViolations() { return violations; }
    public int getViolationCount() { return violationCount; }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating violation handling in REST APIs
 */
@RestController
@RequestMapping("/api/violations")
class ViolationController {
    
    private final Validator validator;
    private final ViolationHandler violationHandler;
    
    public ViolationController(ViolationHandler violationHandler) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
        this.violationHandler = violationHandler;
    }
    
    /**
     * Validates user and returns violations
     */
    @PostMapping("/validate-user")
    public ResponseEntity<?> validateUser(@RequestBody User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        if (violations.isEmpty()) {
            return ResponseEntity.ok("User is valid");
        }
        
        ViolationErrorResponse errorResponse = violationHandler.createErrorResponse(violations);
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Validates order with detailed error formatting
     */
    @PostMapping("/validate-order")
    public ResponseEntity<?> validateOrder(@RequestBody Order order) {
        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        
        if (violations.isEmpty()) {
            return ResponseEntity.ok("Order is valid");
        }
        
        Map<String, List<String>> errors = violationHandler.formatViolations(violations);
        return ResponseEntity.badRequest().body(errors);
    }
    
    /**
     * Exception handler for ConstraintViolationException
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        ViolationErrorResponse errorResponse = violationHandler.createErrorResponse(ex.getConstraintViolations());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
