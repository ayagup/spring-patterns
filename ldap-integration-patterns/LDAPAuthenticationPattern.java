package com.example.ldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.web.bind.annotation.*;

import javax.naming.directory.DirContext;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * LDAP Authentication Pattern
 * 
 * Demonstrates LDAP-based authentication using Spring Security.
 * 
 * Authentication Methods:
 * - Bind Authentication: Authenticate by binding with user credentials
 * - Password Comparison: Compare submitted password with LDAP stored password
 * - Delegated Authentication: Delegate to LDAP server for authentication
 * 
 * Key Features:
 * - User credential validation against LDAP
 * - Group-based authority extraction
 * - Failed login attempt tracking
 * - Account lockout after multiple failures
 * - Authentication audit logging
 * 
 * Use Cases:
 * - Corporate LDAP authentication
 * - Active Directory integration
 * - Single Sign-On (SSO) with LDAP
 * - Centralized user management
 * - Enterprise authentication
 * 
 * Security Considerations:
 * - Always use LDAPS (secure LDAP) for authentication
 * - Implement account lockout after failed attempts
 * - Log authentication events for audit
 * - Sanitize inputs to prevent LDAP injection
 * - Use strong password policies
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class LDAPAuthenticationPattern {

    /**
     * LDAP authentication provider
     */
    @Bean
    public AuthenticationProvider ldapAuthenticationProvider(
            LdapTemplate ldapTemplate,
            org.springframework.ldap.core.support.LdapContextSource contextSource) {
        
        // Configure user search
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
            "ou=people",
            "(uid={0})",
            contextSource
        );

        // Configure bind authenticator
        BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
        bindAuthenticator.setUserSearch(userSearch);

        // Configure authorities populator
        DefaultLdapAuthoritiesPopulator authoritiesPopulator = 
            new DefaultLdapAuthoritiesPopulator(contextSource, "ou=groups");
        authoritiesPopulator.setGroupRoleAttribute("cn");
        authoritiesPopulator.setGroupSearchFilter("(member={0})");
        authoritiesPopulator.setRolePrefix("ROLE_");
        authoritiesPopulator.setConvertToUpperCase(true);

        return new LdapAuthenticationProvider(bindAuthenticator, authoritiesPopulator);
    }
}

/**
 * Custom LDAP authentication service
 */
@RestController
@RequestMapping("/api/ldap-auth")
class LDAPAuthenticationService {

    private final LdapTemplate ldapTemplate;
    private final AuthenticationProvider authenticationProvider;
    
    private final Map<String, AuthenticationAttemptTracker> attemptTrackers = new ConcurrentHashMap<>();
    private final List<AuthenticationAuditEntry> auditLog = new ArrayList<>();
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes

    public LDAPAuthenticationService(
            LdapTemplate ldapTemplate,
            AuthenticationProvider ldapAuthenticationProvider) {
        this.ldapTemplate = ldapTemplate;
        this.authenticationProvider = ldapAuthenticationProvider;
    }

    /**
     * Authenticate user with username and password
     */
    public AuthenticationResult authenticate(String username, String password) {
        // Check if account is locked
        AuthenticationAttemptTracker tracker = attemptTrackers.computeIfAbsent(
            username, k -> new AuthenticationAttemptTracker());
        
        if (tracker.isLocked()) {
            long remainingLockoutMs = tracker.getRemainingLockoutTime();
            logAuthenticationEvent(username, false, "Account locked");
            return new AuthenticationResult(
                false,
                username,
                null,
                Collections.emptyList(),
                "Account is locked. Try again in " + (remainingLockoutMs / 1000) + " seconds",
                tracker.getFailedAttempts(),
                true
            );
        }

        try {
            // Attempt authentication
            Authentication authentication = authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            if (authentication != null && authentication.isAuthenticated()) {
                // Authentication successful
                tracker.recordSuccess();
                
                List<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

                logAuthenticationEvent(username, true, "Authentication successful");
                
                return new AuthenticationResult(
                    true,
                    username,
                    authentication.getName(),
                    authorities,
                    "Authentication successful",
                    0,
                    false
                );
            }
        } catch (AuthenticationException e) {
            // Authentication failed
            tracker.recordFailure();
            logAuthenticationEvent(username, false, "Invalid credentials");
            
            int remainingAttempts = MAX_FAILED_ATTEMPTS - tracker.getFailedAttempts();
            String message = remainingAttempts > 0 ?
                "Invalid credentials. " + remainingAttempts + " attempts remaining" :
                "Account locked due to too many failed attempts";
            
            return new AuthenticationResult(
                false,
                username,
                null,
                Collections.emptyList(),
                message,
                tracker.getFailedAttempts(),
                tracker.isLocked()
            );
        }

        return new AuthenticationResult(
            false,
            username,
            null,
            Collections.emptyList(),
            "Authentication failed",
            tracker.getFailedAttempts(),
            false
        );
    }

    /**
     * Verify user exists in LDAP
     */
    public boolean userExists(String username) {
        try {
            LdapQuery query = query()
                .base("ou=people")
                .where("uid").is(username);
            
            List<Object> results = ldapTemplate.search(query, (DirContextOperations ctx) -> ctx);
            return !results.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if user is member of group
     */
    public boolean isMemberOfGroup(String username, String groupName) {
        try {
            LdapQuery userQuery = query()
                .base("ou=people")
                .where("uid").is(username);
            
            List<DirContextOperations> users = ldapTemplate.search(
                userQuery, (DirContextOperations ctx) -> ctx);
            
            if (users.isEmpty()) {
                return false;
            }
            
            String userDn = users.get(0).getNameInNamespace();
            
            LdapQuery groupQuery = query()
                .base("ou=groups")
                .where("cn").is(groupName)
                .and("member").is(userDn);
            
            List<Object> groups = ldapTemplate.search(groupQuery, (DirContextOperations ctx) -> ctx);
            return !groups.isEmpty();
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get user groups
     */
    public List<String> getUserGroups(String username) {
        try {
            LdapQuery userQuery = query()
                .base("ou=people")
                .where("uid").is(username);
            
            List<DirContextOperations> users = ldapTemplate.search(
                userQuery, (DirContextOperations ctx) -> ctx);
            
            if (users.isEmpty()) {
                return Collections.emptyList();
            }
            
            String userDn = users.get(0).getNameInNamespace();
            
            LdapQuery groupQuery = query()
                .base("ou=groups")
                .where("member").is(userDn);
            
            return ldapTemplate.search(groupQuery, (DirContextOperations ctx) -> 
                ctx.getStringAttribute("cn"));
            
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Unlock user account
     */
    public boolean unlockAccount(String username) {
        AuthenticationAttemptTracker tracker = attemptTrackers.get(username);
        if (tracker != null) {
            tracker.reset();
            logAuthenticationEvent(username, true, "Account unlocked manually");
            return true;
        }
        return false;
    }

    /**
     * Get authentication statistics
     */
    public AuthenticationStatistics getStatistics() {
        long totalAttempts = auditLog.size();
        long successfulAuth = auditLog.stream().filter(AuthenticationAuditEntry::success).count();
        long failedAuth = auditLog.stream().filter(entry -> !entry.success()).count();
        long lockedAccounts = attemptTrackers.values().stream().filter(AuthenticationAttemptTracker::isLocked).count();
        
        return new AuthenticationStatistics(
            totalAttempts,
            successfulAuth,
            failedAuth,
            lockedAccounts,
            attemptTrackers.size()
        );
    }

    /**
     * Get authentication audit log
     */
    public List<AuthenticationAuditEntry> getAuditLog(int limit) {
        return auditLog.stream()
            .sorted(Comparator.comparing(AuthenticationAuditEntry::timestamp).reversed())
            .limit(limit)
            .toList();
    }

    /**
     * Get user authentication history
     */
    public List<AuthenticationAuditEntry> getUserHistory(String username, int limit) {
        return auditLog.stream()
            .filter(entry -> entry.username().equals(username))
            .sorted(Comparator.comparing(AuthenticationAuditEntry::timestamp).reversed())
            .limit(limit)
            .toList();
    }

    // Helper method
    private void logAuthenticationEvent(String username, boolean success, String message) {
        auditLog.add(new AuthenticationAuditEntry(
            Instant.now(),
            username,
            success,
            message
        ));
    }

    record AuthenticationResult(boolean success, String username, String authenticatedName,
                               List<String> authorities, String message, int failedAttempts,
                               boolean accountLocked) {}
    
    record AuthenticationStatistics(long totalAttempts, long successfulAuthentications,
                                   long failedAuthentications, long lockedAccounts,
                                   long trackedUsers) {}
    
    record AuthenticationAuditEntry(Instant timestamp, String username, boolean success, String message) {}
}

/**
 * Authentication attempt tracker
 */
class AuthenticationAttemptTracker {
    private int failedAttempts = 0;
    private Instant lastFailedAttempt;
    private Instant lockoutUntil;
    private Instant lastSuccessfulAuth;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes

    public synchronized void recordFailure() {
        failedAttempts++;
        lastFailedAttempt = Instant.now();
        
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockoutUntil = Instant.now().plusMillis(LOCKOUT_DURATION_MS);
        }
    }

    public synchronized void recordSuccess() {
        failedAttempts = 0;
        lastFailedAttempt = null;
        lockoutUntil = null;
        lastSuccessfulAuth = Instant.now();
    }

    public synchronized void reset() {
        failedAttempts = 0;
        lastFailedAttempt = null;
        lockoutUntil = null;
    }

    public synchronized boolean isLocked() {
        if (lockoutUntil == null) {
            return false;
        }
        
        if (Instant.now().isAfter(lockoutUntil)) {
            reset();
            return false;
        }
        
        return true;
    }

    public synchronized long getRemainingLockoutTime() {
        if (lockoutUntil == null) {
            return 0;
        }
        
        long remaining = lockoutUntil.toEpochMilli() - Instant.now().toEpochMilli();
        return Math.max(0, remaining);
    }

    public synchronized int getFailedAttempts() {
        return failedAttempts;
    }

    public synchronized Instant getLastFailedAttempt() {
        return lastFailedAttempt;
    }

    public synchronized Instant getLastSuccessfulAuth() {
        return lastSuccessfulAuth;
    }
}

/**
 * REST controller for LDAP authentication endpoints
 */
@RestController
@RequestMapping("/api/ldap-auth")
class LDAPAuthenticationController {

    private final LDAPAuthenticationService authService;

    public LDAPAuthenticationController(LDAPAuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<LDAPAuthenticationService.AuthenticationResult> authenticate(
            @RequestBody AuthenticationRequest request) {
        LDAPAuthenticationService.AuthenticationResult result = 
            authService.authenticate(request.username(), request.password());
        
        return result.success() ? 
            ResponseEntity.ok(result) : 
            ResponseEntity.status(401).body(result);
    }

    @GetMapping("/user-exists")
    public ResponseEntity<UserExistsResponse> userExists(@RequestParam String username) {
        boolean exists = authService.userExists(username);
        return ResponseEntity.ok(new UserExistsResponse(username, exists));
    }

    @GetMapping("/is-member")
    public ResponseEntity<MembershipResponse> isMemberOfGroup(
            @RequestParam String username,
            @RequestParam String group) {
        boolean isMember = authService.isMemberOfGroup(username, group);
        return ResponseEntity.ok(new MembershipResponse(username, group, isMember));
    }

    @GetMapping("/groups/{username}")
    public ResponseEntity<List<String>> getUserGroups(@PathVariable String username) {
        List<String> groups = authService.getUserGroups(username);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/unlock/{username}")
    public ResponseEntity<UnlockResponse> unlockAccount(@PathVariable String username) {
        boolean unlocked = authService.unlockAccount(username);
        return ResponseEntity.ok(new UnlockResponse(username, unlocked, 
            unlocked ? "Account unlocked successfully" : "Account not found or not locked"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<LDAPAuthenticationService.AuthenticationStatistics> getStatistics() {
        LDAPAuthenticationService.AuthenticationStatistics stats = authService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/audit-log")
    public ResponseEntity<List<LDAPAuthenticationService.AuthenticationAuditEntry>> getAuditLog(
            @RequestParam(defaultValue = "100") int limit) {
        List<LDAPAuthenticationService.AuthenticationAuditEntry> log = authService.getAuditLog(limit);
        return ResponseEntity.ok(log);
    }

    @GetMapping("/history/{username}")
    public ResponseEntity<List<LDAPAuthenticationService.AuthenticationAuditEntry>> getUserHistory(
            @PathVariable String username,
            @RequestParam(defaultValue = "50") int limit) {
        List<LDAPAuthenticationService.AuthenticationAuditEntry> history = 
            authService.getUserHistory(username, limit);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        PatternInfo info = new PatternInfo(
            "LDAP Authentication Pattern",
            "LDAP-based authentication using Spring Security",
            "1.0",
            List.of(
                "User credential validation against LDAP",
                "Group-based authority extraction",
                "Failed login attempt tracking",
                "Account lockout after failures",
                "Authentication audit logging"
            ),
            List.of(
                "Corporate LDAP authentication",
                "Active Directory integration",
                "Single Sign-On (SSO) with LDAP",
                "Centralized user management",
                "Enterprise authentication"
            )
        );
        return ResponseEntity.ok(info);
    }

    record AuthenticationRequest(String username, String password) {}
    record UserExistsResponse(String username, boolean exists) {}
    record MembershipResponse(String username, String group, boolean isMember) {}
    record UnlockResponse(String username, boolean unlocked, String message) {}
    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
