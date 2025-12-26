package com.example.devtools.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

/**
 * ⚙️ SPRING BOOT DEVTOOLS - PROPERTY DEFAULTS PATTERN ⚙️
 * =======================================================
 * 
 * Demonstrates Spring Boot DevTools property defaults feature. DevTools
 * automatically applies sensible development-time property defaults that
 * improve the development experience without affecting production settings.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ AUTOMATIC PROPERTY DEFAULTS:
 *    - DevTools applies development-friendly defaults
 *    - Caching disabled for templates and static resources
 *    - Detailed error pages enabled
 *    - H2 console enabled
 *    - No manual configuration needed
 * 
 * 2️⃣ CACHE DISABLING:
 *    - Thymeleaf template caching disabled
 *    - FreeMarker template caching disabled
 *    - Groovy template caching disabled
 *    - Mustache template caching disabled
 *    - Static resource caching disabled
 * 
 * 3️⃣ WEB DEVELOPMENT DEFAULTS:
 *    - Error pages with stack traces
 *    - H2 console at /h2-console
 *    - LiveReload enabled
 *    - No need for browser refresh
 * 
 * 4️⃣ OVERRIDE DEFAULTS:
 *    - Explicit properties override DevTools defaults
 *    - Profile-specific overrides
 *    - Environment-specific configuration
 * 
 * 📦 DEPENDENCIES:
 * ===============
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-devtools</artifactId>
 *     <optional>true</optional>
 *     <scope>runtime</scope>
 * </dependency>
 * 
 * 🔧 DEVTOOLS DEFAULT PROPERTIES:
 * ===============================
 * When DevTools is present, these properties are automatically set:
 * 
 * # Template Engine Caching (DISABLED)
 * spring.thymeleaf.cache=false
 * spring.freemarker.cache=false
 * spring.groovy.template.cache=false
 * spring.mustache.cache=false
 * 
 * # Static Resources (CACHING DISABLED)
 * spring.web.resources.cache.cachecontrol.no-cache=true
 * spring.web.resources.chain.cache=false
 * 
 * # H2 Console (ENABLED)
 * spring.h2.console.enabled=true
 * 
 * # Error Handling (DETAILED)
 * server.error.include-stacktrace=always
 * server.error.include-message=always
 * server.error.include-binding-errors=always
 * 
 * # Logging
 * logging.level.web=DEBUG
 * 
 * ⚙️ OVERRIDE DEVTOOLS DEFAULTS:
 * ==============================
 * You can override any DevTools default in application.yml:
 * 
 * # application-dev.yml
 * spring:
 *   thymeleaf:
 *     cache: true              # Override DevTools default (enable cache)
 *   h2:
 *     console:
 *       enabled: false         # Override DevTools default (disable H2 console)
 * 
 * 📋 COMPLETE LIST OF DEVTOOLS DEFAULTS:
 * ======================================
 * 
 * Template Engines:
 * - spring.thymeleaf.cache = false
 * - spring.freemarker.cache = false
 * - spring.groovy.template.cache = false
 * - spring.mustache.cache = false
 * - spring.velocity.cache = false (deprecated)
 * 
 * Static Resources:
 * - spring.web.resources.cache.cachecontrol.no-cache = true
 * - spring.web.resources.chain.cache = false
 * 
 * H2 Database Console:
 * - spring.h2.console.enabled = true
 * 
 * Error Handling:
 * - server.error.include-stacktrace = always
 * - server.error.include-message = always
 * - server.error.include-binding-errors = always
 * 
 * Reactor Debugging:
 * - spring.reactor.debug = true
 * 
 * 💡 BENEFITS:
 * ===========
 * ✅ No cache clearing needed - templates reload automatically
 * ✅ See stack traces immediately - faster debugging
 * ✅ H2 console ready - database inspection
 * ✅ LiveReload works - browser refresh automation
 * ✅ No manual configuration - just add DevTools dependency
 * ✅ Production unaffected - defaults only in development
 * 
 * ⚠️ IMPORTANT NOTES:
 * ==================
 * - DevTools defaults only apply when DevTools is present
 * - DevTools is automatically disabled in packaged JARs
 * - Explicit properties in application.yml override defaults
 * - Profile-specific properties take precedence
 * - Can disable specific defaults by overriding them
 * 
 * 🎨 TEMPLATE ENGINE CACHING:
 * ==========================
 * WITHOUT DevTools:
 * - Thymeleaf caches templates (spring.thymeleaf.cache=true)
 * - Must restart app or clear cache to see changes
 * - Production-ready but inconvenient for development
 * 
 * WITH DevTools:
 * - Thymeleaf caching disabled (spring.thymeleaf.cache=false)
 * - Template changes visible immediately
 * - No restart needed
 * - Perfect for development
 * 
 * 💡 WHEN TO USE:
 * ==============
 * ✅ Local development (always recommended)
 * ✅ Template-heavy applications (Thymeleaf, FreeMarker)
 * ✅ Applications with static resources (CSS, JS)
 * ✅ H2 database development
 * ✅ Rapid prototyping
 * 
 * ❌ WHEN NOT TO USE:
 * ==================
 * ❌ Production deployments (DevTools auto-disabled anyway)
 * ❌ Performance testing (caching disabled affects performance)
 * ❌ Load testing (not representative of production)
 * ❌ When you explicitly need caching in development
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
@EnableConfigurationProperties(DevToolsPropertiesConfig.class)
public class PropertyDefaultsPattern {

    public static void main(String[] args) {
        SpringApplication.run(PropertyDefaultsPattern.class, args);
    }
}

/**
 * DevTools Properties Configuration
 * Demonstrates capturing and displaying DevTools-applied properties
 */
@Configuration
@Profile("dev")
class PropertyDefaultsConfiguration {

    @Bean
    public PropertyDefaultsService propertyDefaultsService() {
        return new PropertyDefaultsService();
    }

    @Bean
    public TemplateEngineConfigService templateEngineConfigService() {
        return new TemplateEngineConfigService();
    }

    @Bean
    public StaticResourceConfigService staticResourceConfigService() {
        return new StaticResourceConfigService();
    }
}

/**
 * DevTools Properties Config
 * ConfigurationProperties to capture template and resource settings
 */
@ConfigurationProperties(prefix = "spring")
@Validated
class DevToolsPropertiesConfig {
    
    private Thymeleaf thymeleaf = new Thymeleaf();
    private Freemarker freemarker = new Freemarker();
    private H2 h2 = new H2();
    private Web web = new Web();

    public static class Thymeleaf {
        private boolean cache = true;
        public boolean isCache() { return cache; }
        public void setCache(boolean cache) { this.cache = cache; }
    }

    public static class Freemarker {
        private boolean cache = true;
        public boolean isCache() { return cache; }
        public void setCache(boolean cache) { this.cache = cache; }
    }

    public static class H2 {
        private Console console = new Console();
        public static class Console {
            private boolean enabled = false;
            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
        }
        public Console getConsole() { return console; }
    }

    public static class Web {
        private Resources resources = new Resources();
        public static class Resources {
            private Chain chain = new Chain();
            public static class Chain {
                private boolean cache = true;
                public boolean isCache() { return cache; }
                public void setCache(boolean cache) { this.cache = cache; }
            }
            public Chain getChain() { return chain; }
        }
        public Resources getResources() { return resources; }
    }

    public Thymeleaf getThymeleaf() { return thymeleaf; }
    public Freemarker getFreemarker() { return freemarker; }
    public H2 getH2() { return h2; }
    public Web getWeb() { return web; }
}

/**
 * Property Defaults Service
 * Provides information about DevTools property defaults
 */
@Service
class PropertyDefaultsService {

    /**
     * Get all DevTools default properties
     */
    public Map<String, Object> getDevToolsDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        
        // Template Engine Defaults
        Map<String, String> templates = new LinkedHashMap<>();
        templates.put("spring.thymeleaf.cache", "false");
        templates.put("spring.freemarker.cache", "false");
        templates.put("spring.groovy.template.cache", "false");
        templates.put("spring.mustache.cache", "false");
        defaults.put("templateEngines", templates);
        
        // Static Resources Defaults
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("spring.web.resources.cache.cachecontrol.no-cache", "true");
        resources.put("spring.web.resources.chain.cache", "false");
        defaults.put("staticResources", resources);
        
        // H2 Console Defaults
        Map<String, String> h2 = new LinkedHashMap<>();
        h2.put("spring.h2.console.enabled", "true");
        defaults.put("h2Console", h2);
        
        // Error Handling Defaults
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("server.error.include-stacktrace", "always");
        errors.put("server.error.include-message", "always");
        errors.put("server.error.include-binding-errors", "always");
        defaults.put("errorHandling", errors);
        
        // Reactor Defaults
        Map<String, String> reactor = new LinkedHashMap<>();
        reactor.put("spring.reactor.debug", "true");
        defaults.put("reactor", reactor);
        
        return defaults;
    }

    /**
     * Get benefits of DevTools defaults
     */
    public List<String> getDevToolsDefaultsBenefits() {
        List<String> benefits = new ArrayList<>();
        benefits.add("✅ Template changes visible immediately without restart");
        benefits.add("✅ Static resources (CSS/JS) reload automatically");
        benefits.add("✅ Stack traces shown in error pages for quick debugging");
        benefits.add("✅ H2 console available at /h2-console");
        benefits.add("✅ No manual configuration required");
        benefits.add("✅ Production unaffected (DevTools auto-disabled)");
        benefits.add("✅ LiveReload enabled for browser auto-refresh");
        benefits.add("✅ Reactor debug mode for better reactive stack traces");
        return benefits;
    }

    /**
     * Get property override examples
     */
    public Map<String, String> getOverrideExamples() {
        Map<String, String> examples = new LinkedHashMap<>();
        
        examples.put("Enable Thymeleaf cache", 
            "spring.thymeleaf.cache=true");
        
        examples.put("Disable H2 console", 
            "spring.h2.console.enabled=false");
        
        examples.put("Minimal stack traces", 
            "server.error.include-stacktrace=on_param");
        
        examples.put("Enable static resource caching", 
            "spring.web.resources.chain.cache=true");
        
        return examples;
    }

    /**
     * Check if property is a DevTools default
     */
    public boolean isDevToolsDefault(String propertyName) {
        Set<String> devToolsProperties = Set.of(
            "spring.thymeleaf.cache",
            "spring.freemarker.cache",
            "spring.groovy.template.cache",
            "spring.mustache.cache",
            "spring.web.resources.cache.cachecontrol.no-cache",
            "spring.web.resources.chain.cache",
            "spring.h2.console.enabled",
            "server.error.include-stacktrace",
            "server.error.include-message",
            "server.error.include-binding-errors",
            "spring.reactor.debug"
        );
        
        return devToolsProperties.contains(propertyName);
    }
}

/**
 * Template Engine Configuration Service
 * Shows template engine caching status
 */
@Service
class TemplateEngineConfigService {

    private final DevToolsPropertiesConfig config;

    public TemplateEngineConfigService() {
        this.config = new DevToolsPropertiesConfig();
    }

    /**
     * Get template engine configurations
     */
    public Map<String, Object> getTemplateEngineConfigs() {
        Map<String, Object> configs = new ConcurrentHashMap<>();
        
        configs.put("thymeleaf", Map.of(
            "cache", config.getThymeleaf().isCache(),
            "devToolsDefault", false,
            "benefit", "Template changes visible immediately"
        ));
        
        configs.put("freemarker", Map.of(
            "cache", config.getFreemarker().isCache(),
            "devToolsDefault", false,
            "benefit", "FreeMarker templates reload automatically"
        ));
        
        configs.put("note", "DevTools automatically disables caching for faster development");
        
        return configs;
    }

    /**
     * Simulate template change detection
     */
    public String simulateTemplateChange(String templateName) {
        boolean cacheEnabled = config.getThymeleaf().isCache();
        
        if (cacheEnabled) {
            return String.format("⚠️ Template '%s' changed, but cache is ENABLED. " +
                "Restart required to see changes.", templateName);
        } else {
            return String.format("✅ Template '%s' changed and cache is DISABLED. " +
                "Changes visible immediately (DevTools default).", templateName);
        }
    }
}

/**
 * Static Resource Configuration Service
 * Shows static resource caching status
 */
@Service
class StaticResourceConfigService {

    private final DevToolsPropertiesConfig config;

    public StaticResourceConfigService() {
        this.config = new DevToolsPropertiesConfig();
    }

    /**
     * Get static resource configurations
     */
    public Map<String, Object> getStaticResourceConfigs() {
        Map<String, Object> configs = new ConcurrentHashMap<>();
        
        configs.put("chainCache", Map.of(
            "enabled", config.getWeb().getResources().getChain().isCache(),
            "devToolsDefault", false,
            "benefit", "CSS/JS changes reflected immediately"
        ));
        
        configs.put("cacheControl", Map.of(
            "noCache", "true (DevTools default)",
            "benefit", "Browser doesn't cache resources during development"
        ));
        
        configs.put("note", "DevTools disables static resource caching for live updates");
        
        return configs;
    }

    /**
     * Simulate static resource change detection
     */
    public String simulateResourceChange(String resourceName) {
        boolean cacheEnabled = config.getWeb().getResources().getChain().isCache();
        
        if (cacheEnabled) {
            return String.format("⚠️ Resource '%s' changed, but cache is ENABLED. " +
                "Browser hard refresh (Ctrl+F5) required.", resourceName);
        } else {
            return String.format("✅ Resource '%s' changed and cache is DISABLED. " +
                "Browser auto-refreshes with LiveReload (DevTools default).", resourceName);
        }
    }
}

/**
 * H2 Console Configuration Service
 */
@Service
class H2ConsoleConfigService {

    private final DevToolsPropertiesConfig config;

    public H2ConsoleConfigService() {
        this.config = new DevToolsPropertiesConfig();
    }

    /**
     * Get H2 console configuration
     */
    public Map<String, Object> getH2ConsoleConfig() {
        Map<String, Object> h2Config = new ConcurrentHashMap<>();
        
        h2Config.put("enabled", config.getH2().getConsole().isEnabled());
        h2Config.put("devToolsDefault", true);
        h2Config.put("url", "/h2-console");
        h2Config.put("benefit", "Instant database inspection without external tools");
        h2Config.put("note", "DevTools automatically enables H2 console in development");
        
        return h2Config;
    }

    /**
     * Get H2 console access instructions
     */
    public Map<String, String> getH2ConsoleInstructions() {
        Map<String, String> instructions = new LinkedHashMap<>();
        
        instructions.put("step1", "Open browser to http://localhost:8080/h2-console");
        instructions.put("step2", "JDBC URL: jdbc:h2:mem:testdb");
        instructions.put("step3", "Username: sa");
        instructions.put("step4", "Password: (leave empty)");
        instructions.put("step5", "Click 'Connect'");
        
        return instructions;
    }
}

/**
 * Property Defaults REST Controller
 */
@RestController
@RequestMapping("/api/property-defaults")
class PropertyDefaultsController {

    private final PropertyDefaultsService propertyDefaultsService;
    private final TemplateEngineConfigService templateEngineConfigService;
    private final StaticResourceConfigService staticResourceConfigService;
    private final H2ConsoleConfigService h2ConsoleConfigService;

    public PropertyDefaultsController(PropertyDefaultsService propertyDefaultsService,
                                      TemplateEngineConfigService templateEngineConfigService,
                                      StaticResourceConfigService staticResourceConfigService,
                                      H2ConsoleConfigService h2ConsoleConfigService) {
        this.propertyDefaultsService = propertyDefaultsService;
        this.templateEngineConfigService = templateEngineConfigService;
        this.staticResourceConfigService = staticResourceConfigService;
        this.h2ConsoleConfigService = h2ConsoleConfigService;
    }

    /**
     * GET /api/property-defaults/devtools-defaults
     * Get all DevTools default properties
     */
    @GetMapping("/devtools-defaults")
    public Map<String, Object> getDevToolsDefaults() {
        return propertyDefaultsService.getDevToolsDefaults();
    }

    /**
     * GET /api/property-defaults/benefits
     * Get benefits of DevTools defaults
     */
    @GetMapping("/benefits")
    public List<String> getBenefits() {
        return propertyDefaultsService.getDevToolsDefaultsBenefits();
    }

    /**
     * GET /api/property-defaults/override-examples
     * Get property override examples
     */
    @GetMapping("/override-examples")
    public Map<String, String> getOverrideExamples() {
        return propertyDefaultsService.getOverrideExamples();
    }

    /**
     * GET /api/property-defaults/check/{propertyName}
     * Check if property is a DevTools default
     */
    @GetMapping("/check/{propertyName}")
    public Map<String, Boolean> checkDevToolsDefault(@PathVariable String propertyName) {
        Map<String, Boolean> result = new ConcurrentHashMap<>();
        result.put("isDevToolsDefault", propertyDefaultsService.isDevToolsDefault(propertyName));
        return result;
    }

    /**
     * GET /api/property-defaults/template-engines
     * Get template engine configurations
     */
    @GetMapping("/template-engines")
    public Map<String, Object> getTemplateEngineConfigs() {
        return templateEngineConfigService.getTemplateEngineConfigs();
    }

    /**
     * POST /api/property-defaults/simulate-template-change
     * Simulate template change
     */
    @PostMapping("/simulate-template-change")
    public String simulateTemplateChange(@RequestParam String templateName) {
        return templateEngineConfigService.simulateTemplateChange(templateName);
    }

    /**
     * GET /api/property-defaults/static-resources
     * Get static resource configurations
     */
    @GetMapping("/static-resources")
    public Map<String, Object> getStaticResourceConfigs() {
        return staticResourceConfigService.getStaticResourceConfigs();
    }

    /**
     * POST /api/property-defaults/simulate-resource-change
     * Simulate static resource change
     */
    @PostMapping("/simulate-resource-change")
    public String simulateResourceChange(@RequestParam String resourceName) {
        return staticResourceConfigService.simulateResourceChange(resourceName);
    }

    /**
     * GET /api/property-defaults/h2-console
     * Get H2 console configuration
     */
    @GetMapping("/h2-console")
    public Map<String, Object> getH2ConsoleConfig() {
        return h2ConsoleConfigService.getH2ConsoleConfig();
    }

    /**
     * GET /api/property-defaults/h2-console/instructions
     * Get H2 console access instructions
     */
    @GetMapping("/h2-console/instructions")
    public Map<String, String> getH2ConsoleInstructions() {
        return h2ConsoleConfigService.getH2ConsoleInstructions();
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ ADD DEVTOOLS DEPENDENCY:
 * ----------------------------
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-devtools</artifactId>
 *     <optional>true</optional>
 *     <scope>runtime</scope>
 * </dependency>
 * 
 * DevTools automatically applies property defaults!
 * 
 * 2️⃣ THYMELEAF TEMPLATE DEVELOPMENT:
 * -----------------------------------
 * WITHOUT DevTools:
 * - Edit src/main/resources/templates/home.html
 * - Restart application
 * - Refresh browser
 * 
 * WITH DevTools:
 * - Edit src/main/resources/templates/home.html
 * - Just refresh browser (no restart!)
 * - spring.thymeleaf.cache=false automatically
 * 
 * 3️⃣ STATIC RESOURCES (CSS/JS):
 * ------------------------------
 * WITHOUT DevTools:
 * - Edit src/main/resources/static/css/style.css
 * - Hard refresh browser (Ctrl+F5)
 * 
 * WITH DevTools + LiveReload:
 * - Edit src/main/resources/static/css/style.css
 * - Browser auto-refreshes!
 * - Cache-Control: no-cache automatically
 * 
 * 4️⃣ H2 CONSOLE ACCESS:
 * ----------------------
 * WITH DevTools:
 * 1. Open http://localhost:8080/h2-console
 * 2. JDBC URL: jdbc:h2:mem:testdb
 * 3. Click Connect
 * # spring.h2.console.enabled=true automatically!
 * 
 * WITHOUT DevTools:
 * # Must enable manually in application.yml:
 * spring:
 *   h2:
 *     console:
 *       enabled: true
 * 
 * 5️⃣ ERROR PAGES WITH STACK TRACES:
 * ----------------------------------
 * WITH DevTools:
 * - Trigger error in controller
 * - Full stack trace shown automatically
 * - server.error.include-stacktrace=always
 * 
 * WITHOUT DevTools:
 * - Generic error page
 * - Must check logs for stack trace
 * 
 * 6️⃣ OVERRIDE DEVTOOLS DEFAULTS:
 * -------------------------------
 * # application-dev.yml
 * spring:
 *   thymeleaf:
 *     cache: true       # Override: Enable cache even with DevTools
 *   h2:
 *     console:
 *       enabled: false  # Override: Disable H2 console
 * 
 * 7️⃣ GET DEVTOOLS DEFAULTS INFO:
 * -------------------------------
 * curl http://localhost:8080/api/property-defaults/devtools-defaults
 * curl http://localhost:8080/api/property-defaults/benefits
 * curl http://localhost:8080/api/property-defaults/template-engines
 * curl http://localhost:8080/api/property-defaults/h2-console
 * 
 * 8️⃣ SIMULATE TEMPLATE CHANGE:
 * -----------------------------
 * curl -X POST "http://localhost:8080/api/property-defaults/simulate-template-change?templateName=home.html"
 * 
 * 9️⃣ CHECK IF PROPERTY IS DEVTOOLS DEFAULT:
 * ------------------------------------------
 * curl http://localhost:8080/api/property-defaults/check/spring.thymeleaf.cache
 * # Response: {"isDevToolsDefault": true}
 * 
 * 🔟 PROFILE-SPECIFIC CONFIGURATION:
 * -----------------------------------
 * # application-dev.yml (DevTools defaults apply)
 * spring:
 *   profiles:
 *     active: dev
 * 
 * # application-prod.yml (DevTools not present, defaults don't apply)
 * spring:
 *   thymeleaf:
 *     cache: true       # Must enable caching explicitly
 */
