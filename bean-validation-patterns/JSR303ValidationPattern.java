package com.example.beanvalidation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * JSR-303 Bean Validation Pattern
 * 
 * Demonstrates JSR-303 (Bean Validation 1.0) standard annotations for data validation.
 * JSR-303 provides declarative validation constraints:
 * - Null/not-null checks
 * - Size/length validation
 * - Range validation
 * - Pattern matching
 * - Boolean assertions
 * 
 * Key Features:
 * - Standard validation annotations
 * - Declarative constraints
 * - Custom error messages
 * - Nested validation
 * - Collection validation
 * 
 * Use Cases:
 * - DTO/form validation
 * - Entity validation
 * - Method parameter validation
 * - REST API input validation
 * - Domain model constraints
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class JSR303ValidationPattern {

    public static void main(String[] args) {
        SpringApplication.run(JSR303ValidationPattern.class, args);
    }

    // =========================================================================
    // BASIC JSR-303 VALIDATIONS
    // =========================================================================

    /**
     * User DTO with basic JSR-303 validations
     */
    public static class UserDTO {
        
        @NotNull(message = "User ID cannot be null")
        private Long id;
        
        @NotNull(message = "Username is required")
        @NotEmpty(message = "Username cannot be empty")
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        private String username;
        
        @NotNull(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
        
        @NotNull(message = "Age is required")
        @Min(value = 18, message = "Must be at least 18 years old")
        @Max(value = 120, message = "Age must not exceed 120")
        private Integer age;
        
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        private String phoneNumber;
        
        @AssertTrue(message = "Terms and conditions must be accepted")
        private Boolean termsAccepted;
        
        @AssertFalse(message = "Account must not be banned")
        private Boolean banned;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public Boolean getTermsAccepted() { return termsAccepted; }
        public void setTermsAccepted(Boolean termsAccepted) { this.termsAccepted = termsAccepted; }

        public Boolean getBanned() { return banned; }
        public void setBanned(Boolean banned) { this.banned = banned; }
    }

    /**
     * Product DTO with numeric validations
     */
    public static class ProductDTO {
        
        @NotNull
        @NotBlank
        @Size(min = 1, max = 100)
        private String name;
        
        @NotNull
        @Size(max = 500)
        private String description;
        
        @NotNull
        @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
        @DecimalMax(value = "999999.99", message = "Price must not exceed 999999.99")
        @Digits(integer = 6, fraction = 2, message = "Price must have max 6 digits and 2 decimal places")
        private BigDecimal price;
        
        @NotNull
        @Min(0)
        @Max(10000)
        private Integer stockQuantity;
        
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private BigDecimal discountRate;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

        public BigDecimal getDiscountRate() { return discountRate; }
        public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }
    }

    /**
     * Order DTO with temporal validations
     */
    public static class OrderDTO {
        
        @NotNull
        private Long orderId;
        
        @NotNull
        @Past(message = "Order date must be in the past")
        private LocalDate orderDate;
        
        @Future(message = "Delivery date must be in the future")
        private LocalDate deliveryDate;
        
        @NotNull
        @Size(min = 1, message = "Order must contain at least one item")
        @Valid  // Validate nested objects
        private List<OrderItemDTO> items;
        
        @NotNull
        @DecimalMin("0.01")
        private BigDecimal totalAmount;
        
        // Getters and setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }

        public LocalDate getOrderDate() { return orderDate; }
        public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

        public LocalDate getDeliveryDate() { return deliveryDate; }
        public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

        public List<OrderItemDTO> getItems() { return items; }
        public void setItems(List<OrderItemDTO> items) { this.items = items; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }

    /**
     * Nested validation example
     */
    public static class OrderItemDTO {
        
        @NotNull
        private Long productId;
        
        @NotNull
        @Min(1)
        @Max(100)
        private Integer quantity;
        
        @NotNull
        @DecimalMin("0.01")
        private BigDecimal unitPrice;
        
        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }

    /**
     * Address DTO with pattern validations
     */
    public static class AddressDTO {
        
        @NotBlank
        @Size(min = 5, max = 100)
        private String street;
        
        @NotBlank
        @Size(min = 2, max = 50)
        private String city;
        
        @NotBlank
        @Size(min = 2, max = 50)
        private String state;
        
        @NotBlank
        @Pattern(regexp = "\\d{5}(-\\d{4})?", message = "Invalid ZIP code format (use 12345 or 12345-6789)")
        private String zipCode;
        
        @NotBlank
        @Size(min = 2, max = 2)
        private String country;
        
        // Getters and setters
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

    // =========================================================================
    // REST CONTROLLERS WITH VALIDATION
    // =========================================================================

    @RestController
    @RequestMapping("/api/users")
    @Validated  // Enable method-level validation
    public static class UserController {
        
        /**
         * Create user with @Valid triggering validation
         */
        @PostMapping
        public String createUser(@Valid @RequestBody UserDTO user) {
            System.out.println("Creating user: " + user.getUsername());
            return "User created successfully";
        }
        
        /**
         * Update user
         */
        @PutMapping("/{id}")
        public String updateUser(
                @PathVariable @Min(1) Long id,
                @Valid @RequestBody UserDTO user) {
            System.out.println("Updating user: " + id);
            return "User updated successfully";
        }
        
        /**
         * Get user by ID with parameter validation
         */
        @GetMapping("/{id}")
        public String getUser(@PathVariable @Min(value = 1, message = "ID must be at least 1") Long id) {
            System.out.println("Getting user: " + id);
            return "User details for ID: " + id;
        }
        
        /**
         * Search users with query validation
         */
        @GetMapping("/search")
        public String searchUsers(
                @RequestParam @NotBlank @Size(min = 3) String query) {
            System.out.println("Searching users: " + query);
            return "Search results for: " + query;
        }
    }

    @RestController
    @RequestMapping("/api/products")
    public static class ProductController {
        
        @PostMapping
        public String createProduct(@Valid @RequestBody ProductDTO product) {
            System.out.println("Creating product: " + product.getName());
            return "Product created successfully";
        }
        
        @PutMapping("/{id}")
        public String updateProduct(
                @PathVariable Long id,
                @Valid @RequestBody ProductDTO product) {
            System.out.println("Updating product: " + id);
            return "Product updated successfully";
        }
    }

    @RestController
    @RequestMapping("/api/orders")
    public static class OrderController {
        
        @PostMapping
        public String createOrder(@Valid @RequestBody OrderDTO order) {
            System.out.println("Creating order: " + order.getOrderId());
            System.out.println("Items count: " + order.getItems().size());
            return "Order created successfully";
        }
    }

    // =========================================================================
    // SERVICE LAYER VALIDATION
    // =========================================================================

    @Service
    @Validated  // Enable method parameter validation
    public static class UserService {
        
        /**
         * Method parameter validation
         */
        public void createUser(@Valid UserDTO user) {
            System.out.println("Service: Creating user " + user.getUsername());
        }
        
        /**
         * Parameter constraints
         */
        public void deleteUser(@NotNull @Min(1) Long userId) {
            System.out.println("Service: Deleting user " + userId);
        }
        
        /**
         * Multiple validated parameters
         */
        public void updateEmail(
                @NotNull @Min(1) Long userId,
                @NotNull @Email String newEmail) {
            System.out.println("Service: Updating email for user " + userId + " to " + newEmail);
        }
    }
}

/**
 * DOCUMENTATION
 * 
 * JSR-303 Bean Validation:
 * 
 * 1. Standard Annotations:
 *    - @NotNull: Value must not be null
 *    - @NotEmpty: String/Collection must not be empty
 *    - @NotBlank: String must not be blank (trimmed)
 *    - @Null: Value must be null
 * 
 * 2. String Constraints:
 *    - @Size(min, max): String/Collection length
 *    - @Pattern(regexp): Regex pattern match
 *    - @Email: Valid email format
 * 
 * 3. Numeric Constraints:
 *    - @Min(value): Minimum value
 *    - @Max(value): Maximum value
 *    - @DecimalMin(value): Decimal minimum
 *    - @DecimalMax(value): Decimal maximum
 *    - @Digits(integer, fraction): Digit constraints
 *    - @Positive: Must be positive
 *    - @PositiveOrZero: Must be >= 0
 *    - @Negative: Must be negative
 *    - @NegativeOrZero: Must be <= 0
 * 
 * 4. Temporal Constraints:
 *    - @Past: Date must be in past
 *    - @PastOrPresent: Past or present
 *    - @Future: Date must be in future
 *    - @FutureOrPresent: Future or present
 * 
 * 5. Boolean Assertions:
 *    - @AssertTrue: Must be true
 *    - @AssertFalse: Must be false
 * 
 * 6. @Valid Annotation:
 *    - Triggers validation cascade
 *    - Use on nested objects
 *    - Use on collections
 *    - Use on method parameters
 * 
 * 7. Custom Messages:
 *    - message attribute for custom error messages
 *    - Example: @NotNull(message = "Field is required")
 *    - Can use MessageSource for i18n
 * 
 * 8. Validation Groups:
 *    - group attribute for conditional validation
 *    - Example: @NotNull(groups = Create.class)
 *    - Validate different fields for different operations
 * 
 * 9. Spring Integration:
 *    - @Valid on controller method parameters
 *    - @Validated on class for method validation
 *    - BindingResult for error handling
 *    - Automatic 400 Bad Request on validation failure
 * 
 * 10. Usage Locations:
 *     - DTOs/form objects
 *     - JPA entities
 *     - Method parameters
 *     - Method return values
 *     - Request/response objects
 * 
 * 11. Best Practices:
 *     - Use @NotBlank instead of @NotNull + @NotEmpty for strings
 *     - Provide meaningful error messages
 *     - Validate at API boundary
 *     - Use nested validation for complex objects
 *     - Combine with custom validators when needed
 * 
 * 12. Validation Execution:
 *     - Automatic in REST controllers with @Valid
 *     - Manual: validator.validate(object)
 *     - Returns Set<ConstraintViolation>
 *     - Each violation has message, property path, invalid value
 */
