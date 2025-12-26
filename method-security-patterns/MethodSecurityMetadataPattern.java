package com.example.methodsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Method Security Metadata Pattern
 * 
 * Demonstrates managing and inspecting method security metadata:
 * - Custom security annotations
 * - Security metadata extraction
 * - Method security configuration
 * - Runtime security introspection
 * - Security annotation composition
 * 
 * Key Features:
 * - Custom meta-annotations
 * - Security attribute sources
 * - Method security inspection
 * - Configuration attributes
 * - Annotation processing
 * 
 * Use Cases:
 * - Custom security annotations
 * - Security auditing
 * - Documentation generation
 * - Testing security configuration
 * - Dynamic security rules
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class MethodSecurityMetadataPattern {

    public static void main(String[] args) {
        SpringApplication.run(MethodSecurityMetadataPattern.class, args);
    }

    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
    public static class SecurityConfig extends GlobalMethodSecurityConfiguration {

        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder().encode("admin123"))
                    .roles("ADMIN")
                    .build();

            UserDetails user = User.builder()
                    .username("user")
                    .password(passwordEncoder().encode("user123"))
                    .roles("USER")
                    .build();

            return new InMemoryUserDetailsManager(admin, user);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        /**
         * Override to customize method security metadata source
         */
        @Override
        protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
            // Can provide custom metadata source
            return super.customMethodSecurityMetadataSource();
        }
    }

    /**
     * Custom security annotations
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasRole('ADMIN')")
    public @interface RequireAdmin {
        String reason() default "";
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasRole('USER')")
    public @interface RequireUser {
        String description() default "";
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public @interface RequireManagement {
        String operation() default "";
    }

    /**
     * Custom annotation with parameter-based security
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface SecureOperation {
        String resource();
        String action();
        String[] roles() default {};
        boolean ownershipRequired() default false;
    }

    /**
     * Audit annotation for tracking security checks
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface AuditSecure {
        String category();
        int sensitivityLevel() default 1;
    }

    /**
     * Service using custom security annotations
     */
    @Service
    public static class UserManagementService {

        /**
         * Using custom @RequireAdmin annotation
         */
        @RequireAdmin(reason = "User management requires admin privileges")
        public List<UserDto> getAllUsers() {
            System.out.println("Fetching all users");
            return Arrays.asList(
                new UserDto(1L, "user1"),
                new UserDto(2L, "user2")
            );
        }

        /**
         * Using @RequireUser annotation
         */
        @RequireUser(description = "View own profile")
        public UserDto getMyProfile() {
            System.out.println("Getting current user profile");
            return new UserDto(1L, "current_user");
        }

        /**
         * Using @RequireManagement annotation
         */
        @RequireManagement(operation = "approve_user")
        public void approveUser(Long userId) {
            System.out.println("Approving user: " + userId);
        }

        /**
         * Multiple security annotations
         */
        @RequireAdmin(reason = "Critical operation")
        @AuditSecure(category = "USER_DELETION", sensitivityLevel = 5)
        public void deleteUser(Long userId) {
            System.out.println("Deleting user: " + userId);
        }
    }

    /**
     * Service using SecureOperation annotation
     */
    @Service
    public static class DocumentManagementService {

        @SecureOperation(resource = "DOCUMENT", action = "READ", roles = {"USER", "ADMIN"})
        public DocumentDto readDocument(Long documentId) {
            System.out.println("Reading document: " + documentId);
            return new DocumentDto(documentId, "Sample Document");
        }

        @SecureOperation(resource = "DOCUMENT", action = "WRITE", roles = {"ADMIN"})
        public DocumentDto createDocument(String title) {
            System.out.println("Creating document: " + title);
            return new DocumentDto(1L, title);
        }

        @SecureOperation(resource = "DOCUMENT", action = "DELETE", roles = {"ADMIN"}, ownershipRequired = true)
        @AuditSecure(category = "DOCUMENT_DELETION", sensitivityLevel = 3)
        public void deleteDocument(Long documentId) {
            System.out.println("Deleting document: " + documentId);
        }
    }

    /**
     * Service for inspecting method security metadata
     */
    @Service
    public static class SecurityMetadataInspector {

        /**
         * Extract security annotations from a method
         */
        public SecurityMetadata inspectMethod(Method method) {
            SecurityMetadata metadata = new SecurityMetadata();
            metadata.setMethodName(method.getName());

            // Check for @PreAuthorize
            PreAuthorize preAuth = method.getAnnotation(PreAuthorize.class);
            if (preAuth != null) {
                metadata.setPreAuthorizeExpression(preAuth.value());
            }

            // Check for custom @RequireAdmin
            RequireAdmin reqAdmin = method.getAnnotation(RequireAdmin.class);
            if (reqAdmin != null) {
                metadata.setRequiresAdmin(true);
                metadata.setAdminReason(reqAdmin.reason());
            }

            // Check for @SecureOperation
            SecureOperation secOp = method.getAnnotation(SecureOperation.class);
            if (secOp != null) {
                metadata.setResource(secOp.resource());
                metadata.setAction(secOp.action());
                metadata.setRequiredRoles(Arrays.asList(secOp.roles()));
                metadata.setOwnershipRequired(secOp.ownershipRequired());
            }

            // Check for @AuditSecure
            AuditSecure audit = method.getAnnotation(AuditSecure.class);
            if (audit != null) {
                metadata.setAuditCategory(audit.category());
                metadata.setSensitivityLevel(audit.sensitivityLevel());
            }

            return metadata;
        }

        /**
         * Get all secured methods from a class
         */
        public List<SecurityMetadata> inspectClass(Class<?> clazz) {
            List<SecurityMetadata> metadataList = new ArrayList<>();

            for (Method method : clazz.getDeclaredMethods()) {
                if (hasSecurityAnnotation(method)) {
                    metadataList.add(inspectMethod(method));
                }
            }

            return metadataList;
        }

        /**
         * Check if method has any security annotation
         */
        private boolean hasSecurityAnnotation(Method method) {
            return method.isAnnotationPresent(PreAuthorize.class) ||
                   method.isAnnotationPresent(RequireAdmin.class) ||
                   method.isAnnotationPresent(RequireUser.class) ||
                   method.isAnnotationPresent(RequireManagement.class) ||
                   method.isAnnotationPresent(SecureOperation.class);
        }

        /**
         * Generate security documentation
         */
        public String generateSecurityDocumentation(Class<?> clazz) {
            StringBuilder doc = new StringBuilder();
            doc.append("Security Documentation for ").append(clazz.getSimpleName()).append("\n");
            doc.append("=".repeat(50)).append("\n\n");

            List<SecurityMetadata> metadataList = inspectClass(clazz);
            for (SecurityMetadata metadata : metadataList) {
                doc.append("Method: ").append(metadata.getMethodName()).append("\n");
                
                if (metadata.getPreAuthorizeExpression() != null) {
                    doc.append("  PreAuthorize: ").append(metadata.getPreAuthorizeExpression()).append("\n");
                }
                
                if (metadata.isRequiresAdmin()) {
                    doc.append("  Requires: ADMIN\n");
                    doc.append("  Reason: ").append(metadata.getAdminReason()).append("\n");
                }
                
                if (metadata.getResource() != null) {
                    doc.append("  Resource: ").append(metadata.getResource()).append("\n");
                    doc.append("  Action: ").append(metadata.getAction()).append("\n");
                    doc.append("  Roles: ").append(metadata.getRequiredRoles()).append("\n");
                }
                
                if (metadata.getAuditCategory() != null) {
                    doc.append("  Audit Category: ").append(metadata.getAuditCategory()).append("\n");
                    doc.append("  Sensitivity: ").append(metadata.getSensitivityLevel()).append("\n");
                }
                
                doc.append("\n");
            }

            return doc.toString();
        }
    }

    /**
     * Utility for programmatic security checks
     */
    @Service
    public static class SecurityAnalyzer {

        private final SecurityMetadataInspector inspector;

        public SecurityAnalyzer(SecurityMetadataInspector inspector) {
            this.inspector = inspector;
        }

        /**
         * Analyze security coverage of a service
         */
        public SecurityCoverageReport analyzeSecurityCoverage(Class<?> serviceClass) {
            SecurityCoverageReport report = new SecurityCoverageReport();
            report.setClassName(serviceClass.getSimpleName());

            Method[] methods = serviceClass.getDeclaredMethods();
            int totalMethods = methods.length;
            int securedMethods = 0;

            List<String> unsecuredMethods = new ArrayList<>();

            for (Method method : methods) {
                if (hasAnySecurityAnnotation(method)) {
                    securedMethods++;
                } else {
                    unsecuredMethods.add(method.getName());
                }
            }

            report.setTotalMethods(totalMethods);
            report.setSecuredMethods(securedMethods);
            report.setUnsecuredMethods(unsecuredMethods);
            report.setCoveragePercentage((securedMethods * 100.0) / totalMethods);

            return report;
        }

        private boolean hasAnySecurityAnnotation(Method method) {
            return method.isAnnotationPresent(PreAuthorize.class) ||
                   method.isAnnotationPresent(RequireAdmin.class) ||
                   method.isAnnotationPresent(RequireUser.class) ||
                   method.isAnnotationPresent(RequireManagement.class) ||
                   method.isAnnotationPresent(SecureOperation.class) ||
                   method.isAnnotationPresent(org.springframework.security.access.annotation.Secured.class);
        }
    }

    // Domain Classes

    public static class UserDto {
        private Long id;
        private String username;

        public UserDto(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
    }

    public static class DocumentDto {
        private Long id;
        private String title;

        public DocumentDto(Long id, String title) {
            this.id = id;
            this.title = title;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
    }

    public static class SecurityMetadata {
        private String methodName;
        private String preAuthorizeExpression;
        private boolean requiresAdmin;
        private String adminReason;
        private String resource;
        private String action;
        private List<String> requiredRoles;
        private boolean ownershipRequired;
        private String auditCategory;
        private int sensitivityLevel;

        // Getters and setters
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }

        public String getPreAuthorizeExpression() { return preAuthorizeExpression; }
        public void setPreAuthorizeExpression(String preAuthorizeExpression) { 
            this.preAuthorizeExpression = preAuthorizeExpression; 
        }

        public boolean isRequiresAdmin() { return requiresAdmin; }
        public void setRequiresAdmin(boolean requiresAdmin) { this.requiresAdmin = requiresAdmin; }

        public String getAdminReason() { return adminReason; }
        public void setAdminReason(String adminReason) { this.adminReason = adminReason; }

        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public List<String> getRequiredRoles() { return requiredRoles; }
        public void setRequiredRoles(List<String> requiredRoles) { this.requiredRoles = requiredRoles; }

        public boolean isOwnershipRequired() { return ownershipRequired; }
        public void setOwnershipRequired(boolean ownershipRequired) { 
            this.ownershipRequired = ownershipRequired; 
        }

        public String getAuditCategory() { return auditCategory; }
        public void setAuditCategory(String auditCategory) { this.auditCategory = auditCategory; }

        public int getSensitivityLevel() { return sensitivityLevel; }
        public void setSensitivityLevel(int sensitivityLevel) { this.sensitivityLevel = sensitivityLevel; }
    }

    public static class SecurityCoverageReport {
        private String className;
        private int totalMethods;
        private int securedMethods;
        private List<String> unsecuredMethods;
        private double coveragePercentage;

        // Getters and setters
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public int getTotalMethods() { return totalMethods; }
        public void setTotalMethods(int totalMethods) { this.totalMethods = totalMethods; }

        public int getSecuredMethods() { return securedMethods; }
        public void setSecuredMethods(int securedMethods) { this.securedMethods = securedMethods; }

        public List<String> getUnsecuredMethods() { return unsecuredMethods; }
        public void setUnsecuredMethods(List<String> unsecuredMethods) { 
            this.unsecuredMethods = unsecuredMethods; 
        }

        public double getCoveragePercentage() { return coveragePercentage; }
        public void setCoveragePercentage(double coveragePercentage) { 
            this.coveragePercentage = coveragePercentage; 
        }

        @Override
        public String toString() {
            return "SecurityCoverageReport{" +
                    "className='" + className + '\'' +
                    ", totalMethods=" + totalMethods +
                    ", securedMethods=" + securedMethods +
                    ", coveragePercentage=" + String.format("%.2f%%", coveragePercentage) +
                    ", unsecuredMethods=" + unsecuredMethods +
                    '}';
        }
    }
}

/**
 * DOCUMENTATION
 * 
 * Method Security Metadata:
 * 
 * 1. Custom Security Annotations:
 *    - Create meta-annotations with @PreAuthorize
 *    - Compose multiple security constraints
 *    - Add custom attributes for documentation
 *    - Enable domain-specific security vocabulary
 * 
 * 2. Meta-Annotation Pattern:
 *    - @RequireAdmin = @PreAuthorize("hasRole('ADMIN')")
 *    - @RequireManagement = @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
 *    - Simplifies code and improves readability
 * 
 * 3. Security Metadata Inspection:
 *    - Use Java Reflection to inspect annotations
 *    - Extract security requirements
 *    - Generate documentation
 *    - Validate security coverage
 * 
 * 4. Use Cases:
 *    - Security auditing
 *    - Documentation generation
 *    - Testing security configuration
 *    - Compliance reporting
 *    - Dynamic security analysis
 * 
 * 5. Custom Metadata Attributes:
 *    - reason: Why security is needed
 *    - resource: What resource is protected
 *    - action: What action is performed
 *    - sensitivityLevel: Data sensitivity
 *    - auditCategory: For logging/monitoring
 * 
 * 6. Security Coverage Analysis:
 *    - Count secured vs unsecured methods
 *    - Identify missing security annotations
 *    - Calculate coverage percentage
 *    - Generate compliance reports
 * 
 * 7. Best Practices:
 *    - Use custom annotations for common patterns
 *    - Document security requirements
 *    - Validate all public methods are secured
 *    - Generate security documentation automatically
 *    - Include in CI/CD pipeline
 * 
 * 8. Testing:
 *    - Unit test custom annotations
 *    - Verify metadata extraction
 *    - Test security coverage analysis
 *    - Validate annotation composition
 */
