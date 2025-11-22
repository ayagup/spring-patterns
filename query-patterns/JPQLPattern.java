package com.example.querypatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPQL Pattern Implementation
 * 
 * Demonstrates Java Persistence Query Language (JPQL) queries.
 * 
 * Key Components:
 * - @Query annotation with JPQL
 * - Object-oriented query language
 * - Entity and field names (not table/column names)
 * - JOIN FETCH for eager loading
 * - Named parameters and positional parameters
 * 
 * Benefits:
 * - Database independent
 * - Object-oriented (works with entities)
 * - Type-safe entity navigation
 * - Supports polymorphism
 * - JPA standard (portable)
 * 
 * Use Cases:
 * - Cross-database compatibility needed
 * - Entity relationship navigation
 * - Standard CRUD with conditions
 * - Moderate complexity queries
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class JPQLPattern {

    public static void main(String[] args) {
        SpringApplication.run(JPQLPattern.class, args);
    }

    /**
     * Customer Entity
     */
    @Entity
    @Table(name = "customers")
    public static class Customer {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @Column(name = "first_name", nullable = false)
        private String firstName;
        
        @Column(name = "last_name", nullable = false)
        private String lastName;
        
        @Column(unique = true, nullable = false)
        private String email;
        
        @Column(name = "phone_number")
        private String phoneNumber;
        
        @Column(nullable = false)
        private String city;
        
        @Column(nullable = false)
        private String country;
        
        @Enumerated(EnumType.STRING)
        @Column(name = "customer_status", nullable = false)
        private CustomerStatus status;
        
        @Column(name = "total_purchases")
        private BigDecimal totalPurchases;
        
        @Column(name = "loyalty_points")
        private Integer loyaltyPoints;
        
        @Column(name = "registration_date", nullable = false)
        private LocalDateTime registrationDate;
        
        @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
        private List<Purchase> purchases;
        
        // Constructors
        public Customer() {
            this.status = CustomerStatus.ACTIVE;
            this.totalPurchases = BigDecimal.ZERO;
            this.loyaltyPoints = 0;
            this.registrationDate = LocalDateTime.now();
        }
        
        public Customer(String firstName, String lastName, String email, 
                       String city, String country) {
            this();
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.city = city;
            this.country = country;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public CustomerStatus getStatus() { return status; }
        public void setStatus(CustomerStatus status) { this.status = status; }
        
        public BigDecimal getTotalPurchases() { return totalPurchases; }
        public void setTotalPurchases(BigDecimal totalPurchases) { this.totalPurchases = totalPurchases; }
        
        public Integer getLoyaltyPoints() { return loyaltyPoints; }
        public void setLoyaltyPoints(Integer loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
        
        public LocalDateTime getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(LocalDateTime registrationDate) { 
            this.registrationDate = registrationDate; 
        }
        
        public List<Purchase> getPurchases() { return purchases; }
        public void setPurchases(List<Purchase> purchases) { this.purchases = purchases; }
    }
    
    /**
     * Purchase Entity
     */
    @Entity
    @Table(name = "purchases")
    public static class Purchase {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "customer_id", nullable = false)
        private Customer customer;
        
        @Column(name = "product_name", nullable = false)
        private String productName;
        
        @Column(nullable = false)
        private BigDecimal amount;
        
        @Column(name = "purchase_date", nullable = false)
        private LocalDateTime purchaseDate;
        
        // Constructors, Getters, Setters
        public Purchase() {
            this.purchaseDate = LocalDateTime.now();
        }
        
        public Purchase(String productName, BigDecimal amount) {
            this();
            this.productName = productName;
            this.amount = amount;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Customer getCustomer() { return customer; }
        public void setCustomer(Customer customer) { this.customer = customer; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public LocalDateTime getPurchaseDate() { return purchaseDate; }
        public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
    }
    
    /**
     * Customer Status Enum
     */
    public enum CustomerStatus {
        ACTIVE, INACTIVE, VIP, SUSPENDED
    }
    
    /**
     * Repository with JPQL Queries
     */
    @Repository
    public interface CustomerRepository extends JpaRepository<Customer, Long> {
        
        /**
         * Simple JPQL query
         */
        @Query("SELECT c FROM Customer c WHERE c.status = :status")
        List<Customer> findByStatus(@Param("status") CustomerStatus status);
        
        /**
         * JPQL with LIKE
         */
        @Query("SELECT c FROM Customer c WHERE LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
        List<Customer> findByLastNameContaining(@Param("lastName") String lastName);
        
        /**
         * JPQL with multiple conditions
         */
        @Query("SELECT c FROM Customer c WHERE c.country = :country AND c.status = :status")
        List<Customer> findByCountryAndStatus(@Param("country") String country, 
                                              @Param("status") CustomerStatus status);
        
        /**
         * JPQL with OR condition
         */
        @Query("SELECT c FROM Customer c WHERE c.city = :city OR c.country = :country")
        List<Customer> findByCityOrCountry(@Param("city") String city, 
                                          @Param("country") String country);
        
        /**
         * JPQL with BETWEEN
         */
        @Query("SELECT c FROM Customer c WHERE c.totalPurchases BETWEEN :min AND :max")
        List<Customer> findByPurchaseRange(@Param("min") BigDecimal min, 
                                          @Param("max") BigDecimal max);
        
        /**
         * JPQL with ORDER BY
         */
        @Query("SELECT c FROM Customer c WHERE c.status = :status ORDER BY c.totalPurchases DESC")
        List<Customer> findByStatusOrderedByPurchases(@Param("status") CustomerStatus status);
        
        /**
         * JPQL with JOIN
         */
        @Query("SELECT c FROM Customer c JOIN c.purchases p WHERE p.productName = :productName")
        List<Customer> findCustomersWhoPurchased(@Param("productName") String productName);
        
        /**
         * JPQL with JOIN FETCH (eager loading to avoid N+1)
         */
        @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.purchases WHERE c.status = :status")
        List<Customer> findByStatusWithPurchases(@Param("status") CustomerStatus status);
        
        /**
         * JPQL with COUNT
         */
        @Query("SELECT COUNT(c) FROM Customer c WHERE c.country = :country")
        Long countByCountry(@Param("country") String country);
        
        /**
         * JPQL with SUM
         */
        @Query("SELECT SUM(c.totalPurchases) FROM Customer c WHERE c.status = :status")
        BigDecimal sumTotalPurchasesByStatus(@Param("status") CustomerStatus status);
        
        /**
         * JPQL with AVG
         */
        @Query("SELECT AVG(c.loyaltyPoints) FROM Customer c WHERE c.country = :country")
        Double averageLoyaltyPointsByCountry(@Param("country") String country);
        
        /**
         * JPQL with MAX/MIN
         */
        @Query("SELECT MAX(c.totalPurchases) FROM Customer c")
        BigDecimal findMaxPurchaseAmount();
        
        /**
         * JPQL with GROUP BY
         */
        @Query("SELECT c.country, COUNT(c) FROM Customer c GROUP BY c.country")
        List<Object[]> countCustomersByCountry();
        
        /**
         * JPQL with HAVING
         */
        @Query("SELECT c.country, COUNT(c) FROM Customer c " +
               "GROUP BY c.country HAVING COUNT(c) > :minCount")
        List<Object[]> findCountriesWithMinCustomers(@Param("minCount") Long minCount);
        
        /**
         * JPQL with subquery
         */
        @Query("SELECT c FROM Customer c WHERE c.totalPurchases > " +
               "(SELECT AVG(c2.totalPurchases) FROM Customer c2)")
        List<Customer> findAboveAveragePurchasers();
        
        /**
         * JPQL with EXISTS
         */
        @Query("SELECT c FROM Customer c WHERE EXISTS " +
               "(SELECT p FROM Purchase p WHERE p.customer = c AND p.amount > :amount)")
        List<Customer> findWithPurchaseAbove(@Param("amount") BigDecimal amount);
        
        /**
         * JPQL with IN clause
         */
        @Query("SELECT c FROM Customer c WHERE c.city IN :cities")
        List<Customer> findByCities(@Param("cities") List<String> cities);
        
        /**
         * JPQL with NOT IN
         */
        @Query("SELECT c FROM Customer c WHERE c.status NOT IN :statuses")
        List<Customer> findByStatusNotIn(@Param("statuses") List<CustomerStatus> statuses);
        
        /**
         * JPQL with IS NULL/IS NOT NULL
         */
        @Query("SELECT c FROM Customer c WHERE c.phoneNumber IS NOT NULL")
        List<Customer> findWithPhoneNumber();
        
        /**
         * JPQL with DISTINCT
         */
        @Query("SELECT DISTINCT c.country FROM Customer c ORDER BY c.country")
        List<String> findAllCountries();
        
        /**
         * JPQL Constructor Expression (DTO projection)
         */
        @Query("SELECT NEW com.example.querypatterns.JPQLPattern$CustomerSummary(" +
               "c.id, c.firstName, c.lastName, c.email, c.totalPurchases) " +
               "FROM Customer c WHERE c.status = :status")
        List<CustomerSummary> findCustomerSummaries(@Param("status") CustomerStatus status);
        
        /**
         * JPQL with CASE
         */
        @Query("SELECT c, CASE " +
               "WHEN c.totalPurchases > 10000 THEN 'PREMIUM' " +
               "WHEN c.totalPurchases > 5000 THEN 'GOLD' " +
               "WHEN c.totalPurchases > 1000 THEN 'SILVER' " +
               "ELSE 'BRONZE' END " +
               "FROM Customer c")
        List<Object[]> findCustomersWithTier();
        
        /**
         * JPQL with CONCAT
         */
        @Query("SELECT CONCAT(c.firstName, ' ', c.lastName) FROM Customer c")
        List<String> findAllFullNames();
        
        /**
         * JPQL with date functions
         */
        @Query("SELECT c FROM Customer c WHERE YEAR(c.registrationDate) = :year")
        List<Customer> findByRegistrationYear(@Param("year") int year);
        
        /**
         * JPQL update query
         */
        @org.springframework.data.jpa.repository.Modifying
        @Query("UPDATE Customer c SET c.status = :newStatus WHERE c.status = :oldStatus")
        int updateCustomerStatus(@Param("oldStatus") CustomerStatus oldStatus, 
                                @Param("newStatus") CustomerStatus newStatus);
        
        /**
         * JPQL delete query
         */
        @org.springframework.data.jpa.repository.Modifying
        @Query("DELETE FROM Customer c WHERE c.status = :status AND c.totalPurchases = 0")
        int deleteInactiveCustomers(@Param("status") CustomerStatus status);
        
        /**
         * JPQL with positional parameters (instead of named)
         */
        @Query("SELECT c FROM Customer c WHERE c.firstName = ?1 AND c.lastName = ?2")
        Optional<Customer> findByFullName(String firstName, String lastName);
    }
    
    /**
     * DTO for Customer Summary
     */
    public static class CustomerSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private BigDecimal totalPurchases;
        
        public CustomerSummary(Long id, String firstName, String lastName, 
                             String email, BigDecimal totalPurchases) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.totalPurchases = totalPurchases;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public BigDecimal getTotalPurchases() { return totalPurchases; }
    }
    
    /**
     * Service using JPQL
     */
    @Service
    @Transactional
    public static class CustomerJPQLService {
        
        private final CustomerRepository customerRepository;
        
        public CustomerJPQLService(CustomerRepository customerRepository) {
            this.customerRepository = customerRepository;
        }
        
        public List<Customer> getActiveCustomers() {
            return customerRepository.findByStatus(CustomerStatus.ACTIVE);
        }
        
        public List<Customer> searchByLastName(String lastName) {
            return customerRepository.findByLastNameContaining(lastName);
        }
        
        public List<Customer> getCustomersByLocation(String country, CustomerStatus status) {
            return customerRepository.findByCountryAndStatus(country, status);
        }
        
        public List<Customer> getCustomersInPurchaseRange(BigDecimal min, BigDecimal max) {
            return customerRepository.findByPurchaseRange(min, max);
        }
        
        public List<Customer> getTopCustomers(CustomerStatus status) {
            return customerRepository.findByStatusOrderedByPurchases(status);
        }
        
        public List<Customer> getCustomersWhoPurchased(String productName) {
            return customerRepository.findCustomersWhoPurchased(productName);
        }
        
        public List<Customer> getActiveCustomersWithPurchases() {
            return customerRepository.findByStatusWithPurchases(CustomerStatus.ACTIVE);
        }
        
        public Long countCustomersInCountry(String country) {
            return customerRepository.countByCountry(country);
        }
        
        public BigDecimal getTotalPurchasesByStatus(CustomerStatus status) {
            return customerRepository.sumTotalPurchasesByStatus(status);
        }
        
        public List<CustomerSummary> getCustomerSummaries(CustomerStatus status) {
            return customerRepository.findCustomerSummaries(status);
        }
        
        public List<Customer> getAboveAveragePurchasers() {
            return customerRepository.findAboveAveragePurchasers();
        }
        
        public List<String> getAllCountries() {
            return customerRepository.findAllCountries();
        }
        
        public List<Customer> getCustomersByCities(List<String> cities) {
            return customerRepository.findByCities(cities);
        }
        
        public int promoteCustomers(CustomerStatus from, CustomerStatus to) {
            return customerRepository.updateCustomerStatus(from, to);
        }
        
        public int cleanupInactiveCustomers() {
            return customerRepository.deleteInactiveCustomers(CustomerStatus.INACTIVE);
        }
    }
    
    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/jpql/customers")
    public static class CustomerJPQLController {
        
        private final CustomerJPQLService jpqlService;
        private final CustomerRepository customerRepository;
        
        public CustomerJPQLController(CustomerJPQLService jpqlService,
                                     CustomerRepository customerRepository) {
            this.jpqlService = jpqlService;
            this.customerRepository = customerRepository;
        }
        
        @PostMapping
        public Customer create(@RequestBody Customer customer) {
            return customerRepository.save(customer);
        }
        
        @GetMapping("/active")
        public List<Customer> getActive() {
            return jpqlService.getActiveCustomers();
        }
        
        @GetMapping("/search/lastname/{lastName}")
        public List<Customer> searchByLastName(@PathVariable String lastName) {
            return jpqlService.searchByLastName(lastName);
        }
        
        @GetMapping("/location")
        public List<Customer> getByLocation(
                @RequestParam String country,
                @RequestParam CustomerStatus status) {
            return jpqlService.getCustomersByLocation(country, status);
        }
        
        @GetMapping("/purchase-range")
        public List<Customer> getByPurchaseRange(
                @RequestParam BigDecimal min,
                @RequestParam BigDecimal max) {
            return jpqlService.getCustomersInPurchaseRange(min, max);
        }
        
        @GetMapping("/top")
        public List<Customer> getTop(@RequestParam CustomerStatus status) {
            return jpqlService.getTopCustomers(status);
        }
        
        @GetMapping("/product/{productName}")
        public List<Customer> getByProduct(@PathVariable String productName) {
            return jpqlService.getCustomersWhoPurchased(productName);
        }
        
        @GetMapping("/summaries")
        public List<CustomerSummary> getSummaries(@RequestParam CustomerStatus status) {
            return jpqlService.getCustomerSummaries(status);
        }
        
        @GetMapping("/countries")
        public List<String> getAllCountries() {
            return jpqlService.getAllCountries();
        }
        
        @PutMapping("/promote")
        public int promote(@RequestParam CustomerStatus from, @RequestParam CustomerStatus to) {
            return jpqlService.promoteCustomers(from, to);
        }
    }
}

/**
 * Configuration (application.properties):
 * 
 * spring.datasource.url=jdbc:h2:mem:jpqldb
 * spring.datasource.driver-class-name=org.h2.Driver
 * spring.jpa.hibernate.ddl-auto=create-drop
 * spring.jpa.show-sql=true
 * spring.jpa.properties.hibernate.format_sql=true
 * 
 * Best Practices:
 * 1. Use named parameters (:param) instead of positional (?1)
 * 2. Use JOIN FETCH to avoid N+1 query problems
 * 3. Use constructor expressions for DTO projections
 * 4. Prefer JPQL over native queries for portability
 * 5. Use @Modifying for UPDATE/DELETE queries
 * 6. Test queries with various data scenarios
 * 7. Use DISTINCT with JOIN FETCH when necessary
 * 8. Consider query performance and add indexes
 * 9. Document complex queries
 * 10. Use entity names and field names, not table/column names
 */
