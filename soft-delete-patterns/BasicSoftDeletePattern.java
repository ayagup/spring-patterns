package com.example.softdelete.basic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;

/**
 * Basic Soft Delete Pattern
 * 
 * Marks records as deleted instead of removing them.
 * Uses @SQLDelete and @Where annotations.
 */
@SpringBootApplication
public class BasicSoftDeletePattern {

    public static void main(String[] args) {
        SpringApplication.run(BasicSoftDeletePattern.class, args);
    }

    @Entity
    @Table(name = "users")
    @SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
    @Where(clause = "deleted = false")
    public static class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String username;
        private String email;
        private Boolean deleted = false;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Boolean getDeleted() { return deleted; }
        public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    }

    public interface UserRepository extends JpaRepository<User, Long> {}
}
