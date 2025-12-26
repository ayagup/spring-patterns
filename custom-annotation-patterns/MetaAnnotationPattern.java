package com.example.customannotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.*;

/**
 * Meta-Annotation Pattern
 * 
 * Demonstrates creating annotations that are themselves annotated with other annotations.
 * Meta-annotations allow you to combine multiple annotations into a single custom annotation,
 * providing:
 * - Annotation composition
 * - Reusable annotation patterns
 * - Domain-specific vocabulary
 * - Centralized annotation configuration
 * - Reduced code duplication
 * 
 * Key Features:
 * - Annotations on annotations
 * - @AliasFor for attribute mapping
 * - Composed annotations
 * - Stereotype meta-annotations
 * - Security meta-annotations
 * - Mapping meta-annotations
 * 
 * Use Cases:
 * - Create domain-specific annotations
 * - Combine common annotation sets
 * - Simplify repetitive annotations
 * - Enforce annotation standards
 * - Create custom stereotypes
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class MetaAnnotationPattern {

    public static void main(String[] args) {
        SpringApplication.run(MetaAnnotationPattern.class, args);
    }

    // =========================================================================
    // BASIC META-ANNOTATIONS
    // =========================================================================

    /**
     * Meta-annotation combining @Service with custom behavior
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Service // This is a meta-annotation
    public @interface BusinessService {
        
        /**
         * Service name (alias for Spring's @Service value)
         */
        @AliasFor(annotation = Service.class)
        String value() default "";
        
        /**
         * Business domain
         */
        String domain() default "";
        
        /**
         * Whether service is transactional
         */
        boolean transactional() default true;
    }

    /**
     * Meta-annotation for REST controllers with common configuration
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @RestController // Meta-annotation
    @RequestMapping // Meta-annotation
    public @interface ApiController {
        
        /**
         * API path (alias for @RequestMapping value)
         */
        @AliasFor(annotation = RequestMapping.class, attribute = "value")
        String[] value() default {};
        
        /**
         * API version
         */
        String version() default "v1";
        
        /**
         * Whether API requires authentication
         */
        boolean secured() default true;
    }

    // =========================================================================
    // SECURITY META-ANNOTATIONS
    // =========================================================================

    /**
     * Meta-annotation for admin-only access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasRole('ADMIN')")
    public @interface AdminOnly {
        
        /**
         * Additional permission required
         */
        String permission() default "";
    }

    /**
     * Meta-annotation for user or admin access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public @interface UserOrAdmin {
    }

    /**
     * Meta-annotation for manager-level access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public @interface ManagerLevel {
        
        /**
         * Minimum level required
         */
        int level() default 1;
    }

    // =========================================================================
    // REQUEST MAPPING META-ANNOTATIONS
    // =========================================================================

    /**
     * Meta-annotation for GET mappings with common configuration
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @GetMapping
    @ResponseBody
    public @interface JsonGetMapping {
        
        /**
         * Request path (alias for @GetMapping value)
         */
        @AliasFor(annotation = GetMapping.class, attribute = "value")
        String[] value() default {};
        
        /**
         * Whether caching is enabled
         */
        boolean cacheable() default false;
    }

    /**
     * Meta-annotation for POST mappings
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PostMapping
    @ResponseBody
    public @interface JsonPostMapping {
        
        /**
         * Request path
         */
        @AliasFor(annotation = PostMapping.class)
        String[] value() default {};
        
        /**
         * Whether validation is required
         */
        boolean validated() default true;
    }

    /**
     * Meta-annotation for PUT mappings
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PutMapping
    @ResponseBody
    public @interface JsonPutMapping {
        
        @AliasFor(annotation = PutMapping.class)
        String[] value() default {};
    }

    /**
     * Meta-annotation for DELETE mappings
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @DeleteMapping
    @ResponseBody
    public @interface JsonDeleteMapping {
        
        @AliasFor(annotation = DeleteMapping.class)
        String[] value() default {};
        
        /**
         * Whether soft delete is used
         */
        boolean softDelete() default false;
    }

    // =========================================================================
    // COMPONENT META-ANNOTATIONS
    // =========================================================================

    /**
     * Meta-annotation for repository components
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface DataRepository {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Data source name
         */
        String dataSource() default "primary";
        
        /**
         * Whether repository is read-only
         */
        boolean readOnly() default false;
    }

    /**
     * Meta-annotation for utility components
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface Utility {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Utility category
         */
        String category() default "general";
    }

    // =========================================================================
    // USAGE EXAMPLES
    // =========================================================================

    /**
     * Service using @BusinessService meta-annotation
     */
    @BusinessService(domain = "User Management", transactional = true)
    public static class UserManagementService {
        
        @AdminOnly
        public String createUser(String username) {
            System.out.println("Creating user: " + username);
            return "User created with admin privileges";
        }
        
        @UserOrAdmin
        public String getUserInfo(String username) {
            System.out.println("Getting user info: " + username);
            return "User info for " + username;
        }
        
        @ManagerLevel(level = 2)
        public String promoteUser(String username) {
            System.out.println("Promoting user: " + username);
            return "User promoted by manager";
        }
    }

    /**
     * Service using @BusinessService meta-annotation
     */
    @BusinessService(domain = "Order Processing", transactional = true)
    public static class OrderService {
        
        @UserOrAdmin
        public String placeOrder(String orderId) {
            System.out.println("Placing order: " + orderId);
            return "Order placed successfully";
        }
        
        @AdminOnly(permission = "ORDER_DELETE")
        public String cancelOrder(String orderId) {
            System.out.println("Canceling order: " + orderId);
            return "Order canceled by admin";
        }
    }

    /**
     * Controller using @ApiController meta-annotation
     */
    @ApiController(value = "/api/users", version = "v2", secured = true)
    public static class UserController {
        
        @JsonGetMapping("/{id}")
        public User getUser(@PathVariable String id) {
            System.out.println("GET user: " + id);
            return new User(id, "User " + id);
        }
        
        @JsonPostMapping
        public User createUser(@RequestBody User user) {
            System.out.println("POST user: " + user.getUsername());
            return user;
        }
        
        @JsonPutMapping("/{id}")
        public User updateUser(@PathVariable String id, @RequestBody User user) {
            System.out.println("PUT user: " + id);
            return user;
        }
        
        @JsonDeleteMapping("/{id}")
        public String deleteUser(@PathVariable String id) {
            System.out.println("DELETE user: " + id);
            return "User deleted: " + id;
        }
    }

    /**
     * Controller using @ApiController meta-annotation
     */
    @ApiController("/api/products")
    public static class ProductController {
        
        @JsonGetMapping
        public String getAllProducts() {
            System.out.println("GET all products");
            return "Product list";
        }
        
        @JsonGetMapping("/{id}")
        public String getProduct(@PathVariable String id) {
            System.out.println("GET product: " + id);
            return "Product " + id;
        }
        
        @JsonPostMapping
        public String createProduct(@RequestBody Product product) {
            System.out.println("POST product: " + product.getName());
            return "Product created";
        }
    }

    /**
     * Repository using @DataRepository meta-annotation
     */
    @DataRepository(dataSource = "primary", readOnly = false)
    public static class UserRepository {
        
        public User findById(String id) {
            System.out.println("Finding user by ID: " + id);
            return new User(id, "User " + id);
        }
        
        public void save(User user) {
            System.out.println("Saving user: " + user.getUsername());
        }
    }

    /**
     * Repository using @DataRepository meta-annotation
     */
    @DataRepository(dataSource = "secondary", readOnly = true)
    public static class ReadOnlyReportRepository {
        
        public String generateReport(String type) {
            System.out.println("Generating report: " + type);
            return "Report data";
        }
    }

    /**
     * Utility using @Utility meta-annotation
     */
    @Utility(category = "String Processing")
    public static class StringUtils {
        
        public String sanitize(String input) {
            System.out.println("Sanitizing: " + input);
            return input != null ? input.trim() : "";
        }
    }

    /**
     * Utility using @Utility meta-annotation
     */
    @Utility(category = "Data Conversion")
    public static class DataConverter {
        
        public String toJson(Object obj) {
            System.out.println("Converting to JSON: " + obj);
            return "{}";
        }
    }

    // Domain Classes

    public static class User {
        private String id;
        private String username;

        public User(String id, String username) {
            this.id = id;
            this.username = username;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class Product {
        private String id;
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}

/**
 * DOCUMENTATION
 * 
 * Meta-Annotation Pattern:
 * 
 * 1. What is a Meta-Annotation?
 *    - An annotation that is applied to another annotation
 *    - Allows annotation composition
 *    - Creates custom stereotypes
 *    - Examples: @Service, @Controller on custom annotations
 * 
 * 2. Core Meta-Annotations in Spring:
 *    - @Component: Base stereotype annotation
 *    - @Service: Service layer meta-annotation
 *    - @Repository: Data access layer meta-annotation
 *    - @Controller: Web controller meta-annotation
 *    - @RestController: RESTful controller meta-annotation
 *    - @Configuration: Configuration class meta-annotation
 * 
 * 3. Built-in Java Meta-Annotations:
 *    - @Target: Specifies where annotation can be applied
 *    - @Retention: Specifies how long annotation is retained
 *    - @Documented: Include in JavaDoc
 *    - @Inherited: Inherited by subclasses
 *    - @Repeatable: Can be applied multiple times
 * 
 * 4. @AliasFor Annotation:
 *    - Maps attributes between annotations
 *    - Syntax: @AliasFor(annotation = X.class, attribute = "y")
 *    - Allows attribute overriding
 *    - Creates attribute aliases
 *    - Example: Map custom value to @RequestMapping value
 * 
 * 5. Benefits:
 *    - Code reduction: One annotation instead of many
 *    - Consistency: Enforces standard patterns
 *    - Semantics: Domain-specific vocabulary
 *    - Maintainability: Change in one place
 *    - Readability: Clear intent
 * 
 * 6. Common Patterns:
 *    - Stereotype annotations (@BusinessService)
 *    - Security annotations (@AdminOnly)
 *    - Mapping annotations (@JsonGetMapping)
 *    - Qualifier annotations (@Primary with custom logic)
 *    - Validation annotations (custom constraints)
 * 
 * 7. Creating Meta-Annotations:
 *    - Annotate with existing annotations
 *    - Add custom attributes
 *    - Use @AliasFor for attribute mapping
 *    - Document purpose and usage
 *    - Consider retention and target
 * 
 * 8. @AliasFor Examples:
 *    - @AliasFor(annotation = Service.class)
 *      Maps to Spring's @Service value
 *    - @AliasFor(annotation = RequestMapping.class, attribute = "value")
 *      Maps to @RequestMapping's value attribute
 *    - @AliasFor("path")
 *      Alias within same annotation
 * 
 * 9. Retention Policies:
 *    - SOURCE: Discarded by compiler
 *    - CLASS: Retained in class file (default)
 *    - RUNTIME: Available at runtime via reflection
 *    - Spring annotations use RUNTIME
 * 
 * 10. Target Elements:
 *     - TYPE: Classes, interfaces, enums
 *     - FIELD: Fields (instance variables)
 *     - METHOD: Methods
 *     - PARAMETER: Method parameters
 *     - CONSTRUCTOR: Constructors
 *     - LOCAL_VARIABLE: Local variables
 *     - ANNOTATION_TYPE: Other annotations
 *     - PACKAGE: Package declarations
 *     - TYPE_PARAMETER: Type parameters (generics)
 *     - TYPE_USE: Any use of a type
 * 
 * 11. Best Practices:
 *     - Use for common annotation combinations
 *     - Create domain-specific vocabulary
 *     - Document custom attributes
 *     - Use @AliasFor for consistency
 *     - Keep meta-annotations focused
 *     - Consider backward compatibility
 * 
 * 12. Spring Processing:
 *     - Spring scans for meta-annotated classes
 *     - Processes transitively (annotations on annotations)
 *     - Attribute overrides via @AliasFor
 *     - Component scanning finds meta-stereotypes
 *     - Security processes meta-security annotations
 * 
 * 13. Testing Meta-Annotations:
 *     - Test annotation detection
 *     - Verify attribute mapping
 *     - Check Spring bean registration
 *     - Validate security enforcement
 *     - Test with component scanning
 */
