package com.example.errorhandling.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Error Response Pattern
 * 
 * Standardized error response structure for RESTful APIs.
 * Provides consistent error format across all endpoints.
 * 
 * Key Concepts:
 * - Standard error response DTO
 * - Multiple error detail levels
 * - Error categorization
 * - Developer-friendly debugging info
 * 
 * Dependencies:
 * - spring-boot-starter-web
 * - jackson-databind
 */
@SpringBootApplication
public class ErrorResponsePattern {

    public static void main(String[] args) {
        SpringApplication.run(ErrorResponsePattern.class, args);
    }

    /**
     * Standard Error Response DTO
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private String errorCode;
        private List<ValidationError> validationErrors;
        private Map<String, Object> metadata;
        private String debugMessage;
        private String trace;

        public ErrorResponse() {
            this.timestamp = LocalDateTime.now();
        }

        public static ErrorResponse of(HttpStatus status, String message) {
            ErrorResponse error = new ErrorResponse();
            error.status = status.value();
            error.error = status.getReasonPhrase();
            error.message = message;
            return error;
        }

        public static ErrorResponse of(HttpStatus status, String message, String path) {
            ErrorResponse error = of(status, message);
            error.path = path;
            return error;
        }

        public ErrorResponse withErrorCode(String code) {
            this.errorCode = code;
            return this;
        }

        public ErrorResponse withValidationErrors(List<ValidationError> errors) {
            this.validationErrors = errors;
            return this;
        }

        public ErrorResponse withMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ErrorResponse withDebugInfo(String debugMessage, String trace) {
            this.debugMessage = debugMessage;
            this.trace = trace;
            return this;
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
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public List<ValidationError> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(List<ValidationError> validationErrors) { 
            this.validationErrors = validationErrors; 
        }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        public String getDebugMessage() { return debugMessage; }
        public void setDebugMessage(String debugMessage) { this.debugMessage = debugMessage; }
        public String getTrace() { return trace; }
        public void setTrace(String trace) { this.trace = trace; }
    }

    /**
     * Validation Error Detail
     */
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public ValidationError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getRejectedValue() { return rejectedValue; }
        public void setRejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; }
    }

    /**
     * Error Response Builder
     */
    public static class ErrorResponseBuilder {
        private final ErrorResponse error;

        private ErrorResponseBuilder(HttpStatus status) {
            this.error = new ErrorResponse();
            this.error.status = status.value();
            this.error.error = status.getReasonPhrase();
        }

        public static ErrorResponseBuilder status(HttpStatus status) {
            return new ErrorResponseBuilder(status);
        }

        public ErrorResponseBuilder message(String message) {
            this.error.message = message;
            return this;
        }

        public ErrorResponseBuilder path(String path) {
            this.error.path = path;
            return this;
        }

        public ErrorResponseBuilder code(String code) {
            this.error.errorCode = code;
            return this;
        }

        public ErrorResponseBuilder validationErrors(List<ValidationError> errors) {
            this.error.validationErrors = errors;
            return this;
        }

        public ErrorResponse build() {
            return this.error;
        }
    }

    /**
     * Sample Controller using Error Responses
     */
    @RestController
    @RequestMapping("/api/demo")
    public static class DemoController {

        @GetMapping("/not-found")
        public ResponseEntity<ErrorResponse> notFound() {
            ErrorResponse error = ErrorResponse
                .of(HttpStatus.NOT_FOUND, "Resource not found", "/api/demo/not-found")
                .withErrorCode("ERR_NOT_FOUND");
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @GetMapping("/validation-error")
        public ResponseEntity<ErrorResponse> validationError() {
            List<ValidationError> errors = List.of(
                new ValidationError("email", "Invalid email format", "invalid@"),
                new ValidationError("age", "Must be 18 or older", 15)
            );
            
            ErrorResponse error = ErrorResponseBuilder
                .status(HttpStatus.BAD_REQUEST)
                .message("Validation failed")
                .code("ERR_VALIDATION")
                .validationErrors(errors)
                .build();
            
            return ResponseEntity.badRequest().body(error);
        }
    }
}
