import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.Mod

ifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Request/Response Modification Pattern - Spring Cloud Gateway
 * ===========================================================
 * 
 * Modify request/response headers, body, and metadata during routing.
 * 
 * Modification Types:
 * - Header Modification: Add, remove, set headers
 * - Body Modification: Transform request/response body
 * - Parameter Modification: Add, modify query parameters
 * - Status Modification: Change HTTP status code
 * 
 * Use Cases:
 * - Add correlation/tracking headers
 * - Remove sensitive headers
 * - Transform request/response format
 * - Inject authentication tokens
 * - Sanitize responses
 * - Add security headers
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 */
@Configuration
public class RequestResponseModificationPattern {

    /**
     * Example 1: Request Header Modification
     */
    @Bean
    public RouteLocator requestHeaderModificationRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("add_request_headers", r -> r
                .path("/api/**")
                .filters(f -> f
                    .addRequestHeader("X-Request-Source", "gateway")
                    .addRequestHeader("X-Correlation-ID", "#{T(java.util.UUID).randomUUID().toString()}")
                    .addRequestHeader("X-Forwarded-For", "#{@environment['server.address']}")
                    .removeRequestHeader("Cookie")
                    .setRequestHeader("Host", "backend-service")
                )
                .uri("http://localhost:8081"))
            .build();
    }

    /**
     * Example 2: Response Header Modification
     */
    @Bean
    public RouteLocator responseHeaderModificationRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("add_response_headers", r -> r
                .path("/api/**")
                .filters(f -> f
                    .addResponseHeader("X-Response-Time", "#{T(System).currentTimeMillis()}")
                    .addResponseHeader("X-Gateway-Version", "1.0")
                    .addResponseHeader("X-Content-Type-Options", "nosniff")
                    .removeResponseHeader("Server")
                    .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                )
                .uri("http://localhost:8081"))
            .build();
    }

    /**
     * Example 3: Request Parameter Modification
     */
    @Bean
    public RouteLocator requestParameterModificationRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("add_request_parameter", r -> r
                .path("/api/search")
                .filters(f -> f
                    .addRequestParameter("source", "gateway")
                    .addRequestParameter("timestamp", "#{T(System).currentTimeMillis()}")
                )
                .uri("http://localhost:8081"))
            .build();
    }

    /**
     * Example 4: Request Body Modification
     */
    @Bean
    public RouteLocator requestBodyModificationRoutes(RouteLocatorBuilder builder, 
                                                      ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter) {
        return builder.routes()
            .route("modify_request_body", r -> r
                .path("/api/users")
                .filters(f -> f
                    .modifyRequestBody(String.class, String.class, 
                        (exchange, originalBody) -> {
                            // Add gateway metadata to JSON body
                            String modified = originalBody.replace("}", 
                                ", \"source\": \"gateway\", \"timestamp\": " + 
                                System.currentTimeMillis() + "}");
                            return Mono.just(modified);
                        })
                )
                .uri("http://localhost:8081"))
            .build();
    }

    /**
     * Example 5: Response Body Modification
     */
    @Bean
    public RouteLocator responseBodyModificationRoutes(RouteLocatorBuilder builder,
                                                       ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter) {
        return builder.routes()
            .route("modify_response_body", r -> r
                .path("/api/**")
                .filters(f -> f
                    .modifyResponseBody(String.class, String.class,
                        (exchange, originalBody) -> {
                            // Sanitize response (remove sensitive fields)
                            String sanitized = originalBody
                                .replaceAll("\"password\":\"[^\"]*\"", "\"password\":\"***\"")
                                .replaceAll("\"ssn\":\"[^\"]*\"", "\"ssn\":\"***\"");
                            return Mono.just(sanitized);
                        })
                )
                .uri("http://localhost:8081"))
            .build();
    }

    /**
     * Example 6: Status Code Modification
     */
    @Bean
    public RouteLocator statusCodeModificationRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("set_status", r -> r
                .path("/maintenance")
                .filters(f -> f
                    .setStatus(HttpStatus.SERVICE_UNAVAILABLE)
                )
                .uri("no://op"))
            
            .route("deprecated_api", r -> r
                .path("/api/v1/**")
                .filters(f -> f
                    .addResponseHeader("X-API-Deprecated", "true")
                    .addResponseHeader("X-API-Sunset", "2025-12-31")
                )
                .uri("http://localhost:8081"))
            .build();
    }

    /**
     * Example 7: Custom Request Modifier Filter
     */
    @Component
    public static class CustomRequestModifierFilterFactory 
            extends AbstractGatewayFilterFactory<CustomRequestModifierFilterFactory.Config> {
        
        public CustomRequestModifierFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                ServerHttpRequest request = exchange.getRequest();
                
                // Build modified request
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Custom-Header", config.getHeaderValue())
                    .header("X-Timestamp", String.valueOf(System.currentTimeMillis()))
                    .header("X-Client-IP", getClientIp(request))
                    .build();
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            };
        }
        
        private String getClientIp(ServerHttpRequest request) {
            String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
            if (forwardedFor != null) {
                return forwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        
        public static class Config {
            private String headerValue = "default";
            
            public String getHeaderValue() {
                return headerValue;
            }
            
            public void setHeaderValue(String headerValue) {
                this.headerValue = headerValue;
            }
        }
    }

    /**
     * Example 8: Custom Response Modifier Filter
     */
    @Component
    public static class CustomResponseModifierFilterFactory
            extends AbstractGatewayFilterFactory<CustomResponseModifierFilterFactory.Config> {
        
        public CustomResponseModifierFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    
                    // Add custom response headers
                    response.getHeaders().add("X-Response-From", "gateway");
                    response.getHeaders().add("X-Cache-Status", config.getCacheStatus());
                    
                    if (config.isAddTimestamp()) {
                        response.getHeaders().add("X-Response-Timestamp", 
                            String.valueOf(System.currentTimeMillis()));
                    }
                }));
            };
        }
        
        public static class Config {
            private String cacheStatus = "MISS";
            private boolean addTimestamp = true;
            
            public String getCacheStatus() {
                return cacheStatus;
            }
            
            public void setCacheStatus(String cacheStatus) {
                this.cacheStatus = cacheStatus;
            }
            
            public boolean isAddTimestamp() {
                return addTimestamp;
            }
            
            public void setAddTimestamp(boolean addTimestamp) {
                this.addTimestamp = addTimestamp;
            }
        }
    }

    /**
     * Example 9: Content-Type Transformation
     */
    @Bean
    public RouteLocator contentTypeTransformationRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("json_to_xml", r -> r
                .path("/api/xml/**")
                .and().header("Accept", "application/xml")
                .filters(f -> f
                    .setRequestHeader("Accept", "application/json")
                    .modifyResponseBody(String.class, String.class,
                        (exchange, jsonBody) -> {
                            // Convert JSON to XML (simplified)
                            String xml = convertJsonToXml(jsonBody);
                            exchange.getResponse().getHeaders()
                                .setContentType(MediaType.APPLICATION_XML);
                            return Mono.just(xml);
                        })
                )
                .uri("http://localhost:8081"))
            .build();
    }
    
    private String convertJsonToXml(String json) {
        // Simplified conversion - use proper library in production
        return "<response>" + json + "</response>";
    }

    /**
     * Example 10: Request/Response Logging Filter
     */
    @Component
    public static class LoggingModificationFilterFactory
            extends AbstractGatewayFilterFactory<LoggingModificationFilterFactory.Config> {
        
        public LoggingModificationFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                ServerHttpRequest request = exchange.getRequest();
                
                if (config.isLogRequest()) {
                    System.out.println("=== Request ===");
                    System.out.println("Method: " + request.getMethod());
                    System.out.println("Path: " + request.getPath());
                    System.out.println("Headers: " + request.getHeaders());
                }
                
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    if (config.isLogResponse()) {
                        ServerHttpResponse response = exchange.getResponse();
                        System.out.println("=== Response ===");
                        System.out.println("Status: " + response.getStatusCode());
                        System.out.println("Headers: " + response.getHeaders());
                    }
                }));
            };
        }
        
        public static class Config {
            private boolean logRequest = true;
            private boolean logResponse = true;
            
            public boolean isLogRequest() {
                return logRequest;
            }
            
            public void setLogRequest(boolean logRequest) {
                this.logRequest = logRequest;
            }
            
            public boolean isLogResponse() {
                return logResponse;
            }
            
            public void setLogResponse(boolean logResponse) {
                this.logResponse = logResponse;
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
     *         - id: modify_headers_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             - AddRequestHeader=X-Request-Source, gateway
     *             - AddResponseHeader=X-Gateway-Version, 1.0
     *             - RemoveRequestHeader=Cookie
     *             - RemoveResponseHeader=Server
     * 
     *         - id: modify_body_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/users
     *           filters:
     *             - name: ModifyRequestBody
     *               args:
     *                 inClass: java.lang.String
     *                 outClass: java.lang.String
     * 
     * Best Practices:
     * 1. Validate modified content
     * 2. Preserve important headers
     * 3. Log modifications for debugging
     * 4. Handle errors gracefully
     * 5. Use reactive operators properly
     * 6. Avoid blocking operations
     * 7. Test with various content types
     * 8. Document modifications
     * 9. Consider performance impact
     * 10. Sanitize sensitive data
     * 
     * Common Pitfalls:
     * 1. Modifying content-length header incorrectly
     * 2. Blocking in reactive chain
     * 3. Not handling errors in body modification
     * 4. Losing important headers
     * 5. Breaking request/response format
     * 6. Not considering content encoding
     * 7. Memory issues with large bodies
     * 8. Not preserving original request
     * 9. Incorrect content-type handling
     * 10. Not testing edge cases
     */
}
