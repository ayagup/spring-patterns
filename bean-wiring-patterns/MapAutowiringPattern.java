package com.spring.patterns.wiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Map Autowiring Pattern
 * 
 * Spring can inject a Map<String, T> where:
 * - Key = Bean name (String)
 * - Value = Bean instance (T)
 * 
 * This allows runtime selection of beans by name.
 * 
 * Characteristics:
 * - Injects ALL beans of type T as Map entries
 * - Key is the bean name
 * - Value is the bean instance
 * - Empty map if no beans found
 * - Useful for strategy pattern
 * - Runtime bean selection
 * 
 * Use Cases:
 * - Plugin registry (name → implementation)
 * - Strategy selection (type → strategy)
 * - Handler mapping (event → handler)
 * - Formatter registry (format → formatter)
 * - Parser selection (type → parser)
 */
@SpringBootApplication
public class MapAutowiringPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(MapAutowiringPattern.class, args);
        
        System.out.println("\n=== Map Autowiring Pattern ===");
        
        // Demonstrate map injection
        PaymentService paymentService = context.getBean(PaymentService.class);
        paymentService.showAvailableProcessors();
        paymentService.processPayment("stripe", 99.99);
        paymentService.processPayment("paypal", 149.99);
        
        // Demonstrate formatter service
        FormatterService formatterService = context.getBean(FormatterService.class);
        formatterService.formatData("json", new Object());
        formatterService.formatData("xml", new Object());
        
        // Demonstrate handler service
        EventHandlerService handlerService = context.getBean(EventHandlerService.class);
        handlerService.handleEvent("user", "User created");
        handlerService.handleEvent("order", "Order placed");
    }
}

/**
 * Configuration
 */
@Configuration
class MapWiringConfig {
    
    // Payment processors with specific names
    @Bean(name = "stripe")
    public PaymentProcessor stripeProcessor() {
        return new StripeProcessor();
    }
    
    @Bean(name = "paypal")
    public PaymentProcessor paypalProcessor() {
        return new PayPalProcessor();
    }
    
    @Bean(name = "square")
    public PaymentProcessor squareProcessor() {
        return new SquareProcessor();
    }
    
    @Bean(name = "bitcoin")
    public PaymentProcessor bitcoinProcessor() {
        return new BitcoinProcessor();
    }
}

/**
 * Payment Processor interface
 */
interface PaymentProcessor {
    String process(double amount);
    String getProcessorName();
}

/**
 * Stripe implementation
 */
class StripeProcessor implements PaymentProcessor {
    
    @Override
    public String process(double amount) {
        return "Stripe processed: $" + amount;
    }
    
    @Override
    public String getProcessorName() {
        return "Stripe";
    }
}

/**
 * PayPal implementation
 */
class PayPalProcessor implements PaymentProcessor {
    
    @Override
    public String process(double amount) {
        return "PayPal processed: $" + amount;
    }
    
    @Override
    public String getProcessorName() {
        return "PayPal";
    }
}

/**
 * Square implementation
 */
class SquareProcessor implements PaymentProcessor {
    
    @Override
    public String process(double amount) {
        return "Square processed: $" + amount;
    }
    
    @Override
    public String getProcessorName() {
        return "Square";
    }
}

/**
 * Bitcoin implementation
 */
class BitcoinProcessor implements PaymentProcessor {
    
    @Override
    public String process(double amount) {
        return "Bitcoin processed: $" + amount;
    }
    
    @Override
    public String getProcessorName() {
        return "Bitcoin";
    }
}

/**
 * Example 1: Map Injection for Runtime Selection
 */
@Service
class PaymentService {
    
    // Injects ALL PaymentProcessor beans as a Map
    // Key = bean name, Value = bean instance
    private final Map<String, PaymentProcessor> processors;
    
    @Autowired
    public PaymentService(Map<String, PaymentProcessor> processors) {
        this.processors = processors;
        System.out.println("\nPaymentService created with " + processors.size() + " processors:");
        processors.forEach((name, processor) -> 
            System.out.println("  - " + name + ": " + processor.getProcessorName())
        );
    }
    
    public String processPayment(String processorName, double amount) {
        System.out.println("\nProcessing payment via: " + processorName);
        
        PaymentProcessor processor = processors.get(processorName);
        if (processor != null) {
            return processor.process(amount);
        } else {
            return "Processor not found: " + processorName;
        }
    }
    
    public void showAvailableProcessors() {
        System.out.println("\nAvailable payment processors:");
        processors.keySet().forEach(name -> System.out.println("  - " + name));
    }
}

/**
 * Data Formatter interface
 */
interface DataFormatter {
    String format(Object data);
    String getFormatType();
}

/**
 * JSON Formatter
 */
@Component("json")
class JsonFormatter implements DataFormatter {
    
    @Override
    public String format(Object data) {
        return "{\"data\": \"" + data.toString() + "\"}";
    }
    
    @Override
    public String getFormatType() {
        return "JSON";
    }
}

/**
 * XML Formatter
 */
@Component("xml")
class XmlFormatter implements DataFormatter {
    
    @Override
    public String format(Object data) {
        return "<data>" + data.toString() + "</data>";
    }
    
    @Override
    public String getFormatType() {
        return "XML";
    }
}

/**
 * CSV Formatter
 */
@Component("csv")
class CsvFormatter implements DataFormatter {
    
    @Override
    public String format(Object data) {
        return "data," + data.toString();
    }
    
    @Override
    public String getFormatType() {
        return "CSV";
    }
}

/**
 * Example 2: Formatter Selection using Map
 */
@Service
class FormatterService {
    
    private final Map<String, DataFormatter> formatters;
    
    @Autowired
    public FormatterService(Map<String, DataFormatter> formatters) {
        this.formatters = formatters;
        System.out.println("\nFormatterService created with formatters:");
        formatters.forEach((name, formatter) -> 
            System.out.println("  - " + name + ": " + formatter.getFormatType())
        );
    }
    
    public String formatData(String format, Object data) {
        System.out.println("\nFormatting data as: " + format);
        
        DataFormatter formatter = formatters.get(format);
        if (formatter != null) {
            String result = formatter.format(data);
            System.out.println("Result: " + result);
            return result;
        } else {
            return "Formatter not found: " + format;
        }
    }
    
    public String formatMultiple(Object data) {
        System.out.println("\nFormatting in all formats:");
        StringBuilder result = new StringBuilder();
        formatters.forEach((name, formatter) -> {
            String formatted = formatter.format(data);
            result.append(name).append(": ").append(formatted).append("\n");
        });
        return result.toString();
    }
}

/**
 * Event Handler interface
 */
interface EventHandler {
    void handle(String data);
    String getHandlerType();
}

/**
 * User Event Handler
 */
@Component("user")
class UserEventHandler implements EventHandler {
    
    @Override
    public void handle(String data) {
        System.out.println("  [UserHandler] " + data);
    }
    
    @Override
    public String getHandlerType() {
        return "User Events";
    }
}

/**
 * Order Event Handler
 */
@Component("order")
class OrderEventHandler implements EventHandler {
    
    @Override
    public void handle(String data) {
        System.out.println("  [OrderHandler] " + data);
    }
    
    @Override
    public String getHandlerType() {
        return "Order Events";
    }
}

/**
 * Payment Event Handler
 */
@Component("payment")
class PaymentEventHandler implements EventHandler {
    
    @Override
    public void handle(String data) {
        System.out.println("  [PaymentHandler] " + data);
    }
    
    @Override
    public String getHandlerType() {
        return "Payment Events";
    }
}

/**
 * Example 3: Event Handler Registry
 */
@Service
class EventHandlerService {
    
    private final Map<String, EventHandler> handlers;
    
    @Autowired
    public EventHandlerService(Map<String, EventHandler> handlers) {
        this.handlers = handlers;
        System.out.println("\nEventHandlerService created with handlers:");
        handlers.forEach((name, handler) -> 
            System.out.println("  - " + name + ": " + handler.getHandlerType())
        );
    }
    
    public void handleEvent(String eventType, String data) {
        System.out.println("\nHandling event type: " + eventType);
        
        EventHandler handler = handlers.get(eventType);
        if (handler != null) {
            handler.handle(data);
        } else {
            System.out.println("No handler found for: " + eventType);
        }
    }
    
    public void handleAllEvents(String data) {
        System.out.println("\nBroadcasting to all handlers:");
        handlers.forEach((type, handler) -> {
            System.out.print(type + " -> ");
            handler.handle(data);
        });
    }
}

/**
 * Validator interface
 */
interface DataValidator {
    boolean validate(String data);
    String getValidatorName();
}

/**
 * Email Validator
 */
@Component("emailValidator")
class EmailDataValidator implements DataValidator {
    
    @Override
    public boolean validate(String data) {
        boolean valid = data.contains("@");
        System.out.println("  Email validation: " + (valid ? "✓" : "✗"));
        return valid;
    }
    
    @Override
    public String getValidatorName() {
        return "Email";
    }
}

/**
 * URL Validator
 */
@Component("urlValidator")
class UrlDataValidator implements DataValidator {
    
    @Override
    public boolean validate(String data) {
        boolean valid = data.startsWith("http://") || data.startsWith("https://");
        System.out.println("  URL validation: " + (valid ? "✓" : "✗"));
        return valid;
    }
    
    @Override
    public String getValidatorName() {
        return "URL";
    }
}

/**
 * Phone Validator
 */
@Component("phoneValidator")
class PhoneDataValidator implements DataValidator {
    
    @Override
    public boolean validate(String data) {
        boolean valid = data.matches("\\d{10}");
        System.out.println("  Phone validation: " + (valid ? "✓" : "✗"));
        return valid;
    }
    
    @Override
    public String getValidatorName() {
        return "Phone";
    }
}

/**
 * Example 4: Validator Registry
 */
@Service
class ValidationRegistry {
    
    private final Map<String, DataValidator> validators;
    
    @Autowired
    public ValidationRegistry(Map<String, DataValidator> validators) {
        this.validators = validators;
        System.out.println("\nValidationRegistry created with validators:");
        validators.forEach((name, validator) -> 
            System.out.println("  - " + name + ": " + validator.getValidatorName())
        );
    }
    
    public boolean validate(String validatorName, String data) {
        System.out.println("\nValidating with: " + validatorName);
        
        DataValidator validator = validators.get(validatorName);
        if (validator != null) {
            return validator.validate(data);
        } else {
            System.out.println("Validator not found: " + validatorName);
            return false;
        }
    }
    
    public Map<String, Boolean> validateAll(String data) {
        System.out.println("\nRunning all validators:");
        Map<String, Boolean> results = new java.util.HashMap<>();
        validators.forEach((name, validator) -> {
            boolean valid = validator.validate(data);
            results.put(name, valid);
        });
        return results;
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/map-wiring")
class MapWiringController {
    
    private final PaymentService paymentService;
    private final FormatterService formatterService;
    private final EventHandlerService handlerService;
    private final ValidationRegistry validationRegistry;
    
    public MapWiringController(PaymentService paymentService,
                              FormatterService formatterService,
                              EventHandlerService handlerService,
                              ValidationRegistry validationRegistry) {
        this.paymentService = paymentService;
        this.formatterService = formatterService;
        this.handlerService = handlerService;
        this.validationRegistry = validationRegistry;
    }
    
    @GetMapping("/payment/{processor}/{amount}")
    public String processPayment(String processor, double amount) {
        return paymentService.processPayment(processor, amount);
    }
    
    @GetMapping("/format/{format}")
    public String formatData(String format) {
        return formatterService.formatData(format, "Sample data");
    }
    
    @GetMapping("/event/{type}")
    public String handleEvent(String type) {
        handlerService.handleEvent(type, "Event data");
        return "Event handled: " + type;
    }
    
    @GetMapping("/validate/{validator}/{data}")
    public String validate(String validator, String data) {
        boolean valid = validationRegistry.validate(validator, data);
        return "Validation result: " + (valid ? "PASS" : "FAIL");
    }
    
    @GetMapping("/processors")
    public String listProcessors() {
        paymentService.showAvailableProcessors();
        return "Check console for processor list";
    }
}

/**
 * Key Points:
 * 
 * 1. Map Injection Syntax:
 *    @Autowired
 *    private Map<String, Service> services;
 *    // Key = Bean name
 *    // Value = Bean instance
 * 
 * 2. Bean Name as Key:
 *    @Component("myService")
 *    class MyService implements Service { }
 *    
 *    @Bean(name = "customService")
 *    public Service customService() { }
 *    
 *    // Map will have keys: "myService", "customService"
 * 
 * 3. Runtime Bean Selection:
 *    @Autowired
 *    private Map<String, PaymentProcessor> processors;
 *    
 *    public String pay(String type, double amount) {
 *        PaymentProcessor processor = processors.get(type);
 *        return processor.process(amount);
 *    }
 * 
 * 4. Use Cases:
 *    ✓ Plugin registry (name → plugin)
 *    ✓ Strategy pattern (type → strategy)
 *    ✓ Handler mapping (event → handler)
 *    ✓ Format selection (format → formatter)
 *    ✓ Validator registry (type → validator)
 * 
 * 5. Advantages:
 *    ✓ Runtime bean selection by name
 *    ✓ Clear mapping (name → implementation)
 *    ✓ Easy to list available beans
 *    ✓ Flexible strategy selection
 *    ✓ Type-safe
 * 
 * 6. Empty Map:
 *    - No beans found → Empty Map (not null)
 *    - Safe to use map.get(key) → returns null if not found
 * 
 * 7. Default Bean Names:
 *    @Component
 *    class MyService { }
 *    // Bean name: "myService" (uncapitalized class name)
 *    
 *    @Component("customName")
 *    class MyService { }
 *    // Bean name: "customName"
 * 
 * 8. Iteration Patterns:
 *    // All beans
 *    processors.forEach((name, processor) -> {
 *        processor.process(amount);
 *    });
 *    
 *    // Specific bean
 *    PaymentProcessor p = processors.get("stripe");
 *    
 *    // Check existence
 *    if (processors.containsKey("paypal")) { ... }
 * 
 * 9. Best Practices:
 *    ✓ Use descriptive bean names
 *    ✓ Document available bean names
 *    ✓ Handle missing beans gracefully
 *    ✓ Prefer map for runtime selection
 *    ✓ Use List<T> if order matters
 * 
 * 10. Comparison with Other Patterns:
 *     
 *     Map<String, T>:
 *     ✓ Runtime selection by name
 *     ✓ Clear mapping
 *     ✓ Easy lookup
 *     
 *     List<T>:
 *     ✓ Ordered processing
 *     ✓ All beans iteration
 *     ✓ No key needed
 *     
 *     @Qualifier:
 *     ✓ Compile-time selection
 *     ✓ Explicit bean
 *     ✓ Type-safe
 * 
 * 11. Testing:
 *     @TestConfiguration
 *     static class TestConfig {
 *         @Bean("mockProcessor")
 *         public PaymentProcessor mockProcessor() {
 *             return mock(PaymentProcessor.class);
 *         }
 *     }
 *     // Map will include "mockProcessor"
 */
