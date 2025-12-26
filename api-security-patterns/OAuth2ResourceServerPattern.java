package com.example.security.resourceserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OAuth2 Resource Server Pattern
 * 
 * Demonstrates:
 * - Resource server configuration
 * - JWT token validation
 * - Scope-based authorization
 * - Custom JWT claims processing
 * - Stateless authentication
 * - Method-level security
 * 
 * Resource Server:
 * - Protects API resources
 * - Validates JWT access tokens
 * - Extracts authorities from tokens
 * - Enforces authorization rules
 * 
 * Dependencies:
 * - spring-boot-starter-oauth2-resource-server
 * - spring-security-oauth2-jose
 */

@SpringBootApplication
public class OAuth2ResourceServerPattern {
    public static void main(String[] args) {
        SpringApplication.run(OAuth2ResourceServerPattern.class, args);
    }
}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class ResourceServerSecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/user/**").hasAuthority("SCOPE_user.read")
                .requestMatchers("/api/admin/**").hasAuthority("SCOPE_admin")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        // In production, use actual JWK Set URI from authorization server
        String jwkSetUri = "https://auth-server.example.com/.well-known/jwks.json";
        
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        // Custom JWT validation
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer("https://auth-server.example.com"),
            new CustomJwtValidator()
        ));
        
        return jwtDecoder;
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = 
            new JwtGrantedAuthoritiesConverter();
        
        // Configure scope prefix
        grantedAuthoritiesConverter.setAuthorityPrefix("SCOPE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = 
            new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        
        return jwtAuthenticationConverter;
    }
}

/**
 * Custom JWT validator for additional validation rules
 */
class CustomJwtValidator implements OAuth2TokenValidator<Jwt> {
    
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<OAuth2Error> errors = new ArrayList<>();
        
        // Example: Validate custom claim
        String tenantId = jwt.getClaimAsString("tenant_id");
        if (tenantId == null) {
            errors.add(new OAuth2Error("invalid_token", 
                "Token must contain tenant_id claim", null));
        }
        
        // Example: Validate audience
        List<String> audience = jwt.getAudience();
        if (audience == null || !audience.contains("my-api")) {
            errors.add(new OAuth2Error("invalid_token", 
                "Token audience is invalid", null));
        }
        
        return errors.isEmpty() ? 
            OAuth2TokenValidatorResult.success() :
            OAuth2TokenValidatorResult.failure(errors);
    }
}

@RestController
@RequestMapping("/api")
class ResourceServerController {
    
    @GetMapping("/public/health")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "Public endpoint - no authentication required"
        ));
    }
    
    @GetMapping("/user/profile")
    public ResponseEntity<UserProfile> getUserProfile(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        UserProfile profile = new UserProfile(
            jwt.getSubject(),
            jwt.getClaimAsString("email"),
            jwt.getClaimAsString("name"),
            jwt.getClaimAsString("tenant_id"),
            authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );
        
        return ResponseEntity.ok(profile);
    }
    
    @GetMapping("/user/data")
    public ResponseEntity<Map<String, Object>> getUserData(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", jwt.getSubject());
        data.put("data", Arrays.asList("item1", "item2", "item3"));
        data.put("timestamp", new Date());
        
        return ResponseEntity.ok(data);
    }
    
    @GetMapping("/admin/users")
    public ResponseEntity<List<Map<String, String>>> getUsers(Authentication authentication) {
        // Only accessible with admin scope
        List<Map<String, String>> users = Arrays.asList(
            Map.of("id", "1", "name", "User 1", "email", "user1@example.com"),
            Map.of("id", "2", "name", "User 2", "email", "user2@example.com"),
            Map.of("id", "3", "name", "User 3", "email", "user3@example.com")
        );
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/token-info")
    public ResponseEntity<TokenDetails> getTokenInfo(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        TokenDetails details = new TokenDetails(
            jwt.getSubject(),
            jwt.getIssuer().toString(),
            jwt.getAudience(),
            jwt.getIssuedAt(),
            jwt.getExpiresAt(),
            jwt.getClaims(),
            authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );
        
        return ResponseEntity.ok(details);
    }
    
    @GetMapping("/scopes")
    public ResponseEntity<ScopeInfo> getScopes(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        List<String> scopes = new ArrayList<>();
        String scopeClaim = jwt.getClaimAsString("scope");
        if (scopeClaim != null) {
            scopes = Arrays.asList(scopeClaim.split(" "));
        }
        
        List<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        
        ScopeInfo scopeInfo = new ScopeInfo(scopes, authorities);
        
        return ResponseEntity.ok(scopeInfo);
    }
}

class UserProfile {
    private String userId;
    private String email;
    private String name;
    private String tenantId;
    private List<String> authorities;
    
    public UserProfile(String userId, String email, String name, 
                      String tenantId, List<String> authorities) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.tenantId = tenantId;
        this.authorities = authorities;
    }
    
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getTenantId() { return tenantId; }
    public List<String> getAuthorities() { return authorities; }
}

class TokenDetails {
    private String subject;
    private String issuer;
    private List<String> audience;
    private Object issuedAt;
    private Object expiresAt;
    private Map<String, Object> claims;
    private List<String> authorities;
    
    public TokenDetails(String subject, String issuer, List<String> audience,
                       Object issuedAt, Object expiresAt, Map<String, Object> claims,
                       List<String> authorities) {
        this.subject = subject;
        this.issuer = issuer;
        this.audience = audience;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.claims = claims;
        this.authorities = authorities;
    }
    
    public String getSubject() { return subject; }
    public String getIssuer() { return issuer; }
    public List<String> getAudience() { return audience; }
    public Object getIssuedAt() { return issuedAt; }
    public Object getExpiresAt() { return expiresAt; }
    public Map<String, Object> getClaims() { return claims; }
    public List<String> getAuthorities() { return authorities; }
}

class ScopeInfo {
    private List<String> scopes;
    private List<String> authorities;
    
    public ScopeInfo(List<String> scopes, List<String> authorities) {
        this.scopes = scopes;
        this.authorities = authorities;
    }
    
    public List<String> getScopes() { return scopes; }
    public List<String> getAuthorities() { return authorities; }
}
