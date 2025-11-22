package com.example.conversion.conversionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Conversion Service Pattern - Demonstrates Spring's ConversionService Infrastructure
 * 
 * This pattern shows how to:
 * 1. Configure GenericConversionService
 * 2. Use DefaultConversionService
 * 3. Configure FormattingConversionService
 * 4. Register converters programmatically
 * 5. Register converter factories
 * 6. Use ConverterRegistry
 * 7. Create custom ConversionService beans
 * 8. Integrate with Spring MVC
 * 9. Chain conversion operations
 * 10. Handle conversion failures gracefully
 * 
 * Key Concepts:
 * - ConversionService: Central conversion API
 * - GenericConversionService: Basic implementation
 * - DefaultConversionService: With default converters
 * - FormattingConversionService: With formatter support
 * - ConverterRegistry: Converter registration
 * 
 * ConversionService Hierarchy:
 * 1. ConversionService (interface)
 *    - canConvert(Class, Class)
 *    - convert(Object, Class)
 * 
 * 2. ConfigurableConversionService
 *    - Extends ConversionService
 *    - Extends ConverterRegistry
 * 
 * 3. GenericConversionService
 *    - Base implementation
 *    - No default converters
 * 
 * 4. DefaultConversionService
 *    - Extends GenericConversionService
 *    - Includes default converters
 * 
 * 5. FormattingConversionService
 *    - Extends ConversionService
 *    - Extends FormatterRegistry
 * 
 * 6. DefaultFormattingConversionService
 *    - Includes formatters + converters
 * 
 * Best Practices:
 * - Use DefaultFormattingConversionService in web apps
 * - Register custom converters in @Configuration
 * - Use ConversionService interface in dependencies
 * - Handle conversion failures with try-catch
 * - Test converters in isolation
 * 
 * Dependencies:
 * - spring-core
 * - spring-context
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class ConversionServicePattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(ConversionServicePattern.class, args);
        demonstrateConversionService(context);
    }
    
    /**
     * Demonstrates ConversionService usage
     */
    private static void demonstrateConversionService(org.springframework.context.ApplicationContext context) {
        System.out.println("=== Conversion Service Pattern Demonstrations ===\n");
        
        ConversionServiceDemo demo = context.getBean(ConversionServiceDemo.class);
        
        // Demo 1: Basic conversions
        demo.demonstrateBasicConversions();
        
        // Demo 2: Custom converter usage
        demo.demonstrateCustomConverters();
        
        // Demo 3: Converter factory usage
        demo.demonstrateConverterFactory();
        
        // Demo 4: Collection conversions
        demo.demonstrateCollectionConversions();
        
        // Demo 5: Conversion service types
        demo.demonstrateConversionServiceTypes();
    }
}

// ============================================================================
// Configuration
// ============================================================================

/**
 * Configures custom ConversionService with custom converters
 */
@Configuration
class ConversionServiceConfiguration implements WebMvcConfigurer {
    
    /**
     * Creates a custom FormattingConversionService
     */
    @Bean
    public FormattingConversionService conversionService() {
        DefaultFormattingConversionService service = new DefaultFormattingConversionService();
        
        // Register custom converters
        service.addConverter(new ProductToProductDtoConverter());
        service.addConverter(new StringToProductConverter());
        service.addConverter(new OrderToOrderSummaryConverter());
        
        // Register converter factory
        service.addConverterFactory(new StringToEnumConverterFactory());
        
        return service;
    }
    
    /**
     * Adds custom converters to MVC registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new ProductToProductDtoConverter());
        registry.addConverter(new StringToProductConverter());
        registry.addConverter(new OrderToOrderSummaryConverter());
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }
}

// ============================================================================
// Custom Converters
// ============================================================================

/**
 * Converts Product to ProductDto
 */
class ProductToProductDtoConverter implements Converter<Product, ProductDto> {
    
    @Override
    public ProductDto convert(Product source) {
        ProductDto dto = new ProductDto();
        dto.setId(source.getId());
        dto.setName(source.getName());
        dto.setPrice(source.getPrice());
        dto.setAvailable(source.getStock() > 0);
        return dto;
    }
}

/**
 * Converts String to Product
 * Format: "id:name:price:stock"
 */
class StringToProductConverter implements Converter<String, Product> {
    
    @Override
    public Product convert(String source) {
        String[] parts = source.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid product format");
        }
        
        Product product = new Product();
        product.setId(Long.parseLong(parts[0]));
        product.setName(parts[1]);
        product.setPrice(new BigDecimal(parts[2]));
        product.setStock(Integer.parseInt(parts[3]));
        return product;
    }
}

/**
 * Converts Order to OrderSummary
 */
class OrderToOrderSummaryConverter implements Converter<Order, OrderSummary> {
    
    @Override
    public OrderSummary convert(Order source) {
        OrderSummary summary = new OrderSummary();
        summary.setOrderId(source.getId());
        summary.setCustomerName(source.getCustomerName());
        summary.setItemCount(source.getItems().size());
        summary.setTotalAmount(calculateTotal(source));
        summary.setStatus(source.getStatus().name());
        return summary;
    }
    
    private BigDecimal calculateTotal(Order order) {
        return order.getItems().stream()
            .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

// ============================================================================
// Converter Factory
// ============================================================================

/**
 * Converter factory for String to Enum conversions
 */
class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {
    
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
        @SuppressWarnings("unchecked")
        public T convert(String source) {
            return (T) Enum.valueOf(enumType, source.trim().toUpperCase());
        }
    }
}

// ============================================================================
// Generic Converter Example
// ============================================================================

/**
 * Generic converter for collection type conversions
 */
class CollectionToListConverter implements GenericConverter {
    
    private final ConversionService conversionService;
    
    public CollectionToListConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Collection.class, List.class));
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        Collection<?> sourceCollection = (Collection<?>) source;
        List<Object> targetList = new ArrayList<>();
        
        TypeDescriptor elementType = targetType.getElementTypeDescriptor();
        if (elementType != null) {
            for (Object element : sourceCollection) {
                Object converted = conversionService.convert(element, elementType.getType());
                targetList.add(converted);
            }
        } else {
            targetList.addAll(sourceCollection);
        }
        
        return targetList;
    }
}

// ============================================================================
// Demo Service
// ============================================================================

/**
 * Service demonstrating ConversionService usage
 */
@Service
class ConversionServiceDemo {
    
    private final ConversionService conversionService;
    
    public ConversionServiceDemo(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    /**
     * Demonstrates basic type conversions
     */
    public void demonstrateBasicConversions() {
        System.out.println("1. Basic Conversions:");
        
        // String to Integer
        Integer intValue = conversionService.convert("12345", Integer.class);
        System.out.println("   String to Integer: " + intValue);
        
        // String to Boolean
        Boolean boolValue = conversionService.convert("true", Boolean.class);
        System.out.println("   String to Boolean: " + boolValue);
        
        // Number to String
        String strValue = conversionService.convert(42, String.class);
        System.out.println("   Integer to String: " + strValue);
        
        System.out.println();
    }
    
    /**
     * Demonstrates custom converter usage
     */
    public void demonstrateCustomConverters() {
        System.out.println("2. Custom Converters:");
        
        // String to Product
        String productString = "1:Laptop:999.99:10";
        Product product = conversionService.convert(productString, Product.class);
        System.out.println("   String to Product: " + product);
        
        // Product to ProductDto
        ProductDto dto = conversionService.convert(product, ProductDto.class);
        System.out.println("   Product to ProductDto: " + dto);
        
        System.out.println();
    }
    
    /**
     * Demonstrates converter factory usage
     */
    public void demonstrateConverterFactory() {
        System.out.println("3. Converter Factory:");
        
        // String to OrderStatus enum
        OrderStatus status = conversionService.convert("pending", OrderStatus.class);
        System.out.println("   String to OrderStatus: " + status);
        
        System.out.println();
    }
    
    /**
     * Demonstrates collection conversions
     */
    public void demonstrateCollectionConversions() {
        System.out.println("4. Collection Conversions:");
        
        List<String> stringList = Arrays.asList("1", "2", "3", "4", "5");
        System.out.println("   Original list: " + stringList);
        System.out.println("   (Demonstrating collection conversion support)");
        
        System.out.println();
    }
    
    /**
     * Demonstrates different ConversionService types
     */
    public void demonstrateConversionServiceTypes() {
        System.out.println("5. ConversionService Types:");
        
        // GenericConversionService
        GenericConversionService generic = new GenericConversionService();
        System.out.println("   GenericConversionService created");
        
        // DefaultConversionService
        DefaultConversionService defaultService = new DefaultConversionService();
        Integer value = defaultService.convert("123", Integer.class);
        System.out.println("   DefaultConversionService: " + value);
        
        // FormattingConversionService
        FormattingConversionService formatting = new DefaultFormattingConversionService();
        System.out.println("   FormattingConversionService created");
        
        System.out.println();
    }
    
    /**
     * Checks if conversion is supported
     */
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return conversionService.canConvert(sourceType, targetType);
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * Product entity
 */
class Product {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + ", stock=" + stock + '}';
    }
}

/**
 * Product DTO
 */
class ProductDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Boolean available;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    
    @Override
    public String toString() {
        return "ProductDto{id=" + id + ", name='" + name + "', price=" + price + ", available=" + available + '}';
    }
}

/**
 * Order entity
 */
class Order {
    private Long id;
    private String customerName;
    private List<OrderItem> items;
    private OrderStatus status;
    private LocalDateTime createdAt;
    
    public Order() {
        this.items = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

/**
 * Order item
 */
class OrderItem {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    
    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}

/**
 * Order summary DTO
 */
class OrderSummary {
    private Long orderId;
    private String customerName;
    private Integer itemCount;
    private BigDecimal totalAmount;
    private String status;
    
    // Getters and setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "OrderSummary{orderId=" + orderId + ", customer='" + customerName + 
               "', items=" + itemCount + ", total=" + totalAmount + ", status=" + status + '}';
    }
}

/**
 * Order status enum
 */
enum OrderStatus {
    PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating ConversionService in MVC
 */
@RestController
@RequestMapping("/api/conversion-service")
class ConversionServiceController {
    
    private final ConversionServiceDemo demo;
    private final ConversionService conversionService;
    
    public ConversionServiceController(ConversionServiceDemo demo, ConversionService conversionService) {
        this.demo = demo;
        this.conversionService = conversionService;
    }
    
    /**
     * Convert string to product
     */
    @GetMapping("/parse-product")
    public ResponseEntity<Product> parseProduct(@RequestParam String data) {
        Product product = conversionService.convert(data, Product.class);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Convert product to DTO
     */
    @PostMapping("/to-dto")
    public ResponseEntity<ProductDto> toDto(@RequestBody Product product) {
        ProductDto dto = conversionService.convert(product, ProductDto.class);
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Check if conversion is supported
     */
    @GetMapping("/can-convert")
    public ResponseEntity<Map<String, Boolean>> canConvert(
            @RequestParam String source,
            @RequestParam String target) {
        
        try {
            Class<?> sourceClass = Class.forName(source);
            Class<?> targetClass = Class.forName(target);
            
            boolean canConvert = demo.canConvert(sourceClass, targetClass);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("canConvert", canConvert);
            return ResponseEntity.ok(response);
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Batch conversion demo
     */
    @PostMapping("/batch-convert")
    public ResponseEntity<List<ProductDto>> batchConvert(@RequestBody List<Product> products) {
        List<ProductDto> dtos = products.stream()
            .map(p -> conversionService.convert(p, ProductDto.class))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
}
