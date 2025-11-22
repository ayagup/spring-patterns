package com.example.miscellaneous.resourcebundle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ResourceBundle Pattern - Demonstrates Spring's MessageSource for internationalization
 * 
 * This pattern shows how to:
 * 1. Configure MessageSource
 * 2. Use ResourceBundleMessageSource
 * 3. Use ReloadableResourceBundleMessageSource
 * 4. Retrieve localized messages
 * 5. Pass message parameters
 * 6. Handle default messages
 * 7. Support multiple locales
 * 8. Load properties from files
 * 9. Reload messages dynamically
 * 10. Handle missing keys
 * 
 * Key Concepts:
 * - MessageSource: Spring's abstraction for internationalization
 * - ResourceBundleMessageSource: Uses Java ResourceBundle
 * - ReloadableResourceBundleMessageSource: Can reload changes
 * - Locale: Language and region settings
 * - Message Parameters: Dynamic message formatting
 * 
 * Dependencies:
 * - spring-context
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class ResourceBundlePattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(ResourceBundlePattern.class, args);
        demonstrateMessageSource(context);
    }
    
    /**
     * Demonstrates message source functionality
     */
    private static void demonstrateMessageSource(org.springframework.context.ApplicationContext context) {
        System.out.println("=== ResourceBundle Pattern Demonstrations ===\n");
        
        MessageSource messageSource = context.getBean(MessageSource.class);
        
        // Demo 1: Simple message
        System.out.println("1. Simple Messages:");
        System.out.println("   English: " + messageSource.getMessage("greeting", null, Locale.ENGLISH));
        System.out.println("   French: " + messageSource.getMessage("greeting", null, Locale.FRENCH));
        System.out.println();
        
        // Demo 2: Message with parameters
        System.out.println("2. Messages with Parameters:");
        Object[] params = {"John", LocalDate.now()};
        System.out.println("   English: " + messageSource.getMessage("welcome.user", params, Locale.ENGLISH));
        System.out.println("   French: " + messageSource.getMessage("welcome.user", params, Locale.FRENCH));
        System.out.println();
        
        // Demo 3: Default message
        System.out.println("3. Default Messages:");
        String defaultMsg = messageSource.getMessage("nonexistent.key", null, "Default Message", Locale.ENGLISH);
        System.out.println("   Default: " + defaultMsg);
        System.out.println();
    }
}

// ============================================================================
// Message Service
// ============================================================================

/**
 * Service for message retrieval
 */
@org.springframework.stereotype.Service
class MessageService {
    
    private final MessageSource messageSource;
    
    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }
    
    public String getMessage(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }
    
    public String getMessageWithDefault(String key, String defaultMessage, Locale locale) {
        return messageSource.getMessage(key, null, defaultMessage, locale);
    }
    
    public Map<String, String> getAllMessages(List<String> keys, Locale locale) {
        Map<String, String> messages = new HashMap<>();
        for (String key : keys) {
            try {
                String message = messageSource.getMessage(key, null, locale);
                messages.put(key, message);
            } catch (Exception e) {
                messages.put(key, "Message not found");
            }
        }
        return messages;
    }
}

// ============================================================================
// Locale Helper
// ============================================================================

/**
 * Helper class for locale operations
 */
@org.springframework.stereotype.Component
class LocaleHelper {
    
    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
        Locale.ENGLISH,
        Locale.FRENCH,
        Locale.GERMAN,
        new Locale("es"),
        new Locale("zh")
    );
    
    public List<Locale> getSupportedLocales() {
        return new ArrayList<>(SUPPORTED_LOCALES);
    }
    
    public Locale parseLocale(String localeString) {
        if (localeString == null || localeString.isEmpty()) {
            return Locale.getDefault();
        }
        
        String[] parts = localeString.split("_");
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        }
        
        return Locale.getDefault();
    }
    
    public String formatLocale(Locale locale) {
        return locale.getLanguage() + 
               (locale.getCountry().isEmpty() ? "" : "_" + locale.getCountry());
    }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller for i18n demonstrations
 */
@RestController
@RequestMapping("/api/resource-bundle")
class ResourceBundleController {
    
    private final MessageService messageService;
    private final LocaleHelper localeHelper;
    
    public ResourceBundleController(MessageService messageService, LocaleHelper localeHelper) {
        this.messageService = messageService;
        this.localeHelper = localeHelper;
    }
    
    /**
     * Get message by key
     */
    @GetMapping("/message/{key}")
    public ResponseEntity<Map<String, String>> getMessage(
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "en") String locale) {
        
        Locale loc = localeHelper.parseLocale(locale);
        String message = messageService.getMessage(key, loc);
        
        Map<String, String> response = new HashMap<>();
        response.put("key", key);
        response.put("locale", localeHelper.formatLocale(loc));
        response.put("message", message);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get message with parameters
     */
    @PostMapping("/message/{key}")
    public ResponseEntity<Map<String, String>> getMessageWithParams(
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "en") String locale,
            @RequestBody List<String> params) {
        
        Locale loc = localeHelper.parseLocale(locale);
        String message = messageService.getMessage(key, params.toArray(), loc);
        
        Map<String, String> response = new HashMap<>();
        response.put("key", key);
        response.put("locale", localeHelper.formatLocale(loc));
        response.put("message", message);
        response.put("params", String.join(", ", params));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get multiple messages
     */
    @PostMapping("/messages")
    public ResponseEntity<Map<String, String>> getMessages(
            @RequestParam(required = false, defaultValue = "en") String locale,
            @RequestBody List<String> keys) {
        
        Locale loc = localeHelper.parseLocale(locale);
        Map<String, String> messages = messageService.getAllMessages(keys, loc);
        
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get supported locales
     */
    @GetMapping("/locales")
    public ResponseEntity<List<Map<String, String>>> getSupportedLocales() {
        List<Map<String, String>> locales = new ArrayList<>();
        
        for (Locale locale : localeHelper.getSupportedLocales()) {
            Map<String, String> localeInfo = new HashMap<>();
            localeInfo.put("code", localeHelper.formatLocale(locale));
            localeInfo.put("language", locale.getDisplayLanguage());
            localeInfo.put("country", locale.getDisplayCountry());
            locales.add(localeInfo);
        }
        
        return ResponseEntity.ok(locales);
    }
}

// ============================================================================
// Configuration
// ============================================================================

/**
 * Configuration for message sources
 */
@Configuration
class MessageSourceConfiguration {
    
    /**
     * ResourceBundleMessageSource - uses Java ResourceBundle
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages", "errors", "validation");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }
    
    /**
     * ReloadableResourceBundleMessageSource - can reload changes
     * Uncomment to use instead of ResourceBundleMessageSource
     */
    // @Bean
    public MessageSource reloadableMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = 
            new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages", "classpath:errors");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache for 1 hour
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
}

// ============================================================================
// Helper Classes
// ============================================================================

/**
 * Simple date holder (since java.time.LocalDate used in demo)
 */
class LocalDate {
    public static LocalDate now() {
        return new LocalDate();
    }
    
    @Override
    public String toString() {
        return java.time.LocalDate.now().toString();
    }
}
