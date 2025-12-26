package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Before Transaction Pattern
 * ===========================
 * 
 * Demonstrates the @BeforeTransaction annotation pattern for executing
 * code before a transaction starts in Spring transactional tests.
 * 
 * Use Cases:
 * ----------
 * 1. Pre-transaction database setup
 * 2. Verify database state before transaction
 * 3. Set up test data outside transaction
 * 4. Initialize resources before transaction
 * 5. Log transaction start
 * 6. Configure transaction-specific settings
 * 7. Validate preconditions
 * 8. Prepare external systems
 * 
 * Key Features:
 * -------------
 * - Executes before transaction begins
 * - Runs outside transaction context
 * - Useful for non-transactional setup
 * - Can verify initial state
 * - Multiple methods supported
 * - Works with @Transactional tests
 * - Complements @AfterTransaction
 * - Method-level annotation
 * 
 * Execution Order:
 * ----------------
 * 1. @BeforeEach methods (if any)
 * 2. @BeforeTransaction methods
 * 3. Transaction begins
 * 4. @Test method executes
 * 5. Transaction commits/rolls back
 * 6. @AfterTransaction methods
 * 7. @AfterEach methods (if any)
 * 
 * Differences from @BeforeEach:
 * -----------------------------
 * @BeforeEach:
 *   - Runs before every test
 *   - Inside or outside transaction (depending on where transaction starts)
 * 
 * @BeforeTransaction:
 *   - Runs only before transactional tests
 *   - Always outside transaction
 *   - Specifically for transaction-related setup
 * 
 * Best Practices:
 * ---------------
 * 1. Use for database state verification
 * 2. Set up data that shouldn't be in transaction
 * 3. Keep setup logic lightweight
 * 4. Combine with @AfterTransaction for cleanup
 * 5. Document why setup is before transaction
 * 6. Avoid heavy computations
 * 7. Use for transaction-specific initialization
 * 8. Verify preconditions
 * 
 * Common Patterns:
 * ----------------
 * 1. Verify empty database before test
 * 2. Set up reference data outside transaction
 * 3. Initialize external resources
 * 4. Log transaction boundaries
 * 5. Validate database connections
 * 6. Configure transaction settings
 * 7. Prepare test fixtures
 * 8. Check constraint states
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Test configuration
@Configuration
class BeforeTransactionTestConfig {
    
    @Bean
    public DataSource dataSource() {
        return new SimulatedDataSource();
    }
    
    @Bean
    public AccountRepository accountRepository(DataSource dataSource) {
        return new AccountRepository(dataSource);
    }
    
    @Bean
    public TransactionLogger transactionLogger() {
        return new TransactionLogger();
    }
}

// Simulated DataSource
class SimulatedDataSource implements DataSource {
    private final List<Account> accounts = new ArrayList<>();
    private int nextId = 1;
    
    public Account save(Account account) {
        if (account.getId() == null) {
            account.setId(nextId++);
        }
        accounts.add(account);
        System.out.println("  [DB] Saved account: " + account.getName() + 
            " (Balance: $" + account.getBalance() + ")");
        return account;
    }
    
    public Account findById(int id) {
        return accounts.stream()
            .filter(a -> a.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public List<Account> findAll() {
        return new ArrayList<>(accounts);
    }
    
    public int count() {
        return accounts.size();
    }
    
    public void clear() {
        accounts.clear();
        nextId = 1;
        System.out.println("  [DB] Database cleared");
    }
    
    // DataSource interface methods (stub implementations)
    @Override public java.sql.Connection getConnection() { return null; }
    @Override public java.sql.Connection getConnection(String username, String password) { return null; }
    @Override public java.io.PrintWriter getLogWriter() { return null; }
    @Override public void setLogWriter(java.io.PrintWriter out) {}
    @Override public void setLoginTimeout(int seconds) {}
    @Override public int getLoginTimeout() { return 0; }
    @Override public java.util.logging.Logger getParentLogger() { return null; }
    @Override public <T> T unwrap(Class<T> iface) { return null; }
    @Override public boolean isWrapperFor(Class<?> iface) { return false; }
}

// Domain model
class Account {
    private Integer id;
    private String name;
    private double balance;
    
    public Account(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}

// Repository
class AccountRepository {
    private final SimulatedDataSource dataSource;
    
    public AccountRepository(DataSource dataSource) {
        this.dataSource = (SimulatedDataSource) dataSource;
    }
    
    public Account save(Account account) {
        return dataSource.save(account);
    }
    
    public Account findById(int id) {
        return dataSource.findById(id);
    }
    
    public List<Account> findAll() {
        return dataSource.findAll();
    }
    
    public int count() {
        return dataSource.count();
    }
    
    public void deleteAll() {
        dataSource.clear();
    }
}

// Transaction logger
class TransactionLogger {
    private final List<String> logs = new ArrayList<>();
    
    public void log(String message) {
        String entry = System.currentTimeMillis() + ": " + message;
        logs.add(entry);
        System.out.println("  [TX-LOG] " + message);
    }
    
    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }
    
    public void clear() {
        logs.clear();
    }
}

/**
 * Example 1: Basic Before Transaction Usage
 * Demonstrates @BeforeTransaction for setup
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BeforeTransactionTestConfig.class)
@Transactional
class BasicBeforeTransactionTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionLogger logger;
    
    @BeforeTransaction
    void setupBeforeTransaction() {
        System.out.println("\n=== @BeforeTransaction: Setup ===");
        System.out.println("  Executing BEFORE transaction starts");
        
        // Verify database is empty before transaction
        assertEquals(0, accountRepository.count(), 
            "Database should be empty before transaction");
        
        logger.log("Transaction setup complete");
        
        System.out.println("  Setup complete, transaction will start now");
    }
    
    @Test
    void testWithBeforeTransaction() {
        System.out.println("\n=== Test: Inside Transaction ===");
        
        // Now inside transaction
        logger.log("Test executing inside transaction");
        
        Account account = new Account("John Doe", 1000.00);
        accountRepository.save(account);
        
        assertEquals(1, accountRepository.count());
        
        System.out.println("✓ Test executed inside transaction");
    }
}

/**
 * Example 2: Verify Database State
 * Demonstrates verifying state before transaction
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BeforeTransactionTestConfig.class)
@Transactional
class VerifyStateBeforeTransactionTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @BeforeTransaction
    void verifyDatabaseState() {
        System.out.println("\n=== @BeforeTransaction: Verify State ===");
        
        int initialCount = accountRepository.count();
        System.out.println("  Initial account count: " + initialCount);
        
        // Could verify constraints, indexes, etc.
        System.out.println("  Database state verified");
    }
    
    @Test
    void testDatabaseOperations() {
        System.out.println("\n=== Test: Database Operations ===");
        
        Account account1 = new Account("Alice", 500.00);
        Account account2 = new Account("Bob", 750.00);
        
        accountRepository.save(account1);
        accountRepository.save(account2);
        
        assertEquals(2, accountRepository.count());
        
        System.out.println("✓ Operations executed");
    }
}

/**
 * Example 3: Multiple Before Transaction Methods
 * Demonstrates multiple @BeforeTransaction methods
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BeforeTransactionTestConfig.class)
@Transactional
class MultipleBeforeTransactionTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionLogger logger;
    
    @BeforeTransaction
    void firstBeforeTransaction() {
        System.out.println("\n=== @BeforeTransaction #1 ===");
        logger.log("First setup method");
        System.out.println("  First setup complete");
    }
    
    @BeforeTransaction
    void secondBeforeTransaction() {
        System.out.println("\n=== @BeforeTransaction #2 ===");
        logger.log("Second setup method");
        assertEquals(0, accountRepository.count());
        System.out.println("  Second setup complete");
    }
    
    @BeforeTransaction
    void thirdBeforeTransaction() {
        System.out.println("\n=== @BeforeTransaction #3 ===");
        logger.log("Third setup method");
        System.out.println("  Third setup complete");
    }
    
    @Test
    void testWithMultipleSetups() {
        System.out.println("\n=== Test: After Multiple Setups ===");
        
        List<String> logs = logger.getLogs();
        assertTrue(logs.size() >= 3, "Should have at least 3 log entries");
        
        Account account = new Account("Test User", 100.00);
        accountRepository.save(account);
        
        System.out.println("✓ Test after multiple @BeforeTransaction methods");
    }
}

/**
 * Example 4: Log Transaction Boundaries
 * Demonstrates logging transaction lifecycle
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BeforeTransactionTestConfig.class)
@Transactional
class LogTransactionBoundariesTest {
    
    @Autowired
    private TransactionLogger logger;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @BeforeTransaction
    void logTransactionStart() {
        System.out.println("\n=== @BeforeTransaction: Log Start ===");
        logger.log("BEFORE TRANSACTION - Transaction about to start");
        logger.log("Timestamp: " + System.currentTimeMillis());
        System.out.println("  Transaction start logged");
    }
    
    @org.junit.jupiter.api.BeforeEach
    void beforeEach() {
        System.out.println("\n=== @BeforeEach: Regular Setup ===");
        logger.log("BEFORE EACH - Inside transaction");
        System.out.println("  Regular setup (inside transaction)");
    }
    
    @Test
    void testTransactionLogging() {
        System.out.println("\n=== Test: Transaction Logging ===");
        
        logger.log("TEST EXECUTION - Creating account");
        
        Account account = new Account("Logged User", 200.00);
        accountRepository.save(account);
        
        logger.log("TEST EXECUTION - Account created");
        
        List<String> logs = logger.getLogs();
        assertTrue(logs.size() >= 4);
        
        System.out.println("✓ Transaction logged successfully");
        System.out.println("  Total log entries: " + logs.size());
    }
}

/**
 * Example 5: Initialize Resources
 * Demonstrates resource initialization before transaction
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BeforeTransactionTestConfig.class)
@Transactional
class InitializeResourcesTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    private boolean resourcesInitialized = false;
    
    @BeforeTransaction
    void initializeResources() {
        System.out.println("\n=== @BeforeTransaction: Initialize Resources ===");
        
        // Initialize resources (connections, caches, etc.)
        System.out.println("  Initializing external resources...");
        resourcesInitialized = true;
        
        // Verify database connection
        assertNotNull(accountRepository);
        
        System.out.println("  Resources initialized successfully");
    }
    
    @Test
    void testWithInitializedResources() {
        System.out.println("\n=== Test: Using Initialized Resources ===");
        
        assertTrue(resourcesInitialized, "Resources should be initialized");
        
        Account account = new Account("Resource User", 300.00);
        accountRepository.save(account);
        
        assertEquals(1, accountRepository.count());
        
        System.out.println("✓ Resources used successfully");
    }
}

/**
 * Example 6: Validate Preconditions
 * Demonstrates validating conditions before transaction
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BeforeTransactionTestConfig.class)
@Transactional
class ValidatePreconditionsTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @BeforeTransaction
    void validatePreconditions() {
        System.out.println("\n=== @BeforeTransaction: Validate Preconditions ===");
        
        // Validate database is accessible
        assertNotNull(accountRepository, "Repository should be available");
        
        // Validate initial state
        int count = accountRepository.count();
        System.out.println("  Initial count: " + count);
        
        // Could validate other conditions
        System.out.println("  All preconditions valid");
    }
    
    @Test
    void testAfterValidation() {
        System.out.println("\n=== Test: After Validation ===");
        
        Account account = new Account("Validated User", 400.00);
        accountRepository.save(account);
        
        assertTrue(accountRepository.count() >= 1);
        
        System.out.println("✓ Test executed after validation");
    }
}

/**
 * Main class for demonstration
 */
public class BeforeTransactionPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Before Transaction Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. @BeforeTransaction basic usage");
        System.out.println("2. Verifying database state before transaction");
        System.out.println("3. Multiple @BeforeTransaction methods");
        System.out.println("4. Logging transaction boundaries");
        System.out.println("5. Resource initialization before transaction");
        System.out.println("6. Validating preconditions");
        System.out.println("7. Execution order with @BeforeEach");
        System.out.println("8. Transaction lifecycle management");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * Before Transaction Summary:
 * 
 * Basic Usage:
 * ------------
 * @Transactional
 * class MyTest {
 *     
 *     @BeforeTransaction
 *     void setup() {
 *         // Runs BEFORE transaction starts
 *         // Outside transaction context
 *     }
 *     
 *     @Test
 *     void test() {
 *         // Runs INSIDE transaction
 *     }
 * }
 * 
 * Multiple Methods:
 * -----------------
 * @Transactional
 * class MyTest {
 *     
 *     @BeforeTransaction
 *     void setup1() {
 *         // First setup
 *     }
 *     
 *     @BeforeTransaction
 *     void setup2() {
 *         // Second setup
 *     }
 *     
 *     @Test
 *     void test() {
 *         // Test
 *     }
 * }
 * 
 * Execution Order:
 * ----------------
 * @Transactional
 * class MyTest {
 *     
 *     @BeforeEach
 *     void beforeEach() {
 *         // 1. Runs first (may be outside transaction)
 *     }
 *     
 *     @BeforeTransaction
 *     void beforeTx() {
 *         // 2. Runs before transaction starts
 *     }
 *     
 *     // 3. Transaction begins
 *     
 *     @Test
 *     void test() {
 *         // 4. Test executes (inside transaction)
 *     }
 *     
 *     // 5. Transaction commits/rolls back
 *     
 *     @AfterTransaction
 *     void afterTx() {
 *         // 6. Runs after transaction ends
 *     }
 *     
 *     @AfterEach
 *     void afterEach() {
 *         // 7. Runs last
 *     }
 * }
 */
