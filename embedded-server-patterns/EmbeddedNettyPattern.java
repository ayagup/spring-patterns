package com.example.embeddedserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;
import java.util.Map;

/**
 * Embedded Netty Pattern
 * 
 * Demonstrates configuring embedded Netty server for reactive applications,
 * including event loops, channels, and reactive streams.
 * 
 * Key Concepts:
 * - NettyReactiveWebServerFactory
 * - Reactive streams
 * - Event loop groups
 * - Channel options
 * - Non-blocking IO
 * 
 * Use Cases:
 * - Reactive applications
 * - WebFlux support
 * - High concurrency
 * - Streaming data
 * - Real-time processing
 */
@SpringBootApplication
public class EmbeddedNettyPattern {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddedNettyPattern.class, args);
    }
}

/**
 * Netty server customization
 */
@Configuration
class NettyConfig {

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyCustomizer() {
        return factory -> {
            factory.setPort(8080);
            
            // Customize Netty server
            factory.addServerCustomizers(httpServer -> 
                httpServer
                    .idleTimeout(Duration.ofSeconds(60))
                    .compress(true)
                    .accessLog(true)
            );
        };
    }
}

/**
 * Service providing Netty information
 */
@Service
class NettyInfoService {

    public Mono<Map<String, Object>> getNettyInfo() {
        return Mono.just(Map.of(
                "serverType", "Embedded Netty",
                "defaultPort", 8080,
                "ioModel", "Event Loop (NIO)",
                "features", Map.of(
                        "reactive", true,
                        "nonBlocking", true,
                        "webFlux", true,
                        "streaming", true
                )
        ));
    }

    public Mono<Map<String, Object>> getReactiveInfo() {
        return Mono.just(Map.of(
                "framework", "Spring WebFlux",
                "reactiveLibrary", "Project Reactor",
                "serverType", "Netty",
                "backpressure", true,
                "streamingSupport", true
        ));
    }
}

/**
 * Reactive controller
 */
@RestController
class NettyInfoController {

    private final NettyInfoService nettyInfoService;

    public NettyInfoController(NettyInfoService nettyInfoService) {
        this.nettyInfoService = nettyInfoService;
    }

    @GetMapping("/netty/info")
    public Mono<Map<String, Object>> getNettyInfo() {
        return nettyInfoService.getNettyInfo();
    }

    @GetMapping("/netty/reactive")
    public Mono<Map<String, Object>> getReactiveInfo() {
        return nettyInfoService.getReactiveInfo();
    }
}

/**
 * Documentation:
 * 
 * Netty for Reactive Applications:
 * 
 * Dependency (Spring WebFlux):
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-webflux</artifactId>
 * </dependency>
 * 
 * Configuration (application.properties):
 * server.port=8080
 * spring.webflux.base-path=/api
 * server.netty.connection-timeout=20000
 * server.netty.idle-timeout=60000
 * 
 * Programmatic Configuration:
 * @Bean
 * public NettyReactiveWebServerFactory nettyFactory() {
 *     NettyReactiveWebServerFactory factory = 
 *         new NettyReactiveWebServerFactory();
 *     factory.setPort(8080);
 *     factory.addServerCustomizers(httpServer -> {
 *         return httpServer
 *             .idleTimeout(Duration.ofSeconds(60))
 *             .compress(true)
 *             .protocol(HttpProtocol.H2, HttpProtocol.HTTP11);
 *     });
 *     return factory;
 * }
 * 
 * Netty Features:
 * - Event-driven architecture
 * - Asynchronous and non-blocking
 * - High performance
 * - Low resource consumption
 * - Backpressure support
 * 
 * WebFlux vs MVC:
 * - WebFlux: Reactive, non-blocking, Netty
 * - MVC: Servlet-based, blocking, Tomcat/Jetty
 * 
 * Use Cases:
 * - Streaming data
 * - Real-time updates
 * - High concurrency
 * - Backpressure handling
 * - Reactive microservices
 * 
 * Reactive Programming:
 * @GetMapping("/stream")
 * public Flux<Data> streamData() {
 *     return Flux.interval(Duration.ofSeconds(1))
 *         .map(i -> new Data(i));
 * }
 * 
 * Backpressure:
 * - Publisher controls rate
 * - Subscriber requests data
 * - Prevents overflow
 * 
 * Performance:
 * - Non-blocking IO
 * - Event loop threads
 * - Lower memory usage
 * - Higher throughput
 */
