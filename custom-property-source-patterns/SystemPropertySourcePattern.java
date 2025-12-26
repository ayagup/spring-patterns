package com.example.propertysource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * System Property Source Pattern
 * 
 * Demonstrates accessing Java system properties and OS environment variables
 * as property sources in Spring applications.
 * 
 * Key Concepts:
 * - System.getProperty() access
 * - System.getenv() access
 * - SystemEnvironmentPropertySource
 * - JVM arguments as properties
 * - OS-level configuration
 * 
 * Use Cases:
 * - Container-based deployments
 * - Cloud-native applications
 * - CI/CD pipeline configuration
 * - Environment-specific settings
 * - Security-sensitive configuration
 */
@SpringBootApplication
public class SystemPropertySourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(SystemPropertySourcePattern.class, args);
    }
}

/**
 * Service for accessing system properties
 */
@org.springframework.stereotype.Service
class SystemPropertyService {

    private final ConfigurableEnvironment environment;

    public SystemPropertyService(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Get Java system property
     */
    public String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * Get environment variable
     */
    public String getEnvironmentVariable(String key) {
        return System.getenv(key);
    }

    /**
     * Get from Spring environment (checks all sources)
     */
    public String getFromEnvironment(String key) {
        return environment.getProperty(key);
    }

    /**
     * Get all system properties
     */
    public Map<String, String> getAllSystemProperties() {
        Map<String, String> props = new HashMap<>();
        System.getProperties().forEach((key, value) -> 
                props.put(key.toString(), value.toString()));
        return props;
    }

    /**
     * Get all environment variables
     */
    public Map<String, String> getAllEnvironmentVariables() {
        return new HashMap<>(System.getenv());
    }

    /**
     * Common system properties
     */
    public Map<String, String> getCommonSystemProperties() {
        return Map.of(
                "java.version", System.getProperty("java.version", "N/A"),
                "java.home", System.getProperty("java.home", "N/A"),
                "os.name", System.getProperty("os.name", "N/A"),
                "os.version", System.getProperty("os.version", "N/A"),
                "user.name", System.getProperty("user.name", "N/A"),
                "user.home", System.getProperty("user.home", "N/A"),
                "user.dir", System.getProperty("user.dir", "N/A"),
                "file.separator", System.getProperty("file.separator", "N/A")
        );
    }
}

/**
 * Controller to expose system properties
 */
@RestController
@RequestMapping("/api/system")
class SystemPropertyController {

    private final SystemPropertyService systemPropertyService;

    public SystemPropertyController(SystemPropertyService systemPropertyService) {
        this.systemPropertyService = systemPropertyService;
    }

    @GetMapping("/property")
    public Map<String, String> getSystemProperty(String key) {
        return Map.of(
                "key", key,
                "value", systemPropertyService.getSystemProperty(key) != null ? 
                        systemPropertyService.getSystemProperty(key) : "Not found"
        );
    }

    @GetMapping("/env")
    public Map<String, String> getEnvironmentVariable(String key) {
        return Map.of(
                "key", key,
                "value", systemPropertyService.getEnvironmentVariable(key) != null ? 
                        systemPropertyService.getEnvironmentVariable(key) : "Not found"
        );
    }

    @GetMapping("/properties/all")
    public Map<String, String> getAllSystemProperties() {
        return systemPropertyService.getAllSystemProperties();
    }

    @GetMapping("/env/all")
    public Map<String, String> getAllEnvironmentVariables() {
        return systemPropertyService.getAllEnvironmentVariables();
    }

    @GetMapping("/properties/common")
    public Map<String, String> getCommonProperties() {
        return systemPropertyService.getCommonSystemProperties();
    }
}

/**
 * Documentation:
 * 
 * System Properties:
 * - Set via -D JVM arguments: java -Dkey=value -jar app.jar
 * - Accessed via System.getProperty(String key)
 * - Can be set programmatically: System.setProperty(String key, String value)
 * - Available in Spring Environment with "systemProperties" source name
 * 
 * Environment Variables:
 * - Set at OS level
 * - Accessed via System.getenv(String key)
 * - Available in Spring Environment with "systemEnvironment" source name
 * - Naming convention: UPPERCASE_WITH_UNDERSCORES
 * - Spring converts to lowercase.with.dots for property access
 * 
 * Common System Properties:
 * - java.version: Java version
 * - java.home: Java installation directory
 * - java.class.path: Java classpath
 * - os.name: Operating system name
 * - os.version: Operating system version
 * - os.arch: Operating system architecture
 * - user.name: User account name
 * - user.home: User home directory
 * - user.dir: Current working directory
 * - file.separator: File separator (/ or \)
 * - path.separator: Path separator (: or ;)
 * - line.separator: Line separator
 * 
 * Setting System Properties:
 * 
 * 1. Command Line:
 *    java -Dapp.name=MyApp -Dserver.port=8080 -jar app.jar
 * 
 * 2. Programmatically:
 *    System.setProperty("app.name", "MyApp");
 * 
 * 3. In application.properties:
 *    Cannot directly set, but can reference with ${...}
 * 
 * Setting Environment Variables:
 * 
 * 1. Windows:
 *    set APP_NAME=MyApp
 *    setx APP_NAME MyApp (permanent)
 * 
 * 2. Linux/Mac:
 *    export APP_NAME=MyApp
 *    Add to .bashrc or .zshrc for permanence
 * 
 * 3. Docker:
 *    docker run -e APP_NAME=MyApp myimage
 * 
 * 4. Kubernetes:
 *    env:
 *      - name: APP_NAME
 *        value: MyApp
 * 
 * Spring Environment Access:
 * - Both system properties and environment variables are automatically available
 * - Environment variables have lower precedence than system properties
 * - Use environment.getProperty(key) for unified access
 * 
 * Property Name Mapping:
 * - Environment variable: DATABASE_URL
 * - Spring property: database.url or database_url or databaseUrl
 * - Relaxed binding applies
 * 
 * Best Practices:
 * - Use environment variables for deployment-specific config
 * - Use system properties for JVM-specific config
 * - Never hardcode sensitive data
 * - Document required environment variables
 * - Provide defaults for optional variables
 * - Use meaningful names
 * - Follow naming conventions
 * 
 * Security Considerations:
 * - Environment variables visible in process listings
 * - System properties visible in JMX
 * - Use secrets management for sensitive data
 * - Rotate credentials regularly
 * - Limit access to environment
 * 
 * Common Use Cases:
 * - Database connection strings
 * - API keys and tokens
 * - Service URLs
 * - Feature flags
 * - Debug/verbose mode
 * - Deployment environment (dev/prod)
 * 
 * Priority Order:
 * 1. System properties (-D arguments)
 * 2. Environment variables
 * 3. Application properties
 * 
 * Tips:
 * - Use SPRING_APPLICATION_JSON for complex config in env var
 * - Override any property via environment variable
 * - Test with different environments
 * - Use profiles with environment variables
 * - Document all required variables
 */
