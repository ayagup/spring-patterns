package com.example.staticresources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Resource Resolver Pattern
 * 
 * Demonstrates custom resource resolvers for locating static resources.
 * ResourceResolvers determine how to find resources based on request paths.
 */
@SpringBootApplication
public class ResourceResolverPattern {

    public static void main(String[] args) {
        SpringApplication.run(ResourceResolverPattern.class, args);
    }

    /**
     * Path Resource Resolver (default)
     */
    @Configuration
    static class PathResolverConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/path/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver());
        }
    }

    /**
     * Custom Resource Resolver
     */
    static class CustomResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            // Custom logic to resolve resources
            Resource resource = location.createRelative(resourcePath);
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            
            // Fallback logic
            return null;
        }
    }

    /**
     * Configuration with custom resolver
     */
    @Configuration
    static class CustomResolverConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/custom/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true)
                    .addResolver(new CustomResourceResolver());
        }
    }

    /**
     * Fallback Resource Resolver
     */
    static class FallbackResourceResolver extends PathResourceResolver {

        private final String fallbackResource;

        public FallbackResourceResolver(String fallbackResource) {
            this.fallbackResource = fallbackResource;
        }

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource resource = super.getResource(resourcePath, location);
            
            if (resource == null || !resource.exists()) {
                // Return fallback resource (e.g., index.html for SPA)
                resource = location.createRelative(fallbackResource);
            }
            
            return resource;
        }
    }

    /**
     * SPA (Single Page Application) resolver
     */
    @Configuration
    static class SPAResolverConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true)
                    .addResolver(new FallbackResourceResolver("index.html"));
        }
    }

    /**
     * Locale-based Resource Resolver
     */
    static class LocaleResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            // Try locale-specific resource first
            String locale = getCurrentLocale(); // Get from LocaleContextHolder
            String localizedPath = locale + "/" + resourcePath;
            
            Resource localizedResource = location.createRelative(localizedPath);
            if (localizedResource.exists()) {
                return localizedResource;
            }
            
            // Fall back to default resource
            return super.getResource(resourcePath, location);
        }

        private String getCurrentLocale() {
            // In real implementation: LocaleContextHolder.getLocale().toString()
            return "en";
        }
    }

    /**
     * Version-aware Resource Resolver
     */
    static class VersionResourceResolver extends PathResourceResolver {

        private final String version;

        public VersionResourceResolver(String version) {
            this.version = version;
        }

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            // Remove version prefix if present
            String actualPath = resourcePath.replaceFirst("^" + version + "/", "");
            return super.getResource(actualPath, location);
        }
    }

    /**
     * Gzip Resource Resolver simulation
     */
    static class GzipResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            // Check if gzip version exists
            Resource gzipResource = location.createRelative(resourcePath + ".gz");
            
            if (gzipResource.exists() && acceptsGzip()) {
                return gzipResource;
            }
            
            // Return original resource
            return super.getResource(resourcePath, location);
        }

        private boolean acceptsGzip() {
            // In real implementation: check Accept-Encoding header
            return true;
        }
    }

    /**
     * Cache-busting Resource Resolver
     */
    static class CacheBustingResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            // Remove cache-busting query parameters or hash
            String cleanPath = resourcePath.replaceAll("\\?.*$", "");
            cleanPath = cleanPath.replaceAll("-[a-f0-9]{8,}\\.", ".");
            
            return super.getResource(cleanPath, location);
        }
    }

    /**
     * Multiple resolvers configuration
     */
    @Configuration
    static class MultipleResolversConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/multi/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true)
                    .addResolver(new GzipResourceResolver())
                    .addResolver(new PathResourceResolver());
        }
    }

    /**
     * Comprehensive resolver configuration
     */
    @Configuration
    static class ComprehensiveResolverConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Production assets with multiple resolvers
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations("classpath:/static/assets/")
                    .setCachePeriod(31536000)
                    .resourceChain(true)
                    .addResolver(new CacheBustingResourceResolver())
                    .addResolver(new GzipResourceResolver())
                    .addResolver(new PathResourceResolver());

            // Versioned resources
            registry.addResourceHandler("/v1/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true)
                    .addResolver(new VersionResourceResolver("v1"));

            // Localized resources
            registry.addResourceHandler("/i18n/**")
                    .addResourceLocations("classpath:/static/i18n/")
                    .resourceChain(true)
                    .addResolver(new LocaleResourceResolver());
        }
    }
}
