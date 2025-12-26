package com.example.staticresources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.concurrent.TimeUnit;

/**
 * WebJars Pattern
 * 
 * Demonstrates using WebJars to manage client-side dependencies (JavaScript, CSS libraries)
 * as JAR files through Maven/Gradle. WebJars package web libraries into JAR format.
 * 
 * Popular WebJars: jQuery, Bootstrap, Angular, React, Vue, Font Awesome, etc.
 */
@SpringBootApplication
public class WebJarsPattern {

    public static void main(String[] args) {
        SpringApplication.run(WebJarsPattern.class, args);
    }

    /**
     * Basic WebJars configuration
     * WebJars are automatically mapped to /webjars/** by Spring Boot
     */
    @Configuration
    static class BasicWebJarsConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // WebJars are located in /META-INF/resources/webjars/
            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/")
                    .setCacheControl(CacheControl
                            .maxAge(365, TimeUnit.DAYS)
                            .immutable());
        }
    }

    /**
     * Maven dependencies for WebJars (pom.xml):
     * 
     * <dependencies>
     *     <!-- jQuery WebJar -->
     *     <dependency>
     *         <groupId>org.webjars</groupId>
     *         <artifactId>jquery</artifactId>
     *         <version>3.6.4</version>
     *     </dependency>
     *     
     *     <!-- Bootstrap WebJar -->
     *     <dependency>
     *         <groupId>org.webjars</groupId>
     *         <artifactId>bootstrap</artifactId>
     *         <version>5.3.0</version>
     *     </dependency>
     *     
     *     <!-- Font Awesome WebJar -->
     *     <dependency>
     *         <groupId>org.webjars</groupId>
     *         <artifactId>font-awesome</artifactId>
     *         <version>6.4.0</version>
     *     </dependency>
     *     
     *     <!-- WebJars Locator (optional, for version-agnostic paths) -->
     *     <dependency>
     *         <groupId>org.webjars</groupId>
     *         <artifactId>webjars-locator-core</artifactId>
     *         <version>0.52</version>
     *     </dependency>
     * </dependencies>
     */

    /**
     * Gradle dependencies for WebJars (build.gradle):
     * 
     * dependencies {
     *     implementation 'org.webjars:jquery:3.6.4'
     *     implementation 'org.webjars:bootstrap:5.3.0'
     *     implementation 'org.webjars:font-awesome:6.4.0'
     *     implementation 'org.webjars:webjars-locator-core:0.52'
     * }
     */

    /**
     * HTML usage with versioned paths:
     * 
     * <!DOCTYPE html>
     * <html>
     * <head>
     *     <!-- Bootstrap CSS -->
     *     <link rel="stylesheet" href="/webjars/bootstrap/5.3.0/css/bootstrap.min.css">
     *     
     *     <!-- Font Awesome -->
     *     <link rel="stylesheet" href="/webjars/font-awesome/6.4.0/css/all.min.css">
     * </head>
     * <body>
     *     <h1>Hello WebJars!</h1>
     *     
     *     <!-- jQuery -->
     *     <script src="/webjars/jquery/3.6.4/jquery.min.js"></script>
     *     
     *     <!-- Bootstrap JS -->
     *     <script src="/webjars/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
     * </body>
     * </html>
     */

    /**
     * Version-agnostic paths with WebJars Locator:
     * 
     * When webjars-locator-core is included, you can omit version numbers:
     * 
     * <!DOCTYPE html>
     * <html>
     * <head>
     *     <link rel="stylesheet" href="/webjars/bootstrap/css/bootstrap.min.css">
     *     <link rel="stylesheet" href="/webjars/font-awesome/css/all.min.css">
     * </head>
     * <body>
     *     <script src="/webjars/jquery/jquery.min.js"></script>
     *     <script src="/webjars/bootstrap/js/bootstrap.bundle.min.js"></script>
     * </body>
     * </html>
     * 
     * The locator automatically resolves to the correct version.
     */

    /**
     * Advanced WebJars configuration with versioning
     */
    @Configuration
    static class VersionedWebJarsConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Long cache for versioned WebJars
            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/")
                    .setCacheControl(CacheControl
                            .maxAge(365, TimeUnit.DAYS)
                            .cachePublic()
                            .immutable())
                    .resourceChain(true);
        }
    }

    /**
     * Custom WebJars path mapping
     */
    @Configuration
    static class CustomPathWebJarsConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Map WebJars to custom path
            registry.addResourceHandler("/libs/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/")
                    .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

            // Usage: /libs/jquery/3.6.4/jquery.min.js
        }
    }

    /**
     * NPM WebJars (for npm packages):
     * 
     * <dependency>
     *     <groupId>org.webjars.npm</groupId>
     *     <artifactId>vue</artifactId>
     *     <version>3.3.4</version>
     * </dependency>
     * 
     * <dependency>
     *     <groupId>org.webjars.npm</groupId>
     *     <artifactId>react</artifactId>
     *     <version>18.2.0</version>
     * </dependency>
     */

    /**
     * Bower WebJars (legacy):
     * 
     * <dependency>
     *     <groupId>org.webjars.bower</groupId>
     *     <artifactId>angular</artifactId>
     *     <version>1.8.3</version>
     * </dependency>
     */

    /**
     * WebJars benefits:
     * 
     * 1. Dependency Management:
     *    - Manage front-end libraries like back-end dependencies
     *    - Version control through Maven/Gradle
     *    - Transitive dependency resolution
     * 
     * 2. Offline Development:
     *    - No CDN required
     *    - Libraries packaged with application
     *    - Works without internet
     * 
     * 3. Cache Control:
     *    - Immutable versioned URLs
     *    - Aggressive caching possible
     *    - Automatic version updates
     * 
     * 4. Security:
     *    - No external CDN dependencies
     *    - Control over library versions
     *    - Avoid CDN compromises
     * 
     * 5. Build Integration:
     *    - Part of standard build process
     *    - Consistent versioning
     *    - Easy upgrades
     */

    /**
     * WebJars vs CDN comparison:
     */
    static class WebJarsVsCDN {
        /**
         * WebJars Advantages:
         * - Works offline
         * - No external dependencies
         * - Version control
         * - Build-time resolution
         * - Security (no CDN compromise)
         * 
         * CDN Advantages:
         * - Potentially faster (geographically distributed)
         * - Browser caching across sites
         * - Reduced server bandwidth
         * - No packaging overhead
         * 
         * Best Practice:
         * - Use WebJars for development
         * - Consider CDN for production (with fallback)
         * - Or use WebJars in production for full control
         */
    }

    /**
     * Popular WebJars:
     */
    static class PopularWebJars {
        /**
         * UI Frameworks:
         * - Bootstrap (org.webjars:bootstrap)
         * - Foundation (org.webjars:foundation)
         * - Bulma (org.webjars.npm:bulma)
         * 
         * JavaScript Libraries:
         * - jQuery (org.webjars:jquery)
         * - Lodash (org.webjars:lodash)
         * - Moment.js (org.webjars:momentjs)
         * 
         * JavaScript Frameworks:
         * - Angular (org.webjars.npm:angular)
         * - React (org.webjars.npm:react)
         * - Vue (org.webjars.npm:vue)
         * 
         * Icons:
         * - Font Awesome (org.webjars:font-awesome)
         * - Bootstrap Icons (org.webjars.npm:bootstrap-icons)
         * 
         * Charts:
         * - Chart.js (org.webjars:chartjs)
         * - D3.js (org.webjars:d3js)
         * 
         * Search at: https://www.webjars.org/
         */
    }
}
