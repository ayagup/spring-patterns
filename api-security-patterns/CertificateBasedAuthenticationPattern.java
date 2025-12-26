package com.example.apisecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Certificate-Based Authentication Pattern (mTLS)
 * 
 * Demonstrates mutual TLS (mTLS) authentication where clients authenticate
 * using X.509 certificates instead of passwords.
 * 
 * Features:
 * - X.509 client certificate authentication
 * - Mutual TLS (mTLS) support
 * - Certificate DN (Distinguished Name) extraction
 * - Certificate metadata validation
 * - Pre-authenticated security filter
 * - Certificate-based user details service
 * 
 * Key Components:
 * - X509AuthenticationFilter: Extracts client certificates
 * - SubjectDnX509PrincipalExtractor: Extracts principal from certificate DN
 * - PreAuthenticatedAuthenticationProvider: Validates pre-authenticated requests
 * - SSL/TLS configuration for mutual authentication
 * 
 * Setup Requirements:
 * - Server keystore with server certificate
 * - Truststore with trusted CA certificates
 * - Client certificates signed by trusted CA
 */
@SpringBootApplication
public class CertificateBasedAuthenticationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(CertificateBasedAuthenticationPattern.class, args);
    }
    
    /**
     * Security Configuration for Certificate Authentication
     */
    @Configuration
    @EnableWebSecurity
    public static class CertificateSecurityConfig {
        
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers("/api/public/**").permitAll()
                    .antMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                .and()
                .x509()
                    .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                    .userDetailsService(x509UserDetailsService())
                .and()
                .authenticationProvider(preAuthenticatedAuthenticationProvider());
            
            return http.build();
        }
        
        /**
         * X.509 Certificate User Details Service
         * Maps certificate DN to user details
         */
        @Bean
        public UserDetailsService x509UserDetailsService() {
            return new CertificateUserDetailsService();
        }
        
        /**
         * Pre-authenticated Authentication Provider
         */
        @Bean
        public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
            PreAuthenticatedAuthenticationProvider provider = 
                new PreAuthenticatedAuthenticationProvider();
            provider.setPreAuthenticatedUserDetailsService(
                new PreAuthenticatedGrantedAuthoritiesUserDetailsService()
            );
            return provider;
        }
        
        /**
         * X.509 Authentication Filter
         */
        @Bean
        public X509AuthenticationFilter x509AuthenticationFilter() {
            X509AuthenticationFilter filter = new X509AuthenticationFilter();
            filter.setPrincipalExtractor(new SubjectDnX509PrincipalExtractor());
            return filter;
        }
    }
    
    /**
     * Custom User Details Service for Certificate Authentication
     * Maps certificate common name (CN) to user details
     */
    public static class CertificateUserDetailsService implements UserDetailsService {
        
        private final Map<String, CertificateUserInfo> certificateUsers = new HashMap<>();
        
        public CertificateUserDetailsService() {
            // Initialize certificate users
            certificateUsers.put("client1", new CertificateUserInfo(
                "client1", "CN=client1,O=Example Corp", "ROLE_USER"
            ));
            certificateUsers.put("admin-client", new CertificateUserInfo(
                "admin-client", "CN=admin-client,O=Example Corp", "ROLE_ADMIN"
            ));
            certificateUsers.put("service-client", new CertificateUserInfo(
                "service-client", "CN=service-client,O=Example Corp", "ROLE_SERVICE"
            ));
        }
        
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            CertificateUserInfo userInfo = certificateUsers.get(username);
            
            if (userInfo == null) {
                throw new UsernameNotFoundException("Certificate user not found: " + username);
            }
            
            return User.builder()
                .username(userInfo.getUsername())
                .password("") // No password needed for certificate auth
                .authorities(AuthorityUtils.createAuthorityList(userInfo.getRole()))
                .build();
        }
        
        public CertificateUserInfo getCertificateUserInfo(String username) {
            return certificateUsers.get(username);
        }
    }
    
    /**
     * Certificate User Information
     */
    public static class CertificateUserInfo {
        private String username;
        private String distinguishedName;
        private String role;
        private LocalDateTime registeredAt;
        
        public CertificateUserInfo(String username, String distinguishedName, String role) {
            this.username = username;
            this.distinguishedName = distinguishedName;
            this.role = role;
            this.registeredAt = LocalDateTime.now();
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getDistinguishedName() { return distinguishedName; }
        public String getRole() { return role; }
        public LocalDateTime getRegisteredAt() { return registeredAt; }
    }
    
    /**
     * Certificate Information Service
     */
    @Service
    public static class CertificateService {
        
        /**
         * Extract certificate information from request
         */
        public CertificateInfo extractCertificateInfo(HttpServletRequest request) {
            X509Certificate[] certs = (X509Certificate[]) 
                request.getAttribute("javax.servlet.request.X509Certificate");
            
            if (certs != null && certs.length > 0) {
                X509Certificate cert = certs[0];
                return new CertificateInfo(
                    cert.getSubjectDN().getName(),
                    cert.getIssuerDN().getName(),
                    cert.getSerialNumber().toString(),
                    cert.getNotBefore().toInstant().toString(),
                    cert.getNotAfter().toInstant().toString()
                );
            }
            
            return null;
        }
        
        /**
         * Validate certificate chain
         */
        public boolean validateCertificateChain(X509Certificate[] chain) {
            if (chain == null || chain.length == 0) {
                return false;
            }
            
            // In production, implement proper certificate chain validation
            // Check validity dates, revocation status, etc.
            try {
                for (X509Certificate cert : chain) {
                    cert.checkValidity();
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
    
    /**
     * Certificate Information Model
     */
    public static class CertificateInfo {
        private String subject;
        private String issuer;
        private String serialNumber;
        private String validFrom;
        private String validTo;
        
        public CertificateInfo(String subject, String issuer, String serialNumber,
                              String validFrom, String validTo) {
            this.subject = subject;
            this.issuer = issuer;
            this.serialNumber = serialNumber;
            this.validFrom = validFrom;
            this.validTo = validTo;
        }
        
        // Getters
        public String getSubject() { return subject; }
        public String getIssuer() { return issuer; }
        public String getSerialNumber() { return serialNumber; }
        public String getValidFrom() { return validFrom; }
        public String getValidTo() { return validTo; }
    }
    
    /**
     * REST Controller for Certificate-Based Authentication
     */
    @RestController
    @RequestMapping("/api")
    public static class CertificateAuthController {
        
        private final CertificateService certificateService;
        private final CertificateUserDetailsService userDetailsService;
        
        public CertificateAuthController(CertificateService certificateService,
                                        CertificateUserDetailsService userDetailsService) {
            this.certificateService = certificateService;
            this.userDetailsService = userDetailsService;
        }
        
        /**
         * Public endpoint - no certificate required
         */
        @GetMapping("/public/info")
        public Map<String, String> publicInfo() {
            return Map.of(
                "message", "Public endpoint - certificate not required",
                "timestamp", LocalDateTime.now().toString(),
                "authType", "None"
            );
        }
        
        /**
         * Get certificate details
         */
        @GetMapping("/cert/details")
        public Map<String, Object> getCertificateDetails(
                HttpServletRequest request,
                Authentication authentication) {
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            
            CertificateInfo certInfo = certificateService.extractCertificateInfo(request);
            if (certInfo != null) {
                response.put("certificate", certInfo);
            }
            
            CertificateUserInfo userInfo = 
                userDetailsService.getCertificateUserInfo(authentication.getName());
            if (userInfo != null) {
                response.put("userInfo", userInfo);
            }
            
            response.put("timestamp", LocalDateTime.now());
            
            return response;
        }
        
        /**
         * Protected resource endpoint
         */
        @GetMapping("/protected/resource")
        public Map<String, Object> getProtectedResource(Authentication authentication) {
            Map<String, Object> resource = new HashMap<>();
            resource.put("message", "Access granted via certificate authentication");
            resource.put("authenticatedAs", authentication.getName());
            resource.put("authorities", authentication.getAuthorities());
            resource.put("resourceData", "Sensitive certificate-protected data");
            resource.put("timestamp", LocalDateTime.now());
            
            return resource;
        }
        
        /**
         * Admin endpoint - requires ADMIN role
         */
        @GetMapping("/admin/certificates")
        public Map<String, Object> listCertificates(Authentication authentication) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Certificate registry");
            response.put("admin", authentication.getName());
            response.put("registeredCertificates", Arrays.asList(
                "CN=client1,O=Example Corp",
                "CN=admin-client,O=Example Corp",
                "CN=service-client,O=Example Corp"
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return response;
        }
        
        /**
         * Validate certificate chain
         */
        @GetMapping("/cert/validate")
        public Map<String, Object> validateCertificate(HttpServletRequest request) {
            X509Certificate[] certs = (X509Certificate[]) 
                request.getAttribute("javax.servlet.request.X509Certificate");
            
            boolean valid = certificateService.validateCertificateChain(certs);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", valid);
            response.put("certificateCount", certs != null ? certs.length : 0);
            response.put("timestamp", LocalDateTime.now());
            
            if (certs != null && certs.length > 0) {
                CertificateInfo certInfo = certificateService.extractCertificateInfo(request);
                response.put("certificate", certInfo);
            }
            
            return response;
        }
    }
    
    /**
     * SSL/TLS Configuration
     * Configure server to require client certificates
     */
    @Configuration
    public static class TlsConfig {
        
        @Bean
        public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainer() {
            return factory -> {
                // In production, configure:
                // - server.ssl.key-store (server certificate)
                // - server.ssl.key-store-password
                // - server.ssl.trust-store (trusted CA certificates)
                // - server.ssl.trust-store-password
                // - server.ssl.client-auth=need (require client cert)
                
                factory.addConnectorCustomizers(connector -> {
                    // Custom SSL/TLS connector configuration
                    // This would be configured via application.properties in production
                });
            };
        }
    }
}
