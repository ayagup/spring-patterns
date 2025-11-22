package com.spring.patterns.resourcemanagement;

import org.springframework.core.io.*;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resource Abstraction Pattern
 * 
 * Demonstrates Spring's Resource abstraction which provides a unified
 * interface for accessing resources from different locations.
 * 
 * Key Concepts:
 * - Resource interface abstracts low-level resource access
 * - Supports multiple resource types: classpath, file, URL, etc.
 * - Provides common methods for all resource types
 * - Simplifies resource handling across different sources
 */

/**
 * Service demonstrating Resource abstraction usage
 */
class ResourceService {
    
    /**
     * Display resource information
     */
    public void displayResourceInfo(Resource resource) throws IOException {
        System.out.println("\n=== Resource Information ===");
        System.out.println("Description: " + resource.getDescription());
        System.out.println("Exists: " + resource.exists());
        System.out.println("Is Readable: " + resource.isReadable());
        System.out.println("Is Open: " + resource.isOpen());
        System.out.println("Is File: " + resource.isFile());
        
        if (resource.exists()) {
            try {
                System.out.println("URI: " + resource.getURI());
                System.out.println("URL: " + resource.getURL());
                
                if (resource.isFile()) {
                    System.out.println("File: " + resource.getFile().getAbsolutePath());
                }
                
                System.out.println("Filename: " + resource.getFilename());
                
                if (resource.isReadable()) {
                    System.out.println("Content Length: " + resource.contentLength() + " bytes");
                    System.out.println("Last Modified: " + resource.lastModified());
                }
            } catch (Exception e) {
                System.out.println("Additional info unavailable: " + e.getMessage());
            }
        }
    }
    
    /**
     * Read resource content
     */
    public String readResourceContent(Resource resource) throws IOException {
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("Resource not readable: " + resource.getDescription());
        }
        
        try (InputStream is = resource.getInputStream()) {
            return new String(FileCopyUtils.copyToByteArray(is), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Read resource as lines
     */
    public void readResourceLines(Resource resource, int maxLines) throws IOException {
        System.out.println("\n=== Reading Resource Content ===");
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            int count = 0;
            
            while ((line = reader.readLine()) != null && count < maxLines) {
                System.out.println(line);
                count++;
            }
            
            if (count == maxLines) {
                System.out.println("... (truncated)");
            }
        }
    }
    
    /**
     * Copy resource to file
     */
    public void copyResourceToFile(Resource source, File destination) throws IOException {
        System.out.println("\n=== Copying Resource ===");
        System.out.println("From: " + source.getDescription());
        System.out.println("To: " + destination.getAbsolutePath());
        
        try (InputStream is = source.getInputStream();
             OutputStream os = new FileOutputStream(destination)) {
            
            FileCopyUtils.copy(is, os);
            System.out.println("Copy completed: " + destination.length() + " bytes");
        }
    }
}

/**
 * Demonstration of different Resource implementations
 */
class ResourceImplementationsDemo {
    
    /**
     * Demonstrate ClassPathResource
     */
    public static void demonstrateClassPathResource() {
        System.out.println("\n=== ClassPathResource Demo ===");
        
        // Create ClassPathResource
        Resource resource = new ClassPathResource("application.properties");
        
        System.out.println("Resource Type: ClassPathResource");
        System.out.println("Description: " + resource.getDescription());
        System.out.println("Exists: " + resource.exists());
        
        // Can also specify package path
        Resource resourceInPackage = new ClassPathResource("com/example/config.xml");
        System.out.println("\nResource in package: " + resourceInPackage.getDescription());
        
        // Relative to a class
        Resource relativeResource = new ClassPathResource("data.txt", ResourceService.class);
        System.out.println("Resource relative to class: " + relativeResource.getDescription());
    }
    
    /**
     * Demonstrate FileSystemResource
     */
    public static void demonstrateFileSystemResource() throws IOException {
        System.out.println("\n=== FileSystemResource Demo ===");
        
        // Create a temporary file
        Path tempFile = Files.createTempFile("spring-resource", ".txt");
        Files.writeString(tempFile, "Hello from FileSystemResource!");
        
        // Create FileSystemResource
        Resource resource = new FileSystemResource(tempFile.toFile());
        
        System.out.println("Resource Type: FileSystemResource");
        System.out.println("Description: " + resource.getDescription());
        System.out.println("Exists: " + resource.exists());
        System.out.println("Is File: " + resource.isFile());
        System.out.println("File Path: " + resource.getFile().getAbsolutePath());
        
        // Read content
        String content = Files.readString(tempFile);
        System.out.println("Content: " + content);
        
        // Cleanup
        Files.deleteIfExists(tempFile);
        System.out.println("Temporary file deleted");
    }
    
    /**
     * Demonstrate UrlResource
     */
    public static void demonstrateUrlResource() {
        System.out.println("\n=== UrlResource Demo ===");
        
        try {
            // HTTP URL
            Resource httpResource = new UrlResource("https://www.example.com");
            System.out.println("Resource Type: UrlResource (HTTP)");
            System.out.println("Description: " + httpResource.getDescription());
            System.out.println("URL: " + httpResource.getURL());
            
            // File URL
            File tempFile = File.createTempFile("url-resource", ".txt");
            tempFile.deleteOnExit();
            Files.writeString(tempFile.toPath(), "URL Resource content");
            
            Resource fileUrlResource = new UrlResource(tempFile.toURI().toURL());
            System.out.println("\nResource Type: UrlResource (File)");
            System.out.println("Description: " + fileUrlResource.getDescription());
            System.out.println("Exists: " + fileUrlResource.exists());
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Demonstrate ByteArrayResource
     */
    public static void demonstrateByteArrayResource() throws IOException {
        System.out.println("\n=== ByteArrayResource Demo ===");
        
        String content = "This is content stored in a byte array";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        
        Resource resource = new ByteArrayResource(bytes);
        
        System.out.println("Resource Type: ByteArrayResource");
        System.out.println("Description: " + resource.getDescription());
        System.out.println("Content Length: " + resource.contentLength() + " bytes");
        System.out.println("Is Readable: " + resource.isReadable());
        System.out.println("Is Open: " + resource.isOpen());
        
        // Read content
        try (InputStream is = resource.getInputStream()) {
            String readContent = new String(FileCopyUtils.copyToByteArray(is), StandardCharsets.UTF_8);
            System.out.println("Content: " + readContent);
        }
    }
    
    /**
     * Demonstrate InputStreamResource
     */
    public static void demonstrateInputStreamResource() throws IOException {
        System.out.println("\n=== InputStreamResource Demo ===");
        
        String content = "Content from InputStream";
        InputStream inputStream = new ByteArrayInputStream(
                content.getBytes(StandardCharsets.UTF_8));
        
        Resource resource = new InputStreamResource(inputStream);
        
        System.out.println("Resource Type: InputStreamResource");
        System.out.println("Description: " + resource.getDescription());
        System.out.println("Is Open: " + resource.isOpen());
        
        // Note: InputStreamResource can only be read once
        System.out.println("\nNote: InputStreamResource can only be read once!");
        
        try (InputStream is = resource.getInputStream()) {
            String readContent = new String(FileCopyUtils.copyToByteArray(is), StandardCharsets.UTF_8);
            System.out.println("Content: " + readContent);
        }
        
        // Second read will fail
        try {
            resource.getInputStream();
            System.out.println("Second read succeeded (unexpected)");
        } catch (Exception e) {
            System.out.println("Second read failed as expected: " + e.getMessage());
        }
    }
}

/**
 * Advanced Resource usage patterns
 */
class AdvancedResourcePatterns {
    
    /**
     * Create relative resource
     */
    public static void demonstrateRelativeResource() throws IOException {
        System.out.println("\n=== Relative Resource Demo ===");
        
        // Create temporary directory structure
        Path tempDir = Files.createTempDirectory("spring-resources");
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Path file1 = Files.createFile(tempDir.resolve("file1.txt"));
        Path file2 = Files.createFile(subDir.resolve("file2.txt"));
        
        try {
            Files.writeString(file1, "Content of file1");
            Files.writeString(file2, "Content of file2");
            
            // Create resource for file1
            Resource baseResource = new FileSystemResource(file1.toFile());
            System.out.println("Base resource: " + baseResource.getFilename());
            
            // Create relative resource
            Resource relativeResource = baseResource.createRelative("subdir/file2.txt");
            System.out.println("Relative resource: " + relativeResource.getFilename());
            System.out.println("Relative exists: " + relativeResource.exists());
            
            if (relativeResource.exists()) {
                String content = Files.readString(file2);
                System.out.println("Content: " + content);
            }
            
        } finally {
            // Cleanup
            Files.deleteIfExists(file2);
            Files.deleteIfExists(subDir);
            Files.deleteIfExists(file1);
            Files.deleteIfExists(tempDir);
        }
    }
    
    /**
     * Resource comparison
     */
    public static void demonstrateResourceComparison() throws IOException {
        System.out.println("\n=== Resource Comparison Demo ===");
        
        Path tempFile = Files.createTempFile("resource-compare", ".txt");
        
        try {
            Files.writeString(tempFile, "Test content");
            
            // Create two resources pointing to same file
            Resource resource1 = new FileSystemResource(tempFile.toFile());
            Resource resource2 = new FileSystemResource(tempFile.toFile());
            
            System.out.println("Resource 1 description: " + resource1.getDescription());
            System.out.println("Resource 2 description: " + resource2.getDescription());
            System.out.println("Are equal: " + resource1.equals(resource2));
            System.out.println("Same URI: " + resource1.getURI().equals(resource2.getURI()));
            
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
    
    /**
     * Resource writing
     */
    public static void demonstrateResourceWriting() throws IOException {
        System.out.println("\n=== Resource Writing Demo ===");
        
        Path tempFile = Files.createTempFile("writable-resource", ".txt");
        
        try {
            // Create WritableResource
            WritableResource resource = new FileSystemResource(tempFile.toFile());
            
            System.out.println("Is writable: " + resource.isWritable());
            
            // Write to resource
            String content = "Writing to resource via OutputStream";
            try (OutputStream os = resource.getOutputStream()) {
                os.write(content.getBytes(StandardCharsets.UTF_8));
            }
            
            System.out.println("Content written successfully");
            
            // Read back
            String readContent = Files.readString(tempFile);
            System.out.println("Read back: " + readContent);
            
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}

public class ResourceAbstractionPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Resource Abstraction Pattern Demo ===\n");
        
        try {
            ResourceService service = new ResourceService();
            
            // Demo 1: ClassPathResource
            System.out.println("=== Demo 1: ClassPathResource ===");
            ResourceImplementationsDemo.demonstrateClassPathResource();
            
            // Demo 2: FileSystemResource
            System.out.println("\n=== Demo 2: FileSystemResource ===");
            ResourceImplementationsDemo.demonstrateFileSystemResource();
            
            // Demo 3: UrlResource
            System.out.println("\n=== Demo 3: UrlResource ===");
            ResourceImplementationsDemo.demonstrateUrlResource();
            
            // Demo 4: ByteArrayResource
            System.out.println("\n=== Demo 4: ByteArrayResource ===");
            ResourceImplementationsDemo.demonstrateByteArrayResource();
            
            // Demo 5: InputStreamResource
            System.out.println("\n=== Demo 5: InputStreamResource ===");
            ResourceImplementationsDemo.demonstrateInputStreamResource();
            
            // Demo 6: Resource operations
            System.out.println("\n=== Demo 6: Resource Operations ===");
            demonstrateResourceOperations(service);
            
            // Demo 7: Relative resources
            AdvancedResourcePatterns.demonstrateRelativeResource();
            
            // Demo 8: Resource comparison
            AdvancedResourcePatterns.demonstrateResourceComparison();
            
            // Demo 9: Resource writing
            AdvancedResourcePatterns.demonstrateResourceWriting();
            
        } catch (Exception e) {
            System.err.println("Error in demo: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Demo Completed ===");
    }
    
    private static void demonstrateResourceOperations(ResourceService service) throws IOException {
        // Create a temporary file for demonstration
        Path tempFile = Files.createTempFile("resource-demo", ".txt");
        
        try {
            // Write some content
            String content = "Line 1: Hello from Spring Resource\n" +
                           "Line 2: Resource abstraction is powerful\n" +
                           "Line 3: It simplifies resource handling\n" +
                           "Line 4: Across different sources\n" +
                           "Line 5: And provides a unified API";
            
            Files.writeString(tempFile, content);
            
            // Create resource
            Resource resource = new FileSystemResource(tempFile.toFile());
            
            // Display info
            service.displayResourceInfo(resource);
            
            // Read content
            service.readResourceLines(resource, 3);
            
            // Copy to another file
            Path destFile = Files.createTempFile("resource-copy", ".txt");
            try {
                service.copyResourceToFile(resource, destFile.toFile());
                System.out.println("Destination file size: " + Files.size(destFile));
            } finally {
                Files.deleteIfExists(destFile);
            }
            
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}

/*
 * Key Takeaways:
 * 
 * 1. Resource interface provides unified access to different resource types
 * 2. Multiple implementations for different resource sources
 * 3. Common API across all resource types
 * 4. Supports reading, writing, and metadata operations
 * 5. Simplifies resource handling in applications
 * 
 * Resource Implementations:
 * - ClassPathResource: Resources from classpath
 * - FileSystemResource: File system resources
 * - UrlResource: URL-based resources (HTTP, FTP, file)
 * - ByteArrayResource: In-memory byte array
 * - InputStreamResource: From InputStream (single-use)
 * - ServletContextResource: Web application resources
 * - PathResource: Java NIO Path-based resources
 * 
 * Resource Interface Methods:
 * - exists(): Check if resource exists
 * - isReadable(): Check if resource is readable
 * - isOpen(): Check if InputStream is already open
 * - isFile(): Check if resource is a file
 * - getInputStream(): Get input stream
 * - getURL(): Get URL
 * - getURI(): Get URI
 * - getFile(): Get File object
 * - contentLength(): Get size in bytes
 * - lastModified(): Get last modified timestamp
 * - createRelative(): Create relative resource
 * - getFilename(): Get filename
 * - getDescription(): Get description
 * 
 * WritableResource Methods:
 * - isWritable(): Check if writable
 * - getOutputStream(): Get output stream
 * 
 * Benefits:
 * - Protocol-independent resource access
 * - Unified API for all resource types
 * - Easy resource manipulation
 * - Integration with Spring's ResourceLoader
 * - Testability (can mock Resource)
 * - Support for various sources
 * 
 * Use Cases:
 * - Loading configuration files
 * - Reading template files
 * - Accessing static resources
 * - File upload/download handling
 * - Resource streaming
 * - Test data loading
 * - Dynamic resource access
 * 
 * Best Practices:
 * - Use try-with-resources for InputStreams
 * - Check exists() before reading
 * - Handle IOException appropriately
 * - Prefer ClassPathResource for bundled resources
 * - Use FileSystemResource for external files
 * - Be aware of InputStreamResource single-use limitation
 * - Use appropriate resource type for the source
 * - Consider WritableResource for output operations
 * 
 * Integration Points:
 * - Works with ResourceLoader
 * - Used by ApplicationContext
 * - Integration with @Value annotation
 * - Support in Spring Boot
 * - Template engine integration
 * - Static resource serving
 */
