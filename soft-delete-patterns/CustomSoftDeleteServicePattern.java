package com.example.softdelete.custom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Custom Soft Delete Service Pattern
 * 
 * Service layer handles soft delete logic.
 * Provides restore functionality.
 */
@SpringBootApplication
public class CustomSoftDeleteServicePattern {

    public static void main(String[] args) {
        SpringApplication.run(CustomSoftDeleteServicePattern.class, args);
    }

    @Entity
    @Table(name = "documents")
    public static class Document {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;
        private String content;
        private Boolean deleted = false;
        private LocalDateTime deletedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Boolean getDeleted() { return deleted; }
        public void setDeleted(Boolean deleted) { this.deleted = deleted; }
        public LocalDateTime getDeletedAt() { return deletedAt; }
        public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    }

    public interface DocumentRepository extends JpaRepository<Document, Long> {}

    @Service
    public static class DocumentService {

        private final DocumentRepository repository;

        public DocumentService(DocumentRepository repository) {
            this.repository = repository;
        }

        public void softDelete(Long id) {
            Document doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
            
            doc.setDeleted(true);
            doc.setDeletedAt(LocalDateTime.now());
            repository.save(doc);
        }

        public void restore(Long id) {
            Document doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
            
            doc.setDeleted(false);
            doc.setDeletedAt(null);
            repository.save(doc);
        }

        public void permanentDelete(Long id) {
            repository.deleteById(id);
        }
    }
}
