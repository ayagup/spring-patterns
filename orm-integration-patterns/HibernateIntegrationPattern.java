package com.example.orm.integration;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Hibernate Integration Pattern
 * 
 * Purpose:
 * - Native Hibernate ORM integration with Spring
 * - SessionFactory management
 * - Advanced Hibernate features
 * - HQL (Hibernate Query Language) support
 * 
 * Features:
 * 1. SessionFactory configuration
 * 2. Session management
 * 3. HQL queries
 * 4. Criteria API
 * 5. Native SQL support
 * 6. Second-level caching
 * 7. Lazy loading
 * 8. Batch processing
 * 
 * When to Use:
 * - Need Hibernate-specific features
 * - Advanced caching requirements
 * - Complex mappings
 * - Batch operations
 * - Performance optimization
 * 
 * Benefits:
 * - Rich ORM features
 * - Powerful caching
 * - Flexible query options
 * - Batch processing support
 * - Performance tuning capabilities
 */
@SpringBootApplication
public class HibernateIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(HibernateIntegrationPattern.class, args);
        System.out.println("Hibernate Integration Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/hibernate/customers");
    }

    /**
     * Customer Entity
     */
    @javax.persistence.Entity
    @javax.persistence.Table(name = "customers")
    @org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
    public static class Customer {
        
        @javax.persistence.Id
        @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
        private Long id;
        
        @javax.persistence.Column(name = "first_name", nullable = false)
        private String firstName;
        
        @javax.persistence.Column(name = "last_name", nullable = false)
        private String lastName;
        
        @javax.persistence.Column(name = "email", unique = true)
        private String email;
        
        @javax.persistence.Column(name = "phone")
        private String phone;
        
        @javax.persistence.Column(name = "active")
        private Boolean active = true;

        // Constructors
        public Customer() {}

        public Customer(String firstName, String lastName, String email, String phone) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
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
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    /**
     * Hibernate Configuration
     */
    @org.springframework.context.annotation.Configuration
    public static class HibernateConfig {

        @Bean
        public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
            LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
            sessionFactory.setDataSource(dataSource);
            sessionFactory.setPackagesToScan("com.example.orm.integration");
            sessionFactory.setHibernateProperties(hibernateProperties());
            return sessionFactory;
        }

        @Bean
        public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
            HibernateTransactionManager transactionManager = new HibernateTransactionManager();
            transactionManager.setSessionFactory(sessionFactory);
            return transactionManager;
        }

        private Properties hibernateProperties() {
            Properties properties = new Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", "update");
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            properties.setProperty("hibernate.show_sql", "true");
            properties.setProperty("hibernate.format_sql", "true");
            properties.setProperty("hibernate.use_sql_comments", "true");
            
            // Second-level cache
            properties.setProperty("hibernate.cache.use_second_level_cache", "true");
            properties.setProperty("hibernate.cache.region.factory_class", 
                    "org.hibernate.cache.jcache.JCacheRegionFactory");
            properties.setProperty("hibernate.cache.use_query_cache", "true");
            
            // Batch processing
            properties.setProperty("hibernate.jdbc.batch_size", "20");
            properties.setProperty("hibernate.order_inserts", "true");
            properties.setProperty("hibernate.order_updates", "true");
            properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
            
            // Performance
            properties.setProperty("hibernate.jdbc.fetch_size", "50");
            properties.setProperty("hibernate.default_batch_fetch_size", "16");
            
            return properties;
        }
    }

    /**
     * Customer Repository using Hibernate Session
     */
    @Repository
    public static class CustomerRepository {

        private final SessionFactory sessionFactory;

        public CustomerRepository(SessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }

        /**
         * Get current session
         */
        private Session getCurrentSession() {
            return sessionFactory.getCurrentSession();
        }

        /**
         * Save customer
         */
        @Transactional
        public Customer save(Customer customer) {
            Session session = getCurrentSession();
            if (customer.getId() == null) {
                session.save(customer);
            } else {
                session.update(customer);
            }
            return customer;
        }

        /**
         * Find by ID
         */
        @Transactional(readOnly = true)
        public Optional<Customer> findById(Long id) {
            Session session = getCurrentSession();
            Customer customer = session.get(Customer.class, id);
            return Optional.ofNullable(customer);
        }

        /**
         * Find all customers using HQL
         */
        @Transactional(readOnly = true)
        public List<Customer> findAll() {
            Session session = getCurrentSession();
            Query<Customer> query = session.createQuery("FROM Customer", Customer.class);
            return query.list();
        }

        /**
         * Find by email using HQL
         */
        @Transactional(readOnly = true)
        public Optional<Customer> findByEmail(String email) {
            Session session = getCurrentSession();
            Query<Customer> query = session.createQuery(
                    "FROM Customer c WHERE c.email = :email", Customer.class);
            query.setParameter("email", email);
            return query.uniqueResultOptional();
        }

        /**
         * Find by last name using HQL
         */
        @Transactional(readOnly = true)
        public List<Customer> findByLastName(String lastName) {
            Session session = getCurrentSession();
            Query<Customer> query = session.createQuery(
                    "FROM Customer c WHERE c.lastName LIKE :lastName", Customer.class);
            query.setParameter("lastName", "%" + lastName + "%");
            return query.list();
        }

        /**
         * Find active customers
         */
        @Transactional(readOnly = true)
        public List<Customer> findActiveCustomers() {
            Session session = getCurrentSession();
            Query<Customer> query = session.createQuery(
                    "FROM Customer c WHERE c.active = true", Customer.class);
            query.setCacheable(true); // Enable query cache
            return query.list();
        }

        /**
         * Update customer email
         */
        @Transactional
        public int updateEmail(Long id, String newEmail) {
            Session session = getCurrentSession();
            Query query = session.createQuery(
                    "UPDATE Customer c SET c.email = :email WHERE c.id = :id");
            query.setParameter("email", newEmail);
            query.setParameter("id", id);
            return query.executeUpdate();
        }

        /**
         * Delete customer
         */
        @Transactional
        public void delete(Long id) {
            Session session = getCurrentSession();
            Customer customer = session.get(Customer.class, id);
            if (customer != null) {
                session.delete(customer);
            }
        }

        /**
         * Soft delete (deactivate)
         */
        @Transactional
        public void deactivate(Long id) {
            Session session = getCurrentSession();
            Query query = session.createQuery(
                    "UPDATE Customer c SET c.active = false WHERE c.id = :id");
            query.setParameter("id", id);
            query.executeUpdate();
        }

        /**
         * Count all customers
         */
        @Transactional(readOnly = true)
        public Long count() {
            Session session = getCurrentSession();
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(c) FROM Customer c", Long.class);
            return query.uniqueResult();
        }

        /**
         * Native SQL query example
         */
        @Transactional(readOnly = true)
        public List<Customer> findByNativeQuery(String firstName) {
            Session session = getCurrentSession();
            Query<Customer> query = session.createNativeQuery(
                    "SELECT * FROM customers WHERE first_name = ?", Customer.class);
            query.setParameter(1, firstName);
            return query.list();
        }

        /**
         * Batch save customers
         */
        @Transactional
        public void batchSave(List<Customer> customers) {
            Session session = getCurrentSession();
            int batchSize = 20;
            
            for (int i = 0; i < customers.size(); i++) {
                session.save(customers.get(i));
                
                if (i % batchSize == 0 && i > 0) {
                    session.flush();
                    session.clear();
                }
            }
        }

        /**
         * Pagination example
         */
        @Transactional(readOnly = true)
        public List<Customer> findWithPagination(int pageNumber, int pageSize) {
            Session session = getCurrentSession();
            Query<Customer> query = session.createQuery("FROM Customer", Customer.class);
            query.setFirstResult((pageNumber - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        }

        /**
         * Named query example
         */
        @Transactional(readOnly = true)
        public List<Customer> searchCustomers(String searchTerm) {
            Session session = getCurrentSession();
            Query<Customer> query = session.createQuery(
                    "FROM Customer c WHERE c.firstName LIKE :term OR c.lastName LIKE :term", 
                    Customer.class);
            query.setParameter("term", "%" + searchTerm + "%");
            return query.list();
        }
    }

    /**
     * Customer Service
     */
    @Service
    public static class CustomerService {

        private final CustomerRepository repository;

        public CustomerService(CustomerRepository repository) {
            this.repository = repository;
        }

        @Transactional
        public Customer createCustomer(Customer customer) {
            return repository.save(customer);
        }

        public Optional<Customer> getCustomer(Long id) {
            return repository.findById(id);
        }

        public List<Customer> getAllCustomers() {
            return repository.findAll();
        }

        public Optional<Customer> getCustomerByEmail(String email) {
            return repository.findByEmail(email);
        }

        public List<Customer> searchByLastName(String lastName) {
            return repository.findByLastName(lastName);
        }

        public List<Customer> getActiveCustomers() {
            return repository.findActiveCustomers();
        }

        @Transactional
        public void updateEmail(Long id, String email) {
            repository.updateEmail(id, email);
        }

        @Transactional
        public void deleteCustomer(Long id) {
            repository.delete(id);
        }

        @Transactional
        public void deactivateCustomer(Long id) {
            repository.deactivate(id);
        }

        public Long getTotalCustomers() {
            return repository.count();
        }

        @Transactional
        public void batchCreateCustomers(List<Customer> customers) {
            repository.batchSave(customers);
        }

        public List<Customer> getCustomersPage(int page, int size) {
            return repository.findWithPagination(page, size);
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/hibernate")
    public static class CustomerController {

        private final CustomerService service;

        public CustomerController(CustomerService service) {
            this.service = service;
        }

        @PostMapping("/customers")
        public Customer createCustomer(@RequestBody Customer customer) {
            return service.createCustomer(customer);
        }

        @PostMapping("/customers/batch")
        public void batchCreate(@RequestBody List<Customer> customers) {
            service.batchCreateCustomers(customers);
        }

        @GetMapping("/customers/{id}")
        public Optional<Customer> getCustomer(@PathVariable Long id) {
            return service.getCustomer(id);
        }

        @GetMapping("/customers")
        public List<Customer> getAllCustomers(
                @RequestParam(required = false) Integer page,
                @RequestParam(required = false) Integer size) {
            if (page != null && size != null) {
                return service.getCustomersPage(page, size);
            }
            return service.getAllCustomers();
        }

        @GetMapping("/customers/email/{email}")
        public Optional<Customer> getByEmail(@PathVariable String email) {
            return service.getCustomerByEmail(email);
        }

        @GetMapping("/customers/search")
        public List<Customer> searchByLastName(@RequestParam String lastName) {
            return service.searchByLastName(lastName);
        }

        @GetMapping("/customers/active")
        public List<Customer> getActiveCustomers() {
            return service.getActiveCustomers();
        }

        @PutMapping("/customers/{id}/email")
        public void updateEmail(@PathVariable Long id, @RequestParam String email) {
            service.updateEmail(id, email);
        }

        @DeleteMapping("/customers/{id}")
        public void deleteCustomer(@PathVariable Long id) {
            service.deleteCustomer(id);
        }

        @PutMapping("/customers/{id}/deactivate")
        public void deactivateCustomer(@PathVariable Long id) {
            service.deactivateCustomer(id);
        }

        @GetMapping("/customers/count")
        public Long getCount() {
            return service.getTotalCustomers();
        }
    }
}

/**
 * Configuration Examples:
 * 
 * application.properties:
 * 
 * # DataSource
 * spring.datasource.url=jdbc:mysql://localhost:3306/mydb
 * spring.datasource.username=root
 * spring.datasource.password=password
 * spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
 * 
 * # Hibernate
 * spring.jpa.hibernate.ddl-auto=update
 * spring.jpa.show-sql=true
 * spring.jpa.properties.hibernate.format_sql=true
 * spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
 * 
 * # Second-level cache (EhCache)
 * spring.jpa.properties.hibernate.cache.use_second_level_cache=true
 * spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
 * spring.jpa.properties.hibernate.javax.cache.provider=org.ehcache.jsr107.EhcacheCachingProvider
 * 
 * # Batch processing
 * spring.jpa.properties.hibernate.jdbc.batch_size=20
 * spring.jpa.properties.hibernate.order_inserts=true
 * spring.jpa.properties.hibernate.order_updates=true
 * 
 * 
 * Best Practices:
 * 
 * 1. Use SessionFactory for Hibernate-specific features
 * 2. Enable second-level cache for read-heavy entities
 * 3. Use batch processing for bulk operations
 * 4. Leverage HQL for database-independent queries
 * 5. Configure fetch strategies appropriately
 * 6. Use @Transactional for automatic session management
 * 7. Monitor SQL queries in production
 * 8. Tune batch size based on your use case
 */
