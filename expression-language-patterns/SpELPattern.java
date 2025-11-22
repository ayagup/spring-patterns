package com.spring.patterns.expressionlanguage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * SpEL (Spring Expression Language) Pattern
 * 
 * Demonstrates comprehensive usage of Spring Expression Language for:
 * - Property access and manipulation
 * - Method invocation
 * - Mathematical and logical operations
 * - Collection manipulation
 * - Type operations
 * - Bean references
 * - Conditional expressions
 * 
 * SpEL is a powerful expression language that supports querying and
 * manipulating object graphs at runtime.
 */

// ===================== Domain Models =====================

record User(
    Long id,
    String username,
    String email,
    int age,
    boolean active,
    LocalDate registrationDate,
    List<String> roles
) {}

record Product(
    Long id,
    String name,
    double price,
    String category,
    int stock,
    boolean available
) {}

record Order(
    Long id,
    User user,
    List<Product> products,
    double totalAmount,
    String status
) {}

class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
    
    public int multiply(int a, int b) {
        return a * b;
    }
    
    public double calculate(String operation, double a, double b) {
        return switch (operation) {
            case "add" -> a + b;
            case "subtract" -> a - b;
            case "multiply" -> a * b;
            case "divide" -> b != 0 ? a / b : 0;
            default -> 0;
        };
    }
}

// ===================== SpEL Service =====================

@Service
class SpelService {
    
    private final ExpressionParser parser = new SpelExpressionParser();
    
    /**
     * Basic property access
     */
    public Object accessProperty() {
        User user = new User(1L, "john_doe", "john@example.com", 25, true, 
                           LocalDate.now(), List.of("USER", "ADMIN"));
        
        StandardEvaluationContext context = new StandardEvaluationContext(user);
        
        // Access properties
        Expression exp1 = parser.parseExpression("username");
        String username = exp1.getValue(context, String.class);
        
        Expression exp2 = parser.parseExpression("age");
        Integer age = exp2.getValue(context, Integer.class);
        
        Expression exp3 = parser.parseExpression("email");
        String email = exp3.getValue(context, String.class);
        
        return Map.of(
            "username", username,
            "age", age,
            "email", email
        );
    }
    
    /**
     * Nested property access
     */
    public Object accessNestedProperty() {
        User user = new User(1L, "john_doe", "john@example.com", 25, true,
                           LocalDate.now(), List.of("USER", "ADMIN"));
        Product product = new Product(1L, "Laptop", 999.99, "Electronics", 10, true);
        Order order = new Order(1L, user, List.of(product), 999.99, "CONFIRMED");
        
        StandardEvaluationContext context = new StandardEvaluationContext(order);
        
        // Nested property access
        Expression exp1 = parser.parseExpression("user.username");
        String username = exp1.getValue(context, String.class);
        
        Expression exp2 = parser.parseExpression("products[0].name");
        String productName = exp2.getValue(context, String.class);
        
        Expression exp3 = parser.parseExpression("user.roles[0]");
        String firstRole = exp3.getValue(context, String.class);
        
        return Map.of(
            "username", username,
            "productName", productName,
            "firstRole", firstRole
        );
    }
    
    /**
     * Mathematical operations
     */
    public Object performMathOperations() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // Basic arithmetic
        int sum = parser.parseExpression("10 + 5").getValue(Integer.class);
        int diff = parser.parseExpression("10 - 5").getValue(Integer.class);
        int product = parser.parseExpression("10 * 5").getValue(Integer.class);
        int quotient = parser.parseExpression("10 / 5").getValue(Integer.class);
        int modulo = parser.parseExpression("10 % 3").getValue(Integer.class);
        
        // Complex expressions
        int complex = parser.parseExpression("(10 + 5) * 2 - 3").getValue(Integer.class);
        
        // Power operation
        double power = parser.parseExpression("T(Math).pow(2, 3)").getValue(Double.class);
        
        return Map.of(
            "sum", sum,
            "difference", diff,
            "product", product,
            "quotient", quotient,
            "modulo", modulo,
            "complex", complex,
            "power", power
        );
    }
    
    /**
     * Logical operations
     */
    public Object performLogicalOperations() {
        User user = new User(1L, "john_doe", "john@example.com", 25, true,
                           LocalDate.now(), List.of("USER", "ADMIN"));
        
        StandardEvaluationContext context = new StandardEvaluationContext(user);
        
        // Comparison operators
        boolean isAdult = parser.parseExpression("age >= 18").getValue(context, Boolean.class);
        boolean isActive = parser.parseExpression("active == true").getValue(context, Boolean.class);
        
        // Logical operators
        boolean and = parser.parseExpression("age > 18 and active").getValue(context, Boolean.class);
        boolean or = parser.parseExpression("age < 18 or active").getValue(context, Boolean.class);
        boolean not = parser.parseExpression("!active").getValue(context, Boolean.class);
        
        return Map.of(
            "isAdult", isAdult,
            "isActive", isActive,
            "andResult", and,
            "orResult", or,
            "notResult", not
        );
    }
    
    /**
     * Ternary operator
     */
    public Object useTernaryOperator() {
        User user = new User(1L, "john_doe", "john@example.com", 15, true,
                           LocalDate.now(), List.of("USER"));
        
        StandardEvaluationContext context = new StandardEvaluationContext(user);
        
        // Ternary operator
        String ageCategory = parser.parseExpression("age >= 18 ? 'Adult' : 'Minor'")
                                   .getValue(context, String.class);
        
        String status = parser.parseExpression("active ? 'Active User' : 'Inactive User'")
                              .getValue(context, String.class);
        
        return Map.of(
            "ageCategory", ageCategory,
            "status", status
        );
    }
    
    /**
     * Method invocation
     */
    public Object invokeMethod() {
        Calculator calculator = new Calculator();
        StandardEvaluationContext context = new StandardEvaluationContext(calculator);
        
        // Invoke methods
        int sum = parser.parseExpression("add(10, 5)").getValue(context, Integer.class);
        int product = parser.parseExpression("multiply(10, 5)").getValue(context, Integer.class);
        double result = parser.parseExpression("calculate('multiply', 10.5, 2)")
                             .getValue(context, Double.class);
        
        return Map.of(
            "sum", sum,
            "product", product,
            "calculated", result
        );
    }
    
    /**
     * Collection operations
     */
    public Object performCollectionOperations() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("numbers", numbers);
        
        // Collection size
        int size = parser.parseExpression("#numbers.size()").getValue(context, Integer.class);
        
        // Collection access
        Integer first = parser.parseExpression("#numbers[0]").getValue(context, Integer.class);
        Integer last = parser.parseExpression("#numbers[#numbers.size()-1]")
                            .getValue(context, Integer.class);
        
        // Collection contains
        boolean contains = parser.parseExpression("#numbers.contains(3)")
                                .getValue(context, Boolean.class);
        
        return Map.of(
            "size", size,
            "first", first,
            "last", last,
            "contains", contains
        );
    }
    
    /**
     * Collection filtering (selection)
     */
    @SuppressWarnings("unchecked")
    public Object filterCollection() {
        List<Product> products = Arrays.asList(
            new Product(1L, "Laptop", 999.99, "Electronics", 10, true),
            new Product(2L, "Phone", 499.99, "Electronics", 20, true),
            new Product(3L, "Desk", 199.99, "Furniture", 5, true),
            new Product(4L, "Chair", 99.99, "Furniture", 0, false)
        );
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Filter: select products with price > 200
        List<Product> expensive = (List<Product>) parser
            .parseExpression("#products.?[price > 200]")
            .getValue(context);
        
        // Filter: select available products
        List<Product> available = (List<Product>) parser
            .parseExpression("#products.?[available == true]")
            .getValue(context);
        
        return Map.of(
            "expensiveProducts", expensive.size(),
            "availableProducts", available.size()
        );
    }
    
    /**
     * Collection projection (transformation)
     */
    @SuppressWarnings("unchecked")
    public Object projectCollection() {
        List<User> users = Arrays.asList(
            new User(1L, "john_doe", "john@example.com", 25, true, 
                   LocalDate.now(), List.of("USER")),
            new User(2L, "jane_doe", "jane@example.com", 30, true,
                   LocalDate.now(), List.of("ADMIN"))
        );
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("users", users);
        
        // Project: extract usernames
        List<String> usernames = (List<String>) parser
            .parseExpression("#users.![username]")
            .getValue(context);
        
        // Project: extract emails
        List<String> emails = (List<String>) parser
            .parseExpression("#users.![email]")
            .getValue(context);
        
        return Map.of(
            "usernames", usernames,
            "emails", emails
        );
    }
    
    /**
     * Type operations
     */
    public Object performTypeOperations() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // Static method call
        double random = parser.parseExpression("T(Math).random()").getValue(Double.class);
        double pi = parser.parseExpression("T(Math).PI").getValue(Double.class);
        
        // Type instantiation
        Date date = parser.parseExpression("new java.util.Date()").getValue(Date.class);
        
        // String methods
        String upper = parser.parseExpression("'hello'.toUpperCase()").getValue(String.class);
        int length = parser.parseExpression("'hello world'.length()").getValue(Integer.class);
        
        return Map.of(
            "pi", pi,
            "hasRandom", random >= 0,
            "dateCreated", date != null,
            "uppercase", upper,
            "length", length
        );
    }
    
    /**
     * Elvis operator (null-safe)
     */
    public Object useElvisOperator() {
        User user = new User(1L, "john_doe", null, 25, true,
                           LocalDate.now(), List.of("USER"));
        
        StandardEvaluationContext context = new StandardEvaluationContext(user);
        
        // Elvis operator - provides default value if null
        String email = parser.parseExpression("email ?: 'no-email@example.com'")
                            .getValue(context, String.class);
        
        return Map.of("email", email);
    }
    
    /**
     * Safe navigation operator
     */
    public Object useSafeNavigation() {
        Order order = new Order(1L, null, List.of(), 0.0, "PENDING");
        
        StandardEvaluationContext context = new StandardEvaluationContext(order);
        
        // Safe navigation - doesn't throw NullPointerException
        String username = parser.parseExpression("user?.username")
                               .getValue(context, String.class);
        
        return Map.of("username", username != null ? username : "null");
    }
}

// ===================== Configuration with SpEL =====================

@Configuration
class SpelConfiguration {
    
    @Value("#{systemProperties['user.name']}")
    private String systemUser;
    
    @Value("#{T(Math).random() * 100}")
    private double randomNumber;
    
    @Value("#{10 + 5}")
    private int calculatedValue;
    
    @Bean
    public Map<String, Object> spelConfigValues() {
        return Map.of(
            "systemUser", systemUser,
            "randomNumber", randomNumber,
            "calculatedValue", calculatedValue
        );
    }
}

// ===================== Component with SpEL =====================

@Component
class SpelComponent {
    
    // Property access
    @Value("#{systemProperties['java.version']}")
    private String javaVersion;
    
    // Mathematical expression
    @Value("#{10 * 2 + 5}")
    private int mathResult;
    
    // Ternary operator
    @Value("#{systemProperties['os.name'] matches '.*Windows.*' ? 'Windows' : 'Other'}")
    private String osType;
    
    // Collection expression
    @Value("#{T(java.util.Arrays).asList('spring', 'boot', 'spel')}")
    private List<String> frameworks;
    
    public Map<String, Object> getSpelValues() {
        return Map.of(
            "javaVersion", javaVersion,
            "mathResult", mathResult,
            "osType", osType,
            "frameworks", frameworks
        );
    }
}

// ===================== REST Controller =====================

@RestController
@RequestMapping("/api/spel")
class SpelController {
    
    private final SpelService spelService;
    private final SpelComponent spelComponent;
    
    public SpelController(SpelService spelService, SpelComponent spelComponent) {
        this.spelService = spelService;
        this.spelComponent = spelComponent;
    }
    
    @GetMapping("/property-access")
    public Object propertyAccess() {
        return spelService.accessProperty();
    }
    
    @GetMapping("/nested-property")
    public Object nestedProperty() {
        return spelService.accessNestedProperty();
    }
    
    @GetMapping("/math-operations")
    public Object mathOperations() {
        return spelService.performMathOperations();
    }
    
    @GetMapping("/logical-operations")
    public Object logicalOperations() {
        return spelService.performLogicalOperations();
    }
    
    @GetMapping("/ternary-operator")
    public Object ternaryOperator() {
        return spelService.useTernaryOperator();
    }
    
    @GetMapping("/method-invocation")
    public Object methodInvocation() {
        return spelService.invokeMethod();
    }
    
    @GetMapping("/collection-operations")
    public Object collectionOperations() {
        return spelService.performCollectionOperations();
    }
    
    @GetMapping("/collection-filtering")
    public Object collectionFiltering() {
        return spelService.filterCollection();
    }
    
    @GetMapping("/collection-projection")
    public Object collectionProjection() {
        return spelService.projectCollection();
    }
    
    @GetMapping("/type-operations")
    public Object typeOperations() {
        return spelService.performTypeOperations();
    }
    
    @GetMapping("/elvis-operator")
    public Object elvisOperator() {
        return spelService.useElvisOperator();
    }
    
    @GetMapping("/safe-navigation")
    public Object safeNavigation() {
        return spelService.useSafeNavigation();
    }
    
    @GetMapping("/component-values")
    public Object componentValues() {
        return spelComponent.getSpelValues();
    }
    
    /**
     * Dynamic expression evaluation
     */
    @PostMapping("/evaluate")
    public Map<String, Object> evaluateExpression(@RequestBody Map<String, String> request) {
        String expression = request.get("expression");
        ExpressionParser parser = new SpelExpressionParser();
        
        try {
            Object result = parser.parseExpression(expression).getValue();
            return Map.of(
                "success", true,
                "expression", expression,
                "result", result != null ? result : "null"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "expression", expression,
                "error", e.getMessage()
            );
        }
    }
}

/**
 * Key Concepts Demonstrated:
 * 
 * 1. SpEL Basics:
 *    - ExpressionParser and Expression interfaces
 *    - StandardEvaluationContext for context
 *    - getValue() methods with type parameters
 * 
 * 2. Property Access:
 *    - Direct property access: "username"
 *    - Nested property access: "user.username"
 *    - Array/List access: "roles[0]"
 * 
 * 3. Operators:
 *    - Arithmetic: +, -, *, /, %
 *    - Comparison: ==, !=, <, >, <=, >=
 *    - Logical: and, or, not (!)
 *    - Ternary: condition ? true : false
 *    - Elvis: value ?: default
 * 
 * 4. Method Invocation:
 *    - Instance methods: "add(10, 5)"
 *    - Static methods: "T(Math).random()"
 *    - String methods: "'hello'.toUpperCase()"
 * 
 * 5. Collection Operations:
 *    - Selection (filtering): .?[condition]
 *    - Projection (mapping): .![property]
 *    - First match: .^[condition]
 *    - Last match: .$[condition]
 * 
 * 6. Type Operations:
 *    - Static access: T(ClassName)
 *    - Object creation: new ClassName()
 *    - Type checking: instanceof
 * 
 * 7. Safe Navigation:
 *    - Null-safe operator: ?.
 *    - Prevents NullPointerException
 * 
 * 8. Variables:
 *    - Context variables: #variableName
 *    - Root object properties: propertyName
 * 
 * 9. @Value Annotation:
 *    - Inject SpEL expressions
 *    - Access system properties
 *    - Perform calculations
 * 
 * 10. Use Cases:
 *     - Dynamic property evaluation
 *     - Configuration expressions
 *     - Security expressions
 *     - Conditional bean creation
 *     - Data transformation
 *     - Validation rules
 * 
 * Testing Examples:
 * 
 * # Property access
 * curl http://localhost:8080/api/spel/property-access
 * 
 * # Math operations
 * curl http://localhost:8080/api/spel/math-operations
 * 
 * # Collection filtering
 * curl http://localhost:8080/api/spel/collection-filtering
 * 
 * # Dynamic evaluation
 * curl -X POST http://localhost:8080/api/spel/evaluate \
 *   -H "Content-Type: application/json" \
 *   -d '{"expression": "10 + 5 * 2"}'
 * 
 * SpEL Syntax Quick Reference:
 * 
 * - Literal expressions: 'text', 123, true, null
 * - Property: property, property.nestedProperty
 * - Array/List: array[index], list[0]
 * - Map: map['key'], map.key
 * - Method: method(), object.method(args)
 * - Operator: +, -, *, /, %, <, >, ==, !=, and, or, not
 * - Ternary: condition ? true : false
 * - Elvis: value ?: default
 * - Safe navigation: object?.property
 * - Type: T(java.lang.Math), T(ClassName)
 * - Variable: #variableName
 * - Collection selection: .?[condition]
 * - Collection projection: .![expression]
 * - Regex: matches 'pattern'
 */
