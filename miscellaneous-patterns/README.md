# Miscellaneous Patterns in Spring Framework

This directory contains comprehensive examples of various Miscellaneous Patterns used in Spring Framework. These patterns demonstrate essential Spring features that don't fit neatly into other categories but are crucial for building robust applications.

## Table of Contents

1. [Callback Pattern](#1-callback-pattern)
2. [Template Callback Pattern](#2-template-callback-pattern)
3. [ResourceBundle Pattern](#3-resourcebundle-pattern)
4. [Locale Resolver Pattern](#4-locale-resolver-pattern)
5. [Theme Resolver Pattern](#5-theme-resolver-pattern)
6. [Multipart Resolver Pattern](#6-multipart-resolver-pattern)
7. [Handler Exception Resolver Pattern](#7-handler-exception-resolver-pattern)
8. [Bean Post Processor Pattern](#8-bean-post-processor-pattern)
9. [Bean Factory Post Processor Pattern](#9-bean-factory-post-processor-pattern)
10. [Destruction Aware Bean Post Processor Pattern](#10-destruction-aware-bean-post-processor-pattern)

## Overview

Miscellaneous patterns in Spring provide essential functionality for:
- **Asynchronous Operations**: Callback-based patterns for non-blocking code
- **Resource Management**: Template-callback for safe resource handling
- **Internationalization**: ResourceBundle and Locale resolution
- **Theming**: Dynamic UI theme management
- **File Upload**: Multipart request handling
- **Error Handling**: Comprehensive exception resolution
- **Bean Lifecycle**: Customizing bean creation and destruction

## Pattern Descriptions

### 1. Callback Pattern

**File**: `CallbackPattern.java`

**Purpose**: Implements callback-based asynchronous execution patterns.

**Key Components**:
- `Callback`: Basic callback interface
- `ParameterizedCallback<T>`: Callback with parameter
- `ResultCallback<T,R>`: Callback that returns a result
- `AsyncCallback<T>`: Asynchronous callback
- `ProgressCallback`: Reports progress of operations
- `CompletionCallback<T>`: Handles completion events
- `CallbackService`: Executes operations with callbacks
- `CallbackRegistry`: Event-driven callback management
- `CallbackChain`: Chainable callback execution
- `CallbackTemplate<T>`: Template with lifecycle hooks
- `CallbackTask<T>`: Task with success/error handlers

**Use Cases**:
- Asynchronous processing
- Event-driven architectures
- Progress reporting
- Error handling workflows
- Chainable operations
- Non-blocking I/O

**Example**:
```java
// Simple callback
callbackService.executeWithCallback("Task", 
    result -> System.out.println("Result: " + result));

// Async callback
callbackService.executeAsync("Async Task", 
    result -> System.out.println("Completed: " + result));

// Callback chain
callbackChain
    .then(data -> processStep1(data))
    .then(data -> processStep2(data))
    .execute("Initial Data");
```

**REST Endpoints**:
- `POST /api/callback/execute` - Execute with callback
- `POST /api/callback/execute-async` - Execute asynchronously
- `POST /api/callback/register` - Register event callback
- `POST /api/callback/trigger` - Trigger event callbacks

---

### 2. Template Callback Pattern

**File**: `TemplateCallbackPattern.java`

**Purpose**: Implements template-callback pattern for resource management and reusable operations (JdbcTemplate-style).

**Key Components**:
- `RowMapper<T>`: Maps single database row
- `ResultSetExtractor<T>`: Processes entire ResultSet
- `ConnectionCallback<T>`: Executes with Connection
- `StatementCallback<T>`: Executes with Statement
- `PreparedStatementCallback<T>`: Executes with PreparedStatement
- `PreparedStatementCreator`: Creates PreparedStatement
- `PreparedStatementSetter`: Sets PreparedStatement parameters
- `DataAccessTemplate`: Template for database operations
- `OperationTemplate<T>`: Abstract template with lifecycle hooks
- `HttpTemplate`: Template for HTTP operations

**Use Cases**:
- Database access (JDBC operations)
- Resource management (connections, streams)
- Exception translation
- Transaction management
- HTTP client operations
- Consistent error handling

**Example**:
```java
// Query with RowMapper
List<User> users = dataAccessTemplate.query(
    "SELECT * FROM users", 
    rs -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        return user;
    });

// Execute with ConnectionCallback
DatabaseInfo info = dataAccessTemplate.execute(conn -> {
    DatabaseMetaData metaData = conn.getMetaData();
    return new DatabaseInfo(
        metaData.getDatabaseProductName(),
        metaData.getDatabaseProductVersion()
    );
});

// HTTP template
HttpResponse response = httpTemplate.getForObject(
    "https://api.example.com/data",
    HttpResponse.class);
```

**REST Endpoints**:
- `GET /api/template-callback/users` - Query all users
- `GET /api/template-callback/users/{id}` - Query single user
- `POST /api/template-callback/execute` - Execute connection callback

---

### 3. ResourceBundle Pattern

**File**: `ResourceBundlePattern.java`

**Purpose**: Implements internationalization (i18n) using Spring's MessageSource.

**Key Components**:
- `MessageSource`: Spring's i18n abstraction
- `ResourceBundleMessageSource`: Uses Java ResourceBundle
- `ReloadableResourceBundleMessageSource`: Can reload changes
- `MessageService`: Service for message retrieval
- `LocaleHelper`: Locale parsing and formatting utilities

**Use Cases**:
- Internationalization (i18n)
- Localization (l10n)
- Multi-language support
- User preference management
- Dynamic message loading
- Message parameterization

**Example**:
```java
// Simple message
String greeting = messageSource.getMessage(
    "greeting", null, Locale.ENGLISH);
// Result: "Hello"

// Message with parameters
Object[] params = {"John", LocalDate.now()};
String welcome = messageSource.getMessage(
    "welcome.user", params, Locale.FRENCH);
// Result: "Bienvenue, John! Date: 2024-01-15"

// Default message
String msg = messageSource.getMessage(
    "nonexistent.key", null, "Default", Locale.ENGLISH);
// Result: "Default"
```

**Configuration**:
```java
@Bean
public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = 
        new ResourceBundleMessageSource();
    messageSource.setBasenames("messages", "errors");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
}
```

**REST Endpoints**:
- `GET /api/resource-bundle/message/{key}?locale=en` - Get message
- `POST /api/resource-bundle/message/{key}` - Get message with params
- `POST /api/resource-bundle/messages` - Get multiple messages
- `GET /api/resource-bundle/locales` - Get supported locales

---

### 4. Locale Resolver Pattern

**File**: `LocaleResolverPattern.java`

**Purpose**: Demonstrates strategies for determining user's locale in web applications.

**Key Components**:
- `AcceptHeaderLocaleResolver`: Uses Accept-Language header
- `CookieLocaleResolver`: Stores locale in cookie
- `SessionLocaleResolver`: Stores locale in session
- `FixedLocaleResolver`: Fixed locale for all users
- `LocaleChangeInterceptor`: Detects locale change requests
- `CustomLocaleResolver`: Custom resolution logic

**Use Cases**:
- User locale detection
- Locale persistence (cookie/session)
- Dynamic locale switching
- Multi-region applications
- User preference management

**Example**:
```java
// Cookie-based locale resolution
@Bean
public LocaleResolver localeResolver() {
    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setDefaultLocale(Locale.ENGLISH);
    resolver.setCookieName("user-locale");
    resolver.setCookieMaxAge(3600);
    return resolver;
}

// Locale change interceptor
@Bean
public LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor interceptor = 
        new LocaleChangeInterceptor();
    interceptor.setParamName("lang");
    return interceptor;
}

// Usage: /page?lang=fr
```

**REST Endpoints**:
- `GET /api/locale-resolver/current` - Get current locale
- `POST /api/locale-resolver/change?locale=fr` - Change locale
- `GET /api/locale-resolver/supported` - Get supported locales

---

### 5. Theme Resolver Pattern

**File**: `ThemeResolverPattern.java`

**Purpose**: Manages UI themes dynamically based on user preferences.

**Key Components**:
- `ThemeSource`: Manages theme resources
- `ThemeResolver`: Strategy for determining current theme
- `CookieThemeResolver`: Stores theme in cookie
- `SessionThemeResolver`: Stores theme in session
- `FixedThemeResolver`: Fixed theme for all users
- `ThemeChangeInterceptor`: Detects theme change requests
- `CustomThemeResolver`: Custom theme resolution

**Use Cases**:
- Dark/Light mode
- Corporate branding
- Accessibility themes (high contrast)
- User customization
- A/B testing different themes
- Seasonal themes

**Example**:
```java
// Session-based theme resolution
@Bean
public ThemeResolver themeResolver() {
    SessionThemeResolver resolver = new SessionThemeResolver();
    resolver.setDefaultThemeName("light");
    return resolver;
}

// Theme change interceptor
@Bean
public ThemeChangeInterceptor themeChangeInterceptor() {
    ThemeChangeInterceptor interceptor = 
        new ThemeChangeInterceptor();
    interceptor.setParamName("theme");
    return interceptor;
}

// Usage: /page?theme=dark
```

**Available Themes**:
- `light`: Light theme (default)
- `dark`: Dark theme
- `blue`: Blue theme
- `green`: Green theme
- `high-contrast`: High contrast theme

**REST Endpoints**:
- `GET /api/theme-resolver/current` - Get current theme
- `POST /api/theme-resolver/change?theme=dark` - Change theme
- `GET /api/theme-resolver/available` - Get available themes
- `GET /api/theme-resolver/resources/{themeName}` - Get theme resources

---

### 6. Multipart Resolver Pattern

**File**: `MultipartResolverPattern.java`

**Purpose**: Handles file upload operations using Spring's multipart resolution.

**Key Components**:
- `MultipartResolver`: Resolves multipart HTTP requests
- `StandardServletMultipartResolver`: Servlet 3.0+ implementation
- `MultipartFile`: Represents uploaded file
- `MultipartConfigElement`: Configuration for file uploads
- `FileUploadService`: Service for file operations

**Use Cases**:
- File uploads
- Image processing
- Document management
- Profile picture uploads
- Batch file processing
- CSV/Excel imports

**Example**:
```java
// Single file upload
@PostMapping("/upload")
public ResponseEntity<?> uploadFile(
        @RequestParam("file") MultipartFile file) {
    UploadResult result = fileUploadService.uploadFile(file);
    return ResponseEntity.ok(result);
}

// Multiple file upload
@PostMapping("/upload-multiple")
public ResponseEntity<?> uploadMultipleFiles(
        @RequestParam("files") List<MultipartFile> files) {
    List<UploadResult> results = 
        fileUploadService.uploadMultipleFiles(files);
    return ResponseEntity.ok(results);
}

// Configuration
@Bean
public MultipartConfigElement multipartConfigElement() {
    return new MultipartConfigElement(
        "uploads/temp",        // Temp location
        10 * 1024 * 1024,      // Max file size (10MB)
        50 * 1024 * 1024,      // Max request size (50MB)
        0                      // File size threshold
    );
}
```

**Validation**:
- File size limits (10MB per file)
- File type validation (MIME types)
- Empty file detection
- Filename sanitization

**REST Endpoints**:
- `POST /api/multipart-resolver/upload` - Upload single file
- `POST /api/multipart-resolver/upload-multiple` - Upload multiple files
- `GET /api/multipart-resolver/download/{filename}` - Download file
- `DELETE /api/multipart-resolver/{filename}` - Delete file
- `GET /api/multipart-resolver/files` - List all files

---

### 7. Handler Exception Resolver Pattern

**File**: `HandlerExceptionResolverPattern.java`

**Purpose**: Demonstrates comprehensive exception handling in Spring MVC.

**Key Components**:
- `@ExceptionHandler`: Handle exceptions in controllers
- `@ControllerAdvice`: Global exception handling
- `ResponseEntityExceptionHandler`: Base class for exception handlers
- `HandlerExceptionResolver`: Low-level exception resolution
- `ErrorResponse`: Structured error information
- Custom exception classes

**Use Cases**:
- Global error handling
- Custom error responses
- Exception logging
- User-friendly error messages
- API error standardization
- HTTP status code mapping

**Example**:
```java
// Global exception handler
@ControllerAdvice
public class GlobalExceptionHandler 
        extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex, 
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            ex.getMessage(),
            request.getRequestURI()
        );
        error.setDetails(ex.getErrors());
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
```

**Custom Exceptions**:
- `ResourceNotFoundException`: 404 Not Found
- `ValidationException`: 400 Bad Request
- `BusinessException`: 422 Unprocessable Entity
- `UnauthorizedException`: 401 Unauthorized

**REST Endpoints**:
- `GET /api/exception-handler/not-found` - Test 404 error
- `GET /api/exception-handler/validation-error` - Test validation error
- `GET /api/exception-handler/business-error` - Test business error
- `GET /api/exception-handler/unauthorized` - Test 401 error
- `GET /api/exception-handler/generic-error` - Test generic error

---

### 8. Bean Post Processor Pattern

**File**: `BeanPostProcessorPattern.java`

**Purpose**: Customizes beans during the initialization phase of the bean lifecycle.

**Key Components**:
- `BeanPostProcessor`: Hook into bean lifecycle
- `postProcessBeforeInitialization`: Called before init methods
- `postProcessAfterInitialization`: Called after init methods
- `Ordered`: Controls execution order
- Multiple post processors for different concerns

**Bean Lifecycle Order**:
1. Bean instantiation
2. Property population
3. **BeanPostProcessor.postProcessBeforeInitialization**
4. @PostConstruct / InitializingBean.afterPropertiesSet
5. custom init-method
6. **BeanPostProcessor.postProcessAfterInitialization**
7. Bean ready for use

**Use Cases**:
- AOP proxy creation
- Bean validation
- Dependency injection
- Bean wrapping/enhancement
- Custom annotation processing
- Metrics collection

**Example**:
```java
@Component
public class LoggingBeanPostProcessor 
        implements BeanPostProcessor, Ordered {
    
    @Override
    public Object postProcessBeforeInitialization(
            Object bean, String beanName) throws BeansException {
        System.out.println("Before init: " + beanName);
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(
            Object bean, String beanName) throws BeansException {
        System.out.println("After init: " + beanName);
        
        // Create proxy for services
        if (bean.getClass().isAnnotationPresent(Service.class)) {
            return createLoggingProxy(bean);
        }
        
        return bean;
    }
    
    @Override
    public int getOrder() {
        return 1; // Execute first
    }
}
```

**Included Post Processors**:
- `LoggingBeanPostProcessor`: Logs bean lifecycle and creates proxies
- `ValidationBeanPostProcessor`: Validates bean configuration
- `MetricsBeanPostProcessor`: Collects bean metrics
- `CustomDependencyInjectionBeanPostProcessor`: Custom DI logic

**REST Endpoints**:
- `POST /api/bean-post-processor/users` - Create user (uses enhanced service)
- `POST /api/bean-post-processor/orders` - Create order (uses enhanced service)
- `GET /api/bean-post-processor/metrics` - Get bean metrics

---

### 9. Bean Factory Post Processor Pattern

**File**: `BeanFactoryPostProcessorPattern.java`

**Purpose**: Modifies bean definitions before beans are created.

**Key Components**:
- `BeanFactoryPostProcessor`: Modifies bean definitions
- `BeanDefinitionRegistryPostProcessor`: Can add new bean definitions
- `ConfigurableListableBeanFactory`: Access to bean factory
- `BeanDefinition`: Metadata about a bean
- Property placeholder resolution

**Execution Order**:
1. **BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry**
2. **BeanDefinitionRegistryPostProcessor.postProcessBeanFactory**
3. **BeanFactoryPostProcessor.postProcessBeanFactory**
4. Bean instantiation begins
5. BeanPostProcessor methods execute

**Use Cases**:
- Property placeholder resolution
- Custom bean definition modification
- Conditional bean registration
- Bean definition validation
- Dynamic bean creation
- Bean scope modification

**Example**:
```java
@Component
public class BeanDefinitionModifierPostProcessor 
        implements BeanFactoryPostProcessor {
    
    @Override
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) 
            throws BeansException {
        
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = 
                beanFactory.getBeanDefinition(beanName);
            
            // Modify specific beans
            if (beanName.contains("Service")) {
                beanDefinition.setLazyInit(true);
            }
        }
    }
}

// Register new beans dynamically
@Component
public class DynamicBeanRegistrarPostProcessor 
        implements BeanDefinitionRegistryPostProcessor {
    
    @Override
    public void postProcessBeanDefinitionRegistry(
            BeanDefinitionRegistry registry) 
            throws BeansException {
        
        GenericBeanDefinition beanDefinition = 
            new GenericBeanDefinition();
        beanDefinition.setBeanClass(DynamicService.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        
        registry.registerBeanDefinition(
            "dynamicService", beanDefinition);
    }
}
```

**Included Post Processors**:
- `BeanDefinitionModifierPostProcessor`: Modifies existing bean definitions
- `DynamicBeanRegistrarPostProcessor`: Registers new bean definitions
- `CustomPropertyPlaceholderPostProcessor`: Custom placeholder resolution
- `BeanScopeModifierPostProcessor`: Changes bean scopes

**REST Endpoints**:
- `GET /api/bean-factory-post-processor/stats` - Get bean definition statistics
- `GET /api/bean-factory-post-processor/properties` - Get custom properties
- `GET /api/bean-factory-post-processor/modified-beans` - Get modified beans

---

### 10. Destruction Aware Bean Post Processor Pattern

**File**: `DestructionAwareBeanPostProcessorPattern.java`

**Purpose**: Executes cleanup logic before beans are destroyed during application shutdown.

**Key Components**:
- `DestructionAwareBeanPostProcessor`: Hook into bean destruction
- `postProcessBeforeDestruction`: Called before bean is destroyed
- `requiresDestruction`: Check if bean requires cleanup
- `ResourceHolder`: Interface for beans that hold resources
- Cleanup and resource release logic

**Destruction Order**:
1. Context shutdown initiated
2. **DestructionAwareBeanPostProcessor.postProcessBeforeDestruction**
3. @PreDestroy methods
4. DisposableBean.destroy()
5. custom destroy-method
6. Bean destroyed

**Use Cases**:
- Database connection cleanup
- File handle closing
- Thread pool shutdown
- Cache clearing
- Audit logging
- Resource release

**Example**:
```java
@Component
public class CleanupBeanPostProcessor 
        implements DestructionAwareBeanPostProcessor {
    
    @Override
    public void postProcessBeforeDestruction(
            Object bean, String beanName) throws BeansException {
        
        System.out.println("Cleaning up bean: " + beanName);
        
        // Perform cleanup based on bean type
        if (bean instanceof ResourceHolder) {
            ((ResourceHolder) bean).cleanup();
        }
    }
    
    @Override
    public boolean requiresDestruction(Object bean) {
        // Only process beans that need cleanup
        return bean instanceof ResourceHolder || 
               bean.getClass().isAnnotationPresent(Service.class);
    }
}

// Resource-managed service
@Service
public class ConnectionPoolService implements ResourceHolder {
    
    private final Map<String, Connection> connectionPool = 
        new ConcurrentHashMap<>();
    
    @Override
    public void cleanup() {
        System.out.println("Closing " + 
            connectionPool.size() + " connections");
        connectionPool.values().forEach(Connection::close);
        connectionPool.clear();
    }
    
    @PreDestroy
    public void shutdown() {
        System.out.println("Shutting down connection pool");
    }
}
```

**Included Post Processors**:
- `CleanupBeanPostProcessor`: Handles resource cleanup
- `LoggingDestructionBeanPostProcessor`: Logs all destructions
- `ResourceReleaseBeanPostProcessor`: Releases AutoCloseable resources

**Managed Services**:
- `ResourceManagedService`: Manages generic resources
- `ConnectionPoolService`: Manages database connections
- `FileHandlerService`: Manages file handles

**REST Endpoints**:
- `GET /api/destruction-aware/stats` - Get destruction statistics
- `POST /api/destruction-aware/use-resource` - Use resource service
- `GET /api/destruction-aware/connection` - Get connection from pool

---

## Dependencies

Add these dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Context -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
    </dependency>
    
    <!-- Servlet API (for multipart, locale, theme patterns) -->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Running the Examples

Each pattern is a standalone Spring Boot application:

```bash
# Callback Pattern
javac CallbackPattern.java
java CallbackPattern

# Template Callback Pattern
javac TemplateCallbackPattern.java
java TemplateCallbackPattern

# ResourceBundle Pattern
javac ResourceBundlePattern.java
java ResourceBundlePattern

# Locale Resolver Pattern
javac LocaleResolverPattern.java
java LocaleResolverPattern

# Theme Resolver Pattern
javac ThemeResolverPattern.java
java ThemeResolverPattern

# Multipart Resolver Pattern
javac MultipartResolverPattern.java
java MultipartResolverPattern

# Handler Exception Resolver Pattern
javac HandlerExceptionResolverPattern.java
java HandlerExceptionResolverPattern

# Bean Post Processor Pattern
javac BeanPostProcessorPattern.java
java BeanPostProcessorPattern

# Bean Factory Post Processor Pattern
javac BeanFactoryPostProcessorPattern.java
java BeanFactoryPostProcessorPattern

# Destruction Aware Bean Post Processor Pattern
javac DestructionAwareBeanPostProcessorPattern.java
java DestructionAwareBeanPostProcessorPattern
```

## Pattern Comparison

| Pattern | Purpose | When to Use | Complexity |
|---------|---------|-------------|------------|
| Callback | Async execution | Event-driven, non-blocking code | Medium |
| Template Callback | Resource management | JDBC, file I/O, HTTP | Medium |
| ResourceBundle | i18n messages | Multi-language apps | Low |
| Locale Resolver | Locale detection | Multi-region apps | Low |
| Theme Resolver | UI theming | Customizable UIs | Low |
| Multipart Resolver | File uploads | File handling apps | Low |
| Handler Exception Resolver | Error handling | All web apps | Medium |
| Bean Post Processor | Bean enhancement | AOP, validation, metrics | High |
| Bean Factory Post Processor | Bean definition modification | Dynamic configuration | High |
| Destruction Aware BPP | Resource cleanup | Connection pools, file handles | Medium |

## Best Practices

### Callback Pattern
- Keep callbacks simple and focused
- Use functional interfaces where possible
- Handle errors in callbacks appropriately
- Consider callback ordering and dependencies
- Document callback execution context

### Template Callback Pattern
- Always use templates for resource management
- Close resources in finally blocks (within template)
- Translate checked exceptions to unchecked
- Reuse callback implementations
- Keep callbacks stateless when possible

### ResourceBundle Pattern
- Organize messages by module/feature
- Use consistent key naming conventions
- Provide fallback messages
- Cache MessageSource beans
- Externalize all user-facing strings

### Locale Resolver Pattern
- Choose resolver based on persistence needs
- Use LocaleChangeInterceptor for dynamic switching
- Validate locale before setting
- Provide sensible defaults
- Support common locales

### Theme Resolver Pattern
- Keep themes consistent (same CSS classes)
- Lazy-load theme resources
- Cache theme configurations
- Validate theme names
- Provide preview functionality

### Multipart Resolver Pattern
- Validate file size before processing
- Validate file types (MIME and extension)
- Use streaming for large files
- Clean up temporary files
- Handle upload errors gracefully

### Handler Exception Resolver Pattern
- Use @ControllerAdvice for global handling
- Return consistent error response structure
- Log exceptions appropriately
- Don't expose sensitive information
- Map exceptions to appropriate HTTP status codes

### Bean Post Processor Pattern
- Order processors correctly
- Return bean even if unmodified
- Handle null beans gracefully
- Document side effects
- Keep processing fast

### Bean Factory Post Processor Pattern
- Don't instantiate beans in BFPPs
- Modify only necessary bean definitions
- Use BeanDefinitionRegistry for new beans
- Document configuration changes
- Be careful with bean dependencies

### Destruction Aware Bean Post Processor Pattern
- Implement requiresDestruction efficiently
- Handle exceptions in cleanup
- Release resources properly
- Log cleanup activities
- Don't throw exceptions from postProcessBeforeDestruction

## Common Use Cases

### Building a Multi-Language Web Application
1. Use **ResourceBundle Pattern** for messages
2. Use **Locale Resolver Pattern** for user locale detection
3. Use **Theme Resolver Pattern** for region-specific styling

### Building a File Upload System
1. Use **Multipart Resolver Pattern** for uploads
2. Use **Handler Exception Resolver Pattern** for upload errors
3. Use **Destruction Aware BPP Pattern** for cleanup

### Building a Customizable Dashboard
1. Use **Callback Pattern** for async data loading
2. Use **Template Callback Pattern** for data access
3. Use **Theme Resolver Pattern** for UI customization

### Enhancing Application Observability
1. Use **Bean Post Processor Pattern** for AOP proxies
2. Use **Bean Factory Post Processor Pattern** for configuration
3. Use **Destruction Aware BPP Pattern** for cleanup logging

## Testing

Each pattern includes REST endpoints for testing:

```bash
# Test Callback Pattern
curl -X POST http://localhost:8080/api/callback/execute \
  -H "Content-Type: application/json" \
  -d '{"operation": "test"}'

# Test ResourceBundle Pattern
curl http://localhost:8080/api/resource-bundle/message/greeting?locale=fr

# Test File Upload
curl -X POST http://localhost:8080/api/multipart-resolver/upload \
  -F "file=@document.pdf"

# Test Exception Handling
curl http://localhost:8080/api/exception-handler/not-found

# Test Bean Metrics
curl http://localhost:8080/api/bean-post-processor/metrics
```

## Production Considerations

1. **Performance**
   - Cache MessageSource lookups
   - Use async callbacks for I/O operations
   - Optimize bean post processor execution
   - Monitor file upload performance

2. **Security**
   - Validate all file uploads
   - Sanitize user inputs in error messages
   - Validate locale/theme parameters
   - Protect sensitive information in exceptions

3. **Scalability**
   - Use stateless beans when possible
   - Consider distributed caching for themes/locales
   - Implement file upload quotas
   - Monitor bean creation overhead

4. **Monitoring**
   - Track callback execution times
   - Monitor file upload success/failure rates
   - Log exception frequencies
   - Track bean lifecycle metrics

5. **Maintenance**
   - Keep resource bundles organized
   - Version control theme files
   - Document custom bean processors
   - Test cleanup logic thoroughly

## Related Patterns

- **AOP Pattern**: Often implemented using Bean Post Processors
- **Proxy Pattern**: Created in post processor after initialization
- **Strategy Pattern**: Used in Locale/Theme Resolvers
- **Template Method Pattern**: Foundation of Template Callback
- **Observer Pattern**: Similar to Callback Pattern

## References

- [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring MVC Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)
- [Bean Post Processors](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-extension-bpp)
- [Internationalization](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-functionality-messagesource)

## License

These examples are provided for educational purposes.

## Author

Spring Patterns Team

---

**Note**: All patterns include comprehensive JavaDoc, inline comments, and working examples. The compile errors shown are expected as these are demonstration files without actual Spring dependencies in the classpath.
