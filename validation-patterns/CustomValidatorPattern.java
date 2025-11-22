package com.example.validation.customvalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.lang.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Custom Validator Pattern - Demonstrates Creating Custom Validation Constraints
 * 
 * This pattern shows how to:
 * 1. Create custom constraint annotations
 * 2. Implement ConstraintValidator interface
 * 3. Define validation logic
 * 4. Add custom messages
 * 5. Access annotation parameters
 * 6. Validate complex business rules
 * 7. Compose multiple constraints
 * 8. Validate multiple fields
 * 9. Cross-field validation
 * 10. Conditional validation
 * 
 * Key Concepts:
 * - @Constraint: Marks an annotation as a validation constraint
 * - ConstraintValidator: Interface for validation logic
 * - initialize(): Called once to set up validator
 * - isValid(): Performs the actual validation
 * - ConstraintValidatorContext: Access to constraint context
 * 
 * Custom Validator Steps:
 * 1. Create constraint annotation with @Constraint
 * 2. Implement ConstraintValidator interface
 * 3. Override initialize() and isValid() methods
 * 4. Register validator with constraint annotation
 * 5. Apply annotation to fields/classes
 * 
 * Advanced Features:
 * - Annotation parameters for configuration
 * - Message interpolation with placeholders
 * - Custom constraint composition
 * - Class-level validation
 * - Cross-field validation
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
public class CustomValidatorPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(CustomValidatorPattern.class, args);
        demonstrateCustomValidators();
    }
    
    /**
     * Demonstrates various custom validator scenarios
     */
    private static void demonstrateCustomValidators() {
        System.out.println("=== Custom Validator Pattern Demonstrations ===\n");
        
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        
        // Demo 1: Simple custom validators
        demonstrateSimpleCustomValidators(validator);
        
        // Demo 2: Parameterized validators
        demonstrateParameterizedValidators(validator);
        
        // Demo 3: Cross-field validation
        demonstrateCrossFieldValidation(validator);
        
        // Demo 4: Composite constraints
        demonstrateCompositeConstraints(validator);
        
        // Demo 5: Conditional validation
        demonstrateConditionalValidation(validator);
    }
    
    /**
     * Demonstrates simple custom validators
     */
    private static void demonstrateSimpleCustomValidators(Validator validator) {
        System.out.println("1. Simple Custom Validators:");
        
        UserRegistration registration = new UserRegistration();
        registration.setUsername("john@doe"); // Contains invalid character
        registration.setPassword("weak"); // Too weak
        registration.setCreditCard("1234-5678-9012-3456"); // Invalid card
        
        Set<ConstraintViolation<UserRegistration>> violations = validator.validate(registration);
        printViolations(violations);
    }
    
    /**
     * Demonstrates parameterized validators
     */
    private static void demonstrateParameterizedValidators(Validator validator) {
        System.out.println("2. Parameterized Validators:");
        
        ProductListing product = new ProductListing();
        product.setSku("ABC-123"); // Too short
        product.setPrice(5000.0); // Too low
        product.setDiscount(150); // Exceeds max
        
        Set<ConstraintViolation<ProductListing>> violations = validator.validate(product);
        printViolations(violations);
    }
    
    /**
     * Demonstrates cross-field validation
     */
    private static void demonstrateCrossFieldValidation(Validator validator) {
        System.out.println("3. Cross-Field Validation:");
        
        DateRange dateRange = new DateRange();
        dateRange.setStartDate(LocalDate.now().plusDays(10));
        dateRange.setEndDate(LocalDate.now()); // End before start
        
        Set<ConstraintViolation<DateRange>> violations = validator.validate(dateRange);
        printViolations(violations);
    }
    
    /**
     * Demonstrates composite constraints
     */
    private static void demonstrateCompositeConstraints(Validator validator) {
        System.out.println("4. Composite Constraints:");
        
        SecurePassword password = new SecurePassword();
        password.setPassword("simple"); // Doesn't meet composite requirements
        
        Set<ConstraintViolation<SecurePassword>> violations = validator.validate(password);
        printViolations(violations);
    }
    
    /**
     * Demonstrates conditional validation
     */
    private static void demonstrateConditionalValidation(Validator validator) {
        System.out.println("5. Conditional Validation:");
        
        PaymentDetails payment = new PaymentDetails();
        payment.setPaymentMethod("CREDIT_CARD");
        // Missing credit card details
        
        Set<ConstraintViolation<PaymentDetails>> violations = validator.validate(payment);
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
// Custom Constraint Annotations
// ============================================================================

/**
 * Custom constraint for validating username format
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameValidator.class)
@Documented
@interface ValidUsername {
    String message() default "Username must contain only letters, numbers, and underscores";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * Validator for ValidUsername constraint
 */
class UsernameValidator implements ConstraintValidator<ValidUsername, String> {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }
        return USERNAME_PATTERN.matcher(value).matches();
    }
}

/**
 * Custom constraint for password strength
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordStrengthValidator.class)
@Documented
@interface PasswordStrength {
    String message() default "Password does not meet strength requirements";
    PasswordLevel level() default PasswordLevel.MEDIUM;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * Password strength levels
 */
enum PasswordLevel {
    WEAK, MEDIUM, STRONG
}

/**
 * Validator for password strength
 */
class PasswordStrengthValidator implements ConstraintValidator<PasswordStrength, String> {
    
    private PasswordLevel level;
    
    @Override
    public void initialize(PasswordStrength constraint) {
        this.level = constraint.level();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        switch (level) {
            case WEAK:
                return value.length() >= 6;
            case MEDIUM:
                return value.length() >= 8 && 
                       value.matches(".*[a-z].*") &&
                       value.matches(".*[A-Z].*");
            case STRONG:
                return value.length() >= 12 &&
                       value.matches(".*[a-z].*") &&
                       value.matches(".*[A-Z].*") &&
                       value.matches(".*[0-9].*") &&
                       value.matches(".*[!@#$%^&*].*");
            default:
                return false;
        }
    }
}

/**
 * Custom constraint for credit card validation
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreditCardValidator.class)
@Documented
@interface ValidCreditCard {
    String message() default "Invalid credit card number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * Credit card validator using Luhn algorithm
 */
class CreditCardValidator implements ConstraintValidator<ValidCreditCard, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        // Remove all non-digits
        String digits = value.replaceAll("\\D", "");
        
        if (digits.length() < 13 || digits.length() > 19) {
            return false;
        }
        
        return passesLuhnCheck(digits);
    }
    
    private boolean passesLuhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(cardNumber.substring(i, i + 1));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
}

/**
 * Custom constraint for SKU format with parameters
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SkuValidator.class)
@Documented
@interface ValidSku {
    String message() default "Invalid SKU format";
    int minLength() default 8;
    int maxLength() default 20;
    String prefix() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * SKU validator with parameters
 */
class SkuValidator implements ConstraintValidator<ValidSku, String> {
    
    private int minLength;
    private int maxLength;
    private String prefix;
    
    @Override
    public void initialize(ValidSku constraint) {
        this.minLength = constraint.minLength();
        this.maxLength = constraint.maxLength();
        this.prefix = constraint.prefix();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        if (value.length() < minLength || value.length() > maxLength) {
            return false;
        }
        
        if (!prefix.isEmpty() && !value.startsWith(prefix)) {
            return false;
        }
        
        return value.matches("^[A-Z0-9-]+$");
    }
}

/**
 * Custom constraint for price range
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PriceRangeValidator.class)
@Documented
@interface PriceRange {
    String message() default "Price must be within range {min} to {max}";
    double min() default 0.0;
    double max() default Double.MAX_VALUE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * Price range validator
 */
class PriceRangeValidator implements ConstraintValidator<PriceRange, Double> {
    
    private double min;
    private double max;
    
    @Override
    public void initialize(PriceRange constraint) {
        this.min = constraint.min();
        this.max = constraint.max();
    }
    
    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value >= min && value <= max;
    }
}

/**
 * Custom constraint for percentage range
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PercentageValidator.class)
@Documented
@interface Percentage {
    String message() default "Value must be between 0 and 100";
    int min() default 0;
    int max() default 100;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * Percentage validator
 */
class PercentageValidator implements ConstraintValidator<Percentage, Integer> {
    
    private int min;
    private int max;
    
    @Override
    public void initialize(Percentage constraint) {
        this.min = constraint.min();
        this.max = constraint.max();
    }
    
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value >= min && value <= max;
    }
}

// ============================================================================
// Cross-Field Validation
// ============================================================================

/**
 * Custom constraint for validating date range
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
@Documented
@interface ValidDateRange {
    String message() default "End date must be after start date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * Date range validator (class-level)
 */
class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, DateRange> {
    
    @Override
    public boolean isValid(DateRange value, ConstraintValidatorContext context) {
        if (value == null || value.getStartDate() == null || value.getEndDate() == null) {
            return true;
        }
        return value.getEndDate().isAfter(value.getStartDate());
    }
}

/**
 * Custom constraint for password confirmation
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Documented
@interface PasswordMatches {
    String message() default "Passwords do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * Password matching validator
 */
class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj instanceof SecurePassword) {
            SecurePassword sp = (SecurePassword) obj;
            if (sp.getPassword() == null || sp.getConfirmPassword() == null) {
                return true;
            }
            return sp.getPassword().equals(sp.getConfirmPassword());
        }
        return true;
    }
}

// ============================================================================
// Composite Constraints
// ============================================================================

/**
 * Composite constraint combining multiple validations
 */
@NotBlank
@Size(min = 8, max = 50)
@Pattern(regexp = ".*[a-z].*", message = "Must contain lowercase letter")
@Pattern(regexp = ".*[A-Z].*", message = "Must contain uppercase letter")
@Pattern(regexp = ".*[0-9].*", message = "Must contain digit")
@Pattern(regexp = ".*[!@#$%^&*].*", message = "Must contain special character")
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
@interface StrongPassword {
    String message() default "Password must meet all security requirements";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// ============================================================================
// Conditional Validation
// ============================================================================

/**
 * Custom constraint for conditional validation
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalValidator.class)
@Documented
@interface ConditionalValidation {
    String message() default "Conditional validation failed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * Conditional validator
 */
class ConditionalValidator implements ConstraintValidator<ConditionalValidation, PaymentDetails> {
    
    @Override
    public boolean isValid(PaymentDetails value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        if ("CREDIT_CARD".equals(value.getPaymentMethod())) {
            if (value.getCardNumber() == null || value.getCardNumber().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Card number is required for credit card payments"
                ).addPropertyNode("cardNumber").addConstraintViolation();
                return false;
            }
        } else if ("PAYPAL".equals(value.getPaymentMethod())) {
            if (value.getPaypalEmail() == null || value.getPaypalEmail().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "PayPal email is required for PayPal payments"
                ).addPropertyNode("paypalEmail").addConstraintViolation();
                return false;
            }
        }
        
        return true;
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * User registration with custom validators
 */
class UserRegistration {
    
    @ValidUsername
    @NotBlank
    private String username;
    
    @PasswordStrength(level = PasswordLevel.STRONG)
    private String password;
    
    @ValidCreditCard
    private String creditCard;
    
    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getCreditCard() { return creditCard; }
    public void setCreditCard(String creditCard) { this.creditCard = creditCard; }
}

/**
 * Product listing with parameterized validators
 */
class ProductListing {
    
    @ValidSku(minLength = 10, maxLength = 15, prefix = "PROD-")
    private String sku;
    
    @PriceRange(min = 10.0, max = 1000.0)
    private Double price;
    
    @Percentage(min = 0, max = 100)
    private Integer discount;
    
    // Getters and setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Integer getDiscount() { return discount; }
    public void setDiscount(Integer discount) { this.discount = discount; }
}

/**
 * Date range with cross-field validation
 */
@ValidDateRange
class DateRange {
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    // Getters and setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}

/**
 * Secure password with composite constraints
 */
@PasswordMatches
class SecurePassword {
    
    @StrongPassword
    private String password;
    
    @NotBlank
    private String confirmPassword;
    
    // Getters and setters
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}

/**
 * Payment details with conditional validation
 */
@ConditionalValidation
class PaymentDetails {
    
    @NotBlank
    private String paymentMethod; // CREDIT_CARD, PAYPAL, BANK_TRANSFER
    
    private String cardNumber;
    private String paypalEmail;
    private String bankAccount;
    
    // Getters and setters
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    
    public String getPaypalEmail() { return paypalEmail; }
    public void setPaypalEmail(String paypalEmail) { this.paypalEmail = paypalEmail; }
    
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating custom validation
 */
@RestController
@RequestMapping("/api/custom-validation")
class CustomValidationController {
    
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistration registration) {
        return ResponseEntity.ok("Registration successful");
    }
    
    @PostMapping("/products")
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductListing product) {
        return ResponseEntity.ok("Product created");
    }
    
    @PostMapping("/payment")
    public ResponseEntity<String> processPayment(@Valid @RequestBody PaymentDetails payment) {
        return ResponseEntity.ok("Payment processed");
    }
}
