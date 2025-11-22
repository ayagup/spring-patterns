I'll create a comprehensive Spring Boot project demonstrating all resilience patterns using Resilience4j and other modern frameworks.

## Project Structure

```xml pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.resilience</groupId>
    <artifactId>resilience-patterns</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <properties>
        <java.version>17</java.version>
        <resilience4j.version>2.1.0</resilience4j.version>
    </properties>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>

        <!-- Resilience4j -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot3</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-circuitbreaker</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-ratelimiter</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-retry</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-bulkhead</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-timelimiter</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-cache</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>

        <!-- Micrometer for metrics -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Main Application

```java src/main/java/com/resilience/ResiliencePatternsApplication.java
package com.resilience;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class ResiliencePatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResiliencePatternsApplication.class, args);
    }
}
```

## 1. Circuit Breaker Pattern

```java src/main/java/com/resilience/service/CircuitBreakerService.java
package com.resilience.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Circuit Breaker Pattern using Resilience4j
 * Prevents cascading failures by stopping calls to failing services
 */
@Service
@Slf4j
public class CircuitBreakerService {

    private final Random random = new Random();

    /**
     * Circuit Breaker with fallback
     */
    @CircuitBreaker(name = "externalService", fallbackMethod = "fallbackGetData")
    public String getData(String id) {
        log.info("Calling external service for ID: {}", id);
        
        // Simulate external service call with random failures
        if (random.nextDouble() < 0.5) {
            throw new RuntimeException("External service unavailable");
        }
        
        return "Data from external service: " + id;
    }

    /**
     * Fallback method when circuit is open
     */
    private String fallbackGetData(String id, Throwable t) {
        log.warn("Circuit breaker activated. Fallback for ID: {}", id);
        return "Fallback data for: " + id;
    }

    /**
     * Circuit breaker for payment processing
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackProcessPayment")
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing payment: {}", request.getAmount());
        
        // Simulate payment processing
        if (random.nextDouble() < 0.3) {
            throw new RuntimeException("Payment gateway error");
        }
        
        return PaymentResult.builder()
            .success(true)
            .transactionId("TXN-" + System.currentTimeMillis())
            .message("Payment processed successfully")
            .build();
    }

    private PaymentResult fallbackProcessPayment(PaymentRequest request, Throwable t) {
        log.error("Payment processing failed, using fallback", t);
        return PaymentResult.builder()
            .success(false)
            .message("Payment processing temporarily unavailable. Please try again later.")
            .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentRequest {
        private String customerId;
        private Double amount;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentResult {
        private boolean success;
        private String transactionId;
        private String message;
    }
}
```

## 2. Fallback Pattern

```java src/main/java/com/resilience/service/FallbackService.java
package com.resilience.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Fallback Pattern - Provide alternative response when primary fails
 */
@Service
@Slf4j
public class FallbackService {

    /**
     * Primary method with multiple fallback strategies
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserFromCache")
    public User getUser(String userId) {
        log.info("Fetching user from database: {}", userId);
        
        // Simulate database failure
        throw new RuntimeException("Database connection failed");
    }

    /**
     * Level 1 Fallback - Try cache
     */
    private User fallbackGetUserFromCache(String userId, Throwable t) {
        log.warn("Database failed, trying cache for user: {}", userId);
        
        try {
            return getUserFromCache(userId);
        } catch (Exception e) {
            throw new RuntimeException("Cache also failed", e);
        }
    }

    /**
     * Level 2 Fallback - Return default user
     */
    @CircuitBreaker(name = "cacheService", fallbackMethod = "fallbackGetDefaultUser")
    private User getUserFromCache(String userId) {
        log.info("Fetching user from cache: {}", userId);
        // Simulate cache failure
        throw new RuntimeException("Cache unavailable");
    }

    /**
     * Final fallback - Return safe default
     */
    private User fallbackGetDefaultUser(String userId, Throwable t) {
        log.warn("All systems failed, returning default user");
        return User.builder()
            .id(userId)
            .name("Guest User")
            .email("guest@example.com")
            .roles(Arrays.asList("GUEST"))
            .build();
    }

    /**
     * Graceful degradation - return partial data
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetProductsPartial")
    public List<Product> getProducts() {
        log.info("Fetching full product catalog");
        throw new RuntimeException("Product service unavailable");
    }

    /**
     * Fallback with degraded functionality
     */
    private List<Product> fallbackGetProductsPartial(Throwable t) {
        log.warn("Product service down, returning cached popular products");
        return Arrays.asList(
            Product.builder().id("1").name("Popular Product 1").available(true).build(),
            Product.builder().id("2").name("Popular Product 2").available(true).build()
        );
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class User {
        private String id;
        private String name;
        private String email;
        private List<String> roles;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Product {
        private String id;
        private String name;
        private boolean available;
    }
}
```

## 3. Timeout Pattern

```java src/main/java/com/resilience/service/TimeoutService.java
package com.resilience.service;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Timeout Pattern - Prevent indefinite waiting
 */
@Service
@Slf4j
public class TimeoutService {

    /**
     * Synchronous timeout with fallback
     */
    @TimeLimiter(name = "apiCall", fallbackMethod = "fallbackApiCall")
    public CompletableFuture<String> callExternalApi(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Calling external API: {}", endpoint);
            
            try {
                // Simulate slow API call
                Thread.sleep(5000);
                return "API Response from: " + endpoint;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("API call interrupted", e);
            }
        });
    }

    private CompletableFuture<String> fallbackApiCall(String endpoint, TimeoutException e) {
        log.warn("API call timed out for: {}", endpoint);
        return CompletableFuture.completedFuture("Cached response for: " + endpoint);
    }

    /**
     * Database query with timeout
     */
    @TimeLimiter(name = "databaseQuery", fallbackMethod = "fallbackDatabaseQuery")
    public CompletableFuture<QueryResult> executeQuery(String query) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Executing database query: {}", query);
            
            try {
                // Simulate long-running query
                Thread.sleep(3000);
                return QueryResult.builder()
                    .success(true)
                    .data("Query results")
                    .executionTime(3000L)
                    .build();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Query interrupted", e);
            }
        });
    }

    private CompletableFuture<QueryResult> fallbackDatabaseQuery(String query, TimeoutException e) {
        log.warn("Database query timed out: {}", query);
        return CompletableFuture.completedFuture(
            QueryResult.builder()
                .success(false)
                .message("Query timed out - please try with more specific criteria")
                .build()
        );
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QueryResult {
        private boolean success;
        private String data;
        private Long executionTime;
        private String message;
    }
}
```

## 4. Retry Pattern

```java src/main/java/com/resilience/service/RetryService.java
package com.resilience.service;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Retry Pattern - Automatically retry failed operations
 */
@Service
@Slf4j
public class RetryService {

    private final Random random = new Random();
    private int attemptCount = 0;

    /**
     * Simple retry with exponential backoff
     */
    @Retry(name = "simpleRetry", fallbackMethod = "fallbackAfterRetries")
    public String fetchDataWithRetry(String id) {
        attemptCount++;
        log.info("Attempt {}: Fetching data for ID: {}", attemptCount, id);
        
        // Simulate transient failure
        if (attemptCount < 3) {
            throw new RuntimeException("Transient network error");
        }
        
        attemptCount = 0; // Reset for next call
        return "Data successfully fetched for: " + id;
    }

    private String fallbackAfterRetries(String id, Throwable t) {
        log.error("All retry attempts failed for ID: {}", id);
        attemptCount = 0;
        return "Unable to fetch data after retries. Using cached data for: " + id;
    }

    /**
     * Retry with custom conditions
     */
    @Retry(name = "conditionalRetry", fallbackMethod = "fallbackConditionalRetry")
    public OrderStatus checkOrderStatus(String orderId) {
        log.info("Checking order status: {}", orderId);
        
        double rand = random.nextDouble();
        
        if (rand < 0.3) {
            // Retryable error
            throw new TransientException("Service temporarily unavailable");
        } else if (rand < 0.4) {
            // Non-retryable error
            throw new PermanentException("Order not found");
        }
        
        return OrderStatus.builder()
            .orderId(orderId)
            .status("DELIVERED")
            .message("Order delivered successfully")
            .build();
    }

    private OrderStatus fallbackConditionalRetry(String orderId, Throwable t) {
        log.error("Failed to check order status: {}", orderId, t);
        return OrderStatus.builder()
            .orderId(orderId)
            .status("UNKNOWN")
            .message("Unable to determine order status")
            .build();
    }

    /**
     * Retry for external API calls
     */
    @Retry(name = "apiRetry", fallbackMethod = "fallbackApiRetry")
    public ApiResponse callThirdPartyApi(String endpoint, String payload) {
        log.info("Calling third-party API: {}", endpoint);
        
        // Simulate random failures
        if (random.nextDouble() < 0.6) {
            throw new RuntimeException("API error: 503 Service Unavailable");
        }
        
        return ApiResponse.builder()
            .success(true)
            .data("API response data")
            .statusCode(200)
            .build();
    }

    private ApiResponse fallbackApiRetry(String endpoint, String payload, Throwable t) {
        log.error("API call failed after retries: {}", endpoint);
        return ApiResponse.builder()
            .success(false)
            .message("Third-party service unavailable")
            .statusCode(503)
            .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderStatus {
        private String orderId;
        private String status;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApiResponse {
        private boolean success;
        private String data;
        private int statusCode;
        private String message;
    }

    public static class TransientException extends RuntimeException {
        public TransientException(String message) {
            super(message);
        }
    }

    public static class PermanentException extends RuntimeException {
        public PermanentException(String message) {
            super(message);
        }
    }
}
```

## 5. Rate Limiter Pattern

```java src/main/java/com/resilience/service/RateLimiterService.java
package com.resilience.service;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Rate Limiter Pattern - Control the rate of requests
 */
@Service
@Slf4j
public class RateLimiterService {

    /**
     * Rate limited API endpoint
     */
    @RateLimiter(name = "apiRateLimiter", fallbackMethod = "fallbackRateLimited")
    public ApiResult callApi(String userId) {
        log.info("API called by user: {}", userId);
        
        return ApiResult.builder()
            .requestId(UUID.randomUUID().toString())
            .userId(userId)
            .timestamp(LocalDateTime.now())
            .data("API response data")
            .build();
    }

    private ApiResult fallbackRateLimited(String userId, RequestNotPermitted e) {
        log.warn("Rate limit exceeded for user: {}", userId);
        return ApiResult.builder()
            .requestId(UUID.randomUUID().toString())
            .userId(userId)
            .timestamp(LocalDateTime.now())
            .error("Rate limit exceeded. Please try again later.")
            .build();
    }

    /**
     * Rate limited payment processing
     */
    @RateLimiter(name = "paymentRateLimiter", fallbackMethod = "fallbackPaymentRateLimited")
    public PaymentResult processPayment(String customerId, double amount) {
        log.info("Processing payment for customer: {}, amount: {}", customerId, amount);
        
        return PaymentResult.builder()
            .success(true)
            .transactionId("TXN-" + System.currentTimeMillis())
            .amount(amount)
            .message("Payment processed successfully")
            .build();
    }

    private PaymentResult fallbackPaymentRateLimited(String customerId, double amount, RequestNotPermitted e) {
        log.warn("Payment rate limit exceeded for customer: {}", customerId);
        return PaymentResult.builder()
            .success(false)
            .amount(amount)
            .message("Too many payment attempts. Please wait before trying again.")
            .build();
    }

    /**
     * Rate limited resource-intensive operation
     */
    @RateLimiter(name = "reportRateLimiter", fallbackMethod = "fallbackReportRateLimited")
    public ReportResult generateReport(String reportType, String userId) {
        log.info("Generating {} report for user: {}", reportType, userId);
        
        // Simulate resource-intensive operation
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return ReportResult.builder()
            .reportId(UUID.randomUUID().toString())
            .reportType(reportType)
            .status("COMPLETED")
            .url("/reports/" + UUID.randomUUID())
            .build();
    }

    private ReportResult fallbackReportRateLimited(String reportType, String userId, RequestNotPermitted e) {
        log.warn("Report generation rate limit exceeded for user: {}", userId);
        return ReportResult.builder()
            .reportType(reportType)
            .status("RATE_LIMITED")
            .message("Report generation limit exceeded. Maximum 5 reports per minute.")
            .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApiResult {
        private String requestId;
        private String userId;
        private LocalDateTime timestamp;
        private String data;
        private String error;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentResult {
        private boolean success;
        private String transactionId;
        private double amount;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReportResult {
        private String reportId;
        private String reportType;
        private String status;
        private String url;
        private String message;
    }
}
```

## 6. Bulkhead Isolation Pattern

```java src/main/java/com/resilience/service/BulkheadService.java
package com.resilience.service;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Bulkhead Isolation Pattern - Isolate resources to prevent cascading failures
 */
@Service
@Slf4j
public class BulkheadService {

    /**
     * Semaphore-based bulkhead for synchronous calls
     */
    @Bulkhead(name = "databaseBulkhead", type = Bulkhead.Type.SEMAPHORE, 
              fallbackMethod = "fallbackDatabaseOperation")
    public String performDatabaseOperation(String query) {
        log.info("Executing database query: {}", query);
        
        try {
            // Simulate database operation
            Thread.sleep(2000);
            return "Query result for: " + query;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Database operation interrupted", e);
        }
    }

    private String fallbackDatabaseOperation(String query, BulkheadFullException e) {
        log.warn("Database bulkhead full for query: {}", query);
        return "Database busy. Please try again later.";
    }

    /**
     * Thread pool-based bulkhead for async operations
     */
    @Bulkhead(name = "fileProcessingBulkhead", type = Bulkhead.Type.THREADPOOL,
              fallbackMethod = "fallbackFileProcessing")
    public String processFile(String fileName) {
        log.info("Processing file: {}", fileName);
        
        try {
            // Simulate file processing
            Thread.sleep(3000);
            return "File processed: " + fileName;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("File processing interrupted", e);
        }
    }

    private String fallbackFileProcessing(String fileName, BulkheadFullException e) {
        log.warn("File processing bulkhead full for: {}", fileName);
        return "File processing queue full. Your request has been queued.";
    }

    /**
     * Isolated external service calls
     */
    @Bulkhead(name = "externalServiceBulkhead", type = Bulkhead.Type.SEMAPHORE,
              fallbackMethod = "fallbackExternalService")
    public ServiceResponse callExternalService(String serviceId, String request) {
        log.info("Calling external service: {}", serviceId);
        
        try {
            Thread.sleep(1500);
            return ServiceResponse.builder()
                .success(true)
                .data("Response from service: " + serviceId)
                .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Service call interrupted", e);
        }
    }

    private ServiceResponse fallbackExternalService(String serviceId, String request, BulkheadFullException e) {
        log.warn("External service bulkhead full for: {}", serviceId);
        return ServiceResponse.builder()
            .success(false)
            .message("Service temporarily unavailable due to high load")
            .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ServiceResponse {
        private boolean success;
        private String data;
        private String message;
    }
}
```

## 7. Health Check Pattern

```java src/main/java/com/resilience/health/CustomHealthIndicator.java
package com.resilience.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Health Check Pattern - Monitor service health
 */
@Component
@Slf4j
public class CustomHealthIndicator implements HealthIndicator {

    private final Random random = new Random();

    @Override
    public Health health() {
        boolean healthy = checkDatabaseConnection() && 
                         checkExternalServiceConnection() &&
                         checkDiskSpace();

        if (healthy) {
            return Health.up()
                .withDetail("database", "Connected")
                .withDetail("externalService", "Available")
                .withDetail("diskSpace", "Sufficient")
                .build();
        } else {
            return Health.down()
                .withDetail("database", "Connection issues")
                .withDetail("externalService", "Unavailable")
                .build();
        }
    }

    private boolean checkDatabaseConnection() {
        // Simulate database health check
        return random.nextDouble() > 0.1;
    }

    private boolean checkExternalServiceConnection() {
        // Simulate external service health check
        return random.nextDouble() > 0.2;
    }

    private boolean checkDiskSpace() {
        // Simulate disk space check
        return random.nextDouble() > 0.05;
    }
}
```

```java src/main/java/com/resilience/health/DatabaseHealthIndicator.java
package com.resilience.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Database-specific health check
 */
@Component("database")
@Slf4j
public class DatabaseHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Simulate database ping
            int responseTime = pingDatabase();
            
            if (responseTime < 100) {
                return Health.up()
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("status", "HEALTHY")
                    .build();
            } else if (responseTime < 500) {
                return Health.up()
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("status", "DEGRADED")
                    .build();
            } else {
                return Health.down()
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("status", "SLOW")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    private int pingDatabase() {
        // Simulate database ping
        return (int) (Math.random() * 1000);
    }
}
```

## 8. Graceful Degradation Pattern

```java src/main/java/com/resilience/service/GracefulDegradationService.java
package com.resilience.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Graceful Degradation Pattern - Reduce functionality instead of complete failure
 */
@Service
@Slf4j
public class GracefulDegradationService {

    /**
     * Full-featured product search with graceful degradation
     */
    @CircuitBreaker(name = "productSearch", fallbackMethod = "degradedProductSearch")
    public SearchResult searchProducts(String query, SearchOptions options) {
        log.info("Full product search for: {}", query);
        
        // Simulate full search with all features
        throw new RuntimeException("Search service unavailable");
    }

```java src/main/java/com/resilience/service/GracefulDegradationService.java
    /**
     * Degraded search - basic functionality only
     */
    private SearchResult degradedProductSearch(String query, SearchOptions options, Throwable t) {
        log.warn("Using degraded product search");
        
        // Return cached popular products instead
        return SearchResult.builder()
            .query(query)
            .results(Arrays.asList(
                ProductSummary.builder().id("1").name("Popular Product 1").build(),
                ProductSummary.builder().id("2").name("Popular Product 2").build()
            ))
            .degraded(true)
            .message("Showing popular products. Full search temporarily unavailable.")
            .build();
    }

    /**
     * Recommendation engine with degradation levels
     */
    @CircuitBreaker(name = "recommendations", fallbackMethod = "degradedRecommendations")
    public List<Product> getPersonalizedRecommendations(String userId) {
        log.info("Getting personalized recommendations for: {}", userId);
        
        // Simulate ML-based recommendations
        throw new RuntimeException("Recommendation engine unavailable");
    }

    /**
     * Level 1 degradation - Use simpler algorithm
     */
    private List<Product> degradedRecommendations(String userId, Throwable t) {
        log.warn("Using degraded recommendations");
        
        try {
            return getPopularProducts();
        } catch (Exception e) {
            return getStaticRecommendations(userId, e);
        }
    }

    /**
     * Level 2 degradation - Static recommendations
     */
    @CircuitBreaker(name = "popularProducts", fallbackMethod = "getStaticRecommendations")
    private List<Product> getPopularProducts() {
        log.info("Getting popular products");
        throw new RuntimeException("Popular products cache unavailable");
    }

    /**
     * Final degradation - Return static list
     */
    private List<Product> getStaticRecommendations(String userId, Throwable t) {
        log.warn("Using static recommendations");
        return Arrays.asList(
            Product.builder().id("default-1").name("Featured Product").build()
        );
    }

    /**
     * Image service with quality degradation
     */
    @CircuitBreaker(name = "imageService", fallbackMethod = "degradedImageService")
    public ImageResponse getProductImage(String productId, ImageQuality quality) {
        log.info("Getting {} image for product: {}", quality, productId);
        
        throw new RuntimeException("Image service unavailable");
    }

    private ImageResponse degradedImageService(String productId, ImageQuality quality, Throwable t) {
        log.warn("Image service degraded for product: {}", productId);
        
        // Degrade to lower quality or placeholder
        return ImageResponse.builder()
            .url("/images/placeholder.jpg")
            .quality(ImageQuality.LOW)
            .degraded(true)
            .message("High quality images temporarily unavailable")
            .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchResult {
        private String query;
        private List<ProductSummary> results;
        private boolean degraded;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProductSummary {
        private String id;
        private String name;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchOptions {
        private String category;
        private String sortBy;
        private Integer maxResults;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Product {
        private String id;
        private String name;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ImageResponse {
        private String url;
        private ImageQuality quality;
        private boolean degraded;
        private String message;
    }

    public enum ImageQuality {
        HIGH, MEDIUM, LOW
    }
}
```

## 9. Fail Fast Pattern

```java src/main/java/com/resilience/service/FailFastService.java
package com.resilience.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Fail Fast Pattern - Validate early and fail immediately
 */
@Service
@Slf4j
public class FailFastService {

    /**
     * Fail fast validation
     */
    public OrderResult processOrder(OrderRequest request) {
        log.info("Processing order: {}", request.getOrderId());
        
        // Fail fast - validate immediately
        validateOrder(request);
        
        // Process order
        return OrderResult.builder()
            .orderId(request.getOrderId())
            .status("PROCESSING")
            .message("Order accepted")
            .build();
    }

    private void validateOrder(OrderRequest request) {
        // Fail fast validations
        if (request.getOrderId() == null || request.getOrderId().isEmpty()) {
            throw new ValidationException("Order ID is required");
        }
        
        if (request.getCustomerId() == null || request.getCustomerId().isEmpty()) {
            throw new ValidationException("Customer ID is required");
        }
        
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ValidationException("Order must contain at least one item");
        }
        
        if (request.getTotalAmount() <= 0) {
            throw new ValidationException("Total amount must be greater than zero");
        }
        
        // Fail fast - check prerequisites
        if (!isCustomerActive(request.getCustomerId())) {
            throw new ValidationException("Customer account is not active");
        }
        
        if (!hasPaymentMethod(request.getCustomerId())) {
            throw new ValidationException("No payment method on file");
        }
    }

    /**
     * API call with fail fast
     */
    public ApiCallResult callApi(ApiRequest request) {
        log.info("API call: {}", request.getEndpoint());
        
        // Fail fast - check preconditions
        if (!isServiceAvailable(request.getServiceName())) {
            throw new ServiceUnavailableException(
                "Service " + request.getServiceName() + " is not available"
            );
        }
        
        if (!hasValidApiKey(request.getApiKey())) {
            throw new AuthenticationException("Invalid API key");
        }
        
        if (isRateLimitExceeded(request.getApiKey())) {
            throw new RateLimitException("Rate limit exceeded");
        }
        
        // Proceed with API call
        return ApiCallResult.builder()
            .success(true)
            .response("API response")
            .build();
    }

    private boolean isCustomerActive(String customerId) {
        return true; // Simulate check
    }

    private boolean hasPaymentMethod(String customerId) {
        return true; // Simulate check
    }

    private boolean isServiceAvailable(String serviceName) {
        return true; // Simulate check
    }

    private boolean hasValidApiKey(String apiKey) {
        return apiKey != null && !apiKey.isEmpty();
    }

    private boolean isRateLimitExceeded(String apiKey) {
        return false; // Simulate check
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderRequest {
        private String orderId;
        private String customerId;
        private java.util.List<String> items;
        private double totalAmount;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderResult {
        private String orderId;
        private String status;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApiRequest {
        private String serviceName;
        private String endpoint;
        private String apiKey;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApiCallResult {
        private boolean success;
        private String response;
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }

    public static class RateLimitException extends RuntimeException {
        public RateLimitException(String message) {
            super(message);
        }
    }
}
```

## 10. Fail Safe Pattern

```java src/main/java/com/resilience/service/FailSafeService.java
package com.resilience.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Fail Safe Pattern - Continue operating even when errors occur
 */
@Service
@Slf4j
public class FailSafeService {

    /**
     * Fail safe data retrieval
     */
    public List<DataItem> getData(String category) {
        try {
            return fetchDataFromDatabase(category);
        } catch (Exception e) {
            log.error("Database error, using cache", e);
            try {
                return fetchDataFromCache(category);
            } catch (Exception cacheError) {
                log.error("Cache error, using default data", cacheError);
                return getDefaultData(category);
            }
        }
    }

    private List<DataItem> fetchDataFromDatabase(String category) {
        throw new RuntimeException("Database connection failed");
    }

    private List<DataItem> fetchDataFromCache(String category) {
        throw new RuntimeException("Cache unavailable");
    }

    private List<DataItem> getDefaultData(String category) {
        // Always succeed - return safe default
        return Collections.singletonList(
            DataItem.builder()
                .id("default")
                .name("Default Item")
                .category(category)
                .build()
        );
    }

    /**
     * Fail safe logging
     */
    public void logEvent(LogEvent event) {
        try {
            logToDatabase(event);
        } catch (Exception e) {
            try {
                logToFile(event);
            } catch (Exception fileError) {
                // Last resort - log to console
                System.err.println("Failed to log event: " + event);
            }
        }
    }

    private void logToDatabase(LogEvent event) {
        throw new RuntimeException("Database logging failed");
    }

    private void logToFile(LogEvent event) {
        log.info("Logged to file: {}", event);
    }

    /**
     * Fail safe notification
     */
    public void sendNotification(Notification notification) {
        boolean sent = false;
        
        // Try email
        if (!sent) {
            try {
                sendEmail(notification);
                sent = true;
            } catch (Exception e) {
                log.warn("Email failed, trying SMS", e);
            }
        }
        
        // Fallback to SMS
        if (!sent) {
            try {
                sendSms(notification);
                sent = true;
            } catch (Exception e) {
                log.warn("SMS failed, trying push notification", e);
            }
        }
        
        // Fallback to push notification
        if (!sent) {
            try {
                sendPushNotification(notification);
                sent = true;
            } catch (Exception e) {
                log.error("All notification methods failed, queuing for retry", e);
                queueForRetry(notification);
            }
        }
    }

    private void sendEmail(Notification notification) {
        throw new RuntimeException("Email service unavailable");
    }

    private void sendSms(Notification notification) {
        log.info("SMS sent: {}", notification.getMessage());
    }

    private void sendPushNotification(Notification notification) {
        log.info("Push notification sent: {}", notification.getMessage());
    }

    private void queueForRetry(Notification notification) {
        log.info("Queued notification for retry: {}", notification);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DataItem {
        private String id;
        private String name;
        private String category;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LogEvent {
        private String eventType;
        private String message;
        private java.time.LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Notification {
        private String userId;
        private String message;
        private String type;
    }
}
```

## 11. Throttling Pattern

```java src/main/java/com/resilience/service/ThrottlingService.java
package com.resilience.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Throttling Pattern - Control resource consumption
 */
@Service
@Slf4j
public class ThrottlingService {

    private final Map<String, UserThrottle> userThrottles = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    /**
     * Throttled API endpoint
     */
    public ApiResponse callThrottledApi(String userId, String request) {
        if (isThrottled(userId)) {
            return ApiResponse.builder()
                .success(false)
                .message("Request throttled. Too many requests.")
                .retryAfter(calculateRetryAfter(userId))
                .build();
        }

        recordRequest(userId);
        
        // Process request
        return ApiResponse.builder()
            .success(true)
            .data("Request processed successfully")
            .build();
    }

    private boolean isThrottled(String userId) {
        UserThrottle throttle = userThrottles.computeIfAbsent(
            userId, k -> new UserThrottle()
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minus(Duration.ofMinutes(1));

        // Remove old requests
        throttle.requests.removeIf(time -> time.isBefore(windowStart));

        return throttle.requests.size() >= MAX_REQUESTS_PER_MINUTE;
    }

    private void recordRequest(String userId) {
        userThrottles.get(userId).requests.add(LocalDateTime.now());
    }

    private long calculateRetryAfter(String userId) {
        UserThrottle throttle = userThrottles.get(userId);
        if (throttle.requests.isEmpty()) {
            return 0;
        }

        LocalDateTime oldestRequest = throttle.requests.get(0);
        LocalDateTime retryTime = oldestRequest.plus(Duration.ofMinutes(1));
        
        return Duration.between(LocalDateTime.now(), retryTime).getSeconds();
    }

    /**
     * Adaptive throttling based on system load
     */
    public ProcessingResult processWithAdaptiveThrottling(String taskId) {
        double systemLoad = getSystemLoad();
        
        if (systemLoad > 0.8) {
            // High load - throttle aggressively
            throttle(500);
            log.warn("High system load, applying aggressive throttling");
        } else if (systemLoad > 0.6) {
            // Medium load - throttle moderately
            throttle(200);
            log.info("Medium system load, applying moderate throttling");
        }

        // Process task
        return ProcessingResult.builder()
            .taskId(taskId)
            .status("COMPLETED")
            .processingTime(100L)
            .build();
    }

    private double getSystemLoad() {
        // Simulate system load check
        return Math.random();
    }

    private void throttle(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class UserThrottle {
        private final java.util.List<LocalDateTime> requests = 
            new java.util.concurrent.CopyOnWriteArrayList<>();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApiResponse {
        private boolean success;
        private String data;
        private String message;
        private long retryAfter;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProcessingResult {
        private String taskId;
        private String status;
        private Long processingTime;
    }
}
```

## 12. Debouncing Pattern

```java src/main/java/com/resilience/service/DebouncingService.java
package com.resilience.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Debouncing Pattern - Delay action until activity stops
 */
@Service
@Slf4j
public class DebouncingService {

    private final Map<String, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static final long DEBOUNCE_DELAY_MS = 1000;

    /**
     * Debounced search
     */
    public void searchWithDebounce(String userId, String query) {
        String key = "search-" + userId;
        
        // Cancel previous search
        ScheduledFuture<?> existingTask = pendingTasks.get(key);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
            log.debug("Cancelled previous search for user: {}", userId);
        }

        // Schedule new search
        ScheduledFuture<?> newTask = scheduler.schedule(
            () -> executeSearch(userId, query),
            DEBOUNCE_DELAY_MS,
            TimeUnit.MILLISECONDS
        );

        pendingTasks.put(key, newTask);
        log.debug("Scheduled debounced search for user: {}", userId);
    }

    private void executeSearch(String userId, String query) {
        log.info("Executing search for user: {}, query: {}", userId, query);
        // Actual search logic here
    }

    /**
     * Debounced auto-save
     */
    public void autoSaveWithDebounce(String documentId, String content) {
        String key = "autosave-" + documentId;
        
        ScheduledFuture<?> existingTask = pendingTasks.get(key);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
        }

        ScheduledFuture<?> newTask = scheduler.schedule(
            () -> saveDocument(documentId, content),
            2000, // 2 second debounce for auto-save
            TimeUnit.MILLISECONDS
        );

        pendingTasks.put(key, newTask);
        log.debug("Scheduled auto-save for document: {}", documentId);
    }

    private void saveDocument(String documentId, String content) {
        log.info("Auto-saving document: {}", documentId);
        // Save logic here
    }

    /**
     * Debounced API call
     */
    public CompletableFuture<ApiResult> callApiWithDebounce(String endpoint, String payload) {
        String key = "api-" + endpoint;
        CompletableFuture<ApiResult> future = new CompletableFuture<>();

        ScheduledFuture<?> existingTask = pendingTasks.get(key);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
        }

        ScheduledFuture<?> newTask = scheduler.schedule(
            () -> {
                try {
                    ApiResult result = executeApiCall(endpoint, payload);
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            },
            DEBOUNCE_DELAY_MS,
            TimeUnit.MILLISECONDS
        );

        pendingTasks.put(key, newTask);
        return future;
    }

    private ApiResult executeApiCall(String endpoint, String payload) {
        log.info("Executing API call to: {}", endpoint);
        return ApiResult.builder()
            .success(true)
            .timestamp(LocalDateTime.now())
            .response("API response")
            .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApiResult {
        private boolean success;
        private LocalDateTime timestamp;
        private String response;
    }
}
```

## 13. Cache Stampede Prevention Pattern

```java src/main/java/com/resilience/service/CacheStampedePreventionService.java
package com.resilience.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cache Stampede Prevention Pattern
 * Prevent multiple simultaneous cache misses from overwhelming backend
 */
@Service
@Slf4j
public class CacheStampedePreventionService {

    private final Map<String, Lock> locks = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();

    /**
     * Prevent cache stampede using locks
     */
    public String getDataWithLock(String key) {
        // Check cache first
        CacheEntry cached = localCache.get(key);
        if (cached != null && !cached.isExpired()) {
            log.debug("Cache hit for key: {}", key);
            return cached.value;
        }

        // Get or create lock for this key
        Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());

        // Only one thread fetches data
        if (lock.tryLock()) {
            try {
                // Double-check cache
                cached = localCache.get(key);
                if (cached != null && !cached.isExpired()) {
                    return cached.value;
                }

                log.info("Cache miss, fetching data for key: {}", key);
                String data = fetchDataFromBackend(key);
                
                localCache.put(key, new CacheEntry(data, 60000)); // 60 second TTL
                return data;
            } finally {
                lock.unlock();
            }
        } else {
            // Wait for other thread to fetch
            try {
                lock.lock();
                cached = localCache.get(key);
                return cached != null ? cached.value : fetchDataFromBackend(key);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Prevent stampede using request coalescing
     */
    public CompletableFuture<String> getDataWithCoalescing(String key) {
        // Check cache
        CacheEntry cached = localCache.get(key);
        if (cached != null && !cached.isExpired()) {
            return CompletableFuture.completedFuture(cached.value);
        }

        // Check if request already pending
        CompletableFuture<String> existing = pendingRequests.get(key);
        if (existing != null) {
            log.debug("Coalescing request for key: {}", key);
            return existing;
        }

        // Create new request
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            log.info("Fetching data for key: {}", key);
            String data = fetchDataFromBackend(key);
            localCache.put(key, new CacheEntry(data, 60000));
            return data;
        });

        pendingRequests.put(key, future);
        
        // Remove from pending when complete
        future.whenComplete((result, error) -> pendingRequests.remove(key));

        return future;
    }

    /**
     * Probabilistic early expiration to prevent stampede
     */
    @Cacheable(value = "products", key = "#productId")
    public Product getProduct(String productId) {
        CacheEntry cached = localCache.get(productId);
        
        if (cached != null) {
            // Probabilistic early expiration
            long timeToExpiry = cached.expiryTime - System.currentTimeMillis();
            long ttl = 60000; // 60 seconds
            
            // Probability increases as we approach expiration
            double probability = 1.0 - (double) timeToExpiry / ttl;
            
            if (Math.random() < probability) {
                log.info("Probabilistic refresh for key: {}", productId);
                refreshCacheAsync(productId);
            }
            
            return Product.builder()
                .id(productId)
                .name(cached.value)
                .build();
        }

        // Cache miss
        return fetchProductFromBackend(productId);
    }

    private void refreshCacheAsync(String key) {
        CompletableFuture.runAsync(() -> {
            String data = fetchDataFromBackend(key);
            localCache.put(key, new CacheEntry(data, 60000));
        });
    }

    private String fetchDataFromBackend(String key) {
        // Simulate slow backend call
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Data for: " + key;
    }

    private Product fetchProductFromBackend(String productId) {
        log.info("Fetching product from backend: {}", productId);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Product product = Product.builder()
            .id(productId)
            .name("Product " + productId)
            .price(99.99)
            .build();

        localCache.put(productId, new CacheEntry(product.getName(), 60000));
        return product;
    }

    private static class CacheEntry {
        private final String value;
        private final long expiryTime;

        public CacheEntry(String value, long ttlMs) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttlMs;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Product {
        private String id;
        private String name;
        private Double price;
    }
}
```

## Controllers

```java src/main/java/com/resilience/controller/ResilienceController.java
package com.resilience.controller;

import com.resilience.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller demonstrating all resilience patterns
 */
@RestController
@RequestMapping("/api/resilience")
@RequiredArgsConstructor
@Slf4j
public class ResilienceController {

    private final CircuitBreakerService circuitBreakerService;
    private final FallbackService fallbackService;
    private final TimeoutService timeoutService;
    private final RetryService retryService;
    private final RateLimiterService rateLimiterService;
    private final BulkheadService bulkheadService;
    private final GracefulDegradationService gracefulDegradationService;
    private final FailFastService failFastService;
    private final FailSafeService failSafeService;
    private final ThrottlingService throttlingService;
    private final DebouncingService debouncingService;
    private final CacheStampedePreventionService cacheStampedePreventionService;

    /**
     * Circuit Breaker Pattern
     */
    @GetMapping("/circuit-breaker/{id}")
    public ResponseEntity<String> testCircuitBreaker(@PathVariable String id) {
        String result = circuitBreakerService.getData(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/circuit-breaker/payment")
    public ResponseEntity<CircuitBreakerService.PaymentResult> testPaymentCircuitBreaker(
            @RequestBody CircuitBreakerService.PaymentRequest request) {
        CircuitBreakerService.PaymentResult result = circuitBreakerService.processPayment(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Fallback Pattern
     */
    @GetMapping("/fallback/user/{userId}")
    public ResponseEntity<FallbackService.User> testFallback(@PathVariable String userId) {
        FallbackService.User user = fallbackService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Timeout Pattern
     */
    @GetMapping("/timeout/api/{endpoint}")
    public CompletableFuture<ResponseEntity<String>> testTimeout(@PathVariable String endpoint) {
        return timeoutService.callExternalApi(endpoint)
            .thenApply(ResponseEntity::ok);
    }

    /**
     * Retry Pattern
     */
    @GetMapping("/retry/{id}")
    public ResponseEntity<String> testRetry(@PathVariable String id) {
        String result = retryService.fetchDataWithRetry(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Rate Limiter Pattern
     */
    @GetMapping("/rate-limiter/{userId}")
    public ResponseEntity<RateLimiterService.ApiResult> testRateLimiter(@PathVariable String userId) {
        RateLimiterService.ApiResult result = rateLimiterService.callApi(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Bulkhead Pattern
     */
    @GetMapping("/bulkhead/database")
    public ResponseEntity<String> testBulkhead(@RequestParam String query) {
        String result = bulkheadService.performDatabaseOperation(query);
        return ResponseEntity.ok(result);
    }

```java src/main/java/com/resilience/controller/ResilienceController.java
    /**
     * Graceful Degradation Pattern
     */
    @GetMapping("/graceful-degradation/search")
    public ResponseEntity<GracefulDegradationService.SearchResult> testGracefulDegradation(
            @RequestParam String query) {
        GracefulDegradationService.SearchOptions options = 
            GracefulDegradationService.SearchOptions.builder().build();
        GracefulDegradationService.SearchResult result = 
            gracefulDegradationService.searchProducts(query, options);
        return ResponseEntity.ok(result);
    }

    /**
     * Fail Fast Pattern
     */
    @PostMapping("/fail-fast/order")
    public ResponseEntity<FailFastService.OrderResult> testFailFast(
            @RequestBody FailFastService.OrderRequest request) {
        FailFastService.OrderResult result = failFastService.processOrder(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Fail Safe Pattern
     */
    @GetMapping("/fail-safe/data/{category}")
    public ResponseEntity<java.util.List<FailSafeService.DataItem>> testFailSafe(
            @PathVariable String category) {
        java.util.List<FailSafeService.DataItem> data = failSafeService.getData(category);
        return ResponseEntity.ok(data);
    }

    /**
     * Throttling Pattern
     */
    @GetMapping("/throttling/{userId}")
    public ResponseEntity<ThrottlingService.ApiResponse> testThrottling(
            @PathVariable String userId,
            @RequestParam String request) {
        ThrottlingService.ApiResponse response = throttlingService.callThrottledApi(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Debouncing Pattern
     */
    @PostMapping("/debouncing/search")
    public ResponseEntity<Map<String, String>> testDebouncing(
            @RequestParam String userId,
            @RequestParam String query) {
        debouncingService.searchWithDebounce(userId, query);
        return ResponseEntity.ok(Map.of(
            "status", "search scheduled",
            "message", "Search will execute after debounce delay"
        ));
    }

    /**
     * Cache Stampede Prevention
     */
    @GetMapping("/cache-stampede/product/{productId}")
    public CompletableFuture<ResponseEntity<CacheStampedePreventionService.Product>> testCacheStampede(
            @PathVariable String productId) {
        return CompletableFuture.completedFuture(
            ResponseEntity.ok(cacheStampedePreventionService.getProduct(productId))
        );
    }

    /**
     * Combined patterns demo
     */
    @PostMapping("/combined/process")
    public ResponseEntity<Map<String, Object>> testCombinedPatterns(
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Apply multiple resilience patterns
            String userId = request.get("userId");
            String data = request.get("data");
            
            // Rate limiting
            RateLimiterService.ApiResult rateLimitResult = rateLimiterService.callApi(userId);
            response.put("rateLimiter", rateLimitResult);
            
            // Circuit breaker with retry
            String processedData = circuitBreakerService.getData(data);
            response.put("circuitBreaker", processedData);
            
            // Bulkhead isolation
            String dbResult = bulkheadService.performDatabaseOperation("SELECT * FROM data");
            response.put("bulkhead", dbResult);
            
            response.put("status", "success");
            
        } catch (Exception e) {
            log.error("Error in combined patterns", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "Resilience patterns service is running"
        ));
    }
}
```

## Configuration

```yaml src/main/resources/application.yml
spring:
  application:
    name: resilience-patterns
  
  # Redis for distributed rate limiting
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
  
  # Cache configuration
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=60s

server:
  port: 8080

# Resilience4j Configuration
resilience4j:
  # Circuit Breaker
  circuitbreaker:
    configs:
      default:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
        recordExceptions:
          - org.springframework.web.client.HttpServerErrorException
          - java.util.concurrent.TimeoutException
          - java.io.IOException
    instances:
      externalService:
        baseConfig: default
        slidingWindowSize: 20
        failureRateThreshold: 60
      paymentService:
        baseConfig: default
        waitDurationInOpenState: 30s
        failureRateThreshold: 40
      userService:
        baseConfig: default
      productService:
        baseConfig: default
      recommendations:
        baseConfig: default
      imageService:
        baseConfig: default
  
  # Retry
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 1s
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - org.springframework.web.client.HttpServerErrorException
          - java.util.concurrent.TimeoutException
    instances:
      simpleRetry:
        baseConfig: default
      conditionalRetry:
        maxAttempts: 5
        waitDuration: 500ms
        retryExceptions:
          - com.resilience.service.RetryService$TransientException
        ignoreExceptions:
          - com.resilience.service.RetryService$PermanentException
      apiRetry:
        baseConfig: default
        maxAttempts: 4
  
  # Rate Limiter
  ratelimiter:
    configs:
      default:
        registerHealthIndicator: true
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0s
        eventConsumerBufferSize: 100
    instances:
      apiRateLimiter:
        limitForPeriod: 100
        limitRefreshPeriod: 1s
      paymentRateLimiter:
        limitForPeriod: 5
        limitRefreshPeriod: 1m
      reportRateLimiter:
        limitForPeriod: 5
        limitRefreshPeriod: 1m
  
  # Bulkhead
  bulkhead:
    configs:
      default:
        maxConcurrentCalls: 10
        maxWaitDuration: 1s
    instances:
      databaseBulkhead:
        maxConcurrentCalls: 5
        maxWaitDuration: 2s
      externalServiceBulkhead:
        maxConcurrentCalls: 8
  
  # Thread Pool Bulkhead
  thread-pool-bulkhead:
    configs:
      default:
        maxThreadPoolSize: 4
        coreThreadPoolSize: 2
        queueCapacity: 100
        keepAliveDuration: 20ms
    instances:
      fileProcessingBulkhead:
        maxThreadPoolSize: 3
        coreThreadPoolSize: 1
        queueCapacity: 50
  
  # Time Limiter
  timelimiter:
    configs:
      default:
        timeoutDuration: 3s
        cancelRunningFuture: true
    instances:
      apiCall:
        timeoutDuration: 2s
      databaseQuery:
        timeoutDuration: 5s

# Management and Monitoring
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
  health:
    circuitbreakers:
      enabled: true
    ratelimiters:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
  tracing:
    sampling:
      probability: 1.0

# Logging
logging:
  level:
    com.resilience: DEBUG
    io.github.resilience4j: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## Testing

```java src/test/java/com/resilience/ResiliencePatternsTest.java
package com.resilience;

import com.resilience.service.*;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for resilience patterns
 */
@SpringBootTest
class ResiliencePatternsTest {

    @Autowired
    private CircuitBreakerService circuitBreakerService;

    @Autowired
    private RetryService retryService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    void testCircuitBreakerPattern() {
        // Should eventually succeed or return fallback
        String result = circuitBreakerService.getData("test-1");
        assertNotNull(result);
    }

    @Test
    void testRetryPattern() {
        // Should retry and eventually succeed
        String result = retryService.fetchDataWithRetry("test-2");
        assertNotNull(result);
        assertTrue(result.contains("Data") || result.contains("cached"));
    }

    @Test
    void testRateLimiterPattern() {
        String userId = "test-user";
        
        // First few requests should succeed
        for (int i = 0; i < 10; i++) {
            RateLimiterService.ApiResult result = rateLimiterService.callApi(userId);
            assertNotNull(result);
        }
        
        // Subsequent requests might be rate limited
        boolean rateLimited = false;
        for (int i = 0; i < 100; i++) {
            try {
                RateLimiterService.ApiResult result = rateLimiterService.callApi(userId);
                if (result.getError() != null && result.getError().contains("Rate limit")) {
                    rateLimited = true;
                    break;
                }
            } catch (RequestNotPermitted e) {
                rateLimited = true;
                break;
            }
        }
        
        assertTrue(rateLimited, "Should eventually hit rate limit");
    }

    @Test
    void testFailFastPattern() {
        FailFastService.OrderRequest invalidRequest = FailFastService.OrderRequest.builder()
            .orderId(null) // Invalid - should fail fast
            .customerId("test")
            .build();
        
        assertThrows(FailFastService.ValidationException.class, () -> {
            new FailFastService().processOrder(invalidRequest);
        });
    }

    @Test
    void testFailSafePattern() {
        FailSafeService service = new FailSafeService();
        
        // Should always return something, never throw
        java.util.List<FailSafeService.DataItem> result = service.getData("test-category");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testCircuitBreakerState() {
        io.github.resilience4j.circuitbreaker.CircuitBreaker cb = 
            circuitBreakerRegistry.circuitBreaker("externalService");
        
        assertEquals(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED, 
                    cb.getState());
    }
}
```

## README Documentation

```markdown README.md
# Resilience Patterns Implementation

Comprehensive implementation of 13 resilience patterns using Spring Boot and Resilience4j.

## Patterns Implemented

### 1. Circuit Breaker Pattern
- **Library**: Resilience4j
- **Purpose**: Prevent cascading failures
- **Example**: `CircuitBreakerService.java`
- States: CLOSED → OPEN → HALF_OPEN

### 2. Fallback Pattern
- **Purpose**: Provide alternative responses
- **Example**: `FallbackService.java`
- Multi-level fallbacks supported

### 3. Timeout Pattern
- **Purpose**: Prevent indefinite waiting
- **Example**: `TimeoutService.java`
- Uses `@TimeLimiter` annotation

### 4. Retry Pattern
- **Purpose**: Automatic retry on failures
- **Example**: `RetryService.java`
- Exponential backoff supported

### 5. Rate Limiter Pattern
- **Purpose**: Control request rate
- **Example**: `RateLimiterService.java`
- Configurable limits per time period

### 6. Bulkhead Isolation Pattern
- **Purpose**: Isolate resources
- **Example**: `BulkheadService.java`
- Semaphore and thread pool types

### 7. Health Check Pattern
- **Purpose**: Monitor service health
- **Example**: `CustomHealthIndicator.java`
- Actuator integration

### 8. Graceful Degradation Pattern
- **Purpose**: Reduce functionality gracefully
- **Example**: `GracefulDegradationService.java`
- Progressive degradation levels

### 9. Fail Fast Pattern
- **Purpose**: Validate early, fail immediately
- **Example**: `FailFastService.java`
- Pre-condition checking

### 10. Fail Safe Pattern
- **Purpose**: Continue operating on errors
- **Example**: `FailSafeService.java`
- Always returns safe defaults

### 11. Throttling Pattern
- **Purpose**: Control resource consumption
- **Example**: `ThrottlingService.java`
- Adaptive throttling based on load

### 12. Debouncing Pattern
- **Purpose**: Delay action until activity stops
- **Example**: `DebouncingService.java`
- Useful for search, auto-save

### 13. Cache Stampede Prevention
- **Purpose**: Prevent simultaneous cache misses
- **Example**: `CacheStampedePreventionService.java`
- Request coalescing, probabilistic expiration

## Quick Start

### Build and Run
```bash
mvn clean package
mvn spring-boot:run
```

### Start Redis (for rate limiting)
```bash
docker run -d -p 6379:6379 redis:7-alpine
```

## API Examples

### Circuit Breaker
```bash
curl http://localhost:8080/api/resilience/circuit-breaker/test-1
```

### Retry Pattern
```bash
curl http://localhost:8080/api/resilience/retry/test-2
```

### Rate Limiter
```bash
# Make multiple requests to trigger rate limit
for i in {1..15}; do
  curl http://localhost:8080/api/resilience/rate-limiter/user123
done
```

### Bulkhead
```bash
curl "http://localhost:8080/api/resilience/bulkhead/database?query=SELECT"
```

### Graceful Degradation
```bash
curl "http://localhost:8080/api/resilience/graceful-degradation/search?query=laptop"
```

### Fail Fast
```bash
curl -X POST http://localhost:8080/api/resilience/fail-fast/order \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER-123",
    "customerId": "CUST-456",
    "items": ["item1"],
    "totalAmount": 99.99
  }'
```

### Throttling
```bash
curl "http://localhost:8080/api/resilience/throttling/user123?request=test"
```

### Debouncing
```bash
curl -X POST "http://localhost:8080/api/resilience/debouncing/search?userId=user1&query=test"
```

### Cache Stampede Prevention
```bash
curl http://localhost:8080/api/resilience/cache-stampede/product/PROD-123
```

## Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Circuit Breaker Metrics
```bash
curl http://localhost:8080/actuator/circuitbreakers
curl http://localhost:8080/actuator/circuitbreakerevents
```

### Rate Limiter Metrics
```bash
curl http://localhost:8080/actuator/ratelimiters
```

### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

## Configuration

All patterns are configured in `application.yml`:

- Circuit breaker thresholds
- Retry attempts and backoff
- Rate limits
- Bulkhead sizes
- Timeout durations

## Testing Patterns

### Test Circuit Breaker State
```bash
# Make requests until circuit opens
for i in {1..50}; do
  curl http://localhost:8080/api/resilience/circuit-breaker/test
  sleep 0.1
done

# Check circuit breaker state
curl http://localhost:8080/actuator/circuitbreakers
```

### Test Rate Limiting
```bash
# Rapid requests to trigger rate limit
ab -n 100 -c 10 http://localhost:8080/api/resilience/rate-limiter/user123
```

### Test Bulkhead
```bash
# Concurrent requests to test bulkhead
for i in {1..20}; do
  curl "http://localhost:8080/api/resilience/bulkhead/database?query=SELECT" &
done
wait
```

## Pattern Combinations

The service demonstrates how to combine multiple patterns:

```java
@CircuitBreaker(name = "service")
@Retry(name = "service")
@RateLimiter(name = "service")
@Bulkhead(name = "service")
public String resilientOperation() {
    // All patterns applied
}
```

## Best Practices

1. **Circuit Breaker**: Set appropriate failure thresholds
2. **Retry**: Use exponential backoff
3. **Rate Limiter**: Configure per user/API key
4. **Bulkhead**: Size based on available resources
5. **Timeout**: Set realistic limits
6. **Fallback**: Provide meaningful alternatives
7. **Monitoring**: Track all resilience metrics

## Troubleshooting

### Circuit Breaker Not Opening
```bash
# Check configuration
curl http://localhost:8080/actuator/configprops | grep resilience4j

# Check metrics
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls
```

### Rate Limit Not Working
```bash
# Verify Redis connection
redis-cli ping

# Check rate limiter config
curl http://localhost:8080/actuator/ratelimiters
```

## License
MIT License
```

This comprehensive implementation demonstrates all 13 resilience patterns with working code, configuration, and detailed documentation!