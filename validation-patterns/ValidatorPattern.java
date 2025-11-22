package com.example.validation.validator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Validator Pattern - Demonstrates Spring's Validator Interface
 * 
 * This pattern shows how to:
 * 1. Implement Spring's Validator interface
 * 2. Create custom validation logic
 * 3. Validate domain objects
 * 4. Register validators with WebDataBinder
 * 5. Handle validation errors
 * 6. Implement multi-field validation
 * 7. Create reusable validators
 * 8. Combine multiple validators
 * 9. Validate nested objects
 * 10. Implement conditional validation
 * 
 * Key Concepts:
 * - Validator: Interface for validating objects
 * - Errors: Object for storing validation errors
 * - ValidationUtils: Helper class for common validations
 * - FieldError: Specific field validation error
 * - ObjectError: Global object validation error
 * 
 * Validator Benefits:
 * 1. Programmatic Validation - Full control over logic
 * 2. Multi-Field Validation - Validate relationships
 * 3. Conditional Logic - Complex validation rules
 * 4. Reusability - Share validators across application
 * 5. Testability - Easy to unit test
 * 
 * Dependencies:
 * - spring-context
 * - spring-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class ValidatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(ValidatorPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("VALIDATOR PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateValidator();
        demonstrateValidationUtils();
        
        System.out.println("\nApplication running with Validator patterns");
        System.out.println("Test endpoints:");
        System.out.println("POST /api/validator/user - Validate user");
        System.out.println("POST /api/validator/order - Validate order");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateValidator() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("VALIDATOR INTERFACE");
        System.out.println("=".repeat(80));
        
        System.out.println("\npublic interface Validator {");
        System.out.println("    boolean supports(Class<?> clazz);");
        System.out.println("    void validate(Object target, Errors errors);");
        System.out.println("}");
    }
    
    private static void demonstrateValidationUtils() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("VALIDATION UTILS HELPERS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n- rejectIfEmpty(errors, field, errorCode)");
        System.out.println("- rejectIfEmptyOrWhitespace(errors, field, errorCode)");
        System.out.println("- invokeValidator(validator, target, errors)");
    }
}

/**
 * User Domain Object
 */
class User {
    private String username;
    private String email;
    private String password;
    private Integer age;
    private String phoneNumber;
    
    public User() {}
    
    public User(String username, String email, String password, Integer age, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.age = age;
        this.phoneNumber = phoneNumber;
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}

/**
 * Order Domain Object
 */
class Order {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private Double totalAmount;
    private LocalDate orderDate;
    private String shippingAddress;
    
    public Order() {
        this.items = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}

/**
 * Order Item
 */
class OrderItem {
    private String productId;
    private Integer quantity;
    private Double price;
    
    public OrderItem() {}
    
    public OrderItem(String productId, Integer quantity, Double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Double getSubtotal() {
        return (quantity != null && price != null) ? quantity * price : 0.0;
    }
}

/**
 * User Validator
 */
@Component
class UserValidator implements Validator {
    
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PHONE_PATTERN = "^\\+?[0-9]{10,15}$";
    
    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        
        // Validate username
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", 
            "username.empty", "Username is required");
        
        if (user.getUsername() != null) {
            if (user.getUsername().length() < 3) {
                errors.rejectValue("username", "username.tooShort", 
                    "Username must be at least 3 characters");
            }
            if (user.getUsername().length() > 50) {
                errors.rejectValue("username", "username.tooLong", 
                    "Username must not exceed 50 characters");
            }
            if (!user.getUsername().matches("^[a-zA-Z0-9_]+$")) {
                errors.rejectValue("username", "username.invalid", 
                    "Username can only contain letters, numbers, and underscores");
            }
        }
        
        // Validate email
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", 
            "email.empty", "Email is required");
        
        if (user.getEmail() != null && !user.getEmail().matches(EMAIL_PATTERN)) {
            errors.rejectValue("email", "email.invalid", "Invalid email format");
        }
        
        // Validate password
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", 
            "password.empty", "Password is required");
        
        if (user.getPassword() != null) {
            if (user.getPassword().length() < 8) {
                errors.rejectValue("password", "password.tooShort", 
                    "Password must be at least 8 characters");
            }
            if (!user.getPassword().matches(".*[A-Z].*")) {
                errors.rejectValue("password", "password.noUppercase", 
                    "Password must contain at least one uppercase letter");
            }
            if (!user.getPassword().matches(".*[0-9].*")) {
                errors.rejectValue("password", "password.noDigit", 
                    "Password must contain at least one digit");
            }
        }
        
        // Validate age
        if (user.getAge() != null) {
            if (user.getAge() < 18) {
                errors.rejectValue("age", "age.tooYoung", 
                    "User must be at least 18 years old");
            }
            if (user.getAge() > 150) {
                errors.rejectValue("age", "age.invalid", "Invalid age");
            }
        }
        
        // Validate phone number
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            if (!user.getPhoneNumber().matches(PHONE_PATTERN)) {
                errors.rejectValue("phoneNumber", "phoneNumber.invalid", 
                    "Invalid phone number format");
            }
        }
    }
}

/**
 * Order Validator
 */
@Component
class OrderValidator implements Validator {
    
    private final OrderItemValidator orderItemValidator;
    
    public OrderValidator(OrderItemValidator orderItemValidator) {
        this.orderItemValidator = orderItemValidator;
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return Order.class.equals(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        Order order = (Order) target;
        
        // Validate order ID
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "orderId", 
            "orderId.empty", "Order ID is required");
        
        // Validate customer ID
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "customerId", 
            "customerId.empty", "Customer ID is required");
        
        // Validate items
        if (order.getItems() == null || order.getItems().isEmpty()) {
            errors.rejectValue("items", "items.empty", 
                "Order must contain at least one item");
        } else {
            // Validate each item
            for (int i = 0; i < order.getItems().size(); i++) {
                OrderItem item = order.getItems().get(i);
                errors.pushNestedPath("items[" + i + "]");
                ValidationUtils.invokeValidator(orderItemValidator, item, errors);
                errors.popNestedPath();
            }
            
            // Validate total amount
            double calculatedTotal = order.getItems().stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
            
            if (order.getTotalAmount() == null) {
                errors.rejectValue("totalAmount", "totalAmount.null", 
                    "Total amount is required");
            } else if (Math.abs(order.getTotalAmount() - calculatedTotal) > 0.01) {
                errors.rejectValue("totalAmount", "totalAmount.mismatch", 
                    String.format("Total amount %.2f does not match calculated total %.2f", 
                        order.getTotalAmount(), calculatedTotal));
            }
        }
        
        // Validate order date
        if (order.getOrderDate() != null && order.getOrderDate().isAfter(LocalDate.now())) {
            errors.rejectValue("orderDate", "orderDate.future", 
                "Order date cannot be in the future");
        }
        
        // Validate shipping address
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "shippingAddress", 
            "shippingAddress.empty", "Shipping address is required");
        
        if (order.getShippingAddress() != null && order.getShippingAddress().length() < 10) {
            errors.rejectValue("shippingAddress", "shippingAddress.tooShort", 
                "Shipping address is too short");
        }
    }
}

/**
 * Order Item Validator
 */
@Component
class OrderItemValidator implements Validator {
    
    @Override
    public boolean supports(Class<?> clazz) {
        return OrderItem.class.equals(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        OrderItem item = (OrderItem) target;
        
        // Validate product ID
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "productId", 
            "productId.empty", "Product ID is required");
        
        // Validate quantity
        if (item.getQuantity() == null) {
            errors.rejectValue("quantity", "quantity.null", "Quantity is required");
        } else if (item.getQuantity() <= 0) {
            errors.rejectValue("quantity", "quantity.invalid", 
                "Quantity must be greater than 0");
        } else if (item.getQuantity() > 1000) {
            errors.rejectValue("quantity", "quantity.tooLarge", 
                "Quantity cannot exceed 1000");
        }
        
        // Validate price
        if (item.getPrice() == null) {
            errors.rejectValue("price", "price.null", "Price is required");
        } else if (item.getPrice() < 0) {
            errors.rejectValue("price", "price.negative", "Price cannot be negative");
        } else if (item.getPrice() > 1000000) {
            errors.rejectValue("price", "price.tooHigh", "Price is unreasonably high");
        }
    }
}

/**
 * Address Validator
 */
@Component
class AddressValidator implements Validator {
    
    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        String address = (String) target;
        
        if (address == null || address.trim().isEmpty()) {
            errors.reject("address.empty", "Address is required");
        } else {
            if (address.length() < 10) {
                errors.reject("address.tooShort", 
                    "Address must be at least 10 characters");
            }
            if (!address.matches(".*\\d+.*")) {
                errors.reject("address.noNumber", 
                    "Address must contain a street number");
            }
        }
    }
}

/**
 * Validation Service
 */
@Service
class ValidationService {
    
    private final UserValidator userValidator;
    private final OrderValidator orderValidator;
    
    public ValidationService(UserValidator userValidator, OrderValidator orderValidator) {
        this.userValidator = userValidator;
        this.orderValidator = orderValidator;
    }
    
    public ValidationResult validateUser(User user) {
        org.springframework.validation.BeanPropertyBindingResult errors = 
            new org.springframework.validation.BeanPropertyBindingResult(user, "user");
        
        userValidator.validate(user, errors);
        
        return new ValidationResult(errors);
    }
    
    public ValidationResult validateOrder(Order order) {
        org.springframework.validation.BeanPropertyBindingResult errors = 
            new org.springframework.validation.BeanPropertyBindingResult(order, "order");
        
        orderValidator.validate(order, errors);
        
        return new ValidationResult(errors);
    }
}

/**
 * Validation Result
 */
class ValidationResult {
    private final boolean valid;
    private final List<FieldErrorInfo> fieldErrors;
    private final List<String> globalErrors;
    
    public ValidationResult(Errors errors) {
        this.valid = !errors.hasErrors();
        this.fieldErrors = new ArrayList<>();
        this.globalErrors = new ArrayList<>();
        
        errors.getFieldErrors().forEach(error -> 
            fieldErrors.add(new FieldErrorInfo(
                error.getField(),
                error.getCode(),
                error.getDefaultMessage(),
                error.getRejectedValue()
            ))
        );
        
        errors.getGlobalErrors().forEach(error -> 
            globalErrors.add(error.getDefaultMessage())
        );
    }
    
    public boolean isValid() { return valid; }
    public List<FieldErrorInfo> getFieldErrors() { return fieldErrors; }
    public List<String> getGlobalErrors() { return globalErrors; }
    
    static class FieldErrorInfo {
        private final String field;
        private final String code;
        private final String message;
        private final Object rejectedValue;
        
        public FieldErrorInfo(String field, String code, String message, Object rejectedValue) {
            this.field = field;
            this.code = code;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
        
        public String getField() { return field; }
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public Object getRejectedValue() { return rejectedValue; }
    }
}

/**
 * REST Controller for Validator Pattern
 */
@RestController
@RequestMapping("/api/validator")
class ValidatorController {
    
    private final ValidationService validationService;
    private final UserValidator userValidator;
    private final OrderValidator orderValidator;
    
    public ValidatorController(ValidationService validationService,
                              UserValidator userValidator,
                              OrderValidator orderValidator) {
        this.validationService = validationService;
        this.userValidator = userValidator;
        this.orderValidator = orderValidator;
    }
    
    @InitBinder("user")
    protected void initUserBinder(WebDataBinder binder) {
        binder.setValidator(userValidator);
    }
    
    @InitBinder("order")
    protected void initOrderBinder(WebDataBinder binder) {
        binder.setValidator(orderValidator);
    }
    
    @PostMapping("/user")
    public Map<String, Object> validateUser(@RequestBody User user) {
        ValidationResult result = validationService.validateUser(user);
        
        return Map.of(
            "valid", result.isValid(),
            "fieldErrors", result.getFieldErrors(),
            "globalErrors", result.getGlobalErrors()
        );
    }
    
    @PostMapping("/order")
    public Map<String, Object> validateOrder(@RequestBody Order order) {
        ValidationResult result = validationService.validateOrder(order);
        
        return Map.of(
            "valid", result.isValid(),
            "fieldErrors", result.getFieldErrors(),
            "globalErrors", result.getGlobalErrors()
        );
    }
    
    @PostMapping("/user/check")
    public Map<String, String> checkUserSupport() {
        boolean supports = userValidator.supports(User.class);
        
        return Map.of(
            "validator", "UserValidator",
            "targetClass", "User",
            "supports", String.valueOf(supports)
        );
    }
}
