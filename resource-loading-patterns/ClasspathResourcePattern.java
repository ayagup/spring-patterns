package com.example.resource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Classpath Resource Pattern
 * ===========================
 * 
 * Demonstrates ClassPathResource for loading resources from classpath
 * (JAR files, class directories, etc.).
 * 
 * Key Concepts:
 * ------------
 * 1. ClassPathResource - Classpath-based resource access
 * 2. Relative Paths - Package-relative or absolute paths
 * 3. Class-Based Loading - Load relative to a class
 * 4. JAR Support - Works with resources in JAR files
 * 5. Multiple Classpaths - Searches all classpath entries
 * 
 * Path Formats:
 * ------------
 * - Absolute: "/config/app.properties" (from classpath root)
 * - Relative: "data/file.txt" (from current package)
 * - With Class: Load relative to specific class location
 * 
 * When to Use:
 * -----------
 * - Load bundled resources
 * - Read configuration files
 * - Access template files
 * - Read static data
 * - Resource in JAR deployment
 * 
 * Advantages:
 * ----------
 * - Works in JAR and IDE
 * - Package-relative loading
 * - ClassLoader integration
 * - Platform-independent paths
 * 
 * Best Practices:
 * --------------
 * - Use forward slashes (/)
 * - Absolute paths start with /
 * - Check existence before reading
 * - Close streams properly
 * - Use try-with-resources
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ClasspathResourcePattern {
    
    /**
     * Load resource from classpath root
     */
    public String loadFromRoot(String path) throws IOException {
        // Path starting with / is from classpath root
        ClassPathResource resource = new ClassPathResource("/" + path);
        
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + path);
        }
        
        return readResource(resource);
    }
    
    /**
     * Load resource from package
     */
    public String loadFromPackage(String packagePath, String filename) throws IOException {
        // Convert package to path
        String path = packagePath.replace('.', '/') + "/" + filename;
        ClassPathResource resource = new ClassPathResource(path);
        
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + path);
        }
        
        return readResource(resource);
    }
    
    /**
     * Load resource relative to this class
     */
    public String loadRelativeToClass(Class<?> clazz, String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource(filename, clazz);
        
        if (!resource.exists()) {
            throw new IOException("Resource not found relative to " + clazz.getName());
        }
        
        return readResource(resource);
    }
    
    /**
     * Read resource content
     */
    private String readResource(ClassPathResource resource) throws IOException {
        try (InputStream is = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 2: Properties Loader
 */
@Component
class PropertiesLoader {
    
    /**
     * Load properties from classpath
     */
    public Properties loadProperties(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource(filename);
        
        Properties props = new Properties();
        try (InputStream is = resource.getInputStream()) {
            props.load(is);
        }
        
        return props;
    }
    
    /**
     * Load properties from specific directory
     */
    public Properties loadPropertiesFromDir(String directory, String filename) 
                                           throws IOException {
        String path = directory + "/" + filename;
        ClassPathResource resource = new ClassPathResource(path);
        
        Properties props = new Properties();
        try (InputStream is = resource.getInputStream()) {
            props.load(is);
        }
        
        return props;
    }
    
    /**
     * Load multiple properties files
     */
    public Properties loadMultipleProperties(String... filenames) throws IOException {
        Properties combined = new Properties();
        
        for (String filename : filenames) {
            ClassPathResource resource = new ClassPathResource(filename);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    Properties props = new Properties();
                    props.load(is);
                    combined.putAll(props);
                }
            }
        }
        
        return combined;
    }
}

/**
 * Example 3: Data File Loader
 */
@Component
class DataFileLoader {
    
    /**
     * Load JSON data file
     */
    public String loadJsonData(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource("data/" + filename);
        return readAsString(resource);
    }
    
    /**
     * Load XML data file
     */
    public String loadXmlData(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource("data/" + filename);
        return readAsString(resource);
    }
    
    /**
     * Load CSV data file
     */
    public java.util.List<String[]> loadCsvData(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource("data/" + filename);
        
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(line.split(","));
            }
        }
        
        return rows;
    }
    
    private String readAsString(ClassPathResource resource) throws IOException {
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 4: Template Loader
 */
@Component
class TemplateResourceLoader {
    
    private static final String TEMPLATE_DIR = "templates/";
    
    /**
     * Load HTML template
     */
    public String loadHtmlTemplate(String name) throws IOException {
        String path = TEMPLATE_DIR + "html/" + name + ".html";
        ClassPathResource resource = new ClassPathResource(path);
        return readTemplate(resource);
    }
    
    /**
     * Load email template
     */
    public String loadEmailTemplate(String name) throws IOException {
        String path = TEMPLATE_DIR + "email/" + name + ".html";
        ClassPathResource resource = new ClassPathResource(path);
        return readTemplate(resource);
    }
    
    /**
     * Load SQL template
     */
    public String loadSqlTemplate(String name) throws IOException {
        String path = TEMPLATE_DIR + "sql/" + name + ".sql";
        ClassPathResource resource = new ClassPathResource(path);
        return readTemplate(resource);
    }
    
    private String readTemplate(ClassPathResource resource) throws IOException {
        if (!resource.exists()) {
            throw new IOException("Template not found: " + resource.getPath());
        }
        
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 5: Binary Resource Loader
 */
@Component
class BinaryResourceLoader {
    
    /**
     * Load image resource
     */
    public byte[] loadImage(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource("static/images/" + filename);
        return loadBinary(resource);
    }
    
    /**
     * Load font resource
     */
    public byte[] loadFont(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource("static/fonts/" + filename);
        return loadBinary(resource);
    }
    
    /**
     * Load any binary resource
     */
    public byte[] loadBinary(ClassPathResource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            return is.readAllBytes();
        }
    }
}

/**
 * Example 6: Resource Validator
 */
@Component
class ClasspathResourceValidator {
    
    /**
     * Check if resource exists
     */
    public boolean exists(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        return resource.exists();
    }
    
    /**
     * Check if resource is readable
     */
    public boolean isReadable(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        return resource.exists() && resource.isReadable();
    }
    
    /**
     * Get resource metadata
     */
    public ResourceMetadata getMetadata(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        
        return new ResourceMetadata(
            resource.getPath(),
            resource.exists(),
            resource.isReadable(),
            resource.exists() ? resource.contentLength() : 0,
            resource.getFilename()
        );
    }
}

/**
 * Resource metadata holder
 */
class ResourceMetadata {
    private final String path;
    private final boolean exists;
    private final boolean readable;
    private final long size;
    private final String filename;
    
    public ResourceMetadata(String path, boolean exists, boolean readable, 
                          long size, String filename) {
        this.path = path;
        this.exists = exists;
        this.readable = readable;
        this.size = size;
        this.filename = filename;
    }
    
    @Override
    public String toString() {
        return String.format("ResourceMetadata{path='%s', exists=%s, readable=%s, size=%d, filename='%s'}",
                           path, exists, readable, size, filename);
    }
}

/**
 * Example 7: Configuration Loader with Fallback
 */
@Component
class ConfigurationWithFallback {
    
    /**
     * Load with fallback to default
     */
    public Properties loadWithFallback(String primary, String fallback) 
                                      throws IOException {
        ClassPathResource resource = new ClassPathResource(primary);
        
        if (!resource.exists()) {
            resource = new ClassPathResource(fallback);
        }
        
        if (!resource.exists()) {
            throw new IOException("Neither primary nor fallback config found");
        }
        
        Properties props = new Properties();
        try (InputStream is = resource.getInputStream()) {
            props.load(is);
        }
        
        return props;
    }
    
    /**
     * Load environment-specific configuration
     */
    public Properties loadEnvironmentConfig(String environment) throws IOException {
        String primary = "config/application-" + environment + ".properties";
        String fallback = "config/application.properties";
        
        return loadWithFallback(primary, fallback);
    }
}

/**
 * Usage Examples
 */
class ClasspathResourceUsageExamples {
    
    public static void main(String[] args) throws IOException {
        ClasspathResourcePattern loader = new ClasspathResourcePattern();
        
        // Example 1: Load from root
        // String content = loader.loadFromRoot("config/application.properties");
        
        // Example 2: Load from package
        // String data = loader.loadFromPackage("com.example.data", "sample.json");
        
        // Example 3: Load relative to class
        // String relative = loader.loadRelativeToClass(
        //     ClasspathResourcePattern.class, "config.xml");
        
        // Properties loading
        PropertiesLoader propsLoader = new PropertiesLoader();
        // Properties props = propsLoader.loadProperties("application.properties");
        
        // Data file loading
        DataFileLoader dataLoader = new DataFileLoader();
        // String json = dataLoader.loadJsonData("users.json");
        // List<String[]> csv = dataLoader.loadCsvData("data.csv");
        
        // Template loading
        TemplateResourceLoader templateLoader = new TemplateResourceLoader();
        // String template = templateLoader.loadEmailTemplate("welcome");
        
        // Binary loading
        BinaryResourceLoader binaryLoader = new BinaryResourceLoader();
        // byte[] image = binaryLoader.loadImage("logo.png");
        
        System.out.println("Classpath Resource Pattern Demonstration");
        System.out.println("Example paths:");
        System.out.println("- /config/application.properties (root)");
        System.out.println("- data/users.json (relative)");
        System.out.println("- templates/email/welcome.html");
        System.out.println("- static/images/logo.png");
    }
}
