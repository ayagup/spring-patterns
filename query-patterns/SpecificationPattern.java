package com.example.querypatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.domain.Specification;
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
 * Specification Pattern Implementation
 * 
 * Demonstrates reusable, composable query specifications using JPA Criteria API.
 * 
 * Key Components:
 * - Specification<T> interface
 * - JpaSpecificationExecutor for repository
 * - Predicate-based query building
 * - Composable specifications (and, or, not)
 * - Type-safe query construction
 * 
 * Benefits:
 * - Reusable query components
 * - Composable specifications
 * - Type-safe at compile time
 * - Clean separation of concerns
 * - Testable query logic
 * 
 * Use Cases:
 * - Complex dynamic queries
 * - Reusable filtering logic
 * - Business rule queries
 * - Advanced search features
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class SpecificationPattern {

    public static void main(String[] args) {
        SpringApplication.run(SpecificationPattern.class, args);
    }

    /**
     * Invoice Entity
     */
    @Entity
    @Table(name = "invoices")
    public static class Invoice {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @Column(name = "invoice_number", unique = true, nullable = false)
        private String invoiceNumber;
        
        @Column(name = "customer_name", nullable = false)
        private String customerName;
        
        @Column(nullable = false)
        private BigDecimal amount;
        
        @Column(name = "issue_date", nullable = false)
        private LocalDate issueDate;
        
        @Column(name = "due_date", nullable = false)
        private LocalDate dueDate;
        
        @Column(name = "payment_date")
        private LocalDate paymentDate;
        
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private InvoiceStatus status;
        
        @Column(name = "tax_amount")
        private BigDecimal taxAmount;
        
        private String category;
        
        private String description;
        
        // Constructors
        public Invoice() {
            this.status = InvoiceStatus.DRAFT;
            this.issueDate = LocalDate.now();
            this.dueDate = LocalDate.now().plusDays(30);
        }
        
        public Invoice(String invoiceNumber, String customerName, BigDecimal amount) {
            this();
            this.invoiceNumber = invoiceNumber;
            this.customerName = customerName;
            this.amount = amount;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public LocalDate getIssueDate() { return issueDate; }
        public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
        
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        
        public LocalDate getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
        
        public InvoiceStatus getStatus() { return status; }
        public void setStatus(InvoiceStatus status) { this.status = status; }
        
        public BigDecimal getTaxAmount() { return taxAmount; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * Invoice Status Enum
     */
    public enum InvoiceStatus {
        DRAFT, SENT, PAID, OVERDUE, CANCELLED
    }
    
    /**
     * Repository with Specification support
     */
    @Repository
    public interface InvoiceRepository extends JpaRepository<Invoice, Long>, 
                                               JpaSpecificationExecutor<Invoice> {
    }
    
    /**
     * Invoice Specifications
     * 
     * Contains reusable specification factory methods
     */
    public static class InvoiceSpecs {
        
        /**
         * Specification for status filtering
         */
        public static Specification<Invoice> hasStatus(InvoiceStatus status) {
            return (root, query, criteriaBuilder) -> 
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
        }
        
        /**
         * Specification for customer name filtering
         */
        public static Specification<Invoice> customerNameContains(String customerName) {
            return (root, query, criteriaBuilder) -> {
                if (customerName == null || customerName.isEmpty()) {
                    return null;
                }
                return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("customerName")),
                    "%" + customerName.toLowerCase() + "%"
                );
            };
        }
        
        /**
         * Specification for amount range
         */
        public static Specification<Invoice> amountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
            return (root, query, criteriaBuilder) -> {
                if (minAmount == null && maxAmount == null) {
                    return null;
                }
                if (minAmount != null && maxAmount != null) {
                    return criteriaBuilder.between(root.get("amount"), minAmount, maxAmount);
                }
                if (minAmount != null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount);
                }
                return criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount);
            };
        }
        
        /**
         * Specification for issue date range
         */
        public static Specification<Invoice> issuedBetween(LocalDate startDate, LocalDate endDate) {
            return (root, query, criteriaBuilder) -> {
                if (startDate == null && endDate == null) {
                    return null;
                }
                if (startDate != null && endDate != null) {
                    return criteriaBuilder.between(root.get("issueDate"), startDate, endDate);
                }
                if (startDate != null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("issueDate"), startDate);
                }
                return criteriaBuilder.lessThanOrEqualTo(root.get("issueDate"), endDate);
            };
        }
        
        /**
         * Specification for overdue invoices
         */
        public static Specification<Invoice> isOverdue() {
            return (root, query, criteriaBuilder) -> {
                LocalDate today = LocalDate.now();
                return criteriaBuilder.and(
                    criteriaBuilder.lessThan(root.get("dueDate"), today),
                    criteriaBuilder.isNull(root.get("paymentDate")),
                    criteriaBuilder.notEqual(root.get("status"), InvoiceStatus.PAID),
                    criteriaBuilder.notEqual(root.get("status"), InvoiceStatus.CANCELLED)
                );
            };
        }
        
        /**
         * Specification for paid invoices
         */
        public static Specification<Invoice> isPaid() {
            return (root, query, criteriaBuilder) -> 
                criteriaBuilder.isNotNull(root.get("paymentDate"));
        }
        
        /**
         * Specification for unpaid invoices
         */
        public static Specification<Invoice> isUnpaid() {
            return (root, query, criteriaBuilder) -> 
                criteriaBuilder.isNull(root.get("paymentDate"));
        }
        
        /**
         * Specification for category filtering
         */
        public static Specification<Invoice> hasCategory(String category) {
            return (root, query, criteriaBuilder) -> 
                category == null ? null : criteriaBuilder.equal(root.get("category"), category);
        }
        
        /**
         * Specification for description search
         */
        public static Specification<Invoice> descriptionContains(String keyword) {
            return (root, query, criteriaBuilder) -> {
                if (keyword == null || keyword.isEmpty()) {
                    return null;
                }
                return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + keyword.toLowerCase() + "%"
                );
            };
        }
        
        /**
         * Specification for invoice number pattern
         */
        public static Specification<Invoice> invoiceNumberStartsWith(String prefix) {
            return (root, query, criteriaBuilder) -> {
                if (prefix == null || prefix.isEmpty()) {
                    return null;
                }
                return criteriaBuilder.like(root.get("invoiceNumber"), prefix + "%");
            };
        }
        
        /**
         * Specification for high-value invoices
         */
        public static Specification<Invoice> isHighValue(BigDecimal threshold) {
            return (root, query, criteriaBuilder) -> 
                threshold == null ? null : criteriaBuilder.greaterThan(root.get("amount"), threshold);
        }
        
        /**
         * Specification for due soon (within days)
         */
        public static Specification<Invoice> dueSoon(int days) {
            return (root, query, criteriaBuilder) -> {
                LocalDate futureDate = LocalDate.now().plusDays(days);
                return criteriaBuilder.and(
                    criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), futureDate),
                    criteriaBuilder.isNull(root.get("paymentDate"))
                );
            };
        }
    }
    
    /**
     * Service using Specifications
     */
    @Service
    @Transactional
    public static class InvoiceSpecificationService {
        
        private final InvoiceRepository invoiceRepository;
        
        public InvoiceSpecificationService(InvoiceRepository invoiceRepository) {
            this.invoiceRepository = invoiceRepository;
        }
        
        /**
         * Using single specification
         */
        public List<Invoice> findByStatus(InvoiceStatus status) {
            return invoiceRepository.findAll(InvoiceSpecs.hasStatus(status));
        }
        
        /**
         * Composing specifications with AND
         */
        public List<Invoice> findByStatusAndCustomer(InvoiceStatus status, String customerName) {
            Specification<Invoice> spec = Specification
                .where(InvoiceSpecs.hasStatus(status))
                .and(InvoiceSpecs.customerNameContains(customerName));
            
            return invoiceRepository.findAll(spec);
        }
        
        /**
         * Complex query with multiple composed specifications
         */
        public List<Invoice> searchInvoices(String customerName, InvoiceStatus status,
                                           BigDecimal minAmount, BigDecimal maxAmount,
                                           LocalDate startDate, LocalDate endDate,
                                           String category) {
            Specification<Invoice> spec = Specification
                .where(InvoiceSpecs.customerNameContains(customerName))
                .and(InvoiceSpecs.hasStatus(status))
                .and(InvoiceSpecs.amountBetween(minAmount, maxAmount))
                .and(InvoiceSpecs.issuedBetween(startDate, endDate))
                .and(InvoiceSpecs.hasCategory(category));
            
            return invoiceRepository.findAll(spec);
        }
        
        /**
         * Using OR composition
         */
        public List<Invoice> findOverdueOrHighValue(BigDecimal threshold) {
            Specification<Invoice> spec = Specification
                .where(InvoiceSpecs.isOverdue())
                .or(InvoiceSpecs.isHighValue(threshold));
            
            return invoiceRepository.findAll(spec);
        }
        
        /**
         * Using NOT
         */
        public List<Invoice> findNotPaid() {
            Specification<Invoice> spec = Specification.not(InvoiceSpecs.isPaid());
            return invoiceRepository.findAll(spec);
        }
        
        /**
         * Complex combination with AND, OR, NOT
         */
        public List<Invoice> findComplexCriteria() {
            Specification<Invoice> spec = Specification
                .where(InvoiceSpecs.hasStatus(InvoiceStatus.SENT))
                .and(
                    Specification.where(InvoiceSpecs.isOverdue())
                        .or(InvoiceSpecs.dueSoon(7))
                )
                .and(Specification.not(InvoiceSpecs.isPaid()));
            
            return invoiceRepository.findAll(spec);
        }
        
        /**
         * Count using specifications
         */
        public long countOverdueInvoices() {
            return invoiceRepository.count(InvoiceSpecs.isOverdue());
        }
        
        /**
         * Check existence using specifications
         */
        public boolean hasHighValueInvoices(BigDecimal threshold) {
            return invoiceRepository.exists(InvoiceSpecs.isHighValue(threshold));
        }
        
        /**
         * Find one using specification
         */
        public Invoice findByInvoiceNumber(String invoiceNumber) {
            return invoiceRepository.findOne(
                InvoiceSpecs.invoiceNumberStartsWith(invoiceNumber)
            ).orElse(null);
        }
        
        /**
         * Dynamic specification builder
         */
        public List<Invoice> dynamicSearch(InvoiceSearchCriteria criteria) {
            Specification<Invoice> spec = Specification.where(null);
            
            if (criteria.getCustomerName() != null) {
                spec = spec.and(InvoiceSpecs.customerNameContains(criteria.getCustomerName()));
            }
            
            if (criteria.getStatus() != null) {
                spec = spec.and(InvoiceSpecs.hasStatus(criteria.getStatus()));
            }
            
            if (criteria.getMinAmount() != null || criteria.getMaxAmount() != null) {
                spec = spec.and(InvoiceSpecs.amountBetween(criteria.getMinAmount(), 
                                                          criteria.getMaxAmount()));
            }
            
            if (criteria.getStartDate() != null || criteria.getEndDate() != null) {
                spec = spec.and(InvoiceSpecs.issuedBetween(criteria.getStartDate(), 
                                                          criteria.getEndDate()));
            }
            
            if (criteria.getCategory() != null) {
                spec = spec.and(InvoiceSpecs.hasCategory(criteria.getCategory()));
            }
            
            if (criteria.getIncludeOverdue() != null && criteria.getIncludeOverdue()) {
                spec = spec.and(InvoiceSpecs.isOverdue());
            }
            
            return invoiceRepository.findAll(spec);
        }
    }
    
    /**
     * Search criteria DTO
     */
    public static class InvoiceSearchCriteria {
        private String customerName;
        private InvoiceStatus status;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private LocalDate startDate;
        private LocalDate endDate;
        private String category;
        private Boolean includeOverdue;
        
        // Getters and Setters
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public InvoiceStatus getStatus() { return status; }
        public void setStatus(InvoiceStatus status) { this.status = status; }
        
        public BigDecimal getMinAmount() { return minAmount; }
        public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
        
        public BigDecimal getMaxAmount() { return maxAmount; }
        public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Boolean getIncludeOverdue() { return includeOverdue; }
        public void setIncludeOverdue(Boolean includeOverdue) { this.includeOverdue = includeOverdue; }
    }
    
    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/spec/invoices")
    public static class InvoiceSpecController {
        
        private final InvoiceSpecificationService specService;
        private final InvoiceRepository invoiceRepository;
        
        public InvoiceSpecController(InvoiceSpecificationService specService,
                                    InvoiceRepository invoiceRepository) {
            this.specService = specService;
            this.invoiceRepository = invoiceRepository;
        }
        
        @PostMapping
        public Invoice create(@RequestBody Invoice invoice) {
            return invoiceRepository.save(invoice);
        }
        
        @GetMapping("/status/{status}")
        public List<Invoice> getByStatus(@PathVariable InvoiceStatus status) {
            return specService.findByStatus(status);
        }
        
        @PostMapping("/search")
        public List<Invoice> search(@RequestBody InvoiceSearchCriteria criteria) {
            return specService.dynamicSearch(criteria);
        }
        
        @GetMapping("/overdue")
        public List<Invoice> getOverdue() {
            return specService.findComplexCriteria();
        }
        
        @GetMapping("/count/overdue")
        public long countOverdue() {
            return specService.countOverdueInvoices();
        }
        
        @GetMapping("/high-value")
        public List<Invoice> getHighValue(@RequestParam BigDecimal threshold) {
            return invoiceRepository.findAll(InvoiceSpecs.isHighValue(threshold));
        }
    }
}

/**
 * Configuration (application.properties):
 * 
 * spring.datasource.url=jdbc:h2:mem:specdb
 * spring.datasource.driver-class-name=org.h2.Driver
 * spring.jpa.hibernate.ddl-auto=create-drop
 * spring.jpa.show-sql=true
 * 
 * Best Practices:
 * 1. Create reusable specification factory methods
 * 2. Use Specification.where() to start chains
 * 3. Handle null values in specifications
 * 4. Compose specifications for complex queries
 * 5. Use descriptive specification method names
 * 6. Test specifications in isolation
 * 7. Consider performance implications
 * 8. Document complex specification logic
 * 9. Use Specification for dynamic queries
 * 10. Combine with Pageable for pagination
 * 
 * Advantages:
 * - Reusable query logic
 * - Composable and testable
 * - Type-safe
 * - Clean separation of concerns
 * - Easy to maintain
 * 
 * When to use Specifications:
 * - Dynamic search filters
 * - Reusable query components
 * - Complex business rules
 * - Need for query composition
 */
