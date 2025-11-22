package com.example.orm.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * JPA Integration Pattern
 * 
 * Purpose:
 * - Java Persistence API (JPA) integration with Spring
 * - Object-Relational Mapping (ORM) abstraction
 * - Entity management and persistence
 * - Database operations through EntityManager
 * 
 * Features:
 * 1. EntityManagerFactory configuration
 * 2. JPA annotations (@Entity, @Id, @Table)
 * 3. CRUD operations via EntityManager
 * 4. JPQL query support
 * 5. Transaction management integration
 * 6. Vendor-neutral persistence layer
 * 
 * When to Use:
 * - Need standard JPA interface
 * - Vendor-neutral ORM layer
 * - Complex object mappings
 * - Advanced query capabilities
 * - Transaction-aware persistence
 * 
 * Benefits:
 * - Vendor independence
 * - Rich ORM features
 * - Type-safe queries
 * - First and second-level caching
 * - Lazy loading support
 * - Relationship management
 */
@SpringBootApplication
public class JPAIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(JPAIntegrationPattern.class, args);
        System.out.println("JPA Integration Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/jpa/products");
    }

    /**
     * Product Entity
     */
    @Entity
    @Table(name = "products")
    public static class Product {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @Column(name = "name", nullable = false, length = 100)
        private String name;
        
        @Column(name = "description", length = 500)
        private String description;
        
        @Column(name = "price", nullable = false)
        private Double price;
        
        @Column(name = "quantity")
        private Integer quantity;
        
        @Version
        private Long version; // Optimistic locking
        
        @Embedded
        private Audit audit;

        // Constructors
        public Product() {}

        public Product(String name, String description, Double price, Integer quantity) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.quantity = quantity;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public Long getVersion() { return version; }
        public void setVersion(Long version) { this.version = version; }
        
        public Audit getAudit() { return audit; }
        public void setAudit(Audit audit) { this.audit = audit; }
    }

    /**
     * Embeddable Audit Information
     */
    @Embeddable
    public static class Audit {
        @Column(name = "created_by")
        private String createdBy;
        
        @Column(name = "created_date")
        @Temporal(TemporalType.TIMESTAMP)
        private java.util.Date createdDate;
        
        @Column(name = "modified_by")
        private String modifiedBy;
        
        @Column(name = "modified_date")
        @Temporal(TemporalType.TIMESTAMP)
        private java.util.Date modifiedDate;

        public Audit() {}

        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        
        public java.util.Date getCreatedDate() { return createdDate; }
        public void setCreatedDate(java.util.Date createdDate) { this.createdDate = createdDate; }
        
        public String getModifiedBy() { return modifiedBy; }
        public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
        
        public java.util.Date getModifiedDate() { return modifiedDate; }
        public void setModifiedDate(java.util.Date modifiedDate) { this.modifiedDate = modifiedDate; }
    }

    /**
     * JPA Configuration
     */
    @Configuration
    public static class JpaConfig {

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(dataSource);
            em.setPackagesToScan("com.example.orm.integration");
            
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setShowSql(true);
            vendorAdapter.setGenerateDdl(true);
            em.setJpaVendorAdapter(vendorAdapter);
            
            Properties properties = new Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", "update");
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            properties.setProperty("hibernate.format_sql", "true");
            properties.setProperty("hibernate.use_sql_comments", "true");
            properties.setProperty("hibernate.show_sql", "true");
            em.setJpaProperties(properties);
            
            return em;
        }

        @Bean
        public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }

    /**
     * Product Repository using EntityManager
     */
    @Repository
    public static class ProductRepository {

        @PersistenceContext
        private EntityManager entityManager;

        /**
         * Save or update product
         */
        @Transactional
        public Product save(Product product) {
            if (product.getId() == null) {
                entityManager.persist(product);
                return product;
            } else {
                return entityManager.merge(product);
            }
        }

        /**
         * Find by ID
         */
        public Optional<Product> findById(Long id) {
            Product product = entityManager.find(Product.class, id);
            return Optional.ofNullable(product);
        }

        /**
         * Find all products
         */
        public List<Product> findAll() {
            TypedQuery<Product> query = entityManager.createQuery(
                    "SELECT p FROM Product p", Product.class);
            return query.getResultList();
        }

        /**
         * Find by name using JPQL
         */
        public List<Product> findByName(String name) {
            TypedQuery<Product> query = entityManager.createQuery(
                    "SELECT p FROM Product p WHERE p.name LIKE :name", Product.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        }

        /**
         * Find products by price range
         */
        public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
            TypedQuery<Product> query = entityManager.createQuery(
                    "SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice", 
                    Product.class);
            query.setParameter("minPrice", minPrice);
            query.setParameter("maxPrice", maxPrice);
            return query.getResultList();
        }

        /**
         * Update product price
         */
        @Transactional
        public int updatePrice(Long id, Double newPrice) {
            Query query = entityManager.createQuery(
                    "UPDATE Product p SET p.price = :price WHERE p.id = :id");
            query.setParameter("price", newPrice);
            query.setParameter("id", id);
            return query.executeUpdate();
        }

        /**
         * Delete product
         */
        @Transactional
        public void delete(Long id) {
            Product product = entityManager.find(Product.class, id);
            if (product != null) {
                entityManager.remove(product);
            }
        }

        /**
         * Count all products
         */
        public Long count() {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(p) FROM Product p", Long.class);
            return query.getSingleResult();
        }

        /**
         * Native SQL query example
         */
        public List<Product> findExpensiveProducts(Double threshold) {
            Query query = entityManager.createNativeQuery(
                    "SELECT * FROM products WHERE price > ?", Product.class);
            query.setParameter(1, threshold);
            return query.getResultList();
        }

        /**
         * Named query example
         */
        public List<Product> findLowStock(Integer threshold) {
            TypedQuery<Product> query = entityManager.createQuery(
                    "SELECT p FROM Product p WHERE p.quantity < :threshold", Product.class);
            query.setParameter("threshold", threshold);
            return query.getResultList();
        }

        /**
         * Flush changes to database
         */
        @Transactional
        public void flush() {
            entityManager.flush();
        }

        /**
         * Clear persistence context
         */
        public void clear() {
            entityManager.clear();
        }

        /**
         * Detach entity
         */
        public void detach(Product product) {
            entityManager.detach(product);
        }

        /**
         * Refresh entity from database
         */
        @Transactional
        public void refresh(Product product) {
            entityManager.refresh(product);
        }
    }

    /**
     * Product Service
     */
    @Service
    public static class ProductService {

        private final ProductRepository repository;

        public ProductService(ProductRepository repository) {
            this.repository = repository;
        }

        @Transactional
        public Product createProduct(Product product) {
            Audit audit = new Audit();
            audit.setCreatedBy("system");
            audit.setCreatedDate(new java.util.Date());
            product.setAudit(audit);
            return repository.save(product);
        }

        public Optional<Product> getProduct(Long id) {
            return repository.findById(id);
        }

        public List<Product> getAllProducts() {
            return repository.findAll();
        }

        public List<Product> searchProducts(String name) {
            return repository.findByName(name);
        }

        public List<Product> getProductsByPriceRange(Double min, Double max) {
            return repository.findByPriceRange(min, max);
        }

        @Transactional
        public void updateProductPrice(Long id, Double newPrice) {
            repository.updatePrice(id, newPrice);
        }

        @Transactional
        public void deleteProduct(Long id) {
            repository.delete(id);
        }

        public Long getTotalProducts() {
            return repository.count();
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/jpa")
    public static class ProductController {

        private final ProductService service;

        public ProductController(ProductService service) {
            this.service = service;
        }

        @PostMapping("/products")
        public Product createProduct(@RequestBody Product product) {
            return service.createProduct(product);
        }

        @GetMapping("/products/{id}")
        public Optional<Product> getProduct(@PathVariable Long id) {
            return service.getProduct(id);
        }

        @GetMapping("/products")
        public List<Product> getAllProducts() {
            return service.getAllProducts();
        }

        @GetMapping("/products/search")
        public List<Product> searchProducts(@RequestParam String name) {
            return service.searchProducts(name);
        }

        @GetMapping("/products/price-range")
        public List<Product> getProductsByPrice(
                @RequestParam Double min, 
                @RequestParam Double max) {
            return service.getProductsByPriceRange(min, max);
        }

        @PutMapping("/products/{id}/price")
        public void updatePrice(@PathVariable Long id, @RequestParam Double price) {
            service.updateProductPrice(id, price);
        }

        @DeleteMapping("/products/{id}")
        public void deleteProduct(@PathVariable Long id) {
            service.deleteProduct(id);
        }

        @GetMapping("/products/count")
        public Long getCount() {
            return service.getTotalProducts();
        }
    }
}

/**
 * Configuration Examples:
 * 
 * application.properties:
 * 
 * # DataSource
 * spring.datasource.url=jdbc:h2:mem:testdb
 * spring.datasource.username=sa
 * spring.datasource.password=
 * 
 * # JPA
 * spring.jpa.hibernate.ddl-auto=update
 * spring.jpa.show-sql=true
 * spring.jpa.properties.hibernate.format_sql=true
 * spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
 * 
 * # Enable second level cache
 * spring.jpa.properties.hibernate.cache.use_second_level_cache=true
 * spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
 * 
 * # Enable query cache
 * spring.jpa.properties.hibernate.cache.use_query_cache=true
 * 
 * 
 * Usage Examples:
 * 
 * 1. Create Product:
 * POST /api/jpa/products
 * {
 *   "name": "Laptop",
 *   "description": "High-performance laptop",
 *   "price": 999.99,
 *   "quantity": 10
 * }
 * 
 * 2. Get all products:
 * GET /api/jpa/products
 * 
 * 3. Search products:
 * GET /api/jpa/products/search?name=Laptop
 * 
 * 4. Get by price range:
 * GET /api/jpa/products/price-range?min=100&max=1000
 * 
 * 
 * Best Practices:
 * 
 * 1. Use @Transactional on service methods
 * 2. Leverage lazy loading for associations
 * 3. Use JPQL for database-independent queries
 * 4. Enable second-level cache for read-heavy entities
 * 5. Use @Version for optimistic locking
 * 6. Clear EntityManager for batch operations
 * 7. Use DTOs to avoid lazy loading issues
 */
