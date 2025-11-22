# Expression Language Patterns

This directory contains comprehensive examples of **Expression Language Patterns** in Spring Framework, focusing on Spring Expression Language (SpEL) and property placeholders.

## Overview

Spring Expression Language (SpEL) is a powerful expression language that supports querying and manipulating object graphs at runtime. It provides features found in most expression languages like operators, built-in functions, and the ability to reference beans and properties.

## Patterns Included

### 1. SpEL (Spring Expression Language) Pattern (`SpELPattern.java`)
Comprehensive demonstration of SpEL features and capabilities.

**Key Features:**
- Property access (direct and nested)
- Mathematical operations (+, -, *, /, %)
- Logical operations (and, or, not)
- Comparison operators (==, !=, <, >, <=, >=)
- Ternary operator (? :)
- Elvis operator (?:)
- Safe navigation operator (?.)
- Method invocation
- Collection operations
- Type operations (T())
- Variables (#variableName)

**Examples:**
```java
// Property access
"username"  // Direct property
"user.email"  // Nested property
"roles[0]"  // Array/List access

// Operations
"10 + 5"  // Addition
"age >= 18"  // Comparison
"active == true and age > 18"  // Logical

// Ternary
"age >= 18 ? 'Adult' : 'Minor'"

// Elvis (null-safe default)
"email ?: 'no-email@example.com'"

// Safe navigation
"user?.username"  // Returns null if user is null

// Method calls
"getFullName()"
"calculateTax(0.25)"

// Type operations
"T(Math).sqrt(16)"
"T(Math).PI"
```

**Use Cases:**
- Dynamic property evaluation
- Configuration expressions
- Security expressions (@PreAuthorize, @PostAuthorize)
- Conditional bean creation
- Data transformation
- Validation rules

---

### 2. Property Placeholder Pattern (`PropertyPlaceholderPattern.java`)
Demonstrates property resolution using ${} syntax.

**Key Features:**
- ${property.name} - Basic placeholder
- ${property.name:default} - With default value
- System properties access
- Environment variables
- @ConfigurationProperties
- Environment API
- Profile-specific properties
- Type conversion

**Examples:**
```java
// Basic placeholder
@Value("${app.name}")
private String appName;

// With default
@Value("${server.port:8080}")
private int serverPort;

// Nested properties
@Value("${database.pool.max-size:10}")
private int maxPoolSize;

// System properties
@Value("${user.name}")
private String systemUser;

// List/Array
@Value("${allowed.origins:http://localhost:3000,http://localhost:4200}")
private List<String> allowedOrigins;

// ConfigurationProperties
@ConfigurationProperties(prefix = "app")
record AppProperties(
    String name,
    String version,
    ServerConfig server
) {}
```

**application.properties:**
```properties
app.name=My Application
app.version=1.0.0
server.port=8080
database.url=jdbc:postgresql://localhost:5432/mydb
database.pool.max-size=20
allowed.origins=http://localhost:3000,http://localhost:4200
```

**Use Cases:**
- Externalized configuration
- Environment-specific settings
- Feature flags
- Database configuration
- API keys and secrets
- Multi-environment deployments

---

### 3. Bean Reference Pattern (`BeanReferencePattern.java`)
Demonstrates referencing Spring beans using SpEL.

**Key Features:**
- @beanName - Reference bean by name
- @beanName.property - Access bean property
- @beanName.method() - Call bean method
- ApplicationContext bean lookup
- Dynamic bean resolution
- Bean method chaining
- Conditional bean selection

**Examples:**
```java
// Reference bean
@Value("#{@configService}")
private ConfigService configService;

// Access bean property
@Value("#{@configService.appName}")
private String appName;

// Call bean method
@Value("#{@configService.getVersion()}")
private String version;

// Method with arguments
@Value("#{@calculatorService.add(10, 20)}")
private int sum;

// Chain methods
@Value("#{@productService.getAllProducts().size()}")
private int productCount;

// Conditional bean selection
@Value("#{condition ? @bean1 : @bean2}")
private SomeService service;

// Filter collection from bean
@Value("#{@productService.getAllProducts().?[price > 500]}")
private List<Product> expensiveProducts;
```

**Use Cases:**
- Injecting bean method results
- Dynamic bean selection
- Configuration-driven bean wiring
- Cross-bean calculations
- Bean property aggregation

---

### 4. Method Invocation Pattern (`MethodInvocationPattern.java`)
Comprehensive guide to method invocation in SpEL.

**Key Features:**
- Instance method calls
- Static method calls
- Method chaining
- Overloaded methods
- Varargs methods
- String methods
- Math methods
- Collection methods

**Examples:**
```java
// Instance methods
"employee.getFullName()"
"employee.calculateTax(0.25)"

// Static methods
"T(Math).sqrt(16)"
"T(Math).pow(2, 8)"
"T(String).valueOf(123)"
"T(java.time.LocalDate).now()"

// String methods
"'hello world'.toUpperCase()"
"'test'.substring(0, 2)"
"'  trim  '.trim()"

// Method chaining
"'hello'.toUpperCase().substring(0, 3)"
"user.getAddress().getCity()"

// Collection methods
"#list.size()"
"#list.isEmpty()"
"#list.contains('value')"
"#list.stream().filter(x -> x > 5).count()"
```

**Common Methods:**
- **String**: length(), substring(), toUpperCase(), toLowerCase(), trim(), concat(), replace(), split()
- **Math**: sqrt(), pow(), abs(), max(), min(), ceil(), floor(), round(), random()
- **Collection**: size(), isEmpty(), contains(), get(), add(), remove(), stream()

---

### 5. Collection Selection Pattern (`CollectionSelectionPattern.java`)
Demonstrates filtering collections using SpEL.

**Key Features:**
- .?[condition] - Select all matching
- .^[condition] - First match
- .$[condition] - Last match
- Property-based filtering
- Method-based filtering
- Complex conditions (and, or)
- Range checks
- String matching

**Examples:**
```java
// Select all matching
"#products.?[price > 500]"  // Expensive products
"#customers.?[tier == 'GOLD']"  // Gold customers
"#employees.?[department == 'IT' and salary > 80000]"  // IT high earners

// First match
"#products.^[price > 500]"  // First expensive product

// Last match
"#products.$[price > 500]"  // Last expensive product

// Method-based
"#products.?[isExpensive()]"
"#customers.?[isPremium()]"

// Complex conditions
"#products.?[price > 100 and available == true and stock > 0]"
"#customers.?[isAdult() and active == true]"

// String matching
"#products.?[name.startsWith('L')]"
"#products.?[category.contains('Electronics')]"

// Range checks
"#products.?[price >= 200 and price <= 600]"
```

**Use Cases:**
- Filter products by criteria
- Find qualified customers
- Search implementations
- Data querying
- Report generation

---

### 6. Collection Projection Pattern (`CollectionProjectionPattern.java`)
Demonstrates transforming/mapping collections using SpEL.

**Key Features:**
- .![expression] - Transform all elements
- Property extraction
- Method-based transformation
- Expression-based transformation
- Nested projections
- Combining selection and projection

**Examples:**
```java
// Extract property
"#products.![name]"  // List of product names
"#customers.![email]"  // List of emails

// Method-based
"#products.![getDisplayName()]"
"#customers.![getFullName()]"

// Nested property
"#customers.![address.city]"  // List of cities
"#orders.![customer.name]"  // List of customer names

// Expressions
"#products.![price * 0.9]"  // Discounted prices
"#products.![name + ' - $' + price]"  // Formatted strings
"#numbers.![#this * #this]"  // Squared numbers

// Selection + Projection
"#products.?[price > 500].![name]"  // Names of expensive products
"#customers.?[tier == 'GOLD'].![email]"  // Emails of gold customers

// Nested collections
"#employees.![skills.![name]]"  // List of skill name lists
```

**Use Cases:**
- Extract specific fields for DTOs
- Transform data for display
- Create summaries/reports
- Map domain objects to view models
- Data aggregation

---

### 7. Template Expression Pattern (`TemplateExpressionPattern.java`)
Demonstrates string templating with SpEL.

**Key Features:**
- TemplateParserContext with #{} delimiters
- Custom delimiters
- String interpolation
- Mixed literal and expression content
- Email/notification templates
- Report templates
- Dynamic message generation

**Examples:**
```java
// Basic template
"Hello #{#name}! Welcome to #{#appName}."

// With method calls
"Welcome back, #{#user.getFullName()}!"

// Multiple expressions
"""
Order ##{#orderNumber}
Customer: #{#customer.name}
Total: $#{#total}
Status: #{#status}
"""

// Conditional content
"#{#stock > 0 ? 'In Stock' : 'Out of Stock'}"

// Email template
"""
To: #{#user.email}
Subject: #{#subject}

Dear #{#user.getFullName()},

#{#message}

Best regards,
The #{#appName} Team
"""

// Report template
"""
Daily Report - #{#date}
Orders: #{#totalOrders}
Revenue: $#{T(String).format('%.2f', #revenue)}
#{#revenue > #target ? 'TARGET ACHIEVED!' : 'Below target'}
"""

// Custom delimiters ({{ }})
ParserContext custom = new ParserContext() {
    public String getExpressionPrefix() { return "{{"; }
    public String getExpressionSuffix() { return "}}"; }
    public boolean isTemplate() { return true; }
};
```

**Use Cases:**
- Email templates
- SMS notifications
- Invoice generation
- Receipt templates
- Report generation
- Dynamic content
- Localized messages

---

## SpEL Syntax Quick Reference

### Operators
```java
// Arithmetic
+, -, *, /, %, ^  // Basic math, power

// Comparison
==, !=, <, >, <=, >=

// Logical
and, or, not (!)

// Ternary
condition ? true : false

// Elvis (null-safe default)
value ?: defaultValue

// Safe Navigation
object?.property  // null if object is null

// Regex
matches 'pattern'
```

### Literals
```java
'text'    // String
123       // Integer
3.14      // Double
true      // Boolean
null      // Null
```

### Collections
```java
// Selection (filtering)
.?[condition]     // All matching
.^[condition]     // First match
.$[condition]     // Last match

// Projection (mapping)
.![expression]    // Transform all

// Access
collection[index]
map['key']
```

### Types and Methods
```java
// Type reference
T(ClassName)
T(java.lang.Math)

// Static method
T(Math).sqrt(16)

// Instance method
object.method()
object.method(arg1, arg2)

// Constructor
new ClassName()
new java.util.Date()
```

### Variables
```java
#variableName      // Variable reference
#this              // Current iteration item
#root              // Root context object
```

---

## Configuration

### Enable SpEL
```java
@Configuration
public class SpelConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
```

### Using SpEL in Annotations
```java
// @Value
@Value("#{systemProperties['user.name']}")
private String user;

// @PreAuthorize
@PreAuthorize("hasRole('ADMIN')")
public void adminOnly() {}

@PreAuthorize("#user.name == authentication.name")
public void ownerOnly(User user) {}

// @Cacheable
@Cacheable(value = "users", key = "#id")
public User getUser(Long id) {}

// @ConditionalOnExpression
@ConditionalOnExpression("${feature.enabled:false}")
public class FeatureConfig {}
```

---

## Testing Examples

### Testing SpEL Expressions
```java
@Test
void testSpelExpression() {
    ExpressionParser parser = new SpelExpressionParser();
    StandardEvaluationContext context = new StandardEvaluationContext();
    
    context.setVariable("name", "John");
    
    Expression exp = parser.parseExpression("#name.toUpperCase()");
    String result = exp.getValue(context, String.class);
    
    assertEquals("JOHN", result);
}
```

### Testing with curl
```bash
# SpEL operations
curl http://localhost:8080/api/spel/property-access
curl http://localhost:8080/api/spel/math-operations
curl http://localhost:8080/api/spel/collection-filtering

# Property placeholders
curl http://localhost:8080/api/properties/basic
curl http://localhost:8080/api/properties/config

# Bean references
curl http://localhost:8080/api/bean-reference/component
curl http://localhost:8080/api/bean-reference/multiple

# Method invocation
curl http://localhost:8080/api/method-invocation/instance
curl http://localhost:8080/api/method-invocation/static

# Collection selection
curl http://localhost:8080/api/collection-selection/basic
curl http://localhost:8080/api/collection-selection/complex

# Collection projection
curl http://localhost:8080/api/collection-projection/basic
curl http://localhost:8080/api/collection-projection/nested

# Templates
curl http://localhost:8080/api/template/greeting
curl http://localhost:8080/api/template/order
```

---

## Best Practices

### 1. **Keep Expressions Simple**
- Avoid complex logic in expressions
- Use methods for business logic
- Keep expressions readable

### 2. **Handle Null Values**
- Use safe navigation operator (?.)
- Provide default values with Elvis (?:)
- Validate input data

### 3. **Performance Considerations**
- Cache compiled expressions
- Avoid expensive operations in expressions
- Consider alternatives for large collections

### 4. **Security**
- Never use user input directly in SpEL
- Validate and sanitize data
- Use @Value with caution in web apps
- Be aware of SpEL injection risks

### 5. **Type Safety**
- Specify expected types: getValue(context, Type.class)
- Handle ClassCastException
- Use proper type conversion

### 6. **Testing**
- Test all SpEL expressions
- Test edge cases (null, empty collections)
- Use MockMvc for integration tests

### 7. **Documentation**
- Document complex expressions
- Explain available variables
- Provide usage examples

### 8. **Maintenance**
- Extract complex expressions to methods
- Use constants for repeated expressions
- Version your expression templates

---

## Common Patterns

### Pattern 1: Configuration-Driven Features
```java
@ConditionalOnExpression("${feature.enabled:false}")
@Component
public class OptionalFeature {
    // Feature implementation
}
```

### Pattern 2: Dynamic Bean Selection
```java
@Value("#{${use.cache:false} ? @cacheService : @directService}")
private DataService dataService;
```

### Pattern 3: Filtering and Transformation
```java
@Value("#{@productService.getAllProducts().?[price > 100].![name]}")
private List<String> expensiveProductNames;
```

### Pattern 4: Template-Based Messages
```java
public String generateWelcome(User user) {
    String template = "Welcome #{#user.firstName}! " +
                     "You have #{#user.unreadMessages} unread messages.";
    return evaluateTemplate(template, user);
}
```

### Pattern 5: Security Expressions
```java
@PreAuthorize("hasRole('ADMIN') or #user.id == authentication.principal.id")
public void updateUser(User user) {
    // Update user
}
```

---

## Troubleshooting

### Issue 1: PropertyNotFoundException
**Problem**: Property not found in expression

**Solution**:
```java
// Check property exists
if (context.lookupVariable("propertyName") != null) {
    // Property exists
}

// Use safe navigation
"object?.property"
```

### Issue 2: EvaluationException
**Problem**: Error evaluating expression

**Solution**:
```java
try {
    Object result = expression.getValue(context);
} catch (EvaluationException e) {
    // Handle evaluation error
    log.error("SpEL evaluation failed", e);
}
```

### Issue 3: Type Mismatch
**Problem**: Returned type doesn't match expected

**Solution**:
```java
// Specify expected type
String result = expression.getValue(context, String.class);

// Or check type first
Object result = expression.getValue(context);
if (result instanceof String) {
    String str = (String) result;
}
```

---

## Dependencies

```xml
<!-- Spring Context (includes SpEL) -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
</dependency>

<!-- Spring Boot Starter (includes SpEL) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
```

---

## Additional Resources

- [Spring Expression Language (SpEL) Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions)
- [SpEL Language Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions-language-ref)
- [Spring @Value Annotation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-value-annotations)
- [Spring Boot Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

---

## Summary

Expression Language Patterns provide powerful mechanisms for:

✅ Dynamic property evaluation and configuration  
✅ Bean reference and dependency injection  
✅ Method invocation and chaining  
✅ Collection filtering and transformation  
✅ Template-based content generation  
✅ Security expressions  
✅ Conditional logic in configurations  

All patterns include comprehensive examples, testing strategies, and best practices for production use.
