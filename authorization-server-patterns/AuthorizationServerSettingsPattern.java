package com.example.authorizationserver;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
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

/**
 * Authorization Server Settings Pattern
 * 
 * Demonstrates comprehensive configuration and customization of Spring Authorization Server settings.
 * 
 * Server Settings:
 * - Issuer URL configuration
 * - Endpoint customization (authorization, token, introspection, revocation, jwks)
 * - OIDC endpoints (userinfo, client registration)
 * - Token settings (lifetime, format, rotation policies)
 * - Security policies and constraints
 * 
 * Key Configuration Areas:
 * - Endpoint URLs and paths
 * - Token lifetimes and formats
 * - Refresh token rotation
 * - PKCE requirements
 * - Consent requirements
 * - CORS configuration
 * - Session management
 * 
 * Use Cases:
 * - Custom domain and endpoint paths
 * - Multi-tenant authorization server
 * - Specific token lifetime requirements
 * - Enhanced security policies
 * - Integration with existing identity systems
 * 
 * Security Considerations:
 * - Issuer URL must match actual server URL
 * - Token lifetimes should balance security and usability
 * - PKCE should be required for public clients
 * - Refresh token rotation recommended for enhanced security
 * - CORS policies must be carefully configured
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class AuthorizationServerSettingsPattern {

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
                .requestMatchers("/api/server-settings/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * Authorization server settings with custom configuration
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
            // Issuer URL - must match the actual server URL
            .issuer("https://auth.example.com")
            
            // OAuth 2.0 Endpoints
            .authorizationEndpoint("/oauth2/v1/authorize")
            .tokenEndpoint("/oauth2/v1/token")
            .tokenIntrospectionEndpoint("/oauth2/v1/introspect")
            .tokenRevocationEndpoint("/oauth2/v1/revoke")
            .jwkSetEndpoint("/oauth2/v1/jwks")
            
            // OIDC Endpoints
            .oidcUserInfoEndpoint("/oauth2/v1/userinfo")
            .oidcClientRegistrationEndpoint("/oauth2/v1/register")
            
            // Device Authorization Endpoints (RFC 8628)
            .deviceAuthorizationEndpoint("/oauth2/v1/device_authorization")
            .deviceVerificationEndpoint("/oauth2/v1/device_verification")
            
            .build();
    }

    /**
     * Registered client repository with various token settings
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        // Web application with standard token settings
        RegisteredClient webApp = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("web-app")
            .clientSecret("{noop}web-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://localhost:8080/authorized")
            .scope("openid")
            .scope("profile")
            .scope("email")
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .requireProofKey(false)
                .build())
            .tokenSettings(TokenSettings.builder()
                // Access token lifetime
                .accessTokenTimeToLive(Duration.ofHours(1))
                // Refresh token lifetime
                .refreshTokenTimeToLive(Duration.ofDays(30))
                // Refresh token rotation
                .reuseRefreshTokens(false)
                // Authorization code lifetime
                .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                // Device code lifetime
                .deviceCodeTimeToLive(Duration.ofMinutes(10))
                .build())
            .build();

        // Mobile app with shorter token lifetimes and PKCE required
        RegisteredClient mobileApp = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("mobile-app")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("myapp://callback")
            .scope("openid")
            .scope("profile")
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .requireProofKey(true) // PKCE required for public clients
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .refreshTokenTimeToLive(Duration.ofDays(90))
                .reuseRefreshTokens(false)
                .authorizationCodeTimeToLive(Duration.ofMinutes(2))
                .build())
            .build();

        // Service client with long-lived tokens
        RegisteredClient serviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("service-client")
            .clientSecret("{noop}service-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("api.read")
            .scope("api.write")
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(4))
                .build())
            .build();

        return new InMemoryRegisteredClientRepository(webApp, mobileApp, serviceClient);
    }

    /**
     * JWK source for JWT signing
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
     * Generate RSA key pair for JWT signing
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
 * Server settings management service
 */
@RestController
@RequestMapping("/api/server-settings")
class AuthorizationServerSettingsService {

    private final ServerConfiguration serverConfig = new ServerConfiguration();

    /**
     * Get current server settings
     */
    public ServerSettingsInfo getServerSettings() {
        return new ServerSettingsInfo(
            serverConfig.issuer,
            serverConfig.endpoints,
            serverConfig.tokenSettings,
            serverConfig.securityPolicies,
            serverConfig.corsSettings
        );
    }

    /**
     * Get endpoint configuration
     */
    public EndpointConfiguration getEndpointConfiguration() {
        return serverConfig.endpoints;
    }

    /**
     * Get token settings configuration
     */
    public ServerTokenSettings getTokenSettings() {
        return serverConfig.tokenSettings;
    }

    /**
     * Get security policies
     */
    public SecurityPolicies getSecurityPolicies() {
        return serverConfig.securityPolicies;
    }

    /**
     * Update token settings
     */
    public ServerTokenSettings updateTokenSettings(TokenSettingsUpdate update) {
        if (update.accessTokenTtl() != null) {
            serverConfig.tokenSettings.accessTokenTtl = update.accessTokenTtl();
        }
        if (update.refreshTokenTtl() != null) {
            serverConfig.tokenSettings.refreshTokenTtl = update.refreshTokenTtl();
        }
        if (update.authorizationCodeTtl() != null) {
            serverConfig.tokenSettings.authorizationCodeTtl = update.authorizationCodeTtl();
        }
        if (update.reuseRefreshTokens() != null) {
            serverConfig.tokenSettings.reuseRefreshTokens = update.reuseRefreshTokens();
        }
        
        serverConfig.lastModified = Instant.now();
        return serverConfig.tokenSettings;
    }

    /**
     * Update security policies
     */
    public SecurityPolicies updateSecurityPolicies(SecurityPolicyUpdate update) {
        if (update.requirePkce() != null) {
            serverConfig.securityPolicies.requirePkceForPublicClients = update.requirePkce();
        }
        if (update.requireConsent() != null) {
            serverConfig.securityPolicies.requireConsentForThirdParty = update.requireConsent();
        }
        if (update.allowRefreshTokenRotation() != null) {
            serverConfig.securityPolicies.allowRefreshTokenRotation = update.allowRefreshTokenRotation();
        }
        
        serverConfig.lastModified = Instant.now();
        return serverConfig.securityPolicies;
    }

    /**
     * Get server metadata (OAuth 2.0 Discovery)
     */
    public ServerMetadata getServerMetadata() {
        return new ServerMetadata(
            serverConfig.issuer,
            serverConfig.endpoints.authorizationEndpoint,
            serverConfig.endpoints.tokenEndpoint,
            serverConfig.endpoints.jwksUri,
            List.of("code", "client_credentials", "refresh_token", "urn:ietf:params:oauth:grant-type:device_code"),
            List.of("client_secret_basic", "client_secret_post", "client_secret_jwt", "private_key_jwt"),
            List.of("query", "fragment"),
            List.of("S256", "plain"),
            List.of("openid", "profile", "email"),
            true,
            true
        );
    }

    /**
     * Get server health and status
     */
    public ServerHealth getServerHealth() {
        return new ServerHealth(
            "UP",
            Instant.now(),
            serverConfig.lastModified,
            Duration.between(serverConfig.startTime, Instant.now()),
            serverConfig.endpoints != null,
            true,
            true
        );
    }

    /**
     * Get server statistics
     */
    public ServerStatistics getStatistics() {
        return new ServerStatistics(
            serverConfig.startTime,
            Instant.now(),
            Duration.between(serverConfig.startTime, Instant.now()),
            serverConfig.endpoints.getEndpointCount(),
            3, // Registered clients count
            serverConfig.tokenSettings.accessTokenTtl,
            serverConfig.tokenSettings.refreshTokenTtl
        );
    }

    record ServerSettingsInfo(String issuer, EndpointConfiguration endpoints, 
                             ServerTokenSettings tokenSettings, SecurityPolicies securityPolicies,
                             CorsSettings corsSettings) {}
    
    record TokenSettingsUpdate(Duration accessTokenTtl, Duration refreshTokenTtl, 
                              Duration authorizationCodeTtl, Boolean reuseRefreshTokens) {}
    
    record SecurityPolicyUpdate(Boolean requirePkce, Boolean requireConsent, 
                               Boolean allowRefreshTokenRotation) {}
    
    record ServerMetadata(String issuer, String authorizationEndpoint, String tokenEndpoint, 
                         String jwksUri, List<String> grantTypesSupported, 
                         List<String> tokenEndpointAuthMethodsSupported, List<String> responseModes,
                         List<String> codeChallengeMethodsSupported, List<String> scopesSupported,
                         boolean introspectionEndpointAvailable, boolean revocationEndpointAvailable) {}
    
    record ServerHealth(String status, Instant currentTime, Instant lastConfigUpdate, 
                       Duration uptime, boolean endpointsConfigured, boolean jwksAvailable, 
                       boolean databaseConnected) {}
    
    record ServerStatistics(Instant serverStartTime, Instant currentTime, Duration uptime, 
                           int totalEndpoints, int registeredClients, Duration avgAccessTokenTtl, 
                           Duration avgRefreshTokenTtl) {}
}

/**
 * Server configuration holder
 */
class ServerConfiguration {
    String issuer = "https://auth.example.com";
    EndpointConfiguration endpoints = new EndpointConfiguration();
    ServerTokenSettings tokenSettings = new ServerTokenSettings();
    SecurityPolicies securityPolicies = new SecurityPolicies();
    CorsSettings corsSettings = new CorsSettings();
    Instant startTime = Instant.now();
    Instant lastModified = Instant.now();
}

/**
 * Endpoint configuration
 */
class EndpointConfiguration {
    String authorizationEndpoint = "/oauth2/v1/authorize";
    String tokenEndpoint = "/oauth2/v1/token";
    String introspectionEndpoint = "/oauth2/v1/introspect";
    String revocationEndpoint = "/oauth2/v1/revoke";
    String jwksUri = "/oauth2/v1/jwks";
    String userInfoEndpoint = "/oauth2/v1/userinfo";
    String registrationEndpoint = "/oauth2/v1/register";
    String deviceAuthorizationEndpoint = "/oauth2/v1/device_authorization";
    
    int getEndpointCount() {
        return 8;
    }
}

/**
 * Server token settings
 */
class ServerTokenSettings {
    Duration accessTokenTtl = Duration.ofHours(1);
    Duration refreshTokenTtl = Duration.ofDays(30);
    Duration authorizationCodeTtl = Duration.ofMinutes(5);
    Duration deviceCodeTtl = Duration.ofMinutes(10);
    boolean reuseRefreshTokens = false;
    String tokenFormat = "self-contained"; // or "reference"
}

/**
 * Security policies
 */
class SecurityPolicies {
    boolean requirePkceForPublicClients = true;
    boolean requireConsentForThirdParty = true;
    boolean allowRefreshTokenRotation = true;
    boolean allowPlainPkce = false;
    int maxAuthorizationCodeAge = 300; // seconds
    int maxDeviceCodeAge = 600; // seconds
}

/**
 * CORS settings
 */
class CorsSettings {
    List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:8080");
    List<String> allowedMethods = List.of("GET", "POST");
    List<String> allowedHeaders = List.of("Authorization", "Content-Type");
    boolean allowCredentials = true;
    long maxAge = 3600;
}

/**
 * REST controller for server settings endpoints
 */
@RestController
@RequestMapping("/api/server-settings")
class AuthorizationServerSettingsController {

    private final AuthorizationServerSettingsService settingsService = new AuthorizationServerSettingsService();

    @GetMapping
    public ResponseEntity<AuthorizationServerSettingsService.ServerSettingsInfo> getServerSettings() {
        AuthorizationServerSettingsService.ServerSettingsInfo settings = settingsService.getServerSettings();
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/endpoints")
    public ResponseEntity<EndpointConfiguration> getEndpoints() {
        EndpointConfiguration endpoints = settingsService.getEndpointConfiguration();
        return ResponseEntity.ok(endpoints);
    }

    @GetMapping("/token-settings")
    public ResponseEntity<ServerTokenSettings> getTokenSettings() {
        ServerTokenSettings tokenSettings = settingsService.getTokenSettings();
        return ResponseEntity.ok(tokenSettings);
    }

    @PutMapping("/token-settings")
    public ResponseEntity<ServerTokenSettings> updateTokenSettings(
            @RequestBody AuthorizationServerSettingsService.TokenSettingsUpdate update) {
        ServerTokenSettings updated = settingsService.updateTokenSettings(update);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/security-policies")
    public ResponseEntity<SecurityPolicies> getSecurityPolicies() {
        SecurityPolicies policies = settingsService.getSecurityPolicies();
        return ResponseEntity.ok(policies);
    }

    @PutMapping("/security-policies")
    public ResponseEntity<SecurityPolicies> updateSecurityPolicies(
            @RequestBody AuthorizationServerSettingsService.SecurityPolicyUpdate update) {
        SecurityPolicies updated = settingsService.updateSecurityPolicies(update);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/.well-known/oauth-authorization-server")
    public ResponseEntity<AuthorizationServerSettingsService.ServerMetadata> getServerMetadata() {
        AuthorizationServerSettingsService.ServerMetadata metadata = settingsService.getServerMetadata();
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/health")
    public ResponseEntity<AuthorizationServerSettingsService.ServerHealth> getHealth() {
        AuthorizationServerSettingsService.ServerHealth health = settingsService.getServerHealth();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/statistics")
    public ResponseEntity<AuthorizationServerSettingsService.ServerStatistics> getStatistics() {
        AuthorizationServerSettingsService.ServerStatistics stats = settingsService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        PatternInfo info = new PatternInfo(
            "Authorization Server Settings Pattern",
            "Comprehensive configuration of Spring Authorization Server",
            "1.0",
            List.of(
                "Custom endpoint paths",
                "Token lifetime configuration",
                "Refresh token rotation",
                "PKCE requirements",
                "Security policies",
                "CORS configuration",
                "OAuth 2.0 Discovery metadata"
            ),
            List.of(
                "Custom domain and endpoint paths",
                "Multi-tenant authorization server",
                "Specific token lifetime requirements",
                "Enhanced security policies"
            )
        );
        return ResponseEntity.ok(info);
    }

    record PatternInfo(String name, String description, String version, List<String> features, List<String> useCases) {}
}
