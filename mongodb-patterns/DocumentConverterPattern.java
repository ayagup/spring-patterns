package com.example.mongodb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Document Converter Pattern
 * 
 * Demonstrates custom converters for MongoDB document transformations.
 * 
 * Converter Types:
 * - ReadingConverter: Database → Java Object
 * - WritingConverter: Java Object → Database
 * - Bidirectional converters
 * 
 * Use Cases:
 * - Custom type mapping
 * - Date/time conversions
 * - Enum conversions
 * - Complex object transformations
 * - Legacy data format handling
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class DocumentConverterPattern {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new LocalDateToStringConverter());
        converters.add(new StringToLocalDateConverter());
        converters.add(new StatusToStringConverter());
        converters.add(new StringToStatusConverter());
        return new MongoCustomConversions(converters);
    }
}

enum Status {
    ACTIVE, INACTIVE, PENDING, SUSPENDED
}

@WritingConverter
class LocalDateToStringConverter implements Converter<LocalDate, String> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public String convert(LocalDate source) {
        return source.format(FORMATTER);
    }
}

@ReadingConverter
class StringToLocalDateConverter implements Converter<String, LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate convert(String source) {
        return LocalDate.parse(source, FORMATTER);
    }
}

@WritingConverter
class StatusToStringConverter implements Converter<Status, String> {
    @Override
    public String convert(Status source) {
        return source.name().toLowerCase();
    }
}

@ReadingConverter
class StringToStatusConverter implements Converter<String, Status> {
    @Override
    public Status convert(String source) {
        return Status.valueOf(source.toUpperCase());
    }
}

@RestController
@RequestMapping("/api/mongo/converters")
class ConverterInfoController {

    @GetMapping("/info")
    public ResponseEntity<ConverterInfo> getInfo() {
        return ResponseEntity.ok(new ConverterInfo(
            "Document Converter Pattern",
            "Custom converters for MongoDB document transformations",
            "1.0",
            List.of("LocalDate → String", "String → LocalDate", "Status enum conversion"),
            List.of("Custom type mapping", "Date conversions", "Enum handling", "Legacy format")
        ));
    }

    record ConverterInfo(String name, String description, String version,
                        List<String> converters, List<String> useCases) {}
}
