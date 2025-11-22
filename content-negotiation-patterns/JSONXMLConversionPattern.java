package com.example.contentnegotiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;

/**
 * JSON/XML CONVERSION PATTERN
 * ============================
 * 
 * Demonstrates automatic JSON and XML conversion based on Accept header.
 * Shows how to configure converters and handle both formats seamlessly.
 * 
 * Key Concepts:
 * - Jackson JSON serialization/deserialization
 * - Jackson XML serialization/deserialization
 * - Automatic format selection based on Accept header
 * - Custom serializers for complex types
 * - JAXB annotations for XML binding
 * - Format-specific configurations
 * 
 * Use Cases:
 * - REST APIs supporting multiple formats
 * - Legacy system integration (XML)
 * - Modern app integration (JSON)
 * - Data export in multiple formats
 * - Configuration file generation
 */

@SpringBootApplication
public class JSONXMLConversionPattern {

    public static void main(String[] args) {
        SpringApplication.run(JSONXMLConversionPattern.class, args);
        demonstrateConversions();
    }

    private static void demonstrateConversions() {
        System.out.println("=== JSON/XML Conversion Pattern Demonstrations ===\n");

        // Demo 1: Object to JSON
        System.out.println("1. Object to JSON Conversion:");
        ObjectMapper jsonMapper = new ObjectMapper();
        Customer customer = new Customer(1L, "John Doe", "john@example.com", 
            Arrays.asList("123 Main St", "456 Oak Ave"));
        try {
            String json = jsonMapper.writeValueAsString(customer);
            System.out.println("   JSON Output:\n" + json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Demo 2: Object to XML
        System.out.println("\n2. Object to XML Conversion:");
        XmlMapper xmlMapper = new XmlMapper();
        try {
            String xml = xmlMapper.writeValueAsString(customer);
            System.out.println("   XML Output:\n" + xml);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Demo 3: JSON to Object
        System.out.println("\n3. JSON to Object Conversion:");
        String jsonInput = "{\"id\":2,\"name\":\"Jane Smith\",\"email\":\"jane@example.com\"}";
        try {
            Customer fromJson = jsonMapper.readValue(jsonInput, Customer.class);
            System.out.println("   Parsed Customer: " + fromJson.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Demo 4: XML to Object
        System.out.println("\n4. XML to Object Conversion:");
        String xmlInput = "<Customer><id>3</id><name>Bob Wilson</name><email>bob@example.com</email></Customer>";
        try {
            Customer fromXml = xmlMapper.readValue(xmlInput, Customer.class);
            System.out.println("   Parsed Customer: " + fromXml.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Demo 5: Complex object conversion
        System.out.println("\n5. Complex Object Conversion:");
        Order order = new Order(
            100L,
            customer,
            Arrays.asList(
                new OrderItem(1L, "Product A", 2, 25.99),
                new OrderItem(2L, "Product B", 1, 49.99)
            ),
            LocalDateTime.now(),
            OrderStatus.PENDING
        );
        
        try {
            System.out.println("   Order as JSON:");
            System.out.println(jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(order));
            
            System.out.println("\n   Order as XML:");
            System.out.println(xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(order));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// ============================================================================
// CONFIGURATION
// ============================================================================

@Configuration
class ConversionConfiguration {

    /**
     * Configure JSON converter with custom settings
     */
    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure mapper
        mapper.findAndRegisterModules();
        // mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        converter.setObjectMapper(mapper);
        converter.setPrettyPrint(true);
        
        return converter;
    }

    /**
     * Configure XML converter with custom settings
     */
    @Bean
    public MappingJackson2XmlHttpMessageConverter xmlConverter() {
        MappingJackson2XmlHttpMessageConverter converter = new MappingJackson2XmlHttpMessageConverter();
        XmlMapper mapper = new XmlMapper();
        
        // Configure mapper
        mapper.findAndRegisterModules();
        
        converter.setObjectMapper(mapper);
        
        return converter;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper().findAndRegisterModules();
    }
}

// ============================================================================
// REST CONTROLLERS
// ============================================================================

@RestController
@RequestMapping("/api/customers")
class CustomerController {

    /**
     * Get customer in JSON or XML based on Accept header
     */
    @GetMapping(value = "/{id}", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        Customer customer = new Customer(
            id,
            "John Doe",
            "john@example.com",
            Arrays.asList("123 Main St", "Apt 4B", "City, State 12345")
        );
        
        return ResponseEntity.ok(customer);
    }

    /**
     * Get all customers
     */
    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public ResponseEntity<CustomerList> getCustomers() {
        List<Customer> customers = Arrays.asList(
            new Customer(1L, "John Doe", "john@example.com", Arrays.asList("Address 1")),
            new Customer(2L, "Jane Smith", "jane@example.com", Arrays.asList("Address 2")),
            new Customer(3L, "Bob Wilson", "bob@example.com", Arrays.asList("Address 3"))
        );
        
        return ResponseEntity.ok(new CustomerList(customers));
    }

    /**
     * Create customer accepting JSON or XML
     */
    @PostMapping(consumes = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@RequestBody Customer customer) {
        // Simulate saving
        customer.setId(System.currentTimeMillis());
        
        ApiResponse<Customer> response = new ApiResponse<>(
            true,
            "Customer created successfully",
            customer,
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update customer
     */
    @PutMapping(value = "/{id}", consumes = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer customer) {
        
        customer.setId(id);
        
        ApiResponse<Customer> response = new ApiResponse<>(
            true,
            "Customer updated successfully",
            customer,
            LocalDateTime.now()
        );
        
        return ResponseEntity.ok(response);
    }
}

@RestController
@RequestMapping("/api/orders")
class OrderController {

    /**
     * Get order with items (complex object)
     */
    @GetMapping(value = "/{id}", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Customer customer = new Customer(1L, "John Doe", "john@example.com", 
            Arrays.asList("123 Main St"));
        
        List<OrderItem> items = Arrays.asList(
            new OrderItem(1L, "Product A", 2, 25.99),
            new OrderItem(2L, "Product B", 1, 49.99),
            new OrderItem(3L, "Product C", 3, 15.99)
        );
        
        Order order = new Order(id, customer, items, LocalDateTime.now(), OrderStatus.PENDING);
        
        return ResponseEntity.ok(order);
    }

    /**
     * Get all orders
     */
    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public ResponseEntity<OrderList> getOrders() {
        Customer customer = new Customer(1L, "John Doe", "john@example.com", 
            Arrays.asList("123 Main St"));
        
        List<Order> orders = Arrays.asList(
            new Order(1L, customer, new ArrayList<>(), LocalDateTime.now(), OrderStatus.COMPLETED),
            new Order(2L, customer, new ArrayList<>(), LocalDateTime.now(), OrderStatus.PENDING),
            new Order(3L, customer, new ArrayList<>(), LocalDateTime.now(), OrderStatus.SHIPPED)
        );
        
        return ResponseEntity.ok(new OrderList(orders));
    }

    /**
     * Create order
     */
    @PostMapping(consumes = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
        order.setId(System.currentTimeMillis());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        
        ApiResponse<Order> response = new ApiResponse<>(
            true,
            "Order created successfully",
            order,
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

@RestController
@RequestMapping("/api/convert")
class ConversionController {

    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;

    public ConversionController(ObjectMapper jsonMapper, XmlMapper xmlMapper) {
        this.jsonMapper = jsonMapper;
        this.xmlMapper = xmlMapper;
    }

    /**
     * Convert JSON to XML
     */
    @PostMapping(value = "/json-to-xml", 
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> convertJsonToXml(@RequestBody String json) {
        try {
            Object obj = jsonMapper.readValue(json, Object.class);
            String xml = xmlMapper.writeValueAsString(obj);
            return ResponseEntity.ok(xml);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Conversion error: " + e.getMessage());
        }
    }

    /**
     * Convert XML to JSON
     */
    @PostMapping(value = "/xml-to-json",
        consumes = MediaType.APPLICATION_XML_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> convertXmlToJson(@RequestBody String xml) {
        try {
            Object obj = xmlMapper.readValue(xml, Object.class);
            String json = jsonMapper.writeValueAsString(obj);
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Conversion error: " + e.getMessage());
        }
    }

    /**
     * Format conversion endpoint
     */
    @PostMapping(value = "/format", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public ResponseEntity<ConversionResult> convertFormat(
            @RequestParam String input,
            @RequestParam String inputFormat,
            @RequestHeader("Accept") String accept) {
        
        ConversionResult result = new ConversionResult();
        result.inputFormat = inputFormat;
        result.outputFormat = accept.contains("xml") ? "XML" : "JSON";
        result.input = input;
        result.timestamp = LocalDateTime.now();
        
        try {
            if ("json".equalsIgnoreCase(inputFormat)) {
                Object obj = jsonMapper.readValue(input, Object.class);
                result.output = accept.contains("xml") ? 
                    xmlMapper.writeValueAsString(obj) : 
                    jsonMapper.writeValueAsString(obj);
            } else if ("xml".equalsIgnoreCase(inputFormat)) {
                Object obj = xmlMapper.readValue(input, Object.class);
                result.output = accept.contains("xml") ? 
                    xmlMapper.writeValueAsString(obj) : 
                    jsonMapper.writeValueAsString(obj);
            } else {
                result.error = "Unsupported input format: " + inputFormat;
            }
        } catch (Exception e) {
            result.error = e.getMessage();
        }
        
        return ResponseEntity.ok(result);
    }
}

// ============================================================================
// DOMAIN MODELS
// ============================================================================

class Customer {
    private Long id;
    private String name;
    private String email;
    private List<String> addresses;

    public Customer() {}

    public Customer(Long id, String name, String email, List<String> addresses) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.addresses = addresses;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<String> getAddresses() { return addresses; }
    public void setAddresses(List<String> addresses) { this.addresses = addresses; }
}

class CustomerList {
    private List<Customer> customers;

    public CustomerList() {}

    public CustomerList(List<Customer> customers) {
        this.customers = customers;
    }

    public List<Customer> getCustomers() { return customers; }
    public void setCustomers(List<Customer> customers) { this.customers = customers; }
}

class Order {
    private Long id;
    private Customer customer;
    private List<OrderItem> items;
    private LocalDateTime orderDate;
    private OrderStatus status;

    public Order() {}

    public Order(Long id, Customer customer, List<OrderItem> items, 
                LocalDateTime orderDate, OrderStatus status) {
        this.id = id;
        this.customer = customer;
        this.items = items;
        this.orderDate = orderDate;
        this.status = status;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}

class OrderItem {
    private Long id;
    private String productName;
    private Integer quantity;
    private Double price;

    public OrderItem() {}

    public OrderItem(Long id, String productName, Integer quantity, Double price) {
        this.id = id;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

class OrderList {
    private List<Order> orders;

    public OrderList() {}

    public OrderList(List<Order> orders) {
        this.orders = orders;
    }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
}

enum OrderStatus {
    PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED, COMPLETED
}

class ConversionResult {
    String inputFormat;
    String outputFormat;
    String input;
    String output;
    String error;
    LocalDateTime timestamp;

    // Getters and setters
    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }
    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data, LocalDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

/*
 * BEST PRACTICES:
 * ===============
 * 1. Use Jackson for both JSON and XML conversion
 * 2. Configure ObjectMapper and XmlMapper as beans
 * 3. Handle serialization/deserialization errors gracefully
 * 4. Use appropriate annotations (@JsonProperty, @JacksonXmlProperty)
 * 5. Configure date/time format consistently
 * 6. Enable pretty printing for development
 * 7. Handle null values appropriately
 * 8. Use proper MIME types in produces/consumes
 * 
 * JACKSON ANNOTATIONS:
 * ====================
 * @JsonProperty - Map property to JSON field
 * @JsonIgnore - Ignore field during serialization
 * @JsonFormat - Specify date/time format
 * @JsonInclude - Include/exclude null values
 * @JsonTypeInfo - Handle polymorphic types
 * @JacksonXmlRootElement - XML root element name
 * @JacksonXmlProperty - XML element/attribute mapping
 * @JacksonXmlElementWrapper - XML collection wrapper
 * 
 * COMMON PITFALLS:
 * ================
 * 1. Not configuring ObjectMapper properly
 * 2. Mixing JSON and XML annotations incorrectly
 * 3. Not handling circular references
 * 4. Poor date/time format handling
 * 5. Not testing both formats thoroughly
 * 
 * TESTING SCENARIOS:
 * ==================
 * # Get as JSON
 * curl -H "Accept: application/json" http://localhost:8080/api/customers/1
 * 
 * # Get as XML
 * curl -H "Accept: application/xml" http://localhost:8080/api/customers/1
 * 
 * # Post JSON
 * curl -X POST -H "Content-Type: application/json" -H "Accept: application/json" \
 *   -d '{"name":"John","email":"john@example.com"}' \
 *   http://localhost:8080/api/customers
 * 
 * # Post XML
 * curl -X POST -H "Content-Type: application/xml" -H "Accept: application/xml" \
 *   -d '<Customer><name>John</name><email>john@example.com</email></Customer>' \
 *   http://localhost:8080/api/customers
 * 
 * MAVEN DEPENDENCIES:
 * ===================
 * <dependency>
 *   <groupId>com.fasterxml.jackson.core</groupId>
 *   <artifactId>jackson-databind</artifactId>
 * </dependency>
 * <dependency>
 *   <groupId>com.fasterxml.jackson.dataformat</groupId>
 *   <artifactId>jackson-dataformat-xml</artifactId>
 * </dependency>
 * <dependency>
 *   <groupId>com.fasterxml.jackson.datatype</groupId>
 *   <artifactId>jackson-datatype-jsr310</artifactId>
 * </dependency>
 */
