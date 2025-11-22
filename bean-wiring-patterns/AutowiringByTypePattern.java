package com.spring.patterns.wiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Autowiring by Type Pattern
 * 
 * Spring autowires beans by matching the type of the dependency.
 * This is the default and most common autowiring strategy.
 * 
 * Characteristics:
 * - Matches bean by type (class or interface)
 * - Default autowiring mode
 * - Works with @Autowired, @Inject, or constructor injection
 * - Fails if multiple beans of same type exist (without @Primary or @Qualifier)
 * - Can be used on fields, setters, constructors, and methods
 * 
 * Autowiring Types:
 * 1. Field Injection - Direct field autowiring
 * 2. Setter Injection - Via setter method
 * 3. Constructor Injection - Via constructor (recommended)
 * 4. Method Injection - Via any method
 * 
 * Use Cases:
 * - Single implementation of an interface
 * - Type-based dependency resolution
 * - Standard dependency injection
 * - Service layer wiring
 */
@SpringBootApplication
public class AutowiringByTypePattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AutowiringByTypePattern.class, args);
        
        System.out.println("\n=== Autowiring by Type Pattern ===");
        
        // Demonstrate autowiring
        UserService userService = context.getBean(UserService.class);
        System.out.println("UserService created with autowired dependencies");
        System.out.println("UserRepository type: " + userService.getUserRepository().getClass().getSimpleName());
        System.out.println("EmailService type: " + userService.getEmailService().getClass().getSimpleName());
        
        // Test functionality
        String result = userService.registerUser("john@example.com");
        System.out.println(result);
    }
}

/**
 * Configuration class
 */
@Configuration
class TypeWiringConfig {
    
    @Bean
    public NotificationService notificationService() {
        return new NotificationService();
    }
    
    @Bean
    public ValidationService validationService() {
        return new ValidationService();
    }
}

/**
 * Repository layer - automatically detected by component scanning
 */
@Repository
class UserRepository {
    
    public UserRepository() {
        System.out.println("UserRepository created");
    }
    
    public String saveUser(String email) {
        System.out.println("User saved: " + email);
        return "USER-" + System.currentTimeMillis();
    }
    
    public String findUser(String userId) {
        return "User found: " + userId;
    }
}

/**
 * Email service - component
 */
@Component
class EmailService {
    
    public EmailService() {
        System.out.println("EmailService created");
    }
    
    public void sendEmail(String to, String subject) {
        System.out.println("Email sent to " + to + ": " + subject);
    }
}

/**
 * Notification service - defined via @Bean
 */
class NotificationService {
    
    public NotificationService() {
        System.out.println("NotificationService created");
    }
    
    public void sendNotification(String message) {
        System.out.println("Notification: " + message);
    }
}

/**
 * Validation service
 */
class ValidationService {
    
    public ValidationService() {
        System.out.println("ValidationService created");
    }
    
    public boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}

/**
 * Service layer demonstrating different autowiring approaches
 */
@Service
class UserService {
    
    // 1. FIELD INJECTION - Not recommended but commonly used
    @Autowired
    private UserRepository userRepository;
    
    // 2. SETTER INJECTION - Allows optional dependencies
    private EmailService emailService;
    
    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
        System.out.println("EmailService injected via setter");
    }
    
    // 3. CONSTRUCTOR INJECTION - Recommended approach
    private final NotificationService notificationService;
    private final ValidationService validationService;
    
    @Autowired // Optional in Spring 4.3+ if only one constructor
    public UserService(NotificationService notificationService, 
                      ValidationService validationService) {
        this.notificationService = notificationService;
        this.validationService = validationService;
        System.out.println("UserService created with constructor injection");
    }
    
    public String registerUser(String email) {
        if (!validationService.isValidEmail(email)) {
            return "Invalid email";
        }
        
        String userId = userRepository.saveUser(email);
        emailService.sendEmail(email, "Welcome!");
        notificationService.sendNotification("New user registered: " + email);
        
        return "User registered: " + userId;
    }
    
    // Getters for demonstration
    public UserRepository getUserRepository() {
        return userRepository;
    }
    
    public EmailService getEmailService() {
        return emailService;
    }
}

/**
 * Order service demonstrating method injection
 */
@Service
class OrderService {
    
    private UserRepository userRepository;
    private NotificationService notificationService;
    
    // 4. METHOD INJECTION - Inject multiple dependencies via method
    @Autowired
    public void setupDependencies(UserRepository userRepository, 
                                 NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        System.out.println("OrderService dependencies injected via method");
    }
    
    public String createOrder(String userId, String product) {
        String user = userRepository.findUser(userId);
        notificationService.sendNotification("Order created for " + user);
        return "ORDER-" + System.currentTimeMillis();
    }
}

/**
 * Product service with multiple injection points
 */
@Service
class ProductService {
    
    // Multiple fields autowired
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ValidationService validationService;
    
    public ProductService() {
        System.out.println("ProductService created");
    }
    
    public String addProduct(String userId, String productName) {
        notificationService.sendNotification("Product added: " + productName);
        return "PRODUCT-" + System.currentTimeMillis();
    }
}

/**
 * REST Controller demonstrating type-based autowiring
 */
@RestController
@RequestMapping("/api/type-wiring")
class TypeWiringController {
    
    // Constructor injection - recommended
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    
    public TypeWiringController(UserService userService,
                               OrderService orderService,
                               ProductService productService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
    }
    
    @GetMapping("/register")
    public String registerUser() {
        return userService.registerUser("test@example.com");
    }
    
    @GetMapping("/order")
    public String createOrder() {
        return orderService.createOrder("USER-123", "Laptop");
    }
    
    @GetMapping("/product")
    public String addProduct() {
        return productService.addProduct("USER-123", "Phone");
    }
}

/**
 * Key Points:
 * 
 * 1. Autowiring Modes:
 *    - By Type (default): Matches bean type
 *    - Spring looks for single bean of required type
 *    - Fails if multiple beans of same type exist
 * 
 * 2. Injection Types:
 *    a) Field Injection:
 *       @Autowired
 *       private Service service;
 *       
 *    b) Setter Injection:
 *       @Autowired
 *       public void setService(Service service) { ... }
 *       
 *    c) Constructor Injection (Recommended):
 *       @Autowired
 *       public MyClass(Service service) { ... }
 *       
 *    d) Method Injection:
 *       @Autowired
 *       public void setup(Service service) { ... }
 * 
 * 3. Best Practices:
 *    ✓ Use constructor injection (immutable, testable)
 *    ✓ Make dependencies final when using constructor injection
 *    ✓ Avoid field injection (hard to test, hides dependencies)
 *    ✓ Use setter injection for optional dependencies
 * 
 * 4. When Autowiring by Type Fails:
 *    - Multiple beans of same type → Use @Qualifier or @Primary
 *    - No bean of required type → NoSuchBeanDefinitionException
 *    - Circular dependencies → Use @Lazy or refactor
 * 
 * 5. Advantages:
 *    ✓ Simple and intuitive
 *    ✓ Type-safe
 *    ✓ Works with interfaces
 *    ✓ IDE support (refactoring, navigation)
 * 
 * 6. Disadvantages:
 *    ✗ Fails with multiple beans of same type
 *    ✗ Field injection hides dependencies
 *    ✗ Can lead to tight coupling if overused
 * 
 * 7. Testing:
 *    - Constructor injection: Easy to mock
 *    - Field injection: Requires reflection or Spring test context
 *    - Setter injection: Can set dependencies manually
 * 
 * 8. @Autowired Annotations:
 *    - @Autowired (Spring)
 *    - @Inject (JSR-330)
 *    - @Resource (JSR-250, by name first)
 */
