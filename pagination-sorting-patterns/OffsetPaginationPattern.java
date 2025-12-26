package com.example.pagination.offset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;
import java.util.List;

/**
 * Offset-based Pagination Pattern
 * 
 * Traditional SQL OFFSET/LIMIT pagination.
 * Simple but can have performance issues with large offsets.
 */
@SpringBootApplication
public class OffsetPaginationPattern {

    public static void main(String[] args) {
        SpringApplication.run(OffsetPaginationPattern.class, args);
    }

    @Entity
    @Table(name = "items")
    public static class Item {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private String description;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public interface ItemRepository extends JpaRepository<Item, Long> {
        
        @Query(value = "SELECT * FROM items ORDER BY id LIMIT :limit OFFSET :offset", 
               nativeQuery = true)
        List<Item> findWithOffsetLimit(int offset, int limit);
        
        @Query("SELECT COUNT(i) FROM Item i")
        long countAll();
    }

    @Service
    public static class ItemService {
        
        private final ItemRepository repository;

        public ItemService(ItemRepository repository) {
            this.repository = repository;
        }

        public OffsetResponse<Item> getItems(int offset, int limit) {
            List<Item> items = repository.findWithOffsetLimit(offset, limit);
            long total = repository.countAll();
            
            return new OffsetResponse<>(
                items,
                offset,
                limit,
                total,
                (int) Math.ceil((double) total / limit)
            );
        }
    }

    @RestController
    @RequestMapping("/api/items")
    public static class ItemController {

        private final ItemService service;

        public ItemController(ItemService service) {
            this.service = service;
        }

        /**
         * Offset-based pagination
         * GET /api/items?offset=0&limit=20
         */
        @GetMapping
        public OffsetResponse<Item> getItems(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
        ) {
            // Validate limits
            if (limit > 100) limit = 100;
            if (offset < 0) offset = 0;
            
            return service.getItems(offset, limit);
        }
    }

    /**
     * Offset response wrapper
     */
    public record OffsetResponse<T>(
        List<T> data,
        int offset,
        int limit,
        long total,
        int totalPages
    ) {}
}
