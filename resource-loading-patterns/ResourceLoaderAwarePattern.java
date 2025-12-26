package com.example.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Resource Loader Aware Pattern
 * ==============================
 * 
 * Demonstrates ResourceLoaderAware interface for automatic injection
 * of ResourceLoader to load external resources.
 * 
 * Key Concepts:
 * ------------
 * 1. ResourceLoaderAware - Callback interface
 * 2. setResourceLoader() - Automatic injection
 * 3. Resource Loading - Unified resource access
 * 4. Location Prefixes - classpath:, file:, http:, etc.
 * 5. Resource Abstraction - Common interface for all resources
 * 
 * Resource Prefixes:
 * -----------------
 * - classpath: - Classpath resources
 * - file: - File system resources
 * - http: - HTTP resources
 * - ftp: - FTP resources
 * - No prefix - Default (typically classpath)
 * 
 * When to Use:
 * -----------
 * - Load configuration files
 * - Read template files
 * - Access external resources
 * - Location-independent resource access
 * - Multiple resource types
 * 
 * Advantages:
 * ----------
 * - Automatic ResourceLoader injection
 * - No @Autowired needed
 * - Unified resource access API
 * - Location prefix support
 * - Environment-independent
 * 
 * Best Practices:
 * --------------
 * - Check resource existence before reading
 * - Handle IOException appropriately
 * - Close streams properly
 * - Use try-with-resources
 * - Cache loaded resources if needed
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ResourceLoaderAwarePattern implements ResourceLoaderAware {
    
    private ResourceLoader resourceLoader;
    
    /**
     * Called automatically by Spring to inject ResourceLoader
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * Load resource from classpath
     */
    public String loadClasspathResource(String path) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + path);
        
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + path);
        }
        
        return readResource(resource);
    }
    
    /**
     * Load resource from file system
     */
    public String loadFileResource(String path) throws IOException {
        Resource resource = resourceLoader.getResource("file:" + path);
        
        if (!resource.exists()) {
            throw new IOException("File not found: " + path);
        }
        
        return readResource(resource);
    }
    
    /**
     * Load resource with any prefix
     */
    public String loadResource(String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + location);
        }
        
        return readResource(resource);
    }
    
    /**
     * Read resource content as string
     */
    private String readResource(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            
            return reader.lines()
                        .collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 2: Configuration File Loader
 */
@Component
class ConfigurationFileLoader implements ResourceLoaderAware {
    
    private ResourceLoader resourceLoader;
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * Load properties file from classpath
     */
    public java.util.Properties loadProperties(String filename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:config/" + filename);
        
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = resource.getInputStream()) {
            props.load(is);
        }
        
        return props;
    }
    
    /**
     * Load JSON configuration
     */
    public String loadJsonConfig(String filename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:config/" + filename);
        
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 3: Template Loader
 */
@Component
class TemplateLoader implements ResourceLoaderAware {
    
    private ResourceLoader resourceLoader;
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * Load email template
     */
    public String loadEmailTemplate(String templateName) throws IOException {
        Resource resource = resourceLoader.getResource(
            "classpath:templates/email/" + templateName + ".html");
        
        if (!resource.exists()) {
            throw new IOException("Template not found: " + templateName);
        }
        
        return readTemplate(resource);
    }
    
    /**
     * Load template with variable substitution
     */
    public String loadAndPopulateTemplate(String templateName, 
                                         java.util.Map<String, String> variables) 
                                         throws IOException {
        String template = loadEmailTemplate(templateName);
        
        // Simple variable replacement
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", 
                                       entry.getValue());
        }
        
        return template;
    }
    
    private String readTemplate(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 4: Resource Validator
 */
@Component
class ResourceValidator implements ResourceLoaderAware {
    
    private ResourceLoader resourceLoader;
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * Check if resource exists
     */
    public boolean resourceExists(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if resource is readable
     */
    public boolean isReadable(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            return resource.exists() && resource.isReadable();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get resource information
     */
    public ResourceInfo getResourceInfo(String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        
        return new ResourceInfo(
            resource.getFilename(),
            resource.exists(),
            resource.isReadable(),
            resource.contentLength(),
            resource.getURI().toString()
        );
    }
}

/**
 * Resource information holder
 */
class ResourceInfo {
    private final String filename;
    private final boolean exists;
    private final boolean readable;
    private final long size;
    private final String uri;
    
    public ResourceInfo(String filename, boolean exists, boolean readable, 
                       long size, String uri) {
        this.filename = filename;
        this.exists = exists;
        this.readable = readable;
        this.size = size;
        this.uri = uri;
    }
    
    @Override
    public String toString() {
        return String.format("ResourceInfo{filename='%s', exists=%s, readable=%s, size=%d, uri='%s'}",
                           filename, exists, readable, size, uri);
    }
}

/**
 * Example 5: Multi-Location Resource Loader
 */
@Component
class MultiLocationResourceLoader implements ResourceLoaderAware {
    
    private ResourceLoader resourceLoader;
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * Try loading from multiple locations
     */
    public Resource loadFromMultipleLocations(String filename, String... locations) {
        for (String location : locations) {
            String fullPath = location.endsWith("/") 
                ? location + filename 
                : location + "/" + filename;
                
            Resource resource = resourceLoader.getResource(fullPath);
            if (resource.exists()) {
                return resource;
            }
        }
        
        return null;
    }
    
    /**
     * Load with fallback
     */
    public String loadWithFallback(String primaryLocation, String fallbackLocation) 
                                   throws IOException {
        Resource resource = resourceLoader.getResource(primaryLocation);
        
        if (!resource.exists()) {
            resource = resourceLoader.getResource(fallbackLocation);
        }
        
        if (!resource.exists()) {
            throw new IOException("Resource not found in primary or fallback location");
        }
        
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Usage Examples
 */
class ResourceLoaderAwareUsageExamples {
    
    public static void main(String[] args) throws IOException {
        // Note: In real Spring app, these would be autowired
        
        // Example 1: Basic resource loading
        ResourceLoaderAwarePattern loader = new ResourceLoaderAwarePattern();
        // loader would have resourceLoader injected by Spring
        
        // Load from classpath
        // String content = loader.loadClasspathResource("data/sample.txt");
        
        // Load from file system
        // String fileContent = loader.loadFileResource("/path/to/file.txt");
        
        // Example 2: Configuration loading
        ConfigurationFileLoader configLoader = new ConfigurationFileLoader();
        // java.util.Properties props = configLoader.loadProperties("application.properties");
        // String jsonConfig = configLoader.loadJsonConfig("settings.json");
        
        // Example 3: Template loading
        TemplateLoader templateLoader = new TemplateLoader();
        // String template = templateLoader.loadEmailTemplate("welcome");
        
        // With variables
        java.util.Map<String, String> vars = new java.util.HashMap<>();
        vars.put("username", "John Doe");
        vars.put("action", "registered");
        // String populated = templateLoader.loadAndPopulateTemplate("welcome", vars);
        
        // Example 4: Resource validation
        ResourceValidator validator = new ResourceValidator();
        // boolean exists = validator.resourceExists("classpath:data/file.txt");
        // ResourceInfo info = validator.getResourceInfo("classpath:config/app.properties");
        
        System.out.println("ResourceLoaderAware pattern demonstration");
        System.out.println("Resources would be loaded from various locations:");
        System.out.println("- classpath:config/application.properties");
        System.out.println("- file:/etc/myapp/config.yml");
        System.out.println("- classpath:templates/email/welcome.html");
    }
}
