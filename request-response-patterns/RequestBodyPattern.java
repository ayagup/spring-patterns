package com.example.requestresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Request Body Pattern
 * 
 * Demonstrates how to handle HTTP request bodies in Spring MVC.
 * @RequestBody annotation binds HTTP request body to method parameter.
 * Commonly used with POST and PUT requests.
 */
@SpringBootApplication
public class RequestBodyPattern {

    public static void main(String[] args) {
        SpringApplication.run(RequestBodyPattern.class, args);
    }

    @RestController
    @RequestMapping("/api/users")
    static class UserController {

        /**
         * Simple request body binding
         */
        @PostMapping
        public ResponseEntity<User> createUser(@RequestBody User user) {
            user.setCreatedAt(LocalDateTime.now());
            return ResponseEntity.ok(user);
        }

        /**
         * Request body with validation
         */
        @PostMapping("/validated")
        public ResponseEntity<User> createValidatedUser(@Valid @RequestBody User user) {
            user.setCreatedAt(LocalDateTime.now());
            return ResponseEntity.ok(user);
        }

        /**
         * Request body with custom DTO
         */
        @PutMapping("/{id}")
        public ResponseEntity<User> updateUser(
                @PathVariable Long id,
                @RequestBody UserUpdateRequest request) {
            User user = new User();
            user.setId(id);
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(user);
        }

        /**
         * Partial update with request body
         */
        @PatchMapping("/{id}")
        public ResponseEntity<User> patchUser(
                @PathVariable Long id,
                @RequestBody UserPatchRequest request) {
            User user = new User();
            user.setId(id);
            if (request.getName() != null) {
                user.setName(request.getName());
            }
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            user.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(user);
        }
    }

    /**
     * User entity with validation annotations
     */
    static class User {
        private Long id;

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100)
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @Min(18)
        @Max(120)
        private Integer age;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * DTO for update operations
     */
    static class UserUpdateRequest {
        @NotBlank
        private String name;
        
        @Email
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /**
     * DTO for partial updates
     */
    static class UserPatchRequest {
        private String name;
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
