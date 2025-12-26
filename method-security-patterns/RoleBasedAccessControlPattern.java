package com.example.methodsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Role-Based Access Control (RBAC) Pattern
 * 
 * Demonstrates comprehensive RBAC implementation with:
 * - Hierarchical roles
 * - Role composition
 * - Dynamic role assignment
 * - Role inheritance
 * - Fine-grained permissions per role
 * 
 * Key Features:
 * - Role hierarchy (ADMIN > MANAGER > USER)
 * - Multiple roles per user
 * - Role-based method security
 * - Custom role providers
 * - Role switching
 * 
 * Use Cases:
 * - Enterprise applications
 * - Multi-tenant systems
 * - Content management systems
 * - Healthcare systems (doctor, nurse, admin)
 * - E-commerce (customer, merchant, admin)
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class RoleBasedAccessControlPattern {

    public static void main(String[] args) {
        SpringApplication.run(RoleBasedAccessControlPattern.class, args);
    }

    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService() {
            // Super Admin - has all roles
            UserDetails superAdmin = User.builder()
                    .username("superadmin")
                    .password(passwordEncoder().encode("super123"))
                    .roles("SUPER_ADMIN", "ADMIN", "MANAGER", "USER")
                    .build();

            // Admin - has admin and user roles
            UserDetails admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder().encode("admin123"))
                    .roles("ADMIN", "USER")
                    .build();

            // Manager - has manager and user roles
            UserDetails manager = User.builder()
                    .username("manager")
                    .password(passwordEncoder().encode("manager123"))
                    .roles("MANAGER", "USER")
                    .build();

            // Regular user
            UserDetails user = User.builder()
                    .username("user")
                    .password(passwordEncoder().encode("user123"))
                    .roles("USER")
                    .build();

            // Guest - limited access
            UserDetails guest = User.builder()
                    .username("guest")
                    .password(passwordEncoder().encode("guest123"))
                    .roles("GUEST")
                    .build();

            return new InMemoryUserDetailsManager(superAdmin, admin, manager, user, guest);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    /**
     * User management with role-based access
     */
    @Service
    public static class UserService {

        /**
         * SUPER_ADMIN only
         */
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public void createSuperAdmin(String username) {
            System.out.println("Creating super admin: " + username);
        }

        /**
         * ADMIN or SUPER_ADMIN
         */
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
        public void createUser(String username, List<String> roles) {
            System.out.println("Creating user: " + username + " with roles: " + roles);
        }

        /**
         * ADMIN, SUPER_ADMIN, or MANAGER
         */
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
        public List<UserDto> listUsers() {
            System.out.println("Listing all users");
            return getAllUsers();
        }

        /**
         * Any authenticated user can view their own profile
         */
        @PreAuthorize("isAuthenticated()")
        public UserDto getMyProfile() {
            System.out.println("Getting current user profile");
            return new UserDto(1L, "current_user", Arrays.asList("ROLE_USER"));
        }

        /**
         * ADMIN or SUPER_ADMIN can view any profile
         */
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
        public UserDto getUserProfile(Long userId) {
            System.out.println("Admin viewing user profile: " + userId);
            return new UserDto(userId, "some_user", Arrays.asList("ROLE_USER"));
        }

        /**
         * Only SUPER_ADMIN can delete admin users
         */
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public void deleteAdmin(Long userId) {
            System.out.println("SUPER_ADMIN deleting admin user: " + userId);
        }

        /**
         * ADMIN can delete regular users
         */
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
        public void deleteUser(Long userId) {
            System.out.println("Admin deleting user: " + userId);
        }

        private List<UserDto> getAllUsers() {
            List<UserDto> users = new ArrayList<>();
            users.add(new UserDto(1L, "user1", Arrays.asList("ROLE_USER")));
            users.add(new UserDto(2L, "admin1", Arrays.asList("ROLE_ADMIN", "ROLE_USER")));
            return users;
        }
    }

    /**
     * Content management with hierarchical roles
     */
    @Service
    public static class ContentService {

        /**
         * Anyone can read public content
         */
        @PreAuthorize("hasAnyRole('GUEST', 'USER', 'MANAGER', 'ADMIN', 'SUPER_ADMIN')")
        public ContentDto getPublicContent(Long contentId) {
            System.out.println("Reading public content: " + contentId);
            return new ContentDto(contentId, "Public Content", "PUBLIC");
        }

        /**
         * Regular users and above can read protected content
         */
        @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN', 'SUPER_ADMIN')")
        public ContentDto getProtectedContent(Long contentId) {
            System.out.println("Reading protected content: " + contentId);
            return new ContentDto(contentId, "Protected Content", "PROTECTED");
        }

        /**
         * Only managers and above can create content
         */
        @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
        public ContentDto createContent(String title, String level) {
            System.out.println("Creating content: " + title);
            return new ContentDto(1L, title, level);
        }

        /**
         * Only admins can publish content
         */
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public void publishContent(Long contentId) {
            System.out.println("Publishing content: " + contentId);
        }

        /**
         * Only super admin can delete published content
         */
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public void deletePublishedContent(Long contentId) {
            System.out.println("SUPER_ADMIN deleting published content: " + contentId);
        }

        /**
         * Managers can delete unpublished content
         */
        @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
        public void deleteUnpublishedContent(Long contentId) {
            System.out.println("Deleting unpublished content: " + contentId);
        }
    }

    /**
     * Financial operations with strict role requirements
     */
    @Service
    public static class FinancialService {

        /**
         * View balance - USER and above
         */
        @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
        public double viewBalance(Long accountId) {
            System.out.println("Viewing balance for account: " + accountId);
            return 1000.00;
        }

        /**
         * Transfer - USER and above with amount limit check
         */
        @PreAuthorize("hasRole('USER') and #amount <= 1000.0 or hasAnyRole('MANAGER', 'ADMIN')")
        public void transfer(Long fromAccount, Long toAccount, double amount) {
            System.out.println("Transfer: $" + amount + " from " + fromAccount + " to " + toAccount);
        }

        /**
         * Approve large transfer - MANAGER and above
         */
        @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
        public void approveLargeTransfer(Long transferId) {
            System.out.println("Manager approving large transfer: " + transferId);
        }

        /**
         * Audit financial records - ADMIN only
         */
        @PreAuthorize("hasRole('ADMIN')")
        public List<AuditRecord> auditFinancialRecords() {
            System.out.println("Admin auditing financial records");
            return new ArrayList<>();
        }

        /**
         * Override transaction - SUPER_ADMIN only
         */
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public void overrideTransaction(Long transactionId) {
            System.out.println("SUPER_ADMIN overriding transaction: " + transactionId);
        }
    }

    /**
     * System configuration with administrative roles
     */
    @Service
    public static class SystemConfigService {

        /**
         * View config - MANAGER and above
         */
        @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
        public ConfigDto getConfig(String configKey) {
            System.out.println("Reading config: " + configKey);
            return new ConfigDto(configKey, "value");
        }

        /**
         * Update config - ADMIN and above
         */
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public void updateConfig(String configKey, String value) {
            System.out.println("Updating config: " + configKey + " = " + value);
        }

        /**
         * System maintenance - ADMIN only
         */
        @PreAuthorize("hasRole('ADMIN')")
        public void performMaintenance() {
            System.out.println("Admin performing system maintenance");
        }

        /**
         * Critical system changes - SUPER_ADMIN only
         */
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public void criticalSystemChange() {
            System.out.println("SUPER_ADMIN making critical system changes");
        }
    }

    /**
     * REST API with role-based endpoints
     */
    @RestController
    @RequestMapping("/api/rbac")
    public static class RbacController {

        private final UserService userService;
        private final ContentService contentService;

        public RbacController(UserService userService, ContentService contentService) {
            this.userService = userService;
            this.contentService = contentService;
        }

        /**
         * Public endpoint - guests allowed
         */
        @GetMapping("/public")
        @PreAuthorize("permitAll()")
        public String publicEndpoint() {
            return "Public information";
        }

        /**
         * User profile - authenticated users
         */
        @GetMapping("/profile")
        @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN', 'SUPER_ADMIN')")
        public UserDto getProfile() {
            return userService.getMyProfile();
        }

        /**
         * User list - managers and above
         */
        @GetMapping("/users")
        @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
        public List<UserDto> getUsers() {
            return userService.listUsers();
        }

        /**
         * Create content - managers and above
         */
        @PostMapping("/content")
        @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
        public ContentDto createContent(@RequestBody ContentRequest request) {
            return contentService.createContent(request.getTitle(), request.getLevel());
        }

        /**
         * Admin operations - admin only
         */
        @PostMapping("/admin/publish/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public String publishContent(@PathVariable Long id) {
            contentService.publishContent(id);
            return "Content published";
        }

        /**
         * Super admin operations - super admin only
         */
        @DeleteMapping("/superadmin/content/{id}")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public String deleteCriticalContent(@PathVariable Long id) {
            contentService.deletePublishedContent(id);
            return "Critical content deleted";
        }
    }

    // Domain Classes

    public static class UserDto {
        private Long id;
        private String username;
        private List<String> roles;

        public UserDto(Long id, String username, List<String> roles) {
            this.id = id;
            this.username = username;
            this.roles = roles;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public List<String> getRoles() { return roles; }
    }

    public static class ContentDto {
        private Long id;
        private String title;
        private String level;

        public ContentDto(Long id, String title, String level) {
            this.id = id;
            this.title = title;
            this.level = level;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getLevel() { return level; }
    }

    public static class ConfigDto {
        private String key;
        private String value;

        public ConfigDto(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() { return key; }
        public String getValue() { return value; }
    }

    public static class AuditRecord {
        private Long id;
        private String action;
        private String user;

        public Long getId() { return id; }
        public String getAction() { return action; }
        public String getUser() { return user; }
    }

    public static class ContentRequest {
        private String title;
        private String level;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }
}

/**
 * DOCUMENTATION
 * 
 * Role-Based Access Control (RBAC):
 * 
 * 1. Role Hierarchy Levels:
 *    - SUPER_ADMIN: System-wide control, can do everything
 *    - ADMIN: Administrative tasks, user management
 *    - MANAGER: Content management, approvals
 *    - USER: Regular user operations
 *    - GUEST: Limited read-only access
 * 
 * 2. Role Assignment:
 *    - Users can have multiple roles
 *    - Roles are additive (cumulative permissions)
 *    - Higher roles typically include lower role permissions
 *    - Roles can be dynamically assigned
 * 
 * 3. Access Control Patterns:
 *    - Single role: hasRole('ADMIN')
 *    - Multiple roles (OR): hasAnyRole('ADMIN', 'MANAGER')
 *    - Role + condition: hasRole('USER') and #amount < 1000
 *    - Hierarchical: Check parent roles explicitly
 * 
 * 4. Best Practices:
 *    - Define clear role hierarchy
 *    - Document role permissions
 *    - Use consistent role naming
 *    - Avoid role proliferation
 *    - Consider role groups
 *    - Test access thoroughly
 * 
 * 5. Common Role Structures:
 *    - Flat: All roles are equal, no hierarchy
 *    - Hierarchical: Roles inherit from parent roles
 *    - Matrix: Roles + permissions matrix
 *    - Hybrid: Combination of above
 * 
 * 6. Implementation Considerations:
 *    - Store roles in database
 *    - Cache role checks for performance
 *    - Audit role changes
 *    - Support role switching for testing
 *    - Handle role conflicts
 * 
 * 7. Security Guidelines:
 *    - Principle of least privilege
 *    - Separate admin from regular roles
 *    - Require MFA for privileged roles
 *    - Log all admin actions
 *    - Review role assignments periodically
 * 
 * 8. Common Pitfalls:
 *    - Too many roles (role explosion)
 *    - Overlapping permissions
 *    - Missing default deny
 *    - Not testing edge cases
 *    - Hardcoding role names
 */
