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

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Token Introspection Pattern - RFC 7662
 * 
 * Demonstrates OAuth 2.0 Token Introspection endpoint for validating access tokens
 * and obtaining metadata about them.
 * 
 * Key Components:
 * - Introspection endpoint (/oauth2/introspect)
 * - Token validation and lookup
 * - Active/inactive status determination
 * - Token metadata retrieval (scopes, expiration, client, etc.)
 * - Resource server integration
 * 
 * Response Fields:
 * - active: Boolean indicating token validity
 * - scope: Space-separated list of scopes
 * - client_id: Client identifier
 * - username: Resource owner identifier
 * - token_type: Token type (Bearer)
 * - exp: Expiration timestamp
 * - iat: Issued at timestamp
 * - sub: Subject identifier
 * - aud: Audience
 * - iss: Issuer
 * 
 * Use Cases:
 * - Resource servers validating access tokens
 * - Token validation without JWT parsing
 * - Opaque token validation
 * - Centralized token validation
 * - Token metadata retrieval
 * 
 * Security Considerations:
 * - Require client authentication for introspection
 * - Rate limit introspection requests
 * - Cache introspection results
 * - Only return metadata for active tokens
 * - Protect introspection endpoint
 */
@SpringBootApplication
public class TokenIntrospectionPattern {

    public static void main(String[] args) {
        SpringApplication.run(TokenIntrospectionPattern.class, args);
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
            // Resource server client for introspection
            RegisteredClient resourceServer = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("resource-server")
                .clientSecret("{noop}resource-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("introspect")
                .build();

            // API Gateway client
            RegisteredClient apiGateway = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("api-gateway")
                .clientSecret("{noop}gateway-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("introspect")
                .build();

            return new InMemoryRegisteredClientRepository(resourceServer, apiGateway);
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
     * Service to manage token introspection
     */
    @Service
    public static class TokenIntrospectionService {

        private final RegisteredClientRepository clientRepository;
        private final Map<String, TokenMetadata> tokenStore = new HashMap<>();
        private final Map<String, IntrospectionCache> cacheStore = new HashMap<>();
        private final Map<String, IntrospectionStatistics> statsStore = new HashMap<>();

        public TokenIntrospectionService(RegisteredClientRepository clientRepository) {
            this.clientRepository = clientRepository;
            initializeSampleTokens();
        }

        /**
         * Introspect token (RFC 7662)
         */
        public IntrospectionResponse introspectToken(String token, String clientId) {
            // Validate client
            RegisteredClient client = clientRepository.findByClientId(clientId);
            if (client == null || !hasIntrospectionScope(client)) {
                updateStatistics(clientId, "unauthorized");
                throw new SecurityException("Client not authorized for introspection");
            }

            updateStatistics(clientId, "request");

            // Check cache
            IntrospectionCache cached = cacheStore.get(token);
            if (cached != null && Instant.now().isBefore(cached.expiresAt)) {
                updateStatistics(clientId, "cache_hit");
                return cached.response;
            }

            // Lookup token
            TokenMetadata metadata = tokenStore.get(token);
            if (metadata == null) {
                updateStatistics(clientId, "not_found");
                return new IntrospectionResponse(false, null, null, null, null, null, 
                    null, null, null, null, null);
            }

            // Determine if active
            boolean active = Instant.now().isBefore(metadata.expiresAt);
            
            if (!active) {
                updateStatistics(clientId, "expired");
                return new IntrospectionResponse(false, null, null, null, null, null,
                    null, null, null, null, null);
            }

            updateStatistics(clientId, "active");

            // Build response with metadata
            IntrospectionResponse response = new IntrospectionResponse(
                true,
                String.join(" ", metadata.scopes),
                metadata.clientId,
                metadata.username,
                metadata.tokenType,
                metadata.expiresAt.getEpochSecond(),
                metadata.issuedAt.getEpochSecond(),
                metadata.subject,
                metadata.audience,
                metadata.issuer,
                metadata.jti
            );

            // Cache result
            cacheStore.put(token, new IntrospectionCache(response, Instant.now().plusSeconds(60)));

            return response;
        }

        /**
         * Introspect token with hints
         */
        public IntrospectionResponse introspectTokenWithHint(String token, String clientId, String tokenTypeHint) {
            // Token type hint can be "access_token" or "refresh_token"
            // In production, this would optimize token lookup
            updateStatistics(clientId, "hint_used");
            return introspectToken(token, clientId);
        }

        /**
         * Batch introspection (non-standard extension)
         */
        public List<BatchIntrospectionResult> batchIntrospect(List<String> tokens, String clientId) {
            List<BatchIntrospectionResult> results = new ArrayList<>();
            
            for (String token : tokens) {
                try {
                    IntrospectionResponse response = introspectToken(token, clientId);
                    results.add(new BatchIntrospectionResult(token, response, null));
                } catch (Exception e) {
                    results.add(new BatchIntrospectionResult(token, null, e.getMessage()));
                }
            }
            
            updateStatistics(clientId, "batch_request");
            return results;
        }

        /**
         * Get introspection statistics
         */
        public Map<String, IntrospectionStatistics> getStatistics() {
            return new HashMap<>(statsStore);
        }

        /**
         * Get cache statistics
         */
        public CacheStatistics getCacheStatistics() {
            long totalCached = cacheStore.size();
            long activeCached = cacheStore.values().stream()
                .filter(cache -> Instant.now().isBefore(cache.expiresAt))
                .count();
            
            return new CacheStatistics(totalCached, activeCached, totalCached - activeCached);
        }

        /**
         * Clear introspection cache
         */
        public void clearCache() {
            cacheStore.clear();
        }

        /**
         * Clear expired cache entries
         */
        public void clearExpiredCache() {
            Instant now = Instant.now();
            cacheStore.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt));
        }

        private boolean hasIntrospectionScope(RegisteredClient client) {
            return client.getScopes().contains("introspect");
        }

        private void updateStatistics(String clientId, String event) {
            IntrospectionStatistics stats = statsStore.computeIfAbsent(
                clientId, k -> new IntrospectionStatistics()
            );
            
            switch (event) {
                case "request":
                    stats.totalRequests++;
                    break;
                case "active":
                    stats.activeTokens++;
                    break;
                case "expired":
                    stats.expiredTokens++;
                    break;
                case "not_found":
                    stats.notFoundTokens++;
                    break;
                case "cache_hit":
                    stats.cacheHits++;
                    break;
                case "unauthorized":
                    stats.unauthorizedRequests++;
                    break;
                case "hint_used":
                    stats.hintsUsed++;
                    break;
                case "batch_request":
                    stats.batchRequests++;
                    break;
            }
        }

        private void initializeSampleTokens() {
            // Sample active token
            String activeToken = "active-token-" + UUID.randomUUID();
            tokenStore.put(activeToken, new TokenMetadata(
                activeToken,
                "Bearer",
                "web-client",
                "john.doe",
                "john.doe",
                Set.of("read", "write"),
                Instant.now(),
                Instant.now().plus(Duration.ofHours(1)),
                "https://auth-server.example.com",
                "https://api.example.com",
                UUID.randomUUID().toString()
            ));

            // Sample expired token
            String expiredToken = "expired-token-" + UUID.randomUUID();
            tokenStore.put(expiredToken, new TokenMetadata(
                expiredToken,
                "Bearer",
                "mobile-app",
                "jane.smith",
                "jane.smith",
                Set.of("read"),
                Instant.now().minus(Duration.ofHours(2)),
                Instant.now().minus(Duration.ofHours(1)),
                "https://auth-server.example.com",
                "https://api.example.com",
                UUID.randomUUID().toString()
            ));
        }
    }

    /**
     * REST Controller for Token Introspection operations
     */
    @RestController
    @RequestMapping("/api/introspection")
    public static class TokenIntrospectionController {

        private final TokenIntrospectionService introspectionService;

        public TokenIntrospectionController(TokenIntrospectionService introspectionService) {
            this.introspectionService = introspectionService;
        }

        /**
         * Introspect token (RFC 7662 compliant)
         */
        @PostMapping("/introspect")
        public IntrospectionResponse introspect(@RequestBody IntrospectionRequest request) {
            return introspectionService.introspectToken(request.token, request.clientId);
        }

        /**
         * Introspect token with type hint
         */
        @PostMapping("/introspect-hint")
        public IntrospectionResponse introspectWithHint(@RequestBody IntrospectionHintRequest request) {
            return introspectionService.introspectTokenWithHint(
                request.token,
                request.clientId,
                request.tokenTypeHint
            );
        }

        /**
         * Batch introspection (non-standard)
         */
        @PostMapping("/batch-introspect")
        public BatchIntrospectionResponse batchIntrospect(@RequestBody BatchIntrospectionRequest request) {
            List<BatchIntrospectionResult> results = introspectionService.batchIntrospect(
                request.tokens,
                request.clientId
            );
            return new BatchIntrospectionResponse(results);
        }

        /**
         * Get introspection statistics
         */
        @GetMapping("/statistics")
        public Map<String, IntrospectionStatistics> getStatistics() {
            return introspectionService.getStatistics();
        }

        /**
         * Get cache statistics
         */
        @GetMapping("/cache-statistics")
        public CacheStatistics getCacheStatistics() {
            return introspectionService.getCacheStatistics();
        }

        /**
         * Clear introspection cache
         */
        @PostMapping("/clear-cache")
        public ClearCacheResponse clearCache() {
            introspectionService.clearCache();
            return new ClearCacheResponse(true, "Cache cleared successfully");
        }

        /**
         * Clear expired cache entries
         */
        @PostMapping("/clear-expired-cache")
        public ClearCacheResponse clearExpiredCache() {
            introspectionService.clearExpiredCache();
            return new ClearCacheResponse(true, "Expired cache entries cleared");
        }

        /**
         * Get pattern information
         */
        @GetMapping("/info")
        public PatternInfo getInfo() {
            return new PatternInfo(
                "Token Introspection Pattern - RFC 7662",
                "OAuth 2.0 Token Introspection endpoint for token validation and metadata retrieval",
                List.of(
                    "POST /api/introspection/introspect - Introspect token",
                    "POST /api/introspection/introspect-hint - Introspect with type hint",
                    "POST /api/introspection/batch-introspect - Batch introspection",
                    "GET /api/introspection/statistics - Get statistics",
                    "GET /api/introspection/cache-statistics - Get cache statistics",
                    "POST /api/introspection/clear-cache - Clear cache",
                    "POST /api/introspection/clear-expired-cache - Clear expired cache",
                    "GET /api/introspection/info - Get pattern information"
                ),
                Map.of(
                    "rfc", "7662",
                    "endpoint", "/oauth2/introspect",
                    "authentication", "Required (client credentials)",
                    "use_cases", List.of("Resource server validation", "Opaque token validation", "Token metadata retrieval")
                )
            );
        }
    }

    // DTOs
    public record IntrospectionRequest(String token, String clientId) {}
    public record IntrospectionHintRequest(String token, String clientId, String tokenTypeHint) {}
    public record BatchIntrospectionRequest(List<String> tokens, String clientId) {}
    public record BatchIntrospectionResponse(List<BatchIntrospectionResult> results) {}
    public record ClearCacheResponse(boolean success, String message) {}
    public record PatternInfo(String name, String description, List<String> endpoints, Map<String, Object> details) {}

    // Domain Objects
    public record IntrospectionResponse(
        boolean active,
        String scope,
        String clientId,
        String username,
        String tokenType,
        Long exp,
        Long iat,
        String sub,
        String aud,
        String iss,
        String jti
    ) {}

    public record BatchIntrospectionResult(String token, IntrospectionResponse response, String error) {}

    public static class TokenMetadata {
        public final String token;
        public final String tokenType;
        public final String clientId;
        public final String username;
        public final String subject;
        public final Set<String> scopes;
        public final Instant issuedAt;
        public final Instant expiresAt;
        public final String issuer;
        public final String audience;
        public final String jti;

        public TokenMetadata(String token, String tokenType, String clientId, String username,
                           String subject, Set<String> scopes, Instant issuedAt, Instant expiresAt,
                           String issuer, String audience, String jti) {
            this.token = token;
            this.tokenType = tokenType;
            this.clientId = clientId;
            this.username = username;
            this.subject = subject;
            this.scopes = scopes;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
            this.issuer = issuer;
            this.audience = audience;
            this.jti = jti;
        }
    }

    public static class IntrospectionCache {
        public final IntrospectionResponse response;
        public final Instant expiresAt;

        public IntrospectionCache(IntrospectionResponse response, Instant expiresAt) {
            this.response = response;
            this.expiresAt = expiresAt;
        }
    }

    public static class IntrospectionStatistics {
        public long totalRequests = 0;
        public long activeTokens = 0;
        public long expiredTokens = 0;
        public long notFoundTokens = 0;
        public long cacheHits = 0;
        public long unauthorizedRequests = 0;
        public long hintsUsed = 0;
        public long batchRequests = 0;
    }

    public record CacheStatistics(long totalCached, long activeCached, long expiredCached) {}
}
