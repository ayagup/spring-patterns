package com.example.embeddedserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

/**
 * Server Customizer Pattern
 * 
 * Demonstrates customizing embedded servers using WebServerFactoryCustomizer,
 * including ports, timeouts, error pages, and server-specific settings.
 * 
 * Key Concepts:
 * - WebServerFactoryCustomizer
 * - ConfigurableServletWebServerFactory
 * - Server properties
 * - Error pages
 * - Timeout configuration
 * 
 * Use Cases:
 * - Custom ports
 * - Error handling
 * - Timeout configuration
 * - Server tuning
 * - Multi-server setup
 */
@SpringBootApplication
public class ServerCustomizerPattern {

    public static void main(String[] args) {
        SpringApplication.run(ServerCustomizerPattern.class, args);
    }
}

/**
 * Server customization configuration
 */
@Configuration
class ServerCustomizerConfig {

    /**
     * General server customization
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> serverCustomizer() {
        return factory -> {
            // Port configuration
            factory.setPort(8080);
            factory.setContextPath("/api");
            
            // Session timeout
            factory.getSession().setTimeout(Duration.ofMinutes(30));
            factory.getSession().setCookie().setHttpOnly(true);
            factory.getSession().setCookie().setSecure(false);
            
            // Error pages
            factory.addErrorPages(
                new ErrorPage(HttpStatus.NOT_FOUND, "/error/404"),
                new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error/500"),
                new ErrorPage(HttpStatus.FORBIDDEN, "/error/403")
            );
            
            // Compression
            factory.setCompression(compression -> {
                compression.setEnabled(true);
                compression.setMinResponseSize(2048);
                compression.setMimeTypes(new String[]{
                    "text/html",
                    "text/xml",
                    "text/plain",
                    "text/css",
                    "application/javascript",
                    "application/json"
                });
            });
        };
    }
}

/**
 * Service providing server configuration info
 */
@Service
class ServerConfigService {

    public Map<String, Object> getServerConfig() {
        return Map.of(
                "port", 8080,
                "contextPath", "/api",
                "sessionTimeout", "30 minutes",
                "compression", Map.of(
                        "enabled", true,
                        "minSize", 2048,
                        "types", "text/html, application/json, etc."
                ),
                "errorPages", Map.of(
                        "404", "/error/404",
                        "500", "/error/500",
                        "403", "/error/403"
                )
        );
    }

    public Map<String, Object> getSessionConfig() {
        return Map.of(
                "timeout", "30 minutes",
                "cookieHttpOnly", true,
                "cookieSecure", false,
                "cookieName", "JSESSIONID"
        );
    }
}

/**
 * Controller exposing server configuration
 */
@RestController
class ServerConfigController {

    private final ServerConfigService configService;

    public ServerConfigController(ServerConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/server/config")
    public Map<String, Object> getServerConfig() {
        return configService.getServerConfig();
    }

    @GetMapping("/server/session")
    public Map<String, Object> getSessionConfig() {
        return configService.getSessionConfig();
    }
}

/**
 * Documentation:
 * 
 * WebServerFactoryCustomizer:
 * 
 * @Bean
 * public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
 *         customizer() {
 *     return factory -> {
 *         // Customize server
 *         factory.setPort(8080);
 *         factory.setContextPath("/app");
 *     };
 * }
 * 
 * Server Properties (application.properties):
 * 
 * # Port and context
 * server.port=8080
 * server.servlet.context-path=/api
 * 
 * # Session
 * server.servlet.session.timeout=30m
 * server.servlet.session.cookie.http-only=true
 * server.servlet.session.cookie.secure=false
 * server.servlet.session.cookie.name=JSESSIONID
 * 
 * # Compression
 * server.compression.enabled=true
 * server.compression.min-response-size=2048
 * server.compression.mime-types=text/html,application/json
 * 
 * # Error handling
 * server.error.include-message=always
 * server.error.include-binding-errors=always
 * server.error.include-stacktrace=on-param
 * server.error.include-exception=false
 * 
 * # Timeouts
 * server.connection-timeout=20000
 * server.shutdown=graceful
 * spring.lifecycle.timeout-per-shutdown-phase=20s
 * 
 * Error Pages:
 * factory.addErrorPages(
 *     new ErrorPage(HttpStatus.NOT_FOUND, "/404"),
 *     new ErrorPage(Throwable.class, "/error")
 * );
 * 
 * Customizer Types:
 * - ConfigurableServletWebServerFactory: All servlet servers
 * - TomcatServletWebServerFactory: Tomcat-specific
 * - JettyServletWebServerFactory: Jetty-specific
 * - UndertowServletWebServerFactory: Undertow-specific
 * - NettyReactiveWebServerFactory: Netty (WebFlux)
 * 
 * Multiple Customizers:
 * @Bean
 * public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
 *         portCustomizer() {
 *     return factory -> factory.setPort(8080);
 * }
 * 
 * @Bean
 * public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
 *         compressionCustomizer() {
 *     return factory -> factory.setCompression(...);
 * }
 * 
 * Best Practices:
 * - Use properties for simple config
 * - Use customizers for complex config
 * - Keep customizers focused
 * - Use appropriate timeout values
 * - Enable compression for better performance
 * - Configure session properly for security
 * - Set up proper error pages
 */
