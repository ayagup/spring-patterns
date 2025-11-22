package com.example.conversion.formatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.number.CurrencyStyleFormatter;
import org.springframework.format.number.NumberStyleFormatter;
import org.springframework.format.number.PercentStyleFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Formatter Pattern - Demonstrates Spring's Formatting System
 * 
 * This pattern shows how to:
 * 1. Implement Formatter interface for text formatting
 * 2. Create custom formatters for domain objects
 * 3. Use annotation-based formatting
 * 4. Format dates and times
 * 5. Format numbers and currency
 * 6. Register formatters globally
 * 7. Use PrinterParser pattern
 * 8. Handle localization
 * 9. Format collections
 * 10. Create reusable formatters
 * 
 * Key Concepts:
 * - Formatter<T>: Interface for parsing and printing
 * - print(): Converts object to String
 * - parse(): Converts String to object
 * - @DateTimeFormat: Annotation for date formatting
 * - @NumberFormat: Annotation for number formatting
 * 
 * Formatter Types:
 * 1. DateFormatter - Date formatting
 * 2. NumberStyleFormatter - Number formatting
 * 3. CurrencyStyleFormatter - Currency formatting
 * 4. PercentStyleFormatter - Percentage formatting
 * 5. Custom Formatters - Domain-specific formatting
 * 
 * Use Cases:
 * - Display formatting in web applications
 * - Input parsing from forms
 * - Locale-aware formatting
 * - Currency and number display
 * - Date/time presentation
 * 
 * Dependencies:
 * - spring-context
 * - spring-web
 * - spring-webmvc
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class FormatterPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(FormatterPattern.class, args);
        demonstrateFormatters(context);
    }
    
    /**
     * Demonstrates various formatter scenarios
     */
    private static void demonstrateFormatters(org.springframework.context.ApplicationContext context) {
        System.out.println("=== Formatter Pattern Demonstrations ===\n");
        
        // Demo 1: Date formatters
        demonstrateDateFormatters();
        
        // Demo 2: Number formatters
        demonstrateNumberFormatters();
        
        // Demo 3: Currency formatters
        demonstrateCurrencyFormatters();
        
        // Demo 4: Custom formatters
        demonstrateCustomFormatters();
        
        // Demo 5: Locale-aware formatters
        demonstrateLocaleFormatters();
    }
    
    /**
     * Demonstrates date formatters
     */
    private static void demonstrateDateFormatters() {
        System.out.println("1. Date Formatters:");
        
        DateFormatter formatter = new DateFormatter("yyyy-MM-dd");
        LocalDate date = LocalDate.now();
        
        String formatted = formatter.print(date, Locale.US);
        System.out.println("   Formatted date: " + formatted);
        
        try {
            LocalDate parsed = formatter.parse("2024-12-25", Locale.US);
            System.out.println("   Parsed date: " + parsed);
        } catch (ParseException e) {
            System.out.println("   Parse error: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Demonstrates number formatters
     */
    private static void demonstrateNumberFormatters() {
        System.out.println("2. Number Formatters:");
        
        NumberStyleFormatter formatter = new NumberStyleFormatter();
        Number number = 1234567.89;
        
        String formatted = formatter.print(number, Locale.US);
        System.out.println("   Formatted number (US): " + formatted);
        
        formatted = formatter.print(number, Locale.GERMANY);
        System.out.println("   Formatted number (DE): " + formatted);
        
        System.out.println();
    }
    
    /**
     * Demonstrates currency formatters
     */
    private static void demonstrateCurrencyFormatters() {
        System.out.println("3. Currency Formatters:");
        
        CurrencyStyleFormatter formatter = new CurrencyStyleFormatter();
        BigDecimal amount = new BigDecimal("1234.56");
        
        String formatted = formatter.print(amount, Locale.US);
        System.out.println("   Formatted currency (US): " + formatted);
        
        formatted = formatter.print(amount, Locale.UK);
        System.out.println("   Formatted currency (UK): " + formatted);
        
        System.out.println();
    }
    
    /**
     * Demonstrates custom formatters
     */
    private static void demonstrateCustomFormatters() {
        System.out.println("4. Custom Formatters:");
        
        PhoneNumberFormatter formatter = new PhoneNumberFormatter();
        PhoneNumber phone = new PhoneNumber("1234567890");
        
        String formatted = formatter.print(phone, Locale.US);
        System.out.println("   Formatted phone: " + formatted);
        
        try {
            PhoneNumber parsed = formatter.parse("(123) 456-7890", Locale.US);
            System.out.println("   Parsed phone: " + parsed);
        } catch (ParseException e) {
            System.out.println("   Parse error: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Demonstrates locale-aware formatters
     */
    private static void demonstrateLocaleFormatters() {
        System.out.println("5. Locale-Aware Formatters:");
        
        PercentStyleFormatter formatter = new PercentStyleFormatter();
        Number percent = 0.75;
        
        String formattedUS = formatter.print(percent, Locale.US);
        String formattedFR = formatter.print(percent, Locale.FRANCE);
        
        System.out.println("   Percent (US): " + formattedUS);
        System.out.println("   Percent (FR): " + formattedFR);
        
        System.out.println();
    }
}

// ============================================================================
// Custom Formatters
// ============================================================================

/**
 * Phone number formatter
 */
@Component
class PhoneNumberFormatter implements Formatter<PhoneNumber> {
    
    @Override
    public String print(PhoneNumber phone, Locale locale) {
        String number = phone.getNumber();
        if (number.length() == 10) {
            return String.format("(%s) %s-%s",
                number.substring(0, 3),
                number.substring(3, 6),
                number.substring(6));
        }
        return number;
    }
    
    @Override
    public PhoneNumber parse(String text, Locale locale) throws ParseException {
        // Remove all non-digits
        String cleaned = text.replaceAll("[^0-9]", "");
        if (cleaned.length() != 10) {
            throw new ParseException("Invalid phone number format", 0);
        }
        return new PhoneNumber(cleaned);
    }
}

/**
 * Credit card formatter
 */
@Component
class CreditCardFormatter implements Formatter<CreditCard> {
    
    @Override
    public String print(CreditCard card, Locale locale) {
        String number = card.getNumber();
        // Mask all but last 4 digits
        if (number.length() >= 4) {
            String masked = "**** **** **** " + number.substring(number.length() - 4);
            return masked;
        }
        return number;
    }
    
    @Override
    public CreditCard parse(String text, Locale locale) throws ParseException {
        String cleaned = text.replaceAll("\\s+", "");
        if (cleaned.length() < 13 || cleaned.length() > 19) {
            throw new ParseException("Invalid credit card number", 0);
        }
        return new CreditCard(cleaned);
    }
}

/**
 * Email formatter
 */
@Component
class EmailFormatter implements Formatter<Email> {
    
    @Override
    public String print(Email email, Locale locale) {
        return email.getAddress().toLowerCase();
    }
    
    @Override
    public Email parse(String text, Locale locale) throws ParseException {
        if (!text.contains("@")) {
            throw new ParseException("Invalid email format", 0);
        }
        return new Email(text.trim().toLowerCase());
    }
}

/**
 * Money formatter
 */
@Component
class MoneyFormatter implements Formatter<Money> {
    
    @Override
    public String print(Money money, Locale locale) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        return money.getCurrency() + " " + currencyFormat.format(money.getAmount());
    }
    
    @Override
    public Money parse(String text, Locale locale) throws ParseException {
        String[] parts = text.split("\\s+");
        if (parts.length != 2) {
            throw new ParseException("Invalid money format. Expected: CURRENCY AMOUNT", 0);
        }
        
        try {
            NumberFormat format = NumberFormat.getInstance(locale);
            Number amount = format.parse(parts[1]);
            return new Money(parts[0], new BigDecimal(amount.toString()));
        } catch (ParseException e) {
            throw new ParseException("Invalid amount format", 0);
        }
    }
}

/**
 * SSN (Social Security Number) formatter
 */
@Component
class SSNFormatter implements Formatter<SSN> {
    
    @Override
    public String print(SSN ssn, Locale locale) {
        String number = ssn.getNumber();
        if (number.length() == 9) {
            return String.format("***-**-%s", number.substring(5));
        }
        return "***-**-****";
    }
    
    @Override
    public SSN parse(String text, Locale locale) throws ParseException {
        String cleaned = text.replaceAll("[^0-9]", "");
        if (cleaned.length() != 9) {
            throw new ParseException("Invalid SSN format", 0);
        }
        return new SSN(cleaned);
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * Phone number value object
 */
class PhoneNumber {
    private String number;
    
    public PhoneNumber(String number) {
        this.number = number;
    }
    
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    
    @Override
    public String toString() {
        return number;
    }
}

/**
 * Credit card value object
 */
class CreditCard {
    private String number;
    
    public CreditCard(String number) {
        this.number = number;
    }
    
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
}

/**
 * Email value object
 */
class Email {
    private String address;
    
    public Email(String address) {
        this.address = address;
    }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}

/**
 * Money value object
 */
class Money {
    private String currency;
    private BigDecimal amount;
    
    public Money(String currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
    }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

/**
 * SSN value object
 */
class SSN {
    private String number;
    
    public SSN(String number) {
        this.number = number;
    }
    
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
}

// ============================================================================
// Formatter Configuration
// ============================================================================

/**
 * Configures custom formatters
 */
@Configuration
class FormatterConfiguration implements WebMvcConfigurer {
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Register custom formatters
        registry.addFormatter(new PhoneNumberFormatter());
        registry.addFormatter(new CreditCardFormatter());
        registry.addFormatter(new EmailFormatter());
        registry.addFormatter(new MoneyFormatter());
        registry.addFormatter(new SSNFormatter());
        
        // Register date formatters
        DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd");
        registry.addFormatter(dateFormatter);
        
        // Register number formatters
        registry.addFormatter(new NumberStyleFormatter());
        registry.addFormatter(new CurrencyStyleFormatter());
        registry.addFormatter(new PercentStyleFormatter());
    }
}

// ============================================================================
// DTO with Formatting Annotations
// ============================================================================

/**
 * DTO demonstrating formatting annotations
 */
class FormattedDto {
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @NumberFormat(style = NumberFormat.Style.CURRENCY)
    private BigDecimal price;
    
    @NumberFormat(style = NumberFormat.Style.PERCENT)
    private BigDecimal taxRate;
    
    @NumberFormat(pattern = "#,###.##")
    private BigDecimal amount;
    
    // Getters and setters
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating formatter usage
 */
@RestController
@RequestMapping("/api/formatters")
class FormatterController {
    
    private final PhoneNumberFormatter phoneFormatter;
    private final CreditCardFormatter cardFormatter;
    private final MoneyFormatter moneyFormatter;
    
    public FormatterController(PhoneNumberFormatter phoneFormatter,
                              CreditCardFormatter cardFormatter,
                              MoneyFormatter moneyFormatter) {
        this.phoneFormatter = phoneFormatter;
        this.cardFormatter = cardFormatter;
        this.moneyFormatter = moneyFormatter;
    }
    
    /**
     * Formats phone number
     */
    @GetMapping("/phone")
    public ResponseEntity<Map<String, String>> formatPhone(@RequestParam String number) {
        try {
            PhoneNumber phone = phoneFormatter.parse(number, Locale.US);
            String formatted = phoneFormatter.print(phone, Locale.US);
            
            Map<String, String> response = new HashMap<>();
            response.put("original", number);
            response.put("formatted", formatted);
            return ResponseEntity.ok(response);
        } catch (ParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Formats credit card
     */
    @GetMapping("/card")
    public ResponseEntity<Map<String, String>> formatCard(@RequestParam String number) {
        try {
            CreditCard card = cardFormatter.parse(number, Locale.US);
            String formatted = cardFormatter.print(card, Locale.US);
            
            Map<String, String> response = new HashMap<>();
            response.put("masked", formatted);
            return ResponseEntity.ok(response);
        } catch (ParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Formats money with locale
     */
    @GetMapping("/money")
    public ResponseEntity<Map<String, String>> formatMoney(
            @RequestParam String currency,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "US") String locale) {
        
        Money money = new Money(currency, amount);
        Locale loc = "US".equals(locale) ? Locale.US :
                     "UK".equals(locale) ? Locale.UK :
                     "FR".equals(locale) ? Locale.FRANCE : Locale.US;
        
        String formatted = moneyFormatter.print(money, loc);
        
        Map<String, String> response = new HashMap<>();
        response.put("formatted", formatted);
        response.put("locale", loc.toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Demonstrates date formatting with annotation
     */
    @PostMapping("/dto")
    public ResponseEntity<FormattedDto> processDto(@RequestBody FormattedDto dto) {
        return ResponseEntity.ok(dto);
    }
}
