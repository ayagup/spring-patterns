package com.example.miscellaneous.handlerexceptionresolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Handler Exception Resolver Pattern - Demonstrates Spring's exception handling
 * 
 * This pattern shows how to:
 * 1. Use @ExceptionHandler methods
 * 2. Use @ControllerAdvice for global exception handling
 * 3. Extend ResponseEntityExceptionHandler
 * 4. Implement custom HandlerExceptionResolver
 * 5. Handle specific exception types
 * 6. Return custom error responses
 * 7. Log exceptions
 * 8. Map exceptions to HTTP status codes
 * 9. Handle validation errors
 * 10. Provide user-friendly error messages
 * 
 * Key Concepts:
 * - @ExceptionHandler: Handle exceptions in specific controllers
 * - @ControllerAdvice: Global exception handling
 * - ResponseEntityExceptionHandler: Base class for exception handlers
 * - HandlerExceptionResolver: Low-level exception resolution
 * - ErrorResponse: Structured error information
 * 
 * Exception Handling Order:
 * 1. @ExceptionHandler in controller
 * 2. @ExceptionHandler in @ControllerAdvice
 * 3. HandlerExceptionResolver
 * 4. DefaultHandlerExceptionResolver
 * 5. Framework default handling
 * 
 * Dependencies:
 * - spring-webmvc
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class HandlerExceptionResolverPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(HandlerExceptionResolverPattern.class, args);
    }
}

// ============================================================================
// Custom Exceptions
// ============================================================================

/**
 * Resource not found exception
 */
class ResourceNotFoundException extends RuntimeException {
    private String resourceName;
    private String resourceId;
    
    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(resourceName + " not found with id: " + resourceId);
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }
    
    public String getResourceName() { return resourceName; }
    public String getResourceId() { return resourceId; }
}

/**
 * Validation exception
 */
class ValidationException extends RuntimeException {
    private List<String> errors;
    
    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
    
    public List<String> getErrors() { return errors; }
}

/**
 * Business logic exception
 */
class BusinessException extends RuntimeException {
    private String errorCode;
    
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() { return errorCode; }
}

/**
 * Unauthorized exception
 */
class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

// ============================================================================
// Error Response Models
// ============================================================================

/**
 * Standard error response
 */
class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
    
    // Getters and setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }
}

// ============================================================================
// Global Exception Handler
// ============================================================================

/**
 * Global exception handler using @ControllerAdvice
 */
@ControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle ValidationException
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex, HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            ex.getMessage(),
            request.getRequestURI()
        );
        error.setDetails(ex.getErrors());
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Business Error",
            ex.getMessage(),
            request.getRequestURI()
        );
        error.setDetails(Arrays.asList("Error Code: " + ex.getErrorCode()));
        
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    /**
     * Handle UnauthorizedException
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getRequestURI()
        );
        error.setDetails(Arrays.asList(ex.getMessage()));
        
        // Log exception
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

// ============================================================================
// Custom Handler Exception Resolver
// ============================================================================

/**
 * Custom implementation of HandlerExceptionResolver
 */
@org.springframework.stereotype.Component
class CustomHandlerExceptionResolver implements HandlerExceptionResolver {
    
    @Override
    public ModelAndView resolveException(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Object handler, 
                                        Exception ex) {
        
        System.out.println("[CustomResolver] Resolving exception: " + ex.getClass().getSimpleName());
        
        // Return null to pass to next resolver
        // Return ModelAndView to handle the exception
        return null;
    }
}

// ============================================================================
// Exception Service
// ============================================================================

/**
 * Service that throws various exceptions for testing
 */
@org.springframework.stereotype.Service
class ExceptionService {
    
    public void throwResourceNotFound(String resourceName, String id) {
        throw new ResourceNotFoundException(resourceName, id);
    }
    
    public void throwValidationException(List<String> errors) {
        throw new ValidationException("Validation failed", errors);
    }
    
    public void throwBusinessException(String message, String errorCode) {
        throw new BusinessException(message, errorCode);
    }
    
    public void throwUnauthorizedException(String message) {
        throw new UnauthorizedException(message);
    }
    
    public void throwIllegalArgumentException(String message) {
        throw new IllegalArgumentException(message);
    }
    
    public void throwGenericException(String message) throws Exception {
        throw new Exception(message);
    }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller with exception handling demonstrations
 */
@RestController
@RequestMapping("/api/exception-handler")
class ExceptionHandlerController {
    
    private final ExceptionService exceptionService;
    
    public ExceptionHandlerController(ExceptionService exceptionService) {
        this.exceptionService = exceptionService;
    }
    
    /**
     * Test ResourceNotFoundException
     */
    @GetMapping("/not-found")
    public ResponseEntity<Map<String, String>> testNotFound() {
        exceptionService.throwResourceNotFound("User", "123");
        return ResponseEntity.ok().build();
    }
    
    /**
     * Test ValidationException
     */
    @GetMapping("/validation-error")
    public ResponseEntity<Map<String, String>> testValidation() {
        List<String> errors = Arrays.asList(
            "Name is required",
            "Email is invalid",
            "Age must be positive"
        );
        exceptionService.throwValidationException(errors);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Test BusinessException
     */
    @GetMapping("/business-error")
    public ResponseEntity<Map<String, String>> testBusiness() {
        exceptionService.throwBusinessException(
            "Insufficient balance for transaction", 
            "INSUFFICIENT_BALANCE"
        );
        return ResponseEntity.ok().build();
    }
    
    /**
     * Test UnauthorizedException
     */
    @GetMapping("/unauthorized")
    public ResponseEntity<Map<String, String>> testUnauthorized() {
        exceptionService.throwUnauthorizedException("Invalid credentials");
        return ResponseEntity.ok().build();
    }
    
    /**
     * Test IllegalArgumentException
     */
    @GetMapping("/illegal-argument")
    public ResponseEntity<Map<String, String>> testIllegalArgument() {
        exceptionService.throwIllegalArgumentException("Invalid parameter value");
        return ResponseEntity.ok().build();
    }
    
    /**
     * Test generic exception
     */
    @GetMapping("/generic-error")
    public ResponseEntity<Map<String, String>> testGeneric() throws Exception {
        exceptionService.throwGenericException("Unexpected error occurred");
        return ResponseEntity.ok().build();
    }
    
    /**
     * Local exception handler (takes precedence over global)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleLocalException(
            IllegalStateException ex, HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            "Local handler: " + ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}
