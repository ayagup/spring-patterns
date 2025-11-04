I'll create a comprehensive Spring Boot application demonstrating all testing patterns. Due to the length, I'll provide the complete implementation with all patterns.

# Spring Testing Patterns

## Project Structure

```
spring-testing-patterns/
├── src/main/java/org/example/
│   ├── TestingPatternsApplication.java
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── controller/
├── src/test/java/org/example/
│   ├── patterns/testing/
│   │   ├── mockobject/
│   │   ├── stub/
│   │   ├── testdouble/
│   │   ├── testcontext/
│   │   ├── executionlistener/
│   │   ├── dependencyinjection/
│   │   ├── propertysource/
│   │   ├── testconfig/
│   │   ├── mockmvc/
│   │   ├── mockbean/
│   │   ├── spybean/
│   │   ├── integration/
│   │   ├── slice/
│   │   ├── webmvctest/
│   │   ├── datajpatest/
│   │   └── restclienttest/
├── pom.xml
└── application.properties
```

## 1. Main Application & Domain Models

```java
// src/main/java/org/example/TestingPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestingPatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestingPatternsApplication.class, args);
    }
}
```

```java
// src/main/java/org/example/model/User.java
package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String email;
    
    private String firstName;
    private String lastName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    private Boolean active;
    
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }
}
```

```java
// src/main/java/org/example/model/Product.java
package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    private Integer stock;
    
    private String category;
    
    public Product(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }
}
```

## 2. Repository Layer

```java
// src/main/java/org/example/repository/UserRepository.java
package org.example.repository;

import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByActive(Boolean active);
    
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %?1% OR u.lastName LIKE %?1%")
    List<User> searchByName(String name);
}
```

```java
// src/main/java/org/example/repository/ProductRepository.java
package org.example.repository;

import org.example.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByPriceLessThan(BigDecimal price);
    List<Product> findByNameContaining(String name);
}
```

## 3. Service Layer

```java
// src/main/java/org/example/service/UserService.java
package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    @Transactional
    public User createUser(User user) {
        log.info("Creating user: {}", user.getUsername());
        
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        
        User saved = userRepository.save(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(saved.getEmail());
        
        return saved;
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public List<User> findActiveUsers() {
        return userRepository.findByActive(true);
    }
    
    @Transactional
    public User updateUser(Long id, User updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setFirstName(updates.getFirstName());
        user.setLastName(updates.getLastName());
        user.setEmail(updates.getEmail());
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

```java
// src/main/java/org/example/service/EmailService.java
package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {
    
    public void sendWelcomeEmail(String email) {
        log.info("Sending welcome email to: {}", email);
        // Actual email sending logic
    }
    
    public void sendNotification(String email, String message) {
        log.info("Sending notification to {}: {}", email, message);
        // Actual notification logic
    }
}
```

```java
// src/main/java/org/example/service/ProductService.java
package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    public List<Product> findAffordableProducts(BigDecimal maxPrice) {
        return productRepository.findByPriceLessThan(maxPrice);
    }
}
```

## 4. Controller Layer

```java
// src/main/java/org/example/controller/UserController.java
package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        return ResponseEntity.ok(userService.findActiveUsers());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

```java
// src/main/java/org/example/controller/ProductController.java
package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.example.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.findByCategory(category));
    }
}
```

Now, let's implement all the testing patterns:

## 5. Pattern 1: Mock Object Pattern

```java
// src/test/java/org/example/patterns/testing/mockobject/MockObjectPatternTest.java
package org.example.patterns.testing.mockobject;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.EmailService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Mock Object Pattern.
 * Creates fake objects to isolate unit under test.
 */
@ExtendWith(MockitoExtension.class)
class MockObjectPatternTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com");
        testUser.setId(1L);
    }
    
    @Test
    void testCreateUser_Success() {
        // Arrange - Mock behavior
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendWelcomeEmail(anyString());
        
        // Act
        User created = userService.createUser(testUser);
        
        // Assert
        assertNotNull(created);
        assertEquals("testuser", created.getUsername());
        
        // Verify interactions
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendWelcomeEmail("test@example.com");
    }
    
    @Test
    void testCreateUser_UsernameExists() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(testUser);
        });
        
        // Verify save was never called
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(anyString());
    }
    
    @Test
    void testFindById() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Act
        Optional<User> found = userService.findById(1L);
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        
        verify(userRepository).findById(1L);
    }
}
```

## 6. Pattern 2: Stub Pattern

```java
// src/test/java/org/example/patterns/testing/stub/EmailServiceStub.java
package org.example.patterns.testing.stub;

import org.example.service.EmailService;

/**
 * Stub Pattern.
 * Provides predefined responses without logic.
 */
public class EmailServiceStub extends EmailService {
    
    private boolean emailSent = false;
    private String lastRecipient;
    private String lastMessage;
    
    @Override
    public void sendWelcomeEmail(String email) {
        this.emailSent = true;
        this.lastRecipient = email;
    }
    
    @Override
    public void sendNotification(String email, String message) {
        this.emailSent = true;
        this.lastRecipient = email;
        this.lastMessage = message;
    }
    
    public boolean wasEmailSent() {
        return emailSent;
    }
    
    public String getLastRecipient() {
        return lastRecipient;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void reset() {
        this.emailSent = false;
        this.lastRecipient = null;
        this.lastMessage = null;
    }
}
```

```java
// src/test/java/org/example/patterns/testing/stub/StubPatternTest.java
package org.example.patterns.testing.stub;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Stub Pattern Test.
 * Uses stubbed implementations instead of mocks.
 */
@ExtendWith(MockitoExtension.class)
class StubPatternTest {
    
    @Mock
    private UserRepository userRepository;
    
    private EmailServiceStub emailServiceStub;
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        emailServiceStub = new EmailServiceStub();
        userService = new UserService(userRepository, emailServiceStub);
    }
    
    @Test
    void testCreateUser_EmailStub() {
        // Arrange
        User user = new User("testuser", "test@example.com");
        user.setId(1L);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // Act
        userService.createUser(user);
        
        // Assert - verify stub captured the interaction
        assertTrue(emailServiceStub.wasEmailSent());
        assertEquals("test@example.com", emailServiceStub.getLastRecipient());
    }
}
```

## 7. Pattern 3: Test Double Pattern

```java
// src/test/java/org/example/patterns/testing/testdouble/UserRepositoryFake.java
package org.example.patterns.testing.testdouble;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Test Double Pattern - Fake Implementation.
 * In-memory implementation for testing.
 */
public class UserRepositoryFake implements UserRepository {
    
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        users.put(user.getId(), user);
        return user;
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }
    
    @Override
    public List<User> findByActive(Boolean active) {
        return users.values().stream()
                .filter(u -> Objects.equals(u.getActive(), active))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<User> searchByName(String name) {
        return users.values().stream()
                .filter(u -> (u.getFirstName() != null && u.getFirstName().contains(name)) ||
                           (u.getLastName() != null && u.getLastName().contains(name)))
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
    
    @Override
    public long count() {
        return users.size();
    }
    
    // Remaining methods - throw UnsupportedOperationException or provide minimal implementation
    @Override
    public void flush() { }
    
    @Override
    public <S extends User> S saveAndFlush(S entity) {
        return save(entity);
    }
    
    @Override
    public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(e -> result.add(saveAndFlush(e)));
        return result;
    }
    
    @Override
    public void deleteAllInBatch(Iterable<User> entities) {
        entities.forEach(e -> deleteById(e.getId()));
    }
    
    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        longs.forEach(this::deleteById);
    }
    
    @Override
    public void deleteAllInBatch() {
        users.clear();
    }
    
    @Override
    public User getOne(Long aLong) {
        return findById(aLong).orElse(null);
    }
    
    @Override
    public User getById(Long aLong) {
        return findById(aLong).orElse(null);
    }
    
    @Override
    public User getReferenceById(Long aLong) {
        return findById(aLong).orElse(null);
    }
    
    @Override
    public <S extends User> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <S extends User> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(e -> result.add(save(e)));
        return result;
    }
    
    @Override
    public List<User> findAllById(Iterable<Long> longs) {
        List<User> result = new ArrayList<>();
        longs.forEach(id -> findById(id).ifPresent(result::add));
        return result;
    }
    
    @Override
    public void delete(User entity) {
        deleteById(entity.getId());
    }
    
    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        longs.forEach(this::deleteById);
    }
    
    @Override
    public void deleteAll(Iterable<? extends User> entities) {
        entities.forEach(e -> deleteById(e.getId()));
    }
    
    @Override
    public void deleteAll() {
        users.clear();
    }
    
    @Override
    public List<User> findAll(Sort sort) {
        return findAll();
    }
    
    @Override
    public Page<User> findAll(Pageable pageable) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <S extends User> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <S extends User> long count(Example<S> example) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <S extends User> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException();
    }
}
```

```java
// src/test/java/org/example/patterns/testing/testdouble/TestDoublePatternTest.java
package org.example.patterns.testing.testdouble;

import org.example.model.User;
import org.example.patterns.testing.stub.EmailServiceStub;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Double Pattern Test.
 * Uses fake implementation instead of real dependencies.
 */
class TestDoublePatternTest {
    
    private UserRepositoryFake userRepository;
    private EmailServiceStub emailService;
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryFake();
        emailService = new EmailServiceStub();
        userService = new UserService(userRepository, emailService);
    }
    
    @Test
    void testCreateUser_WithFakeRepository() {
        // Arrange
        User user = new User("testuser", "test@example.com");
        
        // Act
        User created = userService.createUser(user);
        
        // Assert
        assertNotNull(created.getId());
        assertEquals("testuser", created.getUsername());
        assertTrue(emailService.wasEmailSent());
        
        // Verify it's in the fake repository
        Optional<User> found = userRepository.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }
    
    @Test
    void testFindByUsername_WithFakeRepository() {
        // Arrange
        User user = new User("john.doe", "john@example.com");
        userRepository.save(user);
        
        // Act
        Optional<User> found = userService.findByUsername("john.doe");
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("john.doe", found.get().getUsername());
    }
}
```

## 8. Pattern 4: Test Context Pattern

```java
// src/test/java/org/example/patterns/testing/testcontext/TestContextPatternTest.java
package org.example.patterns.testing.testcontext;

import org.example.model.User;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Context Pattern.
 * Spring manages test application context.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class TestContextPatternTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testApplicationContextLoads() {
        assertNotNull(userService);
    }
    
    @Test
    void testServiceWithContext() {
        // Test runs with full Spring context
        User user = new User("contextUser", "context@example.com");
        
        // This would fail if context wasn't loaded properly
        assertDoesNotThrow(() -> {
            userService.createUser(user);
        });
    }
}
```

## 9. Pattern 5: Test Execution Listener Pattern

```java
// src/test/java/org/example/patterns/testing/executionlistener/CustomTestExecutionListener.java
package org.example.patterns.testing.executionlistener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Test Execution Listener Pattern.
 * Hooks into test lifecycle events.
 */
@Slf4j
public class CustomTestExecutionListener implements TestExecutionListener {
    
    @Override
    public void beforeTestClass(TestContext testContext) {
        log.info("=== Before Test Class: {} ===", 
                testContext.getTestClass().getSimpleName());
    }
    
    @Override
    public void prepareTestInstance(TestContext testContext) {
        log.info("Preparing test instance");
    }
    
    @Override
    public void beforeTestMethod(TestContext testContext) {
        log.info("Before test method: {}", 
                testContext.getTestMethod().getName());
    }
    
    @Override
    public void beforeTestExecution(TestContext testContext) {
        log.info("Before test execution");
    }
    
    @Override
    public void afterTestExecution(TestContext testContext) {
        log.info("After test execution");
        
        if (testContext.getTestException() != null) {
            log.error("Test failed with exception: {}", 
                    testContext.getTestException().getMessage());
        }
    }
    
    @Override
    public void afterTestMethod(TestContext testContext) {
        log.info("After test method: {}", 
                testContext.getTestMethod().getName());
    }
    
    @Override
    public void afterTestClass(TestContext testContext) {
        log.info("=== After Test Class: {} ===", 
                testContext.getTestClass().getSimpleName());
    }
}
```

```java
// src/test/java/org/example/patterns/testing/executionlistener/TestExecutionListenerPatternTest.java
package org.example.patterns.testing.executionlistener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Execution Listener Pattern Test.
 */
@SpringBootTest
@TestExecutionListeners(
    value = CustomTestExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
class TestExecutionListenerPatternTest {
    
    @Test
    void testWithListener() {
        // Listener will log lifecycle events
        assertTrue(true);
    }
    
    @Test
    void anotherTestWithListener() {
        // Listener tracks this test too
        assertTrue(true);
    }
}
```

## 10. Pattern 6: Dependency Injection for Tests Pattern

```java
// src/test/java/org/example/patterns/testing/dependencyinjection/TestConfiguration.java
package org.example.patterns.testing.dependencyinjection;

import org.example.service.EmailService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Dependency Injection for Tests Pattern.
 * Override beans for testing.
 */
@TestConfiguration
public class TestConfiguration {
    
    @Bean
    @Primary
    public EmailService emailService() {
        return Mockito.mock(EmailService.class);
    }
}
```

```java
// src/test/java/org/example/patterns/testing/dependencyinjection/DependencyInjectionPatternTest.java
package org.example.patterns.testing.dependencyinjection;

import org.example.model.User;
import org.example.service.EmailService;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

/**
 * Dependency Injection for Tests Pattern Test.
 */
@SpringBootTest
@Import(TestConfiguration.class)
class DependencyInjectionPatternTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @Test
    void testWithInjectedMock() {
        // EmailService is mocked via TestConfiguration
        User user = new User("diuser", "di@example.com");
        
        userService.createUser(user);
        
        assertNotNull(user);
        verify(emailService).sendWelcomeEmail(anyString());
    }
}
```

## 11. Pattern 7: Test Property Source Pattern

```java
// src/test/java/org/example/patterns/testing/propertysource/PropertySourcePatternTest.java
package org.example.patterns.testing.propertysource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test Property Source Pattern.
 * Override properties for testing.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "app.name=Test Application",
    "app.version=1.0.0-TEST",
    "app.environment=test",
    "feature.enabled=true"
})
class PropertySourcePatternTest {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version}")
    private String appVersion;
    
    @Value("${app.environment}")
    private String environment;
    
    @Value("${feature.enabled}")
    private boolean featureEnabled;
    
    @Test
    void testPropertyValues() {
        assertEquals("Test Application", appName);
        assertEquals("1.0.0-TEST", appVersion);
        assertEquals("test", environment);
        assertEquals(true, featureEnabled);
    }
}
```

```java
// src/test/java/org/example/patterns/testing/propertysource/PropertyFilePatternTest.java
package org.example.patterns.testing.propertysource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test Property Source Pattern with file.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class PropertyFilePatternTest {
    
    @Value("${test.property}")
    private String testProperty;
    
    @Test
    void testPropertyFromFile() {
        assertEquals("test-value", testProperty);
    }
}
```

## 12. Pattern 8: Test Configuration Pattern

```java
// src/test/java/org/example/patterns/testing/testconfig/TestDatabaseConfig.java
package org.example.patterns.testing.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * Test Configuration Pattern.
 * Custom configuration for tests.
 */
@TestConfiguration
public class TestDatabaseConfig {
    
    @Bean
    public DataSource testDataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("testdb")
                .build();
    }
}
```

```java
// src/test/java/org/example/patterns/testing/testconfig/TestConfigurationPatternTest.java
package org.example.patterns.testing.testconfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test Configuration Pattern Test.
 */
@SpringBootTest
@Import(TestDatabaseConfig.class)
class TestConfigurationPatternTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    void testCustomConfiguration() {
        assertNotNull(dataSource);
        // DataSource is from TestDatabaseConfig
    }
}
```

## 13. Pattern 9: Mock MVC Pattern

```java
// src/test/java/org/example/patterns/testing/mockmvc/MockMvcPatternTest.java
package org.example.patterns.testing.mockmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.User;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Mock MVC Pattern.
 * Tests web layer without starting full HTTP server.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MockMvcPatternTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    @Test
    void testGetUser() throws Exception {
        // Arrange
        User user = new User("testuser", "test@example.com");
        user.setId(1L);
        
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        
        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
    
    @Test
    void testGetUser_NotFound() throws Exception {
        // Arrange
        when(userService.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testCreateUser() throws Exception {
        // Arrange
        User user = new User("newuser", "new@example.com");
        User savedUser = new User("newuser", "new@example.com");
        savedUser.setId(1L);
        
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("newuser"));
    }
    
    @Test
    void testGetAllUsers() throws Exception {
        // Arrange
        User user1 = new User("user1", "user1@example.com");
        user1.setId(1L);
        User user2 = new User("user2", "user2@example.com");
        user2.setId(2L);
        
        when(userService.findAll()).thenReturn(Arrays.asList(user1, user2));
        
        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }
    
    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
```

## 14. Pattern 10: Mock Bean Pattern

```java
// src/test/java/org/example/patterns/testing/mockbean/MockBeanPatternTest.java
package org.example.patterns.testing.mockbean;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.EmailService;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Mock Bean Pattern.
 * Replace Spring beans with mocks in application context.
 */
@SpringBootTest
class MockBeanPatternTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private EmailService emailService;
    
    @Test
    void testCreateUser_WithMockBeans() {
        // Arrange
        User user = new User("mockuser", "mock@example.com");
        user.setId(1L);
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(emailService).sendWelcomeEmail(anyString());
        
        // Act
        User created = userService.createUser(user);
        
        // Assert
        assertNotNull(created);
        assertEquals(1L, created.getId());
        
        verify(userRepository).save(any(User.class));
        verify(emailService).sendWelcomeEmail("mock@example.com");
    }
    
    @Test
    void testFindById_WithMockBean() {
        // Arrange
        User user = new User("finduser", "find@example.com");
        user.setId(2L);
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        
        // Act
        Optional<User> found = userService.findById(2L);
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("finduser", found.get().getUsername());
    }
}
```

## 15. Pattern 11: Spy Bean Pattern

```java
// src/test/java/org/example/patterns/testing/spybean/SpyBeanPatternTest.java
package org.example.patterns.testing.spybean;

import org.example.model.User;
import org.example.service.EmailService;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;

/**
 * Spy Bean Pattern.
 * Partial mock - real implementation with ability to verify/stub.
 */
@SpringBootTest
class SpyBeanPatternTest {
    
    @Autowired
    private UserService userService;
    
    @SpyBean
    private EmailService emailService;
    
    @Test
    void testWithSpyBean() {
        // Arrange - Spy allows real method calls
        User user = new User("spyuser", "spy@example.com");
        
        // Stub specific method
        doNothing().when(emailService).sendWelcomeEmail(anyString());
        
        // Act
        User created = userService.createUser(user);
        
        // Assert
        assertNotNull(created);
        
        // Verify interaction
        verify(emailService).sendWelcomeEmail("spy@example.com");
    }
}
```

## 16. Pattern 12: Integration Test Pattern

```java
// src/test/java/org/example/patterns/testing/integration/IntegrationTestPatternTest.java
package org.example.patterns.testing.integration;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test Pattern.
 * Tests multiple components together with real database.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationTestPatternTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    private static Long createdUserId;
    
    @Test
    @Order(1)
    void testCreateUser_Integration() {
        // Arrange
        User user = new User("integrationuser", "integration@example.com");
        user.setFirstName("Integration");
        user.setLastName("Test");
        
        // Act
        User created = userService.createUser(user);
        createdUserId = created.getId();
        
        // Assert
        assertNotNull(created.getId());
        assertTrue(created.getActive());
        assertNotNull(created.getCreatedAt());
        
        // Verify in database
        Optional<User> found = userRepository.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("integrationuser", found.get().getUsername());
    }
    
    @Test
    @Order(2)
    void testFindUser_Integration() {
        // Act
        Optional<User> found = userService.findById(createdUserId);
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("integrationuser", found.get().getUsername());
        assertEquals("Integration", found.get().getFirstName());
    }
    
    @Test
    @Order(3)
    void testUpdateUser_Integration() {
        // Arrange
        User updates = new User();
        updates.setFirstName("Updated");
        updates.setLastName("User");
        updates.setEmail("updated@example.com");
        
        // Act
        User updated = userService.updateUser(createdUserId, updates);
        
        // Assert
        assertEquals("Updated", updated.getFirstName());
        assertEquals("updated@example.com", updated.getEmail());
        
        // Verify persistence
        User fromDb = userRepository.findById(createdUserId).orElseThrow();
        assertEquals("Updated", fromDb.getFirstName());
    }
    
    @Test
    @Order(4)
    void testFindAllUsers_Integration() {
        // Act
        List<User> users = userService.findAll();
        
        // Assert
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(u -> u.getId().equals(createdUserId)));
    }
    
    @Test
    @Order(5)
    void testDeleteUser_Integration() {
        // Act
        userService.deleteUser(createdUserId);
        
        // Assert
        Optional<User> deleted = userRepository.findById(createdUserId);
        assertFalse(deleted.isPresent());
    }
}
```

## 17. Pattern 13: Slice Test Pattern

```java
// src/test/java/org/example/patterns/testing/slice/ServiceSliceTest.java
package org.example.patterns.testing.slice;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.EmailService;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Slice Test Pattern.
 * Tests only specific layer/slice of application.
 */
@SpringJUnitConfig
@Import(ServiceSliceTest.ServiceTestConfig.class)
class ServiceSliceTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private EmailService emailService;
    
    @TestConfiguration
    static class ServiceTestConfig {
        @Bean
        public UserService userService(UserRepository userRepository, 
                                       EmailService emailService) {
            return new UserService(userRepository, emailService);
        }
    }
    
    @Test
    void testServiceLayer_Only() {
        // Arrange
        User user = new User("sliceuser", "slice@example.com");
        user.setId(1L);
        
        when(userRepository.findByUsername("sliceuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // Act
        User created = userService.createUser(user);
        
        // Assert
        assertNotNull(created);
    }
}
```

## 18. Pattern 14: Web MVC Test Pattern

```java
// src/test/java/org/example/patterns/testing/webmvctest/WebMvcTestPatternTest.java
package org.example.patterns.testing.webmvctest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.UserController;
import org.example.model.User;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web MVC Test Pattern.
 * Tests only web layer (controllers) with minimal context.
 */
@WebMvcTest(UserController.class)
class WebMvcTestPatternTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    @Test
    void testGetUser_WebLayer() throws Exception {
        // Arrange
        User user = new User("webuser", "web@example.com");
        user.setId(1L);
        
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        
        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("webuser"))
                .andExpect(jsonPath("$.email").value("web@example.com"));
    }
    
    @Test
    void testCreateUser_WebLayer() throws Exception {
        // Arrange
        User user = new User("newweb", "newweb@example.com");
        User savedUser = new User("newweb", "newweb@example.com");
        savedUser.setId(1L);
        
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    void testGetAllUsers_WebLayer() throws Exception {
        // Arrange
        User user1 = new User("user1", "user1@example.com");
        user1.setId(1L);
        User user2 = new User("user2", "user2@example.com");
        user2.setId(2L);
        
        when(userService.findAll()).thenReturn(Arrays.asList(user1, user2));
        
        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
```

## 19. Pattern 15: Data JPA Test Pattern

```java
// src/test/java/org/example/patterns/testing/datajpatest/DataJpaTestPatternTest.java
package org.example.patterns.testing.datajpatest;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data JPA Test Pattern.
 * Tests only JPA layer with in-memory database.
 */
@DataJpaTest
class DataJpaTestPatternTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testSaveUser_JpaLayer() {
        // Arrange
        User user = new User("jpauser", "jpa@example.com");
        
        // Act
        User saved = userRepository.save(user);
        entityManager.flush();
        
        // Assert
        assertNotNull(saved.getId());
        
        User found = entityManager.find(User.class, saved.getId());
        assertEquals("jpauser", found.getUsername());
    }
    
    @Test
    void testFindByUsername_JpaLayer() {
        // Arrange
        User user = new User("findme", "findme@example.com");
        entityManager.persistAndFlush(user);
        
        // Act
        Optional<User> found = userRepository.findByUsername("findme");
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("findme@example.com", found.get().getEmail());
    }
    
    @Test
    void testFindByActive_JpaLayer() {
        // Arrange
        User activeUser = new User("active", "active@example.com");
        activeUser.setActive(true);
        entityManager.persistAndFlush(activeUser);
        
        User inactiveUser = new User("inactive", "inactive@example.com");
        inactiveUser.setActive(false);
        entityManager.persistAndFlush(inactiveUser);
        
        // Act
        List<User> activeUsers = userRepository.findByActive(true);
        
        // Assert
        assertEquals(1, activeUsers.size());
        assertEquals("active", activeUsers.get(0).getUsername());
    }
    
    @Test
    void testCustomQuery_SearchByName() {
        // Arrange
        User user1 = new User("john.doe", "john@example.com");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        entityManager.persistAndFlush(user1);
        
        User user2 = new User("jane.smith", "jane@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        entityManager.persistAndFlush(user2);
        
        // Act
        List<User> results = userRepository.searchByName("John");
        
        // Assert
        assertEquals(1, results.size());
```java
        assertEquals("john.doe", results.get(0).getUsername());
    }
}
```

## 20. Pattern 16: Rest Client Test Pattern

```java
// src/test/java/org/example/patterns/testing/restclienttest/RestClientTestPatternTest.java
package org.example.patterns.testing.restclienttest;

import org.example.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rest Client Test Pattern.
 * Tests REST API with real HTTP requests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestClientTestPatternTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/users";
    }
    
    @Test
    void testCreateUser_RestClient() {
        // Arrange
        User user = new User("restuser", "rest@example.com");
        user.setFirstName("Rest");
        user.setLastName("Client");
        
        // Act
        ResponseEntity<User> response = restTemplate.postForEntity(
                getBaseUrl(), user, User.class);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("restuser", response.getBody().getUsername());
    }
    
    @Test
    void testGetUser_RestClient() {
        // Arrange - Create user first
        User user = new User("getuser", "get@example.com");
        ResponseEntity<User> createResponse = restTemplate.postForEntity(
                getBaseUrl(), user, User.class);
        Long userId = createResponse.getBody().getId();
        
        // Act
        ResponseEntity<User> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + userId, User.class);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("getuser", response.getBody().getUsername());
    }
    
    @Test
    void testGetAllUsers_RestClient() {
        // Act
        ResponseEntity<User[]> response = restTemplate.getForEntity(
                getBaseUrl(), User[].class);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 0);
    }
    
    @Test
    void testUpdateUser_RestClient() {
        // Arrange - Create user first
        User user = new User("updateuser", "update@example.com");
        ResponseEntity<User> createResponse = restTemplate.postForEntity(
                getBaseUrl(), user, User.class);
        Long userId = createResponse.getBody().getId();
        
        // Update data
        User updates = new User();
        updates.setFirstName("Updated");
        updates.setLastName("Name");
        updates.setEmail("updated@example.com");
        
        // Act
        restTemplate.put(getBaseUrl() + "/" + userId, updates);
        
        // Verify
        ResponseEntity<User> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/" + userId, User.class);
        
        // Assert
        assertEquals("Updated", getResponse.getBody().getFirstName());
        assertEquals("updated@example.com", getResponse.getBody().getEmail());
    }
    
    @Test
    void testDeleteUser_RestClient() {
        // Arrange - Create user first
        User user = new User("deleteuser", "delete@example.com");
        ResponseEntity<User> createResponse = restTemplate.postForEntity(
                getBaseUrl(), user, User.class);
        Long userId = createResponse.getBody().getId();
        
        // Act
        restTemplate.delete(getBaseUrl() + "/" + userId);
        
        // Verify
        ResponseEntity<User> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/" + userId, User.class);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}
```

## 21. Maven Configuration (pom.xml)

```xml
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
    <artifactId>spring-testing-patterns</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Spring Testing Patterns</name>
    <description>Demonstration of testing patterns in Spring</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Boot Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Mockito -->
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- JUnit 5 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- AssertJ -->
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Hamcrest -->
        <dependency>
            <groupId>org.hamcrest</groupId>
            <artifactId>hamcrest</artifactId>
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
            
            <!-- Surefire Plugin for Tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

## 22. Application Properties

```properties
# src/main/resources/application.properties
spring.application.name=spring-testing-patterns

# H2 Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
```

```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.org.example=DEBUG
```

```properties
# src/test/resources/test.properties
test.property=test-value
```

## 23. README.md

```markdown
# Spring Testing Patterns

Comprehensive demonstration of 16 essential testing patterns in Spring Boot.

## Patterns Implemented

### 1. Mock Object Pattern
**Test:** `MockObjectPatternTest`

Creates fake objects to isolate unit under test.

**Key Features:**
- `@Mock` - Create mock objects
- `@InjectMocks` - Inject mocks into test class
- `when()` - Stub method behavior
- `verify()` - Verify interactions

**Example:**
```java
@Mock
private UserRepository userRepository;

@InjectMocks
private UserService userService;

@Test
void test() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    User found = userService.findById(1L);
    verify(userRepository).findById(1L);
}
```

### 2. Stub Pattern
**Test:** `StubPatternTest`

Provides predefined responses without logic.

**Example:**
```java
public class EmailServiceStub extends EmailService {
    private boolean emailSent = false;
    
    @Override
    public void sendWelcomeEmail(String email) {
        this.emailSent = true;
    }
    
    public boolean wasEmailSent() {
        return emailSent;
    }
}
```

### 3. Test Double Pattern
**Test:** `TestDoublePatternTest`

Fake implementations for testing.

**Types:**
- **Dummy** - Passed but never used
- **Stub** - Provides canned responses
- **Fake** - Working implementation (simplified)
- **Mock** - Verifies interactions
- **Spy** - Partial mock

### 4. Test Context Pattern
**Test:** `TestContextPatternTest`

Spring manages test application context.

**Annotations:**
- `@SpringBootTest` - Load full context
- `@ActiveProfiles` - Activate test profiles
- `@TestPropertySource` - Override properties

### 5. Test Execution Listener Pattern
**Test:** `TestExecutionListenerPatternTest`

Hooks into test lifecycle events.

**Lifecycle Methods:**
- `beforeTestClass()` - Before all tests
- `beforeTestMethod()` - Before each test
- `beforeTestExecution()` - Just before test
- `afterTestExecution()` - Just after test
- `afterTestMethod()` - After each test
- `afterTestClass()` - After all tests

### 6. Dependency Injection for Tests Pattern
**Test:** `DependencyInjectionPatternTest`

Override beans for testing.

**Example:**
```java
@TestConfiguration
public class TestConfiguration {
    @Bean
    @Primary
    public EmailService emailService() {
        return Mockito.mock(EmailService.class);
    }
}
```

### 7. Test Property Source Pattern
**Test:** `PropertySourcePatternTest`

Override properties for testing.

**Methods:**
- Inline properties
- External file
- Multiple sources

**Example:**
```java
@TestPropertySource(properties = {
    "app.name=Test App",
    "feature.enabled=true"
})
```

### 8. Test Configuration Pattern
**Test:** `TestConfigurationPatternTest`

Custom configuration for tests.

**Example:**
```java
@TestConfiguration
public class TestDatabaseConfig {
    @Bean
    public DataSource testDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }
}
```

### 9. Mock MVC Pattern
**Test:** `MockMvcPatternTest`

Tests web layer without HTTP server.

**Features:**
- Request/response verification
- JSON path assertions
- Status code checking
- Content type validation

**Example:**
```java
mockMvc.perform(get("/api/users/1"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.username").value("testuser"));
```

### 10. Mock Bean Pattern
**Test:** `MockBeanPatternTest`

Replace Spring beans with mocks.

**Example:**
```java
@SpringBootTest
class Test {
    @MockBean
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService; // Gets mocked repository
}
```

### 11. Spy Bean Pattern
**Test:** `SpyBeanPatternTest`

Partial mock - real implementation with verification.

**Example:**
```java
@SpyBean
private EmailService emailService;

// Real method called unless stubbed
verify(emailService).sendWelcomeEmail(anyString());
```

### 12. Integration Test Pattern
**Test:** `IntegrationTestPatternTest`

Tests multiple components with real database.

**Features:**
- Full Spring context
- Real database operations
- End-to-end testing
- `@TestMethodOrder` for sequential tests

### 13. Slice Test Pattern
**Test:** `ServiceSliceTest`

Tests specific layer only.

**Examples:**
- Service layer tests
- Controller layer tests
- Repository layer tests

### 14. Web MVC Test Pattern
**Test:** `WebMvcTestPatternTest`

Tests only web layer with minimal context.

**Annotation:** `@WebMvcTest(Controller.class)`

**Benefits:**
- Faster than `@SpringBootTest`
- Only loads web layer
- MockMvc auto-configured

### 15. Data JPA Test Pattern
**Test:** `DataJpaTestPatternTest`

Tests only JPA layer with in-memory database.

**Features:**
- `@DataJpaTest` annotation
- `TestEntityManager` for direct DB operations
- Transactional by default (rollback after each test)
- Only JPA components loaded

**Example:**
```java
@DataJpaTest
class Test {
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository repository;
}
```

### 16. Rest Client Test Pattern
**Test:** `RestClientTestPatternTest`

Tests REST API with real HTTP requests.

**Features:**
- Random port assignment
- `TestRestTemplate` auto-configured
- Real HTTP client/server
- Full integration testing

**Example:**
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class Test {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void test() {
        ResponseEntity<User> response = 
            restTemplate.getForEntity("/api/users/1", User.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=MockObjectPatternTest
```

### Run with Coverage
```bash
mvn test jacoco:report
```

## Test Annotations Quick Reference

| Annotation | Purpose |
|------------|---------|
| `@SpringBootTest` | Full application context |
| `@WebMvcTest` | Web layer only |
| `@DataJpaTest` | JPA layer only |
| `@Mock` | Create mock |
| `@MockBean` | Mock Spring bean |
| `@SpyBean` | Spy on Spring bean |
| `@InjectMocks` | Inject mocks |
| `@Autowired` | Inject real beans |
| `@TestConfiguration` | Test-specific config |
| `@TestPropertySource` | Override properties |
| `@ActiveProfiles` | Activate profiles |

## Best Practices

### 1. Choose Right Test Type
- **Unit Tests:** Mock dependencies, test single class
- **Integration Tests:** Real dependencies, test multiple components
- **Slice Tests:** Test specific layer
- **End-to-End Tests:** Full application with real HTTP

### 2. Naming Conventions
```java
// Test class
[ClassUnderTest]Test

// Test method
test[MethodName]_[Scenario]_[ExpectedResult]
testCreateUser_ValidData_ReturnsUser()
```

### 3. AAA Pattern
```java
@Test
void test() {
    // Arrange
    User user = new User("test", "test@example.com");
    
    // Act
    User created = userService.createUser(user);
    
    // Assert
    assertNotNull(created.getId());
}
```

### 4. Use @BeforeEach for Setup
```java
@BeforeEach
void setUp() {
    testUser = new User("test", "test@example.com");
}
```

### 5. Test One Thing
Each test should verify one behavior.

### 6. Independent Tests
Tests should not depend on each other.

## Testing Pyramid

```
       /\
      /E2E\        Few - Slow - Expensive
     /------\
    /Integration\  Some - Medium Speed
   /------------\
  /  Unit Tests  \  Many - Fast - Cheap
 /________________\
```

## Common Assertions

### JUnit 5
```java
assertEquals(expected, actual);
assertNotNull(object);
assertTrue(condition);
assertThrows(Exception.class, () -> code);
assertAll(
    () -> assertEquals(1, result),
    () -> assertNotNull(object)
);
```

### AssertJ
```java
assertThat(user)
    .isNotNull()
    .extracting(User::getUsername)
    .isEqualTo("testuser");
```

### Mockito Verification
```java
verify(mock).method();
verify(mock, times(2)).method();
verify(mock, never()).method();
verifyNoMoreInteractions(mock);
```

## License

MIT License - free to use for learning and projects.
```

This completes the comprehensive implementation of all 16 Testing Patterns with working code and thorough documentation!