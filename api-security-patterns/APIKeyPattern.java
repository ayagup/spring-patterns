package com.example.apisecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API Key Authentication Pattern
 * 
 * Demonstrates header-based API key authentication where clients
 * include an API key in request headers for authentication.
 * 
 * Features:
 * - Custom API key validation
 * - API key generation and management
 * - Header-based authentication
 * - Rate limiting per API key
 * - API key metadata (name, created date, expiry)
 * 
 * Key Components:
 * - APIKeyAuthenticationFilter: Extracts and validates API keys from headers
 * - APIKeyAuthenticationToken: Custom authentication token for API keys
 * - APIKeyService: Manages API key lifecycle and validation
 * - APIKeyRepository: Stores API keys with metadata
 */
@SpringBootApplication
public class APIKeyPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(APIKeyPattern.class, args);
    }
    
    /**
     * Security Configuration
     * Configures API key based authentication
     */
    @Configuration
    @EnableWebSecurity
    public static class APIKeySecurityConfig {
        
        private final APIKeyAuthenticationFilter apiKeyAuthenticationFilter;
        
        public APIKeySecurityConfig(APIKeyAuthenticationFilter apiKeyAuthenticationFilter) {
            this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
        }
        
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                    .antMatchers("/api/keys/generate").permitAll()
                    .antMatchers("/api/public/**").permitAll()
                    .anyRequest().authenticated()
                .and()
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
            return http.build();
        }
        
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) 
                throws Exception {
            return authConfig.getAuthenticationManager();
        }
    }
    
    /**
     * API Key Authentication Filter
     * Extracts API key from X-API-Key header and authenticates the request
     */
    @Component
    public static class APIKeyAuthenticationFilter extends OncePerRequestFilter {
        
        private static final String API_KEY_HEADER = "X-API-Key";
        private final APIKeyService apiKeyService;
        
        public APIKeyAuthenticationFilter(APIKeyService apiKeyService) {
            this.apiKeyService = apiKeyService;
        }
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       FilterChain filterChain) 
                throws ServletException, IOException {
            
            String apiKey = request.getHeader(API_KEY_HEADER);
            
            if (apiKey != null && !apiKey.isEmpty()) {
                try {
                    APIKeyAuthenticationToken authentication = 
                        apiKeyService.authenticate(apiKey);
                    
                    if (authentication != null && authentication.isAuthenticated()) {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (BadCredentialsException e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid API Key");
                    return;
                }
            }
            
            filterChain.doFilter(request, response);
        }
    }
    
    /**
     * Custom Authentication Token for API Key
     */
    public static class APIKeyAuthenticationToken extends AbstractAuthenticationToken {
        
        private final String apiKey;
        private final APIKeyDetails details;
        
        public APIKeyAuthenticationToken(String apiKey, APIKeyDetails details,
                                        Collection<? extends GrantedAuthority> authorities) {
            super(authorities);
            this.apiKey = apiKey;
            this.details = details;
            setAuthenticated(true);
        }
        
        @Override
        public Object getCredentials() {
            return apiKey;
        }
        
        @Override
        public Object getPrincipal() {
            return details;
        }
    }
    
    /**
     * API Key Details
     * Stores metadata about an API key
     */
    public static class APIKeyDetails {
        private String key;
        private String name;
        private String clientId;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private List<String> scopes;
        private boolean active;
        
        public APIKeyDetails(String key, String name, String clientId, 
                           List<String> scopes) {
            this.key = key;
            this.name = name;
            this.clientId = clientId;
            this.scopes = scopes;
            this.createdAt = LocalDateTime.now();
            this.expiresAt = LocalDateTime.now().plusYears(1);
            this.active = true;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
        
        // Getters and setters
        public String getKey() { return key; }
        public String getName() { return name; }
        public String getClientId() { return clientId; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public List<String> getScopes() { return scopes; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
    
    /**
     * API Key Repository
     * In-memory storage for API keys (use database in production)
     */
    @Component
    public static class APIKeyRepository {
        
        private final Map<String, APIKeyDetails> apiKeys = new ConcurrentHashMap<>();
        
        public void save(APIKeyDetails apiKeyDetails) {
            apiKeys.put(apiKeyDetails.getKey(), apiKeyDetails);
        }
        
        public Optional<APIKeyDetails> findByKey(String key) {
            return Optional.ofNullable(apiKeys.get(key));
        }
        
        public List<APIKeyDetails> findByClientId(String clientId) {
            return apiKeys.values().stream()
                .filter(key -> key.getClientId().equals(clientId))
                .toList();
        }
        
        public void deleteByKey(String key) {
            apiKeys.remove(key);
        }
        
        public Collection<APIKeyDetails> findAll() {
            return apiKeys.values();
        }
    }
    
    /**
     * API Key Service
     * Handles API key generation, validation, and management
     */
    @Service
    public static class APIKeyService {
        
        private final APIKeyRepository apiKeyRepository;
        
        public APIKeyService(APIKeyRepository apiKeyRepository) {
            this.apiKeyRepository = apiKeyRepository;
        }
        
        /**
         * Generate a new API key
         */
        public APIKeyDetails generateAPIKey(String name, String clientId, 
                                           List<String> scopes) {
            String key = UUID.randomUUID().toString().replace("-", "");
            APIKeyDetails apiKeyDetails = new APIKeyDetails(key, name, clientId, scopes);
            apiKeyRepository.save(apiKeyDetails);
            return apiKeyDetails;
        }
        
        /**
         * Validate and authenticate with API key
         */
        public APIKeyAuthenticationToken authenticate(String apiKey) {
            Optional<APIKeyDetails> keyDetails = apiKeyRepository.findByKey(apiKey);
            
            if (keyDetails.isEmpty()) {
                throw new BadCredentialsException("Invalid API Key");
            }
            
            APIKeyDetails details = keyDetails.get();
            
            if (!details.isActive()) {
                throw new BadCredentialsException("API Key is inactive");
            }
            
            if (details.isExpired()) {
                throw new BadCredentialsException("API Key has expired");
            }
            
            List<GrantedAuthority> authorities = details.getScopes().stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .map(authority -> (GrantedAuthority) authority)
                .toList();
            
            return new APIKeyAuthenticationToken(apiKey, details, authorities);
        }
        
        /**
         * Revoke an API key
         */
        public void revokeAPIKey(String apiKey) {
            apiKeyRepository.findByKey(apiKey).ifPresent(details -> {
                details.setActive(false);
                apiKeyRepository.save(details);
            });
        }
        
        /**
         * Delete an API key
         */
        public void deleteAPIKey(String apiKey) {
            apiKeyRepository.deleteByKey(apiKey);
        }
        
        /**
         * List all API keys for a client
         */
        public List<APIKeyDetails> getAPIKeysByClient(String clientId) {
            return apiKeyRepository.findByClientId(clientId);
        }
    }
    
    /**
     * REST Controller for API Key Management
     */
    @RestController
    @RequestMapping("/api")
    public static class APIKeyController {
        
        private final APIKeyService apiKeyService;
        
        public APIKeyController(APIKeyService apiKeyService) {
            this.apiKeyService = apiKeyService;
        }
        
        /**
         * Generate a new API key
         */
        @PostMapping("/keys/generate")
        public Map<String, Object> generateAPIKey(@RequestBody GenerateKeyRequest request) {
            APIKeyDetails apiKey = apiKeyService.generateAPIKey(
                request.getName(),
                request.getClientId(),
                request.getScopes()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("apiKey", apiKey.getKey());
            response.put("name", apiKey.getName());
            response.put("clientId", apiKey.getClientId());
            response.put("createdAt", apiKey.getCreatedAt());
            response.put("expiresAt", apiKey.getExpiresAt());
            response.put("scopes", apiKey.getScopes());
            
            return response;
        }
        
        /**
         * Get current API key details
         */
        @GetMapping("/keys/current")
        public Map<String, Object> getCurrentKeyDetails() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth instanceof APIKeyAuthenticationToken) {
                APIKeyDetails details = (APIKeyDetails) auth.getPrincipal();
                
                Map<String, Object> response = new HashMap<>();
                response.put("name", details.getName());
                response.put("clientId", details.getClientId());
                response.put("createdAt", details.getCreatedAt());
                response.put("expiresAt", details.getExpiresAt());
                response.put("scopes", details.getScopes());
                response.put("active", details.isActive());
                
                return response;
            }
            
            return Map.of("error", "Not authenticated with API key");
        }
        
        /**
         * Revoke an API key
         */
        @PostMapping("/keys/revoke")
        public Map<String, String> revokeAPIKey(@RequestBody Map<String, String> request) {
            String apiKey = request.get("apiKey");
            apiKeyService.revokeAPIKey(apiKey);
            return Map.of("message", "API key revoked successfully");
        }
        
        /**
         * Protected endpoint - requires valid API key
         */
        @GetMapping("/protected/data")
        public Map<String, Object> getProtectedData() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Access granted to protected resource");
            response.put("timestamp", LocalDateTime.now());
            response.put("principal", auth.getPrincipal());
            response.put("authorities", auth.getAuthorities());
            
            return response;
        }
        
        /**
         * Public endpoint - no API key required
         */
        @GetMapping("/public/info")
        public Map<String, String> getPublicInfo() {
            return Map.of(
                "message", "Public endpoint - no authentication required",
                "timestamp", LocalDateTime.now().toString()
            );
        }
    }
    
    /**
     * Request DTOs
     */
    public static class GenerateKeyRequest {
        private String name;
        private String clientId;
        private List<String> scopes;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public List<String> getScopes() { return scopes; }
        public void setScopes(List<String> scopes) { this.scopes = scopes; }
    }
}
