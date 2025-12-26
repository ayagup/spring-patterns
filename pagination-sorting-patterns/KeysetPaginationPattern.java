package com.example.pagination.keyset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;
import java.util.List;

/**
 * Keyset Pagination Pattern
 * 
 * Uses unique key combinations for pagination (better than offset).
 * Provides consistent results even when data changes.
 * Also known as "seek method" pagination.
 */
@SpringBootApplication
public class KeysetPaginationPattern {

    public static void main(String[] args) {
        SpringApplication.run(KeysetPaginationPattern.class, args);
    }

    @Entity
    @Table(name = "orders", indexes = {
        @Index(name = "idx_created_id", columnList = "createdAt,id")
    })
    public static class Order {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String orderNumber;
        private Double amount;
        
        @Column(name = "createdAt")
        private java.time.LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public interface OrderRepository extends JpaRepository<Order, Long> {
        
        @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC, o.id DESC")
        List<Order> findFirstPage(org.springframework.data.domain.Pageable pageable);
        
        @Query("SELECT o FROM Order o WHERE o.createdAt < :createdAt OR " +
               "(o.createdAt = :createdAt AND o.id < :id) " +
               "ORDER BY o.createdAt DESC, o.id DESC")
        List<Order> findNextPage(
            java.time.LocalDateTime createdAt, 
            Long id, 
            org.springframework.data.domain.Pageable pageable
        );
    }

    @RestController
    @RequestMapping("/api/orders")
    public static class OrderController {

        private final OrderRepository repository;

        public OrderController(OrderRepository repository) {
            this.repository = repository;
        }

        /**
         * First page
         * GET /api/orders?limit=20
         */
        @GetMapping
        public KeysetResponse<Order> getOrders(
            @RequestParam(defaultValue = "20") int limit
        ) {
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(0, limit);
            
            List<Order> orders = repository.findFirstPage(pageable);
            
            if (orders.isEmpty()) {
                return new KeysetResponse<>(orders, null);
            }
            
            Order lastOrder = orders.get(orders.size() - 1);
            String nextKey = lastOrder.getCreatedAt().toString() + "_" + lastOrder.getId();
            
            return new KeysetResponse<>(orders, nextKey);
        }

        /**
         * Next page using keyset
         * GET /api/orders?key=2023-01-01T10:00:00_123&limit=20
         */
        @GetMapping("/next")
        public KeysetResponse<Order> getNextOrders(
            @RequestParam String key,
            @RequestParam(defaultValue = "20") int limit
        ) {
            String[] parts = key.split("_");
            java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(parts[0]);
            Long id = Long.parseLong(parts[1]);
            
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(0, limit);
            
            List<Order> orders = repository.findNextPage(createdAt, id, pageable);
            
            if (orders.isEmpty()) {
                return new KeysetResponse<>(orders, null);
            }
            
            Order lastOrder = orders.get(orders.size() - 1);
            String nextKey = lastOrder.getCreatedAt().toString() + "_" + lastOrder.getId();
            
            return new KeysetResponse<>(orders, nextKey);
        }
    }

    public record KeysetResponse<T>(List<T> data, String nextKey) {}
}
