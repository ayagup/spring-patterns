package com.example.kotlin.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 KOTLIN DATA CLASS PATTERN 💡
 * ================================
 * 
 * Kotlin data classes with Spring Data JPA, Jackson, and validation.
 * Data classes provide automatic equals(), hashCode(), toString(),
 * copy(), and destructuring.
 * 
 * 🎯 KEY FEATURES:
 * - @Entity with data class
 * - Immutable entities
 * - Jackson serialization
 * - Validation support
 * - copy() for updates
 * - Component N destructuring
 * 
 * Example (Kotlin):
 * -----------------
 * @Entity
 * data class User(
 *     @Id val id: String,
 *     val name: String,
 *     val email: String,
 *     @Version val version: Long = 0
 * )
 * 
 * @author Spring Patterns
 * @since 2024-01-20
 */
@SpringBootApplication
public class DataClassPattern {
    public static void main(String[] args) {
        SpringApplication.run(DataClassPattern.class, args);
    }
}

@Service
class DataClassService {
    public List<String> getDataClassFeatures() {
        return Arrays.asList(
            "Automatic equals(), hashCode(), toString()",
            "copy() method for immutable updates",
            "Destructuring: val (id, name) = user",
            "Jackson serialization without annotations",
            "JPA @Entity support with all-open plugin",
            "Validation with @Valid annotation",
            "Default parameter values",
            "Named parameters for construction"
        );
    }
}

@RestController
@RequestMapping("/api/data-class")
class DataClassController {
    private final DataClassService service;
    
    public DataClassController(DataClassService service) {
        this.service = service;
    }
    
    @GetMapping("/features")
    public List<String> getFeatures() {
        return service.getDataClassFeatures();
    }
}
