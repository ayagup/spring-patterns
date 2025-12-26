import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Caching Pattern - Spring Cloud Gateway
 * ======================================
 * 
 * Cache responses to reduce backend load and improve performance.
 * 
 * Caching Strategies:
 * - TTL (Time To Live)
 * - LRU (Least Recently Used)
 * - Cache invalidation
 * - Conditional caching (based on headers, status)
 * 
 * Use Cases:
 * - Static content caching
 * - API response caching
 * - Reduce backend load
 * - Improve response time
 */
@Configuration
@EnableCaching
public class CachingPattern {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("gatewayCache");
    }

    @Component
    public static class ResponseCachingFilterFactory
            extends AbstractGatewayFilterFactory<ResponseCachingFilterFactory.Config> {
        
        private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
        
        public ResponseCachingFilterFactory() {
            super(Config.class);
        }
        
        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                String cacheKey = exchange.getRequest().getURI().toString();
                
                // Check cache
                CachedResponse cached = cache.get(cacheKey);
                if (cached != null && !cached.isExpired(config.getTtlSeconds())) {
                    exchange.getResponse().getHeaders().add("X-Cache", "HIT");
                    // Return cached response (simplified)
                    return Mono.empty();
                }
                
                // Cache miss - proceed with request
                exchange.getResponse().getHeaders().add("X-Cache", "MISS");
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    // Store response in cache (simplified)
                    cache.put(cacheKey, new CachedResponse(System.currentTimeMillis()));
                }));
            };
        }
        
        private static class CachedResponse {
            private final long timestamp;
            
            public CachedResponse(long timestamp) {
                this.timestamp = timestamp;
            }
            
            public boolean isExpired(int ttlSeconds) {
                return (System.currentTimeMillis() - timestamp) > (ttlSeconds * 1000);
            }
        }
        
        public static class Config {
            private int ttlSeconds = 300;  // 5 minutes default
            
            public int getTtlSeconds() {
                return ttlSeconds;
            }
            
            public void setTtlSeconds(int ttlSeconds) {
                this.ttlSeconds = ttlSeconds;
            }
        }
    }
}
