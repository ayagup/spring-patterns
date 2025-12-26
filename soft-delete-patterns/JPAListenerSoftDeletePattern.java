package com.example.softdelete.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Listener Soft Delete Pattern
 * 
 * Uses JPA lifecycle callbacks for soft delete.
 * Automatic soft delete on entity removal.
 */
@SpringBootApplication
public class JPAListenerSoftDeletePattern {

    public static void main(String[] args) {
        SpringApplication.run(JPAListenerSoftDeletePattern.class, args);
    }

    @MappedSuperclass
    @EntityListeners(SoftDeleteListener.class)
    public static abstract class SoftDeletable {
        private Boolean deleted = false;
        private LocalDateTime deletedAt;
        private String deletedBy;

        public Boolean getDeleted() { return deleted; }
        public void setDeleted(Boolean deleted) { this.deleted = deleted; }
        public LocalDateTime getDeletedAt() { return deletedAt; }
        public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
        public String getDeletedBy() { return deletedBy; }
        public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
    }

    public static class SoftDeleteListener {

        @PreRemove
        public void preRemove(SoftDeletable entity) {
            entity.setDeleted(true);
            entity.setDeletedAt(LocalDateTime.now());
            entity.setDeletedBy("system");
        }
    }

    @Entity
    @Table(name = "orders")
    public static class Order extends SoftDeletable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String orderNumber;
        private Double total;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
    }

    public interface OrderRepository extends JpaRepository<Order, Long> {}
}
