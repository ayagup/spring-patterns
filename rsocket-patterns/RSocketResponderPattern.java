package com.example.rsocket;

import io.rsocket.SocketAcceptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RSocket Responder Pattern
 * 
 * Demonstrates server-side RSocket responder implementation:
 * - Handling incoming RSocket connections
 * - Implementing message handlers
 * - Managing connection lifecycle
 * - Connection setup and metadata handling
 * 
 * @author Spring Patterns
 */

@Data
class ConnectionInfo {
    private String clientId;
    private Long connectedAt;
    private String metadata;
}

@Data
class Request {
    private String operation;
    private Map<String, Object> params;
}

@Data
class Response {
    private boolean success;
    private String message;
    private Object data;
}

/**
 * Main RSocket Responder Controller
 */
@Controller
@Slf4j
class RSocketResponderController {
    
    private final Map<String, ConnectionInfo> activeConnections = new ConcurrentHashMap<>();
    
    /**
     * Handle connection setup
     */
    @ConnectMapping("client.connect")
    public Mono<Void> handleConnect(@Payload String clientId) {
        log.info("Client connecting: {}", clientId);
        
        ConnectionInfo info = new ConnectionInfo();
        info.setClientId(clientId);
        info.setConnectedAt(System.currentTimeMillis());
        
        activeConnections.put(clientId, info);
        
        return Mono.empty();
    }
    
    /**
     * Request-Response handler
     */
    @MessageMapping("process.request")
    public Mono<Response> processRequest(Request request) {
        log.info("Processing request: {}", request.getOperation());
        
        return Mono.just(new Response())
                .map(resp -> {
                    resp.setSuccess(true);
                    resp.setMessage("Request processed: " + request.getOperation());
                    resp.setData(request.getParams());
                    return resp;
                })
                .delayElement(Duration.ofMillis(100));
    }
    
    /**
     * Fire-and-Forget handler
     */
    @MessageMapping("log.event")
    public Mono<Void> logEvent(String event) {
        log.info("Event logged: {}", event);
        return Mono.empty();
    }
    
    /**
     * Request-Stream handler
     */
    @MessageMapping("data.stream")
    public Flux<String> streamData(String query) {
        log.info("Streaming data for query: {}", query);
        
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> String.format("Data-%s-%d", query, i))
                .take(5);
    }
    
    /**
     * Channel handler
     */
    @MessageMapping("channel.process")
    public Flux<String> handleChannel(Flux<String> inputs) {
        log.info("Channel established");
        
        return inputs
                .doOnNext(input -> log.info("Received: {}", input))
                .map(input -> "Processed: " + input);
    }
    
    /**
     * Get active connections
     */
    @MessageMapping("connections.list")
    public Flux<ConnectionInfo> listConnections() {
        return Flux.fromIterable(activeConnections.values());
    }
}

/**
 * RSocket Responder Information Service
 */
@Service
class ResponderInfoService {
    
    public String getPatternInfo() {
        return """
                RSocket Responder Pattern
                ========================
                
                Purpose:
                - Server-side RSocket endpoint implementation
                - Handle incoming RSocket connections
                - Implement message handlers for all interaction models
                - Manage connection lifecycle
                
                Key Components:
                1. @Controller
                   - Marks class as RSocket responder
                   - Handles incoming connections
                
                2. @ConnectMapping
                   - Handles connection setup
                   - Processes setup payload
                   - Returns setup response
                
                3. @MessageMapping
                   - Maps routes to handler methods
                   - Supports all interaction models
                   - Processes incoming messages
                
                4. Handler Methods
                   - Mono<T>: Request-Response
                   - Mono<Void>: Fire-and-Forget
                   - Flux<T>: Request-Stream
                   - Flux<T>(Flux<T>): Channel
                
                Best Practices:
                1. Use @ConnectMapping for connection setup
                2. Implement proper error handling
                3. Log connection lifecycle events
                4. Track active connections
                5. Use reactive return types
                6. Handle backpressure properly
                7. Implement timeout strategies
                8. Monitor responder health
                """;
    }
    
    public List<String> getHandlerTypes() {
        return List.of(
                "@ConnectMapping: Connection setup handler",
                "@MessageMapping + Mono<T>: Request-Response",
                "@MessageMapping + Mono<Void>: Fire-and-Forget",
                "@MessageMapping + Flux<T>: Request-Stream",
                "@MessageMapping + Flux<T>(Flux): Channel"
        );
    }
}

/**
 * REST Controller for Responder info
 */
@RestController
@RequestMapping("/rsocket/responder")
class ResponderInfoController {
    
    private final ResponderInfoService infoService;
    
    public ResponderInfoController(ResponderInfoService infoService) {
        this.infoService = infoService;
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return infoService.getPatternInfo();
    }
    
    @GetMapping("/handlers")
    public List<String> getHandlerTypes() {
        return infoService.getHandlerTypes();
    }
}

@Configuration
class ResponderConfig {
    
    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder().build();
    }
    
    @Bean
    public RSocketServerCustomizer rSocketServerCustomizer() {
        return server -> server
                .resume()
                .lease();
    }
}

@SpringBootApplication
public class RSocketResponderPattern {
    public static void main(String[] args) {
        SpringApplication.run(RSocketResponderPattern.class, args);
    }
}
