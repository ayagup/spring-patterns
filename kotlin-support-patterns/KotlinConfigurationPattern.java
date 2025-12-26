package com.example.kotlin.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 KOTLIN CONFIGURATION PATTERN 💡
 * ===================================
 * 
 * Kotlin @Configuration classes with data classes for
 * ConfigurationProperties and functional bean definitions.
 * 
 * 🎯 KEY FEATURES:
 * - @ConfigurationProperties with data class
 * - Default parameter values
 * - Nullable property support
 * - @ConstructorBinding for immutability
 * - Bean DSL for functional config
 * - Nested configuration classes
 * 
 * Example (Kotlin):
 * -----------------
 * @ConfigurationProperties("app")
 * data class AppProperties(
 *     val name: String = "MyApp",
 *     val version: String = "1.0",
 *     val database: DatabaseConfig = DatabaseConfig()
 * )
 * 
 * data class DatabaseConfig(
 *     val url: String = "jdbc:h2:mem:testdb",
 *     val maxPoolSize: Int = 10
 * )
 * 
 * @Configuration
 * class AppConfig {
 *     @Bean
 *     fun dataSource(props: AppProperties): DataSource {
 *         return HikariDataSource().apply {
 *             jdbcUrl = props.database.url
 *             maximumPoolSize = props.database.maxPoolSize
 *         }
 *     }
 * }
 * 
 * @author Spring Patterns
 * @since 2024-01-20
 */
@SpringBootApplication
public class KotlinConfigurationPattern {
    public static void main(String[] args) {
        SpringApplication.run(KotlinConfigurationPattern.class, args);
    }
}

@Service
class KotlinConfigurationService {
    public List<String> getConfigurationFeatures() {
        return Arrays.asList(
            "@ConfigurationProperties with data class",
            "Default parameter values",
            "Nullable properties (String?)",
            "@ConstructorBinding for immutable config",
            "Nested configuration objects",
            "Type-safe property binding",
            "Validation with @Valid",
            "Bean DSL for functional configuration",
            "Profile-specific beans with when",
            "Environment property access with env[]"
        );
    }
    
    public Map<String, String> getExamples() {
        Map<String, String> examples = new LinkedHashMap<>();
        
        examples.put("ConfigurationProperties", 
            "@ConfigurationProperties(\"app\")\n" +
            "data class AppProps(\n" +
            "    val name: String = \"MyApp\",\n" +
            "    val timeout: Duration = Duration.ofSeconds(30)\n" +
            ")");
        
        examples.put("Bean Configuration", 
            "@Configuration\n" +
            "class Config {\n" +
            "    @Bean\n" +
            "    fun restTemplate() = RestTemplate()\n" +
            "}");
        
        examples.put("Conditional Bean", 
            "@Bean\n" +
            "@ConditionalOnProperty(\"feature.enabled\")\n" +
            "fun featureService() = FeatureService()");
        
        return examples;
    }
}

@RestController
@RequestMapping("/api/kotlin-configuration")
class KotlinConfigurationController {
    private final KotlinConfigurationService service;
    
    public KotlinConfigurationController(KotlinConfigurationService service) {
        this.service = service;
    }
    
    @GetMapping("/features")
    public List<String> getFeatures() {
        return service.getConfigurationFeatures();
    }
    
    @GetMapping("/examples")
    public Map<String, String> getExamples() {
        return service.getExamples();
    }
}
