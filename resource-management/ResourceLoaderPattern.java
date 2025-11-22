package com.spring.patterns.resourcemanagement;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Resource Loader Pattern
 * 
 * Demonstrates Spring's ResourceLoader interface for loading resources
 * from different locations (classpath, file system, URL).
 * 
 * Key Concepts:
 * - ResourceLoader interface provides a unified way to load resources
 * - Supports classpath:, file:, http:, and other protocols
 * - ApplicationContext implements ResourceLoader
 */

@Configuration
class ResourceLoaderConfig {
    
    @Bean
    public FileService fileService() {
        return new FileService();
    }
}

@Component
class FileService {
    
    private final ResourceLoader resourceLoader;
    
    // Constructor injection of ResourceLoader
    public FileService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    // Default constructor for manual instantiation
    public FileService() {
        this.resourceLoader = null;
    }
    
    public void setResourceLoader(ResourceLoader resourceLoader) {
        // Can be set manually if needed
    }
    
    /**
     * Load resource from classpath
     */
    public void loadClasspathResource(String location) {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + location);
            displayResourceInfo(resource, "Classpath Resource");
        } catch (Exception e) {
            System.err.println("Error loading classpath resource: " + e.getMessage());
        }
    }
    
    /**
     * Load resource from file system
     */
    public void loadFileSystemResource(String location) {
        try {
            Resource resource = resourceLoader.getResource("file:" + location);
            displayResourceInfo(resource, "File System Resource");
        } catch (Exception e) {
            System.err.println("Error loading file system resource: " + e.getMessage());
        }
    }
    
    /**
     * Load resource from URL
     */
    public void loadUrlResource(String url) {
        try {
            Resource resource = resourceLoader.getResource(url);
            displayResourceInfo(resource, "URL Resource");
        } catch (Exception e) {
            System.err.println("Error loading URL resource: " + e.getMessage());
        }
    }
    
    /**
     * Display resource information
     */
    private void displayResourceInfo(Resource resource, String type) throws IOException {
        System.out.println("\n=== " + type + " ===");
        System.out.println("Description: " + resource.getDescription());
        System.out.println("Exists: " + resource.exists());
        System.out.println("Is Readable: " + resource.isReadable());
        System.out.println("Is Open: " + resource.isOpen());
        
        if (resource.exists() && resource.isReadable()) {
            System.out.println("URI: " + resource.getURI());
            System.out.println("File Name: " + resource.getFilename());
            System.out.println("Content Length: " + resource.contentLength() + " bytes");
            
            // Read and display first few lines
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                System.out.println("\nFirst 3 lines of content:");
                for (int i = 0; i < 3; i++) {
                    String line = reader.readLine();
                    if (line != null) {
                        System.out.println(line);
                    }
                }
            }
        }
    }
}

/**
 * Service demonstrating programmatic resource loading
 */
class ResourceLoaderService {
    
    private final ResourceLoader resourceLoader;
    
    public ResourceLoaderService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    public String readResourceContent(String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + location);
        }
        
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    public boolean resourceExists(String location) {
        Resource resource = resourceLoader.getResource(location);
        return resource.exists();
    }
}

public class ResourceLoaderPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Resource Loader Pattern Demo ===\n");
        
        // Create Spring Application Context
        ApplicationContext context = new AnnotationConfigApplicationContext(ResourceLoaderConfig.class);
        
        // Get FileService bean
        FileService fileService = context.getBean(FileService.class);
        
        // Demo 1: Load from classpath
        System.out.println("1. Loading from Classpath:");
        fileService.loadClasspathResource("application.properties");
        
        // Demo 2: Load from file system
        System.out.println("\n2. Loading from File System:");
        fileService.loadFileSystemResource("C:/temp/sample.txt");
        
        // Demo 3: Load from URL
        System.out.println("\n3. Loading from URL:");
        fileService.loadUrlResource("https://www.example.com");
        
        // Demo 4: Using ApplicationContext as ResourceLoader directly
        System.out.println("\n4. Using ApplicationContext as ResourceLoader:");
        Resource contextResource = context.getResource("classpath:application.properties");
        System.out.println("Resource Description: " + contextResource.getDescription());
        System.out.println("Resource Exists: " + contextResource.exists());
        
        // Demo 5: ResourceLoaderService
        System.out.println("\n5. ResourceLoaderService Demo:");
        ResourceLoaderService service = new ResourceLoaderService(context);
        
        // Check if resources exist
        System.out.println("application.properties exists: " + 
                service.resourceExists("classpath:application.properties"));
        System.out.println("non-existent.txt exists: " + 
                service.resourceExists("classpath:non-existent.txt"));
        
        // Demo 6: Different resource protocols
        System.out.println("\n6. Different Resource Protocols:");
        demonstrateResourceProtocols(context);
        
        // Close context
        ((AnnotationConfigApplicationContext) context).close();
        
        System.out.println("\n=== Demo Completed ===");
    }
    
    private static void demonstrateResourceProtocols(ApplicationContext context) {
        String[] resourceLocations = {
            "classpath:application.properties",          // Classpath resource
            "file:C:/temp/sample.txt",                   // File system resource
            "classpath*:META-INF/*.properties",          // Pattern (requires PathMatchingResourcePatternResolver)
            "http://example.com/resource.txt",           // HTTP resource
            "ftp://example.com/resource.txt"             // FTP resource
        };
        
        for (String location : resourceLocations) {
            try {
                Resource resource = context.getResource(location);
                System.out.println("\nLocation: " + location);
                System.out.println("  Description: " + resource.getDescription());
                System.out.println("  Exists: " + resource.exists());
            } catch (Exception e) {
                System.out.println("\nLocation: " + location);
                System.out.println("  Error: " + e.getMessage());
            }
        }
    }
}

/*
 * Key Takeaways:
 * 
 * 1. ResourceLoader provides abstraction for loading resources
 * 2. ApplicationContext implements ResourceLoader interface
 * 3. Supports multiple protocols: classpath:, file:, http:, ftp:
 * 4. Can inject ResourceLoader into any Spring bean
 * 5. Resource interface provides unified access to different resource types
 * 
 * Benefits:
 * - Protocol-independent resource access
 * - Testability (can mock ResourceLoader)
 * - Consistent API across different resource types
 * - Integration with Spring's dependency injection
 * 
 * Use Cases:
 * - Loading configuration files
 * - Reading template files
 * - Accessing external resources
 * - Loading test data
 */
