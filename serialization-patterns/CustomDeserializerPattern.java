package com.example.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Custom Deserializer Pattern
 * 
 * Demonstrates creating custom deserializers for handling complex JSON parsing in Jackson.
 * 
 * Key Concepts:
 * 1. JsonDeserializer<T> - Custom deserializer interface
 * 2. @JsonDeserialize annotation - Apply custom deserializer
 * 3. DeserializationContext - Context for deserialization
 * 4. JsonParser - Low-level JSON reading
 * 5. JsonNode - Tree model for JSON
 * 6. Type conversion and validation
 * 7. Error handling during deserialization
 * 8. Multiple format support
 * 9. Backward compatibility
 * 10. Data normalization
 * 
 * Use Cases:
 * - Parsing multiple date formats
 * - String to enum conversion with fallback
 * - Data validation during deserialization
 * - Legacy JSON format support
 * - Complex type construction
 * - Data transformation during deserialization
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class CustomDeserializerPattern {

    public static void main(String[] args) {
        SpringApplication.run(CustomDeserializerPattern.class, args);
        
        // Demo custom deserializers
        demonstrateCustomDeserializers();
    }
    
    private static void demonstrateCustomDeserializers() {
        System.out.println("=== Custom Deserializer Pattern Demo ===\n");
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            
            // Register custom deserializers
            SimpleModule module = new SimpleModule();
            module.addDeserializer(FlexibleDate.class, new FlexibleDateDeserializer());
            module.addDeserializer(PriceAmount.class, new PriceDeserializer());
            module.addDeserializer(PhoneNumber.class, new PhoneNumberDeserializer());
            mapper.registerModule(module);
            
            // 1. Flexible Date Parsing - Multiple Formats
            String[] dateJsons = {
                "{\"value\":\"2024-01-15\"}",
                "{\"value\":\"15-Jan-2024\"}",
                "{\"value\":\"01/15/2024\"}",
                "{\"value\":\"1705276800000\"}"  // timestamp
            };
            
            System.out.println("1. Flexible Date Deserialization:");
            for (String json : dateJsons) {
                FlexibleDate date = mapper.readValue(json, FlexibleDate.class);
                System.out.println("Input: " + json + " -> Parsed: " + date.getValue());
            }
            System.out.println();
            
            // 2. Price Parsing - Different Formats
            String[] priceJsons = {
                "{\"price\":\"$99.99\"}",
                "{\"price\":\"€150.50\"}",
                "{\"price\":\"250.75\"}",
                "{\"price\":\"1,234.56\"}"
            };
            
            System.out.println("2. Price Amount Deserialization:");
            for (String json : priceJsons) {
                PriceWrapper wrapper = mapper.readValue(json, PriceWrapper.class);
                System.out.println("Input: " + json + " -> Amount: " + wrapper.getPrice().getAmount() + 
                                 ", Currency: " + wrapper.getPrice().getCurrency());
            }
            System.out.println();
            
            // 3. Phone Number Normalization
            String[] phoneJsons = {
                "{\"phone\":\"(555) 123-4567\"}",
                "{\"phone\":\"555-123-4567\"}",
                "{\"phone\":\"5551234567\"}",
                "{\"phone\":\"+1-555-123-4567\"}"
            };
            
            System.out.println("3. Phone Number Deserialization:");
            for (String json : phoneJsons) {
                PhoneWrapper wrapper = mapper.readValue(json, PhoneWrapper.class);
                System.out.println("Input: " + json + " -> Normalized: " + wrapper.getPhone().getNormalized());
            }
            System.out.println();
            
            // 4. Complex Object with Custom Deserializers
            String customerJson = "{\n" +
                "  \"id\": 101,\n" +
                "  \"name\": \"John Smith\",\n" +
                "  \"registrationDate\": \"15-Dec-2023\",\n" +
                "  \"phone\": \"(555) 987-6543\",\n" +
                "  \"creditLimit\": \"$5,000.00\"\n" +
                "}";
            
            CustomerData customer = mapper.readValue(customerJson, CustomerData.class);
            System.out.println("4. Complex Object Deserialization:");
            System.out.println("Customer: " + customer);
            System.out.println();
            
            // 5. Boolean Flexible Parsing
            String[] boolJsons = {
                "{\"enabled\":\"true\"}",
                "{\"enabled\":\"yes\"}",
                "{\"enabled\":\"1\"}",
                "{\"enabled\":\"on\"}",
                "{\"enabled\":true}"
            };
            
            System.out.println("5. Flexible Boolean Deserialization:");
            for (String json : boolJsons) {
                BooleanWrapper wrapper = mapper.readValue(json, BooleanWrapper.class);
                System.out.println("Input: " + json + " -> Value: " + wrapper.getEnabled().getValue());
            }
            System.out.println();
            
            // 6. Enum with Fallback
            String[] statusJsons = {
                "{\"status\":\"ACTIVE\"}",
                "{\"status\":\"active\"}",
                "{\"status\":\"Active\"}",
                "{\"status\":\"UNKNOWN_STATUS\"}",  // Will fallback to default
                "{\"status\":\"\"}"  // Empty - will fallback
            };
            
            System.out.println("6. Enum Deserialization with Fallback:");
            for (String json : statusJsons) {
                StatusWrapper wrapper = mapper.readValue(json, StatusWrapper.class);
                System.out.println("Input: " + json + " -> Status: " + wrapper.getStatus().getValue());
            }
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Deserialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Flexible Date class
 */
class FlexibleDate {
    private LocalDate value;
    
    public FlexibleDate() {}
    
    public FlexibleDate(LocalDate value) {
        this.value = value;
    }
    
    public LocalDate getValue() { return value; }
    public void setValue(LocalDate value) { this.value = value; }
}

/**
 * Custom Deserializer for FlexibleDate - Supports multiple date formats
 */
class FlexibleDateDeserializer extends JsonDeserializer<FlexibleDate> {
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE,           // 2024-01-15
        DateTimeFormatter.ofPattern("dd-MMM-yyyy"), // 15-Jan-2024
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),  // 01/15/2024
        DateTimeFormatter.ofPattern("yyyy-MM-dd")   // 2024-01-15
    };
    
    @Override
    public FlexibleDate deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        String dateString = p.getText();
        
        // Try timestamp first
        try {
            long timestamp = Long.parseLong(dateString);
            LocalDate date = new Date(timestamp).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            return new FlexibleDate(date);
        } catch (NumberFormatException e) {
            // Not a timestamp, continue with format parsing
        }
        
        // Try each formatter
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(dateString, formatter);
                return new FlexibleDate(date);
            } catch (Exception e) {
                // Try next formatter
            }
        }
        
        throw new IOException("Unable to parse date: " + dateString);
    }
}

/**
 * Price Amount class
 */
class PriceAmount {
    private BigDecimal amount;
    private String currency;
    
    public PriceAmount() {}
    
    public PriceAmount(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}

/**
 * Custom Deserializer for Price - Handles currency symbols and formatting
 */
class PriceDeserializer extends JsonDeserializer<PriceAmount> {
    @Override
    public PriceAmount deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        String priceString = p.getText().trim();
        String currency = "USD";  // Default
        String amountString = priceString;
        
        // Extract currency symbol
        if (priceString.startsWith("$")) {
            currency = "USD";
            amountString = priceString.substring(1);
        } else if (priceString.startsWith("€")) {
            currency = "EUR";
            amountString = priceString.substring(1);
        } else if (priceString.startsWith("£")) {
            currency = "GBP";
            amountString = priceString.substring(1);
        }
        
        // Remove commas
        amountString = amountString.replace(",", "");
        
        try {
            BigDecimal amount = new BigDecimal(amountString);
            return new PriceAmount(amount, currency);
        } catch (NumberFormatException e) {
            throw new IOException("Unable to parse price: " + priceString, e);
        }
    }
}

/**
 * Wrapper for Price
 */
class PriceWrapper {
    @JsonDeserialize(using = PriceDeserializer.class)
    private PriceAmount price;
    
    public PriceAmount getPrice() { return price; }
    public void setPrice(PriceAmount price) { this.price = price; }
}

/**
 * Phone Number class
 */
class PhoneNumber {
    private String normalized;
    private String original;
    
    public PhoneNumber() {}
    
    public PhoneNumber(String normalized, String original) {
        this.normalized = normalized;
        this.original = original;
    }
    
    public String getNormalized() { return normalized; }
    public void setNormalized(String normalized) { this.normalized = normalized; }
    
    public String getOriginal() { return original; }
    public void setOriginal(String original) { this.original = original; }
}

/**
 * Custom Deserializer for Phone Number - Normalizes format
 */
class PhoneNumberDeserializer extends JsonDeserializer<PhoneNumber> {
    @Override
    public PhoneNumber deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        String phoneString = p.getText();
        
        // Remove all non-digit characters
        String digitsOnly = phoneString.replaceAll("[^0-9]", "");
        
        // Format as (XXX) XXX-XXXX if 10 digits
        String normalized;
        if (digitsOnly.length() == 10) {
            normalized = String.format("(%s) %s-%s",
                digitsOnly.substring(0, 3),
                digitsOnly.substring(3, 6),
                digitsOnly.substring(6, 10));
        } else if (digitsOnly.length() == 11 && digitsOnly.startsWith("1")) {
            // Remove leading 1 for US numbers
            String withoutCountryCode = digitsOnly.substring(1);
            normalized = String.format("(%s) %s-%s",
                withoutCountryCode.substring(0, 3),
                withoutCountryCode.substring(3, 6),
                withoutCountryCode.substring(6, 10));
        } else {
            normalized = digitsOnly;
        }
        
        return new PhoneNumber(normalized, phoneString);
    }
}

/**
 * Wrapper for Phone
 */
class PhoneWrapper {
    @JsonDeserialize(using = PhoneNumberDeserializer.class)
    private PhoneNumber phone;
    
    public PhoneNumber getPhone() { return phone; }
    public void setPhone(PhoneNumber phone) { this.phone = phone; }
}

/**
 * Customer with multiple custom deserializers
 */
class CustomerData {
    private Long id;
    private String name;
    
    @JsonDeserialize(using = FlexibleDateDeserializer.class)
    private FlexibleDate registrationDate;
    
    @JsonDeserialize(using = PhoneNumberDeserializer.class)
    private PhoneNumber phone;
    
    @JsonDeserialize(using = PriceDeserializer.class)
    private PriceAmount creditLimit;
    
    public CustomerData() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public FlexibleDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(FlexibleDate registrationDate) { 
        this.registrationDate = registrationDate; 
    }
    
    public PhoneNumber getPhone() { return phone; }
    public void setPhone(PhoneNumber phone) { this.phone = phone; }
    
    public PriceAmount getCreditLimit() { return creditLimit; }
    public void setCreditLimit(PriceAmount creditLimit) { this.creditLimit = creditLimit; }
    
    @Override
    public String toString() {
        return "CustomerData{id=" + id + ", name='" + name + 
               "', registrationDate=" + (registrationDate != null ? registrationDate.getValue() : null) +
               ", phone=" + (phone != null ? phone.getNormalized() : null) +
               ", creditLimit=" + (creditLimit != null ? creditLimit.getAmount() : null) + "}";
    }
}

/**
 * Flexible Boolean class
 */
class FlexibleBoolean {
    private boolean value;
    
    public FlexibleBoolean() {}
    
    public FlexibleBoolean(boolean value) {
        this.value = value;
    }
    
    public boolean getValue() { return value; }
    public void setValue(boolean value) { this.value = value; }
}

/**
 * Custom Deserializer for Boolean - Accepts yes/no, 1/0, on/off
 */
class FlexibleBooleanDeserializer extends JsonDeserializer<FlexibleBoolean> {
    @Override
    public FlexibleBoolean deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        String text = p.getText().toLowerCase().trim();
        
        boolean value = false;
        if (text.equals("true") || text.equals("yes") || text.equals("1") || 
            text.equals("on") || text.equals("enabled")) {
            value = true;
        }
        
        return new FlexibleBoolean(value);
    }
}

/**
 * Wrapper for Boolean
 */
class BooleanWrapper {
    @JsonDeserialize(using = FlexibleBooleanDeserializer.class)
    private FlexibleBoolean enabled;
    
    public FlexibleBoolean getEnabled() { return enabled; }
    public void setEnabled(FlexibleBoolean enabled) { this.enabled = enabled; }
}

/**
 * Status Enum
 */
enum AccountStatus {
    ACTIVE, INACTIVE, SUSPENDED, CLOSED
}

/**
 * Flexible Status class
 */
class FlexibleStatus {
    private AccountStatus value;
    
    public FlexibleStatus() {}
    
    public FlexibleStatus(AccountStatus value) {
        this.value = value;
    }
    
    public AccountStatus getValue() { return value; }
    public void setValue(AccountStatus value) { this.value = value; }
}

/**
 * Custom Deserializer for Enum with fallback
 */
class StatusDeserializer extends JsonDeserializer<FlexibleStatus> {
    @Override
    public FlexibleStatus deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        String text = p.getText().toUpperCase().trim();
        
        AccountStatus status;
        try {
            status = AccountStatus.valueOf(text);
        } catch (IllegalArgumentException e) {
            // Fallback to INACTIVE if unknown status
            status = AccountStatus.INACTIVE;
        }
        
        return new FlexibleStatus(status);
    }
}

/**
 * Wrapper for Status
 */
class StatusWrapper {
    @JsonDeserialize(using = StatusDeserializer.class)
    private FlexibleStatus status;
    
    public FlexibleStatus getStatus() { return status; }
    public void setStatus(FlexibleStatus status) { this.status = status; }
}

/**
 * Configuration for custom deserializers
 */
@Configuration
class CustomDeserializerConfiguration {
    
    @Bean
    public ObjectMapper customDeserializerMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        SimpleModule module = new SimpleModule();
        module.addDeserializer(FlexibleDate.class, new FlexibleDateDeserializer());
        module.addDeserializer(PriceAmount.class, new PriceDeserializer());
        module.addDeserializer(PhoneNumber.class, new PhoneNumberDeserializer());
        module.addDeserializer(FlexibleBoolean.class, new FlexibleBooleanDeserializer());
        module.addDeserializer(FlexibleStatus.class, new StatusDeserializer());
        
        mapper.registerModule(module);
        return mapper;
    }
}

/**
 * REST Controller demonstrating custom deserializers
 */
@RestController
@RequestMapping("/api/custom-deserializer")
class CustomDeserializerController {
    
    private final ObjectMapper objectMapper;
    
    public CustomDeserializerController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @PostMapping("/customer")
    public CustomerData createCustomer(@RequestBody String customerJson) throws JsonProcessingException {
        return objectMapper.readValue(customerJson, CustomerData.class);
    }
    
    @PostMapping("/parse-date")
    public FlexibleDate parseDate(@RequestBody String dateJson) throws JsonProcessingException {
        return objectMapper.readValue(dateJson, FlexibleDate.class);
    }
    
    @PostMapping("/parse-price")
    public PriceWrapper parsePrice(@RequestBody String priceJson) throws JsonProcessingException {
        return objectMapper.readValue(priceJson, PriceWrapper.class);
    }
    
    @PostMapping("/parse-phone")
    public PhoneWrapper parsePhone(@RequestBody String phoneJson) throws JsonProcessingException {
        return objectMapper.readValue(phoneJson, PhoneWrapper.class);
    }
}
