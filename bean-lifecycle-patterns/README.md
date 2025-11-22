# Bean Lifecycle Patterns

This directory contains comprehensive examples of **Bean Lifecycle Patterns** in Spring Framework, demonstrating initialization, destruction, and lifecycle management techniques.

## Overview

Spring provides multiple mechanisms to hook into bean lifecycle events. Understanding these patterns is crucial for proper resource management, initialization, and cleanup in Spring applications.

## Patterns Included

### 1. Bean Initialization Pattern (`BeanInitializationPattern.java`)
Demonstrates all initialization techniques and their execution order.

**Key Features:**
- Constructor-based initialization
- @PostConstruct annotation
- InitializingBean.afterPropertiesSet()
- @Bean(initMethod) attribute
- Custom initialization logic

**Initialization Order:**
1. Constructor
2. Dependency Injection
3. @PostConstruct
4. InitializingBean.afterPropertiesSet()
5. Custom init-method

**Examples:**
```java
// Constructor
public DatabaseConnection() {
    this.connectionString = "jdbc:...";
}

// @PostConstruct
@PostConstruct
public void init() {
    loadUsersFromDatabase();
}

// InitializingBean
@Override
public void afterPropertiesSet() throws Exception {
    validateConfiguration();
}

// @Bean initMethod
@Bean(initMethod = "initialize")
public DataSource dataSource() {
    return new DataSource();
}
```

---

### 2. Bean Destruction Pattern (`BeanDestructionPattern.java`)
Demonstrates all cleanup/destruction techniques.

**Key Features:**
- @PreDestroy annotation
- DisposableBean.destroy()
- @Bean(destroyMethod) attribute
- Resource cleanup (files, connections, threads)

**Destruction Order:**
1. @PreDestroy methods
2. DisposableBean.destroy()
3. Custom destroy-method

**Examples:**
```java
// @PreDestroy
@PreDestroy
public void cleanup() {
    closeAllConnections();
}

// DisposableBean
@Override
public void destroy() throws Exception {
    releaseResources();
}

// @Bean destroyMethod
@Bean(destroyMethod = "shutdown")
public ThreadPoolManager threadPool() {
    return new ThreadPoolManager();
}
```

---

### 3. @PostConstruct Pattern (`PostConstructPattern.java`)
Standard JSR-250 annotation for initialization.

**Key Features:**
- Standard Java annotation (JSR-250)
- Called after dependency injection
- Cannot accept parameters
- Can throw exceptions
- Most common initialization approach

**Use Cases:**
- Load configuration
- Initialize caches
- Populate data
- Validate dependencies
- Compile patterns/templates

**Example:**
```java
@Service
class UserService {
    private final DatabaseService dbService;
    private List<User> cachedUsers;
    
    public UserService(DatabaseService dbService) {
        this.dbService = dbService;
    }
    
    @PostConstruct
    public void init() {
        cachedUsers = dbService.fetchAllUsers();
    }
}
```

**Best Practices:**
- Keep methods simple and fast
- Validate dependencies
- Handle exceptions appropriately
- Use meaningful method names
- Only one @PostConstruct per class (recommended)

---

### 4. @PreDestroy Pattern (`PreDestroyPattern.java`)
Standard JSR-250 annotation for cleanup.

**Key Features:**
- Standard Java annotation (JSR-250)
- Called before bean destruction
- Cannot accept parameters
- Should not throw exceptions
- Most common cleanup approach

**Use Cases:**
- Close database connections
- Release file handles
- Shutdown thread pools
- Save application state
- Clear caches
- Clean up temporary files

**Example:**
```java
@Component
class DatabaseConnectionPool {
    private final List<Connection> connections;
    
    @PreDestroy
    public void closeConnections() {
        for (Connection conn : connections) {
            conn.close();
        }
        connections.clear();
    }
}
```

**Best Practices:**
- Keep cleanup fast
- Don't block shutdown
- Handle exceptions gracefully
- Log cleanup actions
- Don't throw exceptions

---

### 5. Init Method Pattern (`InitMethodPattern.java`)
Using @Bean(initMethod) for third-party classes.

**Key Features:**
- Specify init method in configuration
- Works with classes you can't modify
- Method must be public/protected and no-arg
- XML equivalent: `<bean init-method="methodName"/>`

**When to Use:**
- Third-party library classes
- Can't modify source code
- Different init methods for different beans

**Example:**
```java
@Configuration
class Config {
    @Bean(initMethod = "initialize")
    public DataSource dataSource() {
        return new ThirdPartyDataSource();
    }
}

class ThirdPartyDataSource {
    public void initialize() {
        // Initialization logic
    }
}
```

**Best Practices:**
- Use for third-party classes
- Document init method clearly
- Handle exceptions appropriately
- Consider @PostConstruct if you can modify the class

---

### 6. Destroy Method Pattern (`DestroyMethodPattern.java`)
Using @Bean(destroyMethod) for cleanup.

**Key Features:**
- Specify destroy method in configuration
- Works with third-party classes
- Spring auto-detects "close()" and "shutdown()"
- Use destroyMethod="" to disable auto-detection

**Auto-Detection:**
Spring automatically calls these methods if they exist:
- close()
- shutdown()

**Example:**
```java
@Configuration
class Config {
    @Bean(destroyMethod = "cleanup")
    public ExternalResourceManager resourceManager() {
        return new ExternalResourceManager();
    }
    
    // Auto-detects close() method
    @Bean
    public AutoCloseableResource resource() {
        return new AutoCloseableResource();
    }
    
    // Disable auto-detection
    @Bean(destroyMethod = "")
    public NoAutoClose noAutoClose() {
        return new NoAutoClose();
    }
}
```

**Best Practices:**
- Use for third-party classes
- Be aware of auto-detection
- Use destroyMethod="" to disable auto-detection
- Keep cleanup fast

---

### 7. DisposableBean Pattern (`DisposableBeanPattern.java`)
Spring-specific interface for cleanup.

**Key Features:**
- Spring-specific interface
- Single destroy() method
- Can throw checked exceptions
- Tighter Spring integration
- Called after @PreDestroy

**Interface:**
```java
public interface DisposableBean {
    void destroy() throws Exception;
}
```

**Example:**
```java
@Component
class CacheService implements DisposableBean {
    private final Map<String, Object> cache;
    
    @Override
    public void destroy() throws Exception {
        saveStateToDisk();
        cache.clear();
    }
}
```

**When to Use:**
- Need Spring-aware cleanup
- Want to throw checked exceptions
- Require programmatic control
- Building Spring framework extensions

**vs @PreDestroy:**
- DisposableBean: Spring-specific, can throw exceptions
- @PreDestroy: Standard annotation, more portable

---

### 8. InitializingBean Pattern (`InitializingBeanPattern.java`)
Spring-specific interface for initialization.

**Key Features:**
- Spring-specific interface
- Single afterPropertiesSet() method
- Can throw checked exceptions
- Tighter Spring integration
- Called after @PostConstruct

**Interface:**
```java
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
```

**Example:**
```java
@Component
class UserRepository implements InitializingBean {
    private final DatabaseService dbService;
    private List<User> cache;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (dbService == null) {
            throw new IllegalStateException("DatabaseService required!");
        }
        cache = dbService.fetchAllUsers();
    }
}
```

**When to Use:**
- Need Spring-aware initialization
- Want to throw checked exceptions
- Require validation after all properties set
- Building Spring framework extensions

**vs @PostConstruct:**
- InitializingBean: Spring-specific, better exception handling
- @PostConstruct: Standard annotation, more portable

---

### 9. Lifecycle Interface Pattern (`LifecycleInterfacePattern.java`)
Provides start/stop control over beans.

**Key Features:**
- Start and stop methods
- Manual lifecycle control
- isRunning() status check
- Not auto-started by default

**Interface:**
```java
public interface Lifecycle {
    void start();
    void stop();
    boolean isRunning();
}
```

**Example:**
```java
@Component
class BackgroundService implements Lifecycle {
    private boolean running;
    
    @Override
    public void start() {
        running = true;
        // Start background processing
    }
    
    @Override
    public void stop() {
        running = false;
        // Stop background processing
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
}
```

**Use Cases:**
- Start/stop services manually
- Control message listeners
- Manage background threads
- Control resource consumption

---

### 10. SmartLifecycle Pattern (`SmartLifecyclePattern.java`)
Advanced lifecycle with auto-start and phases.

**Key Features:**
- Extends Lifecycle
- Auto-start capability (isAutoStartup)
- Phased startup/shutdown (getPhase)
- Graceful shutdown callback
- Ordered initialization

**Interface:**
```java
public interface SmartLifecycle extends Lifecycle, Phased {
    default boolean isAutoStartup() { return true; }
    default void stop(Runnable callback) { stop(); callback.run(); }
    default int getPhase() { return DEFAULT_PHASE; }
}
```

**Example:**
```java
@Component
class DatabaseConnectionManager implements SmartLifecycle {
    private boolean running;
    
    @Override
    public void start() {
        running = true;
        // Initialize connections
    }
    
    @Override
    public void stop() {
        running = false;
        // Close connections
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public boolean isAutoStartup() {
        return true; // Auto-start on application startup
    }
    
    @Override
    public void stop(Runnable callback) {
        stop(); // Cleanup
        callback.run(); // Notify completion
    }
    
    @Override
    public int getPhase() {
        return 1; // Start first, stop last
    }
}
```

**Phases:**
- Lower phase values start first
- Higher phase values stop first
- DEFAULT_PHASE = Integer.MAX_VALUE

**Startup Order:** Phase 1 → Phase 2 → Phase 3  
**Shutdown Order:** Phase 3 → Phase 2 → Phase 1

---

### 11. Phased Bean Pattern (`PhasedBeanPattern.java`)
Ordered lifecycle management using phase values.

**Key Features:**
- Control startup/shutdown order
- Phase-based dependency management
- Critical-to-optional ordering
- Predictable lifecycle

**Phase Recommendations:**
- Phase 0: Critical infrastructure (DB, messaging)
- Phase 100: Business services
- Phase 200: Application services
- Phase 300: Web/API layer
- Phase Integer.MAX_VALUE: Default

**Example:**
```java
@Component
class CriticalInfrastructure implements SmartLifecycle {
    @Override
    public int getPhase() {
        return 0; // Start first, stop last
    }
}

@Component
class WebLayer implements SmartLifecycle {
    @Override
    public int getPhase() {
        return 300; // Start last, stop first
    }
}
```

**Use Cases:**
- Database before services
- Services before web layer
- Infrastructure before applications
- Ordered shutdown

---

## Lifecycle Order Summary

### Initialization Order:
1. **Constructor**
2. **Dependency Injection** (setters/fields)
3. **BeanPostProcessor.postProcessBeforeInitialization**
4. **@PostConstruct** methods
5. **InitializingBean.afterPropertiesSet()**
6. **Custom init-method** (@Bean initMethod)
7. **BeanPostProcessor.postProcessAfterInitialization**

### Destruction Order:
1. **@PreDestroy** methods
2. **DisposableBean.destroy()**
3. **Custom destroy-method** (@Bean destroyMethod)

### SmartLifecycle Order:
**Startup:** Lower phase → Higher phase  
**Shutdown:** Higher phase → Lower phase

---

## Testing Examples

### Basic Status Check:
```bash
# Bean Initialization
curl http://localhost:8080/api/initialization/all

# Bean Destruction
curl http://localhost:8080/api/destruction/status

# @PostConstruct
curl http://localhost:8080/api/postconstruct/status

# @PreDestroy
curl http://localhost:8080/api/predestroy/status

# Init Method
curl http://localhost:8080/api/init-method/status

# Destroy Method
curl http://localhost:8080/api/destroy-method/status

# DisposableBean
curl http://localhost:8080/api/disposable/status

# InitializingBean
curl http://localhost:8080/api/initializing-bean/status

# Lifecycle
curl http://localhost:8080/api/lifecycle/status

# SmartLifecycle
curl http://localhost:8080/api/smart-lifecycle/status

# Phased Bean
curl http://localhost:8080/api/phased/status
```

### Trigger Shutdown:
```bash
curl -X POST http://localhost:8080/api/destruction/shutdown
curl -X POST http://localhost:8080/api/predestroy/shutdown
curl -X POST http://localhost:8080/api/destroy-method/shutdown
curl -X POST http://localhost:8080/api/disposable/shutdown
```

---

## Best Practices

### Initialization:

1. **Use @PostConstruct for most cases**
   - Standard, portable approach
   - Easy to understand
   - No Spring dependencies

2. **Use InitializingBean when:**
   - Need to throw checked exceptions
   - Require Spring-aware logic
   - Building framework extensions

3. **Use @Bean(initMethod) for:**
   - Third-party classes
   - Can't modify source code
   - Multiple beans with different init methods

4. **Keep initialization fast**
   - Don't block application startup
   - Consider async initialization
   - Use lazy initialization for heavy tasks

5. **Validate dependencies**
   - Check for null dependencies
   - Validate configuration values
   - Throw exceptions for critical failures

### Cleanup:

1. **Use @PreDestroy for most cases**
   - Standard, portable approach
   - Easy to understand
   - Widely supported

2. **Use DisposableBean when:**
   - Need to throw checked exceptions
   - Require Spring-aware cleanup
   - Building framework extensions

3. **Use @Bean(destroyMethod) for:**
   - Third-party classes
   - Auto-detect close/shutdown methods
   - Custom cleanup method names

4. **Keep cleanup fast**
   - Set reasonable timeouts
   - Don't block shutdown indefinitely
   - Consider force shutdown if needed

5. **Handle cleanup failures**
   - Log errors but don't throw exceptions
   - Clean up as much as possible
   - Release resources in reverse order

### Lifecycle Management:

1. **Use SmartLifecycle for:**
   - Auto-starting services
   - Ordered startup/shutdown
   - Graceful shutdown requirements

2. **Phase values:**
   - 0-100: Critical infrastructure
   - 100-200: Business services
   - 200-300: Application services
   - 300+: Web/API layer

3. **Start/Stop semantics:**
   - Lower phase starts first
   - Higher phase stops first
   - Respect dependencies

---

## Common Pitfalls

### Initialization:

1. **Long-running operations**
   - Blocks application startup
   - Use @Async or background tasks

2. **Circular dependencies**
   - Avoid during initialization
   - Use @Lazy if needed

3. **Null dependencies**
   - Always validate injected dependencies
   - Use constructor injection

4. **Assuming order**
   - Use @DependsOn if order matters
   - Don't assume bean creation order

5. **Complex logic in constructors**
   - Move to @PostConstruct
   - Keep constructors simple

### Cleanup:

1. **Resource leaks**
   - Always close connections/files
   - Release external resources

2. **Long-running cleanup**
   - Set timeouts
   - Consider force shutdown

3. **Exceptions in cleanup**
   - Log but don't throw
   - Don't block shutdown

4. **Not saving state**
   - Persist important data
   - Log final statistics

5. **Accessing destroyed beans**
   - Be aware of destruction order
   - Don't call methods on destroyed beans

---

## Comparison Matrix

| Feature | @PostConstruct | InitializingBean | @Bean(initMethod) |
|---------|---------------|------------------|-------------------|
| Standard | JSR-250 ✓ | Spring-specific | Spring-specific |
| Portable | Yes | No | No |
| Exceptions | Runtime only | Checked ✓ | Runtime only |
| Source code | Modify class | Implement interface | No modification needed |
| Common use | General init | Validation | Third-party classes |

| Feature | @PreDestroy | DisposableBean | @Bean(destroyMethod) |
|---------|------------|----------------|----------------------|
| Standard | JSR-250 ✓ | Spring-specific | Spring-specific |
| Portable | Yes | No | No |
| Exceptions | Avoid | Checked ✓ | Avoid |
| Source code | Modify class | Implement interface | No modification needed |
| Auto-detect | No | No | Yes (close/shutdown) |

---

## Recommendations

### For New Applications:

**Initialization:**
1. **First choice:** @PostConstruct
2. **Need exceptions:** InitializingBean
3. **Third-party:** @Bean(initMethod)

**Cleanup:**
1. **First choice:** @PreDestroy
2. **Need exceptions:** DisposableBean
3. **Third-party:** @Bean(destroyMethod)

**Lifecycle:**
1. **Auto-start services:** SmartLifecycle
2. **Manual control:** Lifecycle
3. **Ordered startup:** SmartLifecycle with phases

### For Microservices:

- Use SmartLifecycle for graceful shutdown
- Implement proper phase ordering
- Handle shutdown signals properly
- Save state before destruction
- Log lifecycle events

### For Legacy Applications:

- Prefer @PostConstruct over InitializingBean
- Migrate XML init-method to @Bean annotations
- Use @PreDestroy instead of destroy-method
- Consider SmartLifecycle for ordered shutdown

---

## Dependencies

```xml
<!-- Spring Boot Starter (includes all lifecycle features) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>

<!-- JSR-250 (for @PostConstruct and @PreDestroy) -->
<dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
</dependency>
```

---

## Additional Resources

- [Spring Bean Lifecycle Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-lifecycle)
- [JSR-250 Common Annotations](https://jcp.org/en/jsr/detail?id=250)
- [Spring Lifecycle Callbacks](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-nature)
- [SmartLifecycle Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/SmartLifecycle.html)

---

## Summary

Bean Lifecycle Patterns provide powerful mechanisms for:

✅ **Initialization** - Multiple approaches for bean setup  
✅ **Destruction** - Proper resource cleanup  
✅ **Lifecycle Management** - Start/stop control  
✅ **Ordered Execution** - Phase-based ordering  
✅ **Resource Management** - Connection pools, caches, files  
✅ **Graceful Shutdown** - Clean application termination  
✅ **Validation** - Dependency and configuration checks  

All patterns include comprehensive examples, testing strategies, and best practices for production use.
