package com.example.auditing.versioning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.*;

/**
 * Version Control Pattern
 * 
 * Optimistic locking with version tracking.
 * Prevents lost updates in concurrent scenarios.
 */
@SpringBootApplication
public class VersionControlPattern {

    public static void main(String[] args) {
        SpringApplication.run(VersionControlPattern.class, args);
    }

    @Entity
    @Table(name = "documents")
    public static class Document {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;
        private String content;

        @Version
        private Long version;

        private java.time.LocalDateTime lastModified;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getVersion() { return version; }
        public void setVersion(Long version) { this.version = version; }
        public java.time.LocalDateTime getLastModified() { return lastModified; }
        public void setLastModified(java.time.LocalDateTime lastModified) { 
            this.lastModified = lastModified; 
        }
    }

    public interface DocumentRepository extends JpaRepository<Document, Long> {}

    public static class DocumentService {

        private final DocumentRepository repository;

        public DocumentService(DocumentRepository repository) {
            this.repository = repository;
        }

        public Document update(Long id, String newContent) {
            Document doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
            
            doc.setContent(newContent);
            doc.setLastModified(java.time.LocalDateTime.now());
            
            try {
                return repository.save(doc);
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                throw new RuntimeException("Document was modified by another user");
            }
        }
    }
}
