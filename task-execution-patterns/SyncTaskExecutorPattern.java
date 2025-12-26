package com.example.taskexecution;

import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sync Task Executor Pattern - Synchronous Task Execution
 * 
 * SyncTaskExecutor is a TaskExecutor implementation that executes tasks
 * synchronously in the caller's thread. It doesn't create any new threads
 * and provides synchronous execution semantics.
 * 
 * Key Characteristics:
 * 
 * 1. Synchronous Execution:
 *    - Tasks execute in caller's thread
 *    - Blocking execution
 *    - No parallelism
 *    - No async benefits
 * 
 * 2. No Thread Creation:
 *    - Zero thread overhead
 *    - No thread management
 *    - Simplest possible executor
 * 
 * 3. Immediate Execution:
 *    - Task runs immediately when submitted
 *    - execute() blocks until completion
 *    - No queueing or scheduling
 * 
 * Execution Flow:
 * 1. Task submitted via execute()
 * 2. Task runs immediately in caller's thread
 * 3. Method returns after task completes
 * 4. Next line executes after task done
 * 
 * Comparison with Other Executors:
 * 
 * SyncTaskExecutor:
 * - Synchronous execution
 * - Caller's thread
 * - No parallelism
 * - Testing/development
 * 
 * SimpleAsyncTaskExecutor:
 * - Creates new thread per task
 * - Asynchronous execution
 * - No thread reuse
 * 
 * ThreadPoolTaskExecutor:
 * - Thread pool
 * - Asynchronous execution
 * - Thread reuse
 * - Production ready
 * 
 * Use Cases:
 * 
 * 1. Testing:
 *    - Simplify async code testing
 *    - Deterministic execution order
 *    - No thread synchronization needed
 * 
 * 2. Development:
 *    - Quick prototyping
 *    - Debugging async code
 *    - Step-through debugging
 * 
 * 3. Conditional Execution:
 *    - Switch between sync/async based on config
 *    - Feature flags
 *    - Environment-specific behavior
 * 
 * 4. Interface Compatibility:
 *    - Satisfy TaskExecutor interface
 *    - Without actual async execution
 *    - Adapter pattern usage
 * 
 * Advantages:
 * + Extremely simple
 * + No thread overhead
 * + Deterministic execution
 * + Easy debugging
 * + No concurrency issues
 * + Predictable behavior
 * 
 * Disadvantages:
 * - No parallelism
 * - Blocks caller
 * - No performance benefit
 * - Not suitable for I/O operations
 * - Defeats purpose of async
 * 
 * When to Use:
 * ✓ Unit testing async code
 * ✓ Development/debugging
 * ✓ Conditional sync mode
 * ✓ Interface compliance
 * 
 * When NOT to Use:
 * ✗ Production async execution
 * ✗ I/O-bound operations
 * ✗ Long-running tasks
 * ✗ Parallelism required
 * 
 * Testing Benefits:
 * - No thread timing issues
 * - Deterministic test execution
 * - Easier to verify results
 * - No need for CountDownLatch/await
 * - Simpler test code
 * 
 * Best Practices:
 * - Use only for testing/development
 * - Don't use in production for async tasks
 * - Good for debugging async logic
 * - Switch to async executor for production
 * - Document sync behavior clearly
 */
public class SyncTaskExecutorPattern {

    /**
     * Basic SyncTaskExecutor configuration
     */
    @Configuration
    static class SyncExecutorConfiguration {
        
        @Bean(name = "syncExecutor")
        public TaskExecutor syncTaskExecutor() {
            return new SyncTaskExecutor();
        }
    }

    /**
     * Conditional executor (sync for test, async for production)
     */
    @Configuration
    static class ConditionalExecutorConfiguration {
        
        @Bean(name = "conditionalExecutor")
        public TaskExecutor conditionalExecutor() {
            String environment = System.getProperty("env", "dev");
            
            if ("test".equals(environment) || "dev".equals(environment)) {
                System.out.println("Using SyncTaskExecutor for " + environment);
                return new SyncTaskExecutor();
            } else {
                System.out.println("Using async executor for " + environment);
                // In production, return ThreadPoolTaskExecutor
                return new SyncTaskExecutor(); // placeholder
            }
        }
    }

    /**
     * Service using TaskExecutor
     */
    static class EmailService {
        
        private TaskExecutor taskExecutor;
        
        public EmailService(TaskExecutor taskExecutor) {
            this.taskExecutor = taskExecutor;
        }
        
        public void sendEmail(String recipient, String message) {
            System.out.println("Submitting email task for: " + recipient);
            
            taskExecutor.execute(() -> {
                System.out.println("  Sending email to " + recipient + 
                                 " on thread: " + Thread.currentThread().getName());
                System.out.println("  Message: " + message);
                // Simulate email sending
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("  Email sent to " + recipient);
            });
            
            System.out.println("Email task submitted for: " + recipient);
        }
    }

    /**
     * Usage examples
     */
    static class SyncTaskExecutorExamples {
        
        public void demonstrateSyncExecution() {
            System.out.println("\n=== Synchronous Execution ===");
            
            SyncTaskExecutor executor = new SyncTaskExecutor();
            
            System.out.println("Before task 1 (Thread: " + Thread.currentThread().getName() + ")");
            
            executor.execute(() -> {
                System.out.println("  Task 1 executing (Thread: " + 
                                 Thread.currentThread().getName() + ")");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("  Task 1 completed");
            });
            
            System.out.println("After task 1 (task completed before this line)");
            
            System.out.println("\nBefore task 2");
            
            executor.execute(() -> {
                System.out.println("  Task 2 executing");
                System.out.println("  Task 2 completed");
            });
            
            System.out.println("After task 2");
            System.out.println("\nNotice: All tasks run in main thread synchronously!");
        }
        
        public void demonstrateExecutionOrder() {
            System.out.println("\n=== Deterministic Execution Order ===");
            
            SyncTaskExecutor executor = new SyncTaskExecutor();
            
            for (int i = 1; i <= 5; i++) {
                final int taskId = i;
                System.out.println("Submitting task " + taskId);
                
                executor.execute(() -> {
                    System.out.println("  Executing task " + taskId);
                });
                
                System.out.println("Task " + taskId + " completed\n");
            }
            
            System.out.println("Execution order is completely deterministic!");
        }
        
        public void demonstrateTestingScenario() {
            System.out.println("\n=== Testing Scenario ===");
            
            // Test with SyncTaskExecutor
            EmailService emailService = new EmailService(new SyncTaskExecutor());
            
            System.out.println("Test: Sending email");
            emailService.sendEmail("test@example.com", "Test message");
            System.out.println("Verification: Email was sent (completed before this line)");
            
            // No need for CountDownLatch or Thread.sleep in tests!
            System.out.println("Test passed!");
        }
        
        public void compareWithAsync() throws InterruptedException {
            System.out.println("\n=== Comparison: Sync vs Async ===");
            
            System.out.println("\n1. SyncTaskExecutor:");
            SyncTaskExecutor syncExecutor = new SyncTaskExecutor();
            
            long startSync = System.currentTimeMillis();
            for (int i = 1; i <= 3; i++) {
                final int taskId = i;
                System.out.println("  Submitting sync task " + taskId);
                syncExecutor.execute(() -> {
                    System.out.println("    Executing sync task " + taskId);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            long endSync = System.currentTimeMillis();
            System.out.println("  Sync execution time: " + (endSync - startSync) + "ms");
            
            System.out.println("\n2. SimpleAsyncTaskExecutor (simulated async):");
            System.out.println("  - Would run in parallel");
            System.out.println("  - Faster total time");
            System.out.println("  - But harder to test");
        }
        
        public void demonstrateDebugging() {
            System.out.println("\n=== Debugging Benefits ===");
            
            SyncTaskExecutor executor = new SyncTaskExecutor();
            
            System.out.println("Setting breakpoint in task...");
            executor.execute(() -> {
                int x = 10;
                int y = 20;
                int result = x + y; // Easy to step through
                System.out.println("  Result: " + result);
                System.out.println("  Easy to debug - no thread switching!");
            });
            
            System.out.println("Debugging completed in same thread");
        }
        
        public void demonstrateNoThreadOverhead() {
            System.out.println("\n=== No Thread Overhead ===");
            
            SyncTaskExecutor executor = new SyncTaskExecutor();
            
            long threadId = Thread.currentThread().getId();
            
            for (int i = 1; i <= 5; i++) {
                final int taskId = i;
                executor.execute(() -> {
                    long taskThreadId = Thread.currentThread().getId();
                    System.out.println("Task " + taskId + " - Thread ID: " + taskThreadId + 
                                     " (same as main: " + (taskThreadId == threadId) + ")");
                });
            }
            
            System.out.println("\nAll tasks ran in thread ID: " + threadId);
            System.out.println("Zero thread creation overhead!");
        }
    }

    /**
     * Test helper demonstrating testing benefits
     */
    static class EmailServiceTest {
        
        public void testEmailSendingWithSyncExecutor() {
            System.out.println("\n=== Unit Test Example ===");
            
            // Arrange
            SyncTaskExecutor syncExecutor = new SyncTaskExecutor();
            EmailService emailService = new EmailService(syncExecutor);
            
            // Act
            System.out.println("Test: Send email");
            emailService.sendEmail("user@test.com", "Hello");
            
            // Assert
            // With SyncTaskExecutor, email is guaranteed to be sent
            // No need for Thread.sleep() or CountDownLatch
            System.out.println("Assert: Email sent successfully");
            System.out.println("✓ Test passed (no async complexity)");
        }
        
        public void testMultipleEmails() {
            System.out.println("\n=== Multiple Operations Test ===");
            
            SyncTaskExecutor syncExecutor = new SyncTaskExecutor();
            EmailService emailService = new EmailService(syncExecutor);
            
            // Send multiple emails
            emailService.sendEmail("user1@test.com", "Message 1");
            emailService.sendEmail("user2@test.com", "Message 2");
            emailService.sendEmail("user3@test.com", "Message 3");
            
            // All emails guaranteed to be sent at this point
            System.out.println("✓ All emails sent (deterministic)");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Sync Task Executor Pattern - Synchronous Task Execution");
        System.out.println("========================================================");
        
        SyncTaskExecutorExamples examples = new SyncTaskExecutorExamples();
        EmailServiceTest test = new EmailServiceTest();
        
        examples.demonstrateSyncExecution();
        examples.demonstrateExecutionOrder();
        examples.demonstrateTestingScenario();
        examples.compareWithAsync();
        examples.demonstrateDebugging();
        examples.demonstrateNoThreadOverhead();
        
        test.testEmailSendingWithSyncExecutor();
        test.testMultipleEmails();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Characteristics:");
        System.out.println("- Executes tasks in caller's thread");
        System.out.println("- Synchronous/blocking execution");
        System.out.println("- No thread creation");
        System.out.println("- Deterministic execution order");
        
        System.out.println("\nAdvantages:");
        System.out.println("✓ Extremely simple");
        System.out.println("✓ No thread overhead");
        System.out.println("✓ Easy debugging");
        System.out.println("✓ Deterministic behavior");
        System.out.println("✓ Perfect for testing");
        
        System.out.println("\nDisadvantages:");
        System.out.println("✗ No parallelism");
        System.out.println("✗ Blocks caller");
        System.out.println("✗ No async benefits");
        
        System.out.println("\nPrimary Use Cases:");
        System.out.println("1. Unit testing async code");
        System.out.println("2. Development/debugging");
        System.out.println("3. Conditional sync mode");
        System.out.println("4. Interface compliance");
        
        System.out.println("\nDO NOT use for:");
        System.out.println("- Production async execution");
        System.out.println("- I/O-bound operations");
        System.out.println("- Long-running tasks");
    }
}
