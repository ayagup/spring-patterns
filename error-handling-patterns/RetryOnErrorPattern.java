package com.example.errorhandling.retry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.*;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

/**
 * Retry on Error Pattern
 * 
 * Automatic retry mechanism for transient failures.
 * Uses Spring Retry for declarative retry logic.
 * 
 * Dependencies:
 * - spring-retry
 * - spring-aspects
 */
@SpringBootApplication
@EnableRetry
public class RetryOnErrorPattern {

    public static void main(String[] args) {
        SpringApplication.run(RetryOnErrorPattern.class, args);
    }

    /**
     * Service with retry logic
     */
    @Service
    public static class RetryService {

        @Retryable(
            value = {TransientException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
        )
        public String performOperationWithRetry() {
            System.out.println("Attempting operation...");
            if (Math.random() > 0.7) {
                throw new TransientException("Temporary failure");
            }
            return "Success";
        }

        @Recover
        public String recover(TransientException ex) {
            return "Recovered after max attempts: " + ex.getMessage();
        }

        @Retryable(
            value = {NetworkException.class},
            maxAttemptsExpression = "${retry.max-attempts:5}",
            backoff = @Backoff(
                delayExpression = "${retry.delay:2000}",
                multiplierExpression = "${retry.multiplier:2.0}"
            )
        )
        public String performWithExponentialBackoff() {
            System.out.println("Attempting with exponential backoff...");
            if (Math.random() > 0.8) {
                throw new NetworkException("Network error");
            }
            return "Network operation successful";
        }
    }

    /**
     * Manual RetryTemplate usage
     */
    @Service
    public static class RetryTemplateService {

        public String executeWithRetryTemplate() {
            RetryTemplate retryTemplate = new RetryTemplate();
            
            // Configure retry policy
            SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
            retryPolicy.setMaxAttempts(3);
            retryTemplate.setRetryPolicy(retryPolicy);
            
            // Configure backoff policy
            FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
            backOffPolicy.setBackOffPeriod(2000L);
            retryTemplate.setBackOffPolicy(backOffPolicy);
            
            return retryTemplate.execute(context -> {
                System.out.println("Attempt: " + (context.getRetryCount() + 1));
                if (Math.random() > 0.7) {
                    throw new TransientException("Retry needed");
                }
                return "Success";
            });
        }
    }

    public static class TransientException extends RuntimeException {
        public TransientException(String message) { super(message); }
    }

    public static class NetworkException extends RuntimeException {
        public NetworkException(String message) { super(message); }
    }

    @RestController
    @RequestMapping("/api/retry")
    public static class RetryController {

        private final RetryService retryService;

        public RetryController(RetryService retryService) {
            this.retryService = retryService;
        }

        @GetMapping("/test")
        public String testRetry() {
            return retryService.performOperationWithRetry();
        }
    }
}
