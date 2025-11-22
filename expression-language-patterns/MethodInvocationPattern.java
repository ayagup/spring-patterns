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

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Method Invocation Pattern
 * 
 * Demonstrates comprehensive usage of method invocation in SpEL:
 * - Instance method invocation
 * - Static method invocation
 * - Method chaining
 * - Method with parameters
 * - Overloaded methods
 * - Varargs methods
 * - Java standard library methods
 * - Custom class methods
 * - Reflection-based invocation
 */

// ===================== Domain Models =====================

record Employee(
    Long id,
    String firstName,
    String lastName,
    String email,
    String department,
    double salary,
    LocalDate hireDate
) {
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public int getYearsOfService() {
        return LocalDate.now().getYear() - hireDate.getYear();
    }
    
    public double getAnnualBonus() {
        return salary * 0.10;
    }
    
    public double calculateTax(double rate) {
        return salary * rate;
    }
}

record Account(
    String accountNumber,
    String accountType,
    double balance,
    String currency
) {
    public double convertCurrency(String targetCurrency, double exchangeRate) {
        if (currency.equals(targetCurrency)) {
            return balance;
        }
        return balance * exchangeRate;
    }
    
    public double applyInterest(double rate, int years) {
        return balance * Math.pow(1 + rate, years);
    }
}

// ===================== Utility Classes =====================

class StringUtils {
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    public static String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }
    
    public static int countWords(String str) {
        return str.trim().split("\\s+").length;
    }
    
    public static String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
}

class MathUtils {
    public static double calculatePercentage(double value, double percent) {
        return value * (percent / 100);
    }
    
    public static double roundToDecimal(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }
    
    public static boolean isPrime(int number) {
        if (number <= 1) return false;
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) return false;
        }
        return true;
    }
    
    public static int factorial(int n) {
        if (n <= 1) return 1;
        return n * factorial(n - 1);
    }
}

class DateUtils {
    public static String formatDate(LocalDate date, String pattern) {
        return date.toString(); // Simplified
    }
    
    public static boolean isWeekend(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7;
    }
    
    public static long daysBetween(LocalDate start, LocalDate end) {
        return end.toEpochDay() - start.toEpochDay();
    }
}

// ===================== Service with Methods =====================

@Service
class EmployeeService {
    
    private final List<Employee> employees = List.of(
        new Employee(1L, "John", "Doe", "john@example.com", "IT", 75000.0, LocalDate.of(2020, 1, 15)),
        new Employee(2L, "Jane", "Smith", "jane@example.com", "HR", 65000.0, LocalDate.of(2019, 3, 20)),
        new Employee(3L, "Bob", "Johnson", "bob@example.com", "IT", 80000.0, LocalDate.of(2018, 6, 10))
    );
    
    public List<Employee> getAllEmployees() {
        return employees;
    }
    
    public Employee findById(Long id) {
        return employees.stream()
            .filter(e -> e.id().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Employee> findByDepartment(String department) {
        return employees.stream()
            .filter(e -> e.department().equals(department))
            .collect(Collectors.toList());
    }
    
    public double calculateAverageSalary() {
        return employees.stream()
            .mapToDouble(Employee::salary)
            .average()
            .orElse(0.0);
    }
    
    public double calculateTotalSalary(String department) {
        return employees.stream()
            .filter(e -> e.department().equals(department))
            .mapToDouble(Employee::salary)
            .sum();
    }
    
    public List<String> getFullNames() {
        return employees.stream()
            .map(Employee::getFullName)
            .collect(Collectors.toList());
    }
    
    public Map<String, Long> countByDepartment() {
        return employees.stream()
            .collect(Collectors.groupingBy(Employee::department, Collectors.counting()));
    }
}

// ===================== Method Invocation Service =====================

@Service
class MethodInvocationService {
    
    private final ExpressionParser parser = new SpelExpressionParser();
    
    /**
     * Invoke instance methods
     */
    public Map<String, Object> invokeInstanceMethods() {
        Employee emp = new Employee(1L, "John", "Doe", "john@example.com", 
                                   "IT", 75000.0, LocalDate.of(2020, 1, 15));
        
        StandardEvaluationContext context = new StandardEvaluationContext(emp);
        
        // Call methods without parameters
        String fullName = parser.parseExpression("getFullName()").getValue(context, String.class);
        int yearsOfService = parser.parseExpression("getYearsOfService()").getValue(context, Integer.class);
        double bonus = parser.parseExpression("getAnnualBonus()").getValue(context, Double.class);
        
        // Call method with parameter
        double tax = parser.parseExpression("calculateTax(0.25)").getValue(context, Double.class);
        
        return Map.of(
            "fullName", fullName,
            "yearsOfService", yearsOfService,
            "bonus", bonus,
            "tax", tax
        );
    }
    
    /**
     * Invoke static methods
     */
    public Map<String, Object> invokeStaticMethods() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // Math static methods
        double sqrt = parser.parseExpression("T(Math).sqrt(16)").getValue(Double.class);
        double power = parser.parseExpression("T(Math).pow(2, 8)").getValue(Double.class);
        double max = parser.parseExpression("T(Math).max(10, 20)").getValue(Double.class);
        int abs = parser.parseExpression("T(Math).abs(-42)").getValue(Integer.class);
        
        // String static methods
        String valueOf = parser.parseExpression("T(String).valueOf(123)").getValue(String.class);
        
        // Custom static methods
        String capitalized = parser.parseExpression("T(com.spring.patterns.expressionlanguage.StringUtils).capitalize('hello')")
                                   .getValue(String.class);
        double percentage = parser.parseExpression("T(com.spring.patterns.expressionlanguage.MathUtils).calculatePercentage(1000, 15)")
                                  .getValue(Double.class);
        
        return Map.of(
            "sqrt", sqrt,
            "power", power,
            "max", max,
            "abs", abs,
            "valueOf", valueOf,
            "capitalized", capitalized,
            "percentage", percentage
        );
    }
    
    /**
     * Method chaining
     */
    public Map<String, Object> invokeChainedMethods() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // String method chaining
        String result1 = parser.parseExpression("'hello world'.toUpperCase().substring(0, 5)")
                              .getValue(String.class);
        
        String result2 = parser.parseExpression("'  spring boot  '.trim().replace(' ', '-')")
                              .getValue(String.class);
        
        int length = parser.parseExpression("'test'.concat(' string').length()")
                          .getValue(Integer.class);
        
        // Array/List method chaining
        context.setVariable("numbers", Arrays.asList(1, 2, 3, 4, 5));
        int size = parser.parseExpression("#numbers.stream().filter(n -> n > 2).count()")
                        .getValue(context, Integer.class);
        
        return Map.of(
            "upperSubstring", result1,
            "trimReplace", result2,
            "concatLength", length,
            "filteredCount", size
        );
    }
    
    /**
     * Invoke methods on collections
     */
    public Map<String, Object> invokeCollectionMethods() {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("names", names);
        
        // Collection methods
        int size = parser.parseExpression("#names.size()").getValue(context, Integer.class);
        boolean isEmpty = parser.parseExpression("#names.isEmpty()").getValue(context, Boolean.class);
        boolean contains = parser.parseExpression("#names.contains('Bob')").getValue(context, Boolean.class);
        String first = parser.parseExpression("#names.get(0)").getValue(context, String.class);
        
        // Stream operations
        String joined = parser.parseExpression("#names.stream().collect(T(java.util.stream.Collectors).joining(', '))")
                             .getValue(context, String.class);
        
        return Map.of(
            "size", size,
            "isEmpty", isEmpty,
            "contains", contains,
            "first", first,
            "joined", joined
        );
    }
    
    /**
     * Invoke varargs methods
     */
    public Map<String, Object> invokeVarargsMethods() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // String.format (varargs)
        String formatted = parser.parseExpression("T(String).format('%s is %d years old', 'John', 30)")
                                .getValue(String.class);
        
        // Math.max with multiple values
        int maxValue = parser.parseExpression("T(Math).max(T(Math).max(10, 20), 30)")
                            .getValue(Integer.class);
        
        return Map.of(
            "formatted", formatted,
            "maxValue", maxValue
        );
    }
    
    /**
     * Invoke overloaded methods
     */
    public Map<String, Object> invokeOverloadedMethods() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // String.valueOf overloaded methods
        String intValue = parser.parseExpression("T(String).valueOf(123)").getValue(String.class);
        String boolValue = parser.parseExpression("T(String).valueOf(true)").getValue(String.class);
        String doubleValue = parser.parseExpression("T(String).valueOf(3.14)").getValue(String.class);
        
        // Math.abs overloaded methods
        int absInt = parser.parseExpression("T(Math).abs(-42)").getValue(Integer.class);
        double absDouble = parser.parseExpression("T(Math).abs(-3.14)").getValue(Double.class);
        
        return Map.of(
            "intValue", intValue,
            "boolValue", boolValue,
            "doubleValue", doubleValue,
            "absInt", absInt,
            "absDouble", absDouble
        );
    }
    
    /**
     * Dynamic method invocation
     */
    public Object invokeDynamicMethod(String className, String methodName, Object... args) {
        try {
            Class<?> clazz = Class.forName(className);
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            StringBuilder expression = new StringBuilder("T(").append(className).append(").").append(methodName).append("(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) expression.append(", ");
                if (args[i] instanceof String) {
                    expression.append("'").append(args[i]).append("'");
                } else {
                    expression.append(args[i]);
                }
            }
            expression.append(")");
            
            return parser.parseExpression(expression.toString()).getValue(context);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

// ===================== Component with Method Invocations =====================

@Component
class MethodInvocationComponent {
    
    // String methods
    @Value("#{'hello world'.toUpperCase()}")
    private String upperCaseText;
    
    @Value("#{'SPRING BOOT'.toLowerCase()}")
    private String lowerCaseText;
    
    @Value("#{'test'.length()}")
    private int stringLength;
    
    // Math methods
    @Value("#{T(Math).PI}")
    private double pi;
    
    @Value("#{T(Math).sqrt(144)}")
    private double squareRoot;
    
    @Value("#{T(Math).ceil(3.14)}")
    private double ceiling;
    
    @Value("#{T(Math).floor(3.14)}")
    private double floor;
    
    // Custom static methods
    @Value("#{T(com.spring.patterns.expressionlanguage.StringUtils).capitalize('spring')}")
    private String capitalizedString;
    
    @Value("#{T(com.spring.patterns.expressionlanguage.MathUtils).calculatePercentage(500, 20)}")
    private double calculatedPercentage;
    
    // Method chaining
    @Value("#{'  trim me  '.trim().toUpperCase()}")
    private String chainedMethods;
    
    public Map<String, Object> getMethodInvocations() {
        return Map.of(
            "upperCaseText", upperCaseText,
            "lowerCaseText", lowerCaseText,
            "stringLength", stringLength,
            "pi", pi,
            "squareRoot", squareRoot,
            "ceiling", ceiling,
            "floor", floor,
            "capitalizedString", capitalizedString,
            "calculatedPercentage", calculatedPercentage,
            "chainedMethods", chainedMethods
        );
    }
}

// ===================== Configuration with Method Invocations =====================

@Configuration
class MethodInvocationConfiguration {
    
    @Bean
    public Map<String, Object> systemInfo() {
        return Map.of(
            "javaVersion", System.getProperty("java.version"),
            "osName", System.getProperty("os.name"),
            "userHome", System.getProperty("user.home"),
            "currentTime", System.currentTimeMillis()
        );
    }
}

// ===================== REST Controller =====================

@RestController
@RequestMapping("/api/method-invocation")
class MethodInvocationController {
    
    private final MethodInvocationService methodService;
    private final MethodInvocationComponent methodComponent;
    
    public MethodInvocationController(
        MethodInvocationService methodService,
        MethodInvocationComponent methodComponent
    ) {
        this.methodService = methodService;
        this.methodComponent = methodComponent;
    }
    
    @GetMapping("/instance")
    public Map<String, Object> instanceMethods() {
        return methodService.invokeInstanceMethods();
    }
    
    @GetMapping("/static")
    public Map<String, Object> staticMethods() {
        return methodService.invokeStaticMethods();
    }
    
    @GetMapping("/chained")
    public Map<String, Object> chainedMethods() {
        return methodService.invokeChainedMethods();
    }
    
    @GetMapping("/collection")
    public Map<String, Object> collectionMethods() {
        return methodService.invokeCollectionMethods();
    }
    
    @GetMapping("/varargs")
    public Map<String, Object> varargsMethods() {
        return methodService.invokeVarargsMethods();
    }
    
    @GetMapping("/overloaded")
    public Map<String, Object> overloadedMethods() {
        return methodService.invokeOverloadedMethods();
    }
    
    @GetMapping("/component")
    public Map<String, Object> componentMethods() {
        return methodComponent.getMethodInvocations();
    }
    
    @PostMapping("/dynamic")
    public Map<String, Object> dynamicMethodInvocation(@RequestBody Map<String, Object> request) {
        String className = (String) request.get("className");
        String methodName = (String) request.get("methodName");
        List<?> args = (List<?>) request.getOrDefault("args", List.of());
        
        Object result = methodService.invokeDynamicMethod(className, methodName, args.toArray());
        
        return Map.of(
            "className", className,
            "methodName", methodName,
            "args", args,
            "result", result
        );
    }
}

/**
 * Key Concepts Demonstrated:
 * 
 * 1. Instance Method Invocation:
 *    - object.method() - No parameters
 *    - object.method(arg) - With parameters
 *    - object.method(arg1, arg2) - Multiple parameters
 * 
 * 2. Static Method Invocation:
 *    - T(ClassName).method()
 *    - T(java.lang.Math).sqrt(16)
 *    - T(java.lang.String).valueOf(123)
 * 
 * 3. Method Chaining:
 *    - object.method1().method2()
 *    - 'hello'.toUpperCase().substring(0, 3)
 *    - Multiple method calls in sequence
 * 
 * 4. Built-in Methods:
 *    - String: toUpperCase(), toLowerCase(), substring(), etc.
 *    - Math: sqrt(), pow(), max(), min(), etc.
 *    - Collection: size(), isEmpty(), contains(), etc.
 * 
 * 5. Custom Static Methods:
 *    - T(CustomClass).staticMethod()
 *    - Full package path required
 * 
 * 6. Varargs Methods:
 *    - Methods accepting variable arguments
 *    - String.format()
 * 
 * 7. Overloaded Methods:
 *    - SpEL resolves based on argument types
 *    - Math.abs(int) vs Math.abs(double)
 * 
 * 8. Collection Methods:
 *    - List/Set/Map methods
 *    - Stream operations
 *    - Collectors
 * 
 * 9. @Value with Methods:
 *    - Inject method results
 *    - Static method calls
 *    - Method chains
 * 
 * 10. Use Cases:
 *     - Data transformation
 *     - Calculations
 *     - String manipulation
 *     - Collection processing
 *     - Dynamic method execution
 * 
 * Testing Examples:
 * 
 * # Instance methods
 * curl http://localhost:8080/api/method-invocation/instance
 * 
 * # Static methods
 * curl http://localhost:8080/api/method-invocation/static
 * 
 * # Chained methods
 * curl http://localhost:8080/api/method-invocation/chained
 * 
 * # Collection methods
 * curl http://localhost:8080/api/method-invocation/collection
 * 
 * # Dynamic invocation
 * curl -X POST http://localhost:8080/api/method-invocation/dynamic \
 *   -H "Content-Type: application/json" \
 *   -d '{"className":"java.lang.Math","methodName":"sqrt","args":[144]}'
 * 
 * Common Methods in SpEL:
 * 
 * String:
 * - length(), substring(), toUpperCase(), toLowerCase()
 * - trim(), concat(), replace(), split()
 * - startsWith(), endsWith(), contains()
 * 
 * Math:
 * - sqrt(), pow(), abs(), max(), min()
 * - ceil(), floor(), round()
 * - random(), PI, E
 * 
 * Collection:
 * - size(), isEmpty(), contains()
 * - get(), add(), remove()
 * - stream(), filter(), map()
 * 
 * Best Practices:
 * 
 * 1. Use static imports for frequently used methods
 * 2. Prefer type-safe method calls over reflection
 * 3. Handle null values appropriately
 * 4. Use method chaining for readability
 * 5. Document complex method invocations
 * 6. Test method calls with edge cases
 * 7. Use meaningful variable names in SpEL
 */
