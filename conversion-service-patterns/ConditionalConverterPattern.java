package com.example.conversion;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

/**
 * Conditional Converter Pattern
 * ==============================
 * 
 * Demonstrates the use of ConditionalConverter for converters that should only
 * apply under certain conditions. This pattern allows fine-grained control over
 * when a converter is used based on source/target types and annotations.
 * 
 * Key Concepts:
 * ------------
 * 1. ConditionalConverter - Converter with conditional logic
 * 2. matches() Method - Determines if converter should apply
 * 3. TypeDescriptor - Provides type metadata for decision
 * 4. Annotation-based Conditions - Check for specific annotations
 * 5. Type-based Conditions - Check source/target type characteristics
 * 6. Priority Control - Control converter selection
 * 7. Fallback Behavior - Allow other converters to handle conversion
 * 
 * ConditionalConverter Interface:
 * ------------------------------
 * public interface ConditionalConverter {
 *     boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType);
 * }
 * 
 * Combined with GenericConverter:
 * ------------------------------
 * public interface ConditionalGenericConverter 
 *     extends GenericConverter, ConditionalConverter {
 * }
 * 
 * When to Use:
 * -----------
 * - Converter should apply only in specific contexts
 * - Multiple converters for same type pair
 * - Annotation-driven conversion
 * - Format-specific conversions
 * - Optional conversions
 * - Performance optimizations
 * 
 * Common Conditions:
 * -----------------
 * - Presence of annotations (@DateFormat, @NumberFormat)
 * - Source value characteristics (null, empty, format)
 * - Target type characteristics (generic parameters)
 * - Configuration/environment settings
 * - Collection element types
 * 
 * Best Practices:
 * --------------
 * - Make matches() fast (no expensive operations)
 * - Return false if conversion not applicable
 * - Document matching conditions clearly
 * - Test with various scenarios
 * - Consider converter ordering
 * - Avoid circular dependencies
 * - Handle edge cases in matches()
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Annotation-Based Conditional Converter
 * Only convert if target field has specific annotation
 */
@Component
public class ConditionalConverterPattern implements GenericConverter, ConditionalConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, LocalDate.class));
    }
    
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        // Only apply if target has @CustomDateFormat annotation
        return targetType.hasAnnotation(CustomDateFormat.class);
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        String dateString = (String) source;
        CustomDateFormat annotation = targetType.getAnnotation(CustomDateFormat.class);
        String pattern = annotation.pattern();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateString, formatter);
    }
}

// Custom annotation for date formatting
@interface CustomDateFormat {
    String pattern() default "yyyy-MM-dd";
}

/**
 * Example 2: Type-Condition Based Converter
 * Convert only if source matches specific pattern
 */
@Component
class PatternBasedConverter implements GenericConverter, ConditionalConverter {
    
    private static final String ISO_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";
    private static final String ISO_DATETIME_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new java.util.HashSet<>();
        pairs.add(new ConvertiblePair(String.class, LocalDate.class));
        pairs.add(new ConvertiblePair(String.class, LocalDateTime.class));
        return pairs;
    }
    
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        // This converter only handles ISO format strings
        // Let other converters handle different formats
        return true; // Could check source value pattern here
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        String value = (String) source;
        
        if (targetType.getType() == LocalDate.class) {
            if (value.matches(ISO_DATE_PATTERN)) {
                return LocalDate.parse(value);
            }
        } else if (targetType.getType() == LocalDateTime.class) {
            if (value.matches(ISO_DATETIME_PATTERN)) {
                return LocalDateTime.parse(value);
            }
        }
        
        return null; // Let other converters handle non-ISO formats
    }
}

/**
 * Example 3: Non-Empty String Converter
 * Only convert non-empty strings
 */
@Component
class NonEmptyStringConverter implements GenericConverter, ConditionalConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new java.util.HashSet<>();
        pairs.add(new ConvertiblePair(String.class, Integer.class));
        pairs.add(new ConvertiblePair(String.class, Long.class));
        pairs.add(new ConvertiblePair(String.class, Double.class));
        return pairs;
    }
    
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        // Only apply converter if dealing with non-empty strings
        // This prevents unnecessary conversion attempts
        return true; // Actual check done in convert method
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        String value = ((String) source).trim();
        if (value.isEmpty()) {
            return null; // Don't convert empty strings
        }
        
        Class<?> targetClass = targetType.getType();
        
        try {
            if (targetClass == Integer.class) {
                return Integer.valueOf(value);
            } else if (targetClass == Long.class) {
                return Long.valueOf(value);
            } else if (targetClass == Double.class) {
                return Double.valueOf(value);
            }
        } catch (NumberFormatException e) {
            // Return null instead of throwing exception
            return null;
        }
        
        return null;
    }
}

/**
 * Example 4: Collection Element Type Converter
 * Only convert if collection has specific element type
 */
@Component
class CollectionElementTypeConverter implements GenericConverter, ConditionalConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(
            new ConvertiblePair(java.util.Collection.class, java.util.List.class)
        );
    }
    
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        // Only apply if source has String elements and target expects Integer elements
        TypeDescriptor sourceElement = sourceType.getElementTypeDescriptor();
        TypeDescriptor targetElement = targetType.getElementTypeDescriptor();
        
        if (sourceElement == null || targetElement == null) {
            return false;
        }
        
        return sourceElement.getType() == String.class && 
               targetElement.getType() == Integer.class;
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        java.util.Collection<String> sourceCollection = (java.util.Collection<String>) source;
        java.util.List<Integer> targetList = new java.util.ArrayList<>();
        
        for (String element : sourceCollection) {
            try {
                targetList.add(Integer.parseInt(element));
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }
        
        return targetList;
    }
}

/**
 * Example 5: Environment-Based Conditional Converter
 * Only apply in specific environments
 */
@Component
class EnvironmentAwareConverter implements GenericConverter, ConditionalConverter {
    
    private final boolean developmentMode;
    
    public EnvironmentAwareConverter() {
        // In real app, inject Environment bean
        this.developmentMode = true; // Simulated
    }
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, String.class));
    }
    
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        // Only apply in development mode
        return developmentMode && targetType.hasAnnotation(DevOnly.class);
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        // Add development-specific processing
        return "[DEV] " + source;
    }
}

@interface DevOnly {}

/**
 * Example 6: Lenient Conversion Conditional Converter
 * Apply lenient conversion only if annotation present
 */
@Component
class LenientConversionConverter implements GenericConverter, ConditionalConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new java.util.HashSet<>();
        pairs.add(new ConvertiblePair(String.class, Integer.class));
        pairs.add(new ConvertiblePair(String.class, Boolean.class));
        return pairs;
    }
    
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        // Only apply if target field is marked as lenient
        return targetType.hasAnnotation(LenientConversion.class);
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return getDefaultValue(targetType.getType());
        }
        
        String value = ((String) source).trim();
        Class<?> targetClass = targetType.getType();
        
        if (targetClass == Integer.class) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return 0; // Default value for lenient conversion
            }
        } else if (targetClass == Boolean.class) {
            // Lenient boolean conversion
            return "true".equalsIgnoreCase(value) || 
                   "yes".equalsIgnoreCase(value) || 
                   "1".equals(value) ||
                   "on".equalsIgnoreCase(value);
        }
        
        return null;
    }
    
    private Object getDefaultValue(Class<?> type) {
        if (type == Integer.class) return 0;
        if (type == Boolean.class) return false;
        return null;
    }
}

@interface LenientConversion {}

/**
 * Example 7: Priority-Based Converter
 * Use specific converter based on priority annotation
 */
@Component
class PriorityConverter implements GenericConverter, ConditionalConverter {
    
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Integer.class));
    }
    
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        // Only apply if high priority conversion requested
        if (targetType.hasAnnotation(ConversionPriority.class)) {
            ConversionPriority priority = targetType.getAnnotation(ConversionPriority.class);
            return priority.value().equals("HIGH");
        }
        return false;
    }
    
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        
        // High-priority conversion with validation
        String value = ((String) source).trim();
        
        try {
            int result = Integer.parseInt(value);
            
            // Additional validation for high-priority conversions
            if (result < 0) {
                throw new IllegalArgumentException("Negative values not allowed");
            }
            
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + value, e);
        }
    }
}

@interface ConversionPriority {
    String value() default "NORMAL";
}

/**
 * Usage Examples
 */
class ConditionalConverterUsageExamples {
    
    public static void main(String[] args) {
        org.springframework.core.convert.support.DefaultConversionService service = 
            new org.springframework.core.convert.support.DefaultConversionService();
        
        // Register conditional converters
        service.addConverter(new ConditionalConverterPattern());
        service.addConverter(new PatternBasedConverter());
        service.addConverter(new NonEmptyStringConverter());
        service.addConverter(new LenientConversionConverter());
        
        // Example 1: Conversion with matching condition
        System.out.println("Conditional conversion examples:");
        
        // Example 2: Pattern-based conversion
        LocalDate date = service.convert("2024-01-15", LocalDate.class);
        System.out.println("ISO Date: " + date);
        
        // Example 3: Non-empty string conversion
        Integer number = service.convert("123", Integer.class);
        System.out.println("Number: " + number);
        
        // Example 4: Empty string returns null
        Integer emptyResult = service.convert("", Integer.class);
        System.out.println("Empty string result: " + emptyResult);
    }
}

/**
 * Usage Summary:
 * =============
 * 
 * 1. Implement ConditionalConverter:
 *    class MyConverter implements GenericConverter, ConditionalConverter {
 *        @Override
 *        public boolean matches(TypeDescriptor source, TypeDescriptor target) {
 *            return target.hasAnnotation(MyAnnotation.class);
 *        }
 *    }
 * 
 * 2. Check Annotations:
 *    boolean hasAnnotation = targetType.hasAnnotation(MyAnnotation.class);
 *    MyAnnotation annotation = targetType.getAnnotation(MyAnnotation.class);
 * 
 * 3. Check Type Characteristics:
 *    TypeDescriptor elementType = targetType.getElementTypeDescriptor();
 *    Class<?> type = targetType.getType();
 * 
 * 4. Return False to Skip:
 *    @Override
 *    public boolean matches(...) {
 *        if (someCondition) return false; // Skip this converter
 *        return true;
 *    }
 * 
 * Benefits:
 * ========
 * - Fine-grained control over converter application
 * - Multiple converters for same type pair
 * - Annotation-driven conversion logic
 * - Performance optimization (skip unnecessary conversions)
 * - Flexible converter selection
 * - Clear separation of concerns
 * 
 * Common Use Cases:
 * ================
 * - Format-specific conversions (@DateFormat, @NumberFormat)
 * - Lenient vs strict conversion modes
 * - Environment-specific conversions
 * - Collection element type conversions
 * - Validation-based conversions
 * - Priority-based converter selection
 */
