package com.example.conversion;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generic Converter Pattern
 * ==========================
 * 
 * Demonstrates the use of GenericConverter for complex type conversions in Spring.
 * GenericConverter is the most flexible converter interface, allowing conversion
 * between multiple source/target type pairs with access to field context.
 * 
 * Key Concepts:
 * ------------
 * 1. GenericConverter - Most flexible converter interface
 * 2. ConvertiblePair - Defines source and target type pair
 * 3. TypeDescriptor - Provides type metadata (generics, annotations)
 * 4. Multiple Type Pairs - Single converter for multiple conversions
 * 5. Field Context - Access to field annotations and metadata
 * 6. Null Handling - Explicit null value handling
 * 7. Collection Conversion - Convert between collection types
 * 
 * GenericConverter Interface:
 * --------------------------
 * public interface GenericConverter {
 *     Set<ConvertiblePair> getConvertibleTypes();
 *     Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);
 * }
 * 
 * When to Use GenericConverter:
 * ----------------------------
 * - Convert between multiple type pairs with single converter
 * - Need access to field context (annotations, generics)
 * - Complex conversion logic based on metadata
 * - Collection element conversion
 * - Conditional conversion based on type information
 * - Custom null handling
 * 
 * Advantages over Converter<S,T>:
 * -------------------------------
 * - Support multiple source/target pairs
 * - Access to TypeDescriptor (generics, annotations)
 * - More flexible and powerful
 * - Can inspect field metadata
 * 
 * When NOT to Use:
 * ---------------
 * - Simple one-to-one conversions (use Converter<S,T>)
 * - No need for metadata access
 * - Performance-critical simple conversions
 * 
 * Best Practices:
 * --------------
 * - Return specific convertible pairs (not null)
 * - Handle null values explicitly
 * - Use TypeDescriptor for metadata
 * - Keep conversion logic focused
 * - Register with ConversionService
 * - Test with various type scenarios
 * - Document supported type pairs
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic GenericConverter
 * Convert String to various date/time types
 */
@Component
public class GenericConverterPattern implements GenericConverter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new HashSet<>();
        pairs.add(new ConvertiblePair(String.class, LocalDate.class));
        pairs.add(new ConvertiblePair(String.class, LocalDateTime.class));
        pairs.add(new ConvertiblePair(String.class, Date.class));
        return pairs;
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        String sourceString = (String) source;
        Class<?> targetClass = targetType.getType();
        
        if (targetClass == LocalDate.class) {
            return LocalDate.parse(sourceString, DATE_FORMATTER);
        } else if (targetClass == LocalDateTime.class) {
            return LocalDateTime.parse(sourceString, DATETIME_FORMATTER);
        } else if (targetClass == Date.class) {
            LocalDateTime ldt = LocalDateTime.parse(sourceString, DATETIME_FORMATTER);
            return java.sql.Timestamp.valueOf(ldt);
        }
        
        throw new IllegalArgumentException("Unsupported target type: " + targetClass);
    }
}

/**
 * Example 2: Collection Element Converter
 * Convert between different collection types with element transformation
 */
@Component
class CollectionElementConverter implements GenericConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new HashSet<>();
        // Convert List<String> to Set<Integer>
        pairs.add(new ConvertiblePair(List.class, Set.class));
        // Convert Set to List
        pairs.add(new ConvertiblePair(Set.class, List.class));
        return pairs;
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        Collection<?> sourceCollection = (Collection<?>) source;
        Class<?> targetCollectionType = targetType.getType();
        TypeDescriptor elementType = targetType.getElementTypeDescriptor();
        
        Collection<Object> targetCollection;
        if (targetCollectionType == Set.class || Set.class.isAssignableFrom(targetCollectionType)) {
            targetCollection = new HashSet<>();
        } else {
            targetCollection = new ArrayList<>();
        }
        
        // Convert each element
        for (Object element : sourceCollection) {
            if (elementType != null && elementType.getType() == Integer.class && element instanceof String) {
                targetCollection.add(Integer.parseInt((String) element));
            } else if (elementType != null && elementType.getType() == String.class && element instanceof Integer) {
                targetCollection.add(element.toString());
            } else {
                targetCollection.add(element);
            }
        }
        
        return targetCollection;
    }
}

/**
 * Example 3: Annotation-Aware Converter
 * Convert based on field annotations
 */
@Component
class AnnotationAwareConverter implements GenericConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, String.class));
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        String value = (String) source;
        
        // Check for custom annotations on target field
        if (targetType.hasAnnotation(Uppercase.class)) {
            return value.toUpperCase();
        } else if (targetType.hasAnnotation(Lowercase.class)) {
            return value.toLowerCase();
        } else if (targetType.hasAnnotation(Trim.class)) {
            return value.trim();
        }
        
        return value;
    }
}

// Custom annotations for demonstration
@interface Uppercase {}
@interface Lowercase {}
@interface Trim {}

/**
 * Example 4: Map Converter
 * Convert between different map types with key/value transformation
 */
@Component
class MapConverter implements GenericConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new HashSet<>();
        pairs.add(new ConvertiblePair(Map.class, Map.class));
        return pairs;
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        Map<?, ?> sourceMap = (Map<?, ?>) source;
        Map<Object, Object> targetMap = new HashMap<>();
        
        TypeDescriptor keyType = targetType.getMapKeyTypeDescriptor();
        TypeDescriptor valueType = targetType.getMapValueTypeDescriptor();
        
        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            Object key = convertElement(entry.getKey(), keyType);
            Object value = convertElement(entry.getValue(), valueType);
            targetMap.put(key, value);
        }
        
        return targetMap;
    }
    
    private Object convertElement(Object element, TypeDescriptor targetType) {
        if (element == null || targetType == null) {
            return element;
        }
        
        // Simple conversion examples
        if (targetType.getType() == String.class && !(element instanceof String)) {
            return element.toString();
        } else if (targetType.getType() == Integer.class && element instanceof String) {
            return Integer.parseInt((String) element);
        }
        
        return element;
    }
}

/**
 * Example 5: Enum Converter with Case Insensitivity
 * Convert String to Enum with flexible matching
 */
@Component
class FlexibleEnumConverter implements GenericConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Enum.class));
    }
    
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        String value = ((String) source).trim();
        Class<? extends Enum> enumType = (Class<? extends Enum>) targetType.getType();
        
        // Try exact match first
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            // Try case-insensitive match
            for (Enum enumConstant : enumType.getEnumConstants()) {
                if (enumConstant.name().equalsIgnoreCase(value)) {
                    return enumConstant;
                }
            }
        }
        
        throw new IllegalArgumentException("No enum constant " + enumType.getName() + "." + value);
    }
}

/**
 * Example 6: Object to Properties Converter
 * Convert object to Properties format
 */
@Component
class ObjectToPropertiesConverter implements GenericConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Object.class, Properties.class));
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        Properties properties = new Properties();
        
        // Convert Map to Properties
        if (source instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) source;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                properties.setProperty(
                    String.valueOf(entry.getKey()),
                    String.valueOf(entry.getValue())
                );
            }
        }
        
        return properties;
    }
}

/**
 * Example 7: Array to List Converter
 * Convert arrays to lists with element transformation
 */
@Component
class ArrayToListConverter implements GenericConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new HashSet<>();
        pairs.add(new ConvertiblePair(Object[].class, List.class));
        pairs.add(new ConvertiblePair(int[].class, List.class));
        pairs.add(new ConvertiblePair(long[].class, List.class));
        return pairs;
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        List<Object> targetList = new ArrayList<>();
        
        if (source instanceof Object[]) {
            Collections.addAll(targetList, (Object[]) source);
        } else if (source instanceof int[]) {
            for (int value : (int[]) source) {
                targetList.add(value);
            }
        } else if (source instanceof long[]) {
            for (long value : (long[]) source) {
                targetList.add(value);
            }
        }
        
        return targetList;
    }
}

/**
 * Example 8: Conditional Converter
 * Convert based on source value characteristics
 */
@Component
class ConditionalStringConverter implements GenericConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new HashSet<>();
        pairs.add(new ConvertiblePair(String.class, Integer.class));
        pairs.add(new ConvertiblePair(String.class, Boolean.class));
        pairs.add(new ConvertiblePair(String.class, Double.class));
        return pairs;
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        String value = (String) source;
        Class<?> targetClass = targetType.getType();
        
        if (targetClass == Integer.class) {
            return parseInteger(value);
        } else if (targetClass == Boolean.class) {
            return parseBoolean(value);
        } else if (targetClass == Double.class) {
            return parseDouble(value);
        }
        
        return null;
    }
    
    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private Boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || 
               "yes".equalsIgnoreCase(value) || 
               "1".equals(value);
    }
    
    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

/**
 * Usage Examples and Testing
 */
class GenericConverterUsageExamples {
    
    public static void main(String[] args) {
        DefaultConversionService conversionService = new DefaultConversionService();
        
        // Register converters
        conversionService.addConverter(new GenericConverterPattern());
        conversionService.addConverter(new CollectionElementConverter());
        conversionService.addConverter(new FlexibleEnumConverter());
        
        // Example 1: String to LocalDate
        LocalDate date = conversionService.convert("2024-01-15", LocalDate.class);
        System.out.println("Converted date: " + date);
        
        // Example 2: String to LocalDateTime
        LocalDateTime dateTime = conversionService.convert(
            "2024-01-15 14:30:00", 
            LocalDateTime.class
        );
        System.out.println("Converted datetime: " + dateTime);
        
        // Example 3: List<String> to Set<Integer>
        List<String> stringList = Arrays.asList("1", "2", "3", "4", "5");
        TypeDescriptor sourceDesc = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class));
        TypeDescriptor targetDesc = TypeDescriptor.collection(Set.class, TypeDescriptor.valueOf(Integer.class));
        
        @SuppressWarnings("unchecked")
        Set<Integer> intSet = (Set<Integer>) conversionService.convert(stringList, sourceDesc, targetDesc);
        System.out.println("Converted set: " + intSet);
        
        // Example 4: String to Enum (case insensitive)
        // Status status = conversionService.convert("active", Status.class);
        
        // Example 5: Null handling
        LocalDate nullDate = conversionService.convert(null, LocalDate.class);
        System.out.println("Null converted: " + nullDate);
    }
}

/**
 * Usage Summary:
 * =============
 * 
 * 1. Define Convertible Pairs:
 *    @Override
 *    public Set<ConvertiblePair> getConvertibleTypes() {
 *        Set<ConvertiblePair> pairs = new HashSet<>();
 *        pairs.add(new ConvertiblePair(String.class, LocalDate.class));
 *        return pairs;
 *    }
 * 
 * 2. Implement Conversion Logic:
 *    @Override
 *    public Object convert(Object source, TypeDescriptor sourceType, 
 *                         TypeDescriptor targetType) {
 *        if (source == null) return null;
 *        // Conversion logic
 *    }
 * 
 * 3. Register Converter:
 *    conversionService.addConverter(new MyGenericConverter());
 * 
 * 4. Use TypeDescriptor:
 *    TypeDescriptor descriptor = TypeDescriptor.valueOf(MyClass.class);
 *    descriptor.getAnnotations();
 *    descriptor.getElementTypeDescriptor();
 * 
 * Comparison with Other Converters:
 * =================================
 * 
 * Converter<S,T>:
 * - Simple one-to-one conversion
 * - No metadata access
 * - Best for simple cases
 * 
 * ConverterFactory<S, R>:
 * - One source, multiple target types
 * - Target must share common interface
 * - Good for enum conversions
 * 
 * GenericConverter:
 * - Multiple source/target pairs
 * - Full metadata access
 * - Most flexible
 * - Complex conversions
 */

enum Status {
    ACTIVE, INACTIVE, PENDING
}
