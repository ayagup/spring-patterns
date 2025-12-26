package com.example.embeddedserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * SSL/TLS Configuration Pattern
 * 
 * Demonstrates configuring SSL/TLS for embedded servers,
 * including certificates, protocols, and HTTPS setup.
 * 
 * Key Concepts:
 * - SSL configuration
 * - Keystore setup
 * - Protocol selection
 * - Certificate management
 * - HTTPS enforcement
 * 
 * Use Cases:
 * - Secure connections
 * - Certificate-based auth
 * - TLS protocol selection
 * - Mutual TLS
 * - Production security
 */
@SpringBootApplication
public class SslConfigurationPattern {

    public static void main(String[] args) {
        SpringApplication.run(SslConfigurationPattern.class, args);
    }
}

/**
 * SSL configuration
 */
@Configuration
class SslConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> sslCustomizer() {
        return factory -> {
            Ssl ssl = new Ssl();
            
            // Keystore configuration
            ssl.setEnabled(true);
            ssl.setKeyStore("classpath:keystore.p12");
            ssl.setKeyStorePassword("changeit");
            ssl.setKeyStoreType("PKCS12");
            ssl.setKeyAlias("tomcat");
            
            // Protocol and cipher suites
            ssl.setProtocol("TLS");
            ssl.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            ssl.setCiphers(new String[]{
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256"
            });
            
            // Client authentication (mutual TLS)
            ssl.setClientAuth(Ssl.ClientAuth.NONE); // or NEED, WANT
            
            // Trust store (for mutual TLS)
            // ssl.setTrustStore("classpath:truststore.p12");
            // ssl.setTrustStorePassword("changeit");
            
            factory.setSsl(ssl);
            factory.setPort(8443);
        };
    }
}

/**
 * Service providing SSL information
 */
@Service
class SslInfoService {

    public Map<String, Object> getSslInfo() {
        return Map.of(
                "enabled", true,
                "port", 8443,
                "protocol", "TLS",
                "enabledProtocols", new String[]{"TLSv1.2", "TLSv1.3"},
                "keystoreType", "PKCS12",
                "clientAuth", "NONE"
        );
    }

    public Map<String, Object> getSecurityConfig() {
        return Map.of(
                "https", true,
                "http2", true,
                "mutualTls", false,
                "strictTransportSecurity", true
        );
    }
}

/**
 * Controller exposing SSL information
 */
@RestController
class SslInfoController {

    private final SslInfoService sslInfoService;

    public SslInfoController(SslInfoService sslInfoService) {
        this.sslInfoService = sslInfoService;
    }

    @GetMapping("/ssl/info")
    public Map<String, Object> getSslInfo() {
        return sslInfoService.getSslInfo();
    }

    @GetMapping("/ssl/security")
    public Map<String, Object> getSecurityConfig() {
        return sslInfoService.getSecurityConfig();
    }
}

/**
 * Documentation:
 * 
 * SSL Configuration (application.properties):
 * 
 * # Enable HTTPS
 * server.port=8443
 * server.ssl.enabled=true
 * 
 * # Keystore
 * server.ssl.key-store=classpath:keystore.p12
 * server.ssl.key-store-password=changeit
 * server.ssl.key-store-type=PKCS12
 * server.ssl.key-alias=tomcat
 * 
 * # Protocol
 * server.ssl.protocol=TLS
 * server.ssl.enabled-protocols=TLSv1.2,TLSv1.3
 * 
 * # Cipher suites
 * server.ssl.ciphers=TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
 * 
 * # Client auth (mutual TLS)
 * server.ssl.client-auth=none
 * server.ssl.trust-store=classpath:truststore.p12
 * server.ssl.trust-store-password=changeit
 * 
 * Generate Self-Signed Certificate:
 * keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 \
 *   -storetype PKCS12 -keystore keystore.p12 -validity 3650 \
 *   -storepass changeit
 * 
 * Generate Certificate Signing Request (CSR):
 * keytool -certreq -alias tomcat -keystore keystore.p12 \
 *   -file server.csr -storepass changeit
 * 
 * Import Signed Certificate:
 * keytool -importcert -alias tomcat -keystore keystore.p12 \
 *   -file server.crt -storepass changeit
 * 
 * HTTP to HTTPS Redirect:
 * @Bean
 * public ServletWebServerFactory servletContainer() {
 *     TomcatServletWebServerFactory tomcat = 
 *         new TomcatServletWebServerFactory() {
 *         @Override
 *         protected void postProcessContext(Context context) {
 *             SecurityConstraint constraint = new SecurityConstraint();
 *             constraint.setUserConstraint("CONFIDENTIAL");
 *             SecurityCollection collection = new SecurityCollection();
 *             collection.addPattern("/*");
 *             constraint.addCollection(collection);
 *             context.addConstraint(constraint);
 *         }
 *     };
 *     tomcat.addAdditionalTomcatConnectors(redirectConnector());
 *     return tomcat;
 * }
 * 
 * private Connector redirectConnector() {
 *     Connector connector = new Connector(Http11NioProtocol.class.getName());
 *     connector.setScheme("http");
 *     connector.setPort(8080);
 *     connector.setSecure(false);
 *     connector.setRedirectPort(8443);
 *     return connector;
 * }
 * 
 * Mutual TLS (mTLS):
 * server.ssl.client-auth=need
 * server.ssl.trust-store=classpath:truststore.p12
 * server.ssl.trust-store-password=changeit
 * 
 * Best Practices:
 * - Use TLSv1.2 or higher
 * - Disable weak cipher suites
 * - Use strong key sizes (2048+ bit)
 * - Keep certificates updated
 * - Use proper certificate chain
 * - Enable HSTS header
 * - Use certificate pinning in clients
 * 
 * Production Considerations:
 * - Use certificates from trusted CA
 * - Implement certificate rotation
 * - Monitor certificate expiration
 * - Use separate keystores for dev/prod
 * - Implement proper key management
 * - Enable security headers
 * - Use HTTP/2 for performance
 */
