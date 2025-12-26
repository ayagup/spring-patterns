package com.example.templateengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Template Caching Pattern
 * 
 * Demonstrates template caching strategies for improved performance.
 * Proper caching can significantly reduce template processing overhead.
 * 
 * Features:
 * - Template resolution caching
 * - Compiled template caching
 * - Fragment caching
 * - Cache configuration
 * - Cache invalidation
 * - Development vs production modes
 * 
 * Benefits:
 * - Improved performance
 * - Reduced I/O operations
 * - Lower CPU usage
 * - Better scalability
 */
@SpringBootApplication
@EnableCaching
public class TemplateCachingPattern {

    public static void main(String[] args) {
        SpringApplication.run(TemplateCachingPattern.class, args);
    }

    /**
     * Thymeleaf Caching Configuration
     */
    @Configuration
    public static class ThymeleafCachingConfig {

        /**
         * Template Resolver with caching enabled
         */
        @Bean
        public SpringResourceTemplateResolver templateResolver() {
            SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
            resolver.setPrefix("classpath:/templates/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode(TemplateMode.HTML);
            resolver.setCharacterEncoding("UTF-8");
            
            // Enable template caching (production mode)
            resolver.setCacheable(true);
            
            // Cache TTL in milliseconds (null = no expiration)
            resolver.setCacheTTLMs(3600000L); // 1 hour
            
            // Check template modifications (development mode)
            resolver.setCheckExistence(false);
            
            return resolver;
        }

        /**
         * Template Engine with caching configuration
         */
        @Bean
        public SpringTemplateEngine templateEngine() {
            SpringTemplateEngine engine = new SpringTemplateEngine();
            engine.setTemplateResolver(templateResolver());
            
            // Enable Spring EL compiler for better performance
            engine.setEnableSpringELCompiler(true);
            
            // Message resolver caching
            engine.setMessageResolverCacheable(true);
            
            // Template cache size (default: 200)
            // Note: This is managed by the template resolver
            
            return engine;
        }

        /**
         * Development Mode Configuration
         */
        @Bean
        public SpringResourceTemplateResolver devTemplateResolver() {
            SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
            resolver.setPrefix("classpath:/templates/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode(TemplateMode.HTML);
            resolver.setCharacterEncoding("UTF-8");
            
            // Disable caching for development
            resolver.setCacheable(false);
            
            // Check for template changes
            resolver.setCheckExistence(true);
            
            return resolver;
        }

        /**
         * Production Mode Configuration
         */
        @Bean
        public SpringResourceTemplateResolver prodTemplateResolver() {
            SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
            resolver.setPrefix("classpath:/templates/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode(TemplateMode.HTML);
            resolver.setCharacterEncoding("UTF-8");
            
            // Enable aggressive caching for production
            resolver.setCacheable(true);
            resolver.setCacheTTLMs(null); // Cache forever
            resolver.setCheckExistence(false);
            
            return resolver;
        }
    }

    /**
     * Cache Manager Configuration
     */
    @Configuration
    public static class CacheConfig {

        @Bean
        public CacheManager cacheManager() {
            // Simple in-memory cache manager
            ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
            cacheManager.setCacheNames(java.util.Arrays.asList(
                "templateFragments",
                "processedData",
                "userPreferences"
            ));
            return cacheManager;
        }
    }

    /**
     * Service with cacheable operations
     */
    @org.springframework.stereotype.Service
    public static class TemplateDataService {

        @org.springframework.cache.annotation.Cacheable("processedData")
        public String getProcessedData(String key) {
            // Expensive operation
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Processed: " + key;
        }

        @org.springframework.cache.annotation.CacheEvict(value = "processedData", key = "#key")
        public void invalidateCache(String key) {
            // Cache invalidation
        }

        @org.springframework.cache.annotation.CacheEvict(value = "processedData", allEntries = true)
        public void clearAllCache() {
            // Clear all cache entries
        }
    }

    /**
     * Controller demonstrating template caching
     */
    @Controller
    public static class CachingController {

        private final TemplateDataService dataService;

        public CachingController(TemplateDataService dataService) {
            this.dataService = dataService;
        }

        @GetMapping("/cache/demo")
        public String cacheDemo(Model model) {
            // First call: slow (1 second)
            // Subsequent calls: fast (cached)
            String data = dataService.getProcessedData("demo");
            model.addAttribute("data", data);
            model.addAttribute("timestamp", System.currentTimeMillis());
            return "cache/demo";
        }

        @GetMapping("/cache/clear")
        public String clearCache() {
            dataService.clearAllCache();
            return "redirect:/cache/demo";
        }
    }
}

/*
 * application.properties - Template Caching Configuration:
 * 
 * # Thymeleaf cache settings
 * spring.thymeleaf.cache=true
 * spring.thymeleaf.check-template=false
 * spring.thymeleaf.check-template-location=true
 * 
 * # Development mode (disable caching)
 * # spring.thymeleaf.cache=false
 * 
 * # FreeMarker cache settings
 * spring.freemarker.cache=true
 * spring.freemarker.check-template-location=true
 * 
 * # Mustache cache settings
 * spring.mustache.cache=true
 * 
 * # Spring cache
 * spring.cache.type=simple
 * spring.cache.cache-names=templateFragments,processedData
 * 
 * 
 * application-dev.properties (Development):
 * 
 * spring.thymeleaf.cache=false
 * spring.freemarker.cache=false
 * spring.mustache.cache=false
 * 
 * 
 * application-prod.properties (Production):
 * 
 * spring.thymeleaf.cache=true
 * spring.freemarker.cache=true
 * spring.mustache.cache=true
 * 
 * 
 * Cache Types:
 * 
 * 1. Template Resolution Cache
 *    - Caches template file locations
 *    - Avoids repeated file system lookups
 *    - Configured via resolver.setCacheable()
 * 
 * 2. Compiled Template Cache
 *    - Caches parsed/compiled templates
 *    - Avoids re-parsing template files
 *    - Most significant performance gain
 * 
 * 3. Fragment Cache
 *    - Caches rendered fragment output
 *    - Useful for expensive fragments
 *    - Requires custom implementation
 * 
 * 4. Data Cache
 *    - Caches model data
 *    - Uses Spring Cache abstraction
 *    - Reduces database queries
 * 
 * 
 * Cache Strategies:
 * 
 * Development Mode:
 * - Disable all caching
 * - Enable template change detection
 * - Fast iteration
 * - No cache invalidation needed
 * 
 * Production Mode:
 * - Enable aggressive caching
 * - Disable change detection
 * - Maximum performance
 * - Manual cache invalidation if needed
 * 
 * Hybrid Mode:
 * - Cache with TTL
 * - Periodic cache refresh
 * - Balance between performance and updates
 * 
 * 
 * Cache Invalidation Strategies:
 * 
 * 1. Time-based (TTL)
 *    - Automatic expiration after time period
 *    - Good for semi-static content
 * 
 * 2. Manual Invalidation
 *    - Explicit cache clearing
 *    - Full control over cache lifecycle
 * 
 * 3. Event-based Invalidation
 *    - Clear cache on specific events
 *    - Content updates trigger invalidation
 * 
 * 4. Deployment-based
 *    - Clear cache on application restart
 *    - Simple and effective
 * 
 * 
 * Custom Fragment Caching (Thymeleaf):
 * 
 * @Component
 * public class FragmentCacheProcessor extends AbstractAttributeTagProcessor {
 *     
 *     private final CacheManager cacheManager;
 *     
 *     public FragmentCacheProcessor(TemplateMode templateMode, 
 *                                   String dialectPrefix, 
 *                                   CacheManager cacheManager) {
 *         super(templateMode, dialectPrefix, "cache", false, null, false, 1000);
 *         this.cacheManager = cacheManager;
 *     }
 *     
 *     @Override
 *     protected void doProcess(ITemplateContext context, 
 *                             IProcessableElementTag tag, 
 *                             AttributeName attributeName, 
 *                             String attributeValue, 
 *                             IElementTagStructureHandler structureHandler) {
 *         
 *         Cache cache = cacheManager.getCache("templateFragments");
 *         String cacheKey = attributeValue;
 *         String cachedContent = cache.get(cacheKey, String.class);
 *         
 *         if (cachedContent != null) {
 *             structureHandler.replaceWith(cachedContent, false);
 *         } else {
 *             // Process fragment and cache result
 *             // Implementation depends on requirements
 *         }
 *     }
 * }
 * 
 * 
 * Performance Best Practices:
 * 
 * 1. Always enable caching in production
 * 2. Use appropriate cache TTL
 * 3. Cache expensive operations separately
 * 4. Monitor cache hit rates
 * 5. Use fragment caching for expensive fragments
 * 6. Pre-compile templates in production
 * 7. Minimize template complexity
 * 8. Use efficient template expressions
 * 9. Avoid unnecessary template processing
 * 10. Profile and optimize hot paths
 * 
 * 
 * Monitoring Cache Performance:
 * 
 * @Component
 * public class CacheMonitor {
 *     
 *     @Scheduled(fixedRate = 60000)
 *     public void logCacheStatistics() {
 *         CacheManager cacheManager = ...;
 *         cacheManager.getCacheNames().forEach(name -> {
 *             Cache cache = cacheManager.getCache(name);
 *             // Log cache statistics
 *         });
 *     }
 * }
 */
