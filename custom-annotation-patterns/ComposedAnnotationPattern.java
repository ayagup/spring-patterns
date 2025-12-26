package com.example.customannotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.lang.annotation.*;

/**
 * Composed Annotation Pattern
 * 
 * Demonstrates creating composed annotations that combine multiple annotations
 * into a single, reusable annotation. This pattern:
 * - Combines multiple Spring annotations
 * - Reduces annotation clutter
 * - Creates semantic, domain-specific annotations
 * - Centralizes cross-cutting concerns
 * - Improves code readability
 * 
 * Key Features:
 * - Multiple annotation composition
 * - Attribute forwarding with @AliasFor
 * - Cross-cutting concern combinations
 * - Domain vocabulary creation
 * - Annotation inheritance
 * 
 * Use Cases:
 * - Combine security + caching + transaction
 * - Create standardized service annotations
 * - Enforce architectural patterns
 * - Simplify REST controller methods
 * - Package validation rules
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class ComposedAnnotationPattern {

    public static void main(String[] args) {
        SpringApplication.run(ComposedAnnotationPattern.class, args);
    }

    // =========================================================================
    // SERVICE LAYER COMPOSED ANNOTATIONS
    // =========================================================================

    /**
     * Composed annotation for transactional services
     * Combines @Service + @Transactional + @Validated
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Service
    @Transactional
    @Validated
    public @interface TransactionalService {
        
        /**
         * Service bean name
         */
        @AliasFor(annotation = Service.class)
        String value() default "";
        
        /**
         * Whether transaction is read-only
         */
        @AliasFor(annotation = Transactional.class)
        boolean readOnly() default false;
        
        /**
         * Transaction timeout in seconds
         */
        @AliasFor(annotation = Transactional.class)
        int timeout() default -1;
    }

    /**
     * Composed annotation for cacheable services
     * Combines @Service + @Cacheable
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Service
    public @interface CacheableService {
        
        @AliasFor(annotation = Service.class)
        String value() default "";
        
        /**
         * Default cache name for this service
         */
        String cacheName() default "default";
    }

    /**
     * Composed annotation for read-only services
     * Combines @Service + @Transactional(readOnly=true)
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Service
    @Transactional(readOnly = true)
    public @interface ReadOnlyService {
        
        @AliasFor(annotation = Service.class)
        String value() default "";
    }

    // =========================================================================
    // CONTROLLER COMPOSED ANNOTATIONS
    // =========================================================================

    /**
     * Composed annotation for secured REST endpoints
     * Combines @GetMapping + @PreAuthorize + @ResponseBody
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @GetMapping
    @ResponseBody
    public @interface SecuredGetMapping {
        
        /**
         * Request mapping path
         */
        @AliasFor(annotation = GetMapping.class)
        String[] value() default {};
        
        /**
         * Security expression
         */
        String secured() default "isAuthenticated()";
        
        /**
         * Whether caching is enabled
         */
        boolean cacheable() default false;
    }

    /**
     * Composed annotation for admin-only POST operations
     * Combines @PostMapping + @PreAuthorize("hasRole('ADMIN')") + @ResponseBody
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public @interface AdminPostMapping {
        
        @AliasFor(annotation = PostMapping.class)
        String[] value() default {};
    }

    /**
     * Composed annotation for validated PUT operations
     * Combines @PutMapping + @ResponseBody + validation
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PutMapping
    @ResponseBody
    @Validated
    public @interface ValidatedPutMapping {
        
        @AliasFor(annotation = PutMapping.class)
        String[] value() default {};
        
        /**
         * Whether optimistic locking is checked
         */
        boolean checkVersion() default true;
    }

    /**
     * Composed annotation for soft-delete operations
     * Combines @DeleteMapping + custom behavior
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @DeleteMapping
    @ResponseBody
    public @interface SoftDeleteMapping {
        
        @AliasFor(annotation = DeleteMapping.class)
        String[] value() default {};
        
        /**
         * Whether to archive before deleting
         */
        boolean archive() default true;
    }

    // =========================================================================
    // METHOD-LEVEL COMPOSED ANNOTATIONS
    // =========================================================================

    /**
     * Composed annotation for cached, transactional operations
     * Combines @Cacheable + @Transactional(readOnly=true)
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Cacheable
    @Transactional(readOnly = true)
    public @interface CachedReadOnly {
        
        /**
         * Cache name
         */
        @AliasFor(annotation = Cacheable.class, attribute = "value")
        String[] value() default {};
        
        /**
         * Cache key expression
         */
        @AliasFor(annotation = Cacheable.class)
        String key() default "";
        
        /**
         * Cache condition
         */
        @AliasFor(annotation = Cacheable.class)
        String condition() default "";
    }

    /**
     * Composed annotation for secured, transactional writes
     * Combines @Transactional + @PreAuthorize
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public @interface SecuredWrite {
        
        /**
         * Transaction timeout
         */
        @AliasFor(annotation = Transactional.class)
        int timeout() default 30;
        
        /**
         * Additional security expression
         */
        String additionalSecurity() default "";
    }

    /**
     * Composed annotation for audited operations
     * Combines custom auditing with transaction
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Transactional
    public @interface Audited {
        
        /**
         * Audit event type
         */
        String eventType();
        
        /**
         * Audit category
         */
        String category() default "GENERAL";
        
        /**
         * Transaction timeout
         */
        @AliasFor(annotation = Transactional.class)
        int timeout() default -1;
    }

    // =========================================================================
    // CROSS-CUTTING COMPOSED ANNOTATIONS
    // =========================================================================

    /**
     * Composed annotation for asynchronous, cacheable operations
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Cacheable
    public @interface AsyncCached {
        
        @AliasFor(annotation = Cacheable.class)
        String[] value() default {};
        
        /**
         * Cache TTL in seconds
         */
        long ttl() default 300;
    }

    /**
     * Composed annotation for retry-enabled operations
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Transactional
    public @interface Retryable {
        
        /**
         * Maximum retry attempts
         */
        int maxAttempts() default 3;
        
        /**
         * Backoff delay in milliseconds
         */
        long backoff() default 1000;
        
        @AliasFor(annotation = Transactional.class)
        int timeout() default 60;
    }

    // =========================================================================
    // USAGE EXAMPLES
    // =========================================================================

    /**
     * Service using @TransactionalService
     */
    @TransactionalService(readOnly = false, timeout = 30)
    public static class UserService {
        
        @SecuredWrite(timeout = 60)
        public User createUser(@NotNull String username, @NotNull String email) {
            System.out.println("Creating user: " + username);
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            return user;
        }
        
        @Audited(eventType = "USER_UPDATE", category = "SECURITY")
        public void updateUser(Long userId, String email) {
            System.out.println("Updating user: " + userId + " with email: " + email);
        }
        
        @Retryable(maxAttempts = 5, backoff = 2000)
        public void syncUserData(Long userId) {
            System.out.println("Syncing user data: " + userId);
            // Simulated external API call that might fail
        }
    }

    /**
     * Service using @ReadOnlyService
     */
    @ReadOnlyService
    public static class ReportService {
        
        @CachedReadOnly(value = "reports", key = "#reportType", condition = "#cache")
        public String generateReport(String reportType, boolean cache) {
            System.out.println("Generating report: " + reportType);
            return "Report data for " + reportType;
        }
        
        @AsyncCached(value = "analytics", ttl = 600)
        public String calculateAnalytics(String metric) {
            System.out.println("Calculating analytics: " + metric);
            return "Analytics for " + metric;
        }
    }

    /**
     * Service using @CacheableService
     */
    @CacheableService(cacheName = "products")
    public static class ProductService {
        
        @Cacheable(value = "products", key = "#productId")
        @Transactional(readOnly = true)
        public Product getProduct(Long productId) {
            System.out.println("Fetching product: " + productId);
            Product product = new Product();
            product.setId(productId);
            product.setName("Product " + productId);
            return product;
        }
        
        @SecuredWrite
        public Product updateProduct(Long productId, String name) {
            System.out.println("Updating product: " + productId);
            Product product = new Product();
            product.setId(productId);
            product.setName(name);
            return product;
        }
    }

    /**
     * Controller using composed mapping annotations
     */
    @RestController
    @RequestMapping("/api/users")
    public static class UserController {
        
        private final UserService userService;

        public UserController(UserService userService) {
            this.userService = userService;
        }

        @SecuredGetMapping(value = "/{id}", secured = "hasRole('USER')", cacheable = true)
        public User getUser(@PathVariable Long id) {
            System.out.println("GET user: " + id);
            return new User();
        }
        
        @AdminPostMapping
        public User createUser(@RequestBody User user) {
            System.out.println("POST user: " + user.getUsername());
            return userService.createUser(user.getUsername(), user.getEmail());
        }
        
        @ValidatedPutMapping("/{id}")
        public User updateUser(@PathVariable Long id, @RequestBody User user) {
            System.out.println("PUT user: " + id);
            userService.updateUser(id, user.getEmail());
            return user;
        }
        
        @SoftDeleteMapping("/{id}")
        public String deleteUser(@PathVariable Long id) {
            System.out.println("Soft DELETE user: " + id);
            return "User " + id + " marked as deleted";
        }
    }

    /**
     * Controller using composed mapping annotations
     */
    @RestController
    @RequestMapping("/api/products")
    public static class ProductController {
        
        private final ProductService productService;

        public ProductController(ProductService productService) {
            this.productService = productService;
        }

        @SecuredGetMapping("/{id}")
        public Product getProduct(@PathVariable Long id) {
            return productService.getProduct(id);
        }
        
        @AdminPostMapping
        public Product createProduct(@RequestBody Product product) {
            System.out.println("POST product: " + product.getName());
            return product;
        }
        
        @ValidatedPutMapping("/{id}")
        public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
            return productService.updateProduct(id, product.getName());
        }
    }

    // Domain Classes

    public static class User {
        private Long id;
        private String username;
        private String email;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class Product {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}

/**
 * DOCUMENTATION
 * 
 * Composed Annotation Pattern:
 * 
 * 1. Definition:
 *    - Annotation that combines multiple other annotations
 *    - Creates higher-level semantic annotations
 *    - Reduces repetition and improves readability
 *    - Enforces consistent configuration
 * 
 * 2. Composition Strategies:
 *    a) Service Layer Composition:
 *       - @Service + @Transactional = @TransactionalService
 *       - @Service + @Validated = @ValidatedService
 *       - @Service + @Cacheable = @CacheableService
 *    
 *    b) Controller Composition:
 *       - @GetMapping + @ResponseBody = @GetJson
 *       - @PostMapping + @PreAuthorize = @SecuredPost
 *       - @PutMapping + @Validated = @ValidatedPut
 *    
 *    c) Cross-Cutting Composition:
 *       - @Transactional + @Cacheable = @CachedReadOnly
 *       - @Transactional + @PreAuthorize = @SecuredWrite
 *       - @Async + @Cacheable = @AsyncCached
 * 
 * 3. Benefits:
 *    - Reduces annotation clutter
 *    - Creates domain vocabulary
 *    - Centralizes configuration
 *    - Improves maintainability
 *    - Enforces standards
 *    - Better readability
 * 
 * 4. @AliasFor Usage:
 *    - Forward attributes to composed annotations
 *    - Syntax: @AliasFor(annotation = X.class, attribute = "y")
 *    - Allows customization while keeping composition
 *    - Example: Forward timeout to @Transactional
 * 
 * 5. Common Combinations:
 *    - Security + Transaction: @SecuredWrite
 *    - Cache + Transaction: @CachedReadOnly
 *    - Validation + Mapping: @ValidatedPost
 *    - Async + Cache: @AsyncCached
 *    - Audit + Transaction: @Audited
 * 
 * 6. Design Considerations:
 *    - Keep compositions focused
 *    - Document composed annotations well
 *    - Use meaningful names
 *    - Provide sensible defaults
 *    - Allow overrides via @AliasFor
 * 
 * 7. Spring Processing:
 *    - Spring processes all composed annotations
 *    - Transitively applies behavior
 *    - Respects attribute overrides
 *    - Maintains annotation order
 * 
 * 8. Example Compositions:
 *    
 *    @TransactionalService:
 *    - Marks as Spring service bean
 *    - Enables transaction management
 *    - Enables method validation
 *    - Configurable read-only and timeout
 *    
 *    @SecuredGetMapping:
 *    - Maps GET requests
 *    - Returns JSON response
 *    - Applies security check
 *    - Optional caching
 *    
 *    @CachedReadOnly:
 *    - Caches method result
 *    - Read-only transaction
 *    - Configurable cache name and key
 * 
 * 9. When to Use:
 *    - Repeated annotation patterns
 *    - Standard architectural layers
 *    - Cross-cutting concerns
 *    - Domain-specific operations
 *    - Team coding standards
 * 
 * 10. When NOT to Use:
 *     - One-off annotation needs
 *     - Highly variable configurations
 *     - Overly complex compositions
 *     - When flexibility is more important than consistency
 * 
 * 11. Best Practices:
 *     - Create for 3+ annotation combinations
 *     - Use clear, semantic names
 *     - Document purpose and behavior
 *     - Provide attribute forwarding
 *     - Keep defaults sensible
 *     - Version carefully (breaking changes)
 * 
 * 12. Testing:
 *     - Verify all composed behaviors apply
 *     - Test attribute forwarding
 *     - Check annotation processing order
 *     - Validate security enforcement
 *     - Test transaction boundaries
 *     - Verify caching behavior
 */
