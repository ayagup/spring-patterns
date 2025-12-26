package com.example.staticresources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformer;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Resource Transformer Pattern
 * 
 * Demonstrates resource transformers that modify resource content.
 * Transformers can minify, compress, or otherwise modify resources.
 */
@SpringBootApplication
public class ResourceTransformerPattern {

    public static void main(String[] args) {
        SpringApplication.run(ResourceTransformerPattern.class, args);
    }

    /**
     * Custom CSS Link Transformer (updates CSS @import and url() references)
     */
    static class CustomCssLinkTransformer implements ResourceTransformer {

        @Override
        public Resource transform(HttpServletRequest request, Resource resource,
                                ResourceTransformerChain transformerChain) throws IOException {
            
            Resource transformed = transformerChain.transform(request, resource);
            
            if (resource.getFilename() != null && resource.getFilename().endsWith(".css")) {
                // Read CSS content
                byte[] bytes = org.springframework.util.FileCopyUtils.copyToByteArray(transformed.getInputStream());
                String content = new String(bytes, StandardCharsets.UTF_8);
                
                // Transform CSS links (add version, CDN prefix, etc.)
                String transformedContent = content.replaceAll("url\\(([^)]+)\\)", "url(/cdn/$1)");
                
                return new TransformedResource(transformed, transformedContent.getBytes(StandardCharsets.UTF_8));
            }
            
            return transformed;
        }
    }

    /**
     * Minification Transformer (simulated)
     */
    static class MinificationTransformer implements ResourceTransformer {

        @Override
        public Resource transform(HttpServletRequest request, Resource resource,
                                ResourceTransformerChain transformerChain) throws IOException {
            
            Resource transformed = transformerChain.transform(request, resource);
            
            String filename = resource.getFilename();
            if (filename != null && (filename.endsWith(".js") || filename.endsWith(".css"))) {
                byte[] bytes = org.springframework.util.FileCopyUtils.copyToByteArray(transformed.getInputStream());
                String content = new String(bytes, StandardCharsets.UTF_8);
                
                // Simulated minification (remove extra whitespace)
                String minified = content
                        .replaceAll("\\s+", " ")
                        .replaceAll("\\s*([{}:;,])\\s*", "$1");
                
                return new TransformedResource(transformed, minified.getBytes(StandardCharsets.UTF_8));
            }
            
            return transformed;
        }
    }

    /**
     * Cache Manifest Transformer
     */
    static class CacheManifestTransformer implements ResourceTransformer {

        @Override
        public Resource transform(HttpServletRequest request, Resource resource,
                                ResourceTransformerChain transformerChain) throws IOException {
            
            Resource transformed = transformerChain.transform(request, resource);
            
            if (resource.getFilename() != null && resource.getFilename().endsWith(".appcache")) {
                byte[] bytes = org.springframework.util.FileCopyUtils.copyToByteArray(transformed.getInputStream());
                String content = new String(bytes, StandardCharsets.UTF_8);
                
                // Add timestamp to force cache update
                String updated = content.replace("# Version", "# Version: " + System.currentTimeMillis());
                
                return new TransformedResource(transformed, updated.getBytes(StandardCharsets.UTF_8));
            }
            
            return transformed;
        }
    }

    /**
     * Version Injection Transformer
     */
    static class VersionInjectionTransformer implements ResourceTransformer {

        private final String version;

        public VersionInjectionTransformer(String version) {
            this.version = version;
        }

        @Override
        public Resource transform(HttpServletRequest request, Resource resource,
                                ResourceTransformerChain transformerChain) throws IOException {
            
            Resource transformed = transformerChain.transform(request, resource);
            
            if (resource.getFilename() != null && resource.getFilename().endsWith(".html")) {
                byte[] bytes = org.springframework.util.FileCopyUtils.copyToByteArray(transformed.getInputStream());
                String content = new String(bytes, StandardCharsets.UTF_8);
                
                // Inject version into HTML
                String withVersion = content.replace("{{VERSION}}", version);
                
                return new TransformedResource(transformed, withVersion.getBytes(StandardCharsets.UTF_8));
            }
            
            return transformed;
        }
    }

    /**
     * Configuration with transformers
     */
    @Configuration
    static class TransformerConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/transformed/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true)
                    .addTransformer(new MinificationTransformer());
        }
    }

    /**
     * CSS-specific transformer configuration
     */
    @Configuration
    static class CssTransformerConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/css/**")
                    .addResourceLocations("classpath:/static/css/")
                    .resourceChain(true)
                    .addTransformer(new CustomCssLinkTransformer());
        }
    }

    /**
     * Multiple transformers configuration
     */
    @Configuration
    static class MultipleTransformersConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations("classpath:/static/assets/")
                    .resourceChain(true)
                    .addTransformer(new VersionInjectionTransformer("1.0.0"))
                    .addTransformer(new MinificationTransformer())
                    .addTransformer(new CustomCssLinkTransformer());
        }
    }

    /**
     * Production transformer configuration
     */
    @Configuration
    static class ProductionTransformerConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // JavaScript files
            registry.addResourceHandler("/js/**")
                    .addResourceLocations("classpath:/static/js/")
                    .setCachePeriod(31536000)
                    .resourceChain(true)
                    .addTransformer(new MinificationTransformer());

            // CSS files
            registry.addResourceHandler("/styles/**")
                    .addResourceLocations("classpath:/static/css/")
                    .setCachePeriod(31536000)
                    .resourceChain(true)
                    .addTransformer(new CustomCssLinkTransformer())
                    .addTransformer(new MinificationTransformer());

            // HTML files
            registry.addResourceHandler("/pages/**")
                    .addResourceLocations("classpath:/static/pages/")
                    .resourceChain(true)
                    .addTransformer(new VersionInjectionTransformer("2.0.0"));
        }
    }
}
