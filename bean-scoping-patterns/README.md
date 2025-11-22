# Bean Scoping Patterns in Spring Framework

This directory contains comprehensive examples of all Spring Framework bean scoping patterns.

## Overview

Bean scope defines the lifecycle and visibility of bean instances within the Spring container. Spring provides several built-in scopes and allows custom scope implementation.

## Table of Contents

1. [Singleton Scope Pattern](#1-singleton-scope-pattern)
2. [Prototype Scope Pattern](#2-prototype-scope-pattern)
3. [Request Scope Pattern](#3-request-scope-pattern)
4. [Session Scope Pattern](#4-session-scope-pattern)
5. [Application Scope Pattern](#5-application-scope-pattern)
6. [WebSocket Scope Pattern](#6-websocket-scope-pattern)
7. [Custom Scope Pattern](#7-custom-scope-pattern)
8. [Thread Scope Pattern](#8-thread-scope-pattern)
9. [Refresh Scope Pattern](#9-refresh-scope-pattern)
10. [Step Scope Pattern (Batch)](#10-step-scope-pattern-batch)
11. [Job Scope Pattern (Batch)](#11-job-scope-pattern-batch)
12. [Scope Comparison Matrix](#scope-comparison-matrix)
13. [Best Practices](#best-practices)

---

## 1. Singleton Scope Pattern

**File:** `SingletonScopePattern.java`

### Description
The singleton scope (default) creates a single instance of a bean per Spring IoC container. All requests for that bean return the same shared instance.

### Characteristics
- **Default scope** in Spring
- One instance per ApplicationContext
- Shared state across the application
- Thread-safe considerations required
- Created at startup (eager) or first request (lazy)

### Configuration
```java
@Bean
@Scope("singleton")  // or @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public UserService userService() {
    return new UserService();
}

// Or implicit (default)
@Bean
public UserService userService() {
    return new UserService();
}
```

### Use Cases
✓ Stateless services  
✓ Configuration objects  
✓ Utility beans  
✓ DAO/Repository beans  
✓ Controllers and Services  

### Thread Safety
```java
class UserService {
    // Thread-safe: using AtomicInteger
    private final AtomicInteger counter = new AtomicInteger(0);
    
    // Thread-safe: immutable
    private final LocalDateTime createdAt = LocalDateTime.now();
    
    public void incrementCounter() {
        counter.incrementAndGet();
    }
}
```

### Best Practices
- Design for immutability when possible
- Avoid mutable state or use thread-safe collections
- Use for stateless or thread-safe beans
- Consider @Lazy for expensive beans

### When to Avoid
✗ Beans with user-specific state  
✗ Beans requiring new instance per request  
✗ Non-thread-safe third-party libraries  
✗ Beans with request/session-specific data  

---

## 2. Prototype Scope Pattern

**File:** `PrototypeScopePattern.java`

### Description
Prototype scope creates a new bean instance every time it is requested from the container.

### Characteristics
- New instance on every `getBean()` call
- New instance for every injection point
- Spring doesn't manage complete lifecycle (no destruction callbacks)
- Each instance is independent
- Not cached by container

### Configuration
```java
@Bean
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public ShoppingCart shoppingCart() {
    return new ShoppingCart();
}
```

### Injection Strategies

#### 1. ObjectFactory (Recommended)
```java
@Component
class OrderService {
    @Autowired
    private ObjectFactory<ShoppingCart> cartFactory;
    
    public void processOrder() {
        ShoppingCart cart = cartFactory.getObject(); // New instance
    }
}
```

#### 2. @Lookup Method Injection
```java
@Component
abstract class ReportService {
    @Lookup
    protected abstract ReportGenerator createReportGenerator();
    
    public String generateReport() {
        ReportGenerator generator = createReportGenerator(); // New instance
        return generator.generate();
    }
}
```

#### 3. ApplicationContext.getBean()
```java
@Component
class SessionManager {
    @Autowired
    private ApplicationContext context;
    
    public UserSession createSession() {
        return context.getBean(UserSession.class); // New instance
    }
}
```

### Lifecycle Management
```java
@Component
@Scope("prototype")
class TaskExecutor {
    @PostConstruct
    public void init() {
        // Called by Spring
    }
    
    @PreDestroy
    public void cleanup() {
        // NOT called by Spring for prototype beans!
    }
}
```

### Use Cases
✓ Stateful beans  
✓ Command objects  
✓ Task objects  
✓ User-specific operations  
✓ Per-request processing  

### Common Pitfall
```java
// WRONG: Prototype injected into singleton loses prototype behavior
@Component
class OrderService {
    @Autowired
    private ShoppingCart cart; // Same instance always!
}

// CORRECT: Use ObjectFactory
@Component
class OrderService {
    @Autowired
    private ObjectFactory<ShoppingCart> cartFactory;
}
```

---

## 3. Request Scope Pattern

**File:** `RequestScopePattern.java`

### Description
Request scope creates a new bean instance for each HTTP request. Available only in web applications.

### Characteristics
- New instance per HTTP request
- Lifecycle tied to request
- Destroyed after request completes
- Thread-safe (isolated per request)

### Configuration
```java
@Bean
@RequestScope  // Shorthand
public RequestInfo requestInfo() {
    return new RequestInfo();
}

// Or explicit with proxy
@Bean
@Scope(value = WebApplicationContext.SCOPE_REQUEST, 
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public RequestContext requestContext() {
    return new RequestContext();
}
```

### Scoped Proxy Requirement
```java
// Singleton service using request-scoped bean
@Component
class RequestProcessor {
    // Requires proxyMode to inject request-scoped into singleton
    private final RequestInfo requestInfo;
    
    public RequestProcessor(RequestInfo requestInfo) {
        this.requestInfo = requestInfo; // Actually a proxy
    }
}
```

### SpEL Expressions
```java
@Component
@RequestScope
class RequestInfo {
    public RequestInfo(@Value("#{request.remoteAddr}") String remoteAddr) {
        // Access request attributes
    }
}
```

### Use Cases
✓ Request correlation tracking  
✓ Form backing objects  
✓ Request-specific configuration  
✓ Per-request logging context  
✓ Request metrics collection  

### Testing
```java
@WebMvcTest
class RequestScopeTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testRequestScopedBean() throws Exception {
        mockMvc.perform(get("/api/request/info"))
               .andExpect(status().isOk());
    }
}
```

---

## 4. Session Scope Pattern

**File:** `SessionScopePattern.java`

### Description
Session scope creates a single bean instance per HTTP session, persisting across multiple requests.

### Characteristics
- One instance per HTTP session
- Persists across multiple requests
- Destroyed when session expires or invalidates
- Session-bound lifecycle

### Configuration
```java
@Bean
@SessionScope
public ShoppingCart shoppingCart() {
    return new ShoppingCart();
}

// Or explicit
@Bean
@Scope(value = WebApplicationContext.SCOPE_SESSION, 
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public UserSession userSession() {
    return new UserSession();
}
```

### Session Configuration
```properties
# application.properties
server.servlet.session.timeout=30m
server.servlet.session.cookie.name=JSESSIONID
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
```

### Use Cases
✓ Shopping carts  
✓ User authentication state  
✓ User preferences  
✓ Multi-step wizards  
✓ Session-level caching  
✓ User activity tracking  

### Memory Considerations
```java
@Component
@SessionScope
class ShoppingCart {
    private final List<CartItem> items = new ArrayList<>();
    
    @PreDestroy
    public void cleanup() {
        items.clear(); // Clean up on session destruction
        System.out.println("Cart cleaned up");
    }
}
```

### Session Storage Options
- In-memory (default)
- Redis (Spring Session)
- JDBC (Spring Session)
- Hazelcast

```xml
<!-- Spring Session Redis -->
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
```

---

## 5. Application Scope Pattern

**File:** `ApplicationScopePattern.java`

### Description
Application scope creates a single bean instance per ServletContext (web application).

### Characteristics
- One instance per ServletContext
- Shared across all sessions and requests
- Lifecycle tied to web application
- Destroyed when application stops

### Singleton vs Application Scope
| Aspect | Singleton | Application |
|--------|-----------|-------------|
| Scope | Per ApplicationContext | Per ServletContext |
| Context | Any Spring application | Web applications only |
| Use case | General beans | Web-specific shared state |

### Configuration
```java
@Bean
@ApplicationScope
public ApplicationStatistics statistics() {
    return new ApplicationStatistics();
}
```

### Thread Safety (Critical!)
```java
@Component
@ApplicationScope
class ApplicationStatistics {
    // Thread-safe: AtomicLong
    private final AtomicLong totalRequests = new AtomicLong(0);
    
    // Thread-safe: ConcurrentHashMap
    private final Map<String, AtomicLong> endpointHits = new ConcurrentHashMap<>();
    
    public void incrementRequests() {
        totalRequests.incrementAndGet();
    }
}
```

### Use Cases
✓ Application-wide statistics  
✓ Global configuration  
✓ Application metadata  
✓ Shared caches  
✓ Application-level counters  
✓ Feature toggles  

---

## 6. WebSocket Scope Pattern

**File:** `WebSocketScopePattern.java`

### Description
WebSocket scope creates bean instances bound to WebSocket sessions.

### Characteristics
- One instance per WebSocket session
- Lifecycle tied to WebSocket connection
- Destroyed when connection closes

### Configuration
```java
@Bean
@Scope(value = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public WebSocketSession webSocketSession() {
    return new WebSocketSession();
}
```

### Use Cases
✓ WebSocket session state  
✓ Real-time chat sessions  
✓ Live updates tracking  
✓ WebSocket-specific configuration  
✓ Per-connection caching  

---

## 7. Custom Scope Pattern

**File:** `WebSocketScopePattern.java` (includes CustomScope)

### Description
Custom scopes allow you to define your own bean scoping logic by implementing Spring's `Scope` interface.

### Implementation
```java
@Component
class CustomScope implements Scope {
    private final Map<String, Object> scopedObjects = new ConcurrentHashMap<>();
    private final Map<String, Runnable> destructionCallbacks = new ConcurrentHashMap<>();
    
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return scopedObjects.computeIfAbsent(name, k -> objectFactory.getObject());
    }
    
    @Override
    public Object remove(String name) {
        destructionCallbacks.remove(name);
        return scopedObjects.remove(name);
    }
    
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        destructionCallbacks.put(name, callback);
    }
    
    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }
    
    @Override
    public String getConversationId() {
        return "custom-scope-" + Thread.currentThread().getId();
    }
}
```

### Registration
```java
@Bean
public static CustomScopeConfigurer customScopeConfigurer() {
    CustomScopeConfigurer configurer = new CustomScopeConfigurer();
    Map<String, Object> scopes = new HashMap<>();
    scopes.put("custom", new CustomScope());
    configurer.setScopes(scopes);
    return configurer;
}
```

### Usage
```java
@Bean
@Scope("custom")
public CustomScopedBean customBean() {
    return new CustomScopedBean();
}
```

### Use Cases
✓ Tenant-specific scoping  
✓ Feature-flag based scoping  
✓ Time-based scoping  
✓ Cache-backed scoping  
✓ Custom lifecycle requirements  

---

## 8. Thread Scope Pattern

**File:** `ThreadScopePattern.java`

### Description
Thread scope creates bean instances bound to specific threads via ThreadLocal storage.

### Characteristics
- One instance per thread
- Thread-local storage
- No cross-thread visibility
- Manual cleanup required

### Implementation
```java
@Component("threadScope")
class SimpleThreadScope implements Scope {
    private final ThreadLocal<Map<String, Object>> threadScope = 
        new NamedThreadLocal<>("ThreadScope") {
            @Override
            protected Map<String, Object> initialValue() {
                return new HashMap<>();
            }
        };
    
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return threadScope.get().computeIfAbsent(name, 
            k -> objectFactory.getObject());
    }
    
    // ... other methods
}
```

### Configuration
```java
@Bean
@Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
public ThreadLocalContext threadContext() {
    return new ThreadLocalContext();
}
```

### Use Cases
✓ Thread-specific context  
✓ Async task processing  
✓ Thread-local caching  
✓ Per-thread metrics  
✓ Thread-bound transactions  

### Cleanup Warning
```java
// IMPORTANT: Clean up ThreadLocal to prevent memory leaks
public void clearThread() {
    destructionCallbacks.get().values().forEach(Runnable::run);
    destructionCallbacks.get().clear();
    threadScope.get().clear();
}
```

---

## 9. Refresh Scope Pattern

**File:** `ThreadScopePattern.java` (includes RefreshScope)

### Description
Refresh scope allows beans to be recreated when configuration changes. Primarily used in Spring Cloud Config.

### Characteristics
- Beans recreated on demand
- Used for dynamic configuration
- Spring Cloud feature
- Trigger via `/actuator/refresh`

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

### Configuration
```java
@Bean
@RefreshScope
public DynamicConfiguration dynamicConfig() {
    return new DynamicConfiguration();
}
```

### Triggering Refresh
```bash
# Refresh configuration
curl -X POST http://localhost:8080/actuator/refresh
```

### Use Cases
✓ Dynamic configuration  
✓ Feature toggles  
✓ A/B testing  
✓ Runtime config changes  
✓ Cloud-native applications  

---

## 10. Step Scope Pattern (Batch)

**File:** `StepScopePattern.java`

### Description
Step scope creates bean instances bound to the execution context of a Spring Batch step.

### Characteristics
- One instance per step execution
- Late binding of step execution context
- Access to step execution parameters
- Lifecycle tied to step
- Allows parameter injection via SpEL

### Configuration
```java
@Bean
@StepScope
public ItemReader<String> stepScopedReader(
        @Value("#{jobParameters['inputFile']}") String inputFile,
        @Value("#{jobParameters['batchSize']}") Integer batchSize,
        @Value("#{stepExecution}") StepExecution stepExecution) {
    return new FileItemReader(inputFile, batchSize, stepExecution);
}
```

### SpEL Expressions
| Expression | Description |
|------------|-------------|
| `#{jobParameters['key']}` | Access job parameter |
| `#{stepExecution}` | Step execution object |
| `#{stepExecutionContext['key']}` | Step context value |
| `#{jobExecution}` | Job execution object |
| `#{jobExecutionContext['key']}` | Job context value |

### Use Cases
✓ Parameterized ItemReaders  
✓ Parameterized ItemProcessors  
✓ Parameterized ItemWriters  
✓ Step-specific configuration  
✓ Late-bound step parameters  

### Example
```java
@Bean
@StepScope
public ItemProcessor<String, String> processor(
        @Value("#{jobParameters['mode']}") String mode) {
    return item -> "UPPERCASE".equals(mode) ? 
                  item.toUpperCase() : 
                  item.toLowerCase();
}
```

---

## 11. Job Scope Pattern (Batch)

**File:** `JobScopePattern.java`

### Description
Job scope creates bean instances bound to the execution context of a Spring Batch job, shared across all steps.

### Characteristics
- One instance per job execution
- Late binding of job execution context
- Shared across all steps in job
- Lifecycle tied to job

### Configuration
```java
@Bean
@JobScope
public JobConfiguration jobConfig(
        @Value("#{jobParameters['environment']}") String environment,
        @Value("#{jobExecution}") JobExecution jobExecution) {
    return new JobConfiguration(environment, jobExecution);
}
```

### Job vs Step Scope
| Aspect | Job Scope | Step Scope |
|--------|-----------|------------|
| Lifecycle | Per job execution | Per step execution |
| Sharing | Across all steps | Single step only |
| Use case | Job-level config | Step-specific config |
| Parameters | Job parameters | Job + step parameters |

### Cross-Step Data Sharing
```java
@Component
@JobScope
class JobDataCollector {
    private final Map<String, List<String>> stepData = new ConcurrentHashMap<>();
    
    public void addData(String stepName, String data) {
        stepData.computeIfAbsent(stepName, k -> new ArrayList<>()).add(data);
    }
    
    public Map<String, List<String>> getAllData() {
        return new HashMap<>(stepData);
    }
}
```

### Use Cases
✓ Job-level configuration  
✓ Cross-step data collection  
✓ Job-wide metrics  
✓ Shared resources  
✓ Job execution tracking  

---

## Scope Comparison Matrix

### Lifecycle Comparison

| Scope | Instances | Created | Destroyed | Context Required |
|-------|-----------|---------|-----------|------------------|
| **Singleton** | 1 per ApplicationContext | Startup/first access | Context shutdown | Any |
| **Prototype** | Per request | On each getBean() | Not managed | Any |
| **Request** | 1 per HTTP request | Request start | Request end | Web |
| **Session** | 1 per HTTP session | Session creation | Session timeout | Web |
| **Application** | 1 per ServletContext | First access | App shutdown | Web |
| **WebSocket** | 1 per WS session | Connection | Disconnection | WebSocket |
| **Thread** | 1 per thread | First access | Manual cleanup | Custom |
| **Refresh** | 1 until refresh | First access | Refresh trigger | Cloud |
| **Step** | 1 per step execution | Step start | Step end | Batch |
| **Job** | 1 per job execution | Job start | Job end | Batch |

### Feature Comparison

| Feature | Singleton | Prototype | Request | Session | Application | Batch |
|---------|-----------|-----------|---------|---------|-------------|-------|
| **Thread-safe required** | Yes | No | No | Yes | Yes | Depends |
| **Proxy mode needed** | No | No | Yes | Yes | Yes | Yes |
| **Stateful** | No* | Yes | Yes | Yes | No* | Yes |
| **@PreDestroy called** | Yes | No | Yes | Yes | Yes | Yes |
| **Memory overhead** | Low | High | Medium | High | Low | Medium |
| **Performance** | Excellent | Fair | Good | Good | Excellent | Good |

*Can be stateful if thread-safe

### When to Use Which Scope

```
┌─────────────────────────────────────────────────────────┐
│                    Decision Tree                        │
└─────────────────────────────────────────────────────────┘

Is it a web application?
├─ NO  → Singleton (stateless) or Prototype (stateful)
└─ YES
    │
    ├─ Need per-request data?
    │   └─ YES → Request Scope
    │
    ├─ Need per-session data (shopping cart, user prefs)?
    │   └─ YES → Session Scope
    │
    ├─ Need application-wide shared state?
    │   └─ YES → Application Scope (or Singleton)
    │
    ├─ Is it a batch job?
    │   ├─ Job-level config? → Job Scope
    │   └─ Step-level config? → Step Scope
    │
    ├─ Is it a WebSocket?
    │   └─ YES → WebSocket Scope
    │
    ├─ Need thread-specific data?
    │   └─ YES → Thread Scope
    │
    ├─ Need dynamic refresh?
    │   └─ YES → Refresh Scope
    │
    └─ Need custom lifecycle?
        └─ YES → Custom Scope
```

---

## Best Practices

### 1. Singleton Scope Best Practices

```java
// ✓ GOOD: Immutable singleton
@Component
class ConfigService {
    private final String appName;
    private final int timeout;
    
    public ConfigService(@Value("${app.name}") String appName,
                        @Value("${app.timeout}") int timeout) {
        this.appName = appName;
        this.timeout = timeout;
    }
}

// ✓ GOOD: Thread-safe mutable singleton
@Component
class CounterService {
    private final AtomicInteger counter = new AtomicInteger(0);
    private final Map<String, Integer> stats = new ConcurrentHashMap<>();
    
    public void increment() {
        counter.incrementAndGet();
    }
}

// ✗ BAD: Non-thread-safe mutable singleton
@Component
class UnsafeService {
    private int counter = 0; // NOT thread-safe!
    
    public void increment() {
        counter++; // Race condition!
    }
}
```

### 2. Prototype Scope Best Practices

```java
// ✓ GOOD: Inject via ObjectFactory
@Component
class OrderProcessor {
    @Autowired
    private ObjectFactory<ShoppingCart> cartFactory;
    
    public void process() {
        ShoppingCart cart = cartFactory.getObject();
        cart.addItem("Product");
    }
}

// ✗ BAD: Direct injection loses prototype behavior
@Component
class BadOrderProcessor {
    @Autowired
    private ShoppingCart cart; // Always same instance!
}

// ✓ GOOD: Manual cleanup
@Component
@Scope("prototype")
class ResourceHandler implements AutoCloseable {
    private Resource resource;
    
    @Override
    public void close() {
        if (resource != null) {
            resource.cleanup();
        }
    }
}
```

### 3. Web Scope Best Practices

```java
// ✓ GOOD: Use proxyMode for injection into singletons
@Bean
@Scope(value = WebApplicationContext.SCOPE_REQUEST, 
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public RequestContext requestContext() {
    return new RequestContext();
}

// ✓ GOOD: Clean up in @PreDestroy
@Component
@SessionScope
class UserSession {
    private List<Resource> resources = new ArrayList<>();
    
    @PreDestroy
    public void cleanup() {
        resources.forEach(Resource::close);
        resources.clear();
    }
}

// ✗ BAD: No proxy mode
@Bean
@Scope(WebApplicationContext.SCOPE_REQUEST) // Missing proxyMode!
public RequestContext requestContext() {
    return new RequestContext();
}
```

### 4. Batch Scope Best Practices

```java
// ✓ GOOD: Use SpEL for late binding
@Bean
@StepScope
public ItemReader<User> reader(
        @Value("#{jobParameters['inputFile']}") String file) {
    return new FlatFileItemReader<>(file);
}

// ✓ GOOD: Access execution context
@Bean
@JobScope
public JobMetrics metrics(@Value("#{jobExecution}") JobExecution execution) {
    return new JobMetrics(execution.getJobId());
}

// ✗ BAD: Hardcoded values
@Bean
@StepScope
public ItemReader<User> reader() {
    return new FlatFileItemReader<>("hardcoded.csv"); // Not flexible!
}
```

### 5. Thread Scope Best Practices

```java
// ✓ GOOD: Clean up ThreadLocal
public class ThreadScopeCleaner {
    @Autowired
    private SimpleThreadScope threadScope;
    
    @PreDestroy
    public void cleanup() {
        threadScope.clearThread(); // Prevent memory leak!
    }
}

// ✗ BAD: No cleanup
// ThreadLocal not cleaned → Memory leak!
```

### 6. Custom Scope Best Practices

```java
// ✓ GOOD: Thread-safe custom scope
class TenantScope implements Scope {
    private final Map<String, Object> beans = new ConcurrentHashMap<>();
    
    @Override
    public Object get(String name, ObjectFactory<?> factory) {
        String tenantId = TenantContext.getCurrentTenant();
        String key = tenantId + ":" + name;
        return beans.computeIfAbsent(key, k -> factory.getObject());
    }
}
```

### 7. General Best Practices

#### Memory Management
```java
// Monitor session bean memory
@Component
@SessionScope
class LargeSessionBean {
    private List<Data> cache = new ArrayList<>();
    
    @PreDestroy
    public void cleanup() {
        cache.clear(); // Prevent memory leak
        System.out.println("Cleaned up session bean");
    }
}
```

#### Configuration
```properties
# Session timeout
server.servlet.session.timeout=30m

# Enable lazy initialization for singletons
spring.main.lazy-initialization=true
```

#### Testing
```java
// Test request-scoped beans
@WebMvcTest
class RequestScopeTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testRequestScope() throws Exception {
        mockMvc.perform(get("/api/request/info"))
               .andExpect(status().isOk());
    }
}

// Test with @DirtiesContext for stateful beans
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SingletonTest {
    @Autowired
    private CounterService counter;
    
    @Test
    void test1() {
        counter.increment();
        assertEquals(1, counter.getCount());
    }
}
```

---

## Common Pitfalls and Solutions

### 1. Prototype in Singleton
```java
// PROBLEM
@Component
class SingletonService {
    @Autowired
    private PrototypeBean prototypeBean; // Same instance always!
}

// SOLUTION 1: ObjectFactory
@Component
class SingletonService {
    @Autowired
    private ObjectFactory<PrototypeBean> factory;
    
    public void doSomething() {
        PrototypeBean bean = factory.getObject(); // New instance
    }
}

// SOLUTION 2: @Lookup
@Component
abstract class SingletonService {
    @Lookup
    protected abstract PrototypeBean createPrototype();
    
    public void doSomething() {
        PrototypeBean bean = createPrototype(); // New instance
    }
}
```

### 2. Missing Scoped Proxy
```java
// PROBLEM
@Component
class SingletonController {
    @Autowired
    private RequestScopedBean requestBean; // Error at startup!
}

// SOLUTION
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class RequestScopedBean {
    // Now works with proxy
}
```

### 3. Thread Safety in Singletons
```java
// PROBLEM
@Component
class UnsafeCounter {
    private int count = 0; // NOT thread-safe
    
    public void increment() {
        count++; // Race condition!
    }
}

// SOLUTION
@Component
class SafeCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet(); // Thread-safe
    }
}
```

### 4. ThreadLocal Memory Leaks
```java
// PROBLEM
class ThreadScopedService {
    private static final ThreadLocal<Data> threadData = new ThreadLocal<>();
    // Never cleaned up → Memory leak!
}

// SOLUTION
class ThreadScopedService {
    private static final ThreadLocal<Data> threadData = new ThreadLocal<>();
    
    @PreDestroy
    public void cleanup() {
        threadData.remove(); // Clean up!
    }
}
```

---

## Testing Examples

### Testing Singleton Scope
```java
@SpringBootTest
class SingletonScopeTest {
    @Autowired
    private ApplicationContext context;
    
    @Test
    void testSingletonBehavior() {
        UserService service1 = context.getBean(UserService.class);
        UserService service2 = context.getBean(UserService.class);
        
        assertSame(service1, service2);
    }
}
```

### Testing Prototype Scope
```java
@SpringBootTest
class PrototypeScopeTest {
    @Autowired
    private ApplicationContext context;
    
    @Test
    void testPrototypeBehavior() {
        ShoppingCart cart1 = context.getBean(ShoppingCart.class);
        ShoppingCart cart2 = context.getBean(ShoppingCart.class);
        
        assertNotSame(cart1, cart2);
        assertNotEquals(cart1.getCartId(), cart2.getCartId());
    }
}
```

### Testing Request Scope
```java
@WebMvcTest(RequestScopeController.class)
class RequestScopeTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testRequestScope() throws Exception {
        // Each request gets new instance
        String response1 = mockMvc.perform(get("/api/request/info"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
            
        String response2 = mockMvc.perform(get("/api/request/info"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        
        // Different request IDs
        assertNotEquals(response1, response2);
    }
}
```

### Testing Session Scope
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SessionScopeTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testSessionScope() {
        // First request creates session
        ResponseEntity<String> response1 = restTemplate.getForEntity(
            "/api/session/cart/add?item=Laptop", String.class);
        
        // Extract session cookie
        String cookie = response1.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        
        // Second request with same session
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response2 = restTemplate.exchange(
            "/api/session/cart/view", HttpMethod.GET, request, String.class);
        
        // Same cart across requests in same session
        assertTrue(response2.getBody().contains("Laptop"));
    }
}
```

---

## Performance Considerations

### Scope Performance Ranking (Fastest to Slowest)

1. **Singleton** - Best performance, no object creation overhead
2. **Application** - Similar to singleton, web-specific
3. **Prototype** - Object creation on each request
4. **Request** - Per HTTP request, moderate overhead
5. **Session** - Per session, memory intensive
6. **Thread** - ThreadLocal overhead
7. **Custom** - Depends on implementation
8. **Step/Job** - Batch-specific, parameter resolution overhead

### Memory Usage

```
Singleton/Application: ●○○○○ (Minimal)
Prototype: ●●●●○ (High, proportional to usage)
Request: ●●○○○ (Moderate, proportional to traffic)
Session: ●●●○○ (High, proportional to active sessions)
Thread: ●●○○○ (Moderate, proportional to threads)
Batch: ●●○○○ (Moderate, per execution)
```

---

## Running the Examples

### Prerequisites
```bash
# Java 17+
java -version

# Maven
mvn -version
```

### Build and Run
```bash
# Build all patterns
mvn clean install

# Run specific pattern
mvn spring-boot:run -Dspring-boot.run.mainClass=com.spring.patterns.scope.SingletonScopePattern

# Or run JAR
java -jar target/bean-scoping-patterns-1.0.0.jar
```

### Test Endpoints

#### Singleton Scope
```bash
curl http://localhost:8080/api/singleton/user-info
curl http://localhost:8080/api/singleton/increment
curl http://localhost:8080/api/singleton/counter
```

#### Prototype Scope
```bash
curl http://localhost:8080/api/prototype/create-cart
curl http://localhost:8080/api/prototype/create-order
curl http://localhost:8080/api/prototype/compare-instances
```

#### Request Scope
```bash
curl http://localhost:8080/api/request/info
curl http://localhost:8080/api/request/counter
curl -X POST "http://localhost:8080/api/request/submit?name=John&email=john@example.com&message=Hello"
```

#### Session Scope
```bash
curl -c cookies.txt http://localhost:8080/api/session/login?username=alice
curl -b cookies.txt "http://localhost:8080/api/session/cart/add?item=Laptop&price=999"
curl -b cookies.txt http://localhost:8080/api/session/cart/view
curl -b cookies.txt http://localhost:8080/api/session/user/profile
```

#### Batch Scope
```bash
curl -X POST "http://localhost:8080/api/batch/run?inputFile=data.txt&batchSize=50&processingMode=UPPERCASE"
curl -X POST "http://localhost:8080/api/job/execute?environment=production&runMode=full"
```

---

## Summary

### Quick Reference

| When you need... | Use this scope |
|------------------|----------------|
| Application-wide shared service | **Singleton** |
| New instance per request | **Prototype** |
| HTTP request-specific data | **Request** |
| User session data (shopping cart) | **Session** |
| App-wide statistics | **Application** |
| WebSocket connection state | **WebSocket** |
| Thread-specific data | **Thread** |
| Dynamic configuration | **Refresh** |
| Batch step parameters | **Step** |
| Batch job-wide state | **Job** |
| Custom lifecycle logic | **Custom** |

### Key Takeaways

1. **Default is Singleton** - Use unless you have a reason not to
2. **Prototype needs special injection** - Use ObjectFactory or @Lookup
3. **Web scopes need proxyMode** - Required for singleton injection
4. **Thread safety matters** - Especially for singleton and application scopes
5. **Clean up resources** - Implement @PreDestroy for session/request beans
6. **Batch scopes use late binding** - SpEL expressions for parameters
7. **Custom scopes are powerful** - But use built-in scopes first

---

## References

- [Spring Framework Documentation - Bean Scopes](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-scopes)
- [Spring Batch Documentation - Scopes](https://docs.spring.io/spring-batch/docs/current/reference/html/index.html)
- [Spring Cloud Documentation - Refresh Scope](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#refresh-scope)
- [Spring Session Documentation](https://docs.spring.io/spring-session/reference/)

---

**Created:** November 2025  
**Spring Framework Version:** 6.x  
**Spring Boot Version:** 3.x
