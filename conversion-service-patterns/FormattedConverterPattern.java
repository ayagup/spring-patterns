package com.example.conversion;

import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.number.CurrencyStyleFormatter;
import org.springframework.format.number.NumberStyleFormatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatted Converter Pattern
 * ============================
 * 
 * Demonstrates formatters for locale-sensitive conversions between
 * String and Object types with configurable formatting rules.
 * 
 * Key Concepts:
 * ------------
 * 1. Formatter<T> - Bidirectional String <-> Object conversion
 * 2. Locale-Aware - Formatting based on locale
 * 3. print() - Object to String
 * 4. parse() - String to Object
 * 5. Format Annotations - @DateTimeFormat, @NumberFormat
 * 
 * When to Use:
 * -----------
 * - User-facing string representations
 * - Locale-specific formatting
 * - Date/time formatting
 * - Number/currency formatting
 * - Custom display formats
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class FormattedConverterPattern implements Formatter<LocalDate> {
    
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public String print(LocalDate date, Locale locale) {
        if (date == null) {
            return "";
        }
        return date.format(FORMATTER);
    }
    
    @Override
    public LocalDate parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), FORMATTER);
        } catch (Exception e) {
            throw new ParseException("Unable to parse date: " + text, 0);
        }
    }
}

/**
 * Example 2: Currency Formatter
 */
@Component
class CurrencyFormatter implements Formatter<Double> {
    
    @Override
    public String print(Double amount, Locale locale) {
        if (amount == null) {
            return "";
        }
        CurrencyStyleFormatter formatter = new CurrencyStyleFormatter();
        return formatter.print(amount, locale);
    }
    
    @Override
    public Double parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        CurrencyStyleFormatter formatter = new CurrencyStyleFormatter();
        return formatter.parse(text.trim(), locale).doubleValue();
    }
}

/**
 * Example 3: Percentage Formatter
 */
@Component
class PercentageFormatter implements Formatter<Double> {
    
    @Override
    public String print(Double value, Locale locale) {
        if (value == null) {
            return "";
        }
        NumberStyleFormatter formatter = new NumberStyleFormatter("##.##%");
        return formatter.print(value * 100, locale);
    }
    
    @Override
    public Double parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String cleaned = text.trim().replace("%", "");
        NumberStyleFormatter formatter = new NumberStyleFormatter();
        return formatter.parse(cleaned, locale).doubleValue() / 100;
    }
}

/**
 * Example 4: Custom DateTime Formatter
 */
@Component
class CustomDateTimeFormatter implements Formatter<LocalDateTime> {
    
    @Override
    public String print(LocalDateTime dateTime, Locale locale) {
        if (dateTime == null) {
            return "";
        }
        
        DateTimeFormatter formatter = locale.equals(Locale.US) 
            ? DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")
            : DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
        return dateTime.format(formatter);
    }
    
    @Override
    public LocalDateTime parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        DateTimeFormatter formatter = locale.equals(Locale.US)
            ? DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")
            : DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
        try {
            return LocalDateTime.parse(text.trim(), formatter);
        } catch (Exception e) {
            throw new ParseException("Unable to parse datetime: " + text, 0);
        }
    }
}

/**
 * Usage Examples
 */
class FormatterUsageExamples {
    
    public static void main(String[] args) throws ParseException {
        FormattedConverterPattern dateFormatter = new FormattedConverterPattern();
        
        // Format date
        LocalDate date = LocalDate.of(2024, 1, 15);
        String formatted = dateFormatter.print(date, Locale.getDefault());
        System.out.println("Formatted: " + formatted);
        
        // Parse date
        LocalDate parsed = dateFormatter.parse("15/01/2024", Locale.getDefault());
        System.out.println("Parsed: " + parsed);
        
        // Currency
        CurrencyFormatter currFormatter = new CurrencyFormatter();
        System.out.println("Currency: " + currFormatter.print(1234.56, Locale.US));
        
        // Percentage
        PercentageFormatter pctFormatter = new PercentageFormatter();
        System.out.println("Percentage: " + pctFormatter.print(0.845, Locale.getDefault()));
    }
}
