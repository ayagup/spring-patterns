package com.example.aspectlogging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.util.*;

/**
 * Aspect-based Logging Pattern
 * 
 * Demonstrates cross-cutting logging concerns using Spring AOP:
 * - Method execution logging
 * - Performance timing
 * - Exception logging
 * - Parameter/return value logging
 * - Custom annotation-based logging
 * 
 * Benefits:
 * - Centralized logging logic
 * - Non-invasive (no code pollution)
 * - Reusable across components
 * - Easy to enable/disable
 * 
 * Use Cases:
 * - Service method tracing
 * - Performance monitoring
 * - Audit logging
 * - Debug assistance
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class AspectBasedLoggingPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(AspectBasedLoggingPattern.class, args);
    }
}

/**
 * Custom annotation to mark methods for logging
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface LogExecution {
    String value() default "";
    boolean logArgs() default true;
    boolean logResult() default true;
    boolean logExecutionTime() default true;
}

/**
 * Aspect for method execution logging
 */
@Aspect
@Component
class LoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    
    /**
     * Log all controller methods
     */
    @Around("execution(* com.example.aspectlogging.*Controller.*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        logger.info("→ Entering: {}.{}", className, methodName);
        
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            
            logger.info("← Exiting: {}.{} [{}ms]", className, methodName, duration);
            return result;
            
        } catch (Exception e) {
            logger.error("✗ Exception in: {}.{} - {}", className, methodName, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Log all service methods with @LogExecution annotation
     */
    @Around("@annotation(logExecution)")
    public Object logAnnotatedMethods(ProceedingJoinPoint joinPoint, LogExecution logExecution) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // Log method entry
        if (logExecution.logArgs()) {
            Object[] args = joinPoint.getArgs();
            logger.debug("→ {}.{} with args: {}", className, methodName, Arrays.toString(args));
        } else {
            logger.debug("→ {}.{}", className, methodName);
        }
        
        long start = System.currentTimeMillis();
        Object result = null;
        
        try {
            result = joinPoint.proceed();
            return result;
            
        } catch (Exception e) {
            logger.error("✗ Exception in {}.{}: {}", className, methodName, e.getMessage(), e);
            throw e;
            
        } finally {
            long duration = System.currentTimeMillis() - start;
            
            if (logExecution.logExecutionTime()) {
                logger.debug("← {}.{} completed in {}ms", className, methodName, duration);
            }
            
            if (logExecution.logResult() && result != null) {
                logger.debug("← {}.{} returned: {}", className, methodName, result);
            }
        }
    }
    
    /**
     * Log all exceptions thrown by service methods
     */
    @AfterThrowing(pointcut = "execution(* com.example.aspectlogging.*Service.*(..))", throwing = "ex")
    public void logServiceExceptions(Exception ex) {
        logger.error("Service exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
    }
    
    /**
     * Log before repository methods (if any)
     */
    @Before("execution(* com.example.aspectlogging.*Repository.*(..))")
    public void logBeforeRepository(org.aspectj.lang.JoinPoint joinPoint) {
        logger.debug("Repository operation: {}", joinPoint.getSignature().getName());
    }
}

/**
 * Service demonstrating aspect-based logging
 */
@Service
class BusinessService {
    
    @LogExecution(value = "User creation", logArgs = true, logResult = true)
    public Map<String, Object> createUser(String username, String email) {
        // Simulate business logic
        return Map.of(
            "id", UUID.randomUUID().toString(),
            "username", username,
            "email", email,
            "created", System.currentTimeMillis()
        );
    }
    
    @LogExecution(value = "User retrieval", logExecutionTime = true)
    public Map<String, Object> getUser(String userId) {
        // Simulate database lookup
        return Map.of(
            "id", userId,
            "username", "john_doe",
            "email", "john@example.com"
        );
    }
    
    @LogExecution(value = "Order processing")
    public String processOrder(String orderId, double amount) throws Exception {
        if (amount < 0) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }
        
        // Simulate processing
        Thread.sleep(100);
        return "ORDER_" + orderId + "_PROCESSED";
    }
    
    public void methodWithoutLogging() {
        // This method won't be logged by annotation aspect
        // but will be logged by service exception aspect if it throws
    }
}

/**
 * REST Controller (all methods automatically logged)
 */
@RestController
@RequestMapping("/api/aspect-logging")
class AspectLoggingController {
    
    private final BusinessService businessService;
    
    public AspectLoggingController(BusinessService businessService) {
        this.businessService = businessService;
    }
    
    /**
     * Create user (demonstrates arg and result logging)
     */
    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, String> request) {
        return businessService.createUser(
            request.get("username"),
            request.get("email")
        );
    }
    
    /**
     * Get user (demonstrates execution time logging)
     */
    @GetMapping("/users/{userId}")
    public Map<String, Object> getUser(@PathVariable String userId) {
        return businessService.getUser(userId);
    }
    
    /**
     * Process order (demonstrates exception logging)
     */
    @PostMapping("/orders/{orderId}")
    public Map<String, String> processOrder(
            @PathVariable String orderId,
            @RequestParam double amount) throws Exception {
        
        String result = businessService.processOrder(orderId, amount);
        return Map.of("status", result);
    }
    
    /**
     * Test endpoint without service call
     */
    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("status", "pong");
    }
}

/**
 * Performance monitoring aspect
 */
@Aspect
@Component
class PerformanceLoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceLoggingAspect.class);
    private static final long SLOW_THRESHOLD_MS = 1000;
    
    /**
     * Log slow methods
     */
    @Around("execution(* com.example.aspectlogging..*(..))")
    public Object logSlowMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;
        
        if (duration > SLOW_THRESHOLD_MS) {
            logger.warn("⚠ SLOW METHOD: {}.{} took {}ms (threshold: {}ms)",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                duration,
                SLOW_THRESHOLD_MS);
        }
        
        return result;
    }
}
