package com.spring.patterns.factory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory Method Pattern
 * 
 * Factory Method Pattern uses @Bean methods to create and configure beans.
 * The factory method is responsible for instantiation logic and configuration.
 * 
 * Characteristics:
 * - Methods annotated with @Bean
 * - Declared in @Configuration classes
 * - Can accept parameters (other beans)
 * - Return type is the bean type
 * - Method name becomes bean name (default)
 * - Can specify custom bean name
 * - Allows complex initialization
 * 
 * Use Cases:
 * - Custom bean configuration
 * - Third-party library integration
 * - Conditional bean creation
 * - Complex dependency wiring
 * - Multiple beans of same type
 * - Bean customization based on environment
 */
@SpringBootApplication
public class FactoryMethodPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(FactoryMethodPattern.class, args);
        
        System.out.println("\n=== Factory Method Pattern ===");
        
        // Get beans created by factory methods
        UserService userService = context.getBean(UserService.class);
        userService.createUser("john@example.com");
        
        PaymentService paymentService = context.getBean(PaymentService.class);
        paymentService.processPayment(100.0);
        
        NotificationService notificationService = context.getBean(NotificationService.class);
        notificationService.sendNotification("Order confirmed");
        
        // Beans with custom names
        EmailProvider emailProvider = context.getBean("customEmailProvider", EmailProvider.class);
        emailProvider.sendEmail("test@example.com", "Hello");
        
        // Multiple beans of same type with different configurations
        DataSource primaryDataSource = context.getBean("primaryDataSource", DataSource.class);
        primaryDataSource.connect();
        
        DataSource secondaryDataSource = context.getBean("secondaryDataSource", DataSource.class);
        secondaryDataSource.connect();
    }
}

/**
 * Main Configuration with Factory Methods
 */
@Configuration
class FactoryMethodConfig {
    
    /**
     * Example 1: Simple Factory Method
     * Method name becomes bean name
     */
    @Bean
    public UserRepository userRepository() {
        System.out.println("Factory method: Creating UserRepository");
        return new UserRepository("users_table");
    }
    
    /**
     * Example 2: Factory Method with Dependencies
     * Parameters are auto-injected by Spring
     */
    @Bean
    public UserService userService(UserRepository userRepository, EmailService emailService) {
        System.out.println("Factory method: Creating UserService with dependencies");
        UserService service = new UserService(userRepository);
        service.setEmailService(emailService);
        return service;
    }
    
    /**
     * Example 3: Factory Method with Custom Bean Name
     */
    @Bean(name = "customEmailProvider")
    public EmailProvider emailProvider() {
        System.out.println("Factory method: Creating EmailProvider with custom name");
        EmailProvider provider = new EmailProvider();
        provider.setSmtpHost("smtp.gmail.com");
        provider.setSmtpPort(587);
        return provider;
    }
    
    /**
     * Example 4: Factory Method with Init and Destroy
     */
    @Bean(initMethod = "initialize", destroyMethod = "cleanup")
    public EmailService emailService() {
        System.out.println("Factory method: Creating EmailService with lifecycle callbacks");
        return new EmailService("smtp.example.com", 587);
    }
    
    /**
     * Example 5: Multiple Beans of Same Type
     * Different configurations for different purposes
     */
    @Bean
    public DataSource primaryDataSource() {
        System.out.println("Factory method: Creating primary DataSource");
        DataSource ds = new DataSource();
        ds.setUrl("jdbc:postgresql://localhost:5432/primary_db");
        ds.setUsername("primary_user");
        ds.setMaxConnections(20);
        return ds;
    }
    
    @Bean
    public DataSource secondaryDataSource() {
        System.out.println("Factory method: Creating secondary DataSource");
        DataSource ds = new DataSource();
        ds.setUrl("jdbc:postgresql://localhost:5432/secondary_db");
        ds.setUsername("secondary_user");
        ds.setMaxConnections(10);
        return ds;
    }
    
    /**
     * Example 6: Factory Method with Complex Logic
     */
    @Bean
    public PaymentGateway paymentGateway() {
        System.out.println("Factory method: Creating PaymentGateway with complex logic");
        
        // Complex initialization logic
        String environment = System.getProperty("env", "production");
        PaymentGateway gateway;
        
        if ("development".equals(environment)) {
            gateway = new PaymentGateway("sandbox.payment.com", "test_key");
        } else {
            gateway = new PaymentGateway("api.payment.com", "prod_key");
        }
        
        gateway.setTimeout(30000);
        gateway.setRetryAttempts(3);
        
        return gateway;
    }
    
    /**
     * Example 7: Factory Method with Builder Pattern
     */
    @Bean
    public NotificationService notificationService(EmailService emailService) {
        System.out.println("Factory method: Creating NotificationService using builder");
        
        return NotificationService.builder()
            .emailService(emailService)
            .maxRetries(5)
            .timeout(10000)
            .enableAsync(true)
            .build();
    }
    
    /**
     * Example 8: Factory Method with Conditional Creation
     */
    @Bean
    public PaymentService paymentService(PaymentGateway paymentGateway) {
        System.out.println("Factory method: Creating PaymentService");
        
        PaymentService service = new PaymentService(paymentGateway);
        
        // Configure based on environment
        String env = System.getProperty("env", "production");
        if ("development".equals(env)) {
            service.setDebugMode(true);
        }
        
        return service;
    }
}

/**
 * UserRepository class
 */
class UserRepository {
    private final String tableName;
    private final Map<String, User> users = new HashMap<>();
    
    public UserRepository(String tableName) {
        this.tableName = tableName;
        System.out.println("   UserRepository created for table: " + tableName);
    }
    
    public void save(User user) {
        users.put(user.getEmail(), user);
        System.out.println("   User saved to " + tableName + ": " + user.getEmail());
    }
    
    public User findByEmail(String email) {
        return users.get(email);
    }
}

/**
 * User class
 */
class User {
    private String email;
    private LocalDateTime createdAt;
    
    public User(String email) {
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }
    
    public String getEmail() {
        return email;
    }
}

/**
 * EmailService class with lifecycle methods
 */
class EmailService {
    private String smtpHost;
    private int smtpPort;
    private boolean connected;
    
    public EmailService(String smtpHost, int smtpPort) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        System.out.println("   EmailService created: " + smtpHost + ":" + smtpPort);
    }
    
    // Init method
    public void initialize() {
        System.out.println("   EmailService.initialize() called - connecting to SMTP");
        this.connected = true;
    }
    
    // Destroy method
    public void cleanup() {
        System.out.println("   EmailService.cleanup() called - disconnecting from SMTP");
        this.connected = false;
    }
    
    public void send(String to, String subject, String body) {
        if (!connected) {
            throw new IllegalStateException("Email service not connected");
        }
        System.out.println("   Sending email to: " + to);
    }
}

/**
 * UserService using dependencies
 */
class UserService {
    private final UserRepository userRepository;
    private EmailService emailService;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    public void createUser(String email) {
        System.out.println("\nUserService.createUser():");
        User user = new User(email);
        userRepository.save(user);
        
        if (emailService != null) {
            emailService.send(email, "Welcome", "Welcome to our service!");
        }
    }
}

/**
 * EmailProvider with configuration
 */
class EmailProvider {
    private String smtpHost;
    private int smtpPort;
    
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }
    
    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }
    
    public void sendEmail(String to, String message) {
        System.out.println("\nEmailProvider.sendEmail():");
        System.out.println("   SMTP: " + smtpHost + ":" + smtpPort);
        System.out.println("   To: " + to);
        System.out.println("   Message: " + message);
    }
}

/**
 * DataSource class
 */
class DataSource {
    private String url;
    private String username;
    private int maxConnections;
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public void connect() {
        System.out.println("\nDataSource.connect():");
        System.out.println("   URL: " + url);
        System.out.println("   User: " + username);
        System.out.println("   Max Connections: " + maxConnections);
    }
}

/**
 * PaymentGateway class
 */
class PaymentGateway {
    private final String apiUrl;
    private final String apiKey;
    private int timeout;
    private int retryAttempts;
    
    public PaymentGateway(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
    
    public boolean processPayment(double amount) {
        System.out.println("   Processing payment: $" + amount + " via " + apiUrl);
        return true;
    }
}

/**
 * PaymentService class
 */
class PaymentService {
    private final PaymentGateway paymentGateway;
    private boolean debugMode = false;
    
    public PaymentService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
    
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    public void processPayment(double amount) {
        System.out.println("\nPaymentService.processPayment():");
        if (debugMode) {
            System.out.println("   DEBUG MODE: Processing payment");
        }
        paymentGateway.processPayment(amount);
    }
}

/**
 * NotificationService with Builder
 */
class NotificationService {
    private final EmailService emailService;
    private final int maxRetries;
    private final int timeout;
    private final boolean enableAsync;
    
    private NotificationService(Builder builder) {
        this.emailService = builder.emailService;
        this.maxRetries = builder.maxRetries;
        this.timeout = builder.timeout;
        this.enableAsync = builder.enableAsync;
        System.out.println("   NotificationService built: retries=" + maxRetries + 
                         ", timeout=" + timeout + ", async=" + enableAsync);
    }
    
    public void sendNotification(String message) {
        System.out.println("\nNotificationService.sendNotification():");
        System.out.println("   Message: " + message);
        System.out.println("   Max Retries: " + maxRetries);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    static class Builder {
        private EmailService emailService;
        private int maxRetries = 3;
        private int timeout = 5000;
        private boolean enableAsync = false;
        
        public Builder emailService(EmailService emailService) {
            this.emailService = emailService;
            return this;
        }
        
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }
        
        public Builder enableAsync(boolean enableAsync) {
            this.enableAsync = enableAsync;
            return this;
        }
        
        public NotificationService build() {
            return new NotificationService(this);
        }
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/factory-method")
class FactoryMethodController {
    
    private final UserService userService;
    private final PaymentService paymentService;
    
    public FactoryMethodController(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }
    
    @GetMapping("/user/{email}")
    public String createUser(@PathVariable String email) {
        userService.createUser(email);
        return "User created: " + email;
    }
    
    @GetMapping("/payment/{amount}")
    public String processPayment(@PathVariable double amount) {
        paymentService.processPayment(amount);
        return "Payment processed: $" + amount;
    }
}

/**
 * Key Points:
 * 
 * 1. Factory Method Declaration:
 *    @Bean
 *    public MyBean myBean() {
 *        return new MyBean();
 *    }
 * 
 * 2. Custom Bean Name:
 *    @Bean(name = "customName")
 *    @Bean({"name1", "name2"}) // Multiple names
 * 
 * 3. Dependencies:
 *    @Bean
 *    public Service service(Repository repo) {
 *        return new Service(repo);
 *    }
 * 
 * 4. Lifecycle Callbacks:
 *    @Bean(initMethod = "init", destroyMethod = "destroy")
 * 
 * 5. Advantages:
 *    ✓ Full control over bean creation
 *    ✓ Complex initialization logic
 *    ✓ Conditional bean creation
 *    ✓ Integration with third-party libraries
 *    ✓ Multiple beans of same type
 *    ✓ Builder pattern integration
 * 
 * 6. Best Practices:
 *    ✓ Use for complex bean creation
 *    ✓ Prefer constructor injection in beans
 *    ✓ Use meaningful method names
 *    ✓ Document factory method purpose
 *    ✓ Keep factory methods simple
 * 
 * 7. When to Use:
 *    - Third-party library integration
 *    - Complex configuration required
 *    - Multiple instances with different configs
 *    - Conditional bean creation
 *    - Builder pattern usage
 * 
 * 8. Comparison with FactoryBean:
 *    Factory Method: Simple, method-based creation
 *    FactoryBean: Complex, interface-based creation
 */
