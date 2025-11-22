I'll create a comprehensive Spring Boot project demonstrating all validation patterns.

## Project Structure

```xml pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.validationpatterns</groupId>
    <artifactId>validation-patterns</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Bean Validation API -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Spring AOP for Method Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Apache Commons Validator -->
        <dependency>
            <groupId>commons-validator</groupId>
            <artifactId>commons-validator</artifactId>
            <version>1.8.0</version>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Main Application

```java src/main/java/com/validationpatterns/ValidationPatternsApplication.java
package com.validationpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication
@Validated // Enable method-level validation
@EnableAspectJAutoProxy
public class ValidationPatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ValidationPatternsApplication.class, args);
    }
}
```

## 1. Validator Pattern

```java src/main/java/com/validationpatterns/validator/UserValidator.java
package com.validationpatterns.validator;

import com.validationpatterns.model.User;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator Pattern - Traditional Spring Validator interface
 * Implements custom validation logic for User objects
 */
@Component
public class UserValidator implements Validator {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 120;

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

        // Validate username
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", 
            "field.required", "Username is required");
        
        if (user.getUsername() != null) {
            if (user.getUsername().length() < 3) {
                errors.rejectValue("username", "field.min.length", 
                    "Username must be at least 3 characters");
            }
            if (user.getUsername().length() > 50) {
                errors.rejectValue("username", "field.max.length", 
                    "Username must not exceed 50 characters");
            }
            if (!user.getUsername().matches("^[a-zA-Z0-9_]+$")) {
                errors.rejectValue("username", "field.invalid.pattern", 
                    "Username can only contain letters, numbers, and underscores");
            }
        }

        // Validate email
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", 
            "field.required", "Email is required");
        
        if (user.getEmail() != null && !isValidEmail(user.getEmail())) {
            errors.rejectValue("email", "field.invalid.format", 
                "Email format is invalid");
        }

        // Validate password
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", 
            "field.required", "Password is required");
        
        if (user.getPassword() != null) {
            if (user.getPassword().length() < MIN_PASSWORD_LENGTH) {
                errors.rejectValue("password", "field.min.length", 
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            }
            if (!hasUpperCase(user.getPassword())) {
                errors.rejectValue("password", "field.missing.uppercase", 
                    "Password must contain at least one uppercase letter");
            }
            if (!hasLowerCase(user.getPassword())) {
                errors.rejectValue("password", "field.missing.lowercase", 
                    "Password must contain at least one lowercase letter");
            }
            if (!hasDigit(user.getPassword())) {
                errors.rejectValue("password", "field.missing.digit", 
                    "Password must contain at least one digit");
            }
            if (!hasSpecialChar(user.getPassword())) {
                errors.rejectValue("password", "field.missing.special", 
                    "Password must contain at least one special character");
            }
        }

        // Validate age
        if (user.getAge() != null) {
            if (user.getAge() < MIN_AGE) {
                errors.rejectValue("age", "field.min.value", 
                    "Age must be at least " + MIN_AGE);
            }
            if (user.getAge() > MAX_AGE) {
                errors.rejectValue("age", "field.max.value", 
                    "Age must not exceed " + MAX_AGE);
            }
        }

        // Validate phone number
        if (user.getPhoneNumber() != null && !isValidPhoneNumber(user.getPhoneNumber())) {
            errors.rejectValue("phoneNumber", "field.invalid.format", 
                "Phone number format is invalid");
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }

    private boolean hasUpperCase(String str) {
        return str.matches(".*[A-Z].*");
    }

    private boolean hasLowerCase(String str) {
        return str.matches(".*[a-z].*");
    }

    private boolean hasDigit(String str) {
        return str.matches(".*\\d.*");
    }

    private boolean hasSpecialChar(String str) {
        return str.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }
}
```

```java src/main/java/com/validationpatterns/validator/OrderValidator.java
package com.validationpatterns.validator;

import com.validationpatterns.model.Order;
import com.validationpatterns.model.OrderItem;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Validator Pattern for Order entity
 */
@Component
public class OrderValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Order.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Order order = (Order) target;

        // Validate customer ID
        if (order.getCustomerId() == null || order.getCustomerId().isEmpty()) {
            errors.rejectValue("customerId", "field.required", 
                "Customer ID is required");
        }

        // Validate order items
        if (order.getItems() == null || order.getItems().isEmpty()) {
            errors.rejectValue("items", "field.required", 
                "Order must contain at least one item");
        } else {
            validateOrderItems(order, errors);
        }

        // Validate total amount
        if (order.getTotalAmount() != null) {
            if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                errors.rejectValue("totalAmount", "field.min.value", 
                    "Total amount must be greater than zero");
            }
            
            // Verify calculated total matches
            BigDecimal calculatedTotal = calculateTotal(order);
            if (calculatedTotal.compareTo(order.getTotalAmount()) != 0) {
                errors.rejectValue("totalAmount", "field.mismatch", 
                    "Total amount does not match sum of items");
            }
        }

        // Validate delivery date
        if (order.getDeliveryDate() != null) {
            if (order.getDeliveryDate().isBefore(LocalDateTime.now())) {
                errors.rejectValue("deliveryDate", "field.past.date", 
                    "Delivery date cannot be in the past");
            }
        }

        // Validate shipping address
        if (order.getShippingAddress() == null || order.getShippingAddress().isEmpty()) {
            errors.rejectValue("shippingAddress", "field.required", 
                "Shipping address is required");
        }
    }

    private void validateOrderItems(Order order, Errors errors) {
        for (int i = 0; i < order.getItems().size(); i++) {
            OrderItem item = order.getItems().get(i);
            String fieldPrefix = "items[" + i + "].";

            if (item.getProductId() == null || item.getProductId().isEmpty()) {
                errors.rejectValue(fieldPrefix + "productId", "field.required", 
                    "Product ID is required for item " + (i + 1));
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                errors.rejectValue(fieldPrefix + "quantity", "field.min.value", 
                    "Quantity must be greater than zero for item " + (i + 1));
            }

            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                errors.rejectValue(fieldPrefix + "price", "field.min.value", 
                    "Price must be greater than zero for item " + (i + 1));
            }
        }
    }

    private BigDecimal calculateTotal(Order order) {
        return order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

## 2. Constraint Violation Pattern

```java src/main/java/com/validationpatterns/exception/ConstraintViolationException.java
package com.validationpatterns.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Constraint Violation Pattern
 * Custom exception to collect and report validation violations
 */
@Getter
public class ConstraintViolationException extends RuntimeException {

    private final List<ConstraintViolation> violations;

    public ConstraintViolationException(String message) {
        super(message);
        this.violations = new ArrayList<>();
    }

    public ConstraintViolationException(String message, List<ConstraintViolation> violations) {
        super(message);
        this.violations = violations;
    }

    public void addViolation(String field, String message) {
        violations.add(new ConstraintViolation(field, message, null));
    }

    public void addViolation(String field, String message, Object rejectedValue) {
        violations.add(new ConstraintViolation(field, message, rejectedValue));
    }

    public boolean hasViolations() {
        return !violations.isEmpty();
    }

    @Getter
    public static class ConstraintViolation {
        private final String field;
        private final String message;
        private final Object rejectedValue;

        public ConstraintViolation(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
    }
}
```

```java src/main/java/com/validationpatterns/validator/ProductValidator.java
package com.validationpatterns.validator;

import com.validationpatterns.exception.ConstraintViolationException;
import com.validationpatterns.model.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Constraint Violation Pattern - Validator that throws custom exceptions
 */
@Component
public class ProductValidator {

    public void validate(Product product) {
        ConstraintViolationException exception = 
            new ConstraintViolationException("Product validation failed");

        // Validate product name
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            exception.addViolation("name", "Product name is required");
        } else if (product.getName().length() < 3) {
            exception.addViolation("name", "Product name must be at least 3 characters", 
                product.getName());
        } else if (product.getName().length() > 100) {
            exception.addViolation("name", "Product name must not exceed 100 characters", 
                product.getName());
        }

        // Validate SKU
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            exception.addViolation("sku", "SKU is required");
        } else if (!product.getSku().matches("^[A-Z0-9-]+$")) {
            exception.addViolation("sku", "SKU must contain only uppercase letters, numbers, and hyphens", 
                product.getSku());
        }

        // Validate price
        if (product.getPrice() == null) {
            exception.addViolation("price", "Price is required");
        } else if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            exception.addViolation("price", "Price must be greater than zero", 
                product.getPrice());
        } else if (product.getPrice().compareTo(new BigDecimal("1000000")) > 0) {
            exception.addViolation("price", "Price cannot exceed 1,000,000", 
                product.getPrice());
        }

        // Validate stock quantity
        if (product.getStockQuantity() == null) {
            exception.addViolation("stockQuantity", "Stock quantity is required");
        } else if (product.getStockQuantity() < 0) {
            exception.addViolation("stockQuantity", "Stock quantity cannot be negative", 
                product.getStockQuantity());
        }

        // Validate category
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            exception.addViolation("category", "Category is required");
        }

        // Validate weight
        if (product.getWeight() != null && product.getWeight() <= 0) {
            exception.addViolation("weight", "Weight must be greater than zero", 
                product.getWeight());
        }

        // Validate dimensions
        if (product.getLength() != null && product.getLength() <= 0) {
            exception.addViolation("length", "Length must be greater than zero", 
                product.getLength());
        }
        if (product.getWidth() != null && product.getWidth() <= 0) {
            exception.addViolation("width", "Width must be greater than zero", 
                product.getWidth());
        }
        if (product.getHeight() != null && product.getHeight() <= 0) {
            exception.addViolation("height", "Height must be greater than zero", 
                product.getHeight());
        }

        // Throw exception if there are violations
        if (exception.hasViolations()) {
            throw exception;
        }
    }

    public void validateForUpdate(Product product) {
        validate(product);

        ConstraintViolationException exception = 
            new ConstraintViolationException("Product update validation failed");

        // Additional validation for updates
        if (product.getId() == null) {
            exception.addViolation("id", "Product ID is required for updates");
        }

        if (exception.hasViolations()) {
            throw exception;
        }
    }
}
```

## 3. Bean Validation Pattern

```java src/main/java/com/validationpatterns/model/User.java
package com.validationpatterns.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Bean Validation Pattern - Using JSR-303/380 annotations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 120, message = "Age must not exceed 120")
    private Integer age;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    private String phoneNumber;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @AssertTrue(message = "Terms and conditions must be accepted")
    private Boolean termsAccepted;
}
```

```java src/main/java/com/validationpatterns/model/Product.java
package com.validationpatterns.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Bean Validation Pattern for Product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    private String sku;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @DecimalMax(value = "1000000.00", message = "Price cannot exceed 1,000,000")
    @Digits(integer = 7, fraction = 2, message = "Price must have at most 7 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotBlank(message = "Category is required")
    private String category;

    @Positive(message = "Weight must be positive")
    private Double weight;

    @Positive(message = "Length must be positive")
    private Double length;

    @Positive(message = "Width must be positive")
    private Double width;

    @Positive(message = "Height must be positive")
    private Double height;

    @AssertTrue(message = "Product must be either in stock or marked as out of stock")
    public boolean isStockValid() {
        return stockQuantity == null || stockQuantity >= 0;
    }
}
```

```java src/main/java/com/validationpatterns/model/Order.java
package com.validationpatterns.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bean Validation Pattern with nested validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid // Cascade validation to nested objects
    private List<OrderItem> items;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;

    @NotNull(message = "Order date is required")
    @PastOrPresent(message = "Order date cannot be in the future")
    private LocalDateTime orderDate;

    @Future(message = "Delivery date must be in the future")
    private LocalDateTime deliveryDate;

    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 500, message = "Shipping address must be between 10 and 500 characters")
    private String shippingAddress;

    @Pattern(regexp = "^[A-Z0-9]{5,20}$", message = "Postal code must be 5-20 uppercase alphanumeric characters")
    private String postalCode;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
```

```java src/main/java/com/validationpatterns/model/OrderItem.java
package com.validationpatterns.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000, message = "Quantity cannot exceed 1000")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    @DecimalMax(value = "100.00", message = "Discount cannot exceed 100%")
    private BigDecimal discountPercentage;
}
```

## 4. Custom Validator Pattern

```java src/main/java/com/validationpatterns/validation/ValidPassword.java
package com.validationpatterns.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom Validator Pattern - Custom annotation for password validation
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface ValidPassword {

    String message() default "Password must meet security requirements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minLength() default 8;

    int maxLength() default 128;

    boolean requireUppercase() default true;

    boolean requireLowercase() default true;

    boolean requireDigit() default true;

    boolean requireSpecialChar() default true;
}
```

```java src/main/java/com/validationpatterns/validation/PasswordValidator.java
package com.validationpatterns.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Custom Validator Pattern - Validator implementation
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        context.disableDefaultConstraintViolation();

        boolean valid = true;

        // Check length
        if (password.length() < minLength) {
            context.buildConstraintViolationWithTemplate(
                "Password must be at least " + minLength + " characters long"
            ).addConstraintViolation();
            valid = false;
        }

        if (password.length() > maxLength) {
            context.buildConstraintViolationWithTemplate(
                "Password must not exceed " + maxLength + " characters"
            ).addConstraintViolation();
            valid = false;
        }

        // Check uppercase
        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one uppercase letter"
            ).addConstraintViolation();
            valid = false;
        }

        // Check lowercase
        if (requireLowercase && !password.matches(".*[a-z].*")) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one lowercase letter"
            ).addConstraintViolation();
            valid = false;
        }

        // Check digit
        if (requireDigit && !password.matches(".*\\d.*")) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one digit"
            ).addConstraintViolation();
            valid = false;
        }

        // Check special character
        if (requireSpecialChar && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one special character"
            ).addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
```

```java src/main/java/com/validationpatterns/validation/ValidCreditCard.java
package com/validationpatterns/validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom Validator for Credit Card numbers
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreditCardValidator.class)
@Documented
public @interface ValidCreditCard {

    String message() default "Invalid credit card number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    CardType[] acceptedTypes() default {CardType.VISA, CardType.MASTERCARD, CardType.AMEX};

    enum CardType {
        VISA, MASTERCARD, AMEX, DISCOVER
    }
}
```

```java src/main/java/com/validationpatterns/validation/CreditCardValidator.java
package com.validationpatterns.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

/**
 * Custom Validator - Credit Card validation with Luhn algorithm
 */
public class CreditCardValidator implements ConstraintValidator<ValidCreditCard, String> {

    private List<ValidCreditCard.CardType> acceptedTypes;

    @Override
    public void initialize(ValidCreditCard constraintAnnotation) {
        this.acceptedTypes = Arrays.asList(constraintAnnotation.acceptedTypes());
    }

```java src/main/java/com/validationpatterns/validation/CreditCardValidator.java
    @Override
    public boolean isValid(String cardNumber, ConstraintValidatorContext context) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }

        // Remove spaces and dashes
        String cleanedNumber = cardNumber.replaceAll("[\\s-]", "");

        // Check if it contains only digits
        if (!cleanedNumber.matches("\\d+")) {
            return false;
        }

        // Validate card type
        ValidCreditCard.CardType cardType = detectCardType(cleanedNumber);
        if (cardType == null || !acceptedTypes.contains(cardType)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Card type not accepted. Accepted types: " + acceptedTypes
            ).addConstraintViolation();
            return false;
        }

        // Validate using Luhn algorithm
        return luhnCheck(cleanedNumber);
    }

    private ValidCreditCard.CardType detectCardType(String cardNumber) {
        if (cardNumber.matches("^4[0-9]{12}(?:[0-9]{3})?$")) {
            return ValidCreditCard.CardType.VISA;
        } else if (cardNumber.matches("^5[1-5][0-9]{14}$") || 
                   cardNumber.matches("^2[2-7][0-9]{14}$")) {
            return ValidCreditCard.CardType.MASTERCARD;
        } else if (cardNumber.matches("^3[47][0-9]{13}$")) {
            return ValidCreditCard.CardType.AMEX;
        } else if (cardNumber.matches("^6(?:011|5[0-9]{2})[0-9]{12}$")) {
            return ValidCreditCard.CardType.DISCOVER;
        }
        return null;
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
```

```java src/main/java/com/validationpatterns/validation/ValidDateRange.java
package com.validationpatterns.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom Validator - Date range validation at class level
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@Documented
public @interface ValidDateRange {

    String message() default "End date must be after start date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String startDateField();

    String endDateField();

    boolean allowEqual() default false;
}
```

```java src/main/java/com/validationpatterns/validation/DateRangeValidator.java
package com.validationpatterns.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Custom Validator - Validates date ranges
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startDateField;
    private String endDateField;
    private boolean allowEqual;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startDateField = constraintAnnotation.startDateField();
        this.endDateField = constraintAnnotation.endDateField();
        this.allowEqual = constraintAnnotation.allowEqual();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Field startField = value.getClass().getDeclaredField(startDateField);
            Field endField = value.getClass().getDeclaredField(endDateField);

            startField.setAccessible(true);
            endField.setAccessible(true);

            Object startValue = startField.get(value);
            Object endValue = endField.get(value);

            if (startValue == null || endValue == null) {
                return true; // Let @NotNull handle null values
            }

            if (startValue instanceof LocalDateTime && endValue instanceof LocalDateTime) {
                LocalDateTime start = (LocalDateTime) startValue;
                LocalDateTime end = (LocalDateTime) endValue;
                return allowEqual ? !end.isBefore(start) : end.isAfter(start);
            } else if (startValue instanceof LocalDate && endValue instanceof LocalDate) {
                LocalDate start = (LocalDate) startValue;
                LocalDate end = (LocalDate) endValue;
                return allowEqual ? !end.isBefore(start) : end.isAfter(start);
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }
}
```

```java src/main/java/com/validationpatterns/validation/ValidPhone.java
package com.validationpatterns.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom Validator for phone numbers
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
@Documented
public @interface ValidPhone {

    String message() default "Invalid phone number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] regions() default {"US", "UK", "CA"};
}
```

```java src/main/java/com/validationpatterns/validation/PhoneValidator.java
package com.validationpatterns.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.RegexValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom Validator - Phone number validation for different regions
 */
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private static final Map<String, RegexValidator> REGION_PATTERNS = new HashMap<>();

    static {
        REGION_PATTERNS.put("US", new RegexValidator("^\\+?1?[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$"));
        REGION_PATTERNS.put("UK", new RegexValidator("^\\+?44\\s?\\d{4}\\s?\\d{6}$|^0\\d{4}\\s?\\d{6}$"));
        REGION_PATTERNS.put("CA", new RegexValidator("^\\+?1?[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$"));
        REGION_PATTERNS.put("INTL", new RegexValidator("^\\+?[1-9]\\d{1,14}$"));
    }

    private String[] regions;

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        this.regions = constraintAnnotation.regions();
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        for (String region : regions) {
            RegexValidator validator = REGION_PATTERNS.get(region);
            if (validator != null && validator.isValid(phone)) {
                return true;
            }
        }

        return false;
    }
}
```

## 5. Validation Group Pattern

```java src/main/java/com/validationpatterns/validation/groups/ValidationGroups.java
package com.validationpatterns.validation.groups;

/**
 * Validation Group Pattern - Define validation groups
 */
public class ValidationGroups {

    /**
     * Group for create operations
     */
    public interface Create {}

    /**
     * Group for update operations
     */
    public interface Update {}

    /**
     * Group for basic validation
     */
    public interface Basic {}

    /**
     * Group for extended validation
     */
    public interface Extended {}

    /**
     * Group for admin operations
     */
    public interface Admin {}

    /**
     * Group for user operations
     */
    public interface User {}

    /**
     * Sequential validation groups
     */
    @jakarta.validation.GroupSequence({Basic.class, Extended.class})
    public interface CompleteValidation {}
}
```

```java src/main/java/com/validationpatterns/model/Employee.java
package com.validationpatterns.model;

import com.validationpatterns.validation.ValidPassword;
import com.validationpatterns.validation.ValidPhone;
import com.validationpatterns.validation.groups.ValidationGroups;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Validation Group Pattern - Using different validation groups
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Null(groups = ValidationGroups.Create.class, message = "ID must be null for create operations")
    @NotNull(groups = ValidationGroups.Update.class, message = "ID is required for update operations")
    private Long id;

    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
              message = "Employee number is required")
    @Pattern(regexp = "^EMP-\\d{6}$", 
             groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
             message = "Employee number must be in format EMP-XXXXXX")
    private String employeeNumber;

    @NotBlank(groups = ValidationGroups.Basic.class, message = "First name is required")
    @Size(min = 2, max = 50, groups = ValidationGroups.Basic.class)
    private String firstName;

    @NotBlank(groups = ValidationGroups.Basic.class, message = "Last name is required")
    @Size(min = 2, max = 50, groups = ValidationGroups.Basic.class)
    private String lastName;

    @NotBlank(groups = ValidationGroups.Basic.class, message = "Email is required")
    @Email(groups = ValidationGroups.Basic.class)
    private String email;

    @ValidPassword(groups = {ValidationGroups.Create.class, ValidationGroups.Admin.class})
    private String password;

    @NotNull(groups = ValidationGroups.Extended.class, message = "Date of birth is required")
    @Past(groups = ValidationGroups.Extended.class)
    private LocalDate dateOfBirth;

    @ValidPhone(groups = ValidationGroups.Extended.class)
    private String phoneNumber;

    @NotBlank(groups = ValidationGroups.Basic.class, message = "Department is required")
    private String department;

    @NotBlank(groups = ValidationGroups.Basic.class, message = "Position is required")
    private String position;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
             message = "Salary is required")
    @DecimalMin(value = "0.01", groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Digits(integer = 10, fraction = 2, 
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private BigDecimal salary;

    @NotNull(groups = ValidationGroups.Create.class, message = "Hire date is required")
    @PastOrPresent(groups = ValidationGroups.Create.class)
    private LocalDate hireDate;

    @Future(groups = ValidationGroups.Extended.class, message = "Contract end date must be in the future")
    private LocalDate contractEndDate;

    @NotNull(groups = ValidationGroups.Admin.class, message = "Active status is required for admin")
    private Boolean active;

    @Size(max = 1000, groups = ValidationGroups.Extended.class)
    private String notes;

    // Security clearance level - only validated for admin operations
    @NotNull(groups = ValidationGroups.Admin.class, message = "Security clearance is required for admin")
    @Min(value = 1, groups = ValidationGroups.Admin.class)
    @Max(value = 5, groups = ValidationGroups.Admin.class)
    private Integer securityClearance;
}
```

```java src/main/java/com/validationpatterns/model/Booking.java
package com.validationpatterns.model;

import com.validationpatterns.validation.ValidDateRange;
import com.validationpatterns.validation.groups.ValidationGroups;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Validation Group Pattern with date range validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidDateRange(
    startDateField = "checkInDate",
    endDateField = "checkOutDate",
    message = "Check-out date must be after check-in date",
    groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}
)
public class Booking {

    @Null(groups = ValidationGroups.Create.class)
    @NotNull(groups = ValidationGroups.Update.class)
    private Long id;

    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private String guestName;

    @NotBlank(groups = ValidationGroups.Basic.class)
    @Email(groups = ValidationGroups.Basic.class)
    private String guestEmail;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Future(groups = ValidationGroups.Create.class)
    private LocalDateTime checkInDate;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Future(groups = ValidationGroups.Create.class)
    private LocalDateTime checkOutDate;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Min(value = 1, groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Max(value = 10, groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private Integer numberOfGuests;

    @NotBlank(groups = ValidationGroups.Basic.class)
    private String roomType;

    @NotNull(groups = ValidationGroups.Extended.class)
    @DecimalMin(value = "0.01", groups = ValidationGroups.Extended.class)
    private BigDecimal totalAmount;

    @Size(max = 500, groups = ValidationGroups.Extended.class)
    private String specialRequests;

    @AssertTrue(groups = ValidationGroups.Create.class, 
                message = "Terms must be accepted")
    private Boolean termsAccepted;
}
```

## 6. Method Validation Pattern

```java src/main/java/com/validationpatterns/config/MethodValidationConfig.java
package com.validationpatterns.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Method Validation Pattern - Configuration
 */
@Configuration
public class MethodValidationConfig {

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
```

```java src/main/java/com/validationpatterns/service/UserService.java
package com.validationpatterns.service;

import com.validationpatterns.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Method Validation Pattern - Service with validated methods
 */
@Service
@Validated // Enable method-level validation
@Slf4j
public class UserService {

    private final Map<Long, User> userStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Method validation on parameters
     */
    public User createUser(@Valid User user) {
        log.info("Creating user: {}", user.getUsername());
        user.setId(idGenerator.getAndIncrement());
        userStore.put(user.getId(), user);
        return user;
    }

    /**
     * Method validation with constraint annotations
     */
    public User getUserById(@NotNull(message = "User ID cannot be null")
                           @Min(value = 1, message = "User ID must be positive") 
                           Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = userStore.get(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        return user;
    }

    /**
     * Method validation with multiple parameters
     */
    public List<User> searchUsers(
            @NotBlank(message = "Search query cannot be blank") 
            @Size(min = 3, message = "Search query must be at least 3 characters") 
            String query,
            
            @Min(value = 1, message = "Page number must be at least 1") 
            Integer page,
            
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size cannot exceed 100") 
            Integer pageSize) {
        
        log.info("Searching users: query={}, page={}, pageSize={}", query, page, pageSize);
        
        return userStore.values().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(query.toLowerCase()))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    /**
     * Method validation on return value
     */
    @NotNull(message = "User list cannot be null")
    @Size(min = 1, message = "At least one user must exist")
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return new ArrayList<>(userStore.values());
    }

    /**
     * Method validation with email parameter
     */
    public User getUserByEmail(@NotBlank @Email(message = "Valid email is required") String email) {
        log.info("Fetching user by email: {}", email);
        return userStore.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    /**
     * Update user with validation
     */
    public User updateUser(@NotNull @Min(1) Long id, @Valid User user) {
        log.info("Updating user with ID: {}", id);
        
        if (!userStore.containsKey(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        
        user.setId(id);
        userStore.put(id, user);
        return user;
    }

    /**
     * Delete user with validation
     */
    public void deleteUser(@NotNull @Min(1) Long id) {
        log.info("Deleting user with ID: {}", id);
        
        if (!userStore.containsKey(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        
        userStore.remove(id);
    }

    /**
     * Batch operation with collection validation
     */
    public List<User> createUsers(@NotEmpty(message = "User list cannot be empty")
                                  @Size(max = 100, message = "Cannot create more than 100 users at once")
                                  List<@Valid User> users) {
        log.info("Creating {} users", users.size());
        
        List<User> createdUsers = new ArrayList<>();
        for (User user : users) {
            user.setId(idGenerator.getAndIncrement());
            userStore.put(user.getId(), user);
            createdUsers.add(user);
        }
        
        return createdUsers;
    }

    /**
     * Method with pattern validation
     */
    public List<User> getUsersByDepartment(
            @NotBlank 
            @Pattern(regexp = "^[A-Z_]+$", message = "Department must be uppercase with underscores") 
            String department) {
        
        log.info("Fetching users from department: {}", department);
        // Implementation here
        return new ArrayList<>();
    }
}
```

```java src/main/java/com/validationpatterns/service/ProductService.java
package com.validationpatterns.service;

import com.validationpatterns.model.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Method Validation Pattern - Product service
 */
@Service
@Validated
@Slf4j
public class ProductService {

    private final Map<Long, Product> productStore = new ConcurrentHashMap<>();

    public Product createProduct(@Valid Product product) {
        log.info("Creating product: {}", product.getName());
        productStore.put(product.getId(), product);
        return product;
    }

    public void updateStock(
            @NotNull @Min(1) Long productId,
            @NotNull @Min(0) Integer quantity) {
        
        log.info("Updating stock for product {}: quantity={}", productId, quantity);
        
        Product product = productStore.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        
        product.setStockQuantity(quantity);
    }

    public void applyDiscount(
            @NotNull @Min(1) Long productId,
            @NotNull 
            @DecimalMin(value = "0.01", message = "Discount must be at least 0.01%")
            @DecimalMax(value = "99.99", message = "Discount cannot exceed 99.99%")
            BigDecimal discountPercentage) {
        
        log.info("Applying {}% discount to product {}", discountPercentage, productId);
        
        Product product = productStore.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        
        BigDecimal discount = BigDecimal.ONE.subtract(discountPercentage.divide(new BigDecimal("100")));
        product.setPrice(product.getPrice().multiply(discount));
    }

    public List<Product> getProductsByPriceRange(
            @NotNull @DecimalMin("0.00") BigDecimal minPrice,
            @NotNull @DecimalMin("0.01") BigDecimal maxPrice) {
        
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Min price cannot be greater than max price");
        }
        
        log.info("Fetching products in price range: {} - {}", minPrice, maxPrice);
        
        return productStore.values().stream()
                .filter(p -> p.getPrice().compareTo(minPrice) >= 0 && 
                            p.getPrice().compareTo(maxPrice) <= 0)
                .toList();
    }
}
```

## Controllers

```java src/main/java/com/validationpatterns/controller/UserController.java
package com.validationpatterns.controller;

import com.validationpatterns.model.User;
import com.validationpatterns.service.UserService;
import com.validationpatterns.validator.UserValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Demonstrates Validator Pattern and Bean Validation Pattern
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserValidator userValidator;

    /**
     * Register custom validator
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userValidator);
    }

    /**
     * Bean Validation Pattern - @Valid triggers validation
     */
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.info("Creating user: {}", user.getUsername());
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Validator Pattern - Manual validation using BindingResult
     */
    @PostMapping("/validate")
    public ResponseEntity<?> createUserWithCustomValidation(
            @RequestBody User user,
            BindingResult bindingResult) {
        
        log.info("Creating user with custom validation: {}", user.getUsername());
        
        // Manual validation
        userValidator.validate(user, bindingResult);
        
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Method Validation Pattern - validates path variable
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable @NotNull @Min(1) Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Method Validation Pattern - validates multiple parameters
     */
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam @NotBlank String query,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer pageSize) {
        
        List<User> users = userService.searchUsers(query, page, pageSize);
        return ResponseEntity.ok(users);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable @NotNull @Min(1) Long id,
            @Valid @RequestBody User user) {
        
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @NotNull @Min(1) Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

```java src/main/java/com/validationpatterns/controller/ProductController.java
package com.validationpatterns.controller;

import com.validationpatterns.exception.ConstraintViolationException;
import com.validationpatterns.model.Product;
import com.validationpatterns.service.ProductService;
import com.validationpatterns.validator.ProductValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Demonstrates Constraint Violation Pattern
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductValidator productValidator;

    /**
     * Constraint Violation Pattern - Manual validation with custom exception
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("Creating product: {}", product.getName());
        
        // Manual validation using Constraint Violation Pattern
        productValidator.validate(product);
        
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Bean Validation Pattern
     */
    @PostMapping("/validated")
    public ResponseEntity<Product> createProductWithBeanValidation(@Valid @RequestBody Product product) {
        log.info("Creating product with bean validation: {}", product.getName());
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        
        List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
}
```

```java src/main/java/com/validationpatterns/controller/EmployeeController.java
package com.validationpatterns.controller;

import com.validationpatterns.model.Employee;
import com.validationpatterns.validation.groups.ValidationGroups;
import jakarta.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Demonstrates Validation Group Pattern
 */
@RestController
@RequestMapping("/api/employees")
@Slf4j
public class EmployeeController {

    private final Map<Long, Employee> employeeStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

```java src/main/java/com/validationpatterns/controller/EmployeeController.java
    /**
     * Validation Group Pattern - Create with Create group
     */
    @PostMapping
    public ResponseEntity<Employee> createEmployee(
            @Validated({ValidationGroups.Create.class, ValidationGroups.Basic.class})
            @RequestBody Employee employee) {
        
        log.info("Creating employee: {}", employee.getEmployeeNumber());
        employee.setId(idGenerator.getAndIncrement());
        employeeStore.put(employee.getId(), employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    /**
     * Validation Group Pattern - Update with Update group
     */
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @Validated({ValidationGroups.Update.class, ValidationGroups.Basic.class})
            @RequestBody Employee employee) {
        
        log.info("Updating employee: {}", id);
        employee.setId(id);
        employeeStore.put(id, employee);
        return ResponseEntity.ok(employee);
    }

    /**
     * Validation Group Pattern - Basic validation only
     */
    @PostMapping("/basic")
    public ResponseEntity<Employee> createEmployeeBasic(
            @Validated(ValidationGroups.Basic.class)
            @RequestBody Employee employee) {
        
        log.info("Creating employee with basic validation: {}", employee.getEmployeeNumber());
        employee.setId(idGenerator.getAndIncrement());
        employeeStore.put(employee.getId(), employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    /**
     * Validation Group Pattern - Complete validation (sequential)
     */
    @PostMapping("/complete")
    public ResponseEntity<Employee> createEmployeeComplete(
            @Validated(ValidationGroups.CompleteValidation.class)
            @RequestBody Employee employee) {
        
        log.info("Creating employee with complete validation: {}", employee.getEmployeeNumber());
        employee.setId(idGenerator.getAndIncrement());
        employeeStore.put(employee.getId(), employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    /**
     * Validation Group Pattern - Admin-only validation
     */
    @PostMapping("/admin")
    public ResponseEntity<Employee> createEmployeeAdmin(
            @Validated({ValidationGroups.Create.class, ValidationGroups.Admin.class})
            @RequestBody Employee employee) {
        
        log.info("Creating employee with admin validation: {}", employee.getEmployeeNumber());
        employee.setId(idGenerator.getAndIncrement());
        employeeStore.put(employee.getId(), employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Long id) {
        Employee employee = employeeStore.get(id);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee);
    }
}
```

```java src/main/java/com/validationpatterns/controller/BookingController.java
package com.validationpatterns.controller;

import com.validationpatterns.model.Booking;
import com.validationpatterns.validation.groups.ValidationGroups;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Demonstrates Validation Group Pattern with date range validation
 */
@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class BookingController {

    private final Map<Long, Booking> bookingStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Validated({ValidationGroups.Create.class, ValidationGroups.Basic.class})
            @RequestBody Booking booking) {
        
        log.info("Creating booking for: {}", booking.getGuestName());
        booking.setId(idGenerator.getAndIncrement());
        bookingStore.put(booking.getId(), booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(
            @PathVariable Long id,
            @Validated({ValidationGroups.Update.class, ValidationGroups.Basic.class})
            @RequestBody Booking booking) {
        
        log.info("Updating booking: {}", id);
        booking.setId(id);
        bookingStore.put(id, booking);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/extended")
    public ResponseEntity<Booking> createBookingExtended(
            @Validated({ValidationGroups.Create.class, ValidationGroups.Basic.class, ValidationGroups.Extended.class})
            @RequestBody Booking booking) {
        
        log.info("Creating booking with extended validation: {}", booking.getGuestName());
        booking.setId(idGenerator.getAndIncrement());
        bookingStore.put(booking.getId(), booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        Booking booking = bookingStore.get(id);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(booking);
    }
}
```

## Global Exception Handler

```java src/main/java/com/validationpatterns/exception/GlobalExceptionHandler.java
package com.validationpatterns.exception;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Global exception handler for validation errors
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle Bean Validation errors (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        
        log.error("Validation error: {}", ex.getMessage());

        Map<String, List<String>> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            
            errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        });

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid input data")
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle Method Validation errors (@Validated on methods)
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex) {
        
        log.error("Constraint violation: {}", ex.getMessage());

        Map<String, List<String>> errors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            
            errors.computeIfAbsent(propertyPath, k -> new ArrayList<>()).add(message);
        }

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message("Method parameter validation failed")
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle custom Constraint Violation Exception
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleCustomConstraintViolation(
            ConstraintViolationException ex) {
        
        log.error("Custom constraint violation: {}", ex.getMessage());

        Map<String, List<String>> errors = new HashMap<>();
        
        for (ConstraintViolationException.ConstraintViolation violation : ex.getViolations()) {
            errors.computeIfAbsent(violation.getField(), k -> new ArrayList<>())
                    .add(violation.getMessage());
        }

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message(ex.getMessage())
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

```java src/main/java/com/validationpatterns/exception/ValidationErrorResponse.java
package com.validationpatterns.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, List<String>> errors;
}
```

```java src/main/java/com/validationpatterns/exception/ErrorResponse.java
package com.validationpatterns.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}
```

## Additional Custom Validators

```java src/main/java/com/validationpatterns/model/Payment.java
package com.validationpatterns.model;

import com.validationpatterns.validation.ValidCreditCard;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model using custom validators
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Cardholder name is required")
    @Size(min = 2, max = 100, message = "Cardholder name must be between 2 and 100 characters")
    private String cardholderName;

    @ValidCreditCard(
        acceptedTypes = {ValidCreditCard.CardType.VISA, ValidCreditCard.CardType.MASTERCARD},
        message = "Invalid credit card number or card type not accepted"
    )
    private String cardNumber;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Card expiry date must be in the future")
    private LocalDate expiryDate;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^\\d{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;

    @NotBlank(message = "Billing address is required")
    private String billingAddress;

    @NotBlank(message = "Postal code is required")
    private String postalCode;
}
```

```java src/main/java/com/validationpatterns/controller/PaymentController.java
package com.validationpatterns.controller;

import com.validationpatterns.model.Payment;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demonstrates Custom Validator Pattern with credit card validation
 */
@RestController
@RequestMapping("/api/payments")
@Validated
@Slf4j
public class PaymentController {

    private final Map<String, Payment> paymentStore = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<Map<String, String>> processPayment(@Valid @RequestBody Payment payment) {
        log.info("Processing payment: {}", payment.getPaymentId());
        
        // Simulate payment processing
        paymentStore.put(payment.getPaymentId(), payment);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            Map.of(
                "paymentId", payment.getPaymentId(),
                "status", "APPROVED",
                "message", "Payment processed successfully"
            )
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String paymentId) {
        Payment payment = paymentStore.get(paymentId);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        // Mask sensitive data before returning
        payment.setCardNumber("****-****-****-" + payment.getCardNumber().substring(payment.getCardNumber().length() - 4));
        payment.setCvv("***");
        return ResponseEntity.ok(payment);
    }
}
```

## Configuration

```yaml src/main/resources/application.yml
spring:
  application:
    name: validation-patterns

  # Database Configuration
  datasource:
    url: jdbc:h2:mem:validationdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  h2:
    console:
      enabled: true
      path: /h2-console

  # Jackson Configuration
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false

# Server Configuration
server:
  port: 8080
  error:
    include-message: always
    include-binding-errors: always

# Logging Configuration
logging:
  level:
    com.validationpatterns: DEBUG
    org.springframework.validation: DEBUG
    org.hibernate.validator: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

# Validation Configuration
spring.mvc:
  throw-exception-if-no-handler-found: true
```

## Testing

```java src/test/java/com/validationpatterns/UserValidationTest.java
package com.validationpatterns;

import com.validationpatterns.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Bean Validation Pattern
 */
@SpringBootTest
class UserValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void testValidUser() {
        User user = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .age(25)
                .termsAccepted(true)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid user should have no violations");
    }

    @Test
    void testInvalidUsername() {
        User user = User.builder()
                .username("ab") // Too short
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .age(25)
                .termsAccepted(true)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void testInvalidEmail() {
        User user = User.builder()
                .username("john_doe")
                .email("invalid-email") // Invalid format
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .age(25)
                .termsAccepted(true)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testInvalidAge() {
        User user = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .age(15) // Too young
                .termsAccepted(true)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("age")));
    }

    @Test
    void testTermsNotAccepted() {
        User user = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .age(25)
                .termsAccepted(false) // Not accepted
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("termsAccepted")));
    }

    @Test
    void testMultipleViolations() {
        User user = User.builder()
                .username("ab") // Too short
                .email("invalid") // Invalid format
                .password("weak") // Too short
                .firstName("J") // Too short
                .lastName("D") // Too short
                .age(15) // Too young
                .termsAccepted(false) // Not accepted
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.size() >= 6, 
                "Should have multiple violations, got: " + violations.size());
    }
}
```

```java src/test/java/com/validationpatterns/ProductValidatorTest.java
package com.validationpatterns;

import com.validationpatterns.exception.ConstraintViolationException;
import com.validationpatterns.model.Product;
import com.validationpatterns.validator.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Constraint Violation Pattern
 */
class ProductValidatorTest {

    private ProductValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProductValidator();
    }

    @Test
    void testValidProduct() {
        Product product = Product.builder()
                .name("Laptop")
                .sku("TECH-001")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .category("Electronics")
                .weight(2.5)
                .build();

        assertDoesNotThrow(() -> validator.validate(product));
    }

    @Test
    void testInvalidProductName() {
        Product product = Product.builder()
                .name("AB") // Too short
                .sku("TECH-001")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build();

        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> validator.validate(product)
        );

        assertTrue(exception.hasViolations());
        assertTrue(exception.getViolations().stream()
                .anyMatch(v -> v.getField().equals("name")));
    }

    @Test
    void testInvalidSKU() {
        Product product = Product.builder()
                .name("Laptop")
                .sku("tech-001") // Lowercase not allowed
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build();

        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> validator.validate(product)
        );

        assertTrue(exception.getViolations().stream()
                .anyMatch(v -> v.getField().equals("sku")));
    }

    @Test
    void testInvalidPrice() {
        Product product = Product.builder()
                .name("Laptop")
                .sku("TECH-001")
                .price(BigDecimal.ZERO) // Zero not allowed
                .stockQuantity(10)
                .category("Electronics")
                .build();

        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> validator.validate(product)
        );

        assertTrue(exception.getViolations().stream()
                .anyMatch(v -> v.getField().equals("price")));
    }

    @Test
    void testMultipleViolations() {
        Product product = Product.builder()
                .name("") // Empty
                .sku("invalid") // Lowercase
                .price(new BigDecimal("-10")) // Negative
                .stockQuantity(-5) // Negative
                .category("") // Empty
                .build();

        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> validator.validate(product)
        );

        assertTrue(exception.getViolations().size() >= 5);
    }
}
```

```java src/test/java/com/validationpatterns/ValidationGroupTest.java
package com.validationpatterns;

import com.validationpatterns.model.Employee;
import com.validationpatterns.validation.groups.ValidationGroups;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Validation Group Pattern
 */
@SpringBootTest
class ValidationGroupTest {

    @Autowired
    private Validator validator;

    @Test
    void testCreateGroup() {
        Employee employee = Employee.builder()
                .id(1L) // Should be null for create
                .employeeNumber("EMP-123456")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .department("IT")
                .position("Developer")
                .salary(new BigDecimal("75000"))
                .hireDate(LocalDate.now())
                .build();

        Set<ConstraintViolation<Employee>> violations = 
                validator.validate(employee, ValidationGroups.Create.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("id")));
    }

    @Test
    void testUpdateGroup() {
        Employee employee = Employee.builder()
                // ID is null - should fail for update
                .employeeNumber("EMP-123456")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .department("IT")
                .position("Developer")
                .salary(new BigDecimal("75000"))
                .build();

        Set<ConstraintViolation<Employee>> violations = 
                validator.validate(employee, ValidationGroups.Update.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("id")));
    }

    @Test
    void testBasicGroup() {
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .department("IT")
                .position("Developer")
                .build();

        Set<ConstraintViolation<Employee>> violations = 
                validator.validate(employee, ValidationGroups.Basic.class);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testExtendedGroup() {
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .department("IT")
                .position("Developer")
                // Missing extended fields
                .build();

        Set<ConstraintViolation<Employee>> violations = 
                validator.validate(employee, ValidationGroups.Extended.class);

        assertFalse(violations.isEmpty());
    }

    @Test
    void testAdminGroup() {
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .department("IT")
                .position("Developer")
                .password("SecurePass123!")
                // Missing admin fields
                .build();

        Set<ConstraintViolation<Employee>> violations = 
                validator.validate(employee, ValidationGroups.Admin.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("active") ||
                              v.getPropertyPath().toString().equals("securityClearance")));
    }
}
```

```java src/test/java/com/validationpatterns/CustomValidatorTest.java
package com.validationpatterns;

import com.validationpatterns.validation.ValidCreditCard;
import com.validationpatterns.validation.ValidPassword;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Custom Validator Pattern
 */
@SpringBootTest
class CustomValidatorTest {

    @Autowired
    private Validator validator;

    @Test
    void testValidPassword() {
        TestPasswordObject obj = new TestPasswordObject("SecurePass123!");
        Set<ConstraintViolation<TestPasswordObject>> violations = validator.validate(obj);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidPasswordTooShort() {
        TestPasswordObject obj = new TestPasswordObject("Short1!");
        Set<ConstraintViolation<TestPasswordObject>> violations = validator.validate(obj);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidPasswordNoUppercase() {
        TestPasswordObject obj = new TestPasswordObject("securepass123!");
        Set<ConstraintViolation<TestPasswordObject>> violations = validator.validate(obj);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidPasswordNoLowercase() {
        TestPasswordObject obj = new TestPasswordObject("SECUREPASS123!");
        Set<ConstraintViolation<TestPasswordObject>> violations = validator.validate(obj);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidPasswordNoDigit() {
        TestPasswordObject obj = new TestPasswordObject("SecurePassword!");
        Set<ConstraintViolation<TestPasswordObject>> violations = validator.validate(obj);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidPasswordNoSpecialChar() {
        TestPasswordObject obj = new TestPasswordObject("SecurePass123");
        Set<ConstraintViolation<TestPasswordObject>> violations = validator.validate(obj);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidVisaCard() {
        TestCardObject obj = new TestCardObject("4532015112830366");
        Set<ConstraintViolation<TestCardObject>> violations = validator.validate(obj);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidMastercard() {
        TestCardObject obj = new TestCardObject("5425233430109903");
        Set<ConstraintViolation<TestCardObject>> violations = validator.validate(obj);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidCardNumber() {
        TestCardObject obj = new TestCardObject("1234567890123456");
        Set<ConstraintViolation<TestCardObject>> violations = validator.validate(obj);
        assertFalse(violations.isEmpty());
    }

```java src/test/java/com/validationpatterns/CustomValidatorTest.java
    @Test
    void testInvalidCardChecksum() {
        TestCardObject obj = new TestCardObject("4532015112830367"); // Invalid checksum
        Set<ConstraintViolation<TestCardObject>> violations = validator.validate(obj);
        assertFalse(violations.isEmpty());
    }

    // Helper classes for testing
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestPasswordObject {
        @ValidPassword
        private String password;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestCardObject {
        @ValidCreditCard
        private String cardNumber;
    }
}
```

```java src/test/java/com/validationpatterns/MethodValidationTest.java
package com.validationpatterns;

import com.validationpatterns.model.User;
import com.validationpatterns.service.UserService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Method Validation Pattern
 */
@SpringBootTest
class MethodValidationTest {

    @Autowired
    private UserService userService;

    @Test
    void testGetUserByIdWithNull() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.getUserById(null);
        });
    }

    @Test
    void testGetUserByIdWithZero() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.getUserById(0L);
        });
    }

    @Test
    void testGetUserByIdWithNegative() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.getUserById(-1L);
        });
    }

    @Test
    void testSearchUsersWithBlankQuery() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.searchUsers("", 1, 10);
        });
    }

    @Test
    void testSearchUsersWithShortQuery() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.searchUsers("ab", 1, 10);
        });
    }

    @Test
    void testSearchUsersWithInvalidPage() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.searchUsers("test", 0, 10);
        });
    }

    @Test
    void testSearchUsersWithInvalidPageSize() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.searchUsers("test", 1, 0);
        });
    }

    @Test
    void testSearchUsersWithExcessivePageSize() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.searchUsers("test", 1, 101);
        });
    }

    @Test
    void testGetUserByEmailWithInvalidEmail() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.getUserByEmail("invalid-email");
        });
    }

    @Test
    void testCreateUsersWithEmptyList() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.createUsers(java.util.Collections.emptyList());
        });
    }
}
```

## Integration Tests

```java src/test/java/com/validationpatterns/UserControllerIntegrationTest.java
package com.validationpatterns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.validationpatterns.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for User validation
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateValidUser() throws Exception {
        User user = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .age(25)
                .termsAccepted(true)
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testCreateInvalidUser() throws Exception {
        User user = User.builder()
                .username("ab") // Too short
                .email("invalid-email") // Invalid format
                .password("weak") // Too short
                .firstName("J") // Too short
                .lastName("D") // Too short
                .age(15) // Too young
                .termsAccepted(false) // Not accepted
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    void testGetUserWithInvalidId() throws Exception {
        mockMvc.perform(get("/api/users/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Constraint Violation"));
    }

    @Test
    void testSearchUsersWithInvalidQuery() throws Exception {
        mockMvc.perform(get("/api/users/search")
                .param("query", "ab") // Too short
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchUsersWithInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/users/search")
                .param("query", "test")
                .param("page", "0") // Invalid
                .param("pageSize", "10"))
                .andExpect(status().isBadRequest());
    }
}
```

```java src/test/java/com/validationpatterns/EmployeeControllerIntegrationTest.java
package com.validationpatterns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.validationpatterns.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Validation Groups
 */
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateEmployeeWithId() throws Exception {
        Employee employee = Employee.builder()
                .id(1L) // Should be null for create
                .employeeNumber("EMP-123456")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .department("IT")
                .position("Developer")
                .salary(new BigDecimal("75000"))
                .hireDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").exists());
    }

    @Test
    void testCreateEmployeeValid() throws Exception {
        Employee employee = Employee.builder()
                .employeeNumber("EMP-123456")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .department("IT")
                .position("Developer")
                .salary(new BigDecimal("75000"))
                .hireDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeNumber").value("EMP-123456"));
    }

    @Test
    void testUpdateEmployeeWithoutId() throws Exception {
        Employee employee = Employee.builder()
                // ID is null - should fail for update
                .employeeNumber("EMP-123456")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .department("IT")
                .position("Developer")
                .salary(new BigDecimal("75000"))
                .build();

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").exists());
    }

    @Test
    void testCreateEmployeeBasicValidation() throws Exception {
        Employee employee = Employee.builder()
                .employeeNumber("EMP-123456")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .department("IT")
                .position("Developer")
                // Missing extended and create fields
                .build();

        mockMvc.perform(post("/api/employees/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateEmployeeAdminValidation() throws Exception {
        Employee employee = Employee.builder()
                .employeeNumber("EMP-123456")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .department("IT")
                .position("Developer")
                .salary(new BigDecimal("75000"))
                .hireDate(LocalDate.now())
                .active(true)
                .securityClearance(3)
                .build();

        mockMvc.perform(post("/api/employees/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated());
    }
}
```

## README Documentation

```markdown README.md
# Validation Patterns in Spring Boot

This project demonstrates all major validation patterns using Spring Boot and Bean Validation (JSR-303/380).

## Patterns Implemented

### 1. **Validator Pattern**
- Location: `validator/UserValidator.java`, `validator/OrderValidator.java`
- Traditional Spring `Validator` interface implementation
- Manual validation with `Errors` object
- Complete control over validation logic

**Example:**
```java
@Component
public class UserValidator implements Validator {
    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        if (user.getUsername().length() < 3) {
            errors.rejectValue("username", "field.min.length");
        }
    }
}
```

### 2. **Constraint Violation Pattern**
- Location: `exception/ConstraintViolationException.java`, `validator/ProductValidator.java`
- Custom exception for collecting validation violations
- Programmatic validation with detailed error information
- Flexible error handling and reporting

**Example:**
```java
ConstraintViolationException exception = new ConstraintViolationException("Validation failed");
exception.addViolation("price", "Price must be positive", product.getPrice());
if (exception.hasViolations()) {
    throw exception;
}
```

### 3. **Bean Validation Pattern**
- Location: `model/User.java`, `model/Product.java`, `model/Order.java`
- JSR-303/380 annotations (`@NotNull`, `@Size`, `@Email`, etc.)
- Declarative validation on model classes
- Automatic validation with `@Valid` annotation

**Example:**
```java
public class User {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;
    
    @Email(message = "Email must be valid")
    private String email;
}
```

### 4. **Custom Validator Pattern**
- Location: `validation/ValidPassword.java`, `validation/ValidCreditCard.java`
- Custom constraint annotations
- Reusable validation logic
- Complex validation rules

**Example:**
```java
@ValidPassword(minLength = 8, requireUppercase = true)
private String password;

@ValidCreditCard(acceptedTypes = {VISA, MASTERCARD})
private String cardNumber;
```

### 5. **Validation Group Pattern**
- Location: `validation/groups/ValidationGroups.java`, `model/Employee.java`
- Different validation rules for different scenarios
- Sequential validation with `@GroupSequence`
- Conditional validation based on operation type

**Example:**
```java
@Null(groups = Create.class)
@NotNull(groups = Update.class)
private Long id;

// Controller usage
@PostMapping
public ResponseEntity<Employee> create(
    @Validated({Create.class, Basic.class}) @RequestBody Employee employee) {
    // ...
}
```

### 6. **Method Validation Pattern**
- Location: `service/UserService.java`, `service/ProductService.java`
- Validation on method parameters and return values
- Service-layer validation
- AOP-based validation with `@Validated`

**Example:**
```java
@Service
@Validated
public class UserService {
    public User getUserById(
        @NotNull @Min(1) Long id) {
        // ...
    }
}
```

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    REST Controllers                          │
│  - UserController (Validator Pattern)                        │
│  - ProductController (Constraint Violation Pattern)          │
│  - EmployeeController (Validation Group Pattern)            │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                 Validation Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Validator  │  │ Bean         │  │  Custom      │      │
│  │   Pattern    │  │ Validation   │  │  Validators  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  Service Layer                               │
│  - UserService (Method Validation)                           │
│  - ProductService (Method Validation)                        │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Global Exception Handler                        │
│  - MethodArgumentNotValidException                           │
│  - ConstraintViolationException                             │
│  - Custom Exceptions                                         │
└─────────────────────────────────────────────────────────────┘
```

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Access the application
# URL: http://localhost:8080
```

## API Examples

### Bean Validation Pattern

**Create User (Valid):**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "age": 25,
    "termsAccepted": true
  }'
```

**Response:** `201 Created`

**Create User (Invalid):**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ab",
    "email": "invalid-email",
    "password": "weak",
    "firstName": "J",
    "age": 15,
    "termsAccepted": false
  }'
```

**Response:** `400 Bad Request`
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "errors": {
    "username": ["Username must be between 3 and 50 characters"],
    "email": ["Email must be valid"],
    "password": ["Password must be at least 8 characters"],
    "firstName": ["First name must be between 2 and 50 characters"],
    "age": ["Age must be at least 18"],
    "termsAccepted": ["Terms and conditions must be accepted"]
  }
}
```

### Constraint Violation Pattern

**Create Product:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "sku": "TECH-001",
    "price": 999.99,
    "stockQuantity": 10,
    "category": "Electronics",
    "weight": 2.5
  }'
```

**Create Product (Invalid):**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "AB",
    "sku": "tech-001",
    "price": -10,
    "stockQuantity": -5,
    "category": ""
  }'
```

### Method Validation Pattern

**Search Users (Valid):**
```bash
curl "http://localhost:8080/api/users/search?query=john&page=1&pageSize=10"
```

**Search Users (Invalid - short query):**
```bash
curl "http://localhost:8080/api/users/search?query=ab&page=1&pageSize=10"
```

**Response:** `400 Bad Request`
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Constraint Violation",
  "message": "Method parameter validation failed",
  "errors": {
    "searchUsers.query": ["Search query must be at least 3 characters"]
  }
}
```

**Get User by ID (Invalid):**
```bash
curl "http://localhost:8080/api/users/0"
```

**Response:** `400 Bad Request`
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Constraint Violation",
  "message": "Method parameter validation failed",
  "errors": {
    "getUserById.id": ["User ID must be positive"]
  }
}
```

### Validation Group Pattern

**Create Employee (Create Group):**
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "employeeNumber": "EMP-123456",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "department": "IT",
    "position": "Developer",
    "salary": 75000,
    "hireDate": "2024-01-15"
  }'
```

**Create Employee (Basic Group - less strict):**
```bash
curl -X POST http://localhost:8080/api/employees/basic \
  -H "Content-Type: application/json" \
  -d '{
    "employeeNumber": "EMP-123456",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "department": "IT",
    "position": "Developer"
  }'
```

**Create Employee (Admin Group):**
```bash
curl -X POST http://localhost:8080/api/employees/admin \
  -H "Content-Type: application/json" \
  -d '{
    "employeeNumber": "EMP-123456",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "department": "IT",
    "position": "Developer",
    "salary": 75000,
    "hireDate": "2024-01-15",
    "active": true,
    "securityClearance": 3
  }'
```

### Custom Validator Pattern

**Process Payment (Valid Visa):**
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-123",
    "amount": 99.99,
    "cardholderName": "John Doe",
    "cardNumber": "4532015112830366",
    "expiryDate": "2025-12-31",
    "cvv": "123",
    "billingAddress": "123 Main St",
    "postalCode": "12345"
  }'
```

**Process Payment (Invalid Card):**
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-124",
    "amount": 99.99,
    "cardholderName": "John Doe",
    "cardNumber": "1234567890123456",
    "expiryDate": "2025-12-31",
    "cvv": "123",
    "billingAddress": "123 Main St",
    "postalCode": "12345"
  }'
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=UserValidationTest
mvn test -Dtest=ProductValidatorTest
mvn test -Dtest=ValidationGroupTest
mvn test -Dtest=CustomValidatorTest
mvn test -Dtest=MethodValidationTest
```

### Test Coverage
```bash
mvn test jacoco:report
# Report available at: target/site/jacoco/index.html
```

## Pattern Comparison

| Pattern | Use Case | Flexibility | Reusability | Complexity |
|---------|----------|-------------|-------------|------------|
| Validator Pattern | Complex custom validation | High | Medium | Medium |
| Constraint Violation | Programmatic validation | High | Medium | Medium |
| Bean Validation | Simple field validation | Low | High | Low |
| Custom Validator | Reusable complex rules | Medium | High | Medium |
| Validation Group | Contextual validation | Medium | High | Medium |
| Method Validation | Service-layer validation | Low | High | Low |

## Best Practices

### 1. Use Bean Validation for Simple Cases
```java
@NotBlank
@Size(min = 3, max = 50)
private String username;
```

### 2. Create Custom Validators for Complex Rules
```java
@ValidPassword(minLength = 8, requireSpecialChar = true)
private String password;
```

### 3. Use Validation Groups for Different Contexts
```java
@Null(groups = Create.class)
@NotNull(groups = Update.class)
private Long id;
```

### 4. Validate at Multiple Layers
```java
// Controller - Input validation
@PostMapping
public ResponseEntity<User> create(@Valid @RequestBody User user) {
    // Service - Business rule validation
    userService.createUser(user);
}
```

### 5. Provide Clear Error Messages
```java
@NotBlank(message = "Username is required and cannot be empty")
@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
private String username;
```

### 6. Use Method Validation for Service Layer
```java
@Service
@Validated
public class UserService {
    public User getUserById(@NotNull @Min(1) Long id) {
        // ...
    }
}
```

## Common Validation Annotations

### Standard Annotations
- `@NotNull` - Field must not be null
- `@NotBlank` - String must not be null or empty
- `@NotEmpty` - Collection/array must not be empty
- `@Size` - String/collection size constraints
- `@Min/@Max` - Numeric minimum/maximum
- `@Email` - Valid email format
- `@Pattern` - Regex pattern matching
- `@Past/@Future` - Date constraints
- `@Positive/@Negative` - Numeric sign constraints
- `@Digits` - Numeric precision constraints

### Custom Annotations (This Project)
- `@ValidPassword` - Complex password validation
- `@ValidCreditCard` - Credit card validation with Luhn algorithm
- `@ValidPhone` - Phone number validation for multiple regions
- `@ValidDateRange` - Date range validation

## Troubleshooting

### Validation Not Working

1. **Check `@Validated` annotation:**
```java
@RestController
@Validated // Required for method validation
public class UserController {
    // ...
}
```

2. **Check `@Valid` annotation:**
```java
@PostMapping
public ResponseEntity<User> create(@Valid @RequestBody User user) {
    // @Valid triggers bean validation
}
```

3. **Enable method validation:**
```java
@Bean
public MethodValidationPostProcessor methodValidationPostProcessor() {
    return new MethodValidationPostProcessor();
}
```

### Custom Validator Not Working

1. **Ensure validator is registered:**
```java
@Component // Must be a Spring bean
public class CustomValidator implements ConstraintValidator<ValidPassword, String> {
    // ...
}
```

2. **Check constraint annotation:**
```java
@Constraint(validatedBy = CustomValidator.class) // Link to validator
public @interface ValidPassword {
    // ...
}
```

### Validation Groups Not Applied

1. **Specify groups in validation:**
```java
@Validated({Create.class, Basic.class}) // Multiple groups
@RequestBody Employee employee
```

2. **Define groups on constraints:**
```java
@NotNull(groups = Update.class)
private Long id;
```

## License

MIT License
```

This comprehensive implementation covers all 6 validation patterns with working code, tests, and detailed documentation. Each pattern is demonstrated with practical examples that you can run and test immediately.