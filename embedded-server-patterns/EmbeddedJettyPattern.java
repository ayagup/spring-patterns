package com.example.embeddedserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.Map;

/**
 * Embedded Jetty Pattern
 * 
 * Demonstrates configuring and customizing embedded Jetty server in Spring Boot,
 * including thread pools, connectors, and server handlers.
 * 
 * Key Concepts:
 * - JettyServletWebServerFactory
 * - QueuedThreadPool
 * - ServerConnector
 * - Server customization
 * - Handler configuration
 * 
 * Use Cases:
 * - Lightweight server
 * - WebSocket support
 * - HTTP/2 support
 * - Async servlets
 * - Custom handlers
 */
@SpringBootApplication
public class EmbeddedJettyPattern {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddedJettyPattern.class, args);
    }
}

/**
 * Jetty server customization
 */
@Configuration
class JettyConfig {

    /**
     * Customize Jetty server
     */
    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> jettyCustomizer() {
        return factory -> {
            factory.setPort(8080);
            
            // Add server customizer
            factory.addServerCustomizers(server -> {
                // Configure thread pool
                QueuedThreadPool threadPool = (QueuedThreadPool) server.getThreadPool();
                threadPool.setMaxThreads(200);
                threadPool.setMinThreads(10);
                threadPool.setIdleTimeout(60000);
                
                // Configure connector
                ServerConnector connector = (ServerConnector) server.getConnectors()[0];
                connector.setIdleTimeout(30000);
                connector.setAcceptQueueSize(100);
            });
        };
    }

    /**
     * Create additional Jetty connector
     */
    private ServerConnector createAdditionalConnector(Server server) {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8081);
        connector.setIdleTimeout(30000);
        return connector;
    }
}

/**
 * Service providing Jetty information
 */
@Service
class JettyInfoService {

    public Map<String, Object> getJettyInfo() {
        return Map.of(
                "serverType", "Embedded Jetty",
                "defaultPort", 8080,
                "features", Map.of(
                        "webSocket", true,
                        "http2", true,
                        "asyncServlet", true,
                        "threadPool", "QueuedThreadPool"
                )
        );
    }

    public Map<String, Object> getThreadPoolInfo() {
        return Map.of(
                "type", "QueuedThreadPool",
                "maxThreads", 200,
                "minThreads", 10,
                "idleTimeout", 60000
        );
    }
}

/**
 * Controller exposing Jetty information
 */
@RestController
class JettyInfoController {

    private final JettyInfoService jettyInfoService;

    public JettyInfoController(JettyInfoService jettyInfoService) {
        this.jettyInfoService = jettyInfoService;
    }

    @GetMapping("/jetty/info")
    public Map<String, Object> getJettyInfo() {
        return jettyInfoService.getJettyInfo();
    }

    @GetMapping("/jetty/thread-pool")
    public Map<String, Object> getThreadPoolInfo() {
        return jettyInfoService.getThreadPoolInfo();
    }
}

/**
 * Documentation:
 * 
 * Jetty Configuration (application.properties):
 * server.port=8080
 * server.jetty.threads.max=200
 * server.jetty.threads.min=8
 * server.jetty.threads.idle-timeout=60000
 * server.jetty.connection-idle-timeout=30000
 * server.jetty.max-http-form-post-size=200000
 * 
 * Dependency:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-jetty</artifactId>
 * </dependency>
 * 
 * Exclude Tomcat:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-web</artifactId>
 *     <exclusions>
 *         <exclusion>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-starter-tomcat</artifactId>
 *         </exclusion>
 *     </exclusions>
 * </dependency>
 * 
 * HTTP/2 Support:
 * @Bean
 * public JettyServletWebServerFactory jettyFactory() {
 *     JettyServletWebServerFactory factory = new JettyServletWebServerFactory();
 *     factory.addServerCustomizers(server -> {
 *         // Enable HTTP/2
 *         ServerConnector connector = new ServerConnector(server,
 *             new HttpConnectionFactory(),
 *             new HTTP2CServerConnectionFactory());
 *         connector.setPort(8080);
 *         server.addConnector(connector);
 *     });
 *     return factory;
 * }
 * 
 * Jetty vs Tomcat:
 * - Lighter footprint
 * - Better WebSocket support
 * - Async processing
 * - Lower memory usage
 * - Easier embedding
 */
