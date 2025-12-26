package com.example.errorhandling.errorcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * Error Code Pattern
 * 
 * Standardized error codes for consistent error identification across the API.
 * Maps business errors to unique codes for client-side handling.
 */
@SpringBootApplication
public class ErrorCodePattern {

    public static void main(String[] args) {
        SpringApplication.run(ErrorCodePattern.class, args);
    }

    /**
     * Enum of standard error codes
     */
    public enum ErrorCode {
        // Validation errors (1000-1999)
        VALIDATION_FAILED("ERR_1000", "Validation failed"),
        INVALID_INPUT("ERR_1001", "Invalid input"),
        MISSING_REQUIRED_FIELD("ERR_1002", "Required field missing"),
        
        // Business logic errors (2000-2999)
        RESOURCE_NOT_FOUND("ERR_2000", "Resource not found"),
        DUPLICATE_RESOURCE("ERR_2001", "Resource already exists"),
        OPERATION_NOT_ALLOWED("ERR_2002", "Operation not allowed"),
        
        // Authentication/Authorization (3000-3999)
        UNAUTHORIZED("ERR_3000", "Unauthorized"),
        FORBIDDEN("ERR_3001", "Forbidden"),
        TOKEN_EXPIRED("ERR_3002", "Token expired"),
        
        // System errors (4000-4999)
        INTERNAL_ERROR("ERR_4000", "Internal server error"),
        SERVICE_UNAVAILABLE("ERR_4001", "Service temporarily unavailable");

        private final String code;
        private final String message;

        ErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
    }

    /**
     * Error response with code
     */
    public record ErrorResponse(String code, String message, Object details) {
        public static ErrorResponse of(ErrorCode errorCode) {
            return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null);
        }

        public static ErrorResponse of(ErrorCode errorCode, Object details) {
            return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), details);
        }
    }

    /**
     * Controller Advice
     */
    @ControllerAdvice
    public static class ErrorCodeAdvice {

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
            ErrorResponse error = ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(DuplicateException.class)
        public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateException ex) {
            ErrorResponse error = ErrorResponse.of(ErrorCode.DUPLICATE_RESOURCE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }

    public static class DuplicateException extends RuntimeException {
        public DuplicateException(String message) { super(message); }
    }
}
