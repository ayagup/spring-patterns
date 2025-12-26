package com.example.staticresources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Resource Chain Pattern
 * 
 * Demonstrates resource handling with resolvers and transformers in a chain.
 * The resource chain allows preprocessing of static resources.
 */
@SpringBootApplication
public class ResourceChainPattern {

    public static void main(String[] args) {
        SpringApplication.run(ResourceChainPattern.class, args);
    }

    /**
     * Basic resource chain configuration
     */
    @Configuration
    static class BasicChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true) // Enable resource chain
                    .addResolver(null) // Custom resolvers can be added
                    .addTransformer(null); // Custom transformers can be added
        }
    }

    /**
     * Resource chain with caching
     */
    @Configuration
    static class CachedChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/cached/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true)
                    .addResolver(null); // PathResourceResolver is added by default
        }
    }

    /**
     * Resource chain with compression support
     */
    @Configuration
    static class CompressedChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Enable resource chain for compressed resources
            registry.addResourceHandler("/compressed/**")
                    .addResourceLocations("classpath:/static/compressed/")
                    .resourceChain(true);
                    // GzipResourceResolver would be added here for gzip support
        }
    }

    /**
     * Resource chain with version strategy
     */
    @Configuration
    static class VersionedChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/versioned/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true);
                    // VersionResourceResolver would handle versioning
        }
    }

    /**
     * Resource chain with content version strategy
     */
    @Configuration
    static class ContentVersionChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/content-versioned/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(31536000)
                    .resourceChain(true);
                    // Content-based versioning (MD5 hash)
        }
    }

    /**
     * Resource chain with fixed version strategy
     */
    @Configuration
    static class FixedVersionChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/v1.0.0/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true);
                    // Fixed version in path: /v1.0.0/js/app.js
        }
    }

    /**
     * Resource chain with multiple resolvers
     */
    @Configuration
    static class MultiResolverChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/multi/**")
                    .addResourceLocations("classpath:/static/", "classpath:/public/")
                    .resourceChain(true);
                    // Can add: GzipResourceResolver, VersionResourceResolver, PathResourceResolver
        }
    }

    /**
     * Resource chain with transformers
     */
    @Configuration
    static class TransformerChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/transformed/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true);
                    // Can add: CssLinkResourceTransformer, AppCacheManifestTransformer
        }
    }

    /**
     * Production-optimized resource chain
     */
    @Configuration
    static class ProductionChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/prod/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(31536000) // 1 year cache
                    .resourceChain(true); // Chain with resolvers and transformers
                    // In production: Gzip + Version + Cache
        }
    }

    /**
     * Comprehensive resource chain configuration
     */
    @Configuration
    static class ComprehensiveChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // JavaScript resources with chain
            registry.addResourceHandler("/js/**")
                    .addResourceLocations("classpath:/static/js/")
                    .setCachePeriod(86400)
                    .resourceChain(true);

            // CSS resources with chain
            registry.addResourceHandler("/css/**")
                    .addResourceLocations("classpath:/static/css/")
                    .setCachePeriod(86400)
                    .resourceChain(true);

            // Image resources with chain
            registry.addResourceHandler("/img/**")
                    .addResourceLocations("classpath:/static/img/")
                    .setCachePeriod(2592000)
                    .resourceChain(true);

            // Font resources with chain
            registry.addResourceHandler("/fonts/**")
                    .addResourceLocations("classpath:/static/fonts/")
                    .setCachePeriod(31536000)
                    .resourceChain(true);
        }
    }
}
