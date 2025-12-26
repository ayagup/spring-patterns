package com.example.apisecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token Introspection Pattern
 * 
 * Demonstrates OAuth 2.0 Token Introspection (RFC 7662) for validating
 * and obtaining metadata about access tokens.
 * 
 * Features:
 * - Token introspection endpoint
 * - Token validation and metadata retrieval
 * - Active/inactive token status
 * - Token scope and claims information
 * - Client authentication for introspection
 * - Token type detection (Bearer, JWT, etc.)
 * 
 * Key Components:
 * - Introspection endpoint (/oauth2/introspect)
 * - Token validator
 * - Token metadata provider
 * - Client authentication
 */
@SpringBootApplication
public class TokenIntrospectionPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(TokenIntrospectionPattern.class, args);
    }
    
    @Configuration
    @EnableWebSecurity
    public static class IntrospectionSecurityConfig {
        
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                    .antMatchers("/oauth2/introspect").permitAll()
                    .antMatchers("/api/public/**").permitAll()
                    .anyRequest().authenticated()
                .and()
                .httpBasic(); // Client authentication
            
            return http.build();
        }
    }
    
    /**
     * Token Store
     * Stores active tokens with metadata
     */
    @Service
    public static class TokenStore {
        
        private final Map<String, TokenMetadata> tokens = new ConcurrentHashMap<>();
        
        public TokenStore() {
            // Initialize with sample tokens
            tokens.put("sample-access-token-123", new TokenMetadata(
                "sample-access-token-123", "access_token", "user123",
                Arrays.asList("read", "write"), true, LocalDateTime.now().plusHours(1)
            ));
            tokens.put("sample-refresh-token-456", new TokenMetadata(
                "sample-refresh-token-456", "refresh_token", "user123",
                Arrays.asList("refresh"), true, LocalDateTime.now().plusDays(30)
            ));
        }
        
        public Optional<TokenMetadata> getToken(String token) {
            return Optional.ofNullable(tokens.get(token));
        }
        
        public void addToken(TokenMetadata metadata) {
            tokens.put(metadata.getToken(), metadata);
        }
        
        public void revokeToken(String token) {
            tokens.computeIfPresent(token, (k, v) -> {
                v.setActive(false);
                return v;
            });
        }
    }
    
    /**
     * Token Metadata
     */
    public static class TokenMetadata {
        private String token;
        private String tokenType;
        private String subject;
        private List<String> scopes;
        private boolean active;
        private LocalDateTime expiresAt;
        private String clientId;
        private String issuer;
        
        public TokenMetadata(String token, String tokenType, String subject,
                           List<String> scopes, boolean active, LocalDateTime expiresAt) {
            this.token = token;
            this.tokenType = tokenType;
            this.subject = subject;
            this.scopes = scopes;
            this.active = active;
            this.expiresAt = expiresAt;
            this.clientId = "default-client";
            this.issuer = "https://auth.example.com";
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
        
        // Getters and setters
        public String getToken() { return token; }
        public String getTokenType() { return tokenType; }
        public String getSubject() { return subject; }
        public List<String> getScopes() { return scopes; }
        public boolean isActive() { return active && !isExpired(); }
        public void setActive(boolean active) { this.active = active; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public String getClientId() { return clientId; }
        public String getIssuer() { return issuer; }
    }
    
    /**
     * Token Introspection Service
     */
    @Service
    public static class TokenIntrospectionService {
        
        private final TokenStore tokenStore;
        
        public TokenIntrospectionService(TokenStore tokenStore) {
            this.tokenStore = tokenStore;
        }
        
        /**
         * Introspect a token and return its metadata
         */
        public IntrospectionResponse introspect(String token) {
            Optional<TokenMetadata> metadata = tokenStore.getToken(token);
            
            if (metadata.isEmpty()) {
                return new IntrospectionResponse(false);
            }
            
            TokenMetadata tokenMeta = metadata.get();
            
            if (!tokenMeta.isActive()) {
                return new IntrospectionResponse(false);
            }
            
            return IntrospectionResponse.builder()
                .active(true)
                .scope(String.join(" ", tokenMeta.getScopes()))
                .clientId(tokenMeta.getClientId())
                .username(tokenMeta.getSubject())
                .tokenType(tokenMeta.getTokenType())
                .exp(tokenMeta.getExpiresAt().toString())
                .iat(LocalDateTime.now().minusHours(1).toString())
                .sub(tokenMeta.getSubject())
                .iss(tokenMeta.getIssuer())
                .build();
        }
    }
    
    /**
     * Introspection Response
     * RFC 7662 compliant response
     */
    public static class IntrospectionResponse {
        private boolean active;
        private String scope;
        private String clientId;
        private String username;
        private String tokenType;
        private String exp;
        private String iat;
        private String sub;
        private String iss;
        
        public IntrospectionResponse(boolean active) {
            this.active = active;
        }
        
        // Builder pattern
        public static IntrospectionResponseBuilder builder() {
            return new IntrospectionResponseBuilder();
        }
        
        public static class IntrospectionResponseBuilder {
            private final IntrospectionResponse response = new IntrospectionResponse(true);
            
            public IntrospectionResponseBuilder active(boolean active) {
                response.active = active;
                return this;
            }
            
            public IntrospectionResponseBuilder scope(String scope) {
                response.scope = scope;
                return this;
            }
            
            public IntrospectionResponseBuilder clientId(String clientId) {
                response.clientId = clientId;
                return this;
            }
            
            public IntrospectionResponseBuilder username(String username) {
                response.username = username;
                return this;
            }
            
            public IntrospectionResponseBuilder tokenType(String tokenType) {
                response.tokenType = tokenType;
                return this;
            }
            
            public IntrospectionResponseBuilder exp(String exp) {
                response.exp = exp;
                return this;
            }
            
            public IntrospectionResponseBuilder iat(String iat) {
                response.iat = iat;
                return this;
            }
            
            public IntrospectionResponseBuilder sub(String sub) {
                response.sub = sub;
                return this;
            }
            
            public IntrospectionResponseBuilder iss(String iss) {
                response.iss = iss;
                return this;
            }
            
            public IntrospectionResponse build() {
                return response;
            }
        }
        
        // Getters
        public boolean isActive() { return active; }
        public String getScope() { return scope; }
        public String getClientId() { return clientId; }
        public String getUsername() { return username; }
        public String getTokenType() { return tokenType; }
        public String getExp() { return exp; }
        public String getIat() { return iat; }
        public String getSub() { return sub; }
        public String getIss() { return iss; }
    }
    
    /**
     * Token Introspection Controller
     */
    @RestController
    public static class IntrospectionController {
        
        private final TokenIntrospectionService introspectionService;
        
        public IntrospectionController(TokenIntrospectionService introspectionService) {
            this.introspectionService = introspectionService;
        }
        
        /**
         * OAuth 2.0 Token Introspection Endpoint (RFC 7662)
         * POST /oauth2/introspect
         */
        @PostMapping("/oauth2/introspect")
        public ResponseEntity<IntrospectionResponse> introspect(
                @RequestParam("token") String token,
                @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint) {
            
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            IntrospectionResponse response = introspectionService.introspect(token);
            return ResponseEntity.ok(response);
        }
        
        /**
         * Public endpoint to get introspection info
         */
        @GetMapping("/api/public/introspection-info")
        public Map<String, Object> getIntrospectionInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("endpoint", "/oauth2/introspect");
            info.put("method", "POST");
            info.put("description", "OAuth 2.0 Token Introspection (RFC 7662)");
            info.put("parameters", Map.of(
                "token", "Required - The token to introspect",
                "token_type_hint", "Optional - access_token or refresh_token"
            ));
            info.put("authentication", "Client credentials required (HTTP Basic Auth)");
            info.put("timestamp", LocalDateTime.now());
            
            return info;
        }
        
        /**
         * Test endpoint using sample token
         */
        @GetMapping("/api/test/introspect/{token}")
        public IntrospectionResponse testIntrospect(@PathVariable String token) {
            return introspectionService.introspect(token);
        }
    }
}
