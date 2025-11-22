package com.spring.patterns.resourcemanagement;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

/**
 * Resource Pattern Resolver Pattern
 * 
 * Demonstrates Spring's ResourcePatternResolver for pattern-based resource resolution
 * with support for wildcards and multiple resource matching.
 * 
 * Key Concepts:
 * - ResourcePatternResolver extends ResourceLoader
 * - Supports Ant-style path patterns with wildcards (*, **, ?)
 * - Can find multiple resources matching a pattern
 * - classpath*: prefix searches all classpath locations
 */

@Configuration
class ResourcePatternResolverConfig {
    
    @Bean
    public ResourceScannerService resourceScannerService() {
        return new ResourceScannerService();
    }
    
    @Bean
    public ResourcePatternResolver resourcePatternResolver() {
        return new PathMatchingResourcePatternResolver();
    }
}

@Service
class ResourceScannerService {
    
    private final ResourcePatternResolver resourcePatternResolver;
    
    public ResourceScannerService(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }
    
    // Default constructor
    public ResourceScannerService() {
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
    }
    
    /**
     * Find all resources matching a pattern
     */
    public Resource[] findResources(String locationPattern) throws IOException {
        return resourcePatternResolver.getResources(locationPattern);
    }
    
    /**
     * Find all property files in classpath
     */
    public Resource[] findAllPropertyFiles() throws IOException {
        return resourcePatternResolver.getResources("classpath*:*.properties");
    }
    
    /**
     * Find all XML files in a specific package
     */
    public Resource[] findXmlFilesInPackage(String packagePath) throws IOException {
        String pattern = "classpath*:" + packagePath.replace('.', '/') + "/**/*.xml";
        return resourcePatternResolver.getResources(pattern);
    }
    
    /**
     * Find all resources in META-INF directory
     */
    public Resource[] findMetaInfResources() throws IOException {
        return resourcePatternResolver.getResources("classpath*:META-INF/**");
    }
    
    /**
     * Find all Java class files in a package
     */
    public Resource[] findClassFiles(String packagePath) throws IOException {
        String pattern = "classpath*:" + packagePath.replace('.', '/') + "/**/*.class";
        return resourcePatternResolver.getResources(pattern);
    }
    
    /**
     * Display resource information
     */
    public void displayResources(Resource[] resources, String description) throws IOException {
        System.out.println("\n=== " + description + " ===");
        System.out.println("Total resources found: " + resources.length);
        
        for (int i = 0; i < Math.min(resources.length, 10); i++) {
            Resource resource = resources[i];
            System.out.println("\nResource #" + (i + 1) + ":");
            System.out.println("  Description: " + resource.getDescription());
            System.out.println("  Filename: " + resource.getFilename());
            System.out.println("  Exists: " + resource.exists());
            if (resource.exists()) {
                System.out.println("  URI: " + resource.getURI());
                System.out.println("  Readable: " + resource.isReadable());
            }
        }
        
        if (resources.length > 10) {
            System.out.println("\n... and " + (resources.length - 10) + " more resources");
        }
    }
}

/**
 * Service for advanced pattern matching
 */
class AdvancedResourcePatternService {
    
    private final ResourcePatternResolver resolver;
    
    public AdvancedResourcePatternService() {
        this.resolver = new PathMatchingResourcePatternResolver();
    }
    
    /**
     * Find resources using complex patterns
     */
    public void demonstratePatternMatching() throws IOException {
        System.out.println("\n=== Advanced Pattern Matching ===");
        
        // Pattern 1: Single wildcard (*)
        System.out.println("\n1. Single wildcard - classpath:*.properties");
        Resource[] resources1 = resolver.getResources("classpath:*.properties");
        printResourceCount(resources1);
        
        // Pattern 2: Double wildcard (**)
        System.out.println("\n2. Double wildcard - classpath*:**/*.properties");
        Resource[] resources2 = resolver.getResources("classpath*:**/*.properties");
        printResourceCount(resources2);
        
        // Pattern 3: Specific directory pattern
        System.out.println("\n3. Specific directory - classpath*:com/spring/**/*.class");
        Resource[] resources3 = resolver.getResources("classpath*:com/spring/**/*.class");
        printResourceCount(resources3);
        
        // Pattern 4: Character wildcard (?)
        System.out.println("\n4. Character wildcard - classpath:application-?.properties");
        Resource[] resources4 = resolver.getResources("classpath:application-?.properties");
        printResourceCount(resources4);
        
        // Pattern 5: Multiple extensions
        System.out.println("\n5. Finding all config files");
        String[] configPatterns = {
            "classpath*:*.properties",
            "classpath*:*.yml",
            "classpath*:*.yaml",
            "classpath*:*.xml"
        };
        
        int totalConfigs = 0;
        for (String pattern : configPatterns) {
            Resource[] resources = resolver.getResources(pattern);
            totalConfigs += resources.length;
        }
        System.out.println("Total configuration files: " + totalConfigs);
    }
    
    private void printResourceCount(Resource[] resources) {
        System.out.println("Found " + resources.length + " resource(s)");
        Arrays.stream(resources)
                .limit(3)
                .forEach(r -> System.out.println("  - " + r.getFilename()));
        if (resources.length > 3) {
            System.out.println("  ... and " + (resources.length - 3) + " more");
        }
    }
}

/**
 * Classpath scanner for finding classes and resources
 */
class ClasspathScanner {
    
    private final ResourcePatternResolver resolver;
    
    public ClasspathScanner() {
        this.resolver = new PathMatchingResourcePatternResolver();
    }
    
    /**
     * Scan for all classes in a package
     */
    public void scanPackage(String basePackage) throws IOException {
        System.out.println("\n=== Scanning Package: " + basePackage + " ===");
        
        String pattern = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";
        Resource[] resources = resolver.getResources(pattern);
        
        System.out.println("Found " + resources.length + " classes");
        
        Arrays.stream(resources)
                .limit(5)
                .forEach(resource -> {
                    try {
                        System.out.println("  Class: " + resource.getURL().getPath());
                    } catch (IOException e) {
                        System.err.println("  Error reading: " + resource.getDescription());
                    }
                });
    }
    
    /**
     * Find all Spring configuration files
     */
    public Resource[] findSpringConfigFiles() throws IOException {
        String[] patterns = {
            "classpath*:applicationContext*.xml",
            "classpath*:spring-*.xml",
            "classpath*:**/spring/*.xml"
        };
        
        return Arrays.stream(patterns)
                .flatMap(pattern -> {
                    try {
                        return Arrays.stream(resolver.getResources(pattern));
                    } catch (IOException e) {
                        return Arrays.stream(new Resource[0]);
                    }
                })
                .toArray(Resource[]::new);
    }
}

public class ResourcePatternResolverPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Resource Pattern Resolver Demo ===\n");
        
        try {
            // Create Spring Application Context
            ApplicationContext context = new AnnotationConfigApplicationContext(
                    ResourcePatternResolverConfig.class);
            
            // Get ResourceScannerService
            ResourceScannerService scannerService = context.getBean(ResourceScannerService.class);
            
            // Demo 1: Find all property files
            System.out.println("1. Finding all property files:");
            Resource[] propertyFiles = scannerService.findAllPropertyFiles();
            scannerService.displayResources(propertyFiles, "Property Files");
            
            // Demo 2: Find resources in META-INF
            System.out.println("\n2. Finding META-INF resources:");
            Resource[] metaInfResources = scannerService.findMetaInfResources();
            scannerService.displayResources(metaInfResources, "META-INF Resources");
            
            // Demo 3: Pattern matching with wildcards
            System.out.println("\n3. Pattern Matching Examples:");
            demonstrateWildcardPatterns(context);
            
            // Demo 4: Advanced pattern matching
            AdvancedResourcePatternService advancedService = new AdvancedResourcePatternService();
            advancedService.demonstratePatternMatching();
            
            // Demo 5: Classpath scanning
            System.out.println("\n5. Classpath Scanning:");
            ClasspathScanner scanner = new ClasspathScanner();
            scanner.scanPackage("com.spring.patterns");
            
            // Demo 6: Find specific file types
            System.out.println("\n6. Finding specific file types:");
            findSpecificFileTypes(context);
            
            // Demo 7: ApplicationContext as ResourcePatternResolver
            System.out.println("\n7. Using ApplicationContext as ResourcePatternResolver:");
            Resource[] contextResources = context.getResources("classpath*:*.xml");
            System.out.println("XML files found: " + contextResources.length);
            
            // Close context
            ((AnnotationConfigApplicationContext) context).close();
            
        } catch (Exception e) {
            System.err.println("Error in demo: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Demo Completed ===");
    }
    
    private static void demonstrateWildcardPatterns(ApplicationContext context) throws IOException {
        System.out.println("\nWildcard Pattern Examples:");
        
        String[] patterns = {
            "classpath:*.properties",              // Files in root of classpath
            "classpath*:*.properties",             // Files in root of all classpath entries
            "classpath:com/spring/**/*.class",     // All classes in package and subpackages
            "classpath*:META-INF/spring.*.xml",   // Spring config files in META-INF
            "classpath:application-?.properties"   // Match single character
        };
        
        for (String pattern : patterns) {
            try {
                Resource[] resources = context.getResources(pattern);
                System.out.println("\nPattern: " + pattern);
                System.out.println("  Matches: " + resources.length + " resource(s)");
            } catch (Exception e) {
                System.out.println("\nPattern: " + pattern);
                System.out.println("  Error: " + e.getMessage());
            }
        }
    }
    
    private static void findSpecificFileTypes(ApplicationContext context) throws IOException {
        String[][] fileTypes = {
            {"Properties", "classpath*:**/*.properties"},
            {"XML", "classpath*:**/*.xml"},
            {"YAML", "classpath*:**/*.yml"},
            {"JSON", "classpath*:**/*.json"}
        };
        
        for (String[] fileType : fileTypes) {
            try {
                Resource[] resources = context.getResources(fileType[1]);
                System.out.println(fileType[0] + " files: " + resources.length);
            } catch (Exception e) {
                System.out.println(fileType[0] + " files: Error - " + e.getMessage());
            }
        }
    }
}

/*
 * Key Takeaways:
 * 
 * 1. ResourcePatternResolver extends ResourceLoader with pattern matching
 * 2. Supports Ant-style patterns: *, **, ?
 * 3. classpath*: searches all JAR files and directories
 * 4. Useful for component scanning and resource discovery
 * 5. ApplicationContext implements ResourcePatternResolver
 * 
 * Pattern Syntax:
 * - * matches any number of characters in a path segment
 * - ** matches any number of path segments
 * - ? matches exactly one character
 * - classpath: searches only the first matching location
 * - classpath*: searches all matching locations
 * 
 * Benefits:
 * - Batch resource loading
 * - Dynamic resource discovery
 * - Component scanning support
 * - Flexible pattern matching
 * 
 * Use Cases:
 * - Finding all configuration files
 * - Component scanning for annotations
 * - Loading multiple resources at once
 * - Resource inventory and auditing
 * - Plugin/module discovery
 */
