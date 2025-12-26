package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Execution Listener Pattern
 * =================================
 * 
 * Demonstrates the TestExecutionListener pattern for hooking into the
 * test execution lifecycle in Spring Test Framework.
 * 
 * Use Cases:
 * ----------
 * 1. Custom test lifecycle hooks
 * 2. Test data setup and cleanup
 * 3. Performance monitoring
 * 4. Custom logging and reporting
 * 5. Database state management
 * 6. Resource allocation/deallocation
 * 7. Test execution metrics
 * 8. Custom test validation
 * 
 * Key Features:
 * -------------
 * - Hook into test execution lifecycle
 * - Access TestContext during execution
 * - Execute code before/after test methods
 * - Execute code before/after test classes
 * - Modify ApplicationContext
 * - Track test execution state
 * - Chain multiple listeners
 * - Order listener execution
 * 
 * Lifecycle Callbacks:
 * --------------------
 * 1. beforeTestClass(TestContext)
 * 2. prepareTestInstance(TestContext)
 * 3. beforeTestMethod(TestContext)
 * 4. beforeTestExecution(TestContext)
 * 5. afterTestExecution(TestContext)
 * 6. afterTestMethod(TestContext)
 * 7. afterTestClass(TestContext)
 * 
 * Execution Order:
 * ----------------
 * Class Setup:
 *   beforeTestClass()
 *   prepareTestInstance()
 * 
 * Each Test:
 *   beforeTestMethod()
 *   beforeTestExecution()
 *   [TEST EXECUTION]
 *   afterTestExecution()
 *   afterTestMethod()
 * 
 * Class Teardown:
 *   afterTestClass()
 * 
 * Implementation Options:
 * -----------------------
 * 1. Implement TestExecutionListener interface
 * 2. Extend AbstractTestExecutionListener
 * 3. Use default methods (Java 8+)
 * 
 * Registration Methods:
 * ---------------------
 * 1. @TestExecutionListeners annotation
 * 2. META-INF/spring.factories
 * 3. Programmatic registration
 * 
 * Best Practices:
 * ---------------
 * 1. Keep listeners focused and lightweight
 * 2. Use AbstractTestExecutionListener
 * 3. Handle exceptions appropriately
 * 4. Document listener behavior
 * 5. Consider listener order
 * 6. Avoid heavy operations
 * 7. Clean up resources properly
 * 8. Use for cross-cutting concerns
 * 
 * Common Patterns:
 * ----------------
 * 1. Database setup/cleanup
 * 2. Performance timing
 * 3. Test logging
 * 4. Resource management
 * 5. Test data initialization
 * 6. Metric collection
 * 7. Custom validation
 * 8. State management
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Configuration
@Configuration
class ListenerTestConfig {
    
    @Bean
    public TestService testService() {
        return new TestService();
    }
    
    @Bean
    public DataService dataService() {
        return new DataService();
    }
}

// Services
class TestService {
    private int callCount = 0;
    
    public String process(String input) {
        callCount++;
        return "Processed: " + input;
    }
    
    public int getCallCount() {
        return callCount;
    }
    
    public void reset() {
        callCount = 0;
    }
}

class DataService {
    private final java.util.List<String> data = new java.util.ArrayList<>();
    
    public void addData(String item) {
        data.add(item);
    }
    
    public int getDataCount() {
        return data.size();
    }
    
    public void clearData() {
        data.clear();
    }
}

/**
 * Custom Test Execution Listener 1
 * Logging-focused listener
 */
class LoggingTestExecutionListener extends AbstractTestExecutionListener {
    
    @Override
    public void beforeTestClass(TestContext testContext) {
        System.out.println("\n[LISTENER] Before Test Class: " + 
            testContext.getTestClass().getSimpleName());
    }
    
    @Override
    public void prepareTestInstance(TestContext testContext) {
        System.out.println("[LISTENER] Prepare Test Instance");
    }
    
    @Override
    public void beforeTestMethod(TestContext testContext) {
        System.out.println("[LISTENER] Before Test Method: " + 
            testContext.getTestMethod().getName());
    }
    
    @Override
    public void beforeTestExecution(TestContext testContext) {
        System.out.println("[LISTENER] Before Test Execution");
    }
    
    @Override
    public void afterTestExecution(TestContext testContext) {
        System.out.println("[LISTENER] After Test Execution");
    }
    
    @Override
    public void afterTestMethod(TestContext testContext) {
        System.out.println("[LISTENER] After Test Method: " + 
            testContext.getTestMethod().getName());
    }
    
    @Override
    public void afterTestClass(TestContext testContext) {
        System.out.println("[LISTENER] After Test Class: " + 
            testContext.getTestClass().getSimpleName());
    }
}

/**
 * Custom Test Execution Listener 2
 * Performance monitoring listener
 */
class PerformanceTestExecutionListener extends AbstractTestExecutionListener {
    
    private long startTime;
    private long totalTime = 0;
    private int testCount = 0;
    
    @Override
    public void beforeTestClass(TestContext testContext) {
        totalTime = 0;
        testCount = 0;
        System.out.println("[PERF] Starting performance monitoring");
    }
    
    @Override
    public void beforeTestExecution(TestContext testContext) {
        startTime = System.nanoTime();
    }
    
    @Override
    public void afterTestExecution(TestContext testContext) {
        long duration = System.nanoTime() - startTime;
        totalTime += duration;
        testCount++;
        
        System.out.println(String.format("[PERF] Test '%s' took %.2f ms", 
            testContext.getTestMethod().getName(),
            duration / 1_000_000.0));
    }
    
    @Override
    public void afterTestClass(TestContext testContext) {
        double avgTime = totalTime / (double) testCount / 1_000_000.0;
        
        System.out.println(String.format("[PERF] Total tests: %d, Average time: %.2f ms", 
            testCount, avgTime));
    }
}

/**
 * Custom Test Execution Listener 3
 * Data cleanup listener
 */
class DataCleanupTestExecutionListener extends AbstractTestExecutionListener {
    
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        System.out.println("[CLEANUP] Cleaning up test data");
        
        // Get DataService bean and clean up
        if (testContext.getApplicationContext().containsBean("dataService")) {
            DataService dataService = testContext.getApplicationContext()
                .getBean(DataService.class);
            dataService.clearData();
            System.out.println("[CLEANUP] Data cleared");
        }
    }
}

/**
 * Custom Test Execution Listener 4
 * Test validation listener
 */
class ValidationTestExecutionListener extends AbstractTestExecutionListener {
    
    @Override
    public void beforeTestExecution(TestContext testContext) {
        System.out.println("[VALIDATION] Validating test preconditions");
        
        // Validate test context
        assertNotNull(testContext.getApplicationContext(), 
            "ApplicationContext should be available");
        assertNotNull(testContext.getTestMethod(), 
            "Test method should be available");
        
        System.out.println("[VALIDATION] Preconditions valid");
    }
    
    @Override
    public void afterTestExecution(TestContext testContext) {
        System.out.println("[VALIDATION] Validating test postconditions");
        
        // Validate test results
        Exception exception = testContext.getTestException();
        if (exception == null) {
            System.out.println("[VALIDATION] Test passed successfully");
        } else {
            System.out.println("[VALIDATION] Test failed: " + exception.getMessage());
        }
    }
}

/**
 * Example 1: Single Listener
 * Demonstrates using a single custom listener
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ListenerTestConfig.class)
@org.springframework.test.context.TestExecutionListeners(
    listeners = LoggingTestExecutionListener.class,
    mergeMode = org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
class SingleListenerTest {
    
    @Autowired
    private TestService testService;
    
    @Test
    void testOne() {
        System.out.println("  -> Executing testOne");
        
        String result = testService.process("test1");
        assertEquals("Processed: test1", result);
    }
    
    @Test
    void testTwo() {
        System.out.println("  -> Executing testTwo");
        
        String result = testService.process("test2");
        assertEquals("Processed: test2", result);
    }
}

/**
 * Example 2: Multiple Listeners
 * Demonstrates using multiple listeners together
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ListenerTestConfig.class)
@org.springframework.test.context.TestExecutionListeners(
    listeners = {
        LoggingTestExecutionListener.class,
        PerformanceTestExecutionListener.class
    },
    mergeMode = org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
class MultipleListenersTest {
    
    @Autowired
    private TestService testService;
    
    @Test
    void testPerformance1() {
        System.out.println("  -> Executing testPerformance1");
        
        String result = testService.process("perf1");
        assertEquals("Processed: perf1", result);
    }
    
    @Test
    void testPerformance2() {
        System.out.println("  -> Executing testPerformance2");
        
        String result = testService.process("perf2");
        assertEquals("Processed: perf2", result);
    }
    
    @Test
    void testPerformance3() {
        System.out.println("  -> Executing testPerformance3");
        
        String result = testService.process("perf3");
        assertEquals("Processed: perf3", result);
    }
}

/**
 * Example 3: Data Cleanup Listener
 * Demonstrates automatic data cleanup
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ListenerTestConfig.class)
@org.springframework.test.context.TestExecutionListeners(
    listeners = DataCleanupTestExecutionListener.class,
    mergeMode = org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
class DataCleanupListenerTest {
    
    @Autowired
    private DataService dataService;
    
    @Test
    void testDataCleanupFirst() {
        System.out.println("\n  -> Executing testDataCleanupFirst");
        
        dataService.addData("item1");
        dataService.addData("item2");
        
        assertEquals(2, dataService.getDataCount());
        
        System.out.println("  Data will be cleaned up after test");
    }
    
    @Test
    void testDataCleanupSecond() {
        System.out.println("\n  -> Executing testDataCleanupSecond");
        
        // Data should be clean from previous test
        assertEquals(0, dataService.getDataCount(), 
            "Data should be cleaned by listener");
        
        dataService.addData("item3");
        assertEquals(1, dataService.getDataCount());
        
        System.out.println("  Data will be cleaned up after test");
    }
}

/**
 * Example 4: Validation Listener
 * Demonstrates test validation with listener
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ListenerTestConfig.class)
@org.springframework.test.context.TestExecutionListeners(
    listeners = ValidationTestExecutionListener.class,
    mergeMode = org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
class ValidationListenerTest {
    
    @Autowired
    private TestService testService;
    
    @Test
    void testWithValidation() {
        System.out.println("\n  -> Executing testWithValidation");
        
        String result = testService.process("validated");
        assertEquals("Processed: validated", result);
        
        System.out.println("  Test will be validated by listener");
    }
}

/**
 * Example 5: All Listeners Combined
 * Demonstrates using all custom listeners together
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ListenerTestConfig.class)
@org.springframework.test.context.TestExecutionListeners(
    listeners = {
        LoggingTestExecutionListener.class,
        PerformanceTestExecutionListener.class,
        DataCleanupTestExecutionListener.class,
        ValidationTestExecutionListener.class
    },
    mergeMode = org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
class AllListenersTest {
    
    @Autowired
    private TestService testService;
    
    @Autowired
    private DataService dataService;
    
    @Test
    void testWithAllListeners() {
        System.out.println("\n  -> Executing testWithAllListeners");
        
        String result = testService.process("all-listeners");
        assertEquals("Processed: all-listeners", result);
        
        dataService.addData("test-data");
        assertEquals(1, dataService.getDataCount());
        
        System.out.println("  All listeners are active");
    }
}

/**
 * Main class for demonstration
 */
public class TestExecutionListenerPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Test Execution Listener Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Custom test execution listeners");
        System.out.println("2. Lifecycle callback methods");
        System.out.println("3. Logging listener implementation");
        System.out.println("4. Performance monitoring listener");
        System.out.println("5. Data cleanup listener");
        System.out.println("6. Validation listener");
        System.out.println("7. Multiple listeners working together");
        System.out.println("8. Listener execution order");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * Test Execution Listener Summary:
 * 
 * Custom Listener Implementation:
 * --------------------------------
 * public class MyListener extends AbstractTestExecutionListener {
 *     
 *     @Override
 *     public void beforeTestClass(TestContext context) {
 *         // Setup before all tests in class
 *     }
 *     
 *     @Override
 *     public void beforeTestMethod(TestContext context) {
 *         // Setup before each test method
 *     }
 *     
 *     @Override
 *     public void afterTestMethod(TestContext context) {
 *         // Cleanup after each test method
 *     }
 *     
 *     @Override
 *     public void afterTestClass(TestContext context) {
 *         // Cleanup after all tests in class
 *     }
 * }
 * 
 * Register Listener:
 * ------------------
 * @TestExecutionListeners(
 *     listeners = MyListener.class,
 *     mergeMode = MergeMode.MERGE_WITH_DEFAULTS
 * )
 * class MyTest { }
 * 
 * Multiple Listeners:
 * -------------------
 * @TestExecutionListeners(
 *     listeners = {
 *         Listener1.class,
 *         Listener2.class,
 *         Listener3.class
 *     },
 *     mergeMode = MergeMode.MERGE_WITH_DEFAULTS
 * )
 * class MyTest { }
 * 
 * Access Application Context:
 * ---------------------------
 * public class MyListener extends AbstractTestExecutionListener {
 *     @Override
 *     public void beforeTestMethod(TestContext context) {
 *         ApplicationContext appContext = context.getApplicationContext();
 *         MyBean bean = appContext.getBean(MyBean.class);
 *         // Use bean
 *     }
 * }
 * 
 * Merge Modes:
 * ------------
 * MERGE_WITH_DEFAULTS - Merge with default listeners
 * REPLACE_DEFAULTS    - Replace all default listeners
 */
