import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

/**
 * WebSocket Routing Pattern - Spring Cloud Gateway
 * ================================================
 * 
 * Route WebSocket connections through gateway to backend services.
 * 
 * WebSocket Features:
 * - Bidirectional communication
 * - Real-time updates
 * - Low latency
 * - Connection persistence
 * 
 * Use Cases:
 * - Chat applications
 * - Real-time notifications
 * - Live data feeds
 * - Collaborative editing
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 */
@Configuration
public class WebSocketRoutingPattern {

    /**
     * Example: WebSocket Route Configuration
     */
    @Bean
    public RouteLocator webSocketRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("websocket_route", r -> r
                .path("/ws/**")
                .uri("ws://localhost:8081"))  // WebSocket URI
            
            .route("secure_websocket_route", r -> r
                .path("/wss/**")
                .uri("wss://localhost:8443"))  // Secure WebSocket
            
            .build();
    }

    /**
     * YAML Configuration:
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         - id: websocket_route
     *           uri: ws://localhost:8081
     *           predicates:
     *             - Path=/ws/**
     * 
     *         - id: secure_websocket_route
     *           uri: wss://localhost:8443
     *           predicates:
     *             - Path=/wss/**
     */
}
