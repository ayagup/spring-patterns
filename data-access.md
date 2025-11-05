# Spring Data Access Patterns - Java Implementations

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
    <artifactId>spring-data-access-patterns</artifactId>
    <version>1.0.0</version>
    <name>Spring Data Access Patterns</name>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
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
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Test -->
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

## 1. Repository Pattern

```java org/example/patterns/dataaccess/repository/entity/Product.java
package org.example.patterns.dataaccess.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer stock;
    
    private String category;
    
    @Version
    private Long version;
    
    public Product(String name, String description, BigDecimal price, Integer stock, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }
}
```

```java org/example/patterns/dataaccess/repository/ProductRepository.java
package org.example.patterns.dataaccess.repository;

import org.example.patterns.dataaccess.repository.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Query methods
    List<Product> findByCategory(String category);
    
    List<Product> findByPriceLessThan(BigDecimal price);
    
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.stock < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    Optional<Product> findByName(String name);
    
    boolean existsByName(String name);
}
```

```java org/example/patterns/dataaccess/repository/RepositoryPatternDemo.java
package org.example.patterns.dataaccess.repository;

import org.example.patterns.dataaccess.repository.entity.Product;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(1)
public class RepositoryPatternDemo implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    
    public RepositoryPatternDemo(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Repository Pattern Demo ===");
        
        // Create products
        productRepository.save(new Product("Laptop", "High-performance laptop", 
                                          new BigDecimal("1200.00"), 10, "Electronics"));
        productRepository.save(new Product("Mouse", "Wireless mouse", 
                                          new BigDecimal("25.00"), 50, "Electronics"));
        productRepository.save(new Product("Desk", "Standing desk", 
                                          new BigDecimal("350.00"), 5, "Furniture"));
        
        // Query examples
        System.out.println("\n--- All Products ---");
        productRepository.findAll().forEach(p -> 
            System.out.println("  " + p.getName() + ": $" + p.getPrice()));
        
        System.out.println("\n--- Electronics Category ---");
        productRepository.findByCategory("Electronics").forEach(p -> 
            System.out.println("  " + p.getName()));
        
        System.out.println("\n--- Low Stock Products (< 10) ---");
        productRepository.findLowStockProducts(10).forEach(p -> 
            System.out.println("  " + p.getName() + ": Stock = " + p.getStock()));
        
        System.out.println("\n--- Products Under $100 ---");
        productRepository.findByPriceLessThan(new BigDecimal("100.00")).forEach(p -> 
            System.out.println("  " + p.getName() + ": $" + p.getPrice()));
    }
}
```

---

## 2. DAO Pattern

```java org/example/patterns/dataaccess/dao/model/Customer.java
package org.example.patterns.dataaccess.dao.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String phone;
    
    private LocalDate registrationDate;
    
    @Enumerated(EnumType.STRING)
    private CustomerStatus status;
    
    public Customer(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.registrationDate = LocalDate.now();
        this.status = CustomerStatus.ACTIVE;
    }
    
    public enum CustomerStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
```

```java org/example/patterns/dataaccess/dao/CustomerDAO.java
package org.example.patterns.dataaccess.dao;

import org.example.patterns.dataaccess.dao.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDAO {
    Customer create(Customer customer);
    Optional<Customer> findById(Long id);
    List<Customer> findAll();
    Customer update(Customer customer);
    void delete(Long id);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByStatus(Customer.CustomerStatus status);
    long count();
}
```

```java org/example/patterns/dataaccess/dao/CustomerDAOImpl.java
package org.example.patterns.dataaccess.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.patterns.dataaccess.dao.model.Customer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CustomerDAOImpl implements CustomerDAO {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Customer create(Customer customer) {
        System.out.println("DAO: Creating customer - " + customer.getEmail());
        entityManager.persist(customer);
        return customer;
    }
    
    @Override
    public Optional<Customer> findById(Long id) {
        System.out.println("DAO: Finding customer by ID - " + id);
        Customer customer = entityManager.find(Customer.class, id);
        return Optional.ofNullable(customer);
    }
    
    @Override
    public List<Customer> findAll() {
        System.out.println("DAO: Finding all customers");
        TypedQuery<Customer> query = entityManager.createQuery(
            "SELECT c FROM Customer c", Customer.class);
        return query.getResultList();
    }
    
    @Override
    public Customer update(Customer customer) {
        System.out.println("DAO: Updating customer - " + customer.getId());
        return entityManager.merge(customer);
    }
    
    @Override
    public void delete(Long id) {
        System.out.println("DAO: Deleting customer - " + id);
        findById(id).ifPresent(entityManager::remove);
    }
    
    @Override
    public Optional<Customer> findByEmail(String email) {
        System.out.println("DAO: Finding customer by email - " + email);
        TypedQuery<Customer> query = entityManager.createQuery(
            "SELECT c FROM Customer c WHERE c.email = :email", Customer.class);
        query.setParameter("email", email);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Customer> findByStatus(Customer.CustomerStatus status) {
        System.out.println("DAO: Finding customers by status - " + status);
        TypedQuery<Customer> query = entityManager.createQuery(
            "SELECT c FROM Customer c WHERE c.status = :status", Customer.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    @Override
    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(c) FROM Customer c", Long.class);
        return query.getSingleResult();
    }
}
```

```java org/example/patterns/dataaccess/dao/DAOPatternDemo.java
package org.example.patterns.dataaccess.dao;

import org.example.patterns.dataaccess.dao.model.Customer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class DAOPatternDemo implements CommandLineRunner {
    
    private final CustomerDAO customerDAO;
    
    public DAOPatternDemo(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== DAO Pattern Demo ===");
        
        // Create customers
        customerDAO.create(new Customer("John", "Doe", "john@example.com", "555-1234"));
        customerDAO.create(new Customer("Jane", "Smith", "jane@example.com", "555-5678"));
        customerDAO.create(new Customer("Bob", "Johnson", "bob@example.com", "555-9012"));
        
        // Find all
        System.out.println("\n--- All Customers ---");
        customerDAO.findAll().forEach(c -> 
            System.out.println("  " + c.getFirstName() + " " + c.getLastName() + 
                             " - " + c.getEmail()));
        
        // Find by email
        System.out.println("\n--- Find by Email ---");
        customerDAO.findByEmail("john@example.com").ifPresent(c -> 
            System.out.println("  Found: " + c.getFirstName() + " " + c.getLastName()));
        
        // Find by status
        System.out.println("\n--- Active Customers ---");
        customerDAO.findByStatus(Customer.CustomerStatus.ACTIVE).forEach(c -> 
            System.out.println("  " + c.getFirstName() + " " + c.getLastName()));
        
        System.out.println("\nTotal customers: " + customerDAO.count());
    }
}
```

---

## 3. Data Transfer Object (DTO) Pattern

```java org/example/patterns/dataaccess/dto/OrderDTO.java
package org.example.patterns.dataaccess.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    
    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private String customerName;
    private String customerEmail;
    private List<OrderItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private String status;
}
```

```java org/example/patterns/dataaccess/dto/OrderItemDTO.java
package org.example.patterns.dataaccess.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
```

```java org/example/patterns/dataaccess/dto/entity/Order.java
package org.example.patterns.dataaccess.dto.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderNumber;
    
    @Column(nullable = false)
    private LocalDateTime orderDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
```

```java org/example/patterns/dataaccess/dto/entity/CustomerEntity.java
package org.example.patterns.dataaccess.dto.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dto_customers")
@Data
@NoArgsConstructor
public class CustomerEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String firstName;
    private String lastName;
    
    @Column(unique = true)
    private String email;
    
    public CustomerEntity(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
```

```java org/example/patterns/dataaccess/dto/entity/OrderItem.java
package org.example.patterns.dataaccess.dto.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
```

```java org/example/patterns/dataaccess/dto/mapper/OrderMapper.java
package org.example.patterns.dataaccess.dto.mapper;

import org.example.patterns.dataaccess.dto.OrderDTO;
import org.example.patterns.dataaccess.dto.OrderItemDTO;
import org.example.patterns.dataaccess.dto.entity.Order;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    public OrderDTO toDTO(Order order) {
        System.out.println("Mapper: Converting Order entity to DTO");
        
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setOrderDate(order.getOrderDate());
        dto.setCustomerName(order.getCustomer().getFirstName() + " " + 
                           order.getCustomer().getLastName());
        dto.setCustomerEmail(order.getCustomer().getEmail());
        
        dto.setItems(order.getItems().stream()
                .map(item -> new OrderItemDTO(
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
                ))
                .collect(Collectors.toList()));
        
        dto.setSubtotal(order.getSubtotal());
        dto.setTax(order.getTax());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus().name());
        
        return dto;
    }
}
```

```java org/example/patterns/dataaccess/dto/DTOPatternDemo.java
package org.example.patterns.dataaccess.dto;

import org.example.patterns.dataaccess.dto.entity.CustomerEntity;
import org.example.patterns.dataaccess.dto.entity.Order;
import org.example.patterns.dataaccess.dto.entity.OrderItem;
import org.example.patterns.dataaccess.dto.mapper.OrderMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
@Order(3)
public class DTOPatternDemo implements CommandLineRunner {
    
    private final OrderMapper orderMapper;
    
    public DTOPatternDemo(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== DTO Pattern Demo ===");
        
        // Create entity
        CustomerEntity customer = new CustomerEntity("John", "Doe", "john@example.com");
        
        org.example.patterns.dataaccess.dto.entity.Order order = 
            new org.example.patterns.dataaccess.dto.entity.Order();
        order.setOrderNumber("ORD-001");
        order.setOrderDate(LocalDateTime.now());
        order.setCustomer(customer);
        order.setItems(new ArrayList<>());
        
        OrderItem item1 = new OrderItem();
        item1.setProductName("Laptop");
        item1.setQuantity(1);
        item1.setUnitPrice(new BigDecimal("1200.00"));
        item1.setSubtotal(new BigDecimal("1200.00"));
        item1.setOrder(order);
        
        OrderItem item2 = new OrderItem();
        item2.setProductName("Mouse");
        item2.setQuantity(2);
        item2.setUnitPrice(new BigDecimal("25.00"));
        item2.setSubtotal(new BigDecimal("50.00"));
        item2.setOrder(order);
        
        order.getItems().add(item1);
        order.getItems().add(item2);
        order.setSubtotal(new BigDecimal("1250.00"));
        order.setTax(new BigDecimal("100.00"));
        order.setTotal(new BigDecimal("1350.00"));
        order.setStatus(org.example.patterns.dataaccess.dto.entity.Order.OrderStatus.CONFIRMED);
        
        // Convert to DTO
        OrderDTO dto = orderMapper.toDTO(order);
        
        System.out.println("\n--- Order DTO ---");
        System.out.println("Order Number: " + dto.getOrderNumber());
        System.out.println("Customer: " + dto.getCustomerName());
        System.out.println("Items: " + dto.getItems().size());
        dto.getItems().forEach(item -> 
            System.out.println("  - " + item.getProductName() + " x" + item.getQuantity() + 
                             " = $" + item.getSubtotal()));
        System.out.println("Total: $" + dto.getTotal());
        System.out.println("Status: " + dto.getStatus());
    }
}
```

---

## 4. Value Object Pattern

```java org/example/patterns/dataaccess/valueobject/Money.java
package org.example.patterns.dataaccess.valueobject;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class Money {
    
    private BigDecimal amount;
    private String currency;
    
    public Money(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }
    
    public Money add(Money other) {
        validateCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money subtract(Money other) {
        validateCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }
    
    private void validateCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot operate on different currencies");
        }
    }
    
    @Override
    public String toString() {
        return currency + " " + amount;
    }
}
```

```java org/example/patterns/dataaccess/valueobject/Address.java
package org.example.patterns.dataaccess.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    public String getFullAddress() {
        return street + ", " + city + ", " + state + " " + zipCode + ", " + country;
    }
    
    @Override
    public String toString() {
        return getFullAddress();
    }
}
```

```java org/example/patterns/dataaccess/valueobject/Email.java
package org.example.patterns.dataaccess.valueobject;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class Email {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private String value;
    
    public Email(String value) {
        if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.value = value.toLowerCase();
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

```java org/example/patterns/dataaccess/valueobject/entity/Account.java
package org.example.patterns.dataaccess.valueobject.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.patterns.dataaccess.valueobject.Address;
import org.example.patterns.dataaccess.valueobject.Email;
import org.example.patterns.dataaccess.valueobject.Money;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String accountNumber;
    
    @Embedded
    private Email email;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "balance_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "balance_currency"))
    })
    private Money balance;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "billing_street")),
        @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
        @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "billing_zip")),
        @AttributeOverride(name = "country", column = @Column(name = "billing_country"))
    })
    private Address billingAddress;
    
    public Account(String accountNumber, Email email, Money balance, Address billingAddress) {
        this.accountNumber = accountNumber;
        this.email = email;
        this.balance = balance;
        this.billingAddress = billingAddress;
    }
}
```

```java org/example/patterns/dataaccess/valueobject/ValueObjectPatternDemo.java
package org.example.patterns.dataaccess.valueobject;

import org.example.patterns.dataaccess.valueobject.entity.Account;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(4)
public class ValueObjectPatternDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Value Object Pattern Demo ===");
        
        // Create value objects
        Money balance = new Money(new BigDecimal("5000.00"), "USD");
        Email email = new Email("john.doe@example.com");
        Address address = new Address("123 Main St", "New York", "NY", "10001", "USA");
        
        // Create entity with value objects
        Account account = new Account("ACC-001", email, balance, address);
        
        System.out.println("\n--- Account Details ---");
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Email: " + account.getEmail());
        System.out.println("Balance: " + account.getBalance());
        System.out.println("Address: " + account.getBillingAddress());
        
        // Demonstrate value object operations
        System.out.println("\n--- Money Operations ---");
        Money deposit = new Money(new BigDecimal("500.00"), "USD");
        Money newBalance = balance.add(deposit);
        System.out.println("After deposit: " + newBalance);
        
        Money withdrawal = new Money(new BigDecimal("200.00"), "USD");
        newBalance = newBalance.subtract(withdrawal);
        System.out.println("After withdrawal: " + newBalance);
        
        // Value objects are immutable and compared by value
        Money amount1 = new Money(new BigDecimal("100.00"), "USD");
        Money amount2 = new Money(new BigDecimal("100.00"), "USD");
        System.out.println("\n--- Value Equality ---");
        System.out.println("amount1 == amount2: " + (amount1 == amount2)); // false (different objects)
        System.out.println("amount1.equals(amount2): " + amount1.equals(amount2)); // true (same value)
    }
}
```

---

## 5. Entity Pattern

```java org/example/patterns/dataaccess/entity/OrderEntity.java
package org.example.patterns.dataaccess.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "entity_orders")
@Data
@NoArgsConstructor
public class OrderEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderNumber;
    
    @Column(nullable = false)
    private Long customerId;
    
    @Column(nullable = false)
    private LocalDateTime orderDate;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLineItem> items = new ArrayList<>();
    
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Version
    private Long version;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Business logic in entity
    public void addItem(String productName, int quantity, BigDecimal price) {
        OrderLineItem item = new OrderLineItem();
        item.setOrder(this);
        item.setProductName(productName);
        item.setQuantity(quantity);
        item.setUnitPrice(price);
        item.setSubtotal(price.multiply(BigDecimal.valueOf(quantity)));
        
        items.add(item);
        calculateTotal();
    }
    
    public void removeItem(OrderLineItem item) {
        items.remove(item);
        item.setOrder(null);
        calculateTotal();
    }
    
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel shipped or delivered orders");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    private void calculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderLineItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @PrePersist
    protected void onCreate() {
        this.orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
```

```java org/example/patterns/dataaccess/entity/OrderLineItem.java
package org.example.patterns.dataaccess.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "entity_order_items")
@Data
@NoArgsConstructor
public class OrderLineItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
```

```java org/example/patterns/dataaccess/entity/EntityPatternDemo.java
package org.example.patterns.dataaccess.entity;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(5)
public class EntityPatternDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Entity Pattern Demo ===");
        
        // Create order entity with business logic
        OrderEntity order = new OrderEntity();
        order.setCustomerId(100L);
        
        System.out.println("\n--- Creating Order ---");
        System.out.println("Order Number: " + order.getOrderNumber());
        System.out.println("Status: " + order.getStatus());
        
        // Add items using business logic
        System.out.println("\n--- Adding Items ---");
        order.addItem("Laptop", 1, new BigDecimal("1200.00"));
        order.addItem("Mouse", 2, new BigDecimal("25.00"));
        order.addItem("Keyboard", 1, new BigDecimal("75.00"));
        
        System.out.println("Items count: " + order.getItems().size());
        System.out.println("Total amount: $" + order.getTotalAmount());
        
        // Confirm order
        System.out.println("\n--- Confirming Order ---");
        order.confirm();
        System.out.println("Status: " + order.getStatus());
        
        // Try to cancel (should fail)
        System.out.println("\n--- Attempting Operations ---");
        try {
            order.confirm(); // Should fail
        } catch (IllegalStateException e) {
            System.out.println("Cannot confirm again: " + e.getMessage());
        }
        
        System.out.println("\nEntity encapsulates both data and behavior!");
    }
}
```

---

## 6. Aggregate Pattern

```java org/example/patterns/dataaccess/aggregate/OrderAggregate.java
package org.example.patterns.dataaccess.aggregate;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "aggregate_orders")
@Data
@NoArgsConstructor
public class OrderAggregate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String orderNumber;
    
    @Embedded
    private CustomerInfo customer;
    
    @Embedded
    private ShippingAddress shippingAddress;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemAggregate> items = new ArrayList<>();
    
    @Embedded
    private OrderSummary summary;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    // Aggregate root - controls all access to child entities
    public void addItem(String productId, String productName, int quantity, BigDecimal price) {
        validateOrderIsModifiable();
        
        OrderItemAggregate item = new OrderItemAggregate();
        item.setOrder(this);
        item.setProductId(productId);
        item.setProductName(productName);
        item.setQuantity(quantity);
        item.setUnitPrice(price);
        item.calculateSubtotal();
        
        items.add(item);
        recalculateSummary();
    }
    
    public void removeItem(String productId) {
        validateOrderIsModifiable();
        
        items.removeIf(item -> item.getProductId().equals(productId));
        recalculateSummary();
    }
    
    public void updateItemQuantity(String productId, int newQuantity) {
        validateOrderIsModifiable();
        
        items.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .ifPresent(item -> {
                item.setQuantity(newQuantity);
                item.calculateSubtotal();
            });
        
        recalculateSummary();
    }
    
    public void setShippingAddress(String street, String city, String state, String zip, String country) {
        this.shippingAddress = new ShippingAddress(street, city, state, zip, country);
    }
    
    public void confirm() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot confirm order without items");
        }
        if (shippingAddress == null) {
            throw new IllegalStateException("Cannot confirm order without shipping address");
        }
        this.status = OrderStatus.CONFIRMED;
    }
    
    private void validateOrderIsModifiable() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot modify shipped or delivered orders");
        }
    }
    
    private void recalculateSummary() {
        BigDecimal subtotal = items.stream()
            .map(OrderItemAggregate::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.08"));
        BigDecimal total = subtotal.add(tax);
        
        this.summary = new OrderSummary(subtotal, tax, total);
    }
    
    @PrePersist
    protected void onCreate() {
        this.orderNumber = "AGG-" + UUID.randomUUID().toString().substring(0, 8);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
```

```java org/example/patterns/dataaccess/aggregate/CustomerInfo.java
package org.example.patterns.dataaccess.aggregate;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfo {
    private String customerId;
    private String customerName;
    private String customerEmail;
}
```

```java org/example/patterns/dataaccess/aggregate/ShippingAddress.java
package org.example.patterns.dataaccess.aggregate;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
```

```java org/example/patterns/dataaccess/aggregate/OrderSummary.java
package org.example.patterns.dataaccess.aggregate;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummary {
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
}
```

```java org/example/patterns/dataaccess/aggregate/OrderItemAggregate.java
package org.example.patterns.dataaccess.aggregate;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "aggregate_order_items")
@Data
@NoArgsConstructor
public class OrderItemAggregate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderAggregate order;
    
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    
    void calculateSubtotal() {
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

```java org/example/patterns/dataaccess/aggregate/AggregatePatternDemo.java
package org.example.patterns.dataaccess.aggregate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(6)
public class AggregatePatternDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Aggregate Pattern Demo ===");
        
        // Create aggregate root
        OrderAggregate order = new OrderAggregate();
        order.setCustomer(new CustomerInfo("CUST-001", "John Doe", "john@example.com"));
        
        System.out.println("\n--- Building Order Aggregate ---");
        System.out.println("Order Number: " + order.getOrderNumber());
        
        // All modifications go through aggregate root
        order.addItem("PROD-001", "Laptop", 1, new BigDecimal("1200.00"));
        order.addItem("PROD-002", "Mouse", 2, new BigDecimal("25.00"));
        order.addItem("PROD-003", "Keyboard", 1, new BigDecimal("75.00"));
        
        System.out.println("Items added: " + order.getItems().size());
        
        // Update quantity through aggregate root
        order.updateItemQuantity("PROD-002", 3);
        System.out.println("Updated mouse quantity to 3");
        
        // Set shipping address
        order.setShippingAddress("123 Main St", "New York", "NY", "10001", "USA");
        
        // View summary
        System.out.println("\n--- Order Summary ---");
        System.out.println("Subtotal: $" + order.getSummary().getSubtotal());
        System.out.println("Tax: $" + order.getSummary().getTax());
        System.out.println("Total: $" + order.getSummary().getTotal());
        
        // Confirm order
        order.confirm();
        System.out.println("\nOrder Status: " + order.getStatus());
        
        // Try to modify confirmed order
        try {
            order.addItem("PROD-004", "Monitor", 1, new BigDecimal("350.00"));
        } catch (IllegalStateException e) {
            System.out.println("\nCannot modify: " + e.getMessage());
        }
        
        System.out.println("\nAggregate ensures consistency of the entire object graph!");
    }
}
```

---

## 7. Specification Pattern

```java org/example/patterns/dataaccess/specification/Specification.java
package org.example.patterns.dataaccess.specification;

public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
    Specification<T> and(Specification<T> other);
    Specification<T> or(Specification<T> other);
    Specification<T> not();
}
```

```java org/example/patterns/dataaccess/specification/AbstractSpecification.java
package org.example.patterns.dataaccess.specification;

public abstract class AbstractSpecification<T> implements Specification<T> {
    
    @Override
    public Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }
    
    @Override
    public Specification<T> or(Specification<T> other) {
        return new OrSpecification<>(this, other);
    }
    
    @Override
    public Specification<T> not() {
        return new NotSpecification<>(this);
    }
}
```

```java org/example/patterns/dataaccess/specification/AndSpecification.java
package org.example.patterns.dataaccess.specification;

public class AndSpecification<T> extends AbstractSpecification<T> {
    
    private final Specification<T> left;
    private final Specification<T> right;
    
    public AndSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) && right.isSatisfiedBy(candidate);
    }
}
```

```java org/example/patterns/dataaccess/specification/OrSpecification.java
package org.example.patterns.dataaccess.specification;

public class OrSpecification<T> extends AbstractSpecification<T> {
    
    private final Specification<T> left;
    private final Specification<T> right;
    
    public OrSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate);
    }
}
```

```java org/example/patterns/dataaccess/specification/NotSpecification.java
package org.example.patterns.dataaccess.specification;

public class NotSpecification<T> extends AbstractSpecification<T> {
    
    private final Specification<T> specification;
    
    public NotSpecification(Specification<T> specification) {
        this.specification = specification;
    }
    
    @Override
    public boolean isSatisfiedBy(T candidate) {
        return !specification.isSatisfiedBy(candidate);
    }
}
```

```java org/example/patterns/dataaccess/specification/model/ProductSpec.java
package org.example.patterns.dataaccess.specification.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductSpec {
    private String name;
    private BigDecimal price;
    private String category;
    private int stock;
    private boolean active;
}
```

```java org/example/patterns/dataaccess/specification/PriceSpecification.java
package org.example.patterns.dataaccess.specification;

import org.example.patterns.dataaccess.specification.model.ProductSpec;

import java.math.BigDecimal;

public class PriceSpecification extends AbstractSpecification<ProductSpec> {
    
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    
    public PriceSpecification(BigDecimal minPrice, BigDecimal maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
    
    @Override
    public boolean isSatisfiedBy(ProductSpec product) {
        return product.getPrice().compareTo(minPrice) >= 0 && 
               product.getPrice().compareTo(maxPrice) <= 0;
    }
}
```

```java org/example/patterns/dataaccess/specification/CategorySpecification.java
package org.example.patterns.dataaccess.specification;

import org.example.patterns.dataaccess.specification.model.ProductSpec;

public class CategorySpecification extends AbstractSpecification<ProductSpec> {
    
    private final String category;
    
    public CategorySpecification(String category) {
        this.category = category;
    }
    
    @Override
    public boolean isSatisfiedBy(ProductSpec product) {
        return product.getCategory().equalsIgnoreCase(category);
    }
}
```

```java org/example/patterns/dataaccess/specification/InStockSpecification.java
package org.example.patterns.dataaccess.specification;

import org.example.patterns.dataaccess.specification.model.ProductSpec;

public class InStockSpecification extends AbstractSpecification<ProductSpec> {
    
    @Override
    public boolean isSatisfiedBy(ProductSpec product) {
        return product.getStock() > 0 && product.isActive();
    }
}
```

```java org/example/patterns/dataaccess/specification/SpecificationPatternDemo.java
package org.example.patterns.dataaccess.specification;

import org.example.patterns.dataaccess.specification.model.ProductSpec;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@Order(7)
public class SpecificationPatternDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Specification Pattern Demo ===");
        
        // Create products
        List<ProductSpec> products = Arrays.asList(
            new ProductSpec("Laptop", new BigDecimal("1200.00"), "Electronics", 10, true),
            new ProductSpec("Mouse", new BigDecimal("25.00"), "Electronics", 50, true),
            new ProductSpec("Desk", new BigDecimal("350.00"), "Furniture", 0, true),
            new ProductSpec("Chair", new BigDecimal("200.00"), "Furniture", 15, true),
            new ProductSpec("Monitor", new BigDecimal("400.00"), "Electronics", 5, false)
        );
        
        // Create specifications
        Specification<ProductSpec> priceSpec = 
            new PriceSpecification(new BigDecimal("0"), new BigDecimal("500"));
        Specification<ProductSpec> electronicsSpec = 
            new CategorySpecification("Electronics");
        Specification<ProductSpec> inStockSpec = 
            new InStockSpecification();
        
        // Combine specifications
        Specification<ProductSpec> affordableElectronicsInStock = 
            priceSpec.and(electronicsSpec).and(inStockSpec);
        
        System.out.println("\n--- Affordable Electronics In Stock (< $500) ---");
        products.stream()
            .filter(affordableElectronicsInStock::isSatisfiedBy)
            .forEach(p -> System.out.println("  " + p.getName() + " - $" + p.getPrice()));
        
        // Another combination
        Specification<ProductSpec> furnitureOrExpensive = 
            new CategorySpecification("Furniture")
                .or(new PriceSpecification(new BigDecimal("500"), new BigDecimal("10000")));
        
        System.out.println("\n--- Furniture OR Expensive Products (> $500) ---");
        products.stream()
            .filter(furnitureOrExpensive::isSatisfiedBy)
            .forEach(p -> System.out.println("  " + p.getName() + " - " + p.getCategory()));
        
        // Negation
        Specification<ProductSpec> notElectronics = electronicsSpec.not();
        
        System.out.println("\n--- Non-Electronics ---");
        products.stream()
            .filter(notElectronics::isSatisfiedBy)
            .forEach(p -> System.out.println("  " + p.getName()));
    }
}
```

---

## 8. Query Object Pattern

```java org/example/patterns/dataaccess/queryobject/QueryObject.java
package org.example.patterns.dataaccess.queryobject;

import java.util.ArrayList;
import java.util.List;

public class QueryObject<T> {
    
    private final Class<T> entityClass;
    private final List<Criterion> criteria = new ArrayList<>();
    private final List<OrderBy> orderBys = new ArrayList<>();
    private Integer limit;
    private Integer offset;
    
    public QueryObject(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    public QueryObject<T> where(String field, Operator operator, Object value) {
        criteria.add(new Criterion(field, operator, value));
        return this;
    }
    
    public QueryObject<T> orderBy(String field, Direction direction) {
        orderBys.add(new OrderBy(field, direction));
        return this;
    }
    
    public QueryObject<T> limit(int limit) {
        this.limit = limit;
        return this;
    }
    
    public QueryObject<T> offset(int offset) {
        this.offset = offset;
        return this;
    }
    
    public String toSQL() {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(entityClass.getSimpleName().toLowerCase()).append("s");
        
        if (!criteria.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < criteria.size(); i++) {
                if (i > 0) sql.append(" AND ");
                Criterion criterion = criteria.get(i);
                sql.append(criterion.field).append(" ").append(criterion.operator.symbol)
                   .append(" ?");
            }
        }
        
        if (!orderBys.isEmpty()) {
            sql.append(" ORDER BY ");
            for (int i = 0; i < orderBys.size(); i++) {
                if (i > 0) sql.append(", ");
                OrderBy orderBy = orderBys.get(i);
                sql.append(orderBy.field).append(" ").append(orderBy.direction);
            }
        }
        
        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }
        
        if (offset != null) {
            sql.append(" OFFSET ").append(offset);
        }
        
        return sql.toString();
    }
    
    public List<Criterion> getCriteria() {
        return new ArrayList<>(criteria);
    }
    
    public static class Criterion {
        private final String field;
        private final Operator operator;
        private final Object value;
        
        public Criterion(String field, Operator operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
        
        public String getField() { return field; }
        public Operator getOperator() { return operator; }
        public Object getValue() { return value; }
    }
    
    public static class OrderBy {
        private final String field;
        private final Direction direction;
        
        public OrderBy(String field, Direction direction) {
            this.field = field;
            this.direction = direction;
        }
    }
    
    public enum Operator {
        EQUALS("="), NOT_EQUALS("!="), GREATER_THAN(">"), LESS_THAN("<"),
        GREATER_THAN_OR_EQUALS(">="), LESS_THAN_OR_EQUALS("<="), LIKE("LIKE");
        
        private final String symbol;
        
        Operator(String symbol) {
            this.symbol = symbol;
        }
    }
    
    public enum Direction {
        ASC, DESC
    }
}
```

```java org/example/patterns/dataaccess/queryobject/ProductQuery.java
package org.example.patterns.dataaccess.queryobject;

import org.example.patterns.dataaccess.repository.entity.Product;

import java.math.BigDecimal;

public class ProductQuery {
    
    public static QueryObject<Product> findElectronicsUnder500() {
        return new QueryObject<>(Product.class)
            .where("category", QueryObject.Operator.EQUALS, "Electronics")
            .where("price", QueryObject.Operator.LESS_THAN, new BigDecimal("500"))
            .orderBy("price", QueryObject.Direction.ASC);
    }
    
    public static QueryObject<Product> findLowStock(int threshold) {
        return new QueryObject<>(Product.class)
            .where("stock", QueryObject.Operator.LESS_THAN, threshold)
            .orderBy("stock", QueryObject.Direction.ASC)
            .limit(10);
    }
    
    public static QueryObject<Product> findByPriceRange(BigDecimal min, BigDecimal max) {
        return new QueryObject<>(Product.class)
            .where("price", QueryObject.Operator.GREATER_THAN_OR_EQUALS, min)
            .where("price", QueryObject.Operator.LESS_THAN_OR_EQUALS, max)
            .orderBy("price", QueryObject.Direction.DESC);
    }
}
```

```java org/example/patterns/dataaccess/queryobject/QueryObjectPatternDemo.java
package org.example.patterns.dataaccess.queryobject;

import org.example.patterns.dataaccess.repository.entity.Product;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(8)
public class QueryObjectPatternDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Query Object Pattern Demo ===");
        
        // Create query objects
        System.out.println("\n--- Electronics Under $500 Query ---");
        QueryObject<Product> query1 = ProductQuery.findElectronicsUnder500();
        System.out.println("SQL: " + query1.toSQL());
        
        System.out.println("\n--- Low Stock Products Query ---");
        QueryObject<Product> query2 = ProductQuery.findLowStock(10);
        System.out.println("SQL: " + query2.toSQL());
        
        System.out.println("\n--- Price Range Query ---");
        QueryObject<Product> query3 = ProductQuery.findByPriceRange(
            new BigDecimal("100"), new BigDecimal("1000"));
        System.out.println("SQL: " + query3.toSQL());
        
        // Complex query
        System.out.println("\n--- Complex Custom Query ---");
        QueryObject<Product> customQuery = new QueryObject<>(Product.class)
            .where("category", QueryObject.Operator.EQUALS, "Electronics")
            .where("price", QueryObject.Operator.GREATER_THAN, new BigDecimal("50"))
            .where("stock", QueryObject.Operator.GREATER_THAN, 0)
            .orderBy("price", QueryObject.Direction.ASC)
            .orderBy("name", QueryObject.Direction.ASC)
            .limit(20)
            .offset(0);
        
        System.out.println("SQL: " + customQuery.toSQL());
        System.out.println("\nQuery Object encapsulates complex query logic!");
    }
}
```

---

## 9. CQRS Pattern

```java org/example/patterns/dataaccess/cqrs/command/CreateProductCommand.java
package org.example.patterns.dataaccess.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CreateProductCommand {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
}
```

```java org/example/patterns/dataaccess/cqrs/command/UpdateProductPriceCommand.java
package org.example.patterns.dataaccess.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UpdateProductPriceCommand {
    private Long productId;
    private BigDecimal newPrice;
}
```

```java org/example/patterns/dataaccess/cqrs/command/CommandHandler.java
package org.example.patterns.dataaccess.cqrs.command;

public interface CommandHandler<T> {
    void handle(T command);
}
```

```java org/example/patterns/dataaccess/cqrs/command/CreateProductCommandHandler.java
package org.example.patterns.dataaccess.cqrs.command;

import org.example.patterns.dataaccess.repository.ProductRepository;
import org.example.patterns.dataaccess.repository.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateProductCommandHandler implements CommandHandler<CreateProductCommand> {
    
    private final ProductRepository productRepository;
    
    public CreateProductCommandHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    public void handle(CreateProductCommand command) {
        System.out.println("Command Handler: Creating product - " + command.getName());
        
        Product product = new Product(
            command.getName(),
            command.getDescription(),
            command.getPrice(),
            command.getStock(),
            command.getCategory()
        );
        
        productRepository.save(product);
        System.out.println("  Product created with ID: " + product.getId());
    }
}
```

```java org/example/patterns/dataaccess/cqrs/command/UpdateProductPriceCommandHandler.java
package org.example.patterns.dataaccess.cqrs.command;

import org.example.patterns.dataaccess.repository.ProductRepository;
import org.example.patterns.dataaccess.repository.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateProductPriceCommandHandler implements CommandHandler<UpdateProductPriceCommand> {
    
    private final ProductRepository productRepository;
    
    public UpdateProductPriceCommandHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    public void handle(UpdateProductPriceCommand command) {
        System.out.println("Command Handler: Updating price for product " + command.getProductId());
        
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setPrice(command.getNewPrice());
        productRepository.save(product);
        
        System.out.println("  Price updated to: $" + command.getNewPrice());
    }
}
```

```java org/example/patterns/dataaccess/cqrs/query/ProductQueryDTO.java
package org.example.patterns.dataaccess.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductQueryDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String category;
    private Integer stock;
}
```

```java org/example/patterns/dataaccess/cqrs/query/ProductQueryService.java
package org.example.patterns.dataaccess.cqrs.query;

import org.example.patterns.dataaccess.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductQueryService {
    
    private final ProductRepository productRepository;
    
    public ProductQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public List<ProductQueryDTO> getAllProducts() {
        System.out.println("Query Service: Fetching all products");
        
        return productRepository.findAll().stream()
            .map(p -> new ProductQueryDTO(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getCategory(),
                p.getStock()
            ))
            .collect(Collectors.toList());
    }
    
    public List<ProductQueryDTO> getProductsByCategory(String category) {
        System.out.println("Query Service: Fetching products by category - " + category);
        
        return productRepository.findByCategory(category).stream()
            .map(p -> new ProductQueryDTO(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getCategory(),
                p.getStock()
            ))
            .collect(Collectors.toList());
    }
    
    public List<ProductQueryDTO> getProductsUnderPrice(BigDecimal price) {
        System.out.println("Query Service: Fetching products under $" + price);
        
        return productRepository.findByPriceLessThan(price).stream()
            .map(p -> new ProductQueryDTO(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getCategory(),
                p.getStock()
            ))
            .collect(Collectors.toList());
    }
}
```

```java org/example/patterns/dataaccess/cqrs/CQRSPatternDemo.java
package org.example.patterns.dataaccess.cqrs;

import org.example.patterns.dataaccess.cqrs.command.CreateProductCommand;
import org.example.patterns.dataaccess.cqrs.command.CreateProductCommandHandler;
import org.example.patterns.dataaccess.cqrs.command.UpdateProductPriceCommand;
import org.example.patterns.dataaccess.cqrs.command.UpdateProductPriceCommandHandler;
import org.example.patterns.dataaccess.cqrs.query.ProductQueryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(9)
public class CQRSPatternDemo implements CommandLineRunner {
    
    private final CreateProductCommandHandler createHandler;
    private final UpdateProductPriceCommandHandler updateHandler;
    private final ProductQueryService queryService;
    
    public CQRSPatternDemo(CreateProductCommandHandler createHandler,
                          UpdateProductPriceCommandHandler updateHandler,
                          ProductQueryService queryService) {
        this.createHandler = createHandler;
        this.updateHandler = updateHandler;
        this.queryService = queryService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== CQRS Pattern Demo ===");
        
        // Commands (Write operations)
        System.out.println("\n--- Executing Commands ---");
        createHandler.handle(new CreateProductCommand(
            "Gaming Laptop", "High-end gaming laptop", 
            new BigDecimal("2500.00"), 5, "Electronics"
        ));
        
        createHandler.handle(new CreateProductCommand(
            "Office Desk", "Ergonomic standing desk", 
            new BigDecimal("450.00"), 10, "Furniture"
        ));
        
        // Queries (Read operations)
        System.out.println("\n--- Executing Queries ---");
        queryService.getAllProducts().forEach(p -> 
            System.out.println("  " + p.getName() + " - $" + p.getPrice()));
        
        System.out.println("\n--- Electronics Query ---");
        queryService.getProductsByCategory("Electronics").forEach(p -> 
            System.out.println("  " + p.getName()));
        
        // Update command
        System.out.println("\n--- Update Command ---");
        updateHandler.handle(new UpdateProductPriceCommand(1L, new BigDecimal("2300.00")));
        
        System.out.println("\n--- Query After Update ---");
        queryService.getProductsUnderPrice(new BigDecimal("3000.00")).forEach(p -> 
            System.out.println("  " + p.getName() + " - $" + p.getPrice()));
        
        System.out.println("\nCQRS separates read and write operations!");
    }
}
```

---

## 10. Event Sourcing Pattern

```java org/example/patterns/dataaccess/eventsourcing/Event.java
package org.example.patterns.dataaccess.eventsourcing;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Event {
    
    private final String eventId;
    private final LocalDateTime timestamp;
    private final String aggregateId;
    
    protected Event(String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.aggregateId = aggregateId;
    }
    
    public String getEventId() { return eventId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getAggregateId() { return aggregateId; }
    
    public abstract String getEventType();
}
```

```java org/example/patterns/dataaccess/eventsourcing/AccountCreatedEvent.java
package org.example.patterns.dataaccess.eventsourcing;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AccountCreatedEvent extends Event {
    
    private final String accountNumber;
    private final String ownerName;
    private final BigDecimal initialBalance;
    
    public AccountCreatedEvent(String aggregateId, String accountNumber, 
                              String ownerName, BigDecimal initialBalance) {
        super(aggregateId);
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.initialBalance = initialBalance;
    }
    
    @Override
    public String getEventType() {
        return "AccountCreated";
    }
}
```

```java org/example/patterns/dataaccess/eventsourcing/MoneyDepositedEvent.java
package org.example.patterns.dataaccess.eventsourcing;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MoneyDepositedEvent extends Event {
    
    private final BigDecimal amount;
    
    public MoneyDepositedEvent(String aggregateId, BigDecimal amount) {
        super(aggregateId);
        this.amount = amount;
    }
    
    @Override
    public String getEventType() {
        return "MoneyDeposited";
    }
}
```

```java org/example/patterns/dataaccess/eventsourcing/MoneyWithdrawnEvent.java
package org.example.patterns.dataaccess.eventsourcing;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MoneyWithdrawnEvent extends Event {
    
    private final BigDecimal amount;
    
    public MoneyWithdrawnEvent(String aggregateId, BigDecimal amount) {
        super(aggregateId);
        this.amount = amount;
    }
    
    @Override
    public String getEventType() {
        return "MoneyWithdrawn";
    }
}
```

```java org/example/patterns/dataaccess/eventsourcing/EventStore.java
package org.example.patterns.dataaccess.eventsourcing;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventStore {
    
    private final List<Event> events = new ArrayList<>();
    
    public void save(Event event) {
        events.add(event);
        System.out.println("EventStore: Saved " + event.getEventType() + 
                         " event for aggregate " + event.getAggregateId());
    }
    
    public List<Event> getEvents(String aggregateId) {
        return events.stream()
            .filter(e -> e.getAggregateId().equals(aggregateId))
            .collect(Collectors.toList());
    }
    
    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }
}
```

```java org/example/patterns/dataaccess/eventsourcing/BankAccountAggregate.java
package org.example.patterns.dataaccess.eventsourcing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BankAccountAggregate {
    
    private String id;
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance;
    private List<Event> uncommittedEvents = new ArrayList<>();
    
    public BankAccountAggregate() {
        this.id = UUID.randomUUID().toString();
    }
    
    // Command: Create Account
    public void create(String accountNumber, String ownerName, BigDecimal initialBalance) {
        AccountCreatedEvent event = new AccountCreatedEvent(
            id, accountNumber, ownerName, initialBalance
        );
        applyEvent(event);
        uncommittedEvents.add(event);
    }
    
    // Command: Deposit Money
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        MoneyDepositedEvent event = new MoneyDepositedEvent(id, amount);
        applyEvent(event);
        uncommittedEvents.add(event);
    }
    
    // Command: Withdraw Money
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        
        MoneyWithdrawnEvent event = new MoneyWithdrawnEvent(id, amount);
        applyEvent(event);
        uncommittedEvents.add(event);
    }
    
    // Apply event to update state
    private void applyEvent(Event event) {
        if (event instanceof AccountCreatedEvent) {
            AccountCreatedEvent e = (AccountCreatedEvent) event;
            this.accountNumber = e.getAccountNumber();
            this.ownerName = e.getOwnerName();
            this.balance = e.getInitialBalance();
        } else if (event instanceof MoneyDepositedEvent) {
            MoneyDepositedEvent e = (MoneyDepositedEvent) event;
            this.balance = this.balance.add(e.getAmount());
        } else if (event instanceof MoneyWithdrawnEvent) {
            MoneyWithdrawnEvent e = (MoneyWithdrawnEvent) event;
            this.balance = this.balance.subtract(e.getAmount());
        }
    }
    
    // Rebuild state from events
    public void loadFromHistory(List<Event> events) {
        events.forEach(this::applyEvent);
    }
    
    public List<Event> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }
    
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
    
    // Getters
    public String getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getBalance() { return balance; }
}
```

```java org/example/patterns/dataaccess/eventsourcing/EventSourcingPatternDemo.java
package org.example.patterns.dataaccess.eventsourcing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(10)
public class EventSourcingPatternDemo implements CommandLineRunner {
    
    private final EventStore eventStore;
    
    public EventSourcingPatternDemo(EventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Event Sourcing Pattern Demo ===");
        
        // Create account aggregate
        BankAccountAggregate account = new BankAccountAggregate();
        
        // Execute commands (generate events)
        System.out.println("\n--- Executing Commands ---");
        account.create("ACC-12345", "John Doe", new BigDecimal("1000.00"));
        account.deposit(new BigDecimal("500.00"));
        account.withdraw(new BigDecimal("200.00"));
        account.deposit(new BigDecimal("300.00"));
        
        // Save events to event store
        account.getUncommittedEvents().forEach(eventStore::save);
        account.markEventsAsCommitted();
        
        System.out.println("\nCurrent state:");
        System.out.println("  Account: " + account.getAccountNumber());
        System.out.println("  Owner: " + account.getOwnerName());
        System.out.println("  Balance: $" + account.getBalance());
        
        // Rebuild state from events
        System.out.println("\n--- Rebuilding State from Events ---");
        BankAccountAggregate rebuiltAccount = new BankAccountAggregate();
        rebuiltAccount.loadFromHistory(eventStore.getEvents(account.getId()));
        
        System.out.println("Rebuilt state:");
        System.out.println("  Account: " + rebuiltAccount.getAccountNumber());
        System.out.println("  Owner: " + rebuiltAccount.getOwnerName());
        System.out.println("  Balance: $" + rebuiltAccount.getBalance());
        
        // Show event history
        System.out.println("\n--- Event History ---");
        eventStore.getAllEvents().forEach(e -> 
            System.out.println("  " + e.getTimestamp() + " - " + e.getEventType()));
        
        System.out.println("\nEvent Sourcing stores all state changes as events!");
    }
}
```

---

## 11. Optimistic Locking Pattern

```java org/example/patterns/dataaccess/locking/optimistic/InventoryItem.java
package org.example.patterns.dataaccess.locking.optimistic;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
public class InventoryItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String productName;
    private Integer quantity;
    
    @Version // Optimistic locking
    private Long version;
    
    public InventoryItem(String productName, Integer quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }
    
    public void decreaseQuantity(int amount) {
        if (quantity < amount) {
            throw new IllegalArgumentException("Insufficient quantity");
        }
        quantity -= amount;
    }
    
    public void increaseQuantity(int amount) {
        quantity += amount;
    }
}
```

```java org/example/patterns/dataaccess/locking/optimistic/InventoryRepository.java
package org.example.patterns.dataaccess.locking.optimistic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
}
```

```java org/example/patterns/dataaccess/locking/optimistic/OptimisticLockingDemo.java
package org.example.patterns.dataaccess.locking.optimistic;

import jakarta.persistence.OptimisticLockException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(11)
public class OptimisticLockingDemo implements CommandLineRunner {
    
    private final InventoryRepository inventoryRepository;
    
    public OptimisticLockingDemo(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }
    
    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("\n=== Optimistic Locking Pattern Demo ===");
        
        // Create inventory item
        InventoryItem item = new InventoryItem("Laptop", 10);
        item = inventoryRepository.save(item);
        System.out.println("\nCreated item: " + item.getProductName() + 
                         " (Quantity: " + item.getQuantity() + ", Version: " + item.getVersion() + ")");
        
        // Simulate concurrent updates
        Long itemId = item.getId();
        
        // Transaction 1: Read item
        InventoryItem item1 = inventoryRepository.findById(itemId).orElseThrow();
        System.out.println("\nTransaction 1 read: Version " + item1.getVersion());
        
        // Transaction 2: Read and update item
        InventoryItem item2 = inventoryRepository.findById(itemId).orElseThrow();
        System.out.println("Transaction 2 read: Version " + item2.getVersion());
        
        item2.decreaseQuantity(2);
        inventoryRepository.save(item2);
        System.out.println("Transaction 2 committed: Version " + item2.getVersion());
        
        // Transaction 1: Try to update (will fail with OptimisticLockException)
        try {
            item1.decreaseQuantity(3);
            inventoryRepository.save(item1);
            System.out.println("Transaction 1 committed");
        } catch (OptimisticLockException e) {
            System.out.println("Transaction 1 failed: Optimistic lock exception!");
            System.out.println("  Item was modified by another transaction");
            
            // Retry with fresh data
            InventoryItem freshItem = inventoryRepository.findById(itemId).orElseThrow();
            freshItem.decreaseQuantity(3);
            inventoryRepository.save(freshItem);
            System.out.println("  Retried with fresh data: Version " + freshItem.getVersion());
        }
        
        // Final state
        InventoryItem finalItem = inventoryRepository.findById(itemId).orElseThrow();
        System.out.println("\nFinal state: Quantity = " + finalItem.getQuantity() + 
                         ", Version = " + finalItem.getVersion());
        
        System.out.println("\nOptimistic locking prevents lost updates!");
    }
}
```

---

## 12. Pessimistic Locking Pattern

```java org/example/patterns/dataaccess/locking/pessimistic/BankAccountLocking.java
package org.example.patterns.dataaccess.locking.pessimistic;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts_locking")
@Data
@NoArgsConstructor
public class BankAccountLocking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String accountNumber;
    private BigDecimal balance;
    
    public BankAccountLocking(String accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
    
    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
    }
    
    public void withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        balance = balance.subtract(amount);
    }
}
```

```java org/example/patterns/dataaccess/locking/pessimistic/BankAccountLockingRepository.java
package org.example.patterns.dataaccess.locking.pessimistic;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountLockingRepository extends JpaRepository<BankAccountLocking, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM BankAccountLocking a WHERE a.id = :id")
    Optional<BankAccountLocking> findByIdWithLock(@Param("id") Long id);
    
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT a FROM BankAccountLocking a WHERE a.accountNumber = :accountNumber")
    Optional<BankAccountLocking> findByAccountNumberWithReadLock(@Param("accountNumber") String accountNumber);
}
```

```java org/example/patterns/dataaccess/locking/pessimistic/PessimisticLockingDemo.java
package org.example.patterns.dataaccess.locking.pessimistic;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Order(12)
public class PessimisticLockingDemo implements CommandLineRunner {
    
    private final BankAccountLockingRepository repository;
    
    public PessimisticLockingDemo(BankAccountLockingRepository repository) {
        this.repository = repository;
    }
    
    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("\n=== Pessimistic Locking Pattern Demo ===");
        
        // Create account
        BankAccountLocking account = new BankAccountLocking("ACC-999", new BigDecimal("1000.00"));
        account = repository.save(account);
        System.out.println("\nCreated account: " + account.getAccountNumber() + 
                         " with balance $" + account.getBalance());
        
        Long accountId = account.getId();
        
        // Simulate transaction with pessimistic lock
        System.out.println("\n--- Transaction with Pessimistic Write Lock ---");
        BankAccountLocking lockedAccount = repository.findByIdWithLock(accountId).orElseThrow();
        System.out.println("Acquired lock on account: " + lockedAccount.getAccountNumber());
        System.out.println("  Current balance: $" + lockedAccount.getBalance());
        
        // Perform operations while holding lock
        lockedAccount.withdraw(new BigDecimal("200.00"));
        System.out.println("  Withdrawn $200.00");
        
        lockedAccount.deposit(new BigDecimal("50.00"));
        System.out.println("  Deposited $50.00");
        
        repository.save(lockedAccount);
        System.out.println("  New balance: $" + lockedAccount.getBalance());
        System.out.println("Transaction committed, lock released");
        
        // Read lock example
        System.out.println("\n--- Read Lock Example ---");
        BankAccountLocking readLockedAccount = 
            repository.findByAccountNumberWithReadLock("ACC-999").orElseThrow();
        System.out.println("Acquired read lock on account: " + readLockedAccount.getAccountNumber());
        System.out.println("  Balance: $" + readLockedAccount.getBalance());
        
        System.out.println("\nPessimistic locking prevents concurrent modifications!");
    }
}
```

---

## 13. Row Data Gateway Pattern

```java org/example/patterns/dataaccess/gateway/row/CustomerGateway.java
package org.example.patterns.dataaccess.gateway.row;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Data
public class CustomerGateway {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate registrationDate;
    
    // Load from ResultSet
    public void load(ResultSet rs) throws SQLException {
        this.id = rs.getLong("id");
        this.firstName = rs.getString("first_name");
        this.lastName = rs.getString("last_name");
        this.email = rs.getString("email");
        this.registrationDate = rs.getDate("registration_date").toLocalDate();
    }
    
    // Insert
    public void insert() {
        System.out.println("RowGateway: INSERT customer - " + firstName + " " + lastName);
        // Simulated SQL: INSERT INTO customers (first_name, last_name, email, registration_date) VALUES (?, ?, ?, ?)
    }
    
    // Update
    public void update() {
        System.out.println("RowGateway: UPDATE customer ID " + id);
        // Simulated SQL: UPDATE customers SET first_name=?, last_name=?, email=? WHERE id=?
    }
    
    // Delete
    public void delete() {
        System.out.println("RowGateway: DELETE customer ID " + id);
        // Simulated SQL: DELETE FROM customers WHERE id=?
    }
    
    // Finder methods
    public static CustomerGateway findById(Long id) {
        System.out.println("RowGateway: SELECT customer WHERE id=" + id);
        // Simulated database access
        CustomerGateway gateway = new CustomerGateway();
        gateway.setId(id);
        gateway.setFirstName("John");
        gateway.setLastName("Doe");
        gateway.setEmail("john@example.com");
        gateway.setRegistrationDate(LocalDate.now());
        return gateway;
    }
}
```

```java org/example/patterns/dataaccess/gateway/row/RowDataGatewayDemo.java
package org.example.patterns.dataaccess.gateway.row;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Order(13)
public class RowDataGatewayDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Row Data Gateway Pattern Demo ===");
        
        // Create new customer
        System.out.println("\n--- Creating Customer ---");
        CustomerGateway customer = new CustomerGateway();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setEmail("jane@example.com");
        customer.setRegistrationDate(LocalDate.now());
        customer.insert();
        
        // Find customer
        System.out.println("\n--- Finding Customer ---");
        CustomerGateway found = CustomerGateway.findById(1L);
        System.out.println("Found: " + found.getFirstName() + " " + found.getLastName());
        
        // Update customer
        System.out.println("\n--- Updating Customer ---");
        found.setEmail("jane.smith@newdomain.com");
        found.update();
        
        // Delete customer
        System.out.println("\n--- Deleting Customer ---");
        found.delete();
        
        System.out.println("\nRow Data Gateway encapsulates database access for a single row!");
    }
}
```

---

## 14. Table Data Gateway Pattern

```java org/example/patterns/dataaccess/gateway/table/ProductTableGateway.java
package org.example.patterns.dataaccess.gateway.table;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class ProductTableGateway {
    
    // Simulated database table
    private final Map<Long, ProductRecord> table = new HashMap<>();
    private Long idSequence = 1L;
    
    public ProductTableGateway() {
        // Initialize with sample data
        insert("Laptop", new BigDecimal("1200.00"), "Electronics");
        insert("Mouse", new BigDecimal("25.00"), "Electronics");
        insert("Desk", new BigDecimal("350.00"), "Furniture");
    }
    
    // Table-level operations
    public Long insert(String name, BigDecimal price, String category) {
        System.out.println("TableGateway: INSERT into products - " + name);
        Long id = idSequence++;
        ProductRecord record = new ProductRecord(id, name, price, category);
        table.put(id, record);
        return id;
    }
    
    public void update(Long id, String name, BigDecimal price, String category) {
        System.out.println("TableGateway: UPDATE products WHERE id=" + id);
        ProductRecord record = new ProductRecord(id, name, price, category);
        table.put(id, record);
    }
    
    public void delete(Long id) {
        System.out.println("TableGateway: DELETE from products WHERE id=" + id);
        table.remove(id);
    }
    
    public ProductRecord findById(Long id) {
        System.out.println("TableGateway: SELECT * FROM products WHERE id=" + id);
        return table.get(id);
    }
    
    public List<ProductRecord> findAll() {
        System.out.println("TableGateway: SELECT * FROM products");
        return new ArrayList<>(table.values());
    }
    
    public List<ProductRecord> findByCategory(String category) {
        System.out.println("TableGateway: SELECT * FROM products WHERE category='" + category + "'");
        return table.values().stream()
                .filter(p -> p.getCategory().equals(category))
                .toList();
    }
    
    public List<ProductRecord> findByPriceRange(BigDecimal min, BigDecimal max) {
        System.out.println("TableGateway: SELECT * FROM products WHERE price BETWEEN " + min + " AND " + max);
        return table.values().stream()
                .filter(p -> p.getPrice().compareTo(min) >= 0 && p.getPrice().compareTo(max) <= 0)
                .toList();
    }
    
    public int count() {
        System.out.println("TableGateway: SELECT COUNT(*) FROM products");
        return table.size();
    }
    
    // Static record class
    public static class ProductRecord {
        private final Long id;
        private final String name;
        private final BigDecimal price;
        private final String category;
        
        public ProductRecord(Long id, String name, BigDecimal price, String category) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.category = category;
        }
        
        public Long getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public String getCategory() { return category; }
        
        @Override
        public String toString() {
            return "ProductRecord{id=" + id + ", name='" + name + "', price=" + price + 
                   ", category='" + category + "'}";
        }
    }
}
```

```java org/example/patterns/dataaccess/gateway/table/TableDataGatewayDemo.java
package org.example.patterns.dataaccess.gateway.table;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(14)
public class TableDataGatewayDemo implements CommandLineRunner {
    
    private final ProductTableGateway gateway;
    
    public TableDataGatewayDemo(ProductTableGateway gateway) {
        this.gateway = gateway;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Table Data Gateway Pattern Demo ===");
        
        // Insert
        System.out.println("\n--- Inserting Product ---");
        Long newId = gateway.insert("Monitor", new BigDecimal("450.00"), "Electronics");
        System.out.println("Inserted with ID: " + newId);
        
        // Find all
        System.out.println("\n--- All Products ---");
        gateway.findAll().forEach(p -> 
            System.out.println("  " + p.getName() + " - $" + p.getPrice()));
        
        // Find by category
        System.out.println("\n--- Electronics Category ---");
        gateway.findByCategory("Electronics").forEach(p -> 
            System.out.println("  " + p.getName()));
        
        // Find by price range
        System.out.println("\n--- Products $100-$500 ---");
        gateway.findByPriceRange(new BigDecimal("100"), new BigDecimal("500"))
            .forEach(p -> System.out.println("  " + p.getName() + " - $" + p.getPrice()));
        
        // Update
        System.out.println("\n--- Updating Product ---");
        gateway.update(newId, "4K Monitor", new BigDecimal("550.00"), "Electronics");
        
        // Count
        System.out.println("\n--- Total Products ---");
        System.out.println("Count: " + gateway.count());
        
        // Delete
        System.out.println("\n--- Deleting Product ---");
        gateway.delete(newId);
        System.out.println("Products after delete: " + gateway.count());
        
        System.out.println("\nTable Data Gateway provides table-level database operations!");
    }
}
```

---

## 15. Database Session Pattern

```java org/example/patterns/dataaccess/session/DatabaseSession.java
package org.example.patterns.dataaccess.session;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class DatabaseSession {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final Map<Object, Object> identityMap = new HashMap<>();
    private final Set<Object> dirtyObjects = new HashSet<>();
    private final Set<Object> newObjects = new HashSet<>();
    private final Set<Object> deletedObjects = new HashSet<>();
    
    // Load entity with identity map
    public <T> T load(Class<T> entityClass, Object id) {
        System.out.println("Session: Loading " + entityClass.getSimpleName() + " with id " + id);
        
        // Check identity map first
        String key = entityClass.getName() + ":" + id;
        if (identityMap.containsKey(key)) {
            System.out.println("  Found in identity map (cache hit)");
            return entityClass.cast(identityMap.get(key));
        }
        
        // Load from database
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            identityMap.put(key, entity);
            System.out.println("  Loaded from database and cached");
        }
        
        return entity;
    }
    
    // Register new object
    public void registerNew(Object entity) {
        System.out.println("Session: Registering new entity - " + entity.getClass().getSimpleName());
        newObjects.add(entity);
    }
    
    // Register dirty object
    public void registerDirty(Object entity) {
        System.out.println("Session: Marking entity as dirty - " + entity.getClass().getSimpleName());
        if (!newObjects.contains(entity)) {
            dirtyObjects.add(entity);
        }
    }
    
    // Register deleted object
    public void registerDeleted(Object entity) {
        System.out.println("Session: Marking entity for deletion - " + entity.getClass().getSimpleName());
        if (newObjects.remove(entity)) {
            return; // Was new, just remove from new set
        }
        dirtyObjects.remove(entity);
        deletedObjects.add(entity);
    }
    
    // Commit all changes
    @Transactional
    public void commit() {
        System.out.println("\n=== Session: Committing Changes ===");
        
        // Insert new objects
        System.out.println("Inserting " + newObjects.size() + " new objects");
        newObjects.forEach(entityManager::persist);
        
        // Update dirty objects
        System.out.println("Updating " + dirtyObjects.size() + " dirty objects");
        dirtyObjects.forEach(entityManager::merge);
        
        // Delete removed objects
        System.out.println("Deleting " + deletedObjects.size() + " objects");
        deletedObjects.forEach(entity -> {
            if (!entityManager.contains(entity)) {
                entity = entityManager.merge(entity);
            }
            entityManager.remove(entity);
        });
        
        entityManager.flush();
        clear();
        
        System.out.println("Commit completed!\n");
    }
    
    // Clear session
    public void clear() {
        identityMap.clear();
        dirtyObjects.clear();
        newObjects.clear();
        deletedObjects.clear();
    }
    
    public int getPendingChanges() {
        return newObjects.size() + dirtyObjects.size() + deletedObjects.size();
    }
}
```

```java org/example/patterns/dataaccess/session/SessionEntity.java
package org.example.patterns.dataaccess.session;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "session_entities")
@Data
@NoArgsConstructor
public class SessionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    
    public SessionEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
```

```java org/example/patterns/dataaccess/session/DatabaseSessionDemo.java
package org.example.patterns.dataaccess.session;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(15)
public class DatabaseSessionDemo implements CommandLineRunner {
    
    private final DatabaseSession session;
    
    public DatabaseSessionDemo(DatabaseSession session) {
        this.session = session;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Database Session Pattern Demo ===");
        
        // Create new entities
        System.out.println("\n--- Creating Entities ---");
        SessionEntity entity1 = new SessionEntity("Entity 1", "First entity");
        SessionEntity entity2 = new SessionEntity("Entity 2", "Second entity");
        SessionEntity entity3 = new SessionEntity("Entity 3", "Third entity");
        
        session.registerNew(entity1);
        session.registerNew(entity2);
        session.registerNew(entity3);
        
        System.out.println("Pending changes: " + session.getPendingChanges());
        
        // Modify entity
        System.out.println("\n--- Modifying Entity ---");
        entity1.setDescription("Modified description");
        session.registerDirty(entity1);
        
        // Delete entity
        System.out.println("\n--- Deleting Entity ---");
        session.registerDeleted(entity3);
        
        System.out.println("Pending changes: " + session.getPendingChanges());
        
        // Commit all changes
        session.commit();
        
        System.out.println("Pending changes after commit: " + session.getPendingChanges());
        
        System.out.println("\nDatabase Session manages object lifecycle and transactions!");
    }
}
```

---

## Main Application

```java org/example/DataAccessPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataAccessPatternsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DataAccessPatternsApplication.class, args);
    }
}
```

---

## Application Properties

```properties src/main/resources/application.properties
# Application Configuration
spring.application.name=spring-data-access-patterns

# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console (optional)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Disable banner
spring.main.banner-mode=off
```

---

## README

```markdown README.md
# Spring Data Access Patterns

Comprehensive implementation of 15 Data Access Patterns using Spring Data JPA.

## Patterns Implemented

1. **Repository Pattern** - Abstraction for data access with Spring Data JPA
2. **DAO Pattern** - Data Access Object with EntityManager
3. **DTO Pattern** - Data Transfer Objects for layer separation
4. **Value Object Pattern** - Immutable value objects (@Embeddable)
5. **Entity Pattern** - Rich domain entities with business logic
6. **Aggregate Pattern** - Aggregate root controlling object graphs
7. **Specification Pattern** - Composable business rules
8. **Query Object Pattern** - Encapsulated query logic
9. **CQRS Pattern** - Command Query Responsibility Segregation
10. **Event Sourcing Pattern** - State changes as events
11. **Optimistic Locking** - @Version-based concurrency control
12. **Pessimistic Locking** - Database-level locks
13. **Row Data Gateway** - Object wrapping database row
14. **Table Data Gateway** - Object wrapping database table
15. **Database Session** - Unit of Work with identity map

## Running the Application

```bash
mvn spring-boot:run
```

## Accessing H2 Console

URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: (empty)

## Build

```bash
mvn clean package
```

## Requirements

- Java 17+
- Maven 3.6+
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database

## Pattern Demonstrations

Each pattern runs automatically on application startup with @Order annotation.
Check console output to see pattern demonstrations.

## Key Concepts

- **Repository vs DAO**: Repository is domain-centric, DAO is data-centric
- **DTO vs Entity**: DTOs for data transfer, Entities for business logic
- **Value Objects**: Immutable, compared by value
- **Aggregates**: Consistency boundaries with root
- **CQRS**: Separate models for reads and writes
- **Event Sourcing**: Audit trail and state reconstruction
- **Locking**: Optimistic (version) vs Pessimistic (database locks)
- **Gateways**: Encapsulate database access patterns

## Testing

```bash
mvn test
```
```

This completes a comprehensive implementation of 15 Data Access Patterns demonstrating various approaches to data persistence and retrieval in Spring applications!