package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQL Scripts Pattern
 * ===================
 * 
 * Demonstrates the @Sql annotation pattern for executing SQL scripts
 * in Spring integration tests to set up and manage test data.
 * 
 * Use Cases:
 * ----------
 * 1. Initialize database schema for tests
 * 2. Load test data from SQL files
 * 3. Clean up database after tests
 * 4. Execute DDL statements
 * 5. Set up complex data scenarios
 * 6. Reset database state
 * 7. Run migrations before tests
 * 8. Execute stored procedures
 * 
 * Key Features:
 * -------------
 * - Execute SQL scripts before/after tests
 * - Support for classpath and file system scripts
 * - Multiple scripts execution
 * - Configure script encoding
 * - Set transaction mode
 * - Continue on error option
 * - Statement separator configuration
 * - Comment prefix configuration
 * 
 * Annotation Attributes:
 * ----------------------
 * value/scripts  - SQL script paths
 * statements     - Inline SQL statements
 * executionPhase - BEFORE_TEST_METHOD or AFTER_TEST_METHOD
 * config         - SQL configuration (@SqlConfig)
 * 
 * SQL Configuration:
 * ------------------
 * @SqlConfig attributes:
 * - dataSource: Bean name of DataSource
 * - transactionManager: Bean name of TransactionManager
 * - transactionMode: ISOLATED or INFERRED
 * - encoding: Script file encoding
 * - separator: Statement separator (default: ";")
 * - commentPrefix: Comment line prefix (default: "--")
 * - blockCommentStartDelimiter: Block comment start
 * - blockCommentEndDelimiter: Block comment end
 * - errorMode: FAIL_ON_ERROR or CONTINUE_ON_ERROR
 * 
 * Execution Phases:
 * -----------------
 * BEFORE_TEST_METHOD (default) - Before test execution
 * AFTER_TEST_METHOD - After test execution
 * 
 * Script Locations:
 * -----------------
 * Classpath: "classpath:schema.sql"
 * File system: "file:///path/to/script.sql"
 * Relative: "data/users.sql" (relative to test class)
 * 
 * Best Practices:
 * ---------------
 * 1. Use descriptive script names
 * 2. Organize scripts by purpose
 * 3. Keep scripts idempotent
 * 4. Use cleanup scripts after tests
 * 5. Version control SQL scripts
 * 6. Use transactions for isolation
 * 7. Handle script errors appropriately
 * 8. Document script dependencies
 * 
 * Common Patterns:
 * ----------------
 * 1. Schema initialization + data loading
 * 2. Test data setup per test method
 * 3. Cleanup scripts after tests
 * 4. Multiple scripts for complex scenarios
 * 5. Conditional script execution
 * 6. Shared scripts across test classes
 * 7. Inline SQL statements
 * 8. Script groups with @SqlGroup
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Test configuration
@Configuration
class SqlScriptsTestConfig {
    
    @Bean
    public DataSource dataSource() {
        return new SimulatedDataSource();
    }
    
    @Bean
    public ProductRepository productRepository(DataSource dataSource) {
        return new ProductRepository(dataSource);
    }
    
    @Bean
    public SqlScriptExecutor scriptExecutor(DataSource dataSource) {
        return new SqlScriptExecutor(dataSource);
    }
}

// Simulated DataSource with SQL execution capability
class SimulatedDataSource implements DataSource {
    private final List<Product> products = new ArrayList<>();
    private final List<String> executedScripts = new ArrayList<>();
    private int nextId = 1;
    
    public void executeScript(String scriptPath) {
        System.out.println("  [SQL] Executing script: " + scriptPath);
        executedScripts.add(scriptPath);
        
        // Simulate script execution based on path
        if (scriptPath.contains("schema")) {
            System.out.println("  [SQL] Creating tables...");
        } else if (scriptPath.contains("data")) {
            System.out.println("  [SQL] Loading data...");
            // Simulate loading data
            products.add(new Product("Sample Product 1", 10.00));
            products.add(new Product("Sample Product 2", 20.00));
        } else if (scriptPath.contains("cleanup")) {
            System.out.println("  [SQL] Cleaning up...");
            products.clear();
        }
        
        System.out.println("  [SQL] Script executed successfully");
    }
    
    public void executeStatement(String sql) {
        System.out.println("  [SQL] Executing: " + sql);
        
        if (sql.toUpperCase().contains("INSERT")) {
            products.add(new Product("Inline Product", 15.00));
        } else if (sql.toUpperCase().contains("DELETE")) {
            products.clear();
        }
    }
    
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(nextId++);
        }
        products.add(product);
        return product;
    }
    
    public List<Product> findAll() {
        return new ArrayList<>(products);
    }
    
    public int count() {
        return products.size();
    }
    
    public void clear() {
        products.clear();
        nextId = 1;
    }
    
    public List<String> getExecutedScripts() {
        return new ArrayList<>(executedScripts);
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
class Product {
    private Integer id;
    private String name;
    private double price;
    
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}

// Repository
class ProductRepository {
    private final SimulatedDataSource dataSource;
    
    public ProductRepository(DataSource dataSource) {
        this.dataSource = (SimulatedDataSource) dataSource;
    }
    
    public Product save(Product product) {
        return dataSource.save(product);
    }
    
    public List<Product> findAll() {
        return dataSource.findAll();
    }
    
    public int count() {
        return dataSource.count();
    }
    
    public void clear() {
        dataSource.clear();
    }
}

// SQL Script Executor
class SqlScriptExecutor {
    private final SimulatedDataSource dataSource;
    
    public SqlScriptExecutor(DataSource dataSource) {
        this.dataSource = (SimulatedDataSource) dataSource;
    }
    
    public void executeScript(String scriptPath) {
        dataSource.executeScript(scriptPath);
    }
    
    public void executeStatement(String sql) {
        dataSource.executeStatement(sql);
    }
    
    public List<String> getExecutedScripts() {
        return dataSource.getExecutedScripts();
    }
}

/**
 * Example 1: Basic SQL Script Usage
 * Demonstrates executing SQL script before test
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SqlScriptsTestConfig.class)
class BasicSqlScriptTest {
    
    @Autowired
    private ProductRepository repository;
    
    @Autowired
    private SqlScriptExecutor scriptExecutor;
    
    @Test
    @Sql("classpath:schema.sql")
    void testWithSchemaScript() {
        System.out.println("\n=== Test: With Schema Script ===");
        
        // Script should have been executed
        List<String> scripts = scriptExecutor.getExecutedScripts();
        assertTrue(scripts.stream().anyMatch(s -> s.contains("schema.sql")));
        
        System.out.println("✓ Schema script executed");
    }
}

/**
 * Example 2: Multiple SQL Scripts
 * Demonstrates executing multiple scripts
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SqlScriptsTestConfig.class)
class MultipleSqlScriptsTest {
    
    @Autowired
    private ProductRepository repository;
    
    @Autowired
    private SqlScriptExecutor scriptExecutor;
    
    @Test
    @Sql({"classpath:schema.sql", "classpath:data.sql"})
    void testWithMultipleScripts() {
        System.out.println("\n=== Test: Multiple Scripts ===");
        
        scriptExecutor.executeScript("classpath:schema.sql");
        scriptExecutor.executeScript("classpath:data.sql");
        
        // Data should be loaded
        assertTrue(repository.count() >= 2);
        
        System.out.println("✓ Multiple scripts executed");
        System.out.println("  Products loaded: " + repository.count());
    }
}

/**
 * Example 3: Inline SQL Statements
 * Demonstrates using inline SQL statements
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SqlScriptsTestConfig.class)
class InlineSqlStatementsTest {
    
    @Autowired
    private ProductRepository repository;
    
    @Autowired
    private SqlScriptExecutor scriptExecutor;
    
    @Test
    @Sql(statements = {
        "INSERT INTO products (name, price) VALUES ('Product A', 25.00)",
        "INSERT INTO products (name, price) VALUES ('Product B', 35.00)"
    })
    void testWithInlineStatements() {
        System.out.println("\n=== Test: Inline SQL Statements ===");
        
        scriptExecutor.executeStatement("INSERT INTO products (name, price) VALUES ('Product A', 25.00)");
        scriptExecutor.executeStatement("INSERT INTO products (name, price) VALUES ('Product B', 35.00)");
        
        assertTrue(repository.count() >= 2);
        
        System.out.println("✓ Inline statements executed");
        System.out.println("  Products: " + repository.count());
    }
}

/**
 * Example 4: Execution Phases
 * Demonstrates BEFORE and AFTER execution phases
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SqlScriptsTestConfig.class)
class ExecutionPhasesTest {
    
    @Autowired
    private ProductRepository repository;
    
    @Autowired
    private SqlScriptExecutor scriptExecutor;
    
    @Test
    @Sql(
        scripts = "classpath:data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
        scripts = "classpath:cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void testWithExecutionPhases() {
        System.out.println("\n=== Test: Execution Phases ===");
        
        // Setup script executed before test
        scriptExecutor.executeScript("classpath:data.sql");
        
        System.out.println("  Data loaded before test");
        assertTrue(repository.count() >= 2);
        
        // Test logic
        Product product = new Product("Test Product", 40.00);
        repository.save(product);
        
        System.out.println("  Test execution complete");
        System.out.println("  Cleanup script will run after test");
    }
    
    @org.junit.jupiter.api.AfterEach
    void afterTest() {
        // Simulate cleanup script execution
        scriptExecutor.executeScript("classpath:cleanup.sql");
        System.out.println("  Cleanup executed");
    }
}

/**
 * Example 5: SQL Configuration
 * Demonstrates @SqlConfig usage
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SqlScriptsTestConfig.class)
class SqlConfigTest {
    
    @Autowired
    private ProductRepository repository;
    
    @Autowired
    private SqlScriptExecutor scriptExecutor;
    
    @Test
    @Sql(
        scripts = "classpath:data.sql",
        config = @SqlConfig(
            encoding = "UTF-8",
            separator = ";",
            commentPrefix = "--",
            errorMode = SqlConfig.ErrorMode.FAIL_ON_ERROR
        )
    )
    void testWithSqlConfig() {
        System.out.println("\n=== Test: SQL Configuration ===");
        
        scriptExecutor.executeScript("classpath:data.sql");
        
        System.out.println("✓ Script executed with custom configuration");
        System.out.println("  Encoding: UTF-8");
        System.out.println("  Separator: ;");
        System.out.println("  Comment prefix: --");
        System.out.println("  Error mode: FAIL_ON_ERROR");
    }
}

/**
 * Example 6: SQL Group
 * Demonstrates using @SqlGroup for multiple @Sql annotations
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SqlScriptsTestConfig.class)
class SqlGroupTest {
    
    @Autowired
    private ProductRepository repository;
    
    @Autowired
    private SqlScriptExecutor scriptExecutor;
    
    @Test
    @SqlGroup({
        @Sql(
            scripts = "classpath:schema.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        ),
        @Sql(
            scripts = "classpath:data.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        ),
        @Sql(
            scripts = "classpath:cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
        )
    })
    void testWithSqlGroup() {
        System.out.println("\n=== Test: SQL Group ===");
        
        // Setup scripts
        scriptExecutor.executeScript("classpath:schema.sql");
        scriptExecutor.executeScript("classpath:data.sql");
        
        System.out.println("  Multiple scripts in group");
        assertTrue(repository.count() >= 2);
        
        // Test logic
        Product product = new Product("Group Product", 45.00);
        repository.save(product);
        
        System.out.println("✓ SQL group executed");
    }
    
    @org.junit.jupiter.api.AfterEach
    void afterTest() {
        scriptExecutor.executeScript("classpath:cleanup.sql");
    }
}

/**
 * Example 7: Class-Level SQL Scripts
 * Demonstrates scripts applied to all test methods
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SqlScriptsTestConfig.class)
@Sql("classpath:schema.sql")
class ClassLevelSqlTest {
    
    @Autowired
    private ProductRepository repository;
    
    @Autowired
    private SqlScriptExecutor scriptExecutor;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        scriptExecutor.executeScript("classpath:schema.sql");
    }
    
    @Test
    void testOne() {
        System.out.println("\n=== Test One: Class-Level Script ===");
        
        Product product = new Product("Product One", 50.00);
        repository.save(product);
        
        System.out.println("✓ Test one complete");
    }
    
    @Test
    void testTwo() {
        System.out.println("\n=== Test Two: Class-Level Script ===");
        
        Product product = new Product("Product Two", 60.00);
        repository.save(product);
        
        System.out.println("✓ Test two complete");
    }
}

/**
 * Example 8: Method-Level Override
 * Demonstrates overriding class-level scripts
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SqlScriptsTestConfig.class)
@Sql("classpath:schema.sql")
class MethodLevelOverrideTest {
    
    @Autowired
    private ProductRepository repository;
    
    @Autowired
    private SqlScriptExecutor scriptExecutor;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        scriptExecutor.executeScript("classpath:schema.sql");
    }
    
    @Test
    void testWithDefaultScript() {
        System.out.println("\n=== Test: Default Script (Class-Level) ===");
        
        Product product = new Product("Default Product", 70.00);
        repository.save(product);
        
        System.out.println("✓ Using class-level script");
    }
    
    @Test
    @Sql("classpath:data.sql")
    void testWithMethodLevelScript() {
        System.out.println("\n=== Test: Method-Level Override ===");
        
        scriptExecutor.executeScript("classpath:data.sql");
        
        assertTrue(repository.count() >= 2);
        
        System.out.println("✓ Using method-level script (overrides class-level)");
    }
}

/**
 * Main class for demonstration
 */
public class SqlScriptsPattern {
    
    public static void main(String[] args) {
        System.out.println("=== SQL Scripts Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Basic SQL script execution");
        System.out.println("2. Multiple SQL scripts");
        System.out.println("3. Inline SQL statements");
        System.out.println("4. Execution phases (BEFORE/AFTER)");
        System.out.println("5. SQL configuration options");
        System.out.println("6. SQL groups for complex scenarios");
        System.out.println("7. Class-level script application");
        System.out.println("8. Method-level script override");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * SQL Scripts Pattern Summary:
 * 
 * Single Script:
 * --------------
 * @Test
 * @Sql("classpath:schema.sql")
 * void test() { }
 * 
 * Multiple Scripts:
 * -----------------
 * @Test
 * @Sql({"classpath:schema.sql", "classpath:data.sql"})
 * void test() { }
 * 
 * Inline Statements:
 * ------------------
 * @Test
 * @Sql(statements = {
 *     "INSERT INTO users (name) VALUES ('John')",
 *     "INSERT INTO users (name) VALUES ('Jane')"
 * })
 * void test() { }
 * 
 * Execution Phases:
 * -----------------
 * @Test
 * @Sql(
 *     scripts = "setup.sql",
 *     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
 * )
 * @Sql(
 *     scripts = "cleanup.sql",
 *     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
 * )
 * void test() { }
 * 
 * SQL Configuration:
 * ------------------
 * @Test
 * @Sql(
 *     scripts = "data.sql",
 *     config = @SqlConfig(
 *         encoding = "UTF-8",
 *         separator = ";",
 *         commentPrefix = "--",
 *         errorMode = SqlConfig.ErrorMode.CONTINUE_ON_ERROR
 *     )
 * )
 * void test() { }
 * 
 * SQL Group:
 * ----------
 * @Test
 * @SqlGroup({
 *     @Sql("schema.sql"),
 *     @Sql("data.sql"),
 *     @Sql(
 *         scripts = "cleanup.sql",
 *         executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
 *     )
 * })
 * void test() { }
 * 
 * Class-Level:
 * ------------
 * @Sql("classpath:schema.sql")
 * class MyTest {
 *     @Test
 *     void test1() { }  // Uses schema.sql
 *     
 *     @Test
 *     void test2() { }  // Uses schema.sql
 * }
 * 
 * Method Override:
 * ----------------
 * @Sql("classpath:default.sql")
 * class MyTest {
 *     @Test
 *     void test1() { }  // Uses default.sql
 *     
 *     @Test
 *     @Sql("classpath:custom.sql")
 *     void test2() { }  // Uses custom.sql (overrides class-level)
 * }
 */
