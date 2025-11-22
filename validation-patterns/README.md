# Spring Validation Patterns

Comprehensive guide and implementation examples for Spring Framework validation patterns, covering both Spring Validator interface and JSR-303/380 Bean Validation.

## Table of Contents

- [Overview](#overview)
- [Patterns Included](#patterns-included)
- [Dependencies](#dependencies)
- [Configuration](#configuration)
- [Pattern Descriptions](#pattern-descriptions)
- [Best Practices](#best-practices)
- [Testing](#testing)
- [Production Considerations](#production-considerations)

## Overview

This collection demonstrates six essential validation patterns used in Spring applications:

1. **Validator Pattern** - Spring's Validator interface for programmatic validation
2. **Constraint Violation Pattern** - Handling JSR-303/380 constraint violations
3. **Bean Validation Pattern** - Standard Bean Validation annotations
4. **Custom Validator Pattern** - Creating custom validation constraints
5. **Validation Group Pattern** - Conditional validation using groups
6. **Method Validation Pattern** - Method-level parameter and return value validation

### Why Validation Matters

- **Data Integrity** - Ensure data meets business rules before processing
- **Security** - Prevent injection attacks and malformed data
- **User Experience** - Provide clear, immediate feedback on input errors
- **Business Logic** - Enforce domain rules and constraints
- **API Contracts** - Validate request/response data in REST APIs

## Patterns Included

| Pattern | File | Lines | Key Features |
|---------|------|-------|--------------|
| Validator | `ValidatorPattern.java` | ~574 | Spring Validator interface, ValidationUtils, WebDataBinder |
| Constraint Violation | `ConstraintViolationPattern.java` | ~560 | Violation handling, error formatting, property paths |
| Bean Validation | `BeanValidationPattern.java` | ~650 | JSR-303/380 annotations, cascading validation |
| Custom Validator | `CustomValidatorPattern.java` | ~680 | Custom constraints, ConstraintValidator interface |
| Validation Groups | `ValidationGroupPattern.java` | ~620 | Group-based validation, sequences, scenarios |
| Method Validation | `MethodValidationPattern.java` | ~600 | Parameter/return validation, @Validated |

**Total:** 6 patterns, ~3,684 lines of code

## Dependencies

### Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starter Validation (includes everything) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Web (for REST controllers) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Jakarta Validation API -->
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
        <version>3.0.2</version>
    </dependency>
    
    <!-- Hibernate Validator (Reference Implementation) -->
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>8.0.1.Final</version>
    </dependency>
</dependencies>
```

### Gradle Dependencies

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'jakarta.validation:jakarta.validation-api:3.0.2'
    implementation 'org.hibernate.validator:hibernate-validator:8.0.1.Final'
}
```

## Configuration

### Enable Method Validation

```java
@Configuration
public class ValidationConfig {
    
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
```

### Custom Validator Factory

```java
@Configuration
public class ValidationConfig {
    
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        // Customize validator configuration
        return bean;
    }
}
```

### Custom Message Source

```java
@Bean
public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource = 
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:validation-messages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
}
```

## Pattern Descriptions

### 1. Validator Pattern

**Purpose:** Implement Spring's Validator interface for programmatic validation with full control over validation logic.

**Key Components:**
- `Validator` interface
- `Errors` object for storing violations
- `ValidationUtils` helper class
- `WebDataBinder` registration

**Use Cases:**
- Complex multi-field validation
- Conditional validation logic
- Nested object validation
- Custom business rules
- Legacy code integration

**Example:**

```java
@Component
public class UserValidator implements Validator {
    
    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        
        ValidationUtils.rejectIfEmptyOrWhitespace(
            errors, "username", "username.required");
        
        if (user.getAge() != null && user.getAge() < 18) {
            errors.rejectValue("age", "age.tooYoung", 
                "Must be 18 or older");
        }
    }
}
```

**Advantages:**
- Full programmatic control
- Easy to test
- Can access external services
- Complex validation logic
- Multi-field validation

**Limitations:**
- More verbose than annotations
- Requires manual registration
- Not declarative

### 2. Constraint Violation Pattern

**Purpose:** Handle and process JSR-303/380 constraint violations for error reporting and user feedback.

**Key Components:**
- `ConstraintViolation<T>` objects
- `ConstraintViolationException`
- Property paths
- Message interpolation
- Violation metadata

**Use Cases:**
- REST API error responses
- Form validation feedback
- Logging validation errors
- Custom error formatting
- Violation aggregation

**Example:**

```java
@Service
public class ViolationHandler {
    
    public <T> Map<String, List<String>> formatViolations(
            Set<ConstraintViolation<T>> violations) {
        
        return violations.stream()
            .collect(Collectors.groupingBy(
                v -> v.getPropertyPath().toString(),
                Collectors.mapping(
                    ConstraintViolation::getMessage,
                    Collectors.toList()
                )
            ));
    }
}
```

**Information Available:**
- Property path to invalid value
- Error message (interpolated)
- Invalid value
- Root bean class
- Constraint descriptor
- Message template

### 3. Bean Validation Pattern

**Purpose:** Use standard JSR-303/380 annotations for declarative validation.

**Standard Constraints:**

| Category | Annotations |
|----------|-------------|
| Null Checks | `@NotNull`, `@Null` |
| Boolean | `@AssertTrue`, `@AssertFalse` |
| Numeric | `@Min`, `@Max`, `@Positive`, `@Negative`, `@DecimalMin`, `@DecimalMax` |
| Size | `@Size` (String, Collection, Array, Map) |
| String | `@NotEmpty`, `@NotBlank`, `@Email`, `@Pattern` |
| Date/Time | `@Past`, `@PastOrPresent`, `@Future`, `@FutureOrPresent` |
| Cascading | `@Valid` |

**Example:**

```java
public class User {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50)
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Min(value = 18, message = "Must be 18 or older")
    @Max(value = 120, message = "Age must be realistic")
    private Integer age;
    
    @Valid
    @NotNull
    private Address address;
}
```

**Cascading Validation:**

```java
public class Customer {
    
    @Valid
    @NotNull
    private Address address;
    
    @Valid
    @NotEmpty
    private List<Order> orders;
}
```

### 4. Custom Validator Pattern

**Purpose:** Create custom validation constraints with reusable, domain-specific validation logic.

**Key Components:**
- Custom constraint annotation
- `ConstraintValidator` implementation
- `initialize()` method for setup
- `isValid()` method for validation logic

**Example:**

```java
// Define constraint annotation
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCreditCardValidator.class)
@Documented
public @interface ValidCreditCard {
    String message() default "Invalid credit card number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Implement validator
public class ValidCreditCardValidator 
        implements ConstraintValidator<ValidCreditCard, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return passesLuhnCheck(value);
    }
    
    private boolean passesLuhnCheck(String cardNumber) {
        // Luhn algorithm implementation
        return true;
    }
}

// Use custom constraint
public class Payment {
    
    @ValidCreditCard
    private String cardNumber;
}
```

**Advanced Features:**
- Parameterized constraints
- Class-level validation
- Cross-field validation
- Composite constraints
- Conditional validation

**Parameterized Example:**

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PriceRangeValidator.class)
public @interface PriceRange {
    String message() default "Price out of range";
    double min() default 0.0;
    double max() default Double.MAX_VALUE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### 5. Validation Group Pattern

**Purpose:** Apply different validation rules for different scenarios using validation groups.

**Key Concepts:**
- Interface-based groups
- Default group
- Group sequences
- `@GroupSequence` annotation
- Conditional validation

**Example:**

```java
// Define groups
public interface ValidationGroups {
    interface Create {}
    interface Update {}
}

// Apply groups to constraints
public class UserDto {
    
    @Null(groups = Create.class)
    @NotNull(groups = Update.class)
    private Long id;
    
    @NotBlank(groups = {Create.class, Update.class})
    private String username;
    
    @NotBlank(groups = Create.class)
    @Null(groups = Update.class)
    private String password;
}

// Validate with specific group
Set<ConstraintViolation<UserDto>> violations = 
    validator.validate(user, ValidationGroups.Create.class);
```

**Group Sequences:**

```java
@GroupSequence({BasicCheck.class, AdvancedCheck.class, Product.class})
public class Product {
    
    @NotBlank(groups = BasicCheck.class)
    private String name;
    
    @Pattern(regexp = "^[A-Z]{3}-\\d{6}$", groups = AdvancedCheck.class)
    private String sku;
}
```

**Use Cases:**
- Create vs Update operations
- Step-by-step form validation
- Role-based validation
- Partial vs complete validation
- Progressive validation

### 6. Method Validation Pattern

**Purpose:** Validate method parameters and return values at the service layer.

**Key Components:**
- `@Validated` annotation on class
- `MethodValidationPostProcessor` bean
- Parameter constraints
- Return value validation with `@Valid`
- `ConstraintViolationException`

**Example:**

```java
@Service
@Validated
public class UserService {
    
    // Validate parameters
    public User createUser(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @Min(18) Integer age) {
        
        User user = new User(username, email, age);
        return userRepository.save(user);
    }
    
    // Validate return value
    @Valid
    public User getUserById(@NotNull @Positive Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException());
    }
    
    // Validate collections
    public void validateUsers(
            @NotEmpty List<@Valid User> users) {
        // Process users
    }
}
```

**REST Controller Example:**

```java
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
            @PathVariable @NotNull @Positive Long id) {
        
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam @Size(min = 2, max = 100) String query,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        List<User> users = userService.searchUsers(query, limit);
        return ResponseEntity.ok(users);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            ConstraintViolationException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            errors.put(
                violation.getPropertyPath().toString(),
                violation.getMessage()
            );
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
```

## Best Practices

### 1. Choose the Right Validation Approach

| Scenario | Recommended Pattern |
|----------|---------------------|
| Simple field validation | Bean Validation annotations |
| Complex business rules | Spring Validator interface |
| Multi-field validation | Custom constraints or Validator |
| Different scenarios | Validation Groups |
| Service layer | Method Validation |
| REST API parameters | Method Validation |

### 2. Validation Layer Strategy

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  (REST Controllers, Web Controllers)│
│  - Basic format validation          │
│  - @Valid for request bodies        │
│  - @Validated for path/query params │
└─────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│          Service Layer              │
│  - Business rule validation         │
│  - @Validated on service classes    │
│  - Method parameter validation      │
│  - Cross-entity validation          │
└─────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│        Persistence Layer            │
│  - Database constraints             │
│  - Entity validation                │
│  - @Valid on entities               │
└─────────────────────────────────────┘
```

### 3. Error Message Guidelines

**Good Messages:**
```java
@NotBlank(message = "Username is required")
@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
@Email(message = "Please provide a valid email address")
@Pattern(regexp = "^[A-Z]{2}\\d{6}$", 
         message = "Order ID must be in format: XX123456")
```

**Use Message Interpolation:**
```java
@Size(min = 3, max = 50, 
      message = "Username must be between {min} and {max} characters")
@DecimalMin(value = "0.01", 
            message = "Price must be at least {value}")
```

**Externalize Messages:**

`ValidationMessages.properties`:
```properties
user.username.required=Username is required
user.username.size=Username must be between {min} and {max} characters
user.email.invalid=Please provide a valid email address
user.age.min=You must be at least {value} years old
```

Usage:
```java
@NotBlank(message = "{user.username.required}")
@Size(min = 3, max = 50, message = "{user.username.size}")
private String username;
```

### 4. Performance Optimization

**Validation Order:**
```java
// Use group sequences to validate cheap checks first
@GroupSequence({QuickCheck.class, ExpensiveCheck.class, Product.class})
public class Product {
    
    @NotBlank(groups = QuickCheck.class)
    private String name;
    
    @ValidSku(groups = ExpensiveCheck.class) // Expensive validation
    private String sku;
}
```

**Cache Validators:**
```java
@Service
public class ValidationService {
    
    private final Validator validator;
    
    public ValidationService(Validator validator) {
        this.validator = validator; // Reuse validator instance
    }
}
```

### 5. Null Handling

**Always consider null:**
```java
public class CustomValidator implements ConstraintValidator<MyConstraint, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }
        return value.matches("pattern");
    }
}
```

### 6. Fail Fast with Group Sequences

```java
@GroupSequence({First.class, Second.class, Third.class, User.class})
public class User {
    
    @NotNull(groups = First.class)
    private String username;
    
    @Size(min = 3, groups = Second.class)
    private String username;
    
    @Pattern(regexp = "...", groups = Third.class)
    private String username;
}
```

### 7. REST API Error Responses

**Standard Format:**
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex) {
    
    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    response.setError("Validation Failed");
    
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> {
        fieldErrors.put(error.getField(), error.getDefaultMessage());
    });
    
    response.setFieldErrors(fieldErrors);
    return ResponseEntity.badRequest().body(response);
}
```

**Error Response Structure:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "fieldErrors": {
    "username": "Username must be between 3 and 50 characters",
    "email": "Please provide a valid email address",
    "age": "You must be at least 18 years old"
  }
}
```

## Testing

### Unit Testing Validators

```java
@Test
public void testUserValidator() {
    UserValidator validator = new UserValidator();
    User user = new User();
    user.setUsername("ab"); // Too short
    
    DataBinder binder = new DataBinder(user);
    binder.addValidators(validator);
    binder.validate();
    
    BindingResult result = binder.getBindingResult();
    assertTrue(result.hasErrors());
    assertEquals(1, result.getFieldErrorCount("username"));
}
```

### Testing Bean Validation

```java
@Test
public void testBeanValidation() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    
    User user = new User();
    user.setEmail("invalid-email");
    
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
}
```

### Testing Custom Constraints

```java
@Test
public void testCustomConstraint() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    
    Payment payment = new Payment();
    payment.setCardNumber("1234-5678-9012-3456"); // Invalid card
    
    Set<ConstraintViolation<Payment>> violations = validator.validate(payment);
    
    assertFalse(violations.isEmpty());
}
```

### Testing Method Validation

```java
@SpringBootTest
public class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    public void testMethodValidation() {
        assertThrows(ConstraintViolationException.class, () -> {
            userService.createUser("ab", "invalid-email", 15);
        });
    }
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testValidationInController() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ab\",\"email\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }
}
```

## Production Considerations

### 1. Logging

```java
@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        
        log.warn("Validation failed: {} errors", 
                 ex.getBindingResult().getErrorCount());
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            log.debug("Field: {}, Error: {}", 
                     error.getField(), error.getDefaultMessage());
        });
        
        return ResponseEntity.badRequest().body(createErrorResponse(ex));
    }
}
```

### 2. Monitoring

```java
@Aspect
@Component
public class ValidationMonitoringAspect {
    
    private final MeterRegistry meterRegistry;
    
    @AfterThrowing(
        pointcut = "execution(* com.example..*(..))",
        throwing = "ex"
    )
    public void monitorValidationFailures(ConstraintViolationException ex) {
        meterRegistry.counter("validation.failures",
            "type", "constraint_violation",
            "count", String.valueOf(ex.getConstraintViolations().size())
        ).increment();
    }
}
```

### 3. Security Considerations

- **Sanitize error messages** - Don't expose internal details
- **Rate limiting** - Prevent validation abuse
- **Input size limits** - Prevent DOS attacks
- **Escape output** - Prevent XSS in error messages

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex) {
    
    ErrorResponse response = new ErrorResponse();
    
    // Sanitize messages before sending to client
    Map<String, String> sanitizedErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            error -> sanitizeMessage(error.getDefaultMessage())
        ));
    
    response.setFieldErrors(sanitizedErrors);
    return ResponseEntity.badRequest().body(response);
}

private String sanitizeMessage(String message) {
    // Remove sensitive information, escape HTML, etc.
    return HtmlUtils.htmlEscape(message);
}
```

### 4. Internationalization

```java
@Configuration
public class ValidationConfig {
    
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
    
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = 
            new ReloadableResourceBundleMessageSource();
        source.setBasenames(
            "classpath:validation-messages",
            "classpath:messages"
        );
        source.setDefaultEncoding("UTF-8");
        source.setCacheSeconds(3600);
        return source;
    }
}
```

### 5. Performance Tuning

- Cache validator instances
- Use group sequences for expensive validations
- Consider async validation for non-critical paths
- Profile validation hotspots
- Optimize regex patterns

## Common Pitfalls

### 1. Forgetting @Valid for Cascading

```java
// ❌ Wrong - nested object won't be validated
public class Order {
    @NotNull
    private Address address;
}

// ✅ Correct - nested object will be validated
public class Order {
    @Valid
    @NotNull
    private Address address;
}
```

### 2. Mixing @Valid and @Validated

```java
// Use @Valid for bean validation
public void createUser(@Valid @RequestBody User user) { }

// Use @Validated for method validation
@Validated
public class UserService {
    public void createUser(@NotBlank String username) { }
}
```

### 3. Not Handling Null in Custom Validators

```java
// ❌ Wrong - NullPointerException
public boolean isValid(String value, ConstraintValidatorContext context) {
    return value.matches("pattern");
}

// ✅ Correct
public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return true;
    return value.matches("pattern");
}
```

### 4. Incorrect Group Definitions

```java
// ❌ Wrong - using classes
public class CreateGroup { }

// ✅ Correct - using interfaces
public interface Create { }
```

## Resources

- [Jakarta Bean Validation Specification](https://jakarta.ee/specifications/bean-validation/)
- [Hibernate Validator Documentation](https://hibernate.org/validator/documentation/)
- [Spring Framework Validation](https://docs.spring.io/spring-framework/reference/core/validation.html)
- [Spring Boot Validation](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.validation)

## Summary

This collection provides comprehensive examples of all major validation patterns in Spring:

- **6 validation patterns** covering all common scenarios
- **~3,684 lines** of production-ready code
- **Complete examples** with REST endpoints
- **Best practices** and anti-patterns
- **Testing strategies** for each pattern
- **Production considerations** for enterprise applications

Each pattern is self-contained with detailed documentation and can be run independently for demonstration purposes.

---

**Note:** These examples are designed for educational purposes and demonstrate validation concepts. In production, customize validation logic, messages, and error handling to match your specific requirements.
