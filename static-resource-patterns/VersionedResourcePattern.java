package com.example.staticresources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.concurrent.TimeUnit;

/**
 * Versioned Resource Pattern
 * 
 * Demonstrates strategies for versioning static resources to enable
 * aggressive caching while allowing cache invalidation when content changes.
 */
@SpringBootApplication
public class VersionedResourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(VersionedResourcePattern.class, args);
    }

    /**
     * Fixed version in URL path
     * Example: /v1.0.0/js/app.js
     */
    @Configuration
    static class FixedVersionConfig implements WebMvcConfigurer {

        private static final String VERSION = "1.0.0";

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/v" + VERSION + "/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(365, TimeUnit.DAYS)
                            .immutable());
        }
    }

    /**
     * Content-based versioning (MD5 hash)
     * Example: /js/app-d41d8cd98f.js
     */
    @Configuration
    static class ContentVersionConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/content/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(31536000)
                    .resourceChain(true);
                    // VersionResourceResolver with content strategy would be added
        }
    }

    /**
     * Timestamp-based versioning
     * Example: /assets?v=1234567890
     */
    @Configuration
    static class TimestampVersionConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/timestamped/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(365, TimeUnit.DAYS));
                    // Query parameter: ?v=timestamp
        }
    }

    /**
     * Build number versioning
     * Example: /build/123/assets/app.js
     */
    @Configuration
    static class BuildVersionConfig implements WebMvcConfigurer {

        private static final String BUILD_NUMBER = System.getProperty("build.number", "123");

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/build/" + BUILD_NUMBER + "/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(365, TimeUnit.DAYS)
                            .immutable());
        }
    }

    /**
     * Git commit hash versioning
     * Example: /assets/abc123de/app.js
     */
    @Configuration
    static class GitHashVersionConfig implements WebMvcConfigurer {

        private static final String GIT_HASH = "abc123de"; // From build process

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/assets/" + GIT_HASH + "/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(365, TimeUnit.DAYS)
                            .immutable());
        }
    }

    /**
     * Semantic versioning
     * Example: /v2/api/resources.js
     */
    @Configuration
    static class SemanticVersionConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Version 1
            registry.addResourceHandler("/v1/**")
                    .addResourceLocations("classpath:/static/v1/")
                    .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

            // Version 2
            registry.addResourceHandler("/v2/**")
                    .addResourceLocations("classpath:/static/v2/")
                    .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

            // Version 3 (latest)
            registry.addResourceHandler("/v3/**")
                    .addResourceLocations("classpath:/static/v3/")
                    .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS));
        }
    }

    /**
     * Fingerprint-based versioning
     * Example: /js/app.min.d41d8cd98f.js
     */
    @Configuration
    static class FingerprintVersionConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/fingerprinted/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(365, TimeUnit.DAYS)
                            .immutable())
                    .resourceChain(true);
                    // Fingerprint added to filename
        }
    }

    /**
     * Environment-based versioning
     */
    @Configuration
    static class EnvironmentVersionConfig implements WebMvcConfigurer {

        private final String environment = System.getProperty("spring.profiles.active", "dev");

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            if ("prod".equals(environment)) {
                // Production: long cache with version
                registry.addResourceHandler("/assets/**")
                        .addResourceLocations("classpath:/static/prod/")
                        .setCacheControl(CacheControl
                                .maxAge(365, TimeUnit.DAYS)
                                .immutable());
            } else {
                // Development: no cache
                registry.addResourceHandler("/assets/**")
                        .addResourceLocations("classpath:/static/dev/")
                        .setCacheControl(CacheControl.noCache());
            }
        }
    }

    /**
     * Comprehensive versioning strategy
     */
    @Configuration
    static class ComprehensiveVersioningConfig implements WebMvcConfigurer {

        private static final String APP_VERSION = "2.0.0";
        private static final String BUILD_ID = "20231129";

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Versioned by app version and build ID
            String versionPath = "/v" + APP_VERSION + "/build" + BUILD_ID;
            
            registry.addResourceHandler(versionPath + "/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(365, TimeUnit.DAYS)
                            .immutable()
                            .cachePublic());

            // Legacy support (old version)
            registry.addResourceHandler("/v1.0.0/**")
                    .addResourceLocations("classpath:/static/legacy/")
                    .setCacheControl(CacheControl
                            .maxAge(30, TimeUnit.DAYS));

            // Current version alias
            registry.addResourceHandler("/current/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl
                            .maxAge(1, TimeUnit.HOURS)
                            .mustRevalidate());
        }
    }
}
