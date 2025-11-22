package com.spring.patterns.expressionlanguage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Bean Reference Pattern
 * 
 * Demonstrates how to reference and access beans using SpEL:
 * - @beanName syntax for bean references
 * - Accessing bean properties
 * - Calling bean methods
 * - Conditional bean selection
 * - Bean dependency injection via SpEL
 * - Dynamic bean resolution
 * - ApplicationContext bean lookup
 */

// ===================== Domain Models and Services =====================

record Product(Long id, String name, double price, String category) {}

record Customer(Long id, String name, String email, String tier) {}

record Order(Long id, Customer customer, List<Product> products, double total) {}

// ===================== Service Beans =====================

@Service("productService")
class ProductService {
    
    private final List<Product> products = List.of(
        new Product(1L, "Laptop", 999.99, "Electronics"),
        new Product(2L, "Phone", 599.99, "Electronics"),
        new Product(3L, "Desk", 299.99, "Furniture"),
        new Product(4L, "Chair", 149.99, "Furniture")
    );
    
    public List<Product> getAllProducts() {
        return products;
    }
    
    public Product getProductById(Long id) {
        return products.stream()
            .filter(p -> p.id().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Product> getProductsByCategory(String category) {
        return products.stream()
            .filter(p -> p.category().equals(category))
            .toList();
    }
    
    public double calculateTotal(List<Product> products) {
        return products.stream()
            .mapToDouble(Product::price)
            .sum();
    }
    
    public int getProductCount() {
        return products.size();
    }
}

@Service("customerService")
class CustomerService {
    
    private final List<Customer> customers = List.of(
        new Customer(1L, "John Doe", "john@example.com", "GOLD"),
        new Customer(2L, "Jane Smith", "jane@example.com", "SILVER"),
        new Customer(3L, "Bob Johnson", "bob@example.com", "BRONZE")
    );
    
    public List<Customer> getAllCustomers() {
        return customers;
    }
    
    public Customer getCustomerById(Long id) {
        return customers.stream()
            .filter(c -> c.id().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public double getDiscountRate(String tier) {
        return switch (tier) {
            case "GOLD" -> 0.20;
            case "SILVER" -> 0.10;
            case "BRONZE" -> 0.05;
            default -> 0.0;
        };
    }
}

@Service("orderService")
class OrderService {
    
    private final ProductService productService;
    private final CustomerService customerService;
    
    public OrderService(ProductService productService, CustomerService customerService) {
        this.productService = productService;
        this.customerService = customerService;
    }
    
    public Order createOrder(Long customerId, List<Long> productIds) {
        Customer customer = customerService.getCustomerById(customerId);
        List<Product> products = productIds.stream()
            .map(productService::getProductById)
            .toList();
        double total = productService.calculateTotal(products);
        double discount = customerService.getDiscountRate(customer.tier());
        double finalTotal = total * (1 - discount);
        
        return new Order(1L, customer, products, finalTotal);
    }
}

@Service("calculatorService")
class CalculatorService {
    
    public int add(int a, int b) {
        return a + b;
    }
    
    public int multiply(int a, int b) {
        return a * b;
    }
    
    public double percentage(double value, double percent) {
        return value * (percent / 100);
    }
}

@Service("configService")
class ConfigService {
    
    private final String appName = "Spring Application";
    private final String version = "2.0.0";
    private final boolean debugMode = false;
    private final int maxConnections = 100;
    
    public String getAppName() {
        return appName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public String getFullVersion() {
        return appName + " v" + version;
    }
}

// ===================== Bean Reference Component =====================

@Component
class BeanReferenceComponent {
    
    // Reference bean and access its property
    @Value("#{@configService.appName}")
    private String appName;
    
    // Reference bean and call method
    @Value("#{@configService.getVersion()}")
    private String version;
    
    // Reference bean and access method result
    @Value("#{@configService.getFullVersion()}")
    private String fullVersion;
    
    // Reference bean property
    @Value("#{@configService.maxConnections}")
    private int maxConnections;
    
    // Call bean method with arguments
    @Value("#{@calculatorService.add(10, 20)}")
    private int calculatedSum;
    
    // Call bean method with multiple arguments
    @Value("#{@calculatorService.multiply(5, 6)}")
    private int calculatedProduct;
    
    // Complex bean method call
    @Value("#{@calculatorService.percentage(1000, 15)}")
    private double calculatedPercentage;
    
    // Reference bean and get collection size
    @Value("#{@productService.getAllProducts().size()}")
    private int productCount;
    
    // Reference bean and filter collection
    @Value("#{@productService.getAllProducts().?[price > 500].size()}")
    private int expensiveProductCount;
    
    // Conditional bean reference
    @Value("#{@configService.isDebugMode() ? 'DEBUG' : 'PRODUCTION'}")
    private String mode;
    
    public Map<String, Object> getBeanReferences() {
        return Map.of(
            "appName", appName,
            "version", version,
            "fullVersion", fullVersion,
            "maxConnections", maxConnections,
            "calculatedSum", calculatedSum,
            "calculatedProduct", calculatedProduct,
            "calculatedPercentage", calculatedPercentage,
            "productCount", productCount,
            "expensiveProductCount", expensiveProductCount,
            "mode", mode
        );
    }
}

// ===================== Dynamic Bean Reference Service =====================

@Service
class BeanReferenceService {
    
    private final ApplicationContext applicationContext;
    private final ExpressionParser parser = new SpelExpressionParser();
    
    @Autowired
    public BeanReferenceService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    /**
     * Access bean by name using SpEL
     */
    public Object accessBeanByName(String beanName) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver((ctx, name) -> applicationContext.getBean(name));
        
        Expression exp = parser.parseExpression("@" + beanName);
        return exp.getValue(context);
    }
    
    /**
     * Call bean method using SpEL
     */
    public Object callBeanMethod(String beanName, String methodCall) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver((ctx, name) -> applicationContext.getBean(name));
        
        Expression exp = parser.parseExpression("@" + beanName + "." + methodCall);
        return exp.getValue(context);
    }
    
    /**
     * Access bean property using SpEL
     */
    public Object accessBeanProperty(String beanName, String propertyName) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver((ctx, name) -> applicationContext.getBean(name));
        
        Expression exp = parser.parseExpression("@" + beanName + "." + propertyName);
        return exp.getValue(context);
    }
    
    /**
     * Reference multiple beans
     */
    public Map<String, Object> referencMultipleBeans() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver((ctx, name) -> applicationContext.getBean(name));
        
        // Get product count
        int productCount = parser.parseExpression("@productService.getProductCount()")
                                .getValue(context, Integer.class);
        
        // Get all customers size
        int customerCount = parser.parseExpression("@customerService.getAllCustomers().size()")
                                  .getValue(context, Integer.class);
        
        // Get config values
        String appName = parser.parseExpression("@configService.getAppName()")
                              .getValue(context, String.class);
        
        // Calculate something
        int sum = parser.parseExpression("@calculatorService.add(100, 200)")
                       .getValue(context, Integer.class);
        
        return Map.of(
            "productCount", productCount,
            "customerCount", customerCount,
            "appName", appName,
            "calculatedSum", sum
        );
    }
    
    /**
     * Chain bean method calls
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> chainBeanCalls() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver((ctx, name) -> applicationContext.getBean(name));
        
        // Get products, filter by category, then get first item name
        String firstElectronics = parser
            .parseExpression("@productService.getProductsByCategory('Electronics')[0].name()")
            .getValue(context, String.class);
        
        // Get all products, filter expensive ones, get count
        int expensiveCount = ((List<Product>) parser
            .parseExpression("@productService.getAllProducts().?[price() > 500]")
            .getValue(context))
            .size();
        
        return Map.of(
            "firstElectronics", firstElectronics,
            "expensiveCount", expensiveCount
        );
    }
    
    /**
     * Conditional bean selection
     */
    public Object conditionalBeanSelection(boolean useCache) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver((ctx, name) -> applicationContext.getBean(name));
        context.setVariable("useCache", useCache);
        
        // Select bean based on condition
        String serviceName = parser
            .parseExpression("#useCache ? 'productService' : 'customerService'")
            .getValue(context, String.class);
        
        return accessBeanByName(serviceName);
    }
}

// ===================== Configuration with Bean References =====================

@Configuration
class BeanReferenceConfiguration {
    
    /**
     * Bean that depends on other beans via SpEL
     */
    @Bean
    public Map<String, Object> applicationInfo(
        @Value("#{@configService.appName}") String appName,
        @Value("#{@configService.version}") String version,
        @Value("#{@productService.getProductCount()}") int productCount,
        @Value("#{@customerService.getAllCustomers().size()}") int customerCount
    ) {
        return Map.of(
            "appName", appName,
            "version", version,
            "productCount", productCount,
            "customerCount", customerCount
        );
    }
    
    /**
     * Bean with complex SpEL bean references
     */
    @Bean
    public Map<String, Object> statistics(
        @Value("#{@productService.getAllProducts()}") List<Product> products,
        @Value("#{@customerService.getAllCustomers()}") List<Customer> customers
    ) {
        double avgPrice = products.stream()
            .mapToDouble(Product::price)
            .average()
            .orElse(0.0);
        
        long goldCustomers = customers.stream()
            .filter(c -> c.tier().equals("GOLD"))
            .count();
        
        return Map.of(
            "totalProducts", products.size(),
            "totalCustomers", customers.size(),
            "averagePrice", avgPrice,
            "goldCustomers", goldCustomers
        );
    }
}

// ===================== Lazy Bean Reference =====================

@Component
class LazyBeanReferenceComponent {
    
    // Lazy bean reference - bean loaded only when accessed
    @Value("#{@productService}")
    @Lazy
    private ProductService productService;
    
    public int getLazyProductCount() {
        return productService.getProductCount();
    }
}

// ===================== REST Controller =====================

@RestController
@RequestMapping("/api/bean-reference")
class BeanReferenceController {
    
    private final BeanReferenceComponent beanRefComponent;
    private final BeanReferenceService beanRefService;
    private final ApplicationContext applicationContext;
    
    public BeanReferenceController(
        BeanReferenceComponent beanRefComponent,
        BeanReferenceService beanRefService,
        ApplicationContext applicationContext
    ) {
        this.beanRefComponent = beanRefComponent;
        this.beanRefService = beanRefService;
        this.applicationContext = applicationContext;
    }
    
    @GetMapping("/component")
    public Map<String, Object> getComponentReferences() {
        return beanRefComponent.getBeanReferences();
    }
    
    @GetMapping("/bean/{name}")
    public Object getBeanByName(@PathVariable String name) {
        try {
            Object bean = beanRefService.accessBeanByName(name);
            return Map.of(
                "beanName", name,
                "beanClass", bean.getClass().getSimpleName(),
                "success", true
            );
        } catch (Exception e) {
            return Map.of(
                "beanName", name,
                "error", e.getMessage(),
                "success", false
            );
        }
    }
    
    @GetMapping("/bean/{name}/method/{method}")
    public Object callBeanMethod(
        @PathVariable String name,
        @PathVariable String method
    ) {
        try {
            Object result = beanRefService.callBeanMethod(name, method);
            return Map.of(
                "beanName", name,
                "method", method,
                "result", result,
                "success", true
            );
        } catch (Exception e) {
            return Map.of(
                "beanName", name,
                "method", method,
                "error", e.getMessage(),
                "success", false
            );
        }
    }
    
    @GetMapping("/multiple")
    public Map<String, Object> getMultipleBeanReferences() {
        return beanRefService.referencMultipleBeans();
    }
    
    @GetMapping("/chain")
    public Map<String, Object> getChainedBeanCalls() {
        return beanRefService.chainBeanCalls();
    }
    
    @GetMapping("/all-beans")
    public Map<String, Object> getAllRegisteredBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        return Map.of(
            "totalBeans", beanNames.length,
            "serviceBeans", List.of(
                "productService",
                "customerService",
                "orderService",
                "calculatorService",
                "configService"
            )
        );
    }
}

/**
 * Key Concepts Demonstrated:
 * 
 * 1. Bean Reference Syntax:
 *    - @beanName - Reference bean by name
 *    - @beanName.property - Access bean property
 *    - @beanName.method() - Call bean method
 *    - @beanName.method(args) - Call method with arguments
 * 
 * 2. @Value with Bean References:
 *    - Field injection: @Value("#{@beanName}")
 *    - Constructor injection: @Value("#{@beanName}") param
 *    - Method injection: @Bean method(@Value("#{@beanName}") param)
 * 
 * 3. Accessing Bean Properties:
 *    - #{@configService.appName} - Direct property
 *    - #{@configService.getVersion()} - Getter method
 *    - #{@configService.maxConnections} - Public field
 * 
 * 4. Calling Bean Methods:
 *    - #{@service.method()} - No arguments
 *    - #{@service.method(arg)} - Single argument
 *    - #{@service.method(arg1, arg2)} - Multiple arguments
 * 
 * 5. Collection Operations on Beans:
 *    - #{@service.getList().size()} - Collection size
 *    - #{@service.getList()[0]} - Element access
 *    - #{@service.getList().?[condition]} - Filtering
 *    - #{@service.getList().![property]} - Projection
 * 
 * 6. Chaining Bean References:
 *    - #{@service1.getBean().method()}
 *    - #{@service1.getList()[0].property}
 *    - Multiple levels of nesting
 * 
 * 7. Conditional Bean Selection:
 *    - #{condition ? @bean1 : @bean2}
 *    - #{@service.isEnabled() ? @activeBean : @fallbackBean}
 * 
 * 8. ApplicationContext Bean Lookup:
 *    - context.getBean(beanName)
 *    - context.getBean(beanName, BeanClass.class)
 *    - context.getBeanDefinitionNames()
 * 
 * 9. BeanResolver in SpEL:
 *    - StandardEvaluationContext.setBeanResolver()
 *    - Custom bean resolution logic
 *    - Dynamic bean lookup
 * 
 * 10. Use Cases:
 *     - Injecting bean method results
 *     - Dynamic bean selection
 *     - Configuration-driven bean wiring
 *     - Cross-bean calculations
 *     - Bean property aggregation
 * 
 * Testing Examples:
 * 
 * # Get component bean references
 * curl http://localhost:8080/api/bean-reference/component
 * 
 * # Get bean by name
 * curl http://localhost:8080/api/bean-reference/bean/productService
 * 
 * # Call bean method
 * curl http://localhost:8080/api/bean-reference/bean/productService/method/getProductCount()
 * 
 * # Multiple bean references
 * curl http://localhost:8080/api/bean-reference/multiple
 * 
 * # Chained bean calls
 * curl http://localhost:8080/api/bean-reference/chain
 * 
 * Best Practices:
 * 
 * 1. Use constructor injection over field injection
 * 2. Avoid circular dependencies in bean references
 * 3. Use @Lazy for expensive bean operations
 * 4. Prefer type-safe injection over SpEL when possible
 * 5. Document complex bean reference expressions
 * 6. Handle null cases with Elvis operator
 * 7. Use meaningful bean names
 * 8. Test bean reference expressions thoroughly
 */
