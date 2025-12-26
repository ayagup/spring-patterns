package com.example.auditing.temporal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Temporal Auditing Pattern
 * 
 * Tracks creation and modification timestamps.
 * Simple time-based auditing without user tracking.
 */
@SpringBootApplication
@EnableJpaAuditing
public class TemporalAuditingPattern {

    public static void main(String[] args) {
        SpringApplication.run(TemporalAuditingPattern.class, args);
    }

    @MappedSuperclass
    @EntityListeners(AuditingEntityListener.class)
    public static abstract class TemporalEntity {

        @CreatedDate
        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @LastModifiedDate
        private LocalDateTime updatedAt;

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    @Entity
    @Table(name = "events")
    public static class Event extends TemporalEntity {
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
}
