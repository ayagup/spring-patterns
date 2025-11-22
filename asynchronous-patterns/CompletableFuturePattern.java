package com.example.async.completable;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * CompletableFuture Pattern Implementation
 * 
 * Purpose: Build non-blocking asynchronous pipelines with composition and chaining
 * 
 * Key Features:
 * 1. Non-blocking composition
 * 2. Exception handling
 * 3. Combining multiple futures
 * 4. Async transformation
 * 5. Custom executors
 * 
 * Advantages over Future:
 * - No blocking get()
 * - Callback support
 * - Exception handling
 * - Composition and chaining
 * - Combining multiple operations
 */

// Domain Models
class User {
    private final String id;
    private final String name;
    private final String email;
    
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    
    @Override
    public String toString() {
        return String.format("User[id=%s, name=%s, email=%s]", id, name, email);
    }
}

class Order {
    private final String orderId;
    private final String userId;
    private final double amount;
    
    public Order(String orderId, String userId, double amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
    }
    
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public double getAmount() { return amount; }
    
    @Override
    public String toString() {
        return String.format("Order[id=%s, userId=%s, amount=$%.2f]", orderId, userId, amount);
    }
}

class OrderSummary {
    private final User user;
    private final List<Order> orders;
    private final double totalAmount;
    
    public OrderSummary(User user, List<Order> orders) {
        this.user = user;
        this.orders = orders;
        this.totalAmount = orders.stream().mapToDouble(Order::getAmount).sum();
    }
    
    public User getUser() { return user; }
    public List<Order> getOrders() { return orders; }
    public double getTotalAmount() { return totalAmount; }
    
    @Override
    public String toString() {
        return String.format("OrderSummary[user=%s, orders=%d, total=$%.2f]",
            user.getName(), orders.size(), totalAmount);
    }
}

// Async Service Layer
class UserService {
    
    /**
     * Fetch user asynchronously
     */
    public CompletableFuture<User> getUserAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  [UserService] Fetching user " + userId + 
                " on thread: " + Thread.currentThread().getName());
            
            // Simulate database call
            sleep(1000);
            
            return new User(userId, "User-" + userId, userId + "@example.com");
        });
    }
    
    /**
     * Fetch multiple users in parallel
     */
    public CompletableFuture<List<User>> getUsersAsync(List<String> userIds) {
        List<CompletableFuture<User>> futures = userIds.stream()
            .map(this::getUserAsync)
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class OrderService {
    
    /**
     * Fetch orders for user
     */
    public CompletableFuture<List<Order>> getOrdersAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  [OrderService] Fetching orders for " + userId +
                " on thread: " + Thread.currentThread().getName());
            
            // Simulate database call
            sleep(800);
            
            return Arrays.asList(
                new Order("ORD-001", userId, 99.99),
                new Order("ORD-002", userId, 149.50)
            );
        });
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class NotificationService {
    
    /**
     * Send notification asynchronously
     */
    public CompletableFuture<Void> sendNotificationAsync(String userId, String message) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("  [NotificationService] Sending to " + userId + 
                ": " + message + " on thread: " + Thread.currentThread().getName());
            
            // Simulate sending email
            sleep(500);
        });
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// Orchestration Service
class OrderSummaryService {
    private final UserService userService;
    private final OrderService orderService;
    private final NotificationService notificationService;
    
    public OrderSummaryService() {
        this.userService = new UserService();
        this.orderService = new OrderService();
        this.notificationService = new NotificationService();
    }
    
    /**
     * Get order summary using composition
     */
    public CompletableFuture<OrderSummary> getOrderSummaryAsync(String userId) {
        return userService.getUserAsync(userId)
            .thenCompose(user -> {
                // Chain another async operation
                return orderService.getOrdersAsync(user.getId())
                    .thenApply(orders -> new OrderSummary(user, orders));
            });
    }
    
    /**
     * Get order summary and send notification
     */
    public CompletableFuture<OrderSummary> getOrderSummaryAndNotify(String userId) {
        return getOrderSummaryAsync(userId)
            .thenCompose(summary -> {
                String message = String.format("Your total: $%.2f", summary.getTotalAmount());
                return notificationService.sendNotificationAsync(userId, message)
                    .thenApply(v -> summary);
            });
    }
    
    /**
     * Combine multiple independent async operations
     */
    public CompletableFuture<String> processUserDataAsync(String userId) {
        CompletableFuture<User> userFuture = userService.getUserAsync(userId);
        CompletableFuture<List<Order>> ordersFuture = orderService.getOrdersAsync(userId);
        
        return userFuture.thenCombine(ordersFuture, (user, orders) -> {
            double total = orders.stream().mapToDouble(Order::getAmount).sum();
            return String.format("%s has %d orders totaling $%.2f", 
                user.getName(), orders.size(), total);
        });
    }
}

// Exception Handling Examples
class ResilientService {
    
    /**
     * Handle exceptions with exceptionally
     */
    public CompletableFuture<String> fetchDataWithFallback(String id) {
        return CompletableFuture.supplyAsync(() -> {
            if (id.equals("error")) {
                throw new RuntimeException("Data not found for: " + id);
            }
            return "Data for " + id;
        }).exceptionally(ex -> {
            System.out.println("  Error occurred: " + ex.getMessage());
            return "Default data";
        });
    }
    
    /**
     * Handle both success and failure with handle
     */
    public CompletableFuture<String> fetchDataWithHandle(String id) {
        return CompletableFuture.supplyAsync(() -> {
            if (id.equals("error")) {
                throw new RuntimeException("Data not found");
            }
            return "Data for " + id;
        }).handle((result, ex) -> {
            if (ex != null) {
                System.out.println("  Handling error: " + ex.getMessage());
                return "Recovered data";
            }
            return result;
        });
    }
    
    /**
     * Perform cleanup with whenComplete
     */
    public CompletableFuture<String> fetchDataWithCleanup(String id) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  Fetching data for " + id);
            return "Data for " + id;
        }).whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("  Cleanup after error");
            } else {
                System.out.println("  Cleanup after success: " + result);
            }
        });
    }
}

// Advanced Patterns
class AdvancedCompletableFuturePatterns {
    
    /**
     * Race multiple futures - use first result
     */
    public static CompletableFuture<String> raceRequests() {
        CompletableFuture<String> service1 = CompletableFuture.supplyAsync(() -> {
            sleep(1000);
            return "Service 1 response";
        });
        
        CompletableFuture<String> service2 = CompletableFuture.supplyAsync(() -> {
            sleep(500);
            return "Service 2 response";
        });
        
        return CompletableFuture.anyOf(service1, service2)
            .thenApply(result -> (String) result);
    }
    
    /**
     * Wait for all futures to complete
     */
    public static CompletableFuture<List<String>> waitForAll() {
        List<CompletableFuture<String>> futures = Arrays.asList(
            CompletableFuture.supplyAsync(() -> { sleep(500); return "Task 1"; }),
            CompletableFuture.supplyAsync(() -> { sleep(800); return "Task 2"; }),
            CompletableFuture.supplyAsync(() -> { sleep(300); return "Task 3"; })
        );
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
    
    /**
     * Timeout handling
     */
    public static CompletableFuture<String> withTimeout() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            sleep(5000);
            return "Delayed result";
        });
        
        // Java 9+ orTimeout
        // return future.orTimeout(2, TimeUnit.SECONDS);
        
        // Pre-Java 9 alternative
        CompletableFuture<String> timeout = new CompletableFuture<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            timeout.completeExceptionally(new TimeoutException("Operation timed out"));
        }, 2, TimeUnit.SECONDS);
        
        return future.applyToEither(timeout, Function.identity());
    }
    
    /**
     * Custom executor
     */
    public static void customExecutorExample() {
        ExecutorService customExecutor = Executors.newFixedThreadPool(5);
        
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("  Running on custom executor: " + 
                Thread.currentThread().getName());
            return "Custom executor result";
        }, customExecutor);
        
        future.thenAccept(result -> System.out.println("  " + result));
        
        customExecutor.shutdown();
    }
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Demonstration of CompletableFuture Pattern
 */
public class CompletableFuturePattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== CompletableFuture Pattern Demo ===\n");
        
        // 1. Basic async execution
        System.out.println("1. Basic Async Execution:");
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("  Running on: " + Thread.currentThread().getName());
            return "Hello, CompletableFuture!";
        });
        
        future1.thenAccept(result -> System.out.println("  Result: " + result));
        Thread.sleep(100);
        
        // 2. Chaining with thenApply
        System.out.println("\n2. Chaining with thenApply:");
        CompletableFuture.supplyAsync(() -> {
            System.out.println("  Step 1: Fetching data");
            return "raw data";
        }).thenApply(data -> {
            System.out.println("  Step 2: Processing: " + data);
            return data.toUpperCase();
        }).thenApply(processed -> {
            System.out.println("  Step 3: Formatting: " + processed);
            return "Formatted: " + processed;
        }).thenAccept(result -> {
            System.out.println("  Final result: " + result);
        }).join(); // Wait for completion
        
        // 3. Composition with thenCompose
        System.out.println("\n3. Composition (thenCompose):");
        OrderSummaryService summaryService = new OrderSummaryService();
        
        CompletableFuture<OrderSummary> summaryFuture = 
            summaryService.getOrderSummaryAsync("user-123");
        
        summaryFuture.thenAccept(summary -> {
            System.out.println("  " + summary);
        }).join();
        
        // 4. Combining with thenCombine
        System.out.println("\n4. Combining (thenCombine):");
        String result = summaryService.processUserDataAsync("user-456").join();
        System.out.println("  " + result);
        
        // 5. Exception handling
        System.out.println("\n5. Exception Handling:");
        ResilientService resilientService = new ResilientService();
        
        System.out.println("  With exceptionally:");
        String fallbackResult = resilientService.fetchDataWithFallback("error").join();
        System.out.println("  Result: " + fallbackResult);
        
        System.out.println("\n  With handle:");
        String handledResult = resilientService.fetchDataWithHandle("error").join();
        System.out.println("  Result: " + handledResult);
        
        System.out.println("\n  With whenComplete:");
        resilientService.fetchDataWithCleanup("data-123").join();
        
        // 6. Combining multiple futures
        System.out.println("\n6. Combining Multiple Futures (allOf):");
        List<String> allResults = AdvancedCompletableFuturePatterns.waitForAll().join();
        System.out.println("  All results: " + allResults);
        
        // 7. Racing futures
        System.out.println("\n7. Racing Futures (anyOf):");
        String firstResult = AdvancedCompletableFuturePatterns.raceRequests().join();
        System.out.println("  First result: " + firstResult);
        
        // 8. Custom executor
        System.out.println("\n8. Custom Executor:");
        AdvancedCompletableFuturePatterns.customExecutorExample();
        Thread.sleep(100);
        
        System.out.println("\n=== CompletableFuture Methods ===");
        System.out.println("Creation:");
        System.out.println("  supplyAsync() - async with result");
        System.out.println("  runAsync() - async without result");
        System.out.println("  completedFuture() - already completed");
        
        System.out.println("\nTransformation:");
        System.out.println("  thenApply() - transform result");
        System.out.println("  thenAccept() - consume result");
        System.out.println("  thenRun() - run after completion");
        
        System.out.println("\nComposition:");
        System.out.println("  thenCompose() - chain dependent async ops");
        System.out.println("  thenCombine() - combine independent futures");
        
        System.out.println("\nCombining:");
        System.out.println("  allOf() - wait for all");
        System.out.println("  anyOf() - wait for any");
        
        System.out.println("\nError Handling:");
        System.out.println("  exceptionally() - handle exception");
        System.out.println("  handle() - handle both result and exception");
        System.out.println("  whenComplete() - cleanup/logging");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Use thenApply for transformations");
        System.out.println("✓ Use thenCompose for chaining async operations");
        System.out.println("✓ Use thenCombine for independent parallel operations");
        System.out.println("✓ Always handle exceptions with exceptionally/handle");
        System.out.println("✓ Use custom executors for CPU-intensive tasks");
        System.out.println("✓ Avoid blocking with get() - use thenAccept");
        System.out.println("✓ Set timeouts for external operations");
        
        System.out.println("\n=== Async Methods ===");
        System.out.println("thenApplyAsync() - uses different thread");
        System.out.println("thenAcceptAsync() - uses different thread");
        System.out.println("thenComposeAsync() - uses different thread");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("✓ Microservices orchestration");
        System.out.println("✓ Parallel API calls");
        System.out.println("✓ Database + cache queries");
        System.out.println("✓ Event-driven workflows");
        System.out.println("✓ Async notification pipelines");
        
        ForkJoinPool.commonPool().shutdown();
    }
}
