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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * PKCE (Proof Key for Code Exchange) Pattern - RFC 7636
 * 
 * Demonstrates PKCE extension for OAuth 2.0 Authorization Code flow,
 * designed to protect public clients (mobile apps, SPAs) from authorization code
 * interception attacks.
 * 
 * Key Components:
 * - Code verifier generation (cryptographically random string)
 * - Code challenge generation (SHA-256 hash of verifier)
 * - Code challenge method (plain or S256)
 * - Code verifier validation during token exchange
 * - Enhanced security for public clients
 * 
 * Flow:
 * 1. Client generates code_verifier (random string)
 * 2. Client creates code_challenge = BASE64URL(SHA256(code_verifier))
 * 3. Client sends code_challenge + method in authorization request
 * 4. Server stores code_challenge with authorization code
 * 5. Client sends code_verifier in token request
 * 6. Server validates: code_challenge == BASE64URL(SHA256(code_verifier))
 * 7. Server issues tokens only if validation succeeds
 * 
 * Use Cases:
 * - Mobile applications (native apps)
 * - Single Page Applications (SPAs)
 * - Desktop applications
 * - Any public client without client secret
 * 
 * Security Benefits:
 * - Prevents authorization code interception
 * - Protects against malicious apps
 * - No need for client secrets in public clients
 * - Required by OAuth 2.1 for all clients
 */
@SpringBootApplication
public class PKCEPattern {

    public static void main(String[] args) {
        SpringApplication.run(PKCEPattern.class, args);
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
            // Mobile app client - PKCE required
            RegisteredClient mobileClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("mobile-app")
                // No client secret for public clients
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("myapp://callback")
                .scope("read")
                .scope("write")
                .scope("profile")
                .clientSettings(ClientSettings.builder()
                    .requireAuthorizationConsent(true)
                    .requireProofKey(true) // PKCE required
                    .build())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(15))
                    .refreshTokenTimeToLive(Duration.ofDays(30))
                    .reuseRefreshTokens(false)
                    .build())
                .build();

            // SPA client - PKCE required
            RegisteredClient spaClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("spa-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:3000/callback")
                .scope("openid")
                .scope("profile")
                .scope("email")
                .clientSettings(ClientSettings.builder()
                    .requireAuthorizationConsent(false)
                    .requireProofKey(true) // PKCE required
                    .build())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(10))
                    .refreshTokenTimeToLive(Duration.ofDays(7))
                    .build())
                .build();

            // Desktop app client - PKCE optional but recommended
            RegisteredClient desktopClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("desktop-app")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/callback")
                .scope("read")
                .scope("write")
                .clientSettings(ClientSettings.builder()
                    .requireAuthorizationConsent(true)
                    .requireProofKey(true) // PKCE required
                    .build())
                .build();

            return new InMemoryRegisteredClientRepository(mobileClient, spaClient, desktopClient);
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
     * Service to manage PKCE operations
     */
    @Service
    public static class PKCEService {

        private static final int CODE_VERIFIER_MIN_LENGTH = 43;
        private static final int CODE_VERIFIER_MAX_LENGTH = 128;
        private static final String CODE_VERIFIER_CHARSET = 
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";

        private final SecureRandom secureRandom = new SecureRandom();
        private final RegisteredClientRepository clientRepository;
        private final Map<String, PKCEAuthorizationRequest> authorizationStore = new HashMap<>();
        private final Map<String, PKCEStatistics> statsStore = new HashMap<>();

        public PKCEService(RegisteredClientRepository clientRepository) {
            this.clientRepository = clientRepository;
        }

        /**
         * Generate code verifier for PKCE
         */
        public String generateCodeVerifier() {
            int length = CODE_VERIFIER_MIN_LENGTH + 
                        secureRandom.nextInt(CODE_VERIFIER_MAX_LENGTH - CODE_VERIFIER_MIN_LENGTH);
            
            StringBuilder verifier = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int index = secureRandom.nextInt(CODE_VERIFIER_CHARSET.length());
                verifier.append(CODE_VERIFIER_CHARSET.charAt(index));
            }
            return verifier.toString();
        }

        /**
         * Generate code challenge from verifier
         */
        public String generateCodeChallenge(String codeVerifier, PKCEMethod method) {
            if (method == PKCEMethod.PLAIN) {
                return codeVerifier;
            }
            
            // S256 method
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
                return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 algorithm not available", e);
            }
        }

        /**
         * Validate code verifier length
         */
        public boolean isValidCodeVerifier(String codeVerifier) {
            if (codeVerifier == null) {
                return false;
            }
            
            int length = codeVerifier.length();
            if (length < CODE_VERIFIER_MIN_LENGTH || length > CODE_VERIFIER_MAX_LENGTH) {
                return false;
            }

            // Check characters
            for (char c : codeVerifier.toCharArray()) {
                if (CODE_VERIFIER_CHARSET.indexOf(c) == -1) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Initiate authorization with PKCE
         */
        public PKCEAuthorizationResponse initiateAuthorization(
                String clientId, 
                String codeChallenge,
                PKCEMethod codeChallengeMethod,
                Set<String> scopes,
                String redirectUri) {
            
            RegisteredClient client = clientRepository.findByClientId(clientId);
            if (client == null) {
                throw new IllegalArgumentException("Invalid client");
            }

            if (client.getClientSettings().isRequireProofKey() && codeChallenge == null) {
                throw new IllegalArgumentException("PKCE required for this client");
            }

            String authorizationCode = UUID.randomUUID().toString();
            Instant now = Instant.now();

            PKCEAuthorizationRequest request = new PKCEAuthorizationRequest(
                authorizationCode,
                clientId,
                codeChallenge,
                codeChallengeMethod,
                scopes,
                redirectUri,
                now,
                now.plus(Duration.ofMinutes(5)),
                false
            );

            authorizationStore.put(authorizationCode, request);
            updateStatistics(clientId, "authorization_initiated");

            return new PKCEAuthorizationResponse(
                authorizationCode,
                "code",
                redirectUri,
                Duration.ofMinutes(5).toSeconds()
            );
        }

        /**
         * Exchange authorization code for tokens with PKCE validation
         */
        public PKCETokenResponse exchangeCodeForTokens(
                String clientId,
                String authorizationCode,
                String codeVerifier,
                String redirectUri) {

            PKCEAuthorizationRequest request = authorizationStore.get(authorizationCode);
            if (request == null) {
                updateStatistics(clientId, "invalid_code");
                throw new IllegalArgumentException("Invalid or expired authorization code");
            }

            // Validate client
            if (!request.clientId.equals(clientId)) {
                updateStatistics(clientId, "client_mismatch");
                throw new IllegalArgumentException("Client mismatch");
            }

            // Validate redirect URI
            if (!request.redirectUri.equals(redirectUri)) {
                updateStatistics(clientId, "redirect_uri_mismatch");
                throw new IllegalArgumentException("Redirect URI mismatch");
            }

            // Check if already used
            if (request.used) {
                updateStatistics(clientId, "code_reuse_attempt");
                authorizationStore.remove(authorizationCode);
                throw new SecurityException("Authorization code already used");
            }

            // Check expiration
            if (Instant.now().isAfter(request.expiresAt)) {
                updateStatistics(clientId, "expired_code");
                authorizationStore.remove(authorizationCode);
                throw new IllegalArgumentException("Authorization code expired");
            }

            // PKCE validation
            if (request.codeChallenge != null) {
                if (codeVerifier == null) {
                    updateStatistics(clientId, "missing_verifier");
                    throw new IllegalArgumentException("Code verifier required");
                }

                if (!isValidCodeVerifier(codeVerifier)) {
                    updateStatistics(clientId, "invalid_verifier_format");
                    throw new IllegalArgumentException("Invalid code verifier format");
                }

                String computedChallenge = generateCodeChallenge(codeVerifier, request.codeChallengeMethod);
                if (!computedChallenge.equals(request.codeChallenge)) {
                    updateStatistics(clientId, "verifier_mismatch");
                    throw new SecurityException("Code verifier validation failed");
                }
            }

            // Mark as used
            request.used = true;

            // Generate tokens
            String accessToken = UUID.randomUUID().toString();
            String refreshToken = UUID.randomUUID().toString();
            
            RegisteredClient client = clientRepository.findByClientId(clientId);
            Duration accessTokenTTL = client.getTokenSettings().getAccessTokenTimeToLive();
            Duration refreshTokenTTL = client.getTokenSettings().getRefreshTokenTimeToLive();

            updateStatistics(clientId, "token_issued");

            // Clean up authorization code
            authorizationStore.remove(authorizationCode);

            return new PKCETokenResponse(
                accessToken,
                "Bearer",
                accessTokenTTL.toSeconds(),
                refreshToken,
                refreshTokenTTL.toSeconds(),
                request.scopes
            );
        }

        /**
         * Get PKCE configuration for client
         */
        public PKCEConfiguration getClientConfiguration(String clientId) {
            RegisteredClient client = clientRepository.findByClientId(clientId);
            if (client == null) {
                throw new IllegalArgumentException("Invalid client");
            }

            return new PKCEConfiguration(
                client.getClientSettings().isRequireProofKey(),
                List.of(PKCEMethod.S256, PKCEMethod.PLAIN),
                PKCEMethod.S256, // Recommended
                CODE_VERIFIER_MIN_LENGTH,
                CODE_VERIFIER_MAX_LENGTH
            );
        }

        /**
         * Get PKCE statistics
         */
        public Map<String, PKCEStatistics> getStatistics() {
            return new HashMap<>(statsStore);
        }

        /**
         * Get overall PKCE metrics
         */
        public PKCEMetrics getMetrics() {
            long totalAuthorizations = authorizationStore.size();
            long usedCodes = authorizationStore.values().stream()
                .filter(req -> req.used)
                .count();
            
            long totalSuccesses = statsStore.values().stream()
                .mapToLong(s -> s.tokensIssued)
                .sum();
            
            long totalFailures = statsStore.values().stream()
                .mapToLong(s -> s.invalidCodes + s.verifierMismatches + s.codeReuseAttempts)
                .sum();

            return new PKCEMetrics(
                totalAuthorizations,
                usedCodes,
                totalSuccesses,
                totalFailures
            );
        }

        private void updateStatistics(String clientId, String event) {
            PKCEStatistics stats = statsStore.computeIfAbsent(clientId, k -> new PKCEStatistics());
            
            switch (event) {
                case "authorization_initiated":
                    stats.authorizationsInitiated++;
                    break;
                case "token_issued":
                    stats.tokensIssued++;
                    break;
                case "invalid_code":
                    stats.invalidCodes++;
                    break;
                case "verifier_mismatch":
                    stats.verifierMismatches++;
                    break;
                case "code_reuse_attempt":
                    stats.codeReuseAttempts++;
                    break;
            }
        }
    }

    /**
     * REST Controller for PKCE operations
     */
    @RestController
    @RequestMapping("/api/pkce")
    public static class PKCEController {

        private final PKCEService pkceService;

        public PKCEController(PKCEService pkceService) {
            this.pkceService = pkceService;
        }

        /**
         * Generate code verifier
         */
        @GetMapping("/generate-verifier")
        public CodeVerifierResponse generateVerifier() {
            String verifier = pkceService.generateCodeVerifier();
            return new CodeVerifierResponse(verifier, verifier.length());
        }

        /**
         * Generate code challenge
         */
        @PostMapping("/generate-challenge")
        public CodeChallengeResponse generateChallenge(@RequestBody CodeChallengeRequest request) {
            if (!pkceService.isValidCodeVerifier(request.codeVerifier)) {
                throw new IllegalArgumentException("Invalid code verifier format");
            }

            String challenge = pkceService.generateCodeChallenge(request.codeVerifier, request.method);
            return new CodeChallengeResponse(challenge, request.method);
        }

        /**
         * Initiate authorization with PKCE
         */
        @PostMapping("/authorize")
        public PKCEAuthorizationResponse authorize(@RequestBody AuthorizeRequest request) {
            return pkceService.initiateAuthorization(
                request.clientId,
                request.codeChallenge,
                request.codeChallengeMethod,
                request.scopes,
                request.redirectUri
            );
        }

        /**
         * Exchange code for tokens
         */
        @PostMapping("/token")
        public PKCETokenResponse exchangeToken(@RequestBody TokenExchangeRequest request) {
            return pkceService.exchangeCodeForTokens(
                request.clientId,
                request.code,
                request.codeVerifier,
                request.redirectUri
            );
        }

        /**
         * Get client PKCE configuration
         */
        @GetMapping("/config/{clientId}")
        public PKCEConfiguration getConfiguration(@PathVariable String clientId) {
            return pkceService.getClientConfiguration(clientId);
        }

        /**
         * Get PKCE statistics
         */
        @GetMapping("/statistics")
        public Map<String, PKCEStatistics> getStatistics() {
            return pkceService.getStatistics();
        }

        /**
         * Get PKCE metrics
         */
        @GetMapping("/metrics")
        public PKCEMetrics getMetrics() {
            return pkceService.getMetrics();
        }

        /**
         * Get pattern information
         */
        @GetMapping("/info")
        public PatternInfo getInfo() {
            return new PatternInfo(
                "PKCE (Proof Key for Code Exchange) Pattern - RFC 7636",
                "OAuth 2.0 extension protecting public clients from authorization code interception",
                List.of(
                    "GET /api/pkce/generate-verifier - Generate code verifier",
                    "POST /api/pkce/generate-challenge - Generate code challenge",
                    "POST /api/pkce/authorize - Initiate authorization",
                    "POST /api/pkce/token - Exchange code for tokens",
                    "GET /api/pkce/config/{clientId} - Get client configuration",
                    "GET /api/pkce/statistics - Get statistics",
                    "GET /api/pkce/metrics - Get metrics",
                    "GET /api/pkce/info - Get pattern information"
                ),
                Map.of(
                    "methods", List.of("S256", "plain"),
                    "recommended_method", "S256",
                    "verifier_length", "43-128 characters",
                    "use_cases", List.of("Mobile apps", "SPAs", "Desktop apps", "Public clients")
                )
            );
        }
    }

    // DTOs
    public record CodeVerifierResponse(String codeVerifier, int length) {}
    public record CodeChallengeRequest(String codeVerifier, PKCEMethod method) {}
    public record CodeChallengeResponse(String codeChallenge, PKCEMethod method) {}
    public record AuthorizeRequest(String clientId, String codeChallenge, PKCEMethod codeChallengeMethod,
                                  Set<String> scopes, String redirectUri) {}
    public record TokenExchangeRequest(String clientId, String code, String codeVerifier, String redirectUri) {}
    public record PatternInfo(String name, String description, List<String> endpoints, Map<String, Object> details) {}

    // Domain Objects
    public enum PKCEMethod {
        PLAIN, S256
    }

    public record PKCEAuthorizationResponse(String code, String responseType, String redirectUri, long expiresIn) {}

    public record PKCETokenResponse(String accessToken, String tokenType, long expiresIn,
                                   String refreshToken, long refreshTokenExpiresIn, Set<String> scopes) {}

    public record PKCEConfiguration(boolean required, List<PKCEMethod> supportedMethods,
                                   PKCEMethod recommendedMethod, int minVerifierLength, int maxVerifierLength) {}

    public static class PKCEAuthorizationRequest {
        public final String code;
        public final String clientId;
        public final String codeChallenge;
        public final PKCEMethod codeChallengeMethod;
        public final Set<String> scopes;
        public final String redirectUri;
        public final Instant createdAt;
        public final Instant expiresAt;
        public boolean used;

        public PKCEAuthorizationRequest(String code, String clientId, String codeChallenge,
                                       PKCEMethod codeChallengeMethod, Set<String> scopes,
                                       String redirectUri, Instant createdAt, Instant expiresAt, boolean used) {
            this.code = code;
            this.clientId = clientId;
            this.codeChallenge = codeChallenge;
            this.codeChallengeMethod = codeChallengeMethod;
            this.scopes = scopes;
            this.redirectUri = redirectUri;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.used = used;
        }
    }

    public static class PKCEStatistics {
        public long authorizationsInitiated = 0;
        public long tokensIssued = 0;
        public long invalidCodes = 0;
        public long verifierMismatches = 0;
        public long codeReuseAttempts = 0;
    }

    public record PKCEMetrics(long totalAuthorizations, long usedCodes, long successfulExchanges, long failedExchanges) {}
}
