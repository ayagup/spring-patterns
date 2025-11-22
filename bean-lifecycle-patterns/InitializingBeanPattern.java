package com.spring.patterns.lifecycle;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InitializingBean Pattern
 * =========================
 * 
 * Demonstrates the InitializingBean interface for bean initialization.
 * This is a Spring-specific interface that provides afterPropertiesSet() callback.
 * 
 * KEY FEATURES:
 * =============
 * - Spring-specific interface
 * - Single afterPropertiesSet() method
 * - Called after all properties are set
 * - Can throw exceptions
 * - Tighter Spring integration than @PostConstruct
 * - Allows programmatic initialization logic
 * 
 * INTERFACE:
 * ==========
 * public interface InitializingBean {
 *     void afterPropertiesSet() throws Exception;
 * }
 * 
 * EXECUTION ORDER:
 * ================
 * 1. Constructor
 * 2. Dependency Injection
 * 3. @PostConstruct methods
 * 4. InitializingBean.afterPropertiesSet()
 * 5. Custom init-method
 * 
 * WHEN TO USE:
 * ============
 * - Need Spring-aware initialization
 * - Want to throw checked exceptions
 * - Require validation after all properties set
 * - Building Spring framework extensions
 * 
 * vs OTHER INITIALIZATION METHODS:
 * =================================
 * InitializingBean.afterPropertiesSet():
 *   - Spring-specific
 *   - Can throw checked exceptions
 *   - Better exception handling
 * 
 * @PostConstruct:
 *   - Standard annotation (JSR-250)
 *   - Portable across containers
 *   - More commonly used
 * 
 * @Bean(initMethod):
 *   - For third-party classes
 *   - Configuration-based
 *   - No interface implementation needed
 */

@SpringBootApplication
public class InitializingBeanPattern {

    public static void main(String[] args) {
        SpringApplication.run(InitializingBeanPattern.class, args);
        System.out.println("\n=== InitializingBean Pattern Demo ===\n");
    }
}

/**
 * Example 1: Data Repository with validation
 */
@Component
class UserRepository implements InitializingBean {
    private final DatabaseConnectionService dbService;
    private List<User> cache;
    private boolean initialized;
    private LocalDateTime initializedAt;
    
    public UserRepository(DatabaseConnectionService dbService) {
        System.out.println("UserRepository - Constructor");
        this.dbService = dbService;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("UserRepository - afterPropertiesSet() called");
        
        // Validate dependencies
        if (dbService == null) {
            throw new IllegalStateException("DatabaseConnectionService is required!");
        }
        
        // Initialize cache
        loadUsers();
        initialized = true;
        initializedAt = LocalDateTime.now();
        
        System.out.println("  Loaded " + cache.size() + " users into cache");
    }
    
    private void loadUsers() {
        cache = new ArrayList<>();
        cache.add(new User(1L, "john@example.com", "John Doe"));
        cache.add(new User(2L, "jane@example.com", "Jane Smith"));
        cache.add(new User(3L, "bob@example.com", "Bob Johnson"));
    }
    
    public List<User> findAll() {
        return new ArrayList<>(cache);
    }
    
    public String getStatus() {
        return String.format("Initialized: %s, Cached Users: %d, Initialized At: %s",
            initialized, cache != null ? cache.size() : 0, initializedAt);
    }
}

/**
 * Example 2: Configuration Loader with validation
 */
@Service
class ConfigurationLoader implements InitializingBean {
    private Map<String, String> config;
    private boolean validated;
    private LocalDateTime loadedAt;
    
    public ConfigurationLoader() {
        System.out.println("\nConfigurationLoader - Constructor");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("ConfigurationLoader - afterPropertiesSet() called");
        
        // Load configuration
        loadConfiguration();
        
        // Validate configuration
        validateConfiguration();
        
        validated = true;
        loadedAt = LocalDateTime.now();
        
        System.out.println("  Configuration loaded and validated");
    }
    
    private void loadConfiguration() {
        config = new HashMap<>();
        config.put("app.name", "Spring Patterns");
        config.put("app.version", "1.0.0");
        config.put("db.url", "jdbc:postgresql://localhost:5432/mydb");
        config.put("db.max.connections", "100");
    }
    
    private void validateConfiguration() throws Exception {
        if (config.isEmpty()) {
            throw new Exception("Configuration is empty!");
        }
        
        if (!config.containsKey("db.url")) {
            throw new Exception("Database URL is required!");
        }
    }
    
    public String getProperty(String key) {
        return config.get(key);
    }
    
    public Map<String, String> getAllProperties() {
        return new HashMap<>(config);
    }
    
    public String getStatus() {
        return String.format("Validated: %s, Properties: %d, Loaded At: %s",
            validated, config.size(), loadedAt);
    }
}

/**
 * Example 3: Message Template Engine
 */
@Component
class MessageTemplateEngine implements InitializingBean {
    private Map<String, Template> templates;
    private boolean compiled;
    private LocalDateTime compiledAt;
    
    public MessageTemplateEngine() {
        System.out.println("\nMessageTemplateEngine - Constructor");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("MessageTemplateEngine - afterPropertiesSet() called");
        
        // Load templates
        loadTemplates();
        
        // Compile templates
        compileTemplates();
        
        compiled = true;
        compiledAt = LocalDateTime.now();
        
        System.out.println("  Compiled " + templates.size() + " templates");
    }
    
    private void loadTemplates() {
        templates = new HashMap<>();
        templates.put("welcome", new Template("Welcome {{name}}!"));
        templates.put("goodbye", new Template("Goodbye {{name}}!"));
        templates.put("order", new Template("Order #{{orderId}} confirmed"));
    }
    
    private void compileTemplates() throws Exception {
        for (Template template : templates.values()) {
            template.compile();
        }
    }
    
    public String render(String templateName, Map<String, String> data) {
        Template template = templates.get(templateName);
        return template != null ? template.render(data) : "Template not found";
    }
    
    public String getStatus() {
        return String.format("Compiled: %s, Templates: %d, Compiled At: %s",
            compiled, templates.size(), compiledAt);
    }
    
    static class Template {
        private final String content;
        private boolean compiled;
        
        public Template(String content) {
            this.content = content;
        }
        
        public void compile() {
            // Simulate compilation
            this.compiled = true;
        }
        
        public String render(Map<String, String> data) {
            String result = content;
            for (Map.Entry<String, String> entry : data.entrySet()) {
                result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            return result;
        }
    }
}

/**
 * Example 4: API Client Initializer
 */
@Service
class ApiClientInitializer implements InitializingBean {
    private String apiKey;
    private String baseUrl;
    private boolean authenticated;
    private LocalDateTime authenticatedAt;
    
    public ApiClientInitializer() {
        System.out.println("\nApiClientInitializer - Constructor");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("ApiClientInitializer - afterPropertiesSet() called");
        
        // Load API credentials
        loadCredentials();
        
        // Authenticate
        authenticate();
        
        authenticated = true;
        authenticatedAt = LocalDateTime.now();
        
        System.out.println("  API client authenticated");
    }
    
    private void loadCredentials() throws Exception {
        apiKey = "api-key-12345";
        baseUrl = "https://api.example.com";
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("API key is required!");
        }
    }
    
    private void authenticate() {
        // Simulate authentication
        System.out.println("  Authenticating with API key: " + apiKey);
    }
    
    public String getStatus() {
        return String.format("Base URL: %s, Authenticated: %s, Authenticated At: %s",
            baseUrl, authenticated, authenticatedAt);
    }
}

/**
 * Example 5: Data Validator with rule compilation
 */
@Component
class DataValidatorService implements InitializingBean {
    private List<ValidationRule> rules;
    private boolean rulesCompiled;
    private LocalDateTime compiledAt;
    
    public DataValidatorService() {
        System.out.println("\nDataValidatorService - Constructor");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("DataValidatorService - afterPropertiesSet() called");
        
        // Load validation rules
        loadRules();
        
        // Compile rules
        compileRules();
        
        rulesCompiled = true;
        compiledAt = LocalDateTime.now();
        
        System.out.println("  Compiled " + rules.size() + " validation rules");
    }
    
    private void loadRules() {
        rules = new ArrayList<>();
        rules.add(new ValidationRule("email", "^[A-Za-z0-9+_.-]+@(.+)$"));
        rules.add(new ValidationRule("phone", "^\\d{10}$"));
        rules.add(new ValidationRule("zipcode", "^\\d{5}$"));
    }
    
    private void compileRules() throws Exception {
        for (ValidationRule rule : rules) {
            rule.compile();
        }
        
        if (rules.isEmpty()) {
            throw new Exception("No validation rules loaded!");
        }
    }
    
    public boolean validate(String type, String value) {
        return rules.stream()
            .filter(rule -> rule.type.equals(type))
            .findFirst()
            .map(rule -> value.matches(rule.pattern))
            .orElse(false);
    }
    
    public String getStatus() {
        return String.format("Rules Compiled: %s, Total Rules: %d, Compiled At: %s",
            rulesCompiled, rules.size(), compiledAt);
    }
    
    static class ValidationRule {
        private final String type;
        private final String pattern;
        private boolean compiled;
        
        public ValidationRule(String type, String pattern) {
            this.type = type;
            this.pattern = pattern;
        }
        
        public void compile() {
            // Simulate compilation
            this.compiled = true;
        }
    }
}

/**
 * Supporting services
 */
@Service
class DatabaseConnectionService {
    public DatabaseConnectionService() {
        System.out.println("DatabaseConnectionService - Constructor");
    }
}

record User(Long id, String email, String name) {}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/initializing-bean")
class InitializingBeanController {
    
    private final UserRepository userRepository;
    private final ConfigurationLoader configLoader;
    private final MessageTemplateEngine templateEngine;
    private final ApiClientInitializer apiClient;
    private final DataValidatorService validator;
    
    public InitializingBeanController(
            UserRepository userRepository,
            ConfigurationLoader configLoader,
            MessageTemplateEngine templateEngine,
            ApiClientInitializer apiClient,
            DataValidatorService validator) {
        this.userRepository = userRepository;
        this.configLoader = configLoader;
        this.templateEngine = templateEngine;
        this.apiClient = apiClient;
        this.validator = validator;
        System.out.println("\nInitializingBeanController - All beans initialized\n");
    }
    
    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }
    
    @GetMapping("/users/status")
    public String getUsersStatus() {
        return userRepository.getStatus();
    }
    
    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return configLoader.getAllProperties();
    }
    
    @GetMapping("/config/status")
    public String getConfigStatus() {
        return configLoader.getStatus();
    }
    
    @GetMapping("/template/welcome")
    public String renderWelcome() {
        return templateEngine.render("welcome", Map.of("name", "John"));
    }
    
    @GetMapping("/template/status")
    public String getTemplateStatus() {
        return templateEngine.getStatus();
    }
    
    @GetMapping("/api/status")
    public String getApiStatus() {
        return apiClient.getStatus();
    }
    
    @GetMapping("/validate/email/{email}")
    public String validateEmail(String email) {
        boolean valid = validator.validate("email", email);
        return String.format("'%s' is %s", email, valid ? "VALID" : "INVALID");
    }
    
    @GetMapping("/validator/status")
    public String getValidatorStatus() {
        return validator.getStatus();
    }
    
    @GetMapping("/status")
    public String getStatus() {
        return String.format("""
            InitializingBean Pattern Status:
            
            User Repository: %s
            Configuration: %s
            Template Engine: %s
            API Client: %s
            Validator: %s
            """,
            userRepository.getStatus(),
            configLoader.getStatus(),
            templateEngine.getStatus(),
            apiClient.getStatus(),
            validator.getStatus()
        );
    }
}

/**
 * TESTING:
 * ========
 * 
 * curl http://localhost:8080/api/initializing-bean/status
 * curl http://localhost:8080/api/initializing-bean/users
 * curl http://localhost:8080/api/initializing-bean/config
 * curl http://localhost:8080/api/initializing-bean/template/welcome
 * curl http://localhost:8080/api/initializing-bean/validate/email/test@example.com
 * 
 * BEST PRACTICES:
 * ===============
 * 
 * 1. Use for Spring-aware initialization
 * 2. Validate all dependencies
 * 3. Throw exceptions for critical failures
 * 4. Keep initialization logic fast
 * 5. Log initialization steps
 * 6. Handle errors gracefully
 * 
 * ADVANTAGES:
 * ===========
 * - Can throw checked exceptions
 * - Spring-aware initialization
 * - Better exception handling
 * - Framework integration
 * 
 * DISADVANTAGES:
 * ==============
 * - Couples code to Spring
 * - Less portable than @PostConstruct
 * - Requires interface implementation
 */
