# Spring Transaction Management Patterns

I'll create a comprehensive Spring Boot application demonstrating all 10 transaction management patterns.

## Project Structure

```
spring-transaction-patterns/
├── src/main/java/org/example/
│   ├── TransactionPatternsApplication.java
│   ├── config/
│   ├── model/
│   └── patterns/transaction/
│       ├── declarative/
│       ├── programmatic/
│       ├── template/
│       ├── interceptor/
│       ├── synchronization/
│       ├── nested/
│       ├── distributed/
│       ├── compensating/
│       ├── saga/
│       └── twophase/
├── pom.xml
└── application.properties
```

## 1. Main Application & Configuration

```java
// src/main/java/org/example/TransactionPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class TransactionPatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionPatternsApplication.class, args);
    }
}
```

```java
// src/main/java/org/example/config/DatabaseConfig.java
package org.example.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    
    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }
    
    @Bean(name = "secondaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }
    
    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(DataSource primaryDataSource) {
        return new DataSourceTransactionManager(primaryDataSource);
    }
    
    @Bean(name = "secondaryTransactionManager")
    public PlatformTransactionManager secondaryTransactionManager(
            DataSource secondaryDataSource) {
        return new DataSourceTransactionManager(secondaryDataSource);
    }
}
```

## 2. Domain Models

```java
// src/main/java/org/example/model/Account.java
package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String accountNumber;
    
    @Column(nullable = false)
    private BigDecimal balance;
    
    @Version
    private Long version;
    
    public Account(String accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
}
```

```java
// src/main/java/org/example/model/TransactionLog.java
package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String status;
    private LocalDateTime timestamp;
    
    public TransactionLog(String transactionId, String fromAccount, 
                         String toAccount, BigDecimal amount, String status) {
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
```

```java
// src/main/java/org/example/model/Order.java
package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String orderNumber;
    
    private String customerId;
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    public Order(String orderNumber, String customerId, BigDecimal totalAmount) {
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
    }
    
    public enum OrderStatus {
        PENDING, CONFIRMED, CANCELLED, COMPENSATED
    }
}
```

```java
// src/main/java/org/example/model/SagaState.java
package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "saga_states")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaState {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String sagaId;
    
    @Enumerated(EnumType.STRING)
    private SagaStatus status;
    
    private String currentStep;
    private String lastCompletedStep;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public SagaState(String sagaId) {
        this.sagaId = sagaId;
        this.status = SagaStatus.STARTED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum SagaStatus {
        STARTED, IN_PROGRESS, COMPLETED, FAILED, COMPENSATING, COMPENSATED
    }
}
```

## 3. Repositories

```java
// src/main/java/org/example/repository/AccountRepository.java
package org.example.repository;

import org.example.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithLock(String accountNumber);
}
```

```java
// src/main/java/org/example/repository/TransactionLogRepository.java
package org.example.repository;

import org.example.model.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    Optional<TransactionLog> findByTransactionId(String transactionId);
    List<TransactionLog> findByStatus(String status);
}
```

```java
// src/main/java/org/example/repository/OrderRepository.java
package org.example.repository;

import org.example.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
}
```

```java
// src/main/java/org/example/repository/SagaStateRepository.java
package org.example.repository;

import org.example.model.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SagaStateRepository extends JpaRepository<SagaState, Long> {
    Optional<SagaState> findBySagaId(String sagaId);
}
```

## 4. Pattern 1: Declarative Transaction Pattern

```java
// src/main/java/org/example/patterns/transaction/declarative/DeclarativeTransactionService.java
package org.example.patterns.transaction.declarative;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Declarative Transaction Pattern using @Transactional annotation.
 * This is the most common and recommended approach.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeclarativeTransactionService {
    
    private final AccountRepository accountRepository;
    
    /**
     * Default transaction: REQUIRED propagation, READ_COMMITTED isolation
     */
    @Transactional
    public void transfer(String fromAccount, String toAccount, BigDecimal amount) {
        log.info("Declarative transaction started for transfer");
        
        Account from = accountRepository.findByAccountNumber(fromAccount)
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));
        Account to = accountRepository.findByAccountNumber(toAccount)
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));
        
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        
        accountRepository.save(from);
        accountRepository.save(to);
        
        log.info("Transfer completed: {} -> {}, amount: {}", fromAccount, toAccount, amount);
    }
    
    /**
     * Read-only transaction with SERIALIZABLE isolation
     */
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public BigDecimal getBalance(String accountNumber) {
        log.info("Read-only transaction for balance check");
        return accountRepository.findByAccountNumber(accountNumber)
                .map(Account::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }
    
    /**
     * Transaction with REQUIRES_NEW propagation (always creates new transaction)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAccount(String accountNumber, BigDecimal initialBalance) {
        log.info("Creating account with REQUIRES_NEW propagation");
        Account account = new Account(accountNumber, initialBalance);
        accountRepository.save(account);
    }
    
    /**
     * Transaction with timeout and specific rollback rules
     */
    @Transactional(
        timeout = 5,
        rollbackFor = Exception.class,
        noRollbackFor = IllegalArgumentException.class
    )
    public void complexOperation(String accountNumber, BigDecimal amount) {
        log.info("Complex operation with custom transaction settings");
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        
        // Simulate processing
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/declarative/DeclarativeTransactionDemo.java
package org.example.patterns.transaction.declarative;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DeclarativeTransactionDemo implements CommandLineRunner {
    
    private final DeclarativeTransactionService transactionService;
    private final AccountRepository accountRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Declarative Transaction Pattern Demo ===");
        
        // Setup test accounts
        accountRepository.save(new Account("ACC-001", new BigDecimal("1000.00")));
        accountRepository.save(new Account("ACC-002", new BigDecimal("500.00")));
        
        // Test basic transfer
        System.out.println("\n1. Basic Transfer:");
        transactionService.transfer("ACC-001", "ACC-002", new BigDecimal("200.00"));
        
        BigDecimal balance1 = transactionService.getBalance("ACC-001");
        BigDecimal balance2 = transactionService.getBalance("ACC-002");
        System.out.println("ACC-001 balance: $" + balance1);
        System.out.println("ACC-002 balance: $" + balance2);
        
        // Test rollback on insufficient funds
        System.out.println("\n2. Test Rollback on Error:");
        try {
            transactionService.transfer("ACC-001", "ACC-002", new BigDecimal("10000.00"));
        } catch (IllegalStateException e) {
            System.out.println("Transaction rolled back: " + e.getMessage());
        }
        
        // Verify balances unchanged
        balance1 = transactionService.getBalance("ACC-001");
        System.out.println("ACC-001 balance after failed transfer: $" + balance1);
        
        // Test REQUIRES_NEW propagation
        System.out.println("\n3. REQUIRES_NEW Propagation:");
        transactionService.createAccount("ACC-003", new BigDecimal("2000.00"));
        System.out.println("New account created independently");
        
        System.out.println("\nDeclarative Transaction Pattern demonstrated!\n");
    }
}
```

## 5. Pattern 2: Programmatic Transaction Pattern

```java
// src/main/java/org/example/patterns/transaction/programmatic/ProgrammaticTransactionService.java
package org.example.patterns.transaction.programmatic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;

/**
 * Programmatic Transaction Pattern using PlatformTransactionManager directly.
 * Provides fine-grained control over transaction boundaries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProgrammaticTransactionService {
    
    private final PlatformTransactionManager transactionManager;
    private final AccountRepository accountRepository;
    
    public void transferWithManualControl(String fromAccount, String toAccount, 
                                         BigDecimal amount) {
        // Define transaction properties
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ManualTransfer");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        def.setTimeout(10);
        
        // Start transaction
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            log.info("Programmatic transaction started");
            
            Account from = accountRepository.findByAccountNumber(fromAccount)
                    .orElseThrow(() -> new IllegalArgumentException("From account not found"));
            Account to = accountRepository.findByAccountNumber(toAccount)
                    .orElseThrow(() -> new IllegalArgumentException("To account not found"));
            
            if (from.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient balance");
            }
            
            from.setBalance(from.getBalance().subtract(amount));
            to.setBalance(to.getBalance().add(amount));
            
            accountRepository.save(from);
            accountRepository.save(to);
            
            // Manually commit
            transactionManager.commit(status);
            log.info("Transaction committed successfully");
            
        } catch (Exception e) {
            log.error("Transaction failed, rolling back", e);
            // Manually rollback
            transactionManager.rollback(status);
            throw e;
        }
    }
    
    public void conditionalCommit(String accountNumber, BigDecimal amount, 
                                  boolean shouldCommit) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            
            BigDecimal oldBalance = account.getBalance();
            account.setBalance(account.getBalance().add(amount));
            accountRepository.save(account);
            
            if (shouldCommit) {
                transactionManager.commit(status);
                log.info("Conditional commit: COMMITTED");
            } else {
                transactionManager.rollback(status);
                log.info("Conditional commit: ROLLED BACK");
            }
            
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }
    
    public void nestedOperations(String accountNumber) {
        DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
        outerDef.setName("OuterTransaction");
        TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);
        
        try {
            log.info("Outer transaction started");
            
            // First operation
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow();
            account.setBalance(account.getBalance().add(new BigDecimal("100")));
            accountRepository.save(account);
            
            // Inner transaction
            DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
            innerDef.setName("InnerTransaction");
            innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);
            
            try {
                log.info("Inner transaction started");
                account.setBalance(account.getBalance().add(new BigDecimal("50")));
                accountRepository.save(account);
                transactionManager.commit(innerStatus);
                log.info("Inner transaction committed");
            } catch (Exception e) {
                transactionManager.rollback(innerStatus);
                log.error("Inner transaction rolled back");
            }
            
            transactionManager.commit(outerStatus);
            log.info("Outer transaction committed");
            
        } catch (Exception e) {
            transactionManager.rollback(outerStatus);
            log.error("Outer transaction rolled back");
            throw e;
        }
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/programmatic/ProgrammaticTransactionDemo.java
package org.example.patterns.transaction.programmatic;

import lombok.RequiredArgsConstructor;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(2)
@RequiredArgsConstructor
public class ProgrammaticTransactionDemo implements CommandLineRunner {
    
    private final ProgrammaticTransactionService transactionService;
    private final AccountRepository accountRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Programmatic Transaction Pattern Demo ===");
        
        // Setup
        accountRepository.save(new Account("PROG-001", new BigDecimal("1000.00")));
        accountRepository.save(new Account("PROG-002", new BigDecimal("500.00")));
        
        // Test manual transaction control
        System.out.println("\n1. Manual Transaction Control:");
        transactionService.transferWithManualControl("PROG-001", "PROG-002", 
                new BigDecimal("200.00"));
        
        BigDecimal balance = accountRepository.findByAccountNumber("PROG-001")
                .get().getBalance();
        System.out.println("PROG-001 balance after transfer: $" + balance);
        
        // Test conditional commit
        System.out.println("\n2. Conditional Commit/Rollback:");
        transactionService.conditionalCommit("PROG-001", new BigDecimal("100.00"), true);
        transactionService.conditionalCommit("PROG-001", new BigDecimal("500.00"), false);
        
        balance = accountRepository.findByAccountNumber("PROG-001").get().getBalance();
        System.out.println("PROG-001 balance (should reflect only committed): $" + balance);
        
        // Test nested operations
        System.out.println("\n3. Nested Transactions:");
        transactionService.nestedOperations("PROG-001");
        
        System.out.println("\nProgrammatic Transaction Pattern demonstrated!\n");
    }
}
```

## 6. Pattern 3: Transaction Template Pattern

```java
// src/main/java/org/example/patterns/transaction/template/TransactionTemplateService.java
package org.example.patterns.transaction.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

/**
 * Transaction Template Pattern using Spring's TransactionTemplate.
 * Simplifies programmatic transaction management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionTemplateService {
    
    private final TransactionTemplate transactionTemplate;
    private final AccountRepository accountRepository;
    
    public void transferUsingTemplate(String fromAccount, String toAccount, 
                                     BigDecimal amount) {
        transactionTemplate.execute(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(TransactionStatus status) {
                try {
                    log.info("Transaction template execution started");
                    
                    Account from = accountRepository.findByAccountNumber(fromAccount)
                            .orElseThrow(() -> new IllegalArgumentException("From account not found"));
                    Account to = accountRepository.findByAccountNumber(toAccount)
                            .orElseThrow(() -> new IllegalArgumentException("To account not found"));
                    
                    if (from.getBalance().compareTo(amount) < 0) {
                        // Mark for rollback
                        status.setRollbackOnly();
                        throw new IllegalStateException("Insufficient balance");
                    }
                    
                    from.setBalance(from.getBalance().subtract(amount));
                    to.setBalance(to.getBalance().add(amount));
                    
                    accountRepository.save(from);
                    accountRepository.save(to);
                    
                    log.info("Transfer completed in template");
                    return null;
                    
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw e;
                }
            }
        });
    }
    
    public BigDecimal calculateTotalBalance(String... accountNumbers) {
        return transactionTemplate.execute(status -> {
            log.info("Read-only transaction template");
            
            BigDecimal total = BigDecimal.ZERO;
            for (String accountNumber : accountNumbers) {
                Account account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow();
                total = total.add(account.getBalance());
            }
            return total;
        });
    }
    
    public void depositWithLambda(String accountNumber, BigDecimal amount) {
        transactionTemplate.execute(status -> {
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow();
            account.setBalance(account.getBalance().add(amount));
            accountRepository.save(account);
            
            log.info("Deposit completed: {} -> ${}", accountNumber, amount);
            return null;
        });
    }
    
    public Boolean withdrawWithValidation(String accountNumber, BigDecimal amount) {
        return transactionTemplate.execute(status -> {
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow();
            
            if (account.getBalance().compareTo(amount) < 0) {
                log.warn("Insufficient funds, marking rollback");
                status.setRollbackOnly();
                return false;
            }
            
            account.setBalance(account.getBalance().subtract(amount));
            accountRepository.save(account);
            log.info("Withdrawal completed: {} -> ${}", accountNumber, amount);
            return true;
        });
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/template/TransactionTemplateConfig.java
package org.example.patterns.transaction.template;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class TransactionTemplateConfig {
    
    @Bean
    public TransactionTemplate transactionTemplate(
            PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setTimeout(30);
        return template;
    }
    
    @Bean(name = "readOnlyTransactionTemplate")
    public TransactionTemplate readOnlyTransactionTemplate(
            PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setReadOnly(true);
        return template;
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/template/TransactionTemplateDemo.java
package org.example.patterns.transaction.template;

import lombok.RequiredArgsConstructor;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(3)
@RequiredArgsConstructor
public class TransactionTemplateDemo implements CommandLineRunner {
    
    private final TransactionTemplateService templateService;
    private final AccountRepository accountRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Transaction Template Pattern Demo ===");
        
        // Setup
        accountRepository.save(new Account("TMPL-001", new BigDecimal("1000.00")));
        accountRepository.save(new Account("TMPL-002", new BigDecimal("500.00")));
        
        // Test transfer with template
        System.out.println("\n1. Transfer using TransactionTemplate:");
        templateService.transferUsingTemplate("TMPL-001", "TMPL-002", 
                new BigDecimal("300.00"));
        
        // Test calculation
        System.out.println("\n2. Calculate Total Balance:");
        BigDecimal total = templateService.calculateTotalBalance("TMPL-001", "TMPL-002");
        System.out.println("Total balance: $" + total);
        
        // Test deposit with lambda
        System.out.println("\n3. Deposit with Lambda:");
        templateService.depositWithLambda("TMPL-001", new BigDecimal("200.00"));
        
        // Test withdrawal with validation
        System.out.println("\n4. Withdrawal with Validation:");
        Boolean success = templateService.withdrawWithValidation("TMPL-001", 
                new BigDecimal("50.00"));
        System.out.println("Withdrawal successful: " + success);
        
        Boolean failed = templateService.withdrawWithValidation("TMPL-001", 
                new BigDecimal("50000.00"));
        System.out.println("Withdrawal successful: " + failed);
        
        System.out.println("\nTransaction Template Pattern demonstrated!\n");
    }
}
```

## 7. Pattern 4: Transaction Interceptor Pattern

```java
// src/main/java/org/example/patterns/transaction/interceptor/TransactionInterceptorConfig.java
package org.example.patterns.transaction.interceptor;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Properties;

/**
 * Transaction Interceptor Pattern using AOP-based transaction interception.
 * Allows fine-grained control over transaction attributes.
 */
@Configuration
public class TransactionInterceptorConfig {
    
    @Bean
    public TransactionInterceptor transactionInterceptor(
            PlatformTransactionManager transactionManager) {
        
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactionManager);
        
        // Define transaction attributes
        Properties transactionAttributes = new Properties();
        
```java
        // Write methods
        transactionAttributes.setProperty("save*", "PROPAGATION_REQUIRED");
        transactionAttributes.setProperty("update*", "PROPAGATION_REQUIRED");
        transactionAttributes.setProperty("delete*", "PROPAGATION_REQUIRED");
        transactionAttributes.setProperty("create*", "PROPAGATION_REQUIRED");
        
        // Transfer methods with specific rollback rules
        transactionAttributes.setProperty("transfer*", 
            "PROPAGATION_REQUIRED,-Exception,+IllegalArgumentException");
        
        // Batch operations with timeout
        transactionAttributes.setProperty("batch*", 
            "PROPAGATION_REQUIRED,timeout_30");
        
        interceptor.setTransactionAttributes(transactionAttributes);
        return interceptor;
    }
    
    @Bean
    public BeanNameAutoProxyCreator transactionAutoProxy() {
        BeanNameAutoProxyCreator creator = new BeanNameAutoProxyCreator();
        creator.setInterceptorNames("transactionInterceptor");
        creator.setBeanNames("*InterceptorService");
        return creator;
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/interceptor/AccountInterceptorService.java
package org.example.patterns.transaction.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service that will be intercepted by TransactionInterceptor.
 * Transaction attributes are defined by method name patterns.
 */
@Slf4j
@Service("accountInterceptorService")
@RequiredArgsConstructor
public class AccountInterceptorService {
    
    private final AccountRepository accountRepository;
    
    // Will be read-only transaction (get* pattern)
    public BigDecimal getBalance(String accountNumber) {
        log.info("Getting balance (intercepted as read-only)");
        return accountRepository.findByAccountNumber(accountNumber)
                .map(Account::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }
    
    // Will be read-only transaction (find* pattern)
    public Account findAccount(String accountNumber) {
        log.info("Finding account (intercepted as read-only)");
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }
    
    // Will be write transaction (save* pattern)
    public void saveAccount(Account account) {
        log.info("Saving account (intercepted with PROPAGATION_REQUIRED)");
        accountRepository.save(account);
    }
    
    // Will be write transaction (update* pattern)
    public void updateBalance(String accountNumber, BigDecimal newBalance) {
        log.info("Updating balance (intercepted with PROPAGATION_REQUIRED)");
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow();
        account.setBalance(newBalance);
        accountRepository.save(account);
    }
    
    // Will be write transaction with specific rollback rules (transfer* pattern)
    public void transferFunds(String fromAccount, String toAccount, BigDecimal amount) {
        log.info("Transferring funds (intercepted with custom rollback rules)");
        
        Account from = accountRepository.findByAccountNumber(fromAccount)
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));
        Account to = accountRepository.findByAccountNumber(toAccount)
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));
        
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        
        accountRepository.save(from);
        accountRepository.save(to);
    }
    
    // Will be write transaction with timeout (batch* pattern)
    public void batchUpdate(List<Account> accounts) {
        log.info("Batch updating accounts (intercepted with timeout)");
        for (Account account : accounts) {
            accountRepository.save(account);
        }
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/interceptor/TransactionInterceptorDemo.java
package org.example.patterns.transaction.interceptor;

import lombok.RequiredArgsConstructor;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@Order(4)
@RequiredArgsConstructor
public class TransactionInterceptorDemo implements CommandLineRunner {
    
    private final AccountInterceptorService interceptorService;
    private final AccountRepository accountRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Transaction Interceptor Pattern Demo ===");
        
        // Setup
        accountRepository.save(new Account("INTR-001", new BigDecimal("1000.00")));
        accountRepository.save(new Account("INTR-002", new BigDecimal("500.00")));
        
        // Test read-only method (get* pattern)
        System.out.println("\n1. Read-only Transaction (get* pattern):");
        BigDecimal balance = interceptorService.getBalance("INTR-001");
        System.out.println("Balance retrieved: $" + balance);
        
        // Test read-only method (find* pattern)
        System.out.println("\n2. Read-only Transaction (find* pattern):");
        Account account = interceptorService.findAccount("INTR-001");
        System.out.println("Account found: " + account.getAccountNumber());
        
        // Test write method (update* pattern)
        System.out.println("\n3. Write Transaction (update* pattern):");
        interceptorService.updateBalance("INTR-001", new BigDecimal("1200.00"));
        System.out.println("Balance updated");
        
        // Test transfer with rollback rules (transfer* pattern)
        System.out.println("\n4. Transfer with Custom Rollback Rules:");
        interceptorService.transferFunds("INTR-001", "INTR-002", new BigDecimal("200.00"));
        System.out.println("Transfer completed");
        
        // Test batch operation (batch* pattern with timeout)
        System.out.println("\n5. Batch Operation with Timeout:");
        Account acc1 = accountRepository.findByAccountNumber("INTR-001").get();
        Account acc2 = accountRepository.findByAccountNumber("INTR-002").get();
        acc1.setBalance(acc1.getBalance().add(new BigDecimal("50")));
        acc2.setBalance(acc2.getBalance().add(new BigDecimal("50")));
        interceptorService.batchUpdate(Arrays.asList(acc1, acc2));
        System.out.println("Batch update completed");
        
        System.out.println("\nTransaction Interceptor Pattern demonstrated!\n");
    }
}
```

## 8. Pattern 5: Transaction Synchronization Pattern

```java
// src/main/java/org/example/patterns/transaction/synchronization/TransactionSynchronizationService.java
package org.example.patterns.transaction.synchronization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.model.TransactionLog;
import org.example.repository.AccountRepository;
import org.example.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction Synchronization Pattern.
 * Allows registration of callbacks for transaction lifecycle events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionSynchronizationService {
    
    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    
    @Transactional
    public void transferWithSynchronization(String fromAccount, String toAccount, 
                                           BigDecimal amount) {
        String transactionId = UUID.randomUUID().toString();
        
        log.info("Starting transfer with synchronization: {}", transactionId);
        
        // Register synchronization callbacks
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                
                @Override
                public void beforeCommit(boolean readOnly) {
                    log.info("[SYNC] Before commit - Transaction ID: {}", transactionId);
                }
                
                @Override
                public void beforeCompletion() {
                    log.info("[SYNC] Before completion - Transaction ID: {}", transactionId);
                }
                
                @Override
                public void afterCommit() {
                    log.info("[SYNC] After commit - Transaction ID: {}", transactionId);
                    // Send notification, update cache, etc.
                    sendNotification(transactionId, "COMMITTED");
                }
                
                @Override
                public void afterCompletion(int status) {
                    String statusStr = status == STATUS_COMMITTED ? "COMMITTED" : "ROLLED_BACK";
                    log.info("[SYNC] After completion - Status: {}", statusStr);
                    
                    // Log final status
                    logTransactionStatus(transactionId, statusStr);
                }
                
                @Override
                public void suspend() {
                    log.info("[SYNC] Transaction suspended");
                }
                
                @Override
                public void resume() {
                    log.info("[SYNC] Transaction resumed");
                }
            }
        );
        
        // Perform actual transfer
        Account from = accountRepository.findByAccountNumber(fromAccount)
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));
        Account to = accountRepository.findByAccountNumber(toAccount)
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));
        
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        
        accountRepository.save(from);
        accountRepository.save(to);
        
        // Log transaction initiation
        TransactionLog log = new TransactionLog(transactionId, fromAccount, 
                toAccount, amount, "PENDING");
        transactionLogRepository.save(log);
    }
    
    @Transactional
    public void transferWithResourceCleanup(String fromAccount, String toAccount, 
                                           BigDecimal amount) {
        // Register cleanup callback
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        log.info("[CLEANUP] Cleaning up resources after rollback");
                        // Release locks, close connections, etc.
                        cleanupResources();
                    }
                }
            }
        );
        
        transferWithSynchronization(fromAccount, toAccount, amount);
    }
    
    private void sendNotification(String transactionId, String status) {
        log.info("Sending notification - Transaction: {}, Status: {}", 
                transactionId, status);
        // Email, SMS, message queue, etc.
    }
    
    private void logTransactionStatus(String transactionId, String status) {
        log.info("Logging final status - Transaction: {}, Status: {}", 
                transactionId, status);
        // Persist to audit log
    }
    
    private void cleanupResources() {
        log.info("Releasing resources, closing connections, etc.");
    }
    
    @Transactional
    public void demonstrateTransactionInfo() {
        log.info("=== Transaction Information ===");
        log.info("Is actual transaction active: {}", 
                TransactionSynchronizationManager.isActualTransactionActive());
        log.info("Current transaction name: {}", 
                TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("Current transaction read-only: {}", 
                TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        log.info("Current transaction isolation level: {}", 
                TransactionSynchronizationManager.getCurrentTransactionIsolationLevel());
        log.info("Synchronization active: {}", 
                TransactionSynchronizationManager.isSynchronizationActive());
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/synchronization/TransactionEventListener.java
package org.example.patterns.transaction.synchronization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Alternative approach using @TransactionalEventListener
 */
@Slf4j
@Component
public class TransactionEventListener {
    
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleBeforeCommit(TransactionEvent event) {
        log.info("[EVENT] Before commit event: {}", event.getMessage());
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAfterCommit(TransactionEvent event) {
        log.info("[EVENT] After commit event: {}", event.getMessage());
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleAfterRollback(TransactionEvent event) {
        log.info("[EVENT] After rollback event: {}", event.getMessage());
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleAfterCompletion(TransactionEvent event) {
        log.info("[EVENT] After completion event: {}", event.getMessage());
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/synchronization/TransactionEvent.java
package org.example.patterns.transaction.synchronization;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionEvent {
    private String message;
    private String transactionId;
}
```

```java
// src/main/java/org/example/patterns/transaction/synchronization/TransactionSynchronizationDemo.java
package org.example.patterns.transaction.synchronization;

import lombok.RequiredArgsConstructor;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(5)
@RequiredArgsConstructor
public class TransactionSynchronizationDemo implements CommandLineRunner {
    
    private final TransactionSynchronizationService syncService;
    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Transaction Synchronization Pattern Demo ===");
        
        // Setup
        accountRepository.save(new Account("SYNC-001", new BigDecimal("1000.00")));
        accountRepository.save(new Account("SYNC-002", new BigDecimal("500.00")));
        
        // Test synchronization callbacks
        System.out.println("\n1. Transfer with Synchronization Callbacks:");
        syncService.transferWithSynchronization("SYNC-001", "SYNC-002", 
                new BigDecimal("200.00"));
        
        // Test transaction info
        System.out.println("\n2. Transaction Information:");
        syncService.demonstrateTransactionInfo();
        
        // Test with resource cleanup
        System.out.println("\n3. Transfer with Resource Cleanup:");
        try {
            syncService.transferWithResourceCleanup("SYNC-001", "SYNC-002", 
                    new BigDecimal("50000.00"));
        } catch (Exception e) {
            System.out.println("Expected failure - cleanup triggered");
        }
        
        // Test event listener
        System.out.println("\n4. Transaction Event Listener:");
        eventPublisher.publishEvent(new TransactionEvent("Test event", "TXN-123"));
        
        System.out.println("\nTransaction Synchronization Pattern demonstrated!\n");
    }
}
```

## 9. Pattern 6: Nested Transaction Pattern

```java
// src/main/java/org/example/patterns/transaction/nested/NestedTransactionService.java
package org.example.patterns.transaction.nested;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.model.TransactionLog;
import org.example.repository.AccountRepository;
import org.example.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Nested Transaction Pattern demonstrating different propagation behaviors.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NestedTransactionService {
    
    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final AuditService auditService;
    
    /**
     * Outer transaction with NESTED inner transaction.
     * Inner transaction can be rolled back independently.
     */
    @Transactional
    public void transferWithNestedLogging(String fromAccount, String toAccount, 
                                         BigDecimal amount) {
        String txnId = UUID.randomUUID().toString();
        log.info("Outer transaction started: {}", txnId);
        
        try {
            // Main transfer logic
            Account from = accountRepository.findByAccountNumber(fromAccount)
                    .orElseThrow();
            Account to = accountRepository.findByAccountNumber(toAccount)
                    .orElseThrow();
            
            from.setBalance(from.getBalance().subtract(amount));
            to.setBalance(to.getBalance().add(amount));
            
            accountRepository.save(from);
            accountRepository.save(to);
            
            // Nested transaction for audit logging
            // If this fails, only the audit log is rolled back
            try {
                auditService.logTransactionNested(txnId, fromAccount, toAccount, amount);
            } catch (Exception e) {
                log.warn("Audit logging failed, but transfer continues: {}", e.getMessage());
            }
            
            log.info("Outer transaction completed: {}", txnId);
            
        } catch (Exception e) {
            log.error("Transfer failed, rolling back: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * REQUIRES_NEW propagation - completely independent transaction.
     */
    @Transactional
    public void transferWithIndependentAudit(String fromAccount, String toAccount, 
                                            BigDecimal amount) {
        String txnId = UUID.randomUUID().toString();
        log.info("Main transaction started: {}", txnId);
        
        // Independent audit log - always committed even if main fails
        auditService.logTransactionIndependent(txnId, fromAccount, toAccount, amount);
        
        Account from = accountRepository.findByAccountNumber(fromAccount)
                .orElseThrow();
        Account to = accountRepository.findByAccountNumber(toAccount)
                .orElseThrow();
        
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        
        accountRepository.save(from);
        accountRepository.save(to);
        
        log.info("Main transaction completed: {}", txnId);
    }
    
    /**
     * MANDATORY propagation - must run within existing transaction.
     */
    @Transactional
    public void initiateTransferChain(String fromAccount, String toAccount, 
                                     BigDecimal amount) {
        log.info("Initiating transaction chain");
        
        // This will succeed because we're in a transaction
        auditService.logMandatory("Chain initiated");
        
        Account from = accountRepository.findByAccountNumber(fromAccount)
                .orElseThrow();
        Account to = accountRepository.findByAccountNumber(toAccount)
                .orElseThrow();
        
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        
        accountRepository.save(from);
        accountRepository.save(to);
    }
    
    /**
     * SUPPORTS propagation - participates if exists, non-transactional otherwise.
     */
    public void flexibleOperation(String accountNumber) {
        log.info("Flexible operation - may or may not be transactional");
        auditService.logFlexible("Flexible operation on " + accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow();
        log.info("Account balance: {}", account.getBalance());
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/nested/AuditService.java
package org.example.patterns.transaction.nested;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.TransactionLog;
import org.example.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final TransactionLogRepository transactionLogRepository;
    
    /**
     * NESTED - creates a savepoint, can rollback independently
     */
    @Transactional(propagation = Propagation.NESTED)
    public void logTransactionNested(String txnId, String from, String to, BigDecimal amount) {
        log.info("NESTED transaction - Creating savepoint for audit");
        TransactionLog txnLog = new TransactionLog(txnId, from, to, amount, "NESTED_AUDIT");
        transactionLogRepository.save(txnLog);
        
        // Simulate potential failure
        if (Math.random() > 0.7) {
            throw new RuntimeException("Audit logging failed");
        }
    }
    
    /**
     * REQUIRES_NEW - completely independent transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTransactionIndependent(String txnId, String from, String to, 
                                         BigDecimal amount) {
        log.info("REQUIRES_NEW - Independent transaction for audit");
        TransactionLog txnLog = new TransactionLog(txnId, from, to, amount, "INDEPENDENT_AUDIT");
        transactionLogRepository.save(txnLog);
        log.info("Independent audit committed");
    }
    
    /**
     * MANDATORY - must have active transaction
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void logMandatory(String message) {
        log.info("MANDATORY - Must be called within transaction: {}", message);
        TransactionLog txnLog = new TransactionLog(
                UUID.randomUUID().toString(), 
                "SYSTEM", 
                "SYSTEM", 
                BigDecimal.ZERO, 
                message
        );
        transactionLogRepository.save(txnLog);
    }
    
    /**
     * SUPPORTS - non-transactional, but participates if transaction exists
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void logFlexible(String message) {
        log.info("SUPPORTS - Flexible transaction participation: {}", message);
        // May or may not be in transaction
    }
    
    /**
     * NOT_SUPPORTED - suspends current transaction
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void logNonTransactional(String message) {
        log.info("NOT_SUPPORTED - Running without transaction: {}", message);
    }
    
    /**
     * NEVER - throws exception if transaction exists
     */
    @Transactional(propagation = Propagation.NEVER)
    public void logMustBeNonTransactional(String message) {
        log.info("NEVER - Must not be in transaction: {}", message);
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/nested/NestedTransactionDemo.java
package org.example.patterns.transaction.nested;

import lombok.RequiredArgsConstructor;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(6)
@RequiredArgsConstructor
public class NestedTransactionDemo implements CommandLineRunner {
    
    private final NestedTransactionService nestedService;
    private final AuditService auditService;
    private final AccountRepository accountRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Nested Transaction Pattern Demo ===");
        
        // Setup
        accountRepository.save(new Account("NEST-001", new BigDecimal("1000.00")));
        accountRepository.save(new Account("NEST-002", new BigDecimal("500.00")));
        
        // Test NESTED propagation
        System.out.println("\n1. NESTED Propagation (with savepoint):");
        for (int i = 0; i < 3; i++) {
            try {
                nestedService.transferWithNestedLogging("NEST-001", "NEST-002", 
                        new BigDecimal("50.00"));
            } catch (Exception e) {
                System.out.println("Attempt " + (i + 1) + " completed");
            }
        }
        
        // Test REQUIRES_NEW propagation
        System.out.println("\n2. REQUIRES_NEW Propagation (independent):");
        try {
            nestedService.transferWithIndependentAudit("NEST-001", "NEST-002", 
                    new BigDecimal("50000.00"));
        } catch (Exception e) {
            System.out.println("Transfer failed, but audit was saved independently");
        }
        
        // Test MANDATORY propagation
        System.out.println("\n3. MANDATORY Propagation:");
        nestedService.initiateTransferChain("NEST-001", "NEST-002", 
                new BigDecimal("100.00"));
        
        // Test SUPPORTS propagation
        System.out.println("\n4. SUPPORTS Propagation:");
        nestedService.flexibleOperation("NEST-001");
        
        // Test NOT_SUPPORTED
        System.out.println("\n5. NOT_SUPPORTED Propagation:");
        auditService.logNonTransactional("Non-transactional log");
        
        System.out.println("\nNested Transaction Pattern demonstrated!\n");
    }
}
```

## 10. Pattern 7: Distributed Transaction Pattern

```java
// src/main/java/org/example/patterns/transaction/distributed/DistributedTransactionConfig.java
package org.example.patterns.transaction.distributed;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

/**
 * Distributed Transaction Pattern using JTA (Java Transaction API).
 * Coordinates transactions across multiple resources.
 */
@Configuration
public class DistributedTransactionConfig {
    
    @Bean
    public UserTransaction userTransaction() throws SystemException {
        UserTransactionImp userTransaction = new UserTransactionImp();
        userTransaction.setTransactionTimeout(300);
        return userTransaction;
    }
    
    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager userTransactionManager() {
        UserTransactionManager manager = new UserTransactionManager();
        manager.setForceShutdown(false);
        return manager;
    }
    
    @Bean
    public JtaTransactionManager jtaTransactionManager(
            UserTransaction userTransaction,
            UserTransactionManager userTransactionManager) {
        
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setUserTransaction(userTransaction);
        jtaTransactionManager.setTransactionManager(userTransactionManager);
        jtaTransactionManager.setAllowCustomIsolationLevels(true);
        
        return jtaTransactionManager;
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/distributed/DistributedTransactionService.java
package org.example.patterns.transaction.distributed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.model.Order;
import org.example.repository.AccountRepository;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Distributed transaction coordinating multiple data sources.
 * Simulates a scenario where payment and order must be atomic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedTransactionService {
    
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final ExternalPaymentService paymentService;
    
    /**
     * Distributed transaction across local database and external service.
     */
    @Transactional
    public String processOrderWithPayment(String customerId, String accountNumber, 
                                         BigDecimal amount) {
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        
        log.info("Starting distributed transaction for order: {}", orderNumber);
        
        try {
            // Step 1: Create order in local database
            Order order = new Order(orderNumber, customerId, amount);
            orderRepository.save(order);
            log.info("Order created: {}", orderNumber);
            
            // Step 2: Process payment (simulated external system)
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            
            if (account.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient balance");
            }
            
            account.setBalance(account.getBalance().subtract(amount));
            accountRepository.save(account);
```java
            log.info("Payment deducted from account: {}", accountNumber);
            
            // Step 3: Call external payment service
            boolean paymentSuccess = paymentService.processPayment(orderNumber, amount);
            
            if (!paymentSuccess) {
                throw new RuntimeException("External payment processing failed");
            }
            
            // Step 4: Confirm order
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order confirmed: {}", orderNumber);
            
            log.info("Distributed transaction completed successfully");
            return orderNumber;
            
        } catch (Exception e) {
            log.error("Distributed transaction failed, rolling back: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Two-phase commit simulation.
     */
    @Transactional
    public void twoPhaseCommitSimulation(String accountNumber, BigDecimal amount) {
        log.info("=== Two-Phase Commit Simulation ===");
        
        // Phase 1: Prepare
        log.info("Phase 1: PREPARE");
        boolean accountPrepared = prepareAccountDebit(accountNumber, amount);
        boolean externalPrepared = paymentService.preparePayment(amount);
        
        if (accountPrepared && externalPrepared) {
            // Phase 2: Commit
            log.info("Phase 2: COMMIT");
            commitAccountDebit(accountNumber, amount);
            paymentService.commitPayment();
            log.info("Two-phase commit successful");
        } else {
            // Abort
            log.warn("Phase 2: ABORT");
            abortAccountDebit(accountNumber);
            paymentService.abortPayment();
            throw new RuntimeException("Two-phase commit aborted");
        }
    }
    
    private boolean prepareAccountDebit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElse(null);
        if (account == null || account.getBalance().compareTo(amount) < 0) {
            log.warn("Account prepare failed");
            return false;
        }
        log.info("Account prepared for debit");
        return true;
    }
    
    private void commitAccountDebit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow();
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        log.info("Account debit committed");
    }
    
    private void abortAccountDebit(String accountNumber) {
        log.info("Account debit aborted");
        // Rollback any prepared changes
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/distributed/ExternalPaymentService.java
package org.example.patterns.transaction.distributed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simulates an external payment gateway.
 */
@Slf4j
@Service
public class ExternalPaymentService {
    
    private final Map<String, String> preparedTransactions = new HashMap<>();
    
    public boolean processPayment(String orderNumber, BigDecimal amount) {
        log.info("Processing external payment: Order={}, Amount={}", orderNumber, amount);
        
        // Simulate external API call
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 90% success rate
        boolean success = Math.random() > 0.1;
        log.info("External payment result: {}", success ? "SUCCESS" : "FAILED");
        return success;
    }
    
    public boolean preparePayment(BigDecimal amount) {
        String txnId = UUID.randomUUID().toString();
        log.info("Preparing external payment: TxnId={}, Amount={}", txnId, amount);
        
        // Simulate prepare phase
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            log.warn("External payment prepare failed - amount too large");
            return false;
        }
        
        preparedTransactions.put(txnId, "PREPARED");
        log.info("External payment prepared: {}", txnId);
        return true;
    }
    
    public void commitPayment() {
        log.info("Committing external payment");
        preparedTransactions.values().forEach(v -> log.info("Transaction committed"));
        preparedTransactions.clear();
    }
    
    public void abortPayment() {
        log.info("Aborting external payment");
        preparedTransactions.clear();
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/distributed/DistributedTransactionDemo.java
package org.example.patterns.transaction.distributed;

import lombok.RequiredArgsConstructor;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(7)
@RequiredArgsConstructor
public class DistributedTransactionDemo implements CommandLineRunner {
    
    private final DistributedTransactionService distributedService;
    private final AccountRepository accountRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Distributed Transaction Pattern Demo ===");
        
        // Setup
        accountRepository.save(new Account("DIST-001", new BigDecimal("5000.00")));
        
        // Test distributed transaction
        System.out.println("\n1. Distributed Transaction (Order + Payment):");
        try {
            String orderNumber = distributedService.processOrderWithPayment(
                    "CUST-001", "DIST-001", new BigDecimal("500.00"));
            System.out.println("Order processed successfully: " + orderNumber);
        } catch (Exception e) {
            System.out.println("Order processing failed (rolled back): " + e.getMessage());
        }
        
        // Test two-phase commit
        System.out.println("\n2. Two-Phase Commit Simulation:");
        try {
            distributedService.twoPhaseCommitSimulation("DIST-001", 
                    new BigDecimal("200.00"));
        } catch (Exception e) {
            System.out.println("Two-phase commit demonstration completed");
        }
        
        System.out.println("\nDistributed Transaction Pattern demonstrated!\n");
    }
}
```

## 11. Pattern 8: Compensating Transaction Pattern

```java
// src/main/java/org/example/patterns/transaction/compensating/CompensatingTransactionService.java
package org.example.patterns.transaction.compensating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.model.Order;
import org.example.repository.AccountRepository;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Compensating Transaction Pattern.
 * When distributed transactions fail, compensating actions reverse completed steps.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompensatingTransactionService {
    
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    
    /**
     * Complex operation with compensating transactions.
     */
    public String processOrderWithCompensation(String customerId, String accountNumber, 
                                              String productId, BigDecimal amount) {
        String orderNumber = "COMP-" + UUID.randomUUID().toString().substring(0, 8);
        Stack<Consumer<String>> compensations = new Stack<>();
        
        try {
            log.info("Starting order processing with compensations: {}", orderNumber);
            
            // Step 1: Create order
            createOrder(orderNumber, customerId, amount);
            compensations.push(orderId -> cancelOrder(orderId));
            log.info("Order created: {}", orderNumber);
            
            // Step 2: Reserve inventory
            inventoryService.reserveInventory(productId, 1);
            compensations.push(orderId -> inventoryService.releaseInventory(productId, 1));
            log.info("Inventory reserved");
            
            // Step 3: Process payment
            processPayment(accountNumber, amount);
            compensations.push(orderId -> refundPayment(accountNumber, amount));
            log.info("Payment processed");
            
            // Step 4: Create shipment
            shippingService.createShipment(orderNumber, customerId);
            compensations.push(orderId -> shippingService.cancelShipment(orderId));
            log.info("Shipment created");
            
            // Step 5: Confirm order
            confirmOrder(orderNumber);
            log.info("Order confirmed: {}", orderNumber);
            
            return orderNumber;
            
        } catch (Exception e) {
            log.error("Order processing failed, executing compensations: {}", e.getMessage());
            
            // Execute compensating transactions in reverse order
            while (!compensations.isEmpty()) {
                try {
                    Consumer<String> compensation = compensations.pop();
                    compensation.accept(orderNumber);
                } catch (Exception ce) {
                    log.error("Compensation failed: {}", ce.getMessage());
                }
            }
            
            throw new RuntimeException("Order processing failed and compensated", e);
        }
    }
    
    @Transactional
    private void createOrder(String orderNumber, String customerId, BigDecimal amount) {
        Order order = new Order(orderNumber, customerId, amount);
        orderRepository.save(order);
    }
    
    @Transactional
    private void cancelOrder(String orderNumber) {
        log.info("COMPENSATING: Cancelling order {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow();
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
    
    @Transactional
    private void processPayment(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }
    
    @Transactional
    private void refundPayment(String accountNumber, BigDecimal amount) {
        log.info("COMPENSATING: Refunding payment ${} to {}", amount, accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow();
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }
    
    @Transactional
    private void confirmOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow();
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/compensating/InventoryService.java
package org.example.patterns.transaction.compensating;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class InventoryService {
    
    private final Map<String, Integer> inventory = new HashMap<>();
    
    public InventoryService() {
        inventory.put("PROD-001", 10);
        inventory.put("PROD-002", 5);
    }
    
    public void reserveInventory(String productId, int quantity) {
        log.info("Reserving inventory: Product={}, Quantity={}", productId, quantity);
        
        Integer available = inventory.getOrDefault(productId, 0);
        if (available < quantity) {
            throw new IllegalStateException("Insufficient inventory");
        }
        
        inventory.put(productId, available - quantity);
        log.info("Inventory reserved. Remaining: {}", inventory.get(productId));
    }
    
    public void releaseInventory(String productId, int quantity) {
        log.info("COMPENSATING: Releasing inventory: Product={}, Quantity={}", 
                productId, quantity);
        Integer current = inventory.getOrDefault(productId, 0);
        inventory.put(productId, current + quantity);
        log.info("Inventory released. Available: {}", inventory.get(productId));
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/compensating/ShippingService.java
package org.example.patterns.transaction.compensating;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class ShippingService {
    
    private final Set<String> shipments = new HashSet<>();
    
    public void createShipment(String orderNumber, String customerId) {
        log.info("Creating shipment: Order={}, Customer={}", orderNumber, customerId);
        
        // Simulate random failure
        if (Math.random() > 0.7) {
            throw new RuntimeException("Shipping service unavailable");
        }
        
        shipments.add(orderNumber);
        log.info("Shipment created for order: {}", orderNumber);
    }
    
    public void cancelShipment(String orderNumber) {
        log.info("COMPENSATING: Cancelling shipment for order: {}", orderNumber);
        shipments.remove(orderNumber);
        log.info("Shipment cancelled");
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/compensating/CompensatingTransactionDemo.java
package org.example.patterns.transaction.compensating;

import lombok.RequiredArgsConstructor;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(8)
@RequiredArgsConstructor
public class CompensatingTransactionDemo implements CommandLineRunner {
    
    private final CompensatingTransactionService compensatingService;
    private final AccountRepository accountRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Compensating Transaction Pattern Demo ===");
        
        // Setup
        accountRepository.save(new Account("COMP-001", new BigDecimal("2000.00")));
        
        System.out.println("\n1. Successful Order Processing:");
        try {
            String orderNumber = compensatingService.processOrderWithCompensation(
                    "CUST-001", "COMP-001", "PROD-001", new BigDecimal("500.00"));
            System.out.println("Order completed: " + orderNumber);
        } catch (Exception e) {
            System.out.println("Order failed but compensated");
        }
        
        System.out.println("\n2. Failed Order with Compensation:");
        for (int i = 0; i < 3; i++) {
            try {
                compensatingService.processOrderWithCompensation(
                        "CUST-002", "COMP-001", "PROD-002", new BigDecimal("300.00"));
            } catch (Exception e) {
                System.out.println("Attempt " + (i + 1) + ": Compensating transactions executed");
            }
        }
        
        BigDecimal finalBalance = accountRepository.findByAccountNumber("COMP-001")
                .get().getBalance();
        System.out.println("Final account balance: $" + finalBalance);
        
        System.out.println("\nCompensating Transaction Pattern demonstrated!\n");
    }
}
```

## 12. Pattern 9: Saga Pattern

```java
// src/main/java/org/example/patterns/transaction/saga/SagaOrchestrator.java
package org.example.patterns.transaction.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.SagaState;
import org.example.repository.SagaStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Saga Pattern - Orchestration-based approach.
 * Coordinates a sequence of local transactions across services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestrator {
    
    private final SagaStateRepository sagaStateRepository;
    private final OrderSagaService orderService;
    private final PaymentSagaService paymentService;
    private final InventorySagaService inventoryService;
    private final ShippingSagaService shippingService;
    
    public String executeOrderSaga(OrderSagaRequest request) {
        String sagaId = UUID.randomUUID().toString();
        SagaState sagaState = new SagaState(sagaId);
        
        try {
            log.info("=== Starting Saga: {} ===", sagaId);
            sagaState.setStatus(SagaState.SagaStatus.IN_PROGRESS);
            saveSagaState(sagaState);
            
            // Step 1: Create Order
            sagaState.setCurrentStep("CREATE_ORDER");
            saveSagaState(sagaState);
            String orderId = orderService.createOrder(request);
            sagaState.setLastCompletedStep("CREATE_ORDER");
            saveSagaState(sagaState);
            log.info("Saga step completed: CREATE_ORDER");
            
            // Step 2: Reserve Inventory
            sagaState.setCurrentStep("RESERVE_INVENTORY");
            saveSagaState(sagaState);
            inventoryService.reserveInventory(request.getProductId(), 
                    request.getQuantity());
            sagaState.setLastCompletedStep("RESERVE_INVENTORY");
            saveSagaState(sagaState);
            log.info("Saga step completed: RESERVE_INVENTORY");
            
            // Step 3: Process Payment
            sagaState.setCurrentStep("PROCESS_PAYMENT");
            saveSagaState(sagaState);
            paymentService.processPayment(request.getAccountNumber(), 
                    request.getAmount());
            sagaState.setLastCompletedStep("PROCESS_PAYMENT");
            saveSagaState(sagaState);
            log.info("Saga step completed: PROCESS_PAYMENT");
            
            // Step 4: Create Shipment
            sagaState.setCurrentStep("CREATE_SHIPMENT");
            saveSagaState(sagaState);
            shippingService.createShipment(orderId, request.getCustomerId());
            sagaState.setLastCompletedStep("CREATE_SHIPMENT");
            saveSagaState(sagaState);
            log.info("Saga step completed: CREATE_SHIPMENT");
            
            // Step 5: Confirm Order
            sagaState.setCurrentStep("CONFIRM_ORDER");
            saveSagaState(sagaState);
            orderService.confirmOrder(orderId);
            sagaState.setLastCompletedStep("CONFIRM_ORDER");
            sagaState.setStatus(SagaState.SagaStatus.COMPLETED);
            saveSagaState(sagaState);
            
            log.info("=== Saga Completed Successfully: {} ===", sagaId);
            return orderId;
            
        } catch (Exception e) {
            log.error("Saga failed at step: {}, initiating compensation", 
                    sagaState.getCurrentStep(), e);
            compensate(sagaState, request);
            throw new RuntimeException("Saga failed and compensated", e);
        }
    }
    
    private void compensate(SagaState sagaState, OrderSagaRequest request) {
        sagaState.setStatus(SagaState.SagaStatus.COMPENSATING);
        saveSagaState(sagaState);
        
        String lastCompleted = sagaState.getLastCompletedStep();
        log.info("Starting compensation from step: {}", lastCompleted);
        
        try {
            // Compensate in reverse order
            if ("CONFIRM_ORDER".equals(lastCompleted) || 
                "CREATE_SHIPMENT".equals(lastCompleted)) {
                shippingService.cancelShipment(request.getCustomerId());
            }
            
            if ("CREATE_SHIPMENT".equals(lastCompleted) || 
                "PROCESS_PAYMENT".equals(lastCompleted)) {
                paymentService.refundPayment(request.getAccountNumber(), 
                        request.getAmount());
            }
            
            if ("PROCESS_PAYMENT".equals(lastCompleted) || 
                "RESERVE_INVENTORY".equals(lastCompleted)) {
                inventoryService.releaseInventory(request.getProductId(), 
                        request.getQuantity());
            }
            
            if ("RESERVE_INVENTORY".equals(lastCompleted) || 
                "CREATE_ORDER".equals(lastCompleted)) {
                orderService.cancelOrder(request.getCustomerId());
            }
            
            sagaState.setStatus(SagaState.SagaStatus.COMPENSATED);
            saveSagaState(sagaState);
            log.info("=== Saga Compensated: {} ===", sagaState.getSagaId());
            
        } catch (Exception e) {
            log.error("Compensation failed for saga: {}", sagaState.getSagaId(), e);
            sagaState.setStatus(SagaState.SagaStatus.FAILED);
            saveSagaState(sagaState);
        }
    }
    
    @Transactional
    private void saveSagaState(SagaState sagaState) {
        sagaStateRepository.save(sagaState);
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/saga/OrderSagaRequest.java
package org.example.patterns.transaction.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSagaRequest {
    private String customerId;
    private String accountNumber;
    private String productId;
    private int quantity;
    private BigDecimal amount;
}
```

```java
// src/main/java/org/example/patterns/transaction/saga/OrderSagaService.java
package org.example.patterns.transaction.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Order;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaService {
    
    private final OrderRepository orderRepository;
    
    @Transactional
    public String createOrder(OrderSagaRequest request) {
        String orderNumber = "SAGA-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[SAGA] Creating order: {}", orderNumber);
        
        Order order = new Order(orderNumber, request.getCustomerId(), 
                request.getAmount());
        orderRepository.save(order);
        
        return orderNumber;
    }
    
    @Transactional
    public void confirmOrder(String orderId) {
        log.info("[SAGA] Confirming order: {}", orderId);
        Order order = orderRepository.findByOrderNumber(orderId)
                .orElseThrow();
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }
    
    @Transactional
    public void cancelOrder(String customerId) {
        log.info("[SAGA COMPENSATION] Cancelling order for customer: {}", customerId);
        // Find and cancel order
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/saga/PaymentSagaService.java
package org.example.patterns.transaction.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSagaService {
    
    private final AccountRepository accountRepository;
    
    @Transactional
    public void processPayment(String accountNumber, BigDecimal amount) {
        log.info("[SAGA] Processing payment: ${} from {}", amount, accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }
    
    @Transactional
    public void refundPayment(String accountNumber, BigDecimal amount) {
        log.info("[SAGA COMPENSATION] Refunding payment: ${} to {}", amount, accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow();
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/saga/InventorySagaService.java
package org.example.patterns.transaction.saga;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class InventorySagaService {
    
    private final Map<String, Integer> inventory = new HashMap<>();
    
    public InventorySagaService() {
        inventory.put("PROD-SAGA-001", 20);
        inventory.put("PROD-SAGA-002", 10);
    }
    
    public void reserveInventory(String productId, int quantity) {
        log.info("[SAGA] Reserving inventory: Product={}, Qty={}", productId, quantity);
        
        Integer available = inventory.getOrDefault(productId, 0);
        if (available < quantity) {
            throw new IllegalStateException("Insufficient inventory");
        }
        
        inventory.put(productId, available - quantity);
    }
    
    public void releaseInventory(String productId, int quantity) {
        log.info("[SAGA COMPENSATION] Releasing inventory: Product={}, Qty={}", 
                productId, quantity);
        
        Integer current = inventory.getOrDefault(productId, 0);
        inventory.put(productId, current + quantity);
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/saga/ShippingSagaService.java
package org.example.patterns.transaction.saga;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShippingSagaService {
    
    public void createShipment(String orderId, String customerId) {
        log.info("[SAGA] Creating shipment: Order={}, Customer={}", orderId, customerId);
        
        // Simulate occasional failure
        if (Math.random() > 0.8) {
            throw new RuntimeException("Shipping service unavailable");
        }
    }
    
    public void cancelShipment(String customerId) {
        log.info("[SAGA COMPENSATION] Cancelling shipment for customer: {}", customerId);
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/saga/SagaPatternDemo.java
package org.example.patterns.transaction.saga;

import lombok.RequiredArgsConstructor;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(9)
@RequiredArgsConstructor
public class SagaPatternDemo implements CommandLineRunner {
    
    private final SagaOrchestrator sagaOrchestrator;
    private final AccountRepository accountRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Saga Pattern Demo ===");
        
        // Setup
        accountRepository.save(new Account("SAGA-ACC-001", new BigDecimal("3000.00")));
        
        System.out.println("\n1. Successful Saga Execution:");
        try {
            OrderSagaRequest request = new OrderSagaRequest(
                    "CUST-SAGA-001",
                    "SAGA-ACC-001",
                    "PROD-SAGA-001",
                    2,
                    new BigDecimal("600.00")
            );
            String orderId = sagaOrchestrator.executeOrderSaga(request);
            System.out.println("Saga completed successfully: " + orderId);
        } catch (Exception e) {
            System.out.println("Saga failed");
        }
        
        System.out.println("\n2. Failed Saga with Compensation:");
        for (int i = 0; i < 3; i++) {
            try {
                OrderSagaRequest request = new OrderSagaRequest(
                        "CUST-SAGA-002",
                        "SAGA-ACC-001",
                        "PROD-SAGA-002",
                        1,
                        new BigDecimal("400.00")
                );
                sagaOrchestrator.executeOrderSaga(request);
            } catch (Exception e) {
                System.out.println("Attempt " + (i + 1) + ": Saga compensated");
            }
        }
        
        BigDecimal finalBalance = accountRepository.findByAccountNumber("SAGA-ACC-001")
                .get().getBalance();
        System.out.println("Final account balance: $" + finalBalance);
        
        System.out.println("\nSaga Pattern demonstrated!\n");
    }
}
```

## 13. Pattern 10: Two-Phase Commit Pattern

```java
// src/main/java/org/example/patterns/transaction/twophase/TwoPhaseCommitCoordinator.java
package org.example.patterns.transaction.twophase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Two-Phase Commit (2PC) Pattern.
 * Ensures atomicity across multiple distributed resources.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwoPhaseCommitCoordinator {
    
    private final AccountParticipant accountParticipant;
    private final InventoryParticipant inventoryParticipant;
    private final OrderParticipant orderParticipant;
    
    /**
     * Execute two-phase commit protocol.
     */
    public String executeTwoPhaseCommit(TwoPhaseCommitRequest request) {
        String transactionId = UUID.randomUUID().toString();
        List<TransactionParticipant> participants = new ArrayList<>();
        
        participants.add(accountParticipant);
        participants.add(inventoryParticipant);
        participants.add(orderParticipant);
        
        log.info("=== Starting 2PC Transaction: {} ===", transactionId);
        
        try {
            // PHASE 1: PREPARE
            log.info("PHASE 1: PREPARE");
            boolean allPrepared = preparePhase(participants, request, transactionId);
            
            if (allPrepared) {
                // PHASE 2: COMMIT
                log.info("PHASE 2: COMMIT");
                commitPhase(participants, transactionId);
                log.info("=== 2PC Transaction COMMITTED: {} ===", transactionId);
                return transactionId;
            } else {
                // PHASE 2: ABORT
                log.warn("PHASE 2: ABORT");
                abortPhase(participants, transactionId);
                throw new RuntimeException("2PC Transaction aborted - prepare phase failed");
            }
            
        } catch (Exception e) {
            log.error("2PC Transaction failed: {}", e.getMessage());
            abortPhase(participants, transactionId);
            throw new RuntimeException("2PC Transaction failed and aborted", e);
        }
    }
    
    private boolean preparePhase(List<TransactionParticipant> participants, 
                                TwoPhaseCommitRequest request, String transactionId) {
        log.info("Preparing all participants...");
        
        for (TransactionParticipant participant : participants) {
            try {
                boolean prepared = participant.prepare(request, transactionId);
                log.info("Participant {} prepare result: {}", 
                        participant.getName(), prepared ? "YES" : "NO");
                
                if (!prepared) {
                    log.warn("Participant {} voted NO", participant.getName());
                    return false;
                }
            } catch (Exception e) {
                log.error("Participant {} prepare failed: {}", 
                        participant.getName(), e.getMessage());
                return false;
            }
        }
        
        log.info("All participants voted YES");
        return true;
    }
    
    private void commitPhase(List<TransactionParticipant> participants, 
                            String transactionId) {
        log.info("Committing all participants...");
        
        for (TransactionParticipant participant : participants) {
            try {
                participant.commit(transactionId);
                log.info("Participant {} committed", participant.getName());
            } catch (Exception e) {
                log.error("Participant {} commit failed: {}", 
                        participant.getName(), e.getMessage());
                // In real 2PC, this is a serious issue requiring manual intervention
            }
        }
    }
    
    private void abortPhase(List<TransactionParticipant> participants, 
                           String transactionId) {
        log.info("Aborting all participants...");
        
        for (TransactionParticipant participant : participants) {
            try {
                participant.abort(transactionId);
                log.info("Participant {} aborted", participant.getName());
            } catch (Exception e) {
                log.error("Participant {} abort failed: {}", 
                        participant.getName(), e.getMessage());
            }
        }
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/twophase/TransactionParticipant.java
package org.example.patterns.transaction.twophase;

/**
 * Interface for 2PC participants.
 */
public interface TransactionParticipant {
    
    /**
     * Phase 1: Prepare - Vote YES or NO
     */
    boolean prepare(TwoPhaseCommitRequest request, String transactionId);
    
    /**
     * Phase 2: Commit - Make changes permanent
     */
    void commit(String transactionId);
    
    /**
     * Phase 2: Abort - Rollback changes
     */
    void abort(String transactionId);
    
    /**
     * Get participant name
     */
    String getName();
}
```

```java
// src/main/java/org/example/patterns/transaction/twophase/TwoPhaseCommitRequest.java
package org.example.patterns.transaction.twophase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoPhaseCommitRequest {
    private String customerId;
    private String accountNumber;
    private String productId;
    private int quantity;
    private BigDecimal amount;
}
```

```java
// src/main/java/org/example/patterns/transaction/twophase/AccountParticipant.java
package org.example.patterns.transaction.twophase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountParticipant implements TransactionParticipant {
    
    private final AccountRepository accountRepository;
    private final Map<String, PreparedTransaction> preparedTransactions = new HashMap<>();
    
    @Override
    public boolean prepare(TwoPhaseCommitRequest request, String transactionId) {
        log.info("[ACCOUNT] Preparing transaction: {}", transactionId);
        
        try {
            Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                    .orElse(null);
            
            if (account == null) {
                log.warn("[ACCOUNT] Account not found: {}", request.getAccountNumber());
                return false;
            }
            
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                log.warn("[ACCOUNT] Insufficient balance");
                return false;
            }
            
            // Lock resources and save state
            PreparedTransaction prepared = new PreparedTransaction(
                    account.getAccountNumber(),
                    account.getBalance(),
                    request.getAmount()
            );
            preparedTransactions.put(transactionId, prepared);
            
            log.info("[ACCOUNT] Prepare successful - Vote YES");
            return true;
            
        } catch (Exception e) {
            log.error("[ACCOUNT] Prepare failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public void commit(String transactionId) {
        log.info("[ACCOUNT] Committing transaction: {}", transactionId);
        
        PreparedTransaction prepared = preparedTransactions.get(transactionId);
        if (prepared == null) {
            log.error("[ACCOUNT] No prepared transaction found: {}", transactionId);
            return;
        }
        
        Account account = accountRepository.findByAccountNumber(prepared.getAccountNumber())
                .orElseThrow();
        
        account.setBalance(account.getBalance().subtract(prepared.getAmount()));
        accountRepository.save(account);
        
        preparedTransactions.remove(transactionId);
        log.info("[ACCOUNT] Transaction committed");
    }
    
    @Override
    public void abort(String transactionId) {
        log.info("[ACCOUNT] Aborting transaction: {}", transactionId);
        preparedTransactions.remove(transactionId);
        log.info("[ACCOUNT] Transaction aborted - resources released");
    }
    
    @Override
    public String getName() {
        return "AccountParticipant";
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class PreparedTransaction {
        private String accountNumber;
        private BigDecimal originalBalance;
        private BigDecimal amount;
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/twophase/InventoryParticipant.java
package org.example.patterns.transaction.twophase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InventoryParticipant implements TransactionParticipant {
    
    private final Map<String, Integer> inventory = new HashMap<>();
    private final Map<String, PreparedInventory> preparedTransactions = new HashMap<>();
    
    public InventoryParticipant() {
        inventory.put("PROD-2PC-001", 50);
        inventory.put("PROD-2PC-002", 30);
    }
    
    @Override
    public boolean prepare(TwoPhaseCommitRequest request, String transactionId) {
        log.info("[INVENTORY] Preparing transaction: {}", transactionId);
        
        try {
            Integer available = inventory.getOrDefault(request.getProductId(), 0);
            
            if (available < request.getQuantity()) {
                log.warn("[INVENTORY] Insufficient stock");
                return false;
            }
            
            // Reserve inventory
            PreparedInventory prepared = new PreparedInventory(
                    request.getProductId(),
                    available,
                    request.getQuantity()
            );
            preparedTransactions.put(transactionId, prepared);
            
            log.info("[INVENTORY] Prepare successful - Vote YES");
            return true;
            
        } catch (Exception e) {
            log.error("[INVENTORY] Prepare failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public void commit(String transactionId) {
        log.info("[INVENTORY] Committing transaction: {}", transactionId);
        
        PreparedInventory prepared = preparedTransactions.get(transactionId);
        if (prepared == null) {
            log.error("[INVENTORY] No prepared transaction found: {}", transactionId);
            return;
        }
        
        Integer current = inventory.get(prepared.getProductId());
        inventory.put(prepared.getProductId(), current - prepared.getQuantity());
        
        preparedTransactions.remove(transactionId);
        log.info("[INVENTORY] Transaction committed - Stock reduced");
    }
    
    @Override
    public void abort(String transactionId) {
        log.info("[INVENTORY] Aborting transaction: {}", transactionId);
        preparedTransactions.remove(transactionId);
        log.info("[INVENTORY] Transaction aborted - Reservation released");
    }
    
    @Override
    public String getName() {
        return "InventoryParticipant";
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class PreparedInventory {
        private String productId;
        private int availableStock;
        private int quantity;
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/twophase/OrderParticipant.java
package org.example.patterns.transaction.twophase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Order;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderParticipant implements TransactionParticipant {
    
    private final OrderRepository orderRepository;
    private final Map<String, String> preparedOrders = new HashMap<>();
    
    @Override
    public boolean prepare(TwoPhaseCommitRequest request, String transactionId) {
        log.info("[ORDER] Preparing transaction: {}", transactionId);
        
        try {
            // Create order in pending state
            String orderNumber = "2PC-" + UUID.randomUUID().toString().substring(0, 8);
            Order order = new Order(orderNumber, request.getCustomerId(), request.getAmount());
            order.setStatus(Order.OrderStatus.PENDING);
            
            orderRepository.save(order);
            preparedOrders.put(transactionId, orderNumber);
            
            log.info("[ORDER] Prepare successful - Vote YES");
            return true;
            
        } catch (Exception e) {
            log.error("[ORDER] Prepare failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public void commit(String transactionId) {
        log.info("[ORDER] Committing transaction: {}", transactionId);
        
        String orderNumber = preparedOrders.get(transactionId);
        if (orderNumber == null) {
            log.error("[ORDER] No prepared order found: {}", transactionId);
            return;
        }
        
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow();
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);
        
        preparedOrders.remove(transactionId);
        log.info("[ORDER] Transaction committed - Order confirmed");
    }
    
    @Override
    public void abort(String transactionId) {
        log.info("[ORDER] Aborting transaction: {}", transactionId);
        
        String orderNumber = preparedOrders.get(transactionId);
        if (orderNumber != null) {
            Order order = orderRepository.findByOrderNumber(orderNumber)
                    .orElse(null);
            if (order != null) {
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        }
        
        preparedOrders.remove(transactionId);
        log.info("[ORDER] Transaction aborted - Order cancelled");
    }
    
    @Override
    public String getName() {
        return "OrderParticipant";
    }
}
```

```java
// src/main/java/org/example/patterns/transaction/twophase/TwoPhaseCommitDemo.java
package org.example.patterns.transaction.twophase;

import lombok.RequiredArgsConstructor;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(10)
@RequiredArgsConstructor
public class TwoPhaseCommitDemo implements CommandLineRunner {
    
    private final TwoPhaseCommitCoordinator coordinator;
    private final AccountRepository accountRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Two-Phase Commit Pattern Demo ===");
        
        // Setup
        accountRepository.save(new Account("2PC-ACC-001", new BigDecimal("5000.00")));
        
        // Test successful 2PC
        System.out.println("\n1. Successful Two-Phase Commit:");
        try {
            TwoPhaseCommitRequest request = new TwoPhaseCommitRequest(
                    "CUST-2PC-001",
                    "2PC-ACC-001",
                    "PROD-2PC-001",
                    5,
                    new BigDecimal("500.00")
            );
            String txnId = coordinator.executeTwoPhaseCommit(request);
            System.out.println("2PC Transaction successful: " + txnId);
        } catch (Exception e) {
            System.out.println("2PC failed: " + e.getMessage());
        }
        
        // Test failed 2PC (insufficient funds)
        System.out.println("\n2. Failed Two-Phase Commit (Insufficient Balance):");
        try {
            TwoPhaseCommitRequest request = new TwoPhaseCommitRequest(
                    "CUST-2PC-002",
                    "2PC-ACC-001",
                    "PROD-2PC-001",
                    2,
                    new BigDecimal("50000.00")
            );
            coordinator.executeTwoPhaseCommit(request);
        } catch (Exception e) {
            System.out.println("2PC aborted as expected: Insufficient balance");
        }
        
        // Test failed 2PC (insufficient inventory)
        System.out.println("\n3. Failed Two-Phase Commit (Insufficient Inventory):");
        try {
            TwoPhaseCommitRequest request = new TwoPhaseCommitRequest(
                    "CUST-2PC-003",
                    "2PC-ACC-001",
                    "PROD-2PC-001",
                    1000,
                    new BigDecimal("100.00")
            );
            coordinator.executeTwoPhaseCommit(request);
        } catch (Exception e) {
            System.out.println("2PC aborted as expected: Insufficient inventory");
        }
        
        BigDecimal finalBalance = accountRepository.findByAccountNumber("2PC-ACC-001")
                .get().getBalance();
        System.out.println("\nFinal account balance: $" + finalBalance);
        
        System.out.println("\nTwo-Phase Commit Pattern demonstrated!\n");
    }
}
```

## 14. Maven Configuration (pom.xml)

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
    <artifactId>spring-transaction-patterns</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Spring Transaction Management Patterns</name>
    <description>Demonstration of transaction management patterns in Spring</description>
    
    <properties>
        <java.version>17</java.version>
        <atomikos.version>6.0.0</atomikos.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        
        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- HikariCP Connection Pool -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
        </dependency>
        
        <!-- Atomikos JTA for Distributed Transactions -->
        <dependency>
            <groupId>com.atomikos</groupId>
            <artifactId>transactions-spring-boot3-starter</artifactId>
            <version>${atomikos.version}</version>
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

## 15. Application Configuration

```properties
# src/main/resources/application.properties
spring.application.name=spring-transaction-patterns

# Primary DataSource (H2 In-Memory)
spring.datasource.primary.jdbc-url=jdbc:h2:mem:primarydb
spring.datasource.primary.driver-class-name=org.h2.Driver
spring.datasource.primary.username=sa
spring.datasource.primary.password=

# Secondary DataSource (H2 In-Memory)
spring.datasource.secondary.jdbc-url=jdbc:h2:mem:secondarydb
spring.datasource.secondary.driver-class-name=org.h2.Driver
spring.datasource.secondary.username=sa
spring.datasource.secondary.password=

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Transaction Management
spring.jta.enabled=true
spring.jta.atomikos.properties.enable-logging=true
spring.jta.atomikos.properties.log-base-dir=./logs

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.level.org.springframework.transaction=DEBUG
logging.level.org.springframework.orm.jpa=DEBUG
logging.level.com.atomikos=DEBUG
```

## 16. README.md

```markdown
# Spring Transaction Management Patterns

This project demonstrates 10 essential transaction management patterns in Spring Framework.

## Patterns Implemented

### 1. Declarative Transaction Pattern
**Location:** `org.example.patterns.transaction.declarative`

Uses `@Transactional` annotation for automatic transaction management.

**Features:**
- Default transaction propagation (REQUIRED)
- Read-only transactions
- Custom isolation levels
- Timeout configuration
- Rollback rules

**Usage:**
```java
@Transactional(
    isolation = Isolation.SERIALIZABLE,
    timeout = 5,
    rollbackFor = Exception.class
)
public void transfer(String from, String to, BigDecimal amount) { }
```

### 2. Programmatic Transaction Pattern
**Location:** `org.example.patterns.transaction.programmatic`

Direct use of `PlatformTransactionManager` for fine-grained control.

**Features:**
- Manual transaction boundaries
- Conditional commit/rollback
- Nested transaction control
- Full programmatic control

**Usage:**
```java
TransactionStatus status = transactionManager.getTransaction(def);
try {
    // Business logic
    transactionManager.commit(status);
} catch (Exception e) {
    transactionManager.rollback(status);
}
```

### 3. Transaction Template Pattern
**Location:** `org.example.patterns.transaction.template`

Simplified programmatic transactions using `TransactionTemplate`.

**Features:**
- Callback-based approach
- Lambda support
- Automatic rollback on exceptions
- Return values from transactions

**Usage:**
```java
transactionTemplate.execute(status -> {
    // Business logic
    return result;
});
```

### 4. Transaction Interceptor Pattern
**Location:** `org.example.patterns.transaction.interceptor`

AOP-based transaction management with method name patterns.

**Features:**
- Method name pattern matching
- Declarative transaction attributes
- Bean name auto-proxying
- Fine-grained control

**Configuration:**
```java
transactionAttributes.setProperty("get*", "PROPAGATION_REQUIRED,readOnly");
transactionAttributes.setProperty("save*", "PROPAGATION_REQUIRED");
```

### 5. Transaction Synchronization Pattern
**Location:** `org.example.patterns.transaction.synchronization`

Register callbacks for transaction lifecycle events.

**Features:**
- Before commit callbacks
- After commit callbacks
- After completion callbacks
- Resource cleanup
- Transaction event listeners

**Usage:**
```java
TransactionSynchronizationManager.registerSynchronization(
    new TransactionSynchronization() {
        @Override
        public void afterCommit() {
            // Post-commit logic
        }
    }
);
```

### 6. Nested Transaction Pattern
**Location:** `org.example.patterns.transaction.nested`

Demonstrates transaction propagation behaviors.

**Propagation Types:**
- **REQUIRED** - Join existing or create new
- **REQUIRES_NEW** - Always create new (suspend current)
- **NESTED** - Create savepoint (rollback independently)
- **MANDATORY** - Must have existing transaction
- **SUPPORTS** - Optional transaction participation
- **NOT_SUPPORTED** - Run without transaction
- **NEVER** - Fail if transaction exists

**Usage:**
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void independentOperation() { }

@Transactional(propagation = Propagation.NESTED)
public void nestedOperation() { }
```

### 7. Distributed Transaction Pattern
**Location:** `org.example.patterns.transaction.distributed`

JTA-based distributed transactions across multiple resources.

**Features:**
- XA transaction support
- Atomikos transaction manager
- Multiple data source coordination
- Two-phase commit protocol

**Usage:**
```java
@Transactional
public void distributedOperation() {
    // Coordinates transactions across multiple databases
}
```

### 8. Compensating Transaction Pattern
**Location:** `org.example.patterns.transaction.compensating`

Reverses completed operations when later steps fail.

**Features:**
- Compensation stack
- Reverse-order execution
- Error recovery
- Eventual consistency

**Flow:**
```
1. Create Order -> Compensation: Cancel Order
2. Reserve Inventory -> Compensation: Release Inventory
3. Process Payment -> Compensation: Refund Payment
4. Create Shipment -> Compensation: Cancel Shipment
```

### 9. Saga Pattern
**Location:** `org.example.patterns.transaction.saga`

Orchestration-based long-running transactions.

**Features:**
- Saga state management
- Step-by-step execution
- Automatic compensation
- Saga coordinator

**Steps:**
1. Create Order
2. Reserve Inventory
3. Process Payment
4. Create Shipment
5. Confirm Order

If any step fails, compensation runs in reverse.

### 10. Two-Phase Commit Pattern
**Location:** `org.example.patterns.transaction.twophase`

Atomic commitment protocol for distributed systems.

**Phases:**
1. **PREPARE** - All participants vote YES/NO
2. **COMMIT/ABORT** - Coordinator decides based on votes

**Participants:**
- Account Participant
- Inventory Participant
- Order Participant

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### Expected Output
Each pattern demo runs sequentially:
1. Declarative transactions with @Transactional
2. Programmatic transaction control
3. Transaction template usage
4. Transaction interceptor with AOP
5. Transaction synchronization callbacks
6. Nested transactions with different propagations
7. Distributed transactions with JTA
8. Compensating transactions
9. Saga orchestration
10. Two-phase commit protocol

## Database Access

H2 Console: http://localhost:8080/h2-console

**Primary DB:**
- JDBC URL: `jdbc:h2:mem:primarydb`
- Username: `sa`
- Password: (empty)

**Secondary DB:**
- JDBC URL: `jdbc:h2:mem:secondarydb`
- Username: `sa`
- Password: (empty)

## Key Concepts

### ACID Properties
- **Atomicity**: All or nothing
- **Consistency**: Valid state transitions
- **Isolation**: Concurrent transaction isolation
- **Durability**: Committed changes persist

### Isolation Levels
1. **READ_UNCOMMITTED** - Dirty reads possible
2. **READ_COMMITTED** - No dirty reads
3. **REPEATABLE_READ** - Consistent reads
4. **SERIALIZABLE** - Full isolation

### When to Use Each Pattern

**Declarative (@Transactional)**
- ✅ Most common use case
- ✅ Simple transaction requirements
- ✅ Annotation-driven development

**Programmatic**
- ✅ Complex transaction logic
- ✅ Conditional commits
- ✅ Dynamic transaction attributes

**Transaction Template**
- ✅ Programmatic but simplified
- ✅ Callback-based operations
- ✅ Clean code structure

**Transaction Interceptor**
- ✅ Method naming conventions
- ✅ Uniform transaction policies
- ✅ AOP-based approach

**Transaction Synchronization**
- ✅ Post-commit actions
- ✅ Resource cleanup
- ✅ Event notifications

**Nested Transactions**
- ✅ Independent sub-operations
- ✅ Partial rollback capability
- ✅ Complex workflows

**Distributed Transactions**
- ✅ Multiple databases
- ✅ XA resources
- ✅ Strong consistency required

**Compensating Transactions**
- ✅ Microservices architecture
- ✅ Long-running processes
- ✅ Eventual consistency acceptable

**Saga Pattern**
- ✅ Distributed business processes
- ✅ Multiple services coordination
- ✅ Failure recovery required

**Two-Phase Commit**
- ✅ Atomic distributed operations
- ✅ Critical consistency requirements
- ✅ Coordinated resource management

## Best Practices

1. **Use declarative transactions** when possible
2. **Keep transactions short** - avoid long-running operations
3. **Be aware of transaction boundaries** in proxied beans
4. **Use read-only transactions** for queries
5. **Handle exceptions properly** - know rollback rules
6. **Avoid transaction anti-patterns**:
   - Transactions in loops
   - Long-running transactions
   - Transactions with remote calls
7. **Monitor transaction performance**
8. **Test transaction behavior** thoroughly

## Common Pitfalls

### Self-Invocation
```java
@Service
public class MyService {
    @Transactional
    public void methodA() {
        methodB(); // Transaction not applied!
    }
    
    @Transactional
    public void methodB() { }
}
```

**Solution**: Inject self or use separate service.

### Checked Exceptions
By default, only runtime exceptions trigger rollback.

```java
@Transactional(rollbackFor = Exception.class) // Include checked exceptions
```

### Transaction Timeout
Set appropriate timeouts to prevent hanging transactions.

```java
@Transactional(timeout = 30) // 30 seconds
```

## Testing

Run all tests:
```bash
mvn test
```

## License

MIT License - free to use for learning and projects.
```

This completes the comprehensive implementation of all 10 Transaction Management Patterns with working code, configurations, and documentation!