package com.example.cassandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cassandra Repository Pattern
 * 
 * Demonstrates the use of Spring Data Cassandra Repository for simplified
 * data access with Apache Cassandra.
 * 
 * Key concepts:
 * - CassandraRepository interface
 * - Query method derivation
 * - Custom @Query annotations with CQL
 * - Primary key and partition key
 * - Table mapping
 * 
 * Use cases:
 * - Standard CRUD operations
 * - Derived query methods
 * - Custom CQL queries
 * - Simplified data access layer
 * - Type-safe queries
 */
@SpringBootApplication
public class CassandraRepositoryPattern {

    public static void main(String[] args) {
        SpringApplication.run(CassandraRepositoryPattern.class, args);
    }
}

/**
 * User entity with Cassandra annotations
 */
@Table("users")
record User(
    @PrimaryKey UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    Integer age,
    String country,
    LocalDateTime createdAt
) {
    public User {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

/**
 * Order entity for time-series data
 */
@Table("orders")
record Order(
    @PrimaryKey UUID id,
    UUID userId,
    String productName,
    Integer quantity,
    Double amount,
    String status,
    LocalDateTime orderDate
) {
    public Order {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}

/**
 * Repository for User entity with derived query methods
 */
@Repository
interface UserRepository extends CassandraRepository<User, UUID> {
    
    // Derived query methods
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByCountry(String country);
    
    List<User> findByAgeGreaterThan(Integer age);
    
    List<User> findByAgeGreaterThanAndCountry(Integer age, String country);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    long countByCountry(String country);
    
    void deleteByUsername(String username);
    
    // Custom CQL queries
    @Query("SELECT * FROM users WHERE country = ?0 ALLOW FILTERING")
    List<User> findUsersByCountryAllowFiltering(String country);
    
    @Query("SELECT * FROM users WHERE age >= ?0 ALLOW FILTERING")
    List<User> findUsersOlderThan(Integer age);
    
    @Query("SELECT COUNT(*) FROM users WHERE country = ?0 ALLOW FILTERING")
    long countUsersInCountry(String country);
}

/**
 * Repository for Order entity
 */
@Repository
interface OrderRepository extends CassandraRepository<Order, UUID> {
    
    // Derived query methods
    List<Order> findByUserId(UUID userId);
    
    List<Order> findByStatus(String status);
    
    List<Order> findByUserIdAndStatus(UUID userId, String status);
    
    List<Order> findByAmountGreaterThan(Double amount);
    
    long countByUserId(UUID userId);
    
    long countByStatus(String status);
    
    // Custom CQL queries
    @Query("SELECT * FROM orders WHERE user_id = ?0 AND status = ?1 ALLOW FILTERING")
    List<Order> findUserOrdersByStatus(UUID userId, String status);
    
    @Query("SELECT * FROM orders WHERE amount >= ?0 ALLOW FILTERING")
    List<Order> findHighValueOrders(Double minAmount);
    
    @Query("SELECT SUM(amount) FROM orders WHERE user_id = ?0 ALLOW FILTERING")
    Double getTotalAmountForUser(UUID userId);
}

/**
 * Service for user operations
 */
@Service
class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public List<User> findByCountry(String country) {
        return userRepository.findByCountry(country);
    }
    
    public List<User> findByAgeGreaterThan(Integer age) {
        return userRepository.findByAgeGreaterThan(age);
    }
    
    public List<User> findOlderThan(Integer age) {
        return userRepository.findUsersOlderThan(age);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public long countByCountry(String country) {
        return userRepository.countByCountry(country);
    }
    
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }
    
    public void deleteByUsername(String username) {
        userRepository.deleteByUsername(username);
    }
}

/**
 * Service for order operations
 */
@Service
class OrderService {
    
    private final OrderRepository orderRepository;
    
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }
    
    public Optional<Order> findById(UUID id) {
        return orderRepository.findById(id);
    }
    
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    
    public List<Order> findByUserId(UUID userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
    
    public List<Order> findUserOrdersByStatus(UUID userId, String status) {
        return orderRepository.findUserOrdersByStatus(userId, status);
    }
    
    public List<Order> findHighValueOrders(Double minAmount) {
        return orderRepository.findHighValueOrders(minAmount);
    }
    
    public Double getTotalAmountForUser(UUID userId) {
        return orderRepository.getTotalAmountForUser(userId);
    }
    
    public long countByUserId(UUID userId) {
        return orderRepository.countByUserId(userId);
    }
    
    public void deleteById(UUID id) {
        orderRepository.deleteById(id);
    }
}

/**
 * REST controller for user operations
 */
@RestController
@RequestMapping("/api/users")
class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return userService.findById(id)
                         .map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                         .map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                         .map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/country/{country}")
    public ResponseEntity<List<User>> getUsersByCountry(@PathVariable String country) {
        return ResponseEntity.ok(userService.findByCountry(country));
    }
    
    @GetMapping("/age/greater-than/{age}")
    public ResponseEntity<List<User>> getUsersByAgeGreaterThan(@PathVariable Integer age) {
        return ResponseEntity.ok(userService.findByAgeGreaterThan(age));
    }
    
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.existsByUsername(username));
    }
    
    @GetMapping("/count/country/{country}")
    public ResponseEntity<Long> countByCountry(@PathVariable String country) {
        return ResponseEntity.ok(userService.countByCountry(country));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/username/{username}")
    public ResponseEntity<Void> deleteByUsername(@PathVariable String username) {
        userService.deleteByUsername(username);
        return ResponseEntity.noContent().build();
    }
}

/**
 * REST controller for order operations
 */
@RestController
@RequestMapping("/api/orders")
class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return orderService.findById(id)
                          .map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderService.findByUserId(userId));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.findByStatus(status));
    }
    
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Order>> getUserOrdersByStatus(
            @PathVariable UUID userId, 
            @PathVariable String status) {
        return ResponseEntity.ok(orderService.findUserOrdersByStatus(userId, status));
    }
    
    @GetMapping("/high-value")
    public ResponseEntity<List<Order>> getHighValueOrders(@RequestParam Double minAmount) {
        return ResponseEntity.ok(orderService.findHighValueOrders(minAmount));
    }
    
    @GetMapping("/user/{userId}/total")
    public ResponseEntity<Double> getTotalAmountForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderService.getTotalAmountForUser(userId));
    }
    
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> countUserOrders(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderService.countByUserId(userId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        orderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Cassandra Repository Pattern
            
            This pattern demonstrates the use of Spring Data Cassandra Repository
            for simplified data access with Apache Cassandra.
            
            Features:
            - CassandraRepository for CRUD operations
            - Derived query methods (findBy, countBy, existsBy, deleteBy)
            - Custom @Query annotations with CQL
            - Type-safe queries
            - Automatic query generation
            
            User Endpoints:
            - POST /api/users - Create user
            - GET /api/users/{id} - Get user by ID
            - GET /api/users - Get all users
            - GET /api/users/username/{username} - Find by username
            - GET /api/users/email/{email} - Find by email
            - GET /api/users/country/{country} - Find by country
            - GET /api/users/age/greater-than/{age} - Filter by age
            - GET /api/users/exists/username/{username} - Check exists
            - GET /api/users/count/country/{country} - Count by country
            - DELETE /api/users/{id} - Delete user
            
            Order Endpoints:
            - POST /api/orders - Create order
            - GET /api/orders/{id} - Get order by ID
            - GET /api/orders - Get all orders
            - GET /api/orders/user/{userId} - Get user orders
            - GET /api/orders/status/{status} - Get orders by status
            - GET /api/orders/user/{userId}/status/{status} - Filter user orders
            - GET /api/orders/high-value?minAmount= - Filter by amount
            - GET /api/orders/user/{userId}/total - Get user total
            - GET /api/orders/user/{userId}/count - Count user orders
            - DELETE /api/orders/{id} - Delete order
            """);
    }
}
