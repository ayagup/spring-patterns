package com.example.apisecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Basic Authentication Pattern
 * 
 * Demonstrates HTTP Basic Authentication where credentials are sent
 * in the Authorization header as Base64-encoded username:password.
 * 
 * Features:
 * - Standard HTTP Basic Authentication
 * - Base64 encoding of credentials
 * - UserDetailsService for user management
 * - BCrypt password encoding
 * - Stateless session management
 * - Role-based access control
 * 
 * Key Components:
 * - BasicAuth security configuration
 * - In-memory user details manager
 * - Password encoder
 * - Protected and public endpoints
 * 
 * Usage:
 * Send requests with header: Authorization: Basic base64(username:password)
 */
@SpringBootApplication
public class BasicAuthenticationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(BasicAuthenticationPattern.class, args);
    }
    
    /**
     * Security Configuration
     * Configures HTTP Basic Authentication
     */
    @Configuration
    @EnableWebSecurity
    public static class BasicAuthSecurityConfig {
        
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                    .antMatchers("/api/public/**").permitAll()
                    .antMatchers("/api/admin/**").hasRole("ADMIN")
                    .antMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                    .anyRequest().authenticated()
                .and()
                .httpBasic()
                    .realmName("Basic Auth Realm");
            
            return http.build();
        }
        
        /**
         * Configure in-memory users with different roles
         */
        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "USER")
                .build();
            
            UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user123"))
                .roles("USER")
                .build();
            
            UserDetails guest = User.builder()
                .username("guest")
                .password(passwordEncoder().encode("guest123"))
                .roles("GUEST")
                .build();
            
            return new InMemoryUserDetailsManager(admin, user, guest);
        }
        
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
    
    /**
     * User Management Service
     */
    @Service
    public static class UserService {
        
        private final Map<String, UserInfo> userInfoMap = new HashMap<>();
        
        public UserService() {
            // Initialize some user info
            userInfoMap.put("admin", new UserInfo("admin", "Administrator", "admin@example.com"));
            userInfoMap.put("user", new UserInfo("user", "Regular User", "user@example.com"));
            userInfoMap.put("guest", new UserInfo("guest", "Guest User", "guest@example.com"));
        }
        
        public Optional<UserInfo> getUserInfo(String username) {
            return Optional.ofNullable(userInfoMap.get(username));
        }
        
        public List<UserInfo> getAllUsers() {
            return new ArrayList<>(userInfoMap.values());
        }
    }
    
    /**
     * User Information Model
     */
    public static class UserInfo {
        private String username;
        private String fullName;
        private String email;
        private LocalDateTime lastLogin;
        
        public UserInfo(String username, String fullName, String email) {
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.lastLogin = LocalDateTime.now();
        }
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public LocalDateTime getLastLogin() { return lastLogin; }
        public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    }
    
    /**
     * REST Controller demonstrating Basic Authentication
     */
    @RestController
    @RequestMapping("/api")
    public static class BasicAuthController {
        
        private final UserService userService;
        
        public BasicAuthController(UserService userService) {
            this.userService = userService;
        }
        
        /**
         * Public endpoint - no authentication required
         */
        @GetMapping("/public/welcome")
        public Map<String, String> publicWelcome() {
            return Map.of(
                "message", "Welcome! This is a public endpoint.",
                "timestamp", LocalDateTime.now().toString(),
                "authentication", "Not required"
            );
        }
        
        /**
         * User endpoint - requires USER or ADMIN role
         */
        @GetMapping("/user/profile")
        public Map<String, Object> getUserProfile(Authentication authentication) {
            String username = authentication.getName();
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("authorities", authentication.getAuthorities());
            response.put("authenticated", authentication.isAuthenticated());
            
            userService.getUserInfo(username).ifPresent(userInfo -> {
                response.put("fullName", userInfo.getFullName());
                response.put("email", userInfo.getEmail());
                response.put("lastLogin", userInfo.getLastLogin());
            });
            
            return response;
        }
        
        /**
         * User data endpoint - requires authentication
         */
        @GetMapping("/user/data")
        public Map<String, Object> getUserData(Authentication authentication) {
            Map<String, Object> data = new HashMap<>();
            data.put("message", "User-specific data");
            data.put("user", authentication.getName());
            data.put("roles", authentication.getAuthorities());
            data.put("timestamp", LocalDateTime.now());
            data.put("data", Arrays.asList(
                "Item 1 for " + authentication.getName(),
                "Item 2 for " + authentication.getName(),
                "Item 3 for " + authentication.getName()
            ));
            
            return data;
        }
        
        /**
         * Admin endpoint - requires ADMIN role only
         */
        @GetMapping("/admin/users")
        public Map<String, Object> getAllUsers(Authentication authentication) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin access granted");
            response.put("admin", authentication.getName());
            response.put("users", userService.getAllUsers());
            response.put("timestamp", LocalDateTime.now());
            
            return response;
        }
        
        /**
         * Admin configuration endpoint
         */
        @GetMapping("/admin/config")
        public Map<String, Object> getAdminConfig() {
            Map<String, Object> config = new HashMap<>();
            config.put("serverName", "Production Server");
            config.put("version", "1.0.0");
            config.put("environment", "production");
            config.put("features", Arrays.asList("feature1", "feature2", "feature3"));
            config.put("timestamp", LocalDateTime.now());
            
            return config;
        }
        
        /**
         * Protected resource - requires authentication
         */
        @GetMapping("/protected/resource")
        public Map<String, Object> getProtectedResource(Authentication authentication) {
            Map<String, Object> resource = new HashMap<>();
            resource.put("resourceId", "RES-" + UUID.randomUUID().toString());
            resource.put("accessedBy", authentication.getName());
            resource.put("accessTime", LocalDateTime.now());
            resource.put("resourceData", "Sensitive protected data");
            resource.put("authorities", authentication.getAuthorities());
            
            return resource;
        }
        
        /**
         * Update user profile - POST endpoint
         */
        @PostMapping("/user/update-profile")
        public Map<String, Object> updateProfile(
                @RequestBody UpdateProfileRequest request,
                Authentication authentication) {
            
            String username = authentication.getName();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("username", username);
            response.put("updatedFields", request);
            response.put("timestamp", LocalDateTime.now());
            
            return response;
        }
        
        /**
         * Get authentication details
         */
        @GetMapping("/auth/details")
        public Map<String, Object> getAuthDetails(Authentication authentication) {
            Map<String, Object> details = new HashMap<>();
            details.put("principal", authentication.getPrincipal());
            details.put("credentials", "[PROTECTED]");
            details.put("authorities", authentication.getAuthorities());
            details.put("details", authentication.getDetails());
            details.put("authenticated", authentication.isAuthenticated());
            details.put("name", authentication.getName());
            
            return details;
        }
    }
    
    /**
     * Request DTOs
     */
    public static class UpdateProfileRequest {
        private String fullName;
        private String email;
        private String phone;
        
        // Getters and setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
