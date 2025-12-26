package com.example.propertysource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.vault.annotation.VaultPropertySource;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Vault Property Source Pattern
 * 
 * Demonstrates integrating with HashiCorp Vault for secure
 * secrets management and dynamic property loading.
 * 
 * Key Concepts:
 * - Spring Cloud Vault
 * - @VaultPropertySource
 * - Secret management
 * - Dynamic secrets
 * - Token authentication
 * - KV secrets engine
 * 
 * Use Cases:
 * - Database credentials
 * - API keys and tokens
 * - Certificates and keys
 * - Encrypted configuration
 * - Dynamic secret rotation
 */
@SpringBootApplication
public class VaultPropertySourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(VaultPropertySourcePattern.class, args);
    }
}

/**
 * Vault configuration
 * 
 * bootstrap.yml:
 * spring:
 *   cloud:
 *     vault:
 *       host: localhost
 *       port: 8200
 *       scheme: http
 *       authentication: TOKEN
 *       token: s.your-vault-token
 *       kv:
 *         enabled: true
 *         backend: secret
 */
@Configuration
@VaultPropertySource(value = "secret/myapp", renewal = VaultPropertySource.Renewal.RENEW)
@VaultPropertySource(value = "secret/myapp/${spring.profiles.active}", renewal = VaultPropertySource.Renewal.RENEW)
class VaultConfiguration {

    /**
     * VaultTemplate for programmatic access
     */
    @Bean
    public VaultTemplate vaultTemplate() {
        // In real app, this is auto-configured by Spring Cloud Vault
        return null; // Placeholder
    }
}

/**
 * Service for Vault operations
 */
@org.springframework.stereotype.Service
class VaultService {

    private final ConfigurableEnvironment environment;
    private final VaultTemplate vaultTemplate;

    public VaultService(ConfigurableEnvironment environment, VaultTemplate vaultTemplate) {
        this.environment = environment;
        this.vaultTemplate = vaultTemplate;
    }

    /**
     * Get secret from Vault via Environment
     */
    public String getVaultSecret(String key) {
        return environment.getProperty(key);
    }

    /**
     * Read secret from Vault programmatically
     */
    public Map<String, Object> readSecret(String path) {
        if (vaultTemplate == null) {
            // Simulated response
            return Map.of(
                    "username", "dbuser",
                    "password", "encrypted_password",
                    "source", "vault",
                    "path", path
            );
        }
        
        VaultResponse response = vaultTemplate.read(path);
        return response != null ? response.getData() : new HashMap<>();
    }

    /**
     * Write secret to Vault
     */
    public void writeSecret(String path, Map<String, Object> secrets) {
        if (vaultTemplate != null) {
            vaultTemplate.write(path, secrets);
        }
        System.out.println("Secret written to: " + path);
    }

    /**
     * Delete secret from Vault
     */
    public void deleteSecret(String path) {
        if (vaultTemplate != null) {
            vaultTemplate.delete(path);
        }
        System.out.println("Secret deleted from: " + path);
    }

    /**
     * Get database credentials from Vault
     */
    public Map<String, String> getDatabaseCredentials() {
        return Map.of(
                "url", getVaultSecret("spring.datasource.url"),
                "username", getVaultSecret("spring.datasource.username"),
                "password", getVaultSecret("spring.datasource.password")
        );
    }

    /**
     * Get all Vault-sourced properties
     */
    public Map<String, Object> getAllVaultProperties() {
        Map<String, Object> properties = new HashMap<>();
        
        environment.getPropertySources().stream()
                .filter(ps -> ps.getName().contains("vault") || 
                             ps.getName().contains("secret/"))
                .forEach(ps -> {
                    if (ps instanceof org.springframework.core.env.EnumerablePropertySource) {
                        org.springframework.core.env.EnumerablePropertySource<?> eps = 
                                (org.springframework.core.env.EnumerablePropertySource<?>) ps;
                        
                        for (String propertyName : eps.getPropertyNames()) {
                            properties.put(propertyName, eps.getProperty(propertyName));
                        }
                    }
                });
        
        return properties;
    }
}

/**
 * Controller to expose Vault operations (with caution)
 */
@RestController
@RequestMapping("/api/vault")
class VaultController {

    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping("/secret")
    public Map<String, Object> getSecret(String path) {
        return vaultService.readSecret(path);
    }

    @GetMapping("/property")
    public Map<String, String> getProperty(String key) {
        String value = vaultService.getVaultSecret(key);
        return Map.of(
                "key", key,
                "value", value != null ? "***masked***" : "Not found",
                "source", "vault"
        );
    }

    @GetMapping("/db-credentials")
    public Map<String, String> getDatabaseCredentials() {
        Map<String, String> creds = vaultService.getDatabaseCredentials();
        // Mask sensitive values
        return Map.of(
                "url", creds.getOrDefault("url", "N/A"),
                "username", creds.getOrDefault("username", "N/A"),
                "password", "***masked***"
        );
    }

    // WARNING: Do not expose this in production
    @GetMapping("/properties/all")
    public Map<String, String> getAllProperties() {
        return Map.of("message", "Vault properties access restricted");
    }
}

/**
 * Documentation:
 * 
 * HashiCorp Vault:
 * - Industry-standard secrets management
 * - Dynamic secrets generation
 * - Encryption as a service
 * - Identity-based access
 * - Lease management
 * - Secret rotation
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-vault-config</artifactId>
 * </dependency>
 * 
 * Bootstrap Configuration (bootstrap.yml):
 * spring:
 *   cloud:
 *     vault:
 *       host: vault.example.com
 *       port: 8200
 *       scheme: https
 *       authentication: TOKEN
 *       token: ${VAULT_TOKEN}
 *       kv:
 *         enabled: true
 *         backend: secret
 *         default-context: myapp
 *         profile-separator: '/'
 * 
 * Authentication Methods:
 * 
 * 1. Token:
 *    spring.cloud.vault.authentication=TOKEN
 *    spring.cloud.vault.token=s.your-token
 * 
 * 2. AppRole:
 *    spring.cloud.vault.authentication=APPROLE
 *    spring.cloud.vault.app-role.role-id=your-role-id
 *    spring.cloud.vault.app-role.secret-id=your-secret-id
 * 
 * 3. AWS:
 *    spring.cloud.vault.authentication=AWS_EC2
 *    spring.cloud.vault.aws-ec2.role=my-role
 * 
 * 4. Kubernetes:
 *    spring.cloud.vault.authentication=KUBERNETES
 *    spring.cloud.vault.kubernetes.role=my-role
 * 
 * Vault KV Secrets Engine:
 * - Version 1 (KV v1): Simple key-value store
 * - Version 2 (KV v2): Versioned secrets with metadata
 * 
 * Configure KV version:
 * spring.cloud.vault.kv.backend-version=2
 * 
 * Secret Paths:
 * - Default: secret/{application}
 * - Profile-specific: secret/{application}/{profile}
 * - Generic: secret/application
 * 
 * Custom paths:
 * spring.cloud.vault.generic.enabled=true
 * spring.cloud.vault.generic.application-name=myapp
 * 
 * Database Secrets Engine:
 * spring.cloud.vault.database.enabled=true
 * spring.cloud.vault.database.role=my-role
 * spring.cloud.vault.database.backend=database
 * 
 * Generates dynamic DB credentials:
 * spring.datasource.username=${spring.cloud.vault.database.username}
 * spring.datasource.password=${spring.cloud.vault.database.password}
 * 
 * Lease Renewal:
 * spring.cloud.vault.config.lifecycle.enabled=true
 * spring.cloud.vault.config.lifecycle.min-renewal=10s
 * spring.cloud.vault.config.lifecycle.expiry-threshold=1m
 * 
 * @VaultPropertySource:
 * @VaultPropertySource("secret/myapp")
 * @VaultPropertySource(value = "secret/myapp/db", 
 *                      renewal = Renewal.RENEW,
 *                      ignoreSecretNotFound = false)
 * 
 * Programmatic Access:
 * @Autowired
 * VaultTemplate vaultTemplate;
 * 
 * // Read
 * VaultResponse response = vaultTemplate.read("secret/data/myapp");
 * String password = (String) response.getData().get("password");
 * 
 * // Write
 * Map<String, Object> data = Map.of("password", "secret123");
 * vaultTemplate.write("secret/data/myapp", data);
 * 
 * // Delete
 * vaultTemplate.delete("secret/data/myapp");
 * 
 * Transit Encryption (Encryption as a Service):
 * spring.cloud.vault.transit.enabled=true
 * spring.cloud.vault.transit.key-name=my-key
 * 
 * @Autowired
 * VaultTransitOperations transitOperations;
 * 
 * String ciphertext = transitOperations.encrypt("my-key", "plaintext");
 * String plaintext = transitOperations.decrypt("my-key", ciphertext);
 * 
 * PKI Secrets Engine (Certificates):
 * spring.cloud.vault.pki.enabled=true
 * spring.cloud.vault.pki.role=my-role
 * spring.cloud.vault.pki.backend=pki
 * 
 * Generates SSL certificates dynamically
 * 
 * Consul Integration:
 * spring.cloud.vault.consul.enabled=true
 * spring.cloud.vault.consul.role=my-role
 * 
 * AWS Integration:
 * spring.cloud.vault.aws.enabled=true
 * spring.cloud.vault.aws.role=my-role
 * 
 * Generates temporary AWS credentials
 * 
 * Fail-Fast:
 * spring.cloud.vault.fail-fast=true
 * - Application fails to start if can't connect to Vault
 * 
 * SSL/TLS Configuration:
 * spring.cloud.vault.ssl.trust-store=classpath:truststore.jks
 * spring.cloud.vault.ssl.trust-store-password=changeit
 * spring.cloud.vault.ssl.key-store=classpath:keystore.jks
 * spring.cloud.vault.ssl.key-store-password=changeit
 * 
 * Namespace (Vault Enterprise):
 * spring.cloud.vault.namespace=my-namespace
 * 
 * Best Practices:
 * - Never commit Vault tokens to version control
 * - Use short-lived tokens
 * - Enable lease renewal
 * - Use AppRole or cloud-native auth in production
 * - Rotate secrets regularly
 * - Use dynamic secrets when possible
 * - Monitor secret access via Vault audit logs
 * - Use namespaces for multi-tenancy
 * - Implement proper error handling
 * - Use transit encryption for sensitive data
 * 
 * Security:
 * - Enable TLS in production
 * - Restrict network access to Vault
 * - Use fine-grained policies
 * - Enable audit logging
 * - Regularly rotate root tokens
 * - Use MFA for sensitive operations
 * 
 * Testing:
 * @SpringBootTest
 * @TestPropertySource(properties = {
 *     "spring.cloud.vault.enabled=false"
 * })
 * - Disable Vault for unit tests
 * 
 * Common Issues:
 * - Connection refused: Check Vault server status
 * - Permission denied: Verify token policies
 * - Secret not found: Check path and KV version
 * - Lease expired: Configure renewal properly
 * 
 * Monitoring:
 * - /actuator/health shows Vault connection status
 * - /actuator/env shows Vault property sources
 * - Vault audit logs for secret access
 * 
 * Alternatives:
 * - AWS Secrets Manager
 * - Azure Key Vault
 * - Google Secret Manager
 * - Kubernetes Secrets
 */
