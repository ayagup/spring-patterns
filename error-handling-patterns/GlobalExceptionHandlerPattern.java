package com.example.errorhandling.global;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler Pattern
 * 
 * Centralized exception handling for all controllers using @ControllerAdvice.
 * Provides consistent error responses across the application.
 * 
 * Key Concepts:
 * - @ControllerAdvice for global exception handling
 * - @ExceptionHandler for specific exceptions
 * - Consistent error response structure
 * - HTTP status code mapping
 * 
 * Dependencies:
 * - spring-boot-starter-web
 */
@SpringBootApplication
public class GlobalExceptionHandlerPattern {

    public static void main(String[] args) {
        SpringApplication.run(GlobalExceptionHandlerPattern.class, args);
    }

    /**
     * Global Exception Handler
     * Handles all exceptions thrown by controllers
     */
    @ControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(
                ResourceNotFoundException ex, WebRequest request) {
            
            ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getDescription(false)
            );
            
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequest(
                BadRequestException ex, WebRequest request) {
            
            ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false)
            );
            
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                Exception ex, WebRequest request) {
            
            ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getDescription(false)
            );
            
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalArgument(
                IllegalArgumentException ex) {
            
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("message", ex.getMessage());
            body.put("status", HttpStatus.BAD_REQUEST.value());
            
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Error Response DTO
     */
    public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
    ) {}

    /**
     * Custom Exceptions
     */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    /**
     * Sample Controller
     */
    @RestController
    @RequestMapping("/api/books")
    public static class BookController {

        @GetMapping("/{id}")
        public String getBook(@PathVariable String id) {
            if ("999".equals(id)) {
                throw new ResourceNotFoundException("Book not found with id: " + id);
            }
            return "Book: " + id;
        }

        @PostMapping
        public String createBook(@RequestBody Map<String, String> book) {
            if (!book.containsKey("title")) {
                throw new BadRequestException("Title is required");
            }
            return "Created: " + book.get("title");
        }
    }
}
