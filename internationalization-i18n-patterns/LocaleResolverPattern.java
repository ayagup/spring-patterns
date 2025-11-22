package com.spring.patterns.internationalization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Locale Resolver Pattern
 * 
 * Demonstrates various LocaleResolver implementations in Spring:
 * - AcceptHeaderLocaleResolver: Uses Accept-Language header
 * - CookieLocaleResolver: Stores locale in a cookie
 * - SessionLocaleResolver: Stores locale in HTTP session
 * - FixedLocaleResolver: Uses a fixed locale
 * 
 * Use Cases:
 * 1. Automatic locale detection from browser
 * 2. Persistent locale preference across sessions
 * 3. User-specific locale selection
 * 4. Multi-language web applications
 * 5. Regional content customization
 */

@Configuration
class LocaleResolverConfig {
    
    /**
     * AcceptHeaderLocaleResolver
     * Determines locale from Accept-Language HTTP header
     * No state change - read-only
     */
    @Bean
    public LocaleResolver acceptHeaderLocaleResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        resolver.setSupportedLocales(Arrays.asList(
            Locale.US,
            Locale.UK,
            new Locale("es"),
            Locale.FRENCH,
            Locale.GERMAN,
            Locale.JAPANESE,
            Locale.SIMPLIFIED_CHINESE
        ));
        return resolver;
    }
    
    /**
     * CookieLocaleResolver
     * Stores locale preference in a cookie
     * Persists across browser sessions
     */
    @Bean
    public LocaleResolver cookieLocaleResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        resolver.setCookieName("user_locale");
        resolver.setCookieMaxAge(7 * 24 * 60 * 60); // 7 days
        resolver.setCookiePath("/");
        resolver.setCookieSecure(true); // HTTPS only
        resolver.setCookieHttpOnly(true); // JavaScript cannot access
        resolver.setRejectInvalidCookies(true);
        return resolver;
    }
    
    /**
     * SessionLocaleResolver
     * Stores locale in HTTP session
     * Persists only during user session
     */
    @Bean
    public LocaleResolver sessionLocaleResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        resolver.setDefaultTimeZone(TimeZone.getTimeZone("UTC"));
        return resolver;
    }
    
    /**
     * FixedLocaleResolver
     * Always returns the same locale
     * Useful for testing or single-language applications
     */
    @Bean
    public LocaleResolver fixedLocaleResolver() {
        FixedLocaleResolver resolver = new FixedLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        resolver.setDefaultTimeZone(TimeZone.getTimeZone("America/New_York"));
        return resolver;
    }
}

/**
 * Custom Locale Resolver
 * Implements custom locale resolution logic
 */
class CustomLocaleResolver implements LocaleResolver {
    
    private Locale defaultLocale = Locale.US;
    private final Map<String, Locale> userLocalePreferences = new HashMap<>();
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // Try to get locale from custom header
        String localeHeader = request.getHeader("X-User-Locale");
        if (localeHeader != null && !localeHeader.isEmpty()) {
            return Locale.forLanguageTag(localeHeader);
        }
        
        // Try to get from user preferences (requires authentication)
        String userId = getUserId(request);
        if (userId != null && userLocalePreferences.containsKey(userId)) {
            return userLocalePreferences.get(userId);
        }
        
        // Try to get from query parameter
        String localeParam = request.getParameter("locale");
        if (localeParam != null && !localeParam.isEmpty()) {
            return Locale.forLanguageTag(localeParam);
        }
        
        // Fall back to Accept-Language header
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            return request.getLocale();
        }
        
        return defaultLocale;
    }
    
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        String userId = getUserId(request);
        if (userId != null && locale != null) {
            userLocalePreferences.put(userId, locale);
        }
    }
    
    private String getUserId(HttpServletRequest request) {
        // Mock implementation - in real app, get from security context
        return request.getHeader("X-User-Id");
    }
    
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
}

/**
 * Chained Locale Resolver
 * Tries multiple resolvers in sequence
 */
class ChainedLocaleResolver implements LocaleResolver {
    
    private final List<LocaleResolver> resolvers;
    private final Locale defaultLocale;
    
    public ChainedLocaleResolver(List<LocaleResolver> resolvers, Locale defaultLocale) {
        this.resolvers = resolvers;
        this.defaultLocale = defaultLocale;
    }
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        for (LocaleResolver resolver : resolvers) {
            try {
                Locale locale = resolver.resolveLocale(request);
                if (locale != null && !locale.equals(defaultLocale)) {
                    return locale;
                }
            } catch (Exception e) {
                // Continue to next resolver
            }
        }
        return defaultLocale;
    }
    
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        for (LocaleResolver resolver : resolvers) {
            try {
                resolver.setLocale(request, response, locale);
            } catch (Exception e) {
                // Continue to next resolver
            }
        }
    }
}

/**
 * Locale detection service
 */
class LocaleDetectionService {
    
    private final List<Locale> supportedLocales;
    
    public LocaleDetectionService(List<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }
    
    /**
     * Find best matching locale from Accept-Language header
     */
    public Locale findBestMatch(String acceptLanguageHeader) {
        if (acceptLanguageHeader == null || acceptLanguageHeader.isEmpty()) {
            return Locale.getDefault();
        }
        
        List<LocalePreference> preferences = parseAcceptLanguage(acceptLanguageHeader);
        
        for (LocalePreference preference : preferences) {
            Locale matched = findMatchingLocale(preference.locale());
            if (matched != null) {
                return matched;
            }
        }
        
        return Locale.getDefault();
    }
    
    private List<LocalePreference> parseAcceptLanguage(String header) {
        List<LocalePreference> preferences = new ArrayList<>();
        String[] parts = header.split(",");
        
        for (String part : parts) {
            String[] elements = part.trim().split(";");
            String localeStr = elements[0].trim();
            double quality = 1.0;
            
            if (elements.length > 1 && elements[1].startsWith("q=")) {
                try {
                    quality = Double.parseDouble(elements[1].substring(2));
                } catch (NumberFormatException e) {
                    quality = 1.0;
                }
            }
            
            Locale locale = Locale.forLanguageTag(localeStr);
            preferences.add(new LocalePreference(locale, quality));
        }
        
        preferences.sort((a, b) -> Double.compare(b.quality(), a.quality()));
        return preferences;
    }
    
    private Locale findMatchingLocale(Locale requested) {
        // Exact match
        for (Locale supported : supportedLocales) {
            if (supported.equals(requested)) {
                return supported;
            }
        }
        
        // Language and country match
        for (Locale supported : supportedLocales) {
            if (supported.getLanguage().equals(requested.getLanguage()) &&
                supported.getCountry().equals(requested.getCountry())) {
                return supported;
            }
        }
        
        // Language match only
        for (Locale supported : supportedLocales) {
            if (supported.getLanguage().equals(requested.getLanguage())) {
                return supported;
            }
        }
        
        return null;
    }
}

record LocalePreference(Locale locale, double quality) {}

/**
 * Locale context holder
 */
class LocaleContextHolder {
    
    private static final ThreadLocal<Locale> localeHolder = new ThreadLocal<>();
    private static final ThreadLocal<TimeZone> timeZoneHolder = new ThreadLocal<>();
    
    public static void setLocale(Locale locale) {
        localeHolder.set(locale);
    }
    
    public static Locale getLocale() {
        Locale locale = localeHolder.get();
        return locale != null ? locale : Locale.getDefault();
    }
    
    public static void setTimeZone(TimeZone timeZone) {
        timeZoneHolder.set(timeZone);
    }
    
    public static TimeZone getTimeZone() {
        TimeZone timeZone = timeZoneHolder.get();
        return timeZone != null ? timeZone : TimeZone.getDefault();
    }
    
    public static void clear() {
        localeHolder.remove();
        timeZoneHolder.remove();
    }
}

/**
 * Locale validator
 */
class LocaleValidator {
    
    private final Set<Locale> supportedLocales;
    
    public LocaleValidator(Set<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }
    
    public boolean isSupported(Locale locale) {
        return supportedLocales.contains(locale);
    }
    
    public Locale validateOrDefault(Locale locale, Locale defaultLocale) {
        return isSupported(locale) ? locale : defaultLocale;
    }
    
    public List<Locale> getSupportedLocales() {
        return new ArrayList<>(supportedLocales);
    }
}

/**
 * Locale information provider
 */
class LocaleInformationProvider {
    
    public LocaleInfo getLocaleInfo(Locale locale) {
        return new LocaleInfo(
            locale,
            locale.getDisplayName(locale),
            locale.getDisplayLanguage(locale),
            locale.getDisplayCountry(locale),
            locale.getLanguage(),
            locale.getCountry(),
            locale.getScript(),
            locale.getVariant(),
            locale.toLanguageTag()
        );
    }
    
    public List<LocaleInfo> getAllAvailableLocales() {
        return Arrays.stream(Locale.getAvailableLocales())
            .map(this::getLocaleInfo)
            .toList();
    }
}

record LocaleInfo(
    Locale locale,
    String displayName,
    String displayLanguage,
    String displayCountry,
    String language,
    String country,
    String script,
    String variant,
    String languageTag
) {}

/**
 * Mock HTTP Request for demonstration
 */
class MockHttpServletRequest {
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private Locale locale = Locale.US;
    
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
    
    public Locale getLocale() {
        return locale;
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}

/**
 * Mock HTTP Response for demonstration
 */
class MockHttpServletResponse {
    private final Map<String, String> headers = new HashMap<>();
    
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }
    
    public String getHeader(String name) {
        return headers.get(name);
    }
}

/**
 * Demonstration class
 */
public class LocaleResolverPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Locale Resolver Pattern Demo ===\n");
        
        // 1. Accept Header Locale Resolver Demo
        demonstrateAcceptHeaderResolver();
        
        // 2. Cookie Locale Resolver Demo
        demonstrateCookieResolver();
        
        // 3. Session Locale Resolver Demo
        demonstrateSessionResolver();
        
        // 4. Custom Locale Resolver Demo
        demonstrateCustomResolver();
        
        // 5. Locale Detection Demo
        demonstrateLocaleDetection();
        
        // 6. Locale Validation Demo
        demonstrateLocaleValidation();
        
        // 7. Locale Information Demo
        demonstrateLocaleInformation();
    }
    
    private static void demonstrateAcceptHeaderResolver() {
        System.out.println("1. Accept Header Locale Resolver Demo:");
        
        LocaleResolverConfig config = new LocaleResolverConfig();
        AcceptHeaderLocaleResolver resolver = 
            (AcceptHeaderLocaleResolver) config.acceptHeaderLocaleResolver();
        
        System.out.println("Default Locale: " + resolver.getDefaultLocale());
        System.out.println("Supported Locales: " + resolver.getSupportedLocales());
        System.out.println();
    }
    
    private static void demonstrateCookieResolver() {
        System.out.println("2. Cookie Locale Resolver Demo:");
        
        LocaleResolverConfig config = new LocaleResolverConfig();
        CookieLocaleResolver resolver = (CookieLocaleResolver) config.cookieLocaleResolver();
        
        System.out.println("Cookie Name: " + resolver.getCookieName());
        System.out.println("Cookie Max Age: " + resolver.getCookieMaxAge() + " seconds");
        System.out.println("Cookie Path: " + resolver.getCookiePath());
        System.out.println("Cookie Secure: " + resolver.isCookieSecure());
        System.out.println();
    }
    
    private static void demonstrateSessionResolver() {
        System.out.println("3. Session Locale Resolver Demo:");
        
        LocaleResolverConfig config = new LocaleResolverConfig();
        SessionLocaleResolver resolver = (SessionLocaleResolver) config.sessionLocaleResolver();
        
        System.out.println("Default Locale: " + resolver.getDefaultLocale());
        System.out.println("Default TimeZone: " + resolver.getDefaultTimeZone().getID());
        System.out.println();
    }
    
    private static void demonstrateCustomResolver() {
        System.out.println("4. Custom Locale Resolver Demo:");
        
        CustomLocaleResolver resolver = new CustomLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        // Test with custom header
        request.setHeader("X-User-Locale", "es-ES");
        System.out.println("Locale from X-User-Locale header: " + 
            resolver.resolveLocale((HttpServletRequest) request));
        
        // Test with query parameter
        request.setHeader("X-User-Locale", null);
        request.setParameter("locale", "fr-FR");
        System.out.println("Locale from query parameter: " + 
            resolver.resolveLocale((HttpServletRequest) request));
        System.out.println();
    }
    
    private static void demonstrateLocaleDetection() {
        System.out.println("5. Locale Detection Demo:");
        
        List<Locale> supported = Arrays.asList(
            Locale.US, Locale.UK, new Locale("es"), Locale.FRENCH, Locale.GERMAN
        );
        LocaleDetectionService service = new LocaleDetectionService(supported);
        
        String[] acceptHeaders = {
            "en-US,en;q=0.9",
            "es-ES,es;q=0.9,en;q=0.8",
            "fr-FR,fr;q=0.9",
            "de-DE,de;q=0.9,en;q=0.8",
            "ja-JP,ja;q=0.9,en;q=0.8"
        };
        
        for (String header : acceptHeaders) {
            Locale matched = service.findBestMatch(header);
            System.out.println("Accept-Language: " + header + " → " + matched);
        }
        System.out.println();
    }
    
    private static void demonstrateLocaleValidation() {
        System.out.println("6. Locale Validation Demo:");
        
        Set<Locale> supported = new HashSet<>(Arrays.asList(
            Locale.US, Locale.UK, new Locale("es"), Locale.FRENCH
        ));
        LocaleValidator validator = new LocaleValidator(supported);
        
        Locale[] testLocales = {
            Locale.US, Locale.GERMAN, new Locale("es"), Locale.JAPANESE
        };
        
        for (Locale locale : testLocales) {
            boolean isSupported = validator.isSupported(locale);
            Locale validated = validator.validateOrDefault(locale, Locale.US);
            System.out.println(locale + " - Supported: " + isSupported + 
                ", Validated: " + validated);
        }
        System.out.println();
    }
    
    private static void demonstrateLocaleInformation() {
        System.out.println("7. Locale Information Demo:");
        
        LocaleInformationProvider provider = new LocaleInformationProvider();
        
        Locale[] locales = {Locale.US, Locale.UK, new Locale("es", "ES"), Locale.FRENCH};
        
        for (Locale locale : locales) {
            LocaleInfo info = provider.getLocaleInfo(locale);
            System.out.println("Locale: " + info.languageTag());
            System.out.println("  Display Name: " + info.displayName());
            System.out.println("  Language: " + info.displayLanguage());
            System.out.println("  Country: " + info.displayCountry());
            System.out.println();
        }
    }
}
