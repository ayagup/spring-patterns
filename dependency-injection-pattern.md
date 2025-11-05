# Spring Dependency Injection Patterns - Java Implementations

## Project Setup

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
    <artifactId>spring-di-patterns</artifactId>
    <version>1.0.0</version>
    <name>Spring Dependency Injection Patterns</name>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
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
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 1. Constructor Injection Pattern

```java org/example/patterns/di/constructor/EmailService.java
package org.example.patterns.di.constructor;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    public void sendEmail(String to, String subject, String body) {
        System.out.println("EmailService: Sending email");
        System.out.println("  To: " + to);
        System.out.println("  Subject: " + subject);
        System.out.println("  Body: " + body);
    }
}
```

```java org/example/patterns/di/constructor/UserRepository.java
package org.example.patterns.di.constructor;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {
    
    private final Map<Long, String> users = new HashMap<>();
    
    public UserRepository() {
        users.put(1L, "john@example.com");
        users.put(2L, "jane@example.com");
    }
    
    public Optional<String> findEmailById(Long id) {
        System.out.println("UserRepository: Finding user " + id);
        return Optional.ofNullable(users.get(id));
    }
    
    public void save(Long id, String email) {
        System.out.println("UserRepository: Saving user " + id);
        users.put(id, email);
    }
}
```

```java org/example/patterns/di/constructor/UserService.java
package org.example.patterns.di.constructor;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    // Constructor Injection - Recommended approach
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    // Spring automatically injects dependencies through constructor
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        System.out.println("UserService: Constructor injection completed");
    }
    
    public void registerUser(Long userId, String email) {
        userRepository.save(userId, email);
        emailService.sendEmail(email, "Welcome", "Thank you for registering!");
    }
    
    public void notifyUser(Long userId, String message) {
        userRepository.findEmailById(userId).ifPresent(email -> 
            emailService.sendEmail(email, "Notification", message)
        );
    }
}
```

```java org/example/patterns/di/constructor/ConstructorInjectionDemo.java
package org.example.patterns.di.constructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ConstructorInjectionDemo implements CommandLineRunner {
    
    private final UserService userService;
    
    public ConstructorInjectionDemo(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Constructor Injection Pattern Demo ===");
        System.out.println("Advantages: Immutable, testable, prevents null dependencies\n");
        
        userService.registerUser(3L, "alice@example.com");
        userService.notifyUser(1L, "Your account has been updated");
    }
}
```

---

## 2. Setter Injection Pattern

```java org/example/patterns/di/setter/LoggingService.java
package org.example.patterns.di.setter;

import org.springframework.stereotype.Service;

@Service
public class LoggingService {
    
    public void log(String message) {
        System.out.println("LoggingService: " + message);
    }
}
```

```java org/example/patterns/di/setter/NotificationService.java
package org.example.patterns.di.setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    private LoggingService loggingService;
    private String defaultMessage = "Default notification";
    
    // Setter Injection - Optional dependency
    @Autowired
    public void setLoggingService(LoggingService loggingService) {
        System.out.println("NotificationService: Setter injection for LoggingService");
        this.loggingService = loggingService;
    }
    
    // Property setter
    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }
    
    public void sendNotification(String message) {
        if (loggingService != null) {
            loggingService.log("Sending notification: " + message);
        }
        System.out.println("NotificationService: " + message);
    }
    
    public void sendDefaultNotification() {
        sendNotification(defaultMessage);
    }
}
```

```java org/example/patterns/di/setter/SetterInjectionDemo.java
package org.example.patterns.di.setter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class SetterInjectionDemo implements CommandLineRunner {
    
    private final NotificationService notificationService;
    
    public SetterInjectionDemo(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Setter Injection Pattern Demo ===");
        System.out.println("Advantages: Optional dependencies, reconfigurable\n");
        
        notificationService.setDefaultMessage("Custom default message");
        notificationService.sendNotification("Important update!");
        notificationService.sendDefaultNotification();
    }
}
```

---

## 3. Field Injection Pattern

```java org/example/patterns/di/field/CacheService.java
package org.example.patterns.di.field;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CacheService {
    
    private final Map<String, Object> cache = new HashMap<>();
    
    public void put(String key, Object value) {
        cache.put(key, value);
        System.out.println("CacheService: Cached " + key);
    }
    
    public Object get(String key) {
        System.out.println("CacheService: Retrieved " + key);
        return cache.get(key);
    }
    
    public void clear() {
        cache.clear();
        System.out.println("CacheService: Cache cleared");
    }
}
```

```java org/example/patterns/di/field/DataService.java
package org.example.patterns.di.field;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataService {
    
    // Field Injection - Not recommended but widely used
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private LogService logService;
    
    public DataService() {
        System.out.println("DataService: Constructor called (dependencies not yet injected)");
    }
    
    public void processData(String key, String data) {
        logService.log("Processing data for key: " + key);
        cacheService.put(key, data);
        System.out.println("DataService: Data processed");
    }
    
    public Object retrieveData(String key) {
        logService.log("Retrieving data for key: " + key);
        return cacheService.get(key);
    }
}
```

```java org/example/patterns/di/field/LogService.java
package org.example.patterns.di.field;

import org.springframework.stereotype.Service;

@Service
public class LogService {
    
    public void log(String message) {
        System.out.println("LogService: " + message);
    }
}
```

```java org/example/patterns/di/field/FieldInjectionDemo.java
package org.example.patterns.di.field;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class FieldInjectionDemo implements CommandLineRunner {
    
    private final DataService dataService;
    
    public FieldInjectionDemo(DataService dataService) {
        this.dataService = dataService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Field Injection Pattern Demo ===");
        System.out.println("Note: Not recommended - harder to test, hides dependencies\n");
        
        dataService.processData("user:1", "John Doe");
        dataService.retrieveData("user:1");
    }
}
```

---

## 4. Method Injection Pattern

```java org/example/patterns/di/method/ConfigurationService.java
package org.example.patterns.di.method;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigurationService {
    
    private final Map<String, String> config = new HashMap<>();
    
    public ConfigurationService() {
        config.put("app.name", "Spring DI Patterns");
        config.put("app.version", "1.0.0");
    }
    
    public String getConfig(String key) {
        System.out.println("ConfigurationService: Getting config for " + key);
        return config.getOrDefault(key, "default");
    }
}
```

```java org/example/patterns/di/method/ApplicationService.java
package org.example.patterns.di.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
    
    private ConfigurationService configurationService;
    private MetricsService metricsService;
    
    // Method Injection - entire method injected
    @Autowired
    public void initialize(ConfigurationService configurationService, 
                          MetricsService metricsService) {
        System.out.println("ApplicationService: Method injection - initialize");
        this.configurationService = configurationService;
        this.metricsService = metricsService;
        
        // Can perform initialization logic here
        String appName = configurationService.getConfig("app.name");
        metricsService.recordMetric("app.initialized", appName);
    }
    
    public void performOperation(String operation) {
        System.out.println("ApplicationService: Performing " + operation);
        metricsService.recordMetric("operation.performed", operation);
    }
}
```

```java org/example/patterns/di/method/MetricsService.java
package org.example.patterns.di.method;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MetricsService {
    
    private final Map<String, Integer> metrics = new HashMap<>();
    
    public void recordMetric(String name, String value) {
        System.out.println("MetricsService: Recording metric - " + name + ": " + value);
        metrics.merge(name, 1, Integer::sum);
    }
    
    public void showMetrics() {
        System.out.println("\n--- Metrics ---");
        metrics.forEach((key, value) -> 
            System.out.println("  " + key + ": " + value + " times"));
    }
}
```

```java org/example/patterns/di/method/MethodInjectionDemo.java
package org.example.patterns.di.method;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class MethodInjectionDemo implements CommandLineRunner {
    
    private final ApplicationService applicationService;
    private final MetricsService metricsService;
    
    public MethodInjectionDemo(ApplicationService applicationService, 
                              MetricsService metricsService) {
        this.applicationService = applicationService;
        this.metricsService = metricsService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Method Injection Pattern Demo ===");
        System.out.println("Advantages: Can inject multiple dependencies, initialization logic\n");
        
        applicationService.performOperation("data-processing");
        applicationService.performOperation("report-generation");
        
        metricsService.showMetrics();
    }
}
```

---

## 5. Lookup Method Injection Pattern

```java org/example/patterns/di/lookup/PrototypeBean.java
package org.example.patterns.di.lookup;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Scope("prototype")
public class PrototypeBean {
    
    private final String id;
    private final LocalDateTime createdAt;
    
    public PrototypeBean() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.createdAt = LocalDateTime.now();
        System.out.println("PrototypeBean: New instance created - " + id);
    }
    
    public void doWork(String task) {
        System.out.println("PrototypeBean [" + id + "]: Performing task - " + task);
    }
    
    public String getId() {
        return id;
    }
}
```

```java org/example/patterns/di/lookup/SingletonBean.java
package org.example.patterns.di.lookup;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

@Component
public abstract class SingletonBean {
    
    // Lookup Method Injection - Spring implements this at runtime
    @Lookup
    public abstract PrototypeBean getPrototypeBean();
    
    public void processTask(String task) {
        System.out.println("\nSingletonBean: Processing task - " + task);
        
        // Each call returns a new instance
        PrototypeBean prototypeBean = getPrototypeBean();
        prototypeBean.doWork(task);
    }
    
    public void demonstrateLookup() {
        System.out.println("\n--- Demonstrating Lookup Method Injection ---");
        
        PrototypeBean bean1 = getPrototypeBean();
        PrototypeBean bean2 = getPrototypeBean();
        PrototypeBean bean3 = getPrototypeBean();
        
        System.out.println("Bean 1 ID: " + bean1.getId());
        System.out.println("Bean 2 ID: " + bean2.getId());
        System.out.println("Bean 3 ID: " + bean3.getId());
        System.out.println("All different instances: " + 
            (bean1 != bean2 && bean2 != bean3 && bean1 != bean3));
    }
}
```

```java org/example/patterns/di/lookup/LookupMethodInjectionDemo.java
package org.example.patterns.di.lookup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class LookupMethodInjectionDemo implements CommandLineRunner {
    
    private final SingletonBean singletonBean;
    
    public LookupMethodInjectionDemo(SingletonBean singletonBean) {
        this.singletonBean = singletonBean;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Lookup Method Injection Pattern Demo ===");
        System.out.println("Use case: Singleton bean needs prototype-scoped dependencies\n");
        
        singletonBean.demonstrateLookup();
        
        singletonBean.processTask("Task 1");
        singletonBean.processTask("Task 2");
        singletonBean.processTask("Task 3");
    }
}
```

---

## 6. Auto-wiring Pattern

```java org/example/patterns/di/autowiring/DatabaseService.java
package org.example.patterns.di.autowiring;

import org.springframework.stereotype.Service;

@Service
public class DatabaseService {
    
    public void connect() {
        System.out.println("DatabaseService: Connected to database");
    }
    
    public void executeQuery(String query) {
        System.out.println("DatabaseService: Executing query - " + query);
    }
}
```

```java org/example/patterns/di/autowiring/ValidationService.java
package org.example.patterns.di.autowiring;

import org.springframework.stereotype.Service;

@Service
public class ValidationService {
    
    public boolean validate(String data) {
        System.out.println("ValidationService: Validating data");
        return data != null && !data.isEmpty();
    }
}
```

```java org/example/patterns/di/autowiring/TransactionService.java
package org.example.patterns.di.autowiring;

import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    
    public void beginTransaction() {
        System.out.println("TransactionService: Transaction started");
    }
    
    public void commit() {
        System.out.println("TransactionService: Transaction committed");
    }
    
    public void rollback() {
        System.out.println("TransactionService: Transaction rolled back");
    }
}
```

```java org/example/patterns/di/autowiring/AutoWiredService.java
package org.example.patterns.di.autowiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutoWiredService {
    
    // Auto-wiring by type
    @Autowired
    private DatabaseService databaseService;
    
    @Autowired
    private ValidationService validationService;
    
    @Autowired
    private TransactionService transactionService;
    
    public AutoWiredService() {
        System.out.println("AutoWiredService: Constructor - dependencies will be auto-wired");
    }
    
    public void performDatabaseOperation(String data) {
        System.out.println("\nAutoWiredService: Performing database operation");
        
        if (validationService.validate(data)) {
            transactionService.beginTransaction();
            try {
                databaseService.connect();
                databaseService.executeQuery("INSERT INTO table VALUES ('" + data + "')");
                transactionService.commit();
            } catch (Exception e) {
                transactionService.rollback();
            }
        } else {
            System.out.println("Validation failed");
        }
    }
}
```

```java org/example/patterns/di/autowiring/AutoWiringDemo.java
package org.example.patterns.di.autowiring;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(6)
public class AutoWiringDemo implements CommandLineRunner {
    
    private final AutoWiredService autoWiredService;
    
    public AutoWiringDemo(AutoWiredService autoWiredService) {
        this.autoWiredService = autoWiredService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Auto-wiring Pattern Demo ===");
        System.out.println("Spring automatically wires dependencies by type\n");
        
        autoWiredService.performDatabaseOperation("Sample Data");
    }
}
```

---

## 7. Qualifier Pattern

```java org/example/patterns/di/qualifier/MessageSender.java
package org.example.patterns.di.qualifier;

public interface MessageSender {
    void send(String message, String recipient);
    String getType();
}
```

```java org/example/patterns/di/qualifier/EmailMessageSender.java
package org.example.patterns.di.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("emailSender")
public class EmailMessageSender implements MessageSender {
    
    @Override
    public void send(String message, String recipient) {
        System.out.println("EmailMessageSender: Sending email to " + recipient);
        System.out.println("  Message: " + message);
    }
    
    @Override
    public String getType() {
        return "EMAIL";
    }
}
```

```java org/example/patterns/di/qualifier/SmsMessageSender.java
package org.example.patterns.di.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("smsSender")
public class SmsMessageSender implements MessageSender {
    
    @Override
    public void send(String message, String recipient) {
        System.out.println("SmsMessageSender: Sending SMS to " + recipient);
        System.out.println("  Message: " + message);
    }
    
    @Override
    public String getType() {
        return "SMS";
    }
}
```

```java org/example/patterns/di/qualifier/PushMessageSender.java
package org.example.patterns.di.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("pushSender")
public class PushMessageSender implements MessageSender {
    
    @Override
    public void send(String message, String recipient) {
        System.out.println("PushMessageSender: Sending push notification to " + recipient);
        System.out.println("  Message: " + message);
    }
    
    @Override
    public String getType() {
        return "PUSH";
    }
}
```

```java org/example/patterns/di/qualifier/NotificationManager.java
package org.example.patterns.di.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class NotificationManager {
    
    private final MessageSender emailSender;
    private final MessageSender smsSender;
    private final MessageSender pushSender;
    
    // Using @Qualifier to specify which implementation to inject
    public NotificationManager(
            @Qualifier("emailSender") MessageSender emailSender,
            @Qualifier("smsSender") MessageSender smsSender,
            @Qualifier("pushSender") MessageSender pushSender) {
        this.emailSender = emailSender;
        this.smsSender = smsSender;
        this.pushSender = pushSender;
        System.out.println("NotificationManager: Qualified dependencies injected");
    }
    
    public void sendViaEmail(String message, String recipient) {
        emailSender.send(message, recipient);
    }
    
    public void sendViaSms(String message, String recipient) {
        smsSender.send(message, recipient);
    }
    
    public void sendViaPush(String message, String recipient) {
        pushSender.send(message, recipient);
    }
    
    public void sendViaAll(String message, String recipient) {
        System.out.println("\n--- Sending via all channels ---");
        emailSender.send(message, recipient);
        smsSender.send(message, recipient);
        pushSender.send(message, recipient);
    }
}
```

```java org/example/patterns/di/qualifier/QualifierDemo.java
package org.example.patterns.di.qualifier;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(7)
public class QualifierDemo implements CommandLineRunner {
    
    private final NotificationManager notificationManager;
    
    public QualifierDemo(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Qualifier Pattern Demo ===");
        System.out.println("Use @Qualifier when multiple beans of same type exist\n");
        
        notificationManager.sendViaEmail("Welcome!", "user@example.com");
        notificationManager.sendViaSms("Your code is 1234", "+1234567890");
        notificationManager.sendViaPush("New message received", "user-123");
        
        notificationManager.sendViaAll("Important announcement", "everyone");
    }
}
```

---

## 8. Primary Bean Pattern

```java org/example/patterns/di/primary/PaymentProcessor.java
package org.example.patterns.di.primary;

public interface PaymentProcessor {
    void processPayment(double amount);
    String getProcessorName();
}
```

```java org/example/patterns/di/primary/CreditCardProcessor.java
package org.example.patterns.di.primary;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary // This is the default implementation
public class CreditCardProcessor implements PaymentProcessor {
    
    @Override
    public void processPayment(double amount) {
        System.out.println("CreditCardProcessor: Processing payment of $" + amount);
    }
    
    @Override
    public String getProcessorName() {
        return "Credit Card";
    }
}
```

```java org/example/patterns/di/primary/PayPalProcessor.java
package org.example.patterns.di.primary;

import org.springframework.stereotype.Service;

@Service
public class PayPalProcessor implements PaymentProcessor {
    
    @Override
    public void processPayment(double amount) {
        System.out.println("PayPalProcessor: Processing payment of $" + amount);
    }
    
    @Override
    public String getProcessorName() {
        return "PayPal";
    }
}
```

```java org/example/patterns/di/primary/BitcoinProcessor.java
package org.example.patterns.di.primary;

import org.springframework.stereotype.Service;

@Service
public class BitcoinProcessor implements PaymentProcessor {
    
    @Override
    public void processPayment(double amount) {
        System.out.println("BitcoinProcessor: Processing payment of $" + amount);
    }
    
    @Override
    public String getProcessorName() {
        return "Bitcoin";
    }
}
```

```java org/example/patterns/di/primary/PaymentService.java
package org.example.patterns.di.primary;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    
    private final PaymentProcessor defaultProcessor;
    
    // Without @Qualifier, Spring injects the @Primary bean
    public PaymentService(PaymentProcessor defaultProcessor) {
        this.defaultProcessor = defaultProcessor;
        System.out.println("PaymentService: Default processor is " + 
                         defaultProcessor.getProcessorName());
    }
    
    public void makePayment(double amount) {
        System.out.println("\nPaymentService: Making payment using default processor");
        defaultProcessor.processPayment(amount);
    }
}
```

```java org/example/patterns/di/primary/PrimaryBeanDemo.java
package org.example.patterns.di.primary;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(8)
public class PrimaryBeanDemo implements CommandLineRunner {
    
    private final PaymentService paymentService;
    
    public PrimaryBeanDemo(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Primary Bean Pattern Demo ===");
        System.out.println("@Primary marks the default bean when multiple candidates exist\n");
        
        paymentService.makePayment(100.00);
        paymentService.makePayment(250.50);
    }
}
```

---

## 9. Profile-based Injection Pattern

```java org/example/patterns/di/profile/DataSource.java
package org.example.patterns.di.profile;

public interface DataSource {
    void connect();
    String getConnectionInfo();
}
```

```java org/example/patterns/di/profile/DevelopmentDataSource.java
package org.example.patterns.di.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevelopmentDataSource implements DataSource {
    
    @Override
    public void connect() {
        System.out.println("DevelopmentDataSource: Connecting to H2 in-memory database");
    }
    
    @Override
    public String getConnectionInfo() {
        return "jdbc:h2:mem:devdb";
    }
}
```

```java org/example/patterns/di/profile/ProductionDataSource.java
package org.example.patterns.di.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionDataSource implements DataSource {
    
    @Override
    public void connect() {
        System.out.println("ProductionDataSource: Connecting to PostgreSQL production database");
    }
    
    @Override
    public String getConnectionInfo() {
        return "jdbc:postgresql://prod-server:5432/proddb";
    }
}
```

```java org/example/patterns/di/profile/TestDataSource.java
package org.example.patterns.di.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestDataSource implements DataSource {
    
    @Override
    public void connect() {
        System.out.println("TestDataSource: Connecting to H2 test database");
    }
    
    @Override
    public String getConnectionInfo() {
        return "jdbc:h2:mem:testdb";
    }
}
```

```java org/example/patterns/di/profile/DefaultDataSource.java
package org.example.patterns.di.profile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("default")
public class DefaultDataSource implements DataSource {
    
    @Override
    public void connect() {
        System.out.println("DefaultDataSource: Connecting to default embedded database");
    }
    
    @Override
    public String getConnectionInfo() {
        return "jdbc:h2:mem:defaultdb";
    }
}
```

```java org/example/patterns/di/profile/DatabaseManager.java
package org.example.patterns.di.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseManager {
    
    private final List<DataSource> availableDataSources;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    // Spring injects only the DataSource beans matching active profile
    @Autowired(required = false)
    private DataSource dataSource;
    
    public DatabaseManager(List<DataSource> availableDataSources) {
        this.availableDataSources = availableDataSources;
        System.out.println("DatabaseManager: Found " + availableDataSources.size() + " DataSource(s)");
    }
    
    public void initialize() {
        System.out.println("\n--- Database Manager Initialization ---");
        System.out.println("Active profile: " + activeProfile);
        
        if (dataSource != null) {
            System.out.println("DataSource: " + dataSource.getClass().getSimpleName());
            System.out.println("Connection info: " + dataSource.getConnectionInfo());
            dataSource.connect();
        } else {
            System.out.println("No DataSource available for profile: " + activeProfile);
        }
    }
}
```

```java org/example/patterns/di/profile/ProfileBasedInjectionDemo.java
package org.example.patterns.di.profile;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(9)
public class ProfileBasedInjectionDemo implements CommandLineRunner {
    
    private final DatabaseManager databaseManager;
    
    public ProfileBasedInjectionDemo(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Profile-based Injection Pattern Demo ===");
        System.out.println("Different beans are injected based on active profile");
        System.out.println("Set spring.profiles.active=dev|test|prod in application.properties\n");
        
        databaseManager.initialize();
    }
}
```

---

## 10. Conditional Bean Pattern

```java org/example/patterns/di/conditional/OnPropertyCondition.java
package org.example.patterns.di.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnPropertyCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String property = context.getEnvironment().getProperty("feature.cache.enabled");
        return "true".equalsIgnoreCase(property);
    }
}
```

```java org/example/patterns/di/conditional/CacheManager.java
package org.example.patterns.di.conditional;

public interface CacheManager {
    void put(String key, Object value);
    Object get(String key);
    void clear();
}
```

```java org/example/patterns/di/conditional/RedisCacheManager.java
package org.example.patterns.di.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "cache.type", havingValue = "redis")
public class RedisCacheManager implements CacheManager {
    
    private final Map<String, Object> cache = new HashMap<>();
    
    public RedisCacheManager() {
        System.out.println("RedisCacheManager: Initialized (Redis cache enabled)");
    }
    
    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
        System.out.println("RedisCacheManager: Cached " + key + " in Redis");
    }
    
    @Override
    public Object get(String key) {
        System.out.println("RedisCacheManager: Retrieved " + key + " from Redis");
        return cache.get(key);
    }
    
    @Override
    public void clear() {
        cache.clear();
        System.out.println("RedisCacheManager: Redis cache cleared");
    }
}
```

```java org/example/patterns/di/conditional/InMemoryCacheManager.java
package org.example.patterns.di.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnMissingBean(CacheManager.class)
public class InMemoryCacheManager implements CacheManager {
    
    private final Map<String, Object> cache = new HashMap<>();
    
    public InMemoryCacheManager() {
        System.out.println("InMemoryCacheManager: Initialized (default in-memory cache)");
    }
    
    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
        System.out.println("InMemoryCacheManager: Cached " + key + " in memory");
    }
    
    @Override
    public Object get(String key) {
        System.out.println("InMemoryCacheManager: Retrieved " + key + " from memory");
        return cache.get(key);
    }
    
    @Override
    public void clear() {
        cache.clear();
        System.out.println("InMemoryCacheManager: Memory cache cleared");
    }
}
```

```java org/example/patterns/di/conditional/FeatureService.java
package org.example.patterns.di.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "feature.advanced.enabled", havingValue = "true")
public class FeatureService {
    
    public FeatureService() {
        System.out.println("FeatureService: Advanced features enabled");
    }
    
    public void performAdvancedOperation() {
        System.out.println("FeatureService: Executing advanced operation");
    }
}
```

```java org/example/patterns/di/conditional/ApplicationManager.java
package org.example.patterns.di.conditional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationManager {
    
    private final CacheManager cacheManager;
    
    @Autowired(required = false)
    private FeatureService featureService;
    
    public ApplicationManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        System.out.println("ApplicationManager: Using " + cacheManager.getClass().getSimpleName());
    }
    
    public void demonstrateConditionalBeans() {
        System.out.println("\n--- Conditional Bean Demonstration ---");
        
        // Cache is always available (either Redis or InMemory)
        cacheManager.put("user:1", "John Doe");
        Object cached = cacheManager.get("user:1");
        System.out.println("Cached value: " + cached);
        
        // Feature service is conditionally available
        if (featureService != null) {
            System.out.println("\nAdvanced features are enabled:");
            featureService.performAdvancedOperation();
        } else {
            System.out.println("\nAdvanced features are disabled");
        }
    }
}
```

```java org/example/patterns/di/conditional/ConditionalBeanDemo.java
package org.example.patterns.di.conditional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class ConditionalBeanDemo implements CommandLineRunner {
    
    private final ApplicationManager applicationManager;
    
    public ConditionalBeanDemo(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Conditional Bean Pattern Demo ===");
        System.out.println("Beans created based on conditions (properties, classes, etc.)\n");
        
        applicationManager.demonstrateConditionalBeans();
    }
}
```

---

## 11. Interface Injection Pattern (Aware Interfaces)

```java org/example/patterns/di/aware/ApplicationContextAwareBean.java
package org.example.patterns.di.aware;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextAwareBean implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    // Spring calls this method to inject ApplicationContext
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("ApplicationContextAwareBean: ApplicationContext injected via interface");
        this.applicationContext = applicationContext;
    }
    
    public void displayBeanInfo() {
        System.out.println("\n--- ApplicationContext Info ---");
        System.out.println("Application name: " + applicationContext.getApplicationName());
        System.out.println("Bean definition count: " + applicationContext.getBeanDefinitionCount());
        
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("First 5 beans:");
        for (int i = 0; i < Math.min(5, beanNames.length); i++) {
            System.out.println("  " + (i + 1) + ". " + beanNames[i]);
        }
    }
    
    public <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }
}
```

```java org/example/patterns/di/aware/BeanNameAwareBean.java
package org.example.patterns.di.aware;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.stereotype.Component;

@Component
public class BeanNameAwareBean implements BeanNameAware {
    
    private String beanName;
    
    @Override
    public void setBeanName(String name) {
        System.out.println("BeanNameAwareBean: Bean name injected via interface - " + name);
        this.beanName = name;
    }
    
    public void displayBeanName() {
        System.out.println("My bean name is: " + beanName);
    }
}
```

```java org/example/patterns/di/aware/EnvironmentAwareBean.java
package org.example.patterns.di.aware;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentAwareBean implements EnvironmentAware {
    
    private Environment environment;
    
    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("EnvironmentAwareBean: Environment injected via interface");
        this.environment = environment;
    }
    
    public void displayEnvironmentInfo() {
        System.out.println("\n--- Environment Info ---");
        System.out.println("Active profiles: " + String.join(", ", environment.getActiveProfiles()));
        System.out.println("Default profiles: " + String.join(", ", environment.getDefaultProfiles()));
        
        String appName = environment.getProperty("spring.application.name");
        System.out.println("Application name: " + appName);
    }
}
```

```java org/example/patterns/di/aware/InterfaceInjectionDemo.java
package org.example.patterns.di.aware;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(11)
public class InterfaceInjectionDemo implements CommandLineRunner {
    
    private final ApplicationContextAwareBean contextAware;
    private final BeanNameAwareBean nameAware;
    private final EnvironmentAwareBean environmentAware;
    
    public InterfaceInjectionDemo(ApplicationContextAwareBean contextAware,
                                 BeanNameAwareBean nameAware,
                                 EnvironmentAwareBean environmentAware) {
        this.contextAware = contextAware;
        this.nameAware = nameAware;
        this.environmentAware = environmentAware;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Interface Injection Pattern Demo ===");
        System.out.println("Spring injects dependencies via Aware interfaces\n");
        
        contextAware.displayBeanInfo();
        
        System.out.println();
        nameAware.displayBeanName();
        
        environmentAware.displayEnvironmentInfo();
    }
}
```

---

## 12. Replaced Method Injection Pattern

```java org/example/patterns/di/replaced/OriginalService.java
package org.example.patterns.di.replaced;

public class OriginalService {
    
    public String performOperation(String input) {
        System.out.println("OriginalService: performOperation called with: " + input);
        return "Original result: " + input.toUpperCase();
    }
    
    public void doSomething() {
        System.out.println("OriginalService: doSomething called");
    }
}
```

```java org/example/patterns/di/replaced/ReplacementMethodImplementation.java
package org.example.patterns.di.replaced;

import org.springframework.beans.factory.support.MethodReplacer;

import java.lang.reflect.Method;

public class ReplacementMethodImplementation implements MethodReplacer {
    
    @Override
    public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
        System.out.println("ReplacementMethod: Replaced implementation called");
        System.out.println("  Original method: " + method.getName());
        
        if (args != null && args.length > 0) {
            String input = (String) args[0];
            return "Replaced result: " + input.toLowerCase() + " [MODIFIED]";
        }
        
        return null;
    }
}
```

```java org/example/patterns/di/replaced/ReplacedMethodConfig.java
package org.example.patterns.di.replaced;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReplacedMethodConfig {
    
    @Bean
    public OriginalService originalService() {
        return new OriginalService();
    }
    
    @Bean
    public ReplacementMethodImplementation methodReplacer() {
        return new ReplacementMethodImplementation();
    }
}
```

```java org/example/patterns/di/replaced/ReplacedMethodDemo.java
package org.example.patterns.di.replaced;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(12)
public class ReplacedMethodDemo implements CommandLineRunner {
    
    private final OriginalService originalService;
    
    public ReplacedMethodDemo(OriginalService originalService) {
        this.originalService = originalService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Replaced Method Injection Pattern Demo ===");
        System.out.println("Note: Primarily used in XML configuration with <replaced-method>\n");
        
        String result = originalService.performOperation("Hello World");
        System.out.println("Result: " + result);
        
        originalService.doSomething();
        
        System.out.println("\nThis pattern is rarely used in modern Spring applications");
        System.out.println("Consider using AOP or strategy pattern instead");
    }
}
```

---

## Main Application

```java org/example/DependencyInjectionPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DependencyInjectionPatternsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DependencyInjectionPatternsApplication.class, args);
    }
}
```

---

## Application Properties

```properties src/main/resources/application.properties
# Application Configuration
spring.application.name=spring-di-patterns

# Active Profile (change to dev, test, or prod to see different DataSource)
spring.profiles.active=default

# Conditional Bean Properties
cache.type=inmemory
feature.advanced.enabled=false
feature.cache.enabled=true

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Disable banner
spring.main.banner-mode=off

# Allow circular references if needed
spring.main.allow-circular-references=false
```

---

## Comprehensive Demo Runner

```java org/example/ComprehensiveDIDemo.java
package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class ComprehensiveDIDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   Spring Dependency Injection Patterns Demonstration        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Running 12 Dependency Injection Patterns:");
        System.out.println("  1. Constructor Injection - Recommended for required dependencies");
        System.out.println("  2. Setter Injection - For optional dependencies");
        System.out.println("  3. Field Injection - Convenient but not recommended");
        System.out.println("  4. Method Injection - Multiple dependencies at once");
        System.out.println("  5. Lookup Method Injection - Singleton needs prototype");
        System.out.println("  6. Auto-wiring - Automatic dependency resolution");
        System.out.println("  7. Qualifier - Specify which bean to inject");
        System.out.println("  8. Primary - Default bean when multiple exist");
        System.out.println("  9. Profile-based - Environment-specific beans");
        System.out.println("  10. Conditional Bean - Conditional bean creation");
        System.out.println("  11. Interface Injection - Aware interfaces");
        System.out.println("  12. Replaced Method - Method replacement (legacy)");
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println();
    }
}
```

---

## Unit Tests

```java org/example/patterns/di/DIPatternTests.java
package org.example.patterns.di;

import org.example.patterns.di.constructor.UserService;
import org.example.patterns.di.qualifier.NotificationManager;
import org.example.patterns.di.primary.PaymentService;
import org.example.patterns.di.conditional.CacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DIPatternTests {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationManager notificationManager;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Test
    void testConstructorInjection() {
        assertNotNull(userService);
        assertDoesNotThrow(() -> userService.notifyUser(1L, "Test message"));
    }
    
    @Test
    void testQualifierPattern() {
        assertNotNull(notificationManager);
        assertDoesNotThrow(() -> notificationManager.sendViaEmail("Test", "test@example.com"));
    }
    
    @Test
    void testPrimaryBean() {
        assertNotNull(paymentService);
        assertDoesNotThrow(() -> paymentService.makePayment(100.0));
    }
    
    @Test
    void testConditionalBean() {
        assertNotNull(cacheManager);
        cacheManager.put("test", "value");
        assertEquals("value", cacheManager.get("test"));
    }
}
```

---

## README

```markdown README.md
# Spring Dependency Injection Patterns

Comprehensive demonstration of 12 Dependency Injection patterns in Spring Framework.

## Patterns Implemented

1. **Constructor Injection** - Immutable, required dependencies (Recommended)
2. **Setter Injection** - Optional, reconfigurable dependencies
3. **Field Injection** - Convenient but harder to test (Not recommended)
4. **Method Injection** - Multiple dependencies with initialization logic
5. **Lookup Method Injection** - Singleton bean needs prototype dependencies
6. **Auto-wiring** - Automatic dependency resolution by type
7. **Qualifier Pattern** - Specify which bean when multiple candidates exist
8. **Primary Bean** - Default bean selection
9. **Profile-based Injection** - Environment-specific beans (dev/test/prod)
10. **Conditional Bean** - Conditional bean creation based on properties/classes
11. **Interface Injection** - Aware interfaces (ApplicationContextAware, etc.)
12. **Replaced Method** - Runtime method replacement (legacy pattern)

## Running the Application

```bash
mvn spring-boot:run
```

## Switching Profiles

Edit `application.properties` and change:

```properties
spring.profiles.active=dev    # or test, prod
```

## Enabling Features

```properties
cache.type=redis              # Changes cache implementation
feature.advanced.enabled=true # Enables advanced features
```

## Best Practices

### ✅ Recommended
- **Constructor Injection** for required dependencies
- **@Qualifier** when multiple beans of same type exist
- **@Primary** for default implementations
- **@Profile** for environment-specific configurations

### ❌ Avoid
- **Field Injection** - harder to test, hides dependencies
- **Circular dependencies** - indicates design issues
- **Too many dependencies** - consider refactoring

## Injection Types Comparison

| Pattern | Immutable | Required | Testable | Use Case |
|---------|-----------|----------|----------|----------|
| Constructor | ✅ | ✅ | ✅ | Required dependencies |
| Setter | ❌ | ❌ | ✅ | Optional dependencies |
| Field | ❌ | ✅ | ⚠️ | Quick prototyping only |
| Method | ❌ | ✅ | ✅ | Multiple deps + init logic |

## Testing

```bash
mvn test
```

## Requirements

- Java 17+
- Maven 3.6+
- Spring Boot 3.2.0
```

This completes the comprehensive implementation of 12 Dependency Injection Patterns in Spring Framework, demonstrating various ways to inject and manage dependencies in modern Spring applications!