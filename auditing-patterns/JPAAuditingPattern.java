package com.example.auditing.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JPA Auditing Pattern
 * 
 * Automatic auditing of entity creation and modification.
 * Tracks who and when entities were created/modified.
 * 
 * @EnableJpaAuditing required
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JPAAuditingPattern {

    public static void main(String[] args) {
        SpringApplication.run(JPAAuditingPattern.class, args);
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        // In real app, get from security context
        return () -> Optional.of("system");
    }

    @Entity
    @EntityListeners(AuditingEntityListener.class)
    public static class AuditedEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @CreatedBy
        private String createdBy;

        @CreatedDate
        private LocalDateTime createdDate;

        @LastModifiedBy
        private String lastModifiedBy;

        @LastModifiedDate
        private LocalDateTime lastModifiedDate;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
        public String getLastModifiedBy() { return lastModifiedBy; }
        public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
        public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
        public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }
    }

    public interface AuditedEntityRepository extends JpaRepository<AuditedEntity, Long> {}
}
