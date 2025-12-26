package com.example.conversion;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * Converting Comparator Pattern
 * ==============================
 * 
 * Demonstrates comparators that convert values before comparison.
 * Useful for sorting collections of one type by converted values.
 * 
 * Key Concepts:
 * ------------
 * 1. ConvertingComparator - Comparator with built-in conversion
 * 2. Converter Integration - Use converters in comparisons
 * 3. Sort by Converted Value - Compare based on transformation
 * 4. Type-Safe Comparisons - Compile-time type safety
 * 
 * Usage Example:
 * ConvertingComparator<String, Integer> comp = 
 *     new ConvertingComparator<>(String::length);
 * list.sort(comp);
 * 
 * @author Spring Patterns  
 * @version 1.0
 */

@Component
public class ConvertingComparatorPattern<S, T> implements Comparator<S> {
    
    private final Converter<S, T> converter;
    private final Comparator<T> comparator;
    
    public ConvertingComparatorPattern(Converter<S, T> converter, Comparator<T> comparator) {
        this.converter = converter;
        this.comparator = comparator;
    }
    
    @Override
    public int compare(S o1, S o2) {
        T t1 = converter.convert(o1);
        T t2 = converter.convert(o2);
        return comparator.compare(t1, t2);
    }
    
    // Examples
    public static void main(String[] args) {
        java.util.List<String> list = java.util.Arrays.asList("apple", "pie", "banana");
        
        // Sort by string length
        ConvertingComparatorPattern<String, Integer> lengthComp = 
            new ConvertingComparatorPattern<>(String::length, Integer::compare);
        list.sort(lengthComp);
        System.out.println(list); // [pie, apple, banana]
    }
}
