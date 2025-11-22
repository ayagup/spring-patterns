package com.spring.patterns.internationalization;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Predicate;

/**
 * Locale Change Interceptor Pattern
 * 
 * Demonstrates LocaleChangeInterceptor for dynamic locale switching:
 * - Parameter-based locale switching
 * - Header-based locale switching
 * - Path-based locale switching
 * - Custom locale change detection
 * 
 * Use Cases:
 * 1. User-initiated language switching
 * 2. Dynamic locale changes per request
 * 3. Multi-language web applications
 * 4. API internationalization
 * 5. Content localization based on user preference
 */

@Configuration
class LocaleChangeInterceptorConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Standard parameter-based locale change
        registry.addInterceptor(standardLocaleChangeInterceptor())
            .addPathPatterns("/**");
        
        // Header-based locale change
        registry.addInterceptor(headerBasedLocaleChangeInterceptor())
            .addPathPatterns("/api/**");
        
        // Custom locale change
        registry.addInterceptor(customLocaleChangeInterceptor())
            .addPathPatterns("/app/**")
            .excludePathPatterns("/app/static/**");
    }
    
    /**
     * Standard LocaleChangeInterceptor
     * Detects locale from query parameter
     */
    public LocaleChangeInterceptor standardLocaleChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // ?lang=en
        interceptor.setIgnoreInvalidLocale(true); // Ignore invalid locales
        interceptor.setHttpMethods("GET", "POST"); // Allowed HTTP methods
        return interceptor;
    }
    
    /**
     * Header-based LocaleChangeInterceptor
     * Detects locale from HTTP header
     */
    public LocaleChangeInterceptor headerBasedLocaleChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("locale");
        interceptor.setIgnoreInvalidLocale(false); // Strict validation
        return interceptor;
    }
    
    /**
     * Custom LocaleChangeInterceptor
     * With additional validation
     */
    public LocaleChangeInterceptor customLocaleChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("language");
        interceptor.setIgnoreInvalidLocale(true);
        return interceptor;
    }
}

/**
 * Custom Locale Change Interceptor
 * Implements advanced locale switching logic
 */
class AdvancedLocaleChangeInterceptor {
    
    private String paramName = "locale";
    private String headerName = "Accept-Language";
    private Set<Locale> supportedLocales;
    private Locale defaultLocale = Locale.US;
    private boolean ignoreInvalidLocale = true;
    private Set<String> allowedHttpMethods = new HashSet<>(Arrays.asList("GET", "POST"));
    
    public AdvancedLocaleChangeInterceptor(Set<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }
    
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response) {
        // Check if HTTP method is allowed
        if (!allowedHttpMethods.contains(request.getMethod())) {
            return true;
        }
        
        Locale newLocale = extractLocale(request);
        
        if (newLocale != null) {
            if (isValidLocale(newLocale)) {
                setLocale(request, response, newLocale);
            } else if (!ignoreInvalidLocale) {
                throw new IllegalArgumentException("Invalid locale: " + newLocale);
            }
        }
        
        return true;
    }
    
    private Locale extractLocale(HttpServletRequest request) {
        // Try query parameter first
        String localeParam = request.getParameter(paramName);
        if (localeParam != null && !localeParam.isEmpty()) {
            return parseLocale(localeParam);
        }
        
        // Try header
        String localeHeader = request.getHeader(headerName);
        if (localeHeader != null && !localeHeader.isEmpty()) {
            return parseLocale(localeHeader);
        }
        
        // Try path parameter (e.g., /en/products)
        String pathLocale = extractLocaleFromPath(request.getRequestURI());
        if (pathLocale != null) {
            return parseLocale(pathLocale);
        }
        
        return null;
    }
    
    private Locale parseLocale(String localeString) {
        try {
            return Locale.forLanguageTag(localeString.replace("_", "-"));
        } catch (Exception e) {
            return null;
        }
    }
    
    private String extractLocaleFromPath(String path) {
        // Extract locale from path like /en/products
        String[] parts = path.split("/");
        if (parts.length > 1) {
            return parts[1];
        }
        return null;
    }
    
    private boolean isValidLocale(Locale locale) {
        return supportedLocales.contains(locale) || 
               supportedLocales.stream().anyMatch(s -> 
                   s.getLanguage().equals(locale.getLanguage()));
    }
    
    private void setLocale(HttpServletRequest request, HttpServletResponse response, 
                          Locale locale) {
        // Implementation would use LocaleResolver
        request.setAttribute("current_locale", locale);
    }
    
    // Getters and setters
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
    
    public void setIgnoreInvalidLocale(boolean ignoreInvalidLocale) {
        this.ignoreInvalidLocale = ignoreInvalidLocale;
    }
}

/**
 * Conditional Locale Change Interceptor
 * Changes locale based on custom conditions
 */
class ConditionalLocaleChangeInterceptor {
    
    private final List<LocaleChangeCondition> conditions;
    private final Locale defaultLocale;
    
    public ConditionalLocaleChangeInterceptor(List<LocaleChangeCondition> conditions, 
                                             Locale defaultLocale) {
        this.conditions = conditions;
        this.defaultLocale = defaultLocale;
    }
    
    public Locale resolveLocale(HttpServletRequest request) {
        for (LocaleChangeCondition condition : conditions) {
            if (condition.test(request)) {
                Locale locale = condition.resolveLocale(request);
                if (locale != null) {
                    return locale;
                }
            }
        }
        return defaultLocale;
    }
}

/**
 * Locale change condition
 */
interface LocaleChangeCondition extends Predicate<HttpServletRequest> {
    Locale resolveLocale(HttpServletRequest request);
}

/**
 * Parameter-based locale change condition
 */
class ParameterBasedLocaleCondition implements LocaleChangeCondition {
    
    private final String paramName;
    
    public ParameterBasedLocaleCondition(String paramName) {
        this.paramName = paramName;
    }
    
    @Override
    public boolean test(HttpServletRequest request) {
        String param = request.getParameter(paramName);
        return param != null && !param.isEmpty();
    }
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String param = request.getParameter(paramName);
        return Locale.forLanguageTag(param);
    }
}

/**
 * Header-based locale change condition
 */
class HeaderBasedLocaleCondition implements LocaleChangeCondition {
    
    private final String headerName;
    
    public HeaderBasedLocaleCondition(String headerName) {
        this.headerName = headerName;
    }
    
    @Override
    public boolean test(HttpServletRequest request) {
        String header = request.getHeader(headerName);
        return header != null && !header.isEmpty();
    }
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String header = request.getHeader(headerName);
        return Locale.forLanguageTag(header);
    }
}

/**
 * Path-based locale change condition
 */
class PathBasedLocaleCondition implements LocaleChangeCondition {
    
    private final Map<String, Locale> pathLocaleMap;
    
    public PathBasedLocaleCondition(Map<String, Locale> pathLocaleMap) {
        this.pathLocaleMap = pathLocaleMap;
    }
    
    @Override
    public boolean test(HttpServletRequest request) {
        String path = request.getRequestURI();
        return pathLocaleMap.keySet().stream().anyMatch(path::startsWith);
    }
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String path = request.getRequestURI();
        return pathLocaleMap.entrySet().stream()
            .filter(e -> path.startsWith(e.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }
}

/**
 * User preference locale condition
 */
class UserPreferenceLocaleCondition implements LocaleChangeCondition {
    
    private final Map<String, Locale> userPreferences;
    
    public UserPreferenceLocaleCondition() {
        this.userPreferences = new HashMap<>();
    }
    
    @Override
    public boolean test(HttpServletRequest request) {
        String userId = getUserId(request);
        return userId != null && userPreferences.containsKey(userId);
    }
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String userId = getUserId(request);
        return userPreferences.get(userId);
    }
    
    private String getUserId(HttpServletRequest request) {
        // Mock implementation
        return request.getHeader("X-User-Id");
    }
    
    public void setUserPreference(String userId, Locale locale) {
        userPreferences.put(userId, locale);
    }
}

/**
 * Locale change listener
 */
interface LocaleChangeListener {
    void onLocaleChange(Locale oldLocale, Locale newLocale, HttpServletRequest request);
}

/**
 * Locale change event
 */
record LocaleChangeEvent(
    Locale oldLocale,
    Locale newLocale,
    HttpServletRequest request,
    long timestamp
) {}

/**
 * Locale change notifier
 */
class LocaleChangeNotifier {
    
    private final List<LocaleChangeListener> listeners = new ArrayList<>();
    
    public void addListener(LocaleChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(LocaleChangeListener listener) {
        listeners.remove(listener);
    }
    
    public void notifyLocaleChange(Locale oldLocale, Locale newLocale, 
                                   HttpServletRequest request) {
        for (LocaleChangeListener listener : listeners) {
            listener.onLocaleChange(oldLocale, newLocale, request);
        }
    }
}

/**
 * Audit locale change listener
 */
class AuditLocaleChangeListener implements LocaleChangeListener {
    
    private final List<LocaleChangeEvent> auditLog = new ArrayList<>();
    
    @Override
    public void onLocaleChange(Locale oldLocale, Locale newLocale, HttpServletRequest request) {
        LocaleChangeEvent event = new LocaleChangeEvent(
            oldLocale, newLocale, request, System.currentTimeMillis()
        );
        auditLog.add(event);
        System.out.println("Locale changed: " + oldLocale + " → " + newLocale);
    }
    
    public List<LocaleChangeEvent> getAuditLog() {
        return new ArrayList<>(auditLog);
    }
}

/**
 * Mock HTTP Request
 */
class MockHttpRequest {
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private String uri = "/";
    private String method = "GET";
    
    public String getHeader(String name) {
        return headers.get(name);
    }
    
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }
    
    public String getParameter(String name) {
        return parameters.get(name);
    }
    
    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }
    
    public String getRequestURI() {
        return uri;
    }
    
    public void setRequestURI(String uri) {
        this.uri = uri;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
}

/**
 * Demonstration class
 */
public class LocaleChangeInterceptorPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Locale Change Interceptor Pattern Demo ===\n");
        
        // 1. Standard Locale Change Interceptor Demo
        demonstrateStandardInterceptor();
        
        // 2. Advanced Locale Change Interceptor Demo
        demonstrateAdvancedInterceptor();
        
        // 3. Conditional Locale Change Demo
        demonstrateConditionalLocaleChange();
        
        // 4. Parameter-based Locale Change Demo
        demonstrateParameterBasedChange();
        
        // 5. Header-based Locale Change Demo
        demonstrateHeaderBasedChange();
        
        // 6. Path-based Locale Change Demo
        demonstratePathBasedChange();
        
        // 7. Locale Change Notification Demo
        demonstrateLocaleChangeNotification();
    }
    
    private static void demonstrateStandardInterceptor() {
        System.out.println("1. Standard Locale Change Interceptor Demo:");
        
        LocaleChangeInterceptorConfig config = new LocaleChangeInterceptorConfig();
        LocaleChangeInterceptor interceptor = config.standardLocaleChangeInterceptor();
        
        System.out.println("Parameter Name: " + interceptor.getParamName());
        System.out.println("Ignore Invalid Locale: " + interceptor.isIgnoreInvalidLocale());
        System.out.println("HTTP Methods: " + 
            String.join(", ", interceptor.getHttpMethods()));
        System.out.println();
    }
    
    private static void demonstrateAdvancedInterceptor() {
        System.out.println("2. Advanced Locale Change Interceptor Demo:");
        
        Set<Locale> supported = new HashSet<>(Arrays.asList(
            Locale.US, Locale.UK, new Locale("es"), Locale.FRENCH
        ));
        AdvancedLocaleChangeInterceptor interceptor = 
            new AdvancedLocaleChangeInterceptor(supported);
        
        interceptor.setParamName("lang");
        interceptor.setHeaderName("X-Locale");
        interceptor.setIgnoreInvalidLocale(true);
        
        System.out.println("Advanced interceptor configured");
        System.out.println("Supported Locales: " + supported);
        System.out.println();
    }
    
    private static void demonstrateConditionalLocaleChange() {
        System.out.println("3. Conditional Locale Change Demo:");
        
        List<LocaleChangeCondition> conditions = Arrays.asList(
            new ParameterBasedLocaleCondition("lang"),
            new HeaderBasedLocaleCondition("Accept-Language")
        );
        
        ConditionalLocaleChangeInterceptor interceptor = 
            new ConditionalLocaleChangeInterceptor(conditions, Locale.US);
        
        System.out.println("Conditional interceptor with " + conditions.size() + " conditions");
        System.out.println();
    }
    
    private static void demonstrateParameterBasedChange() {
        System.out.println("4. Parameter-based Locale Change Demo:");
        
        ParameterBasedLocaleCondition condition = new ParameterBasedLocaleCondition("lang");
        
        MockHttpRequest request = new MockHttpRequest();
        request.setParameter("lang", "es-ES");
        
        if (condition.test((HttpServletRequest) request)) {
            Locale locale = condition.resolveLocale((HttpServletRequest) request);
            System.out.println("Resolved Locale from parameter: " + locale);
        }
        System.out.println();
    }
    
    private static void demonstrateHeaderBasedChange() {
        System.out.println("5. Header-based Locale Change Demo:");
        
        HeaderBasedLocaleCondition condition = new HeaderBasedLocaleCondition("X-Locale");
        
        MockHttpRequest request = new MockHttpRequest();
        request.setHeader("X-Locale", "fr-FR");
        
        if (condition.test((HttpServletRequest) request)) {
            Locale locale = condition.resolveLocale((HttpServletRequest) request);
            System.out.println("Resolved Locale from header: " + locale);
        }
        System.out.println();
    }
    
    private static void demonstratePathBasedChange() {
        System.out.println("6. Path-based Locale Change Demo:");
        
        Map<String, Locale> pathMap = new HashMap<>();
        pathMap.put("/en/", Locale.US);
        pathMap.put("/es/", new Locale("es"));
        pathMap.put("/fr/", Locale.FRENCH);
        
        PathBasedLocaleCondition condition = new PathBasedLocaleCondition(pathMap);
        
        String[] paths = {"/en/products", "/es/productos", "/fr/produits"};
        
        for (String path : paths) {
            MockHttpRequest request = new MockHttpRequest();
            request.setRequestURI(path);
            
            if (condition.test((HttpServletRequest) request)) {
                Locale locale = condition.resolveLocale((HttpServletRequest) request);
                System.out.println("Path: " + path + " → Locale: " + locale);
            }
        }
        System.out.println();
    }
    
    private static void demonstrateLocaleChangeNotification() {
        System.out.println("7. Locale Change Notification Demo:");
        
        LocaleChangeNotifier notifier = new LocaleChangeNotifier();
        AuditLocaleChangeListener auditListener = new AuditLocaleChangeListener();
        
        notifier.addListener(auditListener);
        notifier.addListener((oldLocale, newLocale, request) -> 
            System.out.println("Custom listener: " + oldLocale + " → " + newLocale));
        
        MockHttpRequest request = new MockHttpRequest();
        notifier.notifyLocaleChange(Locale.US, new Locale("es"), (HttpServletRequest) request);
        notifier.notifyLocaleChange(new Locale("es"), Locale.FRENCH, (HttpServletRequest) request);
        
        System.out.println("\nAudit Log Size: " + auditListener.getAuditLog().size());
    }
}
