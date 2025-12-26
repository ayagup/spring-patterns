package com.example.beanvalidation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;

/**
 * Validation Group and Group Sequence Pattern
 * 
 * Demonstrates using validation groups to conditionally apply constraints
 * and group sequences to enforce validation order.
 * 
 * Combined into one comprehensive pattern covering:
 * - Validation Groups: Conditional validation based on operation
 * - Group Sequences: Ordered validation execution
 * - Payload: Metadata for validation severity/category
 * - Cross-field Validation: Validating relationships between fields
 * - Class-level Validation: Validating entire object state
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class AdvancedValidationPattern {

    public static void main(String[] args) {
        SpringApplication.run(AdvancedValidationPattern.class, args);
    }

    // =========================================================================
    // VALIDATION GROUPS
    // =========================================================================

    /**
     * Validation group for create operations
     */
    public interface CreateValidation {}

    /**
     * Validation group for update operations
     */
    public interface UpdateValidation {}

    /**
     * Validation group for delete operations
     */
    public interface DeleteValidation {}

    /**
     * Validation group for basic checks
     */
    public interface BasicChecks {}

    /**
     * Validation group for advanced checks
     */
    public interface AdvancedChecks {}

    /**
     * User DTO with group-specific validations
     */
    public static class UserDTO {
        
        // ID required only for update/delete
        @Null(groups = CreateValidation.class, message = "ID must be null for create")
        @NotNull(groups = {UpdateValidation.class, DeleteValidation.class}, message = "ID required for update/delete")
        private Long id;
        
        // Username validated for create and update
        @NotBlank(groups = {CreateValidation.class, UpdateValidation.class}, message = "Username required")
        @Size(min = 3, max = 20, groups = {CreateValidation.class, UpdateValidation.class})
        private String username;
        
        // Email required for create, optional for update
        @NotNull(groups = CreateValidation.class, message = "Email required for registration")
        @Email(groups = {CreateValidation.class, UpdateValidation.class})
        private String email;
        
        // Password required only for create
        @NotBlank(groups = CreateValidation.class, message = "Password required for registration")
        @Size(min = 8, groups = CreateValidation.class)
        private String password;
        
        // Age with basic and advanced checks
        @NotNull(groups = BasicChecks.class)
        @Min(value = 0, groups = BasicChecks.class)
        @Max(value = 150, groups = AdvancedChecks.class)
        private Integer age;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    // =========================================================================
    // GROUP SEQUENCE
    // =========================================================================

    /**
     * Ordered group sequence
     * Validates in order: BasicChecks → AdvancedChecks
     * Stops at first failure
     */
    @GroupSequence({BasicChecks.class, AdvancedChecks.class})
    public interface OrderedValidation {}

    /**
     * Complete validation sequence
     */
    @GroupSequence({Default.class, BasicChecks.class, AdvancedChecks.class})
    public interface CompleteValidation {}

    /**
     * Product with ordered validation
     */
    public static class ProductDTO {
        
        // Basic checks first
        @NotBlank(groups = BasicChecks.class, message = "Name is required")
        private String name;
        
        @NotNull(groups = BasicChecks.class, message = "Price is required")
        @DecimalMin(value = "0.01", groups = BasicChecks.class)
        private java.math.BigDecimal price;
        
        // Advanced checks after basic
        @Size(max = 500, groups = AdvancedChecks.class, message = "Description too long")
        private String description;
        
        @Max(value = 10000, groups = AdvancedChecks.class, message = "Stock quantity too high")
        private Integer stock;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
    }

    // =========================================================================
    // PAYLOAD (Severity/Metadata)
    // =========================================================================

    /**
     * Severity levels using Payload
     */
    public static class Severity {
        public interface Info extends javax.validation.Payload {}
        public interface Warning extends javax.validation.Payload {}
        public interface Error extends javax.validation.Payload {}
        public interface Critical extends javax.validation.Payload {}
    }

    /**
     * Entity with severity metadata
     */
    public static class ConfigDTO {
        
        @NotBlank(payload = Severity.Critical.class, message = "Config key is critical")
        private String key;
        
        @NotBlank(payload = Severity.Error.class, message = "Value is required")
        private String value;
        
        @Min(value = 0, payload = Severity.Warning.class, message = "Timeout should be positive")
        private Integer timeout;
        
        @Pattern(regexp = "[A-Z]{2}", payload = Severity.Info.class, message = "Country code format")
        private String countryCode;

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public Integer getTimeout() { return timeout; }
        public void setTimeout(Integer timeout) { this.timeout = timeout; }
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    }

    // =========================================================================
    // CROSS-FIELD VALIDATION
    // =========================================================================

    /**
     * Custom constraint for cross-field validation
     */
    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @javax.validation.Constraint(validatedBy = PasswordMatchValidator.class)
    public @interface PasswordMatch {
        String message() default "Passwords do not match";
        Class<?>[] groups() default {};
        Class<? extends javax.validation.Payload>[] payload() default {};
    }

    /**
     * Validator for password matching
     */
    public static class PasswordMatchValidator 
            implements javax.validation.ConstraintValidator<PasswordMatch, RegistrationDTO> {
        
        @Override
        public boolean isValid(RegistrationDTO dto, javax.validation.ConstraintValidatorContext context) {
            if (dto == null) {
                return true;
            }
            
            String password = dto.getPassword();
            String confirmPassword = dto.getConfirmPassword();
            
            if (password == null || confirmPassword == null) {
                return password == confirmPassword;
            }
            
            return password.equals(confirmPassword);
        }
    }

    /**
     * Cross-field validation: Date range
     */
    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @javax.validation.Constraint(validatedBy = DateRangeValidator.class)
    public @interface ValidDateRange {
        String message() default "End date must be after start date";
        Class<?>[] groups() default {};
        Class<? extends javax.validation.Payload>[] payload() default {};
    }

    public static class DateRangeValidator 
            implements javax.validation.ConstraintValidator<ValidDateRange, EventDTO> {
        
        @Override
        public boolean isValid(EventDTO dto, javax.validation.ConstraintValidatorContext context) {
            if (dto == null || dto.getStartDate() == null || dto.getEndDate() == null) {
                return true;
            }
            
            return dto.getEndDate().isAfter(dto.getStartDate());
        }
    }

    // =========================================================================
    // CLASS-LEVEL VALIDATION
    // =========================================================================

    /**
     * Registration DTO with class-level password matching
     */
    @PasswordMatch
    public static class RegistrationDTO {
        
        @NotBlank
        @Size(min = 3, max = 20)
        private String username;
        
        @NotBlank
        @Email
        private String email;
        
        @NotBlank
        @Size(min = 8)
        private String password;
        
        @NotBlank
        private String confirmPassword;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }

    /**
     * Event DTO with class-level date range validation
     */
    @ValidDateRange
    public static class EventDTO {
        
        @NotBlank
        private String name;
        
        @NotNull
        private java.time.LocalDateTime startDate;
        
        @NotNull
        private java.time.LocalDateTime endDate;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public java.time.LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(java.time.LocalDateTime startDate) { this.startDate = startDate; }
        public java.time.LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(java.time.LocalDateTime endDate) { this.endDate = endDate; }
    }

    /**
     * Consistency validation
     */
    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @javax.validation.Constraint(validatedBy = PriceConsistencyValidator.class)
    public @interface PriceConsistency {
        String message() default "Discount price must be less than regular price";
        Class<?>[] groups() default {};
        Class<? extends javax.validation.Payload>[] payload() default {};
    }

    public static class PriceConsistencyValidator 
            implements javax.validation.ConstraintValidator<PriceConsistency, PricingDTO> {
        
        @Override
        public boolean isValid(PricingDTO dto, javax.validation.ConstraintValidatorContext context) {
            if (dto == null || dto.getRegularPrice() == null || dto.getDiscountPrice() == null) {
                return true;
            }
            
            return dto.getDiscountPrice().compareTo(dto.getRegularPrice()) < 0;
        }
    }

    @PriceConsistency
    public static class PricingDTO {
        
        @NotNull
        @DecimalMin("0.01")
        private java.math.BigDecimal regularPrice;
        
        @NotNull
        @DecimalMin("0.01")
        private java.math.BigDecimal discountPrice;

        public java.math.BigDecimal getRegularPrice() { return regularPrice; }
        public void setRegularPrice(java.math.BigDecimal regularPrice) { this.regularPrice = regularPrice; }
        public java.math.BigDecimal getDiscountPrice() { return discountPrice; }
        public void setDiscountPrice(java.math.BigDecimal discountPrice) { this.discountPrice = discountPrice; }
    }
}

/**
 * DOCUMENTATION
 * 
 * This pattern combines:
 * 
 * 1. Validation Groups:
 *    - Apply constraints conditionally
 *    - Define operation-specific rules
 *    - groups attribute on constraints
 *    - Validate with: validator.validate(obj, CreateValidation.class)
 * 
 * 2. Group Sequences:
 *    - @GroupSequence: Ordered validation
 *    - Stops at first failing group
 *    - Define validation priority
 *    - Prevents expensive checks if basic ones fail
 * 
 * 3. Payload:
 *    - Metadata for constraints
 *    - Severity levels (Info, Warning, Error, Critical)
 *    - payload attribute on constraints
 *    - Access via ConstraintViolation.getConstraintDescriptor().getPayload()
 * 
 * 4. Cross-field Validation:
 *    - Class-level constraints
 *    - Validate relationships between fields
 *    - Examples: password match, date ranges, price consistency
 *    - @Target(TYPE) on annotation
 * 
 * 5. Class-level Validation:
 *    - Entire object state validation
 *    - Business rules enforcement
 *    - Multiple field interdependencies
 *    - Applied at TYPE level
 */
