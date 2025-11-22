package com.example.miscellaneous.themeresolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.theme.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Theme Resolver Pattern - Demonstrates Spring's ThemeResolver for UI theming
 * 
 * This pattern shows how to:
 * 1. Configure ThemeSource and ThemeResolver
 * 2. Use CookieThemeResolver
 * 3. Use SessionThemeResolver
 * 4. Use FixedThemeResolver
 * 5. Implement custom theme resolution
 * 6. Use ThemeChangeInterceptor
 * 7. Switch themes dynamically
 * 8. Load theme resources
 * 9. Apply theme-specific styles
 * 10. Handle theme preferences
 * 
 * Key Concepts:
 * - ThemeSource: Manages theme resources
 * - ThemeResolver: Strategy for determining current theme
 * - Theme: Collection of UI resources (CSS, images, etc.)
 * - ThemeChangeInterceptor: Detects theme change requests
 * 
 * Dependencies:
 * - spring-webmvc
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class ThemeResolverPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(ThemeResolverPattern.class, args);
    }
}

// ============================================================================
// Theme Resolvers
// ============================================================================

/**
 * Custom ThemeResolver implementation
 */
@Component
class CustomThemeResolver implements ThemeResolver {
    
    private static final String THEME_SESSION_ATTR = "user.theme";
    private static final String DEFAULT_THEME = "light";
    
    @Override
    public String resolveThemeName(HttpServletRequest request) {
        // Check query parameter
        String themeParam = request.getParameter("theme");
        if (themeParam != null) {
            return themeParam;
        }
        
        // Check session
        String sessionTheme = (String) request.getSession().getAttribute(THEME_SESSION_ATTR);
        if (sessionTheme != null) {
            return sessionTheme;
        }
        
        // Check cookie or header
        String cookieTheme = extractThemeFromCookie(request);
        if (cookieTheme != null) {
            return cookieTheme;
        }
        
        return DEFAULT_THEME;
    }
    
    @Override
    public void setThemeName(HttpServletRequest request, @Nullable HttpServletResponse response, 
                            @Nullable String themeName) {
        if (themeName != null) {
            request.getSession().setAttribute(THEME_SESSION_ATTR, themeName);
        } else {
            request.getSession().removeAttribute(THEME_SESSION_ATTR);
        }
    }
    
    private String extractThemeFromCookie(HttpServletRequest request) {
        // Implementation would extract theme from cookie
        return null;
    }
}

// ============================================================================
// Theme Service
// ============================================================================

/**
 * Service for theme operations
 */
@org.springframework.stereotype.Service
class ThemeService {
    
    private final ThemeResolver themeResolver;
    private final Map<String, Theme> themes = new HashMap<>();
    
    public ThemeService(ThemeResolver themeResolver) {
        this.themeResolver = themeResolver;
        initializeThemes();
    }
    
    private void initializeThemes() {
        themes.put("light", new Theme("light", "Light Theme", "#FFFFFF", "#000000"));
        themes.put("dark", new Theme("dark", "Dark Theme", "#2C3E50", "#ECF0F1"));
        themes.put("blue", new Theme("blue", "Blue Theme", "#3498DB", "#FFFFFF"));
        themes.put("green", new Theme("green", "Green Theme", "#27AE60", "#FFFFFF"));
        themes.put("high-contrast", new Theme("high-contrast", "High Contrast", "#000000", "#FFFF00"));
    }
    
    public String getCurrentTheme(HttpServletRequest request) {
        return themeResolver.resolveThemeName(request);
    }
    
    public void changeTheme(HttpServletRequest request, HttpServletResponse response, 
                          String themeName) {
        if (themes.containsKey(themeName)) {
            themeResolver.setThemeName(request, response, themeName);
        }
    }
    
    public List<Theme> getAvailableThemes() {
        return new ArrayList<>(themes.values());
    }
    
    public Theme getTheme(String themeName) {
        return themes.getOrDefault(themeName, themes.get("light"));
    }
    
    public Map<String, String> getThemeResources(String themeName) {
        Map<String, String> resources = new HashMap<>();
        Theme theme = getTheme(themeName);
        
        resources.put("css", "/css/themes/" + themeName + ".css");
        resources.put("logo", "/images/themes/" + themeName + "/logo.png");
        resources.put("background", "/images/themes/" + themeName + "/background.jpg");
        resources.put("primaryColor", theme.getPrimaryColor());
        resources.put("secondaryColor", theme.getSecondaryColor());
        
        return resources;
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * Theme model
 */
class Theme {
    private String id;
    private String name;
    private String primaryColor;
    private String secondaryColor;
    
    public Theme(String id, String name, String primaryColor, String secondaryColor) {
        this.id = id;
        this.name = name;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    
    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller for theme operations
 */
@RestController
@RequestMapping("/api/theme-resolver")
class ThemeResolverController {
    
    private final ThemeService themeService;
    
    public ThemeResolverController(ThemeService themeService) {
        this.themeService = themeService;
    }
    
    /**
     * Get current theme
     */
    @GetMapping("/current")
    public ResponseEntity<Theme> getCurrentTheme(HttpServletRequest request) {
        String themeName = themeService.getCurrentTheme(request);
        Theme theme = themeService.getTheme(themeName);
        return ResponseEntity.ok(theme);
    }
    
    /**
     * Change theme
     */
    @PostMapping("/change")
    public ResponseEntity<Map<String, String>> changeTheme(
            @RequestParam String theme,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        themeService.changeTheme(request, response, theme);
        
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("newTheme", theme);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get available themes
     */
    @GetMapping("/available")
    public ResponseEntity<List<Theme>> getAvailableThemes() {
        return ResponseEntity.ok(themeService.getAvailableThemes());
    }
    
    /**
     * Get theme resources
     */
    @GetMapping("/resources/{themeName}")
    public ResponseEntity<Map<String, String>> getThemeResources(@PathVariable String themeName) {
        Map<String, String> resources = themeService.getThemeResources(themeName);
        return ResponseEntity.ok(resources);
    }
}

// ============================================================================
// Configuration
// ============================================================================

/**
 * Configuration for theme resolution
 */
@Configuration
class ThemeResolverConfiguration implements WebMvcConfigurer {
    
    /**
     * ThemeSource - loads theme resources
     */
    @Bean
    public ThemeSource themeSource() {
        ResourceBundleThemeSource themeSource = new ResourceBundleThemeSource();
        themeSource.setBasenamePrefix("themes.");
        return themeSource;
    }
    
    /**
     * CookieThemeResolver - stores theme in cookie
     */
    // @Bean
    public ThemeResolver cookieThemeResolver() {
        CookieThemeResolver resolver = new CookieThemeResolver();
        resolver.setDefaultThemeName("light");
        resolver.setCookieName("user-theme");
        resolver.setCookieMaxAge(3600);
        return resolver;
    }
    
    /**
     * SessionThemeResolver - stores theme in session
     */
    @Bean
    public ThemeResolver sessionThemeResolver() {
        SessionThemeResolver resolver = new SessionThemeResolver();
        resolver.setDefaultThemeName("light");
        return resolver;
    }
    
    /**
     * FixedThemeResolver - fixed theme for all users
     */
    // @Bean
    public ThemeResolver fixedThemeResolver() {
        FixedThemeResolver resolver = new FixedThemeResolver();
        resolver.setDefaultThemeName("light");
        return resolver;
    }
    
    /**
     * ThemeChangeInterceptor - detects theme change requests
     */
    @Bean
    public ThemeChangeInterceptor themeChangeInterceptor() {
        ThemeChangeInterceptor interceptor = new ThemeChangeInterceptor();
        interceptor.setParamName("theme");
        return interceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(themeChangeInterceptor());
    }
}
