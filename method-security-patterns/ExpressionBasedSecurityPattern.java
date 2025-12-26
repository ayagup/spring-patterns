package com.example.methodsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

/**
 * Expression-Based Security Pattern
 * 
 * Demonstrates advanced SpEL (Spring Expression Language) for security with:
 * - Complex conditional expressions
 * - Custom security expressions
 * - Method parameter evaluation
 * - Return value checking
 * - Bean method invocation
 * 
 * Key Features:
 * - Full SpEL power in security annotations
 * - Combining multiple conditions
 * - Dynamic security rules
 * - Business logic integration
 * - Custom expression handlers
 * 
 * Use Cases:
 * - Complex authorization rules
 * - Multi-condition security checks
 * - Dynamic permission evaluation
 * - Business rule enforcement
 * - Context-aware security
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class ExpressionBasedSecurityPattern {

    public static void main(String[] args) {
        SpringApplication.run(ExpressionBasedSecurityPattern.class, args);
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
                    .roles("ADMIN")
                    .build();

            UserDetails user1 = User.builder()
                    .username("user1")
                    .password(passwordEncoder().encode("user123"))
                    .roles("USER")
                    .build();

            UserDetails user2 = User.builder()
                    .username("user2")
                    .password(passwordEncoder().encode("user123"))
                    .roles("USER")
                    .build();

            return new InMemoryUserDetailsManager(admin, user1, user2);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    /**
     * Conditional expressions with operators
     */
    @Service
    public static class TransactionService {

        /**
         * Amount-based authorization
         * Regular users can only transfer up to $1000
         */
        @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #amount <= 1000)")
        public void transfer(String fromAccount, String toAccount, double amount) {
            System.out.println("Transfer: $" + amount + " from " + fromAccount + " to " + toAccount);
        }

        /**
         * Multiple conditions with AND
         */
        @PreAuthorize("hasRole('USER') and #amount > 0 and #amount <= 10000")
        public void deposit(String account, double amount) {
            System.out.println("Deposit: $" + amount + " to " + account);
        }

        /**
         * Comparison operators
         */
        @PreAuthorize("#transaction.amount.compareTo(new java.math.BigDecimal('5000')) <= 0 or hasRole('ADMIN')")
        public void processTransaction(Transaction transaction) {
            System.out.println("Processing transaction: " + transaction.getId());
        }

        /**
         * Null checks and safe navigation
         */
        @PreAuthorize("#account != null and #account.isActive() == true")
        public void processAccount(Account account) {
            System.out.println("Processing account: " + account.getId());
        }

        /**
         * String operations
         */
        @PreAuthorize("#accountId.startsWith('VIP') or hasRole('ADMIN')")
        public void vipOperation(String accountId) {
            System.out.println("VIP operation for: " + accountId);
        }
    }

    /**
     * Time-based and contextual expressions
     */
    @Service
    public static class ScheduledService {

        /**
         * Time-based access control
         * Regular users only during business hours
         */
        @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and " +
                      "T(java.time.LocalTime).now().hour >= 9 and " +
                      "T(java.time.LocalTime).now().hour < 17)")
        public void performBusinessHoursOperation() {
            System.out.println("Operation during business hours");
        }

        /**
         * Day of week check
         */
        @PreAuthorize("hasRole('ADMIN') or " +
                      "T(java.time.LocalDate).now().dayOfWeek.value < 6")
        public void weekdayOperation() {
            System.out.println("Weekday operation");
        }

        /**
         * Date range check
         */
        @PreAuthorize("#startDate.isBefore(#endDate)")
        public void scheduleTask(java.time.LocalDate startDate, java.time.LocalDate endDate) {
            System.out.println("Scheduling task from " + startDate + " to " + endDate);
        }
    }

    /**
     * Custom bean method invocation
     */
    @Service
    public static class DocumentService {

        /**
         * Call custom security service method
         */
        @PreAuthorize("@securityExpressionService.canAccessDocument(#documentId, authentication)")
        public Document getDocument(Long documentId) {
            System.out.println("Accessing document: " + documentId);
            return new Document(documentId, "Sample Doc", "user1");
        }

        /**
         * Combine bean method with role check
         */
        @PreAuthorize("hasRole('ADMIN') or @securityExpressionService.isOwner(#documentId, authentication.name)")
        public void updateDocument(Long documentId, String content) {
            System.out.println("Updating document: " + documentId);
        }

        /**
         * Multiple bean method calls
         */
        @PreAuthorize("@securityExpressionService.isOwner(#documentId, authentication.name) and " +
                      "@securityExpressionService.isDocumentEditable(#documentId)")
        public void editDocument(Long documentId, String content) {
            System.out.println("Editing document: " + documentId);
        }

        /**
         * Bean property access
         */
        @PreAuthorize("@environment.getProperty('feature.document.edit') == 'true'")
        public void experimentalEdit(Long documentId) {
            System.out.println("Using experimental edit feature");
        }
    }

    /**
     * Collection operations in expressions
     */
    @Service
    public static class GroupService {

        /**
         * Check if user is in allowed list
         */
        @PreAuthorize("#allowedUsers.contains(authentication.principal.username)")
        public void restrictedGroupOperation(List<String> allowedUsers) {
            System.out.println("Performing restricted group operation");
        }

        /**
         * Check collection size
         */
        @PreAuthorize("#members.size() <= 10 or hasRole('ADMIN')")
        public void createGroup(List<String> members) {
            System.out.println("Creating group with " + members.size() + " members");
        }

        /**
         * Collection filtering with matches
         */
        @PreAuthorize("hasRole('ADMIN') or " +
                      "#groupIds.?[#this > 100].size() == 0")
        public void accessGroups(List<Long> groupIds) {
            System.out.println("Accessing groups: " + groupIds);
        }
    }

    /**
     * Object property navigation
     */
    @Service
    public static class OrderService {

        /**
         * Navigate object properties
         */
        @PreAuthorize("#order.customer.username == authentication.principal.username")
        public void processOrder(Order order) {
            System.out.println("Processing order: " + order.getId());
        }

        /**
         * Complex object graph navigation
         */
        @PreAuthorize("#order.customer.username == authentication.name and " +
                      "#order.total.compareTo(new java.math.BigDecimal('1000')) <= 0")
        public void approveOrder(Order order) {
            System.out.println("Approving order: " + order.getId());
        }

        /**
         * Null-safe navigation
         */
        @PreAuthorize("#order.shippingAddress?.country == 'USA'")
        public void domesticShipping(Order order) {
            System.out.println("Domestic shipping for order: " + order.getId());
        }
    }

    /**
     * Ternary and conditional expressions
     */
    @Service
    public static class SubscriptionService {

        /**
         * Ternary operator in expression
         */
        @PreAuthorize("#premium ? hasRole('USER') : hasRole('ADMIN')")
        public void accessFeature(boolean premium) {
            System.out.println("Accessing " + (premium ? "premium" : "basic") + " feature");
        }

        /**
         * Elvis operator for defaults
         */
        @PreAuthorize("(#role ?: 'USER') == 'ADMIN' or hasRole('SUPER_ADMIN')")
        public void manageSubscription(String role) {
            System.out.println("Managing subscription");
        }
    }

    /**
     * Regular expression matching
     */
    @Service
    public static class ValidationService {

        /**
         * Pattern matching
         */
        @PreAuthorize("#email matches '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}'")
        public void sendEmail(String email) {
            System.out.println("Sending email to: " + email);
        }

        /**
         * String contains check
         */
        @PreAuthorize("#username.length() >= 3 and #username.length() <= 20")
        public void createUsername(String username) {
            System.out.println("Creating username: " + username);
        }
    }

    /**
     * Custom security expression service
     */
    @Service("securityExpressionService")
    public static class SecurityExpressionService {

        public boolean canAccessDocument(Long documentId, org.springframework.security.core.Authentication auth) {
            System.out.println("Checking document access for: " + documentId);
            // Check database, cache, or business rules
            return true; // Simplified
        }

        public boolean isOwner(Long documentId, String username) {
            System.out.println("Checking if " + username + " owns document " + documentId);
            // Check database
            return true; // Simplified
        }

        public boolean isDocumentEditable(Long documentId) {
            System.out.println("Checking if document " + documentId + " is editable");
            // Check document status, lock, etc.
            return true; // Simplified
        }

        public boolean hasSubscription(String username, String subscriptionType) {
            System.out.println("Checking subscription: " + subscriptionType + " for " + username);
            return true; // Simplified
        }
    }

    // Domain Classes

    public static class Transaction {
        private Long id;
        private BigDecimal amount;
        private String type;
        private LocalDateTime timestamp;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class Account {
        private Long id;
        private String owner;
        private boolean active;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public static class Document {
        private Long id;
        private String title;
        private String owner;

        public Document(Long id, String title, String owner) {
            this.id = id;
            this.title = title;
            this.owner = owner;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getOwner() { return owner; }
    }

    public static class Order {
        private Long id;
        private Customer customer;
        private BigDecimal total;
        private Address shippingAddress;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Customer getCustomer() { return customer; }
        public void setCustomer(Customer customer) { this.customer = customer; }

        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }

        public Address getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }
    }

    public static class Customer {
        private String username;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class Address {
        private String street;
        private String city;
        private String country;

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
}

/**
 * DOCUMENTATION
 * 
 * Spring Expression Language (SpEL) in Security:
 * 
 * 1. Built-in Variables:
 *    - authentication: Current Authentication object
 *    - principal: Current user (authentication.principal)
 *    - #paramName: Method parameter
 *    - returnObject: Method return value (@PostAuthorize)
 *    - filterObject: Collection element (@PostFilter/@PreFilter)
 * 
 * 2. Operators:
 *    - Arithmetic: +, -, *, /, %, ^
 *    - Comparison: <, >, <=, >=, ==, !=, lt, gt, le, ge, eq, ne
 *    - Logical: and, or, not, &&, ||, !
 *    - Ternary: condition ? true : false
 *    - Elvis: value ?: default
 *    - Safe navigation: object?.property
 * 
 * 3. Common Functions:
 *    - hasRole('ROLE'): Check role
 *    - hasAuthority('AUTHORITY'): Check authority
 *    - hasAnyRole('ROLE1', 'ROLE2'): Check multiple roles
 *    - hasAnyAuthority(...): Check multiple authorities
 *    - isAuthenticated(): User is authenticated
 *    - isAnonymous(): User is anonymous
 *    - permitAll(): Allow all
 *    - denyAll(): Deny all
 * 
 * 4. Bean References:
 *    - @beanName: Reference Spring bean
 *    - @beanName.method(): Call bean method
 *    - @beanName.property: Access bean property
 * 
 * 5. Type References:
 *    - T(ClassName): Reference Java class
 *    - T(ClassName).method(): Call static method
 *    - T(ClassName).CONSTANT: Access constant
 *    - Examples:
 *      - T(java.time.LocalTime).now()
 *      - T(java.lang.Math).max(a, b)
 * 
 * 6. Collection Operations:
 *    - #list.contains(item): Check containment
 *    - #list.size(): Get size
 *    - #list.?[condition]: Filter collection
 *    - #list.![property]: Project properties
 *    - #list.^[condition]: First match
 *    - #list.$[condition]: Last match
 * 
 * 7. String Operations:
 *    - #str.length(): String length
 *    - #str.startsWith('prefix'): Check prefix
 *    - #str.endsWith('suffix'): Check suffix
 *    - #str.contains('substring'): Check substring
 *    - #str matches 'regex': Pattern matching
 * 
 * 8. Property Navigation:
 *    - object.property: Access property
 *    - object.property.nestedProperty: Navigate graph
 *    - object?.property: Null-safe access
 * 
 * 9. Best Practices:
 *    - Keep expressions simple and readable
 *    - Extract complex logic to security services
 *    - Use meaningful parameter names
 *    - Document expression intent
 *    - Test expressions thoroughly
 *    - Avoid inline business logic
 * 
 * 10. Performance Tips:
 *     - Cache expensive computations
 *     - Avoid database calls in expressions
 *     - Use bean methods for complex checks
 *     - Consider method-level caching
 * 
 * 11. Common Patterns:
 *     - Ownership: #obj.owner == authentication.name
 *     - Amount limit: #amount <= 1000 or hasRole('ADMIN')
 *     - Time-based: T(LocalTime).now().hour >= 9
 *     - Custom logic: @service.check(#param, authentication)
 *     - Collection: #list.contains(principal.username)
 */
