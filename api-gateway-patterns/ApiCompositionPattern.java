import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * API Composition Pattern - Spring Cloud Gateway
 * =============================================
 * 
 * Aggregate responses from multiple backend APIs into a single response.
 * 
 * Composition Strategies:
 * - Sequential: Call APIs one after another
 * - Parallel: Call APIs concurrently
 * - Conditional: Call based on previous responses
 * - Partial: Handle failures gracefully
 * 
 * Use Cases:
 * - Aggregate microservice responses
 * - Backend for Frontend (BFF)
 * - Reduce client-side API calls
 * - Compose complex responses
 */
@Configuration
public class ApiCompositionPattern {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    /**
     * Example: Parallel API Composition
     * Call user service and order service concurrently
     */
    public Mono<ComposedResponse> composeUserWithOrders(String userId, WebClient webClient) {
        Mono<UserResponse> userMono = webClient.get()
            .uri("http://localhost:8081/users/" + userId)
            .retrieve()
            .bodyToMono(UserResponse.class);
        
        Mono<List<OrderResponse>> ordersMono = webClient.get()
            .uri("http://localhost:8082/orders?userId=" + userId)
            .retrieve()
            .bodyToFlux(OrderResponse.class)
            .collectList();
        
        return Mono.zip(userMono, ordersMono)
            .map(tuple -> new ComposedResponse(tuple.getT1(), tuple.getT2()));
    }

    private static class UserResponse {
        private String id;
        private String name;
        // getters/setters
    }

    private static class OrderResponse {
        private String orderId;
        private String productName;
        // getters/setters
    }

    private static class ComposedResponse {
        private UserResponse user;
        private List<OrderResponse> orders;
        
        public ComposedResponse(UserResponse user, List<OrderResponse> orders) {
            this.user = user;
            this.orders = orders;
        }
        // getters/setters
    }
}
