package com.example.apisecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * SAML Integration Pattern
 * 
 * Demonstrates SAML 2.0 Service Provider configuration for enterprise SSO.
 * SAML (Security Assertion Markup Language) enables Single Sign-On across domains.
 * 
 * Features:
 * - SAML 2.0 Service Provider (SP) configuration
 * - Identity Provider (IdP) integration
 * - SAML assertion validation
 * - SSO (Single Sign-On)
 * - SLO (Single Logout)
 * - Metadata exchange
 * 
 * Key Components:
 * - Relying Party Registration (SP configuration)
 * - SAML authentication filter
 * - Assertion consumer service
 * - Metadata endpoint
 */
@SpringBootApplication
public class SAMLIntegrationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(SAMLIntegrationPattern.class, args);
    }
    
    @Configuration
    @EnableWebSecurity
    public static class SAMLSecurityConfig {
        
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .authorizeRequests(auth -> auth
                    .antMatchers("/", "/error", "/saml/**").permitAll()
                    .anyRequest().authenticated()
                )
                .saml2Login(saml2 -> saml2
                    .loginProcessingUrl("/login/saml2/sso/{registrationId}")
                )
                .saml2Logout(logout -> logout
                    .logoutUrl("/logout/saml2/slo")
                );
            
            return http.build();
        }
        
        @Bean
        public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
            RelyingPartyRegistration registration = RelyingPartyRegistration
                .withRegistrationId("example-idp")
                .entityId("https://example.com/saml/sp")
                .assertionConsumerServiceLocation(
                    "https://example.com/login/saml2/sso/example-idp"
                )
                .assertionConsumerServiceBinding(Saml2MessageBinding.POST)
                .singleLogoutServiceLocation(
                    "https://example.com/logout/saml2/slo"
                )
                .singleLogoutServiceBinding(Saml2MessageBinding.POST)
                .assertingPartyDetails(party -> party
                    .entityId("https://idp.example.com")
                    .singleSignOnServiceLocation(
                        "https://idp.example.com/saml/sso"
                    )
                    .singleSignOnServiceBinding(Saml2MessageBinding.REDIRECT)
                    .wantAuthnRequestsSigned(false)
                )
                .build();
            
            return new InMemoryRelyingPartyRegistrationRepository(registration);
        }
    }
    
    @Controller
    public static class SAMLController {
        
        @GetMapping("/")
        public String index() {
            return "SAML Integration - Redirect to /saml/login";
        }
        
        @GetMapping("/saml/user")
        @ResponseBody
        public Map<String, Object> getUserInfo(
                org.springframework.security.core.Authentication authentication) {
            Map<String, Object> response = new HashMap<>();
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            response.put("authenticated", authentication.isAuthenticated());
            response.put("samlAssertion", "SAML Assertion Details Here");
            response.put("timestamp", LocalDateTime.now());
            return response;
        }
        
        @GetMapping("/saml/metadata")
        @ResponseBody
        public Map<String, String> getMetadata() {
            return Map.of(
                "entityId", "https://example.com/saml/sp",
                "acsUrl", "https://example.com/login/saml2/sso/example-idp",
                "sloUrl", "https://example.com/logout/saml2/slo",
                "type", "Service Provider Metadata"
            );
        }
    }
}
