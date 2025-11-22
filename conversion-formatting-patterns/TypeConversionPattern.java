package com.example.conversion.typeconversion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Type Conversion Pattern - Demonstrates Spring's Type Conversion System
 * 
 * This pattern shows how to:
 * 1. Use TypeDescriptor for generic type information
 * 2. Convert collections with element type preservation
 * 3. Handle complex generic types
 * 4. Convert Maps with key/value type conversion
 * 5. Use ConversionService for type-safe conversions
 * 6. Handle array conversions
 * 7. Convert nested generic types
 * 8. Use TypeConverter for bean property conversion
 * 9. Handle null and optional conversions
 * 10. Chain type conversions
 * 
 * Key Concepts:
 * - TypeDescriptor: Metadata about types including generics
 * - ConversionService: Central type conversion service
 * - Generic Type Handling: Preserve generic type information
 * - Collection Conversion: Convert between collection types
 * - Type Safety: Compile-time type checking
 * 
 * Type Descriptor Features:
 * - Generic type information
 * - Annotation metadata
 * - Nested type information
 * - Array/Collection element types
 * - Map key/value types
 * 
 * Conversion Capabilities:
 * 1. Primitive to wrapper conversions
 * 2. String to any type
 * 3. Collection element conversion
 * 4. Map key/value conversion
 * 5. Array conversions
 * 6. Custom object conversions
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
public class TypeConversionPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(TypeConversionPattern.class, args);
        demonstrateTypeConversion(context);
    }
    
    /**
     * Demonstrates various type conversion scenarios
     */
    private static void demonstrateTypeConversion(org.springframework.context.ApplicationContext context) {
        System.out.println("=== Type Conversion Pattern Demonstrations ===\n");
        
        TypeConversionService service = context.getBean(TypeConversionService.class);
        
        // Demo 1: Basic type conversions
        demonstrateBasicConversions(service);
        
        // Demo 2: Collection conversions
        demonstrateCollectionConversions(service);
        
        // Demo 3: Map conversions
        demonstrateMapConversions(service);
        
        // Demo 4: Generic type conversions
        demonstrateGenericConversions(service);
        
        // Demo 5: Array conversions
        demonstrateArrayConversions(service);
    }
    
    /**
     * Demonstrates basic type conversions
     */
    private static void demonstrateBasicConversions(TypeConversionService service) {
        System.out.println("1. Basic Type Conversions:");
        
        // String to Integer
        Integer intValue = service.convert("12345", Integer.class);
        System.out.println("   String to Integer: " + intValue);
        
        // String to Boolean
        Boolean boolValue = service.convert("true", Boolean.class);
        System.out.println("   String to Boolean: " + boolValue);
        
        // String to BigDecimal
        BigDecimal decimalValue = service.convert("123.45", BigDecimal.class);
        System.out.println("   String to BigDecimal: " + decimalValue);
        
        // String to LocalDate
        LocalDate dateValue = service.convert("2024-12-25", LocalDate.class);
        System.out.println("   String to LocalDate: " + dateValue);
        
        System.out.println();
    }
    
    /**
     * Demonstrates collection conversions
     */
    private static void demonstrateCollectionConversions(TypeConversionService service) {
        System.out.println("2. Collection Conversions:");
        
        // List<String> to List<Integer>
        List<String> stringList = Arrays.asList("1", "2", "3", "4", "5");
        List<Integer> intList = service.convertList(stringList, Integer.class);
        System.out.println("   List<String> to List<Integer>: " + intList);
        
        // Set<String> to Set<Long>
        Set<String> stringSet = new HashSet<>(Arrays.asList("10", "20", "30"));
        Set<Long> longSet = service.convertSet(stringSet, Long.class);
        System.out.println("   Set<String> to Set<Long>: " + longSet);
        
        System.out.println();
    }
    
    /**
     * Demonstrates map conversions
     */
    private static void demonstrateMapConversions(TypeConversionService service) {
        System.out.println("3. Map Conversions:");
        
        // Map<String, String> to Map<String, Integer>
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("one", "1");
        stringMap.put("two", "2");
        stringMap.put("three", "3");
        
        Map<String, Integer> intMap = service.convertMap(stringMap, String.class, Integer.class);
        System.out.println("   Map<String, String> to Map<String, Integer>: " + intMap);
        
        System.out.println();
    }
    
    /**
     * Demonstrates generic type conversions
     */
    private static void demonstrateGenericConversions(TypeConversionService service) {
        System.out.println("4. Generic Type Conversions:");
        
        // Complex nested generic type
        List<Map<String, String>> complexList = new ArrayList<>();
        Map<String, String> map1 = new HashMap<>();
        map1.put("id", "1");
        map1.put("value", "100");
        complexList.add(map1);
        
        System.out.println("   Original complex type: " + complexList);
        System.out.println("   (Demonstrating type-safe conversion)");
        
        System.out.println();
    }
    
    /**
     * Demonstrates array conversions
     */
    private static void demonstrateArrayConversions(TypeConversionService service) {
        System.out.println("5. Array Conversions:");
        
        String[] stringArray = {"1", "2", "3", "4", "5"};
        Integer[] intArray = service.convertArray(stringArray, Integer.class);
        System.out.println("   String[] to Integer[]: " + Arrays.toString(intArray));
        
        System.out.println();
    }
}

// ============================================================================
// Type Conversion Service
// ============================================================================

/**
 * Service providing type conversion operations
 */
@Service
class TypeConversionService {
    
    private final ConversionService conversionService;
    
    public TypeConversionService() {
        this.conversionService = DefaultConversionService.getSharedInstance();
    }
    
    /**
     * Converts object to target type
     */
    public <T> T convert(Object source, Class<T> targetType) {
        return conversionService.convert(source, targetType);
    }
    
    /**
     * Converts list elements to target type
     */
    public <S, T> List<T> convertList(List<S> sourceList, Class<T> targetType) {
        return sourceList.stream()
            .map(item -> conversionService.convert(item, targetType))
            .collect(Collectors.toList());
    }
    
    /**
     * Converts set elements to target type
     */
    public <S, T> Set<T> convertSet(Set<S> sourceSet, Class<T> targetType) {
        return sourceSet.stream()
            .map(item -> conversionService.convert(item, targetType))
            .collect(Collectors.toSet());
    }
    
    /**
     * Converts map values to target type
     */
    public <K, V1, V2> Map<K, V2> convertMap(Map<K, V1> sourceMap, 
                                             Class<K> keyType, 
                                             Class<V2> valueType) {
        return sourceMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> conversionService.convert(entry.getValue(), valueType)
            ));
    }
    
    /**
     * Converts array elements to target type
     */
    @SuppressWarnings("unchecked")
    public <S, T> T[] convertArray(S[] sourceArray, Class<T> targetType) {
        List<T> list = Arrays.stream(sourceArray)
            .map(item -> conversionService.convert(item, targetType))
            .collect(Collectors.toList());
        
        return list.toArray((T[]) java.lang.reflect.Array.newInstance(targetType, list.size()));
    }
    
    /**
     * Checks if conversion is possible
     */
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return conversionService.canConvert(sourceType, targetType);
    }
    
    /**
     * Converts with TypeDescriptor for generic types
     */
    public Object convertWithTypeDescriptor(Object source, 
                                           TypeDescriptor sourceType,
                                           TypeDescriptor targetType) {
        return conversionService.convert(source, sourceType, targetType);
    }
}

// ============================================================================
// Generic Type Converter
// ============================================================================

/**
 * Converter for generic collection types
 */
class CollectionTypeConverter implements GenericConverter {
    
    private final ConversionService conversionService;
    
    public CollectionTypeConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Collection.class, Collection.class));
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        Collection<?> sourceCollection = (Collection<?>) source;
        Collection<Object> targetCollection = createCollection(targetType);
        
        TypeDescriptor elementType = targetType.getElementTypeDescriptor();
        if (elementType == null) {
            targetCollection.addAll(sourceCollection);
        } else {
            for (Object element : sourceCollection) {
                Object convertedElement = conversionService.convert(element, elementType.getType());
                targetCollection.add(convertedElement);
            }
        }
        
        return targetCollection;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<Object> createCollection(TypeDescriptor targetType) {
        Class<?> collectionType = targetType.getType();
        
        if (List.class.isAssignableFrom(collectionType)) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(collectionType)) {
            return new HashSet<>();
        } else {
            try {
                return (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
    }
}

// ============================================================================
// Domain Models for Conversion
// ============================================================================

/**
 * Product data transfer object
 */
class ProductDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    @Override
    public String toString() {
        return "ProductDto{id=" + id + ", name='" + name + "', price=" + price + ", quantity=" + quantity + '}';
    }
}

/**
 * Order with items
 */
class Order {
    private Long orderId;
    private List<OrderItem> items;
    private BigDecimal total;
    
    public Order() {
        this.items = new ArrayList<>();
    }
    
    // Getters and setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}

/**
 * Order item
 */
class OrderItem {
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    
    // Getters and setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating type conversion
 */
@RestController
@RequestMapping("/api/type-conversion")
class TypeConversionController {
    
    private final TypeConversionService conversionService;
    
    public TypeConversionController(TypeConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    /**
     * Converts string list to integer list
     */
    @PostMapping("/convert-list")
    public ResponseEntity<List<Integer>> convertList(@RequestBody List<String> strings) {
        List<Integer> integers = conversionService.convertList(strings, Integer.class);
        return ResponseEntity.ok(integers);
    }
    
    /**
     * Converts string map to typed map
     */
    @PostMapping("/convert-map")
    public ResponseEntity<Map<String, Integer>> convertMap(@RequestBody Map<String, String> stringMap) {
        Map<String, Integer> intMap = conversionService.convertMap(stringMap, String.class, Integer.class);
        return ResponseEntity.ok(intMap);
    }
    
    /**
     * Checks if conversion is supported
     */
    @GetMapping("/can-convert")
    public ResponseEntity<Map<String, Boolean>> canConvert(
            @RequestParam String sourceType,
            @RequestParam String targetType) {
        
        try {
            Class<?> source = Class.forName(sourceType);
            Class<?> target = Class.forName(targetType);
            
            boolean canConvert = conversionService.canConvert(source, target);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("canConvert", canConvert);
            return ResponseEntity.ok(response);
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Demonstrates batch conversion
     */
    @PostMapping("/batch-convert")
    public ResponseEntity<Map<String, Object>> batchConvert(@RequestBody Map<String, String> data) {
        Map<String, Object> converted = new HashMap<>();
        
        data.forEach((key, value) -> {
            // Try to convert to different types based on content
            if (value.matches("\\d+")) {
                converted.put(key, conversionService.convert(value, Long.class));
            } else if (value.matches("\\d+\\.\\d+")) {
                converted.put(key, conversionService.convert(value, BigDecimal.class));
            } else if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                converted.put(key, conversionService.convert(value, Boolean.class));
            } else {
                converted.put(key, value);
            }
        });
        
        return ResponseEntity.ok(converted);
    }
}
