package com.example.auditing.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Audit Metadata Pattern
 * 
 * Stores additional metadata with audit entries.
 * Includes IP address, user agent, session info, etc.
 */
@SpringBootApplication
public class AuditMetadataPattern {

    public static void main(String[] args) {
        SpringApplication.run(AuditMetadataPattern.class, args);
    }

    @Entity
    @Table(name = "audit_entries")
    public static class AuditEntry {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String entityType;
        private Long entityId;
        private String action;
        private String userId;
        private LocalDateTime timestamp;

        // Metadata
        private String ipAddress;
        private String userAgent;
        private String sessionId;
        private String requestId;

        @ElementCollection
        @CollectionTable(name = "audit_metadata", 
                        joinColumns = @JoinColumn(name = "audit_entry_id"))
        @MapKeyColumn(name = "meta_key")
        @Column(name = "meta_value")
        private Map<String, String> metadata = new HashMap<>();

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }

    public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {}

    public static class AuditMetadataService {

        private final AuditEntryRepository repository;

        public AuditMetadataService(AuditEntryRepository repository) {
            this.repository = repository;
        }

        public void audit(String entityType, Long entityId, String action,
                         javax.servlet.http.HttpServletRequest request) {
            AuditEntry entry = new AuditEntry();
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setAction(action);
            entry.setUserId(getCurrentUserId());
            entry.setTimestamp(LocalDateTime.now());
            entry.setIpAddress(getClientIP(request));
            entry.setUserAgent(request.getHeader("User-Agent"));
            entry.setSessionId(request.getSession().getId());
            entry.setRequestId(request.getHeader("X-Request-ID"));
            
            // Add custom metadata
            entry.getMetadata().put("endpoint", request.getRequestURI());
            entry.getMetadata().put("method", request.getMethod());
            
            repository.save(entry);
        }

        private String getCurrentUserId() {
            return "user123";
        }

        private String getClientIP(javax.servlet.http.HttpServletRequest request) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader == null) {
                return request.getRemoteAddr();
            }
            return xfHeader.split(",")[0];
        }
    }
}
