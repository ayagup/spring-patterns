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
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Client Credentials Grant Pattern
 * 
 * Demonstrates OAuth 2.1 Client Credentials flow for machine-to-machine authentication.
 * This grant type is used when the application itself (not a user) needs to access resources.
 * 
 * Key Components:
 * - Client registration with CLIENT_CREDENTIALS grant type
 * - Token endpoint for direct token issuance
 * - Client authentication (secret_basic, secret_post, client_secret_jwt, private_key_jwt)
 * - Scope-based access control
 * - Token settings (lifetime, reuse)
 * 
 * Use Cases:
 * - Backend service-to-service communication
 * - Scheduled jobs accessing APIs
 * - Microservices authentication
 * - CLI tools and automation scripts
 * 
 * Security Considerations:
 * - No user context involved
 * - Client credentials must be securely stored
 * - Use mutual TLS for enhanced security
 * - Limit scope to minimum required permissions
 */
@SpringBootApplication
public class ClientCredentialsGrantPattern {

    public static void main(String[] args) {
        SpringApplication.run(ClientCredentialsGrantPattern.class, args);
    }

    /**
     * OAuth2 Authorization Server Configuration
     */
    @Configuration
    @EnableWebSecurity
    public static class AuthorizationServerConfig {

        /**
         * Configure Authorization Server security
         */
        @Bean
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
            OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
            return http
                .formLogin(Customizer.withDefaults())
                .build();
        }

        /**
         * Configure default security for other requests
         */
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

        /**
         * Register OAuth2 clients with Client Credentials grant
         */
        @Bean
        public RegisteredClientRepository registeredClientRepository() {
            // Client 1: Basic authentication with secret_basic
            RegisteredClient basicClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("service-client-basic")
                .clientSecret("{noop}service-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .scope("write")
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(1))
                    .reuseRefreshTokens(false)
                    .build())
                .clientSettings(ClientSettings.builder()
                    .requireAuthorizationConsent(false)
                    .requireProofKey(false)
                    .build())
                .build();

            // Client 2: Post authentication with secret_post
            RegisteredClient postClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("service-client-post")
                .clientSecret("{noop}post-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("api.read")
                .scope("api.write")
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(30))
                    .build())
                .build();

            // Client 3: JWT authentication (simulated)
            RegisteredClient jwtClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("service-client-jwt")
                .clientSecret("{noop}jwt-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("admin")
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(2))
                    .build())
                .build();

            // Client 4: Limited scope client
            RegisteredClient limitedClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("limited-service-client")
                .clientSecret("{noop}limited-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(15))
                    .build())
                .build();

            return new InMemoryRegisteredClientRepository(
                basicClient, postClient, jwtClient, limitedClient
            );
        }

        /**
         * User details service (required by Spring Security)
         */
        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails user = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("USER", "ADMIN")
                .build();
            return new InMemoryUserDetailsManager(user);
        }
    }

    /**
     * Service to manage client credentials operations
     */
    @Service
    public static class ClientCredentialsService {

        private final RegisteredClientRepository clientRepository;
        private final Map<String, TokenInfo> tokenStore = new HashMap<>();

        public ClientCredentialsService(RegisteredClientRepository clientRepository) {
            this.clientRepository = clientRepository;
        }

        /**
         * Simulate token issuance for client credentials flow
         */
        public TokenInfo issueToken(String clientId, String clientSecret, Set<String> requestedScopes) {
            RegisteredClient client = clientRepository.findByClientId(clientId);
            
            if (client == null) {
                throw new IllegalArgumentException("Invalid client credentials");
            }

            // Validate client secret
            if (!validateClientSecret(client, clientSecret)) {
                throw new IllegalArgumentException("Invalid client credentials");
            }

            // Validate requested scopes
            Set<String> grantedScopes = validateScopes(client, requestedScopes);

            // Generate access token
            String accessToken = UUID.randomUUID().toString();
            Instant issuedAt = Instant.now();
            Duration tokenLifetime = client.getTokenSettings().getAccessTokenTimeToLive();
            Instant expiresAt = issuedAt.plus(tokenLifetime);

            TokenInfo tokenInfo = new TokenInfo(
                accessToken,
                "Bearer",
                tokenLifetime.toSeconds(),
                grantedScopes,
                issuedAt,
                expiresAt
            );

            tokenStore.put(accessToken, tokenInfo);
            return tokenInfo;
        }

        /**
         * Validate client secret
         */
        private boolean validateClientSecret(RegisteredClient client, String providedSecret) {
            String storedSecret = client.getClientSecret();
            if (storedSecret == null) {
                return false;
            }
            // Remove {noop} prefix for comparison
            storedSecret = storedSecret.replace("{noop}", "");
            return storedSecret.equals(providedSecret);
        }

        /**
         * Validate and filter requested scopes
         */
        private Set<String> validateScopes(RegisteredClient client, Set<String> requestedScopes) {
            Set<String> clientScopes = client.getScopes();
            
            if (requestedScopes == null || requestedScopes.isEmpty()) {
                return clientScopes;
            }

            Set<String> grantedScopes = new HashSet<>();
            for (String scope : requestedScopes) {
                if (clientScopes.contains(scope)) {
                    grantedScopes.add(scope);
                }
            }

            if (grantedScopes.isEmpty()) {
                throw new IllegalArgumentException("No valid scopes requested");
            }

            return grantedScopes;
        }

        /**
         * Introspect token
         */
        public TokenIntrospectionResult introspectToken(String token) {
            TokenInfo tokenInfo = tokenStore.get(token);
            
            if (tokenInfo == null) {
                return new TokenIntrospectionResult(false, null);
            }

            boolean active = Instant.now().isBefore(tokenInfo.expiresAt);
            return new TokenIntrospectionResult(active, tokenInfo);
        }

        /**
         * Get all registered clients
         */
        public List<ClientInfo> getAllClients() {
            List<ClientInfo> clients = new ArrayList<>();
            
            // Note: InMemoryRegisteredClientRepository doesn't provide a way to list all clients
            // In production, you would use a database-backed repository
            clients.add(new ClientInfo("service-client-basic", 
                Set.of("read", "write"), 
                "CLIENT_SECRET_BASIC", 
                "CLIENT_CREDENTIALS"));
            clients.add(new ClientInfo("service-client-post", 
                Set.of("api.read", "api.write"), 
                "CLIENT_SECRET_POST", 
                "CLIENT_CREDENTIALS"));
            clients.add(new ClientInfo("service-client-jwt", 
                Set.of("admin"), 
                "CLIENT_SECRET_JWT", 
                "CLIENT_CREDENTIALS"));
            clients.add(new ClientInfo("limited-service-client", 
                Set.of("read"), 
                "CLIENT_SECRET_BASIC", 
                "CLIENT_CREDENTIALS"));
            
            return clients;
        }

        /**
         * Get token statistics
         */
        public TokenStatistics getTokenStatistics() {
            long activeTokens = tokenStore.values().stream()
                .filter(token -> Instant.now().isBefore(token.expiresAt))
                .count();
            
            return new TokenStatistics(
                tokenStore.size(),
                activeTokens,
                tokenStore.size() - activeTokens
            );
        }
    }

    /**
     * REST Controller for Client Credentials operations
     */
    @RestController
    @RequestMapping("/api/client-credentials")
    public static class ClientCredentialsController {

        private final ClientCredentialsService clientCredentialsService;

        public ClientCredentialsController(ClientCredentialsService clientCredentialsService) {
            this.clientCredentialsService = clientCredentialsService;
        }

        /**
         * Issue access token using client credentials
         */
        @PostMapping("/token")
        public TokenResponse issueToken(@RequestBody TokenRequest request) {
            TokenInfo tokenInfo = clientCredentialsService.issueToken(
                request.clientId,
                request.clientSecret,
                request.scopes
            );
            
            return new TokenResponse(
                tokenInfo.accessToken,
                tokenInfo.tokenType,
                tokenInfo.expiresIn,
                tokenInfo.scopes
            );
        }

        /**
         * Introspect token
         */
        @PostMapping("/introspect")
        public IntrospectionResponse introspectToken(@RequestBody IntrospectionRequest request) {
            TokenIntrospectionResult result = clientCredentialsService.introspectToken(request.token);
            
            if (!result.active) {
                return new IntrospectionResponse(false, null, null, null);
            }

            return new IntrospectionResponse(
                true,
                result.tokenInfo.scopes,
                result.tokenInfo.issuedAt,
                result.tokenInfo.expiresAt
            );
        }

        /**
         * Get all registered clients
         */
        @GetMapping("/clients")
        public ClientsResponse getAllClients() {
            return new ClientsResponse(clientCredentialsService.getAllClients());
        }

        /**
         * Get token statistics
         */
        @GetMapping("/statistics")
        public TokenStatistics getStatistics() {
            return clientCredentialsService.getTokenStatistics();
        }

        /**
         * Get pattern information
         */
        @GetMapping("/info")
        public PatternInfo getInfo() {
            return new PatternInfo(
                "Client Credentials Grant Pattern",
                "OAuth 2.1 Client Credentials flow for machine-to-machine authentication",
                List.of(
                    "POST /api/client-credentials/token - Issue access token",
                    "POST /api/client-credentials/introspect - Introspect token",
                    "GET /api/client-credentials/clients - Get all clients",
                    "GET /api/client-credentials/statistics - Get token statistics",
                    "GET /api/client-credentials/info - Get pattern information"
                ),
                Map.of(
                    "grant_type", "client_credentials",
                    "authentication_methods", List.of("client_secret_basic", "client_secret_post", "client_secret_jwt"),
                    "use_cases", List.of("Service-to-service", "Background jobs", "CLI tools")
                )
            );
        }
    }

    // DTOs
    public record TokenRequest(String clientId, String clientSecret, Set<String> scopes) {}
    
    public record TokenResponse(String accessToken, String tokenType, long expiresIn, Set<String> scopes) {}
    
    public record IntrospectionRequest(String token) {}
    
    public record IntrospectionResponse(boolean active, Set<String> scopes, Instant issuedAt, Instant expiresAt) {}
    
    public record ClientsResponse(List<ClientInfo> clients) {}
    
    public record PatternInfo(String name, String description, List<String> endpoints, Map<String, Object> details) {}

    // Domain Objects
    public static class TokenInfo {
        public final String accessToken;
        public final String tokenType;
        public final long expiresIn;
        public final Set<String> scopes;
        public final Instant issuedAt;
        public final Instant expiresAt;

        public TokenInfo(String accessToken, String tokenType, long expiresIn, 
                        Set<String> scopes, Instant issuedAt, Instant expiresAt) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.scopes = scopes;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }
    }

    public static class TokenIntrospectionResult {
        public final boolean active;
        public final TokenInfo tokenInfo;

        public TokenIntrospectionResult(boolean active, TokenInfo tokenInfo) {
            this.active = active;
            this.tokenInfo = tokenInfo;
        }
    }

    public record ClientInfo(String clientId, Set<String> scopes, 
                            String authenticationMethod, String grantType) {}

    public record TokenStatistics(long totalTokens, long activeTokens, long expiredTokens) {}
}
