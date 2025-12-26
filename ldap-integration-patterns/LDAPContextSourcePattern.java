package com.example.ldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.bind.annotation.*;

import javax.naming.Context;
import java.time.Instant;
import java.util.*;

/**
 * LDAP Context Source Pattern
 * 
 * Demonstrates configuration and management of LDAP context source for directory server connections.
 * 
 * Context Source Features:
 * - Connection to LDAP servers (OpenLDAP, Active Directory, etc.)
 * - Connection pooling configuration
 * - SSL/TLS support for secure connections
 * - Authentication methods (simple, DIGEST-MD5, GSSAPI)
 * - Referral handling
 * - Base DN configuration
 * 
 * Configuration Options:
 * - Server URLs (ldap://, ldaps://)
 * - Base distinguished name (DN)
 * - User credentials for binding
 * - Connection timeout settings
 * - Pool size and validation
 * 
 * Use Cases:
 * - Connecting to corporate LDAP servers
 * - Active Directory integration
 * - Multi-server failover configuration
 * - Secure LDAP connections (LDAPS)
 * - Connection pooling for performance
 * 
 * Security Considerations:
 * - Always use LDAPS (ldaps://) for production
 * - Store credentials securely (encrypted properties, vault)
 * - Configure connection timeouts to prevent hanging
 * - Enable certificate validation for SSL
 * - Use connection pooling to limit resource usage
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class LDAPContextSourcePattern {

    /**
     * Primary LDAP context source
     */
    @Bean
    public LdapContextSource primaryLdapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        
        // Server configuration
        contextSource.setUrl("ldap://ldap-primary.example.com:389");
        contextSource.setBase("dc=example,dc=com");
        
        // Authentication
        contextSource.setUserDn("cn=admin,dc=example,dc=com");
        contextSource.setPassword("admin-password");
        
        // Connection pooling
        contextSource.setPooled(true);
        
        // Base environment properties
        Map<String, Object> baseEnvironment = new HashMap<>();
        baseEnvironment.put("java.naming.ldap.version", "3");
        baseEnvironment.put(Context.REFERRAL, "follow");
        baseEnvironment.put("java.naming.ldap.attributes.binary", "objectGUID");
        
        // Connection timeout (milliseconds)
        baseEnvironment.put("com.sun.jndi.ldap.connect.timeout", "5000");
        baseEnvironment.put("com.sun.jndi.ldap.read.timeout", "10000");
        
        contextSource.setBaseEnvironmentProperties(baseEnvironment);
        
        // Pooling configuration
        Map<String, Object> pooling = new HashMap<>();
        pooling.put("java.naming.ldap.pool.maxsize", "10");
        pooling.put("java.naming.ldap.pool.prefsize", "5");
        pooling.put("java.naming.ldap.pool.timeout", "300000");
        pooling.put("java.naming.ldap.pool.protocol", "plain ssl");
        pooling.put("java.naming.ldap.pool.authentication", "simple");
        pooling.put("java.naming.ldap.pool.debug", "off");
        
        contextSource.setPoolingContextSourceProperties(pooling);
        
        return contextSource;
    }

    /**
     * Secure LDAP context source with SSL/TLS
     */
    @Bean
    public LdapContextSource secureLdapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        
        // Secure server configuration
        contextSource.setUrl("ldaps://ldap-secure.example.com:636");
        contextSource.setBase("dc=secure,dc=example,dc=com");
        
        // Authentication
        contextSource.setUserDn("cn=admin,dc=secure,dc=example,dc=com");
        contextSource.setPassword("secure-admin-password");
        
        // Connection pooling
        contextSource.setPooled(true);
        
        // SSL configuration
        Map<String, Object> baseEnvironment = new HashMap<>();
        baseEnvironment.put(Context.SECURITY_PROTOCOL, "ssl");
        baseEnvironment.put("java.naming.ldap.factory.socket", 
            "javax.net.ssl.SSLSocketFactory");
        
        // Optional: Disable certificate validation (NOT for production)
        // baseEnvironment.put("java.naming.ldap.ssl.trust.all", "true");
        
        contextSource.setBaseEnvironmentProperties(baseEnvironment);
        
        return contextSource;
    }

    /**
     * Active Directory context source
     */
    @Bean
    public LdapContextSource activeDirectoryContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        
        // Active Directory configuration
        contextSource.setUrl("ldap://ad.example.com:389");
        contextSource.setBase("dc=ad,dc=example,dc=com");
        
        // AD authentication (domain\username format)
        contextSource.setUserDn("DOMAIN\\admin");
        contextSource.setPassword("ad-admin-password");
        
        // Connection pooling
        contextSource.setPooled(true);
        
        // Active Directory specific settings
        Map<String, Object> baseEnvironment = new HashMap<>();
        baseEnvironment.put(Context.REFERRAL, "follow");
        baseEnvironment.put("java.naming.ldap.attributes.binary", "objectGUID objectSid");
        
        // Enable paging for large result sets
        baseEnvironment.put("java.naming.ldap.pageSize", "1000");
        
        contextSource.setBaseEnvironmentProperties(baseEnvironment);
        
        return contextSource;
    }

    /**
     * Failover LDAP context source with multiple servers
     */
    @Bean
    public LdapContextSource failoverLdapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        
        // Multiple server URLs (space-separated for failover)
        contextSource.setUrls(new String[]{
            "ldap://ldap1.example.com:389",
            "ldap://ldap2.example.com:389",
            "ldap://ldap3.example.com:389"
        });
        contextSource.setBase("dc=example,dc=com");
        
        // Authentication
        contextSource.setUserDn("cn=admin,dc=example,dc=com");
        contextSource.setPassword("admin-password");
        
        // Connection pooling
        contextSource.setPooled(true);
        
        // Failover configuration
        Map<String, Object> baseEnvironment = new HashMap<>();
        baseEnvironment.put("com.sun.jndi.ldap.connect.timeout", "3000");
        baseEnvironment.put("com.sun.jndi.ldap.read.timeout", "5000");
        
        contextSource.setBaseEnvironmentProperties(baseEnvironment);
        
        return contextSource;
    }
}

/**
 * LDAP context source management service
 */
@RestController
@RequestMapping("/api/ldap-context")
class LDAPContextSourceService {

    private final LdapContextSource primarySource;
    private final LdapContextSource secureSource;
    private final LdapContextSource adSource;
    private final LdapContextSource failoverSource;
    
    private final Map<String, ConnectionStats> connectionStats = new HashMap<>();

    public LDAPContextSourceService(
            LdapContextSource primaryLdapContextSource,
            LdapContextSource secureLdapContextSource,
            LdapContextSource activeDirectoryContextSource,
            LdapContextSource failoverLdapContextSource) {
        this.primarySource = primaryLdapContextSource;
        this.secureSource = secureLdapContextSource;
        this.adSource = activeDirectoryContextSource;
        this.failoverSource = failoverLdapContextSource;
        
        // Initialize statistics
        connectionStats.put("primary", new ConnectionStats("primary"));
        connectionStats.put("secure", new ConnectionStats("secure"));
        connectionStats.put("ad", new ConnectionStats("ad"));
        connectionStats.put("failover", new ConnectionStats("failover"));
    }

    /**
     * Test connection to primary LDAP server
     */
    public ConnectionTestResult testPrimaryConnection() {
        return testConnection(primarySource, "primary");
    }

    /**
     * Test connection to secure LDAP server
     */
    public ConnectionTestResult testSecureConnection() {
        return testConnection(secureSource, "secure");
    }

    /**
     * Test connection to Active Directory
     */
    public ConnectionTestResult testADConnection() {
        return testConnection(adSource, "ad");
    }

    /**
     * Test connection to failover LDAP servers
     */
    public ConnectionTestResult testFailoverConnection() {
        return testConnection(failoverSource, "failover");
    }

    /**
     * Test connection helper
     */
    private ConnectionTestResult testConnection(LdapContextSource source, String name) {
        ConnectionStats stats = connectionStats.get(name);
        Instant startTime = Instant.now();
        
        try {
            javax.naming.ldap.LdapContext context = source.getContext(
                source.getUserDn(), 
                source.getPassword()
            );
            
            if (context != null) {
                context.close();
                long responseTime = Instant.now().toEpochMilli() - startTime.toEpochMilli();
                stats.recordSuccess(responseTime);
                
                return new ConnectionTestResult(
                    true,
                    "Connection successful",
                    source.getUrls()[0],
                    source.getBaseLdapPathAsString(),
                    responseTime
                );
            }
        } catch (Exception e) {
            stats.recordFailure();
            return new ConnectionTestResult(
                false,
                "Connection failed: " + e.getMessage(),
                source.getUrls()[0],
                source.getBaseLdapPathAsString(),
                -1L
            );
        }
        
        return new ConnectionTestResult(
            false,
            "Connection failed: Unknown error",
            source.getUrls()[0],
            source.getBaseLdapPathAsString(),
            -1L
        );
    }

    /**
     * Get configuration details for a context source
     */
    public ContextSourceConfig getConfiguration(String sourceName) {
        LdapContextSource source = getSourceByName(sourceName);
        if (source == null) {
            return null;
        }
        
        return new ContextSourceConfig(
            sourceName,
            Arrays.asList(source.getUrls()),
            source.getBaseLdapPathAsString(),
            source.getUserDn(),
            source.isPooled(),
            source.getAnonymousReadOnly(),
            source.getBaseEnvironmentProperties()
        );
    }

    /**
     * Get connection statistics
     */
    public ConnectionStats getStatistics(String sourceName) {
        return connectionStats.get(sourceName);
    }

    /**
     * Get all connection statistics
     */
    public Map<String, ConnectionStats> getAllStatistics() {
        return new HashMap<>(connectionStats);
    }

    /**
     * Reset statistics for a source
     */
    public boolean resetStatistics(String sourceName) {
        ConnectionStats stats = connectionStats.get(sourceName);
        if (stats != null) {
            stats.reset();
            return true;
        }
        return false;
    }

    /**
     * Test all connections
     */
    public Map<String, ConnectionTestResult> testAllConnections() {
        Map<String, ConnectionTestResult> results = new HashMap<>();
        results.put("primary", testPrimaryConnection());
        results.put("secure", testSecureConnection());
        results.put("ad", testADConnection());
        results.put("failover", testFailoverConnection());
        return results;
    }

    /**
     * Get health status for all sources
     */
    public HealthStatus getHealthStatus() {
        Map<String, Boolean> sourceHealth = new HashMap<>();
        sourceHealth.put("primary", testPrimaryConnection().success());
        sourceHealth.put("secure", testSecureConnection().success());
        sourceHealth.put("ad", testADConnection().success());
        sourceHealth.put("failover", testFailoverConnection().success());
        
        boolean allHealthy = sourceHealth.values().stream().allMatch(h -> h);
        boolean anyHealthy = sourceHealth.values().stream().anyMatch(h -> h);
        
        String status = allHealthy ? "UP" : (anyHealthy ? "DEGRADED" : "DOWN");
        
        return new HealthStatus(status, sourceHealth, Instant.now());
    }

    // Helper method
    private LdapContextSource getSourceByName(String name) {
        return switch (name) {
            case "primary" -> primarySource;
            case "secure" -> secureSource;
            case "ad" -> adSource;
            case "failover" -> failoverSource;
            default -> null;
        };
    }

    record ConnectionTestResult(boolean success, String message, String serverUrl, 
                               String baseDn, long responseTimeMs) {}
    
    record ContextSourceConfig(String name, List<String> urls, String baseDn, String userDn, 
                              boolean pooled, boolean anonymousReadOnly, 
                              Map<String, Object> environmentProperties) {}
    
    record HealthStatus(String status, Map<String, Boolean> sources, Instant timestamp) {}
}

/**
 * Connection statistics tracker
 */
class ConnectionStats {
    private final String sourceName;
    private long totalAttempts = 0;
    private long successfulConnections = 0;
    private long failedConnections = 0;
    private long totalResponseTime = 0;
    private long minResponseTime = Long.MAX_VALUE;
    private long maxResponseTime = 0;
    private Instant lastConnectionAttempt;
    private Instant lastSuccessfulConnection;
    private Instant lastFailedConnection;

    public ConnectionStats(String sourceName) {
        this.sourceName = sourceName;
    }

    public synchronized void recordSuccess(long responseTime) {
        totalAttempts++;
        successfulConnections++;
        totalResponseTime += responseTime;
        minResponseTime = Math.min(minResponseTime, responseTime);
        maxResponseTime = Math.max(maxResponseTime, responseTime);
        lastConnectionAttempt = Instant.now();
        lastSuccessfulConnection = Instant.now();
    }

    public synchronized void recordFailure() {
        totalAttempts++;
        failedConnections++;
        lastConnectionAttempt = Instant.now();
        lastFailedConnection = Instant.now();
    }

    public synchronized void reset() {
        totalAttempts = 0;
        successfulConnections = 0;
        failedConnections = 0;
        totalResponseTime = 0;
        minResponseTime = Long.MAX_VALUE;
        maxResponseTime = 0;
        lastConnectionAttempt = null;
        lastSuccessfulConnection = null;
        lastFailedConnection = null;
    }

    public long getAverageResponseTime() {
        return successfulConnections > 0 ? totalResponseTime / successfulConnections : 0;
    }

    public double getSuccessRate() {
        return totalAttempts > 0 ? (double) successfulConnections / totalAttempts * 100 : 0;
    }

    // Getters
    public String getSourceName() { return sourceName; }
    public long getTotalAttempts() { return totalAttempts; }
    public long getSuccessfulConnections() { return successfulConnections; }
    public long getFailedConnections() { return failedConnections; }
    public long getMinResponseTime() { return minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime; }
    public long getMaxResponseTime() { return maxResponseTime; }
    public Instant getLastConnectionAttempt() { return lastConnectionAttempt; }
    public Instant getLastSuccessfulConnection() { return lastSuccessfulConnection; }
    public Instant getLastFailedConnection() { return lastFailedConnection; }
}

/**
 * REST controller for LDAP context source endpoints
 */
@RestController
@RequestMapping("/api/ldap-context")
class LDAPContextSourceController {

    private final LDAPContextSourceService contextService;

    public LDAPContextSourceController(LDAPContextSourceService contextService) {
        this.contextService = contextService;
    }

    @GetMapping("/test/{source}")
    public ResponseEntity<LDAPContextSourceService.ConnectionTestResult> testConnection(
            @PathVariable String source) {
        LDAPContextSourceService.ConnectionTestResult result = switch (source) {
            case "primary" -> contextService.testPrimaryConnection();
            case "secure" -> contextService.testSecureConnection();
            case "ad" -> contextService.testADConnection();
            case "failover" -> contextService.testFailoverConnection();
            default -> null;
        };
        
        return result != null ? 
            ResponseEntity.ok(result) : 
            ResponseEntity.badRequest().build();
    }

    @GetMapping("/test/all")
    public ResponseEntity<Map<String, LDAPContextSourceService.ConnectionTestResult>> testAllConnections() {
        Map<String, LDAPContextSourceService.ConnectionTestResult> results = 
            contextService.testAllConnections();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/config/{source}")
    public ResponseEntity<LDAPContextSourceService.ContextSourceConfig> getConfiguration(
            @PathVariable String source) {
        LDAPContextSourceService.ContextSourceConfig config = contextService.getConfiguration(source);
        return config != null ? 
            ResponseEntity.ok(config) : 
            ResponseEntity.notFound().build();
    }

    @GetMapping("/statistics/{source}")
    public ResponseEntity<ConnectionStats> getStatistics(@PathVariable String source) {
        ConnectionStats stats = contextService.getStatistics(source);
        return stats != null ? 
            ResponseEntity.ok(stats) : 
            ResponseEntity.notFound().build();
    }

    @GetMapping("/statistics/all")
    public ResponseEntity<Map<String, ConnectionStats>> getAllStatistics() {
        Map<String, ConnectionStats> stats = contextService.getAllStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/statistics/{source}/reset")
    public ResponseEntity<ResetResponse> resetStatistics(@PathVariable String source) {
        boolean reset = contextService.resetStatistics(source);
        return ResponseEntity.ok(new ResetResponse(source, reset));
    }

    @GetMapping("/health")
    public ResponseEntity<LDAPContextSourceService.HealthStatus> getHealthStatus() {
        LDAPContextSourceService.HealthStatus health = contextService.getHealthStatus();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        PatternInfo info = new PatternInfo(
            "LDAP Context Source Pattern",
            "Configuration and management of LDAP context sources",
            "1.0",
            List.of(
                "Connection to LDAP servers",
                "Connection pooling configuration",
                "SSL/TLS support for secure connections",
                "Multiple authentication methods",
                "Failover and load balancing",
                "Connection health monitoring"
            ),
            List.of(
                "Corporate LDAP server connections",
                "Active Directory integration",
                "Multi-server failover",
                "Secure LDAP connections",
                "Connection pooling for performance"
            )
        );
        return ResponseEntity.ok(info);
    }

    record ResetResponse(String source, boolean success) {}
    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
