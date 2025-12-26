import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Custom Filter Factory Pattern - Spring Cloud Gateway
 * ===================================================
 * 
 * Create custom gateway filters for specific business requirements.
 * 
 * Filter Factory Types:
 * - AbstractGatewayFilterFactory: Base class for custom filters
 * - AbstractNameValueGatewayFilterFactory: For name-value pair configuration
 * - Ordered: Control filter execution order
 * 
 * Use Cases:
 * - Custom authentication
 * - Business-specific logic
 * - Custom headers/transformations
 * - Integration with internal systems
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 */
public class CustomFilterFactoryPattern {

    /**
     * Example 1: Simple Custom Filter
     */
    @Component
    public static class CustomHeaderFilterFactory
            extends AbstractGatewayFilterFactory<CustomHeaderFilterFactory.Config> {
        
        public CustomHeaderFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                exchange.getRequest().mutate()
                    .header(config.getHeaderName(), config.getHeaderValue())
                    .build();
                
                return chain.filter(exchange);
            };
        }
        
        @Override
        public List<String> shortcutFieldOrder() {
            return Arrays.asList("headerName", "headerValue");
        }
        
        public static class Config {
            private String headerName;
            private String headerValue;
            
            public String getHeaderName() {
                return headerName;
            }
            
            public void setHeaderName(String headerName) {
                this.headerName = headerName;
            }
            
            public String getHeaderValue() {
                return headerValue;
            }
            
            public void setHeaderValue(String headerValue) {
                this.headerValue = headerValue;
            }
        }
    }

    /**
     * Example 2: Name-Value Filter
     */
    @Component
    public static class CustomNameValueFilterFactory
            extends AbstractNameValueGatewayFilterFactory {
        
        @Override
        public GatewayFilter apply(NameValueConfig config) {
            return (exchange, chain) -> {
                exchange.getRequest().mutate()
                    .header(config.getName(), config.getValue())
                    .build();
                
                return chain.filter(exchange);
            };
        }
    }

    /**
     * Example 3: Pre/Post Filter
     */
    @Component
    public static class PrePostFilterFactory
            extends AbstractGatewayFilterFactory<PrePostFilterFactory.Config> {
        
        public PrePostFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                // Pre-filter logic
                if (config.isPreProcess()) {
                    System.out.println("Pre-processing request");
                }
                
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    // Post-filter logic
                    if (config.isPostProcess()) {
                        System.out.println("Post-processing response");
                    }
                }));
            };
        }
        
        public static class Config {
            private boolean preProcess = true;
            private boolean postProcess = true;
            
            public boolean isPreProcess() {
                return preProcess;
            }
            
            public void setPreProcess(boolean preProcess) {
                this.preProcess = preProcess;
            }
            
            public boolean isPostProcess() {
                return postProcess;
            }
            
            public void setPostProcess(boolean postProcess) {
                this.postProcess = postProcess;
            }
        }
    }

    /**
     * Example 4: Ordered Custom Filter
     */
    @Component
    public static class OrderedCustomFilterFactory
            extends AbstractGatewayFilterFactory<OrderedCustomFilterFactory.Config>
            implements org.springframework.core.Ordered {
        
        public OrderedCustomFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                System.out.println("Executing ordered filter with order: " + config.getOrder());
                return chain.filter(exchange);
            };
        }
        
        @Override
        public int getOrder() {
            return -100;  // Execute early
        }
        
        public static class Config {
            private int order = 0;
            
            public int getOrder() {
                return order;
            }
            
            public void setOrder(int order) {
                this.order = order;
            }
        }
    }

    /**
     * Example 5: Conditional Custom Filter
     */
    @Component
    public static class ConditionalFilterFactory
            extends AbstractGatewayFilterFactory<ConditionalFilterFactory.Config> {
        
        public ConditionalFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                String headerValue = exchange.getRequest()
                    .getHeaders()
                    .getFirst(config.getHeaderName());
                
                if (headerValue != null && headerValue.equals(config.getHeaderValue())) {
                    // Apply custom logic when condition matches
                    exchange.getAttributes().put("conditionMatched", true);
                }
                
                return chain.filter(exchange);
            };
        }
        
        public static class Config {
            private String headerName;
            private String headerValue;
            
            public String getHeaderName() {
                return headerName;
            }
            
            public void setHeaderName(String headerName) {
                this.headerName = headerName;
            }
            
            public String getHeaderValue() {
                return headerValue;
            }
            
            public void setHeaderValue(String headerValue) {
                this.headerValue = headerValue;
            }
        }
    }

    /**
     * YAML Configuration:
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         - id: custom_filter_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             # Simple custom filter
     *             - name: CustomHeader
     *               args:
     *                 headerName: X-Custom-Header
     *                 headerValue: custom-value
     * 
     *             # Shortcut notation (if shortcutFieldOrder defined)
     *             - CustomHeader=X-Custom-Header, custom-value
     * 
     *             # Name-value filter
     *             - CustomNameValue=X-Header, value
     * 
     *             # Pre/post filter
     *             - name: PrePost
     *               args:
     *                 preProcess: true
     *                 postProcess: true
     * 
     *             # Conditional filter
     *             - name: Conditional
     *               args:
     *                 headerName: X-Feature-Flag
     *                 headerValue: enabled
     * 
     * Best Practices:
     * ===============
     * 
     * 1. Extend AbstractGatewayFilterFactory for custom filters
     * 2. Use @Component annotation for auto-discovery
     * 3. Define Config inner class for filter configuration
     * 4. Implement shortcutFieldOrder() for shortcut syntax
     * 5. Use Ordered interface to control execution order
     * 6. Handle errors gracefully in filter logic
     * 7. Document filter purpose and configuration
     * 8. Test filters thoroughly
     * 9. Use reactive operators properly
     * 10. Avoid blocking operations
     * 
     * Common Pitfalls:
     * ================
     * 
     * 1. Forgetting to call chain.filter()
     * 2. Blocking operations in reactive chain
     * 3. Not handling errors properly
     * 4. Incorrect filter order
     * 5. Missing @Component annotation
     * 6. Not defining Config class properly
     * 7. Not testing with various configurations
     * 8. Memory leaks from storing request data
     * 9. Not documenting filter behavior
     * 10. Overly complex filter logic
     * 
     * When to Use:
     * ============
     * 
     * - Custom authentication/authorization
     * - Business-specific transformations
     * - Integration with internal systems
     * - Custom monitoring/logging
     * - Specialized header manipulation
     * - Complex conditional logic
     * 
     * When NOT to Use:
     * ================
     * 
     * - Built-in filters already exist
     * - Complex business logic (belongs in services)
     * - Heavy computation (use async processing)
     * - Database operations (use services)
     * - Functionality better suited for Global Filters
     */
}
