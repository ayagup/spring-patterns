package com.example.querypatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Native Query Pattern Implementation
 * 
 * Demonstrates native SQL queries using @Query annotation with nativeQuery=true.
 * 
 * Key Components:
 * - @Query with nativeQuery=true
 * - Database-specific SQL syntax
 * - @Modifying for DML operations
 * - @Param for named parameters
 * - ResultSetMapping for complex results
 * 
 * Benefits:
 * - Full SQL power and database features
 * - Database-specific optimizations
 * - Complex queries not supported by JPQL
 * - Better performance for specific cases
 * - Direct database function usage
 * 
 * Use Cases:
 * - Database-specific features (window functions, CTEs)
 * - Complex reporting queries
 * - Performance-critical operations
 * - Legacy database integration
 * - Bulk operations
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class NativeQueryPattern {

    public static void main(String[] args) {
        SpringApplication.run(NativeQueryPattern.class, args);
    }

    /**
     * Account Entity
     */
    @Entity
    @Table(name = "accounts")
    public static class Account {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @Column(name = "account_number", unique = true, nullable = false)
        private String accountNumber;
        
        @Column(name = "account_holder", nullable = false)
        private String accountHolder;
        
        @Enumerated(EnumType.STRING)
        @Column(name = "account_type", nullable = false)
        private AccountType accountType;
        
        @Column(nullable = false)
        private BigDecimal balance;
        
        @Column(nullable = false)
        private String currency;
        
        @Column(name = "interest_rate")
        private BigDecimal interestRate;
        
        @Column(name = "open_date", nullable = false)
        private LocalDate openDate;
        
        @Column(name = "last_transaction_date")
        private LocalDate lastTransactionDate;
        
        @Column(nullable = false)
        private Boolean active;
        
        @Column(name = "branch_code")
        private String branchCode;
        
        // Constructors
        public Account() {
            this.active = true;
            this.openDate = LocalDate.now();
            this.balance = BigDecimal.ZERO;
            this.currency = "USD";
        }
        
        public Account(String accountNumber, String accountHolder, AccountType accountType) {
            this();
            this.accountNumber = accountNumber;
            this.accountHolder = accountHolder;
            this.accountType = accountType;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public String getAccountHolder() { return accountHolder; }
        public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
        
        public AccountType getAccountType() { return accountType; }
        public void setAccountType(AccountType accountType) { this.accountType = accountType; }
        
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public BigDecimal getInterestRate() { return interestRate; }
        public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
        
        public LocalDate getOpenDate() { return openDate; }
        public void setOpenDate(LocalDate openDate) { this.openDate = openDate; }
        
        public LocalDate getLastTransactionDate() { return lastTransactionDate; }
        public void setLastTransactionDate(LocalDate lastTransactionDate) { 
            this.lastTransactionDate = lastTransactionDate; 
        }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        public String getBranchCode() { return branchCode; }
        public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
    }
    
    /**
     * Account Type Enum
     */
    public enum AccountType {
        CHECKING, SAVINGS, BUSINESS, INVESTMENT
    }
    
    /**
     * DTO for Account Summary
     */
    public static class AccountSummary {
        private String accountType;
        private Long totalAccounts;
        private BigDecimal totalBalance;
        private BigDecimal avgBalance;
        
        public AccountSummary(String accountType, Long totalAccounts, 
                            BigDecimal totalBalance, BigDecimal avgBalance) {
            this.accountType = accountType;
            this.totalAccounts = totalAccounts;
            this.totalBalance = totalBalance;
            this.avgBalance = avgBalance;
        }
        
        // Getters
        public String getAccountType() { return accountType; }
        public Long getTotalAccounts() { return totalAccounts; }
        public BigDecimal getTotalBalance() { return totalBalance; }
        public BigDecimal getAvgBalance() { return avgBalance; }
    }
    
    /**
     * Repository with Native Queries
     */
    @Repository
    public interface AccountRepository extends JpaRepository<Account, Long> {
        
        /**
         * Simple native query
         */
        @Query(value = "SELECT * FROM accounts WHERE active = true", nativeQuery = true)
        List<Account> findAllActiveNative();
        
        /**
         * Native query with parameter
         */
        @Query(value = "SELECT * FROM accounts WHERE account_type = ?1", nativeQuery = true)
        List<Account> findByAccountTypeNative(String accountType);
        
        /**
         * Native query with named parameter
         */
        @Query(value = "SELECT * FROM accounts WHERE balance >= :minBalance AND active = true " +
                      "ORDER BY balance DESC", nativeQuery = true)
        List<Account> findByMinimumBalanceNative(@Param("minBalance") BigDecimal minBalance);
        
        /**
         * Native query with multiple parameters
         */
        @Query(value = "SELECT * FROM accounts WHERE account_type = :type " +
                      "AND balance BETWEEN :minBalance AND :maxBalance", nativeQuery = true)
        List<Account> findByTypeAndBalanceRangeNative(
            @Param("type") String type,
            @Param("minBalance") BigDecimal minBalance,
            @Param("maxBalance") BigDecimal maxBalance
        );
        
        /**
         * Native query with LIKE
         */
        @Query(value = "SELECT * FROM accounts WHERE LOWER(account_holder) LIKE LOWER(CONCAT('%', :name, '%'))", 
               nativeQuery = true)
        List<Account> searchByHolderNameNative(@Param("name") String name);
        
        /**
         * Native query with JOIN
         */
        @Query(value = "SELECT a.* FROM accounts a " +
                      "WHERE a.branch_code IN " +
                      "(SELECT branch_code FROM branches WHERE city = :city)", 
               nativeQuery = true)
        List<Account> findByBranchCityNative(@Param("city") String city);
        
        /**
         * Native query with aggregation
         */
        @Query(value = "SELECT COUNT(*) FROM accounts WHERE account_type = :type AND active = true", 
               nativeQuery = true)
        Long countByTypeNative(@Param("type") String type);
        
        /**
         * Native query with GROUP BY
         */
        @Query(value = "SELECT account_type, COUNT(*) as count, SUM(balance) as total_balance, " +
                      "AVG(balance) as avg_balance FROM accounts WHERE active = true " +
                      "GROUP BY account_type", nativeQuery = true)
        List<Object[]> getAccountStatisticsNative();
        
        /**
         * Native update query
         */
        @Modifying
        @Query(value = "UPDATE accounts SET balance = balance + :amount " +
                      "WHERE account_number = :accountNumber", nativeQuery = true)
        int updateBalanceNative(@Param("accountNumber") String accountNumber, 
                               @Param("amount") BigDecimal amount);
        
        /**
         * Native delete query
         */
        @Modifying
        @Query(value = "DELETE FROM accounts WHERE active = false " +
                      "AND last_transaction_date < :cutoffDate", nativeQuery = true)
        int deleteInactiveAccountsNative(@Param("cutoffDate") LocalDate cutoffDate);
        
        /**
         * Database-specific function (PostgreSQL example)
         */
        @Query(value = "SELECT * FROM accounts " +
                      "WHERE open_date >= CURRENT_DATE - INTERVAL ':days days'", nativeQuery = true)
        List<Account> findRecentAccountsNative(@Param("days") int days);
        
        /**
         * CTE (Common Table Expression) - PostgreSQL/MySQL 8+
         */
        @Query(value = "WITH high_value_accounts AS ( " +
                      "  SELECT * FROM accounts WHERE balance > :threshold " +
                      ") " +
                      "SELECT * FROM high_value_accounts WHERE active = true", nativeQuery = true)
        List<Account> findHighValueAccountsNative(@Param("threshold") BigDecimal threshold);
        
        /**
         * Window function (PostgreSQL/MySQL 8+)
         */
        @Query(value = "SELECT *, " +
                      "ROW_NUMBER() OVER (PARTITION BY account_type ORDER BY balance DESC) as rank " +
                      "FROM accounts WHERE active = true", nativeQuery = true)
        List<Object[]> findAccountsWithRankingNative();
        
        /**
         * Subquery in native SQL
         */
        @Query(value = "SELECT * FROM accounts WHERE balance > " +
                      "(SELECT AVG(balance) FROM accounts WHERE account_type = :type)", 
               nativeQuery = true)
        List<Account> findAboveAverageByTypeNative(@Param("type") String type);
        
        /**
         * CASE statement
         */
        @Query(value = "SELECT *, " +
                      "CASE " +
                      "  WHEN balance < 1000 THEN 'LOW' " +
                      "  WHEN balance < 10000 THEN 'MEDIUM' " +
                      "  ELSE 'HIGH' " +
                      "END as balance_category " +
                      "FROM accounts", nativeQuery = true)
        List<Object[]> findAccountsWithCategoryNative();
        
        /**
         * Date functions
         */
        @Query(value = "SELECT * FROM accounts " +
                      "WHERE EXTRACT(YEAR FROM open_date) = :year", nativeQuery = true)
        List<Account> findByOpenYearNative(@Param("year") int year);
        
        /**
         * String functions
         */
        @Query(value = "SELECT * FROM accounts " +
                      "WHERE LENGTH(account_number) = :length", nativeQuery = true)
        List<Account> findByAccountNumberLengthNative(@Param("length") int length);
        
        /**
         * IN clause with collection
         */
        @Query(value = "SELECT * FROM accounts WHERE branch_code IN (:branchCodes)", 
               nativeQuery = true)
        List<Account> findByBranchCodesNative(@Param("branchCodes") List<String> branchCodes);
        
        /**
         * LIMIT/TOP for pagination (MySQL/PostgreSQL)
         */
        @Query(value = "SELECT * FROM accounts WHERE active = true " +
                      "ORDER BY balance DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
        List<Account> findTopAccountsPaginatedNative(@Param("limit") int limit, 
                                                     @Param("offset") int offset);
    }
    
    /**
     * Service using Native Queries
     */
    @Service
    @Transactional
    public static class AccountNativeQueryService {
        
        private final AccountRepository accountRepository;
        
        public AccountNativeQueryService(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }
        
        public List<Account> getAllActiveAccounts() {
            return accountRepository.findAllActiveNative();
        }
        
        public List<Account> getAccountsByType(AccountType accountType) {
            return accountRepository.findByAccountTypeNative(accountType.name());
        }
        
        public List<Account> getHighBalanceAccounts(BigDecimal minBalance) {
            return accountRepository.findByMinimumBalanceNative(minBalance);
        }
        
        public List<Account> searchAccounts(AccountType type, BigDecimal minBalance, 
                                           BigDecimal maxBalance) {
            return accountRepository.findByTypeAndBalanceRangeNative(
                type.name(), minBalance, maxBalance);
        }
        
        public List<Account> searchByHolderName(String name) {
            return accountRepository.searchByHolderNameNative(name);
        }
        
        public Long countAccountsByType(AccountType type) {
            return accountRepository.countByTypeNative(type.name());
        }
        
        public List<AccountSummary> getAccountStatistics() {
            List<Object[]> results = accountRepository.getAccountStatisticsNative();
            return results.stream()
                .map(row -> new AccountSummary(
                    (String) row[0],
                    ((Number) row[1]).longValue(),
                    (BigDecimal) row[2],
                    (BigDecimal) row[3]
                ))
                .toList();
        }
        
        public int depositFunds(String accountNumber, BigDecimal amount) {
            return accountRepository.updateBalanceNative(accountNumber, amount);
        }
        
        public int cleanupInactiveAccounts(LocalDate cutoffDate) {
            return accountRepository.deleteInactiveAccountsNative(cutoffDate);
        }
        
        public List<Account> getRecentAccounts(int days) {
            return accountRepository.findRecentAccountsNative(days);
        }
        
        public List<Account> getHighValueAccounts(BigDecimal threshold) {
            return accountRepository.findHighValueAccountsNative(threshold);
        }
        
        public List<Account> getAboveAverageByType(AccountType type) {
            return accountRepository.findAboveAverageByTypeNative(type.name());
        }
        
        public List<Account> getAccountsByYear(int year) {
            return accountRepository.findByOpenYearNative(year);
        }
        
        public List<Account> getAccountsByBranches(List<String> branchCodes) {
            return accountRepository.findByBranchCodesNative(branchCodes);
        }
        
        public List<Account> getTopAccounts(int page, int size) {
            return accountRepository.findTopAccountsPaginatedNative(size, page * size);
        }
    }
    
    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/native/accounts")
    public static class AccountNativeController {
        
        private final AccountNativeQueryService nativeQueryService;
        private final AccountRepository accountRepository;
        
        public AccountNativeController(AccountNativeQueryService nativeQueryService,
                                      AccountRepository accountRepository) {
            this.nativeQueryService = nativeQueryService;
            this.accountRepository = accountRepository;
        }
        
        @PostMapping
        public Account create(@RequestBody Account account) {
            return accountRepository.save(account);
        }
        
        @GetMapping("/active")
        public List<Account> getAllActive() {
            return nativeQueryService.getAllActiveAccounts();
        }
        
        @GetMapping("/type/{type}")
        public List<Account> getByType(@PathVariable AccountType type) {
            return nativeQueryService.getAccountsByType(type);
        }
        
        @GetMapping("/high-balance")
        public List<Account> getHighBalance(@RequestParam BigDecimal minBalance) {
            return nativeQueryService.getHighBalanceAccounts(minBalance);
        }
        
        @GetMapping("/search")
        public List<Account> search(
                @RequestParam AccountType type,
                @RequestParam BigDecimal minBalance,
                @RequestParam BigDecimal maxBalance) {
            return nativeQueryService.searchAccounts(type, minBalance, maxBalance);
        }
        
        @GetMapping("/holder/{name}")
        public List<Account> searchByHolder(@PathVariable String name) {
            return nativeQueryService.searchByHolderName(name);
        }
        
        @GetMapping("/stats")
        public List<AccountSummary> getStatistics() {
            return nativeQueryService.getAccountStatistics();
        }
        
        @PutMapping("/deposit/{accountNumber}")
        public int deposit(@PathVariable String accountNumber, @RequestParam BigDecimal amount) {
            return nativeQueryService.depositFunds(accountNumber, amount);
        }
        
        @GetMapping("/recent")
        public List<Account> getRecent(@RequestParam(defaultValue = "30") int days) {
            return nativeQueryService.getRecentAccounts(days);
        }
        
        @GetMapping("/high-value")
        public List<Account> getHighValue(@RequestParam BigDecimal threshold) {
            return nativeQueryService.getHighValueAccounts(threshold);
        }
    }
}

/**
 * Configuration (application.properties):
 * 
 * spring.datasource.url=jdbc:h2:mem:nativedb
 * spring.datasource.driver-class-name=org.h2.Driver
 * spring.jpa.hibernate.ddl-auto=create-drop
 * spring.jpa.show-sql=true
 * spring.jpa.properties.hibernate.format_sql=true
 * 
 * Best Practices:
 * 1. Use native queries only when necessary
 * 2. Be aware of database portability issues
 * 3. Validate SQL syntax for target database
 * 4. Use @Modifying for UPDATE/DELETE queries
 * 5. Consider query performance and indexes
 * 6. Document database-specific features used
 * 7. Test with actual database, not just H2
 * 8. Use named parameters for better readability
 * 9. Handle null values properly
 * 10. Consider using stored procedures for complex logic
 * 
 * When to use Native Queries:
 * - Database-specific features (CTEs, window functions)
 * - Complex reporting requirements
 * - Performance optimization needs
 * - Legacy database integration
 * - Bulk operations
 * - Custom SQL functions
 * 
 * When NOT to use:
 * - Simple CRUD operations (use JPQL/derived queries)
 * - Need database portability
 * - Can be achieved with JPQL/Criteria API
 * - For entity relationship navigation
 */
