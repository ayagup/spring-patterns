package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * After Transaction Pattern
 * ==========================
 * 
 * Demonstrates the @AfterTransaction annotation pattern for executing
 * code after a transaction completes in Spring transactional tests.
 * 
 * Use Cases:
 * ----------
 * 1. Verify final database state
 * 2. Post-transaction cleanup
 * 3. Validate transaction results
 * 4. Check committed/rolled back data
 * 5. Release resources after transaction
 * 6. Log transaction completion
 * 7. Verify constraints and indexes
 * 8. Audit transaction outcomes
 * 
 * Key Features:
 * -------------
 * - Executes after transaction ends
 * - Runs outside transaction context
 * - Useful for post-transaction validation
 * - Can verify final state
 * - Multiple methods supported
 * - Works with @Transactional tests
 * - Complements @BeforeTransaction
 * - Method-level annotation
 * 
 * Execution Order:
 * ----------------
 * 1. @BeforeEach methods
 * 2. @BeforeTransaction methods
 * 3. Transaction begins
 * 4. @Test method executes
 * 5. Transaction commits/rolls back
 * 6. @AfterTransaction methods  ← HERE
 * 7. @AfterEach methods
 * 
 * Differences from @AfterEach:
 * ----------------------------
 * @AfterEach:
 *   - Runs after every test
 *   - May be inside or outside transaction
 * 
 * @AfterTransaction:
 *   - Runs only after transactional tests
 *   - Always outside transaction
 *   - Specifically for post-transaction logic
 * 
 * Best Practices:
 * ---------------
 * 1. Use for verification outside transaction
 * 2. Check final database state
 * 3. Validate transaction outcomes
 * 4. Keep cleanup logic lightweight
 * 5. Log transaction completion
 * 6. Release transaction-specific resources
 * 7. Document post-transaction checks
 * 8. Avoid starting new transactions
 * 
 * Common Patterns:
 * ----------------
 * 1. Verify rollback occurred
 * 2. Check committed data persists
 * 3. Validate referential integrity
 * 4. Audit transaction results
 * 5. Clean up test artifacts
 * 6. Log transaction metrics
 * 7. Verify constraints enforced
 * 8. Check cascade operations
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Test configuration
@Configuration
class AfterTransactionTestConfig {
    
    @Bean
    public DataSource dataSource() {
        return new SimulatedDataSource();
    }
    
    @Bean
    public TransferRepository transferRepository(DataSource dataSource) {
        return new TransferRepository(dataSource);
    }
    
    @Bean
    public TransactionAuditor auditor() {
        return new TransactionAuditor();
    }
}

// Simulated DataSource
class SimulatedDataSource implements DataSource {
    private final List<Transfer> transfers = new ArrayList<>();
    private final List<Transfer> committedTransfers = new ArrayList<>();
    private int nextId = 1;
    private boolean transactionActive = false;
    
    public void beginTransaction() {
        transactionActive = true;
        System.out.println("  [DB] Transaction started");
    }
    
    public void commit() {
        committedTransfers.addAll(transfers);
        transactionActive = false;
        System.out.println("  [DB] Transaction committed (" + transfers.size() + " items)");
    }
    
    public void rollback() {
        transfers.clear();
        transactionActive = false;
        System.out.println("  [DB] Transaction rolled back");
    }
    
    public Transfer save(Transfer transfer) {
        if (transfer.getId() == null) {
            transfer.setId(nextId++);
        }
        transfers.add(transfer);
        System.out.println("  [DB] Saved transfer: " + transfer.getDescription() + 
            " ($" + transfer.getAmount() + ")");
        return transfer;
    }
    
    public int countPending() {
        return transfers.size();
    }
    
    public int countCommitted() {
        return committedTransfers.size();
    }
    
    public List<Transfer> findAllCommitted() {
        return new ArrayList<>(committedTransfers);
    }
    
    public void clearAll() {
        transfers.clear();
        committedTransfers.clear();
        nextId = 1;
        System.out.println("  [DB] All data cleared");
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
class Transfer {
    private Integer id;
    private String description;
    private double amount;
    private String status;
    
    public Transfer(String description, double amount) {
        this.description = description;
        this.amount = amount;
        this.status = "PENDING";
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

// Repository
class TransferRepository {
    private final SimulatedDataSource dataSource;
    
    public TransferRepository(DataSource dataSource) {
        this.dataSource = (SimulatedDataSource) dataSource;
    }
    
    public Transfer save(Transfer transfer) {
        return dataSource.save(transfer);
    }
    
    public int countPending() {
        return dataSource.countPending();
    }
    
    public int countCommitted() {
        return dataSource.countCommitted();
    }
    
    public List<Transfer> findAllCommitted() {
        return dataSource.findAllCommitted();
    }
    
    public void beginTransaction() {
        dataSource.beginTransaction();
    }
    
    public void commit() {
        dataSource.commit();
    }
    
    public void rollback() {
        dataSource.rollback();
    }
    
    public void clearAll() {
        dataSource.clearAll();
    }
}

// Transaction auditor
class TransactionAuditor {
    private final List<String> auditLog = new ArrayList<>();
    
    public void auditTransactionEnd(String result, int itemCount) {
        String entry = String.format("Transaction %s: %d items", result, itemCount);
        auditLog.add(entry);
        System.out.println("  [AUDIT] " + entry);
    }
    
    public List<String> getAuditLog() {
        return new ArrayList<>(auditLog);
    }
    
    public void clear() {
        auditLog.clear();
    }
}

/**
 * Example 1: Basic After Transaction Usage
 * Demonstrates @AfterTransaction for post-transaction verification
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AfterTransactionTestConfig.class)
@Transactional
class BasicAfterTransactionTest {
    
    @Autowired
    private TransferRepository repository;
    
    @Autowired
    private TransactionAuditor auditor;
    
    @Test
    void testWithAfterTransaction() {
        System.out.println("\n=== Test: Inside Transaction ===");
        
        repository.beginTransaction();
        
        Transfer transfer = new Transfer("Test Transfer", 100.00);
        repository.save(transfer);
        
        assertEquals(1, repository.countPending());
        
        System.out.println("  Transaction will commit/rollback");
        
        repository.rollback(); // Simulating rollback
    }
    
    @AfterTransaction
    void verifyAfterTransaction() {
        System.out.println("\n=== @AfterTransaction: Verify ===");
        System.out.println("  Executing AFTER transaction ended");
        
        // Verify rollback occurred
        assertEquals(0, repository.countCommitted(), 
            "Should have 0 committed items after rollback");
        
        auditor.auditTransactionEnd("ROLLBACK", 0);
        
        System.out.println("✓ Transaction outcome verified");
    }
}

/**
 * Example 2: Verify Committed Data
 * Demonstrates verifying data after commit
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AfterTransactionTestConfig.class)
@Transactional
class VerifyCommittedDataTest {
    
    @Autowired
    private TransferRepository repository;
    
    @Test
    @org.springframework.test.annotation.Commit
    void testCommitTransaction() {
        System.out.println("\n=== Test: Commit Transaction ===");
        
        repository.beginTransaction();
        
        Transfer transfer1 = new Transfer("Transfer 1", 200.00);
        Transfer transfer2 = new Transfer("Transfer 2", 300.00);
        
        repository.save(transfer1);
        repository.save(transfer2);
        
        assertEquals(2, repository.countPending());
        
        repository.commit(); // Simulating commit
        
        System.out.println("  Transaction committed");
    }
    
    @AfterTransaction
    void verifyCommittedData() {
        System.out.println("\n=== @AfterTransaction: Verify Committed Data ===");
        
        int committed = repository.countCommitted();
        System.out.println("  Committed transfers: " + committed);
        
        assertEquals(2, committed, "Should have 2 committed items");
        
        List<Transfer> transfers = repository.findAllCommitted();
        assertEquals(2, transfers.size());
        
        System.out.println("✓ Committed data verified");
    }
}

/**
 * Example 3: Multiple After Transaction Methods
 * Demonstrates multiple @AfterTransaction methods
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AfterTransactionTestConfig.class)
@Transactional
class MultipleAfterTransactionTest {
    
    @Autowired
    private TransferRepository repository;
    
    @Autowired
    private TransactionAuditor auditor;
    
    @Test
    void testWithMultipleAfterMethods() {
        System.out.println("\n=== Test: Multiple After Methods ===");
        
        repository.beginTransaction();
        
        Transfer transfer = new Transfer("Multi-Test Transfer", 400.00);
        repository.save(transfer);
        
        repository.rollback();
    }
    
    @AfterTransaction
    void firstAfterTransaction() {
        System.out.println("\n=== @AfterTransaction #1 ===");
        assertEquals(0, repository.countCommitted());
        System.out.println("  First verification complete");
    }
    
    @AfterTransaction
    void secondAfterTransaction() {
        System.out.println("\n=== @AfterTransaction #2 ===");
        auditor.auditTransactionEnd("COMPLETE", repository.countCommitted());
        System.out.println("  Second verification complete");
    }
    
    @AfterTransaction
    void thirdAfterTransaction() {
        System.out.println("\n=== @AfterTransaction #3 ===");
        assertTrue(auditor.getAuditLog().size() >= 1);
        System.out.println("  Third verification complete");
    }
}

/**
 * Example 4: Transaction Lifecycle Logging
 * Demonstrates logging complete transaction lifecycle
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AfterTransactionTestConfig.class)
@Transactional
class TransactionLifecycleTest {
    
    @Autowired
    private TransferRepository repository;
    
    @Autowired
    private TransactionAuditor auditor;
    
    @org.springframework.test.context.transaction.BeforeTransaction
    void logBeforeTransaction() {
        System.out.println("\n=== @BeforeTransaction: Log ===");
        System.out.println("  Transaction about to start");
    }
    
    @org.junit.jupiter.api.BeforeEach
    void beforeEach() {
        System.out.println("\n=== @BeforeEach ===");
        System.out.println("  Before each test method");
    }
    
    @Test
    void testLifecycle() {
        System.out.println("\n=== Test: Transaction Lifecycle ===");
        
        repository.beginTransaction();
        
        Transfer transfer = new Transfer("Lifecycle Transfer", 500.00);
        repository.save(transfer);
        
        repository.commit();
        
        System.out.println("  Test execution complete");
    }
    
    @AfterTransaction
    void logAfterTransaction() {
        System.out.println("\n=== @AfterTransaction: Log ===");
        
        int committed = repository.countCommitted();
        auditor.auditTransactionEnd("COMMITTED", committed);
        
        System.out.println("  Transaction ended, " + committed + " items committed");
    }
    
    @org.junit.jupiter.api.AfterEach
    void afterEach() {
        System.out.println("\n=== @AfterEach ===");
        System.out.println("  After each test method");
    }
}

/**
 * Example 5: Validate Transaction Outcomes
 * Demonstrates validating different transaction outcomes
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AfterTransactionTestConfig.class)
@Transactional
class ValidateOutcomesTest {
    
    @Autowired
    private TransferRepository repository;
    
    private boolean wasCommitted = false;
    
    @Test
    void testRollbackOutcome() {
        System.out.println("\n=== Test: Rollback Outcome ===");
        
        repository.beginTransaction();
        
        Transfer transfer = new Transfer("Rollback Transfer", 600.00);
        repository.save(transfer);
        
        repository.rollback();
        wasCommitted = false;
        
        System.out.println("  Transaction rolled back");
    }
    
    @AfterTransaction
    void validateRollbackOutcome() {
        System.out.println("\n=== @AfterTransaction: Validate Rollback ===");
        
        if (!wasCommitted) {
            assertEquals(0, repository.countCommitted(), 
                "No data should be committed after rollback");
            System.out.println("✓ Rollback validated");
        }
    }
}

/**
 * Example 6: Cleanup Resources
 * Demonstrates cleanup after transaction
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AfterTransactionTestConfig.class)
@Transactional
class CleanupResourcesTest {
    
    @Autowired
    private TransferRepository repository;
    
    @Autowired
    private TransactionAuditor auditor;
    
    @Test
    void testWithCleanup() {
        System.out.println("\n=== Test: With Cleanup ===");
        
        repository.beginTransaction();
        
        Transfer transfer = new Transfer("Cleanup Transfer", 700.00);
        repository.save(transfer);
        
        repository.rollback();
    }
    
    @AfterTransaction
    void cleanupResources() {
        System.out.println("\n=== @AfterTransaction: Cleanup ===");
        
        // Clean up resources
        System.out.println("  Clearing audit log...");
        auditor.clear();
        
        System.out.println("  Clearing repository data...");
        repository.clearAll();
        
        // Verify cleanup
        assertEquals(0, repository.countCommitted());
        assertEquals(0, auditor.getAuditLog().size());
        
        System.out.println("✓ Resources cleaned up");
    }
}

/**
 * Main class for demonstration
 */
public class AfterTransactionPattern {
    
    public static void main(String[] args) {
        System.out.println("=== After Transaction Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. @AfterTransaction basic usage");
        System.out.println("2. Verifying committed data");
        System.out.println("3. Multiple @AfterTransaction methods");
        System.out.println("4. Transaction lifecycle logging");
        System.out.println("5. Validating transaction outcomes");
        System.out.println("6. Resource cleanup after transaction");
        System.out.println("7. Execution order with other annotations");
        System.out.println("8. Transaction result auditing");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * After Transaction Summary:
 * 
 * Basic Usage:
 * ------------
 * @Transactional
 * class MyTest {
 *     
 *     @Test
 *     void test() {
 *         // Runs INSIDE transaction
 *     }
 *     
 *     @AfterTransaction
 *     void verify() {
 *         // Runs AFTER transaction ends
 *         // Outside transaction context
 *     }
 * }
 * 
 * Multiple Methods:
 * -----------------
 * @Transactional
 * class MyTest {
 *     
 *     @Test
 *     void test() {
 *         // Test
 *     }
 *     
 *     @AfterTransaction
 *     void verify1() {
 *         // First verification
 *     }
 *     
 *     @AfterTransaction
 *     void verify2() {
 *         // Second verification
 *     }
 * }
 * 
 * Complete Lifecycle:
 * -------------------
 * @Transactional
 * class MyTest {
 *     
 *     @BeforeTransaction
 *     void beforeTx() {
 *         // 1. Before transaction starts
 *     }
 *     
 *     @BeforeEach
 *     void beforeEach() {
 *         // 2. Before each test
 *     }
 *     
 *     @Test
 *     void test() {
 *         // 3. Test execution (in transaction)
 *     }
 *     
 *     @AfterTransaction
 *     void afterTx() {
 *         // 4. After transaction ends
 *     }
 *     
 *     @AfterEach
 *     void afterEach() {
 *         // 5. After each test
 *     }
 * }
 * 
 * Verify Committed Data:
 * ----------------------
 * @AfterTransaction
 * void verifyCommit() {
 *     // Verify data was committed
 *     int count = repository.count();
 *     assertEquals(expected, count);
 * }
 * 
 * Verify Rollback:
 * ----------------
 * @AfterTransaction
 * void verifyRollback() {
 *     // Verify data was rolled back
 *     int count = repository.count();
 *     assertEquals(0, count);
 * }
 */
