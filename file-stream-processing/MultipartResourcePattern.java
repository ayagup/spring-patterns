package com.spring.patterns.filestream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

/**
 * Multipart File Handling and Resource Handling Patterns
 * 
 * Demonstrates:
 * 1. Multipart File Handling:
 *    - Multipart configuration
 *    - File size limits
 *    - Temporary file handling
 *    - Multiple file uploads
 *    - Multipart validation
 * 
 * 2. Resource Handling:
 *    - Spring Resource abstraction
 *    - ClassPathResource
 *    - FileSystemResource
 *    - UrlResource
 *    - ByteArrayResource
 *    - InputStreamResource
 *    - Resource loading patterns
 *    - Resource path resolution
 * 
 * Use Cases:
 * - Configuration file loading
 * - Template file access
 * - Static resource serving
 * - File upload handling
 * - Classpath resource access
 * - External file access
 * 
 * Dependencies:
 * - spring-boot-starter-web
 */

/**
 * Multipart Configuration
 */
@Configuration
class MultipartFileConfiguration {
    
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Maximum file size (10MB)
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        
        // Maximum request size (50MB)
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        
        // File size threshold for writing to disk (2MB)
        factory.setFileSizeThreshold(DataSize.ofMegabytes(2));
        
        return factory.createMultipartConfig();
    }
}

/**
 * Multipart Configuration Properties
 */
@Configuration
@ConfigurationProperties(prefix = "spring.servlet.multipart")
class MultipartProperties {
    private boolean enabled = true;
    private String location = System.getProperty("java.io.tmpdir");
    private DataSize maxFileSize = DataSize.ofMegabytes(10);
    private DataSize maxRequestSize = DataSize.ofMegabytes(50);
    private DataSize fileSizeThreshold = DataSize.ofMegabytes(1);
    private Duration fileCleanupDelay = Duration.ofMinutes(30);
    
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public DataSize getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(DataSize maxFileSize) { this.maxFileSize = maxFileSize; }
    public DataSize getMaxRequestSize() { return maxRequestSize; }
    public void setMaxRequestSize(DataSize maxRequestSize) { 
        this.maxRequestSize = maxRequestSize; 
    }
    public DataSize getFileSizeThreshold() { return fileSizeThreshold; }
    public void setFileSizeThreshold(DataSize fileSizeThreshold) { 
        this.fileSizeThreshold = fileSizeThreshold; 
    }
    public Duration getFileCleanupDelay() { return fileCleanupDelay; }
}

/**
 * Multipart File Handler
 */
@Service
class MultipartFileHandler {
    
    /**
     * Get file information
     */
    public FileInfo getFileInfo(MultipartFile file) {
        return new FileInfo(
                file.getOriginalFilename(),
                file.getName(),
                file.getContentType(),
                file.getSize(),
                file.isEmpty()
        );
    }
    
    /**
     * Read file content as string
     */
    public String readAsString(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }
    
    /**
     * Read file content as lines
     */
    public List<String> readAsLines(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            return reader.lines().toList();
        }
    }
    
    /**
     * Transfer file to destination
     */
    public void transferTo(MultipartFile file, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        file.transferTo(destination.toFile());
    }
    
    /**
     * Process multiple files
     */
    public List<FileInfo> processMultipleFiles(MultipartFile[] files) {
        return Arrays.stream(files)
                .filter(file -> !file.isEmpty())
                .map(this::getFileInfo)
                .toList();
    }
    
    /**
     * Validate multipart file
     */
    public ValidationResult validate(MultipartFile file, 
                                    Set<String> allowedTypes,
                                    long maxSize) {
        
        List<String> errors = new ArrayList<>();
        
        if (file.isEmpty()) {
            errors.add("File is empty");
        }
        
        if (file.getSize() > maxSize) {
            errors.add("File size exceeds limit: " + maxSize);
        }
        
        String contentType = file.getContentType();
        if (contentType != null && !allowedTypes.contains(contentType)) {
            errors.add("Content type not allowed: " + contentType);
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
}

record FileInfo(
        String originalFilename,
        String fieldName,
        String contentType,
        long size,
        boolean empty
) {}

record ValidationResult(boolean valid, List<String> errors) {}

/**
 * Resource Loader Service
 */
@Service
class ResourceLoaderService implements ResourceLoader {
    
    private final ResourcePatternResolver resourcePatternResolver;
    
    public ResourceLoaderService() {
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
    }
    
    @Override
    public Resource getResource(String location) {
        return resourcePatternResolver.getResource(location);
    }
    
    @Override
    public ClassLoader getClassLoader() {
        return resourcePatternResolver.getClassLoader();
    }
    
    /**
     * Load classpath resource
     */
    public Resource loadClasspathResource(String path) {
        return new ClassPathResource(path);
    }
    
    /**
     * Load filesystem resource
     */
    public Resource loadFileSystemResource(String path) {
        return new FileSystemResource(path);
    }
    
    /**
     * Load URL resource
     */
    public Resource loadUrlResource(String url) throws IOException {
        return new UrlResource(url);
    }
    
    /**
     * Load byte array resource
     */
    public Resource loadByteArrayResource(byte[] data, String description) {
        return new ByteArrayResource(data) {
            @Override
            public String getDescription() {
                return description;
            }
        };
    }
    
    /**
     * Load input stream resource
     */
    public Resource loadInputStreamResource(InputStream inputStream, 
                                           String description) {
        return new InputStreamResource(inputStream) {
            @Override
            public String getDescription() {
                return description;
            }
        };
    }
    
    /**
     * Load multiple resources by pattern
     */
    public Resource[] loadResourcesByPattern(String pattern) throws IOException {
        return resourcePatternResolver.getResources(pattern);
    }
}

/**
 * Resource Handler Service
 */
@Service
class ResourceHandlerService {
    
    /**
     * Read resource content as string
     */
    public String readResourceAsString(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Read resource as lines
     */
    public List<String> readResourceAsLines(Resource resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            return reader.lines().toList();
        }
    }
    
    /**
     * Copy resource to file
     */
    public void copyResourceToFile(Resource resource, File destination) throws IOException {
        FileCopyUtils.copy(resource.getInputStream(), 
                new FileOutputStream(destination));
    }
    
    /**
     * Copy resource to output stream
     */
    public void copyResourceToStream(Resource resource, OutputStream output) 
            throws IOException {
        FileCopyUtils.copy(resource.getInputStream(), output);
    }
    
    /**
     * Get resource metadata
     */
    public ResourceMetadata getMetadata(Resource resource) throws IOException {
        return new ResourceMetadata(
                resource.getFilename(),
                resource.getDescription(),
                resource.exists(),
                resource.isReadable(),
                resource.isOpen(),
                resource.contentLength(),
                resource.lastModified()
        );
    }
    
    /**
     * Check if resource exists
     */
    public boolean exists(Resource resource) {
        return resource.exists();
    }
    
    /**
     * Get resource URL
     */
    public URL getURL(Resource resource) throws IOException {
        return resource.getURL();
    }
    
    /**
     * Get resource file (if possible)
     */
    public File getFile(Resource resource) throws IOException {
        return resource.getFile();
    }
}

record ResourceMetadata(
        String filename,
        String description,
        boolean exists,
        boolean readable,
        boolean open,
        long contentLength,
        long lastModified
) {}

/**
 * Resource Path Resolver
 */
@Service
class ResourcePathResolver {
    
    /**
     * Resolve classpath location
     */
    public String resolveClasspathLocation(String path) {
        return ResourceUtils.CLASSPATH_URL_PREFIX + path;
    }
    
    /**
     * Resolve file location
     */
    public String resolveFileLocation(String path) {
        return ResourceUtils.FILE_URL_PREFIX + path;
    }
    
    /**
     * Check if location is URL
     */
    public boolean isUrl(String location) {
        return ResourceUtils.isUrl(location);
    }
    
    /**
     * Get file from resource location
     */
    public File getFile(String resourceLocation) throws FileNotFoundException {
        return ResourceUtils.getFile(resourceLocation);
    }
    
    /**
     * Get URL from resource location
     */
    public URL getURL(String resourceLocation) throws FileNotFoundException {
        return ResourceUtils.getURL(resourceLocation);
    }
}

/**
 * Template Resource Loader
 */
@Service
class TemplateResourceLoader {
    
    private final ResourceLoaderService resourceLoader;
    
    public TemplateResourceLoader(ResourceLoaderService resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * Load template from classpath
     */
    public String loadTemplate(String templateName) throws IOException {
        Resource resource = resourceLoader.loadClasspathResource(
                "templates/" + templateName
        );
        
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Load and process template with parameters
     */
    public String processTemplate(String templateName, 
                                  Map<String, String> parameters) throws IOException {
        
        String template = loadTemplate(templateName);
        
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            template = template.replace("${" + entry.getKey() + "}", 
                    entry.getValue());
        }
        
        return template;
    }
}

/**
 * Configuration File Loader
 */
@Service
class ConfigurationFileLoader {
    
    private final ResourceLoaderService resourceLoader;
    
    public ConfigurationFileLoader(ResourceLoaderService resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * Load properties file
     */
    public Properties loadProperties(String propertiesFile) throws IOException {
        Resource resource = resourceLoader.getResource(propertiesFile);
        Properties properties = new Properties();
        
        try (InputStream is = resource.getInputStream()) {
            properties.load(is);
        }
        
        return properties;
    }
    
    /**
     * Load configuration from multiple locations
     */
    public Properties loadFromMultipleLocations(String... locations) 
            throws IOException {
        
        Properties combinedProperties = new Properties();
        
        for (String location : locations) {
            Resource resource = resourceLoader.getResource(location);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    Properties props = new Properties();
                    props.load(is);
                    combinedProperties.putAll(props);
                }
            }
        }
        
        return combinedProperties;
    }
}

/**
 * Resource Cache Service
 */
@Service
class ResourceCacheService {
    
    private final Map<String, CachedResource> cache = new HashMap<>();
    
    /**
     * Get or load resource
     */
    public byte[] getOrLoad(String location, ResourceLoader loader) throws IOException {
        CachedResource cached = cache.get(location);
        
        if (cached != null && !cached.isExpired()) {
            return cached.data();
        }
        
        Resource resource = loader.getResource(location);
        byte[] data = resource.getInputStream().readAllBytes();
        
        cache.put(location, new CachedResource(
                data,
                System.currentTimeMillis(),
                Duration.ofMinutes(10).toMillis()
        ));
        
        return data;
    }
    
    /**
     * Clear cache
     */
    public void clearCache() {
        cache.clear();
    }
    
    /**
     * Clear expired entries
     */
    public void clearExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    record CachedResource(byte[] data, long timestamp, long ttl) {
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }
    }
}

/**
 * Multipart and Resource Pattern - Main Demonstration
 */
public class MultipartResourcePattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Multipart and Resource Handling Patterns Demo ===\n");
        
        // 1. Multipart Configuration
        demonstrateMultipartConfig();
        
        // 2. Resource Loading
        demonstrateResourceLoading();
        
        // 3. Classpath Resources
        demonstrateClasspathResources();
        
        // 4. FileSystem Resources
        demonstrateFileSystemResources();
        
        // 5. URL Resources
        demonstrateUrlResources();
        
        // 6. Resource Patterns
        demonstrateResourcePatterns();
        
        // 7. Template Loading
        demonstrateTemplateLoading();
    }
    
    private static void demonstrateMultipartConfig() {
        System.out.println("1. Multipart Configuration:");
        
        MultipartProperties props = new MultipartProperties();
        System.out.println("Enabled: " + props.isEnabled());
        System.out.println("Max File Size: " + props.getMaxFileSize());
        System.out.println("Max Request Size: " + props.getMaxRequestSize());
        System.out.println("File Size Threshold: " + props.getFileSizeThreshold());
        System.out.println("Temp Location: " + props.getLocation());
        
        System.out.println();
    }
    
    private static void demonstrateResourceLoading() throws IOException {
        System.out.println("2. Resource Loading:");
        
        ResourceLoaderService loader = new ResourceLoaderService();
        
        // Load different resource types
        System.out.println("Loading classpath resource:");
        Resource classpathRes = loader.loadClasspathResource("application.properties");
        System.out.println("  Exists: " + classpathRes.exists());
        
        System.out.println("\nLoading filesystem resource:");
        Resource fileRes = loader.loadFileSystemResource(
                System.getProperty("user.home")
        );
        System.out.println("  Exists: " + fileRes.exists());
        
        System.out.println();
    }
    
    private static void demonstrateClasspathResources() throws IOException {
        System.out.println("3. Classpath Resources:");
        
        ClassPathResource resource = new ClassPathResource("application.properties");
        
        System.out.println("Path: " + resource.getPath());
        System.out.println("Description: " + resource.getDescription());
        System.out.println("Exists: " + resource.exists());
        
        if (resource.exists()) {
            System.out.println("Content type can be determined");
            System.out.println("Can be read as InputStream");
        }
        
        System.out.println();
    }
    
    private static void demonstrateFileSystemResources() throws IOException {
        System.out.println("4. FileSystem Resources:");
        
        Path tempFile = Files.createTempFile("resource-test", ".txt");
        Files.write(tempFile, "Test content".getBytes());
        
        FileSystemResource resource = new FileSystemResource(tempFile.toFile());
        
        System.out.println("Path: " + resource.getPath());
        System.out.println("Filename: " + resource.getFilename());
        System.out.println("Exists: " + resource.exists());
        System.out.println("Readable: " + resource.isReadable());
        System.out.println("Writable: " + resource.isWritable());
        System.out.println("Size: " + resource.contentLength() + " bytes");
        
        Files.deleteIfExists(tempFile);
        
        System.out.println();
    }
    
    private static void demonstrateUrlResources() {
        System.out.println("5. URL Resources:");
        
        System.out.println("Can load resources from:");
        System.out.println("- HTTP/HTTPS URLs");
        System.out.println("- FTP URLs");
        System.out.println("- File URLs");
        System.out.println("- JAR URLs");
        
        System.out.println("\nExample URL patterns:");
        System.out.println("- http://example.com/resource.txt");
        System.out.println("- file:///path/to/file.txt");
        System.out.println("- jar:file:/path/to/jar!/resource.txt");
        
        System.out.println();
    }
    
    private static void demonstrateResourcePatterns() throws IOException {
        System.out.println("6. Resource Patterns:");
        
        ResourceLoaderService loader = new ResourceLoaderService();
        
        System.out.println("Pattern examples:");
        System.out.println("- classpath:config/*.properties");
        System.out.println("- classpath*:META-INF/*.xml");
        System.out.println("- file:///data/**/*.txt");
        
        System.out.println("\nAnt-style wildcards:");
        System.out.println("- ? matches one character");
        System.out.println("- * matches zero or more characters");
        System.out.println("- ** matches zero or more directories");
        
        System.out.println();
    }
    
    private static void demonstrateTemplateLoading() {
        System.out.println("7. Template Loading:");
        
        System.out.println("Common use cases:");
        System.out.println("- Email templates");
        System.out.println("- Report templates");
        System.out.println("- Configuration templates");
        System.out.println("- HTML templates");
        
        System.out.println("\nTemplate processing:");
        System.out.println("- Load from classpath");
        System.out.println("- Replace placeholders");
        System.out.println("- Cache compiled templates");
        System.out.println("- Support multiple formats");
        
        System.out.println("\n=== Demo Complete ===");
    }
}
