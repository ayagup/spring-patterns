package com.example.authorizationserver.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Refresh Token Grant Pattern
 * 
 * Demonstrates OAuth 2.1 Refresh Token flow for obtaining new access tokens
 * without requiring user re-authentication.
 * 
 * Key Components:
 * - Refresh token issuance with initial authorization
 * - Refresh token validation and rotation
 * - Access token renewal without user interaction
 * - Refresh token revocation
 * - Token binding and family tracking
 * 
 * Use Cases:
 * - Long-lived mobile applications
 * - Desktop applications with persistent sessions
 * - Web applications with background refresh
 * - API clients requiring continuous access
 * 
 * Security Considerations:
 * - Rotate refresh tokens on each use (recommended)
 * - Implement refresh token families for breach detection
 * - Set appropriate refresh token lifetime
 * - Revoke token families on suspicious activity
 * - Store refresh tokens securely (encrypted)
 */
@SpringBootApplication
public class RefreshTokenGrantPattern {

    public static void main(String[] args) {
        SpringApplication.run(RefreshTokenGrantPattern.class, args);
    }

    /**
     * OAuth2 Authorization Server Configuration
     */
    @Configuration
    @EnableWebSecurity
    public static class AuthorizationServerConfig {

        @Bean
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
            OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
            return http.formLogin(Customizer.withDefaults()).build();
        }

        @Bean
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/**").permitAll()
                    .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .build();
        }

        @Bean
        public RegisteredClientRepository registeredClientRepository() {
            // Client with refresh token rotation enabled
            RegisteredClient rotatingClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("web-client-rotating")
                .clientSecret("{noop}web-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/login/oauth2/code/web-client")
                .scope("read")
                .scope("write")
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(15))
                    .refreshTokenTimeToLive(Duration.ofDays(30))
                    .reuseRefreshTokens(false) // Rotate on each use
                    .build())
                .build();

            // Client with refresh token reuse
            RegisteredClient reusingClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("mobile-client-reuse")
                .clientSecret("{noop}mobile-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("myapp://callback")
                .scope("openid")
                .scope("profile")
                .scope("offline_access")
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(30))
                    .refreshTokenTimeToLive(Duration.ofDays(90))
                    .reuseRefreshTokens(true) // Reuse refresh token
                    .build())
                .build();

            return new InMemoryRegisteredClientRepository(rotatingClient, reusingClient);
        }

        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
            return new InMemoryUserDetailsManager(user);
        }
    }

    /**
     * Service to manage refresh token operations
     */
    @Service
    public static class RefreshTokenService {

        private final RegisteredClientRepository clientRepository;
        private final Map<String, RefreshTokenInfo> refreshTokenStore = new HashMap<>();
        private final Map<String, TokenFamily> tokenFamilies = new HashMap<>();
        private final Map<String, AccessTokenInfo> accessTokenStore = new HashMap<>();

        public RefreshTokenService(RegisteredClientRepository clientRepository) {
            this.clientRepository = clientRepository;
        }

        /**
         * Issue initial tokens (access + refresh)
         */
        public TokenPair issueInitialTokens(String clientId, String userId, Set<String> scopes) {
            RegisteredClient client = clientRepository.findByClientId(clientId);
            if (client == null) {
                throw new IllegalArgumentException("Invalid client");
            }

            String accessToken = UUID.randomUUID().toString();
            String refreshToken = UUID.randomUUID().toString();
            String familyId = UUID.randomUUID().toString();
            
            Instant now = Instant.now();
            Duration accessTokenTTL = client.getTokenSettings().getAccessTokenTimeToLive();
            Duration refreshTokenTTL = client.getTokenSettings().getRefreshTokenTimeToLive();

            // Store access token
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo(
                accessToken,
                clientId,
                userId,
                scopes,
                now,
                now.plus(accessTokenTTL)
            );
            accessTokenStore.put(accessToken, accessTokenInfo);

            // Store refresh token
            RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(
                refreshToken,
                clientId,
                userId,
                scopes,
                now,
                now.plus(refreshTokenTTL),
                familyId,
                1
            );
            refreshTokenStore.put(refreshToken, refreshTokenInfo);

            // Initialize token family
            TokenFamily family = new TokenFamily(familyId, clientId, userId, now);
            family.addToken(refreshToken, 1);
            tokenFamilies.put(familyId, family);

            return new TokenPair(
                accessToken,
                "Bearer",
                accessTokenTTL.toSeconds(),
                refreshToken,
                refreshTokenTTL.toSeconds(),
                scopes
            );
        }

        /**
         * Refresh access token using refresh token
         */
        public TokenPair refreshAccessToken(String clientId, String refreshToken) {
            RegisteredClient client = clientRepository.findByClientId(clientId);
            if (client == null) {
                throw new IllegalArgumentException("Invalid client");
            }

            RefreshTokenInfo refreshTokenInfo = refreshTokenStore.get(refreshToken);
            if (refreshTokenInfo == null) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            // Validate refresh token
            if (!refreshTokenInfo.clientId.equals(clientId)) {
                throw new IllegalArgumentException("Client mismatch");
            }

            if (Instant.now().isAfter(refreshTokenInfo.expiresAt)) {
                refreshTokenStore.remove(refreshToken);
                throw new IllegalArgumentException("Refresh token expired");
            }

            // Check for token reuse detection
            TokenFamily family = tokenFamilies.get(refreshTokenInfo.familyId);
            if (family != null && family.isTokenUsed(refreshToken)) {
                // Possible breach - revoke entire family
                revokeTokenFamily(refreshTokenInfo.familyId);
                throw new SecurityException("Token reuse detected - family revoked");
            }

            boolean reuseRefreshTokens = client.getTokenSettings().isReuseRefreshTokens();
            String newAccessToken = UUID.randomUUID().toString();
            String newRefreshToken = reuseRefreshTokens ? refreshToken : UUID.randomUUID().toString();
            
            Instant now = Instant.now();
            Duration accessTokenTTL = client.getTokenSettings().getAccessTokenTimeToLive();
            Duration refreshTokenTTL = client.getTokenSettings().getRefreshTokenTimeToLive();

            // Store new access token
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo(
                newAccessToken,
                clientId,
                refreshTokenInfo.userId,
                refreshTokenInfo.scopes,
                now,
                now.plus(accessTokenTTL)
            );
            accessTokenStore.put(newAccessToken, accessTokenInfo);

            // Handle refresh token rotation
            if (!reuseRefreshTokens) {
                // Mark old token as used
                if (family != null) {
                    family.markTokenAsUsed(refreshToken);
                }

                // Store new refresh token
                RefreshTokenInfo newRefreshTokenInfo = new RefreshTokenInfo(
                    newRefreshToken,
                    clientId,
                    refreshTokenInfo.userId,
                    refreshTokenInfo.scopes,
                    now,
                    now.plus(refreshTokenTTL),
                    refreshTokenInfo.familyId,
                    refreshTokenInfo.generation + 1
                );
                refreshTokenStore.put(newRefreshToken, newRefreshTokenInfo);

                // Add to family
                if (family != null) {
                    family.addToken(newRefreshToken, newRefreshTokenInfo.generation);
                }

                // Remove old refresh token
                refreshTokenStore.remove(refreshToken);
            }

            return new TokenPair(
                newAccessToken,
                "Bearer",
                accessTokenTTL.toSeconds(),
                newRefreshToken,
                refreshTokenTTL.toSeconds(),
                refreshTokenInfo.scopes
            );
        }

        /**
         * Revoke refresh token
         */
        public void revokeRefreshToken(String refreshToken) {
            RefreshTokenInfo info = refreshTokenStore.remove(refreshToken);
            if (info != null) {
                TokenFamily family = tokenFamilies.get(info.familyId);
                if (family != null) {
                    family.removeToken(refreshToken);
                }
            }
        }

        /**
         * Revoke entire token family (on breach detection)
         */
        public void revokeTokenFamily(String familyId) {
            TokenFamily family = tokenFamilies.get(familyId);
            if (family != null) {
                for (String token : family.getAllTokens()) {
                    refreshTokenStore.remove(token);
                }
                tokenFamilies.remove(familyId);
            }
        }

        /**
         * Get refresh token statistics
         */
        public RefreshTokenStatistics getStatistics() {
            long activeTokens = refreshTokenStore.values().stream()
                .filter(token -> Instant.now().isBefore(token.expiresAt))
                .count();
            
            return new RefreshTokenStatistics(
                refreshTokenStore.size(),
                activeTokens,
                tokenFamilies.size(),
                refreshTokenStore.size() - activeTokens
            );
        }

        /**
         * Get token family info
         */
        public TokenFamilyInfo getTokenFamilyInfo(String refreshToken) {
            RefreshTokenInfo info = refreshTokenStore.get(refreshToken);
            if (info == null) {
                return null;
            }

            TokenFamily family = tokenFamilies.get(info.familyId);
            if (family == null) {
                return null;
            }

            return new TokenFamilyInfo(
                family.familyId,
                family.clientId,
                family.userId,
                family.createdAt,
                family.getAllTokens().size(),
                info.generation
            );
        }
    }

    /**
     * REST Controller for Refresh Token operations
     */
    @RestController
    @RequestMapping("/api/refresh-token")
    public static class RefreshTokenController {

        private final RefreshTokenService refreshTokenService;

        public RefreshTokenController(RefreshTokenService refreshTokenService) {
            this.refreshTokenService = refreshTokenService;
        }

        /**
         * Issue initial token pair
         */
        @PostMapping("/issue")
        public TokenPairResponse issueTokens(@RequestBody IssueTokenRequest request) {
            TokenPair tokens = refreshTokenService.issueInitialTokens(
                request.clientId,
                request.userId,
                request.scopes
            );
            return toResponse(tokens);
        }

        /**
         * Refresh access token
         */
        @PostMapping("/refresh")
        public TokenPairResponse refreshToken(@RequestBody RefreshRequest request) {
            TokenPair tokens = refreshTokenService.refreshAccessToken(
                request.clientId,
                request.refreshToken
            );
            return toResponse(tokens);
        }

        /**
         * Revoke refresh token
         */
        @PostMapping("/revoke")
        public RevokeResponse revokeToken(@RequestBody RevokeRequest request) {
            refreshTokenService.revokeRefreshToken(request.refreshToken);
            return new RevokeResponse(true, "Refresh token revoked successfully");
        }

        /**
         * Get token family information
         */
        @GetMapping("/family/{refreshToken}")
        public TokenFamilyInfo getTokenFamily(@PathVariable String refreshToken) {
            return refreshTokenService.getTokenFamilyInfo(refreshToken);
        }

        /**
         * Get refresh token statistics
         */
        @GetMapping("/statistics")
        public RefreshTokenStatistics getStatistics() {
            return refreshTokenService.getStatistics();
        }

        /**
         * Get pattern information
         */
        @GetMapping("/info")
        public PatternInfo getInfo() {
            return new PatternInfo(
                "Refresh Token Grant Pattern",
                "OAuth 2.1 Refresh Token flow for token renewal without user re-authentication",
                List.of(
                    "POST /api/refresh-token/issue - Issue initial token pair",
                    "POST /api/refresh-token/refresh - Refresh access token",
                    "POST /api/refresh-token/revoke - Revoke refresh token",
                    "GET /api/refresh-token/family/{token} - Get token family info",
                    "GET /api/refresh-token/statistics - Get statistics",
                    "GET /api/refresh-token/info - Get pattern information"
                ),
                Map.of(
                    "grant_type", "refresh_token",
                    "rotation_strategies", List.of("rotate_on_use", "reuse_token"),
                    "security_features", List.of("Token families", "Breach detection", "Automatic revocation")
                )
            );
        }

        private TokenPairResponse toResponse(TokenPair tokens) {
            return new TokenPairResponse(
                tokens.accessToken,
                tokens.tokenType,
                tokens.accessTokenExpiresIn,
                tokens.refreshToken,
                tokens.refreshTokenExpiresIn,
                tokens.scopes
            );
        }
    }

    // DTOs
    public record IssueTokenRequest(String clientId, String userId, Set<String> scopes) {}
    public record RefreshRequest(String clientId, String refreshToken) {}
    public record RevokeRequest(String refreshToken) {}
    public record RevokeResponse(boolean success, String message) {}
    public record TokenPairResponse(String accessToken, String tokenType, long expiresIn,
                                   String refreshToken, long refreshTokenExpiresIn, Set<String> scopes) {}
    public record PatternInfo(String name, String description, List<String> endpoints, Map<String, Object> details) {}

    // Domain Objects
    public static class TokenPair {
        public final String accessToken;
        public final String tokenType;
        public final long accessTokenExpiresIn;
        public final String refreshToken;
        public final long refreshTokenExpiresIn;
        public final Set<String> scopes;

        public TokenPair(String accessToken, String tokenType, long accessTokenExpiresIn,
                        String refreshToken, long refreshTokenExpiresIn, Set<String> scopes) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.accessTokenExpiresIn = accessTokenExpiresIn;
            this.refreshToken = refreshToken;
            this.refreshTokenExpiresIn = refreshTokenExpiresIn;
            this.scopes = scopes;
        }
    }

    public record AccessTokenInfo(String token, String clientId, String userId, Set<String> scopes,
                                 Instant issuedAt, Instant expiresAt) {}

    public static class RefreshTokenInfo {
        public final String token;
        public final String clientId;
        public final String userId;
        public final Set<String> scopes;
        public final Instant issuedAt;
        public final Instant expiresAt;
        public final String familyId;
        public final int generation;

        public RefreshTokenInfo(String token, String clientId, String userId, Set<String> scopes,
                               Instant issuedAt, Instant expiresAt, String familyId, int generation) {
            this.token = token;
            this.clientId = clientId;
            this.userId = userId;
            this.scopes = scopes;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
            this.familyId = familyId;
            this.generation = generation;
        }
    }

    public static class TokenFamily {
        public final String familyId;
        public final String clientId;
        public final String userId;
        public final Instant createdAt;
        private final Map<String, Integer> tokens = new HashMap<>();
        private final Set<String> usedTokens = new HashSet<>();

        public TokenFamily(String familyId, String clientId, String userId, Instant createdAt) {
            this.familyId = familyId;
            this.clientId = clientId;
            this.userId = userId;
            this.createdAt = createdAt;
        }

        public void addToken(String token, int generation) {
            tokens.put(token, generation);
        }

        public void removeToken(String token) {
            tokens.remove(token);
        }

        public void markTokenAsUsed(String token) {
            usedTokens.add(token);
        }

        public boolean isTokenUsed(String token) {
            return usedTokens.contains(token);
        }

        public Set<String> getAllTokens() {
            return new HashSet<>(tokens.keySet());
        }
    }

    public record RefreshTokenStatistics(long totalTokens, long activeTokens, long families, long expiredTokens) {}
    public record TokenFamilyInfo(String familyId, String clientId, String userId, 
                                 Instant createdAt, int totalTokens, int currentGeneration) {}
}
