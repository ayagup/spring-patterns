package com.example.ldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LDAP User Details Pattern
 * 
 * Demonstrates Spring Security integration with LDAP for user authentication and authorization.
 * 
 * User Details Features:
 * - Load user information from LDAP directory
 * - Map LDAP attributes to Spring Security UserDetails
 * - Extract user roles and authorities from LDAP groups
 * - Custom attribute mapping
 * - Password validation support
 * 
 * Key Components:
 * - UserDetailsService: Load user by username
 * - LdapUserDetailsMapper: Map LDAP attributes to UserDetails
 * - Authority extraction from group membership
 * - Custom user attribute mapping
 * 
 * Use Cases:
 * - LDAP-based authentication
 * - Load user profile from directory
 * - Role-based access control from LDAP groups
 * - User attribute synchronization
 * - Corporate directory integration
 * 
 * Security Considerations:
 * - Never return password in UserDetails
 * - Validate user account status (enabled, locked, expired)
 * - Map LDAP groups to application roles correctly
 * - Handle missing or null attributes gracefully
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class LDAPUserDetailsPattern {

    /**
     * LDAP user details service bean
     */
    @Bean
    public UserDetailsService ldapUserDetailsService(LdapTemplate ldapTemplate) {
        return new LdapUserDetailsServiceImpl(ldapTemplate);
    }

    /**
     * Custom LDAP user details mapper
     */
    @Bean
    public LdapUserDetailsMapper ldapUserDetailsMapper() {
        LdapUserDetailsMapper mapper = new LdapUserDetailsMapper();
        mapper.setConvertToUpperCase(false);
        return mapper;
    }
}

/**
 * LDAP user details service implementation
 */
class LdapUserDetailsServiceImpl implements UserDetailsService {

    private final LdapTemplate ldapTemplate;
    private final Map<String, CachedUserDetails> userCache = new HashMap<>();

    public LdapUserDetailsServiceImpl(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Check cache first
        CachedUserDetails cached = userCache.get(username);
        if (cached != null && !cached.isExpired()) {
            return cached.getUserDetails();
        }

        try {
            // Search for user in LDAP
            List<DirContextOperations> users = ldapTemplate.search(
                LdapQueryBuilder.query()
                    .base("ou=people")
                    .where("uid").is(username),
                (DirContextOperations ctx) -> ctx
            );

            if (users.isEmpty()) {
                throw new UsernameNotFoundException("User not found: " + username);
            }

            DirContextOperations userContext = users.get(0);
            
            // Extract user attributes
            String dn = userContext.getNameInNamespace();
            String cn = userContext.getStringAttribute("cn");
            String mail = userContext.getStringAttribute("mail");
            String[] memberOf = userContext.getStringAttributes("memberOf");
            
            // Extract authorities from group membership
            Collection<GrantedAuthority> authorities = extractAuthorities(memberOf);
            
            // Create UserDetails
            LdapUserDetails userDetails = new LdapUserDetails(
                username,
                "", // Password not returned
                authorities,
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                dn,
                cn,
                mail,
                Arrays.asList(memberOf != null ? memberOf : new String[0])
            );

            // Cache user details (5 minutes)
            userCache.put(username, new CachedUserDetails(userDetails, 300000));

            return userDetails;

        } catch (Exception e) {
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }

    /**
     * Extract authorities from LDAP group membership
     */
    private Collection<GrantedAuthority> extractAuthorities(String[] memberOf) {
        if (memberOf == null || memberOf.length == 0) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        
        for (String group : memberOf) {
            // Extract CN from group DN (e.g., cn=Admins,ou=groups,dc=example,dc=com)
            String groupName = extractCN(group);
            if (groupName != null) {
                // Convert group name to Spring Security role format
                String role = "ROLE_" + groupName.toUpperCase().replace(" ", "_");
                authorities.add(new SimpleGrantedAuthority(role));
            }
        }

        // Ensure at least ROLE_USER exists
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }

    /**
     * Extract CN from distinguished name
     */
    private String extractCN(String dn) {
        if (dn == null || dn.isEmpty()) {
            return null;
        }
        
        String[] parts = dn.split(",");
        for (String part : parts) {
            if (part.trim().toLowerCase().startsWith("cn=")) {
                return part.trim().substring(3);
            }
        }
        return null;
    }

    /**
     * Clear user cache
     */
    public void clearCache() {
        userCache.clear();
    }

    /**
     * Remove specific user from cache
     */
    public void evictUser(String username) {
        userCache.remove(username);
    }
}

/**
 * Custom LDAP user details implementation
 */
class LdapUserDetails implements UserDetails {
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean credentialsNonExpired;
    private final boolean accountNonLocked;
    
    // Additional LDAP attributes
    private final String distinguishedName;
    private final String commonName;
    private final String email;
    private final List<String> groups;

    public LdapUserDetails(String username, String password,
                          Collection<? extends GrantedAuthority> authorities,
                          boolean enabled, boolean accountNonExpired,
                          boolean credentialsNonExpired, boolean accountNonLocked,
                          String distinguishedName, String commonName,
                          String email, List<String> groups) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.distinguishedName = distinguishedName;
        this.commonName = commonName;
        this.email = email;
        this.groups = groups;
    }

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

    // Additional getters
    public String getDistinguishedName() { return distinguishedName; }
    public String getCommonName() { return commonName; }
    public String getEmail() { return email; }
    public List<String> getGroups() { return groups; }
}

/**
 * Cached user details with expiration
 */
class CachedUserDetails {
    private final UserDetails userDetails;
    private final long expirationTime;

    public CachedUserDetails(UserDetails userDetails, long ttlMillis) {
        this.userDetails = userDetails;
        this.expirationTime = System.currentTimeMillis() + ttlMillis;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
}

/**
 * LDAP user details service
 */
@RestController
@RequestMapping("/api/ldap-user-details")
class LDAPUserDetailsService {

    private final UserDetailsService userDetailsService;

    public LDAPUserDetailsService(UserDetailsService ldapUserDetailsService) {
        this.userDetailsService = ldapUserDetailsService;
    }

    /**
     * Load user details by username
     */
    public UserDetailsResponse loadUser(String username) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (userDetails instanceof LdapUserDetails ldapUser) {
                return new UserDetailsResponse(
                    true,
                    ldapUser.getUsername(),
                    ldapUser.getCommonName(),
                    ldapUser.getEmail(),
                    ldapUser.getDistinguishedName(),
                    ldapUser.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()),
                    ldapUser.getGroups(),
                    ldapUser.isEnabled(),
                    ldapUser.isAccountNonExpired(),
                    ldapUser.isAccountNonLocked(),
                    ldapUser.isCredentialsNonExpired(),
                    null
                );
            } else {
                return new UserDetailsResponse(
                    true,
                    userDetails.getUsername(),
                    null,
                    null,
                    null,
                    userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()),
                    Collections.emptyList(),
                    userDetails.isEnabled(),
                    userDetails.isAccountNonExpired(),
                    userDetails.isAccountNonLocked(),
                    userDetails.isCredentialsNonExpired(),
                    null
                );
            }
        } catch (UsernameNotFoundException e) {
            return new UserDetailsResponse(
                false, username, null, null, null,
                Collections.emptyList(), Collections.emptyList(),
                false, false, false, false,
                "User not found: " + e.getMessage()
            );
        }
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(String username, String role) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get all roles for user
     */
    public List<String> getUserRoles(String username) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        } catch (UsernameNotFoundException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Clear user cache
     */
    public boolean clearCache() {
        if (userDetailsService instanceof LdapUserDetailsServiceImpl service) {
            service.clearCache();
            return true;
        }
        return false;
    }

    /**
     * Evict specific user from cache
     */
    public boolean evictUser(String username) {
        if (userDetailsService instanceof LdapUserDetailsServiceImpl service) {
            service.evictUser(username);
            return true;
        }
        return false;
    }

    record UserDetailsResponse(boolean found, String username, String commonName, String email,
                              String distinguishedName, List<String> authorities, List<String> groups,
                              boolean enabled, boolean accountNonExpired, boolean accountNonLocked,
                              boolean credentialsNonExpired, String errorMessage) {}
}

/**
 * REST controller for LDAP user details endpoints
 */
@RestController
@RequestMapping("/api/ldap-user-details")
class LDAPUserDetailsController {

    private final LDAPUserDetailsService userDetailsService;

    public LDAPUserDetailsController(LDAPUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/load/{username}")
    public ResponseEntity<LDAPUserDetailsService.UserDetailsResponse> loadUser(
            @PathVariable String username) {
        LDAPUserDetailsService.UserDetailsResponse response = userDetailsService.loadUser(username);
        return response.found() ? 
            ResponseEntity.ok(response) : 
            ResponseEntity.notFound().build();
    }

    @GetMapping("/has-role")
    public ResponseEntity<RoleCheckResponse> hasRole(
            @RequestParam String username,
            @RequestParam String role) {
        boolean hasRole = userDetailsService.hasRole(username, role);
        return ResponseEntity.ok(new RoleCheckResponse(username, role, hasRole));
    }

    @GetMapping("/roles/{username}")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable String username) {
        List<String> roles = userDetailsService.getUserRoles(username);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<CacheResponse> clearCache() {
        boolean cleared = userDetailsService.clearCache();
        return ResponseEntity.ok(new CacheResponse("all", cleared, "Cache cleared"));
    }

    @PostMapping("/cache/evict/{username}")
    public ResponseEntity<CacheResponse> evictUser(@PathVariable String username) {
        boolean evicted = userDetailsService.evictUser(username);
        return ResponseEntity.ok(new CacheResponse(username, evicted, 
            evicted ? "User evicted from cache" : "User not found in cache"));
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        PatternInfo info = new PatternInfo(
            "LDAP User Details Pattern",
            "Spring Security integration with LDAP for user authentication",
            "1.0",
            List.of(
                "Load user information from LDAP",
                "Map LDAP attributes to UserDetails",
                "Extract roles from LDAP groups",
                "Custom attribute mapping",
                "User details caching"
            ),
            List.of(
                "LDAP-based authentication",
                "User profile loading from directory",
                "Role-based access control",
                "User attribute synchronization",
                "Corporate directory integration"
            )
        );
        return ResponseEntity.ok(info);
    }

    record RoleCheckResponse(String username, String role, boolean hasRole) {}
    record CacheResponse(String target, boolean success, String message) {}
    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
