package com.example.async.spring;

import java.util.concurrent.*;

/**
 * ListenableFuture Pattern Implementation
 * 
 * Purpose: Spring's enhanced Future with callback support
 * 
 * Key Components:
 * 1. ListenableFuture<T> - Future with callback support
 * 2. ListenableFutureCallback - Success/failure callbacks
 * 3. AsyncListenableTaskExecutor - Executor returning ListenableFuture
 * 4. AsyncResult - Spring's ListenableFuture implementation
 * 
 * Advantages:
 * - Non-blocking callbacks
 * - Better error handling than Future
 * - Spring integration
 * - Async method support
 */

// Spring-style interfaces (simulated)
interface ListenableFuture<T> extends Future<T> {
    void addCallback(ListenableFutureCallback<? super T> callback);
    void addCallback(SuccessCallback<? super T> successCallback, 
                    FailureCallback failureCallback);
}

interface ListenableFutureCallback<T> {
    void onSuccess(T result);
    void onFailure(Throwable ex);
}

interface SuccessCallback<T> {
    void onSuccess(T result);
}

interface FailureCallback {
    void onFailure(Throwable ex);
}

// Implementation
class AsyncResult<T> implements ListenableFuture<T> {
    private final FutureTask<T> futureTask;
    private final List<ListenableFutureCallback<? super T>> callbacks = new CopyOnWriteArrayList<>();
    private volatile T result;
    private volatile Throwable exception;
    private volatile boolean completed = false;
    
    public AsyncResult(Callable<T> callable) {
        this.futureTask = new FutureTask<T>(callable) {
            @Override
            protected void done() {
                try {
                    result = get();
                    completed = true;
                    notifySuccess(result);
                } catch (Exception e) {
                    exception = e.getCause() != null ? e.getCause() : e;
                    completed = true;
                    notifyFailure(exception);
                }
            }
        };
    }
    
    public void run() {
        futureTask.run();
    }
    
    @Override
    public void addCallback(ListenableFutureCallback<? super T> callback) {
        callbacks.add(callback);
        if (completed) {
            if (exception != null) {
                callback.onFailure(exception);
            } else {
                callback.onSuccess(result);
            }
        }
    }
    
    @Override
    public void addCallback(SuccessCallback<? super T> successCallback, 
                          FailureCallback failureCallback) {
        addCallback(new ListenableFutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                successCallback.onSuccess(result);
            }
            
            @Override
            public void onFailure(Throwable ex) {
                failureCallback.onFailure(ex);
            }
        });
    }
    
    private void notifySuccess(T result) {
        for (ListenableFutureCallback<? super T> callback : callbacks) {
            try {
                callback.onSuccess(result);
            } catch (Exception e) {
                System.err.println("Callback error: " + e.getMessage());
            }
        }
    }
    
    private void notifyFailure(Throwable ex) {
        for (ListenableFutureCallback<? super T> callback : callbacks) {
            try {
                callback.onFailure(ex);
            } catch (Exception e) {
                System.err.println("Callback error: " + e.getMessage());
            }
        }
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return futureTask.cancel(mayInterruptIfRunning);
    }
    
    @Override
    public boolean isCancelled() {
        return futureTask.isCancelled();
    }
    
    @Override
    public boolean isDone() {
        return futureTask.isDone();
    }
    
    @Override
    public T get() throws InterruptedException, ExecutionException {
        return futureTask.get();
    }
    
    @Override
    public T get(long timeout, TimeUnit unit) 
            throws InterruptedException, ExecutionException, TimeoutException {
        return futureTask.get(timeout, unit);
    }
}

// Task Executor
class AsyncListenableTaskExecutor {
    private final ExecutorService executorService;
    
    public AsyncListenableTaskExecutor(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }
    
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        AsyncResult<T> asyncResult = new AsyncResult<>(task);
        executorService.execute(asyncResult::run);
        return asyncResult;
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}

// Service Layer
class EmailService {
    
    public ListenableFuture<String> sendEmailAsync(String to, String subject) {
        AsyncListenableTaskExecutor executor = new AsyncListenableTaskExecutor(2);
        
        return executor.submitListenable(() -> {
            System.out.println("  [EmailService] Sending email to " + to + 
                " on thread: " + Thread.currentThread().getName());
            
            // Simulate sending email
            Thread.sleep(1500);
            
            if (to.contains("invalid")) {
                throw new RuntimeException("Invalid email address: " + to);
            }
            
            return "Email sent to " + to + " with subject: " + subject;
        });
    }
}

class PaymentService {
    
    public ListenableFuture<PaymentResult> processPaymentAsync(String orderId, double amount) {
        AsyncListenableTaskExecutor executor = new AsyncListenableTaskExecutor(2);
        
        return executor.submitListenable(() -> {
            System.out.println("  [PaymentService] Processing payment for order " + orderId +
                " on thread: " + Thread.currentThread().getName());
            
            // Simulate payment processing
            Thread.sleep(2000);
            
            if (amount > 10000) {
                throw new RuntimeException("Payment amount exceeds limit: $" + amount);
            }
            
            return new PaymentResult(orderId, amount, "SUCCESS", "TXN-" + System.currentTimeMillis());
        });
    }
}

class PaymentResult {
    private final String orderId;
    private final double amount;
    private final String status;
    private final String transactionId;
    
    public PaymentResult(String orderId, double amount, String status, String transactionId) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.transactionId = transactionId;
    }
    
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getTransactionId() { return transactionId; }
    
    @Override
    public String toString() {
        return String.format("Payment[order=%s, amount=$%.2f, status=%s, txn=%s]",
            orderId, amount, status, transactionId);
    }
}

// Orchestration with Callbacks
class OrderProcessor {
    private final EmailService emailService;
    private final PaymentService paymentService;
    
    public OrderProcessor() {
        this.emailService = new EmailService();
        this.paymentService = new PaymentService();
    }
    
    /**
     * Process order with callbacks
     */
    public void processOrderWithCallbacks(String orderId, double amount, String customerEmail) {
        System.out.println("  Processing order " + orderId + " for $" + amount);
        
        // Process payment
        ListenableFuture<PaymentResult> paymentFuture = 
            paymentService.processPaymentAsync(orderId, amount);
        
        paymentFuture.addCallback(new ListenableFutureCallback<PaymentResult>() {
            @Override
            public void onSuccess(PaymentResult result) {
                System.out.println("  Payment successful: " + result);
                
                // Send confirmation email
                ListenableFuture<String> emailFuture = 
                    emailService.sendEmailAsync(customerEmail, "Order Confirmation");
                
                emailFuture.addCallback(
                    emailResult -> System.out.println("  " + emailResult),
                    emailError -> System.err.println("  Email failed: " + emailError.getMessage())
                );
            }
            
            @Override
            public void onFailure(Throwable ex) {
                System.err.println("  Payment failed: " + ex.getMessage());
                
                // Send failure notification
                ListenableFuture<String> emailFuture = 
                    emailService.sendEmailAsync(customerEmail, "Payment Failed");
                
                emailFuture.addCallback(
                    emailResult -> System.out.println("  Failure notification sent"),
                    emailError -> System.err.println("  Notification failed: " + emailError.getMessage())
                );
            }
        });
    }
    
    /**
     * Chain multiple async operations
     */
    public ListenableFuture<String> chainOperations(String orderId, double amount) {
        AsyncListenableTaskExecutor executor = new AsyncListenableTaskExecutor(2);
        
        // This is a simplified version - real chaining would be more complex
        return executor.submitListenable(() -> {
            // First operation
            PaymentResult payment = paymentService.processPaymentAsync(orderId, amount).get();
            System.out.println("  Payment completed: " + payment);
            
            // Second operation
            String email = emailService.sendEmailAsync("customer@example.com", "Receipt").get();
            System.out.println("  Email sent: " + email);
            
            return "Order " + orderId + " processed successfully";
        });
    }
}

// Advanced Callback Patterns
class CallbackPatterns {
    
    /**
     * Multiple callbacks on same future
     */
    public static void multipleCallbacks() {
        System.out.println("\n=== Multiple Callbacks Example ===");
        
        AsyncListenableTaskExecutor executor = new AsyncListenableTaskExecutor(2);
        
        ListenableFuture<Integer> future = executor.submitListenable(() -> {
            Thread.sleep(1000);
            return 42;
        });
        
        // Add multiple callbacks
        future.addCallback(
            result -> System.out.println("  Callback 1: Result = " + result),
            error -> System.err.println("  Callback 1: Error")
        );
        
        future.addCallback(
            result -> System.out.println("  Callback 2: Result squared = " + (result * result)),
            error -> System.err.println("  Callback 2: Error")
        );
        
        future.addCallback(new ListenableFutureCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                System.out.println("  Callback 3: Processing result = " + result);
            }
            
            @Override
            public void onFailure(Throwable ex) {
                System.err.println("  Callback 3: Error - " + ex.getMessage());
            }
        });
        
        executor.shutdown();
    }
    
    /**
     * Error handling with callbacks
     */
    public static void errorHandlingCallbacks() {
        System.out.println("\n=== Error Handling Callbacks Example ===");
        
        AsyncListenableTaskExecutor executor = new AsyncListenableTaskExecutor(2);
        
        ListenableFuture<String> future = executor.submitListenable(() -> {
            Thread.sleep(500);
            throw new RuntimeException("Simulated failure");
        });
        
        future.addCallback(
            result -> System.out.println("  Success: " + result),
            error -> {
                System.err.println("  Error caught: " + error.getMessage());
                System.out.println("  Initiating recovery...");
                // Recovery logic here
            }
        );
        
        executor.shutdown();
    }
    
    /**
     * Timeout with callbacks
     */
    public static void timeoutWithCallbacks() throws Exception {
        System.out.println("\n=== Timeout with Callbacks Example ===");
        
        AsyncListenableTaskExecutor executor = new AsyncListenableTaskExecutor(2);
        
        ListenableFuture<String> future = executor.submitListenable(() -> {
            Thread.sleep(5000); // Long operation
            return "Delayed result";
        });
        
        future.addCallback(
            result -> System.out.println("  Success: " + result),
            error -> System.err.println("  Error: " + error.getMessage())
        );
        
        // Attempt to get with timeout
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            if (!future.isDone()) {
                System.out.println("  Operation timed out, cancelling...");
                future.cancel(true);
            }
        }, 2, TimeUnit.SECONDS);
        
        Thread.sleep(3000);
        scheduler.shutdown();
        executor.shutdown();
    }
}

/**
 * Demonstration of ListenableFuture Pattern
 */
public class ListenableFuturePattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== ListenableFuture Pattern Demo ===\n");
        
        // 1. Basic ListenableFuture with callbacks
        System.out.println("1. Basic ListenableFuture:");
        AsyncListenableTaskExecutor executor = new AsyncListenableTaskExecutor(3);
        
        ListenableFuture<String> future1 = executor.submitListenable(() -> {
            Thread.sleep(1000);
            return "Hello from ListenableFuture!";
        });
        
        future1.addCallback(
            result -> System.out.println("  Success: " + result),
            error -> System.err.println("  Error: " + error.getMessage())
        );
        
        Thread.sleep(1500);
        
        // 2. Email service example
        System.out.println("\n2. Email Service with Callbacks:");
        EmailService emailService = new EmailService();
        
        ListenableFuture<String> emailFuture = 
            emailService.sendEmailAsync("user@example.com", "Welcome!");
        
        emailFuture.addCallback(new ListenableFutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("  ✓ " + result);
            }
            
            @Override
            public void onFailure(Throwable ex) {
                System.err.println("  ✗ Email failed: " + ex.getMessage());
            }
        });
        
        Thread.sleep(2000);
        
        // 3. Payment service example
        System.out.println("\n3. Payment Processing:");
        PaymentService paymentService = new PaymentService();
        
        ListenableFuture<PaymentResult> paymentFuture = 
            paymentService.processPaymentAsync("ORD-123", 99.99);
        
        paymentFuture.addCallback(
            result -> System.out.println("  ✓ " + result),
            error -> System.err.println("  ✗ Payment failed: " + error.getMessage())
        );
        
        Thread.sleep(2500);
        
        // 4. Order processing with chained callbacks
        System.out.println("\n4. Order Processing with Chained Callbacks:");
        OrderProcessor orderProcessor = new OrderProcessor();
        orderProcessor.processOrderWithCallbacks("ORD-456", 149.99, "customer@example.com");
        
        Thread.sleep(3000);
        
        // 5. Error handling
        System.out.println("\n5. Error Handling:");
        ListenableFuture<String> invalidEmailFuture = 
            emailService.sendEmailAsync("invalid@", "Test");
        
        invalidEmailFuture.addCallback(
            result -> System.out.println("  ✓ " + result),
            error -> System.err.println("  ✗ Expected error: " + error.getMessage())
        );
        
        Thread.sleep(2000);
        
        // 6. Multiple callbacks
        CallbackPatterns.multipleCallbacks();
        Thread.sleep(1500);
        
        // 7. Error handling callbacks
        CallbackPatterns.errorHandlingCallbacks();
        Thread.sleep(1000);
        
        // 8. Timeout example
        CallbackPatterns.timeoutWithCallbacks();
        
        System.out.println("\n=== ListenableFuture vs Future ===");
        System.out.println("Future:");
        System.out.println("  - Blocking get()");
        System.out.println("  - No callback support");
        System.out.println("  - Basic error handling");
        
        System.out.println("\nListenableFuture:");
        System.out.println("  - Non-blocking callbacks");
        System.out.println("  - Success/failure handlers");
        System.out.println("  - Better error handling");
        System.out.println("  - Multiple callbacks per future");
        System.out.println("  - Spring integration");
        
        System.out.println("\n=== Callback Methods ===");
        System.out.println("addCallback(ListenableFutureCallback):");
        System.out.println("  - Single callback with onSuccess/onFailure");
        
        System.out.println("\naddCallback(SuccessCallback, FailureCallback):");
        System.out.println("  - Separate success and failure handlers");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("✓ Async email sending");
        System.out.println("✓ Payment processing");
        System.out.println("✓ External API calls");
        System.out.println("✓ Background job processing");
        System.out.println("✓ Notification systems");
        System.out.println("✓ Spring async methods");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Always add both success and failure callbacks");
        System.out.println("✓ Handle errors gracefully in callbacks");
        System.out.println("✓ Don't block in callback methods");
        System.out.println("✓ Use appropriate thread pool size");
        System.out.println("✓ Set timeouts for long operations");
        System.out.println("✓ Log callback execution for debugging");
        System.out.println("✓ Consider CompletableFuture for complex chains");
        
        System.out.println("\n=== Spring Integration ===");
        System.out.println("@Async methods can return ListenableFuture");
        System.out.println("AsyncListenableTaskExecutor in Spring");
        System.out.println("AsyncResult for wrapping results");
        System.out.println("Integration with @EnableAsync");
        
        executor.shutdown();
    }
}
