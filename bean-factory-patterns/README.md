# Bean Factory Patterns

Complete guide to Spring Bean Factory design patterns including factory methods and Aware interfaces.

## Table of Contents
1. [Overview](#overview)
2. [Pattern Categories](#pattern-categories)
3. [Factory Creation Patterns](#factory-creation-patterns)
4. [Aware Interface Patterns](#aware-interface-patterns)
5. [Pattern Comparison](#pattern-comparison)
6. [Best Practices](#best-practices)
7. [Testing Strategies](#testing-strategies)
8. [Summary](#summary)

## Overview

Bean Factory Patterns in Spring provide various ways to create, configure, and provide awareness to beans. These patterns fall into two main categories:

### Factory Creation Patterns
- **FactoryBean Pattern**: Interface-based factory for complex bean creation
- **Factory Method Pattern**: @Bean methods for bean creation
- **Static Factory Method Pattern**: Static methods for bean instantiation
- **Instance Factory Method Pattern**: Instance methods for bean creation

### Aware Interface Patterns
- **BeanFactoryAware**: Provides access to BeanFactory
- **ApplicationContextAware**: Provides access to ApplicationContext
- **BeanNameAware**: Provides bean name
- **BeanClassLoaderAware**: Provides ClassLoader

## Pattern Categories

### 1. Complex Bean Creation
For beans requiring complex initialization logic:
- **FactoryBean** - Most flexible, interface-based
- **Factory Method** - Simple, method-based
- **Static Factory Method** - No factory instance needed
- **Instance Factory Method** - Stateful factory

### 2. Container Awareness
For beans that need container information:
- **BeanFactoryAware** - Basic bean lookup
- **ApplicationContextAware** - Full context access
- **BeanNameAware** - Bean identification
- **BeanClassLoaderAware** - Dynamic class loading

---

## Factory Creation Patterns

### 1. FactoryBean Pattern

**Purpose**: Create beans with complex initialization logic using a dedicated factory.

**Key Characteristics**:
```java
public interface FactoryBean<T> {
    T getObject() throws Exception;
    Class<?> getObjectType();
    default boolean isSingleton() { return true; }
}
```

**Implementation Example**:
```java
@Component
class DatabaseConnectionFactoryBean implements FactoryBean<DatabaseConnection> {
    private String url;
    private String username;
    private int maxConnections;
    
    @Override
    public DatabaseConnection getObject() throws Exception {
        validateConfiguration();
        DatabaseConnection conn = new DatabaseConnection(url, username, maxConnections);
        conn.connect();
        return conn;
    }
    
    @Override
    public Class<?> getObjectType() {
        return DatabaseConnection.class;
    }
    
    @Override
    public boolean isSingleton() {
        return true;
    }
    
    private void validateConfiguration() {
        if (url == null) throw new IllegalStateException("URL required");
    }
    
    // Setters...
}
```

**Bean Access**:
```java
// Get product bean
DatabaseConnection conn = context.getBean("databaseConnection", DatabaseConnection.class);

// Get factory itself (with & prefix)
FactoryBean factory = context.getBean("&databaseConnection", FactoryBean.class);
```

**Use Cases**:
- Database connection pools
- Thread pool executors
- Complex object construction
- Third-party library integration
- Proxy generation

**Advantages**:
- ✓ Encapsulates complex creation logic
- ✓ Separation of concerns
- ✓ Reusable factory logic
- ✓ Spring container managed
- ✓ Type-safe

**When to Use**:
- Complex initialization with multiple steps
- Validation before creation
- Integration with legacy code
- Need to create proxies or wrappers

---

### 2. Factory Method Pattern

**Purpose**: Use @Bean methods in @Configuration classes to create beans.

**Key Characteristics**:
```java
@Configuration
class AppConfig {
    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```

**Implementation Example**:
```java
@Configuration
class FactoryMethodConfig {
    
    // Simple factory method
    @Bean
    public UserRepository userRepository() {
        return new UserRepository("users_table");
    }
    
    // Factory method with dependencies
    @Bean
    public UserService userService(UserRepository repository, EmailService emailService) {
        UserService service = new UserService(repository);
        service.setEmailService(emailService);
        return service;
    }
    
    // Custom bean name
    @Bean(name = "customEmailProvider")
    public EmailProvider emailProvider() {
        EmailProvider provider = new EmailProvider();
        provider.setSmtpHost("smtp.gmail.com");
        provider.setSmtpPort(587);
        return provider;
    }
    
    // With lifecycle callbacks
    @Bean(initMethod = "initialize", destroyMethod = "cleanup")
    public EmailService emailService() {
        return new EmailService("smtp.example.com", 587);
    }
    
    // Multiple beans of same type
    @Bean
    public DataSource primaryDataSource() {
        DataSource ds = new DataSource();
        ds.setUrl("jdbc:postgresql://localhost:5432/primary");
        ds.setMaxConnections(20);
        return ds;
    }
    
    @Bean
    public DataSource secondaryDataSource() {
        DataSource ds = new DataSource();
        ds.setUrl("jdbc:postgresql://localhost:5432/secondary");
        ds.setMaxConnections(10);
        return ds;
    }
}
```

**Use Cases**:
- Custom bean configuration
- Third-party library integration
- Conditional bean creation
- Multiple beans of same type with different configs

**Advantages**:
- ✓ Full control over bean creation
- ✓ Complex initialization logic
- ✓ Conditional creation
- ✓ Multiple instances
- ✓ Clear and readable

**When to Use**:
- Integrating third-party libraries
- Custom configuration needed
- Multiple instances with different settings
- Builder pattern usage

---

### 3. Static Factory Method Pattern

**Purpose**: Use static methods for bean creation without factory instance.

**Key Characteristics**:
```java
public class DatabaseConnectionPool {
    private static DatabaseConnectionPool instance;
    
    private DatabaseConnectionPool(int maxConnections) {
        // Private constructor
    }
    
    public static DatabaseConnectionPool getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionPool(50);
        }
        return instance;
    }
}

@Configuration
class Config {
    @Bean
    public DatabaseConnectionPool connectionPool() {
        return DatabaseConnectionPool.getInstance();
    }
}
```

**Implementation Example**:
```java
// Singleton pattern
class CacheManager {
    private static final Map<String, CacheManager> instances = new HashMap<>();
    
    private CacheManager(String name) {
        this.name = name;
    }
    
    public static CacheManager getOrCreate(String name) {
        return instances.computeIfAbsent(name, CacheManager::new);
    }
}

// Named constructors
class CustomerService {
    private CustomerService(Repository repo, boolean notifications, int retries) {
        // Private constructor
    }
    
    public static CustomerService createWithDefaults(Repository repo) {
        return new CustomerService(repo, true, 3);
    }
    
    public static CustomerService createForBatch(Repository repo) {
        return new CustomerService(repo, false, 1);
    }
    
    public static CustomerService createPremiumTier(Repository repo) {
        return new CustomerService(repo, true, 5);
    }
}

// Enum-like pattern
class PaymentMethod {
    private PaymentMethod(String type, String provider, double fee) {
        // Private constructor
    }
    
    public static PaymentMethod creditCard() {
        return new PaymentMethod("CREDIT_CARD", "Stripe", 2.9);
    }
    
    public static PaymentMethod paypal() {
        return new PaymentMethod("PAYPAL", "PayPal", 3.5);
    }
    
    public static PaymentMethod bitcoin() {
        return new PaymentMethod("BITCOIN", "Coinbase", 1.0);
    }
}
```

**Common Static Factory Names**:
- `getInstance()` - Singleton instance
- `create()` - New instance
- `valueOf()` - Type conversion
- `of()` - Alternative to constructor
- `from()` - Type conversion
- `newInstance()` - Guaranteed new instance

**Use Cases**:
- Singleton pattern implementation
- Object pooling
- Cached instances
- Named constructors for clarity
- Validation before creation

**Advantages**:
- ✓ No factory instance needed
- ✓ Can return cached instances
- ✓ Clear naming
- ✓ Thread-safe singleton possible
- ✓ Validation before creation

**When to Use**:
- Singleton pattern needed
- Instance caching required
- Alternative constructors for clarity
- Validation logic before creation

---

### 4. Instance Factory Method Pattern

**Purpose**: Use instance methods of a factory bean to create other beans.

**Key Characteristics**:
```java
@Configuration
class Config {
    @Bean
    public MessageSenderFactory factory() {
        MessageSenderFactory factory = new MessageSenderFactory();
        factory.setDefaultTimeout(5000);
        return factory;
    }
    
    @Bean
    public MessageSender emailSender(MessageSenderFactory factory) {
        return factory.createEmailSender("smtp.gmail.com", 587);
    }
    
    @Bean
    public MessageSender smsSender(MessageSenderFactory factory) {
        return factory.createSmsSender("api.twilio.com", "API_KEY");
    }
}
```

**Implementation Example**:
```java
class MessageSenderFactory {
    private int defaultTimeout;
    private int retryAttempts;
    
    public void setDefaultTimeout(int timeout) {
        this.defaultTimeout = timeout;
    }
    
    public void setRetryAttempts(int attempts) {
        this.retryAttempts = attempts;
    }
    
    // Instance factory method
    public MessageSender createEmailSender(String host, int port) {
        EmailMessageSender sender = new EmailMessageSender(host, port);
        sender.setTimeout(defaultTimeout);
        sender.setRetryAttempts(retryAttempts);
        return sender;
    }
    
    // Instance factory method
    public MessageSender createSmsSender(String apiUrl, String apiKey) {
        SmsMessageSender sender = new SmsMessageSender(apiUrl, apiKey);
        sender.setTimeout(defaultTimeout);
        sender.setRetryAttempts(retryAttempts);
        return sender;
    }
}

// Factory with caching
class DatabaseClientFactory {
    private final Map<String, DatabaseClient> cache = new HashMap<>();
    
    public DatabaseClient createPostgresClient(String host, int port, String db) {
        String key = "postgres:" + host + ":" + port + ":" + db;
        return cache.computeIfAbsent(key, k -> new PostgresClient(host, port, db));
    }
    
    public DatabaseClient createMongoClient(String host, int port, String db) {
        String key = "mongo:" + host + ":" + port + ":" + db;
        return cache.computeIfAbsent(key, k -> new MongoClient(host, port, db));
    }
}

// Factory with configuration
class HttpClientFactory {
    private final HttpConfiguration config;
    
    public HttpClientFactory(HttpConfiguration config) {
        this.config = config;
    }
    
    public HttpClient createRestClient() {
        RestHttpClient client = new RestHttpClient();
        client.setConnectTimeout(config.getConnectTimeout());
        client.setReadTimeout(config.getReadTimeout());
        return client;
    }
}
```

**Use Cases**:
- Stateful bean creation
- Related bean families
- Common configuration sharing
- Instance caching
- Protocol implementations

**Advantages**:
- ✓ Factory can maintain state
- ✓ Factory can have dependencies
- ✓ More flexible than static
- ✓ Can cache instances
- ✓ Can apply common configuration

**When to Use**:
- Factory needs state or dependencies
- Creating related beans (families)
- Shared configuration across products
- Instance caching needed

---

## Aware Interface Patterns

### 5. BeanFactoryAware Pattern

**Purpose**: Provide beans with access to the BeanFactory that created them.

**Key Characteristics**:
```java
public interface BeanFactoryAware extends Aware {
    void setBeanFactory(BeanFactory beanFactory) throws BeansException;
}
```

**Implementation Example**:
```java
@Component
class ServiceLocator implements BeanFactoryAware {
    private BeanFactory beanFactory;
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
    
    public Service getService(String serviceName) {
        if (beanFactory.containsBean(serviceName)) {
            return beanFactory.getBean(serviceName, Service.class);
        }
        throw new IllegalArgumentException("Service not found: " + serviceName);
    }
    
    public boolean hasService(String serviceName) {
        return beanFactory.containsBean(serviceName);
    }
    
    public Map<String, Service> getAllServices() {
        return beanFactory.getBeansOfType(Service.class);
    }
}

// Plugin Manager
@Component
class PluginManager implements BeanFactoryAware {
    private BeanFactory beanFactory;
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    
    public void loadPlugin(String pluginName) {
        if (beanFactory.containsBean(pluginName)) {
            Plugin plugin = beanFactory.getBean(pluginName, Plugin.class);
            plugin.initialize();
            plugin.start();
        }
    }
}

// Strategy Resolver
@Component
class StrategyResolver implements BeanFactoryAware {
    private BeanFactory beanFactory;
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    
    public void executeStrategy(String strategyType) {
        String beanName = strategyType + "Strategy";
        if (beanFactory.containsBean(beanName)) {
            TradingStrategy strategy = beanFactory.getBean(beanName, TradingStrategy.class);
            strategy.trade(1000.0);
        }
    }
}
```

**BeanFactory Capabilities**:
- `getBean(name)` - Retrieve bean by name
- `getBean(Class)` - Retrieve bean by type
- `containsBean(name)` - Check if bean exists
- `isSingleton(name)` - Check if singleton
- `isPrototype(name)` - Check if prototype
- `getType(name)` - Get bean type
- `getAliases(name)` - Get bean aliases

**Use Cases**:
- Service Locator pattern
- Plugin architecture
- Strategy pattern
- Dynamic bean lookup
- Bean introspection

**Advantages**:
- ✓ Dynamic bean retrieval
- ✓ Runtime bean lookup
- ✓ Flexible architecture

**Disadvantages**:
- ✗ Creates Spring coupling
- ✗ Less testable
- ✗ Hides dependencies

**Best Practices**:
- Prefer dependency injection when possible
- Use only for truly dynamic lookup
- Document why BeanFactory access needed
- Consider alternatives (ApplicationContext, Provider)

---

### 6. ApplicationContextAware Pattern

**Purpose**: Provide beans with access to the ApplicationContext (extends BeanFactory).

**Key Characteristics**:
```java
public interface ApplicationContextAware extends Aware {
    void setApplicationContext(ApplicationContext context) throws BeansException;
}
```

**Implementation Example**:
```java
// Event Publisher
@Component
class EventPublisher implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.applicationContext = context;
    }
    
    public void publishUserCreated(String email) {
        UserCreatedEvent event = new UserCreatedEvent(this, email);
        applicationContext.publishEvent(event);
    }
    
    public void publishOrderPlaced(String orderId, double amount) {
        OrderPlacedEvent event = new OrderPlacedEvent(this, orderId, amount);
        applicationContext.publishEvent(event);
    }
}

// Property Accessor
@Component
class PropertyAccessor implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.applicationContext = context;
    }
    
    public void displayProperties() {
        Environment env = applicationContext.getEnvironment();
        
        String[] profiles = env.getActiveProfiles();
        String appName = env.getProperty("spring.application.name", "MyApp");
        String port = env.getProperty("server.port", "8080");
        
        System.out.println("Active Profiles: " + String.join(", ", profiles));
        System.out.println("Application Name: " + appName);
        System.out.println("Server Port: " + port);
    }
    
    public String getProperty(String key, String defaultValue) {
        return applicationContext.getEnvironment().getProperty(key, defaultValue);
    }
}

// Resource Loader
@Component
class ResourceLoader implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.applicationContext = context;
    }
    
    public void loadResource(String location) {
        Resource resource = applicationContext.getResource(location);
        if (resource.exists()) {
            System.out.println("Resource found: " + resource.getFilename());
        }
    }
    
    public Resource[] loadResources(String pattern) throws IOException {
        return applicationContext.getResources(pattern);
    }
}
```

**ApplicationContext Capabilities (extends BeanFactory)**:
- `publishEvent()` - Publish application events
- `getEnvironment()` - Access properties and profiles
- `getResource()` - Load resources
- `getResources()` - Load multiple resources
- `getBeansOfType()` - Find beans by type
- `getBeanNamesForType()` - Get bean names
- `getId()` - Application ID
- `getStartupDate()` - Startup timestamp

**Use Cases**:
- Publishing/listening to events
- Accessing environment properties
- Loading resources
- Bean discovery
- Application-wide operations

**Best Practices**:
- Prefer specific interfaces (EnvironmentAware, ResourceLoaderAware)
- Use @EventListener instead of ApplicationListener
- Prefer @Value for properties
- Document why context access needed

---

### 7. BeanNameAware Pattern

**Purpose**: Provide beans with their name in the Spring container.

**Key Characteristics**:
```java
public interface BeanNameAware extends Aware {
    void setBeanName(String name);
}
```

**Implementation Example**:
```java
// Audit Service
@Component
class AuditService implements BeanNameAware {
    private String beanName;
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
    
    public void logAction(String action) {
        System.out.println("Bean: " + beanName);
        System.out.println("Action: " + action);
        System.out.println("Timestamp: " + LocalDateTime.now());
    }
}

// Worker with identification
class Worker implements BeanNameAware {
    private String beanName;
    private final String task;
    
    public Worker(String task) {
        this.task = task;
    }
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
    
    public void doWork() {
        System.out.println("Worker: " + beanName);
        System.out.println("Task: " + task);
    }
}

// Cache Service
@Component
class CacheService implements BeanNameAware {
    private String beanName;
    private final Map<String, Object> cache = new HashMap<>();
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
    
    public void put(String key, Object value) {
        System.out.println("Cache: " + beanName);
        cache.put(key, value);
    }
    
    public void clear() {
        System.out.println("Clearing cache: " + beanName);
        cache.clear();
    }
}
```

**Bean Name Sources**:
- `@Component("myName")` - Explicit name
- `@Bean(name = "myName")` - Bean method name
- Default: Class name (camelCase)
- Method name for @Bean methods

**Use Cases**:
- Logging with bean identification
- Self-registration
- Debugging
- Audit trails
- Metrics collection

**Best Practices**:
- Use for logging/debugging only
- Don't use for business logic
- Store name in field
- Include in toString()

---

### 8. BeanClassLoaderAware Pattern

**Purpose**: Provide beans with the ClassLoader that loaded their class.

**Key Characteristics**:
```java
public interface BeanClassLoaderAware extends Aware {
    void setBeanClassLoader(ClassLoader classLoader);
}
```

**Implementation Example**:
```java
// Dynamic Class Loader
@Component
class DynamicClassLoader implements BeanClassLoaderAware {
    private ClassLoader classLoader;
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public Class<?> loadClassByName(String className) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            System.out.println("Loaded: " + clazz.getName());
            return clazz;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    public boolean isClassAvailable(String className) {
        try {
            classLoader.loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

// Resource Loader
@Component
class ResourceLoaderService implements BeanClassLoaderAware {
    private ClassLoader classLoader;
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public URL getResource(String resourcePath) {
        return classLoader.getResource(resourcePath);
    }
    
    public InputStream getResourceAsStream(String resourcePath) {
        return classLoader.getResourceAsStream(resourcePath);
    }
}

// Plugin Loader
@Component
class PluginLoader implements BeanClassLoaderAware {
    private ClassLoader classLoader;
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public Object loadPlugin(String className) {
        try {
            Class<?> pluginClass = classLoader.loadClass(className);
            return pluginClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
    
    public void scanForPlugins(String packageName) {
        String path = packageName.replace('.', '/');
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                System.out.println("Found: " + resource);
            }
        } catch (IOException e) {
            // Handle
        }
    }
}
```

**ClassLoader Capabilities**:
- `loadClass(name)` - Load class dynamically
- `getResource(name)` - Get resource URL
- `getResourceAsStream(name)` - Get resource as stream
- `getResources(name)` - Get all matching resources
- `getParent()` - Get parent ClassLoader

**Use Cases**:
- Dynamic class loading
- Plugin systems
- Resource loading
- Reflection utilities
- Class path scanning
- Framework development

**Best Practices**:
- Cache loaded classes
- Handle ClassNotFoundException
- Consider thread context ClassLoader
- Use for framework code only

---

## Pattern Comparison

### Factory Creation Patterns

| Pattern | Factory Instance | State | Complexity | Use Case |
|---------|-----------------|-------|------------|----------|
| **FactoryBean** | Required (Spring creates) | Stateful | High | Complex bean creation, third-party integration |
| **Factory Method** | Not needed (@Bean) | Stateless | Low | Simple configuration, multiple instances |
| **Static Factory Method** | Not needed (static) | Stateless | Low | Singleton, cached instances |
| **Instance Factory Method** | Required (explicitly created) | Stateful | Medium | Related bean families, shared config |

### Aware Interface Patterns

| Pattern | Provides | Lifecycle Order | Coupling | Use Case |
|---------|----------|----------------|----------|----------|
| **BeanFactoryAware** | BeanFactory | 4th | High | Dynamic bean lookup, service locator |
| **ApplicationContextAware** | ApplicationContext | 5th | High | Events, properties, resources |
| **BeanNameAware** | Bean name | 3rd | Low | Logging, debugging, identification |
| **BeanClassLoaderAware** | ClassLoader | 3rd | Medium | Dynamic class loading, plugins |

### Bean Lifecycle Order

```
1. Constructor called
2. Dependencies injected
3. setBeanName() ← BeanNameAware
4. setBeanClassLoader() ← BeanClassLoaderAware
5. setBeanFactory() ← BeanFactoryAware
6. setApplicationContext() ← ApplicationContextAware
7. @PostConstruct methods
8. afterPropertiesSet() ← InitializingBean
9. init-method called
10. Bean ready for use
```

### Decision Matrix

**Choose FactoryBean when**:
- Complex initialization logic required
- Integration with legacy code
- Need to create proxies
- Validation before creation
- Third-party library integration

**Choose Factory Method when**:
- Simple bean configuration
- Multiple beans with different configs
- Third-party library beans
- Clear, readable configuration

**Choose Static Factory Method when**:
- Singleton pattern needed
- Instance caching required
- No factory state needed
- Thread-safe creation

**Choose Instance Factory Method when**:
- Factory needs state/dependencies
- Creating related beans
- Shared configuration
- Instance caching

**Choose BeanFactoryAware when**:
- Dynamic bean lookup required
- Service locator pattern
- Plugin architecture
- Basic bean introspection

**Choose ApplicationContextAware when**:
- Publishing events
- Accessing properties
- Loading resources
- Application-wide operations

**Choose BeanNameAware when**:
- Logging with bean name
- Debugging
- Audit trails
- Bean identification

**Choose BeanClassLoaderAware when**:
- Dynamic class loading
- Plugin system
- Resource loading
- Framework development

---

## Best Practices

### Factory Creation Best Practices

1. **Prefer Dependency Injection**
   ```java
   // ✓ Good
   @Component
   class MyService {
       private final Repository repository;
       
       public MyService(Repository repository) {
           this.repository = repository;
       }
   }
   
   // ✗ Avoid (unless truly needed)
   @Component
   class MyService implements BeanFactoryAware {
       private BeanFactory beanFactory;
       
       public void doSomething() {
           Repository repo = beanFactory.getBean(Repository.class);
       }
   }
   ```

2. **Use FactoryBean for Complex Creation**
   ```java
   // ✓ Good - Complex initialization
   class ConnectionPoolFactoryBean implements FactoryBean<ConnectionPool> {
       @Override
       public ConnectionPool getObject() throws Exception {
           validateConfig();
           ConnectionPool pool = new ConnectionPool(url, user, password);
           pool.initialize();
           pool.warmUp();
           return pool;
       }
   }
   ```

3. **Keep Factory Methods Simple**
   ```java
   // ✓ Good
   @Bean
   public UserService userService(UserRepository repository) {
       return new UserService(repository);
   }
   
   // ✗ Avoid - Too complex for factory method
   @Bean
   public UserService userService() {
       UserRepository repo = new UserRepository();
       repo.setDataSource(createDataSource());
       repo.initialize();
       EmailService email = new EmailService();
       email.configure();
       // ... more complexity
       return new UserService(repo, email);
   }
   ```

4. **Document Factory Purpose**
   ```java
   /**
    * Creates thread pool for async task execution.
    * Pool size: 10-20 threads
    * Queue capacity: 100 tasks
    * 
    * @return configured ThreadPoolExecutor
    */
   @Bean
   public ThreadPoolExecutor taskExecutor() {
       return ThreadPoolExecutor.create(10, 20, 100);
   }
   ```

### Aware Interface Best Practices

5. **Use Specific Aware Interfaces**
   ```java
   // ✓ Good - Use specific interface
   @Component
   class PropertyReader implements EnvironmentAware {
       private Environment environment;
       
       @Override
       public void setEnvironment(Environment environment) {
           this.environment = environment;
       }
   }
   
   // ✗ Avoid - Too broad
   @Component
   class PropertyReader implements ApplicationContextAware {
       private ApplicationContext context;
       
       @Override
       public void setApplicationContext(ApplicationContext context) {
           this.context = context;
       }
       
       public String getProperty(String key) {
           return context.getEnvironment().getProperty(key);
       }
   }
   ```

6. **Prefer @EventListener over ApplicationListener**
   ```java
   // ✓ Good
   @Component
   class EventHandler {
       @EventListener
       public void onUserCreated(UserCreatedEvent event) {
           // Handle event
       }
   }
   
   // ✗ Avoid
   @Component
   class EventHandler implements ApplicationListener<UserCreatedEvent> {
       @Override
       public void onApplicationEvent(UserCreatedEvent event) {
           // Handle event
       }
   }
   ```

7. **Use @Value for Properties**
   ```java
   // ✓ Good
   @Component
   class AppConfig {
       @Value("${app.name}")
       private String appName;
       
       @Value("${server.port:8080}")
       private int serverPort;
   }
   
   // ✗ Avoid
   @Component
   class AppConfig implements ApplicationContextAware {
       private ApplicationContext context;
       
       public String getAppName() {
           return context.getEnvironment().getProperty("app.name");
       }
   }
   ```

8. **Document Why Aware Interface Needed**
   ```java
   /**
    * Uses BeanFactoryAware for dynamic plugin loading.
    * Plugins are registered at runtime and looked up by name.
    * Cannot use constructor injection as plugin names are dynamic.
    */
   @Component
   class PluginManager implements BeanFactoryAware {
       private BeanFactory beanFactory;
       // ...
   }
   ```

### General Best Practices

9. **Validate in Factory Methods**
   ```java
   @Bean
   public DataSource dataSource() {
       if (url == null) {
           throw new IllegalStateException("Database URL required");
       }
       return new DataSource(url, username, password);
   }
   ```

10. **Use Meaningful Bean Names**
    ```java
    // ✓ Good
    @Bean
    public DataSource primaryDataSource() { }
    
    @Bean
    public DataSource secondaryDataSource() { }
    
    // ✗ Avoid
    @Bean
    public DataSource dataSource1() { }
    
    @Bean
    public DataSource dataSource2() { }
    ```

11. **Prefer Constructor Injection in Factories**
    ```java
    // ✓ Good - Immutable, testable
    @Bean
    public UserService userService(UserRepository repo, EmailService email) {
        return new UserService(repo, email);
    }
    
    // ✗ Avoid - Mutable, harder to test
    @Bean
    public UserService userService(UserRepository repo) {
        UserService service = new UserService();
        service.setRepository(repo);
        return service;
    }
    ```

12. **Cache Expensive Operations**
    ```java
    class ResourceFactory implements FactoryBean<Resource> {
        private Resource cachedResource;
        
        @Override
        public Resource getObject() throws Exception {
            if (cachedResource == null) {
                cachedResource = loadExpensiveResource();
            }
            return cachedResource;
        }
    }
    ```

---

## Testing Strategies

### Testing FactoryBean

```java
@Test
void testFactoryBean() throws Exception {
    DatabaseConnectionFactoryBean factory = new DatabaseConnectionFactoryBean();
    factory.setUrl("jdbc:test:mem");
    factory.setUsername("test");
    factory.setMaxConnections(5);
    
    DatabaseConnection connection = factory.getObject();
    
    assertNotNull(connection);
    assertEquals(DatabaseConnection.class, factory.getObjectType());
    assertTrue(factory.isSingleton());
}
```

### Testing Factory Method

```java
@Test
void testFactoryMethod() {
    FactoryMethodConfig config = new FactoryMethodConfig();
    UserRepository repository = new UserRepository("test_table");
    EmailService emailService = new EmailService("smtp.test", 587);
    
    UserService service = config.userService(repository, emailService);
    
    assertNotNull(service);
    assertEquals(repository, service.getRepository());
}
```

### Testing BeanFactoryAware

```java
@Test
void testBeanFactoryAware() {
    BeanFactory beanFactory = mock(BeanFactory.class);
    Service mockService = mock(Service.class);
    
    when(beanFactory.containsBean("userService")).thenReturn(true);
    when(beanFactory.getBean("userService", Service.class)).thenReturn(mockService);
    
    ServiceLocator locator = new ServiceLocator();
    locator.setBeanFactory(beanFactory);
    
    Service service = locator.getService("userService");
    assertSame(mockService, service);
}
```

### Testing ApplicationContextAware

```java
@Test
void testApplicationContextAware() {
    ApplicationContext context = mock(ApplicationContext.class);
    Environment environment = mock(Environment.class);
    
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.getProperty("app.name", "MyApp")).thenReturn("TestApp");
    
    PropertyAccessor accessor = new PropertyAccessor();
    accessor.setApplicationContext(context);
    
    assertEquals("TestApp", accessor.getProperty("app.name", "MyApp"));
}
```

### Testing BeanNameAware

```java
@Test
void testBeanNameAware() {
    AuditService service = new AuditService();
    service.setBeanName("testAuditService");
    
    assertEquals("testAuditService", service.getBeanName());
    
    // Test logging includes bean name
    service.logAction("test action");
}
```

### Testing BeanClassLoaderAware

```java
@Test
void testBeanClassLoaderAware() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    
    DynamicClassLoader loader = new DynamicClassLoader();
    loader.setBeanClassLoader(classLoader);
    
    Class<?> clazz = loader.loadClassByName("java.util.HashMap");
    
    assertNotNull(clazz);
    assertEquals("HashMap", clazz.getSimpleName());
    assertTrue(loader.isClassAvailable("java.util.HashMap"));
}
```

### Integration Testing

```java
@SpringBootTest
class BeanFactoryPatternsIntegrationTest {
    
    @Autowired
    private ApplicationContext context;
    
    @Test
    void testFactoryBeanIntegration() {
        // Get product bean
        DatabaseConnection conn = context.getBean("databaseConnection", DatabaseConnection.class);
        assertNotNull(conn);
        
        // Get factory itself
        Object factory = context.getBean("&databaseConnection");
        assertTrue(factory instanceof FactoryBean);
    }
    
    @Test
    void testBeanNameAwareIntegration() {
        AuditService service = context.getBean(AuditService.class);
        assertEquals("auditService", service.getBeanName());
    }
}
```

---

## Summary

### Quick Reference

| Pattern | Purpose | When to Use |
|---------|---------|-------------|
| **FactoryBean** | Complex bean creation | Third-party integration, complex init |
| **Factory Method** | Simple bean creation | Configuration, multiple instances |
| **Static Factory** | Singleton/cached beans | Singleton pattern, no factory state |
| **Instance Factory** | Stateful bean creation | Related beans, shared config |
| **BeanFactoryAware** | Bean lookup | Service locator, dynamic lookup |
| **ApplicationContextAware** | Full context access | Events, properties, resources |
| **BeanNameAware** | Bean identification | Logging, debugging |
| **BeanClassLoaderAware** | Class loading | Plugins, dynamic loading |

### Key Takeaways

1. **Factory Patterns**:
   - Use FactoryBean for complex initialization
   - Use Factory Method for simple configuration
   - Static factory for singleton/caching
   - Instance factory for stateful creation

2. **Aware Interfaces**:
   - Prefer dependency injection over Aware interfaces
   - Use specific Aware interfaces (not ApplicationContext for everything)
   - Document why Aware interface is needed
   - Consider alternatives (@Value, @EventListener, etc.)

3. **Best Practices**:
   - Keep factory methods simple
   - Validate configuration early
   - Use meaningful bean names
   - Prefer constructor injection
   - Test factory logic independently

4. **Anti-Patterns to Avoid**:
   - Using Aware interfaces for regular dependencies
   - Complex logic in factory methods
   - Service locator as default pattern
   - Hiding dependencies behind dynamic lookup

### Resources

- [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Spring FactoryBean JavaDoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/FactoryBean.html)
- [Spring Aware Interfaces](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aware-list)
- [Effective Java by Joshua Bloch](https://www.oreilly.com/library/view/effective-java/9780134686097/) (Static Factory Methods)

---

**Total Patterns**: 8  
**Factory Creation**: 4 patterns  
**Aware Interfaces**: 4 patterns  
**Files**: 8 Java files + README
