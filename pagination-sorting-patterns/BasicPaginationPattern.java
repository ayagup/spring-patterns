package com.example.pagination.basic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;
import java.util.List;

/**
 * Basic Pagination Pattern
 * 
 * Uses Spring Data's Pageable interface for standard pagination.
 * Returns Page object with metadata.
 */
@SpringBootApplication
public class BasicPaginationPattern {

    public static void main(String[] args) {
        SpringApplication.run(BasicPaginationPattern.class, args);
    }

    @Entity
    @Table(name = "products")
    public static class Product {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private Double price;
        private String category;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public interface ProductRepository extends JpaRepository<Product, Long> {
        Page<Product> findByCategory(String category, Pageable pageable);
    }

    @RestController
    @RequestMapping("/api/products")
    public static class ProductController {

        private final ProductRepository repository;

        public ProductController(ProductRepository repository) {
            this.repository = repository;
        }

        /**
         * Basic pagination with page and size parameters
         * GET /api/products?page=0&size=20
         */
        @GetMapping
        public Page<Product> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Pageable pageable = PageRequest.of(page, size);
            return repository.findAll(pageable);
        }

        /**
         * Pagination with sorting
         * GET /api/products?page=0&size=20&sort=price,desc
         */
        @GetMapping("/sorted")
        public Page<Product> getProductsSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
        ) {
            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            return repository.findAll(pageable);
        }

        /**
         * Using Pageable directly - Spring binds request params automatically
         * GET /api/products/auto?page=0&size=20&sort=price,desc&sort=name,asc
         */
        @GetMapping("/auto")
        public Page<Product> getProductsAuto(Pageable pageable) {
            return repository.findAll(pageable);
        }

        /**
         * Filtered pagination
         */
        @GetMapping("/category/{category}")
        public Page<Product> getByCategory(
            @PathVariable String category,
            Pageable pageable
        ) {
            return repository.findByCategory(category, pageable);
        }
    }
}
