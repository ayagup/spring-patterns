package com.example.databinding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Type Conversion Pattern
 * 
 * Demonstrates Spring's type conversion system for data binding.
 * Converters transform source types to target types during binding.
 * 
 * Features:
 * - Converter interface
 * - ConverterFactory
 * - GenericConverter
 * - Formatter (for display)
 * - ConversionService
 * - Custom type conversion
 * 
 * Use Cases:
 * - String to custom object
 * - Enum conversion
 * - Date/Time formatting
 * - Collection conversion
 * - Complex type transformation
 */
@SpringBootApplication
public class TypeConversionPattern {

    public static void main(String[] args) {
        SpringApplication.run(TypeConversionPattern.class, args);
    }

    /**
     * Web MVC Configuration for registering converters
     */
    @Component
    public static class WebConfig implements WebMvcConfigurer {

        @Override
        public void addFormatters(FormatterRegistry registry) {
            // Register converters
            registry.addConverter(new StringToUserConverter());
            registry.addConverter(new StringToMoneyConverter());
            registry.addConverter(new UserToStringConverter());
            
            // Register formatters
            registry.addFormatter(new DateFormatter());
            registry.addFormatter(new MoneyFormatter());
        }
    }

    /**
     * String to User Converter
     */
    public static class StringToUserConverter implements Converter<String, User> {

        @Override
        public User convert(String source) {
            // Format: "id:name:email"
            String[] parts = source.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid user format");
            }
            
            User user = new User();
            user.setId(Long.parseLong(parts[0]));
            user.setName(parts[1]);
            user.setEmail(parts[2]);
            return user;
        }
    }

    /**
     * User to String Converter (reverse)
     */
    public static class UserToStringConverter implements Converter<User, String> {

        @Override
        public String convert(String source) {
            User user = (User) source;
            return user.getId() + ":" + user.getName() + ":" + user.getEmail();
        }
    }

    /**
     * String to Money Converter
     */
    public static class StringToMoneyConverter implements Converter<String, Money> {

        @Override
        public Money convert(String source) {
            // Format: "USD 100.50" or "100.50"
            source = source.trim();
            
            String currency = "USD";
            double amount;
            
            if (source.contains(" ")) {
                String[] parts = source.split(" ");
                currency = parts[0];
                amount = Double.parseDouble(parts[1]);
            } else {
                amount = Double.parseDouble(source);
            }
            
            return new Money(amount, currency);
        }
    }

    /**
     * Date Formatter (bidirectional)
     */
    public static class DateFormatter implements Formatter<LocalDate> {

        private static final DateTimeFormatter FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public LocalDate parse(String text, Locale locale) throws ParseException {
            return LocalDate.parse(text, FORMATTER);
        }

        @Override
        public String print(LocalDate date, Locale locale) {
            return date.format(FORMATTER);
        }
    }

    /**
     * Money Formatter
     */
    public static class MoneyFormatter implements Formatter<Money> {

        @Override
        public Money parse(String text, Locale locale) throws ParseException {
            // Delegate to converter
            return new StringToMoneyConverter().convert(text);
        }

        @Override
        public String print(Money money, Locale locale) {
            return String.format(locale, "%.2f %s", money.getAmount(), money.getCurrency());
        }
    }

    /**
     * String to Enum Converter (built-in, example for reference)
     */
    public static class StringToEnumConverter implements Converter<String, Status> {

        @Override
        public Status convert(String source) {
            try {
                return Status.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + source);
            }
        }
    }

    /**
     * Controller demonstrating type conversion
     */
    @Controller
    public static class ConversionController {

        @GetMapping("/conversion/form")
        public String showForm(Model model) {
            model.addAttribute("user", new User());
            return "conversion/form";
        }

        @PostMapping("/conversion/user")
        public String handleUser(@RequestParam("user") User user, Model model) {
            // Spring automatically converts "1:John:john@example.com" to User
            model.addAttribute("user", user);
            return "conversion/result";
        }

        @PostMapping("/conversion/money")
        public String handleMoney(@RequestParam("amount") Money money, Model model) {
            // Spring converts "USD 100.50" or "100.50" to Money
            model.addAttribute("money", money);
            return "conversion/result";
        }

        @PostMapping("/conversion/date")
        public String handleDate(@RequestParam("date") LocalDate date, Model model) {
            // Spring converts "2024-01-15" to LocalDate
            model.addAttribute("date", date);
            return "conversion/result";
        }

        @PostMapping("/conversion/enum")
        public String handleEnum(@RequestParam("status") Status status, Model model) {
            // Spring converts "ACTIVE" to Status.ACTIVE (built-in)
            model.addAttribute("status", status);
            return "conversion/result";
        }
    }

    /**
     * User model
     */
    public static class User {
        private Long id;
        private String name;
        private String email;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /**
     * Money value object
     */
    public static class Money {
        private double amount;
        private String currency;

        public Money(double amount, String currency) {
            this.amount = amount;
            this.currency = currency;
        }

        public double getAmount() { return amount; }
        public String getCurrency() { return currency; }

        @Override
        public String toString() {
            return currency + " " + amount;
        }
    }

    /**
     * Status enum
     */
    public enum Status {
        ACTIVE, INACTIVE, PENDING, SUSPENDED
    }
}

/*
 * Converter vs Formatter:
 * 
 * Converter:
 * - One-way conversion
 * - Source type → Target type
 * - No locale support
 * - Used for object transformation
 * 
 * Formatter:
 * - Two-way conversion (parse & print)
 * - String ↔ Object
 * - Locale-aware
 * - Used for display/input
 * 
 * 
 * Built-in Converters:
 * 
 * Spring provides many built-in converters:
 * - String to primitives (int, long, boolean, etc.)
 * - String to Date, LocalDate, LocalDateTime
 * - String to Enum
 * - String to URL, URI, File
 * - String to Locale, Currency
 * - String to Duration, Period
 * - Collection conversions
 * 
 * 
 * Custom Converter Example:
 * 
 * @Component
 * public class StringToProductConverter implements Converter<String, Product> {
 *     
 *     @Autowired
 *     private ProductRepository productRepository;
 *     
 *     @Override
 *     public Product convert(String id) {
 *         return productRepository.findById(Long.parseLong(id))
 *             .orElseThrow(() -> new IllegalArgumentException("Product not found"));
 *     }
 * }
 * 
 * 
 * ConverterFactory Example (for Enum):
 * 
 * public class StringToEnumConverterFactory 
 *         implements ConverterFactory<String, Enum> {
 *     
 *     @Override
 *     public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
 *         return new StringToEnum<>(targetType);
 *     }
 *     
 *     private static class StringToEnum<T extends Enum> implements Converter<String, T> {
 *         private Class<T> enumType;
 *         
 *         public StringToEnum(Class<T> enumType) {
 *             this.enumType = enumType;
 *         }
 *         
 *         @Override
 *         public T convert(String source) {
 *             return (T) Enum.valueOf(enumType, source.trim().toUpperCase());
 *         }
 *     }
 * }
 * 
 * 
 * GenericConverter Example:
 * 
 * public class StringToCollectionConverter implements GenericConverter {
 *     
 *     @Override
 *     public Set<ConvertiblePair> getConvertibleTypes() {
 *         return Collections.singleton(
 *             new ConvertiblePair(String.class, Collection.class)
 *         );
 *     }
 *     
 *     @Override
 *     public Object convert(Object source, TypeDescriptor sourceType, 
 *                          TypeDescriptor targetType) {
 *         String[] elements = ((String) source).split(",");
 *         List<Object> result = new ArrayList<>();
 *         for (String element : elements) {
 *             result.add(element.trim());
 *         }
 *         return result;
 *     }
 * }
 * 
 * 
 * Using ConversionService Programmatically:
 * 
 * @Service
 * public class MyService {
 *     
 *     @Autowired
 *     private ConversionService conversionService;
 *     
 *     public void processData(String input) {
 *         // Convert String to LocalDate
 *         LocalDate date = conversionService.convert(input, LocalDate.class);
 *         
 *         // Convert String to Money
 *         Money money = conversionService.convert("100.50", Money.class);
 *         
 *         // Check if conversion is possible
 *         if (conversionService.canConvert(String.class, User.class)) {
 *             User user = conversionService.convert("1:John:john@example.com", User.class);
 *         }
 *     }
 * }
 * 
 * 
 * Form Example:
 * 
 * <form method="post" action="/conversion/money">
 *     <input type="text" name="amount" value="USD 100.50" />
 *     <button type="submit">Submit</button>
 * </form>
 * 
 * Spring automatically converts "USD 100.50" to Money object
 * 
 * 
 * Best Practices:
 * 
 * 1. Use Converter for object transformations
 * 2. Use Formatter for display/input with locale
 * 3. Register converters globally in WebMvcConfigurer
 * 4. Keep converters stateless
 * 5. Handle parse errors gracefully
 * 6. Document expected input formats
 * 7. Use GenericConverter for complex scenarios
 * 8. Test converters thoroughly
 */
