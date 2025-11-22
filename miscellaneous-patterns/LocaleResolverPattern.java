package com.example.miscellaneous.localeresolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Locale Resolver Pattern - Demonstrates Spring's LocaleResolver strategies
 * 
 * This pattern shows how to:
 * 1. Configure LocaleResolver
 * 2. Use AcceptHeaderLocaleResolver
 * 3. Use CookieLocaleResolver
 * 4. Use SessionLocaleResolver
 * 5. Use FixedLocaleResolver
 * 6. Implement custom locale resolution
 * 7. Use LocaleChangeInterceptor
 * 8. Change locale at runtime
 * 9. Persist locale preference
 * 10. Handle locale fallback
 * 
 * Key Concepts:
 * - LocaleResolver: Strategy for determining user's locale
 * - AcceptHeaderLocaleResolver: Uses Accept-Language header
 * - CookieLocaleResolver: Stores locale in cookie
 * - SessionLocaleResolver: Stores locale in session
 * - FixedLocaleResolver: Fixed locale for all users
 * - LocaleChangeInterceptor: Detects locale change requests
 * 
 * Dependencies:
 * - spring-webmvc
 * - spring-boot-starter-web
 * - servlet-api
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class LocaleResolverPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(LocaleResolverPattern.class, args);
    }
}

// ============================================================================
// Locale Resolvers
// ============================================================================

/**
 * Custom LocaleResolver implementation
 */
@Component
class CustomLocaleResolver implements LocaleResolver {
    
    private static final String DEFAULT_LOCALE_ATTR = "user.locale";
    private Locale defaultLocale = Locale.ENGLISH;
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // Check query parameter
        String localeParam = request.getParameter("locale");
        if (localeParam != null) {
            return parseLocale(localeParam);
        }
        
        // Check session
        Locale sessionLocale = (Locale) request.getSession().getAttribute(DEFAULT_LOCALE_ATTR);
        if (sessionLocale != null) {
            return sessionLocale;
        }
        
        // Check Accept-Language header
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            return Locale.forLanguageTag(acceptLanguage.split(",")[0]);
        }
        
        return defaultLocale;
    }
    
    @Override
    public void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, 
                         @Nullable Locale locale) {
        if (locale != null) {
            request.getSession().setAttribute(DEFAULT_LOCALE_ATTR, locale);
        } else {
            request.getSession().removeAttribute(DEFAULT_LOCALE_ATTR);
        }
    }
    
    private Locale parseLocale(String localeString) {
        String[] parts = localeString.split("_");
        return parts.length == 1 
            ? new Locale(parts[0]) 
            : new Locale(parts[0], parts[1]);
    }
    
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
}

// ============================================================================
// Locale Service
// ============================================================================

/**
 * Service for locale operations
 */
@org.springframework.stereotype.Service
class LocaleService {
    
    private final LocaleResolver localeResolver;
    
    public LocaleService(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }
    
    public Locale getCurrentLocale(HttpServletRequest request) {
        return localeResolver.resolveLocale(request);
    }
    
    public void changeLocale(HttpServletRequest request, HttpServletResponse response, 
                           Locale locale) {
        localeResolver.setLocale(request, response, locale);
    }
    
    public List<Locale> getSupportedLocales() {
        return Arrays.asList(
            Locale.ENGLISH,
            Locale.FRENCH,
            Locale.GERMAN,
            new Locale("es"),
            new Locale("zh"),
            new Locale("ja"),
            new Locale("ar")
        );
    }
    
    public LocaleInfo getLocaleInfo(Locale locale) {
        LocaleInfo info = new LocaleInfo();
        info.setCode(locale.toString());
        info.setLanguage(locale.getLanguage());
        info.setCountry(locale.getCountry());
        info.setDisplayName(locale.getDisplayName());
        info.setDisplayLanguage(locale.getDisplayLanguage());
        info.setDisplayCountry(locale.getDisplayCountry());
        return info;
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * Locale information model
 */
class LocaleInfo {
    private String code;
    private String language;
    private String country;
    private String displayName;
    private String displayLanguage;
    private String displayCountry;
    
    // Getters and setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getDisplayLanguage() { return displayLanguage; }
    public void setDisplayLanguage(String displayLanguage) { this.displayLanguage = displayLanguage; }
    
    public String getDisplayCountry() { return displayCountry; }
    public void setDisplayCountry(String displayCountry) { this.displayCountry = displayCountry; }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller for locale operations
 */
@RestController
@RequestMapping("/api/locale-resolver")
class LocaleResolverController {
    
    private final LocaleService localeService;
    
    public LocaleResolverController(LocaleService localeService) {
        this.localeService = localeService;
    }
    
    /**
     * Get current locale
     */
    @GetMapping("/current")
    public ResponseEntity<LocaleInfo> getCurrentLocale(HttpServletRequest request) {
        Locale currentLocale = localeService.getCurrentLocale(request);
        LocaleInfo info = localeService.getLocaleInfo(currentLocale);
        return ResponseEntity.ok(info);
    }
    
    /**
     * Change locale
     */
    @PostMapping("/change")
    public ResponseEntity<Map<String, String>> changeLocale(
            @RequestParam String locale,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        Locale newLocale = Locale.forLanguageTag(locale);
        localeService.changeLocale(request, response, newLocale);
        
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("newLocale", newLocale.toString());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get supported locales
     */
    @GetMapping("/supported")
    public ResponseEntity<List<LocaleInfo>> getSupportedLocales() {
        List<LocaleInfo> locales = new ArrayList<>();
        for (Locale locale : localeService.getSupportedLocales()) {
            locales.add(localeService.getLocaleInfo(locale));
        }
        return ResponseEntity.ok(locales);
    }
}

// ============================================================================
// Configuration
// ============================================================================

/**
 * Configuration for locale resolution
 */
@Configuration
class LocaleResolverConfiguration implements WebMvcConfigurer {
    
    /**
     * AcceptHeaderLocaleResolver - uses Accept-Language header
     */
    // @Bean
    public LocaleResolver acceptHeaderLocaleResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales(Arrays.asList(
            Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN
        ));
        return resolver;
    }
    
    /**
     * CookieLocaleResolver - stores locale in cookie
     */
    // @Bean
    public LocaleResolver cookieLocaleResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookieName("user-locale");
        resolver.setCookieMaxAge(3600); // 1 hour
        return resolver;
    }
    
    /**
     * SessionLocaleResolver - stores locale in session
     */
    @Bean
    public LocaleResolver sessionLocaleResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
    
    /**
     * FixedLocaleResolver - fixed locale for all users
     */
    // @Bean
    public LocaleResolver fixedLocaleResolver() {
        FixedLocaleResolver resolver = new FixedLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
    
    /**
     * LocaleChangeInterceptor - detects locale change requests
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
