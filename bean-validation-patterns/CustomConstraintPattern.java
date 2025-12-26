package com.example.beanvalidation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom Constraint Pattern
 * 
 * Demonstrates creating custom validation constraints beyond JSR-303 standard annotations.
 * Custom constraints allow domain-specific validation logic:
 * - Custom annotation definition
 * - Constraint validator implementation
 * - Reusable business rules
 * - Complex validation logic
 * - Domain-specific validations
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class CustomConstraintPattern {

    public static void main(String[] args) {
        SpringApplication.run(CustomConstraintPattern.class, args);
    }

    // =========================================================================
    // CUSTOM CONSTRAINT ANNOTATIONS
    // =========================================================================

    /**
     * Custom annotation for phone number validation
     */
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Constraint(validatedBy = PhoneNumberValidator.class)
    public @interface ValidPhoneNumber {
        String message() default "Invalid phone number";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Custom annotation for credit card validation
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = CreditCardValidator.class)
    public @interface ValidCreditCard {
        String message() default "Invalid credit card number";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Custom annotation for password strength
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = PasswordStrengthValidator.class)
    public @interface StrongPassword {
        String message() default "Password must be strong";
        int minLength() default 8;
        boolean requireUpperCase() default true;
        boolean requireLowerCase() default true;
        boolean requireDigit() default true;
        boolean requireSpecialChar() default true;
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Custom annotation for age range based on date
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = AgeRangeValidator.class)
    public @interface AgeRange {
        String message() default "Age must be between {min} and {max}";
        int min() default 0;
        int max() default 150;
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    // =========================================================================
    // CONSTRAINT VALIDATORS
    // =========================================================================

    /**
     * Validator for phone numbers
     */
    public static class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
        
        @Override
        public void initialize(ValidPhoneNumber annotation) {
            // Initialization if needed
        }
        
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) {
                return true; // Use @NotNull for null checks
            }
            
            // Remove common formatting characters
            String cleaned = value.replaceAll("[\\s\\-\\(\\)\\+]", "");
            
            // Check if numeric and length
            return cleaned.matches("\\d{10,15}");
        }
    }

    /**
     * Validator for credit cards using Luhn algorithm
     */
    public static class CreditCardValidator implements ConstraintValidator<ValidCreditCard, String> {
        
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) {
                return true;
            }
            
            String cleaned = value.replaceAll("\\s", "");
            
            if (!cleaned.matches("\\d{13,19}")) {
                return false;
            }
            
            return luhnCheck(cleaned);
        }
        
        private boolean luhnCheck(String cardNumber) {
            int sum = 0;
            boolean alternate = false;
            
            for (int i = cardNumber.length() - 1; i >= 0; i--) {
                int n = Integer.parseInt(cardNumber.substring(i, i + 1));
                
                if (alternate) {
                    n *= 2;
                    if (n > 9) {
                        n = (n % 10) + 1;
                    }
                }
                
                sum += n;
                alternate = !alternate;
            }
            
            return (sum % 10 == 0);
        }
    }

    /**
     * Validator for password strength
     */
    public static class PasswordStrengthValidator implements ConstraintValidator<StrongPassword, String> {
        
        private int minLength;
        private boolean requireUpperCase;
        private boolean requireLowerCase;
        private boolean requireDigit;
        private boolean requireSpecialChar;
        
        @Override
        public void initialize(StrongPassword annotation) {
            this.minLength = annotation.minLength();
            this.requireUpperCase = annotation.requireUpperCase();
            this.requireLowerCase = annotation.requireLowerCase();
            this.requireDigit = annotation.requireDigit();
            this.requireSpecialChar = annotation.requireSpecialChar();
        }
        
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            
            if (value.length() < minLength) {
                return false;
            }
            
            if (requireUpperCase && !value.matches(".*[A-Z].*")) {
                return false;
            }
            
            if (requireLowerCase && !value.matches(".*[a-z].*")) {
                return false;
            }
            
            if (requireDigit && !value.matches(".*\\d.*")) {
                return false;
            }
            
            if (requireSpecialChar && !value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                return false;
            }
            
            return true;
        }
    }

    /**
     * Validator for age range based on birth date
     */
    public static class AgeRangeValidator implements ConstraintValidator<AgeRange, java.time.LocalDate> {
        
        private int min;
        private int max;
        
        @Override
        public void initialize(AgeRange annotation) {
            this.min = annotation.min();
            this.max = annotation.max();
        }
        
        @Override
        public boolean isValid(java.time.LocalDate birthDate, ConstraintValidatorContext context) {
            if (birthDate == null) {
                return true;
            }
            
            int age = java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
            
            return age >= min && age <= max;
        }
    }

    // =========================================================================
    // USAGE EXAMPLES
    // =========================================================================

    public static class UserRegistrationDTO {
        
        @ValidPhoneNumber(message = "Please provide a valid phone number")
        private String phoneNumber;
        
        @StrongPassword(
            minLength = 10,
            requireUpperCase = true,
            requireLowerCase = true,
            requireDigit = true,
            requireSpecialChar = true,
            message = "Password must be at least 10 characters with uppercase, lowercase, digit, and special character"
        )
        private String password;
        
        @AgeRange(min = 18, max = 100, message = "You must be between 18 and 100 years old")
        private java.time.LocalDate birthDate;
        
        // Getters and setters
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public java.time.LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(java.time.LocalDate birthDate) { this.birthDate = birthDate; }
    }

    public static class PaymentDTO {
        
        @ValidCreditCard(message = "Invalid credit card number")
        private String cardNumber;
        
        // Getters and setters
        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    }
}

/**
 * DOCUMENTATION
 * 
 * Custom Constraint Pattern:
 * 
 * 1. Creating Custom Constraints:
 *    - Create annotation with @Constraint
 *    - Specify validator class
 *    - Include message, groups, payload attributes
 *    - Add custom attributes for configuration
 * 
 * 2. Constraint Annotation Requirements:
 *    - @Target: Where annotation can be applied
 *    - @Retention(RUNTIME): Available at runtime
 *    - @Constraint(validatedBy = XValidator.class)
 *    - message(): Error message
 *    - groups(): Validation groups
 *    - payload(): Metadata
 * 
 * 3. Validator Implementation:
 *    - Implement ConstraintValidator<A, T>
 *    - A: Annotation type
 *    - T: Field type being validated
 *    - initialize(): Read annotation attributes
 *    - isValid(): Validation logic
 * 
 * 4. Common Custom Validations:
 *    - Phone numbers
 *    - Credit cards (Luhn algorithm)
 *    - Password strength
 *    - Age ranges
 *    - Business rules
 *    - Cross-field validation
 * 
 * 5. Best Practices:
 *    - Return true for null values (use @NotNull separately)
 *    - Provide meaningful default messages
 *    - Make validators reusable
 *    - Document validation rules
 *    - Test edge cases
 */
