package com.example.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Resource Pattern Resolver Pattern
 * ==================================
 * 
 * Demonstrates ResourcePatternResolver for loading multiple resources
 * using Ant-style path patterns with wildcards.
 * 
 * Key Concepts:
 * ------------
 * 1. ResourcePatternResolver - Multiple resource loading
 * 2. Ant-Style Patterns - Wildcards for flexible matching
 * 3. Pattern Matching - *, **, ? wildcards
 * 4. Bulk Loading - Load many resources at once
 * 5. Classpath Scanning - Find all matching resources
 * 
 * Pattern Syntax:
 * --------------
 * - ? - Matches one character
 * - * - Matches zero or more characters (single directory)
 * - ** - Matches zero or more directories
 * 
 * Examples:
 * --------
 * - classpath*:config/*.properties - All properties in config
 * - classpath*:com/example/**/*.xml - All XML files recursively
 * - file:C:/temp/*.txt - All txt files in directory
 * 
 * classpath vs classpath*:
 * -----------------------
 * - classpath: - Search first matching classpath location
 * - classpath*: - Search ALL classpath locations (multiple JARs)
 * 
 * When to Use:
 * -----------
 * - Load multiple configuration files
 * - Scan for plugin files
 * - Find all matching resources
 * - Process batch files
 * - Dynamic resource discovery
 * 
 * Best Practices:
 * --------------
 * - Use classpath*: for multi-JAR scanning
 * - Narrow patterns for better performance
 * - Check resource existence before reading
 * - Handle empty results gracefully
 * - Sort resources if order matters
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ResourcePatternResolverPattern {
    
    @Autowired
    private ResourcePatternResolver resourcePatternResolver;
    
    /**
     * Load all resources matching pattern
     */
    public Resource[] loadResourcesMatching(String locationPattern) throws IOException {
        return resourcePatternResolver.getResources(locationPattern);
    }
    
    /**
     * Load all properties files from classpath
     */
    public Resource[] loadAllPropertiesFiles() throws IOException {
        return resourcePatternResolver.getResources("classpath*:*.properties");
    }
    
    /**
     * Load all XML files from specific package
     */
    public Resource[] loadXmlFilesFromPackage(String packagePath) throws IOException {
        String pattern = "classpath*:" + packagePath.replace('.', '/') + "/**/*.xml";
        return resourcePatternResolver.getResources(pattern);
    }
    
    /**
     * Load resources from multiple directories
     */
    public List<Resource> loadFromMultipleDirs(String... directories) throws IOException {
        List<Resource> allResources = new ArrayList<>();
        
        for (String dir : directories) {
            String pattern = "classpath*:" + dir + "/**/*.*";
            Resource[] resources = resourcePatternResolver.getResources(pattern);
            allResources.addAll(Arrays.asList(resources));
        }
        
        return allResources;
    }
}

/**
 * Example 2: Configuration File Scanner
 */
@Component
class ConfigurationFileScanner {
    
    @Autowired
    private ResourcePatternResolver resolver;
    
    /**
     * Load all application configuration files
     */
    public List<Resource> loadAllConfigFiles() throws IOException {
        List<Resource> configs = new ArrayList<>();
        
        // Load from config directory
        configs.addAll(Arrays.asList(
            resolver.getResources("classpath*:config/**/*.properties")));
        
        // Load from root
        configs.addAll(Arrays.asList(
            resolver.getResources("classpath*:application*.properties")));
        
        // Load YAML files
        configs.addAll(Arrays.asList(
            resolver.getResources("classpath*:application*.yml")));
        
        return configs;
    }
    
    /**
     * Load profile-specific configurations
     */
    public Resource[] loadProfileConfig(String profile) throws IOException {
        String pattern = String.format("classpath*:application-%s.*", profile);
        return resolver.getResources(pattern);
    }
    
    /**
     * Load environment-specific configurations
     */
    public Resource[] loadEnvironmentConfig(String environment) throws IOException {
        String pattern = String.format("classpath*:config/%s/**/*.*", environment);
        return resolver.getResources(pattern);
    }
}

/**
 * Example 3: Template Scanner
 */
@Component
class TemplateScanner {
    
    @Autowired
    private ResourcePatternResolver resolver;
    
    /**
     * Load all HTML templates
     */
    public Resource[] loadAllHtmlTemplates() throws IOException {
        return resolver.getResources("classpath*:templates/**/*.html");
    }
    
    /**
     * Load templates by type
     */
    public Resource[] loadTemplatesByType(String type) throws IOException {
        String pattern = String.format("classpath*:templates/%s/**/*.html", type);
        return resolver.getResources(pattern);
    }
    
    /**
     * Load email templates
     */
    public Resource[] loadEmailTemplates() throws IOException {
        return loadTemplatesByType("email");
    }
    
    /**
     * Load report templates
     */
    public Resource[] loadReportTemplates() throws IOException {
        return resolver.getResources("classpath*:templates/reports/**/*");
    }
}

/**
 * Example 4: Static Resource Scanner
 */
@Component
class StaticResourceScanner {
    
    @Autowired
    private ResourcePatternResolver resolver;
    
    /**
     * Load all CSS files
     */
    public Resource[] loadAllCssFiles() throws IOException {
        return resolver.getResources("classpath*:static/**/*.css");
    }
    
    /**
     * Load all JavaScript files
     */
    public Resource[] loadAllJsFiles() throws IOException {
        return resolver.getResources("classpath*:static/**/*.js");
    }
    
    /**
     * Load all images
     */
    public Resource[] loadAllImages() throws IOException {
        List<Resource> images = new ArrayList<>();
        
        // Load different image formats
        String[] patterns = {
            "classpath*:static/**/*.png",
            "classpath*:static/**/*.jpg",
            "classpath*:static/**/*.jpeg",
            "classpath*:static/**/*.gif",
            "classpath*:static/**/*.svg"
        };
        
        for (String pattern : patterns) {
            images.addAll(Arrays.asList(resolver.getResources(pattern)));
        }
        
        return images.toArray(new Resource[0]);
    }
}

/**
 * Example 5: Plugin Scanner
 */
@Component
class PluginScanner {
    
    @Autowired
    private ResourcePatternResolver resolver;
    
    /**
     * Scan for plugin configuration files
     */
    public Resource[] scanPluginConfigs() throws IOException {
        return resolver.getResources("classpath*:META-INF/plugins/*.xml");
    }
    
    /**
     * Scan for plugin JAR files
     */
    public Resource[] scanPluginJars(String pluginDir) throws IOException {
        String pattern = "file:" + pluginDir + "/**/*.jar";
        return resolver.getResources(pattern);
    }
    
    /**
     * Scan for Spring configuration
     */
    public Resource[] scanSpringConfigs() throws IOException {
        return resolver.getResources("classpath*:META-INF/spring/**/*.xml");
    }
}

/**
 * Example 6: Resource Filter
 */
@Component
class ResourceFilter {
    
    @Autowired
    private ResourcePatternResolver resolver;
    
    /**
     * Load resources and filter by size
     */
    public List<Resource> loadResourcesBySizeRange(String pattern, 
                                                    long minSize, 
                                                    long maxSize) 
                                                    throws IOException {
        Resource[] resources = resolver.getResources(pattern);
        List<Resource> filtered = new ArrayList<>();
        
        for (Resource resource : resources) {
            try {
                long size = resource.contentLength();
                if (size >= minSize && size <= maxSize) {
                    filtered.add(resource);
                }
            } catch (IOException e) {
                // Skip resources that can't be read
            }
        }
        
        return filtered;
    }
    
    /**
     * Load only readable resources
     */
    public List<Resource> loadReadableResources(String pattern) throws IOException {
        Resource[] resources = resolver.getResources(pattern);
        List<Resource> readable = new ArrayList<>();
        
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                readable.add(resource);
            }
        }
        
        return readable;
    }
}

/**
 * Example 7: Multi-Format Loader
 */
@Component
class MultiFormatLoader {
    
    @Autowired
    private ResourcePatternResolver resolver;
    
    /**
     * Load data files in multiple formats
     */
    public Resource[] loadDataFiles(String directory) throws IOException {
        List<Resource> allFiles = new ArrayList<>();
        
        String[] formats = {"json", "xml", "yml", "yaml", "csv"};
        
        for (String format : formats) {
            String pattern = String.format("classpath*:%s/**/*.%s", directory, format);
            allFiles.addAll(Arrays.asList(resolver.getResources(pattern)));
        }
        
        return allFiles.toArray(new Resource[0]);
    }
    
    /**
     * Load configuration in order of preference
     */
    public Resource loadConfigWithPreference(String name, String... formats) 
                                             throws IOException {
        for (String format : formats) {
            String pattern = String.format("classpath*:%s.%s", name, format);
            Resource[] resources = resolver.getResources(pattern);
            
            if (resources.length > 0) {
                return resources[0];
            }
        }
        
        return null;
    }
}

/**
 * Usage Examples
 */
class ResourcePatternResolverUsageExamples {
    
    public static void main(String[] args) throws IOException {
        // Note: In real Spring app, these would be autowired
        
        System.out.println("Resource Pattern Resolver Demonstration");
        System.out.println("========================================\n");
        
        System.out.println("Pattern Examples:");
        System.out.println("- classpath*:config/*.properties");
        System.out.println("  Loads all .properties files in config directory");
        
        System.out.println("\n- classpath*:com/example/**/*.xml");
        System.out.println("  Loads all .xml files recursively from com/example");
        
        System.out.println("\n- classpath*:application-*.yml");
        System.out.println("  Loads all application profile YAML files");
        
        System.out.println("\n- classpath*:static/**/*.{css,js}");
        System.out.println("  Loads all CSS and JS files from static directory");
        
        System.out.println("\nUse Cases:");
        System.out.println("1. Load all configuration files from multiple JARs");
        System.out.println("2. Scan for plugin configurations");
        System.out.println("3. Find all templates in classpath");
        System.out.println("4. Discover static resources");
        System.out.println("5. Load environment-specific configs");
    }
}
