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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Security Context Holder Pattern
 * 
 * Demonstrates programmatic access to security information using SecurityContextHolder:
 * - Accessing current user information
 * - Checking roles and authorities programmatically
 * - Security context manipulation
 * - Thread-local security storage
 * - Custom security context strategies
 * 
 * Key Features:
 * - Get current Authentication
 * - Access user principal
 * - Check authorities programmatically
 * - Security context propagation
 * - Custom context strategies
 * 
 * Use Cases:
 * - Audit logging with user info
 * - Custom authorization logic
 * - User-specific business logic
 * - Multi-threaded security propagation
 * - Dynamic security decisions
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class SecurityContextHolderPattern {

    public static void main(String[] args) {
        SpringApplication.run(SecurityContextHolderPattern.class, args);
    }

    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder().encode("admin123"))
                    .roles("ADMIN", "USER")
                    .authorities("ROLE_ADMIN", "ROLE_USER", "USER_WRITE", "USER_DELETE")
                    .build();

            UserDetails manager = User.builder()
                    .username("manager")
                    .password(passwordEncoder().encode("manager123"))
                    .roles("MANAGER", "USER")
                    .authorities("ROLE_MANAGER", "ROLE_USER", "USER_WRITE")
                    .build();

            UserDetails user = User.builder()
                    .username("user1")
                    .password(passwordEncoder().encode("user123"))
                    .roles("USER")
                    .authorities("ROLE_USER", "USER_READ")
                    .build();

            return new InMemoryUserDetailsManager(admin, manager, user);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    /**
     * Basic SecurityContextHolder usage
     */
    @Service
    public static class UserContextService {

        /**
         * Get current username
         */
        public String getCurrentUsername() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                return "anonymous";
            }
            
            return authentication.getName();
        }

        /**
         * Get current user's full details
         */
        public UserInfo getCurrentUserInfo() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return new UserInfo("anonymous", false, new ArrayList<>());
            }
            
            String username = authentication.getName();
            boolean isAuthenticated = authentication.isAuthenticated();
            List<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            
            System.out.println("Current user: " + username);
            System.out.println("Authenticated: " + isAuthenticated);
            System.out.println("Authorities: " + authorities);
            
            return new UserInfo(username, isAuthenticated, authorities);
        }

        /**
         * Get principal object
         */
        public Object getCurrentPrincipal() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null ? authentication.getPrincipal() : null;
        }

        /**
         * Get user details if principal is UserDetails
         */
        public UserDetails getCurrentUserDetails() {
            Object principal = getCurrentPrincipal();
            
            if (principal instanceof UserDetails) {
                return (UserDetails) principal;
            }
            
            return null;
        }
    }

    /**
     * Programmatic role and authority checking
     */
    @Service
    public static class AuthorizationService {

        /**
         * Check if current user has specific role
         */
        public boolean hasRole(String role) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }
            
            String roleToCheck = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(roleToCheck));
        }

        /**
         * Check if current user has any of the specified roles
         */
        public boolean hasAnyRole(String... roles) {
            for (String role : roles) {
                if (hasRole(role)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check if current user has all specified roles
         */
        public boolean hasAllRoles(String... roles) {
            for (String role : roles) {
                if (!hasRole(role)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Check if current user has specific authority
         */
        public boolean hasAuthority(String authority) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                return false;
            }
            
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(authority));
        }

        /**
         * Get all authorities of current user
         */
        public List<String> getCurrentAuthorities() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                return new ArrayList<>();
            }
            
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }

        /**
         * Check if user is authenticated
         */
        public boolean isAuthenticated() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && authentication.isAuthenticated();
        }

        /**
         * Check if user is anonymous
         */
        public boolean isAnonymous() {
            return !isAuthenticated();
        }
    }

    /**
     * Audit logging with security context
     */
    @Service
    public static class AuditService {

        /**
         * Log action with current user information
         */
        public void logAction(String action, String resource) {
            String username = getCurrentUsername();
            LocalDateTime timestamp = LocalDateTime.now();
            
            AuditLog log = new AuditLog();
            log.setUsername(username);
            log.setAction(action);
            log.setResource(resource);
            log.setTimestamp(timestamp);
            log.setAuthorities(getCurrentAuthorities());
            
            System.out.println("AUDIT: " + log);
            
            // In real app: save to database
        }

        /**
         * Log security event with full context
         */
        public void logSecurityEvent(String eventType, String details) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            SecurityEvent event = new SecurityEvent();
            event.setEventType(eventType);
            event.setDetails(details);
            event.setTimestamp(LocalDateTime.now());
            
            if (auth != null) {
                event.setUsername(auth.getName());
                event.setAuthenticated(auth.isAuthenticated());
                event.setAuthorities(auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
            }
            
            System.out.println("SECURITY EVENT: " + event);
        }

        private String getCurrentUsername() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "anonymous";
        }

        private List<String> getCurrentAuthorities() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return new ArrayList<>();
            }
            
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Business logic using security context
     */
    @Service
    public static class DocumentService {

        private final AuditService auditService;
        private final AuthorizationService authorizationService;

        public DocumentService(AuditService auditService, AuthorizationService authorizationService) {
            this.auditService = auditService;
            this.authorizationService = authorizationService;
        }

        /**
         * Create document with owner from security context
         */
        public Document createDocument(String title, String content) {
            String owner = SecurityContextHolder.getContext().getAuthentication().getName();
            
            Document doc = new Document();
            doc.setId(1L);
            doc.setTitle(title);
            doc.setContent(content);
            doc.setOwner(owner);
            doc.setCreatedAt(LocalDateTime.now());
            
            auditService.logAction("CREATE_DOCUMENT", "Document: " + title);
            
            System.out.println("Document created by: " + owner);
            return doc;
        }

        /**
         * Check ownership programmatically
         */
        public boolean canEdit(Document document) {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // Owner can always edit
            if (document.getOwner().equals(currentUser)) {
                return true;
            }
            
            // Admin can edit anything
            if (authorizationService.hasRole("ADMIN")) {
                return true;
            }
            
            return false;
        }

        /**
         * Update with ownership check
         */
        public void updateDocument(Long documentId, String content) {
            // Simulate fetching document
            Document doc = new Document();
            doc.setId(documentId);
            doc.setOwner("user1"); // Would come from database
            
            if (!canEdit(doc)) {
                auditService.logSecurityEvent("UNAUTHORIZED_EDIT_ATTEMPT", 
                        "Document ID: " + documentId);
                throw new SecurityException("Not authorized to edit this document");
            }
            
            doc.setContent(content);
            doc.setModifiedAt(LocalDateTime.now());
            
            auditService.logAction("UPDATE_DOCUMENT", "Document ID: " + documentId);
            System.out.println("Document updated");
        }

        /**
         * Conditional logic based on user role
         */
        public List<Document> getDocuments() {
            List<Document> documents = new ArrayList<>();
            
            if (authorizationService.hasRole("ADMIN")) {
                // Admin sees all documents
                System.out.println("Fetching all documents for admin");
                documents = getAllDocuments();
            } else {
                // Regular user sees only their documents
                String currentUser = SecurityContextHolder.getContext()
                        .getAuthentication().getName();
                System.out.println("Fetching documents for user: " + currentUser);
                documents = getUserDocuments(currentUser);
            }
            
            auditService.logAction("VIEW_DOCUMENTS", "Count: " + documents.size());
            return documents;
        }

        private List<Document> getAllDocuments() {
            // Simulate database fetch
            return new ArrayList<>();
        }

        private List<Document> getUserDocuments(String username) {
            // Simulate database fetch
            return new ArrayList<>();
        }
    }

    /**
     * REST controller using SecurityContextHolder
     */
    @RestController
    @RequestMapping("/api/context")
    public static class SecurityContextController {

        private final UserContextService userContextService;
        private final AuthorizationService authorizationService;

        public SecurityContextController(UserContextService userContextService,
                                        AuthorizationService authorizationService) {
            this.userContextService = userContextService;
            this.authorizationService = authorizationService;
        }

        /**
         * Get current user information
         */
        @GetMapping("/me")
        public UserInfo getCurrentUser() {
            return userContextService.getCurrentUserInfo();
        }

        /**
         * Check if current user has role
         */
        @GetMapping("/has-role/{role}")
        public boolean hasRole(@PathVariable String role) {
            return authorizationService.hasRole(role);
        }

        /**
         * Get current user's authorities
         */
        @GetMapping("/authorities")
        public List<String> getAuthorities() {
            return authorizationService.getCurrentAuthorities();
        }

        /**
         * Check authentication status
         */
        @GetMapping("/authenticated")
        public boolean isAuthenticated() {
            return authorizationService.isAuthenticated();
        }
    }

    // Domain Classes

    public static class UserInfo {
        private String username;
        private boolean authenticated;
        private List<String> authorities;

        public UserInfo(String username, boolean authenticated, List<String> authorities) {
            this.username = username;
            this.authenticated = authenticated;
            this.authorities = authorities;
        }

        public String getUsername() { return username; }
        public boolean isAuthenticated() { return authenticated; }
        public List<String> getAuthorities() { return authorities; }
    }

    public static class Document {
        private Long id;
        private String title;
        private String content;
        private String owner;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getModifiedAt() { return modifiedAt; }
        public void setModifiedAt(LocalDateTime modifiedAt) { this.modifiedAt = modifiedAt; }
    }

    public static class AuditLog {
        private String username;
        private String action;
        private String resource;
        private LocalDateTime timestamp;
        private List<String> authorities;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public List<String> getAuthorities() { return authorities; }
        public void setAuthorities(List<String> authorities) { this.authorities = authorities; }

        @Override
        public String toString() {
            return "AuditLog{user='" + username + "', action='" + action + 
                   "', resource='" + resource + "', time=" + timestamp + "}";
        }
    }

    public static class SecurityEvent {
        private String eventType;
        private String details;
        private String username;
        private boolean authenticated;
        private List<String> authorities;
        private LocalDateTime timestamp;

        // Getters and setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public boolean isAuthenticated() { return authenticated; }
        public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

        public List<String> getAuthorities() { return authorities; }
        public void setAuthorities(List<String> authorities) { this.authorities = authorities; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        @Override
        public String toString() {
            return "SecurityEvent{type='" + eventType + "', user='" + username + 
                   "', authenticated=" + authenticated + ", time=" + timestamp + "}";
        }
    }
}

/**
 * DOCUMENTATION
 * 
 * SecurityContextHolder:
 * 
 * 1. Core Methods:
 *    - getContext(): Get current SecurityContext
 *    - setContext(context): Set SecurityContext
 *    - clearContext(): Clear current context
 *    - createEmptyContext(): Create new empty context
 * 
 * 2. Authentication Access:
 *    - SecurityContextHolder.getContext().getAuthentication()
 *    - authentication.getName(): Get username
 *    - authentication.getPrincipal(): Get principal object
 *    - authentication.getAuthorities(): Get authorities
 *    - authentication.isAuthenticated(): Check if authenticated
 * 
 * 3. Context Strategies:
 *    - MODE_THREADLOCAL: ThreadLocal storage (default)
 *    - MODE_INHERITABLETHREADLOCAL: Inherit to child threads
 *    - MODE_GLOBAL: Global (JVM-wide) storage
 *    - Set via: SecurityContextHolder.setStrategyName()
 * 
 * 4. Common Use Cases:
 *    - Get current username for audit logging
 *    - Check roles/authorities programmatically
 *    - Set document owner automatically
 *    - Filter data based on user
 *    - Custom authorization logic
 * 
 * 5. Thread Safety:
 *    - ThreadLocal by default (thread-safe)
 *    - Not inherited by new threads unless using INHERITABLETHREADLOCAL
 *    - Must propagate manually for async/executor threads
 *    - Use DelegatingSecurityContextRunnable for async
 * 
 * 6. Best Practices:
 *    - Always check for null Authentication
 *    - Use service layer for reusable context access
 *    - Clear context in finally blocks if setting manually
 *    - Don't store in instance variables
 *    - Use Spring Security annotations when possible
 * 
 * 7. Security Considerations:
 *    - Context is per-thread by default
 *    - Don't expose full context to clients
 *    - Be careful with asynchronous operations
 *    - Clear context after use if set manually
 *    - Validate authentication state
 * 
 * 8. Integration Patterns:
 *    - Audit logging: Capture user in audit trails
 *    - Ownership: Auto-set owner fields
 *    - Filtering: Filter by user access
 *    - Conditional logic: Different paths based on role
 *    - Testing: Set mock authentication in tests
 * 
 * 9. Alternative Approaches:
 *    - Inject Authentication as method parameter
 *    - Use @AuthenticationPrincipal annotation
 *    - Use SpEL expressions in @PreAuthorize
 *    - SecurityContext available in web layer automatically
 * 
 * 10. Testing:
 *     - Use @WithMockUser for tests
 *     - Set SecurityContext manually in unit tests
 *     - SecurityContextHolder.clearContext() in @After
 *     - Use SecurityMockMvcRequestPostProcessors
 */
