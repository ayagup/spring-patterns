package com.example.embeddedserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;

import java.util.Map;

/**
 * Embedded Undertow Pattern
 * 
 * Demonstrates configuring and customizing embedded Undertow server,
 * including worker threads, buffer pools, and performance settings.
 * 
 * Key Concepts:
 * - UndertowServletWebServerFactory
 * - Builder customization
 * - Worker threads
 * - Buffer configuration
 * - Performance tuning
 * 
 * Use Cases:
 * - High performance
 * - Low memory
 * - Non-blocking IO
 * - WebSocket
 * - HTTP/2
 */
@SpringBootApplication
public class EmbeddedUndertowPattern {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddedUndertowPattern.class, args);
    }
}

/**
 * Undertow configuration
 */
@Configuration
class UndertowConfig {

    @Bean
    public WebServerFactoryCustomizer<UndertowServletWebServerFactory> undertowCustomizer() {
        return factory -> {
            factory.setPort(8080);
            
            // Builder customization
            factory.addBuilderCustomizers(builder -> {
                builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
                builder.setServerOption(UndertowOptions.HTTP2_SETTINGS_ENABLE_PUSH, true);
                builder.setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT, 60000);
                
                // Worker threads
                builder.setWorkerThreads(200);
                builder.setIoThreads(Runtime.getRuntime().availableProcessors() * 2);
                
                // Buffer pool
                builder.setBufferSize(16384);
                builder.setDirectBuffers(true);
            });
        };
    }
}

/**
 * Service providing Undertow information
 */
@Service
class UndertowInfoService {

    public Map<String, Object> getUndertowInfo() {
        return Map.of(
                "serverType", "Embedded Undertow",
                "defaultPort", 8080,
                "ioModel", "Non-blocking NIO",
                "features", Map.of(
                        "http2", true,
                        "webSocket", true,
                        "serverPush", true,
                        "directBuffers", true
                )
        );
    }

    public Map<String, Object> getPerformanceInfo() {
        int processors = Runtime.getRuntime().availableProcessors();
        return Map.of(
                "workerThreads", 200,
                "ioThreads", processors * 2,
                "bufferSize", 16384,
                "directBuffers", true,
                "availableProcessors", processors
        );
    }
}

/**
 * Controller exposing Undertow information
 */
@RestController
class UndertowInfoController {

    private final UndertowInfoService undertowInfoService;

    public UndertowInfoController(UndertowInfoService undertowInfoService) {
        this.undertowInfoService = undertowInfoService;
    }

    @GetMapping("/undertow/info")
    public Map<String, Object> getUndertowInfo() {
        return undertowInfoService.getUndertowInfo();
    }

    @GetMapping("/undertow/performance")
    public Map<String, Object> getPerformanceInfo() {
        return undertowInfoService.getPerformanceInfo();
    }
}

/**
 * Documentation:
 * 
 * Configuration (application.properties):
 * server.port=8080
 * server.undertow.threads.io=16
 * server.undertow.threads.worker=200
 * server.undertow.buffer-size=16384
 * server.undertow.direct-buffers=true
 * server.undertow.max-http-post-size=0
 * 
 * Dependency:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-undertow</artifactId>
 * </dependency>
 * 
 * Undertow Advantages:
 * - Lightweight and fast
 * - Non-blocking architecture
 * - Low memory footprint
 * - Excellent performance
 * - Built-in HTTP/2 support
 * - WebSocket support
 * 
 * Thread Configuration:
 * - IO Threads: CPU cores * 2
 * - Worker Threads: Based on workload
 * - Direct Buffers: Better performance
 * 
 * Performance Tips:
 * - Use direct buffers
 * - Configure buffer size appropriately
 * - Set IO threads to CPU cores * 2
 * - Tune worker threads based on load
 */
