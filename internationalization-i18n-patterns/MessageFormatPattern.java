package com.spring.patterns.internationalization;

import java.text.*;
import java.util.*;

/**
 * Message Format Pattern
 * 
 * Demonstrates MessageFormat usage for parameterized internationalization:
 * - Simple parameter substitution
 * - Number formatting
 * - Date/Time formatting
 * - Choice format (pluralization)
 * - Nested formats
 * 
 * Use Cases:
 * 1. Parameterized messages
 * 2. Pluralization
 * 3. Gender-specific messages
 * 4. Dynamic content formatting
 * 5. Complex message patterns
 */

/**
 * Message Format Service
 */
class MessageFormatService {
    
    private final Locale locale;
    
    public MessageFormatService(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Format simple message with parameters
     */
    public String formatSimple(String pattern, Object... args) {
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(args);
    }
    
    /**
     * Format message with numbers
     */
    public String formatWithNumbers(String pattern, Number... numbers) {
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(numbers);
    }
    
    /**
     * Format message with dates
     */
    public String formatWithDates(String pattern, Date... dates) {
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(dates);
    }
    
    /**
     * Format message with mixed parameters
     */
    public String formatMixed(String pattern, Object... args) {
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(args);
    }
}

/**
 * Choice Format Handler
 * Handles pluralization and choice-based formatting
 */
class ChoiceFormatHandler {
    
    private final Locale locale;
    
    public ChoiceFormatHandler(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Format with pluralization
     */
    public String formatPlural(int count, String singularForm, String pluralForm) {
        String pattern = "{0} " + buildChoicePattern(singularForm, pluralForm);
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{count});
    }
    
    /**
     * Format file count message
     */
    public String formatFileCount(int count) {
        String pattern = "There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}.";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{count});
    }
    
    /**
     * Format item count with choice
     */
    public String formatItemCount(int count) {
        String pattern = "{0,choice,0#No items|1#One item|1<{0,number,integer} items} in cart.";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{count});
    }
    
    /**
     * Format with gender-specific messages
     */
    public String formatGenderMessage(String gender, String name) {
        String pattern = "{0,choice,0#Dear customer|1#Dear Mr. {1}|2#Dear Ms. {1}}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        int genderCode = "male".equals(gender) ? 1 : ("female".equals(gender) ? 2 : 0);
        return formatter.format(new Object[]{genderCode, name});
    }
    
    private String buildChoicePattern(String singular, String plural) {
        return "{0,choice,0#" + plural + "|1#" + singular + "|1<" + plural + "}";
    }
}

/**
 * Number Format Handler
 */
class NumberFormatHandler {
    
    private final Locale locale;
    
    public NumberFormatHandler(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Format integer numbers
     */
    public String formatInteger(int number) {
        String pattern = "Count: {0,number,integer}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{number});
    }
    
    /**
     * Format decimal numbers
     */
    public String formatDecimal(double number) {
        String pattern = "Value: {0,number,#.##}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{number});
    }
    
    /**
     * Format percentage
     */
    public String formatPercentage(double value) {
        String pattern = "Progress: {0,number,percent}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{value});
    }
    
    /**
     * Format currency
     */
    public String formatCurrency(double amount) {
        String pattern = "Total: {0,number,currency}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{amount});
    }
    
    /**
     * Format with custom number pattern
     */
    public String formatCustom(double number, String pattern) {
        String messagePattern = "Value: {0,number," + pattern + "}";
        MessageFormat formatter = new MessageFormat(messagePattern, locale);
        return formatter.format(new Object[]{number});
    }
}

/**
 * Date/Time Format Handler
 */
class DateTimeFormatHandler {
    
    private final Locale locale;
    
    public DateTimeFormatHandler(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Format date with short style
     */
    public String formatDateShort(Date date) {
        String pattern = "Date: {0,date,short}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{date});
    }
    
    /**
     * Format date with medium style
     */
    public String formatDateMedium(Date date) {
        String pattern = "Date: {0,date,medium}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{date});
    }
    
    /**
     * Format date with long style
     */
    public String formatDateLong(Date date) {
        String pattern = "Date: {0,date,long}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{date});
    }
    
    /**
     * Format date with full style
     */
    public String formatDateFull(Date date) {
        String pattern = "Date: {0,date,full}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{date});
    }
    
    /**
     * Format time
     */
    public String formatTime(Date date) {
        String pattern = "Time: {0,time,short}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{date});
    }
    
    /**
     * Format date and time
     */
    public String formatDateTime(Date date) {
        String pattern = "Scheduled for: {0,date,long} at {0,time,short}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{date});
    }
    
    /**
     * Format with custom date pattern
     */
    public String formatCustomDate(Date date, String datePattern) {
        String pattern = "Date: {0,date," + datePattern + "}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{date});
    }
}

/**
 * Complex Message Builder
 * Builds complex messages with multiple parameters
 */
class ComplexMessageBuilder {
    
    private final Locale locale;
    
    public ComplexMessageBuilder(Locale locale) {
        this.locale = locale;
    }
    
    /**
     * Build email notification message
     */
    public String buildEmailNotification(String recipient, int messageCount, Date lastChecked) {
        String pattern = "Dear {0}, you have {1,choice,0#no new messages|1#one new message|1<{1,number,integer} new messages} since {2,date,medium}.";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{recipient, messageCount, lastChecked});
    }
    
    /**
     * Build order confirmation message
     */
    public String buildOrderConfirmation(String orderId, double total, int itemCount) {
        String pattern = "Order #{0} confirmed. Total: {1,number,currency} for {2,choice,1#one item|1<{2,number,integer} items}.";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{orderId, total, itemCount});
    }
    
    /**
     * Build account summary message
     */
    public String buildAccountSummary(String accountHolder, double balance, Date lastTransaction) {
        String pattern = "Account holder: {0}\nBalance: {1,number,currency}\nLast transaction: {2,date,full}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{accountHolder, balance, lastTransaction});
    }
    
    /**
     * Build meeting reminder
     */
    public String buildMeetingReminder(String title, Date meetingTime, String location, int attendees) {
        String pattern = "Reminder: {0}\nWhen: {1,date,long} at {1,time,short}\nWhere: {2}\nAttendees: {3,number,integer}";
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(new Object[]{title, meetingTime, location, attendees});
    }
}

/**
 * Message Template Manager
 */
class MessageTemplateManager {
    
    private final Map<String, String> templates = new HashMap<>();
    private final Locale locale;
    
    public MessageTemplateManager(Locale locale) {
        this.locale = locale;
        initializeTemplates();
    }
    
    private void initializeTemplates() {
        templates.put("welcome", "Welcome, {0}! You registered on {1,date,long}.");
        templates.put("purchase", "Thank you for your purchase of {0,number,currency}!");
        templates.put("reminder", "You have {0,choice,0#no pending tasks|1#one pending task|1<{0,number,integer} pending tasks}.");
        templates.put("notification", "{0} sent you a message on {1,date,short} at {1,time,short}.");
    }
    
    public String format(String templateName, Object... args) {
        String pattern = templates.get(templateName);
        if (pattern == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        MessageFormat formatter = new MessageFormat(pattern, locale);
        return formatter.format(args);
    }
    
    public void addTemplate(String name, String pattern) {
        templates.put(name, pattern);
    }
}

/**
 * Localized Message Formatter
 */
class LocalizedMessageFormatter {
    
    private final Map<Locale, MessageTemplateManager> managers = new HashMap<>();
    
    public void registerTemplate(Locale locale, String name, String pattern) {
        MessageTemplateManager manager = managers.computeIfAbsent(
            locale, MessageTemplateManager::new
        );
        manager.addTemplate(name, pattern);
    }
    
    public String format(Locale locale, String templateName, Object... args) {
        MessageTemplateManager manager = managers.get(locale);
        if (manager == null) {
            manager = managers.get(Locale.getDefault());
        }
        return manager.format(templateName, args);
    }
}

/**
 * Demonstration class
 */
public class MessageFormatPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Message Format Pattern Demo ===\n");
        
        // 1. Simple Message Formatting
        demonstrateSimpleFormatting();
        
        // 2. Choice Format (Pluralization)
        demonstrateChoiceFormat();
        
        // 3. Number Formatting
        demonstrateNumberFormatting();
        
        // 4. Date/Time Formatting
        demonstrateDateTimeFormatting();
        
        // 5. Complex Messages
        demonstrateComplexMessages();
        
        // 6. Message Templates
        demonstrateMessageTemplates();
        
        // 7. Localized Formatting
        demonstrateLocalizedFormatting();
    }
    
    private static void demonstrateSimpleFormatting() {
        System.out.println("1. Simple Message Formatting:");
        
        MessageFormatService service = new MessageFormatService(Locale.US);
        
        System.out.println(service.formatSimple("Hello, {0}!", "World"));
        System.out.println(service.formatSimple("Welcome {0}, you are user #{1}.", "John", 12345));
        System.out.println(service.formatSimple("{0} + {1} = {2}", 5, 3, 8));
        System.out.println();
    }
    
    private static void demonstrateChoiceFormat() {
        System.out.println("2. Choice Format (Pluralization):");
        
        ChoiceFormatHandler handler = new ChoiceFormatHandler(Locale.US);
        
        for (int i = 0; i <= 3; i++) {
            System.out.println(handler.formatFileCount(i));
        }
        
        System.out.println();
        
        for (int i = 0; i <= 2; i++) {
            System.out.println(handler.formatItemCount(i));
        }
        
        System.out.println();
        
        System.out.println(handler.formatGenderMessage("male", "Smith"));
        System.out.println(handler.formatGenderMessage("female", "Johnson"));
        System.out.println(handler.formatGenderMessage("other", null));
        System.out.println();
    }
    
    private static void demonstrateNumberFormatting() {
        System.out.println("3. Number Formatting:");
        
        NumberFormatHandler handler = new NumberFormatHandler(Locale.US);
        
        System.out.println(handler.formatInteger(1234567));
        System.out.println(handler.formatDecimal(1234.56789));
        System.out.println(handler.formatPercentage(0.75));
        System.out.println(handler.formatCurrency(1234.56));
        System.out.println(handler.formatCustom(1234567.89, "#,##0.00"));
        System.out.println();
        
        // Different locales
        System.out.println("German locale:");
        NumberFormatHandler germanHandler = new NumberFormatHandler(Locale.GERMANY);
        System.out.println(germanHandler.formatDecimal(1234.56));
        System.out.println(germanHandler.formatCurrency(1234.56));
        System.out.println();
    }
    
    private static void demonstrateDateTimeFormatting() {
        System.out.println("4. Date/Time Formatting:");
        
        DateTimeFormatHandler handler = new DateTimeFormatHandler(Locale.US);
        Date now = new Date();
        
        System.out.println(handler.formatDateShort(now));
        System.out.println(handler.formatDateMedium(now));
        System.out.println(handler.formatDateLong(now));
        System.out.println(handler.formatTime(now));
        System.out.println(handler.formatDateTime(now));
        System.out.println();
    }
    
    private static void demonstrateComplexMessages() {
        System.out.println("5. Complex Messages:");
        
        ComplexMessageBuilder builder = new ComplexMessageBuilder(Locale.US);
        Date now = new Date();
        
        System.out.println(builder.buildEmailNotification("John", 5, now));
        System.out.println();
        
        System.out.println(builder.buildOrderConfirmation("ORD-12345", 159.99, 3));
        System.out.println();
        
        System.out.println(builder.buildAccountSummary("Jane Doe", 5432.10, now));
        System.out.println();
        
        System.out.println(builder.buildMeetingReminder("Project Review", now, "Conference Room A", 8));
        System.out.println();
    }
    
    private static void demonstrateMessageTemplates() {
        System.out.println("6. Message Templates:");
        
        MessageTemplateManager manager = new MessageTemplateManager(Locale.US);
        Date now = new Date();
        
        System.out.println(manager.format("welcome", "Alice", now));
        System.out.println(manager.format("purchase", 99.99));
        System.out.println(manager.format("reminder", 3));
        System.out.println(manager.format("notification", "Bob", now));
        System.out.println();
    }
    
    private static void demonstrateLocalizedFormatting() {
        System.out.println("7. Localized Formatting:");
        
        LocalizedMessageFormatter formatter = new LocalizedMessageFormatter();
        
        // English templates
        formatter.registerTemplate(Locale.US, "greeting", "Hello, {0}!");
        formatter.registerTemplate(Locale.US, "farewell", "Goodbye, {0}!");
        
        // Spanish templates
        formatter.registerTemplate(new Locale("es"), "greeting", "¡Hola, {0}!");
        formatter.registerTemplate(new Locale("es"), "farewell", "¡Adiós, {0}!");
        
        // French templates
        formatter.registerTemplate(Locale.FRENCH, "greeting", "Bonjour, {0}!");
        formatter.registerTemplate(Locale.FRENCH, "farewell", "Au revoir, {0}!");
        
        Locale[] locales = {Locale.US, new Locale("es"), Locale.FRENCH};
        
        for (Locale locale : locales) {
            System.out.println(locale.getDisplayName() + ":");
            System.out.println("  " + formatter.format(locale, "greeting", "World"));
            System.out.println("  " + formatter.format(locale, "farewell", "Friend"));
        }
    }
}
