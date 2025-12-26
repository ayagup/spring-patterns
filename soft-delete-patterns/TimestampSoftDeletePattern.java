package com.example.softdelete.timestamp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Timestamp Soft Delete Pattern
 * 
 * Uses timestamp field for soft delete.
 * Allows querying deleted items and their deletion time.
 */
@SpringBootApplication
public class TimestampSoftDeletePattern {

    public static void main(String[] args) {
        SpringApplication.run(TimestampSoftDeletePattern.class, args);
    }

    @Entity
    @Table(name = "products")
    public static class Product {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private Double price;
        private LocalDateTime deletedAt;

        @Transient
        public boolean isDeleted() {
            return deletedAt != null;
        }

        public void delete() {
            this.deletedAt = LocalDateTime.now();
        }

        public void restore() {
            this.deletedAt = null;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public LocalDateTime getDeletedAt() { return deletedAt; }
        public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    }

    public interface ProductRepository extends JpaRepository<Product, Long> {
        
        @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
        List<Product> findAllActive();
        
        @Query("SELECT p FROM Product p WHERE p.deletedAt IS NOT NULL")
        List<Product> findAllDeleted();
        
        @Query("SELECT p FROM Product p WHERE p.id = ?1 AND p.deletedAt IS NULL")
        Product findByIdActive(Long id);
    }
}
