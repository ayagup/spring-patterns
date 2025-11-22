package com.example.querypatterns;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
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
 * Query DSL Pattern Implementation
 * 
 * Demonstrates type-safe query construction using Querydsl.
 * 
 * Key Components:
 * - JPAQueryFactory for query creation
 * - Q-types for type-safe query building
 * - QuerydslPredicateExecutor for repository integration
 * - Dynamic predicate composition
 * - Complex joins and projections
 * 
 * Benefits:
 * - Type-safe queries (compile-time checking)
 * - Better IDE support with autocomplete
 * - Refactoring-friendly
 * - Readable and maintainable code
 * - Reusable predicates
 * 
 * Use Cases:
 * - Complex dynamic queries
 * - Advanced filtering requirements
 * - Type-safe database queries
 * - Domain-specific query languages
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class QueryDSLPattern {

    public static void main(String[] args) {
        SpringApplication.run(QueryDSLPattern.class, args);
    }

    /**
     * Product Entity
     * 
     * To generate Q-classes, add querydsl-apt dependency and annotation processor:
     * 
     * <dependency>
     *     <groupId>com.querydsl</groupId>
     *     <artifactId>querydsl-apt</artifactId>
     *     <scope>provided</scope>
     * </dependency>
     * <dependency>
     *     <groupId>com.querydsl</groupId>
     *     <artifactId>querydsl-jpa</artifactId>
     * </dependency>
     */
    @Entity
    @Table(name = "products")
    public static class Product {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @Column(nullable = false)
        private String name;
        
        private String category;
        
        @Column(nullable = false)
        private BigDecimal price;
        
        private Integer stockQuantity;
        
        private String brand;
        
        private Boolean active;
        
        @Column(name = "created_date")
        private LocalDateTime createdDate;
        
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "supplier_id")
        private Supplier supplier;
        
        // Constructors
        public Product() {
            this.active = true;
            this.createdDate = LocalDateTime.now();
        }
        
        public Product(String name, String category, BigDecimal price, Integer stockQuantity, String brand) {
            this();
            this.name = name;
            this.category = category;
            this.price = price;
            this.stockQuantity = stockQuantity;
            this.brand = brand;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
        
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
        
        public Supplier getSupplier() { return supplier; }
        public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    }
    
    /**
     * Supplier Entity
     */
    @Entity
    @Table(name = "suppliers")
    public static class Supplier {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @Column(nullable = false)
        private String name;
        
        private String country;
        
        private String contactEmail;
        
        // Constructors
        public Supplier() {}
        
        public Supplier(String name, String country, String contactEmail) {
            this.name = name;
            this.country = country;
            this.contactEmail = contactEmail;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    }
    
    /**
     * Repository with Querydsl Support
     * 
     * QuerydslPredicateExecutor provides:
     * - findOne(Predicate predicate)
     * - findAll(Predicate predicate)
     * - count(Predicate predicate)
     * - exists(Predicate predicate)
     */
    @Repository
    public interface ProductRepository extends JpaRepository<Product, Long>, 
                                               QuerydslPredicateExecutor<Product>,
                                               QuerydslBinderCustomizer<QProduct> {
        
        @Override
        default void customize(QuerydslBindings bindings, QProduct product) {
            // Customize query bindings for web support
            bindings.bind(product.name).first((path, value) -> path.containsIgnoreCase(value));
            bindings.bind(product.price).all((path, values) -> {
                var iterator = values.iterator();
                BigDecimal min = iterator.next();
                BigDecimal max = iterator.hasNext() ? iterator.next() : null;
                if (max != null) {
                    return Optional.of(path.between(min, max));
                }
                return Optional.of(path.goe(min));
            });
        }
    }
    
    /**
     * Mock Q-Class for Product (normally generated by Querydsl APT)
     * 
     * In real application, this is auto-generated from Product entity
     */
    public static class QProduct {
        public static final QProduct product = new QProduct("product");
        
        public final com.querydsl.core.types.dsl.NumberPath<Long> id;
        public final com.querydsl.core.types.dsl.StringPath name;
        public final com.querydsl.core.types.dsl.StringPath category;
        public final com.querydsl.core.types.dsl.NumberPath<BigDecimal> price;
        public final com.querydsl.core.types.dsl.NumberPath<Integer> stockQuantity;
        public final com.querydsl.core.types.dsl.StringPath brand;
        public final com.querydsl.core.types.dsl.BooleanPath active;
        public final com.querydsl.core.types.dsl.DateTimePath<LocalDateTime> createdDate;
        
        public QProduct(String variable) {
            this.id = new com.querydsl.core.types.dsl.NumberPath<>(Long.class, variable, "id");
            this.name = new com.querydsl.core.types.dsl.StringPath(variable + ".name");
            this.category = new com.querydsl.core.types.dsl.StringPath(variable + ".category");
            this.price = new com.querydsl.core.types.dsl.NumberPath<>(BigDecimal.class, variable, "price");
            this.stockQuantity = new com.querydsl.core.types.dsl.NumberPath<>(Integer.class, variable, "stockQuantity");
            this.brand = new com.querydsl.core.types.dsl.StringPath(variable + ".brand");
            this.active = new com.querydsl.core.types.dsl.BooleanPath(variable + ".active");
            this.createdDate = new com.querydsl.core.types.dsl.DateTimePath<>(LocalDateTime.class, variable, "createdDate");
        }
    }
    
    /**
     * Querydsl Service with Advanced Queries
     */
    @Service
    @Transactional
    public static class ProductQueryService {
        
        private final ProductRepository productRepository;
        private final JPAQueryFactory queryFactory;
        
        public ProductQueryService(ProductRepository productRepository, EntityManager entityManager) {
            this.productRepository = productRepository;
            this.queryFactory = new JPAQueryFactory(entityManager);
        }
        
        /**
         * Simple predicate query
         */
        public List<Product> findProductsByCategory(String category) {
            QProduct product = QProduct.product;
            BooleanExpression predicate = product.category.eq(category)
                                                          .and(product.active.isTrue());
            
            return (List<Product>) productRepository.findAll(predicate);
        }
        
        /**
         * Complex dynamic query with multiple conditions
         */
        public List<Product> searchProducts(String name, String category, 
                                           BigDecimal minPrice, BigDecimal maxPrice,
                                           String brand, Boolean inStock) {
            QProduct product = QProduct.product;
            
            // Build dynamic predicate
            BooleanExpression predicate = product.active.isTrue();
            
            if (name != null && !name.isEmpty()) {
                predicate = predicate.and(product.name.containsIgnoreCase(name));
            }
            
            if (category != null && !category.isEmpty()) {
                predicate = predicate.and(product.category.eq(category));
            }
            
            if (minPrice != null) {
                predicate = predicate.and(product.price.goe(minPrice));
            }
            
            if (maxPrice != null) {
                predicate = predicate.and(product.price.loe(maxPrice));
            }
            
            if (brand != null && !brand.isEmpty()) {
                predicate = predicate.and(product.brand.eq(brand));
            }
            
            if (inStock != null && inStock) {
                predicate = predicate.and(product.stockQuantity.gt(0));
            }
            
            return (List<Product>) productRepository.findAll(predicate);
        }
        
        /**
         * Query with ordering and pagination
         */
        public List<Product> findTopExpensiveProducts(int limit) {
            QProduct product = QProduct.product;
            
            return queryFactory.selectFrom(product)
                              .where(product.active.isTrue())
                              .orderBy(product.price.desc())
                              .limit(limit)
                              .fetch();
        }
        
        /**
         * Aggregation query
         */
        public Long countProductsByCategory(String category) {
            QProduct product = QProduct.product;
            
            return queryFactory.select(product.count())
                              .from(product)
                              .where(product.category.eq(category)
                                    .and(product.active.isTrue()))
                              .fetchOne();
        }
        
        /**
         * Query with grouping
         */
        public List<CategoryStats> getCategoryStatistics() {
            QProduct product = QProduct.product;
            
            return queryFactory.select(
                    new QCategoryStats(
                        product.category,
                        product.count(),
                        product.price.avg(),
                        product.price.min(),
                        product.price.max()
                    ))
                    .from(product)
                    .where(product.active.isTrue())
                    .groupBy(product.category)
                    .fetch();
        }
        
        /**
         * Subquery example
         */
        public List<Product> findProductsAboveAveragePrice() {
            QProduct product = QProduct.product;
            QProduct productSub = new QProduct("productSub");
            
            return queryFactory.selectFrom(product)
                              .where(product.price.gt(
                                  JPAExpressions.select(productSub.price.avg())
                                               .from(productSub)
                              ))
                              .fetch();
        }
        
        /**
         * Join query
         */
        public List<Product> findProductsBySupplierCountry(String country) {
            QProduct product = QProduct.product;
            QSupplier supplier = QSupplier.supplier;
            
            return queryFactory.selectFrom(product)
                              .join(product.supplier, supplier)
                              .where(supplier.country.eq(country)
                                    .and(product.active.isTrue()))
                              .fetch();
        }
        
        /**
         * Projection query (select specific fields)
         */
        public List<ProductSummary> getProductSummaries() {
            QProduct product = QProduct.product;
            
            return queryFactory.select(
                    new QProductSummary(
                        product.id,
                        product.name,
                        product.price
                    ))
                    .from(product)
                    .where(product.active.isTrue())
                    .fetch();
        }
        
        /**
         * Case expression in query
         */
        public List<Product> findProductsWithPriceCategory() {
            QProduct product = QProduct.product;
            
            return queryFactory.selectFrom(product)
                              .where(
                                  product.price.when(product.price.lt(new BigDecimal("100")))
                                               .then("Cheap")
                                               .when(product.price.between(new BigDecimal("100"), new BigDecimal("500")))
                                               .then("Medium")
                                               .otherwise("Expensive")
                                               .eq("Medium")
                              )
                              .fetch();
        }
        
        /**
         * Update query
         */
        public long increasePriceByCategory(String category, BigDecimal percentage) {
            QProduct product = QProduct.product;
            
            return queryFactory.update(product)
                              .set(product.price, product.price.multiply(percentage))
                              .where(product.category.eq(category))
                              .execute();
        }
        
        /**
         * Delete query
         */
        public long deleteInactiveProducts() {
            QProduct product = QProduct.product;
            
            return queryFactory.delete(product)
                              .where(product.active.isFalse())
                              .execute();
        }
    }
    
    /**
     * Mock Q-Classes for related entities
     */
    public static class QSupplier {
        public static final QSupplier supplier = new QSupplier("supplier");
        
        public final com.querydsl.core.types.dsl.StringPath country;
        
        public QSupplier(String variable) {
            this.country = new com.querydsl.core.types.dsl.StringPath(variable + ".country");
        }
    }
    
    /**
     * DTO for category statistics
     */
    public static class CategoryStats {
        private String category;
        private Long count;
        private Double avgPrice;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        
        public CategoryStats(String category, Long count, Double avgPrice, 
                           BigDecimal minPrice, BigDecimal maxPrice) {
            this.category = category;
            this.count = count;
            this.avgPrice = avgPrice;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
        
        // Getters
        public String getCategory() { return category; }
        public Long getCount() { return count; }
        public Double getAvgPrice() { return avgPrice; }
        public BigDecimal getMinPrice() { return minPrice; }
        public BigDecimal getMaxPrice() { return maxPrice; }
    }
    
    public static class QCategoryStats extends com.querydsl.core.types.ConstructorExpression<CategoryStats> {
        public QCategoryStats(com.querydsl.core.types.Expression<String> category,
                            com.querydsl.core.types.Expression<Long> count,
                            com.querydsl.core.types.Expression<Double> avgPrice,
                            com.querydsl.core.types.Expression<BigDecimal> minPrice,
                            com.querydsl.core.types.Expression<BigDecimal> maxPrice) {
            super(CategoryStats.class, new Class<?>[]{String.class, Long.class, Double.class, BigDecimal.class, BigDecimal.class},
                 category, count, avgPrice, minPrice, maxPrice);
        }
    }
    
    /**
     * DTO for product summary
     */
    public static class ProductSummary {
        private Long id;
        private String name;
        private BigDecimal price;
        
        public ProductSummary(Long id, String name, BigDecimal price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
    }
    
    public static class QProductSummary extends com.querydsl.core.types.ConstructorExpression<ProductSummary> {
        public QProductSummary(com.querydsl.core.types.Expression<Long> id,
                             com.querydsl.core.types.Expression<String> name,
                             com.querydsl.core.types.Expression<BigDecimal> price) {
            super(ProductSummary.class, new Class<?>[]{Long.class, String.class, BigDecimal.class},
                 id, name, price);
        }
    }
    
    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/querydsl/products")
    public static class ProductQueryController {
        
        private final ProductQueryService queryService;
        
        public ProductQueryController(ProductQueryService queryService) {
            this.queryService = queryService;
        }
        
        @GetMapping("/category/{category}")
        public List<Product> getByCategory(@PathVariable String category) {
            return queryService.findProductsByCategory(category);
        }
        
        @GetMapping("/search")
        public List<Product> search(
                @RequestParam(required = false) String name,
                @RequestParam(required = false) String category,
                @RequestParam(required = false) BigDecimal minPrice,
                @RequestParam(required = false) BigDecimal maxPrice,
                @RequestParam(required = false) String brand,
                @RequestParam(required = false) Boolean inStock) {
            return queryService.searchProducts(name, category, minPrice, maxPrice, brand, inStock);
        }
        
        @GetMapping("/top-expensive")
        public List<Product> getTopExpensive(@RequestParam(defaultValue = "10") int limit) {
            return queryService.findTopExpensiveProducts(limit);
        }
        
        @GetMapping("/stats")
        public List<CategoryStats> getStatistics() {
            return queryService.getCategoryStatistics();
        }
        
        @GetMapping("/above-average-price")
        public List<Product> getAboveAveragePrice() {
            return queryService.findProductsAboveAveragePrice();
        }
        
        @GetMapping("/by-supplier-country/{country}")
        public List<Product> getBySupplierCountry(@PathVariable String country) {
            return queryService.findProductsBySupplierCountry(country);
        }
        
        @GetMapping("/summaries")
        public List<ProductSummary> getSummaries() {
            return queryService.getProductSummaries();
        }
    }
}

/**
 * Configuration (application.properties):
 * 
 * spring.datasource.url=jdbc:h2:mem:querydsldb
 * spring.datasource.driver-class-name=org.h2.Driver
 * spring.jpa.hibernate.ddl-auto=create-drop
 * spring.jpa.show-sql=true
 * spring.jpa.properties.hibernate.format_sql=true
 * 
 * Maven Dependencies:
 * 
 * <dependencies>
 *     <dependency>
 *         <groupId>org.springframework.boot</groupId>
 *         <artifactId>spring-boot-starter-data-jpa</artifactId>
 *     </dependency>
 *     <dependency>
 *         <groupId>com.querydsl</groupId>
 *         <artifactId>querydsl-jpa</artifactId>
 *     </dependency>
 *     <dependency>
 *         <groupId>com.querydsl</groupId>
 *         <artifactId>querydsl-apt</artifactId>
 *         <scope>provided</scope>
 *     </dependency>
 *     <dependency>
 *         <groupId>com.h2database</groupId>
 *         <artifactId>h2</artifactId>
 *         <scope>runtime</scope>
 *     </dependency>
 * </dependencies>
 * 
 * <build>
 *     <plugins>
 *         <plugin>
 *             <groupId>com.mysema.maven</groupId>
 *             <artifactId>apt-maven-plugin</artifactId>
 *             <version>1.1.3</version>
 *             <executions>
 *                 <execution>
 *                     <goals>
 *                         <goal>process</goal>
 *                     </goals>
 *                     <configuration>
 *                         <outputDirectory>target/generated-sources/java</outputDirectory>
 *                         <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
 *                     </configuration>
 *                 </execution>
 *             </executions>
 *         </plugin>
 *     </plugins>
 * </build>
 * 
 * Best Practices:
 * 1. Use Q-classes for type-safe queries
 * 2. Reuse predicates across queries
 * 3. Prefer QuerydslPredicateExecutor for simple queries
 * 4. Use JPAQueryFactory for complex queries
 * 5. Create custom repository fragments for reusable query logic
 * 6. Use projections to fetch only required fields
 * 7. Be careful with N+1 queries - use fetch joins
 * 8. Test queries with different data sets
 * 9. Monitor query performance
 * 10. Use querydsl-sql for database-specific features
 */
