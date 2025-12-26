import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hystrix Filter Pattern - Spring Cloud Gateway
 * ============================================
 * 
 * Circuit breaker pattern using Netflix Hystrix (DEPRECATED - use Resilience4j instead).
 * Included for legacy support and migration reference.
 * 
 * Hystrix Features:
 * - Circuit breaker
 * - Fallback responses
 * - Timeout handling
 * - Metrics and monitoring
 * - Thread pool isolation
 * 
 * Note: Hystrix is in maintenance mode. Use Resilience4j for new projects.
 * 
 * Use Cases:
 * - Fault tolerance (legacy)
 * - Fallback responses
 * - Service isolation
 * 
 * Dependencies (DEPRECATED):
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
 * </dependency>
 */
@Configuration
public class HystrixFilterPattern {

    /**
     * Example: Hystrix Filter (DEPRECATED - use CircuitBreakerPattern with Resilience4j)
     */
    @Bean
    public RouteLocator hystrixRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("hystrix_route", r -> r
                .path("/api/**")
                .filters(f -> f
                    .hystrix(config -> config
                        .setName("myCommandKey")
                        .setFallbackUri("forward:/fallback")
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * YAML Configuration (DEPRECATED):
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         - id: hystrix_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             - name: Hystrix
     *               args:
     *                 name: myCommandKey
     *                 fallbackUri: forward:/fallback
     * 
     * hystrix:
     *   command:
     *     myCommandKey:
     *       execution:
     *         isolation:
     *           thread:
     *             timeoutInMilliseconds: 3000
     *       circuitBreaker:
     *         requestVolumeThreshold: 20
     *         errorThresholdPercentage: 50
     * 
     * MIGRATION NOTE:
     * ===============
     * Replace Hystrix with Resilience4j CircuitBreaker:
     * 
     * .filters(f -> f
     *     .circuitBreaker(c -> c
     *         .setName("myCircuitBreaker")
     *         .setFallbackUri("forward:/fallback")
     *     )
     * )
     */
}
