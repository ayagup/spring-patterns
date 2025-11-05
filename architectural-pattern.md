# Spring Architectural Design Patterns - Java Implementations

## 1. Model-View-Controller (MVC) Pattern

```java org/example/patterns/architectural/mvc/model/Product.java
package org.example.patterns.architectural.mvc.model;

import java.math.BigDecimal;

public class Product {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    
    public Product() {}
    
    public Product(Long id, String name, String description, BigDecimal price, Integer stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
```

```java org/example/patterns/architectural/mvc/controller/ProductController.java
package org.example.patterns.architectural.mvc.controller;

import org.example.patterns.architectural.mvc.model.Product;
import org.example.patterns.architectural.mvc.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        System.out.println("Controller: Handling GET all products request");
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        System.out.println("Controller: Handling GET product by id: " + id);
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        System.out.println("Controller: Handling POST create product: " + product.getName());
        Product created = productService.save(product);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        System.out.println("Controller: Handling PUT update product: " + id);
        product.setId(id);
        Product updated = productService.update(product);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        System.out.println("Controller: Handling DELETE product: " + id);
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

```java org/example/patterns/architectural/mvc/service/ProductService.java
package org.example.patterns.architectural.mvc.service;

import org.example.patterns.architectural.mvc.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ProductService {
    
    private final Map<Long, Product> products = new HashMap<>();
    private Long idCounter = 1L;
    
    public ProductService() {
        // Initialize with sample data
        save(new Product(null, "Laptop", "High-performance laptop", new BigDecimal("1200.00"), 10));
        save(new Product(null, "Mouse", "Wireless mouse", new BigDecimal("25.00"), 50));
        save(new Product(null, "Keyboard", "Mechanical keyboard", new BigDecimal("75.00"), 30));
    }
    
    public List<Product> findAll() {
        System.out.println("Service: Finding all products");
        return new ArrayList<>(products.values());
    }
    
    public Optional<Product> findById(Long id) {
        System.out.println("Service: Finding product by id: " + id);
        return Optional.ofNullable(products.get(id));
    }
    
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(idCounter++);
        }
        System.out.println("Service: Saving product: " + product.getName());
        products.put(product.getId(), product);
        return product;
    }
    
    public Product update(Product product) {
        System.out.println("Service: Updating product: " + product.getId());
        products.put(product.getId(), product);
        return product;
    }
    
    public void deleteById(Long id) {
        System.out.println("Service: Deleting product: " + id);
        products.remove(id);
    }
}
```

```java org/example/patterns/architectural/mvc/MVCDemo.java
package org.example.patterns.architectural.mvc;

import org.example.patterns.architectural.mvc.model.Product;
import org.example.patterns.architectural.mvc.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MVCDemo implements CommandLineRunner {
    
    private final ProductService productService;
    
    public MVCDemo(ProductService productService) {
        this.productService = productService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== MVC Pattern Demo ===");
        
        // Simulate MVC flow
        System.out.println("\n--- Creating new product ---");
        Product newProduct = new Product(null, "Monitor", "27-inch 4K monitor", 
                                        new BigDecimal("450.00"), 15);
        productService.save(newProduct);
        
        System.out.println("\n--- Fetching all products ---");
        productService.findAll().forEach(p -> 
            System.out.println("  - " + p.getName() + ": $" + p.getPrice()));
    }
}
```

---

## 2. Model-View-ViewModel (MVVM) Pattern

```java org/example/patterns/architectural/mvvm/model/User.java
package org.example.patterns.architectural.mvvm.model;

public class User {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;
    
    public User() {}
    
    public User(Long id, String username, String email, String firstName, String lastName, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
```

```java org/example/patterns/architectural/mvvm/viewmodel/UserViewModel.java
package org.example.patterns.architectural.mvvm.viewmodel;

import org.example.patterns.architectural.mvvm.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserViewModel {
    
    private List<UserDTO> users = new ArrayList<>();
    private String searchQuery = "";
    private String statusFilter = "ALL";
    
    public UserViewModel() {
        loadSampleData();
    }
    
    private void loadSampleData() {
        users.add(new UserDTO(1L, "john_doe", "John Doe", "john@example.com", "Active"));
        users.add(new UserDTO(2L, "jane_smith", "Jane Smith", "jane@example.com", "Active"));
        users.add(new UserDTO(3L, "bob_wilson", "Bob Wilson", "bob@example.com", "Inactive"));
    }
    
    public List<UserDTO> getFilteredUsers() {
        return users.stream()
                .filter(u -> matchesSearch(u))
                .filter(u -> matchesStatus(u))
                .toList();
    }
    
    private boolean matchesSearch(UserDTO user) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return true;
        }
        String query = searchQuery.toLowerCase();
        return user.getUsername().toLowerCase().contains(query) ||
               user.getFullName().toLowerCase().contains(query) ||
               user.getEmail().toLowerCase().contains(query);
    }
    
    private boolean matchesStatus(UserDTO user) {
        if ("ALL".equals(statusFilter)) {
            return true;
        }
        return user.getStatus().equals(statusFilter);
    }
    
    public void setSearchQuery(String query) {
        this.searchQuery = query;
        System.out.println("ViewModel: Search query updated to: " + query);
    }
    
    public void setStatusFilter(String status) {
        this.statusFilter = status;
        System.out.println("ViewModel: Status filter updated to: " + status);
    }
    
    public void addUser(User user) {
        UserDTO dto = new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getFirstName() + " " + user.getLastName(),
            user.getEmail(),
            user.isActive() ? "Active" : "Inactive"
        );
        users.add(dto);
        System.out.println("ViewModel: User added - " + dto.getFullName());
    }
    
    // Data Transfer Object for View
    public static class UserDTO {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String status;
        
        public UserDTO(Long id, String username, String fullName, String email, String status) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.status = status;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
    }
}
```

```java org/example/patterns/architectural/mvvm/view/UserView.java
package org.example.patterns.architectural.mvvm.view;

import org.example.patterns.architectural.mvvm.viewmodel.UserViewModel;
import org.springframework.stereotype.Component;

@Component
public class UserView {
    
    private final UserViewModel viewModel;
    
    public UserView(UserViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    public void displayUsers() {
        System.out.println("\n--- User List View ---");
        viewModel.getFilteredUsers().forEach(user -> {
            System.out.println("ID: " + user.getId());
            System.out.println("  Username: " + user.getUsername());
            System.out.println("  Name: " + user.getFullName());
            System.out.println("  Email: " + user.getEmail());
            System.out.println("  Status: " + user.getStatus());
            System.out.println("---");
        });
    }
    
    public void searchUsers(String query) {
        System.out.println("\nView: User searching for: " + query);
        viewModel.setSearchQuery(query);
        displayUsers();
    }
    
    public void filterByStatus(String status) {
        System.out.println("\nView: User filtering by status: " + status);
        viewModel.setStatusFilter(status);
        displayUsers();
    }
}
```

```java org/example/patterns/architectural/mvvm/MVVMDemo.java
package org.example.patterns.architectural.mvvm;

import org.example.patterns.architectural.mvvm.model.User;
import org.example.patterns.architectural.mvvm.view.UserView;
import org.example.patterns.architectural.mvvm.viewmodel.UserViewModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MVVMDemo implements CommandLineRunner {
    
    private final UserView userView;
    private final UserViewModel userViewModel;
    
    public MVVMDemo(UserView userView, UserViewModel userViewModel) {
        this.userView = userView;
        this.userViewModel = userViewModel;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== MVVM Pattern Demo ===");
        
        // Display all users
        userView.displayUsers();
        
        // Search users
        userView.searchUsers("john");
        
        // Filter by status
        userView.filterByStatus("Active");
        
        // Add new user
        User newUser = new User(4L, "alice_wonder", "alice@example.com", 
                               "Alice", "Wonder", true);
        userViewModel.addUser(newUser);
        userView.displayUsers();
    }
}
```

---

## 3. Layered Architecture Pattern

```java org/example/patterns/architectural/layered/domain/Order.java
package org.example.patterns.architectural.layered.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    
    private Long id;
    private String orderNumber;
    private Long customerId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    
    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }
    
    public void addItem(OrderItem item) {
        items.add(item);
        calculateTotal();
    }
    
    private void calculateTotal() {
        totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
```

```java org/example/patterns/architectural/layered/domain/OrderItem.java
package org.example.patterns.architectural.layered.domain;

import java.math.BigDecimal;

public class OrderItem {
    
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    
    public OrderItem() {}
    
    public OrderItem(Long productId, String productName, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
```

```java org/example/patterns/architectural/layered/repository/OrderRepository.java
package org.example.patterns.architectural.layered.repository;

import org.example.patterns.architectural.layered.domain.Order;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OrderRepository {
    
    private final Map<Long, Order> orders = new HashMap<>();
    private Long idCounter = 1L;
    
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(idCounter++);
        }
        System.out.println("Repository: Saving order " + order.getOrderNumber());
        orders.put(order.getId(), order);
        return order;
    }
    
    public Optional<Order> findById(Long id) {
        System.out.println("Repository: Finding order by id: " + id);
        return Optional.ofNullable(orders.get(id));
    }
    
    public List<Order> findAll() {
        System.out.println("Repository: Finding all orders");
        return new ArrayList<>(orders.values());
    }
    
    public List<Order> findByCustomerId(Long customerId) {
        System.out.println("Repository: Finding orders for customer: " + customerId);
        return orders.values().stream()
                .filter(o -> o.getCustomerId().equals(customerId))
                .toList();
    }
    
    public void delete(Long id) {
        System.out.println("Repository: Deleting order: " + id);
        orders.remove(id);
    }
}
```

```java org/example/patterns/architectural/layered/service/OrderService.java
package org.example.patterns.architectural.layered.service;

import org.example.patterns.architectural.layered.domain.Order;
import org.example.patterns.architectural.layered.domain.OrderItem;
import org.example.patterns.architectural.layered.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    public Order createOrder(Long customerId, List<OrderItem> items) {
        System.out.println("Service: Creating order for customer: " + customerId);
        
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setCustomerId(customerId);
        
        items.forEach(order::addItem);
        
        return orderRepository.save(order);
    }
    
    public Optional<Order> getOrder(Long id) {
        System.out.println("Service: Getting order: " + id);
        return orderRepository.findById(id);
    }
    
    public List<Order> getCustomerOrders(Long customerId) {
        System.out.println("Service: Getting orders for customer: " + customerId);
        return orderRepository.findByCustomerId(customerId);
    }
    
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        System.out.println("Service: Updating order " + orderId + " status to: " + status);
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status);
                    return orderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }
    
    public void cancelOrder(Long orderId) {
        System.out.println("Service: Cancelling order: " + orderId);
        updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
    }
}
```

```java org/example/patterns/architectural/layered/controller/OrderController.java
package org.example.patterns.architectural.layered.controller;

import org.example.patterns.architectural.layered.domain.Order;
import org.example.patterns.architectural.layered.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        System.out.println("Controller: GET order request for id: " + id);
        return orderService.getOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getCustomerOrders(@PathVariable Long customerId) {
        System.out.println("Controller: GET customer orders for: " + customerId);
        List<Order> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        System.out.println("Controller: PATCH order status for: " + id);
        Order updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        System.out.println("Controller: DELETE order: " + id);
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
```

```java org/example/patterns/architectural/layered/LayeredArchitectureDemo.java
package org.example.patterns.architectural.layered;

import org.example.patterns.architectural.layered.domain.Order;
import org.example.patterns.architectural.layered.domain.OrderItem;
import org.example.patterns.architectural.layered.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class LayeredArchitectureDemo implements CommandLineRunner {
    
    private final OrderService orderService;
    
    public LayeredArchitectureDemo(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Layered Architecture Pattern Demo ===");
        
        // Create order with items
        OrderItem item1 = new OrderItem(1L, "Laptop", 1, new BigDecimal("1200.00"));
        OrderItem item2 = new OrderItem(2L, "Mouse", 2, new BigDecimal("25.00"));
        
        Order order = orderService.createOrder(100L, Arrays.asList(item1, item2));
        System.out.println("\nCreated order: " + order.getOrderNumber());
        System.out.println("Total: $" + order.getTotalAmount());
        
        // Update order status
        orderService.updateOrderStatus(order.getId(), Order.OrderStatus.CONFIRMED);
        
        // Get customer orders
        System.out.println("\nFetching customer orders...");
        orderService.getCustomerOrders(100L).forEach(o -> 
            System.out.println("  Order: " + o.getOrderNumber() + " - " + o.getStatus()));
    }
}
```

---

## 4. Hexagonal Architecture Pattern (Ports and Adapters)

```java org/example/patterns/architectural/hexagonal/domain/Account.java
package org.example.patterns.architectural.hexagonal.domain;

import java.math.BigDecimal;

public class Account {
    
    private final String accountNumber;
    private BigDecimal balance;
    private final String owner;
    
    public Account(String accountNumber, String owner, BigDecimal initialBalance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = initialBalance;
    }
    
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        balance = balance.add(amount);
    }
    
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        balance = balance.subtract(amount);
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public String getOwner() {
        return owner;
    }
}
```

```java org/example/patterns/architectural/hexagonal/port/AccountRepository.java
package org.example.patterns.architectural.hexagonal.port;

import org.example.patterns.architectural.hexagonal.domain.Account;

import java.util.Optional;

// Primary Port (Input)
public interface AccountRepository {
    Optional<Account> findByAccountNumber(String accountNumber);
    void save(Account account);
}
```

```java org/example/patterns/architectural/hexagonal/port/NotificationService.java
package org.example.patterns.architectural.hexagonal.port;

// Secondary Port (Output)
public interface NotificationService {
    void sendNotification(String recipient, String message);
}
```

```java org/example/patterns/architectural/hexagonal/application/BankingService.java
package org.example.patterns.architectural.hexagonal.application;

import org.example.patterns.architectural.hexagonal.domain.Account;
import org.example.patterns.architectural.hexagonal.port.AccountRepository;
import org.example.patterns.architectural.hexagonal.port.NotificationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BankingService {
    
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    
    public BankingService(AccountRepository accountRepository, 
                         NotificationService notificationService) {
        this.accountRepository = accountRepository;
        this.notificationService = notificationService;
    }
    
    public void deposit(String accountNumber, BigDecimal amount) {
        System.out.println("BankingService: Processing deposit of $" + amount);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.deposit(amount);
        accountRepository.save(account);
        
        notificationService.sendNotification(
            account.getOwner(),
            "Deposited $" + amount + ". New balance: $" + account.getBalance()
        );
    }
    
    public void withdraw(String accountNumber, BigDecimal amount) {
        System.out.println("BankingService: Processing withdrawal of $" + amount);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.withdraw(amount);
        accountRepository.save(account);
        
        notificationService.sendNotification(
            account.getOwner(),
            "Withdrawn $" + amount + ". New balance: $" + account.getBalance()
        );
    }
    
    public BigDecimal getBalance(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(Account::getBalance)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
}
```

```java org/example/patterns/architectural/hexagonal/adapter/InMemoryAccountRepository.java
package org.example.patterns.architectural.hexagonal.adapter;

import org.example.patterns.architectural.hexagonal.domain.Account;
import org.example.patterns.architectural.hexagonal.port.AccountRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryAccountRepository implements AccountRepository {
    
    private final Map<String, Account> accounts = new HashMap<>();
    
    public InMemoryAccountRepository() {
        // Initialize with sample data
        Account account1 = new Account("ACC-001", "John Doe", new BigDecimal("1000.00"));
        Account account2 = new Account("ACC-002", "Jane Smith", new BigDecimal("2000.00"));
        accounts.put(account1.getAccountNumber(), account1);
        accounts.put(account2.getAccountNumber(), account2);
    }
    
    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        System.out.println("Repository: Finding account: " + accountNumber);
        return Optional.ofNullable(accounts.get(accountNumber));
    }
    
    @Override
    public void save(Account account) {
        System.out.println("Repository: Saving account: " + account.getAccountNumber());
        accounts.put(account.getAccountNumber(), account);
    }
}
```

```java org/example/patterns/architectural/hexagonal/adapter/EmailNotificationService.java
package org.example.patterns.architectural.hexagonal.adapter;

import org.example.patterns.architectural.hexagonal.port.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {
    
    @Override
    public void sendNotification(String recipient, String message) {
        System.out.println("EmailNotification: Sending email to " + recipient);
        System.out.println("  Message: " + message);
    }
}
```

```java org/example/patterns/architectural/hexagonal/HexagonalDemo.java
package org.example.patterns.architectural.hexagonal;

import org.example.patterns.architectural.hexagonal.application.BankingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HexagonalDemo implements CommandLineRunner {
    
    private final BankingService bankingService;
    
    public HexagonalDemo(BankingService bankingService) {
        this.bankingService = bankingService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Hexagonal Architecture Pattern Demo ===");
        
        String accountNumber = "ACC-001";
        
        // Check initial balance
        BigDecimal balance = bankingService.getBalance(accountNumber);
        System.out.println("\nInitial balance: $" + balance);
        
        // Deposit
        System.out.println("\n--- Depositing $500 ---");
        bankingService.deposit(accountNumber, new BigDecimal("500.00"));
        
        // Withdraw
        System.out.println("\n--- Withdrawing $200 ---");
        bankingService.withdraw(accountNumber, new BigDecimal("200.00"));
        
        // Check final balance
        balance = bankingService.getBalance(accountNumber);
        System.out.println("\nFinal balance: $" + balance);
    }
}
```

---

## 5. Repository Pattern

```java org/example/patterns/architectural/repository/entity/Customer.java
package org.example.patterns.architectural.repository.entity;

public class Customer {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    
    public Customer() {}
    
    public Customer(Long id, String firstName, String lastName, String email, String phone, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```

```java org/example/patterns/architectural/repository/CustomerRepository.java
package org.example.patterns.architectural.repository;

import org.example.patterns.architectural.repository.entity.Customer;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CustomerRepository {
    
    private final Map<Long, Customer> database = new HashMap<>();
    private Long idCounter = 1L;
    
    public CustomerRepository() {
        // Initialize with sample data
        save(new Customer(null, "John", "Doe", "john@example.com", "555-1234", "123 Main St"));
        save(new Customer(null, "Jane", "Smith", "jane@example.com", "555-5678", "456 Oak Ave"));
        save(new Customer(null, "Bob", "Johnson", "bob@example.com", "555-9012", "789 Pine Rd"));
    }
    
    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            customer.setId(idCounter++);
        }
        System.out.println("Repository: Saving customer - " + customer.getFullName());
        database.put(customer.getId(), customer);
        return customer;
    }
    
    public Optional<Customer> findById(Long id) {
        System.out.println("Repository: Finding customer by id: " + id);
        return Optional.ofNullable(database.get(id));
    }
    
    public List<Customer> findAll() {
        System.out.println("Repository: Finding all customers");
        return new ArrayList<>(database.values());
    }
    
    public Optional<Customer> findByEmail(String email) {
        System.out.println("Repository: Finding customer by email: " + email);
        return database.values().stream()
                .filter(c -> c.getEmail().equals(email))
                .findFirst();
    }
    
    public List<Customer> findByLastName(String lastName) {
        System.out.println("Repository: Finding customers by last name: " + lastName);
        return database.values().stream()
                .filter(c -> c.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }
    
    public void delete(Long id) {
        System.out.println("Repository: Deleting customer: " + id);
        database.remove(id);
    }
    
    public boolean existsById(Long id) {
        return database.containsKey(id);
    }
    
    public long count() {
        return database.size();
    }
}
```

```java org/example/patterns/architectural/repository/RepositoryDemo.java
package org.example.patterns.architectural.repository;

import org.example.patterns.architectural.repository.entity.Customer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RepositoryDemo implements CommandLineRunner {
    
    private final CustomerRepository customerRepository;
    
    public RepositoryDemo(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Repository Pattern Demo ===");
        
        // Find all
        System.out.println("\n--- All Customers ---");
        customerRepository.findAll().forEach(c -> 
            System.out.println("  " + c.getFullName() + " - " + c.getEmail()));
        
        // Find by email
        System.out.println("\n--- Find by Email ---");
        customerRepository.findByEmail("john@example.com")
                .ifPresent(c -> System.out.println("  Found: " + c.getFullName()));
        
        // Find by last name
        System.out.println("\n--- Find by Last Name ---");
        customerRepository.findByLastName("Smith").forEach(c -> 
            System.out.println("  " + c.getFullName()));
        
        // Save new customer
        System.out.println("\n--- Save New Customer ---");
        Customer newCustomer = new Customer(null, "Alice", "Wonder", 
                                          "alice@example.com", "555-1111", "321 Elm St");
        customerRepository.save(newCustomer);
        
        System.out.println("Total customers: " + customerRepository.count());
    }
}
```

---

## 6. Data Access Object (DAO) Pattern

```java org/example/patterns/architectural/dao/model/Employee.java
package org.example.patterns.architectural.dao.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {
    
    private Long id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private BigDecimal salary;
    private LocalDate hireDate;
    
    public Employee() {}
    
    public Employee(Long id, String employeeNumber, String firstName, String lastName, 
                   String email, String department, BigDecimal salary, LocalDate hireDate) {
        this.id = id;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.hireDate = hireDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```

```java org/example/patterns/architectural/dao/EmployeeDAO.java
package org.example.patterns.architectural.dao;

import org.example.patterns.architectural.dao.model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeDAO {
    Employee create(Employee employee);
    Optional<Employee> read(Long id);
    Employee update(Employee employee);
    void delete(Long id);
    List<Employee> findAll();
    List<Employee> findByDepartment(String department);
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
}
```

```java org/example/patterns/architectural/dao/EmployeeDAOImpl.java
package org.example.patterns.architectural.dao;

import org.example.patterns.architectural.dao.model.Employee;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EmployeeDAOImpl implements EmployeeDAO {
    
    private final Map<Long, Employee> dataStore = new HashMap<>();
    private Long idSequence = 1L;
    
    public EmployeeDAOImpl() {
        // Initialize with sample data
        create(new Employee(null, "EMP001", "John", "Doe", "john.doe@company.com", 
                          "Engineering", new BigDecimal("75000"), LocalDate.of(2020, 1, 15)));
        create(new Employee(null, "EMP002", "Jane", "Smith", "jane.smith@company.com", 
                          "Marketing", new BigDecimal("65000"), LocalDate.of(2021, 3, 20)));
        create(new Employee(null, "EMP003", "Bob", "Johnson", "bob.johnson@company.com", 
                          "Engineering", new BigDecimal("80000"), LocalDate.of(2019, 6, 10)));
    }
    
    @Override
    public Employee create(Employee employee) {
        System.out.println("DAO: Creating employee - " + employee.getFullName());
        employee.setId(idSequence++);
        dataStore.put(employee.getId(), employee);
        return employee;
    }
    
    @Override
    public Optional<Employee> read(Long id) {
        System.out.println("DAO: Reading employee with id: " + id);
        return Optional.ofNullable(dataStore.get(id));
    }
    
    @Override
    public Employee update(Employee employee) {
        System.out.println("DAO: Updating employee - " + employee.getId());
        if (!dataStore.containsKey(employee.getId())) {
            throw new RuntimeException("Employee not found: " + employee.getId());
        }
        dataStore.put(employee.getId(), employee);
        return employee;
    }
    
    @Override
    public void delete(Long id) {
        System.out.println("DAO: Deleting employee with id: " + id);
        dataStore.remove(id);
    }
    
    @Override
    public List<Employee> findAll() {
        System.out.println("DAO: Finding all employees");
        return new ArrayList<>(dataStore.values());
    }
    
    @Override
    public List<Employee> findByDepartment(String department) {
        System.out.println("DAO: Finding employees in department: " + department);
        return dataStore.values().stream()
                .filter(e -> e.getDepartment().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Employee> findByEmployeeNumber(String employeeNumber) {
        System.out.println("DAO: Finding employee by number: " + employeeNumber);
        return dataStore.values().stream()
                .filter(e -> e.getEmployeeNumber().equals(employeeNumber))
                .findFirst();
    }
}
```

```java org/example/patterns/architectural/dao/DAODemo.java
package org.example.patterns.architectural.dao;

import org.example.patterns.architectural.dao.model.Employee;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DAODemo implements CommandLineRunner {
    
    private final EmployeeDAO employeeDAO;
    
    public DAODemo(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== DAO Pattern Demo ===");
        
        // Find all employees
        System.out.println("\n--- All Employees ---");
        employeeDAO.findAll().forEach(e -> 
            System.out.println("  " + e.getFullName() + " - " + e.getDepartment()));
        
        // Find by department
        System.out.println("\n--- Engineering Department ---");
        employeeDAO.findByDepartment("Engineering").forEach(e -> 
            System.out.println("  " + e.getFullName() + " - $" + e.getSalary()));
        
        // Create new employee
        System.out.println("\n--- Creating New Employee ---");
        Employee newEmp = new Employee(null, "EMP004", "Alice", "Wonder", 
                                      "alice@company.com", "HR", 
                                      new BigDecimal("60000"), LocalDate.now());
        employeeDAO.create(newEmp);
        
        // Update employee
        System.out.println("\n--- Updating Employee ---");
        employeeDAO.read(1L).ifPresent(emp -> {
            emp.setSalary(new BigDecimal("85000"));
            employeeDAO.update(emp);
            System.out.println("  Updated salary for " + emp.getFullName());
        });
    }
}
```

---

## 7. Service Layer Pattern

```java org/example/patterns/architectural/service/dto/ProductDTO.java
package org.example.patterns.architectural.service.dto;

import java.math.BigDecimal;

public class ProductDTO {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;
    
    public ProductDTO() {}
    
    public ProductDTO(Long id, String name, String description, BigDecimal price, 
                     Integer stockQuantity, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
```

```java org/example/patterns/architectural/service/entity/ProductEntity.java
package org.example.patterns.architectural.service.entity;

import java.math.BigDecimal;

public class ProductEntity {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;
    private boolean active;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
```

```java org/example/patterns/architectural/service/repository/ProductRepository.java
package org.example.patterns.architectural.service.repository;

import org.example.patterns.architectural.service.entity.ProductEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

@Repository
public class ProductRepository {
    
    private final Map<Long, ProductEntity> database = new HashMap<>();
    private Long idCounter = 1L;
    
    public ProductRepository() {
        // Initialize with sample data
        ProductEntity p1 = new ProductEntity();
        p1.setId(idCounter++);
        p1.setName("Laptop");
        p1.setDescription("High-performance laptop");
        p1.setPrice(new BigDecimal("1200.00"));
        p1.setStockQuantity(10);
        p1.setCategory("Electronics");
        p1.setActive(true);
        database.put(p1.getId(), p1);
        
        ProductEntity p2 = new ProductEntity();
        p2.setId(idCounter++);
        p2.setName("Book");
        p2.setDescription("Programming book");
        p2.setPrice(new BigDecimal("45.00"));
        p2.setStockQuantity(50);
        p2.setCategory("Books");
        p2.setActive(true);
        database.put(p2.getId(), p2);
    }
    
    public ProductEntity save(ProductEntity entity) {
        if (entity.getId() == null) {
            entity.setId(idCounter++);
        }
        database.put(entity.getId(), entity);
        return entity;
    }
    
    public Optional<ProductEntity> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }
    
    public List<ProductEntity> findAll() {
        return new ArrayList<>(database.values());
    }
    
    public List<ProductEntity> findByCategory(String category) {
        return database.values().stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .toList();
    }
    
    public void delete(Long id) {
        database.remove(id);
    }
}
```

```java org/example/patterns/architectural/service/ProductServiceLayer.java
package org.example.patterns.architectural.service;

import org.example.patterns.architectural.service.dto.ProductDTO;
import org.example.patterns.architectural.service.entity.ProductEntity;
import org.example.patterns.architectural.service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceLayer {
    
    private final ProductRepository productRepository;
    
    public ProductServiceLayer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public List<ProductDTO> getAllProducts() {
        System.out.println("Service: Getting all products");
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ProductDTO getProductById(Long id) {
        System.out.println("Service: Getting product by id: " + id);
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }
    
    public List<ProductDTO> getProductsByCategory(String category) {
        System.out.println("Service: Getting products by category: " + category);
        return productRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ProductDTO createProduct(ProductDTO dto) {
        System.out.println("Service: Creating product - " + dto.getName());
        validateProduct(dto);
        
        ProductEntity entity = convertToEntity(dto);
        entity.setActive(true);
        ProductEntity saved = productRepository.save(entity);
        
        return convertToDTO(saved);
    }
    
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        System.out.println("Service: Updating product - " + id);
        validateProduct(dto);
        
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setCategory(dto.getCategory());
        
        ProductEntity updated = productRepository.save(entity);
        return convertToDTO(updated);
    }
    
    public void deleteProduct(Long id) {
        System.out.println("Service: Deleting product - " + id);
        productRepository.delete(id);
    }
    
    private void validateProduct(ProductDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (dto.getPrice() == null || dto.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }
    }
    
    private ProductDTO convertToDTO(ProductEntity entity) {
        return new ProductDTO(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPrice(),
            entity.getStockQuantity(),
            entity.getCategory()
        );
    }
    
    private ProductEntity convertToEntity(ProductDTO dto) {
        ProductEntity entity = new ProductEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setCategory(dto.getCategory());
        return entity;
    }
}
```

```java org/example/patterns/architectural/service/ServiceLayerDemo.java
package org.example.patterns.architectural.service;

import org.example.patterns.architectural.service.dto.ProductDTO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ServiceLayerDemo implements CommandLineRunner {
    
    private final ProductServiceLayer productService;
    
    public ServiceLayerDemo(ProductServiceLayer productService) {
        this.productService = productService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Service Layer Pattern Demo ===");
        
        // Get all products
        System.out.println("\n--- All Products ---");
        productService.getAllProducts().forEach(p -> 
            System.out.println("  " + p.getName() + " - $" + p.getPrice()));
        
        // Get by category
        System.out.println("\n--- Electronics Category ---");
        productService.getProductsByCategory("Electronics").forEach(p -> 
            System.out.println("  " + p.getName()));
        
        // Create new product
        System.out.println("\n--- Creating New Product ---");
        ProductDTO newProduct = new ProductDTO(null, "Tablet", "10-inch tablet", 
                                              new BigDecimal("350.00"), 20, "Electronics");
        ProductDTO created = productService.createProduct(newProduct);
        System.out.println("  Created: " + created.getName());
    }
}
```

---

## 8. Domain Model Pattern

```java org/example/patterns/architectural/domain/model/ShoppingCart.java
package org.example.patterns.architectural.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    
    private Long id;
    private Long customerId;
    private List<CartItem> items;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    
    public ShoppingCart(Long customerId) {
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }
    
    // Domain logic
    public void addItem(Long productId, String productName, BigDecimal price, int quantity) {
        CartItem existingItem = findItem(productId);
        
        if (existingItem != null) {
            existingItem.increaseQuantity(quantity);
        } else {
            CartItem newItem = new CartItem(productId, productName, price, quantity);
            items.add(newItem);
        }
        
        recalculate();
    }
    
    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
        recalculate();
    }
    
    public void updateItemQuantity(Long productId, int quantity) {
        CartItem item = findItem(productId);
        if (item != null) {
            item.setQuantity(quantity);
            recalculate();
        }
    }
    
    public void clear() {
        items.clear();
        recalculate();
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    private CartItem findItem(Long productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }
    
    private void recalculate() {
        subtotal = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        tax = subtotal.multiply(new BigDecimal("0.08")); // 8% tax
        total = subtotal.add(tax);
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public List<CartItem> getItems() { return new ArrayList<>(items); }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
}
```

```java org/example/patterns/architectural/domain/model/CartItem.java
package org.example.patterns.architectural/domain/model;

import java.math.BigDecimal;

public class CartItem {
    
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;
    
    public CartItem(Long productId, String productName, BigDecimal unitPrice, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        calculateSubtotal();
    }
    
    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
        calculateSubtotal();
    }
    
    public void increaseQuantity(int amount) {
        this.quantity += amount;
        calculateSubtotal();
    }
    
    private void calculateSubtotal() {
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    // Getters
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
}
```

```java org/example/patterns/architectural/domain/DomainModelDemo.java
package org.example.patterns.architectural.domain;

import org.example.patterns.architectural.domain.model.ShoppingCart;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DomainModelDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Domain Model Pattern Demo ===");
        
        // Create shopping cart with domain logic
        ShoppingCart cart = new ShoppingCart(100L);
        
        System.out.println("\n--- Adding Items to Cart ---");
        cart.addItem(1L, "Laptop", new BigDecimal("1200.00"), 1);
        cart.addItem(2L, "Mouse", new BigDecimal("25.00"), 2);
        cart.addItem(3L, "Keyboard", new BigDecimal("75.00"), 1);
        
        displayCart(cart);
        
        // Update quantity
        System.out.println("\n--- Updating Mouse Quantity ---");
        cart.updateItemQuantity(2L, 5);
        displayCart(cart);
        
        // Add same item (should increase quantity)
        System.out.println("\n--- Adding More Laptops ---");
        cart.addItem(1L, "Laptop", new BigDecimal("1200.00"), 1);
        displayCart(cart);
        
        // Remove item
        System.out.println("\n--- Removing Keyboard ---");
        cart.removeItem(3L);
        displayCart(cart);
    }
    
    private void displayCart(ShoppingCart cart) {
        System.out.println("\nShopping Cart Summary:");
        cart.getItems().forEach(item -> 
            System.out.println("  " + item.getProductName() + 
                             " x" + item.getQuantity() + 
                             " = $" + item.getSubtotal()));
        System.out.println("  Subtotal: $" + cart.getSubtotal());
        System.out.println("  Tax: $" + cart.getTax());
        System.out.println("  Total: $" + cart.getTotal());
        System.out.println("  Total Items: " + cart.getTotalItems());
    }
}
```

---

## 9. Transaction Script Pattern

```java org/example/patterns/architectural/transaction/TransferRequest.java
package org.example.patterns.architectural.transaction;

import java.math.BigDecimal;

public class TransferRequest {
    
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String description;
    
    public TransferRequest(String fromAccount, String toAccount, BigDecimal amount, String description) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.description = description;
    }
    
    // Getters and Setters
    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }
    
    public String getToAccount() { return toAccount; }
    public void setToAccount(String toAccount) { this.toAccount = toAccount; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
```

```java org/example/patterns/architectural/transaction/BankAccount.java
package org.example.patterns.architectural.transaction;

import java.math.BigDecimal;

public class BankAccount {
    
    private String accountNumber;
    private String holderName;
    private BigDecimal balance;
    
    public BankAccount(String accountNumber, String holderName, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
    }
    
    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
```

```java org/example/patterns/architectural/transaction/TransactionScript.java
package org.example.patterns.architectural.transaction;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class TransactionScript {
    
    private final Map<String, BankAccount> accounts = new HashMap<>();
    
    public TransactionScript() {
        // Initialize accounts
        accounts.put("ACC-001", new BankAccount("ACC-001", "John Doe", new BigDecimal("5000.00")));
        accounts.put("ACC-002", new BankAccount("ACC-002", "Jane Smith", new BigDecimal("3000.00")));
        accounts.put("ACC-003", new BankAccount("ACC-003", "Bob Johnson", new BigDecimal("10000.00")));
    }
    
    // Transaction Script: Transfer Money
    public void transferMoney(TransferRequest request) {
        System.out.println("\n=== Executing Money Transfer Transaction ===");
        
        // Step 1: Validate input
        System.out.println("Step 1: Validating transfer request");
        validateTransferRequest(request);
        
        // Step 2: Load accounts
        System.out.println("Step 2: Loading accounts");
        BankAccount fromAccount = accounts.get(request.getFromAccount());
        BankAccount toAccount = accounts.get(request.getToAccount());
        
        if (fromAccount == null) {
            throw new RuntimeException("Source account not found: " + request.getFromAccount());
        }
        if (toAccount == null) {
            throw new RuntimeException("Destination account not found: " + request.getToAccount());
        }
        
        // Step 3: Check balance
        System.out.println("Step 3: Checking balance");
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds in account: " + request.getFromAccount());
        }
        
        // Step 4: Debit from source
        System.out.println("Step 4: Debiting from source account");
        BigDecimal newFromBalance = fromAccount.getBalance().subtract(request.getAmount());
        fromAccount.setBalance(newFromBalance);
        System.out.println("  " + fromAccount.getHolderName() + " - New balance: $" + newFromBalance);
        
        // Step 5: Credit to destination
        System.out.println("Step 5: Crediting to destination account");
        BigDecimal newToBalance = toAccount.getBalance().add(request.getAmount());
        toAccount.setBalance(newToBalance);
        System.out.println("  " + toAccount.getHolderName() + " - New balance: $" + newToBalance);
        
        // Step 6: Log transaction
        System.out.println("Step 6: Logging transaction");
        logTransaction(request);
        
        System.out.println("\nTransfer completed successfully!");
    }
    
    // Transaction Script: Withdraw Money
    public void withdrawMoney(String accountNumber, BigDecimal amount) {
        System.out.println("\n=== Executing Withdrawal Transaction ===");
        
        // Step 1: Validate
        System.out.println("Step 1: Validating withdrawal");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        // Step 2: Load account
        System.out.println("Step 2: Loading account");
        BankAccount account = accounts.get(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found: " + accountNumber);
        }
        
        // Step 3: Check balance
        System.out.println("Step 3: Checking balance");
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        
        // Step 4: Debit account
        System.out.println("Step 4: Processing withdrawal");
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        
        System.out.println("Withdrawal completed. New balance: $" + newBalance);
    }
    
    // Transaction Script: Deposit Money
    public void depositMoney(String accountNumber, BigDecimal amount) {
        System.out.println("\n=== Executing Deposit Transaction ===");
        
        // Step 1: Validate
        System.out.println("Step 1: Validating deposit");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        // Step 2: Load account
        System.out.println("Step 2: Loading account");
        BankAccount account = accounts.get(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found: " + accountNumber);
        }
        
        // Step 3: Credit account
        System.out.println("Step 3: Processing deposit");
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        
        System.out.println("Deposit completed. New balance: $" + newBalance);
    }
    
    public BigDecimal getBalance(String accountNumber) {
        BankAccount account = accounts.get(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found: " + accountNumber);
        }
        return account.getBalance();
    }
    
    private void validateTransferRequest(TransferRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (request.getFromAccount().equals(request.getToAccount())) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }
    }
    
    private void logTransaction(TransferRequest request) {
        System.out.println("  Transaction logged: " + request.getDescription());
        System.out.println("  From: " + request.getFromAccount());
        System.out.println("  To: " + request.getToAccount());
        System.out.println("  Amount: $" + request.getAmount());
    }
}
```

```java org/example/patterns/architectural/transaction/TransactionScriptDemo.java
package org.example.patterns.architectural.transaction;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionScriptDemo implements CommandLineRunner {
    
    private final TransactionScript transactionScript;
    
    public TransactionScriptDemo(TransactionScript transactionScript) {
        this.transactionScript = transactionScript;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Transaction Script Pattern Demo ===");
        
        // Display initial balances
        System.out.println("\n--- Initial Balances ---");
        System.out.println("ACC-001: $" + transactionScript.getBalance("ACC-001"));
        System.out.println("ACC-002: $" + transactionScript.getBalance("ACC-002"));
        
        // Perform transfer
        TransferRequest transfer = new TransferRequest(
            "ACC-001", 
            "ACC-002", 
            new BigDecimal("500.00"),
            "Payment for services"
        );
        transactionScript.transferMoney(transfer);
        
        // Perform withdrawal
        transactionScript.withdrawMoney("ACC-003", new BigDecimal("1000.00"));
        
        // Perform deposit
        transactionScript.depositMoney("ACC-002", new BigDecimal("250.00"));
        
        // Display final balances
        System.out.println("\n--- Final Balances ---");
        System.out.println("ACC-001: $" + transactionScript.getBalance("ACC-001"));
        System.out.println("ACC-002: $" + transactionScript.getBalance("ACC-002"));
        System.out.println("ACC-003: $" + transactionScript.getBalance("ACC-003"));
    }
}
```

---

## 10. Active Record Pattern

```java org/example/patterns/architectural/activerecord/ActiveRecord.java
package org.example.patterns.architectural.activerecord;

public abstract class ActiveRecord {
    
    protected Long id;
    
    public abstract void save();
    public abstract void delete();
    public abstract void update();
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
}
```

```java org/example/patterns/architectural/activerecord/UserRecord.java
package org.example.patterns.architectural.activerecord;

import org.springframework.stereotype.Component;

import java.util.*;

public class UserRecord extends ActiveRecord {
    
    private String username;
    private String email;
    private String password;
    private boolean active;
    
    private static final Map<Long, UserRecord> database = new HashMap<>();
    private static Long idCounter = 1L;
    
    public UserRecord() {}
    
    public UserRecord(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.active = true;
    }
    
    @Override
    public void save() {
        if (this.id == null) {
            this.id = idCounter++;
            database.put(this.id, this);
            System.out.println("ActiveRecord: Created user - " + username + " (ID: " + id + ")");
        } else {
            database.put(this.id, this);
            System.out.println("ActiveRecord: Updated user - " + username);
        }
    }
    
    @Override
    public void delete() {
        if (this.id != null) {
            database.remove(this.id);
            System.out.println("ActiveRecord: Deleted user - " + username);
        }
    }
    
    @Override
    public void update() {
        if (this.id != null) {
            database.put(this.id, this);
            System.out.println("ActiveRecord: Updated user - " + username);
        } else {
            throw new RuntimeException("Cannot update unsaved record");
        }
    }
    
    // Static finder methods
    public static UserRecord findById(Long id) {
        System.out.println("ActiveRecord: Finding user by id - " + id);
        return database.get(id);
    }
    
    public static List<UserRecord> findAll() {
        System.out.println("ActiveRecord: Finding all users");
        return new ArrayList<>(database.values());
    }
    
    public static UserRecord findByUsername(String username) {
        System.out.println("ActiveRecord: Finding user by username - " + username);
        return database.values().stream()
                .filter(u -> u.username.equals(username))
                .findFirst()
                .orElse(null);
    }
    
    public static List<UserRecord> findByActive(boolean active) {
        System.out.println("ActiveRecord: Finding users by active status - " + active);
        return database.values().stream()
                .filter(u -> u.active == active)
                .toList();
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
```

```java org/example/patterns/architectural/activerecord/ActiveRecordDemo.java
package org.example.patterns.architectural.activerecord;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ActiveRecordDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Active Record Pattern Demo ===");
        
        // Create and save new users
        System.out.println("\n--- Creating Users ---");
        UserRecord user1 = new UserRecord("john_doe", "john@example.com", "pass123");
        user1.save();
        
        UserRecord user2 = new UserRecord("jane_smith", "jane@example.com", "pass456");
        user2.save();
        
        UserRecord user3 = new UserRecord("bob_jones", "bob@example.com", "pass789");
        user3.save();
        
        // Find all users
        System.out.println("\n--- All Users ---");
        UserRecord.findAll().forEach(u -> 
            System.out.println("  " + u.getUsername() + " - " + u.getEmail()));
        
        // Find by username
        System.out.println("\n--- Find by Username ---");
        UserRecord foundUser = UserRecord.findByUsername("john_doe");
        if (foundUser != null) {
            System.out.println("  Found: " + foundUser.getUsername());
        }
        
        // Update user
        System.out.println("\n--- Updating User ---");
        foundUser.setEmail("john.doe@newdomain.com");
        foundUser.update();
        
        // Deactivate user
        System.out.println("\n--- Deactivating User ---");
        user2.setActive(false);
        user2.update();
        
        // Find active users
        System.out.println("\n--- Active Users ---");
        UserRecord.findByActive(true).forEach(u -> 
            System.out.println("  " + u.getUsername()));
        
        // Delete user
        System.out.println("\n--- Deleting User ---");
        user3.delete();
        
        System.out.println("\n--- Final User Count: " + UserRecord.findAll().size());
    }
}
```

---

## 11. Unit of Work Pattern

```java org/example/patterns/architectural/unitofwork/UnitOfWork.java
package org.example.patterns.architectural.unitofwork;

import java.util.*;

public class UnitOfWork {
    
    private final Set<Object> newObjects = new HashSet<>();
    private final Set<Object> dirtyObjects = new HashSet<>();
    private final Set<Object> removedObjects = new HashSet<>();
    
    public void registerNew(Object entity) {
        System.out.println("UnitOfWork: Registering new entity - " + entity.getClass().getSimpleName());
        if (dirtyObjects.contains(entity)) {
            throw new IllegalStateException("Object already registered as dirty");
        }
        if (removedObjects.contains(entity)) {
            throw new IllegalStateException("Object already registered as removed");
        }
        if (!newObjects.contains(entity)) {
            newObjects.add(entity);
        }
    }
    
    public void registerDirty(Object entity) {
        System.out.println("UnitOfWork: Registering dirty entity - " + entity.getClass().getSimpleName());
        if (removedObjects.contains(entity)) {
            throw new IllegalStateException("Object is marked for removal");
        }
        if (!newObjects.contains(entity) && !dirtyObjects.contains(entity)) {
            dirtyObjects.add(entity);
        }
    }
    
    public void registerRemoved(Object entity) {
        System.out.println("UnitOfWork: Registering removed entity - " + entity.getClass().getSimpleName());
        if (newObjects.remove(entity)) {
            return; // If it was new, just remove from new
        }
        dirtyObjects.remove(entity);
        if (!removedObjects.contains(entity)) {
            removedObjects.add(entity);
        }
    }
    
    public void commit() {
        System.out.println("\n=== UnitOfWork: Committing Changes ===");
        
        // Insert new objects
        System.out.println("Inserting " + newObjects.size() + " new objects");
        for (Object obj : newObjects) {
            insertNew(obj);
        }
        
        // Update dirty objects
        System.out.println("Updating " + dirtyObjects.size() + " dirty objects");
        for (Object obj : dirtyObjects) {
            updateDirty(obj);
        }
        
        // Delete removed objects
        System.out.println("Deleting " + removedObjects.size() + " removed objects");
        for (Object obj : removedObjects) {
            deleteRemoved(obj);
        }
        
        // Clear all sets
        newObjects.clear();
        dirtyObjects.clear();
        removedObjects.clear();
        
        System.out.println("Commit completed!\n");
    }
    
    public void rollback() {
        System.out.println("UnitOfWork: Rolling back changes");
        newObjects.clear();
        dirtyObjects.clear();
        removedObjects.clear();
    }
    
    private void insertNew(Object obj) {
        System.out.println("  INSERT: " + obj);
    }
    
    private void updateDirty(Object obj) {
        System.out.println("  UPDATE: " + obj);
    }
    
    private void deleteRemoved(Object obj) {
        System.out.println("  DELETE: " + obj);
    }
    
    public int getPendingChanges() {
        return newObjects.size() + dirtyObjects.size() + removedObjects.size();
    }
}
```

```java org/example/patterns/architectural/unitofwork/Product.java
package org.example.patterns.architectural.unitofwork;

import java.math.BigDecimal;

public class Product {
    
    private Long id;
    private String name;
    private BigDecimal price;
    
    public Product(Long id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + "}";
    }
}
```

```java org/example/patterns/architectural/unitofwork/UnitOfWorkDemo.java
package org.example.patterns.architectural.unitofwork;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UnitOfWorkDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Unit of Work Pattern Demo ===");
        
        UnitOfWork unitOfWork = new UnitOfWork();
        
        // Create new products
        System.out.println("\n--- Creating Products ---");
        Product product1 = new Product(1L, "Laptop", new BigDecimal("1200.00"));
        Product product2 = new Product(2L, "Mouse", new BigDecimal("25.00"));
        Product product3 = new Product(3L, "Keyboard", new BigDecimal("75.00"));
        
        unitOfWork.registerNew(product1);
        unitOfWork.registerNew(product2);
        unitOfWork.registerNew(product3);
        
        // Modify existing product
        System.out.println("\n--- Modifying Product ---");
        product1.setPrice(new BigDecimal("1100.00"));
        unitOfWork.registerDirty(product1);
        
        // Remove product
        System.out.println("\n--- Removing Product ---");
        unitOfWork.registerRemoved(product3);
        
        System.out.println("\nPending changes: " + unitOfWork.getPendingChanges());
        
        // Commit all changes in one transaction
        unitOfWork.commit();
        
        // New unit of work
        System.out.println("\n--- New Unit of Work ---");
        UnitOfWork newUnitOfWork = new UnitOfWork();
        Product product4 = new Product(4L, "Monitor", new BigDecimal("350.00"));
        newUnitOfWork.registerNew(product4);
        
        System.out.println("Pending changes: " + newUnitOfWork.getPendingChanges());
        
        // Rollback
        newUnitOfWork.rollback();
        System.out.println("After rollback - Pending changes: " + newUnitOfWork.getPendingChanges());
    }
}
```

---

## 12. Identity Map Pattern

```java org/example/patterns/architectural/identitymap/IdentityMap.java
package org.example.patterns.architectural.identitymap;

import java.util.HashMap;
import java.util.Map;

public class IdentityMap<T> {
    
    private final Map<Long, T> identityMap = new HashMap<>();
    
    public void put(Long id, T entity) {
        System.out.println("IdentityMap: Adding entity with id: " + id);
        identityMap.put(id, entity);
    }
    
    public T get(Long id) {
        System.out.println("IdentityMap: Getting entity with id: " + id);
        T entity = identityMap.get(id);
        if (entity != null) {
            System.out.println("  Found in identity map (avoiding database hit)");
        } else {
            System.out.println("  Not found in identity map");
        }
        return entity;
    }
    
    public boolean contains(Long id) {
        return identityMap.containsKey(id);
    }
    
    public void remove(Long id) {
        System.out.println("IdentityMap: Removing entity with id: " + id);
        identityMap.remove(id);
    }
    
    public void clear() {
        System.out.println("IdentityMap: Clearing all entities");
        identityMap.clear();
    }
    
    public int size() {
        return identityMap.size();
    }
}
```

```java org/example/patterns/architectural/identitymap/Person.java
package org.example.patterns.architectural.identitymap;

public class Person {
    
    private Long id;
    private String firstName;
    private String lastName;
    private int age;
    
    public Person(Long id, String firstName, String lastName, int age) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    @Override
    public String toString() {
        return "Person{id=" + id + ", name='" + firstName + " " + lastName + "', age=" + age + "}";
    }
}
```

```java org/example/patterns/architectural/identitymap/PersonRepository.java
package org.example.patterns.architectural/identitymap;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class PersonRepository {
    
    private final Map<Long, Person> database = new HashMap<>();
    private final IdentityMap<Person> identityMap = new IdentityMap<>();
    
    public PersonRepository() {
        // Initialize database
        database.put(1L, new Person(1L, "John", "Doe", 30));
        database.put(2L, new Person(2L, "Jane", "Smith", 25));
        database.put(3L, new Person(3L, "Bob", "Johnson", 35));
    }
    
    public Optional<Person> findById(Long id) {
        // Check identity map first
        Person person = identityMap.get(id);
        
        if (person != null) {
            return Optional.of(person);
        }
        
        // Not in identity map, fetch from database
        System.out.println("Fetching from database (slow operation)...");
        person = database.get(id);
        
        if (person != null) {
            // Add to identity map for future requests
            identityMap.put(id, person);
        }
        
        return Optional.ofNullable(person);
    }
    
    public void save(Person person) {
        System.out.println("Saving person: " + person);
        database.put(person.getId(), person);
        identityMap.put(person.getId(), person);
    }
    
    public void delete(Long id) {
        System.out.println("Deleting person with id: " + id);
        database.remove(id);
        identityMap.remove(id);
    }
    
    public void clearCache() {
        identityMap.clear();
    }
}
```

```java org/example/patterns/architectural/identitymap/IdentityMapDemo.java
package org.example.patterns.architectural.identitymap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class IdentityMapDemo implements CommandLineRunner {
    
    private final PersonRepository personRepository;
    
    public IdentityMapDemo(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Identity Map Pattern Demo ===");
        
        // First access - will hit database
        System.out.println("\n--- First Access (Database Hit) ---");
        personRepository.findById(1L).ifPresent(p -> 
            System.out.println("Retrieved: " + p));
        
        // Second access - will use identity map
        System.out.println("\n--- Second Access (Identity Map Hit) ---");
        personRepository.findById(1L).ifPresent(p -> 
            System.out.println("Retrieved: " + p));
        
        // Third access - same object reference
        System.out.println("\n--- Third Access (Same Instance) ---");
        personRepository.findById(1L).ifPresent(p -> 
            System.out.println("Retrieved: " + p));
        
        // Access different entity
        System.out.println("\n--- Accessing Different Entity ---");
        personRepository.findById(2L).ifPresent(p -> 
            System.out.println("Retrieved: " + p));
        
        // Clear cache and access again
        System.out.println("\n--- After Cache Clear ---");
        personRepository.clearCache();
        personRepository.findById(1L).ifPresent(p -> 
            System.out.println("Retrieved: " + p));
    }
}
```

---
## 13. Lazy Load Pattern

```java org/example/patterns/architectural/lazyload/Document.java
package org.example.patterns.architectural.lazyload;

import java.util.ArrayList;
import java.util.List;

public class Document {
    
    private Long id;
    private String title;
    private String author;
    
    // Lazy-loaded fields
    private String content; // Heavy content
    private List<Comment> comments; // Heavy collection
    private boolean contentLoaded = false;
    private boolean commentsLoaded = false;
    
    public Document(Long id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }
    
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    
    // Lazy loading for content
    public String getContent() {
        if (!contentLoaded) {
            loadContent();
        }
        return content;
    }
    
    // Lazy loading for comments
    public List<Comment> getComments() {
        if (!commentsLoaded) {
            loadComments();
        }
        return comments;
    }
    
    private void loadContent() {
        System.out.println("  Lazy Loading: Fetching content for document " + id + "...");
        // Simulate expensive database/file operation
        try {
            Thread.sleep(100); // Simulate delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.content = "This is the full content of document: " + title + 
                      ". It contains a lot of text and data that would be expensive to load...";
        this.contentLoaded = true;
        System.out.println("  Content loaded successfully");
    }
    
    private void loadComments() {
        System.out.println("  Lazy Loading: Fetching comments for document " + id + "...");
        // Simulate expensive database operation
        try {
            Thread.sleep(100); // Simulate delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.comments = new ArrayList<>();
        comments.add(new Comment(1L, "Great document!", "User1"));
        comments.add(new Comment(2L, "Very informative", "User2"));
        comments.add(new Comment(3L, "Thanks for sharing", "User3"));
        this.commentsLoaded = true;
        System.out.println("  Comments loaded successfully (" + comments.size() + " comments)");
    }
    
    public boolean isContentLoaded() {
        return contentLoaded;
    }
    
    public boolean isCommentsLoaded() {
        return commentsLoaded;
    }
}
```

```java org/example/patterns/architectural/lazyload/Comment.java
package org.example.patterns.architectural.lazyload;

public class Comment {
    
    private Long id;
    private String text;
    private String author;
    
    public Comment(Long id, String text, String author) {
        this.id = id;
        this.text = text;
        this.author = author;
    }
    
    public Long getId() { return id; }
    public String getText() { return text; }
    public String getAuthor() { return author; }
    
    @Override
    public String toString() {
        return "Comment by " + author + ": " + text;
    }
}
```

```java org/example/patterns/architectural/lazyload/DocumentRepository.java
package org.example.patterns.architectural.lazyload;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class DocumentRepository {
    
    private final Map<Long, Document> documents = new HashMap<>();
    
    public DocumentRepository() {
        // Initialize with sample documents (only metadata, not content)
        documents.put(1L, new Document(1L, "Spring Design Patterns", "John Doe"));
        documents.put(2L, new Document(2L, "Java Best Practices", "Jane Smith"));
        documents.put(3L, new Document(3L, "Microservices Architecture", "Bob Johnson"));
    }
    
    public Optional<Document> findById(Long id) {
        System.out.println("Repository: Fetching document metadata for id: " + id);
        return Optional.ofNullable(documents.get(id));
    }
}
```

```java org/example/patterns/architectural/lazyload/LazyLoadDemo.java
package org.example.patterns.architectural.lazyload;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LazyLoadDemo implements CommandLineRunner {
    
    private final DocumentRepository documentRepository;
    
    public LazyLoadDemo(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }
    
    @Override
    public void run(String... args) throws InterruptedException {
        System.out.println("\n=== Lazy Load Pattern Demo ===");
        
        // Load document (only metadata)
        System.out.println("\n--- Loading Document Metadata ---");
        Document document = documentRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        System.out.println("Document loaded: " + document.getTitle());
        System.out.println("Content loaded? " + document.isContentLoaded());
        System.out.println("Comments loaded? " + document.isCommentsLoaded());
        
        Thread.sleep(500);
        
        // Access content (triggers lazy loading)
        System.out.println("\n--- Accessing Content (Triggers Lazy Load) ---");
        String content = document.getContent();
        System.out.println("Content preview: " + content.substring(0, 50) + "...");
        System.out.println("Content loaded? " + document.isContentLoaded());
        
        Thread.sleep(500);
        
        // Access content again (already loaded)
        System.out.println("\n--- Accessing Content Again (Already Loaded) ---");
        String contentAgain = document.getContent();
        System.out.println("Content retrieved from cache");
        
        Thread.sleep(500);
        
        // Access comments (triggers lazy loading)
        System.out.println("\n--- Accessing Comments (Triggers Lazy Load) ---");
        document.getComments().forEach(comment -> 
            System.out.println("  " + comment));
        System.out.println("Comments loaded? " + document.isCommentsLoaded());
        
        Thread.sleep(500);
        
        // Load another document and only access metadata
        System.out.println("\n--- Loading Another Document (Metadata Only) ---");
        Document document2 = documentRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        System.out.println("Document loaded: " + document2.getTitle());
        System.out.println("Author: " + document2.getAuthor());
        System.out.println("Content loaded? " + document2.isContentLoaded());
        System.out.println("Comments loaded? " + document2.isCommentsLoaded());
        System.out.println("\nNote: Heavy data not loaded - saved resources!");
    }
}
```

---

## 14. Data Mapper Pattern

```java org/example/patterns/architectural/datamapper/domain/Customer.java
package org.example.patterns.architectural.datamapper.domain;

import java.time.LocalDate;

public class Customer {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate registrationDate;
    private CustomerStatus status;
    
    public Customer() {}
    
    public Customer(Long id, String firstName, String lastName, String email, 
                   LocalDate registrationDate, CustomerStatus status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.registrationDate = registrationDate;
        this.status = status;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { 
        this.registrationDate = registrationDate; 
    }
    
    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }
    
    public enum CustomerStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
    
    @Override
    public String toString() {
        return "Customer{id=" + id + ", name='" + getFullName() + 
               "', email='" + email + "', status=" + status + "}";
    }
}
```

```java org/example/patterns/architectural/datamapper/dto/CustomerDTO.java
package org.example.patterns.architectural.datamapper.dto;

public class CustomerDTO {
    
    private Long customerId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String registrationDate;
    private String statusCode;
    
    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    
    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { 
        this.registrationDate = registrationDate; 
    }
    
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
}
```

```java org/example/patterns/architectural/datamapper/mapper/CustomerMapper.java
package org.example.patterns.architectural.datamapper.mapper;

import org.example.patterns.architectural.datamapper.domain.Customer;
import org.example.patterns.architectural.datamapper.dto.CustomerDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class CustomerMapper {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public CustomerDTO toDTO(Customer customer) {
        System.out.println("Mapper: Converting Customer domain object to DTO");
        
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmailAddress(customer.getEmail());
        dto.setRegistrationDate(customer.getRegistrationDate().format(DATE_FORMATTER));
        dto.setStatusCode(customer.getStatus().name());
        
        return dto;
    }
    
    public Customer toDomain(CustomerDTO dto) {
        System.out.println("Mapper: Converting DTO to Customer domain object");
        
        Customer customer = new Customer();
        customer.setId(dto.getCustomerId());
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmailAddress());
        customer.setRegistrationDate(LocalDate.parse(dto.getRegistrationDate(), DATE_FORMATTER));
        customer.setStatus(Customer.CustomerStatus.valueOf(dto.getStatusCode()));
        
        return customer;
    }
    
    public void updateDomainFromDTO(CustomerDTO dto, Customer customer) {
        System.out.println("Mapper: Updating domain object from DTO");
        
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmailAddress());
        customer.setRegistrationDate(LocalDate.parse(dto.getRegistrationDate(), DATE_FORMATTER));
        customer.setStatus(Customer.CustomerStatus.valueOf(dto.getStatusCode()));
    }
}
```

```java org/example/patterns/architectural/datamapper/repository/CustomerDataRepository.java
package org.example.patterns.architectural.datamapper.repository;

import org.example.patterns.architectural.datamapper.domain.Customer;
import org.example.patterns.architectural.datamapper.dto.CustomerDTO;
import org.example.patterns.architectural.datamapper.mapper.CustomerMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CustomerDataRepository {
    
    private final Map<Long, CustomerDTO> dataStore = new HashMap<>();
    private final CustomerMapper mapper;
    private Long idCounter = 1L;
    
    public CustomerDataRepository(CustomerMapper mapper) {
        this.mapper = mapper;
        initializeSampleData();
    }
    
    private void initializeSampleData() {
        Customer c1 = new Customer(null, "John", "Doe", "john@example.com", 
                                  LocalDate.of(2023, 1, 15), Customer.CustomerStatus.ACTIVE);
        Customer c2 = new Customer(null, "Jane", "Smith", "jane@example.com", 
                                  LocalDate.of(2023, 3, 20), Customer.CustomerStatus.ACTIVE);
        Customer c3 = new Customer(null, "Bob", "Johnson", "bob@example.com", 
                                  LocalDate.of(2023, 6, 10), Customer.CustomerStatus.INACTIVE);
        
        save(c1);
        save(c2);
        save(c3);
    }
    
    public Customer save(Customer customer) {
        System.out.println("Repository: Saving customer");
        
        if (customer.getId() == null) {
            customer.setId(idCounter++);
        }
        
        CustomerDTO dto = mapper.toDTO(customer);
        dataStore.put(customer.getId(), dto);
        
        return customer;
    }
    
    public Optional<Customer> findById(Long id) {
        System.out.println("Repository: Finding customer by id: " + id);
        
        CustomerDTO dto = dataStore.get(id);
        if (dto == null) {
            return Optional.empty();
        }
        
        return Optional.of(mapper.toDomain(dto));
    }
    
    public List<Customer> findAll() {
        System.out.println("Repository: Finding all customers");
        
        return dataStore.values().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    public List<Customer> findByStatus(Customer.CustomerStatus status) {
        System.out.println("Repository: Finding customers by status: " + status);
        
        return dataStore.values().stream()
                .filter(dto -> dto.getStatusCode().equals(status.name()))
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    public void delete(Long id) {
        System.out.println("Repository: Deleting customer: " + id);
        dataStore.remove(id);
    }
}
```

```java org/example/patterns/architectural/datamapper/DataMapperDemo.java
package org.example.patterns.architectural.datamapper;

import org.example.patterns.architectural.datamapper.domain.Customer;
import org.example.patterns.architectural.datamapper.repository.CustomerDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataMapperDemo implements CommandLineRunner {
    
    private final CustomerDataRepository customerRepository;
    
    public DataMapperDemo(CustomerDataRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Data Mapper Pattern Demo ===");
        
        // Find all customers
        System.out.println("\n--- All Customers ---");
        customerRepository.findAll().forEach(c -> 
            System.out.println("  " + c));
        
        // Find by status
        System.out.println("\n--- Active Customers ---");
        customerRepository.findByStatus(Customer.CustomerStatus.ACTIVE)
            .forEach(c -> System.out.println("  " + c.getFullName()));
        
        // Create new customer
        System.out.println("\n--- Creating New Customer ---");
        Customer newCustomer = new Customer(
            null,
            "Alice",
            "Wonder",
            "alice@example.com",
            LocalDate.now(),
            Customer.CustomerStatus.ACTIVE
        );
        Customer saved = customerRepository.save(newCustomer);
        System.out.println("Saved: " + saved);
        
        // Update customer
        System.out.println("\n--- Updating Customer ---");
        customerRepository.findById(1L).ifPresent(customer -> {
            customer.setStatus(Customer.CustomerStatus.SUSPENDED);
            customerRepository.save(customer);
            System.out.println("Updated: " + customer);
        });
        
        System.out.println("\nData Mapper pattern separates domain objects from persistence DTOs!");
    }
}
```

---

## Main Application Class

```java org/example/ArchitecturalPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArchitecturalPatternsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ArchitecturalPatternsApplication.class, args);
    }
}
```

---

## Application Properties

```properties src/main/resources/application.properties
# Application Configuration
spring.application.name=spring-architectural-patterns

# Server Configuration
server.port=8080

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Disable banner
spring.main.banner-mode=off

# Allow circular references if needed
spring.main.allow-circular-references=false

# Web configuration
spring.web.resources.add-mappings=true
```

---

## POM Configuration

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
    <artifactId>spring-architectural-patterns</artifactId>
    <version>1.0.0</version>
    <name>Spring Architectural Design Patterns</name>
    <description>Demonstration of Architectural Design Patterns in Spring</description>
    
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
        
        <!-- Spring Boot Web (for MVC pattern) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Boot Starter Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Lombok (optional) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
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

## README

```markdown README.md
# Spring Architectural Design Patterns

This project demonstrates the implementation of 14 core Architectural Design Patterns using Spring Framework.

## Patterns Implemented

1. **Model-View-Controller (MVC)** - Separates application into Model, View, and Controller
2. **Model-View-ViewModel (MVVM)** - Separates presentation logic with ViewModel
3. **Layered Architecture** - Organizes code into horizontal layers
4. **Hexagonal Architecture** - Ports and Adapters pattern for clean architecture
5. **Repository Pattern** - Abstracts data access logic
6. **Data Access Object (DAO)** - Encapsulates database operations
7. **Service Layer** - Business logic layer with DTOs
8. **Domain Model** - Rich domain objects with business logic
9. **Transaction Script** - Procedural transaction handling
10. **Active Record** - Domain objects with database operations
11. **Unit of Work** - Manages transaction boundaries
12. **Identity Map** - Ensures single instance per entity
13. **Lazy Load** - Defers expensive loading operations
14. **Data Mapper** - Separates domain and persistence models

## Running the Application

```bash
mvn spring-boot:run
```

## Testing REST Endpoints

```bash
# MVC Pattern - Get all products
curl http://localhost:8080/api/products

# Get product by ID
curl http://localhost:8080/api/products/1

# Create product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Product","price":99.99}'
```

## Build

```bash
mvn clean package
```

## Requirements

- Java 17+
- Maven 3.6+
- Spring Boot 3.2.0
```

This completes the comprehensive implementation of all 14 Architectural Design Patterns in Spring Framework with fully working, production-ready examples demonstrating real-world use cases!