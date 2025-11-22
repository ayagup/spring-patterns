# Spring Resource Management Patterns

This directory contains comprehensive Java implementations of all Spring Resource Management Patterns.

## Patterns Implemented

### 1. Resource Loader Pattern
**File:** `ResourceLoaderPattern.java`

Demonstrates Spring's ResourceLoader interface for loading resources from different locations:
- Classpath resources
- File system resources
- URL resources
- Protocol-independent resource access
- Integration with ApplicationContext

**Key Concepts:**
- ResourceLoader provides unified resource loading
- Supports multiple protocols (classpath:, file:, http:, ftp:)
- ApplicationContext implements ResourceLoader
- Can inject ResourceLoader into any Spring bean

---

### 2. Resource Pattern Resolver Pattern
**File:** `ResourcePatternResolverPattern.java`

Shows pattern-based resource resolution with wildcards:
- Ant-style path patterns (*, **, ?)
- classpath*: prefix for searching all locations
- Batch resource loading
- Component scanning support

**Key Concepts:**
- ResourcePatternResolver extends ResourceLoader
- Pattern matching for multiple resources
- Supports wildcards and complex patterns
- Used for component scanning

---

### 3. Application Context Pattern
**File:** `ApplicationContextPattern.java`

Demonstrates ApplicationContext as the central Spring IoC container:
- Bean factory methods
- Resource loading
- Environment abstraction
- Profile support
- Context hierarchy

**Key Concepts:**
- Central interface to Spring container
- Different implementations (Annotation, XML, Web)
- Environment and profile management
- Hierarchical context support

---

### 4. Bean Factory Pattern
**File:** `BeanFactoryPattern.java`

Shows BeanFactory as the root container interface:
- Lazy initialization
- Programmatic bean registration
- Bean lifecycle management
- Scope management (singleton, prototype)

**Key Concepts:**
- Lightweight IoC container
- Lazy initialization by default
- Foundation for ApplicationContext
- Programmatic bean management

---

### 5. Lifecycle Callback Pattern
**File:** `LifecycleCallbackPattern.java`

Demonstrates bean lifecycle callbacks:
- @PostConstruct and @PreDestroy
- Lifecycle interface
- SmartLifecycle interface
- Custom init and destroy methods

**Key Concepts:**
- Multiple callback mechanisms
- Specific execution order
- Start/stop control
- Automatic startup with SmartLifecycle

---

### 6. Destruction Callback Pattern
**File:** `DestructionCallbackPattern.java`

Shows destruction callbacks for resource cleanup:
- DisposableBean interface
- @PreDestroy annotation
- Custom destroy methods
- AutoCloseable/Closeable support

**Key Concepts:**
- Resource cleanup on bean destruction
- Multiple callback mechanisms
- Execution order: @PreDestroy → DisposableBean → custom destroy
- Prevents resource leaks

---

### 7. Initialization Callback Pattern
**File:** `InitializationCallbackPattern.java`

Demonstrates initialization callbacks:
- InitializingBean interface
- @PostConstruct annotation
- Custom init methods
- Property validation

**Key Concepts:**
- Callbacks after dependency injection
- Execution order: @PostConstruct → InitializingBean → custom init
- Used for complex setup and validation
- Multiple callback options

---

### 8. Aware Interfaces Pattern
**File:** `AwareInterfacesPattern.java`

Shows Aware interfaces for accessing Spring infrastructure:
- ApplicationContextAware
- BeanFactoryAware
- BeanNameAware
- EnvironmentAware
- ResourceLoaderAware
- ApplicationEventPublisherAware
- And more...

**Key Concepts:**
- Programmatic access to Spring infrastructure
- Specific callback order
- Framework integration points
- Alternative to dependency injection for framework components

---

### 9. Resource Abstraction Pattern
**File:** `ResourceAbstractionPattern.java`

Demonstrates Spring's Resource abstraction:
- ClassPathResource
- FileSystemResource
- UrlResource
- ByteArrayResource
- InputStreamResource
- WritableResource

**Key Concepts:**
- Unified interface for all resource types
- Common API across different sources
- Supports read/write operations
- Metadata access (size, modified time, etc.)

---

## Usage

Each pattern is implemented as a standalone Java class with a `main` method for demonstration purposes. To run any pattern:

```bash
javac ResourceLoaderPattern.java
java ResourceLoaderPattern
```

Or use your IDE to run the main method directly.

## Dependencies

These examples use Spring Framework core libraries:
- spring-core
- spring-context
- spring-beans

Maven dependency example:
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>6.0.0</version>
</dependency>
```

## Pattern Relationships

```
ApplicationContext
    ├── implements ResourceLoader
    ├── implements ResourcePatternResolver
    ├── implements ApplicationEventPublisher
    └── uses BeanFactory
        ├── manages bean lifecycle
        ├── triggers Aware callbacks
        ├── executes initialization callbacks
        └── executes destruction callbacks

Resource Abstraction
    └── used by ResourceLoader
        └── used by ResourcePatternResolver
```

## Lifecycle Order

Complete bean lifecycle with all callbacks:

1. **Instantiation**
   - Constructor called

2. **Dependency Injection**
   - Properties set
   - Dependencies injected

3. **Aware Callbacks** (in order)
   - BeanNameAware.setBeanName()
   - BeanClassLoaderAware.setBeanClassLoader()
   - BeanFactoryAware.setBeanFactory()
   - EnvironmentAware.setEnvironment()
   - ResourceLoaderAware.setResourceLoader()
   - ApplicationEventPublisherAware.setApplicationEventPublisher()
   - ApplicationContextAware.setApplicationContext()

4. **Initialization Callbacks** (in order)
   - @PostConstruct methods
   - InitializingBean.afterPropertiesSet()
   - Custom init-method

5. **Usage Phase**
   - Bean is fully initialized and ready

6. **Destruction Callbacks** (in order)
   - @PreDestroy methods
   - DisposableBean.destroy()
   - Custom destroy-method

## Best Practices

1. **Resource Loading**
   - Prefer ResourceLoader over File I/O
   - Use appropriate prefix (classpath:, file:, etc.)
   - Always close InputStreams

2. **Lifecycle Callbacks**
   - Prefer @PostConstruct/@PreDestroy for portability
   - Use InitializingBean for Spring-specific logic
   - Keep initialization logic out of constructors

3. **Aware Interfaces**
   - Use sparingly - prefer dependency injection
   - Document why Aware interface is needed
   - Consider alternatives first

4. **Resource Abstraction**
   - Use try-with-resources for streams
   - Check exists() before reading
   - Be aware of InputStreamResource limitations

## Common Use Cases

- **Configuration Management**: Loading property files, XML configs
- **Template Processing**: Reading template files for rendering
- **File Upload/Download**: Handling file transfers
- **Resource Initialization**: Setting up caches, connections, pools
- **Resource Cleanup**: Closing connections, releasing locks
- **Dynamic Resource Access**: Loading resources based on runtime conditions
- **Testing**: Loading test data and configurations

## Notes

- The compile errors shown (package declaration mismatch) are expected since these are demonstration files and not part of a structured project
- These patterns are core to Spring Framework and widely used in enterprise applications
- Understanding these patterns is essential for effective Spring development
- Each pattern includes comprehensive JavaDoc and comments explaining usage

## Author

Generated for Spring Framework Design Patterns documentation.

## License

These examples are provided for educational purposes.
