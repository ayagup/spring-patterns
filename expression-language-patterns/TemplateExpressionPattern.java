package com.spring.patterns.expressionlanguage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Template Expression Pattern
 * 
 * Demonstrates comprehensive usage of template expressions in SpEL:
 * - Template parser context with custom delimiters
 * - String interpolation
 * - Mixed literal and expression content
 * - Dynamic message generation
 * - Email/notification templates
 * - Report templates
 * - Formatting templates
 * - Localization templates
 */

// ===================== Domain Models =====================

record User(
    Long id,
    String username,
    String firstName,
    String lastName,
    String email,
    String role,
    LocalDate registrationDate
) {
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

record Product(
    Long id,
    String name,
    double price,
    String category,
    int stock,
    double discount
) {
    public double getFinalPrice() {
        return price * (1 - discount);
    }
    
    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }
}

record Order(
    String orderNumber,
    User customer,
    List<Product> products,
    double totalAmount,
    LocalDate orderDate,
    String status,
    String shippingAddress
) {
    public int getItemCount() {
        return products.size();
    }
}

record Invoice(
    String invoiceNumber,
    User customer,
    List<InvoiceItem> items,
    double subtotal,
    double tax,
    double total,
    LocalDate dueDate
) {
    record InvoiceItem(String description, int quantity, double unitPrice, double amount) {}
    
    public String getFormattedTotal() {
        return String.format("$%.2f", total);
    }
}

// ===================== Template Service =====================

@Service
class TemplateExpressionService {
    
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParserContext templateContext = new TemplateParserContext();
    
    /**
     * Basic template expression
     */
    public String basicTemplate() {
        String template = "Hello #{#name}! Welcome to #{#appName}.";
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("name", "John");
        context.setVariable("appName", "Spring Application");
        
        Expression exp = parser.parseExpression(template, templateContext);
        return exp.getValue(context, String.class);
    }
    
    /**
     * User greeting template
     */
    public String userGreeting(User user) {
        String template = "Welcome back, #{#user.getFullName()}! " +
                         "Your account (#{#user.email}) has been active since #{#user.registrationDate}.";
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("user", user);
        
        Expression exp = parser.parseExpression(template, templateContext);
        return exp.getValue(context, String.class);
    }
    
    /**
     * Product description template
     */
    public String productDescription(Product product) {
        String template = "#{#product.name} - #{#product.category}\n" +
                         "Regular Price: #{#product.getFormattedPrice()}\n" +
                         "Discount: #{#product.discount * 100}%\n" +
                         "Final Price: $#{T(String).format('%.2f', #product.getFinalPrice())}\n" +
                         "Stock: #{#product.stock > 0 ? #product.stock + ' units available' : 'Out of stock'}";
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("product", product);
        
        Expression exp = parser.parseExpression(template, templateContext);
        return exp.getValue(context, String.class);
    }
    
    /**
     * Order confirmation template
     */
    public String orderConfirmation(Order order) {
        String template = """
                Order Confirmation
                ==================
                Order Number: #{#order.orderNumber}
                Customer: #{#order.customer.getFullName()}
                Email: #{#order.customer.email}
                
                Order Details:
                - Items: #{#order.getItemCount()}
                - Total: $#{T(String).format('%.2f', #order.totalAmount)}
                - Status: #{#order.status}
                - Order Date: #{#order.orderDate}
                
                Shipping Address: #{#order.shippingAddress}
                
                #{#order.status == 'CONFIRMED' ? 'Your order will be shipped soon!' : 'Processing your order...'}
                """;
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("order", order);
        
        Expression exp = parser.parseExpression(template, templateContext);
        return exp.getValue(context, String.class);
    }
    
    /**
     * Email template
     */
    public String emailTemplate(User user, String subject, String message) {
        String template = """
                To: #{#user.email}
                From: noreply@example.com
                Subject: #{#subject}
                
                Dear #{#user.getFullName()},
                
                #{#message}
                
                Best regards,
                The #{#appName} Team
                
                ---
                This email was sent to #{#user.email}
                Registration Date: #{#user.registrationDate}
                User ID: #{#user.id}
                """;
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("user", user);
        context.setVariable("subject", subject);
        context.setVariable("message", message);
        context.setVariable("appName", "Spring Application");
        
        Expression exp = parser.parseExpression(template, templateContext);
        return exp.getValue(context, String.class);
    }
    
    /**
     * Invoice template
     */
    public String invoiceTemplate(Invoice invoice) {
        String template = """
                INVOICE
                =======
                Invoice Number: #{#invoice.invoiceNumber}
                Date: #{T(java.time.LocalDate).now()}
                Due Date: #{#invoice.dueDate}
                
                Bill To:
                #{#invoice.customer.getFullName()}
                #{#invoice.customer.email}
                
                Items:
                #{#itemsDescription}
                
                Subtotal: $#{T(String).format('%.2f', #invoice.subtotal)}
                Tax: $#{T(String).format('%.2f', #invoice.tax)}
                ------------------------
                Total: #{#invoice.getFormattedTotal()}
                
                Payment is due by #{#invoice.dueDate}
                """;
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("invoice", invoice);
        
        // Build items description
        StringBuilder items = new StringBuilder();
        for (Invoice.InvoiceItem item : invoice.items()) {
            items.append(String.format("%s (Qty: %d × $%.2f) = $%.2f\n",
                item.description(), item.quantity(), item.unitPrice(), item.amount()));
        }
        context.setVariable("itemsDescription", items.toString());
        
        Expression exp = parser.parseExpression(template, templateContext);
        return exp.getValue(context, String.class);
    }
    
    /**
     * Notification template with conditions
     */
    public String notificationTemplate(String eventType, Map<String, Object> data) {
        String template = switch (eventType) {
            case "ORDER_PLACED" -> """
                    New Order Received!
                    Order ##{#orderNumber} has been placed by #{#customerName}.
                    Total: $#{#total}
                    #{#urgent ? 'URGENT: Express shipping requested!' : ''}
                    """;
            case "PAYMENT_RECEIVED" -> """
                    Payment Confirmed
                    Payment of $#{#amount} received for Order ##{#orderNumber}.
                    Payment Method: #{#paymentMethod}
                    """;
            case "SHIPPING_UPDATE" -> """
                    Shipping Update
                    Order ##{#orderNumber} is #{#status}.
                    Tracking: #{#trackingNumber}
                    Estimated Delivery: #{#estimatedDelivery}
                    """;
            default -> "Unknown event type: " + eventType;
        };
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        data.forEach(context::setVariable);
        
        Expression exp = parser.parseExpression(template, templateContext);
        return exp.getValue(context, String.class);
    }
    
    /**
     * Report template
     */
    public String reportTemplate(Map<String, Object> stats) {
        String template = """
                Daily Sales Report
                ==================
                Date: #{#date}
                
                Summary:
                - Total Orders: #{#totalOrders}
                - Total Revenue: $#{T(String).format('%.2f', #totalRevenue)}
                - Average Order Value: $#{T(String).format('%.2f', #avgOrderValue)}
                - New Customers: #{#newCustomers}
                
                Performance:
                #{#totalRevenue > #target ? 'TARGET ACHIEVED! 🎉' : 'Below target'}
                #{#totalRevenue > #target ? 
                  'Revenue: ' + T(String).format('%.1f', (#totalRevenue / #target * 100)) + '% of target' : 
                  'Remaining: $' + T(String).format('%.2f', #target - #totalRevenue)}
                
                Top Category: #{#topCategory}
                """;
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        stats.forEach(context::setVariable);
        
        Expression exp = parser.parseExpression(template, templateContext);
        return exp.getValue(context, String.class);
    }
    
    /**
     * Custom parser context with different delimiters
     */
    public String customDelimitersTemplate() {
        // Use {{ }} instead of #{ }
        ParserContext customContext = new ParserContext() {
            @Override
            public boolean isTemplate() { return true; }
            @Override
            public String getExpressionPrefix() { return "{{"; }
            @Override
            public String getExpressionSuffix() { return "}}"; }
        };
        
        String template = "Hello {{#name}}! Today is {{T(java.time.LocalDate).now()}}.";
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("name", "Alice");
        
        Expression exp = parser.parseExpression(template, customContext);
        return exp.getValue(context, String.class);
    }
    
    /**
     * Dynamic template with variable content
     */
    public String dynamicTemplate(String templateString, Map<String, Object> variables) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        variables.forEach(context::setVariable);
        
        Expression exp = parser.parseExpression(templateString, templateContext);
        return exp.getValue(context, String.class);
    }
}

// ===================== Component with Template Expressions =====================

@Component
class TemplateExpressionComponent {
    
    @Value("#{T(java.time.LocalDate).now()}")
    private LocalDate currentDate;
    
    @Value("#{T(System).getProperty('user.name')}")
    private String systemUser;
    
    // Templates would typically come from configuration or database
    private final String welcomeTemplate = "Hello #{#name}! Today is #{#date}.";
    private final String summaryTemplate = "User #{#username} has #{#count} items.";
    
    public String generateWelcome(String name, LocalDate date) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("name", name);
        context.setVariable("date", date);
        
        Expression exp = parser.parseExpression(welcomeTemplate, new TemplateParserContext());
        return exp.getValue(context, String.class);
    }
    
    public String generateSummary(String username, int count) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("username", username);
        context.setVariable("count", count);
        
        Expression exp = parser.parseExpression(summaryTemplate, new TemplateParserContext());
        return exp.getValue(context, String.class);
    }
}

// ===================== REST Controller =====================

@RestController
@RequestMapping("/api/template")
class TemplateExpressionController {
    
    private final TemplateExpressionService templateService;
    private final TemplateExpressionComponent templateComponent;
    
    public TemplateExpressionController(
        TemplateExpressionService templateService,
        TemplateExpressionComponent templateComponent
    ) {
        this.templateService = templateService;
        this.templateComponent = templateComponent;
    }
    
    @GetMapping("/basic")
    public Map<String, String> basicTemplate() {
        return Map.of("result", templateService.basicTemplate());
    }
    
    @GetMapping("/greeting")
    public Map<String, String> userGreeting() {
        User user = new User(1L, "johndoe", "John", "Doe", "john@example.com", 
                           "USER", LocalDate.of(2020, 1, 15));
        return Map.of("result", templateService.userGreeting(user));
    }
    
    @GetMapping("/product")
    public Map<String, String> productDescription() {
        Product product = new Product(1L, "Laptop", 999.99, "Electronics", 10, 0.15);
        return Map.of("result", templateService.productDescription(product));
    }
    
    @GetMapping("/order")
    public Map<String, String> orderConfirmation() {
        User customer = new User(1L, "johndoe", "John", "Doe", "john@example.com", 
                               "USER", LocalDate.now());
        List<Product> products = List.of(
            new Product(1L, "Laptop", 999.99, "Electronics", 10, 0.0)
        );
        Order order = new Order("ORD-2024-001", customer, products, 999.99, 
                              LocalDate.now(), "CONFIRMED", "123 Main St, New York, NY");
        
        return Map.of("result", templateService.orderConfirmation(order));
    }
    
    @GetMapping("/email")
    public Map<String, String> emailTemplate() {
        User user = new User(1L, "johndoe", "John", "Doe", "john@example.com",
                           "USER", LocalDate.now());
        String subject = "Welcome to Our Platform";
        String message = "Thank you for joining us! We're excited to have you on board.";
        
        return Map.of("result", templateService.emailTemplate(user, subject, message));
    }
    
    @GetMapping("/invoice")
    public Map<String, String> invoiceTemplate() {
        User customer = new User(1L, "johndoe", "John", "Doe", "john@example.com",
                               "USER", LocalDate.now());
        List<Invoice.InvoiceItem> items = List.of(
            new Invoice.InvoiceItem("Web Development Services", 40, 150.0, 6000.0),
            new Invoice.InvoiceItem("Consulting", 10, 200.0, 2000.0)
        );
        Invoice invoice = new Invoice("INV-2024-001", customer, items, 8000.0, 
                                     800.0, 8800.0, LocalDate.now().plusDays(30));
        
        return Map.of("result", templateService.invoiceTemplate(invoice));
    }
    
    @GetMapping("/notification/{type}")
    public Map<String, String> notification(@PathVariable String type) {
        Map<String, Object> data = switch (type) {
            case "order" -> Map.of(
                "orderNumber", "ORD-123",
                "customerName", "John Doe",
                "total", 999.99,
                "urgent", true
            );
            case "payment" -> Map.of(
                "orderNumber", "ORD-123",
                "amount", 999.99,
                "paymentMethod", "Credit Card"
            );
            case "shipping" -> Map.of(
                "orderNumber", "ORD-123",
                "status", "in transit",
                "trackingNumber", "TRACK-456",
                "estimatedDelivery", "Dec 25, 2024"
            );
            default -> Map.of();
        };
        
        String eventType = type.toUpperCase() + "_" + 
                          (type.equals("order") ? "PLACED" : 
                           type.equals("payment") ? "RECEIVED" : "UPDATE");
        
        return Map.of("result", templateService.notificationTemplate(eventType, data));
    }
    
    @GetMapping("/report")
    public Map<String, String> report() {
        Map<String, Object> stats = Map.of(
            "date", LocalDate.now(),
            "totalOrders", 150,
            "totalRevenue", 45000.0,
            "avgOrderValue", 300.0,
            "newCustomers", 25,
            "target", 40000.0,
            "topCategory", "Electronics"
        );
        
        return Map.of("result", templateService.reportTemplate(stats));
    }
    
    @GetMapping("/custom-delimiters")
    public Map<String, String> customDelimiters() {
        return Map.of("result", templateService.customDelimitersTemplate());
    }
    
    @PostMapping("/dynamic")
    public Map<String, String> dynamicTemplate(@RequestBody Map<String, Object> request) {
        String template = (String) request.get("template");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.getOrDefault("variables", Map.of());
        
        String result = templateService.dynamicTemplate(template, variables);
        return Map.of("result", result);
    }
    
    @GetMapping("/component/welcome")
    public Map<String, String> componentWelcome() {
        String result = templateComponent.generateWelcome("Alice", LocalDate.now());
        return Map.of("result", result);
    }
    
    @GetMapping("/component/summary")
    public Map<String, String> componentSummary() {
        String result = templateComponent.generateSummary("bob123", 42);
        return Map.of("result", result);
    }
}

/**
 * Key Concepts Demonstrated:
 * 
 * 1. Template Parser Context:
 *    - TemplateParserContext - default #{} delimiters
 *    - Custom parser context with different delimiters
 *    - isTemplate() returns true
 * 
 * 2. Template Syntax:
 *    - Literal text combined with expressions
 *    - Default delimiters: #{}
 *    - Custom delimiters: configurable
 * 
 * 3. Expression Types in Templates:
 *    - Variables: #{#variableName}
 *    - Properties: #{#object.property}
 *    - Methods: #{#object.method()}
 *    - Operations: #{#value1 + #value2}
 *    - Conditionals: #{condition ? true : false}
 * 
 * 4. Template Use Cases:
 *    - Email templates
 *    - Notification messages
 *    - Report generation
 *    - Invoice/receipt templates
 *    - Confirmation messages
 *    - Dynamic content generation
 * 
 * 5. Variables in Templates:
 *    - Set using context.setVariable()
 *    - Access with # prefix: #variableName
 *    - Can be any Java object
 * 
 * 6. Formatting in Templates:
 *    - String.format() for numbers
 *    - DateTimeFormatter for dates
 *    - Custom formatting methods
 * 
 * 7. Conditional Content:
 *    - Ternary operator: #{condition ? text : ''}
 *    - Show/hide sections based on conditions
 *    - Dynamic messages
 * 
 * 8. Multi-line Templates:
 *    - Text blocks (Java 15+)
 *    - Multiple expressions
 *    - Formatted output
 * 
 * Testing Examples:
 * 
 * curl http://localhost:8080/api/template/basic
 * curl http://localhost:8080/api/template/greeting
 * curl http://localhost:8080/api/template/order
 * curl http://localhost:8080/api/template/email
 * curl http://localhost:8080/api/template/invoice
 * curl http://localhost:8080/api/template/report
 * 
 * # Dynamic template
 * curl -X POST http://localhost:8080/api/template/dynamic \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "template": "Hello #{#name}! You have #{#count} messages.",
 *     "variables": {"name": "John", "count": 5}
 *   }'
 * 
 * Template Examples:
 * 
 * # Simple
 * "Hello #{#name}!"
 * 
 * # With method
 * "Welcome #{#user.getFullName()}!"
 * 
 * # With operations
 * "Total: $#{#price * #quantity}"
 * 
 * # Conditional
 * "#{#stock > 0 ? 'In Stock' : 'Out of Stock'}"
 * 
 * # Complex
 * "Order #{#order.id} for #{#order.customer.name} - $#{#order.total}"
 * 
 * Best Practices:
 * 
 * 1. Store templates externally (database, files)
 * 2. Validate template syntax
 * 3. Handle null values gracefully
 * 4. Use meaningful variable names
 * 5. Document available template variables
 * 6. Test templates with various data
 * 7. Consider security (avoid user-provided templates)
 * 8. Cache compiled templates for performance
 * 9. Use localization for multi-language support
 * 10. Provide default values for optional variables
 */
