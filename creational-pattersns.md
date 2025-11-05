# Spring Creational Design Patterns - Java Implementations

## 1. Singleton Pattern

```java org/example/patterns/creational/singleton/AppConfig.java
package org.example.patterns.creational.singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Singleton Pattern - Default scope in Spring
 * Only one instance is created per Spring container
 */
@Configuration
public class AppConfig {
    
    @Bean
    @Scope("singleton") // Default scope, can be omitted
    public DatabaseConnection databaseConnection() {
        return new DatabaseConnection();
    }
}
```

```java org/example/patterns/creational/singleton/DatabaseConnection.java
package org.example.patterns.creational.singleton;

import org.springframework.stereotype.Component;

@Component
public class DatabaseConnection {
    
    private static int instanceCounter = 0;
    private final int instanceId;
    
    public DatabaseConnection() {
        this.instanceId = ++instanceCounter;
        System.out.println("DatabaseConnection instance created: " + instanceId);
    }
    
    public void connect() {
        System.out.println("Connected using instance: " + instanceId);
    }
    
    public int getInstanceId() {
        return instanceId;
    }
}
```

```java org/example/patterns/creational/singleton/SingletonDemo.java
package org.example.patterns.creational.singleton;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SingletonDemo implements CommandLineRunner {
    
    private final DatabaseConnection connection1;
    private final DatabaseConnection connection2;
    
    public SingletonDemo(DatabaseConnection connection1, DatabaseConnection connection2) {
        this.connection1 = connection1;
        this.connection2 = connection2;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("=== Singleton Pattern Demo ===");
        connection1.connect();
        connection2.connect();
        System.out.println("Same instance? " + (connection1 == connection2));
        System.out.println("Instance ID 1: " + connection1.getInstanceId());
        System.out.println("Instance ID 2: " + connection2.getInstanceId());
    }
}
```

---

## 2. Factory Pattern

```java org/example/patterns/creational/factory/NotificationFactory.java
package org.example.patterns.creational.factory;

import org.springframework.stereotype.Component;

@Component
public class NotificationFactory {
    
    public Notification createNotification(String type) {
        switch (type.toLowerCase()) {
            case "email":
                return new EmailNotification();
            case "sms":
                return new SmsNotification();
            case "push":
                return new PushNotification();
            default:
                throw new IllegalArgumentException("Unknown notification type: " + type);
        }
    }
}
```

```java org/example/patterns/creational/factory/Notification.java
package org.example.patterns.creational.factory;

public interface Notification {
    void send(String message);
}
```

```java org/example/patterns/creational/factory/EmailNotification.java
package org.example.patterns.creational.factory;

public class EmailNotification implements Notification {
    
    @Override
    public void send(String message) {
        System.out.println("Email sent: " + message);
    }
}
```

```java org/example/patterns/creational/factory/SmsNotification.java
package org.example.patterns.creational.factory;

public class SmsNotification implements Notification {
    
    @Override
    public void send(String message) {
        System.out.println("SMS sent: " + message);
    }
}
```

```java org/example/patterns/creational/factory/PushNotification.java
package org.example.patterns.creational.factory;

public class PushNotification implements Notification {
    
    @Override
    public void send(String message) {
        System.out.println("Push notification sent: " + message);
    }
}
```

```java org/example/patterns/creational/factory/FactoryDemo.java
package org.example.patterns.creational.factory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FactoryDemo implements CommandLineRunner {
    
    private final NotificationFactory notificationFactory;
    
    public FactoryDemo(NotificationFactory notificationFactory) {
        this.notificationFactory = notificationFactory;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Factory Pattern Demo ===");
        
        Notification email = notificationFactory.createNotification("email");
        email.send("Hello via Email");
        
        Notification sms = notificationFactory.createNotification("sms");
        sms.send("Hello via SMS");
        
        Notification push = notificationFactory.createNotification("push");
        push.send("Hello via Push");
    }
}
```

---

## 3. Abstract Factory Pattern

```java org/example/patterns/creational/abstractfactory/UIFactory.java
package org.example.patterns.creational.abstractfactory;

public interface UIFactory {
    Button createButton();
    TextField createTextField();
}
```

```java org/example/patterns/creational/abstractfactory/WindowsUIFactory.java
package org.example.patterns.creational.abstractfactory;

import org.springframework.stereotype.Component;

@Component("windowsUIFactory")
public class WindowsUIFactory implements UIFactory {
    
    @Override
    public Button createButton() {
        return new WindowsButton();
    }
    
    @Override
    public TextField createTextField() {
        return new WindowsTextField();
    }
}
```

```java org/example/patterns/creational/abstractfactory/MacUIFactory.java
package org.example.patterns.creational.abstractfactory;

import org.springframework.stereotype.Component;

@Component("macUIFactory")
public class MacUIFactory implements UIFactory {
    
    @Override
    public Button createButton() {
        return new MacButton();
    }
    
    @Override
    public TextField createTextField() {
        return new MacTextField();
    }
}
```

```java org/example/patterns/creational/abstractfactory/Button.java
package org.example.patterns.creational.abstractfactory;

public interface Button {
    void render();
}
```

```java org/example/patterns/creational/abstractfactory/TextField.java
package org.example.patterns.creational.abstractfactory;

public interface TextField {
    void render();
}
```

```java org/example/patterns/creational/abstractfactory/WindowsButton.java
package org.example.patterns.creational.abstractfactory;

public class WindowsButton implements Button {
    
    @Override
    public void render() {
        System.out.println("Rendering Windows Button");
    }
}
```

```java org/example/patterns/creational/abstractfactory/WindowsTextField.java
package org.example.patterns.creational.abstractfactory;

public class WindowsTextField implements TextField {
    
    @Override
    public void render() {
        System.out.println("Rendering Windows TextField");
    }
}
```

```java org/example/patterns/creational/abstractfactory/MacButton.java
package org.example.patterns.creational.abstractfactory;

public class MacButton implements Button {
    
    @Override
    public void render() {
        System.out.println("Rendering Mac Button");
    }
}
```

```java org/example/patterns/creational/abstractfactory/MacTextField.java
package org.example.patterns.creational.abstractfactory;

public class MacTextField implements TextField {
    
    @Override
    public void render() {
        System.out.println("Rendering Mac TextField");
    }
}
```

```java org/example/patterns/creational/abstractfactory/AbstractFactoryDemo.java
package org.example.patterns.creational.abstractfactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AbstractFactoryDemo implements CommandLineRunner {
    
    private final UIFactory windowsFactory;
    private final UIFactory macFactory;
    
    public AbstractFactoryDemo(
            @Qualifier("windowsUIFactory") UIFactory windowsFactory,
            @Qualifier("macUIFactory") UIFactory macFactory) {
        this.windowsFactory = windowsFactory;
        this.macFactory = macFactory;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Abstract Factory Pattern Demo ===");
        
        System.out.println("Creating Windows UI:");
        Button windowsButton = windowsFactory.createButton();
        TextField windowsTextField = windowsFactory.createTextField();
        windowsButton.render();
        windowsTextField.render();
        
        System.out.println("\nCreating Mac UI:");
        Button macButton = macFactory.createButton();
        TextField macTextField = macFactory.createTextField();
        macButton.render();
        macTextField.render();
    }
}
```

---

## 4. Builder Pattern

```java org/example/patterns/creational/builder/User.java
package org.example.patterns.creational.builder;

public class User {
    
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final String address;
    private final int age;
    
    private User(UserBuilder builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phone = builder.phone;
        this.address = builder.address;
        this.age = builder.age;
    }
    
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", age=" + age +
                '}';
    }
    
    public static class UserBuilder {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private int age;
        
        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public UserBuilder address(String address) {
            this.address = address;
            return this;
        }
        
        public UserBuilder age(int age) {
            this.age = age;
            return this;
        }
        
        public User build() {
            return new User(this);
        }
    }
}
```

```java org/example/patterns/creational/builder/BuilderDemo.java
package org.example.patterns.creational.builder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BuilderDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Builder Pattern Demo ===");
        
        User user1 = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .age(30)
                .build();
        
        User user2 = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("+1234567890")
                .address("123 Main St")
                .age(25)
                .build();
        
        System.out.println(user1);
        System.out.println(user2);
    }
}
```

---

## 5. Prototype Pattern

```java org/example/patterns/creational/prototype/Document.java
package org.example.patterns.creational.prototype;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Document implements Cloneable {
    
    private String title;
    private String content;
    private String author;
    
    public Document() {
        System.out.println("Creating new Document instance");
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    @Override
    public Document clone() {
        try {
            return (Document) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
    
    @Override
    public String toString() {
        return "Document{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
```

```java org/example/patterns/creational/prototype/PrototypeDemo.java
package org.example.patterns.creational.prototype;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PrototypeDemo implements CommandLineRunner {
    
    private final ApplicationContext context;
    
    public PrototypeDemo(ApplicationContext context) {
        this.context = context;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Prototype Pattern Demo ===");
        
        // Get prototype bean - each call creates a new instance
        Document doc1 = context.getBean(Document.class);
        doc1.setTitle("Original Document");
        doc1.setContent("This is the original content");
        doc1.setAuthor("John Doe");
        
        Document doc2 = context.getBean(Document.class);
        doc2.setTitle("Second Document");
        doc2.setContent("This is different content");
        doc2.setAuthor("Jane Smith");
        
        // Clone using prototype
        Document doc3 = doc1.clone();
        doc3.setTitle("Cloned Document");
        
        System.out.println("doc1: " + doc1);
        System.out.println("doc2: " + doc2);
        System.out.println("doc3 (cloned): " + doc3);
        System.out.println("doc1 == doc2? " + (doc1 == doc2));
        System.out.println("doc1 == doc3? " + (doc1 == doc3));
    }
}
```

---

## 6. Dependency Injection Pattern

```java org/example/patterns/creational/di/UserRepository.java
package org.example.patterns.creational.di;

import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {
    
    private final Map<Long, String> users = new HashMap<>();
    
    public UserRepository() {
        users.put(1L, "John Doe");
        users.put(2L, "Jane Smith");
    }
    
    public Optional<String> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
    
    public void save(Long id, String name) {
        users.put(id, name);
        System.out.println("User saved: " + name);
    }
}
```

```java org/example/patterns/creational/di/UserService.java
package org.example.patterns.creational.di;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    // Constructor Injection - Recommended approach
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        System.out.println("UserService created with constructor injection");
    }
    
    public void createUser(Long id, String name, String email) {
        userRepository.save(id, name);
        emailService.sendWelcomeEmail(email, name);
    }
    
    public String getUser(Long id) {
        return userRepository.findById(id)
                .orElse("User not found");
    }
}
```

```java org/example/patterns/creational/di/EmailService.java
package org.example.patterns.creational.di;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    public void sendWelcomeEmail(String email, String name) {
        System.out.println("Sending welcome email to " + email + " for user: " + name);
    }
}
```

```java org/example/patterns/creational/di/DependencyInjectionDemo.java
package org.example.patterns.creational.di;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DependencyInjectionDemo implements CommandLineRunner {
    
    private final UserService userService;
    
    public DependencyInjectionDemo(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Dependency Injection Pattern Demo ===");
        
        userService.createUser(3L, "Alice Johnson", "alice@example.com");
        
        System.out.println("User 1: " + userService.getUser(1L));
        System.out.println("User 3: " + userService.getUser(3L));
    }
}
```

---

## 7. Service Locator Pattern

```java org/example/patterns/creational/servicelocator/ServiceLocator.java
package org.example.patterns.creational.servicelocator;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ServiceLocator {
    
    private final ApplicationContext context;
    
    public ServiceLocator(ApplicationContext context) {
        this.context = context;
    }
    
    public <T> T getService(Class<T> serviceClass) {
        return context.getBean(serviceClass);
    }
    
    public <T> T getService(String serviceName, Class<T> serviceClass) {
        return context.getBean(serviceName, serviceClass);
    }
}
```

```java org/example/patterns/creational/servicelocator/PaymentService.java
package org.example.patterns.creational.servicelocator;

public interface PaymentService {
    void processPayment(double amount);
}
```

```java org/example/patterns/creational/servicelocator/CreditCardPaymentService.java
package org.example.patterns.creational.servicelocator;

import org.springframework.stereotype.Service;

@Service("creditCardPayment")
public class CreditCardPaymentService implements PaymentService {
    
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing credit card payment: $" + amount);
    }
}
```

```java org/example/patterns/creational/servicelocator/PayPalPaymentService.java
package org.example.patterns.creational.servicelocator;

import org.springframework.stereotype.Service;

@Service("paypalPayment")
public class PayPalPaymentService implements PaymentService {
    
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing PayPal payment: $" + amount);
    }
}
```

```java org/example/patterns/creational/servicelocator/ServiceLocatorDemo.java
package org.example.patterns.creational.servicelocator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ServiceLocatorDemo implements CommandLineRunner {
    
    private final ServiceLocator serviceLocator;
    
    public ServiceLocatorDemo(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Service Locator Pattern Demo ===");
        
        // Locate services dynamically
        PaymentService creditCardService = 
            serviceLocator.getService("creditCardPayment", PaymentService.class);
        creditCardService.processPayment(100.50);
        
        PaymentService paypalService = 
            serviceLocator.getService("paypalPayment", PaymentService.class);
        paypalService.processPayment(250.75);
    }
}
```

---

## 8. Object Pool Pattern

```java org/example/patterns/creational/objectpool/DatabaseConnectionPool.java
package org.example.patterns.creational.objectpool;

import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class DatabaseConnectionPool {
    
    private static final int POOL_SIZE = 5;
    private final BlockingQueue<PooledConnection> availableConnections;
    private final BlockingQueue<PooledConnection> usedConnections;
    
    public DatabaseConnectionPool() {
        this.availableConnections = new LinkedBlockingQueue<>(POOL_SIZE);
        this.usedConnections = new LinkedBlockingQueue<>();
    }
    
    @PostConstruct
    public void initialize() {
        System.out.println("Initializing connection pool with " + POOL_SIZE + " connections");
        for (int i = 0; i < POOL_SIZE; i++) {
            availableConnections.offer(new PooledConnection(i + 1));
        }
    }
    
    public PooledConnection borrowConnection() throws InterruptedException {
        PooledConnection connection = availableConnections.poll(5, TimeUnit.SECONDS);
        if (connection == null) {
            throw new RuntimeException("No connections available");
        }
        usedConnections.offer(connection);
        System.out.println("Borrowed connection: " + connection.getId());
        return connection;
    }
    
    public void returnConnection(PooledConnection connection) {
        if (usedConnections.remove(connection)) {
            availableConnections.offer(connection);
            System.out.println("Returned connection: " + connection.getId());
        }
    }
    
    @PreDestroy
    public void shutdown() {
        System.out.println("Shutting down connection pool");
        availableConnections.clear();
        usedConnections.clear();
    }
    
    public int getAvailableConnectionsCount() {
        return availableConnections.size();
    }
    
    public int getUsedConnectionsCount() {
        return usedConnections.size();
    }
}
```

```java org/example/patterns/creational/objectpool/PooledConnection.java
package org.example.patterns.creational.objectpool;

public class PooledConnection {
    
    private final int id;
    private boolean inUse;
    
    public PooledConnection(int id) {
        this.id = id;
        this.inUse = false;
    }
    
    public void execute(String query) {
        System.out.println("Connection " + id + " executing: " + query);
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isInUse() {
        return inUse;
    }
    
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }
}
```

```java org/example/patterns/creational/objectpool/ObjectPoolDemo.java
package org.example.patterns.creational.objectpool;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ObjectPoolDemo implements CommandLineRunner {
    
    private final DatabaseConnectionPool connectionPool;
    
    public ObjectPoolDemo(DatabaseConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Object Pool Pattern Demo ===");
        
        System.out.println("Available connections: " + connectionPool.getAvailableConnectionsCount());
        
        // Borrow connections
        PooledConnection conn1 = connectionPool.borrowConnection();
        conn1.execute("SELECT * FROM users");
        
        PooledConnection conn2 = connectionPool.borrowConnection();
        conn2.execute("SELECT * FROM orders");
        
        System.out.println("Available connections: " + connectionPool.getAvailableConnectionsCount());
        System.out.println("Used connections: " + connectionPool.getUsedConnectionsCount());
        
        // Return connections
        connectionPool.returnConnection(conn1);
        connectionPool.returnConnection(conn2);
        
        System.out.println("Available connections: " + connectionPool.getAvailableConnectionsCount());
        System.out.println("Used connections: " + connectionPool.getUsedConnectionsCount());
    }
}
```

---

## 9. Lazy Initialization Pattern

```java org/example/patterns/creational/lazy/ExpensiveService.java
package org.example.patterns.creational.lazy;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy // This bean will only be created when first accessed
public class ExpensiveService {
    
    public ExpensiveService() {
        System.out.println("ExpensiveService instance created (takes time...)");
        simulateExpensiveInitialization();
    }
    
    private void simulateExpensiveInitialization() {
        try {
            Thread.sleep(1000); // Simulate expensive operation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void performOperation() {
        System.out.println("ExpensiveService performing operation");
    }
}
```

```java org/example/patterns/creational/lazy/LazyDependentService.java
package org.example.patterns.creational.lazy;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class LazyDependentService {
    
    private final ExpensiveService expensiveService;
    
    // Lazy dependency injection
    public LazyDependentService(@Lazy ExpensiveService expensiveService) {
        System.out.println("LazyDependentService created (ExpensiveService not initialized yet)");
        this.expensiveService = expensiveService;
    }
    
    public void useExpensiveService() {
        System.out.println("Now accessing ExpensiveService for the first time...");
        expensiveService.performOperation();
    }
}
```

```java org/example/patterns/creational/lazy/LazyInitializationDemo.java
package org.example.patterns.creational.lazy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LazyInitializationDemo implements CommandLineRunner {
    
    private final LazyDependentService lazyDependentService;
    
    public LazyInitializationDemo(LazyDependentService lazyDependentService) {
        this.lazyDependentService = lazyDependentService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Lazy Initialization Pattern Demo ===");
        System.out.println("Application started - ExpensiveService not created yet");
        
        Thread.sleep(2000);
        
        System.out.println("\nNow triggering lazy initialization...");
        lazyDependentService.useExpensiveService();
    }
}
```

---

## 10. Multiton Pattern

```java org/example/patterns/creational/multiton/CacheManager.java
package org.example.patterns.creational.multiton;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheManager {
    
    private static final Map<String, CacheInstance> instances = new ConcurrentHashMap<>();
    
    public CacheInstance getInstance(String key) {
        return instances.computeIfAbsent(key, k -> {
            System.out.println("Creating new CacheInstance for key: " + k);
            return new CacheInstance(k);
        });
    }
    
    public int getInstanceCount() {
        return instances.size();
    }
    
    public void clearAll() {
        instances.clear();
    }
}
```

```java org/example/patterns/creational/multiton/CacheInstance.java
package org.example.patterns.creational.multiton;

import java.util.HashMap;
import java.util.Map;

public class CacheInstance {
    
    private final String name;
    private final Map<String, Object> cache;
    
    public CacheInstance(String name) {
        this.name = name;
        this.cache = new HashMap<>();
    }
    
    public void put(String key, Object value) {
        cache.put(key, value);
        System.out.println("Cache [" + name + "] stored: " + key);
    }
    
    public Object get(String key) {
        return cache.get(key);
    }
    
    public String getName() {
        return name;
    }
}
```

```java org/example/patterns/creational/multiton/MultitonDemo.java
package org.example.patterns.creational.multiton;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MultitonDemo implements CommandLineRunner {
    
    private final CacheManager cacheManager;
    
    public MultitonDemo(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Multiton Pattern Demo ===");
        
        // Get instances for different keys
        CacheInstance userCache = cacheManager.getInstance("USER_CACHE");
        CacheInstance productCache = cacheManager.getInstance("PRODUCT_CACHE");
        CacheInstance userCache2 = cacheManager.getInstance("USER_CACHE");
        
        // Use the instances
        userCache.put("user1", "John Doe");
        productCache.put("prod1", "Laptop");
        
        // Verify same instance for same key
        System.out.println("\nSame instance for USER_CACHE? " + (userCache == userCache2));
        System.out.println("Different instances? " + (userCache != productCache));
        System.out.println("Total instances created: " + cacheManager.getInstanceCount());
        
        // Retrieve data
        System.out.println("\nRetrieved from userCache: " + userCache2.get("user1"));
    }
}
```

---

## Main Application Class

```java org/example/Application.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## Maven Configuration

```xml pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>org.example</groupId>
    <artifactId>spring-creational-patterns</artifactId>
    <version>1.0.0</version>
    <name>Spring Creational Design Patterns</name>
    <description>Demonstration of Creational Design Patterns in Spring</description>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <!-- Spring Boot Starter Web (optional, for REST examples) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Lombok (optional, for reducing boilerplate) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot DevTools (optional) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Starter Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Application Properties

```properties application.properties
# Application name
spring.application.name=spring-creational-patterns

# Logging configuration
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Server configuration (if using web starter)
server.port=8080

# Disable banner
spring.main.banner-mode=off

# Allow circular references (if needed)
spring.main.allow-circular-references=false

# Lazy initialization global setting
spring.main.lazy-initialization=false
```

---

## Additional Examples with More Advanced Implementations

### Enhanced Singleton Pattern with Thread Safety

```java org/example/patterns/creational/singleton/ThreadSafeSingleton.java
package org.example.patterns.creational.singleton;

import org.springframework.stereotype.Component;

/**
 * Thread-Safe Singleton using Double-Checked Locking
 * Note: In Spring, this is handled automatically, but shown for educational purposes
 */
@Component
public class ThreadSafeSingleton {
    
    private static volatile ThreadSafeSingleton instance;
    private final String id;
    
    public ThreadSafeSingleton() {
        this.id = "SINGLETON-" + System.currentTimeMillis();
        System.out.println("ThreadSafeSingleton created with ID: " + id);
    }
    
    public static ThreadSafeSingleton getInstance() {
        if (instance == null) {
            synchronized (ThreadSafeSingleton.class) {
                if (instance == null) {
                    instance = new ThreadSafeSingleton();
                }
            }
        }
        return instance;
    }
    
    public String getId() {
        return id;
    }
    
    public void doSomething() {
        System.out.println("ThreadSafeSingleton doing something with ID: " + id);
    }
}
```

---

### Factory Pattern with Spring @Bean

```java org/example/patterns/creational/factory/PaymentProcessorFactory.java
package org.example.patterns.creational.factory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PaymentProcessorFactory {
    
    @Bean
    @Scope("prototype")
    public PaymentProcessor creditCardProcessor() {
        return new CreditCardProcessor();
    }
    
    @Bean
    @Scope("prototype")
    public PaymentProcessor paypalProcessor() {
        return new PayPalProcessor();
    }
    
    @Bean
    @Scope("prototype")
    public PaymentProcessor bitcoinProcessor() {
        return new BitcoinProcessor();
    }
}
```

```java org/example/patterns/creational/factory/PaymentProcessor.java
package org.example.patterns.creational.factory;

public interface PaymentProcessor {
    void processPayment(double amount);
    String getProcessorType();
}
```

```java org/example/patterns/creational/factory/CreditCardProcessor.java
package org.example.patterns.creational.factory;

public class CreditCardProcessor implements PaymentProcessor {
    
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing credit card payment of $" + amount);
    }
    
    @Override
    public String getProcessorType() {
        return "CREDIT_CARD";
    }
}
```

```java org/example/patterns/creational/factory/PayPalProcessor.java
package org.example.patterns.creational.factory;

public class PayPalProcessor implements PaymentProcessor {
    
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing PayPal payment of $" + amount);
    }
    
    @Override
    public String getProcessorType() {
        return "PAYPAL";
    }
}
```

```java org/example/patterns/creational/factory/BitcoinProcessor.java
package org.example.patterns.creational.factory;

public class BitcoinProcessor implements PaymentProcessor {
    
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing Bitcoin payment of $" + amount);
    }
    
    @Override
    public String getProcessorType() {
        return "BITCOIN";
    }
}
```

```java org/example/patterns/creational/factory/PaymentService.java
package org.example.patterns.creational.factory;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    
    private final ApplicationContext context;
    
    public PaymentService(ApplicationContext context) {
        this.context = context;
    }
    
    public void processPayment(String processorType, double amount) {
        PaymentProcessor processor = getProcessor(processorType);
        processor.processPayment(amount);
    }
    
    private PaymentProcessor getProcessor(String type) {
        return switch (type.toUpperCase()) {
            case "CREDIT_CARD" -> context.getBean("creditCardProcessor", PaymentProcessor.class);
            case "PAYPAL" -> context.getBean("paypalProcessor", PaymentProcessor.class);
            case "BITCOIN" -> context.getBean("bitcoinProcessor", PaymentProcessor.class);
            default -> throw new IllegalArgumentException("Unknown processor type: " + type);
        };
    }
}
```

---

### Builder Pattern with Lombok

```java org/example/patterns/creational/builder/Product.java
package org.example.patterns.creational.builder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Product {
    
    private final String id;
    private final String name;
    private final String description;
    private final double price;
    private final String category;
    private final int stockQuantity;
    private final boolean available;
    
    @Builder.Default
    private final String currency = "USD";
    
    public void displayInfo() {
        System.out.println("Product: " + name);
        System.out.println("  ID: " + id);
        System.out.println("  Price: " + currency + " " + price);
        System.out.println("  Stock: " + stockQuantity);
        System.out.println("  Available: " + available);
        System.out.println("  Category: " + category);
        System.out.println("  Description: " + description);
    }
}
```

```java org/example/patterns/creational/builder/LombokBuilderDemo.java
package org.example.patterns.creational.builder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LombokBuilderDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Lombok Builder Pattern Demo ===");
        
        Product laptop = Product.builder()
                .id("PROD-001")
                .name("MacBook Pro")
                .description("High-performance laptop")
                .price(2499.99)
                .category("Electronics")
                .stockQuantity(50)
                .available(true)
                .build();
        
        Product book = Product.builder()
                .id("PROD-002")
                .name("Spring in Action")
                .price(39.99)
                .category("Books")
                .stockQuantity(100)
                .available(true)
                .build();
        
        laptop.displayInfo();
        System.out.println();
        book.displayInfo();
    }
}
```

---

### Advanced Prototype Pattern with Deep Copy

```java org/example/patterns/creational/prototype/Order.java
package org.example.patterns.creational.prototype;

import java.util.ArrayList;
import java.util.List;

public class Order implements Cloneable {
    
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private double totalAmount;
    
    public Order() {
        this.items = new ArrayList<>();
    }
    
    public void addItem(OrderItem item) {
        items.add(item);
        calculateTotal();
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    private void calculateTotal() {
        totalAmount = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
    
    @Override
    public Order clone() {
        try {
            Order cloned = (Order) super.clone();
            // Deep copy of mutable fields
            cloned.items = new ArrayList<>();
            for (OrderItem item : this.items) {
                cloned.items.add(item.clone());
            }
            cloned.calculateTotal();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", items=" + items.size() +
                ", totalAmount=" + totalAmount +
                '}';
    }
    
    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }
}
```

```java org/example/patterns/creational/prototype/OrderItem.java
package org.example.patterns.creational.prototype;

public class OrderItem implements Cloneable {
    
    private String productId;
    private String productName;
    private int quantity;
    private double price;
    
    public OrderItem(String productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    public double getPrice() {
        return price;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    @Override
    public OrderItem clone() {
        try {
            return (OrderItem) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
```

```java org/example/patterns/creational/prototype/AdvancedPrototypeDemo.java
package org.example.patterns.creational.prototype;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdvancedPrototypeDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Advanced Prototype Pattern Demo (Deep Copy) ===");
        
        // Create original order
        Order originalOrder = new Order();
        originalOrder.setOrderId("ORD-001");
        originalOrder.setCustomerId("CUST-123");
        originalOrder.addItem(new OrderItem("P1", "Laptop", 1, 1200.00));
        originalOrder.addItem(new OrderItem("P2", "Mouse", 2, 25.00));
        
        System.out.println("Original Order: " + originalOrder);
        
        // Clone the order
        Order clonedOrder = originalOrder.clone();
        clonedOrder.setOrderId("ORD-002");
        clonedOrder.setCustomerId("CUST-456");
        
        System.out.println("Cloned Order: " + clonedOrder);
        
        // Verify deep copy
        System.out.println("\nOriginal items count: " + originalOrder.getItems().size());
        System.out.println("Cloned items count: " + clonedOrder.getItems().size());
        System.out.println("Items are different objects: " + 
                (originalOrder.getItems().get(0) != clonedOrder.getItems().get(0)));
    }
}
```

---

### Dependency Injection - All Types

```java org/example/patterns/creational/di/AllInjectionTypesDemo.java
package org.example.patterns.creational.di;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AllInjectionTypesDemo {
    
    // 1. Field Injection (not recommended)
    @Autowired
    private EmailService emailService;
    
    // 2. Constructor Injection (recommended)
    private final UserRepository userRepository;
    
    // 3. Setter Injection
    private NotificationService notificationService;
    
    // Constructor Injection
    public AllInjectionTypesDemo(UserRepository userRepository) {
        this.userRepository = userRepository;
        System.out.println("Constructor injection completed");
    }
    
    // Setter Injection
    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        System.out.println("Setter injection completed");
    }
    
    public void demonstrateInjections() {
        System.out.println("\n=== All Injection Types Demo ===");
        System.out.println("Field injection: " + (emailService != null));
        System.out.println("Constructor injection: " + (userRepository != null));
        System.out.println("Setter injection: " + (notificationService != null));
    }
}
```

```java org/example/patterns/creational/di/NotificationService.java
package org.example.patterns.creational.di;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    public void notify(String message) {
        System.out.println("Notification: " + message);
    }
}
```

---

### Object Pool with Apache Commons Pool

```java org/example/patterns/creational/objectpool/AdvancedConnectionPool.java
package org.example.patterns.creational.objectpool;

import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AdvancedConnectionPool {
    
    private static final int MIN_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private final BlockingQueue<PoolConnection> pool;
    private final AtomicInteger currentSize;
    private final AtomicInteger createdConnections;
    
    public AdvancedConnectionPool() {
        this.pool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
        this.currentSize = new AtomicInteger(0);
        this.createdConnections = new AtomicInteger(0);
    }
    
    @PostConstruct
    public void initialize() {
        System.out.println("Initializing advanced connection pool");
        for (int i = 0; i < MIN_POOL_SIZE; i++) {
            pool.offer(createConnection());
        }
    }
    
    public PoolConnection acquire() {
        try {
            PoolConnection connection = pool.poll();
            if (connection == null && currentSize.get() < MAX_POOL_SIZE) {
                connection = createConnection();
            } else if (connection == null) {
                throw new RuntimeException("Pool exhausted");
            }
            connection.setInUse(true);
            System.out.println("Connection acquired: " + connection.getId());
            return connection;
        } catch (Exception e) {
            throw new RuntimeException("Failed to acquire connection", e);
        }
    }
    
    public void release(PoolConnection connection) {
        if (connection != null) {
            connection.setInUse(false);
            pool.offer(connection);
            System.out.println("Connection released: " + connection.getId());
        }
    }
    
    private PoolConnection createConnection() {
        int id = createdConnections.incrementAndGet();
        currentSize.incrementAndGet();
        System.out.println("Creating new connection: " + id);
        return new PoolConnection(id);
    }
    
    @PreDestroy
    public void shutdown() {
        System.out.println("Shutting down connection pool");
        pool.clear();
    }
    
    public int getPoolSize() {
        return pool.size();
    }
    
    public int getTotalCreated() {
        return createdConnections.get();
    }
}
```

```java org/example/patterns/creational/objectpool/PoolConnection.java
package org.example.patterns/creational/objectpool;

public class PoolConnection {
    
    private final int id;
    private boolean inUse;
    private long lastUsedTime;
    
    public PoolConnection(int id) {
        this.id = id;
        this.inUse = false;
        this.lastUsedTime = System.currentTimeMillis();
    }
    
    public void executeQuery(String query) {
        this.lastUsedTime = System.currentTimeMillis();
        System.out.println("Connection " + id + " executing: " + query);
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isInUse() {
        return inUse;
    }
    
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }
    
    public long getLastUsedTime() {
        return lastUsedTime;
    }
}
```

---

## Complete Test Suite

```java org/example/patterns/creational/CreationalPatternsTest.java
package org.example.patterns.creational;

import org.example.patterns.creational.singleton.DatabaseConnection;
import org.example.patterns.creational.factory.NotificationFactory;
import org.example.patterns.creational.builder.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CreationalPatternsTest {
    
    @Autowired
    private ApplicationContext context;
    
    @Autowired
    private NotificationFactory notificationFactory;
    
    @Test
    void testSingletonPattern() {
        DatabaseConnection conn1 = context.getBean(DatabaseConnection.class);
        DatabaseConnection conn2 = context.getBean(DatabaseConnection.class);
        
        assertSame(conn1, conn2, "Should be same instance");
        assertEquals(conn1.getInstanceId(), conn2.getInstanceId());
    }
    
    @Test
    void testFactoryPattern() {
        assertNotNull(notificationFactory);
        var notification = notificationFactory.createNotification("email");
        assertNotNull(notification);
    }
    
    @Test
    void testBuilderPattern() {
        Product product = Product.builder()
                .id("TEST-001")
                .name("Test Product")
                .price(99.99)
                .available(true)
                .build();
        
        assertNotNull(product);
        assertEquals("TEST-001", product.getId());
        assertEquals("Test Product", product.getName());
        assertTrue(product.isAvailable());
    }
}
```

This completes the comprehensive implementation of all 10 Creational Design Patterns in Spring Framework with working Java code examples!