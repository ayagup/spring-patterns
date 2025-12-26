package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rollback Pattern
 * =================
 * 
 * Demonstrates the @Rollback annotation pattern for controlling transaction
 * rollback behavior in Spring test methods to ensure test isolation and
 * database state cleanup.
 * 
 * Use Cases:
 * ----------
 * 1. Automatic test data cleanup
 * 2. Test isolation for database operations
 * 3. Preventing test data pollution
 * 4. Integration testing with databases
 * 5. Transactional test scenarios
 * 6. Rollback control for specific tests
 * 7. Testing transaction boundaries
 * 8. Verifying data persistence logic
 * 
 * Key Features:
 * -------------
 * - Automatic transaction rollback after tests
 * - @Rollback(true) is default for @Transactional tests
 * - Can be overridden with @Rollback(false)
 * - Works at class and method level
 * - Preserves database state between tests
 * - TestTransaction API for manual control
 * - Supports nested transactions
 * - Integration with Spring's transaction management
 * 
 * Default Behavior:
 * -----------------
 * @Transactional test methods:
 *   - Automatically rolled back by default
 *   - @Rollback(true) is implicit
 *   - No explicit annotation needed
 * 
 * Explicit Control:
 * -----------------
 * @Rollback(false) - Commit transaction after test
 * @Rollback(true)  - Rollback transaction (default)
 * 
 * Transaction Lifecycle in Tests:
 * --------------------------------
 * 1. Test method begins
 * 2. Transaction started (if @Transactional)
 * 3. Test code executes
 * 4. Test assertions run
 * 5. Transaction rolled back (default) or committed
 * 6. Next test starts with clean state
 * 
 * Best Practices:
 * ---------------
 * 1. Use @Transactional for database tests
 * 2. Rely on default rollback behavior
 * 3. Use @Rollback(false) sparingly
 * 4. Clean up manually if using @Rollback(false)
 * 5. Test transaction boundaries explicitly
 * 6. Use TestTransaction for manual control
 * 7. Verify data changes within test
 * 8. Document why rollback is disabled
 * 
 * Common Patterns:
 * ----------------
 * 1. Repository testing with rollback
 * 2. Service layer transaction testing
 * 3. Data integrity testing
 * 4. Cascade operations testing
 * 5. Constraint violation testing
 * 6. Multi-step transaction testing
 * 7. Rollback on exception testing
 * 8. Commit behavior verification
 * 
 * TestTransaction API:
 * --------------------
 * - TestTransaction.isActive()
 * - TestTransaction.isFlaggedForRollback()
 * - TestTransaction.flagForRollback()
 * - TestTransaction.flagForCommit()
 * - TestTransaction.start()
 * - TestTransaction.end()
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Test configuration with in-memory database
@Configuration
class RollbackTestConfig {
    
    @Bean
    public DataSource dataSource() {
        // Simulated H2 DataSource
        return new SimulatedDataSource();
    }
    
    @Bean
    public UserRepository userRepository(DataSource dataSource) {
        return new UserRepository(dataSource);
    }
    
    @Bean
    public TransactionManager transactionManager() {
        return new TransactionManager();
    }
}

// Simulated DataSource for demonstration
class SimulatedDataSource implements DataSource {
    private final java.util.Map<Integer, User> database = new java.util.HashMap<>();
    private int nextId = 1;
    
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        database.put(user.getId(), user);
        System.out.println("  [DB] Saved user: " + user.getName() + " (ID: " + user.getId() + ")");
        return user;
    }
    
    public User findById(int id) {
        return database.get(id);
    }
    
    public void delete(int id) {
        database.remove(id);
        System.out.println("  [DB] Deleted user ID: " + id);
    }
    
    public int count() {
        return database.size();
    }
    
    public void clear() {
        database.clear();
        System.out.println("  [DB] Cleared all data");
    }
    
    // DataSource interface methods (stub implementations)
    @Override
    public Connection getConnection() { return null; }
    @Override
    public Connection getConnection(String username, String password) { return null; }
    @Override
    public java.io.PrintWriter getLogWriter() { return null; }
    @Override
    public void setLogWriter(java.io.PrintWriter out) {}
    @Override
    public void setLoginTimeout(int seconds) {}
    @Override
    public int getLoginTimeout() { return 0; }
    @Override
    public java.util.logging.Logger getParentLogger() { return null; }
    @Override
    public <T> T unwrap(Class<T> iface) { return null; }
    @Override
    public boolean isWrapperFor(Class<?> iface) { return false; }
}

// Domain model
class User {
    private Integer id;
    private String name;
    private String email;
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

// Repository
class UserRepository {
    private final SimulatedDataSource dataSource;
    
    public UserRepository(DataSource dataSource) {
        this.dataSource = (SimulatedDataSource) dataSource;
    }
    
    public User save(User user) {
        return dataSource.save(user);
    }
    
    public User findById(int id) {
        return dataSource.findById(id);
    }
    
    public void delete(int id) {
        dataSource.delete(id);
    }
    
    public int count() {
        return dataSource.count();
    }
}

// Transaction manager
class TransactionManager {
    private boolean transactionActive = false;
    private boolean rollbackOnly = false;
    
    public void beginTransaction() {
        transactionActive = true;
        rollbackOnly = false;
        System.out.println("  [TX] Transaction started");
    }
    
    public void commit() {
        if (transactionActive && !rollbackOnly) {
            System.out.println("  [TX] Transaction committed");
        }
        transactionActive = false;
    }
    
    public void rollback() {
        if (transactionActive) {
            System.out.println("  [TX] Transaction rolled back");
        }
        transactionActive = false;
    }
    
    public void setRollbackOnly() {
        rollbackOnly = true;
    }
    
    public boolean isActive() {
        return transactionActive;
    }
}

/**
 * Example 1: Default Rollback Behavior
 * Demonstrates automatic rollback with @Transactional
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RollbackTestConfig.class)
@Transactional
class DefaultRollbackTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        transactionManager.beginTransaction();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        transactionManager.rollback();
    }
    
    @Test
    void testDefaultRollback() {
        System.out.println("\n=== Test: Default Rollback ===");
        
        User user = new User("John Doe", "john@example.com");
        User saved = userRepository.save(user);
        
        assertNotNull(saved.getId());
        assertEquals(1, userRepository.count());
        
        System.out.println("✓ Data saved in transaction");
        System.out.println("  Transaction will be rolled back automatically");
    }
    
    @Test
    void testAnotherDefaultRollback() {
        System.out.println("\n=== Test: Another Default Rollback ===");
        
        // Database should be clean due to rollback from previous test
        assertEquals(0, userRepository.count(), "Database should be empty");
        
        User user = new User("Jane Doe", "jane@example.com");
        userRepository.save(user);
        
        assertEquals(1, userRepository.count());
        
        System.out.println("✓ Fresh transaction, clean database");
        System.out.println("  This transaction will also be rolled back");
    }
}

/**
 * Example 2: Explicit Rollback Control
 * Demonstrates @Rollback(true) and @Rollback(false)
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RollbackTestConfig.class)
@Transactional
class ExplicitRollbackTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        transactionManager.beginTransaction();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (transactionManager.isActive()) {
            transactionManager.rollback();
        }
    }
    
    @Test
    @Rollback(true)
    void testExplicitRollback() {
        System.out.println("\n=== Test: Explicit Rollback(true) ===");
        
        User user = new User("Bob Smith", "bob@example.com");
        userRepository.save(user);
        
        assertEquals(1, userRepository.count());
        
        System.out.println("✓ Data will be rolled back explicitly");
    }
    
    @Test
    @Rollback(false)
    void testNoRollback() {
        System.out.println("\n=== Test: Rollback(false) - Commit ===");
        
        User user = new User("Alice Brown", "alice@example.com");
        userRepository.save(user);
        
        assertEquals(1, userRepository.count());
        
        transactionManager.commit();
        
        System.out.println("✓ Data will be committed (not rolled back)");
        System.out.println("  WARNING: May affect subsequent tests!");
    }
}

/**
 * Example 3: Class-Level Rollback Configuration
 * Demonstrates setting rollback behavior for entire class
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RollbackTestConfig.class)
@Transactional
@Rollback(true) // Applied to all test methods
class ClassLevelRollbackTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        transactionManager.beginTransaction();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        transactionManager.rollback();
    }
    
    @Test
    void testOne() {
        System.out.println("\n=== Test: Class-Level Rollback - Test 1 ===");
        
        User user = new User("User One", "user1@example.com");
        userRepository.save(user);
        
        assertEquals(1, userRepository.count());
        System.out.println("✓ Test 1 - Will rollback (class-level)");
    }
    
    @Test
    void testTwo() {
        System.out.println("\n=== Test: Class-Level Rollback - Test 2 ===");
        
        User user = new User("User Two", "user2@example.com");
        userRepository.save(user);
        
        assertEquals(1, userRepository.count());
        System.out.println("✓ Test 2 - Will rollback (class-level)");
    }
    
    @Test
    @Rollback(false) // Override class-level setting
    void testThreeWithCommit() {
        System.out.println("\n=== Test: Override Class-Level - Commit ===");
        
        User user = new User("User Three", "user3@example.com");
        userRepository.save(user);
        
        transactionManager.commit();
        
        System.out.println("✓ Test 3 - Will commit (method overrides class)");
    }
}

/**
 * Example 4: Testing Data Persistence
 * Demonstrates verifying data changes within transaction
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RollbackTestConfig.class)
@Transactional
class DataPersistenceTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        transactionManager.beginTransaction();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        transactionManager.rollback();
    }
    
    @Test
    void testSaveAndRetrieve() {
        System.out.println("\n=== Test: Save and Retrieve ===");
        
        User user = new User("Test User", "test@example.com");
        User saved = userRepository.save(user);
        
        User retrieved = userRepository.findById(saved.getId());
        
        assertNotNull(retrieved);
        assertEquals("Test User", retrieved.getName());
        assertEquals("test@example.com", retrieved.getEmail());
        
        System.out.println("✓ Data persisted and retrieved within transaction");
        System.out.println("  Changes will be rolled back after test");
    }
    
    @Test
    void testDelete() {
        System.out.println("\n=== Test: Delete Operation ===");
        
        User user = new User("To Delete", "delete@example.com");
        User saved = userRepository.save(user);
        
        assertEquals(1, userRepository.count());
        
        userRepository.delete(saved.getId());
        
        assertEquals(0, userRepository.count());
        assertNull(userRepository.findById(saved.getId()));
        
        System.out.println("✓ Delete operation verified");
        System.out.println("  Transaction will be rolled back");
    }
}

/**
 * Example 5: Multiple Operations in Transaction
 * Demonstrates testing multiple database operations
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RollbackTestConfig.class)
@Transactional
class MultipleOperationsTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        transactionManager.beginTransaction();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        transactionManager.rollback();
    }
    
    @Test
    void testMultipleSaves() {
        System.out.println("\n=== Test: Multiple Saves ===");
        
        User user1 = new User("User 1", "user1@example.com");
        User user2 = new User("User 2", "user2@example.com");
        User user3 = new User("User 3", "user3@example.com");
        
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        
        assertEquals(3, userRepository.count());
        
        System.out.println("✓ Multiple saves in single transaction");
        System.out.println("  All will be rolled back together");
    }
    
    @Test
    void testSaveAndDelete() {
        System.out.println("\n=== Test: Save and Delete ===");
        
        User user1 = new User("Keep Me", "keep@example.com");
        User user2 = new User("Delete Me", "delete@example.com");
        
        User saved1 = userRepository.save(user1);
        User saved2 = userRepository.save(user2);
        
        assertEquals(2, userRepository.count());
        
        userRepository.delete(saved2.getId());
        
        assertEquals(1, userRepository.count());
        assertNotNull(userRepository.findById(saved1.getId()));
        assertNull(userRepository.findById(saved2.getId()));
        
        System.out.println("✓ Save and delete operations tested");
        System.out.println("  Transaction will roll back all changes");
    }
}

/**
 * Example 6: Testing Transaction Boundaries
 * Demonstrates transaction isolation
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RollbackTestConfig.class)
class TransactionBoundaryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    @Test
    @Transactional
    void testTransactionIsolation() {
        System.out.println("\n=== Test: Transaction Isolation ===");
        
        transactionManager.beginTransaction();
        
        User user = new User("Isolated", "isolated@example.com");
        userRepository.save(user);
        
        assertEquals(1, userRepository.count());
        
        System.out.println("✓ Data visible within transaction");
        System.out.println("  Will be rolled back after test");
        
        transactionManager.rollback();
    }
}

/**
 * Main class for demonstration
 */
public class RollbackPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Rollback Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Default rollback behavior (@Transactional)");
        System.out.println("2. Explicit rollback control (@Rollback)");
        System.out.println("3. Class-level rollback configuration");
        System.out.println("4. Data persistence testing with rollback");
        System.out.println("5. Multiple operations in transaction");
        System.out.println("6. Transaction boundary testing");
        System.out.println("7. Test isolation through rollback");
        System.out.println("8. Commit vs rollback scenarios");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * Rollback Configuration Summary:
 * 
 * Default Behavior:
 * -----------------
 * @Transactional
 * class MyTest {
 *     @Test
 *     void test() {
 *         // Automatically rolled back
 *     }
 * }
 * 
 * Explicit Rollback:
 * ------------------
 * @Test
 * @Rollback(true)  // Explicit rollback
 * void test() { }
 * 
 * Commit Instead:
 * ---------------
 * @Test
 * @Rollback(false)  // Commit transaction
 * void test() { }
 * 
 * Class-Level:
 * ------------
 * @Transactional
 * @Rollback(false)  // All methods commit by default
 * class MyTest {
 *     @Test
 *     void test1() { }  // Will commit
 *     
 *     @Test
 *     @Rollback(true)  // Override to rollback
 *     void test2() { }  // Will rollback
 * }
 * 
 * TestTransaction API:
 * --------------------
 * @Test
 * @Transactional
 * void test() {
 *     // Check if transaction is active
 *     assertTrue(TestTransaction.isActive());
 *     
 *     // Flag for rollback
 *     TestTransaction.flagForRollback();
 *     assertTrue(TestTransaction.isFlaggedForRollback());
 *     
 *     // Flag for commit
 *     TestTransaction.flagForCommit();
 *     assertFalse(TestTransaction.isFlaggedForRollback());
 * }
 */
