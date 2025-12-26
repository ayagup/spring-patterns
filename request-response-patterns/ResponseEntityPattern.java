package com.example.requestresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Response Entity Pattern
 * 
 * Demonstrates how to use ResponseEntity for full control over HTTP response.
 * ResponseEntity allows customization of status code, headers, and body.
 */
@SpringBootApplication
public class ResponseEntityPattern {

    public static void main(String[] args) {
        SpringApplication.run(ResponseEntityPattern.class, args);
    }

    @RestController
    @RequestMapping("/api/entity")
    static class ResponseEntityController {

        /**
         * Simple ResponseEntity with OK status
         */
        @GetMapping("/simple")
        public ResponseEntity<String> simple() {
            return ResponseEntity.ok("Success");
        }

        /**
         * ResponseEntity with custom status
         */
        @PostMapping("/create")
        public ResponseEntity<Map<String, Object>> createResource() {
            Map<String, Object> resource = new HashMap<>();
            resource.put("id", 123);
            resource.put("name", "New Resource");
            resource.put("created", true);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(resource);
        }

        /**
         * ResponseEntity with NOT_FOUND status
         */
        @GetMapping("/find/{id}")
        public ResponseEntity<String> findResource(@PathVariable Long id) {
            if (id <= 0) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Resource not found");
            }
            return ResponseEntity.ok("Resource found: " + id);
        }

        /**
         * ResponseEntity with NO_CONTENT status
         */
        @DeleteMapping("/delete/{id}")
        public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
            // Deletion logic here
            return ResponseEntity.noContent().build();
        }

        /**
         * ResponseEntity with ACCEPTED status (async processing)
         */
        @PostMapping("/async")
        public ResponseEntity<Map<String, String>> asyncProcess(@RequestBody String data) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "accepted");
            response.put("jobId", "job_" + System.currentTimeMillis());
            
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(response);
        }

        /**
         * ResponseEntity with custom headers
         */
        @GetMapping("/with-headers")
        public ResponseEntity<String> withHeaders() {
            return ResponseEntity.ok()
                    .header("X-Custom-Header", "CustomValue")
                    .header("X-Request-ID", "REQ-123")
                    .body("Response with custom headers");
        }

        /**
         * ResponseEntity with Location header (created resource)
         */
        @PostMapping("/users")
        public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> user) {
            Long userId = 42L;
            Map<String, Object> createdUser = new HashMap<>(user);
            createdUser.put("id", userId);
            
            return ResponseEntity
                    .created(java.net.URI.create("/api/entity/users/" + userId))
                    .body(createdUser);
        }

        /**
         * ResponseEntity with BAD_REQUEST validation error
         */
        @PostMapping("/validate")
        public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, String> data) {
            if (!data.containsKey("email")) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("field", "email");
                error.put("message", "Email is required");
                
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(error);
            }
            return ResponseEntity.ok(data);
        }

        /**
         * ResponseEntity with UNAUTHORIZED status
         */
        @GetMapping("/protected")
        public ResponseEntity<String> protectedResource(
                @RequestHeader(value = "Authorization", required = false) String auth) {
            if (auth == null || !auth.startsWith("Bearer ")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Bearer")
                        .body("Unauthorized access");
            }
            return ResponseEntity.ok("Protected resource");
        }

        /**
         * ResponseEntity with FORBIDDEN status
         */
        @GetMapping("/admin")
        public ResponseEntity<String> adminResource() {
            boolean hasAdminRole = false; // Check user role
            
            if (!hasAdminRole) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Access denied");
            }
            return ResponseEntity.ok("Admin resource");
        }

        /**
         * ResponseEntity with CONFLICT status
         */
        @PostMapping("/register")
        public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> user) {
            boolean emailExists = true; // Check if email exists
            
            if (emailExists) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email already registered");
                
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(error);
            }
            return ResponseEntity.ok(user);
        }

        /**
         * ResponseEntity with INTERNAL_SERVER_ERROR
         */
        @GetMapping("/error")
        public ResponseEntity<Map<String, String>> serverError() {
            try {
                // Simulate error
                throw new RuntimeException("Unexpected error");
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Internal server error");
                error.put("message", e.getMessage());
                
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(error);
            }
        }

        /**
         * ResponseEntity builder pattern
         */
        @GetMapping("/builder")
        public ResponseEntity<Map<String, Object>> builderPattern() {
            Map<String, Object> body = new HashMap<>();
            body.put("status", "success");
            body.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header("Cache-Control", "no-cache")
                    .header("X-Response-Time", "25ms")
                    .body(body);
        }

        /**
         * Conditional ResponseEntity
         */
        @GetMapping("/conditional/{id}")
        public ResponseEntity<?> conditionalResponse(@PathVariable Long id) {
            if (id == 0) {
                return ResponseEntity.notFound().build();
            } else if (id < 0) {
                return ResponseEntity.badRequest().body("Invalid ID");
            } else {
                Map<String, Object> resource = new HashMap<>();
                resource.put("id", id);
                resource.put("name", "Resource " + id);
                return ResponseEntity.ok(resource);
            }
        }

        /**
         * ResponseEntity with ETag for caching
         */
        @GetMapping("/cached/{id}")
        public ResponseEntity<Map<String, Object>> cachedResource(
                @PathVariable Long id,
                @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
            
            String etag = "\"" + id.hashCode() + "\"";
            
            if (etag.equals(ifNoneMatch)) {
                return ResponseEntity
                        .status(HttpStatus.NOT_MODIFIED)
                        .eTag(etag)
                        .build();
            }
            
            Map<String, Object> resource = new HashMap<>();
            resource.put("id", id);
            resource.put("data", "Resource data");
            
            return ResponseEntity.ok()
                    .eTag(etag)
                    .body(resource);
        }
    }
}
