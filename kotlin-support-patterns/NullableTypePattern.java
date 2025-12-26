package com.example.kotlin.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 KOTLIN NULLABLE TYPE PATTERN 💡
 * ===================================
 * 
 * Kotlin null-safety integration with Spring Framework.
 * Spring APIs respect Kotlin's nullable/non-nullable types.
 * 
 * 🎯 KEY FEATURES:
 * - @Nullable/@NotNull annotations respected
 * - Platform types (Type!) from Java
 * - Safe call operator (?.)
 * - Elvis operator (?:)
 * - findByIdOrNull() extensions
 * - Null-safe configuration properties
 * 
 * @author Spring Patterns
 * @since 2024-01-20
 */
@SpringBootApplication
public class NullableTypePattern {
    public static void main(String[] args) {
        SpringApplication.run(NullableTypePattern.class, args);
    }
}

@Service
class NullableTypeService {
    public List<String> getNullSafetyFeatures() {
        return Arrays.asList(
            "repository.findByIdOrNull(id) - Returns null if not found",
            "val name: String? = user?.name - Safe call",
            "val name = user?.name ?: \"Default\" - Elvis operator",
            "val length = name!!.length - Non-null assertion",
            "@ConfigurationProperties respects nullable types",
            "ServerRequest.queryParamOrNull(\"name\") - Nullable query param",
            "ResponseEntity.ofNullable(value) - Nullable response"
        );
    }
}

@RestController
@RequestMapping("/api/nullable-type")
class NullableTypeController {
    private final NullableTypeService service;
    
    public NullableTypeController(NullableTypeService service) {
        this.service = service;
    }
    
    @GetMapping("/features")
    public List<String> getFeatures() {
        return service.getNullSafetyFeatures();
    }
}
