package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Commit Pattern
 * ==============
 * 
 * Demonstrates the @Commit annotation pattern for explicitly committing
 * transactions in Spring test methods, allowing data to persist after
 * test execution.
 * 
 * Use Cases:
 * ----------
 * 1. Integration tests requiring persistent data
 * 2. Test data setup for subsequent tests
 * 3. Verifying commit behavior
 * 4. End-to-end transaction testing
 * 5. Database migration testing
 * 6. Multi-test data scenarios
 * 7. Testing transaction callbacks
 * 8. Audit log verification
 * 
 * Key Features:
 * -------------
 * - Explicitly commit transactions in tests
 * - @Commit is opposite of @Rollback(false)
 * - More readable than @Rollback(false)
 * - Works at class and method level
 * - Overrides default rollback behavior
 * - Can be combined with @Transactional
 * - Supports TestTransaction API
 * - Useful for setup methods
 * 
 * Annotation Options:
 * -------------------
 * @Commit              - Commit transaction after test
 * @Rollback(false)     - Same as @Commit (alternative syntax)
 * 
 * @Commit vs @Rollback(false):
 * ----------------------------
 * @Commit              - More expressive, shows intent clearly
 * @Rollback(false)     - Traditional approach, less readable
 * 
 * Default Behavior:
 * -----------------
 * Without @Commit:
 *   - Transactions are rolled back by default
 *   - Data doesn't persist after test
 * 
 * With @Commit:
 *   - Transaction is committed
 *   - Data persists in database
 *   - May affect subsequent tests
 * 
 * Best Practices:
 * ---------------
 * 1. Use @Commit sparingly in tests
 * 2. Document why commit is needed
 * 3. Clean up committed data in @AfterAll
 * 4. Prefer rollback for test isolation
 * 5. Use @Commit for integration tests
 * 6. Avoid @Commit in unit tests
 * 7. Consider test execution order
 * 8. Use for test data setup methods
 * 
 * When to Use @Commit:
 * --------------------
 * - Testing actual commit behavior
 * - Integration tests with external systems
 * - Test data setup for multiple tests
 * - Verifying database constraints on commit
 * - Testing commit callbacks
 * - End-to-end transaction scenarios
 * 
 * When NOT to Use @Commit:
 * ------------------------
 * - Standard unit tests
 * - Tests requiring isolation
 * - When test order matters
 * - Performance-critical test suites
 * - Tests with mock data
 * 
 * Common Patterns:
 * ----------------
 * 1. Setup data with @Commit in @BeforeAll
 * 2. Test transaction boundaries with @Commit
 * 3. Verify cascade operations on commit
 * 4. Test constraint violations on commit
 * 5. Integration testing with @Commit
 * 6. Multi-step workflows with @Commit
 * 7. Audit trail verification with @Commit
 * 8. Testing optimistic locking on commit
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Test configuration
@Configuration
class CommitTestConfig {
    
    @Bean
    public DataSource dataSource() {
        return new SimulatedDataSource();
    }
    
    @Bean
    public OrderRepository orderRepository(DataSource dataSource) {
        return new OrderRepository(dataSource);
    }
    
    @Bean
    public AuditLogRepository auditLogRepository(DataSource dataSource) {
        return new AuditLogRepository(dataSource);
    }
    
    @Bean
    public TransactionManager transactionManager() {
        return new TransactionManager();
    }
}

// Simulated DataSource
class SimulatedDataSource implements DataSource {
    private final Map<Integer, Order> orders = new HashMap<>();
    private final List<AuditLog> auditLogs = new ArrayList<>();
    private int nextOrderId = 1;
    
    public Order saveOrder(Order order) {
        if (order.getId() == null) {
            order.setId(nextOrderId++);
        }
        orders.put(order.getId(), order);
        System.out.println("  [DB] Saved order: " + order.getOrderNumber() + " (ID: " + order.getId() + ")");
        return order;
    }
    
    public Order findOrderById(int id) {
        return orders.get(id);
    }
    
    public int countOrders() {
        return orders.size();
    }
    
    public void clearOrders() {
        orders.clear();
        System.out.println("  [DB] Cleared all orders");
    }
    
    public void addAuditLog(AuditLog log) {
        auditLogs.add(log);
        System.out.println("  [AUDIT] Logged: " + log.getAction() + " - " + log.getDetails());
    }
    
    public List<AuditLog> getAuditLogs() {
        return new ArrayList<>(auditLogs);
    }
    
    public int countAuditLogs() {
        return auditLogs.size();
    }
    
    public void clearAuditLogs() {
        auditLogs.clear();
        System.out.println("  [AUDIT] Cleared all logs");
    }
    
    // DataSource interface methods (stub implementations)
    @Override
    public java.sql.Connection getConnection() { return null; }
    @Override
    public java.sql.Connection getConnection(String username, String password) { return null; }
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

// Domain models
class Order {
    private Integer id;
    private String orderNumber;
    private double amount;
    private String status;
    
    public Order(String orderNumber, double amount) {
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.status = "PENDING";
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

class AuditLog {
    private final String action;
    private final String details;
    private final long timestamp;
    
    public AuditLog(String action, String details) {
        this.action = action;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public long getTimestamp() { return timestamp; }
}

// Repositories
class OrderRepository {
    private final SimulatedDataSource dataSource;
    
    public OrderRepository(DataSource dataSource) {
        this.dataSource = (SimulatedDataSource) dataSource;
    }
    
    public Order save(Order order) {
        Order saved = dataSource.saveOrder(order);
        dataSource.addAuditLog(new AuditLog("ORDER_CREATED", "Order " + order.getOrderNumber()));
        return saved;
    }
    
    public Order findById(int id) {
        return dataSource.findOrderById(id);
    }
    
    public int count() {
        return dataSource.countOrders();
    }
    
    public void clear() {
        dataSource.clearOrders();
    }
}

class AuditLogRepository {
    private final SimulatedDataSource dataSource;
    
    public AuditLogRepository(DataSource dataSource) {
        this.dataSource = (SimulatedDataSource) dataSource;
    }
    
    public List<AuditLog> findAll() {
        return dataSource.getAuditLogs();
    }
    
    public int count() {
        return dataSource.countAuditLogs();
    }
    
    public void clear() {
        dataSource.clearAuditLogs();
    }
}

// Transaction manager
class TransactionManager {
    private boolean transactionActive = false;
    private boolean committed = false;
    
    public void beginTransaction() {
        transactionActive = true;
        committed = false;
        System.out.println("  [TX] Transaction started");
    }
    
    public void commit() {
        if (transactionActive) {
            committed = true;
            System.out.println("  [TX] Transaction committed ✓");
        }
        transactionActive = false;
    }
    
    public void rollback() {
        if (transactionActive) {
            System.out.println("  [TX] Transaction rolled back");
        }
        transactionActive = false;
        committed = false;
    }
    
    public boolean isCommitted() {
        return committed;
    }
    
    public boolean isActive() {
        return transactionActive;
    }
}

/**
 * Example 1: Basic Commit Usage
 * Demonstrates @Commit annotation for persisting data
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CommitTestConfig.class)
@Transactional
class BasicCommitTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        transactionManager.beginTransaction();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (transactionManager.isActive() && !transactionManager.isCommitted()) {
            transactionManager.rollback();
        }
    }
    
    @Test
    @Commit
    void testCommitData() {
        System.out.println("\n=== Test: Commit Data ===");
        
        Order order = new Order("ORD-001", 100.00);
        orderRepository.save(order);
        
        assertEquals(1, orderRepository.count());
        
        transactionManager.commit();
        
        System.out.println("✓ Transaction committed - data will persist");
    }
    
    @Test
    void testDefaultRollback() {
        System.out.println("\n=== Test: Default Rollback (No @Commit) ===");
        
        Order order = new Order("ORD-002", 200.00);
        orderRepository.save(order);
        
        assertEquals(1, orderRepository.count());
        
        System.out.println("✓ No @Commit - data will be rolled back");
    }
}

/**
 * Example 2: @Commit vs @Rollback(false)
 * Demonstrates both syntaxes for committing
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CommitTestConfig.class)
@Transactional
class CommitVsRollbackFalseTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        transactionManager.beginTransaction();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (!transactionManager.isCommitted()) {
            transactionManager.rollback();
        }
    }
    
    @Test
    @Commit  // More expressive
    void testWithCommitAnnotation() {
        System.out.println("\n=== Test: Using @Commit ===");
        
        Order order = new Order("ORD-003", 300.00);
        orderRepository.save(order);
        
        transactionManager.commit();
        
        System.out.println("✓ @Commit - clear intent to commit");
    }
    
    @Test
    @org.springframework.test.annotation.Rollback(false)  // Alternative syntax
    void testWithRollbackFalse() {
        System.out.println("\n=== Test: Using @Rollback(false) ===");
        
        Order order = new Order("ORD-004", 400.00);
        orderRepository.save(order);
        
        transactionManager.commit();
        
        System.out.println("✓ @Rollback(false) - same effect as @Commit");
    }
}

/**
 * Example 3: Class-Level Commit
 * Demonstrates applying @Commit to entire test class
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CommitTestConfig.class)
@Transactional
@Commit  // All test methods will commit by default
class ClassLevelCommitTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        transactionManager.beginTransaction();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (transactionManager.isActive()) {
            transactionManager.commit();
        }
    }
    
    @Test
    void testOne() {
        System.out.println("\n=== Test: Class-Level Commit - Test 1 ===");
        
        Order order = new Order("ORD-005", 500.00);
        orderRepository.save(order);
        
        System.out.println("✓ Test 1 - Will commit (class-level)");
    }
    
    @Test
    void testTwo() {
        System.out.println("\n=== Test: Class-Level Commit - Test 2 ===");
        
        Order order = new Order("ORD-006", 600.00);
        orderRepository.save(order);
        
        System.out.println("✓ Test 2 - Will commit (class-level)");
    }
    
    @Test
    @org.springframework.test.annotation.Rollback  // Override to rollback
    void testThreeRollback() {
        System.out.println("\n=== Test: Override Class-Level - Rollback ===");
        
        Order order = new Order("ORD-007", 700.00);
        orderRepository.save(order);
        
        System.out.println("✓ Test 3 - Will rollback (method overrides class)");
    }
}

/**
 * Example 4: Integration Test with Commit
 * Demonstrates using @Commit for integration testing
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CommitTestConfig.class)
@Transactional
class IntegrationTestWithCommitTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        transactionManager.beginTransaction();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (transactionManager.isActive() && !transactionManager.isCommitted()) {
            transactionManager.rollback();
        }
    }
    
    @Test
    @Commit
    void testOrderCreationWithAuditLog() {
        System.out.println("\n=== Test: Order Creation with Audit Log ===");
        
        Order order = new Order("ORD-008", 800.00);
        orderRepository.save(order);
        
        // Verify both order and audit log are created
        assertEquals(1, orderRepository.count());
        assertEquals(1, auditLogRepository.count());
        
        List<AuditLog> logs = auditLogRepository.findAll();
        assertEquals("ORDER_CREATED", logs.get(0).getAction());
        
        transactionManager.commit();
        
        System.out.println("✓ Order and audit log committed together");
    }
}

/**
 * Example 5: Setup Data with Commit
 * Demonstrates using @Commit for test data setup
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CommitTestConfig.class)
@Transactional
class SetupDataWithCommitTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TransactionManager transactionManager;
    
    private static boolean setupDone = false;
    
    @org.junit.jupiter.api.BeforeAll
    static void setupTestData(@Autowired OrderRepository repo, 
                               @Autowired TransactionManager txManager) {
        if (!setupDone) {
            System.out.println("\n=== Setup: Creating Test Data ===");
            
            txManager.beginTransaction();
            
            // Create setup data
            repo.save(new Order("SETUP-001", 100.00));
            repo.save(new Order("SETUP-002", 200.00));
            
            txManager.commit();
            
            setupDone = true;
            
            System.out.println("✓ Setup data committed for all tests");
        }
    }
    
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
    void testUsingSetupData() {
        System.out.println("\n=== Test: Using Setup Data ===");
        
        // Setup data should be available
        assertTrue(orderRepository.count() >= 2);
        
        System.out.println("✓ Can access committed setup data");
    }
    
    @org.junit.jupiter.api.AfterAll
    static void cleanup(@Autowired OrderRepository repo) {
        System.out.println("\n=== Cleanup: Removing Test Data ===");
        repo.clear();
        System.out.println("✓ Cleanup complete");
    }
}

/**
 * Main class for demonstration
 */
public class CommitPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Commit Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Basic @Commit usage for persisting data");
        System.out.println("2. @Commit vs @Rollback(false) comparison");
        System.out.println("3. Class-level @Commit configuration");
        System.out.println("4. Integration testing with @Commit");
        System.out.println("5. Test data setup with @Commit");
        System.out.println("6. Transaction commit verification");
        System.out.println("7. Multi-entity commit scenarios");
        System.out.println("8. Audit logging with commits");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * Commit Pattern Summary:
 * 
 * Basic Usage:
 * ------------
 * @Test
 * @Transactional
 * @Commit
 * void testCommit() {
 *     // Data will be committed
 * }
 * 
 * Alternative Syntax:
 * -------------------
 * @Test
 * @Transactional
 * @Rollback(false)  // Same as @Commit
 * void testCommit() {
 *     // Data will be committed
 * }
 * 
 * Class-Level:
 * ------------
 * @Transactional
 * @Commit
 * class MyTest {
 *     @Test
 *     void test1() { }  // Will commit
 *     
 *     @Test
 *     @Rollback  // Override to rollback
 *     void test2() { }  // Will rollback
 * }
 * 
 * Integration Test Example:
 * -------------------------
 * @SpringBootTest
 * @Transactional
 * class IntegrationTest {
 *     
 *     @BeforeAll
 *     @Commit
 *     static void setupData() {
 *         // Setup data persists for all tests
 *     }
 *     
 *     @Test
 *     void testOne() {
 *         // Can access committed setup data
 *         // This test will rollback by default
 *     }
 *     
 *     @AfterAll
 *     static void cleanup() {
 *         // Clean up committed data
 *     }
 * }
 */
