package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
import org.springframework.boot.system.JavaVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Java Pattern
 * ===========================
 * 
 * Demonstrates @ConditionalOnJava annotation that creates beans based on
 * the Java version running the application. This enables Java version-specific
 * features and optimizations.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnJava - Bean registration based on Java version
 * 2. Version Comparison - Check Java runtime version
 * 3. Version-Specific Features - Enable features by Java version
 * 4. Range Conditions - Minimum, maximum, or range of versions
 * 5. Modern Java Features - Use new APIs conditionally
 * 
 * How It Works:
 * ------------
 * - Checks current JVM version at runtime
 * - Compares against specified version using range operator
 * - Range operators: EQUAL_OR_NEWER, OLDER_THAN
 * - JavaVersion enum: EIGHT, NINE, TEN, ELEVEN, TWELVE, etc.
 * - Condition evaluated at configuration processing time
 * 
 * Range Operators:
 * ---------------
 * - ConditionalOnJava.Range.EQUAL_OR_NEWER - Version >= specified
 * - ConditionalOnJava.Range.OLDER_THAN - Version < specified
 * 
 * Common Use Cases:
 * ----------------
 * - Enable Java 11+ features (modules, var, HTTP/2)
 * - Use Java 17+ records, sealed classes, pattern matching
 * - Optimize for specific Java versions
 * - Provide fallbacks for older Java versions
 * - Enable modern garbage collectors
 * - Use new APIs conditionally
 * 
 * Syntax:
 * ------
 * @ConditionalOnJava(JavaVersion.ELEVEN)
 * @ConditionalOnJava(range = Range.EQUAL_OR_NEWER, value = JavaVersion.SEVENTEEN)
 * @ConditionalOnJava(range = Range.OLDER_THAN, value = JavaVersion.ELEVEN)
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Java 11+ Features
 */
@Configuration
class Java11Configuration {
    
    /**
     * Enable features requiring Java 11 or newer
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.ELEVEN
    )
    public String java11Features() {
        System.out.println("Creating Java 11+ Features");
        System.out.println("  Using: HTTP/2 Client, Local-Variable Syntax");
        return "Java 11 Features";
    }
    
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.ELEVEN
    )
    public String httpClientService() {
        System.out.println("Creating HTTP Client Service (Java 11+ HTTP/2 Client)");
        return "HTTP Client Service";
    }
}

/**
 * Example 2: Java 17+ Modern Features
 */
@Configuration
class Java17Configuration {
    
    /**
     * Enable features requiring Java 17 or newer
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.SEVENTEEN
    )
    public String java17Features() {
        System.out.println("Creating Java 17+ Features");
        System.out.println("  Using: Records, Sealed Classes, Pattern Matching");
        return "Java 17 Features";
    }
    
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.SEVENTEEN
    )
    public String modernDataStructures() {
        System.out.println("Creating Modern Data Structures");
        System.out.println("  Using Java 17 Records for immutable DTOs");
        return "Modern Data Structures";
    }
}

/**
 * Example 3: Legacy Java Support
 */
@Configuration
class LegacyJavaConfiguration {
    
    /**
     * Fallback for Java versions older than 11
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.OLDER_THAN,
        value = JavaVersion.ELEVEN
    )
    public String legacyHttpClient() {
        System.out.println("Creating Legacy HTTP Client (Java < 11)");
        System.out.println("  Using Apache HttpClient instead of java.net.http");
        return "Legacy HTTP Client";
    }
}

/**
 * Example 4: JVM Optimization
 */
@Configuration
class JVMOptimizationConfiguration {
    
    /**
     * Enable modern GC features for Java 11+
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.ELEVEN
    )
    public String zgcConfiguration() {
        System.out.println("Creating ZGC Configuration (Java 11+)");
        System.out.println("  Z Garbage Collector available");
        return "ZGC Configuration";
    }
    
    /**
     * Enable G1GC tuning for Java 9+
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.NINE
    )
    public String g1gcTuning() {
        System.out.println("Creating G1GC Tuning (Java 9+)");
        return "G1GC Tuning";
    }
}

/**
 * Example 5: Module System (JPMS)
 */
@Configuration
class ModuleSystemConfiguration {
    
    /**
     * Enable Java Platform Module System features (Java 9+)
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.NINE
    )
    public String moduleSystemSupport() {
        System.out.println("Creating Module System Support (Java 9+)");
        System.out.println("  JPMS (Project Jigsaw) available");
        return "Module System Support";
    }
}

/**
 * Example 6: Performance Features
 */
@Configuration
class PerformanceConfiguration {
    
    /**
     * Enable compact strings (Java 9+)
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.NINE
    )
    public String compactStringsOptimization() {
        System.out.println("Creating Compact Strings Optimization (Java 9+)");
        System.out.println("  Automatic string compression enabled");
        return "Compact Strings Optimization";
    }
    
    /**
     * Enable JEP 280: String concatenation improvements (Java 9+)
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.NINE
    )
    public String stringConcatenationOptimizer() {
        System.out.println("Creating String Concatenation Optimizer");
        return "String Concatenation Optimizer";
    }
}

/**
 * Example 7: API Version-Specific Services
 */
@Configuration
class APIVersionConfiguration {
    
    /**
     * Use modern Process API (Java 9+)
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.NINE
    )
    public String modernProcessAPI() {
        System.out.println("Creating Modern Process API (Java 9+)");
        System.out.println("  Using ProcessHandle API");
        return "Modern Process API";
    }
    
    /**
     * Use Stream API enhancements (Java 9+)
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.NINE
    )
    public String enhancedStreamAPI() {
        System.out.println("Creating Enhanced Stream API (Java 9+)");
        System.out.println("  Using takeWhile, dropWhile, ofNullable");
        return "Enhanced Stream API";
    }
}

/**
 * Example 8: Security Features
 */
@Configuration
class SecurityVersionConfiguration {
    
    /**
     * Use modern security features (Java 11+)
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.ELEVEN
    )
    public String modernSecurityProvider() {
        System.out.println("Creating Modern Security Provider (Java 11+)");
        System.out.println("  Using ChaCha20-Poly1305, TLS 1.3");
        return "Modern Security Provider";
    }
}

/**
 * Example 9: Text Blocks (Java 15+)
 */
@Configuration
class TextBlocksConfiguration {
    
    /**
     * Enable text blocks feature (Java 15+)
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.FIFTEEN
    )
    public String textBlocksSupport() {
        System.out.println("Creating Text Blocks Support (Java 15+)");
        System.out.println("  Multi-line string literals available");
        return "Text Blocks Support";
    }
}

/**
 * Example 10: Foreign Function & Memory API (Java 17+)
 */
@Configuration
class ForeignFunctionConfiguration {
    
    /**
     * Enable Foreign Function & Memory API (Java 17+)
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.SEVENTEEN
    )
    public String foreignFunctionAPI() {
        System.out.println("Creating Foreign Function API Support (Java 17+)");
        System.out.println("  Panama API (Project Panama) available");
        return "Foreign Function API";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConditionalOnJavaPattern {
    
    /**
     * Example: Enable modern features for Java 11+
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.EQUAL_OR_NEWER,
        value = JavaVersion.ELEVEN
    )
    public String modernJavaFeatures() {
        System.out.println("Creating Modern Java Features Bundle");
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  Features: HTTP/2, var syntax, improved Stream API");
        return "Modern Java Features Bundle";
    }
    
    /**
     * Example: Legacy support for older Java versions
     */
    @Bean
    @ConditionalOnJava(
        range = ConditionalOnJava.Range.OLDER_THAN,
        value = JavaVersion.ELEVEN
    )
    public String legacyJavaSupport() {
        System.out.println("Creating Legacy Java Support");
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  Providing compatibility layer for older Java");
        return "Legacy Java Support";
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConditionalOnJavaUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Java Pattern");
        System.out.println("============================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Create beans based on Java runtime version");
        System.out.println("- Enable version-specific features");
        System.out.println("- Provide fallbacks for older versions\n");
        
        System.out.println("Syntax:");
        System.out.println("@ConditionalOnJava(");
        System.out.println("  range = Range.EQUAL_OR_NEWER,");
        System.out.println("  value = JavaVersion.ELEVEN");
        System.out.println(")\n");
        
        System.out.println("Range Options:");
        System.out.println("1. EQUAL_OR_NEWER - Version >= specified");
        System.out.println("2. OLDER_THAN - Version < specified\n");
        
        System.out.println("Java Versions:");
        System.out.println("- JavaVersion.EIGHT (Java 8)");
        System.out.println("- JavaVersion.NINE (Java 9)");
        System.out.println("- JavaVersion.ELEVEN (Java 11 LTS)");
        System.out.println("- JavaVersion.SEVENTEEN (Java 17 LTS)");
        System.out.println("- JavaVersion.TWENTY_ONE (Java 21 LTS)\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Enable Java 11+ HTTP/2 Client");
        System.out.println("2. Use Java 17+ Records");
        System.out.println("3. Enable Z Garbage Collector (Java 11+)");
        System.out.println("4. Use Module System (Java 9+)");
        System.out.println("5. Pattern Matching (Java 14+)");
        System.out.println("6. Text Blocks (Java 15+)");
        System.out.println("7. Sealed Classes (Java 17+)\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Target LTS versions (8, 11, 17, 21)");
        System.out.println("- Provide fallbacks for older versions");
        System.out.println("- Document minimum Java version");
        System.out.println("- Test on multiple Java versions");
        System.out.println("- Use for significant version differences");
        System.out.println("- Consider maintaining compatibility\n");
        
        System.out.println("Example Pattern:");
        System.out.println("// Modern features for Java 17+");
        System.out.println("@ConditionalOnJava(EQUAL_OR_NEWER, SEVENTEEN)");
        System.out.println("public RecordBasedService modernService() {...}\n");
        
        System.out.println("// Fallback for older Java");
        System.out.println("@ConditionalOnJava(OLDER_THAN, SEVENTEEN)");
        System.out.println("public ClassBasedService legacyService() {...}");
    }
}
