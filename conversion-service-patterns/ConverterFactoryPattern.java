package com.example.conversion;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;

/**
 * Converter Factory Pattern
 * ==========================
 * 
 * Demonstrates the use of ConverterFactory for converting from a single source type
 * to a range of target types that share a common interface or base class.
 * 
 * Key Concepts:
 * ------------
 * 1. ConverterFactory<S, R> - Factory for creating converters
 * 2. Single Source Type - One input type
 * 3. Range of Target Types - Multiple output types with common base
 * 4. Lazy Converter Creation - Converters created on demand
 * 5. Type Safety - Generic type parameters ensure type safety
 * 6. Common Use: Enum conversions, Number conversions
 * 
 * ConverterFactory Interface:
 * --------------------------
 * public interface ConverterFactory<S, R> {
 *     <T extends R> Converter<S, T> getConverter(Class<T> targetType);
 * }
 * 
 * When to Use:
 * -----------
 * - Convert from one type to multiple related types
 * - Target types share common interface/base class
 * - Enum conversions (String to any Enum)
 * - Number conversions (String to any Number)
 * - Type hierarchy conversions
 * 
 * Advantages:
 * ----------
 * - Single factory for multiple target types
 * - Type-safe converter creation
 * - Efficient (converters created on demand)
 * - Clean separation of concerns
 * 
 * Common Use Cases:
 * ----------------
 * - String to Enum (any enum type)
 * - String to Number (Integer, Long, Double, etc.)
 * - Object to DTO (various DTO types)
 * - Primitive to Wrapper conversions
 * 
 * Best Practices:
 * --------------
 * - Check target type compatibility
 * - Handle edge cases (null, invalid values)
 * - Use generic bounds effectively
 * - Document supported target types
 * - Throw clear exceptions for unsupported types
 * - Test with all target types
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: String to Enum Converter Factory
 * Convert String to any Enum type
 */
@Component
public class ConverterFactoryPattern implements ConverterFactory<String, Enum> {
    
    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }
    
    /**
     * Inner converter for String to specific Enum type
     */
    private static class StringToEnumConverter<T extends Enum> implements Converter<String, T> {
        
        private final Class<T> enumType;
        
        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public T convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            
            String value = source.trim();
            
            // Try exact match
            try {
                return (T) Enum.valueOf((Class) enumType, value);
            } catch (IllegalArgumentException e) {
                // Try case-insensitive match
                for (T enumConstant : enumType.getEnumConstants()) {
                    if (enumConstant.name().equalsIgnoreCase(value)) {
                        return enumConstant;
                    }
                }
                throw new IllegalArgumentException(
                    "No enum constant " + enumType.getName() + "." + value);
            }
        }
    }
}

/**
 * Example 2: String to Number Converter Factory
 * Convert String to any Number type (Integer, Long, Double, etc.)
 */
@Component
class StringToNumberConverterFactory implements ConverterFactory<String, Number> {
    
    @Override
    public <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToNumberConverter<>(targetType);
    }
    
    private static class StringToNumberConverter<T extends Number> implements Converter<String, T> {
        
        private final Class<T> targetType;
        
        public StringToNumberConverter(Class<T> targetType) {
            this.targetType = targetType;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public T convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            
            String trimmed = source.trim();
            
            try {
                if (targetType == Integer.class) {
                    return (T) Integer.valueOf(trimmed);
                } else if (targetType == Long.class) {
                    return (T) Long.valueOf(trimmed);
                } else if (targetType == Double.class) {
                    return (T) Double.valueOf(trimmed);
                } else if (targetType == Float.class) {
                    return (T) Float.valueOf(trimmed);
                } else if (targetType == Short.class) {
                    return (T) Short.valueOf(trimmed);
                } else if (targetType == Byte.class) {
                    return (T) Byte.valueOf(trimmed);
                } else {
                    throw new IllegalArgumentException(
                        "Unsupported number type: " + targetType.getName());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Cannot convert '" + source + "' to " + targetType.getSimpleName(), e);
            }
        }
    }
}

/**
 * Example 3: Integer to Enum Converter Factory
 * Convert Integer (ordinal) to any Enum type
 */
@Component
class IntegerToEnumConverterFactory implements ConverterFactory<Integer, Enum> {
    
    @Override
    public <T extends Enum> Converter<Integer, T> getConverter(Class<T> targetType) {
        return new IntegerToEnumConverter<>(targetType);
    }
    
    private static class IntegerToEnumConverter<T extends Enum> implements Converter<Integer, T> {
        
        private final Class<T> enumType;
        
        public IntegerToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }
        
        @Override
        public T convert(Integer source) {
            if (source == null) {
                return null;
            }
            
            T[] constants = enumType.getEnumConstants();
            
            if (source < 0 || source >= constants.length) {
                throw new IllegalArgumentException(
                    "Invalid ordinal " + source + " for enum " + enumType.getName());
            }
            
            return constants[source];
        }
    }
}

/**
 * Example 4: Object to DTO Converter Factory
 * Convert domain objects to various DTO types
 */
interface BaseDTO {
    // Marker interface for all DTOs
}

class UserDTO implements BaseDTO {
    private Long id;
    private String name;
    
    public UserDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
}

class ProductDTO implements BaseDTO {
    private Long id;
    private String name;
    private Double price;
    
    public ProductDTO(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
}

@Component
class EntityToDTOConverterFactory implements ConverterFactory<Object, BaseDTO> {
    
    @Override
    public <T extends BaseDTO> Converter<Object, T> getConverter(Class<T> targetType) {
        if (targetType == UserDTO.class) {
            return (Converter<Object, T>) new UserToUserDTOConverter();
        } else if (targetType == ProductDTO.class) {
            return (Converter<Object, T>) new ProductToProductDTOConverter();
        }
        
        throw new IllegalArgumentException("Unsupported DTO type: " + targetType.getName());
    }
    
    private static class UserToUserDTOConverter implements Converter<Object, UserDTO> {
        @Override
        public UserDTO convert(Object source) {
            if (source == null) return null;
            // Assuming source is a User entity
            // return new UserDTO(user.getId(), user.getName());
            return new UserDTO(1L, "John Doe");
        }
    }
    
    private static class ProductToProductDTOConverter implements Converter<Object, ProductDTO> {
        @Override
        public ProductDTO convert(Object source) {
            if (source == null) return null;
            // Assuming source is a Product entity
            // return new ProductDTO(product.getId(), product.getName(), product.getPrice());
            return new ProductDTO(1L, "Product", 99.99);
        }
    }
}

/**
 * Example 5: Collection Element Converter Factory
 * Convert collections with different element types
 */
interface CollectionWrapper<T> {
    java.util.Collection<T> getElements();
}

class StringCollectionWrapper implements CollectionWrapper<String> {
    private final java.util.List<String> elements;
    
    public StringCollectionWrapper(java.util.List<String> elements) {
        this.elements = elements;
    }
    
    @Override
    public java.util.Collection<String> getElements() {
        return elements;
    }
}

class IntegerCollectionWrapper implements CollectionWrapper<Integer> {
    private final java.util.List<Integer> elements;
    
    public IntegerCollectionWrapper(java.util.List<Integer> elements) {
        this.elements = elements;
    }
    
    @Override
    public java.util.Collection<Integer> getElements() {
        return elements;
    }
}

/**
 * Example 6: Formatter-based Converter Factory
 * Create converters that use formatters
 */
@Component
class FormatterBasedConverterFactory implements ConverterFactory<String, Number> {
    
    @Override
    public <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {
        return new FormattedNumberConverter<>(targetType);
    }
    
    private static class FormattedNumberConverter<T extends Number> implements Converter<String, T> {
        
        private final Class<T> targetType;
        
        public FormattedNumberConverter(Class<T> targetType) {
            this.targetType = targetType;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public T convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            
            // Remove common formatting characters
            String cleaned = source.trim()
                .replace(",", "")
                .replace("$", "")
                .replace("%", "");
            
            try {
                if (targetType == Integer.class) {
                    return (T) Integer.valueOf(cleaned);
                } else if (targetType == Double.class) {
                    return (T) Double.valueOf(cleaned);
                } else if (targetType == Long.class) {
                    return (T) Long.valueOf(cleaned);
                }
                
                throw new IllegalArgumentException(
                    "Unsupported number type: " + targetType.getName());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Cannot parse '" + source + "' as " + targetType.getSimpleName(), e);
            }
        }
    }
}

/**
 * Usage Examples
 */
class ConverterFactoryUsageExamples {
    
    public static void main(String[] args) {
        DefaultConversionService conversionService = new DefaultConversionService();
        
        // Register converter factories
        conversionService.addConverterFactory(new ConverterFactoryPattern());
        conversionService.addConverterFactory(new StringToNumberConverterFactory());
        conversionService.addConverterFactory(new IntegerToEnumConverterFactory());
        conversionService.addConverterFactory(new FormatterBasedConverterFactory());
        
        // Example 1: String to Enum
        Status status = conversionService.convert("ACTIVE", Status.class);
        System.out.println("Status: " + status);
        
        Priority priority = conversionService.convert("high", Priority.class);
        System.out.println("Priority: " + priority);
        
        // Example 2: String to Number
        Integer intValue = conversionService.convert("123", Integer.class);
        System.out.println("Integer: " + intValue);
        
        Double doubleValue = conversionService.convert("123.45", Double.class);
        System.out.println("Double: " + doubleValue);
        
        Long longValue = conversionService.convert("9876543210", Long.class);
        System.out.println("Long: " + longValue);
        
        // Example 3: Integer to Enum
        Status statusByOrdinal = conversionService.convert(0, Status.class);
        System.out.println("Status by ordinal: " + statusByOrdinal);
        
        // Example 4: Formatted numbers
        Integer formatted = conversionService.convert("$1,234", Integer.class);
        System.out.println("Formatted: " + formatted);
        
        Double percentage = conversionService.convert("98.5%", Double.class);
        System.out.println("Percentage: " + percentage);
    }
}

enum Status {
    ACTIVE, INACTIVE, PENDING
}

enum Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Usage Summary:
 * =============
 * 
 * 1. Implement ConverterFactory:
 *    public class MyFactory implements ConverterFactory<String, Enum> {
 *        @Override
 *        public <T extends Enum> Converter<String, T> getConverter(Class<T> type) {
 *            return new MyConverter<>(type);
 *        }
 *    }
 * 
 * 2. Create Inner Converter:
 *    private static class MyConverter<T extends Enum> implements Converter<String, T> {
 *        @Override
 *        public T convert(String source) {
 *            // Conversion logic
 *        }
 *    }
 * 
 * 3. Register Factory:
 *    conversionService.addConverterFactory(new MyFactory());
 * 
 * 4. Use Conversion:
 *    MyEnum value = conversionService.convert("VALUE", MyEnum.class);
 * 
 * Comparison:
 * ==========
 * 
 * Converter<S,T>:
 * - One source, one target
 * - Simple cases
 * 
 * ConverterFactory<S,R>:
 * - One source, multiple targets (with common base)
 * - Enum/Number conversions
 * - Type hierarchies
 * 
 * GenericConverter:
 * - Multiple sources, multiple targets
 * - Complex metadata
 * - Most flexible
 */
