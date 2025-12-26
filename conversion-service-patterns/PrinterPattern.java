package com.example.conversion;

import org.springframework.format.Printer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Printer Pattern
 * ===============
 * 
 * Demonstrates the Printer<T> interface for converting objects to
 * their String representation with locale-aware formatting.
 * 
 * Key Concepts:
 * ------------
 * 1. Printer<T> - Object to String conversion
 * 2. print(T object, Locale locale) - Format method
 * 3. Locale-Sensitive - Different formats per locale
 * 4. Display Formatting - Human-readable output
 * 5. Read-Only - No parsing, only formatting
 * 
 * Differences from Formatter:
 * --------------------------
 * - Printer: One-way (Object -> String)
 * - Formatter: Two-way (Object <-> String)
 * - Use Printer when parsing not needed
 * 
 * When to Use:
 * -----------
 * - Display-only formatting
 * - Report generation
 * - Logging output
 * - Email templates
 * - Export functionality
 * - No need for parsing
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class PrinterPattern implements Printer<LocalDate> {
    
    @Override
    public String print(LocalDate date, Locale locale) {
        if (date == null) {
            return "";
        }
        
        // Different format based on locale
        DateTimeFormatter formatter = locale.equals(Locale.US)
            ? DateTimeFormatter.ofPattern("MMM dd, yyyy")
            : DateTimeFormatter.ofPattern("dd MMM yyyy");
            
        return date.format(formatter);
    }
}

/**
 * Example 2: Money Printer
 * Formats amounts with currency symbol
 */
@Component
class MoneyPrinter implements Printer<BigDecimal> {
    
    @Override
    public String print(BigDecimal amount, Locale locale) {
        if (amount == null) {
            return "";
        }
        
        String symbol = locale.equals(Locale.US) ? "$" : "€";
        return symbol + String.format("%,.2f", amount);
    }
}

/**
 * Example 3: Duration Printer
 * Formats duration in human-readable format
 */
@Component
class DurationPrinter implements Printer<LocalDateTime> {
    
    @Override
    public String print(LocalDateTime dateTime, Locale locale) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        
        if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else {
            return "just now";
        }
    }
}

/**
 * Example 4: File Size Printer
 * Formats byte sizes in human-readable format
 */
@Component
class FileSizePrinter implements Printer<Long> {
    
    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB"};
    
    @Override
    public String print(Long bytes, Locale locale) {
        if (bytes == null || bytes < 0) {
            return "0 B";
        }
        
        if (bytes < 1024) {
            return bytes + " B";
        }
        
        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
        unitIndex = Math.min(unitIndex, UNITS.length - 1);
        
        double size = bytes / Math.pow(1024, unitIndex);
        return String.format("%.2f %s", size, UNITS[unitIndex]);
    }
}

/**
 * Example 5: Percentage Printer
 * Formats decimal as percentage
 */
@Component
class PercentagePrinter implements Printer<Double> {
    
    private final int decimalPlaces;
    
    public PercentagePrinter() {
        this(2);
    }
    
    public PercentagePrinter(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }
    
    @Override
    public String print(Double value, Locale locale) {
        if (value == null) {
            return "0%";
        }
        
        String format = "%." + decimalPlaces + "f%%";
        return String.format(locale, format, value * 100);
    }
}

/**
 * Example 6: Boolean Printer
 * Custom boolean representations
 */
@Component
class BooleanPrinter implements Printer<Boolean> {
    
    private final String trueText;
    private final String falseText;
    
    public BooleanPrinter() {
        this("Yes", "No");
    }
    
    public BooleanPrinter(String trueText, String falseText) {
        this.trueText = trueText;
        this.falseText = falseText;
    }
    
    @Override
    public String print(Boolean value, Locale locale) {
        if (value == null) {
            return "";
        }
        return value ? trueText : falseText;
    }
}

/**
 * Example 7: Enum Printer
 * Pretty-prints enum values
 */
enum Status {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}

@Component
class EnumPrinter implements Printer<Status> {
    
    @Override
    public String print(Status status, Locale locale) {
        if (status == null) {
            return "";
        }
        
        // Convert ENUM_CONSTANT to "Enum Constant"
        String name = status.name();
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(word.charAt(0))
                  .append(word.substring(1).toLowerCase());
        }
        
        return result.toString();
    }
}

/**
 * Example 8: Address Printer
 * Multi-line address formatting
 */
class Address {
    String street;
    String city;
    String state;
    String zipCode;
    String country;
    
    public Address(String street, String city, String state, 
                   String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
}

@Component
class AddressPrinter implements Printer<Address> {
    
    @Override
    public String print(Address address, Locale locale) {
        if (address == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (locale.equals(Locale.US)) {
            // US Format:
            // 123 Main St
            // New York, NY 10001
            // USA
            sb.append(address.street).append("\n")
              .append(address.city).append(", ")
              .append(address.state).append(" ")
              .append(address.zipCode).append("\n")
              .append(address.country);
        } else {
            // European Format:
            // 123 Main St
            // 10001 New York
            // USA
            sb.append(address.street).append("\n")
              .append(address.zipCode).append(" ")
              .append(address.city).append("\n")
              .append(address.country);
        }
        
        return sb.toString();
    }
}

/**
 * Usage Examples
 */
class PrinterUsageExamples {
    
    public static void main(String[] args) {
        // Date printer
        PrinterPattern datePrinter = new PrinterPattern();
        LocalDate date = LocalDate.of(2024, 1, 15);
        System.out.println("US: " + datePrinter.print(date, Locale.US));
        System.out.println("UK: " + datePrinter.print(date, Locale.UK));
        
        // Money printer
        MoneyPrinter moneyPrinter = new MoneyPrinter();
        BigDecimal amount = new BigDecimal("1234.56");
        System.out.println("Amount: " + moneyPrinter.print(amount, Locale.US));
        
        // File size printer
        FileSizePrinter sizePrinter = new FileSizePrinter();
        System.out.println("Size: " + sizePrinter.print(1048576L, Locale.getDefault()));
        
        // Percentage printer
        PercentagePrinter pctPrinter = new PercentagePrinter();
        System.out.println("Percent: " + pctPrinter.print(0.856, Locale.getDefault()));
        
        // Boolean printer
        BooleanPrinter boolPrinter = new BooleanPrinter("Active", "Inactive");
        System.out.println("Status: " + boolPrinter.print(true, Locale.getDefault()));
        
        // Enum printer
        EnumPrinter enumPrinter = new EnumPrinter();
        System.out.println("Status: " + enumPrinter.print(Status.IN_PROGRESS, Locale.getDefault()));
        
        // Address printer
        AddressPrinter addrPrinter = new AddressPrinter();
        Address address = new Address("123 Main St", "New York", "NY", "10001", "USA");
        System.out.println("US Format:\n" + addrPrinter.print(address, Locale.US));
        System.out.println("\nEU Format:\n" + addrPrinter.print(address, Locale.GERMANY));
    }
}
