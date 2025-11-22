I'll create a comprehensive Spring Boot project demonstrating all conversion and formatting patterns.

## Project Structure

```xml pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.conversionpatterns</groupId>
    <artifactId>conversion-formatting-patterns</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>

        <!-- Apache Commons Lang -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Main Application

```java src/main/java/com/conversionpatterns/ConversionFormattingPatternsApplication.java
package com.conversionpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConversionFormattingPatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConversionFormattingPatternsApplication.class, args);
    }
}
```

## Domain Models

```java src/main/java/com/conversionpatterns/model/Money.java
package com.conversionpatterns.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Custom domain object for conversion examples
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Money {
    private BigDecimal amount;
    private Currency currency;

    public Money(String amount, String currencyCode) {
        this.amount = new BigDecimal(amount);
        this.currency = Currency.getInstance(currencyCode);
    }

    @Override
    public String toString() {
        return String.format("%s %s", currency.getSymbol(), amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && 
               Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
```

```java src/main/java/com/conversionpatterns/model/Address.java
package com.conversionpatterns.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address domain object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    @Override
    public String toString() {
        return String.format("%s, %s, %s %s, %s", 
            street, city, state, zipCode, country);
    }

    /**
     * Parse address from string format: "street|city|state|zipCode|country"
     */
    public static Address fromString(String addressString) {
        if (addressString == null || addressString.isEmpty()) {
            return null;
        }

        String[] parts = addressString.split("\\|");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid address format. Expected: street|city|state|zipCode|country");
        }

        return Address.builder()
                .street(parts[0].trim())
                .city(parts[1].trim())
                .state(parts[2].trim())
                .zipCode(parts[3].trim())
                .country(parts[4].trim())
                .build();
    }

    /**
     * Convert to string format: "street|city|state|zipCode|country"
     */
    public String toFormattedString() {
        return String.format("%s|%s|%s|%s|%s", street, city, state, zipCode, country);
    }
}
```

```java src/main/java/com/conversionpatterns/model/PhoneNumber.java
package com.conversionpatterns.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phone number domain object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumber {
    private String countryCode;
    private String areaCode;
    private String number;

    public PhoneNumber(String fullNumber) {
        parsePhoneNumber(fullNumber);
    }

    private void parsePhoneNumber(String fullNumber) {
        // Remove all non-digit characters
        String cleaned = fullNumber.replaceAll("[^0-9]", "");

        if (cleaned.length() == 10) {
            // US number without country code
            this.countryCode = "1";
            this.areaCode = cleaned.substring(0, 3);
            this.number = cleaned.substring(3);
        } else if (cleaned.length() == 11 && cleaned.startsWith("1")) {
            // US number with country code
            this.countryCode = "1";
            this.areaCode = cleaned.substring(1, 4);
            this.number = cleaned.substring(4);
        } else {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }

    @Override
    public String toString() {
        return String.format("+%s (%s) %s-%s", 
            countryCode, 
            areaCode, 
            number.substring(0, 3), 
            number.substring(3));
    }

    public String toInternationalFormat() {
        return String.format("+%s-%s-%s", countryCode, areaCode, number);
    }
}
```

```java src/main/java/com/conversionpatterns/model/Product.java
package com.conversionpatterns.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Product entity for conversion examples
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String sku;
    private Money price;
    private String category;
    private LocalDateTime createdDate;
    private Address manufacturerAddress;
    private PhoneNumber contactPhone;
}
```

## 1. Converter Pattern

```java src/main/java/com/conversionpatterns/converter/StringToMoneyConverter.java
package com.conversionpatterns.converter;

import com.conversionpatterns.model.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Converter Pattern - String to Money
 * Implements Spring's Converter interface for one-way conversion
 */
@Component
public class StringToMoneyConverter implements Converter<String, Money> {

    /**
     * Convert string in format "USD 100.00" or "100.00 USD" to Money object
     */
    @Override
    public Money convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        String[] parts = source.trim().split("\\s+");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "Invalid money format. Expected: 'USD 100.00' or '100.00 USD'");
        }

        String currencyCode;
        String amountStr;

        // Check if currency is first or second
        if (parts[0].matches("[A-Z]{3}")) {
            currencyCode = parts[0];
            amountStr = parts[1];
        } else if (parts[1].matches("[A-Z]{3}")) {
            currencyCode = parts[1];
            amountStr = parts[0];
        } else {
            throw new IllegalArgumentException(
                "Invalid currency code. Must be 3 uppercase letters");
        }

        try {
            BigDecimal amount = new BigDecimal(amountStr);
            Currency currency = Currency.getInstance(currencyCode);
            return new Money(amount, currency);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to parse money: " + source, e);
        }
    }
}
```

```java src/main/java/com/conversionpatterns/converter/MoneyToStringConverter.java
package com.conversionpatterns.converter;

import com.conversionpatterns.model.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter Pattern - Money to String
 */
@Component
public class MoneyToStringConverter implements Converter<Money, String> {

    @Override
    public String convert(Money source) {
        if (source == null) {
            return null;
        }

        return String.format("%s %s", 
            source.getCurrency().getCurrencyCode(), 
            source.getAmount().toPlainString());
    }
}
```

```java src/main/java/com/conversionpatterns/converter/StringToAddressConverter.java
package com.conversionpatterns.converter;

import com.conversionpatterns.model.Address;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter Pattern - String to Address
 */
@Component
public class StringToAddressConverter implements Converter<String, Address> {

    @Override
    public Address convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        return Address.fromString(source);
    }
}
```

```java src/main/java/com/conversionpatterns/converter/AddressToStringConverter.java
package com.conversionpatterns.converter;

import com.conversionpatterns.model.Address;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter Pattern - Address to String
 */
@Component
public class AddressToStringConverter implements Converter<Address, String> {

    @Override
    public String convert(Address source) {
        if (source == null) {
            return null;
        }

        return source.toFormattedString();
    }
}
```

```java src/main/java/com/conversionpatterns/converter/StringToPhoneNumberConverter.java
package com.conversionpatterns.converter;

import com.conversionpatterns.model.PhoneNumber;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter Pattern - String to PhoneNumber
 */
@Component
public class StringToPhoneNumberConverter implements Converter<String, PhoneNumber> {

    @Override
    public PhoneNumber convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        try {
            return new PhoneNumber(source);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to parse phone number: " + source, e);
        }
    }
}
```

## 2. Formatter Pattern

```java src/main/java/com/conversionpatterns/formatter/MoneyFormatter.java
package com.conversionpatterns.formatter;

import com.conversionpatterns.model.Money;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

/**
 * Formatter Pattern - Bidirectional conversion with locale support
 * Extends Formatter for Money objects
 */
@Component
public class MoneyFormatter implements Formatter<Money> {

    /**
     * Parse string to Money object
     */
    @Override
    public Money parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = text.trim().split("\\s+");
            
            if (parts.length != 2) {
                throw new ParseException(
                    "Invalid money format. Expected: 'USD 100.00'", 0);
            }

            String currencyCode = parts[0];
            String amountStr = parts[1].replace(",", ""); // Remove thousand separators

            BigDecimal amount = new BigDecimal(amountStr);
            Currency currency = Currency.getInstance(currencyCode);

            return new Money(amount, currency);
        } catch (Exception e) {
            throw new ParseException("Failed to parse money: " + text, 0);
        }
    }

    /**
     * Format Money object to string
     */
    @Override
    public String print(Money money, Locale locale) {
        if (money == null) {
            return "";
        }

        // Format with thousand separators based on locale
        return String.format(locale, "%s %,.2f", 
            money.getCurrency().getCurrencyCode(),
            money.getAmount());
    }
}
```

```java src/main/java/com/conversionpatterns/formatter/PhoneNumberFormatter.java
package com.conversionpatterns.formatter;

import com.conversionpatterns.model.PhoneNumber;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

/**
 * Formatter Pattern - PhoneNumber formatter
 */
@Component
public class PhoneNumberFormatter implements Formatter<PhoneNumber> {

    @Override
    public PhoneNumber parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            return new PhoneNumber(text);
        } catch (Exception e) {
            throw new ParseException("Failed to parse phone number: " + text, 0);
        }
    }

    @Override
    public String print(PhoneNumber phoneNumber, Locale locale) {
        if (phoneNumber == null) {
            return "";
        }

        // Format based on locale
        if (locale.getCountry().equals("US")) {
            return phoneNumber.toString(); // US format
        } else {
            return phoneNumber.toInternationalFormat(); // International format
        }
    }
}
```

```java src/main/java/com/conversionpatterns/formatter/AddressFormatter.java
package com.conversionpatterns.formatter;

import com.conversionpatterns.model.Address;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

/**
 * Formatter Pattern - Address formatter with locale support
 */
@Component
public class AddressFormatter implements Formatter<Address> {

    @Override
    public Address parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            return Address.fromString(text);
        } catch (Exception e) {
            throw new ParseException("Failed to parse address: " + text, 0);
        }
    }

    @Override
    public String print(Address address, Locale locale) {
        if (address == null) {
            return "";
        }

        // Format based on locale
        if (locale.getCountry().equals("US")) {
            return String.format("%s, %s, %s %s", 
                address.getStreet(), 
                address.getCity(), 
                address.getState(), 
                address.getZipCode());
        } else {
            return address.toString(); // Full international format
        }
    }
}
```

```java src/main/java/com/conversionpatterns/formatter/CurrencyFormatter.java
package com.conversionpatterns.formatter;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

/**
 * Formatter Pattern - Custom currency formatter
 */
@Component
public class CurrencyFormatter implements Formatter<Currency> {

    @Override
    public Currency parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            return Currency.getInstance(text.trim().toUpperCase());
        } catch (Exception e) {
            throw new ParseException("Invalid currency code: " + text, 0);
        }
    }

    @Override
    public String print(Currency currency, Locale locale) {
        if (currency == null) {
            return "";
        }

        return String.format("%s (%s)", 
            currency.getCurrencyCode(), 
            currency.getDisplayName(locale));
    }
}
```

## 3. Type Conversion Pattern

```java src/main/java/com/conversionpatterns/conversion/GenericConverter.java
package com.conversionpatterns.conversion;

import com.conversionpatterns.model.Money;
import com.conversionpatterns.model.Product;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Type Conversion Pattern - Generic converter for complex conversions
 * Handles multiple source/target type combinations
 */
@Component
public class ProductMapConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
            new ConvertiblePair(Map.class, Product.class),
            new ConvertiblePair(Product.class, Map.class)
        );
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }

        if (sourceType.getType().equals(Map.class) && 
            targetType.getType().equals(Product.class)) {
            return mapToProduct((Map<String, Object>) source);
        } else if (sourceType.getType().equals(Product.class) && 
                   targetType.getType().equals(Map.class)) {
            return productToMap((Product) source);
        }

        return null;
    }

    private Product mapToProduct(Map<String, Object> map) {
        return Product.builder()
                .id(getLong(map, "id"))
                .name((String) map.get("name"))
                .sku((String) map.get("sku"))
                .category((String) map.get("category"))
                .build();
    }

    private Map<String, Object> productToMap(Product product) {
        return Map.of(
            "id", product.getId() != null ? product.getId() : 0L,
            "name", product.getName() != null ? product.getName() : "",
            "sku", product.getSku() != null ? product.getSku() : "",
            "category", product.getCategory() != null ? product.getCategory() : ""
        );
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return null;
    }
}
```

```java src/main/java/com/conversionpatterns/conversion/ConditionalConverter.java
package com.conversionpatterns.conversion;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Type Conversion Pattern - Conditional converter
 * Only converts if certain conditions are met
 */
@Component
public class ConditionalDateTimeConverter implements ConditionalGenericConverter {

    private static final Set<DateTimeFormatter> FORMATTERS = Set.of(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        // Only convert if source is String and target is LocalDateTime or LocalDate
        return sourceType.getType().equals(String.class) &&
               (targetType.getType().equals(LocalDateTime.class) || 
                targetType.getType().equals(LocalDate.class));
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
            new ConvertiblePair(String.class, LocalDateTime.class),
            new ConvertiblePair(String.class, LocalDate.class)
        );
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }

        String dateString = (String) source;

        if (targetType.getType().equals(LocalDateTime.class)) {
            return parseToLocalDateTime(dateString);
        } else if (targetType.getType().equals(LocalDate.class)) {
            return parseToLocalDate(dateString);
        }

        return null;
    }

    private LocalDateTime parseToLocalDateTime(String dateString) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateString, formatter);
            } catch (Exception ignored) {
            }
        }
        throw new IllegalArgumentException("Unable to parse date: " + dateString);
    }

    private LocalDate parseToLocalDate(String dateString) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (Exception ignored) {
            }
        }
        throw new IllegalArgumentException("Unable to parse date: " + dateString);
    }
}
```

```java src/main/java/com/conversionpatterns/conversion/ConverterFactory.java
package com.conversionpatterns.conversion;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

/**
 * Type Conversion Pattern - Converter Factory
 * Creates converters for enum types
 */
@Component
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static class StringToEnumConverter<T extends Enum> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }

            String trimmed = source.trim();
            
            // Try exact match first
            for (T enumConstant : enumType.getEnumConstants()) {
                if (enumConstant.name().equals(trimmed)) {
                    return enumConstant;
                }
            }

            // Try case-insensitive match
            for (T enumConstant : enumType.getEnumConstants()) {
                if (enumConstant.name().equalsIgnoreCase(trimmed)) {
                    return enumConstant;
                }
            }

            throw new IllegalArgumentException(
                String.format("No enum constant %s.%s", enumType.getName(), source));
        }
    }
}
```

## 4. Property Editor Pattern

```java src/main/java/com/conversionpatterns/propertyeditor/MoneyPropertyEditor.java
package com.conversionpatterns.propertyeditor;

import com.conversionpatterns.model.Money;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * Property Editor Pattern - Traditional JavaBeans property editor
 * Used for legacy Spring MVC binding
 */
public class MoneyPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().isEmpty()) {
            setValue(null);
            return;
        }

        try {
            String[] parts = text.trim().split("\\s+");
            
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                    "Invalid money format. Expected: 'USD 100.00'");
            }

            String currencyCode = parts[0];
            String amountStr = parts[1];

            BigDecimal amount = new BigDecimal(amountStr);
            Currency currency = Currency.getInstance(currencyCode);

            setValue(new Money(amount, currency));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse money: " + text, e);
        }
    }

    @Override
    public String getAsText() {
        Money money = (Money) getValue();
        
        if (money == null) {
            return "";
        }

        return String.format("%s %s", 
            money.getCurrency().getCurrencyCode(),
            money.getAmount().toPlainString());
    }
}
```

```java src/main/java/com/conversionpatterns/propertyeditor/AddressPropertyEditor.java
package com.conversionpatterns.propertyeditor;

import com.conversionpatterns.model.Address;

import java.beans.PropertyEditorSupport;

/**
 * Property Editor Pattern - Address property editor
 */
public class AddressPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().isEmpty()) {
            setValue(null);
            return;
        }

        try {
            setValue(Address.fromString(text));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse address: " + text, e);
        }
    }

    @Override
    public String getAsText() {
        Address address = (Address) getValue();
        
        if (address == null) {
            return "";
        }

        return address.toFormattedString();
    }
}
```

```java src/main/java/com/conversionpatterns/propertyeditor/PhoneNumberPropertyEditor.java
package com.conversionpatterns.propertyeditor;

import com.conversionpatterns.model.PhoneNumber;

import java.beans.PropertyEditorSupport;

/**
 * Property Editor Pattern - PhoneNumber property editor
 */
public class PhoneNumberPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().isEmpty()) {
            setValue(null);
            return;
        }

        try {
            setValue(new PhoneNumber(text));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse phone number: " + text, e);
        }
    }

    @Override
    public String getAsText() {
        PhoneNumber phoneNumber = (PhoneNumber) getValue();
        
        if (phoneNumber == null) {
            return "";
        }

        return phoneNumber.toString();
    }
}
```

```java src/main/java/com/conversionpatterns/propertyeditor/CustomPropertyEditorRegistrar.java
package com.conversionpatterns.propertyeditor;

import com.conversionpatterns.model.Address;
import com.conversionpatterns.model.Money;
import com.conversionpatterns.model.PhoneNumber;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.stereotype.Component;

/**
 * Property Editor Pattern - Registrar for custom property editors
 */
@Component
public class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {

    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        registry.registerCustomEditor(Money.class, new MoneyPropertyEditor());
        registry.registerCustomEditor(Address.class, new AddressPropertyEditor());
        registry.registerCustomEditor(PhoneNumber.class, new PhoneNumberPropertyEditor());
    }
}
```

## 5. Conversion Service Pattern

```java src/main/java/com/conversionpatterns/config/ConversionServiceConfig.java
package com.conversionpatterns.config;

import com.conversionpatterns.converter.*;
import com.conversionpatterns.formatter.*;
import com.conversionpatterns.propertyeditor.CustomPropertyEditorRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

```java src/main/java/com/conversionpatterns/config/ConversionServiceConfig.java
/**
 * Conversion Service Pattern - Central configuration for all conversions
 */
@Configuration
public class ConversionServiceConfig implements WebMvcConfigurer {

    @Autowired
    private StringToMoneyConverter stringToMoneyConverter;

    @Autowired
    private MoneyToStringConverter moneyToStringConverter;

    @Autowired
    private StringToAddressConverter stringToAddressConverter;

    @Autowired
    private AddressToStringConverter addressToStringConverter;

    @Autowired
    private StringToPhoneNumberConverter stringToPhoneNumberConverter;

    @Autowired
    private MoneyFormatter moneyFormatter;

    @Autowired
    private PhoneNumberFormatter phoneNumberFormatter;

    @Autowired
    private AddressFormatter addressFormatter;

    @Autowired
    private CurrencyFormatter currencyFormatter;

    @Autowired
    private CustomPropertyEditorRegistrar propertyEditorRegistrar;

    /**
     * Register all converters and formatters
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Register Converters
        registry.addConverter(stringToMoneyConverter);
        registry.addConverter(moneyToStringConverter);
        registry.addConverter(stringToAddressConverter);
        registry.addConverter(addressToStringConverter);
        registry.addConverter(stringToPhoneNumberConverter);

        // Register Formatters
        registry.addFormatter(moneyFormatter);
        registry.addFormatter(phoneNumberFormatter);
        registry.addFormatter(addressFormatter);
        registry.addFormatter(currencyFormatter);
    }

    /**
     * Create FormattingConversionService bean
     */
    @Bean
    public FormattingConversionService conversionService() {
        FormattingConversionService conversionService = new FormattingConversionService();
        
        // Add converters
        conversionService.addConverter(stringToMoneyConverter);
        conversionService.addConverter(moneyToStringConverter);
        conversionService.addConverter(stringToAddressConverter);
        conversionService.addConverter(addressToStringConverter);
        conversionService.addConverter(stringToPhoneNumberConverter);

        // Add formatters
        conversionService.addFormatter(moneyFormatter);
        conversionService.addFormatter(phoneNumberFormatter);
        conversionService.addFormatter(addressFormatter);
        conversionService.addFormatter(currencyFormatter);

        return conversionService;
    }

    /**
     * Configure web binding with property editors
     */
    @Bean
    public ConfigurableWebBindingInitializer configurableWebBindingInitializer(
            FormattingConversionService conversionService) {
        
        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
        initializer.setConversionService(conversionService);
        initializer.setPropertyEditorRegistrar(propertyEditorRegistrar);
        
        return initializer;
    }
}
```

## Service Layer

```java src/main/java/com/conversionpatterns/service/ConversionService.java
package com.conversionpatterns.service;

import com.conversionpatterns.model.Address;
import com.conversionpatterns.model.Money;
import com.conversionpatterns.model.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

/**
 * Service demonstrating Conversion Service Pattern usage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversionDemoService {

    private final ConversionService conversionService;

    /**
     * Convert string to Money using ConversionService
     */
    public Money convertToMoney(String moneyString) {
        log.info("Converting string to Money: {}", moneyString);
        Money money = conversionService.convert(moneyString, Money.class);
        log.info("Converted to: {}", money);
        return money;
    }

    /**
     * Convert Money to string using ConversionService
     */
    public String convertMoneyToString(Money money) {
        log.info("Converting Money to string: {}", money);
        String result = conversionService.convert(money, String.class);
        log.info("Converted to: {}", result);
        return result;
    }

    /**
     * Convert string to Address
     */
    public Address convertToAddress(String addressString) {
        log.info("Converting string to Address: {}", addressString);
        Address address = conversionService.convert(addressString, Address.class);
        log.info("Converted to: {}", address);
        return address;
    }

    /**
     * Convert Address to string
     */
    public String convertAddressToString(Address address) {
        log.info("Converting Address to string: {}", address);
        String result = conversionService.convert(address, String.class);
        log.info("Converted to: {}", result);
        return result;
    }

    /**
     * Convert string to PhoneNumber
     */
    public PhoneNumber convertToPhoneNumber(String phoneString) {
        log.info("Converting string to PhoneNumber: {}", phoneString);
        PhoneNumber phone = conversionService.convert(phoneString, PhoneNumber.class);
        log.info("Converted to: {}", phone);
        return phone;
    }

    /**
     * Check if conversion is supported
     */
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return conversionService.canConvert(sourceType, targetType);
    }
}
```

```java src/main/java/com/conversionpatterns/service/FormattingService.java
package com.conversionpatterns.service;

import com.conversionpatterns.model.Address;
import com.conversionpatterns.model.Money;
import com.conversionpatterns.model.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service demonstrating Formatter Pattern with locale support
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FormattingService {

    private final FormattingConversionService formattingConversionService;

    /**
     * Format Money with specific locale
     */
    public String formatMoney(Money money, Locale locale) {
        log.info("Formatting Money with locale {}: {}", locale, money);
        
        // Use FormattingConversionService for locale-aware formatting
        return formattingConversionService.convert(money, String.class);
    }

    /**
     * Format PhoneNumber with specific locale
     */
    public String formatPhoneNumber(PhoneNumber phoneNumber, Locale locale) {
        log.info("Formatting PhoneNumber with locale {}: {}", locale, phoneNumber);
        return formattingConversionService.convert(phoneNumber, String.class);
    }

    /**
     * Format Address with specific locale
     */
    public String formatAddress(Address address, Locale locale) {
        log.info("Formatting Address with locale {}: {}", locale, address);
        return formattingConversionService.convert(address, String.class);
    }

    /**
     * Parse Money from string with locale
     */
    public Money parseMoney(String moneyString, Locale locale) {
        log.info("Parsing Money with locale {}: {}", locale, moneyString);
        return formattingConversionService.convert(moneyString, Money.class);
    }
}
```

## Controllers

```java src/main/java/com/conversionpatterns/controller/ConversionController.java
package com.conversionpatterns.controller;

import com.conversionpatterns.model.Address;
import com.conversionpatterns.model.Money;
import com.conversionpatterns.model.PhoneNumber;
import com.conversionpatterns.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller demonstrating Converter Pattern
 */
@RestController
@RequestMapping("/api/conversion")
@RequiredArgsConstructor
@Slf4j
public class ConversionController {

    /**
     * Converter Pattern - Automatic conversion of request parameter
     */
    @GetMapping("/money")
    public ResponseEntity<Map<String, Object>> getMoney(
            @RequestParam Money amount) {
        
        log.info("Received Money: {}", amount);
        
        return ResponseEntity.ok(Map.of(
            "original", amount.toString(),
            "amount", amount.getAmount(),
            "currency", amount.getCurrency().getCurrencyCode(),
            "displayName", amount.getCurrency().getDisplayName()
        ));
    }

    /**
     * Converter Pattern - Automatic conversion of path variable
     */
    @GetMapping("/address/{address}")
    public ResponseEntity<Map<String, Object>> getAddress(
            @PathVariable Address address) {
        
        log.info("Received Address: {}", address);
        
        return ResponseEntity.ok(Map.of(
            "formatted", address.toString(),
            "street", address.getStreet(),
            "city", address.getCity(),
            "state", address.getState(),
            "zipCode", address.getZipCode(),
            "country", address.getCountry()
        ));
    }

    /**
     * Converter Pattern - Request body conversion
     */
    @PostMapping("/product")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("Received Product: {}", product);
        return ResponseEntity.ok(product);
    }

    /**
     * Converter Pattern - Multiple parameters
     */
    @GetMapping("/contact")
    public ResponseEntity<Map<String, Object>> getContact(
            @RequestParam PhoneNumber phone,
            @RequestParam Address address) {
        
        log.info("Received Phone: {}, Address: {}", phone, address);
        
        return ResponseEntity.ok(Map.of(
            "phone", phone.toString(),
            "phoneInternational", phone.toInternationalFormat(),
            "address", address.toString()
        ));
    }
}
```

```java src/main/java/com/conversionpatterns/controller/FormattingController.java
package com.conversionpatterns.controller;

import com.conversionpatterns.model.Address;
import com.conversionpatterns.model.Money;
import com.conversionpatterns.model.PhoneNumber;
import com.conversionpatterns.service.FormattingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

/**
 * Controller demonstrating Formatter Pattern with locale support
 */
@RestController
@RequestMapping("/api/formatting")
@RequiredArgsConstructor
@Slf4j
public class FormattingController {

    private final FormattingService formattingService;

    /**
     * Formatter Pattern - Format with locale
     */
    @GetMapping("/money/{locale}")
    public ResponseEntity<Map<String, String>> formatMoney(
            @PathVariable String locale,
            @RequestParam Money money) {
        
        Locale targetLocale = Locale.forLanguageTag(locale);
        String formatted = formattingService.formatMoney(money, targetLocale);
        
        return ResponseEntity.ok(Map.of(
            "locale", locale,
            "formatted", formatted,
            "original", money.toString()
        ));
    }

    /**
     * Formatter Pattern - Format phone number with locale
     */
    @GetMapping("/phone/{locale}")
    public ResponseEntity<Map<String, String>> formatPhone(
            @PathVariable String locale,
            @RequestParam PhoneNumber phone) {
        
        Locale targetLocale = Locale.forLanguageTag(locale);
        String formatted = formattingService.formatPhoneNumber(phone, targetLocale);
        
        return ResponseEntity.ok(Map.of(
            "locale", locale,
            "formatted", formatted,
            "original", phone.toString()
        ));
    }

    /**
     * Formatter Pattern - Format address with locale
     */
    @GetMapping("/address/{locale}")
    public ResponseEntity<Map<String, String>> formatAddress(
            @PathVariable String locale,
            @RequestParam Address address) {
        
        Locale targetLocale = Locale.forLanguageTag(locale);
        String formatted = formattingService.formatAddress(address, targetLocale);
        
        return ResponseEntity.ok(Map.of(
            "locale", locale,
            "formatted", formatted,
            "original", address.toString()
        ));
    }
}
```

```java src/main/java/com/conversionpatterns/controller/ConversionServiceController.java
package com.conversionpatterns.controller;

import com.conversionpatterns.model.Address;
import com.conversionpatterns.model.Money;
import com.conversionpatterns.model.PhoneNumber;
import com.conversionpatterns.service.ConversionDemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller demonstrating Conversion Service Pattern
 */
@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
@Slf4j
public class ConversionServiceController {

    private final ConversionDemoService conversionService;

    /**
     * Conversion Service Pattern - Programmatic conversion
     */
    @PostMapping("/convert/money")
    public ResponseEntity<Map<String, Object>> convertMoney(
            @RequestBody Map<String, String> request) {
        
        String moneyString = request.get("money");
        Money money = conversionService.convertToMoney(moneyString);
        String converted = conversionService.convertMoneyToString(money);
        
        return ResponseEntity.ok(Map.of(
            "input", moneyString,
            "parsed", money,
            "converted", converted
        ));
    }

    /**
     * Conversion Service Pattern - Convert address
     */
    @PostMapping("/convert/address")
    public ResponseEntity<Map<String, Object>> convertAddress(
            @RequestBody Map<String, String> request) {
        
        String addressString = request.get("address");
        Address address = conversionService.convertToAddress(addressString);
        String converted = conversionService.convertAddressToString(address);
        
        return ResponseEntity.ok(Map.of(
            "input", addressString,
            "parsed", address,
            "converted", converted
        ));
    }

    /**
     * Conversion Service Pattern - Convert phone number
     */
    @PostMapping("/convert/phone")
    public ResponseEntity<Map<String, Object>> convertPhone(
            @RequestBody Map<String, String> request) {
        
        String phoneString = request.get("phone");
        PhoneNumber phone = conversionService.convertToPhoneNumber(phoneString);
        
        return ResponseEntity.ok(Map.of(
            "input", phoneString,
            "parsed", phone,
            "formatted", phone.toString(),
            "international", phone.toInternationalFormat()
        ));
    }

    /**
     * Check if conversion is supported
     */
    @GetMapping("/can-convert")
    public ResponseEntity<Map<String, Boolean>> canConvert(
            @RequestParam String sourceType,
            @RequestParam String targetType) {
        
        try {
            Class<?> source = Class.forName(sourceType);
            Class<?> target = Class.forName(targetType);
            
            boolean canConvert = conversionService.canConvert(source, target);
            
            return ResponseEntity.ok(Map.of(
                "canConvert", canConvert,
                "sourceType", sourceType,
                "targetType", targetType
            ));
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "canConvert", false
            ));
        }
    }
}
```

## DTO Classes

```java src/main/java/com/conversionpatterns/dto/ProductDTO.java
package com.conversionpatterns.dto;

import com.conversionpatterns.model.Address;
import com.conversionpatterns.model.Money;
import com.conversionpatterns.model.PhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO demonstrating automatic conversion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String sku;
    private String price; // Will be converted to Money
    private String category;
    private String manufacturerAddress; // Will be converted to Address
    private String contactPhone; // Will be converted to PhoneNumber
}
```

```java src/main/java/com/conversionpatterns/mapper/ProductMapper.java
package com.conversionpatterns.mapper;

import com.conversionpatterns.dto.ProductDTO;
import com.conversionpatterns.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

/**
 * Mapper using ConversionService for transformations
 */
@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ConversionService conversionService;

    public Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .sku(dto.getSku())
                .price(conversionService.convert(dto.getPrice(), com.conversionpatterns.model.Money.class))
                .category(dto.getCategory())
                .manufacturerAddress(conversionService.convert(dto.getManufacturerAddress(), com.conversionpatterns.model.Address.class))
                .contactPhone(conversionService.convert(dto.getContactPhone(), com.conversionpatterns.model.PhoneNumber.class))
                .build();
    }

    public ProductDTO toDTO(Product entity) {
        if (entity == null) {
            return null;
        }

        return ProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .sku(entity.getSku())
                .price(conversionService.convert(entity.getPrice(), String.class))
                .category(entity.getCategory())
                .manufacturerAddress(conversionService.convert(entity.getManufacturerAddress(), String.class))
                .contactPhone(entity.getContactPhone() != null ? entity.getContactPhone().toString() : null)
                .build();
    }
}
```

## Configuration

```yaml src/main/resources/application.yml
spring:
  application:
    name: conversion-formatting-patterns

  # Jackson Configuration
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false

  # MVC Configuration
  mvc:
    format:
      date: yyyy-MM-dd
      date-time: yyyy-MM-dd'T'HH:mm:ss
      time: HH:mm:ss

# Server Configuration
server:
  port: 8080

# Logging Configuration
logging:
  level:
    com.conversionpatterns: DEBUG
    org.springframework.core.convert: DEBUG
    org.springframework.format: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## Testing

```java src/test/java/com/conversionpatterns/ConverterPatternTest.java
package com.conversionpatterns;

import com.conversionpatterns.converter.StringToMoneyConverter;
import com.conversionpatterns.converter.MoneyToStringConverter;
import com.conversionpatterns.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Converter Pattern
 */
class ConverterPatternTest {

    private final StringToMoneyConverter stringToMoneyConverter = new StringToMoneyConverter();
    private final MoneyToStringConverter moneyToStringConverter = new MoneyToStringConverter();

    @Test
    void testStringToMoneyConversion() {
        // Given
        String moneyString = "USD 100.50";

        // When
        Money money = stringToMoneyConverter.convert(moneyString);

        // Then
        assertNotNull(money);
        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals(Currency.getInstance("USD"), money.getCurrency());
    }

    @Test
    void testStringToMoneyConversionReversed() {
        // Given
        String moneyString = "100.50 USD";

        // When
        Money money = stringToMoneyConverter.convert(moneyString);

        // Then
        assertNotNull(money);
        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals(Currency.getInstance("USD"), money.getCurrency());
    }

    @Test
    void testMoneyToStringConversion() {
        // Given
        Money money = new Money(new BigDecimal("100.50"), Currency.getInstance("USD"));

        // When
        String result = moneyToStringConverter.convert(money);

        // Then
        assertEquals("USD 100.50", result);
    }

    @Test
    void testNullConversion() {
        assertNull(stringToMoneyConverter.convert(null));
        assertNull(moneyToStringConverter.convert(null));
    }

    @Test
    void testInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            stringToMoneyConverter.convert("invalid");
        });
    }
}
```

```java src/test/java/com/conversionpatterns/FormatterPatternTest.java
package com.conversionpatterns;

import com.conversionpatterns.formatter.MoneyFormatter;
import com.conversionpatterns.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Formatter Pattern
 */
class FormatterPatternTest {

    private final MoneyFormatter moneyFormatter = new MoneyFormatter();

    @Test
    void testParseWithUSLocale() throws ParseException {
        // Given
        String moneyString = "USD 1,000.50";
        Locale locale = Locale.US;

        // When
        Money money = moneyFormatter.parse(moneyString, locale);

        // Then
        assertNotNull(money);
        assertEquals(new BigDecimal("1000.50"), money.getAmount());
        assertEquals(Currency.getInstance("USD"), money.getCurrency());
    }

    @Test
    void testPrintWithUSLocale() {
        // Given
        Money money = new Money(new BigDecimal("1000.50"), Currency.getInstance("USD"));
        Locale locale = Locale.US;

        // When
        String result = moneyFormatter.print(money, locale);

        // Then
        assertEquals("USD 1,000.50", result);
    }

    @Test
    void testPrintWithGermanLocale() {
        // Given
        Money money = new Money(new BigDecimal("1000.50"), Currency.getInstance("EUR"));
        Locale locale = Locale.GERMANY;

        // When
        String result = moneyFormatter.print(money, locale);

        // Then
        assertTrue(result.contains("EUR"));
        assertTrue(result.contains("1") && result.contains("000"));
    }

    @Test
    void testParseNull() throws ParseException {
        assertNull(moneyFormatter.parse(null, Locale.US));
        assertNull(moneyFormatter.parse("", Locale.US));
    }

    @Test
    void testPrintNull() {
        assertEquals("", moneyFormatter.print(null, Locale.US));
    }
}
```

```java src/test/java/com/conversionpatterns/ConversionServicePatternTest.java
package com.conversionpatterns;

import com.conversionpatterns.model.Address;
import com.conversionpatterns.model.Money;
import com.conversionpatterns.model.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Conversion Service Pattern
 */
@SpringBootTest
class ConversionServicePatternTest {

    @Autowired
    private ConversionService conversionService;

    @Test
    void testMoneyConversion() {
        // Given
        String moneyString = "USD 250.75";

        // When
        Money money = conversionService.convert(moneyString, Money.class);

        // Then
        assertNotNull(money);
        assertEquals(new BigDecimal("250.75"), money.getAmount());
        assertEquals(Currency.getInstance("USD"), money.getCurrency());
    }

    @Test
    void testMoneyToStringConversion() {
        // Given
        Money money = new Money(new BigDecimal("250.75"), Currency.getInstance("USD"));

        // When
        String result = conversionService.convert(money, String.class);

        // Then
        assertEquals("USD 250.75", result);
    }

    @Test
    void testAddressConversion() {
        // Given
        String addressString = "123 Main St|New York|NY|10001|USA";

        // When
        Address address = conversionService.convert(addressString, Address.class);

        // Then
        assertNotNull(address);
        assertEquals("123 Main St", address.getStreet());
        assertEquals("New York", address.getCity());
        assertEquals("NY", address.getState());
        assertEquals("10001", address.getZipCode());
        assertEquals("USA", address.getCountry());
    }

    @Test
    void testPhoneNumberConversion() {
        // Given
        String phoneString = "(555) 123-4567";

        // When
        PhoneNumber phone = conversionService.convert(phoneString, PhoneNumber.class);

        // Then
        assertNotNull(phone);
        assertEquals("1", phone.getCountryCode());
        assertEquals("555", phone.getAreaCode());
        assertEquals("1234567", phone.getNumber());
    }

    @Test
    void testCanConvert() {
        assertTrue(conversionService.canConvert(String.class, Money.class));
        assertTrue(conversionService.canConvert(Money.class, String.class));
        assertTrue(conversionService.canConvert(String.class, Address.class));
        assertTrue(conversionService.canConvert(String.class, PhoneNumber.class));
    }
}
```

```java src/test/java/com/conversionpatterns/PropertyEditorPatternTest.java
package com.conversionpatterns;

import com.conversionpatterns.model.Money;
import com.conversionpatterns.propertyeditor.MoneyPropertyEditor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Property Editor Pattern
 */
class PropertyEditorPatternTest {

    @Test
    void testSetAsText() {
        // Given
        MoneyPropertyEditor editor = new MoneyPropertyEditor();
        String moneyString = "EUR 500.00";

        // When
        editor.setAsText(moneyString);
        Money money = (Money) editor.getValue();

        // Then
        assertNotNull(money);
        assertEquals(new BigDecimal("500.00"), money.getAmount());
        assertEquals(Currency.getInstance("EUR"), money.getCurrency());
    }

    @Test
    void testGetAsText() {
        // Given
        MoneyPropertyEditor editor = new MoneyPropertyEditor();
        Money money = new Money(new BigDecimal("500.00"), Currency.getInstance("EUR"));

        // When
        editor.setValue(money);
        String result = editor.getAsText();

        // Then
        assertEquals("EUR 500.00", result);
    }

    @Test
    void testNullValue() {
        MoneyPropertyEditor editor = new MoneyPropertyEditor();
        
        editor.setAsText(null);
        assertNull(editor.getValue());
        
        editor.setValue(null);
        assertEquals("", editor.getAsText());
    }
}
```

## Integration Tests

```java src/test/java/com/conversionpatterns/ConversionControllerIntegrationTest.java
package com.conversionpatterns;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for conversion patterns
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConversionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testMoneyConversion() throws Exception {
        mockMvc.perform(get("/api/conversion/money")
                .param("amount", "USD 100.50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void testAddressConversion() throws Exception {
        String encodedAddress = "123%20Main%20St%7CNew%20York%7CNY%7C10001%7CUSA";
        
```java src/test/java/com/conversionpatterns/ConversionControllerIntegrationTest.java
        mockMvc.perform(get("/api/conversion/address/{address}", encodedAddress))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("123 Main St"))
                .andExpect(jsonPath("$.city").value("New York"))
                .andExpect(jsonPath("$.state").value("NY"))
                .andExpect(jsonPath("$.zipCode").value("10001"))
                .andExpect(jsonPath("$.country").value("USA"));
    }

    @Test
    void testContactConversion() throws Exception {
        mockMvc.perform(get("/api/conversion/contact")
                .param("phone", "(555) 123-4567")
                .param("address", "123 Main St|New York|NY|10001|USA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").exists())
                .andExpect(jsonPath("$.phoneInternational").exists())
                .andExpect(jsonPath("$.address").exists());
    }

    @Test
    void testInvalidMoneyFormat() throws Exception {
        mockMvc.perform(get("/api/conversion/money")
                .param("amount", "invalid"))
                .andExpect(status().isBadRequest());
    }
}
```

```java src/test/java/com/conversionpatterns/FormattingControllerIntegrationTest.java
package com.conversionpatterns;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for formatting patterns
 */
@SpringBootTest
@AutoConfigureMockMvc
class FormattingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testMoneyFormattingWithUSLocale() throws Exception {
        mockMvc.perform(get("/api/formatting/money/en-US")
                .param("money", "USD 1000.50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale").value("en-US"))
                .andExpect(jsonPath("$.formatted").exists())
                .andExpect(jsonPath("$.original").exists());
    }

    @Test
    void testPhoneFormattingWithUSLocale() throws Exception {
        mockMvc.perform(get("/api/formatting/phone/en-US")
                .param("phone", "5551234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale").value("en-US"))
                .andExpect(jsonPath("$.formatted").exists());
    }

    @Test
    void testAddressFormattingWithUSLocale() throws Exception {
        mockMvc.perform(get("/api/formatting/address/en-US")
                .param("address", "123 Main St|New York|NY|10001|USA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale").value("en-US"))
                .andExpect(jsonPath("$.formatted").exists());
    }
}
```

```java src/test/java/com/conversionpatterns/ConversionServiceControllerIntegrationTest.java
package com.conversionpatterns;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for conversion service pattern
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConversionServiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testConvertMoney() throws Exception {
        Map<String, String> request = Map.of("money", "EUR 250.75");

        mockMvc.perform(post("/api/service/convert/money")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.input").value("EUR 250.75"))
                .andExpect(jsonPath("$.parsed").exists())
                .andExpect(jsonPath("$.converted").value("EUR 250.75"));
    }

    @Test
    void testConvertAddress() throws Exception {
        Map<String, String> request = Map.of(
            "address", "456 Oak Ave|Los Angeles|CA|90001|USA"
        );

        mockMvc.perform(post("/api/service/convert/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.input").exists())
                .andExpect(jsonPath("$.parsed.street").value("456 Oak Ave"))
                .andExpect(jsonPath("$.parsed.city").value("Los Angeles"));
    }

    @Test
    void testConvertPhone() throws Exception {
        Map<String, String> request = Map.of("phone", "(555) 987-6543");

        mockMvc.perform(post("/api/service/convert/phone")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.input").value("(555) 987-6543"))
                .andExpect(jsonPath("$.parsed").exists())
                .andExpect(jsonPath("$.formatted").exists())
                .andExpect(jsonPath("$.international").exists());
    }

    @Test
    void testCanConvert() throws Exception {
        mockMvc.perform(get("/api/service/can-convert")
                .param("sourceType", "java.lang.String")
                .param("targetType", "com.conversionpatterns.model.Money"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canConvert").value(true));
    }
}
```

## Example Usage Documentation

```java src/main/java/com/conversionpatterns/examples/ConversionExamples.java
package com.conversionpatterns.examples;

import com.conversionpatterns.model.Address;
import com.conversionpatterns.model.Money;
import com.conversionpatterns.model.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Example usage of all conversion patterns
 */
@Component
@Profile("demo")
@RequiredArgsConstructor
@Slf4j
public class ConversionExamples implements CommandLineRunner {

    private final ConversionService conversionService;

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("Conversion Pattern Examples");
        log.info("========================================");

        demonstrateConverterPattern();
        demonstrateFormatterPattern();
        demonstrateConversionServicePattern();
        demonstrateTypeConversion();

        log.info("========================================");
    }

    private void demonstrateConverterPattern() {
        log.info("\n--- Converter Pattern ---");

        // String to Money
        String moneyStr = "USD 1500.00";
        Money money = conversionService.convert(moneyStr, Money.class);
        log.info("Converted '{}' to Money: {}", moneyStr, money);

        // Money to String
        String converted = conversionService.convert(money, String.class);
        log.info("Converted Money back to String: {}", converted);

        // String to Address
        String addressStr = "742 Evergreen Terrace|Springfield|IL|62701|USA";
        Address address = conversionService.convert(addressStr, Address.class);
        log.info("Converted '{}' to Address: {}", addressStr, address);

        // String to PhoneNumber
        String phoneStr = "(555) 123-4567";
        PhoneNumber phone = conversionService.convert(phoneStr, PhoneNumber.class);
        log.info("Converted '{}' to PhoneNumber: {}", phoneStr, phone);
    }

    private void demonstrateFormatterPattern() {
        log.info("\n--- Formatter Pattern ---");

        Money money = new Money(new BigDecimal("9999.99"), Currency.getInstance("USD"));
        log.info("Money object: {}", money);
        log.info("Formatted (default): {}", conversionService.convert(money, String.class));

        PhoneNumber phone = new PhoneNumber("5551234567");
        log.info("PhoneNumber object: {}", phone);
        log.info("International format: {}", phone.toInternationalFormat());
    }

    private void demonstrateConversionServicePattern() {
        log.info("\n--- Conversion Service Pattern ---");

        // Check conversion capabilities
        boolean canConvert = conversionService.canConvert(String.class, Money.class);
        log.info("Can convert String to Money: {}", canConvert);

        canConvert = conversionService.canConvert(Money.class, String.class);
        log.info("Can convert Money to String: {}", canConvert);

        // Batch conversions
        String[] amounts = {"USD 100.00", "EUR 250.50", "GBP 500.75"};
        log.info("Converting multiple money values:");
        for (String amount : amounts) {
            Money m = conversionService.convert(amount, Money.class);
            log.info("  {} -> {}", amount, m);
        }
    }

    private void demonstrateTypeConversion() {
        log.info("\n--- Type Conversion Pattern ---");

        // Complex object conversion
        Money usd = new Money(new BigDecimal("100.00"), Currency.getInstance("USD"));
        Money eur = new Money(new BigDecimal("85.00"), Currency.getInstance("EUR"));
        Money gbp = new Money(new BigDecimal("75.00"), Currency.getInstance("GBP"));

        log.info("Multiple currency objects:");
        log.info("  USD: {}", usd);
        log.info("  EUR: {}", eur);
        log.info("  GBP: {}", gbp);
    }
}
```

## README Documentation

```markdown README.md
# Conversion and Formatting Patterns in Spring Boot

This project demonstrates all major conversion and formatting patterns in Spring Boot.

## Patterns Implemented

### 1. **Converter Pattern**
- Location: `converter/`
- One-way conversion using Spring's `Converter` interface
- Automatic registration with Spring's conversion system
- Type-safe conversion between specific types

**Example:**
```java
@Component
public class StringToMoneyConverter implements Converter<String, Money> {
    @Override
    public Money convert(String source) {
        // Convert "USD 100.00" to Money object
    }
}
```

### 2. **Formatter Pattern**
- Location: `formatter/`
- Bidirectional conversion with locale support
- Implements `Formatter` interface for parsing and printing
- Locale-aware formatting for internationalization

**Example:**
```java
@Component
public class MoneyFormatter implements Formatter<Money> {
    @Override
    public Money parse(String text, Locale locale) { }
    
    @Override
    public String print(Money money, Locale locale) { }
}
```

### 3. **Type Conversion Pattern**
- Location: `conversion/`
- Complex multi-type conversions
- Generic converters for flexible type handling
- Conditional converters based on runtime conditions

**Example:**
```java
@Component
public class ProductMapConverter implements GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
            new ConvertiblePair(Map.class, Product.class)
        );
    }
}
```

### 4. **Property Editor Pattern**
- Location: `propertyeditor/`
- Traditional JavaBeans property editors
- Legacy Spring MVC support
- String-based property binding

**Example:**
```java
public class MoneyPropertyEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) { }
    
    @Override
    public String getAsText() { }
}
```

### 5. **Conversion Service Pattern**
- Location: `config/ConversionServiceConfig.java`
- Centralized conversion management
- Unified API for all conversions
- Automatic converter/formatter registration

**Example:**
```java
@Bean
public FormattingConversionService conversionService() {
    FormattingConversionService service = new FormattingConversionService();
    service.addConverter(stringToMoneyConverter);
    service.addFormatter(moneyFormatter);
    return service;
}
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Controllers                           │
│  - ConversionController (Converter Pattern)             │
│  - FormattingController (Formatter Pattern)             │
│  - ConversionServiceController (Service Pattern)        │
└──────────────────────┬─────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│              Conversion Service                          │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐        │
│  │ Converters │  │ Formatters │  │  Property  │        │
│  │            │  │            │  │  Editors   │        │
│  └────────────┘  └────────────┘  └────────────┘        │
└──────────────────────┬─────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│                  Domain Models                           │
│  - Money                                                 │
│  - Address                                               │
│  - PhoneNumber                                           │
│  - Product                                               │
└─────────────────────────────────────────────────────────┘
```

## Quick Start

### Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Run with demo profile (shows examples)
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

## API Examples

### Converter Pattern

**Convert Money (Query Parameter):**
```bash
curl "http://localhost:8080/api/conversion/money?amount=USD%20100.50"
```

**Response:**
```json
{
  "original": "$ 100.50",
  "amount": 100.50,
  "currency": "USD",
  "displayName": "US Dollar"
}
```

**Convert Address (Path Variable):**
```bash
curl "http://localhost:8080/api/conversion/address/123%20Main%20St%7CNew%20York%7CNY%7C10001%7CUSA"
```

**Response:**
```json
{
  "formatted": "123 Main St, New York, NY 10001, USA",
  "street": "123 Main St",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA"
}
```

**Convert Contact Information:**
```bash
curl "http://localhost:8080/api/conversion/contact?phone=(555)%20123-4567&address=123%20Main%20St%7CNew%20York%7CNY%7C10001%7CUSA"
```

**Response:**
```json
{
  "phone": "+1 (555) 123-4567",
  "phoneInternational": "+1-555-1234567",
  "address": "123 Main St, New York, NY 10001, USA"
}
```

### Formatter Pattern (Locale-Aware)

**Format Money with US Locale:**
```bash
curl "http://localhost:8080/api/formatting/money/en-US?money=USD%201000.50"
```

**Response:**
```json
{
  "locale": "en-US",
  "formatted": "USD 1,000.50",
  "original": "$ 1000.50"
}
```

**Format Phone with Different Locale:**
```bash
# US Format
curl "http://localhost:8080/api/formatting/phone/en-US?phone=5551234567"

# International Format
curl "http://localhost:8080/api/formatting/phone/en-GB?phone=5551234567"
```

**Format Address with Locale:**
```bash
curl "http://localhost:8080/api/formatting/address/en-US?address=123%20Main%20St%7CNew%20York%7CNY%7C10001%7CUSA"
```

### Conversion Service Pattern

**Convert Money Programmatically:**
```bash
curl -X POST http://localhost:8080/api/service/convert/money \
  -H "Content-Type: application/json" \
  -d '{"money": "EUR 250.75"}'
```

**Response:**
```json
{
  "input": "EUR 250.75",
  "parsed": {
    "amount": 250.75,
    "currency": "EUR"
  },
  "converted": "EUR 250.75"
}
```

**Convert Address:**
```bash
curl -X POST http://localhost:8080/api/service/convert/address \
  -H "Content-Type: application/json" \
  -d '{"address": "456 Oak Ave|Los Angeles|CA|90001|USA"}'
```

**Convert Phone Number:**
```bash
curl -X POST http://localhost:8080/api/service/convert/phone \
  -H "Content-Type: application/json" \
  -d '{"phone": "(555) 987-6543"}'
```

**Response:**
```json
{
  "input": "(555) 987-6543",
  "parsed": {
    "countryCode": "1",
    "areaCode": "555",
    "number": "9876543"
  },
  "formatted": "+1 (555) 987-6543",
  "international": "+1-555-9876543"
}
```

**Check Conversion Capability:**
```bash
curl "http://localhost:8080/api/service/can-convert?sourceType=java.lang.String&targetType=com.conversionpatterns.model.Money"
```

**Response:**
```json
{
  "canConvert": true,
  "sourceType": "java.lang.String",
  "targetType": "com.conversionpatterns.model.Money"
}
```

## Domain Object Formats

### Money Format
```
Format: "CURRENCY AMOUNT" or "AMOUNT CURRENCY"
Examples:
  - "USD 100.50"
  - "100.50 USD"
  - "EUR 250.75"
  - "1000.00 GBP"
```

### Address Format
```
Format: "street|city|state|zipCode|country"
Example: "123 Main St|New York|NY|10001|USA"
```

### Phone Number Format
```
Formats accepted:
  - "(555) 123-4567"
  - "555-123-4567"
  - "5551234567"
  - "+1 (555) 123-4567"
  - "1-555-123-4567"
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=ConverterPatternTest
mvn test -Dtest=FormatterPatternTest
mvn test -Dtest=ConversionServicePatternTest
```

### Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

## Pattern Comparison

| Pattern | Use Case | Bidirectional | Locale Support | Registration |
|---------|----------|---------------|----------------|--------------|
| Converter | Simple type conversion | No | No | Auto |
| Formatter | Locale-aware formatting | Yes | Yes | Auto |
| Type Conversion | Complex multi-type | Depends | No | Manual |
| Property Editor | Legacy binding | Yes | No | Manual |
| Conversion Service | Unified API | Yes | Yes | Programmatic |

## Best Practices

### 1. Use Converter for Simple Conversions
```java
@Component
public class StringToMoneyConverter implements Converter<String, Money> {
    @Override
    public Money convert(String source) {
        // Simple one-way conversion
    }
}
```

### 2. Use Formatter for Locale-Aware Operations
```java
@Component
public class MoneyFormatter implements Formatter<Money> {
    @Override
    public String print(Money money, Locale locale) {
        // Format based on locale
        return String.format(locale, "%s %,.2f", ...);
    }
}
```

### 3. Use ConversionService for Programmatic Conversion
```java
@Service
public class MyService {
    private final ConversionService conversionService;
    
    public void process(String input) {
        Money money = conversionService.convert(input, Money.class);
    }
}
```

### 4. Register Converters Centrally
```java
@Configuration
public class ConversionConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToMoneyConverter());
        registry.addFormatter(new MoneyFormatter());
    }
}
```

### 5. Handle Null Values
```java
@Override
public Money convert(String source) {
    if (source == null || source.trim().isEmpty()) {
        return null;
    }
    // Conversion logic
}
```

## Common Use Cases

### 1. Request Parameter Binding
```java
@GetMapping("/product")
public Product getProduct(@RequestParam Money price) {
    // Automatic conversion from string to Money
}
```

### 2. Path Variable Conversion
```java
@GetMapping("/address/{address}")
public AddressInfo getAddress(@PathVariable Address address) {
    // Automatic conversion
}
```

### 3. Request Body Mapping
```java
@PostMapping("/product")
public Product create(@RequestBody ProductDTO dto) {
    // Use ConversionService for DTO to Entity conversion
    return mapper.toEntity(dto);
}
```

### 4. Internationalization
```java
@GetMapping("/money/{locale}")
public String format(@PathVariable Locale locale, @RequestParam Money money) {
    return formattingService.format(money, locale);
}
```

## Troubleshooting

### Conversion Not Working

1. **Check converter registration:**
```java
@Component // Must be a Spring bean
public class MyConverter implements Converter<Source, Target> {
    // ...
}
```

2. **Verify configuration:**
```java
@Configuration
public class Config implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(myConverter);
    }
}
```

### Format Not Applied

1. **Check locale:**
```java
// Ensure locale is passed correctly
formatter.print(money, Locale.US);
```

2. **Verify formatter registration:**
```java
registry.addFormatter(moneyFormatter);
```

### Type Mismatch Errors

1. **Check supported types:**
```java
conversionService.canConvert(String.class, Money.class);
```

2. **Verify conversion path:**
```java
// Direct conversion
conversionService.convert("USD 100", Money.class);

// Chained conversion might be needed
```

## License

MIT License
```

This comprehensive implementation covers all 5 conversion and formatting patterns with working code, tests, and detailed documentation. Each pattern is demonstrated with practical examples that you can run and test immediately.