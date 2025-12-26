package com.example.authorizationserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Consent Page Pattern
 * 
 * Demonstrates custom user consent page implementation for OAuth 2.1 authorization flows.
 * 
 * Consent Page Features:
 * - Custom consent UI with scope descriptions
 * - Previously granted consents tracking
 * - Scope selection and approval workflow
 * - Consent revocation
 * - Dynamic scope descriptions based on client
 * - Consent audit trail
 * 
 * Key Capabilities:
 * - User-friendly consent interface
 * - Granular scope approval (user can approve/deny individual scopes)
 * - Remember consent decisions
 * - Client information display (name, description, logo)
 * - Consent history and management
 * 
 * Use Cases:
 * - Authorization Code flow requiring user consent
 * - Third-party application access to user data
 * - Granular permission management
 * - GDPR compliance with explicit consent
 * 
 * Security Considerations:
 * - Consent must be explicitly granted by user (no pre-approved consents)
 * - Scope descriptions must be clear and understandable
 * - Users must be able to review and revoke consents at any time
 * - Consent decisions should be audited for compliance
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class ConsentPagePattern {

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
                .requestMatchers("/api/consent/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * Registered client repository with consent requirements
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        // Web application requiring consent
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
            .scope("read")
            .scope("write")
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(1))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(false)
                .build())
            .build();

        // Third-party application
        RegisteredClient thirdPartyApp = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("third-party-app")
            .clientSecret("{noop}third-party-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("https://thirdparty.example.com/callback")
            .scope("openid")
            .scope("profile")
            .scope("photos.read")
            .scope("photos.write")
            .scope("contacts.read")
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .build())
            .build();

        return new InMemoryRegisteredClientRepository(webApp, thirdPartyApp);
    }
}

/**
 * Consent page service
 */
@RestController
@RequestMapping("/api/consent")
class ConsentPageService {

    private final Map<String, UserConsent> consentStore = new ConcurrentHashMap<>();
    private final Map<String, ClientMetadata> clientMetadata = new ConcurrentHashMap<>();
    private final List<ConsentAuditEntry> auditTrail = new ArrayList<>();

    public ConsentPageService() {
        // Initialize client metadata
        clientMetadata.put("web-app", new ClientMetadata(
            "web-app",
            "My Web Application",
            "A trusted web application for managing your account",
            "https://example.com/logo.png",
            "https://example.com/privacy",
            "https://example.com/terms"
        ));

        clientMetadata.put("third-party-app", new ClientMetadata(
            "third-party-app",
            "Third Party Photo Service",
            "A third-party service for photo management and sharing",
            "https://thirdparty.example.com/logo.png",
            "https://thirdparty.example.com/privacy",
            "https://thirdparty.example.com/terms"
        ));

        // Initialize scope descriptions
        initializeScopeDescriptions();
    }

    /**
     * Get consent page data for user
     */
    public ConsentPageData getConsentPage(String userId, String clientId, Set<String> requestedScopes) {
        ClientMetadata metadata = clientMetadata.getOrDefault(clientId, 
            new ClientMetadata(clientId, clientId, "Unknown application", null, null, null));
        
        // Get previously granted consents
        String consentKey = generateConsentKey(userId, clientId);
        UserConsent existingConsent = consentStore.get(consentKey);
        Set<String> previouslyGrantedScopes = existingConsent != null ? 
            existingConsent.grantedScopes : Collections.emptySet();
        
        // Build scope details
        List<ScopeDetail> scopeDetails = requestedScopes.stream()
            .map(scope -> new ScopeDetail(
                scope,
                getScopeDescription(scope),
                getScopeCategory(scope),
                previouslyGrantedScopes.contains(scope),
                isScopeRequired(scope)
            ))
            .collect(Collectors.toList());
        
        return new ConsentPageData(
            metadata,
            scopeDetails,
            previouslyGrantedScopes,
            existingConsent != null ? existingConsent.grantedAt : null
        );
    }

    /**
     * Grant consent for scopes
     */
    public ConsentDecision grantConsent(String userId, String clientId, Set<String> approvedScopes, 
                                       Set<String> deniedScopes, boolean rememberDecision) {
        String consentKey = generateConsentKey(userId, clientId);
        
        UserConsent consent = new UserConsent(
            userId,
            clientId,
            approvedScopes,
            deniedScopes,
            Instant.now(),
            rememberDecision
        );
        
        if (rememberDecision) {
            consentStore.put(consentKey, consent);
        }
        
        // Audit trail
        auditTrail.add(new ConsentAuditEntry(
            Instant.now(),
            userId,
            clientId,
            "GRANTED",
            approvedScopes,
            deniedScopes
        ));
        
        return new ConsentDecision(
            true,
            approvedScopes,
            deniedScopes,
            "Consent granted successfully",
            consent.grantedAt
        );
    }

    /**
     * Deny consent
     */
    public ConsentDecision denyConsent(String userId, String clientId, Set<String> requestedScopes) {
        // Audit trail
        auditTrail.add(new ConsentAuditEntry(
            Instant.now(),
            userId,
            clientId,
            "DENIED",
            Collections.emptySet(),
            requestedScopes
        ));
        
        return new ConsentDecision(
            false,
            Collections.emptySet(),
            requestedScopes,
            "Consent denied by user",
            Instant.now()
        );
    }

    /**
     * Get all consents for user
     */
    public List<UserConsent> getUserConsents(String userId) {
        return consentStore.values().stream()
            .filter(consent -> consent.userId.equals(userId))
            .sorted(Comparator.comparing(c -> c.grantedAt).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Revoke consent
     */
    public boolean revokeConsent(String userId, String clientId) {
        String consentKey = generateConsentKey(userId, clientId);
        UserConsent removed = consentStore.remove(consentKey);
        
        if (removed != null) {
            // Audit trail
            auditTrail.add(new ConsentAuditEntry(
                Instant.now(),
                userId,
                clientId,
                "REVOKED",
                removed.grantedScopes,
                Collections.emptySet()
            ));
            return true;
        }
        
        return false;
    }

    /**
     * Get consent audit trail
     */
    public List<ConsentAuditEntry> getAuditTrail(String userId, int limit) {
        return auditTrail.stream()
            .filter(entry -> entry.userId.equals(userId))
            .sorted(Comparator.comparing(ConsentAuditEntry::timestamp).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get consent statistics
     */
    public ConsentStatistics getStatistics() {
        long totalConsents = consentStore.size();
        long activeConsents = consentStore.values().stream()
            .filter(c -> c.grantedScopes != null && !c.grantedScopes.isEmpty())
            .count();
        
        long totalAudits = auditTrail.size();
        long grantedCount = auditTrail.stream().filter(e -> "GRANTED".equals(e.action)).count();
        long deniedCount = auditTrail.stream().filter(e -> "DENIED".equals(e.action)).count();
        long revokedCount = auditTrail.stream().filter(e -> "REVOKED".equals(e.action)).count();
        
        Map<String, Long> scopeUsage = consentStore.values().stream()
            .flatMap(c -> c.grantedScopes.stream())
            .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        
        return new ConsentStatistics(
            totalConsents,
            activeConsents,
            totalAudits,
            grantedCount,
            deniedCount,
            revokedCount,
            scopeUsage
        );
    }

    // Helper methods
    
    private String generateConsentKey(String userId, String clientId) {
        return userId + ":" + clientId;
    }

    private Map<String, String> scopeDescriptions = new HashMap<>();
    
    private void initializeScopeDescriptions() {
        scopeDescriptions.put("openid", "Verify your identity");
        scopeDescriptions.put("profile", "Access your basic profile information (name, picture)");
        scopeDescriptions.put("email", "Access your email address");
        scopeDescriptions.put("read", "Read your data");
        scopeDescriptions.put("write", "Create and modify your data");
        scopeDescriptions.put("photos.read", "View your photos");
        scopeDescriptions.put("photos.write", "Upload and delete your photos");
        scopeDescriptions.put("contacts.read", "Access your contacts list");
    }

    private String getScopeDescription(String scope) {
        return scopeDescriptions.getOrDefault(scope, "Access " + scope + " resources");
    }

    private String getScopeCategory(String scope) {
        if (scope.equals("openid")) return "Authentication";
        if (scope.equals("profile") || scope.equals("email")) return "Profile Information";
        if (scope.startsWith("photos")) return "Photos";
        if (scope.startsWith("contacts")) return "Contacts";
        return "General";
    }

    private boolean isScopeRequired(String scope) {
        return scope.equals("openid");
    }

    record ConsentPageData(ClientMetadata client, List<ScopeDetail> scopes, 
                          Set<String> previouslyGrantedScopes, Instant previousConsentDate) {}
    
    record ScopeDetail(String scope, String description, String category, 
                      boolean previouslyGranted, boolean required) {}
    
    record ConsentDecision(boolean approved, Set<String> approvedScopes, Set<String> deniedScopes, 
                          String message, Instant timestamp) {}
    
    record ConsentStatistics(long totalConsents, long activeConsents, long totalAudits, 
                            long grantedCount, long deniedCount, long revokedCount, 
                            Map<String, Long> scopeUsage) {}
}

/**
 * User consent information
 */
class UserConsent {
    String userId;
    String clientId;
    Set<String> grantedScopes;
    Set<String> deniedScopes;
    Instant grantedAt;
    boolean remembered;

    UserConsent(String userId, String clientId, Set<String> grantedScopes, 
                Set<String> deniedScopes, Instant grantedAt, boolean remembered) {
        this.userId = userId;
        this.clientId = clientId;
        this.grantedScopes = grantedScopes;
        this.deniedScopes = deniedScopes;
        this.grantedAt = grantedAt;
        this.remembered = remembered;
    }
}

/**
 * Client metadata for consent page display
 */
record ClientMetadata(String clientId, String name, String description, 
                     String logoUrl, String privacyPolicyUrl, String termsOfServiceUrl) {}

/**
 * Consent audit entry
 */
record ConsentAuditEntry(Instant timestamp, String userId, String clientId, 
                        String action, Set<String> approvedScopes, Set<String> deniedScopes) {}

/**
 * REST controller for consent page endpoints
 */
@RestController
@RequestMapping("/api/consent")
class ConsentPageController {

    private final ConsentPageService consentService = new ConsentPageService();

    @GetMapping("/page")
    public ResponseEntity<ConsentPageService.ConsentPageData> getConsentPage(
            @RequestParam String userId,
            @RequestParam String clientId,
            @RequestParam Set<String> scopes) {
        ConsentPageService.ConsentPageData pageData = 
            consentService.getConsentPage(userId, clientId, scopes);
        return ResponseEntity.ok(pageData);
    }

    @PostMapping("/grant")
    public ResponseEntity<ConsentPageService.ConsentDecision> grantConsent(
            @RequestBody ConsentGrantRequest request) {
        ConsentPageService.ConsentDecision decision = consentService.grantConsent(
            request.userId(),
            request.clientId(),
            request.approvedScopes(),
            request.deniedScopes(),
            request.rememberDecision()
        );
        return ResponseEntity.ok(decision);
    }

    @PostMapping("/deny")
    public ResponseEntity<ConsentPageService.ConsentDecision> denyConsent(
            @RequestBody ConsentDenyRequest request) {
        ConsentPageService.ConsentDecision decision = 
            consentService.denyConsent(request.userId(), request.clientId(), request.requestedScopes());
        return ResponseEntity.ok(decision);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserConsent>> getUserConsents(@PathVariable String userId) {
        List<UserConsent> consents = consentService.getUserConsents(userId);
        return ResponseEntity.ok(consents);
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<RevokeResponse> revokeConsent(
            @RequestParam String userId,
            @RequestParam String clientId) {
        boolean revoked = consentService.revokeConsent(userId, clientId);
        return ResponseEntity.ok(new RevokeResponse(userId, clientId, revoked, Instant.now()));
    }

    @GetMapping("/audit/{userId}")
    public ResponseEntity<List<ConsentAuditEntry>> getAuditTrail(
            @PathVariable String userId,
            @RequestParam(defaultValue = "50") int limit) {
        List<ConsentAuditEntry> trail = consentService.getAuditTrail(userId, limit);
        return ResponseEntity.ok(trail);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ConsentPageService.ConsentStatistics> getStatistics() {
        ConsentPageService.ConsentStatistics stats = consentService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        PatternInfo info = new PatternInfo(
            "Consent Page Pattern",
            "Custom user consent page for OAuth 2.1 authorization flows",
            "1.0",
            List.of(
                "Custom consent UI with scope descriptions",
                "Previously granted consents tracking",
                "Granular scope approval",
                "Consent revocation",
                "Dynamic scope descriptions",
                "Consent audit trail"
            ),
            List.of(
                "Authorization Code flow requiring user consent",
                "Third-party application access",
                "Granular permission management",
                "GDPR compliance"
            )
        );
        return ResponseEntity.ok(info);
    }

    record ConsentGrantRequest(String userId, String clientId, Set<String> approvedScopes, 
                              Set<String> deniedScopes, boolean rememberDecision) {}
    record ConsentDenyRequest(String userId, String clientId, Set<String> requestedScopes) {}
    record RevokeResponse(String userId, String clientId, boolean revoked, Instant timestamp) {}
    record PatternInfo(String name, String description, String version, List<String> features, List<String> useCases) {}
}
