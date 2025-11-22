package com.spring.patterns.expressionlanguage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Collection Selection Pattern
 * 
 * Demonstrates comprehensive usage of collection filtering/selection in SpEL:
 * - Selection operator: .?[selectionExpression]
 * - First match: .^[selectionExpression]
 * - Last match: .$[selectionExpression]
 * - Filter by property
 * - Filter by method result
 * - Complex filter expressions
 * - Multiple filter criteria
 * - Nested collection filtering
 */

// ===================== Domain Models =====================

record Product(
    Long id,
    String name,
    double price,
    String category,
    int stock,
    boolean available,
    double rating,
    List<String> tags
) {
    public boolean isExpensive() {
        return price > 500;
    }
    
    public boolean isInStock() {
        return stock > 0 && available;
    }
    
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
}

record Customer(
    Long id,
    String name,
    String email,
    String tier,
    int age,
    boolean active,
    double totalSpent,
    List<String> interests
) {
    public boolean isPremium() {
        return tier.equals("GOLD") || tier.equals("PLATINUM");
    }
    
    public boolean isAdult() {
        return age >= 18;
    }
    
    public boolean hasInterest(String interest) {
        return interests.contains(interest);
    }
}

record Order(
    Long id,
    Customer customer,
    List<Product> products,
    double totalAmount,
    String status,
    LocalDate orderDate,
    boolean isPaid
) {
    public boolean isCompleted() {
        return status.equals("DELIVERED") && isPaid;
    }
    
    public boolean isRecent() {
        return orderDate.isAfter(LocalDate.now().minusDays(30));
    }
}

record Employee(
    Long id,
    String name,
    String department,
    String position,
    double salary,
    int yearsOfExperience,
    List<String> skills
) {
    public boolean isSenior() {
        return yearsOfExperience > 5;
    }
    
    public boolean hasSkill(String skill) {
        return skills.contains(skill);
    }
}

// ===================== Data Service =====================

@Service
class DataService {
    
    public List<Product> getProducts() {
        return List.of(
            new Product(1L, "Laptop", 999.99, "Electronics", 10, true, 4.5, List.of("tech", "portable")),
            new Product(2L, "Phone", 599.99, "Electronics", 25, true, 4.8, List.of("tech", "mobile")),
            new Product(3L, "Desk", 299.99, "Furniture", 5, true, 4.2, List.of("office", "workspace")),
            new Product(4L, "Chair", 149.99, "Furniture", 0, false, 4.0, List.of("office", "seating")),
            new Product(5L, "Monitor", 399.99, "Electronics", 15, true, 4.6, List.of("tech", "display")),
            new Product(6L, "Keyboard", 89.99, "Electronics", 50, true, 4.4, List.of("tech", "input"))
        );
    }
    
    public List<Customer> getCustomers() {
        return List.of(
            new Customer(1L, "John Doe", "john@example.com", "GOLD", 35, true, 5000.0, List.of("tech", "gaming")),
            new Customer(2L, "Jane Smith", "jane@example.com", "SILVER", 28, true, 2500.0, List.of("fashion", "books")),
            new Customer(3L, "Bob Johnson", "bob@example.com", "BRONZE", 42, true, 1000.0, List.of("sports", "outdoor")),
            new Customer(4L, "Alice Brown", "alice@example.com", "PLATINUM", 50, true, 10000.0, List.of("luxury", "travel")),
            new Customer(5L, "Charlie Wilson", "charlie@example.com", "SILVER", 17, false, 500.0, List.of("gaming", "tech"))
        );
    }
    
    public List<Employee> getEmployees() {
        return List.of(
            new Employee(1L, "John Doe", "IT", "Senior Developer", 95000.0, 8, List.of("Java", "Spring", "AWS")),
            new Employee(2L, "Jane Smith", "IT", "Developer", 75000.0, 3, List.of("Java", "React", "MongoDB")),
            new Employee(3L, "Bob Johnson", "HR", "Manager", 85000.0, 10, List.of("Recruitment", "Training")),
            new Employee(4L, "Alice Brown", "IT", "Architect", 120000.0, 12, List.of("Java", "Microservices", "Kubernetes")),
            new Employee(5L, "Charlie Wilson", "Sales", "Representative", 60000.0, 2, List.of("Communication", "CRM"))
        );
    }
}

// ===================== Collection Selection Service =====================

@Service
class CollectionSelectionService {
    
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DataService dataService;
    
    public CollectionSelectionService(DataService dataService) {
        this.dataService = dataService;
    }
    
    /**
     * Basic selection - filter all matching elements
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> basicSelection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Select products with price > 300
        List<Product> expensive = (List<Product>) parser
            .parseExpression("#products.?[price > 300]")
            .getValue(context);
        
        // Select available products
        List<Product> available = (List<Product>) parser
            .parseExpression("#products.?[available == true]")
            .getValue(context);
        
        // Select products in Electronics category
        List<Product> electronics = (List<Product>) parser
            .parseExpression("#products.?[category == 'Electronics']")
            .getValue(context);
        
        return Map.of(
            "expensiveCount", expensive.size(),
            "availableCount", available.size(),
            "electronicsCount", electronics.size(),
            "expensiveProducts", expensive.stream().map(Product::name).toList()
        );
    }
    
    /**
     * First match selection (.^[])
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> firstMatchSelection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // First product with price > 300
        Product firstExpensive = (Product) parser
            .parseExpression("#products.^[price > 300]")
            .getValue(context);
        
        // First available product
        Product firstAvailable = (Product) parser
            .parseExpression("#products.^[available == true]")
            .getValue(context);
        
        return Map.of(
            "firstExpensive", firstExpensive != null ? firstExpensive.name() : "none",
            "firstAvailable", firstAvailable != null ? firstAvailable.name() : "none"
        );
    }
    
    /**
     * Last match selection (.$[])
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> lastMatchSelection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Last product with price > 300
        Product lastExpensive = (Product) parser
            .parseExpression("#products.$[price > 300]")
            .getValue(context);
        
        // Last Electronics product
        Product lastElectronics = (Product) parser
            .parseExpression("#products.$[category == 'Electronics']")
            .getValue(context);
        
        return Map.of(
            "lastExpensive", lastExpensive != null ? lastExpensive.name() : "none",
            "lastElectronics", lastElectronics != null ? lastElectronics.name() : "none"
        );
    }
    
    /**
     * Complex filter expressions with multiple criteria
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> complexSelection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Products that are expensive AND available
        List<Product> expensiveAndAvailable = (List<Product>) parser
            .parseExpression("#products.?[price > 300 and available == true]")
            .getValue(context);
        
        // Products with high rating OR in stock
        List<Product> highRatedOrInStock = (List<Product>) parser
            .parseExpression("#products.?[rating >= 4.5 or stock > 20]")
            .getValue(context);
        
        // Products NOT in Furniture category
        List<Product> notFurniture = (List<Product>) parser
            .parseExpression("#products.?[category != 'Furniture']")
            .getValue(context);
        
        return Map.of(
            "expensiveAndAvailable", expensiveAndAvailable.size(),
            "highRatedOrInStock", highRatedOrInStock.size(),
            "notFurniture", notFurniture.size()
        );
    }
    
    /**
     * Selection by method result
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> methodBasedSelection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Select using method calls
        List<Product> expensive = (List<Product>) parser
            .parseExpression("#products.?[isExpensive()]")
            .getValue(context);
        
        List<Product> inStock = (List<Product>) parser
            .parseExpression("#products.?[isInStock()]")
            .getValue(context);
        
        List<Product> withTechTag = (List<Product>) parser
            .parseExpression("#products.?[hasTag('tech')]")
            .getValue(context);
        
        return Map.of(
            "expensiveCount", expensive.size(),
            "inStockCount", inStock.size(),
            "techProductsCount", withTechTag.size()
        );
    }
    
    /**
     * Selection on different collection types
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> customerSelection() {
        List<Customer> customers = dataService.getCustomers();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("customers", customers);
        
        // Select premium customers
        List<Customer> premium = (List<Customer>) parser
            .parseExpression("#customers.?[isPremium()]")
            .getValue(context);
        
        // Select active adult customers
        List<Customer> activeAdults = (List<Customer>) parser
            .parseExpression("#customers.?[isAdult() and active == true]")
            .getValue(context);
        
        // Select customers who spent more than 2000
        List<Customer> highSpenders = (List<Customer>) parser
            .parseExpression("#customers.?[totalSpent > 2000]")
            .getValue(context);
        
        // Select customers with tech interest
        List<Customer> techInterest = (List<Customer>) parser
            .parseExpression("#customers.?[hasInterest('tech')]")
            .getValue(context);
        
        return Map.of(
            "premiumCount", premium.size(),
            "activeAdultsCount", activeAdults.size(),
            "highSpendersCount", highSpenders.size(),
            "techInterestCount", techInterest.size()
        );
    }
    
    /**
     * Employee selection examples
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> employeeSelection() {
        List<Employee> employees = dataService.getEmployees();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("employees", employees);
        
        // Select IT department employees
        List<Employee> itEmployees = (List<Employee>) parser
            .parseExpression("#employees.?[department == 'IT']")
            .getValue(context);
        
        // Select senior employees
        List<Employee> seniors = (List<Employee>) parser
            .parseExpression("#employees.?[isSenior()]")
            .getValue(context);
        
        // Select employees with Java skill
        List<Employee> javaDevs = (List<Employee>) parser
            .parseExpression("#employees.?[hasSkill('Java')]")
            .getValue(context);
        
        // Select high earners
        List<Employee> highEarners = (List<Employee>) parser
            .parseExpression("#employees.?[salary >= 90000]")
            .getValue(context);
        
        return Map.of(
            "itEmployees", itEmployees.size(),
            "seniors", seniors.size(),
            "javaDevs", javaDevs.size(),
            "highEarners", highEarners.size(),
            "javaDevNames", javaDevs.stream().map(Employee::name).toList()
        );
    }
    
    /**
     * Range-based selection
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> rangeSelection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Select products in price range
        List<Product> midRange = (List<Product>) parser
            .parseExpression("#products.?[price >= 200 and price <= 600]")
            .getValue(context);
        
        // Select products with moderate stock
        List<Product> moderateStock = (List<Product>) parser
            .parseExpression("#products.?[stock >= 5 and stock <= 20]")
            .getValue(context);
        
        return Map.of(
            "midRangeCount", midRange.size(),
            "moderateStockCount", moderateStock.size()
        );
    }
    
    /**
     * String matching selection
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> stringMatchingSelection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Select products with name starting with 'L'
        List<Product> startsWithL = (List<Product>) parser
            .parseExpression("#products.?[name.startsWith('L')]")
            .getValue(context);
        
        // Select products with name containing 'o'
        List<Product> containsO = (List<Product>) parser
            .parseExpression("#products.?[name.contains('o')]")
            .getValue(context);
        
        // Select products with name length > 5
        List<Product> longNames = (List<Product>) parser
            .parseExpression("#products.?[name.length() > 5]")
            .getValue(context);
        
        return Map.of(
            "startsWithL", startsWithL.size(),
            "containsO", containsO.size(),
            "longNames", longNames.size()
        );
    }
}

// ===================== Component with Collection Selection =====================

@Component
class CollectionSelectionComponent {
    
    // This would typically reference a bean that provides the collection
    @Value("#{T(java.util.Arrays).asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).?[#this > 5]}")
    private List<Integer> greaterThanFive;
    
    @Value("#{T(java.util.Arrays).asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).?[#this % 2 == 0]}")
    private List<Integer> evenNumbers;
    
    @Value("#{T(java.util.Arrays).asList('apple', 'banana', 'cherry', 'date').?[#this.length() > 5]}")
    private List<String> longFruits;
    
    public Map<String, Object> getSelections() {
        return Map.of(
            "greaterThanFive", greaterThanFive,
            "evenNumbers", evenNumbers,
            "longFruits", longFruits
        );
    }
}

// ===================== REST Controller =====================

@RestController
@RequestMapping("/api/collection-selection")
class CollectionSelectionController {
    
    private final CollectionSelectionService selectionService;
    private final CollectionSelectionComponent selectionComponent;
    
    public CollectionSelectionController(
        CollectionSelectionService selectionService,
        CollectionSelectionComponent selectionComponent
    ) {
        this.selectionService = selectionService;
        this.selectionComponent = selectionComponent;
    }
    
    @GetMapping("/basic")
    public Map<String, Object> basicSelection() {
        return selectionService.basicSelection();
    }
    
    @GetMapping("/first")
    public Map<String, Object> firstMatch() {
        return selectionService.firstMatchSelection();
    }
    
    @GetMapping("/last")
    public Map<String, Object> lastMatch() {
        return selectionService.lastMatchSelection();
    }
    
    @GetMapping("/complex")
    public Map<String, Object> complexSelection() {
        return selectionService.complexSelection();
    }
    
    @GetMapping("/method-based")
    public Map<String, Object> methodBased() {
        return selectionService.methodBasedSelection();
    }
    
    @GetMapping("/customers")
    public Map<String, Object> customerSelection() {
        return selectionService.customerSelection();
    }
    
    @GetMapping("/employees")
    public Map<String, Object> employeeSelection() {
        return selectionService.employeeSelection();
    }
    
    @GetMapping("/range")
    public Map<String, Object> rangeSelection() {
        return selectionService.rangeSelection();
    }
    
    @GetMapping("/string-matching")
    public Map<String, Object> stringMatching() {
        return selectionService.stringMatchingSelection();
    }
    
    @GetMapping("/component")
    public Map<String, Object> componentSelections() {
        return selectionComponent.getSelections();
    }
}

/**
 * Key Concepts Demonstrated:
 * 
 * 1. Selection Operator (.?[]):
 *    - Filters all matching elements
 *    - Returns List of matching items
 *    - Syntax: collection.?[condition]
 * 
 * 2. First Match (.^[]):
 *    - Returns first matching element
 *    - Returns single item or null
 *    - Syntax: collection.^[condition]
 * 
 * 3. Last Match (.$[]):
 *    - Returns last matching element
 *    - Returns single item or null
 *    - Syntax: collection.$[condition]
 * 
 * 4. Filter Conditions:
 *    - Property comparison: price > 100
 *    - Boolean properties: available == true
 *    - String operations: name.startsWith('A')
 *    - Method calls: isExpensive()
 *    - Range checks: price >= 100 and price <= 500
 * 
 * 5. Operators in Filters:
 *    - Comparison: ==, !=, <, >, <=, >=
 *    - Logical: and, or, not (!)
 *    - String: matches, startsWith, contains
 * 
 * 6. Complex Filters:
 *    - Multiple conditions with and/or
 *    - Nested property access
 *    - Method invocation results
 *    - Type operations
 * 
 * 7. #this Variable:
 *    - Refers to current item in iteration
 *    - Used for primitive collections
 *    - Example: #this > 5
 * 
 * 8. Use Cases:
 *    - Filter products by criteria
 *    - Find qualified customers
 *    - Select employees by skills
 *    - Data filtering/querying
 *    - Search implementations
 * 
 * Testing Examples:
 * 
 * curl http://localhost:8080/api/collection-selection/basic
 * curl http://localhost:8080/api/collection-selection/first
 * curl http://localhost:8080/api/collection-selection/last
 * curl http://localhost:8080/api/collection-selection/complex
 * curl http://localhost:8080/api/collection-selection/customers
 * curl http://localhost:8080/api/collection-selection/employees
 * 
 * SpEL Selection Examples:
 * 
 * # All matching
 * #products.?[price > 500]
 * #customers.?[tier == 'GOLD']
 * #employees.?[department == 'IT' and salary > 80000]
 * 
 * # First matching
 * #products.^[price > 500]
 * #customers.^[tier == 'GOLD']
 * 
 * # Last matching
 * #products.$[price > 500]
 * #customers.$[tier == 'GOLD']
 * 
 * # Complex conditions
 * #products.?[price > 100 and available == true and stock > 0]
 * #customers.?[isAdult() and isPremium() and totalSpent > 1000]
 * #employees.?[hasSkill('Java') and yearsOfExperience > 5]
 * 
 * Best Practices:
 * 
 * 1. Keep filter expressions simple and readable
 * 2. Use method calls for complex business logic
 * 3. Consider performance for large collections
 * 4. Handle null values appropriately
 * 5. Use meaningful variable names
 * 6. Test edge cases (empty collections, no matches)
 * 7. Document complex filter criteria
 * 8. Combine with projection for efficiency
 */
