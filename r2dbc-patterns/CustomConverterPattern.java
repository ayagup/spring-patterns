package com.example.r2dbc.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Custom Converter Pattern
 * 
 * Demonstrates custom type converters for R2DBC.
 * Converts between database types and Java types.
 * 
 * Key Features:
 * - Reading converters (DB → Java)
 * - Writing converters (Java → DB)
 * - Custom type mapping
 * - Enum conversion
 * - Complex type handling
 */
@SpringBootApplication
public class CustomConverterPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(CustomConverterPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Custom Converter Pattern ===\n");
        demonstrateConverters();
    }

    private void demonstrateConverters() {
        System.out.println("Custom Converters:");

        System.out.println("\n1. Reading Converter (DB → Java):");
        System.out.println("   @ReadingConverter");
        System.out.println("   class StringToEnumConverter implements Converter<String, Status> {");
        System.out.println("       public Status convert(String source) {");
        System.out.println("           return Status.valueOf(source);");
        System.out.println("       }");
        System.out.println("   }");

        System.out.println("\n2. Writing Converter (Java → DB):");
        System.out.println("   @WritingConverter");
        System.out.println("   class EnumToStringConverter implements Converter<Status, String> {");
        System.out.println("       public String convert(Status source) {");
        System.out.println("           return source.name();");
        System.out.println("       }");
        System.out.println("   }");

        System.out.println("\n3. Registration:");
        System.out.println("   @Bean");
        System.out.println("   public R2dbcCustomConversions customConversions() {");
        System.out.println("       return new R2dbcCustomConversions(Arrays.asList(");
        System.out.println("           new StringToEnumConverter(),");
        System.out.println("           new EnumToStringConverter()");
        System.out.println("       ));");
        System.out.println("   }");

        System.out.println("\n4. Use Cases:");
        System.out.println("   - Enum conversion");
        System.out.println("   - Date/time formatting");
        System.out.println("   - JSON column mapping");
        System.out.println("   - Custom value objects");
        System.out.println("   - Encryption/decryption");
    }

    // Example converters
    @ReadingConverter
    static class StringToStatusConverter implements Converter<String, Status> {
        @Override
        public Status convert(String source) {
            return Status.valueOf(source.toUpperCase());
        }
    }

    @WritingConverter
    static class StatusToStringConverter implements Converter<Status, String> {
        @Override
        public String convert(Status source) {
            return source.name();
        }
    }

    @Bean
    public R2dbcCustomConversions customConversions() {
        return new R2dbcCustomConversions(Arrays.asList(
            new StringToStatusConverter(),
            new StatusToStringConverter()
        ));
    }

    enum Status {
        ACTIVE, INACTIVE, PENDING
    }

    static class User {
        private Long id;
        private String name;
        private Status status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
    }
}
