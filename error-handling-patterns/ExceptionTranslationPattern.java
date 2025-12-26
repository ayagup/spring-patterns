package com.example.errorhandling.translation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

/**
 * Exception Translation Pattern
 * 
 * Translates low-level exceptions (DB, external services) into
 * meaningful business exceptions for consistent error handling.
 */
@SpringBootApplication
public class ExceptionTranslationPattern {

    public static void main(String[] args) {
        SpringApplication.run(ExceptionTranslationPattern.class, args);
    }

    /**
     * Exception Translator
     */
    public static class ExceptionTranslator {

        public static BusinessException translate(DataAccessException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("duplicate")) {
                return new DuplicateResourceException("Resource already exists");
            }
            return new DataAccessFailedException("Database operation failed");
        }

        public static BusinessException translate(SQLException ex) {
            return switch (ex.getErrorCode()) {
                case 1062 -> new DuplicateResourceException("Duplicate entry");
                case 1451 -> new IntegrityViolationException("Foreign key constraint");
                default -> new DataAccessFailedException("Database error: " + ex.getMessage());
            };
        }
    }

    /**
     * Business Exceptions
     */
    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) { super(message); }
    }

    public static class DuplicateResourceException extends BusinessException {
        public DuplicateResourceException(String message) { super(message); }
    }

    public static class DataAccessFailedException extends BusinessException {
        public DataAccessFailedException(String message) { super(message); }
    }

    public static class IntegrityViolationException extends BusinessException {
        public IntegrityViolationException(String message) { super(message); }
    }

    @ControllerAdvice
    public static class ExceptionTranslationAdvice {

        @ExceptionHandler(DataAccessException.class)
        public ResponseEntity<String> handleDataAccess(DataAccessException ex) {
            BusinessException translated = ExceptionTranslator.translate(ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(translated.getMessage());
        }

        @ExceptionHandler(SQLException.class)
        public ResponseEntity<String> handleSQL(SQLException ex) {
            BusinessException translated = ExceptionTranslator.translate(ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(translated.getMessage());
        }
    }
}
