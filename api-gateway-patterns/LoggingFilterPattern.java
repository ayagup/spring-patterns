import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Logging Filter Pattern - Spring Cloud Gateway
 * ============================================
 * 
 * Request/response logging for monitoring, debugging, and audit trails.
 * 
 * Logging Types:
 * - Request logging (method, path, headers, body)
 * - Response logging (status, headers, body)
 * - Performance logging (response time)
 * - Audit logging (who accessed what)
 * 
 * Use Cases:
 * - Debugging
 * - Monitoring
 * - Audit trails
 * - Performance analysis
 * - Security monitoring
 */
@Component
public class LoggingFilterPattern {

    @Component
    public static class RequestResponseLoggingFilterFactory
            extends AbstractGatewayFilterFactory<RequestResponseLoggingFilterFactory.Config> {
        
        public RequestResponseLoggingFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                long startTime = System.currentTimeMillis();
                
                if (config.isLogRequest()) {
                    System.out.println("Request: " + exchange.getRequest().getMethod() + 
                        " " + exchange.getRequest().getURI());
                }
                
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    if (config.isLogResponse()) {
                        long duration = System.currentTimeMillis() - startTime;
                        System.out.println("Response: " + exchange.getResponse().getStatusCode() +
                            " (" + duration + "ms)");
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
}
