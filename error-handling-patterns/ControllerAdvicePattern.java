package com.example.errorhandling.advice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller Advice Pattern
 * 
 * Advanced @ControllerAdvice usage with ordering, base packages,
 * and annotation-based targeting.
 * 
 * Key Concepts:
 * - Multiple @ControllerAdvice classes
 * - @Order for precedence
 * - Base package scoping
 * - Annotation targeting
 * - Validation error handling
 * 
 * Dependencies:
 * - spring-boot-starter-web
 * - spring-boot-starter-validation
 */
@SpringBootApplication
public class ControllerAdvicePattern {

    public static void main(String[] args) {
        SpringApplication.run(ControllerAdvicePattern.class, args);
    }

    /**
     * High Priority Controller Advice
     * Handles security-related exceptions first
     */
    @ControllerAdvice
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class SecurityExceptionAdvice {

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
            ErrorResponse error = new ErrorResponse(
                "UNAUTHORIZED",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
            ErrorResponse error = new ErrorResponse(
                "FORBIDDEN",
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    /**
     * Validation Controller Advice
     * Handles validation errors
     */
    @ControllerAdvice
    @Order(Ordered.HIGH_PRECEDENCE)
    public static class ValidationExceptionAdvice {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
                MethodArgumentNotValidException ex) {
            
            List<FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                    error.getField(),
                    error.getDefaultMessage()
                ))
                .collect(Collectors.toList());
            
            ValidationErrorResponse response = new ValidationErrorResponse(
                "VALIDATION_FAILED",
                "Input validation failed",
                errors
            );
            
            return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
                ConstraintViolationException ex) {
            
            List<FieldError> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new FieldError(
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
                ))
                .collect(Collectors.toList());
            
            ValidationErrorResponse response = new ValidationErrorResponse(
                "CONSTRAINT_VIOLATION",
                "Constraint validation failed",
                errors
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Business Logic Controller Advice
     * Scoped to specific package
     */
    @ControllerAdvice(basePackages = "com.example.business")
    @Order(Ordered.LOWEST_PRECEDENCE)
    public static class BusinessExceptionAdvice {

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
            ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY.value()
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }
    }

    /**
     * REST Controller Specific Advice
     * Only applies to @RestController annotated classes
     */
    @ControllerAdvice(annotations = RestController.class)
    public static class RestControllerAdvice {

        @ExceptionHandler(DataNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleDataNotFound(DataNotFoundException ex) {
            ErrorResponse error = new ErrorResponse(
                "DATA_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Response DTOs
     */
    public record ErrorResponse(String code, String message, int status) {}
    
    public record FieldError(String field, String message) {}
    
    public record ValidationErrorResponse(
        String code,
        String message,
        List<FieldError> errors
    ) {}

    /**
     * Custom Exceptions
     */
    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }

    public static class BusinessException extends RuntimeException {
        private final String errorCode;

        public BusinessException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    public static class DataNotFoundException extends RuntimeException {
        public DataNotFoundException(String message) {
            super(message);
        }
    }
}
