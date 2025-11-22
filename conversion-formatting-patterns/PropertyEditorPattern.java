package com.example.conversion.propertyeditor;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Property Editor Pattern - Demonstrates Spring's PropertyEditor System
 * 
 * This pattern shows how to:
 * 1. Implement custom PropertyEditor classes
 * 2. Register PropertyEditors globally
 * 3. Use CustomEditorConfigurer for bean factory registration
 * 4. Register PropertyEditors per controller
 * 5. Convert string values to custom objects
 * 6. Handle legacy property editing
 * 7. Integrate with Spring MVC data binding
 * 8. Compare PropertyEditor vs Converter
 * 9. Use PropertyEditorSupport for convenience
 * 10. Handle null and invalid values
 * 
 * Key Concepts:
 * - PropertyEditor: JavaBeans component for property conversion
 * - PropertyEditorSupport: Base class for custom editors
 * - CustomEditorConfigurer: Global editor registration
 * - PropertyEditorRegistrar: Reusable editor registration
 * - WebDataBinder: MVC-specific editor registration
 * 
 * PropertyEditor Features:
 * - String-to-Object conversion
 * - Object-to-String conversion
 * - Legacy JavaBeans integration
 * - Stateful conversion
 * - Simple API
 * 
 * Limitations vs Modern Converters:
 * - Not thread-safe (stateful)
 * - String-based only
 * - No generic type support
 * - Legacy API
 * - Less flexible than Converter
 * 
 * When to Use:
 * - Legacy code integration
 * - Simple string conversions
 * - JavaBeans property binding
 * - Web form data binding
 * 
 * Dependencies:
 * - spring-beans
 * - spring-context
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class PropertyEditorPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(PropertyEditorPattern.class, args);
        demonstratePropertyEditors(context);
    }
    
    /**
     * Demonstrates various PropertyEditor scenarios
     */
    private static void demonstratePropertyEditors(org.springframework.context.ApplicationContext context) {
        System.out.println("=== Property Editor Pattern Demonstrations ===\n");
        
        PropertyEditorService service = context.getBean(PropertyEditorService.class);
        
        // Demo 1: Custom object conversion
        demonstrateCustomObjectConversion(service);
        
        // Demo 2: Money conversion
        demonstrateMoneyConversion(service);
        
        // Demo 3: Date range conversion
        demonstrateDateRangeConversion(service);
        
        // Demo 4: Address conversion
        demonstrateAddressConversion(service);
    }
    
    /**
     * Demonstrates custom object conversion
     */
    private static void demonstrateCustomObjectConversion(PropertyEditorService service) {
        System.out.println("1. Custom Object Conversion:");
        
        String customerString = "John Doe:john@example.com:PREMIUM";
        Customer customer = service.parseCustomer(customerString);
        System.out.println("   Parsed Customer: " + customer);
        System.out.println();
    }
    
    /**
     * Demonstrates money conversion
     */
    private static void demonstrateMoneyConversion(PropertyEditorService service) {
        System.out.println("2. Money Conversion:");
        
        String moneyString = "USD 1,234.56";
        Money money = service.parseMoney(moneyString);
        System.out.println("   Parsed Money: " + money);
        System.out.println();
    }
    
    /**
     * Demonstrates date range conversion
     */
    private static void demonstrateDateRangeConversion(PropertyEditorService service) {
        System.out.println("3. Date Range Conversion:");
        
        String dateRangeString = "2024-01-01 to 2024-12-31";
        DateRange dateRange = service.parseDateRange(dateRangeString);
        System.out.println("   Parsed DateRange: " + dateRange);
        System.out.println();
    }
    
    /**
     * Demonstrates address conversion
     */
    private static void demonstrateAddressConversion(PropertyEditorService service) {
        System.out.println("4. Address Conversion:");
        
        String addressString = "123 Main St|Springfield|IL|62701|USA";
        Address address = service.parseAddress(addressString);
        System.out.println("   Parsed Address: " + address);
        System.out.println();
    }
}

// ============================================================================
// Property Editors
// ============================================================================

/**
 * PropertyEditor for Customer objects
 * Format: "name:email:membershipLevel"
 */
class CustomerPropertyEditor extends PropertyEditorSupport {
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().isEmpty()) {
            setValue(null);
            return;
        }
        
        String[] parts = text.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid customer format. Expected: name:email:level");
        }
        
        Customer customer = new Customer();
        customer.setName(parts[0].trim());
        customer.setEmail(parts[1].trim());
        customer.setMembershipLevel(MembershipLevel.valueOf(parts[2].trim()));
        
        setValue(customer);
    }
    
    @Override
    public String getAsText() {
        Customer customer = (Customer) getValue();
        if (customer == null) {
            return "";
        }
        return customer.getName() + ":" + customer.getEmail() + ":" + customer.getMembershipLevel();
    }
}

/**
 * PropertyEditor for Money objects
 * Format: "CUR amount" (e.g., "USD 123.45")
 */
class MoneyPropertyEditor extends PropertyEditorSupport {
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().isEmpty()) {
            setValue(null);
            return;
        }
        
        String[] parts = text.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid money format. Expected: CUR amount");
        }
        
        String currency = parts[0].trim();
        String amountStr = parts[1].replace(",", "");
        BigDecimal amount = new BigDecimal(amountStr);
        
        Money money = new Money(Currency.getInstance(currency), amount);
        setValue(money);
    }
    
    @Override
    public String getAsText() {
        Money money = (Money) getValue();
        if (money == null) {
            return "";
        }
        return money.getCurrency().getCurrencyCode() + " " + 
               String.format("%,.2f", money.getAmount());
    }
}

/**
 * PropertyEditor for DateRange objects
 * Format: "yyyy-MM-dd to yyyy-MM-dd"
 */
class DateRangePropertyEditor extends PropertyEditorSupport {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().isEmpty()) {
            setValue(null);
            return;
        }
        
        String[] parts = text.split(" to ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid date range format. Expected: yyyy-MM-dd to yyyy-MM-dd");
        }
        
        LocalDate startDate = LocalDate.parse(parts[0].trim(), FORMATTER);
        LocalDate endDate = LocalDate.parse(parts[1].trim(), FORMATTER);
        
        DateRange dateRange = new DateRange(startDate, endDate);
        setValue(dateRange);
    }
    
    @Override
    public String getAsText() {
        DateRange range = (DateRange) getValue();
        if (range == null) {
            return "";
        }
        return range.getStartDate().format(FORMATTER) + " to " + 
               range.getEndDate().format(FORMATTER);
    }
}

/**
 * PropertyEditor for Address objects
 * Format: "street|city|state|zip|country"
 */
class AddressPropertyEditor extends PropertyEditorSupport {
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().isEmpty()) {
            setValue(null);
            return;
        }
        
        String[] parts = text.split("\\|");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid address format. Expected: street|city|state|zip|country");
        }
        
        Address address = new Address();
        address.setStreet(parts[0].trim());
        address.setCity(parts[1].trim());
        address.setState(parts[2].trim());
        address.setZipCode(parts[3].trim());
        address.setCountry(parts[4].trim());
        
        setValue(address);
    }
    
    @Override
    public String getAsText() {
        Address address = (Address) getValue();
        if (address == null) {
            return "";
        }
        return String.join("|", 
            address.getStreet(),
            address.getCity(),
            address.getState(),
            address.getZipCode(),
            address.getCountry()
        );
    }
}

// ============================================================================
// PropertyEditor Registrar
// ============================================================================

/**
 * Registers custom PropertyEditors
 */
class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {
    
    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        registry.registerCustomEditor(Customer.class, new CustomerPropertyEditor());
        registry.registerCustomEditor(Money.class, new MoneyPropertyEditor());
        registry.registerCustomEditor(DateRange.class, new DateRangePropertyEditor());
        registry.registerCustomEditor(Address.class, new AddressPropertyEditor());
    }
}

// ============================================================================
// Service Using PropertyEditors
// ============================================================================

/**
 * Service demonstrating PropertyEditor usage
 */
@Service
class PropertyEditorService {
    
    private final PropertyEditorRegistrar registrar = new CustomPropertyEditorRegistrar();
    
    /**
     * Parses customer from string
     */
    public Customer parseCustomer(String text) {
        CustomerPropertyEditor editor = new CustomerPropertyEditor();
        editor.setAsText(text);
        return (Customer) editor.getValue();
    }
    
    /**
     * Parses money from string
     */
    public Money parseMoney(String text) {
        MoneyPropertyEditor editor = new MoneyPropertyEditor();
        editor.setAsText(text);
        return (Money) editor.getValue();
    }
    
    /**
     * Parses date range from string
     */
    public DateRange parseDateRange(String text) {
        DateRangePropertyEditor editor = new DateRangePropertyEditor();
        editor.setAsText(text);
        return (DateRange) editor.getValue();
    }
    
    /**
     * Parses address from string
     */
    public Address parseAddress(String text) {
        AddressPropertyEditor editor = new AddressPropertyEditor();
        editor.setAsText(text);
        return (Address) editor.getValue();
    }
    
    /**
     * Formats customer to string
     */
    public String formatCustomer(Customer customer) {
        CustomerPropertyEditor editor = new CustomerPropertyEditor();
        editor.setValue(customer);
        return editor.getAsText();
    }
    
    /**
     * Formats money to string
     */
    public String formatMoney(Money money) {
        MoneyPropertyEditor editor = new MoneyPropertyEditor();
        editor.setValue(money);
        return editor.getAsText();
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * Customer entity
 */
class Customer {
    private Long id;
    private String name;
    private String email;
    private MembershipLevel membershipLevel;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public MembershipLevel getMembershipLevel() { return membershipLevel; }
    public void setMembershipLevel(MembershipLevel membershipLevel) { 
        this.membershipLevel = membershipLevel; 
    }
    
    @Override
    public String toString() {
        return "Customer{name='" + name + "', email='" + email + 
               "', level=" + membershipLevel + '}';
    }
}

/**
 * Membership level enum
 */
enum MembershipLevel {
    BASIC, SILVER, GOLD, PLATINUM, PREMIUM
}

/**
 * Money value object
 */
class Money {
    private final Currency currency;
    private final BigDecimal amount;
    
    public Money(Currency currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
    }
    
    public Currency getCurrency() { return currency; }
    public BigDecimal getAmount() { return amount; }
    
    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + amount;
    }
}

/**
 * Date range value object
 */
class DateRange {
    private final LocalDate startDate;
    private final LocalDate endDate;
    
    public DateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    
    @Override
    public String toString() {
        return startDate + " to " + endDate;
    }
}

/**
 * Address value object
 */
class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    // Getters and setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " " + zipCode + ", " + country;
    }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating PropertyEditor usage in MVC
 */
@RestController
@RequestMapping("/api/property-editor")
class PropertyEditorController {
    
    private final PropertyEditorService service;
    
    public PropertyEditorController(PropertyEditorService service) {
        this.service = service;
    }
    
    /**
     * Registers custom PropertyEditors for this controller
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Customer.class, new CustomerPropertyEditor());
        binder.registerCustomEditor(Money.class, new MoneyPropertyEditor());
        binder.registerCustomEditor(DateRange.class, new DateRangePropertyEditor());
        binder.registerCustomEditor(Address.class, new AddressPropertyEditor());
    }
    
    /**
     * Parse customer from query parameter
     * Example: /api/property-editor/customer?data=John Doe:john@example.com:PREMIUM
     */
    @GetMapping("/customer")
    public ResponseEntity<Customer> parseCustomer(@RequestParam String data) {
        Customer customer = service.parseCustomer(data);
        return ResponseEntity.ok(customer);
    }
    
    /**
     * Parse money from query parameter
     * Example: /api/property-editor/money?data=USD 1234.56
     */
    @GetMapping("/money")
    public ResponseEntity<Money> parseMoney(@RequestParam String data) {
        Money money = service.parseMoney(data);
        return ResponseEntity.ok(money);
    }
    
    /**
     * Parse date range from query parameter
     * Example: /api/property-editor/date-range?data=2024-01-01 to 2024-12-31
     */
    @GetMapping("/date-range")
    public ResponseEntity<DateRange> parseDateRange(@RequestParam String data) {
        DateRange dateRange = service.parseDateRange(data);
        return ResponseEntity.ok(dateRange);
    }
    
    /**
     * Parse address from query parameter
     * Example: /api/property-editor/address?data=123 Main St|Springfield|IL|62701|USA
     */
    @GetMapping("/address")
    public ResponseEntity<Address> parseAddress(@RequestParam String data) {
        Address address = service.parseAddress(data);
        return ResponseEntity.ok(address);
    }
    
    /**
     * Format customer to string
     */
    @PostMapping("/format-customer")
    public ResponseEntity<Map<String, String>> formatCustomer(@RequestBody Customer customer) {
        String formatted = service.formatCustomer(customer);
        Map<String, String> response = new HashMap<>();
        response.put("formatted", formatted);
        return ResponseEntity.ok(response);
    }
}
