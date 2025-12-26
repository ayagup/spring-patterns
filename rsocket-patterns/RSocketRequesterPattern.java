package com.example.rsocket;

import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.List;

/**
 * RSocket Requester Pattern
 * 
 * Demonstrates the client-side RSocketRequester API for:
 * - Creating and configuring RSocket connections
 * - Making requests with different interaction models
 * - Managing connection lifecycle
 * - Implementing retry and error handling
 * 
 * RSocketRequester provides:
 * - Fluent API for building requests
 * - Support for all RSocket interaction models
 * - Automatic encoding/decoding
 * - Connection management
 * - Metadata handling
 * 
 * @author Spring Patterns
 */

@Data
class User {
    private Long id;
    private String username;
    private String email;
}

@Data
class Product {
    private Long id;
    private String name;
    private Double price;
}

/**
 * RSocket Requester Service demonstrating various usage patterns
 */
@Service
@Slf4j
class RSocketRequesterService {
    
    private RSocketRequester tcpRequester;
    private RSocketRequester wsRequester;
    private final RSocketRequester.Builder requesterBuilder;
    
    public RSocketRequesterService(RSocketRequester.Builder requesterBuilder) {
        this.requesterBuilder = requesterBuilder;
        initializeRequesters();
    }
    
    /**
     * Initialize requesters with different transports
     */
    private void initializeRequesters() {
        // TCP Transport
        tcpRequester = requesterBuilder
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .tcp("localhost", 7000);
        
        // WebSocket Transport
        wsRequester = requesterBuilder
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .websocket(URI.create("ws://localhost:8080/rsocket"));
        
        log.info("RSocket requesters initialized");
    }
    
    /**
     * Request-Response pattern
     */
    public Mono<User> getUserById(Long id) {
        return tcpRequester
                .route("user.get")
                .data(id)
                .retrieveMono(User.class)
                .doOnError(error -> log.error("Error getting user", error))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(5));
    }
    
    /**
     * Fire-and-Forget pattern
     */
    public Mono<Void> logAction(String action) {
        return tcpRequester
                .route("log.action")
                .data(action)
                .send()
                .doOnError(error -> log.error("Error logging action", error));
    }
    
    /**
     * Request-Stream pattern
     */
    public Flux<Product> streamProducts() {
        return tcpRequester
                .route("products.stream")
                .retrieveFlux(Product.class)
                .doOnNext(product -> log.info("Received product: {}", product.getName()))
                .doOnError(error -> log.error("Error streaming products", error))
                .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2)));
    }
    
    /**
     * Channel pattern
     */
    public Flux<String> establishChannel(Flux<String> requests) {
        return tcpRequester
                .route("data.channel")
                .data(requests)
                .retrieveFlux(String.class)
                .doOnNext(response -> log.info("Received: {}", response));
    }
    
    /**
     * Request with metadata
     */
    public Mono<User> getUserWithMetadata(Long id, String token) {
        return tcpRequester
                .route("user.secure")
                .metadata(token, MimeTypeUtils.TEXT_PLAIN)
                .data(id)
                .retrieveMono(User.class);
    }
    
    /**
     * Request with composite metadata
     */
    public Mono<Product> getProductWithCompositeMetadata(Long id) {
        return tcpRequester
                .route("product.get")
                .metadata(metadataSpec -> {
                    metadataSpec.metadata("auth-token", MimeTypeUtils.TEXT_PLAIN);
                    metadataSpec.metadata("request-id", MimeTypeUtils.TEXT_PLAIN);
                })
                .data(id)
                .retrieveMono(Product.class);
    }
    
    /**
     * WebSocket requester example
     */
    public Mono<User> getUserViaWebSocket(Long id) {
        return wsRequester
                .route("user.get")
                .data(id)
                .retrieveMono(User.class);
    }
    
    /**
     * Dispose requester connection
     */
    public void dispose() {
        if (tcpRequester != null) {
            tcpRequester.rsocketClient().dispose();
        }
        if (wsRequester != null) {
            wsRequester.rsocketClient().dispose();
        }
        log.info("RSocket requesters disposed");
    }
}

/**
 * Advanced RSocket Requester patterns
 */
@Service
@Slf4j
class AdvancedRequesterService {
    
    private final RSocketRequester.Builder requesterBuilder;
    
    public AdvancedRequesterService(RSocketRequester.Builder requesterBuilder) {
        this.requesterBuilder = requesterBuilder;
    }
    
    /**
     * Requester with custom setup
     */
    public RSocketRequester createRequesterWithSetup() {
        return requesterBuilder
                .setupRoute("client.setup")
                .setupData("client-info")
                .setupMetadata("auth-token", MimeTypeUtils.TEXT_PLAIN)
                .tcp("localhost", 7000);
    }
    
    /**
     * Requester with connection lifecycle callbacks
     */
    public RSocketRequester createRequesterWithLifecycle() {
        return requesterBuilder
                .rsocketConnector(connector -> connector
                        .reconnect(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)))
                        .keepAlive(Duration.ofSeconds(30), Duration.ofSeconds(90))
                )
                .tcp("localhost", 7000);
    }
    
    /**
     * Multiple routes with same requester
     */
    public Mono<Void> multipleOperations(RSocketRequester requester) {
        Mono<User> getUser = requester
                .route("user.get")
                .data(1L)
                .retrieveMono(User.class);
        
        Mono<Void> logAction = requester
                .route("log.action")
                .data("user-fetched")
                .send();
        
        return Mono.when(getUser, logAction);
    }
}

/**
 * RSocket Requester Pattern Information Service
 */
@Service
class RequesterInfoService {
    
    public String getPatternInfo() {
        return """
                RSocket Requester Pattern
                ========================
                
                Purpose:
                - Client-side API for RSocket communication
                - Fluent interface for building requests
                - Support for all interaction models
                - Connection lifecycle management
                
                Key Features:
                1. Transport Options
                   - TCP: Direct TCP connection
                   - WebSocket: WebSocket transport
                   - Custom: Implement custom transport
                
                2. Request Building
                   - route(): Specify routing metadata
                   - data(): Set request payload
                   - metadata(): Add custom metadata
                   - retrieveMono(): Request-Response
                   - retrieveFlux(): Request-Stream
                   - send(): Fire-and-Forget
                
                3. Configuration
                   - setupRoute(): Setup route
                   - setupData(): Setup payload
                   - setupMetadata(): Setup metadata
                   - dataMimeType(): Data format
                   - metadataMimeType(): Metadata format
                
                4. Connection Management
                   - reconnect(): Auto-reconnect
                   - keepAlive(): Keep-alive settings
                   - dispose(): Close connection
                
                Best Practices:
                1. Reuse requester instances
                2. Configure timeouts appropriately
                3. Implement retry strategies
                4. Handle connection errors
                5. Use proper data formats
                6. Dispose when done
                7. Monitor connection health
                8. Use connection pooling for high load
                """;
    }
    
    public List<String> getTransportTypes() {
        return List.of(
                "TCP: Direct TCP connection (fastest)",
                "WebSocket: WebSocket transport (firewall-friendly)",
                "Local: In-memory transport (testing)",
                "Custom: Implement custom transport"
        );
    }
    
    public List<String> getConfigurationOptions() {
        return List.of(
                "setupRoute: Setup route for connection",
                "setupData: Initial setup payload",
                "setupMetadata: Setup metadata",
                "dataMimeType: Data format (JSON, CBOR, etc.)",
                "metadataMimeType: Metadata format",
                "rsocketConnector: Low-level connector config",
                "rsocketStrategies: Encoding/decoding strategies"
        );
    }
}

/**
 * REST Controller for testing RSocketRequester
 */
@RestController
@RequestMapping("/rsocket/requester")
@Slf4j
class RequesterController {
    
    private final RSocketRequesterService requesterService;
    private final AdvancedRequesterService advancedService;
    private final RequesterInfoService infoService;
    
    public RequesterController(RSocketRequesterService requesterService,
                              AdvancedRequesterService advancedService,
                              RequesterInfoService infoService) {
        this.requesterService = requesterService;
        this.advancedService = advancedService;
        this.infoService = infoService;
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return infoService.getPatternInfo();
    }
    
    @GetMapping("/transports")
    public List<String> getTransportTypes() {
        return infoService.getTransportTypes();
    }
    
    @GetMapping("/configuration")
    public List<String> getConfigurationOptions() {
        return infoService.getConfigurationOptions();
    }
    
    @GetMapping("/test/user/{id}")
    public Mono<User> testGetUser(@PathVariable Long id) {
        return requesterService.getUserById(id);
    }
    
    @PostMapping("/test/log")
    public Mono<Void> testLogAction(@RequestBody String action) {
        return requesterService.logAction(action);
    }
    
    @GetMapping("/test/products/stream")
    public Flux<Product> testStreamProducts() {
        return requesterService.streamProducts();
    }
    
    @GetMapping("/test/user/websocket/{id}")
    public Mono<User> testWebSocketRequester(@PathVariable Long id) {
        return requesterService.getUserViaWebSocket(id);
    }
}

/**
 * Configuration for RSocketRequester
 */
@Configuration
class RequesterConfig {
    
    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder()
                .build();
    }
    
    @Bean
    public RSocketRequester.Builder rSocketRequesterBuilder(RSocketStrategies strategies) {
        return RSocketRequester.builder()
                .rsocketStrategies(strategies);
    }
}

@SpringBootApplication
public class RSocketRequesterPattern {
    public static void main(String[] args) {
        SpringApplication.run(RSocketRequesterPattern.class, args);
    }
}
