package com.example.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Custom Serializer Pattern
 * 
 * Demonstrates creating custom serializers for specific types or fields in Jackson.
 * 
 * Key Concepts:
 * 1. JsonSerializer<T> - Custom serializer interface
 * 2. @JsonSerialize annotation - Apply custom serializer
 * 3. SerializerProvider - Context for serialization
 * 4. JsonGenerator - Low-level JSON writing
 * 5. Custom date/time formatters
 * 6. Sensitive data masking
 * 7. Complex type serialization
 * 8. Conditional serialization logic
 * 9. Field-level customization
 * 10. Module registration for custom serializers
 * 
 * Use Cases:
 * - Custom date/time formatting
 * - Masking sensitive data (SSN, credit cards)
 * - Currency formatting
 * - Enum custom representation
 * - Computed fields
 * - Data transformation during serialization
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class CustomSerializerPattern {

    public static void main(String[] args) {
        SpringApplication.run(CustomSerializerPattern.class, args);
        
        // Demo custom serializers
        demonstrateCustomSerializers();
    }
    
    private static void demonstrateCustomSerializers() {
        System.out.println("=== Custom Serializer Pattern Demo ===\n");
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            
            // Register custom serializers
            SimpleModule module = new SimpleModule();
            module.addSerializer(CreditCard.class, new CreditCardSerializer());
            module.addSerializer(Money.class, new MoneySerializer());
            module.addSerializer(SocialSecurityNumber.class, new SSNSerializer());
            mapper.registerModule(module);
            
            // 1. Custom Date Serialization
            PersonWithCustomDates person = new PersonWithCustomDates(
                1L, 
                "John Doe", 
                LocalDate.of(1990, 5, 15),
                LocalDateTime.now()
            );
            
            String personJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(person);
            System.out.println("1. Custom Date Serialization:");
            System.out.println(personJson);
            System.out.println();
            
            // 2. Masked Credit Card
            CreditCard creditCard = new CreditCard("1234567890123456", "John Doe", "12/25", "123");
            String cardJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(creditCard);
            System.out.println("2. Masked Credit Card:");
            System.out.println(cardJson);
            System.out.println();
            
            // 3. Currency Formatting
            Money price = new Money(new BigDecimal("1234.56"), "USD");
            String priceJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(price);
            System.out.println("3. Custom Currency Formatting:");
            System.out.println(priceJson);
            System.out.println();
            
            // 4. SSN Masking
            SocialSecurityNumber ssn = new SocialSecurityNumber("123-45-6789");
            String ssnJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ssn);
            System.out.println("4. Masked SSN:");
            System.out.println(ssnJson);
            System.out.println();
            
            // 5. Complex Custom Object
            BankAccount account = new BankAccount(
                101L,
                "ACC-123456",
                new Money(new BigDecimal("5000.75"), "USD"),
                new CreditCard("4532123456789012", "Alice Johnson", "06/26", "456"),
                new SocialSecurityNumber("987-65-4321")
            );
            
            String accountJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(account);
            System.out.println("5. Complex Object with Multiple Custom Serializers:");
            System.out.println(accountJson);
            System.out.println();
            
            // 6. Collection with Custom Serializers
            List<TransactionRecord> transactions = new ArrayList<>();
            transactions.add(new TransactionRecord(
                1L, 
                "TXN-001", 
                new Money(new BigDecimal("150.50"), "USD"),
                LocalDateTime.now().minusDays(5)
            ));
            transactions.add(new TransactionRecord(
                2L, 
                "TXN-002", 
                new Money(new BigDecimal("2500.00"), "EUR"),
                LocalDateTime.now().minusDays(2)
            ));
            
            String transactionsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(transactions);
            System.out.println("6. Transaction Collection:");
            System.out.println(transactionsJson);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Serialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Person with custom date serialization
 */
class PersonWithCustomDates {
    private Long id;
    private String name;
    
    @JsonSerialize(using = CustomLocalDateSerializer.class)
    private LocalDate birthDate;
    
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime createdAt;
    
    public PersonWithCustomDates() {}
    
    public PersonWithCustomDates(Long id, String name, LocalDate birthDate, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

/**
 * Custom LocalDate Serializer
 */
class CustomLocalDateSerializer extends JsonSerializer<LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    
    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        if (value != null) {
            gen.writeString(value.format(FORMATTER));
        }
    }
}

/**
 * Custom LocalDateTime Serializer
 */
class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
    
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        if (value != null) {
            gen.writeString(value.format(FORMATTER));
        }
    }
}

/**
 * Credit Card with masking serializer
 */
class CreditCard {
    private String number;
    private String holderName;
    private String expiryDate;
    private String cvv;
    
    public CreditCard() {}
    
    public CreditCard(String number, String holderName, String expiryDate, String cvv) {
        this.number = number;
        this.holderName = holderName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }
    
    // Getters and Setters
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}

/**
 * Custom Credit Card Serializer - Masks card number
 */
class CreditCardSerializer extends JsonSerializer<CreditCard> {
    @Override
    public void serialize(CreditCard value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        gen.writeStartObject();
        
        // Mask card number - show only last 4 digits
        String maskedNumber = "****-****-****-" + value.getNumber().substring(value.getNumber().length() - 4);
        gen.writeStringField("number", maskedNumber);
        
        gen.writeStringField("holderName", value.getHolderName());
        gen.writeStringField("expiryDate", value.getExpiryDate());
        
        // Don't expose CVV
        gen.writeStringField("cvv", "***");
        
        gen.writeEndObject();
    }
}

/**
 * Money class for currency representation
 */
class Money {
    private BigDecimal amount;
    private String currency;
    
    public Money() {}
    
    public Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    // Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}

/**
 * Custom Money Serializer
 */
class MoneySerializer extends JsonSerializer<Money> {
    @Override
    public void serialize(Money value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        gen.writeStartObject();
        gen.writeStringField("formatted", String.format("%s %.2f", value.getCurrency(), value.getAmount()));
        gen.writeNumberField("amount", value.getAmount());
        gen.writeStringField("currency", value.getCurrency());
        gen.writeEndObject();
    }
}

/**
 * Social Security Number
 */
class SocialSecurityNumber {
    private String value;
    
    public SocialSecurityNumber() {}
    
    public SocialSecurityNumber(String value) {
        this.value = value;
    }
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

/**
 * Custom SSN Serializer - Masks SSN
 */
class SSNSerializer extends JsonSerializer<SocialSecurityNumber> {
    @Override
    public void serialize(SocialSecurityNumber value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        // Show only last 4 digits: ***-**-1234
        String ssn = value.getValue();
        String masked = "***-**-" + ssn.substring(ssn.length() - 4);
        gen.writeString(masked);
    }
}

/**
 * Bank Account with multiple custom serialized fields
 */
class BankAccount {
    private Long id;
    private String accountNumber;
    private Money balance;
    private CreditCard linkedCard;
    private SocialSecurityNumber ssn;
    
    public BankAccount() {}
    
    public BankAccount(Long id, String accountNumber, Money balance, 
                      CreditCard linkedCard, SocialSecurityNumber ssn) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.linkedCard = linkedCard;
        this.ssn = ssn;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public Money getBalance() { return balance; }
    public void setBalance(Money balance) { this.balance = balance; }
    
    public CreditCard getLinkedCard() { return linkedCard; }
    public void setLinkedCard(CreditCard linkedCard) { this.linkedCard = linkedCard; }
    
    public SocialSecurityNumber getSsn() { return ssn; }
    public void setSsn(SocialSecurityNumber ssn) { this.ssn = ssn; }
}

/**
 * Transaction Record
 */
class TransactionRecord {
    private Long id;
    private String transactionId;
    private Money amount;
    
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime timestamp;
    
    public TransactionRecord() {}
    
    public TransactionRecord(Long id, String transactionId, Money amount, LocalDateTime timestamp) {
        this.id = id;
        this.transactionId = transactionId;
        this.amount = amount;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public Money getAmount() { return amount; }
    public void setAmount(Money amount) { this.amount = amount; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

/**
 * Configuration for custom serializers
 */
@Configuration
class CustomSerializerConfiguration {
    
    @Bean
    public ObjectMapper customObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        SimpleModule module = new SimpleModule();
        module.addSerializer(CreditCard.class, new CreditCardSerializer());
        module.addSerializer(Money.class, new MoneySerializer());
        module.addSerializer(SocialSecurityNumber.class, new SSNSerializer());
        
        mapper.registerModule(module);
        return mapper;
    }
}

/**
 * REST Controller demonstrating custom serializers
 */
@RestController
@RequestMapping("/api/custom-serializer")
class CustomSerializerController {
    
    private final ObjectMapper objectMapper;
    
    public CustomSerializerController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @GetMapping("/account/{id}")
    public BankAccount getAccount(@PathVariable Long id) {
        return new BankAccount(
            id,
            "ACC-" + id,
            new Money(new BigDecimal("10000.50"), "USD"),
            new CreditCard("4532123456789012", "Customer " + id, "12/26", "123"),
            new SocialSecurityNumber("123-45-6789")
        );
    }
    
    @GetMapping("/transaction/{id}")
    public TransactionRecord getTransaction(@PathVariable Long id) {
        return new TransactionRecord(
            id,
            "TXN-" + id,
            new Money(new BigDecimal("500.00"), "USD"),
            LocalDateTime.now()
        );
    }
    
    @GetMapping("/person/{id}")
    public PersonWithCustomDates getPerson(@PathVariable Long id) {
        return new PersonWithCustomDates(
            id,
            "Person " + id,
            LocalDate.of(1990, 1, 1).plusYears(id),
            LocalDateTime.now()
        );
    }
}
