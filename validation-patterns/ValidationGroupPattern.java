package com.example.validation.groups;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Validation Group Pattern - Demonstrates Validation Groups
 * 
 * This pattern shows how to:
 * 1. Define validation groups using interfaces
 * 2. Apply different validations per group
 * 3. Use Default group
 * 4. Define group sequences
 * 5. Validate with specific groups
 * 6. Combine multiple groups
 * 7. Create group hierarchies
 * 8. Implement conditional validation
 * 9. Use groups in REST controllers
 * 10. Validate different scenarios
 * 
 * Key Concepts:
 * - Validation Groups: Interfaces marking validation scenarios
 * - Default Group: Default validation group when none specified
 * - Group Sequence: Ordered group validation
 * - @GroupSequence: Defines validation order
 * - Conditional Validation: Different rules per scenario
 * 
 * Group Use Cases:
 * 1. Create vs Update - Different validation rules
 * 2. Partial vs Complete - Progressive validation
 * 3. Step-by-Step Validation - Multi-step forms
 * 4. Role-Based Validation - Different rules per role
 * 5. State-Based Validation - Rules based on object state
 * 
 * Group Benefits:
 * - Reuse same model for different scenarios
 * - Avoid model duplication
 * - Clear validation logic separation
 * - Flexible validation rules
 * - Better maintainability
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
public class ValidationGroupPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(ValidationGroupPattern.class, args);
        demonstrateValidationGroups();
    }
    
    /**
     * Demonstrates various validation group scenarios
     */
    private static void demonstrateValidationGroups() {
        System.out.println("=== Validation Group Pattern Demonstrations ===\n");
        
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        
        // Demo 1: Basic validation groups
        demonstrateBasicGroups(validator);
        
        // Demo 2: Group sequences
        demonstrateGroupSequences(validator);
        
        // Demo 3: Conditional validation with groups
        demonstrateConditionalValidation(validator);
        
        // Demo 4: Multi-step validation
        demonstrateMultiStepValidation(validator);
        
        // Demo 5: Role-based validation
        demonstrateRoleBasedValidation(validator);
    }
    
    /**
     * Demonstrates basic validation groups
     */
    private static void demonstrateBasicGroups(Validator validator) {
        System.out.println("1. Basic Validation Groups:");
        
        UserDto user = new UserDto();
        user.setUsername("john");
        user.setEmail("john@example.com");
        // ID is null
        
        // Validate for Create scenario (ID not required)
        Set<ConstraintViolation<UserDto>> createViolations = 
            validator.validate(user, ValidationGroups.Create.class);
        System.out.println("   Create validation: " + 
            (createViolations.isEmpty() ? "✓ Passed" : "✗ Failed (" + createViolations.size() + " errors)"));
        
        // Validate for Update scenario (ID required)
        Set<ConstraintViolation<UserDto>> updateViolations = 
            validator.validate(user, ValidationGroups.Update.class);
        System.out.println("   Update validation: " + 
            (updateViolations.isEmpty() ? "✓ Passed" : "✗ Failed (" + updateViolations.size() + " errors)"));
        printViolations(updateViolations);
    }
    
    /**
     * Demonstrates group sequences
     */
    private static void demonstrateGroupSequences(Validator validator) {
        System.out.println("2. Group Sequences:");
        
        Product product = new Product();
        product.setName(""); // Fails basic check
        product.setSku("INVALID"); // Would fail advanced check
        product.setPrice(new BigDecimal("-10")); // Would fail advanced check
        
        // Validate with sequence - stops at first failing group
        Set<ConstraintViolation<Product>> violations = 
            validator.validate(product, Product.ValidationSequence.class);
        System.out.println("   Violations from sequence:");
        printViolations(violations);
    }
    
    /**
     * Demonstrates conditional validation
     */
    private static void demonstrateConditionalValidation(Validator validator) {
        System.out.println("3. Conditional Validation:");
        
        OrderDto order = new OrderDto();
        order.setOrderType("ONLINE");
        // Missing shipping address (required for online orders)
        
        Set<ConstraintViolation<OrderDto>> onlineViolations = 
            validator.validate(order, ValidationGroups.OnlineOrder.class);
        System.out.println("   Online order validation:");
        printViolations(onlineViolations);
        
        order.setOrderType("IN_STORE");
        Set<ConstraintViolation<OrderDto>> instoreViolations = 
            validator.validate(order, ValidationGroups.InStoreOrder.class);
        System.out.println("   In-store order validation:");
        printViolations(instoreViolations);
    }
    
    /**
     * Demonstrates multi-step validation
     */
    private static void demonstrateMultiStepValidation(Validator validator) {
        System.out.println("4. Multi-Step Validation:");
        
        RegistrationForm form = new RegistrationForm();
        form.setUsername("john");
        form.setEmail("john@example.com");
        
        // Step 1: Basic info
        Set<ConstraintViolation<RegistrationForm>> step1 = 
            validator.validate(form, ValidationGroups.Step1.class);
        System.out.println("   Step 1 (Basic Info): " + 
            (step1.isEmpty() ? "✓ Passed" : "✗ Failed"));
        
        // Step 2: Contact info (requires phone)
        form.setPhone("123-456-7890");
        Set<ConstraintViolation<RegistrationForm>> step2 = 
            validator.validate(form, ValidationGroups.Step2.class);
        System.out.println("   Step 2 (Contact Info): " + 
            (step2.isEmpty() ? "✓ Passed" : "✗ Failed"));
        
        // Step 3: Complete (requires all fields)
        Set<ConstraintViolation<RegistrationForm>> step3 = 
            validator.validate(form, ValidationGroups.Step3.class);
        System.out.println("   Step 3 (Complete): " + 
            (step3.isEmpty() ? "✓ Passed" : "✗ Failed"));
        printViolations(step3);
    }
    
    /**
     * Demonstrates role-based validation
     */
    private static void demonstrateRoleBasedValidation(Validator validator) {
        System.out.println("5. Role-Based Validation:");
        
        Document document = new Document();
        document.setTitle("Annual Report");
        document.setContent("Report content...");
        
        // Validate for regular user
        Set<ConstraintViolation<Document>> userValidation = 
            validator.validate(document, ValidationGroups.UserRole.class);
        System.out.println("   User role validation: " + 
            (userValidation.isEmpty() ? "✓ Passed" : "✗ Failed"));
        
        // Validate for admin (requires approval status)
        Set<ConstraintViolation<Document>> adminValidation = 
            validator.validate(document, ValidationGroups.AdminRole.class);
        System.out.println("   Admin role validation: " + 
            (adminValidation.isEmpty() ? "✓ Passed" : "✗ Failed"));
        printViolations(adminValidation);
    }
    
    /**
     * Helper method to print violations
     */
    private static <T> void printViolations(Set<ConstraintViolation<T>> violations) {
        if (!violations.isEmpty()) {
            violations.forEach(v -> 
                System.out.println("     - " + v.getPropertyPath() + ": " + v.getMessage())
            );
        }
        System.out.println();
    }
}

// ============================================================================
// Validation Group Definitions
// ============================================================================

/**
 * Validation groups for different scenarios
 */
interface ValidationGroups {
    
    /**
     * Group for create operations
     */
    interface Create {}
    
    /**
     * Group for update operations
     */
    interface Update {}
    
    /**
     * Group for delete operations
     */
    interface Delete {}
    
    /**
     * Group for basic validation
     */
    interface BasicCheck {}
    
    /**
     * Group for advanced validation
     */
    interface AdvancedCheck {}
    
    /**
     * Group for complete validation
     */
    interface CompleteCheck {}
    
    /**
     * Group for online orders
     */
    interface OnlineOrder {}
    
    /**
     * Group for in-store orders
     */
    interface InStoreOrder {}
    
    /**
     * Group for step 1 validation
     */
    interface Step1 {}
    
    /**
     * Group for step 2 validation
     */
    interface Step2 {}
    
    /**
     * Group for step 3 validation
     */
    interface Step3 {}
    
    /**
     * Group for user role
     */
    interface UserRole {}
    
    /**
     * Group for admin role
     */
    interface AdminRole {}
    
    /**
     * Group for manager role
     */
    interface ManagerRole {}
}

// ============================================================================
// Domain Models with Validation Groups
// ============================================================================

/**
 * User DTO with different validation for create/update
 */
class UserDto {
    
    @NotNull(groups = ValidationGroups.Update.class, message = "ID required for update")
    private Long id;
    
    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
              message = "Username is required")
    @Size(min = 3, max = 20, groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private String username;
    
    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Email(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private String email;
    
    @NotBlank(groups = ValidationGroups.Create.class, message = "Password required for creation")
    @Size(min = 8, groups = ValidationGroups.Create.class)
    private String password;
    
    @Null(groups = ValidationGroups.Create.class, message = "Version must be null for creation")
    @NotNull(groups = ValidationGroups.Update.class, message = "Version required for update")
    private Integer version;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}

/**
 * Product with validation sequence
 */
@GroupSequence({ValidationGroups.BasicCheck.class, ValidationGroups.AdvancedCheck.class, Product.class})
class Product {
    
    interface ValidationSequence {}
    
    @NotBlank(groups = ValidationGroups.BasicCheck.class, message = "Name is required")
    private String name;
    
    @NotBlank(groups = ValidationGroups.BasicCheck.class, message = "SKU is required")
    @Pattern(regexp = "^[A-Z]{3}-\\d{6}$", groups = ValidationGroups.AdvancedCheck.class,
             message = "SKU format must be XXX-NNNNNN")
    private String sku;
    
    @NotNull(groups = ValidationGroups.BasicCheck.class, message = "Price is required")
    @DecimalMin(value = "0.01", groups = ValidationGroups.AdvancedCheck.class,
                message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Min(value = 0, groups = ValidationGroups.AdvancedCheck.class, 
         message = "Stock cannot be negative")
    private Integer stock;
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}

/**
 * Order with conditional validation based on type
 */
class OrderDto {
    
    @NotBlank(groups = {ValidationGroups.OnlineOrder.class, ValidationGroups.InStoreOrder.class})
    private String orderType;
    
    @NotBlank(groups = ValidationGroups.OnlineOrder.class, 
              message = "Shipping address required for online orders")
    private String shippingAddress;
    
    @NotBlank(groups = ValidationGroups.OnlineOrder.class,
              message = "Payment method required for online orders")
    private String paymentMethod;
    
    @NotBlank(groups = ValidationGroups.InStoreOrder.class,
              message = "Store location required for in-store orders")
    private String storeLocation;
    
    @NotBlank(groups = ValidationGroups.InStoreOrder.class,
              message = "Pickup time required for in-store orders")
    private String pickupTime;
    
    // Getters and setters
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getStoreLocation() { return storeLocation; }
    public void setStoreLocation(String storeLocation) { this.storeLocation = storeLocation; }
    
    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }
}

/**
 * Registration form with multi-step validation
 */
class RegistrationForm {
    
    @NotBlank(groups = {ValidationGroups.Step1.class, Default.class})
    private String username;
    
    @NotBlank(groups = {ValidationGroups.Step1.class, Default.class})
    @Email(groups = {ValidationGroups.Step1.class, Default.class})
    private String email;
    
    @NotBlank(groups = {ValidationGroups.Step2.class, Default.class})
    private String phone;
    
    @NotBlank(groups = {ValidationGroups.Step2.class, Default.class})
    private String address;
    
    @NotNull(groups = {ValidationGroups.Step3.class, Default.class})
    @AssertTrue(groups = {ValidationGroups.Step3.class, Default.class}, 
                message = "Must accept terms and conditions")
    private Boolean termsAccepted;
    
    @NotBlank(groups = {ValidationGroups.Step3.class, Default.class})
    private String securityQuestion;
    
    @NotBlank(groups = {ValidationGroups.Step3.class, Default.class})
    private String securityAnswer;
    
    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Boolean getTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(Boolean termsAccepted) { this.termsAccepted = termsAccepted; }
    
    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    
    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
}

/**
 * Document with role-based validation
 */
class Document {
    
    @NotBlank(groups = {ValidationGroups.UserRole.class, ValidationGroups.AdminRole.class})
    private String title;
    
    @NotBlank(groups = {ValidationGroups.UserRole.class, ValidationGroups.AdminRole.class})
    private String content;
    
    @NotBlank(groups = ValidationGroups.AdminRole.class, 
              message = "Approval status required for admin review")
    private String approvalStatus;
    
    @NotNull(groups = ValidationGroups.AdminRole.class,
             message = "Approval date required for admin review")
    private LocalDate approvalDate;
    
    @NotBlank(groups = ValidationGroups.ManagerRole.class,
              message = "Department required for manager review")
    private String department;
    
    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    
    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}

// ============================================================================
// Validation Service
// ============================================================================

/**
 * Service for group-based validation
 */
@Service
class GroupValidationService {
    
    private final Validator validator;
    
    public GroupValidationService() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }
    
    /**
     * Validates object with specific groups
     */
    public <T> Set<ConstraintViolation<T>> validateWithGroups(T object, Class<?>... groups) {
        return validator.validate(object, groups);
    }
    
    /**
     * Validates for create operation
     */
    public <T> boolean isValidForCreate(T object) {
        Set<ConstraintViolation<T>> violations = 
            validator.validate(object, ValidationGroups.Create.class);
        return violations.isEmpty();
    }
    
    /**
     * Validates for update operation
     */
    public <T> boolean isValidForUpdate(T object) {
        Set<ConstraintViolation<T>> violations = 
            validator.validate(object, ValidationGroups.Update.class);
        return violations.isEmpty();
    }
    
    /**
     * Validates multi-step form
     */
    public <T> Map<String, Set<ConstraintViolation<T>>> validateSteps(T object) {
        Map<String, Set<ConstraintViolation<T>>> results = new LinkedHashMap<>();
        
        results.put("Step 1", validator.validate(object, ValidationGroups.Step1.class));
        results.put("Step 2", validator.validate(object, ValidationGroups.Step2.class));
        results.put("Step 3", validator.validate(object, ValidationGroups.Step3.class));
        
        return results;
    }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating validation groups in REST endpoints
 */
@RestController
@RequestMapping("/api/group-validation")
class GroupValidationController {
    
    private final GroupValidationService validationService;
    
    public GroupValidationController(GroupValidationService validationService) {
        this.validationService = validationService;
    }
    
    /**
     * Create user - validates with Create group
     */
    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody UserDto user) {
        Set<ConstraintViolation<UserDto>> violations = 
            validationService.validateWithGroups(user, ValidationGroups.Create.class);
        
        if (!violations.isEmpty()) {
            return ResponseEntity.badRequest().body("Validation failed: " + violations.size() + " errors");
        }
        
        return ResponseEntity.ok("User created successfully");
    }
    
    /**
     * Update user - validates with Update group
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserDto user) {
        user.setId(id);
        Set<ConstraintViolation<UserDto>> violations = 
            validationService.validateWithGroups(user, ValidationGroups.Update.class);
        
        if (!violations.isEmpty()) {
            return ResponseEntity.badRequest().body("Validation failed: " + violations.size() + " errors");
        }
        
        return ResponseEntity.ok("User updated successfully");
    }
    
    /**
     * Submit order - validates based on order type
     */
    @PostMapping("/orders")
    public ResponseEntity<String> submitOrder(@RequestBody OrderDto order) {
        Class<?> validationGroup = "ONLINE".equals(order.getOrderType()) 
            ? ValidationGroups.OnlineOrder.class 
            : ValidationGroups.InStoreOrder.class;
        
        Set<ConstraintViolation<OrderDto>> violations = 
            validationService.validateWithGroups(order, validationGroup);
        
        if (!violations.isEmpty()) {
            return ResponseEntity.badRequest().body("Validation failed");
        }
        
        return ResponseEntity.ok("Order submitted successfully");
    }
    
    /**
     * Register - validates multi-step form
     */
    @PostMapping("/register/validate")
    public ResponseEntity<?> validateRegistration(@RequestBody RegistrationForm form) {
        Map<String, Set<ConstraintViolation<RegistrationForm>>> results = 
            validationService.validateSteps(form);
        
        Map<String, String> response = new LinkedHashMap<>();
        results.forEach((step, violations) -> {
            response.put(step, violations.isEmpty() ? "Valid" : violations.size() + " errors");
        });
        
        return ResponseEntity.ok(response);
    }
}
