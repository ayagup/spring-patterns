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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token Refresh Pattern
 * 
 * Demonstrates OAuth 2.0 Refresh Token flow for obtaining new access tokens
 * without re-authentication.
 * 
 * Features:
 * - Refresh token grant flow
 * - Token rotation (optional)
 * - Refresh token validation
 * - New access token generation
 * - Refresh token expiration
 * - Token family tracking (for rotation)
 * 
 * Key Components:
 * - Refresh token endpoint
 * - Token rotation strategy
 * - Refresh token store
 * - Access token regeneration
 */
@SpringBootApplication
public class TokenRefreshPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(TokenRefreshPattern.class, args);
    }
    
    @Configuration
    @EnableWebSecurity
    public static class RefreshTokenSecurityConfig {
        
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                    .antMatchers("/oauth2/**", "/api/public/**").permitAll()
                    .anyRequest().authenticated();
            
            return http.build();
        }
    }
    
    /**
     * Token Pair
     * Represents access token and refresh token pair
     */
    public static class TokenPair {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private List<String> scopes;
        private LocalDateTime issuedAt;
        
        public TokenPair(String accessToken, String refreshToken, List<String> scopes) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.tokenType = "Bearer";
            this.expiresIn = 3600; // 1 hour
            this.scopes = scopes;
            this.issuedAt = LocalDateTime.now();
        }
        
        // Getters
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getTokenType() { return tokenType; }
        public long getExpiresIn() { return expiresIn; }
        public List<String> getScopes() { return scopes; }
        public LocalDateTime getIssuedAt() { return issuedAt; }
    }
    
    /**
     * Refresh Token Details
     */
    public static class RefreshTokenDetails {
        private String refreshToken;
        private String userId;
        private List<String> scopes;
        private LocalDateTime expiresAt;
        private boolean used;
        private String tokenFamily; // For rotation detection
        
        public RefreshTokenDetails(String refreshToken, String userId, 
                                  List<String> scopes, LocalDateTime expiresAt) {
            this.refreshToken = refreshToken;
            this.userId = userId;
            this.scopes = scopes;
            this.expiresAt = expiresAt;
            this.used = false;
            this.tokenFamily = UUID.randomUUID().toString();
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
        
        // Getters and setters
        public String getRefreshToken() { return refreshToken; }
        public String getUserId() { return userId; }
        public List<String> getScopes() { return scopes; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public boolean isUsed() { return used; }
        public void setUsed(boolean used) { this.used = used; }
        public String getTokenFamily() { return tokenFamily; }
        public void setTokenFamily(String tokenFamily) { this.tokenFamily = tokenFamily; }
    }
    
    /**
     * Refresh Token Store
     */
    @Service
    public static class RefreshTokenStore {
        
        private final Map<String, RefreshTokenDetails> refreshTokens = new ConcurrentHashMap<>();
        
        public RefreshTokenStore() {
            // Initialize with sample refresh token
            RefreshTokenDetails sampleToken = new RefreshTokenDetails(
                "sample-refresh-token-xyz",
                "user123",
                Arrays.asList("read", "write"),
                LocalDateTime.now().plusDays(30)
            );
            refreshTokens.put(sampleToken.getRefreshToken(), sampleToken);
        }
        
        public void save(RefreshTokenDetails details) {
            refreshTokens.put(details.getRefreshToken(), details);
        }
        
        public Optional<RefreshTokenDetails> findByToken(String token) {
            return Optional.ofNullable(refreshTokens.get(token));
        }
        
        public void revoke(String token) {
            refreshTokens.remove(token);
        }
        
        public void revokeFamily(String tokenFamily) {
            refreshTokens.values().removeIf(details -> 
                tokenFamily.equals(details.getTokenFamily())
            );
        }
    }
    
    /**
     * Token Refresh Service
     */
    @Service
    public static class TokenRefreshService {
        
        private final RefreshTokenStore refreshTokenStore;
        private final boolean enableRotation = true; // Token rotation enabled
        
        public TokenRefreshService(RefreshTokenStore refreshTokenStore) {
            this.refreshTokenStore = refreshTokenStore;
        }
        
        /**
         * Generate initial token pair
         */
        public TokenPair generateTokenPair(String userId, List<String> scopes) {
            String accessToken = "access-" + UUID.randomUUID().toString();
            String refreshToken = "refresh-" + UUID.randomUUID().toString();
            
            RefreshTokenDetails refreshDetails = new RefreshTokenDetails(
                refreshToken, userId, scopes, LocalDateTime.now().plusDays(30)
            );
            refreshTokenStore.save(refreshDetails);
            
            return new TokenPair(accessToken, refreshToken, scopes);
        }
        
        /**
         * Refresh access token using refresh token
         */
        public TokenPair refresh(String refreshToken) throws InvalidRefreshTokenException {
            Optional<RefreshTokenDetails> detailsOpt = refreshTokenStore.findByToken(refreshToken);
            
            if (detailsOpt.isEmpty()) {
                throw new InvalidRefreshTokenException("Invalid refresh token");
            }
            
            RefreshTokenDetails details = detailsOpt.get();
            
            // Check if already used (potential replay attack with rotation)
            if (enableRotation && details.isUsed()) {
                // Revoke entire token family for security
                refreshTokenStore.revokeFamily(details.getTokenFamily());
                throw new InvalidRefreshTokenException("Refresh token already used - family revoked");
            }
            
            // Check expiration
            if (details.isExpired()) {
                refreshTokenStore.revoke(refreshToken);
                throw new InvalidRefreshTokenException("Refresh token expired");
            }
            
            // Generate new access token
            String newAccessToken = "access-" + UUID.randomUUID().toString();
            String newRefreshToken = refreshToken; // Same refresh token
            
            // If rotation is enabled, generate new refresh token
            if (enableRotation) {
                newRefreshToken = "refresh-" + UUID.randomUUID().toString();
                
                // Mark old refresh token as used
                details.setUsed(true);
                
                // Create new refresh token in same family
                RefreshTokenDetails newDetails = new RefreshTokenDetails(
                    newRefreshToken, details.getUserId(), 
                    details.getScopes(), LocalDateTime.now().plusDays(30)
                );
                newDetails.setTokenFamily(details.getTokenFamily());
                refreshTokenStore.save(newDetails);
            }
            
            return new TokenPair(newAccessToken, newRefreshToken, details.getScopes());
        }
        
        /**
         * Revoke refresh token
         */
        public void revokeRefreshToken(String refreshToken) {
            refreshTokenStore.revoke(refreshToken);
        }
    }
    
    /**
     * Custom Exception
     */
    public static class InvalidRefreshTokenException extends Exception {
        public InvalidRefreshTokenException(String message) {
            super(message);
        }
    }
    
    /**
     * Token Refresh Controller
     */
    @RestController
    @RequestMapping("/oauth2")
    public static class TokenRefreshController {
        
        private final TokenRefreshService tokenRefreshService;
        
        public TokenRefreshController(TokenRefreshService tokenRefreshService) {
            this.tokenRefreshService = tokenRefreshService;
        }
        
        /**
         * OAuth 2.0 Token Endpoint - Refresh Token Grant
         * POST /oauth2/token
         */
        @PostMapping("/token")
        public ResponseEntity<?> token(
                @RequestParam("grant_type") String grantType,
                @RequestParam(value = "refresh_token", required = false) String refreshToken,
                @RequestParam(value = "scope", required = false) String scope) {
            
            if ("refresh_token".equals(grantType)) {
                if (refreshToken == null || refreshToken.isEmpty()) {
                    return ResponseEntity.badRequest().body(
                        Map.of("error", "invalid_request", 
                               "error_description", "refresh_token is required")
                    );
                }
                
                try {
                    TokenPair tokenPair = tokenRefreshService.refresh(refreshToken);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("access_token", tokenPair.getAccessToken());
                    response.put("refresh_token", tokenPair.getRefreshToken());
                    response.put("token_type", tokenPair.getTokenType());
                    response.put("expires_in", tokenPair.getExpiresIn());
                    response.put("scope", String.join(" ", tokenPair.getScopes()));
                    
                    return ResponseEntity.ok(response);
                    
                } catch (InvalidRefreshTokenException e) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("error", "invalid_grant", 
                               "error_description", e.getMessage())
                    );
                }
            }
            
            return ResponseEntity.badRequest().body(
                Map.of("error", "unsupported_grant_type",
                       "error_description", "Only refresh_token grant type is supported")
            );
        }
        
        /**
         * Revoke token endpoint
         */
        @PostMapping("/revoke")
        public ResponseEntity<?> revoke(@RequestParam("token") String token) {
            tokenRefreshService.revokeRefreshToken(token);
            return ResponseEntity.ok(Map.of("message", "Token revoked successfully"));
        }
        
        /**
         * Generate sample token pair (for testing)
         */
        @PostMapping("/test/generate")
        public ResponseEntity<TokenPair> generateTestToken(
                @RequestParam(value = "userId", defaultValue = "user123") String userId) {
            
            TokenPair tokenPair = tokenRefreshService.generateTokenPair(
                userId, Arrays.asList("read", "write")
            );
            
            return ResponseEntity.ok(tokenPair);
        }
    }
    
    @RestController
    @RequestMapping("/api/public")
    public static class PublicController {
        
        @GetMapping("/refresh-info")
        public Map<String, Object> getRefreshInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("endpoint", "/oauth2/token");
            info.put("method", "POST");
            info.put("grantType", "refresh_token");
            info.put("description", "OAuth 2.0 Refresh Token Grant");
            info.put("parameters", Map.of(
                "grant_type", "refresh_token (required)",
                "refresh_token", "The refresh token (required)",
                "scope", "Requested scope (optional)"
            ));
            info.put("rotationEnabled", true);
            info.put("tokenExpiry", Map.of(
                "accessToken", "1 hour",
                "refreshToken", "30 days"
            ));
            info.put("timestamp", LocalDateTime.now());
            
            return info;
        }
    }
}
