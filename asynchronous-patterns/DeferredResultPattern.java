package com.example.async.web;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * DeferredResult Pattern Implementation
 * 
 * Purpose: Handle async request processing in Spring MVC
 * 
 * Key Components:
 * 1. DeferredResult<T> - Async response container
 * 2. Timeout handling
 * 3. Completion callbacks
 * 4. Error handling
 * 
 * Use Cases:
 * - Long polling
 * - Server-Sent Events (SSE)
 * - Async external API calls
 * - Background job status
 * - Real-time notifications
 * 
 * Benefits:
 * - Free up request threads
 * - Better resource utilization
 * - Support for long-running operations
 * - Non-blocking request handling
 */

// Simulated Spring MVC DeferredResult
class DeferredResult<T> {
    private volatile T result;
    private volatile Throwable error;
    private volatile boolean isSet = false;
    private volatile boolean timedOut = false;
    private final Long timeoutValue;
    private final Object timeoutResult;
    
    private Runnable timeoutCallback;
    private Runnable completionCallback;
    private Consumer<Throwable> errorCallback;
    
    public DeferredResult() {
        this(null, null);
    }
    
    public DeferredResult(Long timeoutValue) {
        this(timeoutValue, null);
    }
    
    public DeferredResult(Long timeoutValue, Object timeoutResult) {
        this.timeoutValue = timeoutValue;
        this.timeoutResult = timeoutResult;
        
        if (timeoutValue != null) {
            scheduleTimeout();
        }
    }
    
    /**
     * Set the successful result
     */
    public boolean setResult(T result) {
        synchronized (this) {
            if (isSet || timedOut) {
                return false;
            }
            this.result = result;
            this.isSet = true;
            notifyCompletion();
            return true;
        }
    }
    
    /**
     * Set an error result
     */
    public boolean setErrorResult(Throwable error) {
        synchronized (this) {
            if (isSet || timedOut) {
                return false;
            }
            this.error = error;
            this.isSet = true;
            notifyError(error);
            return true;
        }
    }
    
    /**
     * Check if result is set
     */
    public boolean isSetOrExpired() {
        return isSet || timedOut;
    }
    
    /**
     * Get the result (for demonstration)
     */
    public T getResult() throws Throwable {
        if (timedOut && timeoutResult != null) {
            return (T) timeoutResult;
        }
        if (error != null) {
            throw error;
        }
        return result;
    }
    
    /**
     * Register timeout callback
     */
    public void onTimeout(Runnable callback) {
        this.timeoutCallback = callback;
    }
    
    /**
     * Register completion callback
     */
    public void onCompletion(Runnable callback) {
        this.completionCallback = callback;
    }
    
    /**
     * Register error callback
     */
    public void onError(Consumer<Throwable> callback) {
        this.errorCallback = callback;
    }
    
    private void scheduleTimeout() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            synchronized (this) {
                if (!isSet) {
                    timedOut = true;
                    if (timeoutCallback != null) {
                        timeoutCallback.run();
                    }
                    if (timeoutResult != null) {
                        result = (T) timeoutResult;
                    }
                }
            }
            scheduler.shutdown();
        }, timeoutValue, TimeUnit.MILLISECONDS);
    }
    
    private void notifyCompletion() {
        if (completionCallback != null) {
            completionCallback.run();
        }
    }
    
    private void notifyError(Throwable error) {
        if (errorCallback != null) {
            errorCallback.run();
        }
    }
}

// Controller Examples (Spring-style)
class AsyncController {
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final Map<String, DeferredResult<String>> pendingRequests = new ConcurrentHashMap<>();
    
    /**
     * Long polling endpoint
     */
    public DeferredResult<String> longPolling(String sessionId) {
        System.out.println("  [Controller] Long polling request from: " + sessionId);
        
        DeferredResult<String> deferredResult = new DeferredResult<>(30000L, "No updates");
        
        deferredResult.onTimeout(() -> {
            System.out.println("  [Controller] Request timed out: " + sessionId);
        });
        
        deferredResult.onCompletion(() -> {
            System.out.println("  [Controller] Request completed: " + sessionId);
        });
        
        // Store for later update
        pendingRequests.put(sessionId, deferredResult);
        
        return deferredResult;
    }
    
    /**
     * Send update to waiting client
     */
    public void sendUpdate(String sessionId, String message) {
        DeferredResult<String> deferredResult = pendingRequests.remove(sessionId);
        if (deferredResult != null && !deferredResult.isSetOrExpired()) {
            deferredResult.setResult(message);
            System.out.println("  [Controller] Sent update to: " + sessionId);
        }
    }
    
    /**
     * Async external API call
     */
    public DeferredResult<String> asyncApiCall(String apiName) {
        System.out.println("  [Controller] Async API call to: " + apiName);
        
        DeferredResult<String> deferredResult = new DeferredResult<>(5000L);
        
        deferredResult.onTimeout(() -> {
            System.out.println("  [Controller] API call timed out: " + apiName);
            deferredResult.setResult("API timeout");
        });
        
        // Execute async
        executorService.submit(() -> {
            try {
                System.out.println("  [Worker] Calling external API: " + apiName);
                Thread.sleep(2000);
                
                String response = "Response from " + apiName;
                deferredResult.setResult(response);
            } catch (Exception e) {
                deferredResult.setErrorResult(e);
            }
        });
        
        return deferredResult;
    }
    
    /**
     * Background job status
     */
    public DeferredResult<JobStatus> jobStatus(String jobId) {
        System.out.println("  [Controller] Checking job status: " + jobId);
        
        DeferredResult<JobStatus> deferredResult = new DeferredResult<>(10000L);
        
        deferredResult.onCompletion(() -> {
            System.out.println("  [Controller] Job status check completed: " + jobId);
        });
        
        deferredResult.onError(error -> {
            System.err.println("  [Controller] Job status error: " + error.getMessage());
        });
        
        // Check status asynchronously
        executorService.submit(() -> {
            try {
                Thread.sleep(1500);
                JobStatus status = new JobStatus(jobId, "COMPLETED", 100);
                deferredResult.setResult(status);
            } catch (Exception e) {
                deferredResult.setErrorResult(e);
            }
        });
        
        return deferredResult;
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}

class JobStatus {
    private final String jobId;
    private final String status;
    private final int progress;
    
    public JobStatus(String jobId, String status, int progress) {
        this.jobId = jobId;
        this.status = status;
        this.progress = progress;
    }
    
    public String getJobId() { return jobId; }
    public String getStatus() { return status; }
    public int getProgress() { return progress; }
    
    @Override
    public String toString() {
        return String.format("JobStatus[id=%s, status=%s, progress=%d%%]", 
            jobId, status, progress);
    }
}

// Real-time Notification Service
class NotificationService {
    private final Map<String, List<DeferredResult<String>>> subscribers = new ConcurrentHashMap<>();
    
    /**
     * Subscribe to notifications
     */
    public DeferredResult<String> subscribe(String topic) {
        System.out.println("  [NotificationService] New subscriber to: " + topic);
        
        DeferredResult<String> deferredResult = new DeferredResult<>(60000L, "No notifications");
        
        deferredResult.onTimeout(() -> {
            System.out.println("  [NotificationService] Subscription timed out: " + topic);
            removeSubscriber(topic, deferredResult);
        });
        
        deferredResult.onCompletion(() -> {
            removeSubscriber(topic, deferredResult);
        });
        
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(deferredResult);
        
        return deferredResult;
    }
    
    /**
     * Publish notification to all subscribers
     */
    public void publish(String topic, String message) {
        System.out.println("  [NotificationService] Publishing to " + topic + ": " + message);
        
        List<DeferredResult<String>> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            int count = 0;
            for (DeferredResult<String> subscriber : topicSubscribers) {
                if (!subscriber.isSetOrExpired()) {
                    subscriber.setResult(message);
                    count++;
                }
            }
            System.out.println("  [NotificationService] Notified " + count + " subscribers");
            topicSubscribers.clear();
        }
    }
    
    private void removeSubscriber(String topic, DeferredResult<String> deferredResult) {
        List<DeferredResult<String>> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            topicSubscribers.remove(deferredResult);
        }
    }
}

// Async Processing Service
class AsyncProcessingService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    /**
     * Process data asynchronously
     */
    public DeferredResult<ProcessingResult> processAsync(String data) {
        System.out.println("  [ProcessingService] Starting async processing: " + data);
        
        DeferredResult<ProcessingResult> deferredResult = new DeferredResult<>(15000L);
        
        deferredResult.onTimeout(() -> {
            System.out.println("  [ProcessingService] Processing timed out");
            deferredResult.setResult(new ProcessingResult(data, "TIMEOUT", null));
        });
        
        deferredResult.onCompletion(() -> {
            System.out.println("  [ProcessingService] Processing completed");
        });
        
        deferredResult.onError(error -> {
            System.err.println("  [ProcessingService] Processing error: " + error.getMessage());
        });
        
        executorService.submit(() -> {
            try {
                // Simulate complex processing
                System.out.println("  [Worker] Processing: " + data);
                Thread.sleep(3000);
                
                String result = data.toUpperCase();
                deferredResult.setResult(new ProcessingResult(data, "SUCCESS", result));
            } catch (Exception e) {
                deferredResult.setErrorResult(e);
            }
        });
        
        return deferredResult;
    }
    
    /**
     * Batch processing with progress updates
     */
    public DeferredResult<List<ProcessingResult>> batchProcess(List<String> items) {
        System.out.println("  [ProcessingService] Batch processing " + items.size() + " items");
        
        DeferredResult<List<ProcessingResult>> deferredResult = new DeferredResult<>(30000L);
        
        executorService.submit(() -> {
            try {
                List<ProcessingResult> results = new ArrayList<>();
                for (String item : items) {
                    Thread.sleep(1000);
                    results.add(new ProcessingResult(item, "SUCCESS", item.toUpperCase()));
                }
                deferredResult.setResult(results);
            } catch (Exception e) {
                deferredResult.setErrorResult(e);
            }
        });
        
        return deferredResult;
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}

class ProcessingResult {
    private final String input;
    private final String status;
    private final String result;
    
    public ProcessingResult(String input, String status, String result) {
        this.input = input;
        this.status = status;
        this.result = result;
    }
    
    public String getInput() { return input; }
    public String getStatus() { return status; }
    public String getResult() { return result; }
    
    @Override
    public String toString() {
        return String.format("ProcessingResult[input=%s, status=%s, result=%s]", 
            input, status, result);
    }
}

/**
 * Demonstration of DeferredResult Pattern
 */
public class DeferredResultPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== DeferredResult Pattern Demo ===\n");
        
        // 1. Basic async API call
        System.out.println("1. Async API Call:");
        AsyncController controller = new AsyncController();
        
        DeferredResult<String> apiResult = controller.asyncApiCall("WeatherService");
        
        // Simulate waiting for result
        Thread.sleep(3000);
        System.out.println("  Result: " + apiResult.getResult());
        
        // 2. Long polling
        System.out.println("\n2. Long Polling:");
        DeferredResult<String> longPollResult = controller.longPolling("session-123");
        
        // Simulate update after delay
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                controller.sendUpdate("session-123", "New data available!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        
        Thread.sleep(3000);
        System.out.println("  Result: " + longPollResult.getResult());
        
        // 3. Job status checking
        System.out.println("\n3. Job Status Check:");
        DeferredResult<JobStatus> jobResult = controller.jobStatus("job-456");
        
        Thread.sleep(2000);
        System.out.println("  " + jobResult.getResult());
        
        // 4. Timeout handling
        System.out.println("\n4. Timeout Handling:");
        DeferredResult<String> timeoutResult = new DeferredResult<>(2000L, "Default timeout response");
        
        timeoutResult.onTimeout(() -> {
            System.out.println("  Timeout callback executed");
        });
        
        // Don't set result - let it timeout
        Thread.sleep(2500);
        System.out.println("  Result after timeout: " + timeoutResult.getResult());
        
        // 5. Real-time notifications
        System.out.println("\n5. Real-time Notifications:");
        NotificationService notificationService = new NotificationService();
        
        DeferredResult<String> notification1 = notificationService.subscribe("news");
        DeferredResult<String> notification2 = notificationService.subscribe("news");
        
        // Publish after delay
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                notificationService.publish("news", "Breaking: New Spring version released!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        
        Thread.sleep(2000);
        System.out.println("  Subscriber 1 received: " + notification1.getResult());
        System.out.println("  Subscriber 2 received: " + notification2.getResult());
        
        // 6. Async processing
        System.out.println("\n6. Async Processing:");
        AsyncProcessingService processingService = new AsyncProcessingService();
        
        DeferredResult<ProcessingResult> processResult = processingService.processAsync("test data");
        
        Thread.sleep(4000);
        System.out.println("  " + processResult.getResult());
        
        // 7. Error handling
        System.out.println("\n7. Error Handling:");
        DeferredResult<String> errorResult = new DeferredResult<>(5000L);
        
        errorResult.onError(error -> {
            System.err.println("  Error callback: " + error.getMessage());
        });
        
        errorResult.setErrorResult(new RuntimeException("Simulated error"));
        
        try {
            errorResult.getResult();
        } catch (Throwable e) {
            System.err.println("  Caught error: " + e.getMessage());
        }
        
        // 8. Completion callbacks
        System.out.println("\n8. Completion Callbacks:");
        DeferredResult<String> completionResult = new DeferredResult<>();
        
        completionResult.onCompletion(() -> {
            System.out.println("  Completion callback executed");
        });
        
        completionResult.setResult("Completed successfully");
        Thread.sleep(100);
        
        System.out.println("\n=== DeferredResult Features ===");
        System.out.println("Construction:");
        System.out.println("  new DeferredResult() - no timeout");
        System.out.println("  new DeferredResult(timeout) - with timeout");
        System.out.println("  new DeferredResult(timeout, timeoutResult) - with timeout value");
        
        System.out.println("\nMethods:");
        System.out.println("  setResult(T) - set successful result");
        System.out.println("  setErrorResult(Throwable) - set error");
        System.out.println("  onTimeout(Runnable) - timeout callback");
        System.out.println("  onCompletion(Runnable) - completion callback");
        System.out.println("  onError(Consumer) - error callback");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("✓ Long polling for real-time updates");
        System.out.println("✓ Server-Sent Events (SSE)");
        System.out.println("✓ Async external API calls");
        System.out.println("✓ Background job status polling");
        System.out.println("✓ Chat applications");
        System.out.println("✓ Live dashboards");
        System.out.println("✓ Notification systems");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Always set a timeout");
        System.out.println("✓ Provide meaningful timeout results");
        System.out.println("✓ Handle errors properly");
        System.out.println("✓ Use onCompletion for cleanup");
        System.out.println("✓ Monitor active deferred results");
        System.out.println("✓ Don't hold too many pending requests");
        System.out.println("✓ Consider WebSocket for bidirectional comm");
        
        System.out.println("\n=== Advantages ===");
        System.out.println("✓ Frees up request threads");
        System.out.println("✓ Better scalability");
        System.out.println("✓ Support for long-running operations");
        System.out.println("✓ Built-in timeout handling");
        System.out.println("✓ Callback support");
        System.out.println("✓ Spring MVC integration");
        
        System.out.println("\n=== DeferredResult vs Callable ===");
        System.out.println("Callable:");
        System.out.println("  - Result must be returned from call()");
        System.out.println("  - Executes in Spring managed thread");
        System.out.println("  - Simpler for straightforward async");
        
        System.out.println("\nDeferredResult:");
        System.out.println("  - Result can be set from any thread");
        System.out.println("  - More flexible for event-driven");
        System.out.println("  - Better for long polling");
        System.out.println("  - Supports external event sources");
        
        controller.shutdown();
        processingService.shutdown();
    }
}
