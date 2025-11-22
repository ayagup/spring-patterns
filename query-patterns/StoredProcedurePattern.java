package com.example.querypatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Stored Procedure Pattern Implementation
 * 
 * Demonstrates calling stored procedures using @NamedStoredProcedureQuery and @Procedure.
 * 
 * Key Components:
 * - @NamedStoredProcedureQuery for named procedures
 * - @Procedure annotation in repository
 * - @StoredProcedureParameter for parameters
 * - IN, OUT, INOUT parameters support
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class StoredProcedurePattern {

    public static void main(String[] args) {
        SpringApplication.run(StoredProcedurePattern.class, args);
    }

    /**
     * Transaction Entity with Stored Procedures
     */
    @Entity
    @Table(name = "transactions")
    @NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(
            name = "Transaction.getTotalByType",
            procedureName = "get_total_by_type",
            parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "txn_type", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "total", type = BigDecimal.class)
            }
        ),
        @NamedStoredProcedureQuery(
            name = "Transaction.getStats",
            procedureName = "get_transaction_stats",
            resultClasses = Transaction.class
        )
    })
    public static class Transaction {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        private String type;
        private BigDecimal amount;
        private String description;
        private String status;
        
        public Transaction() {}
        
        public Transaction(String type, BigDecimal amount, String description) {
            this.type = type;
            this.amount = amount;
            this.description = description;
            this.status = "COMPLETED";
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    /**
     * Repository with Stored Procedure calls
     */
    @Repository
    public interface TransactionRepository extends JpaRepository<Transaction, Long> {
        
        /**
         * Call stored procedure using @Procedure
         */
        @Procedure(name = "Transaction.getTotalByType")
        BigDecimal getTotalByType(@Param("txn_type") String type);
        
        /**
         * Alternative: using procedureName directly
         */
        @Procedure(procedureName = "get_transaction_stats")
        List<Transaction> getTransactionStats();
        
        /**
         * Native query calling stored procedure
         */
        @Query(value = "CALL calculate_totals(:type)", nativeQuery = true)
        void calculateTotals(@Param("type") String type);
    }
    
    /**
     * Service using Stored Procedures
     */
    @Service
    @Transactional
    public static class TransactionProcedureService {
        
        @PersistenceContext
        private EntityManager entityManager;
        
        private final TransactionRepository transactionRepository;
        
        public TransactionProcedureService(TransactionRepository transactionRepository) {
            this.transactionRepository = transactionRepository;
        }
        
        /**
         * Call stored procedure using repository
         */
        public BigDecimal getTotalByType(String type) {
            return transactionRepository.getTotalByType(type);
        }
        
        /**
         * Call stored procedure using EntityManager
         */
        public BigDecimal getTotalUsingEntityManager(String type) {
            StoredProcedureQuery query = entityManager
                .createNamedStoredProcedureQuery("Transaction.getTotalByType");
            query.setParameter("txn_type", type);
            query.execute();
            return (BigDecimal) query.getOutputParameterValue("total");
        }
        
        /**
         * Call stored procedure with result set
         */
        public List<Transaction> getStatistics() {
            return transactionRepository.getTransactionStats();
        }
        
        /**
         * Call procedure using createStoredProcedureQuery
         */
        public void processTransactions(String type) {
            StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("process_transactions");
            query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
            query.setParameter(1, type);
            query.execute();
        }
    }
    
    @RestController
    @RequestMapping("/api/procedure/transactions")
    public static class TransactionProcedureController {
        
        private final TransactionProcedureService procedureService;
        private final TransactionRepository transactionRepository;
        
        public TransactionProcedureController(TransactionProcedureService procedureService,
                                             TransactionRepository transactionRepository) {
            this.procedureService = procedureService;
            this.transactionRepository = transactionRepository;
        }
        
        @PostMapping
        public Transaction create(@RequestBody Transaction transaction) {
            return transactionRepository.save(transaction);
        }
        
        @GetMapping("/total/{type}")
        public BigDecimal getTotalByType(@PathVariable String type) {
            return procedureService.getTotalByType(type);
        }
        
        @GetMapping("/stats")
        public List<Transaction> getStats() {
            return procedureService.getStatistics();
        }
        
        @PostMapping("/process/{type}")
        public void process(@PathVariable String type) {
            procedureService.processTransactions(type);
        }
    }
}

/**
 * Example MySQL Stored Procedures:
 * 
 * DELIMITER //
 * 
 * CREATE PROCEDURE get_total_by_type(
 *     IN txn_type VARCHAR(255),
 *     OUT total DECIMAL(19, 2)
 * )
 * BEGIN
 *     SELECT SUM(amount) INTO total
 *     FROM transactions
 *     WHERE type = txn_type;
 * END //
 * 
 * CREATE PROCEDURE get_transaction_stats()
 * BEGIN
 *     SELECT * FROM transactions WHERE status = 'COMPLETED';
 * END //
 * 
 * DELIMITER ;
 * 
 * Best Practices:
 * 1. Use for complex business logic in database
 * 2. Define procedures at entity level for clarity
 * 3. Handle NULL values in procedures
 * 4. Test procedures independently
 * 5. Document procedure behavior
 * 6. Consider database portability issues
 * 7. Use transactions appropriately
 * 8. Handle exceptions from procedures
 */
