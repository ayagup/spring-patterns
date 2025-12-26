package com.example.apisecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * OpenID Connect Pattern
 * 
 * Demonstrates OpenID Connect (OIDC) authentication built on top of OAuth 2.0.
 * OIDC adds an identity layer providing authentication and user information.
 * 
 * Features:
 * - OpenID Connect authentication
 * - ID Token validation
 * - UserInfo endpoint integration
 * - Claims extraction
 * - Multiple OIDC provider support
 * - JWT ID tokens
 * 
 * Key Components:
 * - OIDC login configuration
 * - ID Token with user claims
 * - UserInfo endpoint client
 * - OidcUser principal
 */
@SpringBootApplication
public class OpenIDConnectPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(OpenIDConnectPattern.class, args);
    }
    
    @Configuration
    @EnableWebSecurity
    public static class OIDCSecurityConfig {
        
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .authorizeRequests(auth -> auth
                    .antMatchers("/", "/public/**").permitAll()
                    .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                    .loginPage("/oauth2/authorization/oidc-client")
                    .userInfoEndpoint(userInfo -> userInfo
                        .oidcUserService(oidcUserService())
                    )
                );
            
            return http.build();
        }
        
        @Bean
        public ClientRegistrationRepository clientRegistrationRepository() {
            List<ClientRegistration> registrations = Arrays.asList(
                googleClientRegistration(),
                customOidcProvider()
            );
            return new InMemoryClientRegistrationRepository(registrations);
        }
        
        private ClientRegistration googleClientRegistration() {
            return CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId("google-client-id")
                .clientSecret("google-client-secret")
                .scope("openid", "profile", "email")
                .build();
        }
        
        private ClientRegistration customOidcProvider() {
            return ClientRegistration.withRegistrationId("oidc-client")
                .clientId("oidc-client-id")
                .clientSecret("oidc-client-secret")
                .scope("openid", "profile", "email")
                .authorizationUri("https://idp.example.com/oauth2/authorize")
                .tokenUri("https://idp.example.com/oauth2/token")
                .userInfoUri("https://idp.example.com/oauth2/userinfo")
                .jwkSetUri("https://idp.example.com/oauth2/jwks")
                .issuerUri("https://idp.example.com")
                .userNameAttributeName("sub")
                .clientName("Custom OIDC Provider")
                .authorizationGrantType(
                    org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE
                )
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build();
        }
        
        @Bean
        public org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService oidcUserService() {
            return new org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService();
        }
    }
    
    @RestController
    @RequestMapping("/api")
    public static class OIDCController {
        
        @GetMapping("/public/info")
        public Map<String, String> publicInfo() {
            return Map.of(
                "message", "OpenID Connect Authentication",
                "description", "OIDC adds identity layer to OAuth 2.0",
                "timestamp", LocalDateTime.now().toString()
            );
        }
        
        @GetMapping("/user/profile")
        public Map<String, Object> getUserProfile(@AuthenticationPrincipal OidcUser oidcUser) {
            Map<String, Object> profile = new HashMap<>();
            
            if (oidcUser != null) {
                profile.put("subject", oidcUser.getSubject());
                profile.put("email", oidcUser.getEmail());
                profile.put("name", oidcUser.getFullName());
                profile.put("givenName", oidcUser.getGivenName());
                profile.put("familyName", oidcUser.getFamilyName());
                profile.put("claims", oidcUser.getClaims());
                profile.put("authorities", oidcUser.getAuthorities());
            }
            
            profile.put("timestamp", LocalDateTime.now());
            return profile;
        }
        
        @GetMapping("/user/idtoken")
        public Map<String, Object> getIdToken(@AuthenticationPrincipal OidcUser oidcUser) {
            Map<String, Object> response = new HashMap<>();
            
            if (oidcUser != null && oidcUser.getIdToken() != null) {
                OidcIdToken idToken = oidcUser.getIdToken();
                response.put("tokenValue", "[REDACTED]");
                response.put("issuedAt", idToken.getIssuedAt());
                response.put("expiresAt", idToken.getExpiresAt());
                response.put("issuer", idToken.getIssuer());
                response.put("subject", idToken.getSubject());
                response.put("audience", idToken.getAudience());
                response.put("claims", idToken.getClaims());
            }
            
            response.put("timestamp", LocalDateTime.now());
            return response;
        }
        
        @GetMapping("/user/claims")
        public Map<String, Object> getUserClaims(@AuthenticationPrincipal OidcUser oidcUser,
                                                  Authentication authentication) {
            Map<String, Object> response = new HashMap<>();
            response.put("username", authentication.getName());
            response.put("authenticated", authentication.isAuthenticated());
            
            if (oidcUser != null) {
                response.put("standardClaims", Map.of(
                    "sub", oidcUser.getSubject(),
                    "email", oidcUser.getEmail(),
                    "email_verified", oidcUser.getEmailVerified(),
                    "name", oidcUser.getFullName(),
                    "given_name", oidcUser.getGivenName(),
                    "family_name", oidcUser.getFamilyName()
                ));
                response.put("allClaims", oidcUser.getClaims());
            }
            
            response.put("timestamp", LocalDateTime.now());
            return response;
        }
    }
}
