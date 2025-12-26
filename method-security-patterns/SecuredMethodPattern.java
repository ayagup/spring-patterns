package com.example.methodsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.annotation.Secured;
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

import javax.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.List;

/**
 * Secured Method Pattern
 * 
 * Demonstrates Spring Security's @Secured and @RolesAllowed annotations for:
 * - Simple role-based method security
 * - JSR-250 standard annotations
 * - Legacy security support
 * - Basic authorization without SpEL
 * - Quick security implementation
 * 
 * Key Features:
 * - @Secured annotation (Spring-specific)
 * - @RolesAllowed annotation (JSR-250 standard)
 * - Simple role checking
 * - No expression language
 * - Multiple role support
 * 
 * Use Cases:
 * - Basic role-based access control
 * - Legacy application migration
 * - Simple security requirements
 * - JSR-250 compliance
 * - Microservice authorization
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class SecuredMethodPattern {

    public static void main(String[] args) {
        SpringApplication.run(SecuredMethodPattern.class, args);
    }

    /**
     * Enable @Secured and @RolesAllowed annotations
     */
    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(
        securedEnabled = true,      // Enable @Secured
        jsr250Enabled = true        // Enable @RolesAllowed, @PermitAll, @DenyAll
    )
    public static class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder().encode("admin123"))
                    .roles("ADMIN", "USER")
                    .build();

            UserDetails manager = User.builder()
                    .username("manager")
                    .password(passwordEncoder().encode("manager123"))
                    .roles("MANAGER", "USER")
                    .build();

            UserDetails user = User.builder()
                    .username("user")
                    .password(passwordEncoder().encode("user123"))
                    .roles("USER")
                    .build();

            return new InMemoryUserDetailsManager(admin, manager, user);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    /**
     * Using @Secured annotation (Spring-specific)
     */
    @Service
    public static class UserManagementService {

        /**
         * Single role requirement
         * IMPORTANT: Use ROLE_ prefix for roles
         */
        @Secured("ROLE_ADMIN")
        public List<UserInfo> getAllUsers() {
            System.out.println("Fetching all users (ADMIN only)");
            
            List<UserInfo> users = new ArrayList<>();
            users.add(new UserInfo(1L, "john_doe", "ROLE_USER"));
            users.add(new UserInfo(2L, "jane_smith", "ROLE_MANAGER"));
            users.add(new UserInfo(3L, "admin_user", "ROLE_ADMIN"));
            
            return users;
        }

        /**
         * Multiple role requirement (any of these roles)
         */
        @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
        public void deleteUser(Long userId) {
            System.out.println("Deleting user: " + userId + " (ADMIN or MANAGER)");
        }

        /**
         * Multiple roles - strict requirement
         */
        @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
        public void approveUser(Long userId) {
            System.out.println("Approving user: " + userId);
        }

        /**
         * Anyone can call this method (not recommended for @Secured)
         * Better to use @RolesAllowed with @PermitAll
         */
        public UserInfo getPublicProfile(Long userId) {
            System.out.println("Fetching public profile: " + userId);
            return new UserInfo(userId, "public_user", "ROLE_USER");
        }
    }

    /**
     * Using @RolesAllowed annotation (JSR-250 standard)
     */
    @Service
    public static class DocumentManagementService {

        /**
         * Single role using JSR-250
         * Note: @RolesAllowed doesn't require ROLE_ prefix
         */
        @RolesAllowed("ADMIN")
        public List<DocumentInfo> getAllDocuments() {
            System.out.println("Fetching all documents (ADMIN only)");
            
            List<DocumentInfo> docs = new ArrayList<>();
            docs.add(new DocumentInfo(1L, "Document 1", "admin"));
            docs.add(new DocumentInfo(2L, "Document 2", "user"));
            
            return docs;
        }

        /**
         * Multiple roles - any of these can access
         */
        @RolesAllowed({"ADMIN", "MANAGER"})
        public void deleteDocument(Long documentId) {
            System.out.println("Deleting document: " + documentId);
        }

        /**
         * Any role can access
         */
        @RolesAllowed({"ADMIN", "MANAGER", "USER"})
        public DocumentInfo viewDocument(Long documentId) {
            System.out.println("Viewing document: " + documentId);
            return new DocumentInfo(documentId, "Sample Document", "user");
        }

        /**
         * Using @PermitAll - any authenticated or anonymous user
         */
        @javax.annotation.security.PermitAll
        public String getPublicDocument() {
            System.out.println("Fetching public document");
            return "Public content available to everyone";
        }

        /**
         * Using @DenyAll - denies all access
         */
        @javax.annotation.security.DenyAll
        public String getRestrictedDocument() {
            System.out.println("This method can never be accessed");
            return "Restricted content";
        }
    }

    /**
     * Comparison of @Secured and @RolesAllowed
     */
    @Service
    public static class ComparisonService {

        /**
         * @Secured requires ROLE_ prefix
         */
        @Secured("ROLE_ADMIN")
        public void securedMethod() {
            System.out.println("Called with @Secured - requires ROLE_ADMIN");
        }

        /**
         * @RolesAllowed doesn't require ROLE_ prefix
         */
        @RolesAllowed("ADMIN")
        public void rolesAllowedMethod() {
            System.out.println("Called with @RolesAllowed - requires ADMIN role");
        }

        /**
         * Both can have multiple roles
         */
        @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
        public void multipleSecured() {
            System.out.println("@Secured with multiple roles");
        }

        @RolesAllowed({"ADMIN", "MANAGER"})
        public void multipleRolesAllowed() {
            System.out.println("@RolesAllowed with multiple roles");
        }
    }

    /**
     * REST Controller with secured methods
     */
    @RestController
    @RequestMapping("/api/secured")
    public static class SecuredController {

        private final UserManagementService userService;
        private final DocumentManagementService documentService;

        public SecuredController(UserManagementService userService,
                                DocumentManagementService documentService) {
            this.userService = userService;
            this.documentService = documentService;
        }

        /**
         * Secured endpoint - ADMIN only
         */
        @GetMapping("/users")
        @Secured("ROLE_ADMIN")
        public List<UserInfo> getUsers() {
            return userService.getAllUsers();
        }

        /**
         * Multiple roles allowed
         */
        @DeleteMapping("/users/{id}")
        @RolesAllowed({"ADMIN", "MANAGER"})
        public String deleteUser(@PathVariable Long id) {
            userService.deleteUser(id);
            return "User deleted successfully";
        }

        /**
         * Public endpoint
         */
        @GetMapping("/public")
        @javax.annotation.security.PermitAll
        public String getPublicInfo() {
            return "This is public information";
        }

        /**
         * Document operations
         */
        @GetMapping("/documents")
        @Secured("ROLE_ADMIN")
        public List<DocumentInfo> getDocuments() {
            return documentService.getAllDocuments();
        }

        @GetMapping("/documents/{id}")
        @RolesAllowed({"ADMIN", "MANAGER", "USER"})
        public DocumentInfo getDocument(@PathVariable Long id) {
            return documentService.viewDocument(id);
        }
    }

    /**
     * Mixed security annotations service
     */
    @Service
    public static class MixedSecurityService {

        /**
         * Class-level: affects all methods if no method-level annotation
         */
        @Secured("ROLE_USER")
        public void defaultSecuredMethod() {
            System.out.println("Default security - ROLE_USER required");
        }

        /**
         * Method-level overrides class-level
         */
        @Secured("ROLE_ADMIN")
        public void adminOnlyMethod() {
            System.out.println("Admin only - method-level security");
        }

        /**
         * JSR-250 annotations can be mixed
         */
        @RolesAllowed("MANAGER")
        public void managerMethod() {
            System.out.println("Manager only - JSR-250 style");
        }

        /**
         * Public method within secured class
         */
        @javax.annotation.security.PermitAll
        public void publicMethod() {
            System.out.println("Public method accessible to all");
        }
    }

    /**
     * Authority-based (not role-based) security
     */
    @Service
    public static class AuthorityBasedService {

        /**
         * Check for specific authority (not role)
         * Authorities don't use ROLE_ prefix
         */
        @Secured("READ_PRIVILEGE")
        public void readData() {
            System.out.println("Reading data - requires READ_PRIVILEGE authority");
        }

        /**
         * Multiple authorities
         */
        @Secured({"READ_PRIVILEGE", "WRITE_PRIVILEGE"})
        public void writeData() {
            System.out.println("Writing data - requires READ and WRITE privileges");
        }
    }

    // Domain Classes

    public static class UserInfo {
        private Long id;
        private String username;
        private String role;

        public UserInfo(Long id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class DocumentInfo {
        private Long id;
        private String title;
        private String owner;

        public DocumentInfo(Long id, String title, String owner) {
            this.id = id;
            this.title = title;
            this.owner = owner;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
    }
}

/**
 * DOCUMENTATION
 * 
 * @Secured vs @RolesAllowed:
 * 
 * 1. @Secured (Spring-specific):
 *    - Requires: @EnableGlobalMethodSecurity(securedEnabled = true)
 *    - Syntax: @Secured("ROLE_NAME") or @Secured({"ROLE_1", "ROLE_2"})
 *    - Requires ROLE_ prefix for roles
 *    - Spring-only, not portable
 *    - Multiple values = OR logic (any role grants access)
 * 
 * 2. @RolesAllowed (JSR-250 standard):
 *    - Requires: @EnableGlobalMethodSecurity(jsr250Enabled = true)
 *    - Syntax: @RolesAllowed("ADMIN") or @RolesAllowed({"ADMIN", "MANAGER"})
 *    - No ROLE_ prefix needed
 *    - Java EE standard, portable
 *    - Multiple values = OR logic (any role grants access)
 * 
 * 3. Other JSR-250 Annotations:
 *    - @PermitAll: Allow all access (authenticated + anonymous)
 *    - @DenyAll: Deny all access
 *    - @DeclareRoles: Declare role names (not commonly used)
 * 
 * 4. Limitations (vs @PreAuthorize/@PostAuthorize):
 *    - No SpEL support
 *    - No complex expressions
 *    - No parameter checking
 *    - No return value checking
 *    - Only role/authority checking
 *    - Cannot combine conditions
 * 
 * 5. Use Cases:
 *    - Simple role-based security
 *    - Legacy application compatibility
 *    - JSR-250 compliance required
 *    - Microservice simple auth
 *    - When SpEL not needed
 * 
 * 6. Best Practices:
 *    - Use @RolesAllowed for portability
 *    - Use @Secured if Spring-specific OK
 *    - Document role requirements
 *    - Use @PreAuthorize for complex scenarios
 *    - Prefer method-level over class-level
 *    - Test authorization thoroughly
 * 
 * 7. Configuration:
 *    - Enable securedEnabled for @Secured
 *    - Enable jsr250Enabled for @RolesAllowed
 *    - Can enable both simultaneously
 *    - Can combine with prePostEnabled
 * 
 * 8. Migration Path:
 *    - @Secured/@RolesAllowed → @PreAuthorize for complex needs
 *    - Use hasRole() in @PreAuthorize for consistency
 *    - Consider expression-based security for new code
 * 
 * 9. Common Patterns:
 *    - Single role: @Secured("ROLE_ADMIN")
 *    - Multiple roles: @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
 *    - JSR-250: @RolesAllowed("ADMIN")
 *    - Public: @PermitAll
 *    - Deny: @DenyAll
 */
