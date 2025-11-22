package com.example.async.callback;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * Callback and Promise Pattern Implementation
 * 
 * Purpose: Handle asynchronous operations with callbacks and promises
 * 
 * Key Concepts:
 * 1. Callback - Function passed to async operation
 * 2. Promise - Container for future value
 * 3. Success/Failure handling
 * 4. Chaining callbacks
 * 5. Error propagation
 * 
 * Callback Types:
 * - Success callback
 * - Error callback
 * - Completion callback
 * - Progress callback
 * 
 * Promise States:
 * - Pending
 * - Fulfilled
 * - Rejected
 */

// Callback Interfaces
interface SuccessCallback<T> {
    void onSuccess(T result);
}

interface ErrorCallback {
    void onError(Throwable error);
}

interface CompletionCallback {
    void onComplete();
}

interface ProgressCallback {
    void onProgress(int progress);
}

// Combined Callback
interface AsyncCallback<T> {
    void onSuccess(T result);
    void onError(Throwable error);
}

// Promise Implementation
class Promise<T> {
    private enum State {
        PENDING, FULFILLED, REJECTED
    }
    
    private State state = State.PENDING;
    private T value;
    private Throwable error;
    
    private final List<SuccessCallback<T>> successCallbacks = new CopyOnWriteArrayList<>();
    private final List<ErrorCallback> errorCallbacks = new CopyOnWriteArrayList<>();
    private final List<CompletionCallback> completionCallbacks = new CopyOnWriteArrayList<>();
    
    /**
     * Create resolved promise
     */
    public static <T> Promise<T> resolve(T value) {
        Promise<T> promise = new Promise<>();
        promise.fulfill(value);
        return promise;
    }
    
    /**
     * Create rejected promise
     */
    public static <T> Promise<T> reject(Throwable error) {
        Promise<T> promise = new Promise<>();
        promise.reject(error);
        return promise;
    }
    
    /**
     * Add success callback
     */
    public Promise<T> then(SuccessCallback<T> callback) {
        synchronized (this) {
            if (state == State.FULFILLED) {
                callback.onSuccess(value);
            } else if (state == State.PENDING) {
                successCallbacks.add(callback);
            }
        }
        return this;
    }
    
    /**
     * Add error callback
     */
    public Promise<T> catchError(ErrorCallback callback) {
        synchronized (this) {
            if (state == State.REJECTED) {
                callback.onError(error);
            } else if (state == State.PENDING) {
                errorCallbacks.add(callback);
            }
        }
        return this;
    }
    
    /**
     * Add completion callback (called on either success or failure)
     */
    public Promise<T> finally_(CompletionCallback callback) {
        synchronized (this) {
            if (state != State.PENDING) {
                callback.onComplete();
            } else {
                completionCallbacks.add(callback);
            }
        }
        return this;
    }
    
    /**
     * Transform promise result
     */
    public <R> Promise<R> thenMap(Function<T, R> mapper) {
        Promise<R> newPromise = new Promise<>();
        
        this.then(value -> {
            try {
                R mapped = mapper.apply(value);
                newPromise.fulfill(mapped);
            } catch (Exception e) {
                newPromise.reject(e);
            }
        });
        
        this.catchError(newPromise::reject);
        
        return newPromise;
    }
    
    /**
     * Chain another promise
     */
    public <R> Promise<R> thenCompose(Function<T, Promise<R>> mapper) {
        Promise<R> newPromise = new Promise<>();
        
        this.then(value -> {
            try {
                Promise<R> nextPromise = mapper.apply(value);
                nextPromise.then(newPromise::fulfill);
                nextPromise.catchError(newPromise::reject);
            } catch (Exception e) {
                newPromise.reject(e);
            }
        });
        
        this.catchError(newPromise::reject);
        
        return newPromise;
    }
    
    /**
     * Fulfill the promise
     */
    public void fulfill(T value) {
        synchronized (this) {
            if (state != State.PENDING) {
                return;
            }
            
            this.value = value;
            this.state = State.FULFILLED;
            
            for (SuccessCallback<T> callback : successCallbacks) {
                try {
                    callback.onSuccess(value);
                } catch (Exception e) {
                    System.err.println("Callback error: " + e.getMessage());
                }
            }
            
            for (CompletionCallback callback : completionCallbacks) {
                try {
                    callback.onComplete();
                } catch (Exception e) {
                    System.err.println("Completion callback error: " + e.getMessage());
                }
            }
            
            successCallbacks.clear();
            errorCallbacks.clear();
            completionCallbacks.clear();
        }
    }
    
    /**
     * Reject the promise
     */
    public void reject(Throwable error) {
        synchronized (this) {
            if (state != State.PENDING) {
                return;
            }
            
            this.error = error;
            this.state = State.REJECTED;
            
            for (ErrorCallback callback : errorCallbacks) {
                try {
                    callback.onError(error);
                } catch (Exception e) {
                    System.err.println("Error callback error: " + e.getMessage());
                }
            }
            
            for (CompletionCallback callback : completionCallbacks) {
                try {
                    callback.onComplete();
                } catch (Exception e) {
                    System.err.println("Completion callback error: " + e.getMessage());
                }
            }
            
            successCallbacks.clear();
            errorCallbacks.clear();
            completionCallbacks.clear();
        }
    }
    
    /**
     * Get current state
     */
    public boolean isPending() {
        return state == State.PENDING;
    }
    
    public boolean isFulfilled() {
        return state == State.FULFILLED;
    }
    
    public boolean isRejected() {
        return state == State.REJECTED;
    }
}

// Async Operations with Callbacks
class FileService {
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    
    /**
     * Read file asynchronously with callback
     */
    public void readFileAsync(String filename, AsyncCallback<String> callback) {
        executor.submit(() -> {
            try {
                System.out.println("  [FileService] Reading file: " + filename);
                Thread.sleep(1000);
                
                if (filename.isEmpty()) {
                    throw new IllegalArgumentException("Filename is empty");
                }
                
                String content = "Content of " + filename;
                callback.onSuccess(content);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    /**
     * Read file returning promise
     */
    public Promise<String> readFilePromise(String filename) {
        Promise<String> promise = new Promise<>();
        
        executor.submit(() -> {
            try {
                System.out.println("  [FileService] Reading file: " + filename);
                Thread.sleep(1000);
                
                if (filename.isEmpty()) {
                    throw new IllegalArgumentException("Filename is empty");
                }
                
                String content = "Content of " + filename;
                promise.fulfill(content);
            } catch (Exception e) {
                promise.reject(e);
            }
        });
        
        return promise;
    }
    
    /**
     * Write file with progress callback
     */
    public void writeFileAsync(String filename, String content, 
                              SuccessCallback<Void> onSuccess,
                              ErrorCallback onError,
                              ProgressCallback onProgress) {
        executor.submit(() -> {
            try {
                System.out.println("  [FileService] Writing file: " + filename);
                
                for (int i = 0; i <= 100; i += 20) {
                    Thread.sleep(300);
                    onProgress.onProgress(i);
                }
                
                System.out.println("  [FileService] File written: " + filename);
                onSuccess.onSuccess(null);
            } catch (Exception e) {
                onError.onError(e);
            }
        });
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Database Service with Promises
class DatabaseService {
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    
    public Promise<User> findUserById(String userId) {
        Promise<User> promise = new Promise<>();
        
        executor.submit(() -> {
            try {
                System.out.println("  [DB] Finding user: " + userId);
                Thread.sleep(800);
                
                if (userId.equals("notfound")) {
                    throw new RuntimeException("User not found");
                }
                
                User user = new User(userId, "User " + userId, userId + "@example.com");
                promise.fulfill(user);
            } catch (Exception e) {
                promise.reject(e);
            }
        });
        
        return promise;
    }
    
    public Promise<List<Order>> findOrdersByUserId(String userId) {
        Promise<List<Order>> promise = new Promise<>();
        
        executor.submit(() -> {
            try {
                System.out.println("  [DB] Finding orders for user: " + userId);
                Thread.sleep(1000);
                
                List<Order> orders = Arrays.asList(
                    new Order("ORD-1", userId, 99.99),
                    new Order("ORD-2", userId, 149.50)
                );
                
                promise.fulfill(orders);
            } catch (Exception e) {
                promise.reject(e);
            }
        });
        
        return promise;
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

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

// HTTP Client with Callbacks
class HttpClient {
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    
    public void get(String url, SuccessCallback<String> onSuccess, ErrorCallback onError) {
        executor.submit(() -> {
            try {
                System.out.println("  [HTTP] GET " + url);
                Thread.sleep(1500);
                
                if (url.contains("error")) {
                    throw new RuntimeException("HTTP 500 Internal Server Error");
                }
                
                String response = "{\"status\": \"success\", \"url\": \"" + url + "\"}";
                onSuccess.onSuccess(response);
            } catch (Exception e) {
                onError.onError(e);
            }
        });
    }
    
    public Promise<String> getPromise(String url) {
        Promise<String> promise = new Promise<>();
        
        executor.submit(() -> {
            try {
                System.out.println("  [HTTP] GET " + url);
                Thread.sleep(1500);
                
                if (url.contains("error")) {
                    throw new RuntimeException("HTTP 500 Internal Server Error");
                }
                
                String response = "{\"status\": \"success\", \"url\": \"" + url + "\"}";
                promise.fulfill(response);
            } catch (Exception e) {
                promise.reject(e);
            }
        });
        
        return promise;
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Promise Utilities
class PromiseUtil {
    
    /**
     * Wait for all promises
     */
    public static <T> Promise<List<T>> all(List<Promise<T>> promises) {
        Promise<List<T>> resultPromise = new Promise<>();
        
        int total = promises.size();
        List<T> results = new ArrayList<>(Collections.nCopies(total, null));
        int[] completedCount = {0};
        
        for (int i = 0; i < promises.size(); i++) {
            int index = i;
            promises.get(i)
                .then(value -> {
                    synchronized (results) {
                        results.set(index, value);
                        completedCount[0]++;
                        
                        if (completedCount[0] == total) {
                            resultPromise.fulfill(results);
                        }
                    }
                })
                .catchError(resultPromise::reject);
        }
        
        return resultPromise;
    }
    
    /**
     * Return first resolved promise
     */
    public static <T> Promise<T> race(List<Promise<T>> promises) {
        Promise<T> resultPromise = new Promise<>();
        
        for (Promise<T> promise : promises) {
            promise.then(resultPromise::fulfill);
            promise.catchError(resultPromise::reject);
        }
        
        return resultPromise;
    }
}

/**
 * Demonstration of Callback and Promise Pattern
 */
public class CallbackAndPromisePattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Callback and Promise Pattern Demo ===\n");
        
        FileService fileService = new FileService();
        DatabaseService dbService = new DatabaseService();
        HttpClient httpClient = new HttpClient();
        
        // 1. Basic callback
        System.out.println("1. Basic Callback:");
        fileService.readFileAsync("test.txt", new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("  ✓ Success: " + result);
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.println("  ✗ Error: " + error.getMessage());
            }
        });
        Thread.sleep(1500);
        
        // 2. Lambda callbacks
        System.out.println("\n2. Lambda Callbacks:");
        httpClient.get("https://api.example.com/data",
            response -> System.out.println("  ✓ Response: " + response),
            error -> System.err.println("  ✗ Error: " + error.getMessage())
        );
        Thread.sleep(2000);
        
        // 3. Progress callback
        System.out.println("\n3. Progress Callback:");
        fileService.writeFileAsync("output.txt", "data",
            result -> System.out.println("  ✓ Write completed"),
            error -> System.err.println("  ✗ Write failed: " + error.getMessage()),
            progress -> System.out.println("  Progress: " + progress + "%")
        );
        Thread.sleep(2000);
        
        // 4. Basic promise
        System.out.println("\n4. Basic Promise:");
        Promise<String> filePromise = fileService.readFilePromise("data.txt");
        
        filePromise
            .then(content -> System.out.println("  ✓ File content: " + content))
            .catchError(error -> System.err.println("  ✗ Error: " + error.getMessage()))
            .finally_(() -> System.out.println("  Cleanup complete"));
        
        Thread.sleep(1500);
        
        // 5. Promise chaining
        System.out.println("\n5. Promise Chaining:");
        dbService.findUserById("user-123")
            .then(user -> {
                System.out.println("  Found user: " + user);
            })
            .thenCompose(user -> {
                System.out.println("  Fetching orders...");
                return dbService.findOrdersByUserId(user.getId());
            })
            .then(orders -> {
                System.out.println("  Found " + orders.size() + " orders");
                orders.forEach(order -> System.out.println("    " + order));
            })
            .catchError(error -> {
                System.err.println("  ✗ Chain error: " + error.getMessage());
            });
        
        Thread.sleep(2500);
        
        // 6. Promise transformation
        System.out.println("\n6. Promise Transformation:");
        dbService.findUserById("user-456")
            .thenMap(user -> user.getName().toUpperCase())
            .then(name -> System.out.println("  Uppercase name: " + name))
            .catchError(error -> System.err.println("  ✗ Error: " + error.getMessage()));
        
        Thread.sleep(1200);
        
        // 7. Multiple promises
        System.out.println("\n7. Multiple Promises (Promise.all):");
        List<Promise<String>> filePromises = Arrays.asList(
            fileService.readFilePromise("file1.txt"),
            fileService.readFilePromise("file2.txt"),
            fileService.readFilePromise("file3.txt")
        );
        
        PromiseUtil.all(filePromises)
            .then(results -> {
                System.out.println("  All files read:");
                results.forEach(content -> System.out.println("    " + content));
            })
            .catchError(error -> System.err.println("  ✗ Error: " + error.getMessage()));
        
        Thread.sleep(1500);
        
        // 8. Promise race
        System.out.println("\n8. Promise Race:");
        List<Promise<String>> racePromises = Arrays.asList(
            httpClient.getPromise("https://api1.example.com"),
            httpClient.getPromise("https://api2.example.com")
        );
        
        PromiseUtil.race(racePromises)
            .then(result -> System.out.println("  First result: " + result))
            .catchError(error -> System.err.println("  ✗ Error: " + error.getMessage()));
        
        Thread.sleep(2000);
        
        // 9. Error handling
        System.out.println("\n9. Error Handling:");
        dbService.findUserById("notfound")
            .then(user -> System.out.println("  Found user: " + user))
            .catchError(error -> {
                System.err.println("  ✗ Caught error: " + error.getMessage());
                System.out.println("  Returning default user...");
            });
        
        Thread.sleep(1200);
        
        // 10. Resolved/Rejected promises
        System.out.println("\n10. Resolved/Rejected Promises:");
        
        Promise<String> resolved = Promise.resolve("Immediate value");
        resolved.then(value -> System.out.println("  Resolved: " + value));
        
        Promise<String> rejected = Promise.reject(new RuntimeException("Immediate error"));
        rejected.catchError(error -> System.err.println("  Rejected: " + error.getMessage()));
        
        Thread.sleep(100);
        
        System.out.println("\n=== Callback Pattern ===");
        System.out.println("Types:");
        System.out.println("  - Success Callback: Called on successful completion");
        System.out.println("  - Error Callback: Called on failure");
        System.out.println("  - Progress Callback: Called during execution");
        System.out.println("  - Completion Callback: Called on either success or failure");
        
        System.out.println("\nAdvantages:");
        System.out.println("  ✓ Simple concept");
        System.out.println("  ✓ Widely supported");
        System.out.println("  ✓ Flexible");
        
        System.out.println("\nDisadvantages:");
        System.out.println("  ✗ Callback hell (nested callbacks)");
        System.out.println("  ✗ Error handling complexity");
        System.out.println("  ✗ Inversion of control");
        
        System.out.println("\n=== Promise Pattern ===");
        System.out.println("States:");
        System.out.println("  - Pending: Initial state");
        System.out.println("  - Fulfilled: Operation completed successfully");
        System.out.println("  - Rejected: Operation failed");
        
        System.out.println("\nMethods:");
        System.out.println("  - then(): Handle success");
        System.out.println("  - catch(): Handle errors");
        System.out.println("  - finally(): Cleanup");
        System.out.println("  - all(): Wait for all promises");
        System.out.println("  - race(): First to complete");
        
        System.out.println("\nAdvantages:");
        System.out.println("  ✓ Chainable");
        System.out.println("  ✓ Better error handling");
        System.out.println("  ✓ Composable");
        System.out.println("  ✓ Avoids callback hell");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("✓ Async I/O operations");
        System.out.println("✓ HTTP requests");
        System.out.println("✓ Database queries");
        System.out.println("✓ File operations");
        System.out.println("✓ Event handling");
        System.out.println("✓ Animation/UI updates");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Always handle errors");
        System.out.println("✓ Use promises over nested callbacks");
        System.out.println("✓ Return promises for chaining");
        System.out.println("✓ Use async/await for cleaner code (Java: CompletableFuture)");
        System.out.println("✓ Avoid creating unnecessary promises");
        System.out.println("✓ Use Promise.all for parallel operations");
        System.out.println("✓ Set timeouts for long operations");
        
        System.out.println("\n=== Callback vs Promise vs CompletableFuture ===");
        System.out.println("Callback:");
        System.out.println("  - Function passed to async operation");
        System.out.println("  - Called when operation completes");
        System.out.println("  - Can lead to callback hell");
        
        System.out.println("\nPromise:");
        System.out.println("  - Object representing future value");
        System.out.println("  - Chainable methods");
        System.out.println("  - Better error propagation");
        
        System.out.println("\nCompletableFuture:");
        System.out.println("  - Java's implementation of promises");
        System.out.println("  - Rich API for composition");
        System.out.println("  - Built-in timeout and exception handling");
        
        fileService.shutdown();
        dbService.shutdown();
        httpClient.shutdown();
    }
}
