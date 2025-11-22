# Conversion and Formatting Patterns

Comprehensive guide to Spring Framework's type conversion and formatting system for transforming data between different types and representations.

## Table of Contents

1. [Overview](#overview)
2. [Patterns Included](#patterns-included)
3. [Dependencies](#dependencies)
4. [Pattern Descriptions](#pattern-descriptions)
5. [Quick Start](#quick-start)
6. [Pattern Comparison](#pattern-comparison)
7. [Best Practices](#best-practices)
8. [Common Use Cases](#common-use-cases)
9. [Production Considerations](#production-considerations)
10. [Testing](#testing)

## Overview

The Conversion and Formatting Patterns demonstrate Spring's comprehensive type conversion and formatting infrastructure. These patterns enable seamless transformation of data between different types, handle user input/output formatting, and provide extensible mechanisms for custom conversions.

### Key Benefits

- **Type Safety**: Compile-time type checking with generic support
- **Extensibility**: Easy to add custom converters and formatters
- **Integration**: Seamless Spring MVC and data binding integration
- **Performance**: Efficient conversion with caching
- **Flexibility**: Multiple conversion strategies for different needs

### When to Use

- Converting request parameters to domain objects
- Formatting domain objects for display
- Type conversion in data binding
- API request/response transformation
- Collection and array type conversions
- Locale-aware formatting

## Patterns Included

### 1. Converter Pattern
**File**: `ConverterPattern.java` (~580 lines)

Demonstrates Spring's `Converter` interface for type-safe, bidirectional conversions.

**Key Components**:
- `Converter<S, T>` - Simple one-to-one conversion
- `GenericConverter` - Multi-type conversions with TypeDescriptor
- `ConditionalGenericConverter` - Conditional conversion logic
- Bidirectional converters

**Example**:
```java
public class StringToUserConverter implements Converter<String, User> {
    @Override
    public User convert(String source) {
        String[] parts = source.split(":");
        User user = new User();
        user.setFirstName(parts[0]);
        user.setLastName(parts[1]);
        user.setEmail(parts[2]);
        return user;
    }
}
```

### 2. Formatter Pattern
**File**: `FormatterPattern.java` (~550 lines)

Demonstrates Spring's `Formatter` interface for locale-aware formatting and parsing.

**Key Components**:
- `Formatter<T>` - Print and parse operations
- `AnnotationFormatterFactory` - Annotation-driven formatting
- Date/Time formatters
- Number formatters
- Custom formatters

**Example**:
```java
public class PhoneNumberFormatter implements Formatter<PhoneNumber> {
    @Override
    public String print(PhoneNumber phone, Locale locale) {
        return String.format("(%s) %s-%s", 
            phone.getAreaCode(), 
            phone.getPrefix(), 
            phone.getLineNumber());
    }
    
    @Override
    public PhoneNumber parse(String text, Locale locale) {
        // Parse logic
    }
}
```

### 3. Type Conversion Pattern
**File**: `TypeConversionPattern.java` (~600 lines)

Demonstrates Spring's type conversion system with generic type handling.

**Key Components**:
- `TypeDescriptor` - Generic type metadata
- Collection conversions
- Map conversions
- Array conversions
- Nested generic type handling

**Example**:
```java
TypeDescriptor sourceType = TypeDescriptor.collection(List.class, 
    TypeDescriptor.valueOf(String.class));
TypeDescriptor targetType = TypeDescriptor.collection(List.class, 
    TypeDescriptor.valueOf(Integer.class));

Object result = conversionService.convert(source, sourceType, targetType);
```

### 4. Property Editor Pattern
**File**: `PropertyEditorPattern.java` (~550 lines)

Demonstrates legacy JavaBeans `PropertyEditor` for backward compatibility.

**Key Components**:
- `PropertyEditor` interface
- `PropertyEditorSupport` base class
- `CustomEditorConfigurer` - Global registration
- `PropertyEditorRegistrar` - Reusable registration
- `WebDataBinder` - MVC integration

**Example**:
```java
public class MoneyPropertyEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) {
        String[] parts = text.split(" ");
        Money money = new Money(
            Currency.getInstance(parts[0]), 
            new BigDecimal(parts[1])
        );
        setValue(money);
    }
    
    @Override
    public String getAsText() {
        Money money = (Money) getValue();
        return money.getCurrency() + " " + money.getAmount();
    }
}
```

### 5. Conversion Service Pattern
**File**: `ConversionServicePattern.java` (~650 lines)

Demonstrates Spring's central `ConversionService` infrastructure and configuration.

**Key Components**:
- `ConversionService` - Central conversion API
- `GenericConversionService` - Base implementation
- `DefaultConversionService` - With default converters
- `FormattingConversionService` - With formatter support
- `ConverterRegistry` - Converter registration

**Example**:
```java
@Configuration
public class ConversionConfig {
    @Bean
    public FormattingConversionService conversionService() {
        DefaultFormattingConversionService service = 
            new DefaultFormattingConversionService();
        
        service.addConverter(new ProductToProductDtoConverter());
        service.addFormatter(new MoneyFormatter());
        service.addConverterFactory(new StringToEnumConverterFactory());
        
        return service;
    }
}
```

## Dependencies

### Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starter Web (includes core, context, web, webmvc) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.2.0</version>
    </dependency>
    
    <!-- For standalone usage -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-core</artifactId>
        <version>6.1.0</version>
    </dependency>
    
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>6.1.0</version>
    </dependency>
</dependencies>
```

### Gradle Dependencies

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web:3.2.0'
    
    // Or for standalone
    implementation 'org.springframework:spring-core:6.1.0'
    implementation 'org.springframework:spring-context:6.1.0'
}
```

## Pattern Descriptions

### Converter Pattern

#### Purpose
Provides type-safe, bidirectional conversion between source and target types.

#### When to Use
- Type-safe conversions with compile-time checking
- Bidirectional conversions (A ↔ B)
- Thread-safe, stateless conversions
- Integration with Spring's type conversion system

#### Implementation Types

**1. Simple Converter**
```java
public class StringToIntegerConverter implements Converter<String, Integer> {
    @Override
    public Integer convert(String source) {
        return Integer.parseInt(source);
    }
}
```

**2. Generic Converter**
```java
public class MultiTypeConverter implements GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
            new ConvertiblePair(String.class, User.class),
            new ConvertiblePair(String.class, Product.class)
        );
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, 
                         TypeDescriptor targetType) {
        // Conversion logic based on types
    }
}
```

**3. Conditional Converter**
```java
public class EntityToDtoConverter implements ConditionalGenericConverter {
    @Override
    public boolean matches(TypeDescriptor sourceType, 
                          TypeDescriptor targetType) {
        // Determine if conversion should apply
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, 
                         TypeDescriptor targetType) {
        // Conditional conversion logic
    }
}
```

### Formatter Pattern

#### Purpose
Provides locale-aware formatting and parsing for display/input.

#### When to Use
- User interface formatting
- Locale-specific display
- Date, time, and number formatting
- Custom display formats

#### Implementation

```java
public class CustomFormatter implements Formatter<CustomType> {
    @Override
    public String print(CustomType object, Locale locale) {
        // Format for display
    }
    
    @Override
    public CustomType parse(String text, Locale locale) 
            throws ParseException {
        // Parse from string
    }
}
```

#### Annotation-Based Formatting

```java
public class FormattedDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @NumberFormat(style = NumberFormat.Style.CURRENCY)
    private BigDecimal price;
}
```

### Type Conversion Pattern

#### Purpose
Handles generic type information and collection conversions.

#### When to Use
- Converting collections with element type preservation
- Generic type conversions
- Map key/value conversions
- Array conversions

#### TypeDescriptor Usage

```java
// List<String> to List<Integer>
TypeDescriptor sourceType = TypeDescriptor.collection(
    List.class, TypeDescriptor.valueOf(String.class));
    
TypeDescriptor targetType = TypeDescriptor.collection(
    List.class, TypeDescriptor.valueOf(Integer.class));

List<Integer> result = (List<Integer>) conversionService.convert(
    stringList, sourceType, targetType);
```

### Property Editor Pattern

#### Purpose
Legacy JavaBeans property editing for backward compatibility.

#### When to Use
- Legacy code integration
- Simple string-based conversions
- JavaBeans compatibility required
- Web form data binding (older applications)

#### Limitations
- **Not thread-safe** (stateful)
- String-based only
- No generic type support
- Legacy API

#### Migration to Converter

**Old (PropertyEditor)**:
```java
public class OldEditor extends PropertyEditorSupport {
    public void setAsText(String text) {
        setValue(parse(text));
    }
}
```

**New (Converter)**:
```java
public class NewConverter implements Converter<String, Type> {
    public Type convert(String source) {
        return parse(source);
    }
}
```

### Conversion Service Pattern

#### Purpose
Central service coordinating all conversions and formatters.

#### ConversionService Hierarchy

```
ConversionService (interface)
├── canConvert(Class, Class)
└── convert(Object, Class)

ConfigurableConversionService (interface)
├── extends ConversionService
└── extends ConverterRegistry

GenericConversionService (implementation)
└── Base implementation, no defaults

DefaultConversionService (implementation)
├── extends GenericConversionService
└── includes default converters

FormattingConversionService (interface)
├── extends ConversionService
└── extends FormatterRegistry

DefaultFormattingConversionService (implementation)
├── extends FormattingConversionService
└── includes formatters + converters
```

#### Configuration

```java
@Configuration
public class ConversionConfig implements WebMvcConfigurer {
    
    @Bean
    public FormattingConversionService conversionService() {
        DefaultFormattingConversionService service = 
            new DefaultFormattingConversionService();
        
        // Add converters
        service.addConverter(new MyConverter());
        
        // Add formatters
        service.addFormatter(new MyFormatter());
        
        // Add converter factories
        service.addConverterFactory(new MyConverterFactory());
        
        return service;
    }
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new MyConverter());
        registry.addFormatter(new MyFormatter());
    }
}
```

## Quick Start

### 1. Basic Converter

```java
// 1. Create converter
@Component
public class StringToUserConverter implements Converter<String, User> {
    @Override
    public User convert(String source) {
        String[] parts = source.split(":");
        return new User(parts[0], parts[1], parts[2]);
    }
}

// 2. Register in configuration
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToUserConverter());
    }
}

// 3. Use in controller
@RestController
public class UserController {
    @GetMapping("/user")
    public User getUser(@RequestParam User user) {
        return user;  // Automatic conversion from String
    }
}
```

### 2. Basic Formatter

```java
// 1. Create formatter
public class MoneyFormatter implements Formatter<Money> {
    @Override
    public String print(Money money, Locale locale) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(money.getCurrency());
        return formatter.format(money.getAmount());
    }
    
    @Override
    public Money parse(String text, Locale locale) throws ParseException {
        // Parse logic
    }
}

// 2. Register formatter
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new MoneyFormatter());
    }
}
```

### 3. Using ConversionService

```java
@Service
public class ProductService {
    
    private final ConversionService conversionService;
    
    public ProductService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    public ProductDto toDto(Product product) {
        return conversionService.convert(product, ProductDto.class);
    }
    
    public List<ProductDto> toDtos(List<Product> products) {
        return products.stream()
            .map(p -> conversionService.convert(p, ProductDto.class))
            .collect(Collectors.toList());
    }
}
```

## Pattern Comparison

### Converter vs Formatter

| Feature | Converter | Formatter |
|---------|-----------|-----------|
| **Thread Safety** | Thread-safe (stateless) | Thread-safe (stateless) |
| **Conversion Types** | Any type to any type | Object ↔ String only |
| **Locale Support** | No | Yes |
| **Use Case** | Internal conversions | UI display/parsing |
| **Bidirectional** | Requires two converters | Built-in (print/parse) |
| **Generic Types** | Supported | Limited |

### Converter vs PropertyEditor

| Feature | Converter | PropertyEditor |
|---------|-----------|---------------|
| **Thread Safety** | ✅ Thread-safe | ❌ Not thread-safe |
| **Type Support** | Generic types | String only |
| **API** | Modern, clean | Legacy JavaBeans |
| **Flexibility** | High | Limited |
| **Recommended** | ✅ Yes | ❌ Legacy only |

### ConversionService Types

| Type | Default Converters | Formatters | Use Case |
|------|-------------------|-----------|-----------|
| **GenericConversionService** | ❌ No | ❌ No | Custom only |
| **DefaultConversionService** | ✅ Yes | ❌ No | Non-web apps |
| **FormattingConversionService** | Interface | Interface | Contract |
| **DefaultFormattingConversionService** | ✅ Yes | ✅ Yes | Web apps |

## Best Practices

### 1. Choose the Right Pattern

```java
// ✅ Use Converter for internal type conversions
@Component
public class EntityToDtoConverter implements Converter<Entity, Dto> {
    @Override
    public Dto convert(Entity source) {
        // Conversion logic
    }
}

// ✅ Use Formatter for UI display/input
@Component
public class DateFormatter implements Formatter<LocalDate> {
    @Override
    public String print(LocalDate date, Locale locale) {
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", locale));
    }
}

// ❌ Avoid PropertyEditor in new code
// Only use for legacy compatibility
```

### 2. Make Converters Stateless

```java
// ✅ Good - Stateless
public class StringToUserConverter implements Converter<String, User> {
    @Override
    public User convert(String source) {
        return parseUser(source);  // No state
    }
}

// ❌ Bad - Stateful
public class StatefulConverter implements Converter<String, User> {
    private String lastConverted;  // Shared state!
    
    @Override
    public User convert(String source) {
        lastConverted = source;  // Not thread-safe
        return parseUser(source);
    }
}
```

### 3. Handle Null Values

```java
// ✅ Good - Null handling
public class SafeConverter implements Converter<String, User> {
    @Override
    public User convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        return parseUser(source);
    }
}
```

### 4. Validate Input

```java
// ✅ Good - Input validation
public class ValidatingConverter implements Converter<String, Email> {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    @Override
    public Email convert(String source) {
        if (!EMAIL_PATTERN.matcher(source).matches()) {
            throw new IllegalArgumentException("Invalid email: " + source);
        }
        return new Email(source);
    }
}
```

### 5. Use ConverterFactory for Enum Families

```java
// ✅ Good - One factory for all enums
public class StringToEnumConverterFactory 
        implements ConverterFactory<String, Enum> {
    
    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }
}
```

### 6. Register Converters Properly

```java
// ✅ Good - Centralized registration
@Configuration
public class ConversionConfig implements WebMvcConfigurer {
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Register all converters
        registry.addConverter(new UserConverter());
        registry.addConverter(new ProductConverter());
        
        // Register formatters
        registry.addFormatter(new DateFormatter());
        
        // Register factories
        registry.addConverterFactory(new EnumConverterFactory());
    }
}
```

### 7. Use ConversionService in Services

```java
// ✅ Good - Inject ConversionService
@Service
public class OrderService {
    private final ConversionService conversionService;
    
    public OrderService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    public OrderDto createOrder(OrderRequest request) {
        Order order = conversionService.convert(request, Order.class);
        // ...
        return conversionService.convert(order, OrderDto.class);
    }
}
```

## Common Use Cases

### 1. Request Parameter Conversion

```java
@RestController
public class ProductController {
    
    // Automatic conversion of String to Product
    @GetMapping("/product")
    public Product getProduct(@RequestParam Product product) {
        return product;
    }
    
    // List conversion
    @GetMapping("/products")
    public List<Product> getProducts(@RequestParam List<Product> products) {
        return products;
    }
}
```

### 2. Path Variable Conversion

```java
@RestController
public class OrderController {
    
    @GetMapping("/orders/{orderId}")
    public Order getOrder(@PathVariable("orderId") OrderId orderId) {
        // OrderId automatically converted from String
        return orderService.findById(orderId);
    }
}
```

### 3. Entity to DTO Conversion

```java
@Service
public class UserService {
    private final ConversionService conversionService;
    
    public UserDto getUser(Long id) {
        User user = userRepository.findById(id);
        return conversionService.convert(user, UserDto.class);
    }
    
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(u -> conversionService.convert(u, UserDto.class))
            .collect(Collectors.toList());
    }
}
```

### 4. Form Data Binding

```java
@Controller
public class RegistrationController {
    
    @PostMapping("/register")
    public String register(@ModelAttribute RegistrationForm form) {
        // Formatters automatically parse form fields
        // - Date strings → LocalDate
        // - Price strings → BigDecimal
        // - Enum strings → Enum values
        userService.register(form);
        return "redirect:/success";
    }
}
```

### 5. Collection Conversion

```java
@Service
public class BatchConversionService {
    private final ConversionService conversionService;
    
    public List<Integer> convertStringList(List<String> strings) {
        TypeDescriptor sourceType = TypeDescriptor.collection(
            List.class, TypeDescriptor.valueOf(String.class));
        TypeDescriptor targetType = TypeDescriptor.collection(
            List.class, TypeDescriptor.valueOf(Integer.class));
        
        return (List<Integer>) conversionService.convert(
            strings, sourceType, targetType);
    }
}
```

## Production Considerations

### 1. Performance

```java
// ✅ Cache expensive converters
@Component
public class CachedConverter implements Converter<String, ComplexObject> {
    private final LoadingCache<String, ComplexObject> cache;
    
    public CachedConverter() {
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, ComplexObject>() {
                @Override
                public ComplexObject load(String key) {
                    return parse(key);
                }
            });
    }
    
    @Override
    public ComplexObject convert(String source) {
        return cache.getUnchecked(source);
    }
}
```

### 2. Error Handling

```java
// ✅ Proper error handling
@Service
public class SafeConversionService {
    private final ConversionService conversionService;
    
    public <T> Optional<T> safeConvert(Object source, Class<T> targetType) {
        try {
            if (conversionService.canConvert(source.getClass(), targetType)) {
                return Optional.ofNullable(
                    conversionService.convert(source, targetType));
            }
        } catch (ConversionException e) {
            log.error("Conversion failed", e);
        }
        return Optional.empty();
    }
}
```

### 3. Logging and Monitoring

```java
@Component
public class MonitoredConverter implements Converter<String, User> {
    private static final Logger log = LoggerFactory.getLogger(MonitoredConverter.class);
    private final MeterRegistry meterRegistry;
    
    @Override
    public User convert(String source) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            User user = parseUser(source);
            sample.stop(meterRegistry.timer("converter.user.success"));
            return user;
        } catch (Exception e) {
            sample.stop(meterRegistry.timer("converter.user.failure"));
            log.error("Conversion failed for input: {}", source, e);
            throw e;
        }
    }
}
```

### 4. Validation Integration

```java
@Component
public class ValidatingConverter implements Converter<String, Email> {
    private final Validator validator;
    
    @Override
    public Email convert(String source) {
        Email email = new Email(source);
        
        Set<ConstraintViolation<Email>> violations = 
            validator.validate(email);
        
        if (!violations.isEmpty()) {
            throw new ConversionException("Invalid email", null);
        }
        
        return email;
    }
}
```

## Testing

### 1. Unit Testing Converters

```java
class StringToUserConverterTest {
    
    private StringToUserConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new StringToUserConverter();
    }
    
    @Test
    void shouldConvertValidString() {
        String input = "John:Doe:john@example.com";
        User user = converter.convert(input);
        
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail());
    }
    
    @Test
    void shouldHandleNullInput() {
        assertNull(converter.convert(null));
    }
    
    @Test
    void shouldThrowOnInvalidFormat() {
        assertThrows(IllegalArgumentException.class, 
            () -> converter.convert("invalid"));
    }
}
```

### 2. Integration Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
class ConversionIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldConvertRequestParameter() throws Exception {
        mockMvc.perform(get("/user")
                .param("user", "John:Doe:john@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"));
    }
}
```

### 3. Testing Formatters

```java
class MoneyFormatterTest {
    
    private MoneyFormatter formatter;
    
    @BeforeEach
    void setUp() {
        formatter = new MoneyFormatter();
    }
    
    @Test
    void shouldFormatMoney() {
        Money money = new Money(Currency.getInstance("USD"), 
            new BigDecimal("1234.56"));
        
        String formatted = formatter.print(money, Locale.US);
        assertEquals("$1,234.56", formatted);
    }
    
    @Test
    void shouldParseMoney() throws ParseException {
        Money money = formatter.parse("$1,234.56", Locale.US);
        
        assertEquals("USD", money.getCurrency().getCurrencyCode());
        assertEquals(new BigDecimal("1234.56"), money.getAmount());
    }
}
```

## Summary

The Conversion and Formatting Patterns provide a comprehensive solution for type transformation in Spring applications:

1. **Converter Pattern**: Type-safe, thread-safe conversions for internal use
2. **Formatter Pattern**: Locale-aware formatting for UI display
3. **Type Conversion Pattern**: Generic type handling and collection conversions
4. **Property Editor Pattern**: Legacy support for JavaBeans compatibility
5. **Conversion Service Pattern**: Central infrastructure coordinating all conversions

### Key Takeaways

- Use **Converters** for internal type transformations
- Use **Formatters** for user-facing display and input
- Prefer modern **Converter** over legacy **PropertyEditor**
- Use **DefaultFormattingConversionService** in web applications
- Make all converters and formatters **stateless** and **thread-safe**
- Validate input and handle errors gracefully
- Test converters and formatters in isolation

### Migration Path

**From PropertyEditor** → **To Converter**
```java
// Old
public class OldEditor extends PropertyEditorSupport { }

// New
public class NewConverter implements Converter<String, Type> { }
```

**From Manual Conversion** → **To ConversionService**
```java
// Old
ProductDto dto = new ProductDto();
dto.setId(product.getId());
dto.setName(product.getName());
// ...

// New
ProductDto dto = conversionService.convert(product, ProductDto.class);
```

These patterns form the foundation of Spring's type system, enabling clean, maintainable code with proper separation of concerns between data transformation and business logic.
