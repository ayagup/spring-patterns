package com.example.validation.beanvalidation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Bean Validation Pattern - Demonstrates JSR-303/380 Bean Validation
 * 
 * This pattern shows how to:
 * 1. Use standard validation annotations
 * 2. Validate beans programmatically
 * 3. Cascade validation with @Valid
 * 4. Validate collections
 * 5. Use built-in constraints
 * 6. Validate method parameters
 * 7. Validate method return values
 * 8. Handle validation errors
 * 9. Configure validators
 * 10. Customize error messages
 * 
 * Key Concepts:
 * - @NotNull: Value must not be null
 * - @NotEmpty: Collection/String must not be empty
 * - @NotBlank: String must not be blank
 * - @Size: Size must be within bounds
 * - @Min/@Max: Numeric value constraints
 * - @DecimalMin/@DecimalMax: Decimal constraints
 * - @Positive/@Negative: Sign constraints
 * - @Past/@Future: Date constraints
 * - @Email: Email format validation
 * - @Pattern: Regex validation
 * - @Valid: Cascade validation
 * 
 * Standard Constraints:
 * 1. Null Checks: @NotNull, @Null
 * 2. Boolean: @AssertTrue, @AssertFalse
 * 3. Numeric: @Min, @Max, @DecimalMin, @DecimalMax, @Positive, @Negative
 * 4. Size: @Size (String, Collection, Array, Map)
 * 5. Strings: @NotEmpty, @NotBlank, @Email, @Pattern
 * 6. Date/Time: @Past, @PastOrPresent, @Future, @FutureOrPresent
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
public class BeanValidationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(BeanValidationPattern.class, args);
        demonstrateBeanValidation();
    }
    
    /**
     * Demonstrates various bean validation scenarios
     */
    private static void demonstrateBeanValidation() {
        System.out.println("=== Bean Validation Pattern Demonstrations ===\n");
        
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        
        // Demo 1: Basic constraints
        demonstrateBasicConstraints(validator);
        
        // Demo 2: String constraints
        demonstrateStringConstraints(validator);
        
        // Demo 3: Numeric constraints
        demonstrateNumericConstraints(validator);
        
        // Demo 4: Date/Time constraints
        demonstrateDateTimeConstraints(validator);
        
        // Demo 5: Collection validation
        demonstrateCollectionValidation(validator);
        
        // Demo 6: Cascading validation
        demonstrateCascadingValidation(validator);
    }
    
    /**
     * Demonstrates basic constraint validations
     */
    private static void demonstrateBasicConstraints(Validator validator) {
        System.out.println("1. Basic Constraints (@NotNull, @NotEmpty, @NotBlank):");
        
        BasicConstraintsBean bean = new BasicConstraintsBean();
        bean.setNullableField("value");
        bean.setNotEmptyString("  "); // Blank but not empty
        // notBlankString is null
        
        Set<ConstraintViolation<BasicConstraintsBean>> violations = validator.validate(bean);
        printViolations(violations);
    }
    
    /**
     * Demonstrates string constraint validations
     */
    private static void demonstrateStringConstraints(Validator validator) {
        System.out.println("2. String Constraints (@Email, @Pattern, @Size):");
        
        StringConstraintsBean bean = new StringConstraintsBean();
        bean.setEmail("invalid-email");
        bean.setPhoneNumber("123"); // Invalid format
        bean.setUsername("ab"); // Too short
        bean.setPassword("weak"); // Doesn't match pattern
        
        Set<ConstraintViolation<StringConstraintsBean>> violations = validator.validate(bean);
        printViolations(violations);
    }
    
    /**
     * Demonstrates numeric constraint validations
     */
    private static void demonstrateNumericConstraints(Validator validator) {
        System.out.println("3. Numeric Constraints (@Min, @Max, @Positive, @Negative):");
        
        NumericConstraintsBean bean = new NumericConstraintsBean();
        bean.setAge(-5); // Must be positive
        bean.setQuantity(1001); // Exceeds max
        bean.setPrice(new BigDecimal("-10.00")); // Must be positive
        bean.setDiscount(new BigDecimal("1.5")); // Exceeds max
        
        Set<ConstraintViolation<NumericConstraintsBean>> violations = validator.validate(bean);
        printViolations(violations);
    }
    
    /**
     * Demonstrates date/time constraint validations
     */
    private static void demonstrateDateTimeConstraints(Validator validator) {
        System.out.println("4. Date/Time Constraints (@Past, @Future, @PastOrPresent):");
        
        DateTimeConstraintsBean bean = new DateTimeConstraintsBean();
        bean.setBirthDate(LocalDate.now().plusDays(1)); // Future date
        bean.setExpiryDate(LocalDate.now().minusDays(1)); // Past date
        bean.setCreatedAt(LocalDateTime.now().plusHours(1)); // Future time
        
        Set<ConstraintViolation<DateTimeConstraintsBean>> violations = validator.validate(bean);
        printViolations(violations);
    }
    
    /**
     * Demonstrates collection validation
     */
    private static void demonstrateCollectionValidation(Validator validator) {
        System.out.println("5. Collection Validation:");
        
        CollectionBean bean = new CollectionBean();
        bean.setTags(Arrays.asList()); // Empty list
        bean.setScores(Arrays.asList(50, 60, 70, 80, 90, 100, 110)); // Too many elements
        
        Set<ConstraintViolation<CollectionBean>> violations = validator.validate(bean);
        printViolations(violations);
    }
    
    /**
     * Demonstrates cascading validation with @Valid
     */
    private static void demonstrateCascadingValidation(Validator validator) {
        System.out.println("6. Cascading Validation with @Valid:");
        
        // Create customer with invalid nested objects
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("invalid-email");
        
        Address address = new Address();
        address.setStreet(""); // Blank
        address.setCity("New York");
        address.setZipCode("123"); // Invalid
        customer.setAddress(address);
        
        CreditCard card = new CreditCard();
        card.setNumber("1234"); // Too short
        card.setExpiryDate(LocalDate.now().minusMonths(1)); // Expired
        customer.setCreditCard(card);
        
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        printViolations(violations);
    }
    
    /**
     * Helper method to print violations
     */
    private static <T> void printViolations(Set<ConstraintViolation<T>> violations) {
        if (violations.isEmpty()) {
            System.out.println("   ✓ No violations found\n");
        } else {
            System.out.println("   Found " + violations.size() + " violation(s):");
            violations.forEach(v -> 
                System.out.println("   - " + v.getPropertyPath() + ": " + v.getMessage())
            );
            System.out.println();
        }
    }
}

// ============================================================================
// Basic Constraints Beans
// ============================================================================

/**
 * Bean demonstrating basic constraints
 */
class BasicConstraintsBean {
    
    @NotNull(message = "Required field cannot be null")
    private String requiredField;
    
    private String nullableField; // No constraint
    
    @NotEmpty(message = "String must not be empty")
    private String notEmptyString;
    
    @NotBlank(message = "String must not be blank")
    private String notBlankString;
    
    @AssertTrue(message = "Terms must be accepted")
    private Boolean termsAccepted;
    
    // Getters and setters
    public String getRequiredField() { return requiredField; }
    public void setRequiredField(String requiredField) { this.requiredField = requiredField; }
    
    public String getNullableField() { return nullableField; }
    public void setNullableField(String nullableField) { this.nullableField = nullableField; }
    
    public String getNotEmptyString() { return notEmptyString; }
    public void setNotEmptyString(String notEmptyString) { this.notEmptyString = notEmptyString; }
    
    public String getNotBlankString() { return notBlankString; }
    public void setNotBlankString(String notBlankString) { this.notBlankString = notBlankString; }
    
    public Boolean getTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(Boolean termsAccepted) { this.termsAccepted = termsAccepted; }
}

/**
 * Bean demonstrating string constraints
 */
class StringConstraintsBean {
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^\\d{3}-\\d{3}-\\d{4}$", message = "Phone number must match format XXX-XXX-XXXX")
    private String phoneNumber;
    
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$",
        message = "Password must contain at least 8 characters, one digit, one lowercase, one uppercase, and one special character"
    )
    private String password;
    
    @NotBlank
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

/**
 * Bean demonstrating numeric constraints
 */
class NumericConstraintsBean {
    
    @Positive(message = "Age must be positive")
    private Integer age;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000, message = "Quantity cannot exceed 1000")
    private Integer quantity;
    
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999999.99")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount must be non-negative")
    @DecimalMax(value = "1.0", inclusive = true, message = "Discount cannot exceed 1.0")
    private BigDecimal discount;
    
    @Negative(message = "Debt must be negative")
    private BigDecimal debt;
    
    @PositiveOrZero(message = "Balance must be positive or zero")
    private BigDecimal balance;
    
    // Getters and setters
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    
    public BigDecimal getDebt() { return debt; }
    public void setDebt(BigDecimal debt) { this.debt = debt; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}

/**
 * Bean demonstrating date/time constraints
 */
class DateTimeConstraintsBean {
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;
    
    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDateTime createdAt;
    
    @FutureOrPresent(message = "Scheduled date must be in the future or present")
    private LocalDateTime scheduledAt;
    
    // Getters and setters
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
}

/**
 * Bean demonstrating collection validation
 */
class CollectionBean {
    
    @NotEmpty(message = "Tags list cannot be empty")
    @Size(min = 1, max = 10, message = "Tags list must have between 1 and 10 items")
    private List<String> tags;
    
    @NotNull(message = "Scores cannot be null")
    @Size(max = 5, message = "Maximum 5 scores allowed")
    private List<@Min(0) @Max(100) Integer> scores;
    
    @NotEmpty(message = "Map cannot be empty")
    private Map<@NotBlank String, @NotNull String> properties;
    
    // Getters and setters
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public List<Integer> getScores() { return scores; }
    public void setScores(List<Integer> scores) { this.scores = scores; }
    
    public Map<String, String> getProperties() { return properties; }
    public void setProperties(Map<String, String> properties) { this.properties = properties; }
}

// ============================================================================
// Cascading Validation Beans
// ============================================================================

/**
 * Customer with nested validated objects
 */
class Customer {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Valid email is required")
    private String email;
    
    @Valid
    @NotNull(message = "Address is required")
    private Address address;
    
    @Valid
    private CreditCard creditCard;
    
    @Valid
    @NotEmpty(message = "At least one order is required")
    private List<Order> orders = new ArrayList<>();
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    
    public CreditCard getCreditCard() { return creditCard; }
    public void setCreditCard(CreditCard creditCard) { this.creditCard = creditCard; }
    
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
}

/**
 * Address entity
 */
class Address {
    
    @NotBlank(message = "Street is required")
    private String street;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "Zip code is required")
    @Pattern(regexp = "\\d{5}", message = "Zip code must be 5 digits")
    private String zipCode;
    
    @NotBlank
    @Size(min = 2, max = 2, message = "Country code must be 2 characters")
    private String country;
    
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
 * Credit card entity
 */
class CreditCard {
    
    @NotBlank(message = "Card number is required")
    @Size(min = 13, max = 19, message = "Card number must be between 13 and 19 digits")
    @Pattern(regexp = "\\d+", message = "Card number must contain only digits")
    private String number;
    
    @NotBlank(message = "Cardholder name is required")
    private String holderName;
    
    @Future(message = "Card must not be expired")
    private LocalDate expiryDate;
    
    @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits")
    private String cvv;
    
    // Getters and setters
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}

/**
 * Order entity
 */
class Order {
    
    @NotNull(message = "Order ID is required")
    private Long id;
    
    @PastOrPresent(message = "Order date cannot be in the future")
    private LocalDate orderDate;
    
    @DecimalMin(value = "0.01", message = "Total must be greater than 0")
    private BigDecimal total;
    
    @Valid
    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItem> items = new ArrayList<>();
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}

/**
 * Order item entity
 */
class OrderItem {
    
    @NotBlank(message = "Product name is required")
    private String productName;
    
    @Positive(message = "Quantity must be positive")
    private int quantity;
    
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    
    // Getters and setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}

// ============================================================================
// Validation Service
// ============================================================================

/**
 * Service demonstrating programmatic validation
 */
@Service
class BeanValidationService {
    
    private final Validator validator;
    
    public BeanValidationService() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }
    
    /**
     * Validates a bean and returns validation result
     */
    public <T> ValidationResult<T> validate(T bean) {
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        return new ValidationResult<>(bean, violations);
    }
    
    /**
     * Validates specific properties
     */
    public <T> Set<ConstraintViolation<T>> validateProperty(T bean, String propertyName) {
        return validator.validateProperty(bean, propertyName);
    }
    
    /**
     * Validates a property value
     */
    public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value) {
        return validator.validateValue(beanType, propertyName, value);
    }
}

/**
 * Validation result wrapper
 */
class ValidationResult<T> {
    private final T bean;
    private final Set<ConstraintViolation<T>> violations;
    
    public ValidationResult(T bean, Set<ConstraintViolation<T>> violations) {
        this.bean = bean;
        this.violations = violations;
    }
    
    public boolean isValid() {
        return violations.isEmpty();
    }
    
    public T getBean() { return bean; }
    public Set<ConstraintViolation<T>> getViolations() { return violations; }
    
    public List<String> getErrorMessages() {
        List<String> messages = new ArrayList<>();
        violations.forEach(v -> messages.add(v.getMessage()));
        return messages;
    }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating bean validation in REST endpoints
 */
@RestController
@RequestMapping("/api/bean-validation")
class BeanValidationController {
    
    private final BeanValidationService validationService;
    
    public BeanValidationController(BeanValidationService validationService) {
        this.validationService = validationService;
    }
    
    /**
     * Validates customer with automatic validation
     */
    @PostMapping("/customers")
    public ResponseEntity<String> createCustomer(@Valid @RequestBody Customer customer) {
        // @Valid automatically triggers validation
        return ResponseEntity.ok("Customer created successfully");
    }
    
    /**
     * Validates customer programmatically
     */
    @PostMapping("/customers/validate")
    public ResponseEntity<?> validateCustomer(@RequestBody Customer customer) {
        ValidationResult<Customer> result = validationService.validate(customer);
        
        if (result.isValid()) {
            return ResponseEntity.ok("Customer is valid");
        } else {
            return ResponseEntity.badRequest().body(result.getErrorMessages());
        }
    }
}
