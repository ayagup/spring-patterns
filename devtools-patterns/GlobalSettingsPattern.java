package com.example.devtools.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🌍 SPRING BOOT DEVTOOLS - GLOBAL SETTINGS PATTERN 🌍
 * =====================================================
 * 
 * Demonstrates Spring Boot DevTools global settings configuration via
 * ~/.spring-boot-devtools.properties file. Global settings apply to ALL
 * Spring Boot projects on your machine, providing consistent development
 * environment configuration across projects.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ GLOBAL CONFIGURATION FILE:
 *    - Location: ~/.spring-boot-devtools.properties
 *    - Location (Windows): %USERPROFILE%\.spring-boot-devtools.properties
 *    - Applies to ALL Spring Boot projects
 *    - Lower priority than project-specific config
 * 
 * 2️⃣ COMMON GLOBAL SETTINGS:
 *    - Restart excludes (node_modules, .git, etc.)
 *    - LiveReload port
 *    - Restart trigger file
 *    - Additional paths
 *    - Remote secret
 * 
 * 3️⃣ PRIORITY ORDER:
 *    1. Command-line arguments (highest)
 *    2. application.properties/yml in project
 *    3. application-{profile}.properties
 *    4. ~/.spring-boot-devtools.properties (lowest)
 * 
 * 4️⃣ USE CASES:
 *    - Company-wide defaults
 *    - Personal preferences
 *    - Shared team settings
 *    - IDE-agnostic configuration
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
 * 📄 GLOBAL SETTINGS FILE:
 * ========================
 * 
 * ~/.spring-boot-devtools.properties (Linux/macOS):
 * --------------------------------------------------
 * # Restart Configuration
 * spring.devtools.restart.exclude=static/**,public/**,node_modules/**,target/**
 * spring.devtools.restart.additional-exclude=**/logs/**
 * spring.devtools.restart.trigger-file=.trigger
 * spring.devtools.restart.poll-interval=1s
 * spring.devtools.restart.quiet-period=400ms
 * spring.devtools.restart.log-condition-evaluation-delta=true
 * 
 * # LiveReload Configuration
 * spring.devtools.livereload.enabled=true
 * spring.devtools.livereload.port=35729
 * 
 * # Remote Configuration
 * spring.devtools.remote.secret=my-global-secret
 * spring.devtools.remote.context-path=/.~~spring-boot!~
 * 
 * # Additional Paths (added to all projects)
 * spring.devtools.restart.additional-paths=../other-project/src/main/java
 * 
 * %USERPROFILE%\.spring-boot-devtools.properties (Windows):
 * ----------------------------------------------------------
 * Same properties as above
 * 
 * 🔧 COMMON GLOBAL SETTINGS:
 * ==========================
 * 
 * 1️⃣ RESTART EXCLUDES:
 * ---------------------
 * # Exclude build outputs
 * spring.devtools.restart.exclude=target/**,build/**
 * 
 * # Exclude dependency directories
 * spring.devtools.restart.additional-exclude=node_modules/**,.npm/**
 * 
 * # Exclude version control
 * spring.devtools.restart.additional-exclude=.git/**,.svn/**
 * 
 * # Exclude IDE directories
 * spring.devtools.restart.additional-exclude=.idea/**,.vscode/**,.settings/**
 * 
 * 2️⃣ LIVERELOAD SETTINGS:
 * ------------------------
 * # Use non-standard port (if 35729 conflicts)
 * spring.devtools.livereload.port=35730
 * 
 * # Disable LiveReload globally
 * spring.devtools.livereload.enabled=false
 * 
 * 3️⃣ RESTART TRIGGER FILE:
 * -------------------------
 * # Use .trigger file instead of auto-restart
 * spring.devtools.restart.trigger-file=.trigger
 * 
 * # Allows manual control: touch .trigger to restart
 * 
 * 4️⃣ RESTART TIMING:
 * -------------------
 * # Faster polling (more responsive)
 * spring.devtools.restart.poll-interval=500ms
 * 
 * # Longer quiet period (fewer restarts)
 * spring.devtools.restart.quiet-period=1s
 * 
 * 5️⃣ REMOTE DEBUGGING:
 * ---------------------
 * # Global remote secret (use strong password!)
 * spring.devtools.remote.secret=my-secure-global-secret-123
 * 
 * # Custom remote context path
 * spring.devtools.remote.context-path=/.~~devtools!~
 * 
 * 💡 BENEFITS:
 * ===========
 * ✅ Single configuration for all projects
 * ✅ No per-project configuration needed
 * ✅ Team consistency (share same file)
 * ✅ Personal customization without affecting project
 * ✅ IDE-independent settings
 * ✅ Version control friendly (not in project repo)
 * 
 * ⚠️ IMPORTANT NOTES:
 * ==================
 * - Global settings have LOWEST priority
 * - Project-specific settings override global settings
 * - File must be in user home directory
 * - Changes require application restart
 * - Not version controlled (user-specific)
 * - Security: Don't use weak remote secrets
 * 
 * 🎯 RECOMMENDED GLOBAL SETTINGS:
 * ===============================
 * 
 * ~/.spring-boot-devtools.properties:
 * 
 * # Comprehensive exclude patterns
 * spring.devtools.restart.exclude=static/**,public/**,templates/**,META-INF/maven/**,META-INF/resources/**,resources/**
 * spring.devtools.restart.additional-exclude=target/**,build/**,node_modules/**,.git/**,.idea/**,.vscode/**,.settings/**,logs/**
 * 
 * # Sensible restart timing
 * spring.devtools.restart.poll-interval=1s
 * spring.devtools.restart.quiet-period=400ms
 * spring.devtools.restart.log-condition-evaluation-delta=false
 * 
 * # LiveReload enabled
 * spring.devtools.livereload.enabled=true
 * spring.devtools.livereload.port=35729
 * 
 * # Trigger file for manual restart
 * spring.devtools.restart.trigger-file=.trigger
 * 
 * 💡 WHEN TO USE:
 * ==============
 * ✅ Multiple Spring Boot projects
 * ✅ Team-wide standard settings
 * ✅ Personal development preferences
 * ✅ Avoiding per-project configuration duplication
 * ✅ Company-wide DevTools policies
 * 
 * ❌ WHEN NOT TO USE:
 * ==================
 * ❌ Project-specific requirements (use application.yml)
 * ❌ Sensitive secrets (use project-specific secure config)
 * ❌ Version-controlled settings (use project config)
 * ❌ Production settings (DevTools not in production)
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
@EnableConfigurationProperties(GlobalDevToolsConfig.class)
public class GlobalSettingsPattern {

    public static void main(String[] args) {
        SpringApplication.run(GlobalSettingsPattern.class, args);
    }
}

/**
 * Global DevTools Configuration
 */
@Configuration
class GlobalSettingsConfiguration {

    @Bean
    public GlobalSettingsService globalSettingsService(Environment environment) {
        return new GlobalSettingsService(environment);
    }

    @Bean
    public GlobalSettingsFileService globalSettingsFileService() {
        return new GlobalSettingsFileService();
    }
}

/**
 * Global DevTools Properties Config
 */
@ConfigurationProperties(prefix = "spring.devtools")
@Validated
class GlobalDevToolsConfig {
    
    private Restart restart = new Restart();
    private Livereload livereload = new Livereload();
    private Remote remote = new Remote();

    public static class Restart {
        private boolean enabled = true;
        private String exclude = "";
        private String additionalExclude = "";
        private String triggerFile = "";
        private String pollInterval = "1s";
        private String quietPeriod = "400ms";
        private String additionalPaths = "";
        
        // Getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getExclude() { return exclude; }
        public void setExclude(String exclude) { this.exclude = exclude; }
        public String getAdditionalExclude() { return additionalExclude; }
        public void setAdditionalExclude(String additionalExclude) { this.additionalExclude = additionalExclude; }
        public String getTriggerFile() { return triggerFile; }
        public void setTriggerFile(String triggerFile) { this.triggerFile = triggerFile; }
        public String getPollInterval() { return pollInterval; }
        public void setPollInterval(String pollInterval) { this.pollInterval = pollInterval; }
        public String getQuietPeriod() { return quietPeriod; }
        public void setQuietPeriod(String quietPeriod) { this.quietPeriod = quietPeriod; }
        public String getAdditionalPaths() { return additionalPaths; }
        public void setAdditionalPaths(String additionalPaths) { this.additionalPaths = additionalPaths; }
    }

    public static class Livereload {
        private boolean enabled = true;
        private int port = 35729;
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
    }

    public static class Remote {
        private String secret = "";
        private String contextPath = "/.~~spring-boot!~";
        
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getContextPath() { return contextPath; }
        public void setContextPath(String contextPath) { this.contextPath = contextPath; }
    }

    public Restart getRestart() { return restart; }
    public Livereload getLivereload() { return livereload; }
    public Remote getRemote() { return remote; }
}

/**
 * Global Settings Service
 */
@Service
class GlobalSettingsService {

    private final Environment environment;

    public GlobalSettingsService(Environment environment) {
        this.environment = environment;
    }

    /**
     * Get all DevTools settings (from all sources)
     */
    public Map<String, Object> getAllDevToolsSettings() {
        Map<String, Object> settings = new LinkedHashMap<>();
        
        // Restart settings
        Map<String, String> restart = new LinkedHashMap<>();
        restart.put("enabled", getProperty("spring.devtools.restart.enabled", "true"));
        restart.put("exclude", getProperty("spring.devtools.restart.exclude", ""));
        restart.put("additional-exclude", getProperty("spring.devtools.restart.additional-exclude", ""));
        restart.put("trigger-file", getProperty("spring.devtools.restart.trigger-file", ""));
        restart.put("poll-interval", getProperty("spring.devtools.restart.poll-interval", "1s"));
        restart.put("quiet-period", getProperty("spring.devtools.restart.quiet-period", "400ms"));
        restart.put("additional-paths", getProperty("spring.devtools.restart.additional-paths", ""));
        settings.put("restart", restart);
        
        // LiveReload settings
        Map<String, String> livereload = new LinkedHashMap<>();
        livereload.put("enabled", getProperty("spring.devtools.livereload.enabled", "true"));
        livereload.put("port", getProperty("spring.devtools.livereload.port", "35729"));
        settings.put("livereload", livereload);
        
        // Remote settings
        Map<String, String> remote = new LinkedHashMap<>();
        remote.put("secret", "****** (hidden)");
        remote.put("context-path", getProperty("spring.devtools.remote.context-path", "/.~~spring-boot!~"));
        settings.put("remote", remote);
        
        return settings;
    }

    /**
     * Get property source information
     */
    public Map<String, String> getPropertySources() {
        Map<String, String> sources = new LinkedHashMap<>();
        sources.put("1_Highest", "Command-line arguments");
        sources.put("2_High", "application.properties/yml (project)");
        sources.put("3_Medium", "application-{profile}.properties");
        sources.put("4_Lowest", "~/.spring-boot-devtools.properties (GLOBAL)");
        return sources;
    }

    /**
     * Get recommended global settings
     */
    public Map<String, String> getRecommendedGlobalSettings() {
        Map<String, String> recommendations = new LinkedHashMap<>();
        
        recommendations.put("# Restart Excludes", "");
        recommendations.put("spring.devtools.restart.exclude", "static/**,public/**,templates/**,META-INF/**");
        recommendations.put("spring.devtools.restart.additional-exclude", "target/**,build/**,node_modules/**,.git/**");
        
        recommendations.put("# Restart Timing", "");
        recommendations.put("spring.devtools.restart.poll-interval", "1s");
        recommendations.put("spring.devtools.restart.quiet-period", "400ms");
        
        recommendations.put("# LiveReload", "");
        recommendations.put("spring.devtools.livereload.enabled", "true");
        recommendations.put("spring.devtools.livereload.port", "35729");
        
        recommendations.put("# Manual Restart Trigger", "");
        recommendations.put("spring.devtools.restart.trigger-file", ".trigger");
        
        return recommendations;
    }

    /**
     * Check if global settings file exists
     */
    public boolean globalSettingsFileExists() {
        Path globalSettingsPath = Paths.get(System.getProperty("user.home"), ".spring-boot-devtools.properties");
        return globalSettingsPath.toFile().exists();
    }

    /**
     * Get global settings file path
     */
    public String getGlobalSettingsFilePath() {
        return Paths.get(System.getProperty("user.home"), ".spring-boot-devtools.properties").toString();
    }

    private String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }
}

/**
 * Global Settings File Service
 * Manages global settings file
 */
@Service
class GlobalSettingsFileService {

    private final Path globalSettingsPath;

    public GlobalSettingsFileService() {
        this.globalSettingsPath = Paths.get(System.getProperty("user.home"), ".spring-boot-devtools.properties");
    }

    /**
     * Get global settings file path
     */
    public Path getGlobalSettingsPath() {
        return globalSettingsPath;
    }

    /**
     * Check if global settings file exists
     */
    public boolean exists() {
        return globalSettingsPath.toFile().exists();
    }

    /**
     * Get file info
     */
    public Map<String, Object> getFileInfo() {
        Map<String, Object> info = new ConcurrentHashMap<>();
        
        File file = globalSettingsPath.toFile();
        info.put("path", globalSettingsPath.toString());
        info.put("exists", file.exists());
        
        if (file.exists()) {
            info.put("size", file.length() + " bytes");
            info.put("lastModified", new Date(file.lastModified()));
            info.put("readable", file.canRead());
            info.put("writable", file.canWrite());
        }
        
        return info;
    }

    /**
     * Get creation instructions
     */
    public Map<String, Object> getCreationInstructions() {
        Map<String, Object> instructions = new ConcurrentHashMap<>();
        
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            instructions.put("platform", "Windows");
            instructions.put("path", "%USERPROFILE%\\.spring-boot-devtools.properties");
            instructions.put("command", "notepad %USERPROFILE%\\.spring-boot-devtools.properties");
        } else if (os.contains("mac")) {
            instructions.put("platform", "macOS");
            instructions.put("path", "~/.spring-boot-devtools.properties");
            instructions.put("command", "nano ~/.spring-boot-devtools.properties");
        } else {
            instructions.put("platform", "Linux/Unix");
            instructions.put("path", "~/.spring-boot-devtools.properties");
            instructions.put("command", "nano ~/.spring-boot-devtools.properties");
        }
        
        instructions.put("note", "Create this file and add your global DevTools settings");
        
        return instructions;
    }

    /**
     * Get example content
     */
    public String getExampleContent() {
        return """
                # Spring Boot DevTools Global Settings
                # Location: ~/.spring-boot-devtools.properties (Linux/macOS)
                # Location: %USERPROFILE%\\.spring-boot-devtools.properties (Windows)
                
                # Restart Configuration
                spring.devtools.restart.exclude=static/**,public/**,templates/**,META-INF/**
                spring.devtools.restart.additional-exclude=target/**,build/**,node_modules/**,.git/**,.idea/**
                spring.devtools.restart.trigger-file=.trigger
                spring.devtools.restart.poll-interval=1s
                spring.devtools.restart.quiet-period=400ms
                spring.devtools.restart.log-condition-evaluation-delta=false
                
                # LiveReload Configuration
                spring.devtools.livereload.enabled=true
                spring.devtools.livereload.port=35729
                
                # Remote Configuration (Optional)
                # spring.devtools.remote.secret=my-secure-secret-123
                # spring.devtools.remote.context-path=/.~~spring-boot!~
                
                # Additional Paths (Optional)
                # spring.devtools.restart.additional-paths=../other-project/src/main/java
                """;
    }
}

/**
 * Global Settings REST Controller
 */
@RestController
@RequestMapping("/api/global-settings")
class GlobalSettingsController {

    private final GlobalSettingsService globalSettingsService;
    private final GlobalSettingsFileService globalSettingsFileService;
    private final Environment environment;

    public GlobalSettingsController(GlobalSettingsService globalSettingsService,
                                    GlobalSettingsFileService globalSettingsFileService,
                                    Environment environment) {
        this.globalSettingsService = globalSettingsService;
        this.globalSettingsFileService = globalSettingsFileService;
        this.environment = environment;
    }

    /**
     * GET /api/global-settings/all
     * Get all DevTools settings
     */
    @GetMapping("/all")
    public Map<String, Object> getAllSettings() {
        return globalSettingsService.getAllDevToolsSettings();
    }

    /**
     * GET /api/global-settings/property-sources
     * Get property source priority order
     */
    @GetMapping("/property-sources")
    public Map<String, String> getPropertySources() {
        return globalSettingsService.getPropertySources();
    }

    /**
     * GET /api/global-settings/recommended
     * Get recommended global settings
     */
    @GetMapping("/recommended")
    public Map<String, String> getRecommended() {
        return globalSettingsService.getRecommendedGlobalSettings();
    }

    /**
     * GET /api/global-settings/file-path
     * Get global settings file path
     */
    @GetMapping("/file-path")
    public Map<String, String> getFilePath() {
        Map<String, String> response = new ConcurrentHashMap<>();
        response.put("path", globalSettingsService.getGlobalSettingsFilePath());
        response.put("exists", String.valueOf(globalSettingsService.globalSettingsFileExists()));
        return response;
    }

    /**
     * GET /api/global-settings/file-info
     * Get global settings file information
     */
    @GetMapping("/file-info")
    public Map<String, Object> getFileInfo() {
        return globalSettingsFileService.getFileInfo();
    }

    /**
     * GET /api/global-settings/creation-instructions
     * Get instructions for creating global settings file
     */
    @GetMapping("/creation-instructions")
    public Map<String, Object> getCreationInstructions() {
        return globalSettingsFileService.getCreationInstructions();
    }

    /**
     * GET /api/global-settings/example
     * Get example global settings file content
     */
    @GetMapping("/example")
    public String getExampleContent() {
        return globalSettingsFileService.getExampleContent();
    }

    /**
     * GET /api/global-settings/property/{propertyName}
     * Get specific property value
     */
    @GetMapping("/property/{propertyName}")
    public Map<String, String> getProperty(@PathVariable String propertyName) {
        Map<String, String> response = new ConcurrentHashMap<>();
        response.put("property", propertyName);
        response.put("value", environment.getProperty(propertyName, "Not set"));
        return response;
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ CREATE GLOBAL SETTINGS FILE:
 * --------------------------------
 * # Linux/macOS
 * nano ~/.spring-boot-devtools.properties
 * 
 * # Windows
 * notepad %USERPROFILE%\.spring-boot-devtools.properties
 * 
 * 2️⃣ RECOMMENDED CONTENT:
 * ------------------------
 * # ~/.spring-boot-devtools.properties
 * 
 * # Comprehensive excludes
 * spring.devtools.restart.exclude=static/**,public/**,templates/**
 * spring.devtools.restart.additional-exclude=target/**,node_modules/**,.git/**
 * 
 * # Sensible timing
 * spring.devtools.restart.poll-interval=1s
 * spring.devtools.restart.quiet-period=400ms
 * 
 * # LiveReload
 * spring.devtools.livereload.enabled=true
 * spring.devtools.livereload.port=35729
 * 
 * # Trigger file
 * spring.devtools.restart.trigger-file=.trigger
 * 
 * 3️⃣ GET CURRENT SETTINGS:
 * -------------------------
 * curl http://localhost:8080/api/global-settings/all
 * curl http://localhost:8080/api/global-settings/recommended
 * curl http://localhost:8080/api/global-settings/file-path
 * 
 * 4️⃣ GET SPECIFIC PROPERTY:
 * --------------------------
 * curl http://localhost:8080/api/global-settings/property/spring.devtools.restart.exclude
 * 
 * 5️⃣ OVERRIDE IN PROJECT:
 * ------------------------
 * # application.yml (overrides global settings)
 * spring:
 *   devtools:
 *     restart:
 *       exclude: custom/**
 *       poll-interval: 2s
 * 
 * 6️⃣ TEAM SHARED SETTINGS:
 * -------------------------
 * # Share .spring-boot-devtools.properties with team
 * # Each developer copies to their home directory
 * # Ensures consistent DevTools behavior
 * 
 * 7️⃣ PROJECT-SPECIFIC VS GLOBAL:
 * -------------------------------
 * Global (~/.spring-boot-devtools.properties):
 * - Common excludes
 * - Personal preferences
 * - IDE-agnostic settings
 * 
 * Project (application.yml):
 * - Project-specific requirements
 * - Version-controlled settings
 * - Environment-specific config
 */
