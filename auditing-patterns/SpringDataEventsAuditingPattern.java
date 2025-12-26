package com.example.auditing.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.stereotype.Component;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Spring Data Domain Events Auditing Pattern
 * 
 * Uses Spring Data domain events for auditing.
 * Publishes events when entities are saved.
 */
@SpringBootApplication
public class SpringDataEventsAuditingPattern {

    public static void main(String[] args) {
        SpringApplication.run(SpringDataEventsAuditingPattern.class, args);
    }

    /**
     * Domain Event
     */
    public static class EntityModifiedEvent {
        private final Object entity;
        private final String action;
        private final LocalDateTime timestamp;

        public EntityModifiedEvent(Object entity, String action) {
            this.entity = entity;
            this.action = action;
            this.timestamp = LocalDateTime.now();
        }

        public Object getEntity() { return entity; }
        public String getAction() { return action; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    @Entity
    public static class Order {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String orderNumber;
        private Double total;

        @Transient
        private final List<Object> domainEvents = new ArrayList<>();

        @DomainEvents
        public Collection<Object> domainEvents() {
            return Collections.unmodifiableList(domainEvents);
        }

        @AfterDomainEventPublication
        public void clearDomainEvents() {
            domainEvents.clear();
        }

        public void markCreated() {
            domainEvents.add(new EntityModifiedEvent(this, "CREATED"));
        }

        public void markUpdated() {
            domainEvents.add(new EntityModifiedEvent(this, "UPDATED"));
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
    }

    @Component
    public static class AuditEventListener {

        @EventListener
        public void handleEntityModified(EntityModifiedEvent event) {
            System.out.println("Entity modified: " + event.getAction() +
                             " at " + event.getTimestamp());
            System.out.println("Entity: " + event.getEntity());
            // Save to audit log
        }
    }
}
