package com.example.redis.connection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.util.Map;

/**
 * Redis Connection Factory Pattern
 * 
 * Demonstrates Redis connection factory configuration.
 * Connection Factory provides:
 * - Connection pooling
 * - Connection timeout configuration
 * - Cluster support
 * - Sentinel support
 * - SSL/TLS support
 * - Connection retry logic
 * 
 * Use cases:
 * - Production-ready Redis configuration
 * - High-availability setups
 * - Connection pooling for performance
 * - Secure Redis connections
 * - Clustered Redis deployments
 */

@Configuration
class RedisConnectionFactoryConfig {
    
    // Standalone Redis configuration with connection pooling
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis server configuration
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName("localhost");
        serverConfig.setPort(6379);
        // serverConfig.setPassword("your-password"); // Optional
        serverConfig.setDatabase(0);
        
        // Connection pool configuration
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(20);  // Maximum number of connections
        poolConfig.setMaxIdle(10);   // Maximum idle connections
        poolConfig.setMinIdle(5);    // Minimum idle connections
        poolConfig.setMaxWait(Duration.ofMillis(2000));  // Max wait for connection
        poolConfig.setTestOnBorrow(true);  // Test connection before use
        poolConfig.setTestOnReturn(true);  // Test connection before return
        poolConfig.setTestWhileIdle(true); // Test idle connections
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000)); // Eviction check interval
        poolConfig.setMinEvictableIdleTime(Duration.ofMillis(60000)); // Min idle time before eviction
        poolConfig.setNumTestsPerEvictionRun(3); // Number of connections to test per eviction run
        
        // Client configuration
        SocketOptions socketOptions = SocketOptions.builder()
            .connectTimeout(Duration.ofSeconds(5))
            .keepAlive(true)
            .build();
        
        ClientOptions clientOptions = ClientOptions.builder()
            .socketOptions(socketOptions)
            .autoReconnect(true)
            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
            .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(10)))
            .build();
        
        LettucePoolingClientConfiguration clientConfig = 
            LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofSeconds(5))
                .shutdownTimeout(Duration.ofMillis(100))
                .build();
        
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }
    
    // Alternative: Non-pooled connection
    @Bean
    public RedisConnectionFactory nonPooledConnectionFactory() {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName("localhost");
        serverConfig.setPort(6379);
        
        LettuceClientConfiguration clientConfig = 
            LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .build();
        
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }
    
    // Cluster configuration example (commented out)
    /*
    @Bean
    public RedisConnectionFactory redisClusterConnectionFactory() {
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration();
        clusterConfig.addClusterNode(new RedisNode("localhost", 7000));
        clusterConfig.addClusterNode(new RedisNode("localhost", 7001));
        clusterConfig.addClusterNode(new RedisNode("localhost", 7002));
        
        ClusterTopologyRefreshOptions topologyRefreshOptions = 
            ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(Duration.ofMinutes(10))
                .enableAllAdaptiveRefreshTriggers()
                .build();
        
        ClusterClientOptions clusterClientOptions = 
            ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefreshOptions)
                .autoReconnect(true)
                .build();
        
        LettuceClientConfiguration clientConfig = 
            LettuceClientConfiguration.builder()
                .clientOptions(clusterClientOptions)
                .commandTimeout(Duration.ofSeconds(5))
                .build();
        
        return new LettuceConnectionFactory(clusterConfig, clientConfig);
    }
    */
    
    // Sentinel configuration example (commented out)
    /*
    @Bean
    public RedisConnectionFactory redisSentinelConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = 
            new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel("localhost", 26379)
                .sentinel("localhost", 26380)
                .sentinel("localhost", 26381);
        
        LettuceClientConfiguration clientConfig = 
            LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .build();
        
        return new LettuceConnectionFactory(sentinelConfig, clientConfig);
    }
    */
}

@Service
class ConnectionFactoryService {
    
    private final RedisConnectionFactory connectionFactory;
    private final RedisTemplate<String, String> redisTemplate;
    
    public ConnectionFactoryService(RedisConnectionFactory connectionFactory,
                                   RedisTemplate<String, String> redisTemplate) {
        this.connectionFactory = connectionFactory;
        this.redisTemplate = redisTemplate;
    }
    
    public Map<String, Object> getConnectionInfo() {
        return Map.of(
            "factoryClass", connectionFactory.getClass().getSimpleName(),
            "connectionType", connectionFactory.getConnection().getClass().getSimpleName(),
            "isOpen", !connectionFactory.getConnection().isClosed()
        );
    }
    
    public boolean testConnection() {
        try {
            redisTemplate.opsForValue().set("connection:test", "OK");
            String value = redisTemplate.opsForValue().get("connection:test");
            return "OK".equals(value);
        } catch (Exception e) {
            return false;
        }
    }
    
    public String ping() {
        try {
            return connectionFactory.getConnection().ping();
        } catch (Exception e) {
            return "FAILED: " + e.getMessage();
        }
    }
}

@RestController
@RequestMapping("/api/redis/connection")
class RedisConnectionFactoryController {
    
    private final ConnectionFactoryService connectionFactoryService;
    
    public RedisConnectionFactoryController(ConnectionFactoryService connectionFactoryService) {
        this.connectionFactoryService = connectionFactoryService;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getConnectionInfo() {
        return connectionFactoryService.getConnectionInfo();
    }
    
    @GetMapping("/test")
    public Map<String, Object> testConnection() {
        boolean success = connectionFactoryService.testConnection();
        return Map.of(
            "success", success,
            "message", success ? "Connection successful" : "Connection failed"
        );
    }
    
    @GetMapping("/ping")
    public Map<String, String> ping() {
        String response = connectionFactoryService.ping();
        return Map.of("response", response);
    }
    
    @GetMapping("/guide")
    public String getGuide() {
        return """
                Redis Connection Factory Pattern
                ================================
                
                Configuration Options:
                
                1. Standalone Configuration:
                   - Single Redis server
                   - Host, port, password, database
                   - Simple setup for development
                
                2. Connection Pooling:
                   - maxTotal: Max connections (20)
                   - maxIdle: Max idle connections (10)
                   - minIdle: Min idle connections (5)
                   - maxWait: Wait timeout (2s)
                   - Test on borrow/return
                   - Eviction policy
                
                3. Client Options:
                   - Socket timeout
                   - Connect timeout
                   - Keep-alive
                   - Auto-reconnect
                   - Disconnected behavior
                
                4. Cluster Configuration:
                   - Multiple Redis nodes
                   - Automatic failover
                   - Read from replicas
                   - Topology refresh
                
                5. Sentinel Configuration:
                   - High availability
                   - Automatic master failover
                   - Multiple sentinels
                   - Master discovery
                
                6. SSL/TLS:
                   - Secure connections
                   - Certificate validation
                   - Client certificates
                
                Pool Configuration Best Practices:
                - Set maxTotal based on concurrent requests
                - Keep minIdle for instant connections
                - Enable connection testing
                - Configure eviction for stale connections
                - Set appropriate timeouts
                
                Recommended Settings:
                - Development: Non-pooled, localhost
                - Production: Pooled, with failover
                - maxTotal: 10-50 (depends on load)
                - connectTimeout: 5s
                - commandTimeout: 5s
                - maxWait: 2s
                """;
    }
}
