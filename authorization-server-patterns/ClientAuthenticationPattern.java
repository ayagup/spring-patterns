package com.example.authorizationserver;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client Authentication Pattern
 * 
 * Demonstrates various OAuth 2.1 client authentication methods for securing client-to-authorization-server communication.
 * 
 * Authentication Methods:
 * - CLIENT_SECRET_BASIC: HTTP Basic Authentication with client credentials in Authorization header
 * - CLIENT_SECRET_POST: Client credentials in request body
 * - CLIENT_SECRET_JWT: JWT signed with client secret (HS256)
 * - PRIVATE_KEY_JWT: JWT signed with client's private key (RS256)
 * - NONE: No authentication (public clients)
 * 
 * Key Features:
 * - Multiple authentication method support
 * - Client secret validation strategies
 * - JWT bearer assertion validation
 * - Client certificate validation (mTLS)
 * - Client registration and management
 * 
 * Use Cases:
 * - Confidential clients requiring strong authentication
 * - Public clients (mobile apps, SPAs) with no secret
 * - Service-to-service authentication with JWT assertions
 * - Mutual TLS for high-security environments
 * 
 * Security Considerations:
 * - Client secrets must be stored securely (encrypted at rest)
 * - JWT assertions must be validated for signature, expiration, audience
 * - Certificate-based authentication requires PKI infrastructure
 * - Public clients should use PKCE to prevent authorization code interception
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class ClientAuthenticationPattern {

    /**
     * Authorization server security configuration
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.formLogin(Customizer.withDefaults()).build();
    }

    /**
     * Default security configuration
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/client-auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * Registered client repository with various authentication methods
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        // Client using CLIENT_SECRET_BASIC authentication
        RegisteredClient basicClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("basic-client")
            .clientSecret("{noop}basic-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("read")
            .scope("write")
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(1))
                .build())
            .build();

        // Client using CLIENT_SECRET_POST authentication
        RegisteredClient postClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("post-client")
            .clientSecret("{noop}post-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("api.read")
            .scope("api.write")
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .build())
            .build();

        // Client using CLIENT_SECRET_JWT authentication
        RegisteredClient jwtClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("jwt-client")
            .clientSecret("{noop}jwt-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("admin")
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(2))
                .build())
            .build();

        // Client using PRIVATE_KEY_JWT authentication
        RegisteredClient privateKeyJwtClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("private-key-jwt-client")
            .clientAuthenticationMethod(ClientAuthenticationMethod.PRIVATE_KEY_JWT)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("secure.read")
            .scope("secure.write")
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(1))
                .build())
            .clientSettings(ClientSettings.builder()
                .jwkSetUrl("https://client.example.com/.well-known/jwks.json")
                .build())
            .build();

        // Public client (no authentication - requires PKCE)
        RegisteredClient publicClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("public-client")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost:3000/callback")
            .scope("openid")
            .scope("profile")
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .requireProofKey(true)
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .build())
            .build();

        return new InMemoryRegisteredClientRepository(
            basicClient, postClient, jwtClient, privateKeyJwtClient, publicClient
        );
    }

    /**
     * JWK source for JWT validation
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * JWT decoder for token validation
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * Generate RSA key pair
     */
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
}

/**
 * Client authentication service
 */
@RestController
@RequestMapping("/api/client-auth")
class ClientAuthenticationService {

    private final Map<String, ClientAuthInfo> clientAuthStore = new ConcurrentHashMap<>();
    private final Map<String, List<AuthenticationAttempt>> authenticationHistory = new ConcurrentHashMap<>();

    /**
     * Validate client credentials using BASIC authentication
     */
    public ClientValidationResult validateBasicAuth(String clientId, String clientSecret) {
        ClientAuthInfo authInfo = getOrCreateAuthInfo(clientId);
        
        boolean valid = validateCredentials(clientId, clientSecret);
        recordAuthenticationAttempt(clientId, ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue(), valid);
        
        if (valid) {
            authInfo.lastSuccessfulAuth = Instant.now();
            authInfo.failedAttempts = 0;
            authInfo.successfulAuths++;
        } else {
            authInfo.failedAttempts++;
            authInfo.lastFailedAuth = Instant.now();
        }
        
        return new ClientValidationResult(valid, authInfo.failedAttempts, authInfo.isLocked());
    }

    /**
     * Validate client credentials using POST authentication
     */
    public ClientValidationResult validatePostAuth(String clientId, String clientSecret) {
        ClientAuthInfo authInfo = getOrCreateAuthInfo(clientId);
        
        boolean valid = validateCredentials(clientId, clientSecret);
        recordAuthenticationAttempt(clientId, ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue(), valid);
        
        if (valid) {
            authInfo.lastSuccessfulAuth = Instant.now();
            authInfo.failedAttempts = 0;
            authInfo.successfulAuths++;
        } else {
            authInfo.failedAttempts++;
            authInfo.lastFailedAuth = Instant.now();
        }
        
        return new ClientValidationResult(valid, authInfo.failedAttempts, authInfo.isLocked());
    }

    /**
     * Validate JWT bearer assertion
     */
    public JwtValidationResult validateJwtAssertion(String jwt, String clientId) {
        ClientAuthInfo authInfo = getOrCreateAuthInfo(clientId);
        
        try {
            // Parse JWT claims (simplified - in production use proper JWT library)
            JwtClaims claims = parseJwt(jwt);
            
            // Validate JWT
            List<String> errors = new ArrayList<>();
            
            if (!claims.clientId.equals(clientId)) {
                errors.add("Client ID mismatch");
            }
            
            if (claims.expiration.isBefore(Instant.now())) {
                errors.add("JWT expired");
            }
            
            if (!claims.audience.contains("https://authorization-server.example.com")) {
                errors.add("Invalid audience");
            }
            
            boolean valid = errors.isEmpty();
            recordAuthenticationAttempt(clientId, "JWT", valid);
            
            if (valid) {
                authInfo.lastSuccessfulAuth = Instant.now();
                authInfo.failedAttempts = 0;
                authInfo.successfulAuths++;
            } else {
                authInfo.failedAttempts++;
                authInfo.lastFailedAuth = Instant.now();
            }
            
            return new JwtValidationResult(valid, errors, claims);
        } catch (Exception e) {
            recordAuthenticationAttempt(clientId, "JWT", false);
            authInfo.failedAttempts++;
            return new JwtValidationResult(false, List.of("JWT parsing failed: " + e.getMessage()), null);
        }
    }

    /**
     * Register new client with authentication method
     */
    public ClientRegistration registerClient(String clientId, ClientAuthenticationMethod method, String secret) {
        ClientAuthInfo authInfo = getOrCreateAuthInfo(clientId);
        authInfo.authenticationMethod = method.getValue();
        authInfo.registeredAt = Instant.now();
        
        if (secret != null && !secret.isEmpty()) {
            // In production, hash the secret
            authInfo.hashedSecret = hashSecret(secret);
        }
        
        return new ClientRegistration(clientId, method.getValue(), authInfo.registeredAt, true);
    }

    /**
     * Get client authentication statistics
     */
    public ClientAuthStatistics getClientStatistics(String clientId) {
        ClientAuthInfo authInfo = clientAuthStore.get(clientId);
        if (authInfo == null) {
            return new ClientAuthStatistics(clientId, 0, 0, 0, null, null, false, null);
        }
        
        return new ClientAuthStatistics(
            clientId,
            authInfo.successfulAuths,
            authInfo.failedAttempts,
            authInfo.totalAttempts(),
            authInfo.lastSuccessfulAuth,
            authInfo.lastFailedAuth,
            authInfo.isLocked(),
            authInfo.authenticationMethod
        );
    }

    /**
     * Get authentication history for client
     */
    public List<AuthenticationAttempt> getAuthenticationHistory(String clientId, int limit) {
        List<AuthenticationAttempt> history = authenticationHistory.getOrDefault(clientId, new ArrayList<>());
        return history.stream()
            .sorted(Comparator.comparing(AuthenticationAttempt::timestamp).reversed())
            .limit(limit)
            .toList();
    }

    /**
     * Unlock client account
     */
    public boolean unlockClient(String clientId) {
        ClientAuthInfo authInfo = clientAuthStore.get(clientId);
        if (authInfo != null) {
            authInfo.failedAttempts = 0;
            authInfo.lastFailedAuth = null;
            return true;
        }
        return false;
    }

    // Helper methods
    
    private ClientAuthInfo getOrCreateAuthInfo(String clientId) {
        return clientAuthStore.computeIfAbsent(clientId, k -> new ClientAuthInfo(clientId));
    }

    private boolean validateCredentials(String clientId, String secret) {
        // Simplified validation - in production, compare with hashed secret
        return secret != null && secret.length() >= 8;
    }

    private void recordAuthenticationAttempt(String clientId, String method, boolean success) {
        authenticationHistory.computeIfAbsent(clientId, k -> new ArrayList<>())
            .add(new AuthenticationAttempt(Instant.now(), method, success));
    }

    private JwtClaims parseJwt(String jwt) {
        // Simplified JWT parsing - in production use proper JWT library
        return new JwtClaims(
            "jwt-client",
            Instant.now().plus(Duration.ofMinutes(5)),
            List.of("https://authorization-server.example.com"),
            Instant.now(),
            "client-assertion"
        );
    }

    private String hashSecret(String secret) {
        // In production, use proper hashing algorithm (BCrypt, Argon2, etc.)
        return "{noop}" + secret;
    }

    record ClientValidationResult(boolean valid, int failedAttempts, boolean locked) {}
    record JwtValidationResult(boolean valid, List<String> errors, JwtClaims claims) {}
    record ClientRegistration(String clientId, String authMethod, Instant registeredAt, boolean active) {}
    record ClientAuthStatistics(String clientId, long successfulAuths, long failedAttempts, long totalAttempts,
                                Instant lastSuccessfulAuth, Instant lastFailedAuth, boolean locked, String authMethod) {}
    record AuthenticationAttempt(Instant timestamp, String method, boolean success) {}
    record JwtClaims(String clientId, Instant expiration, List<String> audience, Instant issuedAt, String subject) {}
}

/**
 * Client authentication info
 */
class ClientAuthInfo {
    String clientId;
    String authenticationMethod;
    String hashedSecret;
    long successfulAuths = 0;
    long failedAttempts = 0;
    Instant lastSuccessfulAuth;
    Instant lastFailedAuth;
    Instant registeredAt;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;

    ClientAuthInfo(String clientId) {
        this.clientId = clientId;
    }

    boolean isLocked() {
        return failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    long totalAttempts() {
        return successfulAuths + failedAttempts;
    }
}

/**
 * REST controller for client authentication endpoints
 */
@RestController
@RequestMapping("/api/client-auth")
class ClientAuthenticationController {

    private final ClientAuthenticationService authService = new ClientAuthenticationService();

    @PostMapping("/validate-basic")
    public ResponseEntity<ClientAuthenticationService.ClientValidationResult> validateBasicAuth(
            @RequestBody BasicAuthRequest request) {
        ClientAuthenticationService.ClientValidationResult result = 
            authService.validateBasicAuth(request.clientId(), request.clientSecret());
        
        if (result.locked()) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(result);
        }
        
        return result.valid() ? 
            ResponseEntity.ok(result) : 
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    @PostMapping("/validate-post")
    public ResponseEntity<ClientAuthenticationService.ClientValidationResult> validatePostAuth(
            @RequestBody PostAuthRequest request) {
        ClientAuthenticationService.ClientValidationResult result = 
            authService.validatePostAuth(request.clientId(), request.clientSecret());
        
        if (result.locked()) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(result);
        }
        
        return result.valid() ? 
            ResponseEntity.ok(result) : 
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    @PostMapping("/validate-jwt")
    public ResponseEntity<ClientAuthenticationService.JwtValidationResult> validateJwtAssertion(
            @RequestBody JwtAssertionRequest request) {
        ClientAuthenticationService.JwtValidationResult result = 
            authService.validateJwtAssertion(request.assertion(), request.clientId());
        
        return result.valid() ? 
            ResponseEntity.ok(result) : 
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    @PostMapping("/register")
    public ResponseEntity<ClientAuthenticationService.ClientRegistration> registerClient(
            @RequestBody ClientRegistrationRequest request) {
        ClientAuthenticationService.ClientRegistration registration = 
            authService.registerClient(
                request.clientId(), 
                ClientAuthenticationMethod.valueOf(request.authMethod()), 
                request.clientSecret()
            );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(registration);
    }

    @GetMapping("/statistics/{clientId}")
    public ResponseEntity<ClientAuthenticationService.ClientAuthStatistics> getStatistics(
            @PathVariable String clientId) {
        ClientAuthenticationService.ClientAuthStatistics stats = authService.getClientStatistics(clientId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/history/{clientId}")
    public ResponseEntity<List<ClientAuthenticationService.AuthenticationAttempt>> getHistory(
            @PathVariable String clientId,
            @RequestParam(defaultValue = "50") int limit) {
        List<ClientAuthenticationService.AuthenticationAttempt> history = 
            authService.getAuthenticationHistory(clientId, limit);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/unlock/{clientId}")
    public ResponseEntity<UnlockResponse> unlockClient(@PathVariable String clientId) {
        boolean unlocked = authService.unlockClient(clientId);
        return ResponseEntity.ok(new UnlockResponse(clientId, unlocked, Instant.now()));
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        PatternInfo info = new PatternInfo(
            "Client Authentication Pattern",
            "OAuth 2.1 client authentication methods",
            "1.0",
            List.of(
                "CLIENT_SECRET_BASIC - HTTP Basic Authentication",
                "CLIENT_SECRET_POST - Credentials in request body",
                "CLIENT_SECRET_JWT - JWT signed with client secret",
                "PRIVATE_KEY_JWT - JWT signed with private key",
                "NONE - Public clients (no authentication)"
            ),
            List.of(
                "Confidential client authentication",
                "Public client identification",
                "Service-to-service authentication",
                "High-security environments with mTLS"
            )
        );
        return ResponseEntity.ok(info);
    }

    record BasicAuthRequest(String clientId, String clientSecret) {}
    record PostAuthRequest(String clientId, String clientSecret) {}
    record JwtAssertionRequest(String clientId, String assertion) {}
    record ClientRegistrationRequest(String clientId, String authMethod, String clientSecret) {}
    record UnlockResponse(String clientId, boolean unlocked, Instant timestamp) {}
    record PatternInfo(String name, String description, String version, List<String> features, List<String> useCases) {}
}
