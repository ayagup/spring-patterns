package com.example.async.annotation;

import java.util.*;
import java.util.concurrent.*;

/**
 * Async Method and @Async Annotation Pattern
 * 
 * Purpose: Enable asynchronous method execution with Spring
 * 
 * Key Components:
 * 1. @Async annotation - Mark methods for async execution
 * 2. @EnableAsync - Enable async processing
 * 3. AsyncConfigurer - Configure async behavior
 * 4. ThreadPoolTaskExecutor - Thread pool management
 * 5. AsyncUncaughtExceptionHandler - Global exception handling
 * 
 * Return Types:
 * - void - Fire and forget
 * - Future<T> - Basic async result
 * - CompletableFuture<T> - Enhanced async result
 * - ListenableFuture<T> - Spring async result
 * 
 * Features:
 * - Declarative async execution
 * - Custom thread pools
 * - Exception handling
 * - Method interception
 */

// Simulated Spring annotations
@interface Async {
    String value() default "";
}

@interface EnableAsync {
    int mode() default 0;
}

@interface Service {}

@interface Configuration {}

@interface Bean {}

// Configuration interface
interface AsyncConfigurer {
    Executor getAsyncExecutor();
    AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler();
}

interface AsyncUncaughtExceptionHandler {
    void handleUncaughtException(Throwable ex, String methodName, Object... params);
}

// Thread Pool Configuration
@Configuration
class AsyncConfiguration implements AsyncConfigurer {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
}

class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable ex, String methodName, Object... params) {
        System.err.println("Async method '" + methodName + "' threw exception: " + ex.getMessage());
        System.err.println("Method parameters: " + Arrays.toString(params));
    }
}

// Custom Thread Pool Task Executor
class ThreadPoolTaskExecutor implements Executor {
    private ExecutorService executorService;
    private int corePoolSize = 5;
    private int maxPoolSize = 10;
    private int queueCapacity = 25;
    private String threadNamePrefix = "Task-";
    private boolean waitForTasksToCompleteOnShutdown = false;
    private int awaitTerminationSeconds = 0;
    
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
    
    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }
    
    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }
    
    public void setWaitForTasksToCompleteOnShutdown(boolean wait) {
        this.waitForTasksToCompleteOnShutdown = wait;
    }
    
    public void setAwaitTerminationSeconds(int seconds) {
        this.awaitTerminationSeconds = seconds;
    }
    
    public void initialize() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int counter = 0;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, threadNamePrefix + (counter++));
            }
        };
        
        executorService = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueCapacity),
            threadFactory,
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    @Override
    public void execute(Runnable command) {
        executorService.execute(command);
    }
    
    public <T> Future<T> submit(Callable<T> task) {
        return ((ExecutorService) executorService).submit(task);
    }
    
    public void shutdown() {
        if (waitForTasksToCompleteOnShutdown) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(awaitTerminationSeconds, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        } else {
            executorService.shutdownNow();
        }
    }
    
    public int getActiveCount() {
        return ((ThreadPoolExecutor) executorService).getActiveCount();
    }
    
    public int getPoolSize() {
        return ((ThreadPoolExecutor) executorService).getPoolSize();
    }
}

// Service with @Async methods
@Service
class EmailService {
    
    /**
     * Fire and forget - void return type
     */
    @Async
    public void sendEmailAsync(String to, String subject, String body) {
        System.out.println("  [EmailService] Sending email to " + to + 
            " on thread: " + Thread.currentThread().getName());
        
        try {
            Thread.sleep(2000);
            System.out.println("  [EmailService] Email sent successfully to " + to);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * With Future return type
     */
    @Async
    public Future<String> sendEmailWithResultAsync(String to) {
        System.out.println("  [EmailService] Sending email (Future) to " + to +
            " on thread: " + Thread.currentThread().getName());
        
        try {
            Thread.sleep(1500);
            String result = "Email sent successfully to " + to;
            return new AsyncResult<>(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new AsyncResult<>("Email sending interrupted");
        }
    }
    
    /**
     * With CompletableFuture return type
     */
    @Async
    public CompletableFuture<String> sendEmailCompletableAsync(String to) {
        System.out.println("  [EmailService] Sending email (CompletableFuture) to " + to +
            " on thread: " + Thread.currentThread().getName());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                return "Email sent to " + to;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Email sending failed";
            }
        });
    }
}

// Async Result implementation
class AsyncResult<T> implements Future<T> {
    private final T result;
    
    public AsyncResult(T result) {
        this.result = result;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }
    
    @Override
    public boolean isCancelled() {
        return false;
    }
    
    @Override
    public boolean isDone() {
        return true;
    }
    
    @Override
    public T get() {
        return result;
    }
    
    @Override
    public T get(long timeout, TimeUnit unit) {
        return result;
    }
}

// Data processing service
@Service
class DataProcessingService {
    
    @Async
    public CompletableFuture<List<Integer>> processDataAsync(List<Integer> data) {
        System.out.println("  [DataProcessingService] Processing data on thread: " + 
            Thread.currentThread().getName());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
                
                // Transform data
                List<Integer> processed = new ArrayList<>();
                for (Integer num : data) {
                    processed.add(num * 2);
                }
                
                System.out.println("  [DataProcessingService] Processing completed");
                return processed;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            }
        });
    }
    
    @Async
    public void processInBatches(List<String> items) {
        System.out.println("  [DataProcessingService] Batch processing " + items.size() +
            " items on thread: " + Thread.currentThread().getName());
        
        int batchSize = 10;
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            List<String> batch = items.subList(i, end);
            
            System.out.println("  [DataProcessingService] Processing batch: " + 
                (i/batchSize + 1));
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("  [DataProcessingService] All batches processed");
    }
}

// Report generation service
@Service
class ReportService {
    
    @Async("reportExecutor")
    public CompletableFuture<Report> generateReportAsync(String reportType) {
        System.out.println("  [ReportService] Generating " + reportType + 
            " report on thread: " + Thread.currentThread().getName());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
                
                Report report = new Report(
                    reportType,
                    "Report-" + System.currentTimeMillis(),
                    1000
                );
                
                System.out.println("  [ReportService] Report generated: " + report.getId());
                return report;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        });
    }
}

class Report {
    private final String type;
    private final String id;
    private final int records;
    
    public Report(String type, String id, int records) {
        this.type = type;
        this.id = id;
        this.records = records;
    }
    
    public String getType() { return type; }
    public String getId() { return id; }
    public int getRecords() { return records; }
    
    @Override
    public String toString() {
        return String.format("Report[type=%s, id=%s, records=%d]", type, id, records);
    }
}

// Exception handling examples
@Service
class RiskyService {
    
    @Async
    public void methodThatThrows() {
        System.out.println("  [RiskyService] Method executing on thread: " + 
            Thread.currentThread().getName());
        
        throw new RuntimeException("Something went wrong!");
    }
    
    @Async
    public CompletableFuture<String> methodWithErrorHandling() {
        System.out.println("  [RiskyService] Error handling method on thread: " + 
            Thread.currentThread().getName());
        
        return CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Deliberate error");
        }).exceptionally(ex -> {
            System.err.println("  [RiskyService] Error handled: " + ex.getMessage());
            return "Fallback result";
        });
    }
}

// Orchestration service
class AsyncOrchestrationService {
    private final EmailService emailService;
    private final DataProcessingService dataService;
    private final ReportService reportService;
    
    public AsyncOrchestrationService() {
        this.emailService = new EmailService();
        this.dataService = new DataProcessingService();
        this.reportService = new ReportService();
    }
    
    /**
     * Orchestrate multiple async operations
     */
    public CompletableFuture<String> orchestrateOperations() {
        System.out.println("  [Orchestration] Starting orchestration");
        
        CompletableFuture<String> emailFuture = emailService.sendEmailCompletableAsync("user@example.com");
        CompletableFuture<List<Integer>> dataFuture = dataService.processDataAsync(Arrays.asList(1, 2, 3, 4, 5));
        CompletableFuture<Report> reportFuture = reportService.generateReportAsync("Sales");
        
        return CompletableFuture.allOf(emailFuture, dataFuture, reportFuture)
            .thenApply(v -> {
                System.out.println("  [Orchestration] All operations completed");
                return "Orchestration completed successfully";
            });
    }
}

/**
 * Demonstration of Async Method and @Async Annotation Pattern
 */
public class AsyncMethodAndAnnotationPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Async Method and @Async Annotation Pattern Demo ===\n");
        
        EmailService emailService = new EmailService();
        DataProcessingService dataService = new DataProcessingService();
        ReportService reportService = new ReportService();
        RiskyService riskyService = new RiskyService();
        
        // 1. Fire and forget (void)
        System.out.println("1. Fire and Forget (void):");
        emailService.sendEmailAsync("john@example.com", "Welcome", "Welcome to our service!");
        System.out.println("  Main thread continues immediately");
        Thread.sleep(2500);
        
        // 2. With Future return type
        System.out.println("\n2. Async with Future:");
        Future<String> emailFuture = emailService.sendEmailWithResultAsync("jane@example.com");
        System.out.println("  Email sending initiated...");
        String emailResult = emailFuture.get();
        System.out.println("  Result: " + emailResult);
        
        // 3. With CompletableFuture
        System.out.println("\n3. Async with CompletableFuture:");
        CompletableFuture<String> completableFuture = 
            emailService.sendEmailCompletableAsync("bob@example.com");
        
        completableFuture.thenAccept(result -> {
            System.out.println("  Callback result: " + result);
        });
        
        Thread.sleep(1500);
        
        // 4. Data processing
        System.out.println("\n4. Async Data Processing:");
        CompletableFuture<List<Integer>> dataFuture = 
            dataService.processDataAsync(Arrays.asList(1, 2, 3, 4, 5));
        
        dataFuture.thenAccept(processed -> {
            System.out.println("  Processed data: " + processed);
        });
        
        Thread.sleep(2500);
        
        // 5. Batch processing
        System.out.println("\n5. Batch Processing:");
        List<String> items = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            items.add("Item-" + i);
        }
        dataService.processInBatches(items);
        Thread.sleep(3000);
        
        // 6. Report generation
        System.out.println("\n6. Report Generation:");
        CompletableFuture<Report> reportFuture = reportService.generateReportAsync("Monthly Sales");
        
        reportFuture.thenAccept(report -> {
            if (report != null) {
                System.out.println("  " + report);
            }
        });
        
        Thread.sleep(3500);
        
        // 7. Error handling
        System.out.println("\n7. Error Handling:");
        
        System.out.println("  Method with exception (void):");
        riskyService.methodThatThrows();
        Thread.sleep(500);
        
        System.out.println("\n  Method with error handling:");
        CompletableFuture<String> errorHandledFuture = riskyService.methodWithErrorHandling();
        String errorResult = errorHandledFuture.join();
        System.out.println("  Result after error: " + errorResult);
        
        // 8. Orchestration
        System.out.println("\n8. Orchestrating Multiple Async Operations:");
        AsyncOrchestrationService orchestration = new AsyncOrchestrationService();
        CompletableFuture<String> orchestrationResult = orchestration.orchestrateOperations();
        
        String result = orchestrationResult.join();
        System.out.println("  " + result);
        
        System.out.println("\n=== @Async Configuration ===");
        System.out.println("Enable:");
        System.out.println("  @EnableAsync - Enable async processing");
        System.out.println("  @Configuration class implementing AsyncConfigurer");
        
        System.out.println("\nThread Pool:");
        System.out.println("  corePoolSize - minimum threads");
        System.out.println("  maxPoolSize - maximum threads");
        System.out.println("  queueCapacity - queue size before creating new threads");
        System.out.println("  threadNamePrefix - thread naming pattern");
        System.out.println("  keepAliveSeconds - idle thread timeout");
        
        System.out.println("\nReturn Types:");
        System.out.println("  void - fire and forget");
        System.out.println("  Future<T> - basic async result");
        System.out.println("  CompletableFuture<T> - enhanced async with composition");
        System.out.println("  ListenableFuture<T> - Spring async with callbacks");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Configure appropriate thread pool size");
        System.out.println("✓ Use meaningful thread name prefixes");
        System.out.println("✓ Set queue capacity to prevent memory issues");
        System.out.println("✓ Implement AsyncUncaughtExceptionHandler");
        System.out.println("✓ Use CompletableFuture for complex chains");
        System.out.println("✓ Don't call @Async methods from same class (self-invocation)");
        System.out.println("✓ Monitor thread pool metrics");
        System.out.println("✓ Set graceful shutdown with awaitTermination");
        
        System.out.println("\n=== Thread Pool Sizing ===");
        System.out.println("CPU-intensive tasks:");
        System.out.println("  corePoolSize = CPU cores + 1");
        
        System.out.println("\nI/O-intensive tasks:");
        System.out.println("  corePoolSize = CPU cores * 2");
        System.out.println("  or: CPU cores / (1 - blocking coefficient)");
        
        System.out.println("\nMixed workload:");
        System.out.println("  Separate executors for CPU and I/O tasks");
        System.out.println("  Use @Async(\"executorName\") to specify executor");
        
        System.out.println("\n=== Common Pitfalls ===");
        System.out.println("✗ Self-invocation doesn't work (proxy limitation)");
        System.out.println("✗ Forgetting @EnableAsync configuration");
        System.out.println("✗ Not handling exceptions in void methods");
        System.out.println("✗ Using default thread pool for all tasks");
        System.out.println("✗ Blocking operations in async methods");
        System.out.println("✗ Not setting proper thread pool limits");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("✓ Email/SMS notifications");
        System.out.println("✓ Report generation");
        System.out.println("✓ Batch processing");
        System.out.println("✓ File upload/download");
        System.out.println("✓ External API calls");
        System.out.println("✓ Background data synchronization");
        System.out.println("✓ Audit logging");
        
        System.out.println("\n=== Exception Handling ===");
        System.out.println("void methods:");
        System.out.println("  - Implement AsyncUncaughtExceptionHandler");
        System.out.println("  - Global exception handling");
        
        System.out.println("\nFuture/CompletableFuture:");
        System.out.println("  - Exception wrapped in ExecutionException");
        System.out.println("  - Use exceptionally/handle for recovery");
        System.out.println("  - Caller must handle with try-catch");
        
        Thread.sleep(1000);
    }
}
