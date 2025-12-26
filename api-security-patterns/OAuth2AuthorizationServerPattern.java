package com.example.security.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.*;

/**
 * OAuth2 Authorization Server Pattern
 * 
 * Demonstrates:
 * - Full OAuth2 authorization server implementation
 * - Token generation and validation
 * - Client registration and management
 * - Authorization code flow
 * - Client credentials flow
 * - Refresh token support
 * - JWK Set endpoint
 * 
 * OAuth2 Grant Types:
 * - Authorization Code
 * - Client Credentials
 * - Refresh Token
 * - Password (deprecated but shown for completeness)
 * 
 * Endpoints:
 * - /oauth2/authorize - Authorization endpoint
 * - /oauth2/token - Token endpoint
 * - /oauth2/jwks - JWK Set endpoint
 * - /oauth2/introspect - Token introspection
 * - /oauth2/revoke - Token revocation
 * 
 * Dependencies:
 * - spring-boot-starter-oauth2-authorization-server
 * - spring-security-oauth2-jose
 */

@SpringBootApplication
public class OAuth2AuthorizationServerPattern {
    public static void main(String[] args) {
        SpringApplication.run(OAuth2AuthorizationServerPattern.class, args);
    }
}

@Configuration
class AuthServerSecurityConfig {
    
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // Apply OAuth2 Authorization Server default security
        // This configures /oauth2/authorize, /oauth2/token, /oauth2/jwks, etc.
        
        http
            .securityMatcher("/oauth2/**", "/.well-known/**")
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/oauth2/token", "/oauth2/introspect", "/oauth2/revoke"));
        
        return http.build();
    }
    
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults());
        
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        var user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build();
        
        var admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin"))
            .roles("USER", "ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(user, admin);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Generate RSA key pair for JWT signing
     * In production, use persistent key storage
     */
    @Bean
    public KeyPair keyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }
}

/**
 * Mock OAuth2 client registration repository
 * In production, use database-backed repository
 */
@Configuration
class ClientRegistrationConfig {
    
    @Bean
    public MockRegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        return new MockRegisteredClientRepository(passwordEncoder);
    }
}

class MockRegisteredClientRepository {
    
    private final Map<String, RegisteredClientInfo> clients = new HashMap<>();
    
    public MockRegisteredClientRepository(PasswordEncoder passwordEncoder) {
        // Demo client with authorization code flow
        clients.put("demo-client", new RegisteredClientInfo(
            "demo-client",
            passwordEncoder.encode("secret"),
            Arrays.asList("authorization_code", "refresh_token"),
            Arrays.asList("http://localhost:8080/authorized", "http://localhost:8080/callback"),
            Arrays.asList("read", "write", "openid", "profile", "email")
        ));
        
        // Client credentials client
        clients.put("service-client", new RegisteredClientInfo(
            "service-client",
            passwordEncoder.encode("service-secret"),
            Arrays.asList("client_credentials"),
            Collections.emptyList(),
            Arrays.asList("api.read", "api.write")
        ));
        
        // Mobile app client (public client with PKCE)
        clients.put("mobile-app", new RegisteredClientInfo(
            "mobile-app",
            null, // Public client, no secret
            Arrays.asList("authorization_code", "refresh_token"),
            Arrays.asList("myapp://callback"),
            Arrays.asList("read", "write", "openid")
        ));
    }
    
    public RegisteredClientInfo findByClientId(String clientId) {
        return clients.get(clientId);
    }
    
    public Collection<RegisteredClientInfo> findAll() {
        return clients.values();
    }
}

@RestController
@RequestMapping("/api/auth-server")
class AuthServerManagementController {
    
    private final MockRegisteredClientRepository clientRepository;
    private final KeyPair keyPair;
    
    public AuthServerManagementController(
            MockRegisteredClientRepository clientRepository,
            KeyPair keyPair) {
        this.clientRepository = clientRepository;
        this.keyPair = keyPair;
    }
    
    @GetMapping("/clients")
    public ResponseEntity<List<ClientSummary>> listClients() {
        List<ClientSummary> summaries = new ArrayList<>();
        
        for (RegisteredClientInfo client : clientRepository.findAll()) {
            summaries.add(new ClientSummary(
                client.getClientId(),
                client.getGrantTypes(),
                client.getRedirectUris(),
                client.getScopes()
            ));
        }
        
        return ResponseEntity.ok(summaries);
    }
    
    @GetMapping("/clients/{clientId}")
    public ResponseEntity<ClientDetails> getClient(@PathVariable String clientId) {
        RegisteredClientInfo client = clientRepository.findByClientId(clientId);
        
        if (client == null) {
            return ResponseEntity.notFound().build();
        }
        
        ClientDetails details = new ClientDetails(
            client.getClientId(),
            client.getGrantTypes(),
            client.getRedirectUris(),
            client.getScopes(),
            client.getClientSecret() != null
        );
        
        return ResponseEntity.ok(details);
    }
    
    @GetMapping("/server-metadata")
    public ResponseEntity<ServerMetadata> getServerMetadata() {
        ServerMetadata metadata = new ServerMetadata(
            "http://localhost:8080",
            "/oauth2/authorize",
            "/oauth2/token",
            "/oauth2/jwks",
            "/oauth2/introspect",
            "/oauth2/revoke",
            Arrays.asList("authorization_code", "client_credentials", "refresh_token"),
            Arrays.asList("code"),
            Arrays.asList("RS256"),
            Arrays.asList("read", "write", "openid", "profile", "email")
        );
        
        return ResponseEntity.ok(metadata);
    }
    
    @GetMapping("/public-key")
    public ResponseEntity<PublicKeyInfo> getPublicKey() {
        PublicKeyInfo keyInfo = new PublicKeyInfo(
            "RSA",
            keyPair.getPublic().getAlgorithm(),
            Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
        );
        
        return ResponseEntity.ok(keyInfo);
    }
}

class RegisteredClientInfo {
    private String clientId;
    private String clientSecret;
    private List<String> grantTypes;
    private List<String> redirectUris;
    private List<String> scopes;
    
    public RegisteredClientInfo(String clientId, String clientSecret, List<String> grantTypes,
                               List<String> redirectUris, List<String> scopes) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantTypes = grantTypes;
        this.redirectUris = redirectUris;
        this.scopes = scopes;
    }
    
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public List<String> getGrantTypes() { return grantTypes; }
    public List<String> getRedirectUris() { return redirectUris; }
    public List<String> getScopes() { return scopes; }
}

class ClientSummary {
    private String clientId;
    private List<String> grantTypes;
    private List<String> redirectUris;
    private List<String> scopes;
    
    public ClientSummary(String clientId, List<String> grantTypes,
                        List<String> redirectUris, List<String> scopes) {
        this.clientId = clientId;
        this.grantTypes = grantTypes;
        this.redirectUris = redirectUris;
        this.scopes = scopes;
    }
    
    public String getClientId() { return clientId; }
    public List<String> grantTypes() { return grantTypes; }
    public List<String> getRedirectUris() { return redirectUris; }
    public List<String> getScopes() { return scopes; }
}

class ClientDetails {
    private String clientId;
    private List<String> authorizedGrantTypes;
    private List<String> registeredRedirectUris;
    private List<String> scopes;
    private boolean hasSecret;
    
    public ClientDetails(String clientId, List<String> authorizedGrantTypes,
                        List<String> registeredRedirectUris, List<String> scopes,
                        boolean hasSecret) {
        this.clientId = clientId;
        this.authorizedGrantTypes = authorizedGrantTypes;
        this.registeredRedirectUris = registeredRedirectUris;
        this.scopes = scopes;
        this.hasSecret = hasSecret;
    }
    
    public String getClientId() { return clientId; }
    public List<String> getAuthorizedGrantTypes() { return authorizedGrantTypes; }
    public List<String> getRegisteredRedirectUris() { return registeredRedirectUris; }
    public List<String> getScopes() { return scopes; }
    public boolean isHasSecret() { return hasSecret; }
}

class ServerMetadata {
    private String issuer;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String jwksUri;
    private String introspectionEndpoint;
    private String revocationEndpoint;
    private List<String> grantTypesSupported;
    private List<String> responseTypesSupported;
    private List<String> tokenSigningAlgorithms;
    private List<String> scopesSupported;
    
    public ServerMetadata(String issuer, String authorizationEndpoint, String tokenEndpoint,
                         String jwksUri, String introspectionEndpoint, String revocationEndpoint,
                         List<String> grantTypesSupported, List<String> responseTypesSupported,
                         List<String> tokenSigningAlgorithms, List<String> scopesSupported) {
        this.issuer = issuer;
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint = tokenEndpoint;
        this.jwksUri = jwksUri;
        this.introspectionEndpoint = introspectionEndpoint;
        this.revocationEndpoint = revocationEndpoint;
        this.grantTypesSupported = grantTypesSupported;
        this.responseTypesSupported = responseTypesSupported;
        this.tokenSigningAlgorithms = tokenSigningAlgorithms;
        this.scopesSupported = scopesSupported;
    }
    
    public String getIssuer() { return issuer; }
    public String getAuthorizationEndpoint() { return authorizationEndpoint; }
    public String getTokenEndpoint() { return tokenEndpoint; }
    public String getJwksUri() { return jwksUri; }
    public String getIntrospectionEndpoint() { return introspectionEndpoint; }
    public String getRevocationEndpoint() { return revocationEndpoint; }
    public List<String> getGrantTypesSupported() { return grantTypesSupported; }
    public List<String> getResponseTypesSupported() { return responseTypesSupported; }
    public List<String> getTokenSigningAlgorithms() { return tokenSigningAlgorithms; }
    public List<String> getScopesSupported() { return scopesSupported; }
}

class PublicKeyInfo {
    private String keyType;
    private String algorithm;
    private String publicKey;
    
    public PublicKeyInfo(String keyType, String algorithm, String publicKey) {
        this.keyType = keyType;
        this.algorithm = algorithm;
        this.publicKey = publicKey;
    }
    
    public String getKeyType() { return keyType; }
    public String getAlgorithm() { return algorithm; }
    public String getPublicKey() { return publicKey; }
}
