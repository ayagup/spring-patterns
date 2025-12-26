package com.example.methodsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Permission-Based Access Control (PBAC) Pattern
 * 
 * Demonstrates fine-grained permission-based security with:
 * - Granular permissions (READ, WRITE, DELETE, etc.)
 * - Permission composition
 * - Resource-level permissions
 * - Dynamic permission checking
 * - Permission inheritance
 * 
 * Key Features:
 * - Specific action permissions
 * - Resource-type permissions
 * - Permission hierarchies
 * - Dynamic permission evaluation
 * - Custom permission providers
 * 
 * Use Cases:
 * - Document management (read, edit, delete, share)
 * - API access control (per-endpoint permissions)
 * - Feature flags
 * - Multi-tenant applications
 * - Complex authorization scenarios
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class PermissionBasedAccessControlPattern {

    public static void main(String[] args) {
        SpringApplication.run(PermissionBasedAccessControlPattern.class, args);
    }

    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService() {
            // User with full permissions
            UserDetails fullAccess = User.builder()
                    .username("fullaccess")
                    .password(passwordEncoder().encode("full123"))
                    .authorities(
                        "USER_READ", "USER_WRITE", "USER_DELETE",
                        "DOCUMENT_READ", "DOCUMENT_WRITE", "DOCUMENT_DELETE", "DOCUMENT_SHARE",
                        "REPORT_READ", "REPORT_WRITE", "REPORT_EXPORT",
                        "SYSTEM_CONFIG"
                    )
                    .build();

            // User with read-only permissions
            UserDetails readonly = User.builder()
                    .username("readonly")
                    .password(passwordEncoder().encode("read123"))
                    .authorities("USER_READ", "DOCUMENT_READ", "REPORT_READ")
                    .build();

            // User with editor permissions
            UserDetails editor = User.builder()
                    .username("editor")
                    .password(passwordEncoder().encode("edit123"))
                    .authorities("DOCUMENT_READ", "DOCUMENT_WRITE", "REPORT_READ", "REPORT_WRITE")
                    .build();

            // User with custom permission set
            UserDetails custom = User.builder()
                    .username("custom")
                    .password(passwordEncoder().encode("custom123"))
                    .authorities("USER_READ", "DOCUMENT_READ", "DOCUMENT_WRITE", "REPORT_EXPORT")
                    .build();

            return new InMemoryUserDetailsManager(fullAccess, readonly, editor, custom);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    /**
     * User management with permission-based access
     */
    @Service
    public static class UserService {

        /**
         * Requires specific READ permission
         */
        @PreAuthorize("hasAuthority('USER_READ')")
        public List<UserDto> listUsers() {
            System.out.println("Listing users (USER_READ permission)");
            List<UserDto> users = new ArrayList<>();
            users.add(new UserDto(1L, "john_doe", "john@example.com"));
            users.add(new UserDto(2L, "jane_smith", "jane@example.com"));
            return users;
        }

        /**
         * Requires WRITE permission
         */
        @PreAuthorize("hasAuthority('USER_WRITE')")
        public UserDto createUser(String username, String email) {
            System.out.println("Creating user: " + username + " (USER_WRITE permission)");
            return new UserDto(1L, username, email);
        }

        /**
         * Requires both READ and WRITE permissions
         */
        @PreAuthorize("hasAuthority('USER_READ') and hasAuthority('USER_WRITE')")
        public UserDto updateUser(Long userId, String email) {
            System.out.println("Updating user: " + userId + " (USER_READ and USER_WRITE permissions)");
            return new UserDto(userId, "updated_user", email);
        }

        /**
         * Requires DELETE permission
         */
        @PreAuthorize("hasAuthority('USER_DELETE')")
        public void deleteUser(Long userId) {
            System.out.println("Deleting user: " + userId + " (USER_DELETE permission)");
        }

        /**
         * Multiple permissions - any one grants access
         */
        @PreAuthorize("hasAnyAuthority('USER_READ', 'ADMIN')")
        public UserDto getUserDetails(Long userId) {
            System.out.println("Getting user details: " + userId);
            return new UserDto(userId, "some_user", "user@example.com");
        }
    }

    /**
     * Document management with granular permissions
     */
    @Service
    public static class DocumentService {

        /**
         * Read permission check
         */
        @PreAuthorize("hasAuthority('DOCUMENT_READ')")
        public DocumentDto getDocument(Long documentId) {
            System.out.println("Reading document: " + documentId);
            return new DocumentDto(documentId, "Sample Document", "owner");
        }

        /**
         * Write permission for creating documents
         */
        @PreAuthorize("hasAuthority('DOCUMENT_WRITE')")
        public DocumentDto createDocument(String title, String content) {
            System.out.println("Creating document: " + title);
            return new DocumentDto(1L, title, getCurrentUser());
        }

        /**
         * Edit requires both READ and WRITE
         */
        @PreAuthorize("hasAuthority('DOCUMENT_READ') and hasAuthority('DOCUMENT_WRITE')")
        public DocumentDto updateDocument(Long documentId, String content) {
            System.out.println("Updating document: " + documentId);
            return new DocumentDto(documentId, "Updated Document", getCurrentUser());
        }

        /**
         * Delete permission
         */
        @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
        public void deleteDocument(Long documentId) {
            System.out.println("Deleting document: " + documentId);
        }

        /**
         * Share permission - special action
         */
        @PreAuthorize("hasAuthority('DOCUMENT_SHARE')")
        public void shareDocument(Long documentId, String withUser) {
            System.out.println("Sharing document " + documentId + " with " + withUser);
        }

        /**
         * Custom permission check via service
         */
        @PreAuthorize("@permissionService.canAccessDocument(#documentId)")
        public DocumentDto getDocumentSecure(Long documentId) {
            System.out.println("Accessing document with custom permission check: " + documentId);
            return new DocumentDto(documentId, "Secure Document", "owner");
        }

        private String getCurrentUser() {
            return "current_user";
        }
    }

    /**
     * Report service with export permissions
     */
    @Service
    public static class ReportService {

        /**
         * View report - READ permission
         */
        @PreAuthorize("hasAuthority('REPORT_READ')")
        public ReportDto viewReport(Long reportId) {
            System.out.println("Viewing report: " + reportId);
            return new ReportDto(reportId, "Monthly Report", "SALES");
        }

        /**
         * Generate report - WRITE permission
         */
        @PreAuthorize("hasAuthority('REPORT_WRITE')")
        public ReportDto generateReport(String reportType, Map<String, Object> params) {
            System.out.println("Generating " + reportType + " report");
            return new ReportDto(1L, reportType + " Report", "CUSTOM");
        }

        /**
         * Export report - special EXPORT permission
         */
        @PreAuthorize("hasAuthority('REPORT_EXPORT')")
        public byte[] exportReport(Long reportId, String format) {
            System.out.println("Exporting report " + reportId + " as " + format);
            return new byte[0];
        }

        /**
         * Schedule report - requires both READ and WRITE
         */
        @PreAuthorize("hasAuthority('REPORT_READ') and hasAuthority('REPORT_WRITE')")
        public void scheduleReport(Long reportId, String schedule) {
            System.out.println("Scheduling report " + reportId + ": " + schedule);
        }

        /**
         * Delete report - separate permission
         */
        @PreAuthorize("hasAuthority('REPORT_DELETE')")
        public void deleteReport(Long reportId) {
            System.out.println("Deleting report: " + reportId);
        }
    }

    /**
     * System configuration with admin permissions
     */
    @Service
    public static class ConfigService {

        /**
         * View config - READ permission
         */
        @PreAuthorize("hasAuthority('CONFIG_READ') or hasAuthority('SYSTEM_CONFIG')")
        public Map<String, String> getConfig() {
            System.out.println("Reading system configuration");
            Map<String, String> config = new HashMap<>();
            config.put("app.name", "Spring App");
            config.put("app.version", "1.0.0");
            return config;
        }

        /**
         * Update config - special SYSTEM_CONFIG permission
         */
        @PreAuthorize("hasAuthority('SYSTEM_CONFIG')")
        public void updateConfig(String key, String value) {
            System.out.println("Updating config: " + key + " = " + value);
        }

        /**
         * Backup config - admin permission
         */
        @PreAuthorize("hasAuthority('SYSTEM_CONFIG')")
        public void backupConfig() {
            System.out.println("Backing up system configuration");
        }

        /**
         * Restore config - highest level permission
         */
        @PreAuthorize("hasAuthority('SYSTEM_CONFIG')")
        public void restoreConfig(String backupId) {
            System.out.println("Restoring configuration from backup: " + backupId);
        }
    }

    /**
     * Custom permission evaluation service
     */
    @Service("permissionService")
    public static class PermissionService {

        /**
         * Check if user can access specific document
         */
        public boolean canAccessDocument(Long documentId) {
            // In real application: check database, cache, or ACL
            System.out.println("Checking document access for ID: " + documentId);
            return true; // Simplified
        }

        /**
         * Check if user has permission on resource
         */
        public boolean hasPermission(Authentication auth, String resource, String action) {
            String permission = resource.toUpperCase() + "_" + action.toUpperCase();
            boolean hasPermission = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(permission));
            
            System.out.println("Checking permission: " + permission + " = " + hasPermission);
            return hasPermission;
        }

        /**
         * Check multiple permissions
         */
        public boolean hasAllPermissions(Authentication auth, String... permissions) {
            return Arrays.stream(permissions)
                    .allMatch(perm -> auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals(perm)));
        }

        /**
         * Check if user has any of the permissions
         */
        public boolean hasAnyPermission(Authentication auth, String... permissions) {
            return Arrays.stream(permissions)
                    .anyMatch(perm -> auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals(perm)));
        }
    }

    /**
     * REST API with permission-based endpoints
     */
    @RestController
    @RequestMapping("/api/permissions")
    public static class PermissionController {

        private final DocumentService documentService;
        private final ReportService reportService;

        public PermissionController(DocumentService documentService, ReportService reportService) {
            this.documentService = documentService;
            this.reportService = reportService;
        }

        /**
         * Read document endpoint
         */
        @GetMapping("/documents/{id}")
        @PreAuthorize("hasAuthority('DOCUMENT_READ')")
        public DocumentDto getDocument(@PathVariable Long id) {
            return documentService.getDocument(id);
        }

        /**
         * Create document endpoint
         */
        @PostMapping("/documents")
        @PreAuthorize("hasAuthority('DOCUMENT_WRITE')")
        public DocumentDto createDocument(@RequestBody DocumentRequest request) {
            return documentService.createDocument(request.getTitle(), request.getContent());
        }

        /**
         * Share document endpoint
         */
        @PostMapping("/documents/{id}/share")
        @PreAuthorize("hasAuthority('DOCUMENT_SHARE')")
        public String shareDocument(@PathVariable Long id, @RequestParam String withUser) {
            documentService.shareDocument(id, withUser);
            return "Document shared successfully";
        }

        /**
         * Export report endpoint
         */
        @GetMapping("/reports/{id}/export")
        @PreAuthorize("hasAuthority('REPORT_EXPORT')")
        public byte[] exportReport(@PathVariable Long id, @RequestParam String format) {
            return reportService.exportReport(id, format);
        }

        /**
         * Multiple permission check
         */
        @PostMapping("/reports/{id}/schedule")
        @PreAuthorize("hasAuthority('REPORT_READ') and hasAuthority('REPORT_WRITE')")
        public String scheduleReport(@PathVariable Long id, @RequestBody ScheduleRequest request) {
            reportService.scheduleReport(id, request.getSchedule());
            return "Report scheduled";
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
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }

    public static class DocumentDto {
        private Long id;
        private String title;
        private String owner;

        public DocumentDto(Long id, String title, String owner) {
            this.id = id;
            this.title = title;
            this.owner = owner;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getOwner() { return owner; }
    }

    public static class ReportDto {
        private Long id;
        private String name;
        private String type;

        public ReportDto(Long id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
    }

    public static class DocumentRequest {
        private String title;
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class ScheduleRequest {
        private String schedule;

        public String getSchedule() { return schedule; }
        public void setSchedule(String schedule) { this.schedule = schedule; }
    }
}

/**
 * DOCUMENTATION
 * 
 * Permission-Based Access Control (PBAC):
 * 
 * 1. Permission Naming Convention:
 *    - Format: RESOURCE_ACTION
 *    - Examples: USER_READ, DOCUMENT_WRITE, REPORT_EXPORT
 *    - Consistent uppercase naming
 *    - Specific action verbs
 * 
 * 2. Common Permissions:
 *    - READ: View/retrieve data
 *    - WRITE: Create/update data
 *    - DELETE: Remove data
 *    - SHARE: Share with others
 *    - EXPORT: Export data
 *    - ADMIN: Full control
 * 
 * 3. Permission Granularity:
 *    - Coarse: DOCUMENT (all operations)
 *    - Fine: DOCUMENT_READ, DOCUMENT_WRITE, DOCUMENT_DELETE
 *    - Very Fine: DOCUMENT_READ_OWN, DOCUMENT_READ_ALL
 * 
 * 4. Permission Composition:
 *    - Single: hasAuthority('USER_READ')
 *    - AND: hasAuthority('A') and hasAuthority('B')
 *    - OR: hasAnyAuthority('A', 'B')
 *    - NOT: !hasAuthority('A')
 * 
 * 5. vs Role-Based Access Control:
 *    - RBAC: Coarse-grained (roles)
 *    - PBAC: Fine-grained (specific actions)
 *    - Can be combined: Roles contain permissions
 *    - PBAC more flexible, RBAC simpler
 * 
 * 6. Implementation Strategies:
 *    - Direct authorities: User has permission strings
 *    - Role-Permission mapping: Roles grant permissions
 *    - ACL: Per-resource permissions
 *    - Hybrid: Combine multiple approaches
 * 
 * 7. Best Practices:
 *    - Use consistent naming
 *    - Document all permissions
 *    - Group related permissions
 *    - Avoid permission explosion
 *    - Use permission hierarchies
 *    - Test thoroughly
 * 
 * 8. Storage:
 *    - Database: permissions table
 *    - Cache: for performance
 *    - External: permission service
 *    - Dynamic: computed at runtime
 * 
 * 9. Common Patterns:
 *    - CRUD: CREATE, READ, UPDATE, DELETE
 *    - Data access: READ_OWN, READ_ALL
 *    - Actions: APPROVE, REJECT, PUBLISH
 *    - Export: EXPORT_PDF, EXPORT_CSV
 *    - Admin: ADMIN, SUPER_ADMIN
 * 
 * 10. Advanced Features:
 *     - Temporal permissions (time-based)
 *     - Contextual permissions (location, IP)
 *     - Delegated permissions (temporary)
 *     - Permission inheritance
 *     - Permission scopes (tenant, org, global)
 */
