package com.example.nativeimage.patterns;

import org.springframework.aot.hint.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 💡 SPRING BOOT NATIVE IMAGE - RESOURCE CONFIG PATTERN 💡
 * =========================================================
 * 
 * Demonstrates resource configuration for GraalVM Native Image.
 * Resources (files, templates, properties) must be explicitly registered
 * to be included in the native image. By default, resources are excluded
 * to minimize binary size.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ RESOURCE REGISTRATION:
 *    - Resources not automatically included
 *    - Must register patterns or specific files
 *    - Supports wildcards (*, **)
 *    - ClassLoader.getResource() requires hints
 * 
 * 2️⃣ RESOURCE PATTERNS:
 *    - Single file: "config/app.properties"
 *    - Wildcard: "config/*.json"
 *    - Recursive: "static/**" (all files in static/)
 *    - Extension: "**/*.xml" (all XML files)
 * 
 * 3️⃣ RESOURCE BUNDLES:
 *    - Internationalization (i18n)
 *    - Message bundles
 *    - Resource bundles
 *    - Locale-specific resources
 * 
 * 4️⃣ COMMON RESOURCES:
 *    - application.properties/yml
 *    - Static web resources
 *    - Templates (Thymeleaf, Freemarker)
 *    - JSON/XML configuration
 *    - Certificate files
 *    - Database migration scripts
 * 
 * 📦 DEPENDENCIES:
 * ===============
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-web</artifactId>
 * </dependency>
 * 
 * 🔧 RESOURCE CONFIG API:
 * ======================
 * 
 * Register Single File:
 * ---------------------
 * hints.resources().registerPattern("config/application.json");
 * 
 * Register Pattern (Wildcard):
 * ----------------------------
 * hints.resources().registerPattern("config/*.properties");
 * hints.resources().registerPattern("static/**");
 * hints.resources().registerPattern("**/*.xml");
 * 
 * Register Resource Bundle:
 * -------------------------
 * hints.resources().registerResourceBundle("messages");
 * hints.resources().registerResourceBundle("i18n.labels");
 * 
 * Register Type (Class File):
 * ---------------------------
 * hints.resources().registerType(MyClass.class);
 * 
 * Register Pattern If Present:
 * ----------------------------
 * hints.resources().registerPatternIfPresent(classLoader,
 *     "optional-config.json",
 *     pattern -> {}
 * );
 * 
 * 🎯 RESOURCE PATTERN SYNTAX:
 * ==========================
 * 
 * Exact Match:
 * ------------
 * "application.properties"              # Single file
 * "config/database.yml"                 # Specific path
 * 
 * Extension Wildcard:
 * -------------------
 * "*.properties"                        # All .properties in root
 * "config/*.json"                       # All .json in config/
 * 
 * Recursive Wildcard:
 * -------------------
 * "static/**"                           # All files under static/
 * "templates/**/*.html"                 # All HTML in templates/
 * "**/*.xml"                            # All XML everywhere
 * 
 * Directory Pattern:
 * ------------------
 * "META-INF/resources/**"               # All META-INF resources
 * "db/migration/**"                     # Flyway migrations
 * 
 * 📝 COMMON RESOURCE REGISTRATIONS:
 * =================================
 * 
 * Spring Boot Configuration:
 * --------------------------
 * hints.resources()
 *     .registerPattern("application*.properties")
 *     .registerPattern("application*.yml")
 *     .registerPattern("application*.yaml")
 *     .registerPattern("bootstrap*.properties")
 *     .registerPattern("bootstrap*.yml");
 * 
 * Static Web Resources:
 * ---------------------
 * hints.resources()
 *     .registerPattern("static/**")
 *     .registerPattern("public/**")
 *     .registerPattern("META-INF/resources/**");
 * 
 * Templates:
 * ----------
 * hints.resources()
 *     .registerPattern("templates/**")
 *     .registerPattern("templates/**/*.html")
 *     .registerPattern("templates/**/*.ftl");
 * 
 * Database Migrations:
 * --------------------
 * hints.resources()
 *     .registerPattern("db/migration/**")      # Flyway
 *     .registerPattern("db/changelog/**");     # Liquibase
 * 
 * Internationalization:
 * ---------------------
 * hints.resources()
 *     .registerResourceBundle("messages")
 *     .registerResourceBundle("i18n.labels")
 *     .registerPattern("i18n/**/*.properties");
 * 
 * 💡 WHEN TO USE RESOURCE CONFIG:
 * ==============================
 * ✅ Loading properties/YAML files
 * ✅ Serving static web content
 * ✅ Template rendering
 * ✅ JSON/XML configuration
 * ✅ I18n message bundles
 * ✅ Database migration scripts
 * ✅ SSL certificates
 * ✅ Image files, CSS, JavaScript
 * 
 * ❌ ALTERNATIVES:
 * ===============
 * ❌ Externalize configuration (cloud config)
 * ❌ Database-driven configuration
 * ❌ Environment variables
 * ❌ Mount volumes in containers
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
@ImportRuntimeHints(ResourceConfigPattern.ResourceConfigHints.class)
public class ResourceConfigPattern {

    public static void main(String[] args) {
        SpringApplication.run(ResourceConfigPattern.class, args);
    }

    /**
     * Comprehensive Resource Configuration
     */
    static class ResourceConfigHints implements RuntimeHintsRegistrar {
        
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1. Spring Boot configuration files
            registerSpringBootConfig(hints);
            
            // 2. Static web resources
            registerStaticResources(hints);
            
            // 3. Templates
            registerTemplates(hints);
            
            // 4. Data files
            registerDataFiles(hints);
            
            // 5. Database migrations
            registerDatabaseMigrations(hints);
            
            // 6. Internationalization bundles
            registerI18nBundles(hints);
            
            // 7. Certificates and keys
            registerSecurityResources(hints);
            
            // 8. Custom configuration
            registerCustomResources(hints);
            
            System.out.println("✅ Resource configuration registered successfully");
        }

        private void registerSpringBootConfig(RuntimeHints hints) {
            // Application properties/YAML
            hints.resources()
                .registerPattern("application*.properties")
                .registerPattern("application*.yml")
                .registerPattern("application*.yaml")
                .registerPattern("bootstrap*.properties")
                .registerPattern("bootstrap*.yml")
                .registerPattern("bootstrap*.yaml");
            
            // Profile-specific configs
            hints.resources()
                .registerPattern("application-*.properties")
                .registerPattern("application-*.yml")
                .registerPattern("application-*.yaml");
        }

        private void registerStaticResources(RuntimeHints hints) {
            // Static web content
            hints.resources()
                .registerPattern("static/**")
                .registerPattern("public/**")
                .registerPattern("resources/**")
                .registerPattern("META-INF/resources/**");
            
            // Web assets
            hints.resources()
                .registerPattern("**/*.css")
                .registerPattern("**/*.js")
                .registerPattern("**/*.html")
                .registerPattern("**/*.png")
                .registerPattern("**/*.jpg")
                .registerPattern("**/*.jpeg")
                .registerPattern("**/*.gif")
                .registerPattern("**/*.svg")
                .registerPattern("**/*.ico");
        }

        private void registerTemplates(RuntimeHints hints) {
            // Template engines
            hints.resources()
                .registerPattern("templates/**")
                .registerPattern("templates/**/*.html")    // Thymeleaf
                .registerPattern("templates/**/*.ftl")     // Freemarker
                .registerPattern("templates/**/*.vm")      // Velocity
                .registerPattern("templates/**/*.mustache"); // Mustache
        }

        private void registerDataFiles(RuntimeHints hints) {
            // JSON configuration
            hints.resources()
                .registerPattern("config/**/*.json")
                .registerPattern("data/**/*.json");
            
            // XML configuration
            hints.resources()
                .registerPattern("config/**/*.xml")
                .registerPattern("data/**/*.xml");
            
            // CSV data
            hints.resources()
                .registerPattern("data/**/*.csv");
            
            // YAML data
            hints.resources()
                .registerPattern("config/**/*.yml")
                .registerPattern("config/**/*.yaml");
        }

        private void registerDatabaseMigrations(RuntimeHints hints) {
            // Flyway migrations
            hints.resources()
                .registerPattern("db/migration/**")
                .registerPattern("db/migration/**/*.sql");
            
            // Liquibase changelogs
            hints.resources()
                .registerPattern("db/changelog/**")
                .registerPattern("db/changelog/**/*.xml")
                .registerPattern("db/changelog/**/*.yml");
        }

        private void registerI18nBundles(RuntimeHints hints) {
            // Message bundles
            hints.resources()
                .registerResourceBundle("messages")
                .registerResourceBundle("Messages")
                .registerResourceBundle("i18n.messages")
                .registerResourceBundle("i18n.labels")
                .registerResourceBundle("i18n.errors");
            
            // I18n property files
            hints.resources()
                .registerPattern("i18n/**/*.properties")
                .registerPattern("messages*.properties")
                .registerPattern("**/messages*.properties");
        }

        private void registerSecurityResources(RuntimeHints hints) {
            // SSL certificates
            hints.resources()
                .registerPattern("**/*.crt")
                .registerPattern("**/*.pem")
                .registerPattern("**/*.key")
                .registerPattern("**/*.p12")
                .registerPattern("**/*.jks");
        }

        private void registerCustomResources(RuntimeHints hints) {
            // Custom application resources
            hints.resources()
                .registerPattern("custom/**")
                .registerPattern("schema/**")
                .registerPattern("scripts/**");
            
            // Configuration files
            hints.resources()
                .registerPattern("*.conf")
                .registerPattern("*.cfg")
                .registerPattern("*.ini");
        }
    }
}

/**
 * Resource Configuration Service
 */
@Service
class ResourceConfigService {

    private final PathMatchingResourcePatternResolver resolver = 
        new PathMatchingResourcePatternResolver();

    /**
     * Get all registered resource patterns
     */
    public Map<String, List<String>> getRegisteredResourcePatterns() {
        Map<String, List<String>> patterns = new LinkedHashMap<>();
        
        List<String> springBoot = new ArrayList<>();
        springBoot.add("application*.properties");
        springBoot.add("application*.yml/yaml");
        springBoot.add("bootstrap*.properties");
        springBoot.add("bootstrap*.yml/yaml");
        patterns.put("Spring Boot Config", springBoot);
        
        List<String> staticResources = new ArrayList<>();
        staticResources.add("static/**");
        staticResources.add("public/**");
        staticResources.add("META-INF/resources/**");
        staticResources.add("**/*.css, *.js, *.html");
        staticResources.add("**/*.png, *.jpg, *.svg, *.ico");
        patterns.put("Static Resources", staticResources);
        
        List<String> templates = new ArrayList<>();
        templates.add("templates/**/*.html (Thymeleaf)");
        templates.add("templates/**/*.ftl (Freemarker)");
        templates.add("templates/**/*.mustache");
        patterns.put("Templates", templates);
        
        List<String> data = new ArrayList<>();
        data.add("config/**/*.json");
        data.add("config/**/*.xml");
        data.add("data/**/*.csv");
        patterns.put("Data Files", data);
        
        List<String> migrations = new ArrayList<>();
        migrations.add("db/migration/** (Flyway)");
        migrations.add("db/changelog/** (Liquibase)");
        patterns.put("Database Migrations", migrations);
        
        List<String> i18n = new ArrayList<>();
        i18n.add("Resource Bundle: messages");
        i18n.add("Resource Bundle: i18n.*");
        i18n.add("i18n/**/*.properties");
        patterns.put("Internationalization", i18n);
        
        List<String> security = new ArrayList<>();
        security.add("**/*.crt, *.pem, *.key");
        security.add("**/*.p12, *.jks");
        patterns.put("Security Resources", security);
        
        return patterns;
    }

    /**
     * Load resource content
     */
    public String loadResourceContent(String path) {
        try {
            Resource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                return "Resource not found: " + path;
            }
            
            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            return "Error loading resource: " + e.getMessage();
        }
    }

    /**
     * Check if resource exists
     */
    public boolean resourceExists(String path) {
        try {
            Resource resource = new ClassPathResource(path);
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Find resources by pattern
     */
    public List<String> findResourcesByPattern(String pattern) {
        try {
            Resource[] resources = resolver.getResources("classpath*:" + pattern);
            return Arrays.stream(resources)
                .map(r -> {
                    try {
                        return r.getURL().toString();
                    } catch (IOException e) {
                        return r.toString();
                    }
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.singletonList("Error: " + e.getMessage());
        }
    }

    /**
     * Get resource pattern best practices
     */
    public List<String> getResourceBestPractices() {
        List<String> practices = new ArrayList<>();
        
        practices.add("✅ Register only needed resources (minimize binary size)");
        practices.add("✅ Use specific patterns (avoid excessive wildcards)");
        practices.add("✅ Test resource loading in native image");
        practices.add("✅ Use resource bundles for i18n");
        practices.add("✅ Document why each resource is needed");
        practices.add("✅ Externalize large files when possible");
        practices.add("⚠️ Avoid registering entire directories unnecessarily");
        practices.add("⚠️ Don't include development-only resources");
        practices.add("⚠️ Consider binary size impact");
        practices.add("💡 Use build-time resource generation when possible");
        
        return practices;
    }

    /**
     * Get resource size estimates
     */
    public Map<String, String> getResourceSizeEstimates() {
        Map<String, String> sizes = new LinkedHashMap<>();
        
        sizes.put("Spring Boot Config", "< 100 KB (properties/YAML)");
        sizes.put("Static Resources", "Varies (CSS/JS/images can be large)");
        sizes.put("Templates", "< 500 KB (HTML templates)");
        sizes.put("Database Migrations", "< 1 MB (SQL scripts)");
        sizes.put("I18n Bundles", "< 200 KB (message properties)");
        sizes.put("Security Resources", "< 50 KB (certificates)");
        sizes.put("Total Typical", "1-5 MB (depending on static assets)");
        
        return sizes;
    }
}

/**
 * Resource Test Service
 */
@Service
class ResourceTestService {

    private final ResourceConfigService resourceConfigService;

    public ResourceTestService(ResourceConfigService resourceConfigService) {
        this.resourceConfigService = resourceConfigService;
    }

    /**
     * Test loading application.properties
     */
    public Map<String, Object> testLoadApplicationProperties() {
        String content = resourceConfigService.loadResourceContent("application.properties");
        return Map.of(
            "resource", "application.properties",
            "exists", !content.startsWith("Resource not found"),
            "content", content.substring(0, Math.min(200, content.length()))
        );
    }

    /**
     * Test resource existence
     */
    public Map<String, Object> testResourceExistence(String path) {
        boolean exists = resourceConfigService.resourceExists(path);
        return Map.of(
            "resource", path,
            "exists", exists,
            "message", exists ? "Resource found" : "Resource not found"
        );
    }

    /**
     * Test pattern matching
     */
    public Map<String, Object> testPatternMatching(String pattern) {
        List<String> resources = resourceConfigService.findResourcesByPattern(pattern);
        return Map.of(
            "pattern", pattern,
            "matches", resources.size(),
            "resources", resources.stream().limit(10).collect(Collectors.toList())
        );
    }

    /**
     * Test static resource loading
     */
    public Map<String, Object> testStaticResourceLoading() {
        List<String> testPaths = Arrays.asList(
            "static/index.html",
            "static/css/style.css",
            "static/js/app.js",
            "public/favicon.ico"
        );
        
        Map<String, Boolean> results = new LinkedHashMap<>();
        for (String path : testPaths) {
            results.put(path, resourceConfigService.resourceExists(path));
        }
        
        return Map.of(
            "tested", testPaths.size(),
            "results", results
        );
    }

    /**
     * Test template resource loading
     */
    public Map<String, Object> testTemplateResourceLoading() {
        List<String> testPaths = Arrays.asList(
            "templates/index.html",
            "templates/home.html",
            "templates/error.html"
        );
        
        Map<String, Boolean> results = new LinkedHashMap<>();
        for (String path : testPaths) {
            results.put(path, resourceConfigService.resourceExists(path));
        }
        
        return Map.of(
            "tested", testPaths.size(),
            "results", results
        );
    }
}

/**
 * Resource Configuration REST Controller
 */
@RestController
@RequestMapping("/api/resource-config")
class ResourceConfigController {

    private final ResourceConfigService resourceConfigService;
    private final ResourceTestService resourceTestService;

    public ResourceConfigController(ResourceConfigService resourceConfigService,
                                     ResourceTestService resourceTestService) {
        this.resourceConfigService = resourceConfigService;
        this.resourceTestService = resourceTestService;
    }

    /**
     * GET /api/resource-config/patterns
     * Get all registered resource patterns
     */
    @GetMapping("/patterns")
    public Map<String, List<String>> getResourcePatterns() {
        return resourceConfigService.getRegisteredResourcePatterns();
    }

    /**
     * GET /api/resource-config/best-practices
     * Get resource configuration best practices
     */
    @GetMapping("/best-practices")
    public List<String> getBestPractices() {
        return resourceConfigService.getResourceBestPractices();
    }

    /**
     * GET /api/resource-config/size-estimates
     * Get resource size estimates
     */
    @GetMapping("/size-estimates")
    public Map<String, String> getSizeEstimates() {
        return resourceConfigService.getResourceSizeEstimates();
    }

    /**
     * GET /api/resource-config/load/{path}
     * Load resource content by path
     */
    @GetMapping("/load/{path}")
    public Map<String, Object> loadResource(@PathVariable String path) {
        String content = resourceConfigService.loadResourceContent(path);
        return Map.of(
            "path", path,
            "content", content
        );
    }

    /**
     * GET /api/resource-config/exists/{path}
     * Check if resource exists
     */
    @GetMapping("/exists/{path}")
    public Map<String, Object> resourceExists(@PathVariable String path) {
        boolean exists = resourceConfigService.resourceExists(path);
        return Map.of(
            "path", path,
            "exists", exists
        );
    }

    /**
     * GET /api/resource-config/find
     * Find resources by pattern
     */
    @GetMapping("/find")
    public List<String> findResources(@RequestParam String pattern) {
        return resourceConfigService.findResourcesByPattern(pattern);
    }

    /**
     * GET /api/resource-config/test/application-properties
     * Test loading application.properties
     */
    @GetMapping("/test/application-properties")
    public Map<String, Object> testApplicationProperties() {
        return resourceTestService.testLoadApplicationProperties();
    }

    /**
     * GET /api/resource-config/test/static-resources
     * Test static resource loading
     */
    @GetMapping("/test/static-resources")
    public Map<String, Object> testStaticResources() {
        return resourceTestService.testStaticResourceLoading();
    }

    /**
     * GET /api/resource-config/test/templates
     * Test template resource loading
     */
    @GetMapping("/test/templates")
    public Map<String, Object> testTemplates() {
        return resourceTestService.testTemplateResourceLoading();
    }

    /**
     * GET /api/resource-config/test/pattern
     * Test pattern matching
     */
    @GetMapping("/test/pattern")
    public Map<String, Object> testPattern(@RequestParam String pattern) {
        return resourceTestService.testPatternMatching(pattern);
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ GET RESOURCE PATTERNS:
 * --------------------------
 * curl http://localhost:8080/api/resource-config/patterns
 * 
 * Response:
 * {
 *   "Spring Boot Config": ["application*.properties", "bootstrap*.yml"],
 *   "Static Resources": ["static/**", "public/**"],
 *   "Templates": ["templates/**/*.html"],
 *   ...
 * }
 * 
 * 2️⃣ LOAD RESOURCE CONTENT:
 * --------------------------
 * curl http://localhost:8080/api/resource-config/load/application.properties
 * 
 * Response:
 * {
 *   "path": "application.properties",
 *   "content": "spring.application.name=resource-config-app\n..."
 * }
 * 
 * 3️⃣ CHECK RESOURCE EXISTS:
 * --------------------------
 * curl http://localhost:8080/api/resource-config/exists/static/index.html
 * 
 * Response:
 * {
 *   "path": "static/index.html",
 *   "exists": true
 * }
 * 
 * 4️⃣ FIND RESOURCES BY PATTERN:
 * ------------------------------
 * curl "http://localhost:8080/api/resource-config/find?pattern=static/**/*.css"
 * 
 * Response:
 * [
 *   "classpath:static/css/bootstrap.css",
 *   "classpath:static/css/style.css",
 *   ...
 * ]
 * 
 * 5️⃣ TEST APPLICATION PROPERTIES:
 * --------------------------------
 * curl http://localhost:8080/api/resource-config/test/application-properties
 * 
 * Response:
 * {
 *   "resource": "application.properties",
 *   "exists": true,
 *   "content": "spring.application.name=..."
 * }
 * 
 * 6️⃣ TEST STATIC RESOURCES:
 * --------------------------
 * curl http://localhost:8080/api/resource-config/test/static-resources
 * 
 * Response:
 * {
 *   "tested": 4,
 *   "results": {
 *     "static/index.html": true,
 *     "static/css/style.css": true,
 *     "static/js/app.js": false,
 *     "public/favicon.ico": true
 *   }
 * }
 * 
 * 7️⃣ GET BEST PRACTICES:
 * -----------------------
 * curl http://localhost:8080/api/resource-config/best-practices
 * 
 * Response:
 * [
 *   "✅ Register only needed resources",
 *   "✅ Use specific patterns",
 *   "⚠️ Avoid registering entire directories",
 *   ...
 * ]
 */
