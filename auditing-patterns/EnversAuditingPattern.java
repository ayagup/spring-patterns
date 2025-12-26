package com.example.auditing.envers;

import org.hibernate.envers.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.*;
import java.util.List;

/**
 * Envers Auditing Pattern
 * 
 * Hibernate Envers for entity versioning and history tracking.
 * Automatically creates audit tables for @Audited entities.
 * 
 * Dependencies:
 * - hibernate-envers
 */
@SpringBootApplication
public class EnversAuditingPattern {

    public static void main(String[] args) {
        SpringApplication.run(EnversAuditingPattern.class, args);
    }

    @Entity
    @Audited
    @Table(name = "customers")
    public static class Customer {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String name;

        private String email;

        @NotAudited  // Exclude from auditing
        private String password;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public interface CustomerRepository extends JpaRepository<Customer, Long> {}

    /**
     * Service to query audit history
     */
    public static class AuditService {

        @PersistenceContext
        private EntityManager entityManager;

        public List<Number> getRevisions(Long customerId) {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            return reader.getRevisions(Customer.class, customerId);
        }

        public Customer getRevision(Long customerId, Number revision) {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            return reader.find(Customer.class, customerId, revision);
        }

        public AuditQuery createAuditQuery() {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            return reader.createQuery()
                .forRevisionsOfEntity(Customer.class, false, true);
        }
    }
}
