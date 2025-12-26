package com.example.errorhandling.problemdetails;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.*;

/**
 * Problem Details Pattern (RFC 7807)
 * 
 * Implements RFC 7807 standard for HTTP API error responses.
 * Provides machine-readable error format for REST APIs.
 * 
 * RFC 7807 Fields:
 * - type: URI reference identifying the problem type
 * - title: Short, human-readable summary
 * - status: HTTP status code
 * - detail: Human-readable explanation
 * - instance: URI reference identifying the specific occurrence
 * 
 * Dependencies:
 * - spring-boot-starter-web
 */
@SpringBootApplication
public class ProblemDetailsPattern {

    public static void main(String[] args) {
        SpringApplication.run(ProblemDetailsPattern.class, args);
    }

    /**
     * RFC 7807 Problem Details DTO
     */
    public static class ProblemDetail {
        private String type;
        private String title;
        private int status;
        private String detail;
        private String instance;
        private Instant timestamp;
        private Map<String, Object> extensions;

        public ProblemDetail() {
            this.timestamp = Instant.now();
            this.extensions = new HashMap<>();
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final ProblemDetail problem = new ProblemDetail();

            public Builder type(String type) {
                problem.type = type;
                return this;
            }

            public Builder title(String title) {
                problem.title = title;
                return this;
            }

            public Builder status(HttpStatus status) {
                problem.status = status.value();
                if (problem.title == null) {
                    problem.title = status.getReasonPhrase();
                }
                return this;
            }

            public Builder detail(String detail) {
                problem.detail = detail;
                return this;
            }

            public Builder instance(String instance) {
                problem.instance = instance;
                return this;
            }

            public Builder extension(String key, Object value) {
                problem.extensions.put(key, value);
                return this;
            }

            public ProblemDetail build() {
                return problem;
            }
        }

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
        public String getInstance() { return instance; }
        public void setInstance(String instance) { this.instance = instance; }
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        public Map<String, Object> getExtensions() { return extensions; }
        public void setExtensions(Map<String, Object> extensions) { this.extensions = extensions; }
    }

    /**
     * Controller Advice with Problem Details
     */
    @ControllerAdvice
    public static class ProblemDetailsAdvice {

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ProblemDetail> handleNotFound(
                EntityNotFoundException ex) {
            
            ProblemDetail problem = ProblemDetail.builder()
                .type("https://api.example.com/errors/not-found")
                .status(HttpStatus.NOT_FOUND)
                .detail(ex.getMessage())
                .instance("/api/entities/" + ex.getEntityId())
                .extension("entityType", ex.getEntityType())
                .extension("entityId", ex.getEntityId())
                .build();
            
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
        }

        @ExceptionHandler(ValidationFailedException.class)
        public ResponseEntity<ProblemDetail> handleValidation(
                ValidationFailedException ex) {
            
            ProblemDetail problem = ProblemDetail.builder()
                .type("https://api.example.com/errors/validation-failed")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST)
                .detail("Input validation failed")
                .extension("violations", ex.getViolations())
                .build();
            
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
        }
    }

    /**
     * Custom Exceptions
     */
    public static class EntityNotFoundException extends RuntimeException {
        private final String entityType;
        private final String entityId;

        public EntityNotFoundException(String entityType, String entityId) {
            super(String.format("%s with id %s not found", entityType, entityId));
            this.entityType = entityType;
            this.entityId = entityId;
        }

        public String getEntityType() { return entityType; }
        public String getEntityId() { return entityId; }
    }

    public static class ValidationFailedException extends RuntimeException {
        private final List<String> violations;

        public ValidationFailedException(List<String> violations) {
            super("Validation failed");
            this.violations = violations;
        }

        public List<String> getViolations() { return violations; }
    }
}
