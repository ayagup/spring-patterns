package com.spring.patterns.expressionlanguage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Collection Projection Pattern
 * 
 * Demonstrates comprehensive usage of collection transformation/projection in SpEL:
 * - Projection operator: .![projectionExpression]
 * - Extract single property from all elements
 * - Transform collection elements
 * - Create new objects from projections
 * - Nested projections
 * - Combining selection and projection
 * - Map projections
 */

// ===================== Domain Models =====================

record Product(
    Long id,
    String name,
    double price,
    String category,
    int stock,
    String brand,
    List<String> features
) {
    public String getDisplayName() {
        return brand + " " + name;
    }
    
    public double getPriceWithTax() {
        return price * 1.2;
    }
}

record Customer(
    Long id,
    String firstName,
    String lastName,
    String email,
    String tier,
    Address address,
    List<String> phoneNumbers
) {
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    record Address(String street, String city, String state, String zipCode) {
        public String getFullAddress() {
            return street + ", " + city + ", " + state + " " + zipCode;
        }
    }
}

record Employee(
    Long id,
    String name,
    String department,
    double salary,
    List<Skill> skills,
    ContactInfo contactInfo
) {
    record Skill(String name, int level) {}
    record ContactInfo(String email, String phone) {}
    
    public double getAnnualSalary() {
        return salary * 12;
    }
}

record Order(
    Long id,
    Customer customer,
    List<OrderItem> items,
    LocalDate orderDate,
    String status
) {
    record OrderItem(Product product, int quantity, double price) {
        public double getSubtotal() {
            return quantity * price;
        }
    }
    
    public double getTotal() {
        return items.stream()
            .mapToDouble(OrderItem::getSubtotal)
            .sum();
    }
}

// ===================== Data Service =====================

@Service
class ProjectionDataService {
    
    public List<Product> getProducts() {
        return List.of(
            new Product(1L, "Laptop", 999.99, "Electronics", 10, "Dell", List.of("Intel i7", "16GB RAM", "512GB SSD")),
            new Product(2L, "Phone", 599.99, "Electronics", 25, "Samsung", List.of("5G", "128GB", "AMOLED")),
            new Product(3L, "Desk", 299.99, "Furniture", 5, "IKEA", List.of("Adjustable", "Wood", "Large")),
            new Product(4L, "Chair", 149.99, "Furniture", 15, "Herman Miller", List.of("Ergonomic", "Mesh", "Adjustable"))
        );
    }
    
    public List<Customer> getCustomers() {
        return List.of(
            new Customer(1L, "John", "Doe", "john@example.com", "GOLD",
                new Customer.Address("123 Main St", "New York", "NY", "10001"),
                List.of("555-0101", "555-0102")),
            new Customer(2L, "Jane", "Smith", "jane@example.com", "SILVER",
                new Customer.Address("456 Oak Ave", "Los Angeles", "CA", "90001"),
                List.of("555-0201")),
            new Customer(3L, "Bob", "Johnson", "bob@example.com", "BRONZE",
                new Customer.Address("789 Pine Rd", "Chicago", "IL", "60601"),
                List.of("555-0301", "555-0302", "555-0303"))
        );
    }
    
    public List<Employee> getEmployees() {
        return List.of(
            new Employee(1L, "Alice Brown", "IT", 8000.0,
                List.of(
                    new Employee.Skill("Java", 9),
                    new Employee.Skill("Spring", 8),
                    new Employee.Skill("AWS", 7)
                ),
                new Employee.ContactInfo("alice@company.com", "555-1001")),
            new Employee(2L, "Charlie Wilson", "IT", 7000.0,
                List.of(
                    new Employee.Skill("Python", 8),
                    new Employee.Skill("Django", 7)
                ),
                new Employee.ContactInfo("charlie@company.com", "555-1002"))
        );
    }
}

// ===================== Collection Projection Service =====================

@Service
class CollectionProjectionService {
    
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ProjectionDataService dataService;
    
    public CollectionProjectionService(ProjectionDataService dataService) {
        this.dataService = dataService;
    }
    
    /**
     * Basic projection - extract single property
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> basicProjection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Extract product names
        List<String> names = (List<String>) parser
            .parseExpression("#products.![name]")
            .getValue(context);
        
        // Extract prices
        List<Double> prices = (List<Double>) parser
            .parseExpression("#products.![price]")
            .getValue(context);
        
        // Extract categories
        List<String> categories = (List<String>) parser
            .parseExpression("#products.![category]")
            .getValue(context);
        
        // Extract brands
        List<String> brands = (List<String>) parser
            .parseExpression("#products.![brand]")
            .getValue(context);
        
        return Map.of(
            "names", names,
            "prices", prices,
            "categories", new HashSet<>(categories), // unique categories
            "brands", new HashSet<>(brands) // unique brands
        );
    }
    
    /**
     * Method-based projection
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> methodProjection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Extract display names (from method)
        List<String> displayNames = (List<String>) parser
            .parseExpression("#products.![getDisplayName()]")
            .getValue(context);
        
        // Extract prices with tax
        List<Double> pricesWithTax = (List<Double>) parser
            .parseExpression("#products.![getPriceWithTax()]")
            .getValue(context);
        
        return Map.of(
            "displayNames", displayNames,
            "pricesWithTax", pricesWithTax
        );
    }
    
    /**
     * Nested property projection
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> nestedProjection() {
        List<Customer> customers = dataService.getCustomers();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("customers", customers);
        
        // Extract nested properties
        List<String> cities = (List<String>) parser
            .parseExpression("#customers.![address.city]")
            .getValue(context);
        
        List<String> states = (List<String>) parser
            .parseExpression("#customers.![address.state]")
            .getValue(context);
        
        List<String> fullAddresses = (List<String>) parser
            .parseExpression("#customers.![address.getFullAddress()]")
            .getValue(context);
        
        // Extract first phone number from each customer
        List<String> firstPhones = (List<String>) parser
            .parseExpression("#customers.![phoneNumbers[0]]")
            .getValue(context);
        
        return Map.of(
            "cities", cities,
            "states", new HashSet<>(states),
            "fullAddresses", fullAddresses,
            "firstPhones", firstPhones
        );
    }
    
    /**
     * Combining selection and projection
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> selectionAndProjection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // First filter, then project
        List<String> expensiveProductNames = (List<String>) parser
            .parseExpression("#products.?[price > 500].![name]")
            .getValue(context);
        
        List<String> electronicsBrands = (List<String>) parser
            .parseExpression("#products.?[category == 'Electronics'].![brand]")
            .getValue(context);
        
        List<Double> availablePrices = (List<Double>) parser
            .parseExpression("#products.?[stock > 0].![price]")
            .getValue(context);
        
        return Map.of(
            "expensiveProductNames", expensiveProductNames,
            "electronicsBrands", new HashSet<>(electronicsBrands),
            "availablePrices", availablePrices
        );
    }
    
    /**
     * Complex projections with expressions
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> complexProjection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Create formatted strings
        List<String> formatted = (List<String>) parser
            .parseExpression("#products.![name + ' - $' + price]")
            .getValue(context);
        
        // Calculate derived values
        List<Double> discountedPrices = (List<Double>) parser
            .parseExpression("#products.![price * 0.9]")
            .getValue(context);
        
        // Boolean expressions
        List<Boolean> inStock = (List<Boolean>) parser
            .parseExpression("#products.![stock > 0]")
            .getValue(context);
        
        return Map.of(
            "formatted", formatted,
            "discountedPrices", discountedPrices,
            "inStockCount", inStock.stream().filter(b -> b).count()
        );
    }
    
    /**
     * Nested collection projections
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> nestedCollectionProjection() {
        List<Employee> employees = dataService.getEmployees();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("employees", employees);
        
        // Project nested collections - get all skill names
        List<List<String>> allSkillNames = (List<List<String>>) parser
            .parseExpression("#employees.![skills.![name]]")
            .getValue(context);
        
        // Flatten to get all skills
        List<String> flattenedSkills = allSkillNames.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        // Project skill levels
        List<List<Integer>> skillLevels = (List<List<Integer>>) parser
            .parseExpression("#employees.![skills.![level]]")
            .getValue(context);
        
        return Map.of(
            "allSkills", new HashSet<>(flattenedSkills),
            "skillsCount", flattenedSkills.size(),
            "skillLevels", skillLevels.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList())
        );
    }
    
    /**
     * Customer-specific projections
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> customerProjection() {
        List<Customer> customers = dataService.getCustomers();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("customers", customers);
        
        // Extract full names
        List<String> fullNames = (List<String>) parser
            .parseExpression("#customers.![getFullName()]")
            .getValue(context);
        
        // Extract emails
        List<String> emails = (List<String>) parser
            .parseExpression("#customers.![email]")
            .getValue(context);
        
        // Extract tiers
        List<String> tiers = (List<String>) parser
            .parseExpression("#customers.![tier]")
            .getValue(context);
        
        // Count phone numbers per customer
        List<Integer> phoneCounts = (List<Integer>) parser
            .parseExpression("#customers.![phoneNumbers.size()]")
            .getValue(context);
        
        return Map.of(
            "fullNames", fullNames,
            "emails", emails,
            "tiers", tiers,
            "phoneCounts", phoneCounts
        );
    }
    
    /**
     * Transform to custom format
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> customFormatProjection() {
        List<Product> products = dataService.getProducts();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("products", products);
        
        // Create summary strings
        List<String> summaries = (List<String>) parser
            .parseExpression("#products.![brand + ' ' + name + ' (' + category + ')']")
            .getValue(context);
        
        // Price tiers
        List<String> priceTiers = (List<String>) parser
            .parseExpression("#products.![price > 500 ? 'Premium' : price > 200 ? 'Mid-Range' : 'Budget']")
            .getValue(context);
        
        return Map.of(
            "summaries", summaries,
            "priceTiers", priceTiers
        );
    }
}

// ===================== Component with Projections =====================

@Component
class CollectionProjectionComponent {
    
    // Project from simple list
    @Value("#{T(java.util.Arrays).asList(1, 2, 3, 4, 5).![#this * 2]}")
    private List<Integer> doubled;
    
    @Value("#{T(java.util.Arrays).asList(1, 2, 3, 4, 5).![#this * #this]}")
    private List<Integer> squared;
    
    @Value("#{T(java.util.Arrays).asList('apple', 'banana', 'cherry').![#this.toUpperCase()]}")
    private List<String> upperCaseFruits;
    
    @Value("#{T(java.util.Arrays).asList('hello', 'world', 'spring').![#this.length()]}")
    private List<Integer> stringLengths;
    
    public Map<String, Object> getProjections() {
        return Map.of(
            "doubled", doubled,
            "squared", squared,
            "upperCaseFruits", upperCaseFruits,
            "stringLengths", stringLengths
        );
    }
}

// ===================== REST Controller =====================

@RestController
@RequestMapping("/api/collection-projection")
class CollectionProjectionController {
    
    private final CollectionProjectionService projectionService;
    private final CollectionProjectionComponent projectionComponent;
    
    public CollectionProjectionController(
        CollectionProjectionService projectionService,
        CollectionProjectionComponent projectionComponent
    ) {
        this.projectionService = projectionService;
        this.projectionComponent = projectionComponent;
    }
    
    @GetMapping("/basic")
    public Map<String, Object> basicProjection() {
        return projectionService.basicProjection();
    }
    
    @GetMapping("/method")
    public Map<String, Object> methodProjection() {
        return projectionService.methodProjection();
    }
    
    @GetMapping("/nested")
    public Map<String, Object> nestedProjection() {
        return projectionService.nestedProjection();
    }
    
    @GetMapping("/selection-and-projection")
    public Map<String, Object> selectionAndProjection() {
        return projectionService.selectionAndProjection();
    }
    
    @GetMapping("/complex")
    public Map<String, Object> complexProjection() {
        return projectionService.complexProjection();
    }
    
    @GetMapping("/nested-collection")
    public Map<String, Object> nestedCollectionProjection() {
        return projectionService.nestedCollectionProjection();
    }
    
    @GetMapping("/customers")
    public Map<String, Object> customerProjection() {
        return projectionService.customerProjection();
    }
    
    @GetMapping("/custom-format")
    public Map<String, Object> customFormatProjection() {
        return projectionService.customFormatProjection();
    }
    
    @GetMapping("/component")
    public Map<String, Object> componentProjections() {
        return projectionComponent.getProjections();
    }
}

/**
 * Key Concepts Demonstrated:
 * 
 * 1. Projection Operator (.![]):
 *    - Transforms each element in collection
 *    - Returns new collection with transformed values
 *    - Syntax: collection.![expression]
 * 
 * 2. Property Projection:
 *    - Extract single property: .![propertyName]
 *    - Extract nested property: .![object.property]
 *    - Extract from array: .![array[index]]
 * 
 * 3. Method Projection:
 *    - Call method on each element: .![methodName()]
 *    - Call with arguments: .![method(args)]
 *    - Chain methods: .![method1().method2()]
 * 
 * 4. Expression Projection:
 *    - Arithmetic: .![price * 0.9]
 *    - String concatenation: .![firstName + ' ' + lastName]
 *    - Conditional: .![condition ? value1 : value2]
 *    - Complex expressions: .![property1 + property2 * 2]
 * 
 * 5. #this Variable:
 *    - Refers to current element
 *    - Used for primitive collections
 *    - Example: .![#this * 2]
 * 
 * 6. Combining Selection and Projection:
 *    - Filter then transform: .?[condition].![expression]
 *    - Example: .?[price > 100].![name]
 *    - Order matters: selection first, then projection
 * 
 * 7. Nested Projections:
 *    - Project nested collections: .![nestedList.![property]]
 *    - Results in list of lists
 *    - May need flattening
 * 
 * 8. Use Cases:
 *    - Extract specific fields for DTOs
 *    - Transform data for display
 *    - Create summaries/reports
 *    - Map domain objects to view models
 *    - Data aggregation
 * 
 * Testing Examples:
 * 
 * curl http://localhost:8080/api/collection-projection/basic
 * curl http://localhost:8080/api/collection-projection/method
 * curl http://localhost:8080/api/collection-projection/nested
 * curl http://localhost:8080/api/collection-projection/selection-and-projection
 * curl http://localhost:8080/api/collection-projection/complex
 * curl http://localhost:8080/api/collection-projection/customers
 * 
 * SpEL Projection Examples:
 * 
 * # Simple property
 * #products.![name]
 * #customers.![email]
 * 
 * # Nested property
 * #customers.![address.city]
 * #orders.![customer.name]
 * 
 * # Method call
 * #products.![getDisplayName()]
 * #customers.![getFullName()]
 * 
 * # Expressions
 * #products.![price * 0.9]
 * #products.![name + ' - $' + price]
 * #numbers.![#this * #this]
 * 
 * # Combined with selection
 * #products.?[price > 100].![name]
 * #customers.?[tier == 'GOLD'].![email]
 * #employees.?[department == 'IT'].![name]
 * 
 * # Nested collections
 * #employees.![skills.![name]]
 * #orders.![items.![product.name]]
 * 
 * Best Practices:
 * 
 * 1. Keep projection expressions simple
 * 2. Use method calls for complex transformations
 * 3. Consider performance for large collections
 * 4. Handle null values (use safe navigation ?.)
 * 5. Document complex projections
 * 6. Test with empty collections
 * 7. Combine selection and projection efficiently
 * 8. Consider creating DTOs instead of complex projections
 * 9. Use meaningful variable names
 * 10. Validate transformation results
 */
