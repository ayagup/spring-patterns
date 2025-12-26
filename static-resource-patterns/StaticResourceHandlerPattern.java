package com.example.staticresources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Static Resource Handler Pattern
 * 
 * Demonstrates how to configure static resource handling in Spring MVC.
 * Maps URL patterns to physical resource locations.
 */
@SpringBootApplication
public class StaticResourceHandlerPattern {

    public static void main(String[] args) {
        SpringApplication.run(StaticResourceHandlerPattern.class, args);
    }

    /**
     * Basic static resource configuration
     */
    @Configuration
    static class BasicResourceConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Serve static files from classpath:/static/
            registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/");

            // Serve images from classpath:/images/
            registry.addResourceHandler("/images/**")
                    .addResourceLocations("classpath:/images/");

            // Serve CSS files
            registry.addResourceHandler("/css/**")
                    .addResourceLocations("classpath:/css/");

            // Serve JavaScript files
            registry.addResourceHandler("/js/**")
                    .addResourceLocations("classpath:/js/");
        }
    }

    /**
     * Multiple resource locations
     */
    @Configuration
    static class MultipleLocationConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Serve from multiple locations (checked in order)
            registry.addResourceHandler("/resources/**")
                    .addResourceLocations(
                            "classpath:/static/",
                            "classpath:/public/",
                            "file:/var/www/resources/"
                    );
        }
    }

    /**
     * External file system resources
     */
    @Configuration
    static class FileSystemResourceConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Serve from file system
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:/uploads/");

            // Serve from Windows path
            // registry.addResourceHandler("/files/**")
            //         .addResourceLocations("file:///C:/app/files/");

            // Serve from Unix path
            registry.addResourceHandler("/media/**")
                    .addResourceLocations("file:/var/media/");
        }
    }

    /**
     * Cache control configuration
     */
    @Configuration
    static class CacheControlConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Cache for 1 year
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations("classpath:/assets/")
                    .setCachePeriod(31536000); // 365 days in seconds

            // No cache for dynamic content
            registry.addResourceHandler("/dynamic/**")
                    .addResourceLocations("classpath:/dynamic/")
                    .setCachePeriod(0);
        }
    }

    /**
     * Resource handler with chain
     */
    @Configuration
    static class ResourceChainConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/public/**")
                    .addResourceLocations("classpath:/public/")
                    .setCachePeriod(3600)
                    .resourceChain(true); // Enable resource chain
        }
    }

    /**
     * Conditional resource handling
     */
    @Configuration
    static class ConditionalResourceConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Development resources
            if (isDevelopmentMode()) {
                registry.addResourceHandler("/dev/**")
                        .addResourceLocations("classpath:/dev/")
                        .setCachePeriod(0);
            }

            // Production resources
            if (isProductionMode()) {
                registry.addResourceHandler("/prod/**")
                        .addResourceLocations("classpath:/prod/")
                        .setCachePeriod(31536000);
            }
        }

        private boolean isDevelopmentMode() {
            return System.getProperty("spring.profiles.active", "dev").equals("dev");
        }

        private boolean isProductionMode() {
            return System.getProperty("spring.profiles.active", "").equals("prod");
        }
    }

    /**
     * Default servlet handling (delegate to container's default servlet)
     */
    @Configuration
    static class DefaultServletConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Map unmapped requests to default servlet
            registry.addResourceHandler("/**")
                    .addResourceLocations("classpath:/static/");

            // Explicitly map certain patterns
            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
    }

    /**
     * Custom resource path patterns
     */
    @Configuration
    static class CustomPathConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Version-specific resources
            registry.addResourceHandler("/v1/**")
                    .addResourceLocations("classpath:/static/v1/");

            registry.addResourceHandler("/v2/**")
                    .addResourceLocations("classpath:/static/v2/");

            // Locale-specific resources
            registry.addResourceHandler("/en/**")
                    .addResourceLocations("classpath:/static/en/");

            registry.addResourceHandler("/es/**")
                    .addResourceLocations("classpath:/static/es/");
        }
    }

    /**
     * Comprehensive configuration example
     */
    @Configuration
    static class ComprehensiveResourceConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Static web resources
            registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(86400); // 1 day

            // Images with long cache
            registry.addResourceHandler("/img/**")
                    .addResourceLocations("classpath:/static/images/")
                    .setCachePeriod(2592000); // 30 days

            // CSS and JS with versioning support
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations("classpath:/assets/")
                    .setCachePeriod(31536000)
                    .resourceChain(true);

            // User uploads (file system)
            registry.addResourceHandler("/user-content/**")
                    .addResourceLocations("file:uploads/user-content/")
                    .setCachePeriod(3600); // 1 hour

            // Documentation files
            registry.addResourceHandler("/docs/**")
                    .addResourceLocations("classpath:/docs/")
                    .setCachePeriod(86400);
        }
    }
}
