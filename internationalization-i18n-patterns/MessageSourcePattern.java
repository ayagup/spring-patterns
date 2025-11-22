package com.spring.patterns.internationalization;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

/**
 * Message Source Pattern
 * 
 * Demonstrates various MessageSource implementations in Spring for internationalization:
 * - ResourceBundleMessageSource: Uses ResourceBundle for message resolution
 * - ReloadableResourceBundleMessageSource: Reloadable message source
 * - StaticMessageSource: Programmatic message definition
 * 
 * Use Cases:
 * 1. Multi-language application support
 * 2. Externalized error messages
 * 3. Dynamic content localization
 * 4. User interface text internationalization
 * 5. Email template internationalization
 * 6. Validation message localization
 */

@Configuration
class MessageSourceConfig {
    
    /**
     * ResourceBundleMessageSource
     * Uses standard Java ResourceBundle mechanism
     * Caches loaded bundles for performance
     */
    @Bean
    public MessageSource resourceBundleMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages/messages", "messages/errors");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache for 1 hour
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }
    
    /**
     * ReloadableResourceBundleMessageSource
     * Can reload messages without restarting the application
     * Useful for development and dynamic content updates
     */
    @Bean
    public MessageSource reloadableMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = 
            new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
            "classpath:i18n/messages",
            "classpath:i18n/validation",
            "file:/opt/config/messages"
        );
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(60); // Reload every 60 seconds
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(false);
        messageSource.setAlwaysUseMessageFormat(false);
        return messageSource;
    }
    
    /**
     * StaticMessageSource
     * For programmatic message definition
     * Useful for testing or dynamic message registration
     */
    @Bean
    public MessageSource staticMessageSource() {
        StaticMessageSource messageSource = new StaticMessageSource();
        
        // English messages
        messageSource.addMessage("greeting", Locale.ENGLISH, "Hello, {0}!");
        messageSource.addMessage("farewell", Locale.ENGLISH, "Goodbye, {0}!");
        messageSource.addMessage("error.notfound", Locale.ENGLISH, "Item not found");
        
        // Spanish messages
        messageSource.addMessage("greeting", new Locale("es"), "¡Hola, {0}!");
        messageSource.addMessage("farewell", new Locale("es"), "¡Adiós, {0}!");
        messageSource.addMessage("error.notfound", new Locale("es"), "Artículo no encontrado");
        
        // French messages
        messageSource.addMessage("greeting", Locale.FRENCH, "Bonjour, {0}!");
        messageSource.addMessage("farewell", Locale.FRENCH, "Au revoir, {0}!");
        messageSource.addMessage("error.notfound", Locale.FRENCH, "Article non trouvé");
        
        // German messages
        messageSource.addMessage("greeting", Locale.GERMAN, "Hallo, {0}!");
        messageSource.addMessage("farewell", Locale.GERMAN, "Auf Wiedersehen, {0}!");
        messageSource.addMessage("error.notfound", Locale.GERMAN, "Artikel nicht gefunden");
        
        return messageSource;
    }
}

/**
 * Service demonstrating MessageSource usage
 */
class InternationalizationService {
    
    private final MessageSource messageSource;
    
    public InternationalizationService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    /**
     * Get simple message without parameters
     */
    public String getSimpleMessage(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }
    
    /**
     * Get message with parameters
     */
    public String getMessageWithParams(String code, Object[] params, Locale locale) {
        return messageSource.getMessage(code, params, locale);
    }
    
    /**
     * Get message with default fallback
     */
    public String getMessageWithDefault(String code, String defaultMessage, Locale locale) {
        return messageSource.getMessage(code, null, defaultMessage, locale);
    }
    
    /**
     * Get message using MessageSourceResolvable
     */
    public String getResolvableMessage(MessageSourceResolvable resolvable, Locale locale) {
        return messageSource.getMessage(resolvable, locale);
    }
}

/**
 * Custom MessageSourceResolvable implementation
 */
record LocalizedMessage(
    String[] codes,
    Object[] arguments,
    String defaultMessage
) implements org.springframework.context.MessageSourceResolvable {
    
    @Override
    public String[] getCodes() {
        return codes;
    }
    
    @Override
    public Object[] getArguments() {
        return arguments;
    }
    
    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }
}

/**
 * Error message builder
 */
class ErrorMessageBuilder {
    
    private final MessageSource messageSource;
    
    public ErrorMessageBuilder(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    public String buildValidationError(String field, String constraint, Locale locale) {
        String code = "validation." + constraint;
        Object[] args = new Object[]{field};
        return messageSource.getMessage(code, args, "Validation failed for " + field, locale);
    }
    
    public String buildNotFoundError(String entity, String id, Locale locale) {
        String code = "error.entity.notfound";
        Object[] args = new Object[]{entity, id};
        return messageSource.getMessage(code, args, 
            entity + " with id " + id + " not found", locale);
    }
    
    public String buildAccessDeniedError(String resource, Locale locale) {
        String code = "error.access.denied";
        Object[] args = new Object[]{resource};
        return messageSource.getMessage(code, args, 
            "Access denied to " + resource, locale);
    }
}

/**
 * Hierarchical MessageSource
 * Demonstrates parent-child message source relationships
 */
class HierarchicalMessageSourceExample {
    
    public MessageSource createHierarchicalMessageSource() {
        // Parent message source - common messages
        StaticMessageSource parentMessageSource = new StaticMessageSource();
        parentMessageSource.addMessage("app.name", Locale.ENGLISH, "My Application");
        parentMessageSource.addMessage("app.version", Locale.ENGLISH, "1.0.0");
        
        // Child message source - specific messages
        StaticMessageSource childMessageSource = new StaticMessageSource();
        childMessageSource.setParentMessageSource(parentMessageSource);
        childMessageSource.addMessage("module.name", Locale.ENGLISH, "User Module");
        
        return childMessageSource;
    }
}

/**
 * Message source caching strategy
 */
class MessageSourceCacheManager {
    
    private final ReloadableResourceBundleMessageSource messageSource;
    
    public MessageSourceCacheManager(ReloadableResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    /**
     * Clear message cache
     */
    public void clearCache() {
        messageSource.clearCache();
    }
    
    /**
     * Clear cache for specific locale
     */
    public void clearCacheForLocale(Locale locale) {
        messageSource.clearCacheIncludingAncestors();
    }
    
    /**
     * Set cache duration
     */
    public void setCacheDuration(int seconds) {
        messageSource.setCacheSeconds(seconds);
    }
}

/**
 * Message key generator for consistent naming
 */
class MessageKeyGenerator {
    
    private static final String SEPARATOR = ".";
    
    public String generateValidationKey(String entity, String field, String constraint) {
        return String.join(SEPARATOR, "validation", entity, field, constraint);
    }
    
    public String generateErrorKey(String category, String errorType) {
        return String.join(SEPARATOR, "error", category, errorType);
    }
    
    public String generateLabelKey(String module, String component, String element) {
        return String.join(SEPARATOR, "label", module, component, element);
    }
    
    public String generateButtonKey(String action) {
        return String.join(SEPARATOR, "button", action);
    }
    
    public String generateMessageKey(String category, String type) {
        return String.join(SEPARATOR, "message", category, type);
    }
}

/**
 * Localized content provider
 */
class LocalizedContentProvider {
    
    private final MessageSource messageSource;
    private final MessageKeyGenerator keyGenerator;
    
    public LocalizedContentProvider(MessageSource messageSource) {
        this.messageSource = messageSource;
        this.keyGenerator = new MessageKeyGenerator();
    }
    
    public LocalizedContent getContent(String contentType, String contentId, Locale locale) {
        String titleKey = keyGenerator.generateLabelKey(contentType, contentId, "title");
        String descriptionKey = keyGenerator.generateLabelKey(contentType, contentId, "description");
        
        String title = messageSource.getMessage(titleKey, null, titleKey, locale);
        String description = messageSource.getMessage(descriptionKey, null, "", locale);
        
        return new LocalizedContent(contentId, title, description, locale);
    }
}

record LocalizedContent(
    String id,
    String title,
    String description,
    Locale locale
) {}

/**
 * Email template message resolver
 */
class EmailTemplateMessageResolver {
    
    private final MessageSource messageSource;
    
    public EmailTemplateMessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    public EmailTemplate resolveTemplate(String templateName, Object[] params, Locale locale) {
        String subjectKey = "email." + templateName + ".subject";
        String bodyKey = "email." + templateName + ".body";
        
        String subject = messageSource.getMessage(subjectKey, params, locale);
        String body = messageSource.getMessage(bodyKey, params, locale);
        
        return new EmailTemplate(templateName, subject, body, locale);
    }
}

record EmailTemplate(
    String name,
    String subject,
    String body,
    Locale locale
) {}

/**
 * Demonstration class
 */
public class MessageSourcePattern {
    
    public static void main(String[] args) {
        System.out.println("=== Message Source Pattern Demo ===\n");
        
        // 1. Static Message Source Demo
        demonstrateStaticMessageSource();
        
        // 2. Message with Parameters Demo
        demonstrateParameterizedMessages();
        
        // 3. Hierarchical Message Source Demo
        demonstrateHierarchicalMessageSource();
        
        // 4. Error Message Building Demo
        demonstrateErrorMessages();
        
        // 5. Message Key Generation Demo
        demonstrateMessageKeyGeneration();
        
        // 6. Localized Content Demo
        demonstrateLocalizedContent();
        
        // 7. Email Template Demo
        demonstrateEmailTemplates();
    }
    
    private static void demonstrateStaticMessageSource() {
        System.out.println("1. Static Message Source Demo:");
        
        MessageSourceConfig config = new MessageSourceConfig();
        MessageSource messageSource = config.staticMessageSource();
        InternationalizationService service = new InternationalizationService(messageSource);
        
        Locale[] locales = {Locale.ENGLISH, new Locale("es"), Locale.FRENCH, Locale.GERMAN};
        
        for (Locale locale : locales) {
            String greeting = service.getMessageWithParams("greeting", 
                new Object[]{"World"}, locale);
            String farewell = service.getMessageWithParams("farewell", 
                new Object[]{"Friend"}, locale);
            System.out.println(locale.getDisplayName() + ": " + greeting + " / " + farewell);
        }
        System.out.println();
    }
    
    private static void demonstrateParameterizedMessages() {
        System.out.println("2. Parameterized Messages Demo:");
        
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("user.welcome", Locale.ENGLISH, 
            "Welcome {0}! You have {1} new messages.");
        messageSource.addMessage("order.confirmation", Locale.ENGLISH, 
            "Order #{0} confirmed. Total: ${1,number,#.##}");
        
        InternationalizationService service = new InternationalizationService(messageSource);
        
        String welcome = service.getMessageWithParams("user.welcome", 
            new Object[]{"John", 5}, Locale.ENGLISH);
        String order = service.getMessageWithParams("order.confirmation", 
            new Object[]{"12345", 99.99}, Locale.ENGLISH);
        
        System.out.println(welcome);
        System.out.println(order);
        System.out.println();
    }
    
    private static void demonstrateHierarchicalMessageSource() {
        System.out.println("3. Hierarchical Message Source Demo:");
        
        HierarchicalMessageSourceExample example = new HierarchicalMessageSourceExample();
        MessageSource messageSource = example.createHierarchicalMessageSource();
        
        String appName = messageSource.getMessage("app.name", null, Locale.ENGLISH);
        String appVersion = messageSource.getMessage("app.version", null, Locale.ENGLISH);
        String moduleName = messageSource.getMessage("module.name", null, Locale.ENGLISH);
        
        System.out.println("App Name: " + appName);
        System.out.println("App Version: " + appVersion);
        System.out.println("Module Name: " + moduleName);
        System.out.println();
    }
    
    private static void demonstrateErrorMessages() {
        System.out.println("4. Error Messages Demo:");
        
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("validation.required", Locale.ENGLISH, 
            "Field {0} is required");
        messageSource.addMessage("error.entity.notfound", Locale.ENGLISH, 
            "{0} with id {1} not found");
        messageSource.addMessage("error.access.denied", Locale.ENGLISH, 
            "Access denied to {0}");
        
        ErrorMessageBuilder builder = new ErrorMessageBuilder(messageSource);
        
        System.out.println(builder.buildValidationError("email", "required", Locale.ENGLISH));
        System.out.println(builder.buildNotFoundError("User", "123", Locale.ENGLISH));
        System.out.println(builder.buildAccessDeniedError("admin panel", Locale.ENGLISH));
        System.out.println();
    }
    
    private static void demonstrateMessageKeyGeneration() {
        System.out.println("5. Message Key Generation Demo:");
        
        MessageKeyGenerator generator = new MessageKeyGenerator();
        
        System.out.println("Validation Key: " + 
            generator.generateValidationKey("user", "email", "format"));
        System.out.println("Error Key: " + 
            generator.generateErrorKey("database", "connection"));
        System.out.println("Label Key: " + 
            generator.generateLabelKey("user", "profile", "name"));
        System.out.println("Button Key: " + 
            generator.generateButtonKey("submit"));
        System.out.println();
    }
    
    private static void demonstrateLocalizedContent() {
        System.out.println("6. Localized Content Demo:");
        
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("label.product.prod001.title", Locale.ENGLISH, 
            "Wireless Headphones");
        messageSource.addMessage("label.product.prod001.description", Locale.ENGLISH, 
            "Premium noise-cancelling wireless headphones");
        
        LocalizedContentProvider provider = new LocalizedContentProvider(messageSource);
        LocalizedContent content = provider.getContent("product", "prod001", Locale.ENGLISH);
        
        System.out.println("Content ID: " + content.id());
        System.out.println("Title: " + content.title());
        System.out.println("Description: " + content.description());
        System.out.println();
    }
    
    private static void demonstrateEmailTemplates() {
        System.out.println("7. Email Template Demo:");
        
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("email.welcome.subject", Locale.ENGLISH, 
            "Welcome to {0}!");
        messageSource.addMessage("email.welcome.body", Locale.ENGLISH, 
            "Dear {1},\n\nThank you for joining {0}. We're excited to have you!");
        
        EmailTemplateMessageResolver resolver = new EmailTemplateMessageResolver(messageSource);
        EmailTemplate template = resolver.resolveTemplate("welcome", 
            new Object[]{"MyApp", "John Doe"}, Locale.ENGLISH);
        
        System.out.println("Template: " + template.name());
        System.out.println("Subject: " + template.subject());
        System.out.println("Body:\n" + template.body());
    }
}
