# Bean Wiring Patterns in Spring

Comprehensive guide to dependency injection and autowiring patterns in Spring Framework.

## Table of Contents

1. [Overview](#overview)
2. [Pattern Categories](#pattern-categories)
3. [Autowiring by Type](#autowiring-by-type)
4. [Autowiring by Name](#autowiring-by-name)
5. [Autowiring by Constructor](#autowiring-by-constructor)
6. [Autowiring by Qualifier](#autowiring-by-qualifier)
7. [Autowiring by Primary](#autowiring-by-primary)
8. [Collection Autowiring](#collection-autowiring)
9. [Map Autowiring](#map-autowiring)
10. [Optional Autowiring](#optional-autowiring)
11. [Lazy Autowiring](#lazy-autowiring)
12. [Pattern Comparison](#pattern-comparison)
13. [Best Practices](#best-practices)
14. [Testing Strategies](#testing-strategies)

---

## Overview

**Bean Wiring** is the process of connecting beans (dependencies) together in Spring's IoC container. Spring provides multiple strategies for dependency injection, each suited for different scenarios.

### Why Bean Wiring Matters

- **Loose Coupling**: Depends on abstractions, not implementations
- **Testability**: Easy to mock and test dependencies
- **Maintainability**: Clear dependency relationships
- **Flexibility**: Change implementations without modifying code
- **Dependency Management**: Spring handles bean lifecycle

### Injection Methods

1. **Field Injection**: Direct field autowiring
2. **Setter Injection**: Via setter methods
3. **Constructor Injection**: Via constructor (recommended)
4. **Method Injection**: Via any method

---

## Pattern Categories

### Basic Wiring Patterns
- **Autowiring by Type**: Match by bean type
- **Autowiring by Name**: Match by bean name
- **Autowiring by Constructor**: Constructor-based injection

### Disambiguation Patterns
- **Autowiring by Qualifier**: Explicit bean selection
- **Autowiring by Primary**: Default bean selection

### Collection Patterns
- **Collection Autowiring**: Inject List/Set of beans
- **Map Autowiring**: Inject Map<String, Bean>

### Advanced Patterns
- **Optional Autowiring**: Optional dependencies
- **Lazy Autowiring**: Delayed initialization

---

## Autowiring by Type

**File**: `AutowiringByTypePattern.java`

### Description

Spring autowires beans by matching the type of the dependency. This is the default and most common autowiring strategy.

### Characteristics

- ✅ Matches bean by type (class or interface)
- ✅ Default autowiring mode
- ✅ Works with `@Autowired`, `@Inject`
- ❌ Fails if multiple beans of same type exist (without `@Primary` or `@Qualifier`)

### Usage Example

```java
@Service
class UserService {
    
    // Field injection
    @Autowired
    private UserRepository userRepository;
    
    // Setter injection
    private EmailService emailService;
    
    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    // Constructor injection (recommended)
    private final NotificationService notificationService;
    
    @Autowired // Optional in Spring 4.3+ for single constructor
    public UserService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```

### When to Use

- ✓ Single implementation of an interface
- ✓ Clear type-based dependency resolution
- ✓ Standard dependency injection
- ✓ Service layer wiring

### Advantages

- Type-safe
- Simple and intuitive
- IDE support (refactoring, navigation)
- Works with interfaces

### Limitations

- Fails with multiple beans of same type
- Field injection hides dependencies

---

## Autowiring by Name

**File**: `AutowiringByNamePattern.java`

### Description

Spring autowires beans by matching the name of the dependency field/parameter with the bean name in the application context.

### Characteristics

- ✅ Matches bean by name (field/parameter name = bean name)
- ✅ Uses `@Resource` annotation (JSR-250)
- ✅ Fallback to type matching if name doesn't match
- ✅ Useful when multiple beans of same type exist

### Usage Example

```java
@Configuration
class Config {
    
    @Bean(name = "creditCardPayment")
    public PaymentService creditCardPaymentService() {
        return new CreditCardPaymentService();
    }
    
    @Bean(name = "paypalPayment")
    public PaymentService paypalPaymentService() {
        return new PayPalPaymentService();
    }
}

@Service
class PaymentProcessor {
    
    // Autowire by name - matches "creditCardPayment" bean
    @Resource(name = "creditCardPayment")
    private PaymentService paymentService;
}
```

### Bean Naming Convention

```java
// Default: class name with first letter lowercase
@Component
class UserService { }  // Bean name: "userService"

// Custom: specified in annotation
@Component("customName")
class UserService { }  // Bean name: "customName"

// Multiple names (aliases)
@Component(value = {"primary", "main", "default"})
class UserService { }  // Names: "primary", "main", "default"
```

### @Resource vs @Autowired

| Feature | @Resource | @Autowired |
|---------|-----------|------------|
| Standard | JSR-250 | Spring-specific |
| Match by | Name first, then type | Type only |
| Qualifier | name attribute | @Qualifier needed |
| Required | No option | required attribute |

### When to Use

- ✓ Multiple implementations of same interface
- ✓ Selecting specific implementation by name
- ✓ JSR-250 compliance required
- ✓ Avoiding `@Qualifier` verbosity

---

## Autowiring by Constructor

**File**: `AutowiringByConstructorPattern.java`

### Description

Constructor-based dependency injection is the **RECOMMENDED** approach in Spring. Dependencies are injected through the class constructor.

### Characteristics

- ✅ Dependencies are **IMMUTABLE** (final fields)
- ✅ All required dependencies must be satisfied
- ✅ Better testability (can instantiate without Spring)
- ✅ Makes dependencies explicit
- ✅ Thread-safe initialization
- ✅ `@Autowired` optional since Spring 4.3 for single constructor

### Usage Example

```java
// Single constructor - @Autowired optional
@Service
class UserService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    // @Autowired optional for single constructor
    public UserService(UserRepository userRepository,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}

// Multiple constructors - @Autowired required
@Service
class OrderService {
    
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    @Autowired // REQUIRED when multiple constructors
    public OrderService(UserRepository userRepository,
                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    
    // Secondary constructor for testing
    public OrderService(UserRepository userRepository) {
        this(userRepository, new DefaultNotificationService());
    }
}

// Optional dependencies
@Service
class ReportService {
    
    private final UserRepository userRepository;
    private final Optional<EmailService> emailService;
    
    public ReportService(UserRepository userRepository,
                        Optional<EmailService> emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}
```

### Injection Type Comparison

| Injection Type | Immutable | Easy Testing | Hides Dependencies | Circular Deps |
|---------------|-----------|--------------|-------------------|---------------|
| Constructor | ✅ Yes | ✅ Easy | ❌ No | Prevented |
| Field | ❌ No | ❌ Hard | ✅ Yes | Possible |
| Setter | ❌ No | ⚠️ Medium | ⚠️ Somewhat | Possible |

### Testing with Constructor Injection

```java
@Test
void testUserService() {
    // Easy instantiation without Spring
    UserRepository mockRepo = mock(UserRepository.class);
    EmailService mockEmail = mock(EmailService.class);
    
    UserService service = new UserService(mockRepo, mockEmail);
    
    // Test...
}
```

### When to Use

- ✓ All service layer classes (recommended)
- ✓ Controllers
- ✓ Any class with mandatory dependencies
- ✓ Immutable configurations

### Advantages

- Immutability (final fields)
- Null-safety (NPE impossible)
- Clear contract (all dependencies visible)
- Fail-fast (missing dependencies detected at startup)
- Easy to test

### Best Practices

- ✓ Use constructor injection by default
- ✓ Make dependencies final
- ✓ Avoid too many dependencies (>5 suggests SRP violation)
- ✓ Use `Optional<T>` for optional dependencies
- ✓ Keep one constructor for most cases

---

## Autowiring by Qualifier

**File**: `AutowiringByQualifierPattern.java`

### Description

`@Qualifier` disambiguates which bean to inject when multiple beans of the same type exist in the application context.

### Characteristics

- ✅ Resolves ambiguity for multiple beans of same type
- ✅ Works with `@Autowired`, `@Inject`
- ✅ Can be used on fields, parameters, methods
- ✅ Can create custom qualifier annotations (type-safe)
- ✅ More explicit than `@Primary`

### Usage Example

#### String-based Qualifiers

```java
@Configuration
class Config {
    
    @Bean
    @Qualifier("stripe")
    public PaymentGateway stripeGateway() {
        return new StripePaymentGateway();
    }
    
    @Bean
    @Qualifier("paypal")
    public PaymentGateway paypalGateway() {
        return new PayPalPaymentGateway();
    }
}

@Service
class PaymentProcessor {
    
    private final PaymentGateway primaryGateway;
    private final PaymentGateway fallbackGateway;
    
    @Autowired
    public PaymentProcessor(@Qualifier("stripe") PaymentGateway primaryGateway,
                           @Qualifier("paypal") PaymentGateway fallbackGateway) {
        this.primaryGateway = primaryGateway;
        this.fallbackGateway = fallbackGateway;
    }
}
```

#### Custom Qualifier Annotations (Type-safe)

```java
// Define custom qualifiers
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@interface HighPriority { }

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@interface LowPriority { }

// Use in configuration
@Bean
@HighPriority
public MessageQueue highPriorityQueue() {
    return new MessageQueue("high-priority", 1000);
}

@Bean
@LowPriority
public MessageQueue lowPriorityQueue() {
    return new MessageQueue("low-priority", 100);
}

// Inject with custom qualifiers
@Service
class NotificationManager {
    
    private final MessageQueue urgentQueue;
    private final MessageQueue normalQueue;
    
    @Autowired
    public NotificationManager(@HighPriority MessageQueue urgentQueue,
                              @LowPriority MessageQueue normalQueue) {
        this.urgentQueue = urgentQueue;
        this.normalQueue = normalQueue;
    }
}
```

### Qualifier Resolution Order

1. `@Qualifier` match
2. `@Primary` bean
3. Bean name match
4. Throw `NoUniqueBeanDefinitionException`

### When to Use

- ✓ Multiple databases (read/write replicas)
- ✓ Multiple cache implementations
- ✓ Different message queues (priority levels)
- ✓ Multiple payment gateways
- ✓ Various notification channels

### Advantages

- Explicit bean selection
- Type-safe with custom annotations
- Self-documenting code
- Compile-time checking (custom qualifiers)

---

## Autowiring by Primary

**File**: `AutowiringByPrimaryPattern.java`

### Description

`@Primary` annotation marks a bean as the default choice when multiple beans of the same type exist and no explicit `@Qualifier` is specified.

### Characteristics

- ✅ Resolves ambiguity without explicit `@Qualifier`
- ✅ Only ONE bean can be `@Primary` per type
- ✅ Works with `@Bean`, `@Component`, `@Service`, etc.
- ✅ Can be overridden with `@Qualifier`
- ✅ Provides sensible defaults

### Usage Example

```java
@Configuration
class Config {
    
    // PostgreSQL is primary database
    @Bean
    @Primary
    public DatabaseClient postgresqlClient() {
        return new PostgreSQLClient();
    }
    
    @Bean
    public DatabaseClient mysqlClient() {
        return new MySQLClient();
    }
    
    @Bean
    public DatabaseClient mongoClient() {
        return new MongoDBClient();
    }
}

@Service
class UserService {
    
    // Injects PRIMARY bean (PostgreSQL) automatically
    private final DatabaseClient databaseClient;
    
    @Autowired
    public UserService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }
}

@Service
class AnalyticsService {
    
    // Uses PRIMARY PostgreSQL
    private final DatabaseClient mainDatabase;
    
    // Explicitly uses MongoDB (overrides Primary)
    private final DatabaseClient analyticsDatabase;
    
    @Autowired
    public AnalyticsService(DatabaseClient mainDatabase,
                           @Qualifier("mongoClient") DatabaseClient analyticsDatabase) {
        this.mainDatabase = mainDatabase;
        this.analyticsDatabase = analyticsDatabase;
    }
}
```

### Primary with Component

```java
@Component
@Primary
class EmailNotificationService implements NotificationService {
    // Default notification service
}

@Component
class SmsNotificationService implements NotificationService {
    // Alternative notification service
}

@Service
class NotificationDispatcher {
    
    // Injects PRIMARY (EmailNotificationService)
    private final NotificationService primaryNotification;
    
    @Autowired
    public NotificationDispatcher(NotificationService primaryNotification) {
        this.primaryNotification = primaryNotification;
    }
}
```

### Primary vs Qualifier

| Feature | @Primary | @Qualifier |
|---------|----------|------------|
| Selection | Implicit default | Explicit selection |
| Injection Point | No annotation needed | Annotation at injection point |
| Flexibility | Single default | Multiple selections |
| Override | Can be overridden by @Qualifier | Final choice |
| Count | Only one per type | Multiple possible |

### When to Use

- ✓ Clear default implementation exists
- ✓ Most use cases need same implementation
- ✓ Reduce `@Qualifier` verbosity
- ✓ Provide sensible defaults

### Common Use Cases

- Primary database (PostgreSQL vs MySQL vs MongoDB)
- Default cache (Redis vs Memcached)
- Preferred payment gateway (Stripe vs PayPal)
- Main notification channel (Email vs SMS)

---

## Collection Autowiring

**File**: `CollectionAutowiringPattern.java`

### Description

Spring can automatically inject collections of beans (`List`, `Set`, `Array`) when multiple beans of the same type exist.

### Characteristics

- ✅ Injects ALL beans of specified type
- ✅ Supports `List<T>`, `Set<T>`, `T[]`
- ✅ Maintains insertion order for List
- ✅ Removes duplicates for Set
- ✅ Can use `@Order` to control ordering
- ✅ Empty collection if no beans found

### Usage Example

```java
// Define multiple implementations
@Component
@Order(1)
class EmailNotification implements NotificationChannel { }

@Component
@Order(2)
class SmsNotification implements NotificationChannel { }

@Component
@Order(3)
class PushNotification implements NotificationChannel { }

// Inject as List (ordered)
@Service
class NotificationManager {
    
    private final List<NotificationChannel> channels;
    
    @Autowired
    public NotificationManager(List<NotificationChannel> channels) {
        this.channels = channels;
        // channels contains all 3 implementations in order
    }
    
    public void sendNotification(String message) {
        channels.forEach(channel -> channel.send(message));
    }
}

// Inject as Set (unique)
@Service
class PaymentService {
    
    private final Set<PaymentGateway> gateways;
    
    @Autowired
    public PaymentService(Set<PaymentGateway> gateways) {
        this.gateways = gateways;
    }
}

// Inject as Array
@Service
class GatewayManager {
    
    private final PaymentGateway[] gateways;
    
    @Autowired
    public GatewayManager(PaymentGateway[] gateways) {
        this.gateways = gateways;
    }
}
```

### Ordering with @Order

```java
@Component
@Order(1)  // Lower value = higher priority
class HighPriorityValidator implements Validator { }

@Component
@Order(10)
class LowPriorityValidator implements Validator { }

@Service
class ValidationService {
    
    private final List<Validator> validators;
    
    @Autowired
    public ValidationService(List<Validator> validators) {
        this.validators = validators;
        // validators[0] = HighPriorityValidator
        // validators[1] = LowPriorityValidator
    }
}
```

### Collection Type Comparison

| Type | Ordered | Duplicates | Use Case |
|------|---------|-----------|----------|
| `List<T>` | ✅ Yes | ✅ Allowed | Ordered processing, filter chains |
| `Set<T>` | ❌ No | ❌ Removed | Unique beans, plugin registry |
| `T[]` | ✅ Yes | ✅ Allowed | Array operations, fallback chain |

### Use Cases

- ✓ Plugin architecture (load all plugins)
- ✓ Event handlers (notify all listeners)
- ✓ Validation rules (run all validators)
- ✓ Filter chains (apply all filters)
- ✓ Notification channels (send to all)
- ✓ Strategy pattern (try multiple strategies)

### Advantages

- Automatic discovery of implementations
- No manual registration needed
- Extensible (add new beans → auto-injected)
- Type-safe
- Supports ordering

---

## Map Autowiring

**File**: `MapAutowiringPattern.java`

### Description

Spring can inject a `Map<String, T>` where:
- **Key** = Bean name (String)
- **Value** = Bean instance (T)

This allows runtime selection of beans by name.

### Characteristics

- ✅ Injects ALL beans of type T as Map entries
- ✅ Key is the bean name
- ✅ Value is the bean instance
- ✅ Empty map if no beans found
- ✅ Runtime bean selection

### Usage Example

```java
@Configuration
class Config {
    
    @Bean(name = "stripe")
    public PaymentProcessor stripeProcessor() {
        return new StripeProcessor();
    }
    
    @Bean(name = "paypal")
    public PaymentProcessor paypalProcessor() {
        return new PayPalProcessor();
    }
    
    @Bean(name = "square")
    public PaymentProcessor squareProcessor() {
        return new SquareProcessor();
    }
}

@Service
class PaymentService {
    
    // Inject ALL PaymentProcessor beans as a Map
    private final Map<String, PaymentProcessor> processors;
    
    @Autowired
    public PaymentService(Map<String, PaymentProcessor> processors) {
        this.processors = processors;
        // Map contains: {"stripe": StripeProcessor, "paypal": PayPalProcessor, ...}
    }
    
    public String processPayment(String processorName, double amount) {
        PaymentProcessor processor = processors.get(processorName);
        if (processor != null) {
            return processor.process(amount);
        }
        return "Processor not found: " + processorName;
    }
    
    public void showAvailableProcessors() {
        processors.keySet().forEach(name -> 
            System.out.println("Available: " + name)
        );
    }
}
```

### Bean Name Mapping

```java
// Default name (uncapitalized class name)
@Component
class MyService { }
// Map key: "myService"

// Custom name
@Component("customName")
class MyService { }
// Map key: "customName"

// Bean definition name
@Bean(name = "specificName")
public Service service() { }
// Map key: "specificName"
```

### Runtime Selection Patterns

```java
@Service
class FormatterService {
    
    private final Map<String, DataFormatter> formatters;
    
    @Autowired
    public FormatterService(Map<String, DataFormatter> formatters) {
        this.formatters = formatters;
    }
    
    public String formatData(String format, Object data) {
        DataFormatter formatter = formatters.get(format);
        return formatter != null ? formatter.format(data) : "Unknown format";
    }
    
    public String formatInAllFormats(Object data) {
        StringBuilder result = new StringBuilder();
        formatters.forEach((name, formatter) -> {
            result.append(name).append(": ").append(formatter.format(data)).append("\n");
        });
        return result.toString();
    }
}
```

### Use Cases

- ✓ Plugin registry (name → plugin)
- ✓ Strategy pattern (type → strategy)
- ✓ Handler mapping (event → handler)
- ✓ Format selection (format → formatter)
- ✓ Validator registry (type → validator)

### Advantages

- Runtime bean selection by name
- Clear mapping (name → implementation)
- Easy to list available beans
- Flexible strategy selection
- Type-safe

### Map vs List vs Qualifier

| Pattern | Selection Time | Use Case |
|---------|---------------|----------|
| `Map<String, T>` | Runtime | Dynamic bean selection by name |
| `List<T>` | Compile-time | Process all beans in order |
| `@Qualifier` | Compile-time | Single specific bean |

---

## Optional Autowiring

**File**: `OptionalAutowiringPattern.java`

### Description

Spring supports optional dependencies that may or may not be present:
1. `Optional<T>` - Java 8 Optional wrapper
2. `@Autowired(required = false)` - Bean can be null
3. `@Nullable` - Parameter can be null

### Characteristics

- ✅ Dependency is NOT required
- ✅ Application starts even if bean missing
- ✅ Prevents `NoSuchBeanDefinitionException`
- ✅ Graceful degradation
- ✅ Feature toggles

### Usage Examples

#### Optional<T> Approach (Recommended)

```java
@Service
class UserService {
    
    private final EmailService emailService; // Required
    private final Optional<SmsService> smsService; // Optional
    private final Optional<PushService> pushService; // Optional
    
    @Autowired
    public UserService(EmailService emailService,
                      Optional<SmsService> smsService,
                      Optional<PushService> pushService) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.pushService = pushService;
    }
    
    public String createUser(String email, String name) {
        // Required service - always called
        emailService.sendEmail(email, "Welcome");
        
        // Optional services - called only if present
        smsService.ifPresent(service -> 
            service.sendSms(email, "Welcome via SMS")
        );
        
        pushService.ifPresent(service -> 
            service.sendPush(email, "Welcome push")
        );
        
        return "USER-123";
    }
}
```

#### @Autowired(required = false) Approach

```java
@Service
class OrderService {
    
    private final EmailService emailService;
    
    @Autowired(required = false)
    private CacheService cacheService; // Can be null
    
    @Autowired(required = false)
    private SmsService smsService; // Can be null
    
    public String createOrder(String userId, String product) {
        emailService.sendEmail(userId, "Order created");
        
        // Check if cache is available
        if (cacheService != null) {
            cacheService.put("order", product);
        } else {
            System.out.println("Cache not available");
        }
        
        return "ORDER-123";
    }
}
```

#### @Nullable Approach

```java
@Service
class PaymentService {
    
    private final EmailService emailService;
    private final SmsService smsService; // Can be null
    
    @Autowired
    public PaymentService(EmailService emailService,
                         @Nullable SmsService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }
    
    public String processPayment(String userId, double amount) {
        emailService.sendEmail(userId, "Payment processed");
        
        if (smsService != null) {
            smsService.sendSms(userId, "Payment confirmed");
        }
        
        return "PAYMENT-123";
    }
}
```

### Optional Methods

```java
Optional<T> optional;

// Check and use
optional.ifPresent(t -> t.doSomething());

// Check presence
boolean present = optional.isPresent();

// Get with default
T value = optional.orElse(defaultValue);

// Get with supplier
T value = optional.orElseGet(() -> createDefault());

// Transform
Optional<R> result = optional.map(t -> transform(t));

// Filter
Optional<T> filtered = optional.filter(t -> condition(t));

// Throw if absent
T value = optional.orElseThrow(() -> new Exception());
```

### Approach Comparison

| Approach | Null-Safe | Functional Style | Java Version | IDE Support |
|----------|-----------|------------------|--------------|-------------|
| `Optional<T>` | ✅ Yes | ✅ Yes | Java 8+ | ✅ Good |
| `@Autowired(required=false)` | ❌ No | ❌ No | All | ✅ Good |
| `@Nullable` | ⚠️ Warning | ❌ No | All | ✅ Excellent |

### When to Use Optional Dependencies

- ✓ Feature flags/toggles
- ✓ Environment-specific features (dev vs prod)
- ✓ Optional integrations (monitoring, caching)
- ✓ Backward compatibility
- ✓ Graceful degradation

### When NOT to Use

- ✗ Core business logic
- ✗ Required functionality
- ✗ Lazy initialization (use `@Lazy` instead)

---

## Lazy Autowiring

**File**: `LazyAutowiringPattern.java`

### Description

`@Lazy` annotation delays bean initialization until first access. By default, Spring creates all singleton beans at startup (eager initialization).

### Characteristics

- ✅ Bean created on FIRST ACCESS, not at startup
- ✅ Reduces startup time
- ✅ Saves memory if bean never used
- ✅ Creates proxy for lazy dependencies
- ✅ Can break circular dependencies

### Usage Examples

#### Lazy Bean Definition

```java
@Component
@Lazy
class DatabaseService {
    
    public DatabaseService() {
        System.out.println("DatabaseService created on first access");
        connectToDatabase();
    }
    
    private void connectToDatabase() {
        // Heavy initialization
    }
}
```

#### Lazy Dependency Injection

```java
@Service
class UserService {
    
    // Eager dependency - created at startup
    private final EagerService eagerService;
    
    // Lazy dependency - created on first use
    @Lazy
    @Autowired
    private LazyService lazyService;
    
    // Lazy dependency via constructor
    private final HeavyService heavyService;
    
    @Autowired
    public UserService(EagerService eagerService,
                      @Lazy HeavyService heavyService) {
        this.eagerService = eagerService;
        this.heavyService = heavyService; // Proxy injected
    }
    
    public void processUser() {
        eagerService.process(); // Already created
        
        // LazyService created NOW (first access)
        lazyService.process();
        
        // HeavyService created NOW (first access)
        heavyService.processHeavyTask();
    }
}
```

#### Breaking Circular Dependencies

```java
@Service
class OrderService {
    
    private final PaymentService paymentService;
    
    @Autowired
    public OrderService(@Lazy PaymentService paymentService) {
        this.paymentService = paymentService; // Proxy injected
    }
}

@Service
class PaymentService {
    
    private final OrderService orderService;
    
    @Autowired
    public PaymentService(@Lazy OrderService orderService) {
        this.orderService = orderService; // Proxy injected
    }
}
// @Lazy breaks the circular dependency cycle
```

#### Lazy Configuration

```java
@Configuration
@Lazy  // ALL beans in this config are lazy
class LazyConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        return new CacheManager();
    }
    
    @Bean
    public MonitoringService monitoringService() {
        return new MonitoringService();
    }
}
```

### Lazy vs Eager Comparison

| Aspect | Eager (Default) | Lazy |
|--------|----------------|------|
| Creation Time | At startup | On first access |
| Startup Time | Slower | Faster |
| Memory Usage | Higher (all beans) | Lower (only used beans) |
| Error Detection | Startup (fail-fast) | Runtime (delayed) |
| First Access | Fast | Slower |
| Subsequent Access | Fast | Fast |

### When to Use @Lazy

- ✓ Heavy initialization (DB connections, file I/O)
- ✓ Conditional usage (may not be needed)
- ✓ Break circular dependencies
- ✓ Improve startup time
- ✓ Development/testing environments
- ✓ Optional features

### Proxy Mechanism

```
@Lazy Injection Flow:
1. Spring creates CGLIB proxy at startup
2. Proxy injected into dependent bean
3. First method call on proxy triggers bean creation
4. Proxy delegates to real bean
5. Subsequent calls use same real instance
```

### Performance Considerations

**Startup Time**:
- Lazy: ⬇️ Faster startup
- Eager: ⬆️ Slower startup

**Runtime Performance**:
- Lazy: ⬇️ First access slower
- Eager: ✅ Consistent performance

**Memory**:
- Lazy: ⬇️ Lower if bean unused
- Eager: ⬆️ Fixed memory usage

### Common Pitfalls

- ✗ Overusing `@Lazy` (delayed error detection)
- ✗ Using `@Lazy` for simple beans
- ✗ Not considering first access performance
- ✗ Circular dependencies without `@Lazy`
- ✗ Assuming lazy beans are created per access (they're singletons)

---

## Pattern Comparison

### Disambiguation Patterns

| Pattern | Selection | Annotation Location | Override | Count | Use Case |
|---------|-----------|---------------------|----------|-------|----------|
| **@Qualifier** | Explicit | Injection point | Final | Multiple | Explicit bean selection |
| **@Primary** | Implicit | Bean definition | Can override | One | Default choice |
| **By Name** | Explicit | Injection point | Final | N/A | JSR-250 compliance |

### Collection Patterns

| Pattern | Return Type | Selection | Key/Order | Use Case |
|---------|-------------|-----------|-----------|----------|
| **List<T>** | List | All beans | Ordered (@Order) | Sequential processing |
| **Set<T>** | Set | All beans (unique) | Unordered | Unique beans |
| **Map<String,T>** | Map | All beans | Name → Bean | Runtime selection |

### Initialization Patterns

| Pattern | Creation Time | Memory | Startup | First Access | Use Case |
|---------|--------------|--------|---------|--------------|----------|
| **Eager** | Startup | High | Slow | Fast | Core services |
| **Lazy** | First access | Low | Fast | Slow | Heavy/optional |
| **Optional** | If available | Varies | Fast | Varies | Feature toggles |

### Injection Method Comparison

| Method | Immutable | Testable | Dependencies Visible | Recommended |
|--------|-----------|----------|---------------------|-------------|
| **Constructor** | ✅ Yes | ✅ Easy | ✅ Yes | ✅ Yes |
| **Field** | ❌ No | ❌ Hard | ❌ Hidden | ❌ No |
| **Setter** | ❌ No | ⚠️ Medium | ⚠️ Partial | ⚠️ Optional deps only |

---

## Best Practices

### General Wiring Practices

1. **Use Constructor Injection by Default**
   ```java
   // ✅ Good
   @Service
   class UserService {
       private final Repository repo;
       
       public UserService(Repository repo) {
           this.repo = repo;
       }
   }
   
   // ❌ Bad
   @Service
   class UserService {
       @Autowired
       private Repository repo;
   }
   ```

2. **Make Dependencies Final**
   ```java
   // ✅ Good - Immutable
   private final Repository repo;
   
   // ❌ Bad - Mutable
   @Autowired
   private Repository repo;
   ```

3. **Limit Constructor Parameters**
   ```java
   // ⚠️ Warning - Too many dependencies (SRP violation)
   public UserService(Repo1 r1, Repo2 r2, Service1 s1, 
                     Service2 s2, Service3 s3, Service4 s4) {
       // Refactor to smaller services
   }
   ```

### Disambiguation Best Practices

4. **Use @Primary for Common Use Case**
   ```java
   // ✅ Good
   @Bean
   @Primary
   public Database postgresql() { }
   
   @Bean
   public Database mysql() { }
   ```

5. **Use Custom Qualifiers for Type Safety**
   ```java
   // ✅ Good - Type-safe
   @Qualifier
   @interface ReadDatabase { }
   
   @Qualifier
   @interface WriteDatabase { }
   
   // ❌ Okay but less type-safe
   @Qualifier("readDb")
   @Qualifier("writeDb")
   ```

6. **Combine @Primary with @Qualifier**
   ```java
   @Bean
   @Primary
   @Qualifier("main")
   public Service mainService() { }
   
   @Bean
   @Qualifier("backup")
   public Service backupService() { }
   ```

### Collection Best Practices

7. **Use List for Ordered Processing**
   ```java
   // ✅ Good for filter chains
   @Autowired
   private List<Filter> filters;
   
   public void process(Request req) {
       for (Filter f : filters) {
           req = f.apply(req);
       }
   }
   ```

8. **Use Map for Runtime Selection**
   ```java
   // ✅ Good for strategy pattern
   @Autowired
   private Map<String, PaymentProcessor> processors;
   
   public void pay(String type, double amount) {
       processors.get(type).process(amount);
   }
   ```

9. **Handle Empty Collections**
   ```java
   // ✅ Good
   @Autowired
   private List<Plugin> plugins;
   
   public void execute() {
       if (plugins.isEmpty()) {
           log.warn("No plugins available");
           return;
       }
       plugins.forEach(Plugin::run);
   }
   ```

### Optional Dependency Best Practices

10. **Use Optional<T> for Clarity**
    ```java
    // ✅ Good
    @Autowired
    public Service(Optional<Cache> cache) {
        cache.ifPresent(c -> c.warmUp());
    }
    
    // ❌ Less clear
    @Autowired(required = false)
    private Cache cache;
    ```

11. **Provide Fallback Behavior**
    ```java
    // ✅ Good
    @Autowired
    public Service(Optional<SmsService> sms, EmailService email) {
        this.notification = sms.orElse(email);
    }
    ```

### Lazy Loading Best Practices

12. **Use @Lazy for Heavy Resources**
    ```java
    // ✅ Good
    @Lazy
    @Component
    class DatabaseConnectionPool {
        public DatabaseConnectionPool() {
            // Heavy: load 100 connections
        }
    }
    ```

13. **Document Why Bean is Lazy**
    ```java
    /**
     * Lazy initialization because:
     * - Heavy startup cost (5-10 seconds)
     * - Only needed for batch jobs
     * - Not used in 80% of requests
     */
    @Lazy
    @Component
    class BatchProcessor { }
    ```

14. **Don't Overuse @Lazy**
    ```java
    // ✅ Good - Simple bean, should be eager
    @Component
    class StringUtils { }
    
    // ❌ Bad - Unnecessary lazy
    @Lazy
    @Component
    class StringUtils { }
    ```

### Testing Best Practices

15. **Leverage Constructor Injection for Testing**
    ```java
    @Test
    void test() {
        // ✅ Easy - no Spring needed
        Repository mockRepo = mock(Repository.class);
        Service service = new Service(mockRepo);
        
        // Test...
    }
    ```

16. **Test Optional Dependencies Both Ways**
    ```java
    @Test
    void testWithOptionalPresent() {
        Service s = new Service(Optional.of(mock(Cache.class)));
        // Test caching behavior
    }
    
    @Test
    void testWithOptionalAbsent() {
        Service s = new Service(Optional.empty());
        // Test fallback behavior
    }
    ```

### Naming Best Practices

17. **Use Descriptive Bean Names**
    ```java
    // ✅ Good
    @Bean(name = "primaryDatabaseConnection")
    @Bean(name = "readReplicaConnection")
    
    // ❌ Bad
    @Bean(name = "db1")
    @Bean(name = "db2")
    ```

18. **Follow Naming Conventions**
    ```java
    // ✅ Good
    @Component
    class UserServiceImpl implements UserService { }
    // Bean name: "userServiceImpl"
    
    @Component("userService")
    class UserServiceImpl implements UserService { }
    // Bean name: "userService"
    ```

---

## Testing Strategies

### Unit Testing with Constructor Injection

```java
class UserServiceTest {
    
    @Test
    void testCreateUser() {
        // Arrange
        UserRepository mockRepo = mock(UserRepository.class);
        EmailService mockEmail = mock(EmailService.class);
        NotificationService mockNotif = mock(NotificationService.class);
        
        when(mockRepo.save(any())).thenReturn("USER-123");
        
        // No Spring needed!
        UserService service = new UserService(mockRepo, mockEmail, mockNotif);
        
        // Act
        String userId = service.createUser("test@example.com", "Test User");
        
        // Assert
        assertEquals("USER-123", userId);
        verify(mockRepo).save(any());
        verify(mockEmail).sendEmail(anyString(), anyString());
    }
}
```

### Integration Testing with @SpringBootTest

```java
@SpringBootTest
class PaymentServiceIntegrationTest {
    
    @Autowired
    private Map<String, PaymentProcessor> processors;
    
    @Test
    void testAllProcessors() {
        processors.forEach((name, processor) -> {
            String result = processor.process(100.0);
            assertNotNull(result);
        });
    }
}
```

### Testing with @Qualifier

```java
@SpringBootTest
class QualifierTest {
    
    @Autowired
    @Qualifier("stripe")
    private PaymentProcessor stripeProcessor;
    
    @Autowired
    @Qualifier("paypal")
    private PaymentProcessor paypalProcessor;
    
    @Test
    void testQualifiedBeans() {
        assertNotNull(stripeProcessor);
        assertNotNull(paypalProcessor);
        assertNotEquals(stripeProcessor, paypalProcessor);
    }
}
```

### Testing Optional Dependencies

```java
class OptionalDependencyTest {
    
    @Test
    void testWithDependencyPresent() {
        SmsService mockSms = mock(SmsService.class);
        UserService service = new UserService(
            mock(EmailService.class),
            Optional.of(mockSms),
            Optional.empty()
        );
        
        service.createUser("test@example.com", "Test");
        
        verify(mockSms).sendSms(anyString(), anyString());
    }
    
    @Test
    void testWithDependencyAbsent() {
        UserService service = new UserService(
            mock(EmailService.class),
            Optional.empty(),
            Optional.empty()
        );
        
        // Should not throw exception
        assertDoesNotThrow(() -> 
            service.createUser("test@example.com", "Test")
        );
    }
}
```

### Testing Lazy Beans

```java
@SpringBootTest
class LazyBeanTest {
    
    @Lazy
    @Autowired
    private HeavyService heavyService;
    
    @Test
    void testLazyInitialization() {
        // Bean not created yet (proxy exists)
        assertNotNull(heavyService);
        
        // Trigger lazy initialization
        String result = heavyService.process();
        
        // Bean now initialized
        assertNotNull(result);
    }
}
```

### Mocking in Tests

```java
@SpringBootTest
class MockingTest {
    
    @MockBean  // Replaces actual bean
    private EmailService emailService;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testWithMock() {
        when(emailService.send(any(), any())).thenReturn(true);
        
        userService.createUser("test@example.com", "Test");
        
        verify(emailService).send(anyString(), anyString());
    }
}
```

---

## Summary

### Quick Reference

| Need | Use Pattern | Example |
|------|-------------|---------|
| Single implementation | Autowiring by Type | `@Autowired UserRepository repo;` |
| Multiple implementations, common one | @Primary | `@Primary @Bean DatabaseClient primary()` |
| Multiple implementations, specific | @Qualifier | `@Qualifier("stripe") PaymentGateway gw;` |
| All implementations | Collection Autowiring | `List<NotificationChannel> channels;` |
| Runtime selection | Map Autowiring | `Map<String, Formatter> formatters;` |
| Optional feature | Optional Autowiring | `Optional<CacheService> cache;` |
| Heavy initialization | Lazy Autowiring | `@Lazy @Autowired HeavyService hs;` |
| Immutable dependencies | Constructor Injection | `public Service(Repo r) { }` |
| Optional parameters | Setter Injection | `@Autowired void setCache(Cache c)` |

### Decision Tree

```
Need dependency injection?
├─ Single bean of type exists?
│  └─ YES → Use @Autowired (by type)
│
├─ Multiple beans of same type?
│  ├─ One is default? → Use @Primary
│  ├─ Need specific bean? → Use @Qualifier
│  ├─ Need all beans?
│  │  ├─ Sequential processing? → Use List<T>
│  │  ├─ Unique beans only? → Use Set<T>
│  │  └─ Runtime selection? → Use Map<String, T>
│  
├─ Dependency optional?
│  └─ YES → Use Optional<T> or @Autowired(required=false)
│
├─ Heavy initialization?
│  └─ YES → Use @Lazy
│
└─ Circular dependency?
   └─ YES → Use @Lazy or refactor
```

### Key Takeaways

1. **Constructor injection is the recommended approach** for mandatory dependencies
2. **@Primary provides sensible defaults**, @Qualifier provides explicit control
3. **Collections (List/Set/Map) are powerful** for plugin architectures and strategies
4. **Optional dependencies enable feature toggles** and graceful degradation
5. **@Lazy improves startup time** but delays error detection
6. **Testing is easier with constructor injection** - no Spring container needed
7. **Make dependencies final** whenever possible for immutability
8. **Limit constructor parameters** to maintain Single Responsibility Principle

---

## Resources

### Official Documentation
- [Spring Framework Reference - Dependency Injection](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-dependencies)
- [Spring Boot Reference - Dependency Injection](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.auto-configuration)

### Related Patterns
- Bean Lifecycle Patterns
- Bean Scoping Patterns
- Configuration Patterns
- AOP Patterns

### Tools
- Spring Initializr: https://start.spring.io/
- Spring Boot DevTools for development
- JUnit 5 for testing
- Mockito for mocking

---

*Last Updated: 2024*
