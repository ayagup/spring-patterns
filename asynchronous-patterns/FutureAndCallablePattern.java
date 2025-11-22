package com.example.async.future;

import java.util.*;
import java.util.concurrent.*;

/**
 * Future and Callable Pattern Implementation
 * 
 * Purpose: Execute tasks asynchronously and retrieve results later
 * 
 * Key Components:
 * 1. Callable<T> - Task that returns a result
 * 2. Future<T> - Handle to get result
 * 3. ExecutorService - Manages thread pool
 * 4. FutureTask - Runnable Future implementation
 * 
 * Features:
 * - Asynchronous execution
 * - Result retrieval with get()
 * - Task cancellation
 * - Timeout support
 * - Exception handling
 */

// Task Models
class DataProcessingTask implements Callable<ProcessingResult> {
    private final String taskId;
    private final List<Integer> data;
    
    public DataProcessingTask(String taskId, List<Integer> data) {
        this.taskId = taskId;
        this.data = data;
    }
    
    @Override
    public ProcessingResult call() throws Exception {
        System.out.println("  [" + taskId + "] Starting processing on thread: " + 
            Thread.currentThread().getName());
        
        // Simulate complex processing
        Thread.sleep(2000);
        
        int sum = data.stream().mapToInt(Integer::intValue).sum();
        double average = data.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        
        ProcessingResult result = new ProcessingResult(taskId, sum, average, data.size());
        
        System.out.println("  [" + taskId + "] Completed processing");
        return result;
    }
}

class ProcessingResult {
    private final String taskId;
    private final int sum;
    private final double average;
    private final int count;
    
    public ProcessingResult(String taskId, int sum, double average, int count) {
        this.taskId = taskId;
        this.sum = sum;
        this.average = average;
        this.count = count;
    }
    
    public String getTaskId() { return taskId; }
    public int getSum() { return sum; }
    public double getAverage() { return average; }
    public int getCount() { return count; }
    
    @Override
    public String toString() {
        return String.format("Result[%s: sum=%d, avg=%.2f, count=%d]", 
            taskId, sum, average, count);
    }
}

// Service for async operations
class AsyncProcessingService {
    private final ExecutorService executorService;
    
    public AsyncProcessingService(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }
    
    /**
     * Submit task and return Future
     */
    public Future<ProcessingResult> processAsync(String taskId, List<Integer> data) {
        DataProcessingTask task = new DataProcessingTask(taskId, data);
        return executorService.submit(task);
    }
    
    /**
     * Process multiple tasks in parallel
     */
    public List<Future<ProcessingResult>> processMultiple(Map<String, List<Integer>> tasks) {
        List<Future<ProcessingResult>> futures = new ArrayList<>();
        
        for (Map.Entry<String, List<Integer>> entry : tasks.entrySet()) {
            Future<ProcessingResult> future = processAsync(entry.getKey(), entry.getValue());
            futures.add(future);
        }
        
        return futures;
    }
    
    /**
     * Process with timeout
     */
    public ProcessingResult processWithTimeout(String taskId, List<Integer> data, 
            long timeout, TimeUnit unit) throws Exception {
        Future<ProcessingResult> future = processAsync(taskId, data);
        
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new Exception("Task " + taskId + " timed out after " + timeout + " " + unit);
        }
    }
    
    /**
     * Wait for all tasks to complete
     */
    public List<ProcessingResult> waitForAll(List<Future<ProcessingResult>> futures) 
            throws Exception {
        List<ProcessingResult> results = new ArrayList<>();
        
        for (Future<ProcessingResult> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                System.err.println("Task failed: " + e.getCause().getMessage());
            }
        }
        
        return results;
    }
    
    /**
     * Get first completed result
     */
    public ProcessingResult getFirstCompleted(List<Future<ProcessingResult>> futures) 
            throws Exception {
        while (true) {
            for (Future<ProcessingResult> future : futures) {
                if (future.isDone() && !future.isCancelled()) {
                    try {
                        return future.get();
                    } catch (ExecutionException e) {
                        // Continue to next future
                    }
                }
            }
            Thread.sleep(100);
        }
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}

// Cancellable Task Example
class LongRunningTask implements Callable<String> {
    private final String taskName;
    
    public LongRunningTask(String taskName) {
        this.taskName = taskName;
    }
    
    @Override
    public String call() throws Exception {
        System.out.println("  [" + taskName + "] Starting long running task");
        
        for (int i = 0; i < 10; i++) {
            if (Thread.interrupted()) {
                System.out.println("  [" + taskName + "] Task was cancelled!");
                throw new InterruptedException("Task cancelled");
            }
            
            System.out.println("  [" + taskName + "] Progress: " + (i + 1) * 10 + "%");
            Thread.sleep(1000);
        }
        
        return taskName + " completed successfully";
    }
}

// FutureTask Example
class CustomFutureTask {
    
    public static void runWithFutureTask() throws Exception {
        System.out.println("\n=== FutureTask Example ===");
        
        Callable<String> callable = () -> {
            System.out.println("  FutureTask executing on: " + Thread.currentThread().getName());
            Thread.sleep(1500);
            return "FutureTask Result";
        };
        
        FutureTask<String> futureTask = new FutureTask<>(callable);
        
        // Run in separate thread
        Thread thread = new Thread(futureTask);
        thread.start();
        
        System.out.println("  Main thread continues...");
        
        // Get result (blocks until complete)
        String result = futureTask.get();
        System.out.println("  Result: " + result);
    }
}

/**
 * Demonstration of Future and Callable Patterns
 */
public class FutureAndCallablePattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Future and Callable Pattern Demo ===\n");
        
        AsyncProcessingService service = new AsyncProcessingService(3);
        
        // 1. Simple Future example
        System.out.println("1. Simple Async Processing:");
        List<Integer> data1 = Arrays.asList(1, 2, 3, 4, 5);
        Future<ProcessingResult> future1 = service.processAsync("Task-1", data1);
        
        System.out.println("  Task submitted, continuing with other work...");
        System.out.println("  Is done? " + future1.isDone());
        
        // Do other work
        Thread.sleep(500);
        System.out.println("  Still processing...");
        
        // Get result (blocks if not ready)
        ProcessingResult result1 = future1.get();
        System.out.println("  " + result1);
        
        // 2. Multiple parallel tasks
        System.out.println("\n2. Multiple Parallel Tasks:");
        Map<String, List<Integer>> tasks = new LinkedHashMap<>();
        tasks.put("Task-A", Arrays.asList(1, 2, 3));
        tasks.put("Task-B", Arrays.asList(4, 5, 6));
        tasks.put("Task-C", Arrays.asList(7, 8, 9));
        
        List<Future<ProcessingResult>> futures = service.processMultiple(tasks);
        System.out.println("  Submitted " + futures.size() + " tasks");
        
        List<ProcessingResult> results = service.waitForAll(futures);
        System.out.println("  All tasks completed:");
        results.forEach(r -> System.out.println("    " + r));
        
        // 3. Timeout handling
        System.out.println("\n3. Timeout Handling:");
        try {
            ProcessingResult result = service.processWithTimeout(
                "Timeout-Task", Arrays.asList(1, 2, 3, 4, 5), 1, TimeUnit.SECONDS);
            System.out.println("  " + result);
        } catch (Exception e) {
            System.out.println("  " + e.getMessage());
        }
        
        // 4. Task cancellation
        System.out.println("\n4. Task Cancellation:");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> longFuture = executor.submit(new LongRunningTask("Cancel-Task"));
        
        Thread.sleep(3000);
        
        if (!longFuture.isDone()) {
            System.out.println("  Cancelling task...");
            boolean cancelled = longFuture.cancel(true);
            System.out.println("  Cancelled: " + cancelled);
            System.out.println("  Is cancelled: " + longFuture.isCancelled());
        }
        
        executor.shutdown();
        
        // 5. FutureTask example
        CustomFutureTask.runWithFutureTask();
        
        // 6. Check Future status
        System.out.println("\n6. Future Status Checks:");
        Future<ProcessingResult> statusFuture = service.processAsync(
            "Status-Task", Arrays.asList(10, 20, 30));
        
        System.out.println("  Is done: " + statusFuture.isDone());
        System.out.println("  Is cancelled: " + statusFuture.isCancelled());
        
        ProcessingResult statusResult = statusFuture.get();
        System.out.println("  Is done: " + statusFuture.isDone());
        System.out.println("  " + statusResult);
        
        System.out.println("\n=== Future vs Callable ===");
        System.out.println("Callable<T>:");
        System.out.println("  - Interface with call() method");
        System.out.println("  - Returns a result of type T");
        System.out.println("  - Can throw checked exceptions");
        System.out.println("  - Designed for tasks that compute a value");
        
        System.out.println("\nFuture<T>:");
        System.out.println("  - Interface representing async computation result");
        System.out.println("  - get() - blocks until result is available");
        System.out.println("  - get(timeout, unit) - blocks with timeout");
        System.out.println("  - cancel(mayInterrupt) - attempts to cancel");
        System.out.println("  - isDone() - checks if completed");
        System.out.println("  - isCancelled() - checks if cancelled");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("✓ Long-running computations");
        System.out.println("✓ I/O operations (database, network)");
        System.out.println("✓ Parallel processing of independent tasks");
        System.out.println("✓ Background jobs");
        System.out.println("✓ Resource-intensive calculations");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Always handle ExecutionException");
        System.out.println("✓ Use timeout to prevent indefinite blocking");
        System.out.println("✓ Cancel tasks when no longer needed");
        System.out.println("✓ Shutdown executor service properly");
        System.out.println("✓ Consider CompletableFuture for more features");
        System.out.println("✓ Use appropriate thread pool size");
        
        System.out.println("\n=== Limitations ===");
        System.out.println("✗ Cannot chain multiple async operations easily");
        System.out.println("✗ No built-in support for combining futures");
        System.out.println("✗ Blocking get() can lead to performance issues");
        System.out.println("✗ Limited error handling capabilities");
        System.out.println("✗ Cannot attach callbacks");
        
        service.shutdown();
    }
}
