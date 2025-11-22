package com.example.validation.method;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Method Validation Pattern - Demonstrates Method-Level Validation
 * 
 * This pattern shows how to:
 * 1. Enable method validation with @Validated
 * 2. Validate method parameters
 * 3. Validate method return values
 * 4. Configure MethodValidationPostProcessor
 * 5. Validate constructor parameters
 * 6. Handle validation exceptions
 * 7. Validate service layer methods
 * 8. Validate path variables and request params
 * 9. Cross-parameter validation
 * 10. Custom method validators
 * 
 * Key Concepts:
 * - @Validated: Enables method validation on a class
 * - MethodValidationPostProcessor: Spring bean that enables method validation
 * - Parameter Validation: Validates method input parameters
 * - Return Value Validation: Validates method outputs
 * - ConstraintViolationException: Thrown on validation failure
 * 
 * Method Validation vs Bean Validation:
 * - Bean Validation: Validates object state
 * - Method Validation: Validates method calls
 * - Use @Valid for object validation
 * - Use @Validated for method validation
 * 
 * Validation Points:
 * 1. Service Methods - Business logic validation
 * 2. Controller Methods - Request parameter validation
 * 3. Repository Methods - Data access validation
 * 4. Constructors - Object creation validation
 * 5. Return Values - Output validation
 * 
 * Dependencies:
 * - spring-boot-starter-validation
 * - spring-context
 * - jakarta.validation-api
 * - hibernate-validator
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class MethodValidationPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(MethodValidationPattern.class, args);
        demonstrateMethodValidation(context);
    }
    
    /**
     * Configures method validation processor
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
    
    /**
     * Demonstrates various method validation scenarios
     */
    private static void demonstrateMethodValidation(org.springframework.context.ApplicationContext context) {
        System.out.println("=== Method Validation Pattern Demonstrations ===\n");
        
        UserService userService = context.getBean(UserService.class);
        ProductService productService = context.getBean(ProductService.class);
        OrderService orderService = context.getBean(OrderService.class);
        
        // Demo 1: Parameter validation
        demonstrateParameterValidation(userService);
        
        // Demo 2: Return value validation
        demonstrateReturnValueValidation(productService);
        
        // Demo 3: Multiple parameter validation
        demonstrateMultipleParameterValidation(orderService);
        
        // Demo 4: Path variable validation
        demonstratePathVariableValidation(userService);
        
        // Demo 5: Cross-parameter validation
        demonstrateCrossParameterValidation(orderService);
    }
    
    /**
     * Demonstrates parameter validation
     */
    private static void demonstrateParameterValidation(UserService service) {
        System.out.println("1. Parameter Validation:");
        
        try {
            service.createUser("ab", "invalid-email"); // Invalid
            System.out.println("   ✗ Should have failed validation");
        } catch (ConstraintViolationException e) {
            System.out.println("   ✓ Validation failed as expected:");
            e.getConstraintViolations().forEach(v -> 
                System.out.println("     - " + v.getMessage())
            );
        }
        System.out.println();
    }
    
    /**
     * Demonstrates return value validation
     */
    private static void demonstrateReturnValueValidation(ProductService service) {
        System.out.println("2. Return Value Validation:");
        
        try {
            service.getInvalidProduct(); // Returns invalid product
            System.out.println("   ✗ Should have failed validation");
        } catch (ConstraintViolationException e) {
            System.out.println("   ✓ Return value validation failed as expected:");
            e.getConstraintViolations().forEach(v -> 
                System.out.println("     - " + v.getMessage())
            );
        }
        System.out.println();
    }
    
    /**
     * Demonstrates multiple parameter validation
     */
    private static void demonstrateMultipleParameterValidation(OrderService service) {
        System.out.println("3. Multiple Parameter Validation:");
        
        try {
            service.createOrder(-1L, new BigDecimal("-100"), 0); // All invalid
            System.out.println("   ✗ Should have failed validation");
        } catch (ConstraintViolationException e) {
            System.out.println("   ✓ Validation failed as expected:");
            e.getConstraintViolations().forEach(v -> 
                System.out.println("     - " + v.getMessage())
            );
        }
        System.out.println();
    }
    
    /**
     * Demonstrates path variable validation
     */
    private static void demonstratePathVariableValidation(UserService service) {
        System.out.println("4. Path Variable Validation:");
        
        try {
            service.getUserById(-5L); // Negative ID
            System.out.println("   ✗ Should have failed validation");
        } catch (ConstraintViolationException e) {
            System.out.println("   ✓ Validation failed as expected:");
            e.getConstraintViolations().forEach(v -> 
                System.out.println("     - " + v.getMessage())
            );
        }
        System.out.println();
    }
    
    /**
     * Demonstrates cross-parameter validation
     */
    private static void demonstrateCrossParameterValidation(OrderService service) {
        System.out.println("5. Cross-Parameter Validation:");
        
        try {
            LocalDate start = LocalDate.now().plusDays(10);
            LocalDate end = LocalDate.now();
            service.getOrdersByDateRange(start, end); // End before start
            System.out.println("   ✗ Should have failed validation");
        } catch (ConstraintViolationException e) {
            System.out.println("   ✓ Validation failed as expected:");
            e.getConstraintViolations().forEach(v -> 
                System.out.println("     - " + v.getMessage())
            );
        }
        System.out.println();
    }
}

// ============================================================================
// Service Layer with Method Validation
// ============================================================================

/**
 * User service with method validation
 */
@Service
@Validated
class UserService {
    
    /**
     * Creates user with validated parameters
     */
    public UserEntity createUser(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email) {
        
        System.out.println("   Creating user: " + username);
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
    
    /**
     * Updates user with validated parameters and groups
     */
    public void updateUser(
            @NotNull @Positive Long userId,
            @NotBlank @Size(min = 3, max = 50) String username,
            @Email String email) {
        
        System.out.println("   Updating user " + userId);
    }
    
    /**
     * Gets user by ID with validated parameter
     */
    public UserEntity getUserById(@NotNull @Positive Long userId) {
        System.out.println("   Getting user by ID: " + userId);
        UserEntity user = new UserEntity();
        user.setId(userId);
        return user;
    }
    
    /**
     * Searches users with validated parameters
     */
    public List<UserEntity> searchUsers(
            @Size(min = 2, max = 100) String query,
            @Min(1) @Max(100) int limit) {
        
        System.out.println("   Searching users: " + query);
        return new ArrayList<>();
    }
    
    /**
     * Deletes user with validated ID
     */
    public void deleteUser(@NotNull @Positive Long userId) {
        System.out.println("   Deleting user: " + userId);
    }
    
    /**
     * Gets active users - validates return value
     */
    @Valid
    public List<@Valid UserEntity> getActiveUsers() {
        List<UserEntity> users = new ArrayList<>();
        
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        users.add(user);
        
        return users;
    }
}

/**
 * Product service with return value validation
 */
@Service
@Validated
class ProductService {
    
    /**
     * Gets product by SKU with validated return value
     */
    @Valid
    public ProductEntity getProductBySku(@NotBlank String sku) {
        ProductEntity product = new ProductEntity();
        product.setSku(sku);
        product.setName("Valid Product");
        product.setPrice(new BigDecimal("99.99"));
        return product;
    }
    
    /**
     * Returns invalid product to demonstrate return value validation
     */
    @Valid
    public ProductEntity getInvalidProduct() {
        ProductEntity product = new ProductEntity();
        product.setSku("INVALID");
        product.setPrice(new BigDecimal("-10")); // Invalid price
        return product;
    }
    
    /**
     * Creates product with validated parameters
     */
    public ProductEntity createProduct(
            @NotBlank @Pattern(regexp = "^[A-Z]{3}-\\d{6}$") String sku,
            @NotBlank @Size(min = 3, max = 100) String name,
            @NotNull @DecimalMin("0.01") BigDecimal price) {
        
        ProductEntity product = new ProductEntity();
        product.setSku(sku);
        product.setName(name);
        product.setPrice(price);
        return product;
    }
    
    /**
     * Updates product price with validated parameters
     */
    public void updatePrice(
            @NotBlank String sku,
            @NotNull @DecimalMin(value = "0.01", message = "Price must be greater than 0") BigDecimal newPrice,
            @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal discount) {
        
        System.out.println("   Updating price for " + sku + " to " + newPrice);
    }
    
    /**
     * Validates list of products
     */
    public void validateProducts(@NotEmpty List<@Valid ProductEntity> products) {
        System.out.println("   Validating " + products.size() + " products");
    }
}

/**
 * Order service with multiple parameter validation
 */
@Service
@Validated
class OrderService {
    
    /**
     * Creates order with multiple validated parameters
     */
    public OrderEntity createOrder(
            @NotNull @Positive Long customerId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @Min(1) int itemCount) {
        
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setCustomerId(customerId);
        order.setAmount(amount);
        order.setItemCount(itemCount);
        return order;
    }
    
    /**
     * Updates order status with validated parameters
     */
    public void updateOrderStatus(
            @NotNull @Positive Long orderId,
            @NotBlank @Pattern(regexp = "PENDING|PROCESSING|SHIPPED|DELIVERED|CANCELLED") String status) {
        
        System.out.println("   Updating order " + orderId + " to status " + status);
    }
    
    /**
     * Gets orders by date range with cross-parameter validation
     */
    public List<OrderEntity> getOrdersByDateRange(
            @NotNull @PastOrPresent LocalDate startDate,
            @NotNull LocalDate endDate) {
        
        // Custom cross-parameter validation logic
        if (endDate.isBefore(startDate)) {
            throw new ConstraintViolationException(
                "End date must be after start date", Collections.emptySet());
        }
        
        System.out.println("   Getting orders from " + startDate + " to " + endDate);
        return new ArrayList<>();
    }
    
    /**
     * Calculates order total with validated parameters
     */
    public BigDecimal calculateTotal(
            @NotEmpty List<@NotNull @DecimalMin("0.01") BigDecimal> itemPrices,
            @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal taxRate) {
        
        BigDecimal subtotal = itemPrices.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return subtotal.multiply(BigDecimal.ONE.add(taxRate));
    }
    
    /**
     * Validates return value
     */
    @Valid
    public OrderEntity getOrderById(@NotNull @Positive Long orderId) {
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setCustomerId(1L);
        order.setAmount(new BigDecimal("100.00"));
        order.setItemCount(5);
        return order;
    }
}

// ============================================================================
// Domain Entities
// ============================================================================

/**
 * User entity with validation constraints
 */
class UserEntity {
    
    @NotNull
    @Positive
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    
    @NotBlank
    @Email
    private String email;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

/**
 * Product entity with validation constraints
 */
class ProductEntity {
    
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}-\\d{6}$", message = "SKU format must be XXX-NNNNNN")
    private String sku;
    
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
    
    @NotNull
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Min(0)
    private int stock;
    
    // Getters and setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}

/**
 * Order entity with validation constraints
 */
class OrderEntity {
    
    @NotNull
    @Positive
    private Long id;
    
    @NotNull
    @Positive
    private Long customerId;
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    
    @Min(1)
    private int itemCount;
    
    @PastOrPresent
    private LocalDate orderDate = LocalDate.now();
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
}

// ============================================================================
// REST Controller with Method Validation
// ============================================================================

/**
 * Controller demonstrating method validation in REST endpoints
 */
@RestController
@RequestMapping("/api/method-validation")
@Validated
class MethodValidationController {
    
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    
    public MethodValidationController(UserService userService, 
                                     ProductService productService,
                                     OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
    }
    
    /**
     * Creates user - validates request parameters
     */
    @PostMapping("/users")
    public ResponseEntity<UserEntity> createUser(
            @RequestParam @NotBlank @Size(min = 3, max = 50) String username,
            @RequestParam @NotBlank @Email String email) {
        
        UserEntity user = userService.createUser(username, email);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Gets user by ID - validates path variable
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserEntity> getUser(
            @PathVariable @NotNull @Positive Long id) {
        
        UserEntity user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Searches users - validates query parameters
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<UserEntity>> searchUsers(
            @RequestParam @Size(min = 2, max = 100) String query,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        List<UserEntity> users = userService.searchUsers(query, limit);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Creates product - validates parameters
     */
    @PostMapping("/products")
    public ResponseEntity<ProductEntity> createProduct(
            @RequestParam @NotBlank @Pattern(regexp = "^[A-Z]{3}-\\d{6}$") String sku,
            @RequestParam @NotBlank @Size(min = 3, max = 100) String name,
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal price) {
        
        ProductEntity product = productService.createProduct(sku, name, price);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Gets orders by date range - validates date parameters
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderEntity>> getOrders(
            @RequestParam @NotNull @PastOrPresent LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate) {
        
        List<OrderEntity> orders = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Exception handler for validation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationException(
            ConstraintViolationException ex) {
        
        Map<String, List<String>> errors = new HashMap<>();
        
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            
            errors.computeIfAbsent(propertyPath, k -> new ArrayList<>()).add(message);
        });
        
        return ResponseEntity.badRequest().body(errors);
    }
}

// ============================================================================
// Configuration
// ============================================================================

/**
 * Validation configuration
 */
class ValidationConfiguration {
    
    /**
     * Custom method validation post processor with configuration
     */
    @Bean
    public MethodValidationPostProcessor customMethodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        // Can customize validator factory here if needed
        return processor;
    }
}
