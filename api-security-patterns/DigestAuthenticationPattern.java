package com.example.apisecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Digest Authentication Pattern
 * 
 * Demonstrates HTTP Digest Authentication which is more secure than Basic Auth
 * as it doesn't send passwords in plain text. Uses MD5 hashing with nonce.
 * 
 * Features:
 * - HTTP Digest Authentication with MD5 hashing
 * - Nonce-based challenge-response mechanism
 * - Server-side nonce generation
 * - Protection against replay attacks
 * - More secure than Basic Auth (password never sent)
 * - Quality of Protection (qop) support
 * 
 * Key Components:
 * - DigestAuthenticationEntryPoint: Challenges unauthenticated requests
 * - DigestAuthenticationFilter: Validates digest credentials
 * - UserDetailsService: Provides user credentials
 * - MD5 hashing algorithm
 * 
 * Note: Digest Auth is deprecated in modern applications. Use OAuth2/JWT instead.
 */
@SpringBootApplication
public class DigestAuthenticationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(DigestAuthenticationPattern.class, args);
    }
    
    /**
     * Security Configuration
     * Configures HTTP Digest Authentication
     */
    @Configuration
    @EnableWebSecurity
    public static class DigestAuthSecurityConfig {
        
        private final UserDetailsService userDetailsService;
        
        public DigestAuthSecurityConfig(UserDetailsService userDetailsService) {
            this.userDetailsService = userDetailsService;
        }
        
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers("/api/public/**").permitAll()
                    .antMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                    .authenticationEntryPoint(digestAuthenticationEntryPoint())
                .and()
                .addFilter(digestAuthenticationFilter());
            
            return http.build();
        }
        
        /**
         * Digest Authentication Entry Point
         * Sends WWW-Authenticate challenge to clients
         */
        @Bean
        public DigestAuthenticationEntryPoint digestAuthenticationEntryPoint() {
            DigestAuthenticationEntryPoint entryPoint = new DigestAuthenticationEntryPoint();
            entryPoint.setRealmName("Digest Auth Realm");
            entryPoint.setKey("unique-digest-key");
            entryPoint.setNonceValiditySeconds(300); // 5 minutes
            return entryPoint;
        }
        
        /**
         * Digest Authentication Filter
         * Validates digest authentication credentials
         */
        @Bean
        public DigestAuthenticationFilter digestAuthenticationFilter() {
            DigestAuthenticationFilter filter = new DigestAuthenticationFilter();
            filter.setUserDetailsService(userDetailsService);
            filter.setAuthenticationEntryPoint(digestAuthenticationEntryPoint());
            filter.setPasswordAlreadyEncoded(false);
            return filter;
        }
        
        /**
         * Configure in-memory users
         * Note: Digest auth requires plain text passwords for MD5 hashing
         */
        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails admin = User.builder()
                .username("admin")
                .password("admin123") // Plain text for Digest Auth
                .roles("ADMIN", "USER")
                .build();
            
            UserDetails user = User.builder()
                .username("user")
                .password("user123") // Plain text for Digest Auth
                .roles("USER")
                .build();
            
            return new InMemoryUserDetailsManager(admin, user);
        }
        
        /**
         * No password encoding for Digest Auth
         * Digest auth requires access to plain text password for MD5 hashing
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
            return NoOpPasswordEncoder.getInstance();
        }
    }
    
    /**
     * Digest Helper Service
     * Utility methods for digest authentication
     */
    @Service
    public static class DigestAuthService {
        
        /**
         * Generate MD5 hash
         */
        public String generateMD5(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(input.getBytes());
                StringBuilder hexString = new StringBuilder();
                
                for (byte b : messageDigest) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 algorithm not found", e);
            }
        }
        
        /**
         * Generate response for digest authentication
         * Response = MD5(MD5(username:realm:password):nonce:MD5(method:uri))
         */
        public String generateDigestResponse(String username, String realm, 
                                            String password, String nonce,
                                            String method, String uri) {
            String ha1 = generateMD5(username + ":" + realm + ":" + password);
            String ha2 = generateMD5(method + ":" + uri);
            return generateMD5(ha1 + ":" + nonce + ":" + ha2);
        }
        
        /**
         * Validate digest response
         */
        public boolean validateDigestResponse(String username, String realm,
                                             String password, String nonce,
                                             String method, String uri,
                                             String clientResponse) {
            String expectedResponse = generateDigestResponse(
                username, realm, password, nonce, method, uri
            );
            return expectedResponse.equals(clientResponse);
        }
    }
    
    /**
     * REST Controller demonstrating Digest Authentication
     */
    @RestController
    @RequestMapping("/api")
    public static class DigestAuthController {
        
        private final DigestAuthService digestAuthService;
        
        public DigestAuthController(DigestAuthService digestAuthService) {
            this.digestAuthService = digestAuthService;
        }
        
        /**
         * Public endpoint - no authentication required
         */
        @GetMapping("/public/info")
        public Map<String, String> publicInfo() {
            return Map.of(
                "message", "Public endpoint - Digest authentication not required",
                "timestamp", LocalDateTime.now().toString(),
                "authType", "None"
            );
        }
        
        /**
         * Protected endpoint - requires digest authentication
         */
        @GetMapping("/protected/data")
        public Map<String, Object> getProtectedData(Authentication authentication) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Access granted via Digest Authentication");
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            response.put("authenticated", authentication.isAuthenticated());
            response.put("timestamp", LocalDateTime.now());
            response.put("data", Arrays.asList("Item1", "Item2", "Item3"));
            
            return response;
        }
        
        /**
         * User profile endpoint
         */
        @GetMapping("/user/profile")
        public Map<String, Object> getUserProfile(Authentication authentication) {
            Map<String, Object> profile = new HashMap<>();
            profile.put("username", authentication.getName());
            profile.put("roles", authentication.getAuthorities());
            profile.put("authenticationMethod", "Digest");
            profile.put("lastAccess", LocalDateTime.now());
            
            return profile;
        }
        
        /**
         * Admin endpoint - requires ADMIN role
         */
        @GetMapping("/admin/dashboard")
        public Map<String, Object> getAdminDashboard(Authentication authentication) {
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("message", "Admin Dashboard");
            dashboard.put("admin", authentication.getName());
            dashboard.put("totalUsers", 42);
            dashboard.put("activeUsers", 23);
            dashboard.put("timestamp", LocalDateTime.now());
            dashboard.put("stats", Map.of(
                "requests", 1250,
                "errors", 3,
                "averageResponseTime", "120ms"
            ));
            
            return dashboard;
        }
        
        /**
         * Secure resource endpoint
         */
        @GetMapping("/secure/resource/{id}")
        public Map<String, Object> getSecureResource(
                @PathVariable String id,
                Authentication authentication) {
            
            Map<String, Object> resource = new HashMap<>();
            resource.put("resourceId", id);
            resource.put("accessedBy", authentication.getName());
            resource.put("accessTime", LocalDateTime.now());
            resource.put("resourceType", "Secure Document");
            resource.put("content", "Sensitive content for resource " + id);
            resource.put("authMethod", "Digest Authentication");
            
            return resource;
        }
        
        /**
         * Update operation - POST with digest auth
         */
        @PostMapping("/user/update")
        public Map<String, Object> updateUserData(
                @RequestBody Map<String, Object> data,
                Authentication authentication) {
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Data updated successfully");
            response.put("user", authentication.getName());
            response.put("updatedData", data);
            response.put("timestamp", LocalDateTime.now());
            
            return response;
        }
        
        /**
         * Authentication info endpoint
         */
        @GetMapping("/auth/info")
        public Map<String, Object> getAuthInfo(Authentication authentication) {
            Map<String, Object> info = new HashMap<>();
            info.put("authenticationType", authentication.getClass().getSimpleName());
            info.put("principal", authentication.getName());
            info.put("authorities", authentication.getAuthorities());
            info.put("authenticated", authentication.isAuthenticated());
            info.put("details", authentication.getDetails());
            info.put("timestamp", LocalDateTime.now());
            
            return info;
        }
        
        /**
         * Digest algorithm info
         */
        @GetMapping("/public/digest-info")
        public Map<String, Object> getDigestInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("algorithm", "MD5");
            info.put("description", "HTTP Digest Authentication");
            info.put("security", "More secure than Basic Auth");
            info.put("mechanism", "Challenge-Response with nonce");
            info.put("passwordTransmission", "Never sent over network");
            info.put("response", "MD5(MD5(username:realm:password):nonce:MD5(method:uri))");
            
            return info;
        }
    }
}
