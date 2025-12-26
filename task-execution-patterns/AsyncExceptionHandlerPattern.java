package com.example.taskexecution;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Async Exception Handler Pattern - Handle Async Method Exceptions
 * 
 * AsyncUncaughtExceptionHandler provides a mechanism to handle exceptions
 * that occur in @Async annotated methods, particularly for methods that
 * return void (fire-and-forget scenarios).
 * 
 * Key Concepts:
 * 
 * 1. Exception Handling:
 *    - Catches exceptions from @Async void methods
 *    - Does NOT apply to methods returning Future/CompletableFuture
 *    - Centralized error handling
 * 
 * 2. Method Information:
 *    - Access to method that threw exception
 *    - Access to method parameters
 *    - Useful for logging/debugging
 * 
 * 3. Integration:
 *    - Used with AsyncConfigurer
 *    - Global async exception handling
 *    - Applied to all @Async void methods
 * 
 * Exception Handling Scenarios:
 * 
 * @Async void method:
 * - Exception caught by AsyncUncaughtExceptionHandler
 * - Cannot return result to caller
 * - Fire-and-forget semantics
 * 
 * @Async Future<T> method:
 * - Exception wrapped in Future
 * - Caller can catch via Future.get()
 * - Handler NOT invoked
 * 
 * @Async CompletableFuture<T> method:
 * - Exception in CompletableFuture
 * - Handled via exceptionally(), handle(), etc.
 * - Handler NOT invoked
 * 
 * Handler Method Signature:
 * void handleUncaughtException(
 *     Throwable ex,        // The thrown exception
 *     Method method,       // The async method
 *     Object... params     // Method parameters
 * )
 * 
 * Common Handler Implementations:
 * 
 * 1. Logging Handler:
 *    - Log exception details
 *    - Method name, parameters
 *    - Stack trace
 * 
 * 2. Notification Handler:
 *    - Send alerts/notifications
 *    - Email, Slack, PagerDuty
 *    - Critical error notifications
 * 
 * 3. Metrics Handler:
 *    - Record error metrics
 *    - Error counting
 *    - Failure rate tracking
 * 
 * 4. Retry Handler:
 *    - Trigger retry logic
 *    - Dead letter queue
 *    - Compensating actions
 * 
 * Use Cases:
 * - Centralized async error handling
 * - Logging async failures
 * - Error monitoring/alerting
 * - Metrics collection
 * - Dead letter processing
 * - Audit trail
 * 
 * Best Practices:
 * - Log complete exception info
 * - Include method and parameters
 * - Don't throw exceptions from handler
 * - Consider async method return types
 * - Monitor handler execution
 * - Use for fire-and-forget methods
 */
public class AsyncExceptionHandlerPattern {

    /**
     * Basic logging exception handler
     */
    static class LoggingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            System.err.println("\n========== ASYNC EXCEPTION ==========");
            System.err.println("Timestamp: " + System.currentTimeMillis());
            System.err.println("Method: " + method.getDeclaringClass().getSimpleName() + 
                             "." + method.getName());
            System.err.println("Parameters: " + params.length);
            for (int i = 0; i < params.length; i++) {
                System.err.println("  [" + i + "]: " + params[i]);
            }
            System.err.println("Exception Type: " + ex.getClass().getName());
            System.err.println("Exception Message: " + ex.getMessage());
            System.err.println("=====================================\n");
        }
    }

    /**
     * Detailed exception handler with stack trace
     */
    static class DetailedAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            System.err.println("\n========== DETAILED ASYNC EXCEPTION ==========");
            System.err.println("Time: " + new java.util.Date());
            System.err.println("Class: " + method.getDeclaringClass().getName());
            System.err.println("Method: " + method.getName());
            System.err.println("Return Type: " + method.getReturnType().getName());
            
            System.err.println("\nMethod Parameters:");
            Class<?>[] paramTypes = method.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                System.err.println("  [" + i + "] " + paramTypes[i].getSimpleName() + 
                                 " = " + params[i]);
            }
            
            System.err.println("\nException Details:");
            System.err.println("  Type: " + ex.getClass().getName());
            System.err.println("  Message: " + ex.getMessage());
            
            System.err.println("\nStack Trace:");
            ex.printStackTrace();
            
            if (ex.getCause() != null) {
                System.err.println("\nCaused By:");
                ex.getCause().printStackTrace();
            }
            
            System.err.println("==============================================\n");
        }
    }

    /**
     * Metrics-collecting exception handler
     */
    static class MetricsAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        private int totalErrors = 0;
        private java.util.Map<String, Integer> errorsByMethod = new java.util.HashMap<>();
        private java.util.Map<String, Integer> errorsByType = new java.util.HashMap<>();
        
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            totalErrors++;
            
            String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
            errorsByMethod.merge(methodName, 1, Integer::sum);
            
            String exceptionType = ex.getClass().getSimpleName();
            errorsByType.merge(exceptionType, 1, Integer::sum);
            
            System.err.println("Async error in " + methodName + ": " + ex.getMessage());
        }
        
        public void printMetrics() {
            System.out.println("\n=== Async Exception Metrics ===");
            System.out.println("Total Errors: " + totalErrors);
            
            System.out.println("\nErrors by Method:");
            errorsByMethod.forEach((method, count) -> 
                System.out.println("  " + method + ": " + count));
            
            System.out.println("\nErrors by Type:");
            errorsByType.forEach((type, count) -> 
                System.out.println("  " + type + ": " + count));
        }
    }

    /**
     * Notification-sending exception handler
     */
    static class NotificationAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            String message = buildNotificationMessage(ex, method, params);
            sendNotification(message);
        }
        
        private String buildNotificationMessage(Throwable ex, Method method, Object... params) {
            StringBuilder sb = new StringBuilder();
            sb.append("ASYNC METHOD FAILURE ALERT\n\n");
            sb.append("Method: ").append(method.getName()).append("\n");
            sb.append("Exception: ").append(ex.getClass().getSimpleName()).append("\n");
            sb.append("Message: ").append(ex.getMessage()).append("\n");
            sb.append("Parameters: ").append(java.util.Arrays.toString(params)).append("\n");
            sb.append("Time: ").append(new java.util.Date()).append("\n");
            return sb.toString();
        }
        
        private void sendNotification(String message) {
            System.err.println("\n[NOTIFICATION SENT]");
            System.err.println(message);
            // In real app: send email, Slack message, PagerDuty alert, etc.
        }
    }

    /**
     * Composite exception handler (delegates to multiple handlers)
     */
    static class CompositeAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        private java.util.List<AsyncUncaughtExceptionHandler> handlers;
        
        public CompositeAsyncExceptionHandler(AsyncUncaughtExceptionHandler... handlers) {
            this.handlers = java.util.Arrays.asList(handlers);
        }
        
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            for (AsyncUncaughtExceptionHandler handler : handlers) {
                try {
                    handler.handleUncaughtException(ex, method, params);
                } catch (Exception e) {
                    System.err.println("Error in exception handler: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Example async service
     */
    static class AsyncService {
        
        @Async
        public void voidAsyncMethod(String param) {
            System.out.println("Executing void async method with: " + param);
            if ("error".equals(param)) {
                throw new RuntimeException("Simulated error in void async method");
            }
        }
        
        @Async
        public Future<String> futureAsyncMethod(String param) {
            System.out.println("Executing Future async method with: " + param);
            if ("error".equals(param)) {
                throw new RuntimeException("Error in Future async method");
            }
            return CompletableFuture.completedFuture("Result: " + param);
        }
        
        @Async
        public CompletableFuture<String> completableFutureAsyncMethod(String param) {
            System.out.println("Executing CompletableFuture async method with: " + param);
            return CompletableFuture.supplyAsync(() -> {
                if ("error".equals(param)) {
                    throw new RuntimeException("Error in CompletableFuture async method");
                }
                return "Result: " + param;
            });
        }
    }

    /**
     * Usage examples
     */
    static class AsyncExceptionHandlerExamples {
        
        public void demonstrateBasicHandler() throws NoSuchMethodException {
            System.out.println("\n=== Basic Exception Handler ===");
            
            LoggingAsyncExceptionHandler handler = new LoggingAsyncExceptionHandler();
            
            Method method = AsyncService.class.getMethod("voidAsyncMethod", String.class);
            RuntimeException exception = new RuntimeException("Test exception");
            
            handler.handleUncaughtException(exception, method, "test-param");
        }
        
        public void demonstrateDetailedHandler() throws NoSuchMethodException {
            System.out.println("\n=== Detailed Exception Handler ===");
            
            DetailedAsyncExceptionHandler handler = new DetailedAsyncExceptionHandler();
            
            Method method = AsyncService.class.getMethod("voidAsyncMethod", String.class);
            IllegalArgumentException exception = new IllegalArgumentException(
                "Invalid parameter", new NullPointerException("Null value")
            );
            
            handler.handleUncaughtException(exception, method, "param1");
        }
        
        public void demonstrateMetricsHandler() throws NoSuchMethodException {
            System.out.println("\n=== Metrics Exception Handler ===");
            
            MetricsAsyncExceptionHandler handler = new MetricsAsyncExceptionHandler();
            
            Method method1 = AsyncService.class.getMethod("voidAsyncMethod", String.class);
            Method method2 = AsyncService.class.getMethod("futureAsyncMethod", String.class);
            
            // Simulate multiple exceptions
            handler.handleUncaughtException(new RuntimeException("Error 1"), method1, "p1");
            handler.handleUncaughtException(new RuntimeException("Error 2"), method1, "p2");
            handler.handleUncaughtException(new IllegalStateException("Error 3"), method2, "p3");
            handler.handleUncaughtException(new RuntimeException("Error 4"), method1, "p4");
            
            handler.printMetrics();
        }
        
        public void demonstrateCompositeHandler() throws NoSuchMethodException {
            System.out.println("\n=== Composite Exception Handler ===");
            
            CompositeAsyncExceptionHandler handler = new CompositeAsyncExceptionHandler(
                new LoggingAsyncExceptionHandler(),
                new MetricsAsyncExceptionHandler(),
                new NotificationAsyncExceptionHandler()
            );
            
            Method method = AsyncService.class.getMethod("voidAsyncMethod", String.class);
            RuntimeException exception = new RuntimeException("Critical error");
            
            System.out.println("Handling exception with composite handler:");
            handler.handleUncaughtException(exception, method, "critical-param");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Async Exception Handler Pattern - Handle Async Exceptions");
        System.out.println("===========================================================");
        
        AsyncExceptionHandlerExamples examples = new AsyncExceptionHandlerExamples();
        
        examples.demonstrateBasicHandler();
        examples.demonstrateDetailedHandler();
        examples.demonstrateMetricsHandler();
        examples.demonstrateCompositeHandler();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("AsyncUncaughtExceptionHandler:");
        System.out.println("- Handles exceptions from @Async void methods");
        System.out.println("- Provides method and parameter information");
        System.out.println("- Centralized error handling");
        
        System.out.println("\nApplies To:");
        System.out.println("✓ @Async void methods");
        System.out.println("✗ @Async Future<T> methods (use Future.get())");
        System.out.println("✗ @Async CompletableFuture<T> (use exceptionally())");
        
        System.out.println("\nCommon Implementations:");
        System.out.println("- Logging handler");
        System.out.println("- Notification handler");
        System.out.println("- Metrics handler");
        System.out.println("- Composite handler");
        
        System.out.println("\nBest Practices:");
        System.out.println("- Log complete exception details");
        System.out.println("- Include method and parameters");
        System.out.println("- Don't throw from handler");
        System.out.println("- Consider monitoring/alerting");
    }
}
