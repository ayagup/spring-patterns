package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dirty Context Pattern
 * ======================
 * 
 * Demonstrates the @DirtiesContext annotation pattern for marking the Spring
 * ApplicationContext as "dirty" to force context reloading during testing.
 * 
 * Use Cases:
 * ----------
 * 1. Test modifies application context state
 * 2. Test changes bean definitions
 * 3. Test mutates singleton beans
 * 4. Integration tests with state changes
 * 5. Database state modifications
 * 6. Cache state modifications
 * 7. Testing context refresh scenarios
 * 8. Isolating tests with side effects
 * 
 * Key Features:
 * -------------
 * - Forces context reload after/before tests
 * - Can be applied at class or method level
 * - Supports different dirty modes
 * - Helps with test isolation
 * - Performance impact (context recreation)
 * - Prevents context cache pollution
 * - Supports hierarchical contexts
 * - Works with all test frameworks
 * 
 * Dirty Modes:
 * ------------
 * Class Level:
 * - BEFORE_CLASS: Reload before class
 * - BEFORE_EACH_TEST_METHOD: Reload before each test method
 * - AFTER_EACH_TEST_METHOD: Reload after each test method
 * - AFTER_CLASS: Reload after class (default)
 * 
 * Method Level:
 * - BEFORE_METHOD: Reload before this method
 * - AFTER_METHOD: Reload after this method (default)
 * 
 * Hierarchy Mode:
 * - EXHAUSTIVE: Dirty current and parent contexts (default)
 * - CURRENT_LEVEL: Dirty only current context level
 * 
 * When to Use:
 * ------------
 * ✓ Test modifies shared state
 * ✓ Test changes bean configurations
 * ✓ Test requires fresh context
 * ✓ Integration test with side effects
 * ✓ Testing context lifecycle
 * 
 * When NOT to Use:
 * ----------------
 * ✗ Read-only tests
 * ✗ Stateless operations
 * ✗ Performance-critical test suites
 * ✗ Tests can be isolated otherwise
 * ✗ Excessive use (impacts performance)
 * 
 * Performance Considerations:
 * ---------------------------
 * - Context creation is expensive
 * - Each reload takes time
 * - Minimize @DirtiesContext usage
 * - Prefer test isolation techniques
 * - Use only when necessary
 * - Group tests requiring fresh context
 * 
 * Best Practices:
 * ---------------
 * 1. Use sparingly (performance impact)
 * 2. Document why context needs reload
 * 3. Prefer method-level over class-level
 * 4. Consider alternatives first
 * 5. Group dirty tests together
 * 6. Use specific dirty modes
 * 7. Clean up in @AfterEach when possible
 * 8. Monitor test execution time
 * 
 * Alternatives to Consider:
 * -------------------------
 * 1. @BeforeEach/@AfterEach cleanup
 * 2. @Transactional with rollback
 * 3. Reset singleton state manually
 * 4. Use @MockBean for isolation
 * 5. Separate test contexts
 * 6. Database cleanup strategies
 * 7. Cache clear operations
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Test configuration
@Configuration
class DirtyContextConfig {
    
    @Bean
    public StatefulService statefulService() {
        return new StatefulService();
    }
    
    @Bean
    public CounterService counterService() {
        return new CounterService();
    }
    
    @Bean
    public CacheService cacheService() {
        return new CacheService();
    }
}

// Stateful service that maintains state
class StatefulService {
    private int counter = 0;
    private String lastValue;
    
    public void incrementCounter() {
        counter++;
    }
    
    public int getCounter() {
        return counter;
    }
    
    public void setValue(String value) {
        this.lastValue = value;
    }
    
    public String getValue() {
        return lastValue;
    }
    
    public void reset() {
        counter = 0;
        lastValue = null;
    }
}

// Counter service
class CounterService {
    private int count = 0;
    
    public void increment() {
        count++;
    }
    
    public int getCount() {
        return count;
    }
}

// Cache service
class CacheService {
    private java.util.Map<String, Object> cache = new java.util.HashMap<>();
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    public Object get(String key) {
        return cache.get(key);
    }
    
    public int size() {
        return cache.size();
    }
    
    public void clear() {
        cache.clear();
    }
}

/**
 * Example 1: Method-Level @DirtiesContext
 * Demonstrates marking individual test methods as dirty
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DirtyContextConfig.class)
class MethodLevelDirtiesContextTest {
    
    @Autowired
    private StatefulService statefulService;
    
    @Test
    void testWithoutDirtyContext() {
        statefulService.incrementCounter();
        assertEquals(1, statefulService.getCounter());
        System.out.println("✓ Test 1: Counter = " + statefulService.getCounter());
    }
    
    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    void testWithDirtyContext() {
        statefulService.incrementCounter();
        // Counter might be 1 or 2 depending on test execution order
        System.out.println("✓ Test 2: Counter = " + statefulService.getCounter());
        System.out.println("  Context will be reloaded after this test");
    }
    
    @Test
    void testAfterDirtyContext() {
        // If previous test with @DirtiesContext ran, counter should be 1
        statefulService.incrementCounter();
        System.out.println("✓ Test 3: Counter = " + statefulService.getCounter());
        System.out.println("  Context was reloaded if previous test ran");
    }
}

/**
 * Example 2: Class-Level @DirtiesContext
 * Demonstrates marking entire test class as dirty
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DirtyContextConfig.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class ClassLevelDirtiesContextTest {
    
    @Autowired
    private StatefulService statefulService;
    
    @Autowired
    private CounterService counterService;
    
    @Test
    void testOne() {
        statefulService.incrementCounter();
        counterService.increment();
        
        assertEquals(1, statefulService.getCounter());
        assertEquals(1, counterService.getCount());
        
        System.out.println("✓ Test 1 - Counters incremented");
    }
    
    @Test
    void testTwo() {
        statefulService.incrementCounter();
        counterService.increment();
        
        // Values depend on test execution order
        System.out.println("✓ Test 2 - State Counter: " + statefulService.getCounter());
        System.out.println("  Counter Service: " + counterService.getCount());
    }
    
    @org.junit.jupiter.api.AfterAll
    static void afterAll() {
        System.out.println("✓ All tests completed - context will be reloaded");
    }
}

/**
 * Example 3: BEFORE_EACH_TEST_METHOD Mode
 * Demonstrates reloading context before each test
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DirtyContextConfig.class)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class BeforeEachTestMethodTest {
    
    @Autowired
    private StatefulService statefulService;
    
    @Test
    void testFirstMethod() {
        assertEquals(0, statefulService.getCounter(), "Counter should be 0 (fresh context)");
        statefulService.incrementCounter();
        assertEquals(1, statefulService.getCounter());
        
        System.out.println("✓ First test - Fresh context");
    }
    
    @Test
    void testSecondMethod() {
        assertEquals(0, statefulService.getCounter(), "Counter should be 0 (fresh context)");
        statefulService.incrementCounter();
        assertEquals(1, statefulService.getCounter());
        
        System.out.println("✓ Second test - Fresh context");
    }
    
    @Test
    void testThirdMethod() {
        assertEquals(0, statefulService.getCounter(), "Counter should be 0 (fresh context)");
        
        System.out.println("✓ Third test - Fresh context");
    }
}

/**
 * Example 4: AFTER_EACH_TEST_METHOD Mode
 * Demonstrates reloading context after each test
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DirtyContextConfig.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class AfterEachTestMethodTest {
    
    @Autowired
    private CacheService cacheService;
    
    @Test
    void testCacheOperation1() {
        cacheService.put("key1", "value1");
        assertEquals(1, cacheService.size());
        assertEquals("value1", cacheService.get("key1"));
        
        System.out.println("✓ Cache Test 1 - Cache size: " + cacheService.size());
        System.out.println("  Context will reload after this test");
    }
    
    @Test
    void testCacheOperation2() {
        // Cache should be empty due to context reload
        assertEquals(0, cacheService.size(), "Cache should be empty (fresh context)");
        cacheService.put("key2", "value2");
        assertEquals(1, cacheService.size());
        
        System.out.println("✓ Cache Test 2 - Fresh cache");
    }
}

/**
 * Example 5: Selective Dirty Methods
 * Demonstrates using @DirtiesContext only where needed
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DirtyContextConfig.class)
class SelectiveDirtyMethodsTest {
    
    @Autowired
    private StatefulService statefulService;
    
    @Test
    void readOnlyTest1() {
        // Read-only test - no context dirtying needed
        int counter = statefulService.getCounter();
        System.out.println("✓ Read-only test 1 - Counter: " + counter);
    }
    
    @Test
    void readOnlyTest2() {
        // Another read-only test
        String value = statefulService.getValue();
        System.out.println("✓ Read-only test 2 - Value: " + value);
    }
    
    @Test
    @DirtiesContext
    void modifyingTest() {
        // This test modifies state - mark as dirty
        statefulService.setValue("modified");
        statefulService.incrementCounter();
        
        System.out.println("✓ Modifying test - State changed");
        System.out.println("  Context will reload after this test");
    }
    
    @Test
    void subsequentTest() {
        // This test runs with fresh context if modifyingTest ran before it
        System.out.println("✓ Subsequent test");
        System.out.println("  Counter: " + statefulService.getCounter());
    }
}

/**
 * Example 6: Hierarchy Mode
 * Demonstrates hierarchical context dirtying
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DirtyContextConfig.class)
class HierarchyModeTest {
    
    @Autowired
    private StatefulService statefulService;
    
    @Test
    @DirtiesContext(hierarchyMode = DirtiesContext.HierarchyMode.EXHAUSTIVE)
    void testExhaustiveMode() {
        statefulService.incrementCounter();
        System.out.println("✓ Exhaustive mode - Dirties current and parent contexts");
    }
    
    @Test
    @DirtiesContext(hierarchyMode = DirtiesContext.HierarchyMode.CURRENT_LEVEL)
    void testCurrentLevelMode() {
        statefulService.incrementCounter();
        System.out.println("✓ Current level mode - Dirties only current context");
    }
}

/**
 * Example 7: Performance Comparison
 * Demonstrates performance impact of @DirtiesContext
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DirtyContextConfig.class)
class PerformanceComparisonTest {
    
    @Autowired
    private StatefulService statefulService;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Manual cleanup alternative to @DirtiesContext
        statefulService.reset();
    }
    
    @Test
    void testWithManualCleanup1() {
        statefulService.incrementCounter();
        assertEquals(1, statefulService.getCounter());
        System.out.println("✓ Test with manual cleanup (faster)");
    }
    
    @Test
    void testWithManualCleanup2() {
        statefulService.incrementCounter();
        assertEquals(1, statefulService.getCounter());
        System.out.println("✓ Another test with manual cleanup");
    }
}

/**
 * Example 8: Documentation Best Practice
 * Demonstrates documenting why context is marked dirty
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DirtyContextConfig.class)
class DocumentedDirtyContextTest {
    
    @Autowired
    private CacheService cacheService;
    
    /**
     * This test modifies the cache service state which is a singleton bean.
     * Context is marked dirty to ensure subsequent tests get a fresh cache.
     * Alternative: Implement cache.clear() in @AfterEach
     */
    @Test
    @DirtiesContext
    void testThatRequiresDirtyContext() {
        cacheService.put("persistent-key", "persistent-value");
        
        System.out.println("✓ Test with documented dirty context reason");
        System.out.println("  Reason: Singleton cache modified");
        System.out.println("  Alternative: Manual cache.clear()");
    }
}

/**
 * Main class for demonstration
 */
public class DirtyContextPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Dirty Context Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Method-level @DirtiesContext");
        System.out.println("2. Class-level @DirtiesContext");
        System.out.println("3. BEFORE_EACH_TEST_METHOD mode");
        System.out.println("4. AFTER_EACH_TEST_METHOD mode");
        System.out.println("5. Selective dirty context marking");
        System.out.println("6. Hierarchy modes (EXHAUSTIVE/CURRENT_LEVEL)");
        System.out.println("7. Performance considerations");
        System.out.println("8. Documentation best practices");
        System.out.println("\nIMPORTANT: Use @DirtiesContext sparingly due to performance impact!");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("===============================");
    }
}

/**
 * Summary of Modes:
 * 
 * Class-Level Modes:
 * ------------------
 * @DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
 *   - Reload context before any test in class runs
 * 
 * @DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
 *   - Reload context before each test method
 *   - Ensures complete isolation
 *   - Significant performance impact
 * 
 * @DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
 *   - Reload context after each test method
 *   - Next test gets fresh context
 *   - High performance cost
 * 
 * @DirtiesContext(classMode = ClassMode.AFTER_CLASS)
 *   - Reload context after all tests complete (default)
 *   - Minimal performance impact
 *   - Most common usage
 * 
 * Method-Level Modes:
 * -------------------
 * @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
 *   - Reload context before this specific method
 * 
 * @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
 *   - Reload context after this specific method (default)
 * 
 * Hierarchy Modes:
 * ----------------
 * hierarchyMode = HierarchyMode.EXHAUSTIVE (default)
 *   - Dirties current context and all parent contexts
 * 
 * hierarchyMode = HierarchyMode.CURRENT_LEVEL
 *   - Dirties only the current context level
 */
