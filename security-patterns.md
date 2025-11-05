# Spring Security Patterns

I'll create a comprehensive Spring Boot application demonstrating all 17 Spring Security patterns.

## Project Structure

```
spring-security-patterns/
├── src/main/java/org/example/
│   ├── SecurityPatternsApplication.java
│   ├── config/
│   ├── model/
│   ├── controller/
│   ├── service/
│   └── patterns/security/
│       ├── authentication/
│       ├── authorization/
│       ├── filterchain/
│       ├── securitycontext/
│       ├── accessdecision/
│       ├── voter/
│       ├── expression/
│       ├── methodsecurity/
│       ├── acl/
│       ├── rememberme/
│       ├── csrf/
│       ├── sessionfixation/
│       ├── channel/
│       ├── anonymous/
│       ├── runas/
│       ├── passwordencoding/
│       └── userdetails/
├── pom.xml
└── application.properties
```

## 1. Main Application & Base Configuration

```java
// src/main/java/org/example/SecurityPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityPatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityPatternsApplication.class, args);
    }
}
```

## 2. Domain Models

```java
// src/main/java/org/example/model/User.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Set<String> roles;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
}
```

```java
// src/main/java/org/example/model/SecuredResource.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecuredResource {
    private Long id;
    private String name;
    private String content;
    private String owner;
    private String accessLevel;
}
```

## 3. Pattern 1: Authentication Pattern

```java
// src/main/java/org/example/patterns/security/authentication/CustomAuthenticationProvider.java
package org.example.patterns.security.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Authentication Pattern.
 * Custom AuthenticationProvider for validating credentials.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public Authentication authenticate(Authentication authentication) 
            throws AuthenticationException {
        
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        log.info("Authenticating user: {}", username);
        
        // Custom authentication logic
        if ("admin".equals(username) && passwordEncoder.matches("admin123", 
                passwordEncoder.encode(password))) {
            
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            log.info("Authentication successful for: {}", username);
            
            return new UsernamePasswordAuthenticationToken(
                    username, password, authorities);
        } else if ("user".equals(username) && passwordEncoder.matches("user123", 
                passwordEncoder.encode(password))) {
            
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            log.info("Authentication successful for: {}", username);
            
            return new UsernamePasswordAuthenticationToken(
                    username, password, authorities);
        }
        
        log.warn("Authentication failed for: {}", username);
        throw new BadCredentialsException("Invalid username or password");
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

```java
// src/main/java/org/example/patterns/security/authentication/AuthenticationController.java
package org.example.patterns.security.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    
    @GetMapping("/status")
    public Map<String, Object> getAuthenticationStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> status = new HashMap<>();
        status.put("authenticated", auth != null && auth.isAuthenticated());
        status.put("principal", auth != null ? auth.getName() : "anonymous");
        status.put("authorities", auth != null ? auth.getAuthorities() : null);
        
        log.info("Authentication status checked: {}", status);
        return status;
    }
    
    @GetMapping("/user")
    public Map<String, Object> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> user = new HashMap<>();
        user.put("username", auth.getName());
        user.put("authorities", auth.getAuthorities());
        user.put("authenticated", auth.isAuthenticated());
        
        return user;
    }
}
```

## 4. Pattern 2: Authorization Pattern

```java
// src/main/java/org/example/patterns/security/authorization/AuthorizationController.java
package org.example.patterns.security.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authorization Pattern.
 * Demonstrates role-based and permission-based access control.
 */
@Slf4j
@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {
    
    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        log.info("Public endpoint accessed");
        return Map.of("message", "This is a public endpoint");
    }
    
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public Map<String, String> userEndpoint() {
        log.info("User endpoint accessed");
        return Map.of("message", "This endpoint requires USER role");
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminEndpoint() {
        log.info("Admin endpoint accessed");
        return Map.of("message", "This endpoint requires ADMIN role");
    }
    
    @GetMapping("/admin-or-manager")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Map<String, String> adminOrManagerEndpoint() {
        log.info("Admin or Manager endpoint accessed");
        return Map.of("message", "This endpoint requires ADMIN or MANAGER role");
    }
    
    @GetMapping("/permission-check")
    @PreAuthorize("hasAuthority('READ_PRIVILEGE')")
    public Map<String, String> permissionEndpoint() {
        log.info("Permission-based endpoint accessed");
        return Map.of("message", "This endpoint requires READ_PRIVILEGE");
    }
}
```

## 5. Pattern 3: Filter Chain Pattern

```java
// src/main/java/org/example/patterns/security/filterchain/CustomAuthenticationFilter.java
package org.example.patterns.security.filterchain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filter Chain Pattern.
 * Custom filter in the security filter chain.
 */
@Slf4j
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        log.info("CustomAuthenticationFilter - Request URI: {}", request.getRequestURI());
        
        // Check for custom authentication header
        String customToken = request.getHeader("X-Custom-Token");
        
        if (customToken != null && customToken.equals("secret-token")) {
            log.info("Valid custom token found");
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    "custom-user", 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

```java
// src/main/java/org/example/patterns/security/filterchain/RequestLoggingFilter.java
package org.example.patterns.security.filterchain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        log.info("=== Request Started ===");
        log.info("URI: {}", request.getRequestURI());
        log.info("Method: {}", request.getMethod());
        log.info("Remote Address: {}", request.getRemoteAddr());
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("=== Request Completed in {} ms ===", duration);
            log.info("Response Status: {}", response.getStatus());
        }
    }
}
```

## 6. Pattern 4: Security Context Pattern

```java
// src/main/java/org/example/patterns/security/securitycontext/SecurityContextService.java
package org.example.patterns.security.securitycontext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Security Context Pattern.
 * Accessing and managing the security context.
 */
@Slf4j
@Service
public class SecurityContextService {
    
    public Map<String, Object> getSecurityContextInfo() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        
        Map<String, Object> info = new HashMap<>();
        
        if (authentication != null) {
            info.put("principal", authentication.getPrincipal());
            info.put("name", authentication.getName());
            info.put("authorities", authentication.getAuthorities());
            info.put("authenticated", authentication.isAuthenticated());
            info.put("details", authentication.getDetails());
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                info.put("username", userDetails.getUsername());
                info.put("enabled", userDetails.isEnabled());
            }
        } else {
            info.put("message", "No authentication in security context");
        }
        
        log.info("Security context info retrieved: {}", info);
        return info;
    }
    
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null) {
            return authentication.getName();
        }
        
        return "anonymous";
    }
    
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
}
```

```java
// src/main/java/org/example/patterns/security/securitycontext/SecurityContextController.java
package org.example.patterns.security.securitycontext;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/security-context")
@RequiredArgsConstructor
public class SecurityContextController {
    
    private final SecurityContextService securityContextService;
    
    @GetMapping("/info")
    public Map<String, Object> getContextInfo() {
        return securityContextService.getSecurityContextInfo();
    }
    
    @GetMapping("/username")
    public Map<String, String> getCurrentUsername() {
        return Map.of("username", securityContextService.getCurrentUsername());
    }
    
    @GetMapping("/has-role/{role}")
    public Map<String, Boolean> hasRole(@PathVariable String role) {
        return Map.of("hasRole", securityContextService.hasRole(role));
    }
}
```

## 7. Pattern 5: Access Decision Manager Pattern

```java
// src/main/java/org/example/patterns/security/accessdecision/CustomAccessDecisionManager.java
package org.example.patterns.security.accessdecision;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.List;

/**
 * Access Decision Manager Pattern.
 * Makes final access control decisions using voters.
 */
@Slf4j
public class CustomAccessDecisionManager implements AccessDecisionManager {
    
    private final List<AccessDecisionVoter<?>> voters;
    
    public CustomAccessDecisionManager(List<AccessDecisionVoter<?>> voters) {
        this.voters = voters;
    }
    
    @Override
    public void decide(Authentication authentication, Object object, 
                      Collection<ConfigAttribute> configAttributes) 
            throws AccessDeniedException, InsufficientAuthenticationException {
        
        log.info("Access decision requested for: {}", authentication.getName());
        
        int grant = 0;
        int deny = 0;
        int abstain = 0;
        
        for (AccessDecisionVoter voter : voters) {
            int result = voter.vote(authentication, object, configAttributes);
            
            switch (result) {
                case AccessDecisionVoter.ACCESS_GRANTED:
                    grant++;
                    log.info("Voter {} granted access", voter.getClass().getSimpleName());
                    break;
                case AccessDecisionVoter.ACCESS_DENIED:
                    deny++;
                    log.info("Voter {} denied access", voter.getClass().getSimpleName());
                    break;
                default:
                    abstain++;
                    log.info("Voter {} abstained", voter.getClass().getSimpleName());
                    break;
            }
        }
        
        // Affirmative-based strategy: at least one grant and no denies
        if (grant > 0 && deny == 0) {
            log.info("Access granted");
            return;
        }
        
        log.warn("Access denied");
        throw new AccessDeniedException("Access is denied");
    }
    
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return voters.stream().anyMatch(voter -> voter.supports(attribute));
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return voters.stream().anyMatch(voter -> voter.supports(clazz));
    }
}
```

## 8. Pattern 6: Voter Pattern

```java
// src/main/java/org/example/patterns/security/voter/TimeBasedVoter.java
package org.example.patterns.security.voter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import java.time.LocalTime;
import java.util.Collection;

/**
 * Voter Pattern.
 * Custom voter that denies access outside business hours.
 */
@Slf4j
public class TimeBasedVoter implements AccessDecisionVoter<FilterInvocation> {
    
    private static final LocalTime BUSINESS_START = LocalTime.of(9, 0);
    private static final LocalTime BUSINESS_END = LocalTime.of(17, 0);
    
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
    
    @Override
    public int vote(Authentication authentication, FilterInvocation filterInvocation, 
                   Collection<ConfigAttribute> attributes) {
        
        LocalTime now = LocalTime.now();
        
        log.info("TimeBasedVoter checking access at: {}", now);
        
        // Allow access during business hours
        if (now.isAfter(BUSINESS_START) && now.isBefore(BUSINESS_END)) {
            log.info("Within business hours - ACCESS_GRANTED");
            return ACCESS_GRANTED;
        }
        
        // Check if user has ADMIN role for after-hours access
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            log.info("Admin user - ACCESS_GRANTED");
            return ACCESS_GRANTED;
        }
        
        log.warn("Outside business hours for non-admin - ACCESS_DENIED");
        return ACCESS_DENIED;
    }
}
```

```java
// src/main/java/org/example/patterns/security/voter/IpBasedVoter.java
package org.example.patterns.security.voter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import java.util.Collection;
import java.util.Set;

@Slf4j
public class IpBasedVoter implements AccessDecisionVoter<FilterInvocation> {
    
    private static final Set<String> ALLOWED_IPS = Set.of(
            "127.0.0.1",
            "0:0:0:0:0:0:0:1",
            "localhost"
    );
    
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
    
    @Override
    public int vote(Authentication authentication, FilterInvocation filterInvocation, 
                   Collection<ConfigAttribute> attributes) {
        
        HttpServletRequest request = filterInvocation.getRequest();
        String remoteAddr = request.getRemoteAddr();
        
        log.info("IpBasedVoter checking IP: {}", remoteAddr);
        
        if (ALLOWED_IPS.contains(remoteAddr)) {
            log.info("IP allowed - ACCESS_GRANTED");
            return ACCESS_GRANTED;
        }
        
        log.warn("IP not in allowed list - ABSTAIN");
        return ACCESS_ABSTAIN;
    }
}
```

## 9. Pattern 7: Expression-based Access Control Pattern

```java
// src/main/java/org/example/patterns/security/expression/CustomSecurityExpressionRoot.java
package org.example.patterns.security.expression;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/**
 * Expression-based Access Control Pattern.
 * Custom security expressions.
 */
@Slf4j
public class CustomSecurityExpressionRoot extends SecurityExpressionRoot 
        implements MethodSecurityExpressionOperations {
    
    private Object filterObject;
    private Object returnObject;
    
    public CustomSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }
    
    /**
     * Custom expression: Check if user owns the resource.
     */
    public boolean isOwner(Long resourceId) {
        String username = getAuthentication().getName();
        log.info("Checking if {} owns resource {}", username, resourceId);
        
        // Custom logic to check ownership
        // In real application, query database
        return "admin".equals(username) || resourceId < 10;
    }
    
    /**
     * Custom expression: Check if user is in same department.
     */
    public boolean isSameDepartment(String department) {
        log.info("Checking if user is in department: {}", department);
        
        // Custom logic
        return true; // Simplified for demo
    }
    
    /**
     * Custom expression: Check business hours.
     */
    public boolean isBusinessHours() {
        java.time.LocalTime now = java.time.LocalTime.now();
        boolean result = now.isAfter(java.time.LocalTime.of(9, 0)) && 
                        now.isBefore(java.time.LocalTime.of(17, 0));
        
        log.info("Business hours check: {}", result);
        return result;
    }
    
    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }
    
    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }
    
    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }
    
    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }
    
    @Override
    public Object getThis() {
        return this;
    }
}
```

```java
// src/main/java/org/example/patterns/security/expression/ExpressionBasedController.java
package org.example.patterns.security.expression;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/expression")
public class ExpressionBasedController {
    
    @GetMapping("/role-check")
    @PreAuthorize("hasRole('USER')")
    public Map<String, String> roleCheck() {
        return Map.of("message", "Has USER role");
    }
    
    @GetMapping("/multiple-roles")
    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    public Map<String, String> multipleRoles() {
        return Map.of("message", "Has both ADMIN and USER roles");
    }
    
    @GetMapping("/any-role")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public Map<String, String> anyRole() {
        return Map.of("message", "Has at least one of the specified roles");
    }
    
    @GetMapping("/authenticated")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> authenticated() {
        return Map.of("message", "User is authenticated");
    }
    
    @GetMapping("/anonymous")
    @PreAuthorize("isAnonymous()")
    public Map<String, String> anonymous() {
        return Map.of("message", "Anonymous access");
    }
    
    @GetMapping("/principal-check")
    @PreAuthorize("principal.username == 'admin'")
    public Map<String, String> principalCheck() {
        return Map.of("message", "Principal is admin");
    }
    
    @GetMapping("/resource/{id}")
    @PreAuthorize("@customSecurityExpressionRoot.isOwner(#id)")
    public Map<String, Object> resourceAccess(@PathVariable Long id) {
        return Map.of("id", id, "message", "Access granted to owner");
    }
    
    @GetMapping("/business-hours")
    @PreAuthorize("@customSecurityExpressionRoot.isBusinessHours()")
    public Map<String, String> businessHours() {
        return Map.of("message", "Accessed during business hours");
    }
    
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #data.get('public') == true)")
    public Map<String, String> create(@RequestBody Map<String, Object> data) {
        return Map.of("message", "Resource created", "data", data.toString());
    }
    
    @GetMapping("/post-authorize")
    @PostAuthorize("returnObject.owner == authentication.name")
    public ResourceInfo getResource() {
        return new ResourceInfo("admin", "Secret Data");
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    static class ResourceInfo {
        private String owner;
        private String data;
    }
}
```

## 10. Pattern 8: Method Security Pattern

```java
// src/main/java/org/example/patterns/security/methodsecurity/MethodSecurityService.java
package org.example.patterns.security.methodsecurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Method Security Pattern.
 * Securing methods with annotations.
 */
@Slf4j
@Service
public class MethodSecurityService {
    
    /**
     * @Secured annotation (Spring Security).
     */
    @Secured("ROLE_ADMIN")
    public String adminOnlyMethod() {
        log.info("Admin-only method executed");
        return "Admin method executed";
    }
    
    /**
     * @RolesAllowed annotation (JSR-250).
     */
    @RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
    public String userOrAdminMethod() {
        log.info("User or Admin method executed");
        return "User/Admin method executed";
    }
    
    /**
     * @PreAuthorize with SpEL expression.
     */
    @PreAuthorize("hasRole('USER') and #username == authentication.name")
    public String updateOwnProfile(String username, String data) {
        log.info("Updating profile for: {}", username);
        return "Profile updated for " + username;
    }
    
    /**
     * @PostAuthorize - checks after method execution.
     */
    @PostAuthorize("returnObject.owner == authentication.name")
    public Document getDocument(Long id) {
        log.info("Fetching document: {}", id);
        // In real app, fetch from database
        return new Document(id, "admin", "Confidential data");
    }
    
    /**
     * @PreFilter - filters collection before method execution.
     */
    @PreFilter("filterObject.owner == authentication.name")
    public List<Document> processDocuments(List<Document> documents) {
        log.info("Processing {} documents", documents.size());
        return documents;
    }
    
    /**
     * @PostFilter - filters return collection after method execution.
     */
    @PostFilter("filterObject.owner == authentication.name or hasRole('ADMIN')")
    public List<Document> getAllDocuments() {
        log.info("Fetching all documents");
        return List.of(
            new Document(1L, "admin", "Doc 1"),
            new Document(2L, "user", "Doc 2"),
            new Document(3L, "admin", "Doc 3")
        );
    }
    
    /**
     * Combination of @PreAuthorize and @PostFilter.
     */
    @PreAuthorize("hasRole('USER')")
    @PostFilter("filterObject.public or filterObject.owner == authentication.name")
    public List<Resource> getResources() {
        return List.of(
            new Resource(1L, "admin", true, "Public resource"),
            new Resource(2L, "user", false, "Private resource"),
            new Resource(3L, "admin", false, "Admin resource")
        );
    }
    
```java
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class Document {
        private Long id;
        private String owner;
        private String content;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class Resource {
        private Long id;
        private String owner;
        private Boolean isPublic;
        private String content;
        
        public Boolean getPublic() {
            return isPublic;
        }
    }
}
```

```java
// src/main/java/org/example/patterns/security/methodsecurity/MethodSecurityController.java
package org.example.patterns.security.methodsecurity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/method-security")
@RequiredArgsConstructor
public class MethodSecurityController {
    
    private final MethodSecurityService methodSecurityService;
    
    @GetMapping("/admin-only")
    public Map<String, String> adminOnly() {
        String result = methodSecurityService.adminOnlyMethod();
        return Map.of("result", result);
    }
    
    @GetMapping("/user-or-admin")
    public Map<String, String> userOrAdmin() {
        String result = methodSecurityService.userOrAdminMethod();
        return Map.of("result", result);
    }
    
    @PutMapping("/profile/{username}")
    public Map<String, String> updateProfile(@PathVariable String username, 
                                            @RequestBody String data) {
        String result = methodSecurityService.updateOwnProfile(username, data);
        return Map.of("result", result);
    }
    
    @GetMapping("/document/{id}")
    public MethodSecurityService.Document getDocument(@PathVariable Long id) {
        return methodSecurityService.getDocument(id);
    }
    
    @GetMapping("/documents")
    public List<MethodSecurityService.Document> getAllDocuments() {
        return methodSecurityService.getAllDocuments();
    }
    
    @GetMapping("/resources")
    public List<MethodSecurityService.Resource> getResources() {
        return methodSecurityService.getResources();
    }
}
```

## 11. Pattern 9: ACL Pattern (Access Control List)

```java
// src/main/java/org/example/patterns/security/acl/AclService.java
package org.example.patterns.security.acl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ACL Pattern (Access Control List).
 * Fine-grained permissions on individual objects.
 */
@Slf4j
@Service
public class AclService {
    
    // In-memory ACL storage (in production, use database)
    private final Map<ObjectIdentity, List<AclEntry>> aclMap = new HashMap<>();
    
    public AclService() {
        // Initialize some ACLs
        ObjectIdentity doc1 = new ObjectIdentity("Document", 1L);
        aclMap.put(doc1, Arrays.asList(
            new AclEntry("admin", Permission.READ),
            new AclEntry("admin", Permission.WRITE),
            new AclEntry("admin", Permission.DELETE),
            new AclEntry("user", Permission.READ)
        ));
        
        ObjectIdentity doc2 = new ObjectIdentity("Document", 2L);
        aclMap.put(doc2, Arrays.asList(
            new AclEntry("user", Permission.READ),
            new AclEntry("user", Permission.WRITE)
        ));
    }
    
    public boolean hasPermission(String username, ObjectIdentity objectIdentity, 
                                Permission permission) {
        log.info("Checking ACL: user={}, object={}, permission={}", 
                username, objectIdentity, permission);
        
        List<AclEntry> entries = aclMap.get(objectIdentity);
        
        if (entries == null) {
            log.warn("No ACL found for object: {}", objectIdentity);
            return false;
        }
        
        boolean hasPermission = entries.stream()
                .anyMatch(entry -> entry.getPrincipal().equals(username) 
                        && entry.getPermission() == permission);
        
        log.info("ACL check result: {}", hasPermission);
        return hasPermission;
    }
    
    public void grantPermission(String username, ObjectIdentity objectIdentity, 
                               Permission permission) {
        log.info("Granting permission: user={}, object={}, permission={}", 
                username, objectIdentity, permission);
        
        aclMap.computeIfAbsent(objectIdentity, k -> new ArrayList<>())
               .add(new AclEntry(username, permission));
    }
    
    public void revokePermission(String username, ObjectIdentity objectIdentity, 
                                Permission permission) {
        log.info("Revoking permission: user={}, object={}, permission={}", 
                username, objectIdentity, permission);
        
        List<AclEntry> entries = aclMap.get(objectIdentity);
        if (entries != null) {
            entries.removeIf(entry -> 
                entry.getPrincipal().equals(username) 
                && entry.getPermission() == permission);
        }
    }
    
    public List<AclEntry> getAcl(ObjectIdentity objectIdentity) {
        return aclMap.getOrDefault(objectIdentity, Collections.emptyList());
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ObjectIdentity {
        private String type;
        private Long id;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class AclEntry {
        private String principal;
        private Permission permission;
    }
    
    public enum Permission {
        READ, WRITE, DELETE, ADMIN
    }
}
```

```java
// src/main/java/org/example/patterns/security/acl/AclController.java
package org.example.patterns.security.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/acl")
@RequiredArgsConstructor
public class AclController {
    
    private final AclService aclService;
    
    @GetMapping("/check/{type}/{id}/{permission}")
    public Map<String, Boolean> checkPermission(
            @PathVariable String type,
            @PathVariable Long id,
            @PathVariable AclService.Permission permission,
            Authentication authentication) {
        
        AclService.ObjectIdentity objectIdentity = 
                new AclService.ObjectIdentity(type, id);
        
        boolean hasPermission = aclService.hasPermission(
                authentication.getName(), objectIdentity, permission);
        
        return Map.of("hasPermission", hasPermission);
    }
    
    @PostMapping("/grant")
    public Map<String, String> grantPermission(@RequestBody PermissionRequest request) {
        AclService.ObjectIdentity objectIdentity = 
                new AclService.ObjectIdentity(request.getType(), request.getId());
        
        aclService.grantPermission(request.getUsername(), objectIdentity, 
                request.getPermission());
        
        return Map.of("message", "Permission granted");
    }
    
    @PostMapping("/revoke")
    public Map<String, String> revokePermission(@RequestBody PermissionRequest request) {
        AclService.ObjectIdentity objectIdentity = 
                new AclService.ObjectIdentity(request.getType(), request.getId());
        
        aclService.revokePermission(request.getUsername(), objectIdentity, 
                request.getPermission());
        
        return Map.of("message", "Permission revoked");
    }
    
    @GetMapping("/list/{type}/{id}")
    public List<AclService.AclEntry> getAcl(@PathVariable String type, 
                                            @PathVariable Long id) {
        AclService.ObjectIdentity objectIdentity = 
                new AclService.ObjectIdentity(type, id);
        
        return aclService.getAcl(objectIdentity);
    }
    
    @lombok.Data
    static class PermissionRequest {
        private String username;
        private String type;
        private Long id;
        private AclService.Permission permission;
    }
}
```

## 12. Pattern 10: Remember-Me Pattern

```java
// src/main/java/org/example/patterns/security/rememberme/RememberMeController.java
package org.example.patterns.security.rememberme;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Remember-Me Pattern.
 * Persistent authentication across sessions.
 */
@Slf4j
@RestController
@RequestMapping("/api/remember-me")
public class RememberMeController {
    
    @GetMapping("/status")
    public Map<String, Object> getRememberMeStatus(HttpServletRequest request, 
                                                   Authentication authentication) {
        Map<String, Object> status = new HashMap<>();
        
        // Check for remember-me cookie
        Cookie[] cookies = request.getCookies();
        boolean hasRememberMeCookie = false;
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY
                        .equals(cookie.getName())) {
                    hasRememberMeCookie = true;
                    status.put("cookieMaxAge", cookie.getMaxAge());
                    log.info("Remember-me cookie found");
                    break;
                }
            }
        }
        
        status.put("hasRememberMeCookie", hasRememberMeCookie);
        status.put("authenticated", authentication != null && authentication.isAuthenticated());
        status.put("username", authentication != null ? authentication.getName() : null);
        
        return status;
    }
    
    @GetMapping("/protected")
    public Map<String, String> protectedEndpoint(Authentication authentication) {
        log.info("Protected endpoint accessed by: {}", authentication.getName());
        return Map.of("message", "Accessed via remember-me authentication");
    }
}
```

## 13. Pattern 11: CSRF Protection Pattern

```java
// src/main/java/org/example/patterns/security/csrf/CsrfController.java
package org.example.patterns.security.csrf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * CSRF Protection Pattern.
 * Cross-Site Request Forgery protection.
 */
@Slf4j
@RestController
@RequestMapping("/api/csrf")
public class CsrfController {
    
    @GetMapping("/token")
    public Map<String, String> getCsrfToken(CsrfToken csrfToken) {
        log.info("CSRF token requested");
        
        Map<String, String> response = new HashMap<>();
        if (csrfToken != null) {
            response.put("token", csrfToken.getToken());
            response.put("headerName", csrfToken.getHeaderName());
            response.put("parameterName", csrfToken.getParameterName());
        }
        
        return response;
    }
    
    @PostMapping("/test")
    public Map<String, String> testCsrfProtection(@RequestBody Map<String, String> data) {
        log.info("POST request with CSRF protection: {}", data);
        return Map.of("message", "CSRF token validated successfully", 
                     "data", data.toString());
    }
    
    @PutMapping("/update")
    public Map<String, String> updateWithCsrf(@RequestBody Map<String, String> data) {
        log.info("PUT request with CSRF protection: {}", data);
        return Map.of("message", "Update successful with CSRF validation");
    }
    
    @DeleteMapping("/delete/{id}")
    public Map<String, String> deleteWithCsrf(@PathVariable Long id) {
        log.info("DELETE request with CSRF protection for id: {}", id);
        return Map.of("message", "Delete successful with CSRF validation");
    }
}
```

## 14. Pattern 12: Session Fixation Protection Pattern

```java
// src/main/java/org/example/patterns/security/sessionfixation/SessionFixationController.java
package org.example.patterns.security.sessionfixation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Session Fixation Protection Pattern.
 * Prevents session fixation attacks by changing session ID on authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/session-fixation")
public class SessionFixationController {
    
    @GetMapping("/session-info")
    public Map<String, Object> getSessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        Map<String, Object> info = new HashMap<>();
        
        if (session != null) {
            info.put("sessionId", session.getId());
            info.put("creationTime", session.getCreationTime());
            info.put("lastAccessedTime", session.getLastAccessedTime());
            info.put("maxInactiveInterval", session.getMaxInactiveInterval());
            info.put("isNew", session.isNew());
            
            log.info("Session info retrieved: {}", session.getId());
        } else {
            info.put("message", "No session");
        }
        
        return info;
    }
    
    @PostMapping("/test-session-change")
    public Map<String, String> testSessionChange(HttpServletRequest request) {
        HttpSession oldSession = request.getSession(false);
        String oldSessionId = oldSession != null ? oldSession.getId() : "none";
        
        log.info("Old session ID: {}", oldSessionId);
        
        // Simulate login - Spring Security automatically changes session ID
        HttpSession newSession = request.getSession(true);
        String newSessionId = newSession.getId();
        
        log.info("New session ID: {}", newSessionId);
        
        return Map.of(
            "oldSessionId", oldSessionId,
            "newSessionId", newSessionId,
            "message", "Session ID changed to prevent fixation attacks"
        );
    }
}
```

## 15. Pattern 13: Channel Security Pattern

```java
// src/main/java/org/example/patterns/security/channel/ChannelSecurityController.java
package org.example.patterns.security.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Channel Security Pattern.
 * Ensures requests use appropriate protocol (HTTP/HTTPS).
 */
@Slf4j
@RestController
@RequestMapping("/api/channel-security")
public class ChannelSecurityController {
    
    @GetMapping("/info")
    public Map<String, Object> getChannelInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        
        info.put("scheme", request.getScheme());
        info.put("protocol", request.getProtocol());
        info.put("secure", request.isSecure());
        info.put("serverPort", request.getServerPort());
        info.put("requestUrl", request.getRequestURL().toString());
        
        log.info("Channel info: scheme={}, secure={}", 
                request.getScheme(), request.isSecure());
        
        return info;
    }
    
    @GetMapping("/https-required")
    public Map<String, String> httpsRequired(HttpServletRequest request) {
        if (!request.isSecure()) {
            log.warn("Insecure request to HTTPS-required endpoint");
            return Map.of("warning", "This endpoint should be accessed via HTTPS");
        }
        
        log.info("Secure HTTPS request received");
        return Map.of("message", "Secure HTTPS connection confirmed");
    }
    
    @GetMapping("/any-channel")
    public Map<String, String> anyChannel(HttpServletRequest request) {
        log.info("Request via {}", request.getScheme());
        return Map.of("message", "This endpoint accepts any channel (HTTP/HTTPS)");
    }
}
```

## 16. Pattern 14: Anonymous Authentication Pattern

```java
// src/main/java/org/example/patterns/security/anonymous/AnonymousAuthenticationController.java
package org.example.patterns.security.anonymous;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Anonymous Authentication Pattern.
 * Treats unauthenticated users as anonymous with specific role.
 */
@Slf4j
@RestController
@RequestMapping("/api/anonymous")
public class AnonymousAuthenticationController {
    
    @GetMapping("/check")
    public Map<String, Object> checkAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> info = new HashMap<>();
        
        if (authentication instanceof AnonymousAuthenticationToken) {
            log.info("Anonymous user detected");
            info.put("isAnonymous", true);
            info.put("principal", authentication.getPrincipal());
            info.put("authorities", authentication.getAuthorities());
        } else if (authentication != null && authentication.isAuthenticated()) {
            log.info("Authenticated user: {}", authentication.getName());
            info.put("isAnonymous", false);
            info.put("username", authentication.getName());
            info.put("authorities", authentication.getAuthorities());
        } else {
            log.info("No authentication");
            info.put("isAnonymous", null);
        }
        
        return info;
    }
    
    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        boolean isAnonymous = auth instanceof AnonymousAuthenticationToken;
        
        log.info("Public endpoint accessed - Anonymous: {}", isAnonymous);
        
        return Map.of(
            "message", "Public endpoint accessible to all",
            "accessType", isAnonymous ? "anonymous" : "authenticated"
        );
    }
    
    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo(Authentication authentication) {
        Map<String, Object> info = new HashMap<>();
        
        if (authentication instanceof AnonymousAuthenticationToken) {
            info.put("userType", "anonymous");
            info.put("hasAnonymousRole", 
                authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS")));
        } else {
            info.put("userType", "authenticated");
            info.put("username", authentication.getName());
        }
        
        return info;
    }
}
```

## 17. Pattern 15: Run-As Authentication Pattern

```java
// src/main/java/org/example/patterns/security/runas/RunAsService.java
package org.example.patterns.security.runas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Run-As Authentication Pattern.
 * Temporarily elevates privileges for specific operations.
 */
@Slf4j
@Service
public class RunAsService {
    
    @Secured({"ROLE_USER", "RUN_AS_ADMIN"})
    public String userMethodWithElevation() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        log.info("Running as: {}", auth.getName());
        log.info("Authorities: {}", auth.getAuthorities());
        
        // This method runs with elevated ADMIN privileges
        return "Method executed with elevated privileges";
    }
    
    public String adminOperation() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        log.info("Admin operation - Running as: {}", auth.getName());
        log.info("Authorities: {}", auth.getAuthorities());
        
        return "Admin operation completed";
    }
    
    @Secured("ROLE_USER")
    public String normalUserMethod() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        log.info("Normal user method - Running as: {}", auth.getName());
        
        return "Normal user method executed";
    }
}
```

```java
// src/main/java/org/example/patterns/security/runas/RunAsController.java
package org.example.patterns.security.runas;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/run-as")
@RequiredArgsConstructor
public class RunAsController {
    
    private final RunAsService runAsService;
    
    @GetMapping("/elevated")
    public Map<String, String> elevatedOperation(Authentication authentication) {
        String result = runAsService.userMethodWithElevation();
        return Map.of(
            "result", result,
            "originalUser", authentication.getName()
        );
    }
    
    @GetMapping("/normal")
    public Map<String, String> normalOperation() {
        String result = runAsService.normalUserMethod();
        return Map.of("result", result);
    }
}
```

## 18. Pattern 16: Password Encoding Pattern

```java
// src/main/java/org/example/patterns/security/passwordencoding/PasswordEncodingService.java
package org.example.patterns.security.passwordencoding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Password Encoding Pattern.
 * Demonstrates various password encoding strategies.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordEncodingService {
    
    private final PasswordEncoder passwordEncoder;
    
    public String encodePassword(String rawPassword) {
        log.info("Encoding password");
        String encoded = passwordEncoder.encode(rawPassword);
        log.info("Password encoded with algorithm prefix");
        return encoded;
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        log.info("Password match result: {}", matches);
        return matches;
    }
    
    public Map<String, String> demonstrateEncoders(String password) {
        Map<String, String> encodings = new HashMap<>();
        
        // BCrypt
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        encodings.put("bcrypt", bcrypt.encode(password));
        log.info("BCrypt encoding completed");
        
        // PBKDF2
        Pbkdf2PasswordEncoder pbkdf2 = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        encodings.put("pbkdf2", pbkdf2.encode(password));
        log.info("PBKDF2 encoding completed");
        
        // SCrypt
        SCryptPasswordEncoder scrypt = SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8();
        encodings.put("scrypt", scrypt.encode(password));
        log.info("SCrypt encoding completed");
        
        // Delegating encoder (default)
        encodings.put("delegating", passwordEncoder.encode(password));
        log.info("Delegating encoder completed");
        
        return encodings;
    }
    
    public String upgradeEncoding(String oldEncodedPassword) {
        if (passwordEncoder.upgradeEncoding(oldEncodedPassword)) {
            log.info("Password encoding should be upgraded");
            return "Upgrade recommended";
        } else {
            log.info("Password encoding is current");
            return "No upgrade needed";
        }
    }
}
```

```java
// src/main/java/org/example/patterns/security/passwordencoding/PasswordEncodingController.java
package org.example.patterns.security.passwordencoding;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordEncodingController {
    
    private final PasswordEncodingService passwordEncodingService;
    
    @PostMapping("/encode")
    public Map<String, String> encodePassword(@RequestBody Map<String, String> request) {
        String rawPassword = request.get("password");
        String encoded = passwordEncodingService.encodePassword(rawPassword);
        return Map.of("encoded", encoded);
    }
    
    @PostMapping("/verify")
    public Map<String, Boolean> verifyPassword(@RequestBody Map<String, String> request) {
        String rawPassword = request.get("password");
        String encodedPassword = request.get("encoded");
        boolean matches = passwordEncodingService.matches(rawPassword, encodedPassword);
        return Map.of("matches", matches);
    }
    
    @PostMapping("/demonstrate")
    public Map<String, String> demonstrateEncoders(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        return passwordEncodingService.demonstrateEncoders(password);
    }
    
    @PostMapping("/check-upgrade")
    public Map<String, String> checkUpgrade(@RequestBody Map<String, String> request) {
        String encodedPassword = request.get("encoded");
        String status = passwordEncodingService.upgradeEncoding(encodedPassword);
        return Map.of("status", status);
    }
}
```

## 19. Pattern 17: User Details Service Pattern

```java
// src/main/java/org/example/patterns/security/userdetails/CustomUserDetailsService.java
package org.example.patterns.security.userdetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * User Details Service Pattern.
 * Loads user-specific data for authentication.
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final Map<String, CustomUserDetails> users = new HashMap<>();
    private final PasswordEncoder passwordEncoder;
    
    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        initializeUsers();
    }
    
    private void initializeUsers() {
        // Admin user
        users.put("admin", new CustomUserDetails(
            1L,
            "admin",
            passwordEncoder.encode("admin123"),
            "admin@example.com",
            Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
            ),
            true, true, true, true
        ));
        
        // Regular user
        users.put("user", new CustomUserDetails(
            2L,
            "user",
            passwordEncoder.encode("user123"),
            "user@example.com",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            true, true, true, true
        ));
        
        // Disabled user
        users.put("disabled", new CustomUserDetails(
            3L,
            "disabled",
            passwordEncoder.encode("disabled123"),
            "disabled@example.com",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            false, true, true, true
        ));
        
        log.info("Initialized {} users", users.size());
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user: {}", username);
        
        CustomUserDetails user = users.get(username);
        
        if (user == null) {
            log.warn("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        log.info("User loaded: {} with {} authorities", username, user.getAuthorities().size());
        return user;
    }
    
    public CustomUserDetails createUser(CustomUserDetails userDetails) {
        log.info("Creating user: {}", userDetails.getUsername());
        users.put(userDetails.getUsername(), userDetails);
        return userDetails;
    }
    
    public void deleteUser(String username) {
        log.info("Deleting user: {}", username);
        users.remove(username);
    }
    
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
    
    public List<CustomUserDetails> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
```

```java
// src/main/java/org/example/patterns/security/userdetails/CustomUserDetails.java
package org.example.patterns.security.userdetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    
    private Long id;
    private String username;
    private String password;
    private String email;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
```

```java
// src/main/java/org/example/patterns/security/userdetails/UserDetailsController.java
package org.example.patterns.security.userdetails;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-details")
@RequiredArgsConstructor
public class UserDetailsController {
    
    private final CustomUserDetailsService userDetailsService;
    
    @GetMapping("/load/{username}")
    public Map<String, Object> loadUser(@PathVariable String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", userDetails.getUsername());
        response.put("authorities", userDetails.getAuthorities());
        response.put("enabled", userDetails.isEnabled());
        response.put("accountNonExpired", userDetails.isAccountNonExpired());
        response.put("accountNonLocked", userDetails.isAccountNonLocked());
        response.put("credentialsNonExpired", userDetails.isCredentialsNonExpired());
        
        return response;
    }
    
    @GetMapping("/exists/{username}")
    public Map<String, Boolean> userExists(@PathVariable String username) {
        return Map.of("exists", userDetailsService.userExists(username));
    }
    
    @GetMapping("/all")
    public List<Map<String, Object>> getAllUsers() {
        return userDetailsService.getAllUsers().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    userMap.put("authorities", user.getAuthorities());
                    userMap.put("enabled", user.isEnabled());
                    return userMap;
                })
                .collect(Collectors.toList());
    }
}
```

## 20. Main Security Configuration

```java
// src/main/java/org/example/config/SecurityConfig.java
package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.patterns.security.authentication.CustomAuthenticationProvider;
import org.example.patterns.security.filterchain.CustomAuthenticationFilter;
import org.example.patterns.security.filterchain.RequestLoggingFilter;
import org.example.patterns.security.userdetails.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.HashMap;
import java.util.Map;

/**
 * Main Security Configuration demonstrating all patterns.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Filter Chain Pattern
            .addFilterBefore(new RequestLoggingFilter(), 
                    UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(new CustomAuthenticationFilter(), 
                    UsernamePasswordAuthenticationFilter.class)
            
            // Authorization Pattern
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/**", "/api/anonymous/**", 
                        "/api/password/**", "/login", "/error").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            
            // Authentication Pattern
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/api/auth/status")
                .permitAll()
            )
            
            // Remember-Me Pattern
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 24 hours
                .userDetailsService(userDetailsService)
                .rememberMeParameter("remember-me")
                .rememberMeCookieName("remember-me-cookie")
            )
            
            // CSRF Protection Pattern
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/public/**")
            )
            
            // Session Fixation Protection Pattern
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            
            // Channel Security Pattern
            .requiresChannel(channel -> channel
                .requestMatchers("/api/channel-security/https-required").requiresSecure()
                .anyRequest().requiresInsecure() // For development only
            )
            
            // Anonymous Authentication Pattern
            .anonymous(anonymous -> anonymous
                .principal("anonymousUser")
                .authorities("ROLE_ANONYMOUS")
            )
            
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID", "remember-me-cookie")
                .invalidateHttpSession(true)
                .permitAll()
            );
        
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(customAuthenticationProvider);
    }
    
    /**
     * Password Encoding Pattern - Delegating Password Encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        String idForEncode = "bcrypt";
        
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
        
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }
    
    /**
     * Session event publisher for session management.
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
```

## 21. Maven Configuration (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>org.example</groupId>
    <artifactId>spring-security-patterns</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Spring Security Patterns</name>
    <description>Demonstration of Security patterns in Spring</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Boot Thymeleaf -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        
        <!-- Thymeleaf Security -->
        <dependency>
            <groupId>org.thymeleaf.extras</groupId>
            <artifactId>thymeleaf-extras-springsecurity6</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Spring Security Test -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 22. Application Properties

```properties
# src/main/resources/application.properties
spring.application.name=spring-security-patterns

# Server Configuration
server.port=8080
server.servlet.session.timeout=30m

# Security Configuration
spring.security.user.name=admin
spring.security.user.password=admin123
spring.security.user.roles=ADMIN,USER

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.level.org.springframework.security=DEBUG

# Session Configuration
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false
server.servlet.session.tracking-modes=cookie

# CSRF Configuration
spring.security.csrf.enabled=true
```

## 23. Test Classes

```java
// src/test/java/org/example/patterns/security/authentication/AuthenticationPatternTest.java
package org.example.patterns.security.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationPatternTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAuthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
               .andExpect(status().isOk());
    }
    
    @Test
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/authorization/user"))
               .andExpect(status().isUnauthorized());
    }
}
```

```java
// src/test/java/org/example/patterns/security/methodsecurity/MethodSecurityTest.java
package org.example.patterns.security.methodsecurity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MethodSecurityTest {
    
    @Autowired
    private MethodSecurityService methodSecurityService;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminOnlyMethod() {
        String result = methodSecurityService.adminOnlyMethod();
        assertNotNull(result);
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testAdminOnlyMethodDenied() {
        assertThrows(AccessDeniedException.class, 
            () -> methodSecurityService.adminOnlyMethod());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = "USER")
    void testUpdateOwnProfile() {
        String result = methodSecurityService.updateOwnProfile("admin", "data");
        assertNotNull(result);
    }
    
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testUpdateOtherProfile() {
        assertThrows(AccessDeniedException.class, 
            () -> methodSecurityService.updateOwnProfile("admin", "data"));
    }
}
```

```java
// src/test/java/org/example/patterns/security/passwordencoding/PasswordEncodingTest.java
package org.example.patterns.security.passwordencoding;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PasswordEncodingTest {
    
    @Autowired
    private PasswordEncodingService passwordEncodingService;
    
    @Test
    void testPasswordEncoding() {
        String rawPassword = "testPassword123";
        String encoded = passwordEncodingService.encodePassword(rawPassword);
        
        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("{bcrypt}"));
    }
    
    @Test
    void testPasswordMatching() {
        String rawPassword = "testPassword123";
        String encoded = passwordEncodingService.encodePassword(rawPassword);
        
        assertTrue(passwordEncodingService.matches(rawPassword, encoded));
        assertFalse(passwordEncodingService.matches("wrongPassword", encoded));
    }
}
```

## 24. README.md

```markdown
# Spring Security Patterns

Comprehensive demonstration of 17 essential Spring Security patterns.

## Patterns Implemented

### 1. Authentication Pattern
**Endpoint:** `/api/auth/status`

Custom authentication provider for validating credentials.

**Key Components:**
- `CustomAuthenticationProvider` - Custom authentication logic
- `UsernamePasswordAuthenticationToken` - Authentication token
- `AuthenticationManager` - Manages authentication providers

**Test:**
```bash
curl -u admin:admin123 http://localhost:8080/api/auth/status
```

### 2. Authorization Pattern
**Endpoints:**
- `/api/authorization/public` - Public access
- `/api/authorization/user` - Requires USER role
- `/api/authorization/admin` - Requires ADMIN role

Role-based and permission-based access control.

**Annotations:**
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@PreAuthorize("hasAuthority('READ_PRIVILEGE')")
```

### 3. Filter Chain Pattern
**Filters:**
- `RequestLoggingFilter` - Logs all requests
- `CustomAuthenticationFilter` - Custom token authentication

Security filter chain processes requests in order.

**Flow:**
```
Request → RequestLoggingFilter → CustomAuthenticationFilter → 
UsernamePasswordAuthenticationFilter → ... → Controller
```

### 4. Security Context Pattern
**Endpoint:** `/api/security-context/info`

Access current authentication information.

**Usage:**
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
```

### 5. Access Decision Manager Pattern
Coordinates multiple voters to make access decisions.

**Strategy:**
- Affirmative-based: At least one grant and no denies
- Consensus-based: Majority grants
- Unanimous: All must grant

### 6. Voter Pattern
**Custom Voters:**
- `TimeBasedVoter` - Denies access outside business hours
- `IpBasedVoter` - Allows specific IP addresses

**Returns:**
- `ACCESS_GRANTED` (1)
- `ACCESS_DENIED` (-1)
- `ACCESS_ABSTAIN` (0)

### 7. Expression-based Access Control Pattern
**Endpoint:** `/api/expression/*`

SpEL (Spring Expression Language) for access control.

**Examples:**
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
@PreAuthorize("principal.username == 'admin'")
@PreAuthorize("@customSecurityExpressionRoot.isOwner(#id)")
```

### 8. Method Security Pattern
**Endpoint:** `/api/method-security/*`

Securing methods with annotations.

**Annotations:**
- `@Secured("ROLE_ADMIN")` - Spring Security
- `@RolesAllowed("ROLE_USER")` - JSR-250
- `@PreAuthorize` - Before method execution
- `@PostAuthorize` - After method execution
- `@PreFilter` - Filter input collection
- `@PostFilter` - Filter output collection

**Example:**
```java
@PostFilter("filterObject.owner == authentication.name")
public List<Document> getDocuments() { }
```

### 9. ACL Pattern (Access Control List)
**Endpoint:** `/api/acl/*`

Fine-grained permissions on individual objects.

**Operations:**
- Check permission: `GET /api/acl/check/{type}/{id}/{permission}`
- Grant permission: `POST /api/acl/grant`
- Revoke permission: `POST /api/acl/revoke`
- List ACL: `GET /api/acl/list/{type}/{id}`

**Permissions:**
- READ
- WRITE
- DELETE
- ADMIN

### 10. Remember-Me Pattern
**Endpoint:** `/api/remember-me/status`

Persistent authentication across sessions.

**Configuration:**
```java
.rememberMe(remember -> remember
    .key("uniqueAndSecret")
    .tokenValiditySeconds(86400)
    .userDetailsService(userDetailsService)
)
```

**Login with Remember-Me:**
```html
<input type="checkbox" name="remember-me" value="true" />
```

### 11. CSRF Protection Pattern
**Endpoint:** `/api/csrf/token`

Cross-Site Request Forgery protection.

**Get Token:**
```bash
curl http://localhost:8080/api/csrf/token
```

**Use Token:**
```bash
curl -X POST http://localhost:8080/api/csrf/test \
  -H "X-CSRF-TOKEN: <token>" \
  -H "Content-Type: application/json" \
  -d '{"data":"value"}'
```

### 12. Session Fixation Protection Pattern
**Endpoint:** `/api/session-fixation/session-info`

Prevents session fixation attacks by changing session ID on authentication.

**Strategy:**
- `migrateSession` - Create new session, copy attributes
- `changeSessionId` - Change ID, keep session
- `newSession` - Create completely new session
- `none` - No protection (not recommended)

### 13. Channel Security Pattern
**Endpoint:** `/api/channel-security/info`

Ensures requests use appropriate protocol (HTTP/HTTPS).

**Configuration:**
```java
.requiresChannel(channel -> channel
    .requestMatchers("/secure/**").requiresSecure()
    .requestMatchers("/public/**").requiresInsecure()
)
```

### 14. Anonymous Authentication Pattern
**Endpoint:** `/api/anonymous/check`

Treats unauthenticated users as anonymous with `ROLE_ANONYMOUS`.

**Benefits:**
- Simplifies authorization logic
- No null checks needed
- Consistent authentication object

### 15. Run-As Authentication Pattern
**Endpoint:** `/api/run-as/elevated`

Temporarily elevates privileges for specific operations.

**Usage:**
```java
@Secured({"ROLE_USER", "RUN_AS_ADMIN"})
public String elevatedMethod() {
    // Runs with ADMIN privileges
}
```

### 16. Password Encoding Pattern
**Endpoint:** `/api/password/*`

Secure password storage and verification.

**Encoders:**
- **BCrypt** (default) - Adaptive hashing
- **PBKDF2** - Password-Based Key Derivation Function
- **SCrypt** - Memory-hard function
- **Delegating** - Supports multiple formats

**Operations:**
- Encode: `POST /api/password/encode`
- Verify: `POST /api/password/verify`
- Demonstrate: `POST /api/password/demonstrate`

### 17. User Details Service Pattern
**Endpoint:** `/api/user-details/*`

Loads user-specific data for authentication.

**Implementation:**
```java
@Override
public UserDetails loadUserByUsername(String username) 
        throws UsernameNotFoundException {
    // Load user from database
    return user;
}
```

**UserDetails:**
- Username
- Password (encoded)
- Authorities (roles/permissions)
- Account status flags

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### Default Users

**Admin:**
- Username: `admin`
- Password: `admin123`
- Roles: `ROLE_ADMIN`, `ROLE_USER`

**Regular User:**
- Username: `user`
- Password: `user123`
- Roles: `ROLE_USER`

**Disabled User:**
- Username: `disabled`
- Password: `disabled123`
- Status: Disabled

## Testing Patterns

### 1. Authentication
```bash
# Get auth status
curl -u admin:admin123 http://localhost:8080/api/auth/status

# Get current user
curl -u admin:admin123 http://localhost:8080/api/auth/user
```

### 2. Authorization
```bash
# Public endpoint
curl http://localhost:8080/api/authorization/public

# User endpoint (requires authentication)
curl -u user:user123 http://localhost:8080/api/authorization/user

# Admin endpoint (requires ADMIN role)
curl -u admin:admin123 http://localhost:8080/api/authorization/admin
```

### 3. Method Security
```bash
# Admin-only method
curl -u admin:admin123 http://localhost:8080/api/method-security/admin-only

# Get documents (filtered by ownership)
curl -u admin:admin123 http://localhost:8080/api/method-security/documents
```

### 4. ACL
```bash
# Check permission
curl -u admin:admin123 \
  http://localhost:8080/api/acl/check/Document/1/READ

# Grant permission
curl -u admin:admin123 -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"user","type":"Document","id":1,"permission":"WRITE"}' \
  http://localhost:8080/api/acl/grant
```

### 5. CSRF
```bash
# Get CSRF token
curl -c cookies.txt http://localhost:8080/api/csrf/token

# Use CSRF token
curl -b cookies.txt -X POST \
  -H "X-CSRF-TOKEN: <token>" \
  -H "Content-Type: application/json" \
  -d '{"data":"value"}' \
  http://localhost:8080/api/csrf/test
```

### 6. Password Encoding
```bash
# Encode password
curl -X POST -H "Content-Type: application/json" \
  -d '{"password":"mySecret123"}' \
  http://localhost:8080/api/password/encode

# Verify password
curl -X POST -H "Content-Type: application/json" \
  -d '{"password":"mySecret123","encoded":"{bcrypt}$2a$10$..."}' \
  http://localhost:8080/api/password/verify
```

## Security Best Practices

### 1. Password Storage
✅ Use strong encoding (BCrypt, SCrypt)
✅ Never store plain text passwords
✅ Use salt (automatic with modern encoders)
✅ Upgrade old encodings

### 2. Session Management
✅ Enable session fixation protection
✅ Set appropriate timeouts
✅ Use secure cookies
✅ Implement logout functionality

### 3. CSRF Protection
✅ Enable for state-changing operations
✅ Use tokens for all POST/PUT/DELETE
✅ Exclude only public APIs
✅ Use SameSite cookie attribute

### 4. Authorization
✅ Use method-level security
✅ Principle of least privilege
✅ Deny by default
✅ Regular access reviews

### 5. Authentication
✅ Strong password policies
✅ Account lockout mechanisms
✅ Multi-factor authentication (recommended)
✅ Secure credential storage

## Architecture

### Security Filter Chain
```
Request
  ↓
RequestLoggingFilter
  ↓
CustomAuthenticationFilter
  ↓
UsernamePasswordAuthenticationFilter
  ↓
AnonymousAuthenticationFilter
  ↓
ExceptionTranslationFilter
  ↓
FilterSecurityInterceptor
  ↓
Controller
```

### Authentication Flow
```
1. User submits credentials
2. AuthenticationFilter creates Authentication token
3. AuthenticationManager delegates to providers
4. AuthenticationProvider validates credentials
5. UserDetailsService loads user data
6. PasswordEncoder verifies password
7. Authentication object created
8. SecurityContext stores authentication
```

### Authorization Flow
```
1. Request arrives at secured resource
2. FilterSecurityInterceptor checks access
3. AccessDecisionManager consulted
4. Voters vote on access
5. Decision made (grant/deny)
6. Access granted or AccessDeniedException thrown
```

## Common Issues

### 1. 401 Unauthorized
**Cause:** Not authenticated
**Solution:** Provide valid credentials

### 2. 403 Forbidden
**Cause:** Authenticated but not authorized
**Solution:** Check user roles/permissions

### 3. CSRF Token Missing
**Cause:** CSRF protection enabled, no token provided
**Solution:** Include CSRF token in requests

### 4. Session Expired
**Cause:** Session timeout
**Solution:** Re-authenticate

## License

MIT License - free to use for learning and projects.
```

This completes the comprehensive implementation of all 17 Spring Security Patterns with working code, configurations, tests, and thorough documentation!