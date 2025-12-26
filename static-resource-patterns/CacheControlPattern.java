package com.example.staticresources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.concurrent.TimeUnit;

/**
 * Cache Control Pattern
 * 
 * Demonstrates how to configure HTTP cache control headers for static resources.
 * Proper caching improves performance and reduces server load.
 */
@SpringBootApplication
public class CacheControlPattern {

    public static void main(String[] args) {
        SpringApplication.run(CacheControlPattern.class, args);
    }

    /**
     * Basic cache control with setCachePeriod
     */
    @Configuration
    static class BasicCacheConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Cache for 1 hour (3600 seconds)
            registry.addResourceHandler("/cached/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(3600);
        }
    }

    /**
     * No cache configuration
     */
    @Configuration
    static class NoCacheConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // No caching
            registry.addResourceHandler("/no-cache/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(0);
        }
    }

    /**
     * Advanced Cache Control using CacheControl builder
     */
    @Configuration
    static class AdvancedCacheConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Max age with public cache
            registry.addResourceHandler("/public/**")
                    .addResourceLocations("classpath:/static/public/")
                    .setCacheControl(CacheControl
                            .maxAge(1, TimeUnit.DAYS)
                            .cachePublic());

            // Private cache
            registry.addResourceHandler("/private/**")
                    .addResourceLocations("classpath:/static/private/")
                    .setCacheControl(CacheControl
                            .maxAge(1, TimeUnit.HOURS)
                            .cachePrivate());

            // No store (don't cache at all)
            registry.addResourceHandler("/sensitive/**")
                    .addResourceLocations("classpath:/static/sensitive/")
                    .setCacheControl(CacheControl.noStore());
        }
    }

    /**
     * Immutable resources (long-term caching)
     */
    @Configuration
    static class ImmutableCacheConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Immutable resources (versioned assets)
            registry.addResourceHandler("/immutable/**")
                    .addResourceLocations("classpath:/static/immutable/")
                    .setCacheControl(CacheControl
                            .maxAge(365, TimeUnit.DAYS)
                            .cachePublic()
                            .immutable());
        }
    }

    /**
     * Must revalidate cache
     */
    @Configuration
    static class RevalidateCacheConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/revalidate/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(1, TimeUnit.HOURS)
                            .mustRevalidate());
        }
    }

    /**
     * Proxy revalidate cache
     */
    @Configuration
    static class ProxyRevalidateConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/proxy/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(1, TimeUnit.DAYS)
                            .proxyRevalidate());
        }
    }

    /**
     * S-Max-Age for shared caches (CDN)
     */
    @Configuration
    static class SharedCacheConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/cdn/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(1, TimeUnit.HOURS)  // Browser cache
                            .sMaxAge(7, TimeUnit.DAYS)  // Shared/proxy cache
                            .cachePublic());
        }
    }

    /**
     * Stale while revalidate
     */
    @Configuration
    static class StaleConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/stale/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(1, TimeUnit.HOURS)
                            .staleWhileRevalidate(10, TimeUnit.MINUTES));
        }
    }

    /**
     * Comprehensive caching strategy
     */
    @Configuration
    static class ComprehensiveCacheConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Static images - long cache
            registry.addResourceHandler("/images/**")
                    .addResourceLocations("classpath:/static/images/")
                    .setCacheControl(CacheControl
                            .maxAge(30, TimeUnit.DAYS)
                            .cachePublic());

            // CSS/JS - moderate cache with versioning
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations("classpath:/static/assets/")
                    .setCacheControl(CacheControl
                            .maxAge(1, TimeUnit.DAYS)
                            .mustRevalidate());

            // HTML - short cache or no cache
            registry.addResourceHandler("/pages/**")
                    .addResourceLocations("classpath:/static/pages/")
                    .setCacheControl(CacheControl
                            .maxAge(5, TimeUnit.MINUTES)
                            .mustRevalidate());

            // API responses - no cache
            registry.addResourceHandler("/api-docs/**")
                    .addResourceLocations("classpath:/static/docs/")
                    .setCacheControl(CacheControl.noCache());

            // Fonts - long cache, immutable
            registry.addResourceHandler("/fonts/**")
                    .addResourceLocations("classpath:/static/fonts/")
                    .setCacheControl(CacheControl
                            .maxAge(365, TimeUnit.DAYS)
                            .immutable()
                            .cachePublic());

            // Videos - moderate cache
            registry.addResourceHandler("/videos/**")
                    .addResourceLocations("classpath:/static/videos/")
                    .setCacheControl(CacheControl
                            .maxAge(7, TimeUnit.DAYS)
                            .cachePublic());
        }
    }
}
