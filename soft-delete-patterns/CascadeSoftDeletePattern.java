package com.example.softdelete.cascade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cascade Soft Delete Pattern
 * 
 * Soft deletes cascade to related entities.
 * Maintains referential integrity with soft deletes.
 */
@SpringBootApplication
public class CascadeSoftDeletePattern {

    public static void main(String[] args) {
        SpringApplication.run(CascadeSoftDeletePattern.class, args);
    }

    @Entity
    @Table(name = "categories")
    public static class Category {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private Boolean deleted = false;
        private LocalDateTime deletedAt;

        @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
        private List<Item> items = new ArrayList<>();

        public void softDelete() {
            this.deleted = true;
            this.deletedAt = LocalDateTime.now();
            // Cascade soft delete to children
            items.forEach(Item::softDelete);
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Boolean getDeleted() { return deleted; }
        public void setDeleted(Boolean deleted) { this.deleted = deleted; }
        public LocalDateTime getDeletedAt() { return deletedAt; }
        public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
        public List<Item> getItems() { return items; }
        public void setItems(List<Item> items) { this.items = items; }
    }

    @Entity
    @Table(name = "items")
    public static class Item {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private Boolean deleted = false;
        private LocalDateTime deletedAt;

        @ManyToOne
        @JoinColumn(name = "category_id")
        private Category category;

        public void softDelete() {
            this.deleted = true;
            this.deletedAt = LocalDateTime.now();
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Boolean getDeleted() { return deleted; }
        public void setDeleted(Boolean deleted) { this.deleted = deleted; }
        public LocalDateTime getDeletedAt() { return deletedAt; }
        public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
        public Category getCategory() { return category; }
        public void setCategory(Category category) { this.category = category; }
    }
}
