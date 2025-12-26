package com.example.embeddedserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;

import java.util.Map;

/**
 * Embedded Tomcat Pattern
 * 
 * Demonstrates configuring and customizing embedded Tomcat server in Spring Boot,
 * including connectors, protocols, thread pools, and server settings.
 * 
 * Key Concepts:
 * - TomcatServletWebServerFactory
 * - WebServerFactoryCustomizer
 * - Additional connectors
 * - Protocol handlers
 * - Thread pool configuration
 * 
 * Use Cases:
 * - Multiple ports
 * - HTTPS configuration
 * - Connection pooling
 * - Performance tuning
 * - Custom connectors
 */
@SpringBootApplication
public class EmbeddedTomcatPattern {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddedTomcatPattern.class, args);
    }
}

/**
 * Tomcat server customization
 */
@Configuration
class TomcatConfig {

    /**
     * Customize Tomcat with additional connector
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            // Add additional HTTP connector on port 8081
            factory.addAdditionalTomcatConnectors(createHttpConnector());
            
            // Configure main connector
            factory.addConnectorCustomizers(connector -> {
                connector.setPort(8080);
                connector.setScheme("http");
                
                // Protocol handler configuration
                if (connector.getProtocolHandler() instanceof Http11NioProtocol) {
                    Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                    
                    // Connection settings
                    protocol.setMaxConnections(200);
                    protocol.setMaxThreads(200);
                    protocol.setMinSpareThreads(10);
                    protocol.setConnectionTimeout(20000);
                    protocol.setKeepAliveTimeout(60000);
                    protocol.setMaxKeepAliveRequests(100);
                    
                    // Compression
                    protocol.setCompression("on");
                    protocol.setCompressionMinSize(2048);
                    protocol.setCompressibleMimeType(
                        "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json"
                    );
                }
            });
        };
    }

    /**
     * Create additional HTTP connector
     */
    private Connector createHttpConnector() {
        Connector connector = new Connector(Http11NioProtocol.class.getName());
        connector.setPort(8081);
        connector.setScheme("http");
        
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        protocol.setMaxThreads(100);
        protocol.setConnectionTimeout(20000);
        
        return connector;
    }
}

/**
 * Service providing server information
 */
@Service
class TomcatInfoService {

    public Map<String, Object> getTomcatInfo() {
        return Map.of(
                "serverType", "Embedded Tomcat",
                "defaultPort", 8080,
                "additionalPort", 8081,
                "protocol", "HTTP/1.1 NIO",
                "features", Map.of(
                        "compression", true,
                        "keepAlive", true,
                        "multipleConnectors", true,
                        "threadPool", "configured"
                )
        );
    }

    public Map<String, Object> getConnectorInfo() {
        return Map.of(
                "mainConnector", Map.of(
                        "port", 8080,
                        "maxThreads", 200,
                        "minSpareThreads", 10,
                        "maxConnections", 200,
                        "connectionTimeout", 20000,
                        "keepAliveTimeout", 60000
                ),
                "additionalConnector", Map.of(
                        "port", 8081,
                        "maxThreads", 100,
                        "connectionTimeout", 20000
                )
        );
    }
}

/**
 * Controller exposing server information
 */
@RestController
class TomcatInfoController {

    private final TomcatInfoService tomcatInfoService;

    public TomcatInfoController(TomcatInfoService tomcatInfoService) {
        this.tomcatInfoService = tomcatInfoService;
    }

    @GetMapping("/tomcat/info")
    public Map<String, Object> getTomcatInfo() {
        return tomcatInfoService.getTomcatInfo();
    }

    @GetMapping("/tomcat/connectors")
    public Map<String, Object> getConnectorInfo() {
        return tomcatInfoService.getConnectorInfo();
    }

    @GetMapping("/tomcat/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "server", "Embedded Tomcat"
        );
    }
}

/**
 * Documentation:
 * 
 * Tomcat Configuration (application.properties):
 * 
 * # Server port
 * server.port=8080
 * 
 * # Connection settings
 * server.tomcat.threads.max=200
 * server.tomcat.threads.min-spare=10
 * server.tomcat.max-connections=10000
 * server.tomcat.accept-count=100
 * 
 * # Connection timeout
 * server.tomcat.connection-timeout=20000
 * 
 * # Keep-alive
 * server.tomcat.keep-alive-timeout=60000
 * server.tomcat.max-keep-alive-requests=100
 * 
 * # Compression
 * server.compression.enabled=true
 * server.compression.min-response-size=2048
 * server.compression.mime-types=text/html,text/xml,text/plain,text/css
 * 
 * # Access logs
 * server.tomcat.accesslog.enabled=true
 * server.tomcat.accesslog.directory=logs
 * server.tomcat.accesslog.pattern=common
 * 
 * Programmatic Configuration:
 * 
 * @Bean
 * public WebServerFactoryCustomizer<TomcatServletWebServerFactory> 
 *         tomcatCustomizer() {
 *     return factory -> {
 *         // Customize Tomcat
 *         factory.setPort(8080);
 *         factory.setContextPath("/app");
 *         
 *         // Add connector customizer
 *         factory.addConnectorCustomizers(connector -> {
 *             // Customize connector
 *         });
 *         
 *         // Add context customizer
 *         factory.addContextCustomizers(context -> {
 *             // Customize context
 *         });
 *     };
 * }
 * 
 * HTTPS Configuration:
 * 
 * private Connector createHttpsConnector() {
 *     Connector connector = new Connector(Http11NioProtocol.class.getName());
 *     connector.setScheme("https");
 *     connector.setSecure(true);
 *     connector.setPort(8443);
 *     
 *     Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
 *     protocol.setSSLEnabled(true);
 *     protocol.setKeystoreFile("keystore.jks");
 *     protocol.setKeystorePass("password");
 *     protocol.setKeyAlias("tomcat");
 *     
 *     return connector;
 * }
 * 
 * HTTP/2 Support:
 * server.http2.enabled=true
 * 
 * Tomcat-Specific Features:
 * - JSP support
 * - WebSocket support
 * - Servlet 4.0/5.0
 * - Access logs
 * - Virtual hosts
 * - URL rewriting
 * 
 * Thread Pool Tuning:
 * - maxThreads: Maximum concurrent requests
 * - minSpareThreads: Minimum idle threads
 * - maxConnections: Maximum connections
 * - acceptCount: Queue size when all threads busy
 * 
 * Performance Tips:
 * - Enable compression for text content
 * - Configure appropriate thread pool
 * - Use NIO/NIO2 connector
 * - Enable keep-alive
 * - Set appropriate timeouts
 * - Monitor thread usage
 * 
 * Common Use Cases:
 * 1. Multiple Ports: Add connectors for different ports
 * 2. HTTPS: Configure SSL connector
 * 3. Proxy: Configure behind reverse proxy
 * 4. Performance: Tune thread pools and connections
 */
