package com.example.auditing.custom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Custom Audit Trail Pattern
 * 
 * Manual audit table for tracking all entity changes.
 * Uses JPA listeners to capture events.
 */
@SpringBootApplication
public class CustomAuditTrailPattern {

    public static void main(String[] args) {
        SpringApplication.run(CustomAuditTrailPattern.class, args);
    }

    @Entity
    @Table(name = "audit_log")
    public static class AuditLog {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String entityType;
        private Long entityId;
        private String action; // CREATE, UPDATE, DELETE
        private String fieldName;
        private String oldValue;
        private String newValue;
        private String modifiedBy;
        private LocalDateTime modifiedAt;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }
        public String getOldValue() { return oldValue; }
        public void setOldValue(String oldValue) { this.oldValue = oldValue; }
        public String getNewValue() { return newValue; }
        public void setNewValue(String newValue) { this.newValue = newValue; }
        public String getModifiedBy() { return modifiedBy; }
        public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
        public LocalDateTime getModifiedAt() { return modifiedAt; }
        public void setModifiedAt(LocalDateTime modifiedAt) { this.modifiedAt = modifiedAt; }
    }

    public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}

    @Entity
    @EntityListeners(ProductAuditListener.class)
    public static class Product {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private Double price;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }

    @Component
    public static class ProductAuditListener {

        private static AuditLogRepository auditLogRepository;

        public ProductAuditListener(AuditLogRepository repository) {
            ProductAuditListener.auditLogRepository = repository;
        }

        @PostPersist
        public void onPostPersist(Product product) {
            createAuditLog(product, "CREATE", null, product.getName());
        }

        @PostUpdate
        public void onPostUpdate(Product product) {
            createAuditLog(product, "UPDATE", null, product.getName());
        }

        @PostRemove
        public void onPostRemove(Product product) {
            createAuditLog(product, "DELETE", product.getName(), null);
        }

        private void createAuditLog(Product product, String action, 
                                   String oldValue, String newValue) {
            AuditLog log = new AuditLog();
            log.setEntityType("Product");
            log.setEntityId(product.getId());
            log.setAction(action);
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setModifiedBy("system");
            log.setModifiedAt(LocalDateTime.now());
            auditLogRepository.save(log);
        }
    }
}
