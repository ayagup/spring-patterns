package com.example.propertysource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Properties Property Source Pattern
 * 
 * Demonstrates loading traditional Java Properties files as property sources.
 * This is the classic approach to external configuration in Spring applications.
 * 
 * Key Concepts:
 * - @PropertySource annotation
 * - Properties file format
 * - Multiple property files
 * - PropertiesPropertySource
 * - Resource loading
 * 
 * Use Cases:
 * - Traditional configuration files
 * - Legacy system integration
 * - Simple key-value configuration
 * - Override mechanisms
 * - Profile-specific properties
 */
@SpringBootApplication
@PropertySources({
    @PropertySource("classpath:application.properties"),
    @PropertySource("classpath:database.properties"),
    @PropertySource(value = "classpath:optional.properties", ignoreResourceNotFound = true)
})
public class PropertiesPropertySourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(PropertiesPropertySourcePattern.class, args);
    }
}

/**
 * Configuration for loading properties files
 */
@Configuration
class PropertiesConfig {

    private final ConfigurableEnvironment environment;

    public PropertiesConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Load properties file programmatically
     */
    @PostConstruct
    public void loadPropertiesFiles() throws IOException {
        // Load from classpath
        Resource resource = new ClassPathResource("custom.properties");
        if (resource.exists()) {
            Properties props = PropertiesLoaderUtils.loadProperties(resource);
            PropertiesPropertySource propertySource = 
                    new PropertiesPropertySource("customProperties", props);
            environment.getPropertySources().addLast(propertySource);
        }

        // Load multiple properties files
        loadMultiplePropertyFiles();
    }

    /**
     * Load properties from multiple files
     */
    private void loadMultiplePropertyFiles() throws IOException {
        String[] propertyFiles = {
                "config/app.properties",
                "config/security.properties",
                "config/features.properties"
        };

        for (String file : propertyFiles) {
            Resource resource = new ClassPathResource(file);
            if (resource.exists()) {
                Properties props = PropertiesLoaderUtils.loadProperties(resource);
                String sourceName = file.replace("/", ".").replace(".properties", "");
                PropertiesPropertySource source = new PropertiesPropertySource(sourceName, props);
                environment.getPropertySources().addLast(source);
            }
        }
    }

    /**
     * Bean that creates Properties object
     */
    @Bean
    public Properties applicationProperties() throws IOException {
        Resource resource = new ClassPathResource("application.properties");
        return PropertiesLoaderUtils.loadProperties(resource);
    }
}

/**
 * Service for accessing properties
 */
@org.springframework.stereotype.Service
class PropertiesService {

    private final org.springframework.core.env.Environment environment;
    private final Properties applicationProperties;

    public PropertiesService(org.springframework.core.env.Environment environment,
                            Properties applicationProperties) {
        this.environment = environment;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Get property from environment
     */
    public String getProperty(String key) {
        return environment.getProperty(key);
    }

    /**
     * Get property from loaded Properties bean
     */
    public String getPropertyFromBean(String key) {
        return applicationProperties.getProperty(key);
    }

    /**
     * Get all properties from bean
     */
    public Map<String, String> getAllProperties() {
        Map<String, String> props = new HashMap<>();
        applicationProperties.forEach((key, value) -> 
                props.put(key.toString(), value.toString()));
        return props;
    }

    /**
     * Check if property exists
     */
    public boolean hasProperty(String key) {
        return environment.containsProperty(key);
    }
}

/**
 * Controller to expose properties
 */
@RestController
@RequestMapping("/api/properties")
class PropertiesController {

    private final PropertiesService propertiesService;

    public PropertiesController(PropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    @GetMapping("/get")
    public Map<String, Object> getProperty(String key) {
        return Map.of(
                "key", key,
                "value", propertiesService.getProperty(key),
                "exists", propertiesService.hasProperty(key)
        );
    }

    @GetMapping("/all")
    public Map<String, String> getAllProperties() {
        return propertiesService.getAllProperties();
    }
}

/**
 * Documentation:
 * 
 * Properties File Format:
 * 
 * # Comment line
 * app.name=My Application
 * app.version=1.0.0
 * app.description=This is my application
 * 
 * # Nested properties use dot notation
 * server.port=8080
 * server.host=localhost
 * 
 * # Lists use comma separation
 * app.allowed.origins=http://localhost:3000,http://localhost:4200
 * 
 * # Special characters need escaping
 * message=Hello\nWorld
 * path=C:\\Users\\name\\folder
 * 
 * # Multiline values use backslash
 * long.text=This is a very long \
 *          property value that spans \
 *          multiple lines
 * 
 * @PropertySource Annotation:
 * 
 * Basic Usage:
 * @PropertySource("classpath:app.properties")
 * 
 * Multiple Files:
 * @PropertySource({"classpath:app.properties", "classpath:db.properties"})
 * 
 * Optional Files:
 * @PropertySource(value = "classpath:optional.properties", ignoreResourceNotFound = true)
 * 
 * With Placeholders:
 * @PropertySource("classpath:app-${spring.profiles.active}.properties")
 * 
 * Encoding:
 * @PropertySource(value = "classpath:app.properties", encoding = "UTF-8")
 * 
 * Multiple @PropertySource:
 * @PropertySources({
 *     @PropertySource("classpath:app.properties"),
 *     @PropertySource("classpath:db.properties")
 * })
 * 
 * Loading Properties Programmatically:
 * 
 * 1. Using PropertiesLoaderUtils:
 *    Resource resource = new ClassPathResource("app.properties");
 *    Properties props = PropertiesLoaderUtils.loadProperties(resource);
 * 
 * 2. Using Properties.load():
 *    Properties props = new Properties();
 *    try (InputStream is = getClass().getResourceAsStream("/app.properties")) {
 *        props.load(is);
 *    }
 * 
 * 3. Adding to Environment:
 *    PropertiesPropertySource source = new PropertiesPropertySource("name", props);
 *    environment.getPropertySources().addFirst(source);
 * 
 * Property Placeholder Resolution:
 * - ${property.name}: Resolved from any property source
 * - ${property.name:defaultValue}: With default value
 * - ${property.${nested}}: Nested placeholders
 * 
 * Profile-Specific Properties:
 * - application.properties: Default properties
 * - application-dev.properties: Development profile
 * - application-prod.properties: Production profile
 * - Profile-specific files override default
 * 
 * Property Precedence (highest to lowest):
 * 1. @PropertySource files (order matters)
 * 2. application-{profile}.properties (external)
 * 3. application-{profile}.properties (packaged)
 * 4. application.properties (external)
 * 5. application.properties (packaged)
 * 
 * Best Practices:
 * - Use meaningful property names
 * - Group related properties with common prefix
 * - Provide defaults for optional properties
 * - Document properties with comments
 * - Use profiles for environment-specific config
 * - Externalize sensitive data
 * - Use UTF-8 encoding
 * - Validate property values
 * 
 * Common Property Patterns:
 * - app.*: Application-specific properties
 * - server.*: Server configuration
 * - spring.*: Spring framework properties
 * - logging.*: Logging configuration
 * - security.*: Security settings
 * 
 * Special Characters:
 * - Escape sequences: \n (newline), \t (tab), \\ (backslash)
 * - Spaces: Can be included without quotes
 * - Equals: Can be in value if not the first occurrence
 * - Colons: Can be used instead of equals: key: value
 * - Comments: # or ! at line start
 * 
 * Advantages of Properties Files:
 * - Simple format
 * - Well understood
 * - Good tool support
 * - Easy to diff
 * - Natural for Java developers
 * 
 * Limitations:
 * - Flat structure (repetitive for hierarchical data)
 * - No native support for lists/maps
 * - Less readable for complex configurations
 * - No type information
 * 
 * Integration with Environment:
 * - Properties loaded into Environment
 * - Accessible via @Value
 * - Type conversion support
 * - Placeholder resolution
 * - Profile activation
 */
