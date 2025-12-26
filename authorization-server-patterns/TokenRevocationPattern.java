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
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Token Revocation Pattern - RFC 7009
 * 
 * Demonstrates OAuth 2.0 Token Revocation endpoint for invalidating access tokens
 * and refresh tokens when they are no longer needed.
 * 
 * Key Components:
 * - Revocation endpoint (/oauth2/revoke)
 * - Token type hint support (access_token, refresh_token)
 * - Client authentication
 * - Cascading revocation (revoking refresh token revokes access tokens)
 * - Token family revocation
 * - Revocation audit logging
 * 
 * Use Cases:
 * - User logout (revoke all tokens)
 * - Client uninstallation
 * - Security breach response
 * - Session termination
 * - Token lifecycle management
 * - Compliance requirements (GDPR, etc.)
 * 
 * Security Considerations:
 * - Require client authentication
 * - Validate token ownership
 * - Rate limit revocation requests
 * - Log all revocations for audit
 * - Cascade revocation appropriately
 * - Return 200 OK even for invalid tokens (RFC 7009)
 */
@SpringBootApplication
public class TokenRevocationPattern {

    public static void main(String[] args) {
        SpringApplication.run(TokenRevocationPattern.class, args);
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
            RegisteredClient client1 = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("web-client")
                .clientSecret("{noop}web-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/callback")
                .scope("read")
                .scope("write")
                .build();

            RegisteredClient client2 = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("mobile-app")
                .clientSecret("{noop}mobile-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("myapp://callback")
                .scope("openid")
                .scope("profile")
                .build();

            return new InMemoryRegisteredClientRepository(client1, client2);
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
     * Service to manage token revocation
     */
    @Service
    public static class TokenRevocationService {

        private final RegisteredClientRepository clientRepository;
        private final Map<String, TokenInfo> accessTokens = new HashMap<>();
        private final Map<String, RefreshTokenInfo> refreshTokens = new HashMap<>();
        private final Map<String, TokenFamily> tokenFamilies = new HashMap<>();
        private final List<RevocationEvent> revocationLog = new ArrayList<>();

        public TokenRevocationService(RegisteredClientRepository clientRepository) {
            this.clientRepository = clientRepository;
            initializeSampleTokens();
        }

        /**
         * Revoke token (RFC 7009 compliant)
         * Returns 200 OK regardless of token validity (per RFC)
         */
        public RevocationResult revokeToken(String token, String tokenTypeHint, String clientId) {
            RegisteredClient client = clientRepository.findByClientId(clientId);
            if (client == null) {
                // Per RFC 7009, still return success to avoid token scanning
                logRevocation(token, tokenTypeHint, clientId, "unauthorized_client", false);
                return new RevocationResult(true, "Token revoked", 0);
            }

            int revokedCount = 0;

            // Try as refresh token first if hinted
            if ("refresh_token".equals(tokenTypeHint) || tokenTypeHint == null) {
                RefreshTokenInfo refreshToken = refreshTokens.get(token);
                if (refreshToken != null && refreshToken.clientId.equals(clientId)) {
                    revokedCount += revokeRefreshTokenAndFamily(token, clientId);
                    logRevocation(token, "refresh_token", clientId, "success", true);
                    return new RevocationResult(true, "Refresh token and family revoked", revokedCount);
                }
            }

            // Try as access token
            if ("access_token".equals(tokenTypeHint) || tokenTypeHint == null) {
                TokenInfo accessToken = accessTokens.get(token);
                if (accessToken != null && accessToken.clientId.equals(clientId)) {
                    accessTokens.remove(token);
                    revokedCount++;
                    logRevocation(token, "access_token", clientId, "success", true);
                    return new RevocationResult(true, "Access token revoked", revokedCount);
                }
            }

            // Token not found or not owned by client
            // Per RFC 7009, still return success
            logRevocation(token, tokenTypeHint, clientId, "not_found", false);
            return new RevocationResult(true, "Token revoked", 0);
        }

        /**
         * Revoke all tokens for a user
         */
        public BulkRevocationResult revokeAllUserTokens(String userId, String clientId) {
            int accessCount = 0;
            int refreshCount = 0;

            // Revoke all access tokens
            Iterator<Map.Entry<String, TokenInfo>> accessIterator = accessTokens.entrySet().iterator();
            while (accessIterator.hasNext()) {
                Map.Entry<String, TokenInfo> entry = accessIterator.next();
                if (entry.getValue().userId.equals(userId) && 
                    entry.getValue().clientId.equals(clientId)) {
                    accessIterator.remove();
                    accessCount++;
                }
            }

            // Revoke all refresh tokens
            Iterator<Map.Entry<String, RefreshTokenInfo>> refreshIterator = refreshTokens.entrySet().iterator();
            while (refreshIterator.hasNext()) {
                Map.Entry<String, RefreshTokenInfo> entry = refreshIterator.next();
                if (entry.getValue().userId.equals(userId) && 
                    entry.getValue().clientId.equals(clientId)) {
                    refreshIterator.remove();
                    refreshCount++;
                }
            }

            logRevocation("bulk_user_" + userId, "all", clientId, "success", true);
            return new BulkRevocationResult(true, accessCount + refreshCount, accessCount, refreshCount);
        }

        /**
         * Revoke all tokens for a client
         */
        public BulkRevocationResult revokeAllClientTokens(String clientId) {
            int accessCount = 0;
            int refreshCount = 0;

            // Revoke all access tokens
            Iterator<Map.Entry<String, TokenInfo>> accessIterator = accessTokens.entrySet().iterator();
            while (accessIterator.hasNext()) {
                Map.Entry<String, TokenInfo> entry = accessIterator.next();
                if (entry.getValue().clientId.equals(clientId)) {
                    accessIterator.remove();
                    accessCount++;
                }
            }

            // Revoke all refresh tokens
            Iterator<Map.Entry<String, RefreshTokenInfo>> refreshIterator = refreshTokens.entrySet().iterator();
            while (refreshIterator.hasNext()) {
                Map.Entry<String, RefreshTokenInfo> entry = refreshIterator.next();
                if (entry.getValue().clientId.equals(clientId)) {
                    refreshIterator.remove();
                    refreshCount++;
                }
            }

            logRevocation("bulk_client", "all", clientId, "success", true);
            return new BulkRevocationResult(true, accessCount + refreshCount, accessCount, refreshCount);
        }

        /**
         * Revoke token family (all related tokens)
         */
        public BulkRevocationResult revokeTokenFamily(String familyId, String clientId) {
            TokenFamily family = tokenFamilies.get(familyId);
            if (family == null || !family.clientId.equals(clientId)) {
                return new BulkRevocationResult(false, 0, 0, 0);
            }

            int count = 0;
            for (String token : family.tokens) {
                if (refreshTokens.remove(token) != null) {
                    count++;
                }
            }

            tokenFamilies.remove(familyId);
            logRevocation("family_" + familyId, "family", clientId, "success", true);
            return new BulkRevocationResult(true, count, 0, count);
        }

        /**
         * Get revocation history
         */
        public List<RevocationEvent> getRevocationHistory(int limit) {
            return revocationLog.stream()
                .limit(limit)
                .toList();
        }

        /**
         * Get revocation statistics
         */
        public RevocationStatistics getStatistics() {
            long successful = revocationLog.stream().filter(e -> e.successful).count();
            long accessRevoked = revocationLog.stream()
                .filter(e -> "access_token".equals(e.tokenType) && e.successful)
                .count();
            long refreshRevoked = revocationLog.stream()
                .filter(e -> "refresh_token".equals(e.tokenType) && e.successful)
                .count();
            
            return new RevocationStatistics(
                revocationLog.size(),
                successful,
                accessRevoked,
                refreshRevoked,
                accessTokens.size(),
                refreshTokens.size()
            );
        }

        /**
         * Get tokens by user
         */
        public UserTokensInfo getUserTokens(String userId, String clientId) {
            long accessCount = accessTokens.values().stream()
                .filter(t -> t.userId.equals(userId) && t.clientId.equals(clientId))
                .count();
            
            long refreshCount = refreshTokens.values().stream()
                .filter(t -> t.userId.equals(userId) && t.clientId.equals(clientId))
                .count();

            return new UserTokensInfo(userId, accessCount, refreshCount);
        }

        private int revokeRefreshTokenAndFamily(String refreshToken, String clientId) {
            int count = 0;
            RefreshTokenInfo info = refreshTokens.remove(refreshToken);
            if (info != null) {
                count++;
                
                // Revoke entire family
                if (info.familyId != null) {
                    TokenFamily family = tokenFamilies.get(info.familyId);
                    if (family != null && family.clientId.equals(clientId)) {
                        for (String token : family.tokens) {
                            if (refreshTokens.remove(token) != null) {
                                count++;
                            }
                        }
                        tokenFamilies.remove(info.familyId);
                    }
                }

                // Revoke associated access tokens
                accessTokens.entrySet().removeIf(entry -> 
                    entry.getValue().refreshToken != null && 
                    entry.getValue().refreshToken.equals(refreshToken));
            }
            return count;
        }

        private void logRevocation(String token, String tokenType, String clientId, 
                                   String reason, boolean successful) {
            revocationLog.add(new RevocationEvent(
                UUID.randomUUID().toString(),
                token.length() > 10 ? token.substring(0, 10) + "..." : token,
                tokenType,
                clientId,
                Instant.now(),
                reason,
                successful
            ));
        }

        private void initializeSampleTokens() {
            // Sample access token
            String accessToken1 = "access-" + UUID.randomUUID();
            accessTokens.put(accessToken1, new TokenInfo(
                accessToken1, "web-client", "user1", Set.of("read", "write"), null
            ));

            // Sample refresh token with family
            String familyId = UUID.randomUUID().toString();
            String refreshToken1 = "refresh-" + UUID.randomUUID();
            refreshTokens.put(refreshToken1, new RefreshTokenInfo(
                refreshToken1, "web-client", "user1", Set.of("read", "write"), familyId
            ));

            TokenFamily family = new TokenFamily(familyId, "web-client", "user1");
            family.tokens.add(refreshToken1);
            tokenFamilies.put(familyId, family);
        }
    }

    /**
     * REST Controller for Token Revocation operations
     */
    @RestController
    @RequestMapping("/api/revocation")
    public static class TokenRevocationController {

        private final TokenRevocationService revocationService;

        public TokenRevocationController(TokenRevocationService revocationService) {
            this.revocationService = revocationService;
        }

        /**
         * Revoke token (RFC 7009 compliant)
         */
        @PostMapping("/revoke")
        public RevocationResponse revokeToken(@RequestBody RevocationRequest request) {
            RevocationResult result = revocationService.revokeToken(
                request.token,
                request.tokenTypeHint,
                request.clientId
            );
            return new RevocationResponse(result.success, result.message, result.revokedCount);
        }

        /**
         * Revoke all tokens for a user
         */
        @PostMapping("/revoke-user")
        public BulkRevocationResponse revokeUserTokens(@RequestBody RevokeUserRequest request) {
            BulkRevocationResult result = revocationService.revokeAllUserTokens(
                request.userId,
                request.clientId
            );
            return new BulkRevocationResponse(
                result.success,
                result.totalRevoked,
                result.accessTokensRevoked,
                result.refreshTokensRevoked
            );
        }

        /**
         * Revoke all tokens for a client
         */
        @PostMapping("/revoke-client")
        public BulkRevocationResponse revokeClientTokens(@RequestBody RevokeClientRequest request) {
            BulkRevocationResult result = revocationService.revokeAllClientTokens(request.clientId);
            return new BulkRevocationResponse(
                result.success,
                result.totalRevoked,
                result.accessTokensRevoked,
                result.refreshTokensRevoked
            );
        }

        /**
         * Revoke token family
         */
        @PostMapping("/revoke-family")
        public BulkRevocationResponse revokeFamily(@RequestBody RevokeFamilyRequest request) {
            BulkRevocationResult result = revocationService.revokeTokenFamily(
                request.familyId,
                request.clientId
            );
            return new BulkRevocationResponse(
                result.success,
                result.totalRevoked,
                result.accessTokensRevoked,
                result.refreshTokensRevoked
            );
        }

        /**
         * Get revocation history
         */
        @GetMapping("/history")
        public RevocationHistoryResponse getHistory(@RequestParam(defaultValue = "100") int limit) {
            return new RevocationHistoryResponse(revocationService.getRevocationHistory(limit));
        }

        /**
         * Get revocation statistics
         */
        @GetMapping("/statistics")
        public RevocationStatistics getStatistics() {
            return revocationService.getStatistics();
        }

        /**
         * Get user tokens info
         */
        @GetMapping("/user-tokens/{userId}")
        public UserTokensInfo getUserTokens(
                @PathVariable String userId,
                @RequestParam String clientId) {
            return revocationService.getUserTokens(userId, clientId);
        }

        /**
         * Get pattern information
         */
        @GetMapping("/info")
        public PatternInfo getInfo() {
            return new PatternInfo(
                "Token Revocation Pattern - RFC 7009",
                "OAuth 2.0 Token Revocation endpoint for invalidating access and refresh tokens",
                List.of(
                    "POST /api/revocation/revoke - Revoke token",
                    "POST /api/revocation/revoke-user - Revoke all user tokens",
                    "POST /api/revocation/revoke-client - Revoke all client tokens",
                    "POST /api/revocation/revoke-family - Revoke token family",
                    "GET /api/revocation/history - Get revocation history",
                    "GET /api/revocation/statistics - Get statistics",
                    "GET /api/revocation/user-tokens/{userId} - Get user tokens info",
                    "GET /api/revocation/info - Get pattern information"
                ),
                Map.of(
                    "rfc", "7009",
                    "endpoint", "/oauth2/revoke",
                    "token_types", List.of("access_token", "refresh_token"),
                    "features", List.of("Cascading revocation", "Token families", "Audit logging"),
                    "use_cases", List.of("Logout", "Security breach", "Session termination")
                )
            );
        }
    }

    // DTOs
    public record RevocationRequest(String token, String tokenTypeHint, String clientId) {}
    public record RevokeUserRequest(String userId, String clientId) {}
    public record RevokeClientRequest(String clientId) {}
    public record RevokeFamilyRequest(String familyId, String clientId) {}
    public record RevocationResponse(boolean success, String message, int revokedCount) {}
    public record BulkRevocationResponse(boolean success, int totalRevoked, 
                                        int accessTokensRevoked, int refreshTokensRevoked) {}
    public record RevocationHistoryResponse(List<RevocationEvent> events) {}
    public record PatternInfo(String name, String description, List<String> endpoints, Map<String, Object> details) {}

    // Domain Objects
    public record RevocationResult(boolean success, String message, int revokedCount) {}
    public record BulkRevocationResult(boolean success, int totalRevoked, 
                                      int accessTokensRevoked, int refreshTokensRevoked) {}

    public record TokenInfo(String token, String clientId, String userId, Set<String> scopes, String refreshToken) {}
    
    public record RefreshTokenInfo(String token, String clientId, String userId, 
                                  Set<String> scopes, String familyId) {}

    public static class TokenFamily {
        public final String familyId;
        public final String clientId;
        public final String userId;
        public final Set<String> tokens = new HashSet<>();

        public TokenFamily(String familyId, String clientId, String userId) {
            this.familyId = familyId;
            this.clientId = clientId;
            this.userId = userId;
        }
    }

    public record RevocationEvent(String id, String tokenHint, String tokenType, String clientId,
                                 Instant timestamp, String reason, boolean successful) {}

    public record RevocationStatistics(long totalRevocations, long successful, long accessTokens,
                                      long refreshTokens, long activeAccessTokens, long activeRefreshTokens) {}

    public record UserTokensInfo(String userId, long accessTokens, long refreshTokens) {}
}
