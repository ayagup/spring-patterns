package com.example.methodsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Post-Authorization Pattern
 * 
 * Demonstrates Spring Security's @PostAuthorize and @PostFilter annotations for:
 * - Method-level security after method execution
 * - Return value-based authorization
 * - Filtering collections based on security
 * - Conditional data access
 * - Audit trail for unauthorized access attempts
 * 
 * Key Features:
 * - Access return value in expressions
 * - Filter collections automatically
 * - Remove unauthorized items
 * - Validate returned data
 * - Log access violations
 * 
 * Use Cases:
 * - Verify ownership of returned objects
 * - Filter results based on user permissions
 * - Ensure data confidentiality
 * - Implement row-level security
 * - Audit data access
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class PostAuthorizationPattern {

    public static void main(String[] args) {
        SpringApplication.run(PostAuthorizationPattern.class, args);
    }

    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder().encode("admin123"))
                    .authorities("ROLE_ADMIN")
                    .build();

            UserDetails user1 = User.builder()
                    .username("user1")
                    .password(passwordEncoder().encode("user123"))
                    .authorities("ROLE_USER")
                    .build();

            UserDetails user2 = User.builder()
                    .username("user2")
                    .password(passwordEncoder().encode("user123"))
                    .authorities("ROLE_USER")
                    .build();

            return new InMemoryUserDetailsManager(admin, user1, user2);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    /**
     * Basic @PostAuthorize usage
     */
    @Service
    public static class AccountService {

        /**
         * Check ownership of returned account
         * returnObject refers to the method return value
         */
        @PostAuthorize("returnObject.owner == authentication.principal.username")
        public Account getAccount(Long accountId) {
            System.out.println("Fetching account: " + accountId);
            
            // Simulate database fetch
            Account account = new Account();
            account.setId(accountId);
            account.setOwner("user1"); // Would come from database
            account.setBalance(new BigDecimal("1000.00"));
            
            return account;
        }

        /**
         * Admin can access any account, others only their own
         */
        @PostAuthorize("hasRole('ADMIN') or returnObject.owner == authentication.principal.username")
        public Account getAccountDetails(Long accountId) {
            System.out.println("Fetching account details: " + accountId);
            
            Account account = new Account();
            account.setId(accountId);
            account.setOwner("user1");
            account.setBalance(new BigDecimal("1500.00"));
            account.setAccountType("SAVINGS");
            
            return account;
        }

        /**
         * Check properties of returned object
         */
        @PostAuthorize("returnObject.active == true")
        public Account getActiveAccount(Long accountId) {
            System.out.println("Fetching active account: " + accountId);
            
            Account account = new Account();
            account.setId(accountId);
            account.setOwner(getCurrentUsername());
            account.setActive(true); // Only active accounts allowed
            
            return account;
        }

        /**
         * Validate business rules on return value
         */
        @PostAuthorize("returnObject.balance.compareTo(new java.math.BigDecimal('0')) >= 0")
        public Account createAccount(String accountType) {
            System.out.println("Creating account: " + accountType);
            
            Account account = new Account();
            account.setId(1L);
            account.setOwner(getCurrentUsername());
            account.setAccountType(accountType);
            account.setBalance(BigDecimal.ZERO);
            account.setActive(true);
            
            return account;
        }

        private String getCurrentUsername() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "anonymous";
        }
    }

    /**
     * @PostFilter for filtering collections
     */
    @Service
    public static class TransactionService {

        /**
         * Filter transactions - only return user's own transactions
         * filterObject refers to each element in the collection
         */
        @PostFilter("filterObject.userId == authentication.principal.username")
        public List<Transaction> getAllTransactions() {
            System.out.println("Fetching all transactions (will be filtered)");
            
            List<Transaction> transactions = new ArrayList<>();
            
            // Transactions for different users
            transactions.add(createTransaction(1L, "user1", "Payment", new BigDecimal("100")));
            transactions.add(createTransaction(2L, "user2", "Withdrawal", new BigDecimal("50")));
            transactions.add(createTransaction(3L, "user1", "Deposit", new BigDecimal("200")));
            transactions.add(createTransaction(4L, "user2", "Payment", new BigDecimal("75")));
            
            // Only transactions for authenticated user will be returned
            return transactions;
        }

        /**
         * Admin sees all, users see only their own
         */
        @PostFilter("hasRole('ADMIN') or filterObject.userId == authentication.principal.username")
        public List<Transaction> getRecentTransactions() {
            System.out.println("Fetching recent transactions");
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(5L, "user1", "Transfer", new BigDecimal("150")));
            transactions.add(createTransaction(6L, "user2", "Deposit", new BigDecimal("300")));
            
            return transactions;
        }

        /**
         * Filter based on transaction properties
         */
        @PostFilter("filterObject.amount.compareTo(new java.math.BigDecimal('100')) <= 0")
        public List<Transaction> getSmallTransactions() {
            System.out.println("Fetching small transactions (amount <= 100)");
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(createTransaction(7L, "user1", "Payment", new BigDecimal("50")));
            transactions.add(createTransaction(8L, "user1", "Payment", new BigDecimal("150"))); // Will be filtered out
            transactions.add(createTransaction(9L, "user1", "Payment", new BigDecimal("75")));
            
            return transactions;
        }

        /**
         * Complex filtering with multiple conditions
         */
        @PostFilter("filterObject.status == 'COMPLETED' and filterObject.userId == authentication.principal.username")
        public List<Transaction> getCompletedTransactions() {
            System.out.println("Fetching completed transactions for current user");
            
            List<Transaction> transactions = new ArrayList<>();
            
            Transaction t1 = createTransaction(10L, "user1", "Payment", new BigDecimal("100"));
            t1.setStatus("COMPLETED");
            transactions.add(t1);
            
            Transaction t2 = createTransaction(11L, "user1", "Payment", new BigDecimal("200"));
            t2.setStatus("PENDING");
            transactions.add(t2);
            
            Transaction t3 = createTransaction(12L, "user2", "Payment", new BigDecimal("150"));
            t3.setStatus("COMPLETED");
            transactions.add(t3);
            
            return transactions;
        }

        private Transaction createTransaction(Long id, String userId, String type, BigDecimal amount) {
            Transaction transaction = new Transaction();
            transaction.setId(id);
            transaction.setUserId(userId);
            transaction.setType(type);
            transaction.setAmount(amount);
            transaction.setTimestamp(LocalDateTime.now());
            transaction.setStatus("COMPLETED");
            return transaction;
        }
    }

    /**
     * Combined @PostAuthorize and @PostFilter
     */
    @Service
    public static class DocumentService {

        /**
         * First verify user can access documents, then filter by ownership
         */
        @PostAuthorize("hasRole('USER')")
        @PostFilter("hasRole('ADMIN') or filterObject.owner == authentication.principal.username")
        public List<Document> getDocuments() {
            System.out.println("Fetching documents (filtered by ownership)");
            
            List<Document> documents = new ArrayList<>();
            documents.add(new Document(1L, "user1", "Document 1", "Confidential"));
            documents.add(new Document(2L, "user2", "Document 2", "Public"));
            documents.add(new Document(3L, "user1", "Document 3", "Internal"));
            
            return documents;
        }

        /**
         * Verify ownership and confidentiality level
         */
        @PostAuthorize("returnObject.owner == authentication.principal.username and returnObject.confidentialityLevel != 'TOP_SECRET'")
        public Document getDocument(Long documentId) {
            System.out.println("Fetching document: " + documentId);
            
            Document doc = new Document(documentId, getCurrentUsername(), "My Document", "CONFIDENTIAL");
            return doc;
        }

        private String getCurrentUsername() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "anonymous";
        }
    }

    /**
     * Advanced post-authorization scenarios
     */
    @Service
    public static class ReportService {

        /**
         * Validate report permissions after generation
         */
        @PostAuthorize("@reportSecurityService.canViewReport(returnObject)")
        public Report generateReport(String reportType) {
            System.out.println("Generating report: " + reportType);
            
            Report report = new Report();
            report.setId(1L);
            report.setType(reportType);
            report.setOwner(getCurrentUsername());
            report.setDepartment("SALES");
            report.setSensitive(false);
            
            return report;
        }

        /**
         * Filter reports based on department access
         */
        @PostFilter("@reportSecurityService.hasAccessToDepartment(filterObject.department)")
        public List<Report> getAllReports() {
            System.out.println("Fetching all reports (filtered by department access)");
            
            List<Report> reports = new ArrayList<>();
            reports.add(createReport(1L, "SALES", "Monthly Sales"));
            reports.add(createReport(2L, "HR", "Employee Review"));
            reports.add(createReport(3L, "FINANCE", "Budget Report"));
            
            return reports;
        }

        private Report createReport(Long id, String department, String title) {
            Report report = new Report();
            report.setId(id);
            report.setDepartment(department);
            report.setType(title);
            report.setOwner(getCurrentUsername());
            return report;
        }

        private String getCurrentUsername() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "anonymous";
        }
    }

    /**
     * Custom security service for post-authorization
     */
    @Service("reportSecurityService")
    public static class ReportSecurityService {

        public boolean canViewReport(Report report) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Admin can view any report
            if (auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }
            
            // Owner can view their own reports
            if (report.getOwner().equals(auth.getName())) {
                return true;
            }
            
            // Non-sensitive reports can be viewed by anyone
            return !report.isSensitive();
        }

        public boolean hasAccessToDepartment(String department) {
            // Check if user has access to department
            // This would typically check database or cache
            System.out.println("Checking department access: " + department);
            return true; // Simplified
        }
    }

    // Domain Classes

    public static class Account {
        private Long id;
        private String owner;
        private BigDecimal balance;
        private String accountType;
        private Boolean active;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }

        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }

        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    public static class Transaction {
        private Long id;
        private String userId;
        private String type;
        private BigDecimal amount;
        private LocalDateTime timestamp;
        private String status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class Document {
        private Long id;
        private String owner;
        private String title;
        private String confidentialityLevel;

        public Document(Long id, String owner, String title, String confidentialityLevel) {
            this.id = id;
            this.owner = owner;
            this.title = title;
            this.confidentialityLevel = confidentialityLevel;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getConfidentialityLevel() { return confidentialityLevel; }
        public void setConfidentialityLevel(String confidentialityLevel) { this.confidentialityLevel = confidentialityLevel; }
    }

    public static class Report {
        private Long id;
        private String type;
        private String owner;
        private String department;
        private boolean sensitive;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public boolean isSensitive() { return sensitive; }
        public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }
    }
}

/**
 * DOCUMENTATION
 * 
 * Post-Authorization Annotations:
 * 
 * 1. @PostAuthorize:
 *    - Evaluated after method execution
 *    - Access return value via 'returnObject'
 *    - Throws AccessDeniedException if fails
 *    - Use for ownership verification
 * 
 * 2. @PostFilter:
 *    - Filters collection return values
 *    - Access each element via 'filterObject'
 *    - Removes unauthorized items
 *    - Original collection is modified
 * 
 * 3. Common Expressions:
 *    - returnObject.property: Check return value properties
 *    - filterObject.property: Check collection element properties
 *    - authentication.principal.username: Current user
 *    - hasRole('ROLE'): Check user role
 *    - @beanName.method(): Custom security logic
 * 
 * 4. Use Cases:
 *    - Ownership verification: returnObject.owner == principal.username
 *    - Row-level security: Filter by user/department/permissions
 *    - Data confidentiality: Check sensitivity level
 *    - Business rule validation: Verify returned data meets criteria
 * 
 * 5. Best Practices:
 *    - Use @PostAuthorize for single objects
 *    - Use @PostFilter for collections
 *    - Keep expressions simple
 *    - Extract complex logic to security services
 *    - Consider performance impact on large collections
 *    - Log access violations for audit
 * 
 * 6. Performance Considerations:
 *    - @PostFilter loads all data then filters
 *    - Consider database-level filtering for large datasets
 *    - Use pagination with filtering
 *    - Cache security checks when possible
 *    - Monitor filter performance
 * 
 * 7. Difference from @PreAuthorize:
 *    - @PreAuthorize: Check before execution (faster)
 *    - @PostAuthorize: Check after execution (access return value)
 *    - Use @PreAuthorize when possible
 *    - Use @PostAuthorize when decision depends on result
 * 
 * 8. Common Patterns:
 *    - Ownership: returnObject.owner == authentication.name
 *    - Admin override: hasRole('ADMIN') or [condition]
 *    - Custom logic: @securityService.canAccess(returnObject)
 *    - Collection filtering: filterObject.userId == principal.username
 */
