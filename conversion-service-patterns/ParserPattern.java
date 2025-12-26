package com.example.conversion;

import org.springframework.format.Parser;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Parser Pattern
 * ==============
 * 
 * Demonstrates the Parser<T> interface for converting String
 * representations to objects with locale-aware parsing.
 * 
 * Key Concepts:
 * ------------
 * 1. Parser<T> - String to Object conversion
 * 2. parse(String text, Locale locale) - Parse method
 * 3. Locale-Sensitive - Different parsing per locale
 * 4. Input Validation - Error handling for invalid input
 * 5. Flexible Parsing - Accept multiple formats
 * 
 * Differences from Formatter:
 * --------------------------
 * - Parser: One-way (String -> Object)
 * - Formatter: Two-way (Object <-> String)
 * - Use Parser when formatting not needed
 * 
 * When to Use:
 * -----------
 * - Form input parsing
 * - File import processing
 * - API request parsing
 * - Configuration reading
 * - User input validation
 * - No need for formatting
 * 
 * Best Practices:
 * --------------
 * - Validate input before parsing
 * - Provide clear error messages
 * - Handle null and empty strings
 * - Support multiple formats
 * - Use locale for parsing rules
 * - Throw ParseException for invalid input
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ParserPattern implements Parser<LocalDate> {
    
    @Override
    public LocalDate parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.trim();
        
        // Try multiple formats
        DateTimeFormatter[] formatters = locale.equals(Locale.US)
            ? new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
            }
            : new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
            };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        throw new ParseException("Unable to parse date: " + text, 0);
    }
}

/**
 * Example 2: Money Parser
 * Parses currency amounts with or without symbols
 */
@Component
class MoneyParser implements Parser<BigDecimal> {
    
    @Override
    public BigDecimal parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.trim();
        
        // Remove currency symbols and whitespace
        text = text.replaceAll("[$€£¥,\\s]", "");
        
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid money amount: " + text, 0);
        }
    }
}

/**
 * Example 3: Boolean Parser
 * Flexible boolean parsing
 */
@Component
class FlexibleBooleanParser implements Parser<Boolean> {
    
    private static final Pattern TRUE_PATTERN = 
        Pattern.compile("^(true|yes|y|1|on|enabled|active)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FALSE_PATTERN = 
        Pattern.compile("^(false|no|n|0|off|disabled|inactive)$", Pattern.CASE_INSENSITIVE);
    
    @Override
    public Boolean parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.trim();
        
        if (TRUE_PATTERN.matcher(text).matches()) {
            return true;
        } else if (FALSE_PATTERN.matcher(text).matches()) {
            return false;
        } else {
            throw new ParseException("Invalid boolean value: " + text, 0);
        }
    }
}

/**
 * Example 4: Percentage Parser
 * Parses percentage strings
 */
@Component
class PercentageParser implements Parser<Double> {
    
    @Override
    public Double parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.trim();
        
        // Remove % symbol if present
        boolean hasPercent = text.endsWith("%");
        if (hasPercent) {
            text = text.substring(0, text.length() - 1).trim();
        }
        
        try {
            double value = Double.parseDouble(text);
            return hasPercent ? value / 100 : value;
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid percentage: " + text, 0);
        }
    }
}

/**
 * Example 5: File Size Parser
 * Parses human-readable file sizes
 */
@Component
class FileSizeParser implements Parser<Long> {
    
    @Override
    public Long parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.trim().toLowerCase();
        
        // Extract number and unit
        String numberPart = text.replaceAll("[^0-9.]", "");
        String unitPart = text.replaceAll("[0-9.\\s]", "");
        
        if (numberPart.isEmpty()) {
            throw new ParseException("Invalid file size: " + text, 0);
        }
        
        try {
            double value = Double.parseDouble(numberPart);
            long multiplier = getMultiplier(unitPart);
            return (long) (value * multiplier);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid file size: " + text, 0);
        }
    }
    
    private long getMultiplier(String unit) throws ParseException {
        switch (unit.toLowerCase()) {
            case "":
            case "b": return 1L;
            case "kb": return 1024L;
            case "mb": return 1024L * 1024;
            case "gb": return 1024L * 1024 * 1024;
            case "tb": return 1024L * 1024 * 1024 * 1024;
            default: throw new ParseException("Unknown unit: " + unit, 0);
        }
    }
}

/**
 * Example 6: Enum Parser
 * Case-insensitive enum parsing
 */
enum Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Component
class EnumParser implements Parser<Priority> {
    
    @Override
    public Priority parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.trim().toUpperCase().replace(" ", "_");
        
        try {
            return Priority.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid priority: " + text, 0);
        }
    }
}

/**
 * Example 7: DateTime Parser
 * Flexible datetime parsing with multiple formats
 */
@Component
class FlexibleDateTimeParser implements Parser<LocalDateTime> {
    
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"),
    };
    
    @Override
    public LocalDateTime parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.trim();
        
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        throw new ParseException("Unable to parse datetime: " + text, 0);
    }
}

/**
 * Example 8: Email Parser
 * Validates and parses email addresses
 */
class Email {
    private final String localPart;
    private final String domain;
    
    public Email(String localPart, String domain) {
        this.localPart = localPart;
        this.domain = domain;
    }
    
    public String getLocalPart() { return localPart; }
    public String getDomain() { return domain; }
    
    @Override
    public String toString() {
        return localPart + "@" + domain;
    }
}

@Component
class EmailParser implements Parser<Email> {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^([^@]+)@([^@]+)$");
    
    @Override
    public Email parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.trim().toLowerCase();
        
        var matcher = EMAIL_PATTERN.matcher(text);
        if (!matcher.matches()) {
            throw new ParseException("Invalid email format: " + text, 0);
        }
        
        String localPart = matcher.group(1);
        String domain = matcher.group(2);
        
        // Basic validation
        if (localPart.isEmpty() || domain.isEmpty() || !domain.contains(".")) {
            throw new ParseException("Invalid email: " + text, 0);
        }
        
        return new Email(localPart, domain);
    }
}

/**
 * Usage Examples
 */
class ParserUsageExamples {
    
    public static void main(String[] args) throws ParseException {
        // Date parser
        ParserPattern dateParser = new ParserPattern();
        LocalDate date1 = dateParser.parse("01/15/2024", Locale.US);
        LocalDate date2 = dateParser.parse("15/01/2024", Locale.UK);
        System.out.println("US Date: " + date1);
        System.out.println("UK Date: " + date2);
        
        // Money parser
        MoneyParser moneyParser = new MoneyParser();
        BigDecimal amount = moneyParser.parse("$1,234.56", Locale.US);
        System.out.println("Amount: " + amount);
        
        // Boolean parser
        FlexibleBooleanParser boolParser = new FlexibleBooleanParser();
        System.out.println("Yes: " + boolParser.parse("yes", Locale.getDefault()));
        System.out.println("1: " + boolParser.parse("1", Locale.getDefault()));
        System.out.println("enabled: " + boolParser.parse("enabled", Locale.getDefault()));
        
        // Percentage parser
        PercentageParser pctParser = new PercentageParser();
        System.out.println("85%: " + pctParser.parse("85%", Locale.getDefault()));
        System.out.println("0.85: " + pctParser.parse("0.85", Locale.getDefault()));
        
        // File size parser
        FileSizeParser sizeParser = new FileSizeParser();
        System.out.println("1.5 MB: " + sizeParser.parse("1.5 MB", Locale.getDefault()) + " bytes");
        
        // Enum parser
        EnumParser enumParser = new EnumParser();
        System.out.println("Priority: " + enumParser.parse("high", Locale.getDefault()));
        
        // Email parser
        EmailParser emailParser = new EmailParser();
        Email email = emailParser.parse("user@example.com", Locale.getDefault());
        System.out.println("Email: " + email);
        System.out.println("Domain: " + email.getDomain());
    }
}
