package com.example.querypatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Criteria API Pattern Implementation
 * 
 * Demonstrates JPA Criteria API for dynamic, type-safe queries.
 * 
 * Key Components:
 * - CriteriaBuilder for query construction
 * - CriteriaQuery for defining query structure
 * - Root for entity selection
 * - Predicate for conditions
 * - JpaSpecificationExecutor for repository integration
 * 
 * Benefits:
 * - Type-safe at runtime
 * - Dynamic query construction
 * - No string-based queries
 * - Vendor-neutral (JPA standard)
 * - Complex join support
 * 
 * Use Cases:
 * - Dynamic search filters
 * - Complex conditional queries
 * - Programmatic query construction
 * - Advanced reporting
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class CriteriaAPIPattern {

    public static void main(String[] args) {
        SpringApplication.run(CriteriaAPIPattern.class, args);
    }

    /**
     * Order Entity
     */
    @Entity
    @Table(name = "orders")
    public static class Order {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @Column(name = "order_number", unique = true, nullable = false)
        private String orderNumber;
        
        @Column(name = "customer_name", nullable = false)
        private String customerName;
        
        @Column(name = "customer_email")
        private String customerEmail;
        
        @Column(name = "order_date", nullable = false)
        private LocalDate orderDate;
        
        @Column(name = "total_amount", nullable = false)
        private BigDecimal totalAmount;
        
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private OrderStatus status;
        
        @Column(name = "shipping_address")
        private String shippingAddress;
        
        @Column(name = "shipping_city")
        private String shippingCity;
        
        @Column(name = "shipping_country")
        private String shippingCountry;
        
        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<OrderItem> items = new ArrayList<>();
        
        // Constructors
        public Order() {
            this.orderDate = LocalDate.now();
            this.status = OrderStatus.PENDING;
        }
        
        public Order(String orderNumber, String customerName, BigDecimal totalAmount) {
            this();
            this.orderNumber = orderNumber;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        
        public LocalDate getOrderDate() { return orderDate; }
        public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        
        public String getShippingCity() { return shippingCity; }
        public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }
        
        public String getShippingCountry() { return shippingCountry; }
        public void setShippingCountry(String shippingCountry) { this.shippingCountry = shippingCountry; }
        
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
    }
    
    /**
     * OrderItem Entity
     */
    @Entity
    @Table(name = "order_items")
    public static class OrderItem {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "order_id", nullable = false)
        private Order order;
        
        @Column(name = "product_name", nullable = false)
        private String productName;
        
        @Column(nullable = false)
        private Integer quantity;
        
        @Column(name = "unit_price", nullable = false)
        private BigDecimal unitPrice;
        
        // Constructors, Getters, and Setters
        public OrderItem() {}
        
        public OrderItem(String productName, Integer quantity, BigDecimal unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Order getOrder() { return order; }
        public void setOrder(Order order) { this.order = order; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
    
    /**
     * Order Status Enum
     */
    public enum OrderStatus {
        PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }
    
    /**
     * Repository with Criteria API support
     */
    @Repository
    public interface OrderRepository extends JpaRepository<Order, Long>, 
                                             JpaSpecificationExecutor<Order> {
    }
    
    /**
     * Service with Criteria API Queries
     */
    @Service
    @Transactional
    public static class OrderCriteriaService {
        
        @PersistenceContext
        private EntityManager entityManager;
        
        private final OrderRepository orderRepository;
        
        public OrderCriteriaService(OrderRepository orderRepository) {
            this.orderRepository = orderRepository;
        }
        
        /**
         * Simple criteria query
         */
        public List<Order> findOrdersByStatus(OrderStatus status) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Order> query = cb.createQuery(Order.class);
            Root<Order> root = query.from(Order.class);
            
            query.select(root)
                 .where(cb.equal(root.get("status"), status));
            
            return entityManager.createQuery(query).getResultList();
        }
        
        /**
         * Dynamic query with multiple optional parameters
         */
        public List<Order> searchOrders(String customerName, OrderStatus status,
                                       LocalDate startDate, LocalDate endDate,
                                       BigDecimal minAmount, BigDecimal maxAmount) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Order> query = cb.createQuery(Order.class);
            Root<Order> root = query.from(Order.class);
            
            List<Predicate> predicates = new ArrayList<>();
            
            if (customerName != null && !customerName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("customerName")), 
                                      "%" + customerName.toLowerCase() + "%"));
            }
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), endDate));
            }
            
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"), minAmount));
            }
            
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount"), maxAmount));
            }
            
            query.select(root)
                 .where(cb.and(predicates.toArray(new Predicate[0])))
                 .orderBy(cb.desc(root.get("orderDate")));
            
            return entityManager.createQuery(query).getResultList();
        }
        
        /**
         * Query with JOIN
         */
        public List<Order> findOrdersContainingProduct(String productName) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Order> query = cb.createQuery(Order.class);
            Root<Order> root = query.from(Order.class);
            
            // Join with OrderItem
            Join<Order, OrderItem> itemJoin = root.join("items", JoinType.INNER);
            
            query.select(root)
                 .distinct(true)
                 .where(cb.like(cb.lower(itemJoin.get("productName")), 
                               "%" + productName.toLowerCase() + "%"));
            
            return entityManager.createQuery(query).getResultList();
        }
        
        /**
         * Aggregate query with GROUP BY
         */
        public List<OrderStatusCount> countOrdersByStatus() {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<OrderStatusCount> query = cb.createQuery(OrderStatusCount.class);
            Root<Order> root = query.from(Order.class);
            
            query.select(cb.construct(
                    OrderStatusCount.class,
                    root.get("status"),
                    cb.count(root)
                 ))
                 .groupBy(root.get("status"));
            
            return entityManager.createQuery(query).getResultList();
        }
        
        /**
         * Query with complex WHERE clause (OR and AND)
         */
        public List<Order> findHighValueOrRecentOrders(BigDecimal highValueThreshold, 
                                                        int recentDays) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Order> query = cb.createQuery(Order.class);
            Root<Order> root = query.from(Order.class);
            
            // High value orders
            Predicate highValue = cb.greaterThan(root.get("totalAmount"), highValueThreshold);
            
            // Recent orders
            LocalDate recentDate = LocalDate.now().minusDays(recentDays);
            Predicate recent = cb.greaterThanOrEqualTo(root.get("orderDate"), recentDate);
            
            query.select(root)
                 .where(cb.or(highValue, recent))
                 .orderBy(cb.desc(root.get("orderDate")));
            
            return entityManager.createQuery(query).getResultList();
        }
        
        /**
         * Subquery example
         */
        public List<Order> findOrdersAboveAverageAmount() {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Order> query = cb.createQuery(Order.class);
            Root<Order> root = query.from(Order.class);
            
            // Subquery for average amount
            Subquery<Double> subquery = query.subquery(Double.class);
            Root<Order> subRoot = subquery.from(Order.class);
            subquery.select(cb.avg(subRoot.get("totalAmount")));
            
            query.select(root)
                 .where(cb.gt(root.get("totalAmount"), subquery));
            
            return entityManager.createQuery(query).getResultList();
        }
        
        /**
         * Query with ordering and pagination
         */
        public List<Order> findOrdersPaginated(int page, int size, String sortBy) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Order> query = cb.createQuery(Order.class);
            Root<Order> root = query.from(Order.class);
            
            query.select(root)
                 .orderBy(cb.desc(root.get(sortBy)));
            
            TypedQuery<Order> typedQuery = entityManager.createQuery(query);
            typedQuery.setFirstResult(page * size);
            typedQuery.setMaxResults(size);
            
            return typedQuery.getResultList();
        }
        
        /**
         * Count query
         */
        public Long countOrdersByCountry(String country) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<Order> root = query.from(Order.class);
            
            query.select(cb.count(root))
                 .where(cb.equal(root.get("shippingCountry"), country));
            
            return entityManager.createQuery(query).getSingleResult();
        }
        
        /**
         * Query with IN clause
         */
        public List<Order> findOrdersByStatuses(List<OrderStatus> statuses) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Order> query = cb.createQuery(Order.class);
            Root<Order> root = query.from(Order.class);
            
            query.select(root)
                 .where(root.get("status").in(statuses));
            
            return entityManager.createQuery(query).getResultList();
        }
        
        /**
         * Update query using CriteriaUpdate
         */
        public int updateOrderStatus(Long orderId, OrderStatus newStatus) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaUpdate<Order> update = cb.createCriteriaUpdate(Order.class);
            Root<Order> root = update.from(Order.class);
            
            update.set(root.get("status"), newStatus)
                  .where(cb.equal(root.get("id"), orderId));
            
            return entityManager.createQuery(update).executeUpdate();
        }
        
        /**
         * Delete query using CriteriaDelete
         */
        public int deleteCancelledOrders() {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaDelete<Order> delete = cb.createCriteriaDelete(Order.class);
            Root<Order> root = delete.from(Order.class);
            
            delete.where(cb.equal(root.get("status"), OrderStatus.CANCELLED));
            
            return entityManager.createQuery(delete).executeUpdate();
        }
        
        /**
         * Complex multi-join query
         */
        public List<Order> findOrdersWithItemCountGreaterThan(int itemCount) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Order> query = cb.createQuery(Order.class);
            Root<Order> root = query.from(Order.class);
            
            Join<Order, OrderItem> itemJoin = root.join("items");
            
            query.select(root)
                 .groupBy(root.get("id"))
                 .having(cb.gt(cb.count(itemJoin), (long) itemCount));
            
            return entityManager.createQuery(query).getResultList();
        }
        
        /**
         * Projection query - selecting specific fields
         */
        public List<OrderSummary> getOrderSummaries() {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<OrderSummary> query = cb.createQuery(OrderSummary.class);
            Root<Order> root = query.from(Order.class);
            
            query.select(cb.construct(
                    OrderSummary.class,
                    root.get("orderNumber"),
                    root.get("customerName"),
                    root.get("totalAmount"),
                    root.get("status")
                 ))
                 .orderBy(cb.desc(root.get("orderDate")));
            
            return entityManager.createQuery(query).getResultList();
        }
    }
    
    /**
     * DTO for order status count
     */
    public static class OrderStatusCount {
        private OrderStatus status;
        private Long count;
        
        public OrderStatusCount(OrderStatus status, Long count) {
            this.status = status;
            this.count = count;
        }
        
        public OrderStatus getStatus() { return status; }
        public Long getCount() { return count; }
    }
    
    /**
     * DTO for order summary
     */
    public static class OrderSummary {
        private String orderNumber;
        private String customerName;
        private BigDecimal totalAmount;
        private OrderStatus status;
        
        public OrderSummary(String orderNumber, String customerName, 
                          BigDecimal totalAmount, OrderStatus status) {
            this.orderNumber = orderNumber;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
            this.status = status;
        }
        
        // Getters
        public String getOrderNumber() { return orderNumber; }
        public String getCustomerName() { return customerName; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public OrderStatus getStatus() { return status; }
    }
    
    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/criteria/orders")
    public static class OrderCriteriaController {
        
        private final OrderCriteriaService criteriaService;
        
        public OrderCriteriaController(OrderCriteriaService criteriaService) {
            this.criteriaService = criteriaService;
        }
        
        @GetMapping("/status/{status}")
        public List<Order> getByStatus(@PathVariable OrderStatus status) {
            return criteriaService.findOrdersByStatus(status);
        }
        
        @GetMapping("/search")
        public List<Order> search(
                @RequestParam(required = false) String customerName,
                @RequestParam(required = false) OrderStatus status,
                @RequestParam(required = false) LocalDate startDate,
                @RequestParam(required = false) LocalDate endDate,
                @RequestParam(required = false) BigDecimal minAmount,
                @RequestParam(required = false) BigDecimal maxAmount) {
            return criteriaService.searchOrders(customerName, status, startDate, 
                                               endDate, minAmount, maxAmount);
        }
        
        @GetMapping("/product/{productName}")
        public List<Order> getByProduct(@PathVariable String productName) {
            return criteriaService.findOrdersContainingProduct(productName);
        }
        
        @GetMapping("/stats/by-status")
        public List<OrderStatusCount> getStatusStats() {
            return criteriaService.countOrdersByStatus();
        }
        
        @GetMapping("/above-average")
        public List<Order> getAboveAverage() {
            return criteriaService.findOrdersAboveAverageAmount();
        }
        
        @GetMapping("/summaries")
        public List<OrderSummary> getSummaries() {
            return criteriaService.getOrderSummaries();
        }
    }
}

/**
 * Configuration (application.properties):
 * 
 * spring.datasource.url=jdbc:h2:mem:criteriadb
 * spring.datasource.driver-class-name=org.h2.Driver
 * spring.jpa.hibernate.ddl-auto=create-drop
 * spring.jpa.show-sql=true
 * spring.jpa.properties.hibernate.format_sql=true
 * 
 * Best Practices:
 * 1. Use CriteriaBuilder for all query construction
 * 2. Build predicates dynamically based on input
 * 3. Use Metamodel for type-safe attribute access
 * 4. Combine with JpaSpecificationExecutor for cleaner code
 * 5. Cache frequently used queries
 * 6. Use fetch joins to avoid N+1 problems
 * 7. Test with different data scenarios
 * 8. Consider query performance with EXPLAIN PLAN
 * 9. Use DTO projections for read-only queries
 * 10. Implement proper pagination for large result sets
 */
