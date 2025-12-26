package com.example.auditing.changelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Change Log Pattern
 * 
 * Maintains detailed change history with before/after values.
 * Useful for compliance and debugging.
 */
@SpringBootApplication
public class ChangeLogPattern {

    public static void main(String[] args) {
        SpringApplication.run(ChangeLogPattern.class, args);
    }

    @Entity
    @Table(name = "change_log")
    public static class ChangeLog {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String entityType;
        private Long entityId;
        private String operation; // CREATE, UPDATE, DELETE
        private String changedBy;
        private LocalDateTime changedAt;

        @Column(columnDefinition = "TEXT")
        private String beforeState;

        @Column(columnDefinition = "TEXT")
        private String afterState;

        @Column(columnDefinition = "TEXT")
        private String changes; // JSON of changed fields

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public String getChangedBy() { return changedBy; }
        public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
        public LocalDateTime getChangedAt() { return changedAt; }
        public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
        public String getBeforeState() { return beforeState; }
        public void setBeforeState(String beforeState) { this.beforeState = beforeState; }
        public String getAfterState() { return afterState; }
        public void setAfterState(String afterState) { this.afterState = afterState; }
        public String getChanges() { return changes; }
        public void setChanges(String changes) { this.changes = changes; }
    }

    public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {
        java.util.List<ChangeLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    }

    public static class ChangeLogService {

        private final ChangeLogRepository repository;

        public ChangeLogService(ChangeLogRepository repository) {
            this.repository = repository;
        }

        public void logChange(String entityType, Long entityId, String operation,
                            Object beforeState, Object afterState) {
            ChangeLog log = new ChangeLog();
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setOperation(operation);
            log.setChangedBy(getCurrentUser());
            log.setChangedAt(LocalDateTime.now());
            log.setBeforeState(toJson(beforeState));
            log.setAfterState(toJson(afterState));
            log.setChanges(calculateChanges(beforeState, afterState));
            repository.save(log);
        }

        private String getCurrentUser() {
            return "system"; // Get from security context
        }

        private String toJson(Object obj) {
            // Convert to JSON
            return obj != null ? obj.toString() : null;
        }

        private String calculateChanges(Object before, Object after) {
            // Calculate field-level changes
            return "{}";
        }
    }
}
