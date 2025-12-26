package com.example.methodsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import java.util.Arrays;
import java.util.List;

/**
 * Pre-Authorization Pattern
 * 
 * Demonstrates Spring Security's @PreAuthorize annotation for:
 * - Method-level security before method execution
 * - Role-based access control
 * - Expression-based authorization
 * - Parameter-based security checks
 * - Custom security expressions
 * 
 * Key Features:
 * - SpEL (Spring Expression Language) support
 * - Built-in security expressions
 * - Custom expression handlers
 * - Fine-grained access control
 * - Runtime authorization decisions
 * 
 * Use Cases:
 * - Restrict access by roles
 * - Validate ownership before operations
 * - Check complex business rules
 * - Enforce data-level security
 * - Audit access attempts
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class PreAuthorizationPattern {

    public static void main(String[] args) {
        SpringApplication.run(PreAuthorizationPattern.class, args);
    }

    /**
     * Enable method security with pre/post annotations
     */
    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder().encode("admin123"))
                    .authorities("ROLE_ADMIN", "ROLE_USER")
                    .build();

            UserDetails user = User.builder()
                    .username("user")
                    .password(passwordEncoder().encode("user123"))
                    .authorities("ROLE_USER")
                    .build();

            UserDetails manager = User.builder()
                    .username("manager")
                    .password(passwordEncoder().encode("manager123"))
                    .authorities("ROLE_MANAGER", "ROLE_USER")
                    .build();

            return new InMemoryUserDetailsManager(admin, user, manager);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    /**
     * Basic role-based pre-authorization
     */
    @Service
    public static class UserService {

        /**
         * Only ADMIN can access
         */
        @PreAuthorize("hasRole('ADMIN')")
        public List<UserDto> getAllUsers() {
            System.out.println("Fetching all users (ADMIN only)");
            List<UserDto> users = new ArrayList<>();
            users.add(new UserDto(1L, "john_doe", "john@example.com"));
            users.add(new UserDto(2L, "jane_doe", "jane@example.com"));
            return users;
        }

        /**
         * Either ADMIN or MANAGER can access
         */
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
        public void deleteUser(Long userId) {
            System.out.println("Deleting user: " + userId + " (ADMIN or MANAGER only)");
        }

        /**
         * Any authenticated user can access
         */
        @PreAuthorize("isAuthenticated()")
        public UserDto getProfile() {
            System.out.println("Getting current user profile");
            return new UserDto(1L, getCurrentUsername(), "user@example.com");
        }

        /**
         * Anonymous access allowed
         */
        @PreAuthorize("permitAll()")
        public String getPublicInfo() {
            return "This is public information";
        }

        /**
         * Deny all access
         */
        @PreAuthorize("denyAll()")
        public String getSecretInfo() {
            return "This method can never be accessed";
        }

        private String getCurrentUsername() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "anonymous";
        }
    }

    /**
     * Parameter-based authorization
     */
    @Service
    public static class DocumentService {

        /**
         * Check if user owns the document
         * #userId refers to method parameter
         */
        @PreAuthorize("#userId == authentication.principal.username")
        public Document getUserDocument(String userId, Long documentId) {
            System.out.println("Accessing document " + documentId + " for user " + userId);
            return new Document(documentId, userId, "Document Title", "Content");
        }

        /**
         * Complex expression with multiple conditions
         */
        @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.username)")
        public void updateDocument(String userId, Long documentId, String content) {
            System.out.println("Updating document " + documentId + " for user " + userId);
        }

        /**
         * Check object properties in authorization
         */
        @PreAuthorize("hasRole('ADMIN') or (#doc.owner == authentication.principal.username)")
        public void saveDocument(Document doc) {
            System.out.println("Saving document owned by: " + doc.getOwner());
        }

        /**
         * Validate business rules before execution
         */
        @PreAuthorize("#amount.compareTo(new java.math.BigDecimal('1000')) <= 0 or hasRole('MANAGER')")
        public void processPayment(BigDecimal amount) {
            System.out.println("Processing payment: $" + amount);
        }
    }

    /**
     * Custom security expressions
     */
    @Service
    public static class ProjectService {

        /**
         * Using custom security service
         */
        @PreAuthorize("@projectSecurityService.canAccessProject(#projectId)")
        public Project getProject(Long projectId) {
            System.out.println("Accessing project: " + projectId);
            return new Project(projectId, "Project Name", "owner");
        }

        /**
         * Check project membership
         */
        @PreAuthorize("@projectSecurityService.isMember(#projectId, authentication.principal.username)")
        public void addTask(Long projectId, String taskName) {
            System.out.println("Adding task '" + taskName + "' to project " + projectId);
        }

        /**
         * Complex business logic in security service
         */
        @PreAuthorize("@projectSecurityService.canEditProject(#projectId, authentication)")
        public void updateProject(Long projectId, String newName) {
            System.out.println("Updating project " + projectId + " to '" + newName + "'");
        }

        /**
         * Multiple security checks
         */
        @PreAuthorize("hasRole('USER') and @projectSecurityService.isProjectActive(#projectId)")
        public void contributeToProject(Long projectId, String contribution) {
            System.out.println("Contributing to project " + projectId);
        }
    }

    /**
     * Custom security service for complex authorization logic
     */
    @Service("projectSecurityService")
    public static class ProjectSecurityService {

        public boolean canAccessProject(Long projectId) {
            // Check database, cache, or business rules
            System.out.println("Checking access for project: " + projectId);
            return projectId != null && projectId > 0;
        }

        public boolean isMember(Long projectId, String username) {
            // Check project membership
            System.out.println("Checking if " + username + " is member of project " + projectId);
            return true; // Simplified
        }

        public boolean canEditProject(Long projectId, Authentication authentication) {
            // Complex logic: check role, ownership, permissions
            if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }
            
            // Check if user is project owner
            String username = authentication.getName();
            System.out.println("Checking edit permission for " + username + " on project " + projectId);
            return true; // Simplified
        }

        public boolean isProjectActive(Long projectId) {
            // Check project status
            System.out.println("Checking if project " + projectId + " is active");
            return true; // Simplified
        }
    }

    /**
     * REST Controller with pre-authorization
     */
    @RestController
    @RequestMapping("/api/admin")
    public static class AdminController {

        private final UserService userService;

        public AdminController(UserService userService) {
            this.userService = userService;
        }

        /**
         * Method-level security on controller
         */
        @GetMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
        public List<UserDto> listUsers() {
            return userService.getAllUsers();
        }

        @DeleteMapping("/users/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
        public String deleteUser(@PathVariable Long id) {
            userService.deleteUser(id);
            return "User deleted";
        }

        /**
         * Combined with parameter validation
         */
        @PostMapping("/users/{userId}/promote")
        @PreAuthorize("hasRole('ADMIN') and #userId > 0")
        public String promoteUser(@PathVariable Long userId) {
            System.out.println("Promoting user: " + userId);
            return "User promoted";
        }
    }

    /**
     * Advanced expression examples
     */
    @Service
    public static class AdvancedSecurityService {

        /**
         * Check multiple authorities
         */
        @PreAuthorize("hasAuthority('READ_PRIVILEGE') and hasAuthority('WRITE_PRIVILEGE')")
        public void performSensitiveOperation() {
            System.out.println("Performing sensitive operation");
        }

        /**
         * Check authentication details
         */
        @PreAuthorize("authentication.principal.enabled and authentication.principal.accountNonExpired")
        public void accessPremiumFeature() {
            System.out.println("Accessing premium feature");
        }

        /**
         * Time-based security
         */
        @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and T(java.time.LocalTime).now().hour >= 9 and T(java.time.LocalTime).now().hour < 17)")
        public void performScheduledTask() {
            System.out.println("Performing scheduled task (business hours for regular users)");
        }

        /**
         * Environment-based security
         */
        @PreAuthorize("hasRole('ADMIN') or @environment.getProperty('feature.enabled') == 'true'")
        public void useNewFeature() {
            System.out.println("Using new feature");
        }

        /**
         * Collection-based security
         */
        @PreAuthorize("hasRole('ADMIN') or #allowedUsers.contains(authentication.principal.username)")
        public void restrictedAccess(List<String> allowedUsers) {
            System.out.println("Restricted access granted");
        }
    }

    // Domain Classes

    public static class UserDto {
        private Long id;
        private String username;
        private String email;

        public UserDto(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class Document {
        private Long id;
        private String owner;
        private String title;
        private String content;

        public Document(Long id, String owner, String title, String content) {
            this.id = id;
            this.owner = owner;
            this.title = title;
            this.content = content;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class Project {
        private Long id;
        private String name;
        private String owner;

        public Project(Long id, String name, String owner) {
            this.id = id;
            this.name = name;
            this.owner = owner;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
    }
}

/**
 * DOCUMENTATION
 * 
 * @PreAuthorize Annotation:
 * 
 * 1. Configuration:
 *    - @EnableGlobalMethodSecurity(prePostEnabled = true)
 *    - Enables @PreAuthorize and @PostAuthorize
 *    - Can be applied to interfaces or classes
 * 
 * 2. Built-in Expressions:
 *    - hasRole('ROLE_NAME'): Check single role
 *    - hasAnyRole('ROLE1', 'ROLE2'): Check multiple roles
 *    - hasAuthority('AUTHORITY'): Check authority
 *    - hasAnyAuthority('AUTH1', 'AUTH2'): Check multiple authorities
 *    - isAuthenticated(): User is authenticated
 *    - isAnonymous(): User is anonymous
 *    - permitAll(): Allow all
 *    - denyAll(): Deny all
 *    - principal: Current user object
 *    - authentication: Authentication object
 * 
 * 3. SpEL Features:
 *    - #methodParameter: Access method parameters
 *    - #object.property: Access object properties
 *    - T(ClassName): Access static methods
 *    - @beanName: Access Spring beans
 *    - Arithmetic operators: +, -, *, /, %
 *    - Comparison operators: <, >, <=, >=, ==, !=
 *    - Logical operators: and, or, not
 *    - Collection operations: contains, size
 * 
 * 4. Custom Security Services:
 *    - Create @Service with security logic
 *    - Reference with @beanName in expressions
 *    - Enables complex business rules
 *    - Separates security concerns
 * 
 * 5. Best Practices:
 *    - Keep expressions simple and readable
 *    - Extract complex logic to security services
 *    - Use parameter names for clarity
 *    - Test authorization thoroughly
 *    - Document security requirements
 *    - Handle AccessDeniedException globally
 * 
 * Common Patterns:
 * - Role-based: hasRole('ADMIN')
 * - Ownership: #userId == authentication.principal.username
 * - Custom logic: @securityService.canAccess(#id)
 * - Multiple checks: hasRole('ADMIN') or #owner == principal.username
 * 
 * Performance Considerations:
 * - Expressions evaluated on each method call
 * - Cache expensive checks in security service
 * - Avoid database queries in expressions
 * - Use method caching where appropriate
 */
